package de.ingrid.mdek.example;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.ingrid.mdek.EnumUtil;
import de.ingrid.mdek.MdekError;
import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.MdekKeysSecurity;
import de.ingrid.mdek.MdekUtils;
import de.ingrid.mdek.MdekUtilsSecurity;
import de.ingrid.mdek.MdekError.MdekErrorType;
import de.ingrid.mdek.MdekUtils.AddressType;
import de.ingrid.mdek.MdekUtils.IdcEntityOrderBy;
import de.ingrid.mdek.MdekUtils.IdcEntityVersion;
import de.ingrid.mdek.MdekUtils.IdcQAEntitiesSelectionType;
import de.ingrid.mdek.MdekUtils.IdcStatisticsSelectionType;
import de.ingrid.mdek.MdekUtils.IdcWorkEntitiesSelectionType;
import de.ingrid.mdek.MdekUtils.ObjectType;
import de.ingrid.mdek.MdekUtils.PublishType;
import de.ingrid.mdek.MdekUtils.WorkState;
import de.ingrid.mdek.MdekUtilsSecurity.IdcPermission;
import de.ingrid.mdek.caller.IMdekClientCaller;
import de.ingrid.mdek.caller.MdekCaller;
import de.ingrid.mdek.caller.MdekClientCaller;
import de.ingrid.mdek.caller.IMdekCaller.AddressArea;
import de.ingrid.mdek.caller.IMdekCaller.FetchQuantity;
import de.ingrid.utils.IngridDocument;

/**
 * Encapsulates common example methods ...
 */
public class MdekExampleSupertool {

	private IMdekClientCaller mdekClientCaller;
	
	private MdekExampleSupertoolCatalog supertoolCatalog;
	private MdekExampleSupertoolSecurity supertoolSecurity;
	private MdekExampleSupertoolObject supertoolObject;
	private MdekExampleSupertoolAddress supertoolAddress;
	private MdekExampleSupertoolQuery supertoolQuery;

	// MDEK SERVER TO CALL !
	private String plugId;
	private String myUserUuid;
	boolean doFullOutput = true;

	public MdekExampleSupertool(String plugIdToCall,
			String callingUserUuid)
	{
		this.plugId = plugIdToCall;
		myUserUuid = callingUserUuid;

		mdekClientCaller = MdekClientCaller.getInstance();
		
		supertoolCatalog = new MdekExampleSupertoolCatalog(plugIdToCall, callingUserUuid, this);
		supertoolSecurity = new MdekExampleSupertoolSecurity(plugIdToCall, callingUserUuid, this);
		supertoolObject = new MdekExampleSupertoolObject(plugIdToCall, callingUserUuid, this);
		supertoolAddress = new MdekExampleSupertoolAddress(plugIdToCall, callingUserUuid, this);
		supertoolQuery = new MdekExampleSupertoolQuery(plugIdToCall, callingUserUuid, this);
	}

	public void setPlugIdToCall(String plugIdToCall)
	{
		this.plugId = plugIdToCall;

		supertoolCatalog.setPlugIdToCall(plugIdToCall);
		supertoolSecurity.setPlugIdToCall(plugIdToCall);
		supertoolObject.setPlugIdToCall(plugIdToCall);
		supertoolAddress.setPlugIdToCall(plugIdToCall);
		supertoolQuery.setPlugIdToCall(plugIdToCall);
	}

	public void setCallingUser(String callingUserUuid)
	{
		this.myUserUuid = callingUserUuid;
		
		supertoolCatalog.setCallingUser(callingUserUuid);
		supertoolSecurity.setCallingUser(callingUserUuid);
		supertoolObject.setCallingUser(callingUserUuid);
		supertoolAddress.setCallingUser(callingUserUuid);
		supertoolQuery.setCallingUser(callingUserUuid);
		
		System.out.println("\n###### NEW CALLING USER = " + callingUserUuid + " ######");		
	}
	public String getCallingUserUuid()
	{
		return myUserUuid;
	}

	public void setFullOutput(boolean doFullOutput)
	{
		this.doFullOutput = doFullOutput;
		
		supertoolCatalog.setFullOutput(doFullOutput);
		supertoolSecurity.setFullOutput(doFullOutput);
		supertoolObject.setFullOutput(doFullOutput);
		supertoolAddress.setFullOutput(doFullOutput);
		supertoolQuery.setFullOutput(doFullOutput);
	}

	// MdekExampleSupertoolCatalog FACADE !
	// ----------------------------------
	
	public void getVersion() {
		supertoolCatalog.getVersion();
	}
	public IngridDocument getCatalog() {
		return supertoolCatalog.getCatalog();
	}
	public IngridDocument storeCatalog(IngridDocument catDocIn,
			boolean refetchCatalog) {
		return supertoolCatalog.storeCatalog(catDocIn, refetchCatalog);
	}
	public IngridDocument getSysGuis(String[] guiIds) {
		return supertoolCatalog.getSysGuis(guiIds);
	}
	public IngridDocument storeSysGuis(List<IngridDocument> sysGuis,
			boolean refetch) {
		return supertoolCatalog.storeSysGuis(sysGuis, refetch);
	}
	public IngridDocument getSysGenericKeys(String[] keyNames) {
		return supertoolCatalog.getSysGenericKeys(keyNames);
	}
	public IngridDocument storeSysGenericKeys(String[] keyNames, String[] keyValues) {
		return supertoolCatalog.storeSysGenericKeys(keyNames, keyValues);
	}
	public IngridDocument getSysLists(Integer[] listIds, String language) {
		return supertoolCatalog.getSysLists(listIds, language);
	}
	public IngridDocument getSysAdditionalFields(Long[] fieldIds, String languageCode) {
		return supertoolCatalog.getSysAdditionalFields(fieldIds, languageCode);
	}
	public IngridDocument exportObjectBranch(String rootUuid, boolean exportOnlyRoot) {
		return supertoolCatalog.exportObjectBranch(rootUuid, exportOnlyRoot);
	}
	public IngridDocument exportObjects(String exportCriteria) {
		return supertoolCatalog.exportObjects(exportCriteria);
	}
	public IngridDocument exportAddressBranch(String rootUuid,
			boolean exportOnlyRoot, AddressArea addressArea) {
		return supertoolCatalog.exportAddressBranch(rootUuid, exportOnlyRoot, addressArea);
	}
	public IngridDocument getExportInfo(boolean includeExportData) {
		return supertoolCatalog.getExportInfo(includeExportData);
	}
	public IngridDocument importEntities(byte[] importData,
			String objectImportNodeUuid, String addressImportNodeUuid,
			boolean publishImmediately, boolean doSeparateImport) {
		return supertoolCatalog.importEntities(importData,
				objectImportNodeUuid, addressImportNodeUuid,
				publishImmediately, doSeparateImport);
	}
	public IngridDocument getImportInfo() {
		return supertoolCatalog.getImportInfo();
	}

	public IngridDocument analyze() {
		return supertoolCatalog.analyze();
	}
	
	// MdekExampleSupertoolSecurity FACADE !
	// ----------------------------------

