///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  ToolboxSignonHandler.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2004-2005 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.awt.Frame;
import java.io.IOException;
import java.beans.PropertyVetoException;
import java.net.UnknownHostException;

/**
 The Toolbox's default implementation of the SignonHandler interface.
 The {@link AS400 AS400} class uses a SignonHandler if additional information
 (such as userID or password) must be gathered at runtime
 when connecting to the server.
 **/
final class ToolboxSignonHandler extends SignonHandlerAdapter
{
  private static final boolean DEBUG = false;
  private static final boolean PASSWORD_TRACE = false;

  private String systemName_ = "";
  private String userId_ = "";
  private boolean changingPassword_ = false;



  /**
   Gathers any missing sign-on information.
   If GUI is available, displays an interactive sign-on dialog to the user, with fields for systemName, userID, and password, along with "OK" and "CANCEL" buttons.
   Otherwise, simply returns <tt>true</tt>.
   @param event The sign-on event.
   @param forceUpdate <tt>true</tt> indicates that the sign-on information is known to be incomplete or incorrect. <tt>false</tt> indicates that the information may be correct.
   @return true if sign-on should proceed, false if sign-on should not proceed.
   **/
  public boolean connectionInitiated(SignonEvent event, boolean forceUpdate)
  {
    if (DEBUG) System.out.println("ToolboxSignonHandler.connectionInitiated("+forceUpdate+")");

    return handleSignon((AS400)event.getSource(), forceUpdate, false);
  }


  /**
   Handles an exception that was thrown during a sign-on attempt.
   If the handler cannot deal with the exception, the handler rethrows <tt>exc</tt>.
   @param event The sign-on event.
   @exception AS400SecurityException If the handler cannot handle the exception.
   **/
  public void exceptionOccurred(SignonEvent event)
    throws AS400SecurityException
  {
    if (DEBUG) System.out.println("ToolboxSignonHandler.exceptionOccurred()");

    AS400SecurityException exc = event.getException();
    AS400 system = (AS400)event.getSource();
    if (!system.isGuiAvailable()) throw exc;  // do nothing, rethrow the exception

    handleException(system, exc);
  }


  /**
   Displays a message warning that the password will expire in <tt>daysUntilExpiration</tt> days, and asks the userif they would like to change the password.
   If user clicks OK, then a "change password"
   dialog is displayed, prompting user for old and new passwords.
   Otherwise, simply returns true.
   @param event The sign-on event.
   @return true
   **/
  public boolean passwordAboutToExpire(SignonEvent event, int daysUntilExpiration)
  {
    if (DEBUG) System.out.println("ToolboxSignonHandler.passwordAboutToExpire(" + daysUntilExpiration + ")");

    AS400 system = (AS400)event.getSource();
    if (!system.isGuiAvailable()) return true;  // do nothing, proceed with sign-on

    boolean response = displayMessage(ResourceBundleLoader.substitute(ResourceBundleLoader.getText("DLG_PASSWORD_EXP_WARNING"), Integer.toString(daysUntilExpiration)) + "  " + ResourceBundleLoader.getText("DLG_CHANGE_PASSWORD_PROMPT"), ResourceBundleLoader.getText("DLG_SIGNON_TITLE"), true);
    if (response)
    {
      handlePasswordChange(system); // tolerate cancellation of the password change
    }
    return true;  // do nothing, proceed with sign-on
  }


  /**
   Displays a message indicating that the password has expired, and asks the user
   if they would like to change the password.  If user clicks OK, then a "change password"
   dialog is displayed, prompting user for old and new passwords.
   @param event The sign-on event.
   @return true if user clicks OK, false if user clicks Cancel.
   **/
  public boolean passwordExpired(SignonEvent event)
  {
    if (DEBUG) System.out.println("ToolboxSignonHandler.passwordExpired()");

    AS400 system = (AS400)event.getSource();
    if (!system.isGuiAvailable()) return noGuiAvailable();

    // See if user wants to change password.
    boolean response = displayMessage(ResourceBundleLoader.getText("EXC_PASSWORD_EXPIRED") + "\n" + ResourceBundleLoader.getText("DLG_CHANGE_PASSWORD_PROMPT"), ResourceBundleLoader.getText("DLG_SIGNON_TITLE"), true);
    if (response)  // User wants to change password.
    {
      return handlePasswordChange(system);
    }
    else  // User canceled the password change.
    {
      return false;
    }
  }


