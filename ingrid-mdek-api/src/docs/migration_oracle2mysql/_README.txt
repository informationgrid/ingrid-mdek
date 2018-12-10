
MetaVer 4.2.1 Oracle nach MySQL Migration mit SQLLINES
======================================================

Vorbereitung
------------
Download und Installation Tools s. auch https://redmine.wemove.com/projects/ingrid/wiki/HH_LGV_Testumgebung_Oracle#Migration-nach-MySQL

Migration geschieht mit "SQLines Data" Tool s. http://www.sqlines.com/sqldata/oracle-to-mysql

Für Oracle und MySQL Connections folgendes installieren:
- Oracle Instant Client: http://www.sqlines.com/sqldata_oracle_connection
  Installiert und "oci.dll" in "...\sqlinesdata31773_x64_win\sqldata.cfg" hinzugefügt
- MySQL Connector C: http://www.sqlines.com/sqldata_mysql_connection
  "MySQL Connector C 6.1" installiert und "...MySQL Connector C 6.1\lib" in PATH hinzugefügt

Migration IGC
-------------

ACHTUNG:
Die MetaVer Kataloge liegen in der Version 4.2.0_a vor, dies ist die Version vom IGE iPlug 4.2.0.
Die Scripte hier tragen Version 4.2.1, da dies die MetaVer Portal Version ist.
Es gibt kein IGE iPlug Release in der Version 4.2.1 !

- MySQL Datenbank anlegen, z.B.
    CREATE DATABASE IF NOT EXISTS igc_hh DEFAULT CHARACTER SET utf8 COLLATE utf8_unicode_ci;
    
- Grund-Schema einspielen:
    421_igc_mysql_schema.sql

- Schema an Oracle Spaltenreihenfolge anpassen:
    ACHTUNG: igc_hh und igc_sl_* haben andere Spaltenreihenfolge in Oracle als die anderen Kataloge, d.h. folgendes ausführen
        für igc_hh
            421_igc_mysql_schema_fix_tables_HH.sql
        für igc_sl_kommunal und igc_sl_umwelt
            421_igc_mysql_schema_fix_tables_SL.sql
        für alle anderen Kataloge
            421_igc_mysql_schema_fix_tables.sql

- dann via SQLLINES Daten einspielen, z.B. für igc_hh
    - Source Oracle
        - 192.168.0.237:1521/xe
        - User Name: IGC_HH
    - Source MySQL
        - localhost 3306
        - Database: igc_hh
    - Settings
        - Table List: IGC_HH.*
          "Load the Default Schema" angeklickt lassen
        - Transfer Options: Truncate
        - Performance Options: 1 (damit log chronologisch abläuft)
    - click auf "Transfer"
    - in Tab "Raw Log" detaillierte Ausgaben, auch in log Dateien im sqllines Verzeichnis
    - click auf "Validate" überprüft Anzahl Zeilen in Tabellen

- für SL Kataloge nach der Datenmigration in MySQL alte Spalten löschen, hier sind in Oracle noch Überbleibsel, die nicht mehr benötigt werden (aber migriert wurden):
    421_igc_mysql_schema_post_migration_SL.sql
