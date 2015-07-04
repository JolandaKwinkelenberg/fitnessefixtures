/**
 * This purpose of this fixture is to a temporary table based on a database query 
 * The input parameters are provided by a table in the FitNesse wiki. 
 * @author Jac Beekers
 * @version 10 May 2015
 */
package database;

import java.sql.*;

import java.text.*;

import java.util.*;

import supporting.*;

public class CreateTable {

    private String className = "CreateTable";
    private String logFileName = Constants.NOT_INITIALIZED;
    private String context = Constants.DEFAULT;
    private String startDate = Constants.NOT_INITIALIZED;
    private int logLevel =3;

    private String driver;
    private String url;
    private String userId;
    private String password;
    private String databaseName;
    private String query;
    private String databaseType;
    private String databaseConnDef;
    private String tableOwner;
    private String tableOwnerPassword;
    private String tableComment = Constants.TABLE_COMMENT;
    private String errorMessage = Constants.NO_ERRORS;
    private String errorCode=Constants.OK;


    public CreateTable() {
        //Constructors
        java.util.Date started = new java.util.Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        startDate = sdf.format(started);
        this.context = className;
        logFileName = startDate + "." + className ;

    }

    public CreateTable(String context) {
        java.util.Date started = new java.util.Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        startDate = sdf.format(started);
        this.context = context;
        logFileName = startDate + "." + className +"." + context;

    }

    public String createTable(String tableName, String columnList) {
        
        String sqlStatement =Constants.NOT_INITIALIZED;
        String commentStatement =Constants.NOT_INITIALIZED;
        String rc =Constants.OK;
        
        sqlStatement="CREATE TABLE " +tableName +"(" +columnList +")";
        commentStatement = "comment on table " + tableName + " is '" + getTableComment() + "'";
        rc=execSQL(sqlStatement,commentStatement);
        return rc;
    }


    /**
     * @param inTableName
     * @param inDatabase
     * @param inSelectStmt: The select statement that will be used to create the table
     * @return
     */
    public boolean tableExistsInDatabaseUsing(String inTableName, String inDatabase, String inSelectStmt) {
        String myName = "tableExistsInDatabaseUsing";
        String myArea = "init";
        String logMessage = Constants.NOT_INITIALIZED;
        String sqlStatement = Constants.NOT_INITIALIZED;
        String commentStatement = Constants.NOT_INITIALIZED;
        String rc = Constants.OK;
        String tableName = Constants.TABLE_PREFIX + inTableName;

        setDatabaseName(inDatabase);

            sqlStatement = "create table " + tableName + " as " + inSelectStmt;
            commentStatement = "comment on table " + tableName + " is '" + tableComment + "'";
        
        rc=execSQL(sqlStatement, commentStatement);

        if(Constants.OK.equals(rc))
            return true; else
        return false;
    }

    private String execSQL(String sqlStatement, String commentStatement) {
        String myName="ExecSQL";
        String myArea="init";
        String logMessage=Constants.NOT_INITIALIZED;
        Connection connection = null;
        Statement statement = null;
        int sqlResult = 0;
        String rc=Constants.OK;

        readParameterFile();

        myArea="Check dbtype";
        if ("Oracle".equals(databaseType) || "DB2".equals(databaseType)) {
        } else {
            logMessage = "databaseType >" + databaseType + "< not yet supported";
            log(myName, Constants.ERROR, myArea, logMessage);
            setError(Constants.ERROR,logMessage);
            return getErrorCode();
        }
    
    try {
        myArea = "SQL Execution";
        logMessage = "Connecting to >" + databaseConnDef + "< using userID >" + tableOwner + "<.";
        log(myName, Constants.DEBUG, myArea, logMessage);
        connection = DriverManager.getConnection(url, tableOwner, tableOwnerPassword);
        statement = connection.createStatement();
        logMessage = "SQL >" + sqlStatement + "<.";
        log(myName, Constants.DEBUG, myArea, logMessage);
        sqlResult = statement.executeUpdate(sqlStatement);
        logMessage = "SQL returned >" + Integer.toString(sqlResult) + "<.";
        log(myName, Constants.DEBUG, myArea, logMessage);
        statement.close();

        statement = connection.createStatement();
        logMessage = "SQL >" + commentStatement + "<.";
        log(myName, Constants.DEBUG, myArea, logMessage);
        sqlResult = statement.executeUpdate(commentStatement);
        logMessage = "SQL returned >" + Integer.toString(sqlResult) + "<.";
        log(myName, Constants.DEBUG, myArea, logMessage);
        statement.close();

        connection.close();
        rc = Constants.OK;
        setError(Constants.OK,Constants.NOERRORS);
        
    } catch (SQLException e) {
        myArea = "Exception handling";
        logMessage = "SQLException at >" + myName + "<. Error =>" + e.toString() + "<.";
        log(myName, Constants.ERROR, myArea, logMessage);
        setError(Constants.ERROR, logMessage);
        rc = Constants.ERROR;
    }
    return rc;
    
    }

