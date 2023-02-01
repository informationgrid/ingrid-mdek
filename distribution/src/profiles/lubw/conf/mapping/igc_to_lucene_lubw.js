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
    log.debug("Mapping source record to lucene document: " + sourceRecord.toString());
}

if (!(sourceRecord instanceof DatabaseSourceRecord)) {
    throw new IllegalArgumentException("Record is no DatabaseRecord!");
}

// convert id to number to be used in PreparedStatement as Integer to avoid postgres error !
var objId = +sourceRecord.get("id");

// ---- LUBW specific fields ----
var lubwFields = ["environmentDescription", "oac"];
var addnFieldRows = SQL.all("SELECT * FROM additional_field_data WHERE obj_id = ?", [objId]);
for (var i = 0; i < addnFieldRows.size(); i++) {
    var row = addnFieldRows.get(i);
    var fieldKey = row.get("field_key");
    var data = row.get("data");
    for (var j = 0; j < lubwFields.length; j++) {
        var lubwField = lubwFields[j];
        if (fieldKey == lubwField) {
            IDX.add(fieldKey, data);
        }
    }
}

// add distributor institution to ES document
// type=5 is distributor (Vertrieb)
//var obj = SQL.first("SELECT adr_uuid FROM t012_obj_adr WHERE type = 5 and obj_id = ?", [objId]);
//var address = SQL.first("SELECT institution FROM t02_address WHERE adr_uuid = ?", [obj.get("adr_uuid")]);
var address = SQL.first("SELECT institution FROM t012_obj_adr "
                        + "RIGHT JOIN t02_address ON t012_obj_adr.adr_uuid=t02_address.adr_uuid "
                        + "WHERE t012_obj_adr.type=5 AND t012_obj_adr.obj_id=?", [objId]);
IDX.add("distributor", address.get("institution"));

addTreePath(objId);

function addTreePath(objId) {
    var objRow = SQL.first("SELECT obj_uuid FROM t01_object WHERE id=?", [objId]);
    if (hasValue(objRow)) {
        var objUuid = objRow.get("obj_uuid");
        var row = SQL.first("SELECT tree_path FROM object_node WHERE obj_uuid=?", [objUuid]);
        if (hasValue(row)) {
            var tmpTreePaths = row.get("tree_path");
            tmpTreePaths.split("||").forEach(function (tmpTreePath) {
                if (hasValue(tmpTreePath)) {
                    var tmpObjUuid = tmpTreePath.replaceAll("|", "");
                    var tmpRow = SQL.first("SELECT obj_name FROM t01_object WHERE obj_uuid=?", [tmpObjUuid]);
                    if (hasValue(tmpRow)) {
                        var tmpObjName = tmpRow.get("obj_name");
                        IDX.add("object_node.tree_path.name", tmpObjName);
                    }
                }
            });
        }
    }
}

function hasValue(val) {
    if (typeof val == "undefined") {
        return false;
    } else if (val == null) {
        return false;
    } else if (typeof val == "string" && val == "") {
        return false;
    } else if (typeof val == "object" && Object.keys(val).length === 0) {
        return false;
    } else {
      return true;
    }
}