package de.ingrid.mdek.xml.importer.mapper.version105;

import static de.ingrid.mdek.xml.util.IngridDocUtils.*;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.MdekUtils;
import de.ingrid.mdek.MdekUtils.SearchtermType;
import de.ingrid.mdek.xml.util.XPathUtils;
import de.ingrid.utils.IngridDocument;

abstract public class AbstractXMLToDocMapper {

	protected final static String X_ATTRIBUTE_ID = "@id";
	protected final static String X_ATTRIBUTE_GEMET_ID = "@gemet-id";
	protected final static String X_ATTRIBUTE_ALTERNATE_NAME = "@alternate-name";
	protected final static String X_ATTRIBUTE_SOURCE = "@source";

	private static final String CONTROLLED_TERM = "controlled-term";
	private static final String UNCONTROLLED_TERM = "uncontrolled-term";

	private static final String UMTHES = "UMTHES";
	private static final String GEMET = "GEMET";
	private static final String INSPIRE = "INSPIRE";

	protected static void mapSubjectTerms(NodeList terms, IngridDocument target) {
		List<IngridDocument> termList = new ArrayList<IngridDocument>();
		List<IngridDocument> termInspireList = new ArrayList<IngridDocument>();

		for (int index = 0; index < terms.getLength(); index++) {
			Node term = terms.item(index);
			IngridDocument termDoc = new IngridDocument();

			putString(MdekKeys.TERM_NAME, term.getTextContent(), termDoc);

			String nodeName = term.getNodeName();
			if (CONTROLLED_TERM.equals(nodeName)) {
				String type = XPathUtils.getString(term, X_ATTRIBUTE_SOURCE);

				if (UMTHES.equals(type)) {
					putString(MdekKeys.TERM_TYPE, SearchtermType.UMTHES.getDbValue(), termDoc);
					putString(MdekKeys.TERM_SNS_ID, XPathUtils.getString(term, X_ATTRIBUTE_ID), termDoc);
					termList.add(termDoc);
				} else if (GEMET.equals(type)) {
					putString(MdekKeys.TERM_TYPE, SearchtermType.GEMET.getDbValue(), termDoc);
					putString(MdekKeys.TERM_SNS_ID, XPathUtils.getString(term, X_ATTRIBUTE_ID), termDoc);
					putString(MdekKeys.TERM_GEMET_ID, XPathUtils.getString(term, X_ATTRIBUTE_GEMET_ID), termDoc);
					putString(MdekKeys.TERM_ALTERNATE_NAME, XPathUtils.getString(term, X_ATTRIBUTE_ALTERNATE_NAME), termDoc);
					termList.add(termDoc);
				} else if (INSPIRE.equals(type)) {
					putString(MdekKeys.TERM_TYPE, SearchtermType.INSPIRE.getDbValue(), termDoc);
					putInt(MdekKeys.TERM_ENTRY_ID, XPathUtils.getInt(term, X_ATTRIBUTE_ID), termDoc);
					termInspireList.add(termDoc);
				}

			} else if (UNCONTROLLED_TERM.equals(nodeName)) {
				putString(MdekKeys.TERM_TYPE, MdekUtils.SearchtermType.FREI.getDbValue(), termDoc);
				termList.add(termDoc);
			}
		}

		putDocList(MdekKeys.SUBJECT_TERMS, termList, target);
		putDocList(MdekKeys.SUBJECT_TERMS_INSPIRE, termInspireList, target);
	}
}
