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

        // Also set the data_category and simulation_data_type if applicable
        if (data == "Luftbilder") {
            IDX.add("data_category", "Luftbilder");
        } else if (data == "Messdaten") {
            IDX.add("data_category", "Messdaten");
        } else if (data == "Postprocessing") {
            IDX.add("data_category", "Simulationsdaten");
            IDX.add("simulation_data_type", "Postprocessing");
        } else if (data == "Preprocessing") {
            IDX.add("data_category", "Simulationsdaten");
            IDX.add("simulation_data_type", "Preprocessing");
        } else if (data == "Variante") {
            IDX.add("data_category", "Simulationsdaten");
            IDX.add("simulation_data_type", "Variante");
        } else if (data == "Szenario") {
            IDX.add("data_category", "Simulationsdaten");
            IDX.add("simulation_data_type", "Szenario");
        } else if (data == "Simulationsmodell") {
            IDX.add("data_category", "Simulationsdaten");
            IDX.add("simulation_data_type", "Simulationsmodell");
        } else if (data == "Simulationslauf") {
            IDX.add("data_category", "Simulationsdaten");
            IDX.add("simulation_data_type", "Simulationslauf");
        } else if (data == "Simulationsdatei") {
            IDX.add("data_category", "Simulationsdaten");
            IDX.add("simulation_data_type", "Simulationsdatei");
        } else if (data == "Visualisierung") {
            IDX.add("data_category", "Visualisierung");
        } else if (data == "Sonstiges") {
            IDX.add("data_category", "Sonstiges");
        }
    } else if (fieldKey && data && fieldKey == "simSpatialDimension") {
        // ---- BAW model spatial dimensionality ----
        IDX.add("simspatialdimension", data);
    } else if (fieldKey && data && fieldKey == "simProcess") {
        IDX.add("simprocess", data);
        IDX.add("method", data);
    } else if (fieldKey && fieldKey == "bawKeywordCatalogueTable") {
        indexChildAdditionalFieldAsKeywords(row, "bawKeywordCatalogueEntry", 3950005);
    } else if (fieldKey && fieldKey == "simModelTypeTable") {
        indexChildAdditionalFieldAsKeywords(row, "simModelType", 3950003);
    } else if (fieldKey && fieldKey == "simParamTable") {
        indexPhysicalParameters(row);
    } else if (fieldKey && fieldKey == "measuringMethod") {
        indexMeasurementMethod(row);
    }
}

indexAdditionalFieldValueBWastr(objId);
indexAdditionalFieldValueBWastrSpatialRefFree(objId);
indexAuftragsnummer(objId);
indexAuftragstitel(objId);

indexBawAbteilung(objId);

function indexAuftragsnummer(objId) {
    indexFirstAdditionalFieldValue(objId, "bawAuftragsnummer", "bawauftragsnummer");
}

function indexAuftragstitel(objId) {
    // Default
    indexFirstAdditionalFieldValue(objId, "bawAuftragstitel", "bawauftragstitel");

    // Object class 4 (Project) saves the object title in a different field
    // Index the alternate title (if set) or the object title
    var row = SQL.first("SELECT obj_name, dataset_alternate_name FROM t01_object WHERE id = ? AND obj_class = 4", [objId]);
    if (hasValue(row)) {
        var altTitle = row.get("dataset_alternate_name");
        if (hasValue(altTitle)) {
            IDX.add("bawauftragstitel", altTitle);
        } else {
            var title = row.get("obj_name");
            if (hasValue(title)) {
                IDX.add("bawauftragstitel", title);
            }
        }
    }
}

