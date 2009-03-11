package de.ingrid.mdek.services.persistence.db.dao.hibernate;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.type.LongType;

import de.ingrid.mdek.services.persistence.db.GenericHibernateDao;
import de.ingrid.mdek.services.persistence.db.dao.IT08AttrTypeDao;
import de.ingrid.mdek.services.persistence.db.model.T08Attr;
import de.ingrid.mdek.services.persistence.db.model.T08AttrType;

/**
 * Hibernate-specific implementation of the <tt>IT08AttrTypeDao</tt>
 * non-CRUD (Create, Read, Update, Delete) data access object.
 * 
 * @author Martin
 */
public class T08AttrTypeDaoHibernate
	extends GenericHibernateDao<T08AttrType>
	implements  IT08AttrTypeDao {

    public T08AttrTypeDaoHibernate(SessionFactory factory) {
        super(factory, T08AttrType.class);
    }

	public List<T08AttrType> getT08AttrTypes(Long[] fieldIds, String languageCode) {
		Session session = getSession();

		boolean selectIds = (fieldIds == null) ? false : true;
		boolean selectLanguage = (languageCode == null) ? false : true; 

		String hql = "select distinct addField " +
			"from T08AttrType addField " +
			"left join fetch addField.t08AttrLists addFieldList ";			
		if (selectIds || selectLanguage) {
			hql += "where ";
		}
		boolean addAnd = false;
		if (selectIds) {
			hql += "addField.id in (:idList) ";
			addAnd = true;
		}
		if (selectLanguage) {
			if (addAnd) {
				hql += "and ";				
			}
			// also select "nulls" to select additional without selection list (direct input)
			hql += "(addFieldList.langCode = '" + languageCode + "' " +
					"OR addFieldList.langCode is null)";
			addAnd = true;
		}
		
		Query q = session.createQuery(hql);
		if (selectIds) {
			q.setParameterList("idList", fieldIds, new LongType());
		}
		
		return q.list();
	}

	public List<T08Attr> getT08Attr(Long fieldId) {
		Session session = getSession();

		String hql = "from T08Attr where attrTypeId = " + fieldId;
		
		return session.createQuery(hql).list();
	}
}
