/*
 * **************************************************-
 * ingrid-mdek-services
 * ==================================================
 * Copyright (C) 2014 - 2024 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.2 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl
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
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.FlushModeType;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.hibernate.LockMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.StaleObjectStateException;
import org.hibernate.query.Query;

/**
 * @param <T>
 */
public class GenericHibernateDao<T extends IEntity> extends TransactionService implements
        IGenericDao<T> {

    private Class<T> _persistentClass;

    /**
     * @param factory
     * @param clazz
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
                entity = (T) getSession().get(getPersistentClass(), id, LockMode.UPGRADE_NOWAIT);
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
                entity = (T) getSession().load(getPersistentClass(), id, LockMode.UPGRADE_NOWAIT);
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
        Session session = getSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<T> query = builder.createQuery(getPersistentClass());
        Root<T> root = query.from(getPersistentClass());

        Predicate examplePredicate = buildPredicateFromExample(builder, root, exampleInstance, excludeProperty);

        query.where(examplePredicate);

        Query<T> jpaQuery = session.createQuery(query);
        return jpaQuery.uniqueResult();
    }

    @SuppressWarnings("unchecked")
    protected List<T> findByCriteria(Predicate... predicates) {
        return createCriteria(predicates).list();
    }

    protected Query createCriteria(Predicate... predicates) {
        Session session = getSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<T> query = builder.createQuery(getPersistentClass());
        query.from(getPersistentClass());

        query.where(predicates);
        return session.createQuery(query);
    }

    @SuppressWarnings("unchecked")
    private List<T> findByExample(T exampleInstance, int max, String[] excludeProperties) {
        Session session = getSession();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<T> query = builder.createQuery(getPersistentClass());
        Root<T> root = query.from(getPersistentClass());

        Predicate examplePredicate = buildPredicateFromExample(builder, root, exampleInstance, excludeProperties);

        query.where(examplePredicate);

        Query<T> jpaQuery = session.createQuery(query);

        if (max != -1) {
            jpaQuery.setMaxResults(max);
        }

        return jpaQuery.getResultList();
    }

    public void disableAutoFlush() {
        getSession().setFlushMode(FlushModeType.COMMIT);
    }
    public void flush() {
        getSession().flush();
    }

    private Predicate buildPredicateFromExample(CriteriaBuilder builder, Root<T> root, T exampleInstance, String[] excludeProperty) {
        List<Predicate> predicates = new ArrayList<>();
        List<String> excludeList = List.of(excludeProperty);

        for (Field field : exampleInstance.getClass().getDeclaredFields()) {
            if (!excludeList.contains(field.getName())) {
                field.setAccessible(true);
                try {
                    Object value = field.get(exampleInstance);
                    if (value != null) {
                        predicates.add(builder.equal(root.get(field.getName()), value));
                    }
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Failed to access property: " + field.getName(), e);
                }
            }
        }
        return builder.and(predicates.toArray(new Predicate[0]));
    }
}
