-- **************************************************************************
-- ***                                                                    ***
-- *** File Name:      rdc_sso_authenticate_create_tables.sql             ***
-- ***                                                                    ***
-- *** Written By:     DBMS Consulting Inc.                               ***
-- ***                                                                    ***
-- *** Date Written:   18 March 2016                                      ***
-- ***                                                                    ***
-- *** Description:    The Purpose of the this SQL Script is to Create    ***
-- ***                 All of the Oracle Processing Tables Applicable to  ***
-- ***                 the Remote Data Capture (RDC) Single Sign-on (SSO) ***
-- ***                 Authentication Functionality.                      ***
-- ***                                                                    ***
-- *** Tables Created: RDC_USER_PASSWORD_MAP_DTL                          ***
-- ***                                                                    ***
-- *** Modification History:                                              ***
-- *** --------------------                                               ***
-- *** 18-MAR-2016 - DBMS Consulting - Initial Creation                   ***
-- ***                                                                    ***
-- **************************************************************************
   SET ECHO OFF
   SET VERIFY OFF
   SET TERMOUT OFF
   SET FEEDBACK OFF
   SET PAGESIZE 0
   WHENEVER SQLERROR CONTINUE
   COLUMN date_column NEW_VALUE curr_date NOPRINT
   SELECT TO_CHAR(SYSDATE,'MMDDYY_HHMI') date_column FROM DUAL;

-- *************************************
-- *** Drop Any Pre-Existing Objects ***
-- *************************************
   DROP TABLE rdc_user_password_map_dtl;

   SET TERMOUT ON
   SPOOL rdc_sso_authenticate_create_tables_&curr_date..log

   SELECT 'Process:  '||'rdc_sso_authenticate_create_tables.sql'||CHR(10)||
          'User:     '||USER                                    ||CHR(10)||
          'Date:     '||TO_CHAR(SYSDATE,'DD-MON-YYYY HH:MI AM') ||CHR(10)||
          'Instance: '||global_name
   FROM global_name;

   WHENEVER SQLERROR EXIT FAILURE
   SET ECHO OFF
   SET VERIFY ON
   SET TERMOUT ON
   SET FEEDBACK ON
   SET PAGESIZE 999

   PROMPT ******************************************************************
   PROMPT ***                                                            ***
   PROMPT *** Creating Table RDC_USER_PASSWORD_MAP_DTL                   ***
   PROMPT ***                                                            ***
   PROMPT ******************************************************************
   CREATE TABLE rdc_user_password_map_dtl
      (user_name         VARCHAR2(30) NOT NULL,
       password          VARCHAR2(50) NOT NULL)
   NOLOGGING;

   ALTER TABLE rdc_user_password_map_dtl
     ADD CONSTRAINT rdc_user_pwd_map_dtl_pk
     PRIMARY KEY (user_name);

   EXEC dbms_stats.gather_table_stats             -
         (ownname =>  USER,                       -
          tabname => 'RDC_USER_PASSWORD_MAP_DTL', -
          cascade =>  TRUE,                       -
          degree  =>  4);

   SET TERMOUT ON
   PROMPT 
   PROMPT FYI -  Tables Successfully Created
   PROMPT
   SPOOL OFF
   EXIT
