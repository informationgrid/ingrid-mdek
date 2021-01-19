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

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import de.ingrid.mdek.services.persistence.db.GenericHibernateDao;
import de.ingrid.mdek.services.persistence.db.dao.ISpatialRefSnsDao;
import de.ingrid.mdek.services.persistence.db.model.SpatialRefSns;

/**
 * Hibernate-specific implementation of the <tt>SpatialRefSns</tt>
 * non-CRUD (Create, Read, Update, Delete) data access object.
 * 
 * @author Martin
 */
public class SpatialRefSnsDaoHibernate
	extends GenericHibernateDao<SpatialRefSns>
	implements  ISpatialRefSnsDao {

    public SpatialRefSnsDaoHibernate(SessionFactory factory) {
        super(factory, SpatialRefSns.class);
    }

	public SpatialRefSns load(String snsId) {
		Session session = getSession();

		SpatialRefSns spRefSns = (SpatialRefSns) session.createQuery("from SpatialRefSns " +
			"where snsId = ?")
			.setString(0, snsId)
			.uniqueResult();
		
		return spRefSns;
	}

	public SpatialRefSns loadOrCreate(String snsId) {
		SpatialRefSns spRefSns = load(snsId);
		
		if (spRefSns == null) {
			spRefSns = new SpatialRefSns();
			spRefSns.setSnsId(snsId);
			makePersistent(spRefSns);			
		}
		
		return spRefSns;
	}

}
