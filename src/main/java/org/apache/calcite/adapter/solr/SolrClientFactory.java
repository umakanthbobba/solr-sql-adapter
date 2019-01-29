package org.apache.calcite.adapter.solr;

import org.apache.solr.client.solrj.SolrClient;

public abstract class SolrClientFactory {

	public abstract SolrClient getClient();
}
