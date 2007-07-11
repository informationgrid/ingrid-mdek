/*
 * Created on 11.07.2007
 */
package de.ingrid.mdek.services.persistence.db;

import org.hibernate.StaleObjectStateException;
import org.junit.Test;

public class GenericHibernateDaoTest extends AbstractDaoTest {

    /**
     * "test" prefix is neccessary because of deps from Spring?
     */
    @Test
    public void testOptimisticLockingPersistent() {
        GenericHibernateDao<Metadata, String> dao = new GenericHibernateDao<Metadata, String>(getSessionFactory(), Metadata.class);

        Metadata m1 = new Metadata("m1k", "m1v");
        Metadata m2 = null;

        dao.beginTransaction();
        dao.makePersistent(m1);
        dao.commitTransaction();

        dao.beginTransaction();
        m2 = dao.getById("m1k", false);
        dao.commitTransaction();

        dao.beginTransaction();
        m1 = dao.getById("m1k", false);
        dao.commitTransaction();

        m2.setMetadataValue("value-m2");
        m1.setMetadataValue("value-m1");

        dao.beginTransaction();
        dao.makePersistent(m2);
        dao.commitTransaction();

        dao.beginTransaction();
        try {
            dao.makePersistent(m1);
            dao.commitTransaction();
            fail("We override a version that we doesn't know.");
        } catch (StaleObjectStateException e) {
            dao.rollbackTransaction();
            assertTrue(true);
        }
    }

    /**
     * "test" prefix is neccessary because of deps from Spring?
     */
    @Test
    public void testOptimisticLockingTransient() {
        GenericHibernateDao<Metadata, String> dao = new GenericHibernateDao<Metadata, String>(getSessionFactory(), Metadata.class);

        Metadata m1 = new Metadata("m1k", "m1v");
        Metadata m2 = null;

        dao.beginTransaction();
        dao.makePersistent(m1);
        dao.commitTransaction();

        dao.beginTransaction();
        m2 = dao.getById("m1k", false);
        dao.commitTransaction();

        dao.beginTransaction();
        m1 = dao.getById("m1k", false);
        dao.commitTransaction();

        m2.setMetadataValue("value-m2");
        m1.setMetadataValue("value-m1");

        dao.beginTransaction();
        dao.makePersistent(m2);
        dao.commitTransaction();
        dao.beginTransaction();
        try {
            dao.makeTransient(m1);
            dao.commitTransaction();
            fail("We delete a version that we doesn't know.");
        } catch (StaleObjectStateException e) {
            dao.rollbackTransaction();
            assertTrue(true);
        }
    }
}
