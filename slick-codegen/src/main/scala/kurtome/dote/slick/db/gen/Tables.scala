package kurtome.dote.slick.db.gen
// AUTO-GENERATED Slick data model
/** Stand-alone Slick data model for immediate use */
object Tables extends {
  val profile = kurtome.dote.slick.db.DotePostgresProfile
} with Tables

/** Slick data model trait for extension, choice of backend or usage in the cake pattern. (Make sure to initialize this late.) */
trait Tables {
  val profile: kurtome.dote.slick.db.DotePostgresProfile
  import profile.api._
  import slick.model.ForeignKeyAction
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import slick.jdbc.{GetResult => GR}

  /** DDL for all tables. Call .create to execute. */
  lazy val schema: profile.SchemaDescription = Array(Dotable.schema,
                                                     DotableTag.schema,
                                                     PlayEvolutions.schema,
                                                     PodcastEpisodeIngestion.schema,
                                                     PodcastFeedIngestion.schema,
                                                     Tag.schema).reduceLeft(_ ++ _)
  @deprecated("Use .schema instead of .ddl", "3.0")
  def ddl = schema

  /** Entity class storing rows of table Dotable
    *  @param id Database column id SqlType(bigserial), AutoInc, PrimaryKey
    *  @param kind Database column kind SqlType(dotablekind)
    *  @param title Database column title SqlType(text), Length(2147483647,true)
    *  @param parentId Database column parent_id SqlType(int8)
    *  @param data Database column data SqlType(jsonb) */
  case class DotableRow(id: Long,
                        kind: kurtome.dote.slick.db.DotableKinds.Value,
                        title: Option[String],
                        parentId: Option[Long],
                        data: org.json4s.JsonAST.JValue)

  /** GetResult implicit for fetching DotableRow objects using plain SQL queries */
  implicit def GetResultDotableRow(implicit e0: GR[Long],
                                   e1: GR[kurtome.dote.slick.db.DotableKinds.Value],
                                   e2: GR[Option[String]],
                                   e3: GR[Option[Long]],
                                   e4: GR[org.json4s.JsonAST.JValue]): GR[DotableRow] = GR { prs =>
    import prs._
    DotableRow.tupled(
      (<<[Long],
       <<[kurtome.dote.slick.db.DotableKinds.Value],
       <<?[String],
       <<?[Long],
       <<[org.json4s.JsonAST.JValue]))
  }

