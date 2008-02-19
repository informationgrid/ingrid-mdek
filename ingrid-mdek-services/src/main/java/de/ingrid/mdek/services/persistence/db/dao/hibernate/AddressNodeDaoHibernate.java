package de.ingrid.mdek.services.persistence.db.dao.hibernate;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import de.ingrid.mdek.MdekUtils.AddressType;
import de.ingrid.mdek.services.persistence.db.GenericHibernateDao;
import de.ingrid.mdek.services.persistence.db.dao.IAddressNodeDao;
import de.ingrid.mdek.services.persistence.db.model.AddressNode;
import de.ingrid.mdek.services.persistence.db.model.ObjectNode;

/**
 * Hibernate-specific implementation of the <tt>IAddressNodeDao</tt>
 * non-CRUD (Create, Read, Update, Delete) data access object.
 * 
 * @author Martin
 */
public class AddressNodeDaoHibernate
	extends GenericHibernateDao<AddressNode>
	implements  IAddressNodeDao {

	private static final Logger LOG = Logger.getLogger(AddressNodeDaoHibernate.class);

    public AddressNodeDaoHibernate(SessionFactory factory) {
        super(factory, AddressNode.class);
    }

	public AddressNode loadByUuid(String uuid) {
		if (uuid == null) {
			return null;
		}

		Session session = getSession();

		AddressNode aN = (AddressNode) session.createQuery("from AddressNode aNode " +
			"where aNode.addrUuid = ?")
			.setString(0, uuid)
			.uniqueResult();
		
		return aN;
	}

	public List<AddressNode> getTopAddresses(boolean onlyFreeAddresses) {
		Session session = getSession();
		ArrayList<AddressNode> retList = new ArrayList<AddressNode>();

		String query = "from AddressNode aNode " +
			"left join fetch aNode.t02AddressWork a " +
			"left join fetch aNode.addressNodeChildren aChildren " +
			"where aNode.fkAddrUuid is null ";
		if (onlyFreeAddresses) {
			query += "and a.adrType=" + AddressType.FREI.getDbValue();
		}
		query += "order by a.institution, a.lastname, a.firstname";
		
		List<AddressNode> aNs = session.createQuery(query).list();

		// NOTICE: upper query returns entities multiple times, filter them !
		for (AddressNode aN : aNs) {
			if (!retList.contains(aN)) {
				retList.add(aN);
			}
		}
		return retList;
	}

	public List<AddressNode> getSubAddresses(String parentUuid, boolean fetchAddressLevel) {
		Session session = getSession();
		ArrayList<AddressNode> retList = new ArrayList<AddressNode>();

		String q = "from AddressNode aNode ";
		if (fetchAddressLevel) {
			q += "left join fetch aNode.t02AddressWork a " +
				 "left join fetch aNode.addressNodeChildren aChildren ";
		}
		q += "where aNode.fkAddrUuid = ? ";
		if (fetchAddressLevel) {
			q += "order by a.institution, a.lastname, a.firstname"; 
		}
		
		List<AddressNode> aNodes = session.createQuery(q)
				.setString(0, parentUuid)
				.list();

		// NOTICE: upper query returns entities multiple times, filter them !
		for (AddressNode aNode : aNodes) {
			if (!retList.contains(aNode)) {
				retList.add(aNode);
			}
		}
		return retList;
	}

	public AddressNode getAddrDetails(String uuid) {
		Session session = getSession();

		// fetch all at once (one select with outer joins)
		AddressNode aN = (AddressNode) session.createQuery("from AddressNode aNode " +
			"left join fetch aNode.t02AddressWork aWork " +
			"left join fetch aWork.t021Communications aComm " +

// TODO: FASTER WHITHOUT PRE FETCHING !!!??? Check when all is modeled !

			"where aNode.addrUuid = ?")
			.setString(0, uuid)
			.uniqueResult();

		return aN;
	}

	public List<ObjectNode> getObjectReferencesFrom(String addressUuid) {
		Session session = getSession();
		ArrayList<ObjectNode> retList = new ArrayList<ObjectNode>();

		// fetch all at once (one select with outer joins)
		List<ObjectNode> oNs = session.createQuery("from ObjectNode oNode " +
			"left join fetch oNode.t01ObjectWork oWork " +
			"left join fetch oWork.t012ObjAdrs objAdr " +
			"where objAdr.adrUuid = ?")
			.setString(0, addressUuid)
			.list();

		// NOTICE: upper query returns objects multiple times, filter them !
		for (ObjectNode oN : oNs) {
			if (!retList.contains(oN)) {
				retList.add(oN);
			}
		}
		return retList;
	}

	public AddressNode getParent(String uuid) {
		AddressNode parentNode = null;
		AddressNode aN = loadByUuid(uuid);
		if (aN != null && aN.getFkAddrUuid() != null) {
			parentNode = loadByUuid(aN.getFkAddrUuid());
		}
		
		return parentNode;
	}
}
