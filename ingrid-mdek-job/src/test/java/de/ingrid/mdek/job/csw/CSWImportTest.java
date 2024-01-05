/*
 * **************************************************-
 * InGrid mdek-job
 * ==================================================
 * Copyright (C) 2014 - 2024 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.2 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
package de.ingrid.mdek.job.csw;

import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.job.IJob.JobType;
import de.ingrid.mdek.job.MdekIdcCatalogJob;
import de.ingrid.mdek.job.protocol.ProtocolHandler;
import de.ingrid.mdek.job.protocol.ProtocolHandler.Type;
import de.ingrid.mdek.job.utils.TestSetup;
import de.ingrid.mdek.xml.importer.IngridXMLStreamReader;
import de.ingrid.utils.IngridDocument;
import de.ingrid.utils.xml.XMLUtils;
import de.ingrid.utils.xpath.XPathUtils;
import org.apache.commons.io.FileUtils;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
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
import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CSWImportTest extends TestSetup {
    String[] mappingScripts = new String[]{
            "ingrid-mdek-job/src/main/resources/import/mapper/csw202_to_ingrid_igc.js"
    };

    @BeforeEach
    public void before() throws Exception {

        beforeSetup(mappingScripts);
    }

    @AfterEach
    public void after() {
        mockedDatabaseConnectionUtils.close();
    }


    @Test
    public void handleNullValue() {
        IngridDocument result = plug.cswTransaction(null);

        MatcherAssert.assertThat(result, is(not(nullValue())));
        MatcherAssert.assertThat(result.getBoolean("success"), is(false));
    }

    @Test
    public void analyzeCswDocumentInsert_nonGeographicDataset() throws Exception {

        doAnswer((Answer<Void>) invocation -> {
            Map doc = invocation.getArgument(1);
            List<byte[]> data = (List<byte[]>) doc.get(MdekKeys.REQUESTINFO_IMPORT_ANALYZED_DATA);
            MatcherAssert.assertThat(data, is(not(nullValue())));
            MatcherAssert.assertThat(data.size(), is(1));
            InputStream in = new GZIPInputStream(new ByteArrayInputStream(data.get(0)));
            IngridXMLStreamReader reader = new IngridXMLStreamReader(in, importerCallback, "TEST_USER_ID");
            MatcherAssert.assertThat(reader.getObjectUuids().size(), is(1));
            MatcherAssert.assertThat(reader.getObjectUuids().iterator().next(), is("993E6356-D262-43AD-A69D-FE8EF62189A4"));
            List<Document> domForObject = reader.getDomForObject("993E6356-D262-43AD-A69D-FE8EF62189A4");
            try {
                importMapper.mapDataSource(domForObject.get(0));
                throw new AssertionError("An exception should have occurred, because 'nonGeographicDataset' is not supported.");
            } catch (NumberFormatException ex) {
                // expected
            } catch (Exception ex) {
                throw new AssertionError("An unexpected exception occurred: " + ex.getMessage());
            }
            return null;
        }).when(jobHandler).updateJobInfoDB(ArgumentMatchers.any(), any(), anyString());

        IngridDocument docIn = prepareInsertDocument("csw/insert_nonGeographicDataset.xml");
        IngridDocument analyzeImportData = catJob.analyzeImportData(docIn);
        MatcherAssert.assertThat(analyzeImportData.get("error"), is(nullValue()));
        ProtocolHandler protocol = (ProtocolHandler) analyzeImportData.get("protocol");
        MatcherAssert.assertThat(protocol.getProtocol(Type.ERROR).size(), is(0));
        MatcherAssert.assertThat(protocol.getProtocol(Type.WARN).size(), is(1));
        MatcherAssert.assertThat(protocol.getProtocol(Type.INFO).size(), is(not(0)));

    }

    @Test
    public void analyzeCswDocumentInsert_3_service() throws Exception {

        doAnswer((Answer<Void>) invocation -> {
            Map doc = invocation.getArgument(1);
            List<byte[]> data = (List<byte[]>) doc.get(MdekKeys.REQUESTINFO_IMPORT_ANALYZED_DATA);
            MatcherAssert.assertThat(data, is(not(nullValue())));
            MatcherAssert.assertThat(data.size(), is(1));
            InputStream in = new GZIPInputStream(new ByteArrayInputStream(data.get(0)));
            IngridXMLStreamReader reader = new IngridXMLStreamReader(in, importerCallback, "TEST_USER_ID");
            MatcherAssert.assertThat(reader.getObjectUuids().size(), is(1));
            MatcherAssert.assertThat(reader.getObjectUuids().iterator().next(), is("D9EE3448-8224-4B08-926B-B9E5EDE360FC"));
            List<Document> domForObject = reader.getDomForObject("D9EE3448-8224-4B08-926B-B9E5EDE360FC");
            try {
                IngridDocument docOut = importMapper.mapDataSource(domForObject.get(0));
                System.out.println(XMLUtils.toString(domForObject.get(0)));
                // JSONObject jsonObject = new JSONObject();
                // jsonObject.putAll( docOut );
                MatcherAssert.assertThat(docOut.getString(MdekKeys.TITLE), is("Coupling Service"));
                MatcherAssert.assertThat(docOut.getInt(MdekKeys.CLASS), is(3));
                // check responsible
                MatcherAssert.assertThat(docOut.getString(MdekKeys.ORIGINAL_CONTROL_IDENTIFIER), is("D9EE3448-8224-4B08-926B-B9E5EDE360FC"));

                // check short description
                MatcherAssert.assertThat(docOut.getString(MdekKeys.DATASET_ALTERNATE_NAME), is("Eine kurze Beschreibung"));

                // check preview image
                List<IngridDocument> links = (List<IngridDocument>) docOut.get(MdekKeys.LINKAGES);
                boolean found = false;
                for (IngridDocument link : links) {
                    if (link.getInt(MdekKeys.LINKAGE_REFERENCE_ID) == 9000 && "http://some.pic.com".equals(link.getString(MdekKeys.LINKAGE_URL))) {
                        found = true;
                        break;
                    }
                }
                MatcherAssert.assertThat("Preview image was not found.", found, is(true));

                // check abstract
                MatcherAssert.assertThat(docOut.getString(MdekKeys.ABSTRACT), is("Dienst für den Test um externe gekoppelte Datensätze hinzuzufügen"));

                // check address
                List<IngridDocument> addresses = (List<IngridDocument>) docOut.get(MdekKeys.ADR_REFERENCES_TO);
                // TODO: dataset gets a new UUID but keeps its origUUID!!!
                MatcherAssert.assertThat(addresses.size(), is(3));
                MatcherAssert.assertThat(addresses.get(0).getString(MdekKeys.UUID), is("3E1B7F21-4E56-11D3-9A6B-0060971A0BF7"));
                MatcherAssert.assertThat(addresses.get(0).getInt(MdekKeys.RELATION_TYPE_ID), is(1));
                MatcherAssert.assertThat(addresses.get(0).getInt(MdekKeys.RELATION_TYPE_REF), is(505));
                MatcherAssert.assertThat(addresses.get(1).getInt(MdekKeys.RELATION_TYPE_ID), is(12));
                MatcherAssert.assertThat(addresses.get(1).getInt(MdekKeys.RELATION_TYPE_REF), is(505));

                // inspire relevant
                MatcherAssert.assertThat(docOut.getString(MdekKeys.IS_INSPIRE_RELEVANT), is("Y"));

                // open data
                MatcherAssert.assertThat(docOut.getString(MdekKeys.IS_OPEN_DATA), is("Y"));

                // INSPIRE-topics
                MatcherAssert.assertThat(docOut.getArrayList(MdekKeys.SUBJECT_TERMS_INSPIRE).size(), is(2));
                assertSubjectTerms(docOut.getArrayList(MdekKeys.SUBJECT_TERMS_INSPIRE), "Biogeografische Regionen", "Gebäude");

                // optional topics
                MatcherAssert.assertThat(docOut.getArrayList(MdekKeys.SUBJECT_TERMS).size(), is(3));
                assertSubjectTerms(docOut.getArrayList(MdekKeys.SUBJECT_TERMS), "Adaptronik", "Kabal", "Erdsystem");

                // environment topics
                // NOT mapped in ISO!?
                // TODO: assertThat( docOut.getString( MdekKeys.IS_CATALOG_DATA ), is( "Y" ) );
                MatcherAssert.assertThat(docOut.getArrayList(MdekKeys.ENV_TOPICS).size(), is(1));
                MatcherAssert.assertThat(docOut.getArrayList(MdekKeys.ENV_TOPICS).get(0), is(6));

                IngridDocument serviceMap = (IngridDocument) docOut.get(MdekKeys.TECHNICAL_DOMAIN_SERVICE);
                // check classification of service: Dauerauftragsdienst (211)
                MatcherAssert.assertThat(serviceMap.getArrayList(MdekKeys.SERVICE_TYPE2_LIST).size(), is(1));
                MatcherAssert.assertThat(((IngridDocument) serviceMap.getArrayList(MdekKeys.SERVICE_TYPE2_LIST).get(0)).getInt(MdekKeys.SERVICE_TYPE2_KEY), is(211));

                // service version
                MatcherAssert.assertThat(((IngridDocument) serviceMap.getArrayList(MdekKeys.SERVICE_VERSION_LIST).get(0)).getString(MdekKeys.SERVICE_VERSION_VALUE), is("OGC:WFS 2.0"));

                // check type of service: Downloaddienst === 3?
                MatcherAssert.assertThat(serviceMap.getInt(MdekKeys.SERVICE_TYPE_KEY), is(3));

                // check is atom feed???
                // NOT mapped in ISO!?
                // TODO: assertThat( serviceMap.getString( MdekKeys.HAS_ATOM_DOWNLOAD ), is("Y"));

                // operations
                MatcherAssert.assertThat(serviceMap.getArrayList(MdekKeys.SERVICE_OPERATION_LIST).size(), is(1));
                assertOperation(serviceMap.getArrayList(MdekKeys.SERVICE_OPERATION_LIST).get(0), "GetCapabilities", "http://some.cap.com", "WebServices", "GetCap Beschreibung",
                        "http://some.cap.com/hello?count=10");

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
                MatcherAssert.assertThat(serviceMap.getString(MdekKeys.COUPLING_TYPE), is("tight"));

                // access constraint
                // NOT mapped in ISO!?
                // TODO: assertThat( serviceMap.getString( MdekKeys.HAS_ACCESS_CONSTRAINT ), is( "Y" ) );

                // check spatial ref: EPSG 3068: DHDN / Soldner Berlin
                MatcherAssert.assertThat(docOut.getArrayList(MdekKeys.SPATIAL_SYSTEM_LIST).size(), is(1));
                MatcherAssert.assertThat(((IngridDocument) docOut.getArrayList(MdekKeys.SPATIAL_SYSTEM_LIST).get(0)).getString(MdekKeys.COORDINATE_SYSTEM), is("EPSG 3068: DHDN / Soldner Berlin"));

                // height
                MatcherAssert.assertThat(docOut.get(MdekKeys.VERTICAL_EXTENT_MINIMUM), is(4.0));
                MatcherAssert.assertThat(docOut.get(MdekKeys.VERTICAL_EXTENT_MAXIMUM), is(6.0));
                MatcherAssert.assertThat(docOut.getInt(MdekKeys.VERTICAL_EXTENT_UNIT), is(9001)); // "Meter"
                MatcherAssert.assertThat(docOut.getString(MdekKeys.VERTICAL_EXTENT_VDATUM_VALUE), is("DE_DHHN92_NH"));

                // height explanation
                MatcherAssert.assertThat(docOut.getString(MdekKeys.DESCRIPTION_OF_SPATIAL_DOMAIN), is("nicht sehr hoch"));

                // check creation: 17.03.2015
                MatcherAssert.assertThat(((IngridDocument) docOut.getArrayList(MdekKeys.DATASET_REFERENCES).get(0)).getString(MdekKeys.DATASET_REFERENCE_DATE), is("20150317000000000"));

                // time explanation
                MatcherAssert.assertThat(docOut.getString(MdekKeys.DESCRIPTION_OF_TEMPORAL_DOMAIN), is("nicht sehr alt"));

                // time range
                MatcherAssert.assertThat(docOut.getString(MdekKeys.BEGINNING_DATE), is("20160209000000000"));
                MatcherAssert.assertThat(docOut.getString(MdekKeys.ENDING_DATE), is("20160209000000000"));

                // periodity
                MatcherAssert.assertThat(docOut.getInt(MdekKeys.TIME_PERIOD), is(1));

                // state
                MatcherAssert.assertThat(docOut.getInt(MdekKeys.TIME_STATUS), is(5));

                // interval
                // NOT mapped in ISO!?
                // TODO: assertThat( docOut.get( MdekKeys.TIME_INTERSECT), is( ) );

                // check metadata language: Deutsch
                MatcherAssert.assertThat(docOut.getString(MdekKeys.METADATA_LANGUAGE_NAME), is("Deutsch"));

                // character set (utf8)
                MatcherAssert.assertThat(docOut.getInt(MdekKeys.METADATA_CHARACTER_SET), is(4));

                // check publication info: Internet
                MatcherAssert.assertThat(docOut.getInt(MdekKeys.PUBLICATION_CONDITION), is(1));

                // check conformity: Technical Guidance for the implementation of INSPIRE Download Services => konform
                MatcherAssert.assertThat(docOut.getArrayList(MdekKeys.CONFORMITY_LIST).size(), is(1));
                assertConformity((IngridDocument) docOut.getArrayList(MdekKeys.CONFORMITY_LIST).get(0), "Technical Guidance for the implementation of INSPIRE Download Services", "konform");

                // xml export criteria
                // NOT mapped in ISO!?
                // TODO: assertThat( docOut.getArrayList( MdekKeys.EXPORT_CRITERIA).size(), is( 1 ) );
                // TODO: assertThat( ((IngridDocument)docOut.getArrayList( MdekKeys.EXPORT_CRITERIA).get(0)).get(
                // MdekKeys.EXPORT_CRITERION_VALUE ), is( "CDS" ) );

                // law basics
                MatcherAssert.assertThat(docOut.getArrayList(MdekKeys.LEGISLATIONS).size(), is(1));
                MatcherAssert.assertThat(((IngridDocument) docOut.getArrayList(MdekKeys.LEGISLATIONS).get(0)).getInt(MdekKeys.LEGISLATION_KEY), is(24));
                MatcherAssert.assertThat(((IngridDocument) docOut.getArrayList(MdekKeys.LEGISLATIONS).get(0)).getString(MdekKeys.LEGISLATION_VALUE), is("Bundeswasserstraßengesetz (WaStrG)"));

                // purpose
                MatcherAssert.assertThat(docOut.getString(MdekKeys.DATASET_INTENTIONS), is("kein Zweck"));

                // usage
                MatcherAssert.assertThat(docOut.getString(MdekKeys.DATASET_USAGE), is("keine Nutzung"));

                // check access constraints: Bedingungen unbekannt
                // assertThat( docOut.getArrayList( MdekKeys.USE_LIST ).size(), is( 1 ) );
                MatcherAssert.assertThat(((IngridDocument) docOut.getArrayList(MdekKeys.USE_LIST).get(0)).getString(MdekKeys.USE_TERMS_OF_USE_VALUE), is("Es gelten keine Bedingungen"));

                // check usage constraints:
                MatcherAssert.assertThat(((IngridDocument) docOut.getArrayList(MdekKeys.USE_CONSTRAINTS).get(0)).getString(MdekKeys.USE_LICENSE_VALUE), is("eingeschränkte Geolizenz"));

                // check usage condition: Es gelten keine Bedingungen
                MatcherAssert.assertThat(docOut.getArrayList(MdekKeys.ACCESS_LIST).size(), is(1));
                MatcherAssert.assertThat(((IngridDocument) docOut.getArrayList(MdekKeys.ACCESS_LIST).get(0)).getString(MdekKeys.ACCESS_RESTRICTION_VALUE), is("Es gelten keine Bedingungen"));

                // data format
                MatcherAssert.assertThat(docOut.getArrayList(MdekKeys.DATA_FORMATS).size(), is(1));
                MatcherAssert.assertThat(((IngridDocument) docOut.getArrayList(MdekKeys.DATA_FORMATS).get(0)).getString(MdekKeys.FORMAT_NAME), is("Excel"));
                MatcherAssert.assertThat(((IngridDocument) docOut.getArrayList(MdekKeys.DATA_FORMATS).get(0)).getInt(MdekKeys.FORMAT_NAME_KEY), is(3));
                MatcherAssert.assertThat(((IngridDocument) docOut.getArrayList(MdekKeys.DATA_FORMATS).get(0)).getString(MdekKeys.FORMAT_VERSION), is("2"));
                MatcherAssert.assertThat(((IngridDocument) docOut.getArrayList(MdekKeys.DATA_FORMATS).get(0)).getString(MdekKeys.FORMAT_FILE_DECOMPRESSION_TECHNIQUE), is("zip"));
                MatcherAssert.assertThat(((IngridDocument) docOut.getArrayList(MdekKeys.DATA_FORMATS).get(0)).getString(MdekKeys.FORMAT_SPECIFICATION), is("5"));

                // media
                MatcherAssert.assertThat(docOut.getArrayList(MdekKeys.MEDIUM_OPTIONS).size(), is(1));
                assertMedia((IngridDocument) docOut.getArrayList(MdekKeys.MEDIUM_OPTIONS).get(0), 1, 700.0, "c:/");

                // order info
                MatcherAssert.assertThat(docOut.getString(MdekKeys.ORDERING_INSTRUCTIONS), is("keine Bestellung"));

                // links to

                // links from

                // spatial ref
                // docOut.get( MdekKeys.SPATIAL_REPRESENTATION_TYPE_LIST )

                // free spatial ref
                List<Object> locs = docOut.getArrayList(MdekKeys.LOCATIONS);
                // assertThat( locs.size(), is( 1 ));
                assertLink(links.get(2), "Datensatz mit zwei Download Links",
                        "http://192.168.0.247/interface-csw?REQUEST=GetRecordById&SERVICE=CSW&VERSION=2.0.2&id=93BBCF92-BD74-47A2-9865-BE59ABC90C57&iplug=/ingrid-group:iplug-csw-dsc-test&elementSetName=full",
                        "http://portalu.de/igc_testNS#/b6fb5dab-036d-4c43-82da-98ffa2e9df76#**#93BBCF92-BD74-47A2-9865-BE59ABC90C57");

                // Bounding Boxes are not bound to a name in ISO, so we cannot correctly map it to our structure
                assertLocation(locs.get(0), "Hannover (03241001)", null, null, null, null);
                assertLocation(locs.get(1), "Raumbezug des Datensatzes", 9.603732109069824, 9.919820785522461, 52.30428695678711, 52.454345703125);

            } catch (Exception ex) {
                throw new AssertionError("An unexpected exception occurred: " + ex.getMessage());
            }
            return null;
        }).when(jobHandler).updateJobInfoDB(any(), any(), anyString());

        IngridDocument docIn = prepareInsertDocument("csw/insert_class3_service.xml");
        IngridDocument analyzeImportData = catJob.analyzeImportData(docIn);

        //Mockito.verify( catJob, Mockito.times( 1 ) ).analyzeImportData( (IngridDocument) any() );

        MatcherAssert.assertThat(analyzeImportData.get("error"), is(nullValue()));
        ProtocolHandler protocol = (ProtocolHandler) analyzeImportData.get("protocol");
        MatcherAssert.assertThat(protocol.getProtocol(Type.ERROR).size(), is(0));
        MatcherAssert.assertThat(protocol.getProtocol(Type.WARN).size(), is(2));
        MatcherAssert.assertThat(protocol.getProtocol(Type.INFO).size(), is(not(0)));
    }

    @Test
    public void analyzeCSWDocumentInsert_1_dataset() throws Exception {
        doAnswer((Answer<Void>) invocation -> {
            Map doc = invocation.getArgument(1);
            List<byte[]> data = (List<byte[]>) doc.get(MdekKeys.REQUESTINFO_IMPORT_ANALYZED_DATA);
            MatcherAssert.assertThat(data, is(not(nullValue())));
            MatcherAssert.assertThat(data.size(), is(1));
            InputStream in = new GZIPInputStream(new ByteArrayInputStream(data.get(0)));
            IngridXMLStreamReader reader = new IngridXMLStreamReader(in, importerCallback, "TEST_USER_ID");
            MatcherAssert.assertThat(reader.getObjectUuids().size(), is(1));
            MatcherAssert.assertThat(reader.getObjectUuids().iterator().next(), is("4915275a-733a-47cd-b1a6-1a3f1e976948"));
            List<Document> domForObject = reader.getDomForObject("4915275a-733a-47cd-b1a6-1a3f1e976948");
            try {
                IngridDocument docOut = importMapper.mapDataSource(domForObject.get(0));
                // System.out.println( XMLUtils.toString( domForObject.get( 0 ) ) );

                MatcherAssert.assertThat(docOut.getString(MdekKeys.TITLE), is("Bebauungsplan Barmbek-Nord 18 (1.Änderung) Hamburg"));
                MatcherAssert.assertThat(docOut.getInt(MdekKeys.CLASS), is(1));
                // check responsible
                MatcherAssert.assertThat(docOut.getString(MdekKeys.ORIGINAL_CONTROL_IDENTIFIER), is("4915275a-733a-47cd-b1a6-1a3f1e976948"));

                // check short description
                MatcherAssert.assertThat(docOut.getString(MdekKeys.DATASET_ALTERNATE_NAME), is("BN 18 Ä Textänderungsverfahren"));

                // check abstract
                MatcherAssert.assertThat(docOut.getString(MdekKeys.ABSTRACT), is("siehe Originalplan"));

                // inspire relevant
                MatcherAssert.assertThat(docOut.getString(MdekKeys.IS_INSPIRE_RELEVANT), is(nullValue()));

                // open data
                MatcherAssert.assertThat(docOut.getString(MdekKeys.IS_OPEN_DATA), is(nullValue()));

                // INSPIRE-topics
                MatcherAssert.assertThat(docOut.getArrayList(MdekKeys.SUBJECT_TERMS_INSPIRE).size(), is(1));
                assertSubjectTerms(docOut.getArrayList(MdekKeys.SUBJECT_TERMS_INSPIRE), "Land use");

                // optional topics
                MatcherAssert.assertThat(docOut.getArrayList(MdekKeys.SUBJECT_TERMS).size(), is(4));
                assertSubjectTerms(docOut.getArrayList(MdekKeys.SUBJECT_TERMS), "Raumbezogene Information", "Bauleitplanung", "Bebauungsplan", "Geoinformation");

                // check access constraints: Bedingungen unbekannt
                // assertThat( docOut.getArrayList( MdekKeys.USE_LIST ).size(), is( 1 ) );
                MatcherAssert.assertThat(((IngridDocument) docOut.getArrayList(MdekKeys.USE_LIST).get(0)).getString(MdekKeys.USE_TERMS_OF_USE_VALUE), is("Datenlizenz Deutschland - Namensnennung - Version 2.0; <a href=\"https://www.govdata.de/dl-de/by-2-0\">https://www.govdata.de/dl-de/by-2-0</a>; dl-de-by-2.0; Namensnennung: \"Freie und Hansestadt Hamburg, Bezirksamt Nord, Fachamt Stadt- und Landschaftsplanung\""));

                // check usage constraints:
                MatcherAssert.assertThat(docOut.getArrayList(MdekKeys.USE_CONSTRAINTS).size(), is(0));
                //assertThat( ((IngridDocument) docOut.getArrayList( MdekKeys.USE_CONSTRAINTS ).get( 1 )).getString( MdekKeys.USE_LICENSE_VALUE ), is( "eingeschränkte Geolizenz" ) );

                // check usage condition: Es gelten keine Bedingungen
                MatcherAssert.assertThat(docOut.getArrayList(MdekKeys.ACCESS_LIST).size(), is(3));
                MatcherAssert.assertThat(((IngridDocument) docOut.getArrayList(MdekKeys.ACCESS_LIST).get(0)).getString(MdekKeys.ACCESS_RESTRICTION_VALUE), is("keine"));
                MatcherAssert.assertThat(((IngridDocument) docOut.getArrayList(MdekKeys.ACCESS_LIST).get(1)).getString(MdekKeys.ACCESS_RESTRICTION_VALUE), is("Baugesetzbuch (BauGB)"));
                MatcherAssert.assertThat(((IngridDocument) docOut.getArrayList(MdekKeys.ACCESS_LIST).get(2)).getString(MdekKeys.ACCESS_RESTRICTION_VALUE), is("Baunutzungsverordnung (BauNVO)"));

                // environment topics
                MatcherAssert.assertThat(docOut.getArrayList(MdekKeys.ENV_TOPICS).size(), is(0));

                // ISO topics
                MatcherAssert.assertThat(docOut.getArrayList(MdekKeys.TOPIC_CATEGORIES).size(), is(1));
                MatcherAssert.assertThat(docOut.getArrayList(MdekKeys.TOPIC_CATEGORIES).get(0), is(15));

                // check metadata language: Deutsch
                MatcherAssert.assertThat(docOut.getString(MdekKeys.METADATA_LANGUAGE_NAME), is("Deutsch"));

                // character set (utf8)
                MatcherAssert.assertThat(docOut.get(MdekKeys.METADATA_CHARACTER_SET), is(nullValue()));

                // check spatial ref: EPSG 3068: DHDN / Soldner Berlin
                MatcherAssert.assertThat(docOut.getArrayList(MdekKeys.SPATIAL_SYSTEM_LIST).size(), is(1));
                MatcherAssert.assertThat(((IngridDocument) docOut.getArrayList(MdekKeys.SPATIAL_SYSTEM_LIST).get(0)).getString(MdekKeys.COORDINATE_SYSTEM), is("EPSG 25832: ETRS89 / UTM Zone 32N"));

                // free spatial ref
                List<Object> locs = docOut.getArrayList(MdekKeys.LOCATIONS);
                MatcherAssert.assertThat(locs.size(), is(1));

                // see (#2097), assume relationship since object has exactly 2 gmd:geographicElement AND
                //their order is  1. gmd:EX_GeographicDescription 2. gmd:EX_GeographicBoundingBox
                assertLocation(locs.get(0), "Hamburg", 8.420551, 10.326304, 53.394985, 53.964153);

                // time range
                MatcherAssert.assertThat(docOut.getString(MdekKeys.BEGINNING_DATE), is("20160301000000000"));
                MatcherAssert.assertThat(docOut.getString(MdekKeys.ENDING_DATE), is(nullValue()));

                // data formats
                MatcherAssert.assertThat(docOut.getArrayList(MdekKeys.DATA_FORMATS).size(), is(3));
                assertDataFormat((IngridDocument) docOut.getArrayList(MdekKeys.DATA_FORMATS).get(0), "Geographic Markup Language (GML)", 99, null, null, null);
                assertDataFormat((IngridDocument) docOut.getArrayList(MdekKeys.DATA_FORMATS).get(1), "XPlanGML", 98, "4.1", null, null);
                assertDataFormat((IngridDocument) docOut.getArrayList(MdekKeys.DATA_FORMATS).get(2), "XPlanGML", 98, "3.0", null, null);

                // media
                MatcherAssert.assertThat(docOut.getArrayList(MdekKeys.MEDIUM_OPTIONS).size(), is(0));
                //assertMedia( (IngridDocument) docOut.getArrayList( MdekKeys.MEDIUM_OPTIONS ).get( 0 ), 1, 700.0, "c:/" );

                List<IngridDocument> links = (List<IngridDocument>) docOut.get(MdekKeys.LINKAGES);
                MatcherAssert.assertThat(links.size(), is(4));
                assertLink(links.get(0), "Bekanntmachung im HmbGVBl als PDF Datei",
                        "http://daten-hamburg.de/infrastruktur_bauen_wohnen/bebauungsplaene/pdfs/bplan/Barmbek-Nord18(1Aend).pdf",
                        null);
                assertLink(links.get(1), "URL zu weiteren Informationen über den Datensatz",
                        "http://www.hamburg.de/bebauungsplaene-online.de",
                        null);
                assertLink(links.get(2), "Begründung des Bebauungsplans als PDF Datei",
                        "http://daten-hamburg.de/infrastruktur_bauen_wohnen/bebauungsplaene/pdfs/bplan_begr/Barmbek-Nord18(1Aend).pdf",
                        null);
                assertLink(links.get(3), "Festsetzungen (Planzeichnung / Verordnung) als PDF Datei",
                        "http://daten-hamburg.de/infrastruktur_bauen_wohnen/bebauungsplaene/pdfs/bplan/Barmbek-Nord18(1Aend).pdf",
                        null);

                // check conformity: Technical Guidance for the implementation of INSPIRE Download Services => konform
                MatcherAssert.assertThat(docOut.getArrayList(MdekKeys.CONFORMITY_LIST).size(), is(1));
                assertConformity((IngridDocument) docOut.getArrayList(MdekKeys.CONFORMITY_LIST).get(0), "INSPIRE Richtlinie", "nicht konform");

                IngridDocument techDomain = (IngridDocument) docOut.get(MdekKeys.TECHNICAL_DOMAIN_MAP);
                MatcherAssert.assertThat(techDomain.getString(MdekKeys.TECHNICAL_BASE), is("vergl. eGovernment Vorhaben \"PLIS\""));
                MatcherAssert.assertThat(techDomain.getString(MdekKeys.METHOD_OF_PRODUCTION), is("Die in den Planwerken der verbindlichen Bauleitplanung dokumentierten Festsetzungen, Kennzeichnungen und Hinweise werden auf der Grundlage der aktuellen Örtlichkeit der Liegenschaftskarte (ALKIS) mit Hilfe von Fachapplikationen (AutoCAD + WS LANDCAD bzw. ArcGIS + GeoOffice) digitalisiert."));

                // check address
                List<IngridDocument> addresses = (List<IngridDocument>) docOut.get(MdekKeys.ADR_REFERENCES_TO);
                // TODO: dataset gets a new UUID but keeps its origUUID!!!
                MatcherAssert.assertThat(addresses.size(), is(2));
                MatcherAssert.assertThat(addresses.get(1).getInt(MdekKeys.RELATION_TYPE_ID), is(12));
                MatcherAssert.assertThat(addresses.get(1).getInt(MdekKeys.RELATION_TYPE_REF), is(505));
                MatcherAssert.assertThat(addresses.get(1).getString(MdekKeys.UUID), is("110C6012-1713-44C0-9A33-4E2C24D06966"));
                MatcherAssert.assertThat(addresses.get(0).getInt(MdekKeys.RELATION_TYPE_ID), is(7));
                MatcherAssert.assertThat(addresses.get(0).getInt(MdekKeys.RELATION_TYPE_REF), is(505));
                MatcherAssert.assertThat(addresses.get(0).getString(MdekKeys.UUID), is("DA64401A-2AFC-458D-A8AF-58D0A3C35AA9"));

            } catch (Exception ex) {
                throw new AssertionError("An unexpected exception occurred: " + ex.getMessage());
            }
            return null;
        }).when(jobHandler).updateJobInfoDB(any(), any(), anyString());

        IngridDocument docIn = prepareInsertDocument("csw/insert_class1_dataset.xml");
        IngridDocument analyzeImportData = catJob.analyzeImportData(docIn);

        //Mockito.verify( catJob, Mockito.times( 1 ) ).analyzeImportData( (IngridDocument) any() );

        MatcherAssert.assertThat(analyzeImportData.get("error"), is(nullValue()));
        ProtocolHandler protocol = (ProtocolHandler) analyzeImportData.get("protocol");
        MatcherAssert.assertThat(protocol.getProtocol(Type.ERROR).size(), is(0));
        MatcherAssert.assertThat(protocol.getProtocol(Type.WARN).size(), is(3));
        MatcherAssert.assertThat(protocol.getProtocol(Type.INFO).size(), is(not(0)));
    }

    private void assertConformity(IngridDocument doc, String specValue, String degree) {
        MatcherAssert.assertThat(doc.getString(MdekKeys.CONFORMITY_SPECIFICATION_VALUE), is(specValue));
        MatcherAssert.assertThat(doc.getString(MdekKeys.CONFORMITY_DEGREE_VALUE), is(degree));
    }

    private void assertMedia(IngridDocument doc, int nameId, double transferSize, String note) {
        MatcherAssert.assertThat(doc.getInt(MdekKeys.MEDIUM_NAME), is(nameId));
        MatcherAssert.assertThat(doc.get(MdekKeys.MEDIUM_TRANSFER_SIZE), is(transferSize));
        MatcherAssert.assertThat(doc.getString(MdekKeys.MEDIUM_NOTE), is(note));

    }

    private void assertDataFormat(IngridDocument dataFormat, String name, int nameKey, Object version, Object specification, Object decompress) {
        MatcherAssert.assertThat(dataFormat.getString(MdekKeys.FORMAT_NAME), is(name));
        MatcherAssert.assertThat(dataFormat.getInt(MdekKeys.FORMAT_NAME_KEY), is(nameKey));
        if (version == null)
            MatcherAssert.assertThat(dataFormat.get(MdekKeys.FORMAT_VERSION), is(nullValue()));
        else
            MatcherAssert.assertThat(dataFormat.get(MdekKeys.FORMAT_VERSION), is(version));
        if (specification == null)
            MatcherAssert.assertThat(dataFormat.get(MdekKeys.FORMAT_SPECIFICATION), is(nullValue()));
        else
            MatcherAssert.assertThat(dataFormat.get(MdekKeys.FORMAT_SPECIFICATION), is(specification));
        if (decompress == null)
            MatcherAssert.assertThat(dataFormat.get(MdekKeys.FORMAT_FILE_DECOMPRESSION_TECHNIQUE), is(nullValue()));
        else
            MatcherAssert.assertThat(dataFormat.get(MdekKeys.FORMAT_FILE_DECOMPRESSION_TECHNIQUE), is(decompress));
    }

    private void assertOperation(Object operation, String type, String url, String platform, String description, String invocationUrl) {
        IngridDocument doc = (IngridDocument) operation;
        MatcherAssert.assertThat(doc.getArrayList(MdekKeys.CONNECT_POINT_LIST).get(0), is(url));
        MatcherAssert.assertThat(doc.getString(MdekKeys.SERVICE_OPERATION_DESCRIPTION), is(description));
        MatcherAssert.assertThat(doc.getString(MdekKeys.SERVICE_OPERATION_NAME), is(type));
        MatcherAssert.assertThat(((IngridDocument) doc.getArrayList(MdekKeys.PLATFORM_LIST).get(0)).getString(MdekKeys.PLATFORM_VALUE), is(platform));
        MatcherAssert.assertThat(doc.getString(MdekKeys.INVOCATION_NAME), is(invocationUrl));
    }

    private void assertSubjectTerms(List<Object> terms, String... expectedTerms) {
        for (String expectedTerm : expectedTerms) {
            boolean found = false;
            for (IngridDocument term : (List<IngridDocument>) (List<?>) terms) {
                if (term.get(MdekKeys.TERM_NAME).equals(expectedTerm)) {
                    found = true;
                    break;
                }
            }
            MatcherAssert.assertThat("The expected term was not found: " + expectedTerm, found, is(true));
        }

    }

    private void assertLink(Object link, String name, String url, String description) {
        IngridDocument linkDoc = (IngridDocument) link;
        MatcherAssert.assertThat(linkDoc.getString(MdekKeys.LINKAGE_NAME), is(name));
        MatcherAssert.assertThat(linkDoc.getString(MdekKeys.LINKAGE_URL), is(url));
        MatcherAssert.assertThat(linkDoc.getString(MdekKeys.LINKAGE_DESCRIPTION), is(description));
    }

    private IngridDocument prepareInsertDocument(String filename) throws Exception {
        return prepareDocument(filename, "csw:Insert");
    }

    private IngridDocument prepareUpdateDocument(String filename) throws Exception {
        return prepareDocument(filename, "csw:Update");
    }

    private IngridDocument prepareDocument(String filename, String tag) throws Exception {
        ClassPathResource inputResource = new ClassPathResource(filename);
        File file = inputResource.getFile();
        String xml = FileUtils.readFileToString(file);

        // extract csw-document
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        Document xmlDoc = builder.parse(new InputSource(new StringReader(xml)));
        NodeList insert = xmlDoc.getElementsByTagName(tag);

        Document singleInsertDocument = builder.newDocument();
        Node importedNode = singleInsertDocument.importNode(insert.item(0).getFirstChild().getNextSibling(), true);
        singleInsertDocument.appendChild(importedNode);
        // end of extract csw-document

        String insertDoc = XMLUtils.toString(singleInsertDocument);
        IngridDocument docIn = new IngridDocument();
        docIn.put(MdekKeys.USER_ID, "TEST_USER_ID");
        // docIn.put( MdekKeys.REQUESTINFO_IMPORT_DATA, GZipTool.gzip( insertDoc ).getBytes());
        docIn.put(MdekKeys.REQUESTINFO_IMPORT_DATA, MdekIdcCatalogJob.compress(new ByteArrayInputStream(insertDoc.getBytes())).toByteArray());
        docIn.put(MdekKeys.REQUESTINFO_IMPORT_FRONTEND_PROTOCOL, "csw202");
        docIn.putBoolean(MdekKeys.REQUESTINFO_IMPORT_START_NEW_ANALYSIS, true);
        docIn.putBoolean(MdekKeys.REQUESTINFO_IMPORT_PUBLISH_IMMEDIATELY, true);
        docIn.putBoolean(MdekKeys.REQUESTINFO_IMPORT_DO_SEPARATE_IMPORT, false);
        docIn.putBoolean(MdekKeys.REQUESTINFO_IMPORT_COPY_NODE_IF_PRESENT, false);
        docIn.putBoolean(MdekKeys.REQUESTINFO_IMPORT_IGNORE_PARENT_IMPORT_NODE, true);

        docIn.put(MdekKeys.REQUESTINFO_IMPORT_OBJ_PARENT_UUID, "2768376B-EE24-4F34-969B-084C55B52278");  // IMPORTKNOTEN
        docIn.put(MdekKeys.REQUESTINFO_IMPORT_ADDR_PARENT_UUID, "BD33BC8E-519E-47F9-8A30-465C95CD0355"); // IMPORTKNOTEN
        return docIn;
    }

    //@Test
    public void handleDocumentUpdate() throws Exception {
        doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock invocation) throws Exception {
                IngridDocument doc = (IngridDocument) invocation.getArgument(0);
                MatcherAssert.assertThat(doc.getString(MdekKeys.USER_ID), is("TEST_USER_ID"));
                MatcherAssert.assertThat(doc.getString(MdekKeys.UUID), is("1234-5678-abcd-efgh"));
                MatcherAssert.assertThat(doc.getBoolean(MdekKeys.REQUESTINFO_FORCE_DELETE_REFERENCES), is(false));
                return null;
            }
        }).when(objectJobMock).storeObject(any());

        doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock invocation) throws Exception {
                HashMap doc = (HashMap) invocation.getArgument(1);
                when(jobHandler.getJobDetailsAsHashMap(any(JobType.class), any(String.class))).thenReturn(doc);
                return null;
            }
        }).when(jobHandler).updateJobInfoDB(any(JobType.class), any(HashMap.class), any(String.class));

        IngridDocument doc = prepareUpdateDocument("csw/update_dataset.xml");
        IngridDocument analyzeImportData = catJob.analyzeImportData(doc);

        MatcherAssert.assertThat(analyzeImportData.get("error"), is(nullValue()));
        //        ProtocolHandler protocol = (ProtocolHandler) analyzeImportData.get( "protocol" );
        //        assertThat( protocol.getProtocol( Type.ERROR ).size(), is( 0 ) );
        //        assertThat( protocol.getProtocol( Type.WARN ).size(), is( 13 ) );
        //        assertThat( protocol.getProtocol( Type.INFO ).size(), is( not( 0 ) ) );

        IngridDocument result = catJob.importEntities(doc);

        Mockito.verify(objectJobMock, Mockito.times(1)).storeObject(any());

        MatcherAssert.assertThat(result, is(not(nullValue())));
        MatcherAssert.assertThat(result.getBoolean("success"), is(true));
    }

    @Test
    public void handleDocumentDelete() throws IOException {
        ClassPathResource inputResource = new ClassPathResource("csw/delete_dataset.xml");
        File file = inputResource.getFile();

        doAnswer(new Answer<IngridDocument>() {
            public IngridDocument answer(InvocationOnMock invocation) {
                IngridDocument doc = (IngridDocument) invocation.getArgument(0);
                MatcherAssert.assertThat(doc.getString(MdekKeys.USER_ID), is("TEST_USER_ID"));
                MatcherAssert.assertThat(doc.getString(MdekKeys.UUID), is("1234-5678-abcd-efgh"));
                MatcherAssert.assertThat(doc.getBoolean(MdekKeys.REQUESTINFO_FORCE_DELETE_REFERENCES), is(true));
                IngridDocument resultDelete = new IngridDocument();
                resultDelete.put(MdekKeys.RESULTINFO_WAS_FULLY_DELETED, true);
                return resultDelete;
            }
        }).when(objectJobMock).deleteObject(any());

        String xml = FileUtils.readFileToString(file);
        IngridDocument result = plug.cswTransaction(xml);

        Mockito.verify(objectJobMock, Mockito.times(1)).deleteObject(any());

        MatcherAssert.assertThat(result, is(not(nullValue())));
        MatcherAssert.assertThat(result.getBoolean("success"), is(true));
    }

    @Test
    public void handleDocumentDeleteFail() throws IOException {
        ClassPathResource inputResource = new ClassPathResource("csw/delete_dataset.xml");
        File file = inputResource.getFile();

        doAnswer(new Answer<IngridDocument>() {
            public IngridDocument answer(InvocationOnMock invocation) {
                IngridDocument doc = (IngridDocument) invocation.getArgument(0);
                MatcherAssert.assertThat(doc.getString(MdekKeys.USER_ID), is("TEST_USER_ID"));
                MatcherAssert.assertThat(doc.getString(MdekKeys.UUID), is("1234-5678-abcd-efgh"));
                MatcherAssert.assertThat(doc.getBoolean(MdekKeys.REQUESTINFO_FORCE_DELETE_REFERENCES), is(true));
                IngridDocument resultDelete = new IngridDocument();
                resultDelete.put(MdekKeys.RESULTINFO_WAS_FULLY_DELETED, false);
                return resultDelete;
            }
        }).when(objectJobMock).deleteObject(any());

        String xml = FileUtils.readFileToString(file);
        IngridDocument result = plug.cswTransaction(xml);

        Mockito.verify(objectJobMock, Mockito.times(1)).deleteObject(any());

        MatcherAssert.assertThat(result, is(not(nullValue())));
        MatcherAssert.assertThat("The CSW-T transaction should not have succeeded.", result.getBoolean("success"), is(false));
    }

    @Test
    @Disabled
    public void importAdditionalField() throws Exception {
        doAnswer(new Answer<Void>() {
            @SuppressWarnings({"unchecked", "rawtypes"})
            public Void answer(InvocationOnMock invocation) throws Exception {
                Map doc = invocation.getArgument(1);
                List<byte[]> data = (List<byte[]>) doc.get(MdekKeys.REQUESTINFO_IMPORT_ANALYZED_DATA);
                MatcherAssert.assertThat(data, is(not(nullValue())));
                MatcherAssert.assertThat(data.size(), is(1));
                InputStream in = new GZIPInputStream(new ByteArrayInputStream(data.get(0)));
                IngridXMLStreamReader reader = new IngridXMLStreamReader(in, importerCallback, "TEST_USER_ID");
                MatcherAssert.assertThat(reader.getObjectUuids().size(), is(1));
                MatcherAssert.assertThat(reader.getObjectUuids().iterator().next(), is("4915275a-733a-47cd-1234-1a3f1e976948"));
                List<Document> domForObject = reader.getDomForObject("4915275a-733a-47cd-1234-1a3f1e976948");
                XPathUtils xpath = new XPathUtils();
                NodeList additionalValues = xpath.getNodeList(domForObject.get(0), "//general-additional-value");
                boolean hasOpenDataSupport = false;
                for (int i = 0; i < additionalValues.getLength(); i++) {
                    String key = xpath.getString(additionalValues.item(i), "field-key");
                    String value = xpath.getString(additionalValues.item(i), "field-data");
                    if ("publicationHmbTG".equals(key) && "true".equals(value))
                        hasOpenDataSupport = true;
                }
                MatcherAssert.assertThat(hasOpenDataSupport, is(true));
                return null;
            }
        }).when(jobHandler).updateJobInfoDB(any(), any(), anyString());

        IngridDocument docIn = prepareInsertDocument("csw/importAdditionalFieldDoc.xml");
        IngridDocument analyzeImportData = catJob.analyzeImportData(docIn);

        //Mockito.verify( catJob, Mockito.times( 1 ) ).analyzeImportData( (IngridDocument) any() );

        MatcherAssert.assertThat(analyzeImportData.get("error"), is(nullValue()));
    }

    @Test
    public void importUseConstraints() throws Exception {
        doAnswer((Answer<Void>) invocation -> {

            IngridDocument docOut = getDocument(invocation, "4915275a-733a-47cd-b1a6-1a3f1e976948");

            List<IngridDocument> useList = (List<IngridDocument>) docOut.get(MdekKeys.USE_CONSTRAINTS);
            MatcherAssert.assertThat(useList.size(), is(2));
            MatcherAssert.assertThat(useList.get(1).get(MdekKeys.USE_LICENSE_VALUE), is("Es gelten keine Bedingungen"));
            MatcherAssert.assertThat(useList.get(1).get(MdekKeys.USE_LICENSE_KEY), is(26));
            return null;
        }).when(jobHandler).updateJobInfoDB(any(), any(), anyString());

        IngridDocument docIn = prepareInsertDocument("csw/importUseConstraints.xml");
        IngridDocument analyzeImportData = catJob.analyzeImportData(docIn);

        MatcherAssert.assertThat(analyzeImportData.get("error"), is(nullValue()));
    }

    @Test
    public void importUseConstraintSourceLicense() throws Exception {
        doAnswer((Answer<Void>) invocation -> {

            IngridDocument docOut = getDocument(invocation, "4915275a-733a-47cd-b1a6-1a3f1e976949");

            List<IngridDocument> useList = (List<IngridDocument>) docOut.get(MdekKeys.USE_CONSTRAINTS);
            MatcherAssert.assertThat(useList.size(), is(5));
            MatcherAssert.assertThat(useList.get(0).get(MdekKeys.USE_LICENSE_VALUE), is("restricted"));
            MatcherAssert.assertThat(useList.get(3).get(MdekKeys.USE_LICENSE_VALUE), is("GNU Free Documentation License (GFDL)"));
            MatcherAssert.assertThat(useList.get(3).get(MdekKeys.USE_LICENSE_SOURCE), is("test the source"));
            MatcherAssert.assertThat(useList.get(4).get(MdekKeys.USE_LICENSE_VALUE), is("Es gelten keine Bedingungen"));
            MatcherAssert.assertThat(useList.get(4).get(MdekKeys.USE_LICENSE_KEY), is(26));
            MatcherAssert.assertThat(useList.get(2).get(MdekKeys.USE_LICENSE_VALUE), is("Mozilla Public License 2.0 (MPL)"));
            MatcherAssert.assertThat(useList.get(1).get(MdekKeys.USE_LICENSE_VALUE), is("Public Domain Mark 1.0 (PDM)"));
            return null;
        }).when(jobHandler).updateJobInfoDB(any(), any(), anyString());

        IngridDocument docIn = prepareInsertDocument("csw/importUseConstraintSourceLicense.xml");
        IngridDocument analyzeImportData = catJob.analyzeImportData(docIn);

        MatcherAssert.assertThat(analyzeImportData.get("error"), is(nullValue()));
    }

    @Test
    public void importUseConstraintSourceLicense_02() throws Exception {
        doAnswer((Answer<Void>) invocation -> {

            IngridDocument docOut = getDocument(invocation, "4915275a-733a-47cd-b1a6-1a3f1e976950");

            List<IngridDocument> useList = (List<IngridDocument>) docOut.get(MdekKeys.USE_CONSTRAINTS);
            MatcherAssert.assertThat(useList.size(), is(2));
            MatcherAssert.assertThat(useList.get(0).get(MdekKeys.USE_LICENSE_VALUE), is("restricted"));
            MatcherAssert.assertThat(useList.get(1).get(MdekKeys.USE_LICENSE_VALUE), is("Creative Commons Namensnennung - Nicht kommerziell (CC BY-NC)"));
            MatcherAssert.assertThat(useList.get(1).get(MdekKeys.USE_LICENSE_SOURCE), is("test the source without a JSON"));
            return null;
        }).when(jobHandler).updateJobInfoDB(any(), any(), anyString());

        IngridDocument docIn = prepareInsertDocument("csw/importUseConstraintSourceLicense_02.xml");
        IngridDocument analyzeImportData = catJob.analyzeImportData(docIn);

        MatcherAssert.assertThat(analyzeImportData.get("error"), is(nullValue()));
    }

    @Test
    public void importUseConstraintSourceLicense_03() throws Exception {
        doAnswer((Answer<Void>) invocation -> {

            IngridDocument docOut = getDocument(invocation, "4915275a-733a-47cd-b1a6-1a3f1e976951");

            List<IngridDocument> useList = (List<IngridDocument>) docOut.get(MdekKeys.USE_CONSTRAINTS);
            MatcherAssert.assertThat(useList.size(), is(3));
            MatcherAssert.assertThat(useList.get(0).get(MdekKeys.USE_LICENSE_VALUE), is("restricted"));
            MatcherAssert.assertThat(useList.get(1).get(MdekKeys.USE_LICENSE_VALUE), is("GNU Free Documentation License (GFDL)"));
            MatcherAssert.assertThat(useList.get(1).get(MdekKeys.USE_LICENSE_SOURCE), is("test the source with JSON"));
            MatcherAssert.assertThat(useList.get(2).get(MdekKeys.USE_LICENSE_VALUE), is("Es gelten keine Bedingungen"));
            MatcherAssert.assertThat(useList.get(2).get(MdekKeys.USE_LICENSE_KEY), is(26));
            return null;
        }).when(jobHandler).updateJobInfoDB(any(), any(), anyString());

        IngridDocument docIn = prepareInsertDocument("csw/importUseConstraintSourceLicense_03.xml");
        IngridDocument analyzeImportData = catJob.analyzeImportData(docIn);

        MatcherAssert.assertThat(analyzeImportData.get("error"), is(nullValue()));
    }

    @Test
    public void importEmptyFormat() throws Exception {
        doAnswer((Answer<Void>) invocation -> {

            IngridDocument docOut = getDocument(invocation, "41638279-BC9B-4625-B70E-884C1A2869D0");

            List<IngridDocument> dataFormat = (List<IngridDocument>) docOut.get(MdekKeys.DATA_FORMATS);
            MatcherAssert.assertThat(dataFormat.size(), is(0));

            return null;
        }).when(jobHandler).updateJobInfoDB(any(), any(), anyString());

        IngridDocument docIn = prepareInsertDocument("csw/importDistributionFormat.xml");
        IngridDocument analyzeImportData = catJob.analyzeImportData(docIn);

        MatcherAssert.assertThat(analyzeImportData.get("error"), is(nullValue()));
    }

    @Test
    @Disabled
    public void deleteFailsWhenOrigIdNotFound() {

    }

    @Test
    @Disabled
    public void deleteFailsWhenUuidNotFound() {
    }

    @Test
    @Disabled
    public void deleteSuccessWhenOrigIdFound() {
    }

    @Test
    @Disabled
    public void deleteSuccessWhenUuidFound() {
    }

    @Test
    @Disabled
    public void updateFailsWhenObjectNotExists() {
    }

    @Test
    @Disabled
    public void insertFailsWhenObjectExists() {
    }

    private void checkXmlResponse(String xml, int inserted, int updated, int deleted) {
        MatcherAssert.assertThat(xml, containsString("<csw:totalInserted>" + String.valueOf(inserted) + "</csw:totalInserted>"));
        MatcherAssert.assertThat(xml, containsString("<csw:totalUpdated>" + String.valueOf(updated) + "</csw:totalUpdated>"));
        MatcherAssert.assertThat(xml, containsString("<csw:totalDeleted>" + String.valueOf(deleted) + "</csw:totalDeleted>"));
    }

    private void assertLocation(Object location, String name, Double longWest, Double longEast, Double latSouth, Double latNorth) {
        IngridDocument locationDoc = (IngridDocument) location;
        MatcherAssert.assertThat(locationDoc.getString(MdekKeys.LOCATION_NAME), is(name));
        MatcherAssert.assertThat(locationDoc.get(MdekKeys.NORTH_BOUNDING_COORDINATE), is(latNorth));
        MatcherAssert.assertThat(locationDoc.get(MdekKeys.SOUTH_BOUNDING_COORDINATE), is(latSouth));
        MatcherAssert.assertThat(locationDoc.get(MdekKeys.EAST_BOUNDING_COORDINATE), is(longEast));
        MatcherAssert.assertThat(locationDoc.get(MdekKeys.WEST_BOUNDING_COORDINATE), is(longWest));

    }
}