  /**
   Displays a message indicating that the password is incorrect,
   and redisplays the sign-on dialog.
   @param event The sign-on event.
   @return true if user clicks OK, false if user clicks Cancel.
   **/
  public boolean passwordIncorrect(SignonEvent event)
  {
    if (DEBUG) System.out.println("ToolboxSignonHandler.passwordIncorrect()");

    AS400 system = (AS400)event.getSource();
    if (!system.isGuiAvailable()) return noGuiAvailable();
    displayMessage(ResourceBundleLoader.getText("EXC_PASSWORD_INCORRECT"),
                   ResourceBundleLoader.getText("DLG_SIGNON_TITLE"));
    return handleSignon(system, true, false);
  }


  /**
   Displays a message indicating that the password length is incorrect,
   and redisplays the sign-on dialog.
   @param event The sign-on event.
   @return true if user clicks OK, false if user clicks Cancel.
   **/
  public boolean passwordLengthIncorrect(SignonEvent event)
  {
    if (DEBUG) System.out.println("ToolboxSignonHandler.passwordLengthIncorrect()");

    AS400 system = (AS400)event.getSource();
    if (!system.isGuiAvailable()) return noGuiAvailable();
    displayMessage(ResourceBundleLoader.getText("EXC_PASSWORD_LENGTH_NOT_VALID"),
                   ResourceBundleLoader.getText("DLG_SIGNON_TITLE"));
    return handleSignon(system, true, false);
  }


  /**
   Displays a message indicating that the password was not specified,
   and redisplays the sign-on dialog.
   @param event The sign-on event.
   @return true if user clicks OK, false if user clicks Cancel.
   **/
  public boolean passwordMissing(SignonEvent event)
  {
    if (DEBUG) System.out.println("ToolboxSignonHandler.passwordMissing()");

    AS400 system = (AS400)event.getSource();
    if (!system.isGuiAvailable()) return noGuiAvailable();
    displayMessage(ResourceBundleLoader.getText("DLG_MISSING_PASSWORD"),
                   ResourceBundleLoader.getText("DLG_SIGNON_TITLE"));
    return connectionInitiated(event, true);
  }


  /**
   Displays a message indicating that the system name was not specified,
   and redisplays the sign-on dialog.
   @param event The sign-on event.
   @return true if user clicks OK, false if user clicks Cancel.
   **/
  public boolean systemNameMissing(SignonEvent event)
  {
    if (DEBUG) System.out.println("ToolboxSignonHandler.systemNameMissing()");

    AS400 system = (AS400)event.getSource();
    if (!system.isGuiAvailable()) return noGuiAvailable();
    displayMessage(ResourceBundleLoader.getText("DLG_MISSING_USERID"), // generic MRI
                   ResourceBundleLoader.getText("DLG_SIGNON_TITLE"));
    return handleSignon(system, true, true);
  }


  /**
   Displays a message indicating that the specified system is unknown,
   and redisplays the sign-on dialog.
   @param event The sign-on event.
   @param exc The exception.
   @return true if user clicks OK, false if user clicks Cancel.
   **/
  public boolean systemNameUnknown(SignonEvent event, UnknownHostException exc)
  {
    if (DEBUG) System.out.println("ToolboxSignonHandler.systemNameUnknown()");

    AS400 system = (AS400)event.getSource();
    if (!system.isGuiAvailable()) return noGuiAvailable();

    displayMessage(ResourceBundleLoader.getText("EXC_SYSTEM_UNKNOWN"),
                   ResourceBundleLoader.getText("DLG_SIGNON_TITLE"));
    return handleSignon(system, true, true);
  }


  /**
   Displays a message indicating that the user profile will be disabled if next sign-on attempt is incorrect,
   and redisplays the sign-on dialog.
   @param event The sign-on event.
   @return true if user clicks OK, false if user clicks Cancel.
   **/
  public boolean userIdAboutToBeDisabled(SignonEvent event)
  {
    if (DEBUG) System.out.println("ToolboxSignonHandler.userIdAboutToBeDisabled()");

    AS400 system = (AS400)event.getSource();
    if (!system.isGuiAvailable()) return noGuiAvailable();
    displayMessage(ResourceBundleLoader.getText("EXC_PASSWORD_INCORRECT_USERID_DISABLE"),
                   ResourceBundleLoader.getText("DLG_SIGNON_TITLE"));
    return handleSignon(system, true, false);
  }


