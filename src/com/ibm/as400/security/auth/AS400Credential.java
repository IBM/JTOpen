///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: AS400Credential.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2003 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////
package com.ibm.as400.security.auth;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.AS400SecurityException;
import com.ibm.as400.access.ExtendedIllegalArgumentException;
import com.ibm.as400.access.ExtendedIllegalStateException;
import com.ibm.as400.access.Trace;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Vector;

/**
 * Provides an abstract superclass for
 * representations of IBM i system security-related attributes.
 *
 * <p> Credentials may be used for authenticating to system 
 * services, or may simply enable certain actions to
 * be performed.
 *
 * <p> Typical IBM i system credentials include, but are not
 * limited to, profile tokens.
 *
 * <p> This abstract class must be subclassed to provide
 * specific credential functions.  Each subclass can
 * contain credential data that may be security-sensitive.
 * It is the responsibility of each class to provide
 * appropriate access controls to sensitive data.
 *
 * <p> AS400Credential objects generate the following events:
 * <ul>
 * <li>AS400CredentialEvent
 *   <ul>
 *   <li>CR_CREATE
 *   <li>CR_DESTROY
 *   <li>CR_REFRESH
 *   <li>CR_SWAP
 *   </ul>
 * <li>PropertyChangeEvent
 * <li>VetoableChangeEvent
 * </ul>
 */
public abstract class AS400Credential implements java.io.Serializable, AS400SwappableCredential
{
    static final long serialVersionUID = 4L;


   private transient PropertyChangeSupport changes_ ;
   private transient VetoableChangeSupport vetos_;
   private transient Vector listeners_;
   private transient AS400CredentialImpl impl_;
   private transient RefreshAgent rAgent_;

   private AS400 system_ = null;
   private AS400Principal principal_ = null;
   private Boolean renewable_ = null;
   private Boolean standalone_ = null;
   private Boolean timed_ = null;
   boolean private_ = true;

   private static int minVRM_ = 0;

   // Handles loading the appropriate MRI
   private static ResourceBundleLoader_a loader_;                //$A1A

   /**
    ID indicating that automatic refresh has failed.
    **/
   public static final int CR_AUTO_REFRESH_FAILED  = 0;
   /**
    ID indicating that automatic refresh is not a valid operation.
    **/
   public static final int CR_AUTO_REFRESH_NOT_VALID = 1;
   /**
    ID indicating that automatic refresh is started.
    **/
   public static final int CR_AUTO_REFRESH_STARTED = 2;
   /**
    ID indicating that automatic refresh is stopped.
    **/
   public static final int CR_AUTO_REFRESH_STOPPED = 3;


