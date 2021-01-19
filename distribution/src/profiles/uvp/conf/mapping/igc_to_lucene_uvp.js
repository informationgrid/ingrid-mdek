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
// convert id to number to be used in PreparedStatement as Integer to avoid postgres error !
var objId = +sourceRecord.get("id");
// log.info("Mapping source record to lucene document ID: " + objId);

var objRows = SQL.all("SELECT * FROM t01_object WHERE id=?", [+objId]);
for (i=0; i<objRows.size(); i++) {
/*
    // Example iterating all columns !
    var objRow = objRows.get(i);
    var colNames = objRow.keySet().toArray();
    for (var i in colNames) {
        var colName = colNames[i];
        IDX.add(colName, objRow.get(colName));
    }
*/
    var catalogId = objRows.get(i).get("cat_id");
    var objUuid = objRows.get(i).get("obj_uuid");
    var objClass = objRows.get(i).get("obj_class");
    // skip negative examinations if not defined otherwise in catalog settings
    if (!publishNegativeExaminations && objClass === "12") {
        throw new SkipException("Catalog settings say not to publish negative examinations");
    }

    if (objClass !== "1000") {
        addT01Object(objRows.get(i));

        // ---------- t012_obj_adr ----------
        var rows = SQL.all("SELECT * FROM t012_obj_adr WHERE obj_id=?", [+objId]);
        for (j=0; j<rows.size(); j++) {
            addT012ObjAdr(rows.get(j));
            var adrUuid = rows.get(j).get("adr_uuid");

            // ---------- add referenced address ----------
            addAddress(adrUuid);
        }

        // ---------- t03_catalogue ----------
        var rows = SQL.all("SELECT * FROM t03_catalogue WHERE id=?", [+catalogId]);
        for (j=0; j<rows.size(); j++) {
            addT03Catalogue(rows.get(j));
        }
        
        if(!hasValue(codelist)){
            codelist = 9000;
        }

        // UVP Categories
        var uvpgCategoriesValueRow = SQL.first("SELECT * FROM additional_field_data WHERE obj_id=? AND field_key=?", [+objId, 'uvpgCategory']);
        if (hasValue(uvpgCategoriesValueRow) && hasValue(uvpgCategoriesValueRow.get("id"))) {
            var uvpgCategoryRows = SQL.all("SELECT * FROM additional_field_data WHERE parent_field_id=? AND field_key=?", [uvpgCategoriesValueRow.get("id"), 'categoryId']);
            var uvpCategories = [];
            var uvpCategoryTypes = [];
            for (var i=0; i< uvpgCategoryRows.size(); i++) {
                var categoryId = uvpgCategoryRows.get(i).get("data");
                var uvpNo = TRANSF.getIGCSyslistEntryName(codelist, categoryId, "de");
                var uvpCat = TRANSF.getISOCodeListEntryData(codelist, uvpNo);
                IDX.add("uvp_number", uvpNo);
                if(hasValue(uvpCat)){
                    var uvpCatJson = JSON.parse(uvpCat);
                    if(hasValue(uvpCatJson.cat)){
                        if(uvpCategories.indexOf(uvpCatJson.cat) === -1){
                            log.debug("Test: " + uvpCategories.indexOf(uvpCatJson.cat));
                            uvpCategories.push(uvpCatJson.cat);
                        }
                    }
                    if(hasValue(uvpCatJson.type)) {
                        if (uvpCategoryTypes.indexOf(uvpCatJson.type) === -1) {
                            uvpCategoryTypes.push(uvpCatJson.type);
                        }
                    }
                }
            }
            for (var i=0; i < uvpCategories.length; i++) {
                IDX.add("uvp_category", uvpCategories[i]);
            }
            for (var i=0; i < uvpCategoryTypes.length; i++) {
                IDX.add("uvp_category_type", uvpCategoryTypes[i]);
            }
        }
        
        // Add uvp procedure step
        var phasesRow = SQL.all("SELECT * FROM additional_field_data WHERE obj_id=? AND field_key=?", [+objId, 'UVPPhases']);
        for (var i=0; i < phasesRow.size(); i++) {
            var value = phasesRow.get(i).get("id");
            if (hasValue(value)) {
                var phaseRow = SQL.all("SELECT * FROM additional_field_data WHERE parent_field_id=? ORDER BY sort", [value]);
                if (hasValue(phaseRow)){
                    for (var j=0; j < phaseRow.size(); j++) {
                        var phaseId = phaseRow.get(j).get("id");
                        var phaseFieldKey = phaseRow.get(j).get("field_key");
                        IDX.add("uvp_steps", phaseFieldKey);
                    }
                }
            }
        }

        // Add UVP addresses
        var objAdrValueRow = SQL.first("SELECT * FROM t012_obj_adr WHERE obj_id=? ORDER BY line", [+objId]);
        if (hasValue(objAdrValueRow) && hasValue(objAdrValueRow.get("adr_uuid"))) {
            var adrValueRow = SQL.first("SELECT * FROM t02_address WHERE adr_uuid=?", [objAdrValueRow.get("adr_uuid")]);
            var addrId = adrValueRow.get("id");
            var parentAdressRow = SQL.first("SELECT t02_address.* FROM t02_address, address_node WHERE address_node.addr_id_published=? AND address_node.fk_addr_uuid=t02_address.adr_uuid AND t02_address.work_state=?", [addrId, "V"]);

            var parentAddress = [];
            
            if(hasValue(parentAdressRow)){
                parentAddress.push(parentAdressRow);
            }
            while (hasValue(parentAdressRow)) {
                addrId = parentAdressRow.get("id");
                parentAdressRow = SQL.first("SELECT t02_address.* FROM t02_address, address_node WHERE address_node.addr_id_published=? AND address_node.fk_addr_uuid=t02_address.adr_uuid AND t02_address.work_state=?", [addrId, "V"]);
                if(hasValue(parentAdressRow) && !isHiddenAddress(parentAdressRow)){
                    parentAddress.push(parentAdressRow);
                }
            }
            
            for (var index = parentAddress.length - 1; index >= 0; --index) {
                addAddressRow(parentAddress[index]);
            }
            
            if(!isHiddenAddress(adrValueRow)){
                addAddressRow(adrValueRow);
            }
        }
        
        // add spatial Bounding Box
        var uvpSpatialValueRow = SQL.first("SELECT * FROM additional_field_data WHERE obj_id=? AND field_key=?", [+objId, 'uvp_spatialValue']);
        if (hasValue(uvpSpatialValueRow) && hasValue(uvpSpatialValueRow.get("data"))) {
            // format <name>: <lon_min, lat_min, lon_max, lat_max>
            // <name> is optional
            // Deutschland, Berlin, Berlin: 13.252258300781248, 52.43424610262303, 13.52691650390625, 52.60137941045533
            var spatialValue = uvpSpatialValueRow.get("data");
            
            var spatialLocation = spatialValue.lastIndexOf(": ") === -1 ? false : spatialValue.substr(0, spatialValue.lastIndexOf(": "));
            if (spatialLocation) {
                IDX.add("location", spatialLocation);
            }
            
            spatialValue = spatialValue.lastIndexOf(": ") === -1 ? spatialValue : spatialValue.substr(spatialValue.lastIndexOf(": ") + 2);
            var spatialArray = spatialValue.split(',');
            // convert to numbers
            for (var i=0; i<4; i++) {
                spatialArray[i] = Number(spatialArray[i]);
            }
            
            // [x1, x2, y1, y2] = [lon_min, lon_max, lat_min, lat_max]
            IDX.add("x1", spatialArray[0]);
            IDX.add("x2", spatialArray[2]);
            IDX.add("y1", spatialArray[1]);
            IDX.add("y2", spatialArray[3]);
            
            IDX.add("lon_min", spatialArray[0]);
            IDX.add("lon_max", spatialArray[2]);
            IDX.add("lat_min", spatialArray[1]);
            IDX.add("lat_max", spatialArray[3]);
            
            // get center
            var latCenter = spatialArray[1] + (spatialArray[3] - spatialArray[1])/2;
            var lonCenter = spatialArray[0] + (spatialArray[2] - spatialArray[0])/2;
            IDX.add("lon_center", lonCenter);
            IDX.add("lat_center", latCenter);
        }
        
        // add UVP specific mapping
        // UVP checkbox examination
        var uvpNeedsExamination = SQL.first("SELECT * FROM additional_field_data WHERE obj_id=? AND field_key=?", [+objId, 'uvpNeedsExamination']);
        if (hasValue(uvpNeedsExamination) && hasValue(uvpNeedsExamination.get("data"))) {
            var value = uvpNeedsExamination.get("data");
            if (value === "true") {
                IDX.add("needs_examination", true);
            }
        }
    } else {
        addT01ObjectFolder(objRows.get(i));
    }

    // ---------- object_node CHILDREN ----------
    // only published ones !
    var rows = SQL.all("SELECT * FROM object_node WHERE fk_obj_uuid=? AND obj_id_published IS NOT NULL", [objUuid]);
    for (j=0; j<rows.size(); j++) {
        addObjectNodeChildren(rows.get(j));
    }
    // ---------- object_node PARENT ----------
    // NOTICE: Has to be published !
    var rows = SQL.all("SELECT fk_obj_uuid FROM object_node WHERE obj_uuid=?", [objUuid]);
    for (j=0; j<rows.size(); j++) {
        addObjectNodeParent(rows.get(j));
    }

}

