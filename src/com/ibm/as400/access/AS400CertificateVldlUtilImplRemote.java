///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: AS400CertificateVldlUtilImplRemote.java
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
   <p>The AS400CertificateVldlUtilImplRemote provides the implementation of the remote methods for accessing certificates in an IBM i validation list object.
**/
class AS400CertificateVldlUtilImplRemote  extends AS400CertificateVldlUtilImpl
{

 //********************************************************************/
 //* methods for remote invocation                                    */
 //*                                                                  */
 //* @return  Return code mapped to CPFxxxx error message.            */
 //********************************************************************/

  int calladdCertificate(byte[] cert, int certlen,
			  String ifsPathName, int pathlen)
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
      BinaryConverter.intToByteArray(CALL_VLDL_ADD, pgmEntry, 0);
      parmlist[0] = new ProgramParameter(pgmEntry);

        // 2 parameter: input, is the certificate array
      parmlist[1] = new ProgramParameter(cert);

	 // 3 parameter: input, is the length of the cert array
      byte[] certlenB = new byte[4];
      BinaryConverter.intToByteArray(certlen, certlenB, 0);
      parmlist[2] = new ProgramParameter(certlenB);

	 // 4 parameter: input, is vldl path name
      byte[] vldlPathB = new byte[pathlen];
      converter_.stringToByteArray(ifsPathName, vldlPathB);
      parmlist[3] = new ProgramParameter(vldlPathB);

	 // 5 parameter: input, is vldl path name len
      byte[] pathlenB = new byte[4];
      BinaryConverter.intToByteArray(pathlen, pathlenB, 0);
      parmlist[4] = new ProgramParameter(pathlenB);

	 // 6 parameter: output, is the cpf error id array
      parmlist[5] = new ProgramParameter( 7 );

      	 // 7 parameter: input/output, is the return code
      byte[] retcodeB = new byte[4];
      BinaryConverter.intToByteArray(-1, retcodeB, 0);
      parmlist[6] = new ProgramParameter(retcodeB, 4 );

      ProgramCall pgmCall = new ProgramCall(system_);
      try {
	  pgmCall.setProgram("/QSYS.LIB/QYJSPCTU.PGM", parmlist );
      }
      // PropertyVetoException should never happen
      catch (PropertyVetoException pve) {}
      //pgmCall.suggestThreadsafe();  //@A1A

