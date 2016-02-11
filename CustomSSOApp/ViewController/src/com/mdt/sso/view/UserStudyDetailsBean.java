package com.mdt.sso.view;

import java.io.Serializable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.sql.SQLException;

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
import javax.servlet.http.HttpSession;

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
        // get LoginBean from pageflow scope
        LoginBean loginBean = null;
        loginBean = (LoginBean) ADFUtils.evaluateEL("#{sessionScope.loginBean}");
        if (null != loginBean){
            this.userName = loginBean.getUsername().toUpperCase();
            this.password = loginBean.getPassword();
        }
        System.out.println("User Name from session - "+userName);
        //System.out.println("Password - "+password);
        
        smUserFrmHeader = request.getHeader("SM_USER");
        if (null == smUserFrmHeader || smUserFrmHeader.isEmpty()){
            smUserFrmHeader = request.getParameter("SM_USER");
        }
        //String loginStatus = request.getParameter("status");
        //String rolesFromFcc = request.getParameter("acrole");
        //String hostFromFcc = request.getHeader("host");
        System.out.println("smUserFrmHeader..." + smUserFrmHeader);
        //System.out.println("rolesFromFcc..." + rolesFromFcc);
        //System.out.println("hostFromFcc..." + hostFromFcc);
        // comment this after site minder integration
        
//        if (null == smUserFrmHeader || smUserFrmHeader.isEmpty()){
//            smUserFrmHeader = this.userName;
//        }
//        // comment this block after site minder integration
//        if (null == loginStatus || loginStatus.isEmpty()){
//            loginStatus = "success";
//        }
        if (null != smUserFrmHeader && !smUserFrmHeader.isEmpty() && smUserFrmHeader.equalsIgnoreCase(this.userName)){
                prepareUserStudyMap(smUserFrmHeader);
                if (null == this.studyList || studyList.size() == 0){
                    this.errorMsg = "No Study is associated with the logged in user. Please try with valid User.";
                    this.returnVal = "error";
                } else if (studyList.size() == 1){
                        this.selectedDBName = studyUrlMap.get(studyList.get(0));
                        this.setSingleStudy(Boolean.TRUE);
                        // if only one study , need to send post request to RDC URL based on selected study
                       sendRedirectToRDC(selectedDBName, request);
                }
          
        } else {
            this.errorMsg = "Invalid User name / password. Access Denied.";
            loginBean.clear();
            this.returnVal = "error";
        }
        return this.returnVal;
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
           request.setAttribute("user", this.userName);
           request.setAttribute("password", this.password);
           request.setAttribute("rdcUrl", this.selectedStudyRDCUrl);              
          
           // Call RDC silent Login JS function here
           
           String scriptText = "renderRDCApplication('"+this.userName+"','"+this.password+"','"+this.selectedStudyRDCUrl+"')";
           System.out.println("calling js - "+scriptText);
           request.setAttribute("redirectScript", scriptText);
//           boolean isPasswdUpdate = updatePassword(dbName, userName, password);
//           System.out.println("isPasswdUpdate..." + isPasswdUpdate);
           ADFUtils.addJavaScript(scriptText);
           
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
        System.out.println("calling js - "+"renderRDCApplication('"+this.userName+"','"+this.password+"','"+this.selectedStudyRDCUrl+"')");
        ADFUtils.addJavaScript("renderRDCApplication('"+this.userName+"','"+this.password+"','"+this.selectedStudyRDCUrl+"')");
    }
    private boolean updatePassword(String dbName, String userName, String password){
        boolean isUpdated = Boolean.FALSE;
        String dsName = "jdbc/rdc"+dbName+"DS";
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            InitialContext initialContext = new InitialContext();
            DataSource ds = (DataSource)initialContext.lookup(dsName);
            conn = ds.getConnection();
            String updateQry = "alter user ? identified by ?";
            if (null != conn){
                stmt = conn.prepareCall(updateQry);
                stmt.setString(1, userName.toUpperCase());
                stmt.setString(2, password);
                stmt.executeUpdate();
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
                    System.out.println("error while closing callable statment..." + sqe);     
                }
            }    
        }
        return isUpdated;
        
    }
}
