package resonator.slick.db.gen
// AUTO-GENERATED Slick data model
/** Stand-alone Slick data model for immediate use */
object Tables extends {
  val profile = resonator.slick.db.DotePostgresProfile
} with Tables

/** Slick data model trait for extension, choice of backend or usage in the cake pattern. (Make sure to initialize this late.) */
trait Tables {
  val profile: resonator.slick.db.DotePostgresProfile
  import profile.api._
  import slick.model.ForeignKeyAction
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import slick.jdbc.{GetResult => GR}

  /** DDL for all tables. Call .create to execute. */
  lazy val schema: profile.SchemaDescription = Array(
    AuthToken.schema,
    Dotable.schema,
    DotableTag.schema,
    Dote.schema,
    Follower.schema,
    Person.schema,
    PlayEvolutions.schema,
    PodcastEpisodeIngestion.schema,
    PodcastFeedIngestion.schema,
    RadioStation.schema,
    RadioStationPlaylist.schema,
    RadioStationPodcast.schema,
    SearchIndexQueue.schema,
    Tag.schema
  ).reduceLeft(_ ++ _)
  @deprecated("Use .schema instead of .ddl", "3.0")
  def ddl = schema

  /** Entity class storing rows of table AuthToken
    *  @param id Database column id SqlType(bigserial), AutoInc, PrimaryKey
    *  @param selector Database column selector SqlType(text), Length(2147483647,true)
    *  @param validator Database column validator SqlType(bytea)
    *  @param expirationTime Database column expiration_time SqlType(timestamp)
    *  @param personId Database column person_id SqlType(int8)
    *  @param dbUpdatedTime Database column db_updated_time SqlType(timestamp) */
  case class AuthTokenRow(id: Long,
                          selector: String,
                          validator: Array[Byte],
                          expirationTime: java.time.LocalDateTime,
                          personId: Long,
                          dbUpdatedTime: java.time.LocalDateTime)

  /** GetResult implicit for fetching AuthTokenRow objects using plain SQL queries */
  implicit def GetResultAuthTokenRow(implicit e0: GR[Long],
                                     e1: GR[String],
                                     e2: GR[Array[Byte]],
                                     e3: GR[java.time.LocalDateTime]): GR[AuthTokenRow] = GR {
    prs =>
      import prs._
      AuthTokenRow.tupled(
        (<<[Long],
         <<[String],
         <<[Array[Byte]],
         <<[java.time.LocalDateTime],
         <<[Long],
         <<[java.time.LocalDateTime]))
  }

