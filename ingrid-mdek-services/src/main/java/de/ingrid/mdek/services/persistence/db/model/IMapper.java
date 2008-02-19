package de.ingrid.mdek.services.persistence.db.model;


/**
 * Common stuff for all mappers.
 * 
 * @author Martin
 */
public interface IMapper {

	/** How much to map of bean/doc content */
	public enum MappingQuantity {
		INITIAL_ENTITY, // client: initial data of entity when created
		BASIC_ENTITY, // client: minimum data of entity needed
		TREE_ENTITY, // client: entity displayed in tree
		TABLE_ENTITY, // client: entity displayed in table
		DETAIL_ENTITY, // client: entity edit/save
		COPY_ENTITY // complete data -> copy entity via mapping
	}

	/** Type of entity to map */
	public enum IdcEntityType {
		OBJECT,
		ADDRESS;
	}
}