function addT01ObjectFolder(row) {
    IDX.add("t01_object.id", row.get("id"));
    IDX.add("t01_object.obj_id", row.get("obj_uuid"));
    IDX.add("title", row.get("obj_name"));
    IDX.add("t01_object.org_obj_id", row.get("org_obj_id"));
    IDX.add("t01_object.obj_class", row.get("obj_class"));
    IDX.add("summary", row.get("obj_descr"));
    IDX.add("t01_object.cat_id", row.get("cat_id"));
    IDX.add("t01_object.info_note", row.get("info_note"));
    IDX.add("t01_object.loc_descr", row.get("loc_descr"));

    IDX.add("t01_object.publish_id", row.get("publish_id"));
    // also add plain "publish_id" so objects AND addresses can be queried with "publish_id:1" ...
    IDX.add("publish_id", row.get("publish_id"));
    IDX.add("t01_object.is_catalog_data", row.get("is_catalog_data"));
    IDX.add("t01_object.create_time", row.get("create_time"));
    IDX.add("t01_object.mod_time", row.get("mod_time"));
    IDX.add("t01_object.metadata_time", row.get("metadata_time"));
    IDX.add("isfolder", true);
}
function isHiddenAddress(adrRow) {
    return adrRow["hide_address"] === "Y";
}

