///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: CcsidEncodingMap.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.util.Enumeration;
import java.util.Properties;

// Maps CCSID <--> Encoding
class CcsidEncodingMap
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    static private String Copyright()
    {
	return Copyright.copyright;
    }

    private static Properties ccsidEncoding = new Properties();  // CCSID to encoding map
    private static Properties encodingCcsid = new Properties();  // encoding to CCSID map
    static
    {
	// Build the CCSID to encoding map
	ccsidEncoding.put("37", "Cp037");
	ccsidEncoding.put("273", "Cp273");
	ccsidEncoding.put("277", "Cp277");
	ccsidEncoding.put("278", "Cp278");
	ccsidEncoding.put("280", "Cp280");
	ccsidEncoding.put("284", "Cp284");
	ccsidEncoding.put("285", "Cp285");
	ccsidEncoding.put("290", "290");
	ccsidEncoding.put("297", "Cp297");
	ccsidEncoding.put("300", "300");
	ccsidEncoding.put("420", "Cp420");
	ccsidEncoding.put("423", "423");
	ccsidEncoding.put("424", "Cp424");
	ccsidEncoding.put("437", "Cp437");
	ccsidEncoding.put("500", "Cp500");
	ccsidEncoding.put("737", "Cp737");
	ccsidEncoding.put("775", "Cp775");
	ccsidEncoding.put("813", "8859_7");
	ccsidEncoding.put("819", "8859_1");
	ccsidEncoding.put("833", "833");
	ccsidEncoding.put("834", "834");
	ccsidEncoding.put("835", "835");
	ccsidEncoding.put("836", "836");
	ccsidEncoding.put("837", "837");
	ccsidEncoding.put("838", "Cp838");
	ccsidEncoding.put("850", "Cp850");
	ccsidEncoding.put("852", "Cp852");
	ccsidEncoding.put("855", "Cp855");
	ccsidEncoding.put("856", "Cp856");
	ccsidEncoding.put("857", "Cp857");
	ccsidEncoding.put("860", "Cp860");
	ccsidEncoding.put("861", "Cp861");
	ccsidEncoding.put("862", "Cp862");
	ccsidEncoding.put("863", "Cp863");
	ccsidEncoding.put("864", "Cp864");
	ccsidEncoding.put("865", "Cp865");
	ccsidEncoding.put("866", "Cp866");
	ccsidEncoding.put("868", "Cp868");
	ccsidEncoding.put("869", "Cp869");
	ccsidEncoding.put("870", "Cp870");
	ccsidEncoding.put("871", "Cp871");
	ccsidEncoding.put("874", "Cp874");
	ccsidEncoding.put("875", "Cp875");
	ccsidEncoding.put("880", "880");
	ccsidEncoding.put("912", "8859_2");
	ccsidEncoding.put("913", "8859_3");
	ccsidEncoding.put("914", "8859_4");
	ccsidEncoding.put("915", "8859_5");
	ccsidEncoding.put("916", "8859_8");
	ccsidEncoding.put("918", "Cp918");
	ccsidEncoding.put("920", "8859_9");
	ccsidEncoding.put("921", "Cp921");
	ccsidEncoding.put("922", "Cp922");
	ccsidEncoding.put("930", "Cp930");
	ccsidEncoding.put("933", "Cp933");
	ccsidEncoding.put("935", "Cp935");
	ccsidEncoding.put("937", "Cp937");
	ccsidEncoding.put("939", "Cp939");
	ccsidEncoding.put("942", "Cp942");
	ccsidEncoding.put("943", "JIS");
	ccsidEncoding.put("948", "Cp948");
	ccsidEncoding.put("949", "Cp949");
	ccsidEncoding.put("950", "Cp950");
	ccsidEncoding.put("954", "EUCJIS");
	ccsidEncoding.put("964", "Cp964");
	ccsidEncoding.put("970", "Cp970");
	ccsidEncoding.put("1006", "Cp1006");
	ccsidEncoding.put("1025", "Cp1025");
	ccsidEncoding.put("1026", "Cp1026");
	ccsidEncoding.put("1027", "1027");
	ccsidEncoding.put("1046", "Cp1046");
	ccsidEncoding.put("1089", "8859_6");
	ccsidEncoding.put("1097", "Cp1097");
	ccsidEncoding.put("1098", "Cp1098");
	ccsidEncoding.put("1112", "Cp1112");
	ccsidEncoding.put("1122", "Cp1122");
	ccsidEncoding.put("1123", "Cp1123");
	ccsidEncoding.put("1124", "Cp1124");
	ccsidEncoding.put("1130", "1130");
	ccsidEncoding.put("1132", "1132");
	ccsidEncoding.put("1250", "Cp1250");
	ccsidEncoding.put("1251", "Cp1251");
	ccsidEncoding.put("1252", "Cp1252");
	ccsidEncoding.put("1253", "Cp1253");
	ccsidEncoding.put("1254", "Cp1254");
	ccsidEncoding.put("1255", "Cp1255");
	ccsidEncoding.put("1256", "Cp1256");
	ccsidEncoding.put("1257", "Cp1257");
	ccsidEncoding.put("1258", "Cp1258");
	ccsidEncoding.put("1275", "MacRoman");
	ccsidEncoding.put("1280", "MacGreek");
	ccsidEncoding.put("1281", "MacTurkish");
	ccsidEncoding.put("1282", "MacCentralEurope");
	ccsidEncoding.put("1283", "MacCyrillic");
	ccsidEncoding.put("1350", "EUCJIS");
	ccsidEncoding.put("1381", "Cp1381");
	ccsidEncoding.put("1383", "Cp1383");
	ccsidEncoding.put("1388", "1388");
	ccsidEncoding.put("4396", "4396");
	ccsidEncoding.put("4933", "4933");
	ccsidEncoding.put("5026", "5026");
	ccsidEncoding.put("5035", "5035");
	ccsidEncoding.put("13488", "13488");
	ccsidEncoding.put("28709", "28709");
	ccsidEncoding.put("33722", "Cp33722");
	ccsidEncoding.put("61952", "61952");

        // build encodingCcsid as reverse of ccsidEncoding
	for (Enumeration keys = ccsidEncoding.propertyNames(); keys.hasMoreElements(); )
	{
	    String key = (String)keys.nextElement();
	    String value = ccsidEncoding.getProperty(key);
	    encodingCcsid.put(value, key);
	}
    }

    // Convenience function to get corresponding ccsid number as String
    static String encodingToCcidString(String encoding)
    {
	return encodingCcsid.getProperty(encoding);
    }

    // Convenience function to get corresponding encoding from ccsid
    static String ccsidToEncoding(int ccsid)
    {
	return ccsidEncoding.getProperty(String.valueOf(ccsid));
    }

    private CcsidEncodingMap ()
    {
    }
}
