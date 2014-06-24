///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  RCCallProgramRequestDataStream.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1999-2007 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.CharConversionException;
import java.io.IOException;
import java.io.OutputStream;

class RCCallProgramRequestDataStream extends ClientAccessDataStream
{
    RCCallProgramRequestDataStream(String library, String program, ProgramParameter[] parameterList, ConverterImplRemote converter, int dataStreamLevel, int messageCount) throws CharConversionException
    {
        int dataStreamLength = 43;  // Data stream length is 43 + length of the parameters.

        // Compress parameters and calculate length of datastream.
        //@L10A START
        for (int i = 0; i < parameterList.length; ++i)
        {
            byte[] inputData = parameterList[i].getInputData();

            int parameterLength = 0;
            int parameterMaxLength = parameterList[i].getMaxLength();
            int parameterUsage = parameterList[i].getUsage();
            byte[] compressedInputData = null;

            if (parameterUsage == ProgramParameter.OUTPUT)
            {
                parameterUsage += 20;
            }
            else
            {
                if (inputData == null) inputData = new byte[0];
                if (parameterMaxLength > 1024)
                {
                    byte[] tempInputData;
                    if (inputData.length == parameterMaxLength)
                    {
                        tempInputData = inputData;
                    }
                    else
                    {
                        tempInputData = new byte[parameterMaxLength];
                        System.arraycopy(inputData, 0, tempInputData, 0, inputData.length);
                    }
                    compressedInputData = DataStreamCompression.compressRLE(tempInputData, 0, tempInputData.length, DataStreamCompression.DEFAULT_ESCAPE);
                    if (compressedInputData != null)
                    {
                        parameterLength = compressedInputData.length;
                        parameterUsage += 20;
                    }
                }
            }
            if (parameterUsage < 20)
            {
                for (parameterLength = inputData.length; parameterLength >= 1 && inputData[parameterLength - 1] == 0; --parameterLength);
                compressedInputData = inputData;
                if (parameterUsage == ProgramParameter.INOUT && dataStreamLevel >= 5)
                {
                    // Server allows 33 value.
                    parameterUsage += 30;
                }
                else
                {
                    parameterUsage += 10;
                }
            }
            dataStreamLength +=  12 + parameterLength;
            parameterList[i].length_ = parameterLength;
            parameterList[i].maxLength_ = parameterMaxLength;
            parameterList[i].usage_ = parameterUsage;
            parameterList[i].compressedInputData_ = compressedInputData;
        }
        //@L10A END
        
        // Initialize header.
        data_ = new byte[dataStreamLength];
        setLength(dataStreamLength);
        // setHeaderID(0x0000);
        setServerID(0xE008);
        // setCSInstance(0x00000000);
        // setCorrelation(0x00000000);
        setTemplateLen(23);
        setReqRepID(0x1003);

        // Blank fill program name and library name.
        for (int i = 0; i < 20; ++i)
        {
            data_[20 + i] = (byte)0x40;
        }

        converter.stringToByteArray(program, data_, 20);
        converter.stringToByteArray(library, data_, 30);

        // Return messages.
        if (dataStreamLevel < 7 && messageCount == AS400Message.MESSAGE_OPTION_ALL) messageCount = AS400Message.MESSAGE_OPTION_UP_TO_10;
        //@J4 - Start
        if (dataStreamLevel >= 10 && dataStreamLevel < 11)
        {
            if (messageCount == AS400Message.MESSAGE_OPTION_UP_TO_10) messageCount = 3;
            if (messageCount == AS400Message.MESSAGE_OPTION_ALL) messageCount = 4;
        }
        if (dataStreamLevel >= 11) {
          if (messageCount == AS400Message.MESSAGE_OPTION_UP_TO_10) messageCount = 5;
          if (messageCount == AS400Message.MESSAGE_OPTION_ALL) messageCount = 6;
        }
        //@J4 - End
        data_[40] = (byte)messageCount;

        // Set number of program parameters.
        set16bit(parameterList.length, 41);

        // Now convert the parameter list into data stream.
        for (int index = 43, i = 0; i < parameterList.length; ++i) // Start at 43 in data_
        {
            int usage = parameterList[i].usage_;//@L10C
            int parameterLength = parameterList[i].length_;
            // Set LL for this parameter.
            set32bit(parameterLength + 12, index);
            // Set CP for parameter.
            set16bit(0x1103, index + 4);
            // Set parameter data length.
            set32bit(parameterList[i].getMaxLength(), index + 6);
            // Set parameter usage.
            if (usage == ProgramParameter.NULL)
            {
                if (dataStreamLevel < 6)
                {
                    // Server does not allow null parameters.
                    set16bit(ProgramParameter.INPUT, index + 10);
                }
                else
                {
                    set16bit(usage, index + 10);
                }
            }
            //@L5A start
            /*else if (usage == ProgramParameter.OUTPUT)    
            {   
                usage += 20;   
                parameterList[i].usage_ = usage;
                set16bit(usage, index + 10);
            }*/
            //@L5A end
            else
            {
                set16bit(usage, index + 10); //@L10C
                // Write the input data into the data stream.
                if (parameterLength > 0)
                {
                  System.arraycopy(parameterList[i].compressedInputData_, 0, data_, index + 12, parameterLength);//@L10C
                }
            }

            // Advance 12 + parameter length in data stream.
            index += 12 + parameterLength;
        }
    }

    void write(OutputStream out) throws IOException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Sending call program request...");
        super.write(out);
    }
}
