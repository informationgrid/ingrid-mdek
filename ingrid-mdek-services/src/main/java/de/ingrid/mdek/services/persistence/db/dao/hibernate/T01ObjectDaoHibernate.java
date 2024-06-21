/*
 * **************************************************-
 * ingrid-mdek-services
 * ==================================================
 * Copyright (C) 2014 - 2024 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.2 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl
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

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import de.ingrid.mdek.services.persistence.db.GenericHibernateDao;
import de.ingrid.mdek.services.persistence.db.dao.IT01ObjectDao;
import de.ingrid.mdek.services.persistence.db.model.T01Object;
import org.hibernate.query.Query;
import org.hibernate.transform.ToListResultTransformer;

/**
 * Hibernate-specific implementation of the <tt>IT01ObjectDao</tt>
 * non-CRUD (Create, Read, Update, Delete) data access object.
 *
 * @author Martin
 */
public class T01ObjectDaoHibernate
	extends GenericHibernateDao<T01Object>
	implements  IT01ObjectDao {

    public T01ObjectDaoHibernate(SessionFactory factory) {
        super(factory, T01Object.class);
    }

	public List<T01Object> getObjectsOfResponsibleUser(String responsibleUserUuid, Integer maxNum) {
		Session session = getSession();

		String hql = "select o " +
			"from T01Object o " +
			"where o.responsibleUuid = ?1";

		Query q = session.createQuery(hql)
			.setParameter(1, responsibleUserUuid);
		if (maxNum != null) {
			q.setMaxResults(maxNum);
		}

		return q.setResultTransformer(ToListResultTransformer.INSTANCE)
			.list();
	}
	public String getCsvHQLAllObjectsOfResponsibleUser(String responsibleUserUuid) {
		String hql = "select distinct o.objUuid, o.objName, o.workState " +
			"from T01Object o " +
			"where o.responsibleUuid = '" + responsibleUserUuid + "'";

		return hql;
	}

	public boolean hasAddressRelation(String objectUuid, String addressUuid, Integer realtionTypeId) {
	  Session session = getSession();

	  String hql = "select objAdr " +
      "from T01Object obj " +
      "join obj.t012ObjAdrs objAdr " +
      "where objAdr.adrUuid = ?1 and obj.objUuid = ?2";

    if (realtionTypeId != null) {
      hql += " and objAdr.type = " + realtionTypeId;
    }

    Query q = session.createQuery(hql)
			.setParameter(1, addressUuid)
			.setParameter(2, objectUuid);

    return !q.list().isEmpty();
	}
}
