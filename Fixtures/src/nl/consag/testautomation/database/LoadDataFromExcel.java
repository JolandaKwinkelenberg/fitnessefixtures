/**
 * This purpose of this fixture is to insert a number of database rows using the FitNesse 'decision' table and an excel spreadsheet.
 * The input parameters are provided by a table in the FitNesse wiki. 
 * @author Edward Crain
 * @since 21 March 2015
 * @version 20160108.0 : Added result of readWorksheet to log and check on ResultMessage. Removed some copy-paste bugs in log messages.
 * @version 20170908.0 : bugfix for empty excel cells
 * @version 20170909.0 : Made commitsize configurable
 */
package nl.consag.testautomation.database;

import java.sql.*;
//import java.sql.Date;

import java.text.DateFormat;
//import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.*;

//import org.apache.poi.ss.usermodel.Cell;
//import org.apache.poi.ss.usermodel.DateUtil;

import nl.consag.supporting.Constants;
import nl.consag.supporting.Logging;
import nl.consag.testautomation.supporting.Attribute;
import nl.consag.testautomation.supporting.ExcelFile;
import nl.consag.supporting.GetParameters;

public class LoadDataFromExcel {

    private static String version = "20170909.0";
    private int logLevel = 3;

    private String className = "LoadDataFromExcel";
    private String logFileName = Constants.NOT_INITIALIZED;
    private String context = Constants.DEFAULT;
    private String startDate = Constants.NOT_INITIALIZED;
    private boolean firstTime = true;

    private String driver;
    private String url;
    private String userId;
    private String password;

    private String tableOwner = Constants.NOT_INITIALIZED;
    private String tableOwnerPassword = Constants.NOT_INITIALIZED;
    private String tableName;
    private String databaseName;
    private String inputFile;
    private int numberOfColumns;
    private String concatenatedDatabaseColumnNames; // variables used to create insert query
    private String concatenatedBindVariables; // variables used to create insert query
    private String databaseType;
    private String databaseConnDef;
    private int commitSize =Constants.DEFAULT_COMMIT_SIZE_INSERT;
    private int arraySize =Constants.DEFAULT_ARRAY_SIZE_UPDATE;

    private List<List<Attribute>> tableExcelFile;
    private String worksheetName;

    public String returnMessage = "";
    private String errorCode = Constants.OK;
    private String errorMessage = Constants.NOERRORS;
    private HashMap<Integer,String> previousCellFormatList =new HashMap<Integer,String>();
    
    private String appName=Constants.UNKNOWN;

    public LoadDataFromExcel(String pContext) {
        java.util.Date started = new java.util.Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        startDate = sdf.format(started);
        context = pContext;
    }

    public LoadDataFromExcel(String pContext, String logLevel) {
        java.util.Date started = new java.util.Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        startDate = sdf.format(started);
        context = pContext;
        setLogLevel(logLevel);
    }
    
    public LoadDataFromExcel(String pContext, String logLevel, String appName) {
        java.util.Date started = new java.util.Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        startDate = sdf.format(started);
        context = pContext;
        setLogLevel(logLevel);
        setAppName(appName);
    }

    public LoadDataFromExcel() {
        java.util.Date started = new java.util.Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        startDate = sdf.format(started);
        context = className;
    }

    public void setDatabaseName(String databasename) {
        String myName = "setDatabaseName";
        String myArea = "init";
        String logMessage = Constants.NOT_INITIALIZED;

        this.databaseName = databasename;
        logMessage = "database name >" + databaseName + "<.";
        log(myName, Constants.DEBUG, myArea, logMessage);
    }

    public void setTableName(String tableName) {
        String myName = "setTableName";
        String myArea = "init";
        String logMessage = Constants.NOT_INITIALIZED;

        this.tableName = tableName;
        logMessage = "table name >" + tableName + "<.";
        log(myName, Constants.DEBUG, myArea, logMessage);
    }

    public void setInputFile(String inputFile) {
        String myName = "setInputFile";
        String myArea = "init";
        String logMessage = Constants.NOT_INITIALIZED;

        this.inputFile = determineCompleteFileName(inputFile);
        logMessage = "inputFile >" + inputFile + "<.";
        log(myName, Constants.DEBUG, myArea, logMessage);
    }

