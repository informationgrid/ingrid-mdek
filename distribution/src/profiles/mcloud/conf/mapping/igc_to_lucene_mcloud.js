/*
 * **************************************************-
 * InGrid-iPlug DSC
 * ==================================================
 * Copyright (C) 2014 - 2018 wemove digital solutions GmbH
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
importPackage(Packages.de.ingrid.iplug.dsc.index.mapper);

// constant to punish the rank of a service/data object, which has no coupled resource
var BOOST_NO_COUPLED_RESOURCE  = 0.9;
//constant to boost the rank of a service/data object, which has at least one coupled resource
var BOOST_HAS_COUPLED_RESOURCE = 1.0;

if (log.isDebugEnabled()) {
    log.debug("Mapping source record to lucene document: " + sourceRecord.toString());
}

if (!(sourceRecord instanceof DatabaseSourceRecord)) {
    throw new IllegalArgumentException("Record is no DatabaseRecord!");
}

// add default boost value
IDX.addDocumentBoost(1.0);

// add UVP specific mapping
// UVP Codelist
var behavioursValueRow = SQL.first("SELECT * FROM sys_generic_key WHERE key_name='BEHAVIOURS'");
var codelist = '';
var publishNegativeExaminations = false;
if (hasValue(behavioursValueRow)){
    var behaviours = behavioursValueRow.get("value_string");
    if(hasValue(behaviours)){
        var behavioursJson = JSON.parse(behaviours);
        for(var beh in behavioursJson){
            var behaviour = behavioursJson[beh];
            if(hasValue(behaviour)){
                var behaviourId = behaviour.id;
                if(hasValue(behaviourId)){
                    if (behaviourId.equals("uvpPhaseField")){
                        var behaviourParams = behaviour.params;
                        if (hasValue(behaviourParams)){
                            for(var behParam in behaviourParams){
                                var behaviourParam = behaviourParams[behParam];
                                if (hasValue(behaviourParam)){
                                    var behaviourParamId = behaviourParam.id;
                                    if (behaviourParamId.equals("categoryCodelist")){
                                        var behaviourParamValue = behaviourParam.value;
                                        if (hasValue(behaviourParamValue)){
                                            codelist = behaviourParamValue;
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    } else if (behaviourId.equals("uvpPublishNegativeExamination")) {
                        publishNegativeExaminations = behaviour.active;
                    }
                }
            }
        }
    }
}

// ---------- t01_object ----------
var objId = sourceRecord.get("id");
var objRows = SQL.all("SELECT * FROM t01_object WHERE id=?", [objId]);
for (var i=0; i<objRows.size(); i++) {
/*
    // Example iterating all columns !
    var objRow = objRows.get(i);
    var colNames = objRow.keySet().toArray();
    for (var i in colNames) {
        var colName = colNames[i];
        IDX.add(colName, objRow.get(colName));
    }
*/
    var objRow = objRows.get(i);
    var objClass = objRow.get("obj_class");

    // skip negative examinations if not defined otherwise in catalog settings
    // if (!publishNegativeExaminations && objClass === "12") {
    //     throw new SkipException("Catalog settings say not to publish negative examinations");
    // }

    IDX.add("id", objRow.get("id"));
    IDX.add("uuid", objRow.get("obj_uuid"));

    // title, description, addresses, termsOfUse, category, downloads, license, quellenvermerk, mFund, geothesaurus, Zeitberzug, Zeitspanne, Periodizität
    IDX.add("title", objRow.get("obj_name"));
    IDX.add("description", objRow.get("obj_descr"));
    IDX.add("extras.subgroups", getCategories(objId));
    var dists = getDistributions(objId);
    for (var i=0; i<dists.length; i++) {
        IDX.add("distribution", JSON.stringify(dists[i]));
    }
    IDX.add("extras.license_id", null);
    IDX.add("extras.license_url", null);
    IDX.add("organization", [getOrganization()]);

}

function getCategories(objId) {
    var mcloudCategories = getAdditionalField(objId, "mcloudCategory");
    if (mcloudCategories) {
        var categories = [];
        categories.push(mcloudCategories.get("list_item_id"));
        log.debug("resultCategory: " + categories);
        return categories;
    }
    return [];
}

function getDistributions(objId) {
    var distributions = [];
    var table = getAdditionalFieldTable(objId, "mcloudDownloads");
    if (table) {
        for (var i=0; i<table.length; i++) {
            var row = table[i];
            log.debug("distribution:" + JSON.stringify(row));

            distributions.push({
                format: row["dateFormat"],
                accessUrl: row["link"]
            });
        }
    }
    return distributions;
}

function getOrganization() {
    return null;
}

function getAdditionalField(objId, additionalFieldId) {
    var row = SQL.first("SELECT * FROM additional_field_data WHERE obj_id=? AND field_key=?", [objId, additionalFieldId]);
    if (hasValue(row) && hasValue(row.get("id"))) {
        return row;
    }
    return null;
}


function getAdditionalFieldTable(objId, additionalFieldId) {
    var rows = [];
    var table = getAdditionalField(objId, additionalFieldId);
    var tableResult = SQL.all("SELECT * FROM additional_field_data WHERE parent_field_id=? ORDER BY sort", [table.get("id")]);
    // for (var i=0; i<tableResult.size(); i++) {
        var i = 0;
        var sort = "1";
        var row = {};
        var processing = true;
        while (processing) {
            log.debug("processing: " + i);
            var column = tableResult.get(i);
            log.debug("processing: " + column);
            if (column.get("sort") === sort) {
                row[column.get("field_key")] = column.get("data");
                i++;
            } else {
                sort = column.get("sort");
                rows.push(row);
                row = {};
            }
            log.debug("sort: " + sort);
            if (i === tableResult.size()) {
                rows.push(row);
                processing = false;
            }
        }
    // }
    log.debug("result table: " + rows);
    log.debug("result table: " + rows.length);
    return rows;
}
