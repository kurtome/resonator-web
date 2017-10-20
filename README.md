Run locally
-----------
Compile and run the `server` project:
```
sbt run
```

Open <http://localhost:9000> in your browser

Deploy
-------

#### Prerequisites:
 - Install heroku CLI
 - Then install the deploy plugin with `heroku plugins:install heroku-cli-deploy`

#### Deploying to Heroku:

```
sbt dote-web/assembleJarAndDeployToHeroku
```
