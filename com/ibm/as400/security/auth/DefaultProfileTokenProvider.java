///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  DefaultProfileTokenProvider.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2009-2009 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.security.auth;

import java.beans.PropertyVetoException;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.AS400SecurityException;
import com.ibm.as400.access.ExtendedIllegalArgumentException;
import com.ibm.as400.access.ExtendedIllegalStateException;
import com.ibm.as400.access.InternalErrorException;
import com.ibm.as400.access.Trace;

/**
 * A default implementation of the {@link ProfileTokenProvider ProfileTokenProvider} interface.
 */
public class DefaultProfileTokenProvider implements ProfileTokenProvider
{
  private static final int UNINITIALIZED = -1;

  /** The system used by the ProfileTokenCredential class */
  private AS400 system_;

  /** The timeout interval, in seconds, for the profile token this class creates */
  private int timeoutInterval_ = UNINITIALIZED;

  /** The user which the profile token credential is created for (i.e. who the credential represents) */
  private String userId_ = null;

  /** The type of profile token to create */
  private int tokenType_ = UNINITIALIZED;

  /** Any extended information needed to create the profile token */
  private Object extendedInfo_;

  /**
   * Constructs a new DefaultProfileTokenProvider
   */
  public DefaultProfileTokenProvider() {
    this(new AS400());
  }

  /**
   * Constructs a new DefaultProfileTokenProvider.  The specified system
   * will be used during the creation of the profile token credential.
   *
   * @param system The system to use during the creation of the profile token credential.
   */
  public DefaultProfileTokenProvider(AS400 system) {
    setSystem(system);
  }

  /**
   * Retrieves the system that is used during the creation of the profile token credential.
   *
   * @return The system that is used during the creation of the profile token credential.
   *         Returns null if the system has not been set.
   */
  public AS400 getSystem() {
    return system_;
  }

  /**
   * Sets the system that is used during the creation of the profile token credential.
   *
   * @param system The system for the profile token.
   */
  public void setSystem(AS400 system) {
    system_ = system;
  }

  /**
   * Retrieves the timeout interval that is specified during the creation of the profile token credential.
   *
   * @return The timeout interval for the profile token.
   *         Returns -1 if the timeout interval has not been set.
   */
  public int getTimeoutInterval() {
    return timeoutInterval_;
  }

  /**
   * Sets the timeout interval that is specified during the creation of the profile token credential.
   *
   * @param timeoutInterval The timeout interval for the profile token.
   */
  public void setTimeoutInterval(int timeoutInterval) {
    timeoutInterval_ = timeoutInterval;
  }

  /**
   * Retrieves the token type that is specified during the creation of the profile token credential.
   *
   * @return The token type for the profile token.
   *         Returns null if the token type has not been set.
   */
  public int getTokenType() {
    return tokenType_;
  }

