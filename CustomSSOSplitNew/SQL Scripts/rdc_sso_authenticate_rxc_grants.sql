-- *****************************************************************************
-- ***                                                                       ***
-- *** File Name:    rdc_sso_authenticate_rxc_grants.sql                     ***
-- ***                                                                       ***
-- *** Date Written: 18 March 2018                                           ***
-- ***                                                                       ***
-- *** Written By:   DBMS Consulting Inc.                                    ***
-- ***                                                                       ***
-- *** Invoke As:    Oracle RXC Administrative Schema (RXC)                  ***
-- ***                                                                       ***
-- *** Inputs:       RDC SSO Application Schema (i.e. MDT_SSO)               ***
-- ***                                                                       ***
-- *** Outputs:      rdc_sso_authenticate_rxc_grants_<MMDDYY_HHMI>.log       ***
-- ***                                                                       ***
-- *** Description:  The purpose of this script is Grant the Required RXC    ***
-- ***               Privileges Required for Use by the RDC_SSO_AUTHENTICATE ***
-- ***               Package or other Application Specific Privileges Needed ***        
-- ***                                                                       ***
-- *** Modification History:                                                 ***
-- *** --------------------                                                  ***
-- *** 18-MAR-2016 / DBMS Consulting - Initial Creation                      ***
-- ***                                                                       ***
-- *****************************************************************************
   PROMPT 
   PROMPT ********************************************************************
   PROMPT *** Granting RXC Privileges to the RDC Custom Application Owner  ***
   PROMPT ********************************************************************
   ACCEPT rdc_owner PROMPT 'Enter Oracle RDC-SSO Application Owner: '

   SET ECHO OFF
   SET VERIFY OFF
   SET TERMOUT OFF
   SET FEEDBACK OFF
   SET PAGESIZE 0
   WHENEVER SQLERROR CONTINUE
   COLUMN date_column NEW_VALUE curr_date NOPRINT
   SELECT TO_CHAR(SYSDATE,'MMDDYY_HHMI') date_column FROM DUAL;

   SET TERMOUT ON
   SPOOL rdc_sso_authenticate_rxc_grants_&curr_date..log

   SELECT 'Process:   '||'rdc_sso_authenticate_rxc_grants.sql'  ||CHR(10)||
          'User:      '||USER                                   ||CHR(10)||
          'eCRS User: '||'&&rdc_owner'                          ||CHR(10)||
          'Date:      '||TO_CHAR(SYSDATE,'DD-MON-YYYY HH:MI AM')||CHR(10)||
          'Instance:  '||global_name
   FROM global_name;

   WHENEVER SQLERROR EXIT FAILURE
   SET ECHO OFF
   SET VERIFY ON
   SET TERMOUT ON
   SET FEEDBACK ON
   SET PAGESIZE 999
   SET SERVEROUTPUT ON SIZE 1000000

   PROMPT **************************************************************
   PROMPT ***                                                        ***
   PROMPT *** Granting RXC Privileges...                             ***
   PROMPT ***                                                        ***
   PROMPT **************************************************************
   GRANT SELECT, UPDATE ON clinical_studies TO &&rdc_owner;

   SET ECHO OFF
   SET FEEDBACK OFF
   SET TERMOUT ON
   SET PAGESIZE 0
   SELECT 'Processing Complete:  '||TO_CHAR(SYSDATE,'DD-MON-YYYY HH:MI AM')
   FROM DUAL;

   SPOOL OFF
   EXIT

