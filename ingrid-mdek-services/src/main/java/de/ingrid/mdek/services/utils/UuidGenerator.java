package de.ingrid.mdek.services.utils;

import java.util.UUID;

import org.apache.log4j.Logger;

/**
 * Singleton used for generating unique uuids.
 * 
 * @author Martin
 */
public class UuidGenerator {

	private static final Logger LOG = Logger.getLogger(UuidGenerator.class);

	private static UuidGenerator myInstance;

	/** Get The Singleton */
	public static synchronized UuidGenerator getInstance() {
		if (myInstance == null) {
	        myInstance = new UuidGenerator();
	      }
		return myInstance;
	}

	private UuidGenerator() {}
	
	public String generateUuid() {
		UUID uuid = java.util.UUID.randomUUID();
		StringBuffer idcUuid = new StringBuffer(uuid.toString().toUpperCase());
		while (idcUuid.length() < 36) {
			idcUuid.append("0");
		}

		return idcUuid.toString();
	}

}
