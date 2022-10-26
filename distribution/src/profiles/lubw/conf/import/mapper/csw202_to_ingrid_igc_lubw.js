/*
 * **************************************************-
 * Ingrid Portal MDEK Application
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

if (javaVersion.indexOf( "1.8" ) === 0) {
    load("nashorn:mozilla_compat.js");
    var ProtocolHandler = Java.type("de.ingrid.mdek.job.protocol.ProtocolHandler");
}

importPackage(Packages.de.ingrid.utils.udk);
importPackage(Packages.org.w3c.dom);
importClass(Packages.de.ingrid.utils.xml.XMLUtils);
importClass(Packages.de.ingrid.mdek.job.protocol.ProtocolHandler);

var mappingDescriptionLUBW = {"mappings":[
    {
        "execute": {
            "funct": mapSpecialFields
        }
    }
]};

if (log.isDebugEnabled()) {
    log.debug("LUBW: mapping CSW 2.0.2 AP ISO 1.0 document to IGC import document.");
}

mapToTarget(mappingDescriptionLUBW, source, target.getDocumentElement());

function mapSpecialFields(source, target) {
    /**
     * Zusätzliches Feld -> Textbox: "OAC"
     */
    var keywords = XPATH.getNodeList(source, "//gmd:identificationInfo//gmd:descriptiveKeywords/gmd:MD_Keywords/gmd:keyword/gco:CharacterString");

    for (var i=0; i<keywords.getLength(); i++ ) {
        var keyword = keywords.item(i).getTextContent();
        if (keyword != null && keyword.startsWith("oac:")) {
            var oac = keyword.replace("oac:", "");
            var targetEl = target;
            var additionalValues = XPATH.createElementFromXPath(targetEl, "/igc/data-sources/data-source/data-source-instance/general/general-additional-values");
            var additionalValue = additionalValues.appendChild(targetEl.getOwnerDocument().createElement("general-additional-value"));
            XMLUtils.createOrReplaceTextNode(XPATH.createElementFromXPath(additionalValue, "field-key"), "oac");
            XMLUtils.createOrReplaceTextNode(XPATH.createElementFromXPath(additionalValue, "field-data"), oac);

            // remove as normal keyword
            var terms = XPATH.getNodeList(target, "/igc/data-sources/data-source/data-source-instance/subject-terms/uncontrolled-term");
            for (var j=0; j<terms.getLength(); j++ ) {
                var termNode = terms.item(j);
                if (termNode.getTextContent() === keyword) {
                    XPATH.removeElementAtXPath(termNode, ".");
                }
            }
        }
    }

    /**
     * Zusätzliches Feld -> Textbox: "environmentDescription"
     */
    var targetEl = target;
    var environmentDescription = XPATH.getNode(source, "//gmd:MD_DataIdentification/gmd:environmentDescription/gco:CharacterString");
    if (environmentDescription) {
        var additionalValues = XPATH.createElementFromXPath(targetEl, "/igc/data-sources/data-source/data-source-instance/general/general-additional-values");
        var additionalValue = additionalValues.appendChild(targetEl.getOwnerDocument().createElement("general-additional-value"));
        XMLUtils.createOrReplaceTextNode(XPATH.createElementFromXPath(additionalValue, "field-key"), "environmentDescription");
        XMLUtils.createOrReplaceTextNode(XPATH.createElementFromXPath(additionalValue, "field-data"), environmentDescription.getTextContent());
    }
}
