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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;


public class UserStudyDetailsBean implements Serializable{
    @SuppressWarnings("compatibility:-1330330870396582421")
    private static final long serialVersionUID = 6525028192348714870L;
    private String userName;
    private Map <String , String> studyUrlMap;
    private String selectedStudyName;
    private String selectedDBName;
    private List <String> studyList;
    private String password;
    private String returnVal;
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
        
        String smUserFrmHeader = null;
        this.returnVal = "studyList";
        FacesContext ctx = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest)ctx.getExternalContext().getRequest();
        // get LoginBean from pageflow scope
        LoginBean loginBean = null;
        loginBean = (LoginBean) ADFUtils.evaluateEL("#{sessionScope.loginBean}");
        if (null != loginBean){
            this.userName = loginBean.getUsername();
            this.password = loginBean.getPassword();
        }
        System.out.println("User Name - "+userName);
        System.out.println("Password - "+password);
        
        smUserFrmHeader = request.getHeader("sm_user");
        String loginStatus = request.getParameter("status");
        String rolesFromFcc = request.getParameter("acrole");
        String hostFromFcc = request.getHeader("host");
        System.out.println("smUserFrmHeader..." + smUserFrmHeader);
        System.out.println("rolesFromFcc..." + rolesFromFcc);
        System.out.println("hostFromFcc..." + hostFromFcc);
        //if (null != smUserFrmHeader && !smUserFrmHeader.isEmpty() && smUserFrmHeader.equalsIgnoreCase(this.userName)){
        if (null != smUserFrmHeader && !smUserFrmHeader.isEmpty()){
            if (null != loginStatus && !loginStatus.isEmpty() && "success".equalsIgnoreCase(loginStatus)){
                this.returnVal = "studyList";
                prepareUserStudyMap(smUserFrmHeader);
                if (null == this.studyList || studyList.size() == 0){
                   // this.returnVal = "ssoLogin";
                    FacesMessage msg =
                            new FacesMessage(FacesMessage.SEVERITY_ERROR, "No Study associated with the logged in user", "Please try with valid User.");
                    FacesContext.getCurrentInstance().addMessage(null, msg);
                } 
            } else {
                FacesMessage msg =
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid User name / password", "Please enter valid User name / password.");
                FacesContext.getCurrentInstance().addMessage(null, msg);
            }
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
         System.out.println("Selected Study DB name..." + selectedDBName);
         //return "rdcPage";
         sendRedirectToRDC(this.selectedDBName, request);
         
     }
    public void setReturnVal(String returnVal) {
        this.returnVal = returnVal;
    }

    public String getReturnVal() {
        return returnVal;
    }

    public String getRDCUrlFromStudy(String dbName, HttpServletRequest request){
        System.out.println("Selected Study DB name::" + dbName);
        String rdcURL = null;
        if (null != dbName && !dbName.isEmpty()){
        // get the RDC Login URL with params from ssourl.properties file based on db value
            String hostName = request.getServerName();
            String scheme = request.getScheme();
            StringBuilder serverUrl = new StringBuilder();
            serverUrl.append(scheme).append("://").append(hostName);
            StringBuilder qryString = new StringBuilder();
            String dsName = "jdbc\\rdc"+dbName+"DS";
            qryString.append("db="+dsName+"&setUpDone=Y&mode=P");
            serverUrl.append("/rdcadfsrnd/faces/Login?").append(qryString.toString());
            //rdcURL = SSOUtils.getPropertyValue(dbName);
            rdcURL = serverUrl.toString();
            System.out.println("RedirectUrl from properties file ::" + rdcURL);
        }
        return rdcURL;
    }
    public void prepareUserStudyMap(String loginId){
        String smUserFromSession = null;
        Connection conn = null;
        FacesContext ctx = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest)ctx.getExternalContext().getRequest();        
        HttpSession session= (HttpSession)ctx.getExternalContext().getSession(false);
        System.out.println("Login User ID..." + loginId);
        studyUrlMap = new HashMap<String, String>();
        studyList = new ArrayList<String>();
        this.selectedDBName = null;
        //get db connection from DBUtil
        conn = SSOUtils.getConnection();
        if (null != conn){
            StringBuilder sb = new StringBuilder();
            sb.append("select user_id, fq_db_name,study_assigned,level_type from SSO_RDC_USER_MASTER_LIST ");  
            sb.append("where user_id = ? ");
            sb.append("and level_type = ? "); 
            sb.append("and rdc_mode = ? ");
            PreparedStatement stmt = null;
            ResultSet rs = null;
            System.out.println("***** : " + sb.toString());
            try {
                stmt = conn.prepareStatement(sb.toString());
                stmt.setString(1, loginId);
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
                if (null != this.studyList && studyList.size() == 1){
                    // if only one study then assign value to selectedStudyUrl
                    this.selectedDBName = studyUrlMap.get(studyList.get(0));
                }
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
                        System.out.println("error while callable statment..." + sqe);     
                    }
                }
            }
        }
        if (null != selectedDBName){
            // if only one study , need to send post request to RDC URL based on selected study
           sendRedirectToRDC(selectedDBName, request);
        }
       
    }
    
    private void sendRedirectToRDC(String dbName, HttpServletRequest request) {
       FacesContext ctx = FacesContext.getCurrentInstance();
                  
       String rdcUrl = null;
       if (null != dbName) {
       // get the RDC Login URL with params from ssourl.properties file based on db value
           rdcUrl = getRDCUrlFromStudy(dbName, request);
           System.out.println("RedirectUrl ::" + rdcUrl);
           request.setAttribute("user", this.userName);
           request.setAttribute("password", this.password);
           request.setAttribute("rdcUrl", rdcUrl);              
           
           // Call RDC silent Login JS function here
           System.out.println("calling js - "+"renderRDCApplication('"+this.userName+"','"+this.password+"','"+rdcUrl+"')");
           ADFUtils.addJavaScript("renderRDCApplication('"+this.userName+"','"+this.password+"','"+rdcUrl+"')");
           
       } else {
           FacesMessage msg =
               new FacesMessage(FacesMessage.SEVERITY_ERROR, "Selected Study URL not found", "Please consult logs for detail");
           FacesContext.getCurrentInstance().addMessage(null, msg);
       }
    }

    public void setSelectedDBName(String selectedDBName) {
        this.selectedDBName = selectedDBName;
    }

    public String getSelectedDBName() {
        return selectedDBName;
    }
}
