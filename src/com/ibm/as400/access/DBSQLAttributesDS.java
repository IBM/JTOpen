///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: DBSQLAttributesDS.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.sql.SQLException;                                            //@E9a
import java.io.UnsupportedEncodingException;                             //@H1A


/**
   Create an SQL Attribute data stream
**/

class DBSQLAttributesDS
extends DBBaseRequestDS
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";

    public static final int   FUNCTIONID_RETRIEVE_ATTRIBUTES   = 0x1F81;
    public static final int   FUNCTIONID_SET_ATTRIBUTES        = 0x1F80;


/**
   Constructs a datastream for the SQL Server Attribute functions.
   @param  requestId the 4 digit code that represents the function being called.
   @param  rpbId   the request parameter block id.
   @param  operationResultsBitmap the bitmap which describes how the results are to be returned.
   @param  basedOnORSHandle	the based on operational results set.
   @param  parameterMarkerDescriptorHandle the Parameter marker descriptor handle identifier.
**/

    public DBSQLAttributesDS(int requestId,
                             int rpbId,
                             int operationResultsBitmap,
                             //@P0D int basedOnORSHandle, // This isn't used
                             int parameterMarkerDescriptorHandle)

    {
        // Create the datastream header and template
        super(requestId, rpbId, operationResultsBitmap,
              parameterMarkerDescriptorHandle);
        setServerID(SERVER_SQL);
    }



    //--------------------------------------------------//
    // Create the data stream optional /                //
    // variable length data section via addParameters   //
    //--------------------------------------------------//


/**
   Sets the Ambiguous Select Option parameter in the data stream.
   @param value	the value that indicates how SQL SELECT
   statements which do not have explicit FOR FETCH ONLY or FOR
   UPDATE OF clauses specified should be treated with regard to
   updatability.
   @exception DBDataStreamException If there is not enough space left in the data byte array.
**/
    void setAmbiguousSelectOption (int value)
    throws DBDataStreamException
    {
        addParameter (0x3811, (short) value);
    }



/**
   Sets the ASCII CCSID for Translation Table parameter in the data stream.
   @param value	the ASCII CCSID for the translation table.
   @exception DBDataStreamException If there is not enough space left in the data byte array.
**/

    void setASCIICCSIDForTranslationTable (int value)
    throws DBDataStreamException
    {
        addParameter (0x3810, (short) value);
    }



    // @E1D void setAutoCommit(int value)                                           // @E1A
    // @E1D     throws DBDataStreamException                                        // @E1A
    // @E1D {                                                                       // @E1A
    // @E1D     addParameter(0x3824, (short)value);                                 // @E1A
    // @E1D }                                                                       // @E1A



/**
   Sets the Client CCSID parameter in the data stream.
   @param value	the value to be used to set the default
   client CCSID.
   @exception DBDataStreamException If there is not enough space left in the data byte array.
**/
    void setClientCCSID (int value)
    throws DBDataStreamException
    {
        addParameter (0x3801, (short) value);
    }



// @E2C
/**
   Sets the Client Functional Level parameter in the data stream.
   @param value	the client functional level.
   @exception DBDataStreamException If there is not enough space left in the data byte array.
**/

    void setClientFunctionalLevel(String value)                         // @E2C
    throws DBDataStreamException
    {
        try                                                                   //@H1A
        {
            //@H1D There is no need to use a Converter for this value             @E2A
            //@H1D since it is always numeric.                                    @E2A
            //@H1A Need to use Converter now since this is not all numeric any more.
            addParameter (0x3803, ConvTable.getTable(37, null), value); // @E2C @H1C 
        }
        catch (SQLException se)                                               //@H1A
        {                                                                     //@H1A
            //only throws an SQL exception if bytes.length when converted >   //@H1A
            //65535, which they will never be in this case                    //@H1A
        }                                                                     //@H1A
        catch (UnsupportedEncodingException e)                                //@H1A
        {                                                                     //@H1A
            //37 will always be supported                                     //@H1A
        }                                                                     //@H1A
    }



