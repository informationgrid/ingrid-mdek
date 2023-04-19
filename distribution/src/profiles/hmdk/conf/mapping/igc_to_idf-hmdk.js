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

//add Namespaces to Utility for convenient handling of NS !
DOM.addNS("gmd", "http://www.isotc211.org/2005/gmd");
DOM.addNS("gco", "http://www.isotc211.org/2005/gco");

if (!(sourceRecord instanceof DatabaseSourceRecord)) {
    throw new IllegalArgumentException("Record is no DatabaseRecord!");
}

var id = sourceRecord.get("id");

var contentLabel = SQL.all("SELECT add1.data FROM additional_field_data add1 WHERE add1.obj_id=? AND add1.field_key=?", [+id, "publicationHmbTG"]);
if (contentLabel && contentLabel.size() > 0) {
    var isChecked = contentLabel.get(0).get("data") == "true";
    if (isChecked) {

        var objRow = SQL.first("SELECT obj_class FROM t01_object WHERE id=?", [+id]);
        var objClass = objRow.get("obj_class");

        var i;
        var dataIdentification;
        if (objClass == "3") {
            dataIdentification = DOM.getElement(idfDoc, "//idf:idfMdMetadata/gmd:identificationInfo/srv:SV_ServiceIdentification");
        } else {
            dataIdentification = DOM.getElement(idfDoc, "//idf:idfMdMetadata/gmd:identificationInfo/gmd:MD_DataIdentification");
        }

        var path = ["gmd:resourceFormat", "gmd:graphicOverview", "gmd:resourceMaintenance","gmd:pointOfContact", "gmd:status","gmd:credit","gmd:purpose"];

        // find first present node from paths
        var nodeBeforeInsert = null;
        for (i=0; i<path.length; i++) {
            // get the last occurrence of this path if any
            nodeBeforeInsert = DOM.getElement(dataIdentification, path[i]+"[last()]");
            if (nodeBeforeInsert) { break; }
        }

        // write keys of thesaurus codelist
        var keywords;
        var keywordsParent;
        if (nodeBeforeInsert) {
            keywordsParent = nodeBeforeInsert.addElementAsSibling("gmd:descriptiveKeywords");
        } else {
            keywordsParent = dataIdentification.addElement("gmd:descriptiveKeywords");
        }
        keywords = keywordsParent.addElement("gmd:MD_Keywords");
        keywords.addElement("gmd:keyword/gco:CharacterString").addText("hmbtg");
    }
}


var columnName = 'informationHmbTG'; // the column of the table to get the value from
var contentLabel = SQL.all("SELECT add2.data, add2.list_item_id FROM additional_field_data add1, additional_field_data add2 WHERE add1.obj_id=? AND add1.field_key=? AND add2.parent_field_id=add1.id AND add2.field_key=?", [+id, "Informationsgegenstand", columnName]);

