
Migration InGrid Katalog (IGC) von MySQL/Oracle nach PostgreSQL
===============================================================

Die Migration wird mit dem Tool EDB Postgres Migration Toolkit per Kommandozeile ausgeführt.
Dieses Tool kann von einer normalen Postgres Installation via "StackBuilder" nachinstalliert werden und funktioniert auf Windows und Linux.
https://www.enterprisedb.com/products-services-training/products-overview/postgres-plus-solution-pack/migration-toolkit

Ein Manual findet sich unter:
https://www.enterprisedb.com/docs/en/9.5/migrate/toc.html

Im folgenden ist das Vorgehen auf Windows beschrieben, unter Unix ist dies adäquat auszuführen, s. Manual.


Vorgehen Migration IGC auf Windows nach PostgreSQL 9.5.5
--------------------------------------------------------

Migrationstool installieren:
----------------------------
- im PostgreSQL StartMenu "Application Stack Builder" starten, um zusätzliche Software für Postgres zu installieren
- nach Auswahl der vorhandenen Postgres Installation erscheinen die zusätzlichen Anwendungen, hier folgendes wählen:
  - "Registration Required and Trial Products" -> "EnterpriseDB Tools" -> "Migration Toolkit ..."
- zum Download muss ein Account angelegt werden
- der Installationsprozess via StackBuilder ist hier beschrieben:
  https://www.enterprisedb.com/docs/en/9.5/migrate/EDB_Postgres_Migration_Guide.1.12.html
- das Migrationstool ist in Java realisiert, für den Zugriff auf die Datenbanken müssen die entsprechenden Datenbanktreiber in Java bekannt sein.
  Dazu aus dem installierten IGE iPlug aus dem lib Verzeichnis die Dateien:
    - postgresql-*.jar
    - ojdbc-*.jar
    - mysql-connector-java-*.jar
  nach JAVA_HOME/jre/lib/ext kopieren.


Konfiguration Migrationstool:
-----------------------------
- im folgenden wird das Migration Toolkit Installationsverzeichnis als MTK_HOME bezeichnet
- Quell- und Zieldatenbank werden eingestellt unter:
    MTK_HOME/etc/toolkit.properties
  Die Zieldatenbank muss im Postgres Server bereits existieren (z.B. CREATE DATABASE igc_test).

  Eine MySQL Migration hätte beispielhaft folgenden Inhalt:
  
    SRC_DB_URL=jdbc:mysql://localhost:3306/igc_test
    SRC_DB_USER=root
    SRC_DB_PASSWORD=...

    TARGET_DB_URL=jdbc:postgresql://localhost:5432/igc_test
    TARGET_DB_USER=postgres
    TARGET_DB_PASSWORD=...
    
  Eine Oracle Migration hätte beispielhaft folgenden Inhalt:

    SRC_DB_URL=jdbc:oracle:thin:@192.168.0.237:1521:XE
    SRC_DB_USER=IGC_TEST
    SRC_DB_PASSWORD=...

    TARGET_DB_URL=jdbc:postgresql://localhost:5432/igc_test
    TARGET_DB_USER=postgres
    TARGET_DB_PASSWORD=...

- Weitere Hilfe s. https://www.enterprisedb.com/docs/en/9.5/migrate/EDB_Postgres_Migration_Guide.1.14.html


Ausführen Migration:
--------------------
- nach MTK_HOME/bin wechseln
- Ausführen der batch Datei runMTK.bat

  Beispielhaft für MySQL Migration:
  
    .\runMTK.bat -sourcedbtype mysql -targetdbtype postgresql -targetSchema public igc_test

  Beispielhaft für Oracle Migration:
  
    .\runMTK.bat -sourcedbtype oracle -targetdbtype postgresql -targetSchema public igc_test
    
- das Schema der Quelldatenbank wird in der Postgres Datenbank ins Schema public migriert, dies ist das default Schema und wird im IGE iPlug per default so erwartet.
- eine Ausgabe aller möglichen Migrations-Parameter ist wie folgt möglich:
    .\runMTK.bat -help
- Weitere Hilfe s. https://www.enterprisedb.com/docs/en/9.5/migrate/EDB_Postgres_Migration_Guide.1.23.html


Nacharbeiten für MySQL:
-----------------------
- die MySQL Migration migriert nicht die manuell angelegten Indexe
- diese sind für den Betrieb des IGE nicht zwingend nötig, können jedoch für Performance Gewinn sorgen
- zur Generierung der Indexe das SQL Skript
    mysql2postgres_fix_indexes_4.0.1.sql
  auf der Postgres Datenbank ausführen, dies ist der Indexzustand zum Zeitpunkt der IGC Version 4.0.1


Postgres Datenbankeinstellungen im IGE iPlug
--------------------------------------------
Die Einstellung der Datenbank erfolgt in der Datei:

- conf/config.override.properties

	iplug.database.driver=org.postgresql.Driver
	iplug.database.username=postgres
	iplug.database.password=...
	iplug.database.schema=
	iplug.database.url=jdbc\:postgresql\://localhost\:5432/igc_test
	iplug.database.dialect=org.hibernate.dialect.PostgreSQLDialect

Daraus werden beim Start des iPlugs die Einstellungen in die Datei conf/default-datasource.properties übernommen.
