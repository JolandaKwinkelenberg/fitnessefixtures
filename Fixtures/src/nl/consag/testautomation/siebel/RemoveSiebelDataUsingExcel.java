/**
 * This purpose of this fixture is to remove data, made available in an Excel
 * from Oracle Siebel CRM using a specific Business Component. The Excel must contain a field that will be used as filter.
 * 
 * @author Jac. Beekers
 * @version 19 December 2015
 * @since   May 2015 
 */
package nl.consag.testautomation.siebel;

import com.siebel.data.SiebelBusComp;
import com.siebel.data.SiebelBusObject;
import com.siebel.data.SiebelDataBean;

import com.siebel.data.SiebelException;

import java.io.File;

import java.io.FileInputStream;

import java.io.FileNotFoundException;

import java.io.IOException;

import java.text.*;
import java.util.*;
import nl.consag.supporting.Constants;
import nl.consag.supporting.Logging;
import nl.consag.testautomation.supporting.ExcelFile;

public class RemoveSiebelDataUsingExcel {

    private String version ="20151219.1";
    
	private String className = "RemoveSiebelDataUsingExcel";
	private String logFileName = Constants.NOT_INITIALIZED;
	private String context = Constants.DEFAULT;
	private String startDate = Constants.NOT_INITIALIZED;
    private int nrLog = 0;
    private int logLevel =3;

        private String logicalSourceDir = Constants.NOT_PROVIDED;
        private String physicalSourceDir = Constants.NOT_PROVIDED;
        private String excelFileName = Constants.NOT_PROVIDED;
        private String excelSheetNumber = Constants.NOT_PROVIDED;
        private String excelSheetName = Constants.NOT_PROVIDED;
        
    private SupportingSiebel suppSbl = new SupportingSiebel();
    private String siebelConnection = Constants.NOT_PROVIDED;
    private String userId = Constants.NOT_PROVIDED;

        private String busObj = Constants.NOT_PROVIDED;
        private String busComp = Constants.NOT_PROVIDED;
        private String filterField = Constants.NOT_PROVIDED;
        private String includeIntegrationId = Constants.SIEBEL_INCLUDE_INTEGRATION_ID_DEFAULT;
        
        private Integer amountRecordsRemoved =0;
	  
	private String resultMessage = Constants.OK; //text message that is returned to FitNesse  
        private String errorMessage =Constants.ERROR;

	public RemoveSiebelDataUsingExcel() {
		//Constructors
	      	java.util.Date started = new java.util.Date();
	      	SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
	      	startDate = sdf.format(started);
	      	this.context=className;
	        logFileName = this.startDate + "." + this.className;
	    }

    /**
     * @param context
     */
    public RemoveSiebelDataUsingExcel(String context) {
	    	java.util.Date started = new java.util.Date();
	    	SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
	    	startDate = sdf.format(started);
	    	this.context=context;
	        logFileName = this.startDate + "." + this.className +"." +this.context;

	    }

    /**
     * @param expRecords
     * @return
     */
    public boolean numberOfRecordsRemoved(Integer expRecords) {
        
        return (expRecords == amountRecordsRemoved);
            
    }

    /**
     * @return Fixture version
     */
    public String getVersion() {
        return this.version;
    }
    
