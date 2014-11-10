---
-- **************************************************-
-- ingrid-mdek-api
-- ==================================================
-- Copyright (C) 2014 wemove digital solutions GmbH
-- ==================================================
-- Licensed under the EUPL, Version 1.1 or – as soon they will be
-- approved by the European Commission - subsequent versions of the
-- EUPL (the "Licence");
-- 
-- You may not use this work except in compliance with the Licence.
-- You may obtain a copy of the Licence at:
-- 
-- http://ec.europa.eu/idabc/eupl5
-- 
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the Licence is distributed on an "AS IS" basis,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the Licence for the specific language governing permissions and
-- limitations under the Licence.
-- **************************************************#
---

-- ==========================================================
-- TEMPORAERE TABELLE UM DEN MIGRATIONSSTAND EINER DB ZU TRACKEN
-- NACH JEDER ERGAENZUNG IN DIESEM FILE SOLLTE DIE VERSION HOCHGESETZT WERDEN
CREATE TABLE `tmp_udk_migrationsstand` (
  `version` int NOT NULL default '0'
) TYPE=InnoDB;
INSERT INTO `tmp_udk_migrationsstand` VALUES (0);

-- ==========================================================

-- VERSION SPALTE HINZUFUEGEN:
-- -------------------------
ALTER TABLE t01_object ADD COLUMN version int;
UPDATE t01_object SET version = 0;

ALTER TABLE t02_address ADD COLUMN version int;
UPDATE t02_address SET version = 0;

-- typ wird type
-- -------------
ALTER TABLE t012_obj_obj CHANGE typ type int NOT NULL default '0';
ALTER TABLE t012_obj_adr CHANGE typ type int NOT NULL default '0';

-- ==========================================================
UPDATE tmp_udk_migrationsstand SET version = 1;
-- ==========================================================

ALTER TABLE t012_obj_obj ADD COLUMN version int;
UPDATE t012_obj_obj SET version = 0;

ALTER TABLE t012_obj_adr ADD COLUMN version int;
UPDATE t012_obj_adr SET version = 0;

ALTER TABLE t021_communication ADD COLUMN version int;
UPDATE t021_communication SET version = 0;

