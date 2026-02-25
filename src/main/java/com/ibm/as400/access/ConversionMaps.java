///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  ConversionMaps.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2004 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.util.Enumeration;
import java.util.Hashtable;

public abstract class ConversionMaps
{
    // To prevent rehashing of a Hashtable as it's being built, we construct it with a certain number of entries.  However, the default load factor for a table is .75, meaning that when it becomes 3/4 full, the table is automatically increased in size and rehashed.  We could specify a load factor of 1.0 on the constructor, since that would efficiently utilize memory and prevent rehashing, but we might get ineffective hashing since the algorithm might not be good, so instead, we leave the load factor at the default .75 and just use a size of hash table big enough to accommodate the number of entries we have without rehashing.  This is a pretty good tradeoff between lookup time and memory footprint.

    // If we ever pre-req Java 2, we should use HashMaps for these instead of Hashtables.  Hashtables are synchronized which makes the table lookup slow, especially if multiple threads bottleneck on these static tables.  HashMaps are not synchronized yet provide the same function.
    public final static Hashtable<String, String> ccsidEncoding_ = new Hashtable<String, String> (821); // 821 actual entries.
    public final static Hashtable<String, String> encodingCcsid_ = new Hashtable<String, String>(821); // 127 actual entries.
    public final static Hashtable<String, String> localeCcsidMap_ = new Hashtable<String, String>(120); // 87 actual entries.
    public final static Hashtable<String, String> localeNlvMap_ = new Hashtable<String, String>(100); // 74 actual entries.

    // Convenience function to get corresponding ccsid number as String.
    static final String encodingToCcsidString(String encoding)
    {
        return (String)encodingCcsid_.get(encoding);
    }

    // Convenience function to get corresponding encoding from ccsid.
    static final String ccsidToEncoding(int ccsid)
    {
        return (String)ccsidEncoding_.get(String.valueOf(ccsid));
    }

    // This is a table that maps all Java encodings to IBM i CCSIDs.  Some encodings could map to more than one CCSID, so they are not included in the table.  
    // When a lookup is performed, it will then return null.  Some encodings are supported by the IBM i but not by the Toolbox.  The ConvTable code handles this.
    // Based on http://java.sun.com/products/jdk/1.2/docs/guide/internat/encoding.doc.html
    // V5R1 JVM encodings: http://publib.boulder.ibm.com/pubs/html/as400/v5r1/ic2924/info/rzaha/fileenc.htm
    // V5R1 JVM locales: http://publib.boulder.ibm.com/pubs/html/as400/v5r1/ic2924/info/rzaha/locales.htm

    //See also http://java.sun.com/j2se/1.4/docs/guide/intl/encoding.doc.html and http://java.sun.com/j2se/1.4/docs/guide/intl/locale.doc.html

    // Updated for Java 8 -- see  https://docs.oracle.com/javase/8/docs/technotes/guides/intl/encoding.doc.html
    // The program test.Conv.GenEncodingCcsidConversionMap was used to generate the following list. 
    