  /**
   Displays a message warning that the system object already has a default user assigned.
   @param event The sign-on event.
   @param defaultUser The current default user.
   @return true
   **/
  public boolean userIdDefaultAlreadyAssigned(SignonEvent event, String defaultUser)
  {
    if (DEBUG) System.out.println("ToolboxSignonHandler.userIdDefaultAlreadyAssigned()");

    // Put up an informational message, and proceed with sign-on.
    displayMessage(ResourceBundleLoader.getText("DLG_DEFAULT_USER_EXISTS"),
                   ResourceBundleLoader.getText("DLG_SIGNON_TITLE"));
    displayMessage(ResourceBundleLoader.getText("DLG_SET_DEFAULT_USER_FAILED"),
                   ResourceBundleLoader.getText("DLG_SIGNON_TITLE"));
    return true;
  }


  /**
   Displays a message indicating that the user profile is disabled,
   and redisplays the sign-on dialog.
   @param event The sign-on event.
   @return true if user clicks OK, false if user clicks Cancel.
   **/
  public boolean userIdDisabled(SignonEvent event)
  {
    if (DEBUG) System.out.println("ToolboxSignonHandler.userIdDisabled()");

    AS400 system = (AS400)event.getSource();
    if (!system.isGuiAvailable()) return noGuiAvailable();
    displayMessage(ResourceBundleLoader.getText("EXC_USERID_DISABLE"),
                   ResourceBundleLoader.getText("DLG_SIGNON_TITLE"));
    return handleSignon(system, true, false);
  }


  /**
   Displays a message indicating that the user ID length is incorrect,
   and redisplays the sign-on dialog.
   @param event The sign-on event.
   @return true if user clicks OK, false if user clicks Cancel.
   **/
  public boolean userIdLengthIncorrect(SignonEvent event)
  {
    if (DEBUG) System.out.println("ToolboxSignonHandler.userIdLengthIncorrect()");

    AS400 system = (AS400)event.getSource();
    if (!system.isGuiAvailable()) return noGuiAvailable();
    displayMessage(ResourceBundleLoader.getText("EXC_USERID_LENGTH_NOT_VALID"),
                   ResourceBundleLoader.getText("DLG_SIGNON_TITLE"));
    return handleSignon(system, true, false);
  }


  /**
   Displays a message indicating that the user name was not specified,
   and redisplays the sign-on dialog.
   @param event The sign-on event.
   @return true if user clicks OK, false if user clicks Cancel.
   **/
  public boolean userIdMissing(SignonEvent event)
  {
    if (DEBUG) System.out.println("ToolboxSignonHandler.userIdMissing()");

    AS400 system = (AS400)event.getSource();
    if (!system.isGuiAvailable()) return noGuiAvailable();
    displayMessage(ResourceBundleLoader.getText("DLG_MISSING_USERID"),
                   ResourceBundleLoader.getText("DLG_SIGNON_TITLE"));
    return handleSignon(system, true, false);
  }


  /**
   Displays a message indicating that the specified user ID is unknown,
   and redisplays the sign-on dialog.
   @param event The sign-on event.
   @return true if user clicks OK, false if user clicks Cancel.
   **/
  public boolean userIdUnknown(SignonEvent event)
  {
    if (DEBUG) System.out.println("ToolboxSignonHandler.userIdUnknown()");

    AS400 system = (AS400)event.getSource();
    if (!system.isGuiAvailable()) return noGuiAvailable();
    displayMessage(ResourceBundleLoader.getText("EXC_USERID_UNKNOWN"),
                   ResourceBundleLoader.getText("DLG_SIGNON_TITLE"));
    return handleSignon(system, true, false);
  }


  //
  //
  // Private utility methods.
  //
  //



  /**
   Displays an informational dialog to the user.
   @param message The message text to display.
   @param dialogTitle The title for the dialog.
   **/
  private void displayMessage(String message, String dialogTitle)
  {
    displayMessage(message, dialogTitle, false);
  }


  /**
   Displays an informational dialog to the user.
   @param message The message text to display.
   @param dialogTitle The title for the dialog.
   @param getResponse Whether to get response from user.
   @return The response from the user.  true if user indicated approval, false otherwise.
   **/
  private boolean displayMessage(String message, String dialogTitle, boolean getResponse)
  {
    MessageDialog md = new MessageDialog(new Frame(), message, dialogTitle, getResponse);
    return md.display();
  }


