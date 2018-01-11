/*
 * **************************************************-
 * InGrid-iPlug DSC
 * ==================================================
 * Copyright (C) 2014 - 2017 wemove digital solutions GmbH
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

DOM.addNS("gmd", "http://www.isotc211.org/2005/gmd");
DOM.addNS("gco", "http://www.isotc211.org/2005/gco");
DOM.addNS("gml", "http://www.opengis.net/gml");
DOM.addNS("xlink", "http://www.w3.org/1999/xlink");

if (log.isDebugEnabled()) {
    log.debug("Mapping source record to lucene document: " + sourceRecord.toString());
}
if (!(sourceRecord instanceof Java.type("de.ingrid.iplug.dsc.om.DatabaseSourceRecord"))) {
    var IllegalArgumentException = Java.type("java.lang.IllegalArgumentException");
    var ex = new IllegalArgumentException("Record is no DatabaseRecord!");
}

var CODELIST_URI = "http://www.isotc211.org/2005/resources/codeList.xml";
var SERVICE_TYPE_CODE = "3"; // Geodatendienste haben einen object type code = 3
var DATA_COLUMN = "data";
var OBJ_CLASS_COLUMN = "obj_class";
var FIELD_KEY_COLUMN = "field_key";
var SORT_COLUMN = "sort";
var SERVICE_ID_XPATH = "//idf:idfMdMetadata/gmd:identificationInfo/srv:SV_ServiceIdentification";
var DATASET_ID_XPATH = "//idf:idfMdMetadata/gmd:identificationInfo/gmd:MD_DataIdentification";

var mdMetadataTags = [
    "gmd:fileIdentifier",
    "gmd:featureAttribute",
    "gmd:featureType",
    "gmd:propertyType",
    "gmd:describes",
    "gmd:series",
    "gmd:metadataMaintenance",
    "gmd:applicationSchemaInfo",
    "gmd:metadataConstraints",
    "gmd:portrayalCatalogueInfo",
    "gmd:dataQualityInfo",
    "gmd:distributionInfo",
    "gmd:contentInfo",
    "gmd:identificationInfo",
    "gmd:metadataExtensionInfo",
    "gmd:referenceSystemInfo",
    "gmd:spatialRepresentationInfo",
    "gmd:locale",
    "gmd:dataSetURI",
    "gmd:metadataStandardVersion",
    "gmd:metadataStandardName",
    "gmd:dateStamp",
    "gmd:contact",
    "gmd:hierarchyLevelName",
    "gmd:hierarchyLevel",
    "gmd:parentIdentifier",
    "gmd:characterSet",
    "gmd:language"
];

var idInfoTags = [
    "gmd:aggregationInfo",
    "gmd:resourceConstraints",
    "gmd:resourceSpecificUsage",
    "gmd:descriptiveKeywords",
    "gmd:resourceFormat",
    "gmd:graphicOverview",
    "gmd:resourceMaintenance",
    "gmd:pointOfContact",
    "gmd:status",
    "gmd:credit",
    "gmd:purpose",
    "gmd:abstract",
    "gmd:citation"
];


// ID: We need the id only once
//id = sourceRecord.get(DatabaseSourceRecord.ID);

var DatabaseSourceRecordType = Java.type('de.ingrid.iplug.dsc.om.SourceRecord');
var id = sourceRecord.get(DatabaseSourceRecordType.ID);


// =============================================================================
// Zusätzliches Feld -> Liste: "BAW/WSV Auftragsnummer"
// - Zusätzliches Feld anlegen unter "Allgemeines - Kategorien"
//   - Liste (Id:'bawAuftragsnummer', Sichtbarkeit: Pflichtfeld, Beschriftung:'BAW/WSV Auftragsnummer', Hilfetext:?)
//   - nachfolgendes Javascript in Feld IDF-Mapping hinzufügen
// =============================================================================
// ------------------------
// IDF
// ------------------------
addKeywordToIdfDocument(id, idfDoc, {
    fieldKey : "bawAuftragsnummer",
    thesaurusName : "DEBUNDBAWAUFTRAGNR",
    thesaurusDate : "2016-11-15T13:42:00.484"
});


// ========================================================================================================================
// Zusätzliches Feld -> Liste: "Simulation / Räumliche Dimensionalität"
// - Zusätzliches Feld anlegen unter "Allgemeines - Kategorien"
//   - Liste (Id:'simSpatialDimension', Sichtbarkeit: Pflichtfeld, Beschriftung:'Simulation / Räumliche Dimensionalität', Hilfetext:?)
//   - nachfolgendes Javascript in Feld IDF-Mapping hinzufügen
// ========================================================================================================================


// ------------------------
// IDF
// ------------------------
addKeywordToIdfDocument(id, idfDoc, {
    fieldKey : "simSpatialDimension",
    thesaurusName : "BAW-DMQS Spatial Dimensions",
    thesaurusDate : "2016-11-15T13:42:00.484"
});
//========================================================================================================================
//Zusätzliches Feld -> Liste: "Simulation / Verfahren"
//TODO:
//- Zusätzliches Feld anlegen unter "Allgemeines - Kategorien"
//- Liste (Id:'simProcess', Sichtbarkeit: Pflichtfeld, Beschriftung:'Simulation / Verfahren', Hilfetext:?)
//- nachfolgendes Javascript in Feld IDF-Mapping hinzufügen
//========================================================================================================================

addKeywordToIdfDocument(id, idfDoc, {
    fieldKey : "simProcess",
    thesaurusName : "BAW-DMQS Modelling Method",
    thesaurusDate : "2016-11-15T13:42:00.484"
});



//=============================================================================
// HierarchieLevelName: Beschreibung der Hierarchieebene
// Bescheibt welche Daten dieser Metadatensatz bescheibt, beispielsweise
// Auftrag, Szenarion, Variante, Simulationslauf.
//
// Spalten: (Type: Liste, id:bawHierarchyLevelName, indexName:bawHierarchyLevelName)
//- nachfolgendes Javascript in Feld IDF-Mapping hinzufügen
//=============================================================================
var hierarchyLevelName = selectDataAndObjClassFromAddnFieldDataWhereObjIdAndFieldKeyAre(id, "bawHierarchyLevelName");
if (hierarchyLevelName && hierarchyLevelName.size() > 0) {
    var value = hierarchyLevelName.get(0).get(DATA_COLUMN);
    if (value) {
        // Locate the MD_Metadata node
        var mdMetadata = DOM.getElement(idfDoc, "//idf:idfMdMetadata");
        // Locate sibling before which to insert the new node
        var nextSibling = searchNextSiblingTag(mdMetadata, mdMetadataTags, "gmd:hierarchyLevelName");
        // Create the new node
        var newNode;
        if (nextSibling) {
            newNode = nextSibling.addElementAsSibling("gmd:hierarchyLevelName");
        } else {
            newNode = mdMetadata.addElement("gmd:hierarchyLevelName");
        }
        // Set the value
        newNode.addText(value);
    }
}




//========================================================================================================================
//Zusätzliches Feld -> Liste: "Simulation / Modellart"
//TODO:
//- Zusätzliches Feld anlegen unter "Allgemeines - Kategorien"
//- Tabelle (Id:'simModelTypeTable', Sichtbarkeit: Pflichtfeld, Beschriftung:'Simulation / Modellart', Hilfetext:?)
//  - Spalten: (Type: Liste, id:simModelType, indexName:simModelType)
//- nachfolgendes Javascript in Feld IDF-Mapping hinzufügen
//========================================================================================================================
function selectObjClassAndFieldDataWhereFieldKeyIsSimModelType(id) {
    var query = "SELECT obj.obj_class AS obj_class, fd1.data AS data "
        + "FROM additional_field_data fd0 "
        + "JOIN additional_field_data fd1 ON fd0.id = fd1.parent_field_id "
        + "JOIN t01_object obj ON fd0.obj_id = obj.id "
        + "    WHERE fd0.obj_id = ? "
        + "          AND fd0.field_key = 'simModelTypeTable' "
        + "          AND fd1.field_key = 'simModelType'";
    var params = [id];
    return SQL.all(query, params);
}
var simModelTypes = selectObjClassAndFieldDataWhereFieldKeyIsSimModelType(id);
if (simModelTypes && simModelTypes.size() > 0) {
    var firstRecord = simModelTypes.get(0);

    var data = firstRecord.get(DATA_COLUMN);
    if (data) {
        var objClass = firstRecord.get(OBJ_CLASS_COLUMN);
        var dataIdXpath;
        if (isService(objClass)) {
            dataIdXpath = SERVICE_ID_XPATH;
        } else {
            dataIdXpath = DATASET_ID_XPATH;
        }

        var keywords = [];
        for (var i=0; i<simModelTypes.size(); i++) {
            keywords.push(simModelTypes.get(i).get(DATA_COLUMN));
        }
        var dataIdTag = DOM.getElement(idfDoc, dataIdXpath);
        addKeywords(
            dataIdTag,
            keywords,
            "BAW-DMQS Modelling Type",
            "2016-11-15T13:42:00.485"
        );
    }
}
//========================================================================================================================
//Zusätzliches Feld -> Liste: "Daten / Größen / Parameter"
//TODO:
//- Zusätzliches Feld anlegen unter "Allgemeines - Kategorien"
//- Tabelle (Id:'simParamTable', Sichtbarkeit: , Beschriftung:'Daten / Größen / Parameter', Hilfetext:?)
//- Spalten: (Type: Liste, id:simParamType, indexName:simParamType)
//- Spalten: (Type: Text, id:simParamName, indexName:simParamName)
//- Spalten: (Type: Text, id:simParamUnit, indexName:simParamUnit)
//- Spalten: (Type: Text, id:simParamValue, indexName:simParamValue)
//- Spalten: (Type: Text, id:simParamInfo, indexName:simParamInfo)
//- Spalten: (Type: Text, id:simParamMdInfo, indexName:simParamMdInfo)
//- nachfolgendes Javascript in Feld IDF-Mapping hinzufügen
//========================================================================================================================

// Locate the MD_Metadata node
var mdMetadata = DOM.getElement(idfDoc, "//idf:idfMdMetadata");
selectTabularDataWithObjIdAndFieldKeyAndApplyFunction(id, "simParamTable", function(row) {
    var nextSibling = searchNextSiblingTag(mdMetadata, mdMetadataTags, "gmd:dataQualityInfo");
    // Create the dataQualityInfo element
    var dataQualityInfo;
    if (nextSibling) {
        dataQualityInfo = nextSibling.addElementAsSibling("gmd:dataQualityInfo");
    } else {
        dataQualityInfo = mdMetadata.addElement("gmd:dataQualityInfo");
    }

    // Create the descendents
    var dqDataQuality = dataQualityInfo.addElement("gmd:DQ_DataQuality");
    dqDataQuality.addElement("gmd:scope/gmd:DQ_Scope/gmd:level/gmd:MD_ScopeCode")
        .addAttribute("codeList", CODELIST_URI + "#MD_ScopeCode")
        .addAttribute("codeListValue", "model");

    //  Online-Ressource: /MD_Metadata/dataQualityInfo/DQ_DataQuality/report/DQ_QuantitativeAttributeAccuracy/result[@href]
    var dqQuantitativeResult = dqDataQuality.addElement("gmd:report/gmd:DQ_QuantitativeAttributeAccuracy/gmd:result")
        .addAttribute("xlink:href", row["simParamInfo"])
        .addElement("gmd:DQ_QuantitativeResult");

    //  Name: /MD_Metadata/dataQualityInfo/DQ_DataQuality/report/DQ_QuantitativeAttributeAccuracy/result/DQ_QuantitativeResult/valueType
    dqQuantitativeResult.addElement("gmd:valueType/gco:RecordType")
        .addText(row["simParamName"]);

    //  Einheit: /MD_Metadata/dataQualityInfo/DQ_DataQuality/report/DQ_QuantitativeAttributeAccuracy/result/DQ_QuantitativeResult/valueUnit
    var unitDefinition = dqQuantitativeResult.addElement("gmd:valueUnit/gml:UnitDefinition")
        .addAttribute("gml:id", "unitDefinition_ID_" + Math.floor(Math.random()*1000));
    unitDefinition.addElement("gml:identifier")
        .addAttribute("codeSpace", "");
    unitDefinition.addElement("gml:catalogSymbol")
        .addText(row["simParamUnit"]);

    //  Werte / Wertebereich: /MD_Metadata/dataQualityInfo/DQ_DataQuality/report/DQ_QuantitativeAttributeAccuracy/result/DQ_QuantitativeResult/value
    var simParamValues = row["simParamValue"].split(/;/);
    for (var i=0; i<simParamValues.length; i++) {
        dqQuantitativeResult.addElement("gmd:value/gco:Record")
            .addText(simParamValues[i]);
    }

    //  Metadaten-Verweis: /MD_Metadata/dataQualityInfo/DQ_DataQuality/lineage/LI_Lineage/source[@href]
    var liSource = dqDataQuality.addElement("gmd:lineage/gmd:LI_Lineage/gmd:source")
        .addAttribute("xlink:href", row["simParamMdInfo"]);

    // Rolle: /MD_Metadata/dataQualityInfo/DQ_DataQuality/lineage/LI_Lineage/source/LI_Source/description
    liSource.addElement("gmd:LI_Source/gmd:description/gco:CharacterString")
        .addText(row["simParamType"]);
});



//========================================================================================================================
//Zusätzliches Feld -> Liste: "Zeitliche Genauigkeit"
//TODO:
//- Zusätzliches Feld anlegen unter "Fachbezug"
//- Text (Id:'dqAccTimeMeas', Sichtbarkeit: Pflichtfeld, Beschriftung:'dqAccTimeMeas', Hilfetext:?)
//- nachfolgendes Javascript in Feld IDF-Mapping hinzufügen
//========================================================================================================================
var valueRecords = selectDataAndObjClassFromAddnFieldDataWhereObjIdAndFieldKeyAre(id, "dqAccTimeMeas");
if (valueRecords && valueRecords.size() > 0) {
    var value = valueRecords.get(0).get(DATA_COLUMN);
    if (value) {
        // Locate the MD_Metadata node
        var mdMetadata = DOM.getElement(idfDoc, "//idf:idfMdMetadata");
        // Locate the next sibling
        var nextSibling = searchNextSiblingTag(mdMetadata, mdMetadataTags, "gmd:dataQualityInfo");
        // Create the dataQualityInfo element
        var dataQualityInfo;
        if (nextSibling) {
            dataQualityInfo = nextSibling.addElementAsSibling("gmd:dataQualityInfo");
        } else {
            dataQualityInfo = mdMetadata.addElement("gmd:dataQualityInfo");
        }

        // Create the descendents
        var dqDataQuality = dataQualityInfo.addElement("gmd:DQ_DataQuality");
        dqDataQuality.addElement("gmd:scope/gmd:DQ_Scope/gmd:level/gmd:MD_ScopeCode")
            .addAttribute("codeList", CODELIST_URI + "#MD_ScopeCode")
            .addAttribute("codeListValue", "model");

        //  Name: /MD_Metadata/dataQualityInfo/DQ_DataQuality/report/DQ_QuantitativeAttributeAccuracy/result/DQ_QuantitativeResult/valueType
        var dqQuantitativeResult = dqDataQuality.addElement("gmd:report/gmd:DQ_AccuracyOfATimeMeasurement/gmd:result/gmd:DQ_QuantitativeResult");
        dqQuantitativeResult.addElement("gmd:valueType/gco:RecordType")
            .addText("temporal accuracy");

        //  Einheit: /MD_Metadata/dataQualityInfo/DQ_DataQuality/report/DQ_QuantitativeAttributeAccuracy/result/DQ_QuantitativeResult/valueUnit
        var unitDefinition = dqQuantitativeResult.addElement("gmd:valueUnit/gml:UnitDefinition")
            .addAttribute("gml:id", "unitDefinition_sec_" + Math.floor(Math.random()*1000));
        unitDefinition.addElement("gml:identifier")
            .addAttribute("codeSpace", "");
        unitDefinition.addElement("gml:name")
            .addText("second");
        unitDefinition.addElement("gml:catalogSymbol")
            .addText("s");

        //  Werte / Wertebereich: /MD_Metadata/dataQualityInfo/DQ_DataQuality/report/DQ_QuantitativeAttributeAccuracy/result/DQ_QuantitativeResult/value
        dqQuantitativeResult.addElement("gmd:value/gco:Record")
            .addText(value);

    }
}

selectTabularDataWithObjIdAndFieldKeyAndApplyFunction(id, "resourceFormat", function(row) {
    // Locate the MD_Metadata node
    var mdDataId = DOM.getElement(idfDoc, "//gmd:identificationInfo/gmd:MD_DataIdentification");
    // Locate the next sibling
    var nextSibling = searchNextSiblingTag(mdDataId, idInfoTags, "gmd:resourceFormat");
    if (!nextSibling) {
        log.error("Could not find any element in  ['gmd:resourceFormat', 'gmd:pointOfContact', 'gmd:abstract', 'gmd:citation'] to attach the resourceFormat Node to. Error Mapping record: " + sourceRecord.toString());
    }

    var resourceFormat = nextSibling.addElementAsSibling("gmd:resourceFormat");
    var mdFormat = resourceFormat.addElement("gmd:MD_Format");
    mdFormat.addElement("gmd:name/gco:CharacterString")
        .addText(row["resourceFormatName"]);
    mdFormat.addElement("gmd:version/gco:CharacterString")
        .addText(row["resourceFormatVersion"]);
    nextSibling = resourceFormat;
});










/* ============================================================================
 * Helper functions
 * ============================================================================
 */
