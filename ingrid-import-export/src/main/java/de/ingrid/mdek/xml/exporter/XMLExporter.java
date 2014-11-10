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
package de.ingrid.mdek.xml.exporter;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;

import org.apache.log4j.Logger;

import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.MdekUtils.IdcEntityType;
import de.ingrid.mdek.MdekUtils.IdcEntityVersion;
import de.ingrid.utils.IngridDocument;

public class XMLExporter implements IExporter {

	private static final String XML_ENCODING = "UTF-8";

	private final static Logger log = Logger.getLogger(XMLExporter.class);	

	private final IExporterCallback exporterCallback;
	private final XMLOutputFactory outputFactory;
	private IngridXMLStreamWriter writer = null;

	private ByteArrayOutputStream baos;
	private OutputStream out; 
	private String currentUserUuid;

	private int exportCount;
	private int totalNumExport;
	private boolean doCountAdresses = true;
	private List<String> objectAddresses;

	public XMLExporter(IExporterCallback exporterCallback) {
		this.exporterCallback = exporterCallback;
		this.outputFactory = XMLOutputFactory.newInstance();
	}

	@Override
	public byte[] exportAddresses(List<String> rootUuids,
			IdcEntityVersion whichVersion,
			boolean includeSubnodes,
			String userUuid) {
		this.currentUserUuid = userUuid;
		this.exportCount = 0;
		this.totalNumExport = exporterCallback.getTotalNumAddressesToExport(rootUuids, whichVersion, includeSubnodes, userUuid);
		this.doCountAdresses = true;
		
		try {
			setupZipOutputStream();
			setupWriter();
			writeAddresses(rootUuids, whichVersion, includeSubnodes);
			finalizeWriter();

		} catch (IOException ex) {
			// An IOException can occur while creating the output stream. If it happens,
			// we can't really do anything about it except to log the exception and continue
			log.error("IOException while creating a GZIPOutputStream.", ex);

		} catch (XMLStreamException ex) {
			// A XMLStreamException can occur while writing xml data. If that happens,
			// log the error and continue
			log.error("XMLStreamException while writing xml data.", ex);

		} finally {
			closeOutputStream();
		}

		exporterCallback.writeExportInfo(IdcEntityType.ADDRESS, exportCount, exportCount, currentUserUuid);
		return getResultAsByteArray();
	}

	private void setupZipOutputStream() throws IOException {
		baos = new ByteArrayOutputStream();
		out = new GZIPOutputStream(new BufferedOutputStream(baos));
	}

	private void writeAddresses(List<String> rootUuids,
			IdcEntityVersion whichVersion,
			boolean includeSubnodes) throws XMLStreamException {
		writer.writeStartIngridAddresses();

		if (includeSubnodes) {
			writeAddressesWithChildren(rootUuids, whichVersion);
		} else {
			writeAddresses(rootUuids, whichVersion);
		}

		writer.writeEndIngridAddresses();
	}

	private void writeAddresses(List<String> adrUuids,
			IdcEntityVersion whichVersion) throws XMLStreamException {
		for (String adrUuid : adrUuids) {
			writeSingleAddress(adrUuid, whichVersion);
		}
	}

	private void writeSingleAddress(String adrUuid,
			IdcEntityVersion whichVersion) throws XMLStreamException {
		List<IngridDocument>  addressDetails = exporterCallback.getAddressDetails(adrUuid, whichVersion, currentUserUuid);
		if (addressDetails != null && addressDetails.size() > 0) {
			writer.writeIngridAddress(addressDetails);
			if (this.doCountAdresses) {
				exportCount++;
				exporterCallback.writeExportInfo(IdcEntityType.ADDRESS, exportCount, totalNumExport, currentUserUuid);				
			}

		} else {
			exporterCallback.writeExportInfoMessage("Could not export address with uuid "+adrUuid, currentUserUuid);
		}
	}

	private void writeAddressWithChildren(String adrUuid,
			IdcEntityVersion whichVersion) throws XMLStreamException {
		writeSingleAddress(adrUuid, whichVersion);
		writeAddressesWithChildren(exporterCallback.getSubAddresses(adrUuid, whichVersion, currentUserUuid), whichVersion);
	}

	private void writeAddressesWithChildren(List<String> adrUuids,
			IdcEntityVersion whichVersion) throws XMLStreamException {
		for (String adrUuid : adrUuids) {
			writeAddressWithChildren(adrUuid, whichVersion);
		}
	}

	private byte[] getResultAsByteArray() {
		return baos.toByteArray();
	}

