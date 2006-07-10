///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  AS400ImplRemote.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1999-2006 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.beans.PropertyVetoException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.GregorianCalendar;
import java.util.Vector;

import com.ibm.as400.security.auth.ProfileTokenCredential;

// AS400ImplRemote is the functional implementation of the AS400Impl interface.
class AS400ImplRemote implements AS400Impl
{
    private static final boolean PASSWORD_TRACE = false;

    // The pool of systems.  Tne systems are in service constant order: FILE, PRINT, COMMAND, DATAQUEUE, DATABASE, RECORDACCESS, CENTRAL.
    private Vector[] serverPool_ = { new Vector(), new Vector(), new Vector(), new Vector(), new Vector(), new Vector(), new Vector() };

    // System name.
    private String systemName_ = "";
    // User ID.
    private String userId_ = "";
    // Flag indicating if system name refers to local system.
    private boolean systemNameLocal_ = false;
    // Password bytes twiddled.
    private byte[] bytes_ = null;
    // Type of authentication bytes, start by default in password mode.
    private int byteType_ = AS400.AUTHENTICATION_SCHEME_PASSWORD;
    // GSS Credential object, for Kerberos.  Type set to Object to prevent dependancy on 1.4 JDK.
    private Object gssCredential_ = null;
    // GSS name string, for Kerberos.
    private String gssName_ = "";
    // How to use the GSS framework.
    int gssOption_;
    // Adder for twiddled password bytes.
    private byte[] adder_ = null;
    // Mask for twiddled password bytes.
    private byte[] mask_ = null;

    // Flag that indicates if we connect to SSL ports.
    private SSLOptions useSSLConnection_ = null;
    // Flag that indicates if we should try to use native optimization.
    private boolean canUseNativeOptimization_ = true;
    // Flag that indicates if we use threads in communication with the host servers.
    private boolean threadUsed_ = true;

    // CCSID to use in conversations with the system.
    private int ccsid_ = 0;
    // If the user has told us to override common sense and use the CCSID they want.
    private boolean userOverrideCcsid_ = false;
    // The NLV.
    private String nlv_;
    // Set of socket options to use when creating our connections to the system.
    private SocketProperties socketProperties_ = null;

    // IASP name used for DDM, if specified.
    private String ddmRDB_;

    // VRM information from the sign-on server.  Retrieved from sign-on connect and stored until placed in sign-on information.
    private ServerVersion version_;
    // System level of the sign-on server.  Retrieved from sign-on connect and stored until placed in sign-on information.
    private int serverLevel_;
    // Type of password encryption.
    private boolean passwordType_ = false;  // false == DES, true == SHA-1.
    // Flag indicating if we have determined the password type yet.
    private boolean isPasswordTypeSet_ = false;
    // Sign-on information retrieved on sign-on information request.
    private SignonInfo signonInfo_;
    // EBCDIC bytes of sign-on server job name, held until Job CCSID is returned.
    private byte[] signonJobBytes_;
    // String form of sign-on server job name.
    private String signonJobString_;

    // Dispatcher of connection events from this implementation to public object.
    private ConnectionListener dispatcher_;

    // Single use seed recieved from public object in exchange seed method, held until next sign-on, change password, or generate profile token.
    private byte[] proxySeed_ = null;
    // Single use seed sent to public object in exchange seed method, held until next sign-on, change password, or generate profile token.
    private byte[] remoteSeed_ = null;

    // Connection to the sign-on server.
    SocketContainer signonConnection_;
    // Sign-on server server seed, held from sign-on connection until sign-on disconnect.
    byte[] serverSeed_;
    // Sign-on server client seed, held from sign-on connection until sign-on disconnect.
    byte[] clientSeed_;

