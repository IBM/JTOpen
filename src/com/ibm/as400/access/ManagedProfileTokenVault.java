///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  ManagedProfileTokenVault.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2009-2009 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import com.ibm.as400.security.auth.ProfileTokenCredential;
import com.ibm.as400.security.auth.ProfileTokenProvider;

/**
 * A vault which contains a profile token credential.  The primary purpose of this class is
 * to offer a standardized way to keep a profile token credential valid, either by refreshing
 * it or by generating a new one, for an indefinite period of time.
 * <p>
 * This class differs from
 * the {@link com.ibm.as400.access.ProfileTokenVault ProfileTokenVault} class in several important ways:
 * <ul>
 * <li>
 *   The vault contains a reference to a {@link com.ibm.as400.security.auth.ProfileTokenCredential ProfileTokenCredential} object.
 *   The ProfileTokenVault only contains a reference to the raw bytes contained within a
 *   provided ProfileToken object.
 * </li>
 * <li>
 *   The profile token contained in the vault is provided by an object that implements
 *   the {@link com.ibm.as400.security.auth.ProfileTokenProvider ProfileTokenProvider} interface.  The profile token provider
 *   is responsible for constructing and returning a profile token with the desired credential characteristics.
 *   The {@link com.ibm.as400.security.auth.DefaultProfileTokenProvider DefaultProfileTokenProvider} provides a default implementation of
 *   the profile token provider interface.
 * </li>
 * <li>
 *   The life span of the profile token is managed by the vault.  This means if the profile
 *   token is close to expiring, the vault will initiate a refresh of the token.  If the profile
 *   token expires, a new profile token is generated using the profile token provider.
 * </li>
 * </ul>
 * <p>
 *
 * <h3>How the profile token is managed</h3>
 *
 * The general approach for managing the profile token is a lazy "check before returning" approach.
 * This class does not perform any on-going polling or background checking on the status of the profile token.
 * Instead, the currency of the profile token is only checked when the underlying token bytes are
 * requested from the vault.  When this currency check is made, there are three possible outcomes:
  * <ol>
 * <li>
 *   The profile token is current, and its time to expiration is greater than the specified refresh
 *   threshold.  This is the simplest case because nothing needs to be done.  The underlying token
 *   bytes are simply returned and the request is satisfied.
 * </li>
 * <li>
 *   The profile token is current, but its time to expiration is less than the specified refresh
 *   threshold.  In this case, the profile token is refreshed using the refresh() method on the
 *   ProfileTokenCredential object.  Once the token has been refreshed, the underlying token bytes
 *   are returned and the request is satisfied.
 * </li>
 * <li>
 *   The profile token has expired.  Once a profile token has expired, it cannot be refreshed.
 *   So in this scenario, the only option is to generate a new profile token.  The new profile
 *   token is generated using the profile token provider supplied during the construction of the
 *   vault.  Once the new profile token has been generated, the underlying token bytes are
 *   returned and the request is satisfied.
 * </li>
 * </ol>
 * <p>
 *
 * <b>Note:</b> It is very important to fully understand the consequences of the third outcome listed above.
 * Because many different classes, directly or indirectly, make use of the credential stored in
 * this vault, it is not possible to predict exactly when the profile token provider may be asked
 * to generate a new profile token.  This is an important point, because generating a profile
 * token on an IBM i system may require special authorities.  Therefore, users of this class
 * must make certain that the profile token provider specified on construction is capable of
 * generating a new profile token in a wide variety of circumstances, including the possibility
 * that the request will be made from a thread that is different from the thread that constructed
 * the original credential vault.  For more information about what authority is required to
 * generate a profile token, please reference the {@link com.ibm.as400.security.auth.ProfileTokenCredential}
 * class documentation.
 *
 * <h3>Refresh Threshold</h3>
 *
 * The vault will decide to refresh a profile token if its time to expiration is less than the
 * refresh threshold.  So the refresh threshold represents the minimum amount of time left for
 * the currency of a profile token before the vault will refresh the token.  This concept is
 * easiest to explain and understand using an example.
 * <p>
 * Let's say we have a vault with a profile token that was created with a timeout interval of
 * 3600 seconds (1 hour), and the refresh threshold for the vault is set to 1200 seconds
 * (20 minutes).  When a request for the token's underlying bytes is made, the vault will query
 * the profile token object to see how much longer the profile token is valid for.  If the profile
 * token is valid for longer than 20 minutes (i.e. the refresh threshold), the underlying bytes
 * of the profile token are simply returned.  If the profile token is valid for less than 20
 * minutes, the token will be refreshed.
 * <p>
 * So why does the vault refresh the token instead of simply allowing it to expire and, at that
 * time, generate a new profile token?  There are several reasons.  The primary reason is if
 * we allowed the profile token to expire, we would leave open a timing window where the bytes
 * returned by the vault may become invalid before they can be used by the class that requested
 * them for use in authenticating with an IBM i system.  The refresh threshold greatly reduces
 * this timing window and, if the delta between the refresh threshold for the vault and the
 * timeout interval for the profile token is set high enough, we can reduce this timing window
 * to such a small degree of probability that it becomes a negligable concern.  Another reason
 * for refreshing an existing profile token is that the performance for a refresh operation
 * should be slightly better than the performance for creating a new profile token from scratch.
 * <p>
 * Because the refresh threshold is used to prevent the timing window where the profile token
 * credential are given but expire before being used, the value for the refresh threshold should
 * be set such that the profile token credential will be current for a generous amount of time
 * after it is extracted from the vault.  The default setting for the refresh threshold is half
 * of the timeout interval for the profile token; so a profile token with a one hour timeout
 * interval will be refreshed once it has less than 30 minutes of time remaining before it expires.
 */
