/*
 * Created on 11.07.2007
 */
package de.ingrid.mdek.services.persistence.db;

import java.io.Serializable;

public interface IEntity extends Serializable {

	Serializable getId();

	Integer getVersion();
}
