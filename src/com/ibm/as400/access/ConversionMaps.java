///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: ConversionMaps.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2002 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.util.Enumeration;
import java.util.Hashtable;

abstract class ConversionMaps
{
  private static final String copyright = "Copyright (C) 1997-2002 International Business Machines Corporation and others.";

  // To prevent rehashing of a Hashtable as it's being built, we construct it with
  // a certain number of entries. However, the default load factor for a table is .75, 
  // meaning that when it becomes 3/4 full, the table is automatically increased in size
  // and rehashed. We could specify a load factor of 1.0 on the constructor, since that
  // would efficiently utilize memory and prevent rehashing, but we might get ineffective
  // hashing since the algorithm might not be good, so instead, we leave the load factor
  // at the default .75 and just use a size of hash table big enough to accommodate the
  // number of entries we have without rehashing. This is a pretty good tradeoff between
  // lookup time and memory footprint.
  
  // If we ever pre-req Java 2, we should use HashMaps for these instead of Hashtables.
  // Hashtables are synchronized which makes the table lookup slow, especially if 
  // multiple threads bottleneck on these static tables. HashMaps are not synchronized
  // yet provide the same function.
  
  final static Hashtable ccsidEncoding_ = new Hashtable(175); // 127 actual entries
  final static Hashtable encodingCcsid_ = new Hashtable(175); // 127 actual entries
  final static Hashtable localeCcsidMap_ = new Hashtable(85); // 63 actual entries
  final static Hashtable localeNlvMap_ = new Hashtable(100); // 74 actual entries

  /**
   * Convenience function to get corresponding ccsid number as String.
  **/
  static final String encodingToCcsidString(String encoding)
  {
    return (String)encodingCcsid_.get(encoding);
  }


  /**
   * Convenience function to get corresponding encoding from ccsid.
  **/
  static final String ccsidToEncoding(int ccsid)
  {
    return (String)ccsidEncoding_.get(String.valueOf(ccsid));
  }

    
  //@A4A
  // This is a table that maps all Java encodings to OS/400 CCSIDs.
  // Some encodings could map to more than one CCSID, so they are not included
  // in the table. When a lookup is performed, it will then return null.
  // Some encodings are supported by the iSeries server but not by the Toolbox. The
  // ConvTable code handles this.
  // Based on http://java.sun.com/products/jdk/1.2/docs/guide/internat/encoding.doc.html

  //@C0C
  // V5R1 JVM encodings: http://publib.boulder.ibm.com/pubs/html/as400/v5r1/ic2924/info/rzaha/fileenc.htm
  // V5R1 JVM locales: http://publib.boulder.ibm.com/pubs/html/as400/v5r1/ic2924/info/rzaha/locales.htm
  
  //@C0A - See also http://java.sun.com/j2se/1.4/docs/guide/intl/encoding.doc.html
  // and http://java.sun.com/j2se/1.4/docs/guide/intl/locale.doc.html

