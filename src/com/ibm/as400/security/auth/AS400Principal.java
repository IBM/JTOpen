package com.ibm.as400.security.auth;

///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: AS400Principal.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2003 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////
import com.ibm.as400.access.AS400;
import com.ibm.as400.access.AS400Exception;
import com.ibm.as400.access.AS400SecurityException;
import com.ibm.as400.access.ConnectionDroppedException;
import com.ibm.as400.access.ErrorCompletingRequestException;
import com.ibm.as400.access.ExtendedIllegalStateException;
import com.ibm.as400.access.InternalErrorException;
import com.ibm.as400.access.ObjectDoesNotExistException;
import com.ibm.as400.access.Trace;
import com.ibm.as400.access.User;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Vector;
/**
 * The AS400Principal class provides an abstract superclass
 * for representations of iSeries system security-related
 * identities.
 *
 * <p> Typical iSeries system Principals include, but are not
 * necessarily limited to, user profiles.
 *
 */
public abstract class AS400Principal implements Principal, Serializable {


    private static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";
    static final long serialVersionUID = 4L;


     private transient PropertyChangeSupport changes_ = new PropertyChangeSupport(this);
     private transient VetoableChangeSupport vetos_ = new VetoableChangeSupport(this);
     private transient Vector credentialListeners_ = new Vector();
     private AS400 system_ = null;
/**
 * Constructs an AS400Principal object.
 *
 */
public AS400Principal() {
     super();
     initTransient();
}
/**
 * Constructs an AS400Principal object.
 *
 * <p> The <i>system</i> property is set to the
 * specified value.
 *
 * @param system
 *        The iSeries system associated with the principal.
 *
 */
public AS400Principal(AS400 system) {
     this();
     try {
          setSystem(system); }
     catch (PropertyVetoException pve) {
          AuthenticationSystem.handleUnexpectedException(pve); }
}
/**
 * Adds a PropertyChangeListener.
 *
 * <p> The specified listener's <b>propertyChange</b>
 * method will be called each time the value of a
 * bound property is changed.
 *
 * @param listener
 *        The PropertyChangeListener.
 *
 * @see #removePropertyChangeListener
 *
 */
public void addPropertyChangeListener(PropertyChangeListener listener) {
     if (listener == null) {
         Trace.log(Trace.ERROR, "Parameter 'listener' is null.");
         throw new NullPointerException("listener");
     }
     changes_.addPropertyChangeListener(listener);
}
/**
 * Adds a VetoableChangeListener.
 *
 * <p> The specified listener's <b>vetoableChange</b>
 * method will be called each time the value of a
 * constrained property is changed.
 *
 * @param listener
 *        The VetoableChangeListener.
 *
 * @see #removeVetoableChangeListener
 *
 */
public void addVetoableChangeListener(VetoableChangeListener listener) {
     if (listener == null) {
         Trace.log(Trace.ERROR, "Parameter 'listener' is null.");
         throw new NullPointerException("listener");
     }
     vetos_.addVetoableChangeListener(listener);
}
/**
 * Report a bound property update to any registered listeners.
 *
 * @param propertyName
 *        The programmatic name of the property that was changed.
 *
 * @param oldValue
 *        The old value of the property.
 *
 * @param newValue
 *        The new value of the property.
 *
 */
void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
     changes_.firePropertyChange(propertyName, oldValue, newValue);
}
/**
 * Report a vetoable property update to any registered listeners.
 *
 * @param propertyName
 *        The programmatic name of the property that was changed.
 *
 * @param oldValue
 *        The old value of the property.
 *
 * @param newValue
 *        The new value of the property.
 *
 * @exception PropertyVetoException
 *        If the recipient wishes the property change to be rolled back.
 *
 */
void fireVetoableChange(String propertyName, Object oldValue, Object newValue) throws PropertyVetoException {
     vetos_.fireVetoableChange(propertyName, oldValue, newValue);
}
/**
 * Returns the name commonly used to refer to the principal.
 *
 * <p> Default behavior for the superclass is to return the
 * <A HREF="#getUserProfileName()">user profile name</A>
 *
 * @return
 *        The principal's name.
 *
 */