  /**
   Handles an exception that was thrown during a sign-on attempt.
   If the handler cannot deal with the exception, the handler rethrows <tt>exc</tt>.
   @param system The system.
   @param exc The exception.
   @exception AS400SecurityException If the handler cannot handle the exception.
   **/
  private void handleException(AS400 system, AS400SecurityException exc)
    throws AS400SecurityException
  {
    switch (exc.getReturnCode())
    {
      case AS400SecurityException.USERID_LENGTH_NOT_VALID:
      case AS400SecurityException.PASSWORD_LENGTH_NOT_VALID:
      case AS400SecurityException.USERID_DISABLE:
      case AS400SecurityException.PASSWORD_INCORRECT:
      case AS400SecurityException.PASSWORD_INCORRECT_USERID_DISABLE:
      case AS400SecurityException.SIGNON_REQUEST_NOT_VALID:
      case AS400SecurityException.USERID_UNKNOWN:
      case AS400SecurityException.SIGNON_CHAR_NOT_VALID:

      case AS400SecurityException.PASSWORD_CHANGE_REQUEST_NOT_VALID:
      case AS400SecurityException.PASSWORD_OLD_NOT_VALID:
      case AS400SecurityException.PASSWORD_NEW_NOT_VALID:
      case AS400SecurityException.PASSWORD_NEW_TOO_LONG:
      case AS400SecurityException.PASSWORD_NEW_TOO_SHORT:
      case AS400SecurityException.PASSWORD_NEW_REPEAT_CHARACTER:
      case AS400SecurityException.PASSWORD_NEW_ADJACENT_DIGITS:
      case AS400SecurityException.PASSWORD_NEW_CONSECUTIVE_REPEAT_CHARACTER:
      case AS400SecurityException.PASSWORD_NEW_PREVIOUSLY_USED:
      case AS400SecurityException.PASSWORD_NEW_NO_NUMERIC:
      case AS400SecurityException.PASSWORD_NEW_NO_ALPHABETIC:
      case AS400SecurityException.PASSWORD_NEW_DISALLOWED:
      case AS400SecurityException.PASSWORD_NEW_USERID:
      case AS400SecurityException.PASSWORD_NEW_SAME_POSITION:
      case AS400SecurityException.PASSWORD_NEW_CHARACTER_NOT_VALID:
      case AS400SecurityException.PASSWORD_NEW_VALIDATION_PROGRAM:

        if (changingPassword_)
        {
          displayMessage(exc.getMessage(), ResourceBundleLoader.getText("DLG_CHANGE_PASSWORD_TITLE"));
          if (!handlePasswordChange(system)) throw exc;
        }
        else  // not changing password
        {  // Give user another chance on the sign-on dialog.
          displayMessage(exc.getMessage(), ResourceBundleLoader.getText("DLG_SIGNON_TITLE"));
          if (!handleSignon(system, true, false)) throw exc;
        }
        break;

      case AS400SecurityException.PASSWORD_EXPIRED:

        // See if user wants to change password.
        boolean response = displayMessage(ResourceBundleLoader.getText("EXC_PASSWORD_EXPIRED") + "\n" + ResourceBundleLoader.getText("DLG_CHANGE_PASSWORD_PROMPT"), ResourceBundleLoader.getText("DLG_SIGNON_TITLE"), true);
        if (response)  // User wants to change password.
        {
          if (!handlePasswordChange(system)) throw exc;
        }
        else  // User canceled the password change.
        {  // Fail the sign-on.
          throw exc;
        }
        break;

      default:  // None of the above.  Just display the error text to the user and throw the exception.

        String title;
        if (changingPassword_)
        {
          title = ResourceBundleLoader.getText("DLG_CHANGE_PASSWORD_TITLE");
        }
        else
        {
          title = ResourceBundleLoader.getText("DLG_SIGNON_TITLE");
        }
        displayMessage(exc.getMessage(), title);
        throw exc;
    }
  }


