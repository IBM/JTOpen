///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  CCSID1132.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.jtopenlite.ccsidConversion;

public class CCSID1132 implements SingleByteConversion{
  static CCSID1132 singleton = new CCSID1132();
  
  public static SingleByteConversion getInstance() {
    return singleton;
  }
  
  public int getCcsid() {
    return 1132;
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
/* 40 */ '\u0020','\u00A0','\u0E81','\u0E82','\u0E84','\u0E87','\u0E88','\u0EAA','\u0E8A','\u005B','\u00A2','\u002E','\u003C','\u0028','\u002B','\u007C',
/* 50 */ '\u0026','\u001A','\u0E8D','\u0E94','\u0E95','\u0E96','\u0E97','\u0E99','\u0E9A','\u005D','\u0021','\u0024','\u002A','\u0029','\u003B','\u00AC',
/* 60 */ '\u002D','\u002F','\u0E9B','\u0E9C','\u0E9D','\u0E9E','\u0E9F','\u0EA1','\u0EA2','\u005E','\u00A6','\u002C','\u0025','\u005F','\u003E','\u003F',
/* 70 */ '\u006B','\u001A','\u0EA3','\u0EA5','\u0EA7','\u0EAB','\u0EAD','\u0EAE','\u001A','\u0060','\u003A','\u0023','\u0040',(char)0x27,'\u003D','\u0022',
/* 80 */ '\u001A','\u0061','\u0062','\u0063','\u0064','\u0065','\u0066','\u0067','\u0068','\u0069','\u001A','\u001A','\u0EAF','\u0EB0','\u0EB2','\u0EB3',
/* 90 */ '\u001A','\u006A','\u006B','\u006C','\u006D','\u006E','\u006F','\u0070','\u0071','\u0072','\u0EB4','\u0EB5','\u0EB6','\u0EB7','\u0EB8','\u0EB9',
/* a0 */ '\u001A','\u007E','\u0073','\u0074','\u0075','\u0076','\u0077','\u0078','\u0079','\u007A','\u0EBC','\u0EB1','\u0EBB','\u0EBD','\u001A','\u001A',
/* b0 */ '\u0ED0','\u0ED1','\u0ED2','\u0ED3','\u0ED4','\u0ED5','\u0ED6','\u0ED7','\u0ED8','\u0ED9','\u001A','\u0EC0','\u0EC1','\u0EC2','\u0EC3','\u0EC4',
/* c0 */ '\u007B','\u0041','\u0042','\u0043','\u0044','\u0045','\u0046','\u0047','\u0048','\u0049','\u001A','\u0EC8','\u0EC9','\u0ECA','\u0ECB','\u0ECC',
/* d0 */ '\u007D','\u004A','\u004B','\u004C','\u004D','\u004E','\u004F','\u0050','\u0051','\u0052','\u0ECD','\u0EC6','\u001A','\u0EDC','\u0EDD','\u001A',
/* e0 */ (char)0x5C,'\u001A','\u0053','\u0054','\u0055','\u0056','\u0057','\u0058','\u0059','\u005A','\u001A','\u001A','\u001A','\u001A','\u001A','\u001A',
/* f0 */ '\u0030','\u0031','\u0032','\u0033','\u0034','\u0035','\u0036','\u0037','\u0038','\u0039','\u001A','\u001A','\u001A','\u001A','\u001A','\u009F',};


  private static byte[]  fromUnicode_ = null;  
  /* dynamically generate the inverse table */
  static { 
      fromUnicode_ = SingleByteConversionTable.generateFromUnicode(toUnicode_);
  }

}