    /**
     * @return
     */
    public String removal() {
        /* Load consists of the following steps:
         * 1. Check input parameters, e.g. Excel file exists, connectivity
         * 2. Load Excel into an array
         * 3. Setup connection
         * 4. Process array to load data
         */
        String myName="removal";
        SiebelBusObject bo = null;
        SiebelBusComp bc = null;
        String excelFile = Constants.NOT_PROVIDED;
        String myArea="init";
        boolean err =false;
        String logMessage =Constants.NOERRORS;
        
        setErrorMessage(Constants.OK);

        //read Siebel properties
        suppSbl.setLogLevel(getLogLevel());
        suppSbl.readSiebelProperties(getSiebelConnection());
        
        /*
         * read directory properties if a logical directory was specified
         * if not, it is assumed the file name will be found as-is (e.g. because it actually is the entire path)
        */
        if(getLogicalSourceDirectory().equals(Constants.NOT_PROVIDED)) {
            excelFile = getExcelFileName();
        } else {
            //read physical directory and concatenate
            readDirectoryProperties();
            //In this version: assuming the physical directory ends with a slash or the file name starts with one
            excelFile = getPhysicalSourceDirectory().concat(getExcelFileName());
        }

        if(getPhysicalSourceDirectory().equals(Constants.NOT_FOUND)) {
            setErrorMessage("Mapping to physical directory could not be determined. Logical directory >" + getLogicalSourceDirectory() + "< not found in >" + Constants.DIRECTORY_PROPERTIES +"<.");
            setResultMessage("Directory mapping failed.");
            return getErrorMessage();
        }

        //Check accessibility
        
        if(! excelFileAccessible(excelFile)) {
            setResultMessage("Excel file could not be accessed");
            setErrorMessage("Excel file >" + excelFile + "< is not accessible.");
            return getErrorMessage();
        }
        //Load Excel
        ExcelFile loadFile = new ExcelFile();
        List<List<String>> dataToRemove = null;
        
        if(getExcelSheetNumber().equals(Constants.NOT_PROVIDED)) {
            if(getExcelSheetName().equals(Constants.NOT_PROVIDED)) {
                // error. must supply sheet name or sheet number
                setErrorMessage("Sheet number or sheet name must be provided.");
                return getErrorMessage();
            } else {
                dataToRemove = loadFile.readWorksheetWithName(excelFile, getExcelSheetName());
                } 
        } else {
                    dataToRemove = loadFile.readWorksheetWithNumber(excelFile, Integer.parseInt(getExcelSheetNumber()));
                }

        if(dataToRemove.size() ==0) {
            if(getExcelSheetNumber().equals(Constants.NOT_PROVIDED)) {
                setErrorMessage("dataToRemove is empty. This is likely to be caused by an empty or missing sheet (" + getExcelSheetName() +") in the Excel >" +excelFile +"<.");
            } else {
                setErrorMessage("dataToRemove is empty. This is likely to be caused by an empty or missing sheet (sheet number: " + getExcelSheetNumber() +") in the Excel >" +excelFile +"<.");
            }
            return getErrorMessage();
        }
                
        //Set up connection
        if(suppSbl.getSiebelConnValue().equals(Constants.NOT_FOUND)) {
            setResultMessage("Siebel not configured correctly.");
            setErrorMessage("The Siebel connection definition >" +siebelConnection + "< could not be found in properties file >" + Constants.SIEBEL_PROPERTIES +"<.");
            return getErrorMessage();
        }

        String useUserId =Constants.NOT_INITIALIZED;
        String ssoEnabled =Constants.NOT_FOUND;            
        SiebelDataBean sblBean = new SiebelDataBean();
        try {
            if(Constants.NOT_PROVIDED.equals(getUserId()))
                useUserId = suppSbl.getSiebelUser();
            else
                useUserId = getUserId();
            
            if(suppSbl.getSiebelSSO()) {
                ssoEnabled=Constants.YES;
                sblBean.login(suppSbl.getSiebelConnValue(), useUserId, suppSbl.getSiebelTrustToken(), suppSbl.getSiebelLang());
            }
            else {
                ssoEnabled=Constants.NO;
                sblBean.login(suppSbl.getSiebelConnValue(), useUserId, suppSbl.getSiebelPwd(), suppSbl.getSiebelLang());
            }
        } catch (SiebelException e) {
            setErrorMessage("Connection to >" + getSiebelConnection() +"< failed. Connection value >" + suppSbl.getSiebelConnValue() +"<. User >" 
                            + useUserId +"<. Language >" + suppSbl.getSiebelLang() + "<. SSOEnabled >" + ssoEnabled +"< with token >" 
                            + suppSbl.getSiebelTrustToken() +"<. Error =>" + e.toString() +"<.");
            log(myName,Constants.FATAL,myArea,getErrorMessage());
            return getErrorMessage();
        }

        /*
         *  2015-02-16 Rabobank: If GUI_User =Y, restrictions on queries etc. are enforced
         *  For others parties the setProfileAttr will result in a dynamic attribute, which should not matter.
         */
                myArea="Set profile attributes";
                try {
                    sblBean.setProfileAttr("GUI_User", "N");
                    log(myName,Constants.DEBUG,myArea,"Profile attribute >GUI_User< =>N<.");
                } catch (SiebelException e) {
                    log(myName,Constants.WARNING,myArea,"Profile attribute >GUI_User< could not be set. Error >" +e.toString() + "<.");
                    err=true;
                }
/*                myArea="Set profile attributes";
                try {
                    sblBean.setProfileAttr("MemberAdmin", "Y");
                    log(myName,Constants.DEBUG,myArea,"Profile attribute >MemberAdmin< has been set to =>Y<.");
                } catch (SiebelException e) {
                    logMessage="Profile attribute >MemberAdmin< could not be set. Error >" +e.toString() + "<.";
                    log(myName,Constants.WARNING,myArea,logMessage);
                    setErrorMessage(logMessage);
                    err=true;
                }
*/
                
        myArea="Get BusObjBusComp";
        //Get instances of Siebel business object and component
        try {
            bo = sblBean.getBusObject(getBusinessObject());
            try {
                bc = bo.getBusComp(getBusinessComponent());
            } catch (SiebelException e) {
                setErrorMessage("Could not get business component. Error =>" + e.toString() +"<.");
                setResultMessage("Error getting business component.");
                log(myName,Constants.FATAL,myArea,"Could not get BusComp >"+getBusinessComponent()+"<. Error =>" +e.toString() + "<.");
                err=true;
            }
            
        } catch (SiebelException e) {
            setErrorMessage("Could not get business object. Error =>" + e.toString() +"<.");
            setResultMessage("Error getting business object.");
            log(myName,Constants.FATAL,myArea,"Could not get BusObj >"+getBusinessObject()+"<. Error =>" +e.toString() + "<.");
            err =true;
        }

    if(!err) {
        myArea="CheckResult";
        if(dataToRemove.get(0).indexOf(getFilterField()) == -1) {
            setErrorMessage("Specified field filter >" + getFilterField() +"< could not be found in Excel.");
            setResultMessage("Incorrectly specified field filter.");
            log(myName,Constants.ERROR,myArea,"Test page specifies filter field >" + getFilterField() +"<, but this field was not found in the Excel.");
            err =true;
        } else {
            //Remove data
            setAmountRecordsRemoved(removeRecords(bc, dataToRemove, dataToRemove.get(0).indexOf(getFilterField())));
            if(getIncludeIntegrationId().equals(Constants.YES)) {
                setResultMessage("Removed >" +getAmountRecordsRemoved() +"< record(s) using filter on field >" + getFilterField() +"< and >Integration Id<.");
            } else {
                setResultMessage("Removed >" +getAmountRecordsRemoved() +"< record(s) using filter on field >" + getFilterField() +"<. >Integration Id< was ignored.");    
            }
            log(myName,Constants.DEBUG,myArea,getResultMessage());
        }
      }
        
        //Logoff
        try {
            sblBean.logoff();
        } catch (SiebelException e) {
            logMessage ="Logoff from >" + siebelConnection +"< failed with error >" +e.toString() +"<. Records may have been removed (not guaranteed).";
            log(myName,Constants.WARNING,myArea,getErrorMessage());
        }

            return getErrorMessage();
        
    }

