-- *****************************************************************************
-- ***                                                                       ***
-- *** File Name:     rdc_sso_authenticate_pkg_body.sql                      ***
-- ***                                                                       ***
-- *** Date Written:  18 March 2016                                          ***
-- ***                                                                       ***
-- *** Written By:    DBMS Consulting Inc.                                   ***
-- ***                                                                       ***
-- *** Package Name:  RDC_SSO_AUTHENTICATE (Package Body)                    ***
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
   SPOOL rdc_sso_authenticate_pkg_body_&curr_date..log

   SELECT 'Process:      '||'rdc_sso_authenticate_pkg_body.sql'    ||CHR(10)||
          'Package Body: '||'RDC_SSO_AUTHENTICATE'                 ||CHR(10)||
          'User:         '||USER                                   ||CHR(10)||
          'Date:         '||TO_CHAR(SYSDATE,'DD-MON-YYYY HH:MI AM')||CHR(10)||
          'Instance:     '||global_name
   FROM global_name;
   SET FEEDBACK ON

   PROMPT 
   PROMPT ********************************************************************
   PROMPT ***       Creating RDC_SSO_AUTHENTICATE Package Body             ***
   PROMPT ********************************************************************

-- ****************************************************************************
-- ***                RDC_SSO_AUTHENTICATE Package Body                     ***
-- ****************************************************************************
   CREATE OR REPLACE PACKAGE BODY rdc_sso_authenticate AS


--    **************************************************************************
--    ***                                                                    ***
--    *** Function:     GET_USER_PWD                                         ***
--    ***                                                                    ***
--    *** Input:        pUserName                                            ***
--    ***                                                                    ***
--    *** Output:       pPassword  {NULL Return when pUserName Not Found}    ***
--    ***                                                                    ***
--    *** Description:  This Function will Accept a UserName and will Return ***
--    ***               the Corresponding Password from the Table            ***
--    ***               RDC_USER_PASSWORD_MAP_DTL.  If the UserName is Not   ***
--    ***               Found a NULL value will be Returned to the Caller.   ***
--    ***                                                                    *** 
--    **************************************************************************
      FUNCTION get_user_pwd (pUserName IN VARCHAR2)
      RETURN VARCHAR2
      IS
          pwd   VARCHAR2(60)   := NULL;
          errm  VARCHAR2(4000) := NULL;
      BEGIN
         SELECT password INTO pwd 
         FROM rdc_user_password_map_dtl
         WHERE UPPER(user_name) = UPPER(TRIM(pUserName));
         RETURN pwd;

      EXCEPTION 
         WHEN NO_DATA_FOUND THEN
            RETURN NULL;
         WHEN OTHERS THEN
         errm := '%%% Unhandled Error Selecting Password '||
            'for pUserName '||pUserName||' from '||
            'RDC_USER_PASSWORD_MAP_DTL '||SQLERRM;
         RAISE_APPLICATION_ERROR(-20101,errm);

      END get_user_pwd;


--    **************************************************************************
--    ***                                                                    ***
--    *** Procedure:    STORE_USER_PWD                                       ***
--    ***                                                                    ***
--    *** Inputs:       pUserName                                            ***
--    ***               pPassword                                            ***
--    ***                                                                    ***
--    *** Description:  This Procedure Allows for the UPDATE of a User's     ***
--    ***               Password in the Table RDC_USER_PASSWORD_MAP_DTL      ***
--    ***               Based on the Provided pUserName.                     ***
--    ***               If the pUserName is NOT Found, a New User Record     ***
--    ***               be Created in the Table RDC_USER_PASSWORD_MAP_DTL.   ***
--    ***                                                                    *** 
--    **************************************************************************
      PROCEDURE store_user_pwd (pUserName  IN VARCHAR2,
                                pPassword  IN VARCHAR2)
      IS
          nrec  INTEGER        := 0;
          errm  VARCHAR2(4000) := NULL;
      BEGIN
--       **************************************
--       *** Does the Specified User Exist? ***
--       **************************************
         BEGIN
            SELECT COUNT(*) INTO nrec
            FROM rdc_user_password_map_dtl
            WHERE UPPER(user_name) = UPPER(TRIM(pUserName));
         EXCEPTION WHEN OTHERS THEN
            errm := '%%% Unhandled Error Selecting pUserName '||
               pUserName||' from RDC_USER_PASSWORD_MAP_DTL '||SQLERRM;
            RAISE_APPLICATION_ERROR(-20101,errm);
         END;

--       *******************************
--       *** Update Users Password   ***
--       *******************************
         IF (nrec > 0) THEN
             BEGIN
                UPDATE rdc_user_password_map_dtl
                SET password = TRIM(pPassword)
                WHERE UPPER(user_name) = UPPER(TRIM(pUserName));
             EXCEPTION WHEN OTHERS THEN
                errm := '%%% Unhandled Error Updating Password in '||
                  'RDC_USER_PASSWORD_MAP_DTL for pUserName '||pUserName||
                  ' '||SQLERRM;
                RAISE_APPLICATION_ERROR(-20101,errm);
             END;

--       ***************************************************
--       *** User was not Found - Create New User Record ***
--       ***************************************************
         ELSIF (nrec = 0) THEN
             BEGIN
                INSERT INTO rdc_user_password_map_dtl 
                   (user_name, password)
                VALUES 
                   (UPPER(TRIM(pUserName)), TRIM(pPassword));
             EXCEPTION WHEN OTHERS THEN
                errm := '%%% Unhandled Error Creating New User in '||
                  'RDC_USER_PASSWORD_MAP_DTL for pUserName '||pUserName||
                  ' '||SQLERRM;
                RAISE_APPLICATION_ERROR(-20101,errm);
             END;
         END IF;   
         COMMIT;

      EXCEPTION WHEN OTHERS THEN
          ROLLBACK;
          errm := '%%% Unhandled Error in Procedure STORE_USER_PWD '||
             SQLERRM;
          RAISE_APPLICATION_ERROR(-20101,errm);
     
      END store_user_pwd;


