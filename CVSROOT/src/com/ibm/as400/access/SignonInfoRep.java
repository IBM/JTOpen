///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  SignonInfoRep.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2007 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.io.InputStream;
import java.util.GregorianCalendar;

class SignonInfoRep extends ClientAccessDataStream
{
    /**
     Generate a new instance of this type.
     @return a reference to the new instance
     **/
    public Object getNewDataStream()
    {
      return new SignonInfoRep();
    }

    int getRC()
    {
        return get32bit(20);
    }

    GregorianCalendar getCurrentSignonDate()
    {
        return getDate(0x1106);
    }

    GregorianCalendar getLastSignonDate()
    {
        return getDate(0x1107);
    }

    GregorianCalendar getExpirationDate()
    {
        return getDate(0x1108);
    }

    GregorianCalendar getDate(int cp)
    {
        int offset = 24;
        GregorianCalendar date = null;

        while (offset < (data_.length - 1))
        {
            if (get16bit(offset + 4) != cp)
            {
                offset += get32bit(offset);
            }
            else
            {
                date = new GregorianCalendar((int)(get16bit(offset+6))/*year*/, (int)(data_[offset+8] - 1)/*month convert to zero based*/, (int)(data_[offset+9])/*day*/, (int)(data_[offset+10])/*hour*/, (int)(data_[offset+11])/*minute*/, (int)(data_[offset+12])/*second*/);
                break;
            }
        }

        return date;
    }

    int getServerCCSID()
    {
        int offset = 24;
        int ccsid = 0;

        while (offset < (data_.length - 1))
        {
            if (get16bit(offset + 4) != 0x1114)
            {
                offset = offset + get32bit(offset);
            }
            else
            {
                ccsid = get32bit(offset + 6);
                break;
            }
        }

        return ccsid;
    }

    byte[] getUserIdBytes()
    {
        int offset = 24;
        while (offset < (data_.length - 1))
        {
            if (get16bit(offset + 4) != 0x1104)
            {
                offset += get32bit(offset);
            }
            else
            {
                byte[] userIdBytes = {(byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40};
                System.arraycopy(data_, offset + 10, userIdBytes, 0, get32bit(offset) - 10);
                return userIdBytes;
            }
        }

        return null;
    }

    AS400Message[] getErrorMessages(ConverterImplRemote converter) throws IOException
    {
        return AS400ImplRemote.parseMessages(data_, 24, converter);
    }

    void read(InputStream in) throws IOException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Receiving retrieve signon information reply...");

        // Receive the header.
        byte[] header = new byte[20];
        if (readFromStream(in, header, 0, 20) < 20)
        {
            if (Trace.traceOn_) Trace.log(Trace.ERROR, "Failed to read all of the retrieve signon information reply header.");
            throw new ConnectionDroppedException(ConnectionDroppedException.CONNECTION_DROPPED);
        }

        // Allocate bytes for datastream.
        data_ = new byte[BinaryConverter.byteArrayToInt(header, 0)];
        System.arraycopy(header, 0, data_, 0, 20);

        // Read in the rest of the data.
        readAfterHeader(in);
    }

    /**
     Generates a hash code for this data stream.
     @return the hash code
     **/
    public int hashCode()
    {
      return 0xF004;
    }
}