    static
    {

   // entry count = 1012
      encodingCcsid_.put("cp037", "37");
      encodingCcsid_.put("Cp037", "37");
      encodingCcsid_.put("ibm037", "37");
      encodingCcsid_.put("ibm-037", "37");
      encodingCcsid_.put("csIBM037", "37");
      encodingCcsid_.put("ebcdic-cp-us", "37");
      encodingCcsid_.put("ebcdic-cp-ca", "37");
      encodingCcsid_.put("ebcdic-cp-nl", "37");
      encodingCcsid_.put("ebcdic-cp-wt", "37");
      encodingCcsid_.put("037", "37");
      encodingCcsid_.put("cpibm37", "37");
      encodingCcsid_.put("cs-ebcdic-cp-wt", "37");
      encodingCcsid_.put("ibm-37", "37");
      encodingCcsid_.put("cs-ebcdic-cp-us", "37");
      encodingCcsid_.put("cs-ebcdic-cp-ca", "37");
      encodingCcsid_.put("cs-ebcdic-cp-nl", "37");
      encodingCcsid_.put("IBM037", "37");

      ccsidEncoding_.put("37", "IBM037");

      encodingCcsid_.put("ibm-273", "273");
      encodingCcsid_.put("ibm273", "273");
      encodingCcsid_.put("273", "273");
      encodingCcsid_.put("cp273", "273");
      encodingCcsid_.put("Cp273", "273");
      encodingCcsid_.put("csibm273", "273");
      encodingCcsid_.put("IBM273", "273");

      ccsidEncoding_.put("273", "IBM273");

      encodingCcsid_.put("277", "277");
      encodingCcsid_.put("cp277", "277");
      encodingCcsid_.put("Cp277", "277");
      encodingCcsid_.put("ebcdic-cp-no", "277");
      encodingCcsid_.put("ibm-277", "277");
      encodingCcsid_.put("ibm277", "277");
      encodingCcsid_.put("ebcdic-cp-dk", "277");
      encodingCcsid_.put("csibm277", "277");
      encodingCcsid_.put("IBM277", "277");

      ccsidEncoding_.put("277", "IBM277");

      encodingCcsid_.put("cp278", "278");
      encodingCcsid_.put("Cp278", "278");
      encodingCcsid_.put("278", "278");
      encodingCcsid_.put("ibm-278", "278");
      encodingCcsid_.put("ebcdic-cp-se", "278");
      encodingCcsid_.put("ebcdic-cp-fi", "278");
      encodingCcsid_.put("csibm278", "278");
      encodingCcsid_.put("ibm278", "278");
      encodingCcsid_.put("ebcdic-sv", "278");
      encodingCcsid_.put("IBM278", "278");

      ccsidEncoding_.put("278", "IBM278");

      encodingCcsid_.put("ibm280", "280");
      encodingCcsid_.put("280", "280");
      encodingCcsid_.put("cp280", "280");
      encodingCcsid_.put("Cp280", "280");
      encodingCcsid_.put("csibm280", "280");
      encodingCcsid_.put("ibm-280", "280");
      encodingCcsid_.put("ebcdic-cp-it", "280");
      encodingCcsid_.put("IBM280", "280");

      ccsidEncoding_.put("280", "IBM280");

      encodingCcsid_.put("ebcdic-cp-es", "284");
      encodingCcsid_.put("csIBM284", "284");
      encodingCcsid_.put("cpibm284", "284");
      encodingCcsid_.put("cp284", "284");
      encodingCcsid_.put("Cp284", "284");
      encodingCcsid_.put("ibm-284", "284");
      encodingCcsid_.put("ibm284", "284");
      encodingCcsid_.put("284", "284");
      encodingCcsid_.put("IBM284", "284");

      ccsidEncoding_.put("284", "IBM284");

      encodingCcsid_.put("csIBM285", "285");
      encodingCcsid_.put("cp285", "285");
      encodingCcsid_.put("Cp285", "285");
      encodingCcsid_.put("ebcdic-gb", "285");
      encodingCcsid_.put("ibm-285", "285");
      encodingCcsid_.put("cpibm285", "285");
      encodingCcsid_.put("ibm285", "285");
      encodingCcsid_.put("285", "285");
      encodingCcsid_.put("ebcdic-cp-gb", "285");
      encodingCcsid_.put("IBM285", "285");

      ccsidEncoding_.put("285", "IBM285");

      encodingCcsid_.put("cpibm290", "290");
      encodingCcsid_.put("ibm290", "290");
      encodingCcsid_.put("EBCDIC-JP-kana", "290");
      encodingCcsid_.put("290", "290");
      encodingCcsid_.put("cp290", "290");
      encodingCcsid_.put("csIBM290", "290");
      encodingCcsid_.put("ibm-290", "290");
      encodingCcsid_.put("IBM290", "290");

      ccsidEncoding_.put("290", "IBM290");

      encodingCcsid_.put("297", "297");
      encodingCcsid_.put("csIBM297", "297");
      encodingCcsid_.put("cp297", "297");
      encodingCcsid_.put("Cp297", "297");
      encodingCcsid_.put("ibm297", "297");
      encodingCcsid_.put("ibm-297", "297");
      encodingCcsid_.put("cpibm297", "297");
      encodingCcsid_.put("ebcdic-cp-fr", "297");
      encodingCcsid_.put("IBM297", "297");

      ccsidEncoding_.put("297", "IBM297");

      encodingCcsid_.put("cp300", "300");
      encodingCcsid_.put("ibm300", "300");
      encodingCcsid_.put("300", "300");
      encodingCcsid_.put("ibm-300", "300");
      encodingCcsid_.put("cpibm300", "300");
      encodingCcsid_.put("csIBM300", "300");
      encodingCcsid_.put("x-IBM300", "300");

      ccsidEncoding_.put("300", "x-IBM300");

      encodingCcsid_.put("ANSI_X3.4-1968", "367");
      encodingCcsid_.put("cp367", "367");
      encodingCcsid_.put("csASCII", "367");
      encodingCcsid_.put("ibm-367", "367");
      encodingCcsid_.put("iso-ir-6", "367");
      encodingCcsid_.put("direct", "367");
      encodingCcsid_.put("ASCII", "367");
      encodingCcsid_.put("iso_646.irv:1983", "367");
      encodingCcsid_.put("ANSI_X3.4-1986", "367");
      encodingCcsid_.put("ascii7", "367");
      encodingCcsid_.put("default", "367");
      encodingCcsid_.put("ISO_646.irv:1991", "367");
      encodingCcsid_.put("367", "367");
      encodingCcsid_.put("ISO646-US", "367");
      encodingCcsid_.put("IBM367", "367");
      encodingCcsid_.put("646", "367");
      encodingCcsid_.put("ISO-646.irv:1991", "367");
      encodingCcsid_.put("ansi-x3.4-1968", "367");
      encodingCcsid_.put("iso-646.irv:1983", "367");
      encodingCcsid_.put("ansi-x3.4-1986", "367");
      encodingCcsid_.put("us", "367");
      encodingCcsid_.put("US-ASCII", "367");

      ccsidEncoding_.put("367", "US-ASCII");

      encodingCcsid_.put("ibm420", "420");
      encodingCcsid_.put("420", "420");
      encodingCcsid_.put("cp420", "420");
      encodingCcsid_.put("Cp420", "420");
      encodingCcsid_.put("csIBM420", "420");
      encodingCcsid_.put("ibm-420", "420");
      encodingCcsid_.put("ebcdic-cp-ar1", "420");
      encodingCcsid_.put("IBM420", "420");

      ccsidEncoding_.put("420", "IBM420");

      encodingCcsid_.put("ebcdic-cp-he", "424");
      encodingCcsid_.put("csIBM424", "424");
      encodingCcsid_.put("ibm-424", "424");
      encodingCcsid_.put("ibm424", "424");
      encodingCcsid_.put("424", "424");
      encodingCcsid_.put("cp424", "424");
      encodingCcsid_.put("Cp424", "424");
      encodingCcsid_.put("IBM424", "424");

      ccsidEncoding_.put("424", "IBM424");

      encodingCcsid_.put("ibm437", "437");
      encodingCcsid_.put("437", "437");
      encodingCcsid_.put("ibm-437", "437");
      encodingCcsid_.put("cspc8codepage437", "437");
      encodingCcsid_.put("cp437", "437");
      encodingCcsid_.put("Cp437", "437");
      encodingCcsid_.put("windows-437", "437");
      encodingCcsid_.put("IBM437", "437");

      ccsidEncoding_.put("437", "IBM437");

      encodingCcsid_.put("ibm-500", "500");
      encodingCcsid_.put("ibm500", "500");
      encodingCcsid_.put("500", "500");
      encodingCcsid_.put("ebcdic-cp-be", "500");
      encodingCcsid_.put("ebcdic-cp-bh", "500");
      encodingCcsid_.put("ebcdic-cp-ch", "500");
      encodingCcsid_.put("csIBM500", "500");
      encodingCcsid_.put("cp500", "500");
      encodingCcsid_.put("Cp500", "500");
      encodingCcsid_.put("IBM500", "500");

      ccsidEncoding_.put("500", "IBM500");

      encodingCcsid_.put("cp737", "737");
      encodingCcsid_.put("Cp737", "737");
      encodingCcsid_.put("ibm737", "737");
      encodingCcsid_.put("737", "737");
      encodingCcsid_.put("ibm-737", "737");
      encodingCcsid_.put("x-IBM737", "737");

      ccsidEncoding_.put("737", "x-IBM737");

      encodingCcsid_.put("ibm-775", "775");
      encodingCcsid_.put("csPC775Baltic", "775");
      encodingCcsid_.put("ibm775", "775");
      encodingCcsid_.put("775", "775");
      encodingCcsid_.put("cp775", "775");
      encodingCcsid_.put("Cp775", "775");
      encodingCcsid_.put("IBM775", "775");

      ccsidEncoding_.put("775", "IBM775");

      encodingCcsid_.put("greek", "813");
      encodingCcsid_.put("8859_7", "813");
      encodingCcsid_.put("greek8", "813");
      encodingCcsid_.put("ibm813", "813");
      encodingCcsid_.put("ISO_8859-7", "813");
      encodingCcsid_.put("elot-928", "813");
      encodingCcsid_.put("iso8859_7", "813");
      encodingCcsid_.put("ISO8859_7", "813");
      encodingCcsid_.put("ISO-8859-7:1987", "813");
      encodingCcsid_.put("ELOT_928", "813");
      encodingCcsid_.put("cp813", "813");
      encodingCcsid_.put("ISO_8859-7:1987", "813");
      encodingCcsid_.put("8859-7", "813");
      encodingCcsid_.put("sun_eu_greek", "813");
      encodingCcsid_.put("csISOLatinGreek", "813");
      encodingCcsid_.put("iso-ir-126", "813");
      encodingCcsid_.put("813", "813");
      encodingCcsid_.put("iso8859-7", "813");
      encodingCcsid_.put("ECMA-118", "813");
      encodingCcsid_.put("ibm-813", "813");
      encodingCcsid_.put("ISO-8859-7", "813");

      ccsidEncoding_.put("813", "ISO-8859-7");

      encodingCcsid_.put("819", "819");
      encodingCcsid_.put("ISO8859-1", "819");
      encodingCcsid_.put("ISO8859_1", "819");
      encodingCcsid_.put("l1", "819");
      encodingCcsid_.put("ISO_8859-1:1987", "819");
      encodingCcsid_.put("ISO_8859-1", "819");
      encodingCcsid_.put("8859_1", "819");
      encodingCcsid_.put("iso-ir-100", "819");
      encodingCcsid_.put("latin1", "819");
      encodingCcsid_.put("cp819", "819");
      encodingCcsid_.put("ISO8859_1", "819");
      encodingCcsid_.put("8859-1", "819");
      encodingCcsid_.put("ISO-8859-1:1987", "819");
      encodingCcsid_.put("IBM819", "819");
      encodingCcsid_.put("ISO_8859_1", "819");
      encodingCcsid_.put("IBM-819", "819");
      encodingCcsid_.put("csISOLatin1", "819");
      encodingCcsid_.put("ISO-8859-1", "819");

      ccsidEncoding_.put("819", "ISO-8859-1");

      encodingCcsid_.put("ibm833", "833");
      encodingCcsid_.put("833", "833");
      encodingCcsid_.put("ibm-833", "833");
      encodingCcsid_.put("cp833", "833");
      encodingCcsid_.put("x-IBM833", "833");

      ccsidEncoding_.put("833", "x-IBM833");

      encodingCcsid_.put("ibm834", "834");
      encodingCcsid_.put("834", "834");
      encodingCcsid_.put("cp834", "834");
      encodingCcsid_.put("ibm-834", "834");
      encodingCcsid_.put("x-IBM834", "834");

      ccsidEncoding_.put("834", "x-IBM834");

      encodingCcsid_.put("ibm835", "835");
      encodingCcsid_.put("835", "835");
      encodingCcsid_.put("cp835", "835");
      encodingCcsid_.put("ibm-835", "835");
      encodingCcsid_.put("x-IBM835", "835");

      ccsidEncoding_.put("835", "x-IBM835");

      encodingCcsid_.put("cp836", "836");
      encodingCcsid_.put("ibm836", "836");
      encodingCcsid_.put("836", "836");
      encodingCcsid_.put("ibm-836", "836");
      encodingCcsid_.put("x-IBM836", "836");

      ccsidEncoding_.put("836", "x-IBM836");

      encodingCcsid_.put("cp837", "837");
      encodingCcsid_.put("ibm837", "837");
      encodingCcsid_.put("837", "837");
      encodingCcsid_.put("ibm-837", "837");
      encodingCcsid_.put("x-IBM837", "837");

      ccsidEncoding_.put("837", "x-IBM837");

      encodingCcsid_.put("ibm-838", "838");
      encodingCcsid_.put("ibm838", "838");
      encodingCcsid_.put("838", "838");
      encodingCcsid_.put("cp838", "838");
      encodingCcsid_.put("Cp838", "838");
      encodingCcsid_.put("IBM-Thai", "838");

      ccsidEncoding_.put("838", "IBM-Thai");

      encodingCcsid_.put("cp850", "850");
      encodingCcsid_.put("Cp850", "850");
      encodingCcsid_.put("cspc850multilingual", "850");
      encodingCcsid_.put("ibm850", "850");
      encodingCcsid_.put("850", "850");
      encodingCcsid_.put("ibm-850", "850");
      encodingCcsid_.put("IBM850", "850");

      ccsidEncoding_.put("850", "IBM850");

      encodingCcsid_.put("csPCp852", "852");
      encodingCcsid_.put("ibm-852", "852");
      encodingCcsid_.put("ibm852", "852");
      encodingCcsid_.put("852", "852");
      encodingCcsid_.put("cp852", "852");
      encodingCcsid_.put("Cp852", "852");
      encodingCcsid_.put("IBM852", "852");

      ccsidEncoding_.put("852", "IBM852");

      encodingCcsid_.put("ibm855", "855");
      encodingCcsid_.put("855", "855");
      encodingCcsid_.put("ibm-855", "855");
      encodingCcsid_.put("csIBM855", "855");
      encodingCcsid_.put("cp855", "855");
      encodingCcsid_.put("Cp855", "855");
      encodingCcsid_.put("cspcp855", "855");
      encodingCcsid_.put("IBM855", "855");

      ccsidEncoding_.put("855", "IBM855");

      encodingCcsid_.put("ibm856", "856");
      encodingCcsid_.put("856", "856");
      encodingCcsid_.put("cp856", "856");
      encodingCcsid_.put("Cp856", "856");
      encodingCcsid_.put("ibm-856", "856");
      encodingCcsid_.put("x-IBM856", "856");

      ccsidEncoding_.put("856", "x-IBM856");

      encodingCcsid_.put("ibm857", "857");
      encodingCcsid_.put("857", "857");
      encodingCcsid_.put("cp857", "857");
      encodingCcsid_.put("Cp857", "857");
      encodingCcsid_.put("csIBM857", "857");
      encodingCcsid_.put("ibm-857", "857");
      encodingCcsid_.put("IBM857", "857");

      ccsidEncoding_.put("857", "IBM857");

      encodingCcsid_.put("PC-Multilingual-850+euro", "858");
      encodingCcsid_.put("ibm858", "858");
      encodingCcsid_.put("cp-858", "858");
      encodingCcsid_.put("cp858", "858");
      encodingCcsid_.put("Cp858", "858");
      encodingCcsid_.put("858", "858");
      encodingCcsid_.put("cp00858", "858");
      encodingCcsid_.put("ibm-858", "858");
      encodingCcsid_.put("ccsid00858", "858");
      encodingCcsid_.put("IBM00858", "858");

      ccsidEncoding_.put("858", "IBM00858");

      encodingCcsid_.put("ibm860", "860");
      encodingCcsid_.put("860", "860");
      encodingCcsid_.put("cp860", "860");
      encodingCcsid_.put("Cp860", "860");
      encodingCcsid_.put("csIBM860", "860");
      encodingCcsid_.put("ibm-860", "860");
      encodingCcsid_.put("IBM860", "860");

      ccsidEncoding_.put("860", "IBM860");

      encodingCcsid_.put("cp861", "861");
      encodingCcsid_.put("Cp861", "861");
      encodingCcsid_.put("ibm861", "861");
      encodingCcsid_.put("861", "861");
      encodingCcsid_.put("ibm-861", "861");
      encodingCcsid_.put("cp-is", "861");
      encodingCcsid_.put("csIBM861", "861");
      encodingCcsid_.put("IBM861", "861");

      ccsidEncoding_.put("861", "IBM861");

      encodingCcsid_.put("csIBM862", "862");
      encodingCcsid_.put("cp862", "862");
      encodingCcsid_.put("Cp862", "862");
      encodingCcsid_.put("ibm862", "862");
      encodingCcsid_.put("862", "862");
      encodingCcsid_.put("cspc862latinhebrew", "862");
      encodingCcsid_.put("ibm-862", "862");
      encodingCcsid_.put("IBM862", "862");

      ccsidEncoding_.put("862", "IBM862");

      encodingCcsid_.put("csIBM863", "863");
      encodingCcsid_.put("ibm-863", "863");
      encodingCcsid_.put("ibm863", "863");
      encodingCcsid_.put("863", "863");
      encodingCcsid_.put("cp863", "863");
      encodingCcsid_.put("Cp863", "863");
      encodingCcsid_.put("IBM863", "863");

      ccsidEncoding_.put("863", "IBM863");

      encodingCcsid_.put("csIBM864", "864");
      encodingCcsid_.put("ibm-864", "864");
      encodingCcsid_.put("ibm864", "864");
      encodingCcsid_.put("864", "864");
      encodingCcsid_.put("cp864", "864");
      encodingCcsid_.put("Cp864", "864");
      encodingCcsid_.put("IBM864", "864");

      ccsidEncoding_.put("864", "IBM864");

      encodingCcsid_.put("ibm-865", "865");
      encodingCcsid_.put("csIBM865", "865");
      encodingCcsid_.put("cp865", "865");
      encodingCcsid_.put("Cp865", "865");
      encodingCcsid_.put("ibm865", "865");
      encodingCcsid_.put("865", "865");
      encodingCcsid_.put("IBM865", "865");

      ccsidEncoding_.put("865", "IBM865");

      encodingCcsid_.put("ibm866", "866");
      encodingCcsid_.put("866", "866");
      encodingCcsid_.put("ibm-866", "866");
      encodingCcsid_.put("csIBM866", "866");
      encodingCcsid_.put("cp866", "866");
      encodingCcsid_.put("Cp866", "866");
      encodingCcsid_.put("IBM866", "866");

      ccsidEncoding_.put("866", "IBM866");

      encodingCcsid_.put("ibm868", "868");
      encodingCcsid_.put("868", "868");
      encodingCcsid_.put("cp868", "868");
      encodingCcsid_.put("Cp868", "868");
      encodingCcsid_.put("csIBM868", "868");
      encodingCcsid_.put("ibm-868", "868");
      encodingCcsid_.put("cp-ar", "868");
      encodingCcsid_.put("IBM868", "868");

      ccsidEncoding_.put("868", "IBM868");

      encodingCcsid_.put("cp869", "869");
      encodingCcsid_.put("Cp869", "869");
      encodingCcsid_.put("ibm869", "869");
      encodingCcsid_.put("869", "869");
      encodingCcsid_.put("ibm-869", "869");
      encodingCcsid_.put("cp-gr", "869");
      encodingCcsid_.put("csIBM869", "869");
      encodingCcsid_.put("IBM869", "869");

      ccsidEncoding_.put("869", "IBM869");

      encodingCcsid_.put("870", "870");
      encodingCcsid_.put("cp870", "870");
      encodingCcsid_.put("Cp870", "870");
      encodingCcsid_.put("csIBM870", "870");
      encodingCcsid_.put("ibm-870", "870");
      encodingCcsid_.put("ibm870", "870");
      encodingCcsid_.put("ebcdic-cp-roece", "870");
      encodingCcsid_.put("ebcdic-cp-yu", "870");
      encodingCcsid_.put("IBM870", "870");

      ccsidEncoding_.put("870", "IBM870");

      encodingCcsid_.put("ibm871", "871");
      encodingCcsid_.put("871", "871");
      encodingCcsid_.put("cp871", "871");
      encodingCcsid_.put("Cp871", "871");
      encodingCcsid_.put("ebcdic-cp-is", "871");
      encodingCcsid_.put("csIBM871", "871");
      encodingCcsid_.put("ibm-871", "871");
      encodingCcsid_.put("IBM871", "871");

      ccsidEncoding_.put("871", "IBM871");

      encodingCcsid_.put("ibm-874", "874");
      encodingCcsid_.put("ibm874", "874");
      encodingCcsid_.put("874", "874");
      encodingCcsid_.put("cp874", "874");
      encodingCcsid_.put("Cp874", "874");
      encodingCcsid_.put("x-IBM874", "874");

      ccsidEncoding_.put("874", "x-IBM874");

      encodingCcsid_.put("ibm-875", "875");
      encodingCcsid_.put("ibm875", "875");
      encodingCcsid_.put("875", "875");
      encodingCcsid_.put("cp875", "875");
      encodingCcsid_.put("Cp875", "875");
      encodingCcsid_.put("x-IBM875", "875");

      ccsidEncoding_.put("875", "x-IBM875");

      encodingCcsid_.put("JIS0201", "897");
      encodingCcsid_.put("csHalfWidthKatakana", "897");
      encodingCcsid_.put("X0201", "897");
      encodingCcsid_.put("JIS_X0201", "897");
      encodingCcsid_.put("JIS_X0201", "897");

      ccsidEncoding_.put("897", "JIS_X0201");

      encodingCcsid_.put("ISO8859-2", "912");
      encodingCcsid_.put("ISO8859_2", "912");
      encodingCcsid_.put("ibm912", "912");
      encodingCcsid_.put("l2", "912");
      encodingCcsid_.put("ISO_8859-2", "912");
      encodingCcsid_.put("8859_2", "912");
      encodingCcsid_.put("ISO-8859-2:1987", "912");
      encodingCcsid_.put("cp912", "912");
      encodingCcsid_.put("ISO_8859-2:1987", "912");
      encodingCcsid_.put("8859-2", "912");
      encodingCcsid_.put("iso8859_2", "912");
      encodingCcsid_.put("iso-ir-101", "912");
      encodingCcsid_.put("latin2", "912");
      encodingCcsid_.put("912", "912");
      encodingCcsid_.put("csISOLatin2", "912");
      encodingCcsid_.put("ibm-912", "912");
      encodingCcsid_.put("ISO-8859-2", "912");

      ccsidEncoding_.put("912", "ISO-8859-2");

      encodingCcsid_.put("ibm913", "913");
      encodingCcsid_.put("8859_3", "913");
      encodingCcsid_.put("iso8859-3", "913");
      encodingCcsid_.put("iso-8859-3:1988", "913");
      encodingCcsid_.put("l3", "913");
      encodingCcsid_.put("cp913", "913");
      encodingCcsid_.put("iso_8859-3", "913");
      encodingCcsid_.put("iso8859_3", "913");
      encodingCcsid_.put("ISO8859_3", "913");
      encodingCcsid_.put("iso_8859-3:1988", "913");
      encodingCcsid_.put("latin3", "913");
      encodingCcsid_.put("8859-3", "913");
      encodingCcsid_.put("csISOLatin3", "913");
      encodingCcsid_.put("913", "913");
      encodingCcsid_.put("ibm-913", "913");
      encodingCcsid_.put("iso-ir-109", "913");
      encodingCcsid_.put("ISO-8859-3", "913");

      ccsidEncoding_.put("913", "ISO-8859-3");

      encodingCcsid_.put("8859_4", "914");
      encodingCcsid_.put("latin4", "914");
      encodingCcsid_.put("l4", "914");
      encodingCcsid_.put("cp914", "914");
      encodingCcsid_.put("ISO-8859-4:1988", "914");
      encodingCcsid_.put("ISO_8859-4:1988", "914");
      encodingCcsid_.put("ibm914", "914");
      encodingCcsid_.put("ISO_8859-4", "914");
      encodingCcsid_.put("iso-ir-110", "914");
      encodingCcsid_.put("8859-4", "914");
      encodingCcsid_.put("iso8859_4", "914");
      encodingCcsid_.put("ISO8859_4", "914");
      encodingCcsid_.put("csISOLatin4", "914");
      encodingCcsid_.put("iso8859-4", "914");
      encodingCcsid_.put("914", "914");
      encodingCcsid_.put("ibm-914", "914");
      encodingCcsid_.put("ISO-8859-4", "914");

      ccsidEncoding_.put("914", "ISO-8859-4");

      encodingCcsid_.put("8859_5", "915");
      encodingCcsid_.put("ISO_8859-5", "915");
      encodingCcsid_.put("ibm915", "915");
      encodingCcsid_.put("cyrillic", "915");
      encodingCcsid_.put("ISO_8859-5:1988", "915");
      encodingCcsid_.put("csISOLatinCyrillic", "915");
      encodingCcsid_.put("8859-5", "915");
      encodingCcsid_.put("ISO-8859-5:1988", "915");
      encodingCcsid_.put("iso-ir-144", "915");
      encodingCcsid_.put("iso8859_5", "915");
      encodingCcsid_.put("ISO8859_5", "915");
      encodingCcsid_.put("cp915", "915");
      encodingCcsid_.put("ibm-915", "915");
      encodingCcsid_.put("915", "915");
      encodingCcsid_.put("ISO8859-5", "915");
      encodingCcsid_.put("ISO-8859-5", "915");

      ccsidEncoding_.put("915", "ISO-8859-5");

      encodingCcsid_.put("8859_8", "916");
      encodingCcsid_.put("iso_8859-8", "916");
      encodingCcsid_.put("hebrew", "916");
      encodingCcsid_.put("iso8859_8", "916");
      encodingCcsid_.put("ISO8859_8", "916");
      encodingCcsid_.put("iso-8859-8:1988", "916");
      encodingCcsid_.put("iso_8859-8:1988", "916");
      encodingCcsid_.put("csISOLatinHebrew", "916");
      encodingCcsid_.put("ibm916", "916");
      encodingCcsid_.put("8859-8", "916");
      encodingCcsid_.put("cp916", "916");
      encodingCcsid_.put("iso-ir-138", "916");
      encodingCcsid_.put("ibm-916", "916");
      encodingCcsid_.put("iso8859-8", "916");
      encodingCcsid_.put("916", "916");
      encodingCcsid_.put("ISO-8859-8", "916");

      ccsidEncoding_.put("916", "ISO-8859-8");

      encodingCcsid_.put("ibm918", "918");
      encodingCcsid_.put("918", "918");
      encodingCcsid_.put("ibm-918", "918");
      encodingCcsid_.put("csibm918", "918");
      encodingCcsid_.put("ebcdic-cp-ar2", "918");
      encodingCcsid_.put("cp918", "918");
      encodingCcsid_.put("Cp918", "918");
      encodingCcsid_.put("IBM918", "918");

      ccsidEncoding_.put("918", "IBM918");

      encodingCcsid_.put("ibm-920", "920");
      encodingCcsid_.put("ISO_8859-9", "920");
      encodingCcsid_.put("8859_9", "920");
      encodingCcsid_.put("ISO_8859-9:1989", "920");
      encodingCcsid_.put("ibm920", "920");
      encodingCcsid_.put("latin5", "920");
      encodingCcsid_.put("l5", "920");
      encodingCcsid_.put("iso8859_9", "920");
      encodingCcsid_.put("ISO8859_9", "920");
      encodingCcsid_.put("cp920", "920");
      encodingCcsid_.put("8859-9", "920");
      encodingCcsid_.put("920", "920");
      encodingCcsid_.put("iso-ir-148", "920");
      encodingCcsid_.put("ISO8859-9", "920");
      encodingCcsid_.put("csISOLatin5", "920");
      encodingCcsid_.put("ISO-8859-9", "920");

      ccsidEncoding_.put("920", "ISO-8859-9");

      encodingCcsid_.put("ibm921", "921");
      encodingCcsid_.put("921", "921");
      encodingCcsid_.put("ibm-921", "921");
      encodingCcsid_.put("cp921", "921");
      encodingCcsid_.put("Cp921", "921");
      encodingCcsid_.put("x-IBM921", "921");

      ccsidEncoding_.put("921", "x-IBM921");

      encodingCcsid_.put("ibm922", "922");
      encodingCcsid_.put("922", "922");
      encodingCcsid_.put("cp922", "922");
      encodingCcsid_.put("Cp922", "922");
      encodingCcsid_.put("ibm-922", "922");
      encodingCcsid_.put("x-IBM922", "922");

      ccsidEncoding_.put("922", "x-IBM922");

      encodingCcsid_.put("ISO8859-15-FDIS", "923");
      encodingCcsid_.put("ISO8859-15", "923");
      encodingCcsid_.put("LATIN0", "923");
      encodingCcsid_.put("ISO8859_15", "923");
      encodingCcsid_.put("cp923", "923");
      encodingCcsid_.put("Cp923", "923");
      encodingCcsid_.put("8859_15", "923");
      encodingCcsid_.put("L9", "923");
      encodingCcsid_.put("iso8859_15_fdis", "923");
      encodingCcsid_.put("ISO-8859-15", "923");
      encodingCcsid_.put("IBM923", "923");
      encodingCcsid_.put("csISOlatin9", "923");
      encodingCcsid_.put("ISO_8859-15", "923");
      encodingCcsid_.put("IBM-923", "923");
      encodingCcsid_.put("csISOlatin0", "923");
      encodingCcsid_.put("923", "923");
      encodingCcsid_.put("8859-15", "923");
      encodingCcsid_.put("LATIN9", "923");
      encodingCcsid_.put("ISO-8859-15", "923");

      ccsidEncoding_.put("923", "ISO-8859-15");

      encodingCcsid_.put("ibm924", "924");
      encodingCcsid_.put("ebcdic-latin9--euro", "924");
      encodingCcsid_.put("cp924", "924");
      encodingCcsid_.put("924", "924");
      encodingCcsid_.put("ibm-924", "924");
      encodingCcsid_.put("cp00924", "924");
      encodingCcsid_.put("ibm00924", "924");
      encodingCcsid_.put("ccsid00924", "924");
      encodingCcsid_.put("IBM00924", "924");

      ccsidEncoding_.put("924", "IBM00924");

      
      encodingCcsid_.put("ibm-930", "930");
      encodingCcsid_.put("ibm930", "930");
      encodingCcsid_.put("930", "930");
      encodingCcsid_.put("cp930", "930");
      encodingCcsid_.put("Cp930", "930");
      encodingCcsid_.put("x-IBM930", "930");
      
      ccsidEncoding_.put("930", "x-IBM930");
      

      encodingCcsid_.put("ibm933", "933");
      encodingCcsid_.put("933", "933");
      encodingCcsid_.put("cp933", "933");
      encodingCcsid_.put("Cp933", "933");
      encodingCcsid_.put("ibm-933", "933");
      encodingCcsid_.put("x-IBM933", "933");

      ccsidEncoding_.put("933", "x-IBM933");

      encodingCcsid_.put("cp935", "935");
      encodingCcsid_.put("Cp935", "935");
      encodingCcsid_.put("ibm935", "935");
      encodingCcsid_.put("935", "935");
      encodingCcsid_.put("ibm-935", "935");
      encodingCcsid_.put("x-IBM935", "935");

      ccsidEncoding_.put("935", "x-IBM935");

      encodingCcsid_.put("ibm-937", "937");
      encodingCcsid_.put("ibm937", "937");
      encodingCcsid_.put("937", "937");
      encodingCcsid_.put("cp937", "937");
      encodingCcsid_.put("Cp937", "937");
      encodingCcsid_.put("x-IBM937", "937");

      ccsidEncoding_.put("937", "x-IBM937");

      
      encodingCcsid_.put("cp939", "939");
      encodingCcsid_.put("Cp939", "939");
      encodingCcsid_.put("ibm-939", "939");
      encodingCcsid_.put("ibm939", "939");
      encodingCcsid_.put("939", "939");
      encodingCcsid_.put("x-IBM939", "939");

      ccsidEncoding_.put("939", "x-IBM939");

      encodingCcsid_.put("ibm-942", "942");
      encodingCcsid_.put("cp942", "942");
      encodingCcsid_.put("Cp942", "942");
      encodingCcsid_.put("ibm942", "942");
      encodingCcsid_.put("942", "942");
      encodingCcsid_.put("x-IBM942", "942");

      ccsidEncoding_.put("942", "x-IBM942");

      encodingCcsid_.put("ibm943", "943");
      encodingCcsid_.put("943", "943");
      encodingCcsid_.put("ibm-943", "943");
      encodingCcsid_.put("cp943", "943");
      encodingCcsid_.put("Cp943", "943");
      encodingCcsid_.put("windows-31j", "943");
      encodingCcsid_.put("MS932", "943");
      encodingCcsid_.put("windows-932", "943");
      encodingCcsid_.put("csWindows31J", "943");
      encodingCcsid_.put("x-IBM943", "943");

      ccsidEncoding_.put("943", "x-IBM943");

      encodingCcsid_.put("ibm-948", "948");
      encodingCcsid_.put("ibm948", "948");
      encodingCcsid_.put("948", "948");
      encodingCcsid_.put("cp948", "948");
      encodingCcsid_.put("Cp948", "948");
      encodingCcsid_.put("x-IBM948", "948");

      ccsidEncoding_.put("948", "x-IBM948");

      encodingCcsid_.put("ibm-949", "949");
      encodingCcsid_.put("ibm949", "949");
      encodingCcsid_.put("949", "949");
      encodingCcsid_.put("cp949", "949");
      encodingCcsid_.put("Cp949", "949");
      encodingCcsid_.put("x-IBM949", "949");

      ccsidEncoding_.put("949", "x-IBM949");

      encodingCcsid_.put("cp950", "950");
      encodingCcsid_.put("Cp950", "950");
      encodingCcsid_.put("ibm950", "950");
      encodingCcsid_.put("950", "950");
      encodingCcsid_.put("ibm-950", "950");
      encodingCcsid_.put("Big5", "950");
      encodingCcsid_.put("csBig5", "950");
      encodingCcsid_.put("big5-0", "950");
      encodingCcsid_.put("x-IBM950", "950");

      ccsidEncoding_.put("950", "x-IBM950");

      encodingCcsid_.put("JIS0208", "952");
      encodingCcsid_.put("JIS_C6226-1983", "952");
      encodingCcsid_.put("iso-ir-87", "952");
      encodingCcsid_.put("x0208", "952");
      encodingCcsid_.put("JIS_X0208-1983", "952");
      encodingCcsid_.put("csISO87JISX0208", "952");
      encodingCcsid_.put("x-JIS0208", "952");

      ccsidEncoding_.put("952", "x-JIS0208");

      encodingCcsid_.put("JIS0212", "953");
      encodingCcsid_.put("iso-ir-159", "953");
      encodingCcsid_.put("x0212", "953");
      encodingCcsid_.put("jis_x0212-1990", "953");
      encodingCcsid_.put("csISO159JISX02121990", "953");
      encodingCcsid_.put("JIS_X0212-1990", "953");

      ccsidEncoding_.put("953", "JIS_X0212-1990");

      encodingCcsid_.put("ibm-964", "964");
      encodingCcsid_.put("cp964", "964");
      encodingCcsid_.put("Cp964", "964");
      encodingCcsid_.put("ibm-euctw", "964");
      encodingCcsid_.put("ibm964", "964");
      encodingCcsid_.put("964", "964");
      encodingCcsid_.put("x-IBM964", "964");

      ccsidEncoding_.put("964", "x-IBM964");

      encodingCcsid_.put("cpeuckr", "970");
      encodingCcsid_.put("ibmeuckr", "970");
      encodingCcsid_.put("970", "970");
      encodingCcsid_.put("cp970", "970");
      encodingCcsid_.put("Cp970", "970");
      encodingCcsid_.put("ibm-970", "970");
      encodingCcsid_.put("ibm970", "970");
      encodingCcsid_.put("ibm-euckr", "970");
      encodingCcsid_.put("x-IBM970", "970");

      ccsidEncoding_.put("970", "x-IBM970");

      encodingCcsid_.put("ibm1006", "1006");
      encodingCcsid_.put("ibm-1006", "1006");
      encodingCcsid_.put("1006", "1006");
      encodingCcsid_.put("cp1006", "1006");
      encodingCcsid_.put("Cp1006", "1006");
      encodingCcsid_.put("x-IBM1006", "1006");

      ccsidEncoding_.put("1006", "x-IBM1006");

      encodingCcsid_.put("ibm-1025", "1025");
      encodingCcsid_.put("1025", "1025");
      encodingCcsid_.put("cp1025", "1025");
      encodingCcsid_.put("Cp1025", "1025");
      encodingCcsid_.put("ibm1025", "1025");
      encodingCcsid_.put("x-IBM1025", "1025");

      ccsidEncoding_.put("1025", "x-IBM1025");

      encodingCcsid_.put("cp1026", "1026");
      encodingCcsid_.put("Cp1026", "1026");
      encodingCcsid_.put("ibm-1026", "1026");
      encodingCcsid_.put("1026", "1026");
      encodingCcsid_.put("ibm1026", "1026");
      encodingCcsid_.put("IBM1026", "1026");

      ccsidEncoding_.put("1026", "IBM1026");

      encodingCcsid_.put("cp1027", "1027");
      encodingCcsid_.put("ibm1027", "1027");
      encodingCcsid_.put("ibm-1027", "1027");
      encodingCcsid_.put("1027", "1027");
      encodingCcsid_.put("x-IBM1027", "1027");

      ccsidEncoding_.put("1027", "x-IBM1027");

      encodingCcsid_.put("ibm1046", "1046");
      encodingCcsid_.put("ibm-1046", "1046");
      encodingCcsid_.put("1046", "1046");
      encodingCcsid_.put("cp1046", "1046");
      encodingCcsid_.put("Cp1046", "1046");
      encodingCcsid_.put("x-IBM1046", "1046");

      ccsidEncoding_.put("1046", "x-IBM1046");

      encodingCcsid_.put("ibm-1047", "1047");
      encodingCcsid_.put("1047", "1047");
      encodingCcsid_.put("cp1047", "1047");
      encodingCcsid_.put("ibm1047", "1047");
      encodingCcsid_.put("IBM1047", "1047");

      ccsidEncoding_.put("1047", "IBM1047");

      encodingCcsid_.put("8859_6", "1089");
      encodingCcsid_.put("iso8859_6", "1089");
      encodingCcsid_.put("ISO8859_6", "1089");
      encodingCcsid_.put("iso_8859-6", "1089");
      encodingCcsid_.put("8859-6", "1089");
      encodingCcsid_.put("iso_8859-6:1987", "1089");
      encodingCcsid_.put("ecma-114", "1089");
      encodingCcsid_.put("csISOLatinArabic", "1089");
      encodingCcsid_.put("ibm1089", "1089");
      encodingCcsid_.put("asmo-708", "1089");
      encodingCcsid_.put("arabic", "1089");
      encodingCcsid_.put("ibm-1089", "1089");
      encodingCcsid_.put("1089", "1089");
      encodingCcsid_.put("iso-ir-127", "1089");
      encodingCcsid_.put("iso-8859-6:1987", "1089");
      encodingCcsid_.put("iso8859-6", "1089");
      encodingCcsid_.put("cp1089", "1089");
      encodingCcsid_.put("ISO-8859-6", "1089");

      ccsidEncoding_.put("1089", "ISO-8859-6");

      encodingCcsid_.put("ibm1097", "1097");
      encodingCcsid_.put("ibm-1097", "1097");
      encodingCcsid_.put("1097", "1097");
      encodingCcsid_.put("cp1097", "1097");
      encodingCcsid_.put("Cp1097", "1097");
      encodingCcsid_.put("x-IBM1097", "1097");

      ccsidEncoding_.put("1097", "x-IBM1097");

      encodingCcsid_.put("ibm-1098", "1098");
      encodingCcsid_.put("1098", "1098");
      encodingCcsid_.put("cp1098", "1098");
      encodingCcsid_.put("Cp1098", "1098");
      encodingCcsid_.put("ibm1098", "1098");
      encodingCcsid_.put("x-IBM1098", "1098");

      ccsidEncoding_.put("1098", "x-IBM1098");

      encodingCcsid_.put("ibm1112", "1112");
      encodingCcsid_.put("ibm-1112", "1112");
      encodingCcsid_.put("1112", "1112");
      encodingCcsid_.put("cp1112", "1112");
      encodingCcsid_.put("Cp1112", "1112");
      encodingCcsid_.put("x-IBM1112", "1112");

      ccsidEncoding_.put("1112", "x-IBM1112");

      encodingCcsid_.put("cp1122", "1122");
      encodingCcsid_.put("Cp1122", "1122");
      encodingCcsid_.put("ibm1122", "1122");
      encodingCcsid_.put("ibm-1122", "1122");
      encodingCcsid_.put("1122", "1122");
      encodingCcsid_.put("x-IBM1122", "1122");

      ccsidEncoding_.put("1122", "x-IBM1122");

      encodingCcsid_.put("ibm1123", "1123");
      encodingCcsid_.put("ibm-1123", "1123");
      encodingCcsid_.put("1123", "1123");
      encodingCcsid_.put("cp1123", "1123");
      encodingCcsid_.put("Cp1123", "1123");
      encodingCcsid_.put("x-IBM1123", "1123");

      ccsidEncoding_.put("1123", "x-IBM1123");

      encodingCcsid_.put("ibm-1124", "1124");
      encodingCcsid_.put("1124", "1124");
      encodingCcsid_.put("cp1124", "1124");
      encodingCcsid_.put("Cp1124", "1124");
      encodingCcsid_.put("ibm1124", "1124");
      encodingCcsid_.put("x-IBM1124", "1124");

      ccsidEncoding_.put("1124", "x-IBM1124");

      encodingCcsid_.put("ibm1130", "1130");
      encodingCcsid_.put("ibm-1130", "1130");
      encodingCcsid_.put("1130", "1130");
      encodingCcsid_.put("cp1130", "1130");
      encodingCcsid_.put("x-IBM1130", "1130");

      ccsidEncoding_.put("1130", "x-IBM1130");

      encodingCcsid_.put("ibm1140", "1140");
      encodingCcsid_.put("IBM-1140", "1140");
      encodingCcsid_.put("cp1140", "1140");
      encodingCcsid_.put("Cp1140", "1140");
      encodingCcsid_.put("ibm-1140", "1140");
      encodingCcsid_.put("1140", "1140");
      encodingCcsid_.put("cp01140", "1140");
      encodingCcsid_.put("ccsid01140", "1140");
      encodingCcsid_.put("IBM01140", "1140");

      ccsidEncoding_.put("1140", "IBM01140");

      encodingCcsid_.put("ibm-1141", "1141");
      encodingCcsid_.put("ibm1141", "1141");
      encodingCcsid_.put("1141", "1141");
      encodingCcsid_.put("cp1141", "1141");
      encodingCcsid_.put("Cp1141", "1141");
      encodingCcsid_.put("cp01141", "1141");
      encodingCcsid_.put("ccsid01141", "1141");
      encodingCcsid_.put("IBM01141", "1141");

      ccsidEncoding_.put("1141", "IBM01141");

      encodingCcsid_.put("ibm-1142", "1142");
      encodingCcsid_.put("1142", "1142");
      encodingCcsid_.put("cp1142", "1142");
      encodingCcsid_.put("Cp1142", "1142");
      encodingCcsid_.put("cp01142", "1142");
      encodingCcsid_.put("ibm1142", "1142");
      encodingCcsid_.put("ccsid01142", "1142");
      encodingCcsid_.put("IBM01142", "1142");

      ccsidEncoding_.put("1142", "IBM01142");

      encodingCcsid_.put("1143", "1143");
      encodingCcsid_.put("cp01143", "1143");
      encodingCcsid_.put("ccsid01143", "1143");
      encodingCcsid_.put("ibm1143", "1143");
      encodingCcsid_.put("cp1143", "1143");
      encodingCcsid_.put("Cp1143", "1143");
      encodingCcsid_.put("ibm-1143", "1143");
      encodingCcsid_.put("IBM01143", "1143");

      ccsidEncoding_.put("1143", "IBM01143");

      encodingCcsid_.put("cp01144", "1144");
      encodingCcsid_.put("ccsid01144", "1144");
      encodingCcsid_.put("cp1144", "1144");
      encodingCcsid_.put("Cp1144", "1144");
      encodingCcsid_.put("ibm1144", "1144");
      encodingCcsid_.put("ibm-1144", "1144");
      encodingCcsid_.put("1144", "1144");
      encodingCcsid_.put("IBM01144", "1144");

      ccsidEncoding_.put("1144", "IBM01144");

      encodingCcsid_.put("ccsid01145", "1145");
      encodingCcsid_.put("ibm1145", "1145");
      encodingCcsid_.put("ibm-1145", "1145");
      encodingCcsid_.put("1145", "1145");
      encodingCcsid_.put("cp1145", "1145");
      encodingCcsid_.put("Cp1145", "1145");
      encodingCcsid_.put("cp01145", "1145");
      encodingCcsid_.put("IBM01145", "1145");

      ccsidEncoding_.put("1145", "IBM01145");

      encodingCcsid_.put("ibm-1146", "1146");
      encodingCcsid_.put("1146", "1146");
      encodingCcsid_.put("cp1146", "1146");
      encodingCcsid_.put("Cp1146", "1146");
      encodingCcsid_.put("cp01146", "1146");
      encodingCcsid_.put("ibm1146", "1146");
      encodingCcsid_.put("ccsid01146", "1146");
      encodingCcsid_.put("IBM01146", "1146");

      ccsidEncoding_.put("1146", "IBM01146");

      encodingCcsid_.put("cp1147", "1147");
      encodingCcsid_.put("Cp1147", "1147");
      encodingCcsid_.put("ibm-1147", "1147");
      encodingCcsid_.put("1147", "1147");
      encodingCcsid_.put("cp01147", "1147");
      encodingCcsid_.put("ccsid01147", "1147");
      encodingCcsid_.put("ibm1147", "1147");
      encodingCcsid_.put("IBM01147", "1147");

      ccsidEncoding_.put("1147", "IBM01147");

      encodingCcsid_.put("cp1148", "1148");
      encodingCcsid_.put("Cp1148", "1148");
      encodingCcsid_.put("ibm1148", "1148");
      encodingCcsid_.put("ibm-1148", "1148");
      encodingCcsid_.put("1148", "1148");
      encodingCcsid_.put("cp01148", "1148");
      encodingCcsid_.put("ccsid01148", "1148");
      encodingCcsid_.put("IBM01148", "1148");

      ccsidEncoding_.put("1148", "IBM01148");

      encodingCcsid_.put("ibm1149", "1149");
      encodingCcsid_.put("ibm-1149", "1149");
      encodingCcsid_.put("1149", "1149");
      encodingCcsid_.put("cp1149", "1149");
      encodingCcsid_.put("Cp1149", "1149");
      encodingCcsid_.put("cp01149", "1149");
      encodingCcsid_.put("ccsid01149", "1149");
      encodingCcsid_.put("IBM01149", "1149");

      ccsidEncoding_.put("1149", "IBM01149");

      encodingCcsid_.put("cp01153", "1153");
      encodingCcsid_.put("ibm1153", "1153");
      encodingCcsid_.put("ccsid01153", "1153");
      encodingCcsid_.put("ibm01153", "1153");
      encodingCcsid_.put("ibm-1153", "1153");
      encodingCcsid_.put("1153", "1153");
      encodingCcsid_.put("cp1153", "1153");
      encodingCcsid_.put("x-IBM1153", "1153");

      ccsidEncoding_.put("1153", "x-IBM1153");

      encodingCcsid_.put("ibm-1164", "1164");
      encodingCcsid_.put("1164", "1164");
      encodingCcsid_.put("cp1164", "1164");
      encodingCcsid_.put("ibm1164", "1164");
      encodingCcsid_.put("x-IBM1164", "1164");

      ccsidEncoding_.put("1164", "x-IBM1164");

      encodingCcsid_.put("cp1166", "1166");
      encodingCcsid_.put("ibm1166", "1166");
      encodingCcsid_.put("ibm-1166", "1166");
      encodingCcsid_.put("1166", "1166");
      encodingCcsid_.put("x-IBM1166", "1166");

      ccsidEncoding_.put("1166", "x-IBM1166");

      encodingCcsid_.put("X-UTF-16BE", "1200");
      encodingCcsid_.put("unicode-1-1", "1200");
      encodingCcsid_.put("UTF_16BE", "1200");
      encodingCcsid_.put("ISO-10646-UCS-2", "1200");
      encodingCcsid_.put("UTF16BE", "1200");
      encodingCcsid_.put("UnicodeBigUnmarked", "1200");
      encodingCcsid_.put("UTF-16BE", "1200");

      ccsidEncoding_.put("1200", "UTF-16BE");

      encodingCcsid_.put("x-utf-16le-bom", "1202");
      encodingCcsid_.put("UnicodeLittle", "1202");
      encodingCcsid_.put("utf_16le_bom", "1202");
      encodingCcsid_.put("X-UnicodeLittle", "1202");
      encodingCcsid_.put("x-UTF-16LE-BOM", "1202");

      ccsidEncoding_.put("1202", "x-UTF-16LE-BOM");

      encodingCcsid_.put("unicode-1-1-utf-8", "1208");
      encodingCcsid_.put("UTF_8", "1208");
      encodingCcsid_.put("UTF8", "1208");
      encodingCcsid_.put("UTF-8", "1208");

      ccsidEncoding_.put("1208", "UTF-8");

      encodingCcsid_.put("cp1250", "1250");
      encodingCcsid_.put("Cp1250", "1250");
      encodingCcsid_.put("ibm1250", "1250");
      encodingCcsid_.put("ibm-1250", "1250");
      encodingCcsid_.put("1250", "1250");
      encodingCcsid_.put("cp5346", "1250");
      encodingCcsid_.put("windows-1250", "1250");

      ccsidEncoding_.put("1250", "windows-1250");

      encodingCcsid_.put("ibm-1251", "1251");
      encodingCcsid_.put("ibm1251", "1251");
      encodingCcsid_.put("1251", "1251");
      encodingCcsid_.put("cp1251", "1251");
      encodingCcsid_.put("Cp1251", "1251");
      encodingCcsid_.put("cp5347", "1251");
      encodingCcsid_.put("ansi-1251", "1251");
      encodingCcsid_.put("windows-1251", "1251");

      ccsidEncoding_.put("1251", "windows-1251");

      encodingCcsid_.put("ibm-1252", "1252");
      encodingCcsid_.put("1252", "1252");
      encodingCcsid_.put("cp1252", "1252");
      encodingCcsid_.put("Cp1252", "1252");
      encodingCcsid_.put("ibm1252", "1252");
      encodingCcsid_.put("cp5348", "1252");
      encodingCcsid_.put("windows-1252", "1252");

      ccsidEncoding_.put("1252", "windows-1252");

      encodingCcsid_.put("1253", "1253");
      encodingCcsid_.put("cp5349", "1253");
      encodingCcsid_.put("ibm1253", "1253");
      encodingCcsid_.put("cp1253", "1253");
      encodingCcsid_.put("Cp1253", "1253");
      encodingCcsid_.put("ibm-1253", "1253");
      encodingCcsid_.put("windows-1253", "1253");

      ccsidEncoding_.put("1253", "windows-1253");

      encodingCcsid_.put("cp1254", "1254");
      encodingCcsid_.put("Cp1254", "1254");
      encodingCcsid_.put("cp5350", "1254");
      encodingCcsid_.put("ibm1254", "1254");
      encodingCcsid_.put("ibm-1254", "1254");
      encodingCcsid_.put("1254", "1254");
      encodingCcsid_.put("windows-1254", "1254");

      ccsidEncoding_.put("1254", "windows-1254");

      encodingCcsid_.put("ibm-1255", "1255");
      encodingCcsid_.put("ibm1255", "1255");
      encodingCcsid_.put("1255", "1255");
      encodingCcsid_.put("cp1255", "1255");
      encodingCcsid_.put("Cp1255", "1255");
      encodingCcsid_.put("windows-1255", "1255");

      ccsidEncoding_.put("1255", "windows-1255");

      encodingCcsid_.put("ibm-1256", "1256");
      encodingCcsid_.put("1256", "1256");
      encodingCcsid_.put("cp1256", "1256");
      encodingCcsid_.put("Cp1256", "1256");
      encodingCcsid_.put("ibm1256", "1256");
      encodingCcsid_.put("windows-1256", "1256");

      ccsidEncoding_.put("1256", "windows-1256");

      encodingCcsid_.put("cp1257", "1257");
      encodingCcsid_.put("Cp1257", "1257");
      encodingCcsid_.put("cp5353", "1257");
      encodingCcsid_.put("ibm-1257", "1257");
      encodingCcsid_.put("1257", "1257");
      encodingCcsid_.put("ibm1257", "1257");
      encodingCcsid_.put("windows-1257", "1257");

      ccsidEncoding_.put("1257", "windows-1257");

      encodingCcsid_.put("cp1258", "1258");
      encodingCcsid_.put("Cp1258", "1258");
      encodingCcsid_.put("ibm1258", "1258");
      encodingCcsid_.put("ibm-1258", "1258");
      encodingCcsid_.put("1258", "1258");
      encodingCcsid_.put("windows-1258", "1258");

      ccsidEncoding_.put("1258", "windows-1258");

      encodingCcsid_.put("MacRoman",         "1275");
      encodingCcsid_.put("x-MacRoman",         "1275");
      ccsidEncoding_.put("1275","x-MacRoman");
      encodingCcsid_.put("MacGreek",         "1280");
      encodingCcsid_.put("x-MacGreek",         "1280");
      ccsidEncoding_.put("1280", "x-MacGreek");
      encodingCcsid_.put("MacTurkish",       "1281");
      encodingCcsid_.put("x-MacTurkish",       "1281");
      ccsidEncoding_.put("1281","x-MacTurkish");
      encodingCcsid_.put("MacCentralEurope", "1282");
      encodingCcsid_.put("x-MacCentralEurope", "1282");
      ccsidEncoding_.put("1282","x-MacCentralEurope");
      encodingCcsid_.put("MacCyrillic",      "1283");
      encodingCcsid_.put("x-MacCyrillic",      "1283");
      ccsidEncoding_.put("1283","x-MacCyrillic");
      encodingCcsid_.put("MacCroatian",      "1284");
      encodingCcsid_.put("x-MacCroatian",      "1284");
      ccsidEncoding_.put("1284","x-MacCroatian");
      encodingCcsid_.put("MacRomania",       "1285");
      encodingCcsid_.put("x-MacRomania",       "1285");
      ccsidEncoding_.put("1285","x-MacRomania");
      encodingCcsid_.put("MacIceland",       "1286");
      encodingCcsid_.put("x-MacIceland",       "1286");
      ccsidEncoding_.put("1286","x-MacIceland");

      
      
      
      
      encodingCcsid_.put("cp1364", "1364");
      encodingCcsid_.put("ibm1364", "1364");
      encodingCcsid_.put("ibm-1364", "1364");
      encodingCcsid_.put("1364", "1364");
      encodingCcsid_.put("x-IBM1364", "1364");

      ccsidEncoding_.put("1364", "x-IBM1364");

      encodingCcsid_.put("ibm1371", "1371");
      encodingCcsid_.put("ibm-9563", "1371");
      encodingCcsid_.put("cp9563", "1371");
      encodingCcsid_.put("9563", "1371");
      encodingCcsid_.put("x-ibm9563", "1371");
      encodingCcsid_.put("cp1371", "1371");
      encodingCcsid_.put("ibm-1371", "1371");
      encodingCcsid_.put("1371", "1371");
      encodingCcsid_.put("ibm9563", "1371");
      encodingCcsid_.put("x-IBM1371", "1371");

      ccsidEncoding_.put("1371", "x-IBM1371");

      encodingCcsid_.put("ibm-1377", "1377");
      encodingCcsid_.put("1377", "1377");
      encodingCcsid_.put("cp1377", "1377");
      encodingCcsid_.put("Cp1377", "1377");
      encodingCcsid_.put("ibm1377", "1377");
      encodingCcsid_.put("x-IBM1377", "1377");

      ccsidEncoding_.put("1377", "x-IBM1377");

      encodingCcsid_.put("cp1379", "1379");
      encodingCcsid_.put("ibm1379", "1379");
      encodingCcsid_.put("ibm-1379", "1379");
      encodingCcsid_.put("1379", "1379");
      encodingCcsid_.put("x-IBM1379", "1379");

      ccsidEncoding_.put("1379", "x-IBM1379");

      encodingCcsid_.put("cp1381", "1381");
      encodingCcsid_.put("Cp1381", "1381");
      encodingCcsid_.put("ibm-1381", "1381");
      encodingCcsid_.put("1381", "1381");
      encodingCcsid_.put("ibm1381", "1381");
      encodingCcsid_.put("GB2312", "1381");
      encodingCcsid_.put("euc-cn", "1381");
      encodingCcsid_.put("x-euc_cn", "1381");
      encodingCcsid_.put("gb2312", "1381");
      encodingCcsid_.put("x-euc-cn", "1381");
      encodingCcsid_.put("euccn", "1381");
      encodingCcsid_.put("gb2312-80", "1381");
      encodingCcsid_.put("euc_cn", "1381");
      encodingCcsid_.put("gb2312-1980", "1381");
      encodingCcsid_.put("x-IBM1381", "1381");

      ccsidEncoding_.put("1381", "x-IBM1381");

      encodingCcsid_.put("ibm1383", "1383");
      encodingCcsid_.put("ibm-1383", "1383");
      encodingCcsid_.put("1383", "1383");
      encodingCcsid_.put("cp1383", "1383");
      encodingCcsid_.put("Cp1383", "1383");
      encodingCcsid_.put("cpeuccn", "1383");
      encodingCcsid_.put("ibm-euccn", "1383");
      encodingCcsid_.put("ibmeuccn", "1383");
      encodingCcsid_.put("x-IBM1383", "1383");

      ccsidEncoding_.put("1383", "x-IBM1383");

      encodingCcsid_.put("cp936", "1386");
      encodingCcsid_.put("ibm936", "1386");
      encodingCcsid_.put("936", "1386");
      encodingCcsid_.put("ibm-936", "1386");
      encodingCcsid_.put("GBK", "1386");

      ccsidEncoding_.put("1386", "GBK");

      encodingCcsid_.put("ibm-1388", "1388");
      encodingCcsid_.put("1388", "1388");
      encodingCcsid_.put("cp1388", "1388");
      encodingCcsid_.put("ibm1388", "1388");
      encodingCcsid_.put("x-IBM1388", "1388");

      ccsidEncoding_.put("1388", "x-IBM1388");

      encodingCcsid_.put("windows-54936", "1392");
      encodingCcsid_.put("cp1392", "1392");
      encodingCcsid_.put("ibm-1392", "1392");
      encodingCcsid_.put("1392", "1392");
      encodingCcsid_.put("gb18030-2022", "1392");
      encodingCcsid_.put("ibm1392", "1392");
      encodingCcsid_.put("GB18030", "1392");

      ccsidEncoding_.put("1392", "GB18030");

      encodingCcsid_.put("ibm-1399", "1399");
      encodingCcsid_.put("1399", "1399");
      encodingCcsid_.put("cp1399", "1399");
      encodingCcsid_.put("ibm1399", "1399");
      encodingCcsid_.put("x-IBM1399", "1399");

      ccsidEncoding_.put("1399", "x-IBM1399");

      encodingCcsid_.put("cp4933", "4933");
      encodingCcsid_.put("ibm4933", "4933");
      encodingCcsid_.put("4933", "4933");
      encodingCcsid_.put("ibm-4933", "4933");
      encodingCcsid_.put("x-IBM4933", "4933");

      ccsidEncoding_.put("4933", "x-IBM4933");

      encodingCcsid_.put("ibm5026", "5026");
      encodingCcsid_.put("5026", "5026");
      encodingCcsid_.put("ibm-5026", "5026");
      encodingCcsid_.put("Cp5026", "5026");
      encodingCcsid_.put("x-IBM5026", "5026");
      
      ccsidEncoding_.put("5026", "x-IBM5026");

      encodingCcsid_.put("ibm5035", "5035");
      encodingCcsid_.put("ibm-5035", "5035");
      encodingCcsid_.put("5035", "5035");
      encodingCcsid_.put("cp5035", "5035");
      encodingCcsid_.put("x-IBM5035", "5035");

      ccsidEncoding_.put("5035", "x-IBM5035");

      encodingCcsid_.put("csjisencoding", "5054");
      encodingCcsid_.put("iso2022jp", "5054");
      encodingCcsid_.put("ISO2022JP", "5054");
      encodingCcsid_.put("jis_encoding", "5054");
      encodingCcsid_.put("jis", "5054");
      encodingCcsid_.put("csISO2022JP", "5054");
      encodingCcsid_.put("iso2022-jp", "5054");
      encodingCcsid_.put("jis-encoding", "5054");
      encodingCcsid_.put("ISO-2022-JP", "5054");

      ccsidEncoding_.put("5054", "ISO-2022-JP");

      encodingCcsid_.put("UTF_16", "13488");
      encodingCcsid_.put("unicode", "13488");
      encodingCcsid_.put("utf16", "13488");
      encodingCcsid_.put("UnicodeBig", "13488");
      encodingCcsid_.put("UCS-2", "13488");
      encodingCcsid_.put("UTF-16", "13488");

      ccsidEncoding_.put("13488", "UTF-16");

      encodingCcsid_.put("cp16684", "16684");
      encodingCcsid_.put("ibm16684", "16684");
      encodingCcsid_.put("ibm-16684", "16684");
      encodingCcsid_.put("16684", "16684");
      encodingCcsid_.put("x-IBM16684", "16684");

      ccsidEncoding_.put("16684", "x-IBM16684");

      encodingCcsid_.put("iso-2022-kr", "25546");
      encodingCcsid_.put("csISO2022KR", "25546");
      encodingCcsid_.put("ISO2022KR", "25546");
      encodingCcsid_.put("iso2022-kr", "25546");
      encodingCcsid_.put("ISO-2022-KR", "25546");

      ccsidEncoding_.put("25546", "ISO-2022-KR");

      encodingCcsid_.put("33722", "33722");
      encodingCcsid_.put("cp5050", "33722");
      encodingCcsid_.put("ibm-33722", "33722");
      encodingCcsid_.put("cp33722", "33722");
      encodingCcsid_.put("Cp33722", "33722");
      encodingCcsid_.put("ibm33722", "33722");
      encodingCcsid_.put("ibm-33722_vascii_vpua", "33722");
      encodingCcsid_.put("ibm5050", "33722");
      encodingCcsid_.put("ibm-5050", "33722");
      encodingCcsid_.put("5050", "33722");
      encodingCcsid_.put("x-IBM33722", "33722");

      ccsidEncoding_.put("33722", "x-IBM33722");

        ccsidEncoding_.put("17584", "UTF-16BE"); // IBM i doesn't support this, but other people use it.


    }



