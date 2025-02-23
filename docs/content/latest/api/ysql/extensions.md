---
title: Extensions
linkTitle: Extensions
description: Install and use Extensions
summary: Reference for YSQL Extensions
menu:
  latest:
    identifier: api-ysql-extensions
    parent: api-ysql
    weight: 4400
isTocNested: true
showAsideToc: true
---

This page documents how to install and use PostgreSQL extensions that are tested to work with YSQL. Note that since YugabyteDB’s storage architecture is not the same as that of native PostgreSQL, PostgreSQL extensions especially those that interact with the storage layer are not expected to work as-is on YugabyteDB. We intend to incrementally develop support for as many extensions as possible. 

## Use Included Extensions

These are extensions that are included in the standard YugabyteDB distribution and can be enabled in YSQL by simply running the `CREATE EXTENSION` statememt.

### pgcrypto

The `pgcrypto` extension provides various cryptographic functions.

<!-- 
  Use explicit html for example throughout this file to exclude example 
  subsections from the generated table of contents.
-->
<h4> Example </h4>

```sql
CREATE EXTENSION pgcrypto;
CREATE TABLE pgcrypto_example(id uuid PRIMARY KEY DEFAULT gen_random_uuid(), content text, digest text);
INSERT INTO pgcrypto_example (content, digest) values ('abc', digest('abc', 'sha1'));
SELECT * FROM pgcrypto_example;
```

```
                  id                  | content |                   digest
--------------------------------------+---------+--------------------------------------------
 b8f2e2f7-0b8d-4d26-8902-fa4f5277869d | abc     | \xa9993e364706816aba3e25717850c26c9cd0d89d
(1 row)
```

