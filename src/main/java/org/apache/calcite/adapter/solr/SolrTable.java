
package org.apache.calcite.adapter.solr;

import org.apache.calcite.DataContext;
import org.apache.calcite.linq4j.Enumerable;
import org.apache.calcite.linq4j.Linq4j;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.rex.RexNode;
import org.apache.calcite.schema.FilterableTable;
import org.apache.calcite.schema.ScannableTable;
import org.apache.calcite.schema.impl.AbstractTable;
import org.apache.calcite.sql.type.SqlTypeName;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

public  class SolrTable extends AbstractTable implements ScannableTable, FilterableTable {

	private final SolrClientFactory solrClientFactory;
	private final int pageSize;
	private final Map<String, SqlTypeName> columns;
	private final Map<String, String> columnMapping;

	public int pageSize() {
		return this.pageSize;
	}

	public SolrTable(SolrClientFactory solrClientFactory, Map<String, SqlTypeName> columns, Map<String, String> columnMapping, Map<String, String> options) {
		this.solrClientFactory = solrClientFactory;
		this.columns = columns;
		this.columnMapping = columnMapping;
		this.pageSize = SolrTableConf.parseInt(options, SolrTableConf.PAGE_SIZE, "50");
	}

	public Enumerable<Object[]> scan(DataContext root) {
		final SolrQuery solrQuery = new SolrQuery();
		solrQuery.setQuery("*:*");
		return Linq4j.asEnumerable(new SolrQueryResults(this.solrClientFactory, solrQuery, pageSize()));
	}