	public IngridDocument getCatalogAdmin() {
		return supertoolSecurity.getCatalogAdmin();
	}
	public IngridDocument getGroups(boolean includeCatAdminGroup) {
		return supertoolSecurity.getGroups(includeCatAdminGroup);
	}
	public IngridDocument getGroupDetails(String grpName) {
		return supertoolSecurity.getGroupDetails(grpName);
	}
	public IngridDocument getUsersOfGroup(String grpName) {
		return supertoolSecurity.getUsersOfGroup(grpName);
	}
/*
	private IngridDocument getUserDetails(String addrUuid) {
		return supertoolSecurity.getUserDetails(addrUuid);
	}
*/
	public IngridDocument getSubUsers(Long parentUserId) {
		return supertoolSecurity.getSubUsers(parentUserId);
	}
	public IngridDocument getUsersWithWritePermissionForObject(String objUuid,
			boolean checkWorkflow,
			boolean getDetailedPermissions) {
		return supertoolSecurity.getUsersWithWritePermissionForObject(objUuid, checkWorkflow, getDetailedPermissions);
	}
	public IngridDocument getUsersWithWritePermissionForAddress(String addrUuid,
			boolean checkWorkflow,
			boolean getDetailedPermissions) {
		return supertoolSecurity.getUsersWithWritePermissionForAddress(addrUuid, checkWorkflow, getDetailedPermissions);
	}
	public IngridDocument getObjectPermissions(String objUuid, boolean checkWorkflow) {
		return supertoolSecurity.getObjectPermissions(objUuid, checkWorkflow);
	}
	public IngridDocument getAddressPermissions(String addrUuid, boolean checkWorkflow) {
		return supertoolSecurity.getAddressPermissions(addrUuid, checkWorkflow);
	}
	public IngridDocument getUserPermissions() {
		return supertoolSecurity.getUserPermissions();
	}
	public IngridDocument createGroup(IngridDocument docIn,
			boolean refetch) {
		return supertoolSecurity.createGroup(docIn, refetch);
	}
	public IngridDocument createUser(IngridDocument docIn,
			boolean refetch) {
		return supertoolSecurity.createUser(docIn, refetch);
	}	
	/** ALWAYS ADDS QA user-permission to group to avoid conflicts when workflow is enabled !!! */
	public IngridDocument storeGroup(IngridDocument docIn,
			boolean refetch) {
		return storeGroup(docIn, refetch, true);
	}
	public IngridDocument storeGroup(IngridDocument docIn,
			boolean refetch,
			boolean alwaysAddQA) {
		return supertoolSecurity.storeGroup(docIn, refetch, alwaysAddQA);
	}
	public IngridDocument storeUser(IngridDocument docIn,
			boolean refetch) {
		return supertoolSecurity.storeUser(docIn, refetch);
	}	
	public IngridDocument deleteUser(Long idcUserId) {
		return supertoolSecurity.deleteUser(idcUserId);
	}	
	public IngridDocument deleteGroup(Long idcGroupId,
			boolean forceDeleteGroupWhenUsers) {
		return supertoolSecurity.deleteGroup(idcGroupId, forceDeleteGroupWhenUsers);
	}

	// MdekExampleSupertoolObject FACADE !
	// ----------------------------------

	public IngridDocument getObjectPath(String uuidIn) {
		return supertoolObject.getObjectPath(uuidIn);
	}
	public IngridDocument getInitialObject(IngridDocument newBasicObject) {
		return supertoolObject.getInitialObject(newBasicObject);
	}
	public IngridDocument fetchTopObjects() {
		return supertoolObject.fetchTopObjects();
	}
	public IngridDocument fetchSubObjects(String uuid) {
		return supertoolObject.fetchSubObjects(uuid);
	}
	/** Fetches WORKING VERSION of object ! */
	public IngridDocument fetchObject(String uuid, FetchQuantity howMuch) {
		return fetchObject(uuid, howMuch, IdcEntityVersion.WORKING_VERSION);
		
	}
	public IngridDocument fetchObject(String uuid, FetchQuantity howMuch, IdcEntityVersion whichVersion) {
		return supertoolObject.fetchObject(uuid, howMuch, whichVersion);
	}
	public IngridDocument checkObjectSubTree(String uuid) {
		return supertoolObject.checkObjectSubTree(uuid);
	}
	public IngridDocument storeObject(IngridDocument oDocIn,
			boolean refetchObject) {
		return supertoolObject.storeObject(oDocIn, refetchObject);
	}
	public IngridDocument updateObjectPart(IngridDocument oPartDocIn, IdcEntityVersion whichVersion) {
		return supertoolObject.updateObjectPart(oPartDocIn, whichVersion);
	}
	public IngridDocument assignObjectToQA(IngridDocument oDocIn,
			boolean refetchObject) {
		return supertoolObject.assignObjectToQA(oDocIn, refetchObject);
	}
	public IngridDocument reassignObjectToAuthor(IngridDocument oDocIn,
			boolean refetchObject) {
		return supertoolObject.reassignObjectToAuthor(oDocIn, refetchObject);
	}
	public IngridDocument publishObject(IngridDocument oDocIn,
			boolean withRefetch,
			boolean forcePublicationCondition) {
		return supertoolObject.publishObject(oDocIn, withRefetch, forcePublicationCondition);
	}
	public IngridDocument moveObject(String fromUuid, String toUuid,
			boolean forcePublicationCondition) {
		return supertoolObject.moveObject(fromUuid, toUuid, forcePublicationCondition);
	}
	public IngridDocument copyObject(String fromUuid, String toUuid, boolean copySubtree) {
		return supertoolObject.copyObject(fromUuid, toUuid, copySubtree);
	}
	public IngridDocument deleteObjectWorkingCopy(String uuid,
			boolean forceDeleteReferences) {
		return supertoolObject.deleteObjectWorkingCopy(uuid, forceDeleteReferences);
	}
	public IngridDocument deleteObject(String uuid,
			boolean forceDeleteReferences) {
		return supertoolObject.deleteObject(uuid, forceDeleteReferences);
	}
	public IngridDocument getWorkObjects(IdcWorkEntitiesSelectionType selectionType,
			IdcEntityOrderBy orderBy, boolean orderAsc,
			int startHit, int numHits) {
		return supertoolObject.getWorkObjects(selectionType,
				orderBy, orderAsc,
				startHit, numHits);
	}
	/**
	 * @param whichWorkState only return objects in this work state, pass null if all workstates
	 * @param selectionType further selection criteria (see Enum), pass null if all objects
	 * @param startHit paging: hit to start with (first hit is 0)
	 * @param numHits paging: number of hits requested, beginning from startHit
	 */
	public IngridDocument getQAObjects(WorkState whichWorkState,
			IdcQAEntitiesSelectionType selectionType,
			IdcEntityOrderBy orderBy, boolean orderAsc,
			int startHit, int numHits) {
		return supertoolObject.getQAObjects(whichWorkState, selectionType,
				orderBy, orderAsc,
				startHit, numHits);
	}
	public IngridDocument getObjectStatistics(String uuidIn,
			IdcStatisticsSelectionType whichType,
			int startHit, int numHits) {
		return supertoolObject.getObjectStatistics(uuidIn, whichType,
				startHit, numHits);
	}

	// MdekExampleSupertoolAddress FACADE !
	// ----------------------------------

