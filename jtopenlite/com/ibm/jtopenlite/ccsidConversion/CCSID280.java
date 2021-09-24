///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  CCSID280.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.jtopenlite.ccsidConversion;

public class CCSID280 implements SingleByteConversion{
  static CCSID280 singleton = new CCSID280();
  
  public static SingleByteConversion getInstance() {
    return singleton;
  }
  
  public int getCcsid() {
    return 280;
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
    '\u0020','\u00A0','\u00E2','\u00E4','\u007B','\u00E1','\u00E3','\u00E5',(char)0x5C,'\u00F1','\u00B0','\u002E','\u003C','\u0028','\u002B','\u0021',
    '\u0026','\u005D','\u00EA','\u00EB','\u007D','\u00ED','\u00EE','\u00EF','\u007E','\u00DF','\u00E9','\u0024','\u002A','\u0029','\u003B','\u005E',
    '\u002D','\u002F','\u00C2','\u00C4','\u00C0','\u00C1','\u00C3','\u00C5','\u00C7','\u00D1','\u00F2','\u002C','\u0025','\u005F','\u003E','\u003F',
    '\u00F8','\u00C9','\u00CA','\u00CB','\u00C8','\u00CD','\u00CE','\u00CF','\u00CC','\u00F9','\u003A','\u00A3','\u00A7',(char)0x27,'\u003D','\u0022',
    '\u00D8','\u0061','\u0062','\u0063','\u0064','\u0065','\u0066','\u0067','\u0068','\u0069','\u00AB','\u00BB','\u00F0','\u00FD','\u00FE','\u00B1',
    '\u005B','\u006A','\u006B','\u006C','\u006D','\u006E','\u006F','\u0070','\u0071','\u0072','\u00AA','\u00BA','\u00E6','\u00B8','\u00C6','\u00A4',
    '\u00B5','\u00EC','\u0073','\u0074','\u0075','\u0076','\u0077','\u0078','\u0079','\u007A','\u00A1','\u00BF','\u00D0','\u00DD','\u00DE','\u00AE',
    '\u00A2','\u0023','\u00A5','\u00B7','\u00A9','\u0040','\u00B6','\u00BC','\u00BD','\u00BE','\u00AC','\u007C','\u00AF','\u00A8','\u00B4','\u00D7',
    '\u00E0','\u0041','\u0042','\u0043','\u0044','\u0045','\u0046','\u0047','\u0048','\u0049','\u00AD','\u00F4','\u00F6','\u00A6','\u00F3','\u00F5',
    '\u00E8','\u004A','\u004B','\u004C','\u004D','\u004E','\u004F','\u0050','\u0051','\u0052','\u00B9','\u00FB','\u00FC','\u0060','\u00FA','\u00FF',
    '\u00E7','\u00F7','\u0053','\u0054','\u0055','\u0056','\u0057','\u0058','\u0059','\u005A','\u00B2','\u00D4','\u00D6','\u00D2','\u00D3','\u00D5',
    '\u0030','\u0031','\u0032','\u0033','\u0034','\u0035','\u0036','\u0037','\u0038','\u0039','\u00B3','\u00DB','\u00DC','\u00D9','\u00DA','\u009F',};


  private static byte[]  fromUnicode_ = null;  
  /* dynamically generate the inverse table */
  static { 
      fromUnicode_ = SingleByteConversionTable.generateFromUnicode(toUnicode_);
  }

}
