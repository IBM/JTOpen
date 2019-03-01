///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: NetServerConnection.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import com.ibm.as400.resource.*;
import com.ibm.as400.data.PcmlException;
import com.ibm.as400.data.ProgramCallDocument;
import java.beans.PropertyVetoException;
import java.util.Vector;

/**
 The NetServerConnection class represents a NetServer share connection.
 <p>
 Note: A <b>session</b> (represented by class {@link NetServerSession NetServerSession}) corresponds to a workstation.  A workstation could be a Windows Terminal Server or it could be a single PC on someone's desktop.  A <b>connection</b> (represented by this class) corresponds to a specific user who has mapped a drive and has files opened or spooled output on a print queue.  Since a <b>session</b> can have multiple users, a <b>connection</b> shows a particular user's statistics on that session.
 <p>
 NetServerConnection objects are created and returned by the following methods:
 <ul>
 <li>{@link NetServer#listSessionConnections() NetServer.listSessionConnections}
 <li>{@link NetServer#listShareConnections() NetServer.listShareConnections}
 <li>{@link NetServerShare#listConnections() NetServerShare.listConnections}
 <li>{@link NetServerSession#listConnections() NetServerSession.listConnections}
 </ul>
<p>
<a name="attributeIDs">The following attribute IDs are supported:
<ul>
<li>{@link #CONNECT_TIME CONNECT_TIME}
<li>{@link #FILES_OPEN_COUNT FILES_OPEN_COUNT}
<li>{@link #NAME NAME}
<li>{@link #TYPE TYPE}
<li>{@link #USER USER}
</ul>

<p>Use any of the above attribute IDs with the
{@link com.ibm.as400.resource.ChangeableResource#getAttributeValue(java.lang.Object) getAttributeValue}
method to access the attribute values for a NetServerConnection.
<br>
Note: For the above attributes, getAttributeValue() should never return null.
For String-valued attributes, if the current actual value of the corresponding property on the system is blank, getAttributeValue() will return "" (an empty String).

<blockquote>
<pre>
* import com.ibm.as400.access.*;
* import com.ibm.as400.resource.*;
*
* // Create a NetServer object for a specific system.
* AS400 system = new AS400("MYSYSTEM", "MYUSERID", "MYPASSWORD");
* NetServer ns = new NetServer(system);
*
* try
* {
*
*   // List all current session connections.
*   System.out.println("Session connections:");
*   ResourceList connectionList = ns.listSessionConnections();
*   connectionList.waitForComplete();
*   for (int i=0; i&lt;connectionList.getListLength(); i++)
*   {
*     NetServerConnection connection =
*       (NetServerConnection)connectionList.resourceAt(i);
*     System.out.println(connection.getID() + ": " +
*       (String)connection.getAttributeValue(NetServerConnection.NAME) + "; " +
*       (String)connection.getAttributeValue(NetServerConnection.USER) + "; " +
*       ((Integer)connection.getAttributeValue(NetServerConnection.CONNECT_TIME))
*                                                                 .intValue() );
*   }
* }
* catch (ResourceException e) {
*   e.printStackTrace();
* }
* finally {
*   if (system != null) system.disconnectAllServices();
* }
</pre>
</blockquote>
 @deprecated This class has been replaced by the
 {@link com.ibm.as400.access.ISeriesNetServerConnection ISeriesNetServerConnection}
 class and may be removed in a future release.
 @see NetServer#listSessionConnections()
 @see NetServer#listShareConnections()
 @see NetServerSession
**/

