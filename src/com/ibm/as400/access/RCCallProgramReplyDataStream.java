///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: RCCallProgramReplyDataStream.java
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

class RCCallProgramReplyDataStream extends ClientAccessDataStream
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    Object getNewDataStream()
    {
        return new RCCallProgramReplyDataStream();
    }

    public int hashCode()
    {
        return 0x8003;
    }

    int getRC()
    {
        return get16bit(20);
    }

    AS400Message[] getMessageList(ConverterImplRemote converter)
    {
        return RemoteCommandImplRemote.parseMessages(data_, converter);
    }

    void getParameterList(ProgramParameter[] parameterList)
    {
        // For each output or inout parm, in order, set data returned.
        for (int index = 24, i = 0; i < parameterList.length; ++i)
        {
            if (parameterList[i].getOutputDataLength() > 0)
            {
                int byteLength = BinaryConverter.byteArrayToInt(data_, index);
                int parameterUsage = BinaryConverter.byteArrayToUnsignedShort(data_, index + 10);
                // Copy output data into a new buffer.
                int outputDataLength = BinaryConverter.byteArrayToInt(data_, index + 6);
                byte[] outputData;
                if (parameterUsage == 22 || parameterUsage == 23)
                {
                    outputData = DataStreamCompression.decompressRLE(data_, index + 12, byteLength - 12, outputDataLength, DataStreamCompression.DEFAULT_ESCAPE);
                }
                else
                {
                    outputData = new byte[outputDataLength];
                    System.arraycopy(data_, index + 12, outputData, 0, byteLength - 12);
                }
                parameterList[i].setOutputData(outputData);
                index += byteLength;
            }
        }
    }

    int readAfterHeader(InputStream in) throws IOException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Receiving call program reply...");
        return super.readAfterHeader(in);
    }
}
