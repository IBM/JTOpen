////////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: ProfileTokenCredential.java
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
import java.beans.PropertyVetoException;
import java.util.Random;

/**
 * The ProfileTokenCredential class represents an IBM i system profile token.
 *
 * <p> A profile token provides a timed credential representing an
 * authenticated system user profile and password. A profile token can be
 * established in either a remote (not running on the target system) or local
 * (running on the target system) context. Once created, it may be
 * serialized or distributed as required by the application.
 *
 * <p> When referenced from a running process on the associated IBM i system,
 * a profile token can be used to modify or <i>swap</i> the thread
 * identity and perform a specific task or tasks on behalf of the
 * authenticated user. However, a profile token generated on one IBM i
 * system cannot be used to swap thread identity on any other system.
 *
 * <p> An application of this support would be in a single tier
 * application running on the system when a designated operation must be
 * run under the system authorities and permissions of a specific
 * user profile. A profile token can be used to swap identity prior
 * to performing the operation. Support is also provided to swap
 * back to the original identity upon completion.
 *
 * <p> Another application of this support might be in a two tier
 * application, with authentication of a user profile and password being
 * performed by a graphical user interface on the first tier (i.e. a PC) and
 * work being performed for that user on the second tier (the IBM i system).
 * By utilizing ProfileTokenCredentials, the application can avoid directly
 * passing the user ID and password over the network. The profile token can
 * be distributed as required to the program on the second tier, which can
 * perform the <i>swap()</i> and run designated operations under the
 * system authorities and permissions assigned to the user.
 *
 * <p> <b>Note:</b> While inherently more secure than passing a user profile
 * and password due to limited life span, profile tokens should still be
 * considered sensitive information by the application and handled accordingly.
 * Since the token represents an authenticated user and password, it could
 * potentially be exploited by a hostile application to perform work on
 * behalf of that user. It is ultimately the responsibility of the
 * application to ensure that credentials are accessed in a secure manner.
 *
 * <p> Profile tokens are only supported for IBM i systems
 * at release V4R5M0 or greater.
 *
 * <p> The following example demonstrates the use of a ProfileTokenCredential
 * when run on the <b>local</b> IBM i system. (Note: swap() is an
 * unsupported remote operation.)
 *
 * <pre>
 *  // Prepare to work with the local IBM i system.
 *   AS400 system = new AS400("localhost", "*CURRENT", "*CURRENT");
 *
 *  // Create a single-use ProfileTokenCredential with a 60 second timeout.
 *  // A valid user ID and password must be substituted.
 *   ProfileTokenCredential pt = new ProfileTokenCredential();
 *   pt.setSystem(system);
 *   pt.setTimeoutInterval(60);
 *   pt.setTokenType(ProfileTokenCredential.TYPE_SINGLE_USE);
 *   pt.setTokenExtended("USERID", "PASSWORD");
 *
 *  // Swap the thread identity, retrieving a credential to
 *  // swap back to the original identity later.
 *   AS400Credential cr = pt.swap(true);
 *
 *  // Perform work under the swapped identity at this point.
 *  // Newly-connected AS400 objects will run under the swapped identity.
 *   AS400 swapped = new AS400("localhost", "*CURRENT", "*CURRENT");
 *
 *  // Swap back to the original thread identity.
 *   cr.swap();
 *
 *  // Clean up the credentials.
 *   cr.destroy();
 *   pt.destroy();
 * </pre>
 *
 * <p> General restrictions:
 * <ul>
 * <li> Creating a token updates the last-used date for the associated
 * user and group profiles.</li>
 * <li> Creating a token resets the 'signon attempts not valid' count to
 * zero for the user profile.</li>
 * <li> If security-related events are being audited, creating a token
 * adds an entry to the AUDJRN audit journal.</li>
 * <li> The maximum number of profile tokens that can be generated is
 * approximately 2,000,000; after that, the space to store them is full.
 * Message CPF4AAA is sent to the caller, and no more profile
 * tokens can be generated until one is removed.</li>
 * <li> You cannot obtain a profile token for the following
 * system-supplied user profiles:
 *   <ul>
 *        <li>QAUTPROF
 *        <li>QFNC
 *        <li>QNETSPLF
 *        <li>QSPLJOB
 *        <li>QDBSHR
 *        <li>QGATE
 *        <li>QNFSANON
 *        <li>QSYS
 *        <li>QDFTOWN
 *        <li>QLPAUTO
 *        <li>QRJE
 *        <li>QTCP
 *        <li>QDIRSRV
 *        <li>QLPINSTALL
 *        <li>QSNADS
 *        <li>QTFTP
 *        <li>QDOC
 *        <li>QMSF
 *        <li>QSPL
 *        <li>QTSTRQS
 *        <li>QDSNX
 *   </ul>
 * </li>
 * </ul>
 *
 * <p> Guidelines and restrictions for generating profile tokens based
 * on a specified user profile name and password:
 * <ul>
 * <li> On level 10 systems, only the user ID is validated
 * because no passwords are required.</li>
 * <li> If a provided password is not correct, the incorrect password
 * count for the user profile is increased. (The QMAXSIGN system
 * value contains the maximum number of incorrect attempts to sign on.)</li>
 * <li> If the QMAXSGNACN system value is set to disable
 * user profiles, repeated attempts to validate an incorrect
 * password disables the user ID. This keeps applications
 * from methodically determining user passwords. </li>
 * <li> Considerations when specifying a password of *NOPWD or *NOPWDCHK:
 *   <ul>
 *        <li> To obtain a profile token for a profile that does not have a
 *        password, specify *NOPWD or *NOPWDCHK for the password
 *        parameter.</li>
 *        <li>The user requesting the profile token must have *USE
 *        authority to the user profile.</li>
 *        <li> *NOPWD is not allowed if the user profile name is the name
 *        of the currently running user profile.</li>
 *        <li> If the password is *NOPWDCHK and the user requesting the
 *        profile token has *ALLOBJ and *SECADM special authorities, a
 *        profile token will be generated even when the status of the
 *        profile is disabled or its password is expired.</li>
 *        <li> No profile token is created in the following situations:
 *        <ul>
 *             <li> The user profile is disabled and *NOPWDCHK is not
 *             specified for the password parameter, or *NOPWDCHK was
 *             specified but the user requesting the profile token does
 *             not have *ALLOBJ or *SECADM special authority.</li>
 *             <li> The password is expired and *NOPWDCHK is not
 *             specified for the password parameter, or *NOPWDCHK was
 *             specified but the user requesting the profile token does
 *             not have *ALLOBJ or *SECADM special authority.</li>
 *             <li> The password is *NONE, and *NOPWD or *NOPWDCHK is not
 *             specified for the password parameter.</li>
 *        </ul>
 *        </li>
 *   </ul>
 * </li>
 * </ul>
 *
 * @see AS400Credential
 *
 */
