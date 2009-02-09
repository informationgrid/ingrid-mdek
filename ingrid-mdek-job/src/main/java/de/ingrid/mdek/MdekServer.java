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

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import de.ingrid.mdek.job.repository.IJobRepositoryFacade;

public class MdekServer implements IMdekServer {

    private static int _intervall = 30;

    private final File _communicationProperties;

    private final IJobRepositoryFacade _jobRepositoryFacade;

    private ICommunication _communication;
    
    private volatile boolean _shutdown = false;

    public MdekServer(File communicationProperties, IJobRepositoryFacade jobRepositoryFacade) {
        _communicationProperties = communicationProperties;
        _jobRepositoryFacade = jobRepositoryFacade;

    }

    public void run() throws IOException {
        _communication = initCommunication(_communicationProperties);
        ProxyService.createProxyServer(_communication, IJobRepositoryFacade.class, _jobRepositoryFacade);
        waitForConnection(_intervall);
        while (!_shutdown) {
            if (_communication instanceof TcpCommunication) {
                TcpCommunication tcpCom = (TcpCommunication) _communication;
                if (!tcpCom.isConnected((String) tcpCom.getServerNames().get(0))) {
                    closeConnections();
                    _communication = initCommunication(_communicationProperties);
                    ProxyService.createProxyServer(_communication, IJobRepositoryFacade.class, _jobRepositoryFacade);
                    waitForConnection(_intervall);
                }

                synchronized (MdekServer.class) {
                    try {
                        MdekServer.class.wait(10000);
                    } catch (InterruptedException e) {
                        closeConnections();
                        throw new IOException(e.getMessage());
                    }
                }
            }
        }
    }

    private void waitForConnection(int retries) {
        int count = 0;
        if (_communication instanceof TcpCommunication) {
            TcpCommunication tcpCom = (TcpCommunication) _communication;
            while (!tcpCom.isConnected((String) tcpCom.getServerNames().get(0)) && (count < retries)) {
                synchronized (MdekServer.class) {
                    try {
                        MdekServer.class.wait(1000);
                    } catch (InterruptedException e) {
                        // ignore
                    }
                }
                count++;
            }
        }        
    }

    public void shutdown() {
        _shutdown = true;
        synchronized (MdekServer.class) {
            MdekServer.class.notifyAll();
        }
    }

    private void closeConnections() {
        try {
            List registeredMdekServers = getRegisteredMdekServers();
            for (Object mdekServerName : registeredMdekServers) {
                _communication.closeConnection((String) mdekServerName);
            }
        } catch (Exception e) {
            // ignore this
        }
        _communication.shutdown();
        _communication = null;
    }

    private ICommunication initCommunication(File properties) throws IOException {
        FileInputStream confIS = new FileInputStream(properties);
        ICommunication communication = StartCommunication.create(confIS);
        communication.startup();

        return communication;
    }

    private static Map readParameters(String[] args) {
        Map<String, String> argumentMap = new HashMap<String, String>();
        for (int i = 0; i < args.length; i = i + 2) {
            argumentMap.put(args[i], args[i + 1]);
        }
        return argumentMap;
    }

    private static void printUsage() {
        System.err.println("Usage: " + MdekServer.class.getName() + "--descriptor <communication.xml> --reconnectIntervall <seconds>");
        System.exit(0);
    }

    public static void main(String[] args) throws IOException {
        Map map = readParameters(args);
        if (map.size() < 1 || map.size() > 2) {
            printUsage();
        }

        String communicationFile = (String) map.get("--descriptor");
        
        if (map.containsKey("--reconnectIntervall")) {
            String intervall = (String) map.get("--reconnectIntervall");
            _intervall = Integer.parseInt(intervall);
        }
        

        ApplicationContext context = new ClassPathXmlApplicationContext(new String[] { "startup.xml" });
        IJobRepositoryFacade jobRepositoryFacade = (IJobRepositoryFacade) context.getBean(IJobRepositoryFacade.class
                .getName());
        MdekServer server = new MdekServer(new File(communicationFile), jobRepositoryFacade);
        server.run();
    }

    private List<String> getRegisteredMdekServers() {
        List<String> result = new ArrayList<String>();
        if (_communication instanceof TcpCommunication) {
            TcpCommunication tcpCom = (TcpCommunication) _communication;
            result = tcpCom.getRegisteredClients();
        }
        return result;
    }
}
