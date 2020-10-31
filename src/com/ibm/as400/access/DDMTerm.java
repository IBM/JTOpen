///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: DDMTerm.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2003 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.KeyAgreement;
import javax.crypto.interfaces.DHPrivateKey;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.DHPublicKeySpec;

/**
 *DDM terms.  This class consists of defined constants for the DDM term code points.
**/
class DDMTerm
{
//@B1D  static final int ACCORD     = 0x1162; // Access order for ULDRECF
  static final int ACCSEC     = 0x106D; // Access method for exchange attributes - added by DDM server
  static final int ACCRDBRM   = 0x2201; // Access to RDB completed
  static final int ACCSECRD   = 0x14AC; // Access method reply from ACCSEC
  static final int AGENT      = 0x1403; // Agent resource manager
  static final int AGNPRMRM   = 0x1232; // Permanent agent error
  static final int ALTINDF    = 0x1423; // Alternate index file
  static final int BYTDR      = 0x0043; // Byte character
  static final int BYTSTRDR   = 0x0044; // Byte string
  static final int CHRSTRDR   = 0x0009; // Character string
  static final int CMBACCAM   = 0x1405; // Combined access access method
  static final int CMBKEYAM   = 0x1406; // Combined keyed access method
  static final int CMBRNBAM   = 0x1407; // Combined record number access method
  static final int CMDATHRM   = 0x121C; // Not authorized to command
  static final int CMDCHKRM   = 0x1254; // Command check
  static final int CMDCMPRM   = 0x124B; // Command processing complete reply
  static final int CMDNSPRM   = 0x1250; // Command not supported
  static final int CMMCTLTYP  = 0x11BA; // Commitment control type
  static final int CMMUOW     = 0x105A; // Commit
  static final int CMNAPPC    = 0x1444; // APPC communications manager - Used for pre-V4R2 connections
  static final int CMNTCPIP   = 0x1474; // TCP/IP communications manager - added by DDM server
  static final int CODPNTDR   = 0x0064; // Code point data - byte[2]
  static final int DCLFIL     = 0x102C; // Declare the file name
  static final int DCLNAM     = 0x1136; // Declared file name
  static final int DCLNAMRM   = 0x1256; // Invalid declared name
  static final int DICTIONARY = 0x1458; // Dictionary manager
  static final int DIRECTORY  = 0x1457; // Directory file
  static final int DIRFIL     = 0x140C; // Direct file
  static final int DRCAM      = 0x1419; // Directory access method
  static final int DRCNAM     = 0x1165; // Library name
  static final int DUPDCLRM   = 0x1255; // Duplicate declared file name reply
  static final int ENDUOWRM   = 0x220C; // End unit of work reply message
  static final int FILISOLVL  = 0x1472; // Commitment control lock level
  static final int FILNAM     = 0x110E; // File name
  static final int EXCSAT     = 0x1041; // Exchange server attributes
  static final int EXCSATRD   = 0x1443; // Exchange server attributes reply
  static final int EXTNAM     = 0x115E; // External name
  static final int KEYFIL     = 0x141E; // Keyed file
  static final int KEYORD     = 0x145D; // Key order
  static final int LCKMGR     = 0x1422; // Lock manager
  static final int LUWHLDCSR  = 0x11B5; // Hold cursor parameter
  static final int MGRDEPRM   = 0x1218; // Manager dependency error
  static final int MGRLVLLS   = 0x1404; // Manager level list
  static final int NAMDR      = 0x0066; // Name string
  static final int NAMSYMDR   = 0x0061; // Name string with only A-Z, 0-9 and '_'
  static final int PASSWORD   = 0x11A1; // Password for connecting
  static final int PRCCNVRM   = 0x1245; // Conversational protocol error
  static final int PRMNSPRM   = 0x1251; // Parameter not supported
  static final int RDB        = 0x240F; // Relational database
  static final int RDBACCRM   = 0x2207; // RDB currently accessed
  static final int RDBAFLRM   = 0x221A; // RDB access failed reply message
  static final int RDBATHRM   = 0x2203; // Not authorized to RDB
  static final int RDBNAM     = 0x2110; // Relational database name
  static final int RDBNFNRM   = 0x2211; // RDB not found
//@B1D  static final int RECCNT     = 0x111A; // Record count returned from ULDRECF
  static final int RECAL      = 0x1430; // Record attribute list
//@B1D  static final int RECORD     = 0x144A; // Record object returned from ULDRECF
  static final int RELKEYAM   = 0x1432; // Relative by key access method
  static final int RELRNBAM   = 0x1433; // Relative by record number access method
  static final int RLLBCKUOW  = 0x105B; // Rollback
  static final int RLSFILLK   = 0x1143; // Release file lock
  static final int RNBORD     = 0x145E; // Record number order
  static final int RNDKEYAM   = 0x1434; // Random by key access method
  static final int RNDRNBAM   = 0x1435; // Random by record number access method
  static final int RQSFILLK   = 0x1145; // Request file lock
  static final int RSCLMTRM   = 0x1233; // Resource limits reached
  static final int RSCRCVM    = 0x14A0; // @E0M
  static final int RTNINA     = 0x1155; // Return inactive records
  static final int SECCHK     = 0x106E; // Security check - added by DDM server
  static final int SECCHKCD   = 0x11A4; // Security check code - added by DDM server @B0A
  static final int SECCHKRD   = 0x1219; // Security check reply- added by DDM server
  static final int SECMEC     = 0x11A2; // Security mechanism - added by DDM server
  static final int SECMGR     = 0x1440; // Security manager
  static final int SECTKN     = 0x11DC; // Security token @B0A
  static final int SEQFIL     = 0x143B; // Sequential file
  static final int SQLAM      = 0x2407; // SQL Application manager
  static final int SRVCLSNM   = 0x1147; // Server class name
  static final int SRVDGN     = 0x1153; // Server diagnostic information
  static final int STRAM      = 0x1463; // Stream access method
  static final int STRFIL     = 0x1465; // Stream file
  static final int STRCMMCTL  = 0x105C; // Start commitment control
  static final int SUPERVISOR = 0x143C; // Supervisor
  static final int SVRCOD     = 0x1149; // Severity code
  static final int SYNTAXRM   = 0x124C; // Data stream syntax error
  static final int SYSCMDMGR  = 0x147F; // System command manager
  static final int S38ALCOB   = 0xD002; // Allocate object (lock object)
  static final int S38BUF     = 0xD405; // Input/Output buffer
  static final int S38CLOSE   = 0xD004; // Close file
  static final int S38CLOST   = 0xD121; // Close type
  static final int S38CMD     = 0xD006; // Submit remote command
  static final int S38CMDST   = 0xD103; // Command string
  static final int S38CTLL    = 0xD105; // Control list
  static final int S38DEL     = 0xD007; // Delete record
  static final int S38DLCOB   = 0xD008; // Deallocate object (release explicit locks)
  static final int S38FEOD    = 0xD00B; // Force end of data
  static final int S38GET     = 0xD00C; // Get record
  static final int S38GETD    = 0xD00D; // Get record at file position
  static final int S38GETK    = 0xD00E; // Get record by key
  static final int S38GETM    = 0xD00F; // Get multiple records
  static final int S38IOFB    = 0xD402; // Input/output feedback
  static final int S38LCNRD   = 0xD406; // Number of files unlocked count reply object
  static final int S38LRLS    = 0xD115; // Lock release structure
  static final int S38LRQS    = 0xD10B; // Lock request structure
  static final int S38LWAIT   = 0xD10C; // Lock wait time
  static final int S38MDATA   = 0xD10E; // Message replacement data
  static final int S38MFILE   = 0xD111; // Message file
  static final int S38MID     = 0xD112; // Message id
  static final int S38MTEXT   = 0xD116; // Message text
  static final int S38MTYPE   = 0xD117; // Message type
  static final int S38MSGRM   = 0xD201; // AS400 error message reply
  static final int S38OPEN    = 0xD011; // Open file
  static final int S38OPNFB   = 0xD404; // File open feed back area
  static final int S38OPTL    = 0xD119; // Option list
  static final int S38PUT     = 0xD012; // Put record
  static final int S38PUTM    = 0xD013; // Put multiple record
  static final int S38UFCB    = 0xD11F; // User file control block structure
  static final int S38UPDAT   = 0xD019; // Update record
  static final int SXXASPRQ   = 0xD02A; // Set ASP group
  static final int SXXPUTDR   = 0xD01C; // Insert record at file position
  static final int TRGNSPRM   = 0x125F; // Target not supported
//@B1D  static final int ULDRECF    = 0x1040; // Unload records from file
  static final int UOWDSP     = 0x2115; // Unit of work disposition
  static final int USRID      = 0x11A0; // Userid for connecting
  static final int VALNSPRM   = 0x1252; // Parameter value not supported
  
