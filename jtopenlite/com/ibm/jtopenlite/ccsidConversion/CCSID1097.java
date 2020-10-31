///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  CCSID1097.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.jtopenlite.ccsidConversion;

public class CCSID1097 implements SingleByteConversion{
  static CCSID1097 singleton = new CCSID1097();
  
  public static SingleByteConversion getInstance() {
    return singleton;
  }
  
  public int getCcsid() {
    return 1097;
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
    '\u0020','\u00A0','\u060C','\u064B','\uFE81','\uFE82','\uF8FA','\uFE8D','\uFE8E','\uF8FB','\u00A4','\u002E','\u003C','\u0028','\u002B','\u007C',
    '\u0026','\uFE80','\uFE83','\uFE84','\uF8F9','\uFE85','\uFE8B','\uFE8F','\uFE91','\uFB56','\u0021','\u0024','\u002A','\u0029','\u003B','\u00AC',
    '\u002D','\u002F','\uFB58','\uFE95','\uFE97','\uFE99','\uFE9B','\uFE9D','\uFE9F','\uFB7A','\u061B','\u002C','\u0025','\u005F','\u003E','\u003F',
    '\uFB7C','\uFEA1','\uFEA3','\uFEA5','\uFEA7','\uFEA9','\uFEAB','\uFEAD','\uFEAF','\u0060','\u003A','\u0023','\u0040',(char)0x27,'\u003D','\u0022',
    '\uFB8A','\u0061','\u0062','\u0063','\u0064','\u0065','\u0066','\u0067','\u0068','\u0069','\u00AB','\u00BB','\uFEB1','\uFEB3','\uFEB5','\uFEB7',
    '\uFEB9','\u006A','\u006B','\u006C','\u006D','\u006E','\u006F','\u0070','\u0071','\u0072','\uFEBB','\uFEBD','\uFEBF','\uFEC1','\uFEC3','\uFEC5',
    '\uFEC7','\u007E','\u0073','\u0074','\u0075','\u0076','\u0077','\u0078','\u0079','\u007A','\uFEC9','\uFECA','\uFECB','\uFECC','\uFECD','\uFECE',
    '\uFECF','\uFED0','\uFED1','\uFED3','\uFED5','\uFED7','\uFB8E','\uFEDB','\uFB92','\uFB94','\u005B','\u005D','\uFEDD','\uFEDF','\uFEE1','\u00D7',
    '\u007B','\u0041','\u0042','\u0043','\u0044','\u0045','\u0046','\u0047','\u0048','\u0049','\u00AD','\uFEE3','\uFEE5','\uFEE7','\uFEED','\uFEE9',
    '\u007D','\u004A','\u004B','\u004C','\u004D','\u004E','\u004F','\u0050','\u0051','\u0052','\uFEEB','\uFEEC','\uFBA4','\uFBFC','\uFBFD','\uFBFE',
    (char)0x5C,'\u061F','\u0053','\u0054','\u0055','\u0056','\u0057','\u0058','\u0059','\u005A','\u0640','\u06F0','\u06F1','\u06F2','\u06F3','\u06F4',
    '\u0030','\u0031','\u0032','\u0033','\u0034','\u0035','\u0036','\u0037','\u0038','\u0039','\u06F5','\u06F6','\u06F7','\u06F8','\u06F9','\u009F',};


  private static byte[]  fromUnicode_ = null;  
  /* dynamically generate the inverse table */
  static { 
      fromUnicode_ = SingleByteConversionTable.generateFromUnicode(toUnicode_);
  }

}
