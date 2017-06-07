/*
 * **************************************************-
 * Ingrid Portal MDEK Application
 * ==================================================
 * Copyright (C) 2014 - 2017 wemove digital solutions GmbH
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

var DEBUG = 1;
var INFO = 2;
var WARN = 3;
var ERROR = 4;

var mapAccessTypeToCodelist = {
        copyright: 5,
        license: 6,
        intellectualPropertyRights: 8,
        restricted: 9
};

var mappingDescriptionBkg = {"mappings":[
    {   
        "execute": {
            "funct": mapAccessConstraintsBkg
        }
    }
]};

if (log.isDebugEnabled()) {
    log.debug("BKG: mapping CSW 2.0.2 AP ISO 1.0 document to IGC import document.");
}

mapToTarget(mappingDescriptionBkg, source, target.getDocumentElement());



function mapAccessConstraintsBkg(source, target) {
    var legalConstraints = XPATH.getNodeList(source, "//gmd:identificationInfo//gmd:resourceConstraints/gmd:MD_LegalConstraints");
    if (hasValue(legalConstraints)) {
        // get only first element where bkg specific info should be found
        var entryId = null;
        var freeTextValue = null;
        var legalNode = legalConstraints.item(0);
        var accessConstraintNodes = XPATH.getNodeList(legalNode, "gmd:accessConstraints/gmd:MD_RestrictionCode");
        
        if (hasValue(accessConstraintNodes)) {
            if (accessConstraintNodes.getLength() === 1) {
                // otherRestrictions
                var accessNode1 = XPATH.getString(accessConstraintNodes.item(0), ".");
                if (accessNode1 === "otherRestrictions") {
                    var otherConstraintNodes = XPATH.getNodeList(legalNode, "gmd:otherConstraints/gco:CharacterString");
                    if (otherConstraintNodes.getLength() === 1) {
                        var otherValue = XPATH.getString(otherConstraintNodes.item(0), ".");
                        // could be only selection value
                        entryId = codeListService.getSysListEntryKey(10002, otherValue, "de");
                        
                        // or only free text
                        if (!entryId) freeTextValue = otherValue;
                        
                    } else if (otherConstraintNodes.getLength() === 2) {
                        // a selection and a free text is available 
                        var selectValue = XPATH.getString(otherConstraintNodes.item(0), ".");
                        freeTextValue = XPATH.getString(otherConstraintNodes.item(1), ".");
                        entryId = codeListService.getSysListEntryKey(10002, selectValue, "de");
                    }
                    addValuesToDoc(target, entryId, freeTextValue);

                    removeAccessConstraint(target, codeListService.getSysListEntryName(10002, entryId));
                    removeAccessConstraint(target, freeTextValue);
                } else {
                    log.warn("BKG: Resource contraint is not as expected. Access constraints should be of type otherRestrictions");
                }
            } else if (accessConstraintNodes.getLength() === 2) {
                // copyright and otherRestrictions
                var accessNode1 = XPATH.getString(accessConstraintNodes.item(0), ".");
                var accessNode2 = XPATH.getString(accessConstraintNodes.item(1), ".");
                
                var arrayNodes = [accessNode1, accessNode2];
                if ( (arrayNodes.indexOf("copyright") !== -1 || arrayNodes.indexOf("license") !== -1 || arrayNodes.indexOf("intellectualPropertyRights") !== -1 || arrayNodes.indexOf("restricted") !== -1) && arrayNodes.indexOf("otherRestrictions") !== -1) {
                
                    var otherConstraintNodes = XPATH.getNodeList(legalNode, "gmd:otherConstraints/gco:CharacterString");
                    if (otherConstraintNodes.getLength() === 1) {
                        freeTextValue = XPATH.getString(otherConstraintNodes.item(0), ".");
                        entryId = mapAccessTypeToCodelist[accessNode1];
                    } else if (otherConstraintNodes.getLength() === 2) {
                        var selectValue = XPATH.getString(otherConstraintNodes.item(0), ".");
                        freeTextValue = XPATH.getString(otherConstraintNodes.item(1), ".");
                        entryId = codeListService.getSysListEntryKey(10002, selectValue, "de");
                    }
                    
                    addValuesToDoc(target, entryId, freeTextValue);
                    removeAccessConstraint(target, codeListService.getSysListEntryName(10002, entryId));
                    removeAccessConstraint(target, freeTextValue);
                    removeAccessConstraint(target, accessNode1);
                } else {
                    log.warn("BKG: Resource contraint is not as expected. Access constraints should be of type otherRestrictions and one of these: copyright, license, intellectualPropertyRights, restricted");
                }
                
            } else if (accessConstraintNodes.getLength() === 3) {
                // copyright, license and otherRestrictions
                var accessNode1 = XPATH.getString(accessConstraintNodes.item(0), ".");
                var accessNode2 = XPATH.getString(accessConstraintNodes.item(1), ".");
                var accessNode3 = XPATH.getString(accessConstraintNodes.item(2), ".");
                
                var arrayNodes = [accessNode1, accessNode2, accessNode3];
                if ( arrayNodes.indexOf("copyright") !== -1 && arrayNodes.indexOf("license") !== -1 && arrayNodes.indexOf("otherRestrictions") !== -1) {
                    var otherConstraintNodes = XPATH.getNodeList(legalNode, "gmd:otherConstraints/gco:CharacterString");
                    if (otherConstraintNodes.getLength() === 1) {
                        freeTextValue = XPATH.getString(otherConstraintNodes.item(0), ".");
                        entryId = 7; // "Template für copyright / license"
                    }
                    addValuesToDoc(target, entryId, freeTextValue);
                    
                    removeAccessConstraint(target, codeListService.getSysListEntryName(10002, entryId));
                    removeAccessConstraint(target, freeTextValue);
                    removeAccessConstraint(target, accessNode1);
                    removeAccessConstraint(target, accessNode2);
                } else {
                    log.warn("BKG: Resource contraint is not as expected. Access constraints should be of type copyright, license and otherRestrictions");
                }
                
            }
        }
    }
}

//<general-additional-values>
//  <general-additional-value line="1">
//    <field-key>bkg_accessConstraints_freeText</field-key>
//    <field-data>mein freier Text</field-data>
//    <field-key-parent>bkg_accessConstraints</field-key-parent>
//  </general-additional-value>
//  <general-additional-value line="1">
//    <field-key>bkg_accessConstraints_select</field-key>
//    <field-data id="4"/>
//    <field-key-parent>bkg_accessConstraints</field-key-parent>
//  </general-additional-value>
//</general-additional-values>
function addValuesToDoc(target, codelistEntryId, freeText) {
    
    var additionalFieldsNode = addOrCreateAdditionalFields(target);
    
    addAdditionalValue(additionalFieldsNode, "bkg_accessConstraints_select", codelistEntryId, null);
    addAdditionalValue(additionalFieldsNode, "bkg_accessConstraints_freeText", null, freeText);
}

function addOrCreateAdditionalFields(target) {
    var addValuesNode = XPATH.getNode(target, "/igc/data-sources/data-source/data-source-instance/general/general-additional-values");
    if (!hasValue(addValuesNode)) {
        log.debug("bkg: additional fields ... create")
        addValuesNode = XPATH.createElementFromXPath(target, "general-additional-values");
    }
    return addValuesNode;
}

function addAdditionalValue(additionalFieldsNode, key, codelistEntryId, data) {
    var valueNodeSelect = XPATH.createElementFromXPathAsSibling(additionalFieldsNode, "general-additional-value");
    XMLUtils.createOrReplaceAttribute(valueNodeSelect, "line", "1");
    var keyVar = XPATH.createElementFromXPath(valueNodeSelect, "field-key");
    XMLUtils.createOrReplaceTextNode(keyVar, key);
    var dataVar = XPATH.createElementFromXPath(valueNodeSelect, "field-data");
    
    if (codelistEntryId !== null) XMLUtils.createOrReplaceAttribute(dataVar, "id", codelistEntryId);
    if (data !== null) XMLUtils.createOrReplaceTextNode(dataVar, data);
    
    var parent = XPATH.createElementFromXPath(valueNodeSelect, "field-key-parent");
    XMLUtils.createOrReplaceTextNode(parent, "bkg_accessConstraints");
}

function removeAccessConstraint(target, name) {
    if (hasValue(name)) {
        var nodes = XPATH.getNodeList(target, "//access-constraint/restriction");
        if (hasValue(nodes)) {
            for (var i=0; i<nodes.getLength(); i++ ) {
                var accessName = XPATH.getString(nodes.item(i), ".");
                if (accessName === name) {
                    log.debug("removing node: " + i);
                    XPATH.removeElementAtXPath(nodes.item(i), "..");
                }
            }
        }
    }
}