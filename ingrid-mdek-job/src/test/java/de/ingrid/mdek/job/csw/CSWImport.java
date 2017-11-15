/*
 * **************************************************-
 * InGrid mdek-job
 * ==================================================
 * Copyright (C) 2014 - 2017 wemove digital solutions GmbH
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
package de.ingrid.mdek.job.csw;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.core.io.ClassPathResource;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import de.ingrid.elasticsearch.IndexManager;
import de.ingrid.iplug.dsc.index.DatabaseConnection;
import de.ingrid.iplug.dsc.utils.DatabaseConnectionUtils;
import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.job.IJob.JobType;
import de.ingrid.mdek.job.IgeSearchPlug;
import de.ingrid.mdek.job.MdekIdcCatalogJob;
import de.ingrid.mdek.job.MdekIdcObjectJob;
import de.ingrid.mdek.job.mapping.DataMapperFactory;
import de.ingrid.mdek.job.mapping.ImportDataMapper;
import de.ingrid.mdek.job.mapping.ScriptImportDataMapper;
import de.ingrid.mdek.job.protocol.ProtocolHandler;
import de.ingrid.mdek.job.protocol.ProtocolHandler.Type;
import de.ingrid.mdek.services.catalog.MdekCatalogService;
import de.ingrid.mdek.services.catalog.MdekObjectService;
import de.ingrid.mdek.services.log.ILogService;
import de.ingrid.mdek.services.persistence.db.DaoFactory;
import de.ingrid.mdek.services.persistence.db.GenericHibernateDao;
import de.ingrid.mdek.services.persistence.db.IEntity;
import de.ingrid.mdek.services.persistence.db.IGenericDao;
import de.ingrid.mdek.services.persistence.db.dao.ISysListDao;
import de.ingrid.mdek.services.persistence.db.model.SysList;
import de.ingrid.mdek.services.persistence.db.model.T03Catalogue;
import de.ingrid.mdek.services.security.IPermissionService;
import de.ingrid.mdek.services.utils.MdekJobHandler;
import de.ingrid.mdek.xml.importer.IImporterCallback;
import de.ingrid.mdek.xml.importer.IngridXMLStreamReader;
import de.ingrid.mdek.xml.importer.mapper.IngridXMLMapper;
import de.ingrid.mdek.xml.importer.mapper.IngridXMLMapperFactory;
import de.ingrid.utils.IngridDocument;
import de.ingrid.utils.xml.XMLUtils;
import de.ingrid.utils.xpath.XPathUtils;

@PowerMockIgnore("javax.management.*")
@RunWith(PowerMockRunner.class)
@PrepareForTest({DatabaseConnectionUtils.class, MdekObjectService.class, MdekJobHandler.class})
public class CSWImport {

    // @InjectMocks
    // RelDatabaseCSWPersister databasePersister;

    private IgeSearchPlug plug;

    // @Mock
    // MdekIdcObjectJob objectJob;

    // @BeforeClass
    // public static void setUpBeforeClass() throws Exception {}
    @Mock
    IPermissionService permissionService;
    @Mock
    DaoFactory daoFactory;
    @Mock
    ILogService logService;

    @Mock
    MdekJobHandler jobHandler;

    @Mock
    GenericHibernateDao<IEntity> genericDao;

    @Mock
    IImporterCallback importerCallback;

    // @InjectMocks
    ScriptImportDataMapper cswMapper;
    // @Mock
    // MdekCatalogService catalogService;
    @Mock
    ISysListDao daoSysList;
    @Mock
    IGenericDao<IEntity> daoT03Catalogue;

    private MdekIdcCatalogJob catJob;
    
    @Mock
    private MdekIdcCatalogJob catJobMock;
    
    @Mock
    private MdekIdcObjectJob objectJobMock;
    
    @Mock private DatabaseConnectionUtils dcUtils;
    @Mock private Connection connectionMock;
    @Mock private PreparedStatement ps;
    @Mock private ResultSet resultSet;

    @Mock private MdekObjectService mdekObjectService;

    @Mock
    IndexManager indexManager;

    private IngridXMLMapper importMapper;

    @Before
    public void before() throws Exception {
        // CswTransaction trans = new CswTransaction();
        // trans.setPersist( databasePersister );

        plug = new IgeSearchPlug( null, null, null, null, null );

        when( daoFactory.getDao( IEntity.class ) ).thenReturn( genericDao );
        HashMap<String, List<byte[]>> analyzedDataMap = new HashMap<String, List<byte[]>>();
        analyzedDataMap.put( MdekKeys.REQUESTINFO_IMPORT_ANALYZED_DATA, new ArrayList<byte[]>() );
        when( jobHandler.getJobDetailsAsHashMap( JobType.IMPORT_ANALYZE, "TEST_USER_ID" ) ).thenReturn( analyzedDataMap );
        when( jobHandler.createRunningJobDescription(JobType.IMPORT, 0, 0, false) ).thenReturn( new IngridDocument() );
        when( jobHandler.getRunningJobInfo( any(String.class) ) ).thenReturn( new IngridDocument() );
        when( permissionService.isCatalogAdmin( "TEST_USER_ID" ) ).thenReturn( true );

        PowerMockito.mockStatic( DatabaseConnectionUtils.class );
        when(DatabaseConnectionUtils.getInstance()).thenReturn( dcUtils );
        when( dcUtils.openConnection( any(DatabaseConnection.class) ) ).thenReturn( connectionMock );
        when( connectionMock.prepareStatement( any(String.class) ) ).thenReturn( ps );
        when( ps.executeQuery() ).thenReturn( resultSet );
        
        PowerMockito.mockStatic( MdekObjectService.class );
        when(MdekObjectService.getInstance( any(DaoFactory.class), any(IPermissionService.class) )).thenReturn( mdekObjectService );
        
        PowerMockito.mockStatic( MdekJobHandler.class );
        when(MdekJobHandler.getInstance( any(DaoFactory.class))).thenReturn( jobHandler );
        
        ClassPathResource inputResource = new ClassPathResource( "csw/importAdditionalField.xml" );
        File file = inputResource.getFile();
        String xml = FileUtils.readFileToString( file );
        when( resultSet.getString( any(String.class) )).thenReturn( xml );
        
        when( daoFactory.getSysListDao() ).thenReturn( daoSysList );

        mockSyslists();

        when( daoFactory.getDao( T03Catalogue.class ) ).thenReturn( daoT03Catalogue );
        T03Catalogue t03Catalogue = new T03Catalogue();
        t03Catalogue.setLanguageKey( 150 );
        when( daoT03Catalogue.findFirst() ).thenReturn( t03Catalogue );
        when( catJobMock.getCatalogAdminUserUuid() ).thenReturn( "TEST_USER_ID" );

        cswMapper = new ScriptImportDataMapper( daoFactory );
        cswMapper.setCatalogService( MdekCatalogService.getInstance( daoFactory ) );

        catJob = new MdekIdcCatalogJob( logService, daoFactory, permissionService, indexManager );
        DataMapperFactory dataMapperFactory = new DataMapperFactory();
        HashMap<String, ImportDataMapper> mapper = new HashMap<String, ImportDataMapper>();
        ClassPathResource[] resources = new ClassPathResource[1];
        resources[0] = new ClassPathResource( "ingrid-mdek-job/src/main/resources/import/mapper/csw202_to_ingrid_igc.js" ); 
        cswMapper.setMapperScript( resources );
        cswMapper.setTemplate( new ClassPathResource( "ingrid-mdek-job/src/main/resources/import/templates/igc_template_csw202.xml" ) );
        mapper.put( "csw202", cswMapper );
        dataMapperFactory.setMapperClasses( mapper );
        catJob.setDataMapperFactory( dataMapperFactory );
        catJob.setJobHandler( jobHandler );
        // plug.setCswTransaction( trans );
        //plug.setCatalogJob( catJob );
        plug.setCatalogJob( catJobMock );
        plug.setObjectJob( objectJobMock );

        importMapper = IngridXMLMapperFactory.getIngridXMLMapper( "4.0.3" );
    }

    private void mockSyslists() {
        List<SysList> syslist100 = createSyslist( 100, 3068, "EPSG 3068: DHDN / Soldner Berlin" );
        extendSyslist( syslist100, 25832, "EPSG 25832: ETRS89 / UTM Zone 32N" );
        List<SysList> syslist101 = createSyslist( 101, 90008, "DE_DHHN92_NH" );
        List<SysList> syslist102 = createSyslist( 102, 9001, "Metre" );
        List<SysList> syslist502 = createSyslist( 502, 1, "creation" );
        extendSyslist( syslist502, 2, "publication" );
        List<SysList> syslist505 = createSyslist( 505, 7, "pointOfContact" );
        extendSyslist( syslist505, 1, "resourceProvider" );
        extendSyslist( syslist505, 5, "distributor" );

        List<SysList> syslist510 = createSyslist( 510, 4, "utf8" );
        List<SysList> syslist518 = createSyslist( 518, 1, "continual" );
        List<SysList> syslist520 = createSyslist( 520, 1, "cdRom" );
        List<SysList> syslist523 = createSyslist( 523, 5, "planned" );
        List<SysList> syslist524 = createSyslist( 524, 5, "license" );
        List<SysList> syslist526 = createSyslist( 526, 1, "vector" );
        List<SysList> syslist527 = createSyslist( 527, 15, "planningCadastre" );
        List<SysList> syslist1320 = createSyslist( 1320, 3, "Excel" );
        extendSyslist( syslist1320, 98, "XPlanGML" );
        extendSyslist( syslist1320, 99, "Geographic Markup Language (GML)" );
        List<SysList> syslist1350 = createSyslist( 1350, 24, "Bundeswasserstraßengesetz (WaStrG)" );
        List<SysList> syslist1410 = createSyslist( 1410, 6, "Energy" );
        List<SysList> syslist5120 = createSyslist( 5120, 1, "GetCapabilities" );
        List<SysList> syslist5153 = createSyslist( 5153, 2, "OGC:WFS 2.0" );
        List<SysList> syslist5200 = createSyslist( 5200, 211, "infoStandingOrderService" );
        List<SysList> syslist6005 = createSyslist( 6005, 40, "Technical Guidance for the implementation of INSPIRE Download Services" );
        extendSyslist( syslist6005, 13, "INSPIRE Richtlinie" );
        List<SysList> syslist6010 = createSyslist( 6010, 1, "Es gelten keine Bedingungen" );
        extendSyslist( syslist6010, 2, "keine" );
        extendSyslist( syslist6010, 3, "Baugesetzbuch (BauGB)" );
        extendSyslist( syslist6010, 4, "Baunutzungsverordnung (BauNVO)" );
        List<SysList> syslist6020 = createSyslist( 6020, 1, "Es gelten keine Bedingungen" );
        List<SysList> syslist6100 = createSyslist( 6100, 317, "Biogeografische Regionen" );
        extendSyslist( syslist6100, 302, "Gebäude" );
        extendSyslist( syslist6100, 304, "Land use" );
        List<SysList> syslist6400 = createSyslist( 6400, 5, "Gesundheit" );
        extendSyslist( syslist6400, 11, "Umwelt und Klima" );

        when( daoSysList.getSysList( 100, "iso" ) ).thenReturn( syslist100 );
        when( daoSysList.getSysList( 101, "iso" ) ).thenReturn( syslist101 );
        when( daoSysList.getSysList( 102, "iso" ) ).thenReturn( syslist102 );
        when( daoSysList.getSysList( 502, "iso" ) ).thenReturn( syslist502 );
        when( daoSysList.getSysList( 505, "iso" ) ).thenReturn( syslist505 );
        when( daoSysList.getSysList( 505, "de" ) ).thenReturn( syslist505 );
        when( daoSysList.getSysList( 510, "iso" ) ).thenReturn( syslist510 );
        when( daoSysList.getSysList( 518, "iso" ) ).thenReturn( syslist518 );
        when( daoSysList.getSysList( 520, "iso" ) ).thenReturn( syslist520 );
        when( daoSysList.getSysList( 523, "iso" ) ).thenReturn( syslist523 );
        when( daoSysList.getSysList( 524, "iso" ) ).thenReturn( syslist524 );
        when( daoSysList.getSysList( 526, "iso" ) ).thenReturn( syslist526 );
        when( daoSysList.getSysList( 527, "iso" ) ).thenReturn( syslist527 );
        when( daoSysList.getSysList( 1320, "iso" ) ).thenReturn( syslist1320 );
        when( daoSysList.getSysList( 1350, "iso" ) ).thenReturn( syslist1350 );
        when( daoSysList.getSysList( 1410, "iso" ) ).thenReturn( syslist1410 );
        when( daoSysList.getSysList( 5120, "iso" ) ).thenReturn( syslist5120 );
        when( daoSysList.getSysList( 5153, "iso" ) ).thenReturn( syslist5153 );
        when( daoSysList.getSysList( 5200, "iso" ) ).thenReturn( syslist5200 );
        when( daoSysList.getSysList( 6005, "de" ) ).thenReturn( syslist6005 );
        when( daoSysList.getSysList( 6010, "iso" ) ).thenReturn( syslist6010 );
        when( daoSysList.getSysList( 6020, "iso" ) ).thenReturn( syslist6020 );
        when( daoSysList.getSysList( 6100, "iso" ) ).thenReturn( syslist6100 );
        when( daoSysList.getSysList( 6400, "de" ) ).thenReturn( syslist6400 );
    }

    private List<SysList> createSyslist(int listId, int entryId, String value) {
        List<SysList> syslist = new ArrayList<SysList>();
        SysList entry = new SysList();
        entry.setLstId( listId );
        entry.setEntryId( entryId );
        entry.setName( value );
        syslist.add( entry );
        return syslist;
    }

    private void extendSyslist(List<SysList> list, int entryId, String value) {
        SysList entry = new SysList();
        entry.setLstId( list.get( 0 ).getLstId() );
        entry.setEntryId( entryId );
        entry.setName( value );
        list.add( entry );
    }

    @Test
    public void handleNullValue() {
        IngridDocument result = plug.cswTransaction( null );

        assertThat( result, is( not( nullValue() ) ) );
        assertThat( result.getBoolean( "success" ), is( false ) );
    }

    @Test
    public void analyzeCswDocumentInsert_nonGeographicDataset() throws Exception {

        doAnswer( new Answer<Void>() {
            @SuppressWarnings({ "unchecked", "rawtypes" })
            public Void answer(InvocationOnMock invocation) throws Exception {
                Map doc = invocation.getArgumentAt( 1, Map.class );
                List<byte[]> data = (List<byte[]>) doc.get( MdekKeys.REQUESTINFO_IMPORT_ANALYZED_DATA );
                assertThat( data, is( not( nullValue() ) ) );
                assertThat( data.size(), is( 1 ) );
                InputStream in = new GZIPInputStream( new ByteArrayInputStream( data.get( 0 ) ) );
                IngridXMLStreamReader reader = new IngridXMLStreamReader( in, importerCallback, "TEST_USER_ID" );
                assertThat( reader.getObjectUuids().size(), is( 1 ) );
                assertThat( reader.getObjectUuids().iterator().next(), is( "993E6356-D262-43AD-A69D-FE8EF62189A4" ) );
                List<Document> domForObject = reader.getDomForObject( "993E6356-D262-43AD-A69D-FE8EF62189A4" );
                try {
                    importMapper.mapDataSource( domForObject.get( 0 ) );
                    throw new AssertionError( "An exception should have occurred, because 'nonGeographicDataset' is not supported." );
                } catch (NumberFormatException ex) {
                    // expected
                } catch (Exception ex) {
                    throw new AssertionError( "An unexpected exception occurred: " + ex.getMessage() );
                }
                return null;
            }
        } ).when( jobHandler ).updateJobInfoDB( (JobType) any(), (HashMap) any(), anyString() );

        IngridDocument docIn = prepareInsertDocument( "csw/insert_nonGeographicDataset.xml" );
        IngridDocument analyzeImportData = catJob.analyzeImportData( docIn );
        assertThat( analyzeImportData.get( "error" ), is( nullValue() ) );
        ProtocolHandler protocol = (ProtocolHandler) analyzeImportData.get( "protocol" );
        assertThat( protocol.getProtocol( Type.ERROR ).size(), is( 0 ) );
        assertThat( protocol.getProtocol( Type.WARN ).size(), is( 1 ) );
        assertThat( protocol.getProtocol( Type.INFO ).size(), is( not( 0 ) ) );

    }

    @Test
    public void analyzeCswDocumentInsert_3_service() throws Exception {

        doAnswer( new Answer<Void>() {
            @SuppressWarnings({ "unchecked", "rawtypes" })
            public Void answer(InvocationOnMock invocation) throws Exception {
                Map doc = invocation.getArgumentAt( 1, Map.class );
                List<byte[]> data = (List<byte[]>) doc.get( MdekKeys.REQUESTINFO_IMPORT_ANALYZED_DATA );
                assertThat( data, is( not( nullValue() ) ) );
                assertThat( data.size(), is( 1 ) );
                InputStream in = new GZIPInputStream( new ByteArrayInputStream( data.get( 0 ) ) );
                IngridXMLStreamReader reader = new IngridXMLStreamReader( in, importerCallback, "TEST_USER_ID" );
                assertThat( reader.getObjectUuids().size(), is( 1 ) );
                assertThat( reader.getObjectUuids().iterator().next(), is( "D9EE3448-8224-4B08-926B-B9E5EDE360FC" ) );
                List<Document> domForObject = reader.getDomForObject( "D9EE3448-8224-4B08-926B-B9E5EDE360FC" );
                try {
                    IngridDocument docOut = importMapper.mapDataSource( domForObject.get( 0 ) );
                    System.out.println( XMLUtils.toString( domForObject.get( 0 ) ) );
                    // JSONObject jsonObject = new JSONObject();
                    // jsonObject.putAll( docOut );
                    assertThat( docOut.getString( MdekKeys.TITLE ), is( "Coupling Service" ) );
                    assertThat( docOut.getInt( MdekKeys.CLASS ), is( 3 ) );
                    // check responsible
                    assertThat( docOut.getString( MdekKeys.ORIGINAL_CONTROL_IDENTIFIER ), is( "D9EE3448-8224-4B08-926B-B9E5EDE360FC" ) );

                    // check short description
                    assertThat( docOut.getString( MdekKeys.DATASET_ALTERNATE_NAME ), is( "Eine kurze Beschreibung" ) );

                    // check preview image
                    List<IngridDocument> links = (List<IngridDocument>) docOut.get(MdekKeys.LINKAGES);
                    boolean found = false;
                    for (IngridDocument link : links) {
                        if (link.getInt( MdekKeys.LINKAGE_REFERENCE_ID ) == 9000 && "http://some.pic.com".equals( link.getString( MdekKeys.LINKAGE_URL ))) {
                            found = true;
                            break;
                        }
                    }
                    assertThat("Preview image was not found.", found, is(true));

                    // check abstract
                    assertThat( docOut.getString( MdekKeys.ABSTRACT ), is( "Dienst für den Test um externe gekoppelte Datensätze hinzuzufügen" ) );

                    // check address
                    List<IngridDocument> addresses = (List<IngridDocument>) docOut.get( MdekKeys.ADR_REFERENCES_TO );
                    // TODO: dataset gets a new UUID but keeps its origUUID!!!
                    assertThat( addresses.size(), is( 3 ) );
                    assertThat( addresses.get( 0 ).getInt( MdekKeys.RELATION_TYPE_ID ), is( 7 ) );
                    assertThat( addresses.get( 0 ).getInt( MdekKeys.RELATION_TYPE_REF ), is( 505 ) );
                    assertThat( addresses.get( 2 ).getString( MdekKeys.UUID ), is( "3E1B7F21-4E56-11D3-9A6B-0060971A0BF7" ) );
                    assertThat( addresses.get( 2 ).getInt( MdekKeys.RELATION_TYPE_ID ), is( 1 ) );
                    assertThat( addresses.get( 2 ).getInt( MdekKeys.RELATION_TYPE_REF ), is( 505 ) );

                    // inspire relevant
                    assertThat( docOut.getString( MdekKeys.IS_INSPIRE_RELEVANT ), is( "Y" ) );

                    // open data
                    assertThat( docOut.getString( MdekKeys.IS_OPEN_DATA ), is( "Y" ) );
                    assertThat( docOut.getArrayList( MdekKeys.OPEN_DATA_CATEGORY_LIST ).size(), is( 2 ) );
                    assertSubjectTerms( docOut.getArrayList( MdekKeys.OPEN_DATA_CATEGORY_LIST), "Umwelt und Klima", "Gesundheit" );

                    // INSPIRE-topics
                    assertThat( docOut.getArrayList( MdekKeys.SUBJECT_TERMS_INSPIRE ).size(), is( 2 ) );
                    assertSubjectTerms( docOut.getArrayList( MdekKeys.SUBJECT_TERMS_INSPIRE ), "Biogeografische Regionen", "Gebäude" );

                    // optional topics
                    assertThat( docOut.getArrayList( MdekKeys.SUBJECT_TERMS ).size(), is( 3 ) );
                    assertSubjectTerms( docOut.getArrayList( MdekKeys.SUBJECT_TERMS ), "Adaptronik", "Kabal", "Erdsystem" );

                    // environment topics
                    // NOT mapped in ISO!?
                    // TODO: assertThat( docOut.getString( MdekKeys.IS_CATALOG_DATA ), is( "Y" ) );
                    assertThat( docOut.getArrayList( MdekKeys.ENV_TOPICS ).size(), is( 1 ) );
                    assertThat( (int)docOut.getArrayList( MdekKeys.ENV_TOPICS ).get(0), is( 6 ) );

                    IngridDocument serviceMap = (IngridDocument) docOut.get( MdekKeys.TECHNICAL_DOMAIN_SERVICE );
                    // check classification of service: Dauerauftragsdienst (211)
                    assertThat( serviceMap.getArrayList( MdekKeys.SERVICE_TYPE2_LIST ).size(), is( 1 ) );
                    assertThat( ((IngridDocument) serviceMap.getArrayList( MdekKeys.SERVICE_TYPE2_LIST ).get( 0 )).getInt( MdekKeys.SERVICE_TYPE2_KEY ), is( 211 ) );

                    // service version
                    assertThat( ((IngridDocument) serviceMap.getArrayList( MdekKeys.SERVICE_VERSION_LIST ).get( 0 )).getString( MdekKeys.SERVICE_VERSION_VALUE ), is( "OGC:WFS 2.0" ) );

                    // check type of service: Downloaddienst === 3?
                    assertThat( serviceMap.getInt( MdekKeys.SERVICE_TYPE_KEY ), is( 3 ) );

                    // check is atom feed???
                    // NOT mapped in ISO!?
                    // TODO: assertThat( serviceMap.getString( MdekKeys.HAS_ATOM_DOWNLOAD ), is("Y"));

                    // operations
                    assertThat( serviceMap.getArrayList( MdekKeys.SERVICE_OPERATION_LIST ).size(), is( 1 ) );
                    assertOperation( serviceMap.getArrayList( MdekKeys.SERVICE_OPERATION_LIST ).get( 0 ), "GetCapabilities", "http://some.cap.com", "WebServices", "GetCap Beschreibung",
                            "http://some.cap.com/hello?count=10" );

                    // scale
                    // NOT mapped in ISO! -> only written into abstract
                    // TODO: assertThat( serviceMap.get( MdekKeys.PUBLICATION_SCALE_LIST ), is( nullValue() ) );

                    // system
                    // NOT mapped in ISO! -> only written into abstract
                    // mapping problem since from source: LI_SOURCE has multiple entries, also DATABASE_OF_SYSTEM
                    // TODO: assertThat( serviceMap.get( MdekKeys.SYSTEM_ENVIRONMENT ), is( "Zeitgeschichte" ) );

                    // history
                    // NOT mapped in ISO!?
                    // TODO: assertThat( serviceMap.get( MdekKeys.SYSTEM_HISTORY ), is( "Windows" ) );

                    // explanation
                    // NOT mapped in ISO!?
                    // TODO: assertThat( serviceMap.get( MdekKeys.DESCRIPTION_OF_TECH_DOMAIN ), is( "keine weiteren Erläuterungen" ) );

                    // presentational data
                    // mapping problem since from source: LI_SOURCE has multiple entries, also SYSTEM_ENVIRONMENT
                    // TODO: assertThat( serviceMap.getString( MdekKeys.DATABASE_OF_SYSTEM ), is( "meine dargestellten Daten" ) );

                    // coupling type
                    assertThat( serviceMap.getString( MdekKeys.COUPLING_TYPE ), is( "tight" ) );

                    // access constraint
                    // NOT mapped in ISO!?
                    // TODO: assertThat( serviceMap.getString( MdekKeys.HAS_ACCESS_CONSTRAINT ), is( "Y" ) );

                    // check spatial ref: EPSG 3068: DHDN / Soldner Berlin
                    assertThat( docOut.getArrayList( MdekKeys.SPATIAL_SYSTEM_LIST ).size(), is( 1 ) );
                    assertThat( ((IngridDocument) docOut.getArrayList( MdekKeys.SPATIAL_SYSTEM_LIST ).get( 0 )).getString( MdekKeys.COORDINATE_SYSTEM ), is( "EPSG 3068: DHDN / Soldner Berlin" ) );

                    // height
                    assertThat( (Double) docOut.get( MdekKeys.VERTICAL_EXTENT_MINIMUM ), is( 4.0 ) );
                    assertThat( (Double) docOut.get( MdekKeys.VERTICAL_EXTENT_MAXIMUM ), is( 6.0 ) );
                    assertThat( docOut.getInt( MdekKeys.VERTICAL_EXTENT_UNIT ), is( 9001 ) ); // "Meter"
                    assertThat( docOut.getString( MdekKeys.VERTICAL_EXTENT_VDATUM_VALUE ), is( "DE_DHHN92_NH" ) );

                    // height explanation
                    assertThat( docOut.getString( MdekKeys.DESCRIPTION_OF_SPATIAL_DOMAIN ), is( "nicht sehr hoch" ) );

                    // check creation: 17.03.2015
                    assertThat( ((IngridDocument) docOut.getArrayList( MdekKeys.DATASET_REFERENCES ).get( 0 )).getString( MdekKeys.DATASET_REFERENCE_DATE ), is( "20150317000000000" ) );

                    // time explanation
                    assertThat( docOut.getString( MdekKeys.DESCRIPTION_OF_TEMPORAL_DOMAIN ), is( "nicht sehr alt" ) );

                    // time range
                    assertThat( docOut.getString( MdekKeys.BEGINNING_DATE ), is( "20160209000000000" ) );
                    assertThat( docOut.getString( MdekKeys.ENDING_DATE ), is( "20160209000000000" ) );

                    // periodity
                    assertThat( docOut.getInt( MdekKeys.TIME_PERIOD ), is( 1 ) );

                    // state
                    assertThat( docOut.getInt( MdekKeys.TIME_STATUS ), is( 5 ) );

                    // interval
                    // NOT mapped in ISO!?
                    // TODO: assertThat( docOut.get( MdekKeys.TIME_INTERSECT), is( ) );

                    // check metadata language: Deutsch
                    assertThat( docOut.getString( MdekKeys.METADATA_LANGUAGE_NAME ), is( "Deutsch" ) );

                    // character set (utf8)
                    assertThat( docOut.getInt( MdekKeys.METADATA_CHARACTER_SET ), is( 4 ) );

                    // check publication info: Internet
                    assertThat( docOut.getInt( MdekKeys.PUBLICATION_CONDITION ), is( 1 ) );

                    // check conformity: Technical Guidance for the implementation of INSPIRE Download Services => konform
                    assertThat( docOut.getArrayList( MdekKeys.CONFORMITY_LIST ).size(), is( 1 ) );
                    assertConformity((IngridDocument) docOut.getArrayList( MdekKeys.CONFORMITY_LIST ).get( 0 ), "Technical Guidance for the implementation of INSPIRE Download Services", "konform");

                    // xml export criteria
                    // NOT mapped in ISO!?
                    // TODO: assertThat( docOut.getArrayList( MdekKeys.EXPORT_CRITERIA).size(), is( 1 ) );
                    // TODO: assertThat( ((IngridDocument)docOut.getArrayList( MdekKeys.EXPORT_CRITERIA).get(0)).get(
                    // MdekKeys.EXPORT_CRITERION_VALUE ), is( "CDS" ) );

                    // law basics
                    assertThat( docOut.getArrayList( MdekKeys.LEGISLATIONS ).size(), is( 1 ) );
                    assertThat( ((IngridDocument) docOut.getArrayList( MdekKeys.LEGISLATIONS ).get( 0 )).getInt( MdekKeys.LEGISLATION_KEY ), is( 24 ) );
                    assertThat( ((IngridDocument) docOut.getArrayList( MdekKeys.LEGISLATIONS ).get( 0 )).getString( MdekKeys.LEGISLATION_VALUE ), is( "Bundeswasserstraßengesetz (WaStrG)" ) );

                    // purpose
                    assertThat( docOut.getString( MdekKeys.DATASET_INTENTIONS ), is( "kein Zweck" ) );

                    // usage
                    assertThat( docOut.getString( MdekKeys.DATASET_USAGE ), is( "keine Nutzung" ) );

                    // check access constraints: Bedingungen unbekannt
                    // assertThat( docOut.getArrayList( MdekKeys.USE_LIST ).size(), is( 1 ) );
                    assertThat( ((IngridDocument) docOut.getArrayList( MdekKeys.USE_LIST ).get( 0 )).getString( MdekKeys.USE_TERMS_OF_USE_VALUE ), is( "Es gelten keine Bedingungen" ) );

                    // check usage constraints:
                    assertThat( ((IngridDocument) docOut.getArrayList( MdekKeys.USE_CONSTRAINTS ).get( 0 )).getString( MdekKeys.USE_LICENSE_VALUE ), is( "eingeschränkte Geolizenz" ) );

                    // check usage condition: Es gelten keine Bedingungen
                    assertThat( docOut.getArrayList( MdekKeys.ACCESS_LIST ).size(), is( 1 ) );
                    assertThat( ((IngridDocument) docOut.getArrayList( MdekKeys.ACCESS_LIST ).get( 0 )).getString( MdekKeys.ACCESS_RESTRICTION_VALUE ), is( "Es gelten keine Bedingungen" ) );

                    // data format
                    assertThat( docOut.getArrayList( MdekKeys.DATA_FORMATS ).size(), is( 1 ) );
                    assertThat( ((IngridDocument) docOut.getArrayList( MdekKeys.DATA_FORMATS ).get( 0 )).getString( MdekKeys.FORMAT_NAME ), is( "Excel" ) );
                    assertThat( ((IngridDocument) docOut.getArrayList( MdekKeys.DATA_FORMATS ).get( 0 )).getInt( MdekKeys.FORMAT_NAME_KEY ), is( 3 ) );
                    assertThat( ((IngridDocument) docOut.getArrayList( MdekKeys.DATA_FORMATS ).get( 0 )).getString( MdekKeys.FORMAT_VERSION ), is( "2" ) );
                    assertThat( ((IngridDocument) docOut.getArrayList( MdekKeys.DATA_FORMATS ).get( 0 )).getString( MdekKeys.FORMAT_FILE_DECOMPRESSION_TECHNIQUE ), is( "zip" ) );
                    assertThat( ((IngridDocument) docOut.getArrayList( MdekKeys.DATA_FORMATS ).get( 0 )).getString( MdekKeys.FORMAT_SPECIFICATION ), is( "5" ) );

                    // media
                    assertThat( docOut.getArrayList( MdekKeys.MEDIUM_OPTIONS ).size(), is( 1 ) );
                    assertMedia( (IngridDocument) docOut.getArrayList( MdekKeys.MEDIUM_OPTIONS ).get( 0 ), 1, 700.0, "c:/" );

                    // order info
                    assertThat( docOut.getString( MdekKeys.ORDERING_INSTRUCTIONS ), is( "keine Bestellung" ) );

                    // links to
                    
                    // links from

                    // spatial ref
                    // docOut.get( MdekKeys.SPATIAL_REPRESENTATION_TYPE_LIST )

                    // free spatial ref
                    List<Object> locs = docOut.getArrayList( MdekKeys.LOCATIONS );
                    // assertThat( locs.size(), is( 1 ));
                    assertLink( links.get( 2 ), "Datensatz mit zwei Download Links",
                            "http://192.168.0.247/interface-csw?REQUEST=GetRecordById&SERVICE=CSW&VERSION=2.0.2&id=93BBCF92-BD74-47A2-9865-BE59ABC90C57&iplug=/ingrid-group:iplug-csw-dsc-test&elementSetName=full",
                            "http://portalu.de/igc_testNS#/b6fb5dab-036d-4c43-82da-98ffa2e9df76#**#93BBCF92-BD74-47A2-9865-BE59ABC90C57" );

                    // Bounding Boxes are not bound to a name in ISO, so we cannot correctly map it to our structure
                    assertLocation( locs.get( 0 ), "Hannover (03241001)", null, null, null, null );
                    assertLocation( locs.get( 1 ), "Raumbezug des Datensatzes", 9.603732109069824, 9.919820785522461, 52.30428695678711, 52.454345703125 );

                } catch (Exception ex) {
                    throw new AssertionError( "An unexpected exception occurred: " + ex.getMessage() );
                }
                return null;
            }

        } ).when( jobHandler ).updateJobInfoDB( (JobType) any(), (HashMap) any(), anyString() );

        IngridDocument docIn = prepareInsertDocument( "csw/insert_class3_service.xml" );
        IngridDocument analyzeImportData = catJob.analyzeImportData( docIn );
        
        //Mockito.verify( catJob, Mockito.times( 1 ) ).analyzeImportData( (IngridDocument) any() );
        
        assertThat( analyzeImportData.get( "error" ), is( nullValue() ) );
        ProtocolHandler protocol = (ProtocolHandler) analyzeImportData.get( "protocol" );
        assertThat( protocol.getProtocol( Type.ERROR ).size(), is( 0 ) );
        assertThat( protocol.getProtocol( Type.WARN ).size(), is( 2 ) );
        assertThat( protocol.getProtocol( Type.INFO ).size(), is( not( 0 ) ) );
    }

    @Test
    public void analyzeCSWDocumentInsert_1_dataset() throws Exception {
        doAnswer( new Answer<Void>() {
            @SuppressWarnings({ "unchecked", "rawtypes" })
            public Void answer(InvocationOnMock invocation) throws Exception {
                Map doc = invocation.getArgumentAt( 1, Map.class );
                List<byte[]> data = (List<byte[]>) doc.get( MdekKeys.REQUESTINFO_IMPORT_ANALYZED_DATA );
                assertThat( data, is( not( nullValue() ) ) );
                assertThat( data.size(), is( 1 ) );
                InputStream in = new GZIPInputStream( new ByteArrayInputStream( data.get( 0 ) ) );
                IngridXMLStreamReader reader = new IngridXMLStreamReader( in, importerCallback, "TEST_USER_ID" );
                assertThat( reader.getObjectUuids().size(), is( 1 ) );
                assertThat( reader.getObjectUuids().iterator().next(), is( "4915275a-733a-47cd-b1a6-1a3f1e976948" ) );
                List<Document> domForObject = reader.getDomForObject( "4915275a-733a-47cd-b1a6-1a3f1e976948" );
                try {
                    IngridDocument docOut = importMapper.mapDataSource( domForObject.get( 0 ) );
                    // System.out.println( XMLUtils.toString( domForObject.get( 0 ) ) );
                    
                    assertThat( docOut.getString( MdekKeys.TITLE ), is( "Bebauungsplan Barmbek-Nord 18 (1.Änderung) Hamburg" ) );
                    assertThat( docOut.getInt( MdekKeys.CLASS ), is( 1 ) );
                    // check responsible
                    assertThat( docOut.getString( MdekKeys.ORIGINAL_CONTROL_IDENTIFIER ), is( "4915275a-733a-47cd-b1a6-1a3f1e976948" ) );

                    // check short description
                    assertThat( docOut.getString( MdekKeys.DATASET_ALTERNATE_NAME ), is( "BN 18 Ä Textänderungsverfahren" ) );

                    // check abstract
                    assertThat( docOut.getString( MdekKeys.ABSTRACT ), is( "siehe Originalplan" ) );

                    // inspire relevant
                    assertThat( docOut.getString( MdekKeys.IS_INSPIRE_RELEVANT ), is( nullValue() ) );

                    // open data
                    assertThat( docOut.getString( MdekKeys.IS_OPEN_DATA ), is( nullValue() ) );

                    // INSPIRE-topics
                    assertThat( docOut.getArrayList( MdekKeys.SUBJECT_TERMS_INSPIRE ).size(), is( 1 ) );
                    assertSubjectTerms( docOut.getArrayList( MdekKeys.SUBJECT_TERMS_INSPIRE ), "Land use" );

                    // optional topics
                    assertThat( docOut.getArrayList( MdekKeys.SUBJECT_TERMS ).size(), is( 4 ) );
                    assertSubjectTerms( docOut.getArrayList( MdekKeys.SUBJECT_TERMS ), "Raumbezogene Information", "Bauleitplanung", "Bebauungsplan", "Geoinformation" );

                    // check access constraints: Bedingungen unbekannt
                    // assertThat( docOut.getArrayList( MdekKeys.USE_LIST ).size(), is( 1 ) );
                    assertThat( ((IngridDocument) docOut.getArrayList( MdekKeys.USE_LIST ).get( 0 )).getString( MdekKeys.USE_TERMS_OF_USE_VALUE ), is( "Datenlizenz Deutschland - Namensnennung - Version 2.0; <a href=\"https://www.govdata.de/dl-de/by-2-0\">https://www.govdata.de/dl-de/by-2-0</a>; dl-de-by-2.0; Namensnennung: \"Freie und Hansestadt Hamburg, Bezirksamt Nord, Fachamt Stadt- und Landschaftsplanung\"" ) );

                    // check usage constraints:
                    assertThat( docOut.getArrayList( MdekKeys.USE_CONSTRAINTS).size(), is( 0 ) );
                    //assertThat( ((IngridDocument) docOut.getArrayList( MdekKeys.USE_CONSTRAINTS ).get( 1 )).getString( MdekKeys.USE_LICENSE_VALUE ), is( "eingeschränkte Geolizenz" ) );

                    // check usage condition: Es gelten keine Bedingungen
                    assertThat( docOut.getArrayList( MdekKeys.ACCESS_LIST ).size(), is( 3 ) );
                    assertThat( ((IngridDocument) docOut.getArrayList( MdekKeys.ACCESS_LIST ).get( 0 )).getString( MdekKeys.ACCESS_RESTRICTION_VALUE ), is( "keine" ) );
                    assertThat( ((IngridDocument) docOut.getArrayList( MdekKeys.ACCESS_LIST ).get( 1 )).getString( MdekKeys.ACCESS_RESTRICTION_VALUE ), is( "Baugesetzbuch (BauGB)" ) );
                    assertThat( ((IngridDocument) docOut.getArrayList( MdekKeys.ACCESS_LIST ).get( 2 )).getString( MdekKeys.ACCESS_RESTRICTION_VALUE ), is( "Baunutzungsverordnung (BauNVO)" ) );

                    // environment topics
                    assertThat( docOut.getArrayList( MdekKeys.ENV_TOPICS ).size(), is( 0 ) );
                    
                    // ISO topics
                    assertThat( docOut.getArrayList( MdekKeys.TOPIC_CATEGORIES ).size(), is( 1 ) );
                    assertThat( (Integer)docOut.getArrayList( MdekKeys.TOPIC_CATEGORIES).get( 0 ), is( 15 ) );
                    
                    // check metadata language: Deutsch
                    assertThat( docOut.getString( MdekKeys.METADATA_LANGUAGE_NAME ), is( "Deutsch" ) );

                    // character set (utf8)
                    assertThat( docOut.get( MdekKeys.METADATA_CHARACTER_SET ), is( nullValue() ) );
                    
                    // check spatial ref: EPSG 3068: DHDN / Soldner Berlin
                    assertThat( docOut.getArrayList( MdekKeys.SPATIAL_SYSTEM_LIST ).size(), is( 1 ) );
                    assertThat( ((IngridDocument) docOut.getArrayList( MdekKeys.SPATIAL_SYSTEM_LIST ).get( 0 )).getString( MdekKeys.COORDINATE_SYSTEM ), is( "EPSG 25832: ETRS89 / UTM Zone 32N" ) );
                    
                    // free spatial ref
                    List<Object> locs = docOut.getArrayList( MdekKeys.LOCATIONS );
                    assertThat( locs.size(), is( 2 ));

                    // Bounding Boxes are not bound to a name in ISO, so we cannot correctly map it to our structure
                    assertLocation( locs.get( 0 ), "Hamburg", null, null, null, null );
                    assertLocation( locs.get( 1 ), "Raumbezug des Datensatzes", 8.420551, 10.326304, 53.394985, 53.964153 );
                    
                    // time range
                    assertThat( docOut.getString( MdekKeys.BEGINNING_DATE ), is( "20160301000000000" ) );
                    assertThat( docOut.getString( MdekKeys.ENDING_DATE ), is( nullValue() ) );
                    
                    // data formats
                    assertThat( docOut.getArrayList( MdekKeys.DATA_FORMATS ).size(), is( 3 ) );
                    assertDataFormat( (IngridDocument) docOut.getArrayList( MdekKeys.DATA_FORMATS ).get(0), "Geographic Markup Language (GML)", 99, null, null, null );
                    assertDataFormat( (IngridDocument) docOut.getArrayList( MdekKeys.DATA_FORMATS ).get(1), "XPlanGML", 98, "4.1", null, null );
                    assertDataFormat( (IngridDocument) docOut.getArrayList( MdekKeys.DATA_FORMATS ).get(2), "XPlanGML", 98, "3.0", null, null );
                    
                    // media
                    assertThat( docOut.getArrayList( MdekKeys.MEDIUM_OPTIONS ).size(), is( 0 ) );
                    //assertMedia( (IngridDocument) docOut.getArrayList( MdekKeys.MEDIUM_OPTIONS ).get( 0 ), 1, 700.0, "c:/" );
                    
                    List<IngridDocument> links = (List<IngridDocument>) docOut.get(MdekKeys.LINKAGES);
                    assertThat( links.size(), is( 4 ) );
                    assertLink( links.get( 0 ), "Bekanntmachung im HmbGVBl als PDF Datei",
                            "http://daten-hamburg.de/infrastruktur_bauen_wohnen/bebauungsplaene/pdfs/bplan/Barmbek-Nord18(1Aend).pdf",
                            null );
                    assertLink( links.get( 1 ), "URL zu weiteren Informationen über den Datensatz",
                            "http://www.hamburg.de/bebauungsplaene-online.de",
                            null );
                    assertLink( links.get( 2 ), "Begründung des Bebauungsplans als PDF Datei",
                            "http://daten-hamburg.de/infrastruktur_bauen_wohnen/bebauungsplaene/pdfs/bplan_begr/Barmbek-Nord18(1Aend).pdf",
                            null );
                    assertLink( links.get( 3 ), "Festsetzungen (Planzeichnung / Verordnung) als PDF Datei",
                            "http://daten-hamburg.de/infrastruktur_bauen_wohnen/bebauungsplaene/pdfs/bplan/Barmbek-Nord18(1Aend).pdf",
                            null );

                    // check conformity: Technical Guidance for the implementation of INSPIRE Download Services => konform
                    assertThat( docOut.getArrayList( MdekKeys.CONFORMITY_LIST ).size(), is( 1 ) );
                    assertConformity((IngridDocument) docOut.getArrayList( MdekKeys.CONFORMITY_LIST ).get( 0 ), "INSPIRE Richtlinie", "nicht konform");
                    
                    IngridDocument techDomain = (IngridDocument) docOut.get( MdekKeys.TECHNICAL_DOMAIN_MAP );
                    assertThat( techDomain.getString(MdekKeys.TECHNICAL_BASE), is( "vergl. eGovernment Vorhaben \"PLIS\"" ) );
                    assertThat( techDomain.getString(MdekKeys.METHOD_OF_PRODUCTION), is( "Die in den Planwerken der verbindlichen Bauleitplanung dokumentierten Festsetzungen, Kennzeichnungen und Hinweise werden auf der Grundlage der aktuellen Örtlichkeit der Liegenschaftskarte (ALKIS) mit Hilfe von Fachapplikationen (AutoCAD + WS LANDCAD bzw. ArcGIS + GeoOffice) digitalisiert." ) );

                    // check address
                    List<IngridDocument> addresses = (List<IngridDocument>) docOut.get( MdekKeys.ADR_REFERENCES_TO );
                    // TODO: dataset gets a new UUID but keeps its origUUID!!!
                    assertThat( addresses.size(), is( 2 ) );
                    assertThat( addresses.get( 0 ).getInt( MdekKeys.RELATION_TYPE_ID ), is( 7 ) );
                    assertThat( addresses.get( 0 ).getInt( MdekKeys.RELATION_TYPE_REF ), is( 505 ) );
                    assertThat( addresses.get( 0 ).getString( MdekKeys.UUID ), is( "110C6012-1713-44C0-9A33-4E2C24D06966" ) );
                    assertThat( addresses.get( 1 ).getInt( MdekKeys.RELATION_TYPE_ID ), is( 7 ) );
                    assertThat( addresses.get( 1 ).getInt( MdekKeys.RELATION_TYPE_REF ), is( 505 ) );
                    assertThat( addresses.get( 1 ).getString( MdekKeys.UUID ), is( "DA64401A-2AFC-458D-A8AF-58D0A3C35AA9" ) );
                    
                } catch (Exception ex) {
                    throw new AssertionError( "An unexpected exception occurred: " + ex.getMessage() );
                }
                return null;
            }


        } ).when( jobHandler ).updateJobInfoDB( (JobType) any(), (HashMap) any(), anyString() );

        IngridDocument docIn = prepareInsertDocument( "csw/insert_class1_dataset.xml" );
        IngridDocument analyzeImportData = catJob.analyzeImportData( docIn );
        
        //Mockito.verify( catJob, Mockito.times( 1 ) ).analyzeImportData( (IngridDocument) any() );
        
        assertThat( analyzeImportData.get( "error" ), is( nullValue() ) );
        ProtocolHandler protocol = (ProtocolHandler) analyzeImportData.get( "protocol" );
        assertThat( protocol.getProtocol( Type.ERROR ).size(), is( 0 ) );
        assertThat( protocol.getProtocol( Type.WARN ).size(), is( 3 ) );
        assertThat( protocol.getProtocol( Type.INFO ).size(), is( not( 0 ) ) );
    }
    
    private void assertConformity(IngridDocument doc, String specValue, String degree) {
        assertThat( doc.getString( MdekKeys.CONFORMITY_SPECIFICATION_VALUE ), is( specValue ) );
        assertThat( doc.getString( MdekKeys.CONFORMITY_DEGREE_VALUE ), is( degree ) );
    }

    private void assertMedia(IngridDocument doc, int nameId, double transferSize, String note) {
        assertThat( doc.getInt( MdekKeys.MEDIUM_NAME ), is( nameId ) );
        assertThat( (Double)doc.get( MdekKeys.MEDIUM_TRANSFER_SIZE ), is( transferSize ) );
        assertThat( doc.getString( MdekKeys.MEDIUM_NOTE ), is( note ) );
        
    }
    
    private void assertDataFormat(IngridDocument dataFormat, String name, int nameKey, Object version, Object specification, Object decompress) {
        assertThat( dataFormat.getString( MdekKeys.FORMAT_NAME ), is( name ) );
        assertThat( dataFormat.getInt( MdekKeys.FORMAT_NAME_KEY ), is( nameKey ) );
        if (version == null) assertThat( dataFormat.get( MdekKeys.FORMAT_VERSION ), is( nullValue() ) ); else assertThat( dataFormat.get( MdekKeys.FORMAT_VERSION ), is( version ) );
        if (specification == null) assertThat( dataFormat.get( MdekKeys.FORMAT_SPECIFICATION ), is( nullValue() ) ); else assertThat( dataFormat.get( MdekKeys.FORMAT_SPECIFICATION ), is( specification ) );
        if (decompress == null) assertThat( dataFormat.get( MdekKeys.FORMAT_FILE_DECOMPRESSION_TECHNIQUE ), is( nullValue() ) ); else assertThat( dataFormat.get( MdekKeys.FORMAT_FILE_DECOMPRESSION_TECHNIQUE ), is( decompress ) );
    }
    
    private void assertOperation(Object operation, String type, String url, String platform, String description, String invocationUrl) {
        IngridDocument doc = (IngridDocument) operation;
        assertThat( (String) doc.getArrayList( MdekKeys.CONNECT_POINT_LIST ).get( 0 ), is( url ) );
        assertThat( doc.getString( MdekKeys.SERVICE_OPERATION_DESCRIPTION ), is( description ) );
        assertThat( doc.getString( MdekKeys.SERVICE_OPERATION_NAME ), is( type ) );
        assertThat( ((IngridDocument) doc.getArrayList( MdekKeys.PLATFORM_LIST ).get( 0 )).getString( MdekKeys.PLATFORM_VALUE ), is( platform ) );
        assertThat( doc.getString( MdekKeys.INVOCATION_NAME ), is( invocationUrl ) );
    }

    private void assertSubjectTerms(List<Object> terms, String... expectedTerms) {
        for (String expectedTerm : expectedTerms) {
            boolean found = false;
            for (IngridDocument term : (List<IngridDocument>) (List<?>) terms) {
                if (term.get( MdekKeys.TERM_NAME ).equals( expectedTerm )) {
                    found = true;
                    break;
                }
            }
            assertThat( "The expected term was not found: " + expectedTerm, found, is( true ) );
        }

    }

    private void assertLink(Object link, String name, String url, String description) {
        IngridDocument linkDoc = (IngridDocument) link;
        assertThat( linkDoc.getString( MdekKeys.LINKAGE_NAME ), is( name ) );
        assertThat( linkDoc.getString( MdekKeys.LINKAGE_URL ), is( url ) );
        assertThat( linkDoc.getString( MdekKeys.LINKAGE_DESCRIPTION ), is( description ) );
    }
    
    private IngridDocument prepareInsertDocument(String filename) throws Exception {
        return prepareDocument( filename, "csw:Insert" );
    }
    
    private IngridDocument prepareUpdateDocument(String filename) throws Exception {
        return prepareDocument( filename, "csw:Update" );
    }

    private IngridDocument prepareDocument(String filename, String tag) throws Exception {
        ClassPathResource inputResource = new ClassPathResource( filename );
        File file = inputResource.getFile();
        String xml = FileUtils.readFileToString( file );

        // extract csw-document
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        Document xmlDoc = builder.parse( new InputSource( new StringReader( xml ) ) );
        NodeList insert = xmlDoc.getElementsByTagName( tag );

        Document singleInsertDocument = builder.newDocument();
        Node importedNode = singleInsertDocument.importNode( insert.item( 0 ).getFirstChild().getNextSibling(), true );
        singleInsertDocument.appendChild( importedNode );
        // end of extract csw-document

        String insertDoc = XMLUtils.toString( singleInsertDocument );
        IngridDocument docIn = new IngridDocument();
        docIn.put( MdekKeys.USER_ID, "TEST_USER_ID" );
        // docIn.put( MdekKeys.REQUESTINFO_IMPORT_DATA, GZipTool.gzip( insertDoc ).getBytes());
        docIn.put( MdekKeys.REQUESTINFO_IMPORT_DATA, MdekIdcCatalogJob.compress( new ByteArrayInputStream( insertDoc.getBytes() ) ).toByteArray() );
        docIn.put( MdekKeys.REQUESTINFO_IMPORT_FRONTEND_PROTOCOL, "csw202" );
        docIn.putBoolean( MdekKeys.REQUESTINFO_IMPORT_START_NEW_ANALYSIS, true );
        docIn.putBoolean( MdekKeys.REQUESTINFO_IMPORT_PUBLISH_IMMEDIATELY, true );
        docIn.putBoolean( MdekKeys.REQUESTINFO_IMPORT_DO_SEPARATE_IMPORT, false );
        docIn.putBoolean( MdekKeys.REQUESTINFO_IMPORT_COPY_NODE_IF_PRESENT, false );
        docIn.putBoolean( MdekKeys.REQUESTINFO_IMPORT_IGNORE_PARENT_IMPORT_NODE, true );
        
        docIn.put( MdekKeys.REQUESTINFO_IMPORT_OBJ_PARENT_UUID, "2768376B-EE24-4F34-969B-084C55B52278" );  // IMPORTKNOTEN
        docIn.put( MdekKeys.REQUESTINFO_IMPORT_ADDR_PARENT_UUID, "BD33BC8E-519E-47F9-8A30-465C95CD0355" ); // IMPORTKNOTEN
        return docIn;
    }

    //@Test
    public void handleDocumentUpdate() throws Exception {
        doAnswer( new Answer<Void>() {
            public Void answer(InvocationOnMock invocation) throws Exception {
                IngridDocument doc = (IngridDocument) invocation.getArgumentAt( 0, Map.class );
                assertThat( doc.getString( MdekKeys.USER_ID ), is( "TEST_USER_ID" ));
                assertThat( doc.getString( MdekKeys.UUID ), is( "1234-5678-abcd-efgh" ));
                assertThat( doc.getBoolean( MdekKeys.REQUESTINFO_FORCE_DELETE_REFERENCES ), is( false ));
                return null;                
            }
        }).when( objectJobMock ).storeObject( (IngridDocument) any() );
        
        doAnswer( new Answer<Void>() {
            public Void answer(InvocationOnMock invocation) throws Exception {
                HashMap doc = (HashMap) invocation.getArgumentAt( 1, Map.class );
                when(jobHandler.getJobDetailsAsHashMap( any(JobType.class), any(String.class) )).thenReturn( doc );
                return null;                
            }
        }).when(jobHandler).updateJobInfoDB( any(JobType.class), any(HashMap.class), any(String.class));
        
        IngridDocument doc = prepareUpdateDocument("csw/update_dataset.xml");
        IngridDocument analyzeImportData = catJob.analyzeImportData( doc );
        
        assertThat( analyzeImportData.get( "error" ), is( nullValue() ) );
//        ProtocolHandler protocol = (ProtocolHandler) analyzeImportData.get( "protocol" );
//        assertThat( protocol.getProtocol( Type.ERROR ).size(), is( 0 ) );
//        assertThat( protocol.getProtocol( Type.WARN ).size(), is( 13 ) );
//        assertThat( protocol.getProtocol( Type.INFO ).size(), is( not( 0 ) ) );
        
        IngridDocument result = catJob.importEntities( doc );
        
        Mockito.verify( objectJobMock, Mockito.times( 1 ) ).storeObject( (IngridDocument) any() );

        assertThat( result, is( not( nullValue() ) ) );
        assertThat( result.getBoolean( "success" ), is( true ) );
    }

    @Test
    public void handleDocumentDelete() throws IOException {
        ClassPathResource inputResource = new ClassPathResource( "csw/delete_dataset.xml" );
        File file = inputResource.getFile();
        
        doAnswer( new Answer<IngridDocument>() {
            public IngridDocument answer(InvocationOnMock invocation) throws Exception {
                IngridDocument doc = (IngridDocument) invocation.getArgumentAt( 0, Map.class );
                assertThat( doc.getString( MdekKeys.USER_ID ), is( "TEST_USER_ID" ));
                assertThat( doc.getString( MdekKeys.UUID ), is( "1234-5678-abcd-efgh" ));
                assertThat( doc.getBoolean( MdekKeys.REQUESTINFO_FORCE_DELETE_REFERENCES ), is( false ));
                IngridDocument resultDelete = new IngridDocument();
                resultDelete.put(MdekKeys.RESULTINFO_WAS_FULLY_DELETED, true);
                return resultDelete;                
            }
        }).when( objectJobMock ).deleteObject( (IngridDocument) any() );
        
        
        String xml = FileUtils.readFileToString( file );
        IngridDocument result = plug.cswTransaction( xml );
        
        Mockito.verify( objectJobMock, Mockito.times( 1 ) ).deleteObject( (IngridDocument) any() );

        assertThat( result, is( not( nullValue() ) ) );
        assertThat( result.getBoolean( "success" ), is( true ) );
    }
    
    @Test
    public void handleDocumentDeleteFail() throws IOException {
        ClassPathResource inputResource = new ClassPathResource( "csw/delete_dataset.xml" );
        File file = inputResource.getFile();
        
        
        doAnswer( new Answer<IngridDocument>() {
            public IngridDocument answer(InvocationOnMock invocation) throws Exception {
                IngridDocument doc = (IngridDocument) invocation.getArgumentAt( 0, Map.class );
                assertThat( doc.getString( MdekKeys.USER_ID ), is( "TEST_USER_ID" ));
                assertThat( doc.getString( MdekKeys.UUID ), is( "1234-5678-abcd-efgh" ));
                assertThat( doc.getBoolean( MdekKeys.REQUESTINFO_FORCE_DELETE_REFERENCES ), is( false ));
                IngridDocument resultDelete = new IngridDocument();
                resultDelete.put(MdekKeys.RESULTINFO_WAS_FULLY_DELETED, false);
                return resultDelete;
            }
        }).when( objectJobMock ).deleteObject( (IngridDocument) any() );
        
        String xml = FileUtils.readFileToString( file );
        IngridDocument result = plug.cswTransaction( xml );
        
        Mockito.verify( objectJobMock, Mockito.times( 1 ) ).deleteObject( (IngridDocument) any() );
        
        assertThat( result, is( not( nullValue() ) ) );
        assertThat( "The CSW-T transaction should not have succeeded.", result.getBoolean( "success" ), is( false ) );
    }
    
    @Test
    public void importAdditionalField() throws Exception {
        doAnswer( new Answer<Void>() {
            @SuppressWarnings({ "unchecked", "rawtypes" })
            public Void answer(InvocationOnMock invocation) throws Exception {
                Map doc = invocation.getArgumentAt( 1, Map.class );
                List<byte[]> data = (List<byte[]>) doc.get( MdekKeys.REQUESTINFO_IMPORT_ANALYZED_DATA );
                assertThat( data, is( not( nullValue() ) ) );
                assertThat( data.size(), is( 1 ) );
                InputStream in = new GZIPInputStream( new ByteArrayInputStream( data.get( 0 ) ) );
                IngridXMLStreamReader reader = new IngridXMLStreamReader( in, importerCallback, "TEST_USER_ID" );
                assertThat( reader.getObjectUuids().size(), is( 1 ) );
                assertThat( reader.getObjectUuids().iterator().next(), is( "4915275a-733a-47cd-1234-1a3f1e976948" ) );
                List<Document> domForObject = reader.getDomForObject( "4915275a-733a-47cd-1234-1a3f1e976948" );
                XPathUtils xpath = new XPathUtils();
                NodeList additionalValues = xpath.getNodeList( domForObject.get( 0 ), "//general-additional-value");
                boolean hasOpenDataSupport = false;
                for (int i = 0; i < additionalValues.getLength(); i++) {
                    String key = xpath.getString( additionalValues.item( i ), "field-key");
                    String value = xpath.getString( additionalValues.item( i ), "field-data");
                    if ("publicationHmbTG".equals( key ) && "true".equals( value )) hasOpenDataSupport = true;
                }
                assertThat( hasOpenDataSupport, is(true) );
                return null;
            }
        } ).when( jobHandler ).updateJobInfoDB( (JobType) any(), (HashMap) any(), anyString() );
        
        IngridDocument docIn = prepareInsertDocument( "csw/importAdditionalFieldDoc.xml" );
        IngridDocument analyzeImportData = catJob.analyzeImportData( docIn );
        
        //Mockito.verify( catJob, Mockito.times( 1 ) ).analyzeImportData( (IngridDocument) any() );
        
        assertThat( analyzeImportData.get( "error" ), is( nullValue() ) );
    }
    
    
    @Test @Ignore
    public void deleteFailsWhenOrigIdNotFound() {}
    @Test @Ignore
    public void deleteFailsWhenUuidNotFound() {}
    @Test @Ignore
    public void deleteSuccessWhenOrigIdFound() {}
    @Test @Ignore
    public void deleteSuccessWhenUuidFound() {}
    @Test @Ignore
    public void updateFailsWhenObjectNotExists() {}
    @Test @Ignore
    public void insertFailsWhenObjectExists() {}

    private void checkXmlResponse(String xml, int inserted, int updated, int deleted) {
        assertThat( xml, containsString( "<csw:totalInserted>" + String.valueOf( inserted ) + "</csw:totalInserted>" ) );
        assertThat( xml, containsString( "<csw:totalUpdated>" + String.valueOf( updated ) + "</csw:totalUpdated>" ) );
        assertThat( xml, containsString( "<csw:totalDeleted>" + String.valueOf( deleted ) + "</csw:totalDeleted>" ) );
    }

    private void assertLocation(Object location, String name, Double longWest, Double longEast, Double latSouth, Double latNorth) {
        IngridDocument locationDoc = (IngridDocument) location;
        assertThat( locationDoc.getString( MdekKeys.LOCATION_NAME ), is( name ) );
        assertThat( (Double) locationDoc.get( MdekKeys.NORTH_BOUNDING_COORDINATE ), is( latNorth ) );
        assertThat( (Double) locationDoc.get( MdekKeys.SOUTH_BOUNDING_COORDINATE ), is( latSouth ) );
        assertThat( (Double) locationDoc.get( MdekKeys.EAST_BOUNDING_COORDINATE ), is( longEast ) );
        assertThat( (Double) locationDoc.get( MdekKeys.WEST_BOUNDING_COORDINATE ), is( longWest ) );

    }
}
