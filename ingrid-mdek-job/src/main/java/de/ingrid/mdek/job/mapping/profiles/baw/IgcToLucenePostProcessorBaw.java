/*-
 * **************************************************-
 * InGrid mdek-job
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
package de.ingrid.mdek.job.mapping.profiles.baw;

import de.ingrid.iplug.dsc.index.mapper.IRecordMapper;
import de.ingrid.iplug.dsc.om.DatabaseSourceRecord;
import de.ingrid.iplug.dsc.om.SourceRecord;
import de.ingrid.iplug.dsc.utils.SQLUtils;
import de.ingrid.mdek.job.Configuration;
import de.ingrid.utils.ElasticDocument;
import de.ingrid.utils.index.IndexUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;

import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Order(2)
public class IgcToLucenePostProcessorBaw implements IRecordMapper {
	private static final Logger LOG = Logger.getLogger(IgcToLucenePostProcessorBaw.class);

	@Autowired
	private Configuration igeConfig;

	@Override
	public void map(SourceRecord record, ElasticDocument doc) throws Exception {
		try {
			IndexUtils idx = new IndexUtils(doc);

			Connection connection = (Connection) record.get(DatabaseSourceRecord.CONNECTION);
			SQLUtils sqlUtils = new SQLUtils(connection);
			Long objId = Long.parseLong((String) record.get("id"));

			String query = "SELECT child.sort AS sort, child.field_key AS field_key, child.data AS data FROM additional_field_data child " +
					"JOIN additional_field_data parent ON child.parent_field_id = parent.id " +
					"WHERE parent.obj_id = ? AND parent.field_key = ? " +
					"ORDER BY child.sort";

			List<Map<String, String>> rows = sqlUtils.all(query, new Object[]{objId, "lfsLinkTable"});

			String previousSort = null;
			for(Map<String, String> row: rows) {
				String sort = row.get("sort");
				String key = row.get("field_key");
				String data = row.get("data");

				if (key == null || data == null) continue;

				if (sort != null && !Objects.equals(sort, previousSort)) {
					idx.add("t017_url_ref.line", sort);
					idx.add("t017_url_ref.special_ref", "9990");
					idx.add("t017_url_ref.special_name", "Datendownload");
					previousSort = sort;
				}

				if (Objects.equals(key, "link")) {
					idx.add("t017_url_ref.url_link", igeConfig.bawLfsBaseURL + '/' + data);
				} else if (Objects.equals(key, "name")) {
					idx.add("t017_url_ref.content", data);
				} else if (Objects.equals(key, "fileFormat")) {
					idx.add("t017_url_ref.datatype", data);
				} else if (Objects.equals(key, "explanation")) {
					idx.add("t017_url_ref.descr", data);
				} else if (Objects.equals(key, "urlType")) {
					idx.add("t017_url_ref.url_type", data);
				}
			}
		} catch (Exception e) {
			LOG.error("Error mapping source record to lucene document.", e);
		}
	}
}

