///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  AS400ImplRemote.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1999-2024 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.beans.PropertyVetoException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.ietf.jgss.GSSCredential;

import com.ibm.as400.security.auth.ProfileTokenCredential;
import com.ibm.as400.security.auth.ProfileTokenEnhancedInfo;

/**
 * This is the functional implementation of the AS400Impl interface. While
 * declared as public, it is only intended for the internal use of the driver.
 * 
 */
public class AS400ImplRemote implements AS400Impl
{
  private static boolean PASSWORD_TRACE = false;
  private static final boolean DEBUG = false;
  private static final int UNINITIALIZED = -1;

  // The pool of systems. The systems are in service constant order!
  // The following are not in the pool since these are singleton objects.
  //   -- AS400.SIGNON (7) is special, only one signon server is used. 
  //   -- AS400.HOSTCNN (8) is special, only one hostcnn server is used.
  private Vector[] serverPool_ = {  
      new Vector<AS400Server>(), // AS400.FILE (0)
      new Vector<AS400Server>(), // AS400.PRINT (1)
      new Vector<AS400Server>(), // AS400.COMMAND (2)
      new Vector<AS400Server>(), // AS400.DATAQUEUE (3)
      new Vector<AS400Server>(), // AS400.DATABASE (4)
      new Vector<AS400Server>(), // AS400.RECORDACCESS (5)
      new Vector<AS400Server>()  // AS400.CENTRAL (6)
                                 // AS400.SIGNON (7) - SLOT NOT USED
                                 // AS400.HOSTCNN (8) - SLOT NOT USED
  };

  // System name.
  private String systemName_ = "";
  // User ID.
  private String userId_ = "";
  // Flag indicating if system name refers to local system.
  private boolean systemNameLocal_ = false; 

  // Credential to the IBM i system
  private CredentialVault credVault_ = new PasswordVault(); 

  // GSS Credential object, for Kerberos.
  private GSSCredential gssCredential_ = null;
  // GSS name string, for Kerberos.
  private String gssName_ = "";
  // How to use the GSS framework.
  // int gssOption_; // not used

  // Flag that indicates if we connect to SSL ports.
  private SSLOptions useSSLConnection_ = null;
  // Flag that indicates if we should try to use native optimization.
  private boolean canUseNativeOptimization_ = true;
  // Flag that indicates if we use threads in communication with the host servers.
  private boolean threadUsed_ = true;
  // Flag that indicates if we use virtual threads in communication with the host servers.
  private boolean virtualThreads_ = true;

  // CCSID to use in conversations with the system.
  private int ccsid_ = 0;
  // If the user has told us to override common sense and use the CCSID they want.
  private boolean userOverrideCcsid_ = false;
  // The client NLV.
  private String clientNlv_;
  // Set of socket options to use when creating our connections to the system.
  private SocketProperties socketProperties_ = null;

  // The name of the secondary language library (if any). Used by
  // RemoteCommandImplNative.
  private String languageLibrary_ = null;
  // Flag that gets set by RemoteCommandImplNative to indicate that we should
  // refrain from further attempts to set the secondary language library for
  // this AS400Impl.
  private boolean skipFurtherSettingOfLanguageLibrary_ = false;
  // Flag that gets set by RemoteCommandImplNative to indicate that the V5R4
  // system is missing PTF SI29629 (product 5722SS1).
  private boolean detectedMissingPTF_ = false;

  // IASP name used for DDM, if specified.
  private String ddmRDB_;

  // VRM information from the sign-on or hostcnn server. Retrieved from sign-on connect and
  // stored until placed in sign-on information.
  private ServerVersion version_;
  // System level of the sign-on server. Retrieved from sign-on connect and
  // stored until placed in sign-on information.
  private int serverLevel_;
  // Password level 
  private int passwordLevel_ = 0; // 0-1  == DES, 2-3 == SHA-1.
  // Flag indicating if we have determined the password type yet.
  private boolean isPasswordTypeSet_ = false;
  // Sign-on information retrieved on sign-on information request.
  private SignonInfo signonInfo_;
  // Additional authentication factor indicator
  private boolean aafIndicator_ = false;

  // Dispatcher of connection events from this implementation to public object.
  private ConnectionListener dispatcher_;

  // Single use seed received from public object in exchange seed method, held
  // until next sign-on, change password, or generate profile token.
  private byte[] proxySeed_ = null;
  // Single use seed sent to public object in exchange seed method, held until
  // next sign-on, change password, or generate profile token.
  private byte[] remoteSeed_ = null;
  private int userHandle_ = UNINITIALIZED;

  // Connection to the sign-on server.
  private AS400NoThreadServer signonServer_;
  // Connection to the hostcnn server. Note that this object may be shared across AS400's!!
  private AS400NoThreadServer hostcnnServer_;
  
  // Sign-on server server seed, held from sign-on connection until sign-on disconnect.
  private byte[] serverSeed_;
  // Sign-on server client seed, held from sign-on connection until sign-on disconnect.
  private byte[] clientSeed_;
  
  // hostcnn server server seed, held from hostcnn connection until hostcnn disconnect.
  private byte[] hostcnn_serverSeed_;
  // hostcnn server client seed, held from hostcnn connection until hostcnn disconnect.
  private byte[] hostcnn_clientSeed_;
  
  // Additional authentication factor. We have to hold on to it because it may be timed, and thus can be reused. 
  private byte[] additionalAuthFactor_;
  
  // Profile handles used by swapTo / swapBack. The swapToPH should always match userID to be authenticated
  // In 7.6 a single handle is used and not released
  private byte[] swapToPH76_ =  null;  
  private String swapToPHUserID76_ = null;
  // In 7.5 and earlier, each thread gets a new handle to swap to and the handle is released
  private ThreadLocal<byte[]> threadLocalSwapToPH_ =  new ThreadLocal<byte[]>();  
  private ThreadLocal<String> threadLocalSwapToPHUserID_ = new ThreadLocal<String>();
  // The swapFromPH_ allows a thread to return to the original profile after a swap is completed.
  // This is stored using ThreadLocal storage
  private ThreadLocal<byte[]> threadLocalSwapFromPH_ = new ThreadLocal<byte[]>();
  private AtomicInteger swapToPHRefCount_ = null;
  
  private String localIPAddress_ = null;   /* The IP address from the last opened socket */ 
  private boolean localIPAddressSet_ = false; 
  
  private static final String CLASSNAME = "com.ibm.as400.access.AS400ImplRemote";

  // GSS Token, for Kerberos.
  private byte[] kerbTicket_;

  static {
      if (Trace.traceOn_)
          Trace.logLoadPath(CLASSNAME);
      
      // Identify all remote command server reply data streams.
      // Reply data streams need to be added so that sendAndReceive operations
      // know what class to use for the response. 
      AS400Server.addReplyStream(new ChangePasswordRep(), AS400.SIGNON);
      AS400Server.addReplyStream(new AS400StrSvrReplyDS(), AS400.SIGNON);
      AS400Server.addReplyStream(new SignonGenAuthTokenReplyDS(), AS400.SIGNON);
      AS400Server.addReplyStream(new AS400GenAuthTknReplyDS(), AS400.SIGNON);
      AS400Server.addReplyStream(new AS400XChgRandSeedReplyDS(), AS400.SIGNON);
      AS400Server.addReplyStream(new SignonInfoRep(), AS400.SIGNON);
      AS400Server.addReplyStream(new SignonExchangeAttributeRep(), AS400.SIGNON);
      AS400Server.addReplyStream(new IFSUserHandleSeedRep(), AS400.FILE);
      AS400Server.addReplyStream(new IFSCreateUserHandleRep(), AS400.FILE);
      AS400Server.addReplyStream(new IFSUserHandle2Rep(), AS400.FILE);
      AS400Server.addReplyStream(new SignonExchangeAttributeRep(), AS400.HOSTCNN);
      AS400Server.addReplyStream(new HCSUserInfoReplyDS(), AS400.HOSTCNN);
      AS400Server.addReplyStream(new HCSGetNewConnReplyDS(), AS400.HOSTCNN);
      AS400Server.addReplyStream(new HCSPrepareNewConnReplyDS(), AS400.HOSTCNN);
      AS400Server.addReplyStream(new HCSRouteNewConnReplyDS(), AS400.HOSTCNN);
      AS400Server.addReplyStream(new PingReplyDS(), AS400.HOSTCNN);

    
      if (DEBUG)
          AS400Server.addReplyStream(new IFSReturnCodeRep(), AS400.FILE);
  }

  // Set the connection event dispatcher.
  @Override
  public void addConnectionListener(ConnectionListener listener)
  {
      if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Adding implementation connection listener.");
      dispatcher_ = listener;
  }

  // Indicates if the native optimizations code can be used.
  // Provided for internal use by other ImplRemote classes.
  boolean canUseNativeOptimizations() {
      return canUseNativeOptimization_;
  }

  // Map from CCSID to encoding string.
  @Override
  public String ccsidToEncoding(int ccsid)
  {
    if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Mapping to encoding implementation, CCSID:", ccsid);
    return ConversionMaps.ccsidToEncoding(ccsid);
  }

  // Convenience method for removing trailing space from SHA-1 passwords.
  private static char[] trimUnicodeSpace(char[] inputArray)
  {
      if (inputArray.length == 0)
          return inputArray;
      
      char lastChar = inputArray[inputArray.length - 1];
      if (lastChar != '\u0000' && lastChar != '\u0020' && lastChar != '\u3000')
          return inputArray;

      int trimPosition = inputArray.length - 1;
      while (    inputArray[trimPosition] == '\u0000'
              || inputArray[trimPosition] == '\u0020'
              || inputArray[trimPosition] == '\u3000')
          --trimPosition;
      
      char[] trimedArray = new char[trimPosition + 1];
      System.arraycopy(inputArray, 0, trimedArray, 0, trimedArray.length);
      return trimedArray;
  }

  // Convenience method for generating SHA-1 password token.
  private static byte[] generateShaToken(byte[] userIdBytes, byte[] bytes)
  {
      try
      {
          MessageDigest md = MessageDigest.getInstance("SHA");
          md.update(userIdBytes);
          md.update(bytes);
          byte[] token = md.digest();
          md.reset();
          
          byte[] empty = new byte[userIdBytes.length+bytes.length];
          Arrays.fill(empty, (byte) 0);
          md.update(empty);

          if (PASSWORD_TRACE)
              Trace.log(Trace.DIAGNOSTIC, "SHA-1 token:", token);
          return token;
      }
      catch (NoSuchAlgorithmException e)
      {
          Trace.log(Trace.ERROR, "Error getting instance of SHA-1 algorithm:", e);
          throw new InternalErrorException(InternalErrorException.UNEXPECTED_EXCEPTION, e);
      }
  }

  // Convenience method for generating SHA-1 substitute password.
  private static byte[] generateShaSubstitute(byte[] token, byte[] serverSeed,
      byte[] clientSeed, byte[] userIdBytes, byte[] sequence)
  {
      try
      {
          MessageDigest md = MessageDigest.getInstance("SHA");
          md.update(token);
          md.update(serverSeed);
          md.update(clientSeed);
          md.update(userIdBytes);
          md.update(sequence);
          byte[] substitutePassword = md.digest();
          md.reset(); 

          
          if (PASSWORD_TRACE)
              Trace.log(Trace.DIAGNOSTIC, "SHA-1 substitute:", substitutePassword);

          return substitutePassword;
      }
      catch (NoSuchAlgorithmException e)
      {
          Trace.log(Trace.ERROR, "Error getting instance of SHA-1 algorithm:", e);
          throw new InternalErrorException(InternalErrorException.UNEXPECTED_EXCEPTION, e);
      }
  }

  // Convenience method for generating SHA-1 substitute password.
  private static byte[] generateShaProtected(byte[] bytes, byte[] token,
      byte[] serverSeed, byte[] clientSeed, byte[] userIdBytes, byte[] sequence)
  {
      // Protected length will be rounded up to next 20.
      int protectedLength = (((bytes.length - 1) / 20) + 1) * 20;
      byte[] protectedPassword = new byte[protectedLength];

      for (int i = 0; i < protectedLength; i += 20)
      {
          incrementString(sequence);
          byte[] encryptedSection = generateShaSubstitute(token, serverSeed, clientSeed, userIdBytes, sequence);
          for (int ii = 0; ii < 20; ++ii)
          {
              if (i + ii < bytes.length)
                  protectedPassword[i + ii] = (byte) (encryptedSection[ii] ^ bytes[i + ii]);
              else
                  protectedPassword[i + ii] = encryptedSection[ii];
          }
      }
      return protectedPassword;
  }
  
  // Change password.
  @Override
  public SignonInfo changePassword(String systemName, boolean systemNameLocal, String userId, byte[] oldBytes, byte[] newBytes)
      throws AS400SecurityException, IOException
  {
        return changePassword(systemName, systemNameLocal, userId, oldBytes, newBytes, null);
  }

  // Change password.
  @Override
  public SignonInfo changePassword(String systemName, boolean systemNameLocal,
      String userId, byte[] oldBytes, byte[] newBytes, char[] additionalAuthenticationFactor)
      throws AS400SecurityException, IOException
  {
      if (Trace.traceOn_)
          Trace.log(Trace.DIAGNOSTIC, "Change password implementation, system name: '" + systemName + "' user ID: '" + userId + "'");

      if (PASSWORD_TRACE)
      {
          Trace.log(Trace.DIAGNOSTIC, "Old password bytes:", oldBytes);
          Trace.log(Trace.DIAGNOSTIC, "New password bytes:", newBytes);
      }

      systemName_      = systemName;
      systemNameLocal_ = systemNameLocal;
      userId_          = userId;

      // Decode passwords and discard seeds.
      byte[] oldVault = CredentialVault.decode(proxySeed_, remoteSeed_, oldBytes);
      char[] oldPassword = BinaryConverter.byteArrayToCharArray(oldVault);

      byte[] newVault = CredentialVault.decode(proxySeed_, remoteSeed_, newBytes);
      char[] newPassword = BinaryConverter.byteArrayToCharArray(newVault);

      CredentialVault.clearArray(oldVault);
      CredentialVault.clearArray(newVault);

      proxySeed_ = null;
      remoteSeed_ = null;

      if (PASSWORD_TRACE)
      {
          Trace.log(Trace.DIAGNOSTIC, "Old password unscrambled: '" + new String(oldPassword) + "'");
          Trace.log(Trace.DIAGNOSTIC, "New password unscrambled: '" + new String(newPassword) + "'");
      }

      // Get a socket connection.
      boolean needToDisconnect = (signonServer_ == null);
      signonConnect();

      try
      {
          // Convert user ID to EBCDIC.
          byte[] userIdEbcdic = SignonConverter.stringToByteArray(userId);
          byte[] encryptedPassword;
          byte[] oldProtected;
          byte[] newProtected;

          if (passwordLevel_ < 2)
          {
              if (oldPassword.length > 0 && Character.isDigit(oldPassword[0]))
              {
                  if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Prepending Q to numeric password.");

                  char[] passwordWithQ = new char[oldPassword.length + 1];
                  passwordWithQ[0] = 'Q';
                  System.arraycopy(oldPassword, 0, passwordWithQ, 1, oldPassword.length);
                  CredentialVault.clearArray(oldPassword);
                  oldPassword = passwordWithQ;
              }

              // Do DES encryption.
              if (oldPassword.length > 10)
              {
                  Trace.log(Trace.ERROR, "Length of parameter 'oldPassword' is not valid:", oldPassword.length);
                  
                  CredentialVault.clearArray(oldPassword);
                  throw new AS400SecurityException(AS400SecurityException.PASSWORD_LENGTH_NOT_VALID);
              }
              
              byte[] oldPasswordEbcdic = SignonConverter.upperCharsToByteArray(oldPassword);
              CredentialVault.clearArray(oldPassword);

              if (newPassword.length > 0 && Character.isDigit(newPassword[0]))
              {
                  if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Prepending Q to numeric password.");

                  char[] passwordWithQ = new char[newPassword.length + 1];
                  passwordWithQ[0] = 'Q';
                  System.arraycopy(newPassword, 0, passwordWithQ, 1, newPassword.length);
                  newPassword = passwordWithQ;
              }

              if (newPassword.length > 10)
              {
                  Trace.log(Trace.ERROR, "Length of parameter 'newPassword' is not valid:", newPassword.length);
                  
                  throw new AS400SecurityException(AS400SecurityException.PASSWORD_NEW_NOT_VALID);
              }
              byte[] newPasswordEbcdic = SignonConverter.upperCharsToByteArray(newPassword);

              // Setup output variables for encrypt new password.
              oldProtected = (oldPasswordEbcdic[8] == 0x40 && oldPasswordEbcdic[9] == 0x40) ? new byte[8] : new byte[16];
              newProtected = (newPasswordEbcdic[8] == 0x40 && newPasswordEbcdic[9] == 0x40) ? new byte[8] : new byte[16];

              encryptedPassword = encryptNewPassword(userIdEbcdic, oldPasswordEbcdic, newPasswordEbcdic, oldProtected,
                      newProtected, clientSeed_, serverSeed_);
              CredentialVault.clearArray(oldPasswordEbcdic);
          }
          else if (passwordLevel_ < 4)
          {
              // Do SHA-1 encryption.
              byte[] userIdBytes = BinaryConverter.charArrayToByteArray(SignonConverter.byteArrayToCharArray(userIdEbcdic));

              // Screen out passwords that start with a star.
              if (oldPassword.length == 0) {
                  Trace.log(Trace.ERROR, "Parameter 'oldPassword' is empty.");
                  throw new AS400SecurityException(AS400SecurityException.SIGNON_CHAR_NOT_VALID);
              }
              if (oldPassword[0] == '*') {
                  Trace.log(Trace.ERROR, "Parameter 'oldPassword' begins with a '*' character.");
                  throw new AS400SecurityException(AS400SecurityException.SIGNON_CHAR_NOT_VALID);
              }
              if (newPassword.length == 0) {
                  Trace.log(Trace.ERROR, "Parameter 'newPassword' is empty.");
                  throw new AS400SecurityException(AS400SecurityException.SIGNON_CHAR_NOT_VALID);
              }
              if (newPassword[0] == '*') {
                  Trace.log(Trace.ERROR, "Parameter 'newPassword' begins with a '*' character.");
                  throw new AS400SecurityException(AS400SecurityException.SIGNON_CHAR_NOT_VALID);
              }

              // Trim space and put in byte array.
              char[] trimmedOldPassword = trimUnicodeSpace(oldPassword);
              if (oldPassword != trimmedOldPassword)
              {
                  CredentialVault.clearArray(oldPassword);
                  oldPassword = trimmedOldPassword;
              }
              byte[] oldPasswordBytes = BinaryConverter.charArrayToByteArray(oldPassword);

              char[] trimmedNewPassword = trimUnicodeSpace(newPassword);
              if (newPassword != trimmedNewPassword)
              {
                  CredentialVault.clearArray(newPassword);
                  newPassword = trimmedNewPassword;
              }

              byte[] newPasswordBytes = BinaryConverter.charArrayToByteArray(trimUnicodeSpace(newPassword));
              byte[] sequence = { 0, 0, 0, 0, 0, 0, 0, 1 };

              if (PASSWORD_TRACE) {
                  Trace.log(Trace.DIAGNOSTIC, "Pre SHA-1 userIdBytes:", userIdBytes);
                  Trace.log(Trace.DIAGNOSTIC, "Pre SHA-1 oldPasswordBytes:", oldPasswordBytes);
                  Trace.log(Trace.DIAGNOSTIC, "Pre SHA-1 newPasswordBytes:", newPasswordBytes);
                  Trace.log(Trace.DIAGNOSTIC, "Pre SHA-1 sequence:", sequence);
              }

              byte[] token = generateShaToken(userIdBytes, oldPasswordBytes);
              encryptedPassword = generateShaSubstitute(token, serverSeed_, clientSeed_, userIdBytes, sequence);

              newProtected = generateShaProtected(newPasswordBytes, token, serverSeed_, clientSeed_, userIdBytes, sequence);

              byte[] newToken = generateShaToken(userIdBytes, newPasswordBytes);

              oldProtected = generateShaProtected(oldPasswordBytes, newToken, serverSeed_, clientSeed_, userIdBytes, sequence);

              CredentialVault.clearArray(newPasswordBytes);
              CredentialVault.clearArray(oldPasswordBytes);
          } // password level >= 4 QPWDLVL4
          else
          {
              if (oldPassword.length == 0) {
                  Trace.log(Trace.ERROR, "Parameter 'oldPassword' is empty.");
                  throw new AS400SecurityException(AS400SecurityException.SIGNON_CHAR_NOT_VALID);
              }
              if (oldPassword[0] == '*') {
                  Trace.log(Trace.ERROR, "Parameter 'oldPassword' begins with a '*' character.");
                  throw new AS400SecurityException(AS400SecurityException.SIGNON_CHAR_NOT_VALID);
              }
              if (newPassword.length == 0) {
                  Trace.log(Trace.ERROR, "Parameter 'newPassword' is empty.");
                  throw new AS400SecurityException(AS400SecurityException.SIGNON_CHAR_NOT_VALID);
              }
              if (newPassword[0] == '*') {
                  Trace.log(Trace.ERROR, "Parameter 'newPassword' begins with a '*' character.");
                  throw new AS400SecurityException(AS400SecurityException.SIGNON_CHAR_NOT_VALID);
              }
              
              /*
               * If a sequence number is used, the client increments its password sequence "PWSEQs" by
               * one and saves it. PWSEQs is an 8-byte value. The implementation in the host servers always
               * uses a sequence number of 1.
               */
              byte[] sequence = { 0, 0, 0, 0, 0, 0, 0, 1 };
                 
              char[] trimmedOldPassword = trimUnicodeSpace(oldPassword);
              if (oldPassword != trimmedOldPassword)
              { 
                  CredentialVault.clearArray(oldPassword);
                  oldPassword = trimmedOldPassword; 
              }
              
              char[] trimmedNewPassword = trimUnicodeSpace(newPassword);
              if (newPassword != trimmedNewPassword)
              {
                  CredentialVault.clearArray(newPassword);
                  newPassword = trimmedNewPassword;
              }
        
              byte[] oldPasswordBytes = BinaryConverter.charArrayToByteArray(oldPassword);
              byte[] newPasswordBytes = BinaryConverter.charArrayToByteArray(newPassword);
      
              byte[] token = generatePwdTokenForPasswordLevel4(userId_, oldPassword);
              encryptedPassword = generateSha512Substitute(userId_, token, serverSeed_, clientSeed_, sequence);
          
              newProtected = generateSha512Protected(newPasswordBytes, token, serverSeed_, clientSeed_, userId, sequence);
          
              byte[] newToken = generatePwdTokenForPasswordLevel4(userId, newPassword);
          
              oldProtected = generateSha512Protected(oldPasswordBytes, newToken, serverSeed_, clientSeed_, userId, sequence);
          
              CredentialVault.clearArray(newPasswordBytes);
              CredentialVault.clearArray(oldPasswordBytes);
          }

          if (PASSWORD_TRACE) {
              Trace.log(Trace.DIAGNOSTIC, "Sending Change Password Request...");
              Trace.log(Trace.DIAGNOSTIC, "  User ID:", userId);
              Trace.log(Trace.DIAGNOSTIC, "  User ID bytes:", userIdEbcdic);
              Trace.log(Trace.DIAGNOSTIC, "  Encrypted password:", encryptedPassword);
              Trace.log(Trace.DIAGNOSTIC, "  Protected old password:", oldProtected);
              Trace.log(Trace.DIAGNOSTIC, "  Protected new password:", newProtected);
          }

          ChangePasswordReq chgReq = new ChangePasswordReq(userIdEbcdic,
                  encryptedPassword, oldProtected, oldPassword.length * 2,
                  newProtected, newPassword.length * 2, serverLevel_, 
                  (additionalAuthenticationFactor != null ? (new String(additionalAuthenticationFactor)).getBytes(StandardCharsets.UTF_8) : null));
          ChangePasswordRep chgRep = (ChangePasswordRep) signonServer_.sendAndReceive(chgReq);
          int rc = chgRep.getRC();
          if (rc == 0)
          {
              // Change password worked, so retrieve signon information using new password.
              if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Password change implementation successful.");

              byte[] tempSeed = new byte[9];
              CredentialVault.rng.nextBytes(tempSeed);

              byte[] newPasswordByteArray = BinaryConverter.charArrayToByteArray(newPassword); 
              SignonInfo returnInfo = signon2(systemName, systemNameLocal, userId,
                      CredentialVault.encode(tempSeed, exchangeSeed(tempSeed), newPasswordByteArray),
                      AS400.AUTHENTICATION_SCHEME_PASSWORD, additionalAuthenticationFactor); 
        
              CredentialVault.clearArray(newPasswordByteArray);
        
              if (needToDisconnect)
                  signonDisconnect();
              return returnInfo;
          }
          else
          {
              byte[] rcBytes = new byte[4];
              BinaryConverter.intToByteArray(rc, rcBytes, 0);
              Trace.log(Trace.ERROR, "Change password implementation failed with return code:", rcBytes);

              throw AS400ImplRemote.returnSecurityException(rc, chgRep
                      .getErrorMessages(ConverterImplRemote.getConverter(ExecutionEnvironment.getBestGuessAS400Ccsid(), this)), userId);
          }
      }
      catch (IOException | AS400SecurityException e)
      {
          Trace.log(Trace.ERROR, "Change password failed:", e);
          if (signonServer_ != null)
              signonServer_.forceDisconnect();
          signonServer_ = null;
          throw e;
      }
      finally
      {
          CredentialVault.clearArray(newPassword);
          CredentialVault.clearArray(oldPassword);
      }
  }

  // Implementation for connect.
  @Override
  public void connect(int service) throws AS400SecurityException, IOException {
    connect(service, -1, false);
  }

  @Override
  public void connect(int service, int overridePort, boolean skipSignonServer) throws AS400SecurityException, IOException
  {
      if (service == AS400.SIGNON)
          signonConnect();
      else
          getConnection(service, overridePort, false, skipSignonServer);
  }

