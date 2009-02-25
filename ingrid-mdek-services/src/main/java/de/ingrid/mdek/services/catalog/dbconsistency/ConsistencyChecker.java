package de.ingrid.mdek.services.catalog.dbconsistency;

import java.util.List;

public interface ConsistencyChecker {

	public void run();
	
	public List<ErrorReport> getResult();
	
}
