///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: DBBaseReplyDS.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.InputStream;
import java.io.IOException;
import java.io.PrintStream;


 /**
   This is the base class for all database server reply data
   streams.  Every concrete database server reply data stream
   should inherit from this class.

   Here are the steps needed to handle a reply data stream
   from the server.

     1.  Add a prototype reply data stream to the collection
	     of reply prototypes.  There must be a prototype reply
		 for every type of reply that must be constructed
		 automatically.

		 static
	     {
		   AS400Server.addReplyStream (new DBReplyRequestedDS (),
		   "as-database");
	     }

      2. Send a request and ask for reply data

	     DBReplyRequestedDS reply = sendAndReceive (request);

         a. AS400Server run method will call ClientAccessDataStream
		    construct method.
		 b.	ClientAccessDataStream construct method will call
		    readAfterHeader which parses out the data stream

	  3. Check for errors
	     An error class and return code are always returned.  These
		 report on the success or failure of the request.  The
		 meanings of the various error classes and return codes
		 are described in the database server specification.

         int ErrorClass = requestedData.getErrorClass();
         int ErrorClassReturnCode = requestedData.getReturnCode()

	  4. Use the methods provided to get the information needed from
	     the data stream. The results returned will depend on the
		 values in the Operation Results Bitmap.


 **/
abstract class DBBaseReplyDS
extends ClientAccessDataStream
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // Constants.
	private static final String     NODATA_             = "0";
	private static final int	    TEMPLATE_LENGTH_    = 20;



	// Template variables.
	private int	                    errorClass_             = 0;
	private int	                    returnCode_             = 0;



	// Optional variables.   
   private DBDataFormat            dataFormat_             = null;
   private String		            firstLevelMessageText_  = null;
   private DBLobData               lobData_                = null;             // @D2C
	private String		            messageId_              = null;
	private DBReplyPackageInfo      packageInfo_            = null;
	private DBDataFormat	        parameterMarkerFormat_  = null;
	private DBData	                resultData_             = null;
	private String		            secondLevelMessageText_ = null;
	private DBReplyServerAttributes	serverAttributes_       = null;
	private DBReplySQLCA		    sqlca_                  = null;



/**
Output the byte stream contents to the specified PrintStream.
The output format is two hex digits per byte, one space every
four bytes, and sixteen bytes per line.

@param ps the output stream
**/
    void dump (PrintStream ps)
    {
        DBBaseRequestDS.dump (ps, data_, data_.length);
    }



/**
Copyright.
**/
    private static String getCopyright()
    {
       return Copyright.copyright;
    }



/**
Returns the data format.

@return The data format, or null if not included or empty.
**/
	public DBDataFormat getDataFormat ()
	{
		return dataFormat_;
	}



/**
Returns the error class.

@return The error class.
**/
	public int getErrorClass ()
	{
		return errorClass_;
	}



/**
Returns the first level message text.

@return The first level message text, or null if not included or empty.
**/
	public String getFirstLevelMessageText ()
	{
		return firstLevelMessageText_;
	}



/**
Returns the LOB data.

@return The LOB data, or null if not included or empty.
**/
	public DBLobData getLOBData ()                              // @D2C
	{
		return lobData_;
	}


/**
Returns the message id.

@return The message id, or null if not included or empty.
**/
	public String getMessageId ()
	{
		return messageId_;
	}



/**
Returns the package info.

@return The package info, or null if not included or empty.
**/
	public DBReplyPackageInfo getPackageInfo ()
	{
		return packageInfo_;
	}



/**
Returns the parameter marker format.

@return The parameter marker format, or null if not included or empty.
**/
	public DBDataFormat getParameterMarkerFormat ()
	{
		return parameterMarkerFormat_;
	}



/**
Returns the result data.

@return The result data, or null if not included or empty.
**/
	public DBData getResultData ()
	{
		return resultData_;
	}



/**
Returns the return code.

@return The return code.
**/
	public int getReturnCode ()
	{
		return returnCode_;
	}



/**
Returns the function id of the corresponding request.

@return The function id of the corresponding request.
**/
    public int getReturnDataFunctionId ()
    {
        return get16bit (30);
    }



/**
Returns the second level message text.

@return The second level message text, or null if not
        included or empty.
**/
	public String getSecondLevelMessageText ()
	{
		return secondLevelMessageText_;
	}



/**
Returns the server attributes.

@return The server attributes, or null if not included or empty.
**/
	public DBReplyServerAttributes getServerAttributes ()
	{
		return serverAttributes_;
	}



/**
Returns the SQLCA.

@return The SQLCA, or null if not included or empty.
**/
	public DBReplySQLCA getSQLCA ()
	{
		return sqlca_;
	}



