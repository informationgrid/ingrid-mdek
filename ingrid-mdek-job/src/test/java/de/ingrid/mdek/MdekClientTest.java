/*
 * Created on 09.10.2007
 */
package de.ingrid.mdek;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import de.ingrid.mdek.job.repository.IJobRepositoryFacade;
import de.ingrid.mdek.job.repository.JobRepositoryFacade;

public class MdekClientTest {

    @Test
    public void testShutdown() throws Exception {
        MdekServer temp = null;
        try {
            temp = new MdekServer(new File(File.class.getResource("/communication-server.properties").toURI()),
                    new JobRepositoryFacade(null));
        } catch (URISyntaxException e1) {
            Assert.fail();
        }
        final MdekServer mdekServer = temp;
        Assert.assertNotNull(mdekServer);
        Thread server = new Thread(new Runnable() {
            public void run() {
                try {
                    mdekServer.run();
                } catch (IOException e1) {
                    Assert.fail();
                }
            }
        });
        server.start();
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
            mdekClient = MdekClient.getInstance(new File(File.class.getResource("/communication-client.properties")
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
        mdekClient.shutdown();

        temp.shutdown();
        Thread.sleep(6000);
    }

    @Test
    public void testMdekClientAsComServer() throws InterruptedException, IOException, Exception {
        MdekClient mdekClient = MdekClient.getInstance(new File(File.class.getResource(
                "/communication-server.properties").toURI()));
        Thread.sleep(6000);
        Assert.assertNotNull(mdekClient);

        MdekServer temp = null;
        try {
            temp = new MdekServer(new File(File.class.getResource("/communication-client.properties").toURI()),
                    new JobRepositoryFacade(null));
        } catch (URISyntaxException e1) {
            Assert.fail();
        }
        final MdekServer mdekServer = temp;
        Assert.assertNotNull(mdekServer);
        Thread server = new Thread(new Runnable() {
            public void run() {
                try {
                    mdekServer.run();
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

        temp.shutdown();
        Thread.sleep(6000);
    }

    @Test(expected = IOException.class)
    public void testMdekClientAsComServerWithNoMdekServer() throws Exception {
        MdekClient mdekClient = MdekClient.getInstance(new File(File.class.getResource(
                "/communication-server.properties").toURI()));
        Thread.sleep(6000);
        Assert.assertNotNull(mdekClient);

        try {
            Thread.sleep(6000);
            new Socket("localhost", 56561);
        } catch (IOException e) {
            Assert.fail();
        }

        MdekServer temp = new MdekServer(new File(File.class.getResource("/communication-client.properties").toURI()),
                new JobRepositoryFacade(null));
        final MdekServer mdekServer = temp;
        Assert.assertNotNull(mdekServer);
        Thread server = new Thread(new Runnable() {
            public void run() {
                try {
                    mdekServer.run();
                } catch (IOException e1) {
                    // ignore
                }
            }
        });
        server.start();
        Thread.sleep(15000);

        mdekClient.shutdown();
        Thread.sleep(1000);

        new Socket("localhost", 56561);

        Thread.sleep(10000);

        temp.shutdown();
        Thread.sleep(6000);
    }

    @Test
    public void testMdekServerAsComClientReconnection() throws Exception {
        MdekServer temp = new MdekServer(new File(File.class.getResource("/communication-client.properties").toURI()),
                new JobRepositoryFacade(null));
        final MdekServer mdekServer = temp;
        Assert.assertNotNull(mdekServer);
        Thread server = new Thread(new Runnable() {
            public void run() {
                try {
                    mdekServer.run();
                } catch (IOException e) {
                    // ignore
                }
            }
        });
        server.start();
        Thread.sleep(15000);

        MdekClient mdekClient = MdekClient.getInstance(new File(File.class.getResource(
                "/communication-server.properties").toURI()));
        Thread.sleep(15000);
        Assert.assertNotNull(mdekClient);
        Thread.sleep(15000);

        List registeredMdekServers = mdekClient.getRegisteredMdekServers();
        Assert.assertNotNull(registeredMdekServers);
        Assert.assertEquals(1, registeredMdekServers.size());

        mdekClient.shutdown();
        Thread.sleep(6000);

        mdekClient = MdekClient.getInstance(new File(File.class.getResource(
                "/communication-server.properties").toURI()));
        Thread.sleep(6000);
        Assert.assertNotNull(mdekClient);
        Thread.sleep(15000);

        registeredMdekServers = mdekClient.getRegisteredMdekServers();
        Assert.assertNotNull(registeredMdekServers);
        Assert.assertEquals(1, registeredMdekServers.size());

        mdekClient.shutdown();
        Thread.sleep(6000);

        temp.shutdown();
        Thread.sleep(6000);
    }
}
