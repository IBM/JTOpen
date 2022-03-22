///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: SaveFileAttrFormat.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 2004-2010 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

/**
 Represents the record format of the data returned from the
 DSPFD (Display File Description) command when the type of data requested
 is *ATR and the "file attributes" parameter is *SAVF.
 This combination returns attributes specific to save files.

 @see AS400FileRecordDescriptionImplRemote#getSavefileAttributes
 **/
class SaveFileAttrFormat extends RecordFormat
{
  static final long serialVersionUID = 4L;

  SaveFileAttrFormat(int ccsid)
  {
    super("QWHFDSAV");

    AS400Text txt1 = new AS400Text(1, ccsid);
    AS400Text txt3 = new AS400Text(3, ccsid);
    AS400Text txt4 = new AS400Text(4, ccsid);
    AS400Text txt6 = new AS400Text(6, ccsid);
    AS400Text txt8 = new AS400Text(8, ccsid);
    AS400Text txt9 = new AS400Text(9, ccsid);
    AS400Text txt10 = new AS400Text(10, ccsid);
    AS400Text txt50 = new AS400Text(50, ccsid);
    AS400PackedDecimal p30 = new AS400PackedDecimal(3, 0);
    AS400PackedDecimal p50 = new AS400PackedDecimal(5, 0);
    AS400PackedDecimal p110 = new AS400PackedDecimal(11, 0);

    addFieldDescription(new CharacterFieldDescription(txt1, "SARCEN")); // retrieval century:  0=19XX, 1=20XX
    addFieldDescription(new CharacterFieldDescription(txt6, "SARDAT")); // retrieval date:  year/month/day
    addFieldDescription(new CharacterFieldDescription(txt6, "SARTIM")); // retrieval time:  hour/minute/second
    addFieldDescription(new CharacterFieldDescription(txt10, "SAFILE")); // file
    addFieldDescription(new CharacterFieldDescription(txt10, "SALIB")); // library
      addFieldDescription(new CharacterFieldDescription(txt1, "SAFTYP")); // type of file: D=DEVICE
    addFieldDescription(new CharacterFieldDescription(txt4, "SAFILA")); // file attribute:  *SAV
    addFieldDescription(new CharacterFieldDescription(txt3, "SAMXD")); // reserved
    addFieldDescription(new CharacterFieldDescription(txt6, "SAFATR")); // file attribute:  SAVF
    addFieldDescription(new CharacterFieldDescription(txt8, "SASYSN")); // system name (source system, if file is DDM)
    addFieldDescription(new PackedDecimalFieldDescription(p30, "SALASP")); // library auxiliary storage pool ID: 1=system ASP
    addFieldDescription(new CharacterFieldDescription(txt4, "SARES"));  // reserved
    addFieldDescription(new CharacterFieldDescription(txt1, "SADTAT")); // file type:  D=*DATA
    addFieldDescription(new PackedDecimalFieldDescription(p50, "SAWAIT")); // maximum file wait time:  -1=*IMMED, 0=*CLS
    addFieldDescription(new PackedDecimalFieldDescription(p50, "SAWATR")); // reserved
    addFieldDescription(new CharacterFieldDescription(txt1, "SASHAR")); // share open data path:  N=*NO, Y=*YES
    addFieldDescription(new CharacterFieldDescription(txt1, "SALVLC")); // reserved
    addFieldDescription(new CharacterFieldDescription(txt50, "SATXT")); // text 'description'
    addFieldDescription(new PackedDecimalFieldDescription(p50, "SANOFM")); // reserved
    addFieldDescription(new CharacterFieldDescription(txt1, "SAFCCN")); // century created:  0=19XX, 1=20XX
    addFieldDescription(new CharacterFieldDescription(txt6, "SAFCDT")); // date created:  year/month/day
    addFieldDescription(new CharacterFieldDescription(txt6, "SAFCTM")); // time created:  hour/minute/second
    addFieldDescription(new CharacterFieldDescription(txt1, "SAFLS")); // externally described file: N=NO
    addFieldDescription(new CharacterFieldDescription(txt1, "SAICAP")); // reserved
    addFieldDescription(new CharacterFieldDescription(txt9, "SARES2")); // reserved
    addFieldDescription(new CharacterFieldDescription(txt10, "SAAQDV")); // reserved
    addFieldDescription(new PackedDecimalFieldDescription(p30, "SAMXDV")); // reserved
    addFieldDescription(new CharacterFieldDescription(txt1, "SASPOL")); // reserved
    addFieldDescription(new PackedDecimalFieldDescription(p30, "SANODV")); // reserved
    addFieldDescription(new PackedDecimalFieldDescription(p50, "SAUBL")); // maximum record length
    addFieldDescription(new CharacterFieldDescription(txt1, "SAIDTA")); // reserved
    addFieldDescription(new CharacterFieldDescription(txt9, "SARES3")); // reserved
    addFieldDescription(new PackedDecimalFieldDescription(p50, "SACNRC")); // number of records.  If 99999 use field SACNR2.
    addFieldDescription(new PackedDecimalFieldDescription(p110, "SACNR2")); // current number of records
    addFieldDescription(new PackedDecimalFieldDescription(p110, "SASIZE")); // maximum records:  0=*NOMAX
    addFieldDescription(new PackedDecimalFieldDescription(p30, "SAASP")); // auxiliary storage pool ID: 1=system asp
  }
}