public String getName() {
     return getUserProfileName();
}
/**
 * Returns the AS400 system object for the principal.
 *
 * @return
 *        The AS400 system for the principal;
 *        null if not assigned.
 *
 */
public AS400 getSystem() {
     return system_;
}
/**
 * Returns an iSeries system User object based on the
 * <A HREF="#getUserProfileName()">user profile name</A> and
 * <A HREF="#getSystem()">system</A> associated with
 * the principal.
 *
 * <p> The <i>system</i> property must be set and a
 * valid <i>userProfileName</i> must be identified by
 * the principal prior to requesting the user.
 *
 * @return
 *        The com.ibm.as400.access.User object.
 *
 * @exception ExtendedIllegalStateException
 *        If a required property is not set.
 * @exception AS400Exception
 *        If the iSeries system returns an error message.
 * @exception AS400SecurityException
 *        If a security or authority error occurs.
 * @exception ConnectionDroppedException
 *        If the connection is dropped unexpectedly.
 * @exception ErrorCompletingRequestException
 *        If an error occurs before the request is completed.
 * @exception InterruptedException
 *        If this thread is interrupted.
 * @exception IOException
 *        If an error occurs while communicating with the server.
 * @exception ObjectDoesNotExistException
 *        If the OS/400 object does not exist.
 * @exception UnsupportedEncodingException
 *        If the character encoding is not supported.
 *
 */
public User getUser()
     throws AS400Exception, AS400SecurityException, ConnectionDroppedException,
     ErrorCompletingRequestException, InterruptedException, ObjectDoesNotExistException,
     IOException, UnsupportedEncodingException
{
     if (getSystem() == null) {
          Trace.log(Trace.ERROR, "Required property 'system' not set.");
          throw new ExtendedIllegalStateException(
               ExtendedIllegalStateException.PROPERTY_NOT_SET);
     }
     return new User(getSystem(), getUserProfileName());
}
/**
 * Returns the name of a user profile associated with
 * the OS/400 thread when work is performed on
 * behalf of the principal.
 *
 * @return
 *        A String containing the name; empty if not applicable.
 *
 */
public String getUserProfileName() {
     return "";
}
/**
 * Initializes transient data.
 *
 * <p> Subclasses should override as necessary to
 * initialize additional class-specific data.
 *
 */
void initTransient() {
}
/**
 * Overrides the ObjectInputStream.readObject() method in order to return any
 * transient parts of the object to there properly initialized state.
 *
 * By calling ObjectInputStream.defaultReadObject() we restore the state of
 * any non-static and non-transient variables.  We then continue on to
 * restore the state (as necessary) of the remaining varaibles.
 *
 * @param in
 *        The input stream from which to deserialize the object.
 *
 * @exception ClassNotFoundException
 *        If the class being deserialized is not found.
 *
 * @exception IOException
 *        If an error occurs while communicating with the server.
 *
 */
private void readObject(java.io.ObjectInputStream in)
     throws ClassNotFoundException, java.io.IOException
{
     in.defaultReadObject();
     initTransient();
}
/**
 * Removes the specified listener from the internal list.
 *
 * <p> Does nothing if the listener is not in the list.
 *
 * @param listener
 *        The PropertyChangeListener.
 *
 * @see #addPropertyChangeListener
 *
 */
public void removePropertyChangeListener(PropertyChangeListener listener) {
     changes_.removePropertyChangeListener(listener);
}
/**
 * Removes the specified listener from the internal list.
 *
 * <p> Does nothing if the listener is not in the list.
 *
 * @param listener
 *        The VetoableChangeListener.
 *
 * @see #addVetoableChangeListener
 *
 */
public void removeVetoableChangeListener(VetoableChangeListener listener) {
     vetos_.removeVetoableChangeListener(listener);
}
/**
 * Sets the AS400 system object for the principal.
 *
 * @param system
 *        The AS400 system object.
 *
 * @exception PropertyVetoException
 *        If the change is vetoed.
 *
 */
public void setSystem(AS400 system) throws PropertyVetoException {
     AS400 old = getSystem();
     fireVetoableChange("system", old, system);
     system_ = system;
     firePropertyChange("system", old, system);
}
}
