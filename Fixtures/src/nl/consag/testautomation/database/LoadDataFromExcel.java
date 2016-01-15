/**
 * This purpose of this fixture is to insert a number of database rows using the FitNesse 'decision' table and an excel spreadsheet.
 * The input parameters are provided by a table in the FitNesse wiki. 
 * @author Edward Crain
 * @since 21 March 2015
 * @version 20160108.0 : Added result of readWorksheet to log and check on ResultMessage. Removed some copy-paste bugs in log messages.
 * 
 */
package nl.consag.testautomation.database;


import java.io.*;

import java.sql.*;
import java.sql.Date;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.*;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;

import nl.consag.supporting.Constants;
import nl.consag.supporting.Logging;
import nl.consag.testautomation.supporting.Attribute;
import nl.consag.testautomation.supporting.ExcelFile;
import nl.consag.supporting.GetParameters;

public class LoadDataFromExcel {

    static protected String version = "20160108.0";

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

    private List<List<Attribute>> tableExcelFile;
    private String worksheetName;

    public String returnMessage = "";
    private String errorCode = Constants.OK;
    private String errorMessage = Constants.NOERRORS;

    public LoadDataFromExcel(String pContext) {
        java.util.Date started = new java.util.Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        startDate = sdf.format(started);
        context = pContext;
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
        int counter = 0;

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
                counter++;
                logMessage = "prepare statement cell no >" + String.valueOf(row) + "<.";
                log(myName, Constants.DEBUG, myArea, logMessage);
                preparedStatement = connection.prepareStatement(insertTableSQL);
                logMessage = "prepared.";
                log(myName, Constants.DEBUG, myArea, logMessage);
                int bindVariableNo = 0;
                for (int cell = 0; cell < numberOfColumns; cell++) {
                    bindVariableNo++;
                    logMessage = "Binding variable# >" + Integer.toString(bindVariableNo) + "<.";
                    log(myName, Constants.DEBUG, myArea, logMessage);
                    Attribute attribute = new Attribute();
                    attribute = tableExcelFile.get(row).get(cell);
                    if (attribute.getFormat() == "NUMERIC") {
                        preparedStatement.setDouble(bindVariableNo, attribute.getNumber());
                    } else {
                        if (attribute.getFormat() == "DATE") {
                            java.sql.Timestamp timestamp = new java.sql.Timestamp(attribute.getDate().getTime());
                            preparedStatement.setTimestamp(bindVariableNo, timestamp);
                        } else { //String, boolean and others
                            preparedStatement.setString(bindVariableNo, attribute.getText().trim());
                        }
                    }
                    logMessage = "Done.";
                    log(myName, Constants.DEBUG, myArea, logMessage);
                }
                logMessage = "All bind variables added. Adding batch...";
                log(myName, Constants.DEBUG, myArea, logMessage);

                preparedStatement.addBatch(); 
                logMessage = "Batch added. Executing batch...";
                log(myName, Constants.DEBUG, myArea, logMessage);
                preparedStatement.executeBatch();
                if (counter == 1000) { // if more than 1000 rows, perform the commit to database
                    logMessage = "Commit for (another) > " + String.valueOf(counter) + " rows<.";
                    log(myName, Constants.INFO, myArea, logMessage);
                    connection.commit();
                    logMessage = "Commit done.";
                    log(myName, Constants.DEBUG, myArea, logMessage);
                    counter = 0;
                }
            }
            if (counter > 0) {
                logMessage = "Commit to insert remaining rows in database > " + String.valueOf(counter) + " rows<.";
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

        databaseType = GetParameters.GetDatabaseType(databaseName);
        logMessage = "Database type: " + databaseType;
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

    public void log(String name, String level, String area, String logMessage) {
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

    public String getVersion() {
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

}
