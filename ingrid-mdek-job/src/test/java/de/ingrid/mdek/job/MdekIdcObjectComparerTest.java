/**
 * 
 */
package de.ingrid.mdek.job;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Test;
import de.ingrid.utils.IngridDocument;
import de.ingrid.mdek.MdekKeys;

/**
 * @author joachim
 *
 */
public class MdekIdcObjectComparerTest {

	/**
	 * Test method for {@link de.ingrid.mdek.job.MdekIdcEntityComparer#compareObjectMaps(de.ingrid.utils.IngridDocument, de.ingrid.utils.IngridDocument, java.lang.String[])}.
	 */
	@Test
	public void testCompareObjectMaps() {
		IngridDocument doc1 = new IngridDocument();
		IngridDocument doc2 = new IngridDocument();
		doc1.put(MdekKeys.UUID, "12345678");
		doc2.put(MdekKeys.UUID, "12345678");
		assertTrue(MdekIdcEntityComparer.compareObjectMaps(doc1, doc2, null));
		doc2.put(MdekKeys.UUID, "123456789");
		assertFalse(MdekIdcEntityComparer.compareObjectMaps(doc1, doc2, null));
		assertTrue(MdekIdcEntityComparer.compareObjectMaps(doc1, doc2, new String[] {MdekKeys.UUID}));
		doc1.put(MdekKeys.UUID, "12345678");
		doc2.put(MdekKeys.UUID, "12345678");
		doc1.put(MdekKeys.CLASS, new Integer(1));
		doc2.put(MdekKeys.CLASS, new Integer(1));
		assertTrue(MdekIdcEntityComparer.compareObjectMaps(doc1, doc2, null));
		doc2.put(MdekKeys.CLASS, new Integer(2));
		assertFalse(MdekIdcEntityComparer.compareObjectMaps(doc1, doc2, null));
		doc2.put(MdekKeys.CLASS, new Integer(1));
		doc1.put(MdekKeys.HAS_CHILD, new Boolean(true));
		assertFalse(MdekIdcEntityComparer.compareObjectMaps(doc1, doc2, null));
		doc2.put(MdekKeys.HAS_CHILD, new Boolean(true));
		assertTrue(MdekIdcEntityComparer.compareObjectMaps(doc1, doc2, null));
		IngridDocument technicalDomainMap = new IngridDocument();
		technicalDomainMap.put(MdekKeys.TECHNICAL_BASE, "technical base");
		doc1.put(MdekKeys.TECHNICAL_DOMAIN_MAP, technicalDomainMap);
		doc2.put(MdekKeys.TECHNICAL_DOMAIN_MAP, technicalDomainMap);
		assertTrue(MdekIdcEntityComparer.compareObjectMaps(doc1, doc2, null));
		ArrayList<IngridDocument> commentList1 = new ArrayList<IngridDocument>();
		ArrayList<IngridDocument> commentList2 = new ArrayList<IngridDocument>();
		IngridDocument comment = new IngridDocument();
		comment.put(MdekKeys.COMMENT, "comment 1");
		comment.put(MdekKeys.USER_ID, "user id");
		commentList1.add(comment);
		commentList2.add(comment);
		IngridDocument comment2 = new IngridDocument();
		comment2.put(MdekKeys.COMMENT, "comment 2");
		commentList1.add(comment);
		doc1.put(MdekKeys.COMMENT_LIST, commentList1);
		doc2.put(MdekKeys.COMMENT_LIST, commentList2);
		assertFalse(MdekIdcEntityComparer.compareObjectMaps(doc1, doc2, null));
		commentList2.add(comment2);
		doc2.put(MdekKeys.COMMENT_LIST, commentList1);
		assertTrue(MdekIdcEntityComparer.compareObjectMaps(doc1, doc2, null));
	}
}
