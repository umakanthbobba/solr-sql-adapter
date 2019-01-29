
package org.apache.calcite.adapter.solr;


import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.schema.TableFactory;
import org.apache.calcite.sql.type.SqlTypeName;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SolrTableFactory implements TableFactory<SolrTable> {
	// public constructor, per factory contract
	public SolrTableFactory() {
	}

	public SolrTable create(SchemaPlus parentSchema, String name, Map<String, Object> operands, RelDataType rowType) {
		Map<String, String> args = new HashMap<String, String>();
		for (String key : operands.keySet())
			args.put(key, operands.get(key).toString());
		Map<String, SqlTypeName> columns = null;
		try {
			columns = SolrTableConf.parseColumns(args);
		} catch (SolrSqlException e) {
			e.printStackTrace();
		}
		Map<String, String> options = args;
		Map<String, String> filledColumnMapping = SolrTableConf.parseMap(args);
		SolrClientFactory solrClientFactory = new SolrClientFactory() {

			public SolrClient getClient() {
				if (null != options.get(SolrTableConf.SOLR_ZK_HOSTS)) {
					ArrayList<String> zkHosts = new ArrayList<String>(Arrays.asList(options.get(SolrTableConf.SOLR_ZK_HOSTS).split(",")));
					final CloudSolrClient csc = new CloudSolrClient.Builder(zkHosts, Optional.of("/solr")).build();
					csc.setDefaultCollection(options.get("solrCollection"));
					return csc;
				} else {
                    String solrServerURL = (String) options.get(SolrTableConf.SOLR_SERVER_URL);
					HttpSolrClient.Builder builder = new HttpSolrClient.Builder();
					builder.withBaseSolrUrl(solrServerURL);

					final HttpSolrClient httpSolrClient = builder.build();
					return httpSolrClient;
				}

			}
		};
		return new SolrTable(solrClientFactory, columns, filledColumnMapping, options);
	}
	
	
}

// End SolrTableFactory.java
