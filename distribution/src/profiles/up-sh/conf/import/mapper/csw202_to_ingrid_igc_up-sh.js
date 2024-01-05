/*
 * **************************************************-
 * Ingrid Portal MDEK Application
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

var mappingDescriptionSHUP = {"mappings":[
        {
            "execute": {
                "funct": mapGeometryContext
            }
        }
    ]};

if (log.isDebugEnabled()) {
    log.debug("SH-UP: mapping CSW 2.0.2 AP ISO 1.0 document to IGC import document.");
}

mapToTarget(mappingDescriptionSHUP, source, target.getDocumentElement());

function mapGeometryContext(source, target) {

    var spatialRepInfos = XPATH.getNodeList(source, "//gmd:spatialRepresentationInfo");
    var contextRow = 1;

    if (hasValue(spatialRepInfos)) {
        for (var i=0; i < spatialRepInfos.getLength(); i++) {
            var item = spatialRepInfos.item(i);
            var context = XPATH.getNode(item, "igctx:MD_GeometryContext");

            if (!hasValue(context)) {
                continue;
            }

            var additionalTarget = getOrCreateAdditionalFields(target);
            var geometryType = XPATH.getString(context, "igctx:geometryType/gco:CharacterString");
            var features = XPATH.getNodeList(context, "igctx:geometricFeature");

            for (var j=0; j < features.getLength(); j++) {

                var feature = features.item(j);
                var featureType = getFeatureType(feature);

                switch (featureType) {
                    case "NominalFeature": handleNominalFeature(additionalTarget, contextRow, feature, geometryType); break;
                    case "OrdinalFeature": handleOrdinalFeature(additionalTarget, contextRow, feature, geometryType); break;
                    case "ScalarFeature": handleScalarFeature(additionalTarget, contextRow, feature, geometryType); break;
                    case "OtherFeature": handleOtherFeature(additionalTarget, contextRow, feature, geometryType); break;
                    default: log.warn("Could not recognize Feature Type: " + featureType); contextRow--;
                }

                contextRow++;

            }
        }
    }

}

function getFeatureType(featureNode) {

    var children = featureNode.getChildNodes();
    for (var i=0; i < children.getLength(); i++) {
        var child = children.item(i);
        if (child.getNodeType() === Node.ELEMENT_NODE) {
            return child.getLocalName();
        }
    }

    return null;

}

function handleNominalFeature(target, row, feature, geometryType) {

    addGeneralGeometryContextValues(target, row, feature, geometryType);
    addAdditionalValue(target, row, "featureType", { key: "nominal", value: "nominal"}, "geometryContext");

}

function handleOrdinalFeature(target, row, feature, geometryType) {

    addGeneralGeometryContextValues(target, row, feature, geometryType);
    addAdditionalValue(target, row, "featureType", { key: "ordinal", value: "ordinal"}, "geometryContext");
    addAdditionalValue(target, row, "min", getGeometryContextString(feature, "minValue"), "geometryContext");
    addAdditionalValue(target, row, "max", getGeometryContextString(feature, "maxValue"), "geometryContext");

}

function handleScalarFeature(target, row, feature, geometryType) {

    addGeneralGeometryContextValues(target, row, feature, geometryType);
    addAdditionalValue(target, row, "featureType", { key: "scalar", value: "skalar"}, "geometryContext");
    addAdditionalValue(target, row, "min", getGeometryContextString(feature, "minValue"), "geometryContext");
    addAdditionalValue(target, row, "max", getGeometryContextString(feature, "maxValue"), "geometryContext");
    addAdditionalValue(target, row, "unit", getGeometryContextString(feature, "units"), "geometryContext");

}

function handleOtherFeature(target, row, feature, geometryType) {

    addGeneralGeometryContextValues(target, row, feature, geometryType);
    addAdditionalValue(target, row, "featureType", { key: "other", value: "sonstiges"}, "geometryContext");

}

function getGeometryContextString(node, field) {

    var string = XPATH.getString(node, ".//igctx:" + field + "/gco:CharacterString");
    return string ? string.trim() : string;
}

function addGeneralGeometryContextValues(target, row, feature, geometryType) {

    var featureName = getGeometryContextString(feature, "featureName");
    var featureDescription = getGeometryContextString(feature, "featureDescription");
    var featureDataType = getGeometryContextString(feature, "featureDataType");
    var attributes = getAttributes(feature);

    addAdditionalValue(target, row, "name", featureName, "geometryContext");
    addAdditionalValue(target, row, "description", featureDescription, "geometryContext");
    addAdditionalValue(target, row, "geometryType", geometryType, "geometryContext");
    addAdditionalValue(target, row, "dataType", featureDataType, "geometryContext");
    addAdditionalValue(target, row, "attributes", JSON.stringify(attributes), "geometryContext");

}

function getOrCreateAdditionalFields(target) {
    var addValuesNode = XPATH.getNode(target, "/igc/data-sources/data-source/data-source-instance/general/general-additional-values");
    if (!hasValue(addValuesNode)) {
        addValuesNode = XPATH.createElementFromXPath(target, "general-additional-values");
    }
    return addValuesNode;
}

function getAttributes(feature) {

    var attributes = [];
    var attributeNodeList = XPATH.getNodeList(feature, ".//igctx:featureAttributes/igctx:FeatureAttributes/igctx:attribute");

    for (var i=0; i < attributeNodeList.getLength(); i++) {
        var attribute = attributeNodeList.item(i);
        var description = XPATH.getString(attribute, ".//igctx:attributeDescription/gco:CharacterString");
        var code = XPATH.getString(attribute, ".//igctx:attributeCode/gco:CharacterString");
        if (!hasValue(code)) {
            code = XPATH.getString(attribute, ".//igctx:attributeContent/gco:CharacterString");
        }

        attributes.push({
            key: code ? code.trim() : code,
            value: description ? description.trim() : description
        });
    }
    return attributes;

}

function addAdditionalValue(target, line, key, data, parent) {

    var additionalValue = DOM.addElement(target, "general-additional-value");
    DOM.addAttribute(additionalValue.getElement(), "line", line);
    additionalValue.addElement("field-key").addText(key);
    additionalValue.addElement("field-key-parent").addText(parent);
    if (data instanceof Object) {
        additionalValue.addElement("field-data").addAttribute("id", data.key).addText(data.value);
    } else {
        additionalValue.addElement("field-data").addAttribute("id", "-1").addText(data);
    }

}