    private Integer removeRecords(SiebelBusComp bc, List<List<String>> dataToRemove, Integer fieldFilterIndex)  {
        
        Integer nrRecordsExcel =0;
        Integer nrRecordsDatabase =0;
        String filterFieldValue =Constants.NOT_INITIALIZED;
        String intFieldValue =Constants.NOT_INITIALIZED;
        String myName="removeRecords";
        String myArea="init";
        String logMessage=Constants.NOT_INITIALIZED;
        
        // First row contains business component field names by design
        if (dataToRemove.size() <2) {
            log(myName,Constants.INFO,myArea,"Excel contains only one row. As the first row contains field names, there is no data to be loaded.");
            return -99;
        }

        int intFieldPos = dataToRemove.get(0).indexOf(Constants.SIEBEL_INTEGRATION_ID_FIELD);
        if (intFieldPos >=0)
            log(myName,Constants.INFO,myArea,"Field >" + Constants.SIEBEL_INTEGRATION_ID_FIELD +"< found at position >" +Integer.toString(intFieldPos) +"<.");
        else
            log(myName,Constants.DEBUG,myArea,"Field >" + Constants.SIEBEL_INTEGRATION_ID_FIELD +"< not found in Excel");
                
        for (int i=1 ; i < dataToRemove.size() ; ++i ) {
            // get the field filter value in the specific row in Excel
            filterFieldValue =dataToRemove.get(i).get(fieldFilterIndex);
                
            try {
                log(myName,Constants.VERBOSE,myArea,"clearToQuery...");
                bc.clearToQuery();
                
                try {
                    log(myName,Constants.VERBOSE,myArea,"Setting view mode to 3...");
                    bc.setViewMode(3);
                    log(myName,Constants.VERBOSE,myArea,"Activating filter field >" + getFilterField() +"<...");
                    bc.activateField(getFilterField());
                    log(myName,Constants.VERBOSE,myArea,"Setiing search specification for field >" + getFilterField() +"< to >" + filterFieldValue +"<...");
                    bc.setSearchSpec(getFilterField(), filterFieldValue);
                    // If the filter field is not already Integration Id and preference to ignore Integration Id is not set
                    if((getFilterField().equals(Constants.SIEBEL_INTEGRATION_ID_FIELD)) ) {
                        if(intFieldPos >= 0) { 
                            intFieldValue =dataToRemove.get(i).get(intFieldPos);
                            bc.setSearchSpec(Constants.SIEBEL_INTEGRATION_ID_FIELD, intFieldValue);
                            log(myName,Constants.VERBOSE,myArea,"Specified filter on " + Constants.SIEBEL_INTEGRATION_ID_FIELD +" with value >" + intFieldValue +"<.");
                        } else {
                            setResultMessage("Deletion failed, Field >" + Constants.SIEBEL_INTEGRATION_ID_FIELD +"< not found in Excel.");
                            nrRecordsDatabase =-1;
                            return -1;
                        }
                    } else {
                        //&& getIncludeIntegrationId().equals(Constants.YES)
                        bc.setSearchSpec(filterField,filterFieldValue);
                        log(myName,Constants.VERBOSE,myArea,"Specified filter: field>" +filterField +"< with value >"+filterFieldValue+"<.");
                    }
                    try {
                        boolean deleteFailure=false;
                        boolean recordFound=false;
                        boolean noMoreRecordsFound =true;
                        bc.executeQuery(false);
                        log(myName,Constants.VERBOSE,myArea,"Delete query parsed.");
                        recordFound=bc.firstRecord();
                        if(!recordFound) {
                            log(myName, Constants.INFO,myArea,"No records found.");
                            noMoreRecordsFound =true;
                        } else {
                            noMoreRecordsFound =false;
                        }
                        while(recordFound && !deleteFailure && !noMoreRecordsFound) {
                            if(!deleteFailure)
                                log(myName, Constants.VERBOSE, myArea,"No delete errors.");
                            else
                                log(myName, Constants.VERBOSE, myArea,"Delete errors.");
                            try {
                                bc.deleteRecord();
                                nrRecordsDatabase++;
                                log(myName,Constants.DEBUG,myArea,"Record# >" + Integer.toString(nrRecordsDatabase) +"< successfully deleted.");
                            } catch (SiebelException e) {
                                if(nrRecordsDatabase ==0) {
                                    setResultMessage("Deletion failed.");
                                    setErrorMessage("Delete failed with error >" + e.toString() +"<.");
                                    log(myName,Constants.ERROR,myArea,"Delete failed with >" +e.toString() +"<.");
                                    deleteFailure=true;   
                                    noMoreRecordsFound=false;
                                } else {
                                    noMoreRecordsFound=true;
                                }
                            }
                        }
                    } catch (SiebelException e) {
                        setResultMessage("Deletion failed on querying for record.");
                        setErrorMessage("Siebel query failed with error >" + e.toString() +"<.");
                        log(myName,Constants.ERROR,myArea,"Siebel query failed with error >" +e.toString() +"<.");
                        nrRecordsDatabase =-1;   
                    }

                } catch (SiebelException e) {
                    setResultMessage("Deletion failed: The search specification could not be set.");
                    setErrorMessage("Search Spec could not be set using field >" +getFilterField() + "< and value >" +filterFieldValue 
                                    + "<. Error message =>" +e.toString() +"<.");
                    log(myName,Constants.ERROR,myArea,"Search Spec for deletion could not be set. Error =>" +e.toString() +"<.");
                    nrRecordsDatabase =-1;   
                }
            } catch (SiebelException e) {
                setResultMessage("Deletion failed: The query could not be initialized.");
                setErrorMessage("Method clearToQuery failed with error >" + e.toString() +"<.");
                log(myName,Constants.ERROR,myArea,"Siebel method clearToQuery failed with error >" +e.toString() +"<.");
                nrRecordsDatabase =-1;   
            }

            nrRecordsExcel++;
        }
            
        log(myName,Constants.INFO,myArea,"Deleted >" +Integer.toString(nrRecordsDatabase) +"< record(s).");
            return nrRecordsDatabase;
    }


