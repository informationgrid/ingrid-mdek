/*
 * **************************************************-
 * ingrid-import-export
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
package de.ingrid.mdek.xml.importer;

import static org.junit.Assert.assertNotNull;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Set;

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

public class TestXPath {

	private static XPath xpath;
	private static IngridXMLStreamReader xmlReader;
	private static List<Document> dataSourceInstances;

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
		Set<String> uuids = xmlReader.getObjectUuids();
		if (uuids.size() > 0) {
			String uuid = uuids.toArray(new String[0])[0];
			dataSourceInstances = xmlReader.getDomForObject(uuid);
		} else {
			throw new RuntimeException("!!! Problems reading UUIDs from test.xml !!! Wrong Import File Format ?!!!");
		}
	}

	@Test
	public void testXPathGetObjectIdentifier() throws XPathExpressionException, IOException, SAXException {
		XPathExpression xpathObjectIdentifier = xpath.compile("//data-source/general/object-identifier/text()");

		for (Document dataSource : dataSourceInstances) {
			String objectIdentifier = xpathObjectIdentifier.evaluate(dataSource);
			assertNotNull(objectIdentifier);
		}
	}

	@Test
	public void testXPathGetCatalogueIdentifier() throws XPathExpressionException, IOException, SAXException {
		XPathExpression xpathCatalogueIdentifier = xpath.compile("//data-source/general/catalogue-identifier/text()");

		for (Document dataSource : dataSourceInstances) {
			String catalogueIdentifier = xpathCatalogueIdentifier.evaluate(dataSource);
			assertNotNull(catalogueIdentifier);
		}
	}

	@Test
	public void testXPathGetObjectClass() throws XPathExpressionException, IOException, SAXException {
		XPathExpression xpathObjectClass = xpath.compile("//data-source/general/object-class/@id");

		for (Document dataSource : dataSourceInstances) {
			String objectClass = xpathObjectClass.evaluate(dataSource);
			assertNotNull(objectClass);
		}
	}

	@Test
	public void testXPathGetAdditionalValues() throws XPathExpressionException, IOException, SAXException {
		XPathExpression xpathAdditionalValues = xpath.compile("//data-source/general/general-additional-values/general-additional-value");
		XPathExpression xpathAdditionalValueId = xpath.compile("@id");
		XPathExpression xpathAdditionalValueFieldName = xpath.compile("field-name");
		XPathExpression xpathAdditionalValueFieldValue = xpath.compile("field-value");

		for (Document dataSource : dataSourceInstances) {
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
	}

	@Test
	public void testXPathGetTopicCategories() throws XPathExpressionException, IOException, SAXException {
		XPathExpression xpathTopicCategories = xpath.compile("//data-source/general/topic-categories/topic-category/@id");

		for (Document dataSource : dataSourceInstances) {
			NodeList topicCategories = (NodeList) xpathTopicCategories.evaluate(dataSource, XPathConstants.NODESET);
			assertNotNull(topicCategories);
			for (int index = 0; index < topicCategories.getLength(); ++index) {
				Node topicCategory = topicCategories.item(index);
				assertNotNull(topicCategory.getTextContent());
			}
		}
	}

	@Test
	public void testXPathNodeExists() throws XPathExpressionException, IOException, SAXException {
		XPathExpression xpathExpression = xpath.compile("//data-source/technical-domain/project");

		for (Document dataSource : dataSourceInstances) {
			Boolean result = (Boolean) xpathExpression.evaluate(dataSource, XPathConstants.BOOLEAN);
			assertNotNull(result);
		}
	}
}
