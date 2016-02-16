package de.ingrid.mdek.job.csw;

import java.util.Collection;

import org.geotoolkit.csw.xml.v202.QueryConstraintType;
import org.opengis.metadata.Metadata;
import org.opengis.metadata.citation.ResponsibleParty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.job.MdekIdcObjectJob;
import de.ingrid.utils.IngridDocument;

@Service
public class RelDatabaseCSWPersister implements CSWPersister {

    @Autowired
    MdekIdcObjectJob objectJob;
    
    @Override
    public boolean insertDataset(Metadata metadata) {
        IngridDocument doc = new IngridDocument();
        
        handleFileIdentifier( doc, metadata.getFileIdentifier() );
        
        handleContacts( doc, metadata.getContacts() );
        
        objectJob.storeObject( doc );
        return false;
    }


    @Override
    public boolean updateDataset(Object anyContent) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean deleteDataset(QueryConstraintType constraint) {
        // TODO Auto-generated method stub
        return false;
    }
    
    
    /**
     * HELPER FUNCTIONS
     */
    
    private void handleContacts(IngridDocument doc, Collection<? extends ResponsibleParty> contacts) {
        // TODO Auto-generated method stub
        
    }
    
    private void handleFileIdentifier(IngridDocument doc, String fileIdentifier) {
        doc.put( MdekKeys.ID, fileIdentifier );
    }

}
