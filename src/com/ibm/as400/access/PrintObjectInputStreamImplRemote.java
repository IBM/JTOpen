///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PrintObjectInputStreamImplRemote.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;


/**
  * The PrintObjectInputStream class is used to read data out of an
  * iSeries spooled file or AFP resource such as an overlay or page
  * segment.
  **/

class PrintObjectInputStreamImplRemote
implements PrintObjectInputStreamImpl
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    private String x = Copyright.copyright;     // @A1C - Copyright change
    private NPConversation conversation_;
    private NPCodePoint    cpObjHndl_;       // Spooled File handle or Resource Handle code point
    private NPCPID         cpObjID_;         // spooled file or resource ID codepoint
    private NPCPAttribute  cpCPFMsg_;
    private int            markLimit_      = 0;  // limit of mark/reset
    private boolean        markSet_        = false; // has a mark been set?
    private NPSystem       npSystem_;
    private int            numBytes_       = 0;  // total size in splf or resource in bytes
    private int            objectType_;
    private int            offset_         = 0;  // offset from beginning of file in bytes
    private int            offsetFromMark_ = 0;  // offset from mark



    /**
      * Constructs a  PrintObjectInputStream object. It uses the
      * specified SpooledFile object from which to read and the PrintParameterList.
      * @exception AS400Exception If the server system returns an error message.
      * @exception AS400SecurityException If a security or authority error occurs.
      * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
      * @exception IOException If an error occurs while communicating with the server.
      * @exception InterruptedException If this thread is interrupted.
      * @exception RequestNotSupportedException If the requested function is not supported because the server
      *                                      system is not at the correct level.
      **/
    public synchronized void createPrintObjectInputStream(SpooledFileImpl sf,
                                             PrintParameterList openOptions)
        throws AS400Exception,
               AS400SecurityException,
               ErrorCompletingRequestException,
               IOException,
               InterruptedException,
               RequestNotSupportedException
    {
        objectType_ = NPConstants.SPOOLED_FILE; // @B1C
        NPDataStream openReq = new NPDataStream(objectType_);
        NPDataStream openRep = new NPDataStream(objectType_);
        npSystem_ = NPSystem.getSystem(((SpooledFileImplRemote) sf).getSystem());  // @A2C
        cpCPFMsg_ = new NPCPAttribute();
        cpObjID_   = ((SpooledFileImplRemote) sf).getIDCodePoint();  // @A2C
        cpObjHndl_ = new NPCPSplFHandle();


        // setup the request data stream
        openReq.setAction(NPDataStream.OPEN);
        openReq.addCodePoint(cpObjID_);
        // for opening spooled files we need to send up a selection code point
        // with the attribute of PRECOMPUTE NUMBER OF BYTES set to YES
        NPCPSelection selectionCP = new NPCPSelection();

        // first set any options the user has passed in
        if (openOptions != null)
        {
            selectionCP.addUpdateAttributes(openOptions.getAttrCodePoint());
        }

        // then set the precompute size to *YES.
        selectionCP.setAttrValue(PrintObject.ATTR_PRECOMPUTE_NUMBYTES, "*YES");

        // add the selection codepoint to the open request datastream
        openReq.addCodePoint(selectionCP);

        // setup the reply datastream
        openRep.addCodePoint(cpObjHndl_);
        openRep.addCodePoint(cpCPFMsg_);

        // try to open the spooled file

        conversation_ = npSystem_.getConversation();
        boolean fOpenOK = false;
        try
        {
           int rc = conversation_.makeRequest(openReq, openRep);
           if (rc != NPDataStream.RET_OK)
           {
               Trace.log(Trace.ERROR, "Error opening SpooledFile; rc = " + rc);
               throw new ErrorCompletingRequestException(ErrorCompletingRequestException.AS400_ERROR);
           } else {
              // try to get the number of bytes in the spooled file
              // it will throw an exception if there is any error
              retrieveNumberOfBytes();
              fOpenOK = true;       //we opened the spooled file
           }
        }
        finally
        {
            // if we got here because an exception was thrown
            if (!fOpenOK)
            {
               if (npSystem_ != null) {
                  npSystem_.returnConversation(conversation_);
               }
               conversation_ = null;
            }
        }
    }
