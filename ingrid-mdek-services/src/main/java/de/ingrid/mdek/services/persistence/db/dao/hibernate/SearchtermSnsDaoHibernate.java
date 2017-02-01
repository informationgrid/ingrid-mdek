/*
 * **************************************************-
 * ingrid-mdek-services
 * ==================================================
 * Copyright (C) 2014 - 2017 wemove digital solutions GmbH
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
import de.ingrid.mdek.services.persistence.db.dao.ISearchtermSnsDao;
import de.ingrid.mdek.services.persistence.db.model.SearchtermSns;

/**
 * Hibernate-specific implementation of the <tt>SearchtermSns</tt>
 * non-CRUD (Create, Read, Update, Delete) data access object.
 * 
 * @author Martin
 */
public class SearchtermSnsDaoHibernate
	extends GenericHibernateDao<SearchtermSns>
	implements  ISearchtermSnsDao {

    public SearchtermSnsDaoHibernate(SessionFactory factory) {
        super(factory, SearchtermSns.class);
    }

	private SearchtermSns load(String snsId) {
		Session session = getSession();

		String qString = "from SearchtermSns " +
			"where snsId = '" + snsId + "' ";

		SearchtermSns termSns = (SearchtermSns) session.createQuery(qString)
			.uniqueResult();
		
		return termSns;
	}

	public SearchtermSns loadOrCreate(String snsId, String gemetId) {
		SearchtermSns termSns = load(snsId);
		
		if (termSns == null) {
			termSns = new SearchtermSns();
		}

		// update with newest values
		termSns.setSnsId(snsId);
		termSns.setGemetId(gemetId);
		makePersistent(termSns);
		
		return termSns;
	}
}
