/*-
 * **************************************************-
 * InGrid mdek-job
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
package de.ingrid.mdek.job.mapping.validation.iso;

import de.ingrid.mdek.job.MdekException;
import de.ingrid.mdek.job.mapping.ImportDataMapper;
import de.ingrid.mdek.job.mapping.validation.iso.util.IsoImportValidationUtil;
import de.ingrid.mdek.job.protocol.ProtocolHandler;
import org.w3c.dom.Document;

import static de.ingrid.mdek.job.mapping.validation.iso.util.IsoImportValidationUtil.ISO_ELEMENTS_RESOURCE_BUNDLE;
import static de.ingrid.mdek.job.mapping.validation.iso.util.IsoImportValidationUtil.ISO_MESSAGES_RESOURCE_BUNDLE;

/**
 * Validator for validating ISO (19115:2003/Corrigendum 1:2006(E)) XML-files
 * against the ISO-19139 metadata schema.
 *
 * @author Vikram Notay
 */
public final class ISO_19115_2003_SchemaValidator implements ImportDataMapper<Document, Document> {

    public ISO_19115_2003_SchemaValidator() {
    }

    @Override
    public void convert(Document sourceIso, Document igcIgnored, ProtocolHandler ph) throws MdekException {
        IsoImportValidationUtil validator = new IsoImportValidationUtil(sourceIso, ph, ISO_ELEMENTS_RESOURCE_BUNDLE, ISO_MESSAGES_RESOURCE_BUNDLE);
        validator.validateXmlSchema(sourceIso);
    }

}