	public IngridDocument getAddressPath(String uuidIn) {
		return supertoolAddress.getAddressPath(uuidIn);
	}
	public IngridDocument getInitialAddress(IngridDocument newBasicAddress) {
		return supertoolAddress.getInitialAddress(newBasicAddress);
	}
	public IngridDocument fetchTopAddresses(boolean onlyFreeAddresses) {
		return supertoolAddress.fetchTopAddresses(onlyFreeAddresses);
	}
	public IngridDocument fetchSubAddresses(String uuid) {
		return supertoolAddress.fetchSubAddresses(uuid);
	}
	/** Fetches WORKING VERSION of address ! Don't "page" object references to address instead fetch first 50 ones ! */
	public IngridDocument fetchAddress(String uuid, FetchQuantity howMuch) {
		return fetchAddress(uuid, howMuch, IdcEntityVersion.WORKING_VERSION, 0, 50);
	}
	/** Fetches requested version of address ! Don't "page" object references to address instead fetch first 50 ones ! */
	public IngridDocument fetchAddress(String uuid, FetchQuantity howMuch, IdcEntityVersion whichVersion) {
		return fetchAddress(uuid, howMuch, whichVersion, 0, 50);
	}
	/** Fetches WORKING VERSION of address ! */
	public IngridDocument fetchAddress(String uuid, FetchQuantity howMuch,
			int objRefsStartIndex, int objRefsMaxNum) {
		return fetchAddress(uuid, howMuch, IdcEntityVersion.WORKING_VERSION,
				objRefsStartIndex, objRefsMaxNum);
	}
	public IngridDocument fetchAddress(String uuid, FetchQuantity howMuch, IdcEntityVersion whichVersion,
			int objRefsStartIndex, int objRefsMaxNum) {
		return supertoolAddress.fetchAddress(uuid,
				howMuch, whichVersion,
				objRefsStartIndex, objRefsMaxNum);
	}
	public IngridDocument fetchAddressObjectReferences(String uuid, int objRefsStartIndex, int objRefsMaxNum) {
		return supertoolAddress.fetchAddressObjectReferences(uuid,
				objRefsStartIndex, objRefsMaxNum);
	}
	public IngridDocument checkAddressSubTree(String uuid) {
		return supertoolAddress.checkAddressSubTree(uuid);
	}
	/** Don't "page" object references to address instead fetch first 50 ones ! */
	public IngridDocument storeAddress(IngridDocument aDocIn,
			boolean refetchAddress) {
		return storeAddress(aDocIn, refetchAddress, 0, 50);
	}
	public IngridDocument storeAddress(IngridDocument aDocIn,
			boolean refetchAddress, int objRefsStartIndex, int objRefsMaxNum) {
		return supertoolAddress.storeAddress(aDocIn, refetchAddress,
				objRefsStartIndex, objRefsMaxNum);
	}
	public IngridDocument updateAddressPart(IngridDocument aPartDocIn, IdcEntityVersion whichVersion) {
		return supertoolAddress.updateAddressPart(aPartDocIn, whichVersion);
	}
	/** Don't "page" object references to address instead fetch first 50 ones ! */
	public IngridDocument assignAddressToQA(IngridDocument aDocIn,
			boolean refetchAddress) {
		return assignAddressToQA(aDocIn, refetchAddress, 0, 50);
	}
	public IngridDocument assignAddressToQA(IngridDocument aDocIn,
			boolean refetchAddress, int objRefsStartIndex, int objRefsMaxNum) {
		return supertoolAddress.assignAddressToQA(aDocIn, refetchAddress,
				objRefsStartIndex, objRefsMaxNum);
	}
	/** Don't "page" object references to address instead fetch first 50 ones ! */
	public IngridDocument reassignAddressToAuthor(IngridDocument aDocIn,
			boolean refetchAddress) {
		return reassignAddressToAuthor(aDocIn, refetchAddress, 0, 50);
	}
	public IngridDocument reassignAddressToAuthor(IngridDocument aDocIn,
			boolean refetchAddress, int objRefsStartIndex, int objRefsMaxNum) {
		return supertoolAddress.reassignAddressToAuthor(aDocIn, refetchAddress,
				objRefsStartIndex, objRefsMaxNum);
	}
	/** Don't "page" object references to address instead fetch first 50 ones ! */
	public IngridDocument publishAddress(IngridDocument aDocIn,
			boolean refetchAddress) {
		return publishAddress(aDocIn, refetchAddress, 0, 50);
	}
	public IngridDocument publishAddress(IngridDocument aDocIn,
			boolean withRefetch, int objRefsStartIndex, int objRefsMaxNum) {
		return supertoolAddress.publishAddress(aDocIn, withRefetch,
				objRefsStartIndex, objRefsMaxNum);
	}
	public IngridDocument moveAddress(String fromUuid, String toUuid,
			boolean moveToFreeAddress) {
		return supertoolAddress.moveAddress(fromUuid, toUuid, moveToFreeAddress);
	}
	public IngridDocument copyAddress(String fromUuid, String toUuid,
			boolean copySubtree, boolean copyToFreeAddress) {
		return supertoolAddress.copyAddress(fromUuid, toUuid,
				copySubtree, copyToFreeAddress);
	}
	public IngridDocument deleteAddressWorkingCopy(String uuid,
			boolean forceDeleteReferences) {
		return supertoolAddress.deleteAddressWorkingCopy(uuid, forceDeleteReferences);
	}
	public IngridDocument deleteAddress(String uuid,
			boolean forceDeleteReferences) {
		return supertoolAddress.deleteAddress(uuid, forceDeleteReferences);
	}

	public IngridDocument searchAddress(IngridDocument searchParams,
			int startHit, int numHits) {
		return supertoolAddress.searchAddress(searchParams, startHit, numHits);
	}
	public IngridDocument getWorkAddresses(IdcWorkEntitiesSelectionType selectionType,
			IdcEntityOrderBy orderBy, boolean orderAsc,
			int startHit, int numHits) {
		return supertoolAddress.getWorkAddresses(selectionType,
				orderBy, orderAsc,
				startHit, numHits);
	}
	/**
	 * @param whichWorkState only return addresses in this work state, pass null if all workstates
	 * @param selectionType further selection criteria (see Enum), pass null if all addresses
	 * @param startHit paging: hit to start with (first hit is 0)
	 * @param numHits paging: number of hits requested, beginning from startHit
	 */
	public IngridDocument getQAAddresses(WorkState whichWorkState,
			IdcQAEntitiesSelectionType selectionType,
			IdcEntityOrderBy orderBy, boolean orderAsc,
			int startHit, int numHits) {
		return supertoolAddress.getQAAddresses(whichWorkState, selectionType,
				orderBy, orderAsc,
				startHit, numHits);
	}
	public IngridDocument getAddressStatistics(String uuidIn, boolean onlyFreeAddresses,
			IdcStatisticsSelectionType whichType,
			int startHit, int numHits) {
		return supertoolAddress.getAddressStatistics(uuidIn,
				onlyFreeAddresses, whichType,
				startHit, numHits);
	}

	// MdekExampleSupertoolQuery FACADE !
	// ----------------------------------

	public List<IngridDocument> queryObjectsFullText(String searchTerm,
			int startHit, int numHits) {
		return supertoolQuery.queryObjectsFullText(searchTerm, startHit, numHits);
	}
	public List<IngridDocument> queryObjectsThesaurusTerm(String termSnsId,
			int startHit, int numHits) {
		return supertoolQuery.queryObjectsThesaurusTerm(termSnsId, startHit, numHits);
	}
	public List<IngridDocument> queryObjectsExtended(IngridDocument searchParams,
			int startHit, int numHits) {
		return supertoolQuery.queryObjectsExtended(searchParams, startHit, numHits);
	}
	public List<IngridDocument> queryAddressesFullText(String queryTerm,
			int startHit, int numHits) {
		return supertoolQuery.queryAddressesFullText(queryTerm, startHit, numHits);
	}
	public List<IngridDocument> queryAddressesThesaurusTerm(String termSnsId,
			int startHit, int numHits) {
		return supertoolQuery.queryAddressesThesaurusTerm(termSnsId, startHit, numHits);
	}
	public List<IngridDocument> queryAddressesExtended(IngridDocument searchParams,
			int startHit, int numHits) {
		return supertoolQuery.queryAddressesExtended(searchParams, startHit, numHits);
	}	
	public void queryHQL(String qString,
			int startHit, int numHits) {
		supertoolQuery.queryHQL(qString, startHit, numHits);
	}
	public void queryHQLToCsv(String qString) {
		supertoolQuery.queryHQLToCsv(qString);
	}
	public IngridDocument queryHQLToMap(String qString, Integer maxNumHits) {
		return supertoolQuery.queryHQLToMap(qString, maxNumHits);
	}

	// MdekExampleSupertool METHODS !
	// ----------------------------------

	public void sleep(int sleepTimeMillis) {
		try {
			Thread.sleep(sleepTimeMillis);				
		} catch(Exception ex) {
			System.out.println(ex);
		}		
	}

	public void trackRunningJob(int sleepTimeMillis, boolean doCancel) {
		IngridDocument response;
		IngridDocument result;
		System.out.println("\n###### INVOKE getRunningJobInfo ######");

		boolean jobIsRunning = true;
		int counter = 0;
		while (jobIsRunning) {
			if (doCancel && counter > 4) {
				cancelRunningJob();
				return;
			}

			response = mdekClientCaller.getRunningJobInfo(plugId, myUserUuid);
			result = mdekClientCaller.getResultFromResponse(response);
			if (result != null) {
				String jobDescr = result.getString(MdekKeys.RUNNINGJOB_TYPE);
				Integer numObjs = (Integer) result.get(MdekKeys.RUNNINGJOB_NUMBER_PROCESSED_ENTITIES);
				Integer total = (Integer) result.get(MdekKeys.RUNNINGJOB_NUMBER_TOTAL_ENTITIES);
				if (jobDescr == null) {
					// job finished !
					jobIsRunning = false;					
					System.out.println("JOB FINISHED\n");
				} else {
					System.out.println("job:" + jobDescr + ", entities:" + numObjs + ", total:" + total);
				}
			} else {
				handleError(response);
				jobIsRunning = false;
			}
			
			sleep(sleepTimeMillis);

			counter++;
		}
	}

