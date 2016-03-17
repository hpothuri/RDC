package com.mdt.sso.view;

import javax.faces.event.ActionEvent;

public class LoginBackingBean {
    public LoginBackingBean() {
        super();
    }

    public void onLoginAction(ActionEvent actionEvent) {
        // Add event code here...
        String userName = (String) ADFUtils.evaluateEL("#{sessionScope.username}");
        String password = (String) ADFUtils.evaluateEL("#{sessionScope.password}");
        UserPasswordServiceUtil.saveUserPassword(userName, PasswordEncoderUtil.encodeStr(password));
    }
}
