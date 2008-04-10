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
		String whereString = "where";
		
		String queryTerm = searchParams.getString(MdekKeys.QUERY_TERM);
		Integer relation = searchParams.getInt(MdekKeys.RELATION);
		// parse queryTerm to extract multiple search entries and phrase tokens
		String[] searchTerms = getSearchTerms(queryTerm);
		if (searchTerms.length > 0) {
			fromString += " inner join oNode.fullIndexObjs fidx";
			whereString += " fidx.idxName = '" + IDX_NAME_FULLTEXT + "'";
			String op;
			if (relation == null || relation.intValue() == 0) {
				op = "and";
			} else {
				op = "or";
			}
			for (String term : searchTerms) {
				whereString += " " + op + " fidx.idxValue like '%" + term + "%'";
			}
		}
		
		List<Integer> objClasses = (List<Integer>)searchParams.get(MdekKeys.OBJ_CLASSES);
		if (objClasses != null && objClasses.size() > 0) {
			whereString += " and (";
			for (Integer objClass : objClasses) {
				whereString += "obj.idxValue like '%" + objClass + "%' or ";
			}
			whereString = whereString.substring(0, whereString.lastIndexOf(" or "));
			whereString += ")";
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
				result[i] = queries[i].getTerm();
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