	public IngridDocument getRunningJobInfo() {
		long startTime;
		long endTime;
		long neededTime;
		IngridDocument response;
		IngridDocument result;

		System.out.println("\n###### INVOKE getRunningJobInfo ######");
		startTime = System.currentTimeMillis();
		response = mdekClientCaller.getRunningJobInfo(plugId, myUserUuid);
		endTime = System.currentTimeMillis();
		neededTime = endTime - startTime;
		System.out.println("EXECUTION TIME: " + neededTime + " ms");
		result = mdekClientCaller.getResultFromResponse(response);
		if (result != null) {
			System.out.println("SUCCESS: ");
			System.out.println(result);
		} else {
			handleError(response);
		}
		
		return result;
	}

	public boolean hasRunningJob() {
		boolean jobRunning = false;

		IngridDocument jobInfo = getRunningJobInfo();
		if (jobInfo != null && !jobInfo.isEmpty()) {
			jobRunning = true;			
		}
		
		return jobRunning;
	}

	public void cancelRunningJob() {
		System.out.println("\n###### INVOKE cancelRunningJob ######");

		IngridDocument response = mdekClientCaller.cancelRunningJob(plugId, myUserUuid);
		IngridDocument result = mdekClientCaller.getResultFromResponse(response);
		if (result != null) {
			String jobDescr = result.getString(MdekKeys.RUNNINGJOB_TYPE);
			if (jobDescr == null) {
				System.out.println("JOB FINISHED\n");
			} else {
				System.out.println("JOB CANCELED: " + result);
			}
		} else {
			handleError(response);
		}
	}

	public String extractUserData(IngridDocument inDoc) {
		if (inDoc == null) {
			return null; 
		}

		String user = inDoc.getString(MdekKeys.UUID);
		if (inDoc.get(MdekKeys.NAME) != null) {
			user += " " + inDoc.get(MdekKeys.NAME);
		}
		if (inDoc.get(MdekKeys.GIVEN_NAME) != null) {
			user += " " + inDoc.get(MdekKeys.GIVEN_NAME);
		}
		if (inDoc.get(MdekKeys.ORGANISATION) != null) {
			user += " " + inDoc.get(MdekKeys.ORGANISATION);
		}
		
		return user;
	}

	public void debugCatalogDoc(IngridDocument c) {
		System.out.println("Catalog: " + c.get(MdekKeysSecurity.CATALOG_NAME) 
			+ ", partner: " + c.get(MdekKeys.PARTNER_NAME)
			+ ", provider: " + c.get(MdekKeys.PROVIDER_NAME)
			+ ", country: " + c.get(MdekKeys.COUNTRY)
			+ ", language: " + c.get(MdekKeys.LANGUAGE)
		);
		System.out.println("         "
			+ ", workflow: " + c.get(MdekKeys.WORKFLOW_CONTROL)
			+ ", expiry: " + c.get(MdekKeys.EXPIRY_DURATION)
			+ ", created: " + MdekUtils.timestampToDisplayDate((String)c.get(MdekKeys.DATE_OF_CREATION))
			+ ", modified: " + MdekUtils.timestampToDisplayDate((String)c.get(MdekKeys.DATE_OF_LAST_MODIFICATION))
			+ ", modUser: " + extractUserData((IngridDocument)c.get(MdekKeys.MOD_USER))
		);

		if (!doFullOutput) {
			return;
		}

		System.out.println("  " + c);

		System.out.println("  Location: " + c.get(MdekKeys.CATALOG_LOCATION));
	}
	
	public void debugUserDoc(IngridDocument u) {
		System.out.println("User: " + u.get(MdekKeysSecurity.IDC_USER_ID) 
			+ ", " + u.get(MdekKeysSecurity.IDC_USER_ADDR_UUID)
			+ ", name: " + u.get(MdekKeys.TITLE_OR_FUNCTION)
			+ " " + u.get(MdekKeys.GIVEN_NAME)
			+ " " + u.get(MdekKeys.NAME)
			+ ", organisation: " + u.get(MdekKeys.ORGANISATION)
			+ ", created: " + MdekUtils.timestampToDisplayDate((String)u.get(MdekKeys.DATE_OF_CREATION))
			+ ", modified: " + MdekUtils.timestampToDisplayDate((String)u.get(MdekKeys.DATE_OF_LAST_MODIFICATION))
			+ ", modUuid: " + extractUserData((IngridDocument)u.get(MdekKeys.MOD_USER))
		);

		if (!doFullOutput) {
			return;
		}

		System.out.println("  " + u);

		debugPermissionsDoc(u, "  ");
	}
	
	public void debugGroupDoc(IngridDocument g) {
		System.out.println("Group: " + g.get(MdekKeysSecurity.IDC_GROUP_ID) 
			+ ", " + g.get(MdekKeys.NAME)
			+ ", created: " + MdekUtils.timestampToDisplayDate((String)g.get(MdekKeys.DATE_OF_CREATION))
			+ ", modified: " + MdekUtils.timestampToDisplayDate((String)g.get(MdekKeys.DATE_OF_LAST_MODIFICATION))
			+ ", modUuid: " + extractUserData((IngridDocument)g.get(MdekKeys.MOD_USER))
		);

		if (!doFullOutput) {
			return;
		}

		System.out.println("  " + g);

		List<IngridDocument> docList;

		docList = (List<IngridDocument>) g.get(MdekKeysSecurity.IDC_USER_PERMISSIONS);
		if (docList != null && docList.size() > 0) {
			System.out.println("  User Permissions: " + docList.size() + " Entries");
			for (IngridDocument doc : docList) {
				System.out.println("    " + doc);								
			}			
		}

		docList = (List<IngridDocument>) g.get(MdekKeysSecurity.IDC_ADDRESS_PERMISSIONS);
		if (docList != null && docList.size() > 0) {
			System.out.println("  Address Permissions: " + docList.size() + " Entries");
			for (IngridDocument doc : docList) {
				System.out.println("    " + doc);								
			}			
		}
		docList = (List<IngridDocument>) g.get(MdekKeysSecurity.IDC_OBJECT_PERMISSIONS);
		if (docList != null && docList.size() > 0) {
			System.out.println("  Object Permissions: " + docList.size() + " Entries");
			for (IngridDocument doc : docList) {
				System.out.println("    " + doc);								
			}			
		}
	}

	public void debugPermissionsDoc(IngridDocument p, String indent) {
		List<IngridDocument> docList = (List<IngridDocument>) p.get(MdekKeysSecurity.IDC_PERMISSIONS);
		if (docList != null && docList.size() > 0) {
			System.out.println(indent + "Permissions: " + docList.size() + " Entries");
			for (IngridDocument doc : docList) {
				System.out.println(indent + "  " + doc);								
			}			
		} else {
			System.out.println(indent + "No Permissions");			
		}
	}
	
	public void debugPermissionsDocBoolean(IngridDocument p) {
		List<IngridDocument> docList = (List<IngridDocument>) p.get(MdekKeysSecurity.IDC_PERMISSIONS);
		System.out.println("HAS_WRITE_ACCESS: " + MdekUtilsSecurity.hasWritePermission(docList));
		System.out.println("HAS_WRITE_TREE_ACCESS: " + MdekUtilsSecurity.hasWriteTreePermission(docList));
		System.out.println("HAS_WRITE_SINGLE_ACCESS: " + MdekUtilsSecurity.hasWriteSinglePermission(docList));
	}
	
	public void debugIdcUsersDoc(IngridDocument u) {
		List<IngridDocument> docList = (List<IngridDocument>) u.get(MdekKeysSecurity.IDC_USERS);
		if (docList != null && docList.size() > 0) {
			System.out.println("Users: " + docList.size() + " Entries");
			for (IngridDocument doc : docList) {
				System.out.println("    " + doc);								
			}			
		}
	}
	
