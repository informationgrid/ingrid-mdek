/*-
 * **************************************************-
 * InGrid mdek-job
 * ==================================================
 * Copyright (C) 2014 - 2023 wemove digital solutions GmbH
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
package de.ingrid.mdek.job.mapping.profiles.baw;

import java.util.regex.Pattern;

public class BawConstants {
    static final int BAW_DIMENSIONALITY_CODELIST_ID = 3950000;
    static final int BAW_MODEL_METHOD_CODELIST_ID = 3950001;
    static final int BAW_HIERARCHY_LEVEL_NAME_CODELIST_ID = 3950002;
    static final int BAW_MODEL_TYPE_CODELIST_ID = 3950003;
    static final int BAW_SIMULATION_PARAMETER_TYPE_CODELIST_ID = 3950004;
    static final int BAW_KEYWORD_CATALOGUE_CODELIST_ID = 3950005;
    static final int VV_1103_CODELIST_ID = 3950010;

    static final String BAW_MODEL_THESAURUS_TITLE_PREFIX = "de.baw.codelist.model.";

    static final String BAW_KEYWORD_CATALOGUE_TITLE = "BAW-Schlagwortkatalog";
    static final String BAW_KEYWORD_CATALOGUE_DATE = "2012-01-01";

    static final String BAW_DEFAULT_KEYWORD_TYPE = "discipline";
    static final String BAW_MODEL_THESAURUS_DATE = "2017-01-17";
    static final String BAW_DEFAULT_THESAURUS_DATE_TYPE = "publication";

    public static final String VV_WSV_1103_TITLE = "VV-WSV 1103";
    static final String VV_WSV_1103_DATE = "2019-05-29";
    static final String VV_WSV_1103_DATE_TYPE = "publication";

    static final String VALUE_TYPE_DISCRETE_NUMERIC = "DISCRETE_NUMERIC";
    static final String VALUE_TYPE_DISCRETE_STRING = "DISCRETE_STRING";
    static final String VALUE_TYPE_RANGE_NUMERIC = "RANGE_NUMERIC";

    /**
     * [^0-9]*: any character, as long as it isn't a number
     * ([0-9]{4}): First capture group with exactly 4 digits
     * (-[0-9.]+)?: Second capture group starting with a hyphen and followed by a number (int or float). The whole capture group is optional (Question mark at the end)
     * ditto
     * .*: any trailing character
     */
    public static final Pattern BWASTR_PATTERN = Pattern.compile("[^0-9]*([0-9]{4})(-[0-9.]+)?(-[0-9.]+)?.*");
}