//C1A
    /**
      * Constructs a  PrintObjectInputStream object. It uses the
      * specified SpooledFile object from which to read, the PrintParameterList
      * and a hidden attribute ATTR_ACIF to indicate a ACIF merge file process
      * should be used.
      * @exception AS400Exception If the server system returns an error message.
      * @exception AS400SecurityException If a security or authority error occurs.
      * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
      * @exception IOException If an error occurs while communicating with the server.
      * @exception InterruptedException If this thread is interrupted.
      * @exception RequestNotSupportedException If the requested function is not supported because the server
      *                                      system is not at the correct level.
      **/
    public synchronized void createPrintObjectInputStream(SpooledFileImpl sf,
                                             PrintParameterList openOptions, String acifP)
        throws AS400Exception,
               AS400SecurityException,
               ErrorCompletingRequestException,
               IOException,
               InterruptedException,
               RequestNotSupportedException
    {
        objectType_ = NPConstants.SPOOLED_FILE; // @B1C
        NPDataStream openReq = new NPDataStream(objectType_);
        NPDataStream openRep = new NPDataStream(objectType_);
        npSystem_ = NPSystem.getSystem(((SpooledFileImplRemote) sf).getSystem());  // @A2C
        cpCPFMsg_ = new NPCPAttribute();
        cpObjID_   = ((SpooledFileImplRemote) sf).getIDCodePoint();  // @A2C
        cpObjHndl_ = new NPCPSplFHandle();


        // setup the request data stream
        openReq.setAction(NPDataStream.OPEN);
        openReq.addCodePoint(cpObjID_);
        // for opening spooled files we need to send up a selection code point
        // with the attribute of PRECOMPUTE NUMBER OF BYTES set to YES
        NPCPSelection selectionCP = new NPCPSelection();
        selectionCP.setAttrValue(PrintObject.ATTR_ACIF, acifP);
        // first set any options the user has passed in
        if (openOptions != null)
        {
            selectionCP.addUpdateAttributes(openOptions.getAttrCodePoint());
        }

        // then set the precompute size to *YES.
        selectionCP.setAttrValue(PrintObject.ATTR_PRECOMPUTE_NUMBYTES, "*YES");

        // add the selection codepoint to the open request datastream
        openReq.addCodePoint(selectionCP);

        // setup the reply datastream
        openRep.addCodePoint(cpObjHndl_);
        openRep.addCodePoint(cpCPFMsg_);

        // try to open the spooled file

        conversation_ = npSystem_.getConversation();
        boolean fOpenOK = false;
        try
        {
           int rc = conversation_.makeRequest(openReq, openRep);
           if (rc != NPDataStream.RET_OK)
           {
               Trace.log(Trace.ERROR, "Error opening SpooledFile; rc = " + rc);
               throw new ErrorCompletingRequestException(ErrorCompletingRequestException.AS400_ERROR);
           } else {
              // try to get the number of bytes in the spooled file
              // it will throw an exception if there is any error
              retrieveNumberOfBytes();
              fOpenOK = true;       //we opened the spooled file
           }
        }
        finally
        {
            // if we got here because an exception was thrown
            if (!fOpenOK)
            {
               if (npSystem_ != null) {
                  npSystem_.returnConversation(conversation_);
               }
               conversation_ = null;
            }
        }
    }
