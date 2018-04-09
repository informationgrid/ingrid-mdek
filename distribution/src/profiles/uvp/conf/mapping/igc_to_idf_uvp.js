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
    CAPABILITIES = Java.type('de.ingrid.utils.capabilities.CapabilitiesUtils');
}

importPackage(Packages.org.w3c.dom);
importPackage(Packages.de.ingrid.iplug.dsc.om);
importPackage(Packages.de.ingrid.mdek);


if (log.isDebugEnabled()) {
    log.debug("Mapping source record to idf document: " + sourceRecord.toString());
}

if (!(sourceRecord instanceof DatabaseSourceRecord)) {
    throw new IllegalArgumentException("Record is no DatabaseRecord!");
}
// ---------- Initialize ----------
var globalCodeListAttrURL = "http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/ML_gmxCodelists.xml";

// ---------- <idf:html> ----------
var idfHtml = XPATH.getNode(idfDoc, "/idf:html");

// ---------- <idf:body> ----------
var idfBody = XPATH.getNode(idfDoc, "/idf:html/idf:body");

// ---------- <idf:idfMdMetadata> ----------
var body = DOM.addElement(idfBody, "idf:idfMdMetadata");

// ========== t01_object ==========
var objId = sourceRecord.get("id");
var objRows = SQL.all("SELECT * FROM t01_object WHERE id=?", [objId]);
for (i=0; i<objRows.size(); i++) {
    var objRow = objRows.get(i);
    var objUuid = objRow.get("obj_uuid");
    var objClass = objRow.get("obj_class");
    var objParentUuid = null; // will be set below
    
    // local variables
    var row = null;
    var rows = null;
    var value = null;
    var elem = null;
/*
    // Example iterating all columns !
    var colNames = objRow.keySet().toArray();
    for (var i in colNames) {
        var colName = colNames[i];
        var colValue = objRow.get(colName);
    }
*/

// ---------- <id> ----------
    value = getFileIdentifier(objRow);
    if (hasValue(value)) {
        body.addElement("id").addText(value);
    }

// ---------- <parent_id> ----------
    // NOTICE: Has to be published ! Guaranteed by select of passed sourceRecord ! 
    rows = SQL.all("SELECT fk_obj_uuid FROM object_node WHERE obj_uuid=?", [objUuid]);
    // Should be only one row !
    objParentUuid = rows.get(0).get("fk_obj_uuid");
    if (hasValue(objParentUuid)) {
        body.addElement("parent_id").addText(objParentUuid);
    }
    
// ---------- <date> ----------
    if (hasValue(objRow.get("mod_time"))) {
        var isoDate = TRANSF.getISODateFromIGCDate(objRow.get("mod_time"));
        // do only return the date section, ignore the time part of the date
        // see CSW 2.0.2 AP ISO 1.0 (p.41)
        if (isoDate) {
            body.addElement("date").addText(getDate(isoDate));
        }
    }
    
// ---------- <name> ----------
    var obj_name = objRow.get("obj_name");
    if (hasValue(obj_name)) {
        body.addElement("name").addText(obj_name);
    }

// ---------- <descr> ----------
    var obj_descr = objRow.get("obj_descr");
    if (hasValue(obj_descr)) {
        body.addElement("descr").addText(obj_descr);
    }

// ---------- <spatialValue> ----------
    var uvpSpatialValueRow = SQL.first("SELECT * FROM additional_field_data WHERE obj_id=? AND field_key=?", [objId, 'uvp_spatialValue']);
    if (hasValue(uvpSpatialValueRow)) {
        var value = uvpSpatialValueRow.get("data");
        if (hasValue(value)) {
            body.addElement("spatialValue").addText(value);
        }
    }

// ---------- <uvpgs> ----------
    // UVP Codelist
    var behavioursValueRow = SQL.first("SELECT * FROM sys_generic_key WHERE key_name='BEHAVIOURS'");
    var codelist = '';
    if (hasValue(behavioursValueRow)){
        var behaviours = behavioursValueRow.get("value_string");
        if(hasValue(behaviours)){
            var behavioursJson = JSON.parse(behaviours);
            for(i in behavioursJson){
                var behaviour = behavioursJson[i];
                if(hasValue(behaviour)){
                    var behaviourId = behaviour.id;
                    if(hasValue(behaviourId)){
                        if(behaviourId.equals("uvpPhaseField")){
                            var behaviourParams = behaviour.params;
                            if(hasValue(behaviourParams)){
                                for(j in behaviourParams){
                                    var behaviourParam = behaviourParams[j];
                                    if(hasValue(behaviourParam)){
                                        var behaviourParamId = behaviourParam.id;
                                        if(behaviourParamId.equals("categoryCodelist")){
                                            var behaviourParamValue = behaviourParam.value;
                                            if(hasValue(behaviourParamValue)){
                                                codelist = behaviourParamValue;
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    if(!hasValue(codelist)){
        codelist = 9000;
    }
    var uvpgCategoriesValueRow = SQL.first("SELECT * FROM additional_field_data WHERE obj_id=? AND field_key=?", [objId, 'uvpgCategory']);
    if (hasValue(uvpgCategoriesValueRow)) {
        var id = uvpgCategoriesValueRow.get("id");
        if (hasValue(id)) {
            var uvpgCategories = body.addElement("uvpgs");
            var uvpgCategoryRows = SQL.all("SELECT * FROM additional_field_data WHERE parent_field_id=? AND field_key=?", [id, 'categoryId']);
            for (var i=0; i< uvpgCategoryRows.size(); i++) {
                var categoryId = uvpgCategoryRows.get(i).get("data");
                var uvpNo = TRANSF.getIGCSyslistEntryName(codelist, categoryId, "de");
                var uvpCat = TRANSF.getISOCodeListEntryData(codelist, uvpNo);
                var uvpgElem = uvpgCategories.addElement("uvpg");
                if(hasValue(uvpNo)){
                    uvpgElem.addText(uvpNo);
                }
                if(hasValue(uvpCat)){
                    var uvpCatJson = JSON.parse(uvpCat);
                    if(hasValue(uvpCatJson.cat)){
                        uvpgElem.addAttribute("category", uvpCatJson.cat);
                    }
                    if(hasValue(uvpCatJson.type)){
                        uvpgElem.addAttribute("type", uvpCatJson.type);
                    }
                }
            }
        }
    }

// ---------- <addresses> ----------
    var addressRows = SQL.all("SELECT t02_address.*, t012_obj_adr.type, t012_obj_adr.special_name FROM t012_obj_adr, t02_address WHERE t012_obj_adr.adr_uuid=t02_address.adr_uuid AND t02_address.work_state=? AND t012_obj_adr.obj_id=? AND (t012_obj_adr.special_ref IS NULL OR t012_obj_adr.special_ref=?) ORDER BY line", ['V', objId, '505']);
    var addresses = body.addElement("addresses");
    for (var i=0; i< addressRows.size(); i++) {
        var role = TRANSF.getISOCodeListEntryFromIGCSyslistEntry(505, addressRows.get(i).get("type"));
        if (!hasValue(role)) {
            role = addressRows.get(i).get("special_name");
        }
        if (hasValue(role)) {
            // address may be hidden ! then get first visible parent in hierarchy !
            var addressRow = getFirstVisibleAddress(addressRows.get(i).get("adr_uuid"));
            if (addressRow) {
                addresses.addElement(getIdfResponsibleParty(addressRow, role));
            }
        }
    }

// ---------- <steps> ----------
    var phasesRow = SQL.all("SELECT * FROM additional_field_data WHERE obj_id=? AND field_key=?", [objId, 'UVPPhases']);
    for (var i=0; i < phasesRow.size(); i++) {
        var value = phasesRow.get(i).get("id");
        if (hasValue(value)) {
            var phases = body.addElement("steps");
// ---------- <steps/step> ----------
            var phaseRow = SQL.all("SELECT * FROM additional_field_data WHERE parent_field_id=? ORDER BY sort", [value]);
            for (var j=0; j < phaseRow.size(); j++) {
                var phaseId = phaseRow.get(j).get("id");
                var phaseFieldKey = phaseRow.get(j).get("field_key");
                var phase = DOM.createElement("step").addAttribute("type", phaseFieldKey);
                if (hasValue(phaseId)){
                    var phaseContentRow = SQL.all("SELECT * FROM additional_field_data WHERE parent_field_id=? ORDER BY sort", [phaseId]);
                    var datePeriod;
                    var datePeriodFrom;
                    var datePeriodTo;

                    var startDate;
                    var today = (new Date()).getTime();
                    for (var k=0; k < phaseContentRow.size(); k++) {
                        var id = phaseContentRow.get(k).get("id");
                        var data = phaseContentRow.get(k).get("data");
                        var fieldKey = phaseContentRow.get(k).get("field_key");
                        if(phaseFieldKey == "phase1"){
                            if(fieldKey == "publicDateFrom"){
                                if (hasValue(data)){
                                    datePeriodFrom = TRANSF.getISODateFromMilliseconds(data);
                                    startDate = data;
                                    if(!datePeriod){
                                        datePeriod = DOM.createElement("datePeriod")
                                    }
                                    datePeriod.addElement("from").addText(datePeriodFrom);
                                } else {
                                    startDate = today;
                                }
                            }else if(fieldKey == "publicDateTo"){
                                if (hasValue(data)){
                                    datePeriodTo = TRANSF.getISODateFromMilliseconds(data);
                                    if(!datePeriod){
                                        datePeriod = DOM.createElement("datePeriod")
                                    }
                                    datePeriod.addElement("to").addText(datePeriodTo);
                                }
                                if(datePeriodFrom){
                                    phase.addElement(datePeriod);
                                    datePeriod = null;
                                    datePeriodFrom = null;
                                    datePeriodTo = null;
                                }
                            }else if(fieldKey == "technicalDocs"){
                                var fields = [{"id":"label", "type":"text"}, {"id":"link", "type":"link"}, {"id":"type", "type":"text"}, {"id":"size", "type":"bytes"}, {"id":"expires", "type":"text"}];
                                var table = phase.addElement("docs").addAttribute("type", fieldKey);
                                getAdditionalFieldDataTable(id, fields, table);
                            }else if(fieldKey == "applicationDocs"){
                                var publishLaterRow = SQL.first("SELECT * FROM additional_field_data WHERE field_key = 'applicationDocsPublishLater' AND parent_field_id=? ORDER BY sort", [phaseId]);
                                var shouldPublish = !(hasValue(publishLaterRow))
                                        || publishLaterRow.get("data") == "N"
                                        || (publishLaterRow.get("data") == "Y" && startDate <= today);
                                if (hasValue(publishLaterRow)) {
                                    log.debug(publishLaterRow.get("field_key") + " has value: " + publishLaterRow.get("data"));
                                    log.debug("Start Date: " + startDate);
                                    log.debug("today: " + today);
                                    log.debug("Date comparison: " + (startDate <= today));
                                }
                                if (shouldPublish) {
                                    var fields = [{"id":"label", "type":"text"}, {"id":"link", "type":"link"}, {"id":"type", "type":"text"}, {"id":"size", "type":"bytes"}, {"id":"expires", "type":"text"}];
                                    var table = phase.addElement("docs").addAttribute("type", fieldKey);
                                    getAdditionalFieldDataTable(id, fields, table);
                                }
                            }else if(fieldKey == "reportsRecommendationsDocs"){
                                var publishLaterRow = SQL.first("SELECT * FROM additional_field_data WHERE field_key = 'reportsRecommendationsDocsPublishLater' AND parent_field_id=? ORDER BY sort", [phaseId]);
                                var shouldPublish = !(hasValue(publishLaterRow))
                                        || publishLaterRow.get("data") == "N"
                                        || (publishLaterRow.get("data") == "Y" && startDate <= today);
                                if (hasValue(publishLaterRow)) {
                                    log.debug(publishLaterRow.get("field_key") + " has value: " + publishLaterRow.get("data"));
                                }
                                if (shouldPublish) {
                                    var fields = [{"id":"label", "type":"text"}, {"id":"link", "type":"link"}, {"id":"type", "type":"text"}, {"id":"size", "type":"bytes"}, {"id":"expires", "type":"text"}];
                                    var table = phase.addElement("docs").addAttribute("type", fieldKey);
                                    getAdditionalFieldDataTable(id, fields, table);
                                }
                            }else if(fieldKey == "moreDocs"){
                                var publishLaterRow = SQL.first("SELECT * FROM additional_field_data WHERE field_key = 'moreDocsPublishLater' AND parent_field_id=? ORDER BY sort", [phaseId]);
                                var shouldPublish = !(hasValue(publishLaterRow))
                                        || publishLaterRow.get("data") == "N"
                                        || (publishLaterRow.get("data") == "Y" && startDate <= today);
                                if (hasValue(publishLaterRow)) {
                                    log.debug(publishLaterRow.get("field_key") + " has value: " + publishLaterRow.get("data"));
                                }
                                if (shouldPublish) {
                                    var fields = [{"id":"label", "type":"text"}, {"id":"link", "type":"link"}, {"id":"type", "type":"text"}, {"id":"size", "type":"bytes"}, {"id":"expires", "type":"text"}];
                                    var table = phase.addElement("docs").addAttribute("type", fieldKey);
                                    getAdditionalFieldDataTable(id, fields, table);
                                }
                            }else if(fieldKey == "publicationDocs"){
                                var fields = [{"id":"label", "type":"text"}, {"id":"link", "type":"link"}, {"id":"type", "type":"text"}, {"id":"size", "type":"bytes"}, {"id":"expires", "type":"text"}];
                                var table = phase.addElement("docs").addAttribute("type", fieldKey);
                                getAdditionalFieldDataTable(id, fields, table);
                            }
                        } else if (phaseFieldKey == "phase2"){
                            if(fieldKey == "considerDateFrom"){
                                if (hasValue(data)){
                                    datePeriodFrom = TRANSF.getISODateFromMilliseconds(data);
                                    if(!datePeriod){
                                        datePeriod = DOM.createElement("datePeriod");
                                    }
                                    datePeriod.addElement("from").addText(datePeriodFrom);
                                }
                            }else if(fieldKey == "considerDateTo"){
                                if (hasValue(data)){
                                    datePeriodTo = TRANSF.getISODateFromMilliseconds(data);
                                    if(!datePeriod){
                                        datePeriod = DOM.createElement("datePeriod");
                                    }
                                    datePeriod.addElement("to").addText(datePeriodTo);
                                }
                                if(datePeriodFrom){
                                    phase.addElement(datePeriod);
                                    datePeriod = null;
                                    datePeriodFrom = null;
                                    datePeriodTo = null;
                                }
                            }else if(fieldKey == "considerationDocs"){
                                var fields = [{"id":"label", "type":"text"}, {"id":"link", "type":"link"}, {"id":"type", "type":"text"}, {"id":"size", "type":"bytes"}, {"id":"expires", "type":"text"}];
                                var table = phase.addElement("docs").addAttribute("type", fieldKey);
                                getAdditionalFieldDataTable(id, fields, table);
                            }
                        } else if (phaseFieldKey == "phase3"){
                            if(fieldKey == "approvalDate"){
                                if (hasValue(data)){
                                    phase.addElement("date/from").addText(TRANSF.getISODateFromMilliseconds(data));
                                }
                            }else if(fieldKey == "approvalDescription"){
                                if (hasValue(data)){
                                    phase.addElement(fieldKey).addText(data);
                                }
                            }else if(fieldKey == "approvalDocs"){
                                var fields = [{"id":"label", "type":"text"}, {"id":"link", "type":"link"}, {"id":"type", "type":"text"}, {"id":"size", "type":"bytes"}, {"id":"expires", "type":"text"}];
                                var table = phase.addElement("docs").addAttribute("type", fieldKey);
                                getAdditionalFieldDataTable(id, fields, table);
                            }else if(fieldKey == "designDocs"){
                                var fields = [{"id":"label", "type":"text"}, {"id":"link", "type":"link"}, {"id":"type", "type":"text"}, {"id":"size", "type":"bytes"}, {"id":"expires", "type":"text"}];
                                var table = phase.addElement("docs").addAttribute("type", fieldKey);
                                getAdditionalFieldDataTable(id, fields, table);
                            }
                        }
                    }
                }
                phases.addElement(phase);
            }
        }
    }
}

// Return gco:Date element containing only the date section, ignore the time part of the date
function getDate(dateValue) {
    if (dateValue.indexOf("T") > -1) {
        dateValue = dateValue.substring(0, dateValue.indexOf("T"));
    }
    return dateValue;
}

/**
 * Get the fileIdentifier. Try to use DB column "org_obj_id". If not found use column "obj_uuid".
 * 
 * @param objRow DB row representing a t01_object row.
 * @return
 */
function getFileIdentifier(objRow) {
    var fileIdentifier = objRow.get("org_obj_id");
    if (!hasValue(fileIdentifier)) {
        fileIdentifier = objRow.get("obj_uuid");
    }
    return fileIdentifier;
}

// Get published (or working version if flag passed) address with given uuid.
// If address is hidden then first visible parent in hierarchy is returned.
function getFirstVisibleAddress(addrUuid, useWorkingVersion) {
    var resultAddrRow;

    // ---------- address_node ----------
    var sqlQuery = "SELECT * FROM address_node WHERE addr_uuid=? AND ";
    var addrIdToFetch = "addr_id_published";
    if (useWorkingVersion) {
        if (log.isDebugEnabled()) {
            log.debug("Fetch working version of address !!! USER ADDRESS(?) uuid=" + addrUuid);
        }
        addrIdToFetch = "addr_id";
    }
    sqlQuery = sqlQuery + addrIdToFetch + " IS NOT NULL"
    var addrNodeRows = SQL.all(sqlQuery, [addrUuid]);
    for (k=0; k<addrNodeRows.size(); k++) {
        var parentAddrUuid = addrNodeRows.get(k).get("fk_addr_uuid");
        var addrId = addrNodeRows.get(k).get(addrIdToFetch);

        // ---------- t02_address ----------
        resultAddrRow = SQL.first("SELECT * FROM t02_address WHERE id=? and (hide_address IS NULL OR hide_address != 'Y')", [addrId]);
        if (!hasValue(resultAddrRow)) {
            if (log.isDebugEnabled()) {
                log.debug("Hidden address !!! uuid=" + addrUuid + " -> instead map parent address uuid=" + parentAddrUuid);
            }
            // address hidden, get parent !
            if (hasValue(parentAddrUuid)) {
                resultAddrRow = getFirstVisibleAddress(parentAddrUuid, useWorkingVersion);
            }
        }
    }
    
    return resultAddrRow;
}

/**
 * Creates an ISO CI_ResponsibleParty element based on a address row and a role. 
 * 
 * @param addressRow
 * @param role
 * @return
 */
function getIdfResponsibleParty(addressRow, role) {
    var parentAddressRowPathArray = getAddressRowPathArray(addressRow);
    var myElementName = "address";
    var address = DOM.createElement(myElementName)
        .addAttribute("id", addressRow.get("adr_uuid"))
        .addAttribute("type", addressRow.get("adr_type"));
    if (hasValue(addressRow.get("org_adr_id"))) {
        address.addAttribute("orig-uuid", addressRow.get("org_adr_id"));
    }

    // first extract communication values
    var communicationsRows = SQL.all("SELECT t021_communication.* FROM t021_communication WHERE t021_communication.adr_id=? order by line", [addressRow.get("id")]);
    var phones = new Array();
    var faxes = new Array;
    var emailAddresses = new Array();
    var emailAddressesToShow = new Array();
    var urls = new Array();
    for (var j=0; j< communicationsRows.size(); j++) {
        var communicationsRow = communicationsRows.get(j);
        var commTypeKey = communicationsRow.get("commtype_key");
        var commTypeValue = communicationsRow.get("commtype_value");
        var commValue = communicationsRow.get("comm_value");
        if (commTypeKey == 1) {
            phones.push(commValue);
        } else if (commTypeKey == 2) {
            faxes.push(commValue);
        } else if (commTypeKey == 3) {
            emailAddresses.push(commValue);
        } else if (commTypeKey == 4) {
            urls.push(commValue);

        // special values saved as free entries !
        } else if (commTypeKey == -1) {
            // users email to be shown instead of other emails !
            if (commTypeValue == "emailPointOfContact") {
                emailAddressesToShow.push(commValue);
            }
        }
    }
    if (emailAddressesToShow.length > 0) {
        emailAddresses = emailAddressesToShow;
    }
    
    var individualName = getIndividualNameFromAddressRow(addressRow, address);
    if (hasValue(individualName)){
        address.addElement("name").addText(individualName);
    }
    
    if (!hasValue(individualName)) {
        var institution = addressRow.get("institution");
        if (hasValue(institution)) {
            institution = filterUserPostfix(institution);
            address.addElement("name").addText(institution);
        }
    }

    if (hasValue(addressRow.get("job"))) {
        address.addElement("positionName").addText(addressRow.get("job"));
    }

    if (phones.length > 0 || faxes.length > 0) {
        for (var j=0; j<phones.length; j++) {
            address.addElement("phone").addText(phones[j]);
        }
        for (var j=0; j<faxes.length; j++) {
            address.addElement("fax").addText(faxes[j]);
        }
    }

    if (hasValue(addressRow.get("postbox")) || hasValue(addressRow.get("postbox_pc")) ||
            hasValue(addressRow.get("city")) || hasValue(addressRow.get("street"))) {
        if (hasValue(addressRow.get("postbox"))) {
            if(hasValue(addressRow.get("postbox_pc"))){
                address.addElement("postbox").addText("Postbox " + addressRow.get("postbox") + "," + addressRow.get("postbox_pc") + " " + addressRow.get("city"));
            }else if(hasValue(addressRow.get("postcode"))){
                address.addElement("postbox").addText("Postbox " + addressRow.get("postbox") + "," + addressRow.get("postcode") + " " + addressRow.get("city"));
            }else{
                address.addElement("postbox").addText("Postbox " + addressRow.get("postbox"));
            }
        }
        address.addElement("street").addText(addressRow.get("street"));
        address.addElement("city").addText(addressRow.get("city"));
        address.addElement("postalcode").addText(addressRow.get("postcode"));
    }

    if (hasValue(addressRow.get("country_value"))) {
        address.addElement("country").addText(addressRow.get("country_value"));
    }

    for (var j=0; j<emailAddresses.length; j++) {
        address.addElement("mail").addText(emailAddresses[j]);
    }

    if (urls.length > 0) {
        address.addElement("url").addText(urls[0]);
    }

    for (var j=0; j<parentAddressRowPathArray.length; j++) {
        address.addElement(getIdfAddressReference(parentAddressRowPathArray[j], "parent"));
    }

    return address;
}

/**
 * Removes all [...] from passed name, e.g. "[Nutzer]" was added when user addresses were migrated to hidden addresses. 
 */
function filterUserPostfix(name) {
    var filteredName = name;

    if (hasValue(name)) {
        // first make JS String out of name, so we call JS replace method !!!
        filteredName = ("" + name).replace(/ \[.*\]/g,"");
        // just for sure
        if (!hasValue(filteredName)) {
            filteredName = name;
        }

        if (log.isDebugEnabled()) {
            if (name.length != filteredName.length) {
                log.debug("Filtered name '" + name + "' to '" + filteredName + "' !");
            }
        }
    }

    return filteredName;
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

function getOrganisationNameFromAddressRow(addressRow) {
    var organisationName = "";

    if (hasValue(addressRow.get("institution"))) {
        organisationName = addressRow.get("institution");
    }

    return organisationName;
}

/**
 * Returns an array of address rows representing the complete path from 
 * the given address (first entry in array) to the farthest parent 
 * (last entry in array).
 * 
 * @param addressRow The database address ro to start from.
 * @return The array with all parent address rows.
 */
function getAddressRowPathArray(addressRow) {
    var results = new Array();
    if (log.isDebugEnabled()) {
        log.debug("Add address with uuid '" + addressRow.get("adr_uuid") + "' to address path:" + parentAdressRow);
    }
    var addrId = addressRow.get("id");
    var parentAdressRow = SQL.first("SELECT t02_address.* FROM t02_address, address_node WHERE address_node.addr_id_published=? AND address_node.fk_addr_uuid=t02_address.adr_uuid AND t02_address.work_state=?", [addrId, "V"]);
    while (hasValue(parentAdressRow)) {
        if (log.isDebugEnabled()) {
            log.debug("Add address with uuid '"+parentAdressRow.get("adr_uuid")+"' to address path:" + parentAdressRow);
        }
        results.push(parentAdressRow);
        addrId = parentAdressRow.get("id");
        parentAdressRow = SQL.first("SELECT t02_address.* FROM t02_address, address_node WHERE address_node.addr_id_published=? AND address_node.fk_addr_uuid=t02_address.adr_uuid AND t02_address.work_state=?", [addrId, "V"]);
    }
    return results;
}

function getIdfAddressReference(addrRow, elementName) {
    var idfAddressReference = DOM.createElement(elementName);
    idfAddressReference.addAttribute("id", addrRow.get("adr_uuid"));

    var person = getIndividualNameFromAddressRow(addrRow);
    if (hasValue(person)){
        idfAddressReference.addElement("name").addText(person);
    }

    var institution = getOrganisationNameFromAddressRow(addrRow);
    if (hasValue(institution)) {
        idfAddressReference.addElement("name").addText(institution);
    }

    return idfAddressReference;
}

function getAdditionalFieldDataTable(id, fields, table){
    var docs = SQL.all("SELECT * FROM additional_field_data WHERE parent_field_id=? ORDER BY sort", [id]);
    var sort = "";
    var doc;
    var expired = false;
    for (var r=0; r < docs.size(); r++) {
        var tableData = docs.get(r).get("data");
        var tableFieldKey = docs.get(r).get("field_key");
        var tableSort = docs.get(r).get("sort");
        if(sort != tableSort){
            if(doc){
                table.addElement(doc);
            }
            doc = DOM.createElement("doc");
            sort = tableSort;
        }

        for (var s = 0; s < fields.length; s++) {
            if(fields[s].id == tableFieldKey){
                var value = tableData;
                if(fields[s].type == "bytes"){
                    value = formatBytes(value);
                } else  if(fields[s].type == "link"){
                    // detect relative links from document uploads
                    // excluds (http|https|ftp)://
                    pos = value.indexOf("://");
                    if (pos <= 3 || pos >= 10) {
                        value = MdekServer.conf.profileUvpDocumentStoreBaseUrl+value;
                    }
                } else if (fields[s].id == "expires") {
                    if (value.length > 0) {
                        // check if date lies in past
                        var d = parseDate(value);
                        var now = new Date();
                        now.setHours(0,0,0,0);
                        if (d < now) {
                          expired = true;
                          break;
                        }                       
                    } 
                }
                
                doc.addElement(tableFieldKey).addText(value);
            }
        }

        if (expired) {
            break;
        } else if (doc != null && r >= docs.size() -1){
            table.addElement(doc);
        }
    }
}

//parse a date in dd.mm.yyyy format
function parseDate(input) {
  var parts = input.split('.');
  // new Date(year, month [, day [, hours[, minutes[, seconds[, ms]]]]])
  return new Date(parts[2], parts[1]-1, parts[0]); // Note: months are 0-based
}

function formatBytes (value) {
    if (parseInt(value) != value) {
        return value;
    }
    if (value == 0) {
        return '0 B';
    }
    var k = 1000,
        dm = 1,
        sizes = ['B', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'],
        i = Math.floor(Math.log(value) / Math.log(k));
    return parseFloat((value / Math.pow(k, i)).toFixed(dm)) + " " + sizes[i];
 }
