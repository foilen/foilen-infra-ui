# About

The UI to manage the infrastructure of multiple Linux machines.

You can find more documentation in the "docs" directory.

# Usage

# Configuration environment

Some configuration options can be overridden with the environment variables:

* `CONFIG_FILE` : The path to the config file
* `PLUGINS_JARS` : The path to the jars containing the plugings
* `INFINITE_LOOP_TIMEOUT_IN_MS` : To change `infiniteLoopTimeoutInMs`

## Configuration file

You need to create a json configuration file that maps the object InfraUiConfig.

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
	
	"mongoUri" : "mongodb://root:ABC@172.17.0.1:27017/foilen-ui-test",
	
	"loginConfigDetails" : {
		"appId" : "BC805427E1",
		"baseUrl" : "http://login.localhost",
		"certFile" : null,
		"certText" : null
	},
	"loginCookieSignatureSalt" : "fa7c8c64f538931381e245661",
	
	"externalJsScripts": {
		"en": [
			"https://www.foilen.com/wp-content/themes/foilen/banner/banner-en.js"
		],
		"fr": [
			"https://www.foilen.com/wp-content/themes/foilen/banner/banner-fr.js"
		]
	}
}
```

You can then specify the full path of that file as the *configFile* argument when launching the app or as the
*CONFIG_FILE* environment variable.

# Development

## Text messages

The translations are in:
- /src/main/resources/WEB-INF/infra/ui/messages/messages_en.properties
- /src/main/resources/WEB-INF/infra/ui/messages/messages_fr.properties

And when you add more, you can easily sort them by running `SortMessagesApp.launch`.

## To modify UI vendor libraries  

Edit `src/main/resources/WEB-INF/infra/ui/resources/ui2/vendor/package.json`.

Run `./update-ui-libraries.sh`.

If you are adding or removing dependencies, you can then edit `InfraUiWebSpringConfig`.

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

You can see the API documentation here: http://localhost:8888/swagger-ui.html

