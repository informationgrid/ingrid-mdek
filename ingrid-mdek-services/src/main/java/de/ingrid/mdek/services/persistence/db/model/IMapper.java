package de.ingrid.mdek.services.persistence.db.model;


/**
 * Common stuff for all mappers.
 * 
 * @author Martin
 */
public interface IMapper {

	/** How much to map of bean/doc content */
	public enum MappingQuantity {
		BASIC_ENTITY, // client: minimum data of bean needed
		TREE_ENTITY, // client: bean displayed in tree
		TABLE_ENTITY, // client: bean displayed in table
		DETAIL_ENTITY, // client: bean edit/save
		COPY_ENTITY // complete data -> copy entity via mapping
	}

	/** Type of entity to map */
	public enum IdcEntityType {
		OBJECT,
		ADDRESS;
	}
}
