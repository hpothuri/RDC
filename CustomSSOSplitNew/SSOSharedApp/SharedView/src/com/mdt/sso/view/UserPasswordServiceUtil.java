package com.mdt.sso.view;

public class UserPasswordServiceUtil {
    public UserPasswordServiceUtil() {
        super();
    }

    public static final UserPasswordDAO userPasswordDAO = new UserPasswordDAO();

    public static boolean saveUserPassword(String user, String password) {
        return userPasswordDAO.saveUserPassword(user, password);
    }

    public static String getUserPassword(String user) {
        return userPasswordDAO.getUserPassword(user);
    }
}
