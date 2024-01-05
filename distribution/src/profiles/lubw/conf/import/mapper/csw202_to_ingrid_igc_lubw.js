/*
 * **************************************************-
 * Ingrid Portal MDEK Application
 * ==================================================
 * Copyright (C) 2014 - 2024 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.2 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */

var mappingDescriptionLUBW = {"mappings":[
    {
        "execute": {
            "funct": mapSpecialFields
        }
    },
    {
        "execute": {
            "funct": convertBrowseGraphicLinkToRelative
        }
    },
    {
        "execute": {
            "funct": removeBrowseGraphicDefaultFileDescription
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
     * Existierendes Feld -> Checkbox: "Zugang geschützt"
     */
    for (var i=0; i<keywords.getLength(); i++ ) {
        var keyword = keywords.item(i).getTextContent();
        if (keyword != null && keyword.startsWith("zugangGeschuetzt:")) {
            var zugangGeschuetzt = keyword.replace("zugangGeschuetzt:", "");
            var targetEl = target;
            var hasAccessConstraintNode = XPATH.createElementFromXPath(targetEl, "/igc/data-sources/data-source/data-source-instance/technical-domain/service/has-access-constraint");
            XMLUtils.createOrReplaceTextNode(hasAccessConstraintNode, zugangGeschuetzt == "true" ? "Y" : "N");

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

function convertBrowseGraphicLinkToRelative(source, target) {
    /**
     * Vorschaubilder: Umwandlung von absolutem zu relativem Link
     * (damit Vorschaubilder korrekt verknüpft sind und nicht im Papierkorb landen)
     */
    var browseGraphic = XPATH.getNode(source, "//gmd:MD_DataIdentification/gmd:graphicOverview/gmd:MD_BrowseGraphic/gmd:fileName/gco:CharacterString");
    if (browseGraphic && browseGraphic.getTextContent()) {
        var targetEl = target;
        var relativeLink = browseGraphic.getTextContent().replace(/.*(ingrid-group_ige-iplug\/.*)/, '$1');
        // the following does not work because at this time, the IGC does not contain "preview-image"
//        var igcPath = "/igc/data-sources/data-source/data-source-instance/available-linkage[./linkage-name = 'preview-image']/linkage-url";
        var linkageUrlPath = "/igc/data-sources/data-source/data-source-instance/available-linkage[./linkage-reference[@id='9000']]/linkage-url";
        var linkageUrlNode = XPATH.getNode(targetEl, linkageUrlPath);
        if (linkageUrlNode) {
            XMLUtils.createOrReplaceTextNode(linkageUrlNode, relativeLink);
        }
    }
}

function removeBrowseGraphicDefaultFileDescription(source, target) {
    /**
     * Vorschaubilder: Entfernen der gmd:fileDescription falls sie den Default-Wert enthält (#5500)
     */
    var targetEl = target;
    var defaultValue = 'grafische Darstellung';
    var linkageDescriptionPath = "/igc/data-sources/data-source/data-source-instance/available-linkage[./linkage-reference[@id='9000']]/linkage-description";
    var linkageDescriptionNode = XPATH.getNode(targetEl, linkageDescriptionPath);
    if (linkageDescriptionNode && linkageDescriptionNode.getTextContent() == defaultValue) {
        XPATH.removeElementAtXPath(linkageDescriptionNode, '.');
    }
}
