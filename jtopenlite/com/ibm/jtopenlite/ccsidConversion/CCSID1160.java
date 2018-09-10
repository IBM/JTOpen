///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  CCSID1160.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.jtopenlite.ccsidConversion;

public class CCSID1160 implements SingleByteConversion{
  static CCSID1160 singleton = new CCSID1160();
  
  public static SingleByteConversion getInstance() {
    return singleton;
  }
  
  public int getCcsid() {
    return 1160;
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
/* 40 */ '\u0020','\u00A0','\u0E01','\u0E02','\u0E03','\u0E04','\u0E05','\u0E06','\u0E07','\u005B','\u00A2','\u002E','\u003C','\u0028','\u002B','\u007C',
/* 50 */ '\u0026','\u0E48','\u0E08','\u0E09','\u0E0A','\u0E0B','\u0E0C','\u0E0D','\u0E0E','\u005D','\u0021','\u0024','\u002A','\u0029','\u003B','\u00AC',
/* 60 */ '\u002D','\u002F','\u0E0F','\u0E10','\u0E11','\u0E12','\u0E13','\u0E14','\u0E15','\u005E','\u00A6','\u002C','\u0025','\u005F','\u003E','\u003F',
/* 70 */ '\u0E3F','\u0E4E','\u0E16','\u0E17','\u0E18','\u0E19','\u0E1A','\u0E1B','\u0E1C','\u0060','\u003A','\u0023','\u0040',(char)0x27,'\u003D','\u0022',
/* 80 */ '\u0E4F','\u0061','\u0062','\u0063','\u0064','\u0065','\u0066','\u0067','\u0068','\u0069','\u0E1D','\u0E1E','\u0E1F','\u0E20','\u0E21','\u0E22',
/* 90 */ '\u0E5A','\u006A','\u006B','\u006C','\u006D','\u006E','\u006F','\u0070','\u0071','\u0072','\u0E23','\u0E24','\u0E25','\u0E26','\u0E27','\u0E28',
/* a0 */ '\u0E5B','\u007E','\u0073','\u0074','\u0075','\u0076','\u0077','\u0078','\u0079','\u007A','\u0E29','\u0E2A','\u0E2B','\u0E2C','\u0E2D','\u0E2E',
/* b0 */ '\u0E50','\u0E51','\u0E52','\u0E53','\u0E54','\u0E55','\u0E56','\u0E57','\u0E58','\u0E59','\u0E2F','\u0E30','\u0E31','\u0E32','\u0E33','\u0E34',
/* c0 */ '\u007B','\u0041','\u0042','\u0043','\u0044','\u0045','\u0046','\u0047','\u0048','\u0049','\u0E49','\u0E35','\u0E36','\u0E37','\u0E38','\u0E39',
/* d0 */ '\u007D','\u004A','\u004B','\u004C','\u004D','\u004E','\u004F','\u0050','\u0051','\u0052','\u0E3A','\u0E40','\u0E41','\u0E42','\u0E43','\u0E44',
/* e0 */ (char)0x5C,'\u0E4A','\u0053','\u0054','\u0055','\u0056','\u0057','\u0058','\u0059','\u005A','\u0E45','\u0E46','\u0E47','\u0E48','\u0E49','\u0E4A',
/* f0 */ '\u0030','\u0031','\u0032','\u0033','\u0034','\u0035','\u0036','\u0037','\u0038','\u0039','\u0E4B','\u0E4C','\u0E4D','\u0E4B','\u20AC','\u009F',};


  private static byte[]  fromUnicode_ = null;  
  /* dynamically generate the inverse table */
  static { 
      fromUnicode_ = SingleByteConversionTable.generateFromUnicode(toUnicode_);
  }

}