function addAddressRow(adrValueRow){
    var institution = adrValueRow.get("institution");
    var address_value = adrValueRow.get("address_value");
    var title_value = adrValueRow.get("title_value");
    var firstname = adrValueRow.get("firstname");
    var lastname = adrValueRow.get("lastname");
    var uvp_address = "";
    
    // Add institution
    if(hasValue(institution)){
        IDX.add("uvp_address", institution);
    }

    // Add person
    if(hasValue(address_value)){
        uvp_address += " " + address_value ;
    }
    if(hasValue(title_value)){
        uvp_address += " " + title_value;
    }
    if(hasValue(firstname)){
        uvp_address += " "+ firstname;
    }
    if(hasValue(lastname)){
        uvp_address += " " + lastname;
    }
    
    if(hasValue(uvp_address)){
        IDX.add("uvp_address", uvp_address.trim());
    }
}

function addT01Object(row) {
    IDX.add("t01_object.id", row.get("id"));
    IDX.add("t01_object.obj_id", row.get("obj_uuid"));
    IDX.add("title", row.get("obj_name"));
    IDX.add("t01_object.org_obj_id", row.get("org_obj_id"));
    IDX.add("t01_object.obj_class", row.get("obj_class"));
    IDX.add("summary", row.get("obj_descr"));
    IDX.add("t01_object.cat_id", row.get("cat_id"));
    IDX.add("t01_object.info_note", row.get("info_note"));
    IDX.add("t01_object.loc_descr", row.get("loc_descr"));

    // time: first add pure database values (not needed, but we can do this now ;)
    IDX.add("t01_object.time_from", row.get("time_from"));
    IDX.add("t01_object.time_to", row.get("time_to"));
    IDX.add("t01_object.time_type", row.get("time_type"));

    IDX.add("t01_object.publish_id", row.get("publish_id"));
    // also add plain "publish_id" so objects AND addresses can be queried with "publish_id:1" ...
    IDX.add("publish_id", row.get("publish_id"));

    IDX.add("t01_object.is_catalog_data", row.get("is_catalog_data"));
    IDX.add("t01_object.create_time", row.get("create_time"));
    IDX.add("t01_object.mod_time", row.get("mod_time"));
    IDX.add("created", TRANSF.getISODateFromIGCDate(row.get("create_time")));
    IDX.add("modified", TRANSF.getISODateFromIGCDate(row.get("mod_time")));
    IDX.add("t01_object.mod_uuid", row.get("mod_uuid"));
    IDX.add("t01_object.responsible_uuid", row.get("responsible_uuid"));
    IDX.add("isfolder", false);
}
function addT03Catalogue(row) {
    IDX.add("t03_catalogue.cat_uuid", row.get("cat_uuid"));
    IDX.add("t03_catalogue.cat_name", row.get("cat_name"));
    IDX.add("t03_catalogue.cat_namespace", row.get("cat_namespace"));
    IDX.add("t03_catalogue.country_key", row.get("country_key"));
    IDX.add("t03_catalogue.country_code", row.get("country_value"));
    IDX.add("t03_catalogue.language_key", row.get("language_key"));
    IDX.add("t03_catalogue.language_code", row.get("language_value"));
    IDX.add("t03_catalogue.workflow_control", row.get("workflow_control"));
    IDX.add("t03_catalogue.expiry_duration", row.get("expiry_duration"));
    IDX.add("t03_catalogue.create_time", row.get("create_time"));
    IDX.add("t03_catalogue.mod_uuid", row.get("mod_uuid"));
    IDX.add("t03_catalogue.mod_time", row.get("mod_time"));
    // also language so index can deliver language specific requests !
    // e.g. when portal requests language dependent !
    IDX.add("lang", TRANSF.getLanguageShortcutFromIGCCode(row.get("language_key")));
}
function addT012ObjAdr(row) {
    IDX.add("t012_obj_adr.line", row.get("line"));
    IDX.add("t012_obj_adr.adr_id", row.get("adr_uuid"));
    IDX.add("t012_obj_adr.typ", row.get("type"));
    IDX.add("t012_obj_adr.special_ref", row.get("special_ref"));
    IDX.add("t012_obj_adr.special_name", row.get("special_name"));
    IDX.add("t012_obj_adr.mod_time", row.get("mod_time"));
}

