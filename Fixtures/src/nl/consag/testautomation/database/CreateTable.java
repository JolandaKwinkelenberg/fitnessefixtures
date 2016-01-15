/**
 * This purpose of this fixture is to a temporary table based on a database query or using by specifying columns
 * By design, the table name is ALWAYS prefixed
 * The input parameters are provided by a script in the FitNesse wiki. 
 * @author Jac Beekers
 * @since 10 May 2015
 * @version 20160106.1
 * 
 */
package nl.consag.testautomation.database;

import java.sql.*;

import java.text.*;

import java.util.*;
import nl.consag.supporting.Constants;
import nl.consag.supporting.Logging;
import nl.consag.supporting.GetParameters;

public class CreateTable {
    private String version ="20160106.1";

    private int logLevel =3;
    private int logEntries =0;

    private String className = "CreateTable";
    private String logFileName = Constants.NOT_INITIALIZED;
    private String context = Constants.DEFAULT;
    private String startDate = Constants.NOT_INITIALIZED;

    private String driver;
    private String url;
    private String userId;
    private String password;
    private String databaseConnection;
    private String query;
    private String databaseType;
    private String databaseConnDef;
    private String tableOwner;
    private String tableOwnerPassword;
    private String tableComment = Constants.TABLE_COMMENT;
    private String errorMessage = Constants.NO_ERRORS;
    private String errorCode=Constants.OK;
    private String idaaName =Constants.NOT_PROVIDED;
    private String databaseName =Constants.NOT_PROVIDED;
    private String tableName =Constants.NOT_PROVIDED;
    private List<colDefinition> columns = new ArrayList<colDefinition>();


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
        String myName="createTable";
        String myArea="run";
        String logMessage=Constants.NOT_INITIALIZED;
        String sqlStatement =Constants.NOT_INITIALIZED;
        String commentStatement =Constants.NOT_INITIALIZED;
        String rc =Constants.OK;
        
        sqlStatement="CREATE TABLE " +tableName +"(" +columnList +")";
        //If DB2 and Database name provided, add it
        //If DB2 and Accelerator name is provided, add it
        if(databaseType.equals("DB2")) {
            if(!getDatabase().equals(Constants.NOT_PROVIDED)) {
                sqlStatement +=" IN DATABASE " + getDatabase();
            }
            if(!getAccelerator().equals(Constants.NOT_PROVIDED)) {
                sqlStatement +=" IN ACCELERATOR " + getAccelerator();
            }
        }

        logMessage="Generated SQL statement is >" + sqlStatement +"<.";
        log(myName,Constants.DEBUG, myArea, logMessage);
        
        commentStatement = "comment on table " + tableName + " is '" + getTableComment() + "'";
        logMessage="Comment statement is >" + commentStatement +"<.";
        log(myName,Constants.DEBUG, myArea, logMessage);

