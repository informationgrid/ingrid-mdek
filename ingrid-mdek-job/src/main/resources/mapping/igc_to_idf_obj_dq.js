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
}

importPackage(Packages.org.w3c.dom);
importPackage(Packages.de.ingrid.iplug.dsc.om);

if (log.isDebugEnabled()) {
    log.debug("Mapping source record DQ to idf document: " + sourceRecord.toString());
}

if (!(sourceRecord instanceof DatabaseSourceRecord)) {
    throw new IllegalArgumentException("Record is no DatabaseRecord!");
}
// ---------- Initialize ----------
// add Namespaces to Utility for convenient handling of NS !
DOM.addNS("gmd", "http://www.isotc211.org/2005/gmd");
DOM.addNS("gco", "http://www.isotc211.org/2005/gco");
DOM.addNS("srv", "http://www.isotc211.org/2005/srv");
DOM.addNS("gml", "http://www.opengis.net/gml");
DOM.addNS("gts", "http://www.isotc211.org/2005/gts");
DOM.addNS("xlink", "http://www.w3.org/1999/xlink");

var globalCodeListAttrURL = "http://standards.iso.org/iso/19139/resources/gmxCodelists.xml";

// ---------- <idf:idfMdMetadata> ----------
var mdMetadata = DOM.getElement(idfDoc, "//idf:idfMdMetadata");

// ---------- Extract node after which dataQualityInfo should be added ! ----------
// path of elements "before" DQ. Path until first mandatory element.
var path = ["gmd:distributionInfo", "gmd:contentInfo", "gmd:identificationInfo"];
        
// find first present node from paths
var dqPredecessorNode = null;
for (i=0; i<path.length; i++) {
    // get the last occurrence of this path if any
    dqPredecessorNode = DOM.getElement(mdMetadata, path[i]+"[last()]");
    if (dqPredecessorNode) { break; }
}

if (log.isDebugEnabled()) {
    log.debug("DQ Mapping: dqPredecessorNode = " + dqPredecessorNode);
}

        
// ========== t01_object ==========
// convert id to number to be used in PreparedStatement as Integer to avoid postgres error !
var objId = +sourceRecord.get("id");

