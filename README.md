iPlug MDEK
==========

This software is part of the InGrid software package. The iPlug MDEK (Metadaten Erfassungs Komponente) or iPlug IGE (InGrid Editor) manages all database operations to an InGrid Catalog (IGC) database and provides all interfaces to edit and maintain an InGrid Catalog. It is connected via TCP to an InGrid Editor frontend instance which supplies the user interface for triggering the actions on the iPlug.


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

```
mvn eclipse:eclipse
```

and import projects into eclipse.
NOTICE: This is a multi module project resulting in multiple eclipse projects.

### Start and Debug IGE iPlug under eclipse

- set up a java application Run Configuration with main class `de.ingrid.mdek.MdekServer` in the project `ingrid-mdek-job`
- add the VM argument `--descriptor <BASE-DIRECTORY>\ingrid-mdek-job\src\main\resources\communication.xml` to the Run Configuration. This points to your IGE frontend.
- add `ingrid-mdek-api/src/main` to class path
- add `ingrid-mdek-job/src/main` to class path
- add `ingrid-mdek-services/src/main` to class path
- the connection to your IGC database maintained by this iPlug is set in `ingrid-mdek-services/src/main/resources/default-datasource.properties`
- to debug generated SQL from Hibernate activate output in `ingrid-mdek-services/src/main/resources/datasource-services.xml`: `<prop key="hibernate.show_sql">true</prop>`

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