public final class ProfileTokenCredential extends AS400Credential 
        implements AS400BasicAuthenticationCredential {

    static final long serialVersionUID = 4L;

    private byte[] addr_ = new byte[9]; // Encode/decode adder
    private byte[] mask_ = new byte[7]; // Encode/decode mask
    private byte[] token_ = null; // encoded token
    private int type_ = TYPE_SINGLE_USE;
    private int timeoutInterval_ = 3600;
    private final static int MAX_USERPROFILE_LENGTH = 10;
    private final static int MAX_PASSWORD_LENGTH = 128;

    /**
    ID indicating a single use token.
    **/
    public final static int TYPE_SINGLE_USE = 1;

    /**
    ID indicating a multiple use token that cannot be regenerated.
    **/
    public final static int TYPE_MULTIPLE_USE_NON_RENEWABLE  = 2;

    /**
    ID indicating a multiple use token that can be regenerated.
    **/
    public final static int TYPE_MULTIPLE_USE_RENEWABLE = 3;

    /**
    Indicates the length of a profile token (in bytes)
    **/
    public final static int TOKEN_LENGTH = 32;

    /**
     * Password special value indicating that the current password is 
     * not verified.
     * <p> The user requesting the profile token must have *USE authority 
     * to the user profile.
     * <p> This value is not allowed if the name of the currently running 
     * profile is specified for the user profile name parameter.
     */
    public final static int PW_NOPWD = 1;       //$A2

    /**
     * Password special value indicating that a profile token can be 
     * generated for a profile that is disabled or has an expired password.
     * <p> The user requesting the profile token must have *USE authority to 
     * the user profile.
     * <p> If the profile is disabled, the user requesting the profile token
     * must have *ALLOBJ and *SECADM special authorities to get a token.
     * <p> If the password is expired, the user requesting the profile token
     * must have *ALLOBJ and *SECADM special authorities to get a token.
     * <p> If the requesting user does not have *ALLOBJ and *SECADM special
     * authorities, then the request will be handled as if they had
     * indicated *NOPWD.
     */
    public final static int PW_NOPWDCHK = 2;    //$A2
         
    /**
    * Constructs a ProfileTokenCredential object.
    *
    * <p> The <i>system</i> and <i>token</i> must be set
    * prior to accessing host information or taking
    * action against the credential.
    *
    */
    public ProfileTokenCredential() {
        super();
        new Random().nextBytes(addr_);
        new Random().nextBytes(mask_);
    }

    /**
    * Constructs and initializes a ProfileTokenCredential object.
    *
    * <p> The <i>system</i>, <i>token</i>, <i>tokenType</i>, and
    * <i>timeoutInterval</i> properties are initialized to
    * the specified values.
    *
    * <p> This method allows a credential to be constructed
    * based on an existing token (i.e. previously created using the
    * QSYGENPT system API). It is the responsibility of the
    * application to ensure the <i>tokenType</i> and
    * <i>timeoutInterval</i> are consistent with
    * the specified token value.
    *
    * @param system
    *        The system associated with the credential.
    *
    * @param token
    *        The actual bytes for the token as it exists on the
    *        IBM i system.
    *
    * @param tokenType
    *        The type of token provided.
    *        Possible types are defined as fields on this class:
    *          <ul>
    *             <li>TYPE_SINGLE_USE
    *             <li>TYPE_MULTIPLE_USE_NON_RENEWABLE
    *             <li>TYPE_MULTIPLE_USE_RENEWABLE
    *          </ul>
    *
    * @param timeoutInterval
    *        The number of seconds to expiration, used as the
    *        default value when the token is refreshed (1-3600).
    *
    */
    public ProfileTokenCredential(AS400 system, byte[] token, 
            int tokenType, int timeoutInterval) {
        this();
        try {
            setSystem(system);
            setToken(token);
            setTokenType(tokenType);
            setTimeoutInterval(timeoutInterval);
        }
        catch (PropertyVetoException pve) {
            AuthenticationSystem.handleUnexpectedException(pve);
        }
    }
    
    /**
    * Decode the bytes based on the adder and mask.
    *
    * @return
    *        The decoded byte array.
    *
    */
    private static byte[] decode(byte[] adder, byte[] mask, byte[] bytes) {
        byte[] buf = new byte[bytes.length];
        for (int i = 0; i < bytes.length; i++)
            buf[i] = (byte)(
                (mask[i % mask.length] ^ bytes[i])
                - adder[i % adder.length]);
        return buf;
    }

    /**
    * Encode the bytes based on the adder and mask.
    *
    * @return
    *        The encoded byte array.
    *
    */
    private static byte[] encode(byte[] adder, byte[] mask, byte[] bytes) {
        byte[] buf = new byte[bytes.length];
        for (int i = 0; i < bytes.length; i++)
            buf[i] = (byte)(
                (bytes[i] + adder[i % adder.length])
                ^ mask[i % mask.length]);
        return buf;
    }

    /**
    * Compares the specified Object with the credential
    * for equality.
    *
    * @param o
    *        Object to be compared for equality.
    *
    * @return
    *        true if equal; otherwise false.
    *
    */
    public boolean equals(Object o) {
        if (o == null)
            return false;
        if (this == o)
            return true;
        if (!(o instanceof ProfileTokenCredential))
            return false;
        return
            hashCode() == ((ProfileTokenCredential)o).hashCode();
    }

    /**
    * Returns the number of seconds to expiration assigned
    * when the token was last initialized or refreshed.
    *
    * <p> This value also provides the default value for
    * subsequent <A HREF="#refresh()">refresh</A> attempts.
    *
    * <p> The default value is 3600 (1 hour).
    *
    * @return
    *    The number of seconds.
    *
    */
    public int getTimeoutInterval() {
        return timeoutInterval_;
    }

    /**
    * Returns the actual bytes for the token as it exists
    * on the IBM i system.
    *
    * @return
    *    The token bytes; null if not set.
    *
    */
    public byte[] getToken() {
        if (token_ != null)
            return primitiveGetToken();
        return null;
    }

    /**
    * Returns an integer indicating the type assigned when
    * the token was last initialized or refreshed.
    *
    * <p> This value also provides the default value for
    * subsequent <A HREF="#refresh()">refresh</A> attempts.
    *
    * <p> The default is TYPE_SINGLE_USE.
    *
    * @return
    *        The type of token.
    *        Possible types are defined as fields on this class:
    *          <ul>
    *             <li>TYPE_SINGLE_USE
    *             <li>TYPE_MULTIPLE_USE_NON_RENEWABLE
    *             <li>TYPE_MULTIPLE_USE_RENEWABLE
    *          </ul>
    *
    */
    public int getTokenType() {
        return type_;
    }

    /**
    * Returns a hash code for this credential.
    *
    * @return a hash code for this credential.
    *
    */
    public int hashCode() {
        int hash = 104473;
        if (token_ != null) {
            // Obtain unencrypted form as common base for comparison
            byte[] tkn = getToken();
            for (int i=0; i<tkn.length; i++)
                hash ^= (int)tkn[i];
        }
        hash ^= (type_ ^ 14401);
        hash ^= (timeoutInterval_ ^ 21327);
        hash ^= (isPrivate() ? 15501 : 12003);
        if (getPrincipal() != null)
            hash ^= getPrincipal().hashCode();
        if (getSystem() != null)
            hash ^= getSystem().getSystemName().hashCode();
        return hash;
    }

    /**
    * Returns the name of the class providing an implementation
    * for code delegated by the credential that performs native
    * optimization when running on an IBM i system.
    *
    * @return
    *        The qualified class name for native optimizations;
    *        null if not applicable.
    *
    */
    String implClassNameNative() {
        return "com.ibm.as400.access.ProfileTokenImplNative";
    }

    /**
    * Returns the name of the class providing an implementation
    * for code delegated by the credential when no native
    * optimization is to be performed.
    *
    * @return
    *        The qualified class name.
    *
    */
    String implClassNameRemote() {
        return "com.ibm.as400.security.auth.ProfileTokenImplRemote";
    }

    /**
    * Initializes and validates a credential for the local IBM i system.
    *
    * @param principal
    *        The principal identifying the authenticated user.
    *        If not an instance of AS400Principal, a corresponding
    *        UserProfilePrincipal is generated and assigned.
    *
    * @param password
    *        The password for the authenticated user.
    *
    * @param isPrivate
    *        Indicates whether the credential is considered private.
    *
    * @param isReusable
    *        true if the credential can be used to swap
    *        thread identity multiple times;
    *        otherwise false.
    *
    * @param isRenewable
    *        true if the validity period of the credential
    *        can be programmatically updated or extended;
    *        otherwise false.
    *
    * @param timeoutInterval
    *        The number of seconds to expiration when the credential
    *        is initially created; ignored if the credential
    *        does not expire based on time.
    *
    * @exception Exception
    *        If an exception occurs.
    *
    */
    public void initialize(AS400BasicAuthenticationPrincipal principal,
            String password, boolean isPrivate, boolean isReusable, 
            boolean isRenewable, int timeoutInterval)
        throws Exception {
        if (Trace.isTraceOn())
            Trace.log(Trace.INFORMATION,
                new StringBuffer("Initializing credential >> "
                        ).append(toString()
                        ).append(", for principal >> "
                        ).append(principal.toString()
                        ).append(", isPrivate == "
                        ).append(isPrivate
                        ).append(", isReusable == "
                        ).append(isReusable
                        ).append(", isRenewable == "
                        ).append(isRenewable
                        ).append(", timeoutInterval == "
                        ).append(timeoutInterval
                        ).toString());

        // Validate parameters
        if (isRenewable && !isReusable) {
            Trace.log(Trace.ERROR, "Profile tokens must be multi-use" +
                    " if declared as regenerable.");
            throw new ExtendedIllegalArgumentException("isReusable",
                ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }
        // Assign to the local host system
        AS400 sys = AuthenticationSystem.localHost();
        setSystem(sys);
        // Assign an appropriate principal
        AS400Principal pr =
            (AS400Principal.class.isAssignableFrom(principal.getClass()))
                ? (AS400Principal)principal
                : new UserProfilePrincipal(sys, principal.getUserProfileName());
        setPrincipal(pr);
        // Assign profile token attributes
        private_ = isPrivate;
        setTimeoutInterval(timeoutInterval);
        if (isRenewable) setTokenType(TYPE_MULTIPLE_USE_RENEWABLE);
            else if (isReusable) setTokenType(TYPE_MULTIPLE_USE_NON_RENEWABLE);
                else setTokenType(TYPE_SINGLE_USE);
        // Generate the token
        setTokenExtended(pr, password);
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
    */
    void invalidateProperties() {
        super.invalidateProperties();
        token_ = null;
    }

    /**
    * Indicates if the credential can be refreshed.
    *
    * @return
    *        true if the validity period of the credential
    *        can be programmatically updated or extended
    *        using <i>refresh()</i>; otherwise false.
    *
    * @see #refresh
    */
    public boolean isRenewable() {
        return type_ == TYPE_MULTIPLE_USE_RENEWABLE;
    }

    /**
    * Indicates if the credential can be used multiple
    * times prior to expiration.
    *
    * @return
    *        true if the credential can be used to swap
    *        thread identity multiple times;
    *        otherwise false.
    *
    */
    public boolean isReusable() {
        return type_ == TYPE_MULTIPLE_USE_NON_RENEWABLE ||
            type_ == TYPE_MULTIPLE_USE_RENEWABLE;
    }

    /**
    * Returns the raw bytes for the token represented
    * by the credential, decoding the value in
    * memory.
    *
    * @return
    *    The token bytes.
    *
    */
    private byte[] primitiveGetToken() {
        return decode(addr_, mask_, token_);
    }

    /**
    * Sets the raw bytes for the token represented
    * by the credential, encoding the value in
    * memory.
    *
    * @param bytes
    *        The token bytes.
    *
    */
    private void primitiveSetToken(byte[] bytes) {
        token_ = encode(addr_, mask_, bytes);
    }

    /**
    * Updates or extends the validity period for the credential.
    *
    * <p> Does nothing if the credential cannot be
    * programmatically updated or extended.
    *
    * <p> Otherwise, generates a new profile token based
    * on the previously established <i>token</i>, <i>type</i>,
    * and <i>timeoutInterval</i>.
    *
    * @exception AS400SecurityException
    *        If an IBM i system security or authentication error occurs.
    *
    */
    public void refresh() throws AS400SecurityException {
        refresh(getTokenType(), getTimeoutInterval());
    }

    /**
    * Updates or extends the validity period for the credential.
    *
    * <p> Does nothing if the credential cannot be
    * programmatically updated or extended.
    *
    * <p> Otherwise, generates a new profile token based on
    * the previously established <i>token</i> with the
    * given <i>type</i> and <i>timeoutInterval</i>.
    *
    * <p> If successful, the specified type and interval
    * become the default values for future refresh
    * attempts.
    *
    * <p> This method is provided to handle cases where it is
    * desirable to allow for a more restrictive type of token
    * or a different timeout interval when a new token is
    * generated during the refresh.
    *
    * @param type
    *        The type of token.
    *        Possible types are defined as fields on this class:
    *          <ul>
    *             <li>TYPE_SINGLE_USE
    *             <li>TYPE_MULTIPLE_USE_NON_RENEWABLE
    *             <li>TYPE_MULTIPLE_USE_RENEWABLE
    *          </ul>
    *
    * @param timeoutInterval
    *        The number of seconds before expiration (1-3600).
    *
    * @exception AS400SecurityException
    *        If an IBM i system security or authentication error occurs.
    *
    * @exception ExtendedIllegalArgumentException
    *        If a parameter value is out of range.
    *
    */
    public void refresh(int type, int timeoutInterval) 
            throws AS400SecurityException {
        // Check permissions
        checkAuthenticationPermission("refreshCredential");
        // Check status
        if (!isRenewable())
            return;
        // Validate parms
        if (type < 1 || type > 3) {
            Trace.log(Trace.ERROR, "Token type " + type + " out of range");
            throw new ExtendedIllegalArgumentException(
                "type", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
        }
        if (timeoutInterval < 1 || timeoutInterval > 3600) {
            Trace.log(Trace.ERROR, "Timeout interval " + timeoutInterval + " out of range");
            throw new ExtendedIllegalArgumentException(
                "timeoutInterval", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
        }
        // Refresh the credential
        byte[] old = getToken();
        byte[] bytes = ((ProfileTokenImpl)getImpl()).refresh(type, timeoutInterval);

        primitiveSetToken(bytes);
        type_ = type;
        timeoutInterval_ = timeoutInterval;

        fireRefreshed();
        firePropertyChange("token", old, bytes);
        if (Trace.isTraceOn())
            Trace.log(Trace.INFORMATION,
                new StringBuffer("Credential refreshed with type "
                        ).append(type
                        ).append(" and timeoutInterval = "
                        ).append(timeoutInterval
                        ).append(" >> "
                        ).append(toString()
                        ).toString());
    }

    /**
    * Sets the number of seconds to expiration when the
    * token is generated or refreshed.
    *
    * <p> It is the application's responsibility to maintain
    * consistency between explicitly set token values (those
    * not generated from a user and password) and the
    * <i>tokenType</i> and <i>timeoutInterval</i>.
    *
    * <p> This property cannot be changed once a request
    * initiates a connection for the object to the
    * IBM i system (for example, refresh).
    *
    * @param seconds
    *    The number of seconds to expiration (1-3600).
    *
    * @exception PropertyVetoException
    *        If the change is vetoed.
    *
    * @exception ExtendedIllegalArgumentException
    *        If the provided value is out of range.
    *
    * @exception ExtendedIllegalStateException
    *        If the property cannot be changed due
    *        to the current state.
    *
    */
    public void setTimeoutInterval(int seconds) throws PropertyVetoException {
        // Validate state
        validatePropertyChange("timeoutInterval");

        // Validate parms
        if (seconds < 1 || seconds > 3600) {
            Trace.log(Trace.ERROR, "Number of seconds " + seconds + " out of range");
            throw new ExtendedIllegalArgumentException(
                "seconds", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
        }

        Integer old = new Integer(timeoutInterval_);
        Integer sec = new Integer(seconds);
        fireVetoableChange("timeoutInterval", old, sec);
        timeoutInterval_ = seconds;
        firePropertyChange("timeoutInterval", old, sec);
    }

    /**
    * Sets the actual bytes for the token as it exists
    * on the IBM i system.
    *
    * <p> This method allows a credential to be constructed
    * based on an existing token (i.e. previously created using the
    * QSYGENPT system API). It is the responsibility of the
    * application to ensure the <i>tokenType</i> and
    * <i>timeoutInterval</i> are consistent with
    * the specified token value.
    *
    * <p> This property cannot be changed once a request
    * initiates a connection for the object to the
    * IBM i system (for example, refresh).
    *
    * @param bytes
    *        The token bytes.
    *
    * @exception PropertyVetoException
    *        If the change is vetoed.
    *
    * @exception ExtendedIllegalArgumentException
    *        If the provided value is not the correct
    *        length.
    *
    * @exception ExtendedIllegalStateException
    *        If the property cannot be changed due
    *        to the current state.
    *
    */
    public void setToken(byte[] bytes) throws PropertyVetoException {
        // Validate state
        validatePropertyChange("token");

        // Validate parms
        if (bytes == null) {
            Trace.log(Trace.ERROR, "Token byte array is null");
            throw new ExtendedIllegalArgumentException(
                "bytes", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }
        if (bytes.length != TOKEN_LENGTH) {
            Trace.log(Trace.ERROR, "Token of length " + bytes.length + 
                " not valid ");
            throw new ExtendedIllegalArgumentException(
                "bytes", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
        }

        byte[] old = getToken();
        fireVetoableChange("token", old, bytes);
        primitiveSetToken(bytes);
        firePropertyChange("token", old, bytes);
    }

    /**
    * Sets the token bytes based on the provided principal and password.
    *
    * <p> The <i>system</i> property must be set prior to
    * invoking this method.
    *
    * <p> If successful, this method results in a new token being created
    * on the IBM i system. The new token is generated using the
    * previously established <i>tokenType</i> and <i>timeoutInterval</i>
    * settings.
    *
    * <p> This property cannot be changed once a request
    * initiates a connection for the object to the
    * IBM i system (for example, refresh).
    *
    * @deprecated As of V5R3, replaced 
    * by {@link #setTokenExtended(AS400Principal,String)} for password strings
    * or {@link #setToken(AS400Principal,int)} for password special values.
    *
    * @param principal
    *        The principal identifying the user profile for
    *        which the token is to be generated.
    *
    * @param password
    *        The user profile password. The following special values are allowed:
    *        <ul>
    *        <li>*NOPWD - The password is not verified. This value is not
    *        allowed if the name of the currently running profile is
    *        specified for the <i>name</i> parameter. If specified, the user
    *        requesting the profile token must have *USE authority to
    *        the user profile.</li>
    *        <li>*NOPWDCHK - The password is not verified. This value allows
    *        a profile token to be generated for a profile that is disabled
    *        or has an expired password. If disabled or expired, the user
    *        requesting the profile token must have *ALLOBJ and *SECADM
    *        special authority.</li>
    *        </ul>
    *
    * @exception AS400SecurityException
    *        If an IBM i system security or authentication error occurs.
    *
    * @exception PropertyVetoException
    *        If the change is vetoed.
    *
    * @exception ExtendedIllegalArgumentException
    *        If errors occur during parameter validation.
    *
    * @exception ExtendedIllegalStateException
    *        If the token cannot be initialized due
    *        to the current state.
    *
    */
    public void setToken(AS400Principal principal, String password)
            throws PropertyVetoException, AS400SecurityException {
        setToken(principal.getUserProfileName(), password);
    }

    /**
    * Sets the token bytes based on the provided user profile and password.
    *
    * <p> The <i>system</i> property must be set prior to
    * invoking this method.
    *
    * <p> If successful, this method results in a new token being created
    * on the IBM i system. The new token is generated using the
    * previously established <i>tokenType</i> and <i>timeoutInterval</i>
    * settings.
    *
    * <p> This property cannot be changed once a request
    * initiates a connection for the object to the
    * IBM i system (for example, refresh).
    *
    * @deprecated As of V5R3, replaced
    * by {@link #setTokenExtended(String,String)} for password strings
    * or {@link #setToken(String,int)} for password special values.
    *
    * @param name
    *        The name of the user profile for which the token
    *        is to be generated.
    *
    * @param password
    *        The user profile password. The following special values are allowed:
    *        <ul>
    *        <li>*NOPWD - The password is not verified. This value is not
    *        allowed if the name of the currently running profile is
    *        specified for the <i>name</i> parameter. If specified, the user
    *        requesting the profile token must have *USE authority to
    *        the user profile.</li>
    *        <li>*NOPWDCHK - The password is not verified. This value allows
    *        a profile token to be generated for a profile that is disabled
    *        or has an expired password. If disabled or expired, the user
    *        requesting the profile token must have *ALLOBJ and *SECADM
    *        special authority.</li>
    *        </ul>
    *
    * @exception AS400SecurityException
    *        If an IBM i system security or authentication error occurs.
    *
    * @exception PropertyVetoException
    *        If the change is vetoed.
    *
    * @exception ExtendedIllegalArgumentException
    *        If errors occur during parameter validation.
    *
    * @exception ExtendedIllegalStateException
    *        If the token cannot be initialized due
    *        to the current state.
    *
    */
    public void setToken(String name, String password) 
            throws PropertyVetoException, AS400SecurityException {
        // Validate state
        validatePropertySet("system", getSystem());

        // Validate name and password parameters
        if (name == null) {
            Trace.log(Trace.ERROR, "User profile name is null");
            throw new ExtendedIllegalArgumentException(
                "name", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }
        if (name.length() > MAX_USERPROFILE_LENGTH) {
            Trace.log(Trace.ERROR, "User profile name exceeds" + 
                    " maximum allowed length");
            throw new ExtendedIllegalArgumentException(
                "name", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
        }
        if (password == null) {
            Trace.log(Trace.ERROR, "User profile password is null");
            throw new ExtendedIllegalArgumentException(
                "password", 
                ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }

        // Instantiate a new impl but do not yet set as the default impl_
        ProfileTokenImpl impl = (ProfileTokenImpl)getImplPrimitive();

        // Generate and set the token value
        setToken(
            impl.generateToken(
                name,
                password,
                getTokenType(),
                getTimeoutInterval()));

        // If successful, all defining attributes are now set.
        // Set the impl for subsequent references.
        setImpl(impl);

        // Indicate that a new token was created.
        fireCreated();
    }

    //$A2
    /**
    * Sets the token bytes based on the provided principal and 
    * special value for a password.
    *
    * <p> This method requires a special value to be specified for 
    * the user password parameter. If you need to validate a user password, 
    * see the {@link #setTokenExtended(AS400Principal, String)}.
    *
    * <p> The <i>system</i> property must be set prior to invoking this method.
    *
    * <p> If successful, this method results in a new token being created
    * on the IBM i system. The new token is generated using the
    * previously established <i>tokenType</i> and <i>timeoutInterval</i>
    * settings.
    *
    * <p> This property cannot be changed once a request
    * initiates a connection for the object to the
    * IBM i system (for example, refresh).
    *
    * @param principal
    *        The principal identifying the user profile for
    *        which the token is to be generated.
    *
    * @param passwordSpecialValue
    *        The special value for the user profile password. 
    *        The following special values are allowed:
    *        <ul>
    *        <li>PW_NOPWD - The password is not verified. This value is not
    *        allowed if the name of the currently running profile is
    *        specified for the <i>name</i> parameter. If specified, the user
    *        requesting the profile token must have *USE authority to
    *        the user profile.</li>
    *        <li>PW_NOPWDCHK - The password is not verified. This value allows
    *        a profile token to be generated for a profile that is disabled
    *        or has an expired password. If specified, the user
    *        requesting the profile token must have *USE authority to
    *        the user profile. If disabled or expired, the user
    *        requesting the profile token must have *ALLOBJ and *SECADM
    *        special authority.</li>
    *        </ul>
    *
    * @exception AS400SecurityException
    *        If an IBM i system security or authentication error occurs.
    *
    * @exception PropertyVetoException
    *        If the change is vetoed.
    *
    * @exception ExtendedIllegalArgumentException
    *        If errors occur during parameter validation.
    *
    * @exception ExtendedIllegalStateException
    *        If the token cannot be initialized due
    *        to the current state.
    *
    */
    public void setToken(AS400Principal principal, int passwordSpecialValue) 
            throws PropertyVetoException, AS400SecurityException {
        setToken(principal.getUserProfileName(), passwordSpecialValue);
    }

    //$A2
    /**
    * Sets the token bytes based on the provided user profile and 
    * special value for a password.
    *
    * <p> This method requires a special value to be specified for 
    * the user password parameter. If you need to validate a user 
    * password, see the {@link #setTokenExtended(String, String)}.
    * 
    * <p> The <i>system</i> property must be set prior to invoking this method.
    *
    * <p> If successful, this method results in a new token being created
    * on the IBM i system. The new token is generated using the
    * previously established <i>tokenType</i> and <i>timeoutInterval</i>
    * settings.
    *
    * <p> This property cannot be changed once a request
    * initiates a connection for the object to the
    * IBM i system (for example, refresh).
    *
    * @param name
    *        The name of the user profile for which the token
    *        is to be generated.
    *
    * @param passwordSpecialValue
    *        The special value for the user profile password. 
    *        The following special values are allowed:
    *        <ul>
    *        <li>PW_NOPWD - The password is not verified. This value is not
    *        allowed if the name of the currently running profile is
    *        specified for the <i>name</i> parameter. If specified, the user
    *        requesting the profile token must have *USE authority to
    *        the user profile.</li>
    *        <li>PW_NOPWDCHK - The password is not verified. This value allows
    *        a profile token to be generated for a profile that is disabled
    *        or has an expired password. If specified, the user
    *        requesting the profile token must have *USE authority to
    *        the user profile. If disabled or expired, the user
    *        requesting the profile token must have *ALLOBJ and *SECADM
    *        special authority.</li>
    *        </ul>
    *
    * @exception AS400SecurityException
    *        If an IBM i system security or authentication error occurs.
    *
    * @exception PropertyVetoException
    *        If the change is vetoed.
    *
    * @exception ExtendedIllegalArgumentException
    *        If errors occur during parameter validation.
    *
    * @exception ExtendedIllegalStateException
    *        If the token cannot be initialized due
    *        to the current state.
    *
    */
    public void setToken(String name, int passwordSpecialValue) 
            throws PropertyVetoException, AS400SecurityException {
        // Validate state
        validatePropertySet("system", getSystem());

        // Validate name
        if (name == null) {
            Trace.log(Trace.ERROR, "User profile name is null");
            throw new ExtendedIllegalArgumentException(
                "name", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }
        if (name.length() > MAX_USERPROFILE_LENGTH) {
            Trace.log(Trace.ERROR, "User profile name exceeds " + 
                    "maximum allowed length");
            throw new ExtendedIllegalArgumentException(
                "name", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
        }

        // Validate special value for password
        switch (passwordSpecialValue) {
	    case PW_NOPWD:
	    case PW_NOPWDCHK:
	        break;
	    default:
	        Trace.log(Trace.ERROR, "Special value for password is not valid");
	        throw new ExtendedIllegalArgumentException(
		            "password", 
		            ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }

        // Instantiate a new impl but do not yet set as the default impl_
        ProfileTokenImpl impl = (ProfileTokenImpl)getImplPrimitive();

        // Generate and set the token value
        setToken(
            impl.generateToken(
                name,
                passwordSpecialValue,
                getTokenType(),
                getTimeoutInterval()));

        // If successful, all defining attributes are now set.
        // Set the impl for subsequent references.
        setImpl(impl);

        // Indicate that a new token was created.
        fireCreated();
    }

    //$A2
    /**
    * Sets the token bytes based on the provided principal and password.
    *
    * <p> The <i>system</i> property must be set prior to
    * invoking this method.
    *
    * <p> If successful, this method results in a new token being created
    * on the IBM i system. The new token is generated using the
    * previously established <i>tokenType</i> and <i>timeoutInterval</i>
    * settings.
    *
    * <p> This property cannot be changed once a request
    * initiates a connection for the object to the
    * IBM i system (for example, refresh).
    *
    * @param principal
    *        The principal identifying the user profile for
    *        which the token is to be generated.
    *
    * @param password
    *        The user profile password. 
    *
    * @exception AS400SecurityException
    *        If an IBM i system security or authentication error occurs.
    *
    * @exception PropertyVetoException
    *        If the change is vetoed.
    *
    * @exception ExtendedIllegalArgumentException
    *        If errors occur during parameter validation.
    *
    * @exception ExtendedIllegalStateException
    *        If the token cannot be initialized due
    *        to the current state.
    *
    */
    public void setTokenExtended(AS400Principal principal, String password) 
            throws PropertyVetoException, AS400SecurityException {
        setTokenExtended(principal.getUserProfileName(), password);
    }

    //$A2
    /**
    * Sets the token bytes based on the provided user profile and password.
    *
    * <p> The <i>system</i> property must be set prior to
    * invoking this method.
    *
    * <p> If successful, this method results in a new token being created
    * on the IBM i system. The new token is generated using the
    * previously established <i>tokenType</i> and <i>timeoutInterval</i>
    * settings.
    *
    * <p> This property cannot be changed once a request
    * initiates a connection for the object to the
    * IBM i system (for example, refresh).
    *
    * @param name
    *        The name of the user profile for which the token
    *        is to be generated.
    *
    * @param password
    *        The user profile password. 
    *
    * @exception AS400SecurityException
    *        If an IBM i system security or authentication error occurs.
    *
    * @exception PropertyVetoException
    *        If the change is vetoed.
    *
    * @exception ExtendedIllegalArgumentException
    *        If errors occur during parameter validation.
    *
    * @exception ExtendedIllegalStateException
    *        If the token cannot be initialized due
    *        to the current state.
    *
    */
    public void setTokenExtended(String name, String password) 
            throws PropertyVetoException, AS400SecurityException {
        // Validate state
        validatePropertySet("system", getSystem());

        // Validate name and password parameters
        if (name == null) {
            Trace.log(Trace.ERROR, "User profile name is null");
            throw new ExtendedIllegalArgumentException("name", 
                ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }
        if (name.length() > MAX_USERPROFILE_LENGTH) {
            Trace.log(Trace.ERROR, "User profile name exceeds " + 
                    "maximum allowed length");
            throw new ExtendedIllegalArgumentException("name",
                ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
        }
        if (password == null) {
            Trace.log(Trace.ERROR, "User profile password is null");
            throw new ExtendedIllegalArgumentException("password",
                ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }

        // Instantiate a new impl but do not yet set as the default impl_
        ProfileTokenImpl impl = (ProfileTokenImpl)getImplPrimitive();

        // Generate and set the token value
        setToken(
            impl.generateTokenExtended(
                name,
                password,
                getTokenType(),
                getTimeoutInterval()));

        // If successful, all defining attributes are now set.
        // Set the impl for subsequent references.
        setImpl(impl);

        // Indicate that a new token was created.
        fireCreated();
    }

    /**
    * Sets the type of token.
    *
    * <p> It is the application's responsibility to maintain
    * consistency between explicitly set token values (those
    * not generated from a user and password) and the
    * <i>tokenType</i> and <i>timeoutInterval</i>.
    *
    * <p> This property cannot be changed once a request
    * initiates a connection for the object to the
    * IBM i system (for example, refresh).
    *
    * @param type
    *        The type of token.
    *        Possible types are defined as fields on this class:
    *          <ul>
    *             <li>TYPE_SINGLE_USE
    *             <li>TYPE_MULTIPLE_USE_NON_RENEWABLE
    *             <li>TYPE_MULTIPLE_USE_RENEWABLE
    *          </ul>
    *
    * @exception PropertyVetoException
    *        If the change is vetoed.
    *
    * @exception ExtendedIllegalArgumentException
    *        If the provided value is out of range.
    *
    * @exception ExtendedIllegalStateException
    *        If the property cannot be changed due
    *        to the current state.
    *
    */
    public void setTokenType(int type) throws PropertyVetoException {
        // Validate state
        validatePropertyChange("tokenType");

        // Validate parms
        if (type < 1 || type > 3) {
            Trace.log(Trace.ERROR, "Token type " + type + " out of range");
            throw new ExtendedIllegalArgumentException(
                "type", ExtendedIllegalArgumentException.RANGE_NOT_VALID);
        }

        Integer old = new Integer(type_);
        Integer typ = new Integer(type);
        fireVetoableChange("tokenType", old, typ);
        type_ = type;
        firePropertyChange("tokenType", old, typ);
    }
    
    /**
    * Returns a string representation of the object
    *
    * @return a string representation of the object.
    */
    public String toString() {
        return new StringBuffer(256
            ).append(super.toString()
            ).append('['
            ).append(getTokenType()
            ).append(','
            ).append(getTimeoutInterval()
            ).append(']'
            ).toString();
    }

    /**
    * Indicates if instances of the class are sufficient
    * by themselves to change the OS thread identity.
    *
    * <p> Typically this behavior is dictated by the type
    * of credential and need not be changed for
    * individual instances.
    *
    * @return
    *        true
    *
    */
    boolean typeIsStandalone() {
        return true;
    }

    /**
    * Indicates if instances of the class will expire based on time.
    *
    * <p> Typically this behavior is dictated by the type
    * of credential and need not be changed for
    * individual instances.
    *
    * @return
    *        true
    */
    boolean typeIsTimed() {
        return true;
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
    * @exception ExtendedIllegalStateException
    *        If a required property is not set.
    *
    */
    void validateProperties() {
        super.validateProperties();
        validatePropertySet("token", getToken());
    }
}
