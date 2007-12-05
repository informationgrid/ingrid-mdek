
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