	public void debugObjectDoc(IngridDocument o) {
		System.out.println("Object: " + o.get(MdekKeys.ID) 
			+ ", " + o.get(MdekKeys.UUID)
			+ ", class: " + EnumUtil.mapDatabaseToEnumConst(ObjectType.class, o.get(MdekKeys.CLASS))
			+ ", " + o.get(MdekKeys.TITLE)
			+ ", marked deleted: " + o.get(MdekKeys.MARK_DELETED)
		);
		System.out.println("        "
			+ ", status: " + EnumUtil.mapDatabaseToEnumConst(WorkState.class, o.get(MdekKeys.WORK_STATE))
			+ ", modUser: " + extractUserData((IngridDocument)o.get(MdekKeys.MOD_USER))
			+ ", respUser: " + extractUserData((IngridDocument)o.get(MdekKeys.RESPONSIBLE_USER))
			+ ", assignerUser: " + extractUserData((IngridDocument)o.get(MdekKeys.ASSIGNER_USER))
			+ ", modified: " + MdekUtils.timestampToDisplayDate((String)o.get(MdekKeys.DATE_OF_LAST_MODIFICATION))
			+ ", created: " + MdekUtils.timestampToDisplayDate((String)o.get(MdekKeys.DATE_OF_CREATION))
			+ ", publication condition: " + EnumUtil.mapDatabaseToEnumConst(PublishType.class, o.get(MdekKeys.PUBLICATION_CONDITION))
//			+ ", cat_id: " + o.get(MdekKeys.CATALOGUE_IDENTIFIER)
		);

		System.out.println("  " + o);

		if (!doFullOutput) {
			return;
		}

		debugPermissionsDoc(o, "  ");

		IngridDocument myDoc;
		List<IngridDocument> docList = (List<IngridDocument>) o.get(MdekKeys.OBJ_REFERENCES_TO);
		if (docList != null && docList.size() > 0) {
			System.out.println("  Objects TO (Querverweise): " + docList.size() + " Entities");
			for (IngridDocument doc : docList) {
				System.out.println("   " + doc.get(MdekKeys.UUID) + ": " + doc);								
			}			
		}
		docList = (List<IngridDocument>) o.get(MdekKeys.OBJ_REFERENCES_FROM);
		if (docList != null && docList.size() > 0) {
			System.out.println("  Objects FROM (Querverweise): " + docList.size() + " Entities");
			for (IngridDocument doc : docList) {
				System.out.println("   " + doc.get(MdekKeys.UUID) + ": " + doc);								
			}			
		}
		docList = (List<IngridDocument>) o.get(MdekKeys.OBJ_REFERENCES_FROM_PUBLISHED_ONLY);
		if (docList != null && docList.size() > 0) {
			System.out.println("  Objects FROM (Querverweise) ONLY PUBLISHED !!!: " + docList.size() + " Entities");
			for (IngridDocument doc : docList) {
				System.out.println("   " + doc.get(MdekKeys.UUID) + ": " + doc);								
			}			
		}
		docList = (List<IngridDocument>) o.get(MdekKeys.ADR_REFERENCES_TO);
		if (docList != null && docList.size() > 0) {
			System.out.println("  Addresses TO: " + docList.size() + " Entities");
			for (IngridDocument a : docList) {
				System.out.println("   " + a.get(MdekKeys.UUID) + ": " + a);								
				List<IngridDocument> coms = (List<IngridDocument>) a.get(MdekKeys.COMMUNICATION);
				if (coms != null) {
					System.out.println("    Communication: " + coms.size() + " Entities");
					for (IngridDocument c : coms) {
						System.out.println("     " + c);
					}					
				}
			}			
		}
		docList = (List<IngridDocument>) o.get(MdekKeys.LOCATIONS);
		if (docList != null && docList.size() > 0) {
			System.out.println("  Locations (Spatial References): " + docList.size() + " entries");
			for (IngridDocument doc : docList) {
				System.out.println("   " + doc);								
			}			
		}
		docList = (List<IngridDocument>) o.get(MdekKeys.SUBJECT_TERMS);
		if (docList != null && docList.size() > 0) {
			System.out.println("  Subject terms (Searchterms): " + docList.size() + " entries");
			for (IngridDocument doc : docList) {
				System.out.println("   " + doc);								
			}			
		}
		docList = (List<IngridDocument>) o.get(MdekKeys.SUBJECT_TERMS_INSPIRE);
		if (docList != null && docList.size() > 0) {
			System.out.println("  INSPIRE Searchterms (Themen): " + docList.size() + " entries");
			for (IngridDocument doc : docList) {
				System.out.println("   " + doc);								
			}			
		}
		docList = (List<IngridDocument>) o.get(MdekKeys.LINKAGES);
		if (docList != null && docList.size() > 0) {
			System.out.println("  URL References: " + docList.size() + " entries");
			for (IngridDocument doc : docList) {
				System.out.println("   " + doc);								
			}			
		}
		docList = (List<IngridDocument>) o.get(MdekKeys.DATASET_REFERENCES);
		if (docList != null && docList.size() > 0) {
			System.out.println("  Dataset References: " + docList.size() + " entries");
			for (IngridDocument doc : docList) {
				System.out.println("   " + doc);								
			}			
		}
		List<String> strList = (List<String>) o.get(MdekKeys.EXPORT_CRITERIA);
		if (strList != null && strList.size() > 0) {
			System.out.println("  Exports: " + strList.size() + " entries");
			System.out.println("   " + strList);
		}
		strList = (List<String>) o.get(MdekKeys.LEGISLATIONS);
		if (strList != null && strList.size() > 0) {
			System.out.println("  Legislations: " + strList.size() + " entries");
			System.out.println("   " + strList);
		}
		docList = (List<IngridDocument>) o.get(MdekKeys.DATA_FORMATS);
		if (docList != null && docList.size() > 0) {
			System.out.println("  Data Formats: " + docList.size() + " entries");
			for (IngridDocument doc : docList) {
				System.out.println("   " + doc);								
			}			
		}
		docList = (List<IngridDocument>) o.get(MdekKeys.MEDIUM_OPTIONS);
		if (docList != null && docList.size() > 0) {
			System.out.println("  Medium Options: " + docList.size() + " entries");
			for (IngridDocument doc : docList) {
				System.out.println("   " + doc);								
			}			
		}
		strList = (List<String>) o.get(MdekKeys.ENV_CATEGORIES);
		if (strList != null && strList.size() > 0) {
			System.out.println("  Env Categories: " + strList.size() + " entries");
			System.out.println("   " + strList);
		}
		strList = (List<String>) o.get(MdekKeys.ENV_TOPICS);
		if (strList != null && strList.size() > 0) {
			System.out.println("  Env Topics: " + strList.size() + " entries");
			System.out.println("   " + strList);
		}
		List<Integer> intList = (List<Integer>) o.get(MdekKeys.TOPIC_CATEGORIES);
		if (intList != null && intList.size() > 0) {
			System.out.println("  Topic Categories: " + intList.size() + " entries");
			System.out.println("   " + intList);
		}

		myDoc = (IngridDocument) o.get(MdekKeys.TECHNICAL_DOMAIN_MAP);
		if (myDoc != null) {
			System.out.println("  technical domain MAP:");
			System.out.println("    " + myDoc);								
			docList = (List<IngridDocument>) myDoc.get(MdekKeys.KEY_CATALOG_LIST);
			if (docList != null && docList.size() > 0) {
				System.out.println("    MAP - key catalogs: " + docList.size() + " entries");
				for (IngridDocument doc : docList) {
					System.out.println("     " + doc);								
				}			
			}
			docList = (List<IngridDocument>) myDoc.get(MdekKeys.PUBLICATION_SCALE_LIST);
			if (docList != null && docList.size() > 0) {
				System.out.println("    MAP - publication scales (Erstellungsma�stab): " + docList.size() + " entries");
				for (IngridDocument doc : docList) {
					System.out.println("     " + doc);								
				}			
			}
			docList = (List<IngridDocument>) myDoc.get(MdekKeys.SYMBOL_CATALOG_LIST);
			if (docList != null && docList.size() > 0) {
				System.out.println("    MAP - symbol catalogs: " + docList.size() + " entries");
				for (IngridDocument doc : docList) {
					System.out.println("     " + doc);								
				}			
			}
			strList = (List<String>) myDoc.get(MdekKeys.FEATURE_TYPE_LIST);
			if (docList != null && docList.size() > 0) {
				System.out.println("    MAP - feature types: " + strList.size() + " entries");
				for (String str : strList) {
					System.out.println("     " + str);								
				}			
			}
			docList = (List<IngridDocument>) myDoc.get(MdekKeys.GEO_VECTOR_LIST);
			if (docList != null && docList.size() > 0) {
				System.out.println("    MAP - vector formats, geo vector list: " + docList.size() + " entries");
				for (IngridDocument doc : docList) {
					System.out.println("     " + doc);								
				}			
			}
			intList = (List<Integer>) myDoc.get(MdekKeys.SPATIAL_REPRESENTATION_TYPE_LIST);
			if (docList != null && docList.size() > 0) {
				System.out.println("    MAP - spatial rep types: " + intList.size() + " entries");
				for (Integer i : intList) {
					System.out.println("     " + i);								
				}			
			}
		}
		myDoc = (IngridDocument) o.get(MdekKeys.TECHNICAL_DOMAIN_DOCUMENT);
		if (myDoc != null) {
			System.out.println("  technical domain DOCUMENT:");
			System.out.println("    " + myDoc);								
		}

		myDoc = (IngridDocument) o.get(MdekKeys.TECHNICAL_DOMAIN_SERVICE);
		if (myDoc != null) {
			System.out.println("  technical domain SERVICE:");
			System.out.println("    " + myDoc);								
			strList = (List<String>) myDoc.get(MdekKeys.SERVICE_VERSION_LIST);
			if (strList != null && strList.size() > 0) {
				System.out.println("    SERVICE - versions: " + strList.size() + " entries");
				System.out.println("     " + strList);
			}
			docList = (List<IngridDocument>) myDoc.get(MdekKeys.SERVICE_TYPE2_LIST);
			if (docList != null && docList.size() > 0) {
				System.out.println("    SERVICE - types (INSPIRE): " + docList.size() + " entries");
				for (IngridDocument doc : docList) {
					System.out.println("      " + doc);								
				}
			}
			docList = (List<IngridDocument>) myDoc.get(MdekKeys.PUBLICATION_SCALE_LIST);
			if (docList != null && docList.size() > 0) {
				System.out.println("    SERVICE - publication scales = Erstellungsma�stab (INSPIRE): " + docList.size() + " entries");
				for (IngridDocument doc : docList) {
					System.out.println("      " + doc);								
				}
			}
			docList = (List<IngridDocument>) myDoc.get(MdekKeys.SERVICE_OPERATION_LIST);
			if (docList != null && docList.size() > 0) {
				System.out.println("    SERVICE - operations: " + docList.size() + " entries");
				for (IngridDocument doc : docList) {
					System.out.println("      " + doc);								
					strList = (List<String>) doc.get(MdekKeys.PLATFORM_LIST);
					if (strList != null && strList.size() > 0) {
						System.out.println("      SERVICE - operation - platforms: " + strList.size() + " entries");
						System.out.println("        " + strList);
					}
					strList = (List<String>) doc.get(MdekKeys.DEPENDS_ON_LIST);
					if (strList != null && strList.size() > 0) {
						System.out.println("      SERVICE - operation - dependsOns: " + strList.size() + " entries");
						System.out.println("        " + strList);
					}
					strList = (List<String>) doc.get(MdekKeys.CONNECT_POINT_LIST);
					if (strList != null && strList.size() > 0) {
						System.out.println("      SERVICE - operation - connectPoints: " + strList.size() + " entries");
						System.out.println("        " + strList);
					}
					List<IngridDocument> docList2 = (List<IngridDocument>) doc.get(MdekKeys.PARAMETER_LIST);
					if (docList2 != null) {
						System.out.println("      SERVICE - operation - parameters: " + docList2.size() + " entries");
						for (IngridDocument doc2 : docList2) {
							System.out.println("        " + doc2);
						}			
					}
				}
			}
		}
		myDoc = (IngridDocument) o.get(MdekKeys.TECHNICAL_DOMAIN_PROJECT);
		if (myDoc != null) {
			System.out.println("  technical domain PROJECT:");
			System.out.println("    " + myDoc);								
		}
		myDoc = (IngridDocument) o.get(MdekKeys.TECHNICAL_DOMAIN_DATASET);
		if (myDoc != null) {
			System.out.println("  technical domain DATASET:");
			System.out.println("    " + myDoc);								
		}
		docList = (List<IngridDocument>) o.get(MdekKeys.COMMENT_LIST);
		if (docList != null && docList.size() > 0) {
			System.out.println("  Object comments: " + docList.size() + " entries");
			for (IngridDocument doc : docList) {
				System.out.println("   " + doc);
				System.out.println("    created by user: " + doc.get(MdekKeys.CREATE_USER));
			}
		}
		docList = (List<IngridDocument>) o.get(MdekKeys.ADDITIONAL_FIELDS);
		if (docList != null && docList.size() > 0) {
			System.out.println("  Additional Fields: " + docList.size() + " entries");
			for (IngridDocument doc : docList) {
				System.out.println("   " + doc);								
			}			
		}
		docList = (List<IngridDocument>) o.get(MdekKeys.CONFORMITY_LIST);
		if (docList != null && docList.size() > 0) {
			System.out.println("  Object conformity (INSPIRE): " + docList.size() + " entries");
			for (IngridDocument doc : docList) {
				System.out.println("   " + doc);
			}
		}
		docList = (List<IngridDocument>) o.get(MdekKeys.ACCESS_LIST);
		if (docList != null && docList.size() > 0) {
			System.out.println("  Object access (INSPIRE): " + docList.size() + " entries");
			for (IngridDocument doc : docList) {
				System.out.println("   " + doc);
			}
		}

		myDoc = (IngridDocument) o.get(MdekKeys.PARENT_INFO);
		if (myDoc != null) {
			System.out.println("  parent info:");
			System.out.println("    " + myDoc);								
		}
	}

