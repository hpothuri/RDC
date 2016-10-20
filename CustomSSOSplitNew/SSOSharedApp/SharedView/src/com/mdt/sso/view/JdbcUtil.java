package com.mdt.sso.view;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import javax.sql.DataSource;


public class JdbcUtil {
    public JdbcUtil() {
        super();
    }

    public static final String RDC_JNDI = "jdbc/MDTDS";

    public static Connection getMdtDsConnection() throws NamingException, SQLException {
        return getConnection(RDC_JNDI);
    }

    public static Connection getConnection() throws Exception {
        Class.forName("oracle.jdbc.driver.OracleDriver");
        Connection conn = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:xe", "hr", "hr");
        return conn;
    }

    public static Connection getConnection(String jndiName) throws NamingException, SQLException {
        Context ctx = new InitialContext();
        DataSource ds = (DataSource)ctx.lookup(jndiName);
        return ds.getConnection();
    }

    public static void closeConnection(Connection con) {
        try {
            if (con != null)
                con.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Execption in JdbcUtil closeConnection() is" + e);
        }
    }

    public static void closeStatement(Statement st) {
        try {
            if (st != null )
                st.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Execption in JdbcUtil closeStatement() is" + e);
        }
    }

    public static void closeResultSet(ResultSet rs) {
        try {
            if (rs != null)
                rs.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Execption in JdbcUtil closeResultSet() is" + e);
        }
    }
}
