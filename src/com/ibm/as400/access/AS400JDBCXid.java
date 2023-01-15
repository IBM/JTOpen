///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: AS400JDBCXid.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import javax.transaction.xa.Xid;



/**
The AS400JDBCXid class represents an Xid for use with an
<a href="AS400JDBCXAResource.html">AS400JDBCXAResource</a>.
**/
//
// Implementation notes:
//
// Here is the format of the Xid in bytes:
//
// +--------+--------+---------------------------------------------+
// | Offset | Length | Description                                 |
// +--------+--------+---------------------------------------------+
// |    0   |    4   | Format ID                                   |
// |    4   |    4   | Global transaction ID length                |
// |    8   |    4   | Branch qualifier length                     |
// |   12   |    *   | Global transaction ID (up to 64 bytes)      |
// |    *   |    *   | Branch qualifier (up to 64 bytes)           |
// +--------+--------+---------------------------------------------+
// |  140   |        | Maximum length                              |
// +--------+--------+---------------------------------------------+
//
public class AS400JDBCXid
implements Xid
{
  static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";




    // Private data.
    private int     formatId_               = -1;
    private byte[]  globalTransactionId_    = null;
    private byte[]  branchQualifier_        = null;



/**
Constructs an AS400JDBCXid object.

@param xidBytes     The Xid bytes.
@param offset       The offset.
**/
    AS400JDBCXid(byte[] xidBytes, int offset)
    {
        formatId_ = BinaryConverter.byteArrayToInt(xidBytes, offset);
        int globalTransactionIdLength = BinaryConverter.byteArrayToInt(xidBytes, offset + 4);
        int branchQualifierIdLength = BinaryConverter.byteArrayToInt(xidBytes, offset + 8);
        globalTransactionId_ = new byte[globalTransactionIdLength];
        System.arraycopy(xidBytes, offset + 12, globalTransactionId_, 0, globalTransactionIdLength);
        branchQualifier_ = new byte[branchQualifierIdLength];
        System.arraycopy(xidBytes, globalTransactionIdLength + offset + 12, branchQualifier_, 0, branchQualifierIdLength);
    }



/**
Returns the branch qualifier.

@return The branch qualifier.
**/
    public byte[] getBranchQualifier()
    {
        return branchQualifier_;
    }



/**
Returns the format ID.

@return The format ID.
**/
    public int getFormatId()
    {
        return formatId_;
    }



/**
Returns the global transaction ID.

@return The global transaction ID.
**/
    public byte[] getGlobalTransactionId()
    {
        return globalTransactionId_;
    }



/**
Returns the Xid bytes.

@return The Xid bytes.
**/
    byte[] toBytes()
    {
        return xidToBytes(this);
    }



/**
Returns the Xid bytes.

@param xid      The Xid.
@return         The Xid bytes.
**/
    static byte[] xidToBytes(Xid xid)
    {
        int formatId                = xid.getFormatId();
        byte[] globalTransactionId  = xid.getGlobalTransactionId();
        byte[] branchQualifier      = xid.getBranchQualifier();

        byte[] xidBytes = new byte[140];
        BinaryConverter.intToByteArray(formatId, xidBytes, 0);
        BinaryConverter.intToByteArray(globalTransactionId.length, xidBytes, 4);
        BinaryConverter.intToByteArray(branchQualifier.length, xidBytes, 8);
        System.arraycopy(globalTransactionId, 0, xidBytes, 12, globalTransactionId.length);
        System.arraycopy(branchQualifier, 0, xidBytes, globalTransactionId.length + 12, branchQualifier.length);
        return xidBytes;
    }



}