  // @SAA Create user handle for the connection
  public int createUserHandle() throws AS400SecurityException, IOException
  {
      if (userHandle_ != UNINITIALIZED)
          return userHandle_;
      
      int authScheme = credVault_.getType();
      
      if (authScheme != AS400.AUTHENTICATION_SCHEME_GSS_TOKEN && authScheme != AS400.AUTHENTICATION_SCHEME_PASSWORD)
          return UNINITIALIZED;

      // Do not want to create more than one user handle
      synchronized (this)
      {
          if (userHandle_ != UNINITIALIZED)
              return userHandle_;
          
          AS400Server connectedServer = getConnectedServer(new int[] { AS400.FILE });
          if (connectedServer == null)
              return UNINITIALIZED;
          
          ClientAccessDataStream ds = null;
          int rc = 0;
          
          if (authScheme == AS400.AUTHENTICATION_SCHEME_GSS_TOKEN)
          {
              try
              {
                byte[] authenticationBytes;
                if (this.kerbTicket_ != null){
                    authenticationBytes = this.kerbTicket_;
                } else {
                    authenticationBytes = (gssCredential_ == null) 
                          ? TokenManager.getGSSToken(systemName_, gssName_)
                          : TokenManager2.getGSSToken(systemName_, gssCredential_);
                }
                  IFSUserHandle2Req req = new IFSUserHandle2Req(authenticationBytes, aafIndicator_ ? additionalAuthFactor_ : null);
                  ds = (ClientAccessDataStream) connectedServer.sendAndReceive(req);
              }
              catch (InterruptedException e)
              {
                  Trace.log(Trace.ERROR, "Interrupted");
                  InterruptedIOException throwException = new InterruptedIOException(e.getMessage());
                  throwException.initCause(e);
                  throw throwException;
              }
              catch (Throwable e) {
                  Trace.log(Trace.ERROR, "Error retrieving GSSToken:", e);
                  throw new AS400SecurityException(AS400SecurityException.KERBEROS_TICKET_NOT_VALID_RETRIEVE, e);
              }
          }
          else
          {
              // Password authentication scheme
              
              byte[] ClientSeed = BinaryConverter.longToByteArray(System.currentTimeMillis());
              byte[] ServerSeed = null;
              try 
              {
                  IFSUserHandleSeedReq req = new IFSUserHandleSeedReq(ClientSeed);
                  ds = (ClientAccessDataStream) connectedServer.sendAndReceive(req);
              } 
              catch (InterruptedException e)
              {
                  Trace.log(Trace.ERROR, "Interrupted");
                  InterruptedIOException throwException = new InterruptedIOException( e.getMessage());
                  throwException.initCause(e);
                  throw throwException;
              }
              
              // Verify that we got a handle back.
              if (ds instanceof IFSUserHandleSeedRep)
                  ServerSeed = ((IFSUserHandleSeedRep) ds).getSeed();
              else if (ds instanceof IFSReturnCodeRep)
              {
                  rc = ((IFSReturnCodeRep) ds).getReturnCode();
                  if (rc != IFSReturnCodeRep.SUCCESS) Trace.log(Trace.ERROR, "IFSReturnCodeRep return code", rc);
                  throw new ExtendedIOException(rc);
              }
              else
              {
                  // Unknown data stream.
                  Trace.log(Trace.ERROR, "Unknown reply data stream", ds.getReqRepID());
                  throw new InternalErrorException(Integer.toHexString(ds.getReqRepID()), InternalErrorException.DATA_STREAM_UNKNOWN);
              }
    
              rc = 0;
              ds = null;
              byte[] userIDbytes = SignonConverter.stringToByteArray(userId_);
              byte[] encryptedPassword = getPassword(ClientSeed, ServerSeed);
              IFSCreateUserHandlerReq req = new IFSCreateUserHandlerReq(userIDbytes, encryptedPassword, aafIndicator_ ? additionalAuthFactor_ : null);
              
              try {
                  ds = (ClientAccessDataStream) connectedServer.sendAndReceive(req);
              }
              catch (InterruptedException e)
              {
                  Trace.log(Trace.ERROR, "Interrupted");
                  InterruptedIOException throwException = new InterruptedIOException( e.getMessage());
                  throwException.initCause(e);
                  throw throwException;
              }
          }
          
          // Verify the reply.
          if (ds instanceof IFSCreateUserHandleRep)
          {
              rc = ((IFSCreateUserHandleRep) ds).getReturnCode();
              if (rc != IFSReturnCodeRep.SUCCESS)
              {
                  Trace.log(Trace.ERROR, "IFSCreateUserHandleRep return code", rc);
                  throw new ExtendedIOException(rc);
              }
              setUserHandle(((IFSCreateUserHandleRep) ds).getHandle());
          }
          else if (ds instanceof IFSUserHandle2Rep)
          {
              rc = ((IFSUserHandle2Rep) ds).getReturnCode();
              if (rc != IFSReturnCodeRep.SUCCESS)
              {
                  Trace.log(Trace.ERROR, "IFSUserHandle2Rep return code", rc);
                  throw new ExtendedIOException(rc);
              }
              setUserHandle(((IFSUserHandle2Rep) ds).getHandle());
          }
          else if (ds instanceof IFSReturnCodeRep)
          {
              rc = ((IFSReturnCodeRep) ds).getReturnCode();
              if (rc != IFSReturnCodeRep.SUCCESS) Trace.log(Trace.ERROR, "IFSReturnCodeRep return code", rc);
              throw new ExtendedIOException(rc);
          }
          else
          {
              // Unknown data stream.
              Trace.log(Trace.ERROR, "Unknown reply data stream", ds.getReqRepID());
              throw new InternalErrorException(InternalErrorException.DATA_STREAM_UNKNOWN, Integer.toHexString(ds.getReqRepID()), null);
          }
      }
      
      return userHandle_;
  }

  public int getUserHandle() {
      return userHandle_;
  }

  public void setUserHandle(int userHandle) {
      userHandle_ = userHandle;
  }

  public void freeUserHandle() throws IOException, AS400SecurityException
  {
      if (userHandle_ != UNINITIALIZED)
      {
          IFSFreeUserHandlerReq req = new IFSFreeUserHandlerReq(userHandle_);
          AS400Server connectedServer = getConnectedServer(new int[] { AS400.FILE });
          if (connectedServer != null)
              connectedServer.send(req);
      }
      
      userHandle_ = UNINITIALIZED;
  }

  // Implementation for disconnect.
  @Override
  public void disconnect(int service)
  {
      if (service == AS400.HOSTCNN)
          hostcnnDisconnect();
      else if (service == AS400.SIGNON)
          signonDisconnect();
      else
      {
          if (userHandle_ != UNINITIALIZED && service == AS400.FILE)
          {
              try {
                  freeUserHandle();
              } catch (Exception e) {  }
          }
          
          Vector<?> serverList = serverPool_[service];
          synchronized (serverList)
          {
              while (!serverList.isEmpty()) {
                  disconnectServer((AS400Server) serverList.elementAt(0));
              }
          }
      }
    
      if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Service disconnected implementation:", AS400.getServerName(service));
  }

