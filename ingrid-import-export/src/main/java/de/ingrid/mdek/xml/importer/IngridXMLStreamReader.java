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
package de.ingrid.mdek.xml.importer;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import de.ingrid.mdek.xml.util.file.FileFragmentLoader;
import de.ingrid.mdek.xml.util.file.FileIndex;
import de.ingrid.mdek.xml.util.file.FileIndexer;
import de.ingrid.mdek.xml.util.file.TemporaryFile;

public class IngridXMLStreamReader {
	private final static Logger log = Logger.getLogger(IngridXMLStreamReader.class);
	
	private final FileFragmentLoader fileFragmentLoader;
	private final Map<String, List<FileIndex>> objectIndexMap;
	private final Map<String, List<FileIndex>> addressIndexMap;
	private final DocumentBuilder documentBuilder;
	
	private IImporterCallback importerCallback;
	private String currentUserUuid;

	public IngridXMLStreamReader(InputStream in, IImporterCallback importerCallback, String userUuid) throws IOException {
		TemporaryFile temporaryFile = new TemporaryFile();
		temporaryFile.write(in);

		this.importerCallback 	= importerCallback;
		this.currentUserUuid 	= userUuid;
		fileFragmentLoader 		= new FileFragmentLoader(temporaryFile.getFile());
		FileIndexer fileIndexer = new FileIndexer(temporaryFile.getFile(), importerCallback, userUuid);
		objectIndexMap 			= fileIndexer.getObjectIndexMap();
		addressIndexMap 		= fileIndexer.getAddressIndexMap();

		documentBuilder 		= createDocumentBuilder();
	}

	private DocumentBuilder createDocumentBuilder() {
		try {
			DocumentBuilderFactory documentFactory 	= DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder 		= documentFactory.newDocumentBuilder();
			return documentBuilder;

		} catch (ParserConfigurationException e) {
			// Should not happen with a standard configuration
			log.error("Error creating document builder.", e);
			importerCallback.writeImportInfoMessage(e.toString(), currentUserUuid);
		}
		assert false : "IngridXMLStreamReader was not able to create a standard DocumentBuilder";
		return null;
	}

	public Set<String> getObjectUuids() {
		return objectIndexMap.keySet();
	}

	public Set<String> getAddressUuids() {
		return addressIndexMap.keySet();
	}

	/** Get DOM of all object instances. If size of list > 1 then order is "Bearbeitungsinstanz", "veröffentlichte Instanz". */
	public List<Document> getDomForObject(String uuid) throws IOException, SAXException {
		List<Document> retList = new ArrayList<Document>();
		
		List<FileIndex> fileIndexes = objectIndexMap.get(uuid);
		for (FileIndex fileIndex : fileIndexes) {
			String objectString = fileFragmentLoader.getStringUTF(fileIndex);
			retList.add(documentBuilder.parse(new InputSource(new StringReader(objectString))));
		}

		return retList;
	}

	/** Get DOM of all address instances. If size of list > 1 then order is "Bearbeitungsinstanz", "veröffentlichte Instanz". */
	public List<Document> getDomForAddress(String uuid) throws IOException, SAXException {
		List<Document> retList = new ArrayList<Document>();
		
		List<FileIndex> fileIndexes = addressIndexMap.get(uuid);
		for (FileIndex fileIndex : fileIndexes) {
			String addressString = fileFragmentLoader.getStringUTF(fileIndex);
			retList.add(documentBuilder.parse(new InputSource(new StringReader(addressString))));
		}

		return retList;
	}

	public List<String> getObjectWriteSequence() throws IOException, SAXException {
		Map<String, TreeNode> nodes = new HashMap<String, TreeNode>();

		for (String uuid : getObjectUuids()) {
			List<Document> docs = getDomForObject(uuid);
			String parentUuid = getParentObjectUuid(docs.get(0));
			nodes.put(uuid, new TreeNode(uuid, parentUuid));
		}

		Set<TreeNode> rootNodes = buildTreeNodeHierarchy(nodes);
		List<String> objectSequence = buildUuidSequence(rootNodes);

		return objectSequence;
	}

	public List<String> getAddressWriteSequence() throws IOException, SAXException {
		Map<String, TreeNode> nodes = new HashMap<String, TreeNode>();

		for (String uuid : getAddressUuids()) {
			List<Document> docs = getDomForAddress(uuid);
			String parentUuid = getParentAddressUuid(docs.get(0));
			nodes.put(uuid, new TreeNode(uuid, parentUuid));
		}

		Set<TreeNode> rootNodes = buildTreeNodeHierarchy(nodes);
		List<String> addressSequence = buildUuidSequence(rootNodes);

		return addressSequence;
	}

	private Set<TreeNode> buildTreeNodeHierarchy(Map<String, TreeNode> nodeMap) {
		Set<TreeNode> rootNodes = new HashSet<TreeNode>();
		for (Map.Entry<String, TreeNode> entry : nodeMap.entrySet()) {
			TreeNode node = entry.getValue();
			String parentUuid = node.getParentUuid();
			TreeNode parentNode = nodeMap.get(parentUuid);

			if (parentNode != null) {
				parentNode.addChild(node);

			} else {
				rootNodes.add(node);
			}
		}
		return rootNodes;
	}

	private List<String> buildUuidSequence(Set<TreeNode> rootNodes) {
		List<String> objectSequence = new ArrayList<String>();
		for (TreeNode node : rootNodes) {
			addNodeWithChildren(node, objectSequence);
		}
		return objectSequence;
	}

	private void addNodeWithChildren(TreeNode node, List<String> list) {
		list.add(node.getUuid());
		for (TreeNode child : node.getChildren()) {
			addNodeWithChildren(child, list);
		}
	}

	private String getParentObjectUuid(Document doc) {
		Node parentDataSourceNode = doc.getElementsByTagName("parent-data-source").item(0);
		if (parentDataSourceNode != null) {
			Node parentIdentifierNode = getNodeWithName(parentDataSourceNode.getChildNodes(), "object-identifier");
			return parentIdentifierNode.getTextContent();

		} else {
			return null;
		}
	}

	private String getParentAddressUuid(Document doc) {
		Node parentAddressNode = doc.getElementsByTagName("parent-address").item(0);
		if (parentAddressNode != null) {
			Node parentIdentifierNode = getNodeWithName(parentAddressNode.getChildNodes(), "address-identifier");
			return parentIdentifierNode.getTextContent();

		} else {
			return null;
		}
	}

	private Node getNodeWithName(NodeList nodeList, String nodeName) {
		for (int i = 0; i < nodeList.getLength(); ++i) {
			if (nodeList.item(i).getNodeName().equals(nodeName)) {
				return nodeList.item(i);
			}
		}
		return null;
	}
}


class TreeNode {
	private final String parentUuid;
	private final String uuid;
	private Set<TreeNode> children; 

	public TreeNode(String uuid, String parentUuid) {
		this.uuid = uuid;
		this.parentUuid = parentUuid;
		this.children = new HashSet<TreeNode>();
	}

	public void addChild(TreeNode child) {
		children.add(child);
	}

	public String getParentUuid() {
		return parentUuid;
	}

	public Set<TreeNode> getChildren() {
		return children;
	}

	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(uuid);
		if (!children.isEmpty()) {
			stringBuilder.append('(');
			for (TreeNode child : children) {
				stringBuilder.append(child).append(',');
			}
			stringBuilder.setLength(stringBuilder.length() - 1);
			stringBuilder.append(')');
		}
		return stringBuilder.toString();
	}

	public String getUuid() {
		return uuid;
	}
}
