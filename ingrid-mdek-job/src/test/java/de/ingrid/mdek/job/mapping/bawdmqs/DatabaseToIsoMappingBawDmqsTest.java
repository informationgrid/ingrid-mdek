package de.ingrid.mdek.job.mapping.bawdmqs;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dbunit.DBTestCase;
import org.dbunit.PropertiesBasedJdbcDatabaseTester;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.XmlDataSet;
import org.dbunit.ext.hsqldb.HsqldbDataTypeFactory;
import org.junit.Test;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.ingrid.iplug.dsc.om.DatabaseSourceRecord;
import de.ingrid.iplug.dsc.om.SourceRecord;
import de.ingrid.iplug.dsc.record.mapper.ScriptedIdfMapper;
import de.ingrid.iplug.dsc.utils.DOMUtils;
import de.ingrid.utils.xml.IDFNamespaceContext;
import de.ingrid.utils.xpath.XPathUtils;

public class DatabaseToIsoMappingBawDmqsTest extends DBTestCase {
    private static final Logger LOG = LogManager.getLogger(DatabaseToIsoMappingBawDmqsTest.class);
    private static final String DATASOURCE_FILE_NAME = "src/test/resources/export/baw/dataset_baw_dmqs_simulation.xml";
    
    private boolean needsInitalisation;
    private Document exportedDocument;
    private XPathUtils xpathUtils;
    private ScriptedIdfMapper m0;
    private SourceRecord mockSourceRecord;

    public DatabaseToIsoMappingBawDmqsTest() throws Exception {
        needsInitalisation = true;

        System.setProperty( PropertiesBasedJdbcDatabaseTester.DBUNIT_DRIVER_CLASS, "org.hsqldb.jdbcDriver" );
        System.setProperty( PropertiesBasedJdbcDatabaseTester.DBUNIT_CONNECTION_URL, "jdbc:hsqldb:mem:sample" );
        System.setProperty( PropertiesBasedJdbcDatabaseTester.DBUNIT_SCHEMA, "public");
        System.setProperty( PropertiesBasedJdbcDatabaseTester.DBUNIT_USERNAME, "sa" );
        System.setProperty( PropertiesBasedJdbcDatabaseTester.DBUNIT_PASSWORD, "" );
        getConnection().getConfig().setProperty(DatabaseConfig.FEATURE_CASE_SENSITIVE_TABLE_NAMES, true);

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        
        DocumentBuilder docBuilder = dbf.newDocumentBuilder();
        exportedDocument = docBuilder
                .parse(new FileInputStream("src/test/resources/export/baw/skeleton_exported_idf.xml"));
        xpathUtils = new XPathUtils(new IDFNamespaceContext());



        mockSourceRecord = mock(DatabaseSourceRecord.class);
        when(mockSourceRecord.get(DatabaseSourceRecord.CONNECTION))
                .thenReturn(getConnection().getConnection());
        when(mockSourceRecord.get(DatabaseSourceRecord.ID))
                .thenReturn("11206656");
        when(mockSourceRecord.toString())
                .thenReturn("[Mocked source record]");
        
        Resource[] scripts = { new FileSystemResource("../distribution/src/profiles/baw_dmqs/conf/mapping/igc_to_idf_baw-dmqs_simulation.js") };
        m0 = new ScriptedIdfMapper();
        m0.setMappingScripts(scripts);
        m0.setCompile(true);
    }

    public void initialiseIfNeeded() {
        if (needsInitalisation) {
            try {
                DOMUtils domUtils = new DOMUtils(exportedDocument, xpathUtils);
                domUtils.addNS("idf", "http://www.portalu.de/IDF/1.0");
                domUtils.addNS("gmd", "http://www.isotc211.org/2005/gmd");
                
                m0.map(mockSourceRecord, exportedDocument);
                needsInitalisation = false;
            } catch (Exception ex) {
                LOG.error(ex);
            }
        }
    }
    
    @Test
    public void testContractNumber() {
        initialiseIfNeeded();
        keywordTest("DEBUNDBAWAUFTRAGNR");
    }