/*
 * Tests whether the given object class corresponds to a geodata service
 */
function isService(objClass) {
    return objClass.equals(SERVICE_TYPE_CODE);
}

/*
 * Returns the field data and the object class stored in the database for the
 * given:
 * - id: object id
 * - fieldKey: field key
 *
 * Returns records of the form
 * ----------------------------
 * |     data     | obj_class |
 * ----------------------------
 * |              |           |
 * |              |           |
 * |              |           |
 * ----------------------------
 */
function selectDataAndObjClassFromAddnFieldDataWhereObjIdAndFieldKeyAre(id, fieldKey) {
    if (id && fieldKey) {
        var query = "SELECT fd.data AS data, obj.obj_class AS obj_class "
            + "FROM additional_field_data fd "
            + "JOIN t01_object obj ON fd.obj_id = obj.id "
            + "    WHERE obj.id = ? AND fd.field_key = ?";
        var params = [id, fieldKey];
        return SQL.all(query, params);
    }
}

/*
 * Fetches the tablular data from the database given
 * - id: id for the table
 * - fieldKey: the identifier key for the table
 *
 * The reply from the database is similar to the one shown below. This data is
 * then organised in the two dimensional array of size sort × field_key as:
 * matrix[sort][field_key] = data
 *
 * The rows of the matrix are then used for currying with the given function:
 * matrix.forEach(fx(row))
 * --------------------------------------------------------------------
 * | id | sort | field_key       | data                               |
 * --------------------------------------------------------------------
 * | 37 |    2 | simParamInfo    | http://example.de/download/path    |
 * | 37 |    2 | simParamName    | Telemac.Prescribed_flowrates       |
 * | 37 |    2 | simParamType    | Ergebnis                           |
 * | 37 |    2 | simParamMdInfo  | http://example.de/download/path    |
 * | 37 |    2 | simParamValue   | 0.0;293.0;117.0                    |
 * | 37 |    2 | simParamUnit    | m3/s                               |
 * | 37 |    3 | simParamUnit    | m                                  |
 * | 37 |    3 | simParamValue   | 307.15;0.0;0.0                     |
 * | 37 |    3 | simParamInfo    | http://example.de/download/path    |
 * --------------------------------------------------------------------
 */

