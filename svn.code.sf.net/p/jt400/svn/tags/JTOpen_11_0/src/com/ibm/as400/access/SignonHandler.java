///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  SignonHandler.java
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
 Specifies the methods required for a SignonHandler.
 The application can direct the system object to use a specific SignonHandler by calling {@link AS400#setSignonHandler setSignonHandler()}.
 The AS400 class invokes the SignonHandler at runtime, if additional information (such as userID or password) must be obtained while attempting to connect to the system.
 By default, the Toolbox uses an internal AWT-based implementation of SignonHandler.

 <p>For all methods that return a boolean, a returned value of <tt>true</tt> indicates that the sign-on should proceed; <tt>false</tt> indicates that the sign-on should not proceed, in which case the system object will throw an {@link AS400SecurityException AS400SecurityException} with an error code indicating what information is missing or incorrect.  In the case of {@link #connectionInitiated connectionInitiated()} and {@link #passwordAboutToExpire passwordAboutToExpire()}, the return code will be {@link AS400SecurityException#SIGNON_CANCELED SIGNON_CANCELED}.

 <p>Suggestions for implementers:
 <br>Extend {@link SignonHandlerAdapter SignonHandlerAdapter} rather than implementing this interface directly.  That will insulate your implementation from future additions to the interface.
 <br>In order to avoid <b>hang conditions</b>, the SignonHandler should not attempt to display a GUI if {@link AS400#isGuiAvailable isGuiAvailable()} indicates <tt>false</tt>.
 <br>In order to avoid <b>infinite loops</b>, a SignonHandler must not call the following AS400 methods:
 <ul>
 <li>addPasswordCacheEntry()
 <li>authenticate()
 <li>connectService()
 <li>validateSignon()
 </ul>

<p>Here is a minimal implementation that just prints a message when connectionInitiated() is called.:
<pre>
import com.ibm.as400.access.SignonHandlerAdapter;
import com.ibm.as400.access.SignonEvent;
public class SimpleSignonHandler extends SignonHandlerAdapter
{
  public boolean connectionInitiated(SignonEvent event)
    throws SignonHandlerException
  {
    System.out.println("SimpleSignonHandler.connectionInitiated()");
    return true;  // indicate that the sign-on should proceed
  }
}
</pre>

<p>Here is a somewhat more realistic sample implementation:
<pre>
import com.ibm.as400.access.*;
import java.io.File;
public class MySignonHandler extends SignonHandlerAdapter
{
  public boolean connectionInitiated(SignonEvent event)
  {
    AS400 system = (AS400)event.getSource();
    if (system.isGuiAvailable())
    {
      // Display an interactive dialog to prompt user for userid and password.
      ...
      system.setUserId(userId);
      system.setPassword(password);
    }
    else  // no GUI available
    {
      File myPasswordFile = new File(...);  // file containing sign-on information
      if (myPasswordFile.exists())
      {
        // Read systemName, userId, and password from file, and update the system object.
        ...
        system.setUserId(userId);
        system.setPassword(password);
      }
      else
      {
        // Just return 'true'.  Let the system object proceed with the connection.
        // If anything necessary is missing, the Toolbox will call handleEvent().
      }
    }
    return true;  // indicate that the sign-on should proceed
  }
}
</pre>
 @see AS400#setSignonHandler
 @see AS400#getSignonHandler
 @see AS400#setDefaultSignonHandler
 @see AS400#getDefaultSignonHandler
 **/
public interface SignonHandler
{

  /**
   Informs the SignonHandler that a connection operation has been initiated.
   The SignonHandler inspects the state of the system object (the source of the event), and calls the appropriate setter methods on the system object to fill in or correct fields.
   <p>In order to <b>avoid infinite loops</b>, a SignonHandler must not call the following AS400 methods:
   <ul>
   <li>addPasswordCacheEntry()
   <li>authenticate()
   <li>connectService()
   <li>validateSignon()
   </ul>
   @param event The sign-on event.
   @param forceUpdate <tt>true</tt> indicates that the sign-on information is known to be incomplete or incorrect. <tt>false</tt> indicates that the information may be correct.
   @return true if sign-on should proceed, false if sign-on should not proceed.
   @see SignonEvent#getSource
   **/
  public boolean connectionInitiated(SignonEvent event, boolean forceUpdate);


  /**
   Handles an exception that was thrown during a sign-on attempt.
   If the handler cannot deal with the exception, the handler should rethrow <tt>exc</tt>.
   @param event The sign-on event.  {@link SignonEvent#getException getException()} is guaranteed to return non-null.
   @exception AS400SecurityException If the handler cannot handle the exception.
   @see AS400SecurityException#getReturnCode
   **/
  public void exceptionOccurred(SignonEvent event) throws AS400SecurityException;


  /**
   Handles the situation where the password is within a few days of expiring.
   A typical implementation is to put up a warning message, ask the user if they want to
   change the password, and if so, solicit a new password and call {@link AS400#changePassword changePassword}.
   Another reasonable implementation is to just return true, indicating
   that the password is not to be changed at this time, and the sign-on should proceed.
   @param event The sign-on event.
   @param daysUntilExpiration The number of days until the password expires.
   @return true if sign-on should proceed, false if sign-on should not proceed.
   @see AS400#changePassword
   **/
  public boolean passwordAboutToExpire(SignonEvent event, int daysUntilExpiration);


  /**
   Handles the situation where the password has expired.
   The typical implementation is to solicit the user for old and new passwords,
   and call {@link AS400#changePassword changePassword}.
   @param event The sign-on event.
   @return true if sign-on should proceed, false if sign-on should not proceed.
   @see AS400#changePassword
   **/
  public boolean passwordExpired(SignonEvent event);


  /**
   Handles the situation where an incorrect password has been specified.
   @param event The sign-on event.
   @return true if sign-on should proceed, false if sign-on should not proceed.
   @see AS400#setPassword
   **/
  public boolean passwordIncorrect(SignonEvent event);


  /**
   Handles the situation where a specified password is either too long or too short.
   @param event The sign-on event.
   @return true if sign-on should proceed, false if sign-on should not proceed.
   @see AS400#setPassword
   **/
  public boolean passwordLengthIncorrect(SignonEvent event);


  /**
   Handles the situation where a password has not been specified.
   @param event The sign-on event.
   @return true if sign-on should proceed, false if sign-on should not proceed.
   @see AS400#setPassword
   **/
  public boolean passwordMissing(SignonEvent event);


  /**
   Handles the situation where the system name has not been specified.
   @param event The sign-on event.
   @return true if sign-on should proceed, false if sign-on should not proceed.
   @see AS400#setSystemName
   **/
  public boolean systemNameMissing(SignonEvent event);


  /**
   Handles the situation where the specified system name is unknown to the network.
   @param event The sign-on event.
   @param exc The exception.
   @return true if sign-on should proceed, false if sign-on should not proceed.
   @see AS400#setSystemName
   **/
  public boolean systemNameUnknown(SignonEvent event, UnknownHostException exc);


  /**
   Handles the situation where a default userID has already been assigned for the system object.
   A typical implementation is simply to put up a warning message.
   Another reasonable implementation is simply to return true, indicating
   that the sign-on should proceed.
   @param event The sign-on event.
   @param defaultUser The current default user.
   @return true if sign-on should proceed, false if sign-on should not proceed.
   @see AS400#isUseDefaultUser
   @see AS400#setUseDefaultUser
   @see AS400#setDefaultUser
   @see AS400#removeDefaultUser
   **/
  public boolean userIdDefaultAlreadyAssigned(SignonEvent event, String defaultUser);


  /**
   Handles the situation where the specified user profile will be disabled after next incorrect sign-on attempt.
   This usually indicates that several successive incorrect sign-on attempts have occurred.
   @param event The sign-on event.
   @return true if sign-on should proceed, false if sign-on should not proceed.
   @see AS400#setUserId
   @see AS400#setPassword
   **/
  public boolean userIdAboutToBeDisabled(SignonEvent event);


  /**
   Handles the situation where the specified user profile has been disabled.
   The application may choose to specify a different userID, or re-enable the user profile.
   @param event The sign-on event.
   @return true if sign-on should proceed, false if sign-on should not proceed.
   @see AS400#setUserId
   **/
  public boolean userIdDisabled(SignonEvent event);


  /**
   Handles the situation where a specified userID is either too long or too short.
   @param event The sign-on event.
   @return true if sign-on should proceed, false if sign-on should not proceed.
   @see AS400#setUserId
   **/
  public boolean userIdLengthIncorrect(SignonEvent event);


  /**
   Handles the situation where a userID has not been specified.
   @param event The sign-on event.
   @return true if sign-on should proceed, false if sign-on should not proceed.
   @see AS400#setUserId
   **/
  public boolean userIdMissing(SignonEvent event);


  /**
   Handles the situation where a specified userID is unknown to the system.
   @param event The sign-on event.
   @return true if sign-on should proceed, false if sign-on should not proceed.
   @see AS400#setUserId
   **/
  public boolean userIdUnknown(SignonEvent event);

}
