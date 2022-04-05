package de.ingrid.mdek.job.mapping.profiles.baw;

import de.ingrid.mdek.job.MdekException;
import de.ingrid.mdek.job.mapping.ImportDataMapper;
import de.ingrid.mdek.job.protocol.ProtocolHandler;
import de.ingrid.utils.xml.IDFNamespaceContext;
import de.ingrid.utils.xpath.XPathUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class IsoToIgcPreprocessorBaw implements ImportDataMapper<Document, Document> {

	private static final Logger LOG = Logger.getLogger(IsoToIgcPreprocessorBaw.class);
	private static final String DUMMY_UUID = "ffffffff-ffff-ffff-ffff-ffffffffffff";

	private XPathUtils isoXpathUtil;

	@Override
	public void convert(Document sourceIso, Document targetIgc, ProtocolHandler protocolHandler) throws MdekException {
		try {
			isoXpathUtil = new XPathUtils(new IDFNamespaceContext());

			Element mdMetadata = (Element) isoXpathUtil.getNode(sourceIso, "/gmd:MD_Metadata");
			addDummyUuidToCrossReferenceContacts(mdMetadata);
		} catch (MdekException e) {
			protocolHandler.addMessage(ProtocolHandler.Type.ERROR, e.getMessage());
			throw e;
		}
	}

	private void addDummyUuidToCrossReferenceContacts(Element mdMetadata) {
		String xpath = "gmd:identificationInfo/gmd:MD_DataIdentification/gmd:aggregationInfo//gmd:CI_ResponsibleParty[not(@uuid)]";
		NodeList nodes = isoXpathUtil.getNodeList(mdMetadata, xpath);
		for(int i=0; i<nodes.getLength(); i++) {
			Element element = (Element) nodes.item(i);
			element.setAttribute("uuid", DUMMY_UUID);
		}
	}
}

