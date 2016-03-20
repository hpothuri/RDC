-- *****************************************************************************
-- ***                                                                       ***
-- *** File Name:     rdc_sso_authenticate_pkg_spec.sql                      ***
-- ***                                                                       ***
-- *** Date Written:  18 March 2016                                          ***
-- ***                                                                       ***
-- *** Written By:    DBMS Consulting Inc.                                   ***
-- ***                                                                       ***
-- *** Package Name:  RDC_SSO_AUTHENTICATE (Package Specification)           ***
-- ***                                                                       ***
-- *** Description:   This Package Contains the Procedures and Functions     ***
-- ***                Applicable to Remote Data Capture (RDC) Single Sign    ***
-- ***                On (SSO) Authentication.                               ***
-- ***                                                                       ***
-- *** Modification History:                                                 ***
-- *** --------------------                                                  ***
-- *** 16-MAR-2016 / DBMS Consulting - Initial Creation                      ***
-- ***                                                                       ***
-- *****************************************************************************
   SET ECHO OFF
   SET FEEDBACK OFF
   COLUMN date_column NEW_VALUE curr_date NOPRINT
   SELECT TO_CHAR(SYSDATE,'MMDDYY_HHMI') date_column FROM DUAL;
   SET PAGESIZE 0
   SET VERIFY OFF
   SET TERMOUT ON
   SET SERVEROUTPUT ON SIZE 1000000
   SET ARRAYSIZE 1
   SET FEEDBACK OFF
   SPOOL rdc_sso_authenticate_pkg_spec_&curr_date..log

   SELECT 'Process:      '||'rdc_sso_authenticate_pkg_spec.sql'    ||CHR(10)||
          'Package Body: '||'RDC_SSO_AUTHENTICATE'                 ||CHR(10)||
          'User:         '||USER                                   ||CHR(10)||
          'Date:         '||TO_CHAR(SYSDATE,'DD-MON-YYYY HH:MI AM')||CHR(10)||
          'Instance:     '||global_name
   FROM global_name;
   SET FEEDBACK ON

   PROMPT 
   PROMPT ********************************************************************
   PROMPT *** Creating RDC_SSO_AUTHENTICATE Package Specification          ***
   PROMPT ********************************************************************

-- ****************************************************************************
-- ***                RDC_SSO_AUTHENTICATE Package Specifiction             ***
-- ****************************************************************************
   CREATE OR REPLACE PACKAGE rdc_sso_authenticate AS

      FUNCTION get_user_pwd
        (pUserName  IN VARCHAR2)
      RETURN VARCHAR2;

      PROCEDURE store_user_pwd
        (pUserName  IN VARCHAR2,
         pPassword  IN VARCHAR2);
  
      PROCEDURE reset_last_accessed_study 
        (pUserName  IN VARCHAR2,
         pStudyName IN VARCHAR2 DEFAULT NULL);

   END rdc_sso_authenticate;
/
   SHOW ERRORS
   SPOOL OFF
   EXIT