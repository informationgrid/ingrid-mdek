/*
 * **************************************************-
 * ingrid-mdek-services
 * ==================================================
 * Copyright (C) 2014 - 2020 wemove digital solutions GmbH
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
/**
 * 
 */
package de.ingrid.mdek.services.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.MdekUtils.AddressType;
import de.ingrid.mdek.services.persistence.db.dao.hibernate.IFullIndexAccess;
import de.ingrid.utils.IngridDocument;
import de.ingrid.utils.query.ClauseQuery;
import de.ingrid.utils.query.IngridQuery;
import de.ingrid.utils.query.TermQuery;
import de.ingrid.utils.queryparser.QueryStringParser;

/**
 * @author Administrator
 */
public class ExtendedSearchHqlUtil implements IFullIndexAccess {

	private static final Logger LOG = LogManager.getLogger(ExtendedSearchHqlUtil.class);
	
	public static String createObjectExtendedSearchQuery(IngridDocument searchParams) {
		
		if (searchParams == null) {
			return null;
		}
		
		StringBuilder fromString = new StringBuilder("from ObjectNode oNode inner join oNode.t01ObjectWork obj");
		StringBuilder whereString = new StringBuilder("");
		
		String queryTerm = searchParams.getString(MdekKeys.QUERY_TERM);
		Integer relation = (Integer)searchParams.get(MdekKeys.RELATION);
		Integer searchType = (Integer)searchParams.get(MdekKeys.SEARCH_TYPE);
		// parse queryTerm to extract multiple search entries and phrase tokens
		String[] searchTerms = getSearchTerms(queryTerm);
		if (searchTerms.length > 0) {
			fromString.append(" inner join oNode.fullIndexObjs fidx");
			whereString.append(" fidx.idxName = '" + IDX_NAME_FULLTEXT + "' and (");
			String op;
			if (relation == null || relation == 0) {
				op = " and ";
			} else {
				op = " or ";
			}
			for (String term : searchTerms) {
				if (searchType == null || searchType == 0) {
					whereString.append(getWholeWordTerm(term))
						.append(op);
				} else {
					whereString.append(getPartialWordTerm(term))
						.append(op);
				}
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
			Integer thesaurusRelation = (Integer)searchParams.get(MdekKeys.THESAURUS_RELATION);

			fromString.append(" inner join oNode.fullIndexObjs fidxThes");
			if (whereString.length() > 0) {
				whereString.append(" and");
			}
			whereString.append(" fidxThes.idxName = '" + IDX_NAME_THESAURUS + "' and (");
			String op;
			if (thesaurusRelation == null || thesaurusRelation == 0) {
				op = " and ";
			} else {
				op = " or ";
			}
			for (IngridDocument thesTermDoc : thesaurusTerms) {
				whereString.append("fidxThes.idxValue like '%")
					.append(IDX_SEPARATOR)
					.append(thesTermDoc.getString(MdekKeys.TERM_SNS_ID))
					.append(IDX_SEPARATOR)
					.append("%'")
					.append(op);
			}
			whereString.delete(whereString.lastIndexOf(op), whereString.length());
			whereString.append(")");
		}
		
		List<IngridDocument> geoThesaurusTerms = (List<IngridDocument>)searchParams.get(MdekKeys.GEO_THESAURUS_TERMS);
		if (geoThesaurusTerms != null && geoThesaurusTerms.size() > 0) {
			Integer geoThesaurusRelation = (Integer)searchParams.get(MdekKeys.GEO_THESAURUS_RELATION);

			fromString.append(" inner join oNode.fullIndexObjs fidxGeothes");
			if (whereString.length() > 0) {
				whereString.append(" and");
			}
			whereString.append(" fidxGeothes.idxName = '" + IDX_NAME_GEOTHESAURUS + "' and (");
			String op;
			if (geoThesaurusRelation == null || geoThesaurusRelation == 0) {
				op = " and ";
			} else {
				op = " or ";
			}
			for (IngridDocument geoThesTermDoc : geoThesaurusTerms) {
				// TODO: geothesaurus wie untergeordnete kreise/gemeinden finden ???
				// LOCATION_SNS_ID enthaelt "BUNDESLAND" "KREIS" oder "GEMEINDE" + 2 oder 10 STELLIGER nativeKey !
				// NativeKey Stellen: 12=Bundesland, 345=Kreis, 67890=Gemeinde
				whereString.append("fidxGeothes.idxValue like '%")
					.append(IDX_SEPARATOR)
					.append(geoThesTermDoc.getString(MdekKeys.LOCATION_SNS_ID))
					// funktioniert nicht, da LOCATION_SNS_ID sich nicht "erweitert", s.o.
					.append("%'")
					.append(op);
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

		if (timeFrom != null || timeTo != null || timeAt != null) {
			if (whereString.length() > 0) {
				whereString.append(" and ");
			}

			// exclude records with no time reference
			whereString.append(" (obj.timeFrom IS NOT NULL or obj.timeTo IS NOT NULL) and (");

			// VON (- BIS)
			if (timeFrom != null && timeTo != null) {
				// results inside
				whereString.append("(" +
						"obj.timeFrom IS NOT NULL and obj.timeFrom >= '").append(timeFrom).append("' " +
						"and obj.timeTo IS NOT NULL and obj.timeTo <= '").append(timeTo).append("')");
				// results intersecting
				if (timeIntersect) {
					whereString.append(" or (" +
						"(obj.timeFrom IS NULL or obj.timeFrom < '").append(timeFrom).append("') " +
						"and obj.timeTo IS NOT NULL and obj.timeTo >= '").append(timeFrom).append("' " +
						// timeTo should NOT be included to avoid "contains"
						"and obj.timeTo <= '").append(timeTo).append("')");
					whereString.append(" or (" +
						// timeFrom should NOT be included to avoid "contains"
						"obj.timeFrom IS NOT NULL and obj.timeFrom >= '").append(timeFrom).append("' " +
						"and obj.timeFrom <= '").append(timeTo).append("' and " +
						"(obj.timeTo IS NULL or obj.timeTo > '").append(timeTo).append("'))");
				}
				// results containing
				if (timeContains) {
					whereString.append(" or (" +
							"(obj.timeFrom IS NULL or obj.timeFrom < '").append(timeFrom).append("') " +
							"and (obj.timeTo IS NULL or obj.timeTo > '").append(timeTo).append("'))");
				}

			// SEIT
			} else if (timeFrom != null && timeTo == null) {
				// results inside
				whereString.append("(obj.timeFrom IS NOT NULL and obj.timeFrom >= '").append(timeFrom).append("')");
				// results intersecting
				if (timeIntersect) {
					whereString.append(" or (" +
							"(obj.timeFrom IS NULL or obj.timeFrom < '").append(timeFrom).append("') " +
							// timeTo should NOT be NULL, to avoid "contains"
							"and (obj.timeTo IS NOT NULL and obj.timeTo >= '").append(timeFrom).append("'))");
				}
				// results containing
				if (timeContains) {
					whereString.append(" or (obj.timeTo IS NULL and obj.timeFrom < '").append(timeFrom).append("')");
				}

			// BIS
			} else if (timeFrom == null && timeTo != null) {
				// results inside
				whereString.append("(obj.timeTo IS NOT NULL and obj.timeTo <= '").append(timeTo).append("')");
				// results intersecting
				if (timeIntersect) {
					whereString.append(" or (" +
							// timeFrom should NOT be NULL, to avoid "contains"
							"(obj.timeFrom IS NOT NULL and obj.timeFrom <= '").append(timeTo).append("') " +
							"and (obj.timeTo IS NULL or obj.timeTo > '").append(timeTo).append("'))");
				}
				if (timeContains) {
					whereString.append(" or (obj.timeFrom IS NULL and obj.timeTo > '").append(timeTo).append("')");
				}

			// AM
			} else if (timeAt != null) {
				// results inside
				whereString.append("(obj.timeFrom IS NOT NULL and obj.timeFrom = '").append(timeAt).append("' " +
						"and obj.timeTo IS NOT NULL and obj.timeTo = '").append(timeAt).append("')");
				// results intersecting
				if (timeIntersect) {
					whereString.append(" or (obj.timeFrom IS NOT NULL and obj.timeFrom = '").append(timeAt).append("')");
					whereString.append(" or (obj.timeTo IS NOT NULL and obj.timeTo = '").append(timeAt).append("')");
				}
				// results containing
				if (timeContains) {
					whereString.append(" or (" +
							"(obj.timeFrom IS NULL or obj.timeFrom < '").append(timeAt).append("') " +
							"and (obj.timeTo IS NULL or obj.timeTo > '").append(timeAt).append("'))");
				}
			}

			whereString.append(") ");
		}
		
		if (whereString.length() == 0) {
			return fromString.toString();
		} else {
			return fromString.append(" where").append(whereString).toString();
		}
		
	}

	public static String createAddressExtendedSearchQuery(IngridDocument searchParams) {

		if (searchParams == null) {
			return null;
		}
		
		StringBuilder fromString = new StringBuilder("from AddressNode aNode inner join aNode.t02AddressWork addr");
		// exclude hidden user addresses !
		StringBuilder whereString = new StringBuilder(AddressType.getHQLExcludeIGEUsersViaNode("aNode", "addr"));
		
		String queryTerm = searchParams.getString(MdekKeys.QUERY_TERM);
		Integer relation = (Integer)searchParams.get(MdekKeys.RELATION);
		Integer searchType = (Integer)searchParams.get(MdekKeys.SEARCH_TYPE);
		Integer searchRange = (Integer)searchParams.get(MdekKeys.SEARCH_RANGE);
		// parse queryTerm to extract multiple search entries and phrase tokens
		String[] searchTerms = getSearchTerms(queryTerm);
		if (searchTerms.length > 0) {
			fromString.append(" inner join aNode.fullIndexAddrs fidx");
			if (searchRange == null || searchRange == 0) {
				whereString.append(" AND fidx.idxName = '").append(IDX_NAME_FULLTEXT).append("' and (");
			} else {
				whereString.append(" AND fidx.idxName = '").append(IDX_NAME_PARTIAL).append("' and (");
			}
			String op;
			if (relation == null || relation == 0) {
				op = " and ";
			} else {
				op = " or ";
			}
			for (String term : searchTerms) {
				if (searchType == null || searchType == 0) {
					whereString.append(getWholeWordTerm(term))
						.append(op);
				} else {
					whereString.append(getPartialWordTerm(term))
						.append(op);
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
		
		if (whereString.length() == 0) {
			return fromString.toString();
		} else {
			return fromString.append(" where ").append(whereString).toString();
		}
		
	}
	
	private static String[] getSearchTerms(String qString) {
		if (qString == null) {
			return new String[] {};			
		}

		try {
			IngridQuery q = QueryStringParser.parse(qString);
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
		} catch (Exception e) {
			LOG.warn("Problems extracting search terms from query", e);
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

	private static String getWholeWordTerm(String term) {
		StringBuilder qString = new StringBuilder("");
		qString
			.append("(")
			.append("fidx.idxValue like '% ").append(term).append(" %'")
			.append(" or fidx.idxValue like '%|").append(term).append("|%'")
			.append(" or fidx.idxValue like '%|").append(term).append(" %'")
			.append(" or fidx.idxValue like '% ").append(term).append("|%'")
			.append(" or fidx.idxValue like '% ").append(term).append("'")
			.append(" or fidx.idxValue like '%|").append(term).append("'")
			.append(" or fidx.idxValue like '").append(term).append(" %'")
			.append(" or fidx.idxValue like '").append(term).append("|%'")
			.append(")");
		
		return qString.toString();
	}

	private static String getPartialWordTerm(String term) {
		StringBuilder qString = new StringBuilder("");		
		qString.append("fidx.idxValue like '%").append(term).append("%'");
		
		return qString.toString();
	}
}
