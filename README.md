





# Table of contents    

* [Requirements](#requirements)  
* [Quick start](#quick-start)  
* [Configure databases](#configure-databases)  
  * [Create databases](#create-databases)  
  * [Load the data](#load-the-data)  
* [Managing the code](#managing-the-code)  
  * [Download the project](#download-the-project)  
  * [Elixir Core](#elixir-core)  
  * [Elixir Beacon, the main project](#elixir-beacon-the-main-project)  
    * [Configuration files](#configuration-files)  
   * [Compile and test the code](#compile-and-test-the-code)  
  * [Deploy the JAR](#deploy-the-jar)  
  * [Run integration tests](#run-integration-tests)  
* [Using the application](#using-the-application)  
  * [/beacon/](#beacon)  
  * [/beacon/query](#beaconquery)  
* [Further information](#further-information)  
  * [Project structure](#project-structure)  
  * [Extend/Change functionality](#extendchange-functionality)  
  
# Requirements  

* Java 8 JDK  
* Apache Maven 3  
* PostgreSQL Server 9.0+, or any other SQL server (i. e. MySQL)  
* JMeter  
  
# Quick start  

This quick start guide uses the default configuration and sets the application up using some sample data. It requires a Postgres server running in the local machine and listening to the default port 5432.  
  
If you want to tune the configuration or load custom data, please, skip this section and keep reading.  

1. Create 2 databases and a new user (use *r783qjkldDsiu* as password)  
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
2. Load the schema ([elixir_beacon_db_schema.sql](elixir_beacon/src/main/resources/META-INF/elixir_beacon_db_schema.sql))  
    ```  
    psql -h localhost -p 5432 -d elixir_beacon_dev -U microaccounts_dev < elixir_beacon_db_schema.sql  
    psql -h localhost -p 5432 -d elixir_beacon_testing -U microaccounts_dev < elixir_beacon_db_schema.sql  
    ```  
3. Load data 
    ```  
    psql -h localhost -p 5432 -d elixir_beacon_dev -U microaccounts_dev  
    ```  
    * Dataset:
        ```sql  
        INSERT INTO beacon_dataset_table(id, stable_id, description, access_type, reference_genome, variant_cnt, call_cnt, sample_cnt)  
          VALUES (1, '1000_genomes', 'Variants of chromosomes 21 and Y from the 1000 genomes project', 'PUBLIC', 'GRCh37', 47, 80, 1);  
        -- Init dataset-ConsentCodes table
        INSERT INTO beacon_dataset_consent_code_table (dataset_id, consent_code_id , additional_constraint, version) 
          VALUES(1, 1, null, 'v1.0'); -- NRES - No restrictions on data use
        ```  
   * Variants: [1_ALL.chrY.phase3_integrated_v2a.20130502.genotypes.variants.csv](elixir_beacon/src/main/resources/META-INF/1_ALL.chrY.phase3_integrated_v2a.20130502.genotypes.variants.csv)
        ```  
        cat 1_ALL.chrY.phase3_integrated_v2a.20130502.genotypes.variants.csv | psql -h localhost -p 5432 -U microaccounts_dev -c \
        "copy beacon_data_table (dataset_id,chromosome,start,variant_id,reference,alternate,\"end\","type",sv_length,variant_cnt,call_cnt,sample_cnt,frequency,matching_sample_cnt) from stdin using delimiters ';' csv header" elixir_beacon_dev
        ```  
   * Sample list: [1_ALL.chrY.phase3_integrated_v2a.20130502.genotypes.samples.csv](elixir_beacon/src/main/resources/META-INF/1_ALL.chrY.phase3_integrated_v2a.20130502.genotypes.samples.csv)  
        * Load sample list into the DB you need a temporary table, `tmp_sample_table`:
            ```  
            cat 1_ALL.chrY.phase3_integrated_v2a.20130502.genotypes.samples.csv | psql -h localhost -p 5432 -U microaccounts_dev -c \
            "copy tmp_sample_table (sample_stable_id,dataset_id) from stdin using delimiters ';' csv header" elixir_beacon_dev
            ```  
        * Run this query to fill the final table `beacon_sample_table`:
            ```sql
            INSERT INTO beacon_sample_table (stable_id)
            SELECT DISTINCT t.sample_stable_id
            FROM tmp_sample_table t
            LEFT JOIN beacon_sample_table sam ON sam.stable_id=t.sample_stable_id
            WHERE sam.id IS NULL;
            ```
        * Run this query to fill the final linking table `beacon_dataset_sample_table`:
            ```sql
            INSERT INTO beacon_dataset_sample_table (dataset_id, sample_id)
            select distinct dat.id AS dataset_id, sam.id AS sample_id
            from tmp_sample_table t
            inner join beacon_sample_table sam ON sam.stable_id=t.sample_stable_id
            inner join beacon_dataset_table dat ON dat.id=t.dataset_id
            LEFT JOIN beacon_dataset_sample_table dat_sam ON dat_sam.dataset_id=dat.id AND dat_sam.sample_id=sam.id
            WHERE dat_sam.id IS NULL;
            ```
   * Samples where each variant is found: [1_ALL.chrY.phase3_integrated_v2a.20130502.genotypes.variants.matching.samples.csv](elixir_beacon/src/main/resources/META-INF/1_ALL.chrY.phase3_integrated_v2a.20130502.genotypes.variants.matching.samples.csv)  
       * Load samples by variant into the DB you need a temporary table, `tmp_data_sample_table`:
            ```  
            cat 1_ALL.chrY.phase3_integrated_v2a.20130502.genotypes.variants.matching.samples.csv | psql -h localhost -p 5432 -U microaccounts_dev -c \
            "copy tmp_data_sample_table (dataset_id,chromosome,start,variant_id,reference,alternate,"type",sample_ids) from stdin using delimiters ';' csv header" elixir_beacon_dev
            ```  
        * Run this query to fill the final linking table `beacon_data_sample_table`:
            ```sql
            INSERT INTO beacon_data_sample_table (data_id, sample_id)
            select data_sam_unnested.data_id, s.id AS sample_id
            from (
                select dt.id as data_id, unnest(t.sample_ids) AS sample_stable_id
                from tmp_data_sample_table t
                inner join beacon_data_table dt ON dt.dataset_id=t.dataset_id and dt.chromosome=t.chromosome
                    and dt.variant_id=t.variant_id and dt.reference=t.reference and dt.alternate=t.alternate
                    and dt.start=t.start and dt.type=t.type 
            )data_sam_unnested
            inner join beacon_sample_table s on s.stable_id=data_sam_unnested.sample_stable_id
            left join beacon_data_sample_table ds ON ds.data_id=data_sam_unnested.data_id and ds.sample_id=s.id
            where ds.data_id is null;
            ```
    * Truncate temporary tables, `tmp_sample_table` and `tmp_data_sample_table`:
        ```sql
        TRUNCATE TABLE tmp_sample_table;
        TRUNCATE TABLE tmp_data_sample_table;
        ```
5. Create the function ([elixir_beacon_function_summary_response.sql](elixir_beacon/src/main/resources/META-INF/elixir_beacon_function_summary_response.sql))  
    ```  
    psql -h localhost -p 5432 -d elixir_beacon_dev -U microaccounts_dev < elixir_beacon_function_summary_response.sql  
    psql -h localhost -p 5432 -d elixir_beacon_testing -U microaccounts_dev < elixir_beacon_function_summary_response.sql  
    ```  
6. Download the code  
    ```  
    git clone https://github.com/ga4gh-beacon/beacon-elixir.git  
    ```  
7. Prepare dependencies  
    ```  
    cd beacon-elixir/elixir_core  
    mvn clean compile jar:jar  
    mvn install:install-file -Dfile=target/elixir-core-1.0.1-SNAPSHOT.jar -DgroupId=org.ega_archive -DartifactId=elixir-core -Dversion=1.0.1-SNAPSHOT -Dpackaging=jar -DgeneratePom=true
    ```  
8. Compile and deploy the application  
    ```  
    cd ../elixir_beacon  
    mvn clean compile package -Dmaven.test.skip=true
    java -jar target/elixir-beacon-1.0.1-SNAPSHOT.jar --spring.profiles.active=dev  
    ```  
9. Go to   
    * [localhost:9075/elixirbeacon/v1/beacon/](http://localhost:9075/elixirbeacon/v1/beacon/)  
    * [localhost:9075/elixirbeacon/v1/beacon/query?referenceName=1&start=981930&referenceBases=A&alternateBases=G&assemblyId=GRCh37&datasetIds=EGAD00000000028](http://localhost:9075/elixirbeacon/v1/beacon/query?referenceName=1&start=981930&referenceBases=A&alternateBases=G&assemblyId=GRCh37&datasetIds=EGAD00000000028)  
    * [localhost:9075/elixirbeacon/v1/beacon/query?referenceName=1&start=981930&referenceBases=A&alternateBases=G&assemblyId=GRCh37&datasetIds=EGAD00000000028&includeDatasetResponses=HIT](http://localhost:9075/elixirbeacon/v1/beacon/query?referenceName=1&start=981930&referenceBases=A&alternateBases=G&assemblyId=GRCh37&datasetIds=EGAD00000000028&includeDatasetResponses=HIT)  

# Configure databases  
## Create databases  
1. Connect to the server:  
    ```  
    psql -h localhost -p 5432 -U postgres  
    ```  
    Notice that: 
    * If you want to use a different database **name**, **user** or your Postgres server is running in a different **host** or is listening to a different **port**, please, replace the values in the previous command.    
    * You will need a user with enough permissions to create databases. 
    
    These are the most common options used with the `psql` command:  
    * `-d`: database name (depending on the command the database name will be specified with this option).  
    * `-h`: hostname or IP of the machine where the Postgres server is running.  
    * `-p`: port that the Postgres server is listening to.  
    * `-U`: user name that will be used to connect to the database.  
2. Create two databases. Default names are:  
    * `elixir_beacon_dev`: this is the main database which will be used by the application.  
    * `elixir_beacon_testing`: this is a secondary database which will be used to run the tests.  
    ```sql  
    CREATE DATABASE elixir_beacon_dev;  
    CREATE DATABASE elixir_beacon_testing;  
    ```  
3. Create a user that will be used by the application to connect to the databases we just created:  
    ```  
    createuser -P microaccounts_dev  
    ```  
    This command will prompt for the password of the new user. Remember this password as we will need it to configure the application.  
   
4. Log in each of the databases and grant privileges to the user just created:  
    ```  
    psql elixir_beacon_dev -U postgres  
    ```  
    ```sql  
    GRANT ALL PRIVILEGES ON DATABASE elixir_beacon_dev TO microaccounts_dev;  
    GRANT ALL PRIVILEGES ON DATABASE elixir_beacon_testing TO microaccounts_dev;  
    ```  
    NOTE: You can skip this step and load the schema using a super user in the next step and, after that, grant privileges to a different user (this user will be used by the application to connect to the database).  
  
5. Download the schema ([elixir_beacon_db_schema.sql](elixir_beacon/src/main/resources/META-INF/elixir_beacon_db_schema.sql)) and load it in **both** databases:   
    ```  
    psql -h localhost -p 5432 -d elixir_beacon_dev -U microaccounts_dev < elixir_beacon_db_schema.sql  
    psql -h localhost -p 5432 -d elixir_beacon_testing -U microaccounts_dev < elixir_beacon_db_schema.sql  
    ```  
    That script will create the tables and views and also load some essential data for data use conditions.  
    
        Here you can find a diagram of this schema:
   ![Database schema diagram](elixir_beacon/src/main/resources/META-INF/elixir_beacon_db_schema_diagram.png)    
    If you use a super user to create the schema, then you will need to grant access to the ordinary user that will be used by the application (e.g. microaccounts_dev):  
    ```  
    psql -h localhost -p 5432 -d elixir_beacon_dev -U postgres  
    ```  
    ```sql  
    GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO microaccounts_dev;  
    GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO microaccounts_dev;  
    ```  
    Remember to run these lines in **both** databases.  
    
6. Load the function ([elixir_beacon_function_summary_response.sql](elixir_beacon/src/main/resources/META-INF/elixir_beacon_function_summary_response.sql)):  
    ```  
    psql -h localhost -p 5432 -d elixir_beacon_dev -U microaccounts_dev < elixir_beacon_function.sql  
    psql -h localhost -p 5432 -d elixir_beacon_testing -U microaccounts_dev < elixir_beacon_function.sql  
    ```  

## Load the data  
1. Download the [script](elixir_beacon/src/main/resources/META-INF/vcf_parser.sh) to parse VCF files and give it executable rights:  
    ```  
    chmod +x vcf_parser.sh  
    ```  
2. Run this script executing:  
    ```  
    ./vcf_parser.sh dataset_id file.vcf  
    ```  
   This script will generate 3 output files: 
   * List of variants: `dataset_id_filename.variants.csv` 
   * List of samples: `dataset_id_filename.samples.csv`
   * Lisf of samples where each variant can be found: `dataset_id_filename.variants.matching.samples.csv`
3. Load the dataset information into `beacon_dataset_table`:  
    ```sql  
    INSERT INTO beacon_dataset_table(id, stable_id, description, access_type, reference_genome, variant_cnt, call_cnt, sample_cnt)  
      VALUES (1, '1000_genomes', 'Variants of chromosomes 21 and Y from the 1000 genomes project', 'PUBLIC', 'GRCh37', 47, 80, 1);  
    ```  
    Initialize the row setting `variant_cnt`, `call_cnt` and `sample_cnt` to 1. After loading the data, do some count and set the real values (see step 5).  
    Remember to replace the values in the previous command with the correct ones.   
  
4. Load the variants into `beacon_data_table`:  
    * Download [elixir_beacon/src/main/resources/META-INF/1_ALL.chrY.phase3_integrated_v2a.20130502.genotypes.variants.csv](1_ALL.chrY.phase3_integrated_v2a.20130502.genotypes.variants.csv)
    * Load data into `beacon_data_table`:
    ```  
    cat 1_ALL.chrY.phase3_integrated_v2a.20130502.genotypes.variants.csv | psql -h localhost -p 5432 -U microaccounts_dev -c \
    "copy beacon_data_table (dataset_id,chromosome,start,variant_id,reference,alternate,\"end\","type",sv_length,variant_cnt,call_cnt,sample_cnt,frequency,matching_sample_cnt) from stdin using delimiters ';' csv header" elixir_beacon_dev
    ```  
   NOTE: This command and the following ones should be executed **only** in the `elixir_beacon_dev` database. The testing database will be initialized with specific data when the tests are run.      
   
5. Load the samples into the database:
    * Download [1_ALL.chrY.phase3_integrated_v2a.20130502.genotypes.samples.csv](elixir_beacon/src/main/resources/META-INF/1_ALL.chrY.phase3_integrated_v2a.20130502.genotypes.samples.csv )
    * Load data into a temporary table, `tmp_sample_table`:
        ```
        cat 1_ALL.chrY.phase3_integrated_v2a.20130502.genotypes.samples.csv | psql -h localhost -p 5432 -U microaccounts_dev -c \
        "copy tmp_sample_table (sample_stable_id,dataset_id) from stdin using delimiters ';' csv header" elixir_beacon_dev
        ```
    * Run this query to fill the final table `beacon_sample_table`:
        ```sql
        INSERT INTO beacon_sample_table (stable_id)
        SELECT DISTINCT t.sample_stable_id
        FROM tmp_sample_table t
        LEFT JOIN beacon_sample_table sam ON sam.stable_id=t.sample_stable_id
        WHERE sam.id IS NULL;
        ```
    * Run this query to fill the final linking table `beacon_dataset_sample_table`:
        ```sql
        INSERT INTO beacon_dataset_sample_table (dataset_id, sample_id)
        select distinct dat.id AS dataset_id, sam.id AS sample_id
        from tmp_sample_table t
        inner join beacon_sample_table sam ON sam.stable_id=t.sample_stable_id
        inner join beacon_dataset_table dat ON dat.id=t.dataset_id
        LEFT JOIN beacon_dataset_sample_table dat_sam ON dat_sam.dataset_id=dat.id AND dat_sam.sample_id=sam.id
        WHERE dat_sam.id IS NULL;
        ```
    * You can now truncate the temporary table, `tmp_sample_table`:
        ```sql
        TRUNCATE TABLE tmp_sample_table;
        ```
6. Load the samples where each variant can be found into the database:
    * Download [1_ALL.chrY.phase3_integrated_v2a.20130502.genotypes.variants.matching.samples.csv](elixir_beacon/src/main/resources/META-INF/1_ALL.chrY.phase3_integrated_v2a.20130502.genotypes.variants.matching.samples.csv)
    * Load into a temporary table, `tmp_data_sample_table`:
        ```
        cat 1_ALL.chrY.phase3_integrated_v2a.20130502.genotypes.variants.matching.samples.csv | psql -h localhost -p 5432 -U microaccounts_dev -c \
        "copy tmp_data_sample_table (dataset_id,chromosome,start,variant_id,reference,alternate,"type",sample_ids) from stdin using delimiters ';' csv header" elixir_beacon_dev
        ```
    * Run this query to fill the final linking table `beacon_data_sample_table`:
        ```sql
        INSERT INTO beacon_data_sample_table (data_id, sample_id)
        select data_sam_unnested.data_id, s.id AS sample_id
        from (
            select dt.id as data_id, unnest(t.sample_ids) AS sample_stable_id
            from tmp_data_sample_table t
            inner join beacon_data_table dt ON dt.dataset_id=t.dataset_id and dt.chromosome=t.chromosome
                and dt.variant_id=t.variant_id and dt.reference=t.reference and dt.alternate=t.alternate
                and dt.start=t.start and dt.type=t.type 
        )data_sam_unnested
        inner join beacon_sample_table s on s.stable_id=data_sam_unnested.sample_stable_id
        left join beacon_data_sample_table ds ON ds.data_id=data_sam_unnested.data_id and ds.sample_id=s.id
        where ds.data_id is null;
        ```
      * You can now truncate the temporary table, `tmp_data_sample_table`:
        ```sql
        TRUNCATE TABLE tmp_data_sample_table;
        ```
7. Update counts in `beacon_dataset_table`:  
    * Get counts from database:  
        ```sql  
        SELECT dataset_id, COUNT(*) AS variant_count, SUM(call_cnt) AS call_count  
        FROM beacon_data_table  
        GROUP BY dataset_id;  
            
        SELECT dat.id, COUNT(dat_sam.sample_id) AS sample_count
        FROM beacon_dataset_table dat
        INNER JOIN beacon_dataset_sample_table dat_sam ON dat_sam.dataset_id=dat.id
        GROUP BY dat.id;
        ```  
    * Update the dataset information:  
        ```sql  
        UPDATE beacon_dataset_table 
        SET variant_cnt=47, call_cnt=80, sample_cnt=1 
        WHERE id=1;  
        ```

# Managing the code  
## Download the project  
Clone the projects **elixir_beacon** (current one) and **elixir_core** located at the [Elixir's repository](https://github.com/ga4gh-beacon/beacon-elixir).  
```  
git clone https://github.com/ga4gh-beacon/beacon-elixir.git  
```  
Switch to this release:
```
git checkout v1.0.1
```

## Elixir Core  
First of all, it is necessary to compile the code of the **elixir_core** project because it is a dependency of the main project, elixir_beacon.  
```  
cd elixir_core  
mvn clean compile jar:jar  
```  
This will generate the JAR file `elixir-core-1.0.1-SNAPSHOT.jar` inside the `/target` folder.  
Then run:  
```  
mvn install:install-file -Dfile=target/elixir-core-1.0.1-SNAPSHOT.jar -DgroupId=org.ega_archive -DartifactId=elixir-core -Dversion=1.0.1-SNAPSHOT -Dpackaging=jar -DgeneratePom=true  
```  
Now this dependency will be found when compiling the main project, elixir_beacon. 

## Elixir Beacon, the main project  
### Configuration files  
The key files are:  
* `/src/main/resources/application-{profile}.properties`    
* `/src/test/resources/application-{profile}.properties`    
  
(see [Deploy JAR](#deploy-the-jar) for more information about using profiles).  
  
By default, the application is deployed at port **9075** and the context is **/elixirbeacon/v1/**. You can change this by modifying the following lines of the `application-{profile}.properties` file:  
```INI  
server.port=9075  
server.servlet-path=/v1  
server.context-path=/elixirbeacon  
```  
As explained at the beginning, the application uses two PostgreSQL databases named `elixir_beacon_dev` and `elixir_beacon_testing`.  
```INI  
datasource.elixirbeacon.url=jdbc:postgresql://localhost:5432/elixir_beacon_dev  
datasource.elixirbeacon.username=microaccounts_dev  
datasource.elixirbeacon.password=PUT HERE YOUR PASSWORD  
datasource.elixirbeacon.driverClassName=org.postgresql.Driver  
spring.jpa.properties.hibernate.dialect = org.hibernate.dialect.PostgreSQLDialect  
```  
1. Specify the **type** of the database (postgresql), the **host** (default, localhost), **port** (default, 5432) and finally the database **name** (default, elixir_beacon_dev).  
    * If you use MySQL: `jdbc:mysql`  
2. Username that will be used to connect to the database (default, microaccounts_dev).  
3. Password of this username.  
4. Driver class name   
   * If you use MySQL: `com.mysql.jdbc.Driver`  
5. Set the Hibernate dialect.  
   * If you use MySQL: `org.hibernate.dialect.MySQLDialect`  
    
If you use a different database than Postgres, you must add the corresponding library to the **/lib** folder inside the JAR (you don't need to recompile) or add the dependency to the `pom.xml` so maven can download the library (this will require to compile, see next step).  

## Compile and test the code  
To compile the code run the following command within the project folder:  
```  
cd elixir_beacon  
mvn clean compile package -Dspring.profiles.active="dev"  
```  
That will also execute the tests. To skip them add `-Dmaven.test.skip=true` to the command.  
NOTE: Tests use a different properties file located in `/src/test/resources`.  
  
To only run the tests use:  
```  
mvn test -Dspring.profiles.active="dev"  
```  
NOTE: For running the tests you should use a different database than the main one (e.g. `elixir_beacon_testing`, see [Create databases](#create-databases)) because some testing data will be loaded and overwrite anything in this database.  
  
If compilation and test execution are successful, a JAR file will be generated in the folder `/target` with the name `elixir-beacon-1.0.1-SNAPSHOT.jar`.  

## Deploy the JAR  
To deploy the JAR run run the following command within the **elixir_beacon/target** folder:  
  ```  
java -jar target/elixir-beacon-1.0.1-SNAPSHOT.jar --spring.profiles.active=dev  
 ```  
It will generate a log file in `logs/application.log` located in the same folder where the JAR has been deployed (e.g. `elixir_beacon/logs` but you can move the JAR file wherever you want and deploy it there).  

This argument `--spring.profiles.active=dev` specifies the profile to be used. By default, there are 2 profiles: `dev` and `test`. Each profile will use its own set of properties files (e.g. `dev` profile uses `application-dev.properties` and `application-dev.yml`).  

Using the default configuration, the application will be available at: [localhost:9075/elixirbeacon/v1/](http://localhost:9075/elixirbeacon/v1/)  

## Run integration tests  
We use JMeter to run this kind of tests. We have an artifact called **elixir-beacon-service-tests**.   
To download jmeter and run some basic tests, run the following command:  
```  
cd elixir_beacon_tests  
mvn -P local clean verify  
 ```  

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
[localhost:9075/elixirbeacon/v1/beacon/](http://localhost:9075/elixirbeacon/v1/beacon/)  
```json  
{
  "id" : "elixir-demo-beacon",
  "name" : "Elixir Demo Beacon",
  "apiVersion" : "1.0",
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
  "version" : "v1",
  "welcomeUrl" : "https://ega-archive.org/elixir_demo_beacon/",
  "alternativeUrl" : "https://ega-archive.org/elixir_demo_beacon_web/",
  "createDateTime" : "2015-06-01T00:00.000Z",
  "updateDateTime" : "2018-11-14T00:00.000Z",
  "datasets" : [ {
    "id" : "EGAD00000000028",
    "name" : null,
    "description" : "Sample variants",
    "assemblyId" : "grch37",
    "createDateTime" : null,
    "updateDateTime" : null,
    "dataUseConditions" : {
      "consentCodedataUse" : {
        "primaryCategory" : {
          "code" : "NRES",
          "description" : "No restrictions on data use.",
          "additionalConstraint" : null
        },
        "secondaryCategories" : [ {
          "code" : "RS-[XX]",
          "description" : "Use of the data is limited to studies of [research type] (e.g., pediatric research).",
          "additionalConstraint" : "pediatric research"
        } ],
        "requirements" : [ ],
        "version" : "v1.0"
      }
    },
    "version" : null,
    "variantCount" : 47,
    "callCount" : 80,
    "sampleCount" : 1,
    "externalUrl" : null,
    "info" : [ {
      "key" : "accessType",
      "value" : "PUBLIC"
    }, {
      "key" : "authorized",
      "value" : "true"
    } ]
  } ],
  "sampleAlleleRequests" : [ {
    "referenceName" : "1",
    "start" : 981930,
    "startMin" : null,
    "startMax" : null,
    "end" : null,
    "endMin" : null,
    "endMax" : null,
    "referenceBases" : "A",
    "alternateBases" : "G",
    "variantType" : null,
    "assemblyId" : "GRCh37",
    "datasetIds" : null,
    "includeDatasetResponses" : null
  }, {
    "referenceName" : "X",
    "start" : null,
    "startMin" : 120093168,
    "startMax" : 120093170,
    "end" : null,
    "endMin" : 120095230,
    "endMax" : 120095235,
    "referenceBases" : "A",
    "alternateBases" : null,
    "variantType" : null,
    "assemblyId" : "GRCh37",
    "datasetIds" : [ "EGAD00000000028" ],
    "includeDatasetResponses" : null
  }, {
    "referenceName" : "1",
    "start" : 14748940,
    "startMin" : null,
    "startMax" : null,
    "end" : 1475324,
    "endMin" : null,
    "endMax" : null,
    "referenceBases" : "A",
    "alternateBases" : null,
    "variantType" : null,
    "assemblyId" : "GRCh37",
    "datasetIds" : [ "EGAD00000000028" ],
    "includeDatasetResponses" : null
  } ],
  "info" : [ {
    "key" : "size",
    "value" : "47"
  } ]
} 
```  
The 3 examples that appear in field ` sampleAlleleRequests` can be customized by modifying the following properties in `/src/main/resources/application-{profile}.yml`:  
```yml  
#sample #1
querySamples:
  assemblyId1: GRCh37
  start1: 981930
  startMin1:
  startMax1:
  end1:
  endMin1:
  endMax1:
  referenceName1: 1
  referenceBases1: A
  alternateBases1: G
  variantType1:
  datasetIds1:
  #sample #2
  assemblyId2: GRCh37
  start2:
  startMin2: 120093168
  startMax2: 120093170
  end2:
  endMin2: 120095230
  endMax2: 120095235
  referenceName2: X
  referenceBases2: A
  alternateBases2:
  variantType2: DUP
  datasetIds2: EGAD00000000028
  #sample #3
  assemblyId3: GRCh37
  start3: 14748940
  startMin3:
  startMax3:
  end3: 1475324
  endMin3:
  endMax3:
  referenceName3: 1
  referenceBases3: A
  alternateBases3:
  variantType3: DEL
  datasetIds3: EGAD00000000028
```  
  
## /beacon/query  
To actually ask the beacon for questions like "do you have any genomes with an 'A' at position 100,735 on chromosome 3?" And the answer will be yes or no with some extra information (e.g. `variantCount`, `callCount` and `sampleCount`).  

Parameters (required in bold):  
* **`assemblyId`**: Assembly identifier (GRC notation, e.g. GRCh37).  
* **`referenceName`**: Reference name (chromosome). Accepting values 1-22, X, Y, MT.  
* `start`: Precise start coordinate position, allele locus (0-based, inclusive).  
  * `start` only:  
     * for single positions, e.g. the start of a specified sequence alteration where the size is given through the specified `alternateBases`  
    *  typical use are queries for SNV and small InDels  
     * the use of `start` without an `end` parameter requires the use of `referenceBases`  
  * `start` and `end`:  
    * special use case for exactly determined structural changes  
* `startMin`: Minimum start coordinate 
    * for querying imprecise positions (e.g. identifying all structural variants starting anywhere between `startMin`  <->  `startMax`, and ending anywhere between `endMin`  <->  `endMax`  
    * single or douple sided precise matches can be achieved by setting `startMin = startMax XOR endMin = endMax`  
* `startMax`: Maximum start coordinate. See `startMin`.  
* `end`: Precise end coordinate (0-based, exclusive). See `start`.  
* `endMin`: Minimum end coordinate. See `startMin`.  
* `endMax`: Maximum end coordinate. See `startMin`.  
* **`referenceBases`**: Reference bases for this variant (starting from `start`).
    Accepted values: `[ACGT]*`.  
 When querying for variants without specific base alterations (e.g. imprecise structural variants with separate `variantType` as well as `start_min` & `end_min`... parameters), the use of a single `N` value is required.  
* `alternateBases`: The bases that appear instead of the reference bases. 
    Accepted values: `[ACGT]*` or `N`.  
    Symbolic ALT alleles (DEL, INS, DUP, INV, CNV, DUP:TANDEM, DEL:ME, INS:ME) will be represented in `variantType`.  
    Optional: either `alternateBases` or `variantType` is required.  
* `variantType`: The `variantType` is used to denote e.g. structural variants. 
    Optional: either `alternateBases` or `variantType` is required.  
    Examples:  
    * DUP: duplication of sequence following `start`; not necessarily in situ  
    * DEL: deletion of sequence following `start`  
* `datasetIds`: Identifiers of datasets, as defined in `BeaconDataset`. If this field is null/not specified, all datasets should be queried. E.g. `?datasetIds=some-id&datasetIds=another-id`.  
* `includeDatasetResponses`: Indicator of whether responses for individual datasets (`datasetAlleleResponses`) should be included in the response (`BeaconAlleleResponse`) to this request or not. If null (not specified), the default value of `NONE` is assumed.
    Accepted values : `ALL`, `HIT`, `MISS`, `NONE`.  
    
[http://localhost:9075/elixirbeacon/v1/beacon/query?referenceName=1&start=981930&referenceBases=A&alternateBases=G&assemblyId=GRCh37&includeDatasetResponses=NONE](http://localhost:9075/elixirbeacon/v1/beacon/query?referenceName=1&start=981930&referenceBases=A&alternateBases=G&assemblyId=GRCh37&includeDatasetResponses=NONE)  
```json  
{
  "beaconId" : "elixir-demo-beacon",
  "exists" : true,
  "error" : null,
  "alleleRequest" : {
    "referenceName" : "1",
    "start" : 981930,
    "startMin" : null,
    "startMax" : null,
    "end" : null,
    "endMin" : null,
    "endMax" : null,
    "referenceBases" : "A",
    "alternateBases" : "G",
    "variantType" : null,
    "assemblyId" : "GRCh37",
    "datasetIds" : null,
    "includeDatasetResponses" : "NONE"
  },
  "apiVersion" : "1.0",
  "datasetAlleleResponses" : null
}
```  
Or you can ask for the information in a specific dataset. Example of querying a duplication with fuzzy match:
[http://localhost:9075/elixirbeacon/v1/beacon/query?referenceName=X&startMin=120093168&startMax=120093170&endMin=120095230&endMax=120095235&referenceBases=A&variantType=DUP&assemblyId=GRCh37&datasetIds=EGAD00000000028&&includeDatasetResponses=ALL](http://localhost:9075/elixirbeacon/v1/beacon/query?referenceName=X&startMin=120093168&startMax=120093170&endMin=120095230&endMax=120095235&referenceBases=A&variantType=DUP&assemblyId=GRCh37&datasetIds=EGAD00000000028&&includeDatasetResponses=ALL)  
```json  
{
  "beaconId" : "elixir-demo-beacon",
  "exists" : true,
  "error" : null,
  "alleleRequest" : {
    "referenceName" : "X",
    "start" : null,
    "startMin" : 120093168,
    "startMax" : 120093170,
    "end" : null,
    "endMin" : 120095230,
    "endMax" : 120095235,
    "referenceBases" : "A",
    "alternateBases" : null,
    "variantType" : "DUP",
    "assemblyId" : "GRCh37",
    "datasetIds" : [ "EGAD00000000028" ],
    "includeDatasetResponses" : "ALL"
  },
  "apiVersion" : "1.0",
  "datasetAlleleResponses" : [ {
    "datasetId" : "EGAD00000000028",
    "exists" : true,
    "error" : null,
    "frequency" : 0.5,
    "variantCount" : 1,
    "callCount" : 1,
    "sampleCount" : 1,
    "note" : "OK",
    "externalUrl" : null,
    "info" : null
  } ]
}
```  
This is an example of querying a deletion with exact match:  
[http://localhost:9075/elixirbeacon/v1/beacon/query?referenceName=1&start=14748940&end=1475324&referenceBases=A&variantType=DEL&assemblyId=GRCh37&datasetIds=EGAD00000000028&includeDatasetResponses=HIT](http://localhost:9075/elixirbeacon/v1/beacon/query?referenceName=1&start=14748940&end=1475324&referenceBases=A&variantType=DEL&assemblyId=GRCh37&datasetIds=EGAD00000000028&includeDatasetResponses=HIT)  
```json  
{
  "beaconId" : "elixir-demo-beacon",
  "exists" : true,
  "error" : null,
  "alleleRequest" : {
    "referenceName" : "1",
    "start" : 14748940,
    "startMin" : null,
    "startMax" : null,
    "end" : 1475324,
    "endMin" : null,
    "endMax" : null,
    "referenceBases" : "A",
    "alternateBases" : null,
    "variantType" : "DEL",
    "assemblyId" : "GRCh37",
    "datasetIds" : [ "EGAD00000000028" ],
    "includeDatasetResponses" : "HIT"
  },
  "apiVersion" : "1.0",
  "datasetAlleleResponses" : [ {
    "datasetId" : "EGAD00000000028",
    "exists" : true,
    "error" : null,
    "frequency" : 0.5,
    "variantCount" : 1,
    "callCount" : 1,
    "sampleCount" : 1,
    "note" : "OK",
    "externalUrl" : null,
    "info" : null
  }]
}
```  

# Further information  
## Project structure  
The project has the following structure:  
* `/src/main/java`: Java files (.java).  
* `/src/main/resources`: configuration files: .properies, .yml  
* `/src/test/java`: Java classes for testing.  
* `/src/test/resources`: configuration files for testing: .properties, .yml  
* `/target/generated-sources/java`: auto generated Java files.  
* `/target/classes`: compiled files (.class).  
* `/target`: among other things, contains the .jar file with the compiled classes, libraries, etc.  
  
## Extend/Change functionality  
There are two options:  
1. Editing the source code.  
    * If you want to add new functionalities (i. e. new endpoints).  
2. Changing the implementation class  
    * To change the way something is done (i. e. you want to modify the query, to check some requirements in the parameters, etc.).  

### Option 2  
This application uses [Spring framework](http://docs.spring.io/spring/docs/4.0.x/spring-framework-reference/htmlsingle/). Specifically we use [Spring boot v1.5.4](https://docs.spring.io/spring-boot/docs/1.5.4.RELEASE/reference/htmlsingle/).  
You can write your own implementation of the interface `ElixirBeaconService`.  The following steps will allow you to build and use a custom implementation:  
 1. Create a new maven project:  
    ```xml  
    <?xml version="1.0" encoding="UTF-8"?>  
    <project xmlns="http://maven.apache.org/POM/4.0.0"  
            xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"  
            xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">  
       <modelVersion>4.0.0</modelVersion>  
       
       <groupId>org.ega_archive</groupId>  
       <artifactId>elixir-beacon-custom</artifactId>  
       <version>1.1-SNAPSHOT</version>  
       <packaging>jar</packaging>  
       
       <name>elixir-beacon-custom</name>  
       <description>elixir-beacon-custom</description>  
       
       <dependencies>  
           <dependency>  
               <groupId>org.ega_archive</groupId>  
               <artifactId>elixir-beacon</artifactId>  
               <version>put version here, i.e: 1.0.1-SNAPSHOT</version>  
           </dependency>  
       </dependencies>  
    </project>  
    ```  
2. After this, create the package `org.ega_archive.custom.elixirbeacon` (by default, our app will scan this package to try to find candidates for our services) If you want to use a different package name, you must cusomize your application properties name and add the property:  
    ```INI  
    custom.package.scan=org.my.custom  
    ```  
3. Inside that folder create a services package and write your custom implementation   
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
     
      @Override
      public Beacon listDatasets(CommonQuery commonQuery, String referenceGenome) throws NotFoundException 
          //TODO: Write here your custom code  
          return null;  
      }  
        
      @Override
      public BeaconAlleleResponse queryBeacon(List<String> datasetStableIds, String variantType, 
         String alternateBases, String referenceBases, String chromosome, Integer start, Integer startMin, 
         Integer startMax, Integer end, Integer endMin, Integer endMax, String referenceGenome, 
         String includeDatasetResponses) {  
          //TODO: Write here your custom code  
          return null;  
        }  
         
      @Override
      public List<Integer> checkParams(BeaconAlleleResponse result, List<String> datasetStableIds,
      VariantType type, String alternateBases, String referenceBases, String chromosome,
      Integer start, Integer startMin, Integer startMax, Integer end, Integer endMin, Integer endMax, 
      String referenceGenome) {  
          //TODO: Write here your custom code  
          return null;  
        }  
      
      @Override
      public BeaconAlleleResponse queryBeacon(BeaconRequest request) {
          //TODO: Write here your custom code  
          return null;
      }
      
    }  
    ```  
4. Compile your code:  
    ```  
    mvn clean compile jar:jar  
    ```  
    This will generate a new JAR: `elixir-beacon-custom-version.jar`.  
  
    If you get an error of the depency not found, it is because you don't have a repo with the dependency artifact. In this case, you can go to the elixir-beacon aritfact and execute:  
    ```  
    mvn install:install-file -Dfile=/path_to_project_folder/elixir-beacon-custom-version.jar -DgroupId=org.ega_archive -DartifactId=elixir-beacon-custom -Dversion=version -Dpackaging=jar -DgeneratePom=true  
    ```  
    This will install the artifact in your local repo. After that try to compile again your custom code.  

5. Execute the program with your code:   
    * First create an empty folder an copy there the original elixir jar (`elixir-beacon-1.0.1-SNAPSHOT.jar`)  
    * Then create a `/lib` folder and put the `elixir-beacon-custom-version.jar` file in that folder  
    * After that you can deploy the app running:  
        ```  
    java -Dloader.path=lib/ -Dspring.profiles.active=dev -jar elixir-beacon-1.0.1-SNAPSHOT.jar 
        ```  
