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

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import de.ingrid.admin.elasticsearch.IndexImpl;
import de.ingrid.iplug.HeartBeatPlug;
import de.ingrid.iplug.IPlugdescriptionFieldFilter;
import de.ingrid.iplug.PlugDescriptionFieldFilters;
import de.ingrid.iplug.dsc.record.DscRecordCreator;
import de.ingrid.mdek.job.csw.CswTransaction;
import de.ingrid.mdek.job.csw.TransactionResponse;
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

@Service("ige")
public class IgeSearchPlug extends HeartBeatPlug implements IRecordLoader {
    
    private static Log log = LogFactory.getLog(IgeSearchPlug.class);
    
    @Autowired
    @Qualifier("dscRecordCreator")
    private DscRecordCreator dscRecordProducerObject = null;
    
    @Autowired
    @Qualifier("dscRecordCreatorAddress")
    private DscRecordCreator dscRecordProducerAddress = null;
    
    @Autowired
    private CswTransaction cswTransaction = null;
    
    public void setCswTransaction(CswTransaction cswTransaction) {
        this.cswTransaction = cswTransaction;
    }

    private final IndexImpl _indexSearcher; 

    @Autowired
    public IgeSearchPlug(final IndexImpl indexSearcher,
            IPlugdescriptionFieldFilter[] fieldFilters,
            IMetadataInjector[] injector, IPreProcessor[] preProcessors,
            IPostProcessor[] postProcessors) throws IOException {
        super(60000, new PlugDescriptionFieldFilters(fieldFilters), injector,
                preProcessors, postProcessors);
        _indexSearcher = indexSearcher;
    }

    /* (non-Javadoc)
     * @see de.ingrid.utils.ISearcher#search(de.ingrid.utils.query.IngridQuery, int, int)
     */
    @Override
    public final IngridHits search(final IngridQuery query, final int start,
            final int length) throws Exception {
        
        if (log.isDebugEnabled()) {
            log.debug("Incoming query: " + query.toString() + ", start="
                    + start + ", length=" + length);
        }
        preProcess(query);
        return _indexSearcher.search(query, start, length);
    }

    /* (non-Javadoc)
     * @see de.ingrid.utils.IRecordLoader#getRecord(de.ingrid.utils.IngridHit)
     */
    @Override
    public Record getRecord(IngridHit hit) throws Exception {
        ElasticDocument document = _indexSearcher.getDocById( hit.getDocumentId() );
        // TODO: choose between different mapping types
        if (document != null) {
            if (document.get( "t01_object.id" ) != null) {
                return dscRecordProducerObject.getRecord(document);
            } else {
                return dscRecordProducerAddress.getRecord(document);
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see de.ingrid.iplug.HeartBeatPlug#close()
     */
    @Override
    public void close() throws Exception {
        _indexSearcher.close();
    }

    /* (non-Javadoc)
     * @see de.ingrid.iplug.HeartBeatPlug#close()
     */
    @Override
    public IngridHitDetail getDetail(IngridHit hit, IngridQuery query,
            String[] fields) throws Exception {
        final IngridHitDetail detail = _indexSearcher.getDetail(hit, query,
                fields);
        return detail;
    }

    /* (non-Javadoc)
     * @see de.ingrid.iplug.HeartBeatPlug#close()
     */
    @Override
    public IngridHitDetail[] getDetails(IngridHit[] hits, IngridQuery query, String[] fields) throws Exception {
        final IngridHitDetail[] details = _indexSearcher.getDetails(hits, query, fields);
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
        
        TransactionResponse response = cswTransaction.execute( xml );
        
        doc.put( "result", response );
        
        doc.putBoolean( "success", true);
        return doc;
    }
    
}
