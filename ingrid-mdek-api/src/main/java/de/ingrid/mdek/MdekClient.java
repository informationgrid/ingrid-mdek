package de.ingrid.mdek;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.weta.components.communication.ICommunication;
import net.weta.components.communication.reflect.ProxyService;
import net.weta.components.communication.tcp.StartCommunication;
import net.weta.components.communication.tcp.TcpCommunication;
import de.ingrid.mdek.job.repository.IJobRepositoryFacade;

public class MdekClient {

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
            _communication.closeConnection(null);
        } catch (Exception e) {
            // ignore this
        }
        _communication.shutdown();
    }

    private IJobRepositoryFacade createRepositoryFacade(final String proxyServiceUrl) {
        IJobRepositoryFacade repository = (IJobRepositoryFacade) ProxyService.createProxy(_communication,
                IJobRepositoryFacade.class, proxyServiceUrl);
        return repository;
    }

    private ICommunication initCommunication(File properties) throws IOException {
        FileInputStream confIS = new FileInputStream(properties);
        ICommunication communication = StartCommunication.create(confIS);
        communication.startup();

        return communication;
    }

    public List getRegisteredMdekServers() {
        List result = new ArrayList();
        if (_communication instanceof TcpCommunication) {
            TcpCommunication tcpCom = (TcpCommunication) _communication;
            result = tcpCom.getRegisteredClients();
        }
        return result;
    }
}
