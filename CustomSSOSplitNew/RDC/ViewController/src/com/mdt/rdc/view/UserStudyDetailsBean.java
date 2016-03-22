package com.mdt.rdc.view;


import com.mdt.sso.view.ADFUtils;
import com.mdt.sso.view.PasswordEncoderUtil;
import com.mdt.sso.view.SSOUtils;
import com.mdt.sso.view.UserPasswordServiceUtil;

import java.io.Serializable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import javax.servlet.http.HttpServletRequest;

import javax.sql.DataSource;


public class UserStudyDetailsBean implements Serializable{
    @SuppressWarnings("compatibility:1833587502634901697")
    private static final long serialVersionUID = 6525028192348714870L;
    private String userName;
    private Map <String , String> studyUrlMap;
    private String selectedStudyName;
    private String selectedDBName;
    private List <String> studyList;
    private String password;
    private String returnVal;
    private String errorMsg;
    private boolean singleStudy;
    private String selectedStudyRDCUrl;
   // private static final Properties propertiesFromFile = new Properties();
    public UserStudyDetailsBean() {
        super();
    }

    public void setStudyUrlMap(Map<String, String> studyUrlMap) {
        this.studyUrlMap = studyUrlMap;
    }

    public Map<String, String> getStudyUrlMap() {
        return studyUrlMap;
    }

    public void setSelectedStudyName(String selectedStudyName) {
        this.selectedStudyName = selectedStudyName;
    }

    public String getSelectedStudyName() {
        return selectedStudyName;
    }

   
    public void setStudyList(List<String> studyList) {
        this.studyList = studyList;
    }

    public List<String> getStudyList() {
        return studyList;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
    }
    public String initSSOLogin() {
        this.errorMsg = "";
        this.selectedDBName = "";
        this.setSingleStudy(Boolean.FALSE);
        
        String smUserFrmHeader = null;
        this.returnVal = "studyList";
        FacesContext ctx = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest)ctx.getExternalContext().getRequest();        
             
        smUserFrmHeader = request.getHeader("SM_USER");
        if (null == smUserFrmHeader || smUserFrmHeader.isEmpty()){
            smUserFrmHeader = request.getHeader("sm_user");       
        }
        System.out.println("smUserFrmHeader..." + smUserFrmHeader);   
        
