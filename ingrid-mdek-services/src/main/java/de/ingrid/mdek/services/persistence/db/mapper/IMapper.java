package de.ingrid.mdek.services.persistence.db.mapper;


/**
 * Common stuff for all mappers.
 * 
 * @author Martin
 */
public interface IMapper {

	/** How much to map of bean/doc content */
	public enum MappingQuantity {
		/** IGE: initial data of entity when created */
		INITIAL_ENTITY,
		/** IGE: minimum data of entity needed */
		BASIC_ENTITY,
		/** IGE: entity displayed in tree */
		TREE_ENTITY,
		/** IGE: entity displayed in table */
		TABLE_ENTITY,
		/** IGE: entity edit/save */
		DETAIL_ENTITY,
		/** complete data -> copy entity via mapping */
		COPY_ENTITY,
	}
}
