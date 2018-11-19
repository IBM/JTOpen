///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  CCSID290.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.jtopenlite.ccsidConversion;

public class CCSID290 implements SingleByteConversion{
  static CCSID290 singleton = new CCSID290();
  
  public static SingleByteConversion getInstance() {
    return singleton;
  }
  
  public int getCcsid() {
    return 290;
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
/* 40 */ '\u0020','\uFF61','\uFF62','\uFF63','\uFF64','\uFF65','\uFF66','\uFF67','\uFF68','\uFF69','\u00A3','\u002E','\u003C','\u0028','\u002B','\u007C',
/* 50 */ '\u0026','\uFF6A','\uFF6B','\uFF6C','\uFF6D','\uFF6E','\uFF6F','\u001A','\uFF70','\u001A','\u0021','\u00A5','\u002A','\u0029','\u003B','\u00AC',
/* 60 */ '\u002D','\u002F','\u0061','\u0062','\u0063','\u0064','\u0065','\u0066','\u0067','\u0068','\u001A','\u002C','\u0025','\u005F','\u003E','\u003F',
/* 70 */ '\u005B','\u0069','\u006A','\u006B','\u006C','\u006D','\u006E','\u006F','\u0070','\u0060','\u003A','\u0023','\u0040',(char)0x27,'\u003D','\u0022',
/* 80 */ '\u005D','\uFF71','\uFF72','\uFF73','\uFF74','\uFF75','\uFF76','\uFF77','\uFF78','\uFF79','\uFF7A','\u0071','\uFF7B','\uFF7C','\uFF7D','\uFF7E',
/* 90 */ '\uFF7F','\uFF80','\uFF81','\uFF82','\uFF83','\uFF84','\uFF85','\uFF86','\uFF87','\uFF88','\uFF89','\u0072','\u001A','\uFF8A','\uFF8B','\uFF8C',
/* a0 */ '\u007E','\u203E','\uFF8D','\uFF8E','\uFF8F','\uFF90','\uFF91','\uFF92','\uFF93','\uFF94','\uFF95','\u0073','\uFF96','\uFF97','\uFF98','\uFF99',
/* b0 */ '\u005E','\u00A2',(char)0x5C,'\u0074','\u0075','\u0076','\u0077','\u0078','\u0079','\u007A','\uFF9A','\uFF9B','\uFF9C','\uFF9D','\uFF9E','\uFF9F',
/* c0 */ '\u007B','\u0041','\u0042','\u0043','\u0044','\u0045','\u0046','\u0047','\u0048','\u0049','\u001A','\u001A','\u001A','\u001A','\u001A','\u001A',
/* d0 */ '\u007D','\u004A','\u004B','\u004C','\u004D','\u004E','\u004F','\u0050','\u0051','\u0052','\u001A','\u001A','\u001A','\u001A','\u001A','\u001A',
/* e0 */ '\u0024','\u001A','\u0053','\u0054','\u0055','\u0056','\u0057','\u0058','\u0059','\u005A','\u001A','\u001A','\u001A','\u001A','\u001A','\u001A',
/* f0 */ '\u0030','\u0031','\u0032','\u0033','\u0034','\u0035','\u0036','\u0037','\u0038','\u0039','\u001A','\u001A','\u001A','\u001A','\u001A','\u009F',};


  private static byte[]  fromUnicode_ = null;  
  /* dynamically generate the inverse table */
  static { 
      fromUnicode_ = SingleByteConversionTable.generateFromUnicode(toUnicode_);
  }

}
