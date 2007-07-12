package de.ingrid.mdek.services.log;

import org.apache.log4j.Logger;

public interface ILogService {

	Logger getLogger(Class clazz);
}