  static 
  {
    // 137+ possible Java encodings. 13 have unknown CCSIDs.
    // We have 128 known in this table.
    encodingCcsid_.put("ASCII",         "367"); // ANSI X.34 ASCI
    encodingCcsid_.put("Cp1252",        "1252");
    encodingCcsid_.put("ISO8859_1",     "819");
    encodingCcsid_.put("Unicode",       "13488");
    encodingCcsid_.put("UnicodeBig",    "13488"); // bom is 0xFEFF
//    encodingCcsid_.put("UnicodeBigUnmarked", 13488); //@B0A
    encodingCcsid_.put("UnicodeLittle", "1202"); // bom is 0xFFFE @C1C
//    encodingCcsid_.put("UnicodeLittleUnmarked", 13488); //@B0A
    encodingCcsid_.put("UTF8",          "1208");
    encodingCcsid_.put("UTF-16BE",      "1200"); // @C1A

    encodingCcsid_.put("Big5",      "950");
//    encodingCcsid_.put("Big5 HKSCS", ???); //@B0A: Big5 with Hong Kong extensions
    encodingCcsid_.put("CNS11643",  "964");
    encodingCcsid_.put("Cp037",     "37");
    encodingCcsid_.put("Cp273",     "273");
    encodingCcsid_.put("Cp277",     "277");
    encodingCcsid_.put("Cp278",     "278");
    encodingCcsid_.put("Cp280",     "280");
    encodingCcsid_.put("Cp284",     "284");
    encodingCcsid_.put("Cp285",     "285");
    encodingCcsid_.put("Cp297",     "297");
    encodingCcsid_.put("Cp420",     "420");
    encodingCcsid_.put("Cp424",     "424");
    encodingCcsid_.put("Cp437",     "437");
    encodingCcsid_.put("Cp500",     "500");
    encodingCcsid_.put("Cp737",     "737");
    encodingCcsid_.put("Cp775",     "775");
    encodingCcsid_.put("Cp838",     "838");
    encodingCcsid_.put("Cp850",     "850");
    encodingCcsid_.put("Cp852",     "852");
    encodingCcsid_.put("Cp855",     "855");
    encodingCcsid_.put("Cp856",     "856");
    encodingCcsid_.put("Cp857",     "857");
    encodingCcsid_.put("Cp858",     "858");
    encodingCcsid_.put("Cp860",     "860");
    encodingCcsid_.put("Cp861",     "861");
    encodingCcsid_.put("Cp862",     "862");
    encodingCcsid_.put("Cp863",     "863");
    encodingCcsid_.put("Cp864",     "864");
    encodingCcsid_.put("Cp865",     "865");
    encodingCcsid_.put("Cp866",     "866");
    encodingCcsid_.put("Cp868",     "868");
    encodingCcsid_.put("Cp869",     "869");
    encodingCcsid_.put("Cp870",     "870");
    encodingCcsid_.put("Cp871",     "871");
    encodingCcsid_.put("Cp874",     "874");
    encodingCcsid_.put("Cp875",     "875");
    encodingCcsid_.put("Cp918",     "918");
    encodingCcsid_.put("Cp921",     "921");
    encodingCcsid_.put("Cp922",     "922");
    encodingCcsid_.put("Cp923",     "923"); // IBM Latin-9
    encodingCcsid_.put("Cp930",     "930");
    encodingCcsid_.put("Cp933",     "933");
    encodingCcsid_.put("Cp935",     "935");
    encodingCcsid_.put("Cp937",     "937");
    encodingCcsid_.put("Cp939",     "939");
    encodingCcsid_.put("Cp942",     "942");
//    encodingCcsid_.put("Cp942C",    ???); // Don't know the CCSID - unclear what the 'C' means
    encodingCcsid_.put("Cp943",     "943");
//    encodingCcsid_.put("Cp943C",    ???); // Don't know the CCSID - unclear what the 'C' means
    encodingCcsid_.put("Cp948",     "948");
    encodingCcsid_.put("Cp949",     "949");
//    encodingCcsid_.put("Cp949C",    ???); // Don't know the CCSID - unclear what the 'C' means
    encodingCcsid_.put("Cp950",     "950");
    encodingCcsid_.put("Cp964",     "964");
    encodingCcsid_.put("Cp970",     "970");
    encodingCcsid_.put("Cp1006",   "1006");
    encodingCcsid_.put("Cp1025",   "1025");
    encodingCcsid_.put("Cp1026",   "1026");
    encodingCcsid_.put("Cp1046",   "1046");
    encodingCcsid_.put("Cp1097",   "1097");
    encodingCcsid_.put("Cp1098",   "1098");
    encodingCcsid_.put("Cp1112",   "1112");
    encodingCcsid_.put("Cp1122",   "1122");
    encodingCcsid_.put("Cp1123",   "1123");
    encodingCcsid_.put("Cp1124",   "1124");
    encodingCcsid_.put("Cp1140",   "1140");
    encodingCcsid_.put("Cp1141",   "1141");
    encodingCcsid_.put("Cp1142",   "1142");
    encodingCcsid_.put("Cp1143",   "1143");
    encodingCcsid_.put("Cp1144",   "1144");
    encodingCcsid_.put("Cp1145",   "1145");
    encodingCcsid_.put("Cp1146",   "1146");
    encodingCcsid_.put("Cp1147",   "1147");
    encodingCcsid_.put("Cp1148",   "1148");
    encodingCcsid_.put("Cp1149",   "1149");
    encodingCcsid_.put("Cp1250",   "1250");
    encodingCcsid_.put("Cp1251",   "1251");
    encodingCcsid_.put("Cp1253",   "1253");
    encodingCcsid_.put("Cp1254",   "1254");
    encodingCcsid_.put("Cp1255",   "1255");
    encodingCcsid_.put("Cp1256",   "1256");
    encodingCcsid_.put("Cp1257",   "1257");
    encodingCcsid_.put("Cp1258",   "1258");
    encodingCcsid_.put("Cp1381",   "1381");
    encodingCcsid_.put("Cp1383",   "1383");
    encodingCcsid_.put("Cp33722", "33722");
    
    // The Toolbox does not directly support EUC at this time, Java will do the conversion.
    encodingCcsid_.put("EUC_CN", "1383"); // superset of 5479
    encodingCcsid_.put("EUC_JP", "33722");
    encodingCcsid_.put("EUC_KR", "970"); // superset of 5066
    encodingCcsid_.put("EUC_TW", "964"); // superset of 5060
    
    encodingCcsid_.put("GB2312", "1381");
    encodingCcsid_.put("GB18030", "1392"); //@B0A: 1392 is mixed 4-byte; the individual component CCSIDs are not supported
    encodingCcsid_.put("GBK",    "1386");
    
//    encodingCcsid_.put("ISCII91", ???); //@B0A: Indic scripts
 
    // The Toolbox does not directly support ISO2022
//    encodingCcsid_.put("ISO2022CN",     ???); // Not sure of the CCSID, possibly 9575?
//    encodingCcsid_.put("ISO2022CN_CNS", "965"); // Java doesn't support this one?
//    encodingCcsid_.put("ISO2022CN_GB",  "9575"); // Java doesn't support this one?
    
    encodingCcsid_.put("ISO2022JP", "5054"); // Could be 956 also, but the OS/400 JVM uses 5054.
    encodingCcsid_.put("ISO2022KR", "25546"); // Could be 17354 also, but the OS/400 JVM uses 25546.
    
    encodingCcsid_.put("ISO8859_2", "912");
    encodingCcsid_.put("ISO8859_3", "913");
    encodingCcsid_.put("ISO8859_4", "914");
    encodingCcsid_.put("ISO8859_5", "915");
    encodingCcsid_.put("ISO8859_6", "1089");
    encodingCcsid_.put("ISO8859_7", "813");
    encodingCcsid_.put("ISO8859_8", "916");
    encodingCcsid_.put("ISO8859_9", "920");
//  encodingCcsid_.put("ISO8859_13", ???); //@B0A: Latin alphabet No. 7
//    encodingCcsid_.put("ISO8859_15_FDIS", ???); // Don't know the CCSID; FYI, this codepage is ISO 28605.
  
    // The Toolbox does not directly support JIS
    encodingCcsid_.put("JIS0201",       "897"); // Could be 895, but the OS/400 JVM uses 897.
    encodingCcsid_.put("JIS0208",       "952");
    encodingCcsid_.put("JIS0212",       "953");
//    encodingCcsid_.put("JISAutoDetect", ???); // Can't do this one. Would need to look at the bytes to determine the CCSID.
    
    encodingCcsid_.put("Johab",  "1363");
    encodingCcsid_.put("KOI8_R", "878");
    encodingCcsid_.put("KSC5601", "949");
    
    encodingCcsid_.put("MS874", "874");
    encodingCcsid_.put("MS932", "943");
    encodingCcsid_.put("MS936", "1386");
    encodingCcsid_.put("MS949", "949");
    encodingCcsid_.put("MS950", "950");
    
//    encodingCcsid_.put("MacArabic", ???); // Don't know.
    encodingCcsid_.put("MacCentralEurope", "1282");
    encodingCcsid_.put("MacCroatian", "1284");
    encodingCcsid_.put("MacCyrillic", "1283");
//    encodingCcsid_.put("MacDingbat", ???); // Don't know.
    encodingCcsid_.put("MacGreek", "1280");
//    encodingCcsid_.put("MacHebrew", ???); // Don't know.
    encodingCcsid_.put("MacIceland", "1286");
    encodingCcsid_.put("MacRoman", "1275");
    encodingCcsid_.put("MacRomania", "1285");
//    encodingCcsid_.put("MacSymbol", ???); // Don't know.
//    encodingCcsid_.put("MacThai", ???); // Don't know.
    encodingCcsid_.put("MacTurkish", "1281");
//    encodingCcsid_.put("MacUkraine", ???); // Don't know.

    encodingCcsid_.put("SJIS", "932"); // Could be 943, but the OS/400 JVM uses 932.
    encodingCcsid_.put("TIS620", "874"); // OS/400 JVM uses 874.
  }
    
  
  //@A5:
  // With the encodingCcsid map, we try to assign a CCSID to every
  // Java encoding. With the ccsidEncoding map, the reverse happens...
  // we try to assign an encoding to each CCSID. This is not always 
  // possible, so some CCSIDs may have a null encoding. Also, some
  // CCSIDs may have multiple Java encodings, in this case the first one wins.
  static
  {
    // Build the CCSID to encoding map
    
    //@A5D - previous list of ccsids here
    
    //@A5A
    for (Enumeration keys = encodingCcsid_.keys(); keys.hasMoreElements(); )
    {
      Object key = keys.nextElement();
      ccsidEncoding_.put(encodingCcsid_.get(key), key);
    }
    
    ccsidEncoding_.put("17584", "UTF-16BE"); // iSeries doesn't support this, but other people use it.
    
    // Any other ccsids that are used for which we know no encoding will
    // have their encoding set to equal their ccsid.
    
    // Any encodings that are used for which we know no ccsid will 
    // have their ccsid set to 0.
  }


