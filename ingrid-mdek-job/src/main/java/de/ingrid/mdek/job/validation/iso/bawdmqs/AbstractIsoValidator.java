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
