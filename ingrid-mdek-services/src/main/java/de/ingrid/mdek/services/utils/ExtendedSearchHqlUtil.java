/**
 * 
 */
package de.ingrid.mdek.services.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.services.persistence.db.dao.hibernate.IFullIndexAccess;
import de.ingrid.utils.IngridDocument;
import de.ingrid.utils.query.ClauseQuery;
import de.ingrid.utils.query.IngridQuery;
import de.ingrid.utils.query.TermQuery;
import de.ingrid.utils.queryparser.ParseException;
import de.ingrid.utils.queryparser.QueryStringParser;

/**
 * @author Administrator
 *
 */
public class ExtendedSearchHqlUtil implements IFullIndexAccess {

	public static String createObjectExtendedSearchQuery(IngridDocument searchParams) {
		
		if (searchParams == null) {
			return null;
		}
		
		StringBuilder fromString = new StringBuilder("from ObjectNode oNode inner join oNode.t01ObjectWork obj");
		StringBuilder whereString = new StringBuilder("");
		
		String queryTerm = searchParams.getString(MdekKeys.QUERY_TERM);
		int relation = (Integer)searchParams.get(MdekKeys.RELATION);
		// parse queryTerm to extract multiple search entries and phrase tokens
		String[] searchTerms = getSearchTerms(queryTerm);
		if (searchTerms.length > 0) {
			fromString.append(" inner join oNode.fullIndexObjs fidx");
			whereString.append(" fidx.idxName = '" + IDX_NAME_FULLTEXT + "' and (");
			String op;
			if (relation == 0) {
				op = " and ";
			} else {
				op = " or ";
			}
			for (String term : searchTerms) {
				whereString.append("fidx.idxValue like '%").append(term).append("%'").append(op);
			}
			whereString.delete(whereString.lastIndexOf(op), whereString.length());
			whereString.append(")");
		}
		
		List<Integer> objClasses = (List<Integer>)searchParams.get(MdekKeys.OBJ_CLASSES);
		if (objClasses != null && objClasses.size() > 0) {
			if (whereString.length() > 0) {
				whereString.append(" and");
			}
			whereString.append(" (");
			for (Integer objClass : objClasses) {
				whereString.append("obj.objClass = ").append(objClass).append(" or ");
			}
			whereString.delete(whereString.lastIndexOf(" or "), whereString.length());
			whereString.append(")");
		}
		
		List<IngridDocument> thesaurusTerms = (List<IngridDocument>)searchParams.get(MdekKeys.THESAURUS_TERMS);
		if (thesaurusTerms != null && thesaurusTerms.size() > 0) {
			int thesaurusRelation = (Integer)searchParams.get(MdekKeys.THESAURUS_RELATION);
			fromString.append(" inner join obj.searchtermObjs stObjs inner join stObjs.searchtermValue stVal inner join stVal.searchtermSns stSns");
			if (whereString.length() > 0) {
				whereString.append(" and");
			}
			whereString.append(" stVal.type='T' and (");
			String op;
			if (thesaurusRelation == 0) {
				op = " and ";
			} else {
				op = " or ";
			}
			for (IngridDocument thesTermDoc : thesaurusTerms) {
				whereString.append("stSns.snsId = '").append(thesTermDoc.getString(MdekKeys.TERM_SNS_ID)).append("'").append(op);
			}
			whereString.delete(whereString.lastIndexOf(op), whereString.length());
			whereString.append(")");
		}

		List<IngridDocument> geoThesaurusTerms = (List<IngridDocument>)searchParams.get(MdekKeys.GEO_THESAURUS_TERMS);
		if (geoThesaurusTerms != null && geoThesaurusTerms.size() > 0) {
			int geoThesaurusRelation = (Integer)searchParams.get(MdekKeys.GEO_THESAURUS_RELATION);
			fromString.append(" inner join obj.spatialReferences spcRefs inner join spcRefs.spatialRefValue spcRefVal inner join spcRefVal.spatialRefSns spcRefSns");
			if (whereString.length() > 0) {
				whereString.append(" and");
			}
			whereString.append(" spcRefVal.type='G' and (");
			String op;
			if (geoThesaurusRelation == 0) {
				op = " and ";
			} else {
				op = " or ";
			}
			for (IngridDocument geoThesTermDoc : geoThesaurusTerms) {
				whereString.append("spcRefSns.snsId LIKE '").append(geoThesTermDoc.getString(MdekKeys.LOCATION_SNS_ID)).append("%'").append(op);
			}
			whereString.delete(whereString.lastIndexOf(op), whereString.length());
			whereString.append(")");
		}
		
		Integer customLocation = (Integer)searchParams.get(MdekKeys.CUSTOM_LOCATION);
		if (customLocation != null) {
			if (fromString.indexOf("spcRefVal") == -1) {
				fromString.append(" inner join obj.spatialReferences spcRefs inner join spcRefs.spatialRefValue spcRefVal");
			}
			if (whereString.length() > 0) {
				whereString.append(" and");
			}
			whereString.append(" spcRefVal.nameKey = ").append(customLocation);
		}

		String timeFrom = searchParams.getString(MdekKeys.TIME_FROM);
		String timeTo = searchParams.getString(MdekKeys.TIME_TO);
		String timeAt = searchParams.getString(MdekKeys.TIME_AT);
		Boolean timeIntersect = (Boolean)searchParams.get(MdekKeys.TIME_INTERSECT);
		Boolean timeContains = (Boolean)searchParams.get(MdekKeys.TIME_CONTAINS);
		if (timeFrom != null && timeFrom.length() == 0) {
			timeFrom = null;
		}
		if (timeTo != null && timeTo.length() == 0) {
			timeTo = null;
		}
		if (timeAt != null && timeAt.length() == 0) {
			timeAt = null;
		}
		if (timeIntersect == null) {
			timeIntersect = false;
		}
		if (timeContains == null) {
			timeContains = false;
		}
		
		if (timeFrom != null && timeTo != null) {
			if (whereString.length() > 0) {
				whereString.append(" and");
			}
			whereString.append(" ((obj.timeFrom >= '").append(timeFrom).append("' and obj.timeTo <= '").append(timeTo).append("')");
			if (timeContains) {
				whereString.append(" or (obj.timeFrom <= '").append(timeFrom).append("' and obj.timeTo >= '").append(timeTo).append("')");
			}
			if (timeIntersect) {
				whereString.append(" or (obj.timeFrom >= '").append(timeFrom).append("' and obj.timeTo >= '").append(timeTo).append("')");
				whereString.append(" or (obj.timeFrom <= '").append(timeFrom).append("' and obj.timeTo <= '").append(timeTo).append("')");
			}
			whereString.append(")");
		} else if (timeFrom != null && timeTo == null) {
			if (whereString.length() > 0) {
				whereString.append(" and");
			}
			whereString.append(" ((obj.timeFrom >= '").append(timeFrom).append("')");
			if (timeIntersect) {
				whereString.append(" or (obj.timeFrom <= '").append(timeFrom).append("' and obj.timeTo >= '").append(timeFrom).append("')");
			}
			whereString.append(")");
		} else if (timeFrom == null && timeTo != null) {
			if (whereString.length() > 0) {
				whereString.append(" and");
			}
			whereString.append(" ((obj.timeTo <= '").append(timeTo).append("')");
			if (timeIntersect) {
				whereString.append(" or (obj.timeTo >= '").append(timeTo).append("' and obj.timeFrom <= '").append(timeTo).append("')");
			}
			whereString.append(")");
		} else if (timeAt != null) {
			if (whereString.length() > 0) {
				whereString.append(" and");
			}
			whereString.append(" obj.timeFrom == '").append(timeAt).append("%' and obj.timeType == 'am'");
		}
		
		String qString = fromString.append(" where").append(whereString).toString();

		return qString;
		
	}

