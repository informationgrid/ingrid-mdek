package de.ingrid.mdek.xml.importer.mapper;

import org.apache.log4j.Logger;

import de.ingrid.mdek.job.MdekException;



public class IngridXMLMapperFactory {

	private final static Logger log = Logger.getLogger(IngridXMLMapperFactory.class);

	private IngridXMLMapperFactory() {}

	/** Throws MdekException if version not supported ! */
	public static IngridXMLMapper getIngridXMLMapper(String version) throws MdekException {
		IngridXMLMapper myMapper = null;
		
		if ("1.0".equals(version)) {
			// NO IMPORT OF OLDER VERSION YET ! BUT PREPARED HERE !
			// NOTICE: when implementing then version1_0 needs rework, see TODOs there
//			myMapper = new de.ingrid.mdek.xml.importer.mapper.version1_0.IngridXMLMapperImpl();
			myMapper =  null;
		} else if ("1.0.5".equals(version)) {
			myMapper =  new de.ingrid.mdek.xml.importer.mapper.version105.IngridXMLMapperImpl();

		} else {
			myMapper =  null;
		}

		if (myMapper == null) {
			String msg = "Import Format " + version + " not supported ! Supported version is 1.0.5";
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
