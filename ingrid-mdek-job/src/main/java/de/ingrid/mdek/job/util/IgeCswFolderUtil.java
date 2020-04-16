/*-
 * **************************************************-
 * InGrid mdek-job
 * ==================================================
 * Copyright (C) 2014 - 2020 wemove digital solutions GmbH
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
package de.ingrid.mdek.job.util;

import de.ingrid.mdek.MdekKeys;
import de.ingrid.mdek.services.catalog.MdekObjectService;
import de.ingrid.mdek.services.persistence.db.DaoFactory;
import de.ingrid.mdek.services.security.IPermissionService;
import de.ingrid.utils.IngridDocument;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class IgeCswFolderUtil {
    private final static Logger LOG = Logger.getLogger(IgeCswFolderUtil.class);

    private static final String CSW_IMPORT_FOLDER_NAME = "CSW Import";

    private DaoFactory daoFactory;
    private IPermissionService permissionService;

    @Autowired
    public IgeCswFolderUtil(DaoFactory daoFactory, IPermissionService permissionService) {
        this.daoFactory = daoFactory;
        this.permissionService = permissionService;
    }

    /**
     * Creates a folder with the given name under the "/Objects/CSW Import"
     * folder and returns the UUID of the newly created folder as a string. The
     * "CSW Import" folder is created, if it doesn't already exist. If a folder
     * with the given name already exists at the above mentioned location, then
     * the folder creation is a noop and the UUID of the eixsting folder is
     * returned.
     *
     * @param name name of the folder to create under "Objects/CSW Import"
     * @param userUuid UUID of the catalogue user to use for the folder creation
     * @return the UUID of the newly created folder or the already existing folder, if any
     */
    public String getParentUuidForCswTImportFolder(String name, String userUuid) {
        MdekObjectService objectService = MdekObjectService.getInstance(daoFactory, permissionService);
        // Create the CSW Import parent
        LOG.debug("Creating CSW-T import parent-folder: " + CSW_IMPORT_FOLDER_NAME);
        IngridDocument cswImportFolderDoc = createFolderImportDocument(CSW_IMPORT_FOLDER_NAME, userUuid);
        objectService.storeWorkingCopy(cswImportFolderDoc, userUuid, true);

        String cswImportFolderUuid = cswImportFolderDoc.getString(MdekKeys.UUID);

        // Store the folder under the CSW import parent
        LOG.debug("Creating CSW-T import child-folder: " + CSW_IMPORT_FOLDER_NAME);
        IngridDocument folderImportDoc = createFolderImportDocument(name, userUuid, cswImportFolderUuid);
        objectService.storeWorkingCopy(folderImportDoc, userUuid, true);

        return folderImportDoc.getString(MdekKeys.UUID);
    }

    private IngridDocument createFolderImportDocument(String folderName, String userUuid) {
        return createFolderImportDocument(folderName, userUuid, null);
    }

    // Emulates the creation of a folder in the editor. Parent UUID is set only if given parentUuid is not null
    private IngridDocument createFolderImportDocument(String folderName, String userUuid, String parentUuid) {
        UUID folderUuid = UUID.nameUUIDFromBytes(folderName.getBytes(StandardCharsets.UTF_8));

        IngridDocument responsibleUser = ingridDocumentWithSingleMapping(
                MdekKeys.RESPONSIBLE_USER,
                ingridDocumentWithSingleMapping(MdekKeys.UUID, userUuid));

        List emptyList = new ArrayList();

        List dataLanguageList = new ArrayList();
        dataLanguageList.add(ingridDocumentWithSingleMapping(MdekKeys.DATA_LANGUAGE_CODE, 150));

        IngridDocument document = new IngridDocument();
        document.put(MdekKeys.PUBLICATION_CONDITION, 1);
        document.put(MdekKeys.METADATA_LANGUAGE_CODE, 150);
        document.put(MdekKeys.REQUESTINFO_REFETCH_ENTITY, true);
        document.put(MdekKeys.USER_ID, userUuid);
        document.put(MdekKeys.IS_ADV_COMPATIBLE, "N");
        document.put(MdekKeys.DATA_LANGUAGE_LIST, dataLanguageList);
        document.put(MdekKeys.USE_LIST, emptyList);
        document.put(MdekKeys.TITLE, folderName);
        document.put(MdekKeys.UUID, folderUuid.toString());
        document.put(MdekKeys.WORK_STATE, "B");
        document.put(MdekKeys.RESPONSIBLE_USER, responsibleUser);
        document.put(MdekKeys.IS_CATALOG_DATA, "N");
        document.put(MdekKeys.LOCATIONS, emptyList);
        document.put(MdekKeys.ADDITIONAL_FIELDS, emptyList);
        document.put(MdekKeys.CLASS, 1000);

        if (parentUuid != null) {
            document.put(MdekKeys.PARENT_UUID, parentUuid);
        }
        return document;
    }

    private IngridDocument ingridDocumentWithSingleMapping(String key, Object value) {
        IngridDocument doc = new IngridDocument();
        doc.put(key, value);
        return doc;
    }

}

