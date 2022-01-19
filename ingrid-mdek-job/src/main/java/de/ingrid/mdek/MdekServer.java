/*
 * **************************************************-
 * ingrid-mdek-job
 * ==================================================
 * Copyright (C) 2014 - 2022 wemove digital solutions GmbH
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

import de.ingrid.admin.JettyStarter;
import de.ingrid.mdek.caller.MdekCallerCatalog;
import de.ingrid.mdek.job.Configuration;
import de.ingrid.mdek.job.MdekException;
import de.ingrid.mdek.job.repository.IJobRepository;
import de.ingrid.mdek.job.repository.IJobRepositoryFacade;
import de.ingrid.mdek.job.repository.Pair;
import de.ingrid.utils.IngridDocument;
import net.weta.components.communication.ICommunication;
import net.weta.components.communication.reflect.ProxyService;
import net.weta.components.communication.tcp.StartCommunication;
import net.weta.components.communication.tcp.TcpCommunication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class MdekServer {

    private static int _intervall = 30;

    private static File _communicationProperties = null;
 
    private final IJobRepositoryFacade _jobRepositoryFacade;

    private ICommunication _communication;

    private static volatile boolean _shutdown = false;

    public static Configuration conf;
    private Configuration igeConfig;

    @Autowired
    public MdekServer(IJobRepositoryFacade jobRepositoryFacade, Configuration igeConfig) {
        _jobRepositoryFacade = jobRepositoryFacade;
        this.igeConfig = igeConfig;

        // backwards compatibility for UVP Mappig to IDF
        // (/distribution/src/profiles/uvp/conf/mapping/igc_to_idf_uvp.js)
        MdekServer.conf = igeConfig;

        IngridDocument response = callJob(jobRepositoryFacade, MdekCallerCatalog.MDEK_IDC_CATALOG_JOB_ID, "getCatalog", new IngridDocument());
        _intervall = igeConfig.reconnectInterval;
        _communicationProperties = new File("conf/communication-ige.xml");
        // check response, throws Exception if wrong version !
        checkResponse(response);
    }
    
    @PostConstruct
    public Thread runBackend() throws IOException {
        Thread thread = new CommunicationThread();
        if (igeConfig.igcEnableIBusCommunication){
            thread.start();
        }
        return thread;
    }
    
    // constructor used for tests
    public MdekServer(File communication, IJobRepositoryFacade jobRepositoryFacade) throws IOException {
        _communicationProperties = communication;
        _jobRepositoryFacade = jobRepositoryFacade;
    }

    private class CommunicationThread extends Thread {
        public void run() {
            try {
                _communication = initCommunication(_communicationProperties);
            
                ProxyService.createProxyServer(_communication, IJobRepositoryFacade.class, _jobRepositoryFacade);
                waitForConnection(_intervall);
                while (!_shutdown) {
                    if (_communication instanceof TcpCommunication) {
                        TcpCommunication tcpCom = (TcpCommunication) _communication;
                        // if no connection to ibus then try to connect again
                        if (!tcpCom.isConnected((String) tcpCom.getServerNames().get(0))) {
                            closeConnections();
                            _communication = initCommunication(_communicationProperties);
                            ProxyService.createProxyServer(_communication, IJobRepositoryFacade.class, _jobRepositoryFacade);
                            waitForConnection(_intervall);
                            
                        } else {
                            // if connected, then wait for 10s before checking the connection again
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
            } catch (IOException ex) {
                ex.printStackTrace();
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

    public static void shutdown() {
        _shutdown = true;
        synchronized (MdekServer.class) {
            MdekServer.class.notifyAll();
        }
    }

    private void closeConnections() {
        try {
            List<String> registeredMdekServers = getRegisteredMdekServers();
            for (String mdekServerName : registeredMdekServers) {
                _communication.closeConnection(mdekServerName);
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

    private static Map<String, String> readParameters(String[] args) {
        Map<String, String> argumentMap = new HashMap<String, String>();
        for (int i = 0; i < args.length; i = i + 2) {
            argumentMap.put(args[i], args[i + 1]);
        }
        return argumentMap;
    }

    public static void main(String[] args) throws Exception {
        Map<String, String> map = readParameters(args);
        
        // start the Webserver for admin-page and iplug initialization for search and index
        // this also initializes all spring services and does autowiring
        new JettyStarter(Configuration.class);

        _communicationProperties = getCommunicationFile((String) map.get("--descriptor"));
        
        // shutdown the server normally
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                try {
                    shutdown();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private static File getCommunicationFile(String communicationFile) throws IOException {
        File commFile = null;
        if (communicationFile == null) {
            if (new ClassPathResource( "communication-ige.xml" ).exists()) {
                commFile = new ClassPathResource( "communication-ige.xml" ).getFile();
            } else if ( new File("conf/communication-ige.xml").exists()) {
                commFile = new File("conf/communication-ige.xml");
            }
        } else {
            commFile = new File(communicationFile);
        }
        return commFile;
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
			
			System.exit( -1 );
		}
	}
}
