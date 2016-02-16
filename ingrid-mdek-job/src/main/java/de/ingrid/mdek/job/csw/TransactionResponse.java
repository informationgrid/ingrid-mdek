package de.ingrid.mdek.job.csw;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TransactionResponse implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -8018799824645803273L;

    private String xmlResponse;
    
    private List<ActionResponse> actionResponses;
    
    private boolean successful;

    public TransactionResponse() {
        actionResponses = new ArrayList<ActionResponse>();
    }

    public boolean isSuccessful() {
        return successful;
    }


    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }


    public List<ActionResponse> getActionResponses() {
        return actionResponses;
    }


    public void setActionResponses(List<ActionResponse> actionResponses) {
        this.actionResponses = actionResponses;
    }
    
    public void addActionResponse(ActionResponse actionResponse) {
        this.actionResponses.add( actionResponse );
    }

    public String getXmlResponse() {
        return xmlResponse;
    }

    public void setXmlResponse(String xmlResponse) {
        this.xmlResponse = xmlResponse;
    }

}
