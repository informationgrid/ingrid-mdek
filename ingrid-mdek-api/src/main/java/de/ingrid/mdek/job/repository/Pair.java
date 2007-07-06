package de.ingrid.mdek.job.repository;

import java.io.Serializable;

public class Pair implements Serializable {

	private static final long serialVersionUID = -6462535753622802090L;

	private final String _key;

	private final Serializable _value;

	public Pair(String key, Serializable value) {
		_key = key;
		_value = value;
	}

	public String getKey() {
		return _key;
	}

	public Serializable getValue() {
		return _value;
	}
}
