///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: DBReplyXids.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;



/**
The DBReplyXids class provides access to the Xids portion
of the reply data stream.
**/
class DBReplyXids
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";



    private byte[]          data_;
    private int             offset_;
    private int             length_;

    private AS400JDBCXid[]  xidArray_;



	public DBReplyXids(byte[] data,
					   int offset,
					   int length)
	{
	    data_ = data;
	    offset_ = offset;
	    length_ = length;
	}



    public AS400JDBCXid[] getXidArray()
        throws DBDataStreamException
    {
        if (xidArray_ == null) {

            if (length_ <= 6)
                xidArray_ = new AS400JDBCXid[0];
            else {
                int count = BinaryConverter.byteArrayToInt(data_, offset_ + 6);
                xidArray_ = new AS400JDBCXid[count];
                for(int i = 0; i < count; ++i)
                    xidArray_[i] = new AS400JDBCXid(data_, offset_ + 18 + count * 140);
            }

        }
        return xidArray_;
    }



}


