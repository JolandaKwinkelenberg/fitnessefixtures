/*
 * The input parameters are provided by a Script table in the FitNesse wiki.
 * @author Jac. Beekers
 * @version 4 June 2015
*/
package database;

import supporting.Logging;

import java.util.*;
import java.sql.*;
import java.text.SimpleDateFormat;

import supporting.Constants;
import supporting.GetParameters;

public class BasicUpdate {

    private String className = "BasicUpdate";

    private String logFileName = Constants.NOT_INITIALIZED;
    private String context = Constants.DEFAULT;
    private String startDate = Constants.NOT_INITIALIZED;
    private int logLevel = 3;

    private String driver;
    private String url;
    private String userId;
    private String password;
    private String databaseName;
    private String databaseType;
    private String databaseConnDef;

    private String tableName;
    private String ignore0Records = Constants.NO;

    private String filterColumn = Constants.NOT_PROVIDED;
    private String filterValue = Constants.NOT_PROVIDED;
    private String modifyColumn = Constants.NOT_PROVIDED;
    private String modifyValue = Constants.NOT_PROVIDED;

    private String errorMessage = Constants.NOERRORS;
    private String errorLevel = Constants.OK;


    public BasicUpdate() {
        java.util.Date started = new java.util.Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        startDate = sdf.format(started);
        context = className;
        logFileName = startDate + "." + className;

    }

    public BasicUpdate(String context) {
        java.util.Date started = new java.util.Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        startDate = sdf.format(started);
        logFileName = startDate + "." + className + "." + context;
    }


    public boolean result(String expected) {

        readParameterFile();
        submitUpdateStatement();

        if (getErrorLevel().equals(expected))
            return true;
        else
            return false;

    }

    public void setDatabaseNameFromTestTable(List<String> input_row) {
        String logMessage = Constants.NOT_INITIALIZED;
        String myName = "setDatabaseNameFromTestTable";
        String myArea = "init";
        setDatabaseName(input_row.get(1)); //read first row second column

        logMessage = "database name: " + getDatabaseName();
        log(myName, Constants.INFO, myArea, logMessage);

    }

    public void setTableNameFromTestTable(List<String> input_row) {
        String logMessage = Constants.NOT_INITIALIZED;
        String myName = "setTableNameFromTestTable";
        String myArea = "init";

        setTableName(input_row.get(1)); //read first row second column
        logMessage = "table name: " + getTableName();
        log(myName, Constants.INFO, myArea, logMessage);

    }


    //----------------------------------------------------------
    //Function to submit statement based on input in fitnesse table row >=3
    //----------------------------------------------------------
    private String submitUpdateStatement() {
        String logMessage = Constants.NOT_INITIALIZED;
        String myName = "submitUpdateStatement";
        String myArea = "init";

        Connection connection = null;
        Statement statement = null;
        int updateQuery = 0;
        String updateString=Constants.NOT_INITIALIZED;
        
        try {
            connection = DriverManager.getConnection(url, userId, password);
            // createStatement() is used for create statement object that is used for sending sql statements to the specified database.
            statement = connection.createStatement();
            // sql query of string type to submit update SQL statement into database.
            updateString =
                "UPDATE " + tableName + " SET " + getModifyColumn() + "=" + getModifyValue() + "  WHERE " +
                getFilterColumn() + "=" + getFilterValue();

            logMessage = "SQL: " + updateString;
            log(myName, Constants.INFO, myArea, logMessage);
            updateQuery = statement.executeUpdate(updateString);

            if (updateQuery == 0) {
                if(getIgnore0Records().equals(Constants.NO)) {
                    setErrorMessage(Constants.ERROR,"Update statement did not update any records while Ignore0Records had been set to >" + getIgnore0Records() +"<.");
                } else {
                    logMessage="Update Query returned >0<. OK as Ignore0Records had been set to >" + getIgnore0Records() +"<.";
                    log(myName, Constants.INFO, myArea, logMessage);
                }
            } else {
                logMessage="Update statement resulted in >" +updateQuery + "record(s) updated";
                log(myName, Constants.INFO, myArea, logMessage);
            }

            statement.close();
            connection.close();
        }
        catch (SQLException e) {
            myArea = "exception handling";
            logMessage = "An error occurred executing statement >"+ updateString +"<. SQLException: " + e.toString(); // return "fail: SQLException : " + e;
            setErrorMessage(Constants.ERROR,logMessage);
            log(myName, Constants.ERROR, myArea, logMessage);
        }
        
        logMessage="Process completed with error level >" +getErrorLevel() +"<.";
        log(myName, Constants.ERROR, myArea, logMessage);
        return getErrorLevel();
    }

    private void readParameterFile() {
        String myName = "readParameterFile";
        String myArea = "reading parameters";
        String logMessage = Constants.NOT_INITIALIZED;

        databaseType = GetParameters.GetDatabaseType(databaseName);
        databaseConnDef = GetParameters.GetDatabaseConnectionDefinition(databaseName);
        driver = GetParameters.GetDatabaseDriver(databaseType);
        url = GetParameters.GetDatabaseURL(databaseConnDef);
        userId = GetParameters.GetDatabaseUserName(databaseName);
        password = GetParameters.GetDatabaseUserPWD(databaseName);

        logMessage = "databaseType >" + databaseType + "<.";
        log(myName, Constants.VERBOSE, myArea, logMessage);
        logMessage = "connection >" + databaseConnDef + "<.";
        log(myName, Constants.VERBOSE, myArea, logMessage);
        logMessage = "driver >" + driver + "<.";
        log(myName, Constants.VERBOSE, myArea, logMessage);
        logMessage = "url >" + url + "<.";
        log(myName, Constants.VERBOSE, myArea, logMessage);
        logMessage = "userId >" + userId + "<.";
        log(myName, Constants.VERBOSE, myArea, logMessage);
    }

