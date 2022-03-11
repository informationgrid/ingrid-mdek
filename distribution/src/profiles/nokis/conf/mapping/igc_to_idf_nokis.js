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

handleGeoContext();
addMetadataExtension();
addNokisThesaurus();
// objClass variable is from script before "igc_to_idf.js"
if (objClass.equals("1")) {
    removeCoupledServiceInformation();
}

function handleGeoContext() {

    var content = getAdditionalForTable(objId, 'geometryContext');

    if (hasValue(content)) {
        for (var i = 0; i < content.length; i++) {
            handleGeoContextRow(content[i]);
        }
    }
}

function addMetadataExtension() {

    var nextSiblingForMetadataExtensionInfo = searchNextRootSiblingTag(body, "gmd:metadataExtensionInfo");
    var element;
    if (nextSiblingForMetadataExtensionInfo) {
        element = nextSiblingForMetadataExtensionInfo.addElementAsSibling("gmd:metadataExtensionInfo");
    } else {
        element = body.addElement("gmd:metadataExtensionInfo");
    }

    element.addAttribute("xlink:href", "https://www.ingrid-oss.eu/schemas/igctx/igctx.xsd");

}

function handleGeoContextRow(row) {

    switch (row.featureType.listId) {
        case "nominal": return handleGeoContextNominal(row);
        case "ordinal": return handleGeoContextOrdinal(row);
        case "scalar": return handleGeoContextScalar(row);
        case "other": return handleGeoContextOther(row);
    }

}

function handleGeoContextNominal(row) {

    var featureEl = addGeneralGeometryContext(row, "igctx:NominalFeature");
    addAttributesNotForOther(featureEl, row);

}

function handleGeoContextOrdinal(row) {

    var featureEl = addGeneralGeometryContext(row, "igctx:OrdinalFeature");
    addAttributesNotForOther(featureEl, row);

    addMinMaxUnitGeometryContext(featureEl, row, {
        min: row.min ? row.min.data : null,
        max: row.max ? row.max.data : null
    });
}

function handleGeoContextScalar(row) {

    var featureEl = addGeneralGeometryContext(row, "igctx:ScalarFeature");
    addAttributesNotForOther(featureEl, row);

    addMinMaxUnitGeometryContext(featureEl, row, {
        min: row.min ? row.min.data : null,
        max: row.max ? row.max.data : null,
        unit: row.unit ? row.unit.data : null
    });

}
function handleGeoContextOther(row) {

    var featureEl = addGeneralGeometryContext(row, "igctx:OtherFeature");
    addAttributesForOther(featureEl, row);

    addMinMaxUnitGeometryContext(featureEl, row, {
        min: row.min ? row.min.data : null,
        max: row.max ? row.max.data : null
    });

}


function addGeneralGeometryContext(row, feature) {

    var geometryContext;
    if (nextSiblingForSpatialRepresentationInfo) {
        geometryContext = nextSiblingForSpatialRepresentationInfo.addElementAsSibling("gmd:spatialRepresentationInfo/igctx:MD_GeometryContext");
    } else {
        geometryContext = body.addElement("gmd:spatialRepresentationInfo/igctx:MD_GeometryContext");
    }

    geometryContext.addAttribute("gco:isoType", "AbstractMD_SpatialRepresentation_Type");

    geometryContext.addElement("igctx:geometryType")
        .addElement("gco:CharacterString")
        .addText(row.geometryType.data);

    var featureEl = geometryContext.addElement("igctx:geometricFeature/" + feature);

    featureEl.addElement("igctx:featureName")
        .addElement("gco:CharacterString")
        .addText(row.name.data);
    featureEl.addElement("igctx:featureDescription")
        .addElement("gco:CharacterString")
        .addText(row.description.data);
    featureEl.addElement("igctx:featureDataType")
        .addElement("gco:CharacterString")
        .addText(row.dataType.data);

    return featureEl;

}

function _addAttributes(featureEl, row, attributeType, attributeCodeOrContent) {

    var attributes = row.attributes;
    if (hasValue(attributes)) {
        var attr = JSON.parse(attributes.data);
        var attributesEl = featureEl.addElement("igctx:featureAttributes/igctx:FeatureAttributes");
        for (var i=0; i<attr.length; i++) {
            attributesEl.addElement("igctx:attribute/" + attributeType)
                .addElement("igctx:attributeDescription/gco:CharacterString").addText(attr[i].value)
                .getParent(2)
                .addElement(attributeCodeOrContent + "/gco:CharacterString").addText(attr[i].key);
        }
    }

}

function addAttributesNotForOther(featureEl, row) {

    _addAttributes(featureEl, row, "igctx:RegularFeatureAttribute", "igctx:attributeCode");

}

function addAttributesForOther(featureEl, row) {

    _addAttributes(featureEl, row, "igctx:OtherFeatureAttribute", "igctx:attributeContent");

}

function addMinMaxUnitGeometryContext(featureEl, row, data) {

    if (data.min) {
        featureEl.addElement("igctx:minValue")
            .addElement("gco:CharacterString")
            .addText(data.min);
    }
    if (data.max) {
        featureEl.addElement("igctx:maxValue")
            .addElement("gco:CharacterString")
            .addText(data.max);
    }
    if (data.unit) {
        featureEl.addElement("igctx:units")
            .addElement("gco:CharacterString")
            .addText(data.unit);
    }

}

function getAdditionalFieldFromObject(objId, parentId, fieldId) {

    var field;
    if (objId) {
        field = SQL.first("SELECT * FROM additional_field_data WHERE obj_id=? AND field_key=?", [objId, fieldId]);
    } else {
        field = SQL.first("SELECT * FROM additional_field_data WHERE parent_field_id=? AND field_key=?", [parentId, fieldId]);
    }
    if (hasValue(field)) {
        return field;
    } else {
        return null;
    }

}

function addNokisThesaurus() {

    var content = getAdditionalForTable(objId, 'nokisThesaurus');
    if (hasValue(content)) {
        var mdKeywords = DOM.createElement("gmd:MD_Keywords");
        for (var i = 0; i < content.length; i++) {
            handleThesaurusItem(mdKeywords, content[i]);
        }
        var insertNode = searchNextRootSiblingTag(identificationInfo, "gmd:descriptiveKeywords", identificationInfoChildrenReverseOrder);
        insertNode.addElementAsSibling("gmd:descriptiveKeywords").addElement(mdKeywords);
    }

}

function handleThesaurusItem(keywordsElement, keywordItem) {
    var mdKeyword = keywordsElement.addElement("gmd:keyword");
    var name = TRANSF.getCodeListEntryFromIGCSyslistEntry(7200, keywordItem.name.data, "de");
    IDF_UTIL.addLocalizedCharacterstring(mdKeyword, name);
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