    public String errorMessage() {
        return errorMessage;
    }

    public String tableNameFor(String inTableName) {
        return Constants.TABLE_PREFIX + inTableName;
    }

    public boolean tableExists(String inTableName) {
        String myName = "tableExists";
        String myArea = "init";
        String logMessage = Constants.NOT_INITIALIZED;
        String tableName = Constants.NOT_INITIALIZED;
        String nrTablesFound = Constants.NOT_INITIALIZED;
        
        if(tableName.indexOf(Constants.TABLE_PREFIX) >-1) {
            tableName = inTableName;
        } else {
            tableName = Constants.TABLE_PREFIX + inTableName;
        }
        
        readParameterFile();
        if (databaseType.equals("Oracle")) {
            query = "SELECT count(*) tblcount FROM user_tables WHERE table_name ='" + tableName + "'";
        } else {
            logMessage = "databaseType >" + databaseType + "< not yet supported";
            log(myName, "info", myArea, logMessage);
            setError(Constants.ERROR,logMessage);
            return false;
        }
        GetSingleValue dbCol = new GetSingleValue(className);
        dbCol.setDatabaseName(databaseName);
        dbCol.setQuery(query);
        dbCol.setLogLevel(getLogLevel());
        nrTablesFound = dbCol.getColumn();

        if (nrTablesFound.equals("1"))
            return true;
        setError(Constants.OK,"Table not found.");
        return false;

    }

    public boolean userHasPrivilege(String inUser, String inTableName) {

        return false;
    }

    public String synonymForUser(String inTableName, String inUserName) {

        return Constants.NOT_IMPLEMENTED;
    }

    public void setDatabaseName(String dbName) {
        this.databaseName=dbName;
    }
    public String getDatabaseName() {
        return databaseName;
    }
    public void setTableComment(String comm) {
        this.tableComment = comm;
    }
    public String getTableComment() {
        return tableComment;
    }

    public void readParameterFile() {
        String myName = "readParameterFile";
        String myArea = "reading parameters";
        String logMessage = Constants.NOT_INITIALIZED;

        databaseType = GetParameters.GetDatabaseType(databaseName);
        databaseConnDef = GetParameters.GetDatabaseConnectionDefinition(databaseName);
        driver = GetParameters.GetDatabaseDriver(databaseType);
        url = GetParameters.GetDatabaseURL(databaseConnDef);
        userId = GetParameters.GetDatabaseUserName(databaseName);
        password = GetParameters.GetDatabaseUserPWD(databaseName);
        tableOwner = GetParameters.GetDatabaseTableOwnerName(databaseName);
        tableOwnerPassword = GetParameters.GetDatabaseTableOwnerPWD(databaseName);

        logMessage = "databaseType >" + databaseType + "<.";
        log(myName, "info", myArea, logMessage);
        logMessage = "connection >" + databaseConnDef + "<.";
        log(myName, "info", myArea, logMessage);
        logMessage = "driver >" + driver + "<.";
        log(myName, "info", myArea, logMessage);
        logMessage = "url >" + url + "<.";
        log(myName, "info", myArea, logMessage);
        logMessage = "userId >" + userId + "<.";
        log(myName, "info", myArea, logMessage);
        logMessage = "tblowner >" + tableOwner + "<.";
        log(myName, "info", myArea, logMessage);
    }

    private void log(String name, String level, String location, String logText) {
           if(Constants.logLevel.indexOf(level.toUpperCase()) > getIntLogLevel()) {
               return;
           }

            Logging.LogEntry(logFileName, name, level, location, logText);  
       }

    public String getLogFilename() {
        return logFileName + ".log";
    }

    /**
     * @param level
     */
    public void setLogLevel(String level) {
       String myName ="setLogLevel";
       String myArea ="determineLevel";
       
       logLevel =Constants.logLevel.indexOf(level.toUpperCase());
       if (logLevel <0) {
           log(myName, Constants.WARNING, myArea,"Wrong log level >" + level +"< specified. Defaulting to level 3.");
           logLevel =3;
       }
       
       log(myName,Constants.INFO,myArea,"Log level has been set to >" + level +"< which is level >" +getIntLogLevel() + "<.");
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

    private void setError(String errorCode, String errorMessage) {
        this.errorCode =errorCode;
        this.errorMessage = errorMessage;
    }
    public String getErrorCode() {
        return errorCode;
    }
    public String getErrorMessage() {
        return errorMessage;
    }
}
