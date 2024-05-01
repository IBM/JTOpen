///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: HTMLConstants.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.util.html;

/**
*  The HTMLConstants class is a collection of constants generally used 
*  for positioning and orienting HTML components.
**/
public interface HTMLConstants
{
   /**
   *  Vertical baseline alignment.
   **/
   public final static String BASELINE = "baseline";

   /**
   *  New, unnamed window as the target frame.
   **/
   public final static String TARGET_BLANK = "_blank";

   /**
   *  Vertical bottom alignment.
   **/
   public final static String BOTTOM = "bottom";

   /**
   *  Vertical absolute bottom alignment
   **/
   public final static String ABSBOTTOM = "absbottom";

   /**
   *  Horizontal alignment in center of page.
   **/
   public final static String CENTER = "center";

   /**
   *  Horizontal alignment between both text margins.
   **/
   public final static String JUSTIFY = "justify";

   /**
   *  Horizontal left alignment.
   **/
   public final static String LEFT = "left";
                              
   /**
   *  Vertical middle alignment.
   **/
   public final static String MIDDLE = "middle";

   /**
   *  Vertical absolute middle alignment.
   **/
   public final static String ABSMIDDLE = "absmiddle";

   /**
   *  Frameset parent of current window as target frame.
   **/
   public final static String TARGET_PARENT = "_parent";

   /**
   *  Horizontal right alignment.
   **/
   public final static String RIGHT = "right";

   /**
   *  Current frame as the target frame.
   **/
   public final static String TARGET_SELF = "_self";

   /**
   *  Full, original window as target frame.
   **/
   public final static String TARGET_TOP = "_top";

   /**
   *  Vertical top alignment.
   **/
   public final static String TOP = "top";

   /**
   *  Vertical text top alignment.
   **/
   public final static String TEXTTOP = "texttop";

   /**
   *  Disc, solid bullet, labeling scheme.
   **/
   public final static String DISC = "disc";          //$B0A


   /**
   *  Square, solid square, labeling scheme.
   **/
   public final static String SQUARE = "square";      //$B0A


   /**
   *  Circle, hollow circle, labeling scheme.
   **/
   public final static String CIRCLE = "circle";      //$B0A

   
   /**
    *  Numbered ordered labeling scheme.
    **/
   public final static String NUMBERS = "numbers";     //$B0A


   /**
    *  Capital letter ordered labeling scheme.
    **/
   public final static String CAPITALS = "capitals";      //$B0A


   /**
    *  Lower-case letter ordered labeling scheme.
    */
   public final static String LOWER_CASE = "lower_case";    //$B0A
   
   
   /**
    *  Large Roman numeral ordered labeling scheme.
    **/
   public final static String LARGE_ROMAN = "large_roman";  //$B0A


   /**
    *  Small Roman numeral ordered labeling scheme.
    **/
   public final static String SMALL_ROMAN = "small_roman";  //$B0A


   /**
    *  Left to Right text interpretation direction.
    **/
   public final static String LTR = "ltr";               //$B1A

   /**
    *  Right to Left text interpretation direction.
    **/
   public final static String RTL = "rtl";               //$B1A

   
}
