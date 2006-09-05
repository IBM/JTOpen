///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: IFSFileReader.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 2004-2004 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.io.Reader;

/**
Convenience class for reading character files in the integrated file system.
The behavior of this class is comparable to <tt>java.io.FileReader</tt>.
<br>
IFSFileReader is meant for reading streams of characters.
For reading streams of raw bytes, use {@link IFSFileInputStream IFSFileInputStream}.
If an <tt>InputStream</tt> is required, use {@link IFSTextFileInputStream IFSTextFileInputStream}.
<p>
The following example illustrates the use of IFSFileReader:
<pre>
import java.io.BufferedReader;
// Work with /File1 on the system eniac.
AS400 system = new AS400("eniac");
IFSFile file = new IFSFile(system, "/File1");
BufferedReader reader = new BufferedReader(new IFSFileReader(file));
// Read the first line of the file, converting characters.
String line1 = reader.readLine();
// Display the String that was read.
System.out.println(line1);
// Close the reader.
reader.close();
</pre>
 **/
public class IFSFileReader extends Reader
{
  private static final String copyright = "Copyright (C) 2004-2004 International Business Machines Corporation and others.";

  static final long serialVersionUID = 4L;


  /**
   Share option that allows read and write access by other users.
   **/
  public final static int SHARE_ALL = IFSFileInputStream.SHARE_ALL;
  /**
   Share option that does not allow read or write access by other users.
   **/
  public final static int SHARE_NONE = IFSFileInputStream.SHARE_NONE;
  /**
   Share option that allows only read access by other users.
   **/
  public final static int SHARE_READERS = IFSFileInputStream.SHARE_READERS;
  /**
   Share option that allows only write access by other users.
   **/
  public final static int SHARE_WRITERS = IFSFileInputStream.SHARE_WRITERS;

  transient private ConvTableReader reader_;

  private IFSFileInputStream inputStream_;


  /**
   Constructs an IFSFileReader object. 
   Other readers and writers are allowed to access the file.
   The file is opened if it exists; otherwise an exception is thrown.
   The file's CCSID is obtained by referencing the file's "coded character set ID" tag on the system.
   Other readers and writers are allowed to access the file.
   @param file The file to be opened for reading.

   @exception AS400SecurityException If  a security or authority error occurs.
   @exception IOException If an error occurs while communicating with the system.
   **/
  public IFSFileReader(IFSFile file)
    throws AS400SecurityException, IOException
  {
    if (file == null) throw new NullPointerException("file");
    int ccsid = file.getCCSID();  // do this before opening the stream, to avoid "File In Use"
    if (ccsid == -1) throwException(file.getPath());
    inputStream_ = new IFSFileInputStream(file);
    reader_ = new ConvTableReader(inputStream_, ccsid);
  }


  /**
   Constructs an IFSFileReader object.
   The file is opened if it exists; otherwise an exception is thrown.
   Other readers and writers are allowed to access the file.
   @param file The file to be opened for reading.
   @param ccsid The CCSID that the file data is currently in.

   @exception AS400SecurityException If  a security or authority error occurs.
   @exception IOException If an error occurs while communicating with the system.
   **/
  public IFSFileReader(IFSFile file, int ccsid)
    throws AS400SecurityException, IOException
  {
    if (file == null) throw new NullPointerException("file");
    inputStream_ = new IFSFileInputStream(file);
    reader_ = new ConvTableReader(inputStream_, ccsid);
  }


  /**
   Constructs an IFSFileReader object.
   The file is opened if it exists; otherwise an exception is thrown.
   @param file The file to be opened for reading.
   @param ccsid The CCSID that the file data is currently in.
   @param shareOption Indicates how users can access the file. <ul><li>SHARE_ALL Share access with readers and writers<li>SHARE_NONE Share access with none<li>SHARE_READERS Share access with readers<li>SHARE_WRITERS Share access with writers</ul>

   @exception AS400SecurityException If  a security or authority error occurs.
   @exception IOException If an error occurs while communicating with the system.
   **/
  public IFSFileReader(IFSFile file, int ccsid, int shareOption)
    throws AS400SecurityException, IOException
  {
    if (file == null) throw new NullPointerException("file");
    inputStream_ = new IFSFileInputStream(file, shareOption);
    reader_ = new ConvTableReader(inputStream_, ccsid);
  }

