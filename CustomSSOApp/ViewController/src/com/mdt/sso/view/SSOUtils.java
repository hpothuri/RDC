package com.mdt.sso.view;

import java.sql.Connection;

import java.sql.SQLException;

import javax.naming.InitialContext;

import javax.naming.NamingException;

import javax.sql.DataSource;

public class SSOUtils {
    public SSOUtils() {
        super();
    }
    private static Connection dbConnection;
        // TODO get the databse url string from properties file
        private final static String dbUrlString = "jdbc/MDTDS";
        public static Connection getConnection(){
            if (null != dbConnection){
                return dbConnection;
            }else {
                try {
                    InitialContext initialContext = new InitialContext();
                    DataSource ds = (DataSource)initialContext.lookup(dbUrlString);
                    dbConnection = ds.getConnection();
                } catch (NamingException e) {
                            e.printStackTrace();
                } catch (SQLException e) {
                            e.printStackTrace();
                }
            }
            return dbConnection;
        }
}