  // Disconnect all services.
  void disconnectAllServices()
  {
      if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Disconnecting all services implementation.");

      disconnect(AS400.FILE);
      disconnect(AS400.PRINT);
      disconnect(AS400.DATAQUEUE);
      disconnect(AS400.COMMAND);
      disconnect(AS400.DATABASE);
      disconnect(AS400.RECORDACCESS);
      disconnect(AS400.CENTRAL);
      disconnect(AS400.SIGNON);
      
      // We purposely do not end HOSTCNN - should we?

      if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "All services disconnected implementation.");
  }
  
  // Disconnects the system and removes it from the system list.
  // param server The AS400Server to disconnect and remove.
  public void disconnectServer(AS400Server server)
  {
      server.forceDisconnect();
      int service = server.getService();
      if (service != AS400.SIGNON && service != AS400.HOSTCNN)
      {
          Vector serverList = serverPool_[service];
          synchronized (serverList)
          {
              if (!serverList.isEmpty())
              {
                  serverList.removeElement(server);

                  // Only fire the event if all systems have been disconnected.
                  if (serverList.isEmpty())
                      fireConnectEvent(false, service);
              }
          }
      }
    
      if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Server disconnected. Job: " + server.getJobString());
  }

  // Exchange of seeds between public 'AS400' object and this class.
  // If the public class and the implRemote class are running on different
  // machines, the authentication information must be transmitted securely
  // between the public class and the implRemote class. The transmitted
  // authentication information can be encoded/decoded using the exchanged
  // seeds.
  @Override
  public byte[] exchangeSeed(byte[] proxySeed)
  {
      // Hold the seed they send us.
      proxySeed_ = proxySeed;

      // Generate, hold, and send them our seed.
      remoteSeed_ = new byte[7];
      CredentialVault.rng.nextBytes(remoteSeed_);

      return remoteSeed_;
  }

  // Called during garbage collections - Cleans up all connections.
  @Override
  protected void finalize() throws Throwable
  {
      if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Finalize method for AS400 implementation invoked.");

      try {
          disconnectAllServices();
          disconnect(AS400.HOSTCNN);
      } finally {
          super.finalize();
      }
  }

  // Fire connnect event.
  private void fireConnectEvent(boolean connect, int service)
  {
      if (dispatcher_ == null)
          return;

      ConnectionEvent connectEvent = new ConnectionEvent(this, service);
      if (connect)
          dispatcher_.connected(connectEvent);
      else
          dispatcher_.disconnected(connectEvent);
  }

  // Flow the generate profile token datastream.
  @Override
  public void generateProfileToken(ProfileTokenCredential profileToken, String userIdentity) throws AS400SecurityException, IOException
  {
      signonConnect();

      try
      {
          byte[] userIDbytes = SignonConverter.stringToByteArray(userId_);
          byte[] encryptedPassword = getPassword(clientSeed_, serverSeed_);
          if (PASSWORD_TRACE)
          {
              Trace.log(Trace.DIAGNOSTIC, "Sending Start Server Request for generateProfileToken to signon server...");
              Trace.log(Trace.DIAGNOSTIC, "  User ID:", userId_);
              Trace.log(Trace.DIAGNOSTIC, "  User ID bytes:", userIDbytes);
              Trace.log(Trace.DIAGNOSTIC, "  Client seed:", clientSeed_);
              Trace.log(Trace.DIAGNOSTIC, "  Server seed:", serverSeed_);
              Trace.log(Trace.DIAGNOSTIC, "  Encrypted password:", encryptedPassword);
          }

          int serverId = AS400Server.getServerId(AS400.SIGNON);
          AS400StrSvrDS req = new AS400StrSvrDS(serverId, userIDbytes, encryptedPassword, credVault_.getType());
          AS400StrSvrReplyDS reply = (AS400StrSvrReplyDS) signonServer_.sendAndReceive(req);

          if (reply.getRC() != 0)
          {
              byte[] rcBytes = new byte[4];
              BinaryConverter.intToByteArray(reply.getRC(), rcBytes, 0);
              Trace.log(Trace.ERROR, "Start server failed with return code:", rcBytes);
              throw AS400ImplRemote.returnSecurityException(reply.getRC(), null, userId_);
          }
          
          // [0]=notUsed, [1]=verification ID, [2]=remote ip address 
          Object[] additionalAuthInfo = getAdditionalAuthInfo(profileToken, null, null);
          
          SignonGenAuthTokenRequestDS req2 = new SignonGenAuthTokenRequestDS(
                  BinaryConverter.charArrayToByteArray(userIdentity.toCharArray()), profileToken.getTokenType(),
                  profileToken.getTimeoutInterval(), serverLevel_, 
                  (byte[])additionalAuthInfo[1],  (byte[])additionalAuthInfo[2]);
          SignonGenAuthTokenReplyDS rep = (SignonGenAuthTokenReplyDS) signonServer_.sendAndReceive(req2);

          int rc = rep.getRC();
          if (rc != 0)
          {
              byte[] rcBytes = new byte[4];
              BinaryConverter.intToByteArray(rc, rcBytes, 0);
              Trace.log(Trace.ERROR, "Generate profile token failed with return code:", rcBytes);
              throw AS400ImplRemote.returnSecurityException(rc,
                      rep.getErrorMessages(ConverterImplRemote.getConverter(ExecutionEnvironment.getBestGuessAS400Ccsid(), this)),
                      userId_);
          }
          try {
              int vrm = (version_ != null) ? version_.getVersionReleaseModification() : getVRM();
              if (vrm > 0x00070500 ) {
                    /* server always generates enhanced profile token */
                  ProfileTokenEnhancedInfo enhancedInfo = new ProfileTokenEnhancedInfo((String) additionalAuthInfo[3],
                          (String) additionalAuthInfo[4],
                          profileToken.getRemotePort(),
                          profileToken.getLocalIPAddress(),
                          profileToken.getLocalPort());
                  enhancedInfo.setEnhancedTokenCreated(true); 
                  profileToken.setToken(rep.getProfileTokenBytes(), enhancedInfo);
              } else {
                  profileToken.setToken(rep.getProfileTokenBytes());
              }

              
              profileToken.setTokenCreator(ProfileTokenCredential.CREATOR_SIGNON_SERVER);
          }
          catch (PropertyVetoException e) {
              Trace.log(Trace.ERROR, e);
              throw new InternalErrorException(InternalErrorException.UNEXPECTED_EXCEPTION, e);
          }
      } 
      catch (IOException | AS400SecurityException e)
      {
          Trace.log(Trace.ERROR, "Generate profile token failed:", e);
          signonServer_.forceDisconnect();
          signonServer_ = null;
          throw e;
      }
  }

  @Override
  public void generateProfileToken(ProfileTokenCredential profileToken, String userId, CredentialVault vault, char[] additionalAuthFactor, String gssName)
      throws AS400SecurityException, IOException, InterruptedException
  {
      signonConnect();
      
      try
      {
          byte[] userIdEbcdic = SignonConverter.stringToByteArray(userId);
          byte[] authenticationBytes = null;
          int authScheme = vault.getType();

          switch (authScheme) 
          {
              case AS400.AUTHENTICATION_SCHEME_GSS_TOKEN:
                  try
                  {
                    if (this.kerbTicket_ != null){
                        authenticationBytes = this.kerbTicket_;
                    } else {
                        authenticationBytes = (gssCredential_ == null) 
                            ? TokenManager.getGSSToken(systemName_, gssName) 
                            : TokenManager2.getGSSToken(systemName_, gssCredential_);
                }
                  }
                  catch (Exception e)
                  {
                      Trace.log(Trace.ERROR, "Error retrieving GSSToken:", e);
                      throw new AS400SecurityException(AS400SecurityException.KERBEROS_TICKET_NOT_VALID_RETRIEVE, e);
                  }
                  break;
              case AS400.AUTHENTICATION_SCHEME_PROFILE_TOKEN:
              case AS400.AUTHENTICATION_SCHEME_IDENTITY_TOKEN:
                  authenticationBytes = vault.decode(proxySeed_, remoteSeed_);
                  break;
              default: // Password.
                  byte[] passwordByte = vault.decode(proxySeed_, remoteSeed_); 
                  char[] password = BinaryConverter.byteArrayToCharArray(passwordByte); 
                  CredentialVault.clearArray(passwordByte);  
                  proxySeed_ = null;
                  remoteSeed_ = null;
    
                  // Generate the correct password based on the password encryption level of the system.
                  if (passwordLevel_ < 2)
                  {
                      // Prepend Q to numeric password. A "numeric password" is a password that starts with a numeric digit.
                      if (password.length > 0 && Character.isDigit(password[0]))
                      {
                          if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Prepending Q to numeric password.");
                
                          char[] passwordWithQ = new char[password.length + 1];
                          passwordWithQ[0] = 'Q';
                          System.arraycopy(password, 0, passwordWithQ, 1, password.length);
                          CredentialVault.clearArray(password);
                          password = passwordWithQ;
                      }
    
                      if (password.length > 10)
                      {
                          Trace.log(Trace.ERROR, "Length of parameter 'password' is not valid:", password.length);
                          throw new AS400SecurityException(AS400SecurityException.PASSWORD_LENGTH_NOT_VALID);
                      }
                      authenticationBytes = encryptPassword(userIdEbcdic,
                              SignonConverter.upperCharsToByteArray(password), clientSeed_, serverSeed_);
                      CredentialVault.clearArray(password);
                  }
                  else if (passwordLevel_ < 4)
                  {
                      // Do SHA-1 encryption.
                      byte[] userIdBytes = BinaryConverter.charArrayToByteArray(SignonConverter.byteArrayToCharArray(userIdEbcdic));
    
                      // Screen out passwords that start with a star.
                      if (password.length == 0) {
                          Trace.log(Trace.ERROR, "Parameter 'password' is empty.");
                          throw new AS400SecurityException(AS400SecurityException.SIGNON_CHAR_NOT_VALID);
                      }
                      
                      if (password[0] == '*') {
                          Trace.log(Trace.ERROR, "Parameter 'password' begins with a '*' character.");
                          throw new AS400SecurityException( AS400SecurityException.SIGNON_CHAR_NOT_VALID);
                      }
    
                      char[] trimmedPassword = trimUnicodeSpace(password);
                      byte[] passwordBytes = BinaryConverter.charArrayToByteArray(trimmedPassword);
                      CredentialVault.clearArray(trimmedPassword);
                      CredentialVault.clearArray(password);
                      byte[] sequence = { 0, 0, 0, 0, 0, 0, 0, 1 };
    
                      if (PASSWORD_TRACE) {
                          Trace.log(Trace.DIAGNOSTIC, "Pre SHA-1 userIdBytes:", userIdBytes);
                          Trace.log(Trace.DIAGNOSTIC, "Pre SHA-1 passwordBytes:", passwordBytes);
                          Trace.log(Trace.DIAGNOSTIC, "Pre SHA-1 sequence:", sequence);
                      }
    
                      byte[] token = generateShaToken(userIdBytes, passwordBytes);
                      CredentialVault.clearArray(passwordBytes);
              
                      authenticationBytes = generateShaSubstitute(token, serverSeed_, clientSeed_, userIdBytes, sequence);
                  }
                  else
                  {
                      if (password.length == 0) {
                          Trace.log(Trace.ERROR, "Parameter 'password' is empty.");
                          throw new AS400SecurityException(AS400SecurityException.SIGNON_CHAR_NOT_VALID);
                      }
                
                      // Screen out passwords that start with a star.
                      if (password[0] == '*') {
                          Trace.log(Trace.ERROR, "Parameter 'password' begins with a '*' character.");
                          throw new AS400SecurityException(AS400SecurityException.SIGNON_CHAR_NOT_VALID);
                      }
                      
                      /*
                       * If a sequence number is used, the client increments its password sequence "PWSEQs" by
                       * one and saves it. PWSEQs is an 8-byte value. The implementation in the host servers always
                       * uses a sequence number of 1.
                       */
                      byte[] sequence = { 0, 0, 0, 0, 0, 0, 0, 1 };
                      //Generate salt for password level 4
                      /*
                       * The following steps describe the algorithm used to generate the pwdlvl 4 version of the password:
                       * 1. Convert the 10-character blank padded user ID to upper case.
                       * 2. Convert the 10-character blank padded upper case user ID to Unicode (CCSID 13488).
                       * 3. Convert the password value to Unicode (CCSID 13488).
                       * 4. Generate the salt value:
                       *    a. Fill a 28-byte variable with Unicode blanks (0x0020).
                       *    b. Copy the Unicode user ID value into the first 20 bytes of the 28-byte blank filled variable.
                       *    c. Copy the last 8 bytes (last 4 characters) of the Unicode password value into the last 8 bytes of the 28-byte variable. If the password is less than 4 characters, then copy the entire Unicode password value.
                       *    d. Do a SHA-256 hash on the 28-byte variable to produce the 32-byte salt value.
                       * 5. Generate the pwdlvl 4 version of the password using PBKDF2 with HMAC SHA-512 with the following values:
                       *    Hash algorithm = HMAC SHA-512 (produces a 64-byte key)
                       *    Data = Unicode password value
                       *    Data Length = Length of Unicode password value
                       *    Iterations = 10022
                       *    Initialization vector length = 32
                       *    Initialization vector (salt) = value generated in Step #4.
                       */
                      byte[] token = generatePwdTokenForPasswordLevel4(userId, password);
                      CredentialVault.clearArray(password); 
                      authenticationBytes = generateSha512Substitute(userId, token, serverSeed_, clientSeed_, sequence);
                  }
          }
          
          if (Trace.isTraceOn())  Trace.log(Trace.DIAGNOSTIC, "AS400ImplRemote generating profile token for user: " + userId);

          // [0]=Factor, [1]=verification ID, [2]=remote ip address 
          Object[] additionalAuthInfo = getAdditionalAuthInfo(profileToken, null, additionalAuthFactor);

          AS400GenAuthTknDS req = new AS400GenAuthTknDS(userIdEbcdic,
                  authenticationBytes, authScheme, profileToken.getTokenType(),
                  profileToken.getTimeoutInterval(), serverLevel_, 
                  (byte[])additionalAuthInfo[0],  (byte[])additionalAuthInfo[1],  (byte[])additionalAuthInfo[2]);
          
          CredentialVault.clearArray(authenticationBytes);
          AS400GenAuthTknReplyDS rep = (AS400GenAuthTknReplyDS) signonServer_.sendAndReceive(req);
          req.clear(); 
      
          int rc = rep.getRC();
          if (rc != 0)
          {
              byte[] rcBytes = new byte[4];
              BinaryConverter.intToByteArray(rc, rcBytes, 0);
              Trace.log(Trace.ERROR, "Generate profile token failed with return code:", rcBytes);
              throw AS400ImplRemote.returnSecurityException(rc, rep.getErrorMessages(ConverterImplRemote.getConverter(
                ExecutionEnvironment.getBestGuessAS400Ccsid(), this)), userId);
          }
      
          try {
              int vrm = (version_ != null) ? version_.getVersionReleaseModification() : getVRM();
              if (vrm > 0x00070500 ) {
                  ProfileTokenEnhancedInfo enhancedInfo = new ProfileTokenEnhancedInfo((String) additionalAuthInfo[3],
                          (String) additionalAuthInfo[4],
                          profileToken.getRemotePort(),
                          profileToken.getLocalIPAddress(),
                          profileToken.getLocalPort());
                  enhancedInfo.setEnhancedTokenCreated(true); 
                  profileToken.setToken(rep.getProfileTokenBytes(), enhancedInfo);
              } else { 
                  profileToken.setToken(rep.getProfileTokenBytes());
              }
              profileToken.setTokenCreator(ProfileTokenCredential.CREATOR_SIGNON_SERVER);
          } catch (PropertyVetoException e) {
              Trace.log(Trace.ERROR, e);
              throw new InternalErrorException(InternalErrorException.UNEXPECTED_EXCEPTION, e);
          }
      }
      catch (IOException | AS400SecurityException e)
      {
          Trace.log(Trace.ERROR, "Generate profile token failed:", e);
          signonServer_.forceDisconnect();
          signonServer_ = null;
          throw e;
      } 
  }
  

  private static class SystemInformation {
      ServerVersion version;
      int serverLevel;
      int passwordLevel;
      boolean aafIndicator;
  }
  
  private static SystemInformation getSystemInformation(String systemName, boolean useSSL) throws AS400SecurityException, IOException
  {
      if (systemName == null)
          throw new NullPointerException("systemName");
      
      AS400ImplRemote implRemote = new AS400ImplRemote(); 
            
      try 
      {
          implRemote.systemName_ = systemName; 
          implRemote.socketProperties_ = new SocketProperties();
          implRemote.useSSLConnection_ = useSSL ? new SSLOptions()  : null;
          implRemote.hostcnnConnect(false);

          if (implRemote.hostcnnServer_ == null)
              implRemote.signonConnect(); 
          
          SystemInformation si = new SystemInformation();
          si.version = implRemote.version_;
          si.serverLevel = implRemote.serverLevel_;
          si.passwordLevel = implRemote.passwordLevel_;
          si.aafIndicator = implRemote.aafIndicator_ ; 
          
          return si;
      }
      finally {
          if (implRemote.hostcnnServer_ != null)
              implRemote.hostcnnDisconnect(); 
          else
              implRemote.signonDisconnect(); 
      }
  }
  
  public static boolean getAdditionalAuthenticationIndicator(String systemName, boolean useSSL) throws AS400SecurityException, IOException { 
      return (getSystemInformation(systemName, useSSL)).aafIndicator;
  }
  
  public static int getVRM(String systemName, boolean useSSL) throws AS400SecurityException, IOException { 
      return (getSystemInformation(systemName, useSSL)).version.getVersionReleaseModification();
  }

  // Get either the user's CCSID, the signon server CCSID, or our best guess.
  public int getCcsid()
  {
      int howObtained = 0; // how we got the CCSID value

      // CCSID values obtained from different sources (indexed by 'howObtained')
      int[] ccsidValues = { ccsid_, 0, 0, 0 };

      // First pass:
      // Try to arrive at a CCSID other than 0 or 65535.
      if (ccsid_ == 0 || ccsid_ == 65535)
      {
          if (signonInfo_ != null)
          {
              howObtained = 1;
              ccsidValues[howObtained] = signonInfo_.serverCCSID;
              ccsid_ = ccsidValues[howObtained];
          }

          if (ccsid_ == 0 || ccsid_ == 65535)
          {
              howObtained = 2;
              // Note. This will call the portmapper and signon server.
              ccsidValues[howObtained] = getCcsidFromServer();
              ccsid_ = ccsidValues[howObtained];
          }

          if (ccsid_ == 0 || ccsid_ == 65535)
          {
              howObtained = 3;
              ccsidValues[howObtained] = ExecutionEnvironment.getBestGuessAS400Ccsid();
              ccsid_ = ccsidValues[howObtained];
          }
      }

      // Second pass:
      // If first pass ended up with CCSID == 0, settle for any non-zero CCSID.
      if (ccsid_ == 0)
      {
          if (Trace.traceOn_)
              Trace.log(Trace.DIAGNOSTIC, "AS400ImplRemote.getCcsid() [after first pass]: CCSID=" + ccsid_ + ", howObtained=" + howObtained);

          for (int i = 0; i < ccsidValues.length; i++)
          {
              if (ccsidValues[i] != 0)
              {
                  howObtained = i;
                  ccsid_ = ccsidValues[howObtained];
                  break;
              }
          }
      }

      if (Trace.traceOn_)
      {
          Trace.log(Trace.DIAGNOSTIC, "AS400ImplRemote.getCcsid(): CCSID=" + ccsid_ + ", howObtained=" + howObtained);
          if (ccsid_ < 1 || ccsid_ >= 65535) {
              Trace.log(Trace.WARNING, "AS400ImplRemote.getCcsid(): CCSID is out of valid range: CCSID=" + ccsid_ + ", howObtained=" + howObtained);
          }
      }

      return ccsid_;
  }

  // Get the user's override CCSID or zero if not set.
  int getUserOverrideCcsid()
  {
      if (userOverrideCcsid_)
          return ccsid_;
      return 0;
  }

  // Get CCSID from central server or current job if native.
  public int getCcsidFromServer()
  {
      try
      {
          NLSImpl impl = (NLSImpl) loadImpl("com.ibm.as400.access.NLSImplNative", "com.ibm.as400.access.NLSImplRemote");

          // Get the ccsid from the central server or current job.
          impl.setSystem(this);
          impl.connect();
          impl.disconnect();
          return impl.getCcsid();
      }
      catch (Exception e)
      {
          if (Trace.traceOn_) Trace.log(Trace.WARNING, "Error when attempting to get CCSID from server.", e);
          return 0;
      }
  }

  // Get connection for FTP.
  synchronized Socket getConnection(int port) throws IOException
  {
      // @greenscreens
	  String host = (systemNameLocal_) ? "localhost" : systemName_;
	  Socket socket = PortMapper.createSocket(host, port, socketProperties_);

      try
      {
          PortMapper.setSocketProperties(socket, socketProperties_);
          BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "ISO8859_1"));
          PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "ISO8859_1"), true);

          readFTPLine(reader);
          writer.print("USER " + userId_ + "\r\n");
          writer.flush();
          readFTPLine(reader);
          writer.print("PASS " + new String(BinaryConverter.byteArrayToCharArray(credVault_.getClearCredential())) + "\r\n");
          writer.flush();

          if (!readFTPLine(reader).startsWith("230"))
              throw new IOException();

          return socket;
      }
      catch (IOException e)
      {
          Trace.log(Trace.ERROR, "Establishing FTP connection failed:", e);
          try {
              socket.close();
          } catch (IOException ee) {
              Trace.log(Trace.ERROR, "Error closing socket:", ee);
          }
          throw e;
      }
  }

  private String readFTPLine(BufferedReader reader) throws IOException
  {
      String line = reader.readLine();
      if (line == null || line.length() == 0)
          throw new IOException();
      String code = line.substring(0, 3);
      StringBuilder fullMessage = new StringBuilder(line);
      while ((line != null) && !(line.length() > 3 && line.substring(0, 3).equals(code) && line.charAt(3) == ' '))
      {
          line = reader.readLine();
          fullMessage.append("\n").append(line);
      }
      return fullMessage.toString();
  }

  // Gets the jobs with which we are connected.
  @Override
  public String[] getJobs(int service)
  {
      if (Trace.traceOn_)
          Trace.log(Trace.DIAGNOSTIC, "Getting job names implementation, service:", service);

      if (service == AS400.SIGNON)
          return (signonServer_ != null) ? new String[] { signonServer_.getJobString()} : new String[0];
      else if (service == AS400.HOSTCNN)
          return (hostcnnServer_ != null) ? new String[] { hostcnnServer_.getJobString() } : new String[0];

      Vector serverList = serverPool_[service];
      String[] jobStrings = new String[serverList.size()];
      synchronized (serverList)
      {
          for (int i = 0; i < serverList.size(); ++i) {
              jobStrings[i] = (((AS400Server) serverList.elementAt(i)).getJobString());
          }
      }
      
      return jobStrings;
  }
  
  // Obtain the job identifier for a connection.
  private String obtainJobIdForConnection(byte[] jobBytes) throws UnsupportedEncodingException
  {
      String jobString = "";

      // The name is always invariant, we can use CCSID 37.
      ConverterImplRemote converter = ConverterImplRemote.getConverter(37, this);

      // Perform Bidi transformation for data only
      jobString = AS400BidiTransform.SQL_statement_reordering(jobString, bidiStringType_, converter.table_.bidiStringType_);
      
      // this is a trick to prevent Bidi transformation
      jobString = converter.byteArrayToString(jobBytes, 0, jobBytes.length, converter.table_.bidiStringType_);
      
      return jobString;
  }

  public AS400Server getConnection(int service, boolean forceNewConnection) throws AS400SecurityException, IOException {
      return getConnection(service, forceNewConnection, false /*Skip signon server */);
  }

  AS400Server getConnection(int service, boolean forceNewConnection, boolean skipSignonServer) throws AS400SecurityException, IOException {
      return getConnection(service, -1, forceNewConnection, skipSignonServer);
  }
  
  synchronized AS400Server getConnection(int service, int overridePort, boolean forceNewConnection, boolean skipSignonServer) throws AS400SecurityException, IOException
  {
      if (Trace.traceOn_)
          Trace.log(Trace.DIAGNOSTIC, "Handling request for host server job connection: " + AS400.getServerName(service));

      // -------
      // See if hostcnn server is up, because we will be using that if it is.
      // -------
      
      // Do not allow for connections to hostcnn
      if (service == AS400.HOSTCNN)
      {
          Trace.log(Trace.DIAGNOSTIC, "Get connection for as-hostcnn is not allowed ");
          throw new ServerStartupException(ServerStartupException.CONNECTION_NOT_ESTABLISHED);
      }

      // Necessary for case where we are connecting after native sign-on.
      // Skip this test if not using the signon server.
      if (!isPasswordTypeSet_ && !skipSignonServer)
      {
          hostcnnConnect(true);

          if (hostcnnServer_ == null)
          {
              signonConnect();
              signonDisconnect();
          }
      }

      // -------
      // See if we have a server available to reuse
      // -------
      
      AS400Server server = null;
      Vector<AS400Server> serverList = (Vector<AS400Server>) serverPool_[service];

      synchronized (serverList)
      {
          if (!forceNewConnection && !serverList.isEmpty())
          {
              server = serverList.firstElement();

              if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Reusing previous server object...");

              return server;
          }
      }
    
      // -------
      // Setup communications with the IBM i server, either through the HOSTCNN server
      // or directly to the host server.
      // -------
      
      // Ensure we have an authenticated connection to hostcnn, if possible
      hostcnnConnect(true);
      
      SocketContainer socketContainer = null;
      int connectionID;
      String jobString = "";
      InputStream inStream = null;
      OutputStream outStream = null;

      // DDM (AS400.RECORDACCESS) does not fall under the HOSTCNN umbrella, it is a separate server. 
      if ((hostcnnServer_ == null) || (service == AS400.RECORDACCESS))
      {
          try
          {
              if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "The service as-hostcnn is not available to use or service is DDM");

              socketContainer = PortMapper.getServerSocket((systemNameLocal_) ? "localhost" : systemName_, service,
                      overridePort, useSSLConnection_, socketProperties_, mustUseNetSockets_);

              connectionID = socketContainer.hashCode();
              inStream = socketContainer.getInputStream();
              outStream = socketContainer.getOutputStream();
              localIPAddress_ = socketContainer.getLocalAddress();
              localIPAddressSet_ = true; 
              int authScheme = credVault_.getType();

              if (service == AS400.RECORDACCESS)
              {
                  Object[] returnVals = ClassDecoupler.connectDDMPhase1(outStream, inStream, (passwordLevel_ >= 2), authScheme, connectionID);
                  byte[] clientSeed = (byte[]) returnVals[0];
                  byte[] serverSeed = (byte[]) returnVals[1];
                  byte[] jobBytes   = (byte[]) returnVals[2];
                  jobString = obtainJobIdForConnection(jobBytes); 
                  
                  byte[] sharedKeyBytes = null;
                  boolean encryptUserId = (returnVals[3] != null);
                  KeyPair keyPair = (KeyPair) returnVals[4];

                  if (keyPair != null)
                  {
                      try {
                          sharedKeyBytes = DDMTerm.getSharedKey(keyPair, serverSeed);
                      } 
                      catch (GeneralSecurityException e)
                      {
                          ServerStartupException serverStartupException = new ServerStartupException(
                                  ServerStartupException.CONNECTION_NOT_ESTABLISHED);
                          serverStartupException.initCause(e);
                          throw serverStartupException;
                      }
                  }

                  byte[] userIDbytes;
                  byte[] ddmSubstitutePassword;
                  if (encryptUserId)
                  {
                      authScheme = AS400.AUTHENTICATION_SCHEME_DDM_EUSERIDPWD;

                      userIDbytes = getEncryptedUserid(sharedKeyBytes, serverSeed);
                      if (credVault_.isEmpty() &&
                              !mustUseSuppliedProfile_
                              && AS400.onAS400
                              && AS400.currentUserAvailable()
                              && userId_.equals(CurrentUser.getUserID(AS400.nativeVRM.getVersionReleaseModification()))) {
                          ddmSubstitutePassword = getPassword(clientSeed, serverSeed);
                      } else { 
                          ddmSubstitutePassword = getDdmEncryptedPassword(sharedKeyBytes, serverSeed);
                      }
                  }
                  else
                  {
                      userIDbytes = SignonConverter.stringToByteArray(userId_);
                      // Get the substitute password.
                      ddmSubstitutePassword = getPassword(clientSeed, serverSeed);
                  }

                  if (PASSWORD_TRACE)
                  {
                      Trace.log(Trace.DATASTREAM, "Sending DDM SECCHK request...");
                      Trace.log(Trace.DIAGNOSTIC, "  User ID:", userId_);
                      Trace.log(Trace.DIAGNOSTIC, "  User ID bytes:", userIDbytes);
                      Trace.log(Trace.DIAGNOSTIC, "  Client seed:", clientSeed);
                      Trace.log(Trace.DIAGNOSTIC, "  Server seed:", serverSeed);
                      Trace.log(Trace.DIAGNOSTIC, "  Encrypted password:", ddmSubstitutePassword);
                  }
                  
                  byte[] iaspBytes = null;
                  if (ddmRDB_ != null)
                  {
                      AS400Text text18 = new AS400Text(18, signonInfo_.serverCCSID);
                      iaspBytes = text18.toBytes(ddmRDB_);
                  }
                  
                  // [0]=factor, [1]=verification ID, [2]=remote ip address 
                  Object[] additonalAuthInfo = getAdditionalAuthInfo(null, aafIndicator_, additionalAuthFactor_);

                  ClassDecoupler.connectDDMPhase2(outStream, inStream, userIDbytes, ddmSubstitutePassword, iaspBytes,
                                                  authScheme, ddmRDB_, systemName_, connectionID, 
                                                  (byte[])additonalAuthInfo[0],  (byte[])additonalAuthInfo[1],  (byte[])additonalAuthInfo[2]);
              }
              else
              {
                  // -------
                  // The first request we send is "exchange random seeds"...
                  // -------
                
                  int serverId = AS400Server.getServerId(service);
                  AS400XChgRandSeedDS xChgReq = new AS400XChgRandSeedDS(serverId);
                  if (Trace.traceOn_) xChgReq.setConnectionID(connectionID);
                  xChgReq.write(outStream);

                  AS400XChgRandSeedReplyDS xChgReply = new AS400XChgRandSeedReplyDS();
                  if (Trace.traceOn_) xChgReply.setConnectionID(connectionID);
                  xChgReply.read(inStream);

                  if (xChgReply.getRC() != 0)
                  {
                      byte[] rcBytes = new byte[4];
                      BinaryConverter.intToByteArray(xChgReply.getRC(), rcBytes, 0);
                      Trace.log(Trace.ERROR, "Exchange of random seeds failed with return code:", rcBytes);
                      throw AS400ImplRemote.returnSecurityException(xChgReply.getRC(), null, userId_);
                  }
                   
                  if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Exchange of random seeds successful.");

                  // -------
                  // Next we send the "start server job" request...
                  // -------
                
                  byte[] clientSeed = xChgReq.getClientSeed();
                  byte[] serverSeed = xChgReply.getServerSeed();
                  if (skipSignonServer)
                  {
                      // If the signon server was skipped, get the password level
                      // from the current response.
                      passwordLevel_ = xChgReply.getServerAttributes();
                  }
                
                  byte[] userIDbytes = SignonConverter.stringToByteArray(userId_);
                  byte[] encryptedPassword = getPassword(clientSeed, serverSeed);
                  if (PASSWORD_TRACE)
                  {
                      Trace.log(Trace.DIAGNOSTIC, "Sending Start Server Request for getConnection, service="+service);
                      Trace.log(Trace.DIAGNOSTIC, "  User ID:", userId_);
                      Trace.log(Trace.DIAGNOSTIC, "  User ID bytes:", userIDbytes);
                      Trace.log(Trace.DIAGNOSTIC, "  Client seed:", clientSeed);
                      Trace.log(Trace.DIAGNOSTIC, "  Server seed:", serverSeed);
                      Trace.log(Trace.DIAGNOSTIC, "  Encrypted password:", encryptedPassword);
                      Trace.log(Trace.DIAGNOSTIC, "  Password level: ", passwordLevel_);
                  }

                  // [0]= factor [1]=verification ID, [2]=remote ip address 
                  Object[] additonalAuthInfo = getAdditionalAuthInfo(null, xChgReply.getAAFIndicator(), additionalAuthFactor_);

                  AS400StrSvrDS req = new AS400StrSvrDS(serverId, userIDbytes, encryptedPassword, credVault_.getType(),
                          (byte[])additonalAuthInfo[0],  (byte[])additonalAuthInfo[1],  (byte[])additonalAuthInfo[2]);
                  
                  if (Trace.traceOn_) req.setConnectionID(connectionID);
                  req.write(outStream);

                  AS400StrSvrReplyDS reply = new AS400StrSvrReplyDS();
                  if (Trace.traceOn_) reply.setConnectionID(connectionID);
                  reply.read(inStream);

                  if (reply.getRC() != 0)
                  {
                      byte[] rcBytes = new byte[4];
                      BinaryConverter.intToByteArray(reply.getRC(), rcBytes, 0);
                      Trace.log(Trace.ERROR, "Start server failed with return code:", rcBytes);
                      throw AS400ImplRemote.returnSecurityException(reply.getRC(), null, userId_);
                  }

                  jobString = obtainJobIdForConnection(reply.getJobNameBytes());
              }
          }
          catch (IOException | AS400SecurityException | RuntimeException e)
          {                
              try
              {
                  // If we have host server connection, close it as well. 
                  if (socketContainer != null)
                      socketContainer.close();
              } 
              catch (Throwable ee) {
                  Trace.log(Trace.ERROR, "Error closing socket:", ee);
              }

              throw e;
          }
          
          // At this point the Socket connection is established. Now we need to set up
          // the AS400Server object before passing it back to the caller.

          // Construct a new server...
          server = (threadUsed_) ? new AS400ThreadedServer(this, service, socketContainer, jobString, virtualThreads_)
                                 : new AS400NoThreadServer(this, service, socketContainer, jobString);
      }
      else 
          server = getConnectionViaHOSTCNN(service, overridePort, forceNewConnection, skipSignonServer);

    if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Server started successfully. Job: " + server.jobString_);

    // Add the system to our list so we can return it on a subsequent connect()...
    serverList.addElement(server);

    fireConnectEvent(true, service);

    return server;
  }
  
  private  AS400Server getConnectionViaHOSTCNN(int service, int overridePort, boolean forceNewConnection, boolean skipSignonServer) throws AS400SecurityException, IOException
  {
      if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Attempting to create connection to " + AS400.getServerName(service) + " via as-hostcnn");

      SocketContainer socketContainer = null;
      int connectionID;
      String jobString = "";
      InputStream inStream = null;
      OutputStream outStream = null;
      boolean usingAuthenticatedHostcnnConnection = false;

      try
      {
          // -------
          // Prepare new connection request with server type 
          // -------
    
          int requestedServerID = AS400Server.getServerId(service);
    
          HCSPrepareNewConnDS HCSPrepDS = new HCSPrepareNewConnDS(requestedServerID);
          if (Trace.traceOn_) HCSPrepDS.setConnectionID(hostcnnServer_.getConnectionID());
          
          usingAuthenticatedHostcnnConnection = true;         
          HCSPrepareNewConnReplyDS HCSPrepReply = (HCSPrepareNewConnReplyDS) hostcnnServer_.sendAndReceive(HCSPrepDS);
          usingAuthenticatedHostcnnConnection = false;         
    
          if (HCSPrepReply.getRC() != 0)
          {
              byte[] rcBytes = new byte[4];
              BinaryConverter.intToByteArray(HCSPrepReply.getRC(), rcBytes, 0);
              Trace.log(Trace.ERROR, "Route prepare connection failed with return code:", rcBytes);
              throw AS400ImplRemote.returnSecurityException(HCSPrepReply.getRC(), null, userId_);
          }
    
          byte[] connectionReqID = HCSPrepReply.getConnReqID();
        
          // -------
          // Connect to HCS using new socket
          // -------
        
          if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Connect to as-hostcnn using new socket");
        
          // Note that you cannot override hostcnn port since users cannot connect to it directly!
          socketContainer = PortMapper.getServerSocket((systemNameLocal_) ? "localhost" : systemName_,  
                AS400.HOSTCNN, -1,  useSSLConnection_, socketProperties_,  mustUseNetSockets_);
        
          connectionID = socketContainer.hashCode();
          inStream     = socketContainer.getInputStream();
          outStream    = socketContainer.getOutputStream();
        
          // -------
          // Give new connection request with new connection request ID
          // -------
        
          if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Give new connection request with new connection request ID");
    
          HCSGetNewConnDS HCSGetDS = new HCSGetNewConnDS(connectionReqID);
          if (Trace.traceOn_) HCSGetDS.setConnectionID(connectionID);
          HCSGetDS.write(outStream); 
        
          HCSGetNewConnReplyDS HCSGetReply = new HCSGetNewConnReplyDS();
          if (Trace.traceOn_) HCSGetReply.setConnectionID(connectionID);
          HCSGetReply.read(inStream);
        
          if (HCSGetReply.getRC() != 0)
          {
              byte[] rcBytes = new byte[4];
              BinaryConverter.intToByteArray(HCSGetReply.getRC(), rcBytes, 0);
              Trace.log(Trace.ERROR, "Get new connection failed with return code:", rcBytes);
              throw AS400ImplRemote.returnSecurityException(HCSGetReply.getRC(), null, userId_);
          }
      
          // -------
          // Route new connection request with connection request ID
          // -------
    
          if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Route new connection request with connection request ID");
          HCSRouteNewConnDS HCSRouteDS = new HCSRouteNewConnDS(connectionReqID);
          if (Trace.traceOn_) HCSRouteDS.setConnectionID(connectionID);
        
          usingAuthenticatedHostcnnConnection = true;
          HCSRouteNewConnReplyDS HCSRouteReply = (HCSRouteNewConnReplyDS) hostcnnServer_.sendAndReceive(HCSRouteDS);
          usingAuthenticatedHostcnnConnection = false;
    
          if (HCSRouteReply.getRC() != 0)
          {
              byte[] rcBytes = new byte[4];
              BinaryConverter.intToByteArray(HCSRouteReply.getRC(), rcBytes, 0);
              Trace.log(Trace.ERROR, "Route new connection failed with return code:", rcBytes);
              throw AS400ImplRemote.returnSecurityException(HCSRouteReply.getRC(), null, userId_);
          }
                  
          jobString = obtainJobIdForConnection(HCSRouteReply.getJobNameBytes());

          // Construct a new server...
          return (threadUsed_) ? new AS400ThreadedServer(this, service, socketContainer, jobString, virtualThreads_)
                               : new AS400NoThreadServer(this, service, socketContainer, jobString);
      }
      catch (IOException | AS400SecurityException | RuntimeException e)
      {                
          try
          {
              // If error happened when communicating with hostcnn, close the socket.
              if (usingAuthenticatedHostcnnConnection)
              {
                  hostcnnServer_.forceDisconnect();
                  hostcnnServer_ = null;
              }
              
              // If we have host server connection, close it as well. 
              if (socketContainer != null)
                  socketContainer.close();
          } 
          catch (Throwable ee) {
              Trace.log(Trace.ERROR, "Error closing socket:", ee);
          }
          
          // If we used hostcnn server, then we cannot establish host server connection over an hostcnn connection. 
          if (!(e instanceof AS400SecurityException))
          {
              if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Exception during communication with as-hostcnn server");
              throw new ServerStartupException( ServerStartupException.CONNECTION_NOT_ESTABLISHED, e);
          }

          throw e;
      }
  }


  // The NLV to send to the system.
  String getNLV()
  {
      if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting NLV implementation:", clientNlv_);
      return clientNlv_;
  }

  // Get the encrypted password with the seeds folded in.
  byte[] getPassword(byte[] clientSeed, byte[] serverSeed) throws AS400SecurityException, IOException
  {
      int credType = credVault_.getType();

      if (credType == AS400.AUTHENTICATION_SCHEME_GSS_TOKEN)
      {
          try {
            if (kerbTicket_ != null)
                return kerbTicket_;
              return (gssCredential_ == null) 
                    ? TokenManager.getGSSToken(systemName_, gssName_)
                    : TokenManager2.getGSSToken(systemName_, gssCredential_);
          } catch (Throwable e) {
              Trace.log(Trace.ERROR, "Error retrieving GSSToken:", e);
              throw new AS400SecurityException(AS400SecurityException.KERBEROS_TICKET_NOT_VALID_RETRIEVE, e);
          }
      }
    
      if (credType == AS400.AUTHENTICATION_SCHEME_PROFILE_TOKEN
                 || credType == AS400.AUTHENTICATION_SCHEME_IDENTITY_TOKEN)
          return credVault_.getClearCredential();

      // If we got this far:
      // credType is AS400.AUTHENTICATION_SCHEME_PASSWORD
      
      byte[] encryptedPassword = null;

      if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Retrieving encrypted password.");

      if (credVault_.isEmpty())
      {
          if (!mustUseSuppliedProfile_
                  && AS400.onAS400
                  && AS400.currentUserAvailable()
                  && userId_.equals(CurrentUser.getUserID(AS400.nativeVRM.getVersionReleaseModification())))
          {
              encryptedPassword = CurrentUser.getUserInfo(AS400.nativeVRM.getVersionReleaseModification(), clientSeed, serverSeed, userId_);
              Trace.log(Trace.DIAGNOSTIC, "  encrypted password retrieved1");
          }
          else {
              Trace.log(Trace.ERROR, "Password is null.");
              throw new AS400SecurityException(AS400SecurityException.PASSWORD_NOT_SET);
          }
      }
      else
      {
          byte[] userIdEbcdic = SignonConverter.stringToByteArray(userId_);
          byte[] clearCredential = credVault_.getClearCredential();
          char[] password = BinaryConverter.byteArrayToCharArray(clearCredential);
          CredentialVault.clearArray(clearCredential);
      
          if (PASSWORD_TRACE) {
              Trace.log(Trace.DIAGNOSTIC, "  user ID:", userId_);
              Trace.log(Trace.DIAGNOSTIC, "  user ID EBCDIC:", userIdEbcdic);
              Trace.log(Trace.DIAGNOSTIC, "  password untwiddled: '" + new String(password) + "'");
              Trace.log(Trace.DIAGNOSTIC, "  client seed: ", clientSeed);
              Trace.log(Trace.DIAGNOSTIC, "  server seed: ", serverSeed);
          }

          if ((passwordLevel_< 2 ))
          {
              // Do DES encryption.

              // Prepend Q to numeric password. A "numeric password" is
              // a password that starts with a numeric digit.
              if (password.length > 0 && Character.isDigit(password[0]))
              {
                  if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Prepending Q to numeric password.");
                  char[] passwordWithQ = new char[password.length + 1];
                  passwordWithQ[0] = 'Q';
                  System.arraycopy(password, 0, passwordWithQ, 1, password.length);
                  CredentialVault.clearArray(password);
                  password = passwordWithQ;
              }

              if (password.length > 10)
              {
                  CredentialVault.clearArray(password);
                  Trace.log(Trace.ERROR, "Length of parameter 'password' is not valid:", password.length);
                  throw new AS400SecurityException(AS400SecurityException.PASSWORD_LENGTH_NOT_VALID);
              }
        
              byte[] passwordEbcdic; 
              passwordEbcdic = SignonConverter.upperCharsToByteArray(password);
              CredentialVault.clearArray(password);
              if (PASSWORD_TRACE) Trace.log(Trace.DIAGNOSTIC, "  password in ebcdic: ", passwordEbcdic);
              encryptedPassword = encryptPassword(userIdEbcdic, passwordEbcdic, clientSeed, serverSeed);
              CredentialVault.clearArray(passwordEbcdic);
          }
          else if (passwordLevel_< 4)
          {
              // Do SHA-1 encryption.
              byte[] userIdBytes = BinaryConverter.charArrayToByteArray(SignonConverter.byteArrayToCharArray(userIdEbcdic));

              // Screen out passwords that are empty
              if (password.length == 0) {
                  Trace.log(Trace.ERROR, "Parameter 'password' is empty.");
                  throw new AS400SecurityException(AS400SecurityException.SIGNON_CHAR_NOT_VALID);
              }
              
              // Screen out passwords that start with a star.
              if (password[0] == '*') {
                  Trace.log(Trace.ERROR,"Parameter 'password' begins with a '*' character.");
                  throw new AS400SecurityException(AS400SecurityException.SIGNON_CHAR_NOT_VALID);
              }
              
              // trimUnicodeSpace may return the same pointer if no spaces
              char[] trimmedPassword = trimUnicodeSpace(password);
              if (trimmedPassword != password) {
                CredentialVault.clearArray(password);
              }
              byte[] passwordBytes = BinaryConverter.charArrayToByteArray(trimmedPassword);
              CredentialVault.clearArray(trimmedPassword);
        
              byte[] sequence = { 0, 0, 0, 0, 0, 0, 0, 1 };

              if (PASSWORD_TRACE) {
                  Trace.log(Trace.DIAGNOSTIC, "Pre SHA-1 userIdBytes:", userIdBytes);
                  Trace.log(Trace.DIAGNOSTIC, "Pre SHA-1 passwordBytes:", passwordBytes);
                  Trace.log(Trace.DIAGNOSTIC, "Pre SHA-1 sequence:", sequence);
              }

              byte[] token = generateShaToken(userIdBytes, passwordBytes);
              CredentialVault.clearArray(passwordBytes);
         
              encryptedPassword = generateShaSubstitute(token, serverSeed, clientSeed, userIdBytes, sequence);
          }
          else
          {
              if (password.length == 0) {
                  Trace.log(Trace.ERROR, "Parameter 'password' is empty.");
                  throw new AS400SecurityException(AS400SecurityException.SIGNON_CHAR_NOT_VALID);
              }
              
              // Screen out passwords that start with a star.
              if (password[0] == '*') {
                  Trace.log(Trace.ERROR, "Parameter 'password' begins with a '*' character.");
                  throw new AS400SecurityException(AS400SecurityException.SIGNON_CHAR_NOT_VALID);
              }
              
              /*
               * If a sequence number is used, the client increments its password sequence "PWSEQs" by
               * one and saves it. PWSEQs is an 8-byte value. The implementation in the host servers always
               * uses a sequence number of 1.
               */
              byte[] sequence = { 0, 0, 0, 0, 0, 0, 0, 1 };
          
              //Generate salt for password level 4
              /*
               * The following steps describe the algorithm used to generate the pwdlvl 4 version of the password:
               * 1. Convert the 10-character blank padded user ID to upper case.
               * 2. Convert the 10-character blank padded upper case user ID to Unicode (CCSID 13488).
               * 3. Convert the password value to Unicode (CCSID 13488).
               * 4. Generate the salt value:
               *    a. Fill a 28-byte variable with Unicode blanks (0x0020).
               *    b. Copy the Unicode user ID value into the first 20 bytes of the 28-byte blank filled variable.
               *    c. Copy the last 8 bytes (last 4 characters) of the Unicode password value into the last 8 bytes of 
               *       the 28-byte variable. If the password is less than 4 characters, then copy the entire Unicode password value.
               *    d. Do a SHA-256 hash on the 28-byte variable to produce the 32-byte salt value.
               * 5. Generate the pwdlvl 4 version of the password using PBKDF2 with HMAC SHA-512 with the following values:
               *    Hash algorithm = HMAC SHA-512 (produces a 64-byte key)
               *    Data = Unicode password value
               *    Data Length = Length of Unicode password value
               *    Iterations = 10022
               *    Initialization vector length = 32
               *    Initialization vector (salt) = value generated in Step #4.
               */
              byte[] token = generatePwdTokenForPasswordLevel4(userId_, password);
              CredentialVault.clearArray(password);
              encryptedPassword = generateSha512Substitute(userId_, token, serverSeed, clientSeed, sequence);
          }
      }

      if (PASSWORD_TRACE) Trace.log(Trace.DIAGNOSTIC, "Encrypted password: ", encryptedPassword);

      return encryptedPassword;
  }

  public static byte[] getAESEncryptionKey(byte[] sharedPrivateKey) throws NoSuchAlgorithmException, AS400SecurityException
  {
    // Verify that the JVM can support this.
    // Check the key length.
    int keyLength = Cipher.getMaxAllowedKeyLength("AES");
    if (keyLength < 256) {
        // If the key length is too small, notify the user
        String message = "THE MAX AES KEY LENGTH IS " + keyLength
            + " AND MUST BE >= 256.  UPDATE THE JVM ("
            + System.getProperty("java.vm.info") + ") AT "
            + System.getProperty("java.home") + " WITH JCE";
        throw new AS400SecurityException(AS400SecurityException.UNKNOWN,
           message);
    }


    //
    // The 256 bit encryption key is derived in the following fashion.
    // DRDA AES encryption currently supports the 256-bit encryption key size.
    // The 256-bit encryption key is derived from the 512-bit Diffie-Hellman
    // shared private key as follows:
    // 1. Separate the 512-bit Diffie-Hellman shared private key (Z) into 2
    // equal length, 256-bit (32 byte), bit
    // strings (Z1 and Z2).
    // 2. Using the SHA-1 hash function, reduce the size of Z1 and Z2 to 2
    // 160-bit (20-byte) bit
    // strings, S1 and S2.
    // 3. Compute T = (last 8 bytes from S1) Exclusive OR (first 8 bytes from
    // S2).
    // 4. Set DerivedAES256Key = First 12 bytes of S1 || T || Last 12 bytes of
    // S2.
    //

    byte[] Z1 = new byte[32];
    byte[] Z2 = new byte[32];
    System.arraycopy(sharedPrivateKey, 0, Z1, 0, 32);
    System.arraycopy(sharedPrivateKey, 32, Z2, 0, 32);

    MessageDigest md = MessageDigest.getInstance("SHA-1");
    md.reset();
    byte[] s1 = md.digest(Z1);
    md.reset();
    byte[] s2 = md.digest(Z2);

    byte[] T = new byte[8];
    for (int i = 0; i < 8; i++) {
      T[i] = (byte) (s1[12 + i] ^ s2[i]);
    }

    byte[] encryptionKey = new byte[32];

    System.arraycopy(s1, 0, encryptionKey, 0, 12);
    System.arraycopy(T, 0, encryptionKey, 12, 8);
    System.arraycopy(s2, 8, encryptionKey, 20, 12);

    return encryptionKey;
  }

  // Get the encrypted userid with the seeds folded in.
  private byte[] getEncryptedUserid(byte[] sharedPrivateKey, byte[] serverSeed) throws AS400SecurityException, IOException
  {
    byte[] encryptedUserid = null;

    if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Retrieving encrypted userid.");

    if (credVault_.isEmpty())
    {
      if (!mustUseSuppliedProfile_
          && AS400.onAS400
          && AS400.currentUserAvailable()
          && userId_.equals(CurrentUser.getUserID(AS400.nativeVRM.getVersionReleaseModification())))
      {
       // Fall into code below 
      }
      else {
        Trace.log(Trace.ERROR, "Password is null and unable to determine password at later time");
        throw new AS400SecurityException(AS400SecurityException.PASSWORD_NOT_SET);
      }
    }
   
    {
      byte[] userIdEbcdic = SignonConverter.stringToByteArray(userId_);
      if (PASSWORD_TRACE) {
        Trace.log(Trace.DIAGNOSTIC, "  user ID:", userId_);
        Trace.log(Trace.DIAGNOSTIC, "  user ID EBCDIC:", userIdEbcdic);
        Trace.log(Trace.DIAGNOSTIC, "  sharedPrivateKey: ", sharedPrivateKey);
        Trace.log(Trace.DIAGNOSTIC, "  server seed: ", serverSeed);
      }

      if (userIdEbcdic.length > 10)
      {
        Trace.log(Trace.ERROR, "Length of parameter 'userId' is not valid:", userIdEbcdic.length);
        throw new AS400SecurityException(AS400SecurityException.USERID_LENGTH_NOT_VALID);
      }
      
      try 
      {
        if (sharedPrivateKey.length == 32)
        {
          // Do DES encryption

          // The 56 bit encryption key is derived from the middle 8 bytes of the
          // 32 byte shared secret key
          //
          byte[] encryptionKey = new byte[8];
          System.arraycopy(sharedPrivateKey, 12, encryptionKey, 0, 8);
          Trace.log(Trace.DIAGNOSTIC, "  sharedPrivateKey: ", encryptionKey);

          boolean parityAdjusted = DESKeySpec.isParityAdjusted(encryptionKey, 0);
          Trace.log(Trace.DIAGNOSTIC, "  isParityAdjusted: ", parityAdjusted);

          /*
           * if (!parityAdjusted) { // Fix the parity to see if it makes a
           * difference. for (int i = 0; i < encryptionKey.length; i++) {
           * encryptionKey[i] = fixParity(encryptionKey[i]); } parityAdjusted =
           * DESKeySpec.isParityAdjusted(encryptionKey, 0 ); }
           */

          Trace.log(Trace.DIAGNOSTIC, "  sharedPrivateKey(parity): ", encryptionKey);

          // Cipher c = Cipher.getInstance("DES");
          // Cipher c = Cipher.getInstance("DES/CBC/NoPadding");
          Cipher c = Cipher.getInstance("DES/CBC/PKCS5Padding");
          // Cipher c = Cipher.getInstance("DES/ECB/NoPadding");
          // Cipher c = Cipher.getInstance("DES/ECB/PKCS5Padding");

          DESKeySpec keySpec = new DESKeySpec(encryptionKey);

          SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
          SecretKey key = keyFactory.generateSecret(keySpec);

          // For DES, the initalization vector is the middle 8 bytes of the 32
          // byte public key
          byte[] iv = new byte[8];
          System.arraycopy(serverSeed, 12, iv, 0, 8);
          c.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));

          Trace.log(Trace.DIAGNOSTIC, "  initalizationVector: ", iv);

          encryptedUserid = c.doFinal(userIdEbcdic);
          Trace.log(Trace.DIAGNOSTIC, "  encryptedUserid: ", encryptedUserid);
        }
        else {
          // Do AES encryption.
          // AES/CBC/NoPadding (128)
          // AES/CBC/PKCS5Padding (128)
          // AES/ECB/NoPadding (128)

          encryptedUserid = encryptAES(sharedPrivateKey, serverSeed, userIdEbcdic);
        }

      } catch (Exception e) {
        e.printStackTrace();
        throw new AS400SecurityException(AS400SecurityException.PROFILE_TOKEN_NOT_VALID, e);
      }

    }
    
    if (PASSWORD_TRACE) Trace.log(Trace.DIAGNOSTIC, "Encrypted userid: ", encryptedUserid);

    return encryptedUserid;
  }

  public static byte[] encryptAES(byte[] sharedPrivateKey, byte[] serverSeed,
      byte[] value) throws NoSuchAlgorithmException,
      NoSuchPaddingException, AS400SecurityException, InvalidKeySpecException,
      InvalidKeyException, InvalidAlgorithmParameterException,
      IllegalBlockSizeException, BadPaddingException
  {
    byte[] encryptedValue;
    Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");

    byte[] encryptionKey = getAESEncryptionKey(sharedPrivateKey);

    SecretKey key;
    SecretKeySpec keySpec = new SecretKeySpec(encryptionKey, "AES");
    try {
      SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("AES");
      key = keyFactory.generateSecret(keySpec);
    } catch (java.security.NoSuchAlgorithmException nsae) {
      // Some JVMs do not have AES as a SecretKeyFactory.
      // Just use the keySpec as the secret key
      key = keySpec;
    }

    // For AES, the initialization vector is middle 16 bytes of 64 byte
    // server seen
    byte[] iv = new byte[16];
    System.arraycopy(serverSeed, 24, iv, 0, 16);
    c.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));
    encryptedValue = c.doFinal(value);
    return encryptedValue;
  }

  /*
   * private byte fixParity(byte b) { byte returnByte = (byte) (b & 0xFE); int
   * bitCount =0; b = (byte)(b >> 1); for (int i = 0; i < 7; i++) { if ((b &
   * 0x01) == 1) { bitCount++; } b = (byte)(b >> 1); } if ((bitCount % 2) == 0)
   * { returnByte = (byte) (returnByte | 1); } return returnByte; }
   */

  // Get the encrypted password for EUSRIDPWD.
  private byte[] getDdmEncryptedPassword(byte[] sharedPrivateKey, byte[] serverSeed) throws AS400SecurityException, IOException
  {
      int credType = credVault_.getType();

      if (credType == AS400.AUTHENTICATION_SCHEME_GSS_TOKEN)
      {
          try {
            if (kerbTicket_ != null)
                return kerbTicket_;
              return (gssCredential_ == null) 
                ? TokenManager.getGSSToken(systemName_, gssName_)
                : TokenManager2.getGSSToken(systemName_, gssCredential_);
          }
          catch (Throwable e) {
              Trace.log(Trace.ERROR, "Error retrieving GSSToken:", e);
              throw new AS400SecurityException(AS400SecurityException.KERBEROS_TICKET_NOT_VALID_RETRIEVE, e);
          }
      }
      else if (credType == AS400.AUTHENTICATION_SCHEME_PROFILE_TOKEN
                || credType == AS400.AUTHENTICATION_SCHEME_IDENTITY_TOKEN)
          return credVault_.getClearCredential();

      // If we got this far:
      // credType is AS400.AUTHENTICATION_SCHEME_PASSWORD

      byte[] encryptedPassword = null;

      if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Retrieving encrypted password.");

      if (credVault_.isEmpty()) {
          Trace.log(Trace.ERROR, "Password is null.");
          throw new AS400SecurityException(AS400SecurityException.PASSWORD_NOT_SET);
      }

      byte[] userIdEbcdic = SignonConverter.stringToByteArray(userId_);
      byte[] clearCredential = credVault_ .getClearCredential();
      char[] password = BinaryConverter.byteArrayToCharArray(clearCredential);
      CredentialVault.clearArray(clearCredential);
      
      if (PASSWORD_TRACE) {
        Trace.log(Trace.DIAGNOSTIC, "  user ID:", userId_);
        Trace.log(Trace.DIAGNOSTIC, "  user ID EBCDIC:", userIdEbcdic);
        Trace.log(Trace.DIAGNOSTIC, "  password untwiddled: '" + new String(password) + "'");
        Trace.log(Trace.DIAGNOSTIC, "  server seed: ", serverSeed);
      }

      try
      {
          // Prepend Q to numeric password. A "numeric password" is
          // a password that starts with a numeric digit.
          if (password.length > 0 && Character.isDigit(password[0]))
          {
              if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Prepending Q to numeric password.");
          
              char[] passwordWithQ = new char[password.length + 1];
              passwordWithQ[0] = 'Q';
              System.arraycopy(password, 0, passwordWithQ, 1, password.length);
              CredentialVault.clearArray(password);
              password = passwordWithQ;
          }

          // Codepage 500 is a DRDA standard. Using 37 fails for certain invariant characters, such
          // as the exclamation point ('!')
          byte[] passwordEbcdic = SignonConverter.charArrayToByteArray(password, "Cp500");
          CredentialVault.clearArray(password);
          if (PASSWORD_TRACE) Trace.log(Trace.DIAGNOSTIC, "  password in ebcdic: ", passwordEbcdic);

          if (sharedPrivateKey.length == 32)
          {
              // Do DES encryption.
              Cipher c = Cipher.getInstance("DES/CBC/PKCS5Padding");
              //
              // The 56 bit encryption key is derived from the middle 8 bytes of the
              // 32 byte shared secret key
              //
              byte[] encryptionKey = new byte[8];
              System.arraycopy(sharedPrivateKey, 12, encryptionKey, 0, 8);
              Trace.log(Trace.DIAGNOSTIC, "  sharedPrivateKey: ", encryptionKey);
    
              DESKeySpec keySpec = new DESKeySpec(encryptionKey);
              SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
              SecretKey key = keyFactory.generateSecret(keySpec);
    
              byte[] iv = new byte[8];
              System.arraycopy(serverSeed, 12, iv, 0, 8);
              c.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));
              encryptedPassword = c.doFinal(passwordEbcdic);
          }
          else
          {
              if (password.length == 0)
              {
                  Trace.log(Trace.ERROR, "Parameter 'password' is empty.");
                  throw new AS400SecurityException(AS400SecurityException.SIGNON_CHAR_NOT_VALID);
              }
          
              // Screen out passwords that start with a star.
              if (password[0] == '*')
              {
                  Trace.log(Trace.ERROR, "Parameter 'password' begins with a '*' character.");
                  throw new AS400SecurityException(AS400SecurityException.SIGNON_CHAR_NOT_VALID);
              }

              // Do AES encryption.
              encryptedPassword =  encryptAES(sharedPrivateKey, serverSeed, passwordEbcdic);
          }
      }
      catch (Exception e) {
          throw new AS400SecurityException(AS400SecurityException.PROFILE_TOKEN_NOT_VALID, e);
      }

      if (PASSWORD_TRACE) Trace.log(Trace.DIAGNOSTIC, "Encrypted password: ", encryptedPassword);

      return encryptedPassword;
  }

  // Get port number for service.
  @Override
  public int getServicePort(String systemName, int service) {
      return PortMapper.getServicePort(systemName, service, useSSLConnection_);
  }

  // Get secondary language library name.
  String getLanguageLibrary() {
      return languageLibrary_;
  }

  // Get system name.
  @Override
  public String getSystemName()
  {
      if (Trace.traceOn_)
          Trace.log(Trace.DIAGNOSTIC, "Getting implementation system name: " + systemName_ + " is local:", systemNameLocal_);
      return (systemNameLocal_) ? "localhost" : systemName_;
  }

  // Get user ID.
  String getUserId()
  {
      if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting implementation user ID:", userId_);
      return userId_;
  }

  // Get VRM.
  int getVRM()
  {
      // If we skipped the signon server, assume we are V7R1 or later.
      // The VRM will be fixed later after the other connection is made.
      if (signonInfo_ == null)
          return 0x070100;
    
      if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting implementation VRM.");
    
      int vrm = signonInfo_.version.getVersionReleaseModification();
      if (Trace.traceOn_)
      {
          byte[] vrmBytes = new byte[4];
          BinaryConverter.intToByteArray(vrm, vrmBytes, 0);
          Trace.log(Trace.DIAGNOSTIC, "Implementation VRM:", vrmBytes);
      }

      return vrm;
  }

  // Returns true of password type is SHA
  public boolean getPasswordType() {
      return (passwordLevel_ >= 2);
  }

  // Returns true of password level @AE6A
  public int getPasswordLevel() {
      return passwordLevel_;
  }

  // Check if service is connected.
  @Override
  public boolean isConnected(int service)
  {
      if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Checking for service connection implementation:", service);

      if (service == AS400.SIGNON)
          return (signonServer_ != null && signonServer_.isConnected());
      else if (service == AS400.HOSTCNN)
          return (hostcnnServer_ != null && hostcnnServer_.isConnected());

      Vector serverList = serverPool_[service];
      
      synchronized (serverList)
      {
          for (int i = serverList.size() - 1; i >= 0; i--)
          {
              if (((AS400Server) serverList.elementAt(i)).isConnected())
                  return true;
          }
          return false;
      }
  }

  // The list of ping requests for each server - ORDER MUST BE PRESERVED
  private List<ClientAccessDataStream> pingRequests_ = 
          new ArrayList<>(Arrays.asList(
                  new IFSPingReq(),                                    // AS400.FILE (0)
                  new PingDS(AS400Server.getServerId(AS400.PRINT)),    // AS400.PRINT (1)
                  new PingDS(AS400Server.getServerId(AS400.COMMAND)),  // AS400.COMMAND (2)
                  new PingDS(AS400Server.getServerId(AS400.DATAQUEUE)),// AS400.DATAQUEUE (3)
                  new PingDS(AS400Server.getServerId(AS400.DATABASE)), // AS400.DATABASE (4)
                  null,                                                // AS400.RECORDACCESS (5)
                  new PingDS(AS400Server.getServerId(AS400.CENTRAL)),  // AS400.CENTRAL (6)
                  new PingDS(AS400Server.getServerId(AS400.SIGNON)),   // AS400.SIGNON (7)
                  new PingDS(AS400Server.getServerId(AS400.HOSTCNN))   // AS400.HOSTCNN (8)
                  ));


  private static final int NO_PRIOR_SERVICE = -1;
  private int priorService_ = NO_PRIOR_SERVICE;

  
  private boolean doPingRequest(AS400Server connectedServer, boolean setPriorService) throws IOException, InterruptedException
  {
      int service = connectedServer.getService();

      DataStream request = pingRequests_.get(service);
      
      // A null request indicates server cannot be ping'ed. Simply return true.  For example, 
      // the DDM server cannot be ping'ed without creating an error entry in the job log.
      // Note that the FILE host server cannot be ping'ed, but we send dummy request and it will respond 
      // with something like "request not supported".
      DataStream reply = (request != null) ? connectedServer.sendAndReceive(request) : null;

      if (setPriorService)
          priorService_ = (service != AS400.RECORDACCESS) ? service : NO_PRIOR_SERVICE;
      
      // Note that an exception will be thrown if not connected. 
      return true;
  }
  
  // Check connection's current status.
  @Override
  public boolean isConnectionAlive()
  {
      if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Checking connection's current alive status");

      // The host server 'ping' request is supported starting in V7R1.
      if (getVRM() < 0x00070100)
      {
          Trace.log(Trace.DIAGNOSTIC,
              "The IBM i version is V6R1 or lower, therefore isConnectionAlive() defaults to the behavior of isConnected().");
          if (   isConnected(AS400.FILE) || isConnected(AS400.PRINT)
               || isConnected(AS400.COMMAND) || isConnected(AS400.DATAQUEUE)
               || isConnected(AS400.DATABASE) || isConnected(AS400.RECORDACCESS)
               || isConnected(AS400.CENTRAL) || isConnected(AS400.SIGNON))
              return true;
          
          return false;
      }

      // Note: The Signon Server is the "common gateway" (contains common code)
      // for the following servers: Network Print, Remote Command, Data Queue,
      // Database, Central, Signon.
      // The File Server and DDM Server (Record Level Access) are independent
      // from the Signon Server, and do not yet support a "ping" request.
      // To test connection, we will ping a connected connection to any of the
      // above services.
      
      boolean isAlive = false;
      AS400Server connectedServer = null;

      try
      {
          // First, try the previously-connected common service (if any).
          if (priorService_ != NO_PRIOR_SERVICE 
                  && (   priorService_ == AS400.PRINT     || priorService_ == AS400.COMMAND
                      || priorService_ == AS400.DATAQUEUE || priorService_ == AS400.DATABASE
                      || priorService_ == AS400.CENTRAL 
                      || priorService_ == AS400.SIGNON    || priorService_ == AS400.HOSTCNN))
          {
              connectedServer = getConnectedServer(new int[] { priorService_ });
          }

          if (connectedServer == null)
          {
              // Go through the list of common servers until we find a connected connection.
              connectedServer = getConnectedServer(new int[] { 
                      AS400.SIGNON, AS400.HOSTCNN, AS400.COMMAND,
                      AS400.DATABASE, AS400.PRINT, AS400.DATAQUEUE, AS400.CENTRAL });
          }

          // If no exception gets thrown, report that the connection is alive.
          if (connectedServer != null)
              isAlive = doPingRequest(connectedServer, true);

          // If we have a connection to the File Server, send the ping request.
          // Then if a reply comes back, swallow the "invalid request" error and
          // report that the connection is alive.
          if (connectedServer == null)
          {
              connectedServer = getConnectedServer(new int[] { AS400.FILE });
              if (connectedServer != null)
                  isAlive = doPingRequest(connectedServer, true);
          }

          // If all we have is a connection to the DDM Server, simply return true.
          // We don't have a way to ping the DDM server without creating an error
          // entry in the job log.
          if (connectedServer == null)
          {
              if (isConnected(AS400.RECORDACCESS))
              {
                  Trace.log(Trace.DIAGNOSTIC, "For the RECORDACCESS service, isConnectionAlive() defaults to the behavior of isConnected().");
                  isAlive = true;
              }

              priorService_ = NO_PRIOR_SERVICE;
          }
      }
      catch (Exception e)
      {
          if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, e);
          isAlive = false;
      }

      if (!isAlive)
          priorService_ = NO_PRIOR_SERVICE;

      return isAlive;
  }

  // Check connection's current status, for a specific service.
  @Override
  public boolean isConnectionAlive(int service)
  {
      if (Trace.traceOn_)
          Trace.log(Trace.DIAGNOSTIC, "Checking service connection's current alive status:", service);

      if (!isConnected(service))
          return false;

      // The host server 'ping' request is supported starting in V7R1.
      if (getVRM() < 0x00070100)
      {
          Trace.log(Trace.DIAGNOSTIC,
                  "The IBM i version is V6R1 or lower, therefore isConnectionAlive() defaults to the behavior of isConnected().");

          if (isConnected(service))
              return true;

          return false;
      }

      boolean isAlive = false;
      try
      {
          AS400Server connectedServer = getConnectedServer(new int[] { service });

          // If we have a connection to the specified service, send the ping request.
          // If no exception gets thrown, report that the connection is alive.
          if (connectedServer != null)
              isAlive = doPingRequest(connectedServer, false);
      }
      catch (Exception e)
      {
          if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, e);
          isAlive = false;
      }

      return isAlive;
  }

  private final AS400Server getConnectedServer(int[] services)
  {
      AS400Server server = null;

      for (int i = 0; i < services.length && server == null; i++)
      {
          int service = services[i];
          
          if (service == AS400.SIGNON)
              server = signonServer_;
          else if (service == AS400.HOSTCNN)
              server = hostcnnServer_;
          else
          {
              Vector serverList = serverPool_[service];
              synchronized (serverList)
              {
                  for (int ii = serverList.size() - 1; ii >= 0 && server == null; ii--)
                  {
                      if (((AS400Server) serverList.elementAt(ii)).isConnected())
                          server = (AS400Server) serverList.elementAt(ii);
                  }
              }
          }
      }

      return server;
  }

  // Indicates whether we've discovered that PTF SI29629 is missing on a V5R4
  // system.
  // This method is used by RemoteCommandImplRemote when deciding how to call an
  // API.
  // In particular: If the system is at V5R4, and is missing the PTF, we need to
  // call QCDRCMDI with only 5 parameters instead of 6.
  boolean isMissingPTF() {
    return detectedMissingPTF_;
  }

  // Indicates whether we are required to add the secondary language library.
  boolean isMustAddLanguageLibrary() {
    return mustAddLanguageLibrary_;
  }

  // Indicates whether we've discovered that we should skip further attempts to
  // add the secondary language library for this AS400Impl.
  boolean isSkipFurtherSettingOfLanguageLibrary() {
    return skipFurtherSettingOfLanguageLibrary_;
  }

  // Check if thread can be used.
  boolean isThreadUsed() {
    if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Checking implementation if thread is used:", threadUsed_);
    return threadUsed_;
  }

  // Load the appropriate implementation object.
  // param impl1 fully package named class name for native implementation.
  // param impl2 fully package named class name for remote implementation.
  Object loadImpl(String impl1, String impl2)
  {
      if (canUseNativeOptimization_)
      {
          Object impl = AS400.loadImpl(impl1);
          if (impl != null)
              return impl;

          if (Trace.traceOn_)
              Trace.log(Trace.DIAGNOSTIC, "Load of native implementation '" + impl1
                      + "' failed, attempting to load remote implementation.");
      }
      
      Object impl = AS400.loadImpl(impl2);
      if (impl != null)
          return impl;

      Trace.log(Trace.DIAGNOSTIC, "Load of remote implementation '" + impl2 + "' failed.");
      throw new ExtendedIllegalStateException(impl2, ExtendedIllegalStateException.IMPLEMENTATION_NOT_FOUND);
  }

  // Load a converter object into converter cache.
  @Override
  public void newConverter(int ccsid) throws UnsupportedEncodingException {
    ConverterImplRemote.getConverter(ccsid, this);
  }

  // Remove the connection event dispatcher.
  @Override
  public void removeConnectionListener(ConnectionListener listener) {
    if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Removing implementation connection listener.");
    dispatcher_ = null;
  }

  // Create AS400SecurityException from sign-on server return code.
  // Throw or return proper exception if exchange of random seeds or start
  // server request fail.
  static AS400SecurityException returnSecurityExceptionX(int rc) throws ServerStartupException {
    return returnSecurityException(rc, null, null);
  }

  static AS400SecurityException returnSecurityException(int rc,
      AS400Message[] messageList, String info) throws ServerStartupException
  {
    int exceptionCode = 0;
    switch (rc)
    {
    case 0x00010001:
      // Error on request data: invalid exchange attributes request.
      // Error on request data: invalid exchange random seeds request.
      throw new ServerStartupException(ServerStartupException.RANDOM_SEED_EXCHANGE_INVALID);
    case 0x00010002:
      // Error on request data: invalid server ID.
      throw new ServerStartupException(ServerStartupException.SERVER_ID_NOT_VALID);
    case 0x00010003:
      // Error on request data: invalid request ID.
      throw new ServerStartupException(ServerStartupException.REQUEST_ID_NOT_VALID);
    case 0x00010004:
      // Error on request data: invalid random seed:
      // - Zero is invalid.
      // - Greater than x'DFFFFFFFFFFFFFFF' is invalid.
      throw new ServerStartupException(ServerStartupException.RANDOM_SEED_INVALID);
    case 0x00010005:
      // Error on request data: random seed required when doing password
      // substitution.
      throw new ServerStartupException(ServerStartupException.RANDOM_SEED_REQUIRED);
    case 0x00010006:
      // Error on request data: invalid password encrypt indicator.
      throw new ServerStartupException(ServerStartupException.PASSWORD_ENCRYPT_INVALID);
    case 0x00010007:
      // Error on request data: invalid user ID (length).
      exceptionCode = AS400SecurityException.USERID_LENGTH_NOT_VALID;
      break;
    case 0x00010008:
      // Error on request data: invalid password or passphrase (length).
      exceptionCode = AS400SecurityException.PASSWORD_LENGTH_NOT_VALID;
      break;
    case 0x00010009:
      // Error on request data: invalid client version.
      // Error on request data: invalid send reply indicator.
      throw new ServerStartupException(ServerStartupException.REQUEST_DATA_ERROR);
    case 0x0001000A:
      // Error on request data: invalid data stream level.
      // Error on request data: invalid start server request:
      // - missing user ID.
      // - missing password or passphrase.
      // - missing authentication token.
      // - missing user ID, password or passphrase, and authentication token
      // were specified.
      // - both password and passphrase were specified on the request.
      throw new ServerStartupException(ServerStartupException.REQUEST_DATA_ERROR);
    case 0x0001000B:
      // Error on request data: invalid retrieve sign-on data request:
      // - missing user ID.
      // - missing password or passphrase.
      // - missing authentication token.
      // - user ID / password (or user ID / passphrase) and authentication token
      // both specified.
      // - problems with length fields in the request.
      exceptionCode = AS400SecurityException.SIGNON_REQUEST_NOT_VALID;
      break;
    case 0x0001000C:
      // Error on request data: invalid change password request:
      // - missing user ID.
      // - missing old password or passphrase.
      // - missing new password or passphrase.
      // - authentication token specified.
      // - problems with length fields in the request.
      // - missing old protected password / passphrase length.
      // - missing new protected password / passphrase length.
      // - missing protected password / passphrase CCSID value.
      exceptionCode = AS400SecurityException.PASSWORD_CHANGE_REQUEST_NOT_VALID;
      break;
    case 0x0001000D:
      // Error on request data: invalid protected old password or passphrase.
      exceptionCode = AS400SecurityException.PASSWORD_OLD_NOT_VALID;
      break;
    case 0x0001000E:
      // Error on request data: invalid protected new or clear text password or
      // passphrase.
      return new AS400SecurityException(AS400SecurityException.PASSWORD_NEW_NOT_VALID, messageList);
    case 0x0001000F:
      // Error on request data: invalid token type on generate authentication
      // token request.
      return new AS400SecurityException(AS400SecurityException.TOKEN_TYPE_NOT_VALID, messageList);
    case 0x00010010:
      // Error on request data: invalid generate authentication token request:
      // - missing authentication token.
      // - missing user ID.
      // - missing password or passphrase.
      // - user ID / password (or user ID / passphrase) and authentication token
      // both specified.
      // - problems with length fields in the request.
      exceptionCode = AS400SecurityException.GENERATE_TOKEN_REQUEST_NOT_VALID;
      break;
    case 0x00010011:
      // Error on request data: invalid authentication token (length).
      return new AS400SecurityException(AS400SecurityException.TOKEN_LENGTH_NOT_VALID, messageList);
    case 0x00010012:
      // Invalid generate authentication token for another user.
      exceptionCode = AS400SecurityException.GENERATE_TOKEN_REQUEST_NOT_VALID;
      break;
    case 0x00020001:
      // User ID errors: user ID unknown:
      // - user ID not found on system.
      // - EIM doesn't map Kerberos principal to a user profile.
      // - EIM maps the Kerberos principal to a user profile, but the profile
      // doesn't exist on this system.
      exceptionCode = AS400SecurityException.USERID_UNKNOWN;
      break;
    case 0x00020002:
      exceptionCode = AS400SecurityException.USERID_DISABLE;
      break;
    case 0x00020003:
      // User profile mismatch.
      exceptionCode = AS400SecurityException.USERID_MISMATCH;
      break;
    case 0x00030001:
      // Password errors: new password or passphase longer than maximum accepted
      // length.
      exceptionCode = AS400SecurityException.PASSWORD_NEW_TOO_LONG;
      break;
    case 0x00030002:
      // Password errors: new password or passphase shorter than minimum
      // accepted length.
      exceptionCode = AS400SecurityException.PASSWORD_NEW_TOO_SHORT;
      break;
    case 0x00030003:
      // Password errors: new password or passphase contains character used more
      // than once.
      exceptionCode = AS400SecurityException.PASSWORD_NEW_REPEAT_CHARACTER;
      break;
    case 0x00030004:
      // Password errors: new password or passphase has adjacent digits.
      exceptionCode = AS400SecurityException.PASSWORD_NEW_ADJACENT_DIGITS;
      break;
    case 0x00030005:
      // Password errors: new password or passphase contains a character
      // repeated consecutively.
      exceptionCode = AS400SecurityException.PASSWORD_NEW_CONSECUTIVE_REPEAT_CHARACTER;
      break;
    case 0x00030006:
      // Password errors: new password or passphase was previously used.
      exceptionCode = AS400SecurityException.PASSWORD_NEW_PREVIOUSLY_USED;
      break;
    case 0x00030007:
      // Password errors: new password or passphase must contain at least one
      // numeric.
      exceptionCode = AS400SecurityException.PASSWORD_NEW_NO_NUMERIC;
      break;
    case 0x00030008:
      // Password errors: new password or passphase contains an invalid
      // character.
      exceptionCode = AS400SecurityException.PASSWORD_NEW_CHARACTER_NOT_VALID;
      break;
    case 0x00030009:
      // Password errors: new password or passphase exists in a dictionary of
      // disallowed passwords or passphrases.
      exceptionCode = AS400SecurityException.PASSWORD_NEW_DISALLOWED;
      break;
    case 0x0003000A:
      // Password errors: new password or passphase contains user ID as part of
      // the password or passphrase.
      exceptionCode = AS400SecurityException.PASSWORD_NEW_USERID;
      break;
    case 0x0003000B:
      // Password errors: password or passphrase incorrect.
      exceptionCode = AS400SecurityException.PASSWORD_INCORRECT;
      break;
    case 0x0003000C:
      // Password errors: profile will be disabled on the next invalid password
      // or passphrase.
      exceptionCode = AS400SecurityException.PASSWORD_INCORRECT_USERID_DISABLE;
      break;
    case 0x0003000D:
      // Password errors: password or passphrase correct, but expired.
      exceptionCode = AS400SecurityException.PASSWORD_EXPIRED;
      break;
    case 0x0003000E:
      // Password errors: pre-V2R2 encrypted password.
      exceptionCode = AS400SecurityException.PASSWORD_PRE_V2R2;
      break;
    case 0x0003000F:
      // Password errors: new password or passphrase contains a character in the
      // same position as the last password or passphrase.
      exceptionCode = AS400SecurityException.PASSWORD_NEW_SAME_POSITION;
      break;
    case 0x00030010:
      // Password errors: Password is *NONE.
      exceptionCode = AS400SecurityException.PASSWORD_NONE;
      break;
    case 0x00030011:
      // Password errors: Password validation program failed the request.
      exceptionCode = AS400SecurityException.PASSWORD_NEW_VALIDATION_PROGRAM;
      break;
    case 0x00030012:
      // Password errors: Password change not allowed at this time.
      exceptionCode = AS400SecurityException.PASSWORD_CHANGE_NOT_ALLOWED;
      break;
    case 0x00030013:
      // Password errors: Password value is not valid.
      exceptionCode = AS400SecurityException.PASSWORD_VALUE_NOT_VALID;
      break;
    case 0x00040000:
      // General security errors, function not performed: No meaning. Reasons
      // for getting this return code include:
      // - QUSER's password expired.
      // - incorrect client CCSID passed on exchange client/server attributes
      // request.
      // - failures in program QTQRCSC.
      // - error generating server's random seed.
      // - errors while swapping profiles with the QSYGETPH and QWTSETP API's.
      // - user profile errors.
      // - errors while retrieving the user's password or passphrase.
      // - errors while changing passwords or passphrases.
      // - failures reported by APIs that are used by the signon server. These
      // API's include QWTCHGJB, QSYRUSRI, QSYCHGPR, QWCCVTDT, and QSYCHGPW.
      // - input to Kerberos validation routine is not properly formatted or is
      // not valid.
      // Check the QZSOSIGN jobs for possible messages.
      // Check the daemon jobs for possible messages.
      exceptionCode = AS400SecurityException.SECURITY_GENERAL;
      break;
    case 0x00040001:
      // General security errors, function not performed: QYSMPUT error due to
      // incorrect program data length.
      throw new ServerStartupException(
          ServerStartupException.CONNECTION_NOT_PASSED_LENGTH);
    case 0x00040002:
      // General security errors, function not performed: QYSMPUT error because
      // no responce was received (timeout) from the server job.
      throw new ServerStartupException(
          ServerStartupException.CONNECTION_NOT_PASSED_TIMEOUT);
    case 0x00040003:
      // General security errors, function not performed: QYSMPUT error because
      // the server job could not be started.
      throw new ServerStartupException(
          ServerStartupException.CONNECTION_NOT_PASSED_SERVER_NOT_STARTED);
    case 0x00040004:
      // General security errors, function not performed: QYSMPUT error because
      // the prestart job could not be started.
      throw new ServerStartupException(
          ServerStartupException.CONNECTION_NOT_PASSED_PRESTART_NOT_STARTED);
    case 0x00040005:
      // General security errors, function not performed: QYSMPUT error due to
      // subsystem problems.
      throw new ServerStartupException(
          ServerStartupException.CONNECTION_NOT_PASSED_SUBSYSTEM);
    case 0x00040006:
      // General security errors, function not performed: QYSMPUT error because
      // the server job is ending.
      throw new ServerStartupException(
          ServerStartupException.CONNECTION_NOT_PASSED_SERVER_ENDING);
    case 0x00040007:
      // General security errors, function not performed: QYSMPUT error because
      // the receiver area is too small.
      throw new ServerStartupException(
          ServerStartupException.CONNECTION_NOT_PASSED_RECEIVER_AREA);
    case 0x00040008:
      // General security errors, function not performed: QYSMPUT error because
      // the unknown or unrecoverable errors.
      throw new ServerStartupException(
          ServerStartupException.CONNECTION_NOT_PASSED_UNKNOWN);
    case 0x00040009:
      // General security errors, function not performed: QYSMPUT error because
      // the user profile for the server job soes not exist.
      throw new ServerStartupException(
          ServerStartupException.CONNECTION_NOT_PASSED_PROFILE);
    case 0x0004000A:
      // General security errors, function not performed: QYSMPUT error due to
      // authority problems releated to the profile for the server job.
      throw new ServerStartupException(
          ServerStartupException.CONNECTION_NOT_PASSED_AUTHORITY);
    case 0x0004000B:
      // General security errors, function not performed: QYSMPUT error because
      // the server job program was not found.
      throw new ServerStartupException(
          ServerStartupException.CONNECTION_NOT_PASSED_PROGRAM_NOT_FOUND);
    case 0x0004000C:
      // General security errors, function not performed: QYSMPUT error because
      // the daemon job is not authorized to use the library that contains the
      // server job.
      throw new ServerStartupException(
          ServerStartupException.CONNECTION_NOT_PASSED_LIBRARY_AUTHORITY);
    case 0x0004000D:
      // General security errors, function not performed: QYSMPUT error because
      // the daemon job is not authorized to the server job program.
      throw new ServerStartupException(
          ServerStartupException.CONNECTION_NOT_PASSED_PROGRAM_AUTHORITY);
    case 0x0004000E:
      // General security errors, function not performed: user not authorized to
      // generate token for another user.
      exceptionCode = AS400SecurityException.GENERATE_TOKEN_AUTHORITY_INSUFFICIENT;
      break;
    case 0x0004000F:
      // General security errors, function not performed: no memory is available
      // to allocate space needed for authorization.
      exceptionCode = AS400SecurityException.SERVER_NO_MEMORY;
      break;
    case 0x00040010:
      // General security errors, function not performed: error occurred when
      // converting data between code pages.
      exceptionCode = AS400SecurityException.SERVER_CONVERSION_ERROR;
      break;
    case 0x00040011:
      // General security errors, function not performed: error occurred using
      // EIM interfaces.
      exceptionCode = AS400SecurityException.SERVER_EIM_ERROR;
      break;
    case 0x00040012:
      // General security errors, function not performed: error occurred using
      // cryptographic interfaces.
      exceptionCode = AS400SecurityException.SERVER_CRYPTO_ERROR;
      break;
    case 0x00040013:
      // General security errors, function not performed: this version of token
      // is not supported by this version of code.
      exceptionCode = AS400SecurityException.SERVER_TOKEN_VERSION;
      break;
    case 0x00040014:
      // General security errors, function not performed: public key not found.
      exceptionCode = AS400SecurityException.SERVER_KEY_NOT_FOUND;
      break;
    case 0x00050001:
      // Exit program errors: error processing exit point.
      exceptionCode = AS400SecurityException.EXIT_POINT_PROCESSING_ERROR;
      break;
    case 0x00050002:
      // Exit program errors: resolving to exit program.
      exceptionCode = AS400SecurityException.EXIT_PROGRAM_RESOLVE_ERROR;
      break;
    case 0x00050003:
      // Exit program errors: user exit program call error.
      exceptionCode = AS400SecurityException.EXIT_PROGRAM_CALL_ERROR;
      break;
    case 0x00050004:
      // Exit program errors: user exit program denied request.
      exceptionCode = AS400SecurityException.EXIT_PROGRAM_DENIED_REQUEST;
      break;
    case 0x00060001:
      // Authentication token errors: profile token or identity token not valid.
      exceptionCode = AS400SecurityException.PROFILE_TOKEN_NOT_VALID;
      break;
    case 0x00060002:
      // Authentication token errors: maximum number of profile tokens for the
      // system already generated.
      exceptionCode = AS400SecurityException.PROFILE_TOKEN_NOT_VALID_MAXIMUM;
      break;
    case 0x00060003:
      // Authentication token errors: invalid value sent for timeout interval.
      exceptionCode = AS400SecurityException.PROFILE_TOKEN_NOT_VALID_TIMEOUT_NOT_VALID;
      break;
    case 0x00060004:
      // Authentication token errors: invalid type of profile token request.
      exceptionCode = AS400SecurityException.PROFILE_TOKEN_NOT_VALID_TYPE_NOT_VALID;
      break;
    case 0x00060005:
      // Authentication token errors: existing profile token isn't regenerable,
      // can't be used to generate a new profile token.
      exceptionCode = AS400SecurityException.PROFILE_TOKEN_NOT_VALID_NOT_REGENERABLE;
      break;
    case 0x00060006:
      // Authentication token errors: Kerberos ticket not valid - consistency
      // checks failed.
      exceptionCode = AS400SecurityException.KERBEROS_TICKET_NOT_VALID_CONSISTENCY;
      break;
    case 0x00060007:
      // Authentication token errors: requested mechanisms not supported by
      // local system.
      exceptionCode = AS400SecurityException.KERBEROS_TICKET_NOT_VALID_MECHANISM;
      break;
    case 0x00060008:
      // Authentication token errors: credentials not available or not valid for
      // this context.
      exceptionCode = AS400SecurityException.KERBEROS_TICKET_NOT_VALID_CREDENTIAL_NOT_VALID;
      break;
    case 0x00060009:
      // Authentication token errors: Kerberos token or identity token contains
      // incorrect signature.
      exceptionCode = AS400SecurityException.KERBEROS_TICKET_NOT_VALID_SIGNATURE;
      break;
    case 0x0006000A:
      // Authentication token errors: credentials no longer valid.
      exceptionCode = AS400SecurityException.KERBEROS_TICKET_NOT_VALID_CREDENTIAL_NO_LONGER_VALID;
      break;
    case 0x0006000B:
      // Authentication token errors: consistency checks on the credantial
      // structure failed, or a mismatch exists between an authentication token
      // and information provided to the identity token functions.
      exceptionCode = AS400SecurityException.KERBEROS_TICKET_NOT_VALID_CONSISTENCY;
      break;
    case 0x0006000C:
      // Authentication token errors: failure of verification routine.
      exceptionCode = AS400SecurityException.KERBEROS_TICKET_NOT_VALID_VERIFICATION;
      break;
    case 0x0006000D:
      // Authentication token errors: EIM configuration error, or an EIM
      // identifier was not found, or an application identifier was not found.
      exceptionCode = AS400SecurityException.KERBEROS_TICKET_NOT_VALID_EIM;
      break;
    case 0x0006000E:
      // Authentication token errors: Kerberos principal maps to a system
      // profile which can not be used to sign on.
      exceptionCode = AS400SecurityException.KERBEROS_TICKET_NOT_VALID_SYSTEM_PROFILE;
      break;
    case 0x0006000F:
      // Authentication token errors: Kerberos principal maps to multiple user
      // profile names, or more than one EIM entry was found for an identity
      // token.
      exceptionCode = AS400SecurityException.KERBEROS_TICKET_NOT_VALID_MULTIPLE_PROFILES;
      break;
    case 0x00070001:
      // Generate token errors: can not connect to the system EIM domain.
      exceptionCode = AS400SecurityException.GENERATE_TOKEN_CAN_NOT_CONNECT;
      break;
    case 0x00070002:
      // Generate token errors: can not change the CCSID to use for EIM requests
      // to 13488.
      exceptionCode = AS400SecurityException.GENERATE_TOKEN_CAN_NOT_CHANGE_CCSID;
      break;
    case 0x00070003:
      // Generate token errors: can not obtain the EIM registry name.
      exceptionCode = AS400SecurityException.GENERATE_TOKEN_CAN_NOT_OBTAIN_NAME;
      break;
    case 0x00070004:
      // Generate token errors: no mapping exists between the WebSphere Portal
      // user identity and an IBM i user profile.
      exceptionCode = AS400SecurityException.GENERATE_TOKEN_NO_MAPPING;
      break;
    default:
      // Internal errors or unexpected return codes.
      info = "Unexpected rc=0x"+Integer.toHexString(rc);
      exceptionCode = AS400SecurityException.UNKNOWN;
    }
    
    // Exception code set above
    return (info != null)
      ? new AS400SecurityException(exceptionCode, messageList, info)
      : new AS400SecurityException(exceptionCode, messageList);
  }

  static AS400Message[] parseMessages(byte[] data, int offset, ConverterImplRemote converter) throws IOException
  {
      int originalOffset = offset;
      int messageNumber = 0;
      while (offset < data.length - 1)
      {
          if (BinaryConverter.byteArrayToShort(data, offset + 4) != 0x112A)
              offset += BinaryConverter.byteArrayToInt(data, offset);
          else
          {
              messageNumber = BinaryConverter.byteArrayToShort(data, offset + 6);
              break;
          }
      }

      if (messageNumber == 0)
          return null;

      AS400Message[] messageList = new AS400Message[messageNumber];

      offset = originalOffset;
      for (int i = 0; i < messageNumber; ++i)
      {
          while (offset < data.length - 1)
          {
              if (BinaryConverter.byteArrayToShort(data, offset + 4) != 0x112B)
                  offset += BinaryConverter.byteArrayToInt(data, offset);
              else
              {
                  messageList[i] = parseMessage(data, offset + 6, converter);
                  break;
              }
          }
          offset += BinaryConverter.byteArrayToInt(data, offset);
      }

      return messageList;
  }

  static AS400Message parseMessage(byte[] data, int offset, ConverterImplRemote converter) throws IOException
  {
    AS400Message message = new AS400Message();
    int textCcsid = BinaryConverter.byteArrayToInt(data, offset);
    message.setTextCcsid(textCcsid);
    offset += 4;
    int substitutionCcsid = BinaryConverter.byteArrayToInt(data, offset);
    message.setSubstitutionDataCcsid(substitutionCcsid);
    offset += 4;
    message.setSeverity(BinaryConverter.byteArrayToUnsignedShort(data, offset));
    offset += 2;
    int messageTypeLength = BinaryConverter.byteArrayToInt(data, offset);
    offset += 4;
    message.setType((data[offset] & 0x0F) * 10 + (data[offset + 1] & 0x0F));
    offset += messageTypeLength;
    int messageIdLength = BinaryConverter.byteArrayToInt(data, offset);
    offset += 4;
    message.setID(converter.byteArrayToString(data, offset, messageIdLength));
    offset += messageIdLength;
    int messageFileNameLength = BinaryConverter.byteArrayToInt(data, offset);
    offset += 4;
    message.setFileName(converter.byteArrayToString(data, offset, messageFileNameLength).trim());
    offset += messageFileNameLength;
    int messageFileLibraryNameLength = BinaryConverter.byteArrayToInt(data, offset);
    offset += 4;
    message.setLibraryName(converter.byteArrayToString(data, offset, messageFileLibraryNameLength).trim());
    offset += messageFileLibraryNameLength;
    int messageTextLength = BinaryConverter.byteArrayToInt(data, offset);
    offset += 4;
    message.setText(converter.byteArrayToString(data, offset, messageTextLength));
    offset += messageTextLength;
    int messageSubstitutionTextLength = BinaryConverter.byteArrayToInt(data, offset);
    offset += 4;
    byte[] substitutionData = new byte[messageSubstitutionTextLength];
    System.arraycopy(data, offset, substitutionData, 0, messageSubstitutionTextLength);
    message.setSubstitutionData(substitutionData);
    offset += messageSubstitutionTextLength;
    int messageHelpLength = BinaryConverter.byteArrayToInt(data, offset);
    offset += 4;
    message.setHelp(converter.byteArrayToString(data, offset, messageHelpLength));
    return message;
  }

  @Override
  public void setGSSCredential(GSSCredential gssCredential)
  {
    if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting GSS credential into impl: '" + gssCredential + "'");
    gssCredential_ = gssCredential;
  }

  // Indicates that we've discovered that PTF SI29629 is missing on a V5R4
  // system.
  // This method is used by RemoteCommandImplRemote.
  void setMissingPTF() {
    detectedMissingPTF_ = true;
  }

  // Set secondary language library name.
  void setLanguageLibrary(String libName) {
    languageLibrary_ = libName;
  }

  // Indicates that we've discovered that we should skip further attempts to set
  // the secondary language library for this AS400Impl.
  void setSkipFurtherSettingOfLanguageLibrary() {
    skipFurtherSettingOfLanguageLibrary_ = true;
  }

  // Set port for service.
  @Override
  public void setServicePort(String systemName, int service, int port) {
    PortMapper.setServicePort(systemName, service, port, useSSLConnection_);
  }

  // Set all the ports for a system name to the defaults.
  @Override
  public void setServicePortsToDefault(String systemName) {
    PortMapper.setServicePortsToDefault(systemName);
  }

  // Flag that indicates if we must add the secondary language library.
  private boolean mustAddLanguageLibrary_ = false;
  // Flag that indicates if we must use network sockets and not unix domain
  // sockets.
  private boolean mustUseNetSockets_ = false;
  // Flag that indicates if we must not use the current profile.
  private boolean mustUseSuppliedProfile_ = false;

  // Set the state variables for this implementation object.
  @Override
  public void setState(SSLOptions useSSLConnection,
      boolean canUseNativeOptimization, boolean threadUsed, boolean virtualThreads, int ccsid,
      String nlv, SocketProperties socketProperties, String ddmRDB,
      boolean mustUseNetSockets, boolean mustUseSuppliedProfile,
      boolean mustAddLanguageLibrary)
  {
      if (Trace.traceOn_)
      {
          Trace.log(Trace.DIAGNOSTIC, "Setting up AS400 implementation object:");
          Trace.log(Trace.DIAGNOSTIC, "  Enable SSL connections: " + useSSLConnection);
          Trace.log(Trace.DIAGNOSTIC, "  Native optimizations allowed:", canUseNativeOptimization);
          Trace.log(Trace.DIAGNOSTIC, "  Use threaded communications:", threadUsed);
          Trace.log(Trace.DIAGNOSTIC, "  User specified CCSID:", ccsid);
          Trace.log(Trace.DIAGNOSTIC, "  NLV:", nlv);
          Trace.log(Trace.DIAGNOSTIC, "  Socket properties: " + socketProperties);
          Trace.log(Trace.DIAGNOSTIC, "  DDM RDB:", ddmRDB);
          Trace.log(Trace.DIAGNOSTIC, "  Must use net sockets:", mustUseNetSockets);
          Trace.log(Trace.DIAGNOSTIC, "  Must use supplied profile:", mustUseSuppliedProfile);
          Trace.log(Trace.DIAGNOSTIC, "  Must add language library:", mustAddLanguageLibrary);
      }
      
      useSSLConnection_ = useSSLConnection;
      canUseNativeOptimization_ = canUseNativeOptimization;
      threadUsed_ = threadUsed;
      virtualThreads_ = virtualThreads; 
      if (ccsid != 0)
      {
          userOverrideCcsid_ = true;
          ccsid_ = ccsid;
      }
      clientNlv_ = nlv;
      socketProperties_ = socketProperties;
      ddmRDB_ = ddmRDB;
      mustAddLanguageLibrary_ = mustAddLanguageLibrary;
      mustUseNetSockets_ = mustUseNetSockets;
      mustUseSuppliedProfile_ = mustUseSuppliedProfile;
  }
  
  @Override
  public SignonInfo setState(AS400Impl impl, CredentialVault credVault)
  {
      if (hostcnnServer_ != null) {
          Trace.log(Trace.ERROR, "Attempt to set as-hostcnn server when one already exists.");
          throw new InternalErrorException(InternalErrorException.SECURITY_INVALID_STATE);
      }
      
      if (!(impl instanceof AS400ImplRemote) || ((AS400ImplRemote)impl).hostcnnServer_ == null)
          return null;

      AS400ImplRemote parentImpl = (AS400ImplRemote)impl;
      parentImpl.hostcnnServer_.addReference();
      hostcnnServer_ = parentImpl.hostcnnServer_;

      if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Server as-hostcnn being passed to impl, server is: " + hostcnnServer_);
      
      // TODO - need to verify what can be copied. 
      
      credVault_ =  credVault.clone();
      systemName_ = parentImpl.systemName_;
      userId_ = parentImpl.userId_;
      systemNameLocal_ = parentImpl.systemNameLocal_;
      gssCredential_ = parentImpl.gssCredential_;
      gssName_ = parentImpl.gssName_;
      useSSLConnection_ = new SSLOptions(parentImpl.useSSLConnection_);
      canUseNativeOptimization_ = parentImpl.canUseNativeOptimization_;
      threadUsed_ = parentImpl.threadUsed_;
      ccsid_ = parentImpl.ccsid_;
      userOverrideCcsid_ = parentImpl.userOverrideCcsid_;
      clientNlv_ = parentImpl.clientNlv_;
      languageLibrary_ = parentImpl.languageLibrary_;
      skipFurtherSettingOfLanguageLibrary_ = parentImpl.skipFurtherSettingOfLanguageLibrary_;
      detectedMissingPTF_ = parentImpl.detectedMissingPTF_;
      ddmRDB_ = parentImpl.ddmRDB_;
      version_ = parentImpl.version_;
      serverLevel_ = parentImpl.serverLevel_;
      passwordLevel_ = parentImpl.passwordLevel_;
      isPasswordTypeSet_ = parentImpl.isPasswordTypeSet_;
      signonInfo_ = parentImpl.signonInfo_; // Use same signonInfo object. 
      aafIndicator_ = parentImpl.aafIndicator_;
      proxySeed_ = parentImpl.proxySeed_;
      remoteSeed_ = parentImpl.remoteSeed_;
      userHandle_ = UNINITIALIZED;
      serverSeed_ = parentImpl.serverSeed_;
      clientSeed_ = parentImpl.clientSeed_;
      hostcnn_serverSeed_ = parentImpl.hostcnn_serverSeed_;
      hostcnn_clientSeed_ = parentImpl.hostcnn_clientSeed_;
      additionalAuthFactor_ = parentImpl.additionalAuthFactor_; 
      bidiStringType_ = parentImpl.bidiStringType_;
      socketProperties_ = parentImpl.socketProperties_;
      
      if (swapToPH76_ != null)
      {
          swapToPH76_ = parentImpl.swapToPH76_;
          swapToPHUserID76_ = parentImpl.swapToPHUserID76_;
          threadLocalSwapToPH_.set(swapToPH76_);
          threadLocalSwapToPHUserID_.set(swapToPHUserID76_);
          parentImpl.swapToPHRefCount_.incrementAndGet();
          swapToPHRefCount_ = parentImpl.swapToPHRefCount_;
      }
      
      return signonInfo_;
  }

  // Only called by changePassword
  private SignonInfo signon2(String systemName, boolean systemNameLocal,
                             String userId, byte[] bytes, int byteType, 
                             char[] additionalAuthenticationFactor)  throws AS400SecurityException, IOException
  {
      CredentialVault tempVault;

      if (bytes == null)
          tempVault = new PasswordVault();
      else if (byteType == AS400.AUTHENTICATION_SCHEME_GSS_TOKEN)
          tempVault = new GSSTokenVault(bytes);
      else
      {
          //
          // Create a credential vault based on the type of bytes,
          // and populate it with the raw decoded credential bytes.
          //
          byte[] newBytes = CredentialVault.decode(proxySeed_, remoteSeed_, bytes);
          switch (byteType)
          {
              case AS400.AUTHENTICATION_SCHEME_PASSWORD:
                  tempVault = new PasswordVault(newBytes);
                  break;
              case AS400.AUTHENTICATION_SCHEME_PROFILE_TOKEN:
                  tempVault = new ProfileTokenVault(newBytes);
                  break;
              case AS400.AUTHENTICATION_SCHEME_IDENTITY_TOKEN:
                  tempVault = new IdentityTokenVault(newBytes);
                  break;
              default:
                  Trace.log(Trace.ERROR, "Unsupported byte type: " + byteType);
                  throw new InternalErrorException(InternalErrorException.UNKNOWN, byteType);
          }

          CredentialVault.clearArray(newBytes);

          // This code is a bit strange, but necessary.
          // We decoded the raw bytes above and created a new credential vault using
          // the decoded bytes.
          // This is because a credential vault always requires clear bytes when
          // constructed.
          // Once constructed, we must encode the credential in the vault using the
          // same seeds we just decoded it with.
          // Why? Because the signon() method we are going to invoke expects that
          // the credential in the vault will always be encoded,
          // so we satisfy this expectation by re-encoding here.
          tempVault.storeEncodedUsingExternalSeeds(proxySeed_, remoteSeed_);
      }

      return signon(systemName, systemNameLocal, userId, tempVault, gssName_, additionalAuthenticationFactor);
  }

  // Exchange sign-on flows with sign-on server.
  @Override
  public SignonInfo signon(String systemName, boolean systemNameLocal,  String userId, CredentialVault vault, String gssName)
      throws AS400SecurityException, IOException
  {
      return signon(systemName, systemNameLocal, userId, vault, gssName, null);
  }

  // Exchange sign-on flows with sign-on server.
  @Override
  public SignonInfo signon(String systemName, boolean systemNameLocal, String userId, CredentialVault vault, String gssName,
                           char[] additionalAuthFactor) throws AS400SecurityException, IOException
  {
      // If userid, or system has changed, we need to disconnect any connection to HOSTCNN and SIGNON.
      if (hostcnnServer_ != null && 
              ( !systemName_.equalsIgnoreCase(systemName) || !userId_.equalsIgnoreCase(userId)))
      {
          if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Authentication information has changed...");

          hostcnnDisconnect();
          signonDisconnect();
      }
              
      systemName_           = systemName;
      systemNameLocal_      = systemNameLocal;
      userId_               = userId;
      gssName_              = gssName;
      setAdditionalAuthenticationFactor(additionalAuthFactor);

      // We are accepting a credential vault from the caller.
      // This vault will replace our existing one.
      // So if our existing one is not the same as the one being given to us,
      // then empty our existing one since we are discarding it.
      if (!vault.equals(credVault_))
          credVault_.empty();
    
      credVault_ = vault;

      // If not GSS token, must first decode the credential using the seeds that were previously
      // exchanged between the public AS400 class and this class.
      // Note: The called method ends up storing a "twiddled" representation of the credential info.
      if (credVault_.getType() != AS400.AUTHENTICATION_SCHEME_GSS_TOKEN)
          credVault_.storeEncodedUsingInternalSeeds(proxySeed_, remoteSeed_);

      proxySeed_ = null;
      remoteSeed_ = null;

      // Try to use native signon if we can. 
      if (canUseNativeOptimization_)
      {
          if (AS400.nativeVRM.getVersionReleaseModification() > 0x00070500 
                    && additionalAuthFactor_ != null && useSSLConnection_ != null)
              hostcnnConnect(true);
          
          if (hostcnnServer_ == null)
          {
              nativeSignon();
              return signonInfo_;
          }
      }
      else
      {
          // Get connection to authenticating server. Will try hostcnn, and if that is not up, fall back to signon.
          hostcnnConnect(true);
          if (hostcnnServer_ == null)
              signonConnect();
      }
    
      // -------
      // If we are connected to hostcnn, just use that to retrieve user information
      // -------
      if (hostcnnServer_ != null)
      {
          if (PASSWORD_TRACE) {
              Trace.log(Trace.DIAGNOSTIC, "Sending Retrieve Signon Information Request via as-hostcnn...");
              Trace.log(Trace.DIAGNOSTIC, "  User ID:", userId);
              Trace.log(Trace.DIAGNOSTIC, "  Client seed:", hostcnn_clientSeed_);
              Trace.log(Trace.DIAGNOSTIC, "  Server seed:", hostcnn_serverSeed_);
          }
        
          try
          {
              HCSUserInfoDS signonReq = new HCSUserInfoDS();
              HCSUserInfoReplyDS signonRep = (HCSUserInfoReplyDS) hostcnnServer_.sendAndReceive(signonReq);
    
              int rc = signonRep.getRC();
              if (rc != 0)
              {
                  byte[] rcBytes = new byte[4];
                  BinaryConverter.intToByteArray(rc, rcBytes, 0);
                  Trace.log(Trace.ERROR, "Retrieve Signon Information Request failed with return code:", rcBytes);
                  throw AS400ImplRemote.returnSecurityException(rc, 
                      signonRep.getErrorMessages(ConverterImplRemote.getConverter( ExecutionEnvironment.getBestGuessAS400Ccsid(), this)), userId);
              }
            
              if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Retrieve Signon Information Request successful.");
            
              signonInfo_ = new SignonInfo();
              signonInfo_.currentSignonDate = signonRep.getCurrentSignonDate();
              signonInfo_.lastSignonDate = signonRep.getLastSignonDate();
              signonInfo_.expirationDate = signonRep.getExpirationDate();
              signonInfo_.PWDexpirationWarning = signonRep.getPWDExpirationWarning();
              signonInfo_.version = version_;
              signonInfo_.serverCCSID = signonRep.getServerCCSID();
              
              signonInfo_.userId = userId_;
    
              if (DataStream.getDefaultConverter() == null)
              {
                  if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Server reports CCSID:", signonInfo_.serverCCSID);
                  DataStream.setDefaultConverter(ConverterImplRemote.getConverter(signonInfo_.serverCCSID, this));
              }
          }
          catch (IOException | AS400SecurityException e)
          {
              Trace.log(Trace.ERROR, "Get user information failed:", e);
              hostcnnServer_.forceDisconnect();
              hostcnnServer_ = null;
              throw e;
          }
        
          return signonInfo_;
      }
    
      // -------
      // If here, use signon server to retrieve user information
      // -------
      try
      {
          byte[] userIDbytes = (credVault_.getType() == AS400.AUTHENTICATION_SCHEME_PASSWORD) 
                ? SignonConverter.stringToByteArray(userId) : null;
          byte[] encryptedPassword = (credVault_.getType() == AS400.AUTHENTICATION_SCHEME_GSS_TOKEN)
                ? credVault_.getClearCredential() : getPassword(clientSeed_, serverSeed_);

          if (PASSWORD_TRACE) {
              Trace.log(Trace.DIAGNOSTIC, "Sending Retrieve Signon Information Request...");
              Trace.log(Trace.DIAGNOSTIC, "  User ID:", userId);
              Trace.log(Trace.DIAGNOSTIC, "  User ID bytes:", userIDbytes);
              Trace.log(Trace.DIAGNOSTIC, "  Client seed:", clientSeed_);
              Trace.log(Trace.DIAGNOSTIC, "  Server seed:", serverSeed_);
              Trace.log(Trace.DIAGNOSTIC, "  Encrypted password:", encryptedPassword);
          }

          SignonInfoReq signonReq = new SignonInfoReq(userIDbytes, encryptedPassword, credVault_.getType(), serverLevel_, additionalAuthFactor_);
          CredentialVault.clearArray(encryptedPassword);
          SignonInfoRep signonRep = (SignonInfoRep) signonServer_.sendAndReceive(signonReq);
          signonReq.clear(); 
        
          if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Read security validation reply...");
        
          int rc = signonRep.getRC();
          if (rc != 0)
          {
              byte[] rcBytes = new byte[4];
              BinaryConverter.intToByteArray(rc, rcBytes, 0);
              Trace.log(Trace.ERROR, "Security validation failed with return code:", rcBytes);
              throw AS400ImplRemote.returnSecurityException(rc, 
                     signonRep.getErrorMessages(ConverterImplRemote.getConverter( ExecutionEnvironment.getBestGuessAS400Ccsid(), this)), userId);
          }

          if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Security validated successfully.");
        
          signonInfo_ = new SignonInfo();
          signonInfo_.currentSignonDate = signonRep.getCurrentSignonDate();
          signonInfo_.lastSignonDate = signonRep.getLastSignonDate();
          signonInfo_.expirationDate = signonRep.getExpirationDate();
          signonInfo_.PWDexpirationWarning = signonRep.getPWDExpirationWarning();
          signonInfo_.version = version_;
          signonInfo_.serverCCSID = signonRep.getServerCCSID();
        
          if (userId_.length() == 0)
          {
              byte[] b = signonRep.getUserIdBytes();
              if (b != null) {
                    userId_ = SignonConverter.byteArrayToString(b);
                    signonInfo_.userId = userId_;
              }
          }

          if (DataStream.getDefaultConverter() == null)
          {
              if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Signon server reports CCSID:", signonInfo_.serverCCSID);
              DataStream.setDefaultConverter(ConverterImplRemote.getConverter(signonInfo_.serverCCSID, this));
          }
      }
      catch (IOException | AS400SecurityException e)
      {
          Trace.log(Trace.ERROR, "Signon failed:", e);
          signonServer_.forceDisconnect();
          signonServer_ = null;
          throw e;
      }
      
      return signonInfo_;
  }
  
  private void nativeSignon() throws AS400SecurityException, IOException 
  {
      if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Native signon authentication...");

      // If -Xshareclasses is specified when using Java on an IBM i system
      // then classes cannot be loaded when the profile is swapped.  
      // Load the classes before doing the swap.
      Class x = BinaryConverter.class;
      x = GregorianCalendar.class;
      x = SignonInfo.class;
      x = com.ibm.as400.access.NLSImplNative.class;
      x = com.ibm.as400.access.NLSImplRemote.class;
      boolean didSwap = swapTo();
      try
      {
          byte[] data = AS400ImplNative.signonNative(SignonConverter.stringToByteArray(userId_));
          GregorianCalendar date = new GregorianCalendar(
                BinaryConverter.byteArrayToUnsignedShort(data, 0)/* year */,
                (int) (data[2] - 1)/* month convert to zero based */,
                (int) (data[3])/* day */, (int) (data[4])/* hour */,
                (int) (data[5])/* minute */, (int) (data[6])/* second */);
        
          signonInfo_ = new SignonInfo();
          signonInfo_.currentSignonDate = date;
          signonInfo_.lastSignonDate = date;
          signonInfo_.expirationDate = (BinaryConverter.byteArrayToInt(data, 8) == 0) ? null
                : new GregorianCalendar(
                        BinaryConverter.byteArrayToUnsignedShort(data, 8)/* year */, 
                        (int) (data[10] - 1)/* month convert to zero based */,
                        (int) (data[11])/* day */, 
                        (int) (data[12])/* hour */,
                        (int) (data[13])/* minute */,
                        (int) (data[14])/* second */);

          signonInfo_.version = AS400.nativeVRM;
          signonInfo_.serverCCSID = getCcsidFromServer();
      }
      catch (NativeException e) {
          throw mapNativeSecurityException(e);
      } 
      finally {
          if (didSwap)
              swapBack();
      }
  }

  // Initialize the impl without calling the sign-on server.
  // skipSignon was meant to allow a JDBC user to connect directly to the database host 
  // server without connecting to the signon server.  This only happens if a port is specified on the URL.
  @Override
  public SignonInfo skipSignon(String systemName, boolean systemNameLocal, String userId, CredentialVault vault, String gssName)
      throws AS400SecurityException, IOException 
  {
      systemName_ = systemName;
      systemNameLocal_ = systemNameLocal;
      userId_ = userId;
      gssName_ = gssName;

      // We are accepting a credential vault from the caller.
      // This vault will replace our existing one.
      // So if our existing one is not the same as the one being given to us,
      // then empty our existing one since we are discarding it.
      if (!vault.equals(credVault_))
          credVault_.empty();
    
      credVault_ = vault; 

      if (credVault_.getType() != AS400.AUTHENTICATION_SCHEME_GSS_TOKEN)
      {
          // Must first decode the credential using the seeds that were previously
          // exchanged between the public AS400 class and this class.
          credVault_.storeEncodedUsingInternalSeeds(proxySeed_, remoteSeed_);
          // Note: The called method ends up storing a "twiddled" representation of
          // the credential info.
      }

      proxySeed_ = null;
      remoteSeed_ = null;

      if (canUseNativeOptimization_)
          nativeSignon();
      else
      {
          try
          {
              if (Trace.traceOn_)
                  Trace.log(Trace.DIAGNOSTIC, "Read security validation reply...");

              if (Trace.traceOn_)
                  Trace.log(Trace.DIAGNOSTIC, "Security validated successfully.");

              signonInfo_ = new SignonInfo();
              signonInfo_.currentSignonDate = null;
              signonInfo_.lastSignonDate = null;
              signonInfo_.expirationDate = null;
              signonInfo_.PWDexpirationWarning = -1;
              signonInfo_.version = new ServerVersion(0x70400);
              signonInfo_.serverCCSID = 37;
              signonInfo_.userId = userId_;

              if (DataStream.getDefaultConverter() == null)
              {
                  if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Signon server reports CCSID:", signonInfo_.serverCCSID);
                  DataStream.setDefaultConverter(ConverterImplRemote.getConverter(signonInfo_.serverCCSID, this));
              }
              
              signonInfo_ = null;
          }
          catch (IOException e)
          {
              Trace.log(Trace.ERROR, "Signon failed:", e);
              signonServer_.forceDisconnect();
              signonServer_ = null;
              throw e;
          }
      }
      
      return signonInfo_;
  }
  
  // The hostcnn connection takes over for signon server when it comes to authentication.
  // Of course, it also controls the establishing of host server job
  // connections under the auspices of the initial use of the additional authentication factor. 
  // And it never goes away unless there is a request to disconnect or the connection has been severed.
  synchronized private void hostcnnConnect(boolean authenticate) throws AS400SecurityException, IOException
  {
      // If HOSTCNN server not supported or cannot connect to it over TLS, simply return.
      // Note that if we do have HOSTCNN connection, we later verify that the connection is still alive, and if not, 
      // we try to reestablish connection. 
      if (hostcnnServer_ == null && (useSSLConnection_ == null || (signonInfo_ != null && getVRM() <= 0x00070500)))
          return;
      
      boolean connectedSuccessfully     = false;
     
      AS400NoThreadServer hostcnnServer = (hostcnnServer_ != null) ? hostcnnServer_ : new AS400NoThreadServer(this, AS400.HOSTCNN);
     
      try 
      {
          // So we need to synchronize since we may be reconnecting and thus we do not want other AS400 objects 
          // that may share same HOSTCNN connection attempt the same process of reconnecting. 
          hostcnnServer.lock();
                
          if (hostcnnServer_ != null && isConnectionAlive(AS400.HOSTCNN))
          {
              // User already authenticated and HOSTCNN connection is fine. Simply return. 
              
              if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "The as-hostcnn connnection exists and is alive.");

              connectedSuccessfully = true;
              return;
          }

          // If here, either there is no HOSTCNN connection, or the connection is not alive and thus an
          // attempt is made to reconnect. 
          
          if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Attempting to connect to as-hostcnn server.");

          // If going to releases that do not support MFA, portmapper will throw an exception, need to handle.
          SocketContainer socketContainer  =  PortMapper.getServerSocket( (systemNameLocal_) ? "localhost"  : systemName_,  
                  AS400.HOSTCNN, -1, useSSLConnection_, socketProperties_, mustUseNetSockets_);
          hostcnnServer.setSocket(socketContainer);
          int connectionID = hostcnnServer.getConnectionID();
          
          InputStream inStream = socketContainer.getInputStream();
          OutputStream outStream = socketContainer.getOutputStream();
          
          hostcnn_clientSeed_ = (credVault_.getType() == AS400.AUTHENTICATION_SCHEME_PASSWORD)
                  ? BinaryConverter.longToByteArray(System.currentTimeMillis()) : null;

          // -------
          // The first request we send is "exchange client/server attributes"...
          // -------
          if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Send exchange client/server attributes request");

          int serverId = AS400Server.getServerId(AS400.HOSTCNN);

          SignonExchangeAttributeReq attrReq = new SignonExchangeAttributeReq(serverId, hostcnn_clientSeed_);

          if (Trace.traceOn_) attrReq.setConnectionID(connectionID);
          attrReq.write(outStream);

          SignonExchangeAttributeRep attrRep = new SignonExchangeAttributeRep();
          if (Trace.traceOn_) attrRep.setConnectionID(connectionID);
          attrRep.read(inStream);

          if (attrRep.getRC() != 0)
          {
              // Connect failed, throw exception.
              byte[] rcBytes = new byte[4];
              BinaryConverter.intToByteArray(attrRep.getRC(), rcBytes, 0);
              Trace.log(Trace.ERROR, "Server exchange client/server attributes failed, return code:", rcBytes);
              throw AS400ImplRemote.returnSecurityException(attrRep.getRC(), null, userId_);
          }
          
          // -------
          // Bookkeeping...
          // -------
          
          version_ = new ServerVersion(attrRep.getServerVersion());
          serverLevel_ = attrRep.getServerLevel();
          passwordLevel_ = attrRep.getPasswordLevel();
          isPasswordTypeSet_ = true;
          hostcnn_serverSeed_ = attrRep.getServerSeed();
          aafIndicator_ = attrRep.getAAFIndicator();
          
          if (Trace.traceOn_)
          {
              byte[] versionBytes = new byte[4];
              BinaryConverter.intToByteArray(version_.getVersionReleaseModification(), versionBytes, 0);
              Trace.log(Trace.DIAGNOSTIC, "Server vrm:", versionBytes);
              Trace.log(Trace.DIAGNOSTIC, "Server level: ", serverLevel_);
              Trace.log(Trace.DIAGNOSTIC, "MFA enbled: ", aafIndicator_);
          }
          
          // Will be overridden if we authenticate!
          hostcnnServer.setJobString(obtainJobIdForConnection(attrRep.getJobNameBytes()));

          
          // Only reason we do not authenticate is when we need to retrieve server information
          // and thus the HOSTCNN connection MUST be disconnected immediately. 
          // TODO AMRA move block of code to own method and remove duplication
          if (authenticate)
          {
              connectionID = hostcnnServer.getConnectionID();

              // -------
              // Next we send the "start server job" request...
              // -------
              
              if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Send start server job request for service as-hostcnn");
    
              byte[] userIDbytes = (credVault_.getType() == AS400.AUTHENTICATION_SCHEME_PASSWORD) 
                      ? SignonConverter.stringToByteArray(userId_) : null;
              byte[] encryptedPassword = (credVault_.getType() == AS400.AUTHENTICATION_SCHEME_GSS_TOKEN)
                      ? credVault_.getClearCredential() : getPassword(hostcnn_clientSeed_, hostcnn_serverSeed_);
                
              if (PASSWORD_TRACE)
              {
                  Trace.log(Trace.DIAGNOSTIC, "Sending Start Server Request for hostcnnConnect ...");
                  Trace.log(Trace.DIAGNOSTIC, "  User ID:", userId_);
                  Trace.log(Trace.DIAGNOSTIC, "  User ID bytes:", userIDbytes);
                  Trace.log(Trace.DIAGNOSTIC, "  Client seed:", hostcnn_clientSeed_);
                  Trace.log(Trace.DIAGNOSTIC, "  Server seed:", hostcnn_serverSeed_);
                  Trace.log(Trace.DIAGNOSTIC, "  Encrypted password:", encryptedPassword);
                  Trace.log(Trace.DIAGNOSTIC, "  Password level: ", passwordLevel_);
              }
    
              // [0]=factor, [1]=verification ID, [2]=remote ip address 
              Object[] additonalAuthInfo = getAdditionalAuthInfo(null, aafIndicator_, additionalAuthFactor_);

              AS400StrSvrDS req = new AS400StrSvrDS(serverId, userIDbytes, encryptedPassword, credVault_.getType(), 
                      (byte[])additonalAuthInfo[0],  (byte[])additonalAuthInfo[1],  (byte[])additonalAuthInfo[2]);
              
              if (Trace.traceOn_) req.setConnectionID(connectionID);
              req.write(outStream);
    
              AS400StrSvrReplyDS reply = new AS400StrSvrReplyDS();
              if (Trace.traceOn_) reply.setConnectionID(connectionID);
              reply.read(inStream);
    
              if (reply.getRC() != 0)
              {
                  byte[] rcBytes = new byte[4];
                  BinaryConverter.intToByteArray(reply.getRC(), rcBytes, 0);
                  Trace.log(Trace.ERROR, "Start server failed with return code:", rcBytes);
                  throw AS400ImplRemote.returnSecurityException(reply.getRC(), null, userId_);
              }
              
              hostcnnServer.setJobString(obtainJobIdForConnection(reply.getJobNameBytes()));
              
              // Set authenticated user if not already set. 
              if (userId_.length() == 0)
              {
                  byte[] b = reply.getUserIdBytes();
                  if (b != null)
                     userId_ = SignonConverter.byteArrayToString(b);
              }
          }
          
          // -------
          // Bookkeeping...
          // -------
          
          connectedSuccessfully = true;
          
          if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Socket opened successfully - as-hostcnn server job is " + hostcnnServer.getJobString());
          
          fireConnectEvent(true, AS400.HOSTCNN);
      } 
      catch (ConnectException | ServerStartupException  e)
      {
          if (e instanceof ServerStartupException 
                  && ((ServerStartupException) e).getReturnCode() != ServerStartupException.CONNECTION_PORT_CANNOT_CONNECT_TO)
              throw e;
          
          Trace.log(Trace.DIAGNOSTIC, "The server as-hostcnn is not up and thus cannot be used for authentication");
      } 
      catch (IOException | AS400SecurityException | RuntimeException e) {
          // Some sort of error, may be SSL certificate problem, may be security problem. 
          Trace.log(Trace.ERROR, "Hostcnn server exchange client/server attributes failed:", e);
          throw e;
      }
      finally
      {
          hostcnnServer.unlock();
          
          if (connectedSuccessfully)
              hostcnnServer_ = hostcnnServer;
          else if (hostcnnServer_ != null)
          {
              // Must have been trying to reconnect and failed
              
              if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Reconnecting as-hostcnn failed, marking as closed ");

              // Mark as closed so if shared it is known that it is not viable.
              hostcnnServer.markClosed();
              
              hostcnnDisconnect();
          }
          else
          {
              if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Connecting to as-hostcnn failed, forcing disconnect ");

              // It is not shared...just force disconnect. We do not mark as closed, we want 
              // to go through the process of closing the socket. 
              hostcnnServer.forceDisconnect();     
          }
      }
  }

  // Connect to sign-on server.
  private synchronized void signonConnect() throws AS400SecurityException, IOException
  {
      if (signonServer_ != null)
          return;

      if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Attempting to connect to as-signon server.");

      boolean connectedSuccessfully = false;
      AS400NoThreadServer signonServer = null;
      
      try
      {
          SocketContainer signonConnection = PortMapper.getServerSocket((systemNameLocal_) ? "localhost" : systemName_,
                  AS400.SIGNON, useSSLConnection_, socketProperties_, mustUseNetSockets_);
          
          signonServer = new AS400NoThreadServer(this, AS400.SIGNON, signonConnection, "");
          
          int connectionID = signonConnection.hashCode();

          InputStream inStream = signonConnection.getInputStream();
          OutputStream outStream = signonConnection.getOutputStream();

          clientSeed_ = (credVault_.getType() == AS400.AUTHENTICATION_SCHEME_PASSWORD)
                  ? BinaryConverter.longToByteArray(System.currentTimeMillis()) : null;

          SignonExchangeAttributeReq attrReq = new SignonExchangeAttributeReq(AS400Server.getServerId(AS400.SIGNON), clientSeed_);
          if (Trace.traceOn_) attrReq.setConnectionID(connectionID);
          attrReq.write(outStream);

          SignonExchangeAttributeRep attrRep = new SignonExchangeAttributeRep();
          if (Trace.traceOn_) attrRep.setConnectionID(connectionID);
          attrRep.read(inStream);

          if (attrRep.getRC() != 0)
          {
              // Connect failed, throw exception.
              byte[] rcBytes = new byte[4];
              BinaryConverter.intToByteArray(attrRep.getRC(), rcBytes, 0);
              Trace.log(Trace.ERROR, "Signon server exchange client/server attributes failed, return code:", rcBytes);
              throw AS400ImplRemote.returnSecurityException(attrRep.getRC(), null, userId_);
          }
          
          // -------
          // Bookkeeping...
          // -------
          
          version_ = new ServerVersion(attrRep.getServerVersion());
          serverLevel_ = attrRep.getServerLevel();
          passwordLevel_ = attrRep.getPasswordLevel();
          isPasswordTypeSet_ = true;
          serverSeed_ = attrRep.getServerSeed();
          aafIndicator_ = attrRep.getAAFIndicator();
          localIPAddress_ = signonConnection.getLocalAddress();
          localIPAddressSet_ = true; 
          if (Trace.traceOn_)
          {
              if (PASSWORD_TRACE)
              {
                  Trace.log(Trace.DIAGNOSTIC, "  Client seed:", clientSeed_);
                  Trace.log(Trace.DIAGNOSTIC, "  Server seed:", serverSeed_);
              }
              byte[] versionBytes = new byte[4];
              BinaryConverter.intToByteArray(version_.getVersionReleaseModification(), versionBytes, 0);
              Trace.log(Trace.DIAGNOSTIC, "  Server vrm:", versionBytes);
              Trace.log(Trace.DIAGNOSTIC, "  Server level: ", serverLevel_);
          }
                
          signonServer.setJobString(obtainJobIdForConnection(attrRep.getJobNameBytes()));
          
          connectedSuccessfully = true;
          
          if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Socket opened successfully - as-signon server job is " + signonServer.getJobString());

          fireConnectEvent(true, AS400.SIGNON);
      }
      catch (IOException | AS400SecurityException e)
      {
          Trace.log(Trace.ERROR, "Signon server exchange client/server attributes failed:", e);
          throw e;
      }
      finally
      {
          if (connectedSuccessfully)
              signonServer_ = signonServer;
          else if (signonServer != null)
              signonServer.forceDisconnect();
      }
  }
  
  // Disconnect from HCS daemon server.
  private synchronized void hostcnnDisconnect()
  {
      if (hostcnnServer_ == null)
          return;

      try {
          hostcnnServer_.forceDisconnect();
      } catch (Exception e) {
          Trace.log(Trace.ERROR, "Error on disconnect of as-hostcnn server:", e);
          throw e;
      } finally {
          hostcnnServer_ = null;
      }

      fireConnectEvent(false, AS400.HOSTCNN);
  }

  // Disconnect from sign-on server.
  private synchronized void signonDisconnect()
  {
      if (signonServer_ == null)
          return;

      try {
          signonServer_.forceDisconnect();
      } catch (Exception e) {
          Trace.log(Trace.ERROR, "Error on disconnect of as-signon server:", e);
          throw e;
      } finally {
          signonServer_ = null;
      }

      fireConnectEvent(false, AS400.SIGNON);
  }
  
  private synchronized void createSwapUserProfileHandle76() throws AS400SecurityException, IOException
  {
      if (!AS400.onAS400 || (AS400.nativeVRM.getVersionReleaseModification() <= 0x00070500))
          return;
      
      // credentials cannot be empty
      if (credVault_.isEmpty() || credVault_.getType() != AS400.AUTHENTICATION_SCHEME_PASSWORD)
      {
          Trace.log(Trace.ERROR, "Password is null.");
          throw new AS400SecurityException(AS400SecurityException.PASSWORD_NOT_SET);
      }
      
      // If we have a handle, see if we can use; otherwise, release it. 
      if (swapToPH76_ != null)
      {
          if (swapToPHUserID76_.equals(userId_))
          {
              if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Existing swap profile handle matched userID:" + userId_);
              return;
          }
          
          releaseSwapUserProfileHandle();
      }
      
      // Create profile handle matching credentials
      byte[] temp = credVault_.getClearCredential();
      try
      {
          // Screen out passwords that start with a star.
          if (temp[0] == 0x00 && temp[1] == 0x2A)
          {
              Trace.log(Trace.ERROR, "Parameter 'password' begins with a '*' character.");
              throw new AS400SecurityException(AS400SecurityException.SIGNON_CHAR_NOT_VALID);
          }
          
          if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Creating swap profile handle for userID:" + userId_);

          // Initialize the values before swapping.
          byte[] swapToPH_temp = new byte[12];
          
          char [] tempAAF = additionalAuthFactor_ == null ? null : new String(additionalAuthFactor_, StandardCharsets.UTF_8).toCharArray();

          AS400ImplNative.createProfileHandle2Native(swapToPH_temp,  userId_, temp, tempAAF,
                  ProfileTokenCredential.DEFAULT_VERIFICATION_ID, AS400.DEFAULT_LOCAL_IP_ADDRESS, 0, "", 0 );
          
          if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Created swap profile handle for userID:" + userId_);

          
          swapToPH76_ = swapToPH_temp;
          swapToPHUserID76_ = userId_;

          swapToPHRefCount_ = new AtomicInteger(1);
      }
      catch (NativeException e) {
          throw mapNativeSecurityException(e);
      } finally {
          CredentialVault.clearArray(temp);
      }
  }

  private synchronized void releaseSwapUserProfileHandle() throws AS400SecurityException, IOException
  {
      // If we have a handle, see if we can use; otherwise, release it. 
      if (swapToPH76_ == null) 
          return;

      try 
      {
          int count = swapToPHRefCount_.decrementAndGet();
          if (count == 0)
          {
              if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "releasing swap profile handle for userID:" + swapToPHUserID76_);

              AS400ImplNative.releaseProfileHandleNative(swapToPH76_);
          }
      }
      catch (NativeException e) {
          throw mapNativeSecurityException(e);
      }
      finally {
          swapToPH76_ = null;
          swapToPHUserID76_ = null;
          swapToPHRefCount_ = null;
      }
  }
  
  /**
   * swapTo is called by the various native-optimized pieces of the Java Toolbox when 
   * canUseNativeOptimization_ is set to true. In the determination whether optimization 
   * can be used is the check to ensure that the credential vault is of type 
   * AUTHENTICATION_SCHEME_PASSWORD. The method also ensures this. 
   */
  boolean swapTo() throws AS400SecurityException, IOException
  {
      // This method MUST not be called if not running natively. 
      if (!AS400.onAS400)
      {
          Trace.log(Trace.ERROR, "swapTo called when not running natively");
          throw new AS400SecurityException(AS400SecurityException.UNKNOWN,"swapTo called when not running natively");
      }
      
      // If thread userID matches AS400 userID, no swapping necessary. 
      if (AS400.currentUserAvailable()
              && userId_.equals(CurrentUser.getUserID(AS400.nativeVRM.getVersionReleaseModification())))
          return false;

      // credentials cannot be empty
      if (credVault_.isEmpty() || !(credVault_.getType() == AS400.AUTHENTICATION_SCHEME_PASSWORD))
      {
          Trace.log(Trace.ERROR, "Password is null.");
          throw new AS400SecurityException(AS400SecurityException.PASSWORD_NOT_SET);
      }
      
      // If already swapped, do not allow another swap
       byte[] priorHandle = threadLocalSwapFromPH_.get(); 
      if (priorHandle !=  null) 
      {
          Trace.log(Trace.ERROR, "Nested swapTo / swapBack calls threadLocalHandle=",priorHandle);
          throw new AS400SecurityException(AS400SecurityException.UNKNOWN, "Nested swapTo / swapBack calls.");
      }
    
      // For 7.5 and older releases, we create and delete profile handles as needed. 
      // In subsequent releases, we do not due to additional authentication factor. 
      // We create profile handle corresponding to credential and only delete when 
      // profile handle does not match the credential. 
      if (AS400.nativeVRM.getVersionReleaseModification() <= 0x00070500)
      {
          byte[] temp = credVault_.getClearCredential();
          try
          {
              // Screen out passwords that start with a star.
              if (temp[0] == 0x00 && temp[1] == 0x2A)
              {
                  Trace.log(Trace.ERROR, "Parameter 'password' begins with a '*' character.");
                  throw new AS400SecurityException(AS400SecurityException.SIGNON_CHAR_NOT_VALID);
              }
    
              // Initialize the values before swapping.
              byte[] swapToPH_temp = new byte[12];
              byte[] swapFromPH_temp = new byte[12];
              
              if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Swap old way to userID:" + userId_);

              AS400ImplNative.swapToNative(SignonConverter.stringToByteArray(userId_), temp, swapToPH_temp, swapFromPH_temp);
              threadLocalSwapToPH_.set(swapToPH_temp);
              // if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Swapped from Handle: ", swapFromPH_temp);
              threadLocalSwapFromPH_.set(swapFromPH_temp);
          }
          catch (NativeException e) {
              throw mapNativeSecurityException(e);
          }
          finally {
              CredentialVault.clearArray(temp);
          }
      }
      else
      {
          createSwapUserProfileHandle76(); 
          
          try
          {
              // Initialize the value before swapping. This will hold profile handle for thread userID so we can swap back.
              byte[] swapFromPH_temp = new byte[12];
    
              if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Swap new way to profile handle for userID:" + userId_);

              AS400ImplNative.swapToProfileHandleNative(swapToPH76_, swapFromPH_temp);
              // if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Swapped from Handle: ", swapFromPH_temp);
              threadLocalSwapFromPH_.set( swapFromPH_temp);
          }
          catch (NativeException e) {
              throw mapNativeSecurityException(e);
          } 
      }

      return true;
  }

  void swapBack() throws AS400SecurityException, IOException
  {
    byte[] oldPH = threadLocalSwapFromPH_.get();
    if (oldPH == null) {
      throw new AS400SecurityException(AS400SecurityException.UNKNOWN, "No profile handle for swapping back");
    }
    // if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "swapBack ProfileHandle ", oldPH);
    
      if (AS400.nativeVRM.getVersionReleaseModification() <= 0x00070500)
      {
          byte[] swapToPH = threadLocalSwapToPH_.get(); 
          if (swapToPH == null) { 
            throw new AS400SecurityException(AS400SecurityException.UNKNOWN, "swapBack() No current profile handle to release");
          }
          if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Swapping back - old way");
          try {
              AS400ImplNative.swapBackNative(swapToPH, oldPH);
          }
          catch (NativeException e) {
              throw mapNativeSecurityException(e);
          }
          finally {
              /* swapBackNative releases both profile handles, so remove the references to them */ 
              threadLocalSwapToPH_.remove();
              threadLocalSwapFromPH_.remove();
          }
      }
      else
      {
          if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Swapping back - new way");
          try {
              AS400ImplNative.swapBackAndReleaseNative(oldPH);
          }
          catch (NativeException e) {
              throw mapNativeSecurityException(e);
          }
          finally {
              threadLocalSwapFromPH_.remove();
          }
      }
  }

  // Return a security exception based on the data received from the native method.
  private AS400SecurityException mapNativeSecurityException(NativeException e) throws IOException
  {
      // Parse information from byte array.
      String id = ConverterImplRemote.getConverter(37, this).byteArrayToString(e.data, 12, 7);

      if (id.equals("CPF2203") || id.equals("CPF2204"))
          return new AS400SecurityException(AS400SecurityException.USERID_UNKNOWN, "USER:"+userId_+" "+id);

      if (id.equals("CPF22E3"))
          return new AS400SecurityException(AS400SecurityException.USERID_DISABLE, "USER:"+userId_+" "+id);

      if (id.equals("CPF22E2") || id.equals("CPF22E5"))
          return new AS400SecurityException(AS400SecurityException.PASSWORD_INCORRECT, "USER:"+userId_+" "+id);

      if (id.equals("CPF22E4"))
          return new AS400SecurityException(AS400SecurityException.PASSWORD_EXPIRED, "USER:"+userId_+" "+id);

      return new AS400SecurityException(AS400SecurityException.SECURITY_GENERAL, "USER:"+userId_+" "+id);
  }

  // This method is to generate the password, the protected old password and the
  // protected new password. This is used for change password.
  //
  // Note: All input strings are EBCDIC and maximum length of 10 bytes. They
  // must be terminated with EBCDIC space character (0x40) if length is less
  // than 10.
  //
  // input:
  // userID: EBCDIC userID
  // oldPassword: EBCDIC old password
  // newPassword: EBCDIC new password
  // sequence number: Host format 8-byte sequence number
  // For example: sequence number "1" is { 0, 0, 0, 0, 0, 0, 0, 1 }
  // clientSeed: Host format 8-byte random client seed number.
  // hostSeed: Host format 8-byte random host seed number.
  private static byte[] encryptNewPassword(byte[] userID, byte[] oldPwd,
      byte[] newPwd, byte[] protectedOldPwd, byte[] protectedNewPwd,
      byte[] clientSeed, byte[] serverSeed)
  {
    byte[] verifyToken = new byte[8];
    byte[] sequence = { 0, 0, 0, 0, 0, 0, 0, 1 };

    // generate a token based on the old password
    byte[] token = generateToken(userID, oldPwd);

    // generate the first password substitute
    byte[] encryptedPassword = generatePasswordSubstitute(userID, token, verifyToken, sequence, clientSeed, serverSeed);

    // generate the proctected new password

    // generate the second password substitute
    incrementString(sequence);
    byte[] tempEncryptedPassword = generatePasswordSubstitute(userID, token, verifyToken, sequence, clientSeed, serverSeed);

    // exclusive or the first copy of the protected new password
    // This is the first 8 bytes of the protected new password
    xORArray(tempEncryptedPassword, newPwd, protectedNewPwd);

    // if the newPassword is more than 8 bytes generate the second 8 bytes of
    // the protected new password
    if (protectedNewPwd.length == 16)
    {
      byte[] secondNewPassword = new byte[8];
      // increment the sequence number for the next copy of the substitute
      incrementString(sequence);
      tempEncryptedPassword = generatePasswordSubstitute(userID, token, verifyToken, sequence, clientSeed, serverSeed);

      // left justify the 9 and 10 bytes of the new password
      for (int i = 0; i < 8; i++) {
        secondNewPassword[i] = (byte) 0x40;
      }
      secondNewPassword[0] = newPwd[8];
      secondNewPassword[1] = newPwd[9];

      // second half of protected new password
      byte[] temp = new byte[8];
      xORArray(tempEncryptedPassword, secondNewPassword, temp);
      System.arraycopy(temp, 0, protectedNewPwd, 8, 8);
    }

    // generate the protected old password

    // generate a token based on the old password
    token = generateToken(userID, newPwd);

    // increment the sequence number for the first copy of the protected old
    // password
    incrementString(sequence);

    // generate the first copy of the protected new password
    tempEncryptedPassword = generatePasswordSubstitute(userID, token, verifyToken, sequence, clientSeed, serverSeed);
    // exclusive or the first copy of the protected old password
    // This is the first 8 bytes of the protected old password
    xORArray(tempEncryptedPassword, oldPwd, protectedOldPwd);

    // if the oldPassword is more than 8 bytes
    // generate the second 8 bytes of the protected old password
    if (protectedOldPwd.length == 16)
    {
      byte[] secondOldPassword = new byte[8];
      // increment the sequence number for the next copy of the substitute
      incrementString(sequence);

      tempEncryptedPassword = generatePasswordSubstitute(userID, token, verifyToken, sequence, clientSeed, serverSeed);

      // left justify the 9 and 10 bytes of the old password
      for (int i = 0; i < 8; i++) {
        secondOldPassword[i] = (byte) 0x40;
      }
      secondOldPassword[0] = oldPwd[8];
      secondOldPassword[1] = oldPwd[9];

      // second half of protected old password
      byte[] temp = new byte[8];
      xORArray(tempEncryptedPassword, secondOldPassword, temp);
      System.arraycopy(temp, 0, protectedOldPwd, 8, 8);
    }
    return encryptedPassword;
  }

  private static byte[] encryptPassword(byte[] userID, byte[] pwd, byte[] clientSeed, byte[] serverSeed)
  {
    byte[] sequenceNumber = { 0, 0, 0, 0, 0, 0, 0, 1 };
    byte[] verifyToken = new byte[8];

    byte[] token = generateToken(userID, pwd);
    if (PASSWORD_TRACE) Trace.log(Trace.DIAGNOSTIC, "In encryptPassword, token: ", token);

    byte[] encryptedPassword = generatePasswordSubstitute(userID, token, verifyToken, sequenceNumber, clientSeed, serverSeed);
    if (PASSWORD_TRACE) Trace.log(Trace.DIAGNOSTIC, "In encryptPassword, encryptedPassword: ",  encryptedPassword);

    return encryptedPassword;
  }

  // void gen_pwd_sbs( byte[] user_id,
  // byte[] password_token,
  // byte[] password_substitute,
  // byte[] pwdseq,
  // byte[] rds,
  // byte[] rdr)
  //
  // Note: rdr, rds and pwdseq are all members of the PWDSBS_SEEDS structure.
  //
  // Function: Generate password substitute.
  // Perform steps 5 to 7 of the password substitute formula.
  // Steps 1 to 4 were already performed by the generate password token routine.
  // It also generate the password verifier.
  //
  // Passwor Substitute formula:
  // (5) Increment PWSEQs and store it.
  //
  // (6) Add PWSEQs to RDr to get RDrSEQ.
  //
  // (7) PW_SUB = MAC:sub.DES:esub.( PW_TOKEN,(RDrSEQ,RDs,ID xor RDrSEQ)):
  //
  // LEGEND:
  // PW User password
  // XOR EXCLUSIVE OR
  // ID User identifier
  // ENC:sub.DES:esub Encipher using the Data Encryption Standard algorithm
  // MAC:sub.DES:esub Generate a Message authentication code using DES
  // RDs Random data sent to the partner LU on BIND
  // RDr Random data received from the partner LU on BIND
  // PWSEQs Sequence number for password substitution on the send side
  // RDrSEQ The arithmetic sum of RDr and the current value of PWSEQs.
  // DES Data Encryption Standard algorithm
  //
  // Note: The MAC(DES) function was implemented according to the description
  // given in the MI functional reference for the CIPHER function. Under the
  // section "Cipher Block Chaining". Basically what it says is that the MAC des
  // use the DES algorithm to encrypt the first data block (8 bytes) the result
  // is then exclusive ORed with the next data block and it become the data
  // input for the DES algorithm. For subsequents blocks of data the same
  // operation is repeated.
  private static byte[] generatePasswordSubstitute(byte[] userID, byte[] token,
      byte[] password_verifier, byte[] sequenceNumber, byte[] clientSeed,
      byte[] serverSeed)
  {
    byte[] RDrSEQ = new byte[8];
    byte[] nextData = new byte[8];
    byte[] nextEncryptedData = new byte[8];

    // first data or RDrSEQ = password sequence + host seed
    addArray(sequenceNumber, serverSeed, RDrSEQ, 8);

    // first encrypted data = DES(token, first data)
    nextEncryptedData = enc_des(token, RDrSEQ);

    // second data = first encrypted data ^ client seed
    xORArray(nextEncryptedData, clientSeed, nextData);

    // second encrypted data (password verifier) = DES(token, second data)
    nextEncryptedData = enc_des(token, nextData);

    // let's copy second encrypted password to password verifier.
    // Don't know what it is yet but will ask Leonel.
    System.arraycopy(nextEncryptedData, 0, password_verifier, 0, 8);

    // third data = RDrSEQ ^ first 8 bytes of userID
    xORArray(userID, RDrSEQ, nextData);

    // third data ^= third data ^ second encrypted data
    xORArray(nextData, nextEncryptedData, nextData);

    // third encrypted data = DES(token, third data)
    nextEncryptedData = enc_des(token, nextData);

    // leftJustify the second 8 bytes of user ID
    for (int i = 0; i < 8; i++) {
      nextData[i] = (byte) 0x40;
    }

    nextData[0] = userID[8];
    nextData[1] = userID[9];

    // fourth data = second half of userID ^ RDrSEQ;
    xORArray(RDrSEQ, nextData, nextData);

    // fourth data = third encrypted data ^ fourth data
    xORArray(nextData, nextEncryptedData, nextData);

    // fourth encrypted data = DES(token, fourth data)
    nextEncryptedData = enc_des(token, nextData);

    // fifth data = fourth encrypted data ^ sequence number
    xORArray(sequenceNumber, nextEncryptedData, nextData);

    // fifth encrypted data = DES(token, fifth data) this is the encrypted
    // password
    return enc_des(token, nextData);
  }

  // userID and password are in EBCDIC
  // userID and password are terminated with 0x40 (EBCDIC blank)
  private static byte[] generateToken(byte[] userID, byte[] password)
  {
    byte[] token = new byte[8];
    byte[] workBuffer1 = new byte[10];
    byte[] workBuffer2 = { 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40,
        0x40, 0x40 };
    byte[] workBuffer3 = { 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40,
        0x40, 0x40 };

    // Copy user ID into the work buffer.
    System.arraycopy(userID, 0, workBuffer1, 0, 10);

    // Find userID length.
    int length = ebcdicStrLen(userID, 10);

    if (length > 8) {
      // Fold user ID.
      workBuffer1[0] ^= (workBuffer1[8] & 0xC0);
      workBuffer1[1] ^= (workBuffer1[8] & 0x30) << 2;
      workBuffer1[2] ^= (workBuffer1[8] & 0x0C) << 4;
      workBuffer1[3] ^= (workBuffer1[8] & 0x03) << 6;
      workBuffer1[4] ^= (workBuffer1[9] & 0xC0);
      workBuffer1[5] ^= (workBuffer1[9] & 0x30) << 2;
      workBuffer1[6] ^= (workBuffer1[9] & 0x0C) << 4;
      workBuffer1[7] ^= (workBuffer1[9] & 0x03) << 6;
    }
    if (PASSWORD_TRACE) {
      Trace.log(Trace.DIAGNOSTIC, "In generateToken, folded user ID:",
          workBuffer1);
    }

    // work with password
    length = ebcdicStrLen(password, 10);

    // if password is more than 8 characters long
    if (length > 8)
    {
      // copy the first 8 bytes of password to workBuffer2
      System.arraycopy(password, 0, workBuffer2, 0, 8);

      // copy the remaining password to workBuffer3
      System.arraycopy(password, 8, workBuffer3, 0, length - 8);

      // generate the token for the first 8 bytes of password
      xorWith0x55andLshift(workBuffer2);

      workBuffer2 = // first token
      enc_des(workBuffer2, // shifted result
          workBuffer1); // userID

      // generate the token for the second 8 bytes of password
      xorWith0x55andLshift(workBuffer3);

      workBuffer3 = // second token
      enc_des(workBuffer3, // shifted result
          workBuffer1); // userID

      // exclusive-or the first and second token to get the real token
      xORArray(workBuffer2, workBuffer3, token);
    }
    else
    {
      // copy password to work buffer
      System.arraycopy(password, 0, workBuffer2, 0, length);
      if (PASSWORD_TRACE) Trace.log(Trace.DIAGNOSTIC, "In generateToken, workBuffer2: ", workBuffer2);

      // generate the token for 8 byte userID
      xorWith0x55andLshift(workBuffer2);
      if (PASSWORD_TRACE)  Trace.log(Trace.DIAGNOSTIC, "In generateToken, workBuffer2: ", workBuffer2);

      token =  enc_des(workBuffer2, workBuffer1); // shifted result, userID
    }
    
    return token;
  }

  // Add two byte arrays.
  private static void addArray(byte[] array1, byte[] array2, byte[] result, int length)
  {
    int carryBit = 0;
    for (int i = length - 1; i >= 0; i--)
    {
      int temp = (array1[i] & 0xff) + (array2[i] & 0xff) + carryBit;
      carryBit = temp >>> 8;
      result[i] = (byte) temp;
    }
  }

  private static int ebcdicStrLen(byte[] string, int maxLength)
  {
    int i = 0;
    while ((i < maxLength) && (string[i] != 0x40) && (string[i] != 0))
      ++i;
    return i;
  }

  // increment host format 8-byte number
  private static void incrementString(byte[] string)
  {
    byte[] one = { 0, 0, 0, 0, 0, 0, 0, 1 };
    addArray(string, one, string, 8);
  }

  private static void xORArray(byte[] string1, byte[] string2, byte[] string3) {
    for (int i = 0; i < 8; i++) {
      string3[i] = (byte) (string1[i] ^ string2[i]);
    }
  }

  private static void xorWith0x55andLshift(byte[] bytes) {
    bytes[0] ^= 0x55;
    bytes[1] ^= 0x55;
    bytes[2] ^= 0x55;
    bytes[3] ^= 0x55;
    bytes[4] ^= 0x55;
    bytes[5] ^= 0x55;
    bytes[6] ^= 0x55;
    bytes[7] ^= 0x55;

    bytes[0] = (byte) (bytes[0] << 1 | (bytes[1] & 0x80) >>> 7);
    bytes[1] = (byte) (bytes[1] << 1 | (bytes[2] & 0x80) >>> 7);
    bytes[2] = (byte) (bytes[2] << 1 | (bytes[3] & 0x80) >>> 7);
    bytes[3] = (byte) (bytes[3] << 1 | (bytes[4] & 0x80) >>> 7);
    bytes[4] = (byte) (bytes[4] << 1 | (bytes[5] & 0x80) >>> 7);
    bytes[5] = (byte) (bytes[5] << 1 | (bytes[6] & 0x80) >>> 7);
    bytes[6] = (byte) (bytes[6] << 1 | (bytes[7] & 0x80) >>> 7);
    bytes[7] <<= 1;
  }

  // the E function used in the cipher function
  private static final int[] EPERM = { 32, 1, 2, 3, 4, 5, 4, 5, 6, 7, 8, 9, 8,
      9, 10, 11, 12, 13, 12, 13, 14, 15, 16, 17, 16, 17, 18, 19, 20, 21, 20,
      21, 22, 23, 24, 25, 24, 25, 26, 27, 28, 29, 28, 29, 30, 31, 32, 1 };

  // the initial scrambling of the input data
  private static final int[] INITPERM = { 58, 50, 42, 34, 26, 18, 10, 2, 60,
      52, 44, 36, 28, 20, 12, 4, 62, 54, 46, 38, 30, 22, 14, 6, 64, 56, 48, 40,
      32, 24, 16, 8, 57, 49, 41, 33, 25, 17, 9, 1, 59, 51, 43, 35, 27, 19, 11,
      3, 61, 53, 45, 37, 29, 21, 13, 5, 63, 55, 47, 39, 31, 23, 15, 7 };

  // the inverse permutation of initperm - used on the proutput block
  private static final int[] OUTPERM = { 40, 8, 48, 16, 56, 24, 64, 32, 39, 7,
      47, 15, 55, 23, 63, 31, 38, 6, 46, 14, 54, 22, 62, 30, 37, 5, 45, 13, 53,
      21, 61, 29, 36, 4, 44, 12, 52, 20, 60, 28, 35, 3, 43, 11, 51, 19, 59, 27,
      34, 2, 42, 10, 50, 18, 58, 26, 33, 1, 41, 9, 49, 17, 57, 25 };

  // the P function used in cipher function
  private static final int[] PPERM = { 16, 7, 20, 21, 29, 12, 28, 17, 1, 15,
      23, 26, 5, 18, 31, 10, 2, 8, 24, 14, 32, 27, 3, 9, 19, 13, 30, 6, 22, 11,
      4, 25 };

  private static final int[] PC1 = // Permuted Choice 1
  { // get the 56 bits which make up C0 and D0 (combined into Cn) from the
    // original key
  57, 49, 41, 33, 25, 17, 9, 1, 58, 50, 42, 34, 26, 18, 10, 2, 59, 51, 43, 35,
      27, 19, 11, 3, 60, 52, 44, 36, 63, 55, 47, 39, 31, 23, 15, 7, 62, 54, 46,
      38, 30, 22, 14, 6, 61, 53, 45, 37, 29, 21, 13, 5, 28, 20, 12, 4 };

  private static final int[] PC2 = // Permuted Choice 2
  { // used in generation of the 16 subkeys
  14, 17, 11, 24, 1, 5, 3, 28, 15, 6, 21, 10, 23, 19, 12, 4, 26, 8, 16, 7, 27,
      20, 13, 2, 41, 52, 31, 37, 47, 55, 30, 40, 51, 45, 33, 48, 44, 49, 39,
      56, 34, 53, 46, 42, 50, 36, 29, 32 };

  private static final int[] S1 = { 14, 4, 13, 1, 2, 15, 11, 8, 3, 10, 6, 12,
      5, 9, 0, 7, 0, 15, 7, 4, 14, 2, 13, 1, 10, 6, 12, 11, 9, 5, 3, 8, 4, 1,
      14, 8, 13, 6, 2, 11, 15, 12, 9, 7, 3, 10, 5, 0, 15, 12, 8, 2, 4, 9, 1, 7,
      5, 11, 3, 14, 10, 0, 6, 13 };

  private static final int[] S2 = { 15, 1, 8, 14, 6, 11, 3, 4, 9, 7, 2, 13, 12,
      0, 5, 10, 3, 13, 4, 7, 15, 2, 8, 14, 12, 0, 1, 10, 6, 9, 11, 5, 0, 14, 7,
      11, 10, 4, 13, 1, 5, 8, 12, 6, 9, 3, 2, 15, 13, 8, 10, 1, 3, 15, 4, 2,
      11, 6, 7, 12, 0, 5, 14, 9 };

  private static final int[] S3 = { 10, 0, 9, 14, 6, 3, 15, 5, 1, 13, 12, 7,
      11, 4, 2, 8, 13, 7, 0, 9, 3, 4, 6, 10, 2, 8, 5, 14, 12, 11, 15, 1, 13, 6,
      4, 9, 8, 15, 3, 0, 11, 1, 2, 12, 5, 10, 14, 7, 1, 10, 13, 0, 6, 9, 8, 7,
      4, 15, 14, 3, 11, 5, 2, 12 };

  private static final int[] S4 = { 7, 13, 14, 3, 0, 6, 9, 10, 1, 2, 8, 5, 11,
      12, 4, 15, 13, 8, 11, 5, 6, 15, 0, 3, 4, 7, 2, 12, 1, 10, 14, 9, 10, 6,
      9, 0, 12, 11, 7, 13, 15, 1, 3, 14, 5, 2, 8, 4, 3, 15, 0, 6, 10, 1, 13, 8,
      9, 4, 5, 11, 12, 7, 2, 14 };

  private static final int[] S5 = { 2, 12, 4, 1, 7, 10, 11, 6, 8, 5, 3, 15, 13,
      0, 14, 9, 14, 11, 2, 12, 4, 7, 13, 1, 5, 0, 15, 10, 3, 9, 8, 6, 4, 2, 1,
      11, 10, 13, 7, 8, 15, 9, 12, 5, 6, 3, 0, 14, 11, 8, 12, 7, 1, 14, 2, 13,
      6, 15, 0, 9, 10, 4, 5, 3 };

  private static final int[] S6 = { 12, 1, 10, 15, 9, 2, 6, 8, 0, 13, 3, 4, 14,
      7, 5, 11, 10, 15, 4, 2, 7, 12, 9, 5, 6, 1, 13, 14, 0, 11, 3, 8, 9, 14,
      15, 5, 2, 8, 12, 3, 7, 0, 4, 10, 1, 13, 11, 6, 4, 3, 2, 12, 9, 5, 15, 10,
      11, 14, 1, 7, 6, 0, 8, 13 };

  private static final int[] S7 = { 4, 11, 2, 14, 15, 0, 8, 13, 3, 12, 9, 7, 5,
      10, 6, 1, 13, 0, 11, 7, 4, 9, 1, 10, 14, 3, 5, 12, 2, 15, 8, 6, 1, 4, 11,
      13, 12, 3, 7, 14, 10, 15, 6, 8, 0, 5, 9, 2, 6, 11, 13, 8, 1, 4, 10, 7, 9,
      5, 0, 15, 14, 2, 3, 12 };

  private static final int[] S8 = { 13, 2, 8, 4, 6, 15, 11, 1, 10, 9, 3, 14, 5,
      0, 12, 7, 1, 15, 13, 8, 10, 3, 7, 4, 12, 5, 6, 11, 0, 14, 9, 2, 7, 11, 4,
      1, 9, 12, 14, 2, 0, 6, 10, 13, 15, 3, 5, 8, 2, 1, 14, 7, 4, 10, 8, 13,
      15, 12, 9, 0, 3, 5, 6, 11 };

  // Name: enc_des - Encrypt function front interface
  //
  // Function: This function is the interface to the DES encryption routine
  // It converts the parameters to a format expected by the actual DES
  // encryption routine.
  //
  // Input: 8 byte data to encrypt
  // 8 byte key to encrypt
  //
  // Output: 8 byte encrypted data passed parameter
  //
  // ***************************************************************************
  //
  // enc_des(byte[] data, byte[] key, byte[] enc_data)
  // {
  // Copy the passed parameters to local variables so we can have 9 bytes
  // variables.
  // Expand the key and data variables so we will have one byte representing one
  // bit of the input data.
  // Perform the actual encryption of the input data using the 64 bytes variable
  // according with the DES algorithm.
  // Compress back the result of the encryption to return the 8 bytes data
  // encryption result.
  // }

  private static byte[] enc_des(byte[] key, byte[] data) {
    if (PASSWORD_TRACE) {
      Trace.log(Trace.DIAGNOSTIC, "In enc_des, key: ", key);
      Trace.log(Trace.DIAGNOSTIC, "In enc_des, data: ", data);
    }
    // expend strings, 1 bit per byte, 1 char in 8 bytes
    byte[] e1 = new byte[65];
    byte[] e2 = new byte[65];

    // input strings, 1 character per byte password user id to be used as key
    // encrypted data

    // expand the input string to 1 bit per byte again for the key
    for (int i = 0; i < 8; ++i) {
      e1[8 * i + 1] = (byte) (((data[i] & 0x80) == 0) ? 0x30 : 0x31);
      e1[8 * i + 2] = (byte) (((data[i] & 0x40) == 0) ? 0x30 : 0x31);
      e1[8 * i + 3] = (byte) (((data[i] & 0x20) == 0) ? 0x30 : 0x31);
      e1[8 * i + 4] = (byte) (((data[i] & 0x10) == 0) ? 0x30 : 0x31);
      e1[8 * i + 5] = (byte) (((data[i] & 0x08) == 0) ? 0x30 : 0x31);
      e1[8 * i + 6] = (byte) (((data[i] & 0x04) == 0) ? 0x30 : 0x31);
      e1[8 * i + 7] = (byte) (((data[i] & 0x02) == 0) ? 0x30 : 0x31);
      e1[8 * i + 8] = (byte) (((data[i] & 0x01) == 0) ? 0x30 : 0x31);
    }

    for (int i = 0; i < 8; ++i) {
      e2[8 * i + 1] = (byte) (((key[i] & 0x80) == 0) ? 0x30 : 0x31);
      e2[8 * i + 2] = (byte) (((key[i] & 0x40) == 0) ? 0x30 : 0x31);
      e2[8 * i + 3] = (byte) (((key[i] & 0x20) == 0) ? 0x30 : 0x31);
      e2[8 * i + 4] = (byte) (((key[i] & 0x10) == 0) ? 0x30 : 0x31);
      e2[8 * i + 5] = (byte) (((key[i] & 0x08) == 0) ? 0x30 : 0x31);
      e2[8 * i + 6] = (byte) (((key[i] & 0x04) == 0) ? 0x30 : 0x31);
      e2[8 * i + 7] = (byte) (((key[i] & 0x02) == 0) ? 0x30 : 0x31);
      e2[8 * i + 8] = (byte) (((key[i] & 0x01) == 0) ? 0x30 : 0x31);
    }
    if (PASSWORD_TRACE) {
      Trace.log(Trace.DIAGNOSTIC, "In enc_des, e1: ", e1);
      Trace.log(Trace.DIAGNOSTIC, "In enc_des, e2: ", e2);
    }

    // encryption method
    byte[] preout = new byte[65]; // preoutput block

    // generate keys 1 - 16

    // temp key gen workspace
    byte[] Cn = new byte[58];
    // create Cn from the original key
    for (int n = 1; n <= 56; n++) {
      Cn[n] = e2[PC1[n - 1]];
    }

    // rotate Cn to form C1 (still called Cn...)
    lshift1(Cn);

    byte[] key1 = new byte[49]; // 48 bit key 1 to key 16
    // now Cn[] contains 56 bits for input to PC2 to generate key1
    for (int n = 1; n <= 48; n++) {
      key1[n] = Cn[PC2[n - 1]];
    }

    byte[] key2 = new byte[49];
    // now derive C2 from C1 (which is called Cn)
    lshift1(Cn);
    for (int n = 1; n <= 48; n++) {
      key2[n] = Cn[PC2[n - 1]];
    }

    byte[] key3 = new byte[49];
    // now derive C3 from C2 by left shifting twice
    lshift2(Cn);
    for (int n = 1; n <= 48; n++) {
      key3[n] = Cn[PC2[n - 1]];
    }

    byte[] key4 = new byte[49];
    // now derive C4 from C3 by again left shifting twice
    lshift2(Cn);
    for (int n = 1; n <= 48; n++) {
      key4[n] = Cn[PC2[n - 1]];
    }

    byte[] key5 = new byte[49];
    // now derive C5 from C4 by again left shifting twice
    lshift2(Cn);
    for (int n = 1; n <= 48; n++) {
      key5[n] = Cn[PC2[n - 1]];
    }

    byte[] key6 = new byte[49];
    // now derive C6 from C5 by again left shifting twice
    lshift2(Cn);
    for (int n = 1; n <= 48; n++) {
      key6[n] = Cn[PC2[n - 1]];
    }

    byte[] key7 = new byte[49];
    // now derive C7 from C6 by again left shifting twice
    lshift2(Cn);
    for (int n = 1; n <= 48; n++) {
      key7[n] = Cn[PC2[n - 1]];
    }

    byte[] key8 = new byte[49];
    // now derive C8 from C7 by again left shifting twice
    lshift2(Cn);
    for (int n = 1; n <= 48; n++) {
      key8[n] = Cn[PC2[n - 1]];
    }

    byte[] key9 = new byte[49];
    // now derive C9 from C8 by shifting left once
    lshift1(Cn);
    for (int n = 1; n <= 48; n++) {
      key9[n] = Cn[PC2[n - 1]];
    }

    byte[] key10 = new byte[49];
    // now derive C10 from C9 by again left shifting twice
    lshift2(Cn);
    for (int n = 1; n <= 48; n++) {
      key10[n] = Cn[PC2[n - 1]];
    }

    byte[] key11 = new byte[49];
    // now derive C11 from C10 by again left shifting twice
    lshift2(Cn);
    for (int n = 1; n <= 48; n++) {
      key11[n] = Cn[PC2[n - 1]];
    }

    byte[] key12 = new byte[49];
    // now derive C12 from C11 by again left shifting twice
    lshift2(Cn);
    for (int n = 1; n <= 48; n++) {
      key12[n] = Cn[PC2[n - 1]];
    }

    byte[] key13 = new byte[49];
    // now derive C13 from C12 by again left shifting twice
    lshift2(Cn);
    for (int n = 1; n <= 48; n++) {
      key13[n] = Cn[PC2[n - 1]];
    }

    byte[] key14 = new byte[49];
    // now derive C14 from C13 by again left shifting twice
    lshift2(Cn);
    for (int n = 1; n <= 48; n++) {
      key14[n] = Cn[PC2[n - 1]];
    }

    byte[] key15 = new byte[49];
    // now derive C15 from C14 by again left shifting twice
    lshift2(Cn);
    for (int n = 1; n <= 48; n++) {
      key15[n] = Cn[PC2[n - 1]];
    }

    byte[] key16 = new byte[49];
    // now derive C16 from C15 by again left shifting once
    lshift1(Cn);
    for (int n = 1; n <= 48; n++) {
      key16[n] = Cn[PC2[n - 1]];
    }

    // temp encryption workspace
    byte[] Ln = new byte[33];
    // ditto
    byte[] Rn = new byte[33];

    // perform the initial permutation and store the result in Ln and Rn
    for (int n = 1; n <= 32; n++) {
      Ln[n] = e1[INITPERM[n - 1]];
      Rn[n] = e1[INITPERM[n + 31]];
    }
    if (PASSWORD_TRACE) {
      Trace.log(Trace.DIAGNOSTIC, "In enc_des, Ln: ", Ln);
      Trace.log(Trace.DIAGNOSTIC, "In enc_des, Rn: ", Rn);
    }

    // run cipher to get new Ln and Rn
    cipher(key1, Ln, Rn);
    cipher(key2, Ln, Rn);
    cipher(key3, Ln, Rn);
    cipher(key4, Ln, Rn);
    cipher(key5, Ln, Rn);
    cipher(key6, Ln, Rn);
    cipher(key7, Ln, Rn);
    cipher(key8, Ln, Rn);
    cipher(key9, Ln, Rn);
    cipher(key10, Ln, Rn);
    cipher(key11, Ln, Rn);
    cipher(key12, Ln, Rn);
    cipher(key13, Ln, Rn);
    cipher(key14, Ln, Rn);
    cipher(key15, Ln, Rn);
    cipher(key16, Ln, Rn);
    if (PASSWORD_TRACE) {
      Trace.log(Trace.DIAGNOSTIC, "In enc_des, Ln: ", Ln);
      Trace.log(Trace.DIAGNOSTIC, "In enc_des, Rn: ", Rn);
    }

    // Ln and Rn are now at L16 and R16 - create preout[] by interposing them
    System.arraycopy(Rn, 1, preout, 1, 32);
    System.arraycopy(Ln, 1, preout, 33, 32);

    byte[] e3 = new byte[65];
    // run preout[] through outperm to get ciphertext
    for (int n = 1; n <= 64; n++) {
      e3[n] = preout[OUTPERM[n - 1]];
    }

    byte[] enc_data = new byte[8];
    // compress back to 8 bits per byte
    for (int i = 0; i < 8; ++i) {
      if (e3[8 * i + 1] == 0x31)
        enc_data[i] |= 0x80;
      if (e3[8 * i + 2] == 0x31)
        enc_data[i] |= 0x40;
      if (e3[8 * i + 3] == 0x31)
        enc_data[i] |= 0x20;
      if (e3[8 * i + 4] == 0x31)
        enc_data[i] |= 0x10;
      if (e3[8 * i + 5] == 0x31)
        enc_data[i] |= 0x08;
      if (e3[8 * i + 6] == 0x31)
        enc_data[i] |= 0x04;
      if (e3[8 * i + 7] == 0x31)
        enc_data[i] |= 0x02;
      if (e3[8 * i + 8] == 0x31)
        enc_data[i] |= 0x01;
    }
    return enc_data;
  }

  private static void cipher(byte[] key, byte[] Ln, byte[] Rn) {
    byte[] temp1 = new byte[49]; // Rn run through E
    byte[] temp2 = new byte[49]; // temp1 XORed with key
    byte[] temp3 = new byte[33]; // temp2 run through S boxes
    byte[] fkn = new byte[33]; // f(k,n)
    int[] si = new int[9]; // decimal input to S boxes
    int[] so = new int[9]; // decimal output from S boxes

    // generate temp1[] from Rn[]
    for (int n = 1; n <= 48; n++) {
      temp1[n] = Rn[EPERM[n - 1]];
    }

    // XOR temp1 with key to get temp2
    for (int n = 1; n <= 48; n++) {
      temp2[n] = (temp1[n] != key[n]) ? (byte) 0x31 : (byte) 0x30;
    }

    // we need to get the explicit representation into a form for
    // processing the s boxes...
    si[1] = ((temp2[1] == 0x31) ? 0x0020 : 0x0000)
        | ((temp2[6] == 0x31) ? 0x0010 : 0x0000)
        | ((temp2[2] == 0x31) ? 0x0008 : 0x0000)
        | ((temp2[3] == 0x31) ? 0x0004 : 0x0000)
        | ((temp2[4] == 0x31) ? 0x0002 : 0x0000)
        | ((temp2[5] == 0x31) ? 0x0001 : 0x0000);

    si[2] = ((temp2[7] == 0x31) ? 0x0020 : 0x0000)
        | ((temp2[12] == 0x31) ? 0x0010 : 0x0000)
        | ((temp2[8] == 0x31) ? 0x0008 : 0x0000)
        | ((temp2[9] == 0x31) ? 0x0004 : 0x0000)
        | ((temp2[10] == 0x31) ? 0x0002 : 0x0000)
        | ((temp2[11] == 0x31) ? 0x0001 : 0x0000);

    si[3] = ((temp2[13] == 0x31) ? 0x0020 : 0x0000)
        | ((temp2[18] == 0x31) ? 0x0010 : 0x0000)
        | ((temp2[14] == 0x31) ? 0x0008 : 0x0000)
        | ((temp2[15] == 0x31) ? 0x0004 : 0x0000)
        | ((temp2[16] == 0x31) ? 0x0002 : 0x0000)
        | ((temp2[17] == 0x31) ? 0x0001 : 0x0000);

    si[4] = ((temp2[19] == 0x31) ? 0x0020 : 0x0000)
        | ((temp2[24] == 0x31) ? 0x0010 : 0x0000)
        | ((temp2[20] == 0x31) ? 0x0008 : 0x0000)
        | ((temp2[21] == 0x31) ? 0x0004 : 0x0000)
        | ((temp2[22] == 0x31) ? 0x0002 : 0x0000)
        | ((temp2[23] == 0x31) ? 0x0001 : 0x0000);

    si[5] = ((temp2[25] == 0x31) ? 0x0020 : 0x0000)
        | ((temp2[30] == 0x31) ? 0x0010 : 0x0000)
        | ((temp2[26] == 0x31) ? 0x0008 : 0x0000)
        | ((temp2[27] == 0x31) ? 0x0004 : 0x0000)
        | ((temp2[28] == 0x31) ? 0x0002 : 0x0000)
        | ((temp2[29] == 0x31) ? 0x0001 : 0x0000);

    si[6] = ((temp2[31] == 0x31) ? 0x0020 : 0x0000)
        | ((temp2[36] == 0x31) ? 0x0010 : 0x0000)
        | ((temp2[32] == 0x31) ? 0x0008 : 0x0000)
        | ((temp2[33] == 0x31) ? 0x0004 : 0x0000)
        | ((temp2[34] == 0x31) ? 0x0002 : 0x0000)
        | ((temp2[35] == 0x31) ? 0x0001 : 0x0000);

    si[7] = ((temp2[37] == 0x31) ? 0x0020 : 0x0000)
        | ((temp2[42] == 0x31) ? 0x0010 : 0x0000)
        | ((temp2[38] == 0x31) ? 0x0008 : 0x0000)
        | ((temp2[39] == 0x31) ? 0x0004 : 0x0000)
        | ((temp2[40] == 0x31) ? 0x0002 : 0x0000)
        | ((temp2[41] == 0x31) ? 0x0001 : 0x0000);

    si[8] = ((temp2[43] == 0x31) ? 0x0020 : 0x0000)
        | ((temp2[48] == 0x31) ? 0x0010 : 0x0000)
        | ((temp2[44] == 0x31) ? 0x0008 : 0x0000)
        | ((temp2[45] == 0x31) ? 0x0004 : 0x0000)
        | ((temp2[46] == 0x31) ? 0x0002 : 0x0000)
        | ((temp2[47] == 0x31) ? 0x0001 : 0x0000);

    // Now for the S boxes
    so[1] = S1[si[1]];
    so[2] = S2[si[2]];
    so[3] = S3[si[3]];
    so[4] = S4[si[4]];
    so[5] = S5[si[5]];
    so[6] = S6[si[6]];
    so[7] = S7[si[7]];
    so[8] = S8[si[8]];

    // That wasn't too bad. Now to convert decimal to char hex again so[1-8]
    // must be translated to 32 bits and stored in temp3[1-32]
    dectobin(so[1], temp3, 1);
    dectobin(so[2], temp3, 5);
    dectobin(so[3], temp3, 9);
    dectobin(so[4], temp3, 13);
    dectobin(so[5], temp3, 17);
    dectobin(so[6], temp3, 21);
    dectobin(so[7], temp3, 25);
    dectobin(so[8], temp3, 29);

    // Okay. Now temp3[] contains the data to run through P
    for (int n = 1; n <= 32; n++) {
      fkn[n] = temp3[PPERM[n - 1]];
    }

    // now complete the cipher function to update Ln and Rn
    byte[] temp = new byte[33]; // storage for Ln during cipher function
    System.arraycopy(Rn, 1, temp, 1, 32);
    for (int n = 1; n <= 32; n++) {
      Rn[n] = (Ln[n] == fkn[n]) ? (byte) 0x30 : (byte) 0x31;
    }
    System.arraycopy(temp, 1, Ln, 1, 32);
  }

  // Start of decimal to binary routine
  // ****************************************************************************
  // convert decimal number to four ones and zeros in store
  // them in the input string
  private static void dectobin(int value, byte[] string, int offset) {
    string[offset] = (byte) (((value & 0x0008) != 0) ? 0x31 : 0x30);
    string[offset + 1] = (byte) (((value & 0x0004) != 0) ? 0x31 : 0x30);
    string[offset + 2] = (byte) (((value & 0x0002) != 0) ? 0x31 : 0x30);
    string[offset + 3] = (byte) (((value & 0x0001) != 0) ? 0x31 : 0x30);
  }

  private static void lshift1(byte[] Cn) {
    byte[] hold = new byte[2];

    // get the two rotated bits
    hold[0] = Cn[1];
    hold[1] = Cn[29];

    // shift each position left in two 28 bit group correspondimg to Cn and Dn
    System.arraycopy(Cn, 2, Cn, 1, 27);
    System.arraycopy(Cn, 30, Cn, 29, 27);

    // restore the first bit of each subgroup
    Cn[28] = hold[0];
    Cn[56] = hold[1];
  }

  private static void lshift2(byte[] Cn) {
    byte[] hold = new byte[4];

    hold[0] = Cn[1]; // get the four rotated bits
    hold[1] = Cn[2];
    hold[2] = Cn[29];
    hold[3] = Cn[30];

    // shift each position left in two 28 bit groups corresponding to Cn and Dn
    System.arraycopy(Cn, 3, Cn, 1, 27);
    System.arraycopy(Cn, 31, Cn, 29, 27);

    // restore the first bit of each subgroup
    Cn[27] = hold[0];
    Cn[28] = hold[1];
    Cn[55] = hold[2];
    Cn[56] = hold[3];
  }

  // @Bidi-HCG3 start
  private int bidiStringType_ = BidiStringType.DEFAULT;

  /**
   * Sets bidi string type of the connection. See <a
   * href="BidiStringType.html">BidiStringType</a> for more information and
   * valid values.
   */
  @Override
  public void setBidiStringType(int bidiStringType) {
    bidiStringType_ = bidiStringType;
  }

  /**
   * Returns bidi string type of the connection. See <a
   * href="BidiStringType.html">BidiStringType</a> for more information and
   * valid values.
   */
  @Override
  public int getBidiStringType() {
    return bidiStringType_;
  }
  // @Bidi-HCG3 end
  
  //Generate salt for password level 4
  /*
   * The following steps describe the algorithm used to generate the pwdlvl 4 version of the password:
   * 1. Convert the 10-character blank padded user ID to upper case.
   * 2. Convert the 10-character blank padded upper case user ID to Unicode (CCSID 13488).
   * 3. Convert the password value to Unicode (CCSID 13488).
   * 4. Generate the salt value:
   *    a. Fill a 28-byte variable with Unicode blanks (0x0020).
   *    b. Copy the Unicode user ID value into the first 20 bytes of the 28-byte blank filled variable.
   *    c. Copy the last 8 bytes (last 4 characters) of the Unicode password value into the last 8 bytes of the 28-byte variable. If the password is less than 4 characters, then copy the entire Unicode password value.
   *    d. Do a SHA-256 hash on the 28-byte variable to produce the 32-byte salt value.
   * 5. Generate the pwdlvl 4 version of the password using PBKDF2 with HMAC SHA-512 with the following values:
   *    Hash algorithm = HMAC SHA-512 (produces a 64-byte key)
   *    Data = Unicode password value
   *    Data Length = Length of Unicode password value
   *    Iterations = 10022
   *    Initialization vector length = 32
   *    Initialization vector (salt) = value generated in Step #4.
   */
  private byte[] generateSaltForPasswordLevel4(String userId, char[] password)
  {
      // leftmost 10 chars of userid blank-padded to exactly 10...
      // ...and rightmost 4 chars of password blank-padded to 4, ...
      // ...produces 14 characters (28 bytes)
      final char[] saltCharArray = new char[14];
      final CharBuffer saltCharBuffer = CharBuffer.wrap(saltCharArray);
      saltCharBuffer.put((userId + "          ").substring(0, 10));

      final int passwdEnd = password.length;
      final int passwdStart = Math.max(passwdEnd - 4, 0);
      for (int i = passwdStart; i < passwdEnd; ++i) {
          saltCharBuffer.put(password[i]);
      }

      saltCharBuffer.put("    ".substring(0, 4 - passwdEnd + passwdStart));
      saltCharBuffer.flip();
      final ByteBuffer saltByteBuffer = Charset.forName("utf-16be").encode(saltCharBuffer);
      MessageDigest saltDigest;
      byte[] salt = null;
      try {
            saltDigest = MessageDigest.getInstance("SHA-256");
            saltDigest.update(saltByteBuffer.array());
            salt = saltDigest.digest(); // returns a 32 byte array
            Arrays.fill(saltCharArray, ' ');
            Arrays.fill(saltByteBuffer.array(), (byte) 0);
      } catch (NoSuchAlgorithmException e) {
            Trace.log(Trace.ERROR, "Error getting instance of SHA-256 algorithm:", e);
            throw new InternalErrorException(InternalErrorException.UNEXPECTED_EXCEPTION, e);
      }
      
      return salt;
  }
  //Generates a 64-byte password token "PW_TOKEN" as described by the security team for the QPWDLVL4 algorithm.
  private byte[] generatePwdTokenForPasswordLevel4(final String userProfile, final char[] passwd)
  {
      final byte[] salt = generateSaltForPasswordLevel4(userProfile, passwd);
      final PBEKeySpec spec = new PBEKeySpec(passwd, salt, 10022, 64 * 8); // takes a bit length so *8
      
      SecretKeyFactory factory;
      try {
          factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
          SecretKey secret = factory.generateSecret(spec);
          byte[] pwdToken = secret.getEncoded();
          return pwdToken; 
      } catch (NoSuchAlgorithmException e) {
          Trace.log(Trace.ERROR, "Error getting instance of PBKDF2WithHmacSHA512 algorithm:", e);
          throw new InternalErrorException(InternalErrorException.UNEXPECTED_EXCEPTION, e);
      } catch (InvalidKeySpecException e) {
          Trace.log(Trace.ERROR, "Invalid Key Spec Exception:", e);
          throw new InternalErrorException(InternalErrorException.UNEXPECTED_EXCEPTION, e);
      } finally {
       
          spec.clearPassword();
      }
  }
  
  //Generates a 64-byte password substitute "PW_SUB" as follows: PW_SUB = SHA-2 512(PW_TOKEN, RDr, RDs, ID, PWSEQs)
  static byte[] generateSha512Substitute(final String userProfile, final byte[] passwdToken, final byte[] serverSeed, final byte[] clientSeed, final byte[] sequence)
  {
      MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance("SHA-512");
            messageDigest.update(passwdToken);
            messageDigest.update(serverSeed);
            messageDigest.update(clientSeed);
            messageDigest.update((userProfile + "          ").substring(0, 10).getBytes("utf-16be"));
            messageDigest.update(sequence);
            
            byte[] answer = messageDigest.digest();
            messageDigest.reset(); 
            
            return answer; 
        } catch (NoSuchAlgorithmException e) {
            Trace.log(Trace.ERROR, "Error getting instance of SHA-512 algorithm:", e);
            throw new InternalErrorException(InternalErrorException.UNEXPECTED_EXCEPTION, e);
        } catch (UnsupportedEncodingException e) {
            Trace.log(Trace.ERROR, "Unsupported Encoding Exception:", e);
            throw new InternalErrorException(InternalErrorException.UNEXPECTED_EXCEPTION, e);
        }
  }
  
  private static byte[] generateSha512Protected(byte[] bytes, byte[] token,
          byte[] serverSeed, byte[] clientSeed, String userId, byte[] sequence)
  {
        // Protected length will be rounded up to next 64.
        int protectedLength = (((bytes.length - 1) / 64) + 1) * 64;
        byte[] protectedPassword = new byte[protectedLength];
        for (int i = 0; i < protectedLength; i += 64)
        {
          incrementString(sequence);
          byte[] encryptedSection = generateSha512Substitute(userId, token, serverSeed, clientSeed, sequence);
          for (int ii = 0; ii < 64; ++ii)
          {
            if (i + ii < bytes.length)
              protectedPassword[i + ii] = (byte) (encryptedSection[ii] ^ bytes[i + ii]);
            else
              protectedPassword[i + ii] = encryptedSection[ii];
          }
        }
        return protectedPassword;
      }

  @Override
  public void setVRM(int v, int r, int m)
  {
      if (signonInfo_ == null)
          signonInfo_ = new SignonInfo((v << 16) + (r << 8) + m); 
      else
          signonInfo_.version.setVersionReleaseModification((v << 16) + (r << 8) + m);
  }

  @Override
  public void setAdditionalAuthenticationFactor(char[] additionalAuthFactor)
  {
      additionalAuthFactor_ = (null != additionalAuthFactor && 0 < additionalAuthFactor.length )
              ? (new String(additionalAuthFactor)).getBytes(StandardCharsets.UTF_8) : null;
  }
  
  /*
   * Get the additional authentication info to be sent to the server as well as 
   * the information that needs to be associated with an enhanced profile token. 
   * The output is the following
   *  object[0] = byte[] additional authentication factor UTF-8
   *  object[1] = byte[] verification id in UTF-8
   *  object[2] = byte[] remoteIPAddress in UTF-8
   *  object[3] = String verificationId
   *  object[4] = String remoteIpAddress 
   */
  private Object[] getAdditionalAuthInfo(ProfileTokenCredential profileToken, Boolean aafIndicator,
      Object additionalAuthFactor) {
    Object[] authdata = new Object[] { null, null, null, null, null };

    int vrm = (version_ != null) ? version_.getVersionReleaseModification() : getVRM();
    if (vrm > 0x00070500 || (aafIndicator != null && aafIndicator)) {
      // If additional authentication factor passed in and not already byte[], convert
      // it and stash in array.
      if (additionalAuthFactor != null) {
        authdata[0] = (additionalAuthFactor instanceof char[])
            ? (new String((char[]) additionalAuthFactor)).getBytes(StandardCharsets.UTF_8)
            : (byte[]) additionalAuthFactor;
      }

      // If profile token is null, means we are not generating a profile token, so
      // profile token should be in credential.
      boolean creatingToken = (profileToken != null);
      if (profileToken == null && (credVault_ instanceof ProfileTokenVault))
        profileToken = ((ProfileTokenVault) credVault_).getProfileTokenCredential();

      if ((profileToken != null))
      {
          // Only if we are creating the profile token do we need to do anything with the 
          // verification ID and remote IP address.  In all other cases, we will take what
          // is there, and if not set, will set *NOUSE. When not creating profile token, 
          // we need to also be sensitive to whether the profile token is enhanced or not. If 
          // not, then the fields will be set to *NOUSE
          ProfileTokenEnhancedInfo ei = profileToken.getEnhancedInfo();
          
          String verificationID_s  = (creatingToken || ei.isEnhancedProfileToken())  
                                          ?  profileToken.getVerificationID() : "*NOUSE";
          String remoteIPAddress_s =  (creatingToken || ei.isEnhancedProfileToken()) 
                                          ? profileToken.getRemoteIPAddress() : "*NOUSE";

          // === Process verification ID
          if (verificationID_s == null) {
            if (creatingToken) {
              verificationID_s = ProfileTokenCredential.DEFAULT_VERIFICATION_ID;
              try {
                profileToken.setVerificationID(verificationID_s);
              } catch (Exception e) {
                Trace.log(Trace.DIAGNOSTIC, e);
                verificationID_s = "";
              }
            } else
              verificationID_s = "*NOUSE";
          }

          authdata[1] = verificationID_s.getBytes(StandardCharsets.UTF_8);
          authdata[3] = verificationID_s;

          // === Process remote IP address

          /*
           * Note: If the remoteIP address is not set to a length > 0 , then the
           * AS400GenAuthTkn request will fail. We will set it to the local IP address that we obtained 
           * from a socket. If it was not set, then we use an empty string 
           */

          if (remoteIPAddress_s == null || remoteIPAddress_s.length() == 0 || (!AS400.onAS400 && remoteIPAddress_s.equals(AS400.DEFAULT_LOCAL_IP_ADDRESS))) {
            if (localIPAddressSet_) {
              /* We can should only change the address in the token  */ 
              /* when we are creating the Token */ 
              if (creatingToken  )  {
                String newRemoteIpAddress = localIPAddress_; 
                if (newRemoteIpAddress == null)
                  newRemoteIpAddress = getLocalIPAddress();
                // Set value in profile token
                try {
                  profileToken.setRemoteIPAddress(newRemoteIpAddress);
                  remoteIPAddress_s = newRemoteIpAddress; 
                } catch (Exception e) {
                  Trace.log(Trace.DIAGNOSTIC, e);
                  if (remoteIPAddress_s == null) 
                      remoteIPAddress_s = "";
                }
              }
            } 
          }
          if (remoteIPAddress_s == null)
            remoteIPAddress_s = "*NOUSE";
          authdata[2] = remoteIPAddress_s.getBytes(StandardCharsets.UTF_8);
          authdata[4] = remoteIPAddress_s;
          
        } else {
          authdata[1] = "*NOUSE".getBytes(StandardCharsets.UTF_8);
          authdata[3] = "*NOUSE";
          authdata[2] = "*NOUSE".getBytes(StandardCharsets.UTF_8);
          authdata[4] = "*NOUSE";
        }
      }
    

      if (Trace.traceOn_)
      {
       
          Trace.log(Trace.DIAGNOSTIC, this, "getAdditionalAuthInfo() Verification ID: " + (authdata[1] != null ? new String((byte[])authdata[1]) : null));
          Trace.log(Trace.DIAGNOSTIC, this, "getAdditionalAuthInfo() Remote IP address: " + (authdata[2] != null ? new String((byte[])authdata[2]) : null));
      }
      
      return authdata;
  }

  /* Get the local ip address from a connected socket */ 
  public String getLocalIPAddress()
  {
      if (!localIPAddressSet_)
      {
          localIPAddress_ = AS400.getDefaultLocalIPAddress();
          
          // Will not consider it set if IP address is loopback and not running natively.
          if (AS400.onAS400 || !localIPAddress_.equals(AS400.DEFAULT_LOCAL_IP_ADDRESS))
        localIPAddressSet_ = true; 
    }
    return localIPAddress_; 
  }

    @Override
    public void setKerbTicket(byte[] ticket) {
        this.kerbTicket_ = ticket;
    }

    private byte[] getKerbTicket() {
        return this.kerbTicket_;
    }
  
}