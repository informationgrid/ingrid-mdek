-- fixes für SL Kataloge nach der Migration zu MySQL
-- Schema stimmt nicht mit 4.2.0 überein, obwohl Version 4.2.0_a ?

ALTER TABLE object_access DROP COLUMN terms_of_use;

ALTER TABLE idc_user DROP COLUMN idc_group_id;