var objRows = SQL.all("SELECT * FROM t01_object WHERE id=?", [+objId]);
for (i=0; i<objRows.size(); i++) {
    var objRow = objRows.get(i);
    var objClass = objRow.get("obj_class");

    // ---------- <idf:idfMdMetadata/gmd:dataQualityInfo/gmd:DQ_DataQuality> ----------
    // ---------- <gmd:DQ_DataQuality/gmd:scope/gmd:DQ_Scope/gmd:level/gmd:MD_ScopeCode> ----------
    var dqDataQuality;
    var liLineage;

// GEO-INFORMATION/KARTE(1)
    if (objClass.equals("1")) {
        var objGeoRow = SQL.first("SELECT * FROM t011_obj_geo WHERE obj_id=?", [+objId]);

        // ---------- <gmd:DQ_DataQuality/gmd:report/gmd:DQ_CompletenessOmission> ----------
        if (hasValue(objGeoRow.get("rec_grade"))) {
            dqDataQuality = addDataQualityInfoElement().addElement(getDqDataQualityElement(objClass));
            var completenessOmission = dqDataQuality.addElement("gmd:report/gmd:DQ_CompletenessOmission");
            // map now INSPIRE conform !
            completenessOmission.addElement("gmd:nameOfMeasure/gco:CharacterString").addText("Rate of missing items");
            completenessOmission.addElement("gmd:measureIdentification/gmd:MD_Identifier/gmd:code/gco:CharacterString").addText("7");
            // ATTENTION: ! measureDescription "completeness omission (rec_grade)" is used in portal to differ from display as DataQuality Table !
            completenessOmission.addElement("gmd:measureDescription/gco:CharacterString").addText("completeness omission (rec_grade)");
            var dqQuantitativeResult = completenessOmission.addElement("gmd:result/gmd:DQ_QuantitativeResult");
            var unitDefinition = dqQuantitativeResult.addElement("gmd:valueUnit/gml:UnitDefinition")
                .addAttribute("gml:id", "unitDefinition_ID_".concat(TRANSF.getRandomUUID()));
            unitDefinition.addElement("gml:identifier").addAttribute("codeSpace", "");
            unitDefinition.addElement("gml:name").addText("percent");
            unitDefinition.addElement("gml:quantityType").addText("completeness omission");
            unitDefinition.addElement("gml:catalogSymbol").addText("%");
            dqQuantitativeResult.addElement("gmd:value/gco:Record").addText(objGeoRow.get("rec_grade"));
        }

        // ---------- <gmd:DQ_DataQuality/gmd:report/gmd:DQ_AbsoluteExternalPositionalAccuracy> ----------
        if (hasValue(objGeoRow.get("pos_accuracy_vertical"))) {
            if (!dqDataQuality) {
                dqDataQuality = addDataQualityInfoElement().addElement(getDqDataQualityElement(objClass));
            }
            var dqElem = dqDataQuality.addElement("gmd:report/gmd:DQ_AbsoluteExternalPositionalAccuracy");
            dqElem.addElement("gmd:nameOfMeasure/gco:CharacterString").addText("Mean value of positional uncertainties (1D)");
            // mean value of positional uncertainties (1D, 2D and 3D)
            dqElem.addElement("gmd:measureIdentification/gmd:MD_Identifier/gmd:code/gco:CharacterString").addText("28");
            dqElem.addElement("gmd:measureDescription/gco:CharacterString").addText("vertical");
            var dqQuantitativeResult = dqElem.addElement("gmd:result/gmd:DQ_QuantitativeResult");
            var unitDefinition = dqQuantitativeResult.addElement("gmd:valueUnit/gml:UnitDefinition")
                .addAttribute("gml:id", "unitDefinition_ID_".concat(TRANSF.getRandomUUID()));
            unitDefinition.addElement("gml:identifier").addAttribute("codeSpace", "");
            unitDefinition.addElement("gml:name").addText("meter");
            unitDefinition.addElement("gml:quantityType").addText("absolute external positional accuracy, vertical accuracy");
            unitDefinition.addElement("gml:catalogSymbol").addText("m");
            dqQuantitativeResult.addElement("gmd:value/gco:Record").addText(objGeoRow.get("pos_accuracy_vertical"));
        }
        
        // ---------- <gmd:DQ_DataQuality/gmd:report/gmd:DQ_GriddedDataPositionalAccuracy> ----------
        // Ticket: #378
        if (hasValue(objGeoRow.get("grid_pos_accuracy"))) {
            if (!dqDataQuality) {
                dqDataQuality = addDataQualityInfoElement().addElement(getDqDataQualityElement(objClass));
            }
            var dqElem = dqDataQuality.addElement("gmd:report/gmd:DQ_GriddedDataPositionalAccuracy");
            dqElem.addElement("gmd:nameOfMeasure/gco:CharacterString").addText("Root mean square error of planimetry");
            // mean value of positional uncertainties (1D, 2D and 3D)
            dqElem.addElement("gmd:measureIdentification/gmd:MD_Identifier/gmd:code/gco:CharacterString").addText("47");
            // the field "measure desription" is not necessary according to ticket #378
            // dqElem.addElement("gmd:measureDescription/gco:CharacterString").addText("Root mean square error of planimetry");
            var dqQuantitativeResult = dqElem.addElement("gmd:result/gmd:DQ_QuantitativeResult");
            var unitDefinition = dqQuantitativeResult.addElement("gmd:valueUnit/gml:UnitDefinition")
            .addAttribute("gml:id", "unitDefinition_ID_".concat(TRANSF.getRandomUUID()));
            unitDefinition.addElement("gml:identifier").addAttribute("codeSpace", "");
            unitDefinition.addElement("gml:name").addText("meter");
            unitDefinition.addElement("gml:quantityType").addText("absolute external positional accuracy");
            unitDefinition.addElement("gml:catalogSymbol").addText("m");
            dqQuantitativeResult.addElement("gmd:value/gco:Record").addText(objGeoRow.get("grid_pos_accuracy"));
        }

        // ---------- <gmd:DQ_DataQuality/gmd:report/gmd:DQ_AbsoluteExternalPositionalAccuracy> ----------
        if (hasValue(objGeoRow.get("rec_exact"))) {
            if (!dqDataQuality) {
                dqDataQuality = addDataQualityInfoElement().addElement(getDqDataQualityElement(objClass));
            }
            var dqElem = dqDataQuality.addElement("gmd:report/gmd:DQ_AbsoluteExternalPositionalAccuracy");
            dqElem.addElement("gmd:nameOfMeasure/gco:CharacterString").addText("Mean value of positional uncertainties (2D)");
            // mean value of positional uncertainties (1D, 2D and 3D)
            dqElem.addElement("gmd:measureIdentification/gmd:MD_Identifier/gmd:code/gco:CharacterString").addText("28");
            dqElem.addElement("gmd:measureDescription/gco:CharacterString").addText("geographic");
            var dqQuantitativeResult = dqElem.addElement("gmd:result/gmd:DQ_QuantitativeResult");
            var unitDefinition = dqQuantitativeResult.addElement("gmd:valueUnit/gml:UnitDefinition")
                .addAttribute("gml:id", "unitDefinition_ID_".concat(TRANSF.getRandomUUID()));
            unitDefinition.addElement("gml:identifier").addAttribute("codeSpace", "");
            unitDefinition.addElement("gml:name").addText("meter");
            unitDefinition.addElement("gml:quantityType").addText("absolute external positional accuracy, geographic accuracy");
            unitDefinition.addElement("gml:catalogSymbol").addText("m");
            dqQuantitativeResult.addElement("gmd:value/gco:Record").addText(objGeoRow.get("rec_exact"));
        }

        // ---------- <gmd:DQ_DataQuality/gmd:report/gmd:DQ_DomainConsistency/gmd:result/gmd:DQ_ConformanceResult> ----------
        rows = SQL.all("SELECT * FROM object_conformity WHERE obj_id=?", [+objId]);
        for (i=0; i<rows.size(); i++) {
            var dqConformanceResult = getDqConformanceResultElement(rows.get(i));
            // only write report if evaluated, see https://dev.wemove.com/jira/browse/INGRID23-165
            if (hasValue(dqConformanceResult)) {
                if (!dqDataQuality) {
                    dqDataQuality = addDataQualityInfoElement().addElement(getDqDataQualityElement(objClass));
                }
                dqDataQuality.addElement("gmd:report/gmd:DQ_DomainConsistency/gmd:result")
                    .addElement(dqConformanceResult);
            }
        }

        addObjectDataQualityTable(objRow, dqDataQuality);

        // ---------- <gmd:DQ_DataQuality/gmd:lineage/gmd:LI_Lineage/gmd:statement> ----------
        if (hasValue(objGeoRow.get("special_base"))) {
            if (!dqDataQuality) {
                dqDataQuality = addDataQualityInfoElement().addElement(getDqDataQualityElement(objClass));
            }
            liLineage = dqDataQuality.addElement("gmd:lineage/gmd:LI_Lineage");
            IDF_UTIL.addLocalizedCharacterstring(liLineage.addElement("gmd:statement"), objGeoRow.get("special_base"));
        }

        // ---------- <gmd:DQ_DataQuality/gmd:lineage/gmd:LI_Lineage/gmd:processStep/gmd:LI_ProcessStep/gmd:description> ----------
        if (hasValue(objGeoRow.get("method"))) {
            if (!dqDataQuality) {
                dqDataQuality = addDataQualityInfoElement().addElement(getDqDataQualityElement(objClass));
            }
            if (!liLineage) {
                liLineage = dqDataQuality.addElement("gmd:lineage/gmd:LI_Lineage");
            }
            IDF_UTIL.addLocalizedCharacterstring(liLineage.addElement("gmd:processStep/gmd:LI_ProcessStep/gmd:description"),objGeoRow.get("method"));
        }

        // ---------- <gmd:DQ_DataQuality/gmd:lineage/gmd:LI_Lineage/gmd:source/gmd:LI_Source/gmd:description> ----------
        if (hasValue(objGeoRow.get("data_base"))) {
            if (!dqDataQuality) {
                dqDataQuality = addDataQualityInfoElement().addElement(getDqDataQualityElement(objClass));
            }
            if (!liLineage) {
                liLineage = dqDataQuality.addElement("gmd:lineage/gmd:LI_Lineage");
            }
            IDF_UTIL.addLocalizedCharacterstring(liLineage.addElement("gmd:source/gmd:LI_Source/gmd:description"),objGeoRow.get("data_base"));
        }

// GEODATENDIENST(3) + INFORMATIONSSYSTEM/DIENST/ANWENDUNG(6)
    } else if (objClass.equals("3") || objClass.equals("6")) {

        // "object_conformity" ONLY CLASS 3, but we do not distinguish, class 6 should have no rows here !

        // ---------- <gmd:DQ_DataQuality/gmd:report/gmd:DQ_DomainConsistency/gmd:result/gmd:DQ_ConformanceResult> ----------
        rows = SQL.all("SELECT * FROM object_conformity WHERE obj_id=?", [+objId]);
        for (i=0; i<rows.size(); i++) {
            var dqConformanceResult = getDqConformanceResultElement(rows.get(i));
            // only write report if evaluated, see https://dev.wemove.com/jira/browse/INGRID23-165
            if (hasValue(dqConformanceResult)) {
                if (!dqDataQuality) {
                    dqDataQuality = addDataQualityInfoElement().addElement(getDqDataQualityElement(objClass));
                }
                dqDataQuality.addElement("gmd:report/gmd:DQ_DomainConsistency/gmd:result")
                    .addElement(dqConformanceResult);
            }
        }

        // class 3 and class 6
        var objServRow = SQL.first("SELECT * FROM t011_obj_serv WHERE obj_id=?", [+objId]);

        // ---------- <gmd:DQ_DataQuality/gmd:lineage/gmd:LI_Lineage/gmd:processStep/gmd:LI_ProcessStep/gmd:description> ----------
        if (hasValue(objServRow.get("history"))) {
            dqDataQuality = addDataQualityInfoElement().addElement(getDqDataQualityElement(objClass));
            liLineage = dqDataQuality.addElement("gmd:lineage/gmd:LI_Lineage");
            IDF_UTIL.addLocalizedCharacterstring(liLineage.addElement("gmd:processStep/gmd:LI_ProcessStep/gmd:description"),objServRow.get("history"));
        }

        // ---------- <gmd:DQ_DataQuality/gmd:lineage/gmd:LI_Lineage/gmd:source/gmd:LI_Source/gmd:description> ----------
        if (hasValue(objServRow.get("base"))) {
            if (!dqDataQuality) {
                dqDataQuality = addDataQualityInfoElement().addElement(getDqDataQualityElement(objClass));
            }
            if (!liLineage) {
                liLineage = dqDataQuality.addElement("gmd:lineage/gmd:LI_Lineage");
            }
            IDF_UTIL.addLocalizedCharacterstring(liLineage.addElement("gmd:source/gmd:LI_Source/gmd:description"),objServRow.get("base"));
        }

// DATENSAMMLUNG/DATENBANK(5)
    } else if (objClass.equals("5")) {
        // ---------- <gmd:DQ_DataQuality/gmd:lineage/gmd:LI_Lineage/gmd:source/gmd:LI_Source/gmd:description> ----------
        var rs = SQL.first("SELECT base FROM t011_obj_data WHERE obj_id=?", [+objId]);
        if (hasValue(rs)) {
            value = rs.get("base");
            if (hasValue(value)) {
                dqDataQuality = addDataQualityInfoElement().addElement(getDqDataQualityElement(objClass));
                liLineage = dqDataQuality.addElement("gmd:lineage/gmd:LI_Lineage");
                IDF_UTIL.addLocalizedCharacterstring(liLineage.addElement("gmd:source/gmd:LI_Source/gmd:description"),value);
            }
        }

// DOKUMENT/BERICHT/LITERATUR(2)
    } else if (objClass.equals("2")) {
        // ---------- <gmd:DQ_DataQuality/gmd:lineage/gmd:LI_Lineage/gmd:source/gmd:LI_Source/gmd:description> ----------
        var rs = SQL.first("SELECT base FROM t011_obj_literature WHERE obj_id=?", [+objId]);
        if (hasValue(rs)) {
            value = rs.get("base");
            if (hasValue(value)) {
                dqDataQuality = addDataQualityInfoElement().addElement(getDqDataQualityElement(objClass));
                liLineage = dqDataQuality.addElement("gmd:lineage/gmd:LI_Lineage");
                IDF_UTIL.addLocalizedCharacterstring(liLineage.addElement("gmd:source/gmd:LI_Source/gmd:description"),value);
            }
        }
    }
}


