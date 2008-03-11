package de.ingrid.mdek;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.weta.components.communication.ICommunication;
import net.weta.components.communication.reflect.ProxyService;
import net.weta.components.communication.reflect.ReflectMessageHandler;
import net.weta.components.communication.tcp.StartCommunication;
import net.weta.components.communication.tcp.TcpCommunication;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import de.ingrid.mdek.job.repository.IJobRepositoryFacade;

public class MdekServer implements IMdekServer {

    private final File _communicationProperties;

    private final IJobRepositoryFacade _jobRepositoryFacade;

    private ICommunication _communication;

    public MdekServer(File communicationProperties, IJobRepositoryFacade jobRepositoryFacade) {
        _communicationProperties = communicationProperties;
        _jobRepositoryFacade = jobRepositoryFacade;

    }

    public void run() throws IOException {
        while (true) {
            _communication = initCommunication(_communicationProperties);
            if (_communication instanceof TcpCommunication) {
                TcpCommunication tcpCom = (TcpCommunication) _communication;
                if (!tcpCom.isConnected((String) tcpCom.getServerNames().get(0))) {
                    try {
                        Thread.sleep(15000);
                    } catch (InterruptedException e) {
                        // ignore
                    }
                } else {
                    synchronized (MdekServer.class) {
                        try {
                            ProxyService.createProxyServer(_communication, IJobRepositoryFacade.class,
                                    _jobRepositoryFacade);
                            MdekServer.class.wait();
                        } catch (InterruptedException e) {
                            throw new IOException(e.getMessage());
                        } finally {
                            closeConnections();
                        }
                    }
                }
            }
        }
    }

    public void shutdown() {
        synchronized (MdekServer.class) {
            MdekServer.class.notifyAll();
        }
    }

    private void closeConnections() {
        try {
            _communication.closeConnection(null);
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
        System.err.println("Usage: " + MdekServer.class.getName() + "--descriptor <communication.properties>");
        System.exit(0);
    }

    public static void main(String[] args) throws IOException {
        Map map = readParameters(args);
        if (map.size() != 1) {
            printUsage();
        }

        String communicationFile = (String) map.get("--descriptor");

        ApplicationContext context = new ClassPathXmlApplicationContext(new String[] { "startup.xml" });
        IJobRepositoryFacade jobRepositoryFacade = (IJobRepositoryFacade) context.getBean(IJobRepositoryFacade.class
                .getName());
        MdekServer server = new MdekServer(new File(communicationFile), jobRepositoryFacade);
        server.run();
    }
}
