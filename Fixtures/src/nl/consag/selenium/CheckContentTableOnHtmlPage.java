//----------------------------------------------------------------------------------------------------------------------------------
//Version | Date          | Changed by                     | Change
//-----------------------------------------------------------------------------------------------------------------------------------
//1.0     | 1-Oct-2013   | Edward Crain (edward@crain.nl) | New fixture created to check the content of a html table on an HTML page. (=fitnesse slim 'table' table)
//-----------------------------------------------------------------------------------------------------------------------------------
//
//Purpose:
//	Fixture to get content from an HTML table and compare it with the content of a fitnesse table.
//
//------------------------------------------------------------------------------------------------------------------------------------
package nl.consag.selenium;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

public class CheckContentTableOnHtmlPage {
	private String browser ="htmlunit";   
	private String url ="";   
	private String element_identifier ="";  
	private String element_text ="";  
	private String return_message =""; 
	private int number_of_table_columns;

	List<String> return_row = new ArrayList<String>();	 

	private String action;
	private String data;

	WebDriver driver;
	WebElement element;

	//the return table, returns the outcome of fixture (="pass", "fail", "ignore", "no change")
	private List<List<String>> return_table = new ArrayList<List<String>>();  

	//----------------------------------------------------------
	//Main function; checks input table and populates output table
	//----------------------------------------------------------
	public List doTable(List<List<String>> fitnesse_table) throws InterruptedException { 
	  	System.out.println("------------------------------ Check Content Table On HTML Page ------------------------------------------");
	  	
	  	//Get url
	  	url = fitnesse_table.get(0).get(1);
	  	//Get element identifier
	  	element_identifier = fitnesse_table.get(1).get(1);
	  	
	  	//populate return row with 'pass'
	  	//First row green
		getTextAndReturnPass (fitnesse_table.get(0));
	  	//Second row green
		getTextAndReturnPass (fitnesse_table.get(1));
 		  	
	  	// Create a new instance of the driver    
	  	driver = new HtmlUnitDriver();
	 	System.out.println("driver = htmlunit");  		  		
	 	System.out.println("url = " + url);
	  	System.out.println("element = " + element_identifier);
	 
	    // And now use this to visit url
	 	driver.get(url);
	 	element = driver.findElement(By.id(element_identifier));
	    element_text = element.getText();
	 	Thread.sleep(500);
		driver.quit();
		  
		//Return number of columns in fitnesse table
		number_of_table_columns = fitnesse_table.get(2).size();	  
		CompareExpectedTableWithPageTable (fitnesse_table);
		return return_table;
	}


	//----------------------------------------------------------
	//Function to compare input table with table on url page
	//----------------------------------------------------------
	  public void CompareExpectedTableWithPageTable(List<List<String>> fitnesse_table){  
		  List<String> url_text = new ArrayList<String>();	 
		  List<String> empty_row = new ArrayList<String>();	 

		  //Retrieve names from text.
		  String [] temp = element_text.split(" ");
		  //populate medewerkerDiVetro table
		  for (int i = 0; i < temp.length; ++i)  
		  {	
			  url_text.add(temp[i]);
			  System.out.println("Found name " + Integer.toString(i+1) + " = "+ url_text.get(i));
		 
		  }
		  
		  // Expected result is equal or greater than result rows from html page, the expected table size is - 2, since first 2 rows contain text
		  if((fitnesse_table.size()-2) >= url_text.size()){
			  for (int i = 0; i < (fitnesse_table.size()-2); ++i)  
			  {	// less rows in input than expected
				  if (i < url_text.size()){
					  CompareExpectedRowWithInputRow(fitnesse_table.get(i+2),url_text.get(i));
				  }
				  else{
					  CompareExpectedRowWithInputRow(fitnesse_table.get(i+2), "");
				}
			  }
		  }
		  else{
			  for (int i = 0; i < url_text.size(); ++i)  
			  {	// more rows on page than expected
				if (i < (fitnesse_table.size()-2)){
					  CompareExpectedRowWithInputRow(fitnesse_table.get(i+2),url_text.get(i));
				}
				else{
					  CompareExpectedRowWithInputRow(empty_row, url_text.get(i));  
				}
			  }
		  }
	  }

	//----------------------------------------------------------
	//Function to compare input table row with table row on url page
	//----------------------------------------------------------
	  public void CompareExpectedRowWithInputRow(List<String> fitnesse_row, String retrieved_text){ 
		  List<String> return_row = new ArrayList<String>();	 		  

		  //If expected row is empty
		  if (fitnesse_row.isEmpty()) {
			  return_row.add("fail: Found extra row"); 				//return "failed" to first cell which only contains the value "column name"
			  for (int i = 0; i < number_of_table_columns; ++i)  
			  {	// set value for next column cell 
				  return_row.add("fail: surplus: " + retrieved_text); //return "fail" in next cell with the found value
			  }
		  }
		  else {
		//If page row is empty
			  if (retrieved_text.equals("")) {	
				  for (int i = 0; i < number_of_table_columns; ++i)  
				  {	// set value for next column cell
					  return_row.add("fail: expected: " + fitnesse_row.get(i)); //return "fail" in next cell with the found value
				  } 		
			  }
			  else
			  {	//Compare cell for cell if expected equals outcome
				  for (int i = 0; i < number_of_table_columns; ++i)  
				  {	// set value for next column cell
					  if (fitnesse_row.get(i).equals(retrieved_text)){
						  return_row.add("pass"); //return "pass" in next cell if expected = result
					  }
					  else {
						  return_row.add("fail: expected: " + fitnesse_row.get(i) + " found: " + retrieved_text); //return "fail" in next cell with the found value  
					  }
				  } 				  
			  }
		  }	  
		 addRowToReturnTable (return_row); //return row with outcomes; pass/fail
	  }
	//----------------------------------------------------------	  
	//Function to read rows of table and set value each field in row to "pass"
	//----------------------------------------------------------
	  public void getTextAndReturnPass (List<String> input_row){	 
		  return_row.add("pass"); //return "pass" in next cell
		  addRowToReturnTable (return_row); //return row with outcomes; pass/fail
	  }  
	//----------------------------------------------------------
	//Function to add row to return table; a row contains cells with either "pass" (= green), or "fail" (= red).
	//----------------------------------------------------------
	  public void addRowToReturnTable (List <String> row) {
		  return_table.add(row);
	  } 
}
