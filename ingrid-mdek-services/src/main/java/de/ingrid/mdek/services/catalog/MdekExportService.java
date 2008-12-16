package de.ingrid.mdek.services.catalog;

import java.util.ArrayList;
import java.util.List;

import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.MdekUtils.IdcEntityType;
import de.ingrid.mdek.MdekUtils.IdcEntityVersion;
import de.ingrid.mdek.caller.IMdekCaller.FetchQuantity;
import de.ingrid.mdek.xml.exporter.IExporterCallback;
import de.ingrid.utils.IngridDocument;

/**
 * Callbacks for exporter.
 */
public class MdekExportService implements IExporterCallback {

	private static MdekExportService myInstance;

	private MdekCatalogService catalogService;
	private MdekObjectService objectService;
	private MdekAddressService addressService;

	/** Get The Singleton */
	public static synchronized MdekExportService getInstance(MdekCatalogService catalogService,
			MdekObjectService objectService,
			MdekAddressService addressService) {
		if (myInstance == null) {
	        myInstance = new MdekExportService(catalogService, objectService, addressService);
	      }
		return myInstance;
	}

	private MdekExportService(MdekCatalogService catalogService,
			MdekObjectService objectService,
			MdekAddressService addressService) {
		this.catalogService = catalogService;
		this.objectService = objectService;
		this.addressService = addressService;
	}

	/* (non-Javadoc)
	 * @see de.ingrid.mdek.xml.exporter.IExporterCallback#getObjectDetails(java.lang.String, java.lang.String)
	 */
	public IngridDocument getObjectDetails(String objUuid, String userUuid) {
		return objectService.getObjectDetails(objUuid,
				IdcEntityVersion.PUBLISHED_VERSION, FetchQuantity.EXPORT_ENTITY,
				userUuid);
	}

	/* (non-Javadoc)
	 * @see de.ingrid.mdek.xml.exporter.IExporterCallback#getSubObjects(java.lang.String, java.lang.String)
	 */
	public List<String> getSubObjects(String parentUuid, String userUuid) {
		List<IngridDocument> objDocs =
			objectService.getSubObjects(parentUuid, FetchQuantity.EXPORT_ENTITY, userUuid);

		List<String> objUuids = new ArrayList<String>();
		for (IngridDocument objDoc : objDocs) {
			// NOTICE: we fetched published version of sub objects ! may not exist, so we filter
			// "empty" docs.
			String uuid = objDoc.getString(MdekKeys.UUID);
			if (uuid != null) {
				objUuids.add(uuid);				
			}
		}
		
		return objUuids;
	}

	/* (non-Javadoc)
	 * @see de.ingrid.mdek.xml.exporter.IExporterCallback#getAddressDetails(java.lang.String, java.lang.String)
	 */
	public IngridDocument getAddressDetails(String addrUuid, String userUuid) {
		return addressService.getAddressDetails(addrUuid,
				IdcEntityVersion.PUBLISHED_VERSION, FetchQuantity.EXPORT_ENTITY,
				0, 0,
				userUuid);
	}

	/* (non-Javadoc)
	 * @see de.ingrid.mdek.xml.exporter.IExporterCallback#getSubAddresses(java.lang.String, java.lang.String)
	 */
	public List<String> getSubAddresses(String parentUuid, String userUuid) {
		List<IngridDocument> addrDocs =
			addressService.getSubAddresses(parentUuid, FetchQuantity.EXPORT_ENTITY, userUuid);

		List<String> addrUuids = new ArrayList<String>();
		for (IngridDocument addrDoc : addrDocs) {
			// NOTICE: we fetched published version of sub addresses ! may not exist, so we filter
			// "empty" docs.
			String uuid = addrDoc.getString(MdekKeys.UUID);
			if (uuid != null) {
				addrUuids.add(uuid);
			}
		}
		
		return addrUuids;
	}

	/* (non-Javadoc)
	 * @see de.ingrid.mdek.xml.exporter.IExporterCallback#getSysAdditionalFields(java.lang.Long[])
	 */
	public IngridDocument getSysAdditionalFields(Long[] fieldIds) {
		return catalogService.getSysAdditionalFields(fieldIds, null);
	}

	/* (non-Javadoc)
	 * @see de.ingrid.mdek.xml.exporter.IExporterCallback#writeExportInfo(de.ingrid.mdek.MdekUtils.IdcEntityType, int, int, java.lang.String)
	 */
	public void writeExportInfo(IdcEntityType whichType, int numExported, int totalNum,
			String userUuid) {
		catalogService.updateExportInfoDB(whichType, numExported, totalNum, userUuid);
	}

	/* (non-Javadoc)
	 * @see de.ingrid.mdek.xml.exporter.IExporterCallback#writeExportInfoMessage(java.lang.String, java.lang.String)
	 */
	public void writeExportInfoMessage(String newMessage, String userUuid) {
		catalogService.updateExportInfoDBMessages(newMessage, userUuid);
	}
}
