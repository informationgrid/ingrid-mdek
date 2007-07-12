package de.ingrid.mdek.services.log;

import java.io.File;
import java.util.Enumeration;

import org.apache.log4j.Appender;
import org.apache.log4j.DailyRollingFileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

public class LogService implements ILogService {

	private final File _logDirectory;

	public LogService(File logDirectory) {
		_logDirectory = logDirectory;
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
				"%5p [%d{yyyy-MM-dd HH:mm:ss}] (%F:%L) - %m%n"));
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
