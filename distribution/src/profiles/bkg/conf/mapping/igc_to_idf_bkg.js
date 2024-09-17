/*
 * **************************************************-
 * InGrid-iPlug DSC
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

var DatabaseSourceRecord = Java.type("de.ingrid.iplug.dsc.om.DatabaseSourceRecord");

if (log.isDebugEnabled()) {
    log.debug("BKG: Mapping source record to idf document: " + sourceRecord.toString());
}

if (!(sourceRecord instanceof DatabaseSourceRecord)) {
    throw new IllegalArgumentException("Record is no DatabaseRecord!");
}

//---------- <idf:idfMdMetadata> ----------
var body = DOM.getElement(idfDoc, "/idf:html/idf:body/idf:idfMdMetadata");

var objId = sourceRecord.get("id");
var objRows = SQL.all("SELECT * FROM t01_object WHERE id=?", [+objId]);
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
    handleBKGUseLimitation();
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
            if ((bkgAccessConstraintSelectListItem && bkgAccessConstraintSelectListItem !== "") || bkgAccessConstraintFreeText !== "") {
                var legalConstraint = getFirstNodeInIdentificationBefore("gmd:accessConstraints").addElementAsSibling("gmd:resourceConstraints/gmd:MD_LegalConstraints");
                addAccessConstraints(legalConstraint, bkgAccessConstraintSelectListItem, bkgAccessConstraintFreeText);
            }
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
            log.debug("BKG use constraint select field contains value: " + bkgUseConstraintSelectListItem);
        }

        // get value from free text field
        var bkgUseConstraintFreeText = getAdditionalFieldFromObject(null, bkgUseConstraintId, 'bkg_useConstraints_freeText', 'data');
        if (bkgUseConstraintFreeText) {
            if (log.isDebugEnabled()) {
                log.debug("BKG use constraint free text field contains value: " + bkgUseConstraintFreeText);
            }
        }

        // get value from source note field
        var bkgSourceNoteText = getAdditionalFieldFromObject(null, bkgUseConstraintId, 'bkg_useConstraints_sourceNote', 'data');
        if (bkgSourceNoteText) {
            if (log.isDebugEnabled()) {
                log.debug("BKG use constraint free text field contains value: " + bkgSourceNoteText);
            }
        }

        // add select value and free text to ISO depending on selection
        // if there is any value
        if ((bkgUseConstraintSelectListItem && bkgUseConstraintSelectListItem !== "") || bkgUseConstraintFreeText !== "") {
            var legalConstraint = getFirstNodeInIdentificationBefore("gmd:useConstraints").addElementAsSibling("gmd:resourceConstraints/gmd:MD_LegalConstraints");
            addUseConstraints(legalConstraint, bkgUseConstraintSelectListItem, bkgUseConstraintFreeText,
                bkgSourceNoteText ? "Quellenvermerk: " + bkgSourceNoteText : null);
        }

        // add json from codelist-data field for open data datasets
        if (isOpenData()) {
            var licenseJSON = TRANSF.getISOCodeListEntryData(10003, TRANSF.getIGCSyslistEntryName(10003, +bkgUseConstraintSelectListItem));
            if (hasValue(licenseJSON)) {
                if (bkgSourceNoteText) {
                    var licenseJSONParsed = JSON.parse(licenseJSON);
                    licenseJSONParsed.quelle = bkgSourceNoteText;
                    licenseJSON = JSON.stringify(licenseJSONParsed);
                }
                legalConstraint.addElement("gmd:otherConstraints/gco:CharacterString").addText(licenseJSON);
            }
        }
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
        field = SQL.first("SELECT * FROM additional_field_data WHERE obj_id=? AND field_key=?", [+objId, fieldId]);
    } else {
        field = SQL.first("SELECT * FROM additional_field_data WHERE parent_field_id=? AND field_key=?", [+parentId, fieldId]);
    }
    if (hasValue(field)) {
        return field.get(property);
    } else {
        return null;
    }
}


function getIdentificationInfo() {
    var identificationInfo = null;
    if (objClass == "3") {
        identificationInfo = DOM.getElement(body, "gmd:identificationInfo/srv:SV_ServiceIdentification");
    } else {
        identificationInfo = DOM.getElement(body, "gmd:identificationInfo/gmd:MD_DataIdentification");
    }
    return identificationInfo;
}

function getFirstNodeInIdentificationBefore(subNode) {
    var nodeOrder = ["gmd:resourceConstraints", "gmd:resourceSpecificUsage", "gmd:descriptiveKeywords", "gmd:resourceFormat", "gmd:graphicOverview", "gmd:resourceMaintenance",
        "gmd:pointOfContact", "gmd:status", "gmd:credit", "gmd:purpose", "gmd:abstract"];
    var identificationInfo = getIdentificationInfo();

    // get first element before "resourceConstraints", so that we can insert in front of all other entries
    var beforeResourceElement;
    for (var i=0; i<nodeOrder.length; i++) {
        // try to get last(!) element
        beforeResourceElement = DOM.getElement(identificationInfo, nodeOrder[i] + "[last()]");
        if (beforeResourceElement) {
            if (i === 0 && subNode) {
                var subNodeElement = DOM.getElement(identificationInfo, nodeOrder[i] + "//" + subNode);
                if (subNodeElement) {
                    beforeResourceElement = subNodeElement.getParent(2);
                }
            }
            break;
        }
    }

    return beforeResourceElement;
}

function addAccessConstraints(legalConstraint, codelistEntryId, valueFree) {
    if (codelistEntryId === null || codelistEntryId === undefined || codelistEntryId === "") {
        addAccessConstraintElements(legalConstraint, [], [valueFree]);
        return;
    }

    // codelist 10001/10002
    switch (codelistEntryId) {
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
            addAccessConstraintElements(legalConstraint, [], [TRANSF.getIGCSyslistEntryName(10002, +codelistEntryId), valueFree]);
    }
}

function addUseConstraints(legalConstraint, codelistEntryId, valueFree, sourceNote) {
    log.debug("BKG: Use Constraint codelist: " + codelistEntryId);
    if (codelistEntryId === null || codelistEntryId === undefined | codelistEntryId === "") {
        addUseConstraintElements(legalConstraint, [], [valueFree, sourceNote]);
        return;
    }

    // codelist 10003/10004
    switch (codelistEntryId) {
    case "10":
        addUseConstraintElements(legalConstraint, ["copyright"], [valueFree, sourceNote]);
        break;
    case "11":
        addUseConstraintElements(legalConstraint, [], [valueFree, sourceNote]);
        break;
    case "12":
        addUseConstraintElements(legalConstraint, ["copyright"], [valueFree, sourceNote]);
        break;
    case "13":
        addUseConstraintElements(legalConstraint, ["intellectualPropertyRights"], [valueFree, sourceNote]);
        break;
    case "14":
        addUseConstraintElements(legalConstraint, ["restricted"], [valueFree, sourceNote]);
        break;
    default:
        addUseConstraintElements(legalConstraint, [], [TRANSF.getIGCSyslistEntryName(10004, +codelistEntryId), valueFree, sourceNote]);
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

    if (hasValue(otherConstraints)) {
        for (var j=0; j<otherConstraints.length; j++) {
            if (otherConstraints[j]) {
                if (otherConstraints[j] === "Es gelten keine Zugriffsbeschränkungen" || otherConstraints[j] === "no limitations to public access") {
                    legalConstraint
                        .addElement("gmd:otherConstraints/gmx:Anchor")
                        .addAttribute("xlink:href", "http://inspire.ec.europa.eu/metadata-codelist/LimitationsOnPublicAccess/noLimitations")
                        .addText(otherConstraints[j]);

                } else {
                    legalConstraint
                        .addElement("gmd:otherConstraints/gco:CharacterString")
                        .addText(otherConstraints[j]);
                }
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


    if (hasValue(otherConstraints)) {
        legalConstraint.addElement("gmd:useConstraints/gmd:MD_RestrictionCode")
            .addAttribute("codeListValue", "otherRestrictions")
            .addAttribute("codeList", globalCodeListAttrURL + "#MD_RestrictionCode")
            .addText("otherRestrictions");
        for (var j=0; j<otherConstraints.length; j++) {
            if (otherConstraints[j]) {
                if (isInspireRelevant() && (otherConstraints[j] === "Es gelten keine Bedingungen" || otherConstraints[j] === "No conditions to access and use")) {
                    legalConstraint
                        .addElement("gmd:otherConstraints/gmx:Anchor")
                        .addAttribute("xlink:href", "http://inspire.ec.europa.eu/metadata-codelist/ConditionsApplyingToAccessAndUse/noConditionsApply")
                        .addText(otherConstraints[j]);
                } else {
                    IDF_UTIL.addLocalizedCharacterstring(legalConstraint.addElement("gmd:otherConstraints"), otherConstraints[j]);
                }

            }
        }
    }
}

function isOpenData() {
    var value = objRow.get("is_open_data");
    return hasValue(value) && value == 'Y';
}

function isInspireRelevant() {
    var value = objRow.get("is_inspire_relevant");
    return hasValue(value) && value == 'Y';
}

function handleBKGUseLimitation() {
    // remove "Nutzungseinschränkungen" from useLimitation
    var useLimitationCharNodes = XPATH.getNodeList(body.getElement(), "//gmd:resourceConstraints/gmd:MD_LegalConstraints/gmd:useLimitation/gco:CharacterString");

    if (hasValue(useLimitationCharNodes)) {
        for (var i=0; i < useLimitationCharNodes.getLength(); i++) {
            var myCharNode = useLimitationCharNodes.item(i);
            var myText = XPATH.getString(myCharNode, ".");
            var myNewText = removeConstraintPraefixBkg(myText);
            myCharNode.setTextContent(myNewText);
        }
    }
}

function removeConstraintPraefixBkg(val) {
 	if (hasValue(val)) {
//    	log.warn("MM IN constraint : " + val);

    	val = val.trim();

    	// remove GDI-DE prefix
    	val = val.replace("Nutzungseinschränkungen: ", "");
    	// keep "Nutzungsbedingungen", this one marks fake useLimitations !
//    	val = val.replace("Nutzungsbedingungen: ", "");

//    	log.warn("MM OUT constraint : " + val);
	}
	return val;
}
