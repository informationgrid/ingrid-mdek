/*
 * **************************************************-
 * ingrid-mdek-job
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
/*
 * Created on 09.10.2007
 */
package de.ingrid.mdek;

import de.ingrid.mdek.job.Configuration;
import de.ingrid.mdek.job.DateJob;
import de.ingrid.mdek.job.repository.IJobRepository;
import de.ingrid.mdek.job.repository.IJobRepositoryFacade;
import de.ingrid.mdek.job.repository.JobRepositoryFacade;
import de.ingrid.mdek.job.repository.Pair;
import de.ingrid.utils.IngridDocument;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class MdekClientTest {

    @Test
    public void testShutdown() throws Exception {
        MdekServer temp = null;
        try {
            temp = new MdekServer(new File(MdekClientTest.class.getResource("/communication-server.properties").toURI()), new JobRepositoryFacade(null), new Configuration());
        } catch (URISyntaxException e1) {
            Assert.fail();
        }
        final MdekServer mdekServer = temp;
        Assert.assertNotNull(mdekServer);
        Thread server = mdekServer.runBackend();

        try {
            Thread.sleep(6000);
            new Socket("localhost", 56561);
        } catch (UnknownHostException e) {
            Assert.fail();
        } catch (IOException e) {
            Assert.fail();
        } catch (InterruptedException e) {
            Assert.fail();
        }

        MdekClient mdekClient = null;
        try {
            mdekClient = MdekClient.getInstance(new File(MdekClientTest.class.getResource("/communication-client.properties")
                    .toURI()));
            Thread.sleep(6000);
        } catch (IOException e) {
            Assert.fail();
        } catch (URISyntaxException e) {
            Assert.fail();
        } catch (InterruptedException e) {
            Assert.fail();
        }
        Assert.assertNotNull(mdekClient);
        IJobRepositoryFacade jobRepositoryFacade = mdekClient.getJobRepositoryFacade("message-server");
        Assert.assertNotNull(jobRepositoryFacade);

        MdekServer.shutdown();
        Thread.sleep(6000);
    }

    @Test
    public void testMdekClientAsComServer() throws InterruptedException, IOException, Exception {
        MdekClient mdekClient = MdekClient.getInstance(new File(MdekClientTest.class.getResource(
                "/communication-server.properties").toURI()));
        Thread.sleep(6000);
        Assert.assertNotNull(mdekClient);

        MdekServer temp = null;
        try {
            temp = new MdekServer(new File(MdekClientTest.class.getResource("/communication-client.properties").toURI()),
                    new JobRepositoryFacade(null),
                    new Configuration());
        } catch (URISyntaxException e1) {
            Assert.fail();
        }
        final MdekServer mdekServer = temp;
        Assert.assertNotNull(mdekServer);
        Thread server = new Thread(new Runnable() {
            public void run() {
                try {
                    mdekServer.runBackend();
                } catch (IOException e1) {
                    // ignore
                }
            }
        });
        server.start();

        Thread.sleep(15000);

        List mdekServerList = mdekClient.getRegisteredMdekServers();
        Assert.assertNotNull(mdekServerList);
        Assert.assertEquals(1, mdekServerList.size());
        Assert.assertEquals("message-client", (String) mdekServerList.get(0));
        IJobRepositoryFacade jobRepositoryFacade = mdekClient.getJobRepositoryFacade((String) mdekServerList.get(0));
        Assert.assertNotNull(jobRepositoryFacade);
        mdekClient.shutdown();

        MdekServer.shutdown();
        Thread.sleep(6000);
    }

    public void testMdekClientAsComServerWithNoMdekServer() throws Exception {
        Assertions.assertThrows(IOException.class, () -> {
            MdekClient mdekClient = MdekClient.getInstance(new File(MdekClientTest.class.getResource(
                    "/communication-server.properties").toURI()));
            Thread.sleep(6000);
            Assert.assertNotNull(mdekClient);

            try {
                Thread.sleep(6000);
                new Socket("localhost", 56561);
            } catch (IOException e) {
                Assert.fail();
            }

            MdekServer temp = new MdekServer(new File(MdekClientTest.class.getResource("/communication-client.properties").toURI()),
                    new JobRepositoryFacade(null),
                    new Configuration());
            final MdekServer mdekServer = temp;
            Assert.assertNotNull(mdekServer);
            Thread server = mdekServer.runBackend();
            Thread.sleep(15000);

            mdekClient.shutdown();
            Thread.sleep(1000);

            new Socket("localhost", 56561);

            Thread.sleep(10000);

            MdekServer.shutdown();
            Thread.sleep(6000);
        });
    }

    @Test
    public void testMdekServerAsComClientReconnection() throws Exception {
        MdekServer temp = new MdekServer(
                new File(MdekClientTest.class.getResource("/communication-client.xml").toURI()),
                new JobRepositoryFacade(null),
                new Configuration());
        final MdekServer mdekServer = temp;
        Assert.assertNotNull(mdekServer);
        Thread server = mdekServer.runBackend();
        Thread.sleep(15000);

        MdekClient mdekClient = MdekClient.getInstance(new File(MdekClientTest.class.getResource(
                "/communication-server.xml").toURI()));
        Thread.sleep(15000);
        Assert.assertNotNull(mdekClient);
        Thread.sleep(15000);

        List registeredMdekServers = mdekClient.getRegisteredMdekServers();
        Assert.assertNotNull(registeredMdekServers);
        Assert.assertEquals(1, registeredMdekServers.size());

        mdekClient.shutdown();
        Thread.sleep(6000);

        mdekClient = MdekClient.getInstance(new File(MdekClientTest.class.getResource(
                "/communication-server.xml").toURI()));
        Thread.sleep(6000);
        Assert.assertNotNull(mdekClient);
        Thread.sleep(15000);

        registeredMdekServers = mdekClient.getRegisteredMdekServers();
        Assert.assertNotNull(registeredMdekServers);
        Assert.assertEquals(1, registeredMdekServers.size());

        mdekClient.shutdown();
        Thread.sleep(6000);

        MdekServer.shutdown();
        Thread.sleep(6000);
    }

    @Test
    public void testMessageToBig() throws URISyntaxException, InterruptedException, IOException {
        MdekServer temp = new MdekServer(new File(MdekClientTest.class.getResource(
                "/communication-server_WithSmallMessageSize.properties").toURI()), new JobRepositoryFacade(null), new Configuration());
        final MdekServer mdekServer = temp;
        Assert.assertNotNull(mdekServer);
        Thread server = mdekServer.runBackend();
        Thread.sleep(15000);

        MdekClient mdekClient = null;
        mdekClient = MdekClient
                .getInstance(new File(MdekClientTest.class.getResource("/communication-client.properties").toURI()));
        Thread.sleep(6000);
        Assert.assertNotNull(mdekClient);
        IJobRepositoryFacade jobRepositoryFacade = mdekClient.getJobRepositoryFacade("message-server");
        Assert.assertNotNull(jobRepositoryFacade);

        IngridDocument invokeDocument = new IngridDocument();
        invokeDocument.put(IJobRepository.JOB_ID, DateJob.class.getName());
        List<Pair> methodList = new ArrayList<Pair>();
        methodList.add(new Pair("getResults", null));
        invokeDocument.put(IJobRepository.JOB_METHODS, methodList);
        invokeDocument.putBoolean(IJobRepository.JOB_PERSIST, true);
        try {
            jobRepositoryFacade.execute(invokeDocument);
            Assert.fail();
        } catch (Throwable e) {
            // expected
        }
        mdekClient.shutdown();
        mdekClient = MdekClient
                .getInstance(new File(MdekClientTest.class.getResource("/communication-client.properties").toURI()));
        Thread.sleep(6000);
        Assert.assertNotNull(mdekClient);
        jobRepositoryFacade = mdekClient.getJobRepositoryFacade("message-server");
        Assert.assertNotNull(jobRepositoryFacade);

        mdekClient.shutdown();

        temp.shutdown();
        Thread.sleep(6000);
    }
}