	public byte[] exportObjects(List<String> rootUuids, IdcEntityVersion whichVersion, boolean includeSubnodes, String userUuid) {
		this.currentUserUuid = userUuid;
		this.exportCount = 0;
		this.totalNumExport = exporterCallback.getTotalNumObjectsToExport(rootUuids, whichVersion, includeSubnodes, userUuid);
		this.objectAddresses = new ArrayList<String>();
		this.doCountAdresses = false;

		try {
			setupZipOutputStream();
			setupWriter();
			writeObjects(rootUuids, whichVersion, includeSubnodes);
			if (objectAddresses.size() > 0) {
				writeAddresses(this.objectAddresses, whichVersion, false);
			}
			finalizeWriter();

		} catch (IOException ex) {
			// An IOException can occur while creating the output stream. If it happens,
			// we can't really do anything about it except to log the exception and continue
			log.error("IOException while creating a GZIPOutputStream.", ex);

		} catch (XMLStreamException ex) {
			// A XMLStreamException can occur while writing xml data. If that happens,
			// log the error and continue
			log.error("XMLStreamException while writing xml data.", ex);

		} finally {
			closeOutputStream();
		}

		exporterCallback.writeExportInfo(IdcEntityType.OBJECT, exportCount, exportCount, currentUserUuid);
		return getResultAsByteArray();
	}

	private void setupWriter() throws XMLStreamException {
		writer = new IngridXMLStreamWriter(outputFactory.createXMLStreamWriter(out, XML_ENCODING)); 
		writer.writeStartDocument();
	}

	private void finalizeWriter() throws XMLStreamException {
		writer.writeEndDocument();
		writer.flush();
		writer.close();
	}

	private void writeObjects(List<String> rootUuids,
			IdcEntityVersion whichVersion,
			boolean includeSubnodes) throws XMLStreamException {
		writer.writeStartIngridObjects();

		if (includeSubnodes) {
			writeObjectsWithChildren(rootUuids, whichVersion);

		} else {
			writeObjects(rootUuids, whichVersion);
		}

		writer.writeEndIngridObjects();
	}

	private void writeObjects(List<String> objUuids,
			IdcEntityVersion whichVersion) throws XMLStreamException {
		for (String objUuid : objUuids) {
			writeSingleObject(objUuid, whichVersion);
		}
	}

	private void writeSingleObject(String objUuid,
			IdcEntityVersion whichVersion) throws XMLStreamException {
		List<IngridDocument> objectDetails = exporterCallback.getObjectDetails(objUuid, whichVersion, currentUserUuid);
		if (objectDetails != null && objectDetails.size() > 0) {
			writer.writeIngridObject(objectDetails);
			exportCount++;
			exporterCallback.writeExportInfo(IdcEntityType.OBJECT, exportCount, totalNumExport, currentUserUuid);
			
			// extract address references of object !
			extractAddressReferences(objectDetails);

		} else {
			exporterCallback.writeExportInfoMessage("Could not export object with uuid "+objUuid, currentUserUuid);
		}
	}

	private void extractAddressReferences(List<IngridDocument> objDocs) {
		// we always add at same position, so parent is before child !
		int pos = objectAddresses.size();
		for (IngridDocument objDoc : objDocs) {
			List<IngridDocument> addrDocs = (List<IngridDocument>) objDoc.get(MdekKeys.ADR_REFERENCES_TO);
			if (addrDocs != null) {
				for (IngridDocument addrDoc : addrDocs) {
					String addrUuid = addrDoc.getString(MdekKeys.UUID);
					if (addrUuid != null && !this.objectAddresses.contains(addrUuid)) {
						objectAddresses.add(pos, addrUuid);
						
						// extract parent
						addAddressParents(addrUuid, pos);
					}
				}
			}
		}
	}

	private void addAddressParents(String addrUuid, int posToAdd) {
		// fetch only WORKING VERSION, parent has to be the same as in PUBLISHED one ...
		List<IngridDocument>  addrDocs = exporterCallback.getAddressDetails(addrUuid, IdcEntityVersion.WORKING_VERSION, currentUserUuid);		
		if (addrDocs != null) {
			for (IngridDocument addrDoc : addrDocs) {
				String parentUuid = addrDoc.getString(MdekKeys.PARENT_UUID);
				if (parentUuid != null && !this.objectAddresses.contains(parentUuid)) {
					objectAddresses.add(posToAdd, parentUuid);
					
					// extract parent of parent
					addAddressParents(parentUuid, posToAdd);
				}
			}
		}
	}

	private void writeObjectWithChildren(String objUuid,
			IdcEntityVersion whichVersion) throws XMLStreamException {
		writeSingleObject(objUuid, whichVersion);
		writeObjectsWithChildren(exporterCallback.getSubObjects(objUuid, whichVersion, currentUserUuid), whichVersion);
	}

	private void writeObjectsWithChildren(List<String> objUuids,
			IdcEntityVersion whichVersion) throws XMLStreamException {
		for (String objUuid : objUuids) {
			writeObjectWithChildren(objUuid, whichVersion);
		}
	}

	private void closeOutputStream() {
		try {
			out.close();

		} catch (Exception ex) {
			// Catch and log any exception (IOException, NullPointerException...)
			log.error("Error while closing OutputStream.", ex);
		}
	}
}