  /**
   Constructs an IFSFileReader object. 
   The file is opened if it exists; otherwise an exception is thrown.
   @param fd The file descriptor to be opened for reading.
   **/
  public IFSFileReader(IFSFileDescriptor fd)
    throws AS400SecurityException, IOException
  {
    int ccsid = fd.getCCSID();  // do this before opening the stream, to avoid "File In Use"
    if (ccsid == -1) throwException(fd.getPath());
    inputStream_ = new IFSFileInputStream(fd);
    reader_ = new ConvTableReader(inputStream_, ccsid);
    // Note: IFSFileDescriptor has a shareOption data member.
  }


  /**
   Closes the stream.  Once a stream has been closed, further read(), ready(), mark(), or reset() invocations will throw an IOException.  Closing a previously-closed stream, however, has no effect.
   @exception IOException If an error occurs while communicating with the system.
   **/
  public void close()
    throws IOException
  {
    reader_.close();  // let the Reader close the IFSFileInputStream
  }

  /**
   Returns the CCSID used by this IFSFileReader.
   @return  The CCSID, or -1 if the CCSID is not known.
   **/
  public int getCCSID()
  {
    return reader_.getCcsid();
  }

  /**
   Returns the encoding used by this IFSFileReader.
   @return  The encoding, or null if the encoding is not known.
   **/
  public String getEncoding()
  {
    return reader_.getEncoding();
  }


  /**
   IFSFileReader does not support the mark() operation.
   @return  false.
   **/
  public boolean markSupported()
  {
    return false;
  }


  /**
   Reads a single character.
   @return The character read, or -1 if the end of the stream has been reached.
   @exception IOException If an error occurs while communicating with the system.
   **/
  public int read() throws IOException
  {
    return reader_.read();
  }


  /**
   Reads characters into an array.  This method will block until some input is available, an I/O error occurs, or the end of the stream is reached.
   @param cbuf Destination buffer.
   @return The number of characters read, or -1 if the end of the stream has been reached.

   @exception IOException If an error occurs while communicating with the system.
   **/
  public int read(char[] cbuf) throws IOException
  {
    return reader_.read(cbuf);
  }

  /**
   Reads characters into a portion of an array.  This method will block until some input is available, an I/O error occurs, or the end of the stream is reached.
   @param cbuf Destination buffer.
   @param off Offset at which to start storing characters.
   @param len Maximum number of characters to read.
   @return The number of characters read, or -1 if the end of the stream has been reached.

   @exception IOException If an error occurs while communicating with the system.
   **/
  public int read(char[] cbuf, int off, int len)
    throws IOException
  {
    return reader_.read(cbuf, off, len);
  }


  /**
   Tells whether this stream is ready to be read.
   @return <tt>true</tt> if the next read() is guaranteed not to block for input, <tt>false</tt> otherwise.  Note that returning false does not guarantee that the next read will block.
   @exception IOException If an error occurs while communicating with the system.
   **/
  public boolean ready()
    throws IOException
  {
    return reader_.ready();
  }


  /**
   Resets the stream.  After this call, the stream will read from the beginning.
   @exception IOException If an error occurs while communicating with the system.
   **/
  public void reset()
    throws IOException
  {
    // Just create a new ConvTableReader.  We thereby avoid complications that arise when reset() is supported but mark() isn't.

    // Let the ConvTableReader finish whatever it's doing.
    synchronized(inputStream_)
    {
      inputStream_.reset();
      reader_ = new ConvTableReader(inputStream_, reader_.getCcsid());
    }
  }


  /**
   Skip characters.  This method will block until some characters are available, an I/O error occurs, or the end of the stream is reached.

   @param charsToSkip The number of characters to skip.
   @return The number of characters actually skipped.

   @exception IOException If an error occurs while communicating with the system.
   **/
  public long skip(long charsToSkip)
    throws IOException
  {
    return reader_.skip(charsToSkip);
  }


  /**
   Places a lock on the file at the current position for the specified
   number of bytes.
   @param length The number of bytes to lock.
   @return The key for undoing this lock.
 
   @exception IOException If an error occurs while communicating with the system.
   @see #unlockBytes
   **/
  public IFSKey lockBytes(int length)
    throws IOException
  {
    return inputStream_.lock(length);
  }

  /**
   Undoes a lock on the file.
   @param key The key for the lock.

   @exception IOException If an error occurs while communicating with the system.
 
   @see #lockBytes
   **/
  public void unlockBytes(IFSKey key)
    throws IOException
  {
    inputStream_.unlock(key);
  }


  // Utility method.
  private static final void throwException(String path)
    throws ExtendedIOException
  {
    Trace.log(Trace.ERROR, "File does not exist or is not readable.");
    throw new ExtendedIOException(path, ExtendedIOException.FILE_NOT_FOUND);
  }

}
