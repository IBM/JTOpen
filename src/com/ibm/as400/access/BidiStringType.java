///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: BidiStringType.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

/**
*  The BidiStringType class is a collection of constants generally used 
*  for describing the output string type of bidi data as defined by the 
*  CDRA (Character Data Respresentation Architecture).
*  <p>
*  Each CCSID has a default CDRA string type, which defines a set of Bidi
*  flags.  When using these constants while performing conversions on bidi 
*  strings, the parameter string type should always be either ST5 (LTR), 
*  ST6 (RTL), ST10 (Contextual LTR), or ST11 (Contextual RTL).  
*  <p>
*  In fact, only the orientation of the given string type is used to modify 
*  the Bidi flags to apply to the Java data.  The other Bidi flags of the 
*  Java data always conform to the Unicode standard. 
**/
public interface BidiStringType
{
   /**
   *  The default string type for non bidi data (LTR).
   **/
   final static int DEFAULT = 0;
   
   /**
    * String type used when an EBCDIC/Unicode conversion is desired, but
    * without swapping, shaping, or transformation.
   **/
   public final static int NONE = -1;

   /**
    *  String Type 4
    *  <ul>
    *     <li>Type of text:  Visual  
    *     <li>Orientation:  LTR
    *     <li>Symetric swapping:  No
    *     <li>Numeral shape:  Nominal
    *     <li>Text shapes:  Shaped
    *  </ul>
    **/
   public final static int ST4 = 4;

   /**
    *  String Type 5
    *  <ul>
    *     <li>Type of text:  Implicit  
    *     <li>Orientation:  LTR
    *     <li>Symetric swapping:  Yes
    *     <li>Numeral shape:  Nominal
    *     <li>Text shapes:  Nominal
    *  </ul>
    **/
   public final static int ST5 = 5;

   /**
    *  String Type 6
    *  <ul>
    *     <li>Type of text:  Implicit  
    *     <li>Orientation:  RTL
    *     <li>Symetric swapping:  Yes
    *     <li>Numeral shape:  Nominal
    *     <li>Text shapes:  Nominal
    *  </ul>
    **/
   public final static int ST6 = 6;

   /**
    *  String Type 7
    *  <ul>
    *     <li>Type of text:  Visual  
    *     <li>Orientation:  Contextual LTR
    *     <li>Symetric swapping:  No
    *     <li>Numeral shape:  Nominal
    *     <li>Text shapes:  Nominal
    *  </ul>
    **/
   public final static int ST7 = 7;

   /**
    *  String Type 8
    *  <ul>
    *     <li>Type of text:  Visual  
    *     <li>Orientation:  RTL
    *     <li>Symetric swapping:  No
    *     <li>Numeral shape:  Nominal
    *     <li>Text shapes:  Shaped
    *  </ul>
    **/
   public final static int ST8 = 8;

   /**
    *  String Type 9
    *  <ul>
    *     <li>Type of text:  Visual  
    *     <li>Orientation:  RTL
    *     <li>Symetric swapping:  Yes
    *     <li>Numeral shape:  Nominal
    *     <li>Text shapes:  Shaped
    *  </ul>
    **/
   public final static int ST9 = 9;

   /**
    *  String Type 10
    *  <ul>
    *     <li>Type of text:  Implicit
    *     <li>Orientation:  Contextual LTR
    *     <li>Symetric swapping:  Yes
    *     <li>Numeral shape:  Nominal
    *     <li>Text shapes:  Nominal
    *  </ul>
    **/
   public final static int ST10 = 10;

   /**
    *  String Type 11
    *  <ul>
    *     <li>Type of text:  Implicit
    *     <li>Orientation:  Contextual RTL
    *     <li>Symetric swapping:  Yes
    *     <li>Numeral shape:  Nominal
    *     <li>Text shapes:  Nominal
    *  </ul>
    **/
   public final static int ST11 = 11;
}

