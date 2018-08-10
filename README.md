iPlug IGE
=========

This software is part of the InGrid software package. The iPlug IGE (InGrid Editor) formerly also known as MDEK (Metadaten Erfassungs Komponente) manages all database operations to an InGrid Catalog database (IGC) and provides all interfaces to edit and maintain an InGrid Catalog. It is connected via TCP to an InGrid Editor frontend instance which supplies the user interface for triggering the actions on the iPlug.


Features
--------

- abstracts access to the InGrid Catalog database
- manages updates of database versions


Requirements
-------------

- a running InGrid Editor frontend (InGrid Portal or standalone)

Installation
------------

Download from https://dev.informationgrid.eu/ingrid-distributions/ingrid-iplug-ige/
 
or

build from source with `mvn package`.

Execute

```
java -jar ingrid-iplug-ige-x.x.x-installer.jar
```

and follow the install instructions.

Obtain further information at http://www.ingrid-oss.eu/ (sorry only in German)


Contribute
----------

- Issue Tracker: https://github.com/informationgrid/ingrid-mdek/issues
- Source Code: https://github.com/informationgrid/ingrid-mdek
 
### Set up eclipse project

Just import as Maven Projects.

### Start and Debug IGE iPlug under eclipse

- the connection to your IGC database maintained by this iPlug is set in `ingrid-mdek-job/src/test/resources/config.override.properties`
- set up a java application Run Configuration with main class `de.ingrid.mdek.MdekServer` in the project `ingrid-mdek-job`
- go to tab "Dependencies" and add with "Advanced"->"Add Folder" the directories
- `ingrid-mdek-job/src/test/resources` to class path under "User Entries"
- `ingrid-mdek-job/src/main/resources` to class path under "User Entries"
- make sure that src/test/resources is on top!
- if you are going to use PostgreSQL, then make sure that `hibernate.default_schema` in `src/test/resources/default-datasource.properties` isn't empty. If you haven't defined one on your own, the default schema in PostgreSQL is generally `public`.
- call 'mvn compile' inside 'ingrid-mdek-job'-project to extract admin gui
- optionally: to debug generated SQL from Hibernate activate output in `ingrid-mdek-job/src/main/webapp/WEB-INF/springapp-servlet.xml`: `<prop key="hibernate.show_sql">true</prop>`

### Start/Debug iPlug for another profile

The profiles can be found under "distribution/src/profiles" where all changes should be made specific for the
profile.

To develop for a profile you need to make the following changes:
* edit "ingrid-mdek-job/src/main/webapp/WEB-INF/jetty-web.xml" and uncomment the `<Item>` you need
for your profile (this extends and overwrites the webapp folder)
* add `distribution/src/profiles/<profile>/conf` to your classpath on top
    * IntelliJ IDEA: 
        * Click on ingrid-mdek-job (module) and press F4 (Open Module Settings)
        * add a new dependency ("JARs or directory")
        * choose "Classes" as category
        * move dependency to top

### Start various test examples simulating a frontend server calling various interfaces of IGE iPlug

- set up a java application Run Configuration with main class `de.ingrid.mdek.example.MdekExample*` (choose your example) in the project `ingrid-mdek-api`
- add the VM argument `--descriptor <BASE-DIRECTORY>\ingrid-mdek-api\src\main\resources\communication.xml --threads 1` to the Run Configuration. This configures your IGE frontend server where the IGE iPlug connects to. Also the number of threads the test example is run with (to test parallel calls to the IGE iPlug).
- add `ingrid-mdek-api/src/main` to class path

When starting the example (e.g. de.ingrid.mdek.example.MdekExampleQuery) it takes some time till frontend (example) and backend (IGE iPlug) connect.
Set your breakpoints in the front- and backend to debug different functionality.

Support
-------

If you are having issues, please let us know: info@informationgrid.eu

License
-------

The project is licensed under the EUPL license.
