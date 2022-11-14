/*
 * **************************************************-
 * InGrid-iPlug DSC
 * ==================================================
 * Copyright (C) 2014 - 2022 wemove digital solutions GmbH
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
if (javaVersion.indexOf("1.8") === 0) {
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

var objId = +sourceRecord.get("id");

// objClass variable is from script before "igc_to_idf.js"
if (objClass.equals("1")) {
    removeCoupledServiceInformation();
    var hierarchyLevel = getAdditionalFieldFromObject(objId, "bawHierarchyLevelName", "data")
    if (hierarchyLevel === "Messdaten") {
        addMeasureData();
    }
} else if (objClass.equals("6")) {
    addSoftwareData();
}

function removeCoupledServiceInformation() {
    var linkages = XPATH.getNodeList(idfDoc, "//gmd:linkage/gmd:URL");
    for (var k = 0; k < linkages.length; k++) {
        var linkage = linkages.item(k);

        removeServiceGetCapabilitiesURL(linkage);
    }
}

function removeServiceGetCapabilitiesURL(linkage) {
    var name = XPATH.getString(linkage, "../../gmd:name/gco:CharacterString");
    if (hasValue(name) && name.indexOf("Dienst \"") === 0 && name.indexOf("(GetCapabilities)") !== -1) {
        XPATH.removeElementAtXPath(linkage, "../../../../..");
    }
}

function addMeasureData() {
    var dataIdent = XPATH.getNode(idfDoc, "//gmd:identificationInfo/gmd:MD_DataIdentification");
    var measureInfo = DOM.addElement(dataIdent, "measurementInfo");

    addItemsToDom(objId, measureInfo, "MeasurementMethod", "measuringMethod", ["measuringMethod"], ["measurementMethod"], false)
    measureInfo.addElement("spatialOrientation").addText(getAdditionalFieldFromObject(objId, "spatiality", "data"));
    measureInfo.addElement("MeasurementDepth")
        .addElement("depth").addText(getAdditionalFieldFromObject(objId, "measuringDepth", "data"))
        .getParent()
        .addElement("uom").addText(getAdditionalFieldFromObject(objId, "unitOfMeasurement", "data"))
        .getParent()
        .addElement("verticalCRS").addText(getAdditionalFieldFromObject(objId, "heightReferenceSystem", "data"));
    measureInfo.addElement("measurementFrequency").addText(getAdditionalFieldFromObject(objId, "measuringFrequency", "data"));
    addItemsToDom(objId, measureInfo, "MeanWaterLevel", "averageWaterLevel", ["waterLevel", "unitOfMeasurement"], ["waterLevel", "uom"]);
    addItemsToDom(objId, measureInfo, "GaugeDatum", "zeroLevel", ["zeroLevel", "unitOfMeasurement", "verticalCoordinateReferenceSystem", "description"], ["datum", "uom", "verticalCRS", "description"]);
    measureInfo.addElement("minDischarge").addText(getAdditionalFieldFromObject(objId, "drainMin", "data"));
    measureInfo.addElement("maxDischarge").addText(getAdditionalFieldFromObject(objId, "drainMax", "data"));
    addItemsToDom(objId, measureInfo, "MeasurementDevice", "gauge", ["name", "id", "model", "description"], ["name", "id", "model", "description"]);
    addItemsToDom(objId, measureInfo, "MeasuredQuantities", "targetParameters", ["name", "type", "unitOfMeasurement", "formula"], ["name", "type", "uom", "calculationFormula"], false, [null, 3950014, null, null]);
    measureInfo.addElement("dataQualityDescription").addText(getAdditionalFieldFromObject(objId, "dataQualityDescription", "data"));

}

function convertToISODate(date) {
    // add UTC to date to extract the correct date without timezone issues
    var isoDate = new Date(date.split('.').reverse() + " UTC");
    return isoDate.toISOString().split('T')[0];
}

function addSoftwareData() {
    var dataIdent = XPATH.getNode(idfDoc, "//gmd:identificationInfo/gmd:MD_DataIdentification");
    var software = DOM.addElement(dataIdent, "software");

    software.addElement("einsatzzweck").addText(getAdditionalFieldFromObject(objId, "purpose", "data"));
    var nutzerkreis = software.addElement("Nutzerkreis");
    nutzerkreis.addElement("baw").addElement("gco:Boolean").addText(getAdditionalFieldFromObject(objId, "userGroupBAW", "data"));
    nutzerkreis.addElement("wsv").addElement("gco:Boolean").addText(getAdditionalFieldFromObject(objId, "userGroupWSV", "data"));
    nutzerkreis.addElement("extern").addElement("gco:Boolean").addText(getAdditionalFieldFromObject(objId, "userGroupExtern", "data"));
    nutzerkreis.addElement("anmerkungen").addText(getAdditionalFieldFromObject(objId, "userGroupNotes", "data"));

    var produktiverEinsatz = software.addElement("ProduktiverEinsatz");
    produktiverEinsatz.addElement("wsvAuftrag").addElement("gco:Boolean").addText(getAdditionalFieldFromObject(objId, "productiveUseWSVContract", "data"));
    produktiverEinsatz.addElement("fUndE").addElement("gco:Boolean").addText(getAdditionalFieldFromObject(objId, "productiveUseFuE", "data"));
    produktiverEinsatz.addElement("andere").addElement("gco:Boolean").addText(getAdditionalFieldFromObject(objId, "productiveUseOther", "data"));
    produktiverEinsatz.addElement("anmerkungen").addText(getAdditionalFieldFromObject(objId, "productiveUseNotes", "data"));

    var versionsFromDB = SQL.all("SELECT t011_version.version_value FROM t01_object t01, t011_obj_serv t011, t011_obj_serv_version t011_version WHERE t01.id=? AND t011.obj_id = t01.id AND t011_version.obj_serv_id = t011.id", [+objId]);
    if (versionsFromDB.length > 0) {
        for (var i=0; i<versionsFromDB.size(); i++) {
            var version = software.addElement("Version");
            var versionItem = versionsFromDB.get(i);
            version.addElement("version").addText(versionItem.get("version_value"));
        }
    }    

    var ergaenzungsModul = software.addElement("ErgaenzungsModul");
    ergaenzungsModul.addElement("ergaenzungsModul").addElement("gco:Boolean").addText(getAdditionalFieldFromObject(objId, "hasSupplementaryModule", "data"));
    ergaenzungsModul.addElement("ergaenzteSoftware").addText(getAdditionalFieldFromObject(objId, "nameOfSoftware", "data"));

    var betriebssystem = software.addElement("Betriebssystem");
    betriebssystem.addElement("windows").addElement("gco:Boolean").addText(getAdditionalFieldFromObject(objId, "operatingSystemWindows", "data"));
    betriebssystem.addElement("linux").addElement("gco:Boolean").addText(getAdditionalFieldFromObject(objId, "operatingSystemLinux", "data"));
    betriebssystem.addElement("anmerkungen").addText(getAdditionalFieldFromObject(objId, "operatingSystemNotes", "data"));

    // var language = software.addElement("Programmiersprache");
    addItemsToDom(objId, software, "Programmiersprache", "programmingLanguage", ["programmingLanguage"], ["programmiersprache"], false);
    addItemsToDom(objId, software, "Entwicklungsumgebung", "developmentEnvironment", ["developmentEnvironment"], ["entwicklungsumgebung"], false);
    addItemsToDom(objId, software, "Bibliotheken", "libraries", ["library"], ["library"], false);
    software.addElement("Erstellungsvertrag")
        .addElement("vertragsNummer").addText(getAdditionalFieldFromObject(objId, "creationContractNumber", "data"))
        .getParent()
        .addElement("datum").addText(convertToISODate(getAdditionalFieldFromObject(objId, "creationContractDate", "data")));

    software.addElement("Supportvertrag")
        .addElement("vertragsNummer").addText(getAdditionalFieldFromObject(objId, "supportContractNumber", "data"))
        .getParent()
        .addElement("datum").addText(convertToISODate(getAdditionalFieldFromObject(objId, "supportContractDate", "data")))
        .getParent()
        .addElement("anmerkungen").addText(getAdditionalFieldFromObject(objId, "supportContractNotes", "data"));

    var installationsort = software.addElement("Installationsort");
    installationsort.addElement("lokal").addElement("gco:Boolean").addText(getAdditionalFieldFromObject(objId, "installationLocal", "data"));
    var hlr = installationsort.addElement("HLR");
    hlr.addElement("hlr").addElement("gco:Boolean").addText(getAdditionalFieldFromObject(objId, "installationHLR", "data"));
    addItemsToDom(objId, hlr, "hlrName", "installationServerNames", ["text"], ["text"], false);

    // installationsort.addElement("Server").addText(getAdditionalFieldFromObject(objId, "operatingSystemNotes", "data"));
    var server = installationsort.addElement("Server");
    server.addElement("server").addElement("gco:Boolean").addText(getAdditionalFieldFromObject(objId, "installationServer", "data"));
    addItemsToDom(objId, server, "servername", "installationServerNames", ["text"], ["text"], false);
    software.addElement("installationsMethode").addText(getAdditionalFieldFromObject(objId, "installBy", "data"));

    var bawRechte = software.addElement("QuellCodeRechte");
    bawRechte.addElement("baw").addElement("gco:Boolean").addText(getAdditionalFieldFromObject(objId, "hasSourceRights", "data"));
    bawRechte.addElement("anmerkungen").addText(getAdditionalFieldFromObject(objId, "sourceRightsNotes", "data"));

    var subunternehmensrechte = software.addElement("NutzungsRechte");
    subunternehmensrechte.addElement("dritte").addElement("gco:Boolean").addText(getAdditionalFieldFromObject(objId, "hasUsageRights", "data"));
    subunternehmensrechte.addElement("anmerkungen").addText(getAdditionalFieldFromObject(objId, "usageRightsNotes", "data"));

}

/**
 * 
 * @param objId
 * @param targetElement
 * @param domElementId
 * @param tableId
 * @param columnIds
 * @param domColumnIds
 * @param {boolean} [mapDirectly] this is not used anymore!
 * @param {number[]} [mapCodelistColumn]
 */