/**
   Sets the Commitment Control Level Parser Option parameter in the data stream.
   @param value	the commitment control level.
   @exception DBDataStreamException If there is not enough space left in the data byte array.
**/
    void setCommitmentControlLevelParserOption (int value)
    throws DBDataStreamException
    {
        addParameter (0x380E, (short) value);
    }



    // @D0A
    void setDataCompressionOption(int value)
    throws DBDataStreamException
    {
        addParameter(0x3823, (short)value);
    }



/**
   Sets the Date Format Parser Option parameter in the data stream.
   @param value	the date format.
   @exception DBDataStreamException If there is not enough space left in the data byte array.
**/
    void setDateFormatParserOption (int value)
    throws DBDataStreamException
    {
        addParameter (0x3807, (short) value);
    }



/**
   Sets the Date Separator Parser Option parameter in the data stream.
   @param value	the date separator.
   @exception DBDataStreamException If there is not enough space left in the data byte array.
**/
    void setDateSeparatorParserOption (int value)
    throws DBDataStreamException
    {
        addParameter (0x3808, (short) value);
    }



/**
   Sets the Decimal Separator Parser Option parameter in the data stream.
   @param value	the decimal separator.
   @exception DBDataStreamException If there is not enough space left in the data byte array.
**/
    void setDecimalSeparatorParserOption (int value)
    throws DBDataStreamException
    {
        addParameter (0x380B, (short) value);
    }



/**
   Sets the Default SQL Library Name parameter in the data stream.
   @param value	the qualified library name to use on the
   SQL statement text when no library name is specified in the
   statement text.
   @param converter the converter.
   @exception DBDataStreamException If there is not enough space left in the data byte array.
**/
    void setDefaultSQLLibraryName (String value, ConvTable converter) //@P0C
    throws DBDataStreamException, SQLException                      // @E9c
    {
        addParameter (0x380F, converter, value);
    }



/**
   Sets the DRDA Package Size parameter in the data stream.
   @param value	the DRDA package size.
   @exception DBDataStreamException If there is not enough space left in the data byte array.
**/
    void setDRDAPackageSize (int value)
    throws DBDataStreamException, SQLException                      // @E9c
    {
        addParameter (0x3806, (short) value);
    }



/**
   Sets the Ignore Decimal Data Error Parser Option parameter in the data stream.
   @param value	the value that indicates if decimal data
   errors are to be ignored or not.
   @exception DBDataStreamException If there is not enough space left in the data byte array.
**/
    void setIgnoreDecimalDataErrorParserOption (int value)
    throws DBDataStreamException
    {
        addParameter (0x380D, (short) value);
    }



// @E2C
/**
   Sets the Language Feature Code parameter in the data stream.
   @param value	the server language feature code.
   @exception DBDataStreamException If there is not enough space left in the data byte array.
**/
    void setLanguageFeatureCode(String value)                           // @E2C
    throws DBDataStreamException
    {
        // There is no need to use a Converter for this value              @E2A
        // since it is always numeric.                                     @E2A
        addParameter(0x3802, value);                                    // @E2C
    }



/**
   Sets the LOB Field Threshold parameter in the data stream.
   @param value	the LOB field threshold.
   @exception DBDataStreamException If there is not enough space left in the data byte array.
**/
    void setLOBFieldThreshold (int value)
    throws DBDataStreamException
    {
        addParameter (0x3822, value);
    }



/**
   Sets the Naming Convention Parser Option parameter in the data stream.
   @param value	the naming convention to be used.
   @exception DBDataStreamException If there is not enough space left in the data byte array.
**/
    void setNamingConventionParserOption (int value)
    throws DBDataStreamException
    {
        addParameter (0x380C, (short) value);
    }



