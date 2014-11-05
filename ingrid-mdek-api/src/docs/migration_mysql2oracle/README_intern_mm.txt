
Migration PortalU MV Katalog nach Oracle
========================================
Issue: https://redmine.wemove.com/issues/376

Vorgehen:

[0. MySQL dumpen und lokal einspielen
-------------------------------------
- MV Katalog auf derulo exportieren:
    mysqldump -u root -p --add-drop-table igc_mv > igc_mv_20140707.sql
- Portal/mdek Datenbank auf blunt exportieren:
    mysqldump -u root -p --add-drop-table mdek > mdek_20140707.sql
    mysqldump -u root -p --add-drop-table ingrid-portal > ingrid-portal_20140707.sql
- lokal Datenbank erzeugen:
    mysql -u root -p
    CREATE DATABASE igc_mv DEFAULT CHARACTER SET utf8 COLLATE utf8_unicode_ci;
    CREATE DATABASE mdek_mv DEFAULT CHARACTER SET utf8 COLLATE utf8_unicode_ci;
    CREATE DATABASE ingrid_portal_mv DEFAULT CHARACTER SET utf8 COLLATE utf8_unicode_ci;
    quit
- lokal einspielen (windows):
    mysql -u root -p igc_mv < igc_mv_20140707.sql > mysql_import_igc_mv_out.txt
    mysql -u root -p mdek_mv < mdek_20140707.sql > mysql_import_mdek_out.txt
    mysql -u root -p ingrid_portal_mv < ingrid-portal_20140707.sql > mysql_import_portal_out.txt
]

1. SQL Developer aufsetzen:
---------------------------
auf BIG BOB installiert ! NICHT Redmond !
 
- Installation SQL Developer:
    - Download: sqldeveloper-3.2.20.09.87.zip (jdk included)
      ACHTUNG: SQL Developer 4 führt zu Problemen mit Oracle 10 !
    - Doku:
        http://docs.oracle.com/cd/E35137_01/index.htm
        http://docs.oracle.com/cd/E35137_01/appdev.32/e35117.pdf (s. Chaper 2)
- Verbindung zu redmond (192.168.0.238):
    - Initiale Verbindung angelegt mit User "system"
    - Oracle Version = 10.2.0.1.0:
        select * from product_component_version;
- Verbindung zu MySQL on localhost:
    MySQL Server Version: 5.5.8
        - Treiber hinzuladen unter Voreinstellungen/Datenbank/JDBC-Treiber eines anderen Herstellers, z.B.: mysql-connector-java-5.1.21-bin.jar
        - Neue Verbindung anlegen:
            - localhost:3306, Null-Datumsverarbeitung: Auf NULL setzen, Datenbank wählen: leer lassen
[        - Wenn von Oracle Server Verbindung zu externem MySQL Server, dann eventuell Remote Access auf MySql frei schalten:
            http://www.cyberciti.biz/tips/how-do-i-enable-remote-access-to-mysql-database-server.html
                bzw.
            http://www.howtogeek.com/howto/programming/mysql-give-root-user-logon-permission-from-any-host/
            NICHT VERGESSEN:
                flush privileges;
]


2. Migration via SQL Developer:
-------------------------------
s. Doku: 2.1.1 Migrating Using the Migration Wizard:
- Neuen Benutzer MIGRATIONS/MIGRATIONS angelegt mit vollen Rechten
- Neue Verbindung angelegt "redmond migration_repo" mit Benutzer MIGRATIONS
- Neue Verbindung "migration_repo" als Migrations Repository setzen (Rechtsklick auf Verbindung -> "Migrations-Repository verknüpfen")
- Migration igc_mv:
    - Die Migration via SQL Developer legt immer einen neuen Benutzer (Schema) mit dem Namen der migrierten Datenbank an ! In dieses Schema werden die Daten migriert.
      ACHTUNG: Der Name der MySQL Datenbank sollte KEIN "-" enthalten, da dies zu Problemen führt. Ist dies der Fall, dann z.B. "_" statt "-" benutzen, also "ingrid-portal" in MySQL umkopieren nach "ingrid_portal" und diese migrieren.
    - Vorgehen:
        - Rechtsklick auf Datenbank in MySql Verbindung -> "Zu Oracle migrieren ..."
        - In Wizard:
            - Repository: "migration_repo" von oben benutzen, Abschneiden NEIN
            - Projekt:
                - Name: z.B. "Migration mySQL igc_mv"
                - Ausgabeverzeichnis: z.B. C:\Users\martin\Desktop\ingrid_mySQL_2_oracle\20140707_migration_mv\migration\igc_mv
                    Im Ausgabeverzeichnis werden nützliche Skripte abgelegt (z.B. Oracle Schema, Skripte zum unload der Daten aus MySQL und Einspielen via sqlldr ...)
            - Quelldatenbank: online, mysql Verbindung wählen
            - Erfassen: igc_mv schon ausgewählt
