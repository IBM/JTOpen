///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: AS400CertificateUsrPrfUtilImplRemote.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.InterruptedIOException;
import java.io.IOException;
import java.beans.PropertyVetoException;
import java.net.UnknownHostException;
import java.io.UnsupportedEncodingException;


/**
   <p>The AS400CertificateUsrPrfUtilImplRemote provides the implementation of the remote methods for accessing certificates in an i5/OS user profile object.
 **/
class AS400CertificateUsrPrfUtilImplRemote  extends AS400CertificateUsrPrfUtilImpl
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

  private ProgramCall pgmCall_;


 //********************************************************************/
 //* methods for remote invocation                                    */
 //*                                                                  */
 //* @return  Return code mapped to CPFxxxx error message.            */
 //********************************************************************/

  int calladdCertificate(byte[] cert, int certlen,
			  String userName)
    throws AS400SecurityException,
           ErrorCompletingRequestException,
           InterruptedException,
           IOException,
           ObjectDoesNotExistException
  {

      int rc;

	 // **** Setup the parameter list ****
      ProgramParameter[] parmlist = new ProgramParameter[6];

	 // First parameter: input, is the pgm entry
      byte[] pgmEntry = new byte[4];
      BinaryConverter.intToByteArray(CALL_USRPRF_ADD, pgmEntry, 0);
      parmlist[0] = new ProgramParameter(pgmEntry);

        // 2 parameter: input, is the certificate array
      parmlist[1] = new ProgramParameter(cert);

	 // 3 parameter: input, is the length of the cert array
      byte[] certlenB = new byte[4];
      BinaryConverter.intToByteArray(certlen, certlenB, 0);
      parmlist[2] = new ProgramParameter(certlenB);

	 // 4 parameter: input, is user name
      byte[] usrprfPathB = new byte[10];
      converter_.stringToByteArray(userName, usrprfPathB);
      parmlist[3] = new ProgramParameter(usrprfPathB);

	 // 5 parameter: output, is the cpf error id array
      byte[] errorInfoB = new byte[ 7 ];
      parmlist[4] = new ProgramParameter( 7 );

      	 // 6 parameter: input/output, is the return code
      byte[] retcodeB = new byte[4];
      BinaryConverter.intToByteArray(-1, retcodeB, 0);
      parmlist[5] = new ProgramParameter(retcodeB, 4 );

      pgmCall_ = new ProgramCall(system_);
      try {
	  pgmCall_.setProgram("/QSYS.LIB/QYJSPCTU.PGM", parmlist );
      }
      // PropertyVetoException should never happen
      catch (PropertyVetoException pve) {}
      pgmCall_.setThreadSafe(true);  //@A1A

      // Run the program.  Failure returns message list
      if(pgmCall_.run() != true)
      {
	  AS400Message[] messagelist = pgmCall_.getMessageList();
	  cpfError_ = messagelist[0].toString();
	  return -1;
      }

      else
      {
	 // get the retcode returned from the program
	  retcodeB = parmlist[5].getOutputData();
	  rc = BinaryConverter.byteArrayToInt(retcodeB, 0);

	  if (0 != rc)
	  {
	      //unexpected error
	      if (-1 == rc) return rc;
	      //get cpf error id
	      errorInfoB = parmlist[4].getOutputData();
	      cpfError_ = converter_.byteArrayToString(errorInfoB, 0);
	      //trim off white space
	      cpfError_.trim();
	      return rc;
	  }
      }

      return SUCCESS;
  }




  int calldeleteCertificate(byte[] cert, int certlen,
			     String userName,
			     int certType)
    throws AS400SecurityException,
           ErrorCompletingRequestException,
           InterruptedException,
           IOException,
           ObjectDoesNotExistException
 {


      int rc;

	 // **** Setup the parameter list ****
      ProgramParameter[] parmlist = new ProgramParameter[7];

	 // First parameter: input, is the pgm entry
      byte[] pgmEntry = new byte[4];
      BinaryConverter.intToByteArray(CALL_USRPRF_DELETE, pgmEntry, 0);
      parmlist[0] = new ProgramParameter(pgmEntry);

        // 2 parameter: input, is the certificate array
      parmlist[1] = new ProgramParameter(cert);

	 // 3 parameter: input, is the length of the cert array
      byte[] certlenB = new byte[4];
      BinaryConverter.intToByteArray(certlen, certlenB, 0);
      parmlist[2] = new ProgramParameter(certlenB);

	 // 4 parameter: input, is user profile
      byte[] usrprfPathB = new byte[10];
      converter_.stringToByteArray(userName, usrprfPathB);
      parmlist[3] = new ProgramParameter(usrprfPathB);

        // 5 parameter: input, is certificate type
      byte[] certTypeB = new byte[4];
      BinaryConverter.intToByteArray(certType, certTypeB, 0);
      parmlist[4] = new ProgramParameter(certTypeB);

	 // 6 parameter: output, is the cpf error id array
      byte[] errorInfoB = new byte[ 7];
      parmlist[5] = new ProgramParameter( 7 );

      	 // 7 parameter: input/output, is the return code
      byte[] retcodeB = new byte[4];
      BinaryConverter.intToByteArray(-1, retcodeB, 0);
      parmlist[6] = new ProgramParameter(retcodeB, 4 );

      pgmCall_ = new ProgramCall(system_);
      try {
	  pgmCall_.setProgram("/QSYS.LIB/QYJSPCTU.PGM", parmlist );
      }
      // PropertyVetoException should never happen
      catch (PropertyVetoException pve) {}
      pgmCall_.setThreadSafe(true);  //@A1A

      // Run the program.  Failure returns message list
      if(pgmCall_.run() != true)
      {
	  AS400Message[] messagelist = pgmCall_.getMessageList();
	  cpfError_ = messagelist[0].toString();
	  return -1;
      }

      else
      {
	 // get the retcode returned from the program
	  retcodeB = parmlist[6].getOutputData();
	  rc = BinaryConverter.byteArrayToInt(retcodeB, 0);

	  if (0 != rc)
	  {
	      //unexpected error
	      if (-1 == rc) return rc;
	      //get cpf error id
	      errorInfoB = parmlist[5].getOutputData();
	      cpfError_ = converter_.byteArrayToString(errorInfoB, 0);
	      //trim off white space
	      cpfError_.trim();
	      return rc;
	  }
      }

      return SUCCESS;
  }



  int calllistCertificates(String userName,
			    String usrSpaceName,
			    boolean[] parmEntered,
			    String[] attrS,
			    byte[] [] attrB)
    throws AS400SecurityException,
           ErrorCompletingRequestException,
           InterruptedException,
           IOException,
           ObjectDoesNotExistException
  {

      int rc;
      int i, j;
      int length;

	 // **** Setup the parameter list ****
      ProgramParameter[] parmlist = new ProgramParameter[9];

	 // First parameter: input, is the pgm entry number
      byte[] pgmEntry = new byte[4];
      BinaryConverter.intToByteArray(CALL_USRPRF_LISTCERT, pgmEntry, 0);
      parmlist[0] = new ProgramParameter(pgmEntry);

      	 // 2 parameter: input, is user profile
      byte[] usrprfPathB = new byte[10];
      converter_.stringToByteArray(userName, usrprfPathB);
      parmlist[1] = new ProgramParameter(usrprfPathB);

    	 // 3 parameter: input, is user space native name
      byte[] usrSpaceNameB = new byte[20];
      converter_.stringToByteArray(usrSpaceName, usrSpaceNameB);
      parmlist[2] = new ProgramParameter(usrSpaceNameB);

	 // 4 parameter: input, is parm entered array
      byte[] parmPresent = new byte[SEARCH_PARMS * 4];
      for (i = 0; i < SEARCH_PARMS; ++i)
      {
	  if (parmEntered[i])
	      BinaryConverter.intToByteArray(1, parmPresent, i * 4);
	  else
	      BinaryConverter.intToByteArray(0, parmPresent, i * 4);
      }
      parmlist[3] = new ProgramParameter(parmPresent);


	 // 5, 6 parameter: input, parm sizes and search attrs array
      length = 0;
      int [] parmsize = new int[SEARCH_PARMS];
      //get length estimate and byte[] parm sizes
      for (i = 0; i < attrS.length; ++i)
      {
	  if (attrS[i] != null)
	      length = length + attrS[i].length();
      }

      for (j = 0; j < attrB.length; ++j)
      {
	  if (attrB[j] != null)
	  {
	      length = length + attrB[j].length;
	      parmsize[i] = attrB[j].length;
	  }
	  ++i;
      }

      //allow extra room for mixed ccsids and convert the strings,
      //cannot assume target ccsid is 1 byte per char
      byte[] attrbytes = new byte[length * 2];
      byte[][] attrSbytes = new byte[attrS.length] [];
      for (i = 0; i < attrS.length; ++i)
      {
	  if (attrS[i] != null)
	  {
	      if (0 != attrS[i].length())
	      {
		  attrSbytes[i] = converter_.stringToByteArray(attrS[i]);
		  parmsize[i] = attrSbytes[i].length;
	      }
	  }
      }

      //put/pack the converted string and byte arrays together
      length = 0;
      byte[] parmsizeBytes = new byte[SEARCH_PARMS  * 4];
      //pack string search data first
      for (i = 0; i < attrS.length; ++i)
      {
	  if (null != attrSbytes[i])
	  {
	      System.arraycopy(attrSbytes[i], 0,
			       attrbytes, length,
			       attrSbytes[i].length);

	      length = length + attrSbytes[i].length;
	  }
	  BinaryConverter.intToByteArray(parmsize[i],
					parmsizeBytes,
					i * 4);
      }
      //pack byte[] search data last
      for (i = 0; i < attrB.length; ++i)
      {
	  if (null != attrB[i])
	  {
	      System.arraycopy(attrB[i], 0,
			       attrbytes, length,
			       attrB[i].length);

	      length = length + attrB[i].length;
	  }
	  BinaryConverter.intToByteArray(parmsize[SEARCH_PARMS - 1 + i],
					parmsizeBytes,
					(SEARCH_PARMS - 1 + i) * 4);
	}

      parmlist[4] = new ProgramParameter(parmsizeBytes);
      parmlist[5] = new ProgramParameter(attrbytes);

	 // 7 parameter: output, is the number certs found
      byte[] numberCertificatesFoundB = new byte[4];
      parmlist[6] = new ProgramParameter(4);


 	 // 8 parameter: output, is the cpf error id array
      byte[] errorInfoB = new byte[ERR_STRING_LEN];
      parmlist[7] = new ProgramParameter( ERR_STRING_LEN );

      	 // 9 parameter: input/output, is the return code
      byte[] retcodeB = new byte[4];
      BinaryConverter.intToByteArray(-1, retcodeB, 0);
      parmlist[8] = new ProgramParameter(retcodeB, 4 );

      pgmCall_ = new ProgramCall(system_);
      try {
	  pgmCall_.setProgram("/QSYS.LIB/QYJSPCTU.PGM", parmlist );
      }
      // PropertyVetoException should never happen
      catch (PropertyVetoException pve) {}
      pgmCall_.setThreadSafe(true);  //@A1A

      // Run the program.  Failure returns message list
      if(pgmCall_.run() != true)
      {
	  numberCertificatesFound_ = 0;
	  AS400Message[] messagelist = pgmCall_.getMessageList();
	  cpfError_ = messagelist[0].toString();
	  return -1;

      }

      else
      {
	 // get the retcode returned from the program
	  retcodeB = parmlist[8].getOutputData();
	  rc = BinaryConverter.byteArrayToInt(retcodeB, 0);

	  if (0 != rc)
	  {
	      numberCertificatesFound_ = 0;
	      //unexpected error
	      if (-1 == rc) return rc;
	      //get cpf error id
	      errorInfoB = parmlist[7].getOutputData();
	      cpfError_ = converter_.byteArrayToString(errorInfoB, 0);
	      //trim off white space
	      cpfError_.trim();
	      return rc;
	  }

	  //return output parms, 1st get number of certs found
	  numberCertificatesFoundB = parmlist[6].getOutputData();
	  numberCertificatesFound_ =
	    BinaryConverter.byteArrayToInt(numberCertificatesFoundB, 0);

      }//end else (pgmCall ran)

      return rc;
  }




  int  callfindCertificateUser(byte[] cert,
			       int certlen,
			       int certType)

    throws AS400SecurityException,
           ErrorCompletingRequestException,
           InterruptedException,
           IOException,
           ObjectDoesNotExistException
  {

      int rc;

	 // **** Setup the parameter list ****
      ProgramParameter[] parmlist = new ProgramParameter[7];

	 // First parameter: input, is the pgm entry
      byte[] pgmEntry = new byte[4];
      BinaryConverter.intToByteArray(CALL_USRPRF_FINDCERT, pgmEntry, 0);
      parmlist[0] = new ProgramParameter(pgmEntry);

        // 2 parameter: input, is the certificate array
      parmlist[1] = new ProgramParameter(cert);

	 // 3 parameter: input, is the length of the cert array
      byte[] certlenB = new byte[4];
      BinaryConverter.intToByteArray(certlen, certlenB, 0);
      parmlist[2] = new ProgramParameter(certlenB);

	 // 4 parameter: output, is user profile
      byte[] usrprfPathB = new byte[10];
      parmlist[3] = new ProgramParameter(10);

	 // 5 parameter: input, is certificate type
      byte[] certTypeB = new byte[4];
      BinaryConverter.intToByteArray(certType, certTypeB, 0);
      parmlist[4] = new ProgramParameter(certTypeB);

	 // 6 parameter: output, is the cpf error id array
      byte[] errorInfoB = new byte[ 7];
      parmlist[5] = new ProgramParameter( 7 );

      	 // 7 parameter: input/output, is the return code
      byte[] retcodeB = new byte[4];
      BinaryConverter.intToByteArray(-1, retcodeB, 0);
      parmlist[6] = new ProgramParameter(retcodeB, 4 );

      pgmCall_ = new ProgramCall(system_);
      try {
	  pgmCall_.setProgram("/QSYS.LIB/QYJSPCTU.PGM", parmlist );
      }
      // PropertyVetoException should never happen
      catch (PropertyVetoException pve) {}
      pgmCall_.setThreadSafe(true);  //@A1A

      // Run the program.  Failure returns message list
      if(pgmCall_.run() != true)
      {
	  AS400Message[] messagelist = pgmCall_.getMessageList();
	  cpfError_ = messagelist[0].toString();
	  return -1;
      }

      else
      {
	 // get the retcode returned from the program
	  retcodeB = parmlist[6].getOutputData();
	  rc = BinaryConverter.byteArrayToInt(retcodeB, 0);

	  if (0 != rc)
	  {
	      //unexpected error
	      if (-1 == rc) return rc;
	      //get cpf error id
	      errorInfoB = parmlist[5].getOutputData();
	      cpfError_ = converter_.byteArrayToString(errorInfoB, 0);
	      //trim off white space
	      cpfError_.trim();
	      return rc;
	  }

	   //return output parm, 10 char user name
	  usrprfPathB = parmlist[3].getOutputData();
	  userName_ = converter_.byteArrayToString(usrprfPathB, 0);

      }

      return SUCCESS;

  }

} // End of AS400CertificateVldlUtilImplRemote class





