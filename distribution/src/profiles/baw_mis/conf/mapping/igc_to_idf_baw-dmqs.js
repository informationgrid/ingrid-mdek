/*
 * **************************************************-
 * InGrid-iPlug DSC
 * ==================================================
 * Copyright (C) 2014 - 2019 wemove digital solutions GmbH
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

log.debug("Additional BAW specific mapping from source record to idf document: " + sourceRecord.toString());

if (!(sourceRecord instanceof DatabaseSourceRecord)) {
    throw new IllegalArgumentException("Record is no DatabaseRecord!");
}

// ---------- <idf:body> ----------
var idfBody = XPATH.getNode(idfDoc, "/idf:html/idf:body");

// ---------- <idf:idfMdMetadata> ----------
var mdMetadata = XPATH.getNode(idfBody, "idf:idfMdMetadata");

// ========== t01_object ==========
// convert id to number to be used in PreparedStatement as Integer to avoid postgres error !
var objId = +sourceRecord.get("id");

var objRows = SQL.all("SELECT * FROM t01_object WHERE id=?", [+objId]);
for (i=0; i<objRows.size(); i++) {
    var objRow = objRows.get(i);

    // ---------- <gmd:contact> ----------

    // If no contact for metadata is defined then log an error
    if (!XPATH.nodeExists(mdMetadata, "gmd:contact")) {
    	log.error('No responsible party for metadata found!');
    }

    // ---------- <gmd:dateStamp> ----------
    // Use gco:DateTime instead of gco:Date for BAW
    // Remove node created by the global script
    var dateStamp = XPATH.getNode(mdMetadata, "gmd:dateStamp");

    /*
     * We already searched for the modified date in the global script. If none
     * was found there, then we aren't going to find it here as well. Skip the
     * database query if no XML-Node was created by the previous script.
     */
    if (hasValue(dateStamp)) {

        // Add the modified time again, this time as DateTime
        if (hasValue(objRow.get("mod_time"))) {
            var isoDate = TRANSF.getISODateFromIGCDate(objRow.get("mod_time"));
            if (isoDate.contains("T"))
            XPATH.removeElementAtXPath(dateStamp, "gco:Date");
            if (isoDate) {
                //DOM.addElement(mdMetadata, "gmd:dateStamp").addElement(getDateOrDateTime(isoDate));
                DOM.addElement(dateStamp, "gco:DateTime").addText(isoDate);
            }
        }
    }

    // add BAW specific Information
    var idxDoc = sourceRecord.get("idxDoc");

    var additionalDataSection = DOM.addElement(mdMetadata, "idf:additionalDataSection").addAttribute("id", "bawDmqsAdditionalFields");
    additionalDataSection.addElement("idf:title").addAttribute("lang", "de").addText("BAW DMQS Zusatzfelder");
    // bwstr-bwastr_name (Bundeswasserstraszen Name)
    var field = additionalDataSection.addElement("idf:additionalDataField").addAttribute("id", "bwstr-bwastr_name");
    field.addElement("idf:title").addAttribute("lang", "de").addText("Bwstr Name");
    field.addElement("idf:data").addText(idxDoc.get("bwstr-bwastr_name"));
    // bwstr-strecken_name (Streckenname des Abschnitts)
    field = additionalDataSection.addElement("idf:additionalDataField").addAttribute("id", "bwstr-strecken_name");
    field.addElement("idf:title").addAttribute("lang", "de").addText("Bwstr Streckenname");
    field.addElement("idf:data").addText(idxDoc.get("bwstr-strecken_name"));
    // bwstr-center-lon (Longitude des Zentrums des Abschnitts)
    field = additionalDataSection.addElement("idf:additionalDataField").addAttribute("id", "bwstr-center-lon");
    field.addElement("idf:title").addAttribute("lang", "de").addText("Longitude des Zentrums des Abschnitts");
    field.addElement("idf:data").addText(idxDoc.get("bwstr-center-lon"));
    // bwstr-center-lat (Latitude des Zentrums des Abschnitts)
    field = additionalDataSection.addElement("idf:additionalDataField").addAttribute("id", "bwstr-center-lat");
    field.addElement("idf:title").addAttribute("lang", "de").addText("Latitude des Zentrums des Abschnitts");
    field.addElement("idf:data").addText(idxDoc.get("bwstr-center-lat"));

}

