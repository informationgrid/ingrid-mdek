package de.ingrid.mdek.job;

import java.util.ArrayList;
import java.util.List;

import de.ingrid.mdek.MdekError;
import de.ingrid.mdek.MdekError.MdekErrorType;

public class MdekException extends RuntimeException {

	protected List<MdekError> errors = new ArrayList<MdekError>();

    private MdekException() {}

	/** Throws UNSPECIFIED ERROR ! better encapsulate specific error in exception ! */
    public MdekException(String description) {
    	super(description);
    }

	/** Constructs an exception containing the passed error. */
    public MdekException(MdekError error) {
    	super("" + error);
    	this.errors.add(error);
    }

	/** Constructs an exception with the specified errors. */
    public MdekException(List<MdekError> errors) {
    	this.errors = errors;
    }

    public List<MdekError> getMdekErrors() {
    	return errors; 
    }

    /** Returns first encapsulated error from list or null if no error exists. */
    public MdekError getMdekError() {
    	MdekError retError = null;
    	if (errors.size() > 0) {
    		retError = errors.get(0);    		
    	}
    	
    	return retError;
    }

	public String toString() {
		String retStr = getMessage();
		if (retStr == null) {
			retStr = "";
		}
		
		for (MdekError err : errors) {
			if (retStr.length() > 0) {
				retStr += "\n";				
			}
			retStr += err;
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
}
