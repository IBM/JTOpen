///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: JarMakerListener.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package utilities;
/**
   The JarMakerListener interface provides a listener interface for receiving
   JarMaker events.
**/

public interface JarMakerListener extends java.util.EventListener
{
  /**
   * Reports the start of dependency analysis on a specific entry in a jar file.
   * @param event The "analysis started" event.
   **/
  public void dependencyAnalysisStarted (JarMakerEvent event);

  /**
   * Reports completion of dependency analysis on a specific entry in a jar file.
   * @param event The "analysis completed" event.
   **/
  public void dependencyAnalysisCompleted (JarMakerEvent event);
}
