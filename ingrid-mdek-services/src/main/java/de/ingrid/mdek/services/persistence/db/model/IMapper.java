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
		TABLE_ENTITY, // client: bean displayed in table
		DETAIL_ENTITY, // client: bean edit/save
		FULL_ENTITY // complete data e.g. to copy entity via map
	}
}
