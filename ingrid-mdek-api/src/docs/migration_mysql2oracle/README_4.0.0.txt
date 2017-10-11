
Migration IGC 4.0.0 von MySQL nach Oracle
=========================================

Anmerkung:
Früher wurde bereits eine Migration für Kataloge in der Version 3.2.0 durchgeführt.
Hierzu existiert bereits eine README Datei, deren prinzipielles Vorgehen auch für Katalog 4.0.0 gilt.
Die 4.0.0 Migration wird hier allerdings separat beschrieben, um auf den aktuelleren Stand eingehen zu können (SQl Developer etc.).


Migration ausgeführt mit SQL Developer Version 17.3.0.271
http://www.oracle.com/technetwork/developer-tools/sql-developer/downloads/index.html

Manuals:
http://www.oracle.com/technetwork/database/migration/index.html
http://www.oracle.com/technetwork/database/migration/mysql-093223.html

In SQL Developer:

1. Migration-Repository anlegen
- neuen Benutzer "migration_repo" anlegen (mit allen Rechten, Rechte im Manual reichen nicht aus !)
- neue Verbindung zu diesem Benutzer anlegen
- diese Verbindung als Migration-Repository verknüpfen (Rechtsklick auf Connection / Migrations-Repository / ... verknüpfen)


2. MySQL verbinden
- Treiber Download von https://dev.mysql.com/downloads/connector/j/5.0.html
- Treiber .jar Datei in SQL Developer installieren (Extras / Voreinstellungen / Datenbank / JDBC-Treiber eines anderen Herstellers)
- neue Verbindung anlegen und unter Reiter "MySQL" entsprechende Parameter zur MySQL Datenbank angeben


3. MySQL migrieren
- Rechtsklick auf MySql Verbindung und "Zu Oracle migrieren ..."
- In Wizard:
  - wie in Manual (Video) beschrieben migrieren, also
    - Repository: Repo von oben wählen, "Abschneiden" nur, wenn Repo komplett geleert werden soll
    - Projekt: sinnvollen Namen wählen, z.B. "MySQL_igc_sl_kommunal", in Ausgabeverzeichnis werden Skripte des Schemas oder zur Datenübertragung gespeichert
    - Quelldatenbank: "Online", MySQL Verbindung von oben wählen
    - Erfassen: zu konvertierende Datenbank von links nach rechts schieben
    - Konvertieren: unverändert lassen
    - Übersetzen:
        - die checkbox "Weiter zu Zusammenfassungsseite" anklicken und "Fertigstellen" klicken
        -> Konvertierung des Schemas wird ausgeführt und im Migrations-Repo abgelegt
- danach im Migrations-Repo unter gewähltem Projektnamen "Konvertierte Datenbankobjekte" selektieren: Rechts in den Reitern wird Info ausgegeben, z.B. "Erfassungsprobleme" und "Konverttierungsprobleme" prüfen
- jetzt Zieldatenbank anlegen und Daten verschieben, dafür Rechtsklick auf "Konvertierte Datenbankobjekte" und "Ziel generieren ...."
- In Wizard:
	- Zieldatenbank:
        - "Online", als Verbindung Migration-Repository wählen, dies ist der Benutzer unter dem die Migration stattfindet (braucht Rechte zum Anlegen des neuen Schemas/Benutzers)
        - "Zielobjekte löschen" aktivieren
	- Daten verschieben:
        - zunächst Daten "Offline" migrieren
        - "Daten abschneiden" aktivieren, damit Daten in Zieldatenbank neu erzeugt werden
    - Zusammenfassung: prüfen und "Fertigstellen"
- Nach Migration NEUE VERBINDUNG anlegen zu migriertem Schema: username / password ist der Datenbankname -> ACHTUNG: User ist case insensitive / passwd ist lowercase !
	- bei Migration Datenbank igc_sl_kommunal ist USER / PASSWD z.B. igc_sl_kommunal / igc_sl_kommunal
- Dann via Migrationsprojekt Daten zu neuem Schema migrieren
	- Rechtsklick auf "Konvertierte Datenbankobjekte" (in Ansicht "Migrationsprojekte") und "Daten verschieben..."
	- Modus: "Online", Quelle: MySQl Verbindung wählen, Ziel: Verbindung zu neuem Schema wählen (z.B. igc_sl_kommunal), "Daten abschneiden" aktivieren, damit Daten in Zieldatenbank neu erzeugt werden


Optional: Oracle dump ausführen von migrierter Datenbank
--------------------------------------------------------
- Um auf der migrierten Datenbank zu dumpen muss dem neuen Benutzer (z.B. igc_sl_kommunal) dieses Recht zugewiesen werden, ansonsten erscheint
ORA-39145: directory object parameter must be specified and non-null

