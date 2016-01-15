/**
 * This purpose of this fixture is to execute a database query using the FitNesse slim 'decision' table.
 * The input parameters are provided by a table in the FitNesse wiki. 
 * @author Edward Crain
 * @version 12 October 2014
 */
package nl.consag.testautomation.database;

import java.io.*;
import java.sql.*;
import java.util.Enumeration;
import java.util.Properties;

public class ExecuteQuery {
	  private boolean error=false;
	  private String errorMessage="";

	  private String driver;
	  private String url;
	  private String databaseName;
	  private String query;

	  public void setDatabaseName(String databaseName) {  
		  this.databaseName = databaseName;  
	  }
  
	  public void setQuery (String query) {
		  this.query = query;  		  
	  }  
 
	  public String result () {
		  getDatabaseParameters();
		  printUsedParameters();
		  
		  Connection connection = null;
		  Statement statement = null;
		  String returnMessage;

		  int updateQuery = 0; 
 		  
		  if (!error) {			  
			  try {
				    Class.forName(driver);
				    connection = DriverManager.getConnection(url);	
				    statement = connection.createStatement();
				    updateQuery = statement.executeUpdate(query);

				    if (updateQuery != 0){ 
				    	returnMessage = "OK";}
				    else {
				    	returnMessage = "failed";
				    }	 
				    
				    statement.close();
		    	    connection.close();  			 			    
				} catch (ClassNotFoundException e) {
					returnMessage = "Class not found : " + e;
				} catch (SQLException e) {
					returnMessage = "SQLException : " + e;
				}
			  }
		  else {
			  returnMessage = errorMessage;
		  }
		  return returnMessage; //text message that is passed to FitNesse
	  }
	  
	  public void printUsedParameters() {
		  System.out.println("\n====================== Truncate Database Table ======================");
		  System.out.println("Database: " + databaseName);
		  System.out.println("Query: " + query);
	  }	  	  	  
	  
	  public void getDatabaseParameters(){	 
		  	try{
		  		File file = new File("conf/database.properties");
				FileInputStream fileInput = new FileInputStream(file);
				Properties properties = new Properties();
				properties.load(fileInput);
				fileInput.close();

				Enumeration enuKeys = properties.keys();
				String temp[];  	
				while (enuKeys.hasMoreElements()) {
					String key = (String) enuKeys.nextElement();
					String databaseProperties = properties.getProperty(key);
					  temp = databaseProperties.split(";");						 
					  if (temp[0].equals(databaseName)){
						  driver = temp [1];
						  url = temp [2];									  
					  		}		  		
						}
				    }
			catch (Exception e){//Catch exception if any
				  error=true;
				  System.err.println("Error reading database parameter file: " + e.getMessage());
				  errorMessage= errorMessage + " \nFile error : Reading database parameter file: " + e.getMessage();
			}
		 }	
}