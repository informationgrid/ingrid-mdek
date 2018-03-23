/*-
 * **************************************************-
 * InGrid mdek-job
 * ==================================================
 * Copyright (C) 2014 - 2018 wemove digital solutions GmbH
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
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.ingrid.mdek.job.mapping.bawdmqs;

import de.ingrid.mdek.MdekError;
import de.ingrid.mdek.MdekError.MdekErrorType;
import de.ingrid.mdek.job.MdekException;
import de.ingrid.mdek.job.mapping.ImportDataMapper;
import de.ingrid.mdek.job.protocol.ProtocolHandler;
import de.ingrid.utils.xml.XMLUtils;
import javax.xml.transform.TransformerException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.DOMReader;
import org.dom4j.io.DOMWriter;

/**
 *
 * @author vikram
 */
public class BawDmqsSimulationImportDataMapper implements ImportDataMapper<org.w3c.dom.Document, org.w3c.dom.Document> {
    private static final Log LOG = LogFactory.getLog(BawDmqsSimulationImportDataMapper.class);

    private static final String HIERARCHY_LEVEL_NAME_KEY = "bawHierarchyLevelName";
    
    @Override
    public void convert(
            org.w3c.dom.Document source,
            org.w3c.dom.Document target,
            ProtocolHandler protocolHandler) throws MdekException {
        DOMReader reader = new DOMReader();
        Document from = reader.read(source);
        Document to = reader.read(target);

        mapHierarchyLevelName(from, to);

        DOMWriter writer = new DOMWriter();
        try {
            org.w3c.dom.Document doc = writer.write(to);
            org.w3c.dom.Node root = target.importNode(doc.getDocumentElement(), true);
            target.replaceChild(root, target.getDocumentElement());
            if (LOG.isDebugEnabled()) {
                try {
                    LOG.debug(String.format("Hierarchical import result:%s%n", XMLUtils.toString(target)));
                } catch (TransformerException e) {
                    
                }
            }
        } catch (DocumentException ex) {
            LOG.error("Error while converting the input data!", ex);
            String msg = "Problems converting import file: " + ex;
            throw new MdekException(new MdekError(MdekErrorType.IMPORT_PROBLEM, msg));
        }
    }

    private void mapHierarchyLevelName(Document source, Document target) {
        Element hierarchyLevelNameElement = (Element) source.selectSingleNode("/gmd:MD_Metadata/gmd:hierarchyLevelName/gco:CharacterString");
        String hierarchyLevelName;
        if (hierarchyLevelNameElement == null) {
            hierarchyLevelName = "";
        } else {
            hierarchyLevelName = hierarchyLevelNameElement.getText();
        }

        Element addnValues = (Element) target.selectSingleNode("//general-additional-values[1]");
        Element addnValue = addnValues.addElement("general-additional-value");
        addnValue.addElement("field-key").setText(HIERARCHY_LEVEL_NAME_KEY);
        addnValue.addElement("field-data").setText(hierarchyLevelName);
    }

}
