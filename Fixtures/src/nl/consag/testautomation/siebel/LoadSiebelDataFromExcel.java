/**
 * This purpose of this fixture is to insert data, made available in an Excel
 * into Oracle Siebel CRM using a specific Business Component
 * @author Jac. Beekers 
 * @since May 2015
 * @version 19 December 2015
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

import org.apache.commons.lang3.time.DateUtils;
import nl.consag.supporting.Constants;
import nl.consag.supporting.Logging;
import nl.consag.testautomation.supporting.ExcelFile;


public class LoadSiebelDataFromExcel {
    private static String version ="20151219.0";
    private int nrLog=0;
    
	private String className = "LoadSiebelDataFromExcel";
	private String logFileName = Constants.NOT_INITIALIZED;
	private String context = Constants.DEFAULT;
	private String startDate = Constants.NOT_INITIALIZED;
	private int logLevel =3;
        
        private String logicalSourceDir = Constants.NOT_PROVIDED;
        private String physicalSourceDir = Constants.NOT_PROVIDED;
        private String excelFileName = Constants.NOT_PROVIDED;
        private String excelSheetNumber = Constants.NOT_PROVIDED;
        private String excelSheetName = Constants.NOT_PROVIDED;
        
        private String busObj = Constants.NOT_PROVIDED;
        private String busComp = Constants.NOT_PROVIDED;
        
        private List<String> businessComponentList = new ArrayList<String>();
        private List<String> firstFieldList = new ArrayList<String>();
        private List<Integer> locFirstFieldList = new ArrayList<Integer>();

        private Integer amountRecordsLoaded =0;
	  
	private String resultMessage = Constants.OK;
        private String errorMessage = Constants.OK;
        private String loadResult = Constants.NOT_INITIALIZED;
        private String errorLevel = Constants.NOERRORS;
        private Integer numberMainRecords =0;
        private Integer numberRelatedRecords =0;
        
        private boolean createChildIfNotExists = Constants.SIEBEL_INSERT_CHILD_IF_NOT_EXISTS;
        private boolean updateIfExistsOnInsert = Constants.SIEBEL_UPDATE_IF_EXISTS;
        private boolean deleteIfExistsOnInsert = Constants.SIEBEL_REPLACE_IF_EXISTS;

        private SupportingSiebel suppSbl = new SupportingSiebel();
        private String siebelConnection = Constants.NOT_PROVIDED;
        private String userId = Constants.NOT_PROVIDED;

        private List<List<String>> dataToLoad = null;
        private SiebelDataBean sblBean=null;
        private List<List<String>> hasPickList =new ArrayList<List<String>>();                         // the field has a pick list associated
        private List<List<String>> pickListJoin =new ArrayList<List<String>>();     // the join name used by the field with the pick list
        private List<List<String>> joinName = new ArrayList<List<String>>();
        private List<List<String>> fieldIsReadOnly =new ArrayList<List<String>>();
        private ArrayList<SiebelFieldBean> fieldPropertiesList = new ArrayList<SiebelFieldBean>();
        private String currentPickField = Constants.NOT_FOUND;
        
        private List<Integer> determinedIndices = new ArrayList<Integer>();
        
        private String salesMethod = Constants.SIEBEL_DEFAULT_SALES_METHOD;
        private int locationSalesMethod =-1;    //location of the sales method in Excel. Needed to filter on dependent stage/status values
        
        private List<List<String>> nonPrimaryBCFields =new ArrayList<List<String>>();
        
	public LoadSiebelDataFromExcel() {
		//Constructors
	      	java.util.Date started = new java.util.Date();
	      	SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
	      	startDate = sdf.format(started);
	      	this.context=className;
	        logFileName = startDate + "." + className;
	    }

    public static String getVersion() {
        return version;
    }

    /**
     * @param context
     */
    public LoadSiebelDataFromExcel(String context) {
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
    public boolean numberOfRecordsLoaded(Integer expRecords) {
        
        return (expRecords == amountRecordsLoaded);
            
    }

    /**
     * @return
     */
    public String load() {
        String myName="load";
        String myArea = Constants.NOT_PROVIDED;
        String logMessage = Constants.NOT_INITIALIZED;
        String rc = Constants.OK;
        String rcLogOff = Constants.OK;
        
        /* Load consists of the following steps:
         * 1. Check input parameters, e.g. Excel file exists, connectivity
         * 2. Load Excel into an array
         * 3. Setup connection
         * 4. Process array to load data
         */
        String excelFile = Constants.NOT_PROVIDED;
        
        setErrorMessage(Constants.INFO, Constants.NOERRORS);

        myArea="read Siebel properties";
                logMessage="Reading Siebel properties from file >" + Constants.SIEBEL_PROPERTIES +"<...";
                log(myName, Constants.VERBOSE,myArea,logMessage);

        suppSbl.setLogLevel(getLogLevel());
        suppSbl.readSiebelProperties(getSiebelConnection());
        
                logMessage="Done.";
                log(myName, Constants.VERBOSE,myArea,logMessage);
        
        /*
         * read directory properties if a logical directory was specified
         * if not, it is assumed the file name will be found as-is (e.g. because it actually is the entire path)
        */
                myArea="read directory properties";
        if(getLogicalSourceDirectory().equals(Constants.NOT_PROVIDED)) {
            excelFile = getExcelFileName();
        } else {
            //read physical directory and concatenate
            readDirectoryProperties();
            //In this version: assuming the physical directory ends with a slash or the file name starts with one
            excelFile = getPhysicalSourceDirectory().concat(getExcelFileName());
        }

        if(getPhysicalSourceDirectory().equals(Constants.NOT_FOUND)) {
            return "Mapping to physical directory could not be determined. Logical directory >" + getLogicalSourceDirectory() + "< not found in >" + Constants.DIRECTORY_PROPERTIES +"<.";
        }

        myArea="Check accessibility";        
        
        if(! excelFileAccessible(excelFile)) {
            return "Excel file >" + excelFile + "< is not accessible.";
        }
        myArea="Load Excel";
        ExcelFile loadFile = new ExcelFile();

        if(getExcelSheetNumber().equals(Constants.NOT_PROVIDED)) {
            if(getExcelSheetName().equals(Constants.NOT_PROVIDED)) {
                // error. must supply sheet name or sheet number
                setErrorMessage(Constants.FATAL, "Sheet number or sheet name must be provided.");
                return getErrorMessage();
            } else {
                dataToLoad = loadFile.readWorksheetWithName(excelFile, getExcelSheetName());
                } 
        } else {
                    dataToLoad = loadFile.readWorksheetWithNumber(excelFile, Integer.parseInt(getExcelSheetNumber()));
                }
        
                
        myArea="Set up connection";
        if(suppSbl.getSiebelConnValue().equals(Constants.NOT_FOUND)) {
            setErrorMessage(Constants.FATAL, "The Siebel connection definition >" +getSiebelConnection() + "< could not be found in properties file >" + Constants.SIEBEL_PROPERTIES +"<.");
            return getErrorMessage();
        }

        String useUserId = Constants.NOT_INITIALIZED;
        sblBean = new SiebelDataBean();
        if(Constants.NOT_PROVIDED.equals(getUserId()))
             useUserId = suppSbl.getSiebelUser();
         else
             useUserId = getUserId();
            
         rc = suppSbl.connect(sblBean,useUserId);
         if(Constants.OK.equals(rc)) {
            log(myName, Constants.DEBUG, myArea, "Connection established."); 
         } else {
             setErrorMessage(rc,suppSbl.getErrorMessage());
             logMessage="Connect returned: " +getErrorMessage();
             log(myName, Constants.ERROR, myArea, logMessage);
             return rc;
         }
         
         rc = suppSbl.setProfileAttributes(sblBean);
         // warnings are ignored.
         if(! (Constants.OK.equals(rc) || Constants.WARNING.equals(rc))) {
             setErrorMessage(rc,suppSbl.getErrorMessage());
           rcLogOff =suppSbl.logoff(sblBean);
           logMessage="Logoff returned >" + rcLogOff+"< (Errors will be ignored).";
           log(myName, Constants.VERBOSE, myArea, logMessage);
            //return setprofileattributes result, not the result of the logoff
            return rc;
         }
 

        myArea="Load data";
        amountRecordsLoaded =loadRecords(dataToLoad);


        myArea="Finalizing";
        if(getErrorMessage().equals(Constants.NOERRORS) || getErrorMessage().equals(Constants.OK)) {
            logMessage = "Excel contained >" + Integer.toString(dataToLoad.size() -1) + "< rows (excluding header). Amount of records inserted =>" + Integer.toString(amountRecordsLoaded) +"<.";
            log(myName, Constants.INFO,myArea,logMessage);
            setResultMessage(logMessage);
            rc = Constants.OK;
        } else {
            setResultMessage(getErrorLevel());
            rc = Constants.ERROR;
        }

        myArea="Logoff";
        rcLogOff =suppSbl.logoff(sblBean);
        logMessage="Logoff returned >" + rcLogOff+"< (Errors will be ignored).";
        log(myName, Constants.VERBOSE, myArea, logMessage);
        //If an exception occurred during load, session is already gone. So ignore logoff errors.
        
        return rc;
        
    }

    /*
     * loadRecords
     * - determine other components in Excel
     * - order by component
     * - process main component record
     * - process other components
     * - proceeed to next line in Excel
     */
    private Integer loadRecords(List<List<String>> dataToLoad)  {
        String myName="loadRecords";
        String myArea = Constants.NOT_PROVIDED;
        
        Integer nrRecords =0;
        boolean recordLoaded =false;
        boolean relRecordLoaded =false;
        SiebelBusObject bo =null;
        SiebelBusComp targetBusComp =null;
        SiebelBusComp primaryBusComp =null;
        String primaryBusCompName = Constants.NOT_FOUND;
        boolean err =false;
        int primaryBcLoc=-99;
        
        myArea="Initialization";
        // First row contains business component field names by design
        if (dataToLoad.size() <2) {
            setErrorMessage(Constants.FATAL, "No records in Excel.");
            log(myName, Constants.FATAL,myArea,getErrorMessage());
            return 0;
        }
        
        determineBusinessComponents(dataToLoad.get(0));
        if(businessComponentList ==null) {
            setErrorMessage(Constants.FATAL, "Error creating business component list.");
            log(myName, Constants.FATAL,myArea,getErrorMessage());
            return 0;
        }
        
        //Get instances of Siebel business object and primary component
        try {
            bo = sblBean.getBusObject(getBusinessObject());
            primaryBusCompName=getPrimaryBusComp(bo.name());
        } catch (SiebelException e) {
            setErrorMessage(Constants.FATAL, e.toString());
            log(myName, Constants.FATAL,myArea,getErrorMessage());
            err=true;
        }

        //Get instances of Siebel business object and target component (component mentioned on test page)
        if(!err) {
            try {
                targetBusComp = bo.getBusComp(getBusinessComponent());
            } catch (SiebelException e) {
                setErrorMessage(Constants.FATAL,e.toString());
                log(myName, Constants.FATAL,myArea,getErrorMessage());
                err=true;
            }
        }

        if(!err) {
            myArea="Processing records";
            setLoadResult(Constants.OK);
            //Determine fields in Excel for specific BusComps
            List<String> targetBcFieldList =dataToLoad.get(0);
            targetBcFieldList =determineFieldList(targetBusComp.name(), dataToLoad.get(0));
            if(getFieldProperties(targetBusComp, targetBcFieldList)) {
                //Set context, if needed, for primary Business Component
                primaryBcLoc =businessComponentList.indexOf(primaryBusCompName);
                 String queryField = Constants.NOT_INITIALIZED;
                int locInExcel=0;
                if(primaryBcLoc > -1) {
                    log(myName, Constants.DEBUG,myArea,"Primary BusComp >" +primaryBusCompName + "< found in array at position# >" + Integer.toString(primaryBcLoc) +"<.");
                    locInExcel=locFirstFieldList.get(primaryBcLoc);
                    queryField=firstFieldList.get(primaryBcLoc);
                    log(myName, Constants.DEBUG,myArea,"QueryField >" +queryField + "< is located in Excel column# >" + Integer.toString(locInExcel) + "<.");
                    try {
                        primaryBusComp = bo.getBusComp(primaryBusCompName);
                    } catch (SiebelException e) {
                        setErrorMessage(Constants.FATAL,"Could not get primary BusComp >" + primaryBusCompName + "<. Error =>" + e.toString() +"<.");
                        log(myName, Constants.FATAL,myArea,getErrorMessage());
                        err=true;
                    }
                } else {
                    log(myName, Constants.WARNING,myArea,"Primary BusComp >" +primaryBusCompName + "< NOT found in array as it was not found in Excel. This is OK if no context is needed.");     
                }
                // we start at 1 as the first line is the field list (header line)
                for (int lineNr=1 ; lineNr < dataToLoad.size() ; ++lineNr ) {
                    // provide context for related record
                    if(primaryBcLoc > -1) {
                        queryMainRecord(primaryBusComp,queryField,dataToLoad.get(lineNr).get(locInExcel));
                    }
                    // load  a record
                    List<String> filteredColumns= new ArrayList<String>();
                    filteredColumns =determineFieldValues(dataToLoad.get(lineNr));
                    targetBusComp.release();
                    try {
                        targetBusComp = bo.getBusComp(getBusinessComponent());
                        recordLoaded =loadTgtRecord(targetBusComp, fieldPropertiesList, filteredColumns, lineNr);
                        if(recordLoaded) {
                            setNumberRecords(++nrRecords);
                        } else {
                            log(myName, Constants.ERROR,myArea,"At line# >" +Integer.toString(lineNr) +"< Error: " + getErrorMessage());
                            break;
                        }
                    } catch (SiebelException e) {
                        setErrorMessage(Constants.ERROR, "Cannot get reference for target BusComp >" + targetBusComp.name() +"< from BusObject >" + bo.name() +"<.");
                        log(myName, Constants.ERROR, myArea, getErrorMessage());
                        break;
                    }
               }
            } else {
//                setErrorMessage(Constants.ERROR,"GetFieldProperties returned an error.");
                log(myName, Constants.ERROR, myArea, getErrorMessage());
            }
        }
            targetBusComp.release();
            if(primaryBcLoc > -1) primaryBusComp.release();
            bo.release();
            log(myName, Constants.INFO,myArea,"Ready.");

            return nrRecords;
    }

    private void setNumberRecords(Integer i) {
        numberMainRecords=i;
    }

    /**
     * @return
     */
    public String getNumberRecords() {
        return numberMainRecords.toString();
    }
    private void setNumberRelatedRecords(Integer i) {
        numberRelatedRecords=i;
    }

    /**
     * @return
     */
    public String getNumberRelatedRecords() {
        return numberRelatedRecords.toString();
    }

    /**
     * @param expectedValue
     * @return
     */
    public boolean getNumberRelatedRecords(String expectedValue) {
            if(getNumberRelatedRecords().equals(expectedValue))
                return true;
            else
                return false;
    }

    /**
     * @param expectedValue
     * @return
     */
    public boolean getNumberRecords(String expectedValue) {
            if(getNumberRecords().equals(expectedValue))
                return true;
            else
                return false;
    }


    private boolean getFieldProperties(SiebelBusComp bc, List<String> fieldList) {
        String myName="getFieldProperties";
        String myArea = Constants.NOT_PROVIDED;
        String useUserId = Constants.NOT_PROVIDED;
        
         SiebelFieldBean currField;
         if(Constants.NOT_PROVIDED.equals(getUserId()))
              useUserId = suppSbl.getSiebelUser();
          else
              useUserId = getUserId();

         for(int j=0 ; j < fieldList.size() ; ++j) {
             
                 currField =suppSbl.getBusCompFieldProperties(sblBean, bc, fieldList.get(j));
                 if(currField== null || currField.getName() == null || currField.getName().isEmpty()) {
                     setLoadResult("Load aborted. Check error message for details.");
                     setErrorMessage(suppSbl.getErrorLevel(),suppSbl.getErrorMessage());
                     log(myName, Constants.FATAL,myArea,getErrorMessage());
                     return false;
                 } else {
                     // store the location of special fields
                     if(Constants.SIEBEL_SALES_METHOD_FIELD.equals(currField.getName())) {
                         log(myName, Constants.VERBOSE, myArea,"Sales Method column at location >" + Integer.toString(j) +"<.");
                         setLocationSalesMethod(j);
                     }
                    // add field properties to the field list
                     fieldPropertiesList.add(currField);
                     }
            }
        
        return true;
     
     }
    
    /*
     * Load one record
     * - load the primary component
     */
    private boolean loadTgtRecord(SiebelBusComp bc, ArrayList<SiebelFieldBean> fieldPropList, List<String> recordToLoad, int lineNr) {
        String myName ="loadTgtRecord";
        String myArea = "init";

        boolean recordLoaded=true;
        boolean dateField=false;
        String fieldVal = Constants.NOT_INITIALIZED;
        String fieldType = Constants.NOT_INITIALIZED;
        String currIntegrationId = Constants.NO_ERRORS;
        SiebelFieldBean currField = null;
        boolean emptyVal=false;
        String rc = Constants.OK;

        setErrorMessage(Constants.OK, Constants.NOERRORS);
        setLoadResult(Constants.OK);

        log(myName, Constants.DEBUG,myArea,"Loading line# >" + Integer.toString(lineNr));
       
        try {
            bc.newRecord(true);  // true=NewAfter
        } catch (SiebelException e) {
            setErrorMessage(Constants.ERROR, "New Record method failed: " + e.toString());
            setLoadResult(getErrorMessage());
            return false;
        }
                
        myArea="Processing fields";
        for(int j=0 ; j < fieldPropList.size() ; ++j) {
            log(myName, Constants.VERBOSE,myArea,"Processing =>" +Integer.toString(j+1) +"< of >" + Integer.toString(fieldPropList.size()) 
                                                +"<. recordToLoad size =>" + Integer.toString(recordToLoad.size()) +"<.");
                log(myName, Constants.VERBOSE,myArea,"Getting field properties...");
            currField = fieldPropList.get(j);
            if(j < recordToLoad.size()) {
                log(myName, Constants.VERBOSE,myArea,"Done. Field name =>" + currField.getName() + "< is of type >" + currField.getType() 
                                                    +"<. Multivalued =>" + currField.getIsMultiValued() +"<. Provided value =>" 
                                                    + recordToLoad.get(j) +"<.");
            } else {
                log(myName, Constants.DEBUG, myArea,"Field in PropertyList >" + fieldPropList.get(j) +"< does not have value in Excel.");
                emptyVal=true;
            }
            if(emptyVal) {
                continue;
            }
            if(j == locationSalesMethod) {
                String fieldNameOnThisLocation=fieldPropList.get(j).getName();
                if(Constants.SIEBEL_SALES_METHOD_FIELD.equals(fieldNameOnThisLocation)) {
                    log(myName, Constants.VERBOSE, myArea, "Column is Sales Method column. Field >" + fieldNameOnThisLocation +"< and value >" + dataToLoad.get(j) +"<.");
                    setSalesMethod(recordToLoad.get(j));                    
                } else {
                    log(myName, Constants.VERBOSE, myArea, "Iterator is at a column which could have been Sales Method, but field is named >" + fieldNameOnThisLocation +"< and value >" + dataToLoad.get(j) +"<.");
                }
            }

                fieldVal = Constants.NOT_INITIALIZED;
                fieldType = Constants.NOT_INITIALIZED;
            boolean parseOk=true;
                                    
            if(currField.getName().equals("Integration Id")) { 
                currIntegrationId=recordToLoad.get(j); 
                        log(myName, Constants.INFO,myArea,"Processing Integration Id >" +currIntegrationId +"<.");
                }
            
            // Field has a PickList. Use it to associate it to the new record
            if(currField.getHasPickList().equals(Constants.YES)) {
                myArea="Processing PickList";
                    log(myName, Constants.DEBUG,myArea, "Picklist field >" +currField.getName()+"<.");
                rc =pickRecord(bc,currField.getName(),recordToLoad.get(j));
                if(rc.equals(Constants.OK)) {
                    log(myName, Constants.DEBUG,myArea,"Picking value >" + recordToLoad.get(j) +"< for field >" + currField.getName() + "< succeeded.");
                } else {
                    setErrorMessage(Constants.ERROR, "Picking value >" + recordToLoad.get(j) +"< for field >" + currField.getName() + "< failed. Error =>" + rc + "<.");
                    log(myName, Constants.ERROR,myArea,getErrorMessage());
                    recordLoaded=false;
                    break;
                }    
            } else {
                    // Field is a MultiValued field. shit happens... 
                    if(currField.getIsMultiValued().equals(Constants.YES) ) {
                        rc=HandleMvg(bc, currField, recordToLoad.get(j), lineNr);
                        if(!Constants.OK.equals(rc))
                        continue;
                    }
                     else {
                        fieldType =currField.getType();
                    fieldVal = recordToLoad.get(j);
                    SimpleDateFormat sdf =null;
                    if(Constants.DTYPE_DATE.equals(currField.getType()) ||
                        Constants.DTYPE_DATETIME.equals(currField.getType()) ||
                        Constants.DTYPE_UTCDATETIME.equals(currField.getType())) {
                        dateField=true;
                        myArea="Handling date/time field";
                        log(myName, Constants.DEBUG,myArea, "Date/Time field >" +currField.getName()+"<.");
                        Date d = null;
                            try { 
                                    d = DateUtils.parseDate(recordToLoad.get(j), Constants.SIEBEL_DATE_FORMATS);
                                    if(Constants.DTYPE_DATE.equals(currField.getType()))
                                        sdf = new SimpleDateFormat(Constants.SIEBEL_DATE_FORMAT);
                                    else if(Constants.DTYPE_DATETIME.equals(currField.getType()))
                                        sdf = new SimpleDateFormat(Constants.SIEBEL_DATETIME_FORMAT);
                                    else if(Constants.DTYPE_UTCDATETIME.equals(currField.getType()))
                                        sdf = new SimpleDateFormat(Constants.SIEBEL_UTCDATETIME_FORMAT);
                                    else sdf = new SimpleDateFormat(Constants.DEFAULT_TIMESTAMP_FORMAT);
                                    fieldVal =sdf.format(d);
                                } catch (ParseException e) {
                                    setErrorMessage(Constants.ERROR, "Value >" +fieldVal+" for date/time field >" +currField.getName() + "< could not be parsed. Error =>" + e.toString() +"<.");
                                    log(myName, Constants.ERROR,myArea, getErrorMessage());
                                    setLoadResult(Constants.ERROR);
//                                    fieldVal=e.toString();
                                    parseOk=false;
                                    recordLoaded=false;
                                }
                            }
                        if(parseOk){ 
                            try {
                                if(dateField) {
                                    bc.setFormattedFieldValue(currField.getName(), fieldVal);
                                } else {
                                    bc.setFieldValue(currField.getName(), fieldVal);
                                }
                            } catch (SiebelException e) {
                                setErrorMessage(Constants.ERROR, "At IntegrationId >" + currIntegrationId + "<. Field of type >" + currField.getType() + "< with value >" + recordToLoad.get(j) + "<, was converted to >" + fieldVal 
                                        +"<, could not be assigned to field >" + currField.getName() + "<. Error =>" +e.toString() +"<.");
                                setLoadResult(getErrorMessage());
                                log(myName, Constants.ERROR,myArea, getErrorMessage());
                                recordLoaded=false;
                                break;
                            }
                        } else {
                            //Parse not ok
                        }
                    }
                }
                
        }

        if(recordLoaded) {
            myArea="Write Record";
        try {
            bc.writeRecord();
            log(myName, Constants.DEBUG,myArea, "Method writeRecord >OK< for BusComp >" + bc.name() +"<.");
        } catch (SiebelException e) {
            setErrorMessage(Constants.ERROR, "Write record failed. Error =>" + e.toString() +"<.");
            setLoadResult(getErrorMessage());
            log(myName, Constants.ERROR,myArea, getErrorMessage());
            recordLoaded=false;
        }
        } else {
            try {
                myArea="UndoRecord";
                bc.undoRecord();
                log(myName, Constants.DEBUG,myArea, "Method undoRecord >OK< for BusComp >" + bc.name() +"<.");
            } catch (SiebelException e) {
                setErrorMessage(Constants.ERROR, "Undo record failed. Error =>" + e.toString() +"<.");
                setLoadResult(getErrorMessage());
                log(myName, Constants.ERROR,myArea, getErrorMessage());
                recordLoaded=false;
            }
        }

        return recordLoaded;
    }

    private boolean excelFileAccessible(String fileName) {
        File inputFile = new File(fileName);
        boolean rc = true;
        if (!inputFile.exists()) {
            setErrorMessage(Constants.FATAL, "File >" + fileName +"< does not exist.");
            rc=false;
        } else
        if(!inputFile.canRead()) {
            setErrorMessage(Constants.FATAL, "Cannot read file >" +fileName +"<.");
            rc=false;
        } else
        if(!inputFile.isFile()) {
            setErrorMessage(Constants.FATAL, ">" +fileName +"< is not a file.");
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
     * FitNesse does not use setters
     */
    public void excelSheetNumber(String sheetNr) { setExcelSheetNumber(sheetNr); }

    /**
     * @param sheetNr
     * User provides a number counted from 1, program logic is 0-based.
     */
    public void setExcelSheetNumber(String sheetNr) {
            Integer i=Integer.parseInt(sheetNr) -1;
            excelSheetNumber=Integer.toString(i);        
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
     * @return Excel sheet number as string
     */
    public String getExcelSheetNumber() {
        return excelSheetNumber;
    }

    /**
     * @return Excel sheet number as Integer
     */
    private Integer getExcelSheetNumberAsInteger() {
        return Integer.getInteger(getExcelSheetNumber());
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

    
    private void setErrorMessage (String errMsg) {
        setErrorLevel(Constants.UNKNOWN);
        errorMessage=errMsg;
    }
    private void setErrorMessage (String errLevel, String errMsg) {
        setErrorLevel(errLevel);
        errorMessage=errMsg;
    }

    /**
     * @return
     */
    public String getErrorMessage() {
        return errorMessage;
    }
    
    /**
     * @return
     */
    public String result() {
        return getResultMessage();
    }
    public String getErrorLevel() {
        return this.errorLevel;
    }
    protected void setErrorLevel(String errorLevel) {
        this.errorLevel =errorLevel;
    }
    
    private void setLoadResult(String msg) {
        loadResult =msg;
    }

    /**
     * @return
     */
    public String getLoadResult() {
        return loadResult;
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
                Logging.LogEntry(logFileName, name, Constants.INFO, "get version", "Fixture version: " + getVersion());
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
     * FitNesse does not understand getters/setters when using script table.
     * @param yesNo
     */
    public void updateIfExists(String yesNo) {
        setUpdateIfExists(yesNo);
    }
    
    /**
     * @param yesNo
     */
    public void setUpdateIfExists(String yesNo) {
        if(Constants.YES.equalsIgnoreCase(yesNo)) 
            this.updateIfExistsOnInsert =true;
        else if(Constants.NO.equalsIgnoreCase(yesNo)) {
            this.updateIfExistsOnInsert =false;
        } else this.updateIfExistsOnInsert = Constants.SIEBEL_UPDATE_IF_EXISTS;
    }

    /**
     * @return YES or NO
     */
    public String getUpdateIfExists() {
        if(updateIfExistsOnInsert) 
            return Constants.YES;
            else return Constants.NO;
    }

    /**
     * FitNesse does not understand getters/setters when using script table.
     * @param yesNo
     */
    public void replaceIfExists(String yesNo) {
        setReplaceIfExists(yesNo);
    }
    /**
     * @param yesNo
     */
    public void setReplaceIfExists(String yesNo) {
        if(Constants.YES.equalsIgnoreCase(yesNo)) 
            this.deleteIfExistsOnInsert =true;
        else if(Constants.NO.equalsIgnoreCase(yesNo)) {
            this.deleteIfExistsOnInsert =false;
        } else this.deleteIfExistsOnInsert = Constants.SIEBEL_UPDATE_IF_EXISTS;
    }

    /**
     * @return YES or NO
     */
    public String getReplaceIfExists() {
        if(deleteIfExistsOnInsert) 
            return Constants.YES;
            else return Constants.NO;
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

    private void determineBusinessComponents(List<String> fieldList) {
        String myName="determineBusinessComponents";
        String bcName = Constants.NOT_INITIALIZED;
        String myArea = Constants.NOT_PROVIDED;
        
        // start with the main bc
        myArea="mainBC";
        bcName =getBusinessComponent();
        if(! (bcName == null) && ! (bcName.isEmpty())) {
            businessComponentList.add(bcName);            
            log(myName, Constants.DEBUG,myArea, "BusComp >" + bcName + "< added to list.");
            String firstField = Constants.SIEBEL_INTEGRATION_ID_FIELD;
            firstFieldList.add(firstField);
            locFirstFieldList.add(0);
        }
        
        myArea="Check other BCs";
        for(int i=0; i < fieldList.size() ; ++i) {
            if(fieldList.get(i).indexOf(".") > -1) {
                // The format is [buscompname].[fieldname]
                bcName =stripBrackets(fieldList.get(i).substring(0, fieldList.get(i).indexOf(".")));
                if(! businessComponentList.contains(bcName)) {
                    businessComponentList.add(bcName);
                    String firstField =stripBrackets(fieldList.get(i).substring(fieldList.get(i).indexOf(".") +1));
                    //firstField = firstField.substring(0, firstField.length()-1);
                    log(myName, Constants.DEBUG,myArea, "BusComp >" + bcName + "< added to list with firstField >" + firstField +"<.");
                    firstFieldList.add(firstField);
                    locFirstFieldList.add(i);
                }
            }
        }

    }

    /**
     * @return
     */
    public String getBusinessComponentList () {
        return businessComponentList.toString();
    }

    /**
     * @param YesNo
     */
    public void setCreateChildRecord(String YesNo) {
        if(YesNo.equals(Constants.YES)) {
            setCreateChildIfNotExists(true);
        } else {
            setCreateChildIfNotExists(false);
        }
    }

    private void setCreateChildIfNotExists(boolean b) {
        createChildIfNotExists = b;
    }


    private void setHasPickList(String bcName, String fieldName, String has) {
        if(has ==null || has.isEmpty()) {
            //don't register fields without joins
        } else {
            List<String> thisField = new ArrayList<String>();
            thisField.add(bcName);
            thisField.add(fieldName);
            thisField.add(has);
            hasPickList.add(thisField);
            setCurrentPickField(fieldName);
        }
    }
    
    private String getHasPickList(String bcName, String fieldName) {
        int i =0;
        int j =0;
        
        i=hasPickList.indexOf(bcName);
        if(i >= 0) {
            j =hasPickList.get(i).indexOf(fieldName);
            if(j >= 0) 
             return hasPickList.get(i).get(j +1);
        }
        return Constants.N;
    }
    
    private boolean hasPickList(String bcName, String fieldName) {
        if(getHasPickList(bcName, fieldName).equals(Constants.Y)) {
            return true;
                } else {
                    return false;
                }
        
    }


    private void setPickListJoin(String bcName, String fieldName, String join) {

        if(join ==null || join.isEmpty()) {
            //don't register fields without joins
        } else {
            List<String> thisField = new ArrayList<String>();
            thisField.add(bcName);
            thisField.add(fieldName);
            thisField.add(join);
            pickListJoin.add(thisField);
        }

    }
    
    private String getPickListJoin(String bcName, String fieldName) {
        int i =0;
        int j =0;
        
        i=pickListJoin.indexOf(bcName);
        if(i >= 0) {
            j =pickListJoin.get(i).indexOf(fieldName);
            if(j >= 0) 
             return pickListJoin.get(i).get(j +1);
        }
        return Constants.NO_JOIN;
    }
    

    private boolean isJoinedThroughPickList(String bcName, String fieldName, String pickField) {
        String pickJoin =getPickListJoin(bcName, pickField);
        String fieldJoin =getJoinName(bcName, fieldName);
        
        if(pickJoin.equals(Constants.NO_JOIN)) {
            return false;
        }
        if(fieldJoin.equals(Constants.NO_JOIN)) {
            return false;   
        }
        if(pickJoin.equals(fieldJoin)) {
            return true;
        } else {
            return false;
        }
    }

    private String getJoinName(String bcName, String fieldName) {
        int i =0;
        int j =0;
        
        i=joinName.indexOf(bcName);
        if(i >= 0) {
            j =joinName.get(i).indexOf(fieldName);
            if(j >= 0) 
             return joinName.get(i).get(j +1);
        }
        return Constants.NO_JOIN;        
    }
    
    private void setJoinName(String bcName, String fieldName, String join) {
        if(join ==null || join.isEmpty()) {
            //don't register fields without joins
        } else {
            List<String> thisField = new ArrayList<String>();
            thisField.add(bcName);
            thisField.add(fieldName);
            thisField.add(join);
            joinName.add(thisField);
        }
    }

    private String pickRecord(SiebelBusComp bc, String fieldName, String fieldValue) {
        String myName ="pickRecord";
        String myArea = Constants.NOT_PROVIDED;
        
        String rc = Constants.OK;
        SiebelBusComp pickBC =null;
        String pickBCField = Constants.NOT_INITIALIZED;
        
        if(fieldValue.isEmpty() || fieldValue.equals(Constants.SIEBEL_NULL_VALUE)) {
            return rc;
        }

        try {
            myArea="Query for PickRecord";
            // Some BusComps need special handling
            pickBC = bc.getPicklistBusComp(fieldName);
            if(Constants.SIEBEL_SALES_STAGE_BUSCOMP.equals(pickBC.name())) {
                rc=pickSalesStage(pickBC, fieldName, fieldValue);
            } else {
            pickBC.clearToQuery();
            if((pickBC.name().equals("PickList Generic") || pickBC.name().equals("PickList Hierarchical")
                    || pickBC.name().equals("PickList Hierarchical Sub-Area") || pickBC.name().equals("List Of Values")
                ) 
                && ! ("Sales Method".equals(pickBC))
            ){
                pickBC.setSearchSpec("Value",fieldValue);
            } else {
                //Not all components have an Integration Id. Ignore the error
                String searchField= getAlternativeSearchField(pickBC.name(), Constants.SIEBEL_INTEGRATION_ID_FIELD);
                try {
                    // simple filter using one (1) field and equal sign. The field value is assumed to be a string.
                    String sExpr="[" +  searchField + "] ='"+fieldValue +"'";
                    // TODO: Make flexible
                    if("Sales Method".equals(pickBC.name())) {
                        sExpr="[Name]='" + getSalesMethod() + "'" ; // + " and [" + searchField + "] ='" +fieldValue+"'";
                    }
                    log(myName, Constants.DEBUG,myArea,"PickBC=>" +pickBC.name() +"<. SearchExpr =>" + sExpr +"<.");
                    pickBC.setSearchExpr(sExpr);
                    try {
                        pickBC.executeQuery(false);
                        if(pickBC.firstRecord()) {
                            pickBC.pick();
                        } else {
                            rc="Picking >" + fieldValue +"< on pickBC >" +pickBC.name() +"< failed. Reason >" +
                                    Constants.NOT_FOUND +"<.";
                            setErrorMessage(Constants.ERROR, rc);
                        }
                    } catch(SiebelException e) {
                        rc="Execute Query for pickBC >" + pickBC.name() +"< failed. Error =>" + e.toString() + "<.";
                        setErrorMessage(Constants.ERROR, rc);
                        log(myName, Constants.ERROR, myArea, rc);
                    }
                } catch (SiebelException e){
                        //not all Siebel components have the field Integration Id.
                        log(myName, Constants.WARNING,myArea,"Processing field >" + fieldName +"<. SetSearchSpec on pickBC >" 
                                                          + pickBC.name() +"< for >" + Constants.SIEBEL_INTEGRATION_ID_FIELD +"='" + fieldValue + "'< failed. Filter will be ignored.");
                        rc="Error picking record =>" + e.toString() + "<.";
                        setErrorMessage(Constants.ERROR, rc);
                        log(myName, Constants.ERROR,myArea,rc);
                }
            }
            }
        } catch (SiebelException e) {
            rc="Error picking >" + fieldValue +"< for field >" + fieldName +"<. Error =>" +e.toString() + "<.";
            setErrorMessage(Constants.ERROR, rc);
        }


        return rc;
    }


    private void setFieldIsReadOnly(String bcName, String fieldName, String ro) {
        if(ro ==null || ro.isEmpty()) {
            //don't register fields without joins
        } else {
            List<String> thisField = new ArrayList<String>();
            thisField.add(bcName);
            thisField.add(fieldName);
            thisField.add(ro);
            fieldIsReadOnly.add(thisField);
        }

    }
    
    private String getFieldIsReadOnly(String bcName, String fieldName) {
        int i =0;
        int j =0;
        
        i=fieldIsReadOnly.indexOf(bcName);
        if(i >= 0) {
            j =fieldIsReadOnly.get(i).indexOf(fieldName);
            if(j >= 0) 
             return fieldIsReadOnly.get(i).get(j +1);
        }
        return Constants.N;        
    }
    private boolean isFieldReadOnly(String bcName, String fieldName) {
        
        if(getFieldIsReadOnly(bcName, fieldName).equals(Constants.Y)) {
            return true;
        } else {
            return false;
        }
        
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
     * @param userId
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * @return
     */
    public String getUserId() {
        return this.userId;
    }

    private String getAlternativeSearchField(String bcName, String defaultField) {
        // TODO: Get list from file
        
        /*
        if ("Contact".equals(bcName))
            return "Person UId";
        if ("Party Contact".equals(bcName))
            return "Person UId";
        if ("RB Branch Location AB".equals(bcName))
            return "Location Name";
        */
        if("Currency".equals(bcName))
            return "Currency Code";
        if("Sales Cycle Def".equals(bcName))
            return "Sales Stage Status";
        
        if("Contact".equals(bcName))
            return "Person UId";
        
        return defaultField;
    }

/*
 * getPrimaryBusComp
 * Needed to provide context for related records
 */
    private String getPrimaryBusComp(String boName) {
        SiebelBusObject bo=null;
        SiebelBusComp bcBC=null;
        boolean ok=true;
        String primBC = Constants.NOT_FOUND;
        SiebelFieldBean currField = new SiebelFieldBean();
        
        //Get instance of Siebel business object 
        try {
            bo = sblBean.getBusObject("Repository Business Object");
            try {
                bcBC = bo.getBusComp("Repository Business Object");
                bcBC.activateField("Name");
                bcBC.clearToQuery();
                bcBC.setSearchSpec("Name", boName);
                bcBC.executeQuery(false);
                if(bcBC.firstRecord()) {
                    primBC=bcBC.getFieldValue("Primary Business Component");
                }
            }
            catch (SiebelException e) {
                            setErrorMessage(Constants.FATAL, "Error repository query on busComp >Repository Business Object<. Msg =>" + e.toString() +"<.");
                            primBC = Constants.NOT_FOUND;
                        }
           } catch (SiebelException e) {
                        setErrorMessage(Constants.FATAL, "Error get BusObject repository for busObj >Repository Business Object<. Msg =>" + e.toString() +".");
                        primBC = Constants.NOT_FOUND;
                    }
    return primBC;
    }
    
    /*
     * QueryMainRecord
     * Need to provide context for other busComps within BusObject
     */
    private String queryMainRecord(SiebelBusComp bc, String fieldName, String fieldValue) {
        String myName ="queryMainRecord";
        String myArea = Constants.NOT_PROVIDED;
        
        String rc = Constants.OK;
        
        if(fieldValue.isEmpty() || fieldValue.equals(Constants.SIEBEL_NULL_VALUE)) {
            return rc;
        }

        try {
            String rowId = Constants.NOT_FOUND;
            myArea="Query for Main Record";
            bc.clearToQuery();
            if(bc.name().equals("PickList Generic") || bc.name().equals("PickList Hierarchical")
                    || bc.name().equals("PickList Hierarchical Sub-Area") || bc.name().equals("List Of Values")) {
                bc.setSearchSpec("Value",fieldValue);
            } else {
                //Not all components have an Integration Id. Ignore the error
                //Rabobank uses Person Uid
                String searchField= getAlternativeSearchField(bc.name(), Constants.SIEBEL_INTEGRATION_ID_FIELD);
                try {
                    // simple filter using one (1) field and equal sign. The field value is assumed to be a string.
                    String sExpr="[" +  searchField + "]" + "='"+fieldValue +"'";
                    bc.setSearchExpr(sExpr);
                    log(myName, Constants.DEBUG,myArea,"mainBC=>" +bc.name() +"<. SearchExpr =>" + sExpr +"<.");
                } catch (SiebelException e){
                        //not all Siebel components have the field Integration Id.
                        log(myName, Constants.WARNING,myArea,"Processing field >" + fieldName +"<. SetSearchSpec on pickBC >" 
                                                          + bc.name() +"< for >" +
                        Constants.SIEBEL_INTEGRATION_ID_FIELD +"='" + fieldValue + "'< failed.");
                        log(myName, Constants.ERROR,myArea,"Error picking record =>" + e.toString() + "<.");
                }
            }
            bc.executeQuery(false);
            if(bc.firstRecord()) {
                rowId =bc.getFieldValue("Id");
                log(myName, Constants.DEBUG,myArea,"Query for >" + fieldValue +"< on BusComp >" +bc.name() +"< succeeded. RowId =>" + rowId + "<.");
            } else {
                rc="Query for >" + fieldValue +"< on BusComp >" +bc.name() +"< failed. Reason >" +
                    Constants.NOT_FOUND +"<.";
                log(myName, Constants.ERROR,myArea,"Query for >" + fieldValue +"< on BusComp >" +bc.name() +"< failed. rc=>" +rc + "<.");
                setErrorMessage(Constants.ERROR, rc);
            }
        } catch (SiebelException e) {
            rc="Error query for >" + fieldValue +"< on field >" + fieldName +"<. Error =>" +e.toString() + "<.";
            log(myName, Constants.ERROR,myArea, rc);
            setErrorMessage(Constants.ERROR, rc);
        }


        return rc;
    }

/*
 * Collect all fields in Excel that have a BusComp notation that matches the bcName.
 * If provided bcName matches the bcName on the test page, fields without a bcName are considered part of the provided bcName
 */
    private List<String> determineFieldList(String bcName, List<String> potentialFieldList) {
        String myName="determineFieldList";
        List<String> determinedFieldList=new ArrayList<String>();
        
        String myArea="Check Field List";
        for ( int i=0 ; i < potentialFieldList.size(); ++i) {
            if(potentialFieldList.get(i).contains(".")) {
                String fieldBcName=null;
                fieldBcName =stripBrackets(potentialFieldList.get(i).substring(0, potentialFieldList.get(i).indexOf(".")));
                if(fieldBcName.equals(bcName)) {
                    determinedFieldList.add(potentialFieldList.get(i));
                    determinedIndices.add(i);
                    log(myName, Constants.VERBOSE,myArea,"Field >" + potentialFieldList.get(i) +"< is considered to be part of busComp >" + bcName +"<.");
                } else {
                    log(myName, Constants.DEBUG,myArea,"Field >" + potentialFieldList.get(i) +"< is NOT considered to be part of busComp >" + bcName +"< as its busComp is >" + fieldBcName +"<.");
                    addNonPrimaryBCField(potentialFieldList.get(i), fieldBcName, i);
                }
            } else {
                if(bcName.equals(getBusinessComponent())) {
                    determinedFieldList.add(potentialFieldList.get(i));
                    determinedIndices.add(i);
                    log(myName, Constants.VERBOSE,myArea,"Field >" + potentialFieldList.get(i) +"< is considered to be part of busComp >" + bcName +"<.");
                } else {
                    log(myName, Constants.DEBUG,myArea,"Field >" + potentialFieldList.get(i) +"< is NOT considered to be part of busComp >" + bcName +"<.");                    
                }
            }
        }
        
        return determinedFieldList;
    }

    private void addNonPrimaryBCField(String bcNameAndField, String bcName, int index) {
        String myName="addNonPrimaryBCField";
        String myArea="init";
        List<String> fieldInfo = new ArrayList<String>();
        
        log(myName, Constants.DEBUG, myArea, "Adding >" + bcNameAndField + "< to nonPrimaryBCField list as found in column >" + Integer.toString(index) +"<.");
        
        String bcField =stripBrackets(bcNameAndField.substring(bcNameAndField.indexOf(".") +1));

        fieldInfo.add(bcName);
        fieldInfo.add(bcField);
        fieldInfo.add(Integer.toString(index));
        nonPrimaryBCFields.add(fieldInfo);
        
    }

    private String stripBrackets(String s) {
        String t =s;
        if(s.substring(0,1).equals("[")) {
            t=s.substring(1);
        }
        if(t.substring(t.length()-1).equals("]")) {
            t=t.substring(0,t.length()-1);
        }
        return t;
    }

    private List<String> determineFieldValues(List<String> rowFromExcel) {
        List<String> filtered= new ArrayList<String>();
        
        for( int i =0 ; i < rowFromExcel.size() ; ++i) {
            if(determinedIndices.contains(i)) {
                filtered.add(rowFromExcel.get(i));
            } else {
            continue;
            }
        }
        
        return filtered;
    }

    private String getSalesMethod() {
        return salesMethod;
    }

    private void setLocationSalesMethod(int j) {
        locationSalesMethod =j;
    }

    private void setSalesMethod(String currentValForSalesMethod) {
        salesMethod=currentValForSalesMethod;
    }



    private String pickSalesStage(SiebelBusComp pickBC, String fieldName, String fieldValue) {
        String myName="pickSalesStage";
        String myArea="run";
        String rc = Constants.OK;
        
    String sExpr = Constants.NOT_INITIALIZED;

        try{
            pickBC.clearToQuery();
            sExpr="[Sales Cycle Stage] ='" + fieldValue +"'";
            log(myName, Constants.DEBUG,myArea,"PickBC=>" +pickBC.name() +"<. SearchExpr =>" + sExpr +"<.");
            pickBC.setSearchExpr(sExpr);
            try {
                pickBC.executeQuery(false);
                if(pickBC.firstRecord()) {
                    pickBC.pick();
                } else {
                    rc="Picking >" + fieldValue +"< on pickBC >" +pickBC.name() +"< failed. Reason >" +
                        Constants.NOT_FOUND +"<.";
                    log(myName, Constants.ERROR, myArea, rc);
                    setErrorMessage(Constants.ERROR, rc);
                }
            } catch (SiebelException e) {
                log(myName, Constants.ERROR, myArea,"Error during query execution using SearchSpec >" + sExpr + "< for pickBC >" + pickBC.name() +"<. Field >" +fieldName +"< FieldValue >" + fieldValue +"<. Error =>" + e.toString() +"<.");
                rc = Constants.ERROR;
            }
        } catch (SiebelException e) {
            log(myName, Constants.ERROR, myArea, "Error setting search spec >" + sExpr + "< for pickBC >" + pickBC.name() +"<. Field >" +fieldName +"< FieldValue >" + fieldValue +"<.");
            rc = Constants.ERROR;
        }
 return rc;
}

    private String HandleMvg(SiebelBusComp bc, SiebelFieldBean currField, String fieldValue, int lineNr) {
        String myName="HandleMvg";
        String myArea="Run";
        String progress ="Start";
        SiebelBusComp mvgBC =null;
        SiebelBusComp assocBC =null;
        String rc = Constants.OK;
        int viewMode =3;
        String msg=Constants.NOT_INITIALIZED;

        myArea="Handling MVG Field";
            log(myName, Constants.DEBUG,myArea, "MVG field >" +currField.getName()+"<.");
        try {
            assocBC =bc.getMVGBusComp(currField.getName()).getAssocBusComp();
            progress+=".GetAssoc=OK";
            viewMode = Constants.SIEBEL_VIEWMODES.indexOf(Constants.SIEBEL_VIEWMODE_ALL.toUpperCase());
            assocBC.setViewMode(viewMode); // All view
            progress+=".SetView=OK";
            assocBC.clearToQuery();
            progress+=".Clear=OK";
            String srchExpr=Constants.NOT_INITIALIZED;
            try {
                mvgBC =bc.getMVGBusComp(currField.getName());
                //assocBC.setSearchSpec("Address Name","LIKE *");
                if(mvgBC.name().equals("RB Contact Address AB")) {
                    srchExpr="[Address Name] LIKE '*' AND [" + Constants.SIEBEL_INTEGRATION_ID_FIELD +"] ='" + fieldValue +"'";
                    assocBC.setSearchExpr(srchExpr);
                } else {
                    srchExpr="[" + Constants.SIEBEL_INTEGRATION_ID_FIELD +"] ='" + fieldValue +"'";
                    assocBC.setSearchExpr(srchExpr);
                }
                progress+=".searchSpec=OK";
            } catch (SiebelException e) {
                //not all Siebel components have the field Integration Id.
                log(myName, Constants.WARNING,myArea,"Processing field >" + currField.getName() +"<. SetSearchSpec on assocBC >" 
                                                    + assocBC.name() +"< failed. Search Expression was >" +srchExpr +"<. Filter will be ignored.");
            }
            assocBC.executeQuery(false);
            progress+=".Query=OK";
            if(assocBC.firstRecord()) {
                progress+="firstRecord=OK";
                log(myName, Constants.DEBUG,myArea,"MVG record found. Field >"+currField.getName() +"<. Value >" + fieldValue +"<.");
                assocBC.associate(Constants.SIEBEL_NEWBEFORE);
                progress+="associate=OK";
            //Does not work as field is hidden                assocBC.setFieldValue("SSA Primary Field", Constants.Y);
                progress+="setprimary=OK";
                return rc;
            }
            progress+="firstRecord=NOTFOUND";
            rc = Constants.NOT_FOUND;
            setErrorMessage(Constants.WARNING, "Record to be associated was NOT found. Queried with " +
                             Constants.SIEBEL_INTEGRATION_ID_FIELD 
                                               + "=>" + fieldValue +"< on assocBusComp >" + assocBC.name() +"<. Will try to create a new record.");
            log(myName, Constants.ERROR,myArea,getErrorMessage());      
            
        } catch (SiebelException e) {  //Assoc BC
            msg ="Assoc Business Component failed. Error =>" + e.toString() +"<. Will try to create a new record instead of associating one.";
            log(myName, Constants.DEBUG,myArea, msg);
        }
        try {
            mvgBC =bc.getMVGBusComp(currField.getName());
            progress+=".GetMVG=OK";
            mvgBC.newRecord(true);
            progress+=".NewRecord=OK";
            mvgBC.setFieldValue(Constants.SIEBEL_INTEGRATION_ID_FIELD, fieldValue);
            progress+=".SetFieldIntId=OK";
            
            myArea="Handling nonPrimaryBCFields";
            Iterator<List<String>> fieldIterator = nonPrimaryBCFields.iterator();
            while (fieldIterator.hasNext()) {

                List<String> entry =fieldIterator.next();
                msg="next nonPrimaryBC field =>" + entry.toString() +"<.";
                log(myName, Constants.VERBOSE, myArea, msg);

                if(entry.get(0).equals(mvgBC.name())) {
                    if(entry.size() ==3) {
                        msg="bcName=>" +entry.get(0) +"< bcField=>" +entry.get(1) +"< index=>" +entry.get(2) +"< for Excel line# >" +lineNr +"<.";
                        log(myName, Constants.VERBOSE, myArea, msg);
                        mvgBC.setFieldValue(entry.get(1), dataToLoad.get(lineNr).get(Integer.parseInt(entry.get(2))));
                    } else {
                        setErrorMessage(Constants.FATAL, "Internal error: The field iterator has an incomplete entry.");
                        log(myName, Constants.FATAL, myArea, getErrorMessage());
                        return Constants.FATAL;
                    }
                }
                
            }

            msg="Writing record...";
            log(myName, Constants.VERBOSE, myArea, msg);
            mvgBC.writeRecord();
            msg="Write record completed.";
            log(myName, Constants.VERBOSE, myArea, msg);
            progress+=".WriteRecord=OK";

        } catch (SiebelException e) {
            setErrorMessage(Constants.ERROR, "MVG Business Component >" + currField.getMvlBusCompName() + "< failed. Progress =>" + progress +"<. Error =>" + e.toString() +"<.");
            log(myName, Constants.ERROR,myArea,getErrorMessage());
            rc = Constants.ERROR;
        }
    return rc;    
    }

    private void setCurrentPickField(String fieldName) {
        this.currentPickField =fieldName;
    }
    
    private String getCurrentPickField() {
        return this.currentPickField;
    }

    private String determineViewMode(String busComp) {
        String myName="determineViewMode";
        String myArea="run";
        String rc = Constants.OK;
        
        //the method getBusCompProperties creates a new instance and will return it, if successful
        SiebelBusCompBean bc =null;
        bc =suppSbl.getBusCompProperties(sblBean, busComp);
        if(bc == null) {
            setErrorMessage(suppSbl.getErrorLevel(), suppSbl.getErrorMessage());
            log(myName, suppSbl.getErrorLevel(), myArea, getErrorMessage());
            rc = Constants.NOT_FOUND;
        } else {
            rc =bc.getPopupVisibilityType();
        }
        return rc;
    }
    
}