        if (null != smUserFrmHeader && !smUserFrmHeader.isEmpty()){
                this.userName = smUserFrmHeader;                
                this.password = PasswordEncoderUtil.decodeStr(UserPasswordServiceUtil.getUserPassword(this.userName));
                System.out.println("password Name from DB - "+password);
                prepareUserStudyMap(smUserFrmHeader);
                
                if (null == this.studyList || studyList.size() == 0){
                    // add new function call to check if the user exists in any of the databases or not
                    // If the user is not exists in any one of the data bases error message should be user not configured in database
                    this.errorMsg = "No Study is associated with the logged in user. Please try with valid User.";
//                    if (null != loginBean){
//                        loginBean.clear();    
//                    }
                    this.returnVal = "error";
                
                } else if (studyList.size() == 1){
                        this.selectedDBName = studyUrlMap.get(studyList.get(0));
                        this.setSingleStudy(Boolean.TRUE);
                        // if only one study , need to send post request to RDC URL based on selected study
                       sendRedirectToRDC(selectedDBName, request);
                }
          
        } else {
            this.errorMsg = "Invalid Username / Password. Access Denied.";
//            if (null != loginBean){
//                loginBean.clear();    
//            }
            this.returnVal = "error";
        } 
        return this.returnVal;

        
  //      return "studyList";
    }
      
    private void reportUnexpectedLoginError(String errType, Exception e) {
         FacesMessage msg =
             new FacesMessage(FacesMessage.SEVERITY_ERROR, "Unexpected error during login", "Unexpected error during login (" +
                              errType + "), please consult logs for detail");
         FacesContext.getCurrentInstance().addMessage(null, msg);
         e.printStackTrace();
     }

     public void processUserStudySelection(ActionEvent event) {
         // Add event code here...
         FacesContext ctx = FacesContext.getCurrentInstance();
         HttpServletRequest request = (HttpServletRequest)ctx.getExternalContext().getRequest();
         System.out.println("processUserStudySelection...Selected Study DB name..." + selectedDBName);
         sendRedirectToRDC(this.selectedDBName, request);
         
     }
    public void setReturnVal(String returnVal) {
        this.returnVal = returnVal;
    }

    public String getReturnVal() {
        return returnVal;
    }

    public String getRDCUrlFromStudy(String dbName, HttpServletRequest request){
        System.out.println("getRDCUrlFromStudy...Selected Study DB name::" + dbName);
        String rdcURL = null;
        if (null != dbName && !dbName.isEmpty()){
            rdcURL = SSOUtils.getPropertyValue(dbName);
            System.out.println("RedirectUrl from properties file ::" + rdcURL);
        }
        return rdcURL;
    }
    public void prepareUserStudyMap(String loginId){
        Connection conn = null;
        System.out.println("Login User ID..." + loginId);
        studyUrlMap = new HashMap<String, String>();
        studyList = new ArrayList<String>();
        this.selectedDBName = null;
        //get db connection from DBUtil
        conn = SSOUtils.getConnection();
        if (null != conn){
            StringBuilder sb = new StringBuilder();
            sb.append("select user_id, fq_db_name,study_assigned,level_type from SSO_RDC_USER_MASTER_LIST ");  
            sb.append("where upper(user_id) = ? ");
            sb.append("and level_type = ? "); 
            sb.append("and rdc_mode = ? ");
            PreparedStatement stmt = null;
            ResultSet rs = null;
            System.out.println("***** : " + sb.toString());
            try {
                stmt = conn.prepareStatement(sb.toString());
                stmt.setString(1, loginId.toUpperCase());
                stmt.setString(2, "STUDY");
                stmt.setString(3, "PROD");
                rs = stmt.executeQuery();
               // prepare studylist map and list
                String dbName = "";
                while (rs.next()) {
                    String studySiteUrl = rs.getString("fq_db_name");
                    if (null != studySiteUrl){
                        // study URL will be like this from data base look up query : mmopat.devl.corp.medtronic.com
                        int index = studySiteUrl.indexOf(".");
                        System.out.println("index..." + index);
                        // get the db name from Study URL
                        if (index > 0){
                            dbName = studySiteUrl.substring(0, index);
                        } else {
                            dbName = studySiteUrl.substring(0);  
                        }
                        studyUrlMap.put(rs.getString("study_assigned"), dbName);
                        studyList.add(rs.getString("study_assigned"));             
                    }     
                }
                System.out.println("User Study List..." + studyList);
                
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                if (null != rs){
                    try {
                        rs.close();
                    } catch (SQLException sqe){
                        System.out.println("error while closing result set..." + sqe);     
                    }
                }
                if (null != stmt){
                    try {
                        stmt.close();
                    } catch (SQLException sqe){
                        System.out.println("error while closing callable statment..." + sqe);     
                    }
                }
            }
        }
    }
    
    private void sendRedirectToRDC(String dbName, HttpServletRequest request) {                  
       //String rdcUrl = null;
       if (null != dbName) {
       // get the RDC Login URL with params from ssourl.properties file based on db value
           this.selectedStudyRDCUrl = getRDCUrlFromStudy(dbName, request);
           System.out.println("RedirectUrl ::" + this.selectedStudyRDCUrl);
           // Call RDC silent Login JS function here           
           String scriptText = "renderRDCApplication('"+this.userName+"','"+this.password+"','"+this.selectedStudyRDCUrl+"')";
           boolean isPasswdUpdate = updatePassword(dbName, userName, password);
           System.out.println("isPasswdUpdate..." + isPasswdUpdate);
           ADFUtils.addJavaScript(scriptText);
           //Resetting the LastAccessedStudy for the user
           UserPasswordServiceUtil.resetLastAccessedStudy(this.userName, this.selectedStudyRDCUrl);
       } else {
           this.errorMsg = "Selected Study URL not found. Please consult logs for detail.";
           this.returnVal = "error";
       }
    }

    public void setSelectedDBName(String selectedDBName) {
        this.selectedDBName = selectedDBName;
    }

    public String getSelectedDBName() {
        return selectedDBName;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setSingleStudy(boolean singleStudy) {
        this.singleStudy = singleStudy;
    }

    public boolean isSingleStudy() {
        return singleStudy;
    }

    public void setSelectedStudyRDCUrl(String selectedStudyRDCUrl) {
        this.selectedStudyRDCUrl = selectedStudyRDCUrl;
    }

    public String getSelectedStudyRDCUrl() {
        return selectedStudyRDCUrl;
    }
    public void invokeRDCLogin(){
        ADFUtils.addJavaScript("renderRDCApplication('"+this.userName+"','"+this.password+"','"+this.selectedStudyRDCUrl+"')");
    }
    private boolean updatePassword(String dbName, String userName, String password){
        boolean isUpdated = Boolean.FALSE;
        // need to replace this code with a function call to update the password for the user in all databases where this user account is present
        String dsName = "jdbc/rdc"+dbName+"DS";
        Connection conn = null;
        Statement stmt = null;
        try {
            InitialContext initialContext = new InitialContext();
            DataSource ds = (DataSource)initialContext.lookup(dsName);
            conn = ds.getConnection();
            String updateQry = "alter user " + userName.toUpperCase() + " identified by " + password;
            if (null != conn){
                stmt = conn.createStatement();
                stmt.executeUpdate(updateQry);
                isUpdated = Boolean.TRUE;
            }
        } catch (NamingException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("SQLException in update Password..." + e.getMessage());
            e.printStackTrace();
        } finally {
            if (null != stmt){
                try {
                    stmt.close();
                } catch (SQLException sqe){
                    System.out.println("error while closing callable statment..." + sqe.getMessage());     
                }
            }    
        }
        return isUpdated;
        
    }
}