Recht wird als DBA erteilt mit:
	grant exp_full_database to igc_sl_kommunal;
	grant imp_full_database to igc_sl_kommunal;
	
Dump wäre dann z.B. auf Oracle Server:
	expdp igc_sl_kommunal/igc_sl_kommunal DUMPFILE=igc_sl_kommunal_INITIAL.dmp LOGFILE=igc_sl_kommunal_INITIAL.log VERSION=10.2


Nach der Migration folgendes SQL Skript im Schema des migrierten Katalogs ausführen:
-------------------------------------------------------------------------------------

- migration_igc4.0.0_fixes.sql



Migration Benutzer aus mdek und Portal Datenbank
================================================

Migration aus mdek Datenbank
---------------------------- 

- Tabelle user_data mit vollständigen INSERTS extrahieren
	- z.B. mit phpMyAdmin:
		- Tabelle "user_data" anzeigen
		- Exportieren: "SQL", nur "Daten" klicken, nur "Vollständige 'INSERT's" klicken
	- aus erzeugter Datei die Gänsefüsschen ` entfernen

	  Resultat z.B.
		INSERT INTO user_data (id, version, addr_uuid, portal_login, plug_id) VALUES(3145730, 2, '93C77F51-7724-48A5-849E-87A1680E9E74', 'user 1', '/ingrid-group:ige-iplug1');
		INSERT INTO user_data (id, version, addr_uuid, portal_login, plug_id) VALUES(3375104, 2, '7B9F7CDD-38B8-4889-AF26-834BC25CB703', 'user 2', '/ingrid-group:ige-iplug2');
		...

- In Zieldatenbank einspielen
	- ACHTUNG: Die ids der Zeilen dürfen nicht vorhanden sein und werden in der mdek Datenbank mittels hibernate hilo Algorithmus erzeugt.
	  Der Hi Wert ist in der Tabelle hibernate_unique_key abgelegt, der Lo Wert ist java short.MAX_VALUE==2^15-1=32767
	  Da die so erzeugten IDs große Freiräume lassen, ist der schnelle Weg, niedrige, nicht benutzte Ids manuell zu setzen (die Anzahl der Benutzer hält sich normalerweise in Grenzen).
	  Das finale Script, mit Ids ab 1, könnte also wie folgt aussehen:

		INSERT INTO user_data (id, version, addr_uuid, portal_login, plug_id) VALUES(1, 2, '93C77F51-7724-48A5-849E-87A1680E9E74', 'user 1', '/ingrid-group:ige-iplug1');
		INSERT INTO user_data (id, version, addr_uuid, portal_login, plug_id) VALUES(2, 2, '7B9F7CDD-38B8-4889-AF26-834BC25CB703', 'user 2', '/ingrid-group:ige-iplug2');
		...

	  	
Migration aus Portal Datenbank (manuell)
----------------------------------------

- aus Portal Tabellen entsprechende INSERTS extrahieren mit phpMyAdmin:
	- Vorgehen für alle nachfolgenden Tabellen:
		- SELECT ausführen
		- unter Anzeige "Alle auswählen" und "Exportieren"
		- Exportieren: "SQL", nur "Daten" klicken, nur "Vollständige 'INSERT's" klicken
			- sollte es Fehler geben muss das SQL zum Export vereinfacht werden, dann nur auf der akt. Tabelle die entsprechenden Sätze mit den IDs selektieren 
		- aus erzeugter Datei die Gänsefüsschen ` entfernen
		- die INSERTS in eine Gesamtdatei kopieren

	- aus Tabelle security_principal
		SELECT * from security_principal where PRINCIPAL_TYPE='user' and NOT PRINCIPAL_NAME = 'admin' and NOT PRINCIPAL_NAME = 'guest' ORDER BY PRINCIPAL_ID;

	  Resultat z.B.
		INSERT INTO security_principal (PRINCIPAL_ID, PRINCIPAL_TYPE, PRINCIPAL_NAME, IS_MAPPED, IS_ENABLED, IS_READONLY, IS_REMOVABLE, CREATION_DATE, MODIFIED_DATE, DOMAIN_ID) VALUES(802, 'user', 's.wannemacher', 1, 1, 0, 1, '2016-03-30 09:46:08', '2017-05-03 09:53:41', 1);
		INSERT INTO security_principal (PRINCIPAL_ID, PRINCIPAL_TYPE, PRINCIPAL_NAME, IS_MAPPED, IS_ENABLED, IS_READONLY, IS_REMOVABLE, CREATION_DATE, MODIFIED_DATE, DOMAIN_ID) VALUES(821, 'user', 'test.wannemacher', 1, 1, 0, 1, '2016-03-30 10:58:52', '2017-05-03 09:54:20', 1);
		...

	- aus Tabelle security_credential
		SELECT * FROM security_credential WHERE EXISTS (SELECT * FROM security_principal sp WHERE sp.principal_id = security_credential.principal_id AND sp.PRINCIPAL_TYPE='user' and NOT sp.PRINCIPAL_NAME = 'admin' and NOT sp.PRINCIPAL_NAME = 'guest') ORDER BY PRINCIPAL_ID;

	  Resultat z.B.
		INSERT INTO security_credential (CREDENTIAL_ID, PRINCIPAL_ID, CREDENTIAL_VALUE, TYPE, UPDATE_ALLOWED, IS_STATE_READONLY, UPDATE_REQUIRED, IS_ENCODED, IS_ENABLED, AUTH_FAILURES, IS_EXPIRED, CREATION_DATE, MODIFIED_DATE, PREV_AUTH_DATE, LAST_AUTH_DATE, EXPIRATION_DATE) VALUES(782, 802, '/U6oSE0gy0YiKF5CW1uTJDVx7Sg=', 0, 1, 0, 0, 1, 1, 0, 0, '2016-03-30 09:46:08', '2016-03-31 14:19:11', NULL, '2016-03-31 14:19:11', NULL);
		INSERT INTO security_credential (CREDENTIAL_ID, PRINCIPAL_ID, CREDENTIAL_VALUE, TYPE, UPDATE_ALLOWED, IS_STATE_READONLY, UPDATE_REQUIRED, IS_ENCODED, IS_ENABLED, AUTH_FAILURES, IS_EXPIRED, CREATION_DATE, MODIFIED_DATE, PREV_AUTH_DATE, LAST_AUTH_DATE, EXPIRATION_DATE) VALUES(801, 821, '0ysFvjqKIMIwqrXaoghsOATF+14=', 0, 1, 0, 0, 1, 1, 0, 0, '2016-03-30 10:58:52', '2016-03-30 10:58:52', NULL, NULL, NULL);
		...

	- aus Tabelle security_principal_assoc
		SELECT * FROM security_principal_assoc WHERE EXISTS (SELECT * FROM security_principal sp WHERE sp.principal_id = security_principal_assoc.FROM_PRINCIPAL_ID AND sp.PRINCIPAL_TYPE='user' and NOT sp.PRINCIPAL_NAME = 'admin' and NOT sp.PRINCIPAL_NAME = 'guest') ORDER BY FROM_PRINCIPAL_ID;

	  Resultat z.B.
		INSERT INTO security_principal_assoc (ASSOC_NAME, FROM_PRINCIPAL_ID, TO_PRINCIPAL_ID) VALUES('isMemberOf', 802, 10);
		INSERT INTO security_principal_assoc (ASSOC_NAME, FROM_PRINCIPAL_ID, TO_PRINCIPAL_ID) VALUES('isMemberOf', 802, 13);
		...

	- aus Tabelle security_attribute
		SELECT * FROM security_attribute WHERE EXISTS (SELECT * FROM security_principal sp WHERE sp.principal_id = security_attribute.principal_id AND sp.PRINCIPAL_TYPE='user' and NOT sp.PRINCIPAL_NAME = 'admin' and NOT sp.PRINCIPAL_NAME = 'guest') AND attr_name LIKE 'user.%' ORDER BY principal_id, attr_name;
		
	  Resultat z.B.
		INSERT INTO security_attribute (ATTR_ID, PRINCIPAL_ID, ATTR_NAME, ATTR_VALUE) VALUES(9982, 802, 'user.business-info.online.email', 'user1@user.de');
		INSERT INTO security_attribute (ATTR_ID, PRINCIPAL_ID, ATTR_NAME, ATTR_VALUE) VALUES(9983, 802, 'user.business-info.postal.city', '');
		...

	- aus Tabelle principal_rule_assoc
		SELECT * FROM principal_rule_assoc WHERE EXISTS (SELECT * FROM security_principal sp WHERE sp.PRINCIPAL_NAME = principal_rule_assoc.PRINCIPAL_NAME AND sp.PRINCIPAL_TYPE='user' and NOT sp.PRINCIPAL_NAME = 'admin' and NOT sp.PRINCIPAL_NAME = 'guest') ORDER BY PRINCIPAL_NAME;

	  Resultat z.B.
		INSERT INTO principal_rule_assoc (PRINCIPAL_NAME, LOCATOR_NAME, RULE_ID) VALUES('User1', 'page', 'user-role-fallback');
		INSERT INTO principal_rule_assoc (PRINCIPAL_NAME, LOCATOR_NAME, RULE_ID) VALUES('User2', 'page', 'user-role-fallback');
		...


- Alle obigen INSERTS müssen noch angepasst werden
	- so dass die IDs in der Zieldatenbank nicht vorhanden sind (PRINCIPAL_ID, CREDENTIAL_ID, ATTR_ID)
	- die Ids der Rechte übereinstimmen (security_principal_assoc.TO_PRINCIPAL_ID)

	...
