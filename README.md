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
