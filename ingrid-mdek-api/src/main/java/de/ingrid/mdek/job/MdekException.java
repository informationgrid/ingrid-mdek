/*
 * **************************************************-
 * ingrid-mdek-api
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
package de.ingrid.mdek.job;

import java.util.ArrayList;
import java.util.List;

import de.ingrid.mdek.MdekError;
import de.ingrid.mdek.MdekError.MdekErrorType;

public class MdekException extends RuntimeException {

	protected List<MdekError> errors = new ArrayList<MdekError>();

    private MdekException() {}

    /** Throws UNSPECIFIED ERROR ! better encapsulate specific error in exception !
     * @param message the detail message. The detail message is saved for later retrieval by the getMessage() method.
     */
    public MdekException(String message) {
    	super(message);
    }

	/** Constructs an exception containing the passed error.
	 * Detail message of exception (getMessage()) is set to error type and encapsulated error message ! */
    public MdekException(MdekError error) {
    	super("" + error);
    	this.errors.add(error);
    }

	/** Constructs an exception with the specified errors.
	 * Detail message of exception (getMessage()) is not set ! */
    public MdekException(List<MdekError> errors) {
    	this.errors = errors;
    }

    public List<MdekError> getMdekErrors() {
    	return errors; 
    }
/*
// NEEDED FOR COMMENTED COPY METHOD BELOW !
	public void setMdekErrors(List<MdekError> mdekErrors) {
		this.errors = mdekErrors;
	}
*/
    /** Returns first encapsulated error from list or null if no error exists. */
    public MdekError getMdekError() {
    	MdekError retError = null;
    	if (errors.size() > 0) {
    		retError = errors.get(0);    		
    	}
    	
    	return retError;
    }

	public String toString() {
		// if "Exception message" equals "Error message" output only once !

		String msgStr = getMessage();
		if (msgStr == null) {
			msgStr = "";
		}

		String errStr = "";
		for (MdekError err : errors) {
			if (errStr.length() > 0) {
				errStr += "\n";				
			}
			errStr += err; 
		}
		
		String retStr = msgStr;
		if (!retStr.equals(errStr)) {
			if (retStr.length() > 0) {
				retStr += "\n";				
			}
			retStr += errStr;
		}
		
		return retStr;
	}

	/** Does this exception contain the passed Error ? */
	public boolean containsError(MdekErrorType errorToCheck) {
		for (MdekError err : errors) {
			if (err.getErrorType().equals(errorToCheck)) {
				return true;
			}
		}
		
		return false;
	}

	/** Returns a copy of the passed MdekException but with a new message.
	 * @param origExc encapsulated MdekErrors and stack trace from this one are copied !
	 * @param newMessage the new message of the returned exception retrievable via getMessage()
	 * @return new MdekException with same MdekErrors and stack trace but new message !
	 */
/*
// NOT USED, UNCOMMENT IF NEEDED (for changing message !)
	public static MdekException copyMdekExceptionNewMessage(MdekException origExc, String newMessage) {
		MdekException myCopy = new MdekException(newMessage);
		myCopy.setMdekErrors(origExc.getMdekErrors());
		myCopy.setStackTrace(origExc.getStackTrace());
		
		return myCopy;
	}
*/
}
