///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: DBData.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;



/**
The DBData interface describes an overlay structure for
data within a data stream.  All indexes are 0-based.  An
implementation may choose not to implement some methods
if they are not applicable.
**/
interface DBData
extends DBOverlay
{



    public abstract int getConsistencyToken ()
        throws DBDataStreamException;

    public abstract int getRowCount ()
        throws DBDataStreamException;

    public abstract int getColumnCount ()
        throws DBDataStreamException;

    public abstract int getIndicatorSize ()
        throws DBDataStreamException;

    public abstract int getRowSize ()
        throws DBDataStreamException;

    public abstract int getIndicator (int rowIndex, int columnIndex)
        throws DBDataStreamException;

    public abstract int getRowDataOffset (int rowIndex)
        throws DBDataStreamException;

    public abstract byte[] getRawBytes ()
        throws DBDataStreamException;



    public abstract void setConsistencyToken (int consistencyToken)
        throws DBDataStreamException;

    public abstract void setRowCount (int rowCount)
        throws DBDataStreamException;

    public abstract void setColumnCount (int columnCount)
        throws DBDataStreamException;

    public abstract void setIndicatorSize (int indicatorSize)
        throws DBDataStreamException;

    public abstract void setRowSize (int rowSize)
        throws DBDataStreamException;

    public abstract void setIndicator (int rowIndex, int columnIndex, int indicator)
        throws DBDataStreamException;

}