  /**
   * Sets the token type that is specified during the creation of the profile token credential.
   * @param tokenType The token type for the profile token.
   * Valid values are:
   * <ul>
   * <li>{@link ProfileTokenCredential#TYPE_SINGLE_USE TYPE_SINGLE_USE}</li>
   * <li>{@link ProfileTokenCredential#TYPE_MULTIPLE_USE_RENEWABLE TYPE_MULTIPLE_USE_RENEWABLE}</li>
   * <li>{@link ProfileTokenCredential#TYPE_MULTIPLE_USE_NON_RENEWABLE TYPE_MULTIPLE_USE_NON_RENEWABLE}</li>
   * </ul>
   *
   * @throws ExtendedIllegalArgumentException If the input token type is not valid
   */
  public void setTokenType(int tokenType)
  {
    if ( (tokenType != ProfileTokenCredential.TYPE_SINGLE_USE) &&
         (tokenType != ProfileTokenCredential.TYPE_MULTIPLE_USE_RENEWABLE) &&
         (tokenType != ProfileTokenCredential.TYPE_MULTIPLE_USE_NON_RENEWABLE) )
    {
      throw new ExtendedIllegalArgumentException("tokenType", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }
    tokenType_ = tokenType;
  }

  /**
   * Retrieves the user ID that is specified during the creation of the profile token credential.
   *
   * @return The user ID for the profile token.
   *         Returns null if the user ID has not been set.
   */
  public String getUserId() {
    return userId_;
  }

  /**
   * Sets the user ID that is used during the creation of the profile token credential.
   *
   * @param userId The user ID for the profile token.
   */
  public void setUserId(String userId)
  {
    if ( (null == userId) || (0 == userId.length()) ) {
      throw new ExtendedIllegalArgumentException("userId", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
    }
    userId_ = userId;
  }

  /**
   * Retrieves any extended information used during the creation of the profile token credential.
   * This extended information may be a password for the profile token user ID, a special
   * value that instructs the profile token to be created in a certain manner, etc.
   *
   * @return Any extended information for the profile token.
   *         Returns null if the extended information has not been set.
   */
  public Object getExtendedInfo() {
    return extendedInfo_;
  }

  /**
   * Sets the password for the user ID that is used during the
   * creation of the profile token credential.
   *
   * @param password The password for the user ID that is used
   *                 during the creation of the profile token credential.
   */
  public void setPassword(String password)
  {
    if (password == null) {
      throw new NullPointerException("password");
    }
    if (password.length() == 0) {
      throw new ExtendedIllegalArgumentException("password", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
    }
    setExtendedInfo(password);
  }

  /**
   * Sets the password special value to be used during the creation of the profile token credential.
   *
   * @param specialValue The special value.  Valid values for this parameter are:
   * <p><ul>
   * <li>{@link ProfileTokenCredential#PW_NOPWD PW_NOPWD}</li>
   * <li>{@link ProfileTokenCredential#PW_NOPWDCHK PW_NOPWDCHK}</li>
   * </ul>
   *
   * @throws ExtendedIllegalArgumentException If the input special value is not valid
   */
  public void setPasswordSpecialValue(int specialValue)
  {
    if ( (specialValue != ProfileTokenCredential.PW_NOPWD) &&
         (specialValue != ProfileTokenCredential.PW_NOPWDCHK) ) {
      throw new ExtendedIllegalArgumentException("specialValue", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }
    setExtendedInfo(new Integer(specialValue));
  }

  /**
   * Sets the extended information used during the creation of the profile token credential.
   *
   * @param extendedInfo The extended information for the profile token.
   */
  protected void setExtendedInfo(Object extendedInfo) {
    // Assume that the caller has verified that the value is non-null.
    extendedInfo_ = extendedInfo;
  }

  /**
   * Creates and returns a new profile token credential.
   * Before calling this method, the following properties must be set: system, timeout interval, user ID, token type, and extended info.
   *
   * @return A newly created profile token credential
   * @throws AS400SecurityException If an IBM i system security or authentication error occurs
   * @throws ExtendedIllegalStateException If a required property has not been set
   * @see ProfileTokenProvider#create()
   */
  public ProfileTokenCredential create() throws AS400SecurityException
  {
    // Verify that all required fields have been set
    checkFieldSet(system_, "system");
    checkFieldSet(timeoutInterval_, "timeoutInterval");
    checkFieldSet(userId_, "userId");
    checkFieldSet(tokenType_, "tokenType");
    checkFieldSet(extendedInfo_, "extendedInfo");

    try {
      ProfileTokenCredential newToken = new ProfileTokenCredential();
      newToken.setSystem(getSystem());
      newToken.setTimeoutInterval(getTimeoutInterval());
      newToken.setTokenType(getTokenType());

      Object extended = getExtendedInfo();

      if (extended instanceof Integer) {
        newToken.setToken(getUserId(), ((Integer)extended).intValue());
      }
      else if (extended instanceof String) {
        newToken.setTokenExtended(getUserId(), (String)extended);
      }
      else {
        throw new ExtendedIllegalStateException("extendedInfo", ExtendedIllegalStateException.UNKNOWN);
      }
      return newToken;
    }
    catch (PropertyVetoException pve) {
      // Because we have internally created a new profile token credential
      // object, there should be no property listeners for it.  Thus it should
      // not be possible for anyone to veto any property changes.  If this
      // somehow happens, we do not understand what is going on.
      Trace.log(Trace.ERROR, "Unexpected PropertyVetoException:", pve);
      throw new InternalErrorException(InternalErrorException.UNEXPECTED_EXCEPTION);
    }
  }

  /**
   * Validate the specified field is set, that is, not null.
   *
   * @param field The field
   * @param fieldName The name of the field to check
   */
  protected final void checkFieldSet(Object field, String fieldName) {
    if (null == field) {
      throw new ExtendedIllegalStateException(fieldName, ExtendedIllegalStateException.PROPERTY_NOT_SET);
    }
  }

  /**
   * Validate the specified field is set, that is, not null.
   *
   * @param field The field
   * @param description The description to use for the exception thrown if the field is not set
   */
  protected final void checkFieldSet(int field, String description) {
    if (field < 0) {
      throw new ExtendedIllegalStateException(description, ExtendedIllegalStateException.PROPERTY_NOT_SET);
    }
  }
}
