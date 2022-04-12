/*-
 * **************************************************-
 * InGrid mdek-job
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
package de.ingrid.mdek.job.mapping.profiles.baw;

import de.ingrid.mdek.job.MdekException;
import de.ingrid.mdek.job.mapping.ImportDataMapper;
import de.ingrid.mdek.job.protocol.ProtocolHandler;
import de.ingrid.utils.xml.IDFNamespaceContext;
import de.ingrid.utils.xpath.XPathUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.Objects;

public class IsoToIgcPreprocessorBaw implements ImportDataMapper<Document, Document> {

	private static final String DUMMY_UUID = "ffffffff-ffff-ffff-ffff-ffffffffffff";

	private XPathUtils isoXpathUtil;

	@Override
	public void convert(Document sourceIso, Document targetIgc, ProtocolHandler protocolHandler) throws MdekException {
		try {
			isoXpathUtil = new XPathUtils(new IDFNamespaceContext());

			Element mdMetadata = (Element) isoXpathUtil.getNode(sourceIso, "/gmd:MD_Metadata");
			addDummyUuidsToLiteratureContacts(mdMetadata);
			addDummyUuidToCrossReferenceContacts(mdMetadata);
		} catch (MdekException e) {
			protocolHandler.addMessage(ProtocolHandler.Type.ERROR, e.getMessage());
			throw e;
		}
	}

	private void addDummyUuidsToLiteratureContacts(Element mdMetadata) {
		Node hierarchyLevelNameNode = isoXpathUtil.getNode(mdMetadata, "gmd:hierarchyLevelName/gco:CharacterString");
		String hierarchyLevelName = hierarchyLevelNameNode == null ? null : hierarchyLevelNameNode.getTextContent().trim();
		if (Objects.equals(hierarchyLevelName, "document")) {
			String xpath = "./gmd:identificationInfo/*/gmd:pointOfContact/gmd:CI_ResponsibleParty[not(@uuid) and (./gmd:role/gmd:CI_RoleCode/@codeListValue=\"author\" or ./gmd:role/gmd:CI_RoleCode/@codeListValue=\"publisher\")]";
			addDummyUuids(mdMetadata, xpath);
		}
	}

	private void addDummyUuidToCrossReferenceContacts(Element mdMetadata) {
		String xpath = "gmd:identificationInfo/gmd:MD_DataIdentification/gmd:aggregationInfo//gmd:CI_ResponsibleParty[not(@uuid)]";
		addDummyUuids(mdMetadata, xpath);
	}

	private void addDummyUuids(Element mdMetadata, String xpath) {
		NodeList nodes = isoXpathUtil.getNodeList(mdMetadata, xpath);
		for(int i=0; i<nodes.getLength(); i++) {
			Element element = (Element) nodes.item(i);
			element.setAttribute("uuid", DUMMY_UUID);
		}
	}
}

