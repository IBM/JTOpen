///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  UserSpace.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2004 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;
import java.io.CharConversionException;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Vector;

/**
 Represents a user space object in the IBM i operating system.
 <p>Usage note: By default, the UserSpace class will make use of two different host servers.  
    For performance reasons, the File Server will be used for read() and write() requests, and
    the Remote Command Host Server will be used for create() and other requests.  
    This behavior can be changed with the {@link #setMustUseProgramCall setMustUseProgramCall()} method.
 <p>Similarly, when running directly on IBM i, by default the UserSpace class will call internal 
    API's and commands on-thread when possible, bypassing the Remote Command Host Server.  
    This behavior can be changed with the {@link #setMustUseSockets setMustUseSockets()} method.
 <p>As a performance optimization, when running directly on IBM i, it is possible to use native
    methods to access the user space from the current job.  To enable this support, use the 
    {@link #setMustUseNativeMethods setMustUseNativeMethods()} method. 
 <p>For applications that access user spaces located in <tt>QTEMP</tt>, users are strongly advised 
    to keep everything in the same job, by calling <tt>setMustUseProgramCall(true)</tt> and 
    <tt>setMustUseSockets(true)</tt>, since different jobs have different QTEMP libraries. 
 **/
public class UserSpace implements Serializable
{
    private static final String CLASSNAME = "com.ibm.as400.access.UserSpace";
    static
    {
        if (Trace.traceOn_) Trace.logLoadPath(CLASSNAME);
    }

    static final long serialVersionUID = 4L;

    /**
     Force to auxiliary storage option that allows changes to be forced asynchronously.
     **/
    public static final int FORCE_ASYNCHRONOUS = 1;

    /**
     Force to auxiliary storage option that does not allow changes to be forced.  It uses normal system writes.
     **/
    public static final int FORCE_NONE = 0;

    /**
     Force to auxiliary storage option that allows changes to be forced synchronously.
     **/
    public static final int FORCE_SYNCHRONOUS = 2;

    /**
     Constant representing the default domain for the user space.  The QALWUSRDMN system value is used to determine the domain.
     **/
    public static final String DOMAIN_DEFAULT = "*DEFAULT";

    /** 
     Constant indicating the domain for the user space is *USER.
     **/
    public static final String DOMAIN_USER = "*USER";

    /**
     Constant indicating the domain for the user space is *SYSTEM.
     **/
    public static final String DOMAIN_SYSTEM = "*SYSTEM";

    // Maximum allowed user space size.
    private static final int MAX_USER_SPACE_SIZE = 16776704;

    // The system where the user space is located.
    private AS400 system_ = null;
    // The full IFS path name of the user space.
    private String path_ = "";
    // The library that contains the user space.
    private String library_ = "";
    // The name of the user space.
    private String name_ = "";
    // Use ProgramCall instead of IFS.
    private boolean mustUseProgramCall_ = false;
    // Use sockets instead of native methods when running natively.
    private boolean mustUseSockets_ = false;
    // Use native methods when running natively; 
    private boolean mustUseNativeMethods_ = false; 

    // Data converter for reads and writes with string objects.
    private transient Converter dataConverter_ = null;

    // Implementation object interacts with system or native methods.
    private transient UserSpaceImpl impl_ = null;

    private transient UserSpaceNativeReadWriteImpl nativeReadWriteImpl_ = null; 
    // List of user space event bean listeners.
    private transient Vector userSpaceListeners_ = null;  // Set on first add.
    // List of property change event bean listeners.
    private transient PropertyChangeSupport propertyChangeListeners_ = null;  // Set on first add.
    // List of vetoable change event bean listeners.
    private transient VetoableChangeSupport vetoableChangeListeners_ = null;  // Set on first add.

    /**
     Constructs a UserSpace object.  The <i>system</i> and <i>path</i> properties must be set before using any method requiring a connection to the system.
     **/
    public UserSpace()
    {
        super();
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Constructing UserSpace object.");
    }

    /**
     Constructs a UserSpace object.  It creates a UserSpace instance that represents the user space <i>path</i> on <i>system</i>.
     @param  system  The system that contains the user space.
     @param  path  The fully qualified integrated file system path name to the user space.
     **/
    public UserSpace(AS400 system, String path)
    {
        super();
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Constructing UserSpace object, system: " + system + " path: " + path);
        // Validate arguments.
        if (system == null) {
            throw new NullPointerException("system");
        }
        if (path == null) {
            throw new NullPointerException("path");
        }
        // Verify path is valid IFS path name.
        QSYSObjectPathName ifs = new QSYSObjectPathName(path, "USRSPC");

        // Set instance variables.
        system_ = system;
        path_ = path;
        library_ = ifs.getLibraryName();
        name_ = ifs.getObjectName();
    }

