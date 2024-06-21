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

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import de.ingrid.mdek.job.IJob.JobType;
import de.ingrid.mdek.services.persistence.db.GenericHibernateDao;
import de.ingrid.mdek.services.persistence.db.dao.ISysJobInfoDao;
import de.ingrid.mdek.services.persistence.db.model.SysJobInfo;
import org.hibernate.query.Query;

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
			"where jobType = ?1 " +
			"and userUuid = ?2 ";

		Query q = session.createQuery(qString);
		q.setParameter(1, jobType.getDbValue());
		q.setParameter(2, userUuid);

		return (SysJobInfo) q.uniqueResult();
	}
}
