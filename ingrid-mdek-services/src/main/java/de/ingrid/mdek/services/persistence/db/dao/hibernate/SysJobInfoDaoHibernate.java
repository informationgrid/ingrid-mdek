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
