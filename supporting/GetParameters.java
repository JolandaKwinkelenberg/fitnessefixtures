package supporting;

import java.io.*;

import java.util.Properties;

import supporting.Constants;

public class GetParameters {
  private static String sNotFound = Constants.NOT_FOUND;
  private static String curFields[];
  private static String delimiter = Constants.INPUT_FILE_DELIMITER;
  private static String sIncoming ="incoming";
  private static String sOutgoing ="outgoing";
  private static String sTestdata ="testdata";
  private static String sDeployment="deployment";
  private static String sTemp ="temp";
  private static String sEnvironment ="Environment";
  private static String sLogDir ="logdir";

//Static values for indexes in Database parameter files
  private static int iDatabaseType=1;
  private static int iDatabaseConn=2;
  private static int iDatabaseUserName=3;
  private static int iDatabaseUserPWD=4;
  private static int iDatabaseTableOwnerName=5;
  private static int iDatabaseTableOwnerPWD=6;
//Static values for indexes in PowerCenter parameter files
  private static int iDomainName=1;
  private static int iRepoService=2;
  private static int iIntService=3;
  private static int iUserName=4;
  private static int iPassWord=5;
  private static int iWshUrl=1;
  private static int iRootDirIndex=1;
  
  public static String GetPhysicalSourceDir(String logical) {
      return readDirectoryProperties(logical);
  }
  
  public static String getWshUrl(String searchFor) {
	  // application name is provided
	  // appwsh contains applications and which wsh object to use
	  // wsh contains the definition of the wsh object to use
      String logicalWSH ="Unknown";          
      // first find the app in appwsh      
      logicalWSH =FindParameter(Constants.APPWSH_PROPERTIES, searchFor, iWshUrl);
      if (sNotFound.equals(logicalWSH)) {
    	  return "application >" + searchFor + "< not found in >" + Constants.APPWSH_PROPERTIES +"<."; 
      }
      // now go look for the wsh
      return FindParameter(Constants.WSH_PROPERTIES, logicalWSH);
  }
        
  public static String GetEnvironment() {
	  //GetEnvironment: Determine which environment fitnesse is running
	  return FindParameter(Constants.ENVIRONMENT_PROPERTIES, sEnvironment);
  }

  public static String GetLogDir() {
	  //GetEnvironment: Determine which environment fitnesse is running
	  return FindParameter(Constants.FILEOPERATION_PROPERTIES, sLogDir);
  }

  public static String GetRootDir(String area){
	  //Determine root directory    
	  return FindParameter(Constants.FILEOPERATION_PROPERTIES, area, iRootDirIndex);
  }
  
  public static String GetIncoming() {
	  //Get info for File Operations and determine Incoming directory
      return FindParameter(Constants.FILEOPERATION_PROPERTIES, sIncoming);
    }
  
  public static  String GetOutgoing() {
	  //Get info for File Operations and determine Outgoing directory
	  return FindParameter(Constants.FILEOPERATION_PROPERTIES, sOutgoing);
    }

  public static String GetTemp() {
  	  //Get info for File Operations and determine temporary directory
	  return FindParameter(Constants.FILEOPERATION_PROPERTIES, sTemp);
  }

  public static String GetTestdata() {
	  //Get info for File Operations and determine base directory for test data
	  return FindParameter(Constants.FILEOPERATION_PROPERTIES, sTestdata);
  }

  public static String GetDeployment() {
      return FindParameter(Constants.FILEOPERATION_PROPERTIES, sDeployment);
    }

  /**
   * ==================================================================================
   * Area: PowerCenter parameters
   */
  public static String getDomainName(String pConnectionName) {
	  //Get PowerCenter info and determine domain name
	  return FindParameter(Constants.POWERCENTER_PROPERTIES, pConnectionName, iDomainName);
   }

  public static String getRepoService(String pConnectionName) {
	  //Get PowerCenter info and repository service name
	  return FindParameter(Constants.POWERCENTER_PROPERTIES, pConnectionName, iRepoService);
  }

  public static String getIntService(String pConnectionName) {
	  //Get PowerCenter info and integration service name
	  return FindParameter(Constants.POWERCENTER_PROPERTIES, pConnectionName, iIntService);
  }

  public static String getUsername(String pConnectionName) {
	  //Get PowerCenter info and determine user name
	  return FindParameter(Constants.POWERCENTER_PROPERTIES, pConnectionName, iUserName);
  }