function selectTabularDataWithObjIdAndFieldKeyAndApplyFunction(id, fieldKey, fx) {
    var query = "SELECT DISTINCT fd1.sort AS sort "
        + "FROM additional_field_data fd0 "
        + "JOIN additional_field_data fd1 ON fd1.parent_field_id = fd0.id "
        + "    WHERE fd0.obj_id = ? AND fd0.field_key = ? "
        + "    ORDER BY fd1.sort";
    var params = [id, fieldKey];
    sortCodes = SQL.all(query, params);

    query = "SELECT fd1.field_key AS field_key, fd1.data AS data "
        + "FROM additional_field_data fd0 "
        + "JOIN additional_field_data fd1 ON fd1.parent_field_id = fd0.id "
        + "    WHERE fd0.obj_id = ? AND fd0.field_key = ? AND fd1.sort = ?"
        + "    GROUP BY fd1.field_key, fd1.data "
        + "    ORDER BY fd1.field_key, fd1.data";

    for (var i=0; sortCodes && i<sortCodes.size(); i++) {
        var sortCode = sortCodes.get(i).get(SORT_COLUMN);
        if (!sortCode) {
            continue;
        }
        params = [id, fieldKey, sortCode];
        var records = SQL.all(query, params);
        rowData = {};
        //var valid = false;
        for (var j=0; records && j<records.size(); j++) {
            try {
                var key = records.get(j).get(FIELD_KEY_COLUMN);
                var data = records.get(j).get(DATA_COLUMN);
                rowData[key] = data;
                valid = true;
            } catch (err) {
                log.error(err);
            }
        }
        fx(rowData);
    }
}

