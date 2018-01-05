/*
 * **************************************************-
 * ingrid-mdek-api
 * ==================================================
 * Copyright (C) 2014 - 2018 wemove digital solutions GmbH
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
package de.ingrid.mdek.caller;

import java.util.List;

import org.apache.log4j.Logger;

import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.MdekUtils.IdcEntityOrderBy;
import de.ingrid.mdek.MdekUtils.IdcEntityVersion;
import de.ingrid.mdek.MdekUtils.IdcQAEntitiesSelectionType;
import de.ingrid.mdek.MdekUtils.IdcStatisticsSelectionType;
import de.ingrid.mdek.MdekUtils.IdcWorkEntitiesSelectionType;
import de.ingrid.mdek.MdekUtils.WorkState;
import de.ingrid.utils.IngridDocument;


/**
 * Singleton implementing methods to communicate with the Mdek backend
 * concerning OBJECT Manipulation.
 * 
 * @author Martin
 */
public class MdekCallerObject extends MdekCaller implements IMdekCallerObject {

	private final static Logger log = Logger.getLogger(MdekCallerObject.class);

	private static MdekCallerObject myInstance;

	// Jobs
	private static String MDEK_IDC_OBJECT_JOB_ID = "de.ingrid.mdek.job.MdekIdcObjectJob";

    private MdekCallerObject(IMdekClientCaller mdekClientCaller) {
    	super(mdekClientCaller);
    }

	/**
	 * INITIALIZATION OF SINGLETON !!!
	 * Has to be called once before calling getInstance() !!!
	 */
	public static synchronized void initialize(IMdekClientCaller mdekClientCaller) {
		if (myInstance == null) {
			myInstance = new MdekCallerObject(mdekClientCaller);
		} else {
			log.warn("WARNING! MULTIPLE INITIALIZATION OF " + myInstance.getClass() + " !");
		}
	}

	/**
	 * NOTICE: Singleton has to be initialized once (initialize(...)) before getting the instance !
	 * @return null if not initialized
	 */
	public static MdekCallerObject getInstance() {
		if (myInstance == null) {
			log.warn("WARNING! INITIALIZE " + MdekCallerObject.class + " instance before fetching it !!! we return null !!!");
		}

		return myInstance;
	}

