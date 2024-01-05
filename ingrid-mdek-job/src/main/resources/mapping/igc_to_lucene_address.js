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

if (log.isDebugEnabled()) {
	log.debug("Mapping source record to lucene document: " + sourceRecord.toString());
}

if (!(sourceRecord instanceof DatabaseSourceRecord)) {
    throw new IllegalArgumentException("Record is no DatabaseRecord!");
}

//add default boost value
IDX.addDocumentBoost((Java.type('java.lang.Float')).parseFloat("0.1"));

// ---------- t02_address ----------
// convert id to number to be used in PreparedStatement as Integer to avoid postgres error !
var addrId = +sourceRecord.get("id");

// only index addresses where hide_address is not set !
var addrRows = SQL.all("SELECT * FROM t02_address WHERE id=? and (hide_address IS NULL OR hide_address != 'Y')", [+addrId]);
for (i=0; i<addrRows.size(); i++) {

    var adrType = addrRows.get(i).get("adr_type");
    var addrUuid = addrRows.get(i).get("adr_uuid");
    if (adrType !== "1000") {
        addT02Address(addrRows.get(i));

        // ---------- t021_communication ----------
        var rows = SQL.all("SELECT * FROM t021_communication WHERE adr_id=?", [+addrId]);
        for (j=0; j<rows.size(); j++) {
            addT021Communication(rows.get(j));
        }
        // ---------- searchterm_adr ----------
        var rows = SQL.all("SELECT * FROM searchterm_adr WHERE adr_id=?", [+addrId]);
        for (j=0; j<rows.size(); j++) {
            addSearchtermAdr(rows.get(j));
            var searchtermId = rows.get(j).get("searchterm_id");

            // ---------- searchterm_value ----------
            var subRows = SQL.all("SELECT * FROM searchterm_value WHERE id=?", [+searchtermId]);
            for (k=0; k<subRows.size(); k++) {
                addSearchtermValue(subRows.get(k));
                var searchtermSnsId = subRows.get(k).get("searchterm_sns_id");           
                if (hasValue(searchtermSnsId)) {
                    // ---------- searchterm_sns ----------
                    var subSubRows = SQL.all("SELECT * FROM searchterm_sns WHERE id=?", [+searchtermSnsId]);
                    for (l=0; l<subSubRows.size(); l++) {
                        addSearchtermSns(subSubRows.get(l));
                    }
                }
            }
        }
    } else {
        addT02AddressFolder(addrRows.get(i));
    }

    // ---------- address_node CHILDREN ----------
    // only children published and NOT hidden !
    var rows = SQL.all("SELECT t02_address.* FROM address_node, t02_address WHERE address_node.fk_addr_uuid=? AND address_node.addr_id_published=t02_address.id AND (t02_address.hide_address IS NULL OR t02_address.hide_address != 'Y')", [addrUuid]);
    for (j=0; j<rows.size(); j++) {
        addAddressNodeChildren(rows.get(j));
    }
    // ---------- add all PARENTS ----------
    var row = SQL.first("SELECT fk_addr_uuid FROM address_node WHERE addr_uuid=?", [addrUuid]);
    var parentUuid = row.get("fk_addr_uuid");
    var level = 1;
    while (hasValue(parentUuid)) {
        // NOTICE: Parents HAVE TO BE published if child is published ! We do NOT check hidden address cause only persons are hidden and persons cannot be parents
        //         It's also valid if parent is a folder!
        var parentRow = SQL.first("SELECT * FROM address_node, t02_address WHERE address_node.addr_uuid=? AND (address_node.addr_id_published=t02_address.id OR (address_node.addr_id=t02_address.id AND t02_address.adr_type=1000))", [parentUuid]);
        addAddressParent(level, parentRow);
        parentUuid = parentRow.get("fk_addr_uuid");
        level++;
    }
}

