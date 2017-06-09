/*
 * **************************************************-
 * InGrid-iPlug DSC
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
	CAPABILITIES = Java.type('de.ingrid.utils.capabilities.CapabilitiesUtils');
}

importPackage(Packages.org.w3c.dom);
importPackage(Packages.de.ingrid.iplug.dsc.om);



if (log.isDebugEnabled()) {
    log.debug("BKG: Mapping source record to idf document: " + sourceRecord.toString());
}

if (!(sourceRecord instanceof DatabaseSourceRecord)) {
    throw new IllegalArgumentException("Record is no DatabaseRecord!");
}

//---------- <idf:idfMdMetadata> ----------
var body = DOM.getElement(idfDoc, "/idf:html/idf:body/idf:idfMdMetadata");

var objId = sourceRecord.get("id");
var objRows = SQL.all("SELECT * FROM t01_object WHERE id=?", [objId]);
for (i=0; i<objRows.size(); i++) {
    var objRow = objRows.get(i);
    var objUuid = objRow.get("obj_uuid");
    var objClass = objRow.get("obj_class");
    var objParentUuid = null; // will be set below
    
    // local variables
    var row = null;
    var rows = null;
    var value = null;
    var elem = null;
    
    handleBKGAccessConstraints();
    handleBKGUseConstraints();
    
}

function handleBKGAccessConstraints() {
    // get the container for the select and free text field
    var bkgAccessConstraintId = getAdditionalFieldFromObject(objId, null, 'bkg_accessConstraints', 'id');
    if (bkgAccessConstraintId) {
        
        // get value from select box
        var bkgAccessConstraintSelectListItem = getAdditionalFieldFromObject(null, bkgAccessConstraintId, 'bkg_accessConstraints_select', 'list_item_id');
        if (bkgAccessConstraintSelectListItem) {
            
            if (log.isDebugEnabled()) {
                log.debug("BKG access constraint field contains value: " + bkgAccessConstraintSelectListItem);
            }
            
            // get value from free text field
            var bkgAccessConstraintFreeText = getAdditionalFieldFromObject(null, bkgAccessConstraintId, 'bkg_accessConstraints_freeText', 'data');
            if (bkgAccessConstraintFreeText) {
                if (log.isDebugEnabled()) {
                    log.debug("BKG access constraint free text field contains value: " + bkgAccessConstraintFreeText);
                }
            }
            
            // add select value and free text to ISO depending on selection 
            var legalConstraint = getFirstNodeInIdentificationBefore().addElementAsSibling("gmd:resourceConstraints/gmd:MD_LegalConstraints");
            addAccessConstraints(legalConstraint, bkgAccessConstraintSelectListItem, bkgAccessConstraintFreeText);
        }
    }
}

function handleBKGUseConstraints() {
    // get the container for the select and free text field
    var bkgUseConstraintId = getAdditionalFieldFromObject(objId, null, 'bkg_useConstraints', 'id');
    if (bkgUseConstraintId) {
        
        // get value from select box
        var bkgUseConstraintSelectListItem = getAdditionalFieldFromObject(null, bkgUseConstraintId, 'bkg_useConstraints_select', 'list_item_id');
            
        if (log.isDebugEnabled()) {
            log.debug("BKG use constraint field contains value: " + bkgUseConstraintSelectListItem);
        }
        
        // get value from free text field
        var bkgUseConstraintFreeText = getAdditionalFieldFromObject(null, bkgUseConstraintId, 'bkg_useConstraints_freeText', 'data');
        if (bkgUseConstraintFreeText) {
            if (log.isDebugEnabled()) {
                log.debug("BKG use constraint free text field contains value: " + bkgUseConstraintFreeText);
            }
        }
        
        // add select value and free text to ISO depending on selection 
        var legalConstraint = getFirstNodeInIdentificationBefore().addElementAsSibling("gmd:resourceConstraints/gmd:MD_LegalConstraints");
        addUseConstraints(legalConstraint, bkgUseConstraintSelectListItem, bkgUseConstraintFreeText);
    }
}

/**
 * Get a value from an additional value with a given fieldId that belongs to an object or a parent.
 * objId or parentId must be null.
 * @param objId
 * @param parentId
 * @param fieldId
 * @param property
 * @returns
 */
