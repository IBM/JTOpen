///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: AS400FileRecordDescription.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.Serializable;
import java.net.UnknownHostException;
import java.util.Vector;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport; //@C0A
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;

/**
 *The AS400FileRecordDescription class represents the record descriptions of an AS/400 physical
 *or logical file.  This class is used to retrieve the file field description
 *of an AS/400 physical or logical file, and to create Java source code
 *for a class extending from
 *<a href = "com.ibm.as400.access.RecordFormat.html">RecordFormat</a> that
 *can then be compiled and used as input to the
 *<a href="com.ibm.as400.access.AS400File.html#setRecordFormat">AS400File.setRecordFormat()</a>
 *method.
 *This allows the record format to be created statically during
 *development time and then reused when needed.
 *The class also provides a method for returning RecordFormat objects
 *that can be used as input to the AS400File.setRecordFormat() method.
 *This method can be used to create the record format dynamically.
 *<p>The output from the <a href="#createRecordFormatSource">createRecordFormatSource()</a>
 *and
 *<a href="#retrieveRecordFormat">retrieveRecordFormat()</a> methods
 *contains enough information to use to describe the record format of the
 *existing AS/400 file from which it was generated.  The record formats
 *generated are not meant for creating files with the same format as the
 *file from which they are retrieved.  Use the AS/400 Copy File (CPYF) command to create
 *a file with the same format as an existing file.
 *<br>
 *AS400FileRecordDescription objects generate the following events:
 *<ul>
 *<li><a href="com.ibm.as400.access.AS400FileRecordDescriptionEvent.html">AS400FileRecordDescriptionEvent</a>
 *<br>The events fired are:
 *<ul>
 *<li>recordFormatRetrieved
 *<li>recordFormatSourceCreated
 *</ul>
 *<li><a href="java.beans.PropertyChangeEvent.html">PropertyChangeEvent</a>
 *<li><a href="java.beans.VetoableChangeEvent.html">VetoableChangeEvent</a>
 *</ul>
**/
public class AS400FileRecordDescription implements Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

  // File name
  private String file_ = "";
  // Library name
  //@C0D private String library_ = "";
  // member name
  private String member_ = "";
  // The IFS path name of the file
  private String name_ = "";
  // The AS/400 the file is on
  private AS400 system_ = null;

  // The list of AS400FileRecordDescriptionEvent listeners
  transient Vector rdeListeners_;
  // Use default property change support
  transient PropertyChangeSupport changes_;
  // Use default veto change support
  transient VetoableChangeSupport vetos_; //@C0C

  // The impl.
  transient private AS400FileRecordDescriptionImpl impl_; //@C0A

  /**
   *Constructs an AS400FileRecordDescription object.
   *The system on which the file resides and the name of the
   *file must be set prior to invoking any other method in the class.
   *@see AS400FileRecordDescription#setSystem
   *@see AS400FileRecordDescription#setPath
  **/
  public AS400FileRecordDescription()
  {
    initializeTransient();
  }

  /**
   *Constructs an AS400FileRecordDescription object. It uses the specified system on
   *which the file resides and the
   *integrated file system path name of
   *the file.
   *@param system The AS/400 system on which the file resides.
   *@param name The integrated file system path name
   *of the file.  If a member is not specified in <i>name</i>, the first
   *member of the file is used.
  **/
  public AS400FileRecordDescription(AS400 system, String name)
  {
    if (system == null)
    {
      throw new NullPointerException("system");
    }
    if (name == null)
    {
      throw new NullPointerException("name");
    }
    initializeTransient();
    name_ = name;
    parseName(); //@C0A
    system_ = system;
  }


  /**
   *Adds a listener to be notified when an AS400FileRecordDescriptionEvent is fired.
   *@see #removeAS400FileRecordDescriptionListener
   *@param listener The As400FileRecordDescriptionListener.
  **/
  public void addAS400FileRecordDescriptionListener(AS400FileRecordDescriptionListener listener)
  {
    if (listener == null)
    {
      throw new NullPointerException("listener");
    }
    rdeListeners_.addElement(listener);
  }

  /**
   *Adds a listener to be notified when the value of any bound
   *property is changed.  The <b>propertyChange</b> method will be
   *be called.
   *@see #removePropertyChangeListener
   *@param listener The PropertyChangeListener.
  **/
  public void addPropertyChangeListener(PropertyChangeListener listener)
  {
    if (listener == null)
    {
      throw new NullPointerException("listener");
    }
    changes_.addPropertyChangeListener(listener);
  }

  /**
   *Adds a listener to be notified when the value of any constrained
   *property is changed.
   *The <b>vetoableChange</b> method will be called.
   *@see #removeVetoableChangeListener
   *@param listener The VetoableChangeListener.
  **/
  public void addVetoableChangeListener(VetoableChangeListener listener)
  {
    if (listener == null)
    {
      throw new NullPointerException("listener");
    }
    vetos_.addVetoableChangeListener(listener); //@C0C
  }


  /**
    Chooses the appropriate implementation.
  **/
  private void chooseImpl() //@C0A
  {
    impl_ = (AS400FileRecordDescriptionImpl) system_.loadImpl2("com.ibm.as400.access.AS400FileRecordDescriptionImplRemote",
                                                               "com.ibm.as400.access.AS400FileRecordDescriptionImplProxy");
    try                                           //@B5A
    {
      system_.connectService(AS400.RECORDACCESS); //@B5A
    }
    catch(IOException x)                          //@B5A
    {
      if (Trace.isTraceOn() && Trace.isTraceErrorOn()) //@B5A
        Trace.log(Trace.ERROR, "Exception when connecting during chooseImpl().", x);
    }
    catch(AS400SecurityException x)               //@B5A
    {
      if (Trace.isTraceOn() && Trace.isTraceErrorOn()) //@B5A
        Trace.log(Trace.ERROR, "Exception when connecting during chooseImpl().", x);
    }
    impl_.setPath(name_);     //@C0A
    impl_.setSystem(system_.getImpl()); //@C0A @B5C
  }


  /**
   *Retrieves the file description for the file, and creates a file containing the Java source for
   *a class extending from RecordFormat that represents the record format for the file.  If the
   *file contains more than one record format (for example, is a multiple format logical file), a Java
   *source file for each record format in the file is created; each file will contain the class
   *definition for a single record format.<br>
   *The name of the class is the name of the record format retrieved with the string "Format"
   *appended to it.  The name of the file is the name of the class with the extension .java.<br>
   *The source files generated can be compiled and used as input to the
   *<a href="com.ibm.as400.access.AS400File.html#setRecordFormat">AS400File.setRecordFormat()</a> method.<br>
   *The AS/400 system to which to connect and the integrated file system
   *pathname for the file must be set prior to invoking this method.
   *@see AS400FileRecordDescription#AS400FileRecordDescription(com.ibm.as400.access.AS400, java.lang.String)
   *@see AS400FileRecordDescription#setPath
   *@see AS400FileRecordDescription#setSystem
   *@param filePath The path in which to create the file.  If <i>filePath</i> is null,
   *the file is created in the current working directory.
   *@param packageName The name of the package in which the class belongs. The <i>packageName</i>
   *is used to specify the package statement in the source code for the class.
   * If this value is null, no package statement is specified in the source code for the class.

   *@exception AS400Exception If the AS/400 system returns an error message.
   *@exception AS400SecurityException If a security or authority error occurs.
   *@exception ConnectionDroppedException If the connection is dropped unexpectedly.
   *@exception IOException If an error occurs while communicating with the
   *AS/400.
   *@exception InterruptedException If this thread is interrupted.
   *@exception ServerStartupException If the AS/400 server cannot be started.
   *@exception UnknownHostException If the AS/400 system cannot be located. 
  **/
  public synchronized void createRecordFormatSource(String filePath, String packageName)
    throws AS400Exception,
           AS400SecurityException,
           IOException,
           InterruptedException
  {
    if (impl_ == null) //@C0A
      chooseImpl();    //@C0A
    String[] filesToWrite = impl_.createRecordFormatSource(packageName); //@C0A

    // Get the file separator for the system on which we are running
    String fileSeparator = System.getProperty("file.separator");
    // Append file separator if necessary to filePath
    if (filePath != null)
    {
      if (filePath.lastIndexOf(fileSeparator) != filePath.length() - fileSeparator.length())
      {
        filePath += fileSeparator;
      }
    }
    else
    {
      filePath = "";
    }
    FileOutputStream os;
    PrintWriter sourceFile;
    String fileName;
    //@C0A
    for (int i=0; i<(filesToWrite.length/2); ++i)
    {
      fileName = filePath + filesToWrite[i*2]; //@C0C
      os = new FileOutputStream(fileName);
      sourceFile = new PrintWriter(os, true);
      sourceFile.print(filesToWrite[(i*2)+1]); //@C0A
      if (sourceFile.checkError())
      {
        sourceFile.close();
        throw new InternalErrorException("Error writing to sourceFile.", InternalErrorException.UNKNOWN);
      }
      sourceFile.close();
    }

    //@C0C
    // Fire RECORD_FORMAT_SOURCE_CREATED event
    Vector targets = (Vector) rdeListeners_.clone();
    AS400FileRecordDescriptionEvent event = new AS400FileRecordDescriptionEvent(this, AS400FileRecordDescriptionEvent.RECORD_FORMAT_SOURCE_CREATED);
    for (int i=0; i<targets.size(); ++i)
    {
      AS400FileRecordDescriptionListener target = (AS400FileRecordDescriptionListener)targets.elementAt(i);
      target.recordFormatSourceCreated(event);
    }
  }


  //@C0A
  /**
   * Used internally to parse the pathname and set the individual
   * library, filename, and member strings.
  **/
  private void parseName()
  {
    // Construct a QSYSObjectPathName object and parse out the library,
    // file and member names
    QSYSObjectPathName ifs = new QSYSObjectPathName(name_);
    if (!(ifs.getObjectType().equals("FILE") || ifs.getObjectType().equals("MBR")))
    { // Invalid object type
      throw new IllegalPathNameException(name_, IllegalPathNameException.OBJECT_TYPE_NOT_VALID);
    }
    // Set the instance data as appropriate
    //library_ = ifs.getLibraryName();
    file_ = ifs.getObjectName();
    if (ifs.getObjectType().equals("FILE"))
    { // No member specified; default member to *FIRST
      member_ = "*FIRST";
    }
    else
    { // Member specified; if special value %FILE% was specified, member name
      // is the file name
      member_ = (ifs.getMemberName().equalsIgnoreCase("*FILE") ? file_ :
                 ifs.getMemberName());
    }
  }

  
  /**
   *Returns the file name.
   *@return The file name.  If the integrated file system pathname has not been
   *set for the object, an empty string is returned.
  **/
  public String getFileName()
  {
    return file_;
  }

  /**
   *Returns the member name.
   *@return The member name.  If the integrated file system pathname has not
   *been set for the object, an empty string is returned.
  **/
  public String getMemberName()
  {
    return member_;
  }


  /**
   *Returns the integrated file system path name
   *for the file as specified on the
   *constructor or the setPath() method.
   *@see AS400FileRecordDescription#AS400FileRecordDescription(com.ibm.as400.access.AS400, java.lang.String)
   *@see AS400FileRecordDescription#setPath
   *@return The integrated file system path name
   *associated with this object.
   *If the integrated file system path name has not been set for the object,
   *an empty string is returned.
  **/
  public String getPath()
  {
    return name_;
  }

  /**
   *Returns the AS400 system object for this object.
   *@see AS400FileRecordDescription#AS400FileRecordDescription(com.ibm.as400.access.AS400, java.lang.String)
   *@see AS400FileRecordDescription#setSystem
   *@return The AS400 system for this object.  If the system has not been set,
   *null is returned.
  **/
  public AS400 getSystem()
  {
    return system_;
  }


  /**
   * Initialize the transient data.
  **/
  private void initializeTransient()
  {
    changes_ = new PropertyChangeSupport(this);
    vetos_ = new VetoableChangeSupport(this); //@C0C    
    rdeListeners_ = new Vector();
    impl_ = null; //@C0A
  }


  /**
   *Overrides the ObjectInputStream.readObject() method in order to return any
   *transient parts of the object to there properly initialized state.
   * I.e we in effect
   *call the null constructor.  By calling ObjectInputStream.defaultReadObject()
   *we restore the state of any non-static and non-transient variables.  We
   *then continue on to restore the state (as necessary) of the remaining varaibles.
   *@param in The input stream from which to deserialize the object.
   *@exception ClassNotFoundException If the class being deserialized is not found.
   *@exception IOException If an error occurs while communicating with the AS/400.
  **/

  private void readObject(java.io.ObjectInputStream in)
    throws ClassNotFoundException,
           IOException
  {
    in.defaultReadObject();
    initializeTransient();
  }

  /**
   *Removes a listener from the AS400FileRecordDescription listeners list.
   *@see #addAS400FileRecordDescriptionListener
   *@param listener The AS400FileRecordDescriptionListener.
  **/
  public void removeAS400FileRecordDescriptionListener(AS400FileRecordDescriptionListener listener)
  {
    if (listener == null)
    {
      throw new NullPointerException("listener");
    }
    rdeListeners_.removeElement(listener);
  }

  /**
   *Removes a listener from the change list.
   *@see #addPropertyChangeListener
   *@param listener The PropertyChangeListener.
  **/
  public void removePropertyChangeListener(PropertyChangeListener listener)
  {
    if (listener == null)
    {
      throw new NullPointerException("listener");
    }
    changes_.removePropertyChangeListener(listener);
  }

  /**
   *Removes a listener from the veto change listeners list.
   *@see #addVetoableChangeListener
   *@param listener The VetoableChangeListener.
  **/
  public void removeVetoableChangeListener(VetoableChangeListener listener)
  {
    if (listener == null)
    {
      throw new NullPointerException("listener");
    }
    vetos_.removeVetoableChangeListener(listener); //@C0C
  }

  /**
   *Retrieves the file description for the file, and creates a RecordFormat
   *object for each record format, which can be used as input to the
   *<a href="com.ibm.as400.access.AS400File.html#setRecordFormat">AS400File.setRecordFormat()</a>
   *method.  If the file is a physical file, the RecordFormat array returned
   *contains one
   *RecordFormat object.  If the file is a multiple format logical file, the
   *RecordFormat array may contain
   *more than one RecordFormat object.
   *The AS/400 system to which to connect and the integrated file system
   *pathname for the file must be set prior to invoking this method.
   *@see AS400FileRecordDescription#AS400FileRecordDescription(com.ibm.as400.access.AS400, java.lang.String)
   *@see AS400FileRecordDescription#setPath
   *@see AS400FileRecordDescription#setSystem

   *@return The record format(s) for the file.

   *@exception AS400Exception If the AS/400 system returns an error message.
   *@exception AS400SecurityException If a security or authority error occurs.
   *@exception ConnectionDroppedException If the connection is dropped unexpectedly.
   *@exception IOException If an error occurs while communicating with the
   *AS/400.
   *@exception InterruptedException If this thread is interrupted.
   *@exception ServerStartupException If the AS/400 server cannot be started.
   *@exception UnknownHostException If the AS/400 system cannot be located.
  **/
  public synchronized RecordFormat[] retrieveRecordFormat()
    throws AS400Exception,
           AS400SecurityException,
           InterruptedException,
           IOException
  {
    if (impl_ == null)
      chooseImpl();
    RecordFormat[] rfs = impl_.retrieveRecordFormat();
    
    //@B5A - need to finish filling in the AS400Text objects
    // now that we're back on the client
    for (int i=0; rfs != null && i<rfs.length; ++i)
    {
      rfs[i].initializeTextObjects(system_); //@D0C
    }
          
    //@C0C
    // Fire RECORD_FORMAT_RETRIEVED event
    Vector targets = (Vector) rdeListeners_.clone();
    AS400FileRecordDescriptionEvent event = new AS400FileRecordDescriptionEvent(this, AS400FileRecordDescriptionEvent.RECORD_FORMAT_RETRIEVED);
    for (int i=0; i<targets.size(); ++i)
    {
      AS400FileRecordDescriptionListener target = (AS400FileRecordDescriptionListener)targets.elementAt(i);
      target.recordFormatRetrieved(event);
    }

    return rfs;
  }


  /**
   *Sets the integrated file system path name for
   *the file.
   *@param name The integrated file system path name
   *of the file.  If a member is not specified in <i>name</i>, the first
   *member of the file is used.
   *@exception PropertyVetoException If a change is vetoed.
  **/
  public void setPath(String name)
    throws PropertyVetoException
  {
    // Verify parameters
    if (name == null)
    {
      throw new NullPointerException("name");
    }

    String oldName = name_;

    //@C0C
    // Notify veto listeners of the change
    vetos_.fireVetoableChange("path", oldName, name);

    name_ = name;
    parseName(); //@C0A

    if (impl_ != null) impl_.setPath(name_); //@C0A
    changes_.firePropertyChange("path", oldName, name); //@C0C
  }

  /**
   *Sets the system to which to connect.
   *@param system The system to which to conenct.
   *@exception PropertyVetoException If a change is vetoed.
  **/
  public void setSystem(AS400 system)
    throws PropertyVetoException
  {
    // Verify parameters
    if (system == null)
    {
      throw new NullPointerException("system");
    }

    //@C0C
    // Notify veto listeners of the change
    AS400 old = system_;
    vetos_.fireVetoableChange("system", old, system);

    system_ = system;
    if (impl_ != null) impl_.setSystem(system_.getImpl()); //@C0A @B5C
    changes_.firePropertyChange("system", old, system_);
  }
}