function addT02AddressFolder(row) {
    IDX.add("t02_address.id", row.get("id"));
    IDX.add("t02_address.adr_id", row.get("adr_uuid"));
    IDX.add("t02_address.org_adr_id", row.get("org_adr_id"));
    IDX.add("t02_address.typ", row.get("adr_type"));
    IDX.add("title", row.get("lastname"));
    IDX.add("summary", row.get("job"));
    IDX.add("t02_address.work_state", row.get("work_state"));
    IDX.add("t02_address.create_time", row.get("create_time"));
    IDX.add("t02_address.mod_time", row.get("mod_time"));
    var created = TRANSF.getISODateFromIGCDate(row.get("create_time"));
    if (created) {
        IDX.add("created", created);
    }
    var modified = TRANSF.getISODateFromIGCDate(row.get("mod_time"));
    if (modified) {
        IDX.add("modified", modified);
    }
    IDX.add("t02_address.mod_uuid", row.get("mod_uuid"));
    IDX.add("t02_address.responsible_uuid", row.get("responsible_uuid"));
    IDX.add("t02_address.publish_id", row.get("publish_id"));
    // also add plain "publish_id" so objects AND addresses can be queried with "publish_id:1" ...
    IDX.add("publish_id", row.get("publish_id"));
    IDX.add("isfolder", "true");
}
function addT02Address(row) {
    IDX.add("t02_address.id", row.get("id"));
    IDX.add("t02_address.adr_id", row.get("adr_uuid"));
    IDX.add("t02_address.org_adr_id", row.get("org_adr_id"));
    IDX.add("t02_address.typ", row.get("adr_type"));
    IDX.add("title", row.get("institution"));
    IDX.add("t02_address.lastname", row.get("lastname"));
    IDX.add("t02_address.firstname", row.get("firstname"));
    IDX.add("t02_address.address_key", row.get("address_key"));
    IDX.add("t02_address.address_value", row.get("address_value"));
    IDX.add("t02_address.title_key", row.get("title_key"));
    IDX.add("t02_address.title", row.get("title_value"));
    IDX.add("street", row.get("street"));
    IDX.add("zip", row.get("postcode"));
    IDX.add("t02_address.postbox", row.get("postbox"));
    IDX.add("t02_address.postbox_pc", row.get("postbox_pc"));
    IDX.add("city", row.get("city"));
    IDX.add("t02_address.country_key", row.get("country_key"));
    IDX.add("t02_address.country_code", row.get("country_value"));
    IDX.add("summary", row.get("job"));
    IDX.add("t02_address.work_state", row.get("work_state"));
    IDX.add("t02_address.create_time", row.get("create_time"));
    IDX.add("t02_address.mod_time", row.get("mod_time"));
    var created = TRANSF.getISODateFromIGCDate(row.get("create_time"));
    if (created) {
        IDX.add("created", created);
    }
    var modified = TRANSF.getISODateFromIGCDate(row.get("mod_time"));
    if (modified) {
        IDX.add("modified", modified);
    }
    IDX.add("t02_address.mod_uuid", row.get("mod_uuid"));
    IDX.add("t02_address.responsible_uuid", row.get("responsible_uuid"));
    IDX.add("t02_address.publish_id", row.get("publish_id"));
    // also add plain "publish_id" so objects AND addresses can be queried with "publish_id:1" ...
    IDX.add("publish_id", row.get("publish_id"));
    IDX.add("isfolder", "false");
}
function addT021Communication(row) {
    IDX.add("t021_communication.line", row.get("line"));
    IDX.add("t021_communication.commtype_key", row.get("commtype_key"));
    IDX.add("t021_communication.commtype_value", row.get("commtype_value"));
    IDX.add("t021_communication.comm_value", row.get("comm_value"));
    IDX.add("t021_communication.descr", row.get("descr"));
}
function addAddressNodeChildren(row) {
    IDX.add("children.address_node.addr_uuid", row.get("adr_uuid"));
    IDX.add("children.address_node.addr_type", row.get("adr_type"));
}
function addAddressParent(level, row) {
    if (level == 1) {
        IDX.add("parent.address_node.addr_uuid", row.get("adr_uuid"));
    } else {
        IDX.add("parent".concat(level).concat(".address_node.addr_uuid"), row.get("adr_uuid"));
    }
    IDX.add("t02_address".concat(level + 1).concat(".adr_id"), row.get("adr_uuid"));
    IDX.add("t02_address".concat(level + 1).concat(".typ"), row.get("adr_type"));
    IDX.add("title".concat(level + 1), row.get("institution"));
    if(row.get("adr_type") != "1000"){
        IDX.add("t02_address.parents.title", row.get("institution"));
    }
}
function addSearchtermAdr(row) {
    IDX.add("searchterm_adr.line", row.get("line"));
}
function addSearchtermValue(row) {
    IDX.add("t04_search.type", row.get("type"));
    IDX.add("t04_search.searchterm", row.get("term"));
}
function addSearchtermSns(row) {
    IDX.add("searchterm_sns.sns_id", row.get("sns_id"));
}
