package com.mdt.sso.view;

public class LoginBean {
    public LoginBean() {
        super();
    }
    
    private String username;
    private String password;
    private String siteminderURL;
    private String studyListURL;


    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public void setSiteminderURL(String siteminderURL) {
        this.siteminderURL = siteminderURL;
    }

    public String getSiteminderURL() {
        if(siteminderURL==null)
            siteminderURL=SSOUtils.getPropertyValue("fcc_url");
        return siteminderURL;
    }

    public void setStudyListURL(String studyListURL) {
        this.studyListURL = studyListURL;
    }

    public String getStudyListURL() {
        if(siteminderURL==null)
            siteminderURL=SSOUtils.getPropertyValue("target_url");
        return studyListURL;
    }
}
