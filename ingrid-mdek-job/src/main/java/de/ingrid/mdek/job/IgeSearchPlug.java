/*
 * **************************************************-
 * InGrid mdek-job
 * ==================================================
 * Copyright (C) 2014 - 2015 wemove digital solutions GmbH
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
package de.ingrid.mdek.job;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import de.ingrid.admin.elasticsearch.IndexImpl;
import de.ingrid.iplug.HeartBeatPlug;
import de.ingrid.iplug.IPlugdescriptionFieldFilter;
import de.ingrid.iplug.PlugDescriptionFieldFilters;
import de.ingrid.iplug.dsc.record.DscRecordCreator;
import de.ingrid.mdek.MdekKeys;
import de.ingrid.utils.ElasticDocument;
import de.ingrid.utils.IRecordLoader;
import de.ingrid.utils.IngridCall;
import de.ingrid.utils.IngridDocument;
import de.ingrid.utils.IngridHit;
import de.ingrid.utils.IngridHitDetail;
import de.ingrid.utils.IngridHits;
import de.ingrid.utils.dsc.Record;
import de.ingrid.utils.metadata.IMetadataInjector;
import de.ingrid.utils.processor.IPostProcessor;
import de.ingrid.utils.processor.IPreProcessor;
import de.ingrid.utils.query.IngridQuery;
import de.ingrid.utils.xml.Csw202NamespaceContext;
import de.ingrid.utils.xml.XMLUtils;
import de.ingrid.utils.xpath.XPathUtils;

@Service("ige")
public class IgeSearchPlug extends HeartBeatPlug implements IRecordLoader {

    private static Log log = LogFactory.getLog( IgeSearchPlug.class );

    @Autowired
    @Qualifier("dscRecordCreator")
    private DscRecordCreator dscRecordProducerObject = null;

    @Autowired
    @Qualifier("dscRecordCreatorAddress")
    private DscRecordCreator dscRecordProducerAddress = null;

    @Autowired
    private MdekIdcCatalogJob catalogJob = null;

    @Autowired
    private MdekIdcObjectJob objectJob = null;

    private final IndexImpl _indexSearcher;

    @Autowired
    public IgeSearchPlug(final IndexImpl indexSearcher, IPlugdescriptionFieldFilter[] fieldFilters, IMetadataInjector[] injector, IPreProcessor[] preProcessors, IPostProcessor[] postProcessors)
            throws IOException {
        super( 60000, new PlugDescriptionFieldFilters( fieldFilters ), injector, preProcessors, postProcessors );
        _indexSearcher = indexSearcher;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.ingrid.utils.ISearcher#search(de.ingrid.utils.query.IngridQuery, int, int)
     */
    @Override
    public final IngridHits search(final IngridQuery query, final int start, final int length) throws Exception {

        if (log.isDebugEnabled()) {
            log.debug( "Incoming query: " + query.toString() + ", start=" + start + ", length=" + length );
        }
        preProcess( query );
        return _indexSearcher.search( query, start, length );
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.ingrid.utils.IRecordLoader#getRecord(de.ingrid.utils.IngridHit)
     */
    @Override
    public Record getRecord(IngridHit hit) throws Exception {
        ElasticDocument document = _indexSearcher.getDocById( hit.getDocumentId() );
        // TODO: choose between different mapping types
        if (document != null) {
            if (document.get( "t01_object.id" ) != null) {
                return dscRecordProducerObject.getRecord( document );
            } else {
                return dscRecordProducerAddress.getRecord( document );
            }
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.ingrid.iplug.HeartBeatPlug#close()
     */
    @Override
    public void close() throws Exception {
        _indexSearcher.close();
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.ingrid.iplug.HeartBeatPlug#close()
     */
    @Override
    public IngridHitDetail getDetail(IngridHit hit, IngridQuery query, String[] fields) throws Exception {
        final IngridHitDetail detail = _indexSearcher.getDetail( hit, query, fields );
        return detail;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.ingrid.iplug.HeartBeatPlug#close()
     */
    @Override
    public IngridHitDetail[] getDetails(IngridHit[] hits, IngridQuery query, String[] fields) throws Exception {
        final IngridHitDetail[] details = _indexSearcher.getDetails( hits, query, fields );
        return details;
    }

    public IngridDocument call(IngridCall info) {
        IngridDocument doc = null;

        switch (info.getMethod()) {
        case "importCSWDoc":
            doc = cswTransaction( (String) info.getParameter() );
        }

        return doc;
    }

    public IngridDocument cswTransaction(String xml) {
        IngridDocument doc = new IngridDocument();

        DocumentBuilderFactory factory;
        // TODO: move to a field!
        XPathUtils utils = new XPathUtils( new Csw202NamespaceContext() );
        try {
            factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware( true );
            DocumentBuilder builder = factory.newDocumentBuilder();

            Document xmlDoc = builder.parse( new InputSource( new StringReader( xml ) ) );
            NodeList insertDocs = xmlDoc.getElementsByTagName( "csw:Insert" );
            NodeList updateDocs = xmlDoc.getElementsByTagName( "csw:Update" );
            NodeList deleteDocs = xmlDoc.getElementsByTagName( "csw:Delete" );

            for (int i = 0; i < insertDocs.getLength(); i++) {
                IngridDocument document = prepareImportDocument( builder, insertDocs.item( i ) );
                IngridDocument analyzerResult = catalogJob.analyzeImportData( document );
                catalogJob.importEntities( document );
            }
            for (int i = 0; i < updateDocs.getLength(); i++) {
                Node item = updateDocs.item( i );
                String propName = utils.getString( item, "//ogc:PropertyIsEqualTo/ogc:PropertyName" );
                String propValue = utils.getString( item, "//ogc:PropertyIsEqualTo/ogc:Literal" );
                
                IngridDocument document = prepareImportDocument( builder, updateDocs.item( i ) );
                IngridDocument analyzerResult = catalogJob.analyzeImportData( document );
                catalogJob.importEntities( document );
            }
            for (int i = 0; i < deleteDocs.getLength(); i++) {
                Node item = deleteDocs.item( i );
                String propName = utils.getString( item, "//ogc:PropertyIsEqualTo/ogc:PropertyName" );
                String propValue = utils.getString( item, "//ogc:PropertyIsEqualTo/ogc:Literal" );

                if ("uuid".equals( propName ) && propValue != null) {
                    IngridDocument params = new IngridDocument();
                    params.put( MdekKeys.USER_ID, "TEST_USER_ID" );
                    params.put( MdekKeys.UUID, propValue );
                    params.put( MdekKeys.REQUESTINFO_FORCE_DELETE_REFERENCES, false );

                    objectJob.deleteObject( params );
                } else {
                    log.warn( "Constraint not supported with PropertyName: " + propName + " and Literal: " + propValue );
                }

            }

        } catch (ParserConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SAXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // doc.put( "result", response );

        doc.putBoolean( "success", true );
        return doc;
    }

    private IngridDocument prepareImportDocument(DocumentBuilder builder, Node doc) throws Exception {
        Document singleInsertDocument = builder.newDocument();
        Node importedNode = singleInsertDocument.importNode( doc.getFirstChild().getNextSibling(), true );
        singleInsertDocument.appendChild( importedNode );
        String insertDoc = XMLUtils.toString( singleInsertDocument );

        IngridDocument docIn = new IngridDocument();
        docIn.put( MdekKeys.USER_ID, "TEST_USER_ID" );
        // docIn.put( MdekKeys.REQUESTINFO_IMPORT_DATA, GZipTool.gzip( insertDoc ).getBytes());
        docIn.put( MdekKeys.REQUESTINFO_IMPORT_DATA, catalogJob.compress( new ByteArrayInputStream( insertDoc.getBytes() ) ).toByteArray() );
        docIn.put( MdekKeys.REQUESTINFO_IMPORT_FRONTEND_PROTOCOL, "csw202" );
        docIn.putBoolean( MdekKeys.REQUESTINFO_IMPORT_START_NEW_ANALYSIS, true );
        return docIn;
    }

    public void setCatalogJob(MdekIdcCatalogJob catalogJob) {
        this.catalogJob = catalogJob;
    }
    
    public void setObjectJob(MdekIdcObjectJob objectJob) {
        this.objectJob = objectJob;
    }

}