    private boolean excelFileAccessible(String fileName) {
        File inputFile = new File(fileName);
        boolean rc = true;
        if (!inputFile.exists()) {
            setErrorMessage("File >" + fileName +"< does not exist.");
            rc=false;
        } else
        if(!inputFile.canRead()) {
            setErrorMessage("Cannot read file >" +fileName +"<.");
            rc=false;
        } else
        if(!inputFile.isFile()) {
            setErrorMessage(">" +fileName +"< is not a file.");
            rc=false;
        }
        return rc;
    }


    /**
     * @param logicalDir
     */
    public void setSourceDirectory(String logicalDir) {
        logicalSourceDir = logicalDir; 
    }

    /**
     * @return
     */
    public String getSourceDirectory() {
        return getLogicalSourceDirectory();
    }

    /**
     * @return
     */
    private String getLogicalSourceDirectory() {
        return logicalSourceDir;
    }

    /**
     * @return
     */
    private String getPhysicalSourceDirectory() {
        return physicalSourceDir;
    }
    
    /**
     * @param logicalDir
     */
    public void sourceDirectory(String logicalDir) {
        setSourceDirectory(logicalDir); 
    }

    /**
     * @param fileName
     */
    public void excelFileName(String fileName) { ExcelFileName(fileName); }

    /**
     * @param fileName
     */
    public void setExcelFileName(String fileName) { 
        excelFileName=fileName; 
        }

