///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: PanelGroup.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2002 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * The PanelGroup class represents an OS/400 panel group (*PNLGRP) object.
 * The help text for a given panel group and set of keywords can be retrieved
 * using the {@link #getHelpText getHelpText} method. 
 * <P>
 * To generate HTML documentation from the panel groups of a given CL command,
 * see the {@link com.ibm.as400.util.CommandHelpRetriever CommandHelpRetriever} utility.
**/
public class PanelGroup implements Serializable
{
  private static final String copyright = "Copyright (C) 1997-2002 International Business Machines Corporation and others.";

  /**
   *  This class uses the QUHRHLPT system API to retrieve the help text.
   **/
  static final long serialVersionUID = 6L;  

  private AS400 system_;
  private String path_;

  // List of property change event bean listeners.
  private transient PropertyChangeSupport propertyChangeListeners_ = new PropertyChangeSupport(this);


  /**
   * Constructs a PanelGroup object.
   **/
  public PanelGroup()
  {
    initializeTransient();
  }

  /**
   * Constructs a PanelGroup object.
   * @param system The server on which the panel group resides.
   * @param path The fully integrated file system path name of the panel group.
   * @see com.ibm.as400.access.QSYSObjectPathName
  **/
  public PanelGroup(AS400 system, String path)
  {
    if (system == null)
      throw new NullPointerException("system");

    if (path == null)
      throw new NullPointerException("path");

    QSYSObjectPathName verify = new QSYSObjectPathName(path, "PNLGRP");

    system_ = system;
    path_ = path;

    initializeTransient();
  }

  /**
   *  Adds a PropertyChangeListener.  The specified PropertyChangeListener's <b>propertyChange</b> method will be called each time the value of any bound property is changed.  
   *  The PropertyChangeListener object is added to a list of PropertyChangeListeners managed by this PanelGroup.  It can be removed with removePropertyChangeListener.
   *
   *  @param  listener  The PropertyChangeListener.
  **/
  public void addPropertyChangeListener(PropertyChangeListener listener)
  {
    if (listener == null)
    {
      throw new NullPointerException("listener");
    }
    propertyChangeListeners_.addPropertyChangeListener(listener);
  }


  /**
   * Retrieves the XML help text from the system for the specified keywords.
   * @param keywords An array of keywords for which to retrieve help. The panel group to which
   * the keywords belong is assumed to be the path of this PanelGroup object.
   * @return The help text.
  **/
  public synchronized String getHelpText(String[] keywords) throws AS400Exception, AS400SecurityException,
  ErrorCompletingRequestException, IOException,
  InterruptedException, ObjectDoesNotExistException

  {
    if (keywords == null) throw new NullPointerException("keywords");
    if (system_ == null) throw new ExtendedIllegalStateException("system", ExtendedIllegalStateException.PROPERTY_NOT_SET);
    if (path_ == null) throw new ExtendedIllegalStateException("path", ExtendedIllegalStateException.PROPERTY_NOT_SET);

    if (Trace.isTraceOn())
    {
      Trace.log(Trace.DIAGNOSTIC, "Retrieving help text for "+keywords.length+" keywords: ");
      for (int i=0; i<keywords.length; ++i)
      {
        Trace.log(Trace.DIAGNOSTIC, keywords[i]);
      }
    }
    
    CharConverter conv = new CharConverter(system_.getCcsid());
    int numHelpIDs = keywords.length;
    int outputLength = 32 + 192*numHelpIDs;
    //int docLength = 113152;
    int docLength = 8192;

    ProgramParameter[] parms = new ProgramParameter[8];
    parms[0] = new ProgramParameter(outputLength);
    parms[1] = new ProgramParameter(BinaryConverter.intToByteArray(outputLength));
    parms[2] = new ProgramParameter(conv.stringToByteArray("RHLP0100"));

    byte[] helpIDData = new byte[numHelpIDs*80];
    int offset = 0;
    AS400Text text32 = new AS400Text(32, system_.getCcsid());
    AS400Text text10 = new AS400Text(10, system_.getCcsid());
    QSYSObjectPathName p = new QSYSObjectPathName(path_);

    byte[] panelGroupName = text10.toBytes(p.getObjectName().toUpperCase().trim());
    byte[] panelGroupLibrary = text10.toBytes(p.getLibraryName().toUpperCase().trim());
    byte[] objectType = text10.toBytes("*PNLGRP");
    byte[] reserved = new byte[] { 0x40, 0x40, 0x40, 0x40, 0x40, 0x40,
      0x40, 0x40, 0x40, 0x40, 0x40, 0x40,
      0x40, 0x40, 0x40, 0x40, 0x40, 0x40};

    for (int i=0; i<numHelpIDs; ++i)
    {
      text32.toBytes(keywords[i], helpIDData, offset);
      offset += 32;
      System.arraycopy(panelGroupName, 0, helpIDData, offset, 10);
      offset += 10;
      System.arraycopy(panelGroupLibrary, 0, helpIDData, offset, 10);
      offset += 10;
      System.arraycopy(objectType, 0, helpIDData, offset, 10);
      offset += 10;
      System.arraycopy(reserved, 0, helpIDData, offset, 18);
      offset += 18;
    }

    parms[3] = new ProgramParameter(helpIDData);
    parms[4] = new ProgramParameter(BinaryConverter.intToByteArray(numHelpIDs));
    parms[5] = new ProgramParameter(docLength);
    parms[6] = new ProgramParameter(BinaryConverter.intToByteArray(docLength));
    parms[7] = new ProgramParameter(4);

    ProgramCall pc = new ProgramCall(system_, "/QSYS.LIB/QUHRHLPT.PGM", parms);

    if (!pc.run())
      throw new AS400Exception(pc.getMessageList());

    byte[] outputData = parms[0].getOutputData();
    byte[] docData = parms[5].getOutputData();
    int bytesReturned = BinaryConverter.byteArrayToInt(outputData, 0);
    int bytesAvailable = BinaryConverter.byteArrayToInt(outputData, 4);
    int docBytesReturned = BinaryConverter.byteArrayToInt(docData, 0);
    int docBytesAvailable = BinaryConverter.byteArrayToInt(docData, 4);
//    System.out.println("bytes returned = "+bytesReturned);
//    System.out.println("bytes available = "+bytesAvailable);
//    System.out.println("doc bytes returned = "+docBytesReturned);
//    System.out.println("doc bytes available = "+docBytesAvailable);
//    System.out.println("doc data = "+docData.length);

    if (bytesReturned < bytesAvailable || docBytesReturned < docBytesAvailable)
    {
      try
      {
        if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Performing secondary program call to retrieve help text.");

        parms[0].setOutputDataLength(bytesAvailable+8);
        parms[1] = new ProgramParameter(BinaryConverter.intToByteArray(bytesAvailable+8));
        parms[5].setOutputDataLength(docBytesAvailable+8);
        parms[6] = new ProgramParameter(BinaryConverter.intToByteArray(docBytesAvailable+8));

        if (!pc.run())
          throw new AS400Exception(pc.getMessageList());

        docData = parms[5].getOutputData();
        docBytesReturned = BinaryConverter.byteArrayToInt(docData, 0);
//        System.out.println("Re-retrieved doc bytes returned = "+docBytesReturned);
//        System.out.println("Re-retrieved doc data = "+docData.length);
      }
      catch (java.beans.PropertyVetoException pve)
      {
      }
    }

    //byte[] helpData = new byte[docBytesReturned];
    ConvTable conv1208 = ConvTable.getTable(1208, null); // UTF-8

    String helpText = docBytesReturned <= 8 ? "" : conv1208.byteArrayToString(docData, 8, docBytesReturned-8, 0);
    //String helpText = docData.length <=8 ? "" : conv1208.byteArrayToString(docData, 8, docData.length-8, 0);
    
    if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Successfully retrieved help text.");
    
    return helpText;
  }


  /**
   * Returns the path name for this panel group.
   * @return The path, or null if no path has been set.
   * @see #setPath
  **/
  public String getPath()
  {
    return path_;
  }

  /**
   * Returns the system object.
   * @return The system, or null if no system has been set.
   * @see #setSystem
  **/
  public AS400 getSystem()
  {
    return system_;
  }


  // Called on construct or after de-serialization
  private void initializeTransient()
  {
    propertyChangeListeners_ = new PropertyChangeSupport(this);
  }


  // Called when this object is de-serialized
  private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException
  {
    in.defaultReadObject();
    initializeTransient();
  }


  /**
   *  Removes the PropertyChangeListener.  If the PropertyChangeListener is not on the list, nothing is done.
   *
   *  @param  listener  The PropertyChangeListener.
   **/
  public void removePropertyChangeListener(PropertyChangeListener listener)
  {
    if (listener == null)
    {
      throw new NullPointerException("listener");
    }
    propertyChangeListeners_.removePropertyChangeListener(listener);
  }


  /**
   * Sets the path name of the panel group.
   * @param path The panel group path.
   * @see #getPath
  **/
  public void setPath(String path)
  {
    if (path == null) throw new NullPointerException("path");

    synchronized(this)
    {
      QSYSObjectPathName verify = new QSYSObjectPathName(path, "PNLGRP");

      String old = path_;
      path_ = path;

      propertyChangeListeners_.firePropertyChange("path", old, path);
    }
  }

  /**
   *  Sets the server from which to retrieve the panel group.
   *
   *  @param  system  The server from which to retrieve the panel group.
   * @see #getSystem
   **/
  public void setSystem(AS400 system)
  {
    if (system == null) throw new NullPointerException("system");

    synchronized(this)
    {
      AS400 old = system_;
      system_ = system;

      propertyChangeListeners_.firePropertyChange("system", old, system);
    }
  }


  /**
   * Returns a String representation for this panel group.
   * @return The string, which includes the fully integrated file system
   * path name of this panel group.
  **/
  public String toString()
  {
    return super.toString()+"["+path_+"]";
  }
}
