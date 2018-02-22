package kurtome.dote.server.ingestion

import java.security.MessageDigest
import java.time.ZonedDateTime
import java.util.Locale

import javax.inject._
import kurtome.dote.proto.api.action.add_podcast.AddPodcastRequest.Extras
import kurtome.dote.proto.db.dotable.DotableDetails.PodcastEpisode.Audio
import kurtome.dote.proto.db.dotable.{DotableCommon, DotableDetails, ExternalUrls}
import kurtome.dote.server.model.{MetadataFlag, Tag}
import kurtome.dote.server.util.Slug
import kurtome.dote.slick.db.TagKinds
import wvlet.log.LogSupport

import scala.util.{Failure, Try}
import scala.xml._

@Singleton
class PodcastFeedParser @Inject()() extends LogSupport {

  private val sha256 = MessageDigest.getInstance("SHA-256")

  def parsePodcastRss(itunesUrl: String,
                      feedUrl: String,
                      feedEtag: Option[String],
                      feedDataHash: Array[Byte],
                      extras: Extras,
                      rssXml: String): Seq[RssFetchedPodcast] = {
    val channels = XML.loadString(rssXml) \ "channel"
    channels.map(parsePodcast(itunesUrl, feedUrl, feedEtag, feedDataHash, extras, _))
  }

  private def parsePodcast(itunesUrl: String,
                           feedUrl: String,
                           feedEtag: Option[String],
                           feedDataHash: Array[Byte],
                           extras: Extras,
                           podcast: Node): RssFetchedPodcast = {
    val title: String = podcast \ "title"
    val description: String = podcast \ "description"

    val itunesImagesWithHref =
      firstDefinedAttributeValue((podcast \ "image").filter(_.prefix == "itunes"), "href")
    val imageUrl = if (itunesImagesWithHref.isDefined) {
      debug("using itunes ns image")
      // Prefer the iTunes image as it is more often up to date
      itunesImagesWithHref.get
    } else {
      debug("using other image")
      val imagesWithHref = firstDefinedAttributeValue(podcast \ "image", "href")
      imagesWithHref.getOrElse("")
    }

    val websiteUrl: String = podcast \ "link"

    val rawLanguage: String = podcast \ "language"
    // split "en-US" into "en" and "US"
    val languageParts = rawLanguage.split("-")
    val languageCode: String = if (languageParts.nonEmpty) languageParts(0) else ""
    val countryCode: String = if (languageParts.size > 1) languageParts(1) else ""
    val locale: Option[Locale] = languageCode match {
      case "" => None
      case _ =>
        Try(new Locale(languageCode, countryCode.toUpperCase)).toOption
    }

    val categories: Seq[String] =
      ((podcast \ "category").map(nodeseq2text(_)) ++
        (podcast \ "category").map(_ \@ "text"))
        .map(_.trim)
        .filter(_.size > 2)
        .distinct
        // only the first category is actually representative of the category for most podcasts
        .take(1)

    val keywords: Seq[String] =
      (podcast \ "keywords")
        .map(nodeseq2text(_))
        .flatMap(_.split(","))
        .map(_.trim)
        .filter(_.size > 2)
        .distinct

    val author: String = podcast \ "author"

    val episodeNodes = podcast \ "item"
    val episodes = episodeNodes map parseEpisode sortBy { ep =>
      ep.common.publishedEpochSec
    }

    val tags: Seq[Tag] =
      Seq(Tag(TagKinds.PodcastCreator, Slug(author), author)) ++
        categories.map(category => Tag(TagKinds.PodcastGenre, Slug(category), category)) ++
        keywords.map(keyword => Tag(TagKinds.Keyword, Slug(keyword), keyword)) ++
        maybeSeq(extras.popular, Tag(MetadataFlag.Ids.popular, "Popular"))

    RssFetchedPodcast(
      feedUrl = feedUrl,
      tags = tags,
      feedEtag = feedEtag,
      dataHash = feedDataHash,
      common = DotableCommon(
        title = title,
        description = description,
        publishedEpochSec = episodes.headOption.map(_.common.publishedEpochSec).getOrElse(0),
        updatedEpochSec = episodes.lastOption.map(_.common.publishedEpochSec).getOrElse(0)
      ),
      details = DotableDetails.Podcast(
        websiteUrl = websiteUrl,
        author = author,
        imageUrl = imageUrl,
        externalUrls = Some(ExternalUrls(itunes = itunesUrl)),
        languageCode = languageCode,
        languageDisplay = locale.map(_.getDisplayLanguage).getOrElse("")
      ),
      episodes = episodes
    )
  }