  public static String getPassword(String pConnectionName) {
	  //Get PowerCenter info and determine password
	  return FindParameter(Constants.POWERCENTER_PROPERTIES, pConnectionName, iPassWord);
  }

  /**
   * ==================================================================================
   * Area: Database parameters
   */
    
  public static String GetDatabaseType(String pConnectionName) {
	  //Get database info and determine database type. Use this method to retrieve the value to be used in GetDatabaseDriver
	  return FindParameter(Constants.DATABASE_PROPERTIES, pConnectionName, iDatabaseType);
  }

  public static String GetDatabaseConnectionDefinition(String pConnectionName) {
	  //Get database info and determine database connection definition, e.g. SRV0OPWL101. Call this method to retrieve the value to be used in GetDatabaseURL
	  return FindParameter(Constants.DATABASE_PROPERTIES, pConnectionName, iDatabaseConn);
  }

  public static String GetDatabaseUserName(String pConnectionName) {
	  //Get database info and determine database user name.
	  return FindParameter(Constants.DATABASE_PROPERTIES, pConnectionName, iDatabaseUserName);
  }
  
  public static String GetDatabaseTableOwnerName(String pConnectionName) {
	  return FindParameter(Constants.DATABASE_PROPERTIES, pConnectionName, iDatabaseTableOwnerName);
  }

  public static String GetDatabaseUserPWD(String pConnectionName) {
	  //Get database info and determine database user password.
	  return FindParameter(Constants.DATABASE_PROPERTIES, pConnectionName, iDatabaseUserPWD);
  }

  public static String GetDatabaseTableOwnerPWD(String pConnectionName) {
	  return FindParameter(Constants.DATABASE_PROPERTIES, pConnectionName, iDatabaseTableOwnerPWD);
  }

  public static String GetDatabaseDriver(String pDatabaseType) {
	  //Get database info and determine database driver. Database Type has to be a value determined by GetDatabaseType
	  return FindParameter(Constants.JDBC_PROPERTIES, pDatabaseType);
  }

  public static String GetDatabaseURL(String pDatabaseConn) {
	  //Get database info and determine database driver. The argument has to be a value  determined by GetDatabaseConnectionDefinition
	  return FindParameter(Constants.JDBC_PROPERTIES, pDatabaseConn);
  }


/**
 * ==================================================================================
   * FindParameter
   * Finds the value for a specified parameter in a specified file
   * Arguments:
   *  1 Parameter file to look in
   *  2 Parameter to search for
   * @return
   *  Returns the found value or NOTFOUND
   */

  private static String FindParameter(String pFileName, String pSearchFor) {
	 //Note: This method only exists for backward compatibility and uses a different delimiter.
	  String result;
	  delimiter =": ";
	  result= FindParameter(pFileName, pSearchFor, 1);
	  delimiter =":";
	  return result;
   }

  private static String FindParameter(String pFileName, String pSearchFor, Integer pIndex) {
    String sSearchFor = pSearchFor;
    String sFileName = pFileName;
    boolean bFound;
    String result =sNotFound;
    
    try {
    // Open FitNesse parameter file
    	FileInputStream fstream = new FileInputStream(sFileName);
    	DataInputStream in = new DataInputStream(fstream);
    	BufferedReader br = new BufferedReader(new InputStreamReader(in));
    	String strLine;
    	bFound = false;
    	while ( !bFound && ((strLine = br.readLine()) != null)) {
        	//Read File Line By Line
    		curFields = strLine.split(delimiter);
    		if (curFields[0].equals(sSearchFor)) {
            	result = curFields[pIndex];
            	bFound = true;
    		}
    	}
    	in.close();
    } 
    catch (Exception e) { //Catch exception if any
    	result = e.toString();
    }
    return result;
  }
  
    /*
     * read logical to physical directory mapping properties file
     */
    public static String readDirectoryProperties(String prop) {
        String val = Constants.NOT_FOUND;
        /*
         * Get mapping
         */
        try {
        File file = new File(Constants.DIRECTORY_PROPERTIES);
        FileInputStream fileInput = new FileInputStream(file);
        Properties schedProp = new Properties();
        schedProp.load(fileInput);
        fileInput.close();
        
        val = schedProp.getProperty(prop, Constants.NOT_FOUND);
        } catch (FileNotFoundException e) {
            val = Constants.NOT_FOUND;
            } catch (IOException e) {
                val = Constants.NOT_FOUND;
            }
        return val;
    }

  
}