function getAdditionalFieldFromObject(objId, parentId, fieldId, property) {
    var field = null;
    if (objId) {
        field = SQL.first("SELECT * FROM additional_field_data WHERE obj_id=? AND field_key=?", [objId, fieldId]);
    } else {
        field = SQL.first("SELECT * FROM additional_field_data WHERE parent_field_id=? AND field_key=?", [parentId, fieldId]);
    }
    if (hasValue(field)) {
        return field.get(property);
    } else {
        return null;
    }
}


function getIdentificationInfo() {
    var identificationInfo = null;
    if (objClass.equals("3")) {
        identificationInfo = DOM.getElement(body, "gmd:identificationInfo/srv:SV_ServiceIdentification");
    } else {
        identificationInfo = DOM.getElement(body, "gmd:identificationInfo/gmd:MD_DataIdentification");
    }
    return identificationInfo;
}

function getFirstNodeInIdentificationBefore() {
    var nodeOrder = ["gmd:resourceSpecificUsage", "gmd:descriptiveKeywords", "gmd:resourceFormat", "gmd:graphicOverview", "gmd:resourceMaintenance", 
        "gmd:pointOfContact", "gmd:status", "gmd:credit", "gmd:purpose", "gmd:abstract"];
    var identificationInfo = getIdentificationInfo();
    
    // get first element before "resourceConstraints", so that we can insert in front of all other entries
    var beforeResourceElement;
    for (var i=0; i<nodeOrder.length; i++) {
        beforeResourceElement = DOM.getElement(identificationInfo, nodeOrder[i]);
        if (beforeResourceElement) break;
    }
    
    return beforeResourceElement;
}

function addAccessConstraints(legalConstraint, codelistEntryId, valueFree) {
    if (codelistEntryId === null || codelistEntryId === undefined| codelistEntryId === "") {
        addAccessConstraintElements(legalConstraint, [], [valueFree]);
        return;
    }
    
    switch (codelistEntryId) {
    case "1":
        addAccessConstraintElements(legalConstraint, [], [TRANSF.getIGCSyslistEntryName(10002, codelistEntryId), valueFree]);
        break;
    case "2":
    case "3":
    case "4":
        addAccessConstraintElements(legalConstraint, ["copyright"], [TRANSF.getIGCSyslistEntryName(10002, codelistEntryId), valueFree]);
        break;
    case "5":
        addAccessConstraintElements(legalConstraint, ["copyright"], [valueFree]);
        break;
    case "6":
        addAccessConstraintElements(legalConstraint, ["license"], [valueFree]);
        break;
    case "7":
        addAccessConstraintElements(legalConstraint, ["copyright","license"], [valueFree]);
        break;
    case "8":
        addAccessConstraintElements(legalConstraint, ["intellectualPropertyRights"], [valueFree]);
        break;
    case "9":
        addAccessConstraintElements(legalConstraint, ["restricted"], [valueFree]);
        break;
    default:
        log.warn("Codelist entry not supported for list 10001: " + codelistEntryId);
    }
}

