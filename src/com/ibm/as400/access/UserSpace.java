///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: UserSpace.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.util.Vector;
import java.beans.VetoableChangeSupport;
import java.beans.VetoableChangeListener;

/**
The UserSpace class represents a user space on the AS/400.
**/

public class UserSpace
  implements java.io.Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";



    static final long serialVersionUID = 4L;



   /**
     Constants
   **/
   private final static int maxUserSpaceSize_ = 16776704;       // Maximum allowed user space size.

   /**
     Force to Auxiliary Storage option that allow changes to be forced asynchronously.
   **/
   public final static int FORCE_ASYNCHRONOUS = 1;

   /**
     Force to Auxiliary Storage option that does not allow changes to be forced.  It uses normal system writes.
   **/
   public final static int FORCE_NONE = 0;

   /**
     Force to Auxiliary Storage option that allow changes to be forced synchronously.
   **/
   public final static int FORCE_SYNCHRONOUS = 2;


   /**
     Variables
   ***/
   transient private boolean connected_ = false;
   private AS400 system_ = null;                // The AS400 where the user space is located.
   private String userSpacePathName_ = null;    // The full path name of the user space.
   private String library_ = null;              // The library that contains the user space.
   private String name_ = null;                 // The name of the user space.

   private boolean mustUseProgramCall_ = false; // Use ProgramCall instead of IFS @E1a.

   transient private PropertyChangeSupport changes_ = new PropertyChangeSupport(this);
   transient private VetoableChangeSupport vetos_ = new VetoableChangeSupport(this);
   transient private Vector usListeners_ = new Vector();

   transient private UserSpaceImpl implementation_;


   /**
    Constructs a UserSpace object.
    It creates a default UserSpace object.  The <i>system</i> and <i>path
    properties </i> must be set before attempting a connection.
    **/
   public UserSpace()
   {
   }

   /**
    Constructs a UserSpace object.
    It creates a UserSpace instance that represents the user space <i>path</i>
    on <i>system</i>.
       @param system The AS400 that contains the file.
       @param path The fully qualified integrated file system path name.
   **/
   public UserSpace(AS400   system,
                    String  path)
   {
     // Validate arguments.
     if (system == null)
     {
       Trace.log(Trace.ERROR, "Parameter 'system' is null.");
       throw new NullPointerException("system");
     }

     if (path == null)
     {
       Trace.log(Trace.ERROR, "Parameter 'path' is null.");
       throw new NullPointerException("path");
     }

     QSYSObjectPathName userSpacePath = null;
     userSpacePath = new QSYSObjectPathName(path);

     // set instance vars
     userSpacePathName_ = path;
     system_ = system;

     // abstract the library and name of the user space
     library_ = userSpacePath.getLibraryName();
     name_ = userSpacePath.getObjectName();

   }

   /**
     Adds a listener to be notified when the value of any bound property is changed.
     It can be removed with removePropertyChangeListener.

        @param listener The PropertyChangeListener.
   **/
   public void addPropertyChangeListener(PropertyChangeListener listener)
   {
        if (listener == null)
        {
           Trace.log(Trace.ERROR, "Parameter 'listener' is null.");
           throw new NullPointerException("listener");
        }
        changes_.addPropertyChangeListener(listener);
   }
   /**
     Adds a listener to be notified when a UserSpaceEvent is fired.

        @param listener The object listener.
   **/
   public void addUserSpaceListener(UserSpaceListener listener)
   {
        if (listener == null)
        {
           Trace.log(Trace.ERROR, "Parameter 'listener' is null.");
           throw new NullPointerException("listener");
        }
        usListeners_.addElement(listener);
   }

   /**
     Adds a listener to be notified when the value of any constrained property is changed.

        @param listener The VetoableChangeListener.
   **/
   public void addVetoableChangeListener(VetoableChangeListener listener)
   {
        if (listener == null)
        {
           Trace.log(Trace.ERROR, "Parameter 'listener' is null.");
           throw new NullPointerException("listener");
        }
        vetos_.addVetoableChangeListener(listener);
   }

   /**
     Closes the user space and releases any system resources associated with the stream.
   **/
   public synchronized void close() throws IOException                  // $B3
   {
      // Verify connection.
      if (isConnected() == false) {
         Trace.log(Trace.ERROR, "User space is not open.");
         throw new ExtendedIllegalStateException("user space",
            ExtendedIllegalStateException.OBJECT_MUST_BE_OPEN);
      }
      implementation_.close();                            //$C0A

   }

   /**
     Connect()

     Determines the type of implementation that will be used.
     System and Path parameters are committed at this time.
   **/
   private synchronized void connect() throws IOException, AS400SecurityException
   {
       if (implementation_ == null)
       {
           // Ensure that the system has been set.
           if (system_ == null)
           {
               Trace.log(Trace.ERROR, "Parameter 'system' is null.");
               throw new ExtendedIllegalStateException("system",
                                                       ExtendedIllegalStateException.PROPERTY_NOT_SET);
           }
           // Ensure that the path has been set.
           if (userSpacePathName_ == null)
           {
               Trace.log(Trace.ERROR, "Parameter 'path' is null.");
               throw new ExtendedIllegalStateException("path",
                                                       ExtendedIllegalStateException.PROPERTY_NOT_SET);
           }



           // @D1 use an AS400 object method to load the correct implementation.
           //     That method will load the correct impl based on
           //     canUseNativeOp().  It will also load the remote impl or proxy
           //     impl if the nativeImpl class cannot be found or loaded.

           implementation_ = (com.ibm.as400.access.UserSpaceImpl)
             system_.loadImpl3(
                               "com.ibm.as400.access.UserSpaceImplNative",
                               "com.ibm.as400.access.UserSpaceImplRemote",
                               "com.ibm.as400.access.UserSpaceImplProxy");  // $C0A
           // set the User Space name to be used in API Program call.
           implementation_.setPath(userSpacePathName_);
           implementation_.setSystem(system_.getImpl());


           ConverterImpl conv = (new Converter(system_.getCcsid(), system_)).impl;

           implementation_.setConverter(conv);
           implementation_.setName();
           implementation_.setMustUseProgramCall(mustUseProgramCall_);            // @E1a

           // Set the connection flag, commits system and path parameters.
           connected_ = true;
       }
       system_.signon(false);
   }

   /**
     Creates the user space.

        @param length  The initial size (in bytes) of the user space.
            Valid values are 1 through 16,776,704.
        @param replace The value indicating if an existing user space is to be replaced.
        @param extendedAttribute  The user-defined extended attribute of the user space.  This string must be 10 characters or less.
        @param initialValue  The value used in creation and extension.
        @param textDescription  The text describing the user space.  This string must be 50 characters or less.
        @param authority  The public authority for the user space.  This string must be 10 characters or less.
            Valid values are:
            <ul>
                <li>*ALL
                <li>*CHANGE
                <li>*EXCLUDE
                <li>*LIBCRTAUT
                <li>*USE
                <li>authorization-list name.
            </ul>
        @exception AS400SecurityException If a security or authority error occurs.
        @exception ErrorCompletingRequestException If an error occurs before the request is completed.
        @exception InterruptedException If this thread is interrupted.
        @exception IOException If an error occurs while communicating with the AS/400.
        @exception ObjectDoesNotExistException If the AS400 object does not exist.
   **/
   public void create(int length,
                      boolean replace,
                      String extendedAttribute,
                      byte initialValue,
                      String textDescription,
                      String authority)
          throws AS400SecurityException,
                 ErrorCompletingRequestException,
                 InterruptedException,
                 IOException,
                 ObjectDoesNotExistException          // $B1

   {
      create("*DEFAULT", length, replace, extendedAttribute, initialValue, textDescription, authority);
   }

   /**
     Creates the user space.

        @param domain  The domain into which the user space is created.
            Valid value are: *DEFAULT, *USER, or *SYSTEM.
            *DEFAULT uses the allow user domain system value to determine if *USER or *SYSTEM will be used.
        @param length  The initial size (in bytes) of the user space.
              Valid values are 1 through 16,776,704.
        @param replace The value indicating if an existing user space is to be replaced.
        @param extendedAttribute  The user-defined extended attribute of the user space.  This string must be 10 characters or less.
        @param initialValue  The value used in creation and extension.
        @param textDescription  The text describing the user space.  This string must be 50 characters or less.
        @param authority  The authority given to users.  This string must be 10 characters or less.
            Valid values are:
              <ul>
                  <li>*ALL
                  <li>*CHANGE
                  <li>*EXCLUDE
                  <li>*LIBCRTAUT
                  <li>*USE
                  <li>authorization-list name
              </ul>
        @exception AS400SecurityException If a security or authority error occurs.
        @exception ErrorCompletingRequestException If an error occurs before the request is completed.
        @exception InterruptedException If this thread is interrupted.
        @exception IOException If an error occurs while communicating with the AS/400.
        @exception ObjectDoesNotExistException If the AS400 object does not exist.
   **/
   public void create(String domain,
                      int length,
                      boolean replace,
                      String extendedAttribute,
                      byte initialValue,
                      String textDescription,
                      String authority)
          throws AS400SecurityException,
                 ErrorCompletingRequestException,
                 InterruptedException,
                 IOException,
                 ObjectDoesNotExistException       // $B1
   {
      // Validate the domain parameter
      if (domain == null)
      {
          Trace.log(Trace.ERROR, "Parameter 'domain' is null.");
          throw new NullPointerException("domain");
      }
      //if (domain.length() == 0 || domain.length() > 10)
      if (!domain.equals("*DEFAULT") && !domain.equals("*USE") && !domain.equals("*SYSTEM"))  //$C0C
      {
          Trace.log(Trace.ERROR, "Parameter 'domain' is not valid.");
          throw new ExtendedIllegalArgumentException("domain",
              ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
      }

      // Validate the length parameter
      if (length < 1 || length > maxUserSpaceSize_)
      {
          Trace.log(Trace.ERROR, "Parameter 'length' is not valid.");
          throw new ExtendedIllegalArgumentException("length",
              ExtendedIllegalArgumentException.RANGE_NOT_VALID);
      }

      // Validate the extended attribute parameter.
      if (extendedAttribute == null)
      {
         Trace.log(Trace.ERROR, "Parameter 'extendedAttribute' is null.");
         throw new NullPointerException("Extended Attribute");
      }                                                                          

      if (extendedAttribute.length() == 0)                               // @D3a
          extendedAttribute = " ";                                       // @D3a

      if (extendedAttribute.length() > 10)                               // @D3c
      {
          Trace.log(Trace.ERROR, "Parameter 'extendedAttribute' is not valid.");
          throw new ExtendedIllegalArgumentException("extendedAttribute",
              ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
      }
      // Validate the text description parameter
      if (textDescription == null)
      {
          Trace.log(Trace.ERROR, "Parameter 'textDescription' is null.");
          throw new NullPointerException("textDescription");
      }

      if (textDescription.length() == 0)                                 // @D3a
          textDescription = " ";                                         // @D3a

      if (textDescription.length() > 50)                                 // @D3c
      {
          Trace.log(Trace.ERROR, "Parameter 'textDescription' is not valid.");
          throw new ExtendedIllegalArgumentException("textDescription",
              ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
      }
      // Validate the authority parameter.
      if (authority == null)
      {
          Trace.log(Trace.ERROR, "Parameter 'authority' is null.");
          throw new NullPointerException("authority");
      }
      if (authority.length() == 0 || authority.length() > 10)
      {
          Trace.log(Trace.ERROR, "Parameter 'authority' is not valid.");
          throw new ExtendedIllegalArgumentException("authority",
              ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
      }

      // Verify connection
      connect();

      implementation_.create(domain, length, replace, extendedAttribute, initialValue, textDescription, authority);

      // Fire the CREATED event.
      Vector targets;
      targets = (Vector) usListeners_.clone();
      UserSpaceEvent event = new UserSpaceEvent(this, UserSpaceEvent.US_CREATED);
      for (int i = 0; i < targets.size(); i++) {
          UserSpaceListener target = (UserSpaceListener)targets.elementAt(i);
          target.created(event);
      }
   }

   /**
     Deletes the user space.

        @exception AS400SecurityException If a security or authority error occurs.
        @exception ErrorCompletingRequestException If an error occurs before the request is completed.
        @exception InterruptedException If this thread is interrupted.
        @exception IOException If an error occurs while communicating with the AS/400.
        @exception ObjectDoesNotExistException If the AS400 object does not exist.
   **/
   public void delete()
          throws AS400SecurityException,
                 ErrorCompletingRequestException,
                 InterruptedException,
                 IOException,
                 ObjectDoesNotExistException       // $B1

   {
       connect();
      implementation_.delete();

      // Fire the DELETED event.
      Vector targets;
      targets = (Vector) usListeners_.clone();
      UserSpaceEvent event = new UserSpaceEvent(this, UserSpaceEvent.US_DELETED);
      for (int i = 0; i < targets.size(); i++) {
          UserSpaceListener target = (UserSpaceListener)targets.elementAt(i);
          target.deleted(event);
      }
   }


   /**
     Determines if the user space exists.

        @return true if the user space exists; false otherwise.
        @exception AS400SecurityException If a security or authority error occurs.
        @exception ErrorCompletingRequestException If an error occurs before the request is completed.
        @exception InterruptedException If this thread is interrupted.
        @exception IOException If an error occurs while communicating with the AS/400.
   **/
   // @D2 New method
   public boolean exists()
          throws AS400SecurityException,
                 ErrorCompletingRequestException,
                 InterruptedException,
                 IOException

   {
      try
      {
         getLength();
         return true;
      }
      catch (ObjectDoesNotExistException e)
      {
         return false;
      }
      catch (IOException e2)
      {
         String message = e2.getMessage();

         if (message.startsWith("CPF2209") ||     // library not found
             message.startsWith("CPF9810") ||     // library not found
             message.startsWith("CPF9801"))       // object not found
            return false;
         else
            throw e2;
      }
   }


   /**
     Returns the initial value used for filling in the user space during creation and extension.

        @return The initial value used during user space creation and extension.
        @exception AS400SecurityException If a security or authority error occurs.
        @exception ErrorCompletingRequestException If an error occurs before the request is completed.
        @exception InterruptedException If this thread is interrupted.
        @exception IOException If an error occurs while communicating with the AS/400.
        @exception ObjectDoesNotExistException If the AS400 object does not exist.
   **/
   public byte getInitialValue()
           throws AS400SecurityException,
                  ErrorCompletingRequestException,
                  InterruptedException,
                  IOException,
                  ObjectDoesNotExistException       // $B1
   {
       connect();

      return implementation_.getInitialValue();
   }

   /**
     Returns the size (in bytes) of the user space.

        @return The size (in bytes) of the user space.
        @exception AS400SecurityException If a security or authority error occurs.
        @exception ErrorCompletingRequestException If an error occurs before the request is completed.
        @exception InterruptedException If this thread is interrupted.
        @exception IOException If an error occurs while communicating with the AS/400.
        @exception ObjectDoesNotExistException If the AS400 object does not exist.
   **/
   public int getLength()
           throws AS400SecurityException,
                  ErrorCompletingRequestException,
                  InterruptedException,
                  IOException,
                  ObjectDoesNotExistException       // $B1
   {
       connect();

      return implementation_.getLength();
   }

   /**
     Returns the user space name.

        @return The name of the user space.
   **/
   public String getName()
   {
      return name_;
   }

   /**
     Returns the integrated file system path name of the object represented by the user space.

        @return The integrated file system path name of the object represented by the user space.
   **/
   public String getPath()
   {
       return userSpacePathName_;
   }

   /**
     Returns the AS400 system object for the user space.

        @return The AS400 system object for the user space.
   **/
   public AS400 getSystem()
   {
       return system_;
   }

   /**
     Indicates if the user space is auto extendible.  When running on a workstation the auto extend
     attribute is always true and cannot be changed, so the attribute value returned should be ignored.
     The auto extend attribute can be used when running on the AS/400's JVM with the optimizations that
     are a part of OS/400.

        @return true if the user space is auto extendible; false otherwise.
        @exception AS400SecurityException If a security or authority error occurs.
        @exception ErrorCompletingRequestException If an error occurs before the request is completed.
        @exception InterruptedException If this thread is interrupted.
        @exception IOException If an error occurs while communicating with the AS/400.
        @exception ObjectDoesNotExistException If the AS400 object does not exist.
   **/
   public boolean isAutoExtendible()
          throws AS400SecurityException,
                 ErrorCompletingRequestException,
                 InterruptedException,
                 IOException,
                 ObjectDoesNotExistException       // $B1
   {
       connect();

      return implementation_.isAutoExtendible();
   }

   /**
     Indicates if a connection has been established.
   **/
   boolean isConnected()
   {
      return connected_;
   }


   /**
     Indicates if Toolbox ProgramCall will be used internally to perform
     user space read and write requests.  If false, Toolbox Integrated
     File System classes will be used tp perform user space read and
     write reqeusts.
     @see UserSpace#setMustUseProgramCall

        @return true if user space read and write requests will be
                     performed via program call; false otherwise.
   **/
   // E1a New method
   public boolean isMustUseProgramCall()
   {
      return mustUseProgramCall_;
   }





   /**
     Reads up to <i>dataBuffer.length</i> bytes from the user space beginning at <i>userSpaceOffset</i>
         into <i>dataBuffer</i>.

         @param dataBuffer  The buffer to fill with data.  Buffer.length()
                            bytes will be read from the user space.
         @param userSpaceOffset  The offset in the user space from which to start reading.
         @return The total number of bytes read into the buffer, or -1 if
                     the <i>userSpaceOffset</i> is beyond the end of the user space.
         @exception AS400SecurityException If a security or authority error occurs.
         @exception ErrorCompletingRequestException If an error occurs before the request is completed.
         @exception InterruptedException If this thread is interrupted.
         @exception IOException If an error occurs while communicating with the AS/400.
         @exception ObjectDoesNotExistException If the AS400 object does not exist.
   **/
   public int read(byte[] dataBuffer, int userSpaceOffset)
            throws AS400SecurityException,
                   ErrorCompletingRequestException,
                   InterruptedException,
                   IOException,
                   ObjectDoesNotExistException
   {
      // Validate the data buffer parameter.
       if (dataBuffer == null)
       {
          Trace.log(Trace.ERROR, "Parameter 'dataBuffer' is null.");
          throw new NullPointerException("dataBuffer");
       }

       return read(dataBuffer, userSpaceOffset, 0, dataBuffer.length);
   }

   /**
     Reads up to <i>length</i> bytes from the user space beginning at <i>userSpaceOffset</i> into <i>dataBuffer</i>
         beginning at <i>dataOffset</i>.


         @param dataBuffer  The buffer to fill with data.
         @param userSpaceOffset  The offset in the user space from which to start reading.
         @param dataOffset  The starting offset in the data buffer for the results of the read.
         @param length  The number of bytes to read.
         @return The total number of bytes read into the buffer, or -1 if
                     the <i>userSpaceOffset</i> is beyond the end of the user space.
         @exception AS400SecurityException If a security or authority error occurs.
         @exception ErrorCompletingRequestException If an error occurs before the request is completed.
         @exception InterruptedException If this thread is interrupted.
         @exception IOException If an error occurs while communicating with the AS/400.
         @exception ObjectDoesNotExistException If the AS400 object does not exist.
   **/
   public int read(byte[] dataBuffer, int userSpaceOffset, int dataOffset, int length)
           throws AS400SecurityException,
                  ErrorCompletingRequestException,
                  InterruptedException,
                  IOException,
                  ObjectDoesNotExistException
   {
       // Validate the data buffer parameter.
       if (dataBuffer == null)
       {
          Trace.log(Trace.ERROR, "Parameter 'dataBuffer' is null.");
          throw new NullPointerException("dataBuffer");
       }

       // Validate the data buffer parameter.
       if (dataBuffer.length == 0)
       {
          Trace.log(Trace.ERROR, "Parameter 'dataBuffer' is not valid.");
          throw new ExtendedIllegalArgumentException("dataBuffer",
                       ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
       }

       // Validate the User Space offset parameter.
       if (userSpaceOffset < 0 || userSpaceOffset > maxUserSpaceSize_)
       {
          Trace.log(Trace.ERROR, "Parameter 'userSpaceOffset' is not valid.");
          throw new ExtendedIllegalArgumentException("userSpaceOffset",
                      ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
       }

       // Validate the data offset parameter.
       if (dataOffset < 0 || dataOffset >= dataBuffer.length)
       {
          Trace.log(Trace.ERROR, "Parameter 'dataOffset' is not valid.");
          throw new ExtendedIllegalArgumentException("dataOffset",
                    ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
       }

       // Validate the length parameter.
       if (length < 0 || length > (dataBuffer.length - dataOffset))
       {
          Trace.log(Trace.ERROR, "Parameter 'length' is not valid.");
          throw new ExtendedIllegalArgumentException("length",
                       ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
       }

       // Handle a read of 0 bytes here.
       if (length == 0)
       {
          return 0;
       }

       // Verify connection.
       connect();

       // Do the read.
       int bytesRead = implementation_.read(dataBuffer, userSpaceOffset, dataOffset, length);
       // Fire the READ event.
       Vector targets;
       targets = (Vector) usListeners_.clone();
       UserSpaceEvent event = new UserSpaceEvent(this, UserSpaceEvent.US_READ);
       for (int i = 0; i < targets.size(); i++)
       {
           UserSpaceListener target = (UserSpaceListener)targets.elementAt(i);
           target.read(event);
       }

       return bytesRead;
   }

   /**
     Returns a string from the user space beginning at <i>userSpaceOffset</i>.
     Data is read from the user space as if by the read(byte[],int,int,int) method.
     The resulting byte array is then converted into a String.

         @param userSpaceOffset  The offset in the user space from which to start reading.
         @param length  The number of bytes to read.
         @return The string value from the user space.
         @exception AS400SecurityException If a security or authority error occurs.
         @exception ErrorCompletingRequestException If an error occurs before the request is completed.
         @exception InterruptedException If this thread is interrupted.
         @exception IOException If an error occurs while communicating with the AS/400.
         @exception ObjectDoesNotExistException If the AS400 object does not exist.
   **/
   public String read(int userSpaceOffset, int length)
           throws AS400SecurityException,
                  ErrorCompletingRequestException,
                  InterruptedException,
                  IOException,
                  ObjectDoesNotExistException
   {
      // Validate the length parameter.
       if (length <= 0)
       {
          Trace.log(Trace.ERROR, "Parameter 'length' is not valid.");
          throw new ExtendedIllegalArgumentException("length",
                       ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
       }

       // Create dataBuffer
       byte[] dataBuffer = new byte[length];

       // Do the read.
       int bytesRead = read(dataBuffer, userSpaceOffset, 0, length);

       return implementation_.converter_.byteArrayToString(dataBuffer, 0, length);  // @C1C
   }

   /**
   *Overrides the ObjectInputStream.readObject() method in order to return any
   *transient parts of the object to there properly initialized state.  We also
   *generate a declared file name for the object.  I.e we in effect
   *call the null constructor.  By calling ObjectInputStream.defaultReadObject()
   *we restore the state of any non-static and non-transient variables.  We
   *then continue on to restore the state (as necessary) of the remaining varaibles.
   *@param in The input stream from which to deserialize the object.
   *@exception ClassNotFoundException If the class being deserialized is not found.
   *@exception IOException If an error occurs while communicating with the AS/400.
   **/
   private void readObject(java.io.ObjectInputStream in)
      throws ClassNotFoundException,
             IOException
   {
      in.defaultReadObject();
      resetStateForReadObject();
   }

   /**
     Removes this listener from being notified when a bound property changes.

        @param listener The PropertyChangeListener.
   **/
   public void removePropertyChangeListener(PropertyChangeListener listener)
   {
      if (listener == null)
      {
         Trace.log(Trace.ERROR, "Parameter 'listener' is null.");
         throw new NullPointerException("listener");
      }

       changes_.removePropertyChangeListener(listener);
   }

   /**
     Removes a listener from the UserSpace listeners list.

        @param listener The user space listener.
   **/
   public void removeUserSpaceListener(UserSpaceListener listener)
   {
      if (listener == null)
      {
         Trace.log(Trace.ERROR, "Parameter 'listener' is null.");
         throw new NullPointerException("listener");
      }

       usListeners_.removeElement(listener);
   }

   /**
     Removes this listener from being notified when a constrained property changes.

        @param listener The VetoableChangeListener.
   **/
   public void removeVetoableChangeListener(VetoableChangeListener listener)
   {
      if (listener == null)
      {
         Trace.log(Trace.ERROR, "Parameter 'listener' is null.");
         throw new NullPointerException("listener");
      }

       vetos_.removeVetoableChangeListener(listener);
   }

   /**
   *Resets the state instance variables of this object to the appropriate
   *values for the file being closed.  This method is used to reset the
   *the state of the object when the connection has been ended abruptly.
   **/
   void resetStateForReadObject()
   {
      connected_ = false;            // Reset the connected flag.

      implementation_ = null;        // Reset implementation   $C0A

      // Reset the listeners.
      PropertyChangeSupport changes_ = new PropertyChangeSupport(this);
      VetoableChangeSupport vetos_ = new VetoableChangeSupport(this);
      Vector usListeners_ = new Vector();
   }

   /**
     Sets the auto extend attribute if possible.  When running on a workstation the Toolbox cannot
     set the auto extend attribute so this method is ignored and auto extend is always true.  Auto
     extend can be set when running on the AS/400's JVM with the optimizations that are a part of
     OS/400.

         @param autoExtendibility  The attribute for user space auto extendibility.
         @exception AS400SecurityException If a security or authority error occurs.
         @exception ErrorCompletingRequestException If an error occurs before the request is completed.
         @exception InterruptedException If this thread is interrupted.
         @exception IOException If an error occurs while communicating with the AS/400.
         @exception ObjectDoesNotExistException If the AS400 object does not exist.
   **/
   public void setAutoExtendible(boolean autoExtendibility)
           throws AS400SecurityException,
                  ErrorCompletingRequestException,
                  InterruptedException,
                  IOException,
                  ObjectDoesNotExistException      // $B1
   {
       connect();

      implementation_.setAutoExtendible(autoExtendibility);
   }

   /**
     Sets the initial value to be used during user space creation or extension.

        @param initialValue  The new initial value used during future extensions.
             For best performance set byte to hexadecimal zeros.
        @exception AS400SecurityException If a security or authority error occurs.
        @exception ErrorCompletingRequestException If an error occurs before the request is completed.
        @exception InterruptedException If this thread is interrupted.
        @exception IOException If an error occurs while communicating with the AS/400.
        @exception ObjectDoesNotExistException If the AS400 object does not exist.
   **/
   public void setInitialValue(byte initialValue)
           throws AS400SecurityException,
                  ErrorCompletingRequestException,
                  InterruptedException,
                  IOException,
                  ObjectDoesNotExistException      // $B1
   {
       connect();

      implementation_.setInitialValue(initialValue);
   }

   /**
     Sets the size (in bytes) of the user space.  Valid values are 1 through 16,776,704.

        @param length  The new size (in bytes) of the user space.
        @exception AS400SecurityException If a security or authority error occurs.
        @exception ErrorCompletingRequestException If an error occurs before the request is completed.
        @exception InterruptedException If this thread is interrupted.
        @exception IOException If an error occurs while communicating with the AS/400.
        @exception ObjectDoesNotExistException If the AS400 object does not exist.
   **/
   public void setLength(int length)
           throws AS400SecurityException,
                  ErrorCompletingRequestException,
                  InterruptedException,
                  IOException,
                  ObjectDoesNotExistException    // $B1
   {
      // Validate the length parameter
       if (length < 1 || length > maxUserSpaceSize_)
       {
           Trace.log(Trace.ERROR, "Parameter 'length' is not valid.");
           throw new ExtendedIllegalArgumentException("length",
              ExtendedIllegalArgumentException.RANGE_NOT_VALID);
       }

      // Verify connection.
       connect();

      implementation_.setLength(length);
   }


   /**
     Sets the method used to carry out user space read and write operations.
     If false (the default) read and write requests are made via
     the AS/400 file server.  Internally, an IFSRandomAccessFile
     object is used to perform read and write requests.  If true,
     internally a ProgramCall object is used to perform read and
     write requests.  In general, requests made via the file object
     are faster but the behavior of requests made via a ProgramCall
     object are more consistent with AS/400 user space APIs.
     This option cannot be reset once a connection
     has been established.

        @param useProgramCall Internally use ProgramCall to carry out
                              read and write requests.
   **/
   // @E1a method added
   public void setMustUseProgramCall(boolean useProgramCall)
   {
      // Verify that connection has not been made.
      if (isConnected())
      {
          Trace.log(Trace.ERROR, "Parameter 'mustUseProgramCall' is not changed (Connected=true).");

          throw new ExtendedIllegalStateException("mustUseProgramCall",
                    ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
      }

      // Fire the property change event.
      changes_.firePropertyChange("mustUseProgramCall", new Boolean(mustUseProgramCall_), new Boolean(useProgramCall));

      mustUseProgramCall_ = useProgramCall;
   }






   /**
     Sets the path for the user space.
     The path can only be set before a connection has been established.

        @param path  The fully qualified integrated file system path name.
        @exception PropertyVetoException If the change is vetoed.
   **/
   public void setPath(String path)
           throws PropertyVetoException
   {
       // check parm
       if (path == null)
       {
          Trace.log(Trace.ERROR, "Parameter 'path' is null.");
          throw new NullPointerException("path");
       }
      // Verify name is valid integrated file system path name.
      QSYSObjectPathName userSpacePath = null;
      userSpacePath = new QSYSObjectPathName(path);

      // Remember the current path value.
      String oldPath = userSpacePathName_;               //$C0 Moved here from if/else block

      // Fire a vetoable change event for the path.
      vetos_.fireVetoableChange("path", oldPath, path);  //$C0 Moved here from if/else block

      // Set User Space path the first time.
      if (userSpacePathName_ == null)
      {
         userSpacePathName_ = path;
         library_ = userSpacePath.getLibraryName();
         name_ = userSpacePath.getObjectName();
      }
      else
      {
         // If system property is set, make sure we have not already connected.
         if (system_ != null)
         {
             if (isConnected() )
             {
                 Trace.log(Trace.ERROR, "Parameter 'path' is not changed (Connected=true).");
                 throw new ExtendedIllegalStateException("path",
                     ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
             }
         }

         // Update the path value.
         userSpacePathName_ = path;
         library_ = userSpacePath.getLibraryName();
         name_ = userSpacePath.getObjectName();

      }

      // Fire the property change event.
      changes_.firePropertyChange("path", oldPath, path);  //$C0 Moved here from if/else block

   }

   /**
     Sets the AS400 system for the user space.
     The system can only be set before a connection has been established.

        @param system  The AS400 system.
        @exception PropertyVetoException If the change is vetoed.
   **/
   public void setSystem(AS400 system)
          throws PropertyVetoException
   {
       // check parm
       if (system == null)
       {
           Trace.log(Trace.ERROR, "Parameter 'system' is null.");
           throw new NullPointerException("system");
       }

           // set system parameter for first time.
      if (system_ == null)
         system_ = system;
      else
      {
          // Verify that connection has not been made.
          if (isConnected())
          {
             Trace.log(Trace.ERROR, "Parameter 'system' is not changed (Connected=true).");
             throw new ExtendedIllegalStateException("system",
                  ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
          }

          // Remember the old system.
          AS400 oldSystem = system_;

          // Fire a vetoable change event for system.
          vetos_.fireVetoableChange("system", oldSystem, system);

          system_ = system;

          // Fire the property change event.
          changes_.firePropertyChange("system", oldSystem, system_);
       }
   }

   /**
     Writes up to <i>dataBuffer.length</i> bytes from <i>dataBuffer</i> into the user space beginning at <i>userSpaceOffset</i>.

        @param dataBuffer  The data buffer to be written.
        @param userSpaceOffset  The position in the user space to start writing.
        @exception AS400SecurityException If a security or authority error occurs.
        @exception ErrorCompletingRequestException If an error occurs before the request is completed.
        @exception InterruptedException If this thread is interrupted.
        @exception IOException If an error occurs while communicating with the AS/400.
        @exception ObjectDoesNotExistException If the AS400 object does not exist.
   **/
   public void write(byte[] dataBuffer, int userSpaceOffset)
           throws AS400SecurityException,
                  ErrorCompletingRequestException,
                  InterruptedException,
                  IOException,
                  ObjectDoesNotExistException
   {
      // Validate the data buffer parameter.
       if (dataBuffer == null)
       {
          Trace.log(Trace.ERROR, "Parameter 'dataBuffer' is null.");
          throw new NullPointerException("dataBuffer");
       }

       write(dataBuffer, userSpaceOffset, 0, dataBuffer.length, FORCE_NONE);
   }


   /**
     Writes up to <i>length</i> bytes from <i>dataBuffer</i> beginning at <i>dataOffset</i> into the user space beginning at <i>userSpaceOffset</i>.

        @param dataBuffer  The data buffer to be written.
        @param userSpaceOffset  The position in the user space to start writing.
        @param dataOffset  The position in the write data buffer from which to start copying.
        @param length  The length (in bytes) of data to be written.
        @exception AS400SecurityException If a security or authority error occurs.
        @exception ErrorCompletingRequestException If an error occurs before the request is completed.
        @exception InterruptedException If this thread is interrupted.
        @exception IOException If an error occurs while communicating with the AS/400.
        @exception ObjectDoesNotExistException If the AS400 object does not exist.
   **/
   public void write(byte[] dataBuffer, int userSpaceOffset, int dataOffset, int length)
           throws AS400SecurityException,
                  ErrorCompletingRequestException,
                  InterruptedException,
                  IOException,
                  ObjectDoesNotExistException
   {
      write(dataBuffer, userSpaceOffset, dataOffset, length, FORCE_NONE);
   }

   /**
     Writes up to <i>length</i> bytes from <i>dataBuffer</i> beginning at <i>dataOffset</i> into the user
        space beginning at <i>userSpaceOffset</i>.

        @param dataBuffer  The data buffer to be written to the user space.
        @param userSpaceOffset  The position in the user space to start writing.
        @param dataOffset  The position in the write data buffer from which to start copying.
        @param length  The length (in bytes) of data to be written.
        @param forceAuxiliary  The method of forcing changes made to the user space to
              auxiliary storage.  Valid values are:
              <UL>
              <LI>FORCE_NONE
              <LI>FORCE_ASYNCHRONOUS
              <LI>FORCE_SYNCHRONOUS
              </UL>
        @exception AS400SecurityException If a security or authority error occurs.
        @exception ErrorCompletingRequestException If an error occurs before the request is completed.
        @exception InterruptedException If this thread is interrupted.
        @exception IOException If an error occurs while communicating with the AS/400.
        @exception ObjectDoesNotExistException If the AS400 object does not exist.
   **/
   public void write(byte[] dataBuffer, int userSpaceOffset, int dataOffset, int length, int forceAuxiliary)
           throws AS400SecurityException,
                  ErrorCompletingRequestException,
                  InterruptedException,
                  IOException,
                  ObjectDoesNotExistException

   {
       // Validate the data buffer parameter.
       if (dataBuffer == null)
       {
          Trace.log(Trace.ERROR, "Parameter 'dataBuffer' is null.");
          throw new NullPointerException("dataBuffer");
       }

       // Validate the data buffer parameter.
       if (dataBuffer.length == 0)
       {
          Trace.log(Trace.ERROR, "Parameter 'dataBuffer' is not valid.");
          throw new ExtendedIllegalArgumentException("dataBuffer",
                       ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
       }

       // Validate the User Space offset parameter.
       if (userSpaceOffset < 0 || userSpaceOffset > maxUserSpaceSize_ )
       {
          Trace.log(Trace.ERROR, "Parameter 'userSpaceOffset' is not valid.");
          throw new ExtendedIllegalArgumentException("userSpaceOffset",
                      ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
       }

       // Validate the data offset parameter.
       if (dataOffset < 0 || dataOffset >= dataBuffer.length)
       {
          Trace.log(Trace.ERROR, "Parameter 'dataOffset' is not valid.");
          throw new ExtendedIllegalArgumentException("dataOffset",
                       ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
       }

       // Validate the length parameter.
       if (length < 0 || length > (dataBuffer.length - dataOffset))
       {
          Trace.log(Trace.ERROR, "Parameter 'length' is not valid.");
          throw new ExtendedIllegalArgumentException("length",
                       ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
       }

       // Validate the force auxiliary parameter.
       if (forceAuxiliary < 0 || forceAuxiliary > 2)
       {
          Trace.log(Trace.ERROR, "Parameter 'forceAuxiliary' is not valid.");
          throw new ExtendedIllegalArgumentException("forceAuxiliary",
                       ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
       }

       // Validate the overflow possibility
       if (userSpaceOffset + length >= maxUserSpaceSize_ )
       {
           Trace.log(Trace.ERROR, "Request is not supported, causes space overflow.");
           throw new ExtendedIOException("Request causes space overflow.",
                        ExtendedIOException.REQUEST_NOT_SUPPORTED);
       }


       // Verify connection.
       connect();

       implementation_.write(dataBuffer, userSpaceOffset, dataOffset, length, forceAuxiliary);

       // Fire the WRITTEN event.
       Vector targets;
       targets = (Vector) usListeners_.clone();
       UserSpaceEvent event = new UserSpaceEvent(this, UserSpaceEvent.US_WRITTEN);
       for (int i = 0; i < targets.size(); i++)
       {
          UserSpaceListener target = (UserSpaceListener)targets.elementAt(i);
          target.written(event);
       }
   }

   /**
     Writes a string into the user space beginning at <i>userSpaceOffset</i>.
     String is converted into bytes and written to the user space as if by the write(byte[],int) method.

        @param data  The data buffer to be written to the user space.
        @param userSpaceOffset  The position in the user space to start writing.
        @exception AS400SecurityException If a security or authority error occurs.
        @exception ErrorCompletingRequestException If an error occurs before the request is completed.
        @exception InterruptedException If this thread is interrupted.
        @exception IOException If an error occurs while communicating with the AS/400.
        @exception ObjectDoesNotExistException If the AS400 object does not exist.
   **/
   public void write(String data, int userSpaceOffset)
           throws AS400SecurityException,
                  ErrorCompletingRequestException,
                  InterruptedException,
                  IOException,
                  ObjectDoesNotExistException
   {
      // Validate the data parameter.
      if (data == null)
      {
         Trace.log(Trace.ERROR, "Parameter 'data' is null.");
         throw new NullPointerException("data");
      }
      if (data.length() == 0)
      {
         Trace.log(Trace.ERROR, "Parameter 'data' is not valid.");
         throw new ExtendedIllegalArgumentException("data",
                      ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
      }

      // Verify connection.          - $B2
      connect();

      // Convert String to bytes.
      byte[] dataBuffer = implementation_.converter_.stringToByteArray(data);
      if (Trace.isTraceOn())
      {
         Trace.log(Trace.INFORMATION, "string length: " + data.length() + " dataBuffer length: " + dataBuffer.length);
      }

      // Do the write.
      write(dataBuffer, userSpaceOffset, 0, dataBuffer.length, FORCE_NONE );
   }
}
