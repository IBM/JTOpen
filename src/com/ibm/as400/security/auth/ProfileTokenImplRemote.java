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

import com.ibm.as400.access.*;
import java.util.Random;

/**
 * The ProfileTokenImplRemote class provides an implementation for
 * behavior delegated by a ProfileTokenCredential object.
 *
 */
class ProfileTokenImplRemote extends AS400CredentialImplRemote 
        implements ProfileTokenImpl {

    /**
    * Destroy or clear sensitive information maintained
    * by the credential implementation.
    * 
    * <p> Subsequent requests may result in a NullPointerException.
    *
    * <p> This class will also attempt to remove the associated
    * profile token from the IBM i system.
    *
    * @exception DestroyFailedException
    *		If errors occur while destroying or clearing
    *		credential implementation data.
    *
    */
    public void destroy() throws DestroyFailedException {
	    removeFromSystem();
	    super.destroy();
    }

    /**
    * Generates and returns a new profile token based on
    * the provided information.
    *
    * @deprecated As of V5R3, replaced 
    * by {@link #generateTokenExtended(String,byte[],int,int)}.
    *
    * @param uid
    *		The name of the user profile for which the token
    *		is to be generated.
    *
    * @param pwd
    *		The user profile password. Special values are not supported.
    *
    * @param type
    *		The type of token.
    *		Possible types are defined as fields on the 
    *       ProfileTokenCredential class:
    *		  <ul>
    * 			<li>TYPE_SINGLE_USE
    * 			<li>TYPE_MULTIPLE_USE_NON_RENEWABLE
    * 			<li>TYPE_MULTIPLE_USE_RENEWABLE
    *		  </ul>
    *		<p>
    *
    * @param timeoutInterval
    *    The number of seconds to expiration.
    *
    * @return
    *		The token bytes.
    *
    * @exception RetrieveFailedException
    *		If errors occur while generating the token.
    *
    */
    public byte[] generateToken(String uid, String pwd, int type, 
            int timeoutInterval) throws RetrieveFailedException {

        AS400 sys = getCredential().getSystem();

        // Deprecated as of V5R3
        try {
            if ( sys.getVRM() >= 0x00050300 ) {
                Trace.log(Trace.ERROR, 
                        "setToken(String,String,in,int) deprecated." +
                        "Use setTokenExtended(String,String,int,int).");
                throw new ExtendedIllegalArgumentException("Method deprecated", 
                        ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
            }
        }
        catch (AS400SecurityException se) {
            throw new RetrieveFailedException(se.getReturnCode());
        }
	    catch (java.io.IOException ioe) {
		    AuthenticationSystem.handleUnexpectedException(ioe); }
        
        // Use the AS400 object to obtain the token.
        // This will obtain the token by interacting with the IBM i 
        // system signon server and avoid transmitting a cleartext password.
        byte[] tkn = null;
        try {
            tkn = sys.getProfileToken(uid, pwd, type, timeoutInterval).getToken();
        }
        catch (AS400SecurityException se) {
            throw new RetrieveFailedException(se.getReturnCode());
        }
        catch (Exception e) {
            AuthenticationSystem.handleUnexpectedException(e);
        }
        return tkn;
    }

    //$A2
    /**
    * Generates and returns a new profile token based on
    * the provided information using a password special value.
    * <p>
    * This method should be used for generating a token using a
    * password special value.
    *
    * @param uid
    *		The name of the user profile for which the token
    *		is to be generated.
    *      <p>
    *
    * @param pwdSpecialValue
    *		A password special value.
    *      Possible types are defined as fields on the 
    *      ProfileTokenCredential class:
    *		  <ul>
    * 			<li>PW_NOPWD
    * 			<li>PW_NOPWDCHK
    *		  </ul>
    *		<p>
    *
    * @param type
    *		The type of token.
    *		Possible types are defined as fields on the ProfileTokenCredential class:
    *		  <ul>
    * 			<li>TYPE_SINGLE_USE
    * 			<li>TYPE_MULTIPLE_USE_NON_RENEWABLE
    * 			<li>TYPE_MULTIPLE_USE_RENEWABLE
    *		  </ul>
    *		<p>
    *
    * @param timeoutInterval
    *      The number of seconds to expiration.
    *
    * @return
    *		The token bytes.
    *
    * @exception RetrieveFailedException
    *		If errors occur while generating the token.
    *
    */
    public byte[] generateToken(String uid, int pwdSpecialValue, int type,
            int timeoutInterval) throws RetrieveFailedException {

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
                Trace.log(Trace.ERROR, "Password special value = " + 
                        pwdSpecialValue + " is not valid.");
                throw new ExtendedIllegalArgumentException(
                    "Password special value",
                    ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }

        // Only use SystemProgramCall code with password special values
        // such as *NOPWD or *NOPWD. Transmission is in the clear, so
        // do not use with real passwords

        AS400 sys = getCredential().getSystem();

        ProgramParameter[] parmlist = new ProgramParameter[6];
        
        // Output: Profile token   
        parmlist[0] = new ProgramParameter(ProfileTokenCredential.TOKEN_LENGTH);

        // Input: User profile name
        parmlist[1] = new ProgramParameter(stringToByteArray(uid.toUpperCase()));
        
        // Input: User password
        try {
            parmlist[2] = new ProgramParameter(
                    CharConverter.stringToByteArray(37, sys, pwd));
        }
        catch (java.io.UnsupportedEncodingException uee) {
            Trace.log(Trace.ERROR, "Unexpected UnsupportedEncodingException: ",
                    uee);
            throw new RetrieveFailedException();
        }
            
        // Input: Timeout Interval
        parmlist[3] = new ProgramParameter(
                BinaryConverter.intToByteArray(timeoutInterval));
        
        // Input: Profile token type
        parmlist[4] = new ProgramParameter(
                CharConverter.stringToByteArray(sys, 
                new Integer(type).toString()));

	    // Input/output: Error code. NULL.
	    parmlist[5] = new ProgramParameter(BinaryConverter.intToByteArray(0));

	    ProgramCall programCall = new ProgramCall(sys);

	    try {
		    programCall.setProgram(
		            QSYSObjectPathName.toPath("QSYS", "QSYGENPT", "PGM"),
		            parmlist);
		    programCall.setThreadSafe(true);
		    if (!programCall.run()) {
			    Trace.log(Trace.ERROR, "Call to QSYGENPT failed.");
			    throw new RetrieveFailedException(
			        programCall.getMessageList());
		    }
	    }
        catch (java.io.IOException ioe) {
            AuthenticationSystem.handleUnexpectedException(ioe); }
        catch (java.beans.PropertyVetoException pve) {
            AuthenticationSystem.handleUnexpectedException(pve); }
        catch (InterruptedException ine) {
            AuthenticationSystem.handleUnexpectedException(ine); }
        catch (Exception e) {
            throw new RetrieveFailedException(); }

        return parmlist[0].getOutputData();
    }

    //$A2
    /**
    * Generates and returns a new profile token based on
    * the provided information using a password string
    * <p>
    * This method is used for generating a token using
    * a password string (vs a special value).
    *
    * @param uid
    *		The name of the user profile for which the token
    *		is to be generated.
    *
    * @param pwd
    *		The user profile password. 
    *       Special values are not supported by this method.
    *
    * @param type
    *		The type of token.
    *		Possible types are defined as fields on the 
    *       ProfileTokenCredential class:
    *		  <ul>
    * 			<li>TYPE_SINGLE_USE
    * 			<li>TYPE_MULTIPLE_USE_NON_RENEWABLE
    * 			<li>TYPE_MULTIPLE_USE_RENEWABLE
    *		  </ul>
    *		<p>
    *
    * @param timeoutInterval
    *    The number of seconds to expiration.
    *
    * @return
    *		The token bytes.
    *
    * @exception RetrieveFailedException
    *		If errors occur while generating the token.
    *
    */
    public byte[] generateTokenExtended(String uid, String pwd, int type,
            int timeoutInterval) throws RetrieveFailedException {

        // Use the AS400 object to obtain the token.
        // This will obtain the token by interacting with the IBM i 
        // system signon server and avoid transmitting a cleartext password.
        byte[] tkn = null;
        try {
            tkn = getCredential().getSystem().getProfileToken(uid, pwd, 
                    type, timeoutInterval).getToken();
        }
        catch (AS400SecurityException se) {
            throw new RetrieveFailedException(se.getReturnCode());
        }
        catch (Exception e) {
            AuthenticationSystem.handleUnexpectedException(e);
        }
        return tkn;
    }

    /**
    * Returns the number of seconds before the
    * credential is due to expire.
    *
    * @return
    *		The number of seconds before expiration;
    *		zero (0) if already expired.
    *
    * @exception RetrieveFailedException
    *		If errors occur while retrieving
    *		timeout information.
    *
    */
    public int getTimeToExpiration() throws RetrieveFailedException {
	    ProgramCall programCall = new ProgramCall(getCredential().getSystem());

	    ProgramParameter[] parmlist = new ProgramParameter[3];
	    parmlist[0] = new ProgramParameter(4);
	    parmlist[1] = new ProgramParameter(new AS400ByteArray(
		    ProfileTokenCredential.TOKEN_LENGTH).toBytes(
		    ((ProfileTokenCredential)getCredential()).getToken()));
	    parmlist[2] = new ProgramParameter(new AS400Bin4().toBytes(0));

	    try {
	        programCall.setProgram(QSYSObjectPathName.toPath("QSYS",
	                "QSYGETPT", "PGM"), parmlist);
		    programCall.setThreadSafe(true);  //@A1A
		    if (!programCall.run()) {
			    Trace.log(Trace.ERROR, "Call to QSYGETPT failed.");
			    throw new RetrieveFailedException();
		    }
	    }
	    catch (java.io.IOException ioe) {
		    AuthenticationSystem.handleUnexpectedException(ioe);
        }
	    catch (java.beans.PropertyVetoException pve) {
		    AuthenticationSystem.handleUnexpectedException(pve);
	    }
	    catch (InterruptedException ine) {
		    AuthenticationSystem.handleUnexpectedException(ine);
	    }
	    catch (Exception e) {
		    throw new RetrieveFailedException(programCall.getMessageList());
	    }

	    return (new AS400Bin4()).toInt(parmlist[0].getOutputData());
    }

    /**
    * Updates or extends the validity period for the credential.
    *
    * <p> Generates a new profile token based on the previously
    * established <i>token</i> with the given <i>type</i>
    * and <i>timeoutInterval</i>.
    *
    * <p> This method is provided to handle cases where it is
    * desirable to allow for a more restrictive type of token
    * or a different timeout interval when a new token is
    * generated during the refresh.
    *
    * @param type
    *		The type of token.
    *		Possible types are defined as fields on the 
    *       ProfileTokenCredential class:
    *		  <ul>
    * 			<li>TYPE_SINGLE_USE
    * 			<li>TYPE_MULTIPLE_USE_NON_RENEWABLE
    * 			<li>TYPE_MULTIPLE_USE_RENEWABLE
    *		  </ul>
    *		<p>
    *
    * @param timeoutInterval
    *		The number of seconds before expiration.
    *
    * @return
    *		The new token.
    *
    * @exception RefreshFailedException
    *		If errors occur during refresh.
    *
    */
    public byte[] refresh(int type, int timeoutInterval) 
            throws RefreshFailedException {
                
	    ProfileTokenCredential tgt = (ProfileTokenCredential)getCredential();
	    AS400 sys = tgt.getSystem();
	    ProgramCall programCall = new ProgramCall(tgt.getSystem());

	    ProgramParameter[] parmlist = new ProgramParameter[5];
	    parmlist[0] = new ProgramParameter(
	            ProfileTokenCredential.TOKEN_LENGTH);
	    parmlist[1] = new ProgramParameter(new AS400ByteArray(
	            ProfileTokenCredential.TOKEN_LENGTH).toBytes(
	            tgt.getToken()));
	    parmlist[2] = new ProgramParameter(
	            new AS400Bin4().toBytes(timeoutInterval));
	    parmlist[3] = new ProgramParameter(new AS400Text(
	            1, sys.getCcsid(), sys).toBytes(
	            new Integer(type).toString()));
	    parmlist[4] = new ProgramParameter(new AS400Bin4().toBytes(0));

	    try {
		    programCall.setProgram(QSYSObjectPathName.toPath("QSYS",
		            "QSYGENFT", "PGM"), parmlist);
		    programCall.setThreadSafe(true);  //@A1A
		    if (!programCall.run()) {
			    Trace.log(Trace.ERROR, "Call to QSYGENFT failed.");
			    throw new RefreshFailedException();
		    }
	    }
	    catch (java.io.IOException ioe) {
		    AuthenticationSystem.handleUnexpectedException(ioe);
        }
	    catch (java.beans.PropertyVetoException pve) {
		    AuthenticationSystem.handleUnexpectedException(pve);
		}
	    catch (InterruptedException ine) {
		    AuthenticationSystem.handleUnexpectedException(ine);
		}
	    catch (Exception e) {
		    throw new RefreshFailedException(programCall.getMessageList());
		}
    	
	    return (byte[])new AS400ByteArray(
		    ProfileTokenCredential.TOKEN_LENGTH).toObject(
		    parmlist[0].getOutputData());
    }

    /**
    * Removes the token from the IBM i system.
    *
    * @exception DestroyFailedException
    *		If errors occur while removing the credential.
    *
    */
    void removeFromSystem() throws DestroyFailedException {
	    ProfileTokenCredential tgt = (ProfileTokenCredential)getCredential();
	    AS400 sys = tgt.getSystem();
	    ProgramCall programCall = new ProgramCall(sys);

	    ProgramParameter[] parmlist = new ProgramParameter[3];
	    parmlist[0] = new ProgramParameter(
	        new AS400Text(10, sys.getCcsid(), sys).toBytes("*PRFTKN"));
	    parmlist[1] = new ProgramParameter(new AS400Bin4().toBytes(0));
	    parmlist[2] = new ProgramParameter(
	        new AS400ByteArray(
	        ProfileTokenCredential.TOKEN_LENGTH).toBytes(tgt.getToken()));

	    try {
		    programCall.setProgram(QSYSObjectPathName.toPath("QSYS",
		        "QSYRMVPT", "PGM"), parmlist);
		    programCall.setThreadSafe(true);  //@A1A
		    if (!programCall.run()) {
			    Trace.log(Trace.ERROR, "Call to QSYRMVPT failed.");
			    throw new DestroyFailedException();
		    }
	    }
	    catch (java.io.IOException ioe) {
		    AuthenticationSystem.handleUnexpectedException(ioe);
		}
	    catch (java.beans.PropertyVetoException pve) {
		    AuthenticationSystem.handleUnexpectedException(pve);
		}
	    catch (InterruptedException ine) {
		    AuthenticationSystem.handleUnexpectedException(ine);
		}
	    catch (Exception e) {
		    throw new DestroyFailedException(programCall.getMessageList());
	    }
    }

    //$A2
    /**
     * Convert Unicode string to EBCID CCSID 37 byte array.
     * Copied from com.ibm.as400.access.SignonConverter
     */
    private static byte[] stringToByteArray(String source) 
            throws RetrieveFailedException
    {
        char[] sourceChars = source.toCharArray();
        byte[] returnBytes = {
            (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, 
            (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40, (byte)0x40
            };
        for (int i = 0; i < sourceChars.length; ++i) {
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
                default: throw new RetrieveFailedException(
                        AS400SecurityException.SIGNON_CHAR_NOT_VALID);
            }
        }
        return returnBytes;
    }
}
