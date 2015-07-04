/**
 * This purpose of this fixture is to get a single value from a db query
 * It will only return one value, begin the first column of the first record found. 
 * The fixture does not through errors when multiple columns or records are returned.
 * The input parameters are provided by a table in the FitNesse wiki. 
 * @author Jac Beekers
 * @version 10 May 2015
 */
package database;

import supporting.*;

import java.sql.*;

import java.text.SimpleDateFormat;

import java.util.*;


/**
 */
public class GetSingleValue {

    private String className = "GetSingleValue";
    private String logFileName = Constants.NOT_INITIALIZED;
    private String context = Constants.NOT_INITIALIZED;
    private String startDate = Constants.NOT_INITIALIZED;
    
    private String result=Constants.OK;
    private String errorMessage=Constants.OK;
    private int logLevel =3;

    private String driver;
    private String url;
    private String userId;
    private String password;
    private String databaseName;
    private String query;
    private String databaseType;
    private String databaseConnection;

    private int numberTableColumns;

    //the return table, returns the outcome of fixture (="pass", "fail", "ignore", "no change")
    private List<List<String>> return_table = new ArrayList<List<String>>();
   
    private String return_message = ""; //text message that is returned to fitnesse

    public GetSingleValue() {
        java.util.Date started = new java.util.Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        startDate = sdf.format(started);
        context = className;
        logFileName = startDate + "." + className ;

    }

    /**
     * @param inContext
     */
    public GetSingleValue(String inContext) {
        java.util.Date started = new java.util.Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        startDate = sdf.format(started);
        context = inContext;
        logFileName = startDate + "." + className +"." + context;

    }


