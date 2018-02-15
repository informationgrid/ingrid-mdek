/*
 * **************************************************-
 * ingrid-mdek-services
 * ==================================================
 * Copyright (C) 2014 - 2018 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.1 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl5
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
package de.ingrid.mdek.services.utils;

import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.MdekUtils.IdcEntityType;
import de.ingrid.mdek.services.persistence.db.IEntity;
import de.ingrid.mdek.services.persistence.db.model.AddressNode;
import de.ingrid.mdek.services.persistence.db.model.ObjectNode;
import de.ingrid.utils.IngridDocument;

/**
 * Singleton encapsulating Utility Methods concerning IGC Entities (Objects, Addresses ...).
 * 
 * @author Martin
 */
public class EntityHelper {

	private static final Logger LOG = LogManager.getLogger(EntityHelper.class);

	private static EntityHelper myInstance;

	/** Get The Singleton */
	public static synchronized EntityHelper getInstance() {
		if (myInstance == null) {
	        myInstance = new EntityHelper();
	      }
		return myInstance;
	}

	private EntityHelper() {}
	
	public String generateUuid() {
		UUID uuid = java.util.UUID.randomUUID();
		StringBuffer idcUuid = new StringBuffer(uuid.toString().toUpperCase());
		while (idcUuid.length() < 36) {
			idcUuid.append("0");
		}

		return idcUuid.toString();
	}

	/** Extract UUID of node given as IEntity. Pass Type of Entity. */
	public static String getUuidFromNode(IdcEntityType whichType, IEntity nodeEntity) {
		if (whichType == IdcEntityType.OBJECT) {
			return ((ObjectNode)nodeEntity).getObjUuid();
		} else if (whichType == IdcEntityType.ADDRESS) {
			return ((AddressNode)nodeEntity).getAddrUuid();
		}
		
		return null;
	}

	/** Extract Parent UUID of node given as IEntity. Pass Type of Entity. */
	public static String getParentUuidFromNode(IdcEntityType whichType, IEntity nodeEntity) {
		if (whichType == IdcEntityType.OBJECT) {
			return ((ObjectNode)nodeEntity).getFkObjUuid();
		} else if (whichType == IdcEntityType.ADDRESS) {
			return ((AddressNode)nodeEntity).getFkAddrUuid();
		}
		
		return null;
	}

	/** Extract OrigId from given doc. Pass Type of Entity. */
	public static String getOrigIdFromDoc(IdcEntityType whichType, IngridDocument doc) {
		if (whichType == IdcEntityType.OBJECT) {
			return doc.getString(MdekKeys.ORIGINAL_CONTROL_IDENTIFIER);
		} else if (whichType == IdcEntityType.ADDRESS) {
			return doc.getString(MdekKeys.ORIGINAL_ADDRESS_IDENTIFIER);
		}
		
		return null;
	}

	/** Extract Name of entity from given doc. Pass Type of Entity. */
	public static String getEntityNameFromDoc(IdcEntityType whichType, IngridDocument doc) {
		String retValue = null;

		if (whichType == IdcEntityType.OBJECT) {
			retValue = doc.getString(MdekKeys.TITLE);
		} else if (whichType == IdcEntityType.ADDRESS) {
			String name = "";
			if (doc.get(MdekKeys.GIVEN_NAME) != null) {
				name += doc.get(MdekKeys.GIVEN_NAME);
			}
			if (doc.get(MdekKeys.NAME) != null) {
				if (name.length() > 0) {
					name += " ";
				}
				name += doc.get(MdekKeys.NAME);
			}
			if (doc.get(MdekKeys.ORGANISATION) != null) {
				if (name.length() > 0) {
					name += ", ";
				}
				name += doc.get(MdekKeys.ORGANISATION);
			}
			retValue = name;
		}
		
		return retValue;
	}

	/** Extracts UUID from doc, if null then name of entity is returned ! */
	public static String getEntityIdentifierFromDoc(IdcEntityType whichType, IngridDocument doc) {
		String id = doc.getString(MdekKeys.UUID);
		if (id == null) {
			id = getEntityNameFromDoc(whichType, doc);
		}
		
		return id;
	}
}
