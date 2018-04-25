# Table of contents

* [Requirements](https://github.com/ga4gh-beacon/beacon-elixir/blob/v0.4/README.md#requirements)
* [Quick start](https://github.com/ga4gh-beacon/beacon-elixir/blob/v0.4/README.md#quick-start)
* [Configure databases](https://github.com/ga4gh-beacon/beacon-elixir/blob/v0.4/README.md#configure-databases)
  * [Create databases](https://github.com/ga4gh-beacon/beacon-elixir/blob/v0.4/README.md#create-databases)
  * [Load the data](https://github.com/ga4gh-beacon/beacon-elixir/blob/v0.4/README.md#load-the-data)
* [Managing the code](https://github.com/ga4gh-beacon/beacon-elixir/blob/v0.4/README.md#managing-the-code)
  * [Download the project](https://github.com/ga4gh-beacon/beacon-elixir/blob/v0.4/README.md#download-the-project)
  * [Elixir Core](https://github.com/ga4gh-beacon/beacon-elixir/blob/v0.4/README.md#elixir-core)
  * [Elixir Beacon, the main project](https://github.com/ga4gh-beacon/beacon-elixir/blob/v0.4/README.md#elixir-beacon-the-main-project)
    * [Configuration files](https://github.com/ga4gh-beacon/beacon-elixir/blob/v0.4/README.md#configuration-files)
  * [Compile and test the code](https://github.com/ga4gh-beacon/beacon-elixir/blob/v0.4/README.md#compile-and-test-the-code)
  * [Deploy the JAR](https://github.com/ga4gh-beacon/beacon-elixir/blob/v0.4/README.md#deploy-the-jar)
  * [Run integration tests](https://github.com/ga4gh-beacon/beacon-elixir/blob/v0.4/README.md#run-integration-tests)
* [Using the application](https://github.com/ga4gh-beacon/beacon-elixir/blob/v0.4/README.md#using-the-application)
  * [/beacon/](https://github.com/ga4gh-beacon/beacon-elixir/blob/v0.4/README.md#beacon)
  * [/beacon/query](https://github.com/ga4gh-beacon/beacon-elixir/blob/v0.4/README.md#beaconquery)
* [Further information](https://github.com/ga4gh-beacon/beacon-elixir/blob/v0.4/README.md#further-information)
  * [Project structure](https://github.com/ga4gh-beacon/beacon-elixir/blob/v0.4/README.md#project-structure)
  * [Extend/Change functionality](https://github.com/ga4gh-beacon/beacon-elixir/blob/v0.4/README.md#extendchange-functionality)
* For older version v0.3
  * [Docker (previous version v0.3)](https://github.com/ga4gh-beacon/beacon-elixir/blob/v0.4/README.md#docker-previous-version-v03)
  * [Docker UI (previous version v0.3)](https://github.com/ga4gh-beacon/beacon-elixir/blob/v0.4/README.md#docker-ui-previous-version-v03)

# Requirements
* Java 8 JDK
* Apache Maven 3
* PostgreSQL Server 9.0+, or any other SQL server (i. e. MySQL)
* JMeter

# Quick start
This quick start guide uses the default configuration and sets the application up using some sample data. It requires a Postgres server running in the local machine and listening to the default port 5432.

If you want to tune the configuration or load custom data, please, skip this section and keep reading.

* Create 2 databases and a new user (use *r783qjkldDsiu* as password)
```
createuser -P microaccounts_dev
psql -h localhost -p 5432 -U postgres
```
```sql
CREATE DATABASE elixir_beacon_dev;
CREATE DATABASE elixir_beacon_testing;
GRANT ALL PRIVILEGES ON DATABASE elixir_beacon_dev TO microaccounts_dev;
GRANT ALL PRIVILEGES ON DATABASE elixir_beacon_testing TO microaccounts_dev;
```
* Load the schema (download [elixir_beacon_db_schema.sql](https://raw.githubusercontent.com/elixirhub/human-data-beacon/v0.4/elixir_beacon/src/main/resources/META-INF/elixir_beacon_db_schema.sql))
```
wget https://raw.githubusercontent.com/elixirhub/human-data-beacon/v0.4/elixir_beacon/src/main/resources/META-INF/elixir_beacon_db_schema.sql
psql -h localhost -p 5432 -d elixir_beacon_dev -U microaccounts_dev < elixir_beacon_db_schema.sql
psql -h localhost -p 5432 -d elixir_beacon_testing -U microaccounts_dev < elixir_beacon_db_schema.sql
```
* Load data (download [EGAD00000000028.SNPs](https://raw.githubusercontent.com/elixirhub/human-data-beacon/v0.4/elixir_beacon/src/main/resources/META-INF/EGAD00000000028.SNPs))
```
psql -h localhost -p 5432 -d elixir_beacon_dev -U microaccounts_dev
```
```sql
INSERT INTO beacon_dataset_table(id, stable_id, description, access_type, reference_genome, variant_cnt, call_cnt, sample_cnt)
  VALUES (1, 'EGAD00000000028', 'Sample variants', 'PUBLIC', 'grch37', 74, 74, 1);
```
```
wget https://raw.githubusercontent.com/elixirhub/human-data-beacon/v0.4/elixir_beacon/src/main/resources/META-INF/EGAD00000000028.SNPs
cat EGAD00000000028.SNPs | psql -h localhost -p 5432 -U microaccounts_dev -c "copy beacon_data_table (dataset_id,start,chromosome,reference,alternate,\"end\","type",sv_length,variant_cnt,call_cnt,sample_cnt,frequency) FROM STDIN USING DELIMITERS ';' CSV" elixir_beacon_dev
```
* Create the function (download [elixir_beacon_function.sql](https://raw.githubusercontent.com/ga4gh-beacon/beacon-elixir/v0.4/elixir_beacon/src/main/resources/META-INF/elixir_beacon_function.sql))
```
wget https://raw.githubusercontent.com/ga4gh-beacon/beacon-elixir/v0.4/elixir_beacon/src/main/resources/META-INF/elixir_beacon_function.sql
psql -h localhost -p 5432 -d elixir_beacon_dev -U microaccounts_dev < elixir_beacon_function.sql
psql -h localhost -p 5432 -d elixir_beacon_testing -U microaccounts_dev < elixir_beacon_function.sql
```
* Download the code
```
git clone https://github.com/elixirhub/human-data-beacon.git
```
* Prepare dependencies
```
cd elixir_core
mvn clean compile jar:jar
mvn install:install-file -Dfile=/path_to_project_folder/elixir_core/target/elixir-core-beacon_api_v0.4-SNAPSHOT.jar -DgroupId=org.ega_archive -DartifactId=elixir-core -Dversion=beacon_api_v0.4-SNAPSHOT -Dpackaging=jar -DgeneratePom=true
```
* Compile and deploy the application
```
cd elixir_beacon
mvn clean compile package
java -jar target/elixir-beacon-0.4.jar --spring.profiles.active=dev
```
* Go to 
  * [localhost:9075/elixirbeacon/v04/beacon/](http://localhost:9075/elixirbeacon/v04/beacon/)
  * [localhost:9075/elixirbeacon/v04/beacon/query?referenceName=1&start=14929&referenceBases=A&alternateBases=G&assemblyId=GRCh37&datasetIds=EGAD00000000028](http://localhost:9075/elixirbeacon/v04/beacon/query?referenceName=1&start=14929&referenceBases=A&alternateBases=G&assemblyId=GRCh37&datasetIds=EGAD00000000028)
  * [localhost:9075/elixirbeacon/v04/beacon/query?referenceName=1&start=14929&referenceBases=A&alternateBases=G&assemblyId=GRCh37&datasetIds=EGAD00000000028&includeDatasetResponses=HIT](http://localhost:9075/elixirbeacon/v04/beacon/query?referenceName=1&start=14929&referenceBases=A&alternateBases=G&assemblyId=GRCh37&datasetIds=EGAD00000000028&includeDatasetResponses=HIT)

# Configure databases
## Create databases
* Connect to the server:
  ```
  psql -h localhost -p 5432 -U postgres
  ```
  NOTE: You will need a user with enough permissions to create databases.

* Create two databases. Default names are:
    * `elixir_beacon_dev`: this is the main database that will be used by the application.
    * `elixir_beacon_testing`: this is a secondary database that will be used to run the tests.
  ```sql
  CREATE DATABASE elixir_beacon_dev;
  CREATE DATABASE elixir_beacon_testing;
  ```
  NOTE: If you want to use a different name, user or your Postgres server is running in a different host or is listening to a different port, please, replace the values in the previous command.

  * These are the most common options used in the commands of this section:
    * `-d`: database name (depending on the command the database name will be specified with this option).
    * `-h`: hostname or IP of the machine where the Postgres server is running.
    * `-p`: port that the Postgres server is listening to.
    * `-U`: user name that will be used to connect to the database.

* Create a user that will be used by the application to connect to the databases just created:
  ```
  createuser -P microaccounts_dev
  ```
  This command will prompt for the password of the new user.

* Log in each of the databases and grant privileges to the user just created:
  ```
  psql elixir_beacon_dev -U postgres
  ```
  ```sql
  GRANT ALL PRIVILEGES ON DATABASE elixir_beacon_dev TO microaccounts_dev;
  GRANT ALL PRIVILEGES ON DATABASE elixir_beacon_testing TO microaccounts_dev;
  ```

  NOTE: You can skip this step and load the schema using a super user in the next step and after that, granting privileges to a different user (this user will be used by the application to connect to the database).

* Download the schema [elixir_beacon_db_schema.sql](https://raw.githubusercontent.com/elixirhub/human-data-beacon/v0.4/elixir_beacon/src/main/resources/META-INF/elixir_beacon_db_schema.sql) and load it in **both** databases: 
  ```
  wget https://raw.githubusercontent.com/elixirhub/human-data-beacon/v0.4/elixir_beacon/src/main/resources/META-INF/elixir_beacon_db_schema.sql
  psql -h localhost -p 5432 -d elixir_beacon_dev -U microaccounts_dev < elixir_beacon_db_schema.sql
  psql -h localhost -p 5432 -d elixir_beacon_testing -U microaccounts_dev < elixir_beacon_db_schema.sql
  ```
  That script will create the schema and also load some essential data for data use conditions.

  If you use a super user to create the schema then you will need to grant access to the ordinary user that will be used by the application (that user we created in the second step):
  ```
  psql -h localhost -p 5432 -d elixir_beacon_dev -U postgres
  ```
  ```sql
  GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO microaccounts_dev;
  GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO microaccounts_dev;
  ```
  Remember to run these lines in both databases.
* Load the function [elixir_beacon_function.sql](https://raw.githubusercontent.com/ga4gh-beacon/beacon-elixir/v0.4/elixir_beacon/src/main/resources/META-INF/elixir_beacon_function.sql):
  ```
  wget https://raw.githubusercontent.com/ga4gh-beacon/beacon-elixir/v0.4/elixir_beacon/src/main/resources/META-INF/elixir_beacon_function.sql
  psql -h localhost -p 5432 -d elixir_beacon_dev -U microaccounts_dev < elixir_beacon_function.sql
  psql -h localhost -p 5432 -d elixir_beacon_testing -U microaccounts_dev < elixir_beacon_function.sql
  ```
## Load the data
* Download the [script](https://raw.githubusercontent.com/elixirhub/human-data-beacon/v0.4/elixir_beacon/src/main/resources/META-INF/vcf_parser.sh) to parse VCF files and give it executable rights:
  ```
  wget https://raw.githubusercontent.com/elixirhub/human-data-beacon/v0.4/elixir_beacon/src/main/resources/META-INF/vcf_parser.sh
  chmod +x vcf_parser.sh
  ```
* Run this script executing:
  ```
  ./vcf_parser.sh dataset_id < file.vcf
  ```
  This script will generate 2 output files: `dataset_id_filename.SNPs` and `dataset_id_filename.samples`.

* Load the dataset information into `beacon_dataset_table`:
  ```sql
  INSERT INTO beacon_dataset_table(id, stable_id, description, access_type, reference_genome, variant_cnt, call_cnt, sample_cnt)
    VALUES (1, 'EGAD00000000028', 'Sample variants', 'PUBLIC', 'grch37', 1, 1, 1);
  ```
  Initialize the row setting `variant_cnt`, `call_cnt` and `sample_cnt` to 1. After loading the data, count and set the real counts.
  Remember to replace the values in the previous command with the correct ones. Use lower case in the `reference_genome` field.

* Load the generated file into `beacon_data_table`:
  ```
  cat filename.SNPs | psql -h localhost -p 5432 -U microaccounts_dev -c "copy beacon_data_table (dataset_id,start,chromosome,reference,alternate,\"end\","type",sv_length,variant_cnt,call_cnt,sample_cnt,frequency) FROM STDIN USING DELIMITERS ';' CSV" elixir_beacon_dev
  ```
  NOTE: This command should be executed only in the `elixir_beacon_dev` database. The testing database will be initialized with some data when the tests are run.

* Update counts in `beacon_dataset_table`
   * Get counts from database:
  ```sql
  select dataset_id, count(*) as variant_count
  from beacon_data_table
  group by dataset_id;

  select dataset_id, sum(call_cnt) as call_count
  from beacon_data_table
  group by dataset_id;
  ```
   * Extract the sample count counting the elements listed in `filename.samples` file
  ```
  wc -l filename.samples
  ```
   * Update the row:
  ```sql
  UPDATE beacon_dataset_table SET variant_cnt=74, call_cnt=126, sample_cnt=1 WHERE id=1;
  ```

# Managing the code
## Download the project
Clone the projects **elixir_beacon** (current one) and **elixir_core** located at the [Elixir's repository](https://github.com/ga4gh-beacon/beacon-elixir).
```
git clone https://github.com/ga4gh-beacon/beacon-elixir.git
```

## Elixir Core
First of all, it is necessary to compile the code of the **elixir_core** project because it is a dependency of the main project, elixir_beacon.
```
cd elixir_core
mvn clean compile jar:jar
```
This will generate the JAR file `elixir-core-beacon_api_v0.4-SNAPSHOT.jar` inside the `/target` folder.

Then execute:
```
mvn install:install-file -Dfile=/path_to_project_folder/elixir_core/target/elixir-core-beacon_api_v0.4-SNAPSHOT.jar -DgroupId=org.ega_archive -DartifactId=elixir-core -Dversion=beacon_api_v0.4-SNAPSHOT -Dpackaging=jar -DgeneratePom=true
```
Now this dependency will be found when compiling the main project, elixir_beacon.
NOTE: Remember to replace the `/path_to_project_folder/` part witht a valid one.

## Elixir Beacon, the main project
### Configuration files
The key files are `/src/main/resources/application-{profile}.properties` and `/src/test/resources/application-{profile}.properties` (see [Deploy JAR](https://github.com/elixirhub/human-data-beacon/blob/v0.4/README.md#deploy-the-jar) for more information about profiles).

By default, the application is deployed at port **9075** and the context is **/elixirbeacon/v04/**. You can change this by modifying the following lines of the application-{profile}.properties file:
```INI
server.port=9075
server.servlet-path=/v04
server.context-path=/elixirbeacon
```
As explained at the beginning, the application uses two PostgreSQL databases named `elixir_beacon_dev` and `elixir_beacon_testing`.
```INI
datasource.elixirbeacon.url=jdbc:postgresql://127.0.0.1:5432/elixir_beacon_dev
datasource.elixirbeacon.username=microaccounts_dev
datasource.elixirbeacon.password=PUT HERE YOUR PASSWORD
datasource.elixirbeacon.driverClassName=org.postgresql.Driver
```
* Specify the type of the database (postgresql), the hostname (default, 127.0.0.1), port (default, 5432) and finally the database name.
    * I. e. if you use MySQL: `jdbc:mysql`
* Username that will be used to connect to the database (default, microaccounts_dev).
* Password of that username.
* Driver class name 
    * if you use MySQL: com.mysql.jdbc.Driver
  ```INI
  spring.jpa.properties.hibernate.dialect = org.hibernate.dialect.PostgreSQLDialect
  ```
* Set the Hibernate dialect.
    * if you use MySQL: `org.hibernate.dialect.MySQLDialect`
 
If you use a different DB than Postgres, you must add the corresponding library to the **/lib** folder inside the JAR (you don't need to recompile) or add the dependency to the pom.xml so maven can download the library (this will force you to compile, see next step).

## Compile and test the code
To compile the code run the following command within the project folder:
```
cd elixir_beacon
mvn clean compile package -Dspring.profiles.active="dev"
```
That will also execute the tests. To skip them add `-Dmaven.test.skip=true` to the command.
NOTE: Tests use a different properties file located in `/src/test/resources`.

To execute only the tests run:
```
mvn test -Dspring.profiles.active="dev"
```
NOTE: To execute the tests you should use a different database than the main one (i. e. `elixir_beacon_testing`, see [Create databases](https://github.com/elixirhub/human-data-beacon/blob/v0.4/README.md#create-databases)).

If compilation and test execution are successful, a JAR file will be generated in the folder `/target` with the name `elixir-beacon-0.4.jar`.

## Deploy the JAR
To deploy the JAR run run the following command within the **elixir_beacon/target** folder:
 ```
java -jar target/elixir-beacon-0.4.jar --spring.profiles.active=dev
 ```
It will generate a log file in `/logs/application.log` located in the same folder where the JAR has been launched (e.g. `elixir_beacon/logs` but you can move the JAR file wherever you want and deploy it there).

This argument `--spring.profiles.active=dev` specifies the profile to be used. By default, there are 2 profiles: `dev` and `test`. Each profile will use its own set of properties files (e.g. `dev` profile uses `application-dev.properties` and `application-dev.yml`).

Using the default configuration, the application will be available at: [localhost:9075/elixirbeacon/v04/](http://localhost:9075/elixirbeacon/v04/)

## Run integration tests
We use JMeter to run this kind of tests. We have an artifact called **elixir-beacon-service-tests**. 

Run:
```
cd elixir_beacon_tests
mvn -P local clean verify
 ```
This will download jmeter and run some basic tests.

The `-P local` refers to a maven profile. These profiles can be found in the file `pom.xml`. The `local` profile uses this configuration for running the tests:
* host: localhost 
* port: 9075 

For other configurations please add a profile to `pom.xml`. You will see the results on the console.

# Using the application
The application publishes two endpoints:
* /beacon/
* /beacon/query

They are defined in the `org.ega_archive.elixirbeacon.ElixirBeaconController` class.

## /beacon/
Returns the information about this beacon: its Id, name and description, the API version it is compliant with, the URL where you can access this beacon, etc.

[localhost:9075/elixirbeacon/v04/beacon/](http://localhost:9075/elixirbeacon/v04/beacon/)
```json
{
  "id" : "elixir-demo-beacon",
  "name" : "Elixir Demo Beacon",
  "apiVersion" : "0.4",
  "organization" : {
    "id" : "EGA",
    "name" : "European Genome-Phenome Archive (EGA)",
    "description" : "The European Genome-phenome Archive (EGA) is a service for permanent archiving and sharing of all types of personally identifiable genetic and phenotypic data resulting from biomedical research projects.",
    "address" : "",
    "welcomeUrl" : "https://ega-archive.org/",
    "contactUrl" : "mailto:beacon.ega@crg.eu",
    "logoUrl" : "https://ega-archive.org/images/logo.png",
    "info" : null
  },
  "description" : "This <a href=\"http://ga4gh.org/#/beacon\">Beacon</a> is based on the GA4GH Beacon <a href=\"https://github.com/ga4gh/schemas/blob/beacon/src/main/resources/avro/beacon.avdl\"></a>",
  "version" : "v04",
  "welcomeUrl" : "https://ega-archive.org/elixir_demo_beacon/",
  "alternativeUrl" : "https://ega-archive.org/elixir_demo_beacon_web/",
  "createDateTime" : "2015-06-01T00:00.000Z",
  "updateDateTime" : null,
  "datasets" : [ {
    "id" : "EGAD00000000028",
    "name" : null,
    "description" : "Sample variants",
    "assemblyId" : "grch37",
    "createDateTime" : null,
    "updateDateTime" : null,
    "dataUseConditions" : {
      "consentCodedataUse" : {
        "primaryCategory" : null,
        "secondaryCategories" : [ ],
        "requirements" : [ ],
        "version" : null
      }
    },
    "version" : null,
    "variantCount" : 74,
    "callCount" : 74,
    "sampleCount" : 1,
    "externalUrl" : null,
    "info" : {
      "accessType" : "PUBLIC",
      "authorized" : "true"
    }
  } ],
  "sampleAlleleRequests" : [ {
    "alternateBases" : "C",
    "referenceBases" : "A",
    "referenceName" : "1",
    "start" : 14929,
    "startMin" : null,
    "startMax" : null,
    "end" : null,
    "endMin" : null,
    "endMax" : null,
    "variantType" : null,
    "assemblyId" : "GRCh37",
    "datasetIds" : null,
    "includeDatasetResponses" : null
  }, {
    "alternateBases" : null,
    "referenceBases" : "N",
    "referenceName" : "X",
    "start" : null,
    "startMin" : 153592310,
    "startMax" : 153592317,
    "end" : null,
    "endMin" : 153517030,
    "endMax" : 153517050,
    "variantType" : null,
    "assemblyId" : "GRCh37",
    "datasetIds" : [ "EGAD00000000028" ],
    "includeDatasetResponses" : null
  }, {
    "alternateBases" : null,
    "referenceBases" : "N",
    "referenceName" : "X",
    "start" : 147880925,
    "startMin" : null,
    "startMax" : null,
    "end" : 146342284,
    "endMin" : null,
    "endMax" : null,
    "variantType" : null,
    "assemblyId" : "GRCh37",
    "datasetIds" : [ "EGAD00000000028" ],
    "includeDatasetResponses" : null
  } ],
  "info" : {
    "size" : "74"
  }
}
```
The 3 examples that appear in field ` sampleAlleleRequests` can be customized by modifying the following properties in `/src/main/resources/application-{profile}.yml`:
```yml
#properties
#sample #1
querySamples:
  assembly-id-1: GRCh37
  position-1: 6689
  reference-name-1: 17
  alternate-bases-1: 
  dataset-ids-1: 
#sample #2
  assembly-id-2: GRCh37
  position-2: 1040026
  reference-name-2: 1
  alternate-bases-2: 
  dataset-ids-2: EGAD00001000740,EGAD00001000741
#sample #3
  assembly-id-3: GRCh37
  position-3: 1040026
  reference-name-3: 1
  alternate-bases-3: C
  dataset-ids-3: EGAD00001000740
```

## /beacon/query
To actually ask the beacon for questions like "do you have any genomes with an 'A' at position 100,735 on chromosome 3?" And the answer will be yes or no.

Parameters (required in bold):
* **`assemblyId`**
    Assembly identifier (GRC notation, e.g. GRCh37).
* **`referenceName`**
    Reference name (chromosome). Accepting values 1-22, X, Y.
* `start`
  Precise start coordinate position, allele locus (0-based).
  -   `start` only:
      -   for single positions, e.g. the start of a specified sequence alteration where the size is given through the specified `alternateBases`
      -   typical use are queries for SNV and small InDels
      -   the use of `start` without an `end` parameter requires the use of `referenceBases`
  -   `start` and `end`:
      -   special use case for exactly determined structural changes
* `startMin`
  Minimum start coordinate
  -   `startMin` + `startMax` + `endMin` + `endMax`
      -   for querying imprecise positions (e.g. identifying all structural variants starting anywhere between `startMin` <-> `startMax`, and ending anywhere between `endMin` <-> `endMax`
      -   single or douple sided precise matches can be achieved by setting `startMin = startMax XOR endMin = endMax`
* `startMax`
  Maximum start coordinate. See `startMin`.
* `end`
  Precise end coordinate. See `start`.
* `endMin`
  Minimum end coordinate. See `startMin`.
* `endMax`
  Maximum end coordinate. See `startMin`.
* **`referenceBases`**
  Reference bases for this variant (starting from `start`). Accepted values: `[ACGT]*`.
  When querying for variants without specific base alterations (e.g. imprecise structural variants with separate `variantType` as well as `start_min` & `end_min`... parameters), the use of a single `N` value is required.
* `alternateBases`
  The bases that appear instead of the reference bases. Accepted values: `[ACGT]*` or `N`. 
  Symbolic ALT alleles (DEL, INS, DUP, INV, CNV, DUP:TANDEM, DEL:ME, INS:ME) will be represented in `variantType`. 
  Optional: either `alternateBases` or `variantType` is required.
* `variantType`
  The `variantType` is used to denote e.g. structural variants. Examples:
  -   DUP: duplication of sequence following `start`; not necessarily in situ
  -   DEL: deletion of sequence following `start`
  Optional: either `alternateBases` or `variantType` is required.
* `datasetIds` 
  Identifiers of datasets, as defined in `BeaconDataset`. If this field is null/not specified, all datasets should be queried. E.g. `?datasetIds=some-id&datasetIds=another-id`.
* `includeDatasetResponses`
  Indicator of whether responses for individual datasets (`datasetAlleleResponses`) should be included in the response (`BeaconAlleleResponse`) to this request or not. If null (not specified), the default value of `NONE` is assumed.
  Accepted values : `ALL`, `HIT`, `MISS`, `NONE`.

[localhost:9075/elixirbeacon/v04/beacon/query?referenceName=1&start=14929&referenceBases=A&alternateBases=G&assemblyId=GRCh37&includeDatasetResponses=NONE](http://localhost:9075/elixirbeacon/v04/beacon/query?referenceName=1&start=14929&referenceBases=A&alternateBases=G&assemblyId=GRCh37&includeDatasetResponses=NONE))
```json
{
  "beaconId" : "elixir-demo-beacon",
  "exists" : true,
  "error" : null,
  "alleleRequest" : {
    "alternateBases" : "G",
    "referenceBases" : "A",
    "referenceName" : "1",
    "start" : 14929,
    "startMin" : null,
    "startMax" : null,
    "end" : null,
    "endMin" : null,
    "endMax" : null,
    "variantType" : null,
    "assemblyId" : "GRCh37",
    "datasetIds" : null,
    "includeDatasetResponses" : "NONE"
  },
  "apiVersion" : "0.4",
  "datasetAlleleResponses" : null
}
```
Or you can ask for the information in a specific dataset:
[localhost:9075/elixirbeacon/v04/beacon/query?referenceName=X&start=147880925&end=146342284&referenceBases=N&variantType=DUP&assemblyId=GRCh7&datasetIds=EGAD00000000028&includeDatasetResponses=HIT](http://localhost:9075/elixirbeacon/v04/beacon/query?referenceName=X&start=147880925&end=146342284&referenceBases=N&variantType=DUP&assemblyId=GRCh7&datasetIds=EGAD00000000028&includeDatasetResponses=HIT)
```json
{
  "beaconId" : "elixir-demo-beacon",
  "exists" : true,
  "error" : null,
  "alleleRequest" : {
    "alternateBases" : null,
    "referenceBases" : "N",
    "referenceName" : "X",
    "start" : 147880925,
    "startMin" : null,
    "startMax" : null,
    "end" : 146342284,
    "endMin" : null,
    "endMax" : null,
    "variantType" : "DUP",
    "assemblyId" : "GRCh37",
    "datasetIds" : [ "EGAD00000000028" ],
    "includeDatasetResponses" : "HIT"
  },
  "apiVersion" : "0.4",
  "datasetAlleleResponses" : [ {
    "datasetId" : "EGAD00000000028",
    "exists" : true,
    "error" : null,
    "frequency" : 0.66,
    "variantCount" : 1,
    "callCount" : 2,
    "sampleCount" : 1,
    "note" : "OK",
    "externalUrl" : null,
    "info" : null
  } ]
}
```
This is an example for querying for a deletion with fuzzy match:
[localhost:9075/elixirbeacon/v04/beacon/query?referenceName=X&startMin=153592310&startMax=153592317&endMin=153517030&endMax=153517050&variantType=DEL&referenceBases=N&assemblyId=GRCh37&datasetIds=EGAD00000000028](http://localhost:9075/elixirbeacon/v04/beacon/query?referenceName=X&startMin=153592310&startMax=153592317&endMin=153517030&endMax=153517050&variantType=DEL&referenceBases=N&assemblyId=GRCh37&datasetIds=EGAD00000000028)

# Further information
## Project structure
The project has the following structure:
* `/src/main/java`
    * Java files (.java).
* `/src/main/resources`
    * configuration files: .properies, .yml
* `/src/test/java`
    * Java classes for testing.
* `/src/test/resources`
    * configuration files for testing: .properties, .yml
* `/target/generated-sources/java`
    * auto generated Java files.
* `/target/classes`
    * compiled files (.class).
* `/target`
    * among other things, contains the .jar file with the compiled classes, libraries, etc.

## Extend/Change functionality
There are two options:
1. Editing the source code.
  * If you want to add new functionalities (i. e. new endpoints).
2. Changing the implementation class
  * To change the way something is done (i. e. you want to modify the query, to check some requirements in the parameters, etc.).

### Option 2
This application uses [Spring framework](http://docs.spring.io/spring/docs/4.0.x/spring-framework-reference/htmlsingle/). Specifically we use [Spring boot v1.5.4](https://docs.spring.io/spring-boot/docs/1.5.4.RELEASE/reference/htmlsingle/).
You can write your own implementation of the interface `org.ega_archive.elixirbeacon.ElixirBeaconService`.
The following steps will allow you to build and use a custom implementation:
 * Create a new maven project:
   ```xml
   <?xml version="1.0" encoding="UTF-8"?>
   <project xmlns="http://maven.apache.org/POM/4.0.0"
            xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
            xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
       <modelVersion>4.0.0</modelVersion>
     
       <groupId>org.ega_archive</groupId>
       <artifactId>elixir-beacon-custom</artifactId>
       <version>0.0.1-SNAPSHOT</version>
       <packaging>jar</packaging>
     
       <name>elixir-beacon-custom</name>
       <description>elixir-beacon-custom</description>
     
       <dependencies>
           <dependency>
               <groupId>org.ega_archive</groupId>
               <artifactId>elixir-beacon</artifactId>
               <version>put version here, i.e: 0.4-SNAPSHOT</version>
           </dependency>
       </dependencies>
   </project>
   ```
* After this, create the package `org.ega_archive.custom.elixirbeacon` (by default, our app will scan `org.ega_archive.custom.elixirbeacon` package to try to find candidates for our services) If you want to use a different package name, you must cusomize your application properties name and add the property:
  ```INI
  custom.package.scan=org.my.custom
  ```
* Inside that folder create a services package and write your custom implementation 
  ```java
  package org.ega_archive.elixirbeacon.service;

  import org.ega_archive.elixirbeacon.dto.Beacon;
  import org.ega_archive.elixirbeacon.dto.BeaconAlleleResponse;
  import org.ega_archive.elixircore.helper.CommonQuery;
  import org.springframework.context.annotation.Primary;
  import org.springframework.stereotype.Component;
   
  import java.util.List;
   
  @Primary //This will make that this implementation will be used instead of the default one
  @Component
  public class CustomService implements ElixirBeaconService {
   
    public Beacon listDatasets(CommonQuery commonQuery, String referenceGenome) {
      //Write here your custom code
      return null;
    }
   
    public BeaconAlleleResponse queryBeacon(List<String> datasetStableIds, String alternateBases, String referenceBases, String chromosome, Integer start, String referenceGenome) {
      //Write here your custom code
      return null;
    }
   
    public List<String> checkParams(BeaconAlleleResponse result, List<String> datasetStableIds, String alternateBases, String referenceBases, String chromosome, Integer start, String referenceGenome) {
      //Write here your custom code
      return null;
    }
  }
  ```
* Compile your code:
  ```
  mvn clean compile jar:jar
  ```
  This will generate a `elixir-beacon-custom-version.jar`.

  If you get an error of the depency not found, it is because you don't have a repo with the dependency artifact. In this case, you can go to the elixir-beacon aritfact and execute:
  ```
  mvn install:install-file -Dfile=/path_to_project_folder/elixir-beacon-custom-version.jar -DgroupId=org.ega_archive -DartifactId=elixir-beacon-custom -Dversion=version -Dpackaging=jar -DgeneratePom=true
  ```
  This will install the artifact in your local repo. After that try to compile again your custom code.
* Execute the program with your code: 
    * First create an empty folder an copy there the original elixir jar (`elixir-beacon-0.4.jar`)
    * Then create a `/lib` folder and put the `elixir-beacon-custom-version.jar` jar in that folder
    * After that you can run the program executing:
   ```
      java -Dloader.path=lib/ -Dspring.profiles.active=dev -jar elixir-beacon-0.4.jar
  ```
# Docker (previous version v0.3)
Docker image available at: https://hub.docker.com/r/egacrg/beacon/

It includes the Elixir Beacon application, already deployed and running, and a PostgreSQL database with some sample data.

The JAR file is located at /tmp folder with the default configuration (for further information see section [Elixir Beacon, the main project](https://github.com/elixirhub/human-data-beacon#elixir-beacon-the-main-project)).

The database used is called elixir_beacon_dev and the default user and password are microaccounts_dev and r783qjkldDsiu. If you change anything of this configuration you must also change it in the **application-dev.properties** file inside the JAR file (see section [Configuration files](https://github.com/elixirhub/human-data-beacon#configuration-files)). You only need to edit this properties file (no recompilation needed) and redeploy the application (see section [Deploy the JAR](https://github.com/elixirhub/human-data-beacon#deploy-the-jar)).

To load your own data into the database, first remove sample data provided by default:
```
docker attach the_identifier_of_the_image
psql -h localhost -p 5432 -U microaccounts_dev -d elixir_beacon_dev
```
```sql
TRUNCATE beacon_dataset_table;
TRUNCATE beacon_data_table;
```
And load your own data (see section [Load data](https://github.com/elixirhub/human-data-beacon#load-the-data)).

To detach from the docker container press <code>Ctrl + p + q</code>.

# Docker UI (previous version v0.3)
There is a docker image with the Beacon user interface impementation available at: https://github.com/elixir-europe/human-data-beacon-ui
