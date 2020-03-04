/*
 * **************************************************-
 * Ingrid Portal MDEK Application
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
    var ProtocolHandler = Java.type("de.ingrid.mdek.job.protocol.ProtocolHandler");
}

var mappingDescriptionHzg = {
    "mappings": [
        {
            "execute": {
                "funct": mapObservedProperties
            }
        },
        {
            "execute": {
                "funct": mapPlatformReferences
            }
        },
        {
            "execute": {
                "funct": setFolderForImport
            }
        }
    ]
};

log.debug("HZG: mapping CSW 2.0.2 AP ISO 1.0 document to IGC import document.");
mapToTarget(mappingDescriptionHzg, source, target.getDocumentElement());

function mapObservedProperties(source, target) {
    log.debug("Mapping observed properties for HZG profile.");
    /*
     * Observed properties are mapped as featureCatalogueDescription elements.
     * HZG doesn't use this elements for any other purpose so delete them from
     * the previous mapping, which is common for all profiles.
     */
    var nodesToDelete = XPATH.getNodeList(target, "/igc/data-sources/data-source/data-source-instance/technical-domain/map/feature-type");
    for (var i=0; i<nodesToDelete.getLength(); i++) {
        var node = nodesToDelete.item(i);
        node.getParentNode().removeChild(node);
    }

    // Map the observed properties to the proper location
    var featureCatalogueNodes = XPATH.getNodeList(source, "//gmd:contentInfo/gmd:MD_FeatureCatalogueDescription");
    for (var i=0; i<featureCatalogueNodes.getLength(); i++) {
        var fcNode = featureCatalogueNodes.item(i);
        var rowCount = "" + (i+1);

        var obsPropNameNode = XPATH.getNode(fcNode, "gmd:featureCatalogueCitation/gmd:CI_Citation/gmd:title/gco:CharacterString");
        var obsPropDescNode = XPATH.getNode(fcNode, "gmd:featureTypes/gco:LocalName");

        var nameParent = XPATH.createElementFromXPathAsSibling(target, "/igc/data-sources/data-source/data-source-instance/general/general-additional-values/general-additional-value");
        XMLUtils.createOrReplaceAttribute(nameParent, "line", rowCount);

        var fieldKeyNode = XPATH.createElementFromXPath(nameParent, "field-key");
        XMLUtils.createOrReplaceTextNode(fieldKeyNode, "observedPropertyName");

        var fieldDataNode = XPATH.createElementFromXPath(nameParent, "field-data");
        XMLUtils.createOrReplaceAttribute(fieldDataNode, "id", "-1");
        XMLUtils.createOrReplaceTextNode(fieldDataNode, obsPropNameNode.getTextContent());

        var fieldKeyParentNode = XPATH.createElementFromXPath(nameParent, "field-key-parent");
        XMLUtils.createOrReplaceTextNode(fieldKeyParentNode, "observedPropertiesDataGrid");

        var descriptionParent = XPATH.createElementFromXPathAsSibling(target, "/igc/data-sources/data-source/data-source-instance/general/general-additional-values/general-additional-value");
        XMLUtils.createOrReplaceAttribute(descriptionParent, "line", rowCount);

        fieldKeyNode = XPATH.createElementFromXPath(descriptionParent, "field-key");
        XMLUtils.createOrReplaceTextNode(fieldKeyNode, "observedPropertyXmlDescription");

        fieldDataNode = XPATH.createElementFromXPath(descriptionParent, "field-data");
        XMLUtils.createOrReplaceAttribute(fieldDataNode, "id", "-1");
        XMLUtils.createOrReplaceTextNode(fieldDataNode, obsPropDescNode.getTextContent());

        fieldKeyParentNode = XPATH.createElementFromXPath(descriptionParent, "field-key-parent");
        XMLUtils.createOrReplaceTextNode(fieldKeyParentNode, "observedPropertiesDataGrid");
    }

}

function mapPlatformReferences(source, target) {
    log.debug("Mapping platform references for HZG profile");

    var xpath = "//gmd:MD_DataIdentification/gmd:aggregationInfo/gmd:MD_AggregateInformation[./gmd:associationType/gmd:DS_AssociationTypeCode/@codeListValue='crossReference']";
    var aggregationInfoNodes = XPATH.getNodeList(source, xpath);
    for(var i=0; i<aggregationInfoNodes.getLength(); i++) {
        var aggInfoNode = aggregationInfoNodes.item(i);
        var uuidNode = XPATH.getNode(aggInfoNode, "gmd:aggregateDataSetIdentifier/gmd:MD_Identifier/gmd:code/gco:CharacterString");
        var uuid = uuidNode.getTextContent();

        log.debug("Found Platform reference. Adding reference for platform with UUID: " + uuid);

        var dataSourceParent = XPATH.createElementFromXPathAsSibling(target, "/igc/data-sources/data-source/data-source-instance/link-data-source");

        var linkTypeNode = XPATH.createElementFromXPath(dataSourceParent, "object-link-type");
        XMLUtils.createOrReplaceAttribute(linkTypeNode, "id", "8001");

        var linkIdentifierNode = XPATH.createElementFromXPath(dataSourceParent, "object-identifier");
        XMLUtils.createOrReplaceTextNode(linkIdentifierNode, uuid);
    }
}

function setFolderForImport(source, target) {
    // Use first two characters of the file identifier as folder name
    var fileIdentifier = XPATH.getString(source, "/gmd:MD_Metadata/gmd:fileIdentifier/gco:CharacterString");
    var folderName = fileIdentifier.substring(0, 2).toLowerCase();
    log.debug("Found file identifier: " + fileIdentifier + ". Using folder name: " + folderName);

    // Get the uuid of the catalogue adminsistrator
    var row = SQL.first("SELECT addr_uuid FROM idc_user WHERE parent_id IS NULL");
    var userUuid = row.get("addr_uuid");
    log.debug("Found UUID of catalogue admin: " + userUuid);

    var folderUuid = igeCswFolderUtil.getParentUuidForCswTImportFolder(folderName, userUuid);

    var xpath = "/igc/data-sources/data-source/data-source-instance/parent-data-source/object-identifier";
    if (XPATH.nodeExists(source, xpath)) {
        var node = XPATH.getNode(source, xpath);
        node.getParentNode().removeChild(node);
    }
    var parentObjectNode = XPATH.createElementFromXPath(target, xpath);
    parentObjectNode.setTextContent(folderUuid);
}

