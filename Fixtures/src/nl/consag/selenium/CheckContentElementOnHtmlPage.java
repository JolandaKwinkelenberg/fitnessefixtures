//----------------------------------------------------------------------------------------------------------------------------------
//Version | Date          | Changed by                     | Change
//-----------------------------------------------------------------------------------------------------------------------------------
//1.0     | 26-Sep-2013   | Edward Crain (edward@crain.nl) | New fixture created to check the content of a html element on an HTML page. (=fitnesse slim 'table' table)
//-----------------------------------------------------------------------------------------------------------------------------------
//
//Purpose:
//	Fixture to get content from an HTML page and compare it with the content of a fitnesse table.
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


public class CheckContentElementOnHtmlPage {
	private String browser ="htmlunit";   
	private String url="";   
	private String element_identifier ="";  

	private String element_text ="";  
	private String return_message =""; 

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
	  	System.out.println("------------------------------ Check content element on html page Fixture ------------------------------------------");
	  		  
	  	//Get url
	  	url = fitnesse_table.get(0).get(1);
	  	//Get element identifier
	  	element_identifier = fitnesse_table.get(1).get(1);
	  	
	  	//populate return row with 'pass'
		return_row.add("pass"); //return "pass" in next cell
		return_row.add("pass"); //return "pass" in next cell
	  	//First 2 rows green
		getTextAndReturnPass (fitnesse_table.get(0));
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
	  	System.out.println("element text = " + element_text);
	 	Thread.sleep(500);
		driver.quit();
		
		CompareExpectedTableWithPageTable (fitnesse_table);
		return return_table;
	}


	//----------------------------------------------------------
	//Function to compare input table with element on url page
	//----------------------------------------------------------
	  public void CompareExpectedTableWithPageTable(List<List<String>> fitnesse_table){  
			  //Retrieve names from text.
		  System.out.println("Found text on page = "+ element_text);
		  CompareExpectedRowWithInputRow(fitnesse_table.get(2),element_text);
	  }

	//----------------------------------------------------------
	//Function to compare input table row with element on url page
	//----------------------------------------------------------
	  public void CompareExpectedRowWithInputRow(List<String> fitnesse_row, String retrieved_text){ 
		  List<String> return_row = new ArrayList<String>();	 		  
		  return_row.add("pass"); //return "pass" in first cell
		  if (fitnesse_row.get(1).equals(retrieved_text)){
			  return_row.add("pass"); //return "pass" in next cell if expected = result
		  }
		  else {
			  return_row.add("fail: expected: " + fitnesse_row.get(1) + " found: " + retrieved_text); //return "fail" in next cell with the found value  
		  }
		 addRowToReturnTable (return_row); //return row with outcomes; pass/fail
	  }
	//----------------------------------------------------------	  
	//Function to read rows of table and set value each field in row to "pass"
	//----------------------------------------------------------
	  public void getTextAndReturnPass (List<String> input_row){	 
		  addRowToReturnTable (return_row); //return row with outcomes; pass/fail
	  }  
	//----------------------------------------------------------
	//Function to add row to return table; a row contains cells with either "pass" (= green), or "fail" (= red).
	//----------------------------------------------------------
	  public void addRowToReturnTable (List <String> row) {
		  return_table.add(row);
	  } 
}