  /** Table description of table dotable. Objects of this class serve as prototypes for rows in queries. */
  class Dotable(_tableTag: slick.lifted.Tag)
      extends profile.api.Table[DotableRow](_tableTag, "dotable") {
    def * = (id, kind, title, parentId, data) <> (DotableRow.tupled, DotableRow.unapply)

    /** Maps whole row to an option. Useful for outer joins. */
    def ? =
      (Rep.Some(id), Rep.Some(kind), title, parentId, Rep.Some(data)).shaped.<>({ r =>
        import r._; _1.map(_ => DotableRow.tupled((_1.get, _2.get, _3, _4, _5.get)))
      }, (_: Any) => throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(bigserial), AutoInc, PrimaryKey */
    val id: Rep[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)

    /** Database column kind SqlType(dotablekind) */
    val kind: Rep[kurtome.dote.slick.db.DotableKinds.Value] =
      column[kurtome.dote.slick.db.DotableKinds.Value]("kind")

    /** Database column title SqlType(text), Length(2147483647,true) */
    val title: Rep[Option[String]] =
      column[Option[String]]("title", O.Length(2147483647, varying = true))

    /** Database column parent_id SqlType(int8) */
    val parentId: Rep[Option[Long]] = column[Option[Long]]("parent_id")

    /** Database column data SqlType(jsonb) */
    val data: Rep[org.json4s.JsonAST.JValue] = column[org.json4s.JsonAST.JValue]("data")

    /** Foreign key referencing Dotable (database name dotable_parent_id_fkey) */
    lazy val dotableFk = foreignKey("dotable_parent_id_fkey", parentId, Dotable)(
      r => Rep.Some(r.id),
      onUpdate = ForeignKeyAction.NoAction,
      onDelete = ForeignKeyAction.NoAction)

    /** Index over (kind,id) (database name dotable_kind_id_index) */
    val index1 = index("dotable_kind_id_index", (kind, id))
  }

  /** Collection-like TableQuery object for table Dotable */
  lazy val Dotable = new TableQuery(tag => new Dotable(tag))

  /** Entity class storing rows of table DotableTag
    *  @param tagId Database column tag_id SqlType(int8)
    *  @param dotableId Database column dotable_id SqlType(int8) */
  case class DotableTagRow(tagId: Long, dotableId: Long)

  /** GetResult implicit for fetching DotableTagRow objects using plain SQL queries */
  implicit def GetResultDotableTagRow(implicit e0: GR[Long]): GR[DotableTagRow] = GR { prs =>
    import prs._
    DotableTagRow.tupled((<<[Long], <<[Long]))
  }

  /** Table description of table dotable_tag. Objects of this class serve as prototypes for rows in queries. */
  class DotableTag(_tableTag: slick.lifted.Tag)
      extends profile.api.Table[DotableTagRow](_tableTag, "dotable_tag") {
    def * = (tagId, dotableId) <> (DotableTagRow.tupled, DotableTagRow.unapply)

    /** Maps whole row to an option. Useful for outer joins. */
    def ? =
      (Rep.Some(tagId), Rep.Some(dotableId)).shaped.<>({ r =>
        import r._; _1.map(_ => DotableTagRow.tupled((_1.get, _2.get)))
      }, (_: Any) => throw new Exception("Inserting into ? projection not supported."))

    /** Database column tag_id SqlType(int8) */
    val tagId: Rep[Long] = column[Long]("tag_id")

    /** Database column dotable_id SqlType(int8) */
    val dotableId: Rep[Long] = column[Long]("dotable_id")

    /** Foreign key referencing Dotable (database name dotable_tag_dotable_id_fkey) */
    lazy val dotableFk = foreignKey("dotable_tag_dotable_id_fkey", dotableId, Dotable)(
      r => r.id,
      onUpdate = ForeignKeyAction.NoAction,
      onDelete = ForeignKeyAction.NoAction)

    /** Foreign key referencing Tag (database name dotable_tag_tag_id_fkey) */
    lazy val tagFk = foreignKey("dotable_tag_tag_id_fkey", tagId, Tag)(
      r => r.id,
      onUpdate = ForeignKeyAction.NoAction,
      onDelete = ForeignKeyAction.NoAction)

    /** Uniqueness Index over (tagId,dotableId) (database name dotable_tag_id_uniq_index) */
    val index1 = index("dotable_tag_id_uniq_index", (tagId, dotableId), unique = true)
  }

  /** Collection-like TableQuery object for table DotableTag */
  lazy val DotableTag = new TableQuery(tag => new DotableTag(tag))

  /** Entity class storing rows of table PlayEvolutions
    *  @param id Database column id SqlType(int4), PrimaryKey
    *  @param hash Database column hash SqlType(varchar), Length(255,true)
    *  @param appliedAt Database column applied_at SqlType(timestamp)
    *  @param applyScript Database column apply_script SqlType(text), Length(2147483647,true)
    *  @param revertScript Database column revert_script SqlType(text), Length(2147483647,true)
    *  @param state Database column state SqlType(varchar), Length(255,true)
    *  @param lastProblem Database column last_problem SqlType(text), Length(2147483647,true) */
  case class PlayEvolutionsRow(id: Int,
                               hash: String,
                               appliedAt: java.time.LocalDateTime,
                               applyScript: Option[String],
                               revertScript: Option[String],
                               state: Option[String],
                               lastProblem: Option[String])

  /** GetResult implicit for fetching PlayEvolutionsRow objects using plain SQL queries */
  implicit def GetResultPlayEvolutionsRow(implicit e0: GR[Int],
                                          e1: GR[String],
                                          e2: GR[java.time.LocalDateTime],
                                          e3: GR[Option[String]]): GR[PlayEvolutionsRow] = GR {
    prs =>
      import prs._
      PlayEvolutionsRow.tupled(
        (<<[Int],
         <<[String],
         <<[java.time.LocalDateTime],
         <<?[String],
         <<?[String],
         <<?[String],
         <<?[String]))
  }

  /** Table description of table play_evolutions. Objects of this class serve as prototypes for rows in queries. */
  class PlayEvolutions(_tableTag: slick.lifted.Tag)
      extends profile.api.Table[PlayEvolutionsRow](_tableTag, "play_evolutions") {
    def * =
      (id, hash, appliedAt, applyScript, revertScript, state, lastProblem) <> (PlayEvolutionsRow.tupled, PlayEvolutionsRow.unapply)

    /** Maps whole row to an option. Useful for outer joins. */
    def ? =
      (Rep.Some(id),
       Rep.Some(hash),
       Rep.Some(appliedAt),
       applyScript,
       revertScript,
       state,
       lastProblem).shaped.<>(
        { r =>
          import r._;
          _1.map(_ => PlayEvolutionsRow.tupled((_1.get, _2.get, _3.get, _4, _5, _6, _7)))
        },
        (_: Any) => throw new Exception("Inserting into ? projection not supported.")
      )

    /** Database column id SqlType(int4), PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.PrimaryKey)

    /** Database column hash SqlType(varchar), Length(255,true) */
    val hash: Rep[String] = column[String]("hash", O.Length(255, varying = true))

    /** Database column applied_at SqlType(timestamp) */
    val appliedAt: Rep[java.time.LocalDateTime] = column[java.time.LocalDateTime]("applied_at")

    /** Database column apply_script SqlType(text), Length(2147483647,true) */
    val applyScript: Rep[Option[String]] =
      column[Option[String]]("apply_script", O.Length(2147483647, varying = true))

    /** Database column revert_script SqlType(text), Length(2147483647,true) */
    val revertScript: Rep[Option[String]] =
      column[Option[String]]("revert_script", O.Length(2147483647, varying = true))

    /** Database column state SqlType(varchar), Length(255,true) */
    val state: Rep[Option[String]] = column[Option[String]]("state", O.Length(255, varying = true))

    /** Database column last_problem SqlType(text), Length(2147483647,true) */
    val lastProblem: Rep[Option[String]] =
      column[Option[String]]("last_problem", O.Length(2147483647, varying = true))
  }

  /** Collection-like TableQuery object for table PlayEvolutions */
  lazy val PlayEvolutions = new TableQuery(tag => new PlayEvolutions(tag))

  /** Entity class storing rows of table PodcastEpisodeIngestion
    *  @param id Database column id SqlType(bigserial), AutoInc, PrimaryKey
    *  @param podcastDotableId Database column podcast_dotable_id SqlType(int8)
    *  @param guid Database column guid SqlType(text), Length(2147483647,true)
    *  @param episodeDotableId Database column episode_dotable_id SqlType(int8) */
  case class PodcastEpisodeIngestionRow(id: Long,
                                        podcastDotableId: Long,
                                        guid: String,
                                        episodeDotableId: Long)

  /** GetResult implicit for fetching PodcastEpisodeIngestionRow objects using plain SQL queries */
  implicit def GetResultPodcastEpisodeIngestionRow(
      implicit e0: GR[Long],
      e1: GR[String]): GR[PodcastEpisodeIngestionRow] = GR { prs =>
    import prs._
    PodcastEpisodeIngestionRow.tupled((<<[Long], <<[Long], <<[String], <<[Long]))
  }

  /** Table description of table podcast_episode_ingestion. Objects of this class serve as prototypes for rows in queries. */
  class PodcastEpisodeIngestion(_tableTag: slick.lifted.Tag)
      extends profile.api.Table[PodcastEpisodeIngestionRow](_tableTag, "podcast_episode_ingestion") {
    def * =
      (id, podcastDotableId, guid, episodeDotableId) <> (PodcastEpisodeIngestionRow.tupled, PodcastEpisodeIngestionRow.unapply)

    /** Maps whole row to an option. Useful for outer joins. */
    def ? =
      (Rep.Some(id), Rep.Some(podcastDotableId), Rep.Some(guid), Rep.Some(episodeDotableId)).shaped
        .<>(
          { r =>
            import r._;
            _1.map(_ => PodcastEpisodeIngestionRow.tupled((_1.get, _2.get, _3.get, _4.get)))
          },
          (_: Any) => throw new Exception("Inserting into ? projection not supported.")
        )

    /** Database column id SqlType(bigserial), AutoInc, PrimaryKey */
    val id: Rep[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)

    /** Database column podcast_dotable_id SqlType(int8) */
    val podcastDotableId: Rep[Long] = column[Long]("podcast_dotable_id")

    /** Database column guid SqlType(text), Length(2147483647,true) */
    val guid: Rep[String] = column[String]("guid", O.Length(2147483647, varying = true))

    /** Database column episode_dotable_id SqlType(int8) */
    val episodeDotableId: Rep[Long] = column[Long]("episode_dotable_id")

    /** Foreign key referencing Dotable (database name podcast_episode_ingestion_episode_dotable_id_fkey) */
    lazy val dotableFk1 =
      foreignKey("podcast_episode_ingestion_episode_dotable_id_fkey", episodeDotableId, Dotable)(
        r => r.id,
        onUpdate = ForeignKeyAction.NoAction,
        onDelete = ForeignKeyAction.NoAction)

    /** Foreign key referencing Dotable (database name podcast_episode_ingestion_podcast_dotable_id_fkey) */
    lazy val dotableFk2 =
      foreignKey("podcast_episode_ingestion_podcast_dotable_id_fkey", podcastDotableId, Dotable)(
        r => r.id,
        onUpdate = ForeignKeyAction.NoAction,
        onDelete = ForeignKeyAction.NoAction)

    /** Uniqueness Index over (episodeDotableId) (database name podcast_episode_ingestion_episode_dotable_id_key) */
    val index1 =
      index("podcast_episode_ingestion_episode_dotable_id_key", episodeDotableId, unique = true)

    /** Uniqueness Index over (podcastDotableId,guid) (database name podcast_episode_ingestion_podcast_episode_guid_uniq_index) */
    val index2 = index("podcast_episode_ingestion_podcast_episode_guid_uniq_index",
                       (podcastDotableId, guid),
                       unique = true)
  }

  /** Collection-like TableQuery object for table PodcastEpisodeIngestion */
  lazy val PodcastEpisodeIngestion = new TableQuery(tag => new PodcastEpisodeIngestion(tag))

  /** Entity class storing rows of table PodcastFeedIngestion
    *  @param id Database column id SqlType(bigserial), AutoInc, PrimaryKey
    *  @param feedRssUrl Database column feed_rss_url SqlType(text), Length(2147483647,true)
    *  @param itunesId Database column itunes_id SqlType(int8)
    *  @param podcastDotableId Database column podcast_dotable_id SqlType(int8)
    *  @param nextIngestionTime Database column next_ingestion_time SqlType(timestamp)
    *  @param lastFeedEtag Database column last_feed_etag SqlType(text), Length(2147483647,true) */
  case class PodcastFeedIngestionRow(id: Long,
                                     feedRssUrl: String,
                                     itunesId: Long,
                                     podcastDotableId: Option[Long],
                                     nextIngestionTime: java.time.LocalDateTime,
                                     lastFeedEtag: Option[String])

  /** GetResult implicit for fetching PodcastFeedIngestionRow objects using plain SQL queries */
  implicit def GetResultPodcastFeedIngestionRow(
      implicit e0: GR[Long],
      e1: GR[String],
      e2: GR[Option[Long]],
      e3: GR[java.time.LocalDateTime],
      e4: GR[Option[String]]): GR[PodcastFeedIngestionRow] = GR { prs =>
    import prs._
    PodcastFeedIngestionRow.tupled(
      (<<[Long], <<[String], <<[Long], <<?[Long], <<[java.time.LocalDateTime], <<?[String]))
  }

  /** Table description of table podcast_feed_ingestion. Objects of this class serve as prototypes for rows in queries. */
  class PodcastFeedIngestion(_tableTag: slick.lifted.Tag)
      extends profile.api.Table[PodcastFeedIngestionRow](_tableTag, "podcast_feed_ingestion") {
    def * =
      (id, feedRssUrl, itunesId, podcastDotableId, nextIngestionTime, lastFeedEtag) <> (PodcastFeedIngestionRow.tupled, PodcastFeedIngestionRow.unapply)

    /** Maps whole row to an option. Useful for outer joins. */
    def ? =
      (Rep.Some(id),
       Rep.Some(feedRssUrl),
       Rep.Some(itunesId),
       podcastDotableId,
       Rep.Some(nextIngestionTime),
       lastFeedEtag).shaped.<>(
        { r =>
          import r._;
          _1.map(_ => PodcastFeedIngestionRow.tupled((_1.get, _2.get, _3.get, _4, _5.get, _6)))
        },
        (_: Any) => throw new Exception("Inserting into ? projection not supported.")
      )

    /** Database column id SqlType(bigserial), AutoInc, PrimaryKey */
    val id: Rep[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)

    /** Database column feed_rss_url SqlType(text), Length(2147483647,true) */
    val feedRssUrl: Rep[String] =
      column[String]("feed_rss_url", O.Length(2147483647, varying = true))

    /** Database column itunes_id SqlType(int8) */
    val itunesId: Rep[Long] = column[Long]("itunes_id")

    /** Database column podcast_dotable_id SqlType(int8) */
    val podcastDotableId: Rep[Option[Long]] = column[Option[Long]]("podcast_dotable_id")

    /** Database column next_ingestion_time SqlType(timestamp) */
    val nextIngestionTime: Rep[java.time.LocalDateTime] =
      column[java.time.LocalDateTime]("next_ingestion_time")

    /** Database column last_feed_etag SqlType(text), Length(2147483647,true) */
    val lastFeedEtag: Rep[Option[String]] =
      column[Option[String]]("last_feed_etag", O.Length(2147483647, varying = true))

    /** Foreign key referencing Dotable (database name podcast_feed_ingestion_podcast_dotable_id_fkey) */
    lazy val dotableFk =
      foreignKey("podcast_feed_ingestion_podcast_dotable_id_fkey", podcastDotableId, Dotable)(
        r => Rep.Some(r.id),
        onUpdate = ForeignKeyAction.NoAction,
        onDelete = ForeignKeyAction.NoAction)

    /** Uniqueness Index over (feedRssUrl) (database name podcast_feed_ingestion_feed_rss_url_key) */
    val index1 = index("podcast_feed_ingestion_feed_rss_url_key", feedRssUrl, unique = true)

    /** Uniqueness Index over (itunesId) (database name podcast_feed_ingestion_itunes_id_key) */
    val index2 = index("podcast_feed_ingestion_itunes_id_key", itunesId, unique = true)

    /** Uniqueness Index over (podcastDotableId) (database name podcast_feed_ingestion_podcast_dotable_id_key) */
    val index3 =
      index("podcast_feed_ingestion_podcast_dotable_id_key", podcastDotableId, unique = true)
  }

  /** Collection-like TableQuery object for table PodcastFeedIngestion */
  lazy val PodcastFeedIngestion = new TableQuery(tag => new PodcastFeedIngestion(tag))

  /** Entity class storing rows of table Tag
    *  @param id Database column id SqlType(bigserial), AutoInc, PrimaryKey
    *  @param kind Database column kind SqlType(tagkind)
    *  @param key Database column key SqlType(text), Length(2147483647,true)
    *  @param name Database column name SqlType(text), Length(2147483647,true) */
  case class TagRow(id: Long,
                    kind: kurtome.dote.slick.db.TagKinds.Value,
                    key: String,
                    name: String)

  /** GetResult implicit for fetching TagRow objects using plain SQL queries */
  implicit def GetResultTagRow(implicit e0: GR[Long],
                               e1: GR[kurtome.dote.slick.db.TagKinds.Value],
                               e2: GR[String]): GR[TagRow] = GR { prs =>
    import prs._
    TagRow.tupled((<<[Long], <<[kurtome.dote.slick.db.TagKinds.Value], <<[String], <<[String]))
  }

  /** Table description of table tag. Objects of this class serve as prototypes for rows in queries. */
  class Tag(_tableTag: slick.lifted.Tag) extends profile.api.Table[TagRow](_tableTag, "tag") {
    def * = (id, kind, key, name) <> (TagRow.tupled, TagRow.unapply)

    /** Maps whole row to an option. Useful for outer joins. */
    def ? =
      (Rep.Some(id), Rep.Some(kind), Rep.Some(key), Rep.Some(name)).shaped.<>({ r =>
        import r._; _1.map(_ => TagRow.tupled((_1.get, _2.get, _3.get, _4.get)))
      }, (_: Any) => throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(bigserial), AutoInc, PrimaryKey */
    val id: Rep[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)

    /** Database column kind SqlType(tagkind) */
    val kind: Rep[kurtome.dote.slick.db.TagKinds.Value] =
      column[kurtome.dote.slick.db.TagKinds.Value]("kind")

    /** Database column key SqlType(text), Length(2147483647,true) */
    val key: Rep[String] = column[String]("key", O.Length(2147483647, varying = true))

    /** Database column name SqlType(text), Length(2147483647,true) */
    val name: Rep[String] = column[String]("name", O.Length(2147483647, varying = true))

    /** Uniqueness Index over (kind,key) (database name tag_kind_label_uniq_index) */
    val index1 = index("tag_kind_label_uniq_index", (kind, key), unique = true)
  }

  /** Collection-like TableQuery object for table Tag */
  lazy val Tag = new TableQuery(tag => new Tag(tag))
}