function indexChildAdditionalFieldAsKeywords(parentRow, childKey, syslistId) {
    var parentFieldId = +parentRow.get("id");

    var rows = SQL.all("SELECT * FROM additional_field_data WHERE parent_field_id = ? AND field_key = ?", [parentFieldId, childKey]);
    for (var i=0; i<rows.size(); i++) {
        var data = rows.get(i).get("data");
        if (hasValue(data)) {
            var kw = hasValue(syslistId) ? TRANSF.getIGCSyslistEntryName(syslistId, +data, "de"): data;
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

function indexMeasurementMethod(parentRow) {
    var parentFieldId = +parentRow.get("id");

    var rows = SQL.all("SELECT * FROM additional_field_data WHERE parent_field_id = ? AND field_key = ?", [parentFieldId, "measuringMethod"]);
    for (var i=0; i<rows.size(); i++) {
        var data = rows.get(i).get("data");
        if (hasValue(data)) {
            data = data.trim();
            IDX.add("method", data);
            IDX.add("measuringMethod", data);
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
    var syslistId = 3950010;

    if (hasValue(bwastrId) && !isNaN(bwastrId)) {
        var bwastrName = "";
        var bwastrStreckenName = "";
        if (log.isDebugEnabled()) {
            log.debug("BWaStr. ID is: " + bwastrId + ", km start is: " + bwastrKmStart + ", km end is: " + bwastrKmEnd);
        }

        var entryName = TRANSF.getIGCSyslistEntryName(syslistId, +bwastrId, "de");
        if (log.isDebugEnabled()) {
            log.debug("Entry name for id " + bwastrId + " in codelist 3950010 is: " + entryName);
        }

        bwastrId = "" + bwastrId; // convert to string
        while(bwastrId.length < 4) {
            bwastrId = "0" + bwastrId;
        }
        IDX.add("bwstr-bwastr-id", bwastrId);

        if (hasValue(entryName)) {
            // entryName has format: BWaStr-name, stretch-name - [id]
            entryName = entryName.replace(" - [" + bwastrId + "]", "");
            var arr = entryName.split(",");
            if (hasValue(arr[0])) {
                IDX.add("bwstr-bwastr_name", arr[0].trim());
            }
            if (hasValue(arr[1])) {
                IDX.add("bwstr-strecken_name", arr[1].trim());
            }
        }

        if (hasValue(bwastrKmStart)) {
            IDX.add("bwstr-strecken_km_von", bwastrKmStart);
        } else {
            IDX.add("bwstr-strecken_km_von", "");
        }
        if (hasValue(bwastrKmEnd)) {
            IDX.add("bwstr-strecken_km_bis", bwastrKmEnd);
        } else {
            IDX.add("bwstr-strecken_km_bis", "");
        }
    }
}

function indexBawAbteilung(objId) {
    var query = "SELECT adr_uuid FROM t012_obj_adr WHERE obj_id = ? AND type = 7"; // type = 7 for Ansprechperson
    var rows = SQL.all(query, [objId]);
    for(var i=0; i<rows.size(); i++) {
        var row = rows.get(i);
        var addrUuid = rows.get(i).get("adr_uuid");
        if (addrUuid === "9341dbb5-4e09-3fca-b343-2990fc935761") {
            IDX.add("baw_abteilung_short", "b");
            IDX.add("baw_abteilung_long", "Bautechnik");
        } else if (addrUuid === "88b9d568-288e-391f-9649-af31fc0fc128") {
            IDX.add("baw_abteilung_short", "g");
            IDX.add("baw_abteilung_long", "Geotechnik");
        } else if (addrUuid === "30d30a3b-27fe-3470-aec2-63183b8052ce") {
            IDX.add("baw_abteilung_short", "w");
            IDX.add("baw_abteilung_long", "Wasserbau im Binnenbereich");
        } else if (addrUuid === "eaaf4d0d-44cd-356e-a3e3-520191945ca5") {
            IDX.add("baw_abteilung_short", "k");
            IDX.add("baw_abteilung_long", "Wasserbau im Küstenbereich");
        } else if (addrUuid === "d28ee28e-83d3-3996-aaf7-d053a05ec7ff") {
            IDX.add("baw_abteilung_short", "z");
            IDX.add("baw_abteilung_long", "Zentraler Service");
        }
    }
}