//C1A


    /**
      * Contructs a PrintObjectInputStream object.
      * It uses the specified  AFP Resource object from which to read and
      * the PrintParameterList.
      * @exception AS400Exception If the server system returns an error message.
      * @exception AS400SecurityException If a security or authority error occurs.
      * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
      * @exception IOException If an error occurs while communicating with the server.
      * @exception InterruptedException If this thread is interrupted.
      * @exception RequestNotSupportedException If the requested function is not supported because the server
      *                                      system is not at the correct level.
      **/
    public synchronized void createPrintObjectInputStream(PrintObjectImpl resource,  // @A2C
                                             PrintParameterList openOptions)
        throws AS400Exception,
               AS400SecurityException,
               ErrorCompletingRequestException,
               IOException,
               InterruptedException,
               RequestNotSupportedException
    {
        objectType_ = NPConstants.RESOURCE; // @B1C
        NPDataStream openReq = new NPDataStream(objectType_);
        NPDataStream openRep = new NPDataStream(objectType_);
        npSystem_ = NPSystem.getSystem(((AFPResourceImplRemote) resource).getSystem()); // @A2C
        cpCPFMsg_ = new NPCPAttribute();
        cpObjID_   = ((AFPResourceImplRemote)resource).getIDCodePoint();
        cpObjHndl_ = new NPCPResHandle();

        // setup the request data stream
        openReq.setAction(NPDataStream.OPEN);
        openReq.addCodePoint(cpObjID_);
        if (openOptions != null)
        {
            openReq.addCodePoint(openOptions.getAttrCodePoint());
        }

        // setup the reply datastream
        openRep.addCodePoint(cpObjHndl_);
        openRep.addCodePoint(cpCPFMsg_);

        // try to open the AFP resource
        conversation_ = npSystem_.getConversation();
        boolean fOpenOK = false;
        try
        {
           int rc = conversation_.makeRequest(openReq, openRep);
           if (rc != NPDataStream.RET_OK)
           {
               String curLevel = conversation_.getAttribute(PrintObject.ATTR_NPSLEVEL);
               Trace.log(Trace.ERROR, "Error opening AFP Resource; rc = " + rc);
               switch(rc)
               {
                   // we get back RET_INV_REQ_ACT on pre-V3R7 systems if we try
                   // to open an AFP resource.  The host must be at V3R7 with PTFs
                   // to work with AFP resources so throw a requestNotSupportedException
                   // here.
                  case NPDataStream.RET_INV_REQ_ACT:
                      throw new RequestNotSupportedException(curLevel,
                                   RequestNotSupportedException.SYSTEM_LEVEL_NOT_CORRECT);


                  // any other error is either an unexpected error or an error
                  // completing request
                  default:

                      break;
               }
           } else {
              // try to get the number of bytes in the resource
              // it will throw an exception if there is any error
              retrieveNumberOfBytes();
              fOpenOK = true;       //we opened the spooled file
           }


        }
        finally
        {
            // if we threw an exception, then return the conversation here
            if (!fOpenOK)
            {
               if (npSystem_ != null) {
                  npSystem_.returnConversation(conversation_);
               }
               conversation_ = null;
            }
        }

    }



    /**
      * Returns the number of bytes that can be read without blocking.
      * This class always returns the number of bytes remaining in the spooled
      *  file or AFP resource.
      * @return  The number of available bytes without blocking.
      **/
    public int available()
         throws IOException
    {
         return numBytes_ - offset_;
    }



    /**
      * Closes the input stream.
      * It must be called to release any resources associated with the stream.
      * @exception IOException If an error occurs while communicating with the server.
      **/
    public void close()
       throws IOException
    {
        if (conversation_ == null)
        {
            Trace.log(Trace.ERROR, "Conversation is null.");
            throw new IOException();
        } else {
            NPDataStream closeReq = new NPDataStream(objectType_);
            NPDataStream closeRep = new NPDataStream(objectType_);

            closeReq.setAction(NPDataStream.CLOSE);
            closeReq.addCodePoint(cpObjHndl_);

            closeRep.addCodePoint(cpCPFMsg_);
            try
            {
               int rc = conversation_.makeRequest(closeReq, closeRep);
               if (rc != NPDataStream.RET_OK)
               {
                   Trace.log(Trace.ERROR, "Error opening SpooledFile; rc = " + rc);
                   npSystem_.returnConversation(conversation_);
                   conversation_ = null;
               }
            }
            catch (Exception e)
            {
               Trace.log(Trace.ERROR, "Caught an Exception." + e.toString());
               throw new IOException(e.toString());
            }

            finally
            {
                if (npSystem_ != null) {
                   npSystem_.returnConversation(conversation_);
                   npSystem_ = null;
                }
                conversation_ = null;
            }
        }
    } // close()



    /**
      *Closes the stream when garbage is collected.
      *@exception Throwable If an error occurs.
     **/
    protected void finalize()
       throws Throwable
    {
        if (conversation_ != null)
        {
            // attempt to send the close() request and then
            // return the conversation to the pool...
            // We must ignore any replies here to avoid a deadlock
            // if we are called on the AS400Server's background thread.
            NPDataStream closeReq = new NPDataStream(objectType_);
            closeReq.setAction(NPDataStream.CLOSE);
            closeReq.addCodePoint(cpObjHndl_);
            AS400Server server= conversation_.getServer();
            if (server != null)
            {
                // @B1D closeReq.setHostCCSID(conversation_.getHostCCSID());
                closeReq.setConverter(conversation_.getConverter());            // @B1A
                server.sendAndDiscardReply(closeReq);
            }

            if (npSystem_ != null) {
               npSystem_.returnConversation(conversation_);
               npSystem_  = null;
            }
            conversation_ = null;
        }
        super.finalize();   // always call super.finalize()!
    }



    /**  Marks the current position in the input stream.
      *  A subsequent call to reset() will reposition the stream at the
      *  last marked position, so that subsequent reads will reread the same bytes.
      *  The stream promises to allow readLimit bytes to be read before the
      *  mark position gets invalidated.
      *
      * @param readLimit The maximum limit of bytes allowed
      *  to be read before the mark position becomes invalid.
      **/
    public synchronized void mark(int readLimit)
    {
        offsetFromMark_ = 0;
        markLimit_ = readLimit;
        markSet_ = true;
    }



    /** Returns a boolean indicating whether this stream type
      * supports mark/reset.
      *
      * @return Always true.  Objects of this class will support
      * the mark/reset methods.
      **/
    public boolean markSupported()
    {
        return true;
    }



    /** Reads the next byte of data from this input stream.
      * @return The byte read, or -1 if the end of the stream is reached.
      * @exception IOException If an error occurs while communicating with the server.
      **/
  /*  public int read()
        throws IOException
    {
        int iRC = -1;
        if (conversation_ == null)
        {
        Trace.log(Trace.ERROR, "Conversation is null.");
            throw new IOException();
        } else {
            byte[] byteBuffer = new byte[1];
            int rc = read(byteBuffer);
            if (rc == 1)
            {
               iRC = (int)byteBuffer[0];
            }
        }
        return iRC;

    } // read()

*/

    /** Reads up to <i>data.length</i> bytes of data from this
      * input stream into <i>data</i>.
      *
      * @param data The buffer into which the data is read.
      *
      * @return The total number of bytes read into the buffer,
      *          or -1 if there is no more data because the
      *          end of file has been reached.
      * @exception IOException If an error occurs while communicating with the server.
      **/
  /*  public int read(byte[] data)
        throws IOException
    {
        return read(data, 0, data.length);
    } // read(byte[]) */



    /** Reads up to <i>length</i> bytes of data from this input stream
      * into <i>data</i>, starting at the array offset <i>dataOffset</i>.
      *
      * @param data The buffer into which the data is read.
      * @param dataOffset The start offset of the data.
      * @param length The maximum number of bytes to read.
      *
      * @return The total number of bytes read into the buffer,
      *          or -1 if there is no more data because the
      *          end of file has been reached.
      * @exception IOException If an error occurs while communicating with the server.
      **/
    public int read(byte data[], int dataOffset, int length)
        throws IOException
    {
        int bytesRead = 0;
        if (conversation_ == null)
        {
        Trace.log(Trace.ERROR, "Conversation is null.");
            throw new IOException();
        } else {

            NPDataStream readReq = new NPDataStream(objectType_);
            NPDataStream readRep = new NPDataStream(objectType_);
            NPCPAttribute cpAttr = new NPCPAttribute();
            NPCPData      cpData = new NPCPData();

            // set the number of bytes to read
            cpAttr.setAttrValue(PrintObject.ATTR_NUMBYTES, length);
            readReq.setAction(NPDataStream.READ);
            readReq.addCodePoint(cpObjHndl_);
            readReq.addCodePoint(cpAttr);

            // Point the data codepoint to receive the data into our buffer.
            // Our buffer better be big enough to hold the data or we''ll
            // not get it at all
            cpData.setDataBuffer(data, 0, dataOffset);
            readRep.addCodePoint(cpData);
            readRep.addCodePoint(cpCPFMsg_);
            try
            {
                int iRC = conversation_.makeRequest(readReq, readRep);
                switch (iRC)
                {
                    case NPDataStream.RET_OK:
                    case NPDataStream.RET_READ_INCOMPLETE:   // maybe read some bytes?
                       // see how many bytes we read
                       bytesRead = cpData.getDataLength();
                       offsetFromMark_ += bytesRead;     // update how far we went from the mark
                       offset_ += bytesRead;              // update how far we are from the start of file
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

        catch (Exception e)
        {
            Trace.log(Trace.ERROR, "PrintObjectInputStream::read() - caught an Exception." + e.toString());
            throw new IOException(e.toString());
        }


        }

       return bytesRead;
    } // read(byte[], int, int)



    /** Repositions the stream to the last marked position.
      * If the stream has not been marked or if the mark has been invalidated,
      * an IOException is thrown.
      * @exception IOException If an error occurs while communicating with the server.
      **/
    public synchronized void reset()
       throws IOException
    {
        if (conversation_ == null)
        {
            Trace.log(Trace.ERROR, "Conversation is null.");
            throw new IOException();
        }
        if (!markSet_)
        {
            Trace.log(Trace.ERROR, "Mark not set.");
            throw new IOException();
        } else {
           if ((markLimit_ == 0) || (offsetFromMark_ > markLimit_))
           {
               Trace.log(Trace.WARNING, "Stream has not been marked or mark has been invalidated.");
               throw new IOException();
           } else {
               // seek backwards from the current spot offsetFromMark_ bytes
               // and reset offsetFromMark_ to 0
               if (offsetFromMark_ != 0)
               {
                   seekFromCur(-offsetFromMark_);
               }
           }
        }

    } // reset()



    private void retrieveNumberOfBytes()
       throws AS400Exception,
              AS400SecurityException,
              ErrorCompletingRequestException,
              IOException,
              InterruptedException,
              RequestNotSupportedException

    {
        NPDataStream sendDS  = new NPDataStream(objectType_);
        NPDataStream replyDS = new NPDataStream(objectType_);
        NPCPAttribute  cpAttrs = new NPCPAttribute();
        NPCPAttributeIDList cpAttrsToRetrieve = new NPCPAttributeIDList();
        cpAttrsToRetrieve.addAttrID(PrintObject.ATTR_NUMBYTES);

        sendDS.addCodePoint(cpObjID_);
        sendDS.addCodePoint(cpAttrsToRetrieve);
        sendDS.setAction(NPDataStream.RETRIEVE_ATTRIBUTES);
        replyDS.addCodePoint(cpAttrs);

        int rc = conversation_.makeRequest(sendDS, replyDS);
        if (rc == NPDataStream.RET_OK)
        {
            Integer numBytes = cpAttrs.getIntValue(PrintObject.ATTR_NUMBYTES);
            if (numBytes != null)
            {
                numBytes_ = numBytes.intValue();
            } else {
                Trace.log(Trace.ERROR,
                          " Network Print Server does not support retrieving splf/resource length");
                throw new RequestNotSupportedException(
                            conversation_.getAttribute(PrintObject.ATTR_NPSLEVEL),
                            RequestNotSupportedException.SYSTEM_LEVEL_NOT_CORRECT);
            }
        } else {
            Trace.log(Trace.ERROR,
                     " Network Print Server error retrieving splf/resource length. RC =" +
                      rc);
            throw new RequestNotSupportedException(
                           conversation_.getAttribute(PrintObject.ATTR_NPSLEVEL),
                           RequestNotSupportedException.SYSTEM_LEVEL_NOT_CORRECT);
        }


    } // retrieveNumBytes()



    /** Skips over the next <i>bytesToSkip</i> bytes in the stream.
      * This method may skip less bytes than specified if the end of
      * file is reached. The actual number of bytes skipped is returned.
      * @param bytesToSkip The number of bytes to be skipped.
      * @return The actual number of bytes skipped.
      * @exception IOException If an error occurs while communicating with the server.
      **/
    public long skip(long bytesToSkip) throws IOException
    {
        if ( (conversation_ == null))
        {
            Trace.log(Trace.ERROR, "Conversation is null.");
            throw new IOException();
        } else {
            int maxSkip = numBytes_ - offset_;  // maximum number of bytes you can skip
            if (bytesToSkip > maxSkip)
            {
                bytesToSkip = maxSkip;
            }

            seekFromCur((int)bytesToSkip);     // seek ahead from current pointer n bytes
        }
        return bytesToSkip;

    } // skip(long n);



    private void seekFromCur(int offset)
       throws IOException
    {
        NPDataStream seekReq = new NPDataStream(objectType_);
        NPDataStream seekRep = new NPDataStream(objectType_);
        NPCPAttribute cpAttr = new NPCPAttribute();

        cpAttr.setAttrValue(PrintObject.ATTR_SEEKORG, 2);  // current read pointer
        cpAttr.setAttrValue(PrintObject.ATTR_SEEKOFF, offset);  // offset

        seekReq.setAction(NPDataStream.SEEK);
        seekReq.addCodePoint(cpObjHndl_);
        seekReq.addCodePoint(cpAttr);
        seekRep.addCodePoint(cpCPFMsg_);
        try
        {
           int iRC = conversation_.makeRequest(seekReq, seekRep);
            switch (iRC)
           {
              case NPDataStream.RET_OK:
                 offsetFromMark_ += offset;     // update how far we went from the mark
                 offset_ += offset;            // update how far we are from beginning of file
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

        catch (Exception e)
        {
            Trace.log(Trace.ERROR, "Caught an Exception." + e.toString());
            throw new IOException(e.toString());
        }


    } // seekFromCur()



    /**
      * returns the number of bytes from the beginning of the file.
      *
      * @return The number of bytes from the beginning of the file.
      * @exception IOException If an error occurs while communicating with the server.
      **/
    long tell()
       throws IOException
    {
        if ( (conversation_ == null))
        {
            Trace.log(Trace.ERROR, "Conversation is null.");
            throw new IOException();
        } else {
            NPDataStream tellReq = new NPDataStream(objectType_);
            NPDataStream tellRep = new NPDataStream(objectType_);
            NPCPAttributeIDList cpAttrIDs = new NPCPAttributeIDList();
            NPCPAttribute cpAttr = new NPCPAttribute();

            cpAttrIDs.addAttrID(PrintObject.ATTR_SEEKOFF);


            tellReq.setAction(NPDataStream.TELL);
            tellReq.addCodePoint(cpObjHndl_);
            // the cpAttr will catch either the offset attribute or the CPF message
            tellRep.addCodePoint(cpAttr);
            try
            {
               int iRC = conversation_.makeRequest(tellReq, tellRep);
               switch (iRC)
               {
                  case NPDataStream.RET_OK:
                     Integer curOffset = cpAttr.getIntValue(PrintObject.ATTR_SEEKOFF);
                     if (curOffset == null)
                     {
                        Trace.log(Trace.ERROR, " tell() returned null!");
                        throw new InternalErrorException(InternalErrorException.PROTOCOL_ERROR);
                     } else {
                        return curOffset.intValue();
                     }

                  default:
                     // Anything else would be an error...The conversation should handle
                     // the basic CPF message error for us, so this would be some unexpected
                     // result - throw a runtime error.

                     Trace.log(Trace.ERROR, " NPServer.Tell() returned " + iRC);
                     throw new InternalErrorException(InternalErrorException.PROTOCOL_ERROR);

               }
           }
           catch (Exception e)
           {
               Trace.log(Trace.ERROR, "Caught an Exception." + e.toString());
               throw new IOException(e.toString());
           }

        }

    } // tell()

} // PrintObjectInputStream class

