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
/*
 * Created on 10.07.2007
 */
package de.ingrid.mdek.services.persistence.db.test;

import org.hibernate.SessionFactory;

import de.ingrid.mdek.services.persistence.db.GenericHibernateDao;
import de.ingrid.mdek.services.persistence.db.IDaoFactory;
import de.ingrid.mdek.services.persistence.db.IEntity;
import de.ingrid.mdek.services.persistence.db.IGenericDao;

public class TestDaoFactory implements IDaoFactory {

    private final SessionFactory _sessionFactory;

    TestDaoFactory(SessionFactory sessionFactory) {
        _sessionFactory = sessionFactory;
    }
    
    public IGenericDao<IEntity> getDao(Class clazz) {
		IGenericDao dao = null;
		if (clazz.isAssignableFrom(TestMetadata.class)) {
			dao = new GenericHibernateDao<TestMetadata>(_sessionFactory,
					TestMetadata.class);
		}
		return dao;
    }
}
