package de.ingrid.mdek.services.persistence.db.dao.hibernate;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.type.LongType;

import de.ingrid.mdek.services.persistence.db.GenericHibernateDao;
import de.ingrid.mdek.services.persistence.db.dao.IT08AttrTypeDao;
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

		String sql = "from T08AttrType addField " +
			"left join fetch addField.t08AttrLists addFieldList ";			
		if (selectIds || selectLanguage) {
			sql += "where ";
		}
		boolean addAnd = false;
		if (selectIds) {
			sql += "addField.id in (:idList) ";
			addAnd = true;
		}
		if (selectLanguage) {
			if (addAnd) {
				sql += "and ";				
			}
			// also select "nulls" to select additional without selection list (direct input)
			sql += "(addFieldList.langCode = '" + languageCode + "' " +
					"OR addFieldList.langCode is null)";
			addAnd = true;
		}
		
		Query q = session.createQuery(sql);
		if (selectIds) {
			q.setParameterList("idList", fieldIds, new LongType());
		}
		
		return q.list();
	}
}
