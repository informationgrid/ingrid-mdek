package de.ingrid.mdek.xml.importer;

import static org.junit.Assert.assertNotNull;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import de.ingrid.mdek.xml.util.XPathUtils;
import de.ingrid.utils.IngridDocument;

public class TestXPath {

	private static XPath xpath;
	private static IngridXMLStreamReader xmlReader;
	private static Document dataSource;

	@BeforeClass
	public static void setupClass() throws IOException, SAXException {
		setupXPath();
		setupIngridXMLReader();
		setupDocument();
	}
	
	private static void setupXPath() {
		xpath = XPathUtils.getXPathInstance();
	}

	private static void setupIngridXMLReader() throws IOException {
		IImporterCallback callback = null;
		xmlReader = new IngridXMLStreamReader(
				new BufferedInputStream(new FileInputStream(
						"src/test/resources/test.xml")), callback, "");
	}

	private static void setupDocument() throws IOException, SAXException {
		String uuid = xmlReader.getObjectUuids().toArray(new String[0])[0];
		dataSource = xmlReader.getDomForObject(uuid);
	}

	@Test
	public void testXPathGetObjectIdentifier() throws XPathExpressionException, IOException, SAXException {
		XPathExpression xpathObjectIdentifier = xpath.compile("//data-source/general/object-identifier/text()");

		String objectIdentifier = xpathObjectIdentifier.evaluate(dataSource);
		assertNotNull(objectIdentifier);
	}

	@Test
	public void testXPathGetCatalogueIdentifier() throws XPathExpressionException, IOException, SAXException {
		XPathExpression xpathCatalogueIdentifier = xpath.compile("//data-source/general/catalogue-identifier/text()");

		String catalogueIdentifier = xpathCatalogueIdentifier.evaluate(dataSource);
		assertNotNull(catalogueIdentifier);
	}

	@Test
	public void testXPathGetObjectClass() throws XPathExpressionException, IOException, SAXException {
		XPathExpression xpathObjectClass = xpath.compile("//data-source/general/object-class/@id");

		String objectClass = xpathObjectClass.evaluate(dataSource);
		assertNotNull(objectClass);
	}

	@Test
	public void testXPathGetAdditionalValues() throws XPathExpressionException, IOException, SAXException {
		XPathExpression xpathAdditionalValues = xpath.compile("//data-source/general/general-additional-values/general-additional-value");
		XPathExpression xpathAdditionalValueId = xpath.compile("@id");
		XPathExpression xpathAdditionalValueFieldName = xpath.compile("field-name");
		XPathExpression xpathAdditionalValueFieldValue = xpath.compile("field-value");

		NodeList additionalValues = (NodeList) xpathAdditionalValues.evaluate(dataSource, XPathConstants.NODESET);
		assertNotNull(additionalValues);
		for (int index = 0; index < additionalValues.getLength(); ++index) {
			Node additionalValue = additionalValues.item(index);

			String additionalValueId = xpathAdditionalValueId.evaluate(additionalValue);
			String additionalValueFieldName = xpathAdditionalValueFieldName.evaluate(additionalValue);
			String additionalValueFieldValue = xpathAdditionalValueFieldValue.evaluate(additionalValue);

			assertNotNull(additionalValueId);
			assertNotNull(additionalValueFieldName);
			assertNotNull(additionalValueFieldValue);
		}
	}

	@Test
	public void testXPathGetTopicCategories() throws XPathExpressionException, IOException, SAXException {
		XPathExpression xpathTopicCategories = xpath.compile("//data-source/general/topic-categories/topic-category/@id");

		NodeList topicCategories = (NodeList) xpathTopicCategories.evaluate(dataSource, XPathConstants.NODESET);
		assertNotNull(topicCategories);
		for (int index = 0; index < topicCategories.getLength(); ++index) {
			Node topicCategory = topicCategories.item(index);
			assertNotNull(topicCategory.getTextContent());
		}
	}

	@Test
	public void testXPathNodeExists() throws XPathExpressionException, IOException, SAXException {
		XPathExpression xpathExpression = xpath.compile("//data-source/technical-domain/project");

		Boolean result = (Boolean) xpathExpression.evaluate(dataSource, XPathConstants.BOOLEAN);
		assertNotNull(result);
	}
}