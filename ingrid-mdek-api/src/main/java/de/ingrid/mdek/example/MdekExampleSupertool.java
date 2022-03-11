/*
 * **************************************************-
 * ingrid-mdek-api
 * ==================================================
 * Copyright (C) 2014 - 2022 wemove digital solutions GmbH
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
package de.ingrid.mdek.example;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import de.ingrid.mdek.EnumUtil;
import de.ingrid.mdek.MdekError;
import de.ingrid.mdek.MdekError.MdekErrorType;
import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.MdekKeysSecurity;
import de.ingrid.mdek.MdekUtils;
import de.ingrid.mdek.MdekUtils.AddressType;
import de.ingrid.mdek.MdekUtils.CsvRequestType;
import de.ingrid.mdek.MdekUtils.IdcEntityOrderBy;
import de.ingrid.mdek.MdekUtils.IdcEntityVersion;
import de.ingrid.mdek.MdekUtils.IdcQAEntitiesSelectionType;
import de.ingrid.mdek.MdekUtils.IdcStatisticsSelectionType;
import de.ingrid.mdek.MdekUtils.IdcWorkEntitiesSelectionType;
import de.ingrid.mdek.MdekUtils.MdekSysList;
import de.ingrid.mdek.MdekUtils.ObjectType;
import de.ingrid.mdek.MdekUtils.PublishType;
import de.ingrid.mdek.MdekUtils.SearchtermType;
import de.ingrid.mdek.MdekUtils.SpatialReferenceType;
import de.ingrid.mdek.MdekUtils.WorkState;
import de.ingrid.mdek.MdekUtilsSecurity;
import de.ingrid.mdek.MdekUtilsSecurity.IdcPermission;
import de.ingrid.mdek.caller.IMdekCaller.AddressArea;
import de.ingrid.mdek.caller.IMdekCaller.FetchQuantity;
import de.ingrid.mdek.caller.IMdekClientCaller;
import de.ingrid.mdek.caller.MdekCaller;
import de.ingrid.mdek.caller.MdekClientCaller;
import de.ingrid.mdek.job.IJob.JobType;
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

	public MdekExampleSupertool(String callingUserUuid)
	{
		this.plugId = "/ingrid-group:ige-iplug-test";
		myUserUuid = callingUserUuid;

		mdekClientCaller = MdekClientCaller.getInstance();
		
		supertoolCatalog = new MdekExampleSupertoolCatalog(plugId, callingUserUuid, this);
		supertoolSecurity = new MdekExampleSupertoolSecurity(plugId, callingUserUuid, this);
		supertoolObject = new MdekExampleSupertoolObject(plugId, callingUserUuid, this);
		supertoolAddress = new MdekExampleSupertoolAddress(plugId, callingUserUuid, this);
		supertoolQuery = new MdekExampleSupertoolQuery(plugId, callingUserUuid, this);
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
	public IngridDocument getSysGenericKeys(String[] keyNames) {
		return supertoolCatalog.getSysGenericKeys(keyNames);
	}
	public IngridDocument storeSysGenericKeys(String[] keyNames, String[] keyValues) {
		return supertoolCatalog.storeSysGenericKeys(keyNames, keyValues);
	}
	public IngridDocument getSysLists(Integer[] listIds, String language) {
		return supertoolCatalog.getSysLists(listIds, language);
	}
	public IngridDocument storeSysList(int listId, boolean maintainable, Integer defaultEntryIndex,
			Integer[] entryIds, String[] entryNames_de, String[] entryNames_en, String[] data) {
		return supertoolCatalog.storeSysList(listId, maintainable, defaultEntryIndex,
				entryIds, entryNames_de, entryNames_en, data);
	}
	public IngridDocument storeSysLists(List<IngridDocument> listDocs) {
		return supertoolCatalog.storeSysLists(listDocs);
	}
	public IngridDocument exportObjectBranch(String rootUuid, boolean exportOnlyRoot, boolean includeWorkingCopies) {
		return supertoolCatalog.exportObjectBranch(rootUuid, exportOnlyRoot, includeWorkingCopies);
	}
	public IngridDocument exportObjects(String exportCriteria, boolean includeWorkingCopies) {
		return supertoolCatalog.exportObjects(exportCriteria, includeWorkingCopies);
	}
	public IngridDocument exportAddressBranch(String rootUuid,
			boolean exportOnlyRoot, AddressArea addressArea, boolean includeWorkingCopies) {
		return supertoolCatalog.exportAddressBranch(rootUuid, exportOnlyRoot, addressArea, includeWorkingCopies);
	}
	public IngridDocument getExportInfo(boolean includeExportData) {
		return supertoolCatalog.getExportInfo(includeExportData);
	}
	public IngridDocument importEntities(byte[] importData,
			String objectImportNodeUuid, String addressImportNodeUuid,
			boolean publishImmediately,
			boolean doSeparateImport, boolean copyNodeIfPresent) {
		List<byte[]> importList = new ArrayList<byte[]>();
		importList.add(importData);
		return importEntities(importList,
			objectImportNodeUuid, addressImportNodeUuid,
			publishImmediately,
			doSeparateImport, copyNodeIfPresent,
			null);
	}
	public IngridDocument importEntities(List<byte[]> importData,
			String objectImportNodeUuid, String addressImportNodeUuid,
			boolean publishImmediately,
			boolean doSeparateImport,
			boolean copyNodeIfPresent,
			String frontendProtocol) {
		return supertoolCatalog.importEntities(importData,
				objectImportNodeUuid, addressImportNodeUuid,
				publishImmediately,
				doSeparateImport, copyNodeIfPresent,
				frontendProtocol);
	}
	public IngridDocument getJobInfo(JobType jobType) {
		return supertoolCatalog.getJobInfo(jobType);
	}
	public IngridDocument analyze() {
		return supertoolCatalog.analyze();
	}
	public IngridDocument getObjectsOfAddressByType(String addressUuid, Integer referenceTypeId, Integer maxNum) {
		return supertoolCatalog.getObjectsOfAddressbyType(addressUuid, referenceTypeId, maxNum);
	}
	public IngridDocument getObjectsOfResponsibleUser(String responsibleUserUuid, Integer maxNum) {
		return supertoolCatalog.getObjectsOfResponsibleUser(responsibleUserUuid, maxNum);
	}
	public IngridDocument getAddressesOfResponsibleUser(String responsibleUserUuid, Integer maxNum) {
		return supertoolCatalog.getAddressesOfResponsibleUser(responsibleUserUuid, maxNum);
	}
	public IngridDocument replaceAddress(String oldUuid, String newUuid) {
		return supertoolCatalog.replaceAddress(oldUuid, newUuid);
	}
	public void getCsvData(CsvRequestType csvType, String uuid) {
		supertoolCatalog.getCsvData(csvType, uuid);
	}
	public IngridDocument getFreeListEntries(MdekSysList sysLst) {
		return supertoolCatalog.getFreeListEntries(sysLst);
	}
	public IngridDocument replaceFreeEntryWithSyslistEntry(String freeEntry,
			MdekSysList sysLst, int sysLstEntryId, String sysLstEntryName) {
		return supertoolCatalog.replaceFreeEntryWithSyslistEntry(freeEntry,
				sysLst, sysLstEntryId, sysLstEntryName);
	}
	public IngridDocument rebuildSyslistData() {
		return supertoolCatalog.rebuildSyslistData();
	}
	public IngridDocument getSearchTerms(SearchtermType[] termTypes) {
		return supertoolCatalog.getSearchTerms(termTypes);
	}
	public IngridDocument getSpatialReferences(SpatialReferenceType[] refTypes) {
		return supertoolCatalog.getSpatialReferences(refTypes);
	}
	public IngridDocument updateSearchTerms(List<IngridDocument> oldTerms,
			List<IngridDocument> newTerms) {
		return supertoolCatalog.updateSearchTerms(oldTerms, newTerms);
	}
	public IngridDocument updateSpatialReferences(List<IngridDocument> oldSpatialRefs,
			List<IngridDocument> newSpatialRefs) {
		return supertoolCatalog.updateSpatialReferences(oldSpatialRefs, newSpatialRefs);
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
	public IngridDocument getResponsibleUsersForNewObject(String objUuid,
			boolean checkWorkflow,
			boolean getDetailedPermissions) {
		return supertoolSecurity.getResponsibleUsersForNewObject(objUuid, checkWorkflow, getDetailedPermissions);
	}
	public IngridDocument getResponsibleUsersForNewAddress(String addrUuid,
			boolean checkWorkflow,
			boolean getDetailedPermissions) {
		return supertoolSecurity.getResponsibleUsersForNewAddress(addrUuid, checkWorkflow, getDetailedPermissions);
	}
	public IngridDocument getUsersWithPermissionForObject(String objUuid,
			boolean checkWorkflow,
			boolean getDetailedPermissions) {
		return supertoolSecurity.getUsersWithPermissionForObject(objUuid, checkWorkflow, getDetailedPermissions);
	}
	public IngridDocument getUsersWithPermissionForAddress(String addrUuid,
			boolean checkWorkflow,
			boolean getDetailedPermissions) {
		return supertoolSecurity.getUsersWithPermissionForAddress(addrUuid, checkWorkflow, getDetailedPermissions);
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
	public IngridDocument getUserDetails() {
		return supertoolSecurity.getUserDetails();
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
			boolean refetchAddress, boolean forcePublicationCondition) {
		return publishAddress(aDocIn, refetchAddress, forcePublicationCondition, 0, 50);
	}
	public IngridDocument publishAddress(IngridDocument aDocIn,
			boolean withRefetch,
			boolean forcePublicationCondition,
			int objRefsStartIndex, int objRefsMaxNum) {
		return supertoolAddress.publishAddress(aDocIn, withRefetch, forcePublicationCondition,
				objRefsStartIndex, objRefsMaxNum);
	}
	public IngridDocument moveAddress(String fromUuid, String toUuid,
			boolean moveToFreeAddress,
			boolean forcePublicationCondition) {
		return supertoolAddress.moveAddress(fromUuid, toUuid, moveToFreeAddress, forcePublicationCondition);
	}
	public IngridDocument copyAddress(String fromUuid, String toUuid,
			boolean copySubtree, boolean copyToFreeAddress) {
		return supertoolAddress.copyAddress(fromUuid, toUuid,
				copySubtree, copyToFreeAddress);
	}
	public IngridDocument mergeAddressToSubAddresses(String parentUuid) {
		return supertoolAddress.mergeAddressToSubAddresses(parentUuid);
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
				Integer numEntities = (Integer) result.get(MdekKeys.RUNNINGJOB_NUMBER_PROCESSED_ENTITIES);
				Integer totalEntities = (Integer) result.get(MdekKeys.RUNNINGJOB_NUMBER_TOTAL_ENTITIES);
				Integer numObjs = (Integer) result.get(MdekKeys.RUNNINGJOB_NUMBER_PROCESSED_OBJECTS);
				Integer totalObjs = (Integer) result.get(MdekKeys.RUNNINGJOB_NUMBER_TOTAL_OBJECTS);
				Integer numAddresses = (Integer) result.get(MdekKeys.RUNNINGJOB_NUMBER_PROCESSED_ADDRESSES);
				Integer totalAddresses = (Integer) result.get(MdekKeys.RUNNINGJOB_NUMBER_TOTAL_ADDRESSES);
				if (jobDescr == null) {
					// job finished !
					jobIsRunning = false;					
					System.out.println("JOB FINISHED\n");
				} else {
					System.out.println("job: " + jobDescr + ", entities:" + numEntities + ", total:" + totalEntities +
						", objects:" + numObjs + ", total:" + totalObjs + ", addresses:" + numAddresses+ ", total:" + totalAddresses);
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
		System.out.println("Catalog: " + c.get(MdekKeys.CATALOG_NAME) 
			+ ", namespace: " + c.get(MdekKeys.CATALOG_NAMESPACE)
			+ ", partner: " + c.get(MdekKeys.PARTNER_NAME)
			+ ", provider: " + c.get(MdekKeys.PROVIDER_NAME)
			+ ", country: " + c.get(MdekKeys.COUNTRY_CODE) + "/" + c.get(MdekKeys.COUNTRY_NAME)
			+ ", language: " + c.get(MdekKeys.LANGUAGE_CODE) + "/" + c.get(MdekKeys.LANGUAGE_NAME)
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
		System.out.println("Group: " + g.get(MdekKeysSecurity.ID) 
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
	
	public void debugObjectDoc(IngridDocument oDoc) {
		System.out.println("Object: " + oDoc.get(MdekKeys.ID) 
			+ ", " + oDoc.get(MdekKeys.UUID)
			+ ", class: " + EnumUtil.mapDatabaseToEnumConst(ObjectType.class, oDoc.get(MdekKeys.CLASS))
			+ ", " + oDoc.get(MdekKeys.TITLE)
			+ ", marked deleted: " + oDoc.get(MdekKeys.MARK_DELETED)
		);
		System.out.println("        "
			+ ", status: " + EnumUtil.mapDatabaseToEnumConst(WorkState.class, oDoc.get(MdekKeys.WORK_STATE))
			+ ", publication condition: " + EnumUtil.mapDatabaseToEnumConst(PublishType.class, oDoc.get(MdekKeys.PUBLICATION_CONDITION))
			+ ", modUser: " + extractUserData((IngridDocument)oDoc.get(MdekKeys.MOD_USER))
			+ ", respUser: " + extractUserData((IngridDocument)oDoc.get(MdekKeys.RESPONSIBLE_USER))
			+ ", assignerUser: " + extractUserData((IngridDocument)oDoc.get(MdekKeys.ASSIGNER_USER))
			+ ", modified: " + MdekUtils.timestampToDisplayDate((String)oDoc.get(MdekKeys.DATE_OF_LAST_MODIFICATION))
			+ ", created: " + MdekUtils.timestampToDisplayDate((String)oDoc.get(MdekKeys.DATE_OF_CREATION))
//			+ ", cat_id: " + o.get(MdekKeys.CATALOGUE_IDENTIFIER)
		);

		System.out.println("  " + oDoc);

		if (!doFullOutput) {
			return;
		}

		System.out.println("  IS_INSPIRE_RELEVANT: " + oDoc.get(MdekKeys.IS_INSPIRE_RELEVANT));
		System.out.println("  IS_OPEN_DATA: " + oDoc.get(MdekKeys.IS_OPEN_DATA));
		System.out.println("  VERTICAL_EXTENT_VDATUM_KEY/VALUE: " + oDoc.get(MdekKeys.VERTICAL_EXTENT_VDATUM_KEY) +  "/" +
				oDoc.get(MdekKeys.VERTICAL_EXTENT_VDATUM_VALUE));

		debugPermissionsDoc(oDoc, "  ");

		IngridDocument myDoc;
		List<IngridDocument> docList = (List<IngridDocument>) oDoc.get(MdekKeys.OBJ_REFERENCES_TO);
		if (docList != null && docList.size() > 0) {
			System.out.println("  Objects TO (Querverweise): " + docList.size() + " Entities");
			for (IngridDocument doc : docList) {
				System.out.println("   " + doc.get(MdekKeys.UUID) + ": " + doc);								
			}			
		}
		docList = (List<IngridDocument>) oDoc.get(MdekKeys.OBJ_REFERENCES_FROM);
		if (docList != null && docList.size() > 0) {
			System.out.println("  Objects FROM (Querverweise): " + docList.size() + " Entities");
			for (IngridDocument doc : docList) {
				System.out.println("   " + doc.get(MdekKeys.UUID) + ": " + doc);								
			}			
		}
		docList = (List<IngridDocument>) oDoc.get(MdekKeys.OBJ_REFERENCES_FROM_PUBLISHED_ONLY);
		if (docList != null && docList.size() > 0) {
			System.out.println("  Objects FROM (Querverweise) ONLY PUBLISHED !!!: " + docList.size() + " Entities");
			for (IngridDocument doc : docList) {
				System.out.println("   " + doc.get(MdekKeys.UUID) + ": " + doc);								
			}			
		}
		docList = (List<IngridDocument>) oDoc.get(MdekKeys.ADR_REFERENCES_TO);
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
		docList = (List<IngridDocument>) oDoc.get(MdekKeys.LOCATIONS);
		if (docList != null && docList.size() > 0) {
			System.out.println("  Locations (Spatial References): " + docList.size() + " entries");
			for (IngridDocument doc : docList) {
				System.out.println("   " + doc);								
			}			
		}
		docList = (List<IngridDocument>) oDoc.get(MdekKeys.SUBJECT_TERMS);
		if (docList != null && docList.size() > 0) {
			System.out.println("  Subject terms (Searchterms): " + docList.size() + " entries");
			for (IngridDocument doc : docList) {
				System.out.println("   " + doc);								
			}			
		}
		docList = (List<IngridDocument>) oDoc.get(MdekKeys.SUBJECT_TERMS_INSPIRE);
		if (docList != null && docList.size() > 0) {
			System.out.println("  INSPIRE Searchterms (Themen): " + docList.size() + " entries");
			for (IngridDocument doc : docList) {
				System.out.println("   " + doc);								
			}			
		}
		docList = (List<IngridDocument>) oDoc.get(MdekKeys.LINKAGES);
		if (docList != null && docList.size() > 0) {
			System.out.println("  URL References: " + docList.size() + " entries");
			for (IngridDocument doc : docList) {
				System.out.println("   " + doc);								
			}			
		}
		docList = (List<IngridDocument>) oDoc.get(MdekKeys.DATASET_REFERENCES);
		if (docList != null && docList.size() > 0) {
			System.out.println("  Dataset References: " + docList.size() + " entries");
			for (IngridDocument doc : docList) {
				System.out.println("   " + doc);								
			}			
		}
		List<String> strList = (List<String>) oDoc.get(MdekKeys.EXPORT_CRITERIA);
		if (strList != null && strList.size() > 0) {
			System.out.println("  Exports: " + strList.size() + " entries");
			System.out.println("   " + strList);
		}
		strList = (List<String>) oDoc.get(MdekKeys.LEGISLATIONS);
		if (strList != null && strList.size() > 0) {
			System.out.println("  Legislations: " + strList.size() + " entries");
			System.out.println("   " + strList);
		}
		docList = (List<IngridDocument>) oDoc.get(MdekKeys.DATA_FORMATS);
		if (docList != null && docList.size() > 0) {
			System.out.println("  Data Formats: " + docList.size() + " entries");
			for (IngridDocument doc : docList) {
				System.out.println("   " + doc);								
			}			
		}
		docList = (List<IngridDocument>) oDoc.get(MdekKeys.MEDIUM_OPTIONS);
		if (docList != null && docList.size() > 0) {
			System.out.println("  Medium Options: " + docList.size() + " entries");
			for (IngridDocument doc : docList) {
				System.out.println("   " + doc);								
			}			
		}
		strList = (List<String>) oDoc.get(MdekKeys.ENV_TOPICS);
		if (strList != null && strList.size() > 0) {
			System.out.println("  Env Topics: " + strList.size() + " entries");
			System.out.println("   " + strList);
		}
		List<Integer> intList = (List<Integer>) oDoc.get(MdekKeys.TOPIC_CATEGORIES);
		if (intList != null && intList.size() > 0) {
			System.out.println("  Topic Categories: " + intList.size() + " entries");
			System.out.println("   " + intList);
		}
		docList = (List<IngridDocument>) oDoc.get(MdekKeys.OPEN_DATA_CATEGORY_LIST);
		if (docList != null && docList.size() > 0) {
			System.out.println("  Open Data Categories: " + docList.size() + " entries");
			for (IngridDocument doc : docList) {
				System.out.println("   " + doc);								
			}			
		}

		myDoc = (IngridDocument) oDoc.get(MdekKeys.TECHNICAL_DOMAIN_MAP);
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
				System.out.println("    MAP - publication scales (Erstellungsmaßstab): " + docList.size() + " entries");
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
		myDoc = (IngridDocument) oDoc.get(MdekKeys.TECHNICAL_DOMAIN_DOCUMENT);
		if (myDoc != null) {
			System.out.println("  technical domain DOCUMENT:");
			System.out.println("    " + myDoc);								
		}

		myDoc = (IngridDocument) oDoc.get(MdekKeys.TECHNICAL_DOMAIN_SERVICE);
		if (myDoc != null) {
			System.out.println("  technical domain SERVICE:");
			System.out.println("    " + myDoc);
			System.out.println("    SERVICE type: " + myDoc.get(MdekKeys.SERVICE_TYPE_KEY) + "=" + myDoc.get(MdekKeys.SERVICE_TYPE));
			System.out.println("    SERVICE HAS_ACCESS_CONSTRAINT: " + myDoc.get(MdekKeys.HAS_ACCESS_CONSTRAINT));
			System.out.println("    SERVICE HAS_ATOM_DOWNLOAD: " + myDoc.get(MdekKeys.HAS_ATOM_DOWNLOAD));
            docList = (List<IngridDocument>) myDoc.get(MdekKeys.SERVICE_VERSION_LIST);
            if (docList != null && docList.size() > 0) {
                System.out.println("      SERVICE - versions: " + docList.size() + " entries");
                for (IngridDocument doc : docList) {
                    System.out.println("        " + doc);
                }           
            }
			docList = (List<IngridDocument>) myDoc.get(MdekKeys.SERVICE_TYPE2_LIST);
			if (docList != null && docList.size() > 0) {
				System.out.println("    SERVICE - classifications (INSPIRE): " + docList.size() + " entries");
				for (IngridDocument doc : docList) {
					System.out.println("      " + doc);								
				}
			}
			docList = (List<IngridDocument>) myDoc.get(MdekKeys.PUBLICATION_SCALE_LIST);
			if (docList != null && docList.size() > 0) {
				System.out.println("    SERVICE - publication scales = Erstellungsmaßstab (INSPIRE): " + docList.size() + " entries");
				for (IngridDocument doc : docList) {
					System.out.println("      " + doc);								
				}
			}
			docList = (List<IngridDocument>) myDoc.get(MdekKeys.SERVICE_OPERATION_LIST);
			if (docList != null && docList.size() > 0) {
				System.out.println("    SERVICE - operations: " + docList.size() + " entries");
				for (IngridDocument doc : docList) {
					System.out.println("      " + doc);								
					List<IngridDocument> docList2 = (List<IngridDocument>) doc.get(MdekKeys.PLATFORM_LIST);
					if (docList2 != null && docList2.size() > 0) {
						System.out.println("      SERVICE - operation - platforms: " + docList2.size() + " entries");
						for (IngridDocument doc2 : docList2) {
							System.out.println("        " + doc2);
						}			
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
					docList2 = (List<IngridDocument>) doc.get(MdekKeys.PARAMETER_LIST);
					if (docList2 != null) {
						System.out.println("      SERVICE - operation - parameters: " + docList2.size() + " entries");
						for (IngridDocument doc2 : docList2) {
							System.out.println("        " + doc2);
						}			
					}
				}
			}
			docList = (List<IngridDocument>) myDoc.get(MdekKeys.URL_LIST);
			if (docList != null && docList.size() > 0) {
				System.out.println("    SERVICE (class 6 \"APPLICATION\") - Urls: " + docList.size() + " entries");
				for (IngridDocument doc : docList) {
					System.out.println("      " + doc);								
				}
			}
		}
		myDoc = (IngridDocument) oDoc.get(MdekKeys.TECHNICAL_DOMAIN_PROJECT);
		if (myDoc != null) {
			System.out.println("  technical domain PROJECT:");
			System.out.println("    " + myDoc);								
		}
		myDoc = (IngridDocument) oDoc.get(MdekKeys.TECHNICAL_DOMAIN_DATASET);
		if (myDoc != null) {
			System.out.println("  technical domain DATASET:");
			System.out.println("    " + myDoc);
			docList = (List<IngridDocument>) myDoc.get(MdekKeys.KEY_CATALOG_LIST);
			if (docList != null && docList.size() > 0) {
				System.out.println("    DATASET - key catalogs: " + docList.size() + " entries");
				for (IngridDocument doc : docList) {
					System.out.println("     " + doc);								
				}			
			}
		}
		docList = (List<IngridDocument>) oDoc.get(MdekKeys.COMMENT_LIST);
		if (docList != null && docList.size() > 0) {
			System.out.println("  Object comments: " + docList.size() + " entries");
			for (IngridDocument doc : docList) {
				System.out.println("   " + doc);
				System.out.println("    created by user: " + doc.get(MdekKeys.CREATE_USER));
			}
		}
		docList = (List<IngridDocument>) oDoc.get(MdekKeys.ADDITIONAL_FIELDS);
		if (docList != null && docList.size() > 0) {
			System.out.println("  Additional Fields: " + docList.size() + " entries");
			for (IngridDocument doc : docList) {
				System.out.println("   " + doc);								
			}			
		}
		docList = (List<IngridDocument>) oDoc.get(MdekKeys.CONFORMITY_LIST);
		if (docList != null && docList.size() > 0) {
			System.out.println("  Object conformity (INSPIRE): " + docList.size() + " entries");
			for (IngridDocument doc : docList) {
				System.out.println("   " + doc);
			}
		}
		docList = (List<IngridDocument>) oDoc.get(MdekKeys.ACCESS_LIST);
		if (docList != null && docList.size() > 0) {
			System.out.println("  Object access constraints (INSPIRE): " + docList.size() + " entries");
			for (IngridDocument doc : docList) {
				System.out.println("   " + doc);
			}
		}
		docList = (List<IngridDocument>) oDoc.get(MdekKeys.USE_LIST);
		if (docList != null && docList.size() > 0) {
			System.out.println("  Object useLimitation (INSPIRE): " + docList.size() + " entries");
			for (IngridDocument doc : docList) {
				System.out.println("   " + doc);
			}
		}
        docList = (List<IngridDocument>) oDoc.get(MdekKeys.USE_CONSTRAINTS);
        if (docList != null && docList.size() > 0) {
            System.out.println("  Object useConstraints (INSPIRE): " + docList.size() + " entries");
            for (IngridDocument doc : docList) {
                System.out.println("   " + doc);
            }
        }
		docList = (List<IngridDocument>) oDoc.get(MdekKeys.DATA_QUALITY_LIST);
		if (docList != null && docList.size() > 0) {
			System.out.println("  Object data quality (INSPIRE): " + docList.size() + " entries");
			for (IngridDocument doc : docList) {
				System.out.println("   " + doc);
			}
		}
		docList = (List<IngridDocument>) oDoc.get(MdekKeys.SPATIAL_SYSTEM_LIST);
		if (docList != null && docList.size() > 0) {
			System.out.println("  Object spatial system list: " + docList.size() + " entries");
			for (IngridDocument doc : docList) {
				System.out.println("   " + doc);
			}
		}
        docList = (List<IngridDocument>) oDoc.get(MdekKeys.DATA_LANGUAGE_LIST);
        if (docList != null && docList.size() > 0) {
            System.out.println("  Object data language list: " + docList.size() + " entries");
            for (IngridDocument doc : docList) {
                System.out.println("   " + doc);
            }
        }
		docList = (List<IngridDocument>) oDoc.get(MdekKeys.ADDITIONAL_FIELDS);
		if (docList != null && docList.size() > 0) {
			System.out.println("    Object - Additional Fields: " + docList.size() + " entries");
			for (IngridDocument doc : docList) {
				System.out.println("      " + doc.get(MdekKeys.ADDITIONAL_FIELD_KEY) + ": " + doc);
				if (doc.get(MdekKeys.ADDITIONAL_FIELD_ROWS) != null) {
					List<List<IngridDocument>> rows = (List<List<IngridDocument>>) doc.get(MdekKeys.ADDITIONAL_FIELD_ROWS);
					int rowNum = 0;
					for (List<IngridDocument> row : rows) {
						rowNum++;
						System.out.println("        Row " + rowNum);
						for (IngridDocument col : row) {
							System.out.println("          Col: " + col);
						}
					}
				}
			}
		}

		myDoc = (IngridDocument) oDoc.get(MdekKeys.PARENT_INFO);
		if (myDoc != null) {
			System.out.println("  parent info:");
			System.out.println("    " + myDoc);								
		}
	}


	public void debugAddressDocMergeData(IngridDocument doc) {
		System.out.println("Address MERGE DATA: " + doc.get(MdekKeys.STREET)
				+ ", " + doc.get(MdekKeys.POSTAL_CODE) + " " + doc.get(MdekKeys.CITY)
				+ ", " + doc.get(MdekKeys.COUNTRY_CODE) + " " + doc.get(MdekKeys.COUNTRY_NAME)
				+ ", " + doc.get(MdekKeys.POST_BOX_POSTAL_CODE) + " " + doc.get(MdekKeys.POST_BOX));		
	}

	public void manipulateAddressDocMergeData(IngridDocument doc, String prefix) {
		doc.put(MdekKeys.STREET, prefix + doc.get(MdekKeys.STREET));
		doc.put(MdekKeys.POSTAL_CODE, prefix + doc.get(MdekKeys.POSTAL_CODE));
		doc.put(MdekKeys.CITY, prefix + doc.get(MdekKeys.CITY));
		doc.put(MdekKeys.COUNTRY_CODE, null);
		doc.put(MdekKeys.COUNTRY_NAME, prefix + doc.get(MdekKeys.COUNTRY_NAME));
		doc.put(MdekKeys.POST_BOX_POSTAL_CODE, prefix + doc.get(MdekKeys.POST_BOX_POSTAL_CODE));
		doc.put(MdekKeys.POST_BOX, prefix + doc.get(MdekKeys.POST_BOX));
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
			+ ", publication condition: " + EnumUtil.mapDatabaseToEnumConst(PublishType.class, a.get(MdekKeys.PUBLICATION_CONDITION))
			+ ", modUser: " + extractUserData((IngridDocument)a.get(MdekKeys.MOD_USER))
			+ ", respUser: " + extractUserData((IngridDocument)a.get(MdekKeys.RESPONSIBLE_USER))
			+ ", assignerUser: " + extractUserData((IngridDocument)a.get(MdekKeys.ASSIGNER_USER))
			+ ", modified: " + MdekUtils.timestampToDisplayDate((String)a.get(MdekKeys.DATE_OF_LAST_MODIFICATION))
			+ ", created: " + MdekUtils.timestampToDisplayDate((String)a.get(MdekKeys.DATE_OF_CREATION))
		);
		System.out.println("         "
				+ ", hideAddress: " + a.get(MdekKeys.HIDE_ADDRESS)
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
		debugJobInfoDoc(jobInfoDoc, null);
	}
	public void debugJobInfoDoc(IngridDocument jobInfoDoc, JobType jobType) {
		System.out.println(jobInfoDoc);

		Exception jobExc = MdekCaller.getExceptionFromJobInfo(jobInfoDoc);
		if (jobExc != null) {
			System.out.println("JobInfo Exception !:" + jobExc);
			printThrowable(jobExc);
		}

		String frontendMsgs = (String) jobInfoDoc.get(MdekKeys.JOBINFO_FRONTEND_MESSAGES);
		if (frontendMsgs != null) {
			System.out.println("\nJobInfo Frontend Messages:\n" + jobInfoDoc.get(MdekKeys.JOBINFO_FRONTEND_MESSAGES));			
		}
		
		if (!doFullOutput) {
			return;
		}

//		System.out.println("JobInfo Messages:\n" + jobInfoDoc.get(MdekKeys.JOBINFO_MESSAGES));

		if (jobType == JobType.UPDATE_SEARCHTERMS) {
			List<Map> msgs = (List<Map>) jobInfoDoc.get(MdekKeys.JOBINFO_TERMS_UPDATED);
			if (msgs != null) {
				System.out.println("  " + msgs.size() + " messages to display:");
				int maxNum = 50;
				for (int i=0; i<msgs.size(); i++) {
					Map msg = msgs.get(i);
					System.out.println("    " + msg);
					if (i > maxNum) {
						System.out.println("    ...");
						break;
					}
				}
			}
		} else if (jobType == JobType.UPDATE_SPATIAL_REFERENCES) {
			List<Map> msgs = (List<Map>) jobInfoDoc.get(MdekKeys.JOBINFO_LOCATIONS_UPDATED);
			if (msgs != null) {
				System.out.println("  " + msgs.size() + " messages to display:");
				int maxNum = 50;
				for (int i=0; i<msgs.size(); i++) {
					Map msg = msgs.get(i);
					System.out.println("    " + msg);
					if (i > maxNum) {
						System.out.println("    ...");
						break;
					}
				}
			}
		}

	}
	public void debugArray(Object[] params, String label) {
		System.out.print(label);
		if (params != null) {
			for (Object param : params) {
				System.out.print(param + ", ");
			}
		} else {
			System.out.print(params);
		}
		System.out.println();
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
			String mdekErrorMsg = err.getErrorMessage();
			if (mdekErrorMsg != null) {
				System.out.println("    MdekError -> ErrorMessage: " + mdekErrorMsg);
			}

			if (err.getErrorType().equals(MdekErrorType.ENTITY_REFERENCED_BY_OBJ)) {
				// referenced entity (object or address)
				if (info.get(MdekKeys.TITLE) != null) {
					System.out.println("    referenced Object: ");
					debugObjectDoc(info);
				} else {
					System.out.println("    referenced Address: ");
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

			} else if (err.getErrorType().equals(MdekErrorType.REFERENCED_ADDRESSES_NOT_PUBLISHED) ||
					err.getErrorType().equals(MdekErrorType.REFERENCED_ADDRESSES_HAVE_SMALLER_PUBLICATION_CONDITION)) {
				// addresses referencing
				List<IngridDocument> aDocs = (List<IngridDocument>) info.get(MdekKeys.ADR_ENTITIES);
				if (aDocs != null) {
					System.out.println("    Referenced address(es): " + aDocs.size() + " addresses!");
					for (IngridDocument aDoc : aDocs) {
						debugAddressDoc(aDoc);
					}
				}

			} else if (err.getErrorType().equals(MdekErrorType.ADDRESS_IS_VERWALTER)) {
				// objects referencing address as verwalter
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
			} else if (err.getErrorType().equals(MdekErrorType.IMPORT_OBJECTS_ALREADY_EXIST)) {
				System.out.println("    Objects already exist, Copy not allowed:");
				List<IngridDocument> oDocs = (List<IngridDocument>) info.get(MdekKeys.OBJ_ENTITIES);
				if (oDocs != null) {
					for (IngridDocument oDoc : oDocs) {
						System.out.println("      " + oDoc);
					}
				}
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
	
	/** Passed address has to be published ! */
	public void addPointOfContactAddress(IngridDocument entityDoc, String addrUuid) {
		IngridDocument addrMap = fetchAddress(addrUuid, FetchQuantity.EDITOR_ENTITY, IdcEntityVersion.PUBLISHED_VERSION);
		addrMap.put(MdekKeys.RELATION_TYPE_ID, 7);
		addrMap.put(MdekKeys.RELATION_TYPE_REF, 505);
		List<IngridDocument> refAddressList = new ArrayList<IngridDocument>(1);
		refAddressList.add(addrMap);
		entityDoc.put(MdekKeys.ADR_REFERENCES_TO, refAddressList);
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
		newDoc.put(MdekKeys.PUBLICATION_CONDITION, MdekUtils.PublishType.INTERNET.getDbValue());
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
