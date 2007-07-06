/*
 * Created on 03.07.2007
 */
package de.ingrid.mdek.services.persistence;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.LockMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Example;

import de.ingrid.mdek.services.persistence.IGenericDao;

/**
 * @param <T>
 * @param <ID>
 */
public abstract class AbstractGenericHibernateDao<T, ID extends Serializable> implements IGenericDao<T, ID> {

    private Class<T> _persistentClass;

    protected SessionFactory _sessionFactory;

    /**
     * @param sessionFactory
     */
    @SuppressWarnings("unchecked")
    public AbstractGenericHibernateDao(SessionFactory sessionFactory) {
        assert sessionFactory != null;
        _sessionFactory = sessionFactory;
        _persistentClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    /**
     * @return The currently set session factory.
     */
    public SessionFactory getSessionFactory() {
        return _sessionFactory;
    }

    public Session getSession() {
        org.hibernate.classic.Session currentSession = _sessionFactory.getCurrentSession();
        if (!currentSession.getTransaction().isActive()) {
            currentSession.beginTransaction();
        }
        return _sessionFactory.getCurrentSession();
    }

    protected Class<T> getPersistentClass() {
        return _persistentClass;
    }

    public List<T> findAll() {
        return findByCriteria();
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
    public T getById(ID id, boolean lock) {
        T entity;
        if (lock)
            entity = (T) getSession().get(getPersistentClass(), id, LockMode.UPGRADE);
        else
            entity = (T) getSession().get(getPersistentClass(), id);

        return entity;
    }

    public T getById(ID id) {
        return getById(id, false);
    }

    @SuppressWarnings("unchecked")
    public T loadById(ID id, boolean lock) {
        T entity;
        if (lock)
            entity = (T) getSession().load(getPersistentClass(), id, LockMode.UPGRADE);
        else
            entity = (T) getSession().load(getPersistentClass(), id);

        return entity;
    }

    public T loadById(ID id) {
        return loadById(id, false);
    }

    public T makePersistent(T entity) {
        getSession().saveOrUpdate(entity);
        getSession().getTransaction().commit();
        return entity;
    }

    public void makeTransient(T entity) {
        getSession().delete(entity);
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
}