        rc=execSQL(sqlStatement,commentStatement);
        return rc;
    }


    /**
     * @param inTableName
     * @param inDatabase - database connection (Note: this is NOT the database name as known in e.g. DB2, but the database connection in the properties file)
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

        setDatabaseConnection(inDatabase);

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
        switch (databaseType) {
        case "Oracle":
            query = "SELECT count(*) tblcount FROM user_tables WHERE table_name ='" + tableName + "'";
            break;
        case "DB2":
            //TODO: Decide how to handle DB2 LUW and DB2 z/OS. For now: assume z/OS.
            query = "SELECT count(*) tblcount FROM SYSIBM.SYSTABLES WHERE name ='" + tableName + "'";
            break;
        default:
            logMessage = "databaseType >" + databaseType + "< not yet supported";
            log(myName, "info", myArea, logMessage);
            setError(Constants.ERROR,logMessage);
            return false;
        }
        GetSingleValue dbCol = new GetSingleValue(className);
        dbCol.setDatabaseName(databaseConnection);
        dbCol.setQuery(query);
        dbCol.setLogLevel(getLogLevel());
        nrTablesFound = dbCol.getColumn();

        if (nrTablesFound.equals("1"))
            return true;
        setError(Constants.OK,"Table not found, or multiple occurrences found. NrTablesFound =>" +nrTablesFound +"<.");
        return false;

    }

    public boolean userHasPrivilege(String inUser, String inTableName) {

        return false;
    }

    public String synonymForUser(String inTableName, String inUserName) {

        return Constants.NOT_IMPLEMENTED;
    }

    public void setDatabaseConnection(String dbConn) {
        this.databaseConnection=dbConn;
    }
    public String getDatabaseConnection() {
        return databaseConnection;
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

        databaseType = GetParameters.GetDatabaseType(databaseConnection);
        databaseConnDef = GetParameters.GetDatabaseConnectionDefinition(databaseConnection);
        driver = GetParameters.GetDatabaseDriver(databaseType);
        url = GetParameters.GetDatabaseURL(databaseConnDef);
        userId = GetParameters.GetDatabaseUserName(databaseConnection);
        password = GetParameters.GetDatabaseUserPWD(databaseConnection);
        tableOwner = GetParameters.GetDatabaseTableOwnerName(databaseConnection);
        tableOwnerPassword = GetParameters.GetDatabaseTableOwnerPWD(databaseConnection);

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
        logEntries++;
        if(logEntries ==1) {
        Logging.LogEntry(logFileName, className, Constants.INFO, "Fixture version", getVersion());                 
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

    /**
     * @since 20160106.0
     * @return fixture version number
     */
    public String getVersion() {
        return version;
    }

    public void setTableName(String tblName) {
        this.tableName=tblName;
    }
    public String getTableName() {
        return this.tableName;
    }

    /**
     * @param dbName for DB2, the database name for the table
     */
    public void setDatabase(String dbName) {
        this.databaseName =dbName;
    }
    public String getDatabase() {
        return this.databaseName;
    }

    /**
     * @param accName - The name of the IBM DB2 Analytics Accelerator (IDAA)
     */
    public void setAccelerator(String accName) {
        this.idaaName = accName;
    }
    public String getAccelerator() {
        return this.idaaName;
    }
    public boolean addColumnDataType(String colName, String dataType) {
        String myName="addColumnDataType";
        String myArea="run";
        String logMessage=Constants.NO_ERRORS;
        
        logMessage="Adding column >" +colName+"< to list of columns...";
        log(myName, Constants.DEBUG, myArea, logMessage);
        colDefinition colDef = new colDefinition();
        colDef.colName=colName;
        colDef.dataType=dataType;
        columns.add(colDef);
        
        logMessage="Done. Current # columns: >" +columns.size() +"<.";
        log(myName, Constants.DEBUG, myArea, logMessage);

        return true;    
    }
    
    public boolean createTableIs(String tableName, String expectedResult) {
        String myName="createTableIs";
        String myArea="run";
        String logMessage=Constants.NOERRORS;
        String sqlStatement=Constants.NOT_INITIALIZED;
        String commentStatement=Constants.NOT_INITIALIZED;
        String rc=Constants.OK;
        String colList=Constants.NOT_PROVIDED;
        
        if(!getTableName().equalsIgnoreCase(Constants.NOT_PROVIDED) && !tableName.equalsIgnoreCase(getTableName())) {
            logMessage = "Table name has already been set to >" +getTableName() +"<, hence >" +tableName +"< is not applicable";
            if(expectedResult.equals(Constants.ERROR)) {
                logMessage="Expected error: " +logMessage;
                log(myName, Constants.INFO, myArea, logMessage);
                return true;
            }
            log(myName, Constants.ERROR, myArea, logMessage);
            setError(Constants.ERROR,logMessage);
            return false;
        }
        
        if(tableName.indexOf(Constants.TABLE_PREFIX) <0) {
            tableName = Constants.TABLE_PREFIX + tableName;
        }
        
        readParameterFile();
        
        myArea="Build create stmt";
        //Loop through columns
        Iterator iCol =columns.iterator();
        int i=0;
        while(iCol.hasNext()) {
            colDefinition cd =(colDefinition) iCol.next();
            i++;
            if(i==1) {
                colList=cd.colName +" " + cd.dataType;                
            } else {
                colList+="," + cd.colName +" " + cd.dataType;
            }
        }
        logMessage="Build column list for create statement =>" + colList +"<.";
        log(myName, Constants.DEBUG, myArea, logMessage);
        
        rc =createTable(tableName, colList);
        commentStatement = "comment on table " + tableName + " is '" + tableComment + "'";
        
        logMessage="createTable for >" +sqlStatement +"< returned >" +rc +"<.";
        log(myName, Constants.DEBUG, myArea, logMessage);

        if (expectedResult.equals(rc)) {
                logMessage="Provided expectedResult >" +expectedResult +"< equals createTable result.";
                log(myName, Constants.DEBUG, myArea, logMessage);
                return true;
            }
        logMessage="Provided expectedResult >" +expectedResult +"< does not match createTable result >" +rc +"<.";
        log(myName, Constants.ERROR, myArea, logMessage);
         return false;

    }


    private  class colDefinition {
        private  String colName;
        private  String dataType;
    }
}
