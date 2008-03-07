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
import de.ingrid.utils.IngridDocument;

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

        Thread.sleep(6000);

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

    @Test
    public void testMdekClientAsComServerWithNoMdekServer() throws InterruptedException, IOException, Exception {
        MdekClient mdekClient = MdekClient.getInstance(new File(File.class.getResource(
                "/communication-server.properties").toURI()));
        Thread.sleep(6000);
        Assert.assertNotNull(mdekClient);

        MdekServer temp = null;
        try {
            temp = new MdekServer(new File(File.class.getResource("/communication-client.properties").toURI()),
                    new JobRepositoryFacade(null));
        } catch (URISyntaxException e) {
            Assert.fail();
        }
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

        Thread.sleep(6000);

        List mdekServerList = mdekClient.getRegisteredMdekServers();
        Assert.assertNotNull(mdekServerList);
        Assert.assertEquals(1, mdekServerList.size());
        Assert.assertEquals("message-client", mdekServerList.get(0));
        IJobRepositoryFacade jobRepositoryFacade = mdekClient.getJobRepositoryFacade((String) mdekServerList.get(0));
        Assert.assertNotNull(jobRepositoryFacade);
        jobRepositoryFacade.execute(new IngridDocument());

        temp.shutdown();

        Thread.sleep(6000);

        try {
            jobRepositoryFacade.execute(new IngridDocument());
            Assert.fail();
        } catch (Exception e) {
            // ok
        }
        mdekServerList = mdekClient.getRegisteredMdekServers();
        Assert.assertNotNull(mdekServerList);
        Assert.assertEquals(0, mdekServerList.size());
        mdekClient.shutdown();
        
        temp.shutdown();
        Thread.sleep(6000);
    }
}