// Adds address to index. If address is hidden then parent address is added.
// Also adds address children not hidden (queried from portal ???).
function addAddress(addrUuid) {
    // ---------- address_node ----------
    var addrNodeRows = SQL.all("SELECT * FROM address_node WHERE addr_uuid=? AND addr_id_published IS NOT NULL", [addrUuid]);
    for (k=0; k<addrNodeRows.size(); k++) {
        var parentAddrUuid = addrNodeRows.get(k).get("fk_addr_uuid");
        var addrIdPublished = addrNodeRows.get(k).get("addr_id_published");

        // ---------- t02_address ----------
        var addrRow = SQL.first("SELECT * FROM t02_address WHERE id=? and (hide_address IS NULL OR hide_address != 'Y')", [+addrIdPublished]);
        if (hasValue(addrRow)) {
            // address not hidden, add all data
            addT02Address(addrRow);

            // ---------- t021_communication ----------
            var commRows = SQL.all("SELECT * FROM t021_communication WHERE adr_id=?", [+addrIdPublished]);
            for (l=0; l<commRows.size(); l++) {
                addT021Communication(commRows.get(l));
            }

            // ---------- address_node CHILDREN, queried from portal ??? ----------
            // only children published and NOT hidden !
            var childRows = SQL.all("SELECT address_node.* FROM address_node, t02_address WHERE address_node.fk_addr_uuid=? AND address_node.addr_id_published=t02_address.id AND (t02_address.hide_address IS NULL OR t02_address.hide_address != 'Y')", [addrUuid]);
            for (l=0; l<childRows.size(); l++) {
                addAddressNodeChildren(childRows.get(l));
            }

        } else {
            if (log.isDebugEnabled()) {
                log.debug("Hidden address !!! uuid=" + addrUuid + " -> instead map parent address uuid=" + parentAddrUuid);
            }
            // address hidden, add parent !
            if (hasValue(parentAddrUuid)) {
                addAddress(parentAddrUuid);
            }
        }
    }
}

