Protos in this directory are persisted to the database (or in other long
term storage). Tag numbers should **never** be changed once used, this is
even more important for persisted protos than API protos since it can corrupt
storage. Also, the storage is currently implemented via a converting to a JSON
document, so even field renaming is not safe.