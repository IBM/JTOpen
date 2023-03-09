///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: SVMRI_en.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2003 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////
package com.ibm.as400.access;

/**
 * An empty English MRI resource bundle.
 * <P>
 * Normally we do not need _en or _en_US bundles
 * since the default bundle contains English.
 * However, in the case of SVMRI, since the SystemValue
 * classes let you specify a Locale used to load their MRI,
 * the following scenario exists:
 * <P>
 * The default Locale of the JVM is (for example) Korean.
 * The requested Locale passed into SystemValue is English.
 * Java will attempt to load the English MRI bundle. If that
 * bundle does not exist, it will attempt to load the default
 * Locale's bundle, in this case Korean. If the Korean bundle
 * does exist, the user will get Korean Strings instead of
 * English, even though they requested English and English
 * is available. So in this case, we provide an English bundle.
 * We just provide an empty bundle, so we don't need to instantiate
 * the base English Strings twice.
 * <P>
 * We do not need _en bundles for our other MRI because there
 * is currently not a way for a user to override the Locale
 * we are using to load the MRI, unless they change the default
 * Locale of the JVM (before our ResourceBundleLoader is first
 * touched, since it loads our MRI statically). That is, for
 * our other MRI bundles, if the user wants English MRI, they
 * will have to switch the JVM's default Locale to be English.
**/
public class SVMRI_en extends java.util.ListResourceBundle
{
  private static final String copyright = "Copyright (C) 1997-2003 International Business Machines Corporation and others.";

  private static final Object[][] empty_ = {};

  public Object[][] getContents()
  {
    return empty_;
  }
}

