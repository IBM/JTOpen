///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: DBDataFormat.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;



/**
The DBDataFormat interface describes an overlay structure for
a data format within a data stream.  All indexes are 0-based.
An implementation may choose not to implement some methods
if they are not applicable.
**/
interface DBDataFormat
extends DBOverlay
{



    public abstract int getConsistencyToken ()
        throws DBDataStreamException;
    
    public abstract int getDateFormat()			// @550A
    	throws DBDataStreamException;			// @550A
    
    public abstract int getTimeFormat()			// @550A
		throws DBDataStreamException;			// @550A
    
    public abstract int getDateSeparator()		// @550A
		throws DBDataStreamException;			// @550A
    
    public abstract int getTimeSeparator()		// @550A
		throws DBDataStreamException;			// @550A
    
    public abstract boolean getCSRSData();		// @550A

    public abstract int getNumberOfFields ()
        throws DBDataStreamException;

    public abstract int getRecordSize ()
        throws DBDataStreamException;

    public abstract int getFieldSQLType (int fieldIndex)
        throws DBDataStreamException;

    public abstract int getFieldLength (int fieldIndex)
        throws DBDataStreamException;

    public abstract int getFieldScale (int fieldIndex)
        throws DBDataStreamException;

    public abstract int getFieldPrecision (int fieldIndex)
        throws DBDataStreamException;

    public abstract int getFieldCCSID (int fieldIndex)
        throws DBDataStreamException;

    public abstract int getFieldParameterType (int fieldIndex)
        throws DBDataStreamException;

    public abstract int getFieldLOBLocator (int fieldIndex)     // @A1A
        throws DBDataStreamException;                           // @A1A

    public abstract int getFieldLOBMaxSize (int fieldIndex)     // @A1A
        throws DBDataStreamException;                           // @A1A

    public abstract int getFieldNameLength (int fieldIndex)
        throws DBDataStreamException;

    public abstract int getFieldNameCCSID (int fieldIndex)
        throws DBDataStreamException;

    public abstract String getFieldName (int fieldIndex, ConvTable converter) //@P0C
        throws DBDataStreamException;



    public abstract void setConsistencyToken (int consistencyToken)
        throws DBDataStreamException;
    
    public abstract void setCSRSData(boolean csRsData);
    
    public abstract void setNumberOfFields (int numberOfFields)
        throws DBDataStreamException;

    public abstract void setRecordSize (int recordSize)
        throws DBDataStreamException;

    public abstract void setFieldDescriptionLength (int fieldIndex)
        throws DBDataStreamException;

    public abstract void setFieldSQLType (int fieldIndex, int sqlType)
        throws DBDataStreamException;

    public abstract void setFieldLength (int fieldIndex, int length)
        throws DBDataStreamException;

    public abstract void setFieldScale (int fieldIndex, int scale)
        throws DBDataStreamException;

    public abstract void setFieldPrecision (int fieldIndex, int precision)
        throws DBDataStreamException;

    public abstract void setFieldCCSID (int fieldIndex, int ccsid)
        throws DBDataStreamException;

    public abstract void setFieldParameterType (int fieldIndex, int parameterType)
        throws DBDataStreamException;

    public abstract void setFieldNameLength (int fieldIndex, int nameLength)
        throws DBDataStreamException;

    public abstract void setFieldNameCCSID (int fieldIndex, int nameCCSID)
        throws DBDataStreamException;

    public abstract void setFieldName (int fieldIndex, String name, ConvTable converter) //@P0C
        throws DBDataStreamException;

}
