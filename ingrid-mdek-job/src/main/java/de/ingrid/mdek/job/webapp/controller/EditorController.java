/*
 * **************************************************-
 * ingrid-base-webapp
 * ==================================================
 * Copyright (C) 2014 - 2018 wemove digital solutions GmbH
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
package de.ingrid.mdek.job.webapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;

import de.ingrid.admin.command.PlugdescriptionCommandObject;
import de.ingrid.admin.controller.AbstractController;
import de.ingrid.mdek.MdekServer;
import de.ingrid.mdek.job.Configuration;
import de.ingrid.mdek.job.webapp.object.Editor;
import de.ingrid.mdek.job.webapp.validation.EditorValidator;

@Controller
@SessionAttributes("plugDescription")
public class EditorController extends AbstractController {

    private final EditorValidator _validator;

    @Autowired
    private Configuration igeConfig;

    @Autowired
    public EditorController(final EditorValidator validator) {
        _validator = validator;
    }

    @RequestMapping(value = { MdekUris.EDITOR }, method = RequestMethod.GET)
    public String getEditor(final ModelMap modelMap,
            @ModelAttribute("plugDescription") final PlugdescriptionCommandObject pdCommandObject) {
        
        Editor e = new Editor();
        // check if the forced parameter (for ranking) was set before
        e.setIgcEnableIBusCommunication(igeConfig.igcEnableIBusCommunication);
        
        // write object into model
        modelMap.addAttribute("editorConfig", e);
        
        return MdekViews.EDITOR;
    }

    @RequestMapping(value = MdekUris.EDITOR, method = RequestMethod.POST)
    public String postEditor(@ModelAttribute("plugDescription") final PlugdescriptionCommandObject pdCommandObject, 
            final BindingResult errors,
            @ModelAttribute("editorConfig") final Editor commandObject) {
        if (_validator.validate(errors).hasErrors()) {
            return MdekViews.EDITOR;
        }
        
        if(igeConfig.igcEnableIBusCommunication != commandObject.getIgcEnableIBusCommunication()){
            pdCommandObject.putBoolean( "needsRestart", true );
        }
        igeConfig.igcEnableIBusCommunication = commandObject.getIgcEnableIBusCommunication();
        
        return MdekViews.SAVE;
    }
}
