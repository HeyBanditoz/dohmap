# DOH Map!

_Mapping Salt Lake County health inspections._

## Contributing

You need a Postgres database to run the project.

Copy the `src/main/resources/application-local.example.yml` file to `src/main/resources/application.yml` and set up your
database. If you want to run the Selenium indexers, set `dohmap.selenium.sessions` to be over 0.

## Brief

This projects ingests data from Salt Lake
County's [health inspection database](https://public.cdpehs.com/UTEnvPbl/VW_EST_PUBLIC/ShowVW_EST_PUBLICTablePage.aspx)
by means of scraping most of the data available on the site through concurrent Selenium browser sessions.

Right now, the project is still in development, and the browser sessions are only ran on startup, controlled by the
`dohmap.selenium.sessions` property. I want to change this to be nightly though If I ever deploy it somewhere. **It's
not ready for public use yet!**

Selenium was used because there is no API and their frontend/backend seems to _heavily_ sync state using JS. Seriously,
to view an inspection page calls some JS function that only increments an ID based on the index of the row.

The webpages are rendered server-side using Thymeleaf. Bootstrap is used as the front-end framework, Leaflet to display
the maps, and jQuery 'cause it's old school ;) and I don't need much else.

Within the database, IDs are stored
as [uuidv7](https://buildkite.com/blog/goodbye-integers-hello-uuids), which also act as a `created_on` timestamp for all
entities. The `com.github.f4b6a3:uuid-creator` library is used for this. If you want to convert uuidv7 IDs to SQL
timestamps (such as `timestamp with time zone`) within Postgres,
install the [pg-uuidv7](https://pgxn.org/dist/pg_uuidv7/) extension.

## Assumptions about their data model

Unfortunately, since the site they use seems pretty old (check it out! they use XP-themed tables and such), they do not
expose any IDs of any kind.

Restaurant distinctiveness is currently determined if the name, address, city, state, ZIP, phone, and establishment type
match, if they don't, a new restaurant is created. Same with all inspection and violation fields.

Running the Selenium browsers _shouldn't™_ recreate data, but there are probably bugs. If a restaurant gets a new phone
number, or changes their name, there'll be a new restaurant as there don't seem to be any exposed unique identifiers.
TODO maybe a manual merging process?

## The future?!

* Account sign-up
* Alerts if establishments you select match criteria (critical violations on new inspection)
    * Discord alert destination
* Merge establishments
* Edit locations
* Maybe ingest [Utah County inspection data](http://www.inspectionsonline.us/foodsafety/ututahprovo/search.htm) (whole
  other website, would need to adjust the data model a good bit to support it.)
