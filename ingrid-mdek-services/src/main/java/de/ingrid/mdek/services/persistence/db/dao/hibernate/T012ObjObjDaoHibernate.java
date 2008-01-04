package de.ingrid.mdek.services.persistence.db.dao.hibernate;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import de.ingrid.mdek.services.persistence.db.GenericHibernateDao;
import de.ingrid.mdek.services.persistence.db.dao.IT012ObjObjDao;
import de.ingrid.mdek.services.persistence.db.model.T012ObjObj;
import de.ingrid.mdek.services.persistence.db.model.IMapper.T012ObjObjRelationType;

/**
 * Hibernate-specific implementation of the <tt>IT012ObjObjDao</tt>
 * non-CRUD (Create, Read, Update, Delete) data access object.
 * 
 * @author Martin
 */
public class T012ObjObjDaoHibernate
	extends GenericHibernateDao<T012ObjObj>
	implements  IT012ObjObjDao {

    public T012ObjObjDaoHibernate(SessionFactory factory) {
        super(factory, T012ObjObj.class);
    }

	public T012ObjObj getParentAssociation(String uuid) {
		Session session = getSession();
		
		Integer relationType = T012ObjObjRelationType.STRUKTURBAUM.getDbValue();

		T012ObjObj oO = (T012ObjObj) session.createQuery("from T012ObjObj oO " +
			"where oO.objectToUuid = ? " +
			"and oO.type = " + relationType)
			.setString(0, uuid)
			.uniqueResult();
		
		return oO;
	}
}
