///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PrintObjectPageInputStream.java
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
The PrintObjectPageInputStream class is used to read data out of a
server spooled file one page at a time.  The page of data may be
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

public class PrintObjectPageInputStream extends InputStream
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    // Private data

    transient private AS400                             system_;
    transient private PrintObjectPageInputStreamImpl      impl_;

/**
Constructs a PrintObjectPageInputStream object.

@param  spooledFile The SpooledFile.
@param  openOptions The PrintParameterList options to be used when opening the SpooledFile.

@exception AS400Exception If the server returns an error message.
@exception AS400SecurityException If a security or authority error occurs.
@exception ErrorCompletingRequestException If an error occurs before the request is completed.
@exception IOException If an error occurs while communicating with the server.
@exception InterruptedException If this thread is interrupted.
@exception RequestNotSupportedException If the requested function is not supported because the
           server operating system is not at the correct level.
**/
    PrintObjectPageInputStream(SpooledFile spooledFile,
                               PrintParameterList openOptions)
        throws AS400Exception,
               AS400SecurityException,
               ErrorCompletingRequestException,
               IOException,
               InterruptedException,
               RequestNotSupportedException
    {
        system_ = spooledFile.getSystem();
        if (impl_ == null)
            chooseImpl();
        // do connect here because it could throw Exceptions
        system_.connectService(AS400.PRINT);
        if (spooledFile.getImpl() == null) {
            spooledFile.chooseImpl();
        }
        impl_.createPrintObjectPageInputStream((SpooledFileImpl) spooledFile.getImpl(), openOptions);
    }



/**
Returns the number of bytes remaining in the current page.

@return  The number of available bytes (without blocking) in the current page.
**/
    public int available() throws IOException
    {
        return impl_.available();
    }



    // A2A - Added method
    private void chooseImpl()
    {
        if (system_ == null) {
            Trace.log( Trace.ERROR, "Attempt to use PrintObjectPageInputStream before setting system.");
            throw new ExtendedIllegalStateException("system",
                                    ExtendedIllegalStateException.PROPERTY_NOT_SET);
        }

        impl_ = (PrintObjectPageInputStreamImpl) system_.loadImpl2("com.ibm.as400.access.PrintObjectPageInputStreamImplRemote",
                                                                   "com.ibm.as400.access.PrintObjectPageInputStreamImplProxy");
    }



/**
Closes the input stream and releases any resources associated with it.

@exception IOException If an error occurs while communicating with the server.
**/
    public void close() throws IOException
    {
        impl_.close();
    }



/**
Returns the number of the current page of the input stream.

@return The number of the current page.
**/
    public int getCurrentPageNumber()
    {
        return impl_.getCurrentPageNumber();
    }



/**
Returns the number of pages in the stream.

@return The number of pages in the stream.
**/
    public int getNumberOfPages()
    {
        return impl_.getNumberOfPages();
    }



/**
Indicates if the number of pages is estimated.

@return True if the number of pages is estimated; false otherwise.
**/
    public boolean isPagesEstimated()
    {
        return impl_.isPagesEstimated();
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
    public void mark(int readLimit)
    {
        impl_.mark(readLimit);
    }



/**
Returns a boolean indicating whether this stream type supports mark and reset.

@return Always true.  Objects of this class will support the mark and reset methods.
**/
    public boolean markSupported()
    {
        return true;
    }



/**
Repositions the stream to the next page.

@return True if the stream is positioned to the next page; false otherwise.

@exception IOException If an error occurs while communicating with the server.
**/
    public boolean nextPage() throws IOException
    {
        return impl_.nextPage();
    }



/**
Repositions the stream to the previous page.

@return True if the stream is positioned to the previous page;
false otherwise.

@exception IOException If an error occurs while communicating with the server.
**/
    public boolean previousPage() throws IOException
    {
        return impl_.previousPage();
    }



/**
Reads the next byte of data from this input stream.

@return The byte read, or -1 if the end of the page stream is reached.

@exception  IOException If an error occurs while communicating with the server.
**/
    public int read() throws IOException
    {
        int readchar = -1;

        byte oneByte[] = new byte[1];
        int rc = read(oneByte, 0, 1);
        if (rc == 1) {
            readchar = (int)oneByte[0];
        }
        return readchar;
    }



/**
Reads up to <i>data.length</i> bytes of data from the page input
stream into <i>data</i>.

@param data The buffer into which the data is read.

@return The total number of bytes read into the buffer or -1 if there is no more
data because the end of the page stream has been reached.

@exception IOException If an error occurs while communicating with the server.
**/
    public int read(byte[] data) throws IOException
    {
        return read(data, 0, data.length);
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
        return impl_.read(data, dataOffset, length);
    }



/**
Repositions the stream to the last marked position.
If the stream has not been marked or if the mark has been invalidated,
an IOException is thrown.

@exception IOException If an error occurs while communicating with the server.
**/
    public void reset() throws IOException
    {
        impl_.reset();
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
        return impl_.selectPage(page);
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
        if (bytesToSkip <= 0) {
            return 0;
        }
        return impl_.skip(bytesToSkip);
    }
}

