package de.ingrid.mdek.job;

import de.ingrid.utils.IngridDocument;

/**
 * Abstract base class of mdek jobs encapsulating common stuff and default behaviour
 * 
 * @author Martin
 */
public abstract class MdekJob implements IJob {

	/**
	 * Called when job is registered !
	 * Default Handling here returns empty IngridDocument 
	 * @see de.ingrid.mdek.job.IJob#getResults()
	 */
	public IngridDocument getResults() {
        IngridDocument result = new IngridDocument();
		return result;
	}
}
