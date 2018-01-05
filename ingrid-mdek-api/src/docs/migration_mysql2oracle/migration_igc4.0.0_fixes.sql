
-- Changes from version 3.2.0

PROMPT ! CHANGE COLUMN DATA TYPES IGC AFTER MIGRATION VIA SQL DEVELOPER !
PROMPT --------------------------------------------------------------

PROMPT ! Change t02_address.institution from CLOB to VARCHAR2(4000 CHAR) ...
ALTER TABLE t02_address ADD institution2 VARCHAR2(4000 CHAR);
UPDATE t02_address SET institution2 = institution;
ALTER TABLE t02_address DROP COLUMN institution;
ALTER TABLE t02_address RENAME COLUMN institution2 TO institution;

PROMPT ! Change object_conformity.specification_value from CLOB to VARCHAR2(4000 CHAR) ...
ALTER TABLE object_conformity ADD specification_value2 VARCHAR2(4000 CHAR);
UPDATE object_conformity SET specification_value2 = specification_value;
ALTER TABLE object_conformity DROP COLUMN specification_value;
ALTER TABLE object_conformity RENAME COLUMN specification_value2 TO specification_value;

PROMPT ! Change object_use.terms_of_use_value from CLOB to VARCHAR2(4000 CHAR) ...
ALTER TABLE object_use ADD terms_of_use_value2 VARCHAR2(4000 CHAR);
UPDATE object_use SET terms_of_use_value2 = terms_of_use_value;
ALTER TABLE object_use DROP COLUMN terms_of_use_value;
ALTER TABLE object_use RENAME COLUMN terms_of_use_value2 TO terms_of_use_value;

PROMPT ! Change searchterm_value.term from CLOB to VARCHAR2(4000 CHAR) ...
ALTER TABLE searchterm_value ADD term2 VARCHAR2(4000 CHAR);
UPDATE searchterm_value SET term2 = term;
ALTER TABLE searchterm_value DROP COLUMN term;
ALTER TABLE searchterm_value RENAME COLUMN term2 TO term;

PROMPT ! Change searchterm_value.alternate_term from CLOB to VARCHAR2(4000 CHAR) ...
ALTER TABLE searchterm_value ADD alternate_term2 VARCHAR2(4000 CHAR);
UPDATE searchterm_value SET alternate_term2 = alternate_term;
ALTER TABLE searchterm_value DROP COLUMN alternate_term;
ALTER TABLE searchterm_value RENAME COLUMN alternate_term2 TO alternate_term;

PROMPT ! Change spatial_ref_value.name_value from CLOB to VARCHAR2(4000 CHAR) ...
ALTER TABLE spatial_ref_value ADD name_value2 VARCHAR2(4000 CHAR);
UPDATE spatial_ref_value SET name_value2 = name_value;
ALTER TABLE spatial_ref_value DROP COLUMN name_value;
ALTER TABLE spatial_ref_value RENAME COLUMN name_value2 TO name_value;

PROMPT ! Change sys_list.name from CLOB to VARCHAR2(4000 CHAR) ...
ALTER TABLE sys_list ADD name2 VARCHAR2(4000 CHAR);
UPDATE sys_list SET name2 = name;
ALTER TABLE sys_list DROP COLUMN name;
ALTER TABLE sys_list RENAME COLUMN name2 TO name;

PROMPT ! Change sys_list.description from CLOB to VARCHAR2(4000 CHAR) ...
ALTER TABLE sys_list ADD description2 VARCHAR2(4000 CHAR);
UPDATE sys_list SET description2 = description;
ALTER TABLE sys_list DROP COLUMN description;
ALTER TABLE sys_list RENAME COLUMN description2 TO description;

PROMPT ! Change t011_obj_data.base from CLOB to VARCHAR2(4000 CHAR) ...
ALTER TABLE t011_obj_data ADD base2 VARCHAR2(4000 CHAR);
UPDATE t011_obj_data SET base2 = base;
ALTER TABLE t011_obj_data DROP COLUMN base;
ALTER TABLE t011_obj_data RENAME COLUMN base2 TO base;