	public void debugAddressDoc(IngridDocument a) {
		System.out.println("Address: " + a.get(MdekKeys.ID) 
			+ ", " + a.get(MdekKeys.UUID)
			+ ", marked deleted: " + a.get(MdekKeys.MARK_DELETED)
			+ ", organisation: " + a.get(MdekKeys.ORGANISATION)
			+ ", name: " + a.get(MdekKeys.TITLE_OR_FUNCTION)
			+ " " + a.get(MdekKeys.TITLE_OR_FUNCTION_KEY)			
			+ " " + a.get(MdekKeys.GIVEN_NAME)
			+ " " + a.get(MdekKeys.NAME)
			+ ", class: " + EnumUtil.mapDatabaseToEnumConst(AddressType.class, a.get(MdekKeys.CLASS))
		);
		System.out.println("         "
			+ ", status: " + EnumUtil.mapDatabaseToEnumConst(WorkState.class, a.get(MdekKeys.WORK_STATE))
			+ ", modUser: " + extractUserData((IngridDocument)a.get(MdekKeys.MOD_USER))
			+ ", respUser: " + extractUserData((IngridDocument)a.get(MdekKeys.RESPONSIBLE_USER))
			+ ", assignerUser: " + extractUserData((IngridDocument)a.get(MdekKeys.ASSIGNER_USER))
			+ ", modified: " + MdekUtils.timestampToDisplayDate((String)a.get(MdekKeys.DATE_OF_LAST_MODIFICATION))
			+ ", created: " + MdekUtils.timestampToDisplayDate((String)a.get(MdekKeys.DATE_OF_CREATION))
		);

		System.out.println("  " + a);

		if (!doFullOutput) {
			return;
		}

		debugPermissionsDoc(a, "  ");

		IngridDocument myDoc;
		List<IngridDocument> docList;
		List<String> strList;

		docList = (List<IngridDocument>) a.get(MdekKeys.COMMUNICATION);
		if (docList != null && docList.size() > 0) {
			System.out.println("  Communication: " + docList.size() + " Entities");
			for (IngridDocument doc : docList) {
				System.out.println("    " + doc);								
			}			
		}

		// objects referencing the address !
		Integer objsFromStartIndex = (Integer) a.get(MdekKeys.OBJ_REFERENCES_FROM_START_INDEX);
		if (objsFromStartIndex != null) {
			Integer objsFromTotalNum = (Integer) a.get(MdekKeys.OBJ_REFERENCES_FROM_TOTAL_NUM);
			System.out.println("  Objects FROM (Querverweise): PAGING RESULT ! startIndex=" + objsFromStartIndex +
					", totalNum=" + objsFromTotalNum);
		}
		docList = (List<IngridDocument>) a.get(MdekKeys.OBJ_REFERENCES_FROM);
		if (docList != null && docList.size() > 0) {
			System.out.println("  Objects FROM (Querverweise): " + docList.size() + " Entities");
			for (IngridDocument doc : docList) {
				System.out.println("   " + doc);								
			}			
		}
		docList = (List<IngridDocument>) a.get(MdekKeys.OBJ_REFERENCES_FROM_PUBLISHED_ONLY);
		if (docList != null && docList.size() > 0) {
			System.out.println("  Objects FROM (Querverweise) ONLY PUBLISHED !!!: " + docList.size() + " Entities");
			for (IngridDocument doc : docList) {
				System.out.println("   " + doc);								
			}			
		}
		docList = (List<IngridDocument>) a.get(MdekKeys.SUBJECT_TERMS);
		if (docList != null && docList.size() > 0) {
			System.out.println("  Subject terms (Searchterms): " + docList.size() + " entries");
			for (IngridDocument doc : docList) {
				System.out.println("   " + doc);								
			}			
		}
		docList = (List<IngridDocument>) a.get(MdekKeys.SUBJECT_TERMS_INSPIRE);
		if (docList != null && docList.size() > 0) {
			System.out.println("  INSPIRE Searchterms (Themen): " + docList.size() + " entries");
			for (IngridDocument doc : docList) {
				System.out.println("   " + doc);								
			}			
		}
		docList = (List<IngridDocument>) a.get(MdekKeys.COMMENT_LIST);
		if (docList != null && docList.size() > 0) {
			System.out.println("  Address comments: " + docList.size() + " entries");
			for (IngridDocument doc : docList) {
				System.out.println("   " + doc);
				System.out.println("    created by user: " + doc.get(MdekKeys.CREATE_USER));
			}
		}
		myDoc = (IngridDocument) a.get(MdekKeys.PARENT_INFO);
		if (myDoc != null) {
			System.out.println("  parent info:");
			System.out.println("    " + myDoc);								
		}
		strList = (List<String>) a.get(MdekKeys.PATH);
		if (strList != null && strList.size() > 0) {
			System.out.println("  Path: " + strList.size() + " entries");
			System.out.println("   " + strList);
		}
		docList = (List<IngridDocument>) a.get(MdekKeys.PATH_ORGANISATIONS);
		if (docList != null && docList.size() > 0) {
			System.out.println("  Path Organisations: " + docList.size() + " entries");
			for (IngridDocument doc : docList) {
				System.out.println("   " + doc);
			}			
		}
	}

