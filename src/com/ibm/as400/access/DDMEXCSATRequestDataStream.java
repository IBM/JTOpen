///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: DDMEXCSATRequestDataStream.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

// Constructs the exchange attributes data stream request.
//
// Exchange attributes request:
// Term = EXCSAT
// Parms = SRVCLSNM -> CHRSTRDR => Character string containing server class name in EBCDIC
// Size = 6 --> Header (0 - 5)
//        2 --> LL Length of EXCSAT term and parms (6,7)
//        2 --> CP EXCSAT code point (8,9)
//        2 --> LL Length of SRVCLSNM term and parm (10,11)
//        2 --> CP SRVCLSNM code point (12, 13)
//        2 --> LL Length of CHRSTRDR term and parms (14,15)
//        2 --> CP CHRSTRDR code point (16,17)
//        3 --> SRVCLSNM parm (QAS) (18 - 20)
//        2 --> LL length of MGRLVLLS term and parms
//        2 --> MGRLVLLS code point
//   4 * 23 --> Manager level parms and their values
//       ----
//       117 --> Total length of the data stream
class DDMEXCSATRequestDataStream extends DDMDataStream
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    DDMEXCSATRequestDataStream()
    {
	super(117);
        // Initialize the header:
        //  Don't continue on error, not chained, GDS id = D0, type = RQSDSS,
        //  no same request correlation.
	setContinueOnError(false);
	setIsChained(false);
	setGDSId((byte)0xD0);
	setHasSameRequestCorrelation(false);
	setType(1);

	set16bit(111, 6); // Set total length remaining after header

	set16bit(DDMTerm.EXCSAT, 8); // Set code point for EXCSAT term
	set16bit(11, 10);            // Set length of SRVCLSNM parm
	set16bit(DDMTerm.SRVCLSNM, 12); // Set code point for SRVCLSNM parm
	set16bit(7, 14);                // Set length of CHRSTRDR parm
	set16bit(DDMTerm.CHRSTRDR, 16); // Set code point for CHRSTRDR parm
        // Set the server class name (SRVCLSNM parm)
	data_[18] = (byte)0xD8; data_[19] = (byte)0xC1; data_[20] = (byte)0xE2; // EBCDIC "QAS"
        // Set the MGRLVLS values.  Each parameter for the MGRLVLS term is appended to to the array as code point/value pairs.  No length bytes precede the parameters.
	set16bit(96, 21);
	set16bit(DDMTerm.MGRLVLLS, 23);
	set16bit(DDMTerm.AGENT, 25);
	set16bit(3, 27);
	set16bit(DDMTerm.ALTINDF, 29);
	set16bit(3, 31);
	set16bit(DDMTerm.CMBACCAM, 33);
	set16bit(3, 35);
	set16bit(DDMTerm.CMBKEYAM, 37);
	set16bit(3, 39);
	set16bit(DDMTerm.CMBRNBAM, 41);
	set16bit(3, 43);
        // V4R2 system or later
	set16bit(DDMTerm.CMNTCPIP, 45);  // Set CMNTCPIP code point
	set16bit(5, 47);  // Set value for CMNTCPIP.  Must be 5.
	set16bit(DDMTerm.DICTIONARY, 49);
	set16bit(1, 51);
	set16bit(DDMTerm.DIRECTORY, 53);
	set16bit(3, 55);
	set16bit(DDMTerm.DIRFIL, 57);
	set16bit(3, 59);
	set16bit(DDMTerm.DRCAM, 61);
	set16bit(3, 63);
	set16bit(DDMTerm.KEYFIL, 65);
	set16bit(3, 67);
	set16bit(DDMTerm.LCKMGR, 69);
	set16bit(3, 71);
	set16bit(DDMTerm.RDB, 73);
	set16bit(3, 75);
	set16bit(DDMTerm.RELKEYAM, 77);
	set16bit(3, 79);
	set16bit(DDMTerm.RELRNBAM, 81);
	set16bit(3, 83);
	set16bit(DDMTerm.SECMGR, 85);
	set16bit(1, 87);
	set16bit(DDMTerm.SEQFIL, 89);
	set16bit(3, 91);
	set16bit(DDMTerm.SQLAM, 93);
	set16bit(3, 95);
	set16bit(DDMTerm.STRAM, 97);
	set16bit(3, 99);
	set16bit(DDMTerm.STRFIL, 101);
	set16bit(3, 103);
	set16bit(DDMTerm.SUPERVISOR, 105);
	set16bit(3, 107);
	set16bit(DDMTerm.SYSCMDMGR, 109);
	set16bit(4, 111);
	set16bit(DDMTerm.RSCRCVM, 113);
	set16bit(4, 115);
    }
}