--    **************************************************************************
--    ***                                                                    ***
--    *** Procedure:    RESET_LAST_ACCESSED_STUDY                            ***
--    ***                                                                    ***
--    *** Inputs:       pStudyName                                           ***
--    ***               pUserName                                            ***
--    ***                                                                    ***
--    *** Description:  This Procedure Allows the Caller to UPDATE the       ***
--    ***               LAST_CLINICAL_STUDY_ID in the ORACLE_ACCOUNTS Table  ***
--    ***               for a Specific ORACLE_ACCOUNT_NAME (pUserName)       ***
--    ***                                                                    ***
--    ***               1.) To Clear the LAST_CLINICAL_STUDY_ID and Set the  ***
--    ***                   Value to NULL for the Specified pUserName, the   ***
--    ***                   pStudyName Parameter Should be Omitted Passed as ***
--    ***                   a NULL Value                                     ***
--    ***                                                                    ***
--    ***               2.) To Set the LAST_CLINICAL_STUDY_ID to a Specific  ***
--    ***                   CLINICAL_STUDY_ID for the Specified pUserName,   ***
--    ***                   the pStudyName of the Desired Study Should be    ***
--    ***                      Specified                                     ***
--    ***                                                                    ***
--    *** Privileges:   SELECT on RXA_DES.CLINICAL_STUDIES                   ***
--    ***               SELECT, UPDATE on RXC.ORACLE_ACCOUNTS                ***
--    ***                                                                    *** 
--    **************************************************************************
      PROCEDURE reset_last_accessed_study (pUserName  IN VARCHAR2,
                                           pStudyName IN VARCHAR2 DEFAULT NULL)
      IS  
         curr_study_id  NUMBER(10)     := NULL;
         new_study_id   NUMBER(10)     := NULL;
         errm           VARCHAR2(4000) := NULL;
      BEGIN
--       ***************************************************************
--       *** 1.) Verify the pUserName Exists in ORACLE ACCOUNTS      ***
--       *** 2.) Obtain the Current LAST_CLINICAL_STUDY_ID           ***
--       ***************************************************************
         BEGIN
            SELECT last_clinical_study_id INTO curr_study_id
            FROM rxc.oracle_accounts
            WHERE oracle_account_name = TRIM(UPPER(pUserName));
         EXCEPTION 
            WHEN NO_DATA_FOUND THEN
               errm := '%%% The ORACLE_ACCOUNT_NAME '||pUserName||' was Not '||
                  'Found in ORACLE_ACCOUNTS';
               RAISE_APPLICATION_ERROR(-20101,errm);
            WHEN OTHERS THEN
               errm := '%%% Unhandled Error Selecting ORACLE_ACCOUNTS Record '||
                 'for ORACLE_ACCOUNT_NAME '||pUserName||' '||SQLERRM;
               RAISE_APPLICATION_ERROR(-20101,errm);
         END;

--       *****************************************************************
--       *** 1.) If pStudyName IS NULL then Set New StudyID to NULL    ***
--       *** 2.) Obtain the CLINICAL_STUDY_ID Associated to pStudyName ***
--       *****************************************************************
         IF (pStudyName IS NULL) THEN
             new_study_id := NULL;
         ELSE
             BEGIN
                 SELECT clinical_study_id INTO new_study_id
                 FROM rxa_des.clinical_studies 
                 WHERE study = TRIM(UPPER(pStudyName));
             EXCEPTION      
                 WHEN NO_DATA_FOUND THEN
                     errm := '%%% Study '||pStudyName||' was Not Found '||
                        'in CLINICAL_STUDIES';
                     RAISE_APPLICATION_ERROR(-20101, errm);
                 WHEN OTHERS THEN
                     errm := '%%% Unhandled Error Selecting from '||
                       'CLINICAL_STUDIES for pStudyName '||pStudyName||' '||
                        SQLERRM;
                     RAISE_APPLICATION_ERROR(-20101, errm);
              END;
         END IF;

--       *********************************************************************
--       *** Update ORACLE_ACCOUNTS.LAST_CLINICAL_STUDY_ID (if applicable) ***
--       *********************************************************************
         IF (NVL(new_study_id,0) <> NVL(curr_study_id,0)) THEN
             BEGIN
                UPDATE rxc.oracle_accounts
                SET last_clinical_study_id = new_study_id
                WHERE oracle_account_name = TRIM(UPPER(pUserName));
                COMMIT;
             EXCEPTION WHEN OTHERS THEN
                errm := '%%% Unhandled Error Updating ORACLE_ACCOUNTS '||
                   'LAST_CLINICAL_STUDY_ID for '||pUserName||' '||SQLERRM;
                RAISE_APPLICATION_ERROR(-20101,errm);
             END;
         END IF;
 
      EXCEPTION WHEN OTHERS THEN
         errm := '%%% Unhandled Error in Procedure RESET_LAST_ACCESSED_STUDY '||
            SQLERRM;
         RAISE_APPLICATION_ERROR(-20101,errm);

      END reset_last_accessed_study;



   END rdc_sso_authenticate;
/
   SHOW ERRORS
   SPOOL OFF
   EXIT