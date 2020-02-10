/*
 * **************************************************-
 * ingrid-mdek-services
 * ==================================================
 * Copyright (C) 2014 - 2020 wemove digital solutions GmbH
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

import java.util.Arrays;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import de.ingrid.mdek.MdekUtils;
import de.ingrid.mdek.services.persistence.db.GenericHibernateDao;
import de.ingrid.mdek.services.persistence.db.dao.ISysGenericKeyDao;
import de.ingrid.mdek.services.persistence.db.model.SysGenericKey;

/**
 * Hibernate-specific implementation of the <tt>ISysGenericKeyDao</tt>
 * non-CRUD (Create, Read, Update, Delete) data access object.
 * 
 * @author Martin
 */
public class SysGenericKeyDaoHibernate
	extends GenericHibernateDao<SysGenericKey>
	implements  ISysGenericKeyDao {

    public SysGenericKeyDaoHibernate(SessionFactory factory) {
        super(factory, SysGenericKey.class);
    }

	public List<SysGenericKey> getSysGenericKeys(String[] keyNames) {
		Session session = getSession();

		boolean selectKeys = false;
		if (keyNames != null && keyNames.length > 0) {
			selectKeys = true;
		}

		String sql = "select genericKey from SysGenericKey genericKey";
		if (selectKeys) {
			sql += " where " + MdekUtils.createSplittedSqlQuery( "genericKey.keyName", Arrays.asList( keyNames ), 500 );
		}
		
		Query q = session.createQuery(sql);
		
		return q.list();
	}
}
