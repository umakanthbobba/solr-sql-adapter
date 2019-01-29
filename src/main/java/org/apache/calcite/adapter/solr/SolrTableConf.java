
package org.apache.calcite.adapter.solr;

import java.util.HashMap;
import java.util.Map;

import org.apache.calcite.sql.type.SqlTypeName;

public class SolrTableConf {

	public static String PAGE_SIZE = "pageSize";
	public static String COULMNS = "columns";
	public static String COLUMN_MAPPING = "columnMapping";
	public static String SOLR_ZK_HOSTS = "solrZkHosts";
	public static String SOLR_SERVER_URL = "solrServerURL";

	public static SqlTypeName toSqlTypeName(String typeName) throws SolrSqlException {
		SqlTypeName sqlType = SqlTypeName.get(typeName.toUpperCase());
		if (sqlType == null) {
			throw new SolrSqlException("SqlTypeName cannot be found : " + typeName);
		}

		return sqlType;
	}

	public String parseString(Map<String, String> args, String columnName, String defaultValue) {
		return args.getOrDefault(columnName, defaultValue);
	}

	public static int parseInt(Map<String, String> args, String columnName, String defaultValue) {
		return Integer.parseInt(args.getOrDefault(columnName, defaultValue));
	}

	public static Map<String, SqlTypeName> parseColumns(Map<String, String> args) throws SolrSqlException {
		String val = args.get(COULMNS);
		Map<String, SqlTypeName> columns = new HashMap<String, SqlTypeName>();
		String[] cols = val.split(",");
		for (String col : cols) {
			String split[] = col.split(" ");
			columns.put(split[0], toSqlTypeName(split[1]));
		}
		return columns;
	}

	public static Map<String, String> parseMap(Map<String, String> args) {
		String val = args.get(COLUMN_MAPPING);
		String[] cols = val.split(",");
		Map<String, String> columnsMap = new HashMap<String, String>();
		for (String col : cols) {
			String split[] = col.split(" ");
			columnsMap.put(split[0], split[1]);
		}

		return columnsMap;
	}
}
