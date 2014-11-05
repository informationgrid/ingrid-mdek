
Migration 3.2.0 von MySQL nach Oracle
=====================================

Basis-Migration via SQL Developer
---------------------------------
Download SQL Developer z.B. hier:
http://www.oracle.com/technetwork/developer-tools/sql-developer/downloads/index.html?ssSourceSiteId=ocomen

Migration Dokumentation s. 
http://docs.oracle.com/cd/E18464_01/doc.30/e17472.pdf
s. Chapter 2

Basis vor Migration:
- (Initiale Verbindung angelegt mit User "System")
- Neuen Benutzer MIGRATIONS/MIGRATIONS angelegt mit vollen Rechten
- Neue Verbindung angelegt mit Benutzer MIGRATIONS und als Migrations Repository gesetzt (Rechtsklick auf Verbindung -> "Migrations-Repository verknüpfen")
- Verbindung zu MySQL Datenbank angelegt (Treiber hinzuladen unter Einstellungen/Datenbank/JDBC-Treiber eines anderen Herstellers, z.B.: mysql-connector-java-5.1.21)

Migration:
Die Migration via SQL Developer legt immer einen neuen Benutzer (Schema) mit dem Namen der migrierten Datenbank an !
In dieses Schema werden die Daten migriert.
ACHTUNG:
Der Name der MySQL Datenbank sollte KEIN "-" enthalten, da dies zu Problemen führt.
Ist dies der Fall, dann z.B. "_" statt "-" benutzen, also "ingrid-portal" in MySQL umkopieren nach "ingrid_portal" und diese migrieren.

Vorgehen:
- Rechtsklick auf Datenbank in MySql Verbindung -> "Zu Oracle migrieren ..."
- In Wizard:
    - Projekt:
        - im Ausgabeverzeichnis werden nützliche Skripte abgelegt (z.B. Oracle Schema, Skripte zum unload der Daten aus MySQL und Einspielen via sqlldr ...)
	- Zieldatenbank:
        - Als Verbindung Repository MIGRATIONS wählen, dies ist der Benutzer unter dem die Migration stattfindet (braucht Rechte zum Anlegen des neuen Schemas/Benutzers)
        - "Zielobjekte löschen" aktivieren
	- Daten verschieben:
        - zunächst Daten "Offline" migrieren
        - "Daten leeren" aktivieren
- Nach Migration NEUE VERBINDUNG anlegen zu migriertem Schema: username / password ist der Datenbankname -> ACHTUNG: User ist case insensitive / passwd ist lowercase !
	- bei Migration Datenbank ingrid_portal ist USER / PASSWD z.B. ingrid_portal / ingrid_portal
- Dann via Migrationsprojekt Daten zu neuem Schema migrieren
	- Rechtsklick auf "Konvertierte Datenbankobjekte" (in Ansicht "Migrationsprojekte") und "Daten verschieben..."
	- Modus: Online, Quelle: "MySQL", Ziel: Verbindung zu neuem Schema, "Daten leeren"


Nach der Migration via SQL Developer folgende SQL Skripte im Schema der migrierten Datenbank ausführen:
-------------------------------------------------------------------------------------------------------

- Skripts ausführen:
	- IGC:		migration_igc3.2.0_fixes.sql
	- Portal:	migration_portal3.2.0_fixes.sql
	- mdek:		(kein Fix nötig)

ACHTUNG:
Im Portal werden alle Quartz Jobs gelöscht, da diese aus den persistenten Daten auf Oracle nicht erzeugt werden können (Fehler: EOF beim Lesen BLOB).
Die Default Jobs werden dann beim Starten des Portals wieder erzeugt.
Weitere Jobs zur Überwachung angeschlossener iPlugs etc. müssen im Portal neu angelegt werden (entsprechend "altem" Portal unter MySQL).


Danach in Komponenten Verbindung umstellen zu Oracle
----------------------------------------------------
InGrid Katalog (igc):
    - IGE iPlug in Datei conf\default-datasource.properties, z.B.:
        hibernate.driverClass=oracle.jdbc.OracleDriver
        hibernate.user=igc_test_lgv
        hibernate.password=igc_test_lgv
        hibernate.dialect=org.hibernate.dialect.Oracle10gDialect
        hibernate.jdbcUrl=jdbc:oracle:thin:@localhost:1521:XE
    - DSC iPlugs via Admin GUI oder in conf\plugdescription.xml
Portal Datenbank (ingrid_portal):
    - iplug-management in conf\repository_database.xml
    - Portal, s. apache-tomcat-...\conf\Catalina\localhost\README_oracle.txt
Mdek Datenbank (mdek):
    - Portal, s. apache-tomcat-...\conf\Catalina\localhost\README_oracle.txt

	