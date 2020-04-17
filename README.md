# About

The UI to manage the infrastructure of multiple Linux machines.

# Usage

# Configuration environment

Some configuration options can be overridden with the environment variables:

* `CONFIG_FILE` : The path to the config file
* `PLUGINS_JARS` : The path to the jars containing the plugings
* `MYSQL_PORT_3306_TCP_ADDR` : To change `mysqlHostName` (used by Docker Links)
* `MYSQL_PORT_3306_TCP_PORT` : To change `mysqlPort` (used by Docker Links)
* `INFINITE_LOOP_TIMEOUT_IN_MS` : To change `infiniteLoopTimeoutInMs`

## Configuration file

You need to create a json configuration file that maps the object InfraUiConfig.

The MySQL part is optional. It is only useful for the migration between MySQL to MongoDB. Once migrated, it is no more needed.

Here is an example of the content:

```json
{
	"baseUrl" : "http://infra.localhost",
	"infiniteLoopTimeoutInMs" : 120000,
	"csrfSalt" : "404117EAC615CC20867B2150B",
	
	"mailHost" : "127.0.0.1",
	"mailPort" : 25,
	"mailUsername" : null,
	"mailPassword" : null,
	
	"mailAlertsTo" : "admin@localhost",
	"mailFrom" : "infra-ui@localhost",
	
	"mysqlHostName" : "127.0.0.1",
	"mysqlPort" : 3306,
	"mysqlDatabaseName" : "infra_ui",
	"mysqlDatabaseUserName" : "infra_ui",
	"mysqlDatabasePassword" : "7b6618c64f538a7b181e24642",
	
	"mongoUri" : "mongodb://root:ABC@172.17.0.1:27017/foilen-ui-test",
	
	"loginConfigDetails" : {
		"appId" : "BC805427E1",
		"baseUrl" : "http://login.localhost",
		"certFile" : null,
		"certText" : null
	},
	"loginCookieSignatureSalt" : "fa7c8c64f538931381e245661"
}
```

You can then specify the full path of that file as the *configFile* argument when launching the app or as the
*CONFIG_FILE* environment variable.

# Development

## TEST in Eclipse

To be able to execute the application in Eclipse, you first need to download all the plugins jars:

```bash
./download-local-plugins-jars.sh
```

And you need to start mongodb

```bash
./mongodb-start.sh
```

Then, run *InfraUiApp - TEST.launch*

## TEST in Docker

Simply execute `./test-ui-test.sh` .

When done, cleanup by stopping the DB: `docker stop infra-ui-mongodb` .

# More

## Swagger

You can see the API documentation here: http://localhost:8080/swagger-ui.html