For more information see [`pgcrypto`](https://www.postgresql.org/docs/current/pgcrypto.html) in the PostgreSQL Docs.


### fuzzystrmatch

The `fuzzystrmatch` extension provides several functions to determine similarities and distance between strings.

<h4> Example </h4>

```sql
CREATE EXTENSION fuzzystrmatch;
SELECT levenshtein('Yugabyte', 'yugabyte'), metaphone('yugabyte', 8);
```

```
 levenshtein | metaphone
-------------+-----------
           2 | YKBT
(1 row)
```

### spi module

The spi module includes several separate extensions using the Server Programming Interface (SPI) and triggers.
The specific extensions currently supported in YSQL are:

- `insert_username`: functions for tracking who changed a table
- `moddatetime`: Functions for tracking last modification time
- `autoinc`: functions for autoincrementing fields
- `refint`: functions for implementing referential integrity

<h4> Example </h4>

1. Set up a table with triggers for tracking modification time and user (role).
    Connect with `ysqlsh` and run the commands below.
    ```sql
    CREATE EXTENSION insert_username;
    CREATE EXTENSION moddatetime;

    CREATE TABLE spi_test (
      id int primary key,
      content text,
      username text not null,
      moddate timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL
    );

    CREATE TRIGGER insert_usernames
      BEFORE INSERT OR UPDATE ON spi_test
      FOR EACH ROW
      EXECUTE PROCEDURE insert_username (username);

    CREATE TRIGGER update_moddatetime
      BEFORE UPDATE ON spi_test
      FOR EACH ROW
      EXECUTE PROCEDURE moddatetime (moddate);
```

2. Insert some rows.
    Each insert should add the current role as `username` and the current timestamp as `moddate`.

    ```sql
    SET ROLE yugabyte;
    INSERT INTO spi_test VALUES(1, 'desc1');

    SET ROLE postgres;
    INSERT INTO spi_test VALUES(2, 'desc2');
    INSERT INTO spi_test VALUES(3, 'desc3');

    SET ROLE yugabyte;
    INSERT INTO spi_test VALUES(4, 'desc4');

    SELECT * FROM spi_test ORDER BY id;
    ```

    ```
     id | content | username |          moddate
    ----+---------+----------+----------------------------
      1 | desc1   | yugabyte | 2019-09-13 16:55:53.969907
      2 | desc2   | postgres | 2019-09-13 16:55:53.983306
      3 | desc3   | postgres | 2019-09-13 16:55:53.98658
      4 | desc4   | yugabyte | 2019-09-13 16:55:53.991315
    (4 rows)
    ```

  {{< note title="Note" >}}

  YSQL should have users `yugabyte` and (for compatibility) `postgres` created by default. 

  {{< /note >}}

3. Update some rows.
    Should update both `username`  and `moddate` accordingly.

    ```sql
    UPDATE spi_test SET content = 'desc1_updated' WHERE id = 1;
    UPDATE spi_test SET content = 'desc3_updated' WHERE id = 3;

    SELECT * FROM spi_test ORDER BY id;
    ```

    ```
     id |    content    | username |          moddate
    ----+---------------+----------+----------------------------
      1 | desc1_updated | yugabyte | 2019-09-13 16:56:27.623513
      2 | desc2         | postgres | 2019-09-13 16:55:53.983306
      3 | desc3_updated | yugabyte | 2019-09-13 16:56:27.634099
      4 | desc4         | yugabyte | 2019-09-13 16:55:53.991315
    (4 rows)
    ```
For more information see [`spi module`](https://www.postgresql.org/docs/current/contrib-spi.html) in the PostgreSQL Docs.

## Install Other Extensions

Other extensions have to be installed manually before they can be enabled (with `CREATE EXTENSION`).

{{< note title="Note" >}}

Currently, in a multi-node setup, the installation instructions below must be done on _every_ node in the cluster.

{{< /note >}}


Typically extensions need three types of files:

- Shared library files (`<name>.so`)
- SQL files (`<name>--<version>.sql`)
- Control files (`<name>.control`)

In order to install an extension you need to copy these files into the respective directories of your YugabyteDB installation.


Shared library files will be in the `pkglibdir` directory while SQL and control files should be in the `extension` subdirectory of the `libdir` directory.
To find these directories on your local installation, you can use Yugabyte's `pg_config` executable.
First, alias it to `yb_pg_config` by replacing `<yugabyte-path>` with the path to your YugabyteDB installation in the command below and then running it.  
```sh
$ alias yb_pg_config=/<yugabyte-path>/postgres/bin/pg_config
```

Now you can list existing shared libraries with:
```sh
$ ls "$(yb_pg_config --pkglibdir)"
```

And SQL and control files for already-installed extensions with:
```sh
$ ls "$(yb_pg_config --sharedir)"/extension/
```

To obtain these files for your target extension, you can build it from scratch following the extension's build instructions.
Alternatively, if you already have PostgreSQL (ideally version `11.2` for best YSQL compatibility) with that extension installed, then you can find these files as follows:

```sh
$ ls "$(pg_config --pkglibdir)" | grep <name>
```

```sh
$ ls "$(pg_config --sharedir)"/extension/ | grep <name>
```

Copy those files to the YugabyteDB installation.
Restart the cluster (or the respective node in a multi-node install).
Finally, connect to the cluster with `ysqlsh` and run the `CREATE EXTENSION` statement to create the extension.


{{< note title="Note" >}}

Only some extensions are currently supported.
If you encounter any problems with installing or using a particular extension please post an issue on our [GitHub](https://github.com/yugabyte/yugabyte-db/issues).

{{< /note >}}


### PostGIS

[PostGIS](https://postgis.net/) is a spatial database extender for PostgreSQL-compatible object-relational databases.
The simplest way to set it up locally is to install it together with regular PostgreSQL.

For instance, on macOS, you can either

- download and install [Postgres.app](https://postgresapp.com/)
- install with Homebrew:

    ```sh
    $ brew install postgres && brew install postgis
    ```

Now follow the instructions described above to copy the needed files into your YugabyteDB installation, and then create 
the extension.

```sh
$ cp -v "$(pg_config --pkglibdir)"/*postgis*.so "$(yb_pg_config --pkglibdir)" && 
  cp -v "$(pg_config --sharedir)"/extension/*postgis*.sql "$(yb_pg_config --sharedir)"/extension && 
  cp -v "$(pg_config --sharedir)"/extension/*postgis*.control "$(yb_pg_config --sharedir)"/extension &&
  ./bin/ysqlsh -c "CREATE EXTENSION postgis";
```

This might take a couple of minutes.

<h4> Example </h4>

1. Get a sample [postgis dataset](https://data.edmonton.ca/Geospatial-Boundaries/City-of-Edmonton-Neighbourhood-Boundaries/jfvj-x253):

    ```sh
    $ wget -O edmonton.zip "https://data.edmonton.ca/api/geospatial/jfvj-x253?method=export&format=Shapefile" && unzip edmonton.zip
    ```

2. Extract the dataset using the `shp2pgsql` tool.
    This should come with your PostgreSQL installation, it is not yet packaged with YSQL.

    ```sh
    $ shp2pgsql geo_export_*.shp > edmonton.sql
    ```

3. Edit the generated `edmonton.sql` for YSQL compatibility.
    First inline the `PRIMARY KEY` declaration for `gid` as YSQL does not yet support adding primary key contraints after the table creation.
    Additionally, for simplicity, change the table name (and references to it in the associated `INSERT`s) to just `geo_export` (i.e. remove the UUID postfix).
    The `edmonton.sql` file should now start as follows:

    ```
    SET CLIENT_ENCODING TO UTF8;
    SET STANDARD_CONFORMING_STRINGS TO ON;
    BEGIN;
    CREATE TABLE "geo_export" (gid serial PRIMARY KEY,
      "area_km2" numeric,
      "name" varchar(254),
      "number" numeric);
    SELECT AddGeometryColumn('','geo_export','geom','0','MULTIPOLYGON',2);

    INSERT INTO "geo_export" ("area_km2","name","number",geom) VALUES ...
  ```

4. Load the sample data.

    ```sh
    $ ./bin/ysqlsh -a -f edmonton.sql
    ```

5. Run some sample queries.
    Connect with `ysqlsh` and run:

    ```sql
    SELECT name, area_km2, ST_Area(geom), ST_Area(geom)/area_km2 AS area_ratio FROM "geo_export" LIMIT 10;
    ```

    ```
                name            |     area_km2      |       st_area        |      area_ratio
    ----------------------------+-------------------+----------------------+----------------------
     River Valley Terwillegar   | 3.077820277027079 | 0.000416617423004673 | 0.000135361192501822
     Carleton Square Industrial | 0.410191631391664 | 5.56435079305678e-05 | 0.000135652469899947
     Cy Becker                  | 1.015144841249301 | 0.000137900847258255 | 0.000135843518732308
     Elsinore                   | 0.841471068786406 | 0.000114331091817771 |  0.00013587049639468
     McLeod                     | 0.966538217483227 | 0.000131230296771637 | 0.000135773520796051
     Gainer Industrial          | 0.342464541730177 | 4.63954326887451e-05 | 0.000135475142782225
     Coronet Industrial         | 1.606907195063447 | 0.000217576340986435 | 0.000135400688760899
     Marquis                    | 9.979100854886905 |  0.00135608901739072 | 0.000135892906295924
     South Terwillegar          | 1.742840325820606 | 0.000235695089933611 | 0.000135236192576985
     Carlisle                   | 0.961897333826841 | 0.000130580966739925 | 0.000135753538499185
    (10 rows)
    ```

    ```sql
    SELECT a.name, b.name FROM "geo_export" AS a, "geo_export" AS b
    WHERE ST_Intersects(a.geom, b.geom) AND a.name LIKE 'University of Alberta';
    ```

    ```
             name          |          name
    -----------------------+-------------------------
     University of Alberta | University of Alberta
     University of Alberta | McKernan
     University of Alberta | Belgravia
     University of Alberta | Garneau
     University of Alberta | River Valley Mayfair
     University of Alberta | River Valley Walterdale
     University of Alberta | Windsor Park
    (7 rows)
  ```

{{< note title="Note" >}}

YSQL does not yet support GiST indexes. This is tracked in [this GitHub issue](https://github.com/yugabyte/yugabyte-db/issues/1337).

{{< /note >}}


### uuid-ossp

The `uuid-ossp` extension provides functions to generate universally unique identifiers (UUIDs) and also functions to produce certain special UUID constants.

The easiest way to install it is to copy the files from an existing PostgreSQL installation into Yugabyte, and then create the extension.

```sh
$ cp -v "$(pg_config --pkglibdir)"/*uuid-ossp*.so "$(yb_pg_config --pkglibdir)" && 
  cp -v "$(pg_config --sharedir)"/extension/*uuid-ossp*.sql "$(yb_pg_config --sharedir)"/extension && 
  cp -v "$(pg_config --sharedir)"/extension/*uuid-ossp*.control "$(yb_pg_config --sharedir)"/extension &&
  ./bin/ysqlsh -c "CREATE EXTENSION \"uuid-ossp\"";
```

<h4> Example </h4>

Connect with `ysqlsh` and run:

```sql
SELECT uuid_generate_v1(), uuid_generate_v4(), uuid_nil();
```

```
           uuid_generate_v1           |           uuid_generate_v4           |               uuid_nil
--------------------------------------+--------------------------------------+--------------------------------------
 69975ce4-d827-11e9-b860-bf2e5a7e1380 | 088a9b6c-46d8-4276-852b-64908b06a503 | 00000000-0000-0000-0000-000000000000
(1 row)
```
