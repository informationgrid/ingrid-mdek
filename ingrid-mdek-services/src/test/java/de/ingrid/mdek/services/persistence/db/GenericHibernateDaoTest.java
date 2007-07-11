/*
 * Created on 11.07.2007
 */
package de.ingrid.mdek.services.persistence.db;

import org.junit.Test;

public class GenericHibernateDaoTest extends AbstractDaoTest {

    /**
     * "test" prefix is neccessary because of deps from Spring?
     */
    @Test
    public void testOptimisticLocking() {
        GenericHibernateDao<Metadata, String> dao = new GenericHibernateDao<Metadata, String>(getSessionFactory(), Metadata.class);

        Metadata m1 = new Metadata("m1k", "m1v");
        Metadata m2 = null;

        dao.beginTransaction();
        dao.makePersistent(m1);
        dao.commitTransaction();

        dao.beginTransaction();
        m2 = dao.getById("m1k", true);
        dao.commitTransaction();

        dao.beginTransaction();
        m1 = dao.getById("m1k", true);
        dao.commitTransaction();

        m2.setMetadataValue("value-m2");
        m1.setMetadataValue("value-m1");

        dao.beginTransaction();
        dao.makePersistent(m2);
        dao.commitTransaction();

        System.out.println(m1.getMetadataValue());
        System.out.println(m2.getMetadataValue());

        dao.beginTransaction();
        dao.makePersistent(m1);
        dao.commitTransaction();

        System.out.println(m1.getMetadataValue());
        System.out.println(m2.getMetadataValue());
        
        dao.beginTransaction();
        m1 = dao.getById("m1k", true);
        dao.commitTransaction();
        System.out.println(m1.getMetadataValue());
        
    }
}
