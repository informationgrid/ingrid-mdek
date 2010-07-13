package de.ingrid.mdek.xml.exporter;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;

import org.apache.log4j.Logger;

import de.ingrid.mdek.MdekUtils.IdcEntityType;
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

	public XMLExporter(IExporterCallback exporterCallback) {
		this.exporterCallback = exporterCallback;
		this.outputFactory = XMLOutputFactory.newInstance();
	}

	@Override
	public byte[] exportAddresses(List<String> rootUuids, boolean includeSubnodes, String userUuid) {
		this.currentUserUuid = userUuid;
		this.exportCount = 0;
		this.totalNumExport = exporterCallback.getTotalNumAddressesToExport(rootUuids, includeSubnodes, userUuid);

		try {
			setupZipOutputStream();
			setupWriterForAddresses();
			writeAddresses(rootUuids, includeSubnodes);
			finalizeWriterForAddresses();

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

	private void setupWriterForAddresses() throws XMLStreamException {
		writer = new IngridXMLStreamWriter(outputFactory.createXMLStreamWriter(out, XML_ENCODING)); 
		writer.writeStartDocument();
		writer.writeStartIngridAddresses();
	}

	private void writeAddresses(List<String> rootUuids, boolean includeSubnodes) throws XMLStreamException {
		if (includeSubnodes) {
			writeAddressesWithChildren(rootUuids);
		} else {
			writeAddresses(rootUuids);
		}
	}

	private void writeAddresses(List<String> adrUuids) throws XMLStreamException {
		for (String adrUuid : adrUuids) {
			writeSingleAddress(adrUuid);
		}
	}

	private void writeSingleAddress(String adrUuid) throws XMLStreamException {
		IngridDocument addressDetails = exporterCallback.getAddressDetails(adrUuid, currentUserUuid);
		if (addressDetails != null) {
			writer.writeIngridAddress(addressDetails);
			exportCount++;
			exporterCallback.writeExportInfo(IdcEntityType.ADDRESS, exportCount, totalNumExport, currentUserUuid);

		} else {
			exporterCallback.writeExportInfoMessage("Could not export address with uuid "+adrUuid, currentUserUuid);
		}
	}

	private void writeAddressWithChildren(String adrUuid) throws XMLStreamException {
		writeSingleAddress(adrUuid);
		writeAddressesWithChildren(exporterCallback.getSubAddresses(adrUuid, currentUserUuid));
	}

	private void writeAddressesWithChildren(List<String> adrUuids) throws XMLStreamException {
		for (String adrUuid : adrUuids) {
			writeAddressWithChildren(adrUuid);
		}
	}

	private void finalizeWriterForAddresses() throws XMLStreamException {
		writer.writeEndIngridAddresses();
		writer.writeEndDocument();
		writer.flush();
		writer.close();
	}

	private byte[] getResultAsByteArray() {
		return baos.toByteArray();
	}

	public byte[] exportObjects(List<String> rootUuids, boolean includeSubnodes, String userUuid) {
		this.currentUserUuid = userUuid;
		this.exportCount = 0;
		this.totalNumExport = exporterCallback.getTotalNumObjectsToExport(rootUuids, includeSubnodes, userUuid);

		try {
			setupZipOutputStream();
			setupWriter();
			writeObjects(rootUuids, includeSubnodes);
			writeAdditionalFields();
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

	private void writeBeginObjects() throws XMLStreamException {
		writer.writeStartIngridObjects();
	}

	private void writeEndObjects() throws XMLStreamException {
		writer.writeEndIngridObjects();
	}

	private void finalizeWriter() throws XMLStreamException {
		writer.writeEndDocument();
		writer.flush();
		writer.close();
	}

	private void writeObjects(List<String> rootUuids, boolean includeSubnodes) throws XMLStreamException {
		writeBeginObjects();

		if (includeSubnodes) {
			writeObjectsWithChildren(rootUuids);

		} else {
			writeObjects(rootUuids);
		}

		writeEndObjects();
	}

	private void writeObjects(List<String> objUuids) throws XMLStreamException {
		for (String objUuid : objUuids) {
			writeSingleObject(objUuid);
		}
	}

	private void writeSingleObject(String objUuid) throws XMLStreamException {
		IngridDocument objectDetails = exporterCallback.getObjectDetails(objUuid, currentUserUuid);
		if (objectDetails != null) {
			writer.writeIngridObject(objectDetails);
			exportCount++;
			exporterCallback.writeExportInfo(IdcEntityType.OBJECT, exportCount, totalNumExport, currentUserUuid);

		} else {
			exporterCallback.writeExportInfoMessage("Could not export object with uuid "+objUuid, currentUserUuid);
		}
	}

	private void writeObjectWithChildren(String objUuid) throws XMLStreamException {
		writeSingleObject(objUuid);
		writeObjectsWithChildren(exporterCallback.getSubObjects(objUuid, currentUserUuid));
	}

	private void writeObjectsWithChildren(List<String> objUuids) throws XMLStreamException {
		for (String objUuid : objUuids) {
			writeObjectWithChildren(objUuid);
		}
	}

	private void writeAdditionalFields() throws XMLStreamException {
		writeBeginAdditionalFields();
		IngridDocument additionalFields = exporterCallback.getSysAdditionalFields(null);

		for (IngridDocument additionalField : (Collection<IngridDocument>) additionalFields.values()) {
			writer.writeAdditionalField(additionalField);
		}

		writeEndAdditionalFields();
	}

	private void writeBeginAdditionalFields() throws XMLStreamException {
		writer.writeStartAdditionalFields();
	}

	private void writeEndAdditionalFields() throws XMLStreamException {
		writer.writeEndAdditionalFields();
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