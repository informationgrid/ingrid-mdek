/*
 * **************************************************-
 * ingrid-import-export
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
package de.ingrid.mdek.xml.importer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import de.ingrid.mdek.xml.util.XPathUtils;

public class TestXMLFilterReader {

	private static XPath xpath;
	private static IngridXMLStreamReader xmlReader;

	@BeforeClass
	public static void setupClass() throws IOException {
		setupXPath();
		setupIngridXMLReader();
	}
	
	private static void setupXPath() {
		xpath = XPathUtils.getXPathInstance();
	}

	private static void setupIngridXMLReader() throws IOException {
		long startTime = System.currentTimeMillis();
		IImporterCallback callback = null;
		xmlReader = new IngridXMLStreamReader(
				new BufferedInputStream(new FileInputStream(
						"src/test/resources/test.xml")), callback, "");
		long endTime = System.currentTimeMillis();
		System.out.println("parsing document for uuids took "+(endTime - startTime)+" ms.");

		if (xmlReader.getObjectUuids().size() == 0) {
			throw new RuntimeException("!!! Problems reading UUIDs from test.xml !!! Wrong Import File Format ?!!!");
		}
	}

	@Test
	public void testCanConstructXMLFilterReader() {
		assertNotNull(xmlReader);
		assertNotNull(xmlReader.getObjectUuids());
		assertNotNull(xmlReader.getAddressUuids());
	}

	@Test
	public void testXMLFilterReaderReturnsCorrectObjects() throws XPathExpressionException, IOException, SAXException {
		XPathExpression expression = xpath.compile("//data-source/general/object-identifier/text()");

		for (String uuid : xmlReader.getObjectUuids()) {
			List<Document> docs = xmlReader.getDomForObject(uuid);
			for (Document doc : docs) {
				String objectUuid = expression.evaluate(doc);
				assertEquals(uuid, objectUuid);
			}
		}
	}

	@Test
	public void testXMLFilterReaderReturnsValidObjectDomTree() throws IOException, ParserConfigurationException, SAXException,
			XPathExpressionException {

		for (String uuid : xmlReader.getObjectUuids()) {
			List<Document> docs = xmlReader.getDomForObject(uuid);
			for (Document doc : docs) {
				Node node = doc.getElementsByTagName("general").item(0);
				Node objUuidNode = getNodeWithName(node.getChildNodes(), "object-identifier");
				assertNotNull(objUuidNode);
				assertEquals(objUuidNode.getTextContent(), uuid);
			}
		}
	}


	@Test
	public void testXMLFilterReaderReturnsCorrectAddresses() throws IOException, ParserConfigurationException, SAXException,
			XPathExpressionException {

		XPathExpression expression = xpath.compile("//address/address-identifier/text()");

		for (String uuid : xmlReader.getAddressUuids()) {
			List<Document> docs = xmlReader.getDomForAddress(uuid);
			for (Document doc : docs) {
				String addressUuid = expression.evaluate(doc);
				assertEquals(uuid, addressUuid);
			}
		}
	}

	@Test
	public void testXMLFilterReaderReturnsValidAddressDomTree() throws IOException, ParserConfigurationException, SAXException,
			XPathExpressionException {

		for (String uuid : xmlReader.getAddressUuids()) {
			List<Document> docs = xmlReader.getDomForAddress(uuid);
			for (Document doc : docs) {
				Node adrUuidNode = doc.getElementsByTagName("address-identifier").item(0);
				assertNotNull(adrUuidNode);
				assertEquals(adrUuidNode.getTextContent(), uuid);
			}
		}
	}

	@Test
	public void testGetObjectWriteSequence() throws IOException, SAXException {
		xmlReader.getObjectWriteSequence();
	}

	@Test
	public void testGetAddressWriteSequence() throws IOException, SAXException {
		xmlReader.getAddressWriteSequence();
	}

	private Node getNodeWithName(NodeList nodeList, String nodeName) {
		for (int i = 0; i < nodeList.getLength(); ++i) {
			if (nodeList.item(i).getNodeName().equals(nodeName)) {
				return nodeList.item(i);
			}
		}
		return null;
	}
}