	public IngridDocument fetchObject(String plugId, String uuid, FetchQuantity howMuch,
			IdcEntityVersion whichEntityVersion, String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.UUID, uuid);
		jobParams.put(MdekKeys.REQUESTINFO_FETCH_QUANTITY, howMuch);
		jobParams.put(MdekKeys.REQUESTINFO_WHICH_ENTITY_VERSION, whichEntityVersion);
		jobParams.put(MdekKeys.USER_ID, userId);
		List jobMethods = setUpJobMethod("getObjDetails", jobParams);
		return callJob(plugId, MDEK_IDC_OBJECT_JOB_ID, jobMethods);
	}

	public IngridDocument storeObject(String plugId, IngridDocument objDoc,
			boolean refetchAfterStore,
			String userId) {
		objDoc.put(MdekKeys.REQUESTINFO_REFETCH_ENTITY, refetchAfterStore);
		objDoc.put(MdekKeys.USER_ID, userId);
		List jobMethods = setUpJobMethod("storeObject", objDoc);
		return callJob(plugId, MDEK_IDC_OBJECT_JOB_ID, jobMethods);
	}

	public IngridDocument assignObjectToQA(String plugId, IngridDocument objDoc,
			boolean refetchAfterStore,
			String userId) {
		objDoc.put(MdekKeys.REQUESTINFO_REFETCH_ENTITY, refetchAfterStore);
		objDoc.put(MdekKeys.USER_ID, userId);
		List jobMethods = setUpJobMethod("assignObjectToQA", objDoc);
		return callJob(plugId, MDEK_IDC_OBJECT_JOB_ID, jobMethods);
	}

	public IngridDocument reassignObjectToAuthor(String plugId, IngridDocument objDoc,
			boolean refetchAfterStore,
			String userId) {
		objDoc.put(MdekKeys.REQUESTINFO_REFETCH_ENTITY, refetchAfterStore);
		objDoc.put(MdekKeys.USER_ID, userId);
		List jobMethods = setUpJobMethod("reassignObjectToAuthor", objDoc);
		return callJob(plugId, MDEK_IDC_OBJECT_JOB_ID, jobMethods);
	}

	public IngridDocument updateObjectPart(String plugId, IngridDocument objPartDoc,
			IdcEntityVersion whichEntityVersion, String userId) {
		objPartDoc.put(MdekKeys.USER_ID, userId);
		objPartDoc.put(MdekKeys.REQUESTINFO_WHICH_ENTITY_VERSION, whichEntityVersion);
		List jobMethods = setUpJobMethod("updateObjectPart", objPartDoc);
		return callJob(plugId, MDEK_IDC_OBJECT_JOB_ID, jobMethods);
	}

	public IngridDocument publishObject(String plugId, IngridDocument objDoc,
			boolean refetchAfterStore,
			boolean forcePublicationCondition,
			String userId) {
		objDoc.put(MdekKeys.REQUESTINFO_REFETCH_ENTITY, refetchAfterStore);
		objDoc.put(MdekKeys.REQUESTINFO_FORCE_PUBLICATION_CONDITION, forcePublicationCondition);
		objDoc.put(MdekKeys.USER_ID, userId);
		List jobMethods = setUpJobMethod("publishObject", objDoc);
		return callJob(plugId, MDEK_IDC_OBJECT_JOB_ID, jobMethods);
	}

	public IngridDocument deleteObjectWorkingCopy(String plugId, String uuid,
			boolean forceDeleteReferences,
			String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.UUID, uuid);
		jobParams.put(MdekKeys.REQUESTINFO_FORCE_DELETE_REFERENCES, forceDeleteReferences);
		jobParams.put(MdekKeys.USER_ID, userId);
		List jobMethods = setUpJobMethod("deleteObjectWorkingCopy", jobParams);

		return callJob(plugId, MDEK_IDC_OBJECT_JOB_ID, jobMethods);
	}

	public IngridDocument deleteObject(String plugId, String uuid,
			boolean forceDeleteReferences,
			String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.UUID, uuid);
		jobParams.put(MdekKeys.REQUESTINFO_FORCE_DELETE_REFERENCES, forceDeleteReferences);
		jobParams.put(MdekKeys.USER_ID, userId);
		List jobMethods = setUpJobMethod("deleteObject", jobParams);

		return callJob(plugId, MDEK_IDC_OBJECT_JOB_ID, jobMethods);
	}

	public IngridDocument fetchTopObjects(String plugId, String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.USER_ID, userId);
		List jobMethods = setUpJobMethod("getTopObjects", jobParams);

		return callJob(plugId, MDEK_IDC_OBJECT_JOB_ID, jobMethods);
	}

	public IngridDocument fetchSubObjects(String plugId, String objUuid,
			String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.UUID, objUuid);
		jobParams.put(MdekKeys.USER_ID, userId);
		List jobMethods = setUpJobMethod("getSubObjects", jobParams);

		return callJob(plugId, MDEK_IDC_OBJECT_JOB_ID, jobMethods);
	}

	public IngridDocument getObjectPath(String plugId, String uuid,
			String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.UUID, uuid);
		jobParams.put(MdekKeys.USER_ID, userId);
		List jobMethods = setUpJobMethod("getObjectPath", jobParams);

		return callJob(plugId, MDEK_IDC_OBJECT_JOB_ID, jobMethods);		
	}

	public IngridDocument checkObjectSubTree(String plugId, String uuid,
			String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.UUID, uuid);
		jobParams.put(MdekKeys.USER_ID, userId);
		List jobMethods = setUpJobMethod("checkObjectSubTree", jobParams);

		return callJob(plugId, MDEK_IDC_OBJECT_JOB_ID, jobMethods);
	}

	public IngridDocument copyObject(String plugId, String fromUuid, String toUuid, boolean copySubtree,
			String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.FROM_UUID, fromUuid);
		jobParams.put(MdekKeys.TO_UUID, toUuid);
		jobParams.put(MdekKeys.REQUESTINFO_COPY_SUBTREE, copySubtree);
		jobParams.put(MdekKeys.USER_ID, userId);
		List jobMethods = setUpJobMethod("copyObject", jobParams);

		return callJob(plugId, MDEK_IDC_OBJECT_JOB_ID, jobMethods);
	}

	public IngridDocument moveObject(String plugId, String fromUuid, String toUuid,
			boolean forcePublicationCondition,
			String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.FROM_UUID, fromUuid);
		jobParams.put(MdekKeys.TO_UUID, toUuid);
		jobParams.put(MdekKeys.REQUESTINFO_FORCE_PUBLICATION_CONDITION, forcePublicationCondition);
		jobParams.put(MdekKeys.USER_ID, userId);
		List jobMethods = setUpJobMethod("moveObject", jobParams);

		return callJob(plugId, MDEK_IDC_OBJECT_JOB_ID, jobMethods);
	}

	public IngridDocument getInitialObject(String plugId, IngridDocument newBasicObject,
			String userId) {
		IngridDocument jobParams = newBasicObject;
		jobParams.put(MdekKeys.USER_ID, userId);
		List jobMethods = setUpJobMethod("getInitialObject", jobParams);

		return callJob(plugId, MDEK_IDC_OBJECT_JOB_ID, jobMethods);
	}

	public IngridDocument getWorkObjects(String plugId,
			IdcWorkEntitiesSelectionType selectionType, 
			IdcEntityOrderBy orderBy, boolean orderAsc,
			int startHit, int numHits,
			String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.REQUESTINFO_ENTITY_SELECTION_TYPE, selectionType);
		jobParams.put(MdekKeys.REQUESTINFO_ENTITY_ORDER_BY, orderBy);
		jobParams.put(MdekKeys.REQUESTINFO_ENTITY_ORDER_ASC, orderAsc);
		jobParams.put(MdekKeys.REQUESTINFO_START_HIT, startHit);
		jobParams.put(MdekKeys.REQUESTINFO_NUM_HITS, numHits);
		jobParams.put(MdekKeys.USER_ID, userId);
		List jobMethods = setUpJobMethod("getWorkObjects", jobParams);

		return callJob(plugId, MDEK_IDC_OBJECT_JOB_ID, jobMethods);
	}

	public IngridDocument getQAObjects(String plugId,
			WorkState whichWorkState, IdcQAEntitiesSelectionType selectionType, 
			IdcEntityOrderBy orderBy, boolean orderAsc,
			int startHit, int numHits,
			String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.REQUESTINFO_WHICH_WORK_STATE, whichWorkState);
		jobParams.put(MdekKeys.REQUESTINFO_ENTITY_SELECTION_TYPE, selectionType);
		jobParams.put(MdekKeys.REQUESTINFO_ENTITY_ORDER_BY, orderBy);
		jobParams.put(MdekKeys.REQUESTINFO_ENTITY_ORDER_ASC, orderAsc);
		jobParams.put(MdekKeys.REQUESTINFO_START_HIT, startHit);
		jobParams.put(MdekKeys.REQUESTINFO_NUM_HITS, numHits);
		jobParams.put(MdekKeys.USER_ID, userId);
		List jobMethods = setUpJobMethod("getQAObjects", jobParams);

		return callJob(plugId, MDEK_IDC_OBJECT_JOB_ID, jobMethods);
	}

	public IngridDocument getObjectStatistics(String plugId, String parentUuid,
			IdcStatisticsSelectionType selectionType,
			int startHit, int numHits,
			String userId) {
		IngridDocument jobParams = new IngridDocument();
		jobParams.put(MdekKeys.UUID, parentUuid);
		jobParams.put(MdekKeys.REQUESTINFO_ENTITY_SELECTION_TYPE, selectionType);
		jobParams.put(MdekKeys.REQUESTINFO_START_HIT, startHit);
		jobParams.put(MdekKeys.REQUESTINFO_NUM_HITS, numHits);
		jobParams.put(MdekKeys.USER_ID, userId);
		List jobMethods = setUpJobMethod("getObjectStatistics", jobParams);

		return callJob(plugId, MDEK_IDC_OBJECT_JOB_ID, jobMethods);
	}

    @Override
    public IngridDocument getIsoXml(String plugId, String uuid, IdcEntityVersion version, String userId) {
        IngridDocument jobParams = new IngridDocument();
        jobParams.put(MdekKeys.UUID, uuid);
        jobParams.put(MdekKeys.REQUESTINFO_WHICH_ENTITY_VERSION, version);
        jobParams.put(MdekKeys.USER_ID, userId);
        List jobMethods = setUpJobMethod("getIsoXml", jobParams);

        return callJob(plugId, MDEK_IDC_OBJECT_JOB_ID, jobMethods);
    }
}
