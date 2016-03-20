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
        String SQL_QRY = "begin RDC_SSO_AUTHENTICATE.GET_STORED_USER_PWD(?, ?); end;";
        try {
            conn = JdbcUtil.getDSConnection();
            //conn = JdbcUtil.getConnection();
            cstmt = conn.prepareCall(SQL_QRY);
            cstmt.setString(1, user);
            cstmt.registerOutParameter(2, OracleTypes.VARCHAR);
            cstmt.executeUpdate();
            password = cstmt.getString(2);
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
    
    public static void main(String[] args) {
        UserPasswordDAO dao = new UserPasswordDAO();
        dao.saveUserPassword("NNN", "54353");
        dao.saveUserPassword("Harish", "88888");
        dao.saveUserPassword("Ranga", "4354353");
        System.out.println(dao.getUserPassword("Ranga"));
    }
}
