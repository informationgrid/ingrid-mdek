/*
 * **************************************************-
 * Ingrid Portal MDEK Application
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
var mapUseTypeToCodelist = {
        copyright: 10,
        license: 11,
        intellectualPropertyRights: 13,
        restricted: 14
};
var mapUseTypeToCodelistOpenData = {
        copyright: 2,
        license: 3,
        intellectualPropertyRights: 5,
        restricted: 6
};

var mappingDescriptionBkg = {"mappings":[
    {   
        "execute": {
            "funct": mapAccessConstraintsBkg
        }
    },
    {   
        "execute": {
            "funct": mapUseConstraintsBkg
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
        var entryId = null;
        var freeTextValue = null;
        var accessConstraintNodes = null;
        var legalNode = null;
        
        // get LAST legal constraint that contains access constraints, this is the BKG ONE !
        for (var i=0; i < legalConstraints.getLength(); i++) {
            var myLegalNode = legalConstraints.item(i);
            var myAccessConstraintNodes = XPATH.getNodeList(myLegalNode, "gmd:accessConstraints/gmd:MD_RestrictionCode");
            if (myAccessConstraintNodes.getLength() > 0) {
            	legalNode = myLegalNode;
            	accessConstraintNodes = myAccessConstraintNodes;
            }
        }

        if (hasValue(accessConstraintNodes)) {

        	// DEBUG
        	if (log.isDebugEnabled()) {
                for (var i=0; i < accessConstraintNodes.getLength(); i++) {
                    var isoValue = XPATH.getString(accessConstraintNodes.item(i), "./@codeListValue");
                	log.debug("BKG: Found 'gmd:accessConstraints/gmd:MD_RestrictionCode codeListValue=" + isoValue + "'");
            	}
        	}

            if (accessConstraintNodes.getLength() === 1) {
                // otherRestrictions
                var accessNode1 = XPATH.getString(accessConstraintNodes.item(0), "./@codeListValue");
                if (accessNode1 === "otherRestrictions") {
                    var otherConstraintNodes = XPATH.getNodeList(legalNode, "gmd:otherConstraints/gco:CharacterString");
                    if (otherConstraintNodes.getLength() === 1) {
                        var otherValue = XPATH.getString(otherConstraintNodes.item(0), ".");
                        // could be only selection value
                        entryId = getSysListEntryKey(10002, otherValue);
                        
                        // or only free text
                        if (!entryId) freeTextValue = otherValue;
                        
                    } else if (otherConstraintNodes.getLength() === 2) {
                        // a selection and a free text is available 
                        var selectValue = XPATH.getString(otherConstraintNodes.item(0), ".");
                        freeTextValue = XPATH.getString(otherConstraintNodes.item(1), ".");
                        entryId = getSysListEntryKey(10002, selectValue);
                    }
                    if (addAccessValuesToDoc(target, entryId, freeTextValue)) {
                        removeAccessConstraint(target, codeListService.getSysListEntryName(10002, entryId));
                        removeAccessConstraint(target, freeTextValue);
                    }
                } else {
                    log.warn("BKG: Resource contraint is not as expected. Access constraints should be of type otherRestrictions");
                }
            } else if (accessConstraintNodes.getLength() === 2) {
                // copyright and otherRestrictions
                var accessNode1 = XPATH.getString(accessConstraintNodes.item(0), "./@codeListValue");
                var accessNode2 = XPATH.getString(accessConstraintNodes.item(1), "./@codeListValue");
                
                var arrayNodes = [accessNode1, accessNode2];
                if ( (arrayNodes.indexOf("copyright") !== -1 || arrayNodes.indexOf("license") !== -1 || arrayNodes.indexOf("intellectualPropertyRights") !== -1 || arrayNodes.indexOf("restricted") !== -1) && arrayNodes.indexOf("otherRestrictions") !== -1) {
                
                    var otherConstraintNodes = XPATH.getNodeList(legalNode, "gmd:otherConstraints/gco:CharacterString");
                    if (otherConstraintNodes.getLength() === 1) {
                        var otherValue = XPATH.getString(otherConstraintNodes.item(0), ".");
                        // check if value is from codelist
                        entryId = getSysListEntryKey(10002, otherValue);
                        
                        if (!entryId) {
                            freeTextValue = otherValue
                            entryId = mapAccessTypeToCodelist[accessNode1];
                        }
                    	
                    } else if (otherConstraintNodes.getLength() === 2) {
                        var selectValue = XPATH.getString(otherConstraintNodes.item(0), ".");
                        freeTextValue = XPATH.getString(otherConstraintNodes.item(1), ".");
                        entryId = getSysListEntryKey(10002, selectValue);
                    }
                    
                    if (addAccessValuesToDoc(target, entryId, freeTextValue)) {
                        removeAccessConstraint(target, codeListService.getSysListEntryName(10002, entryId));
                        removeAccessConstraint(target, freeTextValue);
                        removeAccessConstraint(target, accessNode1);                    	
                    }
                } else {
                    log.warn("BKG: Resource contraint is not as expected. Access constraints should be of type otherRestrictions and one of these: copyright, license, intellectualPropertyRights, restricted");
                }
                
            } else if (accessConstraintNodes.getLength() === 3) {
                // copyright, license and otherRestrictions
                var accessNode1 = XPATH.getString(accessConstraintNodes.item(0), "./@codeListValue");
                var accessNode2 = XPATH.getString(accessConstraintNodes.item(1), "./@codeListValue");
                var accessNode3 = XPATH.getString(accessConstraintNodes.item(2), "./@codeListValue");
                
                var arrayNodes = [accessNode1, accessNode2, accessNode3];
                if ( arrayNodes.indexOf("copyright") !== -1 && arrayNodes.indexOf("license") !== -1 && arrayNodes.indexOf("otherRestrictions") !== -1) {
                    var otherConstraintNodes = XPATH.getNodeList(legalNode, "gmd:otherConstraints/gco:CharacterString");
                    if (otherConstraintNodes.getLength() === 1) {
                        freeTextValue = XPATH.getString(otherConstraintNodes.item(0), ".");
                        entryId = 7; // "Template für copyright / license"
                    }
                    if (addAccessValuesToDoc(target, entryId, freeTextValue)) {
                        removeAccessConstraint(target, codeListService.getSysListEntryName(10002, entryId));
                        removeAccessConstraint(target, freeTextValue);
                        removeAccessConstraint(target, accessNode1);
                        removeAccessConstraint(target, accessNode2);                    	
                    }
                } else {
                    log.warn("BKG: Resource contraint is not as expected. Access constraints should be of type copyright, license and otherRestrictions");
                }
                
            }
        }
    }
}

function mapUseConstraintsBkg(source, target) {
    var legalConstraints = XPATH.getNodeList(source, "//gmd:identificationInfo//gmd:resourceConstraints/gmd:MD_LegalConstraints");
    if (hasValue(legalConstraints)) {
        var entryId = null;
        var freeTextValue = null;
        var useConstraintNodes = null;
        var legalNode = null;
        
        // get LAST legal constraint that contains use constraints, this is the BKG ONE !
        for (var i=0; i < legalConstraints.getLength(); i++) {
            var myLegalNode = legalConstraints.item(i);
            var myUseConstraintNodes = XPATH.getNodeList(myLegalNode, "gmd:useConstraints/gmd:MD_RestrictionCode");
            if (myUseConstraintNodes.getLength() > 0) {
            	legalNode = myLegalNode;
            	useConstraintNodes = myUseConstraintNodes;
            }
        }
        
        if (hasValue(useConstraintNodes)) {

        	// DEBUG
        	if (log.isDebugEnabled()) {
                for (var i=0; i < useConstraintNodes.getLength(); i++) {
                    var isoValue = XPATH.getString(useConstraintNodes.item(i), "./@codeListValue");
                	log.debug("BKG: Found 'gmd:useConstraints/gmd:MD_RestrictionCode  codeListValue=" + isoValue + "'");
            	}        		
        	}

        	var codelistIdBkg = 10004;
        	var mapUseTypeToCodelistBkg = mapUseTypeToCodelist;

        	// is open data ? then different codelist
        	var isOpenData = checkOpenData(target);
        	if (isOpenData) {
                log.info("BKG: Record is opendata, we change useConstraint codelist to 10006");
            	codelistIdBkg = 10006;        		
            	mapUseTypeToCodelistBkg = mapUseTypeToCodelistOpenData;
        	}

            if (useConstraintNodes.getLength() === 1) {
                // otherRestrictions
                var accessNode1 = XPATH.getString(useConstraintNodes.item(0), "./@codeListValue");
                if (accessNode1 === "otherRestrictions") {
                    var otherConstraintNodes = XPATH.getNodeList(legalNode, "gmd:otherConstraints/gco:CharacterString");
                    if (otherConstraintNodes.getLength() === 1) {
                        var otherValue = XPATH.getString(otherConstraintNodes.item(0), ".");
                        // could be only selection value
                        entryId = getSysListEntryKey(codelistIdBkg, otherValue);
                        
                        // or only free text
                        if (!entryId) freeTextValue = otherValue;
                        
                    } else if (otherConstraintNodes.getLength() === 2) {
                        // a selection and a free text is available 
                        var selectValue = XPATH.getString(otherConstraintNodes.item(0), ".");
                        freeTextValue = XPATH.getString(otherConstraintNodes.item(1), ".");
                        entryId = getSysListEntryKey(codelistIdBkg, selectValue);
                    }
                    if (addUseValuesToDoc(target, entryId, freeTextValue)) {
                        removeUseConstraint(target, codeListService.getSysListEntryName(codelistIdBkg, entryId));
                        removeUseConstraint(target, freeTextValue);
                    }
                } else {
                    log.warn("BKG: Resource constraint is not as expected. Access constraints should be of type otherRestrictions");
                }
                
            } else if (useConstraintNodes.getLength() === 2) {
                // copyright and otherRestrictions
                var accessNode1 = XPATH.getString(useConstraintNodes.item(0), "./@codeListValue");
                var accessNode2 = XPATH.getString(useConstraintNodes.item(1), "./@codeListValue");

                var arrayNodes = [accessNode1, accessNode2];
                if ( (arrayNodes.indexOf("copyright") !== -1 || arrayNodes.indexOf("license") !== -1 || arrayNodes.indexOf("intellectualPropertyRights") !== -1 || arrayNodes.indexOf("restricted") !== -1) && arrayNodes.indexOf("otherRestrictions") !== -1) {
                
                    var otherConstraintNodes = XPATH.getNodeList(legalNode, "gmd:otherConstraints/gco:CharacterString");

                	// DEBUG
                	if (log.isDebugEnabled()) {
                        for (var i=0; i < otherConstraintNodes.getLength(); i++) {
                            var isoValue = XPATH.getString(otherConstraintNodes.item(i), ".");
                        	log.debug("BKG: Found gmd:otherConstraints/gco:CharacterString: '" + isoValue + "'");
                    	}                		
                	}

                    if (otherConstraintNodes.getLength() === 1) {
                        var otherValue = XPATH.getString(otherConstraintNodes.item(0), ".");
                        // check if value is from codelist
                        entryId = getSysListEntryKey(codelistIdBkg, otherValue);
                        
                        if (!entryId) {
                            freeTextValue = otherValue
                            entryId = mapUseTypeToCodelistBkg[accessNode1];
                        }
                        
                    } else if (otherConstraintNodes.getLength() === 2) {
                        var selectValue = XPATH.getString(otherConstraintNodes.item(0), ".");
                        freeTextValue = XPATH.getString(otherConstraintNodes.item(1), ".");
                        entryId = getSysListEntryKey(codelistIdBkg, selectValue);
                        if (checkJSON(freeTextValue)) {
                            freeTextValue = null;
                        }
                        
                    } else if (otherConstraintNodes.getLength() === 3) {
                        var selectValue = XPATH.getString(otherConstraintNodes.item(0), ".");
                        // item==1 should be json here, which is not needed
                        freeTextValue = XPATH.getString(otherConstraintNodes.item(2), ".");
                        entryId = getSysListEntryKey(codelistIdBkg, selectValue);
                    }

                    if (addUseValuesToDoc(target, entryId, freeTextValue)) {
                        removeUseConstraint(target, codeListService.getSysListEntryName(codelistIdBkg, entryId));
                        removeUseConstraint(target, freeTextValue);
                        removeUseConstraint(target, accessNode1);                    	
                    }
                } else {
                    log.warn("BKG: Resource contraint is not as expected. Access constraints should be of type otherRestrictions and one of these: copyright, license, intellectualPropertyRights, restricted");
                }
                
            } else if (useConstraintNodes.getLength() === 3) {
                // copyright, license and otherRestrictions
                var accessNode1 = XPATH.getString(useConstraintNodes.item(0), "./@codeListValue");
                var accessNode2 = XPATH.getString(useConstraintNodes.item(1), "./@codeListValue");
                var accessNode3 = XPATH.getString(useConstraintNodes.item(2), "./@codeListValue");
                
                var arrayNodes = [accessNode1, accessNode2, accessNode3];
                if ( arrayNodes.indexOf("copyright") !== -1 && arrayNodes.indexOf("license") !== -1 && arrayNodes.indexOf("otherRestrictions") !== -1) {
                    var otherConstraintNodes = XPATH.getNodeList(legalNode, "gmd:otherConstraints/gco:CharacterString");
                    if (otherConstraintNodes.getLength() === 1) {
                        freeTextValue = XPATH.getString(otherConstraintNodes.item(0), ".");
                        entryId = 12; // "Template für copyright / license"
                    }
                    log.debug("BKG: Use constraint 3 nodes, add values: " + entryId + " , " + freeTextValue);
                    if (addUseValuesToDoc(target, entryId, freeTextValue)) {
                        removeUseConstraint(target, codeListService.getSysListEntryName(codelistIdBkg, entryId));
                        removeUseConstraint(target, freeTextValue);
                        removeUseConstraint(target, accessNode1);
                        removeUseConstraint(target, accessNode2);
                    }                    
                } else {
                    log.warn("BKG: Resource contraint is not as expected. Access constraints should be of type copyright, license and otherRestrictions");
                }
                
            }
        }
    }
}

function getSysListEntryKey(codelistId, entryName) {
	if (hasValue(entryName)) {
	    // trim and remove praefix, defined in regular csw import mapper
		entryName = removeConstraintPraefix(entryName);
	}

	log.debug("BKG: getSysListEntryKey -> codelistId='" + codelistId + "', entryName='" + entryName +"'");

	var retValue = codeListService.getSysListEntryKey(codelistId, entryName, "de", true);

	log.debug("BKG: getSysListEntryKey found key='" + retValue + "'");

	return retValue;
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
function addAccessValuesToDoc(target, codelistEntryId, freeText) {
    
	if (!hasValue(codelistEntryId) && !hasValue(freeText)) {
		return false;
	}
    
    var additionalFieldsNode = addOrCreateAdditionalFields(target);
    
    addAdditionalValue(additionalFieldsNode, "bkg_accessConstraints", "bkg_accessConstraints_select", codelistEntryId, null);
    addAdditionalValue(additionalFieldsNode, "bkg_accessConstraints", "bkg_accessConstraints_freeText", null, freeText);

    return true;
}

function addUseValuesToDoc(target, codelistEntryId, freeText) {
	
	log.debug("BKG: addUseValuesToDoc -> codelistEntryId='" + codelistEntryId + "', freeText='" + freeText +"'");

	if (!hasValue(codelistEntryId) && !hasValue(freeText)) {
		return false;
	}
    
    var additionalFieldsNode = addOrCreateAdditionalFields(target);
    
    addAdditionalValue(additionalFieldsNode, "bkg_useConstraints", "bkg_useConstraints_select", codelistEntryId, null);

    // remove praefix, defined in regular csw import mapper
    freeText = removeConstraintPraefix(freeText);
    addAdditionalValue(additionalFieldsNode, "bkg_useConstraints", "bkg_useConstraints_freeText", null, freeText);
    
    return true;
}

function addOrCreateAdditionalFields(target) {
    var addValuesNode = XPATH.getNode(target, "/igc/data-sources/data-source/data-source-instance/general/general-additional-values");
    if (!hasValue(addValuesNode)) {
        log.debug("bkg: additional fields ... create")
        addValuesNode = XPATH.createElementFromXPath(target, "general-additional-values");
    }
    return addValuesNode;
}

function addAdditionalValue(additionalFieldsNode, parent, key, codelistEntryId, data) {
    var valueNodeSelect = XPATH.createElementFromXPathAsSibling(additionalFieldsNode, "general-additional-value");
    XMLUtils.createOrReplaceAttribute(valueNodeSelect, "line", "1");
    var keyVar = XPATH.createElementFromXPath(valueNodeSelect, "field-key");
    XMLUtils.createOrReplaceTextNode(keyVar, key);
    var dataVar = XPATH.createElementFromXPath(valueNodeSelect, "field-data");
    
    if (codelistEntryId !== null) XMLUtils.createOrReplaceAttribute(dataVar, "id", codelistEntryId);
    if (data !== null) XMLUtils.createOrReplaceTextNode(dataVar, data);
    
    var parentNode = XPATH.createElementFromXPath(valueNodeSelect, "field-key-parent");
    XMLUtils.createOrReplaceTextNode(parentNode, parent);
}

function removeAccessConstraint(target, name) {
	log.debug("BKG: Try to remove accessConstraint: '" + name + "'");
    if (hasValue(name)) {
        var nodes = XPATH.getNodeList(target, "//access-constraint/restriction");
        if (hasValue(nodes)) {
            removeConstraint(target, name, nodes);        	
        }
    }
}

function removeUseConstraint(target, name) {
	log.debug("BKG: Try to remove useConstraint: '" + name + "'");
    if (hasValue(name)) {
        var nodes = XPATH.getNodeList(target, "//use-constraint/license");
        if (hasValue(nodes)) {
            name = removeConstraintPraefix(name);
            removeConstraint(target, name, nodes);
        }
    }
}

function removeConstraint(target, name, nodes) {
    for (var i=0; i<nodes.getLength(); i++ ) {
        var constraintName = XPATH.getString(nodes.item(i), ".");
        if (hasValue(constraintName)) {
            if (constraintName.trim() === name.trim()) {
            	log.debug("BKG: REMOVING constraint: '" + name + "'");
                XPATH.removeElementAtXPath(nodes.item(i), "..");
            }
        }
    }
}

function checkOpenData(target) {
    var nodes = XPATH.getNodeList(target, '//is-open-data[text()="Y"]');
    return (hasValue(nodes) && (nodes.getLength() > 0));
}

function checkJSON(val) {
	if (hasValue(val) &&
			val.trim().startsWith('{') &&
			val.trim().endsWith('}')) {
		return true;
	}
	return false;
}