    @Test
    public void testTemporalAccuracy() {
        initialiseIfNeeded();
        NodeList timeAccuracy = retrieveUsingXpath("//gmd:DQ_AccuracyOfATimeMeasurement",
                exportedDocument,
                NodeList.class);
        assertThat(timeAccuracy, is(notNullValue()));
        assertThat(timeAccuracy.getLength(), is(equalTo(1)));

        Node ancestor = timeAccuracy.item(0);
        Node varName = retrieveUsingXpath(".//gco:RecordType",
                ancestor,
                Node.class);
        assertThat(varName, is(notNullValue()));
        assertThat(varName.getTextContent(), is(equalTo("temporal accuracy")));

        Node unitName = retrieveUsingXpath(".//gml:name",
                ancestor,
                Node.class);
        assertThat(unitName, is(notNullValue()));
        assertThat(unitName.getTextContent(), is(equalTo("second")));

        Node unitSymbol = retrieveUsingXpath(
                ".//gml:catalogSymbol",
                ancestor,
                Node.class);
        assertThat(unitSymbol, is(notNullValue()));
        assertThat(unitSymbol.getTextContent(), is(equalTo("s")));

        Node unitDefinition = retrieveUsingXpath(
                ".//gml:UnitDefinition/@id",
                ancestor,
                Node.class);
        assertThat(unitDefinition, is(notNullValue()));
        assertThat(unitDefinition.getTextContent(), not(isEmptyOrNullString()));
    }

    @Test
    public void testResourceFormat() {
        initialiseIfNeeded();
        NodeList formatNodes = retrieveUsingXpath(
                "//gmd:identificationInfo"
                    + "/gmd:MD_DataIdentification"
                        + "/gmd:resourceFormat"
                            + "/gmd:MD_Format",
                exportedDocument,
                NodeList.class);
        assertThat(formatNodes, is(notNullValue()));
        assertThat(formatNodes.getLength(), is(equalTo(1)));

        Node formatNode = formatNodes.item(0);
        Node formatName = retrieveUsingXpath(
                "./gmd:name/gco:CharacterString", 
                formatNode, 
                Node.class);
        assertThat(formatName, is(notNullValue()));
        assertThat(formatName.getTextContent(), not(isEmptyOrNullString()));
        Node formatVersion = retrieveUsingXpath(
                "./gmd:version/gco:CharacterString", 
                formatNode, 
                Node.class);
        assertThat(formatVersion, is(notNullValue()));
        assertThat(formatVersion.getTextContent(), not(isEmptyOrNullString()));
    }

    @Test
    public void testSimulationSpatialDimensions() {
        initialiseIfNeeded();
        keywordTest("BAW-DMQS Spatial Dimensions");
    }

    @Test
    public void testSimulationModellingMethod() {
        initialiseIfNeeded();
        keywordTest("BAW-DMQS Modelling Method");
    }

    @Test
    public void testSimulationModellingType() {
        initialiseIfNeeded();
        keywordTest("BAW-DMQS Modelling Type");
    }

    @Test
    public void testDgsTable() {
        initialiseIfNeeded();
        // Retrieve DQ_DataQuality tags with a quantitative attribute accuracy
        NodeList dqDataQuality = retrieveUsingXpath(
                "//gmd:DQ_QuantitativeAttributeAccuracy/../..",
                exportedDocument,
                NodeList.class);
        assertThat(dqDataQuality, is(notNullValue()));
        assertThat(dqDataQuality.getLength(), is(greaterThan(0)));

        for(int i=0; i<dqDataQuality.getLength(); i++) {
            Node ancestor = dqDataQuality.item(i);

            // Variable or parameter name
            Node varName = retrieveUsingXpath(".//gco:RecordType",
                    ancestor,
                    Node.class);

            assertThat(varName, is(notNullValue()));
            assertThat(varName.getTextContent(), not(isEmptyOrNullString()));

            // Variable or parameter value
            NodeList values = retrieveUsingXpath(".//gco:Record",
                    ancestor,
                    NodeList.class);
            assertThat(values, is(notNullValue()));
            for(int j=0; j<values.getLength(); j++) {
                Node value = values.item(j);
                assertThat(value.getTextContent(), not(isEmptyOrNullString()));
            }
            // Variable or parameter units symbol
            Node symbol = retrieveUsingXpath(".//gml:catalogSymbol",
                    ancestor,
                    Node.class);
            assertThat(symbol, is(notNullValue()));
            assertThat(symbol.getTextContent(), not(isEmptyOrNullString()));
            // Variable or parameter units id
            Node unitDefinition = retrieveUsingXpath(".//gml:UnitDefinition/@id",
                    ancestor,
                    Node.class);
            assertThat(unitDefinition, is(notNullValue()));
            assertThat(unitDefinition.getTextContent(), not(isEmptyOrNullString()));

            // Variable or parameter role
            Node role = retrieveUsingXpath(".//gmd:LI_Source/gmd:description/gco:CharacterString",
                    ancestor,
                    Node.class);
            assertThat(role, is(notNullValue()));
            assertThat(role.getTextContent(), not(isEmptyOrNullString()));

            // Download link
            Node href = retrieveUsingXpath(".//gmd:result/@href",
                    ancestor,
                    Node.class);
            assertThat(href, is(notNullValue()));
            assertThat(href.getTextContent(), not(isEmptyOrNullString()));
        }
    }



