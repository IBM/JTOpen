///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
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

//--------------------------------------------------------------------
//
// Each reply consists of 3 parts: Header, Template, and
//                                   Optional / Variable-length Data
//
//    Header:   Bytes  1 - 4:  Length of the reply
//                     5 - 6:  Header id
//                     7 - 8:  Client/Server id
//                     9 - 12: Client/Server instance
//                    13 - 16: Correlation id
//                    17 - 18: Length of template
//                    19 - 20: Reply id
//
//    Template: This is fixed length data that is required.
//              Each reply has it's own template format and may vary
//              depending on both the request id and client/server id.
//              The operational results are stored on the server
//              system and can be received at a later time.
//                Bitmap: Used to identify the operation results to
//                        return.  Bit 1 is the left-most bit when it
//                        arrives at the server system
//                        Bit 1: 1=reply should be sent immediately to
//                                 the client application
//                               0=reply is not sent and the rest of
//                                 the bit map is ignored
//                        Bit 2: Message Id
//                        Bit 3: First Level Text
//                        Bit 4: Second Level Text
//                        Bit 5: Data Format
//                        Bit 6: Result Data
//                        Bit 7: SQLCA SQL Communications Area
//                        Bit 8: Server Attributes
//                        Bit 9: Parameter Marker format
//                        Bit 10: Translation Tables
//                        Bit 11: Data Source Information
//                        Bit 12: Package Information
//                        Bit 13: Request is RLE compressed                         @E2A
//                        Bit 14: RLE compression reply desired                     @E2A
//                        Bit 15-32: Reserved                                       @E2C
//                Reserved Area:
//                        Bit 1: Are indicators and data compressed?            // @D1A
//                        Bit 2-32: Reserved                                    // @D1A
//          	  RTNORS:   Numeric value of the Operation Results Set
//                          (ORS) that contains the data to be returned
//                          to the client.
//                          It must be non-negative.
//                RTN_FID:  Contains the ID of the function that
//                          requested this data to be returned to the
//                          client application
//                RQSD_FID: Contains the ID of the function that
//                          requested this data to be put in the ORS
//                Error Data: Contains the error level and the
//                            error return code for that level.
//
//    Optional / Variable-length Data:
//               This is optional data and/or mandatory data that
//               varies in length. The following fields may be
//               returned.
//
//               Message ID:  A seven character field that contains
//                            the id of the message generated as a
//                            results of the last operation performed
//                            by the server.
//               First Level Message Text:
//                            The first level text of the message
//                            generated as a result of the last operation
//                            performed by the server.
//               Second Level Message Text:
//                            The second level text of the message
//                            generated as a result of the last operation
//                            performed by the server.
//               Server Attributes:
//                            Character string that contains the server
//                            attribute information.
//               SQLCA:       Character string that contains SQL
//                            information.
//               Data Format: Contains the format (lay out) of the
//                            result data.
//               Result Data: A string of data that was requested by
//                            function identified in the result function
//                            ID field.
//               Parameter Marker Format:
//                            Contains the format (lay out) of the
//                            parameter markers for the statement.
//               Translation Table Information:
//                            Two 256 byte translate tables are returned.
//
//               See the readAfterHeader() method for the complete
//               list.
//
//---------------------------------------------------------------------

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

//
// Performance note:
//
// (C.Nock 06/30/97):
//
// I have been investigating how to speed up the construction of
// replies.  I would like to reuse storage between replies (like
// is done with requests) using DBStorage and DBStoragePool objects.
// There are two reasons why this is currently not possible.
//
// * We are not responsible for allocating the byte array.  This
//   is done in ClientAccessDataStream.construct().
//
// * Reusing storage for replies requires the code to explicitly
//   state that it is done using a particular reply, which would
//   make the code really messy.  Of course, we could release the
//   storage in the finalize() method of the reply, but this would
//   somewhat unreliable.
//
// It was suggested that we override DataStream.read(), but all that
// does is the copying of the bytes from an input stream into a
// byte array.  This is a necessary action and I am not sure how
// we could improve on it.
//

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
    private int                     byteCount_              = -1;               // @E2A
    private long                    currentLOBLength_       = -1;               // @E1A
    private DBDataFormat            dataFormat_             = null;
    private String		            firstLevelMessageText_  = null;
    private DBLobData               lobData_                = null;             // @D2C
    // @C1D private int                     lobLocatorHandle_       = -1;
	private String		            messageId_              = null;
	private DBReplyPackageInfo      packageInfo_            = null;
	private DBDataFormat	        parameterMarkerFormat_  = null;
	private DBData	                resultData_             = null;
    private boolean                 rleCompressed_          = false;            // @E3A
	private String		            secondLevelMessageText_ = null;
	private	DBReplyServerAttributes	serverAttributes_       = null;
	private DBReplySQLCA		    sqlca_                  = null;
    private DBReplyXids             xids_                   = null;             // @E0A



/**
Output the byte stream contents to the specified PrintStream.
The output format is two hex digits per byte, one space every
four bytes, and sixteen bytes per line.

@param ps the output stream
**/
    void dump (PrintStream ps)
    {
        DBBaseRequestDS.dump (ps, data_, data_.length);        
        
        // Report whether or not the datastream was compressed.                    @E3A
        if (rleCompressed_)                                                     // @E3A
            ps.println("Reply was received RLE compressed.");                   // @E3A

    }



	public long getCurrentLOBLength()                               // @E1A
	{                                                               // @E1A
		return currentLOBLength_;                                   // @E1A
	}                                                               // @E1A



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



