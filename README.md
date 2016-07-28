# wspls - PL/SQL over HTTP/JSON
Call Oracle stored procedures or functions over HTTP as webservice, returning JSON of the IN/OUT parameters or sys_refcursor. Use HTTP Session parameters within your Oracle packages plsql.

Simple web application java: wars/wspls.war for:
	
	- Apache Tomcat 8.x
	- JBoss 7.x or WildFly 9.x
	
# Usage	

Example URL:

	http://localhost:8080/wspls/api/test/tt.pkg_teste.prc_teste
	
	/wspls 			-> war name
		/api 		-> api
			/test 	-> datasource name
			/tt.pkg_teste.prc_teste -> OWNER.PACKAGE_NAME.PROCEDURE_NAME or PROCEDURE_NAME only.
			
	Procedure:
		procedure prc_teste(
		    p_id in number,
		    p_name in varchar2,
		    p_date in date,
		    p_dados in out number,
		    p_result_id out number,
		    p_result_name out varchar2,
		    p_result_date out date,
		    p_result_2 out number,
		    p_result_3 out number
		  );
			
	return OUT paramters JSON:
	
		{"p_result_3":null,"p_result_id":41,"p_result_date":null,"p_result_name":"rafael OK","p_dados":31,"p_result_2":2.55}
		
Simple Oracle Stored Procedure

```
CREATE OR REPLACE PACKAGE BODY "PKG_TESTE" as

  procedure prc_teste(
    p_id in number,
    p_name in varchar2,
    p_date in date,
    p_dados in out number,
    p_result_id out number,
    p_result_name out varchar2,
    p_result_date out date,
    p_result_2 out number,
    p_result_3 out number
  ) as
  begin
  
    p_dados := p_id + 10;
    p_result_id := p_id + 20;
    p_result_name := p_name || ' OK';
    p_result_date := p_date + 10;
    p_result_2 := 2.55;
    
  end prc_teste;
  ...
  
```

Simple POST request using curl:

```
$ curl --data "p_id=21&p_name=rafael&p_date=&p_dados=1" "http://localhost:8080/wspls/api/test/tt.pkg_teste.prc_teste"

{"p_result_3":null,"p_result_id":41,"p_result_date":null,"p_result_name":"rafael OK","p_dados":31,"p_result_2":2.55}
```

DataSource for Tomcat:

```
<Resource auth="Container" type="javax.sql.DataSource"
		name="jdbc/pls/test"
		username="tt" password="tt" url="jdbc:oracle:thin:@localhost:1521:XE"
		driverClassName="oracle.jdbc.driver.OracleDriver" />
```

DataSource for JBoss or WildFly:
```
<?xml version="1.0" encoding="UTF-8"?>
<datasources>
	<datasource jta="false" jndi-name="java:jboss/datasources/pls/test" pool-name="pls/test" enabled="true" use-ccm="false">
		<connection-url>jdbc:oracle:thin:@localhost:1521:XE</connection-url>
		<driver-class>oracle.jdbc.OracleDriver</driver-class>
		<driver>ojdbc6.jar</driver>
		<security>
			<user-name>tt</user-name>
			<password>tt</password>
		</security>
	</datasource>
</datasources>
```

See more examples with schema 'TT' in sql/tt.sql.