function searchNextSiblingTag(parentNode, siblings, tagName) {
    /*
     * siblings is an array of all the possible siblings in a reverse order in
     * which they can appear under the parent. We want a slice of all the
     * siblings preceding the given tagName. So we add one to the index of the
     * tagName within the inverted array of siblings.
     */
    var index = siblings.indexOf(tagName) + 1;
    var slice = siblings.slice(index);
    var nextSibling = null;
    for (var i=0; i<slice.length && nextSibling == null; i++) {
        // get the last occurrence of this path if any
        nextSibling = DOM.getElement(parentNode, slice[i]+"[last()]");
    }
    return nextSibling;
}

/*
 * Adds given keywords to the XML document. Parameters are as follows:
 * - dataIdentification: the dataIdentification XML ancestor node to which the
 *   node has to be added
 * - keywordTexts[]: an array with the keyword texts
 * - thesaurusName: the name of the thesaurus in the citation field
 * - thesaurusDate: the date of the thesaurus in the citation field
 */
function addKeywords(dataIdentification, keywordTexts, thesaurusName, thesaurusDate) {
    // Find out which "next sibling" is available
    var nextSibling = searchNextSiblingTag(dataIdentification, idInfoTags, "gmd:descriptiveKeywords");

    // write keyword and thesaurus
    var parentNode;
    var tagName = "gmd:descriptiveKeywords";
    if (nextSibling) {
        parentNode = nextSibling.addElementAsSibling(tagName);
    } else {
        parentNode = dataIdentification.addElement(tagName);
    }

    var keyword = parentNode.addElement("gmd:MD_Keywords");
    for (var i=0; i<keywordTexts.length; i++) {
        keyword.addElement("gmd:keyword/gco:CharacterString")
            .addText(keywordTexts[i]);
    }
    keyword.addElement("gmd:type/gmd:MD_KeywordTypeCode")
        .addAttribute("codeList", CODELIST_URI + "#MD_KeywordTypeCode")
        .addAttribute("codeListValue", "discipline");

    var citation = keyword.addElement("gmd:thesaurusName/gmd:CI_Citation");
    citation.addElement("gmd:title/gco:CharacterString")
        .addText(thesaurusName);
    var ciDate = citation.addElement("gmd:date/gmd:CI_Date");
    ciDate.addElement("gmd:date/gco:Date")
        .addText(thesaurusDate);
    ciDate.addElement("gmd:dateType/gmd:CI_DateTypeCode")
        .addAttribute("codeList", CODELIST_URI + "#CI_DateTypeCode")
        .addAttribute("codeListValue", "publication");
}

function addKeywordToIdfDocument(id, idfDoc, params) {
    var fieldKey = params['fieldKey'];
    var thesaurusName = params['thesaurusName'];
    var thesaurusDate = params['thesaurusDate'];

    var records = selectDataAndObjClassFromAddnFieldDataWhereObjIdAndFieldKeyAre(id, fieldKey);
    if (records && records.size() > 0) {
        var record = records.get(0);
        var data = record.get(DATA_COLUMN);
        if (data) {
            var objClass = record.get(OBJ_CLASS_COLUMN);
            var dataIdXpath;
            if (isService(objClass)) {
                dataIdXpath = SERVICE_ID_XPATH;
            } else {
                dataIdXpath = DATASET_ID_XPATH;
            }

            var dataIdTag = DOM.getElement(idfDoc, dataIdXpath);
            addKeywords(dataIdTag,
                [data],
                thesaurusName,
                thesaurusDate);
        }
    }
}
