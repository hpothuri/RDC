package com.mdt.sso.view;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.sql.SQLException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.Properties;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import javax.faces.event.ActionEvent;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


public class UserStudyDetailsBean implements Serializable{
    @SuppressWarnings("compatibility:-1330330870396582421")
    private static final long serialVersionUID = 6525028192348714870L;
    private String userName;
    private Map <String , String> studyUrlMap;
    private String selectedStudyName;
    private String selectedStudyUrl;
    private List <String> studyList;
    private String password;
    private String returnVal;
    private static final Properties propertiesFromFile = new Properties();
    private String fccUrl;
    private String fccTargetUrl;
    private String userRole;
    public UserStudyDetailsBean() {
        super();
        loadPropsFromFile();
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

    public void setSelectedStudyUrl(String selectedStudyUrl) {
        this.selectedStudyUrl = selectedStudyUrl;
    }

    public String getSelectedStudyUrl() {
        return selectedStudyUrl;
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
    private boolean loadPropsFromFile() {
        try {
            InputStream st = Thread.currentThread().getContextClassLoader().getResourceAsStream("ssourl.properties");
            propertiesFromFile.load(st);
        }
        catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
    private static String getProperty (String property) {
        String retVal = null;
//        if (properties == null) return "";
//        Object o = properties.get(property);
        Object obj = null;
        if (propertiesFromFile != null){
            obj = propertiesFromFile.get(property);
        }
        if (null != obj){
            retVal = (String) obj;
        }
        return retVal;
    }
    public String initSSOLogin() {
        
        String smUserFrmHeader = null;
        this.returnVal = "studyList";
        FacesContext ctx = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest)ctx.getExternalContext().getRequest();
        smUserFrmHeader = request.getHeader("sm_user");
        String loginStatus = request.getParameter("status");
        String rolesFromFcc = request.getParameter("acrole");
        String hostFromFcc = request.getHeader("host");
        if (null != smUserFrmHeader && !smUserFrmHeader.isEmpty() && smUserFrmHeader.equalsIgnoreCase(this.userName)){
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
         HttpServletResponse response = (HttpServletResponse)ctx.getExternalContext().getResponse();
         System.out.println("Selected Study URL..." + selectedStudyUrl);
         //return "rdcPage";
         sendRedirectToRDC(request, response, this.selectedStudyUrl);
         
     }
    public void setReturnVal(String returnVal) {
        this.returnVal = returnVal;
    }

    public String getReturnVal() {
        return returnVal;
    }

    public void setFccUrl(String fccUrl) {
        this.fccUrl = fccUrl;
    }

    public String getFccUrl() {
        return propertiesFromFile.getProperty("fcc_url");

    }

    public void setFccTargetUrl(String fccTargetUrl) {
        this.fccTargetUrl = fccTargetUrl;
    }

    public String getFccTargetUrl() {
        return propertiesFromFile.getProperty("target_url");
    }
    public String getRDCUrlFromStudy(String studyUrl){
        System.out.println("Selected Study URL..." + selectedStudyUrl);
        String dbName = null;
        int index = 0;
        if (null != selectedStudyUrl){
            // study URL will be like this from data base look up query : mmopat.devl.corp.medtronic.com
            index = selectedStudyUrl.indexOf(".");
            System.out.println("index..." + index);
            // get the db name from Study URL
            if (index > 0){
                dbName = selectedStudyUrl.substring(0, index);
            } else {
                dbName = selectedStudyUrl.substring(0);  
            }
        } else {
            dbName = "";
        }
        System.out.println("Selected Study DB name::" + dbName);
        String rdcURL = null;
        if (null != dbName && !dbName.isEmpty()){
        // get the RDC Login URL with params from ssourl.properties file based on db value
            rdcURL = getProperty(dbName);
            System.out.println("RedirectUrl from properties file ::" + rdcURL);
        }
        return rdcURL;
    }
    public void prepareUserStudyMap(String loginId){
        String smUserFromSession = null;
        Connection conn = null;
        FacesContext ctx = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest)ctx.getExternalContext().getRequest();
        HttpServletResponse response = (HttpServletResponse)ctx.getExternalContext().getResponse();
        
        HttpSession session= (HttpSession)ctx.getExternalContext().getSession(false);
        if (null == loginId){
            smUserFromSession = (String)session.getAttribute("theUserName");
            this.userName = smUserFromSession;
        } else {
            this.userName = loginId;
        }
        System.out.println("Login User ID..." + this.userName);
        studyUrlMap = new HashMap<String, String>();
        studyList = new ArrayList<String>();
        this.selectedStudyUrl = null;
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
                stmt.setString(1, this.userName);
                stmt.setString(2, "STUDY");
                stmt.setString(3, "PROD");
                rs = stmt.executeQuery();
               // prepare studylist map and list
                while (rs.next()) {
                    studyUrlMap.put(rs.getString("study_assigned"), rs.getString("fq_db_name"));
                    studyList.add(rs.getString("study_assigned"));                           
                }
                System.out.println("User Study List..." + studyList);
                if (null != this.studyList && studyList.size() == 1){
                    // if only one study then assign value to selectedStudyUrl
                    this.selectedStudyUrl = studyUrlMap.get(studyList.get(0));
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
        if (null != selectedStudyUrl){
            // if only one study , need to send post request to RDC URL based on selected study
           sendRedirectToRDC(request, response, selectedStudyUrl);
          // this.returnVal = "rdcLogin";
        }
       
    }
    private void sendRedirectToRDC(HttpServletRequest request, HttpServletResponse response, String studyUrl) {
       FacesContext ctx = FacesContext.getCurrentInstance();
                  
       String rdcUrl = null;
       if (null != studyUrl){
       // get the RDC Login URL with params from ssourl.properties file based on db value
           rdcUrl = getRDCUrlFromStudy(studyUrl);
           System.out.println("RedirectUrl from properties file ::" + rdcUrl);
           request.setAttribute("user", this.userName);
           request.setAttribute("password", this.password);
           request.setAttribute("rdcUrl", rdcUrl);
           //request.setAttribute("db", dbName);
          // String qryString = "&user="+this.userId+"&password="+this.password;
          // redirectUrl = redirectUrl+qryString;
           System.out.println("RedirectUrl from properties file ::" + rdcUrl);
//           try {
//               response.sendRedirect(rdcUrl);
//           } catch (IOException ie) {
//               reportUnexpectedLoginError("IOException", ie);
//           }
//           ctx.responseComplete();
       } else {
           FacesMessage msg =
               new FacesMessage(FacesMessage.SEVERITY_ERROR, "Selected Study URL not found", "Please consult logs for detail");
           FacesContext.getCurrentInstance().addMessage(null, msg);
       }
    }
}