  // @U4A  New constants added 
  static final int ENCALC     = 0x1909; // Encryption algorithm
  
  static final int EUSRIDPWD  = 9;  // Encode userid and password 
  static final int USRSBSPWD =  6;  // User ID with Substitute Password
  static final int AES       =  2;  // AES encryption
  
  // Error codes from SECCHK
  static final int SECMECHVALUE_NOT_SUPPORTED = 1;
  static final int DCEINFORMATIONAL_STATUS_ISSUED = 0x02;
  static final int DCERETRYABLE_ERROR = 0x03;
  static final int DCENON_RETRYABLE_ERROR = 0x04;
  static final int GSSAPI_INFORMATIONAL_STATUS_ISSUED = 0x05;
  static final int GSSAPI_RETRYABLE_ERROR = 0x06;
  static final int GSSAPI_NON_RETRYABLE_ERROR = 0x07;
  static final int LOCALSECURITY_SERVICE_INFORMATIONAL_STATUS_ISSUED = 0x08;
  static final int LOCALSECURITY_SERVICE_RETRYABLE_ERROR= 0x09;
  static final int LOCALSECURITY_SERVICE_NON_RETRYABLE_ERROR = 0x0A;
  static final int SECTKN_MISSING_WHEN_IT_IS_REQUIRED_OR_IT_IS_INVALID = 0x0B;
  static final int PASSWORD_EXPIRED = 0x0E;
  static final int PASSWORD_INVALID = 0x0F;
  static final int PASSWORD_MISSING = 0x10;
  static final int USERID_MISSING = 0x12;
  static final int USERID_INVALID = 0x13;
  static final int USERID_REVOKED = 0x14;
  static final int NEWPASSWORD_INVALID = 0x15;
  static final int AUTHENTICATION_FAILED_BECAUSE_OF_CONNECTIVITY_RESTRICTIONS_ENFORCED_BY_THE_SECURITY_PLUG_IN = 0x16;
  static final int INVALID_GSS_API_SERVER_CREDENTIAL = 0x17;
  static final int GSS_API_SERVER_CREDENTIAL_EXPIRED_ON_THE_DATABASE_SERVER = 0x18;
  static final int CONTINUE__REQUIRE_MORE_SECURITY_CONTEXT_INFORMATION_FOR_AUTHENTICATION = 0x19;
  static final int SWITCHUSER_IS_INVALID = 0x1a;
  static final int THEENCALG_VALUE_IS_NOT_SUPPORTED_BY_THE_SERVER = 0x1b;
  
 
  // prime for DES's Diffie-Hellman
  // Note, the first 0x00 is need so when this is used with the 
  // BigInteger constructor, it does not appear as a negative number
  static final byte DESprime[] = {
      (byte) 0x00,
      (byte) 0xc6, (byte) 0x21, (byte) 0x12, (byte) 0xd7,
      (byte) 0x3e, (byte) 0xe6, (byte) 0x13, (byte) 0xf0,
      (byte) 0x94, (byte) 0x7a, (byte) 0xb3, (byte) 0x1f, 
      (byte) 0x0f, (byte) 0x68, (byte) 0x46, (byte) 0xa1,
      (byte) 0xbf, (byte) 0xf5, (byte) 0xb3, (byte) 0xa4, 
      (byte) 0xca, (byte) 0x0d, (byte) 0x60, (byte) 0xbc,
      (byte) 0x1e, (byte) 0x4c, (byte) 0x7a, (byte) 0x0d, 
      (byte) 0x8c, (byte) 0x16, (byte) 0xb3, (byte) 0xe3
  };

