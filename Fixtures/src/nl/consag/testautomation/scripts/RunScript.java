/**
 * This purpose of this fixture is to call a script with a variable amount of parameters using the FitNesse slim 'script' table.
 * The input parameters are provided by a table in the FitNesse wiki. 
 * @author Edward Crain
 * @version 12 October 2014
 */
package scripts;

import java.io.*;

public class RunScript { 
	  private String scriptName;
	  private String parameter = "";

	  public void nameScript(String scriptName) {    
		  this.scriptName = scriptName;    
	  }
	   
	  public void addParameter (String parameter) {
		  if (parameter == "") {
			  this.parameter = parameter;
		  }
		  else {
			  this.parameter = this.parameter + " " + parameter;  
		  }
	  }	  

	  public String runScriptReturnCode () {
		  Process process;
		  String returnMessage = null;
	      String s = null;
		  try
		  {
			  Runtime rt = Runtime.getRuntime(); 
			  process = rt.exec(scriptName + parameter);
			  printUsedParameters();
			  
	          BufferedReader stdInput = new BufferedReader(new 
		                 InputStreamReader(process.getInputStream()));
		      System.out.println("Here is the standard output of the command:\n");
		      while ((s = stdInput.readLine()) != null) {
		                System.out.println(s);
		            }
		      
		      BufferedReader stdError = new BufferedReader(new 
		                 InputStreamReader(process.getErrorStream()));		            
		      System.out.println("Here is the standard error of the command (if any):\n");
		      while ((s = stdError.readLine()) != null) {
		                System.out.println(s);
		            }
		      //wait for process to return a return code
		      process.waitFor();
		      returnMessage = Integer.toString(process.exitValue());
		  }
		  catch(Exception e)
		  {
			  System.out.println("Exception: "+ e.toString());
		  }
		  return returnMessage;
	  }
	  
	  public void printUsedParameters() {
		  System.out.println("\n====================== Run Script fixture ======================");
		  System.out.println("Entered parameters: " + parameter);
	  }	      	  
}