    // Set the connection event dispatcher.
    public void addConnectionListener(ConnectionListener listener)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Adding implementation connection listener.");
        dispatcher_ = listener;
    }

    // Map from CCSID to encoding string.
    public String ccsidToEncoding(int ccsid)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Mapping to encoding implementation, CCSID:", ccsid);
        return ConversionMaps.ccsidToEncoding(ccsid);
    }

    // Convenience method for removing trailing space from SHA-1 passwords.
    private static char[] trimUnicodeSpace(char[] inputArray)
    {
        char lastChar = inputArray[inputArray.length - 1];
        if (lastChar != '\u0000' && lastChar != '\u0020' && lastChar != '\u3000') return inputArray;

        int trimPosition = inputArray.length - 1;
        while (inputArray[trimPosition] == '\u0000' || inputArray[trimPosition] == '\u0020' || inputArray[trimPosition] == '\u3000') --trimPosition;
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

            if (PASSWORD_TRACE)
            {
                Trace.log(Trace.DIAGNOSTIC, "SHA-1 token:", token);
            }
            return token;
        }
        catch (NoSuchAlgorithmException e)
        {
            Trace.log(Trace.ERROR, "Error getting instance of SHA-1 algorithm:", e);
            throw new InternalErrorException(InternalErrorException.UNEXPECTED_EXCEPTION);
        }
    }

    // Convenience method for generating SHA-1 substitute password.
    private static byte[] generateShaSubstitute(byte[] token, byte[] serverSeed, byte[] clientSeed, byte[] userIdBytes, byte[] sequence)
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

            if (PASSWORD_TRACE)
            {
                Trace.log(Trace.DIAGNOSTIC, "SHA-1 substitute:", substitutePassword);
            }
            return substitutePassword;
        }
        catch (NoSuchAlgorithmException e)
        {
            Trace.log(Trace.ERROR, "Error getting instance of SHA-1 algorithm:", e);
            throw new InternalErrorException(InternalErrorException.UNEXPECTED_EXCEPTION);
        }
    }

    // Convenience method for generating SHA-1 substitute password.
    private static byte[] generateShaProtected(byte[] bytes, byte[] token, byte[] serverSeed, byte[] clientSeed, byte[] userIdBytes, byte[] sequence)
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
                {
                    protectedPassword[i + ii] = (byte)(encryptedSection[ii] ^ bytes[i + ii]);
                }
                else
                {
                    protectedPassword[i + ii] = encryptedSection[ii];
                }
            }
        }
        return protectedPassword;
    }

    // Change password.
    public SignonInfo changePassword(String systemName, boolean systemNameLocal, String userId, byte[] oldBytes, byte[] newBytes) throws AS400SecurityException, IOException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Change password implementation, system name: '" + systemName + "' user ID: '" + userId + "'");
        if (PASSWORD_TRACE)
        {
            Trace.log(Trace.DIAGNOSTIC, "Old password bytes:", oldBytes);
            Trace.log(Trace.DIAGNOSTIC, "New password bytes:", newBytes);
        }

        systemName_ = systemName;
        systemNameLocal_ = systemNameLocal;
        userId_ = userId;

        // Decode passwords and discard seeds.
        char[] oldPassword = BinaryConverter.byteArrayToCharArray(decode(proxySeed_, remoteSeed_, oldBytes));
        char[] newPassword = BinaryConverter.byteArrayToCharArray(decode(proxySeed_, remoteSeed_, newBytes));
        proxySeed_ = null;
        remoteSeed_ = null;
        if (PASSWORD_TRACE)
        {
            Trace.log(Trace.DIAGNOSTIC, "Old password unscrambled: '" + new String(oldPassword) + "'");
            Trace.log(Trace.DIAGNOSTIC, "New password unscrambled: '" + new String(newPassword) + "'");
        }

        // Get a socket connection.
        boolean needToDisconnect = signonConnection_ == null;
        signonConnect();
        try
        {
            InputStream inStream = signonConnection_.getInputStream();
            OutputStream outStream = signonConnection_.getOutputStream();

            // Convert user ID to EBCDIC.
            byte[] userIdEbcdic = SignonConverter.stringToByteArray(userId);
            byte[] encryptedPassword;
            byte[] oldProtected;
            byte[] newProtected;

            if (passwordType_ == false)
            {
                // Do DES encryption.
                if (oldPassword.length > 10)
                {
                    Trace.log(Trace.ERROR, "Length of parameter 'oldPassword' is not valid:", oldPassword.length);
                    throw new AS400SecurityException(AS400SecurityException.PASSWORD_LENGTH_NOT_VALID);
                }
                byte[] oldPasswordEbcdic = SignonConverter.stringToByteArray(new String(oldPassword).toUpperCase());
                if (newPassword.length > 10)
                {
                    Trace.log(Trace.ERROR, "Length of parameter 'newPassword' is not valid:", newPassword.length);
                    throw new AS400SecurityException(AS400SecurityException.PASSWORD_NEW_NOT_VALID);
                }
                byte[] newPasswordEbcdic = SignonConverter.stringToByteArray(new String(newPassword).toUpperCase());

                // Setup output variables for encrypt new password.
                oldProtected = (oldPasswordEbcdic[8] == 0x40 && oldPasswordEbcdic[9] == 0x40) ? new byte[8] : new byte[16];
                newProtected = (newPasswordEbcdic[8] == 0x40 && newPasswordEbcdic[9] == 0x40) ? new byte[8] : new byte[16];

                encryptedPassword = encryptNewPassword(userIdEbcdic, oldPasswordEbcdic, newPasswordEbcdic, oldProtected, newProtected, clientSeed_, serverSeed_);
            }
            else
            {
                // Do SHA-1 encryption.
                byte[] userIdBytes = BinaryConverter.charArrayToByteArray(SignonConverter.byteArrayToCharArray(userIdEbcdic));

                // Screen out passwords that start with a star.
                if (oldPassword[0] == '*')
                {
                    Trace.log(Trace.ERROR, "Parameter 'oldPassword' begins with a '*' character.");
                    throw new AS400SecurityException(AS400SecurityException.SIGNON_CHAR_NOT_VALID);
                }
                if (newPassword[0] == '*')
                {
                    Trace.log(Trace.ERROR, "Parameter 'newPassword' begins with a '*' character.");
                    throw new AS400SecurityException(AS400SecurityException.SIGNON_CHAR_NOT_VALID);
                }

                // Trim space and put in byte array.
                byte[] oldPasswordBytes = BinaryConverter.charArrayToByteArray(trimUnicodeSpace(oldPassword));
                byte[] newPasswordBytes = BinaryConverter.charArrayToByteArray(trimUnicodeSpace(newPassword));
                byte[] sequence = {0, 0, 0, 0, 0, 0, 0, 1};

                if (PASSWORD_TRACE)
                {
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
            }

            if (PASSWORD_TRACE)
            {
                Trace.log(Trace.DIAGNOSTIC, "Sending Change Password Request...");
                Trace.log(Trace.DIAGNOSTIC, "  User ID: " + userId);
                Trace.log(Trace.DIAGNOSTIC, "  User ID bytes:", userIdEbcdic);
                Trace.log(Trace.DIAGNOSTIC, "  Encrypted password:", encryptedPassword);
                Trace.log(Trace.DIAGNOSTIC, "  Protected old password:", oldProtected);
                Trace.log(Trace.DIAGNOSTIC, "  Protected new password:", newProtected);
            }

            ChangePasswordReq chgReq = new ChangePasswordReq(userIdEbcdic, encryptedPassword, oldProtected, oldPassword.length * 2, newProtected, newPassword.length * 2);
            chgReq.write(outStream);

            ChangePasswordRep chgRep = new ChangePasswordRep();
            chgRep.read(inStream);

            int rc = chgRep.getRC();
            if (rc == 0)
            {
                // Change password worked, so retrive signon information using new password.
                if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Password change implementation successful.");

                byte[] tempSeed = new byte[9];
                AS400.rng.nextBytes(tempSeed);

                SignonInfo returnInfo = signon(systemName, systemNameLocal, userId, encode(tempSeed, exchangeSeed(tempSeed), BinaryConverter.charArrayToByteArray(newPassword)), 0, gssName_, 1);
                if (needToDisconnect) signonDisconnect();
                return returnInfo;
            }
            else
            {
                byte[] rcBytes = new byte[4];
                BinaryConverter.intToByteArray(rc, rcBytes, 0);
                Trace.log(Trace.ERROR, "Change password implementation failed with return code:", rcBytes);

                throw AS400ImplRemote.returnSecurityException(rc);
            }
        }
        catch (IOException e)
        {
            Trace.log(Trace.ERROR, "Change password failed:", e);
            try
            {
                signonConnection_.close();
            }
            catch (IOException ee)
            {
                Trace.log(Trace.ERROR, "Error closing signon socket:", ee);
            }
            signonConnection_ = null;
            throw e;
        }
        catch (AS400SecurityException e)
        {
            Trace.log(Trace.ERROR, "Change password failed:", e);
            try
            {
                signonConnection_.close();
            }
            catch (IOException ee)
            {
                Trace.log(Trace.ERROR, "Error closing signon socket:", ee);
            }
            signonConnection_ = null;
            throw e;
        }
    }

    // Implementation for connect.
    public void connect(int service) throws AS400SecurityException, IOException
    {
        if (service == AS400.SIGNON)
        {
            signonConnect();
        }
        else
        {
            getConnection(service, false);
        }
    }

    // Unscramble some bytes.
    private static byte[] decode(byte[] adder, byte[] mask, byte[] bytes)
    {
        byte[] buf = new byte[bytes.length];
        for (int i = 0; i < bytes.length; ++i)
        {
            buf[i] = (byte)(mask[i % 7] ^ bytes[i]);
        }
        for (int i = 0; i < bytes.length; i++)
        {
            buf[i] = (byte)(buf[i] - adder[i % 9]);
        }
        return buf;
    }

    // Implementation for disconnect.
    public void disconnect(int service)
    {
        if (service == AS400.SIGNON)
        {
            signonDisconnect();
        }
        else
        {
            Vector serverList = serverPool_[service];
            synchronized (serverList)
            {
                while (!serverList.isEmpty())
                {
                    disconnectServer((AS400Server)serverList.elementAt(0));
                }
            }
        }
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Service disconnected implementation: " + AS400.getServerName(service));
    }

    // Disconnect all services.
    void disconnectAllServices()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Disconnecting all services implementation...");

        disconnect(AS400.FILE);
        disconnect(AS400.PRINT);
        disconnect(AS400.DATAQUEUE);
        disconnect(AS400.COMMAND);
        disconnect(AS400.DATABASE);
        disconnect(AS400.RECORDACCESS);
        disconnect(AS400.CENTRAL);
        disconnect(AS400.SIGNON);

        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "All services disconnected implementation.");
    }

    // Disconnects the system and removes it from the system list.
    // param  server  The AS400Server to disconnect and remove.
    void disconnectServer(AS400Server server)
    {
        server.forceDisconnect();
        int service = server.getService();
        Vector serverList = serverPool_[service];
        synchronized (serverList)
        {
            if (!serverList.isEmpty())
            {
                serverList.removeElement(server);

                // Only fire the event if all systems have been disconnected.
                if (serverList.isEmpty())
                {
                    fireConnectEvent(false, service);
                }
            }
        }
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Server disconnected");
    }

    // Scramble some bytes.
    private static byte[] encode(byte[] adder, byte[] mask, byte[] bytes)
    {
        byte[] buf = new byte[bytes.length];
        for (int i = 0; i < bytes.length; ++i)
        {
            buf[i] = (byte)(bytes[i] + adder[i % 9]);
        }
        for (int i = 0; i < bytes.length; ++i)
        {
            buf[i] = (byte)(buf[i] ^ mask[i % 7]);
        }
        return buf;
    }

    // Exchange of seeds between public object and this class.
    public byte[] exchangeSeed(byte[] proxySeed)
    {
        // Hold the seed they send us.
        proxySeed_ = proxySeed;

        // Generate, hold, and send them our seed.
        remoteSeed_ = new byte[7];
        AS400.rng.nextBytes(remoteSeed_);
        return remoteSeed_;
    }

    // Cleans up all connections.
    protected void finalize() throws Throwable
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Finalize method for AS400 implementation invoked.");
        disconnectAllServices();
        super.finalize();
    }

    // Fire connnect event.
    private void fireConnectEvent(boolean connect, int service)
    {
        if (dispatcher_ != null)
        {
            ConnectionEvent connectEvent = new ConnectionEvent(this, service);
            if (connect)
            {
                dispatcher_.connected(connectEvent);
            }
            else
            {
                dispatcher_.disconnected(connectEvent);
            }
        }
    }

    // Flow the generate profile token datastream.
    public void generateProfileToken(ProfileTokenCredential profileToken, String userIdentity) throws AS400SecurityException, IOException
    {
        signonConnect();
        try
        {
            InputStream in = signonConnection_.getInputStream();
            OutputStream out = signonConnection_.getOutputStream();

            byte[] userIDbytes = SignonConverter.stringToByteArray(userId_);
            byte[] encryptedPassword = getPassword(clientSeed_, serverSeed_);
            if (PASSWORD_TRACE)
            {
                Trace.log(Trace.DIAGNOSTIC, "Sending Start Server Request...");
                Trace.log(Trace.DIAGNOSTIC, "  User ID: " + userId_);
                Trace.log(Trace.DIAGNOSTIC, "  User ID bytes:", userIDbytes);
                Trace.log(Trace.DIAGNOSTIC, "  Client seed:", clientSeed_);
                Trace.log(Trace.DIAGNOSTIC, "  Server seed:", serverSeed_);
                Trace.log(Trace.DIAGNOSTIC, "  Encrypted password:", encryptedPassword);
            }

            int serverId = AS400Server.getServerId(AS400.SIGNON);
            AS400StrSvrDS req = new AS400StrSvrDS(serverId, userIDbytes, encryptedPassword, byteType_);
            req.write(out);

            AS400StrSvrReplyDS reply = new AS400StrSvrReplyDS();
            reply.read(in);

            if (reply.getRC() != 0)
            {
                byte[] rcBytes = new byte[4];
                BinaryConverter.intToByteArray(reply.getRC(), rcBytes, 0);
                Trace.log(Trace.ERROR, "Start server failed with return code:", rcBytes);
                throw AS400ImplRemote.returnSecurityException(reply.getRC());
            }

            SignonGenAuthTokenRequestDS req2 = new SignonGenAuthTokenRequestDS(BinaryConverter.charArrayToByteArray(userIdentity.toCharArray()), profileToken.getTokenType(), profileToken.getTimeoutInterval());
            req2.write(out);

            SignonGenAuthTokenReplyDS rep = new SignonGenAuthTokenReplyDS();
            rep.read(in);

            int rc = rep.getRC();
            if (rc != 0)
            {
                byte[] rcBytes = new byte[4];
                BinaryConverter.intToByteArray(rc, rcBytes, 0);
                Trace.log(Trace.ERROR, "Generate profile token failed with return code:", rcBytes);
                throw AS400ImplRemote.returnSecurityException(rc);
            }
            try
            {
                profileToken.setToken(rep.getProfileTokenBytes());
            }
            catch (PropertyVetoException e)
            {
                Trace.log(Trace.ERROR, "Unexpected PropertyVetoException:", e);
                throw new InternalErrorException(InternalErrorException.UNEXPECTED_EXCEPTION);
            }
        }
        catch (IOException e)
        {
            Trace.log(Trace.ERROR, "Generate profile token failed:", e);
            try
            {
                signonConnection_.close();
            }
            catch (IOException ee)
            {
                Trace.log(Trace.ERROR, "Error closing signon socket:", ee);
            }
            signonConnection_ = null;
            throw e;
        }
        catch (AS400SecurityException e)
        {
            Trace.log(Trace.ERROR, "Generate profile token failed:", e);
            try
            {
                signonConnection_.close();
            }
            catch (IOException ee)
            {
                Trace.log(Trace.ERROR, "Error closing signon socket:", ee);
            }
            signonConnection_ = null;
            throw e;
        }
    }

    // Flow the generate profile token datastream.
    public void generateProfileToken(ProfileTokenCredential profileToken, String userId, byte[] bytes, int byteType) throws AS400SecurityException, IOException, InterruptedException
    {
        signonConnect();
        try
        {
            InputStream in = signonConnection_.getInputStream();
            OutputStream out = signonConnection_.getOutputStream();

            byte[] userIdEbcdic = SignonConverter.stringToByteArray(userId);
            byte[] authenticationBytes = null;
            switch (byteType)
            {
                case AS400.AUTHENTICATION_SCHEME_GSS_TOKEN:
                    try
                    {
                        authenticationBytes = (gssCredential_ == null) ? TokenManager.getGSSToken(systemName_, gssName_) : TokenManager2.getGSSToken(systemName_, gssCredential_);
                    }
                    catch (Exception e)
                    {
                        Trace.log(Trace.ERROR, "Error retrieving GSSToken:", e);
                        throw new AS400SecurityException(AS400SecurityException.KERBEROS_TICKET_NOT_VALID_RETRIEVE);
                    }
                    break;
                case AS400.AUTHENTICATION_SCHEME_PROFILE_TOKEN:
                case AS400.AUTHENTICATION_SCHEME_IDENTITY_TOKEN:
                    authenticationBytes = decode(proxySeed_, remoteSeed_, bytes);
                    break;
                default:  // Password.
                    char[] password = BinaryConverter.byteArrayToCharArray(decode(proxySeed_, remoteSeed_, bytes));
                    proxySeed_ = null;
                    remoteSeed_ = null;

                    // Generate the correct password based on the password encryption level of the system.
                    if (passwordType_ == false)
                    {
                        // Prepend Q to numeric password.
                        if (password.length > 0 && Character.isDigit(password[0]))
                        {
                            boolean isAllNumeric = true;
                            for (int i = 0; i < password.length; ++i)
                            {
                                if (password[i] < '\u0030' || password[i] > '\u0039')
                                {
                                    isAllNumeric = false;
                                }
                            }
                            if (isAllNumeric)
                            {
                                if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Prepending Q to numeric password.");
                                char[] passwordWithQ = new char[password.length + 1];
                                passwordWithQ[0] = 'Q';
                                System.arraycopy(password, 0, passwordWithQ, 1, password.length);
                                password = passwordWithQ;
                            }
                        }

                        if (password.length > 10)
                        {
                            Trace.log(Trace.ERROR, "Length of parameter 'password' is not valid:", password.length);
                            throw new AS400SecurityException(AS400SecurityException.PASSWORD_LENGTH_NOT_VALID);
                        }
                        authenticationBytes = encryptPassword(userIdEbcdic, SignonConverter.stringToByteArray(new String(password).toUpperCase()), clientSeed_, serverSeed_);
                    }
                    else
                    {
                        // Do SHA-1 encryption.
                        byte[] userIdBytes = BinaryConverter.charArrayToByteArray(SignonConverter.byteArrayToCharArray(userIdEbcdic));

                        // Screen out passwords that start with a star.
                        if (password[0] == '*')
                        {
                            Trace.log(Trace.ERROR, "Parameter 'password' begins with a '*' character.");
                            throw new AS400SecurityException(AS400SecurityException.SIGNON_CHAR_NOT_VALID);
                        }

                        byte[] passwordBytes = BinaryConverter.charArrayToByteArray(trimUnicodeSpace(password));
                        byte[] sequence = {0, 0, 0, 0, 0, 0, 0, 1};

                        if (PASSWORD_TRACE)
                        {
                            Trace.log(Trace.DIAGNOSTIC, "Pre SHA-1 userIdBytes:", userIdBytes);
                            Trace.log(Trace.DIAGNOSTIC, "Pre SHA-1 passwordBytes:", passwordBytes);
                            Trace.log(Trace.DIAGNOSTIC, "Pre SHA-1 sequence:", sequence);
                        }

                        byte[] token = generateShaToken(userIdBytes, passwordBytes);
                        authenticationBytes =  generateShaSubstitute(token, serverSeed_, clientSeed_, userIdBytes, sequence);
                    }
            }

            AS400GenAuthTknDS req = new AS400GenAuthTknDS(userIdEbcdic, authenticationBytes, byteType, profileToken.getTokenType(), profileToken.getTimeoutInterval());
            req.write(out);

            AS400GenAuthTknReplyDS rep = new AS400GenAuthTknReplyDS();
            rep.read(in);

            int rc = rep.getRC();
            if (rc != 0)
            {
                byte[] rcBytes = new byte[4];
                BinaryConverter.intToByteArray(rc, rcBytes, 0);
                Trace.log(Trace.ERROR, "Generate profile token failed with return code:", rcBytes);
                throw AS400ImplRemote.returnSecurityException(rc);
            }
            try
            {
                profileToken.setToken(rep.getProfileTokenBytes());
            }
            catch (PropertyVetoException e)
            {
                Trace.log(Trace.ERROR, "Unexpected PropertyVetoException:", e);
                throw new InternalErrorException(InternalErrorException.UNEXPECTED_EXCEPTION);
            }
        }
        catch (IOException e)
        {
            Trace.log(Trace.ERROR, "Generate profile token failed:", e);
            try
            {
                signonConnection_.close();
            }
            catch (IOException ee)
            {
                Trace.log(Trace.ERROR, "Error closing signon socket:", ee);
            }
            signonConnection_ = null;
            throw e;
        }
        catch (AS400SecurityException e)
        {
            Trace.log(Trace.ERROR, "Generate profile token failed:", e);
            try
            {
                signonConnection_.close();
            }
            catch (IOException ee)
            {
                Trace.log(Trace.ERROR, "Error closing signon socket:", ee);
            }
            signonConnection_ = null;
            throw e;
        }
    }

    // Get either the user's CCSID, the signon server CCSID, or our best guess.
    int getCcsid()
    {
        if (ccsid_ != 0) return ccsid_;
        if (signonInfo_ != null) ccsid_ = signonInfo_.serverCCSID;
        if (ccsid_ != 0) return ccsid_;
        try { ccsid_ = getCcsidFromServer(); } catch (Exception e) {}
        if (ccsid_ != 0) return ccsid_;
        ccsid_ = ExecutionEnvironment.getBestGuessAS400Ccsid();
        return ccsid_;
    }

    // Get the user's override CCSID or zero if not set.
    int getUserOverrideCcsid()
    {
        if (userOverrideCcsid_) return ccsid_;
        return 0;
    }

    // Get CCSID from central server or current job if native.
    public int getCcsidFromServer() throws AS400SecurityException, IOException, InterruptedException
    {
        NLSImpl impl = (NLSImpl)loadImpl("com.ibm.as400.access.NLSImplNative", "com.ibm.as400.access.NLSImplRemote");

        // Get the ccsid from the central server or current job.
        impl.setSystem(this);
        impl.connect();
        impl.disconnect();
        return impl.getCcsid();
    }

    // Get connection for FTP.
    synchronized Socket getConnection(int port) throws IOException
    {
        Socket socket = new Socket((systemNameLocal_) ? "localhost" : systemName_, port);
        try
        {
            PortMapper.setSocketProperties(socket, socketProperties_);
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "ISO8859_1"));
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "ISO8859_1"), true);

            readFTPLine(reader);

            writer.println("USER " + userId_);
            readFTPLine(reader);

            writer.println("PASS " + new String(BinaryConverter.byteArrayToCharArray(decode(adder_, mask_, bytes_))));
            if (!readFTPLine(reader).startsWith("230")) throw new IOException();

            return socket;
        }
        catch (IOException e)
        {
            Trace.log(Trace.ERROR, "Establishing FTP connection failed:", e);
            try
            {
                socket.close();
            }
            catch (IOException ee)
            {
                Trace.log(Trace.ERROR, "Error closing socket:", ee);
            }
            throw e;
        }
    }

    private String readFTPLine(BufferedReader reader) throws IOException
    {
        String line = reader.readLine();
        if (line == null || line.length() == 0) throw new IOException();
        String code = line.substring(0, 3);
        String fullMessage = line;
        while (!(line.length() > 3 && line.substring(0, 3).equals(code) && line.charAt(3) == ' '))
        {
            line = reader.readLine();
            fullMessage += "\n" + line;
        }
        return fullMessage;
    }

    synchronized Socket getConnection(int dhcp, int port) throws AS400SecurityException, IOException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Establishing connection to system at port:", port);
        Socket socket = new Socket((systemNameLocal_) ? "localhost" : systemName_, port);
        try
        {
            PortMapper.setSocketProperties(socket, socketProperties_);
            InputStream inStream = socket.getInputStream();
            OutputStream outStream = socket.getOutputStream();

            // The first request we send is "exchange random seeds"...
            int serverId = AS400Server.getServerId(AS400.COMMAND);
            AS400XChgRandSeedDS xChgReq = new AS400XChgRandSeedDS(serverId);
            xChgReq.write(outStream);

            AS400XChgRandSeedReplyDS xChgReply = new AS400XChgRandSeedReplyDS();
            xChgReply.read(inStream);

            if (xChgReply.getRC() != 0)
            {
                byte[] rcBytes = new byte[4];
                BinaryConverter.intToByteArray(xChgReply.getRC(), rcBytes, 0);
                Trace.log(Trace.ERROR, "Exchange of random seeds failed with return code:", rcBytes);
                throw AS400ImplRemote.returnSecurityException(xChgReply.getRC());
            }
            if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Exchange of random seeds successful.");

            // Next we send the "start server job" request...
            byte[] clientSeed = xChgReq.getClientSeed();
            byte[] serverSeed = xChgReply.getServerSeed();
            byte[] userIDbytes = SignonConverter.stringToByteArray(userId_);
            byte[] encryptedPassword = getPassword(clientSeed, serverSeed);
            if (PASSWORD_TRACE)
            {
                Trace.log(Trace.DIAGNOSTIC, "Sending Start Server Request...");
                Trace.log(Trace.DIAGNOSTIC, "  User ID: " + userId_);
                Trace.log(Trace.DIAGNOSTIC, "  User ID bytes:", userIDbytes);
                Trace.log(Trace.DIAGNOSTIC, "  Client seed:", clientSeed);
                Trace.log(Trace.DIAGNOSTIC, "  Server seed:", serverSeed);
                Trace.log(Trace.DIAGNOSTIC, "  Encrypted password:", encryptedPassword);
            }

            AS400StrSvrDS req = new AS400StrSvrDS(serverId, userIDbytes, encryptedPassword, byteType_);
            req.write(outStream);

            AS400StrSvrReplyDS reply = new AS400StrSvrReplyDS();
            reply.read(inStream);

            if (reply.getRC() != 0)
            {
                byte[] rcBytes = new byte[4];
                BinaryConverter.intToByteArray(reply.getRC(), rcBytes, 0);
                Trace.log(Trace.ERROR, "Start server failed with return code:", rcBytes);
                throw AS400ImplRemote.returnSecurityException(reply.getRC());
            }

            if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Server started successfully.");
            return socket;
        }
        catch (IOException e)
        {
            Trace.log(Trace.ERROR, "Establishing DHCP connection failed:", e);
            try
            {
                socket.close();
            }
            catch (IOException ee)
            {
                Trace.log(Trace.ERROR, "Error closing socket:", ee);
            }
            throw e;
        }
        catch (AS400SecurityException e)
        {
            Trace.log(Trace.ERROR, "Establishing DHCP connection failed:", e);
            try
            {
                socket.close();
            }
            catch (IOException ee)
            {
                Trace.log(Trace.ERROR, "Error closing socket:", ee);
            }
            throw e;
        }
    }

    // Gets the jobs with which we are connected.
    public String[] getJobs(int service)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting job names implementation, service:", service);
        if (service == AS400.SIGNON)
        {
            return (signonConnection_ != null) ? new String[] { signonJobString_ } : new String[0];
        }
        else
        {
            Vector serverList = serverPool_[service];
            String[] jobStrings = new String[serverList.size()];
            synchronized (serverList)
            {
                for (int i = 0; i < serverList.size(); ++i)
                {
                    jobStrings[i] = (((AS400Server)serverList.elementAt(i)).getJobString());
                }
            }
            return jobStrings;
        }
    }

    // Get AS400Server object connected to indicated service.  You can get either an existing connection or ask for a new connection.
    synchronized AS400Server getConnection(int service, boolean forceNewConnection) throws AS400SecurityException, IOException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Establishing connection to system: " + AS400.getServerName(service));

        // Necessary for case where we are connecting after native sign-on.
        if (!isPasswordTypeSet_)
        {
            signonConnect();
            signonDisconnect();
        }

        AS400Server server = null;
        // Get the list of systems associated with this service.
        Vector serverList = serverPool_[service];
        synchronized (serverList)
        {
            if (!forceNewConnection && !serverList.isEmpty())
            {
                // System exists, get the first available system to reuse.
                server = (AS400Server)serverList.firstElement();
                if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Reusing previous server object...");

                // Return the connected system.
                return server;
            }
        }

        SocketContainer sc = PortMapper.getServerSocket((systemNameLocal_) ? "localhost" : systemName_, service, useSSLConnection_, socketProperties_, mustUseNetSockets_);
        String jobString = "";
        try
        {
            InputStream inStream = sc.getInputStream();
            OutputStream outStream = sc.getOutputStream();

            if (service == AS400.RECORDACCESS)
            {
                Object[] seeds = ClassDecoupler.connectDDMPhase1(outStream, inStream, passwordType_, byteType_);
                byte[] clientSeed = (byte[])seeds[0];
                byte[] serverSeed = (byte[])seeds[1];

                byte[] userIDbytes = SignonConverter.stringToByteArray(userId_);

                // Get the substitute password.
                byte[] ddmSubstitutePassword = getPassword(clientSeed, serverSeed);

                if (PASSWORD_TRACE)
                {
                    Trace.log(Trace.DATASTREAM, "Sending DDM SECCHK request...");
                    Trace.log(Trace.DIAGNOSTIC, "  User ID: " + userId_);
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
                ClassDecoupler.connectDDMPhase2(outStream, inStream, userIDbytes, ddmSubstitutePassword, iaspBytes, byteType_, ddmRDB_, systemName_);
            }
            else
            {
                // The first request we send is "exchange random seeds"...
                int serverId = AS400Server.getServerId(service);
                AS400XChgRandSeedDS xChgReq = new AS400XChgRandSeedDS(serverId);
                xChgReq.write(outStream);

                AS400XChgRandSeedReplyDS xChgReply = new AS400XChgRandSeedReplyDS();
                xChgReply.read(inStream);

                if (xChgReply.getRC() != 0)
                {
                    byte[] rcBytes = new byte[4];
                    BinaryConverter.intToByteArray(xChgReply.getRC(), rcBytes, 0);
                    Trace.log(Trace.ERROR, "Exchange of random seeds failed with return code:", rcBytes);
                    throw AS400ImplRemote.returnSecurityException(xChgReply.getRC());
                }
                if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Exchange of random seeds successful.");

                // Next we send the "start server job" request...
                byte[] clientSeed = xChgReq.getClientSeed();
                byte[] serverSeed = xChgReply.getServerSeed();
                byte[] userIDbytes = SignonConverter.stringToByteArray(userId_);
                byte[] encryptedPassword = getPassword(clientSeed, serverSeed);
                if (PASSWORD_TRACE)
                {
                    Trace.log(Trace.DIAGNOSTIC, "Sending Start Server Request...");
                    Trace.log(Trace.DIAGNOSTIC, "  User ID: " + userId_);
                    Trace.log(Trace.DIAGNOSTIC, "  User ID bytes:", userIDbytes);
                    Trace.log(Trace.DIAGNOSTIC, "  Client seed:", clientSeed);
                    Trace.log(Trace.DIAGNOSTIC, "  Server seed:", serverSeed);
                    Trace.log(Trace.DIAGNOSTIC, "  Encrypted password:", encryptedPassword);
                }

                AS400StrSvrDS req = new AS400StrSvrDS(serverId, userIDbytes, encryptedPassword, byteType_);
                req.write(outStream);

                AS400StrSvrReplyDS reply = new AS400StrSvrReplyDS();
                reply.read(inStream);

                byte[] jobBytes = reply.getJobNameBytes();
                ConverterImplRemote converter = ConverterImplRemote.getConverter(signonInfo_.serverCCSID, this);
                jobString = converter.byteArrayToString(jobBytes);
                if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "System job: " + jobString);

                if (reply.getRC() != 0)
                {
                    byte[] rcBytes = new byte[4];
                    BinaryConverter.intToByteArray(reply.getRC(), rcBytes, 0);
                    Trace.log(Trace.ERROR, "Start server failed with return code:", rcBytes);
                    throw AS400ImplRemote.returnSecurityException(reply.getRC());
                }
            }
        }
        catch (IOException e)
        {
            Trace.log(Trace.ERROR, "Establishing connection failed:", e);
            try
            {
                sc.close();
            }
            catch (IOException ee)
            {
                Trace.log(Trace.ERROR, "Error closing socket:", ee);
            }
            throw e;
        }
        catch (AS400SecurityException e)
        {
            Trace.log(Trace.ERROR, "Establishing connection failed:", e);
            try
            {
                sc.close();
            }
            catch (IOException ee)
            {
                Trace.log(Trace.ERROR, "Error closing socket:", ee);
            }
            throw e;
        }

        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Server started successfully.");

        // At this point the Socket connection is established.  Now we need to set up the AS400Server object before passing it back to the caller.

        // Construct a new server...
        if (threadUsed_)
        {
            server = new AS400ThreadedServer(this, service, sc, jobString);
        }
        else
        {
            server = new AS400NoThreadServer(this, service, sc, jobString);
        }

        // Add the system to our list so we can return it on a subsequent connect()...
        serverList.addElement(server);

        fireConnectEvent(true, service);

        return server;
    }

    // The NLV to send to the system.
    String getNLV()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting NLV implementation: " + nlv_);
        return nlv_;
    }

    // Get the encrypted password with the seeds folded in.
    private byte[] getPassword(byte[] clientSeed, byte[] serverSeed) throws AS400SecurityException, IOException
    {
        if (byteType_ == AS400.AUTHENTICATION_SCHEME_GSS_TOKEN)
        {
            try
            {
                return (gssCredential_ == null) ? TokenManager.getGSSToken(systemName_, gssName_) : TokenManager2.getGSSToken(systemName_, gssCredential_);
            }
            catch (Throwable e)
            {
                Trace.log(Trace.ERROR, "Error retrieving GSSToken:", e);
                throw new AS400SecurityException(AS400SecurityException.KERBEROS_TICKET_NOT_VALID_RETRIEVE);
            }
        }
        if (byteType_ == AS400.AUTHENTICATION_SCHEME_PROFILE_TOKEN || byteType_ == AS400.AUTHENTICATION_SCHEME_IDENTITY_TOKEN)
        {
            return decode(adder_, mask_, bytes_);
        }

        byte[] encryptedPassword = null;

        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Retrieving encrypted password.");

        if (bytes_ == null)
        {
            if (AS400.onAS400 && AS400.currentUserAvailable() && userId_.equals(CurrentUser.getUserID(AS400.nativeVRM.getVersionReleaseModification())))
            {
                encryptedPassword = CurrentUser.getUserInfo(AS400.nativeVRM.getVersionReleaseModification(), clientSeed, serverSeed);
            }
            else
            {
                Trace.log(Trace.ERROR, "Password is null.");
                throw new AS400SecurityException(AS400SecurityException.PASSWORD_NOT_SET);
            }
        }
        else
        {
            byte[] userIdEbcdic = SignonConverter.stringToByteArray(userId_);
            char[] password = BinaryConverter.byteArrayToCharArray(decode(adder_, mask_, bytes_));
            if (PASSWORD_TRACE)
            {
                Trace.log(Trace.DIAGNOSTIC, "  user ID: '" + userId_ + "'");
                Trace.log(Trace.DIAGNOSTIC, "  user ID EBCDIC:", userIdEbcdic);
                Trace.log(Trace.DIAGNOSTIC, "  password untwiddled: '" + new String(password) + "'");
                Trace.log(Trace.DIAGNOSTIC, "  client seed: ", clientSeed);
                Trace.log(Trace.DIAGNOSTIC, "  server seed: ", serverSeed);
            }

            if (passwordType_ == false)
            {
                // Do DES encryption.

                // Prepend Q to numeric password.
                if (password.length > 0 && Character.isDigit(password[0]))
                {
                    boolean isAllNumeric = true;
                    for (int i = 0; i < password.length; ++i)
                    {
                        if (password[i] < '\u0030' || password[i] > '\u0039')
                        {
                            isAllNumeric = false;
                        }
                    }
                    if (isAllNumeric)
                    {
                        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Prepending Q to numeric password.");
                        char[] passwordWithQ = new char[password.length + 1];
                        passwordWithQ[0] = 'Q';
                        System.arraycopy(password, 0, passwordWithQ, 1, password.length);
                        password = passwordWithQ;
                    }
                }

                if (password.length > 10)
                {
                    Trace.log(Trace.ERROR, "Length of parameter 'password' is not valid:", password.length);
                    throw new AS400SecurityException(AS400SecurityException.PASSWORD_LENGTH_NOT_VALID);
                }
                byte[] passwordEbcdic = SignonConverter.stringToByteArray(new String(password).toUpperCase());
                if (PASSWORD_TRACE)
                {
                    Trace.log(Trace.DIAGNOSTIC, "  password in ebcdic: ", passwordEbcdic);
                }
                encryptedPassword = encryptPassword(userIdEbcdic, passwordEbcdic, clientSeed, serverSeed);
            }
            else
            {
                // Do SHA-1 encryption.
                byte[] userIdBytes = BinaryConverter.charArrayToByteArray(SignonConverter.byteArrayToCharArray(userIdEbcdic));

                // Screen out passwords that start with a star.
                if (password[0] == '*')
                {
                    Trace.log(Trace.ERROR, "Parameter 'password' begins with a '*' character.");
                    throw new AS400SecurityException(AS400SecurityException.SIGNON_CHAR_NOT_VALID);
                }

                byte[] passwordBytes = BinaryConverter.charArrayToByteArray(trimUnicodeSpace(password));
                byte[] sequence = {0, 0, 0, 0, 0, 0, 0, 1};

                if (PASSWORD_TRACE)
                {
                    Trace.log(Trace.DIAGNOSTIC, "Pre SHA-1 userIdBytes:", userIdBytes);
                    Trace.log(Trace.DIAGNOSTIC, "Pre SHA-1 passwordBytes:", passwordBytes);
                    Trace.log(Trace.DIAGNOSTIC, "Pre SHA-1 sequence:", sequence);
                }

                byte[] token = generateShaToken(userIdBytes, passwordBytes);
                encryptedPassword =  generateShaSubstitute(token, serverSeed, clientSeed, userIdBytes, sequence);
            }
        }

        if (PASSWORD_TRACE)
        {
            Trace.log(Trace.DIAGNOSTIC, "Encrypted password: ", encryptedPassword);
        }

        return encryptedPassword;
    }

    // Get port number for service.
    public int getServicePort(String systemName, int service)
    {
        return PortMapper.getServicePort(systemName, service, useSSLConnection_);
    }

    // Get system name.
    String getSystemName()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting implementation system name: " + systemName_ + " is local:", systemNameLocal_);
        return (systemNameLocal_) ? "localhost" : systemName_;
    }

    // Get user ID.
    String getUserId()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting implementation user ID: " + userId_);
        return userId_;
    }

    // Get VRM.
    int getVRM()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Getting implementation VRM.");
        int vrm = signonInfo_.version.getVersionReleaseModification();
        if (Trace.traceOn_)
        {
            byte[] vrmBytes = new byte[4];
            BinaryConverter.intToByteArray(vrm, vrmBytes, 0);
            Trace.log(Trace.DIAGNOSTIC, "Implementation VRM:",  vrmBytes);
        }

        return vrm;
    }

    // Check if service is connected.
    public boolean isConnected(int service)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Checking for service connection implementation:", service);
        if (service == AS400.SIGNON)
        {
            return signonConnection_ != null;
        }
        else
        {
            Vector serverList = serverPool_[service];
            synchronized (serverList)
            {
                for (int i = serverList.size() - 1; i >= 0; i--)
                {
                    if (((AS400Server)serverList.elementAt(i)).isConnected()) return true;
                }
                return false;
            }
        }
    }

    // Check if thread can be used.
    boolean isThreadUsed()
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Checking implementation if thread is used:", threadUsed_);
        return threadUsed_;
    }

    // Load the appropriate implementation object.
    // param  impl1  fully package named class name for native implementation.
    // param  impl2  fully package named class name for remote implementation.
    Object loadImpl(String impl1, String impl2)
    {
        if (canUseNativeOptimization_)
        {
            Object impl = AS400.loadImpl(impl1);
            if (impl != null) return impl;
            if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Load of native implementation '" + impl1 + "' failed, attempting to load remote implementation.");
        }
        Object impl = AS400.loadImpl(impl2);
        if (impl != null) return impl;

        Trace.log(Trace.DIAGNOSTIC, "Load of remote implementation '" + impl2 + "' failed.");
        throw new ExtendedIllegalStateException(impl2, ExtendedIllegalStateException.IMPLEMENTATION_NOT_FOUND);
    }

    // Load a converter object into converter cache.
    public void newConverter(int ccsid) throws UnsupportedEncodingException
    {
        ConverterImplRemote.getConverter(ccsid, this);
    }

    // Remove the connection event dispatcher.
    public void removeConnectionListener(ConnectionListener listener)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Removing implementation connection listener.");
        dispatcher_ = null;
    }

    // Create AS400SecurityException from sign-on server return code.
    // Throw or return proper exception if exchange of random seeds or start server request fail.
    static AS400SecurityException returnSecurityException(int rc) throws ServerStartupException
    {
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
                // Error on request data: random seed required when doing password substitution.
                throw new ServerStartupException(ServerStartupException.RANDOM_SEED_REQUIRED);
            case 0x00010006:
                // Error on request data: invalid password encrypt indicator.
                throw new ServerStartupException(ServerStartupException.PASSWORD_ENCRYPT_INVALID);
            case 0x00010007:
                // Error on request data: invalid user ID (length).
                return new AS400SecurityException(AS400SecurityException.USERID_LENGTH_NOT_VALID);
            case 0x00010008:
                // Error on request data: invalid password or passphrase (length).
                return new AS400SecurityException(AS400SecurityException.PASSWORD_LENGTH_NOT_VALID);
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
                // - missing user ID, password or passphrase, and authentication token were specified.
                // - both password and passphrase were specified on the request.
                throw new ServerStartupException(ServerStartupException.REQUEST_DATA_ERROR);
            case 0x0001000B:
                // Error on request data: invalid retrieve sign-on data request:
                // - missing user ID.
                // - missing password or passphrase.
                // - missing authentication token.
                // - user ID / password (or user ID / passphrase) and authentication token both specified.
                // - problems with length fields in the request.
                return new AS400SecurityException(AS400SecurityException.SIGNON_REQUEST_NOT_VALID);
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
                return new AS400SecurityException(AS400SecurityException.PASSWORD_CHANGE_REQUEST_NOT_VALID);
            case 0x0001000D:
                // Error on request data: invalid protected old password or passphrase.
                return new AS400SecurityException(AS400SecurityException.PASSWORD_OLD_NOT_VALID);
            case 0x0001000E:
                // Error on request data: invalid protected new or clear text password or passphrase.
                return new AS400SecurityException(AS400SecurityException.PASSWORD_NEW_NOT_VALID);
            case 0x0001000F:
                // Error on request data: invalid token type on generate authentication token request.
                return new AS400SecurityException(AS400SecurityException.TOKEN_TYPE_NOT_VALID);
            case 0x00010010:
                // Error on request data: invalid generate authentication token request:
                // - missing authentication token.
                // - missing user ID.
                // - missing password or passphrase.
                // - user ID / password (or user ID / passphrase) and authentication token both specified.
                // - problems with length fields in the request.
                return new AS400SecurityException(AS400SecurityException.GENERATE_TOKEN_REQUEST_NOT_VALID);
            case 0x00010011:
                // Error on request data: invalid authentication token (length).
                return new AS400SecurityException(AS400SecurityException.TOKEN_LENGTH_NOT_VALID);
            case 0x00010012:
                // Invalid generate authentication token for another user.
                return new AS400SecurityException(AS400SecurityException.GENERATE_TOKEN_REQUEST_NOT_VALID);
            case 0x00020001:
                // User ID errors: user ID unknown:
                // - user ID not found on system.
                // - EIM doesn't map Kerberos principal to a user profile.
                // - EIM maps the Kerberos principal to a user profile, but the profile doesn't exist on this system.
                return new AS400SecurityException(AS400SecurityException.USERID_UNKNOWN);
            case 0x00020002:
                // User ID errors: user ID valid, but revoked.
                return new AS400SecurityException(AS400SecurityException.USERID_DISABLE);
            case 0x00020003:
                // User profile mismatch.
                return new AS400SecurityException(AS400SecurityException.USERID_MISMATCH);
            case 0x00030001:
                // Password errors: new password or passphase longer than maximum accepted length.
                return new AS400SecurityException(AS400SecurityException.PASSWORD_NEW_TOO_LONG);
            case 0x00030002:
                // Password errors: new password or passphase shorter than minimum accepted length.
                return new AS400SecurityException(AS400SecurityException.PASSWORD_NEW_TOO_SHORT);
            case 0x00030003:
                // Password errors: new password or passphase contains character used more than once.
                return new AS400SecurityException(AS400SecurityException.PASSWORD_NEW_REPEAT_CHARACTER);
            case 0x00030004:
                // Password errors: new password or passphase has adjacent digits.
                return new AS400SecurityException(AS400SecurityException.PASSWORD_NEW_ADJACENT_DIGITS);
            case 0x00030005:
                // Password errors: new password or passphase contains a character repeated consecutively.
                return new AS400SecurityException(AS400SecurityException.PASSWORD_NEW_CONSECUTIVE_REPEAT_CHARACTER);
            case 0x00030006:
                // Password errors: new password or passphase was previously used.
                return new AS400SecurityException(AS400SecurityException.PASSWORD_NEW_PREVIOUSLY_USED);
            case 0x00030007:
                // Password errors: new password or passphase must contain at least one numeric.
                return new AS400SecurityException(AS400SecurityException.PASSWORD_NEW_NO_NUMERIC);
            case 0x00030008:
                // Password errors: new password or passphase contains an invalid character.
                return new AS400SecurityException(AS400SecurityException.PASSWORD_NEW_CHARACTER_NOT_VALID);
            case 0x00030009:
                // Password errors: new password or passphase exists in a dictionary of disallowed passwords or passphrases.
                return new AS400SecurityException(AS400SecurityException.PASSWORD_NEW_DISALLOWED);
            case 0x0003000A:
                // Password errors: new password or passphase contains user ID as part of the password or passphrase.
                return new AS400SecurityException(AS400SecurityException.PASSWORD_NEW_USERID);
            case 0x0003000B:
                // Password errors: password or passphrase incorrect.
                return new AS400SecurityException(AS400SecurityException.PASSWORD_INCORRECT);
            case 0x0003000C:
                // Password errors: profile will be disabled on the next invalid password or passphrase.
                return new AS400SecurityException(AS400SecurityException.PASSWORD_INCORRECT_USERID_DISABLE);
            case 0x0003000D:
                // Password errors: password or passphrase correct, but expired.
                return new AS400SecurityException(AS400SecurityException.PASSWORD_EXPIRED);
            case 0x0003000E:
                // Password errors: pre-V2R2 encrypted password.
                return new AS400SecurityException(AS400SecurityException.PASSWORD_PRE_V2R2);
            case 0x0003000F:
                // Password errors: new password or passphrase contains a character in the same position as the last password or passphrase.
                return new AS400SecurityException(AS400SecurityException.PASSWORD_NEW_SAME_POSITION);
            case 0x00030010:
                // Password errors: Password is *NONE.
                return new AS400SecurityException(AS400SecurityException.PASSWORD_NONE);
            case 0x00030011:
                // Password errors: Password validation program failed the request.
                return new AS400SecurityException(AS400SecurityException.PASSWORD_NEW_VALIDATION_PROGRAM);
            case 0x00040000:
                // General security errors, function not performed: No meaning.  Reasons for getting this return code include:
                // - QUSER's password expired.
                // - incorrect client CCSID passed on exchange client/server attributes request.
                // - failures in program QTQRCSC.
                // - error generating server's random seed.
                // - errors while swapping profiles with the QSYGETPH and QWTSETP API's.
                // - user profile errors.
                // - errors while retrieving the user's password or passphrase.
                // - errors while changing passwords or passphrases.
                // - failures reported by APIs that are used by the signon server.  These API's include QWTCHGJB, QSYRUSRI, QSYCHGPR, QWCCVTDT, and QSYCHGPW.
                // - input to Kerberos validation routine is not properly formatted or is not valid.
                // Check the QZSOSIGN jobs for possible messages.
                // Check the daemon jobs for possible messages.
                return new AS400SecurityException(AS400SecurityException.SECURITY_GENERAL);
            case 0x00040001:
                // General security errors, function not performed: QYSMPUT error due to incorrect program data length.
                throw new ServerStartupException(ServerStartupException.CONNECTION_NOT_PASSED_LENGTH);
            case 0x00040002:
                // General security errors, function not performed: QYSMPUT error because no responce was received (timeout) from the server job.
                throw new ServerStartupException(ServerStartupException.CONNECTION_NOT_PASSED_TIMEOUT);
            case 0x00040003:
                // General security errors, function not performed: QYSMPUT error because the server job could not be started.
                throw new ServerStartupException(ServerStartupException.CONNECTION_NOT_PASSED_SERVER_NOT_STARTED);
            case 0x00040004:
                // General security errors, function not performed: QYSMPUT error because the prestart job could not be started.
                throw new ServerStartupException(ServerStartupException.CONNECTION_NOT_PASSED_PRESTART_NOT_STARTED);
            case 0x00040005:
                // General security errors, function not performed: QYSMPUT error due to subsystem problems.
                throw new ServerStartupException(ServerStartupException.CONNECTION_NOT_PASSED_SUBSYSTEM);
            case 0x00040006:
                // General security errors, function not performed: QYSMPUT error because the server job is ending.
                throw new ServerStartupException(ServerStartupException.CONNECTION_NOT_PASSED_SERVER_ENDING);
            case 0x00040007:
                // General security errors, function not performed: QYSMPUT error because the receiver area is too small.
                throw new ServerStartupException(ServerStartupException.CONNECTION_NOT_PASSED_RECEIVER_AREA);
            case 0x00040008:
                // General security errors, function not performed: QYSMPUT error because the unknown or unrecoverable errors.
                throw new ServerStartupException(ServerStartupException.CONNECTION_NOT_PASSED_UNKNOWN);
            case 0x00040009:
                // General security errors, function not performed: QYSMPUT error because the user profile for the server job soes not exist.
                throw new ServerStartupException(ServerStartupException.CONNECTION_NOT_PASSED_PROFILE);
            case 0x0004000A:
                // General security errors, function not performed: QYSMPUT error due to authority problems releated to the profile for the server job.
                throw new ServerStartupException(ServerStartupException.CONNECTION_NOT_PASSED_AUTHORITY);
            case 0x0004000B:
                // General security errors, function not performed: QYSMPUT error because the server job program was not found.
                throw new ServerStartupException(ServerStartupException.CONNECTION_NOT_PASSED_PROGRAM_NOT_FOUND);
            case 0x0004000C:
                // General security errors, function not performed: QYSMPUT error because the daemon job is not authorized to use the library that contains the server job.
                throw new ServerStartupException(ServerStartupException.CONNECTION_NOT_PASSED_LIBRARY_AUTHORITY);
            case 0x0004000D:
                // General security errors, function not performed: QYSMPUT error because the daemon job is not authorized to the server job program.
                throw new ServerStartupException(ServerStartupException.CONNECTION_NOT_PASSED_PROGRAM_AUTHORITY);
            case 0x0004000E:
                // General security errors, function not performed: user not authorized to generate token for another user.
                return new AS400SecurityException(AS400SecurityException.GENERATE_TOKEN_AUTHORITY_INSUFFICIENT);
            case 0x0004000F:
                // General security errors, function not performed: no memory is available to allocate space needed for authorization.
                return new AS400SecurityException(AS400SecurityException.SERVER_NO_MEMORY);
            case 0x00040010:
                // General security errors, function not performed: error occurred when converting data between code pages.
                return new AS400SecurityException(AS400SecurityException.SERVER_CONVERSION_ERROR);
            case 0x00040011:
                // General security errors, function not performed: error occurred using EIM interfaces.
                return new AS400SecurityException(AS400SecurityException.SERVER_EIM_ERROR);
            case 0x00040012:
                // General security errors, function not performed: error occurred using cryptographic interfaces.
                return new AS400SecurityException(AS400SecurityException.SERVER_CRYPTO_ERROR);
            case 0x00040013:
                // General security errors, function not performed: this version of token is not supported by this version of code.
                return new AS400SecurityException(AS400SecurityException.SERVER_TOKEN_VERSION);
            case 0x00040014:
                // General security errors, function not performed: public key not found.
                return new AS400SecurityException(AS400SecurityException.SERVER_KEY_NOT_FOUND);
            case 0x00050001:
                // Exit program errors: error processing exit point.
                return new AS400SecurityException(AS400SecurityException.EXIT_POINT_PROCESSING_ERROR);
            case 0x00050002:
                // Exit program errors: resolving to exit program.
                return new AS400SecurityException(AS400SecurityException.EXIT_PROGRAM_RESOLVE_ERROR);
            case 0x00050003:
                // Exit program errors: user exit program call error.
                return new AS400SecurityException(AS400SecurityException.EXIT_PROGRAM_CALL_ERROR);
            case 0x00050004:
                // Exit program errors: user exit program denied request.
                return new AS400SecurityException(AS400SecurityException.EXIT_PROGRAM_DENIED_REQUEST);
            case 0x00060001:
                // Authentication token errors: profile token or identity token not valid.
                return new AS400SecurityException(AS400SecurityException.PROFILE_TOKEN_NOT_VALID);
            case 0x00060002:
                // Authentication token errors: maximum number of profile tokens for the system already generated.
                return new AS400SecurityException(AS400SecurityException.PROFILE_TOKEN_NOT_VALID_MAXIMUM);
            case 0x00060003:
                // Authentication token errors: invalid value sent for timeout interval.
                return new AS400SecurityException(AS400SecurityException.PROFILE_TOKEN_NOT_VALID_TIMEOUT_NOT_VALID);
            case 0x00060004:
                // Authentication token errors: invalid type of profile token request.
                return new AS400SecurityException(AS400SecurityException.PROFILE_TOKEN_NOT_VALID_TYPE_NOT_VALID);
            case 0x00060005:
                // Authentication token errors: existing profile token isn't regenerable, can't be used to generate a new profile token.
                return new AS400SecurityException(AS400SecurityException.PROFILE_TOKEN_NOT_VALID_NOT_REGENERABLE);
            case 0x00060006:
                // Authentication token errors: Kerberos ticket not valid - consistency checks failed.
                return new AS400SecurityException(AS400SecurityException.KERBEROS_TICKET_NOT_VALID_CONSISTENCY);
            case 0x00060007:
                // Authentication token errors: requested mechanisms not supported by local system.
                return new AS400SecurityException(AS400SecurityException.KERBEROS_TICKET_NOT_VALID_MECHANISM);
            case 0x00060008:
                // Authentication token errors: credentials not available or not valid for this context.
                return new AS400SecurityException(AS400SecurityException.KERBEROS_TICKET_NOT_VALID_CREDENTIAL_NOT_VALID);
            case 0x00060009:
                // Authentication token errors: Kerberos token or identity token contains incorrect signature.
                return new AS400SecurityException(AS400SecurityException.KERBEROS_TICKET_NOT_VALID_SIGNATURE);
            case 0x0006000A:
                // Authentication token errors: credentials no longer valid.
                return new AS400SecurityException(AS400SecurityException.KERBEROS_TICKET_NOT_VALID_CREDENTIAL_NO_LONGER_VALID);
            case 0x0006000B:
                // Authentication token errors: consistency checks on the credantial structure failed, or a mismatch exists between an authentication token and information provided to the identity token functions.
                return new AS400SecurityException(AS400SecurityException.KERBEROS_TICKET_NOT_VALID_CONSISTENCY);
            case 0x0006000C:
                // Authentication token errors: failure of verification routine.
                return new AS400SecurityException(AS400SecurityException.KERBEROS_TICKET_NOT_VALID_VERIFICATION);
            case 0x0006000D:
                // Authentication token errors: EIM configuration error, or an EIM identifier was not found, or an application identifier was not found.
                return new AS400SecurityException(AS400SecurityException.KERBEROS_TICKET_NOT_VALID_EIM);
            case 0x0006000E:
                // Authentication token errors: Kerberos principal maps to a system profile which can not be used to sign on.
                return new AS400SecurityException(AS400SecurityException.KERBEROS_TICKET_NOT_VALID_SYSTEM_PROFILE);
            case 0x0006000F:
                // Authentication token errors: Kerberos principal maps to multiple user profile names, or more than one EIM entry was found for an identity token.
                return new AS400SecurityException(AS400SecurityException.KERBEROS_TICKET_NOT_VALID_MULTIPLE_PROFILES);
            case 0x00070001:
                // Generate token errors: can not connect to the system EIM domain.
                return new AS400SecurityException(AS400SecurityException.GENERATE_TOKEN_CAN_NOT_CONNECT);
            case 0x00070002:
                // Generate token errors: can not change the CCSID to use for EIM requests to 13488.
                return new AS400SecurityException(AS400SecurityException.GENERATE_TOKEN_CAN_NOT_CHANGE_CCSID);
            case 0x00070003:
                // Generate token errors: can not obtain the EIM registry name.
                return new AS400SecurityException(AS400SecurityException.GENERATE_TOKEN_CAN_NOT_OBTAIN_NAME);
            case 0x00070004:
                // Generate token errors: no mapping exists between the WebSphere Portal user identity and an i5/OS user profile.
                return new AS400SecurityException(AS400SecurityException.GENERATE_TOKEN_NO_MAPPING);
            default:
                // Internal errors or unexpected return codes.
                return new AS400SecurityException(AS400SecurityException.UNKNOWN);
        }
    }

    void setGSSCredential(Object gssCredential)
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Setting GSS credential into impl: '" + gssCredential + "'");
        gssCredential_ = gssCredential;
    }

    // Set port for service.
    public void setServicePort(String systemName, int service, int port)
    {
        PortMapper.setServicePort(systemName, service, port, useSSLConnection_);
    }

    // Set all the ports for a system name to the defaults.
    public void setServicePortsToDefault(String systemName)
    {
        PortMapper.setServicePortsToDefault(systemName);
    }

    // Flag that indicates if we must use network sockets and not unix domain sockets.
    private boolean mustUseNetSockets_ = false;
    // Flag that indicates if we must not use the current profile.
    private boolean mustUseSuppliedProfile_ = false;
    // Set the state variables for this implementation object.
    public void setState(SSLOptions useSSLConnection, boolean canUseNativeOptimization, boolean threadUsed, int ccsid, String nlv, SocketProperties socketProperties, String ddmRDB, boolean mustUseNetSockets, boolean mustUseSuppliedProfile)
    {
        if (Trace.traceOn_)
        {
            Trace.log(Trace.DIAGNOSTIC, "Setting up AS400 implementation object:");
            Trace.log(Trace.DIAGNOSTIC, "  Enable SSL connections: " + useSSLConnection);
            Trace.log(Trace.DIAGNOSTIC, "  Native optimizations allowed:", canUseNativeOptimization);
            Trace.log(Trace.DIAGNOSTIC, "  Use threaded communications:", threadUsed);
            Trace.log(Trace.DIAGNOSTIC, "  User specified CCSID:", ccsid);
            Trace.log(Trace.DIAGNOSTIC, "  NLV: " + nlv);
            Trace.log(Trace.DIAGNOSTIC, "  Socket properties: " + socketProperties);
            Trace.log(Trace.DIAGNOSTIC, "  DDM RDB: " + ddmRDB);
            Trace.log(Trace.DIAGNOSTIC, "  Must use net sockets: " + mustUseNetSockets);
            Trace.log(Trace.DIAGNOSTIC, "  Must use supplied profile: " + mustUseSuppliedProfile);
        }
        useSSLConnection_ = useSSLConnection;
        canUseNativeOptimization_ = canUseNativeOptimization;
        threadUsed_ = threadUsed;
        if (ccsid != 0)
        {
            userOverrideCcsid_ = true;
            ccsid_ = ccsid;
        }
        nlv_ = nlv;
        socketProperties_ = socketProperties;
        ddmRDB_ = ddmRDB;
        mustUseNetSockets_ = mustUseNetSockets;
        mustUseSuppliedProfile_ = mustUseSuppliedProfile;
    }

    // Exchange sign-on flows with sign-on server.
    public SignonInfo signon(String systemName, boolean systemNameLocal, String userId, byte[] bytes, int byteType, String gssName, int gssOption) throws AS400SecurityException, IOException
    {
        systemName_ = systemName;
        systemNameLocal_ = systemNameLocal;
        userId_ = userId;
        gssName_ = gssName;
        gssOption_ = gssOption;

        if (bytes != null)
        {
            if (byteType == AS400.AUTHENTICATION_SCHEME_GSS_TOKEN)
            {
                bytes_ = bytes;
            }
            else
            {
                adder_ = new byte[9];
                AS400.rng.nextBytes(adder_);

                mask_ = new byte[7];
                AS400.rng.nextBytes(mask_);

                bytes_ = encode(adder_, mask_, decode(proxySeed_, remoteSeed_, bytes));
            }
            byteType_ = byteType;
        }
        proxySeed_ = null;
        remoteSeed_ = null;

        if (canUseNativeOptimization_)
        {
            byte[] swapToPH = new byte[12];
            byte[] swapFromPH = new byte[12];
            boolean didSwap = swapTo(swapToPH, swapFromPH);
            try
            {
                byte[] data = AS400ImplNative.signonNative(SignonConverter.stringToByteArray(userId));
                GregorianCalendar date = new GregorianCalendar(BinaryConverter.byteArrayToUnsignedShort(data, 0)/*year*/, (int)(data[2] - 1)/*month convert to zero based*/, (int)(data[3])/*day*/, (int)(data[4])/*hour*/, (int)(data[5])/*minute*/, (int)(data[6])/*second*/);
                signonInfo_ = new SignonInfo();
                signonInfo_.currentSignonDate = date;
                signonInfo_.lastSignonDate = date;
                signonInfo_.expirationDate = (BinaryConverter.byteArrayToInt(data, 8) == 0) ? null : new GregorianCalendar(BinaryConverter.byteArrayToUnsignedShort(data, 8)/*year*/, (int)(data[10] - 1)/*month convert to zero based*/, (int)(data[11])/*day*/, (int)(data[12])/*hour*/, (int)(data[13])/*minute*/, (int)(data[14])/*second*/);

                signonInfo_.version = AS400.nativeVRM;
                try
                {
                    signonInfo_.serverCCSID = getCcsidFromServer();
                }
                catch (InterruptedException e) {}
            }
            catch (NativeException e)
            {
                // Map native exception to AS400SecurityException.
                throw mapNativeSecurityException(e);
            }
            finally
            {
                if (didSwap) swapBack(swapToPH, swapFromPH);
            }
        }
        else
        {
            if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Opening a socket to verify security...");
            // Validate user id and password.
            signonConnect();
            try
            {
                InputStream inStream = signonConnection_.getInputStream();
                OutputStream outStream = signonConnection_.getOutputStream();

                byte[] userIDbytes = byteType_ == AS400.AUTHENTICATION_SCHEME_PASSWORD ? SignonConverter.stringToByteArray(userId) : null;
                byte[] encryptedPassword = byteType_ == AS400.AUTHENTICATION_SCHEME_GSS_TOKEN ? bytes_ : getPassword(clientSeed_, serverSeed_);

                if (PASSWORD_TRACE)
                {
                    Trace.log(Trace.DIAGNOSTIC, "Sending Retrieve Signon Information Request...");
                    Trace.log(Trace.DIAGNOSTIC, "  User ID: " + userId);
                    Trace.log(Trace.DIAGNOSTIC, "  User ID bytes:", userIDbytes);
                    Trace.log(Trace.DIAGNOSTIC, "  Client seed:", clientSeed_);
                    Trace.log(Trace.DIAGNOSTIC, "  Server seed:", serverSeed_);
                    Trace.log(Trace.DIAGNOSTIC, "  Encrypted password:", encryptedPassword);
                }

                SignonInfoReq signonReq = new SignonInfoReq(userIDbytes, encryptedPassword, byteType_);
                signonReq.write(outStream);

                SignonInfoRep signonRep = new SignonInfoRep();
                signonRep.read(inStream);

                if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Read security validation reply...");

                int rc = signonRep.getRC();
                if (rc != 0)
                {
                    byte[] rcBytes = new byte[4];
                    BinaryConverter.intToByteArray(rc, rcBytes, 0);
                    Trace.log(Trace.ERROR, "Security validation failed with return code:", rcBytes);
                    throw AS400ImplRemote.returnSecurityException(rc);
                }

                if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Security validated successfully.");

                signonInfo_ = new SignonInfo();
                signonInfo_.currentSignonDate = signonRep.getCurrentSignonDate();
                signonInfo_.lastSignonDate = signonRep.getLastSignonDate();
                signonInfo_.expirationDate = signonRep.getExpirationDate();
                signonInfo_.version = version_;
                signonInfo_.serverCCSID = signonRep.getServerCCSID();
                if (userId_.length() == 0)
                {
                    byte[] b = signonRep.getUserIdBytes();
                    if (b != null)
                    {
                        userId_ = SignonConverter.byteArrayToString(b);
                        signonInfo_.userId = userId_;
                    }
                }

                if (DataStream.getDefaultConverter() == null)
                {
                    if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Signon server reports CCSID:", signonInfo_.serverCCSID);
                    DataStream.setDefaultConverter(ConverterImplRemote.getConverter(signonInfo_.serverCCSID, this));
                }
                ConverterImplRemote converter = ConverterImplRemote.getConverter(signonInfo_.serverCCSID, this);
                signonJobString_ = converter.byteArrayToString(signonJobBytes_);
                if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Signon server job: " + signonJobString_);
            }
            catch (IOException e)
            {
                Trace.log(Trace.ERROR, "Signon failed:", e);
                try
                {
                    signonConnection_.close();
                }
                catch (IOException ee)
                {
                    Trace.log(Trace.ERROR, "Error closing signon socket:", ee);
                }
                signonConnection_ = null;
                throw e;
            }
            catch (AS400SecurityException e)
            {
                Trace.log(Trace.ERROR, "Signon failed:", e);
                try
                {
                    signonConnection_.close();
                }
                catch (IOException ee)
                {
                    Trace.log(Trace.ERROR, "Error closing signon socket:", ee);
                }
                signonConnection_ = null;
                throw e;
            }
        }
        return signonInfo_;
    }

    // Connect to sign-on server.
    private synchronized void signonConnect() throws AS400SecurityException, IOException
    {
        if (signonConnection_ == null)
        {
            signonConnection_ = PortMapper.getServerSocket((systemNameLocal_) ? "localhost" : systemName_, AS400.SIGNON, useSSLConnection_, socketProperties_, mustUseNetSockets_);
            try
            {
                InputStream inStream = signonConnection_.getInputStream();
                OutputStream outStream = signonConnection_.getOutputStream();

                clientSeed_ = byteType_ == AS400.AUTHENTICATION_SCHEME_PASSWORD ? BinaryConverter.longToByteArray(System.currentTimeMillis()) : null;

                SignonExchangeAttributeReq attrReq = new SignonExchangeAttributeReq(clientSeed_);
                attrReq.write(outStream);

                SignonExchangeAttributeRep attrRep = new SignonExchangeAttributeRep();
                attrRep.read(inStream);

                if (attrRep.getRC() != 0)
                {
                    // Connect failed, throw exception.
                    byte[] rcBytes = new byte[4];
                    BinaryConverter.intToByteArray(attrRep.getRC(), rcBytes, 0);
                    Trace.log(Trace.ERROR, "Signon server exchange client/server attributes failed, return code:", rcBytes);
                    throw AS400ImplRemote.returnSecurityException(attrRep.getRC());
                }

                version_ = new ServerVersion(attrRep.getServerVersion());
                serverLevel_ = attrRep.getServerLevel();
                passwordType_ = attrRep.getPasswordLevel();
                isPasswordTypeSet_ = true;
                serverSeed_ = attrRep.getServerSeed();
                signonJobBytes_ = attrRep.getJobNameBytes();

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

                fireConnectEvent(true, AS400.SIGNON);
                if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Socket opened successfully.");
            }
            catch (IOException e)
            {
                Trace.log(Trace.ERROR, "Signon server exchange client/server attributes failed:", e);
                try
                {
                    signonConnection_.close();
                }
                catch (IOException ee)
                {
                    Trace.log(Trace.ERROR, "Error closing signon socket:", ee);
                }
                signonConnection_ = null;
                throw e;
            }
        }
    }

    // Disconnect from sign-on server.
    private synchronized void signonDisconnect()
    {
        if (signonConnection_ != null)
        {
            try
            {
                if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Sending end job data stream to signon server...");
                OutputStream outStream = signonConnection_.getOutputStream();
                SignonEndServerReq signonEnd = new SignonEndServerReq();
                signonEnd.write(outStream);
                signonConnection_.close();
            }
            catch (IOException e)
            {
                Trace.log(Trace.ERROR, "Error sending end job data stream to signon server:", e);
            }
            signonConnection_ = null;
            fireConnectEvent(false, AS400.SIGNON);
        }
    }

    boolean swapTo(byte[] swapToPH, byte[] swapFromPH) throws AS400SecurityException, IOException
    {
        if (AS400.onAS400 && AS400.currentUserAvailable() && userId_.equals(CurrentUser.getUserID(AS400.nativeVRM.getVersionReleaseModification()))) return false;
        if (bytes_ == null)
        {
            Trace.log(Trace.ERROR, "Password is null.");
            throw new AS400SecurityException(AS400SecurityException.PASSWORD_NOT_SET);
        }
        try
        {
            byte[] temp = decode(adder_, mask_, bytes_);
            // Screen out passwords that start with a star.
            if (temp[0] == 0x00 && temp[1] == 0x2A)
            {
                Trace.log(Trace.ERROR, "Parameter 'password' begins with a '*' character.");
                throw new AS400SecurityException(AS400SecurityException.SIGNON_CHAR_NOT_VALID);
            }
            AS400ImplNative.swapToNative(SignonConverter.stringToByteArray(userId_), temp, swapToPH, swapFromPH);
        }
        catch (NativeException e)
        {
            // Map native exception to AS400SecurityException.
            throw mapNativeSecurityException(e);
        }
        return true;
    }

    void swapBack(byte[] swapToPH, byte[] swapFromPH) throws AS400SecurityException, IOException
    {
        try
        {
            AS400ImplNative.swapBackNative(swapToPH, swapFromPH);
        }
        catch (NativeException e)
        {
            // Map native exception to AS400SecurityException.
            throw mapNativeSecurityException(e);
        }
    }

    // Return a security exception based on the data received from the native method.
    private AS400SecurityException mapNativeSecurityException(NativeException e) throws IOException
    {
        // Parse information from byte array.
        String id = ConverterImplRemote.getConverter(37, this).byteArrayToString(e.data, 12, 7);

        if (id.equals("CPF2203") || id.equals("CPF2204"))
        {
            return new AS400SecurityException(AS400SecurityException.USERID_UNKNOWN);
        }
        if (id.equals("CPF22E3"))
        {
            return new AS400SecurityException(AS400SecurityException.USERID_DISABLE);
        }
        if (id.equals("CPF22E2") || id.equals("CPF22E5"))
        {
            return new AS400SecurityException(AS400SecurityException.PASSWORD_INCORRECT);
        }
        if (id.equals("CPF22E4"))
        {
            return new AS400SecurityException(AS400SecurityException.PASSWORD_EXPIRED);
        }
        return new AS400SecurityException(AS400SecurityException.SECURITY_GENERAL);
    }

    // This method is to generate the password, the protected old password and the protected new password. This is used for change password.
    //
    // Note: All input strings are EBCDIC and maximum length of 10 bytes.  They must be terminated with EBCDIC space character (0x40) if length is less than 10.
    //
    // input:
    //  userID:  EBCDIC userID
    //  oldPassword:  EBCDIC old password
    //  newPassword:  EBCDIC new password
    //  sequence number:  Host format 8-byte sequence number
    //                    For example: sequence number "1" is { 0, 0, 0, 0, 0, 0, 0, 1 }
    //  clientSeed:  Host format 8-byte random client seed number.
    //  hostSeed:  Host format 8-byte random host seed number.
    private static byte[] encryptNewPassword(byte[] userID, byte[] oldPwd, byte[] newPwd, byte[] protectedOldPwd, byte[] protectedNewPwd, byte[] clientSeed, byte[] serverSeed)
    {
        byte[] verifyToken = new byte[8];
        byte[] sequence = {0, 0, 0, 0, 0, 0, 0, 1};

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

        // if the newPassword is more than 8 bytes generate the second 8 bytes of the protected new password
        if (protectedNewPwd.length == 16)
        {
            byte[] secondNewPassword = new byte[8];
            // increment the sequence number for the next copy of the substitute
            incrementString(sequence);
            tempEncryptedPassword = generatePasswordSubstitute(userID, token, verifyToken, sequence, clientSeed, serverSeed);

            // left justify the 9 and 10 bytes of the new password
            for (int i = 0; i < 8; i++)
            {
                secondNewPassword[i] = (byte)0x40;
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

        // increment the sequence number for the first copy of the protected old password
        incrementString(sequence);

        // generate the first copy of the protected new password
        tempEncryptedPassword = generatePasswordSubstitute(userID, token, verifyToken, sequence, clientSeed, serverSeed);
        // exclusive or the first copy of the protected old password
        // This is the first 8 bytes of the protected old password
        xORArray(tempEncryptedPassword, oldPwd, protectedOldPwd);

        // if the oldPassword is more than 8 bytes
        //   generate the second 8 bytes of the protected old password
        if (protectedOldPwd.length == 16)
        {
            byte[] secondOldPassword = new byte[8];
            // increment the sequence number for the next copy of the substitute
            incrementString(sequence);

            tempEncryptedPassword = generatePasswordSubstitute(userID, token, verifyToken, sequence, clientSeed, serverSeed);

            // left justify the 9 and 10 bytes of the old password
            for (int i = 0; i < 8; i++)
            {
                secondOldPassword[i] = (byte)0x40;
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
        byte[] sequenceNumber = {0, 0, 0, 0, 0, 0, 0, 1};
        byte[] verifyToken = new byte[8];

        byte[] token = generateToken(userID, pwd);
        if (PASSWORD_TRACE)
        {
            Trace.log(Trace.DIAGNOSTIC, "In encryptPassword, token: ", token);
        }

        byte[] encryptedPassword = generatePasswordSubstitute(userID, token, verifyToken, sequenceNumber, clientSeed, serverSeed);
        if (PASSWORD_TRACE)
        {
            Trace.log(Trace.DIAGNOSTIC, "In encryptPassword, encryptedPassword: ", encryptedPassword);
        }
        return encryptedPassword;
    }

    // void gen_pwd_sbs( byte[] user_id,
    //                   byte[] password_token,
    //                   byte[] password_substitute,
    //                   byte[] pwdseq,
    //                   byte[] rds,
    //                   byte[] rdr)
    //
    // Note: rdr, rds and pwdseq are all members of the PWDSBS_SEEDS structure.
    //
    // Function: Generate password substitute.
    //           Perform steps 5 to 7 of the password substitute formula.
    //           Steps 1 to 4 were already performed by the generate password token routine.
    //           It also generate the password verifier.
    //
    //  Passwor Substitute formula:
    //  (5) Increment PWSEQs and store it.
    //
    //  (6) Add PWSEQs to RDr to get RDrSEQ.
    //
    //  (7) PW_SUB = MAC:sub.DES:esub.( PW_TOKEN,(RDrSEQ,RDs,ID xor RDrSEQ)):
    //
    //  LEGEND:
    //    PW  User password
    //    XOR  EXCLUSIVE OR
    //    ID  User identifier
    //    ENC:sub.DES:esub  Encipher using the Data Encryption Standard algorithm
    //    MAC:sub.DES:esub  Generate a Message authentication code using DES
    //    RDs  Random data sent to the partner LU on BIND
    //    RDr  Random data received from the partner LU on BIND
    //    PWSEQs  Sequence number for password substitution on the send side
    //    RDrSEQ  The arithmetic sum of RDr and the current value of PWSEQs.
    //    DES  Data Encryption Standard algorithm
    //
    //  Note: The MAC(DES) function was implemented according to the description given in the MI functional reference for the CIPHER function. Under the section "Cipher Block Chaining".  Basically what it says is that the MAC des use the DES algorithm to encrypt the first data block (8 bytes) the result is then exclusive ORed with the next data block and it become the data input for the DES algorithm. For subsequents blocks of data the same operation is repeated.
    private static byte[] generatePasswordSubstitute(byte[] userID, byte[] token, byte[] password_verifier, byte[] sequenceNumber, byte[] clientSeed, byte[] serverSeed)
    {
        byte[] RDrSEQ = new byte[8];
        byte[] nextData = new byte[8];
        byte[] nextEncryptedData = new byte[8];

        //first data or RDrSEQ = password sequence + host seed
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
        for (int i = 0; i < 8; i++)
        {
            nextData[i] = (byte)0x40;
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

        // fifth encrypted data = DES(token, fifth data) this is the encrypted password
        return enc_des(token, nextData);
    }

    // userID and password are in EBCDIC
    // userID and password are terminated with 0x40 (EBCDIC blank)
    private static byte[] generateToken(byte[] userID, byte[] password)
    {
        byte[] token = new byte[8];
        byte[] workBuffer1 = new byte[10];
        byte[] workBuffer2 = { 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40 };
        byte[] workBuffer3 = { 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40, 0x40 };

        // Copy user ID into the work buffer.
        System.arraycopy(userID, 0, workBuffer1, 0, 10);

        // Find userID length.
        int length = ebcdicStrLen(userID, 10);

        if (length > 8)
        {
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
        if (PASSWORD_TRACE)
        {
            Trace.log(Trace.DIAGNOSTIC, "In generateToken, folded user ID: ", workBuffer1);
        }

        // work with password
        length = ebcdicStrLen(password, 10);

        // if password is more than 8 characters long
        if (length > 8)
        {
            // copy the first 8 bytes of password to workBuffer2
            System.arraycopy(password, 0, workBuffer2, 0, 8);

            // copy the remaining password to workBuffer3
            System.arraycopy(password, 8, workBuffer3, 0, length-8);

            // generate the token for the first 8 bytes of password
            xorWith0x55andLshift(workBuffer2);

            workBuffer2 =  // first token
              enc_des(workBuffer2,  // shifted result
                      workBuffer1);  // userID

            // generate the token for the second 8 bytes of password
            xorWith0x55andLshift(workBuffer3);

            workBuffer3 =  // second token
              enc_des(workBuffer3,  // shifted result
                      workBuffer1);  // userID

            // exclusive-or the first and second token to get the real token
            xORArray(workBuffer2, workBuffer3, token);
        }
        else
        {
            // copy password to work buffer
            System.arraycopy(password, 0, workBuffer2, 0, length);
            if (PASSWORD_TRACE)
            {
                Trace.log(Trace.DIAGNOSTIC, "In generateToken, workBuffer2: ", workBuffer2);
            }

            // generate the token for 8 byte userID
            xorWith0x55andLshift(workBuffer2);
            if (PASSWORD_TRACE)
            {
                Trace.log(Trace.DIAGNOSTIC, "In generateToken, workBuffer2: ", workBuffer2);
            }

            token =  // token
              enc_des(workBuffer2,  // shifted result
                      workBuffer1);  // userID
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
            result[i] = (byte)temp;
        }
    }

    private static int ebcdicStrLen(byte[] string, int maxLength)
    {
        int i = 0;
        while ((i < maxLength) && (string[i] != 0x40) && (string[i] != 0)) ++i;
        return i;
    }

    // increment host format 8-byte number
    private static void incrementString(byte[] string)
    {
        byte[] one = { 0, 0, 0, 0, 0, 0, 0, 1 };

        addArray(string, one, string, 8);
    }

    private static void xORArray(byte[] string1, byte[] string2, byte[] string3)
    {
        for (int i = 0; i < 8; i++)
        {
            string3[i] = (byte)(string1[i] ^ string2[i]);
        }
    }

    private static void xorWith0x55andLshift(byte[] bytes)
    {
        bytes[0] ^= 0x55;
        bytes[1] ^= 0x55;
        bytes[2] ^= 0x55;
        bytes[3] ^= 0x55;
        bytes[4] ^= 0x55;
        bytes[5] ^= 0x55;
        bytes[6] ^= 0x55;
        bytes[7] ^= 0x55;

        bytes[0] = (byte)(bytes[0] << 1 | (bytes[1] & 0x80) >>> 7);
        bytes[1] = (byte)(bytes[1] << 1 | (bytes[2] & 0x80) >>> 7);
        bytes[2] = (byte)(bytes[2] << 1 | (bytes[3] & 0x80) >>> 7);
        bytes[3] = (byte)(bytes[3] << 1 | (bytes[4] & 0x80) >>> 7);
        bytes[4] = (byte)(bytes[4] << 1 | (bytes[5] & 0x80) >>> 7);
        bytes[5] = (byte)(bytes[5] << 1 | (bytes[6] & 0x80) >>> 7);
        bytes[6] = (byte)(bytes[6] << 1 | (bytes[7] & 0x80) >>> 7);
        bytes[7] <<= 1;
    }

    // the E function used in the cipher function
    private static final int[] EPERM =
    {
        32,  1,  2,  3,  4,  5,
         4,  5,  6,  7,  8,  9,
         8,  9, 10, 11, 12, 13,
        12, 13, 14, 15, 16, 17,
        16, 17, 18, 19, 20, 21,
        20, 21, 22, 23, 24, 25,
        24, 25, 26, 27, 28, 29,
        28, 29, 30, 31, 32,  1
    };

    // the initial scrambling of the input data
    private static final int[] INITPERM =
    {
        58, 50, 42, 34, 26, 18, 10, 2,
        60, 52, 44, 36, 28, 20, 12, 4,
        62, 54, 46, 38, 30, 22, 14, 6,
        64, 56, 48, 40, 32, 24, 16, 8,
        57, 49, 41, 33, 25, 17,  9, 1,
        59, 51, 43, 35, 27, 19, 11, 3,
        61, 53, 45, 37, 29, 21, 13, 5,
        63, 55, 47, 39, 31, 23, 15, 7
    };

    // the inverse permutation of initperm - used on the proutput block
    private static final int[] OUTPERM =
    {
        40, 8, 48, 16, 56, 24, 64, 32,
        39, 7, 47, 15, 55, 23, 63, 31,
        38, 6, 46, 14, 54, 22, 62, 30,
        37, 5, 45, 13, 53, 21, 61, 29,
        36, 4, 44, 12, 52, 20, 60, 28,
        35, 3, 43, 11, 51, 19, 59, 27,
        34, 2, 42, 10, 50, 18, 58, 26,
        33, 1, 41,  9, 49, 17, 57, 25
    };

    // the P function used in cipher function
    private static final int[] PPERM =
    {
        16,  7, 20, 21,
        29, 12, 28, 17,
         1, 15, 23, 26,
         5, 18, 31, 10,
         2,  8, 24, 14,
        32, 27,  3,  9,
        19, 13, 30,  6,
        22, 11,  4, 25
    };

    private static final int[] PC1 =  // Permuted Choice 1
    {  // get the 56 bits which make up C0 and D0 (combined into Cn) from the original key
        57, 49, 41, 33, 25, 17,  9,
         1, 58, 50, 42, 34, 26, 18,
        10,  2, 59, 51, 43, 35, 27,
        19, 11,  3, 60, 52, 44, 36,
        63, 55, 47, 39, 31, 23, 15,
         7, 62, 54, 46, 38, 30, 22,
        14,  6, 61, 53, 45, 37, 29,
        21, 13,  5, 28, 20, 12,  4
    };

    private static final int[] PC2 =  // Permuted Choice 2
    {  // used in generation of the 16 subkeys
        14, 17, 11, 24,  1,  5,
         3, 28, 15,  6, 21, 10,
        23, 19, 12,  4, 26,  8,
        16, 7,  27, 20, 13,  2,
        41, 52, 31, 37, 47, 55,
        30, 40, 51, 45, 33, 48,
        44, 49, 39, 56, 34, 53,
        46, 42, 50, 36, 29, 32
    };

    private static final int[] S1 =
    {
        14,  4, 13,  1,  2, 15, 11,  8,  3, 10,  6, 12,  5,  9,  0,  7,
         0, 15,  7,  4, 14,  2, 13,  1, 10,  6, 12, 11,  9,  5,  3,  8,
         4,  1, 14,  8, 13,  6,  2, 11, 15, 12,  9,  7,  3, 10,  5,  0,
        15, 12,  8,  2,  4,  9,  1,  7,  5, 11,  3, 14, 10,  0,  6, 13
    };

    private static final int[] S2 =
    {
        15,  1,  8, 14,  6, 11,  3,  4,  9,  7,  2, 13, 12,  0,  5, 10,
         3, 13,  4,  7, 15,  2,  8, 14, 12,  0,  1, 10,  6,  9, 11,  5,
         0, 14,  7, 11, 10,  4, 13,  1,  5,  8, 12,  6,  9,  3,  2, 15,
        13,  8, 10,  1,  3, 15,  4,  2, 11,  6,  7, 12,  0,  5, 14,  9
    };

    private static final int[] S3 =
    {
        10,  0,  9, 14,  6,  3, 15,  5,  1, 13, 12,  7, 11,  4,  2,  8,
        13,  7,  0,  9,  3,  4,  6, 10,  2,  8,  5, 14, 12, 11, 15,  1,
        13,  6,  4,  9,  8, 15,  3,  0, 11,  1,  2, 12,  5, 10, 14,  7,
         1, 10, 13,  0,  6,  9,  8,  7,  4, 15, 14,  3, 11,  5,  2, 12
    };

    private static final int[] S4 =
    {
         7, 13, 14,  3,  0,  6,  9, 10,  1,  2,  8,  5, 11, 12,  4, 15,
        13,  8, 11,  5,  6, 15,  0,  3,  4,  7,  2, 12,  1, 10, 14,  9,
        10,  6,  9,  0, 12, 11,  7, 13, 15,  1,  3, 14,  5,  2,  8,  4,
         3, 15,  0,  6, 10,  1, 13,  8,  9,  4,  5, 11, 12,  7,  2, 14
    };

    private static final int[] S5 =
    {
         2, 12,  4,  1,  7, 10, 11,  6,  8,  5,  3, 15, 13,  0, 14,  9,
        14, 11,  2, 12,  4,  7, 13,  1,  5,  0, 15, 10,  3,  9,  8,  6,
         4,  2,  1, 11, 10, 13,  7,  8, 15,  9, 12,  5,  6,  3,  0, 14,
        11,  8, 12,  7,  1, 14,  2, 13,  6, 15,  0,  9, 10,  4,  5,  3
    };

    private static final int[] S6 =
    {
        12,  1, 10, 15,  9,  2,  6,  8,  0, 13,  3,  4, 14,  7,  5, 11,
        10, 15,  4,  2,  7, 12,  9,  5,  6,  1, 13, 14,  0, 11,  3,  8,
         9, 14, 15,  5,  2,  8, 12,  3,  7,  0,  4, 10,  1, 13, 11,  6,
         4,  3,  2, 12,  9,  5, 15, 10, 11, 14,  1,  7,  6,  0,  8, 13
    };

    private static final int[] S7 =
    {
         4, 11,  2, 14, 15,  0,  8, 13,  3, 12,  9,  7,  5, 10,  6,  1,
        13,  0, 11,  7,  4,  9,  1, 10, 14,  3,  5, 12,  2, 15,  8,  6,
         1,  4, 11, 13, 12,  3,  7, 14, 10, 15,  6,  8,  0,  5,  9,  2,
         6, 11, 13,  8,  1,  4, 10,  7,  9,  5,  0, 15, 14,  2,  3, 12
    };

    private static final int[] S8 =
    {
        13,  2,  8,  4,  6, 15, 11,  1, 10,  9,  3, 14,  5,  0, 12,  7,
         1, 15, 13,  8, 10,  3,  7,  4, 12,  5,  6, 11,  0, 14,  9,  2,
         7, 11,  4,  1,  9, 12, 14,  2,  0,  6, 10, 13, 15,  3,  5,  8,
         2,  1, 14,  7,  4, 10,  8, 13, 15, 12,  9,  0,  3,  5,  6, 11
    };

    // Name:  enc_des - Encrypt function front interface
    //
    // Function:  This function is the interface to the DES encryption routine
    //            It converts the parameters to a format expected by the actual DES encryption routine.
    //
    // Input:   8 byte data to encrypt
    //          8 byte key to encrypt
    //
    // Output:  8 byte encrypted data passed parameter
    //
    //***************************************************************************
    //
    // enc_des(byte[] data, byte[] key, byte[] enc_data)
    // {
    //   Copy the passed parameters to local variables so we can have 9 bytes variables.
    //   Expand the key and data variables so we will have one byte representing one bit of the input data.
    //   Perform the actual encryption of the input data using the 64 bytes variable according with the DES algorithm.
    //   Compress back the result of the encryption to return the 8 bytes data encryption result.
    // }

    private static byte[] enc_des(byte[] key, byte[] data)
    {
        if (PASSWORD_TRACE)
        {
            Trace.log(Trace.DIAGNOSTIC, "In enc_des, key: ", key);
            Trace.log(Trace.DIAGNOSTIC, "In enc_des, data: ", data);
        }
        // expend strings, 1 bit per byte, 1 char in 8 bytes
        byte[] e1 = new byte[65];
        byte[] e2 = new byte[65];

        // input strings, 1 character per byte password user id to be used as key encrypted data


        // expand the input string to 1 bit per byte again for the key
        for (int i = 0; i < 8; ++i)
        {
            e1[8 * i + 1] = (byte)(((data[i] & 0x80) == 0) ? 0x30 : 0x31);
            e1[8 * i + 2] = (byte)(((data[i] & 0x40) == 0) ? 0x30 : 0x31);
            e1[8 * i + 3] = (byte)(((data[i] & 0x20) == 0) ? 0x30 : 0x31);
            e1[8 * i + 4] = (byte)(((data[i] & 0x10) == 0) ? 0x30 : 0x31);
            e1[8 * i + 5] = (byte)(((data[i] & 0x08) == 0) ? 0x30 : 0x31);
            e1[8 * i + 6] = (byte)(((data[i] & 0x04) == 0) ? 0x30 : 0x31);
            e1[8 * i + 7] = (byte)(((data[i] & 0x02) == 0) ? 0x30 : 0x31);
            e1[8 * i + 8] = (byte)(((data[i] & 0x01) == 0) ? 0x30 : 0x31);
        }

        for (int i = 0; i < 8; ++i)
        {
            e2[8 * i + 1] = (byte)(((key[i] & 0x80) == 0) ? 0x30 : 0x31);
            e2[8 * i + 2] = (byte)(((key[i] & 0x40) == 0) ? 0x30 : 0x31);
            e2[8 * i + 3] = (byte)(((key[i] & 0x20) == 0) ? 0x30 : 0x31);
            e2[8 * i + 4] = (byte)(((key[i] & 0x10) == 0) ? 0x30 : 0x31);
            e2[8 * i + 5] = (byte)(((key[i] & 0x08) == 0) ? 0x30 : 0x31);
            e2[8 * i + 6] = (byte)(((key[i] & 0x04) == 0) ? 0x30 : 0x31);
            e2[8 * i + 7] = (byte)(((key[i] & 0x02) == 0) ? 0x30 : 0x31);
            e2[8 * i + 8] = (byte)(((key[i] & 0x01) == 0) ? 0x30 : 0x31);
        }
        if (PASSWORD_TRACE)
        {
            Trace.log(Trace.DIAGNOSTIC, "In enc_des, e1: ", e1);
            Trace.log(Trace.DIAGNOSTIC, "In enc_des, e2: ", e2);
        }

        // encryption method
        byte[] preout = new byte[65];  // preoutput block

        // generate keys 1 - 16

        // temp key gen workspace
        byte[] Cn = new byte[58];
        // create Cn from the original key
        for (int n = 1; n <= 56; n++)
        {
            Cn[n] = e2[PC1[n-1]];
        }

        // rotate Cn to form C1 (still called Cn...)
        lshift1(Cn);

        byte[] key1 = new byte[49];                         // 48 bit key 1 to key 16
        // now Cn[] contains 56 bits for input to PC2 to generate key1
        for (int n = 1; n <= 48; n++)
        {
            key1[n] = Cn[PC2[n-1]];
        }

        byte[] key2 = new byte[49];
        // now derive C2 from C1 (which is called Cn)
        lshift1(Cn);
        for (int n = 1; n <= 48; n++)
        {
            key2[n] = Cn[PC2[n-1]];
        }

        byte[] key3 = new byte[49];
        // now derive C3 from C2 by left shifting twice
        lshift2(Cn);
        for (int n = 1; n <= 48; n++)
        {
            key3[n] = Cn[PC2[n-1]];
        }

        byte[] key4 = new byte[49];
        // now derive C4 from C3 by again left shifting twice
        lshift2(Cn);
        for (int n = 1; n <= 48; n++)
        {
            key4[n] = Cn[PC2[n-1]];
        }

        byte[] key5 = new byte[49];
        // now derive C5 from C4 by again left shifting twice
        lshift2(Cn);
        for (int n = 1; n <= 48; n++)
        {
            key5[n] = Cn[PC2[n-1]];
        }

        byte[] key6 = new byte[49];
        // now derive C6 from C5 by again left shifting twice
        lshift2(Cn);
        for (int n = 1; n <= 48; n++)
        {
            key6[n] = Cn[PC2[n-1]];
        }

        byte[] key7 = new byte[49];
        // now derive C7 from C6 by again left shifting twice
        lshift2(Cn);
        for (int n = 1; n <= 48; n++)
        {
            key7[n] = Cn[PC2[n-1]];
        }

        byte[] key8 = new byte[49];
        // now derive C8 from C7 by again left shifting twice
        lshift2(Cn);
        for (int n = 1; n <= 48; n++)
        {
            key8[n] = Cn[PC2[n-1]];
        }

        byte[] key9 = new byte[49];
        // now derive C9 from C8 by shifting left once
        lshift1(Cn);
        for (int n = 1; n <= 48; n++)
        {
            key9[n] = Cn[PC2[n-1]];
        }

        byte[] key10 = new byte[49];
        // now derive C10 from C9 by again left shifting twice
        lshift2(Cn);
        for (int n = 1; n <= 48; n++)
        {
            key10[n] = Cn[PC2[n-1]];
        }

        byte[] key11 = new byte[49];
        // now derive C11 from C10 by again left shifting twice
        lshift2(Cn);
        for (int n = 1; n <= 48; n++)
        {
            key11[n] = Cn[PC2[n-1]];
        }

        byte[] key12 = new byte[49];
        // now derive C12 from C11 by again left shifting twice
        lshift2(Cn);
        for (int n = 1; n <= 48; n++)
        {
            key12[n] = Cn[PC2[n-1]];
        }

        byte[] key13 = new byte[49];
        // now derive C13 from C12 by again left shifting twice
        lshift2(Cn);
        for (int n = 1; n <= 48; n++)
        {
            key13[n] = Cn[PC2[n-1]];
        }

        byte[] key14 = new byte[49];
        // now derive C14 from C13 by again left shifting twice
        lshift2(Cn);
        for (int n = 1; n <= 48; n++)
        {
            key14[n] = Cn[PC2[n-1]];
        }

        byte[] key15 = new byte[49];
        // now derive C15 from C14 by again left shifting twice
        lshift2(Cn);
        for (int n = 1; n <= 48; n++)
        {
            key15[n] = Cn[PC2[n-1]];
        }

        byte[] key16 = new byte[49];
        // now derive C16 from C15 by again left shifting once
        lshift1(Cn);
        for (int n = 1; n <= 48; n++)
        {
            key16[n] = Cn[PC2[n-1]];
        }

        // temp encryption workspace
        byte[] Ln = new byte[33];
        // ditto
        byte[] Rn = new byte[33];

        // perform the initial permutation and store the result in Ln and Rn
        for (int n = 1; n <= 32; n++)
        {
            Ln[n] = e1[INITPERM[n-1]];
            Rn[n] = e1[INITPERM[n+31]];
        }
        if (PASSWORD_TRACE)
        {
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
        if (PASSWORD_TRACE)
        {
            Trace.log(Trace.DIAGNOSTIC, "In enc_des, Ln: ", Ln);
            Trace.log(Trace.DIAGNOSTIC, "In enc_des, Rn: ", Rn);
        }

        // Ln and Rn are now at L16 and R16 - create preout[] by interposing them
        System.arraycopy(Rn, 1, preout, 1, 32);
        System.arraycopy(Ln, 1, preout, 33, 32);

        byte[] e3 = new byte[65];
        // run preout[] through outperm to get ciphertext
        for (int n = 1; n <= 64; n++)
        {
            e3[n] = preout[OUTPERM[n-1]];
        }

        byte[] enc_data = new byte[8];
        // compress back to 8 bits per byte
        for (int i = 0; i < 8; ++i)
        {
            if (e3[8 * i + 1] == 0x31) enc_data[i] |= 0x80;
            if (e3[8 * i + 2] == 0x31) enc_data[i] |= 0x40;
            if (e3[8 * i + 3] == 0x31) enc_data[i] |= 0x20;
            if (e3[8 * i + 4] == 0x31) enc_data[i] |= 0x10;
            if (e3[8 * i + 5] == 0x31) enc_data[i] |= 0x08;
            if (e3[8 * i + 6] == 0x31) enc_data[i] |= 0x04;
            if (e3[8 * i + 7] == 0x31) enc_data[i] |= 0x02;
            if (e3[8 * i + 8] == 0x31) enc_data[i] |= 0x01;
        }
        return enc_data;
    }

    private static void cipher(byte[] key, byte[] Ln, byte[] Rn)
    {
        byte[] temp1 = new byte[49];  // Rn run through E
        byte[] temp2 = new byte[49];  // temp1 XORed with key
        byte[] temp3 = new byte[33];  // temp2 run through S boxes
        byte[] fkn = new byte[33];  //  f(k,n)
        int[] si = new int[9];  // decimal input to S boxes
        int[] so = new int[9];  // decimal output from S boxes

        // generate temp1[] from Rn[]
        for (int n = 1; n <= 48; n++)
        {
            temp1[n] = Rn[EPERM[n-1]];
        }

        // XOR temp1 with key to get temp2
        for (int n = 1; n <= 48; n++)
        {
            temp2[n] = (temp1[n] != key[n]) ? (byte)0x31 : (byte)0x30;
        }

        // we need to get the explicit representation into a form for
        // processing the s boxes...
        si[1] = ((temp2[1]  == 0x31) ? 0x0020 : 0x0000) |
          ((temp2[6]  == 0x31) ? 0x0010 : 0x0000) |
          ((temp2[2]  == 0x31) ? 0x0008 : 0x0000) |
          ((temp2[3]  == 0x31) ? 0x0004 : 0x0000) |
          ((temp2[4]  == 0x31) ? 0x0002 : 0x0000) |
          ((temp2[5]  == 0x31) ? 0x0001 : 0x0000);

        si[2] = ((temp2[7]  == 0x31) ? 0x0020 : 0x0000) |
          ((temp2[12] == 0x31) ? 0x0010 : 0x0000) |
          ((temp2[8]  == 0x31) ? 0x0008 : 0x0000) |
          ((temp2[9]  == 0x31) ? 0x0004 : 0x0000) |
          ((temp2[10] == 0x31) ? 0x0002 : 0x0000) |
          ((temp2[11] == 0x31) ? 0x0001 : 0x0000);

        si[3] = ((temp2[13] == 0x31) ? 0x0020 : 0x0000) |
          ((temp2[18] == 0x31) ? 0x0010 : 0x0000) |
          ((temp2[14] == 0x31) ? 0x0008 : 0x0000) |
          ((temp2[15] == 0x31) ? 0x0004 : 0x0000) |
          ((temp2[16] == 0x31) ? 0x0002 : 0x0000) |
          ((temp2[17] == 0x31) ? 0x0001 : 0x0000);

        si[4] = ((temp2[19] == 0x31) ? 0x0020 : 0x0000) |
          ((temp2[24] == 0x31) ? 0x0010 : 0x0000) |
          ((temp2[20] == 0x31) ? 0x0008 : 0x0000) |
          ((temp2[21] == 0x31) ? 0x0004 : 0x0000) |
          ((temp2[22] == 0x31) ? 0x0002 : 0x0000) |
          ((temp2[23] == 0x31) ? 0x0001 : 0x0000);

        si[5] = ((temp2[25] == 0x31) ? 0x0020 : 0x0000) |
          ((temp2[30] == 0x31) ? 0x0010 : 0x0000) |
          ((temp2[26] == 0x31) ? 0x0008 : 0x0000) |
          ((temp2[27] == 0x31) ? 0x0004 : 0x0000) |
          ((temp2[28] == 0x31) ? 0x0002 : 0x0000) |
          ((temp2[29] == 0x31) ? 0x0001 : 0x0000);

        si[6] = ((temp2[31] == 0x31) ? 0x0020 : 0x0000) |
          ((temp2[36] == 0x31) ? 0x0010 : 0x0000) |
          ((temp2[32] == 0x31) ? 0x0008 : 0x0000) |
          ((temp2[33] == 0x31) ? 0x0004 : 0x0000) |
          ((temp2[34] == 0x31) ? 0x0002 : 0x0000) |
          ((temp2[35] == 0x31) ? 0x0001 : 0x0000);

        si[7] = ((temp2[37] == 0x31) ? 0x0020 : 0x0000) |
          ((temp2[42] == 0x31) ? 0x0010 : 0x0000) |
          ((temp2[38] == 0x31) ? 0x0008 : 0x0000) |
          ((temp2[39] == 0x31) ? 0x0004 : 0x0000) |
          ((temp2[40] == 0x31) ? 0x0002 : 0x0000) |
          ((temp2[41] == 0x31) ? 0x0001 : 0x0000);

        si[8] = ((temp2[43] == 0x31) ? 0x0020 : 0x0000) |
          ((temp2[48] == 0x31) ? 0x0010 : 0x0000) |
          ((temp2[44] == 0x31) ? 0x0008 : 0x0000) |
          ((temp2[45] == 0x31) ? 0x0004 : 0x0000) |
          ((temp2[46] == 0x31) ? 0x0002 : 0x0000) |
          ((temp2[47] == 0x31) ? 0x0001 : 0x0000);

        // Now for the S boxes
        so[1] = S1[si[1]];
        so[2] = S2[si[2]];
        so[3] = S3[si[3]];
        so[4] = S4[si[4]];
        so[5] = S5[si[5]];
        so[6] = S6[si[6]];
        so[7] = S7[si[7]];
        so[8] = S8[si[8]];

        // That wasn't too bad.  Now to convert decimal to char hex again so[1-8] must be translated to 32 bits and stored in temp3[1-32]
        dectobin(so[1], temp3,  1);
        dectobin(so[2], temp3,  5);
        dectobin(so[3], temp3,  9);
        dectobin(so[4], temp3, 13);
        dectobin(so[5], temp3, 17);
        dectobin(so[6], temp3, 21);
        dectobin(so[7], temp3, 25);
        dectobin(so[8], temp3, 29);

        // Okay. Now temp3[] contains the data to run through P
        for (int n = 1; n <= 32; n++)
        {
            fkn[n] = temp3[PPERM[n-1]];
        }

        // now complete the cipher function to update Ln and Rn
        byte[] temp = new byte[33];  // storage for Ln during cipher function
        System.arraycopy(Rn, 1, temp, 1, 32);
        for (int n = 1; n <= 32; n++)
        {
            Rn[n] = (Ln[n] == fkn[n]) ? (byte)0x30:(byte)0x31;
        }
        System.arraycopy(temp, 1, Ln, 1, 32);
    }

    // Start of decimal to binary routine
    //****************************************************************************
    // convert decimal number to four ones and zeros in store
    // them in the input string
    private static void dectobin(int value, byte[] string, int offset)
    {
        string[offset]   = (byte)(((value & 0x0008) != 0) ? 0x31 : 0x30);
        string[offset+1] = (byte)(((value & 0x0004) != 0) ? 0x31 : 0x30);
        string[offset+2] = (byte)(((value & 0x0002) != 0) ? 0x31 : 0x30);
        string[offset+3] = (byte)(((value & 0x0001) != 0) ? 0x31 : 0x30);
    }

    private static void lshift1(byte[] Cn)
    {
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

    private static void lshift2(byte[] Cn)
    {
        byte[] hold = new byte[4];

        hold[0] = Cn[1];  // get the four rotated bits
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
}
