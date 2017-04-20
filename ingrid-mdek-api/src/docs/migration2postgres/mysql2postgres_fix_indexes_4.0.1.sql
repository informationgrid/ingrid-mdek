
CREATE INDEX idxaddfield_objid ON additional_field_data (obj_id);

CREATE INDEX idxaddrcom_addrid ON address_comment (addr_id);

CREATE INDEX idxaddrn_addrid ON address_node (addr_id);
CREATE INDEX idxaddrn_addridpub ON address_node (addr_id_published);
CREATE INDEX idxaddrn_faddruuid ON address_node (fk_addr_uuid);

CREATE INDEX idxaddridxname ON full_index_addr (idx_name);
CREATE INDEX idxfullidxaddrid ON full_index_addr (addr_node_id);

CREATE INDEX idxfullobjnodedid ON full_index_obj (obj_node_id);
CREATE INDEX idxobjidxname ON full_index_obj (idx_name);

CREATE INDEX idxobjaccess_objid ON object_access (obj_id);

CREATE INDEX idxobjcom_objid ON object_comment (obj_id);

CREATE INDEX idxobjconf_objid ON object_conformity (obj_id);

CREATE INDEX idxobjdq_objid ON object_data_quality (obj_id);

CREATE INDEX idxobjformatinsp_objid ON object_format_inspire (obj_id);

CREATE INDEX idxobjn_fobjuuid ON object_node (fk_obj_uuid);
CREATE INDEX idxobjn_objid ON object_node (obj_id);
CREATE INDEX idxobjn_objidpub ON object_node (obj_id_published);

CREATE INDEX idxobjodcategory_objid ON object_open_data_category (obj_id);

CREATE INDEX idxobjref_ofromid ON object_reference (obj_from_id);

CREATE INDEX idxotypcat_objid ON object_types_catalogue (obj_id);

CREATE INDEX idxobjuse_objid ON object_use (obj_id);
CREATE INDEX idxobjuconstr_objid ON object_use_constraint (obj_id);

CREATE INDEX idxperm_addr_uuid ON permission_addr (uuid);
CREATE INDEX idxperm_obj_uuid ON permission_obj (uuid);

CREATE INDEX idxstadr_adrid ON searchterm_adr (adr_id);
CREATE INDEX idxstadr_stid ON searchterm_adr (searchterm_id);

CREATE INDEX idxtermsnsid ON searchterm_sns (sns_id);
CREATE INDEX idxstval_stsnsid ON searchterm_value (searchterm_sns_id);

CREATE INDEX idxspatialsnsid ON spatial_ref_sns (sns_id);
CREATE INDEX idxsrval_srefsnsid ON spatial_ref_value (spatial_ref_sns_id);
CREATE INDEX idxsref_objid ON spatial_reference (obj_id);
CREATE INDEX idxsref_srefid ON spatial_reference (spatial_ref_id);

CREATE INDEX idxssys_objid ON spatial_system (obj_id);

CREATE INDEX idxsyslist_lstid ON sys_list (lst_id);

CREATE INDEX idxavformat_objid ON t0110_avail_format (obj_id);

CREATE INDEX idxmediaop_objid ON t0112_media_option (obj_id);

CREATE INDEX idxdatref_objid ON t0113_dataset_reference (obj_id);

CREATE INDEX idxenvtop_objid ON t0114_env_topic (obj_id);

CREATE INDEX idxodata_objid ON t011_obj_data (obj_id);
CREATE INDEX idxodatapara_objid ON t011_obj_data_para (obj_id);

CREATE INDEX idxogeo_objid ON t011_obj_geo (obj_id);
CREATE INDEX idxogeoscal_ogeoid ON t011_obj_geo_scale (obj_geo_id);
CREATE INDEX idxogeospat_ogeoid ON t011_obj_geo_spatial_rep (obj_geo_id);
CREATE INDEX idxogeosupp_ogeoid ON t011_obj_geo_supplinfo (obj_geo_id);
CREATE INDEX idxogeosymc_ogeoid ON t011_obj_geo_symc (obj_geo_id);
CREATE INDEX idxogeovect_ogeoid ON t011_obj_geo_vector (obj_geo_id);

CREATE INDEX idxolit_objid ON t011_obj_literature (obj_id);

CREATE INDEX idxoproj_objid ON t011_obj_project (obj_id);

CREATE INDEX idxobjserv_objid ON t011_obj_serv (obj_id);
CREATE INDEX idxoserocp_oseroid ON t011_obj_serv_op_connpoint (obj_serv_op_id);
CREATE INDEX idxoserode_oseroid ON t011_obj_serv_op_depends (obj_serv_op_id);
CREATE INDEX idxoseropa_oseroid ON t011_obj_serv_op_para (obj_serv_op_id);
CREATE INDEX idxoseropl_oseroid ON t011_obj_serv_op_platform (obj_serv_op_id);
CREATE INDEX idxoseroper_oserid ON t011_obj_serv_operation (obj_serv_id);
CREATE INDEX idxosrvscal_osrvid ON t011_obj_serv_scale (obj_serv_id);
CREATE INDEX idxosertype_oserid ON t011_obj_serv_type (obj_serv_id);
CREATE INDEX idxoserurl_oserid ON t011_obj_serv_url (obj_serv_id);
CREATE INDEX idxoservers_oserid ON t011_obj_serv_version (obj_serv_id);

CREATE INDEX idxobjadr_adruuid ON t012_obj_adr (adr_uuid);
CREATE INDEX idxobjadr_objid ON t012_obj_adr (obj_id);

CREATE INDEX idxinfimpart_objid ON t014_info_impart (obj_id);

CREATE INDEX idxlegis_objid ON t015_legist (obj_id);

CREATE INDEX idxurlref_objid ON t017_url_ref (obj_id);

CREATE INDEX idx_objmeta ON t01_object (obj_metadata_id);
CREATE INDEX idxobj_catid ON t01_object (cat_id);
CREATE INDEX idxobjclass ON t01_object (obj_class);

CREATE INDEX idxcomm_adrid ON t021_communication (adr_id);

CREATE INDEX idx_addrmeta ON t02_address (addr_metadata_id);

CREATE INDEX idxcat_srefid ON t03_catalogue (spatial_ref_id);
