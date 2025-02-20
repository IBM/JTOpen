////////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: ProfileTokenImplRemote.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2003 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.security.auth;

import java.beans.PropertyVetoException;
import java.io.IOException;

import com.ibm.as400.access.*;

/**
 * The ProfileTokenImplRemote class provides an implementation for
 * behavior delegated by a ProfileTokenCredential object.
 *
 */
class ProfileTokenImplRemote extends AS400CredentialImplRemote implements ProfileTokenImpl
{
    @Override
    public void destroy() throws DestroyFailedException {
	    removeFromSystem();
	    super.destroy();
    }
      
    private byte[] generateRawToken(String uid, int pwdSpecialValue, 
            int authenticationIndicator, int type, int timeoutInterval, ProfileTokenEnhancedInfo enhancedInfo) throws RetrieveFailedException
    {
        // Convert password special value from enumerated int to String
        String pwd;
        switch(pwdSpecialValue) {
            case ProfileTokenCredential.PW_NOPWD:
                pwd = ProfileTokenImpl.PW_STR_NOPWD;
                break;
            case ProfileTokenCredential.PW_NOPWDCHK:
                pwd = ProfileTokenImpl.PW_STR_NOPWDCHK;
                break;
            default:
                Trace.log(Trace.ERROR, "Password special value = " +  pwdSpecialValue + " is not valid.");
                throw new ExtendedIllegalArgumentException("Password special value", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }

        // Only use SystemProgramCall code with password special values
        // such as *NOPWD or *NOPWD. Transmission is in the clear, so
        // do not use with real passwords

        AS400 sys = getCredential().getSystem();
        
        // Determine if we are using enhanced profile tokens
        boolean useEPT = false;
        try {
            useEPT = enhancedInfo.getCreateEnhancedIfPossible() && (ProfileTokenCredential.useEnhancedProfileTokens() && sys.getVRM() > 0x00070500);
        }
        catch (AS400SecurityException|IOException e) {
            Trace.log(Trace.ERROR, "Unexpected Exception: ", e);
            throw new RetrieveFailedException();
        }
        
        // The API QSYGENPT requires all parameters to be non-null. 
        char[] additionalAuthenticationFactor = new char[] { ' ' };
        
        String verificationId = enhancedInfo.getVerificationID(); 
        boolean isVfyIDNull = (verificationId == null || verificationId.length() == 0);
        if (isVfyIDNull) verificationId = " ";

        String remoteIpAddress = enhancedInfo.getRemoteIPAddress();
        boolean isRemoteIPNull =  (remoteIpAddress == null || remoteIpAddress.length() == 0);
        if (isRemoteIPNull) remoteIpAddress = " ";

        String localIpAddress = enhancedInfo.getLocalIPAddress(); 
        boolean isLocalIPNull =  (localIpAddress == null || localIpAddress.length() == 0);
        if (isLocalIPNull) localIpAddress = " ";

        ProgramParameter[] parmlist = new ProgramParameter[useEPT ? 19 : 6];
        
        // Output: Profile token   
        parmlist[0] = new ProgramParameter(ProfileTokenCredential.TOKEN_LENGTH);

        // Input: User profile name
        parmlist[1] = new ProgramParameter(stringToByteArray(uid.toUpperCase()));
        
        // Input: User password
        try {
            parmlist[2] = new ProgramParameter(CharConverter.stringToByteArray(37, sys, pwd));
        }
        catch (java.io.UnsupportedEncodingException uee) {
            Trace.log(Trace.ERROR, "Unexpected UnsupportedEncodingException: ", uee);
            throw new RetrieveFailedException();
        }
            
        // Input: Timeout Interval
        parmlist[3] = new ProgramParameter(BinaryConverter.intToByteArray(timeoutInterval));
        
        // Input: Profile token type
        parmlist[4] = new ProgramParameter(CharConverter.stringToByteArray(sys, Integer.toString(type)));

        // Input/output: Error code. NULL.
        parmlist[5] = new ProgramParameter(BinaryConverter.intToByteArray(0));
        
        // If enhanced profile tokens supported then set parameters
        if (useEPT)
        {
            // -- Optional Parameter Group 1
            
            // Input: Length of user password. Int to byte[]. Special value is used, thus must be 10
            parmlist[6] = new ProgramParameter(BinaryConverter.intToByteArray(10));

            // Input: CCSID of user password. Int to byte[]. Special value is used, thus must be 37
            parmlist[7] = new ProgramParameter(BinaryConverter.intToByteArray(37));
            
            // -- Optional Parameter Group 2
            
            // Input: Additional authentication factor (unicode)
            parmlist[8] = new ProgramParameter(BinaryConverter.charArrayToByteArray(additionalAuthenticationFactor));

            // Input: Length of additional authentication factor
            parmlist[9] = new ProgramParameter(BinaryConverter.intToByteArray( 0 ));
            
            // Input: CCSID of additional authentication factor
            parmlist[10] = new ProgramParameter(BinaryConverter.intToByteArray(13488));

            // Input: Authentication indicator (for passwords, it is ignored)
            parmlist[11] = new ProgramParameter(BinaryConverter.intToByteArray(authenticationIndicator));

            // Input: Verification ID - must be 30 in length, blank padded
            parmlist[12] = new ProgramParameter(CharConverter.stringToByteArray(sys, (verificationId + "                              ").substring(0, 30)));
            
            // Input: Remote IP address
            parmlist[13] = new ProgramParameter(CharConverter.stringToByteArray(sys, remoteIpAddress));

            // Input: Length of remote IP address
            parmlist[14] = new ProgramParameter(BinaryConverter.intToByteArray((isRemoteIPNull) ? 0 : parmlist[13].getInputData().length));
            
            // Input: Remote port
            parmlist[15] = new ProgramParameter(BinaryConverter.intToByteArray(enhancedInfo.getRemotePort()));

            // Input: Local IP address
            parmlist[16] = new ProgramParameter(CharConverter.stringToByteArray(sys, localIpAddress));

            // Input: Length of local IP address
            parmlist[17] = new ProgramParameter(BinaryConverter.intToByteArray((isLocalIPNull) ? 0 : parmlist[16].getInputData().length));

            // Input: Local port
            parmlist[18] = new ProgramParameter(BinaryConverter.intToByteArray(enhancedInfo.getLocalPort()));
        }

        if (Trace.isTraceOn())  Trace.log(Trace.DIAGNOSTIC, "ProfileTokenImpleRemote generating raw profile token w/special value: user=" + uid);

        ProgramCall programCall = new ProgramCall(sys);

        try {
            programCall.setProgram(QSYSObjectPathName.toPath("QSYS", "QSYGENPT", "PGM"), parmlist);
            programCall.suggestThreadsafe(); // Run on-thread if possible; allows app to use disabled profile.
            if (!programCall.run())
            {
                Trace.log(Trace.ERROR, "Call to QSYGENPT failed.");
                throw new RetrieveFailedException(programCall.getMessageList());
            }
        }
        catch (java.io.IOException|java.beans.PropertyVetoException|InterruptedException e) {
            AuthenticationSystem.handleUnexpectedException(e);
        }
        catch (Exception e) {
            throw new RetrieveFailedException();
        }
        
        enhancedInfo.setEnhancedTokenCreated(useEPT); 

        if (Trace.isTraceOn())  Trace.log(Trace.DIAGNOSTIC, "Raw profile token generated: " + enhancedInfo);

        return parmlist[0].getOutputData();
    }
    
    @Override
    public ProfileTokenCredential generateProfileToken(String uid, int pwdSpecialValue, ProfileTokenCredential profileTokenCred)
            throws RetrieveFailedException, PropertyVetoException 
    {
    	ProfileTokenEnhancedInfo enhancedInfo = new ProfileTokenEnhancedInfo(profileTokenCred.getEnhancedInfo());
    	enhancedInfo.setCreateEnhancedIfPossible(true); 
        byte[] token = generateRawToken(uid, pwdSpecialValue, 
                profileTokenCred.getAuthenticationIndicator(),
                profileTokenCred.getTokenType(), 
                profileTokenCred.getTimeoutInterval(),
                enhancedInfo);
        
        try
        {
        	if (enhancedInfo.wasEnhancedTokenCreated())
        		profileTokenCred.setToken(token,enhancedInfo); 
        	else
        		profileTokenCred.setToken(token);
        	
            profileTokenCred.setTokenCreator(ProfileTokenCredential.CREATOR_NATIVE_API);
        } 
        catch (PropertyVetoException e)
        {
            try {
                removeFromSystem(getCredential().getSystem(), token);
            } catch (DestroyFailedException e1) {
                Trace.log(Trace.ERROR, "Unexpected Exception during profile token destroy: ", e);
            }
            
            throw e;
        }
        
        return profileTokenCred;
    }

   
    public byte[] generateRawTokenExtended(String uid, char[] pwd, char[] aaf, int type, int timeoutInterval, ProfileTokenEnhancedInfo enhancedInfo ) throws RetrieveFailedException {
    	ProfileTokenCredential pt = generateProfileTokenExtended(uid, pwd, aaf,   type, timeoutInterval, enhancedInfo); 
        return pt.getToken();
    }
    
    private ProfileTokenCredential generateProfileTokenExtended(String uid, char[] password, char[] additionalAuthenticationFactor,
            int type, int timeoutInterval,
            ProfileTokenEnhancedInfo enhancedInfo) throws RetrieveFailedException
    {
        // Use the AS400 object to obtain the token.
        // This will obtain the token by interacting with the IBM i 
        // system signon server and avoid transmitting a cleartext password.
        ProfileTokenCredential ptTemp = null;
        try {
        	AS400 system = getCredential().getSystem();
        	
            ptTemp = system.getProfileToken(uid, password, additionalAuthenticationFactor,
                                                                 type, timeoutInterval, 
                                                                 enhancedInfo);
        }
        catch (AS400SecurityException se) {
            throw new RetrieveFailedException(se.getReturnCode());
        }
        catch (Exception e) {
            AuthenticationSystem.handleUnexpectedException(e);
        }
        
        return ptTemp;
    }

    public ProfileTokenCredential generateTokenExtended(String uid, char[] password, 
            ProfileTokenCredential profileTokenCred) throws RetrieveFailedException, PropertyVetoException
    {
       return generateProfileTokenExtended(uid, password, null, profileTokenCred);
    }
    
    public ProfileTokenCredential generateProfileTokenExtended(String uid, char[] password, char[] additionalAuthenticationFactor, 
            ProfileTokenCredential profileTokenCred) throws RetrieveFailedException, PropertyVetoException
    {
         ProfileTokenCredential ptTemp = generateProfileTokenExtended(uid, password, 
                additionalAuthenticationFactor, 
                profileTokenCred.getTokenType(), 
                profileTokenCred.getTimeoutInterval(),
                profileTokenCred.getEnhancedInfo());
        
        try {
        	// Save the impl so that we can change the credential 
        	AS400CredentialImpl impl = profileTokenCred.impl_;
        	profileTokenCred.impl_ = null; 
            profileTokenCred.setTokenCreator(ptTemp.getTokenCreator());
            profileTokenCred.setRemoteIPAddress(ptTemp.getRemoteIPAddress());
            if (ptTemp.isEnhancedProfileToken()) {
            	profileTokenCred.setToken(ptTemp.getToken(),ptTemp.getEnhancedInfo()); 
            } else { 
            profileTokenCred.setToken(ptTemp.getToken());
            }
            profileTokenCred.impl_ = impl; 
        } 
        catch (PropertyVetoException e)
        {
            try {
                removeFromSystem(getCredential().getSystem(), ptTemp.getToken());
            } catch (DestroyFailedException e1) {
                Trace.log(Trace.ERROR, "Unexpected Exception during profile token destroy: ", e);
            }
            
            throw e;
        }
        
        return profileTokenCred;
    }

    @Override
    public int getTimeToExpiration() throws RetrieveFailedException {
	    ProgramCall programCall = new ProgramCall(getCredential().getSystem());

	    ProgramParameter[] parmlist = new ProgramParameter[3];
	    parmlist[0] = new ProgramParameter(4);
	    parmlist[1] = new ProgramParameter(new AS400ByteArray(
		    ProfileTokenCredential.TOKEN_LENGTH).toBytes(
		    ((ProfileTokenCredential)getCredential()).getToken()));
	    parmlist[2] = new ProgramParameter(new AS400Bin4().toBytes(0));

	    try {
	        programCall.setProgram(QSYSObjectPathName.toPath("QSYS", "QSYGETPT", "PGM"), parmlist);
		    programCall.suggestThreadsafe(); // Run on-thread if possible.
		    if (!programCall.run()) {
			    Trace.log(Trace.ERROR, "Call to QSYGETPT failed.");
			    throw new RetrieveFailedException();
		    }
	    }
	    catch (java.io.IOException|java.beans.PropertyVetoException|InterruptedException e) {
		    AuthenticationSystem.handleUnexpectedException(e);
	    }
	    catch (Exception e) {
		    throw new RetrieveFailedException(programCall.getMessageList());
	    }

	    return (new AS400Bin4()).toInt(parmlist[0].getOutputData());
    }

    @Override
    public byte[] refresh(int type, int timeoutInterval) throws RefreshFailedException
    {
	    ProfileTokenCredential tgt = (ProfileTokenCredential)getCredential();
	    AS400 sys = tgt.getSystem();
	    ProgramCall programCall = new ProgramCall(tgt.getSystem());
	    
        // Determine if we are using enhanced profile tokens
        boolean useEPT = false;
        try {
            useEPT = (ProfileTokenCredential.useEnhancedProfileTokens() && sys.getVRM() > 0x00070500);
        }
        catch (AS400SecurityException|IOException e) {
            Trace.log(Trace.ERROR, "Unexpected Exception: ", e);
            throw new RefreshFailedException();
        }
        
        // Parameters cannot be null!
        String verificationId = tgt.getVerificationID();
        boolean isVfyIDNull = (verificationId == null || verificationId.length() == 0);
        if (isVfyIDNull) verificationId = " ";

        String remoteIpAddress = tgt.getRemoteIPAddress();
        boolean isRemoteIPNull =  (remoteIpAddress == null || remoteIpAddress.length() == 0);
        if (isRemoteIPNull) remoteIpAddress = " ";

	    ProgramParameter[] parmlist = new ProgramParameter[useEPT ? 8 : 5];
	    
	    parmlist[0] = new ProgramParameter(ProfileTokenCredential.TOKEN_LENGTH);
	    parmlist[1] = new ProgramParameter(new AS400ByteArray(ProfileTokenCredential.TOKEN_LENGTH).toBytes(tgt.getToken()));
	    parmlist[2] = new ProgramParameter(new AS400Bin4().toBytes(timeoutInterval));
	    parmlist[3] = new ProgramParameter(new AS400Text(1, sys.getCcsid(), sys).toBytes(Integer.toString(type)));
	    parmlist[4] = new ProgramParameter(new AS400Bin4().toBytes(0));
	    
	    if (useEPT)
	    {
            // Input: Verification ID - must be 30 in length, blank padded
            parmlist[5] = new ProgramParameter(CharConverter.stringToByteArray(sys, (verificationId + "                              ").substring(0, 30)));
            
            // Input: Remote IP address
            parmlist[6] = new ProgramParameter(CharConverter.stringToByteArray(sys, remoteIpAddress));

            // Input: Length of remote IP address
            parmlist[7] = new ProgramParameter(BinaryConverter.intToByteArray((isRemoteIPNull) ? 0 : parmlist[6].getInputData().length));
	    }

	    try {
		    programCall.setProgram(QSYSObjectPathName.toPath("QSYS", "QSYGENFT", "PGM"), parmlist);
		    programCall.suggestThreadsafe(); // Run on-thread if possible.
		    if (!programCall.run()) {
			    Trace.log(Trace.ERROR, "Call to QSYGENFT failed.");
			    throw new RefreshFailedException();
		    }
	    }
	    catch (java.io.IOException|java.beans.PropertyVetoException|InterruptedException e) {
		    AuthenticationSystem.handleUnexpectedException(e);
		}
	    catch (Exception e) {
		    throw new RefreshFailedException(programCall.getMessageList());
		}
    	
	    return (byte[])new AS400ByteArray(ProfileTokenCredential.TOKEN_LENGTH).toObject(parmlist[0].getOutputData());
    }
    
    /**
    * Removes the token from the IBM i system.
    *
    * @exception DestroyFailedException
    *		If errors occur while removing the credential.
    *
    */
    void removeFromSystem() throws DestroyFailedException
    {
        ProfileTokenCredential pt = (ProfileTokenCredential)getCredential();
        removeFromSystem(pt.getSystem(), pt.getToken());
    }
    
    private static void removeFromSystem(AS400 sys, byte[] token) throws DestroyFailedException
    {
        ProgramCall programCall = new ProgramCall(sys);

        ProgramParameter[] parmlist = new ProgramParameter[3];
        parmlist[0] = new ProgramParameter(new AS400Text(10, sys.getCcsid(), sys).toBytes("*PRFTKN"));
        parmlist[1] = new ProgramParameter(new AS400Bin4().toBytes(0));
        parmlist[2] = new ProgramParameter(new AS400ByteArray(ProfileTokenCredential.TOKEN_LENGTH).toBytes(token));

        try
        {
            programCall.setProgram(QSYSObjectPathName.toPath("QSYS", "QSYRMVPT", "PGM"), parmlist);
            programCall.suggestThreadsafe(); // Run on-thread if possible.
            if (!programCall.run()) {
                Trace.log(Trace.ERROR, "Call to QSYRMVPT failed.");
                throw new DestroyFailedException();
            }
        }
        catch (java.io.IOException|java.beans.PropertyVetoException|InterruptedException e) {
            AuthenticationSystem.handleUnexpectedException(e);
        }
        catch (Exception e) {
            throw new DestroyFailedException(programCall.getMessageList());
        }
    }

    /**
     * Convert Unicode string to EBCID CCSID 37 byte array.
     * Copied from com.ibm.as400.access.SignonConverter
     */
    private static byte[] stringToByteArray(String source) throws RetrieveFailedException
    {
        char[] sourceChars = source.toCharArray();
        byte[] returnBytes = {
            (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, 
            (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40
            };
        for (int i = 0; i < sourceChars.length; ++i)
        {
            switch (sourceChars[i])
            {
                case 0x0023: returnBytes[i] = (byte)0x7B; break;  // #
                case 0x0024: returnBytes[i] = (byte)0x5B; break;  // $

                case 0x0030: returnBytes[i] = (byte)0xF0; break;  // 0
                case 0x0031: returnBytes[i] = (byte)0xF1; break;  // 1
                case 0x0032: returnBytes[i] = (byte)0xF2; break;  // 2
                case 0x0033: returnBytes[i] = (byte)0xF3; break;  // 3
                case 0x0034: returnBytes[i] = (byte)0xF4; break;  // 4
                case 0x0035: returnBytes[i] = (byte)0xF5; break;  // 5
                case 0x0036: returnBytes[i] = (byte)0xF6; break;  // 6
                case 0x0037: returnBytes[i] = (byte)0xF7; break;  // 7
                case 0x0038: returnBytes[i] = (byte)0xF8; break;  // 8
                case 0x0039: returnBytes[i] = (byte)0xF9; break;  // 9

                case 0x0040: returnBytes[i] = (byte)0x7C; break;  // @

                case 0x0041: returnBytes[i] = (byte)0xC1; break;  // A
                case 0x0042: returnBytes[i] = (byte)0xC2; break;  // B
                case 0x0043: returnBytes[i] = (byte)0xC3; break;  // C
                case 0x0044: returnBytes[i] = (byte)0xC4; break;  // D
                case 0x0045: returnBytes[i] = (byte)0xC5; break;  // E
                case 0x0046: returnBytes[i] = (byte)0xC6; break;  // F
                case 0x0047: returnBytes[i] = (byte)0xC7; break;  // G
                case 0x0048: returnBytes[i] = (byte)0xC8; break;  // H
                case 0x0049: returnBytes[i] = (byte)0xC9; break;  // I
                case 0x004A: returnBytes[i] = (byte)0xD1; break;  // J
                case 0x004B: returnBytes[i] = (byte)0xD2; break;  // K
                case 0x004C: returnBytes[i] = (byte)0xD3; break;  // L
                case 0x004D: returnBytes[i] = (byte)0xD4; break;  // M
                case 0x004E: returnBytes[i] = (byte)0xD5; break;  // N
                case 0x004F: returnBytes[i] = (byte)0xD6; break;  // O
                case 0x0050: returnBytes[i] = (byte)0xD7; break;  // P
                case 0x0051: returnBytes[i] = (byte)0xD8; break;  // Q
                case 0x0052: returnBytes[i] = (byte)0xD9; break;  // R
                case 0x0053: returnBytes[i] = (byte)0xE2; break;  // S
                case 0x0054: returnBytes[i] = (byte)0xE3; break;  // T
                case 0x0055: returnBytes[i] = (byte)0xE4; break;  // U
                case 0x0056: returnBytes[i] = (byte)0xE5; break;  // V
                case 0x0057: returnBytes[i] = (byte)0xE6; break;  // W
                case 0x0058: returnBytes[i] = (byte)0xE7; break;  // X
                case 0x0059: returnBytes[i] = (byte)0xE8; break;  // Y
                case 0x005A: returnBytes[i] = (byte)0xE9; break;  // Z

                case 0x005F: returnBytes[i] = (byte)0x6D; break;  // _

                case 0x00A3: returnBytes[i] = (byte)0x7B; break;  // Cp423, pound sterling.
                case 0x00A5: returnBytes[i] = (byte)0x5B; break;  // Cp281, yen sign.
                case 0x00A7: returnBytes[i] = (byte)0x7C; break;  // Cp273, section sign.
                case 0x00C4: returnBytes[i] = (byte)0x7B; break;  // Cp278, A with dieresis.
                case 0x00C5: returnBytes[i] = (byte)0x5B; break;  // Cp277, A with ring.
                case 0x00C6: returnBytes[i] = (byte)0x7B; break;  // Cp277, ligature AE.
                case 0x00D0: returnBytes[i] = (byte)0x7C; break;  // Cp871, D with stroke.
                case 0x00D1: returnBytes[i] = (byte)0x7B; break;  // Cp284, N with tilde.
                case 0x00D6: returnBytes[i] = (byte)0x7C; break;  // Cp278, O with dieresis.
                case 0x00D8: returnBytes[i] = (byte)0x7C; break;  // Cp277, O with stroke.
                case 0x00E0: returnBytes[i] = (byte)0x7C; break;  // Cp297, a with grave.
                case 0x0130: returnBytes[i] = (byte)0x5B; break;  // Cp905, I with over dot.
                case 0x015E: returnBytes[i] = (byte)0x7C; break;  // Cp905, S with cedilla.
                
                default: throw new RetrieveFailedException(AS400SecurityException.SIGNON_CHAR_NOT_VALID);
            }
        }
        
        return returnBytes;
    }
}
