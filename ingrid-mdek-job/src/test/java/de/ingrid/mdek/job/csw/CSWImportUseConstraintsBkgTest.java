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

import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.job.IJob.JobType;
import de.ingrid.mdek.job.MdekIdcCatalogJob;
import de.ingrid.mdek.job.protocol.ProtocolHandler;
import de.ingrid.mdek.job.protocol.ProtocolHandler.Type;
import de.ingrid.mdek.job.utils.TestSetup;
import de.ingrid.utils.IngridDocument;
import de.ingrid.utils.xml.XMLUtils;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
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
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;

public class CSWImportUseConstraintsBkgTest extends TestSetup {

    @BeforeEach
    public void before() throws Exception {
        String[] mappingScripts = new String[]{
                "ingrid-mdek-job/src/main/resources/import/mapper/csw202_to_ingrid_igc.js",
                "distribution/src/profiles/bkg/conf/import/mapper/csw202_to_ingrid_igc_bkg.js"
        };
        beforeSetup(mappingScripts);
    }

    @AfterEach
    public void after() {
        mockedDatabaseConnectionUtils.close();
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void bkgCsw1() throws Exception {

        doAnswer(new Answer<Void>() {
            @SuppressWarnings("unchecked")
            public Void answer(InvocationOnMock invocation) throws Exception {

                IngridDocument docOut = getDocument(invocation, "4915275a-733a-47cd-b1a6-1a3f1e976948");
                List<IngridDocument> addFields = (List<IngridDocument>) docOut.get(MdekKeys.ADDITIONAL_FIELDS);

                assertThat(addFields.size(), is(2));
                assertThat(addFields.get(1).get(MdekKeys.ADDITIONAL_FIELD_KEY), is("bkg_useConstraints"));

                List<List<IngridDocument>> rowsTemp = (List<List<IngridDocument>>) addFields.get(1).get(MdekKeys.ADDITIONAL_FIELD_ROWS);
                List<IngridDocument> rows = rowsTemp.get(0);
                assertThat(rows.size(), is(2));
                assertThat(rows.get(1).get(MdekKeys.ADDITIONAL_FIELD_KEY), is("bkg_useConstraints_select"));
                assertThat(rows.get(1).get(MdekKeys.ADDITIONAL_FIELD_LIST_ITEM_ID), is("1"));
                assertThat(rows.get(0).get(MdekKeys.ADDITIONAL_FIELD_KEY), is("bkg_useConstraints_freeText"));
                assertThat(rows.get(0).get(MdekKeys.ADDITIONAL_FIELD_DATA), is("meine freie Nutzungsbedingung"));

                List<IngridDocument> useList = (List<IngridDocument>) docOut.get(MdekKeys.USE_CONSTRAINTS);
                for (IngridDocument access : useList) {
                    assertThat(access.get(MdekKeys.USE_LICENSE_VALUE), not(is("meine freie Nutzungsbedingung")));
                    assertThat(access.get(MdekKeys.USE_LICENSE_VALUE), not(is("copyright")));
                    assertThat(access.get(MdekKeys.USE_LICENSE_VALUE), not(is("license")));
                }

                return null;
            }

        }).when(jobHandler).updateJobInfoDB((JobType) any(), (HashMap) any(), anyString());

        IngridDocument docIn = prepareInsertDocument("csw/bkg/doc1.xml");
        IngridDocument analyzeImportData = catJob.analyzeImportData(docIn);

        assertThat(analyzeImportData.get("error"), is(nullValue()));
        ProtocolHandler protocol = (ProtocolHandler) analyzeImportData.get("protocol");
        assertThat(protocol.getProtocol(Type.ERROR).size(), is(0));
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void bkgCsw1Opendata() throws Exception {

        doAnswer(new Answer<Void>() {
            @SuppressWarnings("unchecked")
            public Void answer(InvocationOnMock invocation) throws Exception {

                IngridDocument docOut = getDocument(invocation, "4915275a-733a-47cd-b1a6-1a3f1e976948");
                List<IngridDocument> addFields = (List<IngridDocument>) docOut.get(MdekKeys.ADDITIONAL_FIELDS);

                assertThat(addFields.size(), is(1));
                assertThat(addFields.get(0).get(MdekKeys.ADDITIONAL_FIELD_KEY), is("bkg_useConstraints"));

                List<List<IngridDocument>> rowsTemp = (List<List<IngridDocument>>) addFields.get(0).get(MdekKeys.ADDITIONAL_FIELD_ROWS);
                List<IngridDocument> rows = rowsTemp.get(0);
                assertThat(rows.size(), is(2));
                assertThat(rows.get(0).get(MdekKeys.ADDITIONAL_FIELD_KEY), is("bkg_useConstraints_select"));
                assertThat(rows.get(0).get(MdekKeys.ADDITIONAL_FIELD_LIST_ITEM_ID), is("1"));
                assertThat(rows.get(1).get(MdekKeys.ADDITIONAL_FIELD_KEY), is("bkg_useConstraints_freeText"));
                assertThat(rows.get(1).get(MdekKeys.ADDITIONAL_FIELD_DATA), is("meine freie Nutzungsbedingung"));

                List<IngridDocument> useList = (List<IngridDocument>) docOut.get(MdekKeys.USE_CONSTRAINTS);
                for (IngridDocument access : useList) {
                    assertThat(access.get(MdekKeys.USE_LICENSE_VALUE), not(is("meine freie Nutzungsbedingung")));
                    assertThat(access.get(MdekKeys.USE_LICENSE_VALUE), not(is("copyright")));
                    assertThat(access.get(MdekKeys.USE_LICENSE_VALUE), not(is("license")));
                }

                return null;
            }

        }).when(jobHandler).updateJobInfoDB((JobType) any(), (HashMap) any(), anyString());

        IngridDocument docIn = prepareInsertDocument("csw/bkg/doc1Opendata.xml");
        IngridDocument analyzeImportData = catJob.analyzeImportData(docIn);

        assertThat(analyzeImportData.get("error"), is(nullValue()));
        ProtocolHandler protocol = (ProtocolHandler) analyzeImportData.get("protocol");
        assertThat(protocol.getProtocol(Type.ERROR).size(), is(0));
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void bkgCsw2() throws Exception {

        doAnswer(new Answer<Void>() {
            @SuppressWarnings("unchecked")
            public Void answer(InvocationOnMock invocation) throws Exception {

                IngridDocument docOut = getDocument(invocation, "4915275a-733a-47cd-b1a6-1a3f1e976948");
                List<IngridDocument> addFields = (List<IngridDocument>) docOut.get(MdekKeys.ADDITIONAL_FIELDS);

                assertThat(addFields.size(), is(2));
                assertThat(addFields.get(1).get(MdekKeys.ADDITIONAL_FIELD_KEY), is("bkg_useConstraints"));

                List<List<IngridDocument>> rowsTemp = (List<List<IngridDocument>>) addFields.get(1).get(MdekKeys.ADDITIONAL_FIELD_ROWS);
                List<IngridDocument> rows = rowsTemp.get(0);
                assertThat(rows.size(), is(2));
                assertThat(rows.get(1).get(MdekKeys.ADDITIONAL_FIELD_KEY), is("bkg_useConstraints_select"));
                assertThat(rows.get(1).get(MdekKeys.ADDITIONAL_FIELD_LIST_ITEM_ID), is("14"));
                assertThat(rows.get(0).get(MdekKeys.ADDITIONAL_FIELD_KEY), is("bkg_useConstraints_freeText"));
                assertThat(rows.get(0).get(MdekKeys.ADDITIONAL_FIELD_DATA), is("meine freie Nutzungsbedingung"));

                List<IngridDocument> useList = (List<IngridDocument>) docOut.get(MdekKeys.USE_CONSTRAINTS);
                for (IngridDocument access : useList) {
                    assertThat(access.get(MdekKeys.USE_LICENSE_VALUE), not(is("meine freie Nutzungsbedingung")));
                    assertThat(access.get(MdekKeys.USE_LICENSE_VALUE), not(is("copyright")));
                    assertThat(access.get(MdekKeys.USE_LICENSE_VALUE), not(is("license")));
                }

                return null;
            }

        }).when(jobHandler).updateJobInfoDB((JobType) any(), (HashMap) any(), anyString());

        IngridDocument docIn = prepareInsertDocument("csw/bkg/doc2.xml");
        IngridDocument analyzeImportData = catJob.analyzeImportData(docIn);

        assertThat(analyzeImportData.get("error"), is(nullValue()));
        ProtocolHandler protocol = (ProtocolHandler) analyzeImportData.get("protocol");
        assertThat(protocol.getProtocol(Type.ERROR).size(), is(0));
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void bkgCsw3() throws Exception {

        doAnswer(new Answer<Void>() {
            @SuppressWarnings("unchecked")
            public Void answer(InvocationOnMock invocation) throws Exception {

                IngridDocument docOut = getDocument(invocation, "4915275a-733a-47cd-b1a6-1a3f1e976948");
                List<IngridDocument> addFields = (List<IngridDocument>) docOut.get(MdekKeys.ADDITIONAL_FIELDS);

                assertThat(addFields.size(), is(2));
                assertThat(addFields.get(1).get(MdekKeys.ADDITIONAL_FIELD_KEY), is("bkg_useConstraints"));

                List<List<IngridDocument>> rowsTemp = (List<List<IngridDocument>>) addFields.get(1).get(MdekKeys.ADDITIONAL_FIELD_ROWS);
                List<IngridDocument> rows = rowsTemp.get(0);
                assertThat(rows.size(), is(2));
                assertThat(rows.get(1).get(MdekKeys.ADDITIONAL_FIELD_KEY), is("bkg_useConstraints_select"));
                assertThat(rows.get(1).get(MdekKeys.ADDITIONAL_FIELD_LIST_ITEM_ID), is("1"));
                assertThat(rows.get(0).get(MdekKeys.ADDITIONAL_FIELD_KEY), is("bkg_useConstraints_freeText"));
                assertThat(rows.get(0).get(MdekKeys.ADDITIONAL_FIELD_DATA), is(""));

                List<IngridDocument> useList = (List<IngridDocument>) docOut.get(MdekKeys.USE_CONSTRAINTS);
                for (IngridDocument access : useList) {
                    assertThat(access.get(MdekKeys.USE_LICENSE_VALUE), not(is("copyright")));
                    assertThat(access.get(MdekKeys.USE_LICENSE_VALUE), not(is("license")));
                }

                return null;
            }

        }).when(jobHandler).updateJobInfoDB((JobType) any(), (HashMap) any(), anyString());

        IngridDocument docIn = prepareInsertDocument("csw/bkg/doc3.xml");
        IngridDocument analyzeImportData = catJob.analyzeImportData(docIn);

        assertThat(analyzeImportData.get("error"), is(nullValue()));
        ProtocolHandler protocol = (ProtocolHandler) analyzeImportData.get("protocol");
        assertThat(protocol.getProtocol(Type.ERROR).size(), is(0));
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void bkgCsw4() throws Exception {

        doAnswer(new Answer<Void>() {
            @SuppressWarnings("unchecked")
            public Void answer(InvocationOnMock invocation) throws Exception {

                IngridDocument docOut = getDocument(invocation, "4915275a-733a-47cd-b1a6-1a3f1e976948");
                List<IngridDocument> addFields = (List<IngridDocument>) docOut.get(MdekKeys.ADDITIONAL_FIELDS);

                assertThat(addFields.size(), is(2));
                assertThat(addFields.get(1).get(MdekKeys.ADDITIONAL_FIELD_KEY), is("bkg_useConstraints"));

                List<List<IngridDocument>> rowsTemp = (List<List<IngridDocument>>) addFields.get(1).get(MdekKeys.ADDITIONAL_FIELD_ROWS);
                List<IngridDocument> rows = rowsTemp.get(0);
                assertThat(rows.size(), is(2));
                assertThat(rows.get(1).get(MdekKeys.ADDITIONAL_FIELD_KEY), is("bkg_useConstraints_select"));
                assertThat(rows.get(1).get(MdekKeys.ADDITIONAL_FIELD_LIST_ITEM_ID), is(nullValue()));
                assertThat(rows.get(0).get(MdekKeys.ADDITIONAL_FIELD_KEY), is("bkg_useConstraints_freeText"));
                assertThat(rows.get(0).get(MdekKeys.ADDITIONAL_FIELD_DATA), is("meine freie Nutzungsbedingung"));

                List<IngridDocument> useList = (List<IngridDocument>) docOut.get(MdekKeys.USE_CONSTRAINTS);
                for (IngridDocument access : useList) {
                    assertThat(access.get(MdekKeys.USE_LICENSE_VALUE), not(is("meine freie Nutzungsbedingung")));
                    assertThat(access.get(MdekKeys.USE_LICENSE_VALUE), not(is("copyright")));
                    assertThat(access.get(MdekKeys.USE_LICENSE_VALUE), not(is("license")));
                }

                return null;
            }

        }).when(jobHandler).updateJobInfoDB((JobType) any(), (HashMap) any(), anyString());

        IngridDocument docIn = prepareInsertDocument("csw/bkg/doc4.xml");
        IngridDocument analyzeImportData = catJob.analyzeImportData(docIn);

        assertThat(analyzeImportData.get("error"), is(nullValue()));
        ProtocolHandler protocol = (ProtocolHandler) analyzeImportData.get("protocol");
        assertThat(protocol.getProtocol(Type.ERROR).size(), is(0));
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void bkgCsw5() throws Exception {

        doAnswer(new Answer<Void>() {
            @SuppressWarnings("unchecked")
            public Void answer(InvocationOnMock invocation) throws Exception {

                IngridDocument docOut = getDocument(invocation, "4915275a-733a-47cd-b1a6-1a3f1e976948");
                List<IngridDocument> addFields = (List<IngridDocument>) docOut.get(MdekKeys.ADDITIONAL_FIELDS);

                assertThat(addFields.size(), is(2));
                assertThat(addFields.get(1).get(MdekKeys.ADDITIONAL_FIELD_KEY), is("bkg_useConstraints"));

                List<List<IngridDocument>> rowsTemp = (List<List<IngridDocument>>) addFields.get(1).get(MdekKeys.ADDITIONAL_FIELD_ROWS);
                List<IngridDocument> rows = rowsTemp.get(0);
                assertThat(rows.size(), is(2));
                assertThat(rows.get(1).get(MdekKeys.ADDITIONAL_FIELD_KEY), is("bkg_useConstraints_select"));
                assertThat(rows.get(1).get(MdekKeys.ADDITIONAL_FIELD_LIST_ITEM_ID), is("12"));
                assertThat(rows.get(0).get(MdekKeys.ADDITIONAL_FIELD_KEY), is("bkg_useConstraints_freeText"));
                assertThat(rows.get(0).get(MdekKeys.ADDITIONAL_FIELD_DATA), is("meine freie Nutzungsbedingung"));

                List<IngridDocument> useList = (List<IngridDocument>) docOut.get(MdekKeys.USE_CONSTRAINTS);
                for (IngridDocument access : useList) {
                    assertThat(access.get(MdekKeys.USE_LICENSE_VALUE), not(is("meine freie Nutzungsbedingung")));
                    assertThat(access.get(MdekKeys.USE_LICENSE_VALUE), not(is("copyright")));
                    assertThat(access.get(MdekKeys.USE_LICENSE_VALUE), not(is("license")));
                }

                return null;
            }

        }).when(jobHandler).updateJobInfoDB((JobType) any(), (HashMap) any(), anyString());

        IngridDocument docIn = prepareInsertDocument("csw/bkg/doc5.xml");
        IngridDocument analyzeImportData = catJob.analyzeImportData(docIn);

        assertThat(analyzeImportData.get("error"), is(nullValue()));
        ProtocolHandler protocol = (ProtocolHandler) analyzeImportData.get("protocol");
        assertThat(protocol.getProtocol(Type.ERROR).size(), is(0));
    }


    private IngridDocument prepareInsertDocument(String filename) throws Exception {
        return prepareDocument(filename, "csw:Insert");
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

        docIn.put(MdekKeys.REQUESTINFO_IMPORT_OBJ_PARENT_UUID, "2768376B-EE24-4F34-969B-084C55B52278"); // IMPORTKNOTEN
        docIn.put(MdekKeys.REQUESTINFO_IMPORT_ADDR_PARENT_UUID, "BD33BC8E-519E-47F9-8A30-465C95CD0355"); // IMPORTKNOTEN
        return docIn;
    }


}
