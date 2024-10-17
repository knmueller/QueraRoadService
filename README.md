This is a Kotlin Multiplatform project for a Road Service

* `/shared` - common artifacts between the projects
* `/ktorServerCommon` - a common library that can be shared across ktor applications
* `/roadService` - the "Road" service
* `/docker` - holds the Dockerfile to build and run the app

## Building
```
docker build --pull -f docker/Dockerfile -t road-app .
```

## Running
The app runs on port 8081 inside the container. Use the following command to run:
```
docker run -d -p 12345:8081 -p 12346:8082 --name road-app road-app
```

(See below `Accessing the DB` section for db viewing access on port 8082)

## KtorServerCommon library
This library has some common handling that could be used across ktor apps. Currently, it has
functions for setting up CORS, ContentNegotiation, common exception handling, and config parsing.


## shared kotlin multiplatform section
Separate project to house some common types, constants, and utilities. The type definitions for
`Intersection`, `Road`, and `Sign` are here, along with a few common response types. 


## Road service 

There are 3 apis hosted in the app, all with a base api of `/api/1` to indicate version 1 of the api:
- `/api/1/intersections` - Allows CRUD operations on road intersections
- `/api/1/roads` - Allows CRUD operations on roads
- `/api/1/signs` - Allows CRUD operations on road signs

#### Intersections
`Intersections` are 1-to-many `Roads`. This allows 1 intersection to be associated to 1 or more roads.

#### Roads
In reverse of intersections, `Roads` are many-to-1 `Intersections`. 
Also, `Roads` are 1-to-many `Signs`. This allows 1 road to be associated to 1 or more signs.

#### Signs
In reverse of roads, `Signs` are many-to-1 `Roads`


### Architecture by directory
This app employs:
- `Koin` for dependency injection
- R2dbc backed by an H2 in-memory database for reactive relational database access
- Komapper as a ORM
- Ktor `Resources` to describe routes
- a thirdparty library `name.nkonev.r2dbc-migrate:r2dbc-migrate-core` for auto setup/run of db migrations.

##### top level 
RoadApplication - Set up the ktor app 

##### /config
Installs some common Koin components and houses the database setup/connection. This also contains
the call to migrate the database. This currently happens on the first access to `getDb()`. That can
be improved by running it on app startup instead. 

##### /route
Configures all routing and documentation. Each api is separated into its own file. 

##### /core
Holds the core service layer, responsible for any business logic needed between the incoming request
and the database. Each api has its own service. 

##### /db
`common` - holds some common database class definitions (this should go in the common shared lib...) and a DAO facade to access the DB operations

`model` - Entity definitions and implementations of the DAO interface. Each api has its own DAO implementation. 

##### app resources
`application.conf` - as it sounds, ktor app config

`db.migrations` - SQL files for db migrations

## Accessing the DB
There is a webserver setup with H2's `Server.createWebServer`. This opens a simple DB app on port 8082
allowing viewing of the DB schema and running SQL

Connection information:
`Driver Class`: org.h2.Driver
`JDBC URL`: jdbc:h2:mem:road_service_db

*** The db is created on the FIRST api access.

## Accessing API docs


### TODO 
- request validation
- comment code
- this readme
- more unittests
- api tests
- PUT apis
- docker (i think completed ?)
- caching
- CORS correct ?
- remove unused imports and code