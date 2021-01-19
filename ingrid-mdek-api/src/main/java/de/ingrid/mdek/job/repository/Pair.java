/*
 * **************************************************-
 * ingrid-mdek-api
 * ==================================================
 * Copyright (C) 2014 - 2021 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.1 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl5
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
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

	@Override
	public String toString() {
		return _key + "#" + _value;
	}
}
