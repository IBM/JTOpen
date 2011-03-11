///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  SignonHandlerAdapter.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2004-2005 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.net.UnknownHostException;

/**
 An abstract adapter class for receiving Toolbox sign-on events. The methods in this class perform bare-minimum default processing.  This class exists as a convenience for application writers when creating customized sign-on handler objects.

 <p>Extend this class to create a SignonHandler implementation and override the methods for the events of interest. (If you directly implement the SignonHandler interface, you must provide implementations for all of the SignonHandler methods.  The SignonHandlerAdapter class provides default implementations for all SignonHandler methods, so you only have to implement methods for events you care about.)

 <p>Create a SignonHandler object using your extended class, and then register it with the system object using {@link AS400#setSignonHandler setSignonHandler()}.  When a sign-on related event occurs on the AS400 system object, the relevant method in your handler object is invoked.

 <p>For all methods that return a boolean, returning <tt>true</tt> indicates that the sign-on should proceed; <tt>false</tt> indicates that the sign-on should be terminated.

 <p>In order to avoid <b>hang conditions</b>, the SignonHandler object must not attempt to display a GUI if {@link AS400#isGuiAvailable isGuiAvailable()} indicates <tt>false</tt>.

 <p>In order to avoid <b>infinite loops</b>, a SignonHandler must not call the following AS400 methods:
 <ul>
 <li>addPasswordCacheEntry()
 <li>authenticate()
 <li>connectService()
 <li>validateSignon()
 </ul>

 @see AS400#setSignonHandler
 @see AS400#getSignonHandler
 @see AS400#setDefaultSignonHandler
 @see AS400#getDefaultSignonHandler
 **/
public abstract class SignonHandlerAdapter implements SignonHandler
{
  /**
   Returns <tt>true</tt>, indicating that the sign-on should proceed.
   @param event The sign-on event.
   @param forceUpdate <tt>true</tt> indicates that the sign-on information is known to be incomplete or incorrect. <tt>false</tt> indicates that the information may be correct.
   @return true
   **/
  public boolean connectionInitiated(SignonEvent event, boolean forceUpdate)
  {
    return true;  // Take a chance that everything's OK.  If there's a problem, we'll be notified.
  }


  /**
   Rethrows the exception.
   @param event The sign-on event.
   @exception AS400SecurityException If the handler cannot handle the exception.
   @see AS400SecurityException#getReturnCode
   **/
  public void exceptionOccurred(SignonEvent event) throws AS400SecurityException
  {
    throw event.getException();  // give up
  }


  /**
   Returns <tt>true</tt>, indicating that the sign-on should proceed.
   @param event The sign-on event.
   @param daysUntilExpiration The number of days until the password expires.
   @return true
   @see AS400#changePassword
   **/
  public boolean passwordAboutToExpire(SignonEvent event, int daysUntilExpiration)
  {
    return true;  // don't change password, proceed with sign-on
  }


  /**
   Returns <tt>false</tt>, indicating that the sign-on should not proceed.
   @param event The sign-on event.
   @return false
   @see AS400#changePassword
   **/
  public boolean passwordExpired(SignonEvent event)
  {
    return false;  // give up
  }


  /**
   Returns <tt>false</tt>, indicating that the sign-on should not proceed.
   @param event The sign-on event.
   @return false
   @see AS400#setPassword
   **/
  public boolean passwordIncorrect(SignonEvent event)
  {
    return false;  // give up
  }


  /**
   Returns <tt>false</tt>, indicating that the sign-on should not proceed.
   @param event The sign-on event.
   @return false
   @see AS400#setPassword
   **/
  public boolean passwordLengthIncorrect(SignonEvent event)
  {
    return false;  // give up
  }


  /**
   Returns <tt>false</tt>, indicating that the sign-on should not proceed.
   @param event The sign-on event.
   @return false
   @see AS400#setPassword
   **/
  public boolean passwordMissing(SignonEvent event)
  {
    return false;  // give up
  }


  /**
   Returns <tt>false</tt>, indicating that the sign-on should not proceed.
   @param event The sign-on event.
   @return false
   @see AS400#setSystemName
   **/
  public boolean systemNameMissing(SignonEvent event)
  {
    return false;  // give up
  }


  /**
   Returns <tt>false</tt>, indicating that the sign-on should not proceed.
   @param event The sign-on event.
   @param exc The exception.
   @return false
   @see AS400#setSystemName
   **/
  public boolean systemNameUnknown(SignonEvent event, UnknownHostException exc)
  {
    return false;  // give up
  }


  /**
   Returns <tt>true</tt>, indicating that the sign-on should proceed.
   @param event The sign-on event.
   @param defaultUser The current default user.
   @return true
   @see AS400#isUseDefaultUser
   @see AS400#setUseDefaultUser
   @see AS400#setDefaultUser
   **/
  public boolean userIdDefaultAlreadyAssigned(SignonEvent event, String defaultUser)
  {
    return true;  // proceed with sign-on anyway; don't change default user
  }


  /**
   Returns <tt>false</tt>, indicating that the sign-on should not proceed.
   @param event The sign-on event.
   @return false
   @see AS400#setUserId
   @see AS400#setPassword
   **/
  public boolean userIdAboutToBeDisabled(SignonEvent event)
  {
    return false;  // give up
  }


  /**
   Returns <tt>false</tt>, indicating that the sign-on should not proceed.
   @param event The sign-on event.
   @return false
   @see AS400#setUserId
   **/
  public boolean userIdDisabled(SignonEvent event)
  {
    return false;  // give up
  }


  /**
   Returns <tt>false</tt>, indicating that the sign-on should not proceed.
   @param event The sign-on event.
   @return false
   @see AS400#setUserId
   **/
  public boolean userIdLengthIncorrect(SignonEvent event)
  {
    return false;  // give up
  }


  /**
   Returns <tt>false</tt>, indicating that the sign-on should not proceed.
   @param event The sign-on event.
   @return false
   @see AS400#setUserId
   **/
  public boolean userIdMissing(SignonEvent event)
  {
    return false;  // give up
  }


  /**
   Returns <tt>false</tt>, indicating that the sign-on should not proceed.
   @param event The sign-on event.
   @return false
   @see AS400#setUserId
   **/
  public boolean userIdUnknown(SignonEvent event)
  {
    return false;  // give up
  }


}