/**
   Sets the NLSS indentifier in the data stream.
   @param type	 the NLSS type.
   @param tableFile	 the name of the language table.
   @param tableLibrary  the name of the library containing
   the language table.
   @param languageId  the language id of the NLSS table to use.
   @param converter the converter.
   @exception DBDataStreamException If there is not enough space left in the data byte array.
**/
    void setNLSSortSequence (int type,
                             String tableFile,
                             String tableLibrary,
                             String languageId,
                             ConvTable converter) //@P0C
    throws DBDataStreamException
    {
        addParameter (0x3804, converter, type, tableFile, tableLibrary, languageId);
    }



/**
   Sets the Package Add Statement Allowed parameter in the data stream.
   @param value	the value that indicates whether SQL statements
   should be added to the package, if one is in use.
   @exception DBDataStreamException If there is not enough space left in the data byte array.
**/
    void setPackageAddStatementAllowed(int value)
    throws DBDataStreamException
    {
        addParameter (0x3812, (short) value);
    }


// @J2 new method
/**
   Sets the database (IASP) name for this connection.  The RDB name
   is an 18 byte (blank padded) name.
**/
    void setRDBName(String value, ConvTable converter) //@P0C
    throws DBDataStreamException, SQLException                      // @E9c
    {
        addParameter (0x3826, converter, value, value.length());
    }


// @J1 - added support for ROWID data type
/**
   Sets client support information such as whether we
   support the ROWID type directly or if the host
   should return a VARCHAR FOR BIT DATA instead.
   @parm value of the client support info bitmap
   @exception DBDataStreamException If there is not enough space left in the data byte array.
**/
    void setClientSupportInformation(int value)
    throws DBDataStreamException
    {
        addParameter(0x3825, value);
    }

/**
   Sets the Time Format Parser Option parameter in the data stream.
   @param value	the time format.
   @exception DBDataStreamException If there is not enough space left in the data byte array.
**/
    void setTimeFormatParserOption (int value)
    throws DBDataStreamException
    {
        addParameter (0x3809, (short) value);
    }



/**
   Sets the Time Separator Parser Option parameter in the data stream.
   @param value	the time separator.
   @exception DBDataStreamException If there is not enough space left in the data byte array.
**/
    void setTimeSeparatorParserOption (int value)
    throws DBDataStreamException
    {
        addParameter (0x380A, (short) value);
    }



/**
   Sets the Translate Indicator parameter in the data stream.
   @param value	the value that indicates if the data in the
   operational results set generated by this function should be
   translated to the client's CCSID before the data is returned.
   @exception DBDataStreamException If there is not enough space left in the data byte array.
**/
    void setTranslateIndicator (int value)
    throws DBDataStreamException
    {
        addParameter (0x3805, (byte) value);
    }



/**
   Sets the Use Extended Formats Indicator parameter in the data stream.
   @param value	the value that indicates if the data in the
   operational results set generated by this function should be
   using extended formats.
   @exception DBDataStreamException If there is not enough space left in the data byte array.
**/
    void setUseExtendedFormatsIndicator (int value)
    throws DBDataStreamException
    {
        addParameter (0x3821, (byte) value);
    }

    /**
     * Sets the 63 digit decimal precision parameters in the datastream.
     * @param maximumDecimalPrecision The maximum decimal precision the server should use.
     * @param maximumDecimalScale     The maximum decimal scale the server should use.
     * @param minimumDivideScale      The minimum scale the server should use for decimal division.
     * @exception DBDataStreamException If there is not enough space left in the data byte array.
     **/
    void setDecimalPrecisionIndicators(int maximumDecimalPrecision, int maximumDecimalScale, int minimumDivideScale)
    throws DBDataStreamException
    {
        // build the 6 byte value from its parts
        byte[] value = new byte[6];
        value[0] = (byte)(maximumDecimalPrecision >>> 8);
	value[1] = (byte)maximumDecimalPrecision;
        value[2] = (byte)(maximumDecimalScale >>> 8);
        value[3] = (byte)maximumDecimalScale;
        value[4] = (byte)(minimumDivideScale >>> 8);
        value[5] = (byte)minimumDivideScale;

        addParameter(0x3827, value);
    }

}