   // Initialize information used to check permissions
   // when running JDK1.2 and the appropriate
   // permission class is available.
   static private String permissionClassName_ = "javax.security.auth.AuthPermission";
   static private Constructor permissionClassConstructor_ = null;
   static private String permissionCheckMethodName_ = "checkPermission";
   static private Method permissionCheckMethod_ = null;
   static 
   {
      java.lang.SecurityManager sm = System.getSecurityManager();
      if ( sm != null ) 
         try
         {
            Class permissionClass_ = Class.forName(permissionClassName_);
            permissionClassConstructor_ = permissionClass_.getConstructor(new Class[] {String.class});
            permissionCheckMethod_ =
            sm.getClass().getMethod(
                                   permissionCheckMethodName_, new Class[] {Class.forName("java.security.Permission")});
         }
         catch ( java.security.AccessControlException acf )
         {
            Trace.log(Trace.WARNING,
                      "Access to permission class is denied by SecurityManager, JAAS permissions will not be checked.", acf);
         }
         catch ( ClassNotFoundException cnf )
         {
            Trace.log(Trace.WARNING,
                      "Unable to resolve permission class, JAAS permissions will not be checked.", cnf);
         }
         catch ( NoClassDefFoundError ncd )
         {
            Trace.log(Trace.WARNING,
                      "Unable to resolve permission class, JAAS permissions will not be checked.", ncd);
         }
         catch ( NoSuchMethodException nsm )
         {
            Trace.log(Trace.WARNING,
                      "Security manager does not implement method '" + permissionCheckMethodName_
                      + "'. JAAS permissions will not be checked.", nsm);
         }
   }
   /**
    * Constructs an AS400Credential object.
    *
    */
   public AS400Credential()
   {
      super();
      initTransient();
   }
   /**
    * Adds a listener to receive credential events.
    *
    * @param listener
    *		The AS400CredentialListener.
    *
    * @see #removeCredentialListener
    *
    */
   public void addCredentialListener(AS400CredentialListener listener)
   {
      if ( listener == null )
      {
         Trace.log(Trace.ERROR, "Parameter 'listener' is null.");
         throw new NullPointerException("listener");
      }
      listeners_.addElement(listener);
   }
   /**
    * Adds a PropertyChangeListener.
    *
    * <p> The specified listener's <b>propertyChange</b>
    * method will be called each time the value of a
    * bound property is changed.
    *
    * @param listener
    *		The PropertyChangeListener.
    *
    * @see #removePropertyChangeListener
    *
    */
   public void addPropertyChangeListener(PropertyChangeListener listener)
   {
      if ( listener == null )
      {
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
    *		The VetoableChangeListener.
    *
    * @see #removeVetoableChangeListener
    *
    */
   public void addVetoableChangeListener(VetoableChangeListener listener)
   {
      if ( listener == null )
      {
         Trace.log(Trace.ERROR, "Parameter 'listener' is null.");
         throw new NullPointerException("listener");
      }
      vetos_.addVetoableChangeListener(listener);
   }
   /**
    * Returns text that can be displayed to prompt for the basic user
    * and password information used to initialize the credential.
    *
    * @return
    *		An array of two Strings. The first string is the text to
    *		prompt for the user name; the second is the text to
    *		prompt for the password.
    *
    */
   public String[] basicAuthenticationPrompt()
   {
      return new String[] { loader_.getCoreText("DLG_USER_ID_LABEL"), loader_.getCoreText("DLG_PASSWORD_LABEL") };   //$A1C
   }
   /**
    * Checks the given permission for the caller.
    *
    * <p> Does nothing if a security manager is not assigned
    * or does not check permissions, or if the permission
    * class is not present.
    *
    * @exception SecurityException
    *		If the caller does not have the permission.
    *
    */
   void checkAuthenticationPermission(String p)
   {
      java.lang.SecurityManager sm = System.getSecurityManager();
      if ( sm != null
           && permissionCheckMethod_ != null
           && permissionClassConstructor_ != null )
      {
         try
         {
            permissionCheckMethod_.invoke(sm,
                                          new Object[] {permissionClassConstructor_.newInstance(
                                                                                               new Object[] {p})});
         }
         catch ( InvocationTargetException ite )
         {
            Trace.log(Trace.DIAGNOSTIC, "Authentication permission check failed: " + p);
            Throwable t = ite.getTargetException();
            if ( t instanceof SecurityException ) throw (SecurityException)t;
            AuthenticationSystem.handleUnexpectedException(t);
         }
         catch ( Exception e )
         {
            AuthenticationSystem.handleUnexpectedException(e);
         }
      }
   }
   /**
    * Destroys the credential by destroying or clearing
    * sensitive information for the credential.
    *
    * @exception AS400SecurityException
    *		If an IBM i system security or authentication error occurs.
    *
    */
   public void destroy() throws AS400SecurityException {
      // Check for the associated permission
      checkAuthenticationPermission("destroyCredential");
      // Halt any automatic refresh in progress
      stopAutomaticRefresh();
      // Destroy any associated implementation object
      if ( impl_ != null )
      {
         impl_.destroy();
         setImpl(null);
      }
      // Invalidate all defining properties
      invalidateProperties();
      // Provide notification
      fireDestroyed();
      if ( Trace.isTraceOn() )
         Trace.log(Trace.INFORMATION,
                   new StringBuffer("Credential destroyed >> "
                                   ).append(toString()).toString());
   }
   /**
    * Called when garbage collection determines that there are
    * no more references to the object.
    *
    * @exception  java.lang.Throwable
    *		If an error occurs.
    */
   protected void finalize() throws Throwable {
      stopAutomaticRefresh();
      super.finalize();
   }
   /**
    * Fires a CR_CREATE event for the credential.
    *
    */
   void fireCreated()
   {
      Vector targets = (Vector)listeners_.clone();
      AS400CredentialEvent event = new AS400CredentialEvent(this, AS400CredentialEvent.CR_CREATE);
      for ( int i=0; i<targets.size(); i++ )
      {
         AS400CredentialListener target = (AS400CredentialListener)targets.elementAt(i);
         target.created(event);
      }
   }
   /**
    * Fires a CR_DESTROY event for the credential.
    *
    */
   void fireDestroyed()
   {
      Vector targets = (Vector)listeners_.clone();
      AS400CredentialEvent event = new AS400CredentialEvent(this, AS400CredentialEvent.CR_DESTROY);
      for ( int i=0; i<targets.size(); i++ )
      {
         AS400CredentialListener target = (AS400CredentialListener)targets.elementAt(i);
         target.destroyed(event);
      }
   }
   /**
    * Report a bound property update to any registered listeners.
    *
    * @param propertyName
    *		The programmatic name of the property that was changed.
    *
    * @param oldValue
    *		The old value of the property.   
    *
    * @param newValue
    *		The new value of the property.
    *
    */
   void firePropertyChange(String propertyName, Object oldValue, Object newValue)
   {
      changes_.firePropertyChange(propertyName, oldValue, newValue);
   }
   /**
    * Fires a CR_REFRESH event for the credential.
    *
    */
   void fireRefreshed()
   {
      Vector targets = (Vector)listeners_.clone();
      AS400CredentialEvent event = new AS400CredentialEvent(this, AS400CredentialEvent.CR_REFRESH);
      for ( int i=0; i<targets.size(); i++ )
      {
         AS400CredentialListener target = (AS400CredentialListener)targets.elementAt(i);
         target.refreshed(event);
      }
   }
   /**
    * Fires a CR_SWAP event for the credential.
    *
    */
   void fireSwapped()
   {
      Vector targets = (Vector)listeners_.clone();
      AS400CredentialEvent event = new AS400CredentialEvent(this, AS400CredentialEvent.CR_SWAP);
      for ( int i=0; i<targets.size(); i++ )
      {
         AS400CredentialListener target = (AS400CredentialListener)targets.elementAt(i);
         target.swapped(event);
      }
   }
   /**
    * Report a vetoable property update to any registered listeners.
    *
    * @param propertyName
    *		The programmatic name of the property that was changed.
    *
    * @param oldValue
    *		The old value of the property.
    *
    * @param newValue
    *		The new value of the property.
    *
    * @exception PropertyVetoException
    *		If the recipient wishes the property change to be rolled back.
    *
    */
   void fireVetoableChange(String propertyName, Object oldValue, Object newValue) throws PropertyVetoException {
      vetos_.fireVetoableChange(propertyName, oldValue, newValue);
   }
   /**
    * Returns the exception resulting from failure of
    * the most recent auto-refresh attempt.
    *
    * <p> Available when the automatic refresh status
    * is CR_AUTO_REFRESH_FAILED.
    *
    * @return
    *		The exception; null if not available.
    *
    * @see #getAutomaticRefreshStatus
    *
    */
   public Throwable getAutomaticRefreshFailure()
   {
      if ( rAgent_ != null )
         return rAgent_.getFailure();
      return null;
   }
   /**
    * Returns the current status of automatic refresh
    * activity for the credential.
    *
    * <p> Possible identifiers are defined as fields on this class:
    *	<ul>
    *	  <li>CR_AUTO_REFRESH_STARTED
    *	  <li>CR_AUTO_REFRESH_STOPPED
    *	  <li>CR_AUTO_REFRESH_FAILED
    *	  <li>CR_AUTO_REFRESH_NOT_VALID
    *	</ul>
    *
    * <p> Automatic refresh is not valid if the credential
    * cannot be programmatically updated or extended, or if
    * the associated AS400 system object is not set or
    * not allowed to start additional threads.
    *
    * <p> If automatic refresh fails, the associated exception is available
    * from the <i>getAutomaticRefreshFailure()</i> method.
    *
    * @return
    *		The integer identifier representing the status.
    *
    * @see #startAutomaticRefresh
    * @see #stopAutomaticRefresh
    * @see #getAutomaticRefreshFailure
    *
    */
   public int getAutomaticRefreshStatus()
   {
      if ( !isRenewable()
           || getSystem() == null
           ||!getSystem().isThreadUsed() )
         return CR_AUTO_REFRESH_NOT_VALID;
      if ( rAgent_ != null )
      {
         if ( rAgent_.getFailure() != null ) return CR_AUTO_REFRESH_FAILED;
         if ( rAgent_.isAlive() ) return CR_AUTO_REFRESH_STARTED;
      }
      return CR_AUTO_REFRESH_STOPPED;
   }
   /**
    * Returns the object providing an implementation
    * for code delegated by the credential.
    *
    * @return AS400CredentialImpl
    *		The object to receive delegated requests.
    *
    * @exception AS400SecurityException
    *		If a security or authority error occurs.
    *
    */
   AS400CredentialImpl getImpl() throws AS400SecurityException {
      if ( impl_ == null )
      {
         validateProperties();
         setImpl(getImplPrimitive());
      }
      return impl_;
   }
   /**
    * Initializes and returns a new implementation object
    * to which the credential's behavior can be delegated.
    *
    * <p> The superclass method creates the impl object
    * and initializes common attributes. Subclasses
    * should override as necessary to provide
    * additional initialization.
    *
    * @exception AS400SecurityException
    *		If a security or authority error occurs.
    *
    */
   AS400CredentialImpl getImplPrimitive() throws AS400SecurityException {
      validateVRM();
      AS400CredentialImpl impl = null;
      try
      {
          try
          {
              impl = (AS400CredentialImpl)Class.forName(implClassName()).newInstance();
          }
          catch (Exception e)
          {
              if (implClassNameNative() != null && implClassName().equals(implClassNameNative()))
              {
                  Trace.log(Trace.DIAGNOSTIC, "Load of native implementation '" + implClassNameNative() + "' failed, attempting to load remote implementation.");
                  impl = (AS400CredentialImpl)Class.forName(implClassNameRemote()).newInstance();
              }
              else
              {
                  throw e;
              }
          }
         // Check impl version
         if ( impl.getVersion() < typeMinImplVersion() )
         {
            // If not sufficient and native, try remote instead
            if ( implClassNameNative() != null &&
                 impl.getClass().getName().equals(implClassNameNative()) )
            {
               Trace.log(Trace.DIAGNOSTIC,
                         new StringBuffer("Native impl '"
                                         ).append(impl.getClass().getName()
                                                 ).append("' found at version "
                                                         ).append(impl.getVersion()
                                                                 ).append(" not sufficient to meet required level "
                                                                         ).append(typeMinImplVersion()
                                                                                 ).append(". Attempting to load remote impl instead."
                                                                                         ).toString());
               impl = (AS400CredentialImpl)Class.forName(implClassNameRemote()).newInstance();
               // If still not sufficient, reset to throw exception
               if ( impl.getVersion() < typeMinImplVersion() )
               {
                  Trace.log(Trace.DIAGNOSTIC,
                            new StringBuffer("Remote impl '"
                                            ).append(impl.getClass().getName()
                                                    ).append("' found at version "
                                                            ).append(impl.getVersion()
                                                                    ).append(" not sufficient to meet required level "
                                                                            ).append(typeMinImplVersion()
                                                                                    ).toString());
                  impl = null;
               }
            }
            else
               impl = null;
            // If both native and remote were not sufficient, throw an exception
            if ( impl == null )
            {
               Trace.log(Trace.DIAGNOSTIC, "Load of implementation for " + this.getClass().getName() + " failed.");
               throw new ExtendedIllegalStateException(
                                                      ExtendedIllegalStateException.IMPLEMENTATION_NOT_FOUND);
            }
         };
         impl.setCredential(this);
      }
      catch ( Exception e )
      {
         Trace.log(Trace.DIAGNOSTIC, "Load of implementation " + implClassName() + " failed.");
         AuthenticationSystem.handleUnexpectedException(e);
      }
      return impl;
   }
   /**
    * Returns the AS400Principal associated with the credential.
    * 
    * @return
    *		The principal associated with the credential;
    *		null if not assigned.
    *
    */
   public AS400Principal getPrincipal()
   {
      return principal_;
   }
   /**
    * Returns the AS400 system object for the credential.
    * 
    * @return
    *		The AS400 system for the credential;
    *		null if not assigned.
    *
    */
   public AS400 getSystem()
   {
      return system_;
   }
   /**
    * Returns the number of seconds before the
    * credential is due to expire.
    *
    * @return
    *		The number of seconds before expiration;
    *		zero (0) if already expired or if the
    *		credential is not identified as expiring
    *		based on time.
    *
    * @exception AS400SecurityException
    *		If an IBM i system security or authentication error occurs.
    *
    */
   public int getTimeToExpiration() throws AS400SecurityException {
      if ( isTimed() )
         return getImpl().getTimeToExpiration();
      return 0;
   }
   /**
    * Returns the name of the class providing an implementation
    * for code delegated by the credential.
    *
    * @return
    *		The qualified class name.
    *
    */
   String implClassName()
   {
      if ( implClassNameNative() != null
           && AuthenticationSystem.isLocal(getSystem()) )
         return implClassNameNative();
      else
         return implClassNameRemote();
   }
   /**
    * Returns the name of the class providing an implementation
    * for code delegated by the credential that performs native
    * optimization when running on an IBM i system.
    *
    * <p> Default is to return null, indicating no native
    * optimization for this credential.
    * 
    * @return
    *		The qualified class name for native optimizations;
    *		null if not applicable.
    *
    */
   String implClassNameNative()
   {
      return null;
   }
   /**
    * Returns the name of the class providing an implementation
    * for code delegated by the credential when no native
    * optimization is to be performed.
    *
    * @return
    *		The qualified class name.
    *
    */
   String implClassNameRemote()
   {
      return "com.ibm.as400.security.auth.AS400CredentialImplRemote";
   }
   /**
    * Initializes transient data.
    *
    * <p> Subclasses should override as necessary to
    * initialize additional class-specific data.
    *
    */
   void initTransient()
   {
      changes_ = new PropertyChangeSupport(this);
      vetos_ = new VetoableChangeSupport(this);
      listeners_ = new Vector();
      rAgent_ = null;
      setImpl(null);
   }
   /**
    * Reset the value of all properties used to define
    * the credential.
    * 
    * <p> These are the values initialized prior to
    * accessing host information for or taking action against
    * the credential and not modified thereafter until
    * the credential is destroyed.
    *
    * <p> Subclasses should override as necessary to
    * invalidate class-specific data.
    *
    */
   void invalidateProperties()
   {
      system_ = null;
      principal_ = null;
      renewable_ = null;
      standalone_ = null;
      timed_ = null;
   }
   /**
    * Indicates whether or not the credential is considered
    * to be in a connected or active state.
    *
    * <p> If connected, defining attributes have been
    * initialized and an object has been assigned to
    * implement delegated behavior.
    *
    * <p> Once connected, the defining attributes cannot be
    * modified until the credential is destroyed. Destroying
    * the credential returns it to an disconnected or
    * inactive state.
    *
    * @return
    *		true if connected; false otherwise.
    *
    */
   boolean isConnected()
   {
      return (impl_ != null);
   }
   /**
    * Indicates if a timed credential is still considered valid
    * for authenticating to associated IBM i system services
    * or performing related actions.
    *
    * @return
    *		true if valid or not timed; false if not valid or if
    *		the operation fails.
    *
    */
   public boolean isCurrent()
   {
      try
      {
         if ( !isDestroyed() )
            return getImpl().isCurrent();
      }
      catch ( AS400SecurityException e )
      {
         
      }
      return false;
   }
   /**
    * Indicates if the credential has been destroyed.
    *
    * <p> The credential is considered destroyed if
    * the contained information is no longer sufficient
    * to access host information for or take action
    * against the credential.
    *
    * @return
    *		true if destroyed; otherwise false.
    *
    */
   public boolean isDestroyed()
   {
      if ( impl_ == null )
         try
         {
            validateProperties();
         }
         catch ( Exception e )
         {
            return true;
         }
      return false;
   }
   /**
    * Indicates whether the credential is considered private.
    *
    * <p> This value can be referenced by authentication services
    * as an indication of when to check permissions or otherwise
    * protect access to sensitive credentials.
    *
    * @return
    *		true if private; false if public.
    */
   public boolean isPrivate()
   {
      return private_;
   }
   /**
    * Indicates if the credential can be refreshed.
    *
    * @return
    *		true if the validity period of the credential
    *		can be programmatically updated or extended
    *		using <i>refresh()</i>; otherwise false.
    *
    * @see #refresh
    */
   public boolean isRenewable()
   {
      if ( renewable_ != null )
         return renewable_.booleanValue();
      return typeIsRenewable();
   }
   /**
    * Indicates if the credential is sufficient by itself
    * to change the OS thread identity.
    *
    * @return
    *		true if the credential can be used to perform
    *		a swap independently (without requiring an
    *		associated principal); otherwise false.
    *
    */
   boolean isStandalone()
   {
      if ( standalone_ != null )
         return standalone_.booleanValue();
      return typeIsStandalone();
   }
   /**
    * Indicates if the credential will expire based on time.
    *
    * @return
    *		true if the credential has been identified
    *		as expiring at the end of a predetermined
    *		time interval; otherwise false.
    *
    */
   public boolean isTimed()
   {
      if ( timed_ != null )
         return timed_.booleanValue();
      return typeIsTimed();
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
    *		The input stream from which to deserialize the object.
    *
    * @exception ClassNotFoundException
    *		If the class being deserialized is not found.
    *
    * @exception IOException   
    *		If an error occurs while communicating with the server.
    *
    */
   private void readObject(java.io.ObjectInputStream in)
   throws ClassNotFoundException, java.io.IOException
   {
      in.defaultReadObject();
      initTransient();
   }
   /**
    * Updates or extends the validity period for the credential.
    *
    * <p> Does nothing if the credential cannot be
    * programmatically updated or extended.
    *
    * @exception AS400SecurityException
    *		If an IBM i system security or authentication error occurs.
    *
    */
   public void refresh() throws AS400SecurityException {
      checkAuthenticationPermission("refreshCredential");
      if ( !isRenewable() )
         return;
      getImpl().refresh();
      fireRefreshed();
      if ( Trace.isTraceOn() )
         Trace.log(Trace.INFORMATION,
                   new StringBuffer("Credential refreshed >> ").append(toString()).toString());
   }
   /**
    * Removes the specified listener from the internal list.
    *
    * <p> Does nothing if the listener is not in the list.
    *
    * @param listener
    *		The AS400CredentialListener.
    *
    * @see #addCredentialListener
    *
    */
   public void removeCredentialListener(AS400CredentialListener listener)
   {
      listeners_.removeElement(listener);
   }
   /**
    * Removes the specified listener from the internal list.
    *
    * <p> Does nothing if the listener is not in the list.
    *
    * @param listener
    *		The PropertyChangeListener.
    *
    * @see #addPropertyChangeListener
    *
    */
   public void removePropertyChangeListener(PropertyChangeListener listener)
   {
      changes_.removePropertyChangeListener(listener);
   }
   /**
    * Removes the specified listener from the internal list.
    *
    * <p> Does nothing if the listener is not in the list.
    *
    * @param listener
    *		The VetoableChangeListener.
    *
    * @see #addVetoableChangeListener
    *
    */
   public void removeVetoableChangeListener(VetoableChangeListener listener)
   {
      vetos_.removeVetoableChangeListener(listener);
   }
   /**
    * Sets the object providing an implementation
    * for code delegated by the credential.
    *
    */
   void setImpl(AS400CredentialImpl impl)
   {
      impl_ = impl;
   }
   /**
    * Indicates if the credential can be refreshed.
    *
    * <p> This property cannot be changed once a request
    * initiates a connection for the object to the
    * IBM i system (for example, refresh).
    *
    * @param b
    *		true if the validity period of the credential
    *		can be programmatically updated or extended
    *		using <i>refresh()</i>; otherwise false.
    *
    * @exception ExtendedIllegalStateException
    *		If the property cannot be changed due
    *		to the current state.
    *
    * @see #refresh
    */
   void setIsRenewable(boolean b)
   {
      validatePropertyChange("isRenewable");
      renewable_ = new Boolean(b);
   }
   /**
    * Indicates if the credential is sufficient by itself
    * to change the OS thread identity.
    *
    * <p> This property cannot be changed once a request
    * initiates a connection for the object to the
    * IBM i system (for example, refresh).
    *
    * @param b
    *		true if the credential can be used to perform
    *		a swap independently (without requiring an
    *		associated principal); otherwise false.
    *
    * @exception ExtendedIllegalStateException
    *		If the property cannot be changed due
    *		to the current state.
    *
    */
   void setIsStandalone(boolean b)
   {
      validatePropertyChange("isStandalone");
      standalone_ = new Boolean(b);
   }
   /**
    * Indicates if the credential will expire based on time.
    *
    * <p> This property cannot be changed once a request
    * initiates a connection for the object to the
    * IBM i system (for example, refresh).
    *
    * @param b
    *		true if the credential has been identified
    *		as expiring at the end of a predetermined
    *		time interval; otherwise false.
    *
    * @exception ExtendedIllegalStateException
    *		If the property cannot be changed due
    *		to the current state.
    *
    */
   void setIsTimed(boolean b)
   {
      validatePropertyChange("isTimed");
      timed_ = new Boolean(b);
   }
   /**
    * Sets the principal associated with the credential.
    * 
    * <p> This property cannot be changed once a request
    * initiates a connection for the object to the
    * IBM i system (for example, refresh).
    *
    * @param p
    *		The principal.
    *
    * @exception PropertyVetoException
    *		If the change is vetoed.
    *
    * @exception ExtendedIllegalStateException
    *		If the property cannot be changed due
    *		to the current state.
    *
    */
   public void setPrincipal(AS400Principal p) throws PropertyVetoException {
      validatePropertyChange("principal");
      AS400Principal old = getPrincipal();
      fireVetoableChange("principal", old, p);
      principal_ = p;
      firePropertyChange("principal", old, p);
   }
   /**
    * Sets the AS400 system object for the credential.
    * 
    * <p> This property cannot be changed once a request
    * initiates a connection for the object to the
    * IBM i system (for example, refresh).
    *
    * @param system
    *		The AS400 system object.
    *
    * @exception PropertyVetoException
    *		If the change is vetoed.
    *
    * @exception ExtendedIllegalStateException
    *		If the property cannot be changed due
    *		to the current state.
    *
    */
   public void setSystem(AS400 system) throws PropertyVetoException {
      validatePropertyChange("system");
      AS400 old = getSystem();
      fireVetoableChange("system", old, system);
      system_ = system;
      firePropertyChange("system", old, system);
   }
   /**
    * Starts automatic refresh for the credential.
    *
    * <p> While this action is designed to automatically
    * refresh the credential at the specified interval,
    * this is subject to current workload and scheduling
    * of the underlying Java Virtual Machine. Calling
    * applications should take this into consideration
    * when defining the refresh interval in relation
    * to the credential's time to expiration, as it may
    * not be possible to revive a credential once it
    * has expired.
    *
    * @param refreshInterval
    *		The number of seconds between refresh attempts.
    *		The first refresh will occur immediately;
    *		the second will occur this many seconds after
    *		the first, and so on.
    *
    * @param maxRefreshes
    *		The maximum number of times to refresh the
    *		credential. A value of negative one (-1)
    *		indicates no maximum.
    *
    * @exception IllegalStateException
    *		If automatic refresh has already been started or
    *		is not a valid operation for the credential.
    * 		Automatic refresh is not valid if the credential
    *		cannot be programmatically updated or extended,
    *		or if the associated AS400 system object is
    *		not allowed to start additional threads.
    *
    * @exception ExtendedIllegalArgumentException
    *		If a parameter value is out of range.
    *
    * @see #refresh
    * @see #getAutomaticRefreshStatus
    * @see #stopAutomaticRefresh
    *
    */
   public void startAutomaticRefresh(int refreshInterval, int maxRefreshes)
   {
      // Verify status
      int s = getAutomaticRefreshStatus();
      if ( s == CR_AUTO_REFRESH_NOT_VALID )
      {
         Trace.log(Trace.ERROR, "Automatic refresh for " + toString() + " not valid.");
         throw new IllegalStateException("automaticRefreshStatus");
      }
      if ( s == CR_AUTO_REFRESH_STARTED )
      {
         Trace.log(Trace.ERROR, "Automatic refresh for " + toString() + " already started.");
         throw new IllegalStateException("automaticRefreshStatus");
      }
      // Validate parms
      if ( refreshInterval <= 0 )
      {
         Trace.log(Trace.ERROR, "Refresh interval " + refreshInterval + " must be > 0.");
         throw new ExtendedIllegalArgumentException(
                                                   "refreshInterval", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
      }
      if ( maxRefreshes < -1 )
      {
         Trace.log(Trace.ERROR, "Maximum number of refreshes " + maxRefreshes + " must be >= -1.");
         throw new ExtendedIllegalArgumentException(
                                                   "maxRefreshes", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
      }
      // Create and start the refresh agent (a thread)
      rAgent_ = new RefreshAgent(this, refreshInterval, maxRefreshes);
      rAgent_.start();
      if ( Trace.isTraceOn() )
         Trace.log(Trace.INFORMATION,
                   new StringBuffer("Automatic refresh started >> "
                                   ).append(toString()).toString());
   }
   /**
    * Stops and clears the state of any automatic refresh
    * in progress for the credential.
    *
    * <p> Does nothing if automatic refresh is not active.
    *
    * @see #startAutomaticRefresh
    * @see #getAutomaticRefreshStatus
    *
    */
   public void stopAutomaticRefresh()
   {
      if ( rAgent_ == null || !rAgent_.isAlive() )
         return;
      rAgent_.stopRefresh();
      rAgent_ = null;
      if ( Trace.isTraceOn() )
         Trace.log(Trace.INFORMATION,
                   new StringBuffer("Automatic refresh stopped >> "
                                   ).append(toString()).toString());
   }
   /**
    * Attempts to swap the current thread identity based on this credential.
    *
    * <p> No return credential is generated.
    *
    * <p> <b>Note:</b> This method affects the running user
    * profile for subsequent native code invocations and any
    * further requests against the assigned IBM i system.
    * Any currently existing AS400 instances, even if defined for the local host
    * and current user, are not affected if connections
    * have been established under the old identity.
    *
    * <p> <b>Note:</b> swap() is supported only when running natively on IBM i.
    * It is unsupported as a remote operation.
    *
    * @exception Exception
    *		If an exception occurs.
    *
    * @see Swapper
    */
   public void swap() throws Exception {
      swap(false);
   }
   /**
    * Attempts to swap the current thread identity based on this credential.
    *
    * <p> <b>Note:</b> This method affects the running user
    * profile for subsequent native code invocations and any
    * further requests against the assigned IBM i system.
    * Any currently existing AS400 instances, even if defined for the local host
    * and current user, are not affected if connections
    * have been established under the old identity.
    *
    * <p> <b>Note:</b> swap() is supported only when running natively on IBM i.
    * It is unsupported as a remote operation.
    *
    * @param returnCredential
    *		Indicates whether a credential should be returned
    *		that is capable of swapping back to the original
    *		thread identity. Not generating a return credential
    *		optimizes performance and avoids any potential
    *		problems in generating the return value.
    *		This parameter is ignored by credentials not
    *		supporting the ability to swap back to the
    *		original thread identity.
    *
    * @return
    *		A credential capable of swapping back to the
    *		original thread identity; classes not supporting this
    *		capability will return null. This value will also
    *		be null if <i>returnCredential</i> is false.
    *
    * @exception AS400SecurityException
    *		If an IBM i system security or authentication error occurs.
    *
    * @see Swapper
    */
   public AS400Credential swap(boolean returnCredential) throws AS400SecurityException {
      // Check for the associated permission
      checkAuthenticationPermission("modifyThreadIdentity");
      // Validate compatibility of principal
      validatePrincipalCompatibility();
      // Swap
      AS400Credential cr = null;
      try
      {
         cr = getImpl().swap(returnCredential);
         // Reset local host information on AuthenticationSystem
         if ( getSystem() != AuthenticationSystem.localHost() )
            AuthenticationSystem.resetLocalHost();
         // Disconnect and reset the state of AS400 services
         getSystem().resetAllServices();
         // Reset the system user ID & password to force re-resolve
         getSystem().setUserId("*CURRENT");
         getSystem().setPassword("*CURRENT");
         // Request a service port to take system out of unset state
         getSystem().getServicePort(AS400.SIGNON);
         // Signal completion
         fireSwapped();
         if ( Trace.isTraceOn() )
            Trace.log(Trace.INFORMATION,
                      new StringBuffer("Credential swapped >> ").append(toString()).toString());
      }
      catch ( PropertyVetoException pve )
      {
         AuthenticationSystem.handleUnexpectedException(pve);
      }
      return cr;
   }
   /**
    * Indicates if instances of the class can be refreshed.
    *
    * <p> Typically this behavior is dictated by the type
    * of credential and need not be changed for
    * individual instances. The superclass answers a
    * default value of false. Subclasses should
    * override as necessary.
    *
    * @return
    *		true if the validity period of instances can be
    *		programmatically updated or extended using
    *		<i>refresh()</i>; otherwise false.
    *
    * @see #refresh
    */
   boolean typeIsRenewable()
   {
      return false;
   }
   /**
    * Indicates if instances of the class are sufficient
    * by themselves to change the OS thread identity.
    *
    * <p> Typically this behavior is dictated by the type
    * of credential and need not be changed for
    * individual instances. The superclass answers a
    * default value of false. Subclasses should
    * override as necessary.
    * 
    * @return
    *		true if instances can be used to perform a
    *		swap independently (without requiring an
    *		associated principal); otherwise false.
    *
    */
   boolean typeIsStandalone()
   {
      return false;
   }
   /**
    * Indicates if instances of the class will expire based on time.
    *
    * <p> Typically this behavior is dictated by the type
    * of credential and need not be changed for
    * individual instances. The superclass answers a
    * default value of false. Subclasses should
    * override as necessary.
    *
    * @return
    *		true if the credential will expire at the end
    *		of a known predetermined time interval;
    *		otherwise false.
    */
   boolean typeIsTimed()
   {
      return false;
   }
   /**
    * Returns the minimum version number for implementations.
    *
    * <p> Used to ensure implementations are sufficient
    * to support this version of the credential.
    *
    * @return
    *		The version number.
    *
    */
   int typeMinImplVersion()
   {
      return 1; //mod 3
   }
   /**
    * The minimum VRM level supported by credentials of
    * this type.
    *
    * <p> Superclass assumes V4R5M0; subclasses may override as necessary.
    *
    * @return
    *		The VRM representation (an int).
    *
    */
   int typeMinVRM()
   {
      if ( minVRM_ == 0 )
         minVRM_ = AS400.generateVRM(4, 5, 0);
      return minVRM_;
   }
   /**
    * Validate compatibility of the assigned principal.
    *
    * <p> If not a standalone credential, this method validates
    * that the principal and credential systems do not conflict.
    * Note: A principal with a system assigned to null is
    * interpreted as an implicit match for the system
    * assigned to the credential.
    *
    * @exception IllegalStateException
    *		If the principal system is not compatible.
    *
    */
   void validatePrincipalCompatibility()
   {
      if ( isStandalone()
           || getPrincipal()==null
           || getPrincipal().getSystem()==null )
         return;
      if ( getPrincipal().getSystem().equals(getSystem()) )
      {
         Trace.log(Trace.ERROR, "Incompatible credential and principal systems.");
         throw new IllegalStateException("system");
      }
   }
   /**
    * Validates that all properties required to define the
    * credential have been set.
    * 
    * <p> These are the values initialized prior to
    * accessing host information for or taking action against
    * the credential and not modified thereafter until
    * the credential is destroyed.
    *
    * <p> Subclasses should override as necessary to
    * validate class-specific data.
    *
    * @exception ExtendedIllegalStateException
    *		If a required property is not set.
    *
    */
   void validateProperties()
   {
      validatePropertySet("system", getSystem());
      if ( !isStandalone() )
         validatePropertySet("principal", getPrincipal());
   }
   /**
    * Validates that the given property can be changed.
    * 
    * <p> Used to verify any values initialized prior to
    * accessing host information or taking action against
    * the credential which cannot be modified thereafter
    * until the credential is destroyed and returned
    * to an inactive state.
    *
    * <p> Performs a simple check of active state,
    * used to centralize exception handling.
    *
    * @param propertyName
    *		The property to be validated.
    *
    * @exception ExtendedIllegalStateException
    *		If the credential is in an connected state,
    *		indicating that attributes defining the credential
    *		cannot be modified.
    *
    */
   void validatePropertyChange(String propertyName)
   {
      if ( isConnected() )
      {
         Trace.log(Trace.ERROR, "Property '" + propertyName + "' not changed (connected=true).");
         throw new ExtendedIllegalStateException(propertyName,
                                                 ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
      }
   }
   /**
    * Validates that the given property has been set.
    *
    * <p> Performs a simple null check, used to
    * centralize exception handling.
    *
    * @param propertyName
    *		The property to validate.
    *
    * @param value
    *		The property value.
    *
    * @exception ExtendedIllegalStateException
    *		If the property is not set.
    *
    */
   void validatePropertySet(String propertyName, Object value)
   {
      if ( value == null )
      {
         Trace.log(Trace.ERROR, "Required property '" + propertyName + "' not set.");
         throw new ExtendedIllegalStateException(
                                                ExtendedIllegalStateException.PROPERTY_NOT_SET);
      }
   }
   /**
    * Validates that an implementation exists to in support
    * of the associated system VRM.
    * 
    * @exception AS400SecurityException
    *		If a security or authority error occurs.
    *
    */
   void validateVRM() throws AS400SecurityException {
      try
      {
        if (getSystem().getVRM() < typeMinVRM() )
        {
          Trace.log(Trace.ERROR, "VRM<" + typeMinVRM());
          throw new AS400AuthenticationException(
                                                 AS400SecurityException.SYSTEM_LEVEL_NOT_CORRECT);
        }
      }
      catch ( Exception ioe )
      {
         AuthenticationSystem.handleUnexpectedException(ioe);
      }
   }

} //end class 
