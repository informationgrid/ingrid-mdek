package de.ingrid.mdek.services.persistence.db.model;

import de.ingrid.mdek.IMdekEnum;

/**
 * Common stuff for all mappers.
 * 
 * @author Martin
 */
public interface IMapper {

	/** How much to map of bean/doc content */
	public enum MappingQuantity {
		BASIC_ENTITY, // client: minimum data of bean needed
		TOP_ENTITY, // client: bean displayed in tree as topnode
		SUB_ENTITY, // client: bean displayed in tree as subnode
		TABLE_ENTITY, // client: bean displayed in table
		DETAIL_ENTITY // client: bean edit/save
	}
	
	/** Types of T012ObjObj relations */
	public enum T012ObjObjRelationType implements IMdekEnum {
		ALLE(null),
		STRUKTURBAUM(0),
		QUERVERWEIS(1);

		T012ObjObjRelationType(Integer dbValue) {
			this.dbValue = dbValue;
		}
		public Integer getDbValue() {
			return dbValue;
		}
		Integer dbValue;
	}
}
