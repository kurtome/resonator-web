package resonator.server.search

import javax.inject._
import play.api.Configuration
import wvlet.log.LogSupport
import com.sksamuel.elastic4s._
import com.sksamuel.elastic4s.http._
import com.trueaccord.scalapb.json.JsonFormat
import resonator.proto.api.dotable.Dotable
import resonator.proto.api.tag.Tag
import resonator.proto.db.dotable.DotableData
import resonator.proto.db.dotable.SearchIndexedData
import resonator.proto.db.dotable.SearchIndexedData.IndexedDotable
import org.apache.http.auth.AuthScope
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.client.config.RequestConfig.Builder
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder
import org.elasticsearch.client.RestClientBuilder.HttpClientConfigCallback
import org.elasticsearch.client.RestClientBuilder.RequestConfigCallback

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SearchClient @Inject()(configuration: Configuration)(implicit ec: ExecutionContext)
    extends LogSupport {

  private val stopWords = Seq(
    "pod",
    "podcast",
    "cast",
    "episode",
    "a",
    "an",
    "and",
    "are",
    "as",
    "at",
    "be",
    "but",
    "by",
    "for",
    "if",
    "in",
    "into",
    "is",
    "it",
    "no",
    "not",
    "of",
    "on",
    "or",
    "such",
    "that",
    "the",
    "their",
    "then",
    "there",
    "these",
    "they",
    "this",
    "to",
    "was",
    "will",
    "with"
  )

  import com.sksamuel.elastic4s.http.ElasticDsl._

//  private val esUrl =
//    configuration
//      .get[String]("resonator.search.elasticsearch.url")
//      .replace("https://", "")
//      .replace("http://", "")

//  private val esPwd =
//    configuration.get[String]("resonator.search.elasticsearch.pwd")

//  private val esPort =
//    configuration.get[Int]("resonator.search.elasticsearch.port")

//  private val esSsl =
//    configuration.get[String]("resonator.search.elasticsearch.ssl")

//  private val clientUri =
//    ElasticsearchClientUri(null, List((esUrl, esPort)), Map("ssl" -> esSsl))

//  lazy val provider = {
//    val provider = new BasicCredentialsProvider
//    val credentials = new UsernamePasswordCredentials("elastic", esPwd)
//    provider.setCredentials(AuthScope.ANY, credentials)
//    provider
//  }
//  val client = HttpClient(
//    clientUri,
//    new RequestConfigCallback {
//      override def customizeRequestConfig(requestConfigBuilder: Builder) = {
//        requestConfigBuilder
//      }
//    },
//    new HttpClientConfigCallback {
//      override def customizeHttpClient(httpClientBuilder: HttpAsyncClientBuilder) = {
//        httpClientBuilder.setDefaultCredentialsProvider(provider)
//      }
//    }
//  )

  private val dotablesIndex = "dotables-v2"
  private val docType = "_doc"

  // This cannot alter existing mappings, only create new ones.
  private def ensureIndexSchema(index: String) = {
    Future.unit
//    client.execute {
//      createIndex(index)
//    } flatMap { _ =>
//      client.execute {
//        putMapping(index / docType) fields (
//          objectField("dotable").dynamic(false),
//          objectField("dotable.data").enabled(false),
//          keywordField("dotable.id"),
//          keywordField("dotable.kind"),
//          keywordField("dotable.slug"),
//          objectField("parent").dynamic(false),
//          objectField("parent.data").enabled(false),
//          keywordField("parent.id"),
//          keywordField("parent.kind"),
//          keywordField("parent.slug"),
//          objectField("indexedFields").dynamic(false),
//          textField("indexedFields.title"),
//          textField("indexedFields.parentTitle"),
//          textField("indexedFields.combinedText").termVector("yes").analyzer("english"),
//          keywordField("indexedFields.tagIds"),
//          textField("indexedFields.tagDisplayValues")
//        )
//
//      }
//    }
  }

  ensureIndexSchema(dotablesIndex)

  def indexDotables(dotables: Seq[Dotable]): Future[Unit] = {
    Future.unit
//    val toIndex = dotables
//      .filter(_.kind match {
//        case Dotable.Kind.PODCAST => true
//        case Dotable.Kind.PODCAST_EPISODE => true
//        case _ => false
//      })
//
//    if (toIndex.nonEmpty) {
//      client.execute {
//        bulk(
//          toIndex.map(
//            dotable =>
//              indexInto(dotablesIndex / docType)
//                .id(dotable.id)
//                .doc(extractDataDoc(dotable)))
//        )
//      } map {
//        case Right(requestSuccess) => Unit
//        case Left(requestFailure) => warn(requestFailure.body)
//      } map (_ => Unit)
//    } else {
//      Future.unit
//    }
  }

  def searchAll(query: String, offset: Int = 0, limit: Int = 30): Future[Seq[Dotable]] = {
    Future(Nil)
//    client.execute {
//      search(dotablesIndex)
//        .from(offset)
//        .size(limit)
//        .query(
//          boolQuery()
//            .filter(
//              matchQuery("indexedFields.combinedText", query)
//                .minimumShouldMatch("3<70%")
//                .fuzziness("AUTO")
//                .prefixLength(3)
//            )
//            .should(
//              matchPhraseQuery("indexedFields.combinedText", query)
//                .boost(2),
//              matchQuery("indexedFields.combinedText", query)
//                .minimumShouldMatch("100%")
//                .boost(1)
//                .fuzziness("AUTO")
//                .prefixLength(3),
//              matchQuery("indexedFields.parentTitle", query)
//                .boost(1),
//              matchQuery("indexedFields.tagDisplayValues", query)
//                .boost(50),
//              boolQuery()
//                .must(
//                  matchPhraseQuery("indexedFields.title", query),
//                  termQuery("dotable.kind", "PODCAST")
//                )
//                .boost(100)
//            )
//        )
//    } map {
//      case Right(requestSuccess) => {
//        requestSuccess.result.hits.hits.map(resultData => {
//          val indexedData = JsonFormat.fromJsonString[SearchIndexedData](resultData.sourceAsString)
//          parseDataDoc(indexedData)
//        })
//      }
//      case Left(requestFailure) => {
//        warn(requestFailure.body)
//        Nil
//      }
//    }
  }

  def searchPodcast(query: String, offset: Int = 0, limit: Int = 30): Future[Seq[Dotable]] = {
    Future(Nil)
//    client.execute {
//      search(dotablesIndex)
//        .from(offset)
//        .size(limit)
//        .query(
//          boolQuery()
//            .must(
//              matchQuery("indexedFields.combinedText", query)
//                .minimumShouldMatch("90%")
//                .fuzziness("AUTO")
//                .prefixLength(3))
//            .should(
//              matchQuery("indexedFields.title", query)
//                .minimumShouldMatch("60%")
//                .boost(200)
//            )
//            .filter(termQuery("dotable.kind", "PODCAST"))
//        )
//    } map {
//      case Right(requestSuccess) => {
//        requestSuccess.result.hits.hits.map(resultData => {
//          val indexedData = JsonFormat.fromJsonString[SearchIndexedData](resultData.sourceAsString)
//          parseDataDoc(indexedData)
//        })
//      }
//      case Left(requestFailure) => {
//        warn(requestFailure.body)
//        Nil
//      }
//    }
  }

  def searchEpisode(query: String, offset: Int = 0, limit: Int = 30): Future[Seq[Dotable]] = {
    Future(Nil)
//    client.execute {
//      search(dotablesIndex)
//        .from(offset)
//        .size(limit)
//        .query(
//          boolQuery()
//            .must(
//              matchQuery("indexedFields.combinedText", query)
//                .minimumShouldMatch("100%")
//                .fuzziness("AUTO")
//                .prefixLength(3)
//            )
//            .should(
//              matchQuery("indexedFields.parentTitle", query)
//                .minimumShouldMatch("60%")
//                .boost(100)
//            )
//            .filter(termQuery("dotable.kind", "PODCAST_EPISODE"))
//        )
//    } map {
//      case Right(requestSuccess) => {
//        requestSuccess.result.hits.hits.map(resultData => {
//          val indexedData = JsonFormat.fromJsonString[SearchIndexedData](resultData.sourceAsString)
//          parseDataDoc(indexedData)
//        })
//      }
//      case Left(requestFailure) => {
//        warn(requestFailure.body)
//        Nil
//      }
//    }
  }

  def moreLike(dotable: Dotable, offset: Int = 0, limit: Int = 24): Future[Seq[Dotable]] = {
    Future(Nil)
//    val tags = dotable.kind match {
//      case Dotable.Kind.PODCAST => dotable.getTagCollection.tags
//      case _ => dotable.getRelatives.getParent.getTagCollection.tags
//    }
//    val combinedText = extractIndexStruct(dotable).getIndexedFields.combinedText
//    if (tags.nonEmpty) {
//      client.execute {
//        search(dotablesIndex)
//          .from(offset)
//          .size(limit)
//          .query(
//            boolQuery()
//              .filter(
//                termQuery("dotable.kind", dotable.kind.toString)
//              )
//              .must(
//                moreLikeThisQuery("indexedFields.combinedText")
//                  .likeTexts(combinedText)
//                  .stopWords(stopWords)
//              )
//          )
//          .postFilter(not(termQuery("dotable.id", dotable.id)))
//      } map {
//        case Right(requestSuccess) => {
//          requestSuccess.result.hits.hits.map(resultData => {
//            val indexedData =
//              JsonFormat.fromJsonString[SearchIndexedData](resultData.sourceAsString)
//            parseDataDoc(indexedData)
//          })
//        }
//        case Left(requestFailure) => {
//          warn(requestFailure.body)
//          Nil
//        }
//      }
//    } else {
//      Future(Nil)
//    }
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

  private def extractTagId(tag: Tag): String = {
    s"${tag.getId.kind.toString}:${tag.getId.key}"
  }

  private def extractDataDoc(dotable: Dotable): String = {
    JsonFormat.toJsonString(extractIndexStruct(dotable))
  }

  private def extractIndexStruct(dotable: Dotable): SearchIndexedData = {
    val parentDotable = dotable.getRelatives.parent
    val title = dotable.getCommon.title
    val description = truncateDescription(dotable.getCommon.description)
    val parentTitle = parentDotable.map(_.getCommon.title).getOrElse("")
    val tags =
      (dotable.getTagCollection.tags ++ dotable.getRelatives.getParent.getTagCollection.tags).distinct
    val tagSnippet = tags.map(_.displayValue).mkString(" ")
    val combinedText = Seq(title, parentTitle, description, tagSnippet).mkString("\n")
    SearchIndexedData(dotable = Some(toIndexedDotable(dotable)),
                      parent = parentDotable.map(toIndexedDotable))
      .withIndexedFields(
        SearchIndexedData
          .IndexedFields(title = title,
                         combinedText = combinedText,
                         parentTitle = parentTitle,
                         tagDisplayValues = tags.map(_.displayValue),
                         tagIds = tags.map(extractTagId)))
  }

  private def truncateDescription(description: String): String = {
    if (description.length > 1000) {
      val rawTruncated = description.substring(0, 1000)
      // drop any partial words after the last space
      val lastSpaceIndex = rawTruncated.lastIndexOf(' ')

      if (lastSpaceIndex > 0) {
        rawTruncated.substring(0, lastSpaceIndex)
      } else {
        rawTruncated
      }
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
