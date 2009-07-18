package de.ingrid.mdek.xml.exporter.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.MdekUtils.IdcEntityType;
import de.ingrid.mdek.MdekUtils.IdcEntityVersion;
import de.ingrid.mdek.caller.IMdekCallerAddress;
import de.ingrid.mdek.caller.IMdekCallerCatalog;
import de.ingrid.mdek.caller.IMdekCallerObject;
import de.ingrid.mdek.caller.IMdekCallerQuery;
import de.ingrid.mdek.caller.IMdekClientCaller;
import de.ingrid.mdek.caller.MdekCaller;
import de.ingrid.mdek.caller.MdekCallerAddress;
import de.ingrid.mdek.caller.MdekCallerCatalog;
import de.ingrid.mdek.caller.MdekCallerObject;
import de.ingrid.mdek.caller.MdekCallerQuery;
import de.ingrid.mdek.caller.MdekCallerSecurity;
import de.ingrid.mdek.caller.MdekClientCaller;
import de.ingrid.mdek.caller.IMdekCaller.FetchQuantity;
import de.ingrid.mdek.xml.exporter.IExporterCallback;
import de.ingrid.utils.IngridDocument;

public class RemoteExporterCallback implements IExporterCallback {

	private IMdekClientCaller mdekClientCaller;
	private IMdekCallerObject mdekCallerObject;
	private IMdekCallerAddress mdekCallerAddress;
	private IMdekCallerQuery mdekCallerQuery;
	private IMdekCallerCatalog mdekCallerCatalog;

	private String plugId;

	public RemoteExporterCallback() {
		setupConnection();
	}

	@Override
	protected void finalize() throws Throwable {
		shutdownConnection();
		super.finalize();
	}

	@Override
	public IngridDocument getAddressDetails(String addrUuid, String userUuid) {
		IngridDocument adrDocResponse = mdekCallerAddress.fetchAddress(plugId, addrUuid, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.PUBLISHED_VERSION, 0, 0, userUuid);
		return mdekClientCaller.getResultFromResponse(adrDocResponse);
	}

	@Override
	public IngridDocument getObjectDetails(String objUuid, String userUuid) {
		IngridDocument objDocResponse = mdekCallerObject.fetchObject(plugId, objUuid, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.PUBLISHED_VERSION, userUuid);
		return mdekClientCaller.getResultFromResponse(objDocResponse);
	}

	@Override
	public List<String> getSubAddresses(String addrUuid, String userUuid) {
		IngridDocument doc = mdekCallerAddress.fetchSubAddresses(plugId, addrUuid, userUuid);
		IngridDocument result = mdekClientCaller.getResultFromResponse(doc);
		return extractAddressUuids(result);
	}

	private List<String> extractAddressUuids(IngridDocument result) {
		List<IngridDocument> addressList = (List<IngridDocument>) result.get(MdekKeys.ADR_ENTITIES);
		List<String> addressUuids = new ArrayList<String>();

		for (IngridDocument address : addressList) {
			addressUuids.add(address.getString(MdekKeys.UUID));
		}
		return addressUuids;
	}

	@Override
	public List<String> getSubObjects(String objUuid, String userUuid) {
		IngridDocument doc = mdekCallerObject.fetchSubObjects(plugId, objUuid, userUuid);
		IngridDocument result = mdekClientCaller.getResultFromResponse(doc);
		return extractObjectUuids(result);
	}

	private List<String> extractObjectUuids(IngridDocument result) {
		List<IngridDocument> objectList = (List<IngridDocument>) result.get(MdekKeys.OBJ_ENTITIES);
		List<String> objectUuids = new ArrayList<String>();

		for (IngridDocument object : objectList) {
			objectUuids.add(object.getString(MdekKeys.UUID));
		}
		return objectUuids;
	}

	@Override
	public IngridDocument getSysAdditionalFields(Long[] fieldIds) {
		IngridDocument additionalFieldsResponse = mdekCallerCatalog.getSysAdditionalFields(plugId, null, null, "admin");
		return mdekClientCaller.getResultFromResponse(additionalFieldsResponse);
	}

	@Override
	public void writeExportInfo(IdcEntityType whichType, int numExported,
			int totalNum, String userUuid) {
		System.out.println("Exporting '"+whichType+"'. Count: "+numExported);
	}

	private void setupConnection() {
		File communicationProperties = new File("src/test/resources/communication.properties");
		if (communicationProperties == null || !(communicationProperties instanceof File) || !communicationProperties.exists()) {
			throw new IllegalStateException(
					"Please specify the location of the communication.properties file via the Property 'mdekClientCaller.properties' in /src/resources/mdek.properties");
		}
		MdekClientCaller.initialize(communicationProperties);
		mdekClientCaller = MdekClientCaller.getInstance();

		MdekCallerObject.initialize(mdekClientCaller);
		MdekCallerAddress.initialize(mdekClientCaller);
		MdekCallerQuery.initialize(mdekClientCaller);
		MdekCallerCatalog.initialize(mdekClientCaller);
		MdekCallerSecurity.initialize(mdekClientCaller);

		mdekCallerObject = MdekCallerObject.getInstance();
		mdekCallerAddress = MdekCallerAddress.getInstance();
		mdekCallerQuery = MdekCallerQuery.getInstance();
		mdekCallerCatalog = MdekCallerCatalog.getInstance();

		waitForConnection();
	}

	private void waitForConnection() {
		while (mdekClientCaller.getRegisteredIPlugs().size() == 0) {
			try {
				Thread.sleep(1000);

			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		plugId = mdekClientCaller.getRegisteredIPlugs().get(0);
	}

	private static void shutdownConnection() {
		MdekCaller.shutdown();
	}

	@Override
	public void writeExportInfoMessage(String newMessage, String userUuid) {
		System.out.println("Exporter sent message: "+newMessage);
	}

	@Override
	public int getTotalNumAddressesToExport(List<String> addrUuids,
			boolean includeSubnodes, String userUuid) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getTotalNumObjectsToExport(List<String> objUuids,
			boolean includeSubnodes, String userUuid) {
		// TODO Auto-generated method stub
		return 0;
	}
}
