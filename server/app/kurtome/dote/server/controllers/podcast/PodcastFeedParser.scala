package kurtome.dote.server.controllers.podcast

import java.time.ZonedDateTime
import java.util.Locale
import javax.inject._

import dote.proto.api.action.add_podcast.AddPodcastRequest.Extras
import dote.proto.db.dotable.{DotableCommon, DotableDetails, ExternalUrls}
import kurtome.dote.server.db.{MetadataFlag, Tag}
import kurtome.dote.server.util.Slug
import kurtome.dote.slick.db.TagKinds
import play.Logger

import scala.util.{Failure, Try}
import scala.xml._

@Singleton
class PodcastFeedParser @Inject()() {

  //val pubDateFormatter = DateTimeFormatter.RFC_1123_DATE_TIME
  //val pubDateFormatter2 = DateTimeFormatter.RFC_1123_DATE_TIME.withResolverFields()

  def parsePodcastRss(itunesUrl: String,
                      feedUrl: String,
                      extras: Extras,
                      rssXml: String): Seq[RssFetchedPodcast] = {
    val channels = XML.loadString(rssXml) \ "channel"
    channels.map(parsePodcast(itunesUrl, feedUrl, extras, _))
  }

  private def parsePodcast(itunesUrl: String,
                           feedUrl: String,
                           extras: Extras,
                           podcast: Node): RssFetchedPodcast = {
    val title: String = podcast \ "title"
    val description: String = podcast \ "description"

    val itunesImagesWithHref =
      (podcast \ "image").filter(n => n.attribute("href").isDefined && n.namespace == "itunes")
    val imagesWithHref = (podcast \ "image").filter(n => n.attribute("href").isDefined)
    val imageUrl = if (itunesImagesWithHref.size > 0) {
      // Prefer the iTunes image as it is more often up to date
      itunesImagesWithHref \@ "href"
    } else {
      imagesWithHref \@ "href"
    }

    val websiteUrl: String = podcast \ "link"

    val rawLanguage: String = podcast \ "language"
    // split "en-US" into "en" and "US"
    val languageParts = rawLanguage.split("-")
    val languageCode: String = if (languageParts.size > 0) languageParts(0) else ""
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
    val episodes = episodeNodes map parseEpisode reverse

    val tags: Seq[Tag] =
      Seq(Tag(TagKinds.PodcastCreator, Slug(author), author)) ++
        categories.map(category => Tag(TagKinds.PodcastGenre, Slug(category), category)) ++
        keywords.map(keyword => Tag(TagKinds.Keyword, Slug(keyword), keyword)) ++
        maybeSeq(extras.popular, Tag(MetadataFlag.Ids.popular, "Popular"))

    RssFetchedPodcast(
      feedUrl = feedUrl,
      tags = tags,
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
    val title: String = episode \ "title"
    val description: String = episode \ "summary"
    val author: String = episode \ "author"
    val guid: String = episode \ "guid"
    val pubDateEpochSec =
      Try(ZonedDateTime.from(RichRfc1123DateTimeParser.parse(episode \ "pubDate")).toEpochSecond).toOption
    val duration = parseDurationAsSeconds(episode \ "duration")
    val episodeNum = Try(Integer.parseInt(episode \ "episode")).toOption
    val explicit: Boolean = parseExplicit(episode \ "explicit")

    RssFetchedEpisode(
      common = DotableCommon(title = title,
                             description = description,
                             publishedEpochSec = pubDateEpochSec.getOrElse(0),
                             updatedEpochSec = pubDateEpochSec.getOrElse(0)),
      details = DotableDetails.PodcastEpisode(
        durationSec = duration.getOrElse(0),
        episodeNumber = episodeNum.getOrElse(0),
        rssGuid = guid
      )
    )
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
          if (parts.size == 2) {
            val minutes = Integer.parseInt(parts(0))
            val seconds = Integer.parseInt(parts(1))
            (minutes * 60) + seconds
          } else if (parts.size == 3) {
            val hours = Integer.parseInt(parts(0))
            val minutes = Integer.parseInt(parts(1))
            val seconds = Integer.parseInt(parts(2))
            (hours * 3600) + (minutes * 60) + seconds
          } else {
            throw new IllegalStateException(s"Unable to parse $raw")
          }
        } else {
          // Assume its raw number of seconds
          Integer.parseInt(raw)
        }
      )

      tryParse match {
        case Failure(t) => Logger.warn(s"couldn't parse '$raw' as int")
        case _ =>
      }

      tryParse.toOption
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

  /**
    * Automatically convert a node seq into the text content of the *first* element in the seq.
    * This is useful when selecting a node which should be a singleton with text.
    */
  private implicit def nodeseq2text(nodes: NodeSeq): String = {
    nodes
    // ignore empty elements in case there are duplicates
      .filter(!_.text.isEmpty)
      // Use the first element, in case there are duplicates
      .headOption
      .map(_.text)
      // If there are no elements matching, use empty string
      .getOrElse("")
      // remove extraneous white space, newlines etc.
      .trim
  }
}
