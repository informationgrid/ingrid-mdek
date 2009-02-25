package de.ingrid.mdek.services.persistence.db.dao.hibernate;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;

import de.ingrid.mdek.services.persistence.db.TransactionService;
import de.ingrid.mdek.services.persistence.db.dao.IConsistencyCheckerDao;
import de.ingrid.mdek.services.persistence.db.model.AddressNode;
import de.ingrid.mdek.services.persistence.db.model.ObjectNode;
import de.ingrid.mdek.services.persistence.db.model.T012ObjAdr;
import de.ingrid.mdek.services.persistence.db.model.T01Object;
import de.ingrid.utils.IngridDocument;

/**
 * Generic HQL operations.
 * 
 * @author Martin
 */
public class ConsistencyCheckerDaoHibernate
	extends TransactionService
	implements IConsistencyCheckerDao {

	private static final Logger LOG = Logger.getLogger(ConsistencyCheckerDaoHibernate.class);

	private List<String[]> tableList = new ArrayList<String[]>();
	
	private static final int TABLE = 0;
	
	private static final int REF_TABLE = 1;
	
	public static String TABLE_NAME = "table.name";
	
	public static String ELEMENT_ID = "element.id";
	
	public static String FOREIGN_KEY = "foreign.key";
	
	public ConsistencyCheckerDaoHibernate(SessionFactory factory) {
        super(factory);
        
        initTablesToCheck();
    }

	/**
	 * Initialize a list of tables to be checked for correct associations.
	 * Each entry consists of three String elements:
	 *   1. the first table-name to check against
	 *   2. the second table-name
	 *   3. the column of second table which is the foreign key to the first table
	 * The third String only is used for the solution, so that the column can be named
	 * that contains an id which does not exist in the first table.
	 */
	private void initTablesToCheck() {
		tableList.add(new String[]{"ObjectNode", "permissionObjs", "objUuid"});
		tableList.add(new String[]{"ObjectNode", "t01ObjectPublished", "id"});
		tableList.add(new String[]{"ObjectNode", "t01ObjectWork", "id"});
		
		// 0..* relationship
		//tableList.add(new String[]{"T01Object", "addressNodeMod", "id"});
		//tableList.add(new String[]{"T01Object", "addressNodeResponsible", "id"});
		//tableList.add(new String[]{"T01Object", "t03Catalogue", "id"});		
		tableList.add(new String[]{"T01Object", "objectAccesss", "objId"});
		tableList.add(new String[]{"T01Object", "objectComments", "objId"});
		tableList.add(new String[]{"T01Object", "objectConformitys", "objId"});
		tableList.add(new String[]{"T01Object", "objectMetadata", "id"});
		tableList.add(new String[]{"T01Object", "objectReferences", "objFromId"});
		tableList.add(new String[]{"T01Object", "searchtermObjs", "objId"});
		tableList.add(new String[]{"T01Object", "spatialReferences", "objId"});
		tableList.add(new String[]{"T01Object", "t0110AvailFormats", "objId"});
		tableList.add(new String[]{"T01Object", "t0112MediaOptions", "objId"});
		tableList.add(new String[]{"T01Object", "t0113DatasetReferences", "objId"});
		tableList.add(new String[]{"T01Object", "t0114EnvCategorys", "objId"});
		tableList.add(new String[]{"T01Object", "t0114EnvTopics", "objId"});
		tableList.add(new String[]{"T01Object", "t011ObjDataParas", "objId"});
		tableList.add(new String[]{"T01Object", "t011ObjDatas", "objId"});
		tableList.add(new String[]{"T01Object", "t011ObjGeos", "objId"});
		tableList.add(new String[]{"T01Object", "t011ObjLiteratures", "objId"});
		tableList.add(new String[]{"T01Object", "t011ObjProjects", "objId"});
		tableList.add(new String[]{"T01Object", "t011ObjServs", "objId"});
		tableList.add(new String[]{"T01Object", "t011ObjTopicCats", "objId"});
		tableList.add(new String[]{"T01Object", "t012ObjAdrs", "objId"});
		tableList.add(new String[]{"T01Object", "t014InfoImparts", "objId"});
		tableList.add(new String[]{"T01Object", "t015Legists", "objId"});
		tableList.add(new String[]{"T01Object", "t017UrlRefs", "objId"});
		tableList.add(new String[]{"T01Object", "t08Attrs", "objId"});
		
		//tableList.add(new String[]{"AddressNode", "addressNodeChildren", "id"});
		tableList.add(new String[]{"AddressNode", "fullIndexAddrs", "addrNodeId"});
		tableList.add(new String[]{"AddressNode", "permissionAddrs", "addrUuid"});
		tableList.add(new String[]{"AddressNode", "t012ObjAdrs", "addrUuid"});		
		tableList.add(new String[]{"AddressNode", "t02AddressPublished", "id"});
		tableList.add(new String[]{"AddressNode", "t02AddressWork", "id"});
		
		tableList.add(new String[]{"T02Address", "addressComments", "addrId"});
		tableList.add(new String[]{"T02Address", "addressMetadata", "id"});
		//tableList.add(new String[]{"T02Address", "addressNodeMod", "id"});
		//tableList.add(new String[]{"T02Address", "addressNodeResponsible", "id"});
		tableList.add(new String[]{"T02Address", "searchtermAdrs", "adrId"});
		tableList.add(new String[]{"T02Address", "t021Communications", "adrId"});
		
		tableList.add(new String[]{"T08AttrType", "t08AttrLists","attrTypeId"});
		
		// T03Catalogue has 0..* relationship with T01Object
		//tableList.add(new String[]{"T03Catalogue", "spatialRefValue","id"});
		
		tableList.add(new String[]{"T011ObjGeo", "t011ObjGeoKeycs", "objGeoId"});
		tableList.add(new String[]{"T011ObjGeo", "t011ObjGeoSymcs", "objGeoId"});
		tableList.add(new String[]{"T011ObjGeo", "t011ObjGeoScales", "objGeoId"});
		tableList.add(new String[]{"T011ObjGeo", "t011ObjGeoSupplinfos", "objGeoId"});
		tableList.add(new String[]{"T011ObjGeo", "t011ObjGeoVectors", "objGeoId"});
		tableList.add(new String[]{"T011ObjGeo", "t011ObjGeoSpatialReps", "objGeoId"});
		
		tableList.add(new String[]{"T011ObjServ", "t011ObjServTypes", "objServId"});
		tableList.add(new String[]{"T011ObjServ", "t011ObjServVersions", "objServId"});
		tableList.add(new String[]{"T011ObjServ", "t011ObjServOperations", "objServId"});
		tableList.add(new String[]{"T011ObjServ", "t011ObjServScales", "objServId"});
		
		tableList.add(new String[]{"T011ObjServOperation", "t011ObjServOpPlatforms", "objServOpId"});
		tableList.add(new String[]{"T011ObjServOperation", "t011ObjServOpConnpoints", "objServOpId"});
		tableList.add(new String[]{"T011ObjServOperation", "t011ObjServOpParas", "objServOpId"});
		tableList.add(new String[]{"T011ObjServOperation", "t011ObjServOpDependss", "objServOpId"});
		
		//tableList.add(new String[]{"AddressNode", "idc_user", "id"});
		
		//tableList.add(new String[]{"SpatialRefValue", "spatialRefSns", "id"});
		
		//tableList.add(new String[]{"IdcUser", "addressNode", "id"});
		//tableList.add(new String[]{"IdcUser", "idcGroup", "id"});
		//tableList.add(new String[]{"IdcUser", "idcUsers", "id"});
	}

	public List<AddressNode> checkAddressHierarchy() {
		String hqlQuery = "" +
				"from AddressNode adrNode " +
				"where adrNode.fkAddrUuid " +
				"not in ( select adrNode.addrUuid from adrNode )";
		
		List<AddressNode> resultList = getSession().createQuery(hqlQuery).list();
		return resultList;
	}

	public List<T012ObjAdr> checkAddressReferences() {
		String hqlQuery = "select objAdr " +
				"from AddressNode aNode " +
				"right outer join aNode.t012ObjAdrs objAdr " +
				"where aNode.addrId IS NULL";

		List<T012ObjAdr> resultList = getSession().createQuery(hqlQuery).list();
		
		return resultList;
	}

	public List<T01Object> checkInfoAddress() {
		String hqlQuery = "select objNode " +
				"from T01Object objNode " +
				"left outer join objNode.t012ObjAdrs objAdr " +
				"where objAdr = null " +
				"or (objAdr.type != 7 " +
				"AND objAdr.objId not in ( select objAdr.objId from objAdr " +
				"where objAdr.type = 7)) group by objAdr.objId";

		List<T01Object> resultList = getSession().createQuery(hqlQuery).list();
		
		return resultList;
	}

	public List<ObjectNode> checkObjectHierarchy() {
		String hqlQuery = "" +
				"from ObjectNode objNode " +
				"where objNode.fkObjUuid " +
				"not in ( select objNode.objUuid from objNode )";
	
		List<ObjectNode> resultList = getSession().createQuery(hqlQuery).list();
		return resultList;
	}

	public List<IngridDocument> checkTableAssociations() {
		List<IngridDocument> docList = new ArrayList<IngridDocument>();
		
		for (String[] queryData : tableList) {
			List<Long> resultList = getSession().createQuery(createQueryString(queryData)).list();
			if (!resultList.isEmpty()) {
				for (Long elementId : resultList) {
					IngridDocument document = new IngridDocument();
					document.put(ELEMENT_ID, elementId);
					// table name is only correct for right outer joins!!!
					// createQueryString(...) makes sure of it
					document.put(TABLE_NAME, queryData[1]);
					document.put(FOREIGN_KEY, queryData[2]);
					docList.add(document);
				}
			}
		}
		return docList;
	}

	private String createQueryString(String[] queryData) {
		String hqlQuery = "select element.id from "+queryData[TABLE]+" node " +
				"right outer join node."+queryData[REF_TABLE]+" element " +
				"where node = null";
		
		return hqlQuery;
	}

	
}