  static final byte DESgenerator[] = {
    (byte) 0x46, (byte) 0x90, (byte) 0xfa, (byte) 0x1f, 
    (byte) 0x7b, (byte) 0x9e, (byte) 0x1d, (byte) 0x44,
    (byte) 0x42, (byte) 0xc8, (byte) 0x6c, (byte) 0x91, 
    (byte) 0x14, (byte) 0x60, (byte) 0x3f, (byte) 0xde,
    (byte) 0xcf, (byte) 0x07, (byte) 0x1e, (byte) 0xdc, 
    (byte) 0xec, (byte) 0x5f, (byte) 0x62, (byte) 0x6e,
    (byte) 0x21, (byte) 0xe2, (byte) 0x56, (byte) 0xae, 
    (byte) 0xd9, (byte) 0xea, (byte) 0x34, (byte) 0xe4
  };

  // Prime for AES's Diffie-Hellman
  // Note: Both begin with 0x00 to allow it to be used
  // with the BigInteger constructor
  static final byte AESprime[] = {
    0x00,
    (byte)0xF2, (byte)0x4F, (byte)0x63, (byte)0x15, (byte)0x0E, (byte)0xAA, (byte)0x97, (byte)0xCC,
    (byte)0xE7, (byte)0x8F, (byte)0x57, (byte)0x10, (byte)0xC4, (byte)0x5F, (byte)0xAF, (byte)0xBE,
    (byte)0xB7, (byte)0x1C, (byte)0xF6, (byte)0xA8, (byte)0x72, (byte)0x4F, (byte)0x63, (byte)0x14,
    (byte)0x0E, (byte)0xAA, (byte)0x97, (byte)0xCC, (byte)0xE7, (byte)0x8F, (byte)0x57, (byte)0x10,
    (byte)0xC4, (byte)0x5F, (byte)0xAF, (byte)0xBE, (byte)0xB7, (byte)0x1C, (byte)0xF6, (byte)0xA8,
    (byte)0x72, (byte)0x4F, (byte)0x63, (byte)0x13, (byte)0x08, (byte)0xE3, (byte)0x2B, (byte)0x26,
    (byte)0xEA, (byte)0x15, (byte)0x94, (byte)0x88, (byte)0x9C, (byte)0xBB, (byte)0xFC, (byte)0x91,
    (byte)0xF6, (byte)0xDF, (byte)0x75, (byte)0x24, (byte)0x35, (byte)0x2E, (byte)0xF9, (byte)0x79
    };

