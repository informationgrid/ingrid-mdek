/*-
 * **************************************************-
 * InGrid mdek-job
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
package de.ingrid.mdek.job.utils;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import de.ingrid.elasticsearch.ElasticConfig;
import de.ingrid.iplug.dsc.utils.SQLUtils;
import de.ingrid.mdek.job.util.IgeCswFolderUtil;
import de.ingrid.mdek.xml.Versioning;
import de.ingrid.utils.PlugDescription;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.w3c.dom.Document;

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

@ExtendWith(MockitoExtension.class)
public class TestSetup {

    @Mock
    protected ISysListDao daoSysList;

    @Mock
    protected MdekJobHandler jobHandler;

    protected MdekIdcCatalogJob catJob;

    @Mock
    protected IImporterCallback importerCallback;

    @Mock
    ILogService logService;

    @Mock
    DaoFactory daoFactory;

    @Mock
    IPermissionService permissionService;

    @Mock
    private BasicDataSource dataSourceMock;
    @Mock
    private DatabaseConnectionUtils dcUtils;
    @Mock
    private Connection connectionMock;
    @Mock
    private PreparedStatement ps;

    @Mock
    GenericHibernateDao<IEntity> genericDao;

    @Mock
    IGenericDao<IEntity> daoT03Catalogue;


    @Mock
    protected MdekIdcCatalogJob catJobMock;

    @Mock
    protected MdekIdcObjectJob objectJobMock;


    @Mock
    private ResultSet resultSet;

    @Mock
    private MdekObjectService mdekObjectService;

    @Mock IndexManager indexManager;

    @Mock ElasticConfig elasticConfig;

    @Mock private IgeCswFolderUtil igeCswFolderUtil;

    ScriptImportDataMapper cswMapper;

    protected IgeSearchPlug plug;


    protected IngridXMLMapper importMapper;
    protected MockedStatic<DatabaseConnectionUtils> mockedDatabaseConnectionUtils;

    public TestSetup() {
        importMapper = IngridXMLMapperFactory.getIngridXMLMapper( Versioning.CURRENT_IMPORT_EXPORT_VERSION );
    }

    protected void beforeSetup(String[] mappingScripts) throws Exception {
        mockedDatabaseConnectionUtils = mockStatic( DatabaseConnectionUtils.class );
        mockedDatabaseConnectionUtils.when(DatabaseConnectionUtils::getInstance).thenReturn(dcUtils);
        lenient().when( dcUtils.openConnection( any( DatabaseConnection.class ) ) ).thenReturn( connectionMock );
        lenient().when( connectionMock.prepareStatement( any( String.class ) ) ).thenReturn( ps );
        lenient().when( ps.executeQuery() ).thenReturn( resultSet );
        
        plug = new IgeSearchPlug( null, null, null, null, null );

        elasticConfig.esCommunicationThroughIBus = false;
        /*Boolean spyEsThroughIBus = spy(elasticConfig.esCommunicationThroughIBus);
        //when( elasticConfig.esCommunicationThroughIBus).thenReturn( Boolean.getBoolean("false") );
        doReturn(false).when(spyEsThroughIBus);*/

        when( daoFactory.getDao( IEntity.class ) ).thenReturn( genericDao );
        HashMap<String, List<byte[]>> analyzedDataMap = new HashMap<String, List<byte[]>>();
        analyzedDataMap.put( MdekKeys.REQUESTINFO_IMPORT_ANALYZED_DATA, new ArrayList<byte[]>() );
        lenient().when( jobHandler.getJobDetailsAsHashMap( JobType.IMPORT_ANALYZE, "TEST_USER_ID" ) ).thenReturn( analyzedDataMap );
        lenient().when( jobHandler.createRunningJobDescription( JobType.IMPORT_ANALYZE, 0, 0, false ) ).thenReturn( new IngridDocument() );
        lenient().when( jobHandler.getRunningJobInfo( any( String.class ) ) ).thenReturn( new IngridDocument() );
        lenient().when( permissionService.isCatalogAdmin( "TEST_USER_ID" ) ).thenReturn( true );

        lenient().when( dataSourceMock.getConnection() ).thenReturn( connectionMock );

        try (MockedStatic<MdekObjectService> mocked = mockStatic( MdekObjectService.class )) {
            mocked.when( () -> MdekObjectService.getInstance( any( DaoFactory.class ), any( IPermissionService.class ) ) ).thenReturn( mdekObjectService );
        }

        try (MockedStatic<MdekJobHandler> mocked = mockStatic( MdekJobHandler.class )) {
            mocked.when(() -> MdekJobHandler.getInstance(any(DaoFactory.class))).thenReturn(jobHandler);
        }


        when( daoFactory.getSysListDao() ).thenReturn( daoSysList );

        mockSyslists();

        lenient().when( daoFactory.getDao( T03Catalogue.class ) ).thenReturn( daoT03Catalogue );
        T03Catalogue t03Catalogue = new T03Catalogue();
        t03Catalogue.setLanguageKey( 150 );
        lenient().when( daoT03Catalogue.findFirst() ).thenReturn( t03Catalogue );
        lenient().when( catJobMock.getCatalogAdminUserUuid() ).thenReturn( "TEST_USER_ID" );

        cswMapper = new ScriptImportDataMapper( daoFactory , igeCswFolderUtil);
        cswMapper.setCatalogService( MdekCatalogService.getInstance( daoFactory ) );
        PlugDescription plugDescription = new PlugDescription();
        plugDescription.setConnection(new DatabaseConnection());
        cswMapper.configure(plugDescription);

        ScriptImportDataMapper mapperSpy = spy(cswMapper);
        SQLUtils sqlMock = mock(SQLUtils.class);
        lenient().when(mapperSpy.getSqlUtils(any())).thenReturn(sqlMock);

        Logger mockLogger = mock(Logger.class);
        when(logService.getLogger(any())).thenReturn(mockLogger);

        catJob = new MdekIdcCatalogJob( logService, daoFactory, permissionService, elasticConfig, indexManager, null );
        DataMapperFactory dataMapperFactory = new DataMapperFactory();
        HashMap<String, ImportDataMapper> mapper = new HashMap<String, ImportDataMapper>();

        FileSystemResource[] resources = new FileSystemResource[mappingScripts.length];
        ClassPathResource inputResource = new ClassPathResource( "csw/importAdditionalField.xml" );
        File file = inputResource.getFile();
        String xml = FileUtils.readFileToString( file );
        lenient().when( resultSet.getString( any(String.class) )).thenReturn( xml );

        String absPath = inputResource.getFile().getAbsolutePath();
        int pos = absPath.indexOf( "ingrid-mdek-job" );

        for (int i=0; i < mappingScripts.length; i++) {
            resources[i] = new FileSystemResource( absPath.substring( 0, pos ) + mappingScripts[i] );
        }
        mapperSpy.setMapperScript( resources );

        mapperSpy.setTemplate( new ClassPathResource( "/import/templates/igc_template_csw202.xml" ) );
        mapper.put( "csw202", mapperSpy );
        dataMapperFactory.setMapperClasses( mapper );
        catJob.setDataMapperFactory( dataMapperFactory );
        catJob.setJobHandler( jobHandler );
        // plug.setCswTransaction( trans );
        // plug.setCatalogJob( catJob );
        plug.setCatalogJob( catJobMock );
        plug.setObjectJob( objectJobMock );

