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
    log.debug('Mapping source record to lucene document: ' + sourceRecord.toString());
}

if (!(sourceRecord instanceof DatabaseSourceRecord)) {
    throw new IllegalArgumentException('Record is no DatabaseRecord!');
}

// add default boost value
IDX.addDocumentBoost(1.0);

// ---------- t01_object ----------
var objId = +sourceRecord.get('id');
var objRows = SQL.all('SELECT * FROM t01_object WHERE id=?', [objId]);
for (var i=0; i<objRows.size(); i++) {

    var objRow = objRows.get(i);
    var objClass = objRow.get('obj_class');

    IDX.add('id', objRow.get('id'));
    IDX.add('uuid', objRow.get('obj_uuid'));

    // title, description, addresses, termsOfUse, category, downloads, license, quellenvermerk, mFund, geothesaurus, Zeitberzug, Zeitspanne, Periodizität
    IDX.add('title', objRow.get('obj_name'));
    IDX.add('description', objRow.get('obj_descr'));

    // issued (created) and modified dates
    IDX.add('issued', toMilliseconds(objRow.get('create_time')));
    IDX.add('modified', toMilliseconds(objRow.get('mod_time')));
    IDX.addAllFromJSON( JSON.stringify({accessRights: [ getAdditionalFieldData(objId, 'mcloudTermsOfUse')] }) );

    var extras = createExtras(objId, objRow);
    IDX.addAllFromJSON('{ "extras": { ' + extras + ' } }');

    var dists = getDistributions(objId);
    IDX.addAllFromJSON('{ "distribution": [' + dists.join(',') + ']}');
    IDX.addAllFromJSON('{ "publisher": ' + getOrganizations(objId) + '}');

}

function createExtras(objId, objRow) {
    var extrasArray = [];
    extrasArray.push( '"subgroups": ' + getCategories(objId) );
    extrasArray.push( '"license_id": "' + getAdditionalFieldData(objId, 'mcloudLicense') + '"' );
    extrasArray.push( '"license_url": "' + getAdditionalFieldData(objId, 'mcloudLicenseUrl') + '"' );
    extrasArray.push( '"realtime": ' + (objRow.get('time_period') === '1' ? 'true' : 'false') );

    return extrasArray.join(',');
}

function getCategories(objId) {
    var mcloudCategories = getAdditionalField(objId, 'mcloudCategory');
    if (mcloudCategories) {
        var categories = [];
        categories.push(mcloudCategories.get('list_item_id'));
        return JSON.stringify(categories);
    }
    return [];
}

function getDistributions(objId) {
    var distributions = [];
    var table = getAdditionalFieldTable(objId, 'mcloudDownloads');
    if (table) {
        for (var i=0; i<table.length; i++) {
            var row = table[i];
            distributions.push(JSON.stringify({
                format: row['dateFormat'],
                accessURL: row['link'],
                description: row['title']
            }));
        }
    }
    return distributions;
}

function getOrganizations(objId) {
    var publisher = [];

    var rows = SQL.all("SELECT * FROM t012_obj_adr WHERE obj_id=? AND type=10", [+objId]); // type 10 is Publisher/Herausgeber
    for (j=0; j<rows.size(); j++) {
        var addrUuid = rows.get(j).get("adr_uuid");

        var addrNodeRows = SQL.all("SELECT * FROM address_node WHERE addr_uuid=? AND addr_id_published IS NOT NULL", [addrUuid]);
        for (k=0; k<addrNodeRows.size(); k++) {
            var parentAddrUuid = addrNodeRows.get(k).get("fk_addr_uuid");
            var addrIdPublished = addrNodeRows.get(k).get("addr_id_published");

            var addrRow = SQL.first("SELECT * FROM t02_address WHERE id=? and (hide_address IS NULL OR hide_address != 'Y')", [+addrIdPublished]);
            if (hasValue(addrRow)) {
                var homepage = "";
                var commRows = SQL.all("SELECT * FROM t021_communication WHERE adr_id=? AND commtype_key = 4", [+addrIdPublished]); // commtype_4 is url
                if (commRows && commRows.size() > 0) {
                    // Use the first available value
                    homepage = commRows.get(0).get('comm_value');
                }

                publisher.push({
                    "organization": getOrganization(addrRow),
                    "homepage": homepage
                });

            }
        }
    }
    return JSON.stringify(publisher);
}

function getOrganization(addrRow) {
    var organization = getIndividualNameFromAddressRow(addrRow);

    if (!organization) {
        organization = addrRow.get('institution');
    }

    return organization;
}

/**
 * Get the individual name from a address record.
 *
 * @param addressRow
 * @return The individual name.
 */
function getIndividualNameFromAddressRow(addressRow) {
    var individualName = "";
    var addressing = addressRow.get("address_value");
    var title = addressRow.get("title_value");
    var firstName = addressRow.get("firstname");
    var lastName = addressRow.get("lastname");

    var name = "";

    if (hasValue(title) && !hasValue(addressing)) {
        name = title + " ";
    } else if (!hasValue(title) && hasValue(addressing)) {
        name = addressing + " ";
    } else if (hasValue(title) && hasValue(addressing)) {
        name = addressing + " " + title + " ";
    }

    if (hasValue(firstName)) {
        name = name + "" + firstName +  " ";
    }

    if (hasValue(lastName)) {
        name = name + "" + lastName;
    }

    return name;
}

function getAdditionalField(objId, additionalFieldId) {
    var row = SQL.first('SELECT * FROM additional_field_data WHERE obj_id=? AND field_key=?', [objId, additionalFieldId]);
    if (hasValue(row) && hasValue(row.get('id'))) {
        return row;
    }
    return null;
}

function getAdditionalFieldData(objId, additionalFieldId) {
    var field = getAdditionalField(objId, additionalFieldId);
    return (field === null) ? '' : field.get('data');
}

function getAdditionalFieldTable(objId, additionalFieldId) {
    var rows = [];
    var table = getAdditionalField(objId, additionalFieldId);
    var tableResult = SQL.all('SELECT * FROM additional_field_data WHERE parent_field_id=? ORDER BY sort', [+table.get('id')]);

    var i = 0;
    var sort = '1';
    var row = {};
    var processing = true;
    while (processing) {
        var column = tableResult.get(i);
        if (column.get('sort') === sort) {
            row[column.get('field_key')] = column.get('data');
            i++;
        } else {
            sort = column.get('sort');
            rows.push(row);
            row = {};
        }
        if (i === tableResult.size()) {
            rows.push(row);
            processing = false;
        }
    }
    return rows;
}

function toMilliseconds(igcTimestamp) {
    var millis = TRANSF.getISODateFromIGCDate(igcTimestamp);
    var date = Date.parse(millis);
    return date;
}

