///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: DDMEXCSATReplyDataStream.java
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
import java.util.Hashtable;

class DDMEXCSATReplyDataStream extends DDMDataStream
{
    // Check the reply.
    boolean checkReply()
    {
        if (getCodePoint() != DDMTerm.EXCSATRD)
        {
            Trace.log(Trace.ERROR, "DDM EXCSAT failed with code point:", data_, 8, 2);
            String text;
            switch (getCodePoint())
            {  // Some common errors, that might be meaningful to user:
              case DDMTerm.AGNPRMRM : text = "Permanent agent error."; break;
              case DDMTerm.CMDATHRM : text = "Not authorized to command."; break;
              case DDMTerm.CMDCHKRM : text = "Command check."; break;
              case DDMTerm.CMDNSPRM : text = "Command not supported."; break;
              case DDMTerm.PRMNSPRM : text = "Parameter not supported."; break;
              case DDMTerm.RDBAFLRM : text = "RDB access failed reply message."; break;
              case DDMTerm.RDBATHRM : text = "Not authorized to RDB."; break;
              case DDMTerm.SYNTAXRM : text = "Data stream syntax error."; break;
              default:                text = null;
            }
            if (text != null) Trace.log(Trace.ERROR, text);
            return false;
        }
        return true;
    }

    // Parses the returned terms, and returns the value of the EXTNAM term.
    // This is the job identifier for the DDM Host Server job.
    byte[] getEXTNAM()
    {
      byte[] extNam = null;

      int offset = 6;                      // Start after header.
      int streamLL = get16bit(offset);     // LL: Total length of the data stream after the header.
      //int streamCP = get16bit(offset+2); // CP: Code point of reply

      // Assume we've already verified the datastream code point.
      //if (streamCP != DDMTerm.EXCSATRD)
      //{
      //  if (Trace.traceOn_) Trace.log(Trace.WARNING, "Incorrect code point: 0x" + Integer.toHexString(streamCP));
      //  return new byte[0];
      //}

      if (streamLL <= 4)
      {
        if (Trace.traceOn_) Trace.log(Trace.WARNING, "Insufficient reply length: 0x" + Integer.toHexString(streamLL));
        return new byte[0];
      }

      offset += 4;  // get past the reply's LL/CP

      while (!(offset > (data_.length - 4)))
      {
        // Note to maintenance programmer: If you are generalizing this loop logic so as to parse any term in any DDM datastream, be aware that there's special DRDA term-parsing logic in the case where the high-order bit of the LL value is turned on.
        // Assumption: The high-order LL bit will never be turned on in an EXCSAT reply.

        int termLL = get16bit(offset);    // Get LL (length of term)
        int termCP = get16bit(offset+2);  // get CP (code point of this term)

        switch(termCP)
        {
          case DDMTerm.EXTNAM:
            {
              if (termLL > 4)
              {
                extNam = new byte[termLL-4];
                System.arraycopy(data_, offset+4, extNam, 0, extNam.length);
              }
              else {
                if (Trace.traceOn_) Trace.log(Trace.WARNING, "No data returned in EXTNAM term.");
              }
              break;
            }
        }
        offset += termLL;
      }

      return (extNam == null ? new byte[0] : extNam);
    }

    void read(InputStream in) throws IOException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Receiving DDM EXCSAT Reply...");
        // Receive the header.
        byte[] header = new byte[6];
        if (readFromStream(in, header, 0, 6) < 6)
        {
            Trace.log(Trace.ERROR, "Failed to read all of the DDM EXCSAT reply header.");
            throw new ConnectionDroppedException(ConnectionDroppedException.CONNECTION_DROPPED);
        }

        // Allocate bytes for datastream.
        data_ = new byte[BinaryConverter.byteArrayToUnsignedShort(header, 0)];
        System.arraycopy(header, 0, data_, 0, 6);

        // Read in the rest of the data.
        readAfterHeader(in);
    }
}
