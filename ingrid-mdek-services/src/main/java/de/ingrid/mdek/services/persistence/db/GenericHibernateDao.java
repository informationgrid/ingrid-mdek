/*
 * **************************************************-
 * ingrid-mdek-services
 * ==================================================
 * Copyright (C) 2014 - 2019 wemove digital solutions GmbH
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
/*
 * Created on 03.07.2007
 */
package de.ingrid.mdek.services.persistence.db;

import java.io.Serializable;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.FlushMode;
import org.hibernate.LockMode;
import org.hibernate.SessionFactory;
import org.hibernate.StaleObjectStateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Example;

/**
 * @param <T>
 * @param <ID>
 */
public class GenericHibernateDao<T extends IEntity> extends TransactionService implements
        IGenericDao<T> {

    private Class<T> _persistentClass;

    /**
     * @param factory 
     * @param clazz 
     * @param sessionFactory
     */
    @SuppressWarnings("unchecked")
    public GenericHibernateDao(SessionFactory factory, Class clazz) {
        super(factory);
        _persistentClass = clazz;
    }

    protected Class<T> getPersistentClass() {
        return _persistentClass;
    }

    public List<T> findAll() {
        return findByCriteria();
    }

    public T findFirst() {
        T entity = null;
        
        List<T> all = findByCriteria();
		if (all != null && all.size() > 0) {
			entity = all.get(0);
		}
		
		return entity;
    }

    public List<T> findByExample(T exampleInstance) {
        return findByExample(exampleInstance, -1, new String[0]);
    }

    public List<T> findByExample(T exampleInstance, int maxResults) {
        return findByExample(exampleInstance, maxResults, new String[0]);
    }

    public T findUniqueByExample(T exampleInstance) {
        return findUniqueByExample(exampleInstance, new String[0]);
    }

    @SuppressWarnings("unchecked")
    public T getById(Serializable id, boolean lock) {
        T entity = null;
    	if (id != null) {
            if (lock) {
                entity = (T) getSession().get(getPersistentClass(), id, LockMode.UPGRADE);
            } else {
                entity = (T) getSession().get(getPersistentClass(), id);
            }    		
    	}

        return entity;
    }

    public T getById(Serializable id) {
        return getById(id, false);
    }

    @SuppressWarnings("unchecked")
    public T loadById(Serializable id, boolean lock) {
        T entity = null;
    	if (id != null) {
            if (lock) {
                entity = (T) getSession().load(getPersistentClass(), id, LockMode.UPGRADE);
            } else {
                entity = (T) getSession().load(getPersistentClass(), id);
            }
    	}

        return entity;
    }

    public T loadById(Serializable id) {
        return loadById(id, false);
    }

    public void makePersistent(T entity) {
    	// TODO Why ? Is handled by HIBERNATE ! (when committing).
    	// Further: This doesn't detect changes ! (due to using cache ?)
//        changedInBetween(entity);
        getSession().saveOrUpdate(entity);
    }

    private void changedInBetween(T entity) {
        int oldVersion = entity.getVersion();
        T entityDb = getById(entity.getId());
        if (entityDb != null) {
            if (entityDb.getVersion() != oldVersion) {
                getSession().evict(entityDb);
                throw new StaleObjectStateException(_persistentClass.getName(), entity.getId());
            }
            getSession().evict(entityDb);
        }
    }

    public void makeTransient(T entity) {
    	// TODO Why ? Is handled by HIBERNATE ! (when committing).
    	// Further: This doesn't detect changes ! (due to using cache ?)
//        changedInBetween(entity);
    	if (entity != null) {
            getSession().delete(entity);    		
    	}
    }

    @SuppressWarnings("unchecked")
    private T findUniqueByExample(T exampleInstance, String[] excludeProperty) {
        Criteria crit = createCriteriaForExample(exampleInstance, excludeProperty);
        return (T) crit.uniqueResult();
    }

    @SuppressWarnings("unchecked")
    protected List<T> findByCriteria(Criterion... criterion) {
        return createCriteria(criterion).list();
    }

    protected Criteria createCriteria(Criterion... criterion) {
        Criteria crit = getSession().createCriteria(getPersistentClass());
        for (Criterion c : criterion) {
            crit.add(c);
        }
        return crit;
    }

    private Criteria createCriteriaForExample(T exampleInstance, String[] excludeProperty) {
        Criteria crit = getSession().createCriteria(getPersistentClass());
        Example example = Example.create(exampleInstance);
        for (String exclude : excludeProperty) {
            example.excludeProperty(exclude);
        }
        crit.add(example);
        return crit;
    }

    @SuppressWarnings("unchecked")
    private List<T> findByExample(T exampleInstance, int max, String[] excludeProperties) {
        Criteria crit = createCriteriaForExample(exampleInstance, excludeProperties);
        if (max != -1) {
            crit.setMaxResults(max);
        }
        return crit.list();
    }

    public void disableAutoFlush() {
        getSession().setFlushMode(FlushMode.MANUAL);
    }
    public void flush() {
        getSession().flush();
    }
}
