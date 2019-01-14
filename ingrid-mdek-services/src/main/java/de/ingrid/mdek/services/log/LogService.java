/*
 * **************************************************-
 * ingrid-mdek-services
 * ==================================================
 * Copyright (C) 2014 - 2019 wemove digital solutions GmbH
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

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.RollingFileAppender;
import org.apache.logging.log4j.core.appender.rolling.TimeBasedTriggeringPolicy;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.springframework.stereotype.Service;

@Service
public class LogService implements ILogService {

	private final File _logDirectory;

	public LogService() {
		_logDirectory = new File("logs");
	}

	public Logger getLogger(@SuppressWarnings("rawtypes") Class clazz) {
	    LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
	    Configuration config = ctx.getConfiguration();
		
	    RollingFileAppender appender = RollingFileAppender.newBuilder()
	        .withName( clazz.getName() )
	        .setConfiguration( config )
	        .withFilePattern( _logDirectory.getAbsolutePath() + File.separator + clazz.getName() + ".%d{yyyyMMdd}.log" )
	        .withCreateOnDemand( true )
	        .withAppend( false )
	        .withLayout( PatternLayout.createDefaultLayout() )
	        .withFileName( _logDirectory.getAbsolutePath() + File.separator + clazz.getName() + ".log" )
	        .withPolicy( TimeBasedTriggeringPolicy.createPolicy( "1", "false" ) )
	        .build();
	    
	    config.addAppender( appender );
	    appender.start();
	    
	    Level level = Level.ERROR;
        LoggerConfig loggerConfig = LoggerConfig.createLogger( false, level , clazz.getName(), null, new AppenderRef[0], null, config, null );
	    loggerConfig.addAppender( appender, level, null );
        config.addLogger( clazz.getName(), loggerConfig );

	    return LogManager.getLogger( clazz.getName() );
	}	        
}
