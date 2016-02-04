package com.mdt.sso.view;

import java.io.IOException;
import java.io.InputStream;

import java.sql.Connection;

import java.sql.SQLException;

import java.util.Properties;

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

    public static Connection getConnection() {
        if (null != dbConnection) {
            return dbConnection;
        } else {
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

    private static Properties propertiesFromFile;

    public static String getPropertyValue(String propName) {
        try {
            if (propertiesFromFile == null) {
                propertiesFromFile = new Properties();
                InputStream st =
                    Thread.currentThread().getContextClassLoader().getResourceAsStream("ssourl.properties");
                propertiesFromFile.load(st);
            }

            if (propertiesFromFile != null) {
                return (String)propertiesFromFile.get(propName);
            } else
                return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public static void main(String[] args) {
       System.out.println(SSOUtils.getPropertyValue("fcc_url"));
    }


}