    public void setWorksheetName(String worksheetName) {
        String myName = "setWorksheetName";
        String myArea = "init";
        String logMessage = Constants.NOT_INITIALIZED;

        this.worksheetName = worksheetName;
        logMessage = "worksheet name >" + worksheetName + "<.";
        log(myName, Constants.DEBUG, myArea, logMessage);
    }

    public String result() {
        //Function submit the truncate SQL statement
        String myName = "insertQuery-result";
        String myArea = "init";
        String logMessage = Constants.NOT_INITIALIZED;
        String rc = Constants.OK;
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        String insertTableSQL;
        int commitCounter =0;
        int arrayCounter =0;
        int rowCounter =0;

        myArea = "read Parameter";
        readParameterFile();
        myArea = "read Excel";
        rc = readExcelFile();
        if (tableExcelFile.size() == 0 || !Constants.OK.equals(rc)) {
            logMessage =
                "No data available, Excel file or sheet not found. Message from Excel read method >" + rc + "<.";
            log(myName, Constants.ERROR, myArea, logMessage);
            setErrorMessage(Constants.ERROR, logMessage);
            return Constants.ERROR;
        }
        getDatabaseColumnNames();

        logMessage = "Method: InsertQuery.";
        log(myName, Constants.DEBUG, myArea, logMessage);

        try {
            myArea = "SQL Execution";
            logMessage = "Connecting to >" + databaseConnDef + "< using userID >" + tableOwner + "<.";
            log(myName, Constants.DEBUG, myArea, logMessage);
            connection = DriverManager.getConnection(url, tableOwner, tableOwnerPassword);
            connection.setAutoCommit(false); //commit transaction manually*
            insertTableSQL =
                "INSERT INTO " + tableName + " (" + concatenatedDatabaseColumnNames + ")  VALUES (" +
                concatenatedBindVariables + ")";
            logMessage = "SQL >" + insertTableSQL + "<.";
            log(myName, Constants.DEBUG, myArea, logMessage);

            for (int row = 1; row < tableExcelFile.size(); row++) {
                commitCounter++;
                arrayCounter++;
                rowCounter++;
                //TODO: Prepare only once
                logMessage = "prepare statement cell no >" + String.valueOf(row) + "<.";
                log(myName, Constants.VERBOSE, myArea, logMessage);
                preparedStatement = connection.prepareStatement(insertTableSQL);
                logMessage = "prepared.";
                log(myName, Constants.VERBOSE, myArea, logMessage);
                int bindVariableNo = 0;
                int currentRowSize = tableExcelFile.get(row).size();
                logMessage = "Row #" + Integer.toString(rowCounter) + " has >"  + Integer.toString(currentRowSize) + "< column(s) populated.";
                log(myName, Constants.DEBUG, myArea, logMessage);
//                for (int cell = 0; cell < numberOfColumns; cell++) {
                for (int cell = 0; cell < currentRowSize; cell++) {
                    bindVariableNo++;
                    logMessage = "Binding variable# >" + Integer.toString(bindVariableNo) + "<.";
                    log(myName, Constants.VERBOSE, myArea, logMessage);
                    Attribute attribute = new Attribute();
                    attribute = tableExcelFile.get(row).get(cell);
                    //if a cell is empty, treat is as NULL value if the format is not varchar
                    //if ("".equals(tableExcelFile.get(row).get(cell))) {
                    //    log(myName, Constants.DEBUG, myArea, "Empty string detected. Setting column to NULL.");
                    //    attribute.setText(Constants.SIEBEL_NULL_VALUE);
                    //}
                    log(myName,Constants.VERBOSE, myArea,"Cell string value >" + attribute.getText() +"<.");
                    log(myName,Constants.VERBOSE, myArea,"Cell number value >" + attribute.getNumber() +"<.");
                    log(myName,Constants.VERBOSE, myArea,"Cell date value >" + attribute.getDate() +"<.");
                    log(myName,Constants.VERBOSE, myArea,"Cell format >" + attribute.getFormat() +"<.");
                    if (Constants.EXCEL_CELLFORMAT_UNKNOWN.equals(attribute.getFormat())) {
                        log(myName,Constants.DEBUG, myArea,"Using previous cell format >" + getPreviousCellFormat(bindVariableNo) + "<for bindvariable >" +Integer.toString(bindVariableNo)
                                                           +"<.");
                        attribute.setFormat(getPreviousCellFormat(bindVariableNo));
                    } else {
                        setPreviousCellFormat(bindVariableNo,attribute.getFormat());
                    }
                    if (Constants.SIEBEL_NULL_VALUE.equals(attribute.getText())) {
                        log(myName, Constants.DEBUG, myArea, "Null string detected. Setting columns to NULL.");
                        switch (attribute.getFormat()) {
                        case Constants.EXCEL_CELLFORMAT_NUMERIC:
                            preparedStatement.setNull(bindVariableNo, Types.DOUBLE);
                            break;
                        case Constants.EXCEL_CELLFORMAT_DATE:
                            preparedStatement.setNull(bindVariableNo, Types.DATE);
                            break;
                        default:
                            preparedStatement.setNull(bindVariableNo, Types.VARCHAR);
                            break;
                        }
                    } else {
                        switch (attribute.getFormat()) {
                        case Constants.EXCEL_CELLFORMAT_NUMERIC:
                            preparedStatement.setDouble(bindVariableNo, attribute.getNumber());
                            break;
                        case Constants.EXCEL_CELLFORMAT_DATE:
                            if(attribute.getDate() == null) {
                                preparedStatement.setNull(bindVariableNo, Types.DATE);
                            } else {
                                java.sql.Timestamp timestamp = new java.sql.Timestamp(attribute.getDate().getTime());
                                preparedStatement.setTimestamp(bindVariableNo, timestamp);
                            }
                            break;
                        default:
                                myArea="binding";
                                log(myName, Constants.VERBOSE, myArea, "length >" + attribute.getText().length() +"<. getText =>" +attribute.getText() +"<.");
                                log(myName, Constants.VERBOSE, myArea, "Trimmed length >" + attribute.getText().trim().length() +"<. getText Trimmed =>" +attribute.getText().trim() +"<.");
                                if (Constants.DATABASETYPE_DB2.equals(getDatabaseType())) {
                                    // JDBC Driver Db2 does not yet support setNString
                                    log(myName, Constants.VERBOSE, myArea, "Database type is >" + getDatabaseType() +"<. Using setString.");
                                    preparedStatement.setString(bindVariableNo, attribute.getText().trim());
                                } else {
                                    log(myName, Constants.VERBOSE, myArea, "Database type is >" + getDatabaseType() +"<. Using setNString.");
                                    preparedStatement.setNString(bindVariableNo, attribute.getText().trim());
                                }
                            break;
                            }
                        
                    }
                    logMessage = "Done.";
                    log(myName, Constants.VERBOSE, myArea, logMessage);
                } //for all populated columns

                //remaining columns
                for (int cell = currentRowSize; cell < numberOfColumns; cell++) {
                    bindVariableNo++;
                    logMessage = "Setting remaining bind variable# >" + Integer.toString(bindVariableNo) + "< to NULL.";
                    log(myName, Constants.VERBOSE, myArea, logMessage);
                    //TODO: Determine datatype
                    preparedStatement.setNull(bindVariableNo, Types.VARCHAR);
                }
                //for all columns not populated
                
                logMessage = "All bind variables added. Adding batch...";
                log(myName, Constants.VERBOSE, myArea, logMessage);

                preparedStatement.addBatch(); 
                //TODO: enable array inserts by not executing every single record
                
                logMessage = "Batch added. Executing batch...";
                log(myName, Constants.DEBUG, myArea, logMessage);
                preparedStatement.executeBatch();
                
                //TODO: Let user determine commit size
                //TODO: Set default per application in properties file
                if (commitCounter == commitSize) { // if more than x rows, perform the commit to database
                    logMessage = "Commit for (another) > " + String.valueOf(commitCounter) + " rows<.";
                    log(myName, Constants.INFO, myArea, logMessage);
                    connection.commit();
                    logMessage = "Commit done.";
                    log(myName, Constants.VERBOSE, myArea, logMessage);
                    commitCounter =0;
                    arrayCounter =0;
                }
            }
            //TODO: Handle remaining rows in Array Insert
            
            if (commitCounter > 0) {
                logMessage = "Commit to insert remaining rows in database > " + String.valueOf(commitCounter) + " rows<.";
                log(myName, Constants.INFO, myArea, logMessage);
                connection.commit();
                logMessage = "Commit done.";
                log(myName, Constants.DEBUG, myArea, logMessage);

            }
            connection.close();
            returnMessage = Constants.OK;
        } catch (SQLException e) {
            myArea = "Exception handling";
            logMessage = "SQLException processing data >" + e.toString() + "<.";
            log(myName, Constants.ERROR, myArea, logMessage);
            returnMessage = logMessage;
            setErrorMessage(Constants.ERROR, logMessage);
        }
        logMessage = "Message returning to FitNesse > " + returnMessage + "<.";
        log(myName, Constants.INFO, myArea, logMessage);
        return returnMessage;
    }

