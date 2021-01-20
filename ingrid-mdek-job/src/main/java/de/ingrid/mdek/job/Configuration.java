/*
 * **************************************************-
 * InGrid mdek-job
 * ==================================================
 * Copyright (C) 2014 - 2021 wemove digital solutions GmbH
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

import com.mchange.v2.c3p0.ComboPooledDataSource;
import de.ingrid.admin.Config;
import de.ingrid.admin.command.CommunicationCommandObject;
import de.ingrid.admin.command.PlugdescriptionCommandObject;
import de.ingrid.importer.udk.Importer;
import de.ingrid.iplug.dsc.index.DatabaseConnection;
import de.ingrid.iplug.dsc.utils.DatabaseConnectionUtils;
import de.ingrid.mdek.Versioning;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.beans.PropertyVetoException;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

@org.springframework.context.annotation.Configuration
public class Configuration extends de.ingrid.iplug.dsc.Configuration {
    
    private static Log log = LogFactory.getLog(Configuration.class);

    @Autowired
    private Config config;
    
    @Value("${iplug.database.dialect:org.hibernate.dialect.MySQLInnoDBDialect}")
    public String databaseDialect;

    @Value("${iplug.database.debugSQL:false}")
    public boolean databaseShowSQL;
    
    @Value("${igc.name:}")
    public String igcName;
    
    @Value("${igc.language:de}")
    public String igcLanguage;
    
    @Value("${igc.email:}")
    public String igcEmail;
    
    @Value("${igc.partner:}")
    public String igcPartner;
    
    @Value("${igc.provider:}")
    public String igcProvider;
    
    @Value("${igc.country:de}")
    public String igcCountry;

    @Value("${igc.enableIBusCommunication:true}")
    public boolean igcEnableIBusCommunication;

    @Value("${igc.default.metadata.standard.name.geodata:}")
    public String defaultMdStandardNameGeodata;

    @Value("${igc.default.metadata.standard.version.geodata:}")
    public String defaultMdStandardNameVersionGeodata;

    @Value("${igc.default.metadata.standard.name.geoservice:}")
    public String defaultMdStandardNameGeoservice;

    @Value("${igc.default.metadata.standard.version.geoservice:}")
    public String defaultMdStandardNameVersionGeoservice;

    public List<CommunicationCommandObject> igeCommunication;
    
    @Value("${communications.ige.clientName:ige-iplug-test}")
    public String igeClientName;
    
    // @CommandLineValue(longOpt = "reconnectIntervall", shortOpt = "ri")
    @Value("${communications.ige.reconnectInterval:30}")
    public Integer reconnectInterval;
    
    
    /**
     *  Holds the base url that points to the document store. It will be used as
     *  a base for generating the download links in the uvp portal detail view.
     *  
     *  This property is specific to the uvp profile.
     *  
     *  The property was introduced to be able to connect UVP NI installation to
     *  uvp-verbund.de. Since all document urls were relative before. With this 
     *  property set to an absolute base url, exchange of datasets between uvp
     *  ingrid installations is no problem anymore. 
     *  
     *  see also https://redmine.informationgrid.eu/issues/915
     *  
     */
    @Value("${profile.uvp.document.store.base.url:/documents/}")
    public String profileUvpDocumentStoreBaseUrl;

    /**
     *  If set to true existing addresses are overwritten during import of ISO 19139
     *  data.
     *
     *  The default behavior of the ISO 19139 import is not to overwrite
     *  existing addresses (see INGRID33-16). This allows address management through
     *  the editor ans makes sure that the addresses are not accidentally overwritten
     *  by an ISO 19139 import.
     *
     *  This property was introduced because in cases were the datasets are only managed
     *  by CSW-T imports, there was no way to overwrite existing address data by CSW-T
     *  transactions.
     *
     *  see also https://redmine.informationgrid.eu/issues/1764
     *
     */
    @Value("${import.overwrite.addresses:false}")
    public Boolean importOverwriteAddresses;

    /**
     *  If set to true no publication condition check is executed against datasets that
     *  reference an address that will be overwritten by an ISO 19139 import.
     *
     *  This property was introduced because in cases were the datasets are only managed
     *  by CSW-T imports, this check is not necessary. All datasets are published and have
     *  the same publication condition. Also this check can be very time consuming in cases
     *  of addresses referenced by many datasets (HZG use case has 25.000 datasets
     *  referencing the same address.)
     *
     *  This property depends on import.overwrite.addresses:true.
     *
     *  see also https://redmine.informationgrid.eu/issues/1764
     *
     */
    @Value("${import.overwrite.addresses.check.referencing.datasets:true}")
    public Boolean importOverwriteAddressesCheckReferencingDatasets;




    @Bean
    public ComboPooledDataSource dataSource() throws PropertyVetoException {
        ComboPooledDataSource source = new ComboPooledDataSource();
        source.setDriverClass(databaseDriver);
        source.setJdbcUrl(databaseUrl);
        source.setUser(databaseUsername);
        source.setPassword(databasePassword);
        source.setMinPoolSize(5);
        source.setMaxPoolSize(20);
        source.setAcquireIncrement(5);
        source.setIdleConnectionTestPeriod(300);
        source.setMaxIdleTime(600);
        source.setMaxStatements(0);
        source.setAcquireRetryAttempts(30);
        source.setAcquireRetryDelay(1000);
        return source;
    }

    @Bean
    public PropertiesFactoryBean hibernateProperties() {
        PropertiesFactoryBean props = new PropertiesFactoryBean();
        Properties p = new Properties();
        p.put("hibernate.cache.provider_class", "org.hibernate.cache.NoCacheProvider");
        p.put("hibernate.dialect", databaseDialect);
        p.put("hibernate.transaction.factory_class", "org.hibernate.transaction.JDBCTransactionFactory");
        p.put("hibernate.cache.use_query_cache", false);
        p.put("hibernate.jdbc.batch_size", 0);
        p.put("hibernate.current_session_context_class", "thread");
        // p.put("hibernate.hbm2ddl.auto", "update");
        p.put("hibernate.show_sql", databaseShowSQL);

        String myDatabaseSchema = databaseSchema;
        if (databaseSchema == null || databaseSchema.trim().length() == 0) {
            // no schema set, we have to set default schema for hibernate !
            // e.g. empty schema crashes on postgres/oracle cause schema now set in springapp-servlet.xml hibernateProperties
            myDatabaseSchema = DatabaseConnectionUtils.getDefaultSchema( this.getDatabaseConnection() );
        }
        log.info( "Setting hibernate.schema: " + myDatabaseSchema );
        p.put( "hibernate.default_schema", myDatabaseSchema );

        props.setProperties(p);

        return props;
    }

    @Override
    public void initialize() {
        super.initialize();
        
        updateDatabaseDescriptor();
        String temp = config.communicationProxyUrl;
        config.communicationProxyUrl = igeClientName;
        config.writeCommunication( "conf/communication-ige.xml", igeCommunication );
        config.communicationProxyUrl = temp;
        
        Importer.main( new String[0] );

        if (Importer.importSuccess == null || !Importer.importSuccess) {
            log.error("Error during database migration. Please check out the logs.");
            System.exit(1);
        }

        // since 5.2.0 the topics datatype has to be removed from the configuration, since it is determined
        // dynamically during indexing
        try {
            Properties overrideProperties = this.config.getOverrideProperties();
            List<String> selectedProps = overrideProperties.stringPropertyNames().stream()
                    .filter(prop -> prop.startsWith("plugdescription.dataType"))
                    .collect(Collectors.toList());

            for (String name : selectedProps) {
                String datatypes = overrideProperties.getProperty(name);
                String datatypesWithoutTopics = Arrays.stream(datatypes.split(","))
                        .filter(propValue -> !propValue.equals("topics"))
                        .collect(Collectors.joining(","));
                overrideProperties.put(name, datatypesWithoutTopics);
            }

            try (OutputStream os = new FileOutputStream( getPropertyResource("config.override.properties").getFile().getAbsolutePath() )) {
                overrideProperties.store( os, "Override configuration written by the application" );
            }
        } catch (Exception e) {
            log.error("Could not filter topics from datatypes");
        }
    }

    private void updateDatabaseDescriptor() {
        try {
            File descriptorFile = getPropertyResource( "descriptor.properties" ).getFile();

            if (!descriptorFile.exists()) {
                descriptorFile.createNewFile();
            }

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
            os.flush();
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
        
        props.setProperty( "igc.enableIBusCommunication", this.igcEnableIBusCommunication + "" );
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

    @Value("${communications.ige}")
    private void setCommunication(String ibusse) {
        List<CommunicationCommandObject> list = new ArrayList<>();
        String[] split = ibusse.split( "##" );
        for (String comm : split) {
            String[] communication = comm.split( "," );
            if (communication.length == 3) {
                CommunicationCommandObject commObject = new CommunicationCommandObject();
                commObject.setBusProxyServiceUrl( communication[0] );
                commObject.setIp( communication[1] );
                commObject.setPort( Integer.valueOf( communication[2] ) );
                list.add( commObject );
            }
        }
        igeCommunication = list;
    }

}
