package de.ingrid.mdek.job.csw;

import org.geotoolkit.csw.xml.v202.QueryConstraintType;
import org.opengis.metadata.Metadata;

public interface CSWPersister {
    public boolean insertDataset(Metadata metadata);

    public boolean updateDataset(Object anyContent);

    public boolean deleteDataset(QueryConstraintType constraint);
}
