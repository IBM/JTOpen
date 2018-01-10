///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  CCSID4971.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.jtopenlite.ccsidConversion;

public class CCSID4971 implements SingleByteConversion{
  static CCSID4971 singleton = new CCSID4971();
  
  public static SingleByteConversion getInstance() {
    return singleton;
  }
  
  public int getCcsid() {
    return 4971;
  }
  
  public byte[] returnFromUnicode() {
    return fromUnicode_;
  }
  
  public char[] returnToUnicode() {
    return toUnicode_;
  }
  private static final char[] toUnicode_ = { 
/* 0 */ '\u0000','\u0001','\u0002','\u0003','\u009C','\u0009','\u0086','\u007F','\u0097','\u008D','\u008E','\u000B','\u000C',(char)0xD,'\u000E','\u000F',
/* 10 */ '\u0010','\u0011','\u0012','\u0013','\u009D','\u0085','\u0008','\u0087','\u0018','\u0019','\u0092','\u008F','\u001C','\u001D','\u001E','\u001F',
/* 20 */ '\u0080','\u0081','\u0082','\u0083','\u0084',(char)0xA,'\u0017','\u001B','\u0088','\u0089','\u008A','\u008B','\u008C','\u0005','\u0006','\u0007',
/* 30 */ '\u0090','\u0091','\u0016','\u0093','\u0094','\u0095','\u0096','\u0004','\u0098','\u0099','\u009A','\u009B','\u0014','\u0015','\u009E','\u001A',
/* 40 */ '\u0020','\u0391','\u0392','\u0393','\u0394','\u0395','\u0396','\u0397','\u0398','\u0399','\u005B','\u002E','\u003C','\u0028','\u002B','\u0021',
/* 50 */ '\u0026','\u039A','\u039B','\u039C','\u039D','\u039E','\u039F','\u03A0','\u03A1','\u03A3','\u005D','\u0024','\u002A','\u0029','\u003B','\u005E',
/* 60 */ '\u002D','\u002F','\u03A4','\u03A5','\u03A6','\u03A7','\u03A8','\u03A9','\u03AA','\u03AB','\u007C','\u002C','\u0025','\u005F','\u003E','\u003F',
/* 70 */ '\u00A8','\u0386','\u0388','\u0389','\u00A0','\u038A','\u038C','\u038E','\u038F','\u0060','\u003A','\u0023','\u0040',(char)0x27,'\u003D','\u0022',
/* 80 */ '\u0385','\u0061','\u0062','\u0063','\u0064','\u0065','\u0066','\u0067','\u0068','\u0069','\u03B1','\u03B2','\u03B3','\u03B4','\u03B5','\u03B6',
/* 90 */ '\u00B0','\u006A','\u006B','\u006C','\u006D','\u006E','\u006F','\u0070','\u0071','\u0072','\u03B7','\u03B8','\u03B9','\u03BA','\u03BB','\u03BC',
/* a0 */ '\u00B4','\u007E','\u0073','\u0074','\u0075','\u0076','\u0077','\u0078','\u0079','\u007A','\u03BD','\u03BE','\u03BF','\u03C0','\u03C1','\u03C3',
/* b0 */ '\u00A3','\u03AC','\u03AD','\u03AE','\u03CA','\u03AF','\u03CC','\u03CD','\u03CB','\u03CE','\u03C2','\u03C4','\u03C5','\u03C6','\u03C7','\u03C8',
/* c0 */ '\u007B','\u0041','\u0042','\u0043','\u0044','\u0045','\u0046','\u0047','\u0048','\u0049','\u00AD','\u03C9','\u0390','\u03B0','\u2018','\u2015',
/* d0 */ '\u007D','\u004A','\u004B','\u004C','\u004D','\u004E','\u004F','\u0050','\u0051','\u0052','\u00B1','\u00BD','\u001A','\u0387','\u2019','\u00A6',
/* e0 */ (char)0x5C,'\u001A','\u0053','\u0054','\u0055','\u0056','\u0057','\u0058','\u0059','\u005A','\u00B2','\u00A7','\u001A','\u001A','\u00AB','\u00AC',
/* f0 */ '\u0030','\u0031','\u0032','\u0033','\u0034','\u0035','\u0036','\u0037','\u0038','\u0039','\u00B3','\u00A9','\u20AC','\u001A','\u00BB','\u009F',};


  private static byte[]  fromUnicode_ = null;  
  /* dynamically generate the inverse table */
  static { 
      fromUnicode_ = SingleByteConversionTable.generateFromUnicode(toUnicode_);
  }

}
