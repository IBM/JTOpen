///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: SCSFontData.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

/**
  * This class stores font data used by the SCS Writer classes.
  *
**/


class SCSFontData extends Object
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

   /* The following array stores FGID values for fonts used by the       */
   /* SCS3812Writer class.  The order of the fonts in the array (comment */
   /* value) must match the order of the font constants defined in the   */
   /* SCS3812Writer class.                                               */
   static final int [] fontIDs = {
                                   11,     /* FONT_COURIER_10            */
                                   85,     /* FONT_COURIER_12            */
                                  223,     /* FONT_COURIER_15            */
                                  252,     /* FONT_COURIER_17            */
                                  244,     /* FONT_COURIER_5             */
                                   46,     /* FONT_COURIER_BOLD_10       */
                                  253,     /* FONT_COURIER_BOLD_17       */
                                  245,     /* FONT_COURIER_BOLD_5        */
                                   18,     /* FONT_COURIER_ITALIC_10     */
                                   91,     /* FONT_COURIER_ITALIC_12     */
                                   40,     /* FONT_GOTHIC_10             */
                                   66,     /* FONT_GOTHIC_12             */
                                  204,     /* FONT_GOTHIC_13             */
                                  230,     /* FONT_GOTHIC_15             */
                                  281,     /* FONT_GOTHIC_20             */
                                  290,     /* FONT_GOTHIC_27             */
                                   39,     /* FONT_GOTHIC_BOLD_10        */
                                   69,     /* FONT_GOTHIC_BOLD_12        */
                                   68,     /* FONT_GOTHIC_ITALIC_12      */
                                   87,     /* FONT_LETTER_GOTHIC_12      */
                                  110,     /* FONT_LETTER_GOTHIC_BOLD_12 */
                                   19,     /* FONT_OCR_A_10              */
                                    3,     /* FONT_OCR_B_10              */
                                    5,     /* FONT_ORATOR_10             */
                                   38,     /* FONT_ORATOR_BOLD_10        */
                                   12,     /* FONT_PRESTIGE_10           */
                                   86,     /* FONT_PRESTIGE_12           */
                                  221,     /* FONT_PRESTIGE_15           */
                                  111,     /* FONT_PRESTIGE_BOLD_12      */
                                  112,     /* FONT_PRESTIGE_ITALIC_12    */
                                   41,     /* FONT_ROMAN_10              */
                                   84,     /* FONT_SCRIPT_12             */
                                   42,     /* FONT_SERIF_10              */
                                   70,     /* FONT_SERIF_12              */
                                  229,     /* FONT_SERIF_15              */
                                   72,     /* FONT_SERIF_BOLD_12         */
                                   43,     /* FONT_SERIF_ITALIC_10       */
                                   71      /* FONT_SERIF_ITALIC_12       */
                                     };


   /* The following array stores the width of fonts used by the          */
   /* SCS3812Writer class.  The order of the fonts in the array (comment */
   /* value) must match the order of the font constants defined in the   */
   /* SCS3812Writer class.                                               */
   static final int [] fontWidths = {
                                     144,     /* FONT_COURIER_10            */
                                     120,     /* FONT_COURIER_12            */
                                      96,     /* FONT_COURIER_15            */
                                      84,     /* FONT_COURIER_17            */
                                     288,     /* FONT_COURIER_5             */
                                     144,     /* FONT_COURIER_BOLD_10       */
                                      84,     /* FONT_COURIER_BOLD_17       */
                                     288,     /* FONT_COURIER_BOLD_5        */
                                     144,     /* FONT_COURIER_ITALIC_10     */
                                     120,     /* FONT_COURIER_ITALIC_12     */
                                     144,     /* FONT_GOTHIC_10             */
                                     120,     /* FONT_GOTHIC_12             */
                                     108,     /* FONT_GOTHIC_13             */
                                      96,     /* FONT_GOTHIC_15             */
                                      72,     /* FONT_GOTHIC_20             */
                                      54,     /* FONT_GOTHIC_27             */
                                     144,     /* FONT_GOTHIC_BOLD_10        */
                                     120,     /* FONT_GOTHIC_BOLD_12        */
                                     120,     /* FONT_GOTHIC_ITALIC_12      */
                                     120,     /* FONT_LETTER_GOTHIC_12      */
                                     120,     /* FONT_LETTER_GOTHIC_BOLD_12 */
                                     144,     /* FONT_OCR_A_10              */
                                     144,     /* FONT_OCR_B_10              */
                                     144,     /* FONT_ORATOR_10             */
                                     144,     /* FONT_ORATOR_BOLD_10        */
                                     144,     /* FONT_PRESTIGE_10           */
                                     120,     /* FONT_PRESTIGE_12           */
                                      96,     /* FONT_PRESTIGE_15           */
                                     120,     /* FONT_PRESTIGE_BOLD_12      */
                                     120,     /* FONT_PRESTIGE_ITALIC_12    */
                                     144,     /* FONT_ROMAN_10              */
                                     120,     /* FONT_SCRIPT_12             */
                                     144,     /* FONT_SERIF_10              */
                                     120,     /* FONT_SERIF_12              */
                                      96,     /* FONT_SERIF_15              */
                                     120,     /* FONT_SERIF_BOLD_12         */
                                     144,     /* FONT_SERIF_ITALIC_10       */
                                     120      /* FONT_SERIF_ITALIC_12       */
                                         };


    /* The following array stores code values used by the SCS writer */
    /* classes.  The order of the array must match the codePageID    */
    /* array.                                                        */
    static final int [] codePage = {
                                      0,
                                     29,
                                     37,
                                     38,
                                    256,
                                    259,
                                    260,
                                    273,
                                    274,
                                    275,
                                    276,
                                    277,
                                    278,
                                    279,
                                    280,
                                    281,
                                    282,
                                    283,
                                    284,
                                    285,
                                    286,
                                    287,
                                    288,
                                    289,
                                    290,
                                    297,
                                    305,
                                    310,
                                    340,
                                    361,
                                    420,
                                    423,
                                    424,
                                    437,
                                    500,
                                    803,
                                    831,
                                    870,
                                    871,
                                    875,
                                    880,
                                    892,
                                    893,
                                    905,
                                   1026,
                                   1002 };


    /* The following array stores code page IDs used in the SCGL     */
    /* command.  The order of the array must match that of the       */
    /* codePage array.                                               */
    static final byte [] codePageID = {
                                       (byte)0xFF,     /*   0  */
                                       (byte)0xFF,     /*  29  */
                                       (byte)0x01,     /*  37  */
                                       (byte)0xFF,     /*  38  */
                                       (byte)0xFF,     /* 256  */
                                       (byte)0xFF,     /* 259  */
                                       (byte)0xFF,     /* 260  */
                                       (byte)0x02,     /* 273  */
                                       (byte)0x03,     /* 274  */
                                       (byte)0x03,     /* 275  */
                                       (byte)0x05,     /* 276  */
                                       (byte)0x06,     /* 277  */
                                       (byte)0x07,     /* 278  */
                                       (byte)0x08,     /* 279  */
                                       (byte)0x09,     /* 280  */
                                       (byte)0x0A,     /* 281  */
                                       (byte)0x0C,     /* 282  */
                                       (byte)0x0D,     /* 283  */
                                       (byte)0x0E,     /* 284  */
                                       (byte)0x0F,     /* 285  */
                                       (byte)0xFF,     /* 286  */
                                       (byte)0xFF,     /* 287  */
                                       (byte)0xFF,     /* 288  */
                                       (byte)0xFF,     /* 289  */
                                       (byte)0x0B,     /* 290  */
                                       (byte)0x08,     /* 297  */
                                       (byte)0x0D,     /* 305  */
                                       (byte)0xFF,     /* 310  */
                                       (byte)0xFF,     /* 340  */
                                       (byte)0xFF,     /* 361  */
                                       (byte)0xFF,     /* 420  */
                                       (byte)0xFF,     /* 423  */
                                       (byte)0xFF,     /* 424  */
                                       (byte)0xFF,     /* 437  */
                                       (byte)0xFF,     /* 500  */
                                       (byte)0xFF,     /* 803  */
                                       (byte)0xFF,     /* 831  */
                                       (byte)0xFF,     /* 870  */
                                       (byte)0xFF,     /* 871  */
                                       (byte)0xFF,     /* 875  */
                                       (byte)0xFF,     /* 880  */
                                       (byte)0xFF,     /* 892  */
                                       (byte)0xFF,     /* 893  */
                                       (byte)0xFF,     /* 905  */
                                       (byte)0xFF,     /*1026  */
                                       (byte)0xFF };   /*1002  */

}