    public void getDatabaseColumnNames() {
        //Function to read the names of the database columns	
        Attribute attribute = new Attribute();
        DateFormat df = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss");
        for (int i = 0; i < tableExcelFile.get(0).size(); i++) { // next column names
            attribute = tableExcelFile.get(0).get(i); //first column name
            if (i == 0) {
                if (attribute.getFormat() == "NUMERIC") {
                    concatenatedDatabaseColumnNames = String.valueOf(attribute.getNumber());
                } else {
                    if (attribute.getFormat() == "DATE") {
                        String text = df.format(attribute.getDate());
                        concatenatedDatabaseColumnNames = text;
                    } else { //String, boolean and others
                        concatenatedDatabaseColumnNames = attribute.getText();
                    }
                }
                concatenatedBindVariables = "?";
                numberOfColumns = 1;
            } else {
                if (attribute.getFormat() == "NUMERIC") {
                    concatenatedDatabaseColumnNames =
                        concatenatedDatabaseColumnNames + "," + String.valueOf(attribute.getNumber());
                } else {
                    if (attribute.getFormat() == "DATE") {
                        String text = df.format(attribute.getDate());
                        concatenatedDatabaseColumnNames = concatenatedDatabaseColumnNames + "," + text;
                    } else { //String, boolean and others
                        concatenatedDatabaseColumnNames = concatenatedDatabaseColumnNames + "," + attribute.getText();
                    }
                }
                concatenatedBindVariables = concatenatedBindVariables + ",?";
                numberOfColumns++;
            }
        }
    }