// "nicht evaluiert"(3) leads to nilReason "unknown"
function getDqConformanceResultElement(conformityRow) {

// NO ! Map according to GDI-DE, see https://dev.informationgrid.eu/redmine/issues/86
/*
    if (!hasValue(conformityRow.get("degree_key")) || conformityRow.get("degree_key").equals("3")) {
        // "not evaluated", we retuen null element indicating no "gmd:report" should be written
        // see https://dev.wemove.com/jira/browse/INGRID23-165
        if (log.isDebugEnabled()) {
            log.debug("Object_conformity degree_key = " + conformityRow.get("degree_key") + " (3=not evaluated), we skip this one, no gmd:report !");
        }
        return null;
    }
*/
    var dqConformanceResult = DOM.createElement("gmd:DQ_ConformanceResult");
    var ciCitation = dqConformanceResult.addElement("gmd:specification/gmd:CI_Citation");

    var isInspire = conformityRow.get("is_inspire");
    if (!hasValue(isInspire)) {
        isInspire = "Y"; // Assume that we are dealing with INSPIRE conformity list
    }
    var specification;
    if (isInspire.equals("Y")) {
        // ISO: first iso value, see INGRID-2337
        specification = TRANSF.getCodeListEntryFromIGCSyslistEntry(6005, conformityRow.get("specification_key"), "iso");
    } else {
        specification = TRANSF.getCodeListEntryFromIGCSyslistEntry(6006, conformityRow.get("specification_key"), "iso");
    }
    // if no iso then as usual
    if (!hasValue(specification) && isInspire.equals("Y")) {
        specification = TRANSF.getIGCSyslistEntryName(6005, conformityRow.get("specification_key"));
    } else if (!hasValue(specification) && isInspire.equals("N")) {
        specification = TRANSF.getIGCSyslistEntryName(6006, conformityRow.get("specification_key"));
    }
    if (!hasValue(specification)) {
        specification = conformityRow.get("specification_value");
    }

    var specificationDate;
    if (!hasValue(specification) || conformityRow.get("specification_key") < 0) {
        specificationDate = TRANSF.getISODateFromIGCDate(conformityRow.get("publication_date"));
    } else if (isInspire.equals("Y")) {
    	// INGRID-2270: get date from data field
        specificationDate = TRANSF.getISOCodeListEntryData(6005, specification);
    } else if (hasValue(specification)) {
        specificationDate = TRANSF.getISOCodeListEntryData(6006, specification);
    }

    if (hasValue(specification)) {
        IDF_UTIL.addLocalizedCharacterstring(ciCitation.addElement("gmd:title"),specification);
    } else {
        ciCitation.addElement("gmd:title").addAttribute("gco:nilReason", "missing");
    }

    var ciDate = ciCitation.addElement("gmd:date/gmd:CI_Date");
    if (hasValue(specificationDate)) {
        specificationDate = specificationDate.substring(0, 10);
        ciDate.addElement("gmd:date").addElement(getDateOrDateTime(specificationDate));
    } else {
        ciDate.addElement("gmd:date").addAttribute("gco:nilReason", "unknown");
    }
    ciDate.addElement("gmd:dateType/gmd:CI_DateTypeCode")
        .addAttribute("codeList", globalCodeListAttrURL + "#CI_DateTypeCode")
        .addAttribute("codeListValue", "publication")
        .addText("publication");

    var explanation = conformityRow.get("explanation");
    if (!hasValue(explanation)) {
        explanation  = "see the referenced specification"; // use standard value
    }
    dqConformanceResult.addElement("gmd:explanation/gco:CharacterString").addText(explanation);

    // REDMINE-86: If conformity "not evaluated" then set "unknown" attribute according to GDI-DE !
    if (conformityRow.get("degree_key").equals("3")) {
        // not evaluated
        dqConformanceResult.addElement("gmd:pass").addAttribute("gco:nilReason", "unknown");
    } else {
        // true or false
        dqConformanceResult.addElement("gmd:pass/gco:Boolean").addText(conformityRow.get("degree_key").equals("1"));
    }
    return dqConformanceResult;
}

