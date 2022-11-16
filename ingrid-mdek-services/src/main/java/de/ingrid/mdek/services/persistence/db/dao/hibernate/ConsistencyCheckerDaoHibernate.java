/*
 * **************************************************-
 * ingrid-mdek-services
 * ==================================================
 * Copyright (C) 2014 - 2022 wemove digital solutions GmbH
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

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.transform.DistinctRootEntityResultTransformer;

import de.ingrid.mdek.MdekUtils.AddressType;
import de.ingrid.mdek.services.persistence.db.TransactionService;
import de.ingrid.mdek.services.persistence.db.dao.IConsistencyCheckerDao;
import de.ingrid.mdek.services.persistence.db.model.AdditionalFieldData;
import de.ingrid.mdek.services.persistence.db.model.AddressComment;
import de.ingrid.mdek.services.persistence.db.model.AddressMetadata;
import de.ingrid.mdek.services.persistence.db.model.AddressNode;
import de.ingrid.mdek.services.persistence.db.model.FullIndexAddr;
import de.ingrid.mdek.services.persistence.db.model.ObjectAccess;
import de.ingrid.mdek.services.persistence.db.model.ObjectComment;
import de.ingrid.mdek.services.persistence.db.model.ObjectConformity;
import de.ingrid.mdek.services.persistence.db.model.ObjectMetadata;
import de.ingrid.mdek.services.persistence.db.model.ObjectNode;
import de.ingrid.mdek.services.persistence.db.model.ObjectReference;
import de.ingrid.mdek.services.persistence.db.model.ObjectTypesCatalogue;
import de.ingrid.mdek.services.persistence.db.model.PermissionAddr;
import de.ingrid.mdek.services.persistence.db.model.PermissionObj;
import de.ingrid.mdek.services.persistence.db.model.SearchtermAdr;
import de.ingrid.mdek.services.persistence.db.model.SearchtermObj;
import de.ingrid.mdek.services.persistence.db.model.SpatialReference;
import de.ingrid.mdek.services.persistence.db.model.T0110AvailFormat;
import de.ingrid.mdek.services.persistence.db.model.T0112MediaOption;
import de.ingrid.mdek.services.persistence.db.model.T0113DatasetReference;
import de.ingrid.mdek.services.persistence.db.model.T0114EnvTopic;
import de.ingrid.mdek.services.persistence.db.model.T011ObjData;
import de.ingrid.mdek.services.persistence.db.model.T011ObjDataPara;
import de.ingrid.mdek.services.persistence.db.model.T011ObjGeo;
import de.ingrid.mdek.services.persistence.db.model.T011ObjGeoScale;
import de.ingrid.mdek.services.persistence.db.model.T011ObjGeoSupplinfo;
import de.ingrid.mdek.services.persistence.db.model.T011ObjGeoSymc;
import de.ingrid.mdek.services.persistence.db.model.T011ObjGeoVector;
import de.ingrid.mdek.services.persistence.db.model.T011ObjLiterature;
import de.ingrid.mdek.services.persistence.db.model.T011ObjProject;
import de.ingrid.mdek.services.persistence.db.model.T011ObjServ;
import de.ingrid.mdek.services.persistence.db.model.T011ObjServOpConnpoint;
import de.ingrid.mdek.services.persistence.db.model.T011ObjServOpDepends;
import de.ingrid.mdek.services.persistence.db.model.T011ObjServOpPara;
import de.ingrid.mdek.services.persistence.db.model.T011ObjServOpPlatform;
import de.ingrid.mdek.services.persistence.db.model.T011ObjServOperation;
import de.ingrid.mdek.services.persistence.db.model.T011ObjServScale;
import de.ingrid.mdek.services.persistence.db.model.T011ObjServType;
import de.ingrid.mdek.services.persistence.db.model.T011ObjServVersion;
import de.ingrid.mdek.services.persistence.db.model.T011ObjTopicCat;
import de.ingrid.mdek.services.persistence.db.model.T012ObjAdr;
import de.ingrid.mdek.services.persistence.db.model.T014InfoImpart;
import de.ingrid.mdek.services.persistence.db.model.T015Legist;
import de.ingrid.mdek.services.persistence.db.model.T017UrlRef;
import de.ingrid.mdek.services.persistence.db.model.T01Object;
import de.ingrid.mdek.services.persistence.db.model.T021Communication;
import de.ingrid.mdek.services.persistence.db.model.T02Address;
import de.ingrid.utils.IngridDocument;

/**
 * Generic HQL operations.
 * 
 * @author Martin
 */
