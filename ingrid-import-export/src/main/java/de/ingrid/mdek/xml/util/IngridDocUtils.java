/*
 * **************************************************-
 * ingrid-import-export
 * ==================================================
 * Copyright (C) 2014 - 2015 wemove digital solutions GmbH
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
package de.ingrid.mdek.xml.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import de.ingrid.utils.IngridDocument;

public class IngridDocUtils {
	public static Integer getIntegerForKey(String key, IngridDocument context) {
		try {
			return context.getInt(key);

		} catch (Exception e) {
			return null;
		}
	}

	public static Double getDoubleForKey(String key, IngridDocument context) {
		if (context != null) {
			return (Double) context.get(key);

		} else {
			return null;
		}
	}

	public static Long getLongForKey(String key, IngridDocument context) {
		try {
			return context.getLong(key);

		} catch (NullPointerException e) {
			return null;
		}
	}

	public static String getStringForKey(String key, IngridDocument context) {
		if (context != null) {
			return context.getString(key);
		} else {
			return null;
		}
	}

	public static List<String> getStringListForKey(String key, IngridDocument context) {
		if (context != null && context.getArrayList(key) != null) {
			return context.getArrayList(key);
		} else {
			return Collections.EMPTY_LIST;
		}
	}

	public static List<Integer> getIntegerListForKey(String key, IngridDocument context) {
		if (context != null && context.getArrayList(key) != null) {
			return context.getArrayList(key);
		} else {
			return Collections.EMPTY_LIST;
		}
	}

	public static List<IngridDocument> getIngridDocumentListForKey(String key, IngridDocument context) {
		if (context != null && context.getArrayList(key) != null) {
			return context.getArrayList(key);
		} else {
			return Collections.EMPTY_LIST;
		}
	}

	public static IngridDocument getIngridDocumentForKey(String key, IngridDocument context) {
		return (IngridDocument) context.get(key);
	}

	public static void putString(String key, String value, IngridDocument target) {
		target.put(key, value);
	}

	public static void putString(String[] keyPath, String value, IngridDocument target) {
		if (keyPath.length == 1) {
			putString(keyPath[0], value, target);

		} else {
			IngridDocument containerDoc = getOrCreateNew(keyPath[0], target);
			putString(Arrays.copyOfRange(keyPath, 1, keyPath.length), value, containerDoc);
		}
	}

	public static IngridDocument getOrCreateNew(String key, IngridDocument doc) {
		IngridDocument value = (IngridDocument) doc.get(key);

		if (value == null) {
			value = new IngridDocument();
			doc.put(key, value);
		}

		return value;
	}

	public static void putInt(String key, Integer value, IngridDocument target) {
		// IngridDocument.putInt throws a null pointer exception if value is null
		// We avoid this by explicitly checking for null
		if (value != null) {
			target.putInt(key, value);
		}
	}

	public static void putInt(String[] keyPath, Integer value, IngridDocument target) {
		if (keyPath.length == 1) {
			putInt(keyPath[0], value, target);

		} else {
			IngridDocument containerDoc = getOrCreateNew(keyPath[0], target);
			putInt(Arrays.copyOfRange(keyPath, 1, keyPath.length), value, containerDoc);
		}
	}

	public static void putDouble(String key, Double value, IngridDocument target) {
		target.put(key, value);
	}

	public static void putDouble(String[] keyPath, Double value, IngridDocument target) {
		if (keyPath.length == 1) {
			putDouble(keyPath[0], value, target);

		} else {
			IngridDocument containerDoc = getOrCreateNew(keyPath[0], target);
			putDouble(Arrays.copyOfRange(keyPath, 1, keyPath.length), value, containerDoc);
		}
	}

	public static void putLong(String key, Long value, IngridDocument target) {
		// IngridDocument.putLong throws a null pointer exception if value is null
		// We avoid this by explicitly checking for null
		if (value != null) {
			target.putLong(key, value);
		}
	}

	public static void putDocList(String key, List<IngridDocument> value, IngridDocument target) {
		target.put(key, value);
	}

	public static void putDocList(String[] keyPath, List<IngridDocument> value, IngridDocument target) {
		if (keyPath.length == 1) {
			putDocList(keyPath[0], value, target);

		} else {
			IngridDocument containerDoc = getOrCreateNew(keyPath[0], target);
			putDocList(Arrays.copyOfRange(keyPath, 1, keyPath.length), value, containerDoc);
		}
	}

	public static void putIntList(String key, List<Integer> value, IngridDocument target) {
		target.put(key, value);
	}

	public static void putStringList(String key, List<String> value, IngridDocument target) {
		target.put(key, value);
	}

	public static void putStringArray(String key, String[] value, IngridDocument target) {
		target.put(key, value);
	}
}
