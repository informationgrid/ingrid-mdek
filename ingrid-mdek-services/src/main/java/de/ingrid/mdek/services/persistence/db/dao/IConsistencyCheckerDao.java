package de.ingrid.mdek.services.persistence.db.dao;

import java.util.List;

import de.ingrid.mdek.services.persistence.db.ITransactionService;
import de.ingrid.mdek.services.persistence.db.model.AddressNode;
import de.ingrid.mdek.services.persistence.db.model.ObjectNode;
import de.ingrid.mdek.services.persistence.db.model.T012ObjAdr;
import de.ingrid.mdek.services.persistence.db.model.T01Object;
import de.ingrid.utils.IngridDocument;

/**
 * Generic HQL operations.
 * 
 * @author Martin
 */
public interface IConsistencyCheckerDao
	extends ITransactionService {

	public List<AddressNode> checkAddressHierarchy();
	
	public List<ObjectNode> checkObjectHierarchy();
	
	public List<T012ObjAdr> checkAddressReferences();
	
	public List<T01Object> checkInfoAddress();
	
	public List<IngridDocument> checkTableAssociations();
	
}
