///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: NetServerFileShare.java
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

/**
 The NetServerFileShare class represents a NetServer file server share.
 NetServerFileShare objects are created and returned by {@link NetServer#listFileShares() NetServer.listFileShares()}.
 <p>
 <i>Note: This class uses some API fields that are not available prior to OS/400 V5R1.</i>
 <p>
 <i>Note: The methods that <b>add</b> or <b>change</b> NetServer File Shares are not supported prior to OS/400 V5R1.</i>
<p>
<a name="attributeIDs">The following attribute IDs are supported:
<ul>
<li>{@link #DESCRIPTION DESCRIPTION}
<li>{@link #MAXIMUM_USERS MAXIMUM_USERS}
<li>{@link #PATH PATH}
<li>{@link #PERMISSION PERMISSION}
<li>{@link #USER_COUNT USER_COUNT}
</ul>

<p>Use any of the above attribute IDs with
{@link com.ibm.as400.resource.ChangeableResource#getAttributeValue(java.lang.Object) getAttributeValue}
and
{@link com.ibm.as400.resource.ChangeableResource#setAttributeValue(java.lang.Object,java.lang.Object) setAttributeValue} to access the attribute values for a NetServerFileShare.
<br>
Note: For the above attributes, getAttributeValue() should never return null.
For String-valued attributes, if the current actual value of the corresponding property on the server is blank, getAttributeValue() will return "" (an empty String).

<p>Note: Typically, methods which add, change, or remove a NetServerFileShare require that the server user profile has *IOSYSCFG special authority, or that the user own the integrated file system directory.

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
*   // List all current file shares.
*   System.out.println("File shares:");
*   ResourceList shareList = ns.listFileShares();
*   shareList.waitForComplete();
*   for (int i=0; i&lt;shareList.getListLength(); i++)
*   {
*     NetServerFileShare share = (NetServerFileShare)shareList.resourceAt(i);
*     System.out.println(share.getName() + ": " +
*       (String)share.getAttributeValue(NetServerFileShare.PATH) + ": " +
*       (String)share.getAttributeValue(NetServerFileShare.DESCRIPTION) + "; " +
*       ((Integer)share.getAttributeValue(NetServerFileShare.USER_COUNT))
*                                                             .intValue() );
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
 {@link com.ibm.as400.access.ISeriesNetServerFileShare ISeriesNetServerFileShare}
 class and may be removed in a future release.
**/