// @C1D /**
// @C1D Returns the LOB locator handle.
// @C1D 
// @C1D @return The LOB locator handle, or -1 if not included or empty.
// @C1D **/
// @C1D 	public int getLOBLocatorHandle ()
// @C1D 	{
// @C1D 		return lobLocatorHandle_;
// @C1D 	}



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



// @E0A
/**
Returns the Xids.

@return The Xids, or null if not included or empty.
**/
    public DBReplyXids getXids()
    {
        return xids_;
    }



/**
Parses the datastream.
**/
	public int readAfterHeader (InputStream in)
	 	throws IOException
	{
	    byteCount_ = super.readAfterHeader (in);                                    // @E2C
	 	return byteCount_;                                                          // @E2C
    }                                                                               



    // @E2A - Moved from readAfterHeader().      
    void parse(int dataCompression)                                                 // @E2A
        throws IOException                                                          // @E2A
    {                                                                               // @E2A

        boolean dataCompressed  = ((get32bit(24) & 0x80000000) == 0x80000000);      // @D1A
        boolean oldCompressed = (dataCompressed &&                                  // @E2A
            (dataCompression == AS400JDBCConnection.DATA_COMPRESSION_OLD_));        // @E2A

        // Check to see if the data is RLE compressed.  If so, expand it.           // @E2A
        rleCompressed_ = (dataCompressed &&                                         // @E2A
            (dataCompression == AS400JDBCConnection.DATA_COMPRESSION_RLE_));        // @E2A
        if (rleCompressed_) {                                                       // @E2A
            // Decompress the bytes not including the 44 bytes header and template.    @E2A
            //                                                                         @E2A
            // The format of the header and template is this:                          @E2A
            // Bytes:           Description:                                           @E2A
            //   4              LL - Compressed length of the entire datastream.       @E2A
            //  36              The rest of the uncompressed header and template.      @E2A
            //   4              ll - Length of the compressed data + 10.               @E3A
            //   2              CP - The compression code point.                       @E3A
            //   4              Decompressed length of the data.                       @E2A
            //  LL-10           Compressed data.                                       @E2A @E3C
            //
            // After decompression, the header and template should look like this:     @E2A
            // Bytes:           Description:                                           @E2A
            //   4              LL - Length of the entire datastream after decompress. @E2A
            //  36              The rest of the uncompressed header and template.      @E2A
            //  LL-40           Decompressed data.                                     @E2A
            
            // Check the CP to determine the compression scheme.  We currently only // @E3A
            // handle RLE.                                                          // @E3A
            int compressionSchemeCP = get16bit(44);                                 // @E3A

            if (compressionSchemeCP != AS400JDBCConnection.DATA_COMPRESSION_RLE_)   // @E3A
                throw new IOException();                                            // @E3A
            int lengthOfDecompressedData = get32bit(46);                            // @E2A @E3C
            byte[] newData = new byte[lengthOfDecompressedData + 40];               // @E2A
            BinaryConverter.intToByteArray(newData.length, newData, 0);             // @E2A
            System.arraycopy(data_, 4, newData, 4, 36);                             // @E2A
            DataStreamCompression.decompressRLE(data_, 50, get32bit(0)-50,          // @E2A @E3C
                newData, 40, DataStreamCompression.DEFAULT_ESCAPE);                 // @E2A
            data_ = newData;                                                        // @E2A
            byteCount_ = data_.length - 20;                                         // @E2A
        }                                                                           // @E2A

		// Read the template portion of data stream.
		errorClass_             = get16bit(34);
		returnCode_             = get32bit(36);

		// Move offset to the start of the optional - variable
		// length portion of the data stream.
		int offset = HEADER_LENGTH + TEMPLATE_LENGTH_;

		// Using offset as a pointer into the datastream,
		// walk through the optional - variable length portion
		// of the data stream.
		while (offset < byteCount_ + HEADER_LENGTH) {                               // @E2C

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
			        resultData_	= new DBOriginalData (parmLength, oldCompressed);         // @D1C @E2C
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

            // Translation table information.
			// case 0x3809:
			    // We don't care much about translatation
			    // table information.
				// break;

            // DSN attributes.
			// case 0x380A:
			    // We don't care much about DSN attributes.
				// break;

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
			        resultData_	= new DBExtendedData(parmLength, oldCompressed);           // @D1C @E2C
			        resultData_.overlay (data_, offset + 6);

			        // @C1D // This doubles as lob data.
			        // @C1D lobData_ = new DBByteSubarray (parmLength - 6);
			        // @C1D lobData_.overlay (data_, offset + 6);
			    }
				break;

            // LOB locator data.                                                    // @C1A
			case 0x380F:
                if (parmLength != 6) {                                              // @C1A
    		        // @C1D lobLocatorHandle_ = get32bit (offset + 6);
                    // @D2D int ccsid = get16bit (offset + 6);                      // @C1A
                    int length = get32bit (offset + 8);                             // @C1A
		    lobData_ = new DBLobData (length, parmLength -12, oldCompressed);  // @C1A @D2C @D4C @E2C @E4C
                    lobData_.overlay (data_, offset + 12);                          // @C1A
                }                                                                   // @C1A
				break;

            // Current LOB length.                                                  // @E1A
            case 0x3810:                                                            // @E1A
                int sl = get16bit(offset + 6);                                      // @E1A
                if (sl == 4)                                                        // @E1A
                    currentLOBLength_ = get32bit(offset + 8);                       // @E1A
                else                                                                // @E1A
                    currentLOBLength_ = get64bit(offset + 8);                       // @E1A
                break;                                                              // @E1A

            // Xids.                                                                // @E0A
            case 0x38A1:                                                            // @E0A
				xids_ = new DBReplyXids(data_, offset + 6, parmLength);             // @E0A
                break;                                                              // @E0A

			}            

			offset += parmLength;
		}

	}



}





