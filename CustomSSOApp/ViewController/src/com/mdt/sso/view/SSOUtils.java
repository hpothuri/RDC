package com.mdt.sso.view;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import java.sql.Connection;

import java.sql.SQLException;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.naming.InitialContext;

import javax.naming.NamingException;

import javax.sql.DataSource;

import oracle.adf.share.ADFContext;
import oracle.adf.share.logging.ADFLogger;

public class SSOUtils {
    public SSOUtils() {
        super();
    }
    private static Connection dbConnection;
    // TODO get the databse url string from properties file
    private final static String dbUrlString = "jdbc/MDTDS";
    public static final String SERVER_PROPERTY_FILE_LOCATION =
        "E:\\ECRS\\Custom Login\\RDC.git\\trunk\\CustomSSOApp\\ViewController";
    public static final String PROPERTY_FILE_NAME = "ssourl.properties";
    public static final String FILE_SEPERATOR = System.getProperty("file.separator");
    public static final String SERVER_PROPERTY_FILE =
        SERVER_PROPERTY_FILE_LOCATION + FILE_SEPERATOR + PROPERTY_FILE_NAME;
    private static ADFLogger _logger = ADFLogger.createADFLogger(SSOUtils.class);

    public static String getEnvConstant(String key) {
        _logger.info("Start of AppConstants getEnvConstant");
        String val = getConstantFromProperties(SERVER_PROPERTY_FILE, key, "CUSTOMSSO_CONSTANTS");
        _logger.info("End of AppConstants getEnvConstant");
        return val;
    }

    public static String getConstantFromProperties(String propFileName, String key, String sessionMap) {
        _logger.info("AppConstants getConstantFromProperties Key :" + key);
        ADFContext adfCtx = ADFContext.getCurrent();
        Map sessionScope = adfCtx.getSessionScope();

        //CHECK IF CONSTANT MAP EXIST IN SESSION SCOPE, IF NOT THEN POPULATE IT
        if (sessionScope.get(sessionMap) == null) {

            Properties propertiesBundle = new Properties();
            try {
                String propertyFilePath = propFileName;             
                _logger.info("Property File location : " + propFileName);
                if (propertyFilePath != null) {
                    FileInputStream in = new FileInputStream(propertyFilePath);

                    propertiesBundle.load(in);

                    Enumeration em = propertiesBundle.keys();
                    Map propertyMap = new HashMap();
                    _logger.info("Loading properties into session scope map");
                    while (em.hasMoreElements()) {
                        String keyStr = em.nextElement().toString();
                        propertyMap.put(keyStr, propertiesBundle.get(keyStr));
                        _logger.info(keyStr + " : " + propertiesBundle.get(keyStr));
                    }
                    sessionScope.put(sessionMap, propertyMap);
                }

            } catch (IOException e) {
                _logger.severe("Error getting Property file :" + e.getMessage());
                e.printStackTrace();
            }
        }
        
        //READ KEY-VALUE PAIR FROM CONSTANT MAP

        Map value = (Map)sessionScope.get(sessionMap);
        if (value != null && value.containsKey(key)) {
            _logger.info("AppConstants getConstantFromProperties : Value for contains Key " + key + " is " +
                         value.get(key));
            _logger.info("End of AppConstants getConstantFromProperties");
            return (String)value.get(key);
        } else {
            _logger.info("AppConstants getConstantFromProperties : Value not contains Key " + key);
            _logger.info("End of AppConstants getConstantFromProperties");
            return null;
        }
    }

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