PROMPT ! Change t011_obj_geo_supplinfo.feature_type from CLOB to VARCHAR2(4000 CHAR) ...
ALTER TABLE t011_obj_geo_supplinfo ADD feature_type2 VARCHAR2(4000 CHAR);
UPDATE t011_obj_geo_supplinfo SET feature_type2 = feature_type;
ALTER TABLE t011_obj_geo_supplinfo DROP COLUMN feature_type;
ALTER TABLE t011_obj_geo_supplinfo RENAME COLUMN feature_type2 TO feature_type;

PROMPT ! Change t011_obj_literature.author from CLOB to VARCHAR2(4000 CHAR) ...
ALTER TABLE t011_obj_literature ADD author2 VARCHAR2(4000 CHAR);
UPDATE t011_obj_literature SET author2 = author;
ALTER TABLE t011_obj_literature DROP COLUMN author;
ALTER TABLE t011_obj_literature RENAME COLUMN author2 TO author;

PROMPT ! Change t011_obj_literature.publisher from CLOB to VARCHAR2(4000 CHAR) ...
ALTER TABLE t011_obj_literature ADD publisher2 VARCHAR2(4000 CHAR);
UPDATE t011_obj_literature SET publisher2 = publisher;
ALTER TABLE t011_obj_literature DROP COLUMN publisher;
ALTER TABLE t011_obj_literature RENAME COLUMN publisher2 TO publisher;

PROMPT ! Change t011_obj_literature.publish_in from CLOB to VARCHAR2(4000 CHAR) ...
ALTER TABLE t011_obj_literature ADD publish_in2 VARCHAR2(4000 CHAR);
UPDATE t011_obj_literature SET publish_in2 = publish_in;
ALTER TABLE t011_obj_literature DROP COLUMN publish_in;
ALTER TABLE t011_obj_literature RENAME COLUMN publish_in2 TO publish_in;

PROMPT ! Change t011_obj_literature.publish_loc from CLOB to VARCHAR2(4000 CHAR) ...
ALTER TABLE t011_obj_literature ADD publish_loc2 VARCHAR2(4000 CHAR);
UPDATE t011_obj_literature SET publish_loc2 = publish_loc;
ALTER TABLE t011_obj_literature DROP COLUMN publish_loc;
ALTER TABLE t011_obj_literature RENAME COLUMN publish_loc2 TO publish_loc;

PROMPT ! Change t011_obj_literature.loc from CLOB to VARCHAR2(4000 CHAR) ...
ALTER TABLE t011_obj_literature ADD loc2 VARCHAR2(4000 CHAR);
UPDATE t011_obj_literature SET loc2 = loc;
ALTER TABLE t011_obj_literature DROP COLUMN loc;
ALTER TABLE t011_obj_literature RENAME COLUMN loc2 TO loc;

PROMPT ! Change t011_obj_literature.doc_info from CLOB to VARCHAR2(4000 CHAR) ...
ALTER TABLE t011_obj_literature ADD doc_info2 VARCHAR2(4000 CHAR);
UPDATE t011_obj_literature SET doc_info2 = doc_info;
ALTER TABLE t011_obj_literature DROP COLUMN doc_info;
ALTER TABLE t011_obj_literature RENAME COLUMN doc_info2 TO doc_info;

PROMPT ! Change t011_obj_literature.publishing from CLOB to VARCHAR2(4000 CHAR) ...
ALTER TABLE t011_obj_literature ADD publishing2 VARCHAR2(4000 CHAR);
UPDATE t011_obj_literature SET publishing2 = publishing;
ALTER TABLE t011_obj_literature DROP COLUMN publishing;
ALTER TABLE t011_obj_literature RENAME COLUMN publishing2 TO publishing;

PROMPT ! Change t011_obj_project.leader from CLOB to VARCHAR2(4000 CHAR) ...
ALTER TABLE t011_obj_project ADD leader2 VARCHAR2(4000 CHAR);
UPDATE t011_obj_project SET leader2 = leader;
ALTER TABLE t011_obj_project DROP COLUMN leader;
ALTER TABLE t011_obj_project RENAME COLUMN leader2 TO leader;

