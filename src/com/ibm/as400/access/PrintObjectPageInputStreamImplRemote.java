///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PrintObjectPageInputStreamImplRemote.java
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


// NOTE: This class is based on the PrintObjectInputStream.java class.


/**
The PrintObjectPageInputStream class is used to read data out of an
IBM i spooled file one page at a time.  The page of data may be
transformed, depending on the
<a href="PrintParameterList.html">PrintParameterList</a>
used to create an instance of the class.
<p>
The number of pages in the spooled file may be estimated. To help process
spooled files with estimated page counts, methods nextPage, previousPage, and
selectPage will return false if the requested page is not available.
<p>
An instance of this class is created
using the getPageInputStream method
from the class <a href="SpooledFile.html">SpooledFile</a>.
<p>
NOTE: This class is supported on OS/400 V4R4 or later.
Not all spooled file formats are supported for transform.
**/

class PrintObjectPageInputStreamImplRemote
implements PrintObjectPageInputStreamImpl
{
    // Private data
    private NPConversation  conversation_;       // conversation with Network Print Server
    private NPCPAttribute   cpCPFMsg_;           // CPF message code point
    private NPCodePoint     cpObjHndl_;          // page input stream handle code point
    private NPCPID          cpObjID_;            // page input stream ID codepoint
    private int             currentPage_ = 0;    // current page of input stream
    private int             markLimit_ = 0;      // limit of mark/reset
    private boolean         markSet_= false;     // has a mark been set?
    private int             numberOfPages_ = 0;  // total number of pages
    private boolean         pagesEst_ = false;   // indicates if the total number of pages is estimated
    private int             numBytes_ = 0;       // total size of data in page
    private NPSystem        npSystem_;           // system where input stream resides
    private int             objectType_ ;        // object type (always SpooledFile)
    private int             offset_ = 0;         // offset from beginning of page (in bytes)
    private int             offsetFromMark_ = 0; // offset from mark (in bytes)


/**
Constructs a PrintObjectPageInputStream object.

@param  spooledFile The SpooledFile.
@param  openOptions The PrintParameterList options to be used when opening the SpooledFile.

@exception AS400Exception If the system returns an error message.
@exception AS400SecurityException If a security or authority error occurs.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception IOException If an error occurs while communicating with the server.
@exception InterruptedException If this thread is interrupted.
@exception RequestNotSupportedException If the requested function is not supported because the server
           operating system is not at the correct level.
**/
    public synchronized void createPrintObjectPageInputStream(SpooledFileImpl spooledFile,
                                        PrintParameterList openOptions)
        throws AS400Exception,
               AS400SecurityException,
               ErrorCompletingRequestException,
               IOException,
               InterruptedException,
               RequestNotSupportedException
    {
        objectType_ = NPConstants.SPOOLED_FILE;
        npSystem_   = NPSystem.getSystem(((SpooledFileImplRemote) spooledFile).getSystem());
        cpObjID_    = ((SpooledFileImplRemote)spooledFile).getIDCodePoint();
        cpCPFMsg_   = new NPCPAttribute();
        cpObjHndl_  = new NPCPSplFHandle();

        // set up OPEN request datastream
        NPDataStream openReq = new NPDataStream(objectType_);
        openReq.setAction(NPDataStream.OPEN_MODIFIED_SPLF);
        openReq.addCodePoint(cpObjID_);

        // create the Selection Code Point
        NPCPSelection selectionCP = new NPCPSelection();

        // set any options the user passed in
        if (openOptions != null) {
            selectionCP.addUpdateAttributes(openOptions.getAttrCodePoint());
        }

        // set the PAGE_AT_A_TIME attribute to *YES
        selectionCP.setAttrValue(PrintObject.ATTR_PAGE_AT_A_TIME, "*YES");

        // add the selection codepoint to the open request datastream
        openReq.addCodePoint(selectionCP);

        // setup OPEN reply datastream
        NPDataStream openRep = new NPDataStream(objectType_);
        openRep.addCodePoint(cpObjHndl_);
        openRep.addCodePoint(cpCPFMsg_);

        // retrieve the conversation for opening the spooled file
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
                // retrieve the total number of pages, whether the number
                // of pages is estimated, and the number of bytes in the first page
                retrievePageInformation();
                currentPage_ = 1;
                fOpenOK = true;
            }
        }
        finally {
            if (!fOpenOK) { // if an exception was thrown, return conversation
                if (npSystem_ != null) {
                    npSystem_.returnConversation(conversation_);
                }
                conversation_ = null;
            }
        }
    }



