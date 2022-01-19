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
package de.ingrid.mdek.job.mapping.profiles.baw.validation.iso;

import de.ingrid.utils.xml.IDFNamespaceContext;
import de.ingrid.utils.xpath.XPathUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.URL;

class ValidatorTestsTemplateHelper {
    private static final String TEMPLATE_PATH = "de/ingrid/mdek/job/validation/iso/bawdmqs/baw_template.xml";
    private static final XPathUtils XPATH = new XPathUtils(new IDFNamespaceContext());

    static Document defaultDocument() throws SAXException, IOException, ParserConfigurationException {
        URL url = ValidatorTestsTemplateHelper.class.getClassLoader().getResource(TEMPLATE_PATH);

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);

        DocumentBuilder db = dbf.newDocumentBuilder();
        return db.parse(url.openStream());
    }

    static Node removeElementAtXpath(Document doc, String xpath) {
       return XPATH.removeElementAtXPath(doc, xpath);
    }

    static void removeElementsAtXpath(Document doc, String xpath) {
        NodeList nl = XPATH.getNodeList(doc, xpath);

        for (int i=0; i< nl.getLength(); i++) {
            XPATH.removeElementAtXPath(doc, xpath);
        }
    }

    static void setTextForElementAtXpath(Document doc, String xpath, String text) {
        Element element = (Element) XPATH.getNode(doc, xpath);
        element.setTextContent(text);
    }

    static void setAttributeForElementAtXpath(Document doc, String xpath, String attrName, String attrValue) {
        Element element = (Element) XPATH.getNode(doc, xpath);
        element.setAttribute(attrName, attrValue);
    }

}
