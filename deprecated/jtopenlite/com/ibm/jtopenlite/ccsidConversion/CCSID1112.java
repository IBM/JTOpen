///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  CCSID1112.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.jtopenlite.ccsidConversion;

public class CCSID1112 implements SingleByteConversion{
  static CCSID1112 singleton = new CCSID1112();
  
  public static SingleByteConversion getInstance() {
    return singleton;
  }
  
  public int getCcsid() {
    return 1112;
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
    '\u0020','\u00A0','\u0161','\u00E4','\u0105','\u012F','\u016B','\u00E5','\u0113','\u017E','\u00A2','\u002E','\u003C','\u0028','\u002B','\u007C',
    '\u0026','\u00E9','\u0119','\u0117','\u010D','\u0173','\u201E','\u201C','\u0123','\u00DF','\u0021','\u0024','\u002A','\u0029','\u003B','\u00AC',
    '\u002D','\u002F','\u0160','\u00C4','\u0104','\u012E','\u016A','\u00C5','\u0112','\u017D','\u00A6','\u002C','\u0025','\u005F','\u003E','\u003F',
    '\u00F8','\u00C9','\u0118','\u0116','\u010C','\u0172','\u012A','\u013B','\u0122','\u0060','\u003A','\u0023','\u0040',(char)0x27,'\u003D','\u0022',
    '\u00D8','\u0061','\u0062','\u0063','\u0064','\u0065','\u0066','\u0067','\u0068','\u0069','\u00AB','\u00BB','\u0101','\u017C','\u0144','\u00B1',
    '\u00B0','\u006A','\u006B','\u006C','\u006D','\u006E','\u006F','\u0070','\u0071','\u0072','\u0156','\u0157','\u00E6','\u0137','\u00C6','\u00A4',
    '\u00B5','\u007E','\u0073','\u0074','\u0075','\u0076','\u0077','\u0078','\u0079','\u007A','\u201D','\u017A','\u0100','\u017B','\u0143','\u00AE',
    '\u005E','\u00A3','\u012B','\u00B7','\u00A9','\u00A7','\u00B6','\u00BC','\u00BD','\u00BE','\u005B','\u005D','\u0179','\u0136','\u013C','\u00D7',
    '\u007B','\u0041','\u0042','\u0043','\u0044','\u0045','\u0046','\u0047','\u0048','\u0049','\u00AD','\u014D','\u00F6','\u0146','\u00F3','\u00F5',
    '\u007D','\u004A','\u004B','\u004C','\u004D','\u004E','\u004F','\u0050','\u0051','\u0052','\u00B9','\u0107','\u00FC','\u0142','\u015B','\u2019',
    (char)0x5C,'\u00F7','\u0053','\u0054','\u0055','\u0056','\u0057','\u0058','\u0059','\u005A','\u00B2','\u014C','\u00D6','\u0145','\u00D3','\u00D5',
    '\u0030','\u0031','\u0032','\u0033','\u0034','\u0035','\u0036','\u0037','\u0038','\u0039','\u00B3','\u0106','\u00DC','\u0141','\u015A','\u009F',};


  private static byte[]  fromUnicode_ = null;  
  /* dynamically generate the inverse table */
  static { 
      fromUnicode_ = SingleByteConversionTable.generateFromUnicode(toUnicode_);
  }

}
