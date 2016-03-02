package de.ingrid.mdek.job.csw;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.springframework.core.io.ClassPathResource;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

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

@RunWith(MockitoJUnitRunner.class)
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

    private IngridXMLMapper importMapper;

    @Before
    public void before() throws Exception {
        // CswTransaction trans = new CswTransaction();
        // trans.setPersist( databasePersister );

        plug = new IgeSearchPlug( null, null, null, null, null );

        Mockito.when( daoFactory.getDao( IEntity.class ) ).thenReturn( genericDao );
        HashMap<String, List<byte[]>> analyzedDataMap = new HashMap<String, List<byte[]>>();
        analyzedDataMap.put( MdekKeys.REQUESTINFO_IMPORT_ANALYZED_DATA, new ArrayList<byte[]>() );
        Mockito.when( jobHandler.getJobDetailsAsHashMap( JobType.IMPORT_ANALYZE, "TEST_USER_ID" ) ).thenReturn( analyzedDataMap );

        Mockito.when( daoFactory.getSysListDao() ).thenReturn( daoSysList );

        mockSyslists();

        Mockito.when( daoFactory.getDao( T03Catalogue.class ) ).thenReturn( daoT03Catalogue );
        T03Catalogue t03Catalogue = new T03Catalogue();
        t03Catalogue.setLanguageKey( 150 );
        Mockito.when( daoT03Catalogue.findFirst() ).thenReturn( t03Catalogue );

        cswMapper = new ScriptImportDataMapper( daoFactory );
        cswMapper.setCatalogService( MdekCatalogService.getInstance( daoFactory ) );

        catJob = new MdekIdcCatalogJob( logService, daoFactory, permissionService );
        DataMapperFactory dataMapperFactory = new DataMapperFactory();
        HashMap<String, ImportDataMapper> mapper = new HashMap<String, ImportDataMapper>();
        cswMapper.setMapperScript( new ClassPathResource( "import/mapper/csw202_to_ingrid_igc.js" ) );
        cswMapper.setTemplate( new ClassPathResource( "import/templates/igc_template_csw202.xml" ) );
        mapper.put( "csw202", cswMapper );
        dataMapperFactory.setMapperClasses( mapper );
        catJob.setDataMapperFactory( dataMapperFactory );
        catJob.setJobHandler( jobHandler );
        // plug.setCswTransaction( trans );
        //plug.setCatalogJob( catJob );
        plug.setCatalogJob( catJobMock );
        plug.setObjectJob( objectJobMock );

        importMapper = IngridXMLMapperFactory.getIngridXMLMapper( "3.6.1" );
    }

    private void mockSyslists() {
        List<SysList> syslist100 = createSyslist( 100, 3068, "EPSG 3068: DHDN / Soldner Berlin" );
        List<SysList> syslist101 = createSyslist( 101, 90008, "DE_DHHN92_NH" );
        List<SysList> syslist102 = createSyslist( 102, 9001, "Metre" );
        List<SysList> syslist502 = createSyslist( 502, 1, "creation" );
        List<SysList> syslist505 = createSyslist( 505, 7, "pointOfContact" );
        extendSyslist( syslist505, 1, "resourceProvider" );
        extendSyslist( syslist505, 5, "distributor" );

        List<SysList> syslist510 = createSyslist( 510, 4, "utf8" );
        List<SysList> syslist518 = createSyslist( 518, 1, "continual" );
        List<SysList> syslist520 = createSyslist( 520, 1, "cdRom" );
        List<SysList> syslist523 = createSyslist( 523, 5, "planned" );
        List<SysList> syslist524 = createSyslist( 524, 5, "license" );
        List<SysList> syslist1320 = createSyslist( 1320, 3, "Excel" );
        List<SysList> syslist1350 = createSyslist( 1350, 24, "Bundeswasserstraßengesetz (WaStrG)" );
        List<SysList> syslist5120 = createSyslist( 5120, 1, "GetCapabilities" );
        List<SysList> syslist5153 = createSyslist( 5153, 2, "OGC:WFS 2.0" );
        List<SysList> syslist5200 = createSyslist( 5200, 211, "infoStandingOrderService" );
        List<SysList> syslist6005 = createSyslist( 6005, 40, "Technical Guidance for the implementation of INSPIRE Download Services" );
        List<SysList> syslist6010 = createSyslist( 6010, 1, "Es gelten keine Bedingungen" );
        List<SysList> syslist6020 = createSyslist( 6020, 1, "Es gelten keine Bedingungen" );
        List<SysList> syslist6100 = createSyslist( 6100, 317, "Biogeografische Regionen" );
        extendSyslist( syslist6100, 302, "Gebäude" );

        Mockito.when( daoSysList.getSysList( 100, "iso" ) ).thenReturn( syslist100 );
        Mockito.when( daoSysList.getSysList( 101, "iso" ) ).thenReturn( syslist101 );
        Mockito.when( daoSysList.getSysList( 102, "iso" ) ).thenReturn( syslist102 );
        Mockito.when( daoSysList.getSysList( 502, "iso" ) ).thenReturn( syslist502 );
        Mockito.when( daoSysList.getSysList( 505, "iso" ) ).thenReturn( syslist505 );
        Mockito.when( daoSysList.getSysList( 505, "de" ) ).thenReturn( syslist505 );
        Mockito.when( daoSysList.getSysList( 510, "iso" ) ).thenReturn( syslist510 );
        Mockito.when( daoSysList.getSysList( 518, "iso" ) ).thenReturn( syslist518 );
        Mockito.when( daoSysList.getSysList( 520, "iso" ) ).thenReturn( syslist520 );
        Mockito.when( daoSysList.getSysList( 523, "iso" ) ).thenReturn( syslist523 );
        Mockito.when( daoSysList.getSysList( 524, "iso" ) ).thenReturn( syslist524 );
        Mockito.when( daoSysList.getSysList( 1320, "iso" ) ).thenReturn( syslist1320 );
        Mockito.when( daoSysList.getSysList( 1350, "iso" ) ).thenReturn( syslist1350 );
        Mockito.when( daoSysList.getSysList( 5120, "iso" ) ).thenReturn( syslist5120 );
        Mockito.when( daoSysList.getSysList( 5153, "iso" ) ).thenReturn( syslist5153 );
        Mockito.when( daoSysList.getSysList( 5200, "iso" ) ).thenReturn( syslist5200 );
        Mockito.when( daoSysList.getSysList( 6005, "de" ) ).thenReturn( syslist6005 );
        Mockito.when( daoSysList.getSysList( 6010, "iso" ) ).thenReturn( syslist6010 );
        Mockito.when( daoSysList.getSysList( 6020, "iso" ) ).thenReturn( syslist6020 );
        Mockito.when( daoSysList.getSysList( 6100, "iso" ) ).thenReturn( syslist6100 );
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
        assertThat( result.getBoolean( "success" ), is( true ) );
    }

    @Test
    public void analyzeCswDocumentInsert_nonGeographicDataset() throws Exception {

        Mockito.doAnswer( new Answer<Void>() {
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
        } ).when( jobHandler ).updateJobInfoDB( (JobType) Mockito.any(), (HashMap) Mockito.any(), Mockito.anyString() );

        IngridDocument docIn = prepareInsertDocument( "csw/insert_nonGeographicDataset.xml" );
        IngridDocument analyzeImportData = catJob.analyzeImportData( docIn );
        assertThat( analyzeImportData.get( "error" ), is( nullValue() ) );
        ProtocolHandler protocol = (ProtocolHandler) analyzeImportData.get( "protocol" );
        assertThat( protocol.getProtocol( Type.ERROR ).size(), is( 0 ) );
        assertThat( protocol.getProtocol( Type.WARN ).size(), is( 13 ) );
        assertThat( protocol.getProtocol( Type.INFO ).size(), is( not( 0 ) ) );

    }

    @Test
    public void analyzeCswDocumentInsert_service() throws Exception {

        Mockito.doAnswer( new Answer<Void>() {
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
                    // TODO: is encoded in linksToUrlTable: http://some.pic.com

                    // check abstract
                    assertThat( docOut.getString( MdekKeys.ABSTRACT ), is( "Dienst für den Test um externe gekoppelte Datensätze hinzuzufügen" ) );

                    // check address
                    List<IngridDocument> addresses = (List<IngridDocument>) docOut.get( MdekKeys.ADR_REFERENCES_TO );
                    // gets a new UUID!!!
                    // assertThat( addresses.get( 0 ).getString( MdekKeys.UUID ), is( "3E1B7F21-4E56-11D3-9A6B-0060971A0BF7" ) );
                    assertThat( addresses.get( 0 ).getInt( MdekKeys.RELATION_TYPE_ID ), is( 7 ) );
                    assertThat( addresses.get( 0 ).getInt( MdekKeys.RELATION_TYPE_REF ), is( 505 ) );

                    // inspire relevant
                    assertThat( docOut.getString( MdekKeys.IS_INSPIRE_RELEVANT ), is( "Y" ) );

                    // open data
                    assertThat( docOut.getString( MdekKeys.IS_OPEN_DATA ), is( "Y" ) );
                    // not in keywords anymore after correct analyze: assertSubjectTerms( docOut.getArrayList( MdekKeys.SUBJECT_TERMS ),
                    // "opendata" );

                    // INSPIRE-topics
                    assertThat( docOut.getArrayList( MdekKeys.SUBJECT_TERMS_INSPIRE ).size(), is( 2 ) );
                    assertSubjectTerms( docOut.getArrayList( MdekKeys.SUBJECT_TERMS_INSPIRE ), "Biogeografische Regionen", "Gebäude" );

                    // optional topics
                    assertThat( docOut.getArrayList( MdekKeys.SUBJECT_TERMS ).size(), is( 6 ) );
                    assertSubjectTerms( docOut.getArrayList( MdekKeys.SUBJECT_TERMS ), "Adaptronik", "Umwelt und Klima", "Gesundheit", "Energy", "Kabal", "Erdsystem" );

                    // environment topics
                    // NOT mapped in ISO!?
                    // TODO: assertThat( docOut.getString( MdekKeys.IS_CATALOG_DATA ), is( "Y" ) );
                    // TODO: assertThat( docOut.getString( MdekKeys.ENV_TOPICS ), is( nullValue() ) );

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
                    // NOT mapped in ISO!?
                    // TODO: assertThat( serviceMap.get( MdekKeys.PUBLICATION_SCALE_LIST ), is( nullValue() ) );

                    // system
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

                    // spatial ref
                    // docOut.get( MdekKeys.SPATIAL_REPRESENTATION_TYPE_LIST )

                    // free spatial ref

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
                    assertThat( ((IngridDocument) docOut.getArrayList( MdekKeys.CONFORMITY_LIST ).get( 0 )).getString( MdekKeys.CONFORMITY_SPECIFICATION_VALUE ),
                            is( "Technical Guidance for the implementation of INSPIRE Download Services" ) );
                    assertThat( ((IngridDocument) docOut.getArrayList( MdekKeys.CONFORMITY_LIST ).get( 0 )).getString( MdekKeys.CONFORMITY_DEGREE_VALUE ), is( "konform" ) );

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
                    // TODO: why is size 2 with first one "license"? assertThat( docOut.getArrayList( MdekKeys.USE_CONSTRAINTS).size(), is(
                    // 1 ) );
                    assertThat( ((IngridDocument) docOut.getArrayList( MdekKeys.USE_CONSTRAINTS ).get( 1 )).getString( MdekKeys.USE_LICENSE_VALUE ), is( "eingeschränkte Geolizenz" ) );

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
                    assertThat( ((IngridDocument) docOut.getArrayList( MdekKeys.MEDIUM_OPTIONS ).get( 0 )).getInt( MdekKeys.MEDIUM_NAME ), is( 1 ) );
                    assertThat( (Double) ((IngridDocument) docOut.getArrayList( MdekKeys.MEDIUM_OPTIONS ).get( 0 )).get( MdekKeys.MEDIUM_TRANSFER_SIZE ), is( 700.0 ) );
                    assertThat( ((IngridDocument) docOut.getArrayList( MdekKeys.MEDIUM_OPTIONS ).get( 0 )).getString( MdekKeys.MEDIUM_NOTE ), is( "c:/" ) );

                    // order info
                    assertThat( docOut.getString( MdekKeys.ORDERING_INSTRUCTIONS ), is( "keine Bestellung" ) );

                    // links to

                    // links from

                    List<Object> locs = docOut.getArrayList( MdekKeys.LOCATIONS );
                    List<Object> links = docOut.getArrayList( MdekKeys.LINKAGES );
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

        } ).when( jobHandler ).updateJobInfoDB( (JobType) Mockito.any(), (HashMap) Mockito.any(), Mockito.anyString() );

        IngridDocument docIn = prepareInsertDocument( "csw/insert_service.xml" );
        IngridDocument analyzeImportData = catJob.analyzeImportData( docIn );
        
        Mockito.verify( catJob, Mockito.times( 1 ) ).analyzeImportData( (IngridDocument) Mockito.any() );
        
        assertThat( analyzeImportData.get( "error" ), is( nullValue() ) );
        ProtocolHandler protocol = (ProtocolHandler) analyzeImportData.get( "protocol" );
        assertThat( protocol.getProtocol( Type.ERROR ).size(), is( 0 ) );
        assertThat( protocol.getProtocol( Type.WARN ).size(), is( 2 ) );
        assertThat( protocol.getProtocol( Type.INFO ).size(), is( not( 0 ) ) );
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
        ClassPathResource inputResource = new ClassPathResource( filename );
        File file = inputResource.getFile();
        String xml = FileUtils.readFileToString( file );

        // extract csw-document
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        Document xmlDoc = builder.parse( new InputSource( new StringReader( xml ) ) );
        NodeList insert = xmlDoc.getElementsByTagName( "csw:Insert" );

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
        return docIn;
    }

    @Test
    public void handleDocumentInsert() throws IOException {

        // Mockito.when( objectJob.storeObject( (IngridDocument) Mockito.any() ) ).then( new Answer<IngridDocument>() {
        //
        // @Override
        // public IngridDocument answer(InvocationOnMock invocation) throws Throwable {
        // IngridDocument doc = invocation.getArgumentAt( 0, IngridDocument.class );
        // assertThat( doc.getString( MdekKeys.ID ), is( "993E6356-D262-43AD-A69D-FE8EF62189A4" ) );
        // return null;
        // }
        // } );

        ClassPathResource inputResource = new ClassPathResource( "csw/insert.xml" );
        File file = inputResource.getFile();
        String xml = FileUtils.readFileToString( file );
        IngridDocument result = plug.cswTransaction( xml );

        assertThat( result, is( not( nullValue() ) ) );
        assertThat( result.getBoolean( "success" ), is( true ) );
        TransactionResponse resultXml = (TransactionResponse) result.get( "result" );
        assertThat( resultXml, is( not( nullValue() ) ) );
        // assertThat( resultXml.getActionResponses().size(), is( 1 ) );
        checkXmlResponse( resultXml.getXmlResponse(), 1, 0, 0 );
    }

    @Test
    public void handleDocumentUpdate() throws IOException {
        ClassPathResource inputResource = new ClassPathResource( "csw/update_dataset.xml" );
        File file = inputResource.getFile();
        
        Mockito.doAnswer( new Answer<Void>() {
            public Void answer(InvocationOnMock invocation) throws Exception {
                IngridDocument doc = (IngridDocument) invocation.getArgumentAt( 0, Map.class );
                assertThat( doc.getString( MdekKeys.USER_ID ), is( "TEST_USER_ID" ));
                assertThat( doc.getString( MdekKeys.UUID ), is( "1234-5678-abcd-efgh" ));
                assertThat( doc.getBoolean( MdekKeys.REQUESTINFO_FORCE_DELETE_REFERENCES ), is( false ));
                return null;                
            }
        }).when( objectJobMock ).storeObject( (IngridDocument) Mockito.any() );
        
        
        String xml = FileUtils.readFileToString( file );
        IngridDocument result = plug.cswTransaction( xml );
        
        Mockito.verify( objectJobMock, Mockito.times( 1 ) ).storeObject( (IngridDocument) Mockito.any() );

        assertThat( result, is( not( nullValue() ) ) );
        assertThat( result.getBoolean( "success" ), is( true ) );
    }

    @Test
    public void handleDocumentDelete() throws IOException {
        ClassPathResource inputResource = new ClassPathResource( "csw/delete_dataset.xml" );
        File file = inputResource.getFile();
        
        Mockito.doAnswer( new Answer<Void>() {
            public Void answer(InvocationOnMock invocation) throws Exception {
                IngridDocument doc = (IngridDocument) invocation.getArgumentAt( 0, Map.class );
                assertThat( doc.getString( MdekKeys.USER_ID ), is( "TEST_USER_ID" ));
                assertThat( doc.getString( MdekKeys.UUID ), is( "1234-5678-abcd-efgh" ));
                assertThat( doc.getBoolean( MdekKeys.REQUESTINFO_FORCE_DELETE_REFERENCES ), is( false ));
                return null;                
            }
        }).when( objectJobMock ).deleteObject( (IngridDocument) Mockito.any() );
        
        
        String xml = FileUtils.readFileToString( file );
        IngridDocument result = plug.cswTransaction( xml );
        
        Mockito.verify( objectJobMock, Mockito.times( 1 ) ).deleteObject( (IngridDocument) Mockito.any() );

        assertThat( result, is( not( nullValue() ) ) );
        assertThat( result.getBoolean( "success" ), is( true ) );
    }

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
