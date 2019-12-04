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

Download from https://distributions.informationgrid.eu/ingrid-iplug-ige/
 
or

build from source with `mvn clean package`.

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
 
### Setup Eclipse project

* import project as Maven-Project
* right click on project and select Maven -> Select Maven Profiles ... (Ctrl+Alt+P)
* choose profile "development"
* run "mvn compile" from Commandline (unpacks base-webapp) 
* run de.ingrid.mdek.MdekServer as Java Application
* in browser call "http://localhost:10017" with login "admin/admin"

### Setup IntelliJ IDEA project

* choose action "Add Maven Projects" and select pom.xml
* in Maven panel expand "Profiles" (global entry on top of the project tree) and make sure "development" is checked
* run compile task from Maven panel or run "mvn compile -Pdevelopment" from Commandline (unpacks base-webapp)
* run de.ingrid.mdek.MdekServer
** A new run configuration "MdekServer" should appear
** Make sure in "Edit Configurations" (Run configuration) that the working directory is set to the module directory
** Restart the MdekServer run configuration after changes to the configuration
* in browser call "http://localhost:10017" with login "admin/admin"

### Run with elastic search component

To test the index capabilities of the iPlug, a elastic search node is required. Please see the docker-compose.yml 
in the iBus project to get a fully configured elastic search node running.     

### Run with a profile

In order to run the configuration for a specific profile to create the correct index documents, you need to do the following steps:

* uncomment profile directory from `baseResource` in `ingrid-mdek-job\src\main\webapp\WEB-INF\jetty-web.xml`
* add `distribution/src/profiles/<profile>/conf` as `Resources Root` (IntelliJ) or as Classpath (Eclipse)

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
