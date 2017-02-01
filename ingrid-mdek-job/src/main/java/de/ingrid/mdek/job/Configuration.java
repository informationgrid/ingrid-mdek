/*
 * **************************************************-
 * InGrid mdek-job
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
package de.ingrid.mdek.job;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import com.tngtech.configbuilder.annotation.propertyloaderconfiguration.PropertiesFiles;
import com.tngtech.configbuilder.annotation.propertyloaderconfiguration.PropertyLocations;
import com.tngtech.configbuilder.annotation.typetransformer.TypeTransformers;
import com.tngtech.configbuilder.annotation.valueextractor.DefaultValue;
import com.tngtech.configbuilder.annotation.valueextractor.PropertyValue;

import de.ingrid.admin.Config;
import de.ingrid.admin.Config.StringToCommunications;
import de.ingrid.admin.JettyStarter;
import de.ingrid.admin.command.CommunicationCommandObject;
import de.ingrid.admin.command.PlugdescriptionCommandObject;
import de.ingrid.importer.udk.Importer;
import de.ingrid.iplug.dsc.index.DatabaseConnection;
import de.ingrid.mdek.Versioning;

@PropertiesFiles( {"config"} )
@PropertyLocations(directories = {"conf"}, fromClassLoader = true)
public class Configuration extends de.ingrid.iplug.dsc.Configuration {
    
    private static Log log = LogFactory.getLog(Configuration.class);
    
    @PropertyValue("iplug.database.dialect")
    @DefaultValue("org.hibernate.dialect.MySQLInnoDBDialect")
    public String databaseDialect;
    
    @PropertyValue("igc.name")
    @DefaultValue("")
    public String igcName;
    
    @PropertyValue("igc.language")
    @DefaultValue("de")
    public String igcLanguage;
    
    @PropertyValue("igc.email")
    @DefaultValue("")
    public String igcEmail;
    
    @PropertyValue("igc.partner")
    @DefaultValue("")
    public String igcPartner;
    
    @PropertyValue("igc.provider")
    @DefaultValue("")
    public String igcProvider;
    
    @PropertyValue("igc.country")
    @DefaultValue("de")
    public String igcCountry;
    
    @TypeTransformers(StringToCommunications.class)
    @PropertyValue("communications.ige")
    public List<CommunicationCommandObject> igeCommunication;
    
    @PropertyValue("communications.ige.clientName")
    @DefaultValue("ige-iplug-test")
    public String igeClientName;
    
    @Override
    public void initialize() {
        super.initialize();
        
        updateDatabaseDescriptor();
        Config config = JettyStarter.getInstance().config;
        String temp = config.communicationProxyUrl;
        config.communicationProxyUrl = igeClientName;
        config.writeCommunication( "conf/communication-ige.xml", igeCommunication );
        config.communicationProxyUrl = temp;
        
        Importer.main( new String[0] );
    }

    private void updateDatabaseDescriptor() {
        try {
            File descriptorFile = getPropertyResource( "descriptor.properties" ).getFile();
            Properties descriptor = new Properties();
            FileInputStream is = new FileInputStream( descriptorFile );
            descriptor.load( is );
            descriptor.setProperty( "db.url", databaseUrl );
            descriptor.setProperty( "db.user", databaseUsername );
            descriptor.setProperty( "db.password", databasePassword );
            descriptor.setProperty( "db.driver", databaseDriver );
            descriptor.setProperty( "db.schema", databaseSchema );
            descriptor.setProperty( "idc.catalogue.name", igcName );
            descriptor.setProperty( "idc.email.default", igcEmail );
            descriptor.setProperty( "idc.partner.name", igcPartner );
            descriptor.setProperty( "idc.provider.name", igcProvider );
            descriptor.setProperty( "idc.catalogue.country", igcCountry );
            descriptor.setProperty( "idc.catalogue.language", igcLanguage );
    //        descriptor.setProperty( "idc.profile.file", commandObject.getPassword() );
            descriptor.setProperty( "idc.version", Versioning.UPDATE_TO_IGC_VERSION );
            FileOutputStream os = new FileOutputStream( new File("conf/descriptor.properties") );
            descriptor.store( os, "" );
            is.close();
            os.close();
        } catch (Exception ex) {
            log.error( "Could not write to descriptor.properties file!", ex );
        }
        
        try {
            Properties descriptor = new Properties();
            File datasourceFile = getPropertyResource( "default-datasource.properties" ).getFile();
            if (!datasourceFile.exists()) {
                datasourceFile.createNewFile();
            }
            FileInputStream is = new FileInputStream( datasourceFile );
            descriptor.load( is );
            descriptor.setProperty( "hibernate.driverClass", databaseDriver );
            descriptor.setProperty( "hibernate.user", databaseUsername );
            descriptor.setProperty( "hibernate.password", databasePassword );
            descriptor.setProperty( "hibernate.dialect",  databaseDialect );
            descriptor.setProperty( "hibernate.jdbcUrl", databaseUrl );
            FileOutputStream os = new FileOutputStream( new File("conf/default-datasource.properties") );
            descriptor.store( os, "" );
            is.close();
            os.close();
        } catch (Exception ex) {
            log.error( "Could not write to descriptor.properties file!", ex );
        }
        
    }
    
    @Override
    public void setPropertiesFromPlugdescription( Properties props, PlugdescriptionCommandObject pd ) {
        super.setPropertiesFromPlugdescription( props, pd );
        
        updateDatabaseDescriptor();
    }
    
    @Override
    public void addPlugdescriptionValues( PlugdescriptionCommandObject pdObject ) {
        super.addPlugdescriptionValues( pdObject );
        pdObject.put( "iPlugClass", "de.ingrid.mdek.job.IgeSearchPlug" );
    }
    
    private Resource getPropertyResource(String fileName) {
        ClassPathResource override = new ClassPathResource( fileName );
        try {
            override.getFile();
            return override;
        } catch (FileNotFoundException e) {
            return new FileSystemResource( "conf/" + fileName );
        } catch (IOException e) {
            log.error( "Error when getting config.override.properties" );
            e.printStackTrace();
        }
        return null;
    }

    public DatabaseConnection getDatabaseConnection() {
        return new DatabaseConnection(
                this.databaseDriver,
                this.databaseUrl,
                this.databaseUsername,
                this.databasePassword,
                this.databaseSchema);
    }

}
