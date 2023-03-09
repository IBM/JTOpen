///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  CCSID1137.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2011-2012 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.jtopenlite.ccsidConversion;

public class CCSID1137 implements SingleByteConversion{
  static CCSID1137 singleton = new CCSID1137();
  
  public static SingleByteConversion getInstance() {
    return singleton;
  }
  
  public int getCcsid() {
    return 1137;
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
/* 40 */ '\u0020','\u00A0','\u0901','\u0902','\u0903','\u0905','\u0906','\u0907','\u0908','\u0909','\u090A','\u002E','\u003C','\u0028','\u002B','\u007C',
/* 50 */ '\u0026','\u090B','\u090C','\u090D','\u090E','\u090F','\u0910','\u0911','\u0912','\u0913','\u0021','\u0024','\u002A','\u0029','\u003B','\u005E',
/* 60 */ '\u002D','\u002F','\u0914','\u0915','\u0916','\u0917','\u0918','\u0919','\u091A','\u091B','\u091C','\u002C','\u0025','\u005F','\u003E','\u003F',
/* 70 */ '\u091D','\u091E','\u091F','\u0920','\u0921','\u0922','\u0923','\u0924','\u0925','\u0060','\u003A','\u0023','\u0040',(char)0x27,'\u003D','\u0022',
/* 80 */ '\u0926','\u0061','\u0062','\u0063','\u0064','\u0065','\u0066','\u0067','\u0068','\u0069','\u0927','\u0928','\u092A','\u092B','\u092C','\u092D',
/* 90 */ '\u092E','\u006A','\u006B','\u006C','\u006D','\u006E','\u006F','\u0070','\u0071','\u0072','\u092F','\u0930','\u0932','\u0933','\u0935','\u0936',
/* a0 */ '\u200C','\u007E','\u0073','\u0074','\u0075','\u0076','\u0077','\u0078','\u0079','\u007A','\u0937','\u0938','\u0939','\u005B','\u093C','\u093D',
/* b0 */ '\u093E','\u093F','\u0940','\u0941','\u0942','\u0943','\u0944','\u0945','\u0946','\u0947','\u0948','\u0949','\u094A','\u005D','\u094B','\u094C',
/* c0 */ '\u007B','\u0041','\u0042','\u0043','\u0044','\u0045','\u0046','\u0047','\u0048','\u0049','\u094D','\u0950','\u0951','\u0952','\u001A','\u001A',
/* d0 */ '\u007D','\u004A','\u004B','\u004C','\u004D','\u004E','\u004F','\u0050','\u0051','\u0052','\u0960','\u0961','\u0962','\u0963','\u0964','\u0965',
/* e0 */ (char)0x5C,'\u200D','\u0053','\u0054','\u0055','\u0056','\u0057','\u0058','\u0059','\u005A','\u0966','\u0967','\u0968','\u0969','\u096A','\u096B',
/* f0 */ '\u0030','\u0031','\u0032','\u0033','\u0034','\u0035','\u0036','\u0037','\u0038','\u0039','\u096C','\u096D','\u096E','\u096F','\u0970','\u009F',};


  private static byte[]  fromUnicode_ = null;  
  /* dynamically generate the inverse table */
  static { 
      fromUnicode_ = SingleByteConversionTable.generateFromUnicode(toUnicode_);
  }

}
