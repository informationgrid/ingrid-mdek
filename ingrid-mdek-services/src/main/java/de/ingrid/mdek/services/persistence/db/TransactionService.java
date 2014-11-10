/*
 * **************************************************-
 * ingrid-mdek-services
 * ==================================================
 * Copyright (C) 2014 wemove digital solutions GmbH
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
package de.ingrid.mdek.services.persistence.db;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

public class TransactionService implements ITransactionService {

    private final SessionFactory _sessionFactory;

    public TransactionService(SessionFactory sessionFactory) {
        assert sessionFactory != null;
        _sessionFactory = sessionFactory;
    }

    public void beginTransaction() {
        Session currentSession = getSession();
        if (!currentSession.getTransaction().isActive()) {
            currentSession.beginTransaction();
        }
    }

    public void commitTransaction() {
        Session currentSession = getSession();
        if (currentSession.getTransaction().isActive()) {
        	// flush before commit since during tests sometimes errors occured
        	// during save ("Duplicate entry '15925253' for key 'PRIMARY'")
        	// Let's see if this helps!
        	// NO ! No effect, still errors. We comment flush.
//        	currentSession.flush();
        	currentSession.getTransaction().commit();
        }
    }

    public void rollbackTransaction() {
        Session currentSession = getSession();
        if (currentSession.getTransaction().isActive()) {
            currentSession.getTransaction().rollback();
        }
    }

    public Session getSession() {
        return _sessionFactory.getCurrentSession();
    }
}
