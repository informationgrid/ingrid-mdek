/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.ingrid.mdek.job.mapping.bawdmqs;

import de.ingrid.mdek.MdekError;
import de.ingrid.mdek.job.MdekException;
import de.ingrid.mdek.job.mapping.ImportDataMapper;
import de.ingrid.mdek.job.protocol.ProtocolHandler;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author vikram
 */
public class SerialDataMapper<S, T> implements ImportDataMapper<S, T> {

    private static final Log LOG = LogFactory.getLog(SerialDataMapper.class);

    private List<ImportDataMapper<S, T>> mappers;

    @Override
    public void convert(S in, T target, ProtocolHandler protocolHandler) throws MdekException {
        mappers.forEach((m) -> {
            try {
                m.convert(in, target, protocolHandler);
            } catch(Exception ex) {
                String msg = "Could not convert from ISO-19115 to IGC format";
                LOG.error(msg, ex);
                throw new MdekException(new MdekError(MdekError.MdekErrorType.IMPORT_PROBLEM, msg));
            }
        });
    }

    public void setMappers(List<ImportDataMapper<S, T>> mappers) {
        this.mappers = mappers;
    }

}
