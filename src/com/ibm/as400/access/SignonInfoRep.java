///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: SignonInfoRep.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.io.InputStream;
import java.util.GregorianCalendar;

class SignonInfoRep extends ClientAccessDataStream
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

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
        int index = 24;
        GregorianCalendar date = null;

        while (index < (data_.length - 1))
        {
            if (get16bit(index + 4) != cp)
            {
                index += get32bit(index);
            }
            else
            {
                date = new GregorianCalendar((int)(get16bit(index+6))/*year*/, (int)(data_[index+8] - 1)/*month convert to zero based*/, (int)(data_[index+9])/*day*/, (int)(data_[index+10])/*hour*/, (int)(data_[index+11])/*minute*/, (int)(data_[index+12])/*second*/);
                break;
            }
        }

        return date;
    }

    int getUnsuccessfulAttempts()
    {
        return getAttempt(0x1109);
    }

    int getMaxUnsuccessful()
    {
        return getAttempt(0x110A);
    }

    int getAttempt(int cp)
    {
        int index = 24;
        int attempt = 0;

        while (index < (data_.length - 1))
        {
            if (get16bit(index + 4) != cp)
            {
                index = index + get32bit(index);
            }
            else
            {
                attempt = get16bit(index + 6);
                break;
            }
        }

        return attempt;
    }

    int getServerCCSID()
    {
        int index = 24;
        int ccsid = 0;

        while (index < (data_.length - 1))
        {
            if (get16bit(index + 4) != 0x1114)
            {
                index = index + get32bit(index);
            }
            else
            {
                ccsid = get32bit(index + 6);
                break;
            }
        }

        return ccsid;
    }

    void read(InputStream in) throws IOException
    {
        Trace.log(Trace.DIAGNOSTIC, "Receiving retrieve signon information reply...");

        // Receive the header.
        byte[] header = new byte[20];
        if (DataStream.readFromStream(in, header, 0, 20) < 20)
        {
            Trace.log(Trace.ERROR, "Failed to read all of the retrieve signon information reply header.");
            throw new ConnectionDroppedException(ConnectionDroppedException.CONNECTION_DROPPED);
        }

        // Allocate bytes for datastream.
        data_ = new byte[BinaryConverter.byteArrayToInt(header, 0)];
        System.arraycopy(header, 0, data_, 0, 20);

        // Read in the rest of the data.
        readAfterHeader(in);
    }
}
