///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  CCSID420.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.jtopenlite.ccsidConversion;

public class CCSID420 implements SingleByteConversion{
  static CCSID420 singleton = new CCSID420();
  
  public static SingleByteConversion getInstance() {
    return singleton;
  }
  
  public int getCcsid() {
    return 420;
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
    '\u0020','\u00A0','\u0651','\uFE7D','\u0640','\u200B','\u0621','\u0622','\uFE82','\u0623','\u00A2','\u002E','\u003C','\u0028','\u002B','\u007C',
    '\u0026','\uFE84','\u0624','\u001A','\u001A','\u0626','\u0627','\uFE8E','\u0628','\uFE91','\u0021','\u0024','\u002A','\u0029','\u003B','\u00AC',
    '\u002D','\u002F','\u0629','\u062A','\uFE97','\u062B','\uFE9B','\u062C','\uFE9F','\u062D','\u00A6','\u002C','\u0025','\u005F','\u003E','\u003F',
    '\uFEA3','\u062E','\uFEA7','\u062F','\u0630','\u0631','\u0632','\u0633','\uFEB3','\u060C','\u003A','\u0023','\u0040',(char)0x27,'\u003D','\u0022',
    '\u0634','\u0061','\u0062','\u0063','\u0064','\u0065','\u0066','\u0067','\u0068','\u0069','\uFEB7','\u0635','\uFEBB','\u0636','\uFEBF','\u0637',
    '\u0638','\u006A','\u006B','\u006C','\u006D','\u006E','\u006F','\u0070','\u0071','\u0072','\u0639','\uFECA','\uFECB','\uFECC','\u063A','\uFECE',
    '\uFECF','\u00F7','\u0073','\u0074','\u0075','\u0076','\u0077','\u0078','\u0079','\u007A','\uFED0','\u0641','\uFED3','\u0642','\uFED7','\u0643',
    '\uFEDB','\u0644','\uFEF5','\uFEF6','\uFEF7','\uFEF8','\u001A','\u001A','\uFEFB','\uFEFC','\uFEDF','\u0645','\uFEE3','\u0646','\uFEE7','\u0647',
    '\u061B','\u0041','\u0042','\u0043','\u0044','\u0045','\u0046','\u0047','\u0048','\u0049','\u00AD','\uFEEB','\u001A','\uFEEC','\u001A','\u0648',
    '\u061F','\u004A','\u004B','\u004C','\u004D','\u004E','\u004F','\u0050','\u0051','\u0052','\u0649','\uFEF0','\u064A','\uFEF2','\uFEF3','\u0660',
    '\u00D7','\u001A','\u0053','\u0054','\u0055','\u0056','\u0057','\u0058','\u0059','\u005A','\u0661','\u0662','\u001A','\u0663','\u0664','\u0665',
    '\u0030','\u0031','\u0032','\u0033','\u0034','\u0035','\u0036','\u0037','\u0038','\u0039','\u20AC','\u0666','\u0667','\u0668','\u0669','\u009F',};


  private static byte[]  fromUnicode_ = null;  
  /* dynamically generate the inverse table */
  static { 
      fromUnicode_ = SingleByteConversionTable.generateFromUnicode(toUnicode_);
  }

}
