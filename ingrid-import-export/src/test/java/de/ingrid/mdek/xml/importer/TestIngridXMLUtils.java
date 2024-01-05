/*
 * **************************************************-
 * ingrid-import-export
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
package de.ingrid.mdek.xml.importer;

import de.ingrid.mdek.xml.util.IngridXMLUtils;
import org.junit.jupiter.api.Test;

import javax.xml.stream.XMLStreamException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.*;

public class TestIngridXMLUtils {

    private final static String FILE_NAME = "src/test/resources/test.xml";

    @Test
    public void testGetVersionReturnsValueForValidDoc() throws XMLStreamException, IOException {
        Reader reader = new FileReader(FILE_NAME);
        String version = IngridXMLUtils.getVersion(reader);
        reader.close();
        assertNotNull(version);
    }

    @Test
    public void testGetVersionReturnsCorrectValue() throws XMLStreamException, IOException {
        Reader reader = new StringReader("<igc xmlns='http://informationgrid.eu/igc-import' exchange-format='1.0'>bla</igc>");
        String version = IngridXMLUtils.getVersion(reader);
        assertEquals("1.0", version);
    }

    @Test
    public void testGetVersionReturnsEmptyAttributeIfNotFound() throws XMLStreamException, IOException {
        Reader reader = new StringReader("<igc xmlns='http://informationgrid.eu/igc-import'>bla</igc>");
        String version = IngridXMLUtils.getVersion(reader);
        assertNull(version);
    }

    @Test
    public void testGetVersionThrowsXMLStreamException() {
        assertThrows(XMLStreamException.class, () -> {
            Reader reader = new StringReader("<invalid xml>... >");
            String version = IngridXMLUtils.getVersion(reader);
            reader.close();
        });
    }
}
