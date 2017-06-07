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
    
    // get the container for the select and free text field
    var bkgAccessConstraintContainerRow = SQL.first("SELECT * FROM additional_field_data WHERE obj_id=? AND field_key=?", [objId, 'bkg_accessConstraints']);
    if (hasValue(bkgAccessConstraintContainerRow)) {
        var nodeOrder = ["gmd:resourceSpecificUsage", "gmd:descriptiveKeywords", "gmd:resourceFormat", "gmd:graphicOverview", "gmd:resourceMaintenance", 
            "gmd:pointOfContact", "gmd:status", "gmd:credit", "gmd:purpose", "gmd:abstract"];
        var containerId = bkgAccessConstraintContainerRow.get("id");
        
        var identificationInfo;
        if (objClass.equals("3")) {
            identificationInfo = DOM.getElement(body, "gmd:identificationInfo/srv:SV_ServiceIdentification");
        } else {
            identificationInfo = DOM.getElement(body, "gmd:identificationInfo/gmd:MD_DataIdentification");
        }
        
        // get value from select box
        var bkgAccessConstraintRow = SQL.first("SELECT * FROM additional_field_data WHERE parent_field_id=? AND field_key=?", [containerId, 'bkg_accessConstraints_select']);
        if (hasValue(bkgAccessConstraintRow)) {
            var value = bkgAccessConstraintRow.get("list_item_id");
            var valueFree;
            
            if (log.isDebugEnabled()) {
                log.debug("BKG access constraint field contains value: " + value);
            }
            
            // get first element before "resourceConstraints", so that we can insert in front of all other entries
            var beforeResourceElement;
            for (var i=0; i<nodeOrder.length; i++) {
                beforeResourceElement = DOM.getElement(identificationInfo, nodeOrder[i]);
                if (beforeResourceElement) break;
            }
            
            // get value from free text field
            var bkgAccessConstraintFreeRow = SQL.first("SELECT * FROM additional_field_data WHERE parent_field_id=? AND field_key=?", [containerId, 'bkg_accessConstraints_freeText']);
            if (hasValue(bkgAccessConstraintFreeRow)) {
                valueFree = bkgAccessConstraintFreeRow.get("data");
                if (log.isDebugEnabled()) {
                    log.debug("BKG access constraint free text field contains value: " + valueFree);
                }
            }
            
            // add select value and free text to ISO depending on selection 
            var legalConstraint = beforeResourceElement.addElementAsSibling("gmd:resourceConstraints/gmd:MD_LegalConstraints");
            addAccessConstraints(legalConstraint, value, valueFree);
        }
    }
}

function addAccessConstraints(legalConstraint, codelistEntryId, freeText) {
    if (codelistEntryId === null || codelistEntryId === undefined| codelistEntryId === "") {
        addConstraintElements([], null, valueFree);
        return;
    }
    
    switch (codelistEntryId) {
    case "1":
        addConstraintElements([], TRANSF.getIGCSyslistEntryName(10002, value), valueFree);
        break;
    case "2":
    case "3":
    case "4":
        addConstraintElements(["copyright"], TRANSF.getIGCSyslistEntryName(10002, value), valueFree);
        break;
    case "5":
        addConstraintElements(["copyright"], null, valueFree);
        break;
    case "6":
        addConstraintElements(["license"], null, valueFree);
        break;
    case "7":
        addConstraintElements(["copyright","license"], null, valueFree);
        break;
    case "8":
        addConstraintElements(["intellectualPropertyRights"], null, valueFree);
        break;
    case "9":
        addConstraintElements(["restricted"], null, valueFree);
        break;
    default:
        log.warn("Codelist entry not supported for list 10001: " + codelistEntryId);
    }
}

function addConstraintElements(restrictionCodeValues, valueCodelist, valueFree) {
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
    
    if (valueCodelist) {
        legalConstraint
            .addElement("gmd:otherConstraints/gco:CharacterString")
            .addText(valueCodelist);
    }
    
    if (valueFree) {
        legalConstraint
            .addElement("gmd:otherConstraints/gco:CharacterString")
            .addText(valueFree);
    }
}