  // localeNlvMap
  static
  {
    // 63 entries.
    localeNlvMap_.put("ar", "2954");
    localeNlvMap_.put("be", "2979");
    localeNlvMap_.put("bg", "2974");
    localeNlvMap_.put("ca", "2931");
    localeNlvMap_.put("cs", "2975");
    localeNlvMap_.put("da", "2926");
    localeNlvMap_.put("de", "2929");
    localeNlvMap_.put("de_CH", "2939");
    localeNlvMap_.put("el", "2957");
    localeNlvMap_.put("en", "2924");
    localeNlvMap_.put("en_BE", "2909");
    localeNlvMap_.put("en_CN", "2984");
    localeNlvMap_.put("en_JP", "2938");
    localeNlvMap_.put("en_KR", "2984");
    localeNlvMap_.put("en_SG", "2984");
    localeNlvMap_.put("en_TW", "2984");
    localeNlvMap_.put("es", "2931");
    localeNlvMap_.put("et", "2902");
    localeNlvMap_.put("fa", "2998");
    localeNlvMap_.put("fi", "2925");
    localeNlvMap_.put("fr", "2928");
    localeNlvMap_.put("fr_BE", "2966");
    localeNlvMap_.put("fr_CA", "2981");
    localeNlvMap_.put("fr_CH", "2940");
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
    localeNlvMap_.put("lo", "2906");
    localeNlvMap_.put("lt", "2903");
    localeNlvMap_.put("lv", "2904");
    localeNlvMap_.put("mk", "2913");
    localeNlvMap_.put("nl", "2923");
    localeNlvMap_.put("nl_BE", "2963");
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
    localeNlvMap_.put("th", "2972");
    localeNlvMap_.put("tr", "2956");
    localeNlvMap_.put("uk", "2979");
    localeNlvMap_.put("uz", "2979");
    localeNlvMap_.put("vi", "2905");
    localeNlvMap_.put("zh", "2989");
    localeNlvMap_.put("zh_HK", "2987");
    localeNlvMap_.put("zh_SG", "2989");
    localeNlvMap_.put("zh_TW", "2987");
  }

  
  // localeCcsidMap
  // Make sure that we have a hardcoded ConvTable for
  // each of the ccsids in this map.
  static
  {
    // 74 entries.
    localeCcsidMap_.put("ar", "420");
    localeCcsidMap_.put("be", "1025");
    localeCcsidMap_.put("bg", "1025");
    localeCcsidMap_.put("ca", "284");
    localeCcsidMap_.put("cs", "870");
    localeCcsidMap_.put("da", "277");
    localeCcsidMap_.put("de", "273");
    localeCcsidMap_.put("de_CH", "500");
    localeCcsidMap_.put("de_AT_EURO", "1141"); //@A4A
    localeCcsidMap_.put("de_DE_EURO", "1141"); //@A4A
    localeCcsidMap_.put("de_LU_EURO", "1141"); //@A4A
    localeCcsidMap_.put("el", "875");
    localeCcsidMap_.put("el_GR_EURO", "4971"); //@C0A
    localeCcsidMap_.put("en", "37");
    localeCcsidMap_.put("en_BE", "500");
    localeCcsidMap_.put("en_CN", "1388"); //@A1C - was 935
    localeCcsidMap_.put("en_IE_EURO", "1140"); //@A4A
    localeCcsidMap_.put("en_JP", "1399"); //@A4C - was 930 @A1C - was 5026
    localeCcsidMap_.put("en_KR", "1364"); //@A1C - was 933
    localeCcsidMap_.put("en_SG", "1388"); //@A1C - was 935
    localeCcsidMap_.put("en_TW", "937");
    localeCcsidMap_.put("es", "284");
    localeCcsidMap_.put("es_ES_EURO", "1145"); //@A4A
    localeCcsidMap_.put("et", "1122");
    localeCcsidMap_.put("fi", "278");
    localeCcsidMap_.put("fi_FI_EURO", "1143"); //@A4A
    localeCcsidMap_.put("fr", "297");
    localeCcsidMap_.put("fr_BE", "500");
    localeCcsidMap_.put("fr_BE_EURO", "1148"); //@A4A
    localeCcsidMap_.put("fr_CA", "37");
    localeCcsidMap_.put("fr_CH", "500");
    localeCcsidMap_.put("fr_FR_EURO", "1147"); //@A4A
    localeCcsidMap_.put("fr_LU_EURO", "1147"); //@A4A
    localeCcsidMap_.put("hr", "870");
    localeCcsidMap_.put("hu", "870");
    localeCcsidMap_.put("is", "871");
    localeCcsidMap_.put("it", "280");
    localeCcsidMap_.put("it_CH", "500");
    localeCcsidMap_.put("it_IT_EURO", "1144"); //@C0A
    localeCcsidMap_.put("iw", "424");
    localeCcsidMap_.put("ja", "1399"); //@A4C - was 930 @A1C - was 5026
    localeCcsidMap_.put("ji", "424");
    localeCcsidMap_.put("ka", "1025");
    localeCcsidMap_.put("kk", "1025");
    localeCcsidMap_.put("ko", "1364"); //@A1C - was 933
    localeCcsidMap_.put("lo", "1132"); //@A3C - was 1133
    localeCcsidMap_.put("lt", "1112");
    localeCcsidMap_.put("lv", "1112");
    localeCcsidMap_.put("mk", "1025");
    localeCcsidMap_.put("nl", "37");
    localeCcsidMap_.put("nl_BE", "500");
    localeCcsidMap_.put("nl_BE_EURO", "1148"); //@A4A
    localeCcsidMap_.put("nl_NL_EURO", "1140"); //@A4A
    localeCcsidMap_.put("no", "277");
    localeCcsidMap_.put("pl", "870");
    localeCcsidMap_.put("pt", "500");
    localeCcsidMap_.put("pt_BR", "37");
    localeCcsidMap_.put("pt_PT", "37");
    localeCcsidMap_.put("pt_PT_EURO", "1140"); //@A4A
    localeCcsidMap_.put("ro", "870");
    localeCcsidMap_.put("ru", "1025");
    localeCcsidMap_.put("sh", "870");
    localeCcsidMap_.put("sk", "870");
    localeCcsidMap_.put("sl", "870");
    localeCcsidMap_.put("sq", "500");
    localeCcsidMap_.put("sr", "1025");
    localeCcsidMap_.put("sv", "278");
    localeCcsidMap_.put("th", "838");
    localeCcsidMap_.put("tr", "1026");
    localeCcsidMap_.put("uk", "1123"); //@A1C - was 1025
    localeCcsidMap_.put("uz", "1025");
    localeCcsidMap_.put("vi", "1130");
    localeCcsidMap_.put("zh", "1388"); //@A1C - was 935
    localeCcsidMap_.put("zh_HK", "937");
    localeCcsidMap_.put("zh_SG", "1388"); //@A1C - was 935
    localeCcsidMap_.put("zh_TW", "937");
  }    
}
