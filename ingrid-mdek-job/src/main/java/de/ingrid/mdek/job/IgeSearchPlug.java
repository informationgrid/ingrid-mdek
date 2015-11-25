package de.ingrid.mdek.job;

import java.io.IOException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.ingrid.admin.elasticsearch.IndexImpl;
import de.ingrid.iplug.HeartBeatPlug;
import de.ingrid.iplug.IPlugdescriptionFieldFilter;
import de.ingrid.iplug.PlugDescriptionFieldFilters;
import de.ingrid.iplug.dsc.record.DscRecordCreator;
import de.ingrid.utils.ElasticDocument;
import de.ingrid.utils.IRecordLoader;
import de.ingrid.utils.IngridHit;
import de.ingrid.utils.IngridHitDetail;
import de.ingrid.utils.IngridHits;
import de.ingrid.utils.dsc.Record;
import de.ingrid.utils.metadata.IMetadataInjector;
import de.ingrid.utils.processor.IPostProcessor;
import de.ingrid.utils.processor.IPreProcessor;
import de.ingrid.utils.query.IngridQuery;

@Service
public class IgeSearchPlug extends HeartBeatPlug implements IRecordLoader {
    
    private static Log log = LogFactory.getLog(IgeSearchPlug.class);
    
    private List<DscRecordCreator> dscRecordProducer = null;
    
    private final IndexImpl _indexSearcher; 

    @Autowired
    public IgeSearchPlug(final IndexImpl indexSearcher,
            IPlugdescriptionFieldFilter[] fieldFilters,
            IMetadataInjector[] injector, IPreProcessor[] preProcessors,
            IPostProcessor[] postProcessors, List<DscRecordCreator> recCreator) throws IOException {
        super(60000, new PlugDescriptionFieldFilters(fieldFilters), injector,
                preProcessors, postProcessors);
        _indexSearcher = indexSearcher;
        dscRecordProducer = recCreator;
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
                return dscRecordProducer.get( 0 ).getRecord(document);
            } else {
                return dscRecordProducer.get( 1 ).getRecord(document);
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
    
    public List<DscRecordCreator> getDscRecordProducer() {
        return dscRecordProducer;
    }

    public void setDscRecordProducer(List<DscRecordCreator> dscRecordProducer) {
        this.dscRecordProducer = dscRecordProducer;
    }

}
