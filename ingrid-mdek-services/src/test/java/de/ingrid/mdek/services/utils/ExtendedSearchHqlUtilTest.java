/**
 * 
 */
package de.ingrid.mdek.services.utils;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import de.ingrid.mdek.MdekKeys;
import de.ingrid.utils.IngridDocument;

/**
 * @author joachim
 *
 */
public class ExtendedSearchHqlUtilTest {

	/**
	 * Test method for {@link de.ingrid.mdek.services.utils.ExtendedSearchHqlUtil#createObjectExtendedSearchQuery(de.ingrid.utils.IngridDocument)}.
	 */
	@Test
	public void testCreateObjectExtendedSearchQuery() {
		IngridDocument searchParams = new IngridDocument();
		searchParams.put(MdekKeys.QUERY_TERM, "hallo term1 \"term2 term3\"");
		searchParams.put(MdekKeys.RELATION, new Integer(0));
		ExtendedSearchHqlUtil.createObjectExtendedSearchQuery(searchParams);
		Assert.assertEquals("from ObjectNode oNode inner join oNode.t01ObjectWork obj inner join oNode.fullIndexObjs fidx where fidx.idxName = 'full' and (fidx.idxValue like '%hallo%' and fidx.idxValue like '%term1%' and fidx.idxValue like '%term2 term3%')", ExtendedSearchHqlUtil.createObjectExtendedSearchQuery(searchParams));
		searchParams.putInt(MdekKeys.RELATION, 1);
		Assert.assertEquals("from ObjectNode oNode inner join oNode.t01ObjectWork obj inner join oNode.fullIndexObjs fidx where fidx.idxName = 'full' and (fidx.idxValue like '%hallo%' or fidx.idxValue like '%term1%' or fidx.idxValue like '%term2 term3%')", ExtendedSearchHqlUtil.createObjectExtendedSearchQuery(searchParams));
		
		List<Integer> lInt = new ArrayList<Integer>(); 
		lInt.add(1);
		lInt.add(2);
		lInt.add(5);
		searchParams.put(MdekKeys.OBJ_CLASSES, lInt);
		Assert.assertEquals("from ObjectNode oNode inner join oNode.t01ObjectWork obj inner join oNode.fullIndexObjs fidx where fidx.idxName = 'full' and (fidx.idxValue like '%hallo%' or fidx.idxValue like '%term1%' or fidx.idxValue like '%term2 term3%') and (obj.objClass = 1 or obj.objClass = 2 or obj.objClass = 5)", ExtendedSearchHqlUtil.createObjectExtendedSearchQuery(searchParams));
		
		List<IngridDocument> lDoc = new ArrayList<IngridDocument>();
		lDoc.add(new IngridDocument());
		lDoc.add(new IngridDocument());
		lDoc.get(0).put(MdekKeys.TERM_SNS_ID, "sns_id_1");
		lDoc.get(1).put(MdekKeys.TERM_SNS_ID, "sns_id_2");
		searchParams.put(MdekKeys.THESAURUS_TERMS, lDoc);
		searchParams.put(MdekKeys.THESAURUS_RELATION, new Integer(1));
		Assert.assertEquals("from ObjectNode oNode inner join oNode.t01ObjectWork obj inner join oNode.fullIndexObjs fidx inner join obj.searchtermObjs stObjs inner join stObjs.searchtermValue stVal inner join stVal.searchtermSns stSns where fidx.idxName = 'full' and (fidx.idxValue like '%hallo%' or fidx.idxValue like '%term1%' or fidx.idxValue like '%term2 term3%') and (obj.objClass = 1 or obj.objClass = 2 or obj.objClass = 5) and stVal.type='T' and (stSns.snsId = 'sns_id_1' or stSns.snsId = 'sns_id_2')", ExtendedSearchHqlUtil.createObjectExtendedSearchQuery(searchParams));
		
		lDoc = new ArrayList<IngridDocument>();
		lDoc.add(new IngridDocument());
		lDoc.add(new IngridDocument());
		lDoc.get(0).put(MdekKeys.LOCATION_SNS_ID, "geo_sns_id_1");
		lDoc.get(1).put(MdekKeys.LOCATION_SNS_ID, "geo_sns_id_2");
		searchParams.put(MdekKeys.GEO_THESAURUS_TERMS, lDoc);
		searchParams.put(MdekKeys.GEO_THESAURUS_RELATION, new Integer(0));
		Assert.assertEquals("from ObjectNode oNode inner join oNode.t01ObjectWork obj inner join oNode.fullIndexObjs fidx inner join obj.searchtermObjs stObjs inner join stObjs.searchtermValue stVal inner join stVal.searchtermSns stSns inner join obj.spatialReferences spcRefs inner join spcRefs.spatialRefValue spcRefVal inner join spcRefVal.spatialRefSns spcRefSns where fidx.idxName = 'full' and (fidx.idxValue like '%hallo%' or fidx.idxValue like '%term1%' or fidx.idxValue like '%term2 term3%') and (obj.objClass = 1 or obj.objClass = 2 or obj.objClass = 5) and stVal.type='T' and (stSns.snsId = 'sns_id_1' or stSns.snsId = 'sns_id_2') and spcRefVal.type='G' and (spcRefSns.snsId LIKE 'geo_sns_id_1%' and spcRefSns.snsId LIKE 'geo_sns_id_2%')", ExtendedSearchHqlUtil.createObjectExtendedSearchQuery(searchParams));

		searchParams.put(MdekKeys.CUSTOM_LOCATION, new Integer(12345));
		Assert.assertEquals("from ObjectNode oNode inner join oNode.t01ObjectWork obj inner join oNode.fullIndexObjs fidx inner join obj.searchtermObjs stObjs inner join stObjs.searchtermValue stVal inner join stVal.searchtermSns stSns inner join obj.spatialReferences spcRefs inner join spcRefs.spatialRefValue spcRefVal inner join spcRefVal.spatialRefSns spcRefSns where fidx.idxName = 'full' and (fidx.idxValue like '%hallo%' or fidx.idxValue like '%term1%' or fidx.idxValue like '%term2 term3%') and (obj.objClass = 1 or obj.objClass = 2 or obj.objClass = 5) and stVal.type='T' and (stSns.snsId = 'sns_id_1' or stSns.snsId = 'sns_id_2') and spcRefVal.type='G' and (spcRefSns.snsId LIKE 'geo_sns_id_1%' and spcRefSns.snsId LIKE 'geo_sns_id_2%') and spcRefVal.nameKey = 12345", ExtendedSearchHqlUtil.createObjectExtendedSearchQuery(searchParams));

		searchParams.put(MdekKeys.TIME_FROM, "200712121212123");
		searchParams.put(MdekKeys.TIME_TO, "200812121212123");
		Assert.assertEquals("from ObjectNode oNode inner join oNode.t01ObjectWork obj inner join oNode.fullIndexObjs fidx inner join obj.searchtermObjs stObjs inner join stObjs.searchtermValue stVal inner join stVal.searchtermSns stSns inner join obj.spatialReferences spcRefs inner join spcRefs.spatialRefValue spcRefVal inner join spcRefVal.spatialRefSns spcRefSns where fidx.idxName = 'full' and (fidx.idxValue like '%hallo%' or fidx.idxValue like '%term1%' or fidx.idxValue like '%term2 term3%') and (obj.objClass = 1 or obj.objClass = 2 or obj.objClass = 5) and stVal.type='T' and (stSns.snsId = 'sns_id_1' or stSns.snsId = 'sns_id_2') and spcRefVal.type='G' and (spcRefSns.snsId LIKE 'geo_sns_id_1%' and spcRefSns.snsId LIKE 'geo_sns_id_2%') and spcRefVal.nameKey = 12345 and ((obj.timeFrom >= '200712121212123' and obj.timeTo <= '200812121212123'))", ExtendedSearchHqlUtil.createObjectExtendedSearchQuery(searchParams));
		searchParams.put(MdekKeys.TIME_CONTAINS, true);
		Assert.assertEquals("from ObjectNode oNode inner join oNode.t01ObjectWork obj inner join oNode.fullIndexObjs fidx inner join obj.searchtermObjs stObjs inner join stObjs.searchtermValue stVal inner join stVal.searchtermSns stSns inner join obj.spatialReferences spcRefs inner join spcRefs.spatialRefValue spcRefVal inner join spcRefVal.spatialRefSns spcRefSns where fidx.idxName = 'full' and (fidx.idxValue like '%hallo%' or fidx.idxValue like '%term1%' or fidx.idxValue like '%term2 term3%') and (obj.objClass = 1 or obj.objClass = 2 or obj.objClass = 5) and stVal.type='T' and (stSns.snsId = 'sns_id_1' or stSns.snsId = 'sns_id_2') and spcRefVal.type='G' and (spcRefSns.snsId LIKE 'geo_sns_id_1%' and spcRefSns.snsId LIKE 'geo_sns_id_2%') and spcRefVal.nameKey = 12345 and ((obj.timeFrom >= '200712121212123' and obj.timeTo <= '200812121212123') or (obj.timeFrom <= '200712121212123' and obj.timeTo >= '200812121212123'))", ExtendedSearchHqlUtil.createObjectExtendedSearchQuery(searchParams));
		searchParams.put(MdekKeys.TIME_INTERSECT, true);
		Assert.assertEquals("from ObjectNode oNode inner join oNode.t01ObjectWork obj inner join oNode.fullIndexObjs fidx inner join obj.searchtermObjs stObjs inner join stObjs.searchtermValue stVal inner join stVal.searchtermSns stSns inner join obj.spatialReferences spcRefs inner join spcRefs.spatialRefValue spcRefVal inner join spcRefVal.spatialRefSns spcRefSns where fidx.idxName = 'full' and (fidx.idxValue like '%hallo%' or fidx.idxValue like '%term1%' or fidx.idxValue like '%term2 term3%') and (obj.objClass = 1 or obj.objClass = 2 or obj.objClass = 5) and stVal.type='T' and (stSns.snsId = 'sns_id_1' or stSns.snsId = 'sns_id_2') and spcRefVal.type='G' and (spcRefSns.snsId LIKE 'geo_sns_id_1%' and spcRefSns.snsId LIKE 'geo_sns_id_2%') and spcRefVal.nameKey = 12345 and ((obj.timeFrom >= '200712121212123' and obj.timeTo <= '200812121212123') or (obj.timeFrom <= '200712121212123' and obj.timeTo >= '200812121212123') or (obj.timeFrom >= '200712121212123' and obj.timeTo >= '200812121212123') or (obj.timeFrom <= '200712121212123' and obj.timeTo <= '200812121212123'))", ExtendedSearchHqlUtil.createObjectExtendedSearchQuery(searchParams));
		searchParams.remove(MdekKeys.TIME_TO);
		Assert.assertEquals("from ObjectNode oNode inner join oNode.t01ObjectWork obj inner join oNode.fullIndexObjs fidx inner join obj.searchtermObjs stObjs inner join stObjs.searchtermValue stVal inner join stVal.searchtermSns stSns inner join obj.spatialReferences spcRefs inner join spcRefs.spatialRefValue spcRefVal inner join spcRefVal.spatialRefSns spcRefSns where fidx.idxName = 'full' and (fidx.idxValue like '%hallo%' or fidx.idxValue like '%term1%' or fidx.idxValue like '%term2 term3%') and (obj.objClass = 1 or obj.objClass = 2 or obj.objClass = 5) and stVal.type='T' and (stSns.snsId = 'sns_id_1' or stSns.snsId = 'sns_id_2') and spcRefVal.type='G' and (spcRefSns.snsId LIKE 'geo_sns_id_1%' and spcRefSns.snsId LIKE 'geo_sns_id_2%') and spcRefVal.nameKey = 12345 and ((obj.timeFrom >= '200712121212123') or (obj.timeFrom <= '200712121212123' and obj.timeTo >= '200712121212123'))", ExtendedSearchHqlUtil.createObjectExtendedSearchQuery(searchParams));
		searchParams.remove(MdekKeys.TIME_FROM);
		searchParams.put(MdekKeys.TIME_AT, "200612121212123");
		Assert.assertEquals("from ObjectNode oNode inner join oNode.t01ObjectWork obj inner join oNode.fullIndexObjs fidx inner join obj.searchtermObjs stObjs inner join stObjs.searchtermValue stVal inner join stVal.searchtermSns stSns inner join obj.spatialReferences spcRefs inner join spcRefs.spatialRefValue spcRefVal inner join spcRefVal.spatialRefSns spcRefSns where fidx.idxName = 'full' and (fidx.idxValue like '%hallo%' or fidx.idxValue like '%term1%' or fidx.idxValue like '%term2 term3%') and (obj.objClass = 1 or obj.objClass = 2 or obj.objClass = 5) and stVal.type='T' and (stSns.snsId = 'sns_id_1' or stSns.snsId = 'sns_id_2') and spcRefVal.type='G' and (spcRefSns.snsId LIKE 'geo_sns_id_1%' and spcRefSns.snsId LIKE 'geo_sns_id_2%') and spcRefVal.nameKey = 12345 and obj.timeFrom == '200612121212123%' and obj.timeType == 'am'", ExtendedSearchHqlUtil.createObjectExtendedSearchQuery(searchParams));
		
	}

	/**
	 * Test method for {@link de.ingrid.mdek.services.utils.ExtendedSearchHqlUtil#createAddressExtendedSearchQuery(de.ingrid.utils.IngridDocument)}.
	 */
	@Test
	public void testCreateAddressExtendedSearchQuery() {
		fail("Not yet implemented");
	}

}
