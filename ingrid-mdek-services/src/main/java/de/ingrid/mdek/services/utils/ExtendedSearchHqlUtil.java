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
		
		String fromString = "from ObjectNode oNode inner join oNode.t01ObjectWork obj";
		String whereString = " where";
		
		String queryTerm = searchParams.getString(MdekKeys.QUERY_TERM);
		int relation = (Integer)searchParams.get(MdekKeys.RELATION);
		// parse queryTerm to extract multiple search entries and phrase tokens
		String[] searchTerms = getSearchTerms(queryTerm);
		if (searchTerms.length > 0) {
			fromString += " inner join oNode.fullIndexObjs fidx";
			whereString += " fidx.idxName = '" + IDX_NAME_FULLTEXT + "' and (";
			String op;
			if (relation == 0) {
				op = " and ";
			} else {
				op = " or ";
			}
			for (String term : searchTerms) {
				whereString += "fidx.idxValue like '%" + term + "%'" + op;
			}
			whereString = whereString.substring(0, whereString.lastIndexOf(op));
			whereString += ")";
		}
		
		List<Integer> objClasses = (List<Integer>)searchParams.get(MdekKeys.OBJ_CLASSES);
		if (objClasses != null && objClasses.size() > 0) {
			whereString += " and (";
			for (Integer objClass : objClasses) {
				whereString += "obj.objClass = " + objClass + " or ";
			}
			whereString = whereString.substring(0, whereString.lastIndexOf(" or "));
			whereString += ")";
		}
		
		List<IngridDocument> thesaurusTerms = (List<IngridDocument>)searchParams.get(MdekKeys.THESAURUS_TERMS);
		if (thesaurusTerms != null && thesaurusTerms.size() > 0) {
			int thesaurusRelation = (Integer)searchParams.get(MdekKeys.THESAURUS_RELATION);
			fromString += " inner join obj.searchtermObjs stObjs inner join stObjs.searchtermValue stVal inner join stVal.searchtermSns stSns";
			whereString += " and stVal.type='T' and (";
			String op;
			if (thesaurusRelation == 0) {
				op = " and ";
			} else {
				op = " or ";
			}
			for (IngridDocument thesTermDoc : thesaurusTerms) {
				whereString += "stSns.snsId = '" + thesTermDoc.getString(MdekKeys.TERM_SNS_ID) + "'" + op;
			}
			whereString = whereString.substring(0, whereString.lastIndexOf(op));
			whereString += ")";
		}

		List<IngridDocument> geoThesaurusTerms = (List<IngridDocument>)searchParams.get(MdekKeys.GEO_THESAURUS_TERMS);
		if (geoThesaurusTerms != null && geoThesaurusTerms.size() > 0) {
			int geoThesaurusRelation = (Integer)searchParams.get(MdekKeys.GEO_THESAURUS_RELATION);
			fromString += " inner join obj.spatialReferences spcRefs inner join spcRefs.spatialRefValue spcRefVal inner join spcRefVal.spatialRefSns spcRefSns";
			whereString += " and spcRefVal.type='G' and (";
			String op;
			if (geoThesaurusRelation == 0) {
				op = " and ";
			} else {
				op = " or ";
			}
			for (IngridDocument geoThesTermDoc : geoThesaurusTerms) {
				whereString += "spcRefSns.snsId LIKE '" + geoThesTermDoc.getString(MdekKeys.LOCATION_SNS_ID) + "%'" + op;
			}
			whereString = whereString.substring(0, whereString.lastIndexOf(op));
			whereString += ")";
		}
		
		Integer customLocation = (Integer)searchParams.get(MdekKeys.CUSTOM_LOCATION);
		if (customLocation != null) {
			if (fromString.indexOf("spcRefVal") == -1) {
				fromString += " inner join obj.spatialReferences spcRefs inner join spcRefs.spatialRefValue spcRefVal";
			}
			whereString += " and spcRefVal.nameKey = " + customLocation;
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
			whereString += " and ((obj.timeFrom >= '" + timeFrom + "' and obj.timeTo <= '" + timeTo + "')";
			if (timeContains) {
				whereString += " or (obj.timeFrom <= '" + timeFrom + "' and obj.timeTo >= '" + timeTo + "')";
			}
			if (timeIntersect) {
				whereString += " or (obj.timeFrom >= '" + timeFrom + "' and obj.timeTo >= '" + timeTo + "')";
				whereString += " or (obj.timeFrom <= '" + timeFrom + "' and obj.timeTo <= '" + timeTo + "')";
			}
			whereString += ")";
		} else if (timeFrom != null && timeTo == null) {
			whereString += " and ((obj.timeFrom >= '" + timeFrom + "')";
			if (timeIntersect) {
				whereString += " or (obj.timeFrom <= '" + timeFrom + "' and obj.timeTo >= '" + timeFrom + "')";
			}
			whereString += ")";
		} else if (timeFrom == null && timeTo != null) {
			whereString += " and ((obj.timeTo <= '" + timeTo + "')";
			if (timeIntersect) {
				whereString += " or (obj.timeTo >= '" + timeTo + "' and obj.timeFrom <= '" + timeTo + "')";
			}
			whereString += ")";
		} else if (timeAt != null) {
			whereString += " and obj.timeFrom == '" + timeAt + "%' and obj.timeType == 'am'";
		}
		
		String qString = fromString + whereString;

		return qString;
		
	}

	public static String createAddressExtendedSearchQuery(IngridDocument searchParams) {

		if (searchParams == null) {
			return null;
		}

		// NOTICE: Errors when using "join fetch" !
		String qString = "from AddressNode aNode " +
			"inner join aNode.t02AddressWork addr " +
			"where " +
			"addr.intitution = 'TEST'";

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
