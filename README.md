Other detailed documentation is in the [doc/](doc/) directory.

# Running locally

Compile and run the `server` project:

```
sbt server/run
```

Open <http://localhost:9000> in your browser


# Deploying to heroku

#### Prerequisites:
 - Install heroku CLI
 - Then install the deploy plugin with `heroku plugins:install heroku-cli-deploy`

#### Deploying to Heroku:

```
sbt dote-web/assembleJarAndDeployToHeroku
```


# Updating database schema

1) Add a new .sql file to server/conf/evolutions/default (file should be numbered sequentially).

2) Run Play and let it apply the evolution so it can keep track of the schema versioning in your db.

3) Regenerate the slick scala Tables file by running `sbt slickCodegen/run`, which runs the main
class in that project.
