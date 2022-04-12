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

    if (hasValue(bwastrId)) {
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

var citationAuthors = SQL.all("SELECT t02_address.* FROM t012_obj_adr, t02_address WHERE t012_obj_adr.adr_uuid=t02_address.adr_uuid AND t02_address.work_state=? AND t012_obj_adr.obj_id=? AND t012_obj_adr.type<>? AND (t012_obj_adr.special_ref IS NULL OR t012_obj_adr.special_ref=?) AND t012_obj_adr.type=? ORDER BY line", ['V', +objId, 12, 505, 11]);
var citationAuthorsContent = "";
for (var i=0; i< citationAuthors.size(); i++) {
    var authorLastname = citationAuthors.get(i).get("lastname");
    var authorFirstname = citationAuthors.get(i).get("firstname");
    if(hasValue(citationAuthorsContent)) {
        citationAuthorsContent += ", ";
    }
    if (hasValue(authorLastname)) {
        citationAuthorsContent += authorLastname + ", ";
    }
    if (hasValue(authorFirstname)) {
        citationAuthorsContent += authorFirstname.charAt(0) + ".";
    }
    if(!hasValue(citationAuthorsContent)){
        var authorInstitution = citationAuthors.get(i).get("institution");
        var authorInstitutionParent = "";
        var addrId = citationAuthors.get(i).get("id");
        var parentAdressRow = SQL.first("SELECT t02_address.* FROM t02_address, address_node WHERE address_node.addr_id_published=? AND address_node.fk_addr_uuid=t02_address.adr_uuid AND t02_address.work_state=?", [+addrId, "V"]);
        if(hasValue(parentAdressRow)) {
            while (hasValue(parentAdressRow)) {
                if (log.isDebugEnabled()) {
                    log.debug("Add address with uuid '"+parentAdressRow.get("adr_uuid")+"' to address path:" + parentAdressRow);
                }
                if(hasValue(authorInstitutionParent)) {
                    authorInstitutionParent += ", ";
                }
                authorInstitutionParent += "<b>" + parentAdressRow.get("institution") + "</b> ";
                addrId = parentAdressRow.get("id");
                parentAdressRow = SQL.first("SELECT t02_address.* FROM t02_address, address_node WHERE address_node.addr_id_published=? AND address_node.fk_addr_uuid=t02_address.adr_uuid AND t02_address.work_state=?", [+addrId, "V"]);
            }
            if(hasValue(authorInstitutionParent)) {
                citationAuthorsContent += authorInstitutionParent;
            }
        } else {
            if (hasValue(authorInstitution)) {
                citationAuthorsContent += authorInstitution;
            }
        }
    }
}


if(hasValue(citationAuthorsContent)) {
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
        var publisherInstitution = citationPublishers.get(i).get("institution");
        if(hasValue(citationPublishersContent)) {
            citationPublishersContent += " ,";
        }
        if (hasValue(publisherInstitution)) {
            citationPublishersContent += publisherInstitution;
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
    if (hasValue(additional_html_citation_quote)) {
        additional_html_citation_quote = additional_html_citation_quote.trim();
        additional_html_citation_quote += ": ";
    }
    if(hasValue(citationTitleContent)) {
        additional_html_citation_quote += "<i>" + citationTitleContent + "</i>";
        if(hasValue(doi) && hasValue(doi.type)) {
            additional_html_citation_quote += " [" + doi.type + "]";
        }
        additional_html_citation_quote += ". ";
    }
    if(hasValue(citationPublishersContent)) {
        additional_html_citation_quote += citationPublishersContent + ". ";
    }
    if(hasValue(doi) && hasValue(doi.id)) {
        additional_html_citation_quote += "<a href=\"https://doi.org/" + doi.id + "\" target=\"_blank\">https://doi.org/" + doi.id + "</a>";
    }
    IDX.add("additional_html_citation_quote", additional_html_citation_quote);
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
