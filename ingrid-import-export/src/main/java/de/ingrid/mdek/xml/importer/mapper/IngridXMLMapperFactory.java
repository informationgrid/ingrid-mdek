package de.ingrid.mdek.xml.importer.mapper;

import org.apache.log4j.Logger;

import de.ingrid.mdek.job.MdekException;
import de.ingrid.mdek.xml.Versioning;



public class IngridXMLMapperFactory {

	private final static Logger log = Logger.getLogger(IngridXMLMapperFactory.class);

	private IngridXMLMapperFactory() {}

	/** Throws MdekException if version not supported ! */
	public static IngridXMLMapper getIngridXMLMapper(String version) throws MdekException {
		IngridXMLMapper myMapper = null;
		
		// NO IMPORT OF OLDER VERSION YET ! BUT PREPARED HERE !
		// NOTICE: when implementing then older versions need rework, see TODOs in package files there !
/*
		if ("1.0".equals(version)) {
//			myMapper = new de.ingrid.mdek.xml.importer.mapper.version1_0.IngridXMLMapperImpl();
			myMapper =  null;
		} else if ("1.0.5".equals(version)) {
//			myMapper =  new de.ingrid.mdek.xml.importer.mapper.version105.IngridXMLMapperImpl();
			myMapper =  null;
		} else ...
*/

		if (Versioning.CURRENT_IMPORT_EXPORT_VERSION.equals(version)) {
			try {
				myMapper = (IngridXMLMapper) Versioning.CURRENT_IMPORT_MAPPER_CLASS.newInstance();				
			} catch (Exception exc) {
				String msg = "Problems instantiating Import IngridXMLMapper " + Versioning.CURRENT_IMPORT_MAPPER_CLASS;
				log.error(msg);
				log.error(exc);
				throw new MdekException(msg + "\n" + exc);
			}
		} else {
			String msg = "Import Format " + version + " not supported ! Supported version is " + Versioning.CURRENT_IMPORT_EXPORT_VERSION;
			log.error(msg);
			throw new MdekException(msg);
		}

		if (log.isDebugEnabled()) {
			String msg = "Import Format " + version + " found ! Return mapper that can read this version :" + myMapper.canReadVersion(version);
			log.debug(msg);
		}
		
		return myMapper;
	}
}
