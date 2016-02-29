package de.ingrid.mdek.job.csw;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.geotoolkit.csw.xml.CSWMarshallerPool;
import org.geotoolkit.csw.xml.Transaction;
import org.geotoolkit.csw.xml.v202.DeleteType;
import org.geotoolkit.csw.xml.v202.InsertType;
import org.geotoolkit.csw.xml.v202.QueryConstraintType;
import org.geotoolkit.csw.xml.v202.TransactionResponseType;
import org.geotoolkit.csw.xml.v202.TransactionSummaryType;
import org.geotoolkit.csw.xml.v202.UpdateType;
import org.geotoolkit.xml.MarshallerPool;
import org.opengis.metadata.Metadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@SuppressWarnings("deprecation")
//@Service
public class CswTransaction {

    @Autowired
    CSWPersister persist;

    public void setPersist(CSWPersister persist) {
        this.persist = persist;
    }

    public TransactionResponse execute(String xml) {
        TransactionResponse response = new TransactionResponse();
        boolean isSuccessful = true;
        int inserts = 0;
        int updates = 0;
        int deletes = 0;

        try {
            // parse xml string to Transaction object
            MarshallerPool marshallerPool = CSWMarshallerPool.getInstance();
            Unmarshaller unmarshaller = marshallerPool.acquireUnmarshaller();
            Marshaller marshaller = marshallerPool.acquireMarshaller();
            Transaction trans = (Transaction) unmarshaller.unmarshal( new StringReader( xml ) );
            List<Object> actions = trans.getInsertOrUpdateOrDelete();

            // iterate over all actions and put them into the response
            for (Object action : actions) {
                boolean success = false;
                if (action instanceof InsertType) {
                    InsertType insertAction = (InsertType) action;
                    List<Object> anyContent = insertAction.getAny();
                    for (Object entry : anyContent) {
                        success = persist.insertDataset( (Metadata) entry );
                    }
                    inserts++;

                } else if (action instanceof UpdateType) {
                    UpdateType updateAction = (UpdateType) action;
                    Object anyContent = updateAction.getAny();
                    success = persist.updateDataset( anyContent );
                    updates++;
                } else if (action instanceof DeleteType) {
                    DeleteType deleteType = (DeleteType) action;
                    QueryConstraintType constraint = deleteType.getConstraint();
                    // TODO: delete datasets matching this constraint
                    success = persist.deleteDataset( constraint );
                    deletes++;
                }

                // check if action was successful and flag whole response with unsuccessful
                // if at least one action failed
                if (!success) {
                    isSuccessful = false;
                }
            }

            TransactionSummaryType summaryType = new TransactionSummaryType( inserts, updates, deletes, "xxx" );

            TransactionResponseType ExpResult = new TransactionResponseType( summaryType, null, "2.0.2" );
            StringWriter writer = new StringWriter();
            marshaller.marshal( ExpResult, writer );
            writer.toString();
            response.setXmlResponse( writer.toString() );

        } catch (JAXBException e) {
            isSuccessful = false;
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        response.setSuccessful( isSuccessful );

        return response;
    }

}
