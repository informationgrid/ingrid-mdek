/*
 * **************************************************-
 * ingrid-mdek-job
 * ==================================================
 * Copyright (C) 2014 wemove digital solutions GmbH
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

import de.ingrid.mdek.caller.MdekCallerCatalog;
import de.ingrid.mdek.job.MdekException;
import de.ingrid.mdek.job.repository.IJobRepository;
import de.ingrid.mdek.job.repository.IJobRepositoryFacade;
import de.ingrid.mdek.job.repository.Pair;
import de.ingrid.utils.IngridDocument;

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
        
        // call job checking Version of IGC in database !
        IngridDocument response = callJob(jobRepositoryFacade,
        	MdekCallerCatalog.MDEK_IDC_CATALOG_JOB_ID, "getCatalog", new IngridDocument());
        // check response, throws Exception if wrong version !
        checkResponse(response);

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

	static private IngridDocument callJob(IJobRepositoryFacade jobRepo,
			String jobId, String methodName, IngridDocument methodParams) {
		ArrayList<Pair> methodList = new ArrayList<Pair>();
		methodList.add(new Pair(methodName, methodParams));
		
		IngridDocument invokeDocument = new IngridDocument();
		invokeDocument.put(IJobRepository.JOB_ID, jobId);
		invokeDocument.put(IJobRepository.JOB_METHODS, methodList);
//		invokeDocument.putBoolean(IJobRepository.JOB_PERSIST, true);

		IngridDocument response = jobRepo.execute(invokeDocument);
		
		return response;
	}

	static private void checkResponse(IngridDocument mdekResponse) throws MdekException {
		boolean success = mdekResponse.getBoolean(IJobRepository.JOB_INVOKE_SUCCESS);
		if (!success) {
			int numErrorTypes = 4;
			String[] errMsgs = new String[numErrorTypes];

			errMsgs[0] = (String) mdekResponse.get(IJobRepository.JOB_REGISTER_ERROR_MESSAGE);
			errMsgs[1] = (String) mdekResponse.get(IJobRepository.JOB_INVOKE_ERROR_MESSAGE);
			errMsgs[2] = (String) mdekResponse.get(IJobRepository.JOB_COMMON_ERROR_MESSAGE);
			errMsgs[3] = (String) mdekResponse.get(IJobRepository.JOB_DEREGISTER_ERROR_MESSAGE);

			String retMsg = null;
			for (String errMsg : errMsgs) {
				if (errMsg != null) {
					if (retMsg == null) {
						retMsg = errMsg;
					} else {
						retMsg += "\n!!! Further Error !!!:\n" + errMsg;
					}
				}
			}
			
			throw new MdekException(retMsg);
		}
	}
}
