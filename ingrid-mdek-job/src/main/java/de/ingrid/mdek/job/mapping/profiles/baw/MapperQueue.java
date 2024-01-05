/*-
 * **************************************************-
 * InGrid mdek-job
 * ==================================================
 * Copyright (C) 2014 - 2024 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.2 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
package de.ingrid.mdek.job.mapping.profiles.baw;

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
