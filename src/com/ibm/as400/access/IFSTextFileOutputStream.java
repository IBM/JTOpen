///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: IFSTextFileOutputStream.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.beans.PropertyVetoException;


/**
The IFSTextFileOutputStream class represents an integrated file system output stream for character data.
<br>
IFSTextFileOutputStream object is capable of generating file events which call the following FileListener methods: fileClosed, fileModified, and fileOpened.
<br>
The following example illustrates the use of IFSTextFileOutputStream:
<pre>
// Work with /File on the system eniac.
AS400 as400 = new AS400("eniac");
IFSTextFileOutputStream file = new IFSTextFileOutputStream(as400, "/File");<br>
// Write a String to the file (don't convert characters).
file.write("Hello world");
// Close the file.
file.close();
</pre>
@see com.ibm.as400.access.FileEvent
@see com.ibm.as400.access.IFSFileOutputStream#addFileListener
@see com.ibm.as400.access.IFSFileOutputStream#removeFileListener
 **/

public class IFSTextFileOutputStream extends IFSFileOutputStream
  implements java.io.Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


  

  /**
   Constructs an IFSTextFileOutputStream object.
   **/
  public IFSTextFileOutputStream()
  {
    super();
  }

  /**
   Constructs an IFSTextFileOutputStream object.
   It creates a file output stream to write to the text file with the specified name.
   Other readers and writers are allowed to access the file.  The file is
   replaced if it exists; otherwise the file is created.
   By default, Unicode data is written to the file.
   @param system The AS400 that contains the file.
   @param name The file to be opened for writing.

   @exception AS400SecurityException If a security or authority error occurs.
   @exception IOException If an error occurs while communicating with the AS/400.
   **/
  public IFSTextFileOutputStream(AS400  system,
                                 String name) 
    throws AS400SecurityException, IOException
  {
    super(system, name);
  }


  // @A1A
  /**
   Constructs an IFSTextFileOutputStream object.
   It creates a file output stream to write to the text file with the specified name and CCSID.
   Other readers and writers are allowed to access the file.  The file is
   replaced if it exists; otherwise the file is created.
   @param system The AS400 that contains the file.
   @param name The file to be opened for writing.
   @param ccsid The CCSID of the data being written to the file.

   @exception AS400SecurityException If a security or authority error occurs.
   @exception IOException If an error occurs while communicating with the AS/400.
   **/
  public IFSTextFileOutputStream(AS400  system,
                                 String name,
                                 int ccsid) 
    throws AS400SecurityException, IOException
  {
    super(system, name, ccsid);
  }


  /**
   Constructs an IFSTextFileOutputStream object.
   It creates a file output stream to write to the text file with the specified name.
   By default, Unicode data is written to the file.
   @param system The AS400 that contains the file.
   @param name The file to be opened for writing.
   @param shareOption Indicates how other user's can access the file. <ul><li>SHARE_ALL Share access with readers and writers<li>SHARE_NONE Share access with none<li>SHARE_READERS Share access with readers<li>SHARE_WRITERS Share access with writers</ul>
   @param append Controls the behavior when the file exists.  If true, output
   is appended to the file;
   otherwise, the current contents of the file are erased,
   and output replaces the file contents.

   @exception AS400SecurityException If a security or authority error occurs.
   @exception IOException If an error occurs while communicating with the AS/400.
   **/
  public IFSTextFileOutputStream(AS400   system,
                                 String  name,
                                 int     shareOption,
                                 boolean append) 
    throws AS400SecurityException, IOException
  {
    super(system, name, shareOption, append);
  }


  // @A1A
  /**
   Constructs an IFSTextFileOutputStream object.
   It creates a file output stream to write to the text file with the specified name and CCSID.
   @param system The AS400 that contains the file.
   @param name The file to be opened for writing.
   @param shareOption Indicates how other user's can access the file. <ul><li>SHARE_ALL Share access with readers and writers<li>SHARE_NONE Share access with none<li>SHARE_READERS Share access with readers<li>SHARE_WRITERS Share access with writers</ul>
   @param append Controls the behavior when the file exists.  If true, output
   is appended to the file;
   otherwise, the current contents of the file are erased,
   and output replaces the file contents.
   @param ccsid The CCSID of the data being written to the file.

   @exception AS400SecurityException If a security or authority error occurs.
   @exception IOException If an error occurs while communicating with the AS/400.
   **/
  public IFSTextFileOutputStream(AS400   system,
                                 String  name,
                                 int     shareOption,
                                 boolean append,
                                 int ccsid) 
    throws AS400SecurityException, IOException
  {
    super(system, name, shareOption, append, ccsid);
  }


  /**
   Constructs an IFSTextFileOutputStream object.
   It creates a file output stream to write to the text file specified by <i>file</i>.
   By default, Unicode data is written to the file.
   @param system The AS400 that contains the file.
   @param file The file to be opened for writing.
   @param shareOption Indicates how other user's can access the file. <ul><li>SHARE_ALL Share access with readers and writers<li>SHARE_NONE Share access with none<li>SHARE_READERS Share access with readers<li>SHARE_WRITERS Share access with writers</ul>
   @param append Controls the behavior when the file exists.  If true, output
   is appended to the file;
   otherwise, the current contents of the file are erased,
   and output replaces the file contents.

   @exception AS400SecurityException If a security or authority error occurs.
   @exception IOException If an error occurs while communicating with the AS/400.
    **/
  public IFSTextFileOutputStream(AS400   system,
                                 IFSFile file,
                                 int     shareOption,
                                 boolean append)
    throws AS400SecurityException, IOException
  {
    super(system, file, shareOption, append);
  }


  // @A1A
  /**
   Constructs an IFSTextFileOutputStream object.
   It creates a file output stream to write to the text file specified by <i>file</i> using the
   CCSID specified by <i>ccsid</i>.
   @param system The AS400 that contains the file.
   @param file The file to be opened for writing.
   @param shareOption Indicates how other user's can access the file. <ul><li>SHARE_ALL Share access with readers and writers<li>SHARE_NONE Share access with none<li>SHARE_READERS Share access with readers<li>SHARE_WRITERS Share access with writers</ul>
   @param append Controls the behavior when the file exists.  If true, output
   is appended to the file;
   otherwise, the current contents of the file are erased,
   and output replaces the file contents.
   @param ccsid The CCSID of the data being written to the file.

   @exception AS400SecurityException If a security or authority error occurs.
   @exception IOException If an error occurs while communicating with the AS/400.
    **/
  public IFSTextFileOutputStream(AS400   system,
                                 IFSFile file,
                                 int     shareOption,
                                 boolean append,
                                 int ccsid)
    throws AS400SecurityException, IOException
  {
    super(system, file, shareOption, append, ccsid);
  }


  // @A2A
  /**
   Constructs an IFSTextFileOutputStream object.
   It creates a file output stream to write to the text file specified by <i>file</i>.
   @param system The AS400 that contains the file.
   @param file The file to be opened for writing.
   @param shareOption Indicates how other user's can access the file. <ul><li>SHARE_ALL Share access with readers and writers<li>SHARE_NONE Share access with none<li>SHARE_READERS Share access with readers<li>SHARE_WRITERS Share access with writers</ul>
   @param append Controls the behavior when the file exists.  If true, output
   is appended to the file;
   otherwise, the current contents of the file are erased,
   and output replaces the file contents.

   @exception AS400SecurityException If a security or authority error occurs.
   @exception IOException If an error occurs while communicating with the AS/400.
    **/
  public IFSTextFileOutputStream(AS400   system,
                                 IFSJavaFile file,
                                 int     shareOption,
                                 boolean append)
    throws AS400SecurityException, IOException
  {
    super(system, file, shareOption, append);
  }


  // @A2A
  /**
   Constructs an IFSTextFileOutputStream object.
   It creates a file output stream to write to the text file specified by <i>file</i> using the
   CCSID specified by <i>ccsid</i>.
   @param system The AS400 that contains the file.
   @param file The file to be opened for writing.
   @param shareOption Indicates how other user's can access the file. <ul><li>SHARE_ALL Share access with readers and writers<li>SHARE_NONE Share access with none<li>SHARE_READERS Share access with readers<li>SHARE_WRITERS Share access with writers</ul>
   @param append Controls the behavior when the file exists.  If true, output
   is appended to the file;
   otherwise, the current contents of the file are erased,
   and output replaces the file contents.
   @param ccsid The CCSID of the data being written to the file.

   @exception AS400SecurityException If a security or authority error occurs.
   @exception IOException If an error occurs while communicating with the AS/400.
    **/
  public IFSTextFileOutputStream(AS400   system,
                                 IFSJavaFile file,
                                 int     shareOption,
                                 boolean append,
                                 int ccsid)
    throws AS400SecurityException, IOException
  {
    super(system, file, shareOption, append, ccsid);
  }


  /**
   Constructs an IFSTextFileOutputStream object.
   It creates a file output stream to write to file descriptor <i>fd</i>.
   @param fd The file descriptor to be opened for writing.
   **/
  public IFSTextFileOutputStream(IFSFileDescriptor fd)
  {
    super(fd);
  }

  /**
   Returns the CCSID.  This is just the bean property, and does not
   necessarily represent the file's actual original CCSID on the AS/400.
   @return The CCSID.
   @see com.ibm.as400.access.IFSFile#getCCSID
   **/
  public int getCCSID()
  {
    return super.getCCSID();
  }


  /**
   Returns the implementation object.
   @return The implementation object associated with this stream.
   **/
  IFSFileOutputStreamImpl getImpl()
  {
    return super.getImpl();  // Note: This may be null.
  }


  /**
   Sets the CCSID for the data written to the file.
   <br>Note: This method is of limited usefulness, since it is invalid after
   a connection has been opened to the file on the AS/400, and most of the
   constructors for this class open a connection.  The preferred way to set
   the CCSID of the file is via a constructor that has a "ccsid" argument.
   @param ccsid The target CCSID.
   @exception PropertyVetoException If the change is vetoed.
   **/
  public void setCCSID(int ccsid)
    throws PropertyVetoException
  {
    super.setCCSID(ccsid);
  }


  /**
   Writes characters to this text file input stream.
   The characters that are written to the file are converted to the
   specified CCSID.  
   @param data The characters to write to the stream.

   @exception IOException If an error occurs while communicating with the AS/400.

   @see #getCCSID
   @see #setCCSID
   **/
  public void write(String data)
    throws IOException
  {
    // Validate length.
    if (data == null)
    {
      throw new NullPointerException("data");
    }

    if (data.length() != 0)
    {
      // Ensure that the file is open.
      open(getCCSID());

      impl_.writeText(data, getCCSID());

      // Notify any listeners that a "modify" event has occurred.
      if (fileListeners_.size() != 0)
        IFSFileDescriptor.fireModifiedEvents(this, fileListeners_);
    }
  }

}
