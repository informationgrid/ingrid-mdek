/*
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
package de.ingrid.mdek.job;

import de.ingrid.admin.Config;
import de.ingrid.admin.elasticsearch.IndexScheduler;
import de.ingrid.elasticsearch.ElasticConfig;
import de.ingrid.elasticsearch.IBusIndexManager;
import de.ingrid.elasticsearch.IndexManager;
import de.ingrid.elasticsearch.search.IndexImpl;
import de.ingrid.iplug.HeartBeatPlug;
import de.ingrid.iplug.IPlugdescriptionFieldFilter;
import de.ingrid.iplug.PlugDescriptionFieldFilters;
import de.ingrid.iplug.dsc.record.DscRecordCreator;
import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.job.mapping.validation.iso.IsoValidationException;
import de.ingrid.utils.*;
import de.ingrid.utils.dsc.Record;
import de.ingrid.utils.metadata.IMetadataInjector;
import de.ingrid.utils.processor.IPostProcessor;
import de.ingrid.utils.processor.IPreProcessor;
import de.ingrid.utils.query.ClauseQuery;
import de.ingrid.utils.query.FieldQuery;
import de.ingrid.utils.query.IngridQuery;
import de.ingrid.utils.xml.Csw202NamespaceContext;
import de.ingrid.utils.xml.XMLUtils;
import de.ingrid.utils.xpath.XPathUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Service("ige")
public class IgeSearchPlug extends HeartBeatPlug implements IRecordLoader {

    private static Log log = LogFactory.getLog( IgeSearchPlug.class );

    private static final String DATA_PARAMETER = "data";
    
    private static Pattern PATTERN_IDENTIFIER = Pattern.compile("^(.*:)?identifier", Pattern.CASE_INSENSITIVE);

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

    @Autowired
    private IndexScheduler indexScheduler;

    @Autowired
    private ElasticConfig elasticConfig;

    @Autowired
    private IBusIndexManager iBusIndexManager;

    @Autowired
    private IndexManager indexManager;
    
    @Autowired
    private Config baseConfig;

    private final IndexImpl _indexSearcher;

    private XPathUtils utils = new XPathUtils( new Csw202NamespaceContext() );

    private String adminUserUUID;

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

        // request iBus directly to get search results from within this iPlug
        // adapt query to only get results coming from this iPlug and activated in iBus
        // But when not connected to an iBus then use direct connection to Elasticsearch
        if (elasticConfig.esCommunicationThroughIBus) {

            ClauseQuery cq = new ClauseQuery(true, false);
            cq.addField(new FieldQuery(true, false, "iPlugId", baseConfig.communicationProxyUrl));
            query.addClause(cq);
            return this.iBusIndexManager.search(query, start, length);
        }

        return _indexSearcher.search( query, start, length );
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.ingrid.utils.IRecordLoader#getRecord(de.ingrid.utils.IngridHit)
     */
    @Override
    public Record getRecord(IngridHit hit) throws Exception {

        ElasticDocument document;
        if (elasticConfig.esCommunicationThroughIBus) {
            document = this.iBusIndexManager.getDocById(hit.getDocumentId());
        } else {
            document = indexManager.getDocById(hit.getDocumentId());
        }

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
        // request iBus directly to get search results from within this iPlug
        // adapt query to only get results coming from this iPlug and activated in iBus
        // But when not connected to an iBus then use direct connection to Elasticsearch
        if (elasticConfig.esCommunicationThroughIBus) {
            return this.iBusIndexManager.getDetail(hit, query, fields);
        }

        return _indexSearcher.getDetail( hit, query, fields );
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.ingrid.iplug.HeartBeatPlug#close()
     */
    @Override
    public IngridHitDetail[] getDetails(IngridHit[] hits, IngridQuery query, String[] fields) throws Exception {
        // request iBus directly to get search results from within this iPlug
        // adapt query to only get results coming from this iPlug and activated in iBus
        // But when not connected to an iBus then use direct connection to Elasticsearch
        if (elasticConfig.esCommunicationThroughIBus) {
            return this.iBusIndexManager.getDetails(hits, query, fields);
        }

        return _indexSearcher.getDetails( hits, query, fields );
    }

    public IngridDocument call(IngridCall info) {
        IngridDocument doc = null;

        switch (info.getMethod()) {
        case "importCSWDoc":
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) info.getParameter();
            doc = cswTransaction( (String) map.get( DATA_PARAMETER ) );
            break;
        case "index":
            indexScheduler.triggerManually();
            doc = new IngridDocument();
            doc.put( "success", true );
            break;
        default:
            log.warn( "The following method is not supported: " + info.getMethod() );
        }

        return doc;
    }

    public IngridDocument cswTransaction(String xml) {
        IngridDocument doc = new IngridDocument();

        DocumentBuilderFactory factory;
        IngridDocument resultInsert = null;
        IngridDocument resultUpdate = null;
        IngridDocument resultDelete = null;
        int insertedObjects = 0;
        int updatedObjects = 0;
        int deletedObjects = 0;
        try {
            factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware( true );
            DocumentBuilder builder = factory.newDocumentBuilder();

            Document xmlDoc = builder.parse( new InputSource( new StringReader( xml ) ) );
            NodeList insertDocs = xmlDoc.getElementsByTagName( "csw:Insert" );
            NodeList updateDocs = xmlDoc.getElementsByTagName( "csw:Update" );
            NodeList deleteDocs = xmlDoc.getElementsByTagName( "csw:Delete" );

            adminUserUUID = catalogJob.getCatalogAdminUserUuid();

            catalogJob.beginTransaction();
            
            /**
             * INSERT DOCS
             */
            // remember inserted entities for updating ES index and audit log (AFTER commit !)
            List<HashMap> insertedEntities = new ArrayList<HashMap>();

            for (int i = 0; i < insertDocs.getLength(); i++) {
                Node item = insertDocs.item( i );
                String parentUuid = utils.getString( item, ".//gmd:parentIdentifier/gco:CharacterString" );
                IngridDocument document = prepareImportAnalyzeDocument( builder, item );
                // document.putBoolean( MdekKeys.REQUESTINFO_IMPORT_START_NEW_ANALYSIS, i==0 ? true : false );

                IngridDocument analyseResult = catalogJob.analyzeImportData(document);
                /*
                 * If errors were reported during the mapping, then stop the
                 * import and show the validation messages as the exception text
                 */
                if (analyseResult.containsKey("mapping_errors")) {
                    String err = analyseResult.getString("mapping_errors");
                    err = String.format("%s%n%s", "Validation failed.", err);
                    throw new IsoValidationException(err);
                }
                IngridDocument importDoc = prepareImportDocument();
                importDoc.put( MdekKeys.REQUESTINFO_IMPORT_OBJ_PARENT_UUID, parentUuid );
                resultInsert = catalogJob.importEntities( importDoc );
                Exception ex = (Exception) resultInsert.get( MdekKeys.JOBINFO_EXCEPTION );
                if (ex == null) {
                    insertedObjects++;
                }
                if (resultInsert.get(MdekKeys.CHANGED_ENTITIES) != null) {
                    insertedEntities.addAll( (List<HashMap>) resultInsert.get(MdekKeys.CHANGED_ENTITIES));                    
                }
            }

            /**
             * UPDATE DOCS
             */
            // remember updated entities for updating ES index and audit log (AFTER commit !)
            List<HashMap> updatedEntities = new ArrayList<HashMap>();

            for (int i = 0; i < updateDocs.getLength(); i++) {
                Node item = updateDocs.item( i );
                String parentUuid = utils.getString( item, ".//gmd:parentIdentifier/gco:CharacterString" );
                String propName = utils.getString( item, ".//ogc:PropertyIsEqualTo/ogc:PropertyName" );
                String propValue = utils.getString( item, ".//ogc:PropertyIsEqualTo/ogc:Literal" );
                
                if (("uuid".equals( propName ) || PATTERN_IDENTIFIER.matcher( propName ).matches()) && propValue != null) {
                    IngridDocument document = prepareImportAnalyzeDocument( builder, updateDocs.item( i ) );
                    document.put( MdekKeys.REQUESTINFO_IMPORT_ERROR_ON_MISSING_UUID, true );
                    document.put( MdekKeys.REQUESTINFO_IMPORT_OBJ_PARENT_UUID, parentUuid );

                    IngridDocument analyseResult = catalogJob.analyzeImportData( document );
                    /*
                     * If errors were reported during the mapping, then stop the
                     * import and show the validation messages as the exception text
                     */
                    if (analyseResult.containsKey("mapping_errors")) {
                        String err = analyseResult.getString("mapping_errors");
                        err = String.format("%s%n%s", "Validation failed.", err);
                        throw new IsoValidationException(err);
                    }
                    resultUpdate = catalogJob.importEntities( document );
                    updatedObjects++;
                    if (resultUpdate.get(MdekKeys.CHANGED_ENTITIES) != null) {
                        updatedEntities.addAll( (List<HashMap>) resultUpdate.get(MdekKeys.CHANGED_ENTITIES));                    
                    }
                } else {
                    log.error( "Constraint not supported with PropertyName: " + propName + " and Literal: " + propValue );
                    throw new Exception( "Constraint not supported with PropertyName: " + propName + " and Literal: " + propValue );
                }
            }

            /**
             * DELETE DOCS
             */
            // remember updated entities for updating ES index and audit log (AFTER commit !)
            List<HashMap> deletedEntities = new ArrayList<HashMap>();

            for (int i = 0; i < deleteDocs.getLength(); i++) {
                Node item = deleteDocs.item( i );
                String propName = utils.getString( item, ".//ogc:PropertyIsEqualTo/ogc:PropertyName" );
                if (propName == null) {
                    throw new Exception( "Missing or empty Constraint \".//ogc:PropertyIsEqualTo/ogc:PropertyName\".");
                }

                String propValue = utils.getString( item, ".//ogc:PropertyIsEqualTo/ogc:Literal" );
                if (propValue == null) {
                    throw new Exception( "Missing or empty Constraint \".//ogc:PropertyIsEqualTo/ogc:Literal\".");
                }

                // the property "uuid" is still supported for compatibility reasons, see https://dev.informationgrid.eu/redmine/issues/524
                if (("uuid".equals( propName ) || PATTERN_IDENTIFIER.matcher( propName ).matches()) && propValue != null) {
                    IngridDocument params = new IngridDocument();
                    params.put( MdekKeys.USER_ID, adminUserUUID );
                    params.put( MdekKeys.UUID, propValue );
                    params.put( MdekKeys.REQUESTINFO_FORCE_DELETE_REFERENCES, true );
                    params.putBoolean( MdekKeys.REQUESTINFO_TRANSACTION_IS_HANDLED, true );

                    try {
                        // try to delete by ORIG UUID
                        params.put( MdekKeys.REQUESTINFO_USE_ORIG_ID, true );
                        resultDelete = objectJob.deleteObject( params );
                    } catch (MdekException ex) {
                        if (log.isDebugEnabled()) {
                            log.debug( "Could not delete object by ORIG_UUID '" + propValue + "'. Try to delete the object by UUID.", ex );
                        } else {
                            log.info( "Could not delete object by ORIG_UUID '" + propValue + "'. Try to delete the object by UUID.");
                        }
                        // try to delete by UUID
                        params.put( MdekKeys.REQUESTINFO_USE_ORIG_ID, false );
                        resultDelete = objectJob.deleteObject( params );
                    }
                    if (resultDelete.getBoolean( MdekKeys.RESULTINFO_WAS_FULLY_DELETED )) {
                        deletedObjects++;
                        if (resultDelete.get(MdekKeys.CHANGED_ENTITIES) != null) {
                            deletedEntities.addAll( (List<HashMap>) resultDelete.get(MdekKeys.CHANGED_ENTITIES));                    
                        }
                    } else {
                        throw new Exception( "Object could not be deleted: " + propValue );
                    }
                } else {
                    log.error( "Constraint not supported with PropertyName: " + propName + " and Literal: " + propValue );
                    throw new Exception( "Constraint not supported with PropertyName: " + propName + " and Literal: " + propValue );
                }
            }

            catalogJob.commitTransaction();

            // Update search index with data of all published entities and also log audit if set
            // Has to be executed after commit so data in database is up to date !
            catalogJob.updateSearchIndexAndAudit(insertedEntities);
            catalogJob.updateSearchIndexAndAudit(updatedEntities);
            // also remove from index when deleted
            catalogJob.updateSearchIndexAndAudit(deletedEntities);
            
            
            IngridDocument result = new IngridDocument();
            result.putInt( "inserts", insertedObjects );
            result.putInt( "updates", updatedObjects );
            result.putInt( "deletes", deletedObjects );
            result.put( "resultInserts", resultInsert );
            result.put( "resultUpdates", resultUpdate );
            doc.putBoolean( "success", true );
            doc.put( "result", result );

        } catch (Exception e) {
            catalogJob.rollbackTransaction();
            doc.put( "error", prepareException( e ) );
            log.error("Error in CSW transaction", e);
            doc.putBoolean( "success", false );
        }

        return doc;
    }

    private String prepareException(Exception exception) {
        String errorMsg = exception.toString();
        Throwable cause = exception.getCause();
        if (cause == null) {
            cause = exception;
        } else {
            errorMsg = cause.toString();
        }

        if (!(exception instanceof IsoValidationException)) {
            for (StackTraceElement stackTraceElement : cause.getStackTrace()) {
                errorMsg += "\n" + stackTraceElement;
            }
        }
        return errorMsg;
    }

    private IngridDocument prepareImportAnalyzeDocument(DocumentBuilder builder, Node doc) throws Exception {
        // find first child of doc that is an Element i.e. has type Node.ELEMENT_NODE
        Node nodeToImport = null;
        NodeList importCandidates = doc.getChildNodes();
        for(int i=0; i<importCandidates.getLength() && nodeToImport == null; i++) {
            Node candidate = importCandidates.item(i);
            if (candidate.getNodeType() == Node.ELEMENT_NODE) {
                nodeToImport = candidate;
            }
        }
        if (nodeToImport == null) {
            throw new IllegalArgumentException("No valid node for import found.");
        }

        Document singleInsertDocument = builder.newDocument();
        Node importedNode = singleInsertDocument.importNode( nodeToImport, true );
        singleInsertDocument.appendChild( importedNode );
        String insertDoc = XMLUtils.toString( singleInsertDocument );

        IngridDocument docIn = new IngridDocument();
        docIn.put( MdekKeys.USER_ID, adminUserUUID );

        docIn.put( MdekKeys.REQUESTINFO_IMPORT_DATA, MdekIdcCatalogJob.compress( new ByteArrayInputStream( insertDoc.getBytes() ) ).toByteArray() );
        docIn.put( MdekKeys.REQUESTINFO_IMPORT_FRONTEND_PROTOCOL, "csw202" );
        docIn.putBoolean( MdekKeys.REQUESTINFO_IMPORT_START_NEW_ANALYSIS, true );
        docIn.putBoolean( MdekKeys.REQUESTINFO_TRANSACTION_IS_HANDLED, true );
        docIn.putBoolean( MdekKeys.REQUESTINFO_IMPORT_PUBLISH_IMMEDIATELY, true );
        docIn.putBoolean( MdekKeys.REQUESTINFO_IMPORT_DO_SEPARATE_IMPORT, false );
        docIn.putBoolean( MdekKeys.REQUESTINFO_IMPORT_COPY_NODE_IF_PRESENT, false );
        docIn.putBoolean( MdekKeys.REQUESTINFO_IMPORT_ERROR_ON_EXISTING_UUID, false );
        docIn.putBoolean( MdekKeys.REQUESTINFO_IMPORT_ERROR_ON_EXCEPTION, true );
        docIn.putBoolean( MdekKeys.REQUESTINFO_IMPORT_IGNORE_PARENT_IMPORT_NODE, true );
        return docIn;
    }

    private IngridDocument prepareImportDocument() throws Exception {
        IngridDocument docIn = new IngridDocument();
        docIn.put( MdekKeys.USER_ID, adminUserUUID );
        docIn.put( MdekKeys.REQUESTINFO_IMPORT_FRONTEND_PROTOCOL, "csw202" );
        docIn.putBoolean( MdekKeys.REQUESTINFO_IMPORT_PUBLISH_IMMEDIATELY, true );
        docIn.putBoolean( MdekKeys.REQUESTINFO_IMPORT_DO_SEPARATE_IMPORT, false );
        docIn.putBoolean( MdekKeys.REQUESTINFO_IMPORT_COPY_NODE_IF_PRESENT, false );
        docIn.putBoolean( MdekKeys.REQUESTINFO_TRANSACTION_IS_HANDLED, true );
        docIn.putBoolean( MdekKeys.REQUESTINFO_IMPORT_ERROR_ON_EXISTING_UUID, true );
        docIn.putBoolean( MdekKeys.REQUESTINFO_IMPORT_ERROR_ON_EXCEPTION, true );
        docIn.putBoolean( MdekKeys.REQUESTINFO_IMPORT_IGNORE_PARENT_IMPORT_NODE, true );

        return docIn;
    }

    public void setCatalogJob(MdekIdcCatalogJob catalogJob) {
        this.catalogJob = catalogJob;
    }

    public void setObjectJob(MdekIdcObjectJob objectJob) {
        this.objectJob = objectJob;
    }

}
