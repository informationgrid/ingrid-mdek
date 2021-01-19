/*
 * **************************************************-
 * InGrid-iPlug DSC
 * ==================================================
 * Copyright (C) 2014 - 2021 wemove digital solutions GmbH
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
}

importPackage(Packages.org.apache.lucene.document);
importPackage(Packages.de.ingrid.iplug.dsc.om);
importPackage(Packages.de.ingrid.geo.utils.transformation);

var BwstrLocUtil = Java.type("de.ingrid.iplug.dsc.utils.BwstrLocUtil");
var BWST_LOC_TOOL = new BwstrLocUtil();

if (log.isDebugEnabled()) {
	log.debug("Mapping source record to lucene document: " + sourceRecord.toString());
}

if (!(sourceRecord instanceof DatabaseSourceRecord)) {
    throw new IllegalArgumentException("Record is no DatabaseRecord!");
}

// convert id to number to be used in PreparedStatement as Integer to avoid postgres error !
var objId = +sourceRecord.get("id");

// ---- BAW specific fields ----
var addnFieldRows = SQL.all("SELECT * FROM additional_field_data WHERE obj_id = ?", [objId]);
for(var i=0; i<addnFieldRows.size(); i++) {
    var row = addnFieldRows.get(i);
    var fieldKey = row.get("field_key");
    var data = row.get("data");
    if (fieldKey && data && fieldKey == "bawHierarchyLevelName") {
        // ---- BAW Hierarchy level name (Auftrag, Variante, etc.) ------
        IDX.add("bawhierarchylevelname", data);
    } else if (fieldKey && data && fieldKey == "simSpatialDimension") {
        // ---- BAW model spatial dimensionality ----
        IDX.add("simspatialdimension", data);
    } else if (fieldKey && data && fieldKey == "simProcess") {
        IDX.add("simprocess", data);
    }
}

var bwastrId = getAdditionalFieldValue(objId, "bwastr_name");
var bwastrKmStart = getAdditionalFieldValue(objId, "bwastr_km_start");
var bwastrKmEnd = getAdditionalFieldValue(objId, "bwastr_km_end");
log.debug("BWaStr. ID is: " + bwastrId + ", km start is: " + bwastrKmStart + ", km end is: " + bwastrKmEnd);

if (hasValue(bwastrId)) {
    if (bwastrId === "9600") {
        IDX.add("bwstr-bwastr_name", "Binnenwasserstraßen");
    } else if (bwastrId === "9700") {
        IDX.add("bwstr-bwastr_name", "Seewasserstraßen");
    } else if (bwastrId === "9800") {
        IDX.add("bwstr-bwastr_name", "Bundeswasserstraßen");
    } else if (bwastrId === "9900") {
        IDX.add("bwstr-bwastr_name", "Sonstige Gewässer");
    } else if (hasValue(bwastrKmStart)) {
        var bwstrIdAndKm = bwastrId + "-" + bwastrKmStart + "-" + bwastrKmEnd;
        for (var i=bwastrId.length; i<4; i++) {
            bwstrIdAndKm = "0" + bwstrIdAndKm;
        }
        var parts = BWST_LOC_TOOL.parseCenterSectionFromBwstrIdAndKm(bwstrIdAndKm);
        var parsedResponse = BWST_LOC_TOOL.parse(BWST_LOC_TOOL.getResponse(parts[0], parts[1], parts[2]));
        var center = BWST_LOC_TOOL.getCenter(parsedResponse);
        log.debug("Parsed centre from BWaStr. Locator tool is: " + center[0] + ", " + center[1]);
        if (!isNaN(center[0])) {
            IDX.addNumeric("bwstr-center-lon", center[0]);
        }
        if (!isNaN(center[1])) {
            IDX.addNumeric("bwstr-center-lat", center[1]);
        }
        var locNames = BWST_LOC_TOOL.getLocationNames(parsedResponse);
        if (locNames && locNames.length==2) {
            IDX.add("bwstr-bwastr_name", locNames[0]);
            IDX.add("bwstr-strecken_name", locNames[1]);
        }
    }
    // Add the BWaStr-ID itself to the index.
    // Use workaround to store it as string to preserve leading zeros and
    // have a predictable behaviour in elasticsearch.
    var bwastrIdPrefix = "id_";
    for (var i=bwastrId.length; i<4; i++) {
        bwastrIdPrefix += "0";
    }
    IDX.add("bwstr-bwastr-id", bwastrIdPrefix + bwastrId);
}

function getAdditionalFieldValue(objId, fieldKey) {
    var query = "SELECT fd1.data FROM additional_field_data fd0 " +
        "JOIN additional_field_data fd1 ON fd1.parent_field_id = fd0.id " +
        "WHERE fd1.field_key = ? AND fd0.obj_id = ?";
    var row = SQL.first(query, [fieldKey, objId]);
    if (hasValue(row)) {
        return row.get("data");
    }
}