class ManagedProfileTokenVault extends ProfileTokenVault implements Cloneable
{
  /**
   * Constant that indicates the profile token credential managed by the vault
   * should be refreshed every time its raw bytes (i.e. the underlying
   * credential) is requested.
   */
  private static final int REFRESH_TOKEN_EVERY_TIME = -1;

  /**
   * Constant representing the minimum amount of time, in seconds, allowed
   * between a refresh of the profile token credential managed by the vault.
   */
  private static final int MIN_TOKEN_REFRESH_TIME_INTERVAL = 30;

  /**
   * Constant representing the maximum amount of time, in seconds, allowed
   * between a refresh of the profile token credential managed by the vault.
   */
  private static final int MAX_TOKEN_REFRESH_TIME_INTERVAL = (60 * 59); // 59 minutes

  /** The object that provides a new profile token credential for the vault. */
  private ProfileTokenProvider tokenProvider_;

  /** The profile token credential. */
  private ProfileTokenCredential profileToken_;

  /**
   * The amount of time, in seconds, to wait before refreshing the
   * existing profile token credential.  The maximum value for this
   * field is {@link #MAX_TOKEN_REFRESH_TIME_INTERVAL}
   */
  private int refreshThreshold_;

  /**
   * Constructs a ManagedProfileTokenVault object.  A new profile token
   * is generated during the construction of the vault using the specified
   * token provider.  If a new profile token is needed in the future, the
   * same token provider will be used.  The refresh threshold is set to
   * a default value of half the profile token's timeout interval.
   *
   * @param tokenProvider The provider to use when a new profile token needs to be generated
   */
  protected ManagedProfileTokenVault(ProfileTokenProvider tokenProvider) {
    this(tokenProvider, REFRESH_TOKEN_EVERY_TIME);
  }

  /**
   * Constructs a ManagedProfileTokenVault object.  A new profile token
   * is generated during the construction of the vault using the specified
   * token provider.  If a new profile token is needed in the future, the
   * same token provider will be used.  The refresh threshold is set to
   * the value specified by the refreshThreshold parameter.
   *
   * @param tokenProvider The provider to use when a new profile token needs to be generated
   * @param refreshThreshold The refresh threshold, in seconds, for the profile token.  Used
   *                         by the vault to manage the currency of the profile token to
   *                         help ensure it remains current for an indefinite period of time.
   */
  protected ManagedProfileTokenVault(ProfileTokenProvider tokenProvider, int refreshThreshold) {
    super();
    try {
      profileToken_ = tokenProvider.create();
      encodedCredential_ = store(profileToken_.getToken());
      initRefreshThreshold(refreshThreshold == REFRESH_TOKEN_EVERY_TIME ? profileToken_.getTimeoutInterval() / 2 : refreshThreshold);
    }
    catch (AS400SecurityException e) {
      Trace.log(Trace.ERROR, "Error while created ManagedProfileTokenVault.", e);
    }
    tokenProvider_ = tokenProvider;
  }

