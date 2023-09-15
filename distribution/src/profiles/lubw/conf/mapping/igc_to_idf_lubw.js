/*
 * **************************************************-
 * InGrid-iPlug DSC
 * ==================================================
 * Copyright (C) 2014 - 2023 wemove digital solutions GmbH
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
var DatabaseSourceRecord = Java.type("de.ingrid.iplug.dsc.om.DatabaseSourceRecord");

if (log.isDebugEnabled()) {
    log.debug("LUBW: Mapping source record to idf document: " + sourceRecord.toString());
}

if (!(sourceRecord instanceof DatabaseSourceRecord)) {
    throw new IllegalArgumentException("Record is no DatabaseRecord!");
}

//---------- <idf:idfMdMetadata> ----------
var objId = sourceRecord.get("id");

/*
 * Export environmentDescription
 */
var environmentDescription = getAdditionalFieldFromObject(objId, null, 'environmentDescription', 'data');
if (environmentDescription) {
    var mdDataIdentification = DOM.getElement(idfDoc, "//idf:idfMdMetadata/gmd:identificationInfo/gmd:MD_DataIdentification");
    // if MD_DataIdentification doesn't exist, create it and add environmentDescription
    // WARN: this should never be the case (due to ISO restrictions) and is only added for completeness
    if (!mdDataIdentification) {
        var dataMetadata = DOM.getElement(idfDoc, "//idf:idfMdMetadata/gmd:identificationInfo");
        mdDataIdentification = dataMetadata.addElement("gmd:MD_DataIdentification");
        mdDataIdentification.addElement("gmd:environmentDescription/gco:CharacterString").addText(environmentDescription);
    }
    // if MD_DataIdentification _does_ exist, add environmentDescription at the correct palce:
    // directly before extent if it exists
    // otherwise directly before supplementalInformation if it exists
    // otherwise at the end
    else {
        var dataIdentificationChildNodes = XPATH.getNodeList(idfDoc, "//idf:idfMdMetadata/gmd:identificationInfo/gmd:MD_DataIdentification/*");
        var previousSibling;
        for (var i = 0; i < dataIdentificationChildNodes.getLength(); i++) {
            var currentSibling = dataIdentificationChildNodes.item(i);
            if (currentSibling.getTagName() == "gmd:extent" || currentSibling.getTagName() == "gmd:supplementalInformation") {
                break;
            }
            previousSibling = currentSibling;
        }
        if (previousSibling) {
            var previousElem = DOM.getElement(mdDataIdentification, previousSibling.getTagName() + "[last()]");
            previousElem.addElementAsSibling("gmd:environmentDescription/gco:CharacterString").addText(environmentDescription);
        }
    }
}

/**
 * Export OAC
 */
var oac = getAdditionalFieldFromObject(objId, null, 'oac', 'data');
if (oac) {
    var previousSibling = DOM.getElement(idfDoc, "//idf:idfMdMetadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:descriptiveKeywords[last()]");
    if (!previousSibling) {
        var mdDataIdentification = DOM.getElement(idfDoc, "//idf:idfMdMetadata/gmd:identificationInfo/gmd:MD_DataIdentification");
        var path = ["gmd:citation", "gmd:abstract", "gmd:purpose", "gmd:credit", "gmd:status", "gmd:pointOfContact", "gmd:resourceMaintenance", "gmd:graphicOverview", "gmd:resourceFormat", "gmd:descriptiveKeywords"];
        // find last present node from paths
        for (i = 0; i < path.length; i++) {
            // get the last occurrence of this path if any
            var currentSibling = DOM.getElement(mdDataIdentification, path[i] + "[last()]");
            if (currentSibling) {
                previousSibling = currentSibling;
            }
        }
    }
    if (previousSibling) {
        previousSibling.addElementAsSibling("gmd:descriptiveKeywords/gmd:MD_Keywords/gmd:keyword/gco:CharacterString").addText("oac:" + oac);
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
