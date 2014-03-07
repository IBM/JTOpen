///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: NLSExchangeAttrReply.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.InputStream;
import java.io.IOException;

class NLSExchangeAttrReply extends ClientAccessDataStream
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    int primaryRC_=0;            // return code returned by server
    int secondaryRC_=0;          // return code returned by server
    int ccsid_=0;                // host CCSCID

    /* @KDA */ 
    private int dataStreamLevel_;
    private int serverVersion_;
    private int function1Level_;
    private int function2Level_;
    private int function3Level_;
    private int[] ccsidList_;

    NLSExchangeAttrReply()
    {
        super();
    }

    public int getCcsid()
    {
        return ccsid_;
    }

    /*@KDA */ 
    public int getDataStreamLevel() {
      return dataStreamLevel_; 
    }
    public int getServerVersion() {
      return serverVersion_;
    }
    public int getFunction1Level() {
      return function1Level_;
    }
    public int getFunction2Level() {
      return function2Level_;
    }
    public int getFunction3Level() {
      return function3Level_;
    }
    public int[] getCcsidList() {
      return ccsidList_; 
    }

    public Object getNewDataStream()
    {
        return new NLSExchangeAttrReply();
    }

    public int hashCode()
    {
        return 0x1301;  // returns the reply ID
    }

    public int readAfterHeader(InputStream in) throws IOException
    {
        // read in rest of data
        int bytes=super.readAfterHeader(in);
        // get return codes
        primaryRC_ = get16bit(HEADER_LENGTH+2);
        secondaryRC_ = get16bit(HEADER_LENGTH+4);
        dataStreamLevel_ = get16bit(HEADER_LENGTH+6);
        ccsid_ = get32bit(HEADER_LENGTH+8);
        /* Adding new surrogate information @KDA*/ 
        serverVersion_ = get32bit(HEADER_LENGTH+12);
        function1Level_ = get16bit(HEADER_LENGTH+16);
        function2Level_ = get16bit(HEADER_LENGTH+18);
        function3Level_ = get16bit(HEADER_LENGTH+20);
        if (function3Level_ > 0 && (bytes > 22) ) {
           
           int codepoint = get16bit(HEADER_LENGTH+26);
           if (codepoint == 0x8) { // CCSID LIST
             int count = get32bit(HEADER_LENGTH+28);
             ccsidList_ = new int[count]; 
             for (int i = 0; i < count ; i ++) {
               ccsidList_[i] = get32bit(HEADER_LENGTH+32+4*i); 
             }
                 
           }
        }
        // Note: chain,  version, function levels
        // not currently used.
        return bytes;
    }
}
