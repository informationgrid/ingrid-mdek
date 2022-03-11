/*
 * **************************************************-
 * ingrid-mdek-services
 * ==================================================
 * Copyright (C) 2014 - 2022 wemove digital solutions GmbH
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
/**
 * 
 */
package de.ingrid.mdek.services.security;

import de.ingrid.mdek.services.persistence.db.model.Permission;

/**
 * Factory for different types of entity permissions.
 * 
 * @author joachim
 * 
 */
public class PermissionFactory {

	public static EntityPermission getSingleObjectPermissionTemplate(String uuid) {
		Permission p = getPermissionTemplateSingle();
		return new EntityPermission(p, uuid);
	}

	public static EntityPermission getTreeObjectPermissionTemplate(String uuid) {
		Permission p = getPermissionTemplateTree();
		return new EntityPermission(p, uuid);
	}

	public static EntityPermission getSingleAddressPermissionTemplate(String uuid) {
		Permission p = getPermissionTemplateSingle();
		return new EntityPermission(p, uuid);
	}

	public static EntityPermission getTreeAddressPermissionTemplate(String uuid) {
		Permission p = getPermissionTemplateTree();
		return new EntityPermission(p, uuid);
	}

    public static EntityPermission getSubNodeObjectPermissionTemplate(String uuid) {
        Permission p = getPermissionTemplateSubNode();
        return new EntityPermission(p, uuid);
    }

    public static EntityPermission getSubNodeAddressPermissionTemplate(String uuid) {
        Permission p = getPermissionTemplateSubNode();
        return new EntityPermission(p, uuid);
    }
	
	
	public static Permission getPermissionTemplateCreateRoot() {
		Permission p = new Permission();
		p.setClassName("IdcUserPermission");
		p.setName("catalog");
		p.setAction("create-root");
		return p;
	}

	public static Permission getPermissionTemplateQA() {
		Permission p = new Permission();
		p.setClassName("IdcUserPermission");
		p.setName("catalog");
		p.setAction("qa");
		return p;
	}

	public static Permission getPermissionTemplateSingle() {
		Permission p = new Permission();
		p.setClassName("IdcEntityPermission");
		p.setName("entity");
		p.setAction("write");
		return p;
	}

	public static Permission getPermissionTemplateTree() {
		Permission p = new Permission();
		p.setClassName("IdcEntityPermission");
		p.setName("entity");
		p.setAction("write-tree");
		return p;
	}

    public static Permission getPermissionTemplateSubNode() {
        Permission p = new Permission();
        p.setClassName("IdcEntityPermission");
        p.setName("entity");
        p.setAction("write-subnode");
        return p;
    }

	public static Permission getDummyPermissionSubTree() {
		Permission p = new Permission();
		p.setClassName("Dummy Permission for frontend");
		p.setName("entity");
		p.setAction("write-subtree");
		return p;
	}
}
