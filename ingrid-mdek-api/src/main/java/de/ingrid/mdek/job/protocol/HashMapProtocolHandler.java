/*
 * **************************************************-
 * Ingrid Portal MDEK Application
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
package de.ingrid.mdek.job.protocol;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;


public class HashMapProtocolHandler implements ProtocolHandler, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 7024006659307001499L;

    private final static Logger log = Logger.getLogger( HashMapProtocolHandler.class );

    private Map<Type, List<String>> messageList;
    private String currentFilename;

    public void startProtocol() {
        if (messageList == null) {
            messageList = new HashMap<Type, List<String>>();
        } else {
            messageList.clear();
        }
    }

    public void addMessage(Type type, String protocolMessage) {
        if (messageList == null) {
            startProtocol();
        }
        if (messageList.get( type ) == null) {
            messageList.put( type, new ArrayList<String>() );
        }
        messageList.get( type ).add( protocolMessage );
    }

    public String getCurrentFilename() {
        return currentFilename;
    }

    public void setCurrentFilename(String currentFilename) {
        this.currentFilename = currentFilename;
    }

    public void stopProtocol() {
        // Not in use for HashMap Protocol
    }

    public void clearProtocol() {
        if (messageList != null) {
            messageList.clear();
        }
    }

    public Map<Type, List<String>> getProtocol() {
        return messageList;
    }
    
    public List<String> getProtocol(Type type) {
        String messageEntities = "";
        if (messageList != null) {
            List<String> messages = messageList.get( type );
            if (messages == null) return new ArrayList<String>();
            return messages;
//            for (String message : messages) {
//                messageEntities = messageEntities.concat( message );
//            }
//            return messageEntities;
        } else {
            return new ArrayList<String>();
        }
    }

    public boolean isDebugEnabled() {
        return log.isDebugEnabled();
    }

    public boolean isInfoEnabled() {
        return log.isInfoEnabled();
    }

    public boolean isErrorEnabled() {
        return log.isErrorEnabled();
    }

    public boolean isWarningEnabled() {
        return log.isWarnEnabled();
    }
}