  static final byte AESgenerator[] = {
    0x00,
    (byte)0xE8, (byte)0xCE, (byte)0x9E, (byte)0x08, (byte)0x44, (byte)0xC6, (byte)0x7A, (byte)0x00,
    (byte)0x9F, (byte)0xB7, (byte)0x84, (byte)0x3C, (byte)0xD9, (byte)0x45, (byte)0xA0, (byte)0x58,
    (byte)0x93, (byte)0x5D, (byte)0xA5, (byte)0x1B, (byte)0x02, (byte)0x8A, (byte)0x49, (byte)0xE5,
    (byte)0xA9, (byte)0x1F, (byte)0x83, (byte)0x1B, (byte)0x78, (byte)0x36, (byte)0x44, (byte)0x91,
    (byte)0xCD, (byte)0x0E, (byte)0x0A, (byte)0x8F, (byte)0x72, (byte)0x34, (byte)0x5D, (byte)0xF8,
    (byte)0x07, (byte)0x69, (byte)0x54, (byte)0x99, (byte)0x26, (byte)0xFD, (byte)0x16, (byte)0xEC,
    (byte)0xD6, (byte)0xF6, (byte)0x85, (byte)0x94, (byte)0x81, (byte)0x64, (byte)0x7C, (byte)0xA9,
    (byte)0xEF, (byte)0xB2, (byte)0xBA, (byte)0xAC, (byte)0x7B, (byte)0xC0, (byte)0x9A, (byte)0x92
    };
  
  
  
  static KeyPairGenerator desKeyPairGenerator = null; 
  
  // get the DESKeyPair from the shared prime and generator @U4A
  static KeyPair getDESKeyPair() throws NoSuchAlgorithmException, InvalidAlgorithmParameterException  {
     if (desKeyPairGenerator == null) { 
       
       try {
        desKeyPairGenerator = KeyPairGenerator.getInstance("DiffieHellman");
      } catch (NoSuchAlgorithmException e) {
        throw e; 
      } 
      BigInteger p = new BigInteger(DDMTerm.DESprime);
      BigInteger g = new BigInteger(DDMTerm.DESgenerator); 
      
      DHParameterSpec dhParameterSpec = new DHParameterSpec(p, g, 256); 
      
      try {
        desKeyPairGenerator.initialize(dhParameterSpec);
      } catch (InvalidAlgorithmParameterException e) {
        desKeyPairGenerator = null; 
        throw e; 
        
      }
      
     }
     return desKeyPairGenerator.genKeyPair();
  }
  
