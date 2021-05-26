/*-
 * **************************************************-
 * InGrid mdek-job
 * ==================================================
 * Copyright (C) 2014 - 2021 wemove digital solutions GmbH
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
package de.ingrid.mdek.job.mapping;

import de.ingrid.iplug.dsc.om.DatabaseSourceRecord;
import de.ingrid.utils.xml.ConfigurableNamespaceContext;
import de.ingrid.utils.xml.IDFNamespaceContext;
import de.ingrid.utils.xml.IgcProfileNamespaceContext;
import de.ingrid.utils.xml.XMLUtils;
import de.ingrid.utils.xpath.XPathUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


@RunWith(PowerMockRunner.class)
@PowerMockIgnore("javax.management.*")
public class IsoMetadataDateMapperTest extends IgcDbUnitEnabledTestCase {

    DocumentBuilder builder;
    XPathUtils xpathUtils;

    public IsoMetadataDateMapperTest(String name) {
        super(name);
        this.datasourceFileName = "src/test/resources/de/ingrid/mdek/job/mapping/db.test.xml";
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        DocumentBuilderFactory domFactory = DocumentBuilderFactory
                .newInstance();
        domFactory.setNamespaceAware(true);
        try {
            builder = domFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            new RuntimeException("Error creating DocumentBuilder", e);
        }

        ConfigurableNamespaceContext cnc = new ConfigurableNamespaceContext();
        cnc.addNamespaceContext(new IDFNamespaceContext());
        cnc.addNamespaceContext(new IgcProfileNamespaceContext());

        xpathUtils = new XPathUtils(cnc);

    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void map() throws Exception {

        Connection conn = this.getConnection().getConnection();

        DatabaseSourceRecord dsr = new DatabaseSourceRecord("1", conn);
        String idfString = new String(Files.readAllBytes(Paths.get("src/test/resources/de/ingrid/mdek/job/mapping/idf.xml")));
        Document idfDoc = builder.parse(new InputSource(new StringReader(idfString)));

        IsoMetadataDateMapper imdm = new IsoMetadataDateMapper();
        imdm.map(dsr, idfDoc);

        String nowDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        // Caution: Can cause failures when run on day boundary. Sorry do not know how to mock LocalDateTime.
        Assert.assertEquals("Date in XML must have been set to the current date.", nowDate, xpathUtils.getString(idfDoc, "/idf:html/idf:body/idf:idfMdMetadata/gmd:dateStamp/gco:Date").substring(0, 10));
        try (PreparedStatement p = conn.prepareStatement("SELECT * FROM t01_object WHERE ID=1")) {
            ResultSet rs = p.executeQuery();
            rs.next();
            String testSQLDate = rs.getString(3);
            Assert.assertEquals("Date in database must be set to the current date.", nowDate.replace("-", "").substring(0, 8), testSQLDate.substring(0, 8));
        }
    }

    @Test
    public void mapFingerprintNotChanged() throws Exception {

        Connection conn = this.getConnection().getConnection();
        DatabaseSourceRecord dsr = new DatabaseSourceRecord("2", conn);
        String idfString = new String(Files.readAllBytes(Paths.get("src/test/resources/de/ingrid/mdek/job/mapping/idf.xml")));
        Document idfDoc = builder.parse(new InputSource(new StringReader(idfString)));

        IsoMetadataDateMapper imdm = new IsoMetadataDateMapper();
        Document iso = imdm.idf2iso(idfDoc);
        imdm.prepareIsoForFingerprinting(iso);
        String fp = imdm.createFingerprint(iso);
        String testDate = xpathUtils.getString(idfDoc, "/idf:html/idf:body/idf:idfMdMetadata/gmd:dateStamp/gco:Date");
        imdm.map(dsr, idfDoc);

        Assert.assertEquals("Date in XML must be unchanged with identical fingerprints. (Fingerprint: '"+fp+"')", testDate, xpathUtils.getString(idfDoc, "/idf:html/idf:body/idf:idfMdMetadata/gmd:dateStamp/gco:Date"));
        try (PreparedStatement p = conn.prepareStatement("SELECT * FROM t01_object WHERE ID=2")) {
            ResultSet rs = p.executeQuery();
            rs.next();
            String testSQLDate = rs.getString(3);
            Assert.assertEquals("Date in database must be unchanged with identical fingerprints.", "20191217000000000", testSQLDate);
        }
    }

    @Test
    public void mapNothingChangedForNonPublishedDatasets() throws Exception {

        Connection conn = this.getConnection().getConnection();
        DatabaseSourceRecord dsr = new DatabaseSourceRecord("3", conn);
        String idfString = new String(Files.readAllBytes(Paths.get("src/test/resources/de/ingrid/mdek/job/mapping/idf.xml")));
        Document idfDoc = builder.parse(new InputSource(new StringReader(idfString)));

        IsoMetadataDateMapper imdm = new IsoMetadataDateMapper();
        Document iso = imdm.idf2iso(idfDoc);
        imdm.prepareIsoForFingerprinting(iso);
        String fp = imdm.createFingerprint(iso);
        String testDate = xpathUtils.getString(idfDoc, "/idf:html/idf:body/idf:idfMdMetadata/gmd:dateStamp/gco:Date");
        imdm.map(dsr, idfDoc);

        Assert.assertEquals("Date in XML must be unchanged with non-published datasets.", testDate, xpathUtils.getString(idfDoc, "/idf:html/idf:body/idf:idfMdMetadata/gmd:dateStamp/gco:Date"));
        try (PreparedStatement p = conn.prepareStatement("SELECT * FROM t01_object WHERE ID=3")) {
            ResultSet rs = p.executeQuery();
            rs.next();
            String testSQLDate = rs.getString("METADATA_TIME");
            Assert.assertEquals("Date in database must be unchanged with non-published datasets.", "20191217000000000", testSQLDate);
            String isoHash = rs.getString("ISO_HASH");
            Assert.assertEquals("Fingerprint must not be changed with non-published datasets.", "NOT_TO_BE_CHANGED", isoHash);
        }
    }

    @Test
    public void checkIsoForFingerprintPreparation() throws Exception {
        IsoMetadataDateMapper imdm = new IsoMetadataDateMapper();

        String idfString = new String(Files.readAllBytes(Paths.get("src/test/resources/de/ingrid/mdek/job/mapping/idf.xml")));
        Document idfDoc = builder.parse(new InputSource(new StringReader(idfString)));
        Document iso = null;

        for (String xpath : IsoMetadataDateMapper.ELEMENTS_REMOVED_FROM_ISO_BEFORE_FINGERPRINTING) {
            iso = imdm.idf2iso(idfDoc);
            boolean a = xpathUtils.nodeExists(iso, xpath);
            imdm.prepareIsoForFingerprinting(iso);
            boolean b = xpathUtils.nodeExists(iso, xpath);
            Assert.assertNotEquals("Element '" + xpath + "' should have been removed.", a, b);
        }
    }

    @Test
    public void checkFingerprinting() throws Exception {
        IsoMetadataDateMapper imdm = new IsoMetadataDateMapper();

        String idfString = new String(Files.readAllBytes(Paths.get("src/test/resources/de/ingrid/mdek/job/mapping/idf.xml")));
        Document idfDoc = builder.parse(new InputSource(new StringReader(idfString)));
        Document iso = imdm.idf2iso(idfDoc);

        String idfString2 = new String(Files.readAllBytes(Paths.get("src/test/resources/de/ingrid/mdek/job/mapping/idf.xml")));
        Document idfDoc2 = builder.parse(new InputSource(new StringReader(idfString)));
        Document iso2 = imdm.idf2iso(idfDoc);

        // create fingerprints from identical XML docs
        Assert.assertEquals("Fingerprints should be identical.", imdm.createFingerprint(iso), imdm.createFingerprint(iso2));

        // create fingerprints from different XML docs
        imdm.prepareIsoForFingerprinting(iso2);
        Assert.assertNotEquals("Fingerprints should be different.", imdm.createFingerprint(iso), imdm.createFingerprint(iso2));

        // change data that is removed by fingerprint preparation (dateStamp will be removed)
        XMLUtils.createOrReplaceTextNode(xpathUtils.getNode(iso, "/gmd:MD_Metadata/gmd:dateStamp/gco:Date"), "2020-03-23");
        imdm.prepareIsoForFingerprinting(iso);
        Assert.assertEquals("Fingerprints should be identical after changing to be removed elements.", imdm.createFingerprint(iso), imdm.createFingerprint(iso2));
    }

    @Test
    public void checkIdf2Iso() throws Exception {
        IsoMetadataDateMapper imdm = new IsoMetadataDateMapper();

        String idfString = new String(Files.readAllBytes(Paths.get("src/test/resources/de/ingrid/mdek/job/mapping/idf2.xml")));
        Document idfDoc = builder.parse(new InputSource(new StringReader(idfString)));
        Document iso = imdm.idf2iso(idfDoc);

        Assert.assertEquals("Date in XML must exist.", true, xpathUtils.nodeExists(idfDoc, "/idf:html/idf:body/idf:idfMdMetadata/gmd:dateStamp/gco:Date"));

    }

    @Test
    public void mapDateTime() throws Exception {

        Connection conn = this.getConnection().getConnection();

        DatabaseSourceRecord dsr = new DatabaseSourceRecord("4", conn);
        String idfString = new String(Files.readAllBytes(Paths.get("src/test/resources/de/ingrid/mdek/job/mapping/idf3_datetime.xml")));
        Document idfDoc = builder.parse(new InputSource(new StringReader(idfString)));

        IsoMetadataDateMapper imdm = new IsoMetadataDateMapper();
        imdm.map(dsr, idfDoc);

        String nowDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        // Caution: Can cause failures when run on day boundary. Sorry do not know how to mock LocalDateTime.
        Assert.assertEquals("Date in XML must have been set to the current date.", nowDate, xpathUtils.getString(idfDoc, "/idf:html/idf:body/idf:idfMdMetadata/gmd:dateStamp/gco:DateTime").substring(0, 10));
        try (PreparedStatement p = conn.prepareStatement("SELECT * FROM t01_object WHERE ID=4")) {
            ResultSet rs = p.executeQuery();
            rs.next();
            String testSQLDate = rs.getString(3);
            Assert.assertEquals("Date in database must be set to the current date.", nowDate.replace("-", "").substring(0, 8), testSQLDate.substring(0, 8));
        }
    }

    @Test
    public void mapNoValidNode() throws Exception {

        Connection conn = this.getConnection().getConnection();

        DatabaseSourceRecord dsr = new DatabaseSourceRecord("5", conn);
        String idfString = new String(Files.readAllBytes(Paths.get("src/test/resources/de/ingrid/mdek/job/mapping/idf4_no_valid_node.xml")));
        Document idfDoc = builder.parse(new InputSource(new StringReader(idfString)));

        IsoMetadataDateMapper imdm = new IsoMetadataDateMapper();
        try {
            imdm.map(dsr, idfDoc);
            fail("Mapper must throw exception.");
        } catch (Exception e) {
        }
    }



}
