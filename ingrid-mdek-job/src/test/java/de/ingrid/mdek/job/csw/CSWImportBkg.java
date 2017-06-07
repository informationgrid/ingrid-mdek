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
import org.apache.jasper.tagplugins.jstl.ForEach;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

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

@PowerMockIgnore("javax.management.*")
@RunWith(PowerMockRunner.class)
@PrepareForTest({ DatabaseConnectionUtils.class, MdekObjectService.class, MdekJobHandler.class })
public class CSWImportBkg {

    private IgeSearchPlug plug;

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

    @Mock
    private DatabaseConnectionUtils dcUtils;
    @Mock
    private Connection connectionMock;
    @Mock
    private PreparedStatement ps;
    @Mock
    private ResultSet resultSet;

    @Mock
    private MdekObjectService mdekObjectService;

    private IngridXMLMapper importMapper;

    @Before
    public void before() throws Exception {

        plug = new IgeSearchPlug( null, null, null, null, null );

        when( daoFactory.getDao( IEntity.class ) ).thenReturn( genericDao );
        HashMap<String, List<byte[]>> analyzedDataMap = new HashMap<String, List<byte[]>>();
        analyzedDataMap.put( MdekKeys.REQUESTINFO_IMPORT_ANALYZED_DATA, new ArrayList<byte[]>() );
        when( jobHandler.getJobDetailsAsHashMap( JobType.IMPORT_ANALYZE, "TEST_USER_ID" ) ).thenReturn( analyzedDataMap );
        when( jobHandler.createRunningJobDescription( JobType.IMPORT, 0, 0, false ) ).thenReturn( new IngridDocument() );
        when( jobHandler.getRunningJobInfo( any( String.class ) ) ).thenReturn( new IngridDocument() );
        when( permissionService.isCatalogAdmin( "TEST_USER_ID" ) ).thenReturn( true );

        PowerMockito.mockStatic( DatabaseConnectionUtils.class );
        when( DatabaseConnectionUtils.getInstance() ).thenReturn( dcUtils );
        when( dcUtils.openConnection( any( DatabaseConnection.class ) ) ).thenReturn( connectionMock );
        when( connectionMock.prepareStatement( any( String.class ) ) ).thenReturn( ps );
        when( ps.executeQuery() ).thenReturn( resultSet );

        PowerMockito.mockStatic( MdekObjectService.class );
        when( MdekObjectService.getInstance( any( DaoFactory.class ), any( IPermissionService.class ) ) ).thenReturn( mdekObjectService );

        PowerMockito.mockStatic( MdekJobHandler.class );
        when( MdekJobHandler.getInstance( any( DaoFactory.class ) ) ).thenReturn( jobHandler );

        ClassPathResource inputResource = new ClassPathResource( "csw/importAdditionalField.xml" );
//        File file = inputResource.getFile();
//        String xml = FileUtils.readFileToString( file );
//        when( resultSet.getString( any( String.class ) ) ).thenReturn( xml );

        when( daoFactory.getSysListDao() ).thenReturn( daoSysList );

        mockSyslists();

        when( daoFactory.getDao( T03Catalogue.class ) ).thenReturn( daoT03Catalogue );
        T03Catalogue t03Catalogue = new T03Catalogue();
        t03Catalogue.setLanguageKey( 150 );
        when( daoT03Catalogue.findFirst() ).thenReturn( t03Catalogue );
        when( catJobMock.getCatalogAdminUserUuid() ).thenReturn( "TEST_USER_ID" );

        cswMapper = new ScriptImportDataMapper( daoFactory );
        cswMapper.setCatalogService( MdekCatalogService.getInstance( daoFactory ) );

        catJob = new MdekIdcCatalogJob( logService, daoFactory, permissionService );
        DataMapperFactory dataMapperFactory = new DataMapperFactory();
        HashMap<String, ImportDataMapper> mapper = new HashMap<String, ImportDataMapper>();
        FileSystemResource[] resources = new FileSystemResource[2];
        String absPath = inputResource.getFile().getAbsolutePath();
        int pos = absPath.indexOf( "ingrid-mdek-job" );
        resources[0] = new FileSystemResource( absPath.substring( 0, pos ) + "ingrid-mdek-job/src/main/resources/import/mapper/csw202_to_ingrid_igc.js" );
        resources[1] = new FileSystemResource( absPath.substring( 0, pos ) + "distribution/src/profiles/bkg/conf/import/mapper/csw202_to_ingrid_igc_bkg.js" );
        cswMapper.setMapperScript( resources );
        cswMapper.setTemplate( new ClassPathResource( "import/templates/igc_template_csw202.xml" ) );
        mapper.put( "csw202", cswMapper );
        dataMapperFactory.setMapperClasses( mapper );
        catJob.setDataMapperFactory( dataMapperFactory );
        catJob.setJobHandler( jobHandler );
        // plug.setCswTransaction( trans );
        // plug.setCatalogJob( catJob );
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
        List<SysList> syslist10002 = createSyslist( 10002, 4, "Diese Daten oder dieser Dienst stehen/steht nur ausgewählten Bundesbehörden zur Verfügung." );
        extendSyslist( syslist10002, 1, "Es gelten keine Zugriffsbeschränkungen" );

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
        when( daoSysList.getSysList( 10002, "de" ) ).thenReturn( syslist10002 );
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

    @SuppressWarnings("rawtypes")
    @Test
    public void bkgCsw1() throws Exception {

        doAnswer( new Answer<Void>() {
            @SuppressWarnings("unchecked")
            public Void answer(InvocationOnMock invocation) throws Exception {

                IngridDocument docOut = getDocument( invocation, "4915275a-733a-47cd-b1a6-1a3f1e976948" );
                List<IngridDocument> addFields = (List<IngridDocument>) docOut.get( MdekKeys.ADDITIONAL_FIELDS );

                assertThat( addFields.size(), is( 1 ) );
                assertThat( addFields.get( 0 ).get(  MdekKeys.ADDITIONAL_FIELD_KEY ), is( "bkg_accessConstraints" ) );
                
                List<List<IngridDocument>> rowsTemp = (List<List<IngridDocument>>) addFields.get( 0 ).get(  MdekKeys.ADDITIONAL_FIELD_ROWS );
                List<IngridDocument> rows = rowsTemp.get( 0 );
                assertThat( rows.size(), is( 2 ) );
                assertThat( rows.get( 0 ).get( MdekKeys.ADDITIONAL_FIELD_KEY ), is( "bkg_accessConstraints_select" ) );
                assertThat( rows.get( 0 ).get( MdekKeys.ADDITIONAL_FIELD_LIST_ITEM_ID ), is( "4" ) );
                assertThat( rows.get( 1 ).get( MdekKeys.ADDITIONAL_FIELD_KEY ), is( "bkg_accessConstraints_freeText" ) );
                assertThat( rows.get( 1 ).get( MdekKeys.ADDITIONAL_FIELD_DATA ), is( "mein freier Text" ) );
                
                List<IngridDocument> accessList = (List<IngridDocument>) docOut.get( MdekKeys.ACCESS_LIST );
                for (IngridDocument access : accessList) {
                    assertThat( access.get( MdekKeys.ACCESS_RESTRICTION_VALUE), not( is( "Diese Daten oder dieser Dienst stehen/steht nur ausgewählten Bundesbehörden zur Verfügung." ) ) );
                    assertThat( access.get( MdekKeys.ACCESS_RESTRICTION_VALUE), not( is( "mein freier Text" ) ) );
                    assertThat( access.get( MdekKeys.ACCESS_RESTRICTION_VALUE), not( is( "copyright" ) ) );
                }

                return null;
            }

        } ).when( jobHandler ).updateJobInfoDB( (JobType) any(), (HashMap) any(), anyString() );

        IngridDocument docIn = prepareInsertDocument( "csw/bkg/doc1.xml" );
        IngridDocument analyzeImportData = catJob.analyzeImportData( docIn );

        assertThat( analyzeImportData.get( "error" ), is( nullValue() ) );
        ProtocolHandler protocol = (ProtocolHandler) analyzeImportData.get( "protocol" );
        assertThat( protocol.getProtocol( Type.ERROR ).size(), is( 0 ) );
    }
    
    @SuppressWarnings("rawtypes")
    @Test
    public void bkgCsw2() throws Exception {

        doAnswer( new Answer<Void>() {
            @SuppressWarnings("unchecked")
            public Void answer(InvocationOnMock invocation) throws Exception {

                IngridDocument docOut = getDocument( invocation, "4915275a-733a-47cd-b1a6-1a3f1e976948" );
                List<IngridDocument> addFields = (List<IngridDocument>) docOut.get( MdekKeys.ADDITIONAL_FIELDS );

                assertThat( addFields.size(), is( 1 ) );
                assertThat( addFields.get( 0 ).get(  MdekKeys.ADDITIONAL_FIELD_KEY ), is( "bkg_accessConstraints" ) );
                
                List<List<IngridDocument>> rowsTemp = (List<List<IngridDocument>>) addFields.get( 0 ).get(  MdekKeys.ADDITIONAL_FIELD_ROWS );
                List<IngridDocument> rows = rowsTemp.get( 0 );
                assertThat( rows.size(), is( 2 ) );
                assertThat( rows.get( 0 ).get( MdekKeys.ADDITIONAL_FIELD_KEY ), is( "bkg_accessConstraints_select" ) );
                assertThat( rows.get( 0 ).get( MdekKeys.ADDITIONAL_FIELD_LIST_ITEM_ID ), is( "1" ) );
                assertThat( rows.get( 1 ).get( MdekKeys.ADDITIONAL_FIELD_KEY ), is( "bkg_accessConstraints_freeText" ) );
                assertThat( rows.get( 1 ).get( MdekKeys.ADDITIONAL_FIELD_DATA ), is( "mein freier Text" ) );

                List<IngridDocument> accessList = (List<IngridDocument>) docOut.get( MdekKeys.ACCESS_LIST );
                for (IngridDocument access : accessList) {
                    assertThat( access.get( MdekKeys.ACCESS_RESTRICTION_VALUE), not( is( "Es gelten keine Zugriffsbeschränkungen" ) ) );
                    assertThat( access.get( MdekKeys.ACCESS_RESTRICTION_VALUE), not( is( "mein freier Text" ) ) );
                }
                
                return null;
            }

        } ).when( jobHandler ).updateJobInfoDB( (JobType) any(), (HashMap) any(), anyString() );

        IngridDocument docIn = prepareInsertDocument( "csw/bkg/doc2.xml" );
        IngridDocument analyzeImportData = catJob.analyzeImportData( docIn );

        assertThat( analyzeImportData.get( "error" ), is( nullValue() ) );
        ProtocolHandler protocol = (ProtocolHandler) analyzeImportData.get( "protocol" );
        assertThat( protocol.getProtocol( Type.ERROR ).size(), is( 0 ) );
    }
    
    @SuppressWarnings("rawtypes")
    @Test
    public void bkgCsw3() throws Exception {
        
        doAnswer( new Answer<Void>() {
            @SuppressWarnings("unchecked")
            public Void answer(InvocationOnMock invocation) throws Exception {
                
                IngridDocument docOut = getDocument( invocation, "4915275a-733a-47cd-b1a6-1a3f1e976948" );
                List<IngridDocument> addFields = (List<IngridDocument>) docOut.get( MdekKeys.ADDITIONAL_FIELDS );
                
                assertThat( addFields.size(), is( 1 ) );
                assertThat( addFields.get( 0 ).get(  MdekKeys.ADDITIONAL_FIELD_KEY ), is( "bkg_accessConstraints" ) );
                
                List<List<IngridDocument>> rowsTemp = (List<List<IngridDocument>>) addFields.get( 0 ).get(  MdekKeys.ADDITIONAL_FIELD_ROWS );
                List<IngridDocument> rows = rowsTemp.get( 0 );
                assertThat( rows.size(), is( 2 ) );
                assertThat( rows.get( 0 ).get( MdekKeys.ADDITIONAL_FIELD_KEY ), is( "bkg_accessConstraints_select" ) );
                assertThat( rows.get( 0 ).get( MdekKeys.ADDITIONAL_FIELD_LIST_ITEM_ID ), is( "1" ) );
                assertThat( rows.get( 1 ).get( MdekKeys.ADDITIONAL_FIELD_KEY ), is( "bkg_accessConstraints_freeText" ) );
                assertThat( rows.get( 1 ).get( MdekKeys.ADDITIONAL_FIELD_DATA ), is( "" ) );
                
                List<IngridDocument> accessList = (List<IngridDocument>) docOut.get( MdekKeys.ACCESS_LIST );
                for (IngridDocument access : accessList) {
                    assertThat( access.get( MdekKeys.ACCESS_RESTRICTION_VALUE), not( is( "Es gelten keine Zugriffsbeschränkungen" ) ) );
                }
                
                return null;
            }
            
        } ).when( jobHandler ).updateJobInfoDB( (JobType) any(), (HashMap) any(), anyString() );
        
        IngridDocument docIn = prepareInsertDocument( "csw/bkg/doc3.xml" );
        IngridDocument analyzeImportData = catJob.analyzeImportData( docIn );
        
        assertThat( analyzeImportData.get( "error" ), is( nullValue() ) );
        ProtocolHandler protocol = (ProtocolHandler) analyzeImportData.get( "protocol" );
        assertThat( protocol.getProtocol( Type.ERROR ).size(), is( 0 ) );
    }
    
    @SuppressWarnings("rawtypes")
    @Test
    public void bkgCsw4() throws Exception {
        
        doAnswer( new Answer<Void>() {
            @SuppressWarnings("unchecked")
            public Void answer(InvocationOnMock invocation) throws Exception {
                
                IngridDocument docOut = getDocument( invocation, "4915275a-733a-47cd-b1a6-1a3f1e976948" );
                List<IngridDocument> addFields = (List<IngridDocument>) docOut.get( MdekKeys.ADDITIONAL_FIELDS );
                
                assertThat( addFields.size(), is( 1 ) );
                assertThat( addFields.get( 0 ).get(  MdekKeys.ADDITIONAL_FIELD_KEY ), is( "bkg_accessConstraints" ) );
                
                List<List<IngridDocument>> rowsTemp = (List<List<IngridDocument>>) addFields.get( 0 ).get(  MdekKeys.ADDITIONAL_FIELD_ROWS );
                List<IngridDocument> rows = rowsTemp.get( 0 );
                assertThat( rows.size(), is( 2 ) );
                assertThat( rows.get( 0 ).get( MdekKeys.ADDITIONAL_FIELD_KEY ), is( "bkg_accessConstraints_select" ) );
                assertThat( rows.get( 0 ).get( MdekKeys.ADDITIONAL_FIELD_LIST_ITEM_ID ), is( nullValue() ) );
                assertThat( rows.get( 1 ).get( MdekKeys.ADDITIONAL_FIELD_KEY ), is( "bkg_accessConstraints_freeText" ) );
                assertThat( rows.get( 1 ).get( MdekKeys.ADDITIONAL_FIELD_DATA ), is( "mein freier Text" ) );
                
                List<IngridDocument> accessList = (List<IngridDocument>) docOut.get( MdekKeys.ACCESS_LIST );
                for (IngridDocument access : accessList) {
                    assertThat( access.get( MdekKeys.ACCESS_RESTRICTION_VALUE), not( is( "mein freier Text" ) ) );
                }
                
                return null;
            }
            
        } ).when( jobHandler ).updateJobInfoDB( (JobType) any(), (HashMap) any(), anyString() );
        
        IngridDocument docIn = prepareInsertDocument( "csw/bkg/doc4.xml" );
        IngridDocument analyzeImportData = catJob.analyzeImportData( docIn );
        
        assertThat( analyzeImportData.get( "error" ), is( nullValue() ) );
        ProtocolHandler protocol = (ProtocolHandler) analyzeImportData.get( "protocol" );
        assertThat( protocol.getProtocol( Type.ERROR ).size(), is( 0 ) );
    }
    
    @SuppressWarnings("rawtypes")
    @Test
    public void bkgCsw5() throws Exception {
        
        doAnswer( new Answer<Void>() {
            @SuppressWarnings("unchecked")
            public Void answer(InvocationOnMock invocation) throws Exception {
                
                IngridDocument docOut = getDocument( invocation, "4915275a-733a-47cd-b1a6-1a3f1e976948" );
                List<IngridDocument> addFields = (List<IngridDocument>) docOut.get( MdekKeys.ADDITIONAL_FIELDS );
                
                assertThat( addFields.size(), is( 1 ) );
                assertThat( addFields.get( 0 ).get(  MdekKeys.ADDITIONAL_FIELD_KEY ), is( "bkg_accessConstraints" ) );
                
                List<List<IngridDocument>> rowsTemp = (List<List<IngridDocument>>) addFields.get( 0 ).get(  MdekKeys.ADDITIONAL_FIELD_ROWS );
                List<IngridDocument> rows = rowsTemp.get( 0 );
                assertThat( rows.size(), is( 2 ) );
                assertThat( rows.get( 0 ).get( MdekKeys.ADDITIONAL_FIELD_KEY ), is( "bkg_accessConstraints_select" ) );
                assertThat( rows.get( 0 ).get( MdekKeys.ADDITIONAL_FIELD_LIST_ITEM_ID ), is( "7" ) );
                assertThat( rows.get( 1 ).get( MdekKeys.ADDITIONAL_FIELD_KEY ), is( "bkg_accessConstraints_freeText" ) );
                assertThat( rows.get( 1 ).get( MdekKeys.ADDITIONAL_FIELD_DATA ), is( "mein freier Text" ) );
                
                List<IngridDocument> accessList = (List<IngridDocument>) docOut.get( MdekKeys.ACCESS_LIST );
                for (IngridDocument access : accessList) {
                    assertThat( access.get( MdekKeys.ACCESS_RESTRICTION_VALUE), not( is( "mein freier Text" ) ) );
                    assertThat( access.get( MdekKeys.ACCESS_RESTRICTION_VALUE), not( is( "copyright" ) ) );
                    assertThat( access.get( MdekKeys.ACCESS_RESTRICTION_VALUE), not( is( "license" ) ) );
                }
                
                return null;
            }
            
        } ).when( jobHandler ).updateJobInfoDB( (JobType) any(), (HashMap) any(), anyString() );
        
        IngridDocument docIn = prepareInsertDocument( "csw/bkg/doc5.xml" );
        IngridDocument analyzeImportData = catJob.analyzeImportData( docIn );
        
        assertThat( analyzeImportData.get( "error" ), is( nullValue() ) );
        ProtocolHandler protocol = (ProtocolHandler) analyzeImportData.get( "protocol" );
        assertThat( protocol.getProtocol( Type.ERROR ).size(), is( 0 ) );
    }
    
    

    private IngridDocument prepareInsertDocument(String filename) throws Exception {
        return prepareDocument( filename, "csw:Insert" );
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

        docIn.put( MdekKeys.REQUESTINFO_IMPORT_OBJ_PARENT_UUID, "2768376B-EE24-4F34-969B-084C55B52278" ); // IMPORTKNOTEN
        docIn.put( MdekKeys.REQUESTINFO_IMPORT_ADDR_PARENT_UUID, "BD33BC8E-519E-47F9-8A30-465C95CD0355" ); // IMPORTKNOTEN
        return docIn;
    }

    private IngridDocument getDocument(InvocationOnMock invocation, String uuid) {
        Map doc = invocation.getArgumentAt( 1, Map.class );
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
}
