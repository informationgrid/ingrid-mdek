/*
 * **************************************************-
 * InGrid-iPlug DSC
 * ==================================================
 * Copyright (C) 2014 - 2024 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.2 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
package de.ingrid.mdek.job.webapp.controller;

import de.ingrid.admin.command.PlugdescriptionCommandObject;
import de.ingrid.admin.controller.AbstractController;
import de.ingrid.iplug.dsc.index.DatabaseConnection;
import de.ingrid.iplug.dsc.webapp.controller.AdminViews;
import de.ingrid.iplug.dsc.webapp.validation.DatabaseConnectionValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;

/**
 * Control the database parameter page.
 * 
 * @author joachim@wemove.com
 * 
 */
@Controller
@SessionAttributes("plugDescription")
public class DatabaseParameterController extends AbstractController {
    private final DatabaseConnectionValidator _validator;

    @Autowired
    private de.ingrid.mdek.job.Configuration igeConfig;

    @Autowired
    public DatabaseParameterController(DatabaseConnectionValidator validator) {
        _validator = validator;
    }

    @RequestMapping(value = { "/iplug-pages/welcome.html", "/iplug-pages/dbParams.html" }, method = RequestMethod.GET)
    public String getParameters(
            final ModelMap modelMap,
            @ModelAttribute("plugDescription") final PlugdescriptionCommandObject commandObject) {

        DatabaseConnection dbConfig = igeConfig.getDatabaseConnection();

        // write object into session
        modelMap.addAttribute("dbConfig", dbConfig);
        return AdminViews.DB_PARAMS;
    }

    @RequestMapping(value = "/iplug-pages/dbParams.html", method = RequestMethod.POST)
    public String post(
            @ModelAttribute("dbConfig") final DatabaseConnection commandObject,
            final BindingResult errors,
            @ModelAttribute("plugDescription") final PlugdescriptionCommandObject pdCommandObject) {

        // check if page contains any errors
        if (_validator.validateDBParams(errors).hasErrors()) {
            return AdminViews.DB_PARAMS;
        }

        // put values into plugdescription
        mapParamsToPD(commandObject, pdCommandObject);
        
        if (!commandObject.getConnectionURL().equals( igeConfig.databaseUrl )) {
            pdCommandObject.putBoolean( "needsRestart", true );
        }
        // save in config object
        igeConfig.databaseDriver = commandObject.getDataBaseDriver();
        igeConfig.databaseUrl = commandObject.getConnectionURL();
        igeConfig.databaseUsername = commandObject.getUser();
        igeConfig.databasePassword = commandObject.getPassword();
        igeConfig.databaseSchema = commandObject.getSchema();
        
        return redirect( MdekUris.EDITOR );
    }

    private void mapParamsToPD(DatabaseConnection commandObject, PlugdescriptionCommandObject pdCommandObject) {

        pdCommandObject.setConnection(commandObject);

        // add required datatypes to PD
        //pdCommandObject.addDataType("IDF_1.0");
    }

    public boolean rankSupported(String rankType, String[] types) {
        for (String type : types) {
            if (type.contains(rankType))
                return true;
        }
        return false;
    }

}
