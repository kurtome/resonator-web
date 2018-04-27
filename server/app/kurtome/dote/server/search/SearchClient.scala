package kurtome.dote.server.search

import javax.inject._
import play.api.Configuration
import wvlet.log.LogSupport
import com.sksamuel.elastic4s._
import com.sksamuel.elastic4s.http._
import com.trueaccord.scalapb.json.JsonFormat
import kurtome.dote.proto.api.dotable.Dotable
import kurtome.dote.proto.db.dotable.DotableData
import kurtome.dote.proto.db.dotable.SearchIndexedData
import kurtome.dote.proto.db.dotable.SearchIndexedData.IndexedDotable
import org.apache.http.auth.AuthScope
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.client.config.RequestConfig.Builder
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder
import org.elasticsearch.client.RestClientBuilder.HttpClientConfigCallback
import org.elasticsearch.client.RestClientBuilder.RequestConfigCallback
import org.json4s._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SearchClient @Inject()(configuration: Configuration)(implicit ec: ExecutionContext)
    extends LogSupport {

  import com.sksamuel.elastic4s.http.ElasticDsl._

  private val esUrl =
    configuration
      .get[String]("kurtome.dote.search.elasticsearch.url")
      .replace("https://", "")
      .replace("http://", "")

  private val esPwd =
    configuration.get[String]("kurtome.dote.search.elasticsearch.pwd")

  private val esPort =
    configuration.get[Int]("kurtome.dote.search.elasticsearch.port")

  private val esSsl =
    configuration.get[String]("kurtome.dote.search.elasticsearch.ssl")

  private val clientUri =
    ElasticsearchClientUri(null, List((esUrl, esPort)), Map("ssl" -> esSsl))

  lazy val provider = {
    val provider = new BasicCredentialsProvider
    val credentials = new UsernamePasswordCredentials("elastic", esPwd)
    provider.setCredentials(AuthScope.ANY, credentials)
    provider
  }
  val client = HttpClient(
    clientUri,
    new RequestConfigCallback {
      override def customizeRequestConfig(requestConfigBuilder: Builder) = {
        requestConfigBuilder
      }
    },
    new HttpClientConfigCallback {
      override def customizeHttpClient(httpClientBuilder: HttpAsyncClientBuilder) = {
        httpClientBuilder.setDefaultCredentialsProvider(provider)
      }
    }
  )

  private val dotablesIndex = "dotables"
  private val docType = "_doc"

  // This cannot alter existing mappings, only create new ones.
  private def ensureIndexSchema(index: String) = {
    client.execute {
      createIndex(index)
    } flatMap { _ =>
      client.execute {
        putMapping(index / docType) fields (
          objectField("dotable").dynamic(false),
          objectField("dotable.data").enabled(false),
          keywordField("dotable.id"),
          keywordField("dotable.kind"),
          keywordField("dotable.slug"),
          objectField("parent").dynamic(false),
          objectField("parent.data").enabled(false),
          keywordField("parent.id"),
          keywordField("parent.kind"),
          keywordField("parent.slug"),
          objectField("indexedFields").dynamic(false),
          textField("indexedFields.title"),
          textField("indexedFields.parentTitle"),
          textField("indexedFields.combinedText").analyzer("english"),
        )

      }
    }
  }

  ensureIndexSchema(dotablesIndex)

  def indexDotables(dotables: Seq[Dotable]): Future[Unit] = {
    client.execute {
      bulk(
        dotables.map(
          dotable =>
            indexInto(dotablesIndex / docType)
              .id(dotable.id)
              .doc(extractDataDoc(dotable, dotable.getRelatives.parent)))
      )
    } map {
      case Right(requestSuccess) => Unit
      case Left(requestFailure)  => warn(requestFailure.body)
    } map (_ => Unit)
  }

  @deprecated
  def indexPodcastWithEpisodes(podcast: Dotable): Future[Unit] = {
    val episodeParent = Some(podcast)
    client.execute {
      bulk(
        Seq(
          indexInto(dotablesIndex / docType)
            .id(podcast.id)
            .doc(extractDataDoc(podcast, None))
        ) ++ podcast.getRelatives.children.map(
          episode =>
            indexInto(dotablesIndex / docType)
              .id(episode.id)
              .doc(extractDataDoc(episode, episodeParent)))
      )
    } map {
      case Right(requestSuccess) => Unit
      case Left(requestFailure)  => warn(requestFailure.body)
    } map (_ => Unit)
  }

  def searchAll(query: String, offset: Int = 0, limit: Int = 30): Future[Seq[Dotable]] = {
    implicit val formats = DefaultFormats
    client.execute {
      search(dotablesIndex)
        .from(offset)
        .size(limit)
        .query(
          boolQuery()
            .must(
              boolQuery().should(
                matchQuery("indexedFields.combinedText", query)
                  .fuzziness("AUTO")
                  .prefixLength(3),
                matchPhrasePrefixQuery("indexedFields.title", query)
                  .boost(2),
                matchPhrasePrefixQuery("indexedFields.parentTitle", query)
                  .boost(3)
              )
            )
        )
    } map {
      case Right(requestSuccess) => {
        requestSuccess.result.hits.hits.map(resultData => {
          val indexedData = JsonFormat.fromJsonString[SearchIndexedData](resultData.sourceAsString)
          parseDataDoc(indexedData)
        })
      }
      case Left(requestFailure) => {
        warn(requestFailure.body)
        Nil
      }
    }
  }

  def searchPodcast(query: String, offset: Int = 0, limit: Int = 30): Future[Seq[Dotable]] = {
    implicit val formats = DefaultFormats
    client.execute {
      search(dotablesIndex)
        .from(offset)
        .size(limit)
        .query(
          boolQuery()
            .must(
              matchQuery("indexedFields.combinedText", query)
                .minimumShouldMatch("90%")
                .fuzziness("AUTO")
                .prefixLength(3))
            .should(
              matchQuery("indexedFields.title", query)
                .minimumShouldMatch("60%")
                .boost(200)
            )
            .filter(termQuery("dotable.kind", "PODCAST"))
        )
    } map {
      case Right(requestSuccess) => {
        requestSuccess.result.hits.hits.map(resultData => {
          val indexedData = JsonFormat.fromJsonString[SearchIndexedData](resultData.sourceAsString)
          parseDataDoc(indexedData)
        })
      }
      case Left(requestFailure) => {
        warn(requestFailure.body)
        Nil
      }
    }
  }

  def searchEpisode(query: String, offset: Int = 0, limit: Int = 30): Future[Seq[Dotable]] = {
    implicit val formats = DefaultFormats
    client.execute {
      search(dotablesIndex)
        .from(offset)
        .size(limit)
        .query(
          boolQuery()
            .must(
              matchQuery("indexedFields.combinedText", query)
                .minimumShouldMatch("100%")
                .fuzziness("AUTO")
                .prefixLength(3)
            )
            .should(
              matchQuery("indexedFields.parentTitle", query)
                .minimumShouldMatch("60%")
                .boost(100)
            )
            .filter(termQuery("dotable.kind", "PODCAST_EPISODE"))
        )
    } map {
      case Right(requestSuccess) => {
        requestSuccess.result.hits.hits.map(resultData => {
          val indexedData = JsonFormat.fromJsonString[SearchIndexedData](resultData.sourceAsString)
          parseDataDoc(indexedData)
        })
      }
      case Left(requestFailure) => {
        warn(requestFailure.body)
        Nil
      }
    }
  }

  private def extractType(dotable: Dotable): String = {
    dotable.kind.toString
  }

  private def parseDataDoc(indexedData: SearchIndexedData): Dotable = {
    val data = indexedData.getDotable
    parseIndexedDotable(indexedData.getDotable, indexedData.parent)
  }

  private def parseIndexedDotable(data: IndexedDotable, parent: Option[IndexedDotable]): Dotable = {
    Dotable(
      id = data.id,
      slug = data.slug,
      kind = Dotable.Kind.fromName(data.kind).get,
      common = data.getData.common,
      details = data.getData.details
    ).withRelatives(Dotable.Relatives(parentFetched = parent.isDefined,
                                      parent = parent.map(parseIndexedDotable(_, None))))
  }

  private def extractDataDoc(dotable: Dotable, parentDotable: Option[Dotable]): String = {
    val title = dotable.getCommon.title
    val description = truncateDescription(dotable.getCommon.description)
    val parentTitle = parentDotable.map(_.getCommon.title).getOrElse("")
    val tags = dotable.getTagCollection.tags.map(_.displayValue).mkString(" ")
    val combinedText = Seq(title, parentTitle, description, tags).mkString("\n")
    JsonFormat.toJsonString(
      SearchIndexedData(dotable = Some(toIndexedDotable(dotable)),
                        parent = parentDotable.map(toIndexedDotable))
        .withIndexedFields(SearchIndexedData
          .IndexedFields(title = title, combinedText = combinedText, parentTitle = parentTitle)))
  }

  private def truncateDescription(description: String): String = {
    if (description.length > 1000) {
      val rawTruncated = description.substring(0, 1000)
      // drop any partial words after the last space
      rawTruncated.substring(0, rawTruncated.lastIndexOf(' '))
    } else {
      description
    }
  }

  private def toIndexedDotable(dotable: Dotable): IndexedDotable = {
    IndexedDotable(id = dotable.id,
                   slug = dotable.slug,
                   kind = dotable.kind.toString,
                   data = Some(DotableData(common = dotable.common, details = dotable.details)))
  }

}
