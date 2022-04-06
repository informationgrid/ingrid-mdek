/*-
 * **************************************************-
 * InGrid mdek-job
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
package de.ingrid.mdek.job.mapping.profiles.baw;

import de.ingrid.iplug.dsc.om.DatabaseSourceRecord;
import de.ingrid.iplug.dsc.om.SourceRecord;
import de.ingrid.iplug.dsc.record.mapper.IIdfMapper;
import org.apache.log4j.Logger;
import org.springframework.core.annotation.Order;
import org.w3c.dom.Document;

@Order(2)
public class IgcToIdfMapperBawMis implements IIdfMapper {

    private static final Logger LOG = Logger.getLogger(IgcToIdfMapperBawMis.class);

    @Override
    public void map(SourceRecord sourceRecord, Document target) throws Exception {
        if (!(sourceRecord instanceof DatabaseSourceRecord)) {
            throw new IllegalArgumentException("Record is no DatabaseRecord!");
        }

        LOG.debug("Additional mapping from source record to idf document for baw_mis profile: " + sourceRecord);

        try {
            IgcToIdfHelperBaw helper = new IgcToIdfHelperBaw(sourceRecord, target);
            helper.logMissingMetadataContact();
            helper.addWaterwayInformation();

            helper.addLfsLinks();
            helper.addAuthorsAndPublishersNotInCatalogue();
            helper.addLiteratureCrossReference();
            helper.addHandleInformation();

            helper.setHierarchyLevelName();
            helper.addAuftragsInfos();
            helper.addBWaStrIdentifiers();
            helper.addBawKewordCatalogeKeywords();
            helper.addSimSpatialDimensionKeyword();
            helper.addSimModelMethodKeyword();
            helper.addSimModelTypeKeywords();
            helper.addTimestepSizeElement();
            helper.addDgsValues();
            helper.changeMetadataDateAsDateTime();
        } catch (Exception e) {
            LOG.error("Error mapping source record to idf document.", e);
            throw e;
        }
    }

}

