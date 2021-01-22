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

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import de.ingrid.mdek.job.IJob.JobType;
import de.ingrid.mdek.services.persistence.db.GenericHibernateDao;
import de.ingrid.mdek.services.persistence.db.dao.ISysJobInfoDao;
import de.ingrid.mdek.services.persistence.db.model.SysJobInfo;

/**
 * Hibernate-specific implementation of the <tt>ISysJobInfoDao</tt>
 * non-CRUD (Create, Read, Update, Delete) data access object.
 * 
 * @author Martin
 */
public class SysJobInfoDaoHibernate
	extends GenericHibernateDao<SysJobInfo>
	implements  ISysJobInfoDao {

    public SysJobInfoDaoHibernate(SessionFactory factory) {
        super(factory, SysJobInfo.class);
    }

	public SysJobInfo getJobInfo(JobType jobType, String userUuid) {
		Session session = getSession();
		
		String qString = "from SysJobInfo " +
			"where jobType = ? " +
			"and userUuid = ? ";

		Query q = session.createQuery(qString);
		q.setString(0, jobType.getDbValue());
		q.setString(1, userUuid);

		return (SysJobInfo) q.uniqueResult();
	}
}