function addObjectDataQualityTable(objRow, dqDataQuality) {
    var objId = objRow.get("id");
    var objClass = objRow.get("obj_class");

    var rows = SQL.all("SELECT * FROM object_data_quality WHERE obj_id=?", [+objId]);
    for (i=0; i<rows.size(); i++) {
        var igcRow = rows.get(i);
        var igcDqElementId = igcRow.get("dq_element_id");
        var igcNameOfMeasureKey = igcRow.get("name_of_measure_key");
        var igcNameOfMeasureValue = igcRow.get("name_of_measure_value");
        var igcMeasureDescription = igcRow.get("measure_description");
        var igcResultValue = igcRow.get("result_value");
        
        if (igcDqElementId.equals("109")) {
            // ---------- <gmd:DQ_DataQuality/gmd:report/gmd:DQ_CompletenessCommission> ----------

            if (!dqDataQuality) {
                dqDataQuality = addDataQualityInfoElement().addElement(getDqDataQualityElement(objClass));
            }
            var dqElem = dqDataQuality.addElement("gmd:report/gmd:DQ_CompletenessCommission");
            IDF_UTIL.addLocalizedCharacterstring(dqElem.addElement("gmd:nameOfMeasure"),igcNameOfMeasureValue);
            if (igcNameOfMeasureKey.equals("1")) {
                // Rate of excess items
                dqElem.addElement("gmd:measureIdentification/gmd:MD_Identifier/gmd:code/gco:CharacterString").addText("3");
            } else if (igcNameOfMeasureKey.equals("2")) {
                // Number of duplicate feature instances
                dqElem.addElement("gmd:measureIdentification/gmd:MD_Identifier/gmd:code/gco:CharacterString").addText("4");
            }
            if (hasValue(igcMeasureDescription)) {
                dqElem.addElement("gmd:measureDescription/gco:CharacterString").addText(igcMeasureDescription);
            }
            var dqQuantitativeResult = dqElem.addElement("gmd:result/gmd:DQ_QuantitativeResult");
            if (igcNameOfMeasureKey.equals("1")) {
                var unitDefinition = dqQuantitativeResult.addElement("gmd:valueUnit/gml:UnitDefinition")
                    .addAttribute("gml:id", "unitDefinition_ID_".concat(TRANSF.getRandomUUID()));
                unitDefinition.addElement("gml:identifier").addAttribute("codeSpace", "");
                unitDefinition.addElement("gml:name").addText("percent");
                unitDefinition.addElement("gml:quantityType").addText("completeness commission");
                unitDefinition.addElement("gml:catalogSymbol").addText("%");
            } else if (igcNameOfMeasureKey.equals("2")) {
                dqQuantitativeResult.addElement("gmd:valueUnit").addAttribute("gco:nilReason", "inapplicable");
            } else {
                dqQuantitativeResult.addElement("gmd:valueUnit").addAttribute("gco:nilReason", "unknown");
            }
            dqQuantitativeResult.addElement("gmd:value/gco:Record").addText(igcResultValue);

        } else if (igcDqElementId.equals("112")) {
            // ---------- <gmd:DQ_DataQuality/gmd:report/gmd:DQ_ConceptualConsistency> ----------

            if (!dqDataQuality) {
                dqDataQuality = addDataQualityInfoElement().addElement(getDqDataQualityElement(objClass));
            }
            var dqElem = dqDataQuality.addElement("gmd:report/gmd:DQ_ConceptualConsistency");
            IDF_UTIL.addLocalizedCharacterstring(dqElem.addElement("gmd:nameOfMeasure"),igcNameOfMeasureValue);
            if (igcNameOfMeasureKey.equals("1")) {
                // Number of invalid overlaps of surfaces
                dqElem.addElement("gmd:measureIdentification/gmd:MD_Identifier/gmd:code/gco:CharacterString").addText("11");
            } else if (igcNameOfMeasureKey.equals("2")) {
                // Conceptual Schema compliance = (Compliance rate with the rules of the conceptual schema)
                dqElem.addElement("gmd:measureIdentification/gmd:MD_Identifier/gmd:code/gco:CharacterString").addText("13");
            }
            if (hasValue(igcMeasureDescription)) {
                dqElem.addElement("gmd:measureDescription/gco:CharacterString").addText(igcMeasureDescription);
            }
            var dqQuantitativeResult = dqElem.addElement("gmd:result/gmd:DQ_QuantitativeResult");
            if (igcNameOfMeasureKey.equals("1")) {
                dqQuantitativeResult.addElement("gmd:valueUnit").addAttribute("gco:nilReason", "inapplicable");
            } else if (igcNameOfMeasureKey.equals("2")) {
                var unitDefinition = dqQuantitativeResult.addElement("gmd:valueUnit/gml:UnitDefinition")
                    .addAttribute("gml:id", "unitDefinition_ID_".concat(TRANSF.getRandomUUID()));
                unitDefinition.addElement("gml:identifier").addAttribute("codeSpace", "");
                unitDefinition.addElement("gml:name").addText("percent");
                unitDefinition.addElement("gml:quantityType").addText("conceptual consistency");
                unitDefinition.addElement("gml:catalogSymbol").addText("%");
            } else {
                dqQuantitativeResult.addElement("gmd:valueUnit").addAttribute("gco:nilReason", "unknown");
            }
            dqQuantitativeResult.addElement("gmd:value/gco:Record").addText(igcResultValue);

        } else if (igcDqElementId.equals("113")) {
            // ---------- <gmd:DQ_DataQuality/gmd:report/gmd:DQ_DomainConsistency> ----------

            if (!dqDataQuality) {
                dqDataQuality = addDataQualityInfoElement().addElement(getDqDataQualityElement(objClass));
            }
            var dqElem = dqDataQuality.addElement("gmd:report/gmd:DQ_DomainConsistency");
            IDF_UTIL.addLocalizedCharacterstring(dqElem.addElement("gmd:nameOfMeasure"),igcNameOfMeasureValue);
            if (igcNameOfMeasureKey.equals("1")) {
                // Value domain conformance rate
                dqElem.addElement("gmd:measureIdentification/gmd:MD_Identifier/gmd:code/gco:CharacterString").addText("17");
            }
            if (hasValue(igcMeasureDescription)) {
                dqElem.addElement("gmd:measureDescription/gco:CharacterString").addText(igcMeasureDescription);
            }
            var dqQuantitativeResult = dqElem.addElement("gmd:result/gmd:DQ_QuantitativeResult");
            if (igcNameOfMeasureKey.equals("1")) {
                var unitDefinition = dqQuantitativeResult.addElement("gmd:valueUnit/gml:UnitDefinition")
                    .addAttribute("gml:id", "unitDefinition_ID_".concat(TRANSF.getRandomUUID()));
                unitDefinition.addElement("gml:identifier").addAttribute("codeSpace", "");
                unitDefinition.addElement("gml:name").addText("percent");
                unitDefinition.addElement("gml:quantityType").addText("domain consistency");
                unitDefinition.addElement("gml:catalogSymbol").addText("%");
            } else {
                dqQuantitativeResult.addElement("gmd:valueUnit").addAttribute("gco:nilReason", "unknown");
            }
            dqQuantitativeResult.addElement("gmd:value/gco:Record").addText(igcResultValue);

        } else if (igcDqElementId.equals("114")) {
            // ---------- <gmd:DQ_DataQuality/gmd:report/gmd:DQ_FormatConsistency> ----------

            if (!dqDataQuality) {
                dqDataQuality = addDataQualityInfoElement().addElement(getDqDataQualityElement(objClass));
            }
            var dqElem = dqDataQuality.addElement("gmd:report/gmd:DQ_FormatConsistency");
            IDF_UTIL.addLocalizedCharacterstring(dqElem.addElement("gmd:nameOfMeasure"),igcNameOfMeasureValue);
            if (igcNameOfMeasureKey.equals("1")) {
                // Physical structure conflict rate
                dqElem.addElement("gmd:measureIdentification/gmd:MD_Identifier/gmd:code/gco:CharacterString").addText("20");
            }
            if (hasValue(igcMeasureDescription)) {
                dqElem.addElement("gmd:measureDescription/gco:CharacterString").addText(igcMeasureDescription);
            }
            var dqQuantitativeResult = dqElem.addElement("gmd:result/gmd:DQ_QuantitativeResult");
            if (igcNameOfMeasureKey.equals("1")) {
                var unitDefinition = dqQuantitativeResult.addElement("gmd:valueUnit/gml:UnitDefinition")
                    .addAttribute("gml:id", "unitDefinition_ID_".concat(TRANSF.getRandomUUID()));
                unitDefinition.addElement("gml:identifier").addAttribute("codeSpace", "");
                unitDefinition.addElement("gml:name").addText("percent");
                unitDefinition.addElement("gml:quantityType").addText("format consistency");
                unitDefinition.addElement("gml:catalogSymbol").addText("%");
            } else {
                dqQuantitativeResult.addElement("gmd:valueUnit").addAttribute("gco:nilReason", "unknown");
            }
            dqQuantitativeResult.addElement("gmd:value/gco:Record").addText(igcResultValue);

        } else if (igcDqElementId.equals("115")) {
            // ---------- <gmd:DQ_DataQuality/gmd:report/gmd:DQ_TopologicalConsistency> ----------

            if (!dqDataQuality) {
                dqDataQuality = addDataQualityInfoElement().addElement(getDqDataQualityElement(objClass));
            }
            var dqElem = dqDataQuality.addElement("gmd:report/gmd:DQ_TopologicalConsistency");
            IDF_UTIL.addLocalizedCharacterstring(dqElem.addElement("gmd:nameOfMeasure"),igcNameOfMeasureValue);
            if (igcNameOfMeasureKey.equals("1")) {
                // Number of invalid overlaps of surfaces
                dqElem.addElement("gmd:measureIdentification/gmd:MD_Identifier/gmd:code/gco:CharacterString").addText("11");
            } else if (igcNameOfMeasureKey.equals("2")) {
                // Number of missing connections due to undershoots
                dqElem.addElement("gmd:measureIdentification/gmd:MD_Identifier/gmd:code/gco:CharacterString").addText("23");
            } else if (igcNameOfMeasureKey.equals("3")) {
                // Number of missing connections due to overshoots
                dqElem.addElement("gmd:measureIdentification/gmd:MD_Identifier/gmd:code/gco:CharacterString").addText("24");
            } else if (igcNameOfMeasureKey.equals("4")) {
                // Number of invalid slivers
                dqElem.addElement("gmd:measureIdentification/gmd:MD_Identifier/gmd:code/gco:CharacterString").addText("25");
            } else if (igcNameOfMeasureKey.equals("5")) {
                // Number of invalid self-intersect errors
                dqElem.addElement("gmd:measureIdentification/gmd:MD_Identifier/gmd:code/gco:CharacterString").addText("26");
            } else if (igcNameOfMeasureKey.equals("6")) {
                // Number of invalid self-overlap errors
                dqElem.addElement("gmd:measureIdentification/gmd:MD_Identifier/gmd:code/gco:CharacterString").addText("27");
            } else if (igcNameOfMeasureKey.equals("7")) {
                // Number of faulty point-curve connections
                dqElem.addElement("gmd:measureIdentification/gmd:MD_Identifier/gmd:code/gco:CharacterString").addText("21");
            } else if (igcNameOfMeasureKey.equals("8")) {
                // Number of missing connections due to crossing of bridge/road
                dqElem.addElement("gmd:measureIdentification").addAttribute("gco:nilReason", "missing");
            } else if (igcNameOfMeasureKey.equals("9")) {
                // Number of watercourse links below threshold length
                dqElem.addElement("gmd:measureIdentification").addAttribute("gco:nilReason", "missing");
            } else if (igcNameOfMeasureKey.equals("10")) {
                // Number of closed watercourse links
                dqElem.addElement("gmd:measureIdentification").addAttribute("gco:nilReason", "missing");
            } else if (igcNameOfMeasureKey.equals("11")) {
                // Number of multi-part watercourse links
                dqElem.addElement("gmd:measureIdentification").addAttribute("gco:nilReason", "missing");
            }
            if (hasValue(igcMeasureDescription)) {
                dqElem.addElement("gmd:measureDescription/gco:CharacterString").addText(igcMeasureDescription);
            }
            var dqQuantitativeResult = dqElem.addElement("gmd:result/gmd:DQ_QuantitativeResult");
            if (igcNameOfMeasureKey.equals("-1")) {
                dqQuantitativeResult.addElement("gmd:valueUnit").addAttribute("gco:nilReason", "unknown");
            } else {
                dqQuantitativeResult.addElement("gmd:valueUnit").addAttribute("gco:nilReason", "inapplicable");
            }
            dqQuantitativeResult.addElement("gmd:value/gco:Record").addText(igcResultValue);

        } else if (igcDqElementId.equals("120")) {
            // ---------- <gmd:DQ_DataQuality/gmd:report/gmd:DQ_TemporalConsistency> ----------

            if (!dqDataQuality) {
                dqDataQuality = addDataQualityInfoElement().addElement(getDqDataQualityElement(objClass));
            }
            var dqElem = dqDataQuality.addElement("gmd:report/gmd:DQ_TemporalConsistency");
            IDF_UTIL.addLocalizedCharacterstring(dqElem.addElement("gmd:nameOfMeasure"),igcNameOfMeasureValue);
            if (igcNameOfMeasureKey.equals("1")) {
                // Percentage of items that are correctly events ordered
                // -> INSPIRE: Measure identifier: There is no measure for temporal accuracy in ISO 19138
                dqElem.addElement("gmd:measureIdentification").addAttribute("gco:nilReason", "missing");
            }
            if (hasValue(igcMeasureDescription)) {
                dqElem.addElement("gmd:measureDescription/gco:CharacterString").addText(igcMeasureDescription);
            }
            var dqQuantitativeResult = dqElem.addElement("gmd:result/gmd:DQ_QuantitativeResult");
            if (igcNameOfMeasureKey.equals("1")) {
                var unitDefinition = dqQuantitativeResult.addElement("gmd:valueUnit/gml:UnitDefinition")
                    .addAttribute("gml:id", "unitDefinition_ID_".concat(TRANSF.getRandomUUID()));
                unitDefinition.addElement("gml:identifier").addAttribute("codeSpace", "");
                unitDefinition.addElement("gml:name").addText("percent");
                unitDefinition.addElement("gml:quantityType").addText("temporal consistency");
                unitDefinition.addElement("gml:catalogSymbol").addText("%");
            } else {
                dqQuantitativeResult.addElement("gmd:valueUnit").addAttribute("gco:nilReason", "unknown");
            }
            dqQuantitativeResult.addElement("gmd:value/gco:Record").addText(igcResultValue);

        } else if (igcDqElementId.equals("125")) {
            // ---------- <gmd:DQ_DataQuality/gmd:report/gmd:DQ_ThematicClassificationCorrectness> ----------

            if (!dqDataQuality) {
                dqDataQuality = addDataQualityInfoElement().addElement(getDqDataQualityElement(objClass));
            }
            var dqElem = dqDataQuality.addElement("gmd:report/gmd:DQ_ThematicClassificationCorrectness");
            IDF_UTIL.addLocalizedCharacterstring(dqElem.addElement("gmd:nameOfMeasure"),igcNameOfMeasureValue);
            if (igcNameOfMeasureKey.equals("1")) {
                // Misclassification rate
                dqElem.addElement("gmd:measureIdentification/gmd:MD_Identifier/gmd:code/gco:CharacterString").addText("61");
            }
            if (hasValue(igcMeasureDescription)) {
                dqElem.addElement("gmd:measureDescription/gco:CharacterString").addText(igcMeasureDescription);
            }
            var dqQuantitativeResult = dqElem.addElement("gmd:result/gmd:DQ_QuantitativeResult");
            if (igcNameOfMeasureKey.equals("1")) {
                var unitDefinition = dqQuantitativeResult.addElement("gmd:valueUnit/gml:UnitDefinition")
                    .addAttribute("gml:id", "unitDefinition_ID_".concat(TRANSF.getRandomUUID()));
                unitDefinition.addElement("gml:identifier").addAttribute("codeSpace", "");
                unitDefinition.addElement("gml:name").addText("percent");
                unitDefinition.addElement("gml:quantityType").addText("thematic classification correctness");
                unitDefinition.addElement("gml:catalogSymbol").addText("%");
            } else {
                dqQuantitativeResult.addElement("gmd:valueUnit").addAttribute("gco:nilReason", "unknown");
            }
            dqQuantitativeResult.addElement("gmd:value/gco:Record").addText(igcResultValue);

        } else if (igcDqElementId.equals("126")) {
            // ---------- <gmd:DQ_DataQuality/gmd:report/gmd:DQ_NonQuantitativeAttributeAccuracy> ----------

            if (!dqDataQuality) {
                dqDataQuality = addDataQualityInfoElement().addElement(getDqDataQualityElement(objClass));
            }
            var dqElem = dqDataQuality.addElement("gmd:report/gmd:DQ_NonQuantitativeAttributeAccuracy");
            IDF_UTIL.addLocalizedCharacterstring(dqElem.addElement("gmd:nameOfMeasure"),igcNameOfMeasureValue);
            if (igcNameOfMeasureKey.equals("1")) {
                // Rate of incorrect attributes names values
                dqElem.addElement("gmd:measureIdentification/gmd:MD_Identifier/gmd:code/gco:CharacterString").addText("67");
            }
            if (hasValue(igcMeasureDescription)) {
                dqElem.addElement("gmd:measureDescription/gco:CharacterString").addText(igcMeasureDescription);
            }
            var dqQuantitativeResult = dqElem.addElement("gmd:result/gmd:DQ_QuantitativeResult");
            if (igcNameOfMeasureKey.equals("1")) {
                var unitDefinition = dqQuantitativeResult.addElement("gmd:valueUnit/gml:UnitDefinition")
                    .addAttribute("gml:id", "unitDefinition_ID_".concat(TRANSF.getRandomUUID()));
                unitDefinition.addElement("gml:identifier").addAttribute("codeSpace", "");
                unitDefinition.addElement("gml:name").addText("percent");
                unitDefinition.addElement("gml:quantityType").addText("non quantitative attribute accuracy");
                unitDefinition.addElement("gml:catalogSymbol").addText("%");
            } else {
                dqQuantitativeResult.addElement("gmd:valueUnit").addAttribute("gco:nilReason", "unknown");
            }
            dqQuantitativeResult.addElement("gmd:value/gco:Record").addText(igcResultValue);

        } else if (igcDqElementId.equals("127")) {
            // ---------- <gmd:DQ_DataQuality/gmd:report/gmd:DQ_QuantitativeAttributeAccuracy> ----------

            if (!dqDataQuality) {
                dqDataQuality = addDataQualityInfoElement().addElement(getDqDataQualityElement(objClass));
            }
            var dqElem = dqDataQuality.addElement("gmd:report/gmd:DQ_QuantitativeAttributeAccuracy");
            IDF_UTIL.addLocalizedCharacterstring(dqElem.addElement("gmd:nameOfMeasure"),igcNameOfMeasureValue);
            if (igcNameOfMeasureKey.equals("1")) {
                // Attribute value uncertainty at 95 % significance level
                dqElem.addElement("gmd:measureIdentification/gmd:MD_Identifier/gmd:code/gco:CharacterString").addText("71");
            }
            if (hasValue(igcMeasureDescription)) {
                dqElem.addElement("gmd:measureDescription/gco:CharacterString").addText(igcMeasureDescription);
            }
            var dqQuantitativeResult = dqElem.addElement("gmd:result/gmd:DQ_QuantitativeResult");
            if (igcNameOfMeasureKey.equals("1")) {
                dqQuantitativeResult.addElement("gmd:valueUnit").addAttribute("gco:nilReason", "inapplicable");
            } else {
                dqQuantitativeResult.addElement("gmd:valueUnit").addAttribute("gco:nilReason", "unknown");
            }
            dqQuantitativeResult.addElement("gmd:value/gco:Record").addText(igcResultValue);
        } else if (igcDqElementId.equals("128")) {
            // ---------- <gmd:DQ_DataQuality/gmd:report/gmd:DQ_RelativeInternalPositionalAccuracy> ----------

            if (!dqDataQuality) {
                dqDataQuality = addDataQualityInfoElement().addElement(getDqDataQualityElement(objClass));
            }
            var dqElem = dqDataQuality.addElement("gmd:report/gmd:DQ_RelativeInternalPositionalAccuracy");
            dqElem.addElement("gmd:nameOfMeasure/gco:CharacterString").addText(igcNameOfMeasureValue);
            // if (igcNameOfMeasureKey.equals("1")) {
            //     // Attribute value uncertainty at 95 % significance level
            //     dqElem.addElement("gmd:measureIdentification/gmd:MD_Identifier/gmd:code/gco:CharacterString").addText("71");
            // }
            if (hasValue(igcMeasureDescription)) {
                dqElem.addElement("gmd:measureDescription/gco:CharacterString").addText(igcMeasureDescription);
            }
            var dqQuantitativeResult = dqElem.addElement("gmd:result/gmd:DQ_QuantitativeResult");
            var unitDefinition = dqQuantitativeResult.addElement("gmd:valueUnit/gml:UnitDefinition")
                .addAttribute("gml:id", "unitDefinition_ID_".concat(TRANSF.getRandomUUID()));
            unitDefinition.addElement("gml:identifier").addAttribute("codeSpace", "");
            unitDefinition.addElement("gml:name").addText("percent");
            unitDefinition.addElement("gml:quantityType").addText("relative external positional accuracy");
            unitDefinition.addElement("gml:catalogSymbol").addText("%");

            dqQuantitativeResult.addElement("gmd:value/gco:Record").addText(igcResultValue);
        }
    }
}


