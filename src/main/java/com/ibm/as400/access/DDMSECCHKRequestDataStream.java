///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  DDMSECCHKRequestDataStream.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2003 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.IOException;
import java.io.OutputStream;

//Constructs the DDM "security check" data stream request.
//
//Term = SECCHK.
class DDMSECCHKRequestDataStream extends DDMDataStream
{
    private static final String copyright = "Copyright (C) 1997-2024 International Business Machines Corporation and others.";

    DDMSECCHKRequestDataStream(byte[] userIDbytes, byte[] authenticationBytes, byte[] iasp, int authScheme, 
                               byte[] addAuthFactor, byte[] verificationID, byte[] clientIPAddr)
    {
        super(new byte[authenticationBytes.length + userIDbytes.length +
                   (iasp == null ? 0 : 22) + 
                   ((authScheme == AS400.AUTHENTICATION_SCHEME_PASSWORD 
                         || authScheme == AS400.AUTHENTICATION_SCHEME_DDM_EUSERIDPWD) ? 24 : 16) +
                   (((authScheme == AS400.AUTHENTICATION_SCHEME_PASSWORD 
                         || authScheme == AS400.AUTHENTICATION_SCHEME_DDM_EUSERIDPWD) && addAuthFactor != null) ? addAuthFactor.length + 12: 0) +
                   ((authScheme == AS400.AUTHENTICATION_SCHEME_PROFILE_TOKEN && verificationID != null) ? verificationID.length + 8: 0) +
                   ((authScheme == AS400.AUTHENTICATION_SCHEME_PROFILE_TOKEN && clientIPAddr != null) ? clientIPAddr.length + 8: 0)]);
        
        // Initialize the header: Don't continue on error, not chained, GDS id = D0,
        // type = RQSDSS, no same request correlation.
        setGDSId((byte) 0xD0);
        if ((authScheme != AS400.AUTHENTICATION_SCHEME_PASSWORD)
                && (authScheme != AS400.AUTHENTICATION_SCHEME_DDM_EUSERIDPWD))
        { 
            byte[] verficationIDBytes = (authScheme == AS400.AUTHENTICATION_SCHEME_PROFILE_TOKEN) ? verificationID : null;
            byte[] clientIPAddrBytes  = (authScheme == AS400.AUTHENTICATION_SCHEME_PROFILE_TOKEN) ? clientIPAddr : null;
            
            setLength(16 +  
                    + ((verficationIDBytes != null) ? verficationIDBytes.length + 8 : 0)
                    + ((clientIPAddrBytes != null) ? clientIPAddrBytes.length + 8 : 0));
            setIsChained(true);
            // setContinueOnError(false);
            setHasSameRequestCorrelation(true);
            setType(1); // 1 = RQSDSS
            
            set16bit(10 +
                    + ((verficationIDBytes != null) ? verficationIDBytes.length + 8 : 0)
                    + ((clientIPAddrBytes != null) ? clientIPAddrBytes.length + 8 : 0), 6); // Set total length remaining after header.
            set16bit(DDMTerm.SECCHK, 8); // Set code point for SECCHK.
            
            set16bit(6, 10); // Set LL for SECMEC term.
            set16bit(DDMTerm.SECMEC, 12); // Set code point for SECMEC.
            // Set value of SECMEC parm.
            if (authScheme == AS400.AUTHENTICATION_SCHEME_GSS_TOKEN)
                set16bit(DDMTerm.KERBEROS, 14);
            else
                set16bit(DDMTerm.EUSRIDONL, 14);

            int offset = 16;

            if (authScheme == AS400.AUTHENTICATION_SCHEME_PROFILE_TOKEN)
            {
                if (verficationIDBytes != null)
                {
                    if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Sending SECCHK request with verification ID.");

                    set16bit(8 + verficationIDBytes.length, offset); // LL
                    set16bit(DDMTerm.SXXVERID, offset + 2); // Term/Code point
                    set32bit(1208, offset + 4); // ccsid
                    System.arraycopy(verficationIDBytes, 0, data_, offset + 8, verficationIDBytes.length); // Data
                    
                    offset += 8 + verficationIDBytes.length;
                }
                
                if (clientIPAddrBytes != null)
                {
                    if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Sending SECCHK request with client IP address.");

                    set16bit(8 + clientIPAddrBytes.length, offset); // LL
                    set16bit(DDMTerm.SXXCLTIP, offset + 2); // Term/Code point
                    set32bit(1208, offset + 4); // ccsid
                    System.arraycopy(clientIPAddrBytes, 0, data_, offset + 8, clientIPAddrBytes.length); // Data
                    
                    offset += 8 + clientIPAddrBytes.length;
                }
            }
            
            set16bit(authenticationBytes.length + 10, offset); // Set LL for entire token-related data
            set16bit(0xD003, offset + 2); // Set code point

            // Request correlator included
            offset += 6;
            
            set16bit(authenticationBytes.length + 4, offset); // Set LL token
            set16bit(DDMTerm.SECTKN, offset + 2); // Set code point 
            System.arraycopy(authenticationBytes, 0, data_, offset + 4, authenticationBytes.length);
            
            offset += 4 + authenticationBytes.length;
        } 
        else
        {            
            boolean useRDB = (iasp != null);
            // setIsChained(false);
            // setContinueOnError(false);
            // setHasSameRequestCorrelation(false);
            setType(1); // 1 = RQSDSS
          
            set16bit(authenticationBytes.length + userIDbytes.length + (useRDB ? 40 : 18) + ((addAuthFactor != null && 0 < addAuthFactor.length) ? addAuthFactor.length + 12 : 0), 6); // Set LL for SECCHK term.
            set16bit(DDMTerm.SECCHK, 8); // Set code point for SECCHK.
            set16bit(6, 10); // Set LL for SECMEC term.
            set16bit(DDMTerm.SECMEC, 12); // Set code point for SECMEC.
            if (authScheme == AS400.AUTHENTICATION_SCHEME_DDM_EUSERIDPWD)
                set16bit(DDMTerm.EUSRIDPWD, 14); // Set value for SECMEC term.
            else if (authenticationBytes.length == 20 || authenticationBytes.length == 64)
                set16bit(8, 14); // Set value for SECMEC term. password level 4 as 8.
            else
                set16bit(DDMTerm.USRSBSPWD, 14); // Set value for SECMEC term. USRSBSPWD - User ID with Substitute Password
          
            if (authScheme == AS400.AUTHENTICATION_SCHEME_DDM_EUSERIDPWD)
            {  
                set16bit(userIDbytes.length + 4, 16); // Set LL for USRID term as SECTKN
                set16bit(DDMTerm.SECTKN, 18); // Set code point for USRID as SECTKN
                System.arraycopy(userIDbytes, 0, data_, 20, userIDbytes.length); // Data
    
                set16bit(authenticationBytes.length + 4, 20 + userIDbytes.length); // Set LL for PASSWORD term.
                set16bit(DDMTerm.SECTKN, 22+userIDbytes.length); // Set code point for PASSWORD.
                System.arraycopy(authenticationBytes, 0, data_, 24+userIDbytes.length, authenticationBytes.length); // Data
            }
            else
            {
                set16bit(14, 16); // Set LL for USRID term.
                set16bit(DDMTerm.USRID, 18); // Set code point for USRID.
                System.arraycopy(userIDbytes, 0, data_, 20, 10); // data - user id
    
                set16bit(authenticationBytes.length + 4, 30); // Set LL for PASSWORD term.
                set16bit(DDMTerm.PASSWORD, 32); // Set code point for PASSWORD.
                System.arraycopy(authenticationBytes, 0, data_, 34, authenticationBytes.length); // data - password
            }
            
            int offset = 24 + userIDbytes.length + authenticationBytes.length;

            if (useRDB)
            {
                if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Sending SECCHK request with RDB / IASP name.");
            
                set16bit(22, offset); // LL
                set16bit(DDMTerm.RDBNAM, offset + 2); // Term/Code point
                System.arraycopy(iasp, 0, data_, offset + 4, iasp.length); // Data
                
                offset += 22;
            }
            
            if (addAuthFactor != null)
            {
                if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Sending SECCHK request with additional authentication factor.");

                set16bit(12 + addAuthFactor.length, offset); // LL
                set16bit(DDMTerm.SXXFACTOR, offset + 2); // Term/Code point
                set16bit(0, offset + 4); // version
                set32bit(1208, offset + 6); // ccsid
                set16bit(addAuthFactor.length, offset + 10); // LL for data item
                System.arraycopy(addAuthFactor, 0, data_, offset + 12, addAuthFactor.length); // Data
                
                offset += 12 + addAuthFactor.length;
            }
        }
    }

    @Override
    void write(OutputStream out) throws IOException
    {
        if (Trace.traceOn_) Trace.log(Trace.DIAGNOSTIC, "Sending DDM SECCHK request...");
        super.write(out);
    }
}
