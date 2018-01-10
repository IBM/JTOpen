///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename: ISeriesNetServerSession.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2000 International Business Machines Corporation and
// others. All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 The ISeriesNetServerSession class represents a NetServer session.
 <p>
 Note: A <b>session</b> (represented by this class) corresponds to a workstation.  A workstation could be a Windows Terminal Server or it could be a single PC on someone's desktop.  A <b>connection</b> (represented by class {@link NetServerConnection NetServerConnection}) corresponds to a specific user who has mapped a drive and has files opened or spooled output on a print queue.  Since a <b>session</b> can have multiple users, a <b>connection</b> shows a particular user's statistics on that session.
 <p>
 ISeriesNetServerSession objects are created and returned by {@link ISeriesNetServer#listSessions() listSessions()} and {@link ISeriesNetServer#listSessionsForWorkstation(String) listSessionsForWorkstation()}.

<blockquote>
<pre>
* import com.ibm.as400.access.*;
*
* // Create an ISeriesNetServer object for a specific system.
* AS400 system = new AS400("MYSYSTEM", "MYUSERID", "MYPASSWORD");
* ISeriesNetServer ns = new ISeriesNetServer(system);
*
* try
* {
*   // List all current sessions.
*   System.out.println("Sessions:");
*   ISeriesNetServerSession[] sessionList = ns.listSessions();
*   for (int i=0; i&lt;sessionList.length; i++)
*   {
*     ISeriesNetServerSession session = sessionList[i];
*     System.out.println(session.getName() + ": " +
*       session.getUserName() + "; " +
*       session.getAge() + "; " +
*       session.isGuest() );
*   }
* }
* catch (AS400Exception e) {
*   AS400Message[] messageList = e.getAS400MessageList();
*   for (int i=0; i&lt;messageList.length; i++) {
*     System.out.println(messageList[i].getText());
*   }
* }
* catch (Exception e) {
*   e.printStackTrace();
* }
* finally {
*   if (system != null) system.disconnectAllServices();
* }
</pre>
</blockquote>

**/


public class ISeriesNetServerSession implements java.io.Serializable
{
    static final long serialVersionUID = 1L;

    // Private data.

    private String workstationName_;  // Uniquely identifies a session on the system.
    private long sessionID_;          // This also uniquely identifies a session on the system.
    private int numberOfConnections_;
    private int sessionAge_;
    private int numberOfFilesOpen_;
    private int idleTime_;
    private boolean isPasswordEncrypted_;
    private boolean isGuestSession_;
    private String userName_;


  // This method is reserved for use by the ISeriesNetServer class.
  ISeriesNetServerSession(String workstation, long sessionID, int numConnections, int sessionAge, int numFilesOpen, int idleTime, boolean isEncryptedPwd, boolean isGuest, String user)
  {
    setAttributeValues(workstation, sessionID, numConnections, sessionAge, numFilesOpen, idleTime, isEncryptedPwd, isGuest, user);
  }


  // This method is reserved for use by the ISeriesNetServer class.
  void setAttributeValues(String workstation, long sessionID, int numConnections, int sessionAge, int numFilesOpen, int idleTime, boolean isEncryptedPwd, boolean isGuest, String user)
  {
    workstationName_ = workstation;
    sessionID_ = sessionID;
    numberOfConnections_ = numConnections;
    sessionAge_ = sessionAge;
    numberOfFilesOpen_ = numFilesOpen;
    idleTime_ = idleTime;
    isPasswordEncrypted_ = isEncryptedPwd;
    isGuestSession_ = isGuest;
    userName_ = user;
  }


  /**
   Returns the name of the workstation from which the session to the system was established.
   @return The workstation name.
   **/
  public String getName()
  {
    return workstationName_;
  }


  /**
   Returns the unique identifier for the system session.
   Note: The session identifier was added to the NetServer API's in V5R1.
   @return The session identifier.
   **/
  public long getID()
  {
    return sessionID_;
  }


  /**
   Returns the number of connections made during the session.
   @return The number of connections.
   **/
  public int getNumberOfConnections()
  {
    return numberOfConnections_;
  }


  /**
   Returns the number of seconds that the session has been active.
   @return The session time.
   **/
  public int getAge()
  {
    return sessionAge_;
  }


  /**
   Returns the number of files that are currently open for the session.
   @return The files open count.
   **/
  public int getNumberOfFilesOpen()
  {
    return numberOfFilesOpen_;
  }


  /**
   Returns the number of seconds the session has been idle.
   @return The session idle time.
   **/
  public int getIdleTime()
  {
    return idleTime_;
  }


  /**
   Indicates whether or not the encrypted password was used to establish the session.
   @return <tt>true</tt> if the encrypted password was used.
   **/
  public boolean isPasswordEncrypted()
  {
    return isPasswordEncrypted_;
  }


  /**
   Indicates whether or not the session is a guest session.
   @return <tt>true</tt> if the session is a guest session.
   **/
  public boolean isGuest()
  {
    return isGuestSession_;
  }


  /**
   Returns the name of the user that is associated with the session.
   @return The user name.
   **/
  public String getUserName()
  {
    return userName_;
  }


  // Implementation note: The API's also define a "session count" attribute.
  // This attribute represents the number of sessions that are established between the server and the requester.  This value is always 0 or 1.
  // Since this attribute seems to be of very limited value, we won't surface it unless someone says they need it.

}