  /**
   * Internal use only.  Used to construct an empty vault when we are
   * creating a copy of an existing vault.
   */
  private ManagedProfileTokenVault() {
    super();
  }

  /**
   * Returns a copy of this ManagedProfileTokenVault.  The new copy will NOT
   * be an exact copy of this vault.  The characteristics (i.e. refresh
   * threshold and token provider) will be exactly the same, but the profile
   * token itself is not duplicated.  Instead, the new vault copy generates
   * its own profile token using the token provider.  This non-copy of the
   * profile token is required, because the vault must always maintain a 1-to-1
   * mapping between the vault and the profile token it is managing.
   *
   * @return A newly created ManagedProfileTokenVault with the same
   *         characteristics as this one, but with its own uniquely
   *         generated profile token.
   */
  public Object clone()
  {
    ManagedProfileTokenVault vaultClone = (ManagedProfileTokenVault)super.clone();

    synchronized(this)
    {
      //
      // When we duplicate the fields from an existing managed profile token vault,
      // we do NOT duplicate the profile token itself.
      // By design, each managed profile token vault contains its very own
      // profile token.  In order to maintain this 1-to-1 correlation between
      // vault and token, we must create a brand new profile token for
      // the newly created vault.  However, we do copy the refresh threshold
      // and token provider from the existing vault, so both the new vault
      // and the profile token in it will have the same characteristics
      // as the vault we are making a copy of.
      //

      vaultClone.refreshThreshold_ = refreshThreshold_;
      vaultClone.tokenProvider_ = tokenProvider_;

      try {
        ProfileTokenCredential newToken = tokenProvider_.create();
        vaultClone.profileToken_ = newToken;
        vaultClone.encodedCredential_ = store(newToken.getToken());
      }
      catch (AS400SecurityException e) {
        Trace.log(Trace.ERROR, "Error while cloning ManagedProfileTokenVault.", e);
      }
      return vaultClone;
    }
  }

  /**
   * Purges the contents of the vault.  All resources consumed by the
   * credential vault are freed, which means the profile token stored in
   * the vault will be destroyed.  If this method is invoked and the vault
   * is already empty, the method simply returns and no exception is thrown.
   */
  protected synchronized void empty() {
    // Let the super class do any cleanup it needs to
    super.empty();
    disposeOfToken();
  }

  /**
   * Retrieves the raw profile token credential bytes stored in the vault.
   * If the profile token time to expiration is less than the refresh threshold,
   * the profile token will be refreshed before returning its bytes.  If the
   * profile token has expired, a new profile token will be generated using the
   * token provider, and the bytes of the newly generated profile token will
   * be returned.
   *
   * @return The credential bytes for the profile token stored in the vault
   */
  protected synchronized byte[] getClearCredential() {
    // If the vault is empty, build ourselves a new token
    if (isEmpty()) {
      buildToken();
      return resolve(encodedCredential_);
    }

    // We have a profile token in the vault, so check if it is current.
    // If it is not, then we have missed our opportunity to renew it
    // and we will need to start over by creating a brand new token.
    if (!profileToken_.isCurrent()) {
      // The profile token has already expired.  This means we need
      // to start all over by creating a new one.
      buildToken();
      return resolve(encodedCredential_);
    }

    // Check to see how much time is left before the token expires.
    // If there is less than 'refreshThreshold' time left, then
    // renew the token before returning it.
    try {
      if ( (isTimeForRefresh()) && (profileToken_.isRenewable()) ) {
        profileToken_.refresh();
        encodedCredential_ = store(profileToken_.getToken());
      }
    }
    catch (Exception e) {
      // In case of exception, just try to build a brand new token.
      if (Trace.traceOn_) {
        Trace.log(Trace.DIAGNOSTIC, "Error while refreshing profile token.", e);
      }
      buildToken();
    }
    return resolve(encodedCredential_);
  }

