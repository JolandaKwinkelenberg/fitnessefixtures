/*
 * @author Jac. Beekers
 * @version 0.1
 * @since July 2015
 */
package nl.consag.testautomation.supporting;

import java.io.*;

import java.text.SimpleDateFormat;

import nl.consag.supporting.Logging;

public class CheckFixtureProperties {
    

  //27-07-2013 Change to new log mechanism, preventing java heap space error with fitnesse stdout
    private String m_className="CheckFixtureProperties";
    private String sNotInit ="Not initialized";
    private String sDefault="default";  // Also used in Logging. Keep value in-sync!

    private String m_context=sDefault;
    private String m_startDate=sNotInit;
    private boolean m_firstTime=true;
  //27-07-2013

    private static String sNotFound ="NOTFOUND";
  private static String sNotImplemented="Not yet implemented";
  private static String curFields[];
  private static String delimiter = ":";
  private static String paramFileOperation ="fileoperation.properties";
  private static String paramApplication ="application.properties";
  private static String paramDatabase ="database.properties";
  private static String paramJDBC ="jdbc.properties";
  private static String paramPowerCenter ="powercenter.properties";
  private static String paramPowerCenterAppWSH ="appwsh.properties";
  private static String paramPowerCenterWSH ="wsh.properties";
  private static String paramDAC ="dac.properties";
  private static String paramEnvironment ="environment.properties";
  private static String sIncoming ="incoming";
  private static String sOutgoing ="outgoing";
  private static String sTestdata ="testdata";
  private static String sTemp ="temp";
  private static String sEnvironment ="Environment";
  private static String sLogDir ="logdir";
  private static String cNotInit ="Not initialized";
  private static String sOk ="OK";
  /**
   * Static values for indexes in Database parameter files
   */
  private static int iDatabaseType=1;
  private static int iDatabaseConn=2;
  private static int iDatabaseUserName=3;
  private static int iDatabaseUserPWD=4;
  private static int iDatabaseTableOwnerName=5;
  private static int iDatabaseTableOwnerPWD=6;
  //
  /**
   * Static values for indexes in PowerCenter parameter files
   */
  private static int iDomainName=1;
  private static int iRepoService=2;
  private static int iIntService=3;
  private static int iUserName=4;
  private static int iPassWord=5;
  
  private static int iWshUrl=1;

//
  private static int iRootDirIndex=1;
  
// holding values for this instance
    private static String m_database =cNotInit;
//
  
    public CheckFixtureProperties() {
      java.util.Date started=new java.util.Date();
      SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
      m_startDate = sdf.format(started);

    }
  public CheckFixtureProperties(String context) {
    java.util.Date started=new java.util.Date();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
    m_startDate = sdf.format(started);
  m_context=context;
    
  }

    public static String forDatabaseName(String dbName) {
        m_database =dbName;
        return sOk;
    }
  // Check login user
  public  boolean loginUser (String userId) {
      if(GetDatabaseUserName(m_database).equals(userId)) 
          return true;          
      else 
          return false;          
  }
  public   String loginUser () {
      return GetDatabaseUserName(m_database);
  }

    // Check login user
    public  boolean dbConnection (String dbConnection) {
        if(GetDatabaseConnectionDefinition(m_database).equals(dbConnection))
        return true;
        else
            return false;
    }
    
    public  String dbConnection() {
        return GetDatabaseConnectionDefinition(m_database);
    }
  // application name is provided
  // appwsh contains applications and which wsh object to use
  // wsh contains the definition of the wsh object to use
  public  String getWshUrl(String searchFor) {
      String logicalWSH ="Unknown";
          
      
      // first find the app in appwsh
      
      logicalWSH =FindParameter(paramPowerCenterAppWSH, searchFor, iWshUrl);
      if (sNotFound.equals(logicalWSH)) {
        return "application >" + searchFor + "< not found in >" + paramPowerCenterAppWSH +"<."; 
      }
      
      // now go look for the wsh
      return FindParameter(paramPowerCenterWSH, logicalWSH);
  }
        
  /**
   * GetEnvironment: Determine which environment fitnesse is running
   * @return
   */
  public  String GetEnvironment() {
    return FindParameter(paramEnvironment, sEnvironment);
  }

  /**
   * GetEnvironment: Determine which environment fitnesse is running
   * @return
   */
  public  String GetLogDir() {
    return FindParameter(paramFileOperation, sLogDir);
  }

/*
 * Determine root directory
 */
  public  String GetRootDir(String area){
    
    return FindParameter(paramFileOperation, area, iRootDirIndex);
  }
  
    /**
     * Get info for File Operations
     * Determine Incoming directory
     * @return
     */
    public  String GetIncoming() {
      return FindParameter(paramFileOperation, sIncoming);
    }
  /**
   * Get info for File Operations
   * Determine Outgoing directory
   * @return
   */
    public   String GetOutgoing() {
      return FindParameter(paramFileOperation, sOutgoing);
    }
  /**
   * Get info for File Operations
   * Determine Temp directory
   * @return
   */
    public  String GetTemp() {
    return FindParameter(paramFileOperation, sTemp);
  }

    /**
     * Get info for File Operation
     * Determine base directory for test data
     * @return
     */
    public  String GetTestdata() {
    return FindParameter(paramFileOperation, sTestdata);
  }

  /**
   * ==================================================================================
   * Area: PowerCenter parameters
   */
    