    private void log(String name, String level, String location, String logText) {
        if (Constants.logLevel.indexOf(level.toUpperCase()) > getIntLogLevel()) {
            return;
        }

        Logging.LogEntry(getLogFilename(), name, level, location, logText);
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
        String myName = "setLogLevel";
        String myArea = "determineLevel";

        logLevel = Constants.logLevel.indexOf(level.toUpperCase());
        if (logLevel < 0) {
            log(myName, Constants.WARNING, myArea, "Wrong log level >" + level + "< specified. Defaulting to level 3.");
            logLevel = 3;
        }

        log(myName, Constants.INFO, myArea,
            "Log level has been set to >" + level + "< which is level >" + getIntLogLevel() + "<.");
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

    public void setDatabaseName(String databaseName) {
        String myName="setDatabaseName";
        String myArea="run";
        String logMessage =Constants.NOT_INITIALIZED;
        this.databaseName = databaseName;

        logMessage="Database name has been set to >" + this.databaseName + "<.";
        log(myName,Constants.VERBOSE,myArea,logMessage);
    }

    public String getDatabaseName() {
        return this.databaseName;
    }

    public void setTableName(String tableName) {
        String myName="setTableName";
        String myArea="run";
        String logMessage =Constants.NOT_INITIALIZED;
        this.tableName = tableName;

        logMessage="Table name has been set to >" + this.tableName + "<.";
        log(myName,Constants.VERBOSE,myArea,logMessage);
    }

    public String getTableName() {
        return tableName;
    }

    public String setIgnore0Records(String ignoreIt) {
        String myName = "setIgnore0Records";
        String myArea = "run";
        String logMessage = Constants.NOT_PROVIDED;
        String rc = Constants.OK;

        if (Constants.YES.equalsIgnoreCase(ignoreIt) || Constants.NO.equalsIgnoreCase(ignoreIt)) {
            this.ignore0Records = ignoreIt;
            logMessage="Ignore 0 Records has been set to >" + this.ignore0Records + "<.";
            log(myName,Constants.VERBOSE,myArea,logMessage);
        } else {
            rc = Constants.ERROR;
            logMessage = "Wrong value >" + ignoreIt + "< for Ignore0Records supplied. Needs to be Yes or No";
            setErrorMessage(Constants.ERROR, logMessage);
            log(myName, Constants.ERROR, myArea, logMessage);
        }
        return rc;
    }
    
    public String getIgnore0Records() {
        return ignore0Records;
    }


    private void setErrorMessage(String errMessage) {
        String myName = "setErrorMessage";
        String myArea = "run";
        String logMessage = Constants.NOT_PROVIDED;
        
        this.errorMessage = errMessage;
        logMessage="Error message has been set to >" + this.errorMessage + "<.";
        log(myName,Constants.VERBOSE,myArea,logMessage);
    }

    private void setErrorMessage(String level, String logMessage) {
        setErrorMessage(logMessage);
        setErrorLevel(level);
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setFilterColumn(String colName) {
        String myName = "setFilterColumn";
        String myArea = "run";
        String logMessage = Constants.NOT_PROVIDED;
        this.filterColumn = colName;

        logMessage="Column to filter on has been set to >" + this.filterColumn + "<.";
        log(myName,Constants.VERBOSE,myArea,logMessage);
    }

    public String getFilterColumn() {
        return filterColumn;
    }

    public void setFilterValue(String colValue) {
        String myName = "setFilterColumn";
        String myArea = "run";
        String logMessage = Constants.NOT_PROVIDED;
        this.filterValue = colValue;

        logMessage="Value to filter on has been set to >" + this.filterValue + "<.";
        log(myName,Constants.VERBOSE,myArea,logMessage);
    }

    public String getFilterValue() {
        return filterValue;
    }

    public void setModifyColumn(String colName) {
        String myName = "setModifyColumn";
        String myArea = "run";
        String logMessage = Constants.NOT_PROVIDED;
        this.modifyColumn = colName;

        logMessage="Column to modify on has been set to >" + this.modifyColumn + "<.";
        log(myName,Constants.VERBOSE,myArea,logMessage);
    }

    public String getModifyColumn() {
        return this.modifyColumn;
    }

    public void setModifyValue(String colValue) {
        String myName = "setModifyValue";
        String myArea = "run";
        String logMessage = Constants.NOT_PROVIDED;
        this.modifyValue = colValue;

        logMessage="Value to modify column to has been set to >" + this.modifyValue + "<.";
        log(myName,Constants.VERBOSE,myArea,logMessage);
    }

    public String getModifyValue() {
        return modifyValue;
    }
    /*
     * Methods needed for FitNesse as it does not call set/get methods
     */
    public void databaseName(String databaseName) {
        setDatabaseName(databaseName);
    }

    public void tableName(String tableName) {
        setTableName(tableName);
    }

    public void Ignore0Records(String ignoreIt) {
        setIgnore0Records(ignoreIt);
    }

    public void FilterOnColumn(String colName) {
        setFilterColumn(colName);
    }

    public void withValue(String colValue) {
        setFilterValue(colValue);
    }

    public void modifyColumn(String colName) {
        setModifyColumn(colName);
    }

    public void setToValue(String colValue) {
        setModifyValue(colValue);
    }

    private void setErrorLevel(String level) {
        String myName = "setModifyValue";
        String myArea = "run";
        String logMessage = Constants.NOT_PROVIDED;

        this.errorLevel = level;
        logMessage="Error level has been set to >" + this.errorLevel + "<.";
        log(myName,Constants.VERBOSE,myArea,logMessage);
    }

    public String getErrorLevel() {
        return errorLevel;
    }
}