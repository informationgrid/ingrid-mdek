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
import org.mockito.InjectMocks;
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
import de.ingrid.mdek.job.mapping.DataMapperFactory;
import de.ingrid.mdek.job.mapping.ImportDataMapper;
import de.ingrid.mdek.job.mapping.ScriptImportDataMapper;
import de.ingrid.mdek.services.catalog.MdekCatalogService;
import de.ingrid.mdek.services.log.ILogService;
import de.ingrid.mdek.services.persistence.db.DaoFactory;
import de.ingrid.mdek.services.persistence.db.GenericHibernateDao;
import de.ingrid.mdek.services.persistence.db.IEntity;
import de.ingrid.mdek.services.security.IPermissionService;
import de.ingrid.mdek.services.utils.MdekJobHandler;
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
    GenericHibernateDao genericDao;

    @InjectMocks
    ScriptImportDataMapper cswMapper;
    @Mock
    MdekCatalogService catalogService;

    private MdekIdcCatalogJob catJob;

    @Before
    public void before() throws Exception {
        // CswTransaction trans = new CswTransaction();
        // trans.setPersist( databasePersister );

        plug = new IgeSearchPlug( null, null, null, null, null );

        Mockito.when( daoFactory.getDao( IEntity.class ) ).thenReturn( genericDao );
        HashMap analyzedDataMap = new HashMap();
        analyzedDataMap.put( "analyzedData", new ArrayList<byte[]>() );
        Mockito.when( jobHandler.getJobDetailsAsHashMap( JobType.IMPORT_ANALYZE, "TEST_USER_ID" ) ).thenReturn( analyzedDataMap );

        cswMapper = new ScriptImportDataMapper( daoFactory );
        cswMapper.setCatalogService( catalogService );

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
        plug.setCatalogJob( catJob );
    }

    @Test
    public void handleNullValue() {
        IngridDocument result = plug.cswTransaction( null );

        assertThat( result, is( not( nullValue() ) ) );
        assertThat( result.getBoolean( "success" ), is( true ) );
    }

    @Test
    public void analyzeCswDocumentInsert() throws Exception {
        ClassPathResource inputResource = new ClassPathResource( "csw/insert.xml" );
        File file = inputResource.getFile();
        String xml = FileUtils.readFileToString( file );
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        Document xmlDoc = builder.parse( new InputSource( new StringReader( xml ) ) );
        NodeList insert = xmlDoc.getElementsByTagName( "csw:Insert" );

        Document singleInsertDocument = builder.newDocument();
        Node importedNode = singleInsertDocument.importNode( insert.item( 0 ).getFirstChild().getNextSibling(), true );
        singleInsertDocument.appendChild( importedNode );
        String insertDoc = XMLUtils.toString( singleInsertDocument );

        Mockito.doAnswer( new Answer<Void>() {
            public Void answer(InvocationOnMock invocation) {
                Map doc = invocation.getArgumentAt( 1, Map.class );
                List<byte[]> data = (List<byte[]>) doc.get( "analyzedData" );
                assertThat( data, is( not( nullValue() ) ) );
                assertThat( data.size(), is( 1 ) );
                //InputStream in = new GZIPInputStream(new ByteArrayInputStream(data.get( 0 )));
                //new StringWriter()
                //new StringReader( data.get(0));
                return null;
            }
        } ).when( jobHandler ).updateJobInfoDB( (JobType) Mockito.any(), (HashMap) Mockito.any(), Mockito.anyString() );

        IngridDocument docIn = new IngridDocument();
        docIn.put( MdekKeys.USER_ID, "TEST_USER_ID" );
        // docIn.put( MdekKeys.REQUESTINFO_IMPORT_DATA, GZipTool.gzip( insertDoc ).getBytes());
        docIn.put( MdekKeys.REQUESTINFO_IMPORT_DATA, catJob.compress( new ByteArrayInputStream( insertDoc.getBytes() ) ).toByteArray() );
        docIn.put( MdekKeys.REQUESTINFO_IMPORT_FRONTEND_PROTOCOL, "csw202" );
        docIn.putBoolean( MdekKeys.REQUESTINFO_IMPORT_START_NEW_ANALYSIS, true );
        IngridDocument analyzeImportData = catJob.analyzeImportData( docIn );

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
    public void handleDocumentUpdate() {
        IngridDocument result = plug.cswTransaction( null );

        assertThat( result, is( not( nullValue() ) ) );
        assertThat( result.getBoolean( "success" ), is( true ) );

        fail( "Not yet implemented" );
    }

    @Test
    public void handleDocumentDelete() {
        IngridDocument result = plug.cswTransaction( null );

        assertThat( result, is( not( nullValue() ) ) );
        assertThat( result.getBoolean( "success" ), is( true ) );

        fail( "Not yet implemented" );
    }

    private void checkXmlResponse(String xml, int inserted, int updated, int deleted) {
        assertThat( xml, containsString( "<csw:totalInserted>" + String.valueOf( inserted ) + "</csw:totalInserted>" ) );
        assertThat( xml, containsString( "<csw:totalUpdated>" + String.valueOf( updated ) + "</csw:totalUpdated>" ) );
        assertThat( xml, containsString( "<csw:totalDeleted>" + String.valueOf( deleted ) + "</csw:totalDeleted>" ) );
    }
}
