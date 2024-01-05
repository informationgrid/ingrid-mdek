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
    log.debug("SH-UP: Mapping source record to idf document: " + sourceRecord.toString());
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
