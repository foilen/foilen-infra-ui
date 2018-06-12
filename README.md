# About

The UI to manage the infrastructure of multiple Linux machines.

# Bootstrap a cluster TODO FIX

## Configuration file TODO FIX

You need to create a json configuration file that maps the object InfraUiConfig.

Here is an example of the content:

```json
{
	"baseUrl" : "http://infra.localhost",
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
	
	"loginConfigDetails" : {
		"appId" : "BC805427E1",
		"baseUrl" : "http://login.localhost",
		"certFile" : null,
		"certText" : null
	},
	"loginCookieSignatureSalt" : "fa7c8c64f538931381e245661"
}
loginBaseUrl" : "http://login.example.com"
}
```

You can then specify the full path of that file as the *configFile* argument when launching the app or as the
*CONFIG_FILE* environment variable.

Some configuration options can be overridden with the environment variables:

* `MYSQL_PORT_3306_TCP_ADDR` : To change `mysqlHostName`
* `MYSQL_PORT_3306_TCP_PORT` : To change `mysqlPort`

## Plugin

Can set a plugin folder in environment PLUGINS_JARS .
*TODO*