function addT02Address(row) {
    IDX.add("t02_address.adr_id", row.get("adr_uuid"));
    IDX.add("t02_address.org_adr_id", row.get("org_adr_id"));
    IDX.add("t02_address.typ", row.get("adr_type"));
    IDX.add("t02_address.institution", row.get("institution"));
    IDX.add("t02_address.lastname", row.get("lastname"));
    IDX.add("t02_address.firstname", row.get("firstname"));
    IDX.add("t02_address.address_key", row.get("address_key"));
    IDX.add("t02_address.address_value", row.get("address_value"));
    IDX.add("t02_address.title_key", row.get("title_key"));
    IDX.add("t02_address.title", row.get("title_value"));
    IDX.add("t02_address.street", row.get("street"));
    IDX.add("t02_address.postcode", row.get("postcode"));
    IDX.add("t02_address.postbox", row.get("postbox"));
    IDX.add("t02_address.postbox_pc", row.get("postbox_pc"));
    IDX.add("t02_address.city", row.get("city"));
    IDX.add("t02_address.country_key", row.get("country_key"));
    IDX.add("t02_address.country_code", row.get("country_value"));
    IDX.add("t02_address.job", row.get("job"));
    IDX.add("t02_address.descr", row.get("descr"));
    IDX.add("t02_address.create_time", row.get("create_time"));
    IDX.add("t02_address.mod_time", row.get("mod_time"));
    IDX.add("t02_address.mod_uuid", row.get("mod_uuid"));
    IDX.add("t02_address.responsible_uuid", row.get("responsible_uuid"));
}
function addT021Communication(row) {
    IDX.add("t021_communication.line", row.get("line"));
    IDX.add("t021_communication.commtype_key", row.get("commtype_key"));
    IDX.add("t021_communication.comm_type", row.get("commtype_value"));
    IDX.add("t021_communication.comm_value", row.get("comm_value"));
    IDX.add("t021_communication.descr", row.get("descr"));
}
function addAddressNodeChildren(row) {
    // QUERIED FROM PORTAL !?
    IDX.add("t022_adr_adr.adr_from_id", row.get("fk_addr_uuid"));
    IDX.add("t022_adr_adr.adr_to_id", row.get("addr_uuid"));
}
function addObjectNodeChildren(row) {
    IDX.add("children.object_node.obj_uuid", row.get("obj_uuid"));
}
function addObjectNodeParent(row) {
    IDX.add("parent.object_node.obj_uuid", row.get("fk_obj_uuid"));
}