/**
Returns the number of bytes remaining in the current page.

@return  The number of available bytes (without blocking) in the current page.
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
Closes the input stream when garbage is collected.

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
                // close the page input stream
                // @B1D closeReq.setHostCCSID(conversation_.getHostCCSID());
                closeReq.setConverter(conversation_.getConverter());
                server.sendAndDiscardReply(closeReq);
            }

            if (npSystem_ != null) {
                npSystem_.returnConversation(conversation_);
                npSystem_  = null;
            }
            conversation_  = null;
        }
        super.finalize();   // always call super.finalize()!
    }



/**
Returns the number of the current page of the input stream.

@return The number of the current page.
**/
    public int getCurrentPageNumber()
    {
        return currentPage_;
    }



/**
Returns the number of pages in the stream.

@return The number of pages in the stream.
**/
    public int getNumberOfPages()
    {
        return numberOfPages_;
    }



/**
Indicates if the number of pages is estimated.

@return True if the number of pages is estimated; false otherwise.
**/
    public boolean isPagesEstimated()
    {
        return pagesEst_;
    }



/**
Marks the current position of the current page of the input stream.

A subsequent call to reset() will reposition the stream at the
last marked position, so that subsequent reads will reread the same bytes.
The stream promises to allow readLimit bytes to be read before the
mark position gets invalidated, provided readLimit does not exceed amount
of page data available, in which case the readLimit is set to a value
equal to the amount of data available until the end of the page is reached.


@param readLimit The maximum limit of bytes allowed to be read before
the mark position is no longer valid.
**/
    public synchronized void mark(int readLimit)
    {
        offsetFromMark_ = 0;
        int maxReadLimit = numBytes_ - offset_;
        if (readLimit > maxReadLimit) {
            readLimit = maxReadLimit;
        }
        markLimit_ = readLimit;
        markSet_ = true;
    }



/**
Repositions the stream to the next page.

@return True if the stream is positioned to the next page; false otherwise.

@exception IOException If an error occurs while communicating with the server.
**/
    public boolean nextPage() throws IOException
    {
        return selectPage(currentPage_ + 1);
    }



/**
Repositions the stream to the previous page.

@return True if the stream is positioned to the previous page;
false otherwise.

@exception IOException If an error occurs while communicating with the server.
**/
    public boolean previousPage() throws IOException
    {
        return selectPage(currentPage_ - 1);
    }