  /**
   * Get PowerCenter info
   * Determine Doamin Name
   * @return
   */
   public  String getDomainName(String pConnectionName) {
   return FindParameter(paramPowerCenter, pConnectionName, iDomainName);
   }
  /**
   * Get PowerCenter info
   * Determine Repository Service Name
   * @return
   */
  public  String getRepoService(String pConnectionName) {
  return FindParameter(paramPowerCenter, pConnectionName, iRepoService);
  }

  /**
   * Get PowerCenter info
   * Determine Integration Service Name
   * @return
   */
  public  String getIntService(String pConnectionName) {
  return FindParameter(paramPowerCenter, pConnectionName, iIntService);
  }

  /**
   * Get PowerCenter info
   * Determine User Name
   * @return
   */
  public  String getUsername(String pConnectionName) {
  return FindParameter(paramPowerCenter, pConnectionName, iUserName);
  }

  /**
   * Get PowerCenter info
   * Determine Passsword
   * @return
   */
  public  String getPassword(String pConnectionName) {
  return FindParameter(paramPowerCenter, pConnectionName, iPassWord);
  }

  /**
   * ==================================================================================
   * Area: Database parameters
   */
    
  /**
   * Get Database info
   * Determine database type
   * Use this method to retrieve the value to be used in GetDatabaseDriver
   * @return
   */
  public  String GetDatabaseType(String pConnectionName) {
  return FindParameter(paramDatabase, pConnectionName, iDatabaseType);
  }

  /**
   * Get Database info
   * Determine database Connection Definition, e.g. SRV0OPWL101
   * Call this method to retrieve the value to be used in GetDatabaseURL
   * @return
   */
  public  String GetDatabaseConnectionDefinition(String pConnectionName) {
  return FindParameter(paramDatabase, pConnectionName, iDatabaseConn);
  }

  /**
   * Get Database info
   * Determine database user name
   * @return
   */
  public  String GetDatabaseUserName(String pConnectionName) {
  return FindParameter(paramDatabase, pConnectionName, iDatabaseUserName);
  }
  public  String GetDatabaseTableOwnerName(String pConnectionName) {
  return FindParameter(paramDatabase, pConnectionName, iDatabaseTableOwnerName);
  }

  /**
   * Get Database info
   * Determine database user password
   * @return
   */
  public  String GetDatabaseUserPWD(String pConnectionName) {
  return FindParameter(paramDatabase, pConnectionName, iDatabaseUserPWD);
  }
  public  String GetDatabaseTableOwnerPWD(String pConnectionName) {
  return FindParameter(paramDatabase, pConnectionName, iDatabaseTableOwnerPWD);
  }

  /**
   * Get Database info
   * Determine database Driver
   * Database Type has to be a value determined by GetDatabaseType
   * @return
   */
  public  String GetDatabaseDriver(String pDatabaseType) {
  return FindParameter(paramJDBC, pDatabaseType);
  }

  /**
   * Get Database info
   * Determine database Driver
   * The argument has to be a value  determined by GetDatabaseConnectionDefinition
   * @return
   */
  public  String GetDatabaseURL(String pDatabaseConn) {
  return FindParameter(paramJDBC, pDatabaseConn);
  }


/**
 * ==================================================================================
 */
    
  /**
   * FindParameter
   * Finds the value for a specified parameter in a specified file
   * Arguments:
   *  1 Parameter file to look in
   *  2 Parameter to search for
   * @return
   *  Returns the found value or NOTFOUND
   */


/*
* Note: This method only exists for backward compatibility and uses a different delimiter.
*/
   private  String FindParameter(String pFileName, String pSearchFor) {
    String sResult;
    delimiter =": ";
    sResult= FindParameter(pFileName, pSearchFor, 1);
    delimiter =":";
return sResult;
   }

    private  String FindParameter(String pFileName, String pSearchFor, Integer pIndex) {
      
  
        String sSearchFor = pSearchFor;
        String sFileName = pFileName;
        boolean bFound;
        String sResult =sNotFound;
    //  log("FindParameter","debug","init","Searching in file =>" + pFileName + "< for >" + pSearchFor + "< Index =>" + pIndex +"<.");
              try {
            // Open fitnesse parameter file
            FileInputStream fstream =
                new FileInputStream(sFileName);
            // Get the object of DataInputStream
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));

            String strLine;
            //Read File Line By Line
            bFound =false;
            while ( !bFound && ((strLine = br.readLine()) != null)) {
                curFields = strLine.split(delimiter);
                if (curFields[0].equals(sSearchFor)) {
                    sResult= curFields[pIndex];
                    bFound=true;
                }
            }
            //Close the input stream
            in.close();
        } catch (Exception e) { //Catch exception if any
            log("ReadParameter","error","exception handling","Error reading file: " + e.getMessage());
        }
      return sResult;
    
    }

      
  //27-07-2013 New log mechanism 
  public void log(String name, String level, String area, String logMessage) {
     
     String logFileName =sNotInit;
     
  
     if(m_context.equals(sDefault)) {
     logFileName=m_startDate+"." + m_className;
   Logging.LogEntry(logFileName, name,level,area,logMessage);
     } else {
         logFileName=m_context +"." +m_startDate;
       Logging.LogEntry(logFileName,name,level,area,logMessage);          
     }

   if(m_firstTime) {
     m_firstTime = false;
     System.out.print("\nLog file =>" + logFileName +"<.");          
   } 
   
  }


}