  /** Table description of table auth_token. Objects of this class serve as prototypes for rows in queries. */
  class AuthToken(_tableTag: slick.lifted.Tag)
      extends profile.api.Table[AuthTokenRow](_tableTag, "auth_token") {
    def * =
      (id, selector, validator, expirationTime, personId, dbUpdatedTime) <> (AuthTokenRow.tupled, AuthTokenRow.unapply)

    /** Maps whole row to an option. Useful for outer joins. */
    def ? =
      (Rep.Some(id),
       Rep.Some(selector),
       Rep.Some(validator),
       Rep.Some(expirationTime),
       Rep.Some(personId),
       Rep.Some(dbUpdatedTime)).shaped.<>(
        { r =>
          import r._;
          _1.map(_ => AuthTokenRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get)))
        },
        (_: Any) => throw new Exception("Inserting into ? projection not supported.")
      )

    /** Database column id SqlType(bigserial), AutoInc, PrimaryKey */
    val id: Rep[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)

    /** Database column selector SqlType(text), Length(2147483647,true) */
    val selector: Rep[String] = column[String]("selector", O.Length(2147483647, varying = true))

    /** Database column validator SqlType(bytea) */
    val validator: Rep[Array[Byte]] = column[Array[Byte]]("validator")

    /** Database column expiration_time SqlType(timestamp) */
    val expirationTime: Rep[java.time.LocalDateTime] =
      column[java.time.LocalDateTime]("expiration_time")

    /** Database column person_id SqlType(int8) */
    val personId: Rep[Long] = column[Long]("person_id")

    /** Database column db_updated_time SqlType(timestamp) */
    val dbUpdatedTime: Rep[java.time.LocalDateTime] =
      column[java.time.LocalDateTime]("db_updated_time")

    /** Foreign key referencing Person (database name auth_token_person_id_fkey) */
    lazy val personFk = foreignKey("auth_token_person_id_fkey", personId, Person)(
      r => r.id,
      onUpdate = ForeignKeyAction.NoAction,
      onDelete = ForeignKeyAction.NoAction)

    /** Index over (selector) (database name auth_token_selector_index) */
    val index1 = index("auth_token_selector_index", selector)

    /** Uniqueness Index over (selector) (database name auth_token_selector_key) */
    val index2 = index("auth_token_selector_key", selector, unique = true)
  }

  /** Collection-like TableQuery object for table AuthToken */
  lazy val AuthToken = new TableQuery(tag => new AuthToken(tag))

  /** Entity class storing rows of table Dotable
    *  @param id Database column id SqlType(bigserial), AutoInc, PrimaryKey
    *  @param kind Database column kind SqlType(dotablekind)
    *  @param title Database column title SqlType(text), Length(2147483647,true)
    *  @param parentId Database column parent_id SqlType(int8)
    *  @param dbUpdatedTime Database column db_updated_time SqlType(timestamp)
    *  @param data Database column data SqlType(jsonb)
    *  @param contentEditedTime Database column content_edited_time SqlType(timestamp) */
  case class DotableRow(id: Long,
                        kind: resonator.shared.constants.DotableKinds.Value,
                        title: Option[String],
                        parentId: Option[Long],
                        dbUpdatedTime: java.time.LocalDateTime,
                        data: org.json4s.JsonAST.JValue,
                        contentEditedTime: java.time.LocalDateTime)

  /** GetResult implicit for fetching DotableRow objects using plain SQL queries */
  implicit def GetResultDotableRow(implicit e0: GR[Long],
                                   e1: GR[resonator.shared.constants.DotableKinds.Value],
                                   e2: GR[Option[String]],
                                   e3: GR[Option[Long]],
                                   e4: GR[java.time.LocalDateTime],
                                   e5: GR[org.json4s.JsonAST.JValue]): GR[DotableRow] = GR { prs =>
    import prs._
    DotableRow.tupled(
      (<<[Long],
       <<[resonator.shared.constants.DotableKinds.Value],
       <<?[String],
       <<?[Long],
       <<[java.time.LocalDateTime],
       <<[org.json4s.JsonAST.JValue],
       <<[java.time.LocalDateTime]))
  }

  /** Table description of table dotable. Objects of this class serve as prototypes for rows in queries. */
  class Dotable(_tableTag: slick.lifted.Tag)
      extends profile.api.Table[DotableRow](_tableTag, "dotable") {
    def * =
      (id, kind, title, parentId, dbUpdatedTime, data, contentEditedTime) <> (DotableRow.tupled, DotableRow.unapply)

    /** Maps whole row to an option. Useful for outer joins. */
    def ? =
      (Rep.Some(id),
       Rep.Some(kind),
       title,
       parentId,
       Rep.Some(dbUpdatedTime),
       Rep.Some(data),
       Rep.Some(contentEditedTime)).shaped.<>(
        { r =>
          import r._;
          _1.map(_ => DotableRow.tupled((_1.get, _2.get, _3, _4, _5.get, _6.get, _7.get)))
        },
        (_: Any) => throw new Exception("Inserting into ? projection not supported.")
      )

    /** Database column id SqlType(bigserial), AutoInc, PrimaryKey */
    val id: Rep[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)

    /** Database column kind SqlType(dotablekind) */
    val kind: Rep[resonator.shared.constants.DotableKinds.Value] =
      column[resonator.shared.constants.DotableKinds.Value]("kind")

    /** Database column title SqlType(text), Length(2147483647,true) */
    val title: Rep[Option[String]] =
      column[Option[String]]("title", O.Length(2147483647, varying = true))

    /** Database column parent_id SqlType(int8) */
    val parentId: Rep[Option[Long]] = column[Option[Long]]("parent_id")

    /** Database column db_updated_time SqlType(timestamp) */
    val dbUpdatedTime: Rep[java.time.LocalDateTime] =
      column[java.time.LocalDateTime]("db_updated_time")

    /** Database column data SqlType(jsonb) */
    val data: Rep[org.json4s.JsonAST.JValue] = column[org.json4s.JsonAST.JValue]("data")

    /** Database column content_edited_time SqlType(timestamp) */
    val contentEditedTime: Rep[java.time.LocalDateTime] =
      column[java.time.LocalDateTime]("content_edited_time")

    /** Foreign key referencing Dotable (database name dotable_parent_id_fkey) */
    lazy val dotableFk = foreignKey("dotable_parent_id_fkey", parentId, Dotable)(
      r => Rep.Some(r.id),
      onUpdate = ForeignKeyAction.NoAction,
      onDelete = ForeignKeyAction.NoAction)

    /** Index over (contentEditedTime) (database name dotable_content_edited_time_index) */
    val index1 = index("dotable_content_edited_time_index", contentEditedTime)

    /** Index over (dbUpdatedTime,id) (database name dotable_db_updated_time_id_compound_index) */
    val index2 = index("dotable_db_updated_time_id_compound_index", (dbUpdatedTime, id))

    /** Index over (dbUpdatedTime) (database name dotable_db_updated_time_index) */
    val index3 = index("dotable_db_updated_time_index", dbUpdatedTime)

    /** Index over (kind,id) (database name dotable_kind_id_index) */
    val index4 = index("dotable_kind_id_index", (kind, id))

    /** Index over (title) (database name dotable_title_index) */
    val index5 = index("dotable_title_index", title)
  }

  /** Collection-like TableQuery object for table Dotable */
  lazy val Dotable = new TableQuery(tag => new Dotable(tag))

  /** Entity class storing rows of table DotableTag
    *  @param tagId Database column tag_id SqlType(int8)
    *  @param dotableId Database column dotable_id SqlType(int8)
    *  @param dbUpdatedTime Database column db_updated_time SqlType(timestamp) */
  case class DotableTagRow(tagId: Long, dotableId: Long, dbUpdatedTime: java.time.LocalDateTime)

  /** GetResult implicit for fetching DotableTagRow objects using plain SQL queries */
  implicit def GetResultDotableTagRow(implicit e0: GR[Long],
                                      e1: GR[java.time.LocalDateTime]): GR[DotableTagRow] = GR {
    prs =>
      import prs._
      DotableTagRow.tupled((<<[Long], <<[Long], <<[java.time.LocalDateTime]))
  }

  /** Table description of table dotable_tag. Objects of this class serve as prototypes for rows in queries. */
  class DotableTag(_tableTag: slick.lifted.Tag)
      extends profile.api.Table[DotableTagRow](_tableTag, "dotable_tag") {
    def * = (tagId, dotableId, dbUpdatedTime) <> (DotableTagRow.tupled, DotableTagRow.unapply)

    /** Maps whole row to an option. Useful for outer joins. */
    def ? =
      (Rep.Some(tagId), Rep.Some(dotableId), Rep.Some(dbUpdatedTime)).shaped.<>({ r =>
        import r._; _1.map(_ => DotableTagRow.tupled((_1.get, _2.get, _3.get)))
      }, (_: Any) => throw new Exception("Inserting into ? projection not supported."))

    /** Database column tag_id SqlType(int8) */
    val tagId: Rep[Long] = column[Long]("tag_id")

    /** Database column dotable_id SqlType(int8) */
    val dotableId: Rep[Long] = column[Long]("dotable_id")

    /** Database column db_updated_time SqlType(timestamp) */
    val dbUpdatedTime: Rep[java.time.LocalDateTime] =
      column[java.time.LocalDateTime]("db_updated_time")

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

  /** Entity class storing rows of table Dote
    *  @param id Database column id SqlType(bigserial), AutoInc, PrimaryKey
    *  @param dotableId Database column dotable_id SqlType(int8)
    *  @param personId Database column person_id SqlType(int8)
    *  @param doteTime Database column dote_time SqlType(timestamp)
    *  @param dbUpdatedTime Database column db_updated_time SqlType(timestamp)
    *  @param emoteKind Database column emote_kind SqlType(emotekind)
    *  @param halfStars Database column half_stars SqlType(int4), Default(0)
    *  @param reviewDotableId Database column review_dotable_id SqlType(int8) */
  case class DoteRow(id: Long,
                     dotableId: Long,
                     personId: Long,
                     doteTime: java.time.LocalDateTime,
                     dbUpdatedTime: java.time.LocalDateTime,
                     emoteKind: Option[resonator.shared.constants.EmoteKinds.Value],
                     halfStars: Int = 0,
                     reviewDotableId: Option[Long])

  /** GetResult implicit for fetching DoteRow objects using plain SQL queries */
  implicit def GetResultDoteRow(implicit e0: GR[Long],
                                e1: GR[java.time.LocalDateTime],
                                e2: GR[Option[resonator.shared.constants.EmoteKinds.Value]],
                                e3: GR[Int],
                                e4: GR[Option[Long]]): GR[DoteRow] = GR { prs =>
    import prs._
    DoteRow.tupled(
      (<<[Long],
       <<[Long],
       <<[Long],
       <<[java.time.LocalDateTime],
       <<[java.time.LocalDateTime],
       <<?[resonator.shared.constants.EmoteKinds.Value],
       <<[Int],
       <<?[Long]))
  }

  /** Table description of table dote. Objects of this class serve as prototypes for rows in queries. */
  class Dote(_tableTag: slick.lifted.Tag) extends profile.api.Table[DoteRow](_tableTag, "dote") {
    def * =
      (id, dotableId, personId, doteTime, dbUpdatedTime, emoteKind, halfStars, reviewDotableId) <> (DoteRow.tupled, DoteRow.unapply)

    /** Maps whole row to an option. Useful for outer joins. */
    def ? =
      (Rep.Some(id),
       Rep.Some(dotableId),
       Rep.Some(personId),
       Rep.Some(doteTime),
       Rep.Some(dbUpdatedTime),
       emoteKind,
       Rep.Some(halfStars),
       reviewDotableId).shaped.<>(
        { r =>
          import r._;
          _1.map(_ => DoteRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6, _7.get, _8)))
        },
        (_: Any) => throw new Exception("Inserting into ? projection not supported.")
      )

    /** Database column id SqlType(bigserial), AutoInc, PrimaryKey */
    val id: Rep[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)

    /** Database column dotable_id SqlType(int8) */
    val dotableId: Rep[Long] = column[Long]("dotable_id")

    /** Database column person_id SqlType(int8) */
    val personId: Rep[Long] = column[Long]("person_id")

    /** Database column dote_time SqlType(timestamp) */
    val doteTime: Rep[java.time.LocalDateTime] = column[java.time.LocalDateTime]("dote_time")

    /** Database column db_updated_time SqlType(timestamp) */
    val dbUpdatedTime: Rep[java.time.LocalDateTime] =
      column[java.time.LocalDateTime]("db_updated_time")

    /** Database column emote_kind SqlType(emotekind) */
    val emoteKind: Rep[Option[resonator.shared.constants.EmoteKinds.Value]] =
      column[Option[resonator.shared.constants.EmoteKinds.Value]]("emote_kind")

    /** Database column half_stars SqlType(int4), Default(0) */
    val halfStars: Rep[Int] = column[Int]("half_stars", O.Default(0))

    /** Database column review_dotable_id SqlType(int8) */
    val reviewDotableId: Rep[Option[Long]] = column[Option[Long]]("review_dotable_id")

    /** Foreign key referencing Dotable (database name dote_dotable_id_fkey) */
    lazy val dotableFk1 = foreignKey("dote_dotable_id_fkey", dotableId, Dotable)(
      r => r.id,
      onUpdate = ForeignKeyAction.NoAction,
      onDelete = ForeignKeyAction.NoAction)

    /** Foreign key referencing Dotable (database name dote_review_dotable_id_fkey) */
    lazy val dotableFk2 = foreignKey("dote_review_dotable_id_fkey", reviewDotableId, Dotable)(
      r => Rep.Some(r.id),
      onUpdate = ForeignKeyAction.NoAction,
      onDelete = ForeignKeyAction.NoAction)

    /** Foreign key referencing Person (database name dote_person_id_fkey) */
    lazy val personFk = foreignKey("dote_person_id_fkey", personId, Person)(
      r => r.id,
      onUpdate = ForeignKeyAction.NoAction,
      onDelete = ForeignKeyAction.NoAction)

    /** Uniqueness Index over (dotableId,personId) (database name dote_dotable_person_index) */
    val index1 = index("dote_dotable_person_index", (dotableId, personId), unique = true)

    /** Uniqueness Index over (reviewDotableId) (database name dote_review_dotable_id) */
    val index2 = index("dote_review_dotable_id", reviewDotableId, unique = true)
  }

  /** Collection-like TableQuery object for table Dote */
  lazy val Dote = new TableQuery(tag => new Dote(tag))

  /** Entity class storing rows of table Follower
    *  @param id Database column id SqlType(bigserial), AutoInc, PrimaryKey
    *  @param followTime Database column follow_time SqlType(timestamp)
    *  @param followerId Database column follower_id SqlType(int8)
    *  @param followeeId Database column followee_id SqlType(int8)
    *  @param dbUpdatedTime Database column db_updated_time SqlType(timestamp) */
  case class FollowerRow(id: Long,
                         followTime: java.time.LocalDateTime,
                         followerId: Long,
                         followeeId: Long,
                         dbUpdatedTime: java.time.LocalDateTime)

  /** GetResult implicit for fetching FollowerRow objects using plain SQL queries */
  implicit def GetResultFollowerRow(implicit e0: GR[Long],
                                    e1: GR[java.time.LocalDateTime]): GR[FollowerRow] = GR { prs =>
    import prs._
    FollowerRow.tupled(
      (<<[Long], <<[java.time.LocalDateTime], <<[Long], <<[Long], <<[java.time.LocalDateTime]))
  }

  /** Table description of table follower. Objects of this class serve as prototypes for rows in queries. */
  class Follower(_tableTag: slick.lifted.Tag)
      extends profile.api.Table[FollowerRow](_tableTag, "follower") {
    def * =
      (id, followTime, followerId, followeeId, dbUpdatedTime) <> (FollowerRow.tupled, FollowerRow.unapply)

    /** Maps whole row to an option. Useful for outer joins. */
    def ? =
      (Rep.Some(id),
       Rep.Some(followTime),
       Rep.Some(followerId),
       Rep.Some(followeeId),
       Rep.Some(dbUpdatedTime)).shaped.<>(
        { r =>
          import r._; _1.map(_ => FollowerRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get)))
        },
        (_: Any) => throw new Exception("Inserting into ? projection not supported.")
      )

    /** Database column id SqlType(bigserial), AutoInc, PrimaryKey */
    val id: Rep[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)

    /** Database column follow_time SqlType(timestamp) */
    val followTime: Rep[java.time.LocalDateTime] = column[java.time.LocalDateTime]("follow_time")

    /** Database column follower_id SqlType(int8) */
    val followerId: Rep[Long] = column[Long]("follower_id")

    /** Database column followee_id SqlType(int8) */
    val followeeId: Rep[Long] = column[Long]("followee_id")

    /** Database column db_updated_time SqlType(timestamp) */
    val dbUpdatedTime: Rep[java.time.LocalDateTime] =
      column[java.time.LocalDateTime]("db_updated_time")

    /** Foreign key referencing Person (database name follower_followee_id_fkey) */
    lazy val personFk1 = foreignKey("follower_followee_id_fkey", followeeId, Person)(
      r => r.id,
      onUpdate = ForeignKeyAction.NoAction,
      onDelete = ForeignKeyAction.NoAction)

    /** Foreign key referencing Person (database name follower_follower_id_fkey) */
    lazy val personFk2 = foreignKey("follower_follower_id_fkey", followerId, Person)(
      r => r.id,
      onUpdate = ForeignKeyAction.NoAction,
      onDelete = ForeignKeyAction.NoAction)

    /** Uniqueness Index over (followeeId,followerId) (database name follower_followee_index) */
    val index1 = index("follower_followee_index", (followeeId, followerId), unique = true)
  }

  /** Collection-like TableQuery object for table Follower */
  lazy val Follower = new TableQuery(tag => new Follower(tag))

  /** Entity class storing rows of table Person
    *  @param id Database column id SqlType(bigserial), AutoInc, PrimaryKey
    *  @param username Database column username SqlType(text), Length(2147483647,true)
    *  @param email Database column email SqlType(text), Length(2147483647,true)
    *  @param loginCode Database column login_code SqlType(text), Length(2147483647,true)
    *  @param loginCodeExpirationTime Database column login_code_expiration_time SqlType(timestamp)
    *  @param dbUpdatedTime Database column db_updated_time SqlType(timestamp) */
  case class PersonRow(id: Long,
                       username: String,
                       email: String,
                       loginCode: Option[String],
                       loginCodeExpirationTime: java.time.LocalDateTime,
                       dbUpdatedTime: java.time.LocalDateTime)

  /** GetResult implicit for fetching PersonRow objects using plain SQL queries */
  implicit def GetResultPersonRow(implicit e0: GR[Long],
                                  e1: GR[String],
                                  e2: GR[Option[String]],
                                  e3: GR[java.time.LocalDateTime]): GR[PersonRow] = GR { prs =>
    import prs._
    PersonRow.tupled(
      (<<[Long],
       <<[String],
       <<[String],
       <<?[String],
       <<[java.time.LocalDateTime],
       <<[java.time.LocalDateTime]))
  }

  /** Table description of table person. Objects of this class serve as prototypes for rows in queries. */
  class Person(_tableTag: slick.lifted.Tag)
      extends profile.api.Table[PersonRow](_tableTag, "person") {
    def * =
      (id, username, email, loginCode, loginCodeExpirationTime, dbUpdatedTime) <> (PersonRow.tupled, PersonRow.unapply)

    /** Maps whole row to an option. Useful for outer joins. */
    def ? =
      (Rep.Some(id),
       Rep.Some(username),
       Rep.Some(email),
       loginCode,
       Rep.Some(loginCodeExpirationTime),
       Rep.Some(dbUpdatedTime)).shaped.<>(
        { r =>
          import r._; _1.map(_ => PersonRow.tupled((_1.get, _2.get, _3.get, _4, _5.get, _6.get)))
        },
        (_: Any) => throw new Exception("Inserting into ? projection not supported.")
      )

    /** Database column id SqlType(bigserial), AutoInc, PrimaryKey */
    val id: Rep[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)

    /** Database column username SqlType(text), Length(2147483647,true) */
    val username: Rep[String] = column[String]("username", O.Length(2147483647, varying = true))

    /** Database column email SqlType(text), Length(2147483647,true) */
    val email: Rep[String] = column[String]("email", O.Length(2147483647, varying = true))

    /** Database column login_code SqlType(text), Length(2147483647,true) */
    val loginCode: Rep[Option[String]] =
      column[Option[String]]("login_code", O.Length(2147483647, varying = true))

    /** Database column login_code_expiration_time SqlType(timestamp) */
    val loginCodeExpirationTime: Rep[java.time.LocalDateTime] =
      column[java.time.LocalDateTime]("login_code_expiration_time")

    /** Database column db_updated_time SqlType(timestamp) */
    val dbUpdatedTime: Rep[java.time.LocalDateTime] =
      column[java.time.LocalDateTime]("db_updated_time")

    /** Uniqueness Index over (email) (database name email_uniq) */
    val index1 = index("email_uniq", email, unique = true)

    /** Uniqueness Index over (username) (database name username_uniq) */
    val index2 = index("username_uniq", username, unique = true)
  }

  /** Collection-like TableQuery object for table Person */
  lazy val Person = new TableQuery(tag => new Person(tag))

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
    *  @param episodeDotableId Database column episode_dotable_id SqlType(int8)
    *  @param dbUpdatedTime Database column db_updated_time SqlType(timestamp)
    *  @param lastDataHash Database column last_data_hash SqlType(bytea) */
  case class PodcastEpisodeIngestionRow(id: Long,
                                        podcastDotableId: Long,
                                        guid: String,
                                        episodeDotableId: Long,
                                        dbUpdatedTime: java.time.LocalDateTime,
                                        lastDataHash: Option[Array[Byte]])

  /** GetResult implicit for fetching PodcastEpisodeIngestionRow objects using plain SQL queries */
  implicit def GetResultPodcastEpisodeIngestionRow(
      implicit e0: GR[Long],
      e1: GR[String],
      e2: GR[java.time.LocalDateTime],
      e3: GR[Option[Array[Byte]]]): GR[PodcastEpisodeIngestionRow] = GR { prs =>
    import prs._
    PodcastEpisodeIngestionRow.tupled(
      (<<[Long], <<[Long], <<[String], <<[Long], <<[java.time.LocalDateTime], <<?[Array[Byte]]))
  }

  /** Table description of table podcast_episode_ingestion. Objects of this class serve as prototypes for rows in queries. */
  class PodcastEpisodeIngestion(_tableTag: slick.lifted.Tag)
      extends profile.api.Table[PodcastEpisodeIngestionRow](_tableTag, "podcast_episode_ingestion") {
    def * =
      (id, podcastDotableId, guid, episodeDotableId, dbUpdatedTime, lastDataHash) <> (PodcastEpisodeIngestionRow.tupled, PodcastEpisodeIngestionRow.unapply)

    /** Maps whole row to an option. Useful for outer joins. */
    def ? =
      (Rep.Some(id),
       Rep.Some(podcastDotableId),
       Rep.Some(guid),
       Rep.Some(episodeDotableId),
       Rep.Some(dbUpdatedTime),
       lastDataHash).shaped.<>(
        { r =>
          import r._;
          _1.map(_ =>
            PodcastEpisodeIngestionRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6)))
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

    /** Database column db_updated_time SqlType(timestamp) */
    val dbUpdatedTime: Rep[java.time.LocalDateTime] =
      column[java.time.LocalDateTime]("db_updated_time")

    /** Database column last_data_hash SqlType(bytea) */
    val lastDataHash: Rep[Option[Array[Byte]]] = column[Option[Array[Byte]]]("last_data_hash")

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
    *  @param dbUpdatedTime Database column db_updated_time SqlType(timestamp)
    *  @param nextIngestionTime Database column next_ingestion_time SqlType(timestamp)
    *  @param lastFeedEtag Database column last_feed_etag SqlType(text), Length(2147483647,true)
    *  @param lastDataHash Database column last_data_hash SqlType(bytea)
    *  @param reingestWaitMinutes Database column reingest_wait_minutes SqlType(int8), Default(60) */
  case class PodcastFeedIngestionRow(id: Long,
                                     feedRssUrl: String,
                                     itunesId: Long,
                                     podcastDotableId: Option[Long],
                                     dbUpdatedTime: java.time.LocalDateTime,
                                     nextIngestionTime: java.time.LocalDateTime,
                                     lastFeedEtag: Option[String],
                                     lastDataHash: Option[Array[Byte]],
                                     reingestWaitMinutes: Long = 60L)

  /** GetResult implicit for fetching PodcastFeedIngestionRow objects using plain SQL queries */
  implicit def GetResultPodcastFeedIngestionRow(
      implicit e0: GR[Long],
      e1: GR[String],
      e2: GR[Option[Long]],
      e3: GR[java.time.LocalDateTime],
      e4: GR[Option[String]],
      e5: GR[Option[Array[Byte]]]): GR[PodcastFeedIngestionRow] = GR { prs =>
    import prs._
    PodcastFeedIngestionRow.tupled(
      (<<[Long],
       <<[String],
       <<[Long],
       <<?[Long],
       <<[java.time.LocalDateTime],
       <<[java.time.LocalDateTime],
       <<?[String],
       <<?[Array[Byte]],
       <<[Long]))
  }

  /** Table description of table podcast_feed_ingestion. Objects of this class serve as prototypes for rows in queries. */
  class PodcastFeedIngestion(_tableTag: slick.lifted.Tag)
      extends profile.api.Table[PodcastFeedIngestionRow](_tableTag, "podcast_feed_ingestion") {
    def * =
      (id,
       feedRssUrl,
       itunesId,
       podcastDotableId,
       dbUpdatedTime,
       nextIngestionTime,
       lastFeedEtag,
       lastDataHash,
       reingestWaitMinutes) <> (PodcastFeedIngestionRow.tupled, PodcastFeedIngestionRow.unapply)

    /** Maps whole row to an option. Useful for outer joins. */
    def ? =
      (Rep.Some(id),
       Rep.Some(feedRssUrl),
       Rep.Some(itunesId),
       podcastDotableId,
       Rep.Some(dbUpdatedTime),
       Rep.Some(nextIngestionTime),
       lastFeedEtag,
       lastDataHash,
       Rep.Some(reingestWaitMinutes)).shaped.<>(
        { r =>
          import r._;
          _1.map(
            _ =>
              PodcastFeedIngestionRow.tupled(
                (_1.get, _2.get, _3.get, _4, _5.get, _6.get, _7, _8, _9.get)))
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

    /** Database column db_updated_time SqlType(timestamp) */
    val dbUpdatedTime: Rep[java.time.LocalDateTime] =
      column[java.time.LocalDateTime]("db_updated_time")

    /** Database column next_ingestion_time SqlType(timestamp) */
    val nextIngestionTime: Rep[java.time.LocalDateTime] =
      column[java.time.LocalDateTime]("next_ingestion_time")

    /** Database column last_feed_etag SqlType(text), Length(2147483647,true) */
    val lastFeedEtag: Rep[Option[String]] =
      column[Option[String]]("last_feed_etag", O.Length(2147483647, varying = true))

    /** Database column last_data_hash SqlType(bytea) */
    val lastDataHash: Rep[Option[Array[Byte]]] = column[Option[Array[Byte]]]("last_data_hash")

    /** Database column reingest_wait_minutes SqlType(int8), Default(60) */
    val reingestWaitMinutes: Rep[Long] = column[Long]("reingest_wait_minutes", O.Default(60L))

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

  /** Entity class storing rows of table RadioStation
    *  @param id Database column id SqlType(bigserial), AutoInc, PrimaryKey
    *  @param enabled Database column enabled SqlType(bool)
    *  @param frequency Database column frequency SqlType(numeric)
    *  @param frequencyKind Database column frequency_kind SqlType(frequencykind), Length(2147483647,true)
    *  @param callSign Database column call_sign SqlType(bpchar), Length(4,false)
    *  @param dbUpdatedTime Database column db_updated_time SqlType(timestamp) */
  case class RadioStationRow(id: Long,
                             enabled: Boolean,
                             frequency: scala.math.BigDecimal,
                             frequencyKind: String,
                             callSign: String,
                             dbUpdatedTime: java.time.LocalDateTime)

  /** GetResult implicit for fetching RadioStationRow objects using plain SQL queries */
  implicit def GetResultRadioStationRow(implicit e0: GR[Long],
                                        e1: GR[Boolean],
                                        e2: GR[scala.math.BigDecimal],
                                        e3: GR[String],
                                        e4: GR[java.time.LocalDateTime]): GR[RadioStationRow] =
    GR { prs =>
      import prs._
      RadioStationRow.tupled(
        (<<[Long],
         <<[Boolean],
         <<[scala.math.BigDecimal],
         <<[String],
         <<[String],
         <<[java.time.LocalDateTime]))
    }

  /** Table description of table radio_station. Objects of this class serve as prototypes for rows in queries. */
  class RadioStation(_tableTag: slick.lifted.Tag)
      extends profile.api.Table[RadioStationRow](_tableTag, "radio_station") {
    def * =
      (id, enabled, frequency, frequencyKind, callSign, dbUpdatedTime) <> (RadioStationRow.tupled, RadioStationRow.unapply)

    /** Maps whole row to an option. Useful for outer joins. */
    def ? =
      (Rep.Some(id),
       Rep.Some(enabled),
       Rep.Some(frequency),
       Rep.Some(frequencyKind),
       Rep.Some(callSign),
       Rep.Some(dbUpdatedTime)).shaped.<>(
        { r =>
          import r._;
          _1.map(_ => RadioStationRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get)))
        },
        (_: Any) => throw new Exception("Inserting into ? projection not supported.")
      )

    /** Database column id SqlType(bigserial), AutoInc, PrimaryKey */
    val id: Rep[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)

    /** Database column enabled SqlType(bool) */
    val enabled: Rep[Boolean] = column[Boolean]("enabled")

    /** Database column frequency SqlType(numeric) */
    val frequency: Rep[scala.math.BigDecimal] = column[scala.math.BigDecimal]("frequency")

    /** Database column frequency_kind SqlType(frequencykind), Length(2147483647,true) */
    val frequencyKind: Rep[String] =
      column[String]("frequency_kind", O.Length(2147483647, varying = true))

    /** Database column call_sign SqlType(bpchar), Length(4,false) */
    val callSign: Rep[String] = column[String]("call_sign", O.Length(4, varying = false))

    /** Database column db_updated_time SqlType(timestamp) */
    val dbUpdatedTime: Rep[java.time.LocalDateTime] =
      column[java.time.LocalDateTime]("db_updated_time")

    /** Uniqueness Index over (callSign) (database name radio_station_call_sign_uniq) */
    val index1 = index("radio_station_call_sign_uniq", callSign, unique = true)

    /** Uniqueness Index over (frequency,frequencyKind) (database name radio_station_fequency_frequency_kind_uniq) */
    val index2 = index("radio_station_fequency_frequency_kind_uniq",
                       (frequency, frequencyKind),
                       unique = true)
  }

  /** Collection-like TableQuery object for table RadioStation */
  lazy val RadioStation = new TableQuery(tag => new RadioStation(tag))

  /** Entity class storing rows of table RadioStationPlaylist
    *  @param id Database column id SqlType(bigserial), AutoInc, PrimaryKey
    *  @param stationId Database column station_id SqlType(int8)
    *  @param episodeId Database column episode_id SqlType(int8)
    *  @param startTime Database column start_time SqlType(timestamp)
    *  @param endTime Database column end_time SqlType(timestamp)
    *  @param dbUpdatedTime Database column db_updated_time SqlType(timestamp) */
  case class RadioStationPlaylistRow(id: Long,
                                     stationId: Long,
                                     episodeId: Long,
                                     startTime: java.time.LocalDateTime,
                                     endTime: java.time.LocalDateTime,
                                     dbUpdatedTime: java.time.LocalDateTime)

  /** GetResult implicit for fetching RadioStationPlaylistRow objects using plain SQL queries */
  implicit def GetResultRadioStationPlaylistRow(
      implicit e0: GR[Long],
      e1: GR[java.time.LocalDateTime]): GR[RadioStationPlaylistRow] = GR { prs =>
    import prs._
    RadioStationPlaylistRow.tupled(
      (<<[Long],
       <<[Long],
       <<[Long],
       <<[java.time.LocalDateTime],
       <<[java.time.LocalDateTime],
       <<[java.time.LocalDateTime]))
  }

  /** Table description of table radio_station_playlist. Objects of this class serve as prototypes for rows in queries. */
  class RadioStationPlaylist(_tableTag: slick.lifted.Tag)
      extends profile.api.Table[RadioStationPlaylistRow](_tableTag, "radio_station_playlist") {
    def * =
      (id, stationId, episodeId, startTime, endTime, dbUpdatedTime) <> (RadioStationPlaylistRow.tupled, RadioStationPlaylistRow.unapply)

    /** Maps whole row to an option. Useful for outer joins. */
    def ? =
      (Rep.Some(id),
       Rep.Some(stationId),
       Rep.Some(episodeId),
       Rep.Some(startTime),
       Rep.Some(endTime),
       Rep.Some(dbUpdatedTime)).shaped.<>(
        { r =>
          import r._;
          _1.map(_ =>
            RadioStationPlaylistRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get)))
        },
        (_: Any) => throw new Exception("Inserting into ? projection not supported.")
      )

    /** Database column id SqlType(bigserial), AutoInc, PrimaryKey */
    val id: Rep[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)

    /** Database column station_id SqlType(int8) */
    val stationId: Rep[Long] = column[Long]("station_id")

    /** Database column episode_id SqlType(int8) */
    val episodeId: Rep[Long] = column[Long]("episode_id")

    /** Database column start_time SqlType(timestamp) */
    val startTime: Rep[java.time.LocalDateTime] = column[java.time.LocalDateTime]("start_time")

    /** Database column end_time SqlType(timestamp) */
    val endTime: Rep[java.time.LocalDateTime] = column[java.time.LocalDateTime]("end_time")

    /** Database column db_updated_time SqlType(timestamp) */
    val dbUpdatedTime: Rep[java.time.LocalDateTime] =
      column[java.time.LocalDateTime]("db_updated_time")

    /** Foreign key referencing Dotable (database name radio_station_playlist_episode_id_fkey) */
    lazy val dotableFk = foreignKey("radio_station_playlist_episode_id_fkey", episodeId, Dotable)(
      r => r.id,
      onUpdate = ForeignKeyAction.NoAction,
      onDelete = ForeignKeyAction.NoAction)

    /** Foreign key referencing RadioStation (database name radio_station_playlist_station_id_fkey) */
    lazy val radioStationFk =
      foreignKey("radio_station_playlist_station_id_fkey", stationId, RadioStation)(
        r => r.id,
        onUpdate = ForeignKeyAction.NoAction,
        onDelete = ForeignKeyAction.NoAction)

    /** Index over (endTime) (database name radio_station_playlist_end_time_index) */
    val index1 = index("radio_station_playlist_end_time_index", endTime)

    /** Index over (startTime) (database name radio_station_playlist_start_time_index) */
    val index2 = index("radio_station_playlist_start_time_index", startTime)

    /** Uniqueness Index over (stationId,startTime) (database name radio_station_playlist_station_start_time_uniq) */
    val index3 = index("radio_station_playlist_station_start_time_uniq",
                       (stationId, startTime),
                       unique = true)
  }

  /** Collection-like TableQuery object for table RadioStationPlaylist */
  lazy val RadioStationPlaylist = new TableQuery(tag => new RadioStationPlaylist(tag))

  /** Entity class storing rows of table RadioStationPodcast
    *  @param stationId Database column station_id SqlType(int8)
    *  @param podcastId Database column podcast_id SqlType(int8) */
  case class RadioStationPodcastRow(stationId: Long, podcastId: Long)

  /** GetResult implicit for fetching RadioStationPodcastRow objects using plain SQL queries */
  implicit def GetResultRadioStationPodcastRow(implicit e0: GR[Long]): GR[RadioStationPodcastRow] =
    GR { prs =>
      import prs._
      RadioStationPodcastRow.tupled((<<[Long], <<[Long]))
    }

  /** Table description of table radio_station_podcast. Objects of this class serve as prototypes for rows in queries. */
  class RadioStationPodcast(_tableTag: slick.lifted.Tag)
      extends profile.api.Table[RadioStationPodcastRow](_tableTag, "radio_station_podcast") {
    def * =
      (stationId, podcastId) <> (RadioStationPodcastRow.tupled, RadioStationPodcastRow.unapply)

    /** Maps whole row to an option. Useful for outer joins. */
    def ? =
      (Rep.Some(stationId), Rep.Some(podcastId)).shaped.<>({ r =>
        import r._; _1.map(_ => RadioStationPodcastRow.tupled((_1.get, _2.get)))
      }, (_: Any) => throw new Exception("Inserting into ? projection not supported."))

    /** Database column station_id SqlType(int8) */
    val stationId: Rep[Long] = column[Long]("station_id")

    /** Database column podcast_id SqlType(int8) */
    val podcastId: Rep[Long] = column[Long]("podcast_id")

    /** Foreign key referencing Dotable (database name radio_station_podcast_podcast_id_fkey) */
    lazy val dotableFk = foreignKey("radio_station_podcast_podcast_id_fkey", podcastId, Dotable)(
      r => r.id,
      onUpdate = ForeignKeyAction.NoAction,
      onDelete = ForeignKeyAction.NoAction)

    /** Foreign key referencing RadioStation (database name radio_station_podcast_station_id_fkey) */
    lazy val radioStationFk =
      foreignKey("radio_station_podcast_station_id_fkey", stationId, RadioStation)(
        r => r.id,
        onUpdate = ForeignKeyAction.NoAction,
        onDelete = ForeignKeyAction.NoAction)

    /** Uniqueness Index over (stationId,podcastId) (database name radio_station_podcast_station_podcast_id_uniq_index) */
    val index1 = index("radio_station_podcast_station_podcast_id_uniq_index",
                       (stationId, podcastId),
                       unique = true)
  }

  /** Collection-like TableQuery object for table RadioStationPodcast */
  lazy val RadioStationPodcast = new TableQuery(tag => new RadioStationPodcast(tag))

  /** Entity class storing rows of table SearchIndexQueue
    *  @param indexName Database column index_name SqlType(text), Length(2147483647,true)
    *  @param syncCompletedThroughTime Database column sync_completed_through_time SqlType(timestamp)
    *  @param lastBatchMaxId Database column last_batch_max_id SqlType(int8), Default(0) */
  case class SearchIndexQueueRow(indexName: String,
                                 syncCompletedThroughTime: java.time.LocalDateTime,
                                 lastBatchMaxId: Long = 0L)

  /** GetResult implicit for fetching SearchIndexQueueRow objects using plain SQL queries */
  implicit def GetResultSearchIndexQueueRow(implicit e0: GR[String],
                                            e1: GR[java.time.LocalDateTime],
                                            e2: GR[Long]): GR[SearchIndexQueueRow] = GR { prs =>
    import prs._
    SearchIndexQueueRow.tupled((<<[String], <<[java.time.LocalDateTime], <<[Long]))
  }

  /** Table description of table search_index_queue. Objects of this class serve as prototypes for rows in queries. */
  class SearchIndexQueue(_tableTag: slick.lifted.Tag)
      extends profile.api.Table[SearchIndexQueueRow](_tableTag, "search_index_queue") {
    def * =
      (indexName, syncCompletedThroughTime, lastBatchMaxId) <> (SearchIndexQueueRow.tupled, SearchIndexQueueRow.unapply)

    /** Maps whole row to an option. Useful for outer joins. */
    def ? =
      (Rep.Some(indexName), Rep.Some(syncCompletedThroughTime), Rep.Some(lastBatchMaxId)).shaped
        .<>({ r =>
          import r._; _1.map(_ => SearchIndexQueueRow.tupled((_1.get, _2.get, _3.get)))
        }, (_: Any) => throw new Exception("Inserting into ? projection not supported."))

    /** Database column index_name SqlType(text), Length(2147483647,true) */
    val indexName: Rep[String] = column[String]("index_name", O.Length(2147483647, varying = true))

    /** Database column sync_completed_through_time SqlType(timestamp) */
    val syncCompletedThroughTime: Rep[java.time.LocalDateTime] =
      column[java.time.LocalDateTime]("sync_completed_through_time")

    /** Database column last_batch_max_id SqlType(int8), Default(0) */
    val lastBatchMaxId: Rep[Long] = column[Long]("last_batch_max_id", O.Default(0L))
  }

  /** Collection-like TableQuery object for table SearchIndexQueue */
  lazy val SearchIndexQueue = new TableQuery(tag => new SearchIndexQueue(tag))

  /** Entity class storing rows of table Tag
    *  @param id Database column id SqlType(bigserial), AutoInc, PrimaryKey
    *  @param kind Database column kind SqlType(tagkind)
    *  @param key Database column key SqlType(text), Length(2147483647,true)
    *  @param dbUpdatedTime Database column db_updated_time SqlType(timestamp)
    *  @param name Database column name SqlType(text), Length(2147483647,true) */
  case class TagRow(id: Long,
                    kind: resonator.shared.constants.TagKinds.Value,
                    key: String,
                    dbUpdatedTime: java.time.LocalDateTime,
                    name: String)

  /** GetResult implicit for fetching TagRow objects using plain SQL queries */
  implicit def GetResultTagRow(implicit e0: GR[Long],
                               e1: GR[resonator.shared.constants.TagKinds.Value],
                               e2: GR[String],
                               e3: GR[java.time.LocalDateTime]): GR[TagRow] = GR { prs =>
    import prs._
    TagRow.tupled(
      (<<[Long],
       <<[resonator.shared.constants.TagKinds.Value],
       <<[String],
       <<[java.time.LocalDateTime],
       <<[String]))
  }

  /** Table description of table tag. Objects of this class serve as prototypes for rows in queries. */
  class Tag(_tableTag: slick.lifted.Tag) extends profile.api.Table[TagRow](_tableTag, "tag") {
    def * = (id, kind, key, dbUpdatedTime, name) <> (TagRow.tupled, TagRow.unapply)

    /** Maps whole row to an option. Useful for outer joins. */
    def ? =
      (Rep.Some(id), Rep.Some(kind), Rep.Some(key), Rep.Some(dbUpdatedTime), Rep.Some(name)).shaped
        .<>(
          { r =>
            import r._; _1.map(_ => TagRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get)))
          },
          (_: Any) => throw new Exception("Inserting into ? projection not supported.")
        )

    /** Database column id SqlType(bigserial), AutoInc, PrimaryKey */
    val id: Rep[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)

    /** Database column kind SqlType(tagkind) */
    val kind: Rep[resonator.shared.constants.TagKinds.Value] =
      column[resonator.shared.constants.TagKinds.Value]("kind")

    /** Database column key SqlType(text), Length(2147483647,true) */
    val key: Rep[String] = column[String]("key", O.Length(2147483647, varying = true))

    /** Database column db_updated_time SqlType(timestamp) */
    val dbUpdatedTime: Rep[java.time.LocalDateTime] =
      column[java.time.LocalDateTime]("db_updated_time")

    /** Database column name SqlType(text), Length(2147483647,true) */
    val name: Rep[String] = column[String]("name", O.Length(2147483647, varying = true))

    /** Uniqueness Index over (kind,key) (database name tag_kind_label_uniq_index) */
    val index1 = index("tag_kind_label_uniq_index", (kind, key), unique = true)
  }

  /** Collection-like TableQuery object for table Tag */
  lazy val Tag = new TableQuery(tag => new Tag(tag))
}
