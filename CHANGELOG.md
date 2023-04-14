
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
    