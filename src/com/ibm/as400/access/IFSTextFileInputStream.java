///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: IFSTextFileInputStream.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2004 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.io.InputStreamReader;


/**
 The IFSTextFileInputStream class represents an integrated file system
input stream for character data.
<br>
IFSTextFileInputStream objects are capable of generating file events 
which call the following FileListener methods: fileClosed and fileOpened.
<br>
The following example illustrates the use of IFSTextFileInputStream:
<pre>
// Work with /File on the system eniac.
AS400 as400 = new AS400("eniac");
IFSTextFileInputStream file = new IFSTextFileInputStream(as400, "/File");<br>
// Read the first four characters of the file.
String s = file.read(4);
// Display the characters read.
System.out.println(s);
// Close the file.
file.close();
</pre>
@see com.ibm.as400.access.FileEvent
@see com.ibm.as400.access.IFSFileInputStream#addFileListener
@see com.ibm.as400.access.IFSFileInputStream#removeFileListener
@deprecated Use {@link IFSFileReader IFSFileReader} instead.
 **/
public class IFSTextFileInputStream extends IFSFileInputStream
  implements java.io.Serializable
{
  private static final String copyright = "Copyright (C) 1997-2004 International Business Machines Corporation and others.";


   
    static final long serialVersionUID = 4L;

  /**
   Constructs an IFSTextFileInputStream object.
   **/
  public IFSTextFileInputStream()
  {
    super();
  }

  /**
   Constructs an IFSTextFileInputStream object. 
   It creates a file input stream to read from the text file <i>name</i>.
   Other readers and writers are allowed to access the file.  The file is
   opened if it exists; otherwise an exception is thrown.
   @param system The AS400 that contains the file.
   @param name The integrated file system name.

   @exception AS400SecurityException If  a security or authority error occurs.
   @exception IOException If an error occurs while communicating with the server.
   **/
  public IFSTextFileInputStream(AS400  system,
                                String name)
    throws AS400SecurityException, IOException
  {
    super(system, name);
  }


  /**
   Constructs an IFSTextFileInputStream object. 
   It creates a file input stream to read from the text file <i>name</i>.
   @param system The AS400 that contains the file.
   @param name The integrated file system name.
   @param shareOption Indicates how users can access the file. <ul><li>SHARE_ALL Share access with readers and writers<li>SHARE_NONE Share access with none<li>SHARE_READERS Share access with readers<li>SHARE_WRITERS Share access with writers</ul>

   @exception AS400SecurityException If  a security or authority error occurs.
   @exception IOException If an error occurs while communicating with the server.
   **/
   public IFSTextFileInputStream(AS400  system,
                                String name,
                                int    shareOption)
    throws AS400SecurityException, IOException
  {
    super(system, name, shareOption);
  }

   // @A5a
  /**
   Constructs an IFSTextFileInputStream object. 
   It creates a file input stream to read from the text file specified by <i>file</i>.
   Other readers and writers are allowed to access the file.  The file is
   opened if it exists; otherwise an exception is thrown.
   @param file The file to be opened for reading.

   @exception AS400SecurityException If  a security or authority error occurs.
   @exception IOException If an error occurs while communicating with the server.
   **/
  public IFSTextFileInputStream(IFSFile file)
    throws AS400SecurityException, IOException
  {
    super(file);
  }

  /**
   Constructs an IFSTextFileInputStream object. 
   It creates a file input stream to read from the text file specified by <i>file</i>.
   @param system The AS400 that contains the file.
   @param file The file to be opened for reading.
   @param shareOption Indicates how users can access the file. <ul><li>SHARE_ALL Share access with readers and writers<li>SHARE_NONE Share access with none<li>SHARE_READERS Share access with readers<li>SHARE_WRITERS Share access with writers</ul>

   @exception AS400SecurityException If  a security or authority error occurs.
   @exception IOException If an error occurs while communicating with the server.
   **/
  public IFSTextFileInputStream(AS400   system,
                                IFSFile file,
                                int     shareOption)
    throws AS400SecurityException, IOException
  {
    super(system, file, shareOption);
  }

  // @A5a
  /**
   Constructs an IFSTextFileInputStream object. 
   It creates a file input stream to read from the text file specified by <i>file</i>.
   Other readers and writers are allowed to access the file.  The file is
   opened if it exists; otherwise an exception is thrown.
   @param file The file to be opened for reading.

   @exception AS400SecurityException If  a security or authority error occurs.
   @exception IOException If an error occurs while communicating with the server.
   **/
  public IFSTextFileInputStream(IFSJavaFile file)
    throws AS400SecurityException, IOException
  {
    super(file);
  }

  // @A2A
  /**
   Constructs an IFSTextFileInputStream object. 
   It creates a file input stream to read from the text file specified by <i>file</i>.
   @param system The AS400 that contains the file.
   @param file The file to be opened for reading.
   @param shareOption Indicates how users can access the file. <ul><li>SHARE_ALL Share access with readers and writers<li>SHARE_NONE Share access with none<li>SHARE_READERS Share access with readers<li>SHARE_WRITERS Share access with writers</ul>

   @exception AS400SecurityException If  a security or authority error occurs.
   @exception IOException If an error occurs while communicating with the server.
   **/
  public IFSTextFileInputStream(AS400   system,
                                IFSJavaFile file,
                                int     shareOption)
    throws AS400SecurityException, IOException
  {
    super(system, file, shareOption);
  }

  /**
   Constructs an IFSTextFileInputStream object. 
   It creates a text file input stream to read from file descriptor <i>fd</i>.
   @param fd The file descriptor to be opened for reading.
   **/
  public IFSTextFileInputStream(IFSFileDescriptor fd)
  {
    super(fd);
  }


  /**
   Returns the implementation object.
   @return The implementation object associated with this stream.
   **/
  IFSFileInputStreamImpl getImpl()
  {
    return super.getImpl();  // Note: This may be null.
  }


  /**
   Reads up to <i>length</i> characters from this text file input stream.
   The file contents are converted from the file data CCSID to Unicode if
   the encoding is supported.
   @param length The number of characters to read from the stream.
   @return The characters read from the stream.  If the end of file has been
   reached an empty String is returned.

   @exception IOException If an error occurs while communicating with the server.
   **/
  public String read(int length)
    throws IOException
  {
    String data = "";

    // Validate length.
    if (length < 0)
    {
      throw new ExtendedIllegalArgumentException("length (" +
                                                 Integer.toString(length) +
                                                 ")",
                          ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
    }
    else if (length == 0)
    {
      return new String("");
    }
    else
    {
      // Ensure that the file is open.
      open();

      return impl_.readText(length);
    }
  }

}
