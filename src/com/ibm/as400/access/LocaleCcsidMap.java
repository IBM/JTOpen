///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: LocaleCcsidMap.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.util.Properties;



class LocaleCcsidMap
extends Properties
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";



    public LocaleCcsidMap ()
    {
        super ();

        // Changes to this list should also be made in
        // LocaleCcsidMap.properties.
        put ("ar", "420");
        put ("be", "1025");
        put ("bg", "1025");
        put ("ca", "284");
        put ("cs", "870");
        put ("da", "277");
        put ("de", "273");
        put ("de_CH", "500");
        put ("el", "875");
        put ("en", "37");
        put ("en_BE", "500");
        put ("en_CN", "935");
        put ("en_JP", "5026");
        put ("en_KR", "933");
        put ("en_SG", "935");
        put ("en_TW", "937");
        put ("es", "284");
        put ("et", "1122");
        put ("fa", "1097");
        put ("fi", "278");
        put ("fr", "297");
        put ("fr_BE", "500");
        put ("fr_CA", "37");
        put ("fr_CH", "500");
        put ("hr", "870");
        put ("hu", "870");
        put ("is", "871");
        put ("it", "280");
        put ("it_CH", "500");
        put ("iw", "424");
        put ("ja", "5026");
        put ("ji", "424");
        put ("ka", "1025");
        put ("kk", "1025");
        put ("ko", "933");
        put ("lo", "1133");
        put ("lt", "1112");
        put ("lv", "1112");
        put ("mk", "1025");
        put ("nl", "37");
        put ("nl_BE", "500");
        put ("no", "277");
        put ("pl", "870");
        put ("pt", "500");
        put ("pt_BR", "37");
        put ("pt_PT", "37");
        put ("ro", "870");
        put ("ru", "1025");
        put ("sh", "870");
        put ("sk", "870");
        put ("sl", "870");
        put ("sq", "500");
        put ("sr", "1025");
        put ("sv", "278");
        put ("th", "838");
        put ("tr", "1026");
        put ("uk", "1025");
        put ("uz", "1025");
        put ("vi", "1130");
        put ("zh", "935");
        put ("zh_HK", "937");
        put ("zh_SG", "935");
        put ("zh_TW", "937");
    }


    
}