	public Enumerable<Object[]> scan(DataContext root, List<RexNode> filters) {
		SolrQuery solrQuery = null;
		
		try {
			solrQuery = buildSolrQuery(filters);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Linq4j.asEnumerable(new SolrQueryResults(this.solrClientFactory, solrQuery, pageSize()));
	}

	public SolrQuery buildEmptySolrQuery() {
		SolrQuery solrQuery = new SolrQuery();
		return solrQuery;
	}

	public SolrQuery buildSolrQuery(List<RexNode> filters) throws Exception {
		SolrQuery solrQuery = new SolrQuery();
		if (filters.isEmpty())
			solrQuery.setQuery("*:*");
		else {
			SqlFilter2SolrFilterTranslator sqlFilter = new SqlFilter2SolrFilterTranslator(columnMapping.values().toArray(new String[0]));
			String query = sqlFilter.translate(filters.get(0)).toSolrQueryString();
			solrQuery.setQuery(query);
		}
		return solrQuery;
	}

	public RelDataType getRowType(final RelDataTypeFactory typeFactory) {
		List<String> colNameList = new ArrayList<String>(columns.keySet());
		List<RelDataType> typeList = new ArrayList<RelDataType>();
		for (String colName : colNameList) {
			typeList.add(typeFactory.createSqlType(columns.get(colName)));
		}
		return typeFactory.createStructType(typeList, colNameList);

	}

	public class SolrQueryResultsIterator implements Iterator<Object[]> {
		private int startOfCurrentPage;
		private Iterator<Object[]> rowIteratorWithinCurrentPage;
		private long totalCountOfRows;
		private final SolrQuery mySolrQuery;
		private SolrClientFactory solrClientFactory;

		public SolrQuery mySolrQuery() {
			return this.mySolrQuery;
		}

		public SolrQueryResultsIterator(SolrClientFactory solrClientFactory, SolrQuery solrQuery, int pageSize) {
			this.startOfCurrentPage = 0;
			this.rowIteratorWithinCurrentPage = null;
			this.totalCountOfRows = -1L;
			this.solrClientFactory = solrClientFactory;
			this.mySolrQuery = solrQuery.getCopy();
			try {
				readNextPage();
			} catch (SolrSqlException | ParseException | SolrServerException | IOException e) {
				e.printStackTrace();
			}
		}

		public void SetStartOfCurrentPage(int page) {
			this.startOfCurrentPage = page;
		}

		public void setTotalCountOfRows(long count) {
			this.totalCountOfRows = count;
		}

		public void SetRowIteratorWithinCurrentPage(Iterator<Object[]> iterator) {
			this.rowIteratorWithinCurrentPage = iterator;
		}

		public Object[] doc2Row(SolrDocument doc) throws SolrSqlException, ParseException {
			ArrayList<Object> row = new ArrayList<Object>();

			for (String colName : columns.keySet())
				row.add(fieldValue2ColumnValue(doc, colName, columns.get(colName)));

			return (Object[]) row.toArray();
		}

		public Object fieldValue2ColumnValue(SolrDocument doc, String fieldName, SqlTypeName targetType) throws SolrSqlException, ParseException {
			Object val = doc.getFieldValue(fieldName);
			if (null == val)
				return null;
			if (val instanceof String && targetType == SqlTypeName.CHAR)
				return val;
			if (val instanceof String && targetType == SqlTypeName.VARCHAR)
				return val;
			if (val instanceof Integer && targetType == SqlTypeName.INTEGER)
				return val;
			if (val instanceof Long && targetType == SqlTypeName.BIGINT)
				return val;
			if (val instanceof Float && targetType == SqlTypeName.FLOAT)
				return val;
			if (val instanceof Double && targetType == SqlTypeName.DOUBLE)
				return val;
			if (val instanceof Date && targetType == SqlTypeName.DATE)
				return val;
			if (targetType == SqlTypeName.CHAR)
				return val.toString();
			if (targetType == SqlTypeName.VARCHAR)
				return val.toString();
			if (targetType == SqlTypeName.INTEGER)
				return Integer.valueOf(val.toString());
			if (targetType == SqlTypeName.BIGINT)
				return Long.valueOf(val.toString());
			if (targetType == SqlTypeName.DATE)
				  return new SimpleDateFormat("yyyy-mm-dd").parse(val.toString());
			throw new SolrSqlException("unexpected value: " + val + "  , type " + targetType + " required");

		}

		public boolean readNextPage() throws SolrSqlException, ParseException, SolrServerException, IOException {
			if (totalCountOfRows < 0L || startOfCurrentPage < totalCountOfRows) {
				mySolrQuery().set("start", startOfCurrentPage);
				mySolrQuery().set("rows", pageSize);
				mySolrQuery().setFields(columns.keySet().toArray(new String[0])); //get only the columns defined in json schema
				SetStartOfCurrentPage(startOfCurrentPage + pageSize);
				QueryResponse rsp = solrClientFactory.getClient().query(mySolrQuery());
				SolrDocumentList docs = rsp.getResults();
				setTotalCountOfRows(docs.getNumFound());

				ArrayList<Object[]> rows = new ArrayList<Object[]>();
				for (SolrDocument doc : docs) {
					rows.add(doc2Row(doc));
				}
				SetRowIteratorWithinCurrentPage(rows.iterator());
				return true;
			} else
				return false;
		}

		public boolean hasNext() {
			return (rowIteratorWithinCurrentPage.hasNext()) || (startOfCurrentPage < totalCountOfRows);
		}

		public Object[] next() {

			try {
				if ((rowIteratorWithinCurrentPage.hasNext()) || (readNextPage())) {
					return (Object[]) rowIteratorWithinCurrentPage.next();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			throw new NoSuchElementException();
		}

	}

	public class SolrQueryResults implements Iterable<Object[]> {
		SolrClientFactory solrClientFactory;
		SolrQuery solrQuery;
		int pageSize;

		public SolrQueryResults(SolrClientFactory solrClientFactory, SolrQuery solrQuery, int pageSize) {
			this.solrClientFactory = solrClientFactory;
			this.solrQuery = solrQuery;
			this.pageSize = pageSize;
		}

		public SolrTable.SolrQueryResultsIterator iterator() {
			return new SolrTable.SolrQueryResultsIterator(this.solrClientFactory, solrQuery, this.pageSize);
		}

	}

}