    private void keywordTest(String thesaurusName) {
        // Retrieve the relevant gmd:descriptiveKeywords tag
        Node node = retrieveUsingXpath(
                "//gmd:IdentificationInfo"
                    + "//gmd:thesaurusName"
                        + "//gco:CharacterString[text() = '" + thesaurusName + "']"
                            + "/../../../../..",
                exportedDocument,
                Node.class
        );

        node = retrieveUsingXpath("//gmd:MD_DataIdentification", exportedDocument, Node.class);
        System.out.println(node);
        assertThat(node, is(notNullValue()));

        // Retrieve tags with keyword texts
        NodeList keywordTexts = retrieveUsingXpath(
                ".//gmd:keyword/gco:CharacterString",
                node,
                NodeList.class);

        assertThat(keywordTexts, is(notNullValue()));
        assertThat(keywordTexts.getLength(), is(greaterThan(0)));

        for(int i=0; i<keywordTexts.getLength(); i++) {
            String kwText = keywordTexts.item(i).getTextContent();
            assertThat(kwText, not(isEmptyOrNullString()));
        }
    }

    private <T> T retrieveUsingXpath(String xpathString, Node start, Class<T> c) {
        if (c == Node.class) {
            Object result = xpathUtils.getNode(start, xpathString);
            return c.cast(result);
        } else if (c == NodeList.class) {
            Object result = xpathUtils.getNodeList(start, xpathString);
            return c.cast(result);
        }
        return null;
    }



    /* *************************************************************************
     *  Database actions not being tested
     **************************************************************************/
    
    @Override
    protected void setUp() throws Exception {
        System.out.println("Try creating tables from data source file: " + DATASOURCE_FILE_NAME);
        try(InputStream is = new FileInputStream(DATASOURCE_FILE_NAME)) {
            IDataSet ds = new XmlDataSet(is);
            createHsqldbTables(ds, this.getConnection().getConnection());
            super.setUp();
        }
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        System.out.println("Drop all tables.");
        PreparedStatement pp = this.getConnection().getConnection().prepareStatement("DROP SCHEMA PUBLIC CASCADE");
        pp.executeUpdate();
        pp.close();
    }

    @Override
    protected IDataSet getDataSet() throws Exception {
        System.out.println("Populating from data source file: " + DATASOURCE_FILE_NAME);
        try(InputStream is = new FileInputStream(DATASOURCE_FILE_NAME)) {
            IDataSet ds = new XmlDataSet(is);
            return ds;
        }
    }

    @Override
    protected void setUpDatabaseConfig(DatabaseConfig config) {
        config.setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new HsqldbDataTypeFactory());
    }

    private void createHsqldbTables(IDataSet dataSet, Connection connection) throws DataSetException, SQLException {
        String sql = "create memory table additional_field_data("
                + "id int primary key, "
                + "obj_id int, "
                + "parent_field_id int, "
                + "field_key varchar(255), "
                + "data varchar(255), "
                + "sort int);";
        PreparedStatement pp = connection.prepareStatement(sql);
        pp.executeUpdate();
        pp.close();
        sql = "create memory table t01_object("
                + "id int primary key, "
                + "obj_class int);";
        pp = connection.prepareStatement(sql);
        pp.executeUpdate();
        pp.close();
    }

}
