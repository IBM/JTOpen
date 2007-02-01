///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename: ISeriesNetServer.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2000 International Business Machines Corporation and
// others. All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.io.ObjectInputStream;
import java.beans.PropertyVetoException;

/**
 The ISeriesNetServer class represents the NetServer service on a system.
 This class allows the user to query and modify the state and configuration
 of the NetServer.
 <p>
 If the NetServer job on the i5/OS system is not started, the "list" methods may return incomplete results.  To determine if the NetServer job is started, use the {@link #isStarted() isStarted} method.  To start the NetServer, use the {@link #start() start} method.
<P>
Note: The first call to one of the attribute "getter" methods will cause an implicit call to
{@link #refresh() refresh}, if refresh() hasn't yet been explicitly called.
If any exceptions are thrown by <tt>refresh()</tt> during the implicit call,
they will be logged to {@link com.ibm.as400.access.Trace#ERROR Trace.ERROR} and
ignored. However, should an exception occur during an explicit call to
<tt>refresh()</tt>, it will be thrown to the caller.
 <p>
 Note: Typically, methods which change the state or attributes of the NetServer require that the system user profile have *IOSYSCFG special authority.  For example, starting or ending the NetServer requires *IOSYSCFG authority.
 <p>
 Note: This class uses some API fields that are available only when connecting to systems at release V5R1 or higher.

<blockquote>
<pre>
* import com.ibm.as400.access.*;
*
* // Create a NetServer object for a specific system.
* AS400 system = new AS400("MYSYSTEM", "MYUSERID", "MYPASSWORD");
* ISeriesNetServer ns = new ISeriesNetServer(system);
*
* try
* {
*
*   // Get the name of the NetServer.
*   System.out.println("Name: " + ns.getName());
*
*   // Get the CCSID of the NetServer.
*   System.out.println("CCSID: " + ns.getCCSID());
*
*   // Get the "allow system name" value of the NetServer.
*   System.out.println("'Allow system name': " +  ns.isAllowSystemName());
*
*   // Set the description of the NetServer.
*   // Note: Changes will take effect after next start of NetServer.
*   ns.setDescription("The NetServer");
*   ns.commitChanges();
*
*   // Set the CCSID of the NetServer to 13488.
*   ns.setCCSID(13488);
*
*   // Set the "allow system name" value of the NetServer to true.
*   ns.setAllowSystemName(true);
*
*   // Commit the attribute changes (send them to the system).
*   ns.commitChanges();
*
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

@see com.ibm.as400.access.ISeriesNetServerFileShare
@see com.ibm.as400.access.ISeriesNetServerPrintShare
@see com.ibm.as400.access.ISeriesNetServerConnection
@see com.ibm.as400.access.ISeriesNetServerSession
**/