  /* Return the shared key.  If the public key is 32 bytes long then the algorithm is DES */
  /* @U4A*/ 
  static byte[] getSharedKey(KeyPair keyPair, byte[] publicKey) throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException {
    
    boolean isDes; 
    
    Trace.log(Trace.DIAGNOSTIC, "getSharedKey:  serverPublicKey:", publicKey);

     if (publicKey.length == 32) { 
       isDes = true; 
     } else {
       isDes = false; 
     }
    KeyAgreement keyAgreement = null;
 
    keyAgreement = KeyAgreement.getInstance("DiffieHellman");
    
    keyAgreement.init(keyPair.getPrivate());
    KeyFactory keyFactory = null; 
  
    keyFactory = KeyFactory.getInstance("DiffieHellman");
   
    BigInteger publicKeyBigInt;
    // If the number is negative, we must make it positive
    if ((publicKey[0] & 0x80)  == 0x80 ) {
      byte[] newPublicKey = new byte[publicKey.length+1]; 
      newPublicKey[0] = 0; 
      System.arraycopy(publicKey, 0, newPublicKey, 1, publicKey.length); 

      publicKey = newPublicKey; 
    }
    publicKeyBigInt = new BigInteger(1, publicKey);
    
    BigInteger p;
    BigInteger g;

    if (isDes) { 
       p = new BigInteger(DDMTerm.DESprime);
       g = new BigInteger(DDMTerm.DESgenerator); 
    } else {
      p = new BigInteger(DDMTerm.AESprime);
      g = new BigInteger(DDMTerm.AESgenerator); 
    }

    
    /* 
    DHPrivateKey privateKey = (DHPrivateKey) keyPair.getPrivate();
    

    Trace.log(Trace.DIAGNOSTIC, "getSharedKey:  clientPrivateKeyBigInt:", privateKey.getX().toString()); 
    Trace.log(Trace.DIAGNOSTIC, "getSharedKey:  serverPublicKeyBigInt: ", publicKeyBigInt.toString());
    Trace.log(Trace.DIAGNOSTIC, "getSharedKey:  p:                     ", p.toString());
    Trace.log(Trace.DIAGNOSTIC, "getSharedKey:  g:                     ", g.toString());
    Trace.log(Trace.DIAGNOSTIC, "getSharedKey:  clientPrivateKeyBigInt:", privateKey.getX().toByteArray()); 
    Trace.log(Trace.DIAGNOSTIC, "getSharedKey:  serverPublicKeyBigInt: ", publicKeyBigInt.toByteArray());
    Trace.log(Trace.DIAGNOSTIC, "getSharedKey:  p:                     ", p.toByteArray());
    Trace.log(Trace.DIAGNOSTIC, "getSharedKey:  g:                     ", g.toByteArray());
    */ 
    
    PublicKey publicKeyObject = null ;
   
    publicKeyObject = keyFactory.generatePublic(new DHPublicKeySpec(publicKeyBigInt, p, g));
    
    keyAgreement.doPhase(publicKeyObject, true);
    
    byte[] sharedKey = keyAgreement.generateSecret(); 
    Trace.log(Trace.DIAGNOSTIC, "getSharedKey:  sharedKey:", sharedKey);

    return sharedKey;
    

  }



  static KeyPairGenerator aesKeyPairGenerator = null;
  /* Get the AES key pair for the DDM prime and generator @U4A */ 
  
  static KeyPair getAESKeyPair() throws InvalidAlgorithmParameterException, NoSuchAlgorithmException  {
     if (aesKeyPairGenerator == null) { 
       
   
        aesKeyPairGenerator = KeyPairGenerator.getInstance("DiffieHellman");
     
      BigInteger p = new BigInteger(DDMTerm.AESprime);
      BigInteger g = new BigInteger(DDMTerm.AESgenerator); 
      
      DHParameterSpec dhParameterSpec = new DHParameterSpec(p, g, 512); 
      
      try {
        aesKeyPairGenerator.initialize(dhParameterSpec);
      } catch (InvalidAlgorithmParameterException e) {
        aesKeyPairGenerator = null; 
        throw e; 
      }
      
     }
     return aesKeyPairGenerator.genKeyPair();
  }


}
