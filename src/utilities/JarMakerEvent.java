///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: JarMakerEvent.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package utilities;

/**
   The JarMakerEvent class represents a JarMaker event.
**/
public class JarMakerEvent extends java.util.EventObject
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    String jarEntryName_;

    /**
     * Constructs a JarMakerEvent object.
     * @param source The object where the event originated.
     * @param jarEntryName The name of the jar entry being processed.
     * For example, <code>com/ibm/as400/access/AS400.class</code>
     **/
  public JarMakerEvent (Object source, String jarEntryName)
  {
    super (source); // note: the superclass checks for null
    if (jarEntryName == null) throw new NullPointerException ("jarEntryName");

    jarEntryName_ = jarEntryName;
  }

  /**
    * Returns the name of the jar entry being processed.
    * @return The name of the jar entry being processed.
    * For example, <code>com/ibm/as400/access/AS400.class</code>
    **/
  public String getJarEntryName () { return jarEntryName_; }

  /**
    * Returns the name of the jar entry being processed.
    * @return The name of the jar entry being processed.
    * For example, <code>com/ibm/as400/access/AS400.class</code>
    **/
  public String toString () { return jarEntryName_; }
}