if ( contentLabel && contentLabel.size() > 0) {
    var i;
    var dataMetadata = DOM.getElement(idfDoc, "//idf:idfMdMetadata/gmd:identificationInfo");
    var dataIdentification;
    if (objClass == "3") {
        dataIdentification = DOM.getElement(idfDoc, "//idf:idfMdMetadata/gmd:identificationInfo/srv:SV_ServiceIdentification");
        if (!dataIdentification) dataIdentification = dataMetadata.addElement("srv:SV_ServiceIdentification");
    } else {
        dataIdentification = DOM.getElement(idfDoc, "//idf:idfMdMetadata/gmd:identificationInfo/gmd:MD_DataIdentification");
        if (!dataIdentification) dataIdentification = dataMetadata.addElement("gmd:MD_DataIdentification");
    }

    var path = ["gmd:resourceFormat", "gmd:graphicOverview", "gmd:resourceMaintenance","gmd:pointOfContact", "gmd:status","gmd:credit","gmd:purpose"];

    // find first present node from paths
    var nodeBeforeInsert = null;
    for (var i=0; i<path.length; i++) {
        // get the last occurrence of this path if any
        nodeBeforeInsert = DOM.getElement(dataIdentification, path[i]+"[last()]");
        if (nodeBeforeInsert) { break; }
    }

    // write keys of thesaurus codelist
    var keywords;
    var keywordsParent;
    if (nodeBeforeInsert) {
        keywordsParent = nodeBeforeInsert.addElementAsSibling("gmd:descriptiveKeywords");
    } else {
        keywordsParent = dataIdentification.addElement("gmd:descriptiveKeywords");
    }
    keywords = keywordsParent.addElement("gmd:MD_Keywords");

    for (i=0; i<contentLabel.size(); i++) {
        keywords.addElement("gmd:keyword/gco:CharacterString").addText(contentLabel.get(i).get("list_item_id"));
    }

    keywords.addElement("gmd:type/gmd:MD_KeywordTypeCode")
        .addAttribute("codeList", "http://www.tc211.org/ISO19139/resources/codeList.xml#MD_KeywordTypeCode")
        .addAttribute("codeListValue", "theme");

    var thesCit = keywords.addElement("gmd:thesaurusName/gmd:CI_Citation");
    thesCit.addElement("gmd:title/gco:CharacterString").addText("HmbTG-Informationsgegenstand");
    var thesCitDate = thesCit.addElement("gmd:date/gmd:CI_Date");
    thesCitDate.addElement("gmd:date/gco:Date").addText("2013-08-02");
    thesCitDate.addElement("gmd:dateType/gmd:CI_DateTypeCode")
        .addAttribute("codeListValue", "publication")
        .addAttribute("codeList", "http://www.isotc211.org/2005/resources/codeList.xml#CI_DateTypeCode");

    // write values of all keywords (no relation to thesaurus anymore!)
    keywords = keywordsParent.addElementAsSibling("gmd:descriptiveKeywords/gmd:MD_Keywords");
    for (i=0; i<contentLabel.size(); i++) {
        keywords.addElement("gmd:keyword/gco:CharacterString").addText(contentLabel.get(i).get("data"));
    }
}

var objRows = SQL.all("SELECT * FROM t01_object WHERE id=?", [+id]);
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
                        log.debug('Add external map url to idf by object_reference.');
                        var mdCrossRef = DOM.getElement(idfDoc, "//idf:idfMdMetadata/idf:crossReference[@uuid='" + objFromIdUuid + "']");
                        if(hasValue(mdCrossRef)) {
                            var tmpMapUrl = addCapabilitiesUrl(capabilitiesUrls.get(l), objFromIdUuid, publishId);
                            mdCrossRef.addElement("idf:mapUrl").addText(tmpMapUrl);
                            break;
                        }
                    }
                }
            }
        }
    }
    if (!mapUrl) {
        var rows = SQL.all("SELECT * FROM t011_obj_serv WHERE obj_id=?", [+id]);
        for (j=0; j<rows.size(); j++) {
            var objServId = rows.get(j).get("id");
            var subRows = SQL.all("SELECT * FROM t011_obj_serv_operation WHERE obj_serv_id=?", [+objServId]);
            for (k=0; k<subRows.size(); k++) {
                var objServOpId   = subRows.get(k).get("id");
                var isCapabilityOperation = rows.get(j).get("type_key") == "2" && subRows.get(k).get("name_key") == "1"; // key of "Darstellungsdienste" is "2" and "GetCapabilities" is "1" !

                // ---------- t011_obj_serv_op_connpoint ----------
                var subSubRows = SQL.all("SELECT * FROM t011_obj_serv_op_connpoint WHERE obj_serv_op_id=?", [+objServOpId]);
                for (l=0; l<subSubRows.size(); l++) {
                    if(isCapabilityOperation) {
                        mapUrl = addCapabilitiesUrl(subSubRows.get(l), objUuid, publishId);
                        if(hasValue(mapUrl)) {
                            log.debug('Add external map url to idf by t011_obj_serv_op_connpoint.');
                            break;
                        }
                    }
                }
            }
        }
    }
    if(hasValue(mapUrl)){
        log.debug('Add external map url to idf: ' + mapUrl);
        var mdMetadata = DOM.getElement(idfDoc, "//idf:idfMdMetadata");
        mdMetadata.addElement("idf:mapUrl").addText(mapUrl);
    }    
}

function addCapabilitiesUrl(row, objUuid, publishId) {
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