### Overview:

 - Postgres database
 
 - IDs are all auto incrementing numbers, these numeric IDs aren't visible to
 the public API or website. Numeric IDs are converted to unique URL safe
 strings for use in the API and website. (this is to protect against
 malicious users which can take advantage of incrementing IDs)
 
 - Schema changes are run using Play framework evolutions
 https://www.playframework.com/documentation/2.6.x/Evolutions
 
### Dotable table

 - One table representing many types with JSONB columns for flexible schema.
 
   - This makes aggregates and actions easier since they only need to be 
   implemented once. For example "liking" a podcast is the same as "liking"
   a comment.
   
 - Types:
   - podcast
   - podcast episode
   - review
   - comment
   
 - The JSON for the columns is parsed to/from proto messages so that the
 schema is documented (and not just random).
 
 - Fields which need to be indexed and foreign keys are proper top level
 columns so they can be used in queries, filters and joins.
 
 ### Code
 
  - Database code is under [server/app/kurtome/dote/server/db]()
 
  - Class names:
    - `DbService` classes orchestrate multiple calls and determine
    transaction boundaries
    - `DbIo` classes implement database queries, CRUD operations and
    row mapping.
 
  - Query functions on `DbIo` classes are often defined in the scala
  classes as `val` declarations with for-comprehensions or `Compiled`,
  this is to make sure SQL isn't re-computed for every query. See
  [Slick's docs on compiled queries](
  http://slick.lightbend.com/doc/3.2.0/queries.html#compiled-queries).
