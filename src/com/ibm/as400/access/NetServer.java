///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: NetServer.java
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
 The NetServer class represents the NetServer service on a server.
 This class allows the user to query and modify the state and configuration
 of the NetServer.
 <p>
 Note: Many of the attributes of NetServer are "pending".  These attributes represent NetServer values that will take effect the next time the NetServer is (re)started.  Wherever there is a pair of related attributes, where one is pending and one is non-pending, the "pending" attribute is read/write, while the non-pending attribute is read-only.
<br>For example, to change the name of the NetServer, using a <code>com.ibm.as400.access.NetServer</code> object named "netServer":
<ol>
<li>netServer.setAttributeValue(NetServer.NAME_PENDING, newName)
<li>netServer.commitAttributeChanges()
<li>netServer.end()
<li>netServer.start()
</ol>
 <p>
 Note: This class does not start or end the QSERVER subsystem on the server.
 If the QSERVER subsystem is not running, various methods of this class will fail.
 <p>
 Note: Typically, methods which change the state or attributes of the NetServer require that the server user profile has *IOSYSCFG special authority.  For example, starting or ending the NetServer requires *IOSYSCFG authority.
 <p>
 Note: This class uses some API fields that are available only in the OS/400 releases following V4R5.
<p>
<a name="attributeIDs">The following attribute IDs are supported:
<ul>
<li>{@link #ALLOW_SYSTEM_NAME ALLOW_SYSTEM_NAME}
<li>{@link #ALLOW_SYSTEM_NAME_PENDING ALLOW_SYSTEM_NAME_PENDING}
<li>{@link #AUTOSTART AUTOSTART}
<li>{@link #BROWSING_INTERVAL BROWSING_INTERVAL}
<li>{@link #BROWSING_INTERVAL_PENDING BROWSING_INTERVAL_PENDING}
<li>{@link #CCSID CCSID}
<li>{@link #CCSID_PENDING CCSID_PENDING}
<li>{@link #DESCRIPTION DESCRIPTION}
<li>{@link #DESCRIPTION_PENDING DESCRIPTION_PENDING}
<li>{@link #DOMAIN DOMAIN}
<li>{@link #DOMAIN_PENDING DOMAIN_PENDING}
<li>{@link #GUEST_USER_PROFILE GUEST_USER_PROFILE}
<li>{@link #GUEST_USER_PROFILE_PENDING GUEST_USER_PROFILE_PENDING}
<li>{@link #IDLE_TIMEOUT IDLE_TIMEOUT}
<li>{@link #IDLE_TIMEOUT_PENDING IDLE_TIMEOUT_PENDING}
<li>{@link #LOGON_SUPPORT LOGON_SUPPORT}
<li>{@link #LOGON_SUPPORT_PENDING LOGON_SUPPORT_PENDING}
<li>{@link #NAME NAME}
<li>{@link #NAME_PENDING NAME_PENDING}
<li>{@link #WINS_ENABLEMENT WINS_ENABLEMENT}
<li>{@link #WINS_ENABLEMENT_PENDING WINS_ENABLEMENT_PENDING}
<li>{@link #WINS_PRIMARY_ADDRESS WINS_PRIMARY_ADDRESS}
<li>{@link #WINS_PRIMARY_ADDRESS_PENDING WINS_PRIMARY_ADDRESS_PENDING}
<li>{@link #WINS_SCOPE_ID WINS_SCOPE_ID}
<li>{@link #WINS_SCOPE_ID_PENDING WINS_SCOPE_ID_PENDING}
<li>{@link #WINS_SECONDARY_ADDRESS WINS_SECONDARY_ADDRESS}
<li>{@link #WINS_SECONDARY_ADDRESS_PENDING WINS_SECONDARY_ADDRESS_PENDING}
</ul>

<p>Use any of the above attribute IDs with
{@link com.ibm.as400.resource.ChangeableResource#getAttributeValue(java.lang.Object) getAttributeValue}
and
{@link com.ibm.as400.resource.ChangeableResource#setAttributeValue(java.lang.Object,java.lang.Object) setAttributeValue} to access the attribute values for a NetServer.
<br>
Note: For the above attributes, getAttributeValue() should never return null.
For String-valued attributes, if the current actual value of the corresponding property on the server is blank, getAttributeValue() will return "" (an empty String).

<blockquote>
<pre>
* import com.ibm.as400.access.*;
* import com.ibm.as400.resource.*;
*
* // Create a NetServer object for a specific server system.
* AS400 system = new AS400("MYSYSTEM", "MYUSERID", "MYPASSWORD");
* NetServer ns = new NetServer(system);
*
* try
* {
*
*   // Get the name of the NetServer.
*   System.out.println("Name: " +
*               (String)ns.getAttributeValue(NetServer.NAME));
*
*   // Get the CCSID of the NetServer.
*   System.out.println("CCSID: " +
*    ((Integer)ns.getAttributeValue(NetServer.CCSID)).intValue());
*
*   // Get the pending CCSID of the NetServer.
*   System.out.println("Pending CCSID: " +
*    ((Integer)ns.getAttributeValue(NetServer.CCSID_PENDING)).intValue());
*
*   // Get the "allow system name" value of the NetServer.
*   System.out.println("'Allow system name': " +
*    ((Boolean)ns.getAttributeValue(NetServer.ALLOW_SYSTEM_NAME)).booleanValue());
*
*   // Set the (pending) description of the NetServer.
*   // Note: Changes to "pending" attributes take effect after the NetServer
*   // is ended and restarted.
*   ns.setAttributeValue(NetServer.DESCRIPTION_PENDING, "The NetServer");
*   ns.commitAttributeChanges();
*
*   // Set the (pending) CCSID of the NetServer to 13488.
*   ns.setAttributeValue(NetServer.CCSID_PENDING, new Integer(13488));
*
*   // Set the (pending) "allow system name" value of the NetServer to true.
*   ns.setAttributeValue(NetServer.ALLOW_SYSTEM_NAME_PENDING, new Boolean(true));
*
*   // Commit the attribute changes (send them to the system).
*   ns.commitAttributeChanges();
*
*   // Print all the attribute values of the NetServer object.
*   ResourceMetaData[] attributeMetaData = ns.getAttributeMetaData();
*   for(int i = 0; i&lt;attributeMetaData.length; i++)
*   {
*     Object attributeID = attributeMetaData[i].getID();
*     Object value = resource.getAttributeValue(attributeID);
*     System.out.println("Attribute " + attributeID + " = " + value);
*   }
*
* }
* catch (ResourceException e) {
*   e.printStackTrace();
* }
* finally {
*   if (system != null) system.disconnectAllServices();
* }
</pre>
</blockquote>

@see NetServerShare
@see NetServerFileShare
@see NetServerPrintShare
@see NetServerConnection
@see NetServerSession
**/

public class NetServer
extends ChangeableResource
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


    static final long serialVersionUID = 4L;

  //-------------------------------------------------------------------------
  // Presentation.
  //-------------------------------------------------------------------------

  private static PresentationLoader   presentationLoader_ = new PresentationLoader("com.ibm.as400.access.MRI2");
  private static final String         ICON_BASE_NAME_     = "NetServer";
  private static final String         PRESENTATION_KEY_   = "NETSERVER";

  private static ResourceMetaDataTable attributes_  = new ResourceMetaDataTable(presentationLoader_, PRESENTATION_KEY_);

  // Attribute getter map and setter map.
  private static ProgramMap getterMap_    = new ProgramMap();
  private static ProgramMap setterMap_    = new ProgramMap();

  private static final String OLST0201_ = "qzlsolst_zlsl0201";

  private static BooleanValueMap BV_MAP_0_1_ = new BooleanValueMap("0", "1");
  private static BooleanValueMap BV_MAP_0_1_INT_ = new BooleanValueMap(new Integer(0), new Integer(1));
  private static BooleanValueMap BV_MAP_NO_YES_ = new BooleanValueMap("*NO", "*YES");
  private static BooleanValueMap BV_MAP_NO_ERR_YES_ = new BooleanValueMap(new String[] {"*NO", "*ERR"}, new String[] { "*YES" });

  //-------------------------------------------------------------------------
  // Attribute IDs.
  //
  // * If you add an attribute here, make sure and add it to the class javadoc.
  //-------------------------------------------------------------------------

  /**
   Attribute ID for "allow system name".  This identifies a read-only Boolean
   attribute, which indicates whether to allow access to the server using the server's TCP/IP system name.
   **/
  public static final String ALLOW_SYSTEM_NAME = "ALLOW_SYSTEM_NAME";
  static {
    attributes_.add(ALLOW_SYSTEM_NAME, Boolean.class, true);
    getterMap_.add (ALLOW_SYSTEM_NAME, OLST0201_, "receiverVariable.allowSystemName", BV_MAP_0_1_);
  }

  /**
   Attribute ID for "allow system name (pending)".  This identifies a Boolean
   attribute, which indicates whether to allow access to the server using the server's TCP/IP system name.
   **/
  public static final String ALLOW_SYSTEM_NAME_PENDING = "ALLOW_SYSTEM_NAME_PENDING";
  static {
    attributes_.add(ALLOW_SYSTEM_NAME_PENDING, Boolean.class);
    getterMap_.add (ALLOW_SYSTEM_NAME_PENDING, OLST0201_, "receiverVariable.allowSystemNameP", BV_MAP_0_1_);
    setterMap_.add (ALLOW_SYSTEM_NAME_PENDING, "qzlschsn", "allowSystemNameP", BV_MAP_0_1_);
  }

  /**
   Attribute ID for "autostart".  This identifies a Boolean
   attribute, which indicates whether or not the NetServer is to be started automatically when TCP is started.
   <br>Note: Due to API restrictions, if the server user profile does not have *IOSYSCFG authority, the value of this attribute is reported as <code>false</code>, regardless of the actual setting on the server.
   **/
  public static final String AUTOSTART = "AUTOSTART";
  static {
    attributes_.add(AUTOSTART, Boolean.class);
    getterMap_.add (AUTOSTART, "qtocauto_rtv", "autostart", BV_MAP_NO_ERR_YES_);
    setterMap_.add (AUTOSTART, "qtocauto_chg", "autostart", BV_MAP_NO_YES_);

    // Format of the QTOCAUTO API:
    //
    // CALL PGM(QTOCAUTO) PARM('*RTV' *NETSVR variable X'00000000')
    //
    // parm 1 - CHAR(4) -
    //               *RTV - retrieves the NetServer autostart value in the
    //                           variable which is parameter number 3.
    //               *CHG - changes the NetServer autostart value to the value
    //                      in the variable which is parameter number 3.
    // parm 2 - CHAR(30) -
    //               *NETSVR - indicates to retrieve or change the NetServer
    //                         autostart value.
    // parm 3 - CHAR(4) variable used to retrieve or change the NetServer autostart value.
    //          Allowed values *YES or *NO.
    // parm 4 - The error code return variable.

  }

  /**
   Attribute ID for "browsing interval".  This identifies a read-only Integer attribute, which represents the amount of time, in milliseconds, between each server announcement that is used for browsing.
   A value of zero indicates that there will be no server announcements.
   **/
  public static final String BROWSING_INTERVAL = "BROWSING_INTERVAL";
  static {
    attributes_.add(BROWSING_INTERVAL, Integer.class, true);
    getterMap_.add (BROWSING_INTERVAL, OLST0201_, "receiverVariable.browsingInterval");
  }

  /**
   Attribute ID for "browsing interval (pending)".  This identifies an Integer attribute, which represents the amount of time, in milliseconds, between each server announcement that is used for browsing.
   A value of zero indicates that there will be no server announcements.
   **/
  public static final String BROWSING_INTERVAL_PENDING = "BROWSING_INTERVAL_PENDING";
  static {
    attributes_.add(BROWSING_INTERVAL_PENDING, Integer.class);
    getterMap_.add (BROWSING_INTERVAL_PENDING, OLST0201_, "receiverVariable.browsingIntervalP");
    setterMap_.add (BROWSING_INTERVAL_PENDING, "qzlschsi", "requestVariable.browsingIntervalP");
  }

  /**
   Attribute ID for "server CCSID".  This identifies a read-only Integer
   attribute, which represents the coded character set identifier
   for the NetServer.
   <p> This is the CCSID that is used for all
   clients connected to the server.
   <br>
   The default value for this field is the
   associated ASCII CCSID for the CCSID of the job
   used to start the server.
   <br>
   Note: A value of 0 indicates that the user would
   like to use the associated ASCII CCSID for the
   CCSID of the job used to start the server.
   **/
  public static final String CCSID = "CCSID";
  static {
    attributes_.add(CCSID, Integer.class, true);
    getterMap_.add (CCSID, OLST0201_, "receiverVariable.ccsid");
  }

  /**
   Attribute ID for "server CCSID (pending)".  This identifies an Integer
   attribute, which represents the pending coded character set identifier
   for the NetServer.
   **/
  public static final String CCSID_PENDING = "CCSID_PENDING";
  static {
    attributes_.add(CCSID_PENDING, Integer.class);
    getterMap_.add (CCSID_PENDING, OLST0201_, "receiverVariable.ccsidP");
    setterMap_.add (CCSID_PENDING, "qzlschsi", "requestVariable.ccsidP");
  }

  /**
   Attribute ID for "description".  This identifies a read-only String
   attribute, which represents the text description of the NetServer.
   **/
  public static final String DESCRIPTION = "DESCRIPTION";
  static {
    attributes_.add(DESCRIPTION, String.class, true);
    getterMap_.add (DESCRIPTION, OLST0201_, "receiverVariable.description");
  }

  /**
   Attribute ID for "description (pending)".  This identifies a String
   attribute, which represents the pending text description of the NetServer.
   **/
  public static final String DESCRIPTION_PENDING = "DESCRIPTION_PENDING";
  static {
    attributes_.add(DESCRIPTION_PENDING, String.class);
    getterMap_.add (DESCRIPTION_PENDING, OLST0201_, "receiverVariable.descriptionP");
    setterMap_.add (DESCRIPTION_PENDING, "qzlschsn", "descriptionP");
  }

  /**
   Attribute ID for "domain name".  This identifies a read-only String
   attribute, which represents the domain name of the NetServer.
   **/
  public static final String DOMAIN = "DOMAIN";
  static {
    attributes_.add(DOMAIN, String.class, true);
    getterMap_.add (DOMAIN, OLST0201_, "receiverVariable.domainName");
  }

  /**
   Attribute ID for "domain name (pending)".  This identifies a String
   attribute, which represents the pending domain name of the NetServer.
   **/
  public static final String DOMAIN_PENDING = "DOMAIN_PENDING";
  static {
    attributes_.add(DOMAIN_PENDING, String.class);
    getterMap_.add (DOMAIN_PENDING, OLST0201_, "receiverVariable.domainNameP");
    setterMap_.add (DOMAIN_PENDING, "qzlschsn", "domainNameP");
  }

  // Note: The NetServer team says they don't use the Guest Support fields.
  // Apparently these fields are used internally by the Host.  There is currently no need for us to surface them.
  /**
   Attribute ID for "Guest support".  This identifies a read-only Boolean
   attribute, which indicates whether a guest user profile may be used in the event an unknown user attempts to access resources on the system.
   **/
  /*public*/ static final String GUEST_SUPPORT = "GUEST_SUPPORT";
  static {
    attributes_.add(GUEST_SUPPORT, Boolean.class, true);
    getterMap_.add (GUEST_SUPPORT, OLST0201_, "receiverVariable.guestSupport", BV_MAP_0_1_INT_);
  }

  // See above note.
  /**
   Attribute ID for "Guest support (pending)".  This identifies a read-only Boolean
   attribute, which indicates whether a guest user profile may be used in the event an unknown user attempts to access resources on the system.
   **/
  /*public*/ static final String GUEST_SUPPORT_PENDING = "GUEST_SUPPORT_PENDING";
  static {
    attributes_.add(GUEST_SUPPORT_PENDING, Boolean.class, true); // TBD - no setter, so it's read-only for now.
    getterMap_.add (GUEST_SUPPORT_PENDING, OLST0201_, "receiverVariable.guestSupportP", BV_MAP_0_1_INT_);
    // Note: No setter, there is no API to set this attribute.
  }

  /**
   Attribute ID for "guest user profile".  This identifies a read-only String
   attribute, which represents the guest user profile for the NetServer.
   If no guest user profile is currently configured on the server, the value of this attribute is "" (an empty String).
   <p>
   Note: Guest support allows customers to have users accessing files and printers on the server, without the requirement of a user profile on the server.  It limits access to data and allows customers to support a set of users who may only need print support but do not otherwise need server access.
   **/
  public static final String GUEST_USER_PROFILE = "GUEST_USER_PROFILE";
  static {
    attributes_.add(GUEST_USER_PROFILE, String.class, true);
    getterMap_.add (GUEST_USER_PROFILE, OLST0201_, "receiverVariable.guestUserProfile");
  }

  /**
   Attribute ID for "guest profile (pending)".  This identifies a String
   attribute, which represents the pending guest profile for the NetServer.
   If no pending guest user profile is currently configured on the server, the value of this attribute is "" (an empty String).
   <br>Note: In order to change this attribute, the server user profile being used to access the NetServer must have *SECADM special authority.  In addition, it requires *USE authority to the guest profile being set.
   **/
  public static final String GUEST_USER_PROFILE_PENDING = "GUEST_USER_PROFILE_PENDING";
  static {
    attributes_.add(GUEST_USER_PROFILE_PENDING, String.class);
    getterMap_.add (GUEST_USER_PROFILE_PENDING, OLST0201_, "receiverVariable.guestUserProfileP");
    setterMap_.add (GUEST_USER_PROFILE_PENDING, "qzlschsg", "guestUserProfileP");
  }

  /**
   Attribute ID for "idle timeout".  This identifies a read-only Integer
   attribute, which represents the amount of time, in seconds, that a connection to the NetServer will remain active once activity has ceased on that connection.
   An idle time-out value of -1 indicates no autodisconnect.
   **/
  public static final String IDLE_TIMEOUT = "IDLE_TIMEOUT";
  static {
    attributes_.add(IDLE_TIMEOUT, Integer.class, true);
    getterMap_.add (IDLE_TIMEOUT, OLST0201_, "receiverVariable.idleTimeOut");
  }

  /**
   Attribute ID for "idle timeout (pending)".  This identifies an Integer
   attribute, which represents the amount of time, in seconds, that a connection to the NetServer will remain active once activity has ceased on that connection.
   An idle time-out value of -1 indicates no autodisconnect.
   **/
  public static final String IDLE_TIMEOUT_PENDING = "IDLE_TIMEOUT_PENDING";
  static {
    attributes_.add(IDLE_TIMEOUT_PENDING, Integer.class);
    getterMap_.add (IDLE_TIMEOUT_PENDING, OLST0201_, "receiverVariable.idleTimeOutP");
    setterMap_.add (IDLE_TIMEOUT_PENDING, "qzlschsi", "requestVariable.idleTimeOutP");
  }

  /**
   Attribute ID for "logon support".  This identifies a read-only Boolean
   attribute, which indicates the logon server role for the server.
   If true, then the server is a logon server; if false, the server is not a logon server.
   <br><i>Note: This attribute corresponds to the "server role" field specified
   in the NetServer API's.</i>
   **/
  public static final String LOGON_SUPPORT = "LOGON_SUPPORT";
  static {
    attributes_.add(LOGON_SUPPORT, Boolean.class, true);
    getterMap_.add (LOGON_SUPPORT, OLST0201_, "receiverVariable.serverRole", BV_MAP_0_1_INT_);
  }

  /**
   Attribute ID for "logon support (pending)".  This identifies a Boolean
   attribute, which indicates the logon server role for the server.
   If true, then the server is a logon server; if false, the server is not a logon server.
   **/
  public static final String LOGON_SUPPORT_PENDING = "LOGON_SUPPORT_PENDING";
  static {
    attributes_.add(LOGON_SUPPORT_PENDING, Boolean.class);
    getterMap_.add (LOGON_SUPPORT_PENDING, OLST0201_, "receiverVariable.serverRoleP", BV_MAP_0_1_INT_);
    setterMap_.add (LOGON_SUPPORT_PENDING, "qzlschsi", "requestVariable.serverRoleP", BV_MAP_0_1_INT_);
  }

  /**
   Attribute ID for "name".  This identifies a read-only String
   attribute, which represents the name of the NetServer.
   <br><i>Note: The NetServer name is uppercase on the server.</i>
   **/
  public static final String NAME = "NAME";
  static {
    attributes_.add(NAME, String.class, true);
    getterMap_.add (NAME, OLST0201_, "receiverVariable.serverName");
  }

  /**
   Attribute ID for "name (pending)".  This identifies a String
   attribute, which represents the pending name of the NetServer.
   <br><i>Note: The pending NetServer name is uppercase on the server.</i>
   **/
  public static final String NAME_PENDING = "NAME_PENDING";
  static {
    attributes_.add(NAME_PENDING, String.class);
    getterMap_.add (NAME_PENDING, OLST0201_, "receiverVariable.serverNameP");
    setterMap_.add (NAME_PENDING, "qzlschsn", "serverNameP");
  }

  // Note: Hide this for now.  The API spec says it is "currently not supported".
  /**
   Attribute ID for "opportunistic lock timeout".  This identifies a read-only Integer attribute, which represents the amount of time, in seconds, that an opportunistic lock is enforced for a session or connection
   **/
  /*public*/ static final String OPPORTUNISTIC_LOCK_TIMEOUT = "OPPORTUNISTIC_LOCK_TIMEOUT";
  static {
    attributes_.add(OPPORTUNISTIC_LOCK_TIMEOUT, Integer.class, true);
    getterMap_.add (OPPORTUNISTIC_LOCK_TIMEOUT, OLST0201_, "receiverVariable.oppLockTimeOut");
  }

  // Note: Hide this for now.  The API spec says it is "currently not supported".
  /**
   Attribute ID for "opportunistic lock timeout (pending)".  This identifies an  Integer attribute, which represents the amount of time, in seconds, that an opportunistic lock is left enforced for a resource.
   **/
  /*public*/ static final String OPPORTUNISTIC_LOCK_TIMEOUT_PENDING = "OPPORTUNISTIC_LOCK_TIMEOUT_PENDING";
  static {
    attributes_.add(OPPORTUNISTIC_LOCK_TIMEOUT_PENDING, Integer.class);
    getterMap_.add (OPPORTUNISTIC_LOCK_TIMEOUT_PENDING, OLST0201_, "receiverVariable.oppLockTimeOutP");
    setterMap_.add (OPPORTUNISTIC_LOCK_TIMEOUT_PENDING, "qzlschsi", "requestVariable.oppLockTimeOutP");
  }

  /**
   Attribute ID for "WINS enablement".  This identifies a read-only Boolean
   attribute, which indicates whether the server uses a WINS server.
   **/
  public static final String WINS_ENABLEMENT = "WINS_ENABLEMENT";
  static {
    attributes_.add(WINS_ENABLEMENT, Boolean.class, true);
    getterMap_.add (WINS_ENABLEMENT, OLST0201_, "receiverVariable.winsEnablement", BV_MAP_0_1_INT_);
  }

  /**
   Attribute ID for "WINS enabled (pending)".  This identifies a Boolean
   attribute, which indicates whether the server uses a WINS server.
   **/
  public static final String WINS_ENABLEMENT_PENDING = "WINS_ENABLEMENT_PENDING";
  static {
    attributes_.add(WINS_ENABLEMENT_PENDING, Boolean.class);
    getterMap_.add (WINS_ENABLEMENT_PENDING, OLST0201_, "receiverVariable.winsEnablementP", BV_MAP_0_1_INT_);
    setterMap_.add (WINS_ENABLEMENT_PENDING, "qzlschsi", "requestVariable.winsEnablementP", BV_MAP_0_1_);
  }

  /**
   Attribute ID for "WINS primary address".  This identifies a read-only String
   attribute, which represents the IP address of the primary WINS server.
   **/
  public static final String WINS_PRIMARY_ADDRESS = "WINS_PRIMARY_ADDRESS";
  static {
    attributes_.add(WINS_PRIMARY_ADDRESS, String.class, true);
    getterMap_.add (WINS_PRIMARY_ADDRESS, OLST0201_, "receiverVariable.winsPrimaryAddress");
  }

  /**
   Attribute ID for "WINS primary address (pending)".  This identifies a String
   attribute, which represents the pending IP address of the primary WINS server.
   **/
  public static final String WINS_PRIMARY_ADDRESS_PENDING = "WINS_PRIMARY_ADDRESS_PENDING";
  static {
    attributes_.add(WINS_PRIMARY_ADDRESS_PENDING, String.class);
    getterMap_.add (WINS_PRIMARY_ADDRESS_PENDING, OLST0201_, "receiverVariable.winsPrimaryAddressP");
    setterMap_.add (WINS_PRIMARY_ADDRESS_PENDING, "qzlschsi", "requestVariable.winsPrimaryAddressP");
  }

  /**
   Attribute ID for "WINS scope ID".  This identifies a read-only String
   attribute, which represents the network scope used by the WINS server.
   If no scope ID is currently configured on the server, the value of this attribute is "" (an empty String).
   **/
  public static final String WINS_SCOPE_ID = "WINS_SCOPE_ID";
  static {
    attributes_.add(WINS_SCOPE_ID, String.class, true);
    getterMap_.add (WINS_SCOPE_ID, OLST0201_, "receiverVariable.scopeId");
  }

  /**
   Attribute ID for "WINS scope ID (pending)".  This identifies a String
   attribute, which represents the pending network scope used by the WINS server.
   If no pending scope ID is currently configured on the server, the value of this attribute is "" (an empty String).
   **/
  public static final String WINS_SCOPE_ID_PENDING = "WINS_SCOPE_ID_PENDING";
  static {
    attributes_.add(WINS_SCOPE_ID_PENDING, String.class);
    getterMap_.add (WINS_SCOPE_ID_PENDING, OLST0201_, "receiverVariable.scopeIdP");
    setterMap_.add (WINS_SCOPE_ID_PENDING, "qzlschsi", "requestVariable.scopeIdP");
  }

  /**
   Attribute ID for "WINS secondary address".  This identifies a read-only String
   attribute, which represents the IP address of the secondary WINS server.
   **/
  public static final String WINS_SECONDARY_ADDRESS = "WINS_SECONDARY_ADDRESS";
  static {
    attributes_.add(WINS_SECONDARY_ADDRESS, String.class, true);
    getterMap_.add (WINS_SECONDARY_ADDRESS, OLST0201_, "receiverVariable.winsSecondaryAddress");
  }

  /**
   Attribute ID for "WINS secondary address (pending)".  This identifies a String
   attribute, which represents the pending IP address of the secondary WINS server.
   **/
  public static final String WINS_SECONDARY_ADDRESS_PENDING = "WINS_SECONDARY_ADDRESS_PENDING";
  static {
    attributes_.add(WINS_SECONDARY_ADDRESS_PENDING, String.class);
    getterMap_.add (WINS_SECONDARY_ADDRESS_PENDING, OLST0201_, "receiverVariable.winsSecondaryAddressP");
    setterMap_.add (WINS_SECONDARY_ADDRESS_PENDING, "qzlschsi", "requestVariable.winsSecondaryAddressP");
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

  private ProgramAttributeGetter attributeGetter_;
  private ProgramAttributeSetter attributeSetter_;



  /**
   Constructs a NetServer object.
   The system must be set before the object is used.
   **/
  public NetServer()
  {
    super(presentationLoader_.getPresentationWithIcon(PRESENTATION_KEY_, ICON_BASE_NAME_), null, attributes_);

    Presentation presentation = getPresentation();
    presentation.setName("NetServer");
    presentation.setFullName("NetServer");
  }

  /**
   Constructs a NetServer object.
   @param system  The server with which the NetServer is associated.
   **/
  public NetServer(AS400 system)
  {
    this();
    try { setSystem(system); }
    catch (PropertyVetoException e) {} // This will never happen.
  }


  /**
   Calls a program via a ProgramCallDocument.  The program is expected to return a list of records.  This method checks for overrun of the receiverVariable, and re-calls the program (specifying a larger receiverVariable) if an overrun is detected.

   @exception ResourceException  If an error occurs.
   **/
  static void callListProgram(ProgramCallDocument document, String programName, boolean recordsAreFixedLength)
    throws ResourceException
  {
    try
    {
      if (document.callProgram(programName) == false) {
        throw new ResourceException(document.getMessageList(programName));
      }

      // See if we overflowed the receiver variable; if so, re-issue the API.
      String informationCompleteIndicator = (String)document.getValue(programName+".listInformation.informationCompleteIndicator");
      if (! informationCompleteIndicator.equals("C"))  // C=complete, I=incomplete
      {
        if (Trace.isTraceOn()) {
          Trace.log(Trace.DIAGNOSTIC, "Returned information incomplete on first API call.");
        }
        int totalRecords = document.getIntValue(programName+".listInformation.totalRecords");
        int recordsReturned = document.getIntValue(programName+".listInformation.recordsReturned");
        int recordLength = document.getIntValue(programName+".listInformation.recordLength");
        if (recordLength == 0) {
          if (recordsAreFixedLength) {
            Trace.log(Trace.ERROR, "System reported recordLength=0 for fixed-length format.");
          }
          recordLength = NetServerShare.ZLSL0100_MAX_RECORD_LENGTH_;  // Make a generous guess.
        }
        document.setIntValue(programName+".lengthOfReceiverVariable", totalRecords*recordLength);
        if (document.callProgram(programName) == false) {
          throw new ResourceException(document.getMessageList(programName));
        }

        // Do one more check for overflowed receiver variable.
        informationCompleteIndicator = (String)document.getValue(programName+".listInformation.informationCompleteIndicator");
        if (! informationCompleteIndicator.equals("C")) { // C=complete, I=incomplete
          document.setIntValue(programName+".lengthOfReceiverVariable", (totalRecords+10)*recordLength); // Try allowing for 10 additional records.
          if (document.callProgram(programName) == false) {
            throw new ResourceException(document.getMessageList(programName));
          }
        }
      }
    }
    catch (PcmlException e) {
      Trace.log(Trace.ERROR, "PcmlException when calling API.", e);
      throw new ResourceException(e);
    }
  }


  /**
   Commits the specified attribute changes.
   This method fires an attributeChangesCommitted() ResourceEvent.

   @exception ResourceException  If an error occurs.
   **/
  protected void commitAttributeChanges(Object[] attributeIDs, Object[] values)
    throws ResourceException
  {
    super.commitAttributeChanges(attributeIDs, values);

    if (! isConnectionEstablished()) {
      establishConnection();
    }

    attributeSetter_.setValues(attributeIDs, values);
  }


  /**
   Computes the resource key.

   @param system  The system.
   **/
  static Object computeResourceKey(AS400 system)
  {
    StringBuffer buffer = new StringBuffer();
    buffer.append(NetServer.class);
    buffer.append(':');
    buffer.append(system.getSystemName());
    return buffer.toString();
  }


  /**
   Ends the NetServer.
   <br>This method requires *IOSYSCFG special authority on the server.

   @exception ResourceException  If an error occurs.
   **/
  public void end()
    throws ResourceException
  {
    if (! isConnectionEstablished()) {
      establishConnection();
    }

    // Set the input parameters and call the API.
    try {
      ProgramCallDocument document = (ProgramCallDocument)staticDocument_.clone();
      document.setSystem(getSystem());

      if (document.callProgram("qzlsends") == false) {
        throw new ResourceException(document.getMessageList("qzlsends"));
      }

      // TBD - fireServerEnded();
    }
    catch (PcmlException e) {
      Trace.log(Trace.ERROR, "PcmlException when ending the NetServer.", e);
      throw new ResourceException(e);
    }
  }


  /**
   Establishes the connection to the server.

   <p>The method is called by the resource framework automatically
   when the connection needs to be established.

   @exception ResourceException  If an error occurs.
   **/
  protected void establishConnection()
    throws ResourceException
  {
    // Call the superclass.
    super.establishConnection();

    AS400 system = getSystem();
    String userId = system.getUserId();
    if (Trace.isTraceWarningOn())  verifyAuthority(system, userId);

    // Initialize the PCML document.
    document_ = (ProgramCallDocument)staticDocument_.clone();
    document_.setSystem(system);

    // Initialize the attribute getter.
    attributeGetter_ = new ProgramAttributeGetter(system, document_, getterMap_);

    // Initialize the attribute setter.
    attributeSetter_ = new ProgramAttributeSetter(system, document_, setterMap_);
    attributeSetter_.initializeAttributeValues(attributeGetter_);
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
    // Verify that the system has been set.
    if (getSystem() == null) {
      throw new ExtendedIllegalStateException("system",
                        ExtendedIllegalStateException.PROPERTY_NOT_SET);
    }

    // Note: We set the presentation in the constructors.

    // Update the resource key.
    if (getResourceKey() == null) {
      setResourceKey(computeResourceKey(getSystem()));
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
   Returns the value of an attribute, disregarding any uncommitted
   changes.

   @param attributeID  Identifies the attribute.
   @return             The attribute value, or null if the attribute
   value is not available.

   @exception ResourceException  If an error occurs.
   **/
  public Object getAttributeUnchangedValue(Object attributeID)
    throws ResourceException
  {
    if (! isConnectionEstablished()) {
      establishConnection();
    }

    Object value = super.getAttributeUnchangedValue(attributeID);

    if (value == null) {
      value = attributeGetter_.getValue(attributeID);
    }
    return value;
  }


  /**
   Indicates whether or not the NetServer is started.
   @return  <code>true</code> if the NetServer is started; <code>false</code> otherwise.

   @exception ResourceException  If an error occurs.
   **/
  public boolean isStarted()
    throws ResourceException
  {
    if (! isConnectionEstablished())
      establishConnection();

    RJobList jobList = new RJobList(getSystem());
    // Note: If the NetServer has been successfully started, there will be at least one QZLSSERVER job in ACTIVE status.
    // Set the selection so that only jobs with the name "QZLSSERVER", in ACTIVE status, are included in the list.
    jobList.setSelectionValue(RJobList.JOB_NAME, "QZLSSERVER");
    jobList.setSelectionValue(RJobList.PRIMARY_JOB_STATUSES, new String[] { RJob.JOB_STATUS_ACTIVE } );
    jobList.open();
    jobList.waitForComplete();
    boolean foundActiveJob;
    if (jobList.getListLength() > 0) foundActiveJob = true;
    else foundActiveJob = false;

    // Close the list.
    jobList.close();

    return foundActiveJob;
  }

  /**
   Lists all file server shares currently associated with the NetServer.
   The returned ResourceList contains NetServerFileShare objects.
   @return  Information about all current file shares.

   @exception ResourceException  If an error occurs.
   @see NetServerFileShare
   **/
  public ResourceList listFileShares()
    throws ResourceException
  {
    if (! isConnectionEstablished()) {
      establishConnection();
    }

    return NetServerFileShare.list(getSystem());
  }

  /**
   Lists all print server shares currently associated with the NetServer.
   The returned ResourceList contains NetServerPrintShare objects.
   @return  Information about all current print shares.

   @exception ResourceException  If an error occurs.
   @see NetServerPrintShare
   **/
  public ResourceList listPrintShares()
    throws ResourceException
  {
    if (! isConnectionEstablished()) {
      establishConnection();
    }

    return NetServerPrintShare.list(getSystem());
  }

  /**
   Lists all session connections currently associated with the NetServer.
   The returned ResourceList contains NetServerConnection objects.
   @return  Information about all current session connections.

   @exception ResourceException  If an error occurs.
   @see NetServerConnection
   **/
  public ResourceList listSessionConnections()
    throws ResourceException
  {
    if (! isConnectionEstablished()) {
      establishConnection();
    }

    return NetServerConnection.list(getSystem(), NetServerConnection.SESSION);
  }

  /**
   Lists all sessions currently associated with the NetServer.
   The returned ResourceList contains NetServerSession objects.
   @return  Information about all current sessions.

   @exception ResourceException  If an error occurs.
   @see NetServerSession
   **/
  public ResourceList listSessions()
    throws ResourceException
  {
    if (! isConnectionEstablished()) {
      establishConnection();
    }

    return NetServerSession.list(getSystem());
  }

  /**
   Lists all share connections currently associated with the NetServer.
   The returned ResourceList contains NetServerConnection objects.
   @return  Information about all current share connections.

   @exception ResourceException  If an error occurs.
   @see NetServerConnection
   **/
  public ResourceList listShareConnections()
    throws ResourceException
  {
    if (! isConnectionEstablished()) {
      establishConnection();
    }

    return NetServerConnection.list(getSystem(), NetServerConnection.SHARE);
  }


  /**
   Refreshes the values for all attributes.  This does not cancel
   uncommitted changes.  This method fires an attributeValuesRefreshed()
   ResourceEvent.

   @exception ResourceException  If an error occurs.
   **/
  public void refreshAttributeValues()
    throws ResourceException
  {
    if (! isConnectionEstablished()) {
      establishConnection();
    }

    if (Trace.isTraceWarningOn())  verifyAuthority(getSystem(), getSystem().getUserId());

    attributeGetter_.clearBuffer();
    attributeSetter_.initializeAttributeValues(attributeGetter_);
    super.refreshAttributeValues();
  }


  /**
   Starts the NetServer.
   If the NetServer is already started, this method does nothing.
   <br>This method requires *IOSYSCFG special authority on the server.
   <br>Note: This method does not reset the server.

   @exception ResourceException  If an error occurs.
   **/
  public void start()
    throws ResourceException
  {
    start(false);
  }

  /**
   Starts and (optionally) resets the NetServer.
   If the NetServer is already started, this method does nothing.
   <br>This method requires *IOSYSCFG special authority on the server.
   <p>Note: Reset is used when the NetServer fails to start normally on the server.  It is on the NetServer context menu so an administrator can use it.  It basically does some under-the-covers cleanup, and is used infrequently.  The times it would be used is if the server ended abnormally and there may be jobs or objects hanging around that need to be cleaned up before the server can start again.  The reset does that.

   @param reset  Whether or not the server is to be reset when started.

   @exception ResourceException  If an error occurs.
   **/
  public void start(boolean reset)
    throws ResourceException
  {
    if (! isConnectionEstablished()) {
      establishConnection();
    }
    if (isStarted()) return;


    // Set the input parameters and call the API.
    try {
      ProgramCallDocument document = (ProgramCallDocument)staticDocument_.clone();
      document.setSystem(getSystem());
      document.setValue("qzlsstrs.resetQualifier", (reset ? "1" : "0"));

      if (document.callProgram("qzlsstrs") == false) {
        throw new ResourceException(document.getMessageList("qzlsstrs"));
      }

      // TBD - fireServerStarted();
    }
    catch (PcmlException e) {
      Trace.log(Trace.ERROR, "PcmlException when starting the NetServer.", e);
      throw new ResourceException(e);
    }
  }


  /**
   Verifies that the user has *IOSYSCFG authority, and logs a warning message if not.
   This authority is required in order for the QTOCAUTO API to work.

   @exception ResourceException  If an error occurs.
   **/
  static void verifyAuthority(AS400 system, String userId)
    throws ResourceException
  {
    RUser user = new RUser(system, userId);
    String[] authorities = (String[])user.getAttributeValue(RUser.SPECIAL_AUTHORITIES);
    boolean foundRequiredAuth = false;
    for (int i=0; i<authorities.length && !foundRequiredAuth; i++) {
      if (authorities[i].equals(RUser.SPECIAL_AUTHORITIES_IO_SYSTEM_CONFIGURATION) )
      {
        foundRequiredAuth = true;
      }
    }
    if (!foundRequiredAuth) {
      Trace.log(Trace.WARNING, "User " + userId + " does not have *IOSYSCFG authority.");
      ///throw new ResourceException(ResourceException.AUTHORITY_INSUFFICIENT);
    }
  }

}
