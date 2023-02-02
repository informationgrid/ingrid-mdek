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
if (javaVersion.indexOf( "1.8" ) === 0) {
    load("nashorn:mozilla_compat.js");
}

importPackage(Packages.org.apache.lucene.document);
importPackage(Packages.de.ingrid.iplug.dsc.om);
importPackage(Packages.de.ingrid.geo.utils.transformation);

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
var distributor = SQL.first("SELECT institution, lastname FROM t012_obj_adr "
                        + "RIGHT JOIN t02_address ON t012_obj_adr.adr_uuid=t02_address.adr_uuid "
                        + "WHERE t012_obj_adr.type=5 AND t012_obj_adr.obj_id=?", [objId]); // type=5 is distributor
if (hasValue(distributor)) {
    // use the more specific information first ("lastname" is typically also used for sub-institutions)
    var name = distributor.get("lastname");
    // if no lastname is found, use "institution" instead
    if (!hasValue(name)) {
        name = distributor.get("institution");
    }
    // only add distributor if there is something to add
    if (hasValue(name)) {
//        // get abbreviation for institution from LUBW-specific codelist if it exists
//        var lubwDistributorCodelistId = 10100;
//        var igcEntryId = TRANSF.getISOCodeListEntryId(lubwDistributorCodelistId, name);
//        if (hasValue(igcEntryId)) {
//            var abbreviation = TRANSF.getCodeListEntryFromIGCSyslistEntry(lubwDistributorCodelistId, igcEntryId, "abbreviation");
//            if (hasValue(abbreviation)) {
//                name = abbreviation;
//            }
//        }
        IDX.add("distributor", name);
    }
}