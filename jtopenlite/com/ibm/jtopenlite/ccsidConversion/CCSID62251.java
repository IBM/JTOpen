///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  CCSID62251.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.jtopenlite.ccsidConversion;

public class CCSID62251 implements SingleByteConversion{
  static CCSID62251 singleton = new CCSID62251();
  
  public static SingleByteConversion getInstance() {
    return singleton;
  }
  
  public int getCcsid() {
    return 62251;
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
/* 40 */ '\u0020','\u00A0','\u00E2','\u060C','\u00E0','\u061B','\u0640','\u061F','\u00E7','\u0621','\u0622','\u002E','\u003C','\u0028','\u002B','\u007C',
/* 50 */ '\u0026','\u00E9','\u00EA','\u00EB','\u00E8','\u0623','\u00EE','\u00EF','\u0624','\u0625','\u0021','\u0024','\u002A','\u0029','\u003B','\u005E',
/* 60 */ '\u002D','\u002F','\u00C2','\u0626','\u00C0','\u0627','\u0628','\u0629','\u00C7','\u062A','\u062B','\u002C','\u0025','\u005F','\u003E','\u003F',
/* 70 */ '\u062C','\u00C9','\u00CA','\u00CB','\u00C8','\u062D','\u00CE','\u00CF','\u062E','\u0060','\u003A','\u0023','\u0040',(char)0x27,'\u003D','\u0022',
/* 80 */ '\u062F','\u0061','\u0062','\u0063','\u0064','\u0065','\u0066','\u0067','\u0068','\u0069','\u00AB','\u00BB','\u0630','\u0631','\u0632','\u0633',
/* 90 */ '\u0634','\u006A','\u006B','\u006C','\u006D','\u006E','\u006F','\u0070','\u0071','\u0072','\u0635','\u0636','\u00E6','\u0637','\u00C6','\u20AC',
/* a0 */ '\u00B5','\u007E','\u0073','\u0074','\u0075','\u0076','\u0077','\u0078','\u0079','\u007A','\u0638','\u0639','\u063A','\u005B','\u0641','\u0642',
/* b0 */ '\u0643','\u0644','\u0645','\u0646','\u00A9','\u00A7','\u0647','\u0152','\u0153','\u0178','\u0648','\u0649','\u064A','\u005D','\u064B','\u00D7',
/* c0 */ '\u007B','\u0041','\u0042','\u0043','\u0044','\u0045','\u0046','\u0047','\u0048','\u0049','\u00AD','\u00F4','\u064C','\u064D','\u064E','\u064F',
/* d0 */ '\u007D','\u004A','\u004B','\u004C','\u004D','\u004E','\u004F','\u0050','\u0051','\u0052','\u0650','\u00FB','\u00FC','\u00F9','\u0651','\u00FF',
/* e0 */ (char)0x5C,'\u00F7','\u0053','\u0054','\u0055','\u0056','\u0057','\u0058','\u0059','\u005A','\u0652','\u00D4','\u200C','\u200D','\u200E','\u200F',
/* f0 */ '\u0030','\u0031','\u0032','\u0033','\u0034','\u0035','\u0036','\u0037','\u0038','\u0039','\u001A','\u00DB','\u00DC','\u00D9','\u00A4','\u009F',};


  private static byte[]  fromUnicode_ = null;  
  /* dynamically generate the inverse table */
  static { 
      fromUnicode_ = SingleByteConversionTable.generateFromUnicode(toUnicode_);
  }

}
