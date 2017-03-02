/*
 * **************************************************-
 * ingrid-mdek-services
 * ==================================================
 * Copyright (C) 2014 - 2017 wemove digital solutions GmbH
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
package de.ingrid.mdek.services.log;

import java.io.File;
import java.util.Enumeration;

import org.apache.log4j.Appender;
import org.apache.log4j.DailyRollingFileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.springframework.stereotype.Service;

@Service
public class LogService implements ILogService {

	private final File _logDirectory;

	public LogService() {
		_logDirectory = new File("logs");
	}

	public Logger getLogger(Class clazz) {
		Logger logger = Logger.getLogger(clazz);
		logger.setAdditivity(false);
		DailyRollingFileAppender debugAppender = new DailyRollingFileAppender();
		debugAppender.setName(clazz.getName());
		debugAppender.setDatePattern("'.'yyyyMMdd");
		debugAppender.setFile(_logDirectory.getAbsolutePath() + File.separator
				+ clazz.getName() + ".log");
		debugAppender.setAppend(true);

		debugAppender.setLayout(new PatternLayout(
				"%5p [%d{yyyy-MM-dd HH:mm:ss}] (%F:%M:%L) - %m%n"));
		debugAppender.setThreshold(Level.DEBUG);
		debugAppender.activateOptions();
		setAppender(debugAppender, clazz.getName());
		
		return logger;
	}

	public void setAppender(Appender appender, String name) {
		Enumeration currentLoggers = LogManager.getCurrentLoggers();
		while (currentLoggers.hasMoreElements()) {
			Logger logger = (Logger) currentLoggers.nextElement();
			if (isLogServiceLogger(logger, name)) {
				logger.removeAppender(appender.getName());
				logger.addAppender(appender);
			}
		}
	}

	private boolean isLogServiceLogger(Logger logger, String otherName) {
		String name = logger.getName();
		if (name.startsWith(otherName)) {
			return true;
		}
		return false;
	}
}
