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
importPackage(Packages.de.ingrid.mdek);

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

// **********************************************
// The following mapping object must be equal to:
//     https://gitlab.wemove.com/mcloud/mcloud-ckan-importer/tree/develop/server/model/index-document.ts
// **********************************************
function map(mapper) {
    return {
        // this is an additional field we need for updating a document
        // we cannot use "extras.generated_id" since nesting is not supported
        // in our update function (IndexManager::update)
        uuid: mapper.getGeneratedId(),
        title: mapper.getTitle(),
        description: mapper.getDescription(),
        theme: mapper.getThemes(),
        issued: mapper.getIssued(),
        modified: mapper.getModifiedDate(),
        accrual_periodicity: mapper.getAccrualPeriodicity(),
        contact_point: mapper.getContactPoint(),
        keywords: mapper.getKeywords(),
        creator: mapper.getCreator(),
        originator: mapper.getOriginator(),
        publisher: mapper.getPublisher(),
        access_rights: mapper.getAccessRights(),
        distribution: mapper.getDistributions(),
        extras: {
            metadata: {
                source: mapper.getMetadataSource(),
                issued: mapper.getMetadataIssued(),
                modified: mapper.getMetadataModified(),
                harvested: mapper.getMetadataHarvested(),
                harvesting_errors: null, // get errors after all operations been done
                is_valid: mapper.isValid(), // checks validity after all operations been done
            },
            generated_id: mapper.getGeneratedId(),
            subgroups: mapper.getCategories(),
            license: mapper.getLicense(),
            harvested_data: mapper.getHarvestedData(),
            subsection: mapper.getSubSections(),
            spatial: mapper.getSpatial(),
            spatial_text: mapper.getSpatialText(),
            temporal: mapper.getTemporal(),
            groups: mapper.getGroups(),
            display_contact: mapper.getDisplayContacts(),
            all: mapper.getExtrasAllData(),
            realtime: mapper.isRealtime(),
            citation: mapper.getCitation(),
            mfund_fkz: mapper.getMFundFKZ(),
            mfund_project_title: mapper.getMFundProjectTitle()
        }
    };
}

// ---------- t01_object ----------
var objId = +sourceRecord.get('id');
var objRows = SQL.all('SELECT * FROM t01_object WHERE id=?', [objId]);
var spatialRows = SQL.all("SELECT spatial_ref_value.* FROM spatial_reference, spatial_ref_value WHERE spatial_reference.spatial_ref_id=spatial_ref_value.id AND spatial_reference.obj_id=?", [+objId]);
for (var i=0; i<objRows.size(); i++) {

    var objRow = objRows.get(i);
    var objClass = objRow.get('obj_class');
    var objUuid = objRow.get('obj_uuid')

    log.debug("Map ID: " + objId);
    var mapper = new McloudMapper({
        objId: objId,
        objUuid: objUuid,
        objRow: objRow,
        spatialRows: spatialRows
    });
    var doc = map(mapper);

    log.debug("add doc to index");
    IDX.addAllFromJSON(JSON.stringify(doc));

}


