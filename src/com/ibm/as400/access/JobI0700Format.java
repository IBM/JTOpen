///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: JobI0700Format.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

class JobI0700Format extends JobFormat
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

  JobI0700Format(AS400 sys)
  {
    super(sys);
    setName("JOBI0700");
    addChar(2, "reserved");
    addBin4("numberOfLibrariesInSYSLIBL");
    addBin4("numberOfProductLibraries");
    addBin4("currentLibraryExistence");
    addBin4("numberOfLibrariesInUSRLIBL");
   
    // There are 4 more fields that belong to this format:
    
    // systemLibraryList is an ARRAY(*) of CHAR(11) fields
    // productLibraries list is an ARRAY(*) of CHAR(11) fields
    // currentLibrary is an ARRAY(*) of CHAR(11) fields
    // userLibraryList is an ARRAY(*) of CHAR(11) fields
    
    // The number of actual elements in each ARRAY(*) is the
    // number in the corresponding BIN4 field described above.
    // The max number in the systemLibraryList is 15.
    // The max number in the productLibraries is 2.
    // The max number in the currentLibrary is 1.
    // The max number in the userLibraryList is 25.
    // ==> Total number of libraries possible is 43.
    // So we create a large enough record format to hold all
    // of them. 43*11 = 473.
    
    // The number of libraries will normally not fill this
    // entire buffer, and could even be 0. The individual library
    // names will need to be parsed out of this field at runtime.    
    
    addChar(473, "libraryList");

  }
}  
