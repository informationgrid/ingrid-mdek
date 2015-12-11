/*
 * **************************************************-
 * InGrid mdek-job
 * ==================================================
 * Copyright (C) 2014 - 2015 wemove digital solutions GmbH
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
package de.ingrid.mdek.job.processors;

import org.springframework.stereotype.Service;

import de.ingrid.utils.processor.IPreProcessor;
import de.ingrid.utils.query.IngridQuery;

@Service
public class IndexTypePreProcessor implements IPreProcessor {
    
    enum Type { OBJECT, ADDRESS, ALL };

    @Override
    public void process(IngridQuery query) throws Exception {
        System.out.print( query.getPositiveDataTypes() );
        String[] types = query.getPositiveDataTypes();
        Type selectedType = Type.OBJECT;
        
        // look for an address datatype -> only look for addresses
        for (String type : types) {
            if ("address".equals( type ) || "dsc_ecs_address".equals( type )) {
                selectedType = Type.ADDRESS;
                break;
            }
        }
        
        if (types.length == 0) {
            selectedType = Type.ALL;
        } else if(selectedType == Type.ADDRESS) {
            // also check if other datatypes for objects are contained, so that we search in all indices
            for (String type : types) {
                if ("default".equals( type ) || "dsc_ecs".equals( type ) || "fis".equals( type ) || "metadata".equals( type ) || "topics".equals( type )) {
                    selectedType = Type.ALL;
                    break;
                }
            }
        }
        
        // if no datatype is given
        if (Type.OBJECT == selectedType) {
            query.setArray( "searchInInstances", new String[] { "object" } );
        } else if (Type.ADDRESS == selectedType) {
            query.setArray( "searchInInstances", new String[] { "address" } );
        } else {
            // do nothing and search in all indices
        }
        
    }

}
