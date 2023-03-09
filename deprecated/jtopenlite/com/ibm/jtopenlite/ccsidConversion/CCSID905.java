///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  CCSID905.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.jtopenlite.ccsidConversion;

public class CCSID905 implements SingleByteConversion{
  static CCSID905 singleton = new CCSID905();
  
  public static SingleByteConversion getInstance() {
    return singleton;
  }
  
  public int getCcsid() {
    return 905;
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
/* 40 */ '\u0020','\u00A0','\u00E2','\u00E4','\u00E0','\u00E1','\u001A','\u010B','\u007B','\u00F1','\u00C7','\u002E','\u003C','\u0028','\u002B','\u0021',
/* 50 */ '\u0026','\u00E9','\u00EA','\u00EB','\u00E8','\u00ED','\u00EE','\u00EF','\u00EC','\u00DF','\u011E','\u0130','\u002A','\u0029','\u003B','\u005E',
/* 60 */ '\u002D','\u002F','\u00C2','\u00C4','\u00C0','\u00C1','\u001A','\u010A','\u005B','\u00D1','\u015F','\u002C','\u0025','\u005F','\u003E','\u003F',
/* 70 */ '\u001A','\u00C9','\u00CA','\u00CB','\u00C8','\u00CD','\u00CE','\u00CF','\u00CC','\u0131','\u003A','\u00D6','\u015E',(char)0x27,'\u003D','\u00DC',
/* 80 */ '\u02D8','\u0061','\u0062','\u0063','\u0064','\u0065','\u0066','\u0067','\u0068','\u0069','\u0127','\u0109','\u015D','\u016D','\u001A','\u007C',
/* 90 */ '\u00B0','\u006A','\u006B','\u006C','\u006D','\u006E','\u006F','\u0070','\u0071','\u0072','\u0125','\u011D','\u0135','\u00B8','\u001A','\u00A4',
/* a0 */ '\u00B5','\u00F6','\u0073','\u0074','\u0075','\u0076','\u0077','\u0078','\u0079','\u007A','\u0126','\u0108','\u015C','\u016C','\u001A','\u0040',
/* b0 */ '\u02D9','\u00A3','\u017C','\u007D','\u017B','\u00A7','\u005D','\u00B7','\u00BD','\u0024','\u0124','\u011C','\u0134','\u00A8','\u00B4','\u00D7',
/* c0 */ '\u00E7','\u0041','\u0042','\u0043','\u0044','\u0045','\u0046','\u0047','\u0048','\u0049','\u00AD','\u00F4','\u007E','\u00F2','\u00F3','\u0121',
/* d0 */ '\u011F','\u004A','\u004B','\u004C','\u004D','\u004E','\u004F','\u0050','\u0051','\u0052','\u0060','\u00FB',(char)0x5C,'\u00F9','\u00FA','\u001A',
/* e0 */ '\u00FC','\u00F7','\u0053','\u0054','\u0055','\u0056','\u0057','\u0058','\u0059','\u005A','\u00B2','\u00D4','\u0023','\u00D2','\u00D3','\u0120',
/* f0 */ '\u0030','\u0031','\u0032','\u0033','\u0034','\u0035','\u0036','\u0037','\u0038','\u0039','\u00B3','\u00DB','\u0022','\u00D9','\u00DA','\u009F',};


  private static byte[]  fromUnicode_ = null;  
  /* dynamically generate the inverse table */
  static { 
      fromUnicode_ = SingleByteConversionTable.generateFromUnicode(toUnicode_);
  }

}