    public void readParameterFile() {
        //Function to read the parameters in a parameter file
        String myName = "readParameterFile";
        String myArea = "reading";
        String logMessage = Constants.NO;
        String result =Constants.NOT_FOUND;

        databaseType = GetParameters.GetDatabaseType(databaseName);
        logMessage = "Database type >" + databaseType +"<.";
        log(myName, Constants.INFO, myArea, logMessage);

        databaseConnDef = GetParameters.GetDatabaseConnectionDefinition(databaseName);
        logMessage = "Database connection definition: " + databaseConnDef;
        log(myName, Constants.INFO, myArea, logMessage);

        driver = GetParameters.GetDatabaseDriver(databaseType);
        logMessage = "driver: " + driver;
        log(myName, Constants.INFO, myArea, logMessage);
        url = GetParameters.GetDatabaseURL(databaseConnDef);

        tableOwner = GetParameters.GetDatabaseTableOwnerName(databaseName);
        logMessage = "Table Owner UserID >" + tableOwner + "<.";
        log(myName, Constants.INFO, myArea, logMessage);

        tableOwnerPassword = GetParameters.GetDatabaseTableOwnerPWD(databaseName);
        logMessage = "Password for user >" + tableOwner + "< retrieved.";
        log(myName, Constants.INFO, myArea, logMessage);

        userId = GetParameters.GetDatabaseUserName(databaseName);
        logMessage = "User UserID >" + userId + "<.";
        log(myName, Constants.INFO, myArea, logMessage);
        password = GetParameters.GetDatabaseUserPWD(databaseName);
        logMessage = "Password for user >" + userId + "< retrieved.";
        log(myName, Constants.INFO, myArea, logMessage);

        //Commit size
        result =GetParameters.getPropertyVal(Constants.FIXTURE_PROPERTIES, getAppName(), this.className, Constants.PARAM_COMMIT_SIZE_INSERT);
        if(Constants.NOT_FOUND.equals(result)) {
            setCommitSize(Constants.DEFAULT_COMMIT_SIZE_INSERT);
        }
        else {
            setCommitSize(Integer.parseInt(result));
        }
        log(myName, Constants.DEBUG, myArea, "Commit size >" + Integer.toString(getCommitSize()) +"<.");

    }
    
