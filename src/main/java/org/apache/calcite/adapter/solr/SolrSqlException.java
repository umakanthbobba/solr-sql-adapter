package org.apache.calcite.adapter.solr;

import java.sql.SQLException;

@SuppressWarnings("serial")
public class SolrSqlException extends SQLException {

	public SolrSqlException(String msg, Throwable e) {
		super(msg, e);
	}

	public SolrSqlException(String msg) {
		this(msg, null);
	}
}
