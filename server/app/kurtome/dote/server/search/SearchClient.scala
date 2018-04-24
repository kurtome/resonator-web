package kurtome.dote.server.search

import javax.inject._
import kurtome.dote.slick.db.gen.Tables
import org.matthicks.mailgun._
import play.api.Configuration
import wvlet.log.LogSupport
import com.sksamuel.elastic4s._
import com.sksamuel.elastic4s.http._
import com.sksamuel.elastic4s.http.search.queries.text.MatchQueryBuilderFn
import com.sksamuel.elastic4s.mappings.Analysis
import com.sksamuel.elastic4s.mappings.FieldDefinition
import com.sksamuel.elastic4s.mappings.JoinFieldDefinition
import com.sksamuel.elastic4s.mappings.MappingDefinition
import com.sksamuel.elastic4s.mappings.Nulls
import com.sksamuel.elastic4s.searches.ScoreMode
import com.sksamuel.elastic4s.searches.queries.BoolQueryDefinition
import com.sksamuel.elastic4s.searches.queries.HasChildQueryDefinition
import com.sksamuel.elastic4s.searches.queries.HasParentQueryDefinition
import com.sksamuel.elastic4s.searches.queries.InnerHitDefinition
import com.sksamuel.elastic4s.searches.queries.NestedQueryDefinition
import com.sksamuel.elastic4s.searches.queries.matches.MatchQueryDefinition
import com.sksamuel.elastic4s.searches.queries.term.TermQueryDefinition
import com.trueaccord.scalapb.json.JsonFormat
import kurtome.dote.proto.api.dotable.Dotable
import kurtome.dote.proto.db.dotable.DotableData
import kurtome.dote.proto.db.dotable.SearchIndexedData
import org.apache.http.auth.AuthScope
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.client.config.RequestConfig.Builder
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder
import org.elasticsearch.client.RestClientBuilder.HttpClientConfigCallback
import org.elasticsearch.client.RestClientBuilder.RequestConfigCallback
import org.json4s.JsonAST
import org.json4s.native.JsonParser
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.json4s.jackson.Serialization

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

  private val index = "dotable"

  def indexPodcastWithEpisodes(podcast: Dotable): Future[Unit] = {
    client.execute {
      bulk {
        Seq(
          indexInto(index / "_doc")
            .id(podcast.id)
            .doc(extractDataDoc(podcast))) ++
          (podcast.getRelatives.children map { episode =>
            indexInto(index / "_doc")
              .id(episode.id)
              .routing(podcast.id)
              .doc(extractDataDoc(episode, podcast.id))
          })
      }
    } map {
      case Right(requestSuccess) => Unit
      case Left(requestFailure) => warn(requestFailure.body)
    } map (_ => Unit)
  }

  def searchPodcast(query: String): Future[Seq[Dotable]] = {
    implicit val formats = DefaultFormats
    client.execute {
      search(index)
        .query {
          BoolQueryDefinition(
            must = Seq(
              BoolQueryDefinition(
                should = Seq(
                  MatchQueryDefinition("data.common.title",
                                       query,
                                       fuzziness = Some("AUTO"),
                                       prefixLength = Some(2)),
                  MatchQueryDefinition("data.common.description",
                                       query,
                                       fuzziness = Some("AUTO"),
                                       prefixLength = Some(2))
                )),
              MatchQueryDefinition("kind", "PODCAST")
            )
          )
        }
    } map {
      case Right(requestSuccess) => {
        requestSuccess.result.hits.hits.map(resultData => {
          val indexedData = JsonFormat.fromJsonString[SearchIndexedData](resultData.sourceAsString)
          val parent = parseDataDoc(indexedData)
          parent
        })
      }
      case Left(requestFailure) => {
        warn(requestFailure.body)
        Nil
      }
    }
  }

  def searchEpisode(query: String): Future[Seq[Dotable]] = {
    implicit val formats = DefaultFormats
    client.execute {
      search(index)
        .query {
          BoolQueryDefinition(
            should = Seq(
              MatchQueryDefinition("data.common.title",
                                   query,
                                   fuzziness = Some("AUTO"),
                                   prefixLength = Some(2)),
              HasChildQueryDefinition(
                `type` = "PODCAST_EPISODE",
                query = BoolQueryDefinition(
                  should = Seq(
                    MatchQueryDefinition("data.common.title",
                                         query,
                                         fuzziness = Some("AUTO"),
                                         prefixLength = Some(2)),
                    MatchQueryDefinition("data.common.description",
                                         query,
                                         fuzziness = Some("AUTO"),
                                         prefixLength = Some(2))
                  )
                ),
                scoreMode = ScoreMode.Avg,
                innerHit = Some(InnerHitDefinition("childJoin"))
              )
            )
          )
        }
    } map {
      case Right(requestSuccess) => {
        requestSuccess.result.hits.hits.flatMap(resultData => {
          val indexedData = JsonFormat.fromJsonString[SearchIndexedData](resultData.sourceAsString)
          val parent = parseDataDoc(indexedData)

          val children = resultData.innerHits
            .get("childJoin")
            .map(_.hits.map(innerHit => {
              val childData =
                JsonFormat.fromJsonString[SearchIndexedData](Serialization.write(innerHit.source))
              val child = parseDataDoc(childData).withRelatives(
                Dotable.Relatives(parentFetched = true).withParent(parent))
              child
            }))
            .getOrElse(Nil)
          children
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
    Dotable(
      id = indexedData.id,
      slug = indexedData.slug,
      kind = Dotable.Kind.fromName(indexedData.kind).get,
      common = indexedData.getData.common,
      details = indexedData.getData.details
    )
  }

  private def extractDataDoc(dotable: Dotable, parentId: String = ""): String = {
    JsonFormat.toJsonString(
      SearchIndexedData(id = dotable.id, slug = dotable.slug, kind = dotable.kind.toString)
        .withChildJoin(if (parentId.nonEmpty) {
          SearchIndexedData.ChildJoin(name = dotable.kind.toString, parent = parentId)
        } else {
          SearchIndexedData.ChildJoin(name = dotable.kind.toString)
        })
        .withData(DotableData(common = dotable.common, details = dotable.details)))
  }

}