	public void debugJobInfoDoc(IngridDocument jobInfoDoc) {
		System.out.println(jobInfoDoc);

		Exception jobExc = MdekCaller.getExceptionFromJobInfo(jobInfoDoc);
		if (jobExc != null) {
			System.out.println("JobInfo Exception !:" + jobExc);
			printThrowable(jobExc);
		}
	}

	public void handleError(IngridDocument response) {
		System.out.println("MDEK ERRORS: " + mdekClientCaller.getErrorsFromResponse(response));			
		System.out.println("ERROR MESSAGE: " + mdekClientCaller.getErrorMsgFromResponse(response));			

		if (!doFullOutput) {
			return;
		}

		// detailed output  
		List<MdekError> errors = mdekClientCaller.getErrorsFromResponse(response);
		if (errors == null) {
			errors = new ArrayList<MdekError>();
		}

		doFullOutput = false;
		for (MdekError err : errors) {
			IngridDocument info = err.getErrorInfo();

			if (err.getErrorType().equals(MdekErrorType.ENTITY_REFERENCED_BY_OBJ)) {
				// referenced entity (object or address)
				if (info.get(MdekKeys.TITLE) != null) {
					System.out.println("    referenced Object:");
					debugObjectDoc(info);
				} else {
					System.out.println("    referenced Address:");
					debugAddressDoc(info);
				}
				// objects referencing
				List<IngridDocument> oDocs = (List<IngridDocument>) info.get(MdekKeys.OBJ_ENTITIES);
				if (oDocs != null) {
					System.out.println("    Referencing objects: " + oDocs.size() + " objects!");
					for (IngridDocument oDoc : oDocs) {
						debugObjectDoc(oDoc);
					}
				}

			} else if (err.getErrorType().equals(MdekErrorType.ADDRESS_IS_AUSKUNFT)) {
				// objects referencing address as auskunft
				List<IngridDocument> oDocs = (List<IngridDocument>) info.get(MdekKeys.OBJ_ENTITIES);
				if (oDocs != null) {
					System.out.println("    Referencing objects: " + oDocs.size() + " objects!");
					for (IngridDocument oDoc : oDocs) {
						debugObjectDoc(oDoc);
					}
				}

			} else if (err.getErrorType().equals(MdekErrorType.GROUP_HAS_USERS)) {
				debugIdcUsersDoc(info);
			} else if (err.getErrorType().equals(MdekErrorType.USER_EDITING_OBJECT_PERMISSION_MISSING)) {
				System.out.println("    Editing User: " + info.get(MdekKeysSecurity.USER_ADDRESSES));
				System.out.println("    Edited Object: " + info.get(MdekKeys.OBJ_ENTITIES));
			} else if (err.getErrorType().equals(MdekErrorType.USER_EDITING_ADDRESS_PERMISSION_MISSING)) {
				System.out.println("    Editing User: " + info.get(MdekKeysSecurity.USER_ADDRESSES));
				System.out.println("    Edited Address: " + info.get(MdekKeys.ADR_ENTITIES));
			} else if (err.getErrorType().equals(MdekErrorType.USER_RESPONSIBLE_FOR_OBJECT_PERMISSION_MISSING)) {
				System.out.println("    Responsible User: " + info.get(MdekKeysSecurity.USER_ADDRESSES));
				System.out.println("    for Object: " + info.get(MdekKeys.OBJ_ENTITIES));
			} else if (err.getErrorType().equals(MdekErrorType.USER_RESPONSIBLE_FOR_ADDRESS_PERMISSION_MISSING)) {
				System.out.println("    Responsible User: " + info.get(MdekKeysSecurity.USER_ADDRESSES));
				System.out.println("    for Address: " + info.get(MdekKeys.ADR_ENTITIES));
			} else if (err.getErrorType().equals(MdekErrorType.MULTIPLE_PERMISSIONS_ON_OBJECT)) {
				System.out.println("    Object with multiple Permissions: " + info.get(MdekKeys.OBJ_ENTITIES));
			} else if (err.getErrorType().equals(MdekErrorType.MULTIPLE_PERMISSIONS_ON_ADDRESS)) {
				System.out.println("    Address with multiple Permissions: " + info.get(MdekKeys.ADR_ENTITIES));
			} else if (err.getErrorType().equals(MdekErrorType.TREE_BELOW_TREE_OBJECT_PERMISSION)) {
				List<IngridDocument> objs = (List<IngridDocument>) info.get(MdekKeys.OBJ_ENTITIES);
				System.out.println("    Parent Object with TREE Permission: " + objs.get(0));
				System.out.println("    Sub Object with TREE Permission: " + objs.get(1));
			} else if (err.getErrorType().equals(MdekErrorType.TREE_BELOW_TREE_ADDRESS_PERMISSION)) {
				List<IngridDocument> addrs = (List<IngridDocument>) info.get(MdekKeys.ADR_ENTITIES);
				System.out.println("    Parent Address with TREE Permission: " + addrs.get(0));
				System.out.println("    Sub Address with TREE Permission: " + addrs.get(1));
			} else if (err.getErrorType().equals(MdekErrorType.SINGLE_BELOW_TREE_OBJECT_PERMISSION)) {
				List<IngridDocument> objs = (List<IngridDocument>) info.get(MdekKeys.OBJ_ENTITIES);
				System.out.println("    Parent Object with TREE Permission: " + objs.get(0));
				System.out.println("    Sub Object with SINGLE Permission: " + objs.get(1));
			} else if (err.getErrorType().equals(MdekErrorType.SINGLE_BELOW_TREE_ADDRESS_PERMISSION)) {
				List<IngridDocument> addrs = (List<IngridDocument>) info.get(MdekKeys.ADR_ENTITIES);
				System.out.println("    Parent Address with TREE Permission: " + addrs.get(0));
				System.out.println("    Sub Address with SINGLE Permission: " + addrs.get(1));
			} else if (err.getErrorType().equals(MdekErrorType.NO_RIGHT_TO_REMOVE_OBJECT_PERMISSION)) {
				System.out.println("    No right to remove object: " + info.get(MdekKeys.OBJ_ENTITIES));
			} else if (err.getErrorType().equals(MdekErrorType.NO_RIGHT_TO_REMOVE_ADDRESS_PERMISSION)) {
				System.out.println("    No right to remove address: " + info.get(MdekKeys.ADR_ENTITIES));
			} else if (err.getErrorType().equals(MdekErrorType.NO_RIGHT_TO_ADD_OBJECT_PERMISSION)) {
				System.out.println("    No right to add object: " + info.get(MdekKeys.OBJ_ENTITIES));
			} else if (err.getErrorType().equals(MdekErrorType.NO_RIGHT_TO_ADD_ADDRESS_PERMISSION)) {
				System.out.println("    No right to add address: " + info.get(MdekKeys.ADR_ENTITIES));
			}
		}
		doFullOutput = true;
	}

