///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: ConvTable4951.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

class ConvTable4951 extends ConvTableAsciiMap
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

  private static final String toUnicode_ = 
    "\u0000\u0001\u0002\u0003\u0004\u0005\u0006\u0007\b\t\n\u000B\f\r\u000E\u000F" +
    "\u0010\u0011\u0012\u0013\u00B6\u0015\u0016\u0017\u0018\u0019\u001C\u001B\u007F\u001D\u001E\u001F" +
    "\u0020\u0021\"\u0023\u0024\u0025\u0026\'\u0028\u0029\u002A\u002B\u002C\u002D\u002E\u002F" +
    "\u0030\u0031\u0032\u0033\u0034\u0035\u0036\u0037\u0038\u0039\u003A\u003B\u003C\u003D\u003E\u003F" +
    "\u0040\u0041\u0042\u0043\u0044\u0045\u0046\u0047\u0048\u0049\u004A\u004B\u004C\u004D\u004E\u004F" +
    "\u0050\u0051\u0052\u0053\u0054\u0055\u0056\u0057\u0058\u0059\u005A\u005B\\\u005D\u005E\u005F" +
    "\u0060\u0061\u0062\u0063\u0064\u0065\u0066\u0067\u0068\u0069\u006A\u006B\u006C\u006D\u006E\u006F" +
    "\u0070\u0071\u0072\u0073\u0074\u0075\u0076\u0077\u0078\u0079\u007A\u007B\u007C\u007D\u007E\u001A" +
    "\u0452\u0402\u0453\u0403\u0451\u0401\u0454\u0404\u0455\u0405\u0456\u0406\u0457\u0407\u0458\u0408" +
    "\u0459\u0409\u045A\u040A\u045B\u040B\u045C\u040C\u045E\u040E\u045F\u040F\u044E\u042E\u044A\u042A" +
    "\u0430\u0410\u0431\u0411\u0446\u0426\u0434\u0414\u0435\u0415\u0444\u0424\u0433\u0413\u001A\u001A" +
    "\u001A\u001A\u001A\u001A\u001A\u0445\u0425\u0438\u0418\u001A\u001A\u001A\u001A\u0439\u0419\u001A" +
    "\u001A\u001A\u001A\u001A\u001A\u001A\u043A\u041A\u001A\u001A\u001A\u001A\u001A\u001A\u001A\u001A" +
    "\u043B\u041B\u043C\u041C\u043D\u041D\u043E\u041E\u043F\u001A\u001A\u001A\u001A\u041F\u044F\u001A" +
    "\u042F\u0440\u0420\u0441\u0421\u0442\u0422\u0443\u0423\u0436\u0416\u0432\u0412\u044C\u042C\u2116" +
    "\u00AD\u044B\u042B\u0437\u0417\u0448\u0428\u044D\u042D\u0449\u0429\u0447\u0427\u00A7\u001A\u00A0";


  private static final String fromUnicode_ = 
    "\u0001\u0203\u0405\u0607\u0809\u0A0B\u0C0D\u0E0F\u1011\u1213\u7F15\u1617\u1819\u7F1B\u1A1D\u1E1F" +
    "\u2021\u2223\u2425\u2627\u2829\u2A2B\u2C2D\u2E2F\u3031\u3233\u3435\u3637\u3839\u3A3B\u3C3D\u3E3F" +
    "\u4041\u4243\u4445\u4647\u4849\u4A4B\u4C4D\u4E4F\u5051\u5253\u5455\u5657\u5859\u5A5B\u5C5D\u5E5F" +
    "\u6061\u6263\u6465\u6667\u6869\u6A6B\u6C6D\u6E6F\u7071\u7273\u7475\u7677\u7879\u7A7B\u7C7D\u7E1C" +
    "\uFFFF\u0010\u7F7F\uFF7F\u0000\u0004\u7F7F\u7FFD\u7F7F\uF07F\u7F7F\u7F7F\u147F\uFFFF\u01A4\u7F7F" +
    "\u7F85\u8183\u8789\u8B8D\u8F91\u9395\u977F\u999B\uA1A3\uECAD\uA7A9\uEAF4\uB8BE\uC7D1\uD3D5\uD7DD" +
    "\uE2E4\uE6E8\uABB6\uA5FC\uF6FA\u9FF2\uEEF8\u9DE0\uA0A2\uEBAC\uA6A8\uE9F3\uB7BD\uC6D0\uD2D4\uD6D8" +
    "\uE1E3\uE5E7\uAAB5\uA4FB\uF5F9\u9EF1\uEDF7\u9CDE\u7F84\u8082\u8688\u8A8C\u8E90\u9294\u967F\u989A" +
    "\uFFFF\u0E5B\u7F7F\uEF7F\uFFFF\u6F74\u7F7F";


  ConvTable4951()
  {
    super(4951, toUnicode_.toCharArray(), fromUnicode_.toCharArray());
  }
}