      // Run the program.  Failure returns message list
      if(pgmCall.run() != true)
      {
	  AS400Message[] messagelist = pgmCall.getMessageList();
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
	      byte[] errorInfoB = parmlist[5].getOutputData();
	      cpfError_ = converter_.byteArrayToString(errorInfoB, 0).trim();
	      return rc;
	  }
      }

      return SUCCESS;
  }




  int calldeleteCertificate(byte[] cert, int certlen,
			     String ifsPathName, int pathlen,
			     int certType)
    throws AS400SecurityException,
           ErrorCompletingRequestException,
           InterruptedException,
           IOException,
           ObjectDoesNotExistException
  {


      int rc;

	 // **** Setup the parameter list ****
      ProgramParameter[] parmlist = new ProgramParameter[8];

	 // First parameter: input, is the pgm entry
      byte[] pgmEntry = new byte[4];
      BinaryConverter.intToByteArray(CALL_VLDL_DELETE, pgmEntry, 0);
      parmlist[0] = new ProgramParameter(pgmEntry);

        // 2 parameter: input, is the certificate array
      parmlist[1] = new ProgramParameter(cert);

	 // 3 parameter: input, is the length of the cert array
      byte[] certlenB = new byte[4];
      BinaryConverter.intToByteArray(certlen, certlenB, 0);
      parmlist[2] = new ProgramParameter(certlenB);

	 // 4 parameter: input, is vldl path name
      byte[] vldlPathB = new byte[pathlen];
      converter_.stringToByteArray(ifsPathName, vldlPathB);
      parmlist[3] = new ProgramParameter(vldlPathB);

	 // 5 parameter: input, is vldl path name len
      byte[] pathlenB = new byte[4];
      BinaryConverter.intToByteArray(pathlen, pathlenB, 0);
      parmlist[4] = new ProgramParameter(pathlenB);

        // 6 parameter: input, is certificate type
      byte[] certTypeB = new byte[4];
      BinaryConverter.intToByteArray(certType, certTypeB, 0);
      parmlist[5] = new ProgramParameter(certTypeB);

	 // 7 parameter: output, is the cpf error id array
      parmlist[6] = new ProgramParameter( 7 );

      	 // 8 parameter: input/output, is the return code
      byte[] retcodeB = new byte[4];
      BinaryConverter.intToByteArray(-1, retcodeB, 0);
      parmlist[7] = new ProgramParameter(retcodeB, 4 );

      ProgramCall pgmCall = new ProgramCall(system_);
      try {
	  pgmCall.setProgram("/QSYS.LIB/QYJSPCTU.PGM", parmlist );
      }
      // PropertyVetoException should never happen
      catch (PropertyVetoException pve) {}
      //pgmCall.suggestThreadsafe();  //@A1A

      // Run the program.  Failure returns message list
      if(pgmCall.run() != true)
      {
	  AS400Message[] messagelist = pgmCall.getMessageList();
	  cpfError_ = messagelist[0].toString();
	  return -1;
      }

      else
      {
	 // get the retcode returned from the program
	  retcodeB = parmlist[7].getOutputData();
	  rc = BinaryConverter.byteArrayToInt(retcodeB, 0);

	  if (0 != rc)
	  {
	      //unexpected error
	      if (-1 == rc) return rc;
	      //get cpf error id
	      byte[] errorInfoB = parmlist[6].getOutputData();
	      cpfError_ = converter_.byteArrayToString(errorInfoB, 0).trim();
	      return rc;
	  }
      }

      return SUCCESS;
  }



  int calllistCertificates(String ifsPathName, int pathlen,
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
      ProgramParameter[] parmlist = new ProgramParameter[10];

	 // First parameter: input, is the pgm entry number
      byte[] pgmEntry = new byte[4];
      BinaryConverter.intToByteArray(CALL_VLDL_LISTCERT, pgmEntry, 0);
      parmlist[0] = new ProgramParameter(pgmEntry);

      	 // 2 parameter: input, is vldl path name
      byte[] vldlPathB = new byte[pathlen];
      converter_.stringToByteArray(ifsPathName, vldlPathB);
      parmlist[1] = new ProgramParameter(vldlPathB);

	 // 3 parameter: input, is vldl path name len
      byte[] vldlLengthB = new byte[4];
      BinaryConverter.intToByteArray(pathlen, vldlLengthB, 0);
      parmlist[2] = new ProgramParameter(vldlLengthB);

    	 // 4 parameter: input, is user space native name
      byte[] usrSpaceNameB = new byte[20];
      converter_.stringToByteArray(usrSpaceName, usrSpaceNameB);
      parmlist[3] = new ProgramParameter(usrSpaceNameB);

	 // 5 parameter: input, is parm entered array
      byte[] parmPresent = new byte[SEARCH_PARMS * 4];
      for (i = 0; i < SEARCH_PARMS; ++i)
      {
	  if (parmEntered[i])
	      BinaryConverter.intToByteArray(1, parmPresent, i * 4);
	  else
	      BinaryConverter.intToByteArray(0, parmPresent, i * 4);
      }
      parmlist[4] = new ProgramParameter(parmPresent);


	 // 6, 7 parameter: input, parm sizes and search attrs array
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

      parmlist[5] = new ProgramParameter(parmsizeBytes);
      parmlist[6] = new ProgramParameter(attrbytes);

	 // 8 parameter: output, is the number certs found
      parmlist[7] = new ProgramParameter(4);


 	 // 9 parameter: output, is the cpf error id array
      byte[] errorInfoB = new byte[ERR_STRING_LEN];
      parmlist[8] = new ProgramParameter( ERR_STRING_LEN );

      	 // 10 parameter: input/output, is the return code
      byte[] retcodeB = new byte[4];
      BinaryConverter.intToByteArray(-1, retcodeB, 0);
      parmlist[9] = new ProgramParameter(retcodeB, 4 );

      ProgramCall pgmCall = new ProgramCall(system_);
      try {
	  pgmCall.setProgram("/QSYS.LIB/QYJSPCTU.PGM", parmlist );
      }
      // PropertyVetoException should never happen
      catch (PropertyVetoException pve) {}
      //pgmCall.suggestThreadsafe();  //@A1A

      // Run the program.  Failure returns message list
      if(pgmCall.run() != true)
      {

	  numberCertificatesFound_ = 0;

	  AS400Message[] messagelist = pgmCall.getMessageList();
	  cpfError_ = messagelist[0].toString();
	  return -1;

      }

      else
      {
	 // get the retcode returned from the program
	  retcodeB = parmlist[9].getOutputData();
	  rc = BinaryConverter.byteArrayToInt(retcodeB, 0);

	  if (0 != rc)
	  {
	      numberCertificatesFound_ = 0;
	      //unexpected error
	      if (-1 == rc) return rc;
	      //get cpf error id
	      errorInfoB = parmlist[8].getOutputData();
	      cpfError_ = converter_.byteArrayToString(errorInfoB, 0).trim();
	      return rc;
	  }

	  //return output parms, 1st get number of certs found
	  byte[] numberCertificatesFoundB = parmlist[7].getOutputData();
	  numberCertificatesFound_  =
	    BinaryConverter.byteArrayToInt(numberCertificatesFoundB, 0);

      }//end else (pgmCall ran)

      return rc;
  }




  int  callcheckCertificate(byte[] cert, int certlen,
			     String ifsPathName, int pathlen,
			     int   certType)
    throws AS400SecurityException,
           ErrorCompletingRequestException,
           InterruptedException,
           IOException,
           ObjectDoesNotExistException
  {

      int rc;

	 // **** Setup the parameter list ****
      ProgramParameter[] parmlist = new ProgramParameter[9];

	 // First parameter: input, is the pgm entry
      byte[] pgmEntry = new byte[4];
      BinaryConverter.intToByteArray(CALL_VLDL_CHECKCERT, pgmEntry, 0);
      parmlist[0] = new ProgramParameter(pgmEntry);

        // 2 parameter: input, is the certificate array
      parmlist[1] = new ProgramParameter(cert);

	 // 3 parameter: input, is the length of the cert array
      byte[] certlenB = new byte[4];
      BinaryConverter.intToByteArray(certlen, certlenB, 0);
      parmlist[2] = new ProgramParameter(certlenB);

	 // 4 parameter: input, is vldl path name
      byte[] vldlPathB = new byte[pathlen];
      converter_.stringToByteArray(ifsPathName, vldlPathB);
      parmlist[3] = new ProgramParameter(vldlPathB);

	 // 5 parameter: input, is vldl path name len
      byte[] pathlenB = new byte[4];
      BinaryConverter.intToByteArray(pathlen, pathlenB, 0);
      parmlist[4] = new ProgramParameter(pathlenB);

	 // 6 parameter: input, is certificate type
      byte[] certTypeB = new byte[4];
      BinaryConverter.intToByteArray(certType, certTypeB, 0);
      parmlist[5] = new ProgramParameter(certTypeB);

        // 7 parameter: output, is cert present indicator
      parmlist[6] = new ProgramParameter( 4 );

	 // 8 parameter: output, is the cpf error id array
      byte[] errorInfoB = new byte[ 7 ];
      parmlist[7] = new ProgramParameter( 7 );

      	 // 9 parameter: input/output, is the return code
      byte[] retcodeB = new byte[4];
      BinaryConverter.intToByteArray(-1, retcodeB, 0);
      parmlist[8] = new ProgramParameter(retcodeB, 4 );

      ProgramCall pgmCall = new ProgramCall(system_);
      try {
	  pgmCall.setProgram("/QSYS.LIB/QYJSPCTU.PGM", parmlist );
      }
      // PropertyVetoException should never happen
      catch (PropertyVetoException pve) {}
      //pgmCall.suggestThreadsafe();  //@A1A

      // Run the program.  Failure returns message list
      if(pgmCall.run() != true)
      {
	  AS400Message[] messagelist = pgmCall.getMessageList();
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
	      //unexpected error
	      if (-1 == rc) return rc;
	      //get cpf error id
	      errorInfoB = parmlist[7].getOutputData();
	      cpfError_ = converter_.byteArrayToString(errorInfoB, 0).trim();
	      return rc;
	  }

	   //return output parm, cert present indicator
	  byte[] presentB = parmlist[6].getOutputData();
	  present_  =
	    BinaryConverter.byteArrayToInt(presentB, 0);
      }

      return SUCCESS;

  }

} // End of AS400CertificateVldlUtilImplRemote class





