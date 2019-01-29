# solr-sql-adapter
Java adapter for solr sql interface

solr-sql-adapter provides sql interface for solr cloud(http://lucene.apache.org/solr/), by which developers can operate on solr cloud via JDBC protocols.

<img src="https://github.com/umakanthbobba/solr-sql-adapter/blob/master/docs/Arch.JPG" width=500/>

# JDBC client code

example:

	Properties info = new Properties();
	info.setProperty("lex", "JAVA");
	Connection connection = DriverManager.getConnection(
			"jdbc:calcite:model=src/java/test/sample.json", info);

	Statement statement = connection.createStatement();
	String sql = "select * from docs where age > 35";
	ResultSet resultSet = statement.executeQuery(sql);
	
this illustrates how to connect to a solr 'database' in JDBC client manners, the schema of 'database' is defined in file 'src/java/test/sample.json'.
# table definition

the file 'src/java/test/sample.json' shows an example schema definition as below:



{ 
version: '1.0', 
defaultSchema: 'solr', 
schemas: [
{ 
name: 'solr', 
tables: [ 
{ 
name: 'docs', 
type: 'custom', factory: 'org.apache.calcite.adapter.solr.SolrTableFactory', 
operand: { 
solrServerURL: 'http://1.1.1.1:8380/solr/sample', //sample url 
solrCollection: 'intellego', 
//solrZkHosts: '10.0.0.0:8380', 
columns:'id integer,age varchar', 
columnMapping: 'id id,age age'
}
} 
] 
} 
] 
}



this defines a custom table named 'docs', several arguments can be defined in the operand field:

* solrServerURL: solr server url, e.g. 'http://1.1.1.1:8983/solr/collection1'
* solrCollection: collection name, e.g. 'collection1'
* solrZkHosts: zookeeper hosts employed by solr cloud, e.g. '1.1.1.1:9983'
* columns: comma seperated column definitions, each column is describled in format 'column_name column_type_name', e.g. 'id integer,  age varchar'
* columnMapping: comma seperated column mappings, each column mapping is describled in format 'columnName field_name_in_solr_document', e.g. 'age age'
* pageSize: solr-sql-adapter does not retrieve all results on querying, for example, it only retrieves first 50 results, if the sql engine requests for more, it retrieves for next 50 results. pageSize defines the size of each query, default value is 50.
