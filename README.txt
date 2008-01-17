
Konfigurieren:
--------------

1. MdekServer konfigurieren:
	- Kommunikationsparameter konfigurieren:
		- in ingrid-mdek-job/src/main/resources/communication.properties
	- Jobverzeichnis konfigurieren:
		- in ingrid-mdek-job/src/main/resources/default-job.properties
	- MdekJob "einspielen":
		- ingrid-mdek-job/src/main/resources/persistentJobs/de.ingrid.mdek.job.MdekIdcJob.xml nach Jobverzeichnis kopieren
	- Datenbank konfigurieren:
		- in ingrid-mdek-services/resources/default-datasource.properties
	- Services Laufzeitverzeichnisse konfigurieren:
		- in ingrid-mdek-services/resources/core-services.properties
	- Hibernate sql Ausgabe einschalten:
		- in ingrid-mdek-services/resources/datasource-services.xml
				"<prop key="hibernate.show_sql">true</prop>"

2. MdekClient konfigurieren:
	- Kommunikationsparameter konfigurieren:
		- in ingrid-mdek-api/src/main/resources/communication.properties
	- wenn zusaetzliche Debugging Ausgabe gewollt wird:
		- in ingrid-mdek-api/pom.xml die log4j.properties als resource hinzunehmen (dekommentieren) -> werden beim build nach classes kopiert


Build
-----

- cd C:\...\ingrid-mdek\trunk
	mvn clean
	mvn install -Dmaven.test.skip=true


Distribution erstellen
----------------------

- cd C:\...\ingrid-mdek\trunk
mvn clean package assembly:assembly -Dmaven.test.skip=true

"Test Suite"
-----------

1. Mdek Server starten
	Projekt:
		ingrid-mdek-job
	Main Class:
		de.ingrid.mdek.MdekServer
  Zusätzliche Einträge im classpath:
    ingrid-mdek-job/src/main/resources
    ingrid-mdek-services/src/main/resources
  
	Arguments:
		--descriptor C:\...\ingrid-mdek\trunk\ingrid-mdek-job\src\main\resources\communication.properties

2. Client Example ausführen
	Projekt:
		ingrid-mdek-api
	Main Class:
		de.ingrid.mdek.example.MdekExample
	Arguments:
		--descriptor C:\...\ingrid-mdek\trunk\ingrid-mdek-api\src\main\resources\communication.properties --threads 1