public class ISeriesNetServer
implements Serializable
{
  static final long serialVersionUID = 1L;

  /**
   Value of the NetServer "authentication method" attribute, indicating that the system authenticates with encrypted passwords only.
   **/
  public final static int ENCRYPTED_PASSWORDS = 0;

  /**
   Value of the NetServer "authentication method" attribute, indicating that the system authenticates with Network authentication only.
   **/
  public final static int NETWORK_AUTHENTICATION = 1;

  /**
   Value of the NetServer "authentication method" attribute, indicating that the system authenticates with Network authentication only.
   @deprecated Renamed to NETWORK_AUTHENTICATION
   **/
  public final static int KERBEROS_V5_TOKENS = NETWORK_AUTHENTICATION;

  /**
   Value of the NetServer "authentication method" attribute, indicating that the system authenticates with Network authentication when possible, but it allows clients to use encrypted passwords when needed.
   <br>Note: This value is valid only for i5/OS release V5R3 and higher.
   **/
  public final static int NETWORK_AUTHENTICATION_OR_PASSWORDS = 2;

  /**
   Value of the NetServer "authentication method" attribute, indicating that the system authenticates with Network authentication when possible, but it allows clients to use encrypted passwords when needed.
   <br>Note: This value is valid only for i5/OS release V5R3 and higher.
   @deprecated Renamed to NETWORK_AUTHENTICATION_OR_PASSWORDS
   **/
  public final static int KERBEROS_OR_PASSWORDS = NETWORK_AUTHENTICATION_OR_PASSWORDS;

  /**
   Value of the "idle timeout" attribute, indicating "no autodisconnect".
   **/
  public final static int NO_AUTO_DISCONNECT = -1;

  /**
   Value of the "opportunistic lock timeout" attribute, indicating that opportunistic locking is disabled.
   **/
  public final static int OPP_LOCK_DISABLED = -1;

  /**
   Value of the "message authentication" attribute, indicating that the system does not support message authentication.
   **/
  public final static int MSG_AUTH_NOT_SUPPORTED = 0;

  /**
   Value of the "message authentication" attribute, indicating that the system supports message authentication, and message authentication is negotiated between the client and the system.
   **/
  public final static int MSG_AUTH_NEGOTIATED = 1;

  /**
   Value of the "message authentication" attribute, indicating that the system requires message authentication for all connections.
   **/
  public final static int MSG_AUTH_REQUIRED = 2;

  /**
   Value of the "minimum message severity" attribute, indicating that administrative alert messages are not sent.
   **/
  public final static int NO_ADMIN_ALERTS = -1;

  /**
   Value of the "LAN Manager authentication" attribute, indicating that the LANMAN password hash is ignored if a stronger password hash is provided by the client.
   **/
  public final static int PASSWORD_STRONGER = 0;

  /**
   Value of the "LAN Manager authentication" attribute, indicating that the LANMAN password hash is used only if a stronger password hash provided by the client does not match, or if a stronger password hash is not provided.
   **/
  public final static int PASSWORD_STRONGER_OR_MISMATCH = 1;



  // Constants for identifying a share's "device type".
  private final static int BOTH = 2;
  private final static int FILE = 0;
  private final static int PRINT = 1;

  private final static ProgramParameter errorCode_ = new ProgramParameter(new byte[4]);
  private AS400 system_;
  private int systemVRM_;
  private boolean gotSystemVRM_;


  // Constants for identifying attributes that are specified as strings in the API's.

  private final static int ALLOW_SYSTEM_NAME      = 0;  // CHAR(1)
  private final static int AUTHENTICATION_METHOD  = 1;  // CHAR(1)
  private final static int AUTOSTART              = 2;  // CHAR(4)
  private final static int DESCRIPTION            = 3;  // CHAR(50)  a.k.a. "text description"
  private final static int DOMAIN_NAME            = 4;  // CHAR(15)
  private final static int GUEST_USER_PROFILE     = 5;  // CHAR(10)
  private final static int SERVER_NAME            = 6;  // CHAR(15)
  private final static int WINS_PRIMARY_ADDRESS   = 7;  // CHAR(15)
  private final static int WINS_SCOPE_ID          = 8;  // CHAR(224)  a.k.a. "scope ID"
  private final static int WINS_SECONDARY_ADDRESS = 9;  // CHAR(15)

  // Constants for identifying attributes that are specified as integers in the API's.

  private final static int BROWSING_INTERVAL      = 20; // BINARY(4)
  private final static int CCSID                  = 21; // BINARY(4)
  private final static int IDLE_TIMEOUT           = 22; // BINARY(4)
  private final static int LOGON_SUPPORT          = 23; // BINARY(4)  a.k.a. "server role"
  private final static int OPP_LOCK_TIMEOUT       = 24; // BINARY(4)  Not surfaced to user.
  private final static int WINS_ENABLEMENT        = 25; // BINARY(4) or CHAR(1), a.k.a. "WINS proxy"
  // Note: In the getter (QZLSOLST API), the "WINS enablement" field is BINARY(4).
  // In the setter (QZLSCHSI API), the "WINS proxy" field is CHAR(1).

  private final static int MESSAGE_AUTHENTICATION = 26; // BINARY(4)   V5R4+
  private final static int MIN_MESSAGE_SEVERITY   = 27; // BINARY(4)   V5R4+
  private final static int LAN_MGR_AUTHENTICATION = 28; // BINARY(4)   V5R4+

  private final static int MAX_ATTR = 28;   // Increment this as new attr's are added.

  private final static int ZLSL0101_MAX_RECORD_LENGTH = 1221; // Max path length is 1024 bytes.

  // Tables of "in effect" and "pending" attribute values.

  private String[] effectiveValueStr_ = new String[MAX_ATTR+1];  // in-effect String attr values
  private String[] pendingValueStr_   = new String[MAX_ATTR+1];  // pending String attr values
  private int[] effectiveValueInt_    = new int[MAX_ATTR+1];     // in-effect Integer attr values
  private int[] pendingValueInt_      = new int[MAX_ATTR+1];     // pending Integer attr values
  // Implementation note: We only partially populate the above arrays, to simplify tracking of changes.
  // That is, in the *ValueStr_ arrays, only offsets 0-9 are occupied,
  // and in the *ValueInt_ arrays, only offsets 20-28 are occupied.


  private boolean[] userChangedAttribute_ = new boolean[MAX_ATTR+1]; // which attrs the user has changed
  private boolean[] userCommittedChange_  = new boolean[MAX_ATTR+1]; // which attrs the user has committed

  // Have we done a refresh() since the last start().
  private transient boolean refreshedSinceStart_ = false;

  // Have we done a setAutoStart() since the last refresh().
  // Note that the autostart attribute is unique in that changes take effect upon commitChanges().
  private boolean changedAutoStartSinceRefresh_ = false;

  // These flags track whether the user has *IOSYSCFG special authority.
  private transient boolean determinedSpecialAuthority_ = false;
  private transient boolean userHasSpecialAuthority_ = false;

  private static final boolean DEBUG = false;


  // Note: If required, we'll make this class into a Bean later.
  // Until then, we don't provide a zero-argument "default" constructor.
  // That way we can enforce that system_ is never null.


  /**
   Constructs a NetServer object.
   @param system  The system with which the NetServer is associated.
   **/
  public ISeriesNetServer(AS400 system)
  {
    if (system == null) { throw new NullPointerException(); }

    system_ = system;
  }


  /**
   Creates a file share.
   @param shareName  The name of the share.
   @param path  The path of the share.
   @exception  AS400SecurityException  If a security or authority error occurs.
   @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
   @exception  InterruptedException  If this thread is interrupted.
   @exception  IOException  If an error occurs while communicating with the system.
   @exception  ObjectDoesNotExistException  If the system object does not exist.
   **/
  public void createFileShare(String shareName, String path)
    throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
  {
    createFileShare(shareName, path, "", ISeriesNetServerFileShare.READ_ONLY, ISeriesNetServerFileShare.NO_MAX);
  }


  /**
   Creates a file share.
   @param shareName  The name of the share.
   @param path  The path of the share.
   @param desc  The description of the share.
   @param permission  The permission of the share.
   Valid values are:
   <ul>
   <li>{@link com.ibm.as400.access.ISeriesNetServerFileShare#READ_ONLY ISeriesNetServerFileShare.READ_ONLY}
   <li>{@link com.ibm.as400.access.ISeriesNetServerFileShare#READ_WRITE ISeriesNetServerFileShare.READ_WRITE}
   </ul>
   @exception  AS400SecurityException  If a security or authority error occurs.
   @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
   @exception  InterruptedException  If this thread is interrupted.
   @exception  IOException  If an error occurs while communicating with the system.
   @exception  ObjectDoesNotExistException  If the system object does not exist.
   **/
  public void createFileShare(String shareName, String path, String desc, int permission)
    throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
  {
    createFileShare(shareName, path, desc, permission, ISeriesNetServerFileShare.NO_MAX);
  }


  /**
   Creates a file share.
   @param shareName  The name of the share.
   @param path  The path of the share.
   @param desc  The description of the share.
   @exception  AS400SecurityException  If a security or authority error occurs.
   @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
   @exception  InterruptedException  If this thread is interrupted.
   @exception  IOException  If an error occurs while communicating with the system.
   @exception  ObjectDoesNotExistException  If the system object does not exist.
   **/
  public void createFileShare(String shareName, String path, String desc)
    throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
  {
    createFileShare(shareName, path, desc, ISeriesNetServerFileShare.READ_ONLY, ISeriesNetServerFileShare.NO_MAX);
  }


  /**
   Creates a file share.
   @param shareName  The name of the share.
   @param path  The path of the share.
   @param desc  The description of the share.
   @param permission  The permission of the share.
   Valid values are:
   <ul>
   <li>{@link com.ibm.as400.access.ISeriesNetServerFileShare#READ_ONLY ISeriesNetServerFileShare.READ_ONLY}
   <li>{@link com.ibm.as400.access.ISeriesNetServerFileShare#READ_WRITE ISeriesNetServerFileShare.READ_WRITE}
   </ul>
   The default is READ_ONLY.
   @param maxUsers  The maximum number of users of the share.
   @exception  AS400SecurityException  If a security or authority error occurs.
   @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
   @exception  InterruptedException  If this thread is interrupted.
   @exception  IOException  If an error occurs while communicating with the system.
   @exception  ObjectDoesNotExistException  If the system object does not exist.
   **/
  public void createFileShare(String shareName, String path, String desc, int permission, int maxUsers)
    throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
  {
    if (shareName == null) { throw new NullPointerException("shareName"); }
    if (path == null) { throw new NullPointerException("path"); }
    if (desc == null) { desc = ""; }

    final int ccsid = system_.getCcsid();
    final CharConverter conv = new CharConverter(ccsid);
    final AS400Text text12 = new AS400Text(12, ccsid);
    final AS400Text text50 = new AS400Text(50, ccsid);

    byte[] pathBytes = path.trim().getBytes("UnicodeBigUnmarked");
    ProgramParameter[] parms = new ProgramParameter[8];

    parms[0] = new ProgramParameter(text12.toBytes(shareName.trim()));
    parms[1] = new ProgramParameter(pathBytes);
    parms[2] = new ProgramParameter(BinaryConverter.intToByteArray(pathBytes.length));
    parms[3] = new ProgramParameter(BinaryConverter.intToByteArray(13488));
    parms[4] = new ProgramParameter(text50.toBytes(desc));
    parms[5] = new ProgramParameter(BinaryConverter.intToByteArray(permission));
    parms[6] = new ProgramParameter(BinaryConverter.intToByteArray(maxUsers));
    parms[7] = errorCode_;

    ProgramCall pc = new ProgramCall(system_, "/QSYS.LIB/QZLSADFS.PGM", parms);

    if (!pc.run()) {
      throw new AS400Exception(pc.getMessageList());
    }
  }

  /**
   Creates a print share.
   The spooled file type is set to {@link com.ibm.as400.access.ISeriesNetServerPrintShare#AUTO_DETECT ISeriesNetServerPrintShare.AUTO_DETECT}.
   @param shareName  The name of the share.
   @param outqLib  The library that contains the output queue for the share.
   @param outqName  The name of the output queue for the share.
   @exception  AS400SecurityException  If a security or authority error occurs.
   @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
   @exception  InterruptedException  If this thread is interrupted.
   @exception  IOException  If an error occurs while communicating with the system.
   @exception  ObjectDoesNotExistException  If the system object does not exist.
   **/
  public void createPrintShare(String shareName, String outqLib, String outqName)
    throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
  {
    createPrintShare(shareName, outqLib, outqName, "", ISeriesNetServerPrintShare.AUTO_DETECT, null, null, null, false);
  }


  /**
   Creates a print share.
   @param shareName  The name of the share.
   @param outqLib  The library that contains the output queue for the share.
   @param outqName  The name of the output queue for the share.
   @param desc  The description of the share.
   @param splfType  The type of spooled files that are created using this share.
   Valid values are:
   <ul>
   <li>{@link com.ibm.as400.access.ISeriesNetServerPrintShare#AFP ISeriesNetServerPrintShare.AFP}
   <li>{@link com.ibm.as400.access.ISeriesNetServerPrintShare#AUTO_DETECT ISeriesNetServerPrintShare.AUTO_DETECT}
   <li>{@link com.ibm.as400.access.ISeriesNetServerPrintShare#SCS ISeriesNetServerPrintShare.SCS}
   <li>{@link com.ibm.as400.access.ISeriesNetServerPrintShare#USER_ASCII ISeriesNetServerPrintShare.USER_ASCII}
   </ul>
   @exception  AS400SecurityException  If a security or authority error occurs.
   @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
   @exception  InterruptedException  If this thread is interrupted.
   @exception  IOException  If an error occurs while communicating with the system.
   @exception  ObjectDoesNotExistException  If the system object does not exist.
   **/
  public void createPrintShare(String shareName, String outqLib, String outqName, String desc, int splfType)
    throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
  {
    createPrintShare(shareName, outqLib, outqName, desc, splfType, null, null, null, false);
  }


  /**
   Creates a print share.
   @param shareName  The name of the share.
   @param outqLib  The library that contains the output queue for the share.
   @param outqName  The name of the output queue for the share.
   @param desc  The description of the share.
   @param splfType  The type of spooled files that are created using this share.
   Valid values are:
   <ul>
   <li>{@link com.ibm.as400.access.ISeriesNetServerPrintShare#AFP ISeriesNetServerPrintShare.AFP}
   <li>{@link com.ibm.as400.access.ISeriesNetServerPrintShare#AUTO_DETECT ISeriesNetServerPrintShare.AUTO_DETECT}
   <li>{@link com.ibm.as400.access.ISeriesNetServerPrintShare#SCS ISeriesNetServerPrintShare.SCS}
   <li>{@link com.ibm.as400.access.ISeriesNetServerPrintShare#USER_ASCII ISeriesNetServerPrintShare.USER_ASCII}
   </ul>
   @param prtDriver  The print driver that is appropriate for this share.
   @param prtFileLib  The library that contains the printer file.
   @param prtFileName  The name of the printer file.  This is a template containing attributes used to create spooled files.
   @exception  AS400SecurityException  If a security or authority error occurs.
   @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
   @exception  InterruptedException  If this thread is interrupted.
   @exception  IOException  If an error occurs while communicating with the system.
   @exception  ObjectDoesNotExistException  If the system object does not exist.
   **/
  public void createPrintShare(String shareName, String outqLib, String outqName, String desc, int splfType, String prtDriver, String prtFileLib, String prtFileName)
    throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
  {
    createPrintShare(shareName, outqLib, outqName, desc, splfType, prtDriver, prtFileLib, prtFileName, false);
  }


  /**
   Creates a print share.
   @param shareName  The name of the share.
   @param outqLib  The library that contains the output queue for the share.
   @param outqName  The name of the output queue for the share.
   @param desc  The description of the share.
   @param splfType  The type of spooled files that are created using this share.
   Valid values are:
   <ul>
   <li>{@link com.ibm.as400.access.ISeriesNetServerPrintShare#AFP ISeriesNetServerPrintShare.AFP}
   <li>{@link com.ibm.as400.access.ISeriesNetServerPrintShare#AUTO_DETECT ISeriesNetServerPrintShare.AUTO_DETECT}
   <li>{@link com.ibm.as400.access.ISeriesNetServerPrintShare#SCS ISeriesNetServerPrintShare.SCS}
   <li>{@link com.ibm.as400.access.ISeriesNetServerPrintShare#USER_ASCII ISeriesNetServerPrintShare.USER_ASCII}
   </ul>
   @param prtDriver  The print driver that is appropriate for this share.
   @param prtFileLib  The library that contains the printer file.
   @param prtFileName  The name of the printer file.  This is a template containing attributes used to create spooled files.
   @param publish  Whether to publish this print share.  Default is false.
   @exception  AS400SecurityException  If a security or authority error occurs.
   @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
   @exception  InterruptedException  If this thread is interrupted.
   @exception  IOException  If an error occurs while communicating with the system.
   @exception  ObjectDoesNotExistException  If the system object does not exist.
   **/
  public void createPrintShare(String shareName, String outqLib, String outqName, String desc, int splfType, String prtDriver, String prtFileLib, String prtFileName, boolean publish)
    throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
  {
    if (shareName == null) { throw new NullPointerException("shareName"); }
    if (outqLib == null) { throw new NullPointerException("outqLib"); }
    if (outqName == null) { throw new NullPointerException("outqName"); }
    if (desc == null) {
      desc = "";
    }
    if (splfType < ISeriesNetServerPrintShare.USER_ASCII ||
        splfType > ISeriesNetServerPrintShare.AUTO_DETECT) {
      throw new ExtendedIllegalArgumentException(Integer.toString(splfType), ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }
    if (prtDriver == null) {
      prtDriver = "";
    }
    if (prtFileLib == null) {
      prtFileLib = "";
    }
    if (prtFileName == null) {
      prtFileName = "";
    }

    final int ccsid = system_.getCcsid();
    final CharConverter conv = new CharConverter(ccsid);
    final AS400Text text12 = new AS400Text(12, ccsid);
    final AS400Text text10 = new AS400Text(10, ccsid);
    final AS400Text text50 = new AS400Text(50, ccsid);

    byte[] outqBytes = new byte[20];
    byte[] prtFileBytes = new byte[20];
    byte[] pubBytes = new byte[1];

    text10.toBytes(outqLib, outqBytes, 10);
    text10.toBytes(outqName, outqBytes, 0);
    text10.toBytes(prtFileLib, prtFileBytes, 0);
    text10.toBytes(prtFileName, prtFileBytes, 10);
    if (publish) {
      pubBytes[0] = (byte)0xF1;
    }
    else {
      pubBytes[0] = (byte)0xF0;
    }

    ProgramParameter[] parms = new ProgramParameter[8];

    parms[0] = new ProgramParameter(text12.toBytes(shareName.trim()));
    parms[1] = new ProgramParameter(outqBytes);
    parms[2] = new ProgramParameter(text50.toBytes(desc));
    parms[3] = new ProgramParameter(BinaryConverter.intToByteArray(splfType));
    parms[4] = new ProgramParameter(text50.toBytes(desc));
    parms[5] = errorCode_;
    parms[6] = new ProgramParameter(prtFileBytes);
    parms[7] = new ProgramParameter(pubBytes);

    ProgramCall pc = new ProgramCall(system_, "/QSYS.LIB/QZLSADPS.PGM", parms);

    if (!pc.run()) {
      throw new AS400Exception(pc.getMessageList());
    }
  }


  /**
   Returns the system.
   @return The system.
   **/
  public AS400 getSystem()
  {
    return system_;
  }


  /**
   Returns the system VRM.
   @return The system VRM.
   **/
  private final int getSystemVRM()
    throws AS400SecurityException, IOException
  {
    if (!gotSystemVRM_) { systemVRM_ = system_.getVRM(); gotSystemVRM_ = true; }
    return systemVRM_;
  }


  /**
   Lists all file shares.
   @return  The file shares.
   @exception  AS400SecurityException  If a security or authority error occurs.
   @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
   @exception  InterruptedException  If this thread is interrupted.
   @exception  IOException  If an error occurs while communicating with the system.
   @exception  ObjectDoesNotExistException  If the system object does not exist.
   **/
  public ISeriesNetServerFileShare[] listFileShares()
    throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
  {
    return (ISeriesNetServerFileShare[])listShares(FILE, null);
  }


  /**
   Lists the file shares.
   @param shareName  The name of the share to list.
   @return  The file shares.
   @exception  AS400SecurityException  If a security or authority error occurs.
   @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
   @exception  InterruptedException  If this thread is interrupted.
   @exception  IOException  If an error occurs while communicating with the system.
   @exception  ObjectDoesNotExistException  If the system object does not exist.
   **/
  public ISeriesNetServerFileShare[] listFileShares(String shareName)
    throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
  {
    if (shareName == null) { throw new NullPointerException(); }

    return (ISeriesNetServerFileShare[])listShares(FILE, shareName);
  }


  /**
   Lists all print shares.
   @return  The print shares.
   @exception  AS400SecurityException  If a security or authority error occurs.
   @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
   @exception  InterruptedException  If this thread is interrupted.
   @exception  IOException  If an error occurs while communicating with the system.
   @exception  ObjectDoesNotExistException  If the system object does not exist.
   **/
  public ISeriesNetServerPrintShare[] listPrintShares()
    throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
  {
    return (ISeriesNetServerPrintShare[])listShares(PRINT, null);
  }


  /**
   Lists the print shares.
   @param shareName  The name of the share to list.
   @return  The print shares.
   @exception  AS400SecurityException  If a security or authority error occurs.
   @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
   @exception  InterruptedException  If this thread is interrupted.
   @exception  IOException  If an error occurs while communicating with the system.
   @exception  ObjectDoesNotExistException  If the system object does not exist.
   **/
  public ISeriesNetServerPrintShare[] listPrintShares(String shareName)
    throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
  {
    if (shareName == null) { throw new NullPointerException(); }

    return (ISeriesNetServerPrintShare[])listShares(PRINT, shareName);
  }


  /**
   Lists the shares (both file shares and print shares).
   @return  The shares.
   @exception  AS400SecurityException  If a security or authority error occurs.
   @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
   @exception  InterruptedException  If this thread is interrupted.
   @exception  IOException  If an error occurs while communicating with the system.
   @exception  ObjectDoesNotExistException  If the system object does not exist.
   **/
  public ISeriesNetServerShare[] listShares()
    throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
  {
    return (ISeriesNetServerShare[])listShares(BOTH, "*ALL");
  }

  // 3 options for "desired type": FILE, PRINT, and BOTH.
  private Object listShares(int desiredType, String shareName)
    throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
  {
    if (shareName == null) { shareName = "*ALL"; }

    final int ccsid = system_.getCcsid();
    final CharConverter conv = new CharConverter(ccsid);
    final AS400Text text15 = new AS400Text(15, ccsid);

    int len = 8192;
    ProgramParameter[] parms = new ProgramParameter[6];

    parms[0] = new ProgramParameter(len);                     // receiver variable
    parms[1] = new ProgramParameter(BinaryConverter.intToByteArray(len));// length of receiver variable
    parms[2] = new ProgramParameter(64);                      // list information
    parms[3] = new ProgramParameter(conv.stringToByteArray("ZLSL0101"));
    parms[4] = new ProgramParameter(text15.toBytes(shareName.trim()));
    parms[5] = errorCode_;

    ProgramCall pc = new ProgramCall(system_, "/QSYS.LIB/QZLSOLST.PGM", parms);

    int numRecords = callListProgram(pc, parms, len);

    byte[] data = parms[0].getOutputData();

    return parseZLSL0101(data, conv, numRecords, desiredType, null);
  }


  /**
   Calls a NetServer listing API.  The API is expected to return a list of records.  This method checks for overrun of the receiverVariable, and re-invokes the API, specifying a larger receiverVariable, if an overrun is detected.

   @return Number of records listed.
   **/
  private static int callListProgram(ProgramCall pc, ProgramParameter[] parms, int totalBytesExpected)
    throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
  {
    if (!pc.run()) {
      throw new AS400Exception(pc.getMessageList());
    }

    // See if we overflowed the receiver variable; if so, re-issue the API.
    // Examine the "information complete indicator" field in the "list information" output parm.
    byte[] listInfo = parms[2].getOutputData();
    int totalRecordsAvail = BinaryConverter.byteArrayToInt(listInfo, 0);
    int numRecordsReturned = BinaryConverter.byteArrayToInt(listInfo, 4);
    if (Trace.isTraceOn() && numRecordsReturned < totalRecordsAvail) {
      Trace.log(Trace.DIAGNOSTIC, "Returned info is incomplete on first API call.");
    }

    while (numRecordsReturned < totalRecordsAvail)
    {
      // Bump up the "length of receiver variable" value, to accommodate more returned data.
      int recLength = BinaryConverter.byteArrayToInt(listInfo, 8);
      if (recLength > 0) {
        totalBytesExpected += recLength * totalRecordsAvail;
      }
      else {
        totalBytesExpected *= 1 + (totalRecordsAvail / numRecordsReturned);
      }
      if (DEBUG) {
        System.out.println("DEBUG ISeriesNetServer.callListProgram("+pc.getProgram()+"): totalRecordsAvail/numRecordsReturned/recLength/totalBytesExpected == " + totalRecordsAvail+"/"+numRecordsReturned+"/"+recLength+"/"+totalBytesExpected);
      }
      try
      {
        parms[0].setOutputDataLength(totalBytesExpected);
        parms[1].setInputData(BinaryConverter.intToByteArray(totalBytesExpected));
      }
      catch (PropertyVetoException pve) {}  // this will never happen

      // Call the program again.
      if (!pc.run()) {
        throw new AS400Exception(pc.getMessageList());
      }
      listInfo = parms[2].getOutputData();
      totalRecordsAvail = BinaryConverter.byteArrayToInt(listInfo, 0);
      numRecordsReturned = BinaryConverter.byteArrayToInt(listInfo, 4);
    }
    return numRecordsReturned;
  }


  /**
   Lists all NetServer sessions.
   @return  The current sessions.
   @exception  AS400SecurityException  If a security or authority error occurs.
   @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
   @exception  InterruptedException  If this thread is interrupted.
   @exception  IOException  If an error occurs while communicating with the system.
   @exception  ObjectDoesNotExistException  If the system object does not exist.
   **/
  public ISeriesNetServerSession[] listSessions()
    throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
  {
    return listSessionsForWorkstation("*ALL");
  }


  /**
   Lists all NetServer sessions for the specified workstation.
   @param name  The name of the workstation.
   @return  The current sessions for the workstation.
   @exception  AS400SecurityException  If a security or authority error occurs.
   @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
   @exception  InterruptedException  If this thread is interrupted.
   @exception  IOException  If an error occurs while communicating with the system.
   @exception  ObjectDoesNotExistException  If the system object does not exist.
   **/
  public ISeriesNetServerSession[] listSessionsForWorkstation(String name)
    throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
  {
    final int ccsid = system_.getCcsid();
    final CharConverter conv = new CharConverter(ccsid);
    final AS400Text text15 = new AS400Text(15, ccsid);

    
    int vrm = getSystemVRM();	// @IPv6
    int recordLength = (vrm < 0x00050500) ? 64 : 113;	// @IPv6
    int len = 20*recordLength; // Expect about 20 records, each one 64 bytes in length if V5R4 or earlier, else 113 bytes in length. @IPv6C
    
    ProgramParameter[] parms = new ProgramParameter[(vrm < 0x00050500) ? 6 : 8];	// @IPv6 V5R5 and later has two addional possible parameters for expanded workstation name info

    parms[0] = new ProgramParameter(len);                                // receiver variable
    parms[1] = new ProgramParameter(BinaryConverter.intToByteArray(len));// length of receiver variable
    parms[2] = new ProgramParameter(64);                      // list information
    parms[3] = new ProgramParameter(conv.stringToByteArray("ZLSL0300"));
    if(vrm < 0x00050500)		// @IPv6
    	parms[4] = new ProgramParameter(text15.toBytes(name));  // information qualifier
    else												// @IPv6
    {													// @IPv6
    	AS400Text text10 = new AS400Text(10, ccsid);	// @IPv6 Session User
        AS400Text text45 = new AS400Text(45, ccsid);	// @IPv6 Expanded Information Qualifier
        parms[4] = new ProgramParameter(text15.toBytes("*EXPANDED"));	// @IPv6
        parms[6] = new ProgramParameter(text10.toBytes("*ALL"));		// @IPv6
        parms[7] = new ProgramParameter(text45.toBytes(name));			// @IPv6
    }													// @IPv6
    parms[5] = errorCode_;

    ProgramCall pc = new ProgramCall(system_, "/QSYS.LIB/QZLSOLST.PGM", parms);
    int numRecords = callListProgram(pc, parms, len);

    byte[] data = parms[0].getOutputData();
    ISeriesNetServerSession[] sessions = new ISeriesNetServerSession[numRecords];
    int offset = 0;

    for (int i = 0; i < numRecords; ++i)
    {
      ISeriesNetServerSession sess = parseZLSL0300(data, offset, conv, null);
      sessions[i] = sess;
      if(getSystemVRM() < 0x00050500) 	// @IPv6
    	  offset += 64;					// increment by length of a ZLSL0300 entry
      else								// @IPv6 increment by length of a ZLSL0300 entry, 
    	  offset += 113;  				// @IPv6 V5R5 and later has a Binary(4) for expanded workstation type and a Char(45) for expanded workstation name
    }
    return sessions;
  }


  /**
   Lists all connections for the specified NetServer session.
   @param sessionID  The session ID.
   Note: The "session identifier" was added to the NetServer API's in V5R1.
   @return  The current connections for the specified session.
   @exception  AS400SecurityException  If a security or authority error occurs.
   @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
   @exception  InterruptedException  If this thread is interrupted.
   @exception  IOException  If an error occurs while communicating with the system.
   @exception  ObjectDoesNotExistException  If the system object does not exist.
   **/
  public ISeriesNetServerConnection[] listConnectionsForSession(long sessionID)
    throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
  {
    final int ccsid = system_.getCcsid();
    final CharConverter conv = new CharConverter(ccsid);
    final AS400Text text10 = new AS400Text(10, ccsid);
    final AS400Text text15 = new AS400Text(15, ccsid);

    int len = 20*64; // Expect about 20 records, each one 64 bytes in length.

    ProgramParameter[] parms = new ProgramParameter[8];

    parms[0] = new ProgramParameter(len);                                // receiver variable
    parms[1] = new ProgramParameter(BinaryConverter.intToByteArray(len));// length of receiver variable
    parms[2] = new ProgramParameter(64);                           // list information
    parms[3] = new ProgramParameter(conv.stringToByteArray("ZLSL0600"));
    parms[4] = new ProgramParameter(text15.toBytes("*SESSID"));  // information qualifier
    parms[5] = errorCode_;
    parms[6] = new ProgramParameter(text10.toBytes("*SESSID"));  // session user
    parms[7] = new ProgramParameter(BinaryConverter.longToByteArray(sessionID)); // session identifier

    ProgramCall pc = new ProgramCall(system_, "/QSYS.LIB/QZLSOLST.PGM", parms);
    int numRecords = callListProgram(pc, parms, len);

    ISeriesNetServerConnection[] connections = new ISeriesNetServerConnection[numRecords];
    int offset = 0;
    byte[] data = parms[0].getOutputData();
    for (int i = 0; i < numRecords; ++i)
    {
      ISeriesNetServerConnection conn = parseZLSL0600or0700(data, offset, conv, true, null);
      connections[i] = conn;
      offset += 64;  // increment by length of a ZLSL0600 entry
    }
    return connections;
  }


  /**
   Lists all connections for the specified NetServer workstation.
   @param workstationName  The name of the workstation.
   @return  The current connections for the specified session.
   @exception  AS400SecurityException  If a security or authority error occurs.
   @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
   @exception  InterruptedException  If this thread is interrupted.
   @exception  IOException  If an error occurs while communicating with the system.
   @exception  ObjectDoesNotExistException  If the system object does not exist.
   **/
  public ISeriesNetServerConnection[] listConnectionsForSession(String workstationName)
    throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
  {
    if (workstationName == null) { throw new NullPointerException(); }

    final int ccsid = system_.getCcsid();
    final CharConverter conv = new CharConverter(ccsid);
    final AS400Text text15 = new AS400Text(15, ccsid);
    
    int len = 20*64; // Expect about 20 records, each one 64 bytes in length.

    int vrm = getSystemVRM();	// @IPv6
    ProgramParameter[] parms = new ProgramParameter[(vrm < 0x00050500) ? 6 : 8];	// @IPv6 V5R5 and later has two additional parameters for expanded workstation name

    parms[0] = new ProgramParameter(len);                                // receiver variable
    parms[1] = new ProgramParameter(BinaryConverter.intToByteArray(len));// length of receiver variable
    parms[2] = new ProgramParameter(64);                           // list information
    parms[3] = new ProgramParameter(conv.stringToByteArray("ZLSL0600"));
    if(vrm < 0x00050500)		// @IPv6
    	parms[4] = new ProgramParameter(text15.toBytes(workstationName));  // information qualifier
    else									// @IPv6
    {										// @IPv6
    	AS400Text text45 = new AS400Text(45, ccsid);	// @IPv6 for Expanded Information Qualifier
        AS400Text text10 = new AS400Text(10, ccsid);	// @IPv6 for Session User
    	parms[4] = new ProgramParameter(text15.toBytes("*EXPANDED"));	// @IPv6
    	parms[6] = new ProgramParameter(text10.toBytes("*ALL"));		// @IPv6
    	parms[7] = new ProgramParameter(text45.toBytes(workstationName));	// @IPv6
    }										// @IPv6
    parms[5] = errorCode_;

    ProgramCall pc = new ProgramCall(system_, "/QSYS.LIB/QZLSOLST.PGM", parms);
    int numRecords = callListProgram(pc, parms, len);

    ISeriesNetServerConnection[] connections = new ISeriesNetServerConnection[numRecords];
    int offset = 0;
    byte[] data = parms[0].getOutputData();
    for (int i = 0; i < numRecords; ++i)
    {
      ISeriesNetServerConnection conn = parseZLSL0600or0700(data, offset, conv, true, null);
      connections[i] = conn;
      offset += 64;  // increment by length of a ZLSL0600 entry
    }
    return connections;
  }


  /**
   Parses one or more ZLSL0101 structures.
   This is the "share information" returned by the QZLSOLST API.
   **/
  // Note: If oldShare is non-null, then this method ignores numRecords.
  // In that case, this method simply updates the attributes of the specified Share,
  // and returns null.
  private static ISeriesNetServerShare[] parseZLSL0101(byte[] data, CharConverter conv, int numRecords, int desiredType, ISeriesNetServerShare oldShare)
  {
    ISeriesNetServerFileShare[] fileShares = (desiredType == FILE ? new ISeriesNetServerFileShare[numRecords] : null);
    ISeriesNetServerPrintShare[] printShares = (desiredType == PRINT ? new ISeriesNetServerPrintShare[numRecords] : null);
    ISeriesNetServerShare[] allShares = (desiredType == BOTH ? new ISeriesNetServerShare[numRecords] : null);

    int counter = 0;  // index into whichever Share array we are building
    int offsetInData = 0;   // offset into the data buffer

    for (int recordNum=0; recordNum<numRecords; recordNum++)
    {
      int entryLength = BinaryConverter.byteArrayToInt(data, offsetInData + 0);

      int deviceType = BinaryConverter.byteArrayToInt(data, offsetInData + 16);// file vs. print
      int maxUsers = BinaryConverter.byteArrayToInt(data, offsetInData + 24);
      int currentUsers = BinaryConverter.byteArrayToInt(data, offsetInData + 28);

      if (deviceType == FILE)  // It's a file share.
      {
        if (desiredType == FILE || desiredType == BOTH)  // Do we care about it
        {
          String shareName = conv.byteArrayToString(data, offsetInData + 4, 12).trim();
          String description = conv.byteArrayToString(data, offsetInData + 114, 50).trim();
          int permissions = BinaryConverter.byteArrayToInt(data, offsetInData + 20);
          int pathOffset = BinaryConverter.byteArrayToInt(data, offsetInData + 36);
          int pathLength = BinaryConverter.byteArrayToInt(data, offsetInData + 40);
          String pathName = conv.byteArrayToString(data, /*offsetInData +*/ pathOffset, pathLength).trim();

          // New fields added in ZLSL0101:
          int ccsidForTextConv = BinaryConverter.byteArrayToInt(data, offsetInData + 184);
          String enableTextConv = conv.byteArrayToString(data, offsetInData + 196, 1).trim();
          int extTableOffset = BinaryConverter.byteArrayToInt(data, offsetInData + 188);
          int numExtTableEntries = BinaryConverter.byteArrayToInt(data, offsetInData + 192);

          // Store the File Extension Table entries into a pair of arrays (lengths, values).
          // From the API spec: "Length of file extension: The size in bytes of the file extension. The length does not include the byte used for null-termination."
          String[] extensions = new String[numExtTableEntries];
          int offsetToTableEntry = extTableOffset;
          for (int extNum=0; extNum < numExtTableEntries; extNum++) {
            int extensionLength = BinaryConverter.byteArrayToInt(data, offsetToTableEntry + 0);
            if (DEBUG) {
              if (extensionLength <= 0 || extensionLength > 20)
                System.out.println("DEBUG ISeriesNetServer.parseZLSL0101: Questionable extension length: " + extensionLength);
            }
            extensions[extNum] = conv.byteArrayToString(data, offsetToTableEntry + 4, extensionLength).trim();
            offsetToTableEntry += 50;  // Each table entry is exactly 50 bytes long.
          }

          if (oldShare == null) { // The caller wants a new object created.
            ISeriesNetServerFileShare share =
              new ISeriesNetServerFileShare(shareName, permissions, maxUsers,
                                            currentUsers, description, pathName,
                                            ccsidForTextConv, enableTextConv,
                                            extensions);
            if (desiredType == FILE) fileShares[counter++] = share;
            else allShares[counter++] = share;
          }
          else { // The caller specified an existing Share object.  Just update it.
            ((ISeriesNetServerFileShare)oldShare).setAttributeValues(shareName,
                                                                     permissions,
                                                                     maxUsers,
                                                                     currentUsers,
                                                                     description,
                                                                     pathName,
                                                                     ccsidForTextConv,
                                                                     enableTextConv,
                                                                     extensions);
            return null; // Don't bother creating an array to return.
          }
        }
      }
      else  // It's a print share.
      {
        if (desiredType == PRINT || desiredType == BOTH)  // Do we care about it
        {
          String shareName = conv.byteArrayToString(data, offsetInData + 4, 12).trim();
          String description = conv.byteArrayToString(data, offsetInData + 114, 50).trim();
          int spooledFileType = BinaryConverter.byteArrayToInt(data, offsetInData + 32);

          String outQueue = conv.byteArrayToString(data, offsetInData + 44, 20); // don't trim

          String printDriverType = conv.byteArrayToString(data, offsetInData + 64, 50).trim();

          // New fields added in ZLSL0101:
          String printerFile = conv.byteArrayToString(data, offsetInData + 164, 20); // don't trim
          String publish = conv.byteArrayToString(data, offsetInData + 197, 1).trim();
          boolean isPublished = (publish.equals("1") ? true : false);

          if (oldShare == null) { // The caller wants a new object created.
            ISeriesNetServerPrintShare share =
              new ISeriesNetServerPrintShare(shareName, spooledFileType,
                                             outQueue, printDriverType, description,
                                             printerFile, isPublished);
            if (desiredType == PRINT) printShares[counter++] = share;
            else allShares[counter++] = share;
          }
          else { // The caller specified an existing Share object.  Just update it.
            ((ISeriesNetServerPrintShare)oldShare).setAttributeValues(shareName,
                                                                      spooledFileType,
                                                                      outQueue,
                                                                      printDriverType,
                                                                      description,
                                                                      printerFile,
                                                                      isPublished);
            return null; // Don't bother creating an array to return.
          }
        }
      }
      offsetInData += entryLength;
    }

    if (desiredType == BOTH)
    {
      return allShares;
    }

    // If we didn't fill our array, truncate it to length.

    Object source = (desiredType == FILE ? (Object)fileShares : (Object)printShares);
    if (counter < numRecords)
    {
      Object temp = (desiredType == FILE ? (Object)(new ISeriesNetServerFileShare[counter]) :
                     (Object)(new ISeriesNetServerPrintShare[counter]));

      System.arraycopy(source, 0, temp, 0, counter);
      return (ISeriesNetServerShare[])temp;
    }
    else return (ISeriesNetServerShare[])source;
  }


  /**
   * Parses a single ZLSL0300 structure.
   * This is the "session information" returned by the QZLSOLST API.
   * @throws IOException   
   * @throws AS400SecurityException 
   **/
  private ISeriesNetServerSession parseZLSL0300(byte[] data, int offset, CharConverter conv, ISeriesNetServerSession sess) throws AS400SecurityException, IOException  // @IPv6 added throws declaration, removed static qualifier
  {
    String workstationName = (getSystemVRM() < 0x00050500) ? conv.byteArrayToString(data,offset+0,15).trim() : conv.byteArrayToString(data, offset+68, 45).trim();	// @IPv6 
    String userProfileName = conv.byteArrayToString(data,offset+15,10).trim();
    int numberOfConnections = BinaryConverter.byteArrayToInt(data, offset+28);
    int numberOfFilesOpen = BinaryConverter.byteArrayToInt(data, offset+32);
    int numberOfSessions = BinaryConverter.byteArrayToInt(data, offset+36);
    int sessionTime = BinaryConverter.byteArrayToInt(data, offset+40);
    int sessionIdleTime = BinaryConverter.byteArrayToInt(data, offset+44);
    String logonType = conv.byteArrayToString(data,offset+48,1).trim();
    String encryptedPassword = conv.byteArrayToString(data,offset+49,1).trim();
    long sessionID = BinaryConverter.byteArrayToLong(data, offset+56);

    boolean isPasswordEncrypted = (encryptedPassword.equals("1") ? true : false);
    boolean isGuest = (logonType.equals("0") ? true : false);

    if (sess == null) {
      sess = new ISeriesNetServerSession(workstationName,
                                         sessionID, numberOfConnections,
                                         sessionTime, numberOfFilesOpen,
                                         sessionIdleTime,
                                         isPasswordEncrypted,
                                         isGuest, userProfileName);
    }
    else {
      sess.setAttributeValues(workstationName,
                              sessionID, numberOfConnections,
                              sessionTime, numberOfFilesOpen,
                              sessionIdleTime,
                              isPasswordEncrypted,
                              isGuest, userProfileName);
    }
    return sess;
  }


  /**
   Parses a single ZLSL0600 or ZLSL0700 structure.
   This is the "session connection information" and "share connection information" returned by the QZLSOLST API.
   **/
  private ISeriesNetServerConnection parseZLSL0600or0700(byte[] data, int offset, CharConverter conv, boolean is0600, ISeriesNetServerConnection conn)
    throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
  {
    int connectionID = BinaryConverter.byteArrayToInt(data, offset+0);
    int connectionType = BinaryConverter.byteArrayToInt(data, offset+4);
    int numberOfFilesOpen = BinaryConverter.byteArrayToInt(data, offset+8);
    int numberOfConnectionUsers = BinaryConverter.byteArrayToInt(data, offset+12);
    int connectionTime = BinaryConverter.byteArrayToInt(data, offset+16);
    String userName = conv.byteArrayToString(data,offset+20,10).trim();
    String resourceName;
    int resourceType;
    if (is0600) {
      resourceName = conv.byteArrayToString(data,offset+30,12).trim();  // share name
      resourceType = ISeriesNetServerConnection.SHARE;
    }
    else {
      resourceName = (getSystemVRM() < 0x00050500) ? conv.byteArrayToString(data,offset+30,15).trim() : conv.byteArrayToString(data, offset+68, 45).trim(); // workstation name @IPv6
      resourceType = ISeriesNetServerConnection.WORKSTATION;
    }

    long sessionID;
    if (getSystemVRM() >= 0x00050100) { // new field added to API in V5R1
      sessionID = BinaryConverter.byteArrayToLong(data, offset+48);
    }
    else sessionID = 0L;

    if (conn == null) {
      conn = new ISeriesNetServerConnection(connectionID,
                                            connectionTime, numberOfFilesOpen, connectionType, resourceName, resourceType, userName, numberOfConnectionUsers, sessionID);
    }
    else {
      conn.setAttributeValues(connectionID,
                              connectionTime, numberOfFilesOpen, connectionType, resourceName, resourceType, userName, numberOfConnectionUsers, sessionID);
    }
    return conn;
  }


  /**
   Returns the connections associated with a specific share.
   @param shareName The name of the share.
   @return  The connections for the specified share.
   @exception  AS400SecurityException  If a security or authority error occurs.
   @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
   @exception  InterruptedException  If this thread is interrupted.
   @exception  IOException  If an error occurs while communicating with the system.
   @exception  ObjectDoesNotExistException  If the system object does not exist.
   **/
  public ISeriesNetServerConnection[] listConnectionsForShare(String shareName)
    throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
  {
    if (shareName == null) { throw new NullPointerException(); }

    final int ccsid = system_.getCcsid();
    final CharConverter conv = new CharConverter(ccsid);
    final AS400Text text15 = new AS400Text(15, ccsid);

    
    int recordLength = (getSystemVRM() < 0x00050500) ? 64 : 113;	// @IPv6
    int len = 20*recordLength; // Expect about 20 records, each one 64 bytes in length if V5R4 and earlier, else 113 bytes

    ProgramParameter[] parms = new ProgramParameter[6];

    parms[0] = new ProgramParameter(len);                                // receiver variable
    parms[1] = new ProgramParameter(BinaryConverter.intToByteArray(len));// length of receiver variable
    parms[2] = new ProgramParameter(64);                           // list information
    parms[3] = new ProgramParameter(conv.stringToByteArray("ZLSL0700"));
    parms[4] = new ProgramParameter(text15.toBytes(shareName));    // information qualifier
    parms[5] = errorCode_;

    ProgramCall pc = new ProgramCall(system_, "/QSYS.LIB/QZLSOLST.PGM", parms);
    int numRecords = callListProgram(pc, parms, len);

    ISeriesNetServerConnection[] connections = new ISeriesNetServerConnection[numRecords];
    int offset = 0;
    byte[] data = parms[0].getOutputData();
    for (int i = 0; i < numRecords; ++i)
    {
      ISeriesNetServerConnection conn = parseZLSL0600or0700(data, offset, conv, false, null);
      connections[i] = conn;
      if(getSystemVRM() < 0x00050500)	// @IPv6
    	  offset += 64;  // increment by length of a ZLSL0700 entry
      else								// @IPv6 increment by length of ZLSL0700 entry
    	  offset += 113;				// @IPv6 V5R5 and later has an additional Binary(4) for expanded workstation name type and a Char(45) for expanded workstation name
    }
    return connections;
  }


  /**
   Removes the specified share.
   @param shareName The name of the share.
   @exception  AS400SecurityException  If a security or authority error occurs.
   @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
   @exception  InterruptedException  If this thread is interrupted.
   @exception  IOException  If an error occurs while communicating with the system.
   @exception  ObjectDoesNotExistException  If the system object does not exist.
   **/
  public void removeShare(String shareName)
    throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
  {
    if (shareName == null) { throw new NullPointerException(); }

    final int ccsid = system_.getCcsid();
    final AS400Text text12 = new AS400Text(12, ccsid);
    ProgramParameter[] parms = new ProgramParameter[2];

    parms[0] = new ProgramParameter(text12.toBytes(shareName.trim()));
    parms[1] = errorCode_;

    ProgramCall pc = new ProgramCall(system_, "/QSYS.LIB/QZLSRMS.PGM", parms);

    if (!pc.run()) {
      throw new AS400Exception(pc.getMessageList());
    }

  }

  /**
   Returns the value of the "allow system name" attribute.
   This attribute indicates whether access is allowed to the system using the system's TCP/IP system name.
   @return  The value of the "allow system name" attribute.
   **/
  public boolean isAllowSystemName()
  {
    if (!refreshedSinceStart_) refreshWithoutException();
    if (DEBUG) System.out.println("ISeriesNetServer.isAllowSystemName(): effectiveValueStr_[ALLOW_SYSTEM_NAME] == |" + effectiveValueStr_[ALLOW_SYSTEM_NAME] + "|");
    return (effectiveValueStr_[ALLOW_SYSTEM_NAME].equals("1") ? true : false);
  }

  /**
   Returns the pending value of the "allow system name" attribute.
   @return  The pending value of the "allow system name" attribute.
   @see #isAllowSystemName()
   **/
  public boolean isAllowSystemNamePending()
  {
    if (!refreshedSinceStart_) refreshWithoutException();
    if (DEBUG) System.out.println("ISeriesNetServer.getAllowSystemNamePending(): pendingValueStr_[ALLOW_SYSTEM_NAME] == |" + pendingValueStr_[ALLOW_SYSTEM_NAME] + "|");
    return (pendingValueStr_[ALLOW_SYSTEM_NAME].equals("1") ? true : false);
  }

  /**
   Sets the value of the "allow system name" attribute.
   This attribute indicates whether access is allowed to the system using the system's TCP/IP system name.
   @param value  The value of the "allow system name" attribute.
   **/
  public void setAllowSystemName(boolean value)
  {
    pendingValueStr_[ALLOW_SYSTEM_NAME] = (value==true ? "1" : "0");
    userChangedAttribute_[ALLOW_SYSTEM_NAME] = true;
    userCommittedChange_[ALLOW_SYSTEM_NAME] = false;
  }


  /**
   Returns the value of the "authentication method" attribute.
   This attribute indicates the method used to authenticate users.
   <i>Note: This attribute is available only if the i5/OS system is at release <b>V5R2</b> or higher.</i>
   @return  The value of the "authentication method" attribute.
   Valid values are {@link #ENCRYPTED_PASSWORDS ENCRYPTED_PASSWORDS}, {@link #NETWORK_AUTHENTICATION NETWORK_AUTHENTICATION}, and {@link #NETWORK_AUTHENTICATION_OR_PASSWORDS NETWORK_AUTHENTICATION_OR_PASSWORDS}.
   **/
  public int getAuthenticationMethod()
  {
    if (!refreshedSinceStart_) refreshWithoutException();
    if (DEBUG) {
      System.out.println("DEBUG getAuthenticationMethod(): effectiveValueStr_[AUTHENTICATION_METHOD] == |" + effectiveValueStr_[AUTHENTICATION_METHOD] + "|");
    }
    switch (effectiveValueStr_[AUTHENTICATION_METHOD].charAt(0)) {
      case '0': return ENCRYPTED_PASSWORDS;
      case '1': return NETWORK_AUTHENTICATION;
      default:  return NETWORK_AUTHENTICATION_OR_PASSWORDS;
    }
  }


  /**
   Returns the pending value of the "authentication method" attribute.
   @return  The pending value of the "authentication method" attribute.
   @see #getAuthenticationMethod()
   **/
  public int getAuthenticationMethodPending()
  {
    if (!refreshedSinceStart_) refreshWithoutException();
    if (DEBUG) {
      System.out.println("DEBUG getAuthenticationMethod(): pendingValueStr_[AUTHENTICATION_METHOD] == |" + pendingValueStr_[AUTHENTICATION_METHOD] + "|");
    }
    switch (pendingValueStr_[AUTHENTICATION_METHOD].charAt(0)) {
      case '0': return ENCRYPTED_PASSWORDS;
      case '1': return NETWORK_AUTHENTICATION;
      default:  return NETWORK_AUTHENTICATION_OR_PASSWORDS;
    }
  }

  /**
   Sets the value of the "authentication method" attribute.
   This attribute indicates the authentication method used to authenticate users.
   <i>Note: This attribute is available only if the i5/OS system is at release <b>V5R2</b> or higher.</i>
   @param value The value of the "authentication method" attribute.
   Valid values are {@link #ENCRYPTED_PASSWORDS ENCRYPTED_PASSWORDS}, {@link #NETWORK_AUTHENTICATION NETWORK_AUTHENTICATION}, and {@link #NETWORK_AUTHENTICATION_OR_PASSWORDS NETWORK_AUTHENTICATION_OR_PASSWORDS}.
   **/
  public void setAuthenticationMethod(int value)
  {
    char[] charArray = new char[1];
    switch (value) {
      case ENCRYPTED_PASSWORDS:                  charArray[0] = '0'; break;
      case NETWORK_AUTHENTICATION:               charArray[0] = '1'; break;
      case NETWORK_AUTHENTICATION_OR_PASSWORDS:  charArray[0] = '2'; break;
      default:
        throw new ExtendedIllegalArgumentException(Integer.toString(value), ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }
    pendingValueStr_[AUTHENTICATION_METHOD] = new String(charArray);
    userChangedAttribute_[AUTHENTICATION_METHOD] = true;
    userCommittedChange_[AUTHENTICATION_METHOD] = false;
  }

  /**
   Returns the value of the "autostart" attribute.
   This attribute indicates whether or not the NetServer is to be started automatically when TCP is started.
   <br>Note: This method requires that the user have *IOSYSCFG authority on the system.  If the user doesn't have that authority, this method throws AS400SecurityException.
   @return  The value of the "autostart" attribute.
   @exception  AS400SecurityException  If a security or authority error occurs.
   **/
  public boolean isAutoStart()
    throws AS400SecurityException
  {
    if (!userHasSpecialAuthority()) {
      Trace.log(Trace.ERROR, "*IOSYSCFG authority is required in order to query the AutoStart attribute.");
      throw new AS400SecurityException(AS400SecurityException.SPECIAL_AUTHORITY_INSUFFICIENT);
    }
    if (!refreshedSinceStart_ || changedAutoStartSinceRefresh_) {
      refreshWithoutException();
    }
    return (effectiveValueStr_[AUTOSTART].equals("*YES") ? true : false);
  }

  /**
   Returns the pending value of the "autostart" attribute.
   @return  The pending value of the "autostart" attribute.
   @exception  AS400SecurityException  If a security or authority error occurs.
   @see #isAutoStart()
   **/
  public boolean isAutoStartPending()
    throws AS400SecurityException
  {
    if (!userHasSpecialAuthority()) {
      Trace.log(Trace.ERROR, "*IOSYSCFG authority is required in order to query the AutoStart attribute.");
      throw new AS400SecurityException(AS400SecurityException.SPECIAL_AUTHORITY_INSUFFICIENT);
    }
    if (!refreshedSinceStart_ || changedAutoStartSinceRefresh_) {
      refreshWithoutException();
    }
    return (pendingValueStr_[AUTOSTART].equals("*YES") ? true : false);
  }

  /**
   Sets the value of the "autostart" attribute.
   This attribute indicates whether or not the NetServer is to be started automatically when TCP is started.
   Note: This is the only NetServer attribute for which changes take effect immediately upon {@link #commitChanges() commitChanges}.  That is, a NetServer restart is not necessary.
   @return  The pending value of the "autostart" attribute.
   **/
  public void setAutoStart(boolean value)
  {
    pendingValueStr_[AUTOSTART] = (value==true ? "*YES" : "*NO");
    userChangedAttribute_[AUTOSTART] = true;
    userCommittedChange_[AUTOSTART] = false;
  }


  /**
   Returns the value of the "browsing interval" attribute.
   This attribute represents the amount of time, in milliseconds, between each system announcement that is used for browsing.  A value of zero indicates that there will be no system announcements.
   @return  The value of the "browsing interval" attribute.
   **/
  public int getBrowsingInterval()
  {
    if (!refreshedSinceStart_) refreshWithoutException();
    return effectiveValueInt_[BROWSING_INTERVAL];
  }


  /**
   Returns the pending value of the "browsing interval" attribute.
   @return  The pending value of the "browsing interval" attribute.
   @see #getBrowsingInterval()
   **/
  public int getBrowsingIntervalPending()
  {
    if (!refreshedSinceStart_) refreshWithoutException();
    return pendingValueInt_[BROWSING_INTERVAL];
  }

  /**
   Sets the value of the "browsing interval" attribute.
   This attribute represents the amount of time, in milliseconds, between each system announcement that is used for browsing.  A value of zero indicates that there will be no system announcements.
   @param value  The value of the "browsing interval" attribute.
   **/
  public void setBrowsingInterval(int value)
  {
    pendingValueInt_[BROWSING_INTERVAL] = value;
    userChangedAttribute_[BROWSING_INTERVAL] = true;
    userCommittedChange_[BROWSING_INTERVAL] = false;
  }


  /**
   Returns the value of the "system CCSID" attribute.
   This attribute represents the coded character set identifier for the NetServer.
   This is the CCSID that is used for all clients connected to the system.
   <br> A value of 0 indicates that the user would like to use the associated ASCII CCSID for the CCSID of the job used to start the system.
   @return  The value of the "system CCSID" attribute.
   **/
  public int getCCSID()
  {
    if (!refreshedSinceStart_) refreshWithoutException();
    return effectiveValueInt_[CCSID];
  }


  /**
   Returns the pending value of the "system CCSID" attribute.
   @return  The pending value of the "system CCSID" attribute.
   @see #getCCSID()
   **/
  public int getCCSIDPending()
  {
    if (!refreshedSinceStart_) refreshWithoutException();
    return pendingValueInt_[CCSID];
  }

  /**
   Sets the value of the "system CCSID" attribute.
   This attribute represents the coded character set identifier for the NetServer.
   This is the CCSID that is used for all clients connected to the system.
   The default value is the associated ASCII CCSID for the CCSID of the job
   used to start the system.
   <br> A value of 0 indicates that the user would like to use the associated ASCII CCSID for the CCSID of the job used to start the system.
   @param value  The value of the "system CCSID" attribute.
   **/
  public void setCCSID(int value)
  {
    pendingValueInt_[CCSID] = value;
    userChangedAttribute_[CCSID] = true;
    userCommittedChange_[CCSID] = false;
  }


  /**
   Returns the value of the "description" attribute.
   This attribute represents the text description of the NetServer.
   @return  The value of the "description" attribute.
   **/
  public String getDescription()
  {
    if (!refreshedSinceStart_) refreshWithoutException();
    return effectiveValueStr_[DESCRIPTION];
  }


  /**
   Returns the pending value of the "description" attribute.
   @return  The pending value of the "description" attribute.
   @see #getDescription()
   **/
  public String getDescriptionPending()
  {
    if (!refreshedSinceStart_) refreshWithoutException();
    return pendingValueStr_[DESCRIPTION];
  }


  /**
   Sets the value of the "description" attribute.
   This attribute represents the text description of the NetServer
   @param value  The value of the "description" attribute.
   Maximum length is 50 characters.
   **/
  public void setDescription(String value)
  {
    if (value == null) { throw new NullPointerException(); }

    pendingValueStr_[DESCRIPTION] = value.trim();
    userChangedAttribute_[DESCRIPTION] = true;
    userCommittedChange_[DESCRIPTION] = false;
  }


  /**
   Returns the value of the "domain name" attribute.
   This attribute represents the domain name of the NetServer.
   @return  The value of the "domain name" attribute.
   **/
  public String getDomainName()
  {
    if (!refreshedSinceStart_) refreshWithoutException();
    return effectiveValueStr_[DOMAIN_NAME];
  }


  /**
   Returns the pending value of the "domain name" attribute.
   @return  The pending value of the "domain name" attribute.
   @see #getDomainName()
   **/
  public String getDomainNamePending()
  {
    if (!refreshedSinceStart_) refreshWithoutException();
    return pendingValueStr_[DOMAIN_NAME];
  }

  /**
   Sets the value of the "domain name" attribute.
   This attribute represents the domain name of the NetServer.
   @param value  The value of the "domain name" attribute.
   **/
  public void setDomainName(String value)
  {
    if (value == null) { throw new NullPointerException(); }

    pendingValueStr_[DOMAIN_NAME] = value.trim();
    userChangedAttribute_[DOMAIN_NAME] = true;
    userCommittedChange_[DOMAIN_NAME] = false;
  }


  // Implementation note: The NetServer team doesn't use the Guest Support fields.
  // Apparently those fields are used internally by the Host.
  // There is currently no need for us to surface them.
  // "Guest support" indicates whether a guest user profile may be used
  // in the event an unknown user attempts to access resources on the system.


  /**
   Returns the value of the "guest user profile" attribute.
   This attribute represents the guest user profile for the NetServer.
   If no guest user profile is currently configured on the system, the value of this attribute is "" (an empty String).
   @return  The value of the "guest user profile" attribute.
   **/
  public String getGuestUserProfile()
  {
    if (!refreshedSinceStart_) refreshWithoutException();
    return effectiveValueStr_[GUEST_USER_PROFILE];
  }

  /**
   Returns the pending value of the "guest user profile" attribute.
   @return  The pending value of the "guest user profile" attribute.
   @see #getGuestUserProfile()
   **/
  public String getGuestUserProfilePending()
  {
    if (!refreshedSinceStart_) refreshWithoutException();
    return pendingValueStr_[GUEST_USER_PROFILE];
  }

  /**
   Sets the value of the "guest user profile" attribute.
   This attribute represents the guest user profile for the NetServer.
   If no guest user profile is currently configured on the system, the value of this attribute is "" (an empty String).
   <p>
   Note: Guest support allows customers to have users accessing files and printers on the system, without the requirement of a user profile on the system.  It limits access to data and allows customers to support a set of users who may only need print support but do not otherwise need system access.
   @param value  The value of the "guest user profile" attribute.
   **/
  public void setGuestUserProfile(String value)
  {
    if (value == null) { throw new NullPointerException(); }

    pendingValueStr_[GUEST_USER_PROFILE] = value.trim();
    userChangedAttribute_[GUEST_USER_PROFILE] = true;
    userCommittedChange_[GUEST_USER_PROFILE] = false;
  }


  /**
   Returns the value of the "idle timeout" attribute.
   This attribute represents the amount of time, in seconds, that a connection to the NetServer will remain active once activity has ceased on that connection.
   An idle time-out value of ({@link #NO_AUTO_DISCONNECT NO_AUTO_DISCONNECT}) indicates no autodisconnect.
   @return  The value of the "idle timeout" attribute.
   **/
  public int getIdleTimeout()
  {
    if (!refreshedSinceStart_) refreshWithoutException();
    return effectiveValueInt_[IDLE_TIMEOUT];
  }

  /**
   Returns the pending value of the "idle timeout" attribute.
   @return  The pending value of the "idle timeout" attribute.
   @see #getIdleTimeout()
   **/
  public int getIdleTimeoutPending()
  {
    if (!refreshedSinceStart_) refreshWithoutException();
    return pendingValueInt_[IDLE_TIMEOUT];
  }

  /**
   Sets the value of the "idle timeout" attribute.
   This attribute represents the amount of time, in seconds, that a connection to the NetServer will remain active once activity has ceased on that connection.
   An idle time-out value of ({@link #NO_AUTO_DISCONNECT NO_AUTO_DISCONNECT}) indicates no autodisconnect.
   @param value  The value of the "idle timeout" attribute.
   **/
  public void setIdleTimeout(int value)
  {
    pendingValueInt_[IDLE_TIMEOUT] = value;
    userChangedAttribute_[IDLE_TIMEOUT] = true;
    userCommittedChange_[IDLE_TIMEOUT] = false;
  }

  /**
   Returns the value of the "LAN Manager authentication" attribute.
   This attribute represents the level of restriction on the use of the LANMAN password hash for authentication.
   Possible values are {@link #PASSWORD_STRONGER PASSWORD_STRONGER} and {@link #PASSWORD_STRONGER_OR_MISMATCH PASSWORD_STRONGER_OR_MISMATCH}.
   <br><em>Note: This attribute is not supported prior to the i5/OS release following V5R3.</em>
   @return  The value of the "LAN Manager authentication" attribute.
   **/
  public int getLANManagerAuthentication()
  {
    if (!refreshedSinceStart_) refreshWithoutException();
    return effectiveValueInt_[LAN_MGR_AUTHENTICATION];
  }

  /**
   Returns the pending value of the "LAN Manager authentication" attribute.
   <br><em>Note: This attribute is not supported prior to the i5/OS release following V5R3.</em>
   @return  The pending value of the "LAN Manager authentication" attribute.
   @see #getLANManagerAuthentication()
   **/
  public int getLANManagerAuthenticationPending()
  {
    if (!refreshedSinceStart_) refreshWithoutException();
    return pendingValueInt_[LAN_MGR_AUTHENTICATION];
  }

  /**
   Sets the value of the "LAN Manager authentication" attribute.
   This attribute represents the level of restriction on the use of the LANMAN password hash for authentication.
   Possible values are {@link #PASSWORD_STRONGER PASSWORD_STRONGER} and {@link #PASSWORD_STRONGER_OR_MISMATCH PASSWORD_STRONGER_OR_MISMATCH}.
   <br><em>Note: This attribute is not supported prior to the i5/OS release following V5R3.</em>
   @param value  The value of the "LAN Manager authentication" attribute.
   **/
  public void setLANManagerAuthentication(int value)
  {
    pendingValueInt_[LAN_MGR_AUTHENTICATION] = value;
    userChangedAttribute_[LAN_MGR_AUTHENTICATION] = true;
    userCommittedChange_[LAN_MGR_AUTHENTICATION] = false;
  }


  /**
   Returns the value of the "logon support" attribute.
   This attribute indicates the logon system role for the system.
   If true, then the server is a logon server; if false, the server is not a logon server.
   <br>Note: This attribute corresponds to the "server role" field specified
   in the NetServer API's.
   @return  The value of the "logon support" attribute.
   **/
  public boolean isLogonServer()
  {
    if (!refreshedSinceStart_) refreshWithoutException();
    return (effectiveValueInt_[LOGON_SUPPORT] == 1 ? true : false);
  }

  /**
   Returns the pending value of the "logon support" attribute.
   @return  The pending value of the "logon support" attribute.
   @see #isLogonServer()
   **/
  public boolean isLogonServerPending()
  {
    if (!refreshedSinceStart_) refreshWithoutException();
    return (pendingValueInt_[LOGON_SUPPORT] == 1 ? true : false);
  }

  /**
   Sets the value of the "logon support" attribute.
   This attribute indicates the logon server role for the server.
   If true, then the server is a logon server; if false, the server is not a logon server.
   <br>Note: This attribute corresponds to the "server role" field specified
   in the NetServer API's.
   @param value  The value of the "logon support" attribute.
   **/
  public void setLogonServer(boolean value)
  {
    pendingValueInt_[LOGON_SUPPORT] = (value==true ? 1 : 0);
    userChangedAttribute_[LOGON_SUPPORT] = true;
    userCommittedChange_[LOGON_SUPPORT] = false;
  }


  /**
   Returns the value of the "message authentication" attribute.
   This attribute represents the status of message authentication.
   Possible values are {@link #MSG_AUTH_NOT_SUPPORTED MSG_AUTH_NOT_SUPPORTED}, {@link #MSG_AUTH_NEGOTIATED MSG_AUTH_NEGOTIATED}, and {@link #MSG_AUTH_REQUIRED MSG_AUTH_REQUIRED}.
   <br><em>Note: This attribute is not supported prior to the i5/OS release following V5R3.</em>
   @return  The value of the "message authentication" attribute.
   **/
  public int getMessageAuthentication()
  {
    if (!refreshedSinceStart_) refreshWithoutException();
    return effectiveValueInt_[MESSAGE_AUTHENTICATION];
  }

  /**
   Returns the pending value of the "message authentication" attribute.
   <br><em>Note: This attribute is not supported prior to the i5/OS release following V5R3.</em>
   @return  The pending value of the "message authentication" attribute.
   @see #getMessageAuthentication()
   **/
  public int getMessageAuthenticationPending()
  {
    if (!refreshedSinceStart_) refreshWithoutException();
    return pendingValueInt_[MESSAGE_AUTHENTICATION];
  }

  /**
   Sets the value of the "message authentication" attribute.
   This attribute represents the status of message authentication.
   Possible values are {@link #MSG_AUTH_NOT_SUPPORTED MSG_AUTH_NOT_SUPPORTED}, {@link #MSG_AUTH_NEGOTIATED MSG_AUTH_NEGOTIATED}, and {@link #MSG_AUTH_REQUIRED MSG_AUTH_REQUIRED}.
   <br><em>Note: This attribute is not supported prior to the i5/OS release following V5R3.</em>
   @param value  The value of the "message authentication" attribute.
   **/
  public void setMessageAuthentication(int value)
  {
    pendingValueInt_[MESSAGE_AUTHENTICATION] = value;
    userChangedAttribute_[MESSAGE_AUTHENTICATION] = true;
    userCommittedChange_[MESSAGE_AUTHENTICATION] = false;
  }

  /**
   Returns the value of the "minimum message severity" attribute.
   This attribute represents the minimum message severity of administrative alerts to send to users of the system.
   A value of ({@link #NO_ADMIN_ALERTS NO_ADMIN_ALERTS}) indicates that administrative alert messages are not sent.
   <br><em>Note: This attribute is not supported prior to the i5/OS release following V5R3.</em>
   @return  The value of the "minimum message severity" attribute.
   **/
  public int getMinimumMessageSeverity()
  {
    if (!refreshedSinceStart_) refreshWithoutException();
    return effectiveValueInt_[MIN_MESSAGE_SEVERITY];
  }

  /**
   Returns the pending value of the "minimum message severity" attribute.
   <br><em>Note: This attribute is not supported prior to the i5/OS release following V5R3.</em>
   @return  The pending value of the "minimum message severity" attribute.
   @see #getMinimumMessageSeverity()
   **/
  public int getMinimumMessageSeverityPending()
  {
    if (!refreshedSinceStart_) refreshWithoutException();
    return pendingValueInt_[MIN_MESSAGE_SEVERITY];
  }

  /**
   Sets the value of the "minimum message severity" attribute.
   This attribute represents the minimum message severity of administrative alerts to send to users of the system.
   A value of ({@link #NO_ADMIN_ALERTS NO_ADMIN_ALERTS}) indicates that administrative alert messages are not sent.
   <br><em>Note: This attribute is not supported prior to the i5/OS release following V5R3.</em>
   @param value  The value of the "minimum message severity" attribute.
   **/
  public void setMinimumMessageSeverity(int value)
  {
    pendingValueInt_[MIN_MESSAGE_SEVERITY] = value;
    userChangedAttribute_[MIN_MESSAGE_SEVERITY] = true;
    userCommittedChange_[MIN_MESSAGE_SEVERITY] = false;
  }


  /**
   Returns the value of the "NetServer name" attribute.
   This attribute represents the name of the NetServer.
   <br>Note: The NetServer name is always uppercase on the system.
   @return  The value of the "NetServer name" attribute.
   **/
  public String getName()
  {
    if (!refreshedSinceStart_) refreshWithoutException();
    return effectiveValueStr_[SERVER_NAME];
  }

  /**
   Returns the pending value of the "NetServer name" attribute.
   @return  The pending value of the "NetServer name" attribute.
   @see #getName()
   **/
  public String getNamePending()
  {
    if (!refreshedSinceStart_) refreshWithoutException();
    return pendingValueStr_[SERVER_NAME];
  }

  /**
   Sets the value of the "NetServer name" attribute.
   This attribute represents the name of the NetServer.
   <br>Note: The NetServer name is always uppercase on the system.
   @param value  The value of the "NetServer name" attribute.
   **/
  public void setName(String value)
  {
    if (value == null) { throw new NullPointerException(); }

    pendingValueStr_[SERVER_NAME] = value.trim();
    userChangedAttribute_[SERVER_NAME] = true;
    userCommittedChange_[SERVER_NAME] = false;
  }


  /**
   Returns the value of the "opportunistic lock timeout" attribute.
   This attribute represents the amount of time, in seconds, that the system will wait for a response to a break lock request sent to a lock holder, before forcefully removing the lock.
   A value of ({@link #OPP_LOCK_DISABLED OPP_LOCK_DISABLED}) indicates that opportunistic locking is disabled.
   The default value is 30 seconds.
   <br><em>Note: This attribute is not supported prior to the i5/OS release following V5R3.</em>
   @return  The value of the "opportunistic lock timeout" attribute.
   **/
  public int getOpportunisticLockTimeout()
  {
    if (!refreshedSinceStart_) refreshWithoutException();
    return effectiveValueInt_[OPP_LOCK_TIMEOUT];
  }

  /**
   Returns the pending value of the "opportunistic lock timeout" attribute.
   <br><em>Note: This attribute is not supported prior to the i5/OS release following V5R3.</em>
   @return  The pending value of the "opportunistic lock timeout" attribute.
   @see #getOpportunisticLockTimeout()
   **/
  public int getOpportunisticLockTimeoutPending()
  {
    if (!refreshedSinceStart_) refreshWithoutException();
    return pendingValueInt_[OPP_LOCK_TIMEOUT];
  }

  /**
   Sets the value of the "opportunistic lock timeout" attribute.
   This attribute represents the amount of time, in seconds, that the system will wait for a response to a break lock request sent to a lock holder, before forcefully removing the lock.
   A value of ({@link #OPP_LOCK_DISABLED OPP_LOCK_DISABLED}) indicates that opportunistic locking is disabled.
   The default value is 30 seconds.
   <br><em>Note: This attribute is not supported prior to the i5/OS release following V5R3.</em>
   @param value  The value of the "opportunistic lock timeout" attribute.
   **/
  public void setOpportunisticLockTimeout(int value)
  {
    pendingValueInt_[OPP_LOCK_TIMEOUT] = value;
    userChangedAttribute_[OPP_LOCK_TIMEOUT] = true;
    userCommittedChange_[OPP_LOCK_TIMEOUT] = false;
  }


  /**
   Returns the value of the "WINS enablement" attribute.
   This attribute indicates whether the system uses a WINS server.
   Note: This attribute is also referred to as the "server role".
   @return  The value of the "WINS enablement" attribute.
   **/
  public boolean isWINSServer()
  {
    if (!refreshedSinceStart_) refreshWithoutException();
    return (effectiveValueInt_[WINS_ENABLEMENT] == 1 ? true : false);
  }


  /**
   Returns the pending value of the "WINS enablement" attribute.
   @return  The pending value of the "WINS enablement" attribute.
   @see #isWINSServer()
   **/
  public boolean isWINSServerPending()
  {
    if (!refreshedSinceStart_) refreshWithoutException();
    return (pendingValueInt_[WINS_ENABLEMENT] == 1 ? true : false);
  }

  /**
   Sets the value of the "WINS enablement" attribute.
   This attribute indicates whether the system uses a WINS server.
   Note: This attribute is also referred to as the "server role".
   @param value  The value of the "WINS enablement" attribute.
   **/
  public void setWINSServer(boolean value)
  {
    pendingValueInt_[WINS_ENABLEMENT] = (value==true ? 1 : 0);
    userChangedAttribute_[WINS_ENABLEMENT] = true;
    userCommittedChange_[WINS_ENABLEMENT] = false;
  }


  /**
   Returns the value of the "WINS primary address" attribute.
   This attribute represents the IP address of the primary WINS server.
   @return  The value of the "WINS primary address" attribute.
   **/
  public String getWINSPrimaryAddress()
  {
    if (!refreshedSinceStart_) refreshWithoutException();
    return effectiveValueStr_[WINS_PRIMARY_ADDRESS];
  }

  /**
   Returns the pending value of the "WINS primary address" attribute.
   @return  The pending value of the "WINS primary address" attribute.
   @see #getWINSPrimaryAddress()
   **/
  public String getWINSPrimaryAddressPending()
  {
    if (!refreshedSinceStart_) refreshWithoutException();
    return pendingValueStr_[WINS_PRIMARY_ADDRESS];
  }

  /**
   Sets the value of the "WINS primary address" attribute.
   This attribute represents the IP address of the primary WINS server.
   @param value  The value of the "WINS primary address" attribute.
   **/
  public void setWINSPrimaryAddress(String value)
  {
    if (value == null) { throw new NullPointerException(); }

    pendingValueStr_[WINS_PRIMARY_ADDRESS] = value.trim();
    userChangedAttribute_[WINS_PRIMARY_ADDRESS] = true;
    userCommittedChange_[WINS_PRIMARY_ADDRESS] = false;
  }


  /**
   Returns the value of the "WINS scope ID" attribute.
   This attribute represents the network scope used by the WINS server.
   If no scope ID is currently configured on the system, the value of this attribute is "" (an empty String).
   @return  The value of the "WINS scope ID" attribute.
   **/
  public String getWINSScopeID()
  {
    if (!refreshedSinceStart_) refreshWithoutException();
    return effectiveValueStr_[WINS_SCOPE_ID];
  }

  /**
   Returns the pending value of the "WINS scope ID" attribute.
   @return  The pending value of the "WINS scope ID" attribute.
   @see #getWINSScopeID()
   **/
  public String getWINSScopeIDPending()
  {
    if (!refreshedSinceStart_) refreshWithoutException();
    return pendingValueStr_[WINS_SCOPE_ID];
  }

  /**
   Sets the value of the "WINS scope ID" attribute.
   This attribute represents the network scope used by the WINS server.
   If no scope ID is currently configured on the system, the value of this attribute is "" (an empty String).
   @param value  The value of the "WINS scope ID" attribute.
   **/
  public void setWINSScopeID(String value)
  {
    if (value == null) { throw new NullPointerException(); }

    pendingValueStr_[WINS_SCOPE_ID] = value.trim();
    userChangedAttribute_[WINS_SCOPE_ID] = true;
    userCommittedChange_[WINS_SCOPE_ID] = false;
  }


  /**
   Returns the value of the "WINS secondary address" attribute.
   This attribute represents the IP address of the secondary WINS server.
   @return  The value of the "WINS secondary address" attribute.
   **/
  public String getWINSSecondaryAddress()
  {
    if (!refreshedSinceStart_) refreshWithoutException();
    return effectiveValueStr_[WINS_SECONDARY_ADDRESS];
  }

  /**
   Returns the pending value of the "WINS secondary address" attribute.
   @return  The pending value of the "WINS secondary address" attribute.
   @see #getWINSSecondaryAddress()
   **/
  public String getWINSSecondaryAddressPending()
  {
    if (!refreshedSinceStart_) refreshWithoutException();
    return pendingValueStr_[WINS_SECONDARY_ADDRESS];
  }

  /**
   Sets the value of the "WINS secondary address" attribute.
   This attribute represents the IP address of the secondary WINS server.
   @param value  The value of the "WINS secondary address" attribute.
   **/
  public void setWINSSecondaryAddress(String value)
  {
    if (value == null) { throw new NullPointerException(); }

    pendingValueStr_[WINS_SECONDARY_ADDRESS] = value.trim();
    userChangedAttribute_[WINS_SECONDARY_ADDRESS] = true;
    userCommittedChange_[WINS_SECONDARY_ADDRESS] = false;
  }

  /**
   Refreshes the attribute values of this ISeriesNetServer object, from the current in-effect values on the system.
   @exception  AS400SecurityException  If a security or authority error occurs.
   @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
   @exception  InterruptedException  If this thread is interrupted.
   @exception  IOException  If an error occurs while communicating with the system.
   @exception  ObjectDoesNotExistException  If the system object does not exist.
   **/
  public void refresh()
    throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
  {
    if (DEBUG) System.out.println("DEBUG: ISeriesNetServer.refresh()");

    // Refresh the "autostart" attribute value.
    retrieveAutostart();
    changedAutoStartSinceRefresh_ = false;

    // Refresh most of the attribute values (all except "autostart").
    openListOfServerInfo();

    // Remember that we've done at least one refresh since the last start.
    refreshedSinceStart_ = true;
  }

    // Helper method.  This calls refresh() and swallows all exceptions,
    // so that all of the getters can use it.
    private void refreshWithoutException()
    {
      try {
        refresh();
      }
      catch (Exception e)
      {
        if (Trace.traceOn_) {
          Trace.log(Trace.ERROR, "Exception swallowed by refresh(): ", e);
        }
      }
    }


    /**
     Commits all attribute value changes to the system.
     Note that for most attributes, changes do not take effect
     until the NetServer process on the system is stopped ({@link #end() end}) and restarted ({@link #start() start}).
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the system object does not exist.
     **/
  public void commitChanges()
    throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
  {
    if (DEBUG) System.out.println("DEBUG: ISeriesNetServer.commitChanges()");
    if (!userHasSpecialAuthority()) {
      Trace.log(Trace.ERROR, "*IOSYSCFG authority is required in order to change NetServer attributes.");
      throw new AS400SecurityException(AS400SecurityException.SPECIAL_AUTHORITY_INSUFFICIENT);
    }

    // Make sure we have the currently in-effect values.
    if (!refreshedSinceStart_) refresh();

    // The API's we call will depend on which attributes have been changed.

    // These attributes are committed via QZLSCHSI ("Change Server Info"):
    //   authenticationMethod, browsingInterval, ccsid, idleTimeout,
    //   logonSupport, oppLockTimeout, winsEnablement, winsPrimaryAddress, winsScopeId,
    //   winsSecondaryAddress.
    //
    // These attributes are committed via QZLSCHSN ("Change Server Name"):
    //   allowSystemName, description, domainName, serverName.
    //
    // This attribute is committed via QZLSCHSG ("Change Server Guest"):
    //   guestUserProfile
    //
    // This attribute is committed via QTOCAUTO ("Change Autostart"):
    //   autostart

    // Determine which API's we need to call to commit the changed attributes.

    boolean needToChangeServerInfo  = false;
    boolean needToChangeServerName  = false;
    boolean needToChangeServerGuest = false;
    boolean needToChangeAutostart   = false;

    if (userChangedAttribute_[ALLOW_SYSTEM_NAME] &&
        !userCommittedChange_[ALLOW_SYSTEM_NAME]) {
      needToChangeServerName = true;
    }
    if (userChangedAttribute_[AUTHENTICATION_METHOD] &&
        !userCommittedChange_[AUTHENTICATION_METHOD]) {
      needToChangeServerInfo = true;
    }
    if (userChangedAttribute_[AUTOSTART] &&
        !userCommittedChange_[AUTOSTART]) {
      needToChangeAutostart = true;
    }
    if (userChangedAttribute_[BROWSING_INTERVAL] &&
        !userCommittedChange_[BROWSING_INTERVAL]) {
      needToChangeServerInfo = true;
    }
    if (userChangedAttribute_[CCSID] &&
        !userCommittedChange_[CCSID]) {
      needToChangeServerInfo = true;
    }
    if (userChangedAttribute_[DESCRIPTION] &&
        !userCommittedChange_[DESCRIPTION]) {
      needToChangeServerName = true;
    }
    if (userChangedAttribute_[DOMAIN_NAME] &&
        !userCommittedChange_[DOMAIN_NAME]) {
      needToChangeServerName = true;
    }
    if (userChangedAttribute_[GUEST_USER_PROFILE] &&
        !userCommittedChange_[GUEST_USER_PROFILE]) {
      needToChangeServerGuest = true;
    }
    if (userChangedAttribute_[IDLE_TIMEOUT] &&
        !userCommittedChange_[IDLE_TIMEOUT]) {
      needToChangeServerInfo = true;
    }
    if (userChangedAttribute_[OPP_LOCK_TIMEOUT] &&
        !userCommittedChange_[OPP_LOCK_TIMEOUT]) {
      needToChangeServerInfo = true;
    }
    if (userChangedAttribute_[LOGON_SUPPORT] &&
        !userCommittedChange_[LOGON_SUPPORT]) {
      needToChangeServerInfo = true;
    }
    if (userChangedAttribute_[SERVER_NAME] &&
        !userCommittedChange_[SERVER_NAME]) {
      needToChangeServerName = true;
    }
    if (userChangedAttribute_[WINS_ENABLEMENT] &&
        !userCommittedChange_[WINS_ENABLEMENT]) {
      needToChangeServerInfo = true;
    }
    if (userChangedAttribute_[WINS_PRIMARY_ADDRESS] &&
        !userCommittedChange_[WINS_PRIMARY_ADDRESS]) {
      needToChangeServerInfo = true;
    }
    if (userChangedAttribute_[WINS_SCOPE_ID] &&
        !userCommittedChange_[WINS_SCOPE_ID]) {
      needToChangeServerInfo = true;
    }
    if (userChangedAttribute_[WINS_SECONDARY_ADDRESS] &&
        !userCommittedChange_[WINS_SECONDARY_ADDRESS]) {
      needToChangeServerInfo = true;
    }
    if (getSystemVRM() >= 0x00050400)
    { // new fields added to API in V5R4
      if (userChangedAttribute_[MESSAGE_AUTHENTICATION] &&
          !userCommittedChange_[MESSAGE_AUTHENTICATION]) {
        needToChangeServerInfo = true;
      }
      if (userChangedAttribute_[MIN_MESSAGE_SEVERITY] &&
          !userCommittedChange_[MIN_MESSAGE_SEVERITY]) {
        needToChangeServerInfo = true;
      }
      if (userChangedAttribute_[LAN_MGR_AUTHENTICATION] &&
          !userCommittedChange_[LAN_MGR_AUTHENTICATION]) {
        needToChangeServerInfo = true;
      }
    }

    if (needToChangeServerInfo)  { changeServerInfo(); }

    if (needToChangeServerName)  { changeServerName(); }

    if (needToChangeServerGuest) { changeServerGuest(); }

    if (needToChangeAutostart)   { changeAutostart(); }

    // If we made it this far, mark all changes as "committed".
    for (int i=0; i<userCommittedChange_.length; i++) {
      if (userChangedAttribute_[i]) userCommittedChange_[i] = true;
    }
  }


  // Invokes the QTOCAUTO API, to change the autostart attribute value.
  private void changeAutostart()
    throws IOException, AS400SecurityException, ErrorCompletingRequestException, InterruptedException, ObjectDoesNotExistException
  {
    // Syntax of the QTOCAUTO API:
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


    // Compose the arguments for the API.

    final int ccsid = system_.getCcsid();
    final AS400Text text4  = new AS400Text(4,  ccsid);
    final AS400Text text30 = new AS400Text(30, ccsid);

    ProgramParameter[] parms = new ProgramParameter[4];

    parms[0] = new ProgramParameter(text4.toBytes("*CHG"));
    parms[1] = new ProgramParameter(text30.toBytes("*NETSVR"));
    parms[2] = new ProgramParameter(text4.toBytes(pendingValueStr_[AUTOSTART]));
    parms[3] = errorCode_;

    ProgramCall pc = new ProgramCall(system_, "/QSYS.LIB/QTOCAUTO.PGM", parms);

    if (!pc.run()) {
      throw new AS400Exception(pc.getMessageList());
    }
    changedAutoStartSinceRefresh_ = true;
  }


  // Invokes the QTOCAUTO API, to retrieve the autostart attribute value.
  // Note: The QTOCAUTO API requires that the user have *IOSYSCFG authority on the system.
  // If the user doesn't have that authority, this method simply reports 'false' for the AutoStart attribute.
  private void retrieveAutostart()
    throws IOException, AS400SecurityException, ErrorCompletingRequestException, InterruptedException, ObjectDoesNotExistException
  {
    if (DEBUG) System.out.println("DEBUG: ISeriesNetServer.retrieveAutostart()");
    String retrievedValue;
    if (!userHasSpecialAuthority()) {
      retrievedValue = "*NO";
    }
    else
    {
      // Compose the arguments for the API.

      final int ccsid = system_.getCcsid();
      final CharConverter conv = new CharConverter(ccsid);
      final AS400Text text4  = new AS400Text(4,  ccsid);
      final AS400Text text30 = new AS400Text(30, ccsid);

      ProgramParameter[] parms = new ProgramParameter[4];

      parms[0] = new ProgramParameter(text4.toBytes("*RTV"));
      parms[1] = new ProgramParameter(text30.toBytes("*NETSVR"));
      parms[2] = new ProgramParameter(4);  // output parameter: *YES, *NO, or *ERR
      parms[3] = errorCode_;

      ProgramCall pc = new ProgramCall(system_, "/QSYS.LIB/QTOCAUTO.PGM", parms);

      if (!pc.run()) {
        throw new AS400Exception(pc.getMessageList());
      }

      byte[] retrievedBytes = parms[2].getOutputData();
      retrievedValue = conv.byteArrayToString(retrievedBytes).trim();
      if (retrievedValue.equals("*ERR")) {
        Trace.log(Trace.ERROR, "The QTOCOAUTO API returned *ERR.");
        throw new InternalErrorException(InternalErrorException.UNKNOWN);
      }
    }

    effectiveValueStr_[AUTOSTART] = retrievedValue;
    if (!userChangedAttribute_[AUTOSTART]) {
      pendingValueStr_[AUTOSTART] = retrievedValue;
    }
  }


  // Invokes the "Change Server Guest" (QZLSCHSG) API.
  private void changeServerGuest()
    throws IOException, AS400SecurityException, ErrorCompletingRequestException, InterruptedException, ObjectDoesNotExistException
  {
    if (DEBUG) System.out.println("DEBUG: ISeriesNetServer.changeServerGuest()");

    // Compose the arguments for the API.

    final AS400Text text10 = new AS400Text(10, system_.getCcsid());

    ProgramParameter[] parms = new ProgramParameter[2];

    parms[0] = new ProgramParameter(text10.toBytes(pendingValueStr_[GUEST_USER_PROFILE]));
    parms[1] = errorCode_;

    ProgramCall pc = new ProgramCall(system_, "/QSYS.LIB/QZLSCHSG.PGM", parms);

    if (!pc.run()) {
      throw new AS400Exception(pc.getMessageList());
    }
  }


  // Invokes the "Change Server Information" (QZLSCHSI) API.
  private void changeServerInfo()
    throws IOException, AS400SecurityException, ErrorCompletingRequestException, InterruptedException, ObjectDoesNotExistException
  {
    if (DEBUG) System.out.println("DEBUG: ISeriesNetServer.changeServerInfo()");

    // Compose the arguments for the API.

    final int ccsid = system_.getCcsid();
    final CharConverter conv = new CharConverter(ccsid);
    final AS400Text text5 = new AS400Text(5, ccsid);
    final AS400Text text15 = new AS400Text(15, ccsid);
    final AS400Text text224 = new AS400Text(224, ccsid);

    boolean userChangedAuthMethod = userChangedAttribute_[AUTHENTICATION_METHOD];  // short-hand

    ProgramParameter[] parms = new ProgramParameter[4];

    // Compose a ZLSS0100 structure (passed-in as argument to the QZLSCHSI API).

    ByteArrayOutputStream stream = new ByteArrayOutputStream(300);

    stream.write(BinaryConverter.intToByteArray(pendingValueInt_[CCSID]));
    stream.write(BinaryConverter.intToByteArray(pendingValueInt_[IDLE_TIMEOUT]));
    stream.write(BinaryConverter.intToByteArray(pendingValueInt_[OPP_LOCK_TIMEOUT]));
    stream.write(BinaryConverter.intToByteArray(pendingValueInt_[BROWSING_INTERVAL]));
    stream.write(text15.toBytes(pendingValueStr_[WINS_PRIMARY_ADDRESS]));
    stream.write(text15.toBytes(pendingValueStr_[WINS_SECONDARY_ADDRESS]));
    stream.write(text224.toBytes(pendingValueStr_[WINS_SCOPE_ID]));
    if (pendingValueInt_[WINS_ENABLEMENT] == 1) { // a.k.a. "WINS proxy"
      stream.write(0xF1);
    }
    else {
      stream.write(0xF0);
    }

    byte[] reserved5 = {(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00};
    stream.write(reserved5);  // reserved field - CHAR(5)

    stream.write(BinaryConverter.intToByteArray(pendingValueInt_[LOGON_SUPPORT]));

    if (getSystemVRM() >= 0x00050200) { // new field added to API in V5R2
      if (getSystemVRM() >= 0x00050400 ||
          userChangedAttribute_[AUTHENTICATION_METHOD])
      {
        switch (pendingValueStr_[AUTHENTICATION_METHOD].charAt(0)) {
          case '0': stream.write(0xF0); break;
          case '1': stream.write(0xF1); break;
          default:  stream.write(0xF2);
        }
      }
    }

    if (getSystemVRM() >= 0x00050400)
    { // new fields added to API in V5R4
      byte[] reserved3 = {(byte)0x00, (byte)0x00, (byte)0x00};
      stream.write(reserved3);  // reserved field - CHAR(3)
      stream.write(BinaryConverter.intToByteArray(pendingValueInt_[MESSAGE_AUTHENTICATION]));
      stream.write(BinaryConverter.intToByteArray(pendingValueInt_[MIN_MESSAGE_SEVERITY]));
      stream.write(BinaryConverter.intToByteArray(pendingValueInt_[LAN_MGR_AUTHENTICATION]));
    }


    stream.flush();
    byte[] requestVariable = stream.toByteArray();

    parms[0] = new ProgramParameter(requestVariable);
    parms[1] = new ProgramParameter(BinaryConverter.intToByteArray(requestVariable.length));
    parms[2] = new ProgramParameter(conv.stringToByteArray("ZLSS0100"));
    parms[3] = errorCode_;

    ProgramCall pc = new ProgramCall(system_, "/QSYS.LIB/QZLSCHSI.PGM", parms);

    if (!pc.run()) {
      throw new AS400Exception(pc.getMessageList());
    }
  }


  // Invokes the "Change Server Name" (QZLSCHSN) API.
  private void changeServerName()
    throws IOException, AS400SecurityException, ErrorCompletingRequestException, InterruptedException, ObjectDoesNotExistException
  {
    if (DEBUG) System.out.println("DEBUG: ISeriesNetServer.changeServerName()");

    // Compose the arguments for the API.

    final int ccsid = system_.getCcsid();
    final AS400Text text15 = new AS400Text(15, ccsid);
    final AS400Text text50 = new AS400Text(50, ccsid);

    ProgramParameter[] parms = new ProgramParameter[5];

    parms[0] = new ProgramParameter(text15.toBytes(pendingValueStr_[SERVER_NAME].trim()));
    parms[1] = new ProgramParameter(text15.toBytes(pendingValueStr_[DOMAIN_NAME].trim()));
    parms[2] = new ProgramParameter(text50.toBytes(pendingValueStr_[DESCRIPTION].trim()));
    parms[3] = errorCode_;

    byte[] allowSysName = new byte[1];
    if (pendingValueStr_[ALLOW_SYSTEM_NAME].equals("1")) {
      allowSysName[0] = (byte)0xF1;
    }
    else {
      allowSysName[0] = (byte)0xF0;
    }
    parms[4] = new ProgramParameter(allowSysName);

    ProgramCall pc = new ProgramCall(system_, "/QSYS.LIB/QZLSCHSN.PGM", parms);

    if (!pc.run()) {
      throw new AS400Exception(pc.getMessageList());
    }
  }



  // Invokes the "Open List of Server Information" (QZLSOLST) API, and parses the returned data.
  private void openListOfServerInfo()
    throws IOException, AS400SecurityException, ErrorCompletingRequestException, InterruptedException, ObjectDoesNotExistException
  {
    if (DEBUG) System.out.println("DEBUG: ISeriesNetServer.openListOfServerInfo()");

    // Compose the arguments for the API.

    final int ccsid = system_.getCcsid();
    final CharConverter conv = new CharConverter(ccsid);
    final AS400Text text15 = new AS400Text(15, ccsid);

    int len = 772;  // length of receiver variable
    ProgramParameter[] parms = new ProgramParameter[6];

    parms[0] = new ProgramParameter(len);             // receiver variable
    parms[1] = new ProgramParameter(BinaryConverter.intToByteArray(len));// length of receiver variable
    parms[2] = new ProgramParameter(64);              // list information
    parms[3] = new ProgramParameter(conv.stringToByteArray("ZLSL0201"));
    parms[4] = new ProgramParameter(text15.toBytes("*ALL")); // ignored for format ZLSL0201
    parms[5] = errorCode_;

    ProgramCall pc = new ProgramCall(system_, "/QSYS.LIB/QZLSOLST.PGM", parms);

    if (!pc.run()) {
      throw new AS400Exception(pc.getMessageList());
    }

    byte[] listInfo = parms[2].getOutputData();

    if (DEBUG) {
      int totalRecords = BinaryConverter.byteArrayToInt(listInfo, 0);
      int numRecords = BinaryConverter.byteArrayToInt(listInfo, 4);
      int recLen = BinaryConverter.byteArrayToInt(listInfo, 8);
      int lengthOfInfoReturned = BinaryConverter.byteArrayToInt(listInfo, 12);
      System.out.println("totalRecords=="+totalRecords+" ; numRecords=="+numRecords+" ; recLen=="+recLen+" ; lengthOfInfoReturned=="+lengthOfInfoReturned);
    }

    // Parse the returned data.

    byte[] data = parms[0].getOutputData();

    effectiveValueInt_[CCSID] = BinaryConverter.byteArrayToInt(data, 0);
    if (!userChangedAttribute_[CCSID]) {
      pendingValueInt_[CCSID] = BinaryConverter.byteArrayToInt(data, 4);
    }
    effectiveValueInt_[IDLE_TIMEOUT] = BinaryConverter.byteArrayToInt(data, 8);
    if (!userChangedAttribute_[IDLE_TIMEOUT]) {
      pendingValueInt_[IDLE_TIMEOUT] = BinaryConverter.byteArrayToInt(data, 12);
    }
    effectiveValueInt_[OPP_LOCK_TIMEOUT] = BinaryConverter.byteArrayToInt(data, 16);
    if (!userChangedAttribute_[OPP_LOCK_TIMEOUT]) {
      pendingValueInt_[OPP_LOCK_TIMEOUT] = BinaryConverter.byteArrayToInt(data, 20);
    }
    effectiveValueInt_[BROWSING_INTERVAL] = BinaryConverter.byteArrayToInt(data, 24);
    if (!userChangedAttribute_[BROWSING_INTERVAL]) {
      pendingValueInt_[BROWSING_INTERVAL] = BinaryConverter.byteArrayToInt(data, 28);
    }
    effectiveValueInt_[WINS_ENABLEMENT] = BinaryConverter.byteArrayToInt(data, 32);
    if (!userChangedAttribute_[WINS_ENABLEMENT]) {
      pendingValueInt_[WINS_ENABLEMENT] = BinaryConverter.byteArrayToInt(data, 36);
    }
    effectiveValueStr_[GUEST_USER_PROFILE] = conv.byteArrayToString(data,48,10).trim();
    if (!userChangedAttribute_[GUEST_USER_PROFILE]) {
      pendingValueStr_[GUEST_USER_PROFILE] = conv.byteArrayToString(data,58,10).trim();
    }
    effectiveValueStr_[SERVER_NAME] = conv.byteArrayToString(data,68,15).trim();
    if (!userChangedAttribute_[SERVER_NAME]) {
      pendingValueStr_[SERVER_NAME] = conv.byteArrayToString(data,83,15).trim();
    }
    effectiveValueStr_[DOMAIN_NAME] = conv.byteArrayToString(data,98,15).trim();
    if (!userChangedAttribute_[DOMAIN_NAME]) {
      pendingValueStr_[DOMAIN_NAME] = conv.byteArrayToString(data,113,15).trim();
    }
    effectiveValueStr_[DESCRIPTION] = conv.byteArrayToString(data,128,50).trim();
    if (!userChangedAttribute_[DESCRIPTION]) {
      pendingValueStr_[DESCRIPTION] = conv.byteArrayToString(data,178,50).trim();
    }
    effectiveValueStr_[WINS_PRIMARY_ADDRESS] = conv.byteArrayToString(data,228,15).trim();
    if (!userChangedAttribute_[WINS_PRIMARY_ADDRESS]) {
      pendingValueStr_[WINS_PRIMARY_ADDRESS] = conv.byteArrayToString(data,243,15).trim();
    }
    effectiveValueStr_[WINS_SECONDARY_ADDRESS] = conv.byteArrayToString(data,258,15).trim();
    if (!userChangedAttribute_[WINS_SECONDARY_ADDRESS]) {
      pendingValueStr_[WINS_SECONDARY_ADDRESS] = conv.byteArrayToString(data,273,15).trim();
    }
    effectiveValueStr_[WINS_SCOPE_ID] = conv.byteArrayToString(data,288,224).trim();
    if (!userChangedAttribute_[WINS_SCOPE_ID]) {
      pendingValueStr_[WINS_SCOPE_ID] = conv.byteArrayToString(data,512,224).trim();
    }
    effectiveValueStr_[ALLOW_SYSTEM_NAME] = conv.byteArrayToString(data,736,1);
    if (!userChangedAttribute_[ALLOW_SYSTEM_NAME]) {
      pendingValueStr_[ALLOW_SYSTEM_NAME] = conv.byteArrayToString(data,737,1);
    }
    effectiveValueStr_[AUTHENTICATION_METHOD] = conv.byteArrayToString(data,738,1);
    if (!userChangedAttribute_[AUTHENTICATION_METHOD]) {
      pendingValueStr_[AUTHENTICATION_METHOD] = conv.byteArrayToString(data,739,1);
    }
    effectiveValueInt_[LOGON_SUPPORT] = BinaryConverter.byteArrayToInt(data, 740); // a.k.a. "server role"
    if (!userChangedAttribute_[LOGON_SUPPORT]) {
      pendingValueInt_[LOGON_SUPPORT] = BinaryConverter.byteArrayToInt(data, 744);
    }
    if (DEBUG) System.out.println("DEBUG: data.length == " + data.length);
    if (getSystemVRM() >= 0x00050400)
    { // new fields added to API in V5R4
      effectiveValueInt_[MESSAGE_AUTHENTICATION] = BinaryConverter.byteArrayToInt(data, 748);
      if (!userChangedAttribute_[MESSAGE_AUTHENTICATION]) {
        pendingValueInt_[MESSAGE_AUTHENTICATION] = BinaryConverter.byteArrayToInt(data, 752);
      }
      effectiveValueInt_[MIN_MESSAGE_SEVERITY] = BinaryConverter.byteArrayToInt(data, 756);
      if (!userChangedAttribute_[MIN_MESSAGE_SEVERITY]) {
        pendingValueInt_[MIN_MESSAGE_SEVERITY] = BinaryConverter.byteArrayToInt(data, 760);
      }
      effectiveValueInt_[LAN_MGR_AUTHENTICATION] = BinaryConverter.byteArrayToInt(data, 764);
      if (!userChangedAttribute_[LAN_MGR_AUTHENTICATION]) {
        pendingValueInt_[LAN_MGR_AUTHENTICATION] = BinaryConverter.byteArrayToInt(data, 768);
      }
    }

  }


  /**
   Reports whether the user has *IOSYSCFG authority.
   This authority is required in order to invoke the following API's:
   QTOCAUTO, QZLSCHSG, QZLSCHSI, QZLSCHSN
   **/
  private boolean userHasSpecialAuthority()
  {
    if (!determinedSpecialAuthority_)
    {
      boolean foundAuth = false;
      try
      {
        User user = new User(system_, system_.getUserId());
        String[] authorities = (String[])user.getSpecialAuthority();
        if (authorities != null) {
          for (int i=0; i<authorities.length && !foundAuth; i++) {
            if (authorities[i].equals(User.SPECIAL_AUTHORITY_IO_SYSTEM_CONFIGURATION) ) {
              foundAuth = true;
            }
          }
        }
      }
      catch (Exception e) {} // This will never happen.  The User constructor doesn't actually throw any exceptions anymore.
      if (!foundAuth) {
        Trace.log(Trace.WARNING, "User " + system_.getUserId() + " does not have *IOSYSCFG authority.");
      }
      userHasSpecialAuthority_ = foundAuth;
      determinedSpecialAuthority_ = true;
    }
    return userHasSpecialAuthority_;
  }


  /**
   Ends the NetServer job on the i5/OS system.
   <br>This method requires *IOSYSCFG special authority on the system.
   @exception  AS400SecurityException  If a security or authority error occurs.
   @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
   @exception  InterruptedException  If this thread is interrupted.
   @exception  IOException  If an error occurs while communicating with the system.
   @exception  ObjectDoesNotExistException  If the system object does not exist.
   **/
  public void end()
    throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
  {
    if (!userHasSpecialAuthority()) {
      Trace.log(Trace.ERROR, "*IOSYSCFG authority is required in order to end the NetServer process.");
      throw new AS400SecurityException(AS400SecurityException.SPECIAL_AUTHORITY_INSUFFICIENT);
    }

    // Compose the arguments for the API.
    ProgramParameter[] parms = new ProgramParameter[1];
    parms[0] = errorCode_;

    ProgramCall pc = new ProgramCall(system_, "/QSYS.LIB/QZLSENDS.PGM", parms);

    if (!pc.run()) {
      throw new AS400Exception(pc.getMessageList());
    }
  }


  /**
   Ends a specific NetServer session.
   <br>This method requires *IOSYSCFG special authority on the system.
   @exception  AS400SecurityException  If a security or authority error occurs.
   @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
   @exception  InterruptedException  If this thread is interrupted.
   @exception  IOException  If an error occurs while communicating with the system.
   @exception  ObjectDoesNotExistException  If the system object does not exist.
   **/
  public void endSession(long sessionID)
    throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
  {
    endSessions(null, sessionID);
  }


  /**
   Ends all the sessions that were established from the specified workstation.
   <br>This method requires *IOSYSCFG special authority on the system.
   @exception  AS400SecurityException  If a security or authority error occurs.
   @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
   @exception  InterruptedException  If this thread is interrupted.
   @exception  IOException  If an error occurs while communicating with the system.
   @exception  ObjectDoesNotExistException  If the system object does not exist.
   **/
  public void endSessionsForWorkstation(String workstationName)
    throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
  {
    if (workstationName == null) { throw new NullPointerException(); }

    endSessions(workstationName, 0L);
  }


  /**
   Ends session(s) specified by the args.
   Note: If workstationName is non-null, sessionID is ignored.
   If workstationName is null, sessionID is required.
   @exception  AS400SecurityException  If a security or authority error occurs.
   @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
   @exception  InterruptedException  If this thread is interrupted.
   @exception  IOException  If an error occurs while communicating with the system.
   @exception  ObjectDoesNotExistException  If the system object does not exist.
   **/
  private void endSessions(String workstationName, long sessionID)
    throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
  {
    if (!userHasSpecialAuthority()) {
      Trace.log(Trace.ERROR, "*IOSYSCFG authority is required in order to end a NetServer session.");
      throw new AS400SecurityException(AS400SecurityException.SPECIAL_AUTHORITY_INSUFFICIENT);
    }

    // Compose the arguments for the API.

    ProgramParameter[] parms = new ProgramParameter[3];
    final AS400Text text15 = new AS400Text(15, system_.getCcsid());
    final AS400Text text45 = new AS400Text(45, system_.getCcsid());	//@IPv6
    if (workstationName == null) {
    	parms[0] = new ProgramParameter(text15.toBytes("*SESSID"));
    	parms[2] = new ProgramParameter(BinaryConverter.longToByteArray(sessionID));
    }
    else {
    	if(getSystemVRM() < 0x00050500)	// @IPv6 - specify to end all sessions for the specified workstation like we always have
    	{								
    		parms[0] = new ProgramParameter(text15.toBytes(workstationName));
    		parms[2] = new ProgramParameter(BinaryConverter.longToByteArray(0L));
    	}								
    	else{																	// @IPv6
    		// Utilize the expanded workstation name parameter.					// @IPv6
    		parms[0] = new ProgramParameter(text15.toBytes("*EXPANDED"));		// @IPv6
    		parms[2] = new ProgramParameter(text45.toBytes(workstationName));	// @IPv6
    	}
    }
    parms[1] = errorCode_;

    ProgramCall pc = new ProgramCall(system_, "/QSYS.LIB/QZLSENSS.PGM", parms);

    if (!pc.run()) {
      throw new AS400Exception(pc.getMessageList());
    }
  }


  /**
   Indicates whether or not the NetServer job on the i5/OS system is started.
   @return  <tt>true</tt> if the NetServer job is started; <tt>false</tt> otherwise.
   @exception  AS400SecurityException  If a security or authority error occurs.
   @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
   @exception  InterruptedException  If this thread is interrupted.
   @exception  IOException  If an error occurs while communicating with the system.
   @exception  ObjectDoesNotExistException  If the system object does not exist.
   **/
  public boolean isStarted()
    throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
  {
    // Note: If the NetServer job has been successfully started, there will be at least one QZLSSERVER job in ACTIVE status.
    // Set the selection so that only jobs with the name "QZLSSERVER", in ACTIVE status, are included in the list.
    JobList list = new JobList(getSystem());
    try {
      list.addJobSelectionCriteria(JobList.SELECTION_JOB_NAME, "QZLSSERVER");
      list.addJobSelectionCriteria(JobList.SELECTION_PRIMARY_JOB_STATUS_ACTIVE, Boolean.TRUE);
      list.addJobSelectionCriteria(JobList.SELECTION_PRIMARY_JOB_STATUS_JOBQ, Boolean.FALSE);
      list.addJobSelectionCriteria(JobList.SELECTION_PRIMARY_JOB_STATUS_OUTQ, Boolean.FALSE);
    } catch (PropertyVetoException e) {} // this will never happen
    list.load();
    boolean foundActiveJob;
    if (list.getLength() != 0) {
      foundActiveJob = true;
    }
    else foundActiveJob = false;

    list.close();
    return foundActiveJob;
  }


  /**
   Indicates whether or not the QSERVER subsystem is started.
   If QSERVER isn't running, we can't start the NetServer job.
   @return  <tt>true</tt> if the QSERVER subsystem is started; <tt>false</tt> otherwise.
   **/
  private boolean isQserverStarted()
    throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
  {
    // Note: If the QSERVER subsystem is running, there will be at least one QSERVER job in ACTIVE status.
    // Set the selection so that only jobs with the name "QSERVER", user QSYS, in ACTIVE status, are included in the list.
    JobList jobList = new JobList(getSystem());
    try {
      jobList.addJobSelectionCriteria(JobList.SELECTION_JOB_NAME, "QSERVER");
      jobList.addJobSelectionCriteria(JobList.SELECTION_USER_NAME, "QSYS");
      jobList.addJobSelectionCriteria(JobList.SELECTION_PRIMARY_JOB_STATUS_ACTIVE, Boolean.TRUE);
    } catch (PropertyVetoException e) {} // this will never happen
    jobList.load();
    boolean foundActiveJob;
    if (jobList.getLength() != 0) foundActiveJob = true;
    else foundActiveJob = false;

    jobList.close();
    return foundActiveJob;
  }


  /**
   Starts the NetServer job on the i5/OS system.
   If the NetServer is already started, this method does nothing.
   This method requires *IOSYSCFG special authority on the system.
   If the QSERVER subsystem is not running, this method will attempt to start the subsystem.
   @exception  AS400SecurityException  If a security or authority error occurs.
   @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
   @exception  InterruptedException  If this thread is interrupted.
   @exception  IOException  If an error occurs while communicating with the system.
   @exception  ObjectDoesNotExistException  If the system object does not exist.
   **/
  public void start()
    throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
  {
    start(false);
  }

  /**
   Starts the NetServer job on the i5/OS system, and (optionally) resets it.
   If the NetServer is already started, this method does nothing.
   This method requires *IOSYSCFG special authority on the system.
   If the QSERVER subsystem is not running, this method will attempt to start it.
   <p>Note: Reset is used when the NetServer fails to start normally on the system.  It is on the NetServer context menu so an administrator can use it.  The reset does some under-the-covers cleanup, and is used infrequently.  The times it would be used is if the system ended abnormally and there may be jobs or objects hanging around that need to be cleaned up before the system can start again.  The reset does that.

   @param reset  Whether or not the system is to be reset when started.  Default is no reset.
   @exception  AS400SecurityException  If a security or authority error occurs.
   @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
   @exception  InterruptedException  If this thread is interrupted.
   @exception  IOException  If an error occurs while communicating with the system.
   @exception  ObjectDoesNotExistException  If the system object does not exist.
   **/
  public void start(boolean reset)
    throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
  {
    if (!userHasSpecialAuthority()) {
      Trace.log(Trace.ERROR, "*IOSYSCFG authority is required in order to start the NetServer process.");
      throw new AS400SecurityException(AS400SecurityException.SPECIAL_AUTHORITY_INSUFFICIENT);
    }

    if (isStarted()) {
      if (DEBUG) System.out.println("DEBUG ISeriesNetServer.start("+reset+"): NetServer is already started.");
      return;
    }

    // See if the QSERVER subsystem is running.  If it's not running, start it.
    if (!isQserverStarted()) {
      // Attempt to start the QSERVER subsystem.
      CommandCall cmd = new CommandCall(getSystem(), "STRSBS SBSD(QSERVER)");
      if (!cmd.run()) {
        Trace.log(Trace.ERROR, "Error when starting QSERVER subsystem.");
        throw new AS400Exception(cmd.getMessageList());
      }
    }

    // Note: Stopping and (re)starting the NetServer causes all "pending" attribute values
    // to take effect, replacing the (former) "current" attribute values.
    refreshedSinceStart_ = false;

    // Start the NetServer job (QZLSSERVER) on the i5/OS.

    // Compose the arguments for the API.

    ProgramParameter[] parms = new ProgramParameter[2];

    byte[] resetBytes = new byte[1];
    if (reset) {
      resetBytes[0] = (byte)0xF1;
    }
    else {
      resetBytes[0] = (byte)0xF0;
    }

    parms[0] = new ProgramParameter(resetBytes);
    parms[1] = errorCode_;

    ProgramCall pc = new ProgramCall(system_, "/QSYS.LIB/QZLSSTRS.PGM", parms);

    if (!pc.run()) {
      throw new AS400Exception(pc.getMessageList());
    }
  }


  /**
   Refreshes the attribute values of the connection object, from the current values on the system.
   @param connection The connection.
   @exception  AS400SecurityException  If a security or authority error occurs.
   @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
   @exception  InterruptedException  If this thread is interrupted.
   @exception  IOException  If an error occurs while communicating with the system.
   @exception  ObjectDoesNotExistException  If the system object does not exist.
   **/
  public void refresh(ISeriesNetServerConnection connection)
    throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
  {
    if (connection == null) { throw new NullPointerException(); }

    final int ccsid = system_.getCcsid();
    final CharConverter conv = new CharConverter(ccsid);
    final AS400Text text15 = new AS400Text(15, ccsid);

    int len = 64; // Expect 1 record, 64 bytes in length.
    int vrm = getSystemVRM();
    
    String format;
    boolean is0600;
    if (connection.getResourceType() == ISeriesNetServerConnection.WORKSTATION) {
      format = "ZLSL0600";
      is0600 = true;
    }
    else {  // SHARE
      format = "ZLSL0700";
      is0600 = false;
      if(vrm >= 0x00050500)	// @IPv6 - we can have two additional fields returned with an extra length of 49
    	  len = 113;
    }
    
    ProgramParameter[] parms = new ProgramParameter[(is0600 && vrm >= 0x00050500) ? 8 : 6];	// @IPv6 if we are requesting format 600 and we are V5R5 and later than we can have 2 additional parameters

    parms[0] = new ProgramParameter(len);                                // receiver variable
    parms[1] = new ProgramParameter(BinaryConverter.intToByteArray(len));// length of receiver variable
    parms[2] = new ProgramParameter(64);                           // list information
    parms[3] = new ProgramParameter(conv.stringToByteArray(format));
    if((is0600) && (vrm >= 0x00050500))	// @IPv6
    {													// @IPv6
    	AS400Text text10 = new AS400Text(10, ccsid);	// @IPv6 Session user
    	AS400Text text45 = new AS400Text(45, ccsid);	// @IPv6 Expanded Information Qualifier
    	parms[4] = new ProgramParameter(text15.toBytes("*EXPANDED"));	// @IPv6
    	parms[6] = new ProgramParameter(text10.toBytes("*ALL"));		// @IPv6
    	parms[7] = new ProgramParameter(text45.toBytes(connection.getName())); // @IPv6
    }													// @IPv6
    else												// @IPv6
    	parms[4] = new ProgramParameter(text15.toBytes(connection.getName()));      // information qualifier
    parms[5] = errorCode_;

    ProgramCall pc = new ProgramCall(system_, "/QSYS.LIB/QZLSOLST.PGM", parms);
    int numRecords = callListProgram(pc, parms, len);

    // We expect exactly one record to come back.
    if (numRecords == 0) {
      Trace.log(Trace.ERROR, "refresh() failed: Connection not found.");
    }
    else if (numRecords > 1) {
      Trace.log(Trace.ERROR, "refresh() failed: More than one connection returned from QZLSOLST.");
    }
    else  // Exactly 1 record was returned in list.
    {
      byte[] data = parms[0].getOutputData();
      parseZLSL0600or0700(data, 0, conv, is0600, connection);
    }
  }


  /**
   Refreshes the attribute values of the session object, from the current values on the system.
   @param session The session.
   @exception  AS400SecurityException  If a security or authority error occurs.
   @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
   @exception  InterruptedException  If this thread is interrupted.
   @exception  IOException  If an error occurs while communicating with the system.
   @exception  ObjectDoesNotExistException  If the system object does not exist.
   **/
  public void refresh(ISeriesNetServerSession session)
    throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
  {
    if (session == null) { throw new NullPointerException(); }

    final int ccsid = system_.getCcsid();
    final CharConverter conv = new CharConverter(ccsid);
    final AS400Text text10 = new AS400Text(10, ccsid);
    final AS400Text text15 = new AS400Text(15, ccsid);

    int len = (getSystemVRM() < 0x00050500) ? 64 : 113; // Expect 1 record, 64 bytes in length if V5R4 or earlier, else 113 bytes

    ProgramParameter[] parms = new ProgramParameter[8];

    parms[0] = new ProgramParameter(len);                                // receiver variable
    parms[1] = new ProgramParameter(BinaryConverter.intToByteArray(len));// length of receiver variable
    parms[2] = new ProgramParameter(64);                      // list information
    parms[3] = new ProgramParameter(conv.stringToByteArray("ZLSL0300"));
    parms[4] = new ProgramParameter(text15.toBytes("*SESSID"));  // information qualifier
    parms[5] = errorCode_;
    parms[6] = new ProgramParameter(text10.toBytes("*SESSID"));  // session user
    parms[7] = new ProgramParameter(BinaryConverter.longToByteArray(session.getID())); // session identifier

    ProgramCall pc = new ProgramCall(system_, "/QSYS.LIB/QZLSOLST.PGM", parms);
    int numRecords = callListProgram(pc, parms, len);

    // We expect exactly one record to come back.
    if (numRecords == 0) {
      Trace.log(Trace.ERROR, "refresh() failed: Session not found.");
    }
    else if (numRecords > 1) {
      Trace.log(Trace.ERROR, "refresh() failed: More than one session returned from QZLSOLST.");
    }
    else  // Exactly 1 record was returned in list.
    {
      byte[] data = parms[0].getOutputData();
      parseZLSL0300(data, 0, conv, session);
    }
  }


  /**
   Refreshes the attribute values of the share object, from the currently in-effect values on the system.
   Note: This overwrites any attribute changes that have been made but not yet committed.
   @param share The share.
   @exception  AS400SecurityException  If a security or authority error occurs.
   @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
   @exception  InterruptedException  If this thread is interrupted.
   @exception  IOException  If an error occurs while communicating with the system.
   @exception  ObjectDoesNotExistException  If the system object does not exist.
   **/
  public void refresh(ISeriesNetServerShare share)
    throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
  {
    if (share == null) { throw new NullPointerException(); }

    final int ccsid = system_.getCcsid();
    final CharConverter conv = new CharConverter(ccsid);
    final AS400Text text15 = new AS400Text(15, ccsid);

    int len = ZLSL0101_MAX_RECORD_LENGTH;  // We expect a single ZLSL0101 record back.
    ProgramParameter[] parms = new ProgramParameter[6];

    parms[0] = new ProgramParameter(len);                     // receiver variable
    parms[1] = new ProgramParameter(BinaryConverter.intToByteArray(len));// length of receiver variable
    parms[2] = new ProgramParameter(64);                      // list information
    parms[3] = new ProgramParameter(conv.stringToByteArray("ZLSL0101"));
    parms[4] = new ProgramParameter(text15.toBytes(share.getName().trim()));
    parms[5] = errorCode_;

    ProgramCall pc = new ProgramCall(system_, "/QSYS.LIB/QZLSOLST.PGM", parms);
    int numRecords = callListProgram(pc, parms, len);

    // We expect exactly one record to come back.
    if (numRecords == 0) {
      Trace.log(Trace.ERROR, "refresh() failed: Share not found.");
    }
    else if (numRecords > 1) {
      Trace.log(Trace.ERROR, "refresh() failed: More than one share returned from QZLSOLST.");
    }
    else  // Exactly 1 record was returned in list.
    {
      byte[] data = parms[0].getOutputData();
      int desiredType = (share.isFile_ == true ? FILE : PRINT);
      parseZLSL0101(data, conv, 1, desiredType, share);
    }
  }


  // Deserializes and initializes transient data.
  private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException
  {
    if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "De-serializing ISeriesNetServer object.");
    in.defaultReadObject();

    refreshedSinceStart_ = false;
    determinedSpecialAuthority_ = false;
    userHasSpecialAuthority_ = false;
  }

    /**
     Returns the String representation of this ISeriesNetServer object.
     @return  The String representation of this ISeriesNetServer object.
     **/
    public String toString()
    {
      String name;
      if (refreshedSinceStart_) { name = effectiveValueStr_[SERVER_NAME]; }
      else                      { name = "unknown"; }

      return "ISeriesNetServer (system: " + system_.getSystemName() + "; name: "+ name + "): " + super.toString();
    }


    /**
     Commits attribute value changes to the system, for the specified share.
     @param share The share.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the system object does not exist.
     **/
  public void commitChanges(ISeriesNetServerFileShare share)
    throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
  {
    if (!userHasSpecialAuthority()) {
      Trace.log(Trace.ERROR, "*IOSYSCFG authority is required in order to change share attributes.");
      throw new AS400SecurityException(AS400SecurityException.SPECIAL_AUTHORITY_INSUFFICIENT);
    }

    // How many of the API's "optional parameters" do we need to set:
    int numberOfOptionalParms = share.numOptionalParmsToSet_;

    if (DEBUG) System.out.println("DEBUG: ISeriesNetServer.commitChanges("+share.getName()+", " + numberOfOptionalParms +")");

    // Compose the arguments for the API.

    final int ccsid = system_.getCcsid();
    final CharConverter conv = new CharConverter(ccsid);
    final AS400Text text12 = new AS400Text(12, ccsid);
    final AS400Text text50 = new AS400Text(50, ccsid);

    // The API has 8 required parameters, plus up to 4 optional parameters.
    ProgramParameter[] parms = new ProgramParameter[8+numberOfOptionalParms];

    // Required parameters:

    parms[0] = new ProgramParameter(text12.toBytes(share.name_));
    parms[1] = new ProgramParameter(conv.stringToByteArray(share.path_));
    // Note: 2 bytes per Unicode character:
    parms[2] = new ProgramParameter(BinaryConverter.intToByteArray(2 * share.path_.length()));
    parms[3] = new ProgramParameter(BinaryConverter.intToByteArray(0));
    parms[4] = new ProgramParameter(text50.toBytes(share.description_));
    parms[5] = new ProgramParameter(BinaryConverter.intToByteArray(share.permissions_));
    parms[6] = new ProgramParameter(BinaryConverter.intToByteArray(share.maxNumberOfUsers_));
    parms[7] = errorCode_;

    // Optional parameters:

    if (numberOfOptionalParms >= 1)
    {
      parms[8] = new ProgramParameter(BinaryConverter.intToByteArray(share.ccsidForTextConversion_));
    }

    if (numberOfOptionalParms >= 2)
    {
      byte[] enableTxtConv = new byte[1];
      if (share.textConversionEnablement_.equals("0")) {
        enableTxtConv[0] = (byte)0xF0;
      }
      else if (share.textConversionEnablement_.equals("1")) {
        enableTxtConv[0] = (byte)0xF1;
      }
      else {
        enableTxtConv[0] = (byte)0xF2;
      }
      parms[9] = new ProgramParameter(enableTxtConv);
    }

    if (numberOfOptionalParms >= 4)
    {
      // Compose the "file extension table" parameter.

      int numFileExtensions = share.fileExtensions_.length;
      if (numFileExtensions == 0) {  // No file extensions.
        parms[10] = new ProgramParameter(conv.stringToByteArray(""));  // empty list
      }
      else // Compose a byte array representing the file extension table.
      {
        // Block out a byte array: 50 bytes per table entry.
        ByteArrayOutputStream stream = new ByteArrayOutputStream(numFileExtensions * 50);
        for (int i=0; i<numFileExtensions; i++)
        {
          int extensionLength = share.fileExtensions_[i].length();
          stream.write(BinaryConverter.intToByteArray(extensionLength));
          stream.write(conv.stringToByteArray(share.fileExtensions_[i]));
          // Fill remainder of entry with null terminators, for a total length of 50 bytes.
          int numFillBytes = 50 - (4+extensionLength); // The 'length' field occupies 4 bytes.
          for (int j=0; j<numFillBytes; j++) {
            stream.write(0x00);  // fill with null terminators
          }
        }
        stream.flush();
        byte[] fileExtensionTable = stream.toByteArray();
        parms[10] = new ProgramParameter(fileExtensionTable);
      }

      parms[11] = new ProgramParameter(BinaryConverter.intToByteArray(numFileExtensions));
    }

    ProgramCall pc = new ProgramCall(system_, "/QSYS.LIB/QZLSCHFS.PGM", parms);

    if (!pc.run()) {
      throw new AS400Exception(pc.getMessageList());
    }

    share.numOptionalParmsToSet_ = 0;
  }


    /**
     Commits attribute value changes to the system, for the specified share.
     @param share The share.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the system object does not exist.
     **/
  public void commitChanges(ISeriesNetServerPrintShare share)
    throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
  {
    if (!userHasSpecialAuthority()) {
      Trace.log(Trace.ERROR, "*IOSYSCFG authority is required in order to change share attributes.");
      throw new AS400SecurityException(AS400SecurityException.SPECIAL_AUTHORITY_INSUFFICIENT);
    }

    // How many of the API's "optional parameters" do we need to set:
    int numberOfOptionalParms = share.numOptionalParmsToSet_;

    if (DEBUG) System.out.println("DEBUG: ISeriesNetServer.commitChanges("+share.getName()+")");

    // Assume the caller has done a refresh (to avoid overwriting current attr values that the user isn't interested in changing).

    // Compose the arguments for the API.

    final int ccsid = system_.getCcsid();
    final AS400Text text12 = new AS400Text(12, ccsid);
    final AS400Text text20 = new AS400Text(20, ccsid);
    final AS400Text text50 = new AS400Text(50, ccsid);

    // The API has 6 required parameters, plus up to 2 optional parameters.
    ProgramParameter[] parms = new ProgramParameter[6+numberOfOptionalParms];

    // Required parameters:

    parms[0] = new ProgramParameter(text12.toBytes(share.name_));
    parms[1] = new ProgramParameter(text20.toBytes(share.outputQueue_));
    parms[2] = new ProgramParameter(text50.toBytes(share.description_));
    parms[3] = new ProgramParameter(BinaryConverter.intToByteArray(share.spooledFileType_));
    parms[4] = new ProgramParameter(text50.toBytes(share.printDriver_));
    parms[5] = errorCode_;

    // Optional parameters:

    if (numberOfOptionalParms >= 1) {
      parms[6] = new ProgramParameter(text20.toBytes(share.printerFile_));
    }

    if (numberOfOptionalParms >= 2) {
      byte[] publishPrintShare = new byte[1];
      if (share.isPublished_ == true) {
        publishPrintShare[0] = (byte)0xF1;
      }
      else {
        publishPrintShare[0] = (byte)0xF0;
      }
      parms[7] = new ProgramParameter(publishPrintShare);
    }

    ProgramCall pc = new ProgramCall(system_, "/QSYS.LIB/QZLSCHPS.PGM", parms);

    if (!pc.run()) {
      throw new AS400Exception(pc.getMessageList());
    }

    share.numOptionalParmsToSet_ = 0;
  }


}