	public static String createAddressExtendedSearchQuery(IngridDocument searchParams) {

		if (searchParams == null) {
			return null;
		}
		
		StringBuilder fromString = new StringBuilder("from AddressNode aNode inner join aNode.t02AddressWork addr");
		StringBuilder whereString = new StringBuilder("");
		
		String queryTerm = searchParams.getString(MdekKeys.QUERY_TERM);
		Integer relation = (Integer)searchParams.get(MdekKeys.RELATION);
		Integer searchType = (Integer)searchParams.get(MdekKeys.SEARCH_TYPE);
		Integer searchRange = (Integer)searchParams.get(MdekKeys.SEARCH_RANGE);
		// parse queryTerm to extract multiple search entries and phrase tokens
		String[] searchTerms = getSearchTerms(queryTerm);
		if (searchTerms.length > 0) {
			fromString.append(" inner join aNode.fullIndexAddrs fidx");
			if (searchRange == null || searchRange == 0) {
				whereString.append(" fidx.idxName = '").append(IDX_NAME_FULLTEXT).append("' and (");
			} else {
				whereString.append(" fidx.idxName = '").append(IDX_NAME_PARTIAL).append("' and (");
			}
			String op;
			if (relation == null || relation == 0) {
				op = " and ";
			} else {
				op = " or ";
			}
			for (String term : searchTerms) {
				if (searchType == null || searchType == 0) {
					whereString.append("(fidx.idxValue like '% ").append(term).append(" %' or fidx.idxValue like '%|").append(term).append("%|' or fidx.idxValue like '%|").append(term).append(" %' or fidx.idxValue like '%").append(term).append("|%')").append(op);
				} else {
					whereString.append("fidx.idxValue like '%").append(term).append("%'").append(op);
				}
			}
			whereString.delete(whereString.lastIndexOf(op), whereString.length());
			whereString.append(")");
		}
		
		
		String street = searchParams.getString(MdekKeys.STREET);
		if (street != null && street.length() > 0) {
			if (whereString.length() > 0) {
				whereString.append(" and");
			}
			whereString.append(" addr.street like '%").append(street).append("%'");
		}
		String city = searchParams.getString(MdekKeys.CITY);
		if (city != null && city.length() > 0) {
			if (whereString.length() > 0) {
				whereString.append(" and");
			}
			whereString.append(" addr.city like '%").append(city).append("%'");
		}
		String postalCode = searchParams.getString(MdekKeys.POSTAL_CODE);
		if (postalCode != null && postalCode.length() > 0) {
			if (whereString.length() > 0) {
				whereString.append(" and");
			}
			whereString.append(" (addr.postcode like '%").append(postalCode).append("%' or addr.postboxPc like '%").append(postalCode).append("%')");
		}
		
		String qString = fromString.append(" where").append(whereString).toString();

		return qString;
		
	}
	
