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
		/** complete data EXCLUDING entity specific stuff (ORG_UUID) -> copy entity to new entity */
		COPY_DATA,
		/** ALL data INCLUDING entity specific stuff (ORG_UUID) -> copy published <-> working version */
		COPY_ENTITY,
	}
}