//        openMocks(this);
    }

    protected void mockSyslists() {
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
        List<SysList> syslist5180 = createSyslist( 5180, 6, "WebServices" );
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
        List<SysList> syslist6400 = createSyslist( 6400, 6, "Gesundheit", "HEAL" );
        extendSyslist( syslist6400, 5, "Umwelt", "ENVI" );
        List<SysList> syslist6500 = createSyslist( 6500, 26, "Es gelten keine Bedingungen" );
        List<SysList> syslist10002 = createSyslist( 10002, 4, "Diese Daten oder dieser Dienst stehen/steht nur ausgewählten Bundesbehörden zur Verfügung." );
        extendSyslist( syslist10002, 1, "Es gelten keine Zugriffsbeschränkungen" );
        List<SysList> syslist10004 = createSyslist( 10004, 1, "Diese Daten können geldleistungsfrei gemäß der Verordnung zur Festlegung der Nutzungsbestimmungen für die Bereitstellung von Geodaten des Bundes (GeoNutzV) vom 19. März 2013 (Bundesgesetzblatt Jahrgang 2013 Teil I Nr. 14) genutzt werden, siehe http://www.geodatenzentrum.de/docpdf/geonutzv.pdf. Der Quellenvermerk ist zu beachten." );
        //extendSyslist( syslist10004, 12, "" );

        lenient().when( daoSysList.getSysList( 100, "iso" ) ).thenReturn( syslist100 );
        lenient().when( daoSysList.getSysList( 101, "iso" ) ).thenReturn( syslist101 );
        lenient().when( daoSysList.getSysList( 102, "iso" ) ).thenReturn( syslist102 );
        lenient().when( daoSysList.getSysList( 502, "iso" ) ).thenReturn( syslist502 );
        lenient().when( daoSysList.getSysList( 505, "iso" ) ).thenReturn( syslist505 );
        lenient().when( daoSysList.getSysList( 505, "de" ) ).thenReturn( syslist505 );
        lenient().when( daoSysList.getSysList( 510, "iso" ) ).thenReturn( syslist510 );
        lenient().when( daoSysList.getSysList( 518, "iso" ) ).thenReturn( syslist518 );
        lenient().when( daoSysList.getSysList( 520, "iso" ) ).thenReturn( syslist520 );
        lenient().when( daoSysList.getSysList( 523, "iso" ) ).thenReturn( syslist523 );
        lenient().when( daoSysList.getSysList( 524, "iso" ) ).thenReturn( syslist524 );
        lenient().when( daoSysList.getSysList( 526, "iso" ) ).thenReturn( syslist526 );
        lenient().when( daoSysList.getSysList( 527, "iso" ) ).thenReturn( syslist527 );
        lenient().when( daoSysList.getSysList( 1320, "iso" ) ).thenReturn( syslist1320 );
        lenient().when( daoSysList.getSysList( 1350, "iso" ) ).thenReturn( syslist1350 );
        lenient().when( daoSysList.getSysList( 1410, "iso" ) ).thenReturn( syslist1410 );
        lenient().when( daoSysList.getSysList( 5120, "iso" ) ).thenReturn( syslist5120 );
        lenient().when( daoSysList.getSysList( 5153, "iso" ) ).thenReturn( syslist5153 );
        lenient().when( daoSysList.getSysList( 5180, "iso" ) ).thenReturn( syslist5180 );
        lenient().when( daoSysList.getSysList( 5200, "iso" ) ).thenReturn( syslist5200 );
        lenient().when( daoSysList.getSysList( 6005, "de" ) ).thenReturn( syslist6005 );
        lenient().when( daoSysList.getSysList( 6010, "iso" ) ).thenReturn( syslist6010 );
        lenient().when( daoSysList.getSysList( 6020, "iso" ) ).thenReturn( syslist6020 );
        lenient().when( daoSysList.getSysList( 6100, "iso" ) ).thenReturn( syslist6100 );
        lenient().when( daoSysList.getSysList( 6400, "de" ) ).thenReturn( syslist6400 );
        lenient().when( daoSysList.getSysList( 6500, "de" ) ).thenReturn( syslist6500 );
        lenient().when( daoSysList.getSysList( 10002, "de" ) ).thenReturn( syslist10002 );
        lenient().when( daoSysList.getSysList( 10004, "de" ) ).thenReturn( syslist10004 );
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected IngridDocument getDocument(InvocationOnMock invocation, String uuid) {
        Map doc = invocation.getArgument( 1 );
        List<byte[]> data = (List<byte[]>) doc.get( MdekKeys.REQUESTINFO_IMPORT_ANALYZED_DATA );
        assertThat( data, is( not( nullValue() ) ) );
        assertThat( data.size(), is( 1 ) );
        try {
            InputStream in = new GZIPInputStream( new ByteArrayInputStream( data.get( 0 ) ) );
            IngridXMLStreamReader reader = new IngridXMLStreamReader( in, importerCallback, "TEST_USER_ID" );
            List<Document> domForObject = reader.getDomForObject( uuid );
            System.out.println( XMLUtils.toString( domForObject.get( 0 ) ) );
            return importMapper.mapDataSource( domForObject.get( 0 ) );
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private List<SysList> createSyslist(int listId, int entryId, String value) {
        return createSyslist( listId, entryId, value, null );
    }
    private List<SysList> createSyslist(int listId, int entryId, String value, String data) {
        List<SysList> syslist = new ArrayList<SysList>();
        SysList entry = new SysList();
        entry.setLstId( listId );
        entry.setEntryId( entryId );
        entry.setName( value );
        entry.setData( data );
        syslist.add( entry );
        return syslist;
    }

    private void extendSyslist(List<SysList> list, int entryId, String value) {
        extendSyslist( list, entryId, value, null );
    }
    private void extendSyslist(List<SysList> list, int entryId, String value, String data) {
        SysList entry = new SysList();
        entry.setLstId( list.get( 0 ).getLstId() );
        entry.setEntryId( entryId );
        entry.setName( value );
        entry.setData( data );
        list.add( entry );
    }


}
