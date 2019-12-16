///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PrintObjectTransformedInputStreamImplRemote.java
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


// NOTE: This class is derived directly from PrintObjectPageInputStream.

/**
The PrintObjectTransformedInputStream class is used to read transformed data
from a server spooled file.  The type of transform to be performed on the data
is dependent on the
<a href="PrintParameterList.html">PrintParameterList</a>
used to create an instance of the class.
<p>
An instance of this class is created using
the getTransformedInputStream method
from the class <a href="SpooledFile.html">SpooledFile</a>.
<p>
NOTE: This class is supported on OS/400 V4R4 or later.
Not all spooled file formats are supported for transform.
**/

class PrintObjectTransformedInputStreamImplRemote
implements PrintObjectTransformedInputStreamImpl
{
    // Private data
    private NPConversation  conversation_;       // conversation with Network Print Server
    private NPCPAttribute   cpCPFMsg_;           // CPF message code point
    private NPCodePoint     cpObjHndl_;          // input stream handle code point
    private NPCPID          cpObjID_;            // input stream ID codepoint
    private NPSystem        npSystem_;           // AS400 system where input stream resides
    private int             numBytes_ = 0;       // total size of data in inputstream
    private int             objectType_ ;        // object type (SpooledFile)
    private int             offset_ = 0;         // offset from beginning of file (in bytes)
    private boolean         cidConv = false;     // add second level ASCII conversion
    private String          convSource = null;   // holds conversion table source values
    private String          convTarget = null;   // holds conversion target values


/**
Constructs a PrintObjectTransformedInputStream object. The PrintParameterList attribute
ATTR_MFGTYPE must be specified to indicate the type of data transform.

@param  spooledFile The SpooledFile.
@param  transformOptions The PrintParameterList options to be used when opening the SpooledFile.

@exception AS400Exception If the system returns an error message.
@exception AS400SecurityException If a security or authority error occurs.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception IOException If an error occurs while communicating with the server.
@exception InterruptedException If this thread is interrupted.
@exception RequestNotSupportedException If the requested function is not supported because the server
           operating system is not at the correct level.
**/
    public synchronized void createPrintObjectTransformedInputStream(SpooledFileImpl spooledFile,
                                                 PrintParameterList transformOptions)
        throws AS400Exception,
               AS400SecurityException,
               ErrorCompletingRequestException,
               IOException,
               InterruptedException,
               RequestNotSupportedException
    {
        AS400ImplRemote system = ((SpooledFileImplRemote)spooledFile).getSystem();
        int ccsid = system.getCcsid();

        objectType_ = NPConstants.SPOOLED_FILE;
        npSystem_   = NPSystem.getSystem(((SpooledFileImplRemote)spooledFile).getSystem());
        cpObjID_    = ((SpooledFileImplRemote)spooledFile).getIDCodePoint();
        cpCPFMsg_   = new NPCPAttribute();
        cpObjHndl_  = new NPCPSplFHandle();
        
        // set up OPEN request datastream
        NPDataStream openReq = new NPDataStream(objectType_);
        openReq.setAction(NPDataStream.OPEN_MODIFIED_SPLF);
        openReq.addCodePoint(cpObjID_);

        // create the Selection Code Point
        NPCPSelection selectionCP = new NPCPSelection();

        // set any options the user passed in,
        // (The transformOptions is supposed to contain manufacture type and
        //  model, and/or a workstation customizing object necessary for
        //  specifying the type of transform to perform; otherwise,
        //  why create an instance of this class?)
        if (transformOptions != null) {
            selectionCP.addUpdateAttributes(transformOptions.getAttrCodePoint());
            // Get the value of the target code page attribute
            String tempTarget = transformOptions.getStringParameter(PrintObject.ATTR_TGT_CODEPAGE);
            // Check for the setting of the client/target code page attribute.
            // If it exist then the source ASCII code page can be determined.
            if (tempTarget != null){
                cidConv = true;
                // prepare the 'CPxxxx' (where xxxx is the actual code page) to be 'Cpxxx'
                convTarget = tempTarget.replace('P', 'p');
                /* table of target ASCII CCSIDs can be found in the "IBM i Workstation 
                   Customization Reference" that can be found on the IBM i Information Center. */
                switch (ccsid) //    @B2A  "Euro Phase 2 = EP2 "
                {
                case 37:                       /* US           @B2A */
                case 836:                      /* Simplified   @B2A */
                    convSource = "Cp437";  /* 8bit ASCII US PC @B2A */      
                    break;
                case 420:                      /* Arabic       @B2A */
                    convSource = "Cp864";  /* 8bit ASCII Arabic@B2A */
                    break;
                case 423:                      /* Greece (old) @B2A */
                    convSource = "Cp1253"; /* MS-Win Greek     @B2A */
                    break;
                case 424:                      /* Hebrew       @B2A */
                    convSource = "Cp856";  /* 8bit ASCII Hebrew@B2A */
                    break;
                case 838:                      /* Thai         @B2A */
                case 1130:                     /* Vietnamese   @B2A */
                case 1132:                     /* Lao          @B2A */
                case 1164:                     /* Viet Nam EP2 @B2A */
                    convSource = "Cp874";/* 8bit ASCII Thailand@B2A */
                    break;
                case 870:                      /* Latin 2      @B2A */
                    convSource = "Cp852";    /* 8bit Latin-2   @B2A */
                    break;
                case 875:                      /* Greece (new) @B2A */
                    convSource = "Cp869";/* 8bit ASCII Greek   @B2A */
                    break;
                case 905:                      /* Turkey (old) @B2A */
                case 1026:                     /* Turkey (new) @B2A */
                    convSource = "Cp857";/* 8bit ASCII Latin-5 @B2A */
                    break;
                case 1097:                     /* Farsi (new)  @B2A */
                    convSource = "Cp1097";/*IBM EBCDIC Farsi   @B2A */
                    break;
                case 1112:                     /* Latvian, Lith@B2A */
                    convSource = "Cp921";/* 8bit ASCII Baltic  @B2A */
                    break;
                case 1122:                     /* Estonia      @B2A */
                    convSource = "Cp922";      /* 8bit Estonia @B2A */      
                    break;
                case 1153:           /* Czech, Poland, EP2     @B2A */
                    convSource = "Cp1250";    /* MS-Win Latin-2@B@A */   
                    break;
                case 1154:                     /* Bulgarian EP2@B2A */
                case 1158:                     /* Ukraine   EP2@B2A */
                    convSource = "Cp1251";/* MS-Win Cyrillic   @B2A */
                    break;
                case 1155:                     /* Turkey  EP2  @B2A */
                    convSource = "Cp1254";/* MS-Win Turkish    @B2A */
                    break;
                case 1156:                     /* Latvia EP2   @B2A */
                case 1157:                     /* Estonia EP2  @B2A */
                    convSource = "Cp1257";/* MS-Win Balic      @B2A */
                    break;
                case 1160:                     /* Thailand EP2 @B2A */
                    convSource = "Cp874";/* 8bit ASCII Thailand@B2A */
                    break;
                case 5026:            /* Japanese Ext Katakana @B2A */
                case 1390:        /* Japanese new Ext Katakana @B2A */
                case 5035:        /* Japanese Ext Latin        @B2A */
                case 1399:        /* Japanese new Ext Latin    @B2A */
                    convSource = "Cp942";/* 8bit ASCII Japanese@B2A */
                    break;
                case 933:                      /* Korean       @B2A */
                case 1364:                     /* Korean (new) @B2A */
                    convSource = "Cp949";/* 8bit ASCII Korean  @B2A */
                    break;
                case 937:                      /* T-Chinese    @B2A */
                case 1371:                  /* T-Chinese (new) @B2A */
                case 1388:                  /* S-Chinese (new) @B2A */
                    convSource = "Cp950";/* 8bit ASCII T-Chinese@B2A*/
                    break;
                case 935:                      /* S-Chinese    @B2A */
                    convSource = "Cp1381";/* 8bit ASCII S-Chinese@B2A*/
                    break;
                                    
                default: convSource = "Cp850";
                }
                
                transformOptions.setParameter(PrintObject.ATTR_SRC_CODEPAGE, convSource);
            }//    @B2A
        }

        // add the selection codepoint to the open request datastream
        openReq.addCodePoint(selectionCP);

        // setup OPEN reply datastream
        NPDataStream openRep = new NPDataStream(objectType_);
        openRep.addCodePoint(cpObjHndl_);
        openRep.addCodePoint(cpCPFMsg_);

        // try to open the spooled file
        conversation_   = npSystem_.getConversation();
        boolean fOpenOK = false;
        try {
            // make the OPEN request
            int rc = conversation_.makeRequest(openReq, openRep);

            if (rc != NPDataStream.RET_OK) {   // failed
                Trace.log(Trace.ERROR, "Error opening SpooledFile; rc = " + rc);
                throw new ErrorCompletingRequestException(ErrorCompletingRequestException.AS400_ERROR);
            }
            else {
                // try to get the number of bytes in the spooled file
                // it will throw an exception if there is any error
                retrieveNumberOfBytes();
                fOpenOK = true;
            }
        }
        finally {
            // if we got here because an exception was thrown
            if (!fOpenOK) {
                if (npSystem_ != null) {
                    npSystem_.returnConversation(conversation_);
                }
                conversation_ = null;
            }
        }
    }



/**
Returns the number of bytes available (with blocking).

@return  The number of available bytes (with blocking).
**/
    public int available() throws IOException
    {
        return numBytes_ - offset_;
    }



/**
Closes the input stream and releases any resources associated with it.

@exception IOException If an error occurs while communicating with the server.
**/
    public void close() throws IOException
    {
        if (conversation_ == null) {
            Trace.log(Trace.ERROR, "Conversation is null.");
            throw new IOException();
        }
        else {
            // set up CLOSE request datastream
            NPDataStream closeReq = new NPDataStream(objectType_);
            closeReq.setAction(NPDataStream.CLOSE);
            closeReq.addCodePoint(cpObjHndl_);

            // set up CLOSE reply datastream
            NPDataStream closeRep = new NPDataStream(objectType_);
            closeRep.addCodePoint(cpCPFMsg_);

            try {
               // make the CLOSE request
               int rc = conversation_.makeRequest(closeReq, closeRep);

               if (rc != NPDataStream.RET_OK) {  // failed
                   Trace.log(Trace.ERROR, "Error closing SpooledFile; rc = " + rc);
                   npSystem_.returnConversation(conversation_);
               }
            }
            catch (Exception e) {
               Trace.log(Trace.ERROR, "Caught an Exception." + e.toString());
               throw new IOException(e.toString());
            }

            finally {
                if (npSystem_ != null) {
                    npSystem_.returnConversation(conversation_);
                    npSystem_ = null;
                }
                conversation_ = null;
            }
        }
    }



/**
Closes the stream when garbage is collected.

@exception Throwable If an error occurs.
**/
    protected void finalize() throws Throwable
    {
        if (conversation_ != null) {
            // attempt to send the close() request and then
            // return the conversation to the pool...
            // Ignore any replies here to avoid a deadlock
            // if we are called on the AS400Server's background thread.
            NPDataStream closeReq = new NPDataStream(objectType_);
            closeReq.setAction(NPDataStream.CLOSE);
            closeReq.addCodePoint(cpObjHndl_);

            AS400Server server= conversation_.getServer();
            if (server != null) {
                // close the input stream
                // @B1D closeReq.setHostCCSID(conversation_.getHostCCSID());
                closeReq.setConverter(conversation_.getConverter());
                server.sendAndDiscardReply(closeReq);
            }

            if (npSystem_ != null) {
                npSystem_.returnConversation(conversation_);
                npSystem_ = null;
            }
            conversation_ = null;
        }
        super.finalize();   // always call super.finalize()!
    }



/**
Reads up to <i>length</i> bytes of data from this input stream into <i>data</i>,
starting at the array offset <i>dataOffset</i>.

@param data The buffer into which the data is read.
@param dataOffset The start offset of the data.
@param length The maximum number of bytes to read.

@return The total number of bytes read into the buffer, or -1 if there is
no more data because the end of file has been reached.

@exception IOException If an error occurs while communicating with the server.
**/
    public int read(byte data[], int dataOffset, int length) throws IOException
    {
        int bytesRead = 0;                               
        int bytesToRead = 0;
        Integer sizeTarget = new Integer(data.length);
        
        byte dataSource[] = new byte[length];
        if (conversation_ == null) {
            Trace.log(Trace.ERROR, "Conversation is null.");
            throw new IOException();
        }
        else {
            // set the number of bytes to read
            NPCPAttribute cpAttr = new NPCPAttribute();
            cpAttr.setAttrValue(PrintObject.ATTR_NUMBYTES, length);

            // set up READ request datastream
            NPDataStream readReq = new NPDataStream(objectType_);
            readReq.setAction(NPDataStream.READ);
            readReq.addCodePoint(cpObjHndl_);
            readReq.addCodePoint(cpAttr);

            // Point the data codepoint to receive the data into our buffer.
            // Our buffer better be big enough to hold the data or we''ll
            // not get it at all
            NPCPData cpData = new NPCPData();
            cpData.setDataBuffer(data, 0, dataOffset);

            // set up READ reply datastream
            NPDataStream readRep = new NPDataStream(objectType_);
            readRep.addCodePoint(cpData);
            readRep.addCodePoint(cpCPFMsg_);

            try {
                // make the READ request
                int iRC = conversation_.makeRequest(readReq, readRep);
                // cidConv and convTarget are checked/initialized w/in
                // createPrintObjectTransformedInputStream and indicate
                // A second ASCII conversion required
                if (cidConv){ //  begin
                    String convString = new String(data, dataOffset, length, convSource);
                    dataSource = convString.getBytes(convTarget);
                    Integer sizeSource = new Integer(dataSource.length);
                    if (sizeSource.compareTo(sizeTarget) >= 0){
                        bytesToRead = sizeTarget.intValue();
                    }
                    else { 
                        bytesToRead = sizeSource.intValue();
                    }
                    System.arraycopy(dataSource, 0, data, 0, bytesToRead);
                } //              end

                switch (iRC) {
                    case NPDataStream.RET_OK:
                    case NPDataStream.RET_READ_INCOMPLETE:   // maybe read some bytes?
                        // see how many bytes we read
                        bytesRead = cpData.getDataLength();
                        offset_ += bytesRead;              // update how far we are from the start of block
                        if (readRep.get32bit(NPDataStream.FLAGS_OFFSET) == 0x20000000) {
                            retrieveNumberOfBytes();
                            offset_ = 0;
                        }
                        break;
                    case NPDataStream.RET_READ_EOF:
                        // this return code is only returned if there are no
                        // bytes read at all
                        bytesRead = -1;   // set rc to end of file
                        break;
                    default:
                        // log an error throw appropriate exception
                        Trace.log(Trace.ERROR, "Error received on read : " + Integer.toString(iRC));
                        throw new IOException(Integer.toString(iRC));
                }
            }

            catch (Exception e) {
                Trace.log(Trace.ERROR, "Caught an Exception." + e.toString());
                throw new IOException(e.toString());
            }
        }
       return bytesRead;
    }



/**
Retrieves the number of bytes of transformed data available in the stream.

@return The number of bytes of transformed data available in the stream.
**/
    private void retrieveNumberOfBytes()
       throws IOException,
              RequestNotSupportedException

    {
        // set up attributes to retrieve ID list
        NPCPAttributeIDList cpAttrsToRetrieve = new NPCPAttributeIDList();
        cpAttrsToRetrieve.addAttrID(PrintObject.ATTR_NUMBYTES);

        // set up TELL request datastream
        NPDataStream tellReq = new NPDataStream(objectType_);
        tellReq.addCodePoint(cpObjHndl_);
        tellReq.addCodePoint(cpAttrsToRetrieve);
        tellReq.setAction(NPDataStream.TELL);

        // set up TELL reply datastream
        NPDataStream tellRep = new NPDataStream(objectType_);
        NPCPAttribute cpAttrs = new NPCPAttribute();
        tellRep.addCodePoint(cpAttrs);

        try {
            // make TELL request
            int rc = conversation_.makeRequest(tellReq, tellRep);
            if (rc == NPDataStream.RET_OK) {
                Integer numBytes = cpAttrs.getIntValue(PrintObject.ATTR_NUMBYTES);
                if (numBytes != null) {
                    numBytes_ = numBytes.intValue();
                }
                else {
                    Trace.log(Trace.ERROR,
                        "Network Print Server does not support retrieving data block length.");
                    throw new RequestNotSupportedException(
                              conversation_.getAttribute(PrintObject.ATTR_NPSLEVEL),
                              RequestNotSupportedException.SYSTEM_LEVEL_NOT_CORRECT);
                }
            }
            else {
                Trace.log(Trace.ERROR,
                    "Network Print Server error retrieving data block length. RC ="+ rc);
                throw new RequestNotSupportedException(
                          conversation_.getAttribute(PrintObject.ATTR_NPSLEVEL),
                          RequestNotSupportedException.SYSTEM_LEVEL_NOT_CORRECT);
            }
        }
        catch (Exception e) {
               Trace.log(Trace.ERROR, "Caught an Exception." + e.toString());
               throw new IOException(e.toString());
        }
    }



/**
Seeks to location <i>offset</i> within the stream.

@param  offset  The number of bytes to seek from current mark.

@exception IOException If an error occurs while communicating with the server.
**/
    private void seekFromCur(int offset)
       throws IOException
    {
        // create the attribute code point for the seek data
        NPCPAttribute cpAttr = new NPCPAttribute();
        cpAttr.setAttrValue(PrintObject.ATTR_SEEKORG, 2);  // current read pointer
        cpAttr.setAttrValue(PrintObject.ATTR_SEEKOFF, offset);  // offset

        // set up the SEEK request datastream
        NPDataStream seekReq = new NPDataStream(objectType_);
        seekReq.setAction(NPDataStream.SEEK);
        seekReq.addCodePoint(cpObjHndl_);
        seekReq.addCodePoint(cpAttr);

        // set up the SEEK reply datastream
        NPDataStream seekRep = new NPDataStream(objectType_);
        seekRep.addCodePoint(cpCPFMsg_);

        try {
            // make SEEK request
            int iRC = conversation_.makeRequest(seekReq, seekRep);

            switch (iRC) {
              case NPDataStream.RET_OK:
                 offset_ += offset;            // update distance from beginning of block
                 break;
              case NPDataStream.RET_SEEK_OFF_BAD:
              default:
                 // we should never get Seek offset bad because we
                 // always check in skip that we aren't going beyond the end of
                 // the file.  The other place we seek is on a reset and that
                 // should work.
                 Trace.log(Trace.ERROR, "Seek from cur error " + Integer.toString(iRC));
                 throw new IOException(Integer.toString(iRC));
           }
        }
        catch (Exception e) {
            Trace.log(Trace.ERROR, "Caught an Exception." + e.toString());
            throw new IOException(e.toString());
        }
    }



/**
Skips over the next <i>bytesToSkip</i> bytes in the stream.
This method may skip less bytes than specified if the end of
the data block is reached. The actual number of bytes skipped is returned.
No action is taken if the number of bytes to skip is not positive.

@param bytesToSkip The number of bytes to be skipped.

@return The actual number of bytes skipped.

@exception IOException If an error occurs while communicating with the server.
**/
    public long skip(long bytesToSkip) throws IOException
    {
        /*
        if (bytesToSkip <= 0) {
            return 0;
        }
        */ // @A2D - check is in public class

        if (conversation_ == null) {
            Trace.log(Trace.ERROR, "Conversation is null.");
            throw new IOException();
        }
        else {
            int maxSkip = numBytes_ - offset_;  // maximum number of bytes you can skip
            if (bytesToSkip > maxSkip) {
                bytesToSkip = maxSkip;
            }
            seekFromCur((int)bytesToSkip);     // seek ahead from current pointer n bytes
        }
        return bytesToSkip;
    }

}