  // Solicits password-change information from the user.  Validates the information, and if valid, calls AS400.changePassword().
  private boolean handlePasswordChange(AS400 system)
  {
    ChangePasswordDialog cpd = new ChangePasswordDialog(new Frame(), ResourceBundleLoader.getText("DLG_CHANGE_PASSWORD_TITLE"));
    boolean changedPassword = false;
    boolean done = false;

    do
    {
      if (!cpd.prompt(system.getSystemName(), system.getUserId()))  // user canceled
      {
        changingPassword_ = false;
        break;
      }
      String oldPassword = cpd.getOldPassword();
      String newPassword = cpd.getNewPassword();
      String confirmPassword = cpd.getConfirmPassword();
      if (PASSWORD_TRACE)
      {
        Trace.log(Trace.DIAGNOSTIC, "Old password: '" + oldPassword + "'");
        Trace.log(Trace.DIAGNOSTIC, "New password: '" + newPassword + "'");
        Trace.log(Trace.DIAGNOSTIC, "Confirm password: '" + confirmPassword + "'");
      }
      if (validatePasswordInfo(oldPassword, newPassword, confirmPassword))
      {
        changingPassword_ = true;
        try {
          system.changePassword(oldPassword, newPassword);
        }
        catch (AS400SecurityException e) {
          displayMessage(e.getMessage(), ResourceBundleLoader.getText("DLG_CHANGE_PASSWORD_TITLE"));
          return handlePasswordChange(system);
        }
        catch (IOException e) {
          displayMessage(e.getMessage(), ResourceBundleLoader.getText("DLG_CHANGE_PASSWORD_TITLE"));
          return handlePasswordChange(system);
        }
        changingPassword_ = false;
        changedPassword = true;
        done = true;
      }

    } while (!done);

    return changedPassword;
  }


  /**
   Gathers any missing sign-on information, and calls the relevant setters in the system object.
   If GUI is available, displays an interactive sign-on dialog to the user, with fields for systemName, userID, and password, along with "OK" and "CANCEL" buttons.
   Otherwise, simply returns <tt>true</tt>.
   @param event The sign-on event.
   @param forceUpdate <tt>true</tt> indicates that the sign-on information is known to be incomplete or incorrect. <tt>false</tt> indicates that the information may be correct.
   @param enableSystemNameField Indicates whether the "system name" field should be enabled for input.
   @return true if sign-on should proceed, false if sign-on should not proceed.
   **/
  private boolean handleSignon(AS400 system, boolean forceUpdate, boolean enableSystemNameField)
  {
    if (DEBUG) System.out.println("ToolboxSignonHandler.handleSignon("+forceUpdate+"," + enableSystemNameField+")");

    if (!forceUpdate &&
        system.getSystemName().length() != 0 &&
        system.getUserId().length() != 0)
    {
      return true;  // assume all the needed info is there
    }

    if (system.isGuiAvailable())
    {
      if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Signing-on with prompting turned on.");
      boolean done = false;
      do
      {
        PasswordDialog pd = setupPasswordDialog(system);
        if (enableSystemNameField) pd.enableSystemNameField();

        if (!pd.prompt()) { // user canceled
          return false;
        }

        // Note: The PasswordDialog getters never return null.
        String systemName = pd.getSystemName().trim();
        String userId = pd.getUserId().trim().toUpperCase();
        String password = pd.getPassword();

        if (validateInfo(systemName, userId, password, system))
        {
          try
          {
            systemName_ = systemName;
            system.setSystemName(systemName_);
            if (userId.length() != 0) {
              userId_ = userId;
              system.setUserId(userId_);
            }
            if (password.length() != 0) system.setPassword(password);

            // Check to see if we should set the default user.
            // Design note: There's a slight usability exposure here, in the event that an erroneous userID is specified.  Then that userID gets set as the default user.  To reset it, the app must first call AS400.removeDefaultUser().
            if (pd.getDefaultState() && userId_.length() != 0)
            {
              if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting default user from dialog...");
              // Set the default user.
              if (!AS400.setDefaultUser(systemName_, userId_))
              {
                if (Trace.traceOn_) Trace.log(Trace.WARNING, "Failed to set default user.");
                if (system.isGuiAvailable())
                {
                  displayMessage(ResourceBundleLoader.getText("DLG_DEFAULT_USER_EXISTS") + "\n\n" + ResourceBundleLoader.getText("DLG_SET_DEFAULT_USER_FAILED"),
                                 ResourceBundleLoader.getText("DLG_SIGNON_TITLE"));
                }
              }
            }

            // Also see if the AS400 should use the cached password.
            if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting password cache entry from dialog...");
            system.setUsePasswordCache(pd.getPasswordCacheState());

            done = true;  // if we got this far, we're good to go
          }
          catch (PropertyVetoException e) {
            displayMessage(e.getMessage(), ResourceBundleLoader.getText("DLG_SIGNON_TITLE"));
            return handleSignon(system, forceUpdate, enableSystemNameField);
          }
        }

      } while (!done);
    }
    else  // no GUI available
    {
      // Just return true.  Let the caller validate it.
      // If anything necessary is missing, they will call our handleEvent() method.
      if (Trace.traceOn_) Trace.log(Trace.INFORMATION, "No GUI is available to sign-on handler.");
    }
    return true;
  }


