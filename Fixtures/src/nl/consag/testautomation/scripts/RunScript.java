/**
 * This purpose of this fixture is to call a script with a variable amount of parameters using the FitNesse slim 'script' table.
 * The input parameters are provided by a table in the FitNesse wiki. 
 * @author Edward Crain
 * @since October 2014
 * @version 20150119.0
 */
package nl.consag.testautomation.scripts;

import java.io.*;

import java.text.SimpleDateFormat;

import nl.consag.supporting.Constants;
import nl.consag.supporting.Logging;

public class RunScript {
    private static final String className = "RunScript";
    private static final String version = "20150119.0";

    private String scriptName = Constants.NOT_PROVIDED;
    private String parameter = "";
    private String startDate = Constants.NOT_INITIALIZED;
    private String logFileName = Constants.NOT_INITIALIZED;
    private String context = Constants.NOT_PROVIDED;

    private int logLevel =3;
    private int logEntries =0;

    //Constructors
    public RunScript() {
        java.util.Date started = new java.util.Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        startDate = sdf.format(started);
        this.context = className;
        logFileName = startDate + "." + className;
    }

    public RunScript(String context) {
        java.util.Date started = new java.util.Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        startDate = sdf.format(started);
        this.context = context;
        logFileName = startDate + "." + className + "." + context;
    }

    /**
     * @param scriptName - Sets the script name. Should be a full path.
     */
    public void nameScript(String scriptName) {
        this.scriptName = scriptName;
    }

    /**
     * @param parameter - Adds a parameter to the script's command line
     */
    public void addParameter(String parameter) {
        if (parameter == "") {
            this.parameter = parameter;
        } else {
            this.parameter = this.parameter + " " + parameter;
        }
    }

    /**
     * @return return code of the called script.
     */
    public String runScriptReturnCode() {
        String myName ="runScriptReturnCode";
        String myArea ="init";
        Process process;
        String returnMessage = Constants.OK;
        String s = null;
            Runtime rt = Runtime.getRuntime();
        try {
            log(myName, Constants.INFO, myArea,"Script name is >" + scriptName +"<.");
            printUsedParameters();
            log(myName, Constants.INFO, myArea,"Command line is >" + scriptName + " " + parameter + "<.");
            process = rt.exec(scriptName + " " + parameter);

                myArea = "stdout";
                BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
                log(myName, Constants.INFO, myArea,
                    "Here is the standard output of the command (if the script does not re-route stdout:\n");
                while ((s = stdInput.readLine()) != null) {
                    log(myName, Constants.INFO, myArea, s);
                }

                myArea = "stderr";
                BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                log(myName, Constants.INFO, myArea,
                    "Here is the standard error of the command (if any and if script does not re-route stderr):\n");
                while ((s = stdError.readLine()) != null) {
                    if(s.contains(Constants.ERROR) || s.contains(Constants.FATAL)) {
                        log(myName, Constants.ERROR, myArea, s);
                    } else {
                        log(myName, Constants.INFO, myArea, s);
                    }
                }
            try {
                //wait for process to return a return code
                process.waitFor();
                returnMessage = Integer.toString(process.exitValue());
            } catch (InterruptedException e) {
                returnMessage ="Script execution was interrupted.";
                log(myName, Constants.ERROR, myArea, "Script was interrupted (killed?). Exception: " +e.toString() +"<.");
            }
        } catch (IOException e) {
            returnMessage ="Script not found.";
            log(myName, Constants.ERROR, myArea,"IOException occurred: >" +e.toString()+"<.");   
        }

        return returnMessage;
    }

    private void printUsedParameters() {
        String myName ="printUsedParameters";
        String myArea ="run";
        log(myName, Constants.DEBUG, myArea, "====================== Run Script fixture ======================");
        log(myName, Constants.INFO, myArea, "Entered parameters: " + parameter);
    }


    private void log(String name, String level, String location, String logText) {
        if(Constants.logLevel.indexOf(level.toUpperCase()) > getIntLogLevel()) {
               return;
        }
        logEntries++;
        if(logEntries ==1) {
        Logging.LogEntry(logFileName, className, Constants.INFO, "Fixture version", getVersion());                 
        }

    Logging.LogEntry(logFileName, name, level, location, logText);  
       }

    /**
    * @return
    */
    public String getLogFilename() {
            return logFileName + ".log";
    }

    /**
    * @param level
    */
    public void setLogLevel(String level) {
    String myName ="setLogLevel";
    String myArea ="determineLevel";
    
    logLevel = Constants.logLevel.indexOf(level.toUpperCase());
    if (logLevel <0) {
       log(myName, Constants.WARNING, myArea,"Wrong log level >" + level +"< specified. Defaulting to level 3.");
       logLevel =3;
    }
    
    log(myName, Constants.INFO,myArea,"Log level has been set to >" + level +"< which is level >" +getIntLogLevel() + "<.");
    }

    /**
    * @return
    */
    public String getLogLevel() {
    return Constants.logLevel.get(getIntLogLevel());
    }

    /**
    * @return
    */
    public Integer getIntLogLevel() {
    return logLevel;
    }

    /**
     * @return fixture version info
     */
    public static String getVersion() {
        return version;
    }
}
