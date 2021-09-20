/*
 * **************************************************-
 * InGrid-iPlug DSC
 * ==================================================
 * Copyright (C) 2014 - 2020 wemove digital solutions GmbH
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

var citationAuthors = SQL.all("SELECT t02_address.* FROM t012_obj_adr, t02_address WHERE t012_obj_adr.adr_uuid=t02_address.adr_uuid AND t02_address.work_state=? AND t012_obj_adr.obj_id=? AND t012_obj_adr.type<>? AND (t012_obj_adr.special_ref IS NULL OR t012_obj_adr.special_ref=?) AND t012_obj_adr.type=? ORDER BY line", ['V', +objId, 12, 505, 11]);
var citationAuthorsContent = "";
for (var i=0; i< citationAuthors.size(); i++) {
    var authorLastname = citationAuthors.get(i).get("lastname");
    var authorFirstname = citationAuthors.get(i).get("firstname");
    if(hasValue(citationAuthorsContent)) {
        citationAuthorsContent += ",";
    }
    if (hasValue(authorLastname)) {
        citationAuthorsContent += authorLastname + ",";
    }
    if (hasValue(authorFirstname)) {
        citationAuthorsContent += authorFirstname.charAt(0) + ".";
    }
}

var citationDates = SQL.all("SELECT * FROM t0113_dataset_reference WHERE obj_id=? AND type=?", [+objId, 2]);
var citationDateContent = "";
for (var i=0; i< citationDates.size(); i++) {
    var publicationDate = citationDates.get(i).get("reference_date");
    if(hasValue(citationDateContent)) {
        citationDateContent += ",";
    }
    if (hasValue(publicationDate)) {
        citationDateContent += publicationDate.substring(0, 4);
    }
}

var citationTitles = SQL.all("SELECT * FROM t01_object WHERE id=?", [+objId]);
var citationTitleContent = "";
for (var i=0; i< citationTitles.size(); i++) {
    var title = citationTitles.get(i).get("obj_name");
    if(hasValue(citationTitleContent)) {
        citationTitleContent += ",";
    }
    if (hasValue(title)) {
        citationTitleContent += title;
    }
}

var citationPublishers = SQL.all("SELECT t02_address.* FROM t012_obj_adr, t02_address WHERE t012_obj_adr.adr_uuid=t02_address.adr_uuid AND t02_address.work_state=? AND t012_obj_adr.obj_id=? AND t012_obj_adr.type<>? AND (t012_obj_adr.special_ref IS NULL OR t012_obj_adr.special_ref=?) AND t012_obj_adr.type=? ORDER BY line", ['V', +objId, 12, 505, 10]);
var citationPublishersContent = "";
for (var i=0; i< citationPublishers.size(); i++) {
    var pubilsherInstitution = citationPublishers.get(i).get("institution");
    if(hasValue(pubilsherInstitution)) {
        citationPublishersContent += ",";
    }
    if (hasValue(pubilsherInstitution)) {
        citationPublishersContent += pubilsherInstitution + ",";
    }
}


var doi = addDOIInfo(objId);
var additional_html_citation_quote = "";
if(hasValue(citationAuthorsContent)) {
    additional_html_citation_quote += "<b>" + citationAuthorsContent + "</b> ";
}
if(hasValue(citationDateContent)) {
    additional_html_citation_quote += "<b>(" + citationDateContent + ")</b> ";
}
if(hasValue(citationTitleContent)) {
    additional_html_citation_quote += "<i>" + citationTitleContent + "</i>";
    if(hasValue(doi) && hasValue(doi.type)) {
        additional_html_citation_quote += "[" + doi.type + "]";
    }
    additional_html_citation_quote += " ";
}
if(hasValue(citationPublishersContent)) {
    additional_html_citation_quote += citationPublishersContent + ". ";
}
if(hasValue(doi) && hasValue(doi.id)) {
    additional_html_citation_quote += "<a href=\"https://doi.org/" + doi.id + "\" target=\"_blank\">" + doi.id + "</a>";
}
IDX.add("additional_html_citation_quote", additional_html_citation_quote);


function getAdditionalFieldValue(objId, fieldKey) {
    var query = "SELECT fd1.data FROM additional_field_data fd0 " +
        "JOIN additional_field_data fd1 ON fd1.parent_field_id = fd0.id " +
        "WHERE fd1.field_key = ? AND fd0.obj_id = ?";
    var row = SQL.first(query, [fieldKey, objId]);
    if (hasValue(row)) {
        return row.get("data");
    }
}

function addDOIInfo(objId) {
    var doiIdData = SQL.first("SELECT * FROM additional_field_data fd WHERE fd.obj_id=? AND fd.field_key = 'doiId'", [objId]);
    var doiType = SQL.first("SELECT * FROM additional_field_data fd WHERE fd.obj_id=? AND fd.field_key = 'doiType'", [objId]);

    if (hasValue(doiIdData) || hasValue(doiType)) {
        var doiId = undefined;
        var type = undefined;

        if (hasValue(doiIdData)) {
            doiId = doiIdData.get("data");
        }

        if (hasValue(doiType)) {
            type = doiType.get("data");
        }

        return {
            id: doiId,
            type: type
        };
    }
}