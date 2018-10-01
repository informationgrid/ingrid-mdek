package de.ingrid.mdek.job.mapping.bawdmqs;

import de.ingrid.mdek.MdekError;
import de.ingrid.mdek.job.MdekException;
import de.ingrid.mdek.job.mapping.ImportDataMapper;
import de.ingrid.mdek.job.protocol.ProtocolHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;

public class MapperQueue<S, T> implements ImportDataMapper<S, T> {
    private static final Log LOG = LogFactory.getLog(MapperQueue.class);

    private List<ImportDataMapper<S, T>> mappers;

    @Override
    public void convert(S source, T target, ProtocolHandler protocolHandler) throws MdekException {
        for(ImportDataMapper<S, T> m: mappers) {
            try {
                m.convert(source, target, protocolHandler);
            } catch(Exception ex) {
                String msg = "Could not convert from ISO-19115 to IGC format";
                LOG.error(msg, ex);
                throw new MdekException(new MdekError(MdekError.MdekErrorType.IMPORT_PROBLEM, msg));
            }
        }
    }

    public void setMappers(List<ImportDataMapper<S, T>> mappers) {
        this.mappers = mappers;
    }

}