function getDqDataQualityElement(objClass) {
    var dqDataQuality = DOM.createElement("gmd:DQ_DataQuality");

    var dqScope = dqDataQuality.addElement("gmd:scope/gmd:DQ_Scope");
    
    dqScope.addElement("gmd:level/gmd:MD_ScopeCode")
        .addAttribute("codeListValue", getHierarchLevel(objClass))
        .addAttribute("codeList", globalCodeListAttrURL + "#MD_ScopeCode");

    // "levelDescription" is mandatory if "level" notEqual 'dataset' or 'series', see INGRID-2263
    if (objClass != "1") {
        dqScope.addElement("gmd:levelDescription/gmd:MD_ScopeDescription/gmd:other/gco:CharacterString").addText(objClass);
    }

if (log.isDebugEnabled()) {
    log.debug("DQ Mapping: Created gmd:DQ_DataQuality");
}

    return dqDataQuality;
}


function addDataQualityInfoElement() {
    var dqDataQualityInfo;

    if (dqPredecessorNode) {
        dqDataQualityInfo = dqPredecessorNode.addElementAsSibling("gmd:dataQualityInfo");
    } else {
    	// SHOULD NOT HAPPEN !!! Mandatory Element has to exist as predecessor !
        dqDataQualityInfo = mdMetadata.addElement("gmd:dataQualityInfo");
    }

    // next dataQualityInfo "after" this one !
    dqPredecessorNode = dqDataQualityInfo;

if (log.isDebugEnabled()) {
    log.debug("DQ Mapping: Added gmd:dataQualityInfo");
}

    return dqDataQualityInfo;
}