    /**
     Adds a listener to be notified when the value of any bound property is changed.  It can be removed with removePropertyChangeListener().
     @param  listener  The listener object.
     **/
    public void addPropertyChangeListener(PropertyChangeListener listener)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Adding property change listener.");
        if (listener == null) {
            throw new NullPointerException("listener");
        }
        // If first add.
        if (propertyChangeListeners_ == null)
        {
          synchronized (this)
          {
            if (propertyChangeListeners_ == null)
            {
              propertyChangeListeners_ = new PropertyChangeSupport(this);
            }
          }
        }
        propertyChangeListeners_.addPropertyChangeListener(listener);
    }

    /**
     Adds a listener to be notified when a UserSpaceEvent is fired.
     @param  listener  The listener object.
     **/
    public void addUserSpaceListener(UserSpaceListener listener)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Adding user space listener.");
        if (listener == null) {
            throw new NullPointerException("listener");
        }
        // If first add.
        if (userSpaceListeners_ == null)
        {
          synchronized (this)
          {
            if (userSpaceListeners_ == null)
            {
              userSpaceListeners_ = new Vector();
            }
          }
        }
        userSpaceListeners_.addElement(listener);
    }

    /**
     Adds a listener to be notified when the value of any constrained property is changed.
     @param  listener  The listener object.
     **/
    public void addVetoableChangeListener(VetoableChangeListener listener)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Adding vetoable change listener.");
        if (listener == null) {
            throw new NullPointerException("listener");
        }
        // If first add.
        if (vetoableChangeListeners_ == null)
        {
          synchronized (this)
          {
            if (vetoableChangeListeners_ == null)
            {
              vetoableChangeListeners_ = new VetoableChangeSupport(this);
            }
          }
        }
        vetoableChangeListeners_.addVetoableChangeListener(listener);
    }

    // Determines the type of implementation that will be used.  Properties system, path, and  mustUseProgramCall are committed at this time.
    private synchronized void chooseImpl() throws AS400SecurityException, IOException
    {
        if (system_ != null) system_.signon(false);
        if (impl_ == null)
        {
            // Verify required attributes have been set.
            if (system_ == null)
            {
                Trace.log(Trace.ERROR, "Cannot connect to the server before setting system.");
                throw new ExtendedIllegalStateException("system", ExtendedIllegalStateException.PROPERTY_NOT_SET);
            }
            if (path_.length() == 0)
            {
                Trace.log(Trace.ERROR, "Cannot connect to the server before setting path.");
                throw new ExtendedIllegalStateException("path", ExtendedIllegalStateException.PROPERTY_NOT_SET);
            }

            if(mustUseSockets_)
                impl_ = (com.ibm.as400.access.UserSpaceImpl)system_.loadImpl2("com.ibm.as400.access.UserSpaceImplRemote", "com.ibm.as400.access.UserSpaceImplProxy");
            else
                impl_ = (com.ibm.as400.access.UserSpaceImpl)system_.loadImpl3("com.ibm.as400.access.UserSpaceImplNative", "com.ibm.as400.access.UserSpaceImplRemote", "com.ibm.as400.access.UserSpaceImplProxy");

            // Set the fixed properties in the implementation object.
            impl_.setProperties(system_.getImpl(), path_, name_, library_, mustUseProgramCall_, mustUseSockets_);
        }
    }

    /**
     Closes the user space and releases any system resources associated with the stream.
     This will not close the connection to the Host Server job held by the associated AS400 object.
     Note: Closing the user space does not delete it.  It simply closes this UserSpace object's file stream connection to the user space.
     * @throws  IOException  If an error occurs while communicating with the system.
     @see #delete
     **/
    public synchronized void close() throws IOException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Closing user space.");
        if (impl_ != null) impl_.close();
    }

    /**
     Creates the user space.
     @param  length  The initial size (in bytes) of the user space.  Valid values are 1 through 16,776,704.  User spaces with lengths of 16,773,120 or less will be created with optimum space alignment.  These user spaces can not be resized to greater than 16,773,120 bytes.  For performance, lengths of 16,773,120 or less are recommended.
     @param  replace  The value indicating if an existing user space is to be replaced.
     @param  extendedAttribute  The user-defined extended attribute of the user space.  This string must be 10 characters or less.
     @param  initialValue  The value used in creation and extension.
     @param  textDescription  The text describing the user space.  This string must be 50 characters or less.
     @param  authority  The public authority for the user space.  This string must be 10 characters or less.  Valid values are:
     <ul>
     <li>*ALL
     <li>*CHANGE
     <li>*EXCLUDE
     <li>*LIBCRTAUT
     <li>*USE
     <li>authorization-list name
     </ul>
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public void create(int length, boolean replace, String extendedAttribute, byte initialValue, String textDescription, String authority) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        // Create with domain "*DEFAULT"
        create(new byte[] { 0x5C, (byte)0xC4, (byte)0xC5, (byte)0xC6, (byte)0xC1, (byte)0xE4, (byte)0xD3, (byte)0xE3, (byte)0x40, (byte)0x40 }, length, replace, extendedAttribute, initialValue, textDescription, authority);
    }

    /**
     Creates the user space.
     @param  domain  The domain into which the user space is created.  Valid value are:
     <ul>
     <li>{@link #DOMAIN_DEFAULT DOMAIN_DEFAULT}
     <li>{@link #DOMAIN_USER DOMAIN_USER}
     <li>{@link #DOMAIN_SYSTEM DOMAIN_SYSTEM}
     </ul>
     @param  length  The initial size (in bytes) of the user space.  Valid values are 1 through 16,776,704.  User spaces with lengths of 16,773,120 or less will be created with optimum space alignment.  These user spaces can not be resized to greater than 16,773,120 bytes.  For performance, lengths of 16,773,120 or less are recommended.
     @param  replace  The value indicating if an existing user space is to be replaced.
     @param  extendedAttribute  The user-defined extended attribute of the user space.  This string must be 10 characters or less.
     @param  initialValue  The value used in creation and extension.
     @param  textDescription  The text describing the user space.  This string must be 50 characters or less.
     @param  authority  The public authority for the user space.  This string must be 10 characters or less.  Valid values are:
     <ul>
     <li>*ALL
     <li>*CHANGE
     <li>*EXCLUDE
     <li>*LIBCRTAUT
     <li>*USE
     <li>authorization-list name
     </ul>
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public void create(String domain, int length, boolean replace, String extendedAttribute, byte initialValue, String textDescription, String authority) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        // Validate the domain parameter.
        if (domain == null) {
            throw new NullPointerException("domain");
        }
        byte[] domainBytes;
        if (domain.equals(DOMAIN_DEFAULT))
        {
            // EBCDIC "*DEFAULT"
            domainBytes = new byte[] { 0x5C, (byte)0xC4, (byte)0xC5, (byte)0xC6, (byte)0xC1, (byte)0xE4, (byte)0xD3, (byte)0xE3, (byte)0x40, (byte)0x40 };
        }
        else if (domain.equals(DOMAIN_USER))
        {
            // EBCDIC "*USER"
            domainBytes = new byte[] { 0x5C, (byte)0xE4, (byte)0xE2, (byte)0xC5, (byte)0xD9, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40 };
        }
        else if (domain.equals(DOMAIN_SYSTEM))
        {
            // EBCDIC "*SYSTEM"
            domainBytes = new byte[] { 0x5C, (byte)0xE2, (byte)0xE8, (byte)0xE2, (byte)0xE3, (byte)0xC5, (byte)0xD4, (byte)0x40, (byte)0x40, (byte)0x40 };
        }
        else
        {
            throw new ExtendedIllegalArgumentException("domain (" + domain + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }
        create(domainBytes, length, replace, extendedAttribute, initialValue, textDescription, authority);
    }

    // Creates the user space.
    private void create(byte[] domainBytes, int length, boolean replace, String extendedAttribute, byte initialValue, String textDescription, String authority) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Creating user space.");
        // Validate the length parameter.
        if (length < 1 || length > MAX_USER_SPACE_SIZE)
        {
            throw new ExtendedIllegalArgumentException("length (" + length + ")", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
        }
        // Validate the extended attribute parameter.
        if (extendedAttribute == null) {
            throw new NullPointerException("extendedAttribute");
        }
        if (extendedAttribute.length() == 0) extendedAttribute = " ";
        if (extendedAttribute.length() > 10)
        {
            throw new ExtendedIllegalArgumentException("extendedAttribute (" + extendedAttribute + ")", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
        }
        // Validate the text description parameter.
        if (textDescription == null) {
            throw new NullPointerException("textDescription");
        }
        if (textDescription.length() == 0) textDescription = " ";
        if (textDescription.length() > 50)
        {
            throw new ExtendedIllegalArgumentException("textDescription (" + textDescription + ")", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
        }
        // Validate the authority parameter.
        if (authority == null) {
            throw new NullPointerException("authority");
        }
        if (authority.length() == 0 || authority.length() > 10)
        {
            throw new ExtendedIllegalArgumentException("authority (" + authority + ")", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
        }

        chooseImpl();
        impl_.create(domainBytes, length, replace, extendedAttribute, initialValue, textDescription, authority);

        // Fire the CREATED event.
        if (userSpaceListeners_ != null) fireUserSpaceEvent(UserSpaceEvent.US_CREATED);
    }

    /**
     Deletes the user space.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public void delete() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Deleting user space.");
        chooseImpl();
        impl_.delete();

        // Fire the DELETED event.
        if (userSpaceListeners_ != null) fireUserSpaceEvent(UserSpaceEvent.US_DELETED);
    }

    /**
     Determines if the user space exists.
     @return  true if the user space exists; false otherwise.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     **/
    public boolean exists() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Determining user space existence.");
        try
        {
            chooseImpl();
            impl_.getLength();
            return true;
        }
        catch (ObjectDoesNotExistException e)
        {
            return false;
        }
    }

    // Fires the user space events.
    private void fireUserSpaceEvent(int eventType)
    {
        Vector targets = (Vector)userSpaceListeners_.clone();
        UserSpaceEvent event = new UserSpaceEvent(this, eventType);
        for (int i = 0; i < targets.size(); ++i)
        {
            UserSpaceListener target = (UserSpaceListener)targets.elementAt(i);
            switch (eventType)
            {
                case UserSpaceEvent.US_CREATED:
                    target.created(event);
                    break;
                case UserSpaceEvent.US_DELETED:
                    target.deleted(event);
                    break;
                case UserSpaceEvent.US_READ:
                    target.read(event);
                    break;
                case UserSpaceEvent.US_WRITTEN:
                    target.written(event);
                    break;
            }
        }
    }

    /**
     Returns the initial value used for filling in the user space during creation and extension.
     @return  The initial value used during user space creation and extension.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public byte getInitialValue() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Retrieving user space initial value.");
        chooseImpl();
        return impl_.getInitialValue();
    }

    /**
     Returns the size (in bytes) of the user space.
     @return  The size (in bytes) of the user space.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public int getLength() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Retrieving user space length.");
        chooseImpl();
        return impl_.getLength();
    }

    /**
     Returns the user space name.
     @return  The name of the user space.
     **/
    public String getName()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting user space name: " + name_);
        return name_;
    }

    /**
     Returns the integrated file system path name of the object represented by the user space.
     @return  The integrated file system path name of the object represented by the user space.
     **/
    public String getPath()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting user space path: " + path_);
        return path_;
    }

    /**
     Returns the system object for this user space.
     @return  The system object for this user space.
     **/
    public AS400 getSystem()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting system: " + system_);
        return system_;
    }

    /**
     Indicates if the user space is auto extendible.  When running on a system other than the system that contains the user space, the auto extend attribute is always true and cannot be changed, so the attribute value returned should be ignored.  The auto extend attribute can be used when running on the same system as the user space with the optimizations that are a part of the operating system.
     @return  true if the user space is auto extendible; false otherwise.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public boolean isAutoExtendible() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Retrieving user space auto extendibility.");
        chooseImpl();
        return impl_.isAutoExtendible();
    }

    /**
     Indicates if the Toolbox ProgramCall class will be used internally to perform user space read and write requests.  If false, Toolbox Integrated File System classes will be used to perform user space read and write requests.
     @return  true if user space read and write requests will be performed via program call; false otherwise.
     @see #setMustUseProgramCall
     **/
    public boolean isMustUseProgramCall()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Checking if user space must use program call:", mustUseProgramCall_);
        return mustUseProgramCall_;
    }

    /**
    Indicates if the native methods will be used internally to perform user space read and write requests.  
    If false, either ProgramCall or Toolbox Integrated File System classes will be used to perform 
    user space read and write requests.
    @return  true if user space read and write requests will be performed via native methods; false otherwise.
    @see #setMustUseNativeMethods
    **/
    
    public boolean isMustUseNativeMethods()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Checking if user space must use native methods:", mustUseNativeMethods_);
        return mustUseNativeMethods_;
    }

    /**
     Reads up to <i>dataBuffer.length</i> bytes from the user space beginning at <i>userSpaceOffset</i> into <i>dataBuffer</i>.
     @param  dataBuffer  The buffer to fill with data.  Buffer.length() bytes will be read from the user space.
     @param  userSpaceOffset  The offset in the user space from which to start reading.
     @return  The total number of bytes read into the buffer, or -1 if the <i>userSpaceOffset</i> is beyond the end of the user space.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public int read(byte[] dataBuffer, int userSpaceOffset) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        // Validate the data buffer parameter.
        if (dataBuffer == null) {
            throw new NullPointerException("dataBuffer");
        }

        return read(dataBuffer, userSpaceOffset, 0, dataBuffer.length);
    }

    /**
     Reads up to <i>length</i> bytes from the user space beginning at <i>userSpaceOffset</i> into <i>dataBuffer</i> beginning at <i>dataOffset</i>.
     @param  dataBuffer  The buffer to fill with data.
     @param  userSpaceOffset  The offset in the user space from which to start reading.
     @param  dataOffset  The starting offset in the data buffer for the results of the read.
     @param  length  The number of bytes to read.
     @return  The total number of bytes read into the buffer, or -1 if the <i>userSpaceOffset</i> is beyond the end of the user space.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public int read(byte[] dataBuffer, int userSpaceOffset, int dataOffset, int length) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        // Validate the data buffer parameter.
        if (dataBuffer == null) {
            throw new NullPointerException("dataBuffer");
        }
        if (dataBuffer.length == 0)
        {
            throw new ExtendedIllegalArgumentException("dataBuffer.length (" + dataBuffer.length + ")", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
        }
        // Validate the user space offset parameter.
        if (userSpaceOffset < 0 || userSpaceOffset > MAX_USER_SPACE_SIZE)
        {
            throw new ExtendedIllegalArgumentException("userSpaceOffset (" + userSpaceOffset + ")", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
        }

        // Validate the data offset parameter.
        if (dataOffset < 0 || dataOffset >= dataBuffer.length)
        {
            throw new ExtendedIllegalArgumentException("dataOffset (" + dataOffset + ")", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
        }
        // Validate the length parameter.
        if (length < 0 || length > (dataBuffer.length - dataOffset))
        {
            throw new ExtendedIllegalArgumentException("length (" + length + ")", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
        }

        // Handle a read of 0 bytes here.
        if (length == 0) return 0;

        // Do the read.
        int bytesRead;
        if (nativeReadWriteImpl_ != null) { 
            bytesRead = nativeReadWriteImpl_.read(dataBuffer, userSpaceOffset, dataOffset, length);
        } else { 
            chooseImpl();
            bytesRead = impl_.read(dataBuffer, userSpaceOffset, dataOffset, length);
        }
        // Fire the READ event.
        if (userSpaceListeners_ != null) fireUserSpaceEvent(UserSpaceEvent.US_READ);

        return bytesRead;
    }

    /**
     Returns a string from the user space beginning at <i>userSpaceOffset</i>.  Data is read from the user space as if by the read(byte[], int, int, int) method.  The resulting byte array is then converted into a String.
     @param  userSpaceOffset  The offset in the user space from which to start reading.
     @param  length  The number of bytes to read.
     @return  The string value from the user space.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public String read(int userSpaceOffset, int length) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        // Validate the length parameter.
        if (length <= 0)
        {
            throw new ExtendedIllegalArgumentException("length (" + length + ")", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
        }

        // Do the read.
        byte[] dataBuffer = new byte[length];
        read(dataBuffer, userSpaceOffset, 0, length);
        if (dataConverter_ == null)
        {
          synchronized (this)
          {
            if (dataConverter_ == null)
            {
              dataConverter_ = new Converter(system_.getCcsid(), system_);
            }
          }
        }
        return dataConverter_.byteArrayToString(dataBuffer);
    }

    /**
     Removes this listener from being notified when a bound property changes.
     @param  listener  The listener object.
     **/
    public void removePropertyChangeListener(PropertyChangeListener listener)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Removing property change listener.");
        if (listener == null) {
            throw new NullPointerException("listener");
        }
        // If we have listeners.
        if (propertyChangeListeners_ != null)
        {
            propertyChangeListeners_.removePropertyChangeListener(listener);
        }
    }

    /**
     Removes a listener from the UserSpace listeners list.
     @param  listener  The listener object.
     **/
    public void removeUserSpaceListener(UserSpaceListener listener)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Removing user space listener.");
        if (listener == null) {
            throw new NullPointerException("listener");
        }
        // If we have listeners.
        if (userSpaceListeners_ != null)
        {
            userSpaceListeners_.removeElement(listener);
        }
    }

    /**
     Removes this listener from being notified when a constrained property changes.
     @param  listener  The listener object.
     **/
    public void removeVetoableChangeListener(VetoableChangeListener listener)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Removing vetoable change listener.");
        if (listener == null) {
            throw new NullPointerException("listener");
        }
        // If we have listeners.
        if (vetoableChangeListeners_ != null)
        {
            vetoableChangeListeners_.removeVetoableChangeListener(listener);
        }
    }

    /**
     Sets the auto extend attribute if possible.  When running on a system other than the system that contains the user space, the auto extend attribute cannot be set, so this method is ignored and auto extend is always true.  Auto extend can be set when running on the same system as the user space with the optimizations that are a part of the operating system.
     @param  autoExtendibility  The attribute for user space auto extendibility.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public void setAutoExtendible(boolean autoExtendibility) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        chooseImpl();
        impl_.setAutoExtendible(autoExtendibility);
    }

    /**
     Sets the initial value to be used during user space creation or extension.
     @param  initialValue  The new initial value used during future extensions.  For best performance, set to zero.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public void setInitialValue(byte initialValue) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        chooseImpl();
        impl_.setInitialValue(initialValue);
    }

    /**
     Sets the size (in bytes) of the user space.
     @param  length  The new size (in bytes) of the user space.  Valid values are 1 through 16,776,704.  User spaces with lengths of 16,773,120 or less, created with optimum space alignment,  can not be resized to greater than 16,773,120 bytes.  For performance, lengths of 16,773,120 or less are recommended.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public void setLength(int length) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        // Validate the length parameter.
        if (length < 1 || length > MAX_USER_SPACE_SIZE)
        {
            throw new ExtendedIllegalArgumentException("length (" + length + ")", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
        }

        chooseImpl();
        impl_.setLength(length);
    }

    /**
     Specifies the API set that is used to perform user space read and write operations.
     This method is useful when using ProgramCall or CommandCall objects in conjunction with user spaces.
     If false (the default), read and write requests are made via the File Server;  internally, an IFSRandomAccessFile object is used to perform read and write requests.  If true, internally a ProgramCall object is used to perform read and write requests, which are made via the Remote Command Host Server.  In general, requests made via the File object are faster, but the behavior of requests made via a ProgramCall object is more consistent with user space API's.
     <p>If accessing user spaces located in QTEMP, it is strongly advised that <tt>setMustUseProgramCall(true)</tt> be called.  In addition, depending on whether subsequent accesses of the user space (such as by program calls) will be run on- or off-thread, you should also call {@link #setMustUseSockets setMustUseSockets(true)}.
     <p>This property cannot be reset once a connection has been established.
     @param  useProgramCall  Internally use ProgramCall to perform read and write requests.
     @see #isMustUseProgramCall
     **/
    public void setMustUseProgramCall(boolean useProgramCall)
    {
        // Verify that connection has not been made.
        if (impl_ != null)
        {
            Trace.log(Trace.ERROR, "Cannot set property 'mustUseProgramCall' after connect.");
            throw new ExtendedIllegalStateException("mustUseProgramCall", ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
        }

        if (propertyChangeListeners_ == null)
        {
            mustUseProgramCall_ = useProgramCall;
        }
        else
        {
            Boolean oldValue = new Boolean(mustUseProgramCall_);
            Boolean newValue = new Boolean(useProgramCall);

            mustUseProgramCall_ = useProgramCall;

            // Fire the property change event.
            propertyChangeListeners_.firePropertyChange("mustUseProgramCall", oldValue, newValue);
        }
    }

    
    /**
    Specifies whether native methods are used by the current to perform user space read and write operations.
    This option can only be set to true when the application is running on the System i.  
    This method may only be called after the path to the user space is set. 
    <p>It is not advised to use native methods to access user spaces in QTEMP because toolbox does not
    provide a method to create a user space in QTEMP of the current job.  
    <p>The programmer must be aware that no locking occurs when native methods are used to read and write from the 
    user space.   If a user space is being used by multiple threads or processes, the values read may reflect a partial
    operation performed by another thread or process. 
    @param  useNativeMethods  Internally use ProgramCall to perform read and write requests.
     * @throws CharConversionException 
     * @throws UnsupportedEncodingException If the Character Encoding is not supported. 
     * @throws SecurityException If a security error occurs.
     * @throws UnsatisfiedLinkError  If link cannot be satisfied.
    @see #isMustUseNativeMethods
    **/
   public void setMustUseNativeMethods(boolean useNativeMethods) throws 
   UnsupportedEncodingException, 
   CharConversionException, 
   UnsatisfiedLinkError,
   SecurityException
   {

       if (propertyChangeListeners_ == null)
       {
           mustUseNativeMethods_ = useNativeMethods;
       }
       else
       {
           Boolean oldValue = new Boolean(mustUseNativeMethods_);
           Boolean newValue = new Boolean(useNativeMethods);

           mustUseNativeMethods_ = useNativeMethods;

           // Fire the property change event.
           propertyChangeListeners_.firePropertyChange("mustUseNativeMethods", oldValue, newValue);
       }
       if (useNativeMethods) {
    	   // Only open it not already set. 
    	   if (nativeReadWriteImpl_ == null) {
    		   // For now assume ILE methods, in the future we may need to choose PASE methods  
    		   nativeReadWriteImpl_ = new UserSpaceNativeReadWriteImplILE(system_);
    		   nativeReadWriteImpl_.open(library_, name_); 
    	   }
       } else {
    	   if (nativeReadWriteImpl_ != null) { 
    		   nativeReadWriteImpl_.close(); 
    		   nativeReadWriteImpl_ = null; 
    	   }
       }
   }


    
    /**
     Sets the path for the user space.  The path can only be set before a connection has been established.
     @param  path  The fully qualified integrated file system path name to the user space.
     @exception  PropertyVetoException  If the change is vetoed.
     **/
    public void setPath(String path) throws PropertyVetoException
    {
        // Check parameter.
        if (path == null) {
            throw new NullPointerException("path");
        }
        // Verify path is valid IFS path name.
        QSYSObjectPathName ifs = new QSYSObjectPathName(path, "USRSPC");

        if (impl_ != null)
        {
            Trace.log(Trace.ERROR, "Cannot set property 'path' after connect.");
            throw new ExtendedIllegalStateException("path", ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
        }

        if (propertyChangeListeners_ == null && vetoableChangeListeners_ == null)
        {
            library_ = ifs.getLibraryName();
            name_ = ifs.getObjectName();
            path_ = path;
        }
        else
        {
            String oldValue = path_;
            String newValue = path;

            if (vetoableChangeListeners_ != null)
            {
                vetoableChangeListeners_.fireVetoableChange("path", oldValue, newValue);
            }

            library_ = ifs.getLibraryName();
            name_ = ifs.getObjectName();
            path_ = newValue;

            if (propertyChangeListeners_ != null)
            {
                propertyChangeListeners_.firePropertyChange("path", oldValue, newValue);
            }
        }
    }

    /**
     Sets the system object for the user space.  The system can only be set before a connection has been established.
     @param  system  The system that contains the user space.
     @exception  PropertyVetoException  If the change is vetoed.
     **/
    public void setSystem(AS400 system) throws PropertyVetoException
    {
        // Check parameter.
        if (system == null) {
            throw new NullPointerException("system");
        }
        // Verify that connection has not been made.
        if (impl_ != null)
        {
            Trace.log(Trace.ERROR, "Cannot set property 'system' after connect.");
            throw new ExtendedIllegalStateException("system", ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
        }

        if (propertyChangeListeners_ == null && vetoableChangeListeners_ == null)
        {
            system_ = system;
        }
        else
        {
            AS400 oldValue = system_;
            AS400 newValue = system;

            if (vetoableChangeListeners_ != null)
            {
                vetoableChangeListeners_.fireVetoableChange("system", oldValue, newValue);
            }
            system_ = newValue;
            if (propertyChangeListeners_ != null)
            {
                propertyChangeListeners_.firePropertyChange("system", oldValue, newValue);
            }
        }
    }

    /**
     Writes up to <i>dataBuffer.length</i> bytes from <i>dataBuffer</i> into the user space beginning at <i>userSpaceOffset</i>.
     @param  dataBuffer  The data buffer to be written to the user space.
     @param  userSpaceOffset  The position in the user space to start writing.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public void write(byte[] dataBuffer, int userSpaceOffset) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        // Validate the data buffer parameter.
        if (dataBuffer == null) {
            throw new NullPointerException("dataBuffer");
        }
        write(dataBuffer, userSpaceOffset, 0, dataBuffer.length, FORCE_NONE);
    }

    /**
     Writes up to <i>length</i> bytes from <i>dataBuffer</i> beginning at <i>dataOffset</i> into the user space beginning at <i>userSpaceOffset</i>.
     @param  dataBuffer  The data buffer to be written to the user space.
     @param  userSpaceOffset  The position in the user space to start writing.
     @param  dataOffset  The position in the write data buffer from which to start copying.
     @param  length  The length (in bytes) of data to be written.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public void write(byte[] dataBuffer, int userSpaceOffset, int dataOffset, int length) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        write(dataBuffer, userSpaceOffset, dataOffset, length, FORCE_NONE);
    }

    /**
     Writes up to <i>length</i> bytes from <i>dataBuffer</i> beginning at <i>dataOffset</i> into the user space beginning at <i>userSpaceOffset</i>.
     @param  dataBuffer  The data buffer to be written to the user space.
     @param  userSpaceOffset  The position in the user space to start writing.
     @param  dataOffset  The position in the write data buffer from which to start copying.
     @param  length  The length (in bytes) of data to be written.
     @param  forceAuxiliary  The method of forcing changes made to the user space to auxiliary storage.  Valid values are:
     <ul>
     <li>FORCE_NONE
     <li>FORCE_ASYNCHRONOUS
     <li>FORCE_SYNCHRONOUS
     </ul>
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public void write(byte[] dataBuffer, int userSpaceOffset, int dataOffset, int length, int forceAuxiliary) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        // Validate the data buffer parameter.
        if (dataBuffer == null) {
            throw new NullPointerException("dataBuffer");
        }
        if (dataBuffer.length == 0)
        {
            throw new ExtendedIllegalArgumentException("dataBuffer.length (" + dataBuffer.length + ")", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
        }
        // Validate the user space offset parameter.
        if (userSpaceOffset < 0 || userSpaceOffset > MAX_USER_SPACE_SIZE)
        {
            throw new ExtendedIllegalArgumentException("userSpaceOffset (" + userSpaceOffset + ")", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
        }
        // Validate the data offset parameter.
        if (dataOffset < 0 || dataOffset >= dataBuffer.length)
        {
            throw new ExtendedIllegalArgumentException("dataOffset (" + dataOffset + ")", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
        }
        // Validate the length parameter.
        if (length < 0 || length > (dataBuffer.length - dataOffset))
        {
            throw new ExtendedIllegalArgumentException("length (" + length + ")", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
        }
        // Validate the overflow possibility.
        if (userSpaceOffset + length >= MAX_USER_SPACE_SIZE)
        {
            Trace.log(Trace.ERROR, "Request is not supported, causes space overflow.");
            throw new ExtendedIllegalArgumentException("userSpaceOffset + length (" + userSpaceOffset + " + " + length + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }
        // Validate the force auxiliary parameter.
        if (forceAuxiliary < 0 || forceAuxiliary > 2)
        {
            throw new ExtendedIllegalArgumentException("forceAuxiliary (" + forceAuxiliary + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }

        if    (nativeReadWriteImpl_ != null) { 
	        nativeReadWriteImpl_.write(dataBuffer, userSpaceOffset, dataOffset, length, forceAuxiliary);
        } else { 
            chooseImpl();
	        impl_.write(dataBuffer, userSpaceOffset, dataOffset, length, forceAuxiliary);
        }

        // Fire the WRITTEN event.
        if (userSpaceListeners_ != null) fireUserSpaceEvent(UserSpaceEvent.US_WRITTEN);
    }

    /**
     Writes a string into the user space beginning at <i>userSpaceOffset</i>.  String is converted into bytes and written to the user space as if by the write(byte[], int) method.
     @param  data  The data buffer to be written to the user space.
     @param  userSpaceOffset  The position in the user space to start writing.
     @exception  AS400SecurityException  If a security or authority error occurs.
     @exception  ErrorCompletingRequestException  If an error occurs before the request is completed.
     @exception  InterruptedException  If this thread is interrupted.
     @exception  IOException  If an error occurs while communicating with the system.
     @exception  ObjectDoesNotExistException  If the object does not exist on the system.
     **/
    public void write(String data, int userSpaceOffset) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
    {
        // Validate the data parameter.
        if (data == null) {
            throw new NullPointerException("data");
        }
        if (data.length() == 0)
        {
            throw new ExtendedIllegalArgumentException("data (" + data + ")", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
        }

        chooseImpl();
        if (dataConverter_ == null)
        {
          synchronized (this)
          {
            if (dataConverter_ == null)
            {
              dataConverter_ = new Converter(system_.getCcsid(), system_);
            }
          }
        }
        write(dataConverter_.stringToByteArray(data), userSpaceOffset);
    }

    /**
     * Sets this object to using sockets.
     * This method is useful when running directly on IBM i, using ProgramCall or CommandCall objects 
     * in conjunction with user spaces.
     * When your Java program runs on the IBM i system, some Toolbox classes access data via a 
     * direct API call instead of making a socket call to the system (for example, to the Remote 
     * Command Host Server).
     * There are minor differences in the behavior of the classes when they use direct API calls 
     * instead of socket calls.  If your program is affected by these differences you can use this 
     * method to force the Toolbox classes to use socket calls instead of direct API calls.  
     * The default is false. 
     * <p>Note: This method has no effect if the Java application is running remotely, that is, not 
     * running directly on an IBM i system.  When running remotely, the Toolbox submits <i>all</i> 
     * program and command calls via sockets, regardless of the setting of this property.
     * <p>This property cannot be reset once a connection has been established.
     * @param  mustUseSockets  true to use sockets; false otherwise.
     * @see #isMustUseSockets
     * @see CommandCall#setThreadSafe(Boolean)
     * @see ProgramCall#setThreadSafe(boolean)
    **/
    public void setMustUseSockets(boolean mustUseSockets)
    {
        // Verify that connection has not been made.
        if (impl_ != null)
        {
            Trace.log(Trace.ERROR, "Cannot set property 'mustUseSockets' after connect.");
            throw new ExtendedIllegalStateException("mustUseSockets", ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
        }

        if (propertyChangeListeners_ == null)
        {
            mustUseSockets_ = mustUseSockets;
        }
        else
        {
            Boolean oldValue = new Boolean(mustUseSockets_);
            Boolean newValue = new Boolean(mustUseSockets);

            mustUseSockets_ = mustUseSockets;

            // Fire the property change event.
            propertyChangeListeners_.firePropertyChange("mustUseSockets", oldValue, newValue);
        }
    }

    /**
     Indicates whether sockets must be used when internally calling programs and commands.
     When your Java program runs on the system, some Toolbox classes access data via a direct call to an API instead of making a socket call to the system (for example, to the Remote Command Host Server).  There are minor differences in the behavior of the classes when they use direct API calls instead of socket calls.  If your program is affected by these differences you can check whether this object will use socket calls instead of API calls by using this method.
     @return  true if this object must use sockets; false otherwise.
     @see #setMustUseSockets
     **/
    public boolean isMustUseSockets()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Checking if must use sockets:", mustUseSockets_);
        return mustUseSockets_;
    }
}
