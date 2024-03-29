# State of project
This project is not under active development, it was hosted at https://resonator.fm, but is
no longer being supported.

# Docs

Other detailed documentation is in the [doc/](doc/) directory.

## Running locally

Compile and run the `server` project:

```sh
sbt server/run
```

Open <http://localhost:9000> in your browser

#### Local config

Typically you will have a config file to override connection info for
running locally, to specify a local config file use a JVM param like so:

``` 
sbt -Dconfig.file=server/conf/local.conf server/run
```

#### run_local_server.sh

Create the two following files:

 - Application config: `server/conf/local.conf`
 - Logging config: `server/conf/local.conf`
 
Then run

```sh
./run_local_server.sh
```


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

# Scraper

```
sbt "feedScraper/run --help"
```
