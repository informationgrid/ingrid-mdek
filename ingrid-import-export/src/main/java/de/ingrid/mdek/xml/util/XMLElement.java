/*
 * **************************************************-
 * ingrid-import-export
 * ==================================================
 * Copyright (C) 2014 wemove digital solutions GmbH
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class XMLElement {

	private String name;
	private String text;
	private List<XMLElement> children;
	private Map<String, String> attributes;

	public XMLElement(String name) {
		this(name, "");
	}

	public XMLElement(String name, Object textContent) {
		this.name = name;
		text = (textContent != null)? textContent.toString() : "";
		children = new ArrayList<XMLElement>();
		attributes = new HashMap<String, String>();
	}

	public String getName() {
		return name;
	}
	public List<XMLElement> getChildren() {
		return children;
	}
	public void setChildren(List<XMLElement> children) {
		this.children = children;
	}
	public void addChild(XMLElement child) {
		this.children.add(child);
	}
	public void addChildren(Collection<XMLElement> newChildren) {
		this.children.addAll(newChildren);
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public void addAttribute(String attribute, String value) {
		if (value != null) {
			attributes.put(attribute, value);
		}
	}
	public void addAttribute(String attribute, Long value) {
		if (value != null) {
			addAttribute(attribute, value.toString());
		}
	}
	public void addAttribute(String attribute, Integer value) {
		if (value != null) {
			addAttribute(attribute, value.toString());
		}
	}
	public Set<Map.Entry<String, String>> getAttributes() {
		return attributes.entrySet();
	}
	public boolean hasChildren() {
		return children.size() != 0;
	}
	public boolean hasTextContent() {
		return text.length() != 0;
	}
	public boolean hasAttributes() {
		return attributes.size() != 0;
	}
}