PROMPT ! Change t011_obj_project.member from CLOB to VARCHAR2(4000 CHAR) ...
ALTER TABLE t011_obj_project ADD member2 VARCHAR2(4000 CHAR);
UPDATE t011_obj_project SET member2 = member;
ALTER TABLE t011_obj_project DROP COLUMN member;
ALTER TABLE t011_obj_project RENAME COLUMN member2 TO member;

-- Alter all URL columns to VARCHAR. CLOB does not work with "URL-Pflege" in IGE.
-- ORA-00932: inconsistent datatypes: expected - got CLOB

-- was changed manually in bb catalog to TEXT (MySQL) so was converted to CLOB
PROMPT ! Change t011_obj_serv_op_connpoint.connect_point from CLOB to VARCHAR2(4000 CHAR) ...
ALTER TABLE t011_obj_serv_op_connpoint ADD connect_point2 VARCHAR2(4000 CHAR);
UPDATE t011_obj_serv_op_connpoint SET connect_point2 = connect_point;
ALTER TABLE t011_obj_serv_op_connpoint DROP COLUMN connect_point;
ALTER TABLE t011_obj_serv_op_connpoint RENAME COLUMN connect_point2 TO connect_point;

PROMPT ! Change t017_url_ref.url_link from CLOB to VARCHAR2(4000 CHAR) ...
ALTER TABLE t017_url_ref ADD url_link2 VARCHAR2(4000 CHAR);
UPDATE t017_url_ref SET url_link2 = url_link;
ALTER TABLE t017_url_ref DROP COLUMN url_link;
ALTER TABLE t017_url_ref RENAME COLUMN url_link2 TO url_link;


-- Changes from version 4.0.0

PROMPT ! Change object_access.restriction_value from CLOB to VARCHAR2(4000 CHAR) ...
ALTER TABLE object_access ADD restriction_value2 VARCHAR2(4000 CHAR);
UPDATE object_access SET restriction_value2 = restriction_value;
ALTER TABLE object_access DROP COLUMN restriction_value;
ALTER TABLE object_access RENAME COLUMN restriction_value2 TO restriction_value;

PROMPT ! Change object_open_data_category.category_value from CLOB to VARCHAR2(4000 CHAR) ...
ALTER TABLE object_open_data_category ADD category_value2 VARCHAR2(4000 CHAR);
UPDATE object_open_data_category SET category_value2 = category_value;
ALTER TABLE object_open_data_category DROP COLUMN category_value;
ALTER TABLE object_open_data_category RENAME COLUMN category_value2 TO category_value;

PROMPT ! Change object_use_constraint.license_value from CLOB to VARCHAR2(4000 CHAR) ...
ALTER TABLE object_use_constraint ADD license_value2 VARCHAR2(4000 CHAR);
UPDATE object_use_constraint SET license_value2 = license_value;
ALTER TABLE object_use_constraint DROP COLUMN license_value;
ALTER TABLE object_use_constraint RENAME COLUMN license_value2 TO license_value;

PROMPT ! Change sys_list.data from CLOB to VARCHAR2(4000 CHAR) ...
ALTER TABLE sys_list ADD data2 VARCHAR2(4000 CHAR);
UPDATE sys_list SET data2 = data;
ALTER TABLE sys_list DROP COLUMN data;
ALTER TABLE sys_list RENAME COLUMN data2 TO data;

PROMPT ! Change t03_catalogue.atom_download_url from CLOB to VARCHAR2(4000 CHAR) ...
ALTER TABLE t03_catalogue ADD atom_download_url2 VARCHAR2(4000 CHAR);
UPDATE t03_catalogue SET atom_download_url2 = atom_download_url;
ALTER TABLE t03_catalogue DROP COLUMN atom_download_url;
ALTER TABLE t03_catalogue RENAME COLUMN atom_download_url2 TO atom_download_url;


commit;

PROMPT ! CHANGE COLUMN DATA TYPES IGC AFTER MIGRATION VIA SQL DEVELOPER !
PROMPT ! DONE !
