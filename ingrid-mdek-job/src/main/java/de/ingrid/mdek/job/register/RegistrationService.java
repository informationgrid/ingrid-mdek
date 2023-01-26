/*
 * **************************************************-
 * ingrid-mdek-job
 * ==================================================
 * Copyright (C) 2014 - 2023 wemove digital solutions GmbH
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
package de.ingrid.mdek.job.register;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.ingrid.mdek.job.IJob;

@Service
public class RegistrationService implements IRegistrationService {

    private static final Logger LOG = LogManager.getLogger( RegistrationService.class );

    private Map<String, IJob> _beanFactoryCache = new HashMap<String, IJob>();

    @Autowired
    public RegistrationService(IJob... jobs) {
        LOG.info( "Jobs count: " + jobs.length );
        for (IJob job : jobs) {
            _beanFactoryCache.put( job.getClass().getName(), job );
        }
    }

    @Deprecated
    public void register(String jobId, String xml, boolean persist) throws IOException {}

    @Deprecated
    public void deRegister(String jobId) {

    }

    public IJob getRegisteredJob(String jobId) {
        return (IJob) _beanFactoryCache.get( jobId );
    }

    /**
     * configure this method as init-method in spring config.xml. this method loads all persited jobs
     * 
     * @throws IOException
     */
    @Deprecated
    public void registerPersistedJobs() throws IOException {}

}
