/*
 * **************************************************-
 * InGrid-iPlug DSC
 * ==================================================
 * Copyright (C) 2014 - 2020 wemove digital solutions GmbH
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

if (log.isDebugEnabled()) {
    log.debug("Nokis: Mapping source record to idf document: " + sourceRecord.toString());
}

if (!(sourceRecord instanceof DatabaseSourceRecord)) {
    throw new IllegalArgumentException("Record is no DatabaseRecord!");
}

DOM.addNS("igctx", "https://www.ingrid-oss.eu/schemas/igctx");

//---------- <idf:idfMdMetadata> ----------
var body = DOM.getElement(idfDoc, "/idf:html/idf:body/idf:idfMdMetadata");
body.addAttribute("xmlns:igctx", DOM.getNS("igctx"));
var oldSchemaLocation = body.getElement().getAttributes().getNamedItem("xsi:schemaLocation").getNodeValue();
body.addAttribute("xsi:schemaLocation", oldSchemaLocation + " https://www.ingrid-oss.eu/schemas/igctx https://www.ingrid-oss.eu/schemas/igctx/igctx.xsd");

var nextSiblingForSpatialRepresentationInfo = searchNextRootSiblingTag(body, "gmd:spatialRepresentationInfo");

var objId = sourceRecord.get("id");

// objClass variable is from script before "igc_to_idf.js"
if (objClass.equals("1")) {
    removeCoupledServiceInformation();
}

function removeCoupledServiceInformation() {
    // get all data downloads from coupled services
    var rows = SQL.all("SELECT t01obj.obj_name, urlref.* FROM object_reference oref, t01_object t01obj, t011_obj_serv t011_object, t017_url_ref urlref WHERE obj_to_uuid=? AND oref.special_ref=3600 AND oref.obj_from_id=t01obj.id AND t01obj.obj_class=3 AND t01obj.work_state='V' AND urlref.obj_id=t01obj.id AND (urlref.special_ref=5066 OR urlref.special_ref=9990) AND t011_object.obj_id=t01obj.id AND (t011_object.type_key=3 OR t011_object.type_key=6)", [objUuid]);
    var dataDownloadsFromService = [];
    for (i=0; i<rows.size(); i++) {
        dataDownloadsFromService.push(rows.get(i).get("url_link"));
    }
    var linkages = XPATH.getNodeList(idfDoc, "//gmd:linkage/gmd:URL");
    for (var k = 0; k < linkages.length; k++) {
        var linkage = linkages.item(k);
        
        removeServiceDownloadURLs(linkage, dataDownloadsFromService);
        removeServiceGetCapabilitiesURL(linkage);
    }
}

function removeServiceDownloadURLs(linkage, dataDownloadsFromService) {
    var urlLink = XPATH.getString(linkage, ".");
    if (dataDownloadsFromService.indexOf(urlLink) !== -1) {
        var dataDownloadsFromService = XPATH.getNodeList(linkage, "../..//idf:attachedToField[@entry-id=9990]");
        for (var i = 0; i < dataDownloadsFromService.length; i++) {
            var item = dataDownloadsFromService.item(i);
            XPATH.removeElementAtXPath(item, "../../../..");
        }
    }
}

function removeServiceGetCapabilitiesURL(linkage) {
    var urlLink = XPATH.getString(linkage, ".");
    var name = XPATH.getString(linkage, "../../gmd:name/gco:CharacterString");
    if (hasValue(name) && name.indexOf("Dienst \"") === 0 && name.indexOf("(GetCapabilities)") !== -1) {
        XPATH.removeElementAtXPath(linkage, "../../../../..");
    }
}
