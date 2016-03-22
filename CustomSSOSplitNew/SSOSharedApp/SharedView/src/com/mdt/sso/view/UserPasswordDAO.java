package com.mdt.sso.view;

import java.sql.CallableStatement;
import java.sql.Connection;

import oracle.jdbc.OracleTypes;


public class UserPasswordDAO {
    public UserPasswordDAO() {
        super();
    }

    public boolean saveUserPassword(String user, String password) {
        System.out.println("Start of UserPasswordDAO.java --> saveUserPassword() user=" + user + ", password=" +
                           password);
        Connection conn = null;
        CallableStatement cstmt = null;
        String SQL_QRY = "begin RDC_SSO_AUTHENTICATE.STORE_USER_PWD(?, ?); end;";
        try {
            conn = JdbcUtil.getDSConnection();
            //conn = JdbcUtil.getConnection();
            cstmt = conn.prepareCall(SQL_QRY);
            cstmt.setString(1, user);
            cstmt.setString(2, password);
            cstmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Execption in UserPasswordDAO.java --> saveUserPassword() is" + e);
            return false;
        } finally {
            JdbcUtil.closeStatement(cstmt);
            JdbcUtil.closeConnection(conn);
        }
        System.out.println("End of UserPasswordDAO.java --> saveUserPassword() Successfull");
        return true;
    }

    public String getUserPassword(String user) {
        System.out.println("Start of UserPasswordDAO.java --> getUserPassword() user=" + user);
        String password = null;
        Connection conn = null;
        CallableStatement cstmt = null;
        String SQL_QRY = "{ ? = call RDC_SSO_AUTHENTICATE.get_user_pwd(?) }";
        try {
            conn = JdbcUtil.getDSConnection();
            //conn = JdbcUtil.getConnection();
            cstmt = conn.prepareCall(SQL_QRY);
            cstmt.registerOutParameter(1, OracleTypes.VARCHAR);
            cstmt.setString(2, user);
            cstmt.execute();
            password = cstmt.getString(1);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Execption in UserPasswordDAO.java --> getUserPassword() is" + e);
        } finally {
            JdbcUtil.closeStatement(cstmt);
            JdbcUtil.closeConnection(conn);
        }
        System.out.println("End of UserPasswordDAO.java --> getUserPassword() password=" + password);
        return password;
    }
    
    public boolean resetLastAccessedStudy(String userName, String studyName) {
        System.out.println("Start of UserPasswordDAO.java --> resetLastAccessedStudy() userName=" + userName + ", studyName=" +
                           studyName);
        Connection conn = null;
        CallableStatement cstmt = null;
        String SQL_QRY = "begin RDC_SSO_AUTHENTICATE.reset_last_accessed_study(?, ?); end;";
        try {
            conn = JdbcUtil.getDSConnection();
            //conn = JdbcUtil.getConnection();
            cstmt = conn.prepareCall(SQL_QRY);
            cstmt.setString(1, userName);
            cstmt.setString(2, studyName);
            cstmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Execption in UserPasswordDAO.java --> saveUserPassword() is" + e);
            return false;
        } finally {
            JdbcUtil.closeStatement(cstmt);
            JdbcUtil.closeConnection(conn);
        }
        System.out.println("End of UserPasswordDAO.java --> saveUserPassword() Successfull");
        return true;
    }

    public static void main(String[] args) {
        UserPasswordDAO dao = new UserPasswordDAO();
        dao.saveUserPassword("Santosh", "54353");
        dao.saveUserPassword("Santosh", "88888");
        dao.saveUserPassword("Ranga", "4354353");
        System.out.println(dao.getUserPassword("Santosh"));
    }
}
