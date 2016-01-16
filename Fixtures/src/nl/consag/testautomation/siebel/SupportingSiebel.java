/**
 * Siebel connections
 * @author Jac. Beekers
 * @since 31 May 2015
 * @version 20151220.0
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

import java.text.SimpleDateFormat;

import java.util.Properties;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

import nl.consag.supporting.Constants;
import nl.consag.supporting.Logging;

public class SupportingSiebel {
    private static String version ="20151220.0";

    private String className = "SupportingSiebel";
    private String logFileName = Constants.NOT_INITIALIZED;
    private String context = Constants.DEFAULT;
    private String startDate = Constants.NOT_INITIALIZED;
    private boolean firstTime = true;
    private int logLevel =3;

    private String siebelConnValue = Constants.NOT_PROVIDED;
    private String siebelUser = Constants.NOT_PROVIDED;

    private String siebelPwd = Constants.NOT_PROVIDED;
    private String siebelLang = Constants.NOT_PROVIDED;        
    private String siebelSSO = Constants.NOT_PROVIDED;        
    private String siebelTrustToken = Constants.NOT_PROVIDED;
//    private SiebelDataBean repConnection = new SiebelDataBean();
    private String repositorySessionId = Constants.SIEBEL_NOTCONNECTED;
    
    private String errorMessage = Constants.NOERRORS;
    private String errorLevel = Constants.OK;

    private String currentPickField = Constants.NOT_FOUND;


    public SupportingSiebel() {
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
    public SupportingSiebel(String context) {
            java.util.Date started = new java.util.Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            startDate = sdf.format(started);
            this.context=context;
            logFileName = this.startDate + "." + this.className +"." +this.context;
        }

    public static String getVersion() {
        return version;
    }

    public String getSiebelConnValue() {
        return this.siebelConnValue;
    }
    public String getSiebelUser() {
        return this.siebelUser;
    }
    protected void setSiebelUser(String siebelUser) {
        this.siebelUser = siebelUser;
    }
    public void setSiebelPwd(String pwd) {
        this.siebelPwd = pwd;
    }

    public String getSiebelPwd() {
        return this.siebelPwd;
    }
    public String getSiebelLang() {
        return this.siebelLang;
    }
    public boolean getSiebelSSO() {
        if (Constants.TRUE.equalsIgnoreCase(this.siebelSSO))
            return true;
        else
            return false;
    }
    
    public String getSiebelTrustToken() {
        return this.siebelTrustToken;
    }
    
    /*
     * read Siebel connectivity properties file
     */
    public void readSiebelProperties(String siebelConn) {
        String myName="readSiebelProperties";

        try {
        File file = new File(Constants.SIEBEL_PROPERTIES);
        FileInputStream fileInput = new FileInputStream(file);
        Properties sblProp = new Properties();
        sblProp.load(fileInput);
        fileInput.close();
        
        siebelConnValue=sblProp.getProperty(siebelConn, Constants.NOT_FOUND);
        siebelUser=sblProp.getProperty(siebelConn + Constants.SIEBEL_USER, Constants.NOT_FOUND);
        siebelPwd=sblProp.getProperty(siebelConn + Constants.SIEBEL_PWD, Constants.NOT_FOUND);
        siebelLang=sblProp.getProperty(siebelConn + Constants.SIEBEL_LANG, Constants.NOT_FOUND);
        siebelSSO=sblProp.getProperty(siebelConn + Constants.SIEBEL_SSO, Constants.NOT_FOUND);
        siebelTrustToken=sblProp.getProperty(siebelConn + Constants.SIEBEL_TRUSTTOKEN, Constants.NOT_FOUND);

            try {
                    String key1 ="oas38seia0d72jvy";
                    String key2 ="supeThatIsConsag";

                    IvParameterSpec iv = new IvParameterSpec(key2.getBytes("UTF-8"));

                    SecretKeySpec skeySpec = new SecretKeySpec(key1.getBytes("UTF-8"),
                            "AES");
                    Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
                    cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
                    byte[] original = cipher.doFinal(Base64.decodeBase64(siebelPwd));
                    siebelPwd=new String(original);

                    log(myName, Constants.VERBOSE,"Password decryption", "Password determined successfully.");

                } catch (Exception e) {
                    log(myName, Constants.FATAL,"Password decryption",e.toString());
                }

        } catch (FileNotFoundException e) {
            siebelConnValue = Constants.NOT_FOUND;
            siebelUser = Constants.NOT_FOUND;
            siebelPwd = Constants.NOT_FOUND;
            siebelLang = Constants.NOT_FOUND;
            siebelSSO = Constants.NOT_FOUND;
            siebelTrustToken = Constants.NOT_FOUND;
            } catch (IOException e) {
                siebelConnValue = Constants.NOT_FOUND;
                siebelUser = Constants.NOT_FOUND;
                siebelPwd = Constants.NOT_FOUND;
                siebelLang = Constants.NOT_FOUND;
                siebelSSO = Constants.NOT_FOUND;
                siebelTrustToken = Constants.NOT_FOUND;
            }

        log(myName, Constants.VERBOSE,"Property results", "================================");
        log(myName, Constants.VERBOSE,"Property results", "siebelConnValue  =>" + siebelConnValue +"<.");
        log(myName, Constants.VERBOSE,"Property results", "siebelUser       =>" + siebelUser +"<.");
        log(myName, Constants.VERBOSE,"Property results", "siebelLang       =>" + siebelLang +"<.");
        log(myName, Constants.VERBOSE,"Property results", "siebelSSO        =>" + siebelSSO +"<.");
        log(myName, Constants.VERBOSE,"Property results", "siebelTrustToken =>" + siebelTrustToken +"<.");
        log(myName, Constants.VERBOSE,"Property results", "================================");

    }

    /**
     * @param name
     * @param level
     * @param location
     * @param logText
     * @param logLevel
     */
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


    public String connect(SiebelDataBean newConn, String userId) {
        String myName="connect";
        String myArea="run";
        
        String ssoEnabled = Constants.NO;
        String rc = Constants.OK;
        
        try {
        if(this.getSiebelSSO()) {
            ssoEnabled = Constants.YES;
            newConn.login(this.getSiebelConnValue(), userId, this.getSiebelTrustToken(), this.getSiebelLang());
        }
        else {
            ssoEnabled = Constants.NO;
            newConn.login(this.getSiebelConnValue(), userId, this.getSiebelPwd(), this.getSiebelLang());
        }
        } catch (SiebelException e) {
            setErrorMessage(Constants.ERROR, "Connection to >" + getSiebelConnValue() +"< failed. User >" 
                            + userId +"<. Language >" + this.getSiebelLang() + "<. SSOEnabled >" + ssoEnabled +"< with token >" 
                            + this.getSiebelTrustToken() +"<. Error =>" + e.toString() +"<.");
            log(myName, Constants.ERROR, myArea, getErrorMessage());
            rc = Constants.ERROR;
        }
        
        return rc;
    }
    
    public String reconnect(SiebelDataBean toConn, String userId) {
        String myName="reconnect";
        String myArea="run";
        String msg = Constants.NOT_INITIALIZED;
        String rc = Constants.OK;
        boolean b=true;
        
        if(getRepositorySessionId().equals(Constants.SIEBEL_NOTCONNECTED)) {
            msg="Not connected yet. Invoking connect method.";
            log(myName, Constants.DEBUG, myArea, msg);
            rc =this.connect(toConn, userId);
            if(!Constants.OK.equals(rc)) {
                b=false;
                log(myName, Constants.DEBUG, myArea, "Connection failed.");
            } else {
                log(myName, Constants.DEBUG, myArea, "Connection established.");
            }
        } else {
            msg="Session >" + getRepositorySessionId() + "< is available. Trying to attach to it...";
            log(myName, Constants.DEBUG, myArea, msg);
            try {
                b = toConn.attach(getRepositorySessionId());
                msg="Attach to session >" + getRepositorySessionId() + "< succeeded.";
                log(myName, Constants.DEBUG, myArea, msg);
            } catch (SiebelException e) {
                setErrorMessage(Constants.ERROR, "Could not attach to previous sessionId >" +getRepositorySessionId() +"<. Error =>" + e.toString() + "<.");
                log(myName, Constants.ERROR, myArea, getErrorMessage());
            }
        }
        
        if(b) return Constants.OK; 
        else return Constants.ERROR;
        
    }
    
    public String detach(SiebelDataBean thisConn) {
        String sessionId = Constants.SIEBEL_NOTCONNECTED;
    
     logoff(thisConn);
     return sessionId;
    
//        if(getRepositorySessionId().equals(Constants.SIEBEL_NOTCONNECTED)) {
//            return Constants.SIEBEL_NOTCONNECTED;
//        }
        
/*        try {
            sessionId = thisConn.detach();
            setRepositorySessionId(sessionId);
        } catch (SiebelException e) {
            sessionId =Constants.SIEBEL_NOTCONNECTED;
            setErrorMessage(Constants.ERROR, "Could not detach from current session id >" + getRepositorySessionId() +"<. Error =>" + e.toString() + "<.");
        }
        
        return sessionId;
*/
    }
    
    public String logoff(SiebelDataBean thisConn) {
        String myName ="logoff";
        String myArea ="run";
        String rc = Constants.OK;
        
        try {
            thisConn.logoff();
        } catch (SiebelException e) {
            setErrorMessage("Logoff failed. Error >" +e.toString() +"<.");
            rc = Constants.WARNING;
            log(myName, Constants.WARNING,myArea,getErrorMessage());
        }
        
        return rc;

    }
    
    public String setProfileAttributes(SiebelDataBean thisConn) {
        String myName ="setProfileAttributes";
        String myArea ="init";
        String rc = Constants.OK;
        
        /*
         *  2015-02-16 Rabobank: If GUI_User =Y, restrictions on queries etc. are enforced
         *  For others parties the setProfileAttr will result in a dynamic attribute, which should not matter.
         */
                myArea="Set profile attributes";
                try {
                    thisConn.setProfileAttr("GUI_User", "N");
                    log(myName, Constants.DEBUG,myArea,"Profile attribute >GUI_User< =>N<.");
                } catch (SiebelException e) {
                    setErrorMessage("Profile attribute >GUI_User< could not be set. Error >" +e.toString() + "<.");
                    log(myName, Constants.WARNING,myArea,getErrorMessage());
                    rc = Constants.WARNING;
                }
                try {
                    thisConn.setProfileAttr("MemberAdmin", "Y");
                    log(myName, Constants.DEBUG,myArea,"Profile attribute >MemberAdmin< =>Y<.");
                } catch (SiebelException e) {
                    setErrorMessage("Profile attribute >MemberAdmin< could not be set. Error >" +e.toString() + "<.");
                    log(myName, Constants.WARNING,myArea,getErrorMessage());
                    rc = Constants.WARNING;
                }
                try {
                    thisConn.setProfileAttr("RB TBUI Flag", "Y");
                    log(myName, Constants.DEBUG,myArea,"Profile attribute >RB TBUI Flag< =>Y<.");
                } catch (SiebelException e) {
                    setErrorMessage("Profile attribute >RB TBUI Flag< could not be set. Error >" +e.toString() + "<.");
                    log(myName, Constants.WARNING,myArea,getErrorMessage());
                    rc = Constants.WARNING;
                }
                
                try {
                    thisConn.setProfileAttr("Read", "Y");
                    log(myName, Constants.DEBUG,myArea,"Profile attribute >Read< =>Y<.");
                } catch (SiebelException e) {
                    setErrorMessage("Profile attribute >Read< could not be set. Error >" +e.toString() + "<.");
                    log(myName, Constants.WARNING,myArea,getErrorMessage());
                    rc = Constants.WARNING;
                }
        try {
            thisConn.setProfileAttr("Edit", "Y");
            log(myName, Constants.DEBUG,myArea,"Profile attribute >Edit< =>Y<.");
        } catch (SiebelException e) {
            setErrorMessage("Profile attribute >Edit< could not be set. Error >" +e.toString() + "<.");
            log(myName, Constants.WARNING,myArea,getErrorMessage());
            rc = Constants.WARNING;
        }
        try {
            thisConn.setProfileAttr("IsViewReadOnly", "N");
            log(myName, Constants.DEBUG,myArea,"Profile attribute >IsViewReadOnly< =>N<.");
        } catch (SiebelException e) {
            setErrorMessage("Profile attribute >IsViewReadOnly< could not be set. Error >" +e.toString() + "<.");
            log(myName, Constants.WARNING,myArea,getErrorMessage());
            rc = Constants.WARNING;
        }

        try {
            thisConn.setProfileAttr("RB Customer Create Update", "Y");
            log(myName, Constants.DEBUG,myArea,"Profile attribute >RB Customer Create Update< =>Y<.");
        } catch (SiebelException e) {
            setErrorMessage("Profile attribute >RB Customer Create Update< could not be set. Error >" +e.toString() + "<.");
            log(myName, Constants.WARNING,myArea,getErrorMessage());
            rc = Constants.WARNING;
        }

        try {
            thisConn.setProfileAttr("CentraalRaadplegen", "Y");
            log(myName, Constants.DEBUG,myArea,"Profile attribute >CentraalRaadplegen< =>Y<.");
        } catch (SiebelException e) {
            setErrorMessage("Profile attribute >CentraalRaadplegen< could not be set. Error >" +e.toString() + "<.");
            log(myName, Constants.WARNING,myArea,getErrorMessage());
            rc = Constants.WARNING;
        }

        try {
            thisConn.setProfileAttr("Admin Anonymous", "Y");
            log(myName, Constants.DEBUG,myArea,"Profile attribute >Admin Anonymous< =>Y<.");
        } catch (SiebelException e) {
            setErrorMessage("Profile attribute >Admin Anonymous< could not be set. Error >" +e.toString() + "<.");
            log(myName, Constants.WARNING,myArea,getErrorMessage());
            rc = Constants.WARNING;
        }


        return rc;

    }


    public SiebelBusCompBean getBusCompProperties(SiebelDataBean conn, String bcName) {
        String myName="getBusCompProperties";
        String myArea="run";
        
        SiebelBusObject bo=null;
        SiebelBusComp bcBC=null;
        SiebelBusCompBean currBC = new SiebelBusCompBean();
        boolean ok=true;
//        String sessionId=Constants.SIEBEL_NOTCONNECTED;
//        String useUserId =Constants.NOT_PROVIDED;

//        if(Constants.NOT_PROVIDED.equals(userId))
//             useUserId = getSiebelUser();
//         else
//             useUserId = userId;

//        this.connect(conn, useUserId);
//        this.reconnect(conn, useUserId);
        
        //Get instances of Siebel business object and  component 
        try {
            bo = conn.getBusObject("Repository Business Component");
            try {
                bcBC = bo.getBusComp("Repository Business Component");
                bcBC.activateField("Name");
                bcBC.clearToQuery();
                bcBC.setSearchSpec("Name", bcName);
                bcBC.executeQuery(false);
                if(bcBC.firstRecord()) {
                    currBC.setRowId(bcBC.getFieldValue("Id"));
                    currBC.setPopupVisibilityType(bcBC.getFieldValue("Popup Visibility Type"));
                }
            } catch (SiebelException e) {
                ok=false;
                setErrorMessage(Constants.ERROR, "Could not find BusComp >" +bcName +"<. Error =>" +e.toString() + "<.");
                log(myName, Constants.ERROR, myArea, getErrorMessage());
                cleanup();
            }
        } catch (SiebelException e) {
            ok=false;
            setErrorMessage(Constants.ERROR, "Could not get the repository object >Repository Business Component<. Error =>" +e.toString() +"<.");
            log(myName, Constants.ERROR, myArea, getErrorMessage());
            cleanup();
        }
        
//    sessionId =this.detach(conn);
//    setRepositorySessionId(sessionId);
//    this.logoff(conn);
    
    
    if(ok) return currBC;
    else return null;
        
    }


    /**
     * @param bc
     * @param fieldName
     * @return
     */
    public SiebelFieldBean getBusCompFieldProperties(SiebelDataBean conn, SiebelBusComp bc, String fieldName) {
        
        SiebelBusObject bo=null;
        SiebelBusComp bcBC=null;
        SiebelBusComp fieldBC=null;
        boolean ok=true;
        String bcId = Constants.NOT_FOUND;
        SiebelFieldBean currField = new SiebelFieldBean();
        String useUserId = Constants.NOT_PROVIDED;
            
//        this.reconnect(conn, userId);
//        if(Constants.NOT_PROVIDED.equals(userId))
//             useUserId = getSiebelUser();
//         else
//             useUserId = userId;
//        this.connect(conn, useUserId);
        
        //Get instances of Siebel business object and  component 
        try {
            bo = conn.getBusObject("Repository Business Component");
            try {
                bcBC = bo.getBusComp("Repository Business Component");
                bcBC.activateField("Name");
                bcBC.clearToQuery();
                bcBC.setSearchSpec("Name", bc.name());
                bcBC.executeQuery(false);
                if(bcBC.firstRecord()) {
                    bcId=bcBC.getFieldValue("Id");
                    try {
                        fieldBC =bo.getBusComp("Repository Field");
                        fieldBC.activateField("Name");
                        fieldBC.clearToQuery();
                        fieldBC.setSearchSpec("Name", fieldName);
                        fieldBC.executeQuery(false);
                        if(fieldBC.firstRecord()) {
                            currField.setName(fieldName);
                            currField.setPickListName(fieldBC.getFieldValue("PickList"));
                            currField.setJoinName(fieldBC.getFieldValue("Join"));
                            currField.setIsReadOnly(fieldBC.getFieldValue("Read Only"));
                            currField.setIsMultiValued(fieldBC.getFieldValue("Multi Valued"));
                            currField.setLinkSpecification(fieldBC.getFieldValue("Link Specification"));  // Y or N if field has the mvl spec
                            currField.setMvlBusCompName(fieldBC.getFieldValue("MV Link BusComp Name"));
                            currField.setMultiValueLink(fieldBC.getFieldValue("Multi Value Link"));
                            currField.setType(fieldBC.getFieldValue("Type"));
                            ok=true;
                            if(currField.getPickListName() == null || currField.getPickListName().isEmpty() || currField.getPickListName().equals(Constants.NOT_FOUND)) {
                                currField.setHasPickList(Constants.NO); 
                            } else {
                                currField.setHasPickList(Constants.YES);    
                            }
                        } else {
                            setErrorMessage("Field >" + fieldName +"< not found in BusComp >" + bc.name() + "<.");
                            ok=false;
                            cleanup();
                              }
                    } catch (SiebelException e) {
                        setErrorMessage("Error repository query on field. Msg =>" + e.toString() +"<.");
                        ok=false;
                        cleanup();
                        }
                } else {
                    setErrorMessage("BusComp >" + bc.name() + "< not found in Siebel repository.");
                    ok=false;
                    cleanup();
                }
            } catch (SiebelException e) {
                setErrorMessage("Error repository query on busComp. Msg =>" + e.toString() +"<.");
                ok=false;
                cleanup();
            }
        } catch (SiebelException e) {
            setErrorMessage("Error get BusObject repository. Msg =>" + e.toString() +".");
            ok=false;
            cleanup();
        }
        
        if(ok) return currField;
        else return null;
    }

    protected void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    protected void setErrorMessage(String errorLevel, String errorMessage) {
        setErrorLevel(errorLevel);
        this.errorMessage = errorMessage;
    }

    protected String getErrorMessage() {
        return errorMessage;
    }
    protected void setErrorLevel(String errorLevel) {
        this.errorLevel =errorLevel;
    }
    public String getErrorLevel() {
        return this.errorLevel;
    }

    private void setRepositorySessionId(String sessionId) {
        this.repositorySessionId = sessionId;
    }
    public String getRepositorySessionId() {
        return this.repositorySessionId;
    }

    private void cleanup() {
//        this.logoff(conn);
//        setRepositorySessionId(Constants.SIEBEL_NOTCONNECTED);

    }
}
