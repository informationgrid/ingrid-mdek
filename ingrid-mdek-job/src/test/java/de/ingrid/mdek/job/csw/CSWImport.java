package de.ingrid.mdek.job.csw;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.springframework.core.io.ClassPathResource;

import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.job.IgeSearchPlug;
import de.ingrid.mdek.job.MdekIdcObjectJob;
import de.ingrid.utils.IngridDocument;

@RunWith(MockitoJUnitRunner.class)
public class CSWImport {

    @InjectMocks
    RelDatabaseCSWPersister databasePersister;

    private IgeSearchPlug plug;

    @Mock
    MdekIdcObjectJob objectJob;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {}

    @Before
    public void before() throws IOException {
        CswTransaction trans = new CswTransaction();
        trans.setPersist( databasePersister );

        plug = new IgeSearchPlug( null, null, null, null, null );
        plug.setCswTransaction( trans );
    }

    @Test
    public void handleNullValue() {
        IngridDocument result = plug.cswTransaction( null );

        assertThat( result, is( not( nullValue() ) ) );
        assertThat( result.getBoolean( "success" ), is( true ) );
    }

    @Test
    public void handleDocumentInsert() throws IOException {

        Mockito.when( objectJob.storeObject( (IngridDocument) Mockito.any() ) ).then( new Answer<IngridDocument>() {

            @Override
            public IngridDocument answer(InvocationOnMock invocation) throws Throwable {
                IngridDocument doc = invocation.getArgumentAt( 0, IngridDocument.class );
                assertThat( doc.getString( MdekKeys.ID ), is( "993E6356-D262-43AD-A69D-FE8EF62189A4" ) );
                return null;
            }
        } );

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
