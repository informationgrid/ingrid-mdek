package de.ingrid.mdek.services.log;

import org.apache.log4j.Logger;

public class AuditService {
    
    private static Logger log = Logger.getLogger( AuditService.class );
    
    public static AuditService instance = null;
    
    public AuditService() {
        instance = this;
    }
    
    public void log(String message) {
        log(message, null);
    }
    
    public void log(String message, String payload) {
        String text = message + (payload != null ? " ==> " + payload : "");
        log.info( text );
    }
}
