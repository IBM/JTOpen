///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
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

    RCCallProgramRequestDataStream(String library, String program, ProgramParameter[] parameterList, ConverterImplRemote converter, boolean zeroSuppression) throws CharConversionException
    {
        int dataStreamLength = 43;  // Data stream length is 43 + length of the parameters.

        // Figure out how much to allocate first.
        for (int i = 0; i < parameterList.length; ++i)
        {
            int parameterLength = 0;
            int parameterType = parameterList[i].getType();
            if (parameterType != ProgramParameter.OUTPUT)
            {
                parameterLength = parameterList[i].getInputData().length;
            }
            if (parameterType == ProgramParameter.INOUT)
            {
                int outParameterLength = parameterList[i].getOutputDataLength();
                if (parameterLength < outParameterLength);
                {
                    parameterLength = outParameterLength;
                }
            }
            dataStreamLength +=  12 + parameterLength;
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

        // Do not suppress messages.
        data_[40] = 0x00;

        // Set number of program parameters.
        set16bit(parameterList.length, 41);

        // Now convert the parameter list into data stream.
        for (int index = 43, i = 0; i < parameterList.length; ++i) // Start at 43 in data_
        {
            // Start input data length at zero.
            int inputDataLength = 0;
            // Get output data length, will be zero for input parameters.
            int outputDataLength = parameterList[i].getOutputDataLength();
            // Get the parameter type, INPUT, OUTPUT, or INOUT.
            int parameterType = parameterList[i].getType();
            // If there is input data.
            if (parameterType != ProgramParameter.OUTPUT)
            {
                // Get the input data.
                byte[] inputData = parameterList[i].getInputData();
                // Set the true input data length, OUTPUT parameters will see this value as zero.
                inputDataLength = inputData.length;
                // Write the input data into the data stream.
                System.arraycopy(inputData, 0, data_, index + 12, inputDataLength);
            }

            // Find the parameter length, INPUT - input length, OUTPUT - zero, INOUT - max length.
            int parameterLength = (parameterType != ProgramParameter.INOUT || inputDataLength >= outputDataLength) ? inputDataLength : outputDataLength;
            // Find the max length.
            int maxLength = (inputDataLength >= outputDataLength) ? inputDataLength : outputDataLength;

            // Set LL for this parameter.
            set32bit(parameterLength + 12, index);
            // Set CP for parameter.
            set16bit(0x1103, index + 4);
            // Set parameter data length.
            set32bit(maxLength, index + 6);

            // If zero suppression is allowed and type is not input.
            if (zeroSuppression && parameterType != ProgramParameter.INPUT)
            {
                // Adding 10 to the type tells the server to zero suppress the returned value.
                parameterType += 10;
            }
            // Set parameter usage.
            set16bit(parameterType, index + 10);

            // Advance 12 + parameter length in data stream.
            index += 12 + parameterLength;
        }
    }

    void write(OutputStream out) throws IOException
    {
        Trace.log(Trace.DIAGNOSTIC, "Sending call program request...");
        super.write(out);
    }
}
