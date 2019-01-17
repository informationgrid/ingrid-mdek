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

import de.ingrid.mdek.job.MdekException;
import de.ingrid.mdek.job.mapping.ImportDataMapper;
import de.ingrid.mdek.job.protocol.ProtocolHandler;
import org.w3c.dom.Document;

import java.util.List;

abstract class AbstractIsoValidator implements ImportDataMapper<Document, Document> {
    @Override
    public void convert(Document sourceIso, Document igcIgnored, ProtocolHandler ph) throws MdekException {
        List<ValidationReportItem> report = validate(sourceIso);
        // Ignore infos. Only report errors and warnings.
        report.stream()
                .filter(e -> e.getLevel() == ValidationReportItem.ReportLevel.FAIL)
                .forEach(e -> {
                    ph.addMessage(ProtocolHandler.Type.ERROR, e.getMessage());
                });
        report.stream()
                .filter(e -> e.getLevel() == ValidationReportItem.ReportLevel.WARN)
                .forEach(e -> {
                    ph.addMessage(ProtocolHandler.Type.WARN, e.getMessage());
                });
    }

    abstract List<ValidationReportItem> validate(Document document);
}
