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

	private static final Logger LOG 	= Logger.getLogger(ConsistencyCheckerDaoHibernate.class);

	private List<QueryParameter> tableList 	= new ArrayList<QueryParameter>();
	
	public static String REF_TABLE_NAME	= "ref.table.name";
	
	public static String ELEMENT_ID 	= "element.id";
	
	public static String FOREIGN_KEY 	= "foreign.key";
	
	public static String TABLE_NAME 	= "table.name";
	
	public ConsistencyCheckerDaoHibernate(SessionFactory factory) {
        super(factory);
        
        initTablesToCheck();
    }

	/**
	 * Initialize a list of tables to be checked for correct associations.
	 * Each entry consists of three String elements:
	 *   1. the first table-name to check against
	 *   2. the reference from first table to second table
	 *   3. the column of second table which is the foreign key to the first table
	 *   4. the actual name of the second table
	 * The third String only is used for the solution, so that the column can be named
	 * that contains an id which does not exist in the first table.
	 */
	private void initTablesToCheck() {
		tableList.add(new QueryParameter("PermissionObj", "uuid", "ObjectNode", new String[]{"objUuid"}));
		tableList.add(new QueryParameter("T01Object", "id", "ObjectNode", new String[]{"objId","objIdPublished"}));
		
		tableList.add(new QueryParameter("ObjectAccess", "objId", "T01Object", new String[]{"id"}));
		tableList.add(new QueryParameter("ObjectComment", "objId", "T01Object", new String[]{"id"}));
		tableList.add(new QueryParameter("ObjectConformity", "objId", "T01Object", new String[]{"id"}));
		tableList.add(new QueryParameter("ObjectMetadata", "id", "T01Object", new String[]{"objMetadataId"}));
		tableList.add(new QueryParameter("ObjectReference", "objFromId", "T01Object", new String[]{"id"}));
		tableList.add(new QueryParameter("SearchtermObj", "objId", "T01Object", new String[]{"id"}));
		tableList.add(new QueryParameter("SpatialReference", "objId", "T01Object", new String[]{"id"}));
		tableList.add(new QueryParameter("T0110AvailFormat", "objId", "T01Object", new String[]{"id"}));
		tableList.add(new QueryParameter("T0112MediaOption", "objId", "T01Object", new String[]{"id"}));
		tableList.add(new QueryParameter("T0113DatasetReference", "objId", "T01Object", new String[]{"id"}));
		tableList.add(new QueryParameter("T0114EnvCategory", "objId", "T01Object", new String[]{"id"}));
		tableList.add(new QueryParameter("T0114EnvTopic", "objId", "T01Object", new String[]{"id"}));
		tableList.add(new QueryParameter("T011ObjDataPara", "objId", "T01Object", new String[]{"id"}));
		tableList.add(new QueryParameter("T011ObjData", "objId", "T01Object", new String[]{"id"}));
		tableList.add(new QueryParameter("T011ObjGeo", "objId", "T01Object", new String[]{"id"}));
		tableList.add(new QueryParameter("T011ObjLiterature", "objId", "T01Object", new String[]{"id"}));
		tableList.add(new QueryParameter("T011ObjProject", "objId", "T01Object", new String[]{"id"}));
		tableList.add(new QueryParameter("T011ObjServ", "objId", "T01Object", new String[]{"id"}));
		tableList.add(new QueryParameter("T011ObjTopicCat", "objId", "T01Object", new String[]{"id"}));
		tableList.add(new QueryParameter("T012ObjAdr", "objId", "T01Object", new String[]{"id"}));
		tableList.add(new QueryParameter("T014InfoImpart", "objId", "T01Object", new String[]{"id"}));
		tableList.add(new QueryParameter("T015Legist", "objId", "T01Object", new String[]{"id"}));
		tableList.add(new QueryParameter("T017UrlRef", "objId", "T01Object", new String[]{"id"}));
		tableList.add(new QueryParameter("T08Attr", "objId", "T01Object", new String[]{"id"}));
		
		tableList.add(new QueryParameter("FullIndexAddr", "addrNodeId", "AddressNode", new String[]{"id"}));
		tableList.add(new QueryParameter("PermissionAddr", "uuid", "AddressNode", new String[]{"addrUuid"}));
		tableList.add(new QueryParameter("T02Address", "id", "AddressNode", new String[]{"addrId","addrIdPublished"}));
		
		tableList.add(new QueryParameter("AddressComment", "addrId", "T02Address", new String[]{"id"}));
		tableList.add(new QueryParameter("AddressMetadata", "id", "T02Address", new String[]{"addrMetadataId"}));
		tableList.add(new QueryParameter("SearchtermAdr", "adrId", "T02Address", new String[]{"id"}));
		tableList.add(new QueryParameter("T021Communication", "adrId", "T02Address", new String[]{"id"}));
		
		tableList.add(new QueryParameter("T08AttrList", "attrTypeId", "T08AttrType", new String[]{"id"}));
		
		tableList.add(new QueryParameter("T011ObjGeoKeyc", "objGeoId", "T011ObjGeo", new String[]{"id"}));
		tableList.add(new QueryParameter("T011ObjGeoSymc", "objGeoId", "T011ObjGeo", new String[]{"id"}));
		tableList.add(new QueryParameter("T011ObjGeoScale", "objGeoId", "T011ObjGeo", new String[]{"id"}));
		tableList.add(new QueryParameter("T011ObjGeoSupplinfo", "objGeoId", "T011ObjGeo", new String[]{"id"}));
		tableList.add(new QueryParameter("T011ObjGeoVector", "objGeoId", "T011ObjGeo", new String[]{"id"}));
		tableList.add(new QueryParameter("T011ObjGeoSpatialRep", "objGeoId", "T011ObjGeo", new String[]{"id"}));
		
		tableList.add(new QueryParameter("T011ObjServType", "objServId", "T011ObjServ", new String[]{"id"}));
		tableList.add(new QueryParameter("T011ObjServVersion", "objServId", "T011ObjServ", new String[]{"id"}));
		tableList.add(new QueryParameter("T011ObjServOperation", "objServId", "T011ObjServ", new String[]{"id"}));
		tableList.add(new QueryParameter("T011ObjServScale", "objServId", "T011ObjServ", new String[]{"id"}));
		
		tableList.add(new QueryParameter("T011ObjServOpPlatform", "objServOpId", "T011ObjServOperation", new String[]{"id"}));
		tableList.add(new QueryParameter("T011ObjServOpConnpoint", "objServOpId", "T011ObjServOperation", new String[]{"id"}));
		tableList.add(new QueryParameter("T011ObjServOpPara", "objServOpId", "T011ObjServOperation", new String[]{"id"}));
		tableList.add(new QueryParameter("T011ObjServOpDepends", "objServOpId", "T011ObjServOperation", new String[]{"id"}));
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
		
		for (QueryParameter queryPar : tableList) {
			List<Long> resultList = getSession().createQuery(createQueryString(queryPar)).list();
			if (!resultList.isEmpty()) {
				for (Long elementId : resultList) {
					IngridDocument document = new IngridDocument();
					document.put(ELEMENT_ID, elementId);
					// table name is only correct for right outer joins!!!
					// createQueryString(...) makes sure of it
					document.put(FOREIGN_KEY, queryPar.getSrcField());
					document.put(TABLE_NAME, queryPar.getFirstTable());
					document.put(REF_TABLE_NAME, queryPar.getSecondTable());
					docList.add(document);
				}
			}
		}
		return docList;
	}

	/**
	 * Create a query string that can be sent to the database. It returns the Ids
	 * of a table that has invalid references to another table. Checked is a column
	 * of one table with one or more tables of another. 
	 * @param queryPar
	 * @return ids of invalid entries in a table
	 */
	private String createQueryString(QueryParameter queryPar) {
		String hqlQuery = "select element.id from "+queryPar.getFirstTable()+" element " +
				"where ";
		int i = 0;
		for (String type : queryPar.getTableFields()) {
			if (++i > 1) hqlQuery += " and ";
			hqlQuery +=	"element."+queryPar.getSrcField()+" not in (select node."+type +
				" from "+queryPar.getSecondTable()+" node)";
		}
		
		return hqlQuery;
	}
	

	/**
	 * This inner class encapsulates the parameters used for a query to the database. 
	 * @author Andre
	 *
	 */
	class QueryParameter {
		private String firstTable;
		
		private String secondTable;
		
		private String srcField;
		
		private List<String> tableFields;
		
		public QueryParameter( String firstTable, String srcField, String secondTable, String[] fields ) {
			this.firstTable = firstTable;
			this.secondTable = secondTable;
			this.setSrcField(srcField);
			this.tableFields = new ArrayList();
			for (String field : fields) {
				this.tableFields.add(field);
			}			
		}

		public void setFirstTable(String firstTable) {
			this.firstTable = firstTable;
		}

		public String getFirstTable() {
			return firstTable;
		}

		public void setSecondTable(String secondTable) {
			this.secondTable = secondTable;
		}

		public String getSecondTable() {
			return secondTable;
		}

		public void setTableFields(List<String> tableFields) {
			this.tableFields = tableFields;
		}

		public List<String> getTableFields() {
			return tableFields;
		}

		public void setSrcField(String srcField) {
			this.srcField = srcField;
		}

		public String getSrcField() {
			return srcField;
		}
	}
}
