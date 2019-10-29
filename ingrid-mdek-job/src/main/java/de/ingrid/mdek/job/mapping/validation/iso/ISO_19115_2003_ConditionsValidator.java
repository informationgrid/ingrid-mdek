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
package de.ingrid.mdek.job.mapping.validation.iso;

import de.ingrid.mdek.job.MdekException;
import de.ingrid.mdek.job.mapping.ImportDataMapper;
import de.ingrid.mdek.job.mapping.validation.iso.util.IsoImportValidationUtil;
import de.ingrid.mdek.job.protocol.ProtocolHandler;
import org.apache.log4j.Logger;
import org.dom4j.Node;

import java.util.List;

import static de.ingrid.mdek.job.mapping.validation.iso.util.IsoImportValidationUtil.ISO_ELEMENTS_RESOURCE_BUNDLE;
import static de.ingrid.mdek.job.mapping.validation.iso.util.IsoImportValidationUtil.ISO_MESSAGES_RESOURCE_BUNDLE;
import static de.ingrid.mdek.job.mapping.validation.iso.util.IsoImportValidationUtil.ValidationType.ONE_OR_MORE_DESCENDANTS_EXIST;
import static de.ingrid.mdek.job.mapping.validation.iso.util.IsoImportValidationUtil.ValidationType.TEXT_CONTENT_NOT_EMPTY;

/**
 * Validation rules for conditions defined in ISO 19115:2003/Corrigendum
 * 1:2006(E) XML-files.
 *
 * @author Vikram Notay
 */
public final class ISO_19115_2003_ConditionsValidator implements ImportDataMapper<org.w3c.dom.Document, org.w3c.dom.Document> {

    private static final Logger LOG = Logger.getLogger(ISO_19115_2003_ConditionsValidator.class);

    public ISO_19115_2003_ConditionsValidator() {
    }

    @Override
    public void convert(org.w3c.dom.Document sourceIso, org.w3c.dom.Document igcIgnored, ProtocolHandler ph) throws MdekException {
        IsoImportValidationUtil validator = new IsoImportValidationUtil(sourceIso, ph, ISO_ELEMENTS_RESOURCE_BUNDLE, ISO_MESSAGES_RESOURCE_BUNDLE);

        checkDataQualityElementsHaveNecessaryChildren(validator);
        validateHierarchyDatasetHasGeographicElement(validator);
        validateDataQualityWithScopeDatasetOrSeriesHasStatement(validator);
        validateLineageChildren(validator);
    }

    /*
     * From ISO (UML diagram A.4):
     * "report" or "lineage" role is mandatory
     * if scope.DQ_Scope.level = 'dataset'
     */
    private void checkDataQualityElementsHaveNecessaryChildren(IsoImportValidationUtil validator) {
        String tagKey = "iso.dataQualityInfo.18";
        String xpath = "/gmd:MD_Metadata/gmd:dataQualityInfo/gmd:DQ_DataQuality[./gmd:scope/gmd:DQ_Scope/gmd:level/gmd:MD_ScopeCode/@codeListValue='dataset']";
        List<Node> nodes = validator.selectNodes(xpath);

        String childXpath = "./gmd:lineage|./gmd:report";
        for(Node n: nodes) {
            validator.validate(n, childXpath, tagKey, "", ONE_OR_MORE_DESCENDANTS_EXIST);
        }
    }

    /*
     * From ISO (MD_Identification):
     * MD_Metadata.hierarchyLevel = "dataset" implies
     * count(extent.geographicElement.EX_GeographicBoundingBox) + count(extent.geographicElement.EX_GeographicDescription) >= 1
     */
    private void validateHierarchyDatasetHasGeographicElement(IsoImportValidationUtil validator) {
        String xpath = "/gmd:MD_Metadata[./gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue='dataset']";
        String tagKey = "iso.geographicElement.336";
        List<Node> nodes = validator.selectNodes(xpath);

        String geXpath = "./gmd:identificationInfo/*/gmd:extent/gmd:EX_Extent/gmd:geographicElement";
        String childXpath = geXpath + "/gmd:EX_GeographicDescription|" + geXpath + "/gmd:EX_GeographicBoundingBox";
        for(Node n: nodes) {
            validator.validate(n, childXpath, tagKey, "", ONE_OR_MORE_DESCENDANTS_EXIST);
        }
    }

    /*
     * From ISO (LI_Lineage):
     * if (count(source) + count(processStep) = 0) and
     *         (DQ_DataQuality.scope = "dataset" or "series") then
     * statement is mandatory
     */
    private void validateDataQualityWithScopeDatasetOrSeriesHasStatement(IsoImportValidationUtil validator) {
        String dqXpath = "/gmd:MD_Metadata/gmd:dataQualityInfo/gmd:DQ_DataQuality[./gmd:scope/gmd:DQ_Scope/gmd:level/gmd:MD_ScopeCode/@codeListValue='dataset']"
                      + "|/gmd:MD_Metadata/gmd:dataQualityInfo/gmd:DQ_DataQuality[./gmd:scope/gmd:DQ_Scope/gmd:level/gmd:MD_ScopeCode/@codeListValue='series']";
        String tagKey = "iso.dataQualityInfo.18";
        List<Node> dqNodes = validator.selectNodes(dqXpath);
        for(Node dq: dqNodes) {
            String descendantXpath = "./gmd:lineage/gmd:LI_Lineage[./gmd:source|./gmd:processStep]";
            List<Node> descendantNodes = validator.selectNodes(dq, descendantXpath);

            String statementXpath = "./gmd:lineage/gmd:LI_Lineage/gmd:statement";

            if (descendantNodes.isEmpty()) {
                validator.validate(dq, statementXpath, tagKey, "", ONE_OR_MORE_DESCENDANTS_EXIST, TEXT_CONTENT_NOT_EMPTY);
            }
        }
    }

    /*
     * At least one out of source, statement and processStep must be defined in LI_Lineage elements
     */
    private void validateLineageChildren(IsoImportValidationUtil validator) {
        String tagKey = "iso.lineage.81";
        String xpath = "/gmd:MD_Metadata/gmd:dataQualityInfo/gmd:DQ_DataQuality/gmd:lineage/gmd:LI_Lineage";
        List<Node> lineageNodes = validator.selectNodes(xpath);

        String descendantXpath = "./gmd:source|./gmd:statement|./gmd:processStep";

        for(Node lineage: lineageNodes) {
            validator.validate(lineage, descendantXpath, tagKey, "", ONE_OR_MORE_DESCENDANTS_EXIST, TEXT_CONTENT_NOT_EMPTY);
        }
    }

}
