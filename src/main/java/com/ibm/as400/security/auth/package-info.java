/**
 * Provides user profile swapping using IBM i profile token and credential
 * classes.
 *
 * <P>
 * These classes interact with the security services provided by IBM i.
 * Specifically, support is provided to authenticate a user identity, sometimes
 * referred to as a
 * <I>principal</I>, and password against the native IBM i user registry. A
 * credential representing the authenticated user can then be established. You
 * can use the credential to alter the identity of the current IBM i thread to
 * perform work under the authorities and permissions of the authenticated user.
 * In effect, this identity swap results in the thread acting as if a sign-on
 * was performed by the authenticated user.
 * </P>
 *
 * <P>
 * <B>Note:</B> The services to establish and swap credentials are only
 * supported for OS/400 release V4R5M0 or greater.
 * </P>
 *
 * <P>
 * The AS400 class in the com.ibm.as400.access package now provides
 * authentication for a given user profile and password against the IBM i
 * system. You can also retrieve credentials representing authenticated user
 * profiles and passwords for the system. These credentials, known as profile
 * tokens, represent an authenticated user profile and password for a specific
 * system. Profile tokens expire based on time, up to one hour, but can be
 * refreshed in certain cases to provide an extended life span.
 * </P>
 *
 * <P>
 * <B>Note:</B> While inherently more secure than passing a user profile and
 * password due to limited life span, profile tokens should still be considered
 * sensitive information by the application and handled accordingly. Since the
 * token represents an authenticated user and password, it could potentially be
 * exploited by a hostile application to perform work on behalf of that user. It
 * is ultimately the responsibility of the application to ensure that
 * credentials are accessed in a secure manner.
 * </P>
 */
package com.ibm.as400.security.auth;
