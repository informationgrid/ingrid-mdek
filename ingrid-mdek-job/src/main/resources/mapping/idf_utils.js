/*
 * **************************************************-
 * InGrid-iPlug DSC
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

var mdMetadataChildrenReverseOrder = [
    "gmd:featureAttribute",
    "gmd:featureType",
    "gmd:propertyType",
    "gmd:describes",
    "gmd:series",
    "gmd:metadataMaintenance",
    "gmd:applicationSchemaInfo",
    "gmd:metadataConstraints",
    "gmd:portrayalCatalogueInfo",
    "gmd:dataQualityInfo",
    "gmd:distributionInfo",
    "gmd:contentInfo",
    "gmd:identificationInfo",
    "gmd:metadataExtensionInfo",
    "gmd:referenceSystemInfo",
    "gmd:spatialRepresentationInfo",
    "gmd:locale",
    "gmd:dataSetURI",
    "gmd:metadataStandardVersion",
    "gmd:metadataStandardName",
    "gmd:dateStamp",
    "gmd:contact",
    "gmd:hierarchyLevelName",
    "gmd:hierarchyLevel",
    "gmd:parentIdentifier",
    "gmd:characterSet",
    "gmd:language",
    "gmd:fileIdentifier"
];

var identificationInfoChildrenReverseOrder = [
    "gmd:aggregationInfo",
    "gmd:resourceConstraints",
    "gmd:resourceSpecificUsage",
    "gmd:descriptiveKeywords",
    "gmd:resourceFormat",
    "gmd:graphicOverview",
    "gmd:resourceMaintenance",
    "gmd:pointOfContact",
    "gmd:status",
    "gmd:credit",
    "gmd:purpose",
    "gmd:abstract",
    "gmd:citation"
];

// Return gco:Date OR gco:DateTime element dependent from passed date format.
function getDateOrDateTime(dateValue) {
    var gcoElement;
    if (dateValue == null) {
        return "null"
    } else if (dateValue.indexOf("T") > -1) {
        gcoElement = DOM.createElement("gco:DateTime");
    } else {
        gcoElement = DOM.createElement("gco:Date");
    }
    gcoElement.addText(dateValue);
    return gcoElement;
}

function getHierarchLevel(objClass) {
    var hierarchyLevel = null;
    if (objClass == "0") {
        hierarchyLevel = "nonGeographicDataset";
    } else if (objClass == "1") {
    	// select via id, convert id to number to be used in PreparedStatement as Integer to avoid postgres error !
        var rows = SQL.all("SELECT hierarchy_level FROM t011_obj_geo WHERE obj_id=?", [+objId]);
        // Should be only one row !
        for (j=0; j<rows.size(); j++) {
            hierarchyLevel = TRANSF.getISOCodeListEntryFromIGCSyslistEntry(525, rows.get(j).get("hierarchy_level"));
        }
    } else if (objClass == "2") {
        hierarchyLevel = "nonGeographicDataset";
    } else if (objClass == "3") {
        hierarchyLevel = "service";
    } else if (objClass == "4") {
        hierarchyLevel = "nonGeographicDataset";
    } else if (objClass == "5") {
        hierarchyLevel = "nonGeographicDataset";
    } else if (objClass == "6") {
        hierarchyLevel = "application";
    } else {
        log.error("Unsupported UDK class '" + objClass
                + "'. Only class 0 to 6 are supported by the CSW interface.");
    }
    
    return hierarchyLevel;
}

function searchNextRootSiblingTag(parentNode, tagName, reversedElementChildren) {
    var list = reversedElementChildren ? reversedElementChildren : mdMetadataChildrenReverseOrder;
    var index = list.indexOf(tagName);
    var nextSibling = null;
    for (var i = index; i < list.length && !nextSibling; i++) {
        nextSibling = DOM.getElement(parentNode, list[i] + "[last()]");
    }
    return nextSibling;
}

function getAdditionalForTable(objId, tableId) {

    var result = {};

    var field = SQL.first("SELECT * FROM additional_field_data WHERE obj_id=? AND field_key=?", [objId, tableId]);

    if (!hasValue(field)) {return}

    var table = SQL.all("SELECT * FROM additional_field_data WHERE parent_field_id=?", [field.get("id")])

    for (var j=0; j<table.size(); j++) {
        var row = table.get(j);
        var rowNumber = row.get("sort");
        if (!result[rowNumber]) result[rowNumber] = {};
        result[rowNumber][row.get("field_key")] = {
            data: row.get("data"),
            listId: row.get("list_item_id")
        };
    }

    var resultArray = [];
    var rowNumbers = Object.keys(result);
    for (var i=0; i<rowNumbers.length; i++) {
        resultArray.push(result[rowNumbers[i]]);
    }

    return resultArray;

}
