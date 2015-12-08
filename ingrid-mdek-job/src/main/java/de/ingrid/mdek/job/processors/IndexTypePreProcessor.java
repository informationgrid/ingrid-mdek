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
    
    enum Type { OBJECT, ADDRESS };

    @Override
    public void process(IngridQuery query) throws Exception {
        System.out.print( query.getPositiveDataTypes() );
        String[] types = query.getPositiveDataTypes();
        Type t = Type.OBJECT;
        for (String type : types) {
            if ("address".equals( type ) || "dsc_ecs_address".equals( type )) {
                t = Type.ADDRESS;
                break;
            }
        }
        if (Type.OBJECT == t) {
            query.setArray( "searchInInstances", new String[] { "object" } );
        } else {
            query.setArray( "searchInInstances", new String[] { "address" } );
        }
        
    }

}
