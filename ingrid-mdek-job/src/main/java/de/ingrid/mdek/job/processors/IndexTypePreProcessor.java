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