  /**
   * Forces the profile token to be refreshed, regardless of how
   * much time is left before it expires.
   */
  protected synchronized void forceRefresh() {
    // See if we have a profile token to refresh.
    if ( (isEmpty()) || (!profileToken_.isRenewable()) ) {
      // No, so just build a new one
      buildToken();
      return;
    }

    try {
      profileToken_.refresh();
      encodedCredential_ = store(profileToken_.getToken());
    }
    catch (Exception e) {
      // In case of exception, just try to build a brand new token.
      if (Trace.traceOn_) {
        Trace.log(Trace.DIAGNOSTIC, "Error while forcefully refreshing profile token.", e);
      }
      buildToken();
    }
  }

  /**
   * {@inheritDoc}
   */
  protected synchronized boolean isEmpty() {
    boolean empty = super.isEmpty();

    if (empty) {
      if (profileToken_ != null) {
        throw new IllegalStateException("Credential vault is empty, but profile token is not null");
      }
    }
    return empty;
  }

  /**
   * Initializes the refresh threshold.
   *
   * @param threshold The refresh threshold, in seconds
   */
  private void initRefreshThreshold(int threshold) {
    // The minimum allowed refresh threshold is 30 seconds.
    // The maximum allowed is 59 minutes.
    if ( (threshold < MIN_TOKEN_REFRESH_TIME_INTERVAL) || (threshold > MAX_TOKEN_REFRESH_TIME_INTERVAL) ) {
      throw new IllegalArgumentException("Refresh threshold must between " +
                                         MIN_TOKEN_REFRESH_TIME_INTERVAL + " and " +
                                         MAX_TOKEN_REFRESH_TIME_INTERVAL + " seconds");
    }
    refreshThreshold_ = threshold;
  }

  /**
   * Unconditionally disposes of the existing profile token,
   * and generates a new profile token using the token provider.
   */
  private void buildToken() {
    try {
      // First dispose of the existing token, if it exists
      disposeOfToken();

      // Next create a new one
      profileToken_ = tokenProvider_.create();

      // Finally, store the bytes of the new token in an encoded form
      encodedCredential_ = store(profileToken_.getToken());
    }
    catch (Exception e) {
      if (Trace.traceOn_) {
        Trace.log(Trace.DIAGNOSTIC, "Error while building profile token.", e);
      }

      // If the build and store of the profile token did not both
      // succeed, then get rid of everything.  This prevents us from
      // getting into a half baked state where the profile token is
      // present but the encoded credential is null (not sure how that
      // scenario would ever happen anyway, but this protects us from
      // it nontheless).
      disposeOfToken();
    }
  }

  /**
   * Unconditionally disposes of the existing profile token.
   */
  private void disposeOfToken() {
    try {
      // Destroy our profile token
      if (profileToken_ != null) {
        profileToken_.destroy();
      }
    }
    catch (Exception e) {
      Trace.log(Trace.ERROR, "Error while disposing of profile token.", e);
    }
    finally {
      profileToken_ = null;
      encodedCredential_ = null;
    }
  }

  /**
   * Determines if it is time to refresh the profile token.  This is decided
   * by comparing the time left until the profile token expires, and the
   * refresh threshold.
   *
   * @return true if the profile token needs to be refreshed, false if it does not.
   *
   * @throws AS400SecurityException If an IBM i system security or authentication error occurs
   */
  private boolean isTimeForRefresh() throws AS400SecurityException {
    if (refreshThreshold_ == REFRESH_TOKEN_EVERY_TIME) {
      return true;
    }
    else if (profileToken_ == null) {
      return true;
    }
    else {
      return (profileToken_.getTimeToExpiration() < refreshThreshold_);
    }
  }
}
