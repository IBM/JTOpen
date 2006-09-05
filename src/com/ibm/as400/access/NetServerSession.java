///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: NetServerSession.java
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
 The NetServerSession class represents a NetServer session.
 <p>
 Note: A <b>session</b> (represented by this class) corresponds to a workstation.  A workstation could be a Windows Terminal Server or it could be a single PC on someone's desktop.  A <b>connection</b> (represented by class {@link NetServerConnection NetServerConnection}) corresponds to a specific user who has mapped a drive and has files opened or spooled output on a print queue.  Since a <b>session</b> can have multiple users, a <b>connection</b> shows a particular user's statistics on that session.
 <p>
 NetServerSession objects are created and returned by {@link NetServer#listSessions() NetServer.listSessions()}.
<p>
<a name="attributeIDs">The following attribute IDs are supported:
<ul>
<li>{@link #CONNECTION_COUNT CONNECTION_COUNT}
<li>{@link #SESSION_TIME SESSION_TIME}
<li>{@link #FILES_OPEN_COUNT FILES_OPEN_COUNT}
<li>{@link #IDLE_TIME IDLE_TIME}
<li>{@link #IS_ENCRYPT_PASSWORD IS_ENCRYPT_PASSWORD}
<li>{@link #IS_GUEST IS_GUEST}
<li>{@link #USER USER}
</ul>

<p>Use any of the above attribute IDs with
{@link com.ibm.as400.resource.ChangeableResource#getAttributeValue(java.lang.Object) getAttributeValue}
to access the attribute values for a NetServerSession.
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
*   // List all current sessions.
*   System.out.println("Sessions:");
*   ResourceList sessionList = ns.listSessions();
*   sessionList.waitForComplete();
*   for (int i=0; i&lt;sessionList.getListLength(); i++)
*   {
*     NetServerSession session =
*       (NetServerSession)sessionList.resourceAt(i);
*     System.out.println(session.getName() + ": " +
*       (String)session.getAttributeValue(NetServerSession.USER) + "; " +
*       ((Integer)session.getAttributeValue(NetServerSession.SESSION_TIME))
*                                                           .intValue() + "; " +
*       ((Boolean)session.getAttributeValue(NetServerSession.IS_GUEST))
*                                                           .booleanValue() );
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
 {@link com.ibm.as400.access.ISeriesNetServerSession ISeriesNetServerSession}
 class and may be removed in a future release.
**/


public class NetServerSession
extends Resource
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


    static final long serialVersionUID = 4L;

  //-------------------------------------------------------------------------
  // Presentation.
  //-------------------------------------------------------------------------

  private static PresentationLoader   presentationLoader_ = new PresentationLoader("com.ibm.as400.access.MRI2");
  private static final String         ICON_BASE_NAME_     = "NetServerSession";
  private static final String         PRESENTATION_KEY_   = "NETSERVER";

  private static ResourceMetaDataTable attributes_ = new ResourceMetaDataTable(presentationLoader_, PRESENTATION_KEY_);

  // Attribute getter map.
  private static ProgramMap getterMap_  = new ProgramMap();
  private static ProgramMap openListAttributeMap_  = new ProgramMap();

  // API names.
  private static final String ENSS_ = "qzlsenss";

  private static final String OLST0300_ = "qzlsolst_zlsl0300";
  private static final int[] INDICES_ = { 0 }; // For specifying first record in list.



  //-------------------------------------------------------------------------
  // Attribute IDs.
  //
  // * If you add an attribute here, make sure and add it to the class javadoc.
  //-------------------------------------------------------------------------

  /**
   Attribute ID for "connection count".  This identifies a read-only Integer
   attribute, which represents the number of connections made during a session.
   **/
  public static final String CONNECTION_COUNT = "CONNECTION_COUNT";
  static {
    attributes_.add(CONNECTION_COUNT, Integer.class, true);
    getterMap_.add (CONNECTION_COUNT, OLST0300_, "receiverVariable.numberOfConnections", INDICES_);
    openListAttributeMap_.add (CONNECTION_COUNT, OLST0300_, "receiverVariable.numberOfConnections");
  }

  /**
   Attribute ID for "session time".  This identifies a read-only Integer
   attribute, which represents the number of seconds that a session has been active.
   **/
  public static final String SESSION_TIME = "SESSION_TIME";
  static {
    attributes_.add(SESSION_TIME, Integer.class, true);
    getterMap_.add (SESSION_TIME, OLST0300_, "receiverVariable.sessionTime", INDICES_);
    openListAttributeMap_.add (SESSION_TIME, OLST0300_, "receiverVariable.sessionTime");
  }

  /**
   Attribute ID for "files open count".  This identifies a read-only Integer
   attribute, which represents the number of files that are currently open for a session.
   **/
  public static final String FILES_OPEN_COUNT = "FILES_OPEN_COUNT";
  static {
    attributes_.add(FILES_OPEN_COUNT, Integer.class, true);
    getterMap_.add (FILES_OPEN_COUNT, OLST0300_, "receiverVariable.numberOfFilesOpen", INDICES_);
    openListAttributeMap_.add (FILES_OPEN_COUNT, OLST0300_, "receiverVariable.numberOfFilesOpen");
  }

  /**
   Attribute ID for "session idle time".  This identifies a read-only Integer
   attribute, which represents the number of seconds a session has been idle.
   **/
  public static final String IDLE_TIME = "IDLE_TIME";
  static {
    attributes_.add(IDLE_TIME, Integer.class, true);
    getterMap_.add (IDLE_TIME, OLST0300_, "receiverVariable.idleTime", INDICES_);
    openListAttributeMap_.add (IDLE_TIME, OLST0300_, "receiverVariable.idleTime");
  }

  /**
   Attribute ID for "is encrypt password".  This identifies a read-only Boolean
   attribute, which indicates whether or not the encrypted password was used to establish a session.
   **/
  public static final String IS_ENCRYPT_PASSWORD = "IS_ENCRYPT_PASSWORD";
  static {
    attributes_.add(IS_ENCRYPT_PASSWORD, Boolean.class, true);
    ValueMap valueMap = new BooleanValueMap("0", "1");
    getterMap_.add (IS_ENCRYPT_PASSWORD, OLST0300_, "receiverVariable.encryptedPassword", INDICES_, valueMap);
    openListAttributeMap_.add (IS_ENCRYPT_PASSWORD, OLST0300_, "receiverVariable.encryptedPassword", valueMap);
  }

  /**
   Attribute ID for "is guest".  This identifies a read-only Boolean
   attribute, which indicates whether or not a session is a guest session.
   **/
  public static final String IS_GUEST = "IS_GUEST";
  static {
    attributes_.add(IS_GUEST, Boolean.class, true);
    ValueMap valueMap = new BooleanValueMap("1", "0");
        // Note the reverse logic: The API field is "logon type", which is 0 if guest, 1 if regular user.
    getterMap_.add (IS_GUEST, OLST0300_, "receiverVariable.logonType", INDICES_, valueMap);
    openListAttributeMap_.add (IS_GUEST, OLST0300_, "receiverVariable.logonType", valueMap);
  }

  /**
   Attribute ID for "session count".  This identifies a read-only Integer
   attribute, which represents the number of sessions that are established between the system and the requester.  This value is always 0 or 1.
   **/
  // Note: We will not make this attribute public unless someone says they need it.
  /*public*/ static final String SESSION_COUNT = "SESSION_COUNT";
  static {
    attributes_.add(SESSION_COUNT, Integer.class, true);
    getterMap_.add (SESSION_COUNT, OLST0300_, "receiverVariable.numberOfSessions", INDICES_);
    openListAttributeMap_.add (SESSION_COUNT, OLST0300_, "receiverVariable.numberOfSessions");
  }

  /**
   Attribute ID for "user name".  This identifies a read-only String
   attribute, which represents the name of the user that is associated with a session.
   **/
  public static final String USER = "USER";
  static {
    attributes_.add(USER, String.class, true);
    getterMap_.add (USER, OLST0300_, "receiverVariable.userProfileName", INDICES_);
    openListAttributeMap_.add (USER, OLST0300_, "receiverVariable.userProfileName");
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

  // Workstation name.  This uniquely identifies a session on the system.
  private String name_;

  private ProgramAttributeGetter attributeGetter_;



  // Note: This method is reserved for use by the NetServer class.
  NetServerSession()
  {
    super(presentationLoader_.getPresentationWithIcon(PRESENTATION_KEY_, ICON_BASE_NAME_), null, attributes_);
  }


  // Note: This method is reserved for use by the NetServer class.
  NetServerSession(AS400 system, String name)
  {
    this();
    try { setSystem(system); } catch (PropertyVetoException e) {}
    setName(name);
  }


  /**
   Computes the resource key.

   @param system  The system.
   **/
  static Object computeResourceKey(AS400 system, String name)
  {
    StringBuffer buffer = new StringBuffer();
    buffer.append(NetServerSession.class);
    buffer.append(':');
    buffer.append(system.getSystemName());
    buffer.append(':');
    buffer.append(name);
    return buffer.toString();
  }

  /**
   Ends the session.
   This method fires a resourceDeleted() ResourceEvent.
   <br>This method requires *IOSYSCFG special authority on the system.

   @exception ResourceException  If an error occurs.
   **/
  public void end()
    throws ResourceException
  {
    if (! isConnectionEstablished()) {
      establishConnection();
    }

    endSession(getSystem(), name_);
    /// fireSessionEnded();  // TBD
  }


  /**
   Ends a session.
   @param sys The system.
   @param sessionName The name of the session.

   @exception ResourceException  If an error occurs.
   **/
  static void endSession(AS400 system, String sessionName)
    throws ResourceException
  {
    // Set the input parameters and call the API.
    try
    {
      ProgramCallDocument document = (ProgramCallDocument)staticDocument_.clone();
      document.setSystem(system);
      document.setValue(ENSS_+".workstationName", sessionName);

      if (document.callProgram(ENSS_) == false) {
        throw new ResourceException(document.getMessageList(ENSS_));
      }
    }
    catch (PcmlException e) {
      Trace.log(Trace.ERROR, "PcmlException when ending a session.", e);
      throw new ResourceException(e);
    }
  }


  /**
   Establishes the connection to the system.

   <p>The method is called by the resource framework automatically
   when the connection needs to be established.

   @exception ResourceException  If an error occurs.
   **/
  protected void establishConnection()
    throws ResourceException
  {
    if (name_ == null) {
      throw new ExtendedIllegalStateException("name",
                          ExtendedIllegalStateException.PROPERTY_NOT_SET);
    }

    // Call the superclass.
    super.establishConnection();

    // Initialize the PCML document.
    AS400 system = getSystem();
    document_ = (ProgramCallDocument)staticDocument_.clone();
    try {
      document_.setSystem(system);
      document_.setValue("qzlsolst_zlsl0300.informationQualifier", name_);

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
    // Verify that the session name has been set.
    if (name_ == null) {
      throw new ExtendedIllegalStateException("name",
                        ExtendedIllegalStateException.PROPERTY_NOT_SET);
    }

    // Update the presentation.
    Presentation presentation = getPresentation();
    presentation.setName(name_);
    presentation.setFullName(name_);

    // Update the resource key.
    if (getResourceKey() == null) {
      setResourceKey(computeResourceKey(getSystem(), name_));
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
   Returns the name of the workstation from which the session to the system was established.
   @return The workstation name.
   **/
  public String getName()
  {
    return name_;
  }


  /**
   Lists the current sessions.
   The returned ResourceList contains {@link NetServerSession NetServerSession} objects.
   @return  Information about the current sessions.

   @exception ResourceException  If an error occurs.
   **/
  static ResourceList list(AS400 system)
    throws ResourceException
  {
    // Note: The on-line spec says that the 0300 format requires a valid session name.  This is not entirely correct.  It will also accept *ALL.

    try
    {
      // Set the input parameters and call the API.
      ProgramCallDocument document = (ProgramCallDocument)staticDocument_.clone();
      document.setSystem(system);
      document.setValue(OLST0300_+".informationQualifier", "*ALL");

      // Note: Formats ZLSL0300 returns a list of 64-byte fixed-length records.
      int expectedInfoLength = 10*64; // Expect about 10 64-byte records.
      document.setIntValue(OLST0300_+".lengthOfReceiverVariable", expectedInfoLength);

      NetServer.callListProgram(document, OLST0300_, true);
        // Note: This method does a retry (with larger buffer) if receiverVariable is overflowed.

      int recCount = document.getIntValue(OLST0300_+".listInformation.recordsReturned");
      int recSize = document.getIntValue(OLST0300_+".listInformation.recordLength");

      Vector sessions = new Vector();
      String prefix = OLST0300_+".receiverVariable.";

      int index = 0;
      int[] indices = new int[1];
      Object[] attributeIDs = openListAttributeMap_.getIDs();
      for (int i=0; i<recCount; i++)
      {
        indices[0] = i;

        String sessionName    = (String)document.getValue(prefix+"workstationName", indices);

        NetServerSession session = new NetServerSession(system, sessionName);

        // Copy the information from the API record to the NetServerSession attributes.
        Object[] values = openListAttributeMap_.getValues(attributeIDs, system, document, null, indices);
        for (int ii = 0; ii < values.length; ++ii) {
          session.initializeAttributeValue(attributeIDs[ii], values[ii]);
        }

        session.freezeProperties();
        sessions.addElement(session);

        index += recSize;
      }

      NetServerSession[] array = new NetServerSession[sessions.size()];
      sessions.copyInto(array);

      // Create a Presentation object to be associated with the list.
      Presentation presentation = presentationLoader_.getPresentationWithIcon(PRESENTATION_KEY_, ICON_BASE_NAME_);

      return new ArrayResourceList(array, presentation, NetServerSession.getAttributeMetaDataStatic());
    }
    catch (PcmlException e) {
      Trace.log(Trace.ERROR, "PcmlException when listing sessions.", e);
      throw new ResourceException(e);
    }
  }


  /**
   Lists the connections currently associated with this session.
   The returned ResourceList contains {@link NetServerConnection NetServerConnection} objects.
   @return  The current connections for the session.

   @exception ResourceException  If an error occurs.
   **/
  public ResourceList listConnections()
    throws ResourceException
  {
    if (! isConnectionEstablished()) {
      establishConnection();
    }

    return NetServerConnection.listConnectionsForSession(getSystem(), name_);
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
  void setName(String name)
  {
    name_ = name;
  }

}
