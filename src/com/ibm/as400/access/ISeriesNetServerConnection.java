///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (AS/400 Toolbox for Java - OSS version)
//
// Filename: ISeriesNetServerConnection.java
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
 The ISeriesNetServerConnection class represents a NetServer share connection.
 <p>
 Note: A <b>session</b> (represented by class {@link ISeriesNetServerSession ISeriesNetServerSession}) corresponds to a workstation.  A workstation could be a Windows Terminal Server or it could be a single PC on someone's desktop.  A <b>connection</b> (represented by this class) corresponds to a specific user who has mapped a drive and has files opened or spooled output on a print queue.  Since a <b>session</b> can have multiple users, a <b>connection</b> shows a particular user's statistics on that session.
 <p>
 ISeriesNetServerConnection objects are created and returned by the following methods:
 <ul>
 <li>{@link ISeriesNetServer#listConnectionsForSession(long) listConnectionsForSession}
 <li>{@link ISeriesNetServer#listConnectionsForSession(String) listConnectionsForSession}
 <li>{@link ISeriesNetServer#listConnectionsForShare(String) listConnectionsForShare}
 </ul>

<blockquote>
<pre>
* import com.ibm.as400.access.*;
*
* // Create a ISeriesNetServer object for a specific server system.
* AS400 system = new AS400("MYSYSTEM", "MYUSERID", "MYPASSWORD");
* ISeriesNetServer ns = new ISeriesNetServer(system);
*
* try
* {
*   // List all current session connections.
*   System.out.println("Session connections:");
*   ISeriesNetServerSession[] sessionList = ns.listSessions();
*   ISeriesNetServerConnection[] connectionList =
*                 ns.listConnectionsForSession(sessionList[0].getID());
*   for (int i=0; i&lt;connectionList.length; i++)
*   {
*     ISeriesNetServerConnection connection = connectionList[i];
*     System.out.println(connection.getID() + ": " +
*       connection.getName() + "; " +
*       connection.getUserName() + "; " +
*       connection.getAge());
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

 @see ISeriesNetServer#listConnectionsForSession(long)
 @see ISeriesNetServer#listConnectionsForSession(String)
**/

public class ISeriesNetServerConnection implements java.io.Serializable
{
  static final long serialVersionUID = 1L;

  /**
   Value of the ISeriesNetServerConnection "resource type" attribute, indicating that the connection is from a specific workstation.
   **/
  public static final int WORKSTATION = 0;

  /**
   Value of the ISeriesNetServerConnection "resource type" attribute, indicating that the connection is to a specific share.
   **/
  public static final int SHARE = 1;

  /**
   Value of the ISeriesNetServerConnection "connection type" attribute, indicating that the connection is to a disk drive, that is, a file share.
   **/
  public static final int DISK_DRIVE = 0;

  /**
   Value of the ISeriesNetServerConnection "connection type" attribute, indicating that the connection is to a spooled output queue, that is, a print share.
   **/
  public static final int SPOOLED_OUTPUT_QUEUE = 1;

  //----------------------------------------------------------------------
  // Private data.
  //----------------------------------------------------------------------

  private ISeriesNetServer netServer_;

  private int connectTime_;
  private int numberOfFilesOpen_;
  private String resourceName_;   // either "share name" or "workstation name"
  private int connectionType_;
  private String userName_;
  private int numberOfUsers_;
  private int connectionID_;
  private long sessionID_;
  private int resourceType_;  // SHARE or WORKSTATION


  // Note: This constructor is reserved for use by the ISeriesNetServer class.
  ISeriesNetServerConnection(int connectionID, int connectTime, int filesOpen, int connType, String resourceName, int resourceType, String userName, int userCount, long sessionID)
  {
    // Assume the caller has already validated the arguments (non-null, etc).
    setAttributeValues(connectionID, connectTime, filesOpen, connType, resourceName, resourceType, userName, userCount, sessionID);
  }


  // Note: This method is reserved for use by the ISeriesNetServer class.
  void setAttributeValues(int connectionID, int connectTime, int filesOpen, int connType, String resourceName, int resourceType, String userName, int userCount, long sessionID)
  {
    // Assume the caller has already validated the arguments (non-null, etc).
    resourceName_ = resourceName;
    connectionID_ = connectionID;
    connectTime_ = connectTime;
    numberOfFilesOpen_ = filesOpen;
    connectionType_ = connType;
    userName_ = userName;
    numberOfUsers_ = userCount;
    sessionID_ = sessionID;

    resourceType_ = resourceType;
  }


  /**
   Returns the connection ID.
   @return  The connection ID.
   **/
  public int getID()
  {
    return connectionID_;
  }

  /**
   Returns the session ID for connection.
   @return  The session ID.
   **/
  public long getSessionID()
  {
    return sessionID_;
  }


  /**
   Returns the number of seconds elapsed since the connection was established.
   @return  The connect time.
   **/
  public int getAge()
  {
    return connectTime_;
  }


  /**
   Returns the number of files that are open currently (on the connection).
   @return  The number of files open.
   **/
  public int getNumberOfFilesOpen()
  {
    return numberOfFilesOpen_;
  }


  /**
   Returns the network name of the resource.
   If the resource type is {@link #SHARE SHARE}, this is the name of a share.
   If the resource type is {@link #WORKSTATION WORKSTATION}, this is the name of a workstation.
   @return The name of the share or workstation.
   **/
  public String getName()
  {
    return resourceName_;
  }


  /**
   Returns the type of resource for this connection.
   Possible values are {@link #WORKSTATION WORKSTATION} and {@link #SHARE SHARE}.
   @return The resource type.
   **/
  public int getResourceType()
  {
    return resourceType_;
  }



  /**
   Returns the type of the connection made from the workstation to the shared resource.
   Possible values are {@link #DISK_DRIVE DISK_DRIVE} and {@link #SPOOLED_OUTPUT_QUEUE SPOOLED_OUTPUT_QUEUE}.
   @return The connection type.
   **/
  public int getConnectionType()
  {
    return connectionType_;
  }


  /**
   Returns the name of the user that is associated with the connection.
   @return The user name.
   **/
  public String getUserName()
  {
    return userName_;
  }


  /**
   Returns the number of current users on the connection.
   @return The number of users.
   **/
  public int getNumberOfUsers()
  {
    return numberOfUsers_;
  }

}
