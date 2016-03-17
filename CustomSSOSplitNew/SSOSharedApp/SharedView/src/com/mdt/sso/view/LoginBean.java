package com.mdt.sso.view;

import java.io.Serializable;

public class LoginBean implements Serializable {
    @SuppressWarnings("compatibility:-9123740863273999735")
    private static final long serialVersionUID = 1L;

    public LoginBean() {
        super();
    }

    private String username;
    private String password;
    private String siteminderURL;
    private String studyListURL;
    private String clinicalTrainngURL;
    private String forgetPasswordURL;

    public void setUsername(String username) {
        this.username = username;
        ADFUtils.setSessionScopeValue("username", username);
    }

    public String getUsername() {
        return username;
    }

    public void setPassword(String password) {
        this.password = password;
        ADFUtils.setSessionScopeValue("password", password);
    }

    public String getPassword() {
        return password;
    }

    public void setSiteminderURL(String siteminderURL) {
        this.siteminderURL = siteminderURL;
    }

    public String getSiteminderURL() {
        if (siteminderURL == null)
            siteminderURL = SSOUtils.getPropertyValue("fcc_url");
        return siteminderURL;
    }

    public void setStudyListURL(String studyListURL) {
        this.studyListURL = studyListURL;
    }

    public String getStudyListURL() {
        if (studyListURL == null)
            studyListURL = SSOUtils.getPropertyValue("target_url");
        return studyListURL;
    }

    public void clear() {
        System.out.println("Clear login details...");
        this.username = null;
        this.password = null;
    }

    public void setClinicalTrainngURL(String clinicalTrainngURL) {
        this.clinicalTrainngURL = clinicalTrainngURL;
    }

    public String getClinicalTrainngURL() {
        if (clinicalTrainngURL == null)
            clinicalTrainngURL = SSOUtils.getPropertyValue("clinical_training_url");
        return clinicalTrainngURL;
    }

    public void setForgetPasswordURL(String forgetPasswordURL) {
        this.forgetPasswordURL = forgetPasswordURL;
    }

    public String getForgetPasswordURL() {
        if (forgetPasswordURL == null)
            forgetPasswordURL = SSOUtils.getPropertyValue("forget_password_url");
        return forgetPasswordURL;
    }
}
