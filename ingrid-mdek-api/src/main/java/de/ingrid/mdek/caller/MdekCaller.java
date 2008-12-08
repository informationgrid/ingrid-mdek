package de.ingrid.mdek.caller;

import java.util.List;

import org.apache.log4j.Logger;

import de.ingrid.mdek.MdekError;
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

	protected List setUpJobMethod(String methodName, IngridDocument methodParams) {
		return mdekClientCaller.setUpJobMethod(methodName, methodParams);
	}
	protected IngridDocument callJob(String plugId, String jobId, List jobMethods) {
		return mdekClientCaller.callJob(plugId, jobId, jobMethods);
	}
	
    // Utilities

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
