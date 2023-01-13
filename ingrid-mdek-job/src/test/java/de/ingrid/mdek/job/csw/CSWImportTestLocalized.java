/*
 * **************************************************-
 * InGrid mdek-job
 * ==================================================
 * Copyright (C) 2014 - 2023 wemove digital solutions GmbH
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

import de.ingrid.iplug.dsc.utils.DatabaseConnectionUtils;
import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.job.MdekIdcCatalogJob;
import de.ingrid.mdek.job.protocol.ProtocolHandler;
import de.ingrid.mdek.job.protocol.ProtocolHandler.Type;
import de.ingrid.mdek.job.utils.TestSetup;
import de.ingrid.mdek.services.catalog.MdekObjectService;
import de.ingrid.mdek.services.utils.MdekJobHandler;
import de.ingrid.mdek.xml.importer.IngridXMLStreamReader;
import de.ingrid.utils.IngridDocument;
import de.ingrid.utils.xml.XMLUtils;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.springframework.core.io.ClassPathResource;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith(MockitoExtension.class)
public class CSWImportTestLocalized extends TestSetup {

    //private IgeSearchPlug plug;

    // @Mock private ResultSet resultSet;

    @BeforeEach
    public void before() throws Exception {
        String[] mappingScripts = new String[] {
                "ingrid-mdek-job/src/main/resources/import/mapper/csw202_to_ingrid_igc.js"
        };

        beforeSetup( mappingScripts );
    }

    @AfterEach
    public void after() {
        mockedDatabaseConnectionUtils.close();
    }

    @Test
    public void importLocalizedISO() throws Exception {

        doAnswer( new Answer<Void>() {
            @SuppressWarnings({ "unchecked", "rawtypes" })
            public Void answer(InvocationOnMock invocation) throws Exception {
                Map doc = invocation.getArgument( 1 );
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
                    System.out.println( XMLUtils.toString( domForObject.get( 0 ) ) );
                    assertThat( docOut.getString( MdekKeys.TITLE ), is( "Bebauungsplan Barmbek-Nord 18 (1.Änderung) Hamburg#locale-eng:English translation of Bebauungsplan Barmbek-Nord 18 (1.Änderung) Hamburg" ) );
                    assertThat( docOut.getString( MdekKeys.DATASET_ALTERNATE_NAME ), is( "BN 18 Ä Textänderungsverfahren#locale-rus:Это тест: BN 18 Ä Textänderungsverfahren#locale-eng:English for BN 18 Ä Textänderungsverfahren" ) );
                    assertThat( docOut.getString( MdekKeys.ABSTRACT ), is( "Dieser Datensatz stellt die geografischen Bezeichnungen von Hamburg im INSPIRE-Zielmodell dar.#locale-rus:Это тест: Dieser Datensatz stellt die geografischen Bezeichnungen von Hamburg im INSPIRE-Zielmodell dar.#locale-eng:English for Dieser Datensatz stellt die geografischen Bezeichnungen von Hamburg im INSPIRE-Zielmodell dar." ) );
                } catch (NumberFormatException ex) {
                    // expected
                } catch (Exception ex) {
                    throw new AssertionError( "An unexpected exception occurred: " + ex.getMessage() );
                }
                return null;
            }
        } ).when( jobHandler ).updateJobInfoDB( any(), any(), anyString() );

        IngridDocument docIn = prepareInsertDocument( "csw/dataset_localized.xml" );
        IngridDocument analyzeImportData = catJob.analyzeImportData( docIn );
        assertThat( analyzeImportData.get( "error" ), is( nullValue() ) );
        ProtocolHandler protocol = (ProtocolHandler) analyzeImportData.get( "protocol" );
        assertThat( protocol.getProtocol( Type.ERROR ).size(), is( 0 ) );
        assertThat( protocol.getProtocol( Type.WARN ).size(), is( 0 ) );
        assertThat( protocol.getProtocol( Type.INFO ).size(), is( not( 0 ) ) );

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

        docIn.put( MdekKeys.REQUESTINFO_IMPORT_OBJ_PARENT_UUID, "2768376B-EE24-4F34-969B-084C55B52278" );  // IMPORTKNOTEN
        docIn.put( MdekKeys.REQUESTINFO_IMPORT_ADDR_PARENT_UUID, "BD33BC8E-519E-47F9-8A30-465C95CD0355" ); // IMPORTKNOTEN
        return docIn;
    }


}