    // localeNlvMap.
    static
    {
        // 74 entries.
        localeNlvMap_.put("ar", "2954");
        localeNlvMap_.put("ar_SA", "2954");
        localeNlvMap_.put("be", "2979");
        localeNlvMap_.put("bg", "2974");
        localeNlvMap_.put("ca", "2931");
        localeNlvMap_.put("cs", "2975");
        localeNlvMap_.put("da", "2926");
        localeNlvMap_.put("de", "2929");
        localeNlvMap_.put("de_CH", "2939");
        localeNlvMap_.put("de_DE", "2929");
        localeNlvMap_.put("el", "2957");
        localeNlvMap_.put("en", "2924");
        localeNlvMap_.put("en_BE", "2909");
        localeNlvMap_.put("en_CN", "2984");
        localeNlvMap_.put("en_JP", "2938");
        localeNlvMap_.put("en_KR", "2984");
        localeNlvMap_.put("en_SG", "2984");
        localeNlvMap_.put("en_TW", "2984");
        localeNlvMap_.put("es", "2931");
        localeNlvMap_.put("es_ES", "2931");
        localeNlvMap_.put("et", "2902");
        localeNlvMap_.put("fa", "2998");
        localeNlvMap_.put("fi", "2925");
        localeNlvMap_.put("fr", "2928");
        localeNlvMap_.put("fr_BE", "2966");
        localeNlvMap_.put("fr_CA", "2981");
        localeNlvMap_.put("fr_CH", "2940");
        localeNlvMap_.put("fr_FR", "2928");
        localeNlvMap_.put("hr", "2912");
        localeNlvMap_.put("hu", "2976");
        localeNlvMap_.put("is", "2958");
        localeNlvMap_.put("it", "2932");
        localeNlvMap_.put("it_CH", "2942");
        localeNlvMap_.put("iw", "2961");
        localeNlvMap_.put("ja", "2962");
        localeNlvMap_.put("ji", "2961");
        localeNlvMap_.put("ka", "2979");
        localeNlvMap_.put("kk", "2979");
        localeNlvMap_.put("ko", "2986");
        localeNlvMap_.put("ko_KR", "2986");
        localeNlvMap_.put("lo", "2906");
        localeNlvMap_.put("lt", "2903");
        localeNlvMap_.put("lv", "2904");
        localeNlvMap_.put("mk", "2913");
        localeNlvMap_.put("nl", "2923");
        localeNlvMap_.put("nl_BE", "2963");
        localeNlvMap_.put("nl_NL", "2923");
        localeNlvMap_.put("no", "2933");
        localeNlvMap_.put("pl", "2978");
        localeNlvMap_.put("pt", "2996");
        localeNlvMap_.put("pt_BR", "2980");
        localeNlvMap_.put("pt_PT", "2922");
        localeNlvMap_.put("ro", "2992");
        localeNlvMap_.put("ru", "2979");
        localeNlvMap_.put("sh", "2912");
        localeNlvMap_.put("sk", "2994");
        localeNlvMap_.put("sl", "2911");
        localeNlvMap_.put("sq", "2995");
        localeNlvMap_.put("sr", "2914");
        localeNlvMap_.put("sv", "2937");
        localeNlvMap_.put("sv_SE", "2937");
        localeNlvMap_.put("th", "2972");
        localeNlvMap_.put("th_TH", "2972");
        localeNlvMap_.put("tr", "2956");
        localeNlvMap_.put("uk", "2979");
        localeNlvMap_.put("uz", "2979");
        localeNlvMap_.put("vi", "2905");
        localeNlvMap_.put("zh", "2989");
        localeNlvMap_.put("zh_CN", "2989");
        localeNlvMap_.put("zh_HK", "2987");
        localeNlvMap_.put("zh_SG", "2989");
        localeNlvMap_.put("zh_TW", "2987");
        localeNlvMap_.put("cht", "2987");  // Chinese/Taiwan
        localeNlvMap_.put("cht_CN", "2987");  // Chinese/Taiwan
    }