	private static String[] getSearchTerms(String qString) {
		IngridQuery q;
		
		try {
			q = QueryStringParser.parse(qString);
			TermQuery[] queries = getAllTerms(q);
			String[] result = new String[queries.length];
			for (int i=0; i<queries.length; i++) {
				if (queries[i].getTerm().startsWith("\"") && queries[i].getTerm().endsWith("\"")) {
					result[i] = queries[i].getTerm().substring(1).substring(0, queries[i].getTerm().lastIndexOf("\""));
				} else {
					result[i] = queries[i].getTerm();
				}
			}
			return result;
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return new String[] {};
		}
		
	}

	
    /**
     * Get all terms in Query. NOTICE: If multiple TermQuerys contain the same
     * term, every TermQuery is returned (may differ in "required",
     * "prohibited"). To remove "double" terms use removeDoubleTerms(...).
     * 
     * @param q
     * @return
     */
    private static TermQuery[] getAllTerms(IngridQuery q) {
        ArrayList<TermQuery> result = new ArrayList<TermQuery>();
        TermQuery[] terms = q.getTerms();
        for (int i = 0; i < terms.length; i++) {
            if (terms[i].getType() == TermQuery.TERM) {
                result.add(terms[i]);
            }
        }
        ClauseQuery[] clauses = q.getClauses();
        for (int i = 0; i < clauses.length; i++) {
            result.addAll(Arrays.asList(getAllTerms(clauses[i])));
        }

        return ((TermQuery[]) result.toArray(new TermQuery[result.size()]));
    }
	
	
}
