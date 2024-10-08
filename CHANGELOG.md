
## 7.3.0 (01.10.2024)


### Bugfixes

* getRecord-Anfrage im iBus nicht eindeutig (#6637)
* Fehler bei Codelist-Synchronisation (#6512)
    
## 7.1.0 (27.03.2024)

### Features

* AdV-MIS: Korrektur der Facette "Verwaltet von" (#5692)
* IGE: BKG-MIS - Feld "Zugriffsbeschränkungen" wieder herstellen (#5481)

### Bugfixes

* Korrekte Wiedergabe der WMS und WFS URLs im CSV-Export (#6074)
* Aktualisierung sicherheitskritischer Libraries (#6018)
    
## 7.0.0 (05.01.2024)

### Features

* Portal: Zugangs-URL von Dienst / Anwendung hervorheben (#5410)
* Umstellung Lizenz auf EUPL Version 1.2 (#1585)

### Bugfixes

* Aktualisierung sicherheitskritischer Libraries (#5724)
* INSPIRE Konformität der Metadatensätze nicht erfüllt (#5702)
* Fehlende Vorschaubilder im Editor (und Portal) (#5500)
    
## 6.3.0 (17.10.2023)

### Features

* Umsetzung der Angabe von InVeKoS-Daten im IGE-Classic (#5549)
* Facette für Filterung nach Themenbereichen / Kategorien (#4575)
* Kontakt "Vertrieb" als "Anbieter" im Portal konfigurieren - entsprechende Facette einrichten (#4488)
* Erweiterung Tipp des Tages um Darstellungen "Neuigkeiten im Datenbestand" (#4121)

### Bugfixes

* Portal: Verweise und Downloades unterschiedlich  (#5581)
* Daten-Dienste Kopplung   (#5575)
* Export von externen gekoppelten Daten inkorrekt ins ISO  (#5394)
* Indizierte Felder werden doppelt in die Konfiguration geschrieben  (#5381)
* IGE, Portal: Format des WKT und Anzeige des Datensatzes im Portal  (#4767)
    
## 6.2.0 (10.07.2023)

### Features

* Portal: Facettenanpassungen für Messverfahren und Similationsdaten (#5265)
* Reduzierung Memory Verbrauch (Anpassung der Garbage Collection Settings in JAVA) (#5117)
*  Anpassung des Importverlaufs für Adressen (#5092)
* HMDK Profil: Linkanpassung bei "Kartenansicht öffnen" (#4680)
* Portal: Reihung der Angaben ändern (#4533)

### Bugfixes

* Fehler beim CSW-Import  (#5314)
* Datenbezüge Adresse: Diskrepanz Anzahl in IGE und Portal  (#5305)
* Feststellungen im Update BKG-MIS auf 6.1.1, speziell fehlende MD  (#5222)
* Korrektur der GetCapabilities-URL  (#5204)
* environmentDescription fehlt im ISO  (#5188)
* ISO-XML Ausgabe im Datenfinder und Datenrepository nicht valide  (#5177)
* Fehler beim Indizieren von Adressen bei eingeschränkten Diensten  (#5089)
* Portal: LFS-Links werden nicht richtig angezeigt  (#4263)
    
## 6.1.0 (14.04.2023)

### Features

* HMDK Profil: Linkanpassung bei "Kartenansicht öffnen" (#4680)
* IGE: Korrektur Regionalschlüssel erfassen - Minimallösung (#4597)
* Kontakt "Vertrieb" als "Anbieter" im Portal konfigurieren - entsprechende Facette einrichten (#4488)
* "Portfolio" - Rechercheliste abspeichern / exportieren können (#4377)
* Mapclient - Positionierung über Request an BWaStr-Locator (#3807)
* Workaround für parentIdentifier mit problematischem Format (#3786)
* Portal: Facette "Diensttyp" im BKG- und AdV-MIS hinzufügen (#3278)

### Bugfixes

* Kritische Sicherheitslücke: snakeyaml + weitere  (#4972)
* Portal: Fehler bei der Anzeige von mehrsprachigen Feldern  (#4958)
* CSW-Schnittstelle: CSW-T Transaktionen funktionen nicht richtig  (#4917)
* IGE iPlug: Implementierung vom Ticket #4026 nicht im 6.0.1-Release vorhanden  (#4891)
* Nach Update Elasticsearch wird die Anzahl der Suchergebnisse beschränkt  (#4744)
* Fix für XSS-Schwachstelle kann nicht deployed werden  (#4734)
* IGE: anderssprachige Eingaben - fehlerhafte Ausgabe im ISO-XML  (#4670)
    
## 6.0.0 (13.01.2023)

### Features

* OAC fehlt in (manchen) Metadatensätzen (#4615)
* Zugriffs­beschränkun­gen - Angabe in MD ergänzen, Darstellung im Portal (#4576)
* IGE: ISO-XML - Maßangabe "m" und "meter" kommen vor - besser vereinheitlichen (#4505)
* Feld für Erfassung des OAC im IGE einfügen (#4378)
* Custom IGE-Felder nach IDF mappen (#4026)
* Metadatentransformation und -migration (#4008)
* IGE: Regionalschlüssel erfassen - Minimallösung (#3928)
* Raumbezug - Koordinateneingabe ermöglichen (#3888)
* Aktualisierung auf JAVA 17 (LTS): Umsetzung (#3324)
* IGE: IGE-Formular für Software anpassen (#2771)
* IGE: Anpassung der Liste "AdV-Produktgruppe" (#1535)
* ZEITERFASSUNG VKoopUIS (#2)

### Bugfixes

* Falsche Platzierung von environmentDescription im Export ISO  (#4622)
* MD zu CSW-Geodatendiensten: GetCapabilities-Verweis wird im MD doppelt angelegt  (#4260)
    