///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: RCCallProgramRequestDataStream.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.CharConversionException;
import java.io.IOException;
import java.io.OutputStream;

class RCCallProgramRequestDataStream extends ClientAccessDataStream
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    RCCallProgramRequestDataStream(String library, String program, ProgramParameter[] parameterList, ConverterImplRemote converter, int dataStreamLevel) throws CharConversionException
    {
        int dataStreamLength = 43;  // Data stream length is 43 + length of the parameters.

        // Compress parameters and calculate length of datastream.
        for (int i = 0; i < parameterList.length; ++i)
        {
            byte[] inputData = parameterList[i].getInputData();

            int parameterLength = 0;
            int parameterMaxLength = parameterList[i].getMaxLength();
            int parameterUsage = parameterList[i].getUsage();
            byte[] compressedInputData = null;

            if (dataStreamLevel >= 3)  // Server allows RLE.
            {
                if (parameterUsage == ProgramParameter.OUTPUT)
                {
                    parameterUsage += 20;
                }
                else
                {
                    if (parameterMaxLength > 1024)
                    {
                        byte[] tempInputData;
                        if (parameterUsage == ProgramParameter.INPUT || inputData.length == parameterMaxLength)
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
            }
            if (parameterUsage < 20)
            {
                if (parameterUsage != ProgramParameter.OUTPUT)
                {
                    for (parameterLength = inputData.length; parameterLength >= 1 && inputData[parameterLength - 1] == 0; --parameterLength);
                    compressedInputData = inputData;
                }
                if (parameterUsage == ProgramParameter.INOUT && dataStreamLevel >= 5)  // Server allows 33 value.
                {
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
        // data_[40] = 0x00;

        // Set number of program parameters.
        set16bit(parameterList.length, 41);

        // Now convert the parameter list into data stream.
        for (int index = 43, i = 0; i < parameterList.length; ++i) // Start at 43 in data_
        {
            // Set LL for this parameter.
            set32bit(parameterList[i].length_ + 12, index);
            // Set CP for parameter.
            set16bit(0x1103, index + 4);
            // Set parameter data length.
            set32bit(parameterList[i].maxLength_, index + 6);
            // Set parameter usage.
            set16bit(parameterList[i].usage_, index + 10);
            // Write the input data into the data stream.
            switch (parameterList[i].usage_)
            {
                case 12:
                case 22:
                    break;
                default:
                    System.arraycopy(parameterList[i].compressedInputData_, 0, data_, index + 12, parameterList[i].length_);
            }

            // Advance 12 + parameter length in data stream.
            index += 12 + parameterList[i].length_;
        }
    }

    void write(OutputStream out) throws IOException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Sending call program request...");
        super.write(out);
    }
}
