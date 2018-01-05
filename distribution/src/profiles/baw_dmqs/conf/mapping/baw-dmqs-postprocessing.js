/*
 * **************************************************-
 * InGrid-iPlug DSC
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
if (javaVersion.indexOf( "1.8" ) === 0) {
	load("nashorn:mozilla_compat.js");
}

importPackage(Packages.org.apache.lucene.document);
importPackage(Packages.de.ingrid.iplug.dsc.om);
importPackage(Packages.de.ingrid.geo.utils.transformation);

var BwstrLocUtil = Java.type("de.ingrid.iplug.dsc.utils.BwstrLocUtil");
var BWST_LOC_TOOL = new BwstrLocUtil();


if (log.isDebugEnabled()) {
	log.debug("Mapping source record to lucene document: " + sourceRecord.toString());
}

if (!(sourceRecord instanceof DatabaseSourceRecord)) {
    throw new IllegalArgumentException("Record is no DatabaseRecord!");
}

var doc = IDX.getDocument();
var bwstrIdAndKm = doc.get('location');

log.debug("Found bwstrIdAndKm: " + bwstrIdAndKm);

if (BWST_LOC_TOOL.isBwstrIdAndKm(bwstrIdAndKm)) {
	var parts = BWST_LOC_TOOL.parseCenterSectionFromBwstrIdAndKm(bwstrIdAndKm);
	var parsedResponse = BWST_LOC_TOOL.parse(BWST_LOC_TOOL.getResponse(parts[0], parts[1], parts[2]));
	var center = BWST_LOC_TOOL.getCenter(parsedResponse);
	if (center && center.length==2) {
		IDX.addNumeric("bwstr-center-lon", center[0]);
		IDX.addNumeric("bwstr-center-lat", center[1]);
	}
	var locNames = BWST_LOC_TOOL.getLocationNames(parsedResponse);
	if (locNames && locNames.length==2) {
		IDX.add("bwstr-bwastr_name", locNames[0]);
		IDX.add("bwstr-strecken_name", locNames[1]);
	}
}
