package com.ibm.as400.security;

///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: SecurityMRI_en.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2003 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

/**
 * Defines locale-specific objects for security-related * components of the IBM Toolbox for Java.
 **/
//
// Implementation note:
//
// This class is not really necessary.  It exists to enhance performance.
// When Java searches for a resource bundle, it searches for a locale-
// specific resource bundle first, then more general resource bundles.
// By creating this subclass of the general resource bundle, we prevent
// Java from having to load multiple resource bundles.
//
public class SecurityMRI_en extends SecurityMRI {

    private static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";
}
