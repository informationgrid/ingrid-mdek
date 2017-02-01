/*
 * **************************************************-
 * ingrid-import-export
 * ==================================================
 * Copyright (C) 2014 - 2017 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.1 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl5
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
package de.ingrid.mdek.xml.exporter.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.MdekUtils.IdcEntityType;
import de.ingrid.mdek.MdekUtils.IdcEntityVersion;
import de.ingrid.mdek.MdekUtils.WorkState;
import de.ingrid.mdek.caller.IMdekCaller.FetchQuantity;
import de.ingrid.mdek.caller.IMdekCallerAddress;
import de.ingrid.mdek.caller.IMdekCallerCatalog;
import de.ingrid.mdek.caller.IMdekCallerObject;
import de.ingrid.mdek.caller.IMdekClientCaller;
import de.ingrid.mdek.caller.MdekCaller;
import de.ingrid.mdek.caller.MdekCallerAddress;
import de.ingrid.mdek.caller.MdekCallerCatalog;
import de.ingrid.mdek.caller.MdekCallerObject;
import de.ingrid.mdek.caller.MdekCallerQuery;
import de.ingrid.mdek.caller.MdekCallerSecurity;
import de.ingrid.mdek.caller.MdekClientCaller;
import de.ingrid.mdek.xml.exporter.IExporterCallback;
import de.ingrid.utils.IngridDocument;

public class RemoteExporterCallback implements IExporterCallback {

	private IMdekClientCaller mdekClientCaller;
	private IMdekCallerObject mdekCallerObject;
	private IMdekCallerAddress mdekCallerAddress;
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
	public List<IngridDocument> getAddressDetails(String addrUuid,
			IdcEntityVersion whichVersion,
			String userUuid) {
		List<IngridDocument> addrInstances = new ArrayList<IngridDocument>();
		
		if (whichVersion == IdcEntityVersion.ALL_VERSIONS) {
			// always add WORKING_VERSION, may be PUBLISHED_VERSION if no working copy !
			IngridDocument addrDocResponse = mdekCallerAddress.fetchAddress(plugId, addrUuid, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.WORKING_VERSION, 0, 0, userUuid);
			IngridDocument addrInstance = mdekClientCaller.getResultFromResponse(addrDocResponse);
			long workingAddrId = -1L;
			if (addrInstance != null) {
				workingAddrId = (Long) addrInstance.get(MdekKeys.ID);
				addrInstances.add(addrInstance);
			}

			// then add PUBLISHED_VERSION if different
			addrDocResponse = mdekCallerAddress.fetchAddress(plugId, addrUuid, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.PUBLISHED_VERSION, 0, 0, userUuid);
			addrInstance = mdekClientCaller.getResultFromResponse(addrDocResponse);
			if (addrInstance != null && workingAddrId != (Long) addrInstance.get(MdekKeys.ID)) {
				addrInstances.add(addrInstance);
			}
			
		} else {
			IngridDocument addrDocResponse = mdekCallerAddress.fetchAddress(plugId, addrUuid, FetchQuantity.EDITOR_ENTITY, whichVersion, 0, 0, userUuid);
			IngridDocument addrInstance = mdekClientCaller.getResultFromResponse(addrDocResponse);
			if (addrInstance != null) {
				addrInstances.add(addrInstance);
			}
		}
		return addrInstances;
	}

	@Override
	public List<IngridDocument>  getObjectDetails(String objUuid,
			IdcEntityVersion whichVersion,
			String userUuid) {
		List<IngridDocument> objInstances = new ArrayList<IngridDocument>();
		
		if (whichVersion == IdcEntityVersion.ALL_VERSIONS) {
			// always add WORKING_VERSION, may be PUBLISHED_VERSION if no working copy !
			IngridDocument objDocResponse = mdekCallerObject.fetchObject(plugId, objUuid, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.WORKING_VERSION, userUuid);
			IngridDocument objInstance = mdekClientCaller.getResultFromResponse(objDocResponse);
			long workingObjId = -1L;
			if (objInstance != null) {
				workingObjId = (Long) objInstance.get(MdekKeys.ID);
				objInstances.add(objInstance);
			}

			// then add PUBLISHED_VERSION if different
			objDocResponse = mdekCallerObject.fetchObject(plugId, objUuid, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.PUBLISHED_VERSION, userUuid);
			objInstance = mdekClientCaller.getResultFromResponse(objDocResponse);
			if (objInstance != null && workingObjId != (Long) objInstance.get(MdekKeys.ID)) {
				objInstances.add(objInstance);
			}

			
		} else {
			IngridDocument objDocResponse = mdekCallerObject.fetchObject(plugId, objUuid, FetchQuantity.EDITOR_ENTITY, whichVersion, userUuid);
			IngridDocument objInstance = mdekClientCaller.getResultFromResponse(objDocResponse);
			if (objInstance != null) {
				objInstances.add(objInstance);
			}			
		}
		return objInstances;
	}

	@Override
	public List<String> getSubAddresses(String addrUuid,
			IdcEntityVersion whichVersion, String userUuid) {
		IngridDocument doc = mdekCallerAddress.fetchSubAddresses(plugId, addrUuid, userUuid);
		IngridDocument result = mdekClientCaller.getResultFromResponse(doc);
		return extractAddressUuids(result, whichVersion, userUuid);
	}

	private List<String> extractAddressUuids(IngridDocument result,
			IdcEntityVersion whichVersion,
			String userUuid) {
		List<IngridDocument> addressList = (List<IngridDocument>) result.get(MdekKeys.ADR_ENTITIES);
		List<String> addressUuids = new ArrayList<String>();

		for (IngridDocument address : addressList) {
			String addrUuid = address.getString(MdekKeys.UUID);
			 
			if (whichVersion == IdcEntityVersion.ALL_VERSIONS) {
				addressUuids.add(addrUuid);

			} else if (whichVersion == IdcEntityVersion.WORKING_VERSION) {
				// fetch WORKING_VERSION !
				IngridDocument addrDocResponse = mdekCallerAddress.fetchAddress(plugId, addrUuid, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.WORKING_VERSION, 0, 0, userUuid);
				IngridDocument addrInstance = mdekClientCaller.getResultFromResponse(addrDocResponse);
				String workState = addrInstance.getString(MdekKeys.WORK_STATE);
				if (!WorkState.VEROEFFENTLICHT.getDbValue().equals(workState)) {
					addressUuids.add(addrUuid);
				}

			} else if (whichVersion == IdcEntityVersion.PUBLISHED_VERSION) {
				// fetch PUBLISHED_VERSION !
				IngridDocument addrDocResponse = mdekCallerAddress.fetchAddress(plugId, addrUuid, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.PUBLISHED_VERSION, 0, 0, userUuid);
				IngridDocument addrInstance = mdekClientCaller.getResultFromResponse(addrDocResponse);
				if (addrInstance != null) {
					addressUuids.add(addrUuid);
				}
			}
		}
		return addressUuids;
	}

	@Override
	public List<String> getSubObjects(String objUuid,
			IdcEntityVersion whichVersion, String userUuid) {
		IngridDocument doc = mdekCallerObject.fetchSubObjects(plugId, objUuid, userUuid);
		IngridDocument result = mdekClientCaller.getResultFromResponse(doc);
		return extractObjectUuids(result, whichVersion, userUuid);
	}

	private List<String> extractObjectUuids(IngridDocument result,
			IdcEntityVersion whichVersion,
			String userUuid) {
		List<IngridDocument> objectList = (List<IngridDocument>) result.get(MdekKeys.OBJ_ENTITIES);
		List<String> objectUuids = new ArrayList<String>();

		for (IngridDocument object : objectList) {
			String objUuid = object.getString(MdekKeys.UUID);
 
			if (whichVersion == IdcEntityVersion.ALL_VERSIONS) {
				objectUuids.add(objUuid);

			} else if (whichVersion == IdcEntityVersion.WORKING_VERSION) {
				// fetch WORKING_VERSION !
				IngridDocument objDocResponse = mdekCallerObject.fetchObject(plugId, objUuid, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.WORKING_VERSION, userUuid);
				IngridDocument objInstance = mdekClientCaller.getResultFromResponse(objDocResponse);
				String workState = objInstance.getString(MdekKeys.WORK_STATE);
				if (!WorkState.VEROEFFENTLICHT.getDbValue().equals(workState)) {
					objectUuids.add(objUuid);
				}

			} else if (whichVersion == IdcEntityVersion.PUBLISHED_VERSION) {
				// fetch PUBLISHED_VERSION !
				IngridDocument objDocResponse = mdekCallerObject.fetchObject(plugId, objUuid, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.PUBLISHED_VERSION, userUuid);
				IngridDocument objInstance = mdekClientCaller.getResultFromResponse(objDocResponse);
				if (objInstance != null) {
					objectUuids.add(objUuid);
				}
			}
		}
		return objectUuids;
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
			IdcEntityVersion whichVersion,
			boolean includeSubnodes, String userUuid) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getTotalNumObjectsToExport(List<String> objUuids,
			IdcEntityVersion whichVersion,
			boolean includeSubnodes, String userUuid) {
		// TODO Auto-generated method stub
		return 0;
	}
}
