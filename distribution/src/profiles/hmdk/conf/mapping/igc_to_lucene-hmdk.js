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

// convert id to number to be used in PreparedStatement as Integer to avoid postgres error !
var objId = +sourceRecord.get("id");

var objRows = SQL.all("SELECT * FROM t01_object WHERE id=?", [+objId]);
for (i=0; i<objRows.size(); i++) {
    
    var objUuid = objRows.get(i).get("obj_uuid");
    var publishId = objRows.get(i).get("publish_id");
    var objClass = objRows.get(i).get("obj_class");

    var mapUrl = '';

    if (objClass == "1") {
        var serviceObjects = SQL.all("SELECT * FROM object_reference oRef, t01_object t01 WHERE oRef.obj_to_uuid=? AND oRef.obj_to_uuid=t01.obj_uuid AND t01.obj_class=1 AND oRef.obj_from_id IN (SELECT t01_b.id FROM t01_object t01_b WHERE t01_b.obj_class=3)", [objUuid]);
        log.debug("Found ServiceObjects from uuid=" + objUuid + ": " + serviceObjects.size());
        for (k=0; k<serviceObjects.size(); k++) {
            // get capabilities urls from service object, who links to this object!
            var objFromId = serviceObjects.get(k).get("obj_from_id");
            var capabilitiesUrls = SQL.all("SELECT * FROM object_reference oref, t01_object t01obj, t011_obj_serv serv, t011_obj_serv_operation servOp, t011_Obj_serv_op_connPoint servOpConn WHERE oref.obj_from_id=t01obj.id AND serv.obj_id=t01obj.id AND servOp.obj_serv_id=serv.id AND servOp.name_key=1 AND servOpConn.obj_serv_op_id=servOp.id AND obj_to_uuid=? AND obj_from_id=? AND special_ref=3600 AND serv.type_key=2 AND t01obj.work_state='V'", [objUuid, +objFromId]);
            for (l=0; l<capabilitiesUrls.size(); l++) {
                var objFromIdRow = SQL.first("SELECT obj_uuid FROM t01_object WHERE id=?", [+objFromId]);
                var objFromIdUuid = objFromIdRow.get("obj_uuid");
                if (hasValue(objFromIdUuid)) {
                    mapUrl = addCapabilitiesUrl(capabilitiesUrls.get(l), objUuid, publishId);
                    if(hasValue(mapUrl)) {
                        log.debug('Add external map url to lucene by object_reference.');
                        break;
                    }
                }
            }
        }
    }
    if (objClass == "3") {
        if (!hasValue(mapUrl)) {
            var rows = SQL.all("SELECT t01_object.*, object_reference.special_ref, object_reference.special_name, object_reference.descr FROM object_reference, t01_object WHERE object_reference.obj_from_id=? AND object_reference.obj_to_uuid=t01_object.obj_uuid AND t01_object.work_state=? AND t01_object.obj_class=?", [+objId, 'V', 1]);
            var tmpObjUuids = '';
            for (j=0; j<rows.size(); j++) {
                var tmpObjUuid = rows.get(j).get("obj_uuid");
                if(hasValue(tmpObjUuid)) {
                    if(hasValue(tmpObjUuids)) {
                        tmpObjUuids += ',';
                    }
                    tmpObjUuids += tmpObjUuid;
                }
            }
            if(hasValue(tmpObjUuids)) {
                var subRow = SQL.first("SELECT * FROM t011_obj_serv WHERE obj_id=?", [+objId]);
                mapUrl = addCapabilitiesUrl(subRow, tmpObjUuids, publishId);
                log.debug('Add external map url to lucene by reference objId.');
            }
        }
    }
    if (hasValue(mapUrl)) {
        log.debug('Add external map url to lucene: ' + mapUrl);
        IDX.add("capabilities_url_with_client", mapUrl);    
    }
}

function addCapabilitiesUrl(row, objUuid, publishId) {
    if (hasValue(row)) { 
        if (!hasValue(row.get("has_access_constraint")) || row.get("has_access_constraint") !== 'Y') {
            var intranetMapUrl = 'https://geofos.fhhnet.stadt.hamburg.de/fhh-atlas/?mdid=';
            var internetMapUrl = 'https://geoportal-hamburg.de/geo-online/?mdid=';
            var externalMapUrl = internetMapUrl;
            if (hasValue(publishId)) {
                if (publishId === '2') {
                    externalMapUrl = intranetMapUrl + '' + objUuid;
                } else {
                    externalMapUrl += objUuid;
                }
            } else {
                externalMapUrl += objUuid;
            }
            if (externalMapUrl !== internetMapUrl) {
                return externalMapUrl;
            }
        }
    }
    return '';
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