public class ConsistencyCheckerDaoHibernate
	extends TransactionService
	implements IConsistencyCheckerDao {

	private static final Logger LOG 	= LogManager.getLogger(ConsistencyCheckerDaoHibernate.class);

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

		// NOTICE: Extract names from Java Objects wherever possible to get compiler errors when database model changes !!!!!!!!!

		String objectNode = ObjectNode.class.getSimpleName();
		String t01Object = T01Object.class.getSimpleName();
		tableList.add(new QueryParameter(PermissionObj.class.getSimpleName(), "uuid", objectNode, new String[]{"objUuid"}));
		tableList.add(new QueryParameter(t01Object, "id", objectNode, new String[]{"objId","objIdPublished"}));
		
		tableList.add(new QueryParameter(ObjectAccess.class.getSimpleName(), "objId", t01Object, new String[]{"id"}));
		tableList.add(new QueryParameter(ObjectComment.class.getSimpleName(), "objId", t01Object, new String[]{"id"}));
		tableList.add(new QueryParameter(ObjectConformity.class.getSimpleName(), "objId", t01Object, new String[]{"id"}));
		tableList.add(new QueryParameter(ObjectMetadata.class.getSimpleName(), "id", t01Object, new String[]{"objMetadataId"}));
		tableList.add(new QueryParameter(ObjectReference.class.getSimpleName(), "objFromId", t01Object, new String[]{"id"}));
		tableList.add(new QueryParameter(SearchtermObj.class.getSimpleName(), "objId", t01Object, new String[]{"id"}));
		tableList.add(new QueryParameter(SpatialReference.class.getSimpleName(), "objId", t01Object, new String[]{"id"}));
		tableList.add(new QueryParameter(T0110AvailFormat.class.getSimpleName(), "objId", t01Object, new String[]{"id"}));
		tableList.add(new QueryParameter(T0112MediaOption.class.getSimpleName(), "objId", t01Object, new String[]{"id"}));
		tableList.add(new QueryParameter(T0113DatasetReference.class.getSimpleName(), "objId", t01Object, new String[]{"id"}));
		tableList.add(new QueryParameter(T0114EnvTopic.class.getSimpleName(), "objId", t01Object, new String[]{"id"}));
		tableList.add(new QueryParameter(T011ObjDataPara.class.getSimpleName(), "objId", t01Object, new String[]{"id"}));
		tableList.add(new QueryParameter(T011ObjData.class.getSimpleName(), "objId", t01Object, new String[]{"id"}));
		tableList.add(new QueryParameter(T011ObjGeo.class.getSimpleName(), "objId", t01Object, new String[]{"id"}));
		tableList.add(new QueryParameter(T011ObjLiterature.class.getSimpleName(), "objId", t01Object, new String[]{"id"}));
		tableList.add(new QueryParameter(T011ObjProject.class.getSimpleName(), "objId", t01Object, new String[]{"id"}));
		tableList.add(new QueryParameter(T011ObjServ.class.getSimpleName(), "objId", t01Object, new String[]{"id"}));
		tableList.add(new QueryParameter(T011ObjTopicCat.class.getSimpleName(), "objId", t01Object, new String[]{"id"}));
		tableList.add(new QueryParameter(T012ObjAdr.class.getSimpleName(), "objId", t01Object, new String[]{"id"}));
		tableList.add(new QueryParameter(T014InfoImpart.class.getSimpleName(), "objId", t01Object, new String[]{"id"}));
		tableList.add(new QueryParameter(T015Legist.class.getSimpleName(), "objId", t01Object, new String[]{"id"}));
		tableList.add(new QueryParameter(T017UrlRef.class.getSimpleName(), "objId", t01Object, new String[]{"id"}));
		tableList.add(new QueryParameter(AdditionalFieldData.class.getSimpleName(), "objId", t01Object, new String[]{"id"}));
		
		String addressNode = AddressNode.class.getSimpleName();
		String t02Address = T02Address.class.getSimpleName();
		tableList.add(new QueryParameter(FullIndexAddr.class.getSimpleName(), "addrNodeId", addressNode, new String[]{"id"}));
		tableList.add(new QueryParameter(PermissionAddr.class.getSimpleName(), "uuid", addressNode, new String[]{"addrUuid"}));
		tableList.add(new QueryParameter(t02Address, "id", addressNode, new String[]{"addrId","addrIdPublished"}));
		
		tableList.add(new QueryParameter(AddressComment.class.getSimpleName(), "addrId", t02Address, new String[]{"id"}));
		tableList.add(new QueryParameter(AddressMetadata.class.getSimpleName(), "id", t02Address, new String[]{"addrMetadataId"}));
		tableList.add(new QueryParameter(SearchtermAdr.class.getSimpleName(), "adrId", t02Address, new String[]{"id"}));
		tableList.add(new QueryParameter(T021Communication.class.getSimpleName(), "adrId", t02Address, new String[]{"id"}));
		
		String t011ObjGeo = T011ObjGeo.class.getSimpleName();
		tableList.add(new QueryParameter(ObjectTypesCatalogue.class.getSimpleName(), "objId", t01Object, new String[]{"id"}));
		tableList.add(new QueryParameter(T011ObjGeoSymc.class.getSimpleName(), "objGeoId", t011ObjGeo, new String[]{"id"}));
		tableList.add(new QueryParameter(T011ObjGeoScale.class.getSimpleName(), "objGeoId", t011ObjGeo, new String[]{"id"}));
		tableList.add(new QueryParameter(T011ObjGeoSupplinfo.class.getSimpleName(), "objGeoId", t011ObjGeo, new String[]{"id"}));
		tableList.add(new QueryParameter(T011ObjGeoVector.class.getSimpleName(), "objGeoId", t011ObjGeo, new String[]{"id"}));
		
		String t011ObjServ = T011ObjServ.class.getSimpleName();
		tableList.add(new QueryParameter(T011ObjServType.class.getSimpleName(), "objServId", t011ObjServ, new String[]{"id"}));
		tableList.add(new QueryParameter(T011ObjServVersion.class.getSimpleName(), "objServId", t011ObjServ, new String[]{"id"}));
		tableList.add(new QueryParameter(T011ObjServOperation.class.getSimpleName(), "objServId", t011ObjServ, new String[]{"id"}));
		tableList.add(new QueryParameter(T011ObjServScale.class.getSimpleName(), "objServId", t011ObjServ, new String[]{"id"}));
		
		String t011ObjServOperation = T011ObjServOperation.class.getSimpleName();
		tableList.add(new QueryParameter(T011ObjServOpPlatform.class.getSimpleName(), "objServOpId", t011ObjServOperation, new String[]{"id"}));
		tableList.add(new QueryParameter(T011ObjServOpConnpoint.class.getSimpleName(), "objServOpId", t011ObjServOperation, new String[]{"id"}));
		tableList.add(new QueryParameter(T011ObjServOpPara.class.getSimpleName(), "objServOpId", t011ObjServOperation, new String[]{"id"}));
		tableList.add(new QueryParameter(T011ObjServOpDepends.class.getSimpleName(), "objServOpId", t011ObjServOperation, new String[]{"id"}));
	}

	public List<AddressNode> checkAddressHierarchy() {
		String hqlQuery = "" +
				"from AddressNode adrNode " +
				"where " +
				// exclude hidden user addresses !
				AddressType.getHQLExcludeIGEUsersViaNode("adrNode", null) +
				" AND adrNode.fkAddrUuid not in ( select adrNode.addrUuid from adrNode )";
		
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
		String hqlQuery = "select obj " +
				"from T01Object obj " +
				"left outer join obj.t012ObjAdrs objAdr " +
				"where obj.objClass != 1000 AND objAdr = null";
		// DEPRECATED: NO CHECK OF VERWALTER ANYMORE, just any address needed, see INGRID32-46
/*
				"or (objAdr.type != " + MdekUtils.OBJ_ADR_TYPE_VERWALTER_ID +
				" AND objAdr.objId not in ( select objAdr.objId from objAdr " +
				// NOTICE: group by removed when porting to ORACLE !!!
				"where objAdr.type = " + MdekUtils.OBJ_ADR_TYPE_VERWALTER_ID +
				"))";
*/

		List<T01Object> resultList = getSession().createQuery(hqlQuery)
			.setResultTransformer(DistinctRootEntityResultTransformer.INSTANCE)
			.list();
		
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
