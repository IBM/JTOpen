///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: AS400CertificateUtilImplRemote.java
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
   Provides the implementation of the remote methods for accessing certificates from an AS400CertificateUtil object.
**/
class AS400CertificateUtilImplRemote  extends AS400CertificateUtilImpl
{

 //********************************************************************/
 //* methods for remote invocation                                    */
 //*                                                                  */
 //* @return  Return code mapped to CPFxxxx error message.            */
 //********************************************************************/


  int callgetCertificates(String usrSpaceName,
			   int buffSize,
			   int nextCertificateToReturn,
			   int nextCertificateOffsetIn)
    throws AS400SecurityException,
           ErrorCompletingRequestException,
           InterruptedException,
           IOException,
           ObjectDoesNotExistException
  {

      int rc;
      int i, j;
      int length, certoffset;

	 // **** Setup the parameter list ****
      ProgramParameter[] parmlist = new ProgramParameter[11];

	 // First parameter: input, is the pgm entry number
      byte[] pgmEntry = new byte[4];
      BinaryConverter.intToByteArray(CALL_GETCERT, pgmEntry, 0);
      parmlist[0] = new ProgramParameter(pgmEntry);

     	 // 2 parameter: input, is user space native name
      byte[] usrSpaceNameB = new byte[20];
      converter_.stringToByteArray(usrSpaceName, usrSpaceNameB);
      parmlist[1] = new ProgramParameter(usrSpaceNameB);

        // 3 parameter: input, is the user's buff size
      byte[] buffSizeB = new byte[4];
      BinaryConverter.intToByteArray(buffSize, buffSizeB, 0);
      parmlist[2] = new ProgramParameter(buffSizeB);

        // 4 parameter: input, is the next certificate to return
      byte[] nextCertificateToReturnB = new byte[4];
      BinaryConverter.intToByteArray(nextCertificateToReturn,
				    nextCertificateToReturnB, 0);
      parmlist[3] = new ProgramParameter(nextCertificateToReturnB);

      // 5 parameter: input, is the cert offset
      byte[] nextCertificateOffsetInB = new byte[4];
      BinaryConverter.intToByteArray(nextCertificateOffsetIn,
				    nextCertificateOffsetInB, 0);
      parmlist[4] = new ProgramParameter(nextCertificateOffsetInB);

      // 6 parameter: output, is the next cert offset to return
      parmlist[5] = new ProgramParameter(4);


	 // 7 parameter: output, is the number certs found
      byte[] numberCertificatesFoundB = new byte[4];
      parmlist[6] = new ProgramParameter(4);

      	 // 8 parameter: output, is the number complete certs
      byte[] numberCompleteCertsB = new byte[4];
      parmlist[7] = new ProgramParameter(4);

      	 // 9 parameter: output, is the certificate buffer
      parmlist[8] = new ProgramParameter(buffSize);

	 // 10 parameter: output, is the cpf error id array
      byte[] errorInfoB = new byte[ ERR_STRING_LEN ];
      parmlist[9] = new ProgramParameter( ERR_STRING_LEN );

      	 // 11 parameter: input/output, is the return code
      byte[] retcodeB = new byte[4];
      BinaryConverter.intToByteArray(-1, retcodeB, 0);
      parmlist[10] = new ProgramParameter(retcodeB, 4 );


      //program lib and name
      ProgramCall pgmCall = new ProgramCall(system_);
      try {
	  pgmCall.setProgram("/QSYS.LIB/QYJSPCTU.PGM", parmlist );
      }
      // PropertyVetoException should never happen
      catch (PropertyVetoException pve) { Trace.log(Trace.ERROR, pve); }
      pgmCall.suggestThreadsafe();  //@A1A

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
	  retcodeB = parmlist[10].getOutputData();
	  rc = BinaryConverter.byteArrayToInt(retcodeB, 0);

	  if (0 != rc)
	  {
	      numberCertificatesFound_ = 0;
	      //unexpected error
	      if (-1 == rc) return rc;
	      //get cpf error id
	      errorInfoB = parmlist[9].getOutputData();
	      cpfError_ = converter_.byteArrayToString(errorInfoB, 0).trim();
	      return rc;
	  }

	  //return output parms, 1st get next certificate's offset
	  byte[] nextCertificateOffsetOutB = parmlist[5].getOutputData();
	  nextCertificateOffsetOut_ =
	    BinaryConverter.byteArrayToInt(nextCertificateOffsetOutB, 0);

	  //return output parms, get number of certs found
	  numberCertificatesFoundB = parmlist[6].getOutputData();
	  numberCertificatesFound_ =
	    BinaryConverter.byteArrayToInt(numberCertificatesFoundB, 0);

	  //get number of complete certs in user buffer
	  numberCompleteCertsB = parmlist[7].getOutputData();
	  int numberCompleteCerts =
	    BinaryConverter.byteArrayToInt(numberCompleteCertsB, 0);

	  //load returned certs into AS400Certificate array
	  if (0 < numberCompleteCerts)
	  {
	      certificates_ = new AS400Certificate[numberCompleteCerts];
	  }
	  int returnedLen;
	  int certLen;
	  length = 0;
	  byte[] returnedBytes = parmlist[8].getOutputData();

	  //hdr format => cert returned length, cert offset, cert len
	  for (i = 0; i < numberCompleteCerts; ++i)
	  {
	      returnedLen = BinaryConverter.byteArrayToInt(returnedBytes,
							   length);
	      certoffset = BinaryConverter.byteArrayToInt(returnedBytes,
							  length + 4);
	      certLen = BinaryConverter.byteArrayToInt(returnedBytes,
						       length + 8);
	      byte[] certdata = new byte[certLen];

	      System.arraycopy(returnedBytes, certoffset,
			       certdata, 0,
			       certLen);

	      certificates_[i] = new AS400Certificate(certdata);

	      //calc offset to next cert header
	      length = length + returnedLen;
	  }

      }//end else (pgmCall ran OK)

