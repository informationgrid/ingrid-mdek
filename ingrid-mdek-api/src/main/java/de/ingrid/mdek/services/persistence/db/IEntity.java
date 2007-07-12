/*
 * Created on 11.07.2007
 */
package de.ingrid.mdek.services.persistence.db;

import java.io.Serializable;

public interface IEntity extends Serializable {

	long getTimestamp();

	void setTimestamp(long timestamp);

	Serializable getID();
}