  // Complains about missing GUI, and returns false.
  private boolean noGuiAvailable()
  {
    Trace.log(Trace.ERROR, "The internal Toolbox sign-on handler requires a GUI in order to gather new information.");
    return false;
  }



  // Sets up a password dialog, to solicit system name, user ID, and password.
  private PasswordDialog setupPasswordDialog(AS400 system)
  {
    PasswordDialog pd = new PasswordDialog(new Frame(), ResourceBundleLoader.getText("DLG_SIGNON_TITLE"), system.isShowCheckboxes());

    // If system name is not set.
    String systemName = system.getSystemName();
    if (systemName.length() == 0) systemName = systemName_;
    if (systemName.length() == 0)
    {
      // Enable default user checkbox.
      pd.enableDefaultUserCheckbox();
      // But uncheck it.
      pd.setDefaultUserState(false);
    }
    else
    {
      // Put system name in dialog.
      pd.setSystemName(systemName);
      systemName_ = systemName;
      // Do we already have a default user for this system
      if (AS400.getDefaultUser(systemName) == null)
      { // No default user yet.
        // Enable the check box.
        pd.enableDefaultUserCheckbox();
        // And check it.
        pd.setDefaultUserState(true);
      }
      else
      {
        // Disable the check box.  There's already a default user for this system.
        pd.disableDefaultUserCheckbox();
      }
    }
    // Check the use cache checkbox.
    pd.setPasswordCacheState(system.isUsePasswordCache());
    // If user ID set, put it in dialog.
    String userId = system.getUserId();
    if (userId.length() == 0) userId = userId_;
    if (userId.length() == 0 && systemName.length() != 0) {
      String defaultUser = AS400.getDefaultUser(systemName);
      if (defaultUser != null) userId = defaultUser;
    }
    if (userId.length() != 0) {
      pd.setUserId(userId);
      userId_ = userId;
    }
    return pd;
  }


  // Checks lengths of systemName, userID, and password.
  private boolean validateInfo(String systemName, String userId, String password, AS400 system)
  {
    // Assume args are already validated as non-null.
    boolean valid = true;
    if (systemName.length() == 0 ||
        (!system.isUseDefaultUser() && userId.length() == 0) ||
        (!system.isUsePasswordCache() && password.length() == 0))
    {
      // A field is not filled in.
      displayMessage(ResourceBundleLoader.getText("DLG_MISSING_PASSWORD"), ResourceBundleLoader.getText("DLG_SIGNON_TITLE"));
      valid = false;
    }
    else if (password.length() > 128)
    {
      Trace.log(Trace.ERROR, "Length of password is greater than 128 characters: " + password.length());
      displayMessage(ResourceBundleLoader.getText("EXC_PASSWORD_LENGTH_NOT_VALID"), ResourceBundleLoader.getText("DLG_SIGNON_TITLE"));
      valid = false;
    }

    return valid;
  }


  // Checks lengths of old/new passwords, and verifies that they match.
  private boolean validatePasswordInfo(String oldPassword, String newPassword, String confirmPassword)
  {
    if (oldPassword.length() == 0 || newPassword.length() == 0 || confirmPassword.length() == 0)
    {
      // A field is not filled in.
      displayMessage(ResourceBundleLoader.getText("DLG_MISSING_PASSWORD"), ResourceBundleLoader.getText("DLG_CHANGE_PASSWORD_TITLE"));
      return false;
    }
    if (!newPassword.equals(confirmPassword))
    {
      // New and confirm are not the same.
      displayMessage(ResourceBundleLoader.getText("EXC_PASSWORD_NOT_MATCH"), ResourceBundleLoader.getText("DLG_CHANGE_PASSWORD_TITLE"));
      return false;
    }
    if (oldPassword.length() > 128)
    {
      Trace.log(Trace.ERROR, "Length of old password is greater than 128 characters: " + oldPassword.length());
      displayMessage(ResourceBundleLoader.getText("EXC_PASSWORD_LENGTH_NOT_VALID"), ResourceBundleLoader.getText("DLG_CHANGE_PASSWORD_TITLE"));
      return false;
    }
    if (newPassword.length() > 128)
    {
      Trace.log(Trace.ERROR, "Length of new password is greater than 128 characters: " + newPassword.length());
      displayMessage(ResourceBundleLoader.getText("EXC_PASSWORD_NEW_NOT_VALID"), ResourceBundleLoader.getText("DLG_CHANGE_PASSWORD_TITLE"));
      return false;
    }

    return true;
  }

}
