iPlug MDEK
==========

This software is part of the InGrid software package. The iPlug MDEK manages all database operations to an InGrid Catalog database and provides all interfaces to search, edit and maintain an InGrid Catalog. It is connected via TCP to an InGrid Editor instance.


Features
--------

- abstracts access to the InGrid Catalog database
- manages updates of database versions


Requirements
-------------

- a running InGrid Editor (InGrid Portal or standalone)

Installation
------------

Download from https://dev.informationgrid.eu/ingrid-distributions/ingrid-mdek/
 
or

build from source with `mvn package assembly:single`.

Execute

```
java -jar ingrid-mdek-x.x.x-installer.jar
```

and follow the install instructions.

Obtain further information at https://dev.informationgrid.eu/


Contribute
----------

- Issue Tracker: https://github.com/informationgrid/ingrid-mdek/issues
- Source Code: https://github.com/informationgrid/ingrid-mdek
 
### Set up eclipse project

```
mvn eclipse:eclipse
```

and import project into eclipse.

### Debug under eclipse

TBD.

Support
-------

If you are having issues, please let us know: info@informationgrid.eu

License
-------

The project is licensed under the EUPL license.