  private def parseEpisode(episode: Node): RssFetchedEpisode = {
    val dataHash = sha256.digest(episode.buildString(false).getBytes)

    val title: String = episode \ "title"
    val summary: String = episode \ "summary"
    val description: String = if (summary.nonEmpty) summary else episode \ "description"
    val author: String = episode \ "author"
    val guid: String = episode \ "guid"
    val pubDateEpochSec =
      Try(ZonedDateTime.from(RichRfc1123DateTimeParser.parse(episode \ "pubDate")).toEpochSecond).toOption
    val duration = parseDurationAsSeconds(episode \ "duration")
    val episodeNum = Try(Integer.parseInt(episode \ "episode")).toOption
    val explicit: Boolean = parseExplicit(episode \ "explicit")
    val audio = parseAudioEnclosure(episode \ "enclosure")

    RssFetchedEpisode(
      common = DotableCommon(title = title,
                             description = description,
                             publishedEpochSec = pubDateEpochSec.getOrElse(0),
                             updatedEpochSec = pubDateEpochSec.getOrElse(0)),
      details = DotableDetails.PodcastEpisode(
        durationSec = duration.getOrElse(0),
        episodeNumber = episodeNum.getOrElse(0),
        rssGuid = guid,
        audio = audio
      ),
      dataHash = dataHash
    )
  }

  private def parseAudioEnclosure(enclosure: NodeSeq): Option[Audio] = {
    val url = enclosure \@ "url"
    val fileType = enclosure \@ "type"
    val sizeBytes = Try((enclosure \@ "length").toLong).getOrElse(0L)
    if (fileType.startsWith("audio/")) {
      Some(Audio(url, fileType, sizeBytes))
    } else {
      None
    }
  }

  private def parseDurationAsSeconds(raw: String): Option[Int] = {
    if (raw.isEmpty) {
      None
    } else {
      val tryParse = Try(
        //  Check for HH:MM:SS format
        if (raw.contains(':')) {
          // Assume it's either MM:SS or HH:MM:SS
          val parts = raw.split(':')
          if (parts.length == 2) {
            val minutes = parts(0).toDouble
            val seconds = parts(1).toDouble
            (minutes * 60) + seconds
          } else if (parts.length == 3) {
            val hours = parts(0).toDouble
            val minutes = parts(1).toDouble
            val seconds = parts(2).toDouble
            (hours * 3600) + (minutes * 60) + seconds
          } else {
            throw new IllegalStateException(s"Unable to parse $raw")
          }
        } else {
          // Assume its raw number of seconds
          raw.toDouble
        }
      )

      tryParse match {
        case Failure(t) => warn(s"couldn't parse '$raw' as int")
        case _ =>
      }

      tryParse.toOption.map(_.toInt)
    }
  }

  private def parseExplicit(s: String): Boolean = s match {
    case "yes" => true
    case _ => false
  }

  private def maybeSeq[T](cond: Boolean, x: T): Seq[T] = {
    if (cond) {
      Seq(x)
    } else {
      Nil
    }
  }

  private def firstDefinedAttributeValue(nodes: NodeSeq, attribute: String): Option[String] = {
    // jump through hoops to find the first node with a non-empty value for the attribute
    nodes.toStream
      .find(n => n.attribute(attribute).map(_.text).getOrElse("").nonEmpty)
      .map(_.attribute(attribute).get.text)
  }

  /**
    * Automatically convert a node seq into the text content of the *first* element in the seq.
    * This is useful when selecting a node which should be a singleton with text.
    */
  private implicit def nodeseq2text(nodes: NodeSeq): String = {
    nodes
    // ignore empty elements in case there are duplicates
    // Use the first element, in case there are duplicates
      .find(!_.text.isEmpty)
      .map(_.text)
      // If there are no elements matching, use empty string
      .getOrElse("")
      // remove extraneous white space, newlines etc.
      .trim
  }
}