    // localeCcsidMap.
    // Make sure that we have a hardcoded ConvTable for each of the ccsids in this map.
    static
    {
        // 87 entries.
        localeCcsidMap_.put("ar", "420");
        localeCcsidMap_.put("ar_SA", "420");
        localeCcsidMap_.put("be", "1025");
        localeCcsidMap_.put("bg", "1025");
        localeCcsidMap_.put("ca", "284");
        localeCcsidMap_.put("cs", "870");
        localeCcsidMap_.put("da", "277");
        localeCcsidMap_.put("de", "273");
        localeCcsidMap_.put("de_CH", "500");
        localeCcsidMap_.put("de_DE", "273");
        localeCcsidMap_.put("de_AT_EURO", "1141");
        localeCcsidMap_.put("de_DE_EURO", "1141");
        localeCcsidMap_.put("de_LU_EURO", "1141");
        localeCcsidMap_.put("el", "875");
        localeCcsidMap_.put("el_GR_EURO", "4971");
        localeCcsidMap_.put("en", "37");
        localeCcsidMap_.put("en_BE", "500");
        localeCcsidMap_.put("en_CN", "1388");  // Was 935.
        localeCcsidMap_.put("en_IE_EURO", "1140");
        localeCcsidMap_.put("en_JP", "1399"); // Was 930 Was 5026.
        localeCcsidMap_.put("en_KR", "1364"); // Was 933.
        localeCcsidMap_.put("en_SG", "1388"); // Was 935.
        localeCcsidMap_.put("en_TW", "937");
        localeCcsidMap_.put("es", "284");
        localeCcsidMap_.put("es_ES", "284");
        localeCcsidMap_.put("es_ES_EURO", "1145");
        localeCcsidMap_.put("et", "1122");
        localeCcsidMap_.put("fi", "278");
        localeCcsidMap_.put("fi_FI_EURO", "1143");
        localeCcsidMap_.put("fr", "297");
        localeCcsidMap_.put("fr_BE", "500");
        localeCcsidMap_.put("fr_BE_EURO", "1148");
        localeCcsidMap_.put("fr_CA", "37");
        localeCcsidMap_.put("fr_CH", "500");
        localeCcsidMap_.put("fr_FR", "297");
        localeCcsidMap_.put("fr_FR_EURO", "1147");
        localeCcsidMap_.put("fr_LU_EURO", "1147");
        localeCcsidMap_.put("hr", "870");
        localeCcsidMap_.put("hu", "870");
        localeCcsidMap_.put("is", "871");
        localeCcsidMap_.put("it", "280");
        localeCcsidMap_.put("it_CH", "500");
        localeCcsidMap_.put("it_IT_EURO", "1144");
        localeCcsidMap_.put("iw", "424");
        localeCcsidMap_.put("ja", "1399"); // Was 930 Was 5026.
        localeCcsidMap_.put("ji", "424");
        localeCcsidMap_.put("ka", "1025");
        localeCcsidMap_.put("kk", "1025");
        localeCcsidMap_.put("ko", "1364"); // Was 933.
        localeCcsidMap_.put("ko_KR", "1364");
        localeCcsidMap_.put("lo", "1132"); // Was 1133.
        localeCcsidMap_.put("lt", "1112");
        localeCcsidMap_.put("lv", "1112");
        localeCcsidMap_.put("mk", "1025");
        localeCcsidMap_.put("nl", "37");
        localeCcsidMap_.put("nl_BE", "500");
        localeCcsidMap_.put("nl_NL", "37");
        localeCcsidMap_.put("nl_BE_EURO", "1148");
        localeCcsidMap_.put("nl_NL_EURO", "1140");
        localeCcsidMap_.put("no", "277");
        localeCcsidMap_.put("pl", "870");
        localeCcsidMap_.put("pt", "500");
        localeCcsidMap_.put("pt_BR", "37");
        localeCcsidMap_.put("pt_PT", "37");
        localeCcsidMap_.put("pt_PT_EURO", "1140");
        localeCcsidMap_.put("ro", "870");
        localeCcsidMap_.put("ru", "1025");
        localeCcsidMap_.put("sh", "870");
        localeCcsidMap_.put("sk", "870");
        localeCcsidMap_.put("sl", "870");
        localeCcsidMap_.put("sq", "500");
        localeCcsidMap_.put("sr", "1025");
        localeCcsidMap_.put("sv", "278");
        localeCcsidMap_.put("sv_SE", "278");
        localeCcsidMap_.put("th", "838");
        localeCcsidMap_.put("th_TH", "838");
        localeCcsidMap_.put("tr", "1026");
        localeCcsidMap_.put("uk", "1123"); // Was 1025.
        localeCcsidMap_.put("uz", "1025");
        localeCcsidMap_.put("vi", "1130");
        localeCcsidMap_.put("zh", "1388"); // Was 935.
        localeCcsidMap_.put("zh_CN", "1388");
        localeCcsidMap_.put("zh_HK", "937");
        localeCcsidMap_.put("zh_SG", "1388"); // Was 935.
        localeCcsidMap_.put("zh_TW", "937");
        localeCcsidMap_.put("cht", "937"); // Chinese/Taiwan
        localeCcsidMap_.put("cht_CN", "937"); // Chinese/Taiwan
    }
}
