///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PrintObjectInputStream.java
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

/**
The PrintObjectInputStream class is used to read data out of a
server spooled file or AFP resource such as an overlay or page
segment.
<p>
An instance of this class can be created either by using
the getInputStream method
from the <a href="AFPResource.html"> AFPResource</a> class or by using
the getInputStream method
from the <a href="SpooledFile.html"> SpooledFile</a> class.
**/

public class PrintObjectInputStream extends InputStream
{
    private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    transient private AS400                        system_;
    transient private PrintObjectInputStreamImpl     impl_;


    /**
     * Constructs a  PrintObjectInputStream object. It uses the
     * specified SpooledFile object from which to read and the PrintParameterList.
     * @exception AS400Exception If the server system returns an error message.
     * @exception AS400SecurityException If a security or authority error occurs.
     * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     * @exception IOException If an error occurs while communicating with the server.
     * @exception InterruptedException If this thread is interrupted.
     * @exception RequestNotSupportedException If the requested function is not supported because the server.
     *                                      operating system is not at the correct level.
     **/
    PrintObjectInputStream(SpooledFile sf,
                           PrintParameterList openOptions)
        throws AS400Exception,
               AS400SecurityException,
               ErrorCompletingRequestException,
               IOException,
               InterruptedException,
               RequestNotSupportedException
    {
        system_ = sf.getSystem();
        if (impl_ == null)
            chooseImpl();
        // Do connect here because it could throw Exceptions
        system_.connectService(AS400.PRINT);    
        if (sf.getImpl() == null) {             
            sf.chooseImpl();                    
        }                                       
        impl_.createPrintObjectInputStream((SpooledFileImpl) sf.getImpl(), openOptions);
    }


    /**
     * Constructs a  PrintObjectInputStream object. It uses the
     * specified SpooledFile object from which to read, the PrintParameterList.
     * and the int value of PrintObject.ATTR_ACIF which indicates of the ACIF
     * merged data is to be used.
     * @exception AS400Exception If the server system returns an error message.
     * @exception AS400SecurityException If a security or authority error occurs.
     * @exception ErrorCompletingRequestException If an error occurs before the request is completed.
     * @exception IOException If an error occurs while communicating with the server.
     * @exception InterruptedException If this thread is interrupted.
     * @exception RequestNotSupportedException If the requested function is not supported because the server.
     *                                      operating system is not at the correct level.
     **/
    PrintObjectInputStream(SpooledFile sf,
                           PrintParameterList openOptions, String acifProcess)
        throws AS400Exception,
               AS400SecurityException,
               ErrorCompletingRequestException,
               IOException,
               InterruptedException,
               RequestNotSupportedException
    {
        system_ = sf.getSystem();
        if (impl_ == null)
            chooseImpl();
        // Do connect here because it could throw Exceptions
        system_.connectService(AS400.PRINT);    
        if (sf.getImpl() == null) {             
            sf.chooseImpl();                    
        }                                       
        impl_.createPrintObjectInputStream((SpooledFileImpl) sf.getImpl(), openOptions, acifProcess);
    }


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
     *                                      operating system is not at the correct level.
    **/
    PrintObjectInputStream(AFPResource resource,
                           PrintParameterList openOptions)
        throws AS400Exception,
               AS400SecurityException,
               ErrorCompletingRequestException,
               IOException,
               InterruptedException,
               RequestNotSupportedException
    {
        system_ = resource.getSystem();
        if (impl_ == null)
            chooseImpl();
        // Do connect here because it could throw Exceptions
        system_.connectService(AS400.PRINT);
        if (resource.getImpl() == null) {
            resource.chooseImpl();
        }
        impl_.createPrintObjectInputStream((PrintObjectImpl)resource.getImpl(), openOptions);
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
        return impl_.available();
    }



    // A1A - Added method
    private void chooseImpl()
    {
        if (system_ == null) {
            Trace.log( Trace.ERROR, "Attempt to use PrintObjectInputStream before setting system.");
            throw new ExtendedIllegalStateException("system",
                                    ExtendedIllegalStateException.PROPERTY_NOT_SET);
        }

        impl_ = (PrintObjectInputStreamImpl) system_.loadImpl2("com.ibm.as400.access.PrintObjectInputStreamImplRemote",
                                                               "com.ibm.as400.access.PrintObjectInputStreamImplProxy");
    }



    /**
      * Closes the input stream.
      * It must be called to release any resources associated with the stream.
      * @exception IOException If an error occurs while communicating with the server.
      **/
    public void close()
       throws IOException
    {
        impl_.close();
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
    public void mark(int readLimit)
    {
        impl_.mark(readLimit);
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
    public int read()
        throws IOException
    {
        int iRC = -1;

        byte[] byteBuffer = new byte[1];
        int rc = read(byteBuffer);
        if (rc == 1) {
           iRC = (int)byteBuffer[0];
        }
        return iRC;

    }



    /** Reads up to <i>data.length</i> bytes of data from this
      *  input stream into <i>data</i>.
      *
      * @param data The buffer into which the data is read.
      *
      * @return The total number of bytes read into the buffer,
      *          or -1 if there is no more data because the
      *          end of file has been reached.
      * @exception IOException If an error occurs while communicating with the server.
      **/
    public int read(byte[] data)
        throws IOException
    {
        return read(data, 0, data.length);
    }



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
        return impl_.read(data, dataOffset, length);
    }



    /** Repositions the stream to the last marked position.
      * If the stream has not been marked or if the mark has been invalidated,
      * an IOException is thrown.
      * @exception IOException If an error occurs while communicating with the server.
      **/
    public void reset()
       throws IOException
    {
        impl_.reset();
    }



    /** Skips over the next <i>bytesToSkip</i> bytes in the stream.
      * This method may skip less bytes than specified if the end of
      * file is reached. The actual number of bytes skipped is returned.
      * @param bytesToSkip The number of bytes to be skipped.
      * @return The actual number of bytes skipped.
      * @exception IOException If an error occurs while communicating with the server.
      **/
    public long skip(long bytesToSkip) throws IOException
    {
        return (long) impl_.skip(bytesToSkip);
    }

}
