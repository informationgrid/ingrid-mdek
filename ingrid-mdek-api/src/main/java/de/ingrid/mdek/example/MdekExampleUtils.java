package de.ingrid.mdek.example;

import de.ingrid.mdek.MdekKeys;
import de.ingrid.utils.IngridDocument;

/**
 * Encapsulates common example methods ...
 */
public class MdekExampleUtils {

	private static MdekExampleUtils myInstance;

	/** Get The Singleton */
	public static synchronized MdekExampleUtils getInstance() {
		if (myInstance == null) {
	        myInstance = new MdekExampleUtils();
	      }
		return myInstance;
	}

	private MdekExampleUtils() {}

	public String extractModUserData(IngridDocument inDoc) {
		if (inDoc == null) {
			return null; 
		}

		String user = inDoc.getString(MdekKeys.UUID);
		if (inDoc.get(MdekKeys.NAME) != null) {
			user += " " + inDoc.get(MdekKeys.NAME);
		}
		if (inDoc.get(MdekKeys.GIVEN_NAME) != null) {
			user += " " + inDoc.get(MdekKeys.GIVEN_NAME);
		}
		if (inDoc.get(MdekKeys.ORGANISATION) != null) {
			user += " " + inDoc.get(MdekKeys.ORGANISATION);
		}
		
		return user;
	}
}
