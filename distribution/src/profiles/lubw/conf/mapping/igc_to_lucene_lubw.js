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

// ---- add distributor lastname or institution to ES document ----
//var distributor = SQL.first("SELECT institution, lastname FROM t012_obj_adr "
//                        + "RIGHT JOIN t02_address ON t012_obj_adr.adr_uuid=t02_address.adr_uuid "
//                        + "WHERE t012_obj_adr.type=5 AND t012_obj_adr.obj_id=?", [objId]); // type=5 is distributor
var distributor = SQL.first(
    "SELECT t02_parent.institution FROM t012_obj_adr "
    + "RIGHT JOIN t02_address AS t02_orig ON t012_obj_adr.adr_uuid=t02_orig.adr_uuid "
    + "RIGHT JOIN address_node ON address_node.addr_id=t02_orig.id "
    + "RIGHT JOIN t02_address AS t02_parent ON t02_parent.adr_uuid=substring(address_node.tree_path, 2, 36) "
    + "WHERE t012_obj_adr.type=5 AND t012_obj_adr.obj_id=?", [objId]
);
if (hasValue(distributor)) {
    var name = distributor.get("institution");
    if (hasValue(name)) {
        name = name.replaceAll(/\n+/g, " ");
        IDX.add("distributor", name);
    }
}

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
                    var tmpRow = SQL.first("SELECT obj_name, obj_uuid FROM t01_object WHERE obj_uuid=?", [tmpObjUuid]);
                    if (hasValue(tmpRow)) {
                        IDX.add("object_node.tree_path.name", tmpRow.get("obj_name"));
                        IDX.add("object_node.tree_path.uuid", tmpRow.get("obj_uuid"));
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