(
            - !!! NICHT KONVERTIEREN !!! Da sonst z.B. Profil verloren geht, da varchar 4000 nicht groß genug !
                - ??? Konvertieren: ??? ODER SO LASSEN UND DANACH SCRIPTE AUSFÜHREN ???
                    - MEDIUMTEXT selektieren -> "Regel bearbeiten" -> Oracle-Datentyp: VARCHAR2, Gesamtstellenanzahl: 4000, Nachkommastellen: 0
                    - TEXT selektieren -> "Regel bearbeiten" -> Oracle-Datentyp: VARCHAR2, Gesamtstellenanzahl: 4000, Nachkommastellen: 0
)
            - Zieldatenbank:
				- Modus "Online"
                - Als Verbindung "redmond migration_repo" (s.o.) wählen, dies ist der Benutzer unter dem die Migration stattfindet (braucht Rechte zum Anlegen des neuen Schemas/Benutzers)
                - "Zielobjekte löschen" aktivieren
            - Daten verschieben:
                - zunächst Daten "Offline" migrieren
                - "Daten abschneiden" aktivieren
        - Nach Migration NEUE VERBINDUNG anlegen zu migriertem Schema: username / password ist der Datenbankname -> ACHTUNG: User ist case insensitive / passwd ist lowercase !
            - bei Migration Datenbank igc_mv -> Verbindung "igc_mv", USER/PASSWD = igc_mv / igc_mv
        - Dann via Migrationsprojekt Daten zu neuem Schema migrieren
            - Rechtsklick auf "Konvertierte Datenbankobjekte" (in Ansicht "Migrationsprojekte") und "Daten verschieben..."
            - Modus: Online, Quelle: "MySQL Verbindung", Ziel: igc_mv (= Verbindung zu neuem Schema/Benutzer), "Daten abschneiden" ja
- Migration mdek_mv:
    - s.o. igc_mv, hier nur Abweichungen
    - Vorgehen:
        - In Wizard:
            - Projekt:
                - Name: z.B. "Migration mySQL mdek_mv"
                - Ausgabeverzeichnis: z.B. C:\Users\martin\Desktop\ingrid_mySQL_2_oracle\20140707_migration_mv\migration\mdek_mv
            - Konvertieren: Keine Änderung
        - Nach Migration NEUE VERBINDUNG anlegen zu migriertem Schema:
            - bei Migration Datenbank mdek_mv -> Verbindung "mdek_mv", USER/PASSWD = mdek_mv / mdek_mv
        - Dann via Migrationsprojekt Daten zu neuem Schema migrieren, wie oben außer:
            - Ziel: mdek_mv (= Verbindung zu neuem Schema/Benutzer)
- Migration ingrid_portal_mv:
    - s.o. igc_mv hier nur Abweichungen
    - Vorgehen:
        - In Wizard:
            - Projekt:
                - Name: z.B. "Migration mySQL ingrid_portal_mv"
                - Ausgabeverzeichnis: z.B. C:\Users\martin\Desktop\ingrid_mySQL_2_oracle\20140707_migration_mv\migration\ingrid_portal_mv
            - Konvertieren: Keine Änderung
        - Nach Migration NEUE VERBINDUNG anlegen zu migriertem Schema:
            - bei Migration Datenbank ingrid_portal_mv -> Verbindung "ingrid_portal_mv", USER/PASSWD = ingrid_portal_mv / ingrid_portal_mv
        - Dann via Migrationsprojekt Daten zu neuem Schema migrieren, wie oben außer:
            - Ziel: ingrid_portal_mv (= Verbindung zu neuem Schema/Benutzer)

2.5 oracle dumps ausführen ?
----------------------------
- Zunächst den migrierten "User/Schemata" alle rechte zuweisen, sonst ORA-39145: directory object parameter must be specified and non-null
auf Redmond:
sqlplus (User system/Pwd s. groupware)
grant exp_full_database to igc_mv;
grant imp_full_database to igc_mv;
grant exp_full_database to mdek_mv;
grant imp_full_database to mdek_mv;
grant exp_full_database to ingrid_portal_mv;
grant imp_full_database to ingrid_portal_mv;
quit
    
- export ausführen:
    expdp igc_mv/igc_mv DUMPFILE=IGC_MV_INITIAL_20141105.dmp LOGFILE=IGC_MV_initial_20141105.log VERSION=10.2
    expdp mdek_mv/mdek_mv DUMPFILE=MDEK_MV.dmp LOGFILE=MDEK_MV.log VERSION=10.2
    expdp ingrid_portal_mv/ingrid_portal_mv DUMPFILE=INGRID_PORTAL_MV.dmp LOGFILE=INGRID_PORTAL_MV.log VERSION=10.2


3. Nach der Migration via SQL Developer folgende SQL Skripte im Schema der migrierten Datenbank ausführen:
-------------------------------------------------------------------------------------------------------

- Oracle Update Skripte für richtige InGrid Version (Portal, mdek) ?

- Fix Skripte ausführen:
	- IGC:		migration_igc3.2.0_fixes.sql
	- Portal:	migration_portal3.2.0_fixes.sql (s. ingrid-portal-distribution\src\main\resources\sql\migration_mysql2oracle)
	- mdek:		(kein Fix nötig)

ACHTUNG:
Im Portal werden alle Quartz Jobs gelöscht, da diese aus den persistenten Daten auf Oracle nicht erzeugt werden können (Fehler: EOF beim Lesen BLOB).
Die Default Jobs werden dann beim Starten des Portals wieder erzeugt.
Weitere Jobs zur Überwachung angeschlossener iPlugs etc. müssen im Portal neu angelegt werden (entsprechend "altem" Portal unter MySQL).


4. Danach in Komponenten Verbindung umstellen zu Oracle
-------------------------------------------------------
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
