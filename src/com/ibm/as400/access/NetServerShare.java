///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: NetServerShare.java
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
 The NetServerShare class represents a NetServer share.

 @deprecated This class has been replaced by the
 {@link com.ibm.as400.access.ISeriesNetServerShare ISeriesNetServerShare}
 class and may be removed in a future release.
 @see NetServer#listFileShares
 @see NetServer#listPrintShares
**/

public abstract class NetServerShare
extends ChangeableResource
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


    static final long serialVersionUID = 4L;

  //-------------------------------------------------------------------------
  // Presentation.
  //-------------------------------------------------------------------------

  private static PresentationLoader   presentationLoader_ = new PresentationLoader("com.ibm.as400.access.MRI2");
  private static final String         ICON_BASE_NAME_     = "NetServerShare";
  private static final String         PRESENTATION_KEY_   = "NETSERVER";

  /*protected*/ static ResourceMetaDataTable attributes_ = new ResourceMetaDataTable(presentationLoader_, PRESENTATION_KEY_);

  // Attribute getter map.
  /*protected*/ static ProgramMap getterMap_    = new ProgramMap();
  // Note: The subclasses have their own setter maps.

  /*protected*/ static final String OLST0100_ = "qzlsolst_zlsl0100";
  static final int ZLSL0100_MAX_RECORD_LENGTH_ = 1188; // Max path length is 1024 bytes.

  /*protected*/ static final int[] INDICES_ = { 0 }; // For specifying first record in list.



  //-------------------------------------------------------------------------
  // Attribute IDs.
  //
  // * If you add an attribute here, make sure and add it to the class javadoc.
  //-------------------------------------------------------------------------

  /**
   Attribute ID for "description".  This identifies a String
   attribute, which represents the text description of a share.
   **/
  public static final String DESCRIPTION = "DESCRIPTION";
  static {
    attributes_.add(DESCRIPTION, String.class, "");
    getterMap_.add (DESCRIPTION, OLST0100_, "receiverVariable.description", INDICES_);
    // Note: The subclasses define their own setter maps for this attribute.
  }

  /**
   Attribute ID for "user count".  This identifies a read-only Integer attribute, which represents the number of connections that are currently made to a share.
   <br>Note: If the NetServer has not been started, this attribute's reported value will be -1.
   See {@link NetServer#isStarted() NetServer.isStarted()} and {@link NetServer#start() NetServer.start()}.
   **/
  public static final String USER_COUNT = "USER_COUNT";
  static {
    attributes_.add(USER_COUNT, Integer.class, true);
    getterMap_.add (USER_COUNT, OLST0100_, "receiverVariable.currentUsers", INDICES_);
 }


  //----------------------------------------------------------------------
  // PCML document initialization.
  //----------------------------------------------------------------------

  /*protected*/ static final String DOCUMENT_NAME_ = "com.ibm.as400.access.NetServer";
  /*protected*/ static ProgramCallDocument staticDocument_ = null;
  /*protected*/ ProgramCallDocument document_;

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

  // Values for "Device type" field for a share.
  /*protected*/ static final int FILE_SHARE    = 0;
  /*protected*/ static final int PRINT_SHARE   = 1;
  /*protected*/ static final int ALL_SHARES    = -1;  // accommodate future enhancement


  //----------------------------------------------------------------------
  // Private data.
  //----------------------------------------------------------------------

  // The name of the share.  Note: Uppercased on the server.
  private String name_;

  // The attributes that need to be set before other attributes are set.
  // Leave it null if no specific attributes need to be set before others.
  private Object[] attrsToSetFirst_;

  private ProgramAttributeGetter attributeGetter_;
  private ProgramAttributeSetter attributeSetter_; 



  /**
   Constructs a NetServerShare object.
   Note: This method is reserved for use by subclasses.
   **/
  /*protected*/ NetServerShare()
  {
    super(presentationLoader_.getPresentationWithIcon(PRESENTATION_KEY_, ICON_BASE_NAME_), null, attributes_);
  }


  /**
   Constructs a NetServerShare object.
   Note: This method is reserved for use by subclasses.
   @param system  The system.
   @param name  The name of the share.
   **/
  /*protected*/ NetServerShare(AS400 system, String name)
  {
    this();
    try { setSystem(system); } catch (PropertyVetoException e) {}
    setName(name);
  }

  /**
   Adds the server share to the NetServer.
   This method fires a resourceCreated() ResourceEvent.
   @exception ResourceException  If an error occurs.
   **/
  public abstract void add() throws ResourceException;


  /**
   Commits the specified attribute changes.
   <br>This method requires *IOSYSCFG special authority on the server.
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
  static Object computeResourceKey(AS400 system, String name)
  {
    StringBuffer buffer = new StringBuffer();
    buffer.append(NetServerShare.class);
    buffer.append(':');
    buffer.append(system.getSystemName());
    buffer.append(':');
    buffer.append(name); // Note: The share name uniquely identifies the share on the system.
    return buffer.toString();
  }


  /**
   Establishes the connection to the server.

   <p>The method is called by the resource framework automatically
   when the connection needs to be established.

   @exception ResourceException  If an error occurs.
   **/
  protected abstract void establishConnection()
    throws ResourceException;

  /**
   Establishes the connection to the server.

   @param initializeSetterFromSystem  Indicates whether or not the setter's values should be initialized from the system.  For example, this would be the case when establishing a connection to an existing share, rather than adding a new share.

   @exception ResourceException  If an error occurs.
   **/
  /*protected*/ abstract void establishConnection(boolean initializeSetterFromSystem)
    throws ResourceException;


  // Note: This method is reserved for use by the subclasses.
  /**
   Establishes the connection to the server.

   <p>The method is called by the resource framework automatically
   when the connection needs to be established.

   @param setterMap  The setter map (defined by the subclass).
   @param attributesToSetFirst  The attributes that must be set first (for example, attributes that specify lengths of other attributes).
   @param initializeSetterFromSystem  Indicates whether or not the setter's values should be initialized from the system.  For example, this would be the case when establishing a connection to an existing share, rather than adding a new share.

   @exception ResourceException  If an error occurs.
   **/
  /*protected*/ void establishConnection(ProgramMap setterMap, Object[] attributesToSetFirst, boolean initializeSetterFromSystem)
    throws ResourceException
  {
    // Verify that the share name has been set.
    if (name_ == null) {
      throw new ExtendedIllegalStateException("name",
                              ExtendedIllegalStateException.PROPERTY_NOT_SET);
    }

    // Call the superclass.
    super.establishConnection();

    attrsToSetFirst_ = attributesToSetFirst;

    // Initialize the PCML document.
    AS400 system = getSystem();
    if (document_ == null) {
      document_ = (ProgramCallDocument)staticDocument_.clone();
    }
    try {     
      document_.setSystem(system);
      document_.setValue("qzlsolst_zlsl0100.informationQualifier", name_);

      // Initialize the attribute getter.
      attributeGetter_ = new ProgramAttributeGetter(system, document_, getterMap_);

      // Note: The attribute setter map is initialized in the subclasses.

      // Initialize the attribute setter.
      attributeSetter_ = new ProgramAttributeSetter(getSystem(), document_, setterMap);
      if (initializeSetterFromSystem) {
        attributeSetter_.initializeAttributeValues(attributeGetter_, attrsToSetFirst_);
      }
    }
    catch (PcmlException e) {
      Trace.log(Trace.ERROR, "PcmlException when establishing connection to share.", e);
      throw new ResourceException(e);
    }
  }


  /**
   Freezes any property changes.  After this is called, property
   changes should not be made.  Properties are not the same thing
   as attributes.  Properties are basic pieces of information
   which must be set to make the object usable, such as the system,
   job name, job number, and user name.

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

    // Verify that the share name has been set.
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
    Object value = super.getAttributeUnchangedValue(attributeID);

    if (value == null) {
      if (! isConnectionEstablished()) {      // @A2M
        establishConnection();                // @A2M
      }
      value = attributeGetter_.getValue(attributeID);
    }
    return value;
  }

  /**
   Returns the network name of the share.
   <br>Note: All share names are uppercase on the server.
   @return The share name.
   **/
  public String getName()
  {
    return name_;
  }


  // Returns a list of NetServerShare objects.
  /*protected*/ static ResourceList list(AS400 system, int desiredShareType, String qualifier, ProgramMap openListAttributeMap)
    throws ResourceException
  {
    try
    {
      // Set the input parameters and call the API.
      ProgramCallDocument document = (ProgramCallDocument)staticDocument_.clone();
      document.setSystem(system);
      String programName = OLST0100_;
      document.setValue(programName+".informationQualifier", qualifier);  // @A1c

      int expectedInfoLength = 10*ZLSL0100_MAX_RECORD_LENGTH_; // Expect about 10 records.
      document.setIntValue(programName+".lengthOfReceiverVariable", expectedInfoLength);

      NetServer.callListProgram(document, programName, false);
        // Note: This method does a retry (with larger buffer) if receiverVariable is overflowed.

      int recCount = document.getIntValue(programName+".listInformation.recordsReturned");
      // Note: Since records are variable-length, recSize will be returned as zero.

      Vector shares = new Vector();
      String prefix = programName+".receiverVariable.";

      int index = 0;
      int[] indices = new int[1];
      Object[] attributeIDs = openListAttributeMap.getIDs();
      for (int i=0; i<recCount; i++)
      {
        indices[0] = i;

        int    size      = document.getIntValue(prefix+"lengthOfThisEntry", indices);
        String shareName = (String)document.getValue(prefix+"shareName", indices);
        int    shareType = document.getIntValue(prefix+"deviceType", indices);
        NetServerShare share = null;

        switch (shareType)
        {
          case FILE_SHARE:
            if (desiredShareType==shareType || desiredShareType==ALL_SHARES)
            {
              share = new NetServerFileShare(system, shareName);
            }
            break;
          case PRINT_SHARE:
            if (desiredShareType==shareType || desiredShareType==ALL_SHARES)
            {
              share = new NetServerPrintShare(system, shareName);
            }
            break;
          default:
            Trace.log(Trace.ERROR, "Bad value returned for shareType for share " + shareName + ": " + shareType);
            throw new InternalErrorException(InternalErrorException.UNEXPECTED_EXCEPTION);
        }

        if (share != null)
        {
          // Copy the information from the API record to the NetServerShare attributes.
          Object[] values = openListAttributeMap.getValues(attributeIDs, system, document, null, indices);
          for (int ii = 0; ii < values.length; ++ii) {
            share.initializeAttributeValue(attributeIDs[ii], values[ii]);
          }
          share.freezeProperties();
          shares.addElement(share);
        }

        index += size;
      }

      // Create a Presentation object to be associated with the list.
      Presentation presentation = presentationLoader_.getPresentationWithIcon(PRESENTATION_KEY_, ICON_BASE_NAME_);

      switch (desiredShareType)
      {
        case FILE_SHARE:
          NetServerFileShare[] fsArray = new NetServerFileShare[shares.size()];
          shares.copyInto(fsArray);
          return new ArrayResourceList(fsArray, presentation, getAttributeMetaDataStatic());
        case PRINT_SHARE:
          NetServerPrintShare[] psArray = new NetServerPrintShare[shares.size()];
          shares.copyInto(psArray);
          return new ArrayResourceList(psArray, presentation, getAttributeMetaDataStatic());
        case ALL_SHARES:
          NetServerShare[] array = new NetServerShare[shares.size()];
          shares.copyInto(array);
          return new ArrayResourceList(array, presentation, getAttributeMetaDataStatic());
        default:
          Trace.log(Trace.ERROR, "Bad value for 'desiredShareType': " + desiredShareType);
          throw new InternalErrorException(InternalErrorException.UNEXPECTED_EXCEPTION);
      }
    }
    catch (PcmlException e) {
      Trace.log(Trace.ERROR, "PcmlException when listing shares.", e);
      throw new ResourceException(e);
    }
  }


  /**
   Lists the connections currently associated with this share.
   The returned ResourceList contains {@link NetServerConnection NetServerConnection} objects.
   @return  The current connections to the share.

   @exception ResourceException  If an error occurs.
   **/
  public ResourceList listConnections()
    throws ResourceException
  {
    if (! isConnectionEstablished()) {
      establishConnection();
    }

    return NetServerConnection.listConnectionsForShare(getSystem(), name_);
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

    attributeGetter_.clearBuffer();
    attributeSetter_.initializeAttributeValues(attributeGetter_, attrsToSetFirst_);

    super.refreshAttributeValues();
  }


  /**
   Removes this share from the NetServer.
   <br>To use this method, the user profile must either have *IOSYSCFG special authority, or own the integrated file system directory or output queue that the share references.
   <br>This method fires a resourceDeleted() ResourceEvent.

   @exception ResourceException  If an error occurs.
   **/
  public void remove()
    throws ResourceException
  {
    if (! isConnectionEstablished()) {
      establishConnection(false);
    }

    remove(getSystem(), name_);
    fireResourceDeleted();
  }


  /**
   Removes a share from the NetServer.

   @exception ResourceException  If an error occurs.
   **/
  static void remove(AS400 system, String shareName)
    throws ResourceException
  {
    // Set the input parameters and call the API.
    try
    {
      ProgramCallDocument document = (ProgramCallDocument)staticDocument_.clone();
      document.setSystem(system);
      document.setValue("qzlsrms.shareName", shareName);

      if (document.callProgram("qzlsrms") == false) {
        throw new ResourceException(document.getMessageList("qzlsrms"));
      }
    }
    catch (PcmlException e) {
      Trace.log(Trace.ERROR, "PcmlException when removing a share.", e);
      throw new ResourceException(e);
    }
  }

  /**
   Sets the network name of the share.
   <br>Note: All share names are uppercase on the server.
   Share names are limited to 12 characters in length. 
   This method fires a PropertyChangeEvent.

   @param name  The name of the share.
   **/
  public void setName(String name)
  {
    if (name == null) {
      throw new NullPointerException("name");
    }
    if (arePropertiesFrozen()) {
      throw new ExtendedIllegalStateException("propertiesFrozen", ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
    }

    String oldValue = name_;
    name_ = name;
    firePropertyChange("name", oldValue, name);
  }

}
