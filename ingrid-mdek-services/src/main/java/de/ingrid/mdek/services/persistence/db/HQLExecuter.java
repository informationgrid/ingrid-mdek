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
package de.ingrid.mdek.services.persistence.db;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.ingrid.mdek.job.repository.Pair;
import de.ingrid.utils.IngridDocument;

@Service
public class HQLExecuter extends TransactionService implements IHQLExecuter {

	private static final Logger LOG = Logger.getLogger(HQLExecuter.class);

	@Autowired
	public HQLExecuter(SessionFactory sessionFactory) {
		super(sessionFactory);
	}

	@SuppressWarnings("unchecked")
	public IngridDocument execute(IngridDocument document) {

		// TODO optimistic locking
		List<Pair> list = new ArrayList<Pair>();
		IngridDocument ret = new IngridDocument();
		List<Pair> pairList = (List<Pair>) document.get(HQL_QUERIES);
		beginTransaction();
		Session session = getSession();
		for (Pair pair : pairList) {
			String key = pair.getKey();
			String hqlString = (String) pair.getValue();
			try {
				Query query = session.createQuery(hqlString);
				Object result = null;
				if (key.equals(IHQLExecuter.HQL_SELECT)) {
					result = query.list();
				} else if (key.equals(IHQLExecuter.HQL_UPDATE)) {
					result = query.executeUpdate();
				} else if (key.equals(IHQLExecuter.HQL_DELETE)) {
					result = query.executeUpdate();
				}
				list.add(new Pair(hqlString, (Serializable) result));
			} catch (HibernateException e) {
				if (LOG.isEnabledFor(Level.ERROR)) {
					LOG.error("error by execution of hqlQuery [" + hqlString
							+ "]", e);
				}
				ret.put(HQL_EXCEPTION, e.getMessage());
				ret.putBoolean(HQL_STATE, false);
				rollbackTransaction();
				return ret;
			}
		}
		ret.put(HQL_RESULT, list);
		commitTransaction();
		ret.putBoolean(HQL_STATE, true);
		return ret;
	}
}