public class NetServerFileShare
extends NetServerShare
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


    static final long serialVersionUID = 4L;


  // Attribute setter map.
  private static ProgramMap setterMap_    = new ProgramMap();
  private static ProgramMap openListAttributeMap_  = new ProgramMap();

  // API names.
  private static final String ADFS_ = "qzlsadfs";
  private static final String CHFS_ = "qzlschfs";

  //-------------------------------------------------------------------------
  // Attribute IDs.
  //
  // * If you add an attribute here, make sure and add it to the class javadoc.
  //-------------------------------------------------------------------------

  // Common attributes for all NetServerShare subclasses: DESCRIPTION, USER_COUNT.
  // Note: The subclasses have distinct setter maps for these attributes.

  // Add a setter map entry for the DESCRIPTION attribute (defined in the superclass).
  static {
    // First ensure that the superclass has been loaded, since we are referencing its static data here.
    try { Class.forName("com.ibm.as400.access.NetServerShare"); }
    catch (ClassNotFoundException e) {}  // This will never happen.
    setterMap_.add (NetServerShare.DESCRIPTION, CHFS_, "description");
    openListAttributeMap_.add (DESCRIPTION, OLST0100_, "receiverVariable.description");
  }

  // Add an open list map entry for the USER_COUNT attribute (defined in the superclass).
  static {
    openListAttributeMap_.add (USER_COUNT, OLST0100_, "receiverVariable.currentUsers");
 }


  // Unique attributes for class NetServerFileShare:

  /**
   Attribute ID for "maximum users".  This identifies an Integer attribute, which represents the maximum number of users who can concurrently access this share.
   The value must be greater than or equal to zero, or -1,
   which means there is no limit to the number of users who can concurrently use this share.
   <br>The default is -1.
   **/
  public static final String MAXIMUM_USERS = "MAXIMUM_USERS";
  static {
    attributes_.add(MAXIMUM_USERS, Integer.class, new Integer(-1));
    getterMap_.add (MAXIMUM_USERS, OLST0100_, "receiverVariable.maximumUsers", INDICES_);
    setterMap_.add (MAXIMUM_USERS, CHFS_, "maximumUsers");
    openListAttributeMap_.add (MAXIMUM_USERS, OLST0100_, "receiverVariable.maximumUsers");
  }

  /**
   Attribute ID for "path".  This identifies a String
   attribute, which represents the path of a share.
   <br>The path name is the path in the integrated file system to be shared with the network.  A forward slash, '/', is required as the first character.  The maximum length is 512 characters.
   **/
  public static final String PATH = "PATH";
  static {
    attributes_.add(PATH, String.class);
    getterMap_.add (PATH, OLST0100_, "receiverVariable.pathName", INDICES_);
    setterMap_.add (PATH, CHFS_, "pathName");
    openListAttributeMap_.add (PATH, OLST0100_, "receiverVariable.pathName");
  }

  // Note: This is a package-scoped "attribute".
  static final String PATH_LENGTH = "PATH_LENGTH";
  static {
    attributes_.add(PATH_LENGTH, Integer.class);
    getterMap_.add (PATH_LENGTH, OLST0100_, "receiverVariable.lengthOfPathName", INDICES_);
    setterMap_.add (PATH_LENGTH, CHFS_, "lengthOfPathName");
    openListAttributeMap_.add (PATH_LENGTH, OLST0100_, "receiverVariable.lengthOfPathName");
  }

  /**
   Attribute ID for "permission".  This identifies an Integer
   attribute, which represents the permission for a share.
   Valid values are:
   <ul>
   <li>{@link #PERMISSION_READ_ONLY PERMISSION_READ_ONLY} - Read-only permission.
   <li>{@link #PERMISSION_READ_WRITE PERMISSION_READ_WRITE} - Read/write permission.
   </ul>
   The default is PERMISSION_READ_WRITE.
   **/
  public static final String PERMISSION = "PERMISSION";
  /**
   {@link #PERMISSION PERMISSION} attribute value indicating "read only" permission to a share.
   **/
  public static final Integer PERMISSION_READ_ONLY = new Integer(1);
  /**
   {@link #PERMISSION PERMISSION} attribute value indicating "read/write" permission to a share.
   **/
  public static final Integer PERMISSION_READ_WRITE = new Integer(2);
  static {
    attributes_.add(PERMISSION, Integer.class, false,
                    new Object[] {PERMISSION_READ_ONLY, PERMISSION_READ_WRITE }, PERMISSION_READ_WRITE, true);
    getterMap_.add (PERMISSION, OLST0100_, "receiverVariable.permissions", INDICES_);
    setterMap_.add (PERMISSION, CHFS_, "permissions");
    openListAttributeMap_.add (PERMISSION, OLST0100_, "receiverVariable.permissions");
  }


  /**
   Constructs a NetServerFileShare object.
   The system and share name must be set before the object is used.
   **/
  public NetServerFileShare()
  {
    super();
  }


  /**
   Constructs a NetServerFileShare object.
   @param name  The name of the share.
   **/
  public NetServerFileShare(AS400 system, String name)
  {
    super(system, name);
  }


  /**
   Adds the file server share to the NetServer.
   This method fires a resourceCreated() ResourceEvent.
   <br>The system and share name be set before this method is called.
   <br>The {@link #PATH PATH} attribute must be set before this method is called.
   <br>{@link com.ibm.as400.resource.ChangeableResource#commitAttributeChanges() commitAttributeChanges} must <b>not</b> be called prior to this method.
   <br>This method requires *IOSYSCFG special authority on the server, or that the user own the integrated file system directory.
   <br><i>Note: This method is not supported prior to OS/400 V5R1.</i>


   @exception ResourceException  If an error occurs.
   **/
  public void add()
    throws ResourceException
  {
    // Set the input parameters and call the API.
    try
    {
      freezeProperties();

      if (document_ == null) {
        document_ = (ProgramCallDocument)staticDocument_.clone();
      }
      document_.setSystem(getSystem());

      document_.setValue(ADFS_+".shareName", getName());
      document_.setValue(CHFS_+".shareName", getName());

      if (hasUncommittedAttributeChanges(DESCRIPTION)) {  // @A1a
        document_.setValue(ADFS_+".description", (String)getAttributeValue(DESCRIPTION));
        document_.setValue(CHFS_+".description", (String)getAttributeValue(DESCRIPTION));
      }

      if (hasUncommittedAttributeChanges(PATH)) {  // @A1a
        String path = (String)getAttributeValue(PATH);
        // Note: There are 2 bytes per Unicode character.  By default the PCML document specifies Unicode for the pathname CCSID.
        Integer pathLength = new Integer(path.length()*2);
        document_.setValue(ADFS_+".lengthOfPathName", pathLength);
        document_.setValue(ADFS_+".pathName", path);
        document_.setValue(CHFS_+".lengthOfPathName", pathLength);
        document_.setValue(CHFS_+".pathName", path);
      }
      else {
        throw new ExtendedIllegalStateException("path",
                                 ExtendedIllegalStateException.PROPERTY_NOT_SET);
      }

      if (hasUncommittedAttributeChanges(PERMISSION)) {  // @A1a
        document_.setValue(ADFS_+".permissions", (Integer)getAttributeValue(PERMISSION));
        document_.setValue(CHFS_+".permissions", (Integer)getAttributeValue(PERMISSION));
      }
      if (hasUncommittedAttributeChanges(MAXIMUM_USERS)) {  // @A1a
        document_.setValue(ADFS_+".maximumUsers", (Integer)getAttributeValue(MAXIMUM_USERS));
        document_.setValue(CHFS_+".maximumUsers", (Integer)getAttributeValue(MAXIMUM_USERS));
      }

      if (! isConnectionEstablished()) {
        establishConnection(false);
      }

      if (document_.callProgram(ADFS_) == false) {
        throw new ResourceException(document_.getMessageList(ADFS_));
      }

      fireResourceCreated();
    }
    catch (PcmlException e) {
      Trace.log(Trace.ERROR, "PcmlException when adding a file share.", e);
      throw new ResourceException(e);
    }
  }


  /**
   Commits the specified attribute changes.
   This method fires an attributeChangesCommitted() ResourceEvent.
   <br>This method requires *IOSYSCFG special authority on the server.

   @exception ResourceException  If an error occurs.
   **/
  protected void commitAttributeChanges(Object[] attributeIDs, Object[] values)
    throws ResourceException
  {
    // See if we are changing PATH.  If so, also change PATH_LENGTH accordingly.
    for (int i=0; i<attributeIDs.length; i++)
    {
      String id = (String)attributeIDs[i];
      if (id.equals(PATH))
      {
        String path = (String)values[i];
        int pathLength = path.length() * 2;  // 2 bytes per Unicode character.
        setAttributeValue(PATH_LENGTH, new Integer(pathLength));

        int numIDs = attributeIDs.length;
        // Implementation note: Assume that attributeIDs[] and values[] have same length.

        Object[] ids = new Object[numIDs+1];
        System.arraycopy(attributeIDs, 0, ids, 0, numIDs);
        ids[numIDs] = PATH_LENGTH;

        Object[] vals = new Object[numIDs+1];
        System.arraycopy(values, 0, vals, 0, numIDs);
        vals[numIDs] = new Integer(pathLength);

        attributeIDs = ids;
        values = vals;
        break;
      }
    }

    super.commitAttributeChanges(attributeIDs, values);
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
    establishConnection(true);
  }


  /**
   Establishes the connection to the server.

   @param initializeSetterFromSystem  Indicates whether or not the setter's values should be initialized from the system.  For example, this would be the case when establishing a connection to an existing share, rather than adding a new share.

   @exception ResourceException  If an error occurs.
   **/
  /*protected*/ void establishConnection(boolean initializeSetterFromSystem)
    throws ResourceException
  {
    Object[] attrsToSetFirst = new Object[] { PATH_LENGTH };
    super.establishConnection(setterMap_, attrsToSetFirst, initializeSetterFromSystem);
  }


  // Returns a list of NetServerFileShare objects.
  static ResourceList list(AS400 sys)
    throws ResourceException
  {
    return list(sys, "*ALL");
  }


  // Returns a list of NetServerFileShare objects.
  static ResourceList list(AS400 sys, String shareName)
    throws ResourceException
  {
    return NetServerShare.list(sys, NetServerShare.FILE_SHARE, shareName, openListAttributeMap_);
  }

}
