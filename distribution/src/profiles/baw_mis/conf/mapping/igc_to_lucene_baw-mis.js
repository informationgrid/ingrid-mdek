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
    } else if (fieldKey && fieldKey == "bawKeywordCatalogueTable") {
        indexChildAdditionalFieldAsKeywords(row, "bawKeywordCatalogueEntry", 3950005);
    } else if (fieldKey && fieldKey == "simModelTypeTable") {
        indexChildAdditionalFieldAsKeywords(row, "simModelType", 3950003);
    } else if (fieldKey && fieldKey == "simParamTable") {
        indexPhysicalParameters(row);
    }
}

indexAdditionalFieldValueBWastr(objId);
indexAdditionalFieldValueBWastrSpatialRefFree(objId);
indexFirstAdditionalFieldValue(objId, "bawAuftragsnummer", "bawauftragsnummer");
indexFirstAdditionalFieldValue(objId, "bawAuftragstitel", "bawauftragstitel");

function indexChildAdditionalFieldAsKeywords(parentRow, childKey, syslistId) {
    var parentFieldId = +parentRow.get("id");

    var rows = SQL.all("SELECT * FROM additional_field_data WHERE parent_field_id = ? AND field_key = ?", [parentFieldId, childKey]);
    for (var i=0; i<rows.size(); i++) {
        var data = rows.get(i).get("data");
        if (hasValue(data)) {
            var kw = hasValue(syslistId) ? TRANSF.getIGCSyslistEntryName(syslistId, data, "de"): data;
            addSearchtermValue("F", kw, "");
        }
    }
}

function indexPhysicalParameters(parentRow) {
    var parentFieldId = +parentRow.get("id");

    var rows = SQL.all("SELECT * FROM additional_field_data WHERE parent_field_id = ? AND field_key = ?", [parentFieldId, "simParamName"]);
    for (var i=0; i<rows.size(); i++) {
        var data = rows.get(i).get("data");
        if (hasValue(data)) {
            data.split(";").forEach(function (paramName) {
                IDX.add("physicalparameter.name", paramName.trim());
            });
        }
    }
}

function indexFirstAdditionalFieldValue(objId, fieldKey, indexField) {
    var query = "SELECT obj.data FROM additional_field_data obj WHERE obj.obj_id=? AND obj.field_key=?";
    var row = SQL.first(query, [objId, fieldKey]);
    if(hasValue(row)) {
        IDX.add(indexField, row.get("data"));
    }
}

function indexAdditionalFieldValueBWastrSpatialRefFree(objId) {
    var query = "SELECT spatial_ref_value.* FROM spatial_reference, spatial_ref_value " +
        "WHERE spatial_ref_value.type = 'F' AND " +
        "spatial_reference.spatial_ref_id=spatial_ref_value.id AND spatial_reference.obj_id=?";
    var rows = SQL.all(query, [objId]);
    for(var i=0; i<rows.size(); i++) {
        var row = rows.get(i);
        var nameValue = row.get("name_value");
        if (hasValue(nameValue)) {
            var bwaStrValue = nameValue.replaceAll("[^-?0-9.0-9]+", "");
            bwaStrValue = bwaStrValue.replaceAll("-", " ");
            var bwaStrValues = bwaStrValue.split(" ");
            var bwastrId = "";
            var bwastrKmStart = "";
            var bwastrKmEnd = "";
            switch (bwaStrValues.length) {
                case 1:
                    bwastrId = bwaStrValues[0];
                    break;
                case 3:
                    bwastrId = bwaStrValues[0];
                    bwastrKmStart = bwaStrValues[1];
                    bwastrKmEnd = bwaStrValues[2];
                    break;
                default:
                    break;
            }
            addBWaStrData(bwastrId, bwastrKmStart, bwastrKmEnd);
        }
    }
}

function indexAdditionalFieldValueBWastr(objId) {
    var query = "SELECT DISTINCT fd1.sort FROM additional_field_data fd0 " +
        "JOIN additional_field_data fd1 ON fd1.parent_field_id = fd0.id " +
        "WHERE fd0.obj_id = ? ORDER BY fd1.sort";
    var rows = SQL.all(query, [objId]);
    for(var i=0; i<rows.size(); i++) {
        var row = rows.get(i);
        var sort = row.get("sort");
        var queryData = "SELECT fd1.field_key, fd1.data FROM additional_field_data fd0 " +
            "JOIN additional_field_data fd1 ON fd1.parent_field_id = fd0.id " +
            "WHERE fd1.sort = ? AND fd0.obj_id = ?";
        var rowsData = SQL.all(queryData, [+sort, objId]);
        var bwastrId = "";
        var bwastrKmStart = "";
        var bwastrKmEnd = "";

        for(var j=0; j<rowsData.size(); j++) {
            var rowData = rowsData.get(j);
            var fieldKey = rowData.get("field_key");
            var data = rowData.get("data");
            if(fieldKey === "bwastr_name") {
                bwastrId = data;
            } else if(fieldKey === "bwastr_km_start") {
                bwastrKmStart = data;
            } else if(fieldKey === "bwastr_km_end") {
                bwastrKmEnd = data;
            }
        }
        addBWaStrData(bwastrId, bwastrKmStart, bwastrKmEnd);
    }
}

function addBWaStrData(bwastrId, bwastrKmStart, bwastrKmEnd) {
    if (hasValue(bwastrId)) {
        var bwastrName = "";
        var bwastrStreckenName = "";
        log.debug("BWaStr. ID is: " + bwastrId + ", km start is: " + bwastrKmStart + ", km end is: " + bwastrKmEnd);
        if (bwastrId === "9600") {
            bwastrName = "Binnenwasserstraßen";
        } else if (bwastrId === "9700") {
            bwastrName = "Seewasserstraßen";
        } else if (bwastrId === "9800") {
            bwastrName = "Bundeswasserstraßen";
        } else if (bwastrId === "9900") {
            bwastrName = "Sonstige Gewässer";
        } else {
            var bwastrInfo = BWST_LOC_TOOL.doBWaStrInfoQuery(bwastrId);
            var name = bwastrInfo.get("bwastr_name");
            var strecke = bwastrInfo.get("strecken_name");

            if (hasValue(name)) {
                bwastrName = name;
            }

            if (hasValue(strecke)) {
                bwastrStreckenName = strecke;
            }
        }

        var bwastrIdPrefix = "";
        for (var k=bwastrId.length; k<4; k++) {
            bwastrIdPrefix += "0";
        }
        IDX.add("bwstr-bwastr-id", bwastrIdPrefix + bwastrId);
        IDX.add("bwstr-strecken_km_von", bwastrKmStart);
        IDX.add("bwstr-strecken_km_bis", bwastrKmEnd);
        IDX.add("bwstr-bwastr_name", bwastrName);
        IDX.add("bwstr-strecken_name", bwastrStreckenName);
    }
}

