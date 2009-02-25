package de.ingrid.mdek.services.catalog.dbconsistency;

import java.io.Serializable;

public class ErrorReport implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2676932715940599160L;

	private String message;
	
	private String solution;

	public ErrorReport(String message, String solution) {
		this.message = message;
		this.solution = solution;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getSolution() {
		return solution;
	}

	public void setSolution(String solution) {
		this.solution = solution;
	}
}