      return SUCCESS;
  }



  int callgetHandle(byte[] certificate,
		     int len)
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
      ProgramParameter[] parmlist = new ProgramParameter[6];

	 // First parameter: input, is the pgm entry number
      byte[] pgmEntry = new byte[4];
      BinaryConverter.intToByteArray(CALL_GETHANDLE, pgmEntry, 0);
      parmlist[0] = new ProgramParameter(pgmEntry);

	 // 2 parameter: input, is the certificate array
      parmlist[1] = new ProgramParameter(certificate);

	 // 3 parameter: input, is the length of the cert array
      byte[] lenB = new byte[4];
      BinaryConverter.intToByteArray(certificate.length, lenB, 0);
      parmlist[2] = new ProgramParameter(lenB);

     	 // 4 parameter: out, is the certificate handle
      parmlist[3] = new ProgramParameter(HANDLE_LEN);

         // 5 parameter: output, is the cpf error id array
      parmlist[4] = new ProgramParameter( 7 );

      	 // 6 parameter: input/output, is the return code
      byte[] retcodeB = new byte[4];
      BinaryConverter.intToByteArray(-1, retcodeB, 0);
      parmlist[5] = new ProgramParameter(retcodeB, 4 );


      //program lib and name
      ProgramCall pgmCall = new ProgramCall(system_);
      try {
	  pgmCall.setProgram("/QSYS.LIB/QYJSPCTU.PGM", parmlist );
      }
      // PropertyVetoException should never happen
      catch (PropertyVetoException pve) { Trace.log(Trace.ERROR, pve); }
      pgmCall.suggestThreadsafe();  //@A1A

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
	  retcodeB = parmlist[5].getOutputData();
	  rc = BinaryConverter.byteArrayToInt(retcodeB, 0);

	  if (0 != rc)
	  {
	      //unexpected error
	      if (-1 == rc) return rc;
	      //get cpf error id
	      byte[] errorInfoB = parmlist[4].getOutputData();
	      cpfError_ = converter_.byteArrayToString(errorInfoB, 0).trim();
	      return rc;
	  }

	  //return output parms, get cert handle
	  handle_ = parmlist[3].getOutputData();

      }//end else (pgmCall ran)

      return SUCCESS;

  }



} // End of AS400CertificateUtilImplRemote class





