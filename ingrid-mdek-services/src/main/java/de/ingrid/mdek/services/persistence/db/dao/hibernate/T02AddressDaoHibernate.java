/*
 * **************************************************-
 * ingrid-mdek-services
 * ==================================================
 * Copyright (C) 2014 - 2021 wemove digital solutions GmbH
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
package de.ingrid.mdek.services.persistence.db.dao.hibernate;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.transform.DistinctRootEntityResultTransformer;

import de.ingrid.mdek.MdekUtils.AddressType;
import de.ingrid.mdek.services.persistence.db.GenericHibernateDao;
import de.ingrid.mdek.services.persistence.db.dao.IT02AddressDao;
import de.ingrid.mdek.services.persistence.db.model.T01Object;
import de.ingrid.mdek.services.persistence.db.model.T02Address;

/**
 * Hibernate-specific implementation of the <tt>IT02AddressDao</tt>
 * non-CRUD (Create, Read, Update, Delete) data access object.
 * 
 * @author Martin
 */
public class T02AddressDaoHibernate
	extends GenericHibernateDao<T02Address>
	implements  IT02AddressDao {

    public T02AddressDaoHibernate(SessionFactory factory) {
        super(factory, T02Address.class);
    }

	public List<T01Object> getObjectReferencesByTypeId(String addressUuid, Integer referenceTypeId, Integer maxNum) {
		Session session = getSession();

		// NOTICE: we DON'T FETCH t012ObjAdrs, so we get ALL t012ObjAdrs when requested afterwards !
		// (otherwise only the t012ObjAdrs matching selection criteria would be fetched !)
		String hql = "select obj " +
			"from T01Object obj " +
			"join obj.t012ObjAdrs objAdr " +
			"where objAdr.adrUuid = ?";
		
		if (referenceTypeId != null) {
			hql += " and objAdr.type = " + referenceTypeId;
		}

		Query q = session.createQuery(hql)
			.setString(0, addressUuid);
		if (maxNum != null) {
			q.setMaxResults(maxNum);
		}

		return q.setResultTransformer(new DistinctRootEntityResultTransformer())
			.list();
	}
	public String getCsvHQLObjectReferencesByTypeId(String addressUuid, Integer referenceTypeId) {
		String hql = "select distinct o.objUuid, o.objName, o.workState " +
			"from T01Object o " +
			"join o.t012ObjAdrs objAdr " +
			"where objAdr.adrUuid = '" + addressUuid + "'";
		
		if (referenceTypeId != null) {
			hql += " and objAdr.type = " + referenceTypeId;
		}
		
		return hql;
	}

	public List<T02Address> getAddressesOfResponsibleUser(String responsibleUserUuid, Integer maxNum) {
		Session session = getSession();

		String hql = "select a " +
			"from T02Address a " +
			"where " +
			// exclude hidden user addresses !
			AddressType.getHQLExcludeIGEUsersViaAddress("a") +
			" AND a.responsibleUuid = ?";
		
		Query q = session.createQuery(hql)
			.setString(0, responsibleUserUuid);
		if (maxNum != null) {
			q.setMaxResults(maxNum);
		}

		return q.setResultTransformer(new DistinctRootEntityResultTransformer())
			.list();
	}
	public String getCsvHQLAllAddressesOfResponsibleUser(String responsibleUserUuid) {
		String hql = "select distinct a.adrUuid, a.institution, a.lastname, a.firstname, a.workState " +
			"from T02Address a " +
			"where " +
			// exclude hidden user addresses !
			AddressType.getHQLExcludeIGEUsersViaAddress("a") +
			" AND a.responsibleUuid = '" + responsibleUserUuid + "'";

		return hql;
	}
}
