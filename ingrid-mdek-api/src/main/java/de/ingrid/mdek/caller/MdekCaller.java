/*
 * **************************************************-
 * ingrid-mdek-api
 * ==================================================
 * Copyright (C) 2014 - 2020 wemove digital solutions GmbH
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

import de.ingrid.mdek.MdekError;
import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.job.repository.Pair;
import de.ingrid.utils.IngridDocument;


/**
 * Abstract base class for all mdek job callers implementing common methods and data types.
 * Facade to IMdekClientCaller !
 * @author Martin
 */
public abstract class MdekCaller implements IMdekCaller {

	private final static Logger log = Logger.getLogger(MdekCaller.class);

	protected IMdekClientCaller mdekClientCaller;

    protected MdekCaller(IMdekClientCaller mdekClientCaller) {
    	this.mdekClientCaller = mdekClientCaller;
    }

    // IMdekClientCaller Facade !

	public IngridDocument getVersion(String plugId) {
		return mdekClientCaller.getVersion(plugId);
	}
	public List<String> getRegisteredIPlugs() {
		return mdekClientCaller.getRegisteredIPlugs();
	}
	public IngridDocument getRunningJobInfo(String plugId, String userId) {
		return mdekClientCaller.getRunningJobInfo(plugId, userId);
	}
	public IngridDocument cancelRunningJob(String plugId, String userId) {
		return mdekClientCaller.cancelRunningJob(plugId, userId);
	}
	public IngridDocument getResultFromResponse(IngridDocument mdekResponse) {
		return mdekClientCaller.getResultFromResponse(mdekResponse);
	}
	public String getErrorMsgFromResponse(IngridDocument mdekResponse) {
		return mdekClientCaller.getErrorMsgFromResponse(mdekResponse);
	}
	public List<MdekError> getErrorsFromResponse(IngridDocument mdekResponse) {
		return mdekClientCaller.getErrorsFromResponse(mdekResponse);
	}
	public static void shutdown() {
		MdekClientCaller.shutdown();
	}

	protected List<Pair> setUpJobMethod(String methodName, IngridDocument methodParams) {
		return mdekClientCaller.setUpJobMethod(methodName, methodParams);
	}
	protected IngridDocument callJob(String plugId, String jobId, List jobMethods) {
		return mdekClientCaller.callJob(plugId, jobId, jobMethods);
	}
	
    // Utilities

	/**
	 * Extracts Exception from passed JobInfo if present.<br>
	 * NOTICE: start of long running job may be ok, but maybe exception is thrown afterwards.
	 * Then it is encapsulated in job info.
	 * @param jobInfo job info requested from job (e.g. via getImportJobInfo)
	 * @return Exception or null if no exception (then job still running or finished properly).
	 */
	static public Exception getExceptionFromJobInfo(IngridDocument jobInfoDoc) {
		return (Exception) jobInfoDoc.get(MdekKeys.JOBINFO_EXCEPTION);
	}

	protected void debugDocument(String title, IngridDocument doc) {
		if (!log.isDebugEnabled()) {
			return;
		}

		if (title != null) {
			log.debug(title);
		}
		if (doc != null) {
			int docLength = doc.toString().length();
			log.debug("IngridDocument length: " + docLength);
		}

		log.debug("IngridDocument: " + doc);			
	}
}
