/*
 * **************************************************-
 * InGrid-iPlug DSC
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

var DatabaseSourceRecord = Java.type("de.ingrid.iplug.dsc.om.DatabaseSourceRecord");

if (log.isDebugEnabled()) {
	log.debug("BKG: Additional mapping of source record to lucene document: " + sourceRecord.toString());
}

if (!(sourceRecord instanceof DatabaseSourceRecord)) {
    throw new IllegalArgumentException("Record is no DatabaseRecord!");
}

// convert id to number to be used in PreparedStatement as Integer to avoid postgres error !
var objId = +sourceRecord.get("id");

var objRows = SQL.all("SELECT * FROM t01_object WHERE id=?", [+objId]);
for (i=0; i<objRows.size(); i++) {
    handleBKGUseConstraints();
}

function handleBKGUseConstraints() {
    // get the container for the select and free text field
    var bkgUseConstraintId = getAdditionalFieldFromObject(objId, null, 'bkg_useConstraints', 'id');
    if (hasValue(bkgUseConstraintId)) {

        // get value from select box
        var bkgUseConstraintSelectListItem = getAdditionalFieldFromObject(null, bkgUseConstraintId, 'bkg_useConstraints_select', 'list_item_id');

        if (log.isDebugEnabled()) {
            log.debug("BKG use constraint select field contains value: " + bkgUseConstraintSelectListItem);
        }

        // get value from free text field
        var bkgUseConstraintFreeText = getAdditionalFieldFromObject(null, bkgUseConstraintId, 'bkg_useConstraints_freeText', 'data');
        if (hasValue(bkgUseConstraintFreeText)) {
            if (log.isDebugEnabled()) {
                log.debug("BKG use constraint free text field contains value: " + bkgUseConstraintFreeText);
            }
        }

        // get value from source note field
        var bkgSourceNoteText = getAdditionalFieldFromObject(null, bkgUseConstraintId, 'bkg_useConstraints_sourceNote', 'data');
        if (hasValue(bkgSourceNoteText)) {
            if (log.isDebugEnabled()) {
                log.debug("BKG use constraint free text field contains value: " + bkgSourceNoteText);
            }
        }

        // add select value and free text to ISO depending on selection
        // if there is any value
        if (hasValue(bkgUseConstraintSelectListItem) || hasValue(bkgUseConstraintFreeText)) {
            if (hasValue(bkgUseConstraintSelectListItem)) {
                var bkgUseConstraintSelectListItemSysListValue = TRANSF.getIGCSyslistEntryName(10004, +bkgUseConstraintSelectListItem);
                if (hasValue(bkgUseConstraintSelectListItemSysListValue)) {
                    IDX.add("object_use_constraint.license_value", bkgUseConstraintSelectListItemSysListValue);
                }
            }
            if (hasValue(bkgUseConstraintFreeText)) {
                IDX.add("object_use_constraint.license_value", bkgUseConstraintFreeText)
            }
            if (hasValue(bkgSourceNoteText)) {
                IDX.add("object_use_constraint.license_value",  "Quellenvermerk: " + bkgSourceNoteText);
            }
        }
    }
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

function hasValue(val) {
    if (typeof val == "undefined") {
        return false;
    } else if (val == null) {
        return false;
    } else if (typeof val == "string" && val == "") {
        return false;
    } else if (typeof val == "object" && Object.keys(val).length === 0) {
        return false;
    } else {
      return true;
    }
}