    /**
     * @param fileName
     */
    public void ExcelFileName(String fileName) {
                setExcelFileName(fileName);
            }

    /**
     * @return
     */
    public String getExcelFileName() {
        return excelFileName;
    }

    /**
     * @param sheetNr
     */
    public void excelSheetNumber(String sheetNr) { ExcelSheetNumber(sheetNr); }

    /**
     * @param sheetNr
     */
    public void setExcelSheetNumber(String sheetNr) { ExcelSheetNumber(sheetNr); }

    /**
     * @param sheetNr
     */
    public void ExcelSheetNumber(String sheetNr) {
                Integer i=Integer.parseInt(sheetNr) -1;
                excelSheetNumber=Integer.toString(i);
            }

    /**
     * @return
     */
    public String getExcelSheetNumber() {
        return excelSheetNumber;
    }


    /**
     * @param siebelConnection
     */
    public void setSiebelConnection(String siebelConnection) { siebelConnection(siebelConnection); }

    /**
     * @param siebelConnection
     */
    public void siebelConnection(String siebelConnection) {
        this.siebelConnection = siebelConnection;
    }

    /**
     * @return
     */
    public String getSiebelConnection() {
        return this.siebelConnection;
    }


    /**
     * @param businessObject
     */
    public void setBusinessObject(String businessObject) { businessObject(businessObject); }

