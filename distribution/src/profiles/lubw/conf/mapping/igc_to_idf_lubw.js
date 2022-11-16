/*
 * **************************************************-
 * InGrid-iPlug DSC
 * ==================================================
 * Copyright (C) 2014 - 2022 wemove digital solutions GmbH
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
if (javaVersion.indexOf( "1.8" ) === 0) {
	load("nashorn:mozilla_compat.js");
	CAPABILITIES = Java.type('de.ingrid.utils.capabilities.CapabilitiesUtils');
}

importPackage(Packages.org.w3c.dom);
importPackage(Packages.de.ingrid.iplug.dsc.om);

if (log.isDebugEnabled()) {
    log.debug("LUBW: Mapping source record to idf document: " + sourceRecord.toString());
}

if (!(sourceRecord instanceof DatabaseSourceRecord)) {
    throw new IllegalArgumentException("Record is no DatabaseRecord!");
}

//---------- <idf:idfMdMetadata> ----------
var objId = sourceRecord.get("id");
var environmentDescription = getAdditionalFieldFromObject(objId, null, 'environmentDescription', 'data');
if (environmentDescription) {
    var mdDataIdentification = DOM.getElement(idfDoc, "//idf:idfMdMetadata/gmd:identificationInfo/gmd:MD_DataIdentification");
    if (!mdDataIdentification) {
        var dataMetadata = DOM.getElement(idfDoc, "//idf:idfMdMetadata/gmd:identificationInfo");
        mdDataIdentification = dataMetadata.addElement("gmd:MD_DataIdentification");
    }
    mdDataIdentification.addElement("gmd:environmentDescription/gco:CharacterString").addText(environmentDescription);
}

/**
 * Get a value from an additional value with a given fieldId that belongs to an object or a parent.
 * objId or parentId must be null.
 * @param objId
 * @param parentId
 * @param fieldId
 * @param property
 * @returns
 */
function getAdditionalFieldFromObject(objId, parentId, fieldId, property) {
    var field = null;
    if (objId) {
        field = SQL.first("SELECT * FROM additional_field_data WHERE obj_id=? AND field_key=?", [objId, fieldId]);
    } else {
        field = SQL.first("SELECT * FROM additional_field_data WHERE parent_field_id=? AND field_key=?", [parentId, fieldId]);
    }
    if (hasValue(field)) {
        return field.get(property);
    } else {
        return null;
    }
}