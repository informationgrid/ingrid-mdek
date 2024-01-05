/*
 * **************************************************-
 * ingrid-mdek-api
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
package de.ingrid.mdek;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.weta.components.communication.ICommunication;
import net.weta.components.communication.reflect.ProxyService;
import net.weta.components.communication.tcp.StartCommunication;
import net.weta.components.communication.tcp.TcpCommunication;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.ingrid.mdek.job.repository.IJobRepositoryFacade;

public class MdekClient {

    private static final Logger LOG = LogManager.getLogger(MdekClient.class);

    private static MdekClient _client = null;

    private static ICommunication _communication;

    private MdekClient() {
        // singleton
    }

    public static MdekClient getInstance(File communicationProperties) throws IOException {
        if (_client == null) {
            _client = new MdekClient();
            _communication = _client.initCommunication(communicationProperties);
        }
        return _client;
    }

    public IJobRepositoryFacade getJobRepositoryFacade(final String proxyServiceUrl) {
        return createRepositoryFacade(proxyServiceUrl);
    }

    public void shutdown() {
        try {
            List registeredMdekServers = getRegisteredMdekServers();
            for (Object mdekServerName : registeredMdekServers) {
                _communication.closeConnection((String) mdekServerName);
            }
        } catch (Exception e) {
            // ignore this
        }
        _communication.shutdown();
        _client = null;
    }

    private IJobRepositoryFacade createRepositoryFacade(final String proxyServiceUrl) {
        IJobRepositoryFacade repository = (IJobRepositoryFacade) ProxyService.createProxy(_communication,
                IJobRepositoryFacade.class, proxyServiceUrl);

        try {
            // We get a proxy from backend (JobRepositoryFacade from mdek-job if ok, something else if not)
            // toString() causes exception if proxy NOT OK, caused by wrong backend URL ! 
            repository.toString();
        } catch (Throwable t) {
            LOG.error("PROBLEMS CONNECTING TO: " + proxyServiceUrl);
            throw t;
        }

        return repository;
    }

    private ICommunication initCommunication(File properties) throws IOException {
        FileInputStream confIS = new FileInputStream(properties);
        ICommunication communication = StartCommunication.create(confIS);
        communication.startup();

        return communication;
    }

    public List<String> getRegisteredMdekServers() {
        List<String> result = new ArrayList();
        if (_communication instanceof TcpCommunication) {
            TcpCommunication tcpCom = (TcpCommunication) _communication;
            result = tcpCom.getRegisteredClients();
        }
        return result;
    }
}