    public String getDatabaseType() {
        return databaseType;
    }

    public String readExcelFile() {
        //Function to read the excel file
        String myName = "readExcelFile";
        String myArea = "reading";
        String logMessage = Constants.NO;

        ExcelFile excelFile = new ExcelFile();
        tableExcelFile = excelFile.readWorksheetWithNameIncludingFormat(inputFile, worksheetName);
        logMessage = "Excel file >" + inputFile + "< retrieved with message >" + excelFile.getReturnMessage() + "<.";
        log(myName, Constants.INFO, myArea, logMessage);

        return excelFile.getReturnMessage();
    }

    private String determineCompleteFileName(String dirAndFile) {
        String[] dirAndFileSeparated;
        String sCompleteFileName;
        String dir = "FROMROOTDIR_UNKNOWN";

        dirAndFileSeparated = dirAndFile.split(" ", 2);

        dir = GetParameters.GetRootDir(dirAndFileSeparated[0]);
        sCompleteFileName = dir + "/" + dirAndFileSeparated[1];

        return sCompleteFileName;

    }

    /**
     * @param level to which logging should be set. Must be VERBOSE, DEBUG, INFO, WARNING, ERROR or FATAL. Defaults to INFO.
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
     * @return - the log level
     */
    public String getLogLevel() {
        return Constants.logLevel.get(getIntLogLevel());
    }

    /**
     * @return - the log level as Integer data type
     */
    public Integer getIntLogLevel() {
        return logLevel;
    }

    public void log(String name, String level, String area, String logMessage) {
        if (Constants.logLevel.indexOf(level.toUpperCase()) > getIntLogLevel()) {
            return;
        }

        if (firstTime) {
            firstTime = false;
            if (context.equals(Constants.DEFAULT)) {
                logFileName = startDate + "." + className;
            } else {
                logFileName = context + "." + startDate;
            }
            Logging.LogEntry(logFileName, className, Constants.INFO, "Fixture version >" + getVersion() + "<.");
        }
        Logging.LogEntry(logFileName, name, level, area, logMessage);
    }

    public String getLogFilename() {
        return logFileName;
    }

    public static String getVersion() {
        return version;
    }

    private void setErrorMessage(String err, String logMessage) {
        errorCode = err;
        errorMessage = logMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getErrorCode() {
        return errorCode;
    }

    private void setPreviousCellFormat(int bindVariableNo, String cellFormat) {
        previousCellFormatList.put(bindVariableNo, cellFormat);
    }

    private String getPreviousCellFormat(int bindVariableNo) {
        if(previousCellFormatList.containsKey(bindVariableNo)) {
            return previousCellFormatList.get(bindVariableNo);
        } else {
            return Constants.EXCEL_CELLFORMAT_UNKNOWN;
        }
    }

    public void setCommitSize(int i) {
        commitSize=i;
    }

    private int getCommitSize() {
        return commitSize;
    }

    private void setAppName(String appName) {
        this.appName=appName;
    }

    private String getAppName() {
        return appName;
    }
}