    /**
     * @param businessObject
     */
    public void businessObject(String businessObject) {
        busObj = businessObject;
    }

    /**
     * @return
     */
    public String getBusinessObject() {
        return busObj;
    }

    /**
     * @param businessComponent
     */
    public void setBusinessComponent(String businessComponent) { businessComponent(businessComponent); }

    /**
     * @param businessComponent
     */
    public void businessComponent(String businessComponent) {
        busComp = businessComponent;
    }

    /**
     * @return
     */
    public String getBusinessComponent() {
        return busComp;
    }

    /**
     * @param fieldName
     */
    public void useFieldAsFilter(String fieldName) {
        setFieldFilter(fieldName);
    }

    /**
     * @param fieldName
     */
    public void setFieldFilter(String fieldName) {
        filterField = fieldName;
    }

    /**
     * @return
     */
    public String getFilterField() {
        return filterField;
    }
    
    private void setErrorMessage (String errMsg) {
        errorMessage=errMsg;
    }

    /**
     * @return
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    private void setResultMessage(String result) {
        resultMessage = result;
    }
    
    /**
     * @return
     */
    public String getResultMessage() {
        return resultMessage;
    }
    /**
     * @return
     */
    public String result() {
        return getResultMessage();
    }

    /**
     * @return
     */
    public String getAmountRecordsRemoved() {
        return Integer.toString(amountRecordsRemoved);
    }

    /**
     * @param recRemoved
     */
    private void setAmountRecordsRemoved(Integer recRemoved) {
        this.amountRecordsRemoved =recRemoved;
    }

    /**
     * @param incIntId
     */
    public void setIncludeIntegrationId(String incIntId) {
        this.includeIntegrationId = incIntId;
    }

    /**
     * @return
     */
    public String getIncludeIntegrationId() {
        return includeIntegrationId;
    }
    /**
     * @param name
     * @param level
     * @param location
     * @param logText
     * @param logLevel
     */
    public void log(String name, String level, String location, String logText) {
               if(Constants.logLevel.indexOf(level.toUpperCase()) > getIntLogLevel()) {
                   return;
               }
               nrLog++;
               if(nrLog ==1) {
                   Logging.LogEntry(logFileName, name, Constants.INFO, "version info", getVersion());
               }
                Logging.LogEntry(logFileName, name, level, location, logText);  
           }

    /**
     * @return
     */
    public String getLogFilename() {
		return logFileName + ".log";
       }

    /*
     * read logical to physical directory mapping properties file
     */
    private void readDirectoryProperties() {

        /*
         * Get directory mapping
         */
        try {
        File file = new File(Constants.DIRECTORY_PROPERTIES);
        FileInputStream fileInput = new FileInputStream(file);
        Properties schedProp = new Properties();
        schedProp.load(fileInput);
        fileInput.close();
        
        physicalSourceDir = schedProp.getProperty(logicalSourceDir, Constants.NOT_FOUND);
        } catch (FileNotFoundException e) {
            physicalSourceDir = Constants.NOT_FOUND;
            } catch (IOException e) {
                physicalSourceDir = Constants.NOT_FOUND;
            }

    }

    /**
     * @param sheetName
     */
    public void excelSheetName(String sheetName) { setExcelSheetName(sheetName); }

    /**
     * @param sheetName
     */
    public void setExcelSheetName(String sheetName) {
        this.excelSheetName=sheetName;
    }

    /**
     * @return
     */
    public String getExcelSheetName() {
        return excelSheetName;
    }
    
    /**
     * @param userId
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * @return User Id
     */
    public String getUserId() {
        return this.userId;
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


}