/**
Reads up to <i>length</i> bytes of data from this input stream into <i>data</i>,
starting at the array offset <i>dataOffset</i>.

@param data The buffer into which the data is read.
@param dataOffset The start offset of the data.
@param length The maximum number of bytes to read.

@return The total number of bytes read into the buffer, or -1 if there is
no more data because the end of the page stream has been reached.

@exception IOException If an error occurs while communicating with the server.
**/
    public int read(byte data[], int dataOffset, int length) throws IOException
    {
        int bytesRead = 0;
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

                switch (iRC) {
                    case NPDataStream.RET_OK:
                    case NPDataStream.RET_READ_INCOMPLETE:   // maybe read some bytes?
                        // see how many bytes we read
                        bytesRead = cpData.getDataLength();
                        offsetFromMark_ += bytesRead;
                        offset_ += bytesRead;              // update distance from the start of the page
                        break;
                    case NPDataStream.RET_READ_OUT_OF_RANGE:
                        // this return code is returned if there are no more
                        // bytes to be read from the current page
                        bytesRead = -1;
                        break;
                    case NPDataStream.RET_READ_EOF:
                        // this return code is returned if there are no
                        // bytes read at all and EOF is reached
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
Repositions the stream to the last marked position.
If the stream has not been marked or if the mark has been invalidated,
an IOException is thrown.

@exception IOException If an error occurs while communicating with the server.
**/
    public synchronized void reset() throws IOException
    {
        if (conversation_ == null) {
            Trace.log(Trace.ERROR, "Conversation is null.");
            throw new IOException();
        }
        else if (!markSet_) {
            Trace.log(Trace.ERROR, "Mark not set.");
            throw new IOException();
        }
        else if ((markLimit_ == 0) || (offsetFromMark_ > markLimit_)) {
               Trace.log(Trace.WARNING, "Stream has not been marked or mark has been invalidated.");
               throw new IOException();
        }
        else {
               // seek backwards from the current spot offsetFromMark_ bytes
               // and reset offsetFromMark_ to 0
               if (offsetFromMark_ != 0)
                   seekFromCur(-offsetFromMark_);
        }
    }



/**
Retrieves the number of bytes in the current page.
**/
    private void retrieveNumberOfPageBytes()
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
                        "Network Print Server does not support retrieving spooled file page length.");
                    throw new RequestNotSupportedException(
                              conversation_.getAttribute(PrintObject.ATTR_NPSLEVEL),
                              RequestNotSupportedException.SYSTEM_LEVEL_NOT_CORRECT);
                }
            }
            else {
                Trace.log(Trace.ERROR,
                    "Network Print Server error retrieving spooled file page length. RC ="+ rc);
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
Retrieves the number of pages in the stream, whether the number of pages
is estimated or an accurate value, and the number of bytes in the first page.
These values are then stored in the appropriate instance variables.

@exception IOException If an error occurs while communicating with the server.
**/
    private void retrievePageInformation() throws IOException
    {
        if (conversation_ == null) {
            Trace.log(Trace.ERROR, "Conversation is null.");
            throw new IOException();
        }
        else {
            // set up attributes to retrieve ID list
            NPCPAttributeIDList cpAttrIDs = new NPCPAttributeIDList();
            cpAttrIDs.addAttrID(PrintObject.ATTR_PAGES);     // number of pages
            cpAttrIDs.addAttrID(PrintObject.ATTR_PAGES_EST); // pages estimated?
            cpAttrIDs.addAttrID(PrintObject.ATTR_NUMBYTES);  // available page bytes

            // set up TELL request datastream
            NPDataStream tellReq = new NPDataStream(objectType_);
            tellReq.setAction(NPDataStream.TELL);
            tellReq.addCodePoint(cpObjHndl_);
            tellReq.addCodePoint(cpAttrIDs);

            // set up TELL reply datastream
            NPDataStream tellRep = new NPDataStream(objectType_);
            NPCPAttribute cpAttr = new NPCPAttribute();
            tellRep.addCodePoint(cpAttr);

            try {
                int rc = conversation_.makeRequest(tellReq, tellRep);

                if (rc == NPDataStream.RET_OK) {
                     // assign temp variables
                     Integer pages    = cpAttr.getIntValue(PrintObject.ATTR_PAGES);
                     String estimated = cpAttr.getStringValue(PrintObject.ATTR_PAGES_EST);
                     Integer bytes    = cpAttr.getIntValue(PrintObject.ATTR_NUMBYTES);

                     // check for null returns; signal error if necessary
                     if ((pages == null) || (estimated == null) || (bytes == null)) {
                        Trace.log(Trace.ERROR, "NPServer.TELL returned null page information!");
                        throw new RequestNotSupportedException(
                            conversation_.getAttribute(PrintObject.ATTR_NPSLEVEL),
                            RequestNotSupportedException.SYSTEM_LEVEL_NOT_CORRECT);
                     }
                     else {
                        // assign instance variables
                        numberOfPages_ = pages.intValue();
                        numBytes_ = bytes.intValue();
                        if (estimated.equals("*YES"))
                                pagesEst_ = true;
                        else
                                pagesEst_ = false;
                    }
                }
                else {
                     // Anything else would be an error...The conversation should handle
                     // the basic CPF message error for us, so this would be some unexpected
                     // result.  We'll throw a runtime error.
                        Trace.log(Trace.ERROR, "Error received retrieving page information : " + Integer.toString(rc));
                        throw new IOException(Integer.toString(rc));
                }
           }
           catch (Exception e) {
               Trace.log(Trace.ERROR, "Caught an Exception." + e.toString());
               throw new IOException(e.toString());
           }
        }
    }



/**
Repositions the stream to page <i>page</i>.

@param  page The page at which to reposition the input stream.

@return True if the stream is positioned to the specified page; false otherwise.

@exception IOException If an error occurs while communicating with the server,
or an error occurs selecting the specified page.
@exception IllegalArgumentException If <i>page</i> is negative.
**/
    public boolean selectPage(int page) throws IOException, IllegalArgumentException
    {
        if (conversation_ == null) {
            Trace.log(Trace.ERROR, "Conversation is null.");
            throw new IOException();
        }
        else if ((currentPage_ == page) && (offset_ == 0)) {
            // do nothing, page is already loaded.
            return true;
        }
        else if (page < 0) {
            Trace.log(Trace.ERROR, "NPServer.SELECT_PAGE error: Page number negative");
            throw new IllegalArgumentException();
        }
        else {
            // set the page number to go to in the input stream
            NPCPAttribute cpAttr = new NPCPAttribute();
            cpAttr.setAttrValue(PrintObject.ATTR_PAGENUMBER, page);

            // set up SELECT_PAGE request datastream
            NPDataStream pageReq = new NPDataStream(objectType_);
            pageReq.setAction(NPDataStream.SELECT_PAGE);
            pageReq.addCodePoint(cpObjHndl_);
            pageReq.addCodePoint(cpAttr);

            // set up SELECT_PAGE reply datastream
            NPDataStream pageRep = new NPDataStream(objectType_);
            pageRep.addCodePoint(cpCPFMsg_);

            try {
                // make the SELECT_PAGE request
                int iRC = conversation_.makeRequest(pageReq, pageRep);

                switch (iRC) {
                    case NPDataStream.RET_OK:
                        // assign current page
                        currentPage_ = page;

                        // reset mark information
                        markSet_ = false;
                        markLimit_ = 0;
                        offset_ = 0;
                        offsetFromMark_ = 0;

                        // retrieve the number of bytes in the page
                        retrieveNumberOfPageBytes();
                        return true;
                        // break;
                    case NPDataStream.RET_PAGE_OUT_OF_RANGE:
                        Trace.log(Trace.ERROR, "NPServer.SELECT_PAGE error: Page out of range");
                        return false;
                        // break;
                        // throw new IOException(Integer.toString(iRC));
                    default:
                        // The conversation should handle the basic CPF
                        // message error for us,  so this would be some unexpected
                        // result.
                        Trace.log(Trace.ERROR, "NPServer.SELECT_PAGE error: " + Integer.toString(iRC));
                        throw new IOException(Integer.toString(iRC));
                }
            }
            catch (Exception e) {
                Trace.log(Trace.ERROR, "Caught an Exception." + e.toString());
                throw new IOException(e.toString());
            }
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
                 offsetFromMark_ += offset;     // update how far we went from the mark
                 offset_ += offset;            // update distance from beginning of page
                 break;
              case NPDataStream.RET_SEEK_OFF_BAD:
              default:
                 // we should never get Seek offset bad because we
                 // always check in skip that we aren't going beyond the end of
                 // the page/file.  The other place we seek is on a reset and that
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
the page is reached. The actual number of bytes skipped is returned.
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
        */  // @A2D - check is in public class

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