    /**
     * @param databaseName
     */
    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }


    /**
     * @param query
     */
    public void setQuery(String query) {
        this.query = query;
    }


    /**
     * @return
     */
    public String getColumn() {

        //Get parameters from file
        readParameterFile();
        //Get the #records
        return runQuery(Constants.FIRST_ONLY,1);
    }
    
    public String getList() {
        return getList("1");
    }

    public String getList(String nrColumns) {
        String myName="getList";
        String myArea="proc";
        readParameterFile();
        log(myName, Constants.DEBUG, myArea, "NrColumns =>" + nrColumns +"<. Parsed to int =>" + Integer.parseInt(nrColumns) +"<.");
        return runQuery(Constants.ALL,Integer.parseInt(nrColumns));
    }

    /**
     * Function to read first row of table and set database name.
     * @param input_row
     */
    public void getDatabaseName(List<String> input_row) {
        String myName = "getDatabaseName";
        String myArea = "init";
        String logMessage = Constants.NOT_INITIALIZED;

        List<String> return_row = new ArrayList<String>();
        databaseName = input_row.get(1); //read first row second column

        logMessage = "database name: " + databaseName;
        log(myName, Constants.DEBUG, myArea, logMessage);

        addRowToReturnTable(return_row);
    }

    /**
     * Function to compare input table with database table.
     * @param expected_table
     * @param database_table
     */
    public void CompareExpectedTableWithDatabaseTable(List<List<String>> expected_table,
                                                      List<List<String>> database_table) {
        //empty row is used, when row is filled with nulls.


        // Expected result is equal or greater than result rows from database, the expected table size is - 3, since first 3 rows contain database name, query and column names
        for (int i = 0; i < (expected_table.size()); ++i) { // less rows in database than expected
            CompareExpectedRowWithDatabaseRow(expected_table.get(i), database_table.get(i));
        }
    }

    /**
     * Function to compare input row with database row.
     * @param expected_row
     * @param database_row
     */
    public void CompareExpectedRowWithDatabaseRow(List<String> expected_row, List<String> database_row) {
        List<String> return_row = new ArrayList<String>();

        //If expected row is empty
        if (expected_row.get(0).equals("")) {
            if (database_row.get(0).equals("")) {
                //do nothing
            } else {
                return_row.add("fail: Found extra row"); //return "failed" to first cell which only contains the value "column name"
                for (int i = 1; i < numberTableColumns; ++i) { // set value for next column cell
                    return_row.add("fail: surplus: " + database_row.get(i)); //return "fail" in next cell with the found value

                }

            }
        } else {
            return_row.add("pass"); //return "pass" in first cell with value "column name"
            //If database row is empty
            if (database_row.get(0).equals("")) {
                for (int i = 1; i < numberTableColumns; ++i) { // set value for next column cell
                    return_row.add("fail: expected: " + expected_row.get(i)); //return "fail" in next cell with the found value

                }
            } else { //Compare cell for cell if expected equals outcome
                for (int i = 1; i < numberTableColumns; ++i) { // set value for next column cell
                    if (expected_row.get(i).equals(database_row.get(i))) {
                        return_row.add("pass"); //return "pass" in next cell if expected = result
                    } else {
                        return_row.add("fail: expected: " + expected_row.get(i) + " found: " +
                                       database_row.get(i)); //return "fail" in next cell with the found value
                    }
                }
            }
        }
        addRowToReturnTable(return_row); //return row with outcomes; pass/fail
    }

    /**
     * Function to insert statement based on input in fitnesse table row >=3.
     * @return
     */
    public String runQuery(String queryMode, int nrColumns) {
        //attributes for internal database table
        //			 List<List<String>> database_table = new ArrayList<List<String>>();

        //attributes for reading database
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        String sResult0 = Constants.NOT_FOUND;
        boolean bRecordFound = false;

        String myName = "runQuery";
        String myArea = "init";
        String logMessage = Constants.NOT_INITIALIZED;

        try {
            myArea = "runQuery";
            // Create a connection to the database
            connection = DriverManager.getConnection(url, userId, password);
            // createStatement() is used for create statement object that is used for sending sql statements to the specified database.
            statement = connection.createStatement();
            // sql query of string type to read database
            logMessage = "Select query >" + query + "<.";
            log(myName, "debug", myArea, logMessage);

            resultSet = statement.executeQuery(query);

            if ("Netezza".equals((databaseType))) {
                // isClosed does not work for Netezza
                logMessage = "Skipping isClosed as this is Netezza...";
                log(myName, "debug", myArea, logMessage);
            } else {
                if (resultSet.isClosed()) {
                    logMessage = "isClosed is true. Result will be set to >" + sResult0 + "<.";
                    log(myName, "debug", myArea, logMessage);
                    connection.close();
                    return sResult0;
                } else {
                    logMessage = "isClosed is not true";
                    log(myName, Constants.DEBUG, myArea, logMessage);
                }
            }

            bRecordFound = resultSet.next();
            if (!bRecordFound) {
                sResult0 = "0";
                logMessage = "No records found. Result will be set to >" + sResult0 + "<.";
                log(myName, "debug", myArea, logMessage);
                setResult(Constants.OK);
                setErrorMessage(logMessage);
                return sResult0;
            }

            logMessage = "Record found. Will create a list using queryMode =>" + queryMode +"< and nrColumns =>" + nrColumns +"<." ;
            log(myName, Constants.DEBUG, myArea, logMessage);

            if(Constants.FIRST_ONLY.equals(queryMode)) {
                sResult0 = resultSet.getString(1);
                logMessage = "Found >" + sResult0 + "<.";
                log(myName, Constants.DEBUG, myArea, logMessage);

                for(int i=2 ; i <= nrColumns ; i++) {
                    sResult0 = sResult0 + Constants.QUERY_DELIMITER + " " + resultSet.getString(i);
                }

                return_message = sResult0;
                setResult(Constants.OK);
                
            } else {
                if(Constants.ALL.equals(queryMode)) {
                    sResult0 = resultSet.getString(1);
                    logMessage = "Found first value >" + sResult0 + "<.";
                    log(myName, Constants.DEBUG, myArea, logMessage);

                    for(int i=2 ; i <= nrColumns ; i++) {
                        sResult0 = sResult0 + Constants.QUERY_DELIMITER + " " + resultSet.getString(i);
                    }
                    
                    while(resultSet.next()) {
                        sResult0 = sResult0 + Constants.QUERY_DELIMITER + " " + resultSet.getString(1);
                        for(int i=2 ; i <= nrColumns ; i++) {
                            sResult0 = sResult0 + Constants.QUERY_DELIMITER + " " + resultSet.getString(i);
                        }
                    }
                    logMessage="Concatenated DB result =>" + sResult0 + "<.";
                    log(myName, Constants.DEBUG, myArea, logMessage);
                    return_message=sResult0;
                    setResult(Constants.OK);
                } else {
                    //unknown queryMode
                    logMessage="Internal error. Invalid query mode >" + queryMode + "<.";
                    log(myName, Constants.FATAL, myArea, logMessage);
                    setResult(Constants.ERROR);
                    setErrorMessage(logMessage);
                }
            }
            
            

            statement.close();
            connection.close();
        } catch (SQLException e) {
            myArea = "exception handling";
            logMessage = "SQLException >" + e.toString() + "<.";
            log(myName, "ERROR", myArea, logMessage);
            return_message = logMessage;
            setResult(Constants.ERROR);
            setErrorMessage(logMessage);
        }
        return return_message;
    }

    /**
     * Function to add row to return table; a row contains cells with either "pass" (= green), or "fail" (= red).
     * @param row
     */
    public void addRowToReturnTable(List<String> row) {
        return_table.add(row);
    }

    /**
     * Function to read the parameters in a parameter file.
     */
    public void readParameterFile() {
        String myName = "readParameterFile";
        String myArea = "reading";
        String logMessage = Constants.NOT_INITIALIZED;

        databaseType = GetParameters.GetDatabaseType(databaseName);
        logMessage = "Database type: " + databaseType;
        log(myName, "debug", myArea, logMessage);
        databaseConnection = GetParameters.GetDatabaseConnectionDefinition(databaseName);
        logMessage = "Database connection definition: " + databaseConnection;
        log(myName, "debug", myArea, logMessage);

        driver = GetParameters.GetDatabaseDriver(databaseType);
        logMessage = "driver: " + driver;
        log(myName, "debug", myArea, logMessage);
        url = GetParameters.GetDatabaseURL(databaseConnection);
        logMessage = "url for database >" + databaseName +"< is >" + url +"<.";
        log(myName, "debug", myArea, logMessage);
        
        userId = GetParameters.GetDatabaseUserName(databaseName);
        logMessage = "user_id: " + userId;
        log(myName, "debug", myArea, logMessage);

        password = GetParameters.GetDatabaseUserPWD(databaseName);

    }

    private void log(String name, String level, String location, String logText) {
           if(Constants.logLevel.indexOf(level.toUpperCase()) > getIntLogLevel()) {
               return;
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

    public String getResult() {
        return result;
    }
    
    private void setResult(String code) {
        result=code;
    }

    private void setErrorMessage(String logMessage) {
        errorMessage=logMessage;
    }
    public String getErrorMessage() {
        return errorMessage;
    }
}

