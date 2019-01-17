/*-
 * **************************************************-
 * InGrid mdek-job
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
package de.ingrid.mdek.job.validation.iso.bawdmqs;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Node;
import org.dom4j.io.DOMReader;

import java.util.List;

/**
 * Interface for defining rules for validating ISO 19115:2003/Corrigendum
 * 1:2006(E) XML-files.
 *
 * @author Vikram Notay
 */
public final class ISO_19115_2003_ConditionsValidator extends AbstractIsoValidator {

    private static final Logger LOG = Logger.getLogger(ISO_19115_2003_ConditionsValidator.class);

    public ISO_19115_2003_ConditionsValidator() {
    }

    @Override
    List<ValidationReportItem> validate(org.w3c.dom.Document w3cDoc) {
        DOMReader reader = new DOMReader();
        Document dom4jDoc = reader.read(w3cDoc);
        ValidationReportHelper reportHelper = new ValidationReportHelper();

        validateDataQualityWithScopeCodeEqualsDataset(dom4jDoc, reportHelper);
        validateHierarchyDatasetHasGeographicElement(dom4jDoc, reportHelper);
        validateDataQualityWithScopeDatasetOrSeriesHasStatement(dom4jDoc, reportHelper);
        validateLineageChildren(dom4jDoc, reportHelper);

        return reportHelper.getReport();
    }

    private void validateDataQualityWithScopeCodeEqualsDataset(Document dom4jDoc, ValidationReportHelper reportHelper) {
        String tagName = ValidationReportHelper.getLocalisedString("validation.iso.tag.dataQualityInfo", "dataQualityInfo");
        String dqXpath = "/gmd:MD_Metadata/gmd:dataQualityInfo/gmd:DQ_DataQuality[./gmd:scope/gmd:DQ_Scope/gmd:level/gmd:MD_ScopeCode/@codeListValue='dataset']";
        List<Node> dqNodes = dom4jDoc.selectNodes(dqXpath);
        if (dqNodes == null || dqNodes.isEmpty()) return; // Nothing to validate

        dqNodes.forEach(dq -> {
            String childXpath = "./gmd:lineage|./gmd:report";
            List<Node> children = dq.selectNodes(childXpath);
            if (children == null || children.isEmpty()) {
                reportHelper.fail(
                        "validation.iso.dataqualityinfo.lineage_or_report.missing.descendant",
                        "Invalid dataQualityInfo",
                        tagName,
                        dq.getUniquePath());
            } else {
                reportHelper.pass(
                        "validation.iso.dataqualityinfo.lineage_or_report.valid",
                        "Valid dataQualityInfo",
                        tagName,
                        dq.getUniquePath());
            }
        });
    }

    /*
     * From ISO (MD_Identification):
     * MD_Metadata.hierarchyLevel = "dataset" implies
     * count(extent.geographicElement.EX_GeographicBoundingBox) + count(extent.geographicElement.EX_GeographicDescription) >= 1
     */
    private void validateHierarchyDatasetHasGeographicElement(Document dom4jDoc, ValidationReportHelper reportHelper) {
        String hlXpath = "/gmd:MD_Metadata[./gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue='dataset']";
        List<Node> hlNodes = dom4jDoc.selectNodes(hlXpath);
        if (hlNodes == null || hlNodes.isEmpty()) return; // No testing needed

        String xpath = hlXpath + "/gmd:identificationInfo/*/gmd:extent/gmd:EX_Extent/gmd:geographicElement[./gmd:EX_GeographicDescription|./gmd:EX_GeographicBoundingBox]";
        List<Node> nodes = dom4jDoc.selectNodes(xpath);
        if (nodes == null || nodes.isEmpty()) {
            reportHelper.fail(
                    "validation.iso.conditional.dataset.geographic_identifier.missing",
                    "Hierarchy level dataset has valid geographic identifier.");
        } else {
            reportHelper.pass(
                    "validation.iso.conditional.dataset.geographic_identifier.present",
                    "Hierarchy level dataset without valid geographic identifier.");
        }
    }

    /*
     * From ISO (LI_Lineage):
     * if (count(source) + count(processStep) = 0) and
     *         (DQ_DataQuality.scope = "dataset" or "series") then
     * statement is mandatory
     */
    private void validateDataQualityWithScopeDatasetOrSeriesHasStatement(Document dom4jDoc, ValidationReportHelper reportHelper) {
        String dqXpath = "/gmd:MD_Metadata/gmd:dataQualityInfo/gmd:DQ_DataQuality[./gmd:scope/gmd:DQ_Scope/gmd:level/gmd:MD_ScopeCode/@codeListValue='dataset']"
                      + "|/gmd:MD_Metadata/gmd:dataQualityInfo/gmd:DQ_DataQuality[./gmd:scope/gmd:DQ_Scope/gmd:level/gmd:MD_ScopeCode/@codeListValue='series']";
        List<Node> dqNodes = dom4jDoc.selectNodes(dqXpath);
        if (dqNodes == null || dqNodes.isEmpty()) return; // Nothing to validate

        // source/processStep descendants
        dqNodes.forEach(dq -> {
            String descendantXpath = "./gmd:lineage/gmd:LI_Lineage[./gmd:source|./gmd:processStep]";
            List<Node> descendantNodes = dq.selectNodes(descendantXpath);

            if (descendantNodes == null || descendantNodes.isEmpty()) return; // Again, condition doesn't apply

            // Check if statement is also present
            String statementXpath = "./gmd:lineage/gmd:LI_Lineage/gmd:statement";
            Node statementNode = dq.selectSingleNode(statementXpath);
            if (statementNode == null) {
                reportHelper.fail(
                        "validation.iso.conditional.dataset_series_statement.invalid",
                        "DataQuality element is invalid",
                        dq.getUniquePath());
            } else {
                reportHelper.pass(
                        "validation.iso.conditional.dataset_series_statement.valid",
                        "DataQuality element is valid");
            }
        });
    }

    /*
     * At least one out of source, statement and processStep must be defined in LI_Lineage elements
     */
    private void validateLineageChildren(Document dom4jDoc, ValidationReportHelper reportHelper) {
        List<Node> lineage = dom4jDoc.selectNodes("//gmd:LI_Lineage");
        lineage.forEach(l -> {
            String xpath = "./gmd:source|./gmd:statement|./gmd:processStep";
            List<Node> children = l.selectNodes(xpath);
            if (children == null || children.isEmpty()) {
                reportHelper.fail(
                        "validation.iso.conditional.lineage.children",
                        "Invalid LI_Lineage element",
                        l.getUniquePath());
            }
        });
    }

}
