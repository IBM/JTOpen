///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: JDMRI_en.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.util.*;

/**
Locale-specific objects for the AS/400 Toolbox for Java.
**/
//
// Implementation note:
//
// This class is not really necessary.  It exists to enhance performance.
// When Java searches for a resource bundle, it searches for a locale-
// specific resource bundle first, then more general resource bundles.
// By creating this subclass of the general resource bundle, we prevent
// Java from having to load multiple resource bundles.  This performance
// boost will be most noticeable for applets.
//

public class JDMRI_en extends JDMRI
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";



}