function addUseConstraints(legalConstraint, codelistEntryId, valueFree) {
    log.debug("BKG: Use Constraint codelist: " + codelistEntryId);
    if (codelistEntryId === null || codelistEntryId === undefined| codelistEntryId === "") {
        addUseConstraintElements(legalConstraint, [], [valueFree]);
        return;
    }
    
    switch (codelistEntryId) {
    case "1":
    case "2":
    case "3":
    case "4":
    case "5":
    case "6":
    case "7":
    case "8":
    case "9":
        
        // if opendata and other codelist was used, then add JSON as other constraint
        if (isOpenData()) {
            var codelistEntryName = TRANSF.getIGCSyslistEntryName(10005, codelistEntryId);
            var json = TRANSF.getISOCodeListEntryData(10005, codelistEntryName);
            
            addUseConstraintElements(legalConstraint, ["license"], [codelistEntryName, json, valueFree]);
            
        } else {
            addUseConstraintElements(legalConstraint, ["license"], [TRANSF.getIGCSyslistEntryName(10006, codelistEntryId), valueFree]);
            
        }
        break;
    case "10":
        addUseConstraintElements(legalConstraint, ["copyright"], [valueFree]);
        break;
    case "11":
        addUseConstraintElements(legalConstraint, ["license"], [valueFree]);
        break;
    case "12":
        addUseConstraintElements(legalConstraint, ["copyright", "license"], [valueFree]);
        break;
    case "13":
        addUseConstraintElements(legalConstraint, ["intellectualPropertyRights"], [valueFree]);
        break;
    case "14":
        addUseConstraintElements(legalConstraint, ["restricted"], null, [valueFree]);
        break;
    default:
        log.warn("Codelist entry not supported for list 10003: " + codelistEntryId);
    }
}

/**
 * 
 * @param legalConstraint
 * @param restrictionCodeValues
 * @param {string[]} otherConstraints
 * @returns
 */
function addAccessConstraintElements(legalConstraint, restrictionCodeValues, otherConstraints) {
    for (var i=0; i<restrictionCodeValues.length; i++) {
        legalConstraint.addElement("gmd:accessConstraints/gmd:MD_RestrictionCode")
            .addAttribute("codeListValue", restrictionCodeValues[i])
            .addAttribute("codeList", globalCodeListAttrURL + "#MD_RestrictionCode")
            .addText(restrictionCodeValues[i]);
    }
    
    legalConstraint.addElement("gmd:accessConstraints/gmd:MD_RestrictionCode")
        .addAttribute("codeListValue", "otherRestrictions")
        .addAttribute("codeList", globalCodeListAttrURL + "#MD_RestrictionCode")
        .addText("otherRestrictions");
    
    if (otherConstraints) {
        for (var j=0; j<otherConstraints.length; j++) {
            if (otherConstraints[j]) {
                legalConstraint
                .addElement("gmd:otherConstraints/gco:CharacterString")
                .addText(otherConstraints[j]);
            }
        }
    }
}

/**
 * 
 * @param legalConstraint
 * @param restrictionCodeValues
 * @param {string[]} otherConstraints
 * @returns
 */
function addUseConstraintElements(legalConstraint, restrictionCodeValues, otherConstraints) {
    for (var i=0; i<restrictionCodeValues.length; i++) {
        legalConstraint.addElement("gmd:useConstraints/gmd:MD_RestrictionCode")
            .addAttribute("codeListValue", restrictionCodeValues[i])
            .addAttribute("codeList", globalCodeListAttrURL + "#MD_RestrictionCode")
            .addText(restrictionCodeValues[i]);
    }
    
    legalConstraint.addElement("gmd:useConstraints/gmd:MD_RestrictionCode")
        .addAttribute("codeListValue", "otherRestrictions")
        .addAttribute("codeList", globalCodeListAttrURL + "#MD_RestrictionCode")
        .addText("otherRestrictions");
    
    if (otherConstraints) {
        for (var j=0; j<otherConstraints.length; j++) {
            if (otherConstraints[j]) {
                legalConstraint
                    .addElement("gmd:otherConstraints/gco:CharacterString")
                    .addText(otherConstraints[j]);
            }
        }
    }
}

function isOpenData() {
    var value = objRow.get("is_open_data");
    return hasValue(value) && value.equals('Y');
}

function isInspireRelevant() {
    var value = objRow.get("is_inspire_relevant");
    return hasValue(value) && value.equals('Y');
}
