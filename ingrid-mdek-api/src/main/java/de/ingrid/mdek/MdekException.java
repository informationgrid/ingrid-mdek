package de.ingrid.mdek;

import java.util.ArrayList;
import java.util.List;

import de.ingrid.mdek.IMdekErrors.MdekError;

public class MdekException extends RuntimeException {

	protected List<MdekError> errors = new ArrayList<MdekError>();

    private MdekException() {}

	/** Constructs an exception containing the passed error. */
    public MdekException(MdekError error) {
    	this.errors.add(error);
    }

	/** Constructs an exception with the specified errors. */
    public MdekException(List<MdekError> errors) {
    	this.errors = errors;
    }

    public List<MdekError> getMdekErrors() {
    	return errors; 
    }

	public String toString() {
		String ret = "errors: ";
		
		ret += errors.toString();
		
		return ret;
	}

	/** Does this exception contain the passed Error ? */
	public boolean containsError(MdekError errorToCheck) {
		if (errors.contains(errorToCheck)) {
			return true;
		}
		
		return false;
	}
}