/**
Parses the datastream.
**/
	public int readAfterHeader (InputStream in)
		throws IOException
	{
		int byteCount = super.readAfterHeader (in);

		// Read the template portion of data stream.
        boolean dataCompressed  = ((get32bit(24) & 0x80000000) == 0x80000000);       // @D1A
		errorClass_             = get16bit(34);
		returnCode_             = get32bit(36);

		// Move offset to the start of the optional - variable
		// length portion of the data stream.
		int offset = HEADER_LENGTH + TEMPLATE_LENGTH_;

		// Using offset as a pointer into the datastream,
		// walk through the optional - variable length portion
		// of the data stream.
		while (offset < byteCount + HEADER_LENGTH) {

			int parmLength	= get32bit (offset);
			int codePoint	= get16bit (offset + 4);

			// There may be times when a length (ll) and a
			// codepoint were returned but no data.  In this
			// case the length will be = 6.  We will treat
			// this as if no such parameter came down.
			switch (codePoint)
			{

            // Message ID.
			case 0x3801:
				// 8 =  length (4) + codePoint (2) + ccsid (2)
				if (parmLength != 6) {	   //  data was sent
				    ConverterImplRemote converter = ConverterImplRemote.getConverter (get16bit (offset + 6), system_); // @D0C
					messageId_ = converter.byteArrayToString (data_, offset + 8, parmLength - 8);
				}
				else				   // no data was sent
					messageId_ = NODATA_;
				break;

            // First level message text.
			case 0x3802:
				// 10 = length (4) + codePoint (2) + ccsid (2) + streamlength (2)
				if (parmLength != 6) {	   //  data was sent
				    ConverterImplRemote converter = ConverterImplRemote.getConverter (get16bit (offset + 6), system_); // @D0C
					firstLevelMessageText_ = converter.byteArrayToString (data_, offset + 10, parmLength - 10);
				}
				else				   // no data was sent
					firstLevelMessageText_ = NODATA_;
				break;

            // Second level message text.
			case 0x3803:
				// 10 = length (4) + codePoint (2) + ccsid (2) + streamlength (2)
				if (parmLength != 6) {	   // data was sent
				    ConverterImplRemote converter = ConverterImplRemote.getConverter (get16bit (offset + 6), system_); // @D0C
					secondLevelMessageText_ = converter.byteArrayToString (data_, offset + 10, parmLength - 10);
				}
 				else				   // no data was sent
					secondLevelMessageText_ = NODATA_;
                break;

            // Server attributes.
			case 0x3804:
			    try
			    {
    			    serverAttributes_	= new DBReplyServerAttributes (data_,
	    								  offset + 8,
		    							  parmLength);
		    	}
		    	catch (DBDataStreamException x)
		    	{
		    	    throw new IOException ();
		    	}
				break;

            // Data format.
			case 0x3805:
			    if (parmLength != 6) {
			        dataFormat_	= new DBOriginalDataFormat ();
			        dataFormat_.overlay (data_, offset + 6);
			    }
				break;

            // Result data.
			case 0x3806:
			    if (parmLength != 6) {
			        resultData_	= new DBOriginalData (parmLength, dataCompressed);         // @D1C
			        resultData_.overlay (data_, offset + 6);
			    }
				break;

            // SQLCA.
			case 0x3807:
				sqlca_		= new DBReplySQLCA (data_, offset + 6, parmLength);

				break;

            // Parameter marker format.
			case 0x3808:
			    if (parmLength != 6) {
			        parameterMarkerFormat_ = new DBOriginalDataFormat ();
			        parameterMarkerFormat_.overlay (data_, offset + 6);
			    }
				break;

            // Package return information.
			case 0x380B:
			    packageInfo_ = new DBReplyPackageInfo (data_,
							offset + 6, parmLength, system_.getCcsid());        // @D3C
				break;

            // Extended data format.
			case 0x380C:
			    if (parmLength != 6) {
			        dataFormat_	= new DBExtendedDataFormat ();
			        dataFormat_.overlay (data_, offset + 6);
			    }
				break;

            // Extended parameter marker format.
			case 0x380D:
			    if (parmLength != 6) {
			        parameterMarkerFormat_ = new DBExtendedDataFormat ();
			        parameterMarkerFormat_.overlay (data_, offset + 6);
			    }
				break;

            // Extended result data.
			case 0x380E:
			    if (parmLength != 6) {
			        resultData_	= new DBExtendedData(parmLength, dataCompressed);           // @D1C
			        resultData_.overlay (data_, offset + 6);
			    }
				break;

            // LOB locator data.                                                    // @C1A
			case 0x380F:
                if (parmLength != 6) {                                              // @C1A
                    int length = get32bit (offset + 8);                             // @C1A
                    lobData_ = new DBLobData (length, parmLength, dataCompressed);  // @C1A @D2C @D4C
                    lobData_.overlay (data_, offset + 12);                          // @C1A
                }                                                                   // @C1A
				break;

			}

			offset += parmLength;
		}

		return byteCount;
	}



}