	public void printThrowable(Throwable t) {
		System.out.println(t);
		System.out.println("   Stack Trace:");
		StackTraceElement[] st = t.getStackTrace();
		for (StackTraceElement stackTraceElement : st) {
	        System.out.println(stackTraceElement);
        }
		Throwable cause = t.getCause();
		if (cause != null) {
			System.out.println("   Cause:");
			printThrowable(cause);			
		}
	}

	public void addUserPermissionToGroupDoc(IngridDocument groupDoc, IdcPermission idcPerm) {
		List<IngridDocument> perms = (List<IngridDocument>) groupDoc.get(MdekKeysSecurity.IDC_USER_PERMISSIONS);
		if (perms == null) {
			perms = new ArrayList<IngridDocument>();
			groupDoc.put(MdekKeysSecurity.IDC_USER_PERMISSIONS, perms);
		}
		// check whether permission already present !
		boolean addPerm = true;
		for (IngridDocument perm : perms) {
			if (idcPerm.getDbValue().equals(perm.getString(MdekKeysSecurity.IDC_PERMISSION))) {
				addPerm = false;
				break;
			}
		}
		
		if (addPerm) {
			IngridDocument newPerm = new IngridDocument();
			newPerm.put(MdekKeysSecurity.IDC_PERMISSION, idcPerm.getDbValue());
			perms.add(newPerm);			
		}
	}

	public void addObjPermissionToGroupDoc(IngridDocument groupDoc, String objUuid, IdcPermission idcPerm) {
		List<IngridDocument> perms = (List<IngridDocument>) groupDoc.get(MdekKeysSecurity.IDC_OBJECT_PERMISSIONS);
		if (perms == null) {
			perms = new ArrayList<IngridDocument>();
			groupDoc.put(MdekKeysSecurity.IDC_OBJECT_PERMISSIONS, perms);
		}
		IngridDocument newPerm = new IngridDocument();
		newPerm.put(MdekKeys.UUID, objUuid);
		newPerm.put(MdekKeysSecurity.IDC_PERMISSION, idcPerm.getDbValue());
		perms.add(newPerm);
	}

	public void addAddrPermissionToGroupDoc(IngridDocument groupDoc, String addrUuid, IdcPermission idcPerm) {
		List<IngridDocument> perms = (List<IngridDocument>) groupDoc.get(MdekKeysSecurity.IDC_ADDRESS_PERMISSIONS);
		if (perms == null) {
			perms = new ArrayList<IngridDocument>();
			groupDoc.put(MdekKeysSecurity.IDC_ADDRESS_PERMISSIONS, perms);
		}
		IngridDocument newPerm = new IngridDocument();
		newPerm.put(MdekKeys.UUID, addrUuid);
		newPerm.put(MdekKeysSecurity.IDC_PERMISSION, idcPerm.getDbValue());
		perms.add(newPerm);
	}

	public void addComment(IngridDocument entityDoc, String comment) {
		List<IngridDocument> docList = (List<IngridDocument>) entityDoc.get(MdekKeys.COMMENT_LIST);
		docList = (docList == null) ? new ArrayList<IngridDocument>() : docList;
		IngridDocument tmpDoc = new IngridDocument();
		tmpDoc.put(MdekKeys.COMMENT, comment);
		tmpDoc.put(MdekKeys.CREATE_TIME, MdekUtils.dateToTimestamp(new Date()));
		IngridDocument createUserDoc = new IngridDocument();
		createUserDoc.put(MdekKeys.UUID, getCallingUserUuid());
		tmpDoc.put(MdekKeys.CREATE_USER, createUserDoc);
		docList.add(tmpDoc);
		entityDoc.put(MdekKeys.COMMENT_LIST, docList);
	}
	
	public void setResponsibleUser(IngridDocument entityDoc, String userUuid) {
		IngridDocument userDoc = (IngridDocument) entityDoc.get(MdekKeys.RESPONSIBLE_USER);
		if (userDoc == null) {
			userDoc = new IngridDocument();
			entityDoc.put(MdekKeys.RESPONSIBLE_USER, userDoc);
		}
		userDoc.put(MdekKeys.UUID, userUuid);
	}
	
	/** Creates default new Object Document including required default data */
	public IngridDocument newObjectDoc(String parentObjUuid) {
		IngridDocument newDoc = new IngridDocument();
		newDoc.put(MdekKeys.PARENT_UUID, parentObjUuid);
		newDoc = getInitialObject(newDoc);
		newDoc.put(MdekKeys.TITLE, "TEST NEUES OBJEKT");
		newDoc.put(MdekKeys.PUBLICATION_CONDITION, MdekUtils.PublishType.INTERNET.getDbValue());

		return newDoc;
	}

	/** Creates default new Address Document including required default data */
	public IngridDocument newAddressDoc(String parentAddrUuid, AddressType whichType) {
		IngridDocument newDoc = new IngridDocument();
		newDoc.put(MdekKeys.PARENT_UUID, parentAddrUuid);
		newDoc = getInitialAddress(newDoc);

		newDoc.put(MdekKeys.NAME, "testNAME");
		newDoc.put(MdekKeys.GIVEN_NAME, "testGIVEN_NAME");
		newDoc.put(MdekKeys.ORGANISATION, "testORGANISATION");
		newDoc.put(MdekKeys.CLASS, whichType.getDbValue());
		// email has to exist !
		List<IngridDocument> docList = (List<IngridDocument>) newDoc.get(MdekKeys.COMMUNICATION);
		docList = (docList == null) ? new ArrayList<IngridDocument>() : docList;
		IngridDocument testDoc = new IngridDocument();
		testDoc.put(MdekKeys.COMMUNICATION_MEDIUM_KEY, MdekUtils.COMM_TYPE_EMAIL);
		testDoc.put(MdekKeys.COMMUNICATION_VALUE, "example@example");
		testDoc.put(MdekKeys.COMMUNICATION_DESCRIPTION, "TEST COMMUNICATION_DESCRIPTION");
		docList.add(testDoc);
		newDoc.put(MdekKeys.COMMUNICATION, docList);
		
		return newDoc;
	}
}
