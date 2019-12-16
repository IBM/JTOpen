///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  CCSID918.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.jtopenlite.ccsidConversion;

public class CCSID918 implements SingleByteConversion{
  static CCSID918 singleton = new CCSID918();
  
  public static SingleByteConversion getInstance() {
    return singleton;
  }
  
  public int getCcsid() {
    return 918;
  }
  
  public byte[] returnFromUnicode() {
    return fromUnicode_;
  }
  
  public char[] returnToUnicode() {
    return toUnicode_;
  }
  private static final char[] toUnicode_ = { 
    '\u0000','\u0001','\u0002','\u0003','\u009C','\u0009','\u0086','\u007F','\u0097','\u008D','\u008E','\u000B','\u000C',(char)0xD,'\u000E','\u000F',
    '\u0010','\u0011','\u0012','\u0013','\u009D','\u0085','\u0008','\u0087','\u0018','\u0019','\u0092','\u008F','\u001C','\u001D','\u001E','\u001F',
    '\u0080','\u0081','\u0082','\u0083','\u0084',(char)0xA,'\u0017','\u001B','\u0088','\u0089','\u008A','\u008B','\u008C','\u0005','\u0006','\u0007',
    '\u0090','\u0091','\u0016','\u0093','\u0094','\u0095','\u0096','\u0004','\u0098','\u0099','\u009A','\u009B','\u0014','\u0015','\u009E','\u001A',
    '\u0020','\u00A0','\u060C','\u061B','\u061F','\uFE81','\uFE8D','\uFE8E','\uF8FB','\uFE8F','\u005B','\u002E','\u003C','\u0028','\u002B','\u0021',
    '\u0026','\uFE91','\uFB56','\uFB58','\uFE93','\uFE95','\uFE97','\uFB66','\uFB68','\uFE99','\u005D','\u0024','\u002A','\u0029','\u003B','\u005E',
    '\u002D','\u002F','\uFE9B','\uFE9D','\uFE9F','\uFB7A','\uFB7C','\uFEA1','\uFEA3','\uFEA5','\u0060','\u002C','\u0025','\u005F','\u003E','\u003F',
    '\u06F0','\u06F1','\u06F2','\u06F3','\u06F4','\u06F5','\u06F6','\u06F7','\u06F8','\u06F9','\u003A','\u0023','\u0040',(char)0x27,'\u003D','\u0022',
    '\uFEA7','\u0061','\u0062','\u0063','\u0064','\u0065','\u0066','\u0067','\u0068','\u0069','\uFEA9','\uFB88','\uFEAB','\uFEAD','\uFB8C','\uFEAF',
    '\uFB8A','\u006A','\u006B','\u006C','\u006D','\u006E','\u006F','\u0070','\u0071','\u0072','\uFEB1','\uFEB3','\uFEB5','\uFEB7','\uFEB9','\uFEBB',
    '\uFEBD','\u007E','\u0073','\u0074','\u0075','\u0076','\u0077','\u0078','\u0079','\u007A','\uFEBF','\uFEC3','\uFEC7','\uFEC9','\uFECA','\uFECB',
    '\uFECC','\uFECD','\uFECE','\uFECF','\uFED0','\uFED1','\uFED3','\uFED5','\uFED7','\uFB8E','\uFEDB','\u007C','\uFB92','\uFB94','\uFEDD','\uFEDF',
    '\u007B','\u0041','\u0042','\u0043','\u0044','\u0045','\u0046','\u0047','\u0048','\u0049','\u00AD','\uFEE0','\uFEE1','\uFEE3','\uFB9E','\uFEE5',
    '\u007D','\u004A','\u004B','\u004C','\u004D','\u004E','\u004F','\u0050','\u0051','\u0052','\uFEE7','\uFE85','\uFEED','\uFBA6','\uFBA8','\uFBA9',
    (char)0x5C,'\uFBAA','\u0053','\u0054','\u0055','\u0056','\u0057','\u0058','\u0059','\u005A','\uFE80','\uFE89','\uFE8A','\uFE8B','\uFBFC','\uFBFD',
    '\u0030','\u0031','\u0032','\u0033','\u0034','\u0035','\u0036','\u0037','\u0038','\u0039','\uFBFE','\uFBB0','\uFBAE','\uFE7C','\uFE7D','\u009F',};


  private static byte[]  fromUnicode_ = null;  
  /* dynamically generate the inverse table */
  static { 
      fromUnicode_ = SingleByteConversionTable.generateFromUnicode(toUnicode_);
  }

}
