///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: NetServerPrintShare.java
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
 The NetServerPrintShare class represents a NetServer print server share.
 NetServerPrintShare objects are created and returned by {@link NetServer#listPrintShares() NetServer.listPrintShares()}.
 <p>
 <i>Note: This class uses some API fields that are not available prior to OS/400 V5R1.</i>
<p>
<a name="attributeIDs">The following attribute IDs are supported:
<ul>
<li>{@link #DESCRIPTION DESCRIPTION}
<li>{@link #OUTPUT_QUEUE_LIBRARY OUTPUT_QUEUE_LIBRARY}
<li>{@link #OUTPUT_QUEUE_NAME OUTPUT_QUEUE_NAME}
<li>{@link #PRINT_DRIVER_TYPE PRINT_DRIVER_TYPE}
<li>{@link #SPOOLED_FILE_TYPE SPOOLED_FILE_TYPE}
<li>{@link #USER_COUNT USER_COUNT}
</ul>

<p>Use any of the above attribute IDs with
{@link com.ibm.as400.resource.ChangeableResource#getAttributeValue(java.lang.Object) getAttributeValue}
and
{@link com.ibm.as400.resource.ChangeableResource#setAttributeValue(java.lang.Object,java.lang.Object) setAttributeValue} to access the attribute values for a NetServerPrintShare.
<br>
Note: For the above attributes, getAttributeValue() should never return null.
For String-valued attributes, if the current actual value of the corresponding property on the server is blank, getAttributeValue() will return "" (an empty String).

<p>Note: Typically, methods which add, change, or remove a NetServerPrintShare require that the server user profile has *IOSYSCFG special authority, or that the user owns the output queue on the server.

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
*   // List all current print shares.
*   System.out.println("Print shares:");
*   ResourceList shareList = ns.listPrintShares();
*   shareList.waitForComplete();
*   for (int i=0; i&lt;shareList.getListLength(); i++)
*   {
*     NetServerPrintShare share = (NetServerPrintShare)shareList.resourceAt(i);
*     System.out.println(share.getName() + ": " +
*       (String)share.getAttributeValue(NetServerPrintShare.OUTPUT_QUEUE_NAME) + ": " +
*       (String)share.getAttributeValue(NetServerPrintShare.DESCRIPTION) + "; " +
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
</blockquote>NetServerPrintShare

 @deprecated This class has been replaced by the
 {@link com.ibm.as400.access.ISeriesNetServerPrintShare ISeriesNetServerPrintShare}
 class and may be removed in a future release.
**/

public class NetServerPrintShare
extends NetServerShare
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


    static final long serialVersionUID = 4L;


  // Attribute setter map.
  private static ProgramMap setterMap_    = new ProgramMap();
  private static ProgramMap openListAttributeMap_  = new ProgramMap();

  // API names.
  private static final String ADPS_ = "qzlsadps";
  private static final String CHPS_ = "qzlschps";


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
    setterMap_.add (NetServerShare.DESCRIPTION, CHPS_, "description");
    openListAttributeMap_.add (DESCRIPTION, OLST0100_, "receiverVariable.description");
  }

  // Add an open list map entry for the USER_COUNT attribute (defined in the superclass).
  static {
    openListAttributeMap_.add (USER_COUNT, OLST0100_, "receiverVariable.currentUsers");
 }


  // Unique attributes for class NetServerPrintShare:

  /**
   Attribute ID for "output queue library".  This identifies a String
   attribute, which represents the library that contains the output queue associated with a print share.
   @see #OUTPUT_QUEUE_NAME
   **/
  public static final String OUTPUT_QUEUE_LIBRARY = "OUTPUT_QUEUE_LIBRARY";
  static {
    attributes_.add(OUTPUT_QUEUE_LIBRARY, String.class);
    getterMap_.add (OUTPUT_QUEUE_LIBRARY, OLST0100_, "receiverVariable.qualifiedOutputQueueName.libraryName", INDICES_);
    setterMap_.add (OUTPUT_QUEUE_LIBRARY, CHPS_, "qualifiedOutputQueueName.libraryName");
    openListAttributeMap_.add (OUTPUT_QUEUE_LIBRARY, OLST0100_, "receiverVariable.qualifiedOutputQueueName.libraryName");
  }

  /**
   Attribute ID for "output queue name".  This identifies a String
   attribute, which represents the name of the output queue associated with a print share.
   @see #OUTPUT_QUEUE_LIBRARY
   **/
  public static final String OUTPUT_QUEUE_NAME = "OUTPUT_QUEUE_NAME";
  static {
    attributes_.add(OUTPUT_QUEUE_NAME, String.class);
    getterMap_.add (OUTPUT_QUEUE_NAME, OLST0100_, "receiverVariable.qualifiedOutputQueueName.queueName", INDICES_);
    setterMap_.add (OUTPUT_QUEUE_NAME, CHPS_, "qualifiedOutputQueueName.queueName");
    openListAttributeMap_.add (OUTPUT_QUEUE_NAME, OLST0100_, "receiverVariable.qualifiedOutputQueueName.queueName");
  }

  /**
   Attribute ID for "print driver type".  This identifies a String
   attribute, which represents the type of printer driver for a share.
   <br>
   The print driver type is a text string that identifies the print driver appropriate for a share. When personal computers connect to this shared printer, this identifies the print driver that they should use. This text should match the name of a print driver known to the personal computer operating system.
   **/
  public static final String PRINT_DRIVER_TYPE = "PRINT_DRIVER_TYPE";
  static {
    attributes_.add(PRINT_DRIVER_TYPE, String.class, "");
    getterMap_.add (PRINT_DRIVER_TYPE, OLST0100_, "receiverVariable.printDriverType", INDICES_);
    setterMap_.add (PRINT_DRIVER_TYPE, CHPS_, "printDriverType");
    openListAttributeMap_.add (PRINT_DRIVER_TYPE, OLST0100_, "receiverVariable.printDriverType");
  }


  /**
   Attribute ID for "spooled file type".  This identifies an Integer
   attribute, which represents the spooled file type for a share.
   <br>
   The spooled file type specifies the type of spooled files that will be created using this share.
   <br>
   Valid values are:
   <ul>
   <li>{@link #SPOOLED_FILE_TYPE_USER_ASCII SPOOLED_FILE_TYPE_USER_ASCII} - User ASCII.
   <li>{@link #SPOOLED_FILE_TYPE_AFP SPOOLED_FILE_TYPE_AFP} - Advanced Function Printing.
   <li>{@link #SPOOLED_FILE_TYPE_SCS SPOOLED_FILE_TYPE_SCS} - SNA character string.
   <li>{@link #SPOOLED_FILE_TYPE_AUTO_DETECT SPOOLED_FILE_TYPE_AUTO_DETECT} - Automatic type sensing.
   </ul>
   The default is {@link #SPOOLED_FILE_TYPE_AUTO_DETECT SPOOLED_FILE_TYPE_AUTO_DETECT}.
   **/
  public static final String SPOOLED_FILE_TYPE = "SPOOLED_FILE_TYPE";
  /**
   {@link #SPOOLED_FILE_TYPE SPOOLED_FILE_TYPE} attribute value indicating spooled file type "User ASCII".
   **/
  public static final Integer SPOOLED_FILE_TYPE_USER_ASCII = new Integer(1);
  /**
   {@link #SPOOLED_FILE_TYPE SPOOLED_FILE_TYPE} attribute value indicating spooled file type "Advanced Function Printing".
   **/
  public static final Integer SPOOLED_FILE_TYPE_AFP = new Integer(2);
  /**
   {@link #SPOOLED_FILE_TYPE SPOOLED_FILE_TYPE} attribute value indicating spooled file type "SNA character string".
   **/
  public static final Integer SPOOLED_FILE_TYPE_SCS = new Integer(3);
  /**
   {@link #SPOOLED_FILE_TYPE SPOOLED_FILE_TYPE} attribute value indicating "Automatic type sensing".
   **/
  public static final Integer SPOOLED_FILE_TYPE_AUTO_DETECT = new Integer(4);

  static {
    attributes_.add(SPOOLED_FILE_TYPE, Integer.class, false,
                    new Object[] {SPOOLED_FILE_TYPE_USER_ASCII, SPOOLED_FILE_TYPE_AFP, SPOOLED_FILE_TYPE_SCS, SPOOLED_FILE_TYPE_AUTO_DETECT }, SPOOLED_FILE_TYPE_AUTO_DETECT, true);
    getterMap_.add (SPOOLED_FILE_TYPE, OLST0100_, "receiverVariable.spooledFileType", INDICES_);
    setterMap_.add (SPOOLED_FILE_TYPE, CHPS_, "spooledFileType");
    openListAttributeMap_.add (SPOOLED_FILE_TYPE, OLST0100_, "receiverVariable.spooledFileType");
  }



  /**
   Constructs a NetServerPrintShare object.
   The system and share name must be set before the object is used.
   **/
  public NetServerPrintShare()
  {
    super();
  }


  /**
   Constructs a NetServerPrintShare object.
   @param system  The system.
   @param name  The name of the share.
   **/
  public NetServerPrintShare(AS400 system, String name)
  {
    super(system, name);
  }


  /**
   Adds this print server share to the NetServer.
   This method fires a resourceCreated() ResourceEvent.
   <br>The system and share name be set before this method is called.
   <br>The {@link #OUTPUT_QUEUE_NAME OUTPUT_QUEUE_NAME} and {@link #OUTPUT_QUEUE_LIBRARY OUTPUT_QUEUE_LIBRARY} attributes must be set before this method is called.
   <br>{@link com.ibm.as400.resource.ChangeableResource#commitAttributeChanges() commitAttributeChanges} must <b>not</b> be called prior to this method.
   <br>This method requires *IOSYSCFG special authority on the server, or that the user owns the output queue on the server.

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

      document_.setValue(ADPS_+".shareName", getName());
      document_.setValue(CHPS_+".shareName", getName());

      if (hasUncommittedAttributeChanges(DESCRIPTION)) {  // @A1a
        document_.setValue(ADPS_+".description", (String)getAttributeValue(DESCRIPTION));
        document_.setValue(CHPS_+".description", (String)getAttributeValue(DESCRIPTION));
      }

      if (hasUncommittedAttributeChanges(OUTPUT_QUEUE_NAME)) {  // @A1a
        document_.setValue(ADPS_+".qualifiedOutputQueueName.queueName", (String)getAttributeValue(OUTPUT_QUEUE_NAME));
        document_.setValue(CHPS_+".qualifiedOutputQueueName.queueName", (String)getAttributeValue(OUTPUT_QUEUE_NAME));
      }
      else {
        throw new ExtendedIllegalStateException("queueName",
                                 ExtendedIllegalStateException.PROPERTY_NOT_SET);
      }

      if (hasUncommittedAttributeChanges(OUTPUT_QUEUE_LIBRARY)) {  // @A1a
        document_.setValue(ADPS_+".qualifiedOutputQueueName.libraryName", (String)getAttributeValue(OUTPUT_QUEUE_LIBRARY));
        document_.setValue(CHPS_+".qualifiedOutputQueueName.libraryName", (String)getAttributeValue(OUTPUT_QUEUE_LIBRARY));
      }
      else {
        throw new ExtendedIllegalStateException("libraryName",
                                 ExtendedIllegalStateException.PROPERTY_NOT_SET);
      }

      if (hasUncommittedAttributeChanges(SPOOLED_FILE_TYPE)) {  // @A1a
        document_.setValue(ADPS_+".spooledFileType", (Integer)getAttributeValue(SPOOLED_FILE_TYPE));
        document_.setValue(CHPS_+".spooledFileType", (Integer)getAttributeValue(SPOOLED_FILE_TYPE));
      }

      if (hasUncommittedAttributeChanges(PRINT_DRIVER_TYPE)) {  // @A1a
        document_.setValue(ADPS_+".printDriverType", (String)getAttributeValue(PRINT_DRIVER_TYPE));
        document_.setValue(CHPS_+".printDriverType", (String)getAttributeValue(PRINT_DRIVER_TYPE));
      }

      if (! isConnectionEstablished()) {
        establishConnection(false);
      }

      if (document_.callProgram(ADPS_) == false) {
        throw new ResourceException(document_.getMessageList(ADPS_));
      }

      fireResourceCreated();
    }
    catch (PcmlException e) {
      Trace.log(Trace.ERROR, "PcmlException when adding a print share.", e);
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
    establishConnection(false);
  }

  /**
   Establishes the connection to the server.

   @param initializeSetterFromSystem  Indicates whether or not the setter's values should be initialized from the system.  For example, this would be the case when establishing a connection to an existing share, rather than adding a new share.

   @exception ResourceException  If an error occurs.
   **/
  /*protected*/ void establishConnection(boolean initializeSetterFromSystem)
    throws ResourceException
  {
    super.establishConnection(setterMap_, null, initializeSetterFromSystem);
  }


  // Returns a list of NetServerPrintShare objects.
  static ResourceList list(AS400 sys)
    throws ResourceException
  {
    return list(sys, "*ALL");
  }


  // Returns a list of NetServerPrintShare objects.
  static ResourceList list(AS400 sys, String shareName)
    throws ResourceException
  {
    return NetServerShare.list(sys, NetServerShare.PRINT_SHARE, shareName, openListAttributeMap_);
  }

}