public class NetServerConnection
extends Resource
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


    static final long serialVersionUID = 4L;

  //-------------------------------------------------------------------------
  // Presentation.
  //-------------------------------------------------------------------------

  private static PresentationLoader   presentationLoader_ = new PresentationLoader("com.ibm.as400.access.MRI2");
  private static final String         ICON_BASE_NAME_     = "NetServerConnection";
  private static final String         PRESENTATION_KEY_   = "NETSERVER";

  private static ResourceMetaDataTable attributes_ = new ResourceMetaDataTable(presentationLoader_, PRESENTATION_KEY_);

  // Attribute getter map.
  private static ProgramMap getterMap_  = new ProgramMap();
  private static ProgramMap openListAttributeMap_  = new ProgramMap();


  // Values for specifying "type of connection" when getting lists of connections.
  static final int SHARE       = 0;
  static final int SESSION     = 1;

  private static final String   OLST0600_ = "qzlsolst_zlsl0600"; // session connection info
  private static final String   OLST0700_ = "qzlsolst_zlsl0700"; // share connection info

  private static final int[] INDICES_ = { 0 }; // For specifying first record in list.

  //-------------------------------------------------------------------------
  // Attribute IDs.
  //
  // * If you add an attribute here, make sure and add it to the class javadoc.
  //-------------------------------------------------------------------------

  /**
   Attribute ID for "connect time".  This identifies a read-only Integer
   attribute, which represents the number of seconds that have elapsed since a connection was established.
   **/
  public static final String CONNECT_TIME = "CONNECT_TIME";
  static {
    attributes_.add(CONNECT_TIME, Integer.class, true);
    getterMap_.add (CONNECT_TIME, OLST0600_, "receiverVariable.connectionTime", INDICES_);
    getterMap_.add (CONNECT_TIME, OLST0700_, "receiverVariable.connectionTime", INDICES_);
    openListAttributeMap_.add (CONNECT_TIME, null, "receiverVariable.connectionTime");
  }

  /**
   Attribute ID for "number of files open".  This identifies a read-only Integer
   attribute, which represents the number of files that are currently open on a connection.
   **/
  public static final String FILES_OPEN_COUNT = "FILES_OPEN_COUNT";
  static {
    attributes_.add(FILES_OPEN_COUNT, Integer.class, true);
    getterMap_.add (FILES_OPEN_COUNT, OLST0600_, "receiverVariable.numberOfFilesOpen", INDICES_);
    getterMap_.add (FILES_OPEN_COUNT, OLST0700_, "receiverVariable.numberOfFilesOpen", INDICES_);
    openListAttributeMap_.add (FILES_OPEN_COUNT, null, "receiverVariable.numberOfFilesOpen");
  }

  /**
   Attribute ID for "connection name".  This identifies a read-only String
   attribute, which represents the name of the share or workstation that is associated with a connection.
   **/
  public static final String NAME = "NAME";
  static {
    attributes_.add(NAME, String.class, true);
    getterMap_.add (NAME, OLST0600_, "receiverVariable.resourceName", INDICES_);
    getterMap_.add (NAME, OLST0700_, "receiverVariable.resourceName", INDICES_);
    openListAttributeMap_.add (NAME, null, "receiverVariable.resourceName");
  }

  /**
   Attribute ID for "connection type".  This identifies a read-only Integer
   attribute, which represents the type of a connection.
   Valid values are:
   <ul>
   <li>{@link #TYPE_DISK_DRIVE TYPE_DISK_DRIVE} - Disk drive.
   <li>{@link #TYPE_SPOOLED_OUTPUT_QUEUE TYPE_SPOOLED_OUTPUT_QUEUE} - Spooled output queue.
   </ul>
   **/
  public static final String TYPE = "TYPE";
  /**
   {@link #TYPE TYPE} attribute value indicating a connection type of "disk drive".
   **/
  public static final Integer TYPE_DISK_DRIVE = new Integer(0);
  /**
   {@link #TYPE TYPE} attribute value indicating a connection type of "spooled output queue".
   **/
  public static final Integer TYPE_SPOOLED_OUTPUT_QUEUE = new Integer(1);
  static {
    attributes_.add(TYPE, Integer.class, true,
                    new Object[] {TYPE_DISK_DRIVE, TYPE_SPOOLED_OUTPUT_QUEUE }, null, true);
    getterMap_.add (TYPE, OLST0600_, "receiverVariable.connectionType", INDICES_);
    getterMap_.add (TYPE, OLST0700_, "receiverVariable.connectionType", INDICES_);
    openListAttributeMap_.add (TYPE, null, "receiverVariable.connectionType");
  }

  /**
   Attribute ID for "user name".  This identifies a read-only String
   attribute, which represents the name of the user that opened the connection.
   **/
  public static final String USER = "USER";
  static {
    attributes_.add(USER, String.class, true);
    getterMap_.add (USER, OLST0600_, "receiverVariable.userName", INDICES_);
    getterMap_.add (USER, OLST0700_, "receiverVariable.userName", INDICES_);
    openListAttributeMap_.add (USER, null, "receiverVariable.userName");
  }

  //@A1M
  /**
   Attribute ID for "number of connection users".  This identifies a read-only Integer
   attribute, which represents the number of users on a connection.
   <br>Note: If the NetServer has not been started, this attribute's reported value will be -1.
   See {@link NetServer#isStarted() NetServer.isStarted()} and {@link NetServer#start() NetServer.start()}.
   **/
  /*public*/ static final String USER_COUNT = "USER_COUNT";
  static {
    attributes_.add(USER_COUNT, Integer.class, true);
    getterMap_.add (USER_COUNT, OLST0600_, "receiverVariable.numberOfConnectionUsers", INDICES_);
    getterMap_.add (USER_COUNT, OLST0700_, "receiverVariable.numberOfConnectionUsers", INDICES_);
    openListAttributeMap_.add (USER_COUNT, null, "receiverVariable.numberOfConnectionUsers");
  }



  //----------------------------------------------------------------------
  // PCML document initialization.
  //----------------------------------------------------------------------

  private static final String DOCUMENT_NAME_ = "com.ibm.as400.access.NetServer";
  private static ProgramCallDocument staticDocument_ = null;
  private ProgramCallDocument document_;

  static {
    // Create a static version of the PCML document, then clone it for each document.
    // This will improve performance, since we will only have to deserialize the PCML
    // object once.
    try {
      staticDocument_ = new ProgramCallDocument();
      staticDocument_.setDocument(DOCUMENT_NAME_);
    }
    catch (PcmlException e) {
      Trace.log(Trace.ERROR, "PcmlException when instantiating ProgramCallDocument.", e);
    }
  }


  //----------------------------------------------------------------------
  // Private data.
  //----------------------------------------------------------------------

  private int id_;  // Connection ID
  private boolean idWasSet_ = false; // indicates whether id_ has been set.

  private ProgramAttributeGetter attributeGetter_;




  // Note: This method is reserved for use by the NetServer class.
  NetServerConnection()
  {
    super(presentationLoader_.getPresentationWithIcon(PRESENTATION_KEY_, ICON_BASE_NAME_), null, attributes_);
  }


  // Note: This method is reserved for use by the NetServer class.
  NetServerConnection(AS400 system, int id)
  {
    this();
    try { setSystem(system); } catch (PropertyVetoException e) {}
    setID(id);
  }


  /**
   Computes the resource key.

   @param system  The system.
   **/
  static Object computeResourceKey(AS400 system, int id)
  {
    StringBuffer buffer = new StringBuffer();
    buffer.append(NetServerConnection.class);
    buffer.append(':');
    buffer.append(system.getSystemName());
    buffer.append(':');
    buffer.append(id);
    return buffer.toString();
  }


  /**
   Establishes the connection (of this object) to the system.

   <p>The method is called by the resource framework automatically
   when the object connection needs to be established.

   @exception ResourceException  If an error occurs.
   **/
  protected void establishConnection()
    throws ResourceException
  {
    // Internal check: Verify that the ID has been set.
    if (! idWasSet_) {
      throw new ExtendedIllegalStateException("id",
                        ExtendedIllegalStateException.PROPERTY_NOT_SET);
    }

    // Call the superclass.
    super.establishConnection();

    // Initialize the PCML document.
    AS400 system = getSystem();
    document_ = (ProgramCallDocument)staticDocument_.clone();
    try {
      document_.setSystem(system);
      String connectionName = (String)getAttributeValue(NAME);
      document_.setValue(OLST0600_+".informationQualifier", connectionName);
      document_.setValue(OLST0700_+".informationQualifier", connectionName);

      // Initialize the attribute getter.
      attributeGetter_ = new ProgramAttributeGetter(system, document_, getterMap_);
    }
    catch (PcmlException e) {
      Trace.log(Trace.ERROR, "PcmlException when establishing connection.", e);
      throw new ResourceException(e);
    }
  }


  /**
   Freezes any property changes.  After this is called, property
   changes should not be made.  Properties are not the same thing
   as attributes.  Properties are basic pieces of information
   which must be set to make the object usable, such as the system
   and the name.

   <p>The method is called by the resource framework automatically
   when the properties need to be frozen.

   @exception ResourceException  If an error occurs.
   **/
  protected void freezeProperties()
    throws ResourceException
  {
    // Internal check: Verify that the ID has been set.
    if (! idWasSet_) {
      throw new ExtendedIllegalStateException("id",
                        ExtendedIllegalStateException.PROPERTY_NOT_SET);
    }

    // Update the presentation.
    Presentation presentation = getPresentation();
    presentation.setName(Integer.toString(id_));
    presentation.setFullName(Integer.toString(id_));

    // Update the resource key.
    if (getResourceKey() == null) {
      setResourceKey(computeResourceKey(getSystem(), id_));
    }

    // Call the superclass.
    super.freezeProperties();
  }


  // Returns the attribute metadata for the class.
  static ResourceMetaData[] getAttributeMetaDataStatic()
  {
    return attributes_.getMetaData();
  }


  /**
   Returns the current value of an attribute.

   @param attributeID  Identifies the attribute.
   @return             The attribute value, or null if the attribute
   value is not available.

   @exception ResourceException  If an error occurs.
   **/
  public Object getAttributeValue(Object attributeID)
    throws ResourceException
  {
    if (! isConnectionEstablished()) {
      establishConnection();
    }

    Object value = super.getAttributeValue(attributeID);

    if (value == null) {
      value = attributeGetter_.getValue(attributeID);
    }
    return value;
  }


  /**
   Returns the connection ID for connection.
   @return  The connection ID.
   **/
  public int getID()
  {
    return id_;
  }


  /**
   Lists connections currently associated with the NetServer.
   The returned ResourceList contains {@link NetServerConnection NetServerConnection} objects.
   @param sys The system.
   @param type Type of connections to list: SHARE or SESSION.
   @return  Information about current connections.

   @exception ResourceException  If an error occurs.
   **/
  static ResourceList list(AS400 sys, int type)
    throws ResourceException
  {
    if (type != SHARE && type != SESSION) {
      Trace.log(Trace.ERROR, "Invalid type of connection: " + type);
      throw new InternalErrorException(InternalErrorException.UNEXPECTED_EXCEPTION);
    }
    // First get the list of the specified type of resource, and then for each resource get its list of connections.
    ResourceList resources;
    if (type == SHARE)   resources = NetServerFileShare.list(sys);
    else                 resources = NetServerSession.list(sys);

    resources.waitForComplete();
    Vector connList = new Vector();
    for (int i=0; i<resources.getListLength(); i++)
    {
      ResourceList conns;
      if (type == SHARE) {
        conns = ((NetServerFileShare)(resources.resourceAt(i))).listConnections();
      }
      else {
        conns = ((NetServerSession)(resources.resourceAt(i))).listConnections();
      }
      conns.waitForComplete();
      for (int ii=0; ii<conns.getListLength(); ii++) {
        connList.addElement(conns.resourceAt(ii));
      }
    }
    NetServerConnection[] array = new NetServerConnection[connList.size()];
    connList.copyInto(array);

    // Create a Presentation object to be associated with the list.
    Presentation presentation = presentationLoader_.getPresentationWithIcon(PRESENTATION_KEY_, ICON_BASE_NAME_);

    return new ArrayResourceList(array, presentation, NetServerConnection.getAttributeMetaDataStatic());
  }


  /**
   Returns a list of the connections associated with a specific resource (share or session).
   The returned ResourceList contains {@link NetServerConnection NetServerConnection} objects.
   @param sys The system.
   @param connectionType The type of connection.  Valid values are SHARE and SESSION.
   @param qualifier The list qualifier.  This identifies the share or session.

   @exception ResourceException  If an error occurs.
   **/
  private static ResourceList list(AS400 system, int connectionType, String qualifier)
    throws ResourceException
  {
    // Assume the caller has validated the arguments.

    // Note: The 0600 format requires a specific "session name", not *ALL.
    // Note: The 0700 format requires a specific "share name", not *ALL.

    try
    {
      // Set the input parameters and call the API.
      ProgramCallDocument document = (ProgramCallDocument)staticDocument_.clone();
      document.setSystem(system);
      String programName;
      if (connectionType == SESSION)    programName = OLST0600_;
      else                              programName = OLST0700_;
      document.setValue(programName+".informationQualifier", qualifier);

      // Note: Formats ZLSL0600 or ZLSL0700 return a list of 64-byte fixed-length records.
      int expectedInfoLength = 10*64; // Expect about 10 64-byte records.
      document.setIntValue(programName+".lengthOfReceiverVariable", expectedInfoLength);

      NetServer.callListProgram(document, programName, true);
        // Note: This method does a retry (with larger buffer) if receiverVariable is overflowed.

      int recCount = document.getIntValue(programName+".listInformation.recordsReturned");
      int recSize = document.getIntValue(programName+".listInformation.recordLength");

      Vector connections = new Vector();
      String prefix = programName+".receiverVariable.";

      int[] indices = new int[1];
      Object[] attributeIDs = openListAttributeMap_.getIDs();
      for (int i=0; i<recCount; i++)
      {
        indices[0] = i;
        int    id             = document.getIntValue(prefix+"connectionId", indices);
        NetServerConnection connection = new NetServerConnection(system, id);

        // Copy the information from the API record to the NetServerConnection attributes.
        Object[] values = openListAttributeMap_.getValues(attributeIDs, system, document, programName, indices);
        for (int ii = 0; ii < values.length; ++ii) {
          connection.initializeAttributeValue(attributeIDs[ii], values[ii]);
        }

        connection.freezeProperties();

        connections.addElement(connection);
      }

      NetServerConnection[] array = new NetServerConnection[connections.size()];
      connections.copyInto(array);

      // Create a Presentation object to be associated with the list.
      Presentation presentation = presentationLoader_.getPresentationWithIcon(PRESENTATION_KEY_, ICON_BASE_NAME_);

      return new ArrayResourceList(array, presentation, NetServerConnection.getAttributeMetaDataStatic());
    }
    catch (PcmlException e) {
      Trace.log(Trace.ERROR, "PcmlException when listing connections.", e);
      throw new ResourceException(e);
    }
  }


  /**
   Returns a list of the connections associated with a specific session.
   The returned ResourceList contains {@link NetServerConnection NetServerConnection} objects.
   @param sys The system.
   @param sessionName The session name.

   @exception ResourceException  If an error occurs.
   **/
  static ResourceList listConnectionsForSession(AS400 sys, String sessionName)
    throws ResourceException
  {
    return list(sys, SESSION, sessionName);
  }


  /**
   Returns a list of the connections associated with a specific share.
   The returned ResourceList contains {@link NetServerConnection NetServerConnection} objects.
   @param sys The system.
   @param shareName The share name.

   @exception ResourceException  If an error occurs.
   **/
  static ResourceList listConnectionsForShare(AS400 sys, String shareName)
    throws ResourceException
  {
    return list(sys, SHARE, shareName);
  }


  /**
   Refreshes the values for all attributes.
   This method fires an attributeValuesRefreshed() ResourceEvent.

   @exception ResourceException  If an error occurs.
   **/
  public void refreshAttributeValues()
    throws ResourceException
  {
    if (! isConnectionEstablished()) {
      establishConnection();
    }

    attributeGetter_.clearBuffer();
    super.refreshAttributeValues();
  }

  // Note: This method is reserved for use by the NetServer class.
  void setID(int id)
  {
    id_ = id;
    idWasSet_ = true;
  }

}
