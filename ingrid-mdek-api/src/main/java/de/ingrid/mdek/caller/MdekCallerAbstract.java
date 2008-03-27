package de.ingrid.mdek.caller;

import org.apache.log4j.Logger;

import de.ingrid.utils.IngridDocument;


/**
 * Abstract base class for all mdek job callers implementing common methods and data types.
 * @author Martin
 */
public abstract class MdekCallerAbstract implements IMdekCallerAbstract {

	private final static Logger log = Logger.getLogger(MdekCallerAbstract.class);

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