function addItemsToDom(objId, targetElement, domElementId, tableId, columnIds, domColumnIds, mapDirectly, mapCodelistColumn) {
    var columns = [];
    for (var i = 0; i < columnIds.length; i++) {
        columns.push(getValuesFromTable(objId, tableId, columnIds[i]));
    }
    for (var i = 0; i < columns[0].length; i++) {
        var element = DOM.createElement(domElementId);
        for (var j = 0; j < columnIds.length; j++) {
            
            var value = columns[j][i];
            if (mapCodelistColumn && mapCodelistColumn[j]) value = TRANSF.getISOCodeListEntryFromIGCSyslistEntry(mapCodelistColumn[j], value)
            if (mapDirectly) {
                element.addText(value);
            } else {
                element.addElement(domColumnIds[j]).addText(value);
            }
        }
        targetElement.addElement(element)
    }
}

function getValuesFromTable(objId, tableId, columnId) {
    var table = getAdditionalForTable(objId, tableId)
    var result = [];
    for (var i = 0; i < table.length; i++) {
        if (table[i][columnId]) {
            var value = table[i][columnId].data;
            if (!value && table[i][columnId].listId !== "-1") value = table[i][columnId].listId;
            log.info("COLUMN VALUE: " + JSON.stringify(table[i][columnId]) + " using value: " + value);
            result.push(value);
        } else {
            result.push("");
        }
    }

    return result
}

function getAdditionalFieldFromObject(objId, fieldId, property) {
    var field = SQL.first("SELECT * FROM additional_field_data WHERE obj_id=? AND field_key=?", [objId, fieldId]);
    if (hasValue(field)) {
        var value= field.get(property);
        if(hasValue(value) && !value.equals("NaN")){
          return value;
        }
     }
     return null;
}