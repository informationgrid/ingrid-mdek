/*
 * **************************************************-
 * Ingrid Portal MDEK Application
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
/**
 * CSW 2.0.2 AP ISO 1.0 import script. This script translates an input xml from
 * the CSW 2.0.2 format structure into a IGC
 * import format structure.
 *
 * It uses a template that provides a basic IGC import format structure.
 *
 * If the input document is invalid an Exception will be raised.
 *
 *
 * The following global variable are passed from the application:
 *
 * @param source A org.w3c.dom.Document instance, that defines the input
 * @param target A org.w3c.dom.Document instance, that defines the output, based on the IGC import format template.
 * @param protocol A Protocol instance to add UI protocol messages.
 * @param codeListService An instance of MdekCatalogService for accessing the codelist repository.
 * @param javaVersion A String with the java version.
 * @param SQL An instance of de.ingrid.iplug.dsc.utils.SQLUtils
 * @param XPATH Utils for XPath
 * @param TRANSF An instance of de.ingrid.iplug.dsc.utils.TransformationUtils
 * @param DOM An instance of de.ingrid.iplug.dsc.utils.DOMUtils
 * @param log A Log instance
 *
 *
 * Example to set debug message for protocol:
 * if(protocol.isDebugEnabled()){
 *		protocol.addMessage(protocol.getCurrentFilename() + ": Debug message");
 * }
 */
if (javaVersion.indexOf( "1.8" ) === 0) {
    load("nashorn:mozilla_compat.js");
    var ProtocolHandler = Java.type("de.ingrid.mdek.job.protocol.ProtocolHandler");
}

importPackage(Packages.de.ingrid.utils.udk);
importPackage(Packages.org.w3c.dom);
importClass(Packages.de.ingrid.utils.xml.XMLUtils);
importClass(Packages.de.ingrid.mdek.job.protocol.ProtocolHandler);

var DEBUG = 1;
var INFO = 2;
var WARN = 3;
var ERROR = 4;

// POST processing

// ------------------------

//import resourceFormat
var resourceFormatNodes = XPATH.getNodeList(source, "//gmd:resourceFormat/gmd:MD_Format");
for (var i=0; i<resourceFormatNodes.getLength(); i++ ) {
    var resourceFormatNode = resourceFormatNodes.item(i);
    var targetEl = target.getDocumentElement();
    var additionalValues = XPATH.createElementFromXPath(targetEl, "/igc/data-sources/data-source/data-source-instance/general/general-additional-values");

    var records = [
              {"key":"resourceFormatName", "value":XPATH.getString(resourceFormatNode, "gmd:name/gco:CharacterString")},
              {"key":"resourceFormatVersion", "value":XPATH.getString(resourceFormatNode, "gmd:version/gco:CharacterString")},
            ];

    var record;
    for (record in records) {
	    var additionalValue = additionalValues.appendChild(targetEl.getOwnerDocument().createElement("general-additional-value"));
	    XMLUtils.createOrReplaceAttribute(additionalValue, "line", (i+1));
	    XMLUtils.createOrReplaceTextNode(XPATH.createElementFromXPath(additionalValue, "field-key"), records[record].key);
	    var fieldData = XPATH.createElementFromXPath(additionalValue, "field-data");
	    XMLUtils.createOrReplaceAttribute(fieldData, "id", records[record].id ? records[record].id : "-1");
	    XMLUtils.createOrReplaceTextNode(fieldData, records[record].value);
	    XMLUtils.createOrReplaceTextNode(XPATH.createElementFromXPath(additionalValue, "field-key-parent"), "resourceFormatTable");
    }
}


// change address import:
//  * remove institutions if they have no email address
//  * in addresses that hold references to that address, remove the reference to the institution and change type to "Freie Adresse"
var unitsWithoutEmail = XPATH.getNodeList(target, "//addresses/address/address-instance[not(./communication/communication-medium/@id=3) and ./type-of-address/@id=0]");
for (var i=0; i<unitsWithoutEmail && unitsWithoutEmail.getLength(); i++ ) {
    var unitsWithoutEmailNode = unitsWithoutEmail.item(i);
    var unitsWithoutEmailsUUID = XPATH.getString(unitsWithoutEmailNode, "./address-identifier");
    // get all referencing addresses
	var targetEl = target.getDocumentElement();
    var referencingAdresses = XPATH.getNodeList(targetEl, "//addresses/address/address-instance[./parent-address/address-identifier = '" + unitsWithoutEmailsUUID + "']");
    for (var j=0; j<referencingAdresses.getLength(); j++ ) {
        var referencingAdressesNode = referencingAdresses.item(j);
        // change type of address to "Freie Adresse"
        XMLUtils.createOrReplaceAttribute(XPATH.getNode(referencingAdressesNode, "./type-of-address"), "id", "3");
        // remove reference to institution
        XPATH.removeElementAtXPath(referencingAdressesNode, "./parent-address");
    }
    // remove the institution
    XMLUtils.remove( unitsWithoutEmailNode )
}

/*
// set address type of "gmd:contact" adresses to pointOfContactMd
var contactMdNodes = XPATH.getNodeList(source, "//gmd:contact/gmd:CI_ResponsibleParty");
for (var i=0; i<contactMdNodes.getLength(); i++) {
    var contactMdNode = contactMdNodes.item(i);
    var ContactMdUUID = createUUIDFromAddress(contactMdNode);
	var targetEl = target.getDocumentElement();
    var relatedAddresses = XPATH.getNodeList(targetEl, "//related-address[./type-of-relation/@entry-id=7 and address-identifier='"+ContactMdUUID+"']");
    for (var j=0; j<relatedAddresses.getLength(); j++) {
        var relatedAddressNode = relatedAddresses.item(j);
        XMLUtils.createOrReplaceAttribute(XPATH.getNode(relatedAddressNode, "./type-of-relation"), "entry-id", "12");
        XMLUtils.createOrReplaceTextNode(XPATH.getNode(relatedAddressNode, "./type-of-relation"), "pointOfContactMd");
    }
}
*/

var targetEl = target.getDocumentElement();
var geoLocationNodesNodes = XPATH.getNodeList(targetEl, "//spatial-domain/geo-location");
if (geoLocationNodesNodes.getLength() == 2 
		&& XPATH.nodeExists(targetEl, "(//spatial-domain/geo-location)[2]/bounding-coordinates")
		&& !XPATH.nodeExists(targetEl, "(//spatial-domain/geo-location)[1]/bounding-coordinates")) {
	var secondLocationName = XPATH.getString(targetEl, "(//spatial-domain/geo-location)[2]/uncontrolled-location/location-name");
	if ( secondLocationName == 'Raumbezug des Datensatzes' ) {
		var boundingBoxNode = XPATH.getNode(targetEl, "//spatial-domain/geo-location/bounding-coordinates");
		XMLUtils.insertAfter(boundingBoxNode, XPATH.getNode(targetEl, "(//spatial-domain/geo-location)[1]/uncontrolled-location"))
		XPATH.removeElementAtXPath(targetEl, "(//spatial-domain/geo-location)[2]");
	}
}


