
"Test Suite"
-----------

1. Mdek Server starten
	Projekt:
		ingrid-mdek-job
	Main Class:
		de.ingrid.mdek.MdekServer
	Arguments:
		--descriptor C:\...\ingrid-mdek\trunk\ingrid-mdek-job\src\main\resources\communication.properties

2. Client Example ausführen
	Projekt:
		ingrid-mdek-api
	Main Class:
		de.ingrid.mdek.example.MdekExample
	Arguments:
		--descriptor C:\...\ingrid-mdek\trunk\ingrid-mdek-api\src\main\resources\communication.properties --threads 1
