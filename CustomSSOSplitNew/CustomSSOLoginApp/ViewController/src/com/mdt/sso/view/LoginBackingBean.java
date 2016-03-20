package com.mdt.sso.view;

import java.io.Serializable;

import javax.faces.event.ActionEvent;

public class LoginBackingBean implements Serializable {

    @SuppressWarnings("compatibility:-4043275205346999528")
    private static final long serialVersionUID = 3226620042213121167L;

    public LoginBackingBean() {
        super();
    }

    public void onLoginAction(ActionEvent actionEvent) {
        // Add event code here...
        String userName = (String) ADFUtils.evaluateEL("#{loginBean.username}");
        String password = (String) ADFUtils.evaluateEL("#{loginBean.password}");
        UserPasswordServiceUtil.saveUserPassword(userName, PasswordEncoderUtil.encodeStr(password));
    }
}
