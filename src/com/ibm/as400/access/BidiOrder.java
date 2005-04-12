///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  BidiOrder.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2004 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.util.Vector;

/**
 *  <p><b>Multi-threading considerations:</b> This class is thread-safe,
 *  since its only public method is synchronized, and all instance variables
 *  are initialized within this method.  However, to avoid delays, each
 *  thread should use its own instances of this class.
 **/

class BidiOrder
{

// Private Variables  :
// ----------------

//private int srcLen;   // length of source text to process

//private String buffer_in ;

  private static final int UBAT_B =   0;          /* block separator   */
  private static final int UBAT_S =   1;          /* segment separator */
  private static final int UBAT_L =   2;          /* left to right     */
  private static final int UBAT_R =   3;          /* right to left     */
  private static final int UBAT_EN =  4;          /* European digit    */
  private static final int UBAT_AN =  5;          /* Arabic-Indic digit */
  private static final int UBAT_W  =  6;          /* white space */
  private static final int UBAT_N  =  7;          /* neutral */
  private static final int UBAT_BD =  8;          /* bidi special codes */
  private static final int UBAT_AL =  9;          /* Arabic Letter      */
  private static final int UBAT_ET = 10;          /* European digit terminator */
  private static final int UBAT_ES = 11;          /* European digit separator  */
  private static final int UBAT_CS = 12;          /* common digit separator    */
  private static final int UBAT_NSM =13;          /* Non Spacing Mark   */

  private static final int ITIL    = 8;
  private static final int ITCOND  = 9;

  private static final byte UBAT_N_SWAP = -UBAT_N;
  private static final byte IMP_LTR = 4;
  private static final byte IMP_RTL = 8;
  private static final byte ORIG = 0;
  private static final byte FINAL = 1;
  private static final char LRM = 0x200E;
  private static final char RLM = 0x200F;
/*****************************************************************************/
/* Not Spacing characters*/
/*-----------------------*/
  private static final char notSpacing[][] = {      /* 60, 2 */
    {0x0300, 0x0385} ,
    {0x0483, 0x0486} ,
    {0x05B0, 0x05BD} ,
    {0x05BF, 0x05BF} ,
    {0x05C1, 0x05C2} ,
    {0x05F5, 0x05F5} ,
    {0x064B, 0x0652} ,
    {0x0670, 0x0670} ,
    {0x06D6, 0x06E4} ,
    {0x06E7, 0x06E8} ,
    {0x06EA, 0x06ED} ,
    {0x0901, 0x0902} ,
    {0x093C, 0x093C} ,
    {0x0941, 0x0948} ,
    {0x094D, 0x094D} ,
    {0x0951, 0x0954} ,
    {0x0962, 0x0963} ,
    {0x0981, 0x0981} ,
    {0x09BC, 0x09BC} ,
    {0x09C1, 0x09C4} ,
    {0x09CD, 0x09CD} ,
    {0x09E2, 0x09E3} ,
    {0x0A02, 0x0A02} ,
    {0x0A3C, 0x0A3C} ,
    {0x0A41, 0x0A4C} ,
    {0x0A70, 0x0A71} ,
    {0x0A81, 0x0A82} ,
    {0x0ABC, 0x0ABC} ,
    {0x0AC1, 0x0AC8} ,
    {0x0ACD, 0x0ACD} ,
    {0x0B01, 0x0B01} ,
    {0x0B3C, 0x0B3C} ,
    {0x0B3F, 0x0B3F} ,
    {0x0B41, 0x0B43} ,
    {0x0B4D, 0x0B4D} ,
    {0x0BC0, 0x0BC0} ,
    {0x0BCD, 0x0BCD} ,
    {0x0C3E, 0x0C40} ,
    {0x0C46, 0x0C56} ,
    {0x0CBF, 0x0CBF} ,
    {0x0CC6, 0x0CC6} ,
    {0x0CCC, 0x0CCD} ,
    {0x0D41, 0x0D43} ,
    {0x0D4D, 0x0D4D} ,
    {0x0E31, 0x0E31} ,
    {0x0E34, 0x0E3A} ,
    {0x0E47, 0x0E4D} ,
    {0x0EB1, 0x0EB1} ,
    {0x0EB4, 0x0EBC} ,
    {0x0EC8, 0x0ECD} ,
    {0x1026, 0x102A} ,
    {0x102E, 0x102E} ,
    {0x1030, 0x1030} ,
    {0x1036, 0x1037} ,
    {0x103B, 0x103B} ,
    {0x103D, 0x103E} ,
    {0x104B, 0x104C} ,
    {0x20D0, 0x20FF} ,
    {0x302A, 0x302F} ,
    {0x3099, 0x309A}

  };

/***********************************************************************/
/* Symmetric Pairs */
/*-----------------*/
  private static final char symPairs[][] = {
    {0x0028, 0x0029} ,
    {0x0029, 0x0028} ,
    {0x003C, 0x003E} ,
    {0x003E, 0x003C} ,
    {0x005B, 0x005D} ,
    {0x005D, 0x005B} ,
    {0x007B, 0x007D} ,
    {0x007D, 0x007B} ,
    {0x00AB, 0x00BB} ,
    {0x00BB, 0x00AB} ,
    {0x207D, 0x207E} ,
    {0x207E, 0x207D} ,
    {0x208D, 0x208E} ,
    {0x208E, 0x208D} ,
    {0x2329, 0x232A} ,
    {0x232A, 0x2329} ,
    {0xFE59, 0xFE5A} ,
    {0xFE5A, 0xFE59} ,
    {0xFE5B, 0xFE5C} ,
    {0xFE5C, 0xFE5B} ,
    {0xFE5D, 0xFE5E} ,
    {0xFE5E, 0xFE5D} ,
    {0xFE64, 0xFE65} ,
    {0xFE65, 0xFE64}
  };

/**************************************************************************/
  private static final short impTab_LTR[][] =
  {
                     /*   B,    S,    L,    R,   EN,   AN,   W,   N,  IL, Cond */

/* 0 LTR text     */  {   0,    0,    0,    3,    0,    1,   0,   0,   0,  0},
/* 1 LTR+AN       */  {   0,    0,    0,    3,    0,    1,   2,   2,   2,  0},
/* 2 LTR+AN+N     */  {   0,    0,    0,    3,    0, 0x11,   2,   2,   0,  1},
/* 3 RTL text     */  {   0,    0,    0,    3,    5,    5,   4,   4,   1,  0},
/* 4 RTL cont     */  {   0,    0,    0,    3, 0x15, 0x15,   4,   4,   0,  1},
/* 5 RTL+EN/AN    */  {   0,    0,    0,    3,    5,    5,   4,   4,   2,  0}
  };

/**************************************************************************/
  private static final short impTab_RTL[][] =
  {
                     /*   B,    S,    L,    R,   EN,   AN,   W,   N,  IL, Cond */

/* 0 RTL text     */  {   0,    0,    2,    0,    1,    1,   0,   0,   0,  0},
/* 1 RTL+EN/AN    */  {   0,    0,    2,    0,    1,    1,   0,   0,   1,  0},
/* 2 LTR text     */  {   0,    0,    2,    0,    2,    1,   3,   3,   1,  0},
/* 3 LTR+cont     */  {   0,    0,    2,    0,    2, 0x21,   3,   3,   0,  1}
  };

/**************************************************************************/
  private static final short impTab_LTR_r[][] =             /* round trip */
  {
                     /*   B,    S,    L,    R,   EN,   AN,   W,   N,  IL, Cond */

/* 0 LTR text     */  {   0,    0,    0,    2,    0,    1,   0,   0,   0,  0},
/* 1 LTR+AN       */  {   0,    0,    0,    2,    0,    1,   3,   3,   2,  0},
/* 2 RTL text     */  {   0,    0,    0,    2,    4,    1,   3,   3,   1,  0},
/* 3 RTL cont     */  {   0,    0,    0, 0x22,    4,    4,   3,   3,   1,  1},
/* 4 RTL+EN/AN    */  {   0,    0,    0, 0x22,    4,    4,   3,   3,   2,  1}
  };

/**************************************************************************/
  private static final short impTab_RTL_r[][] =             /* round trip */
  {
                     /*   B,    S,    L,    R,   EN,   AN,   W,   N,  IL, Cond */

/* 0 RTL text     */  {   0,    0,    3,    0,    1,    2,   0,   0,   0,  0},
/* 1 RTL+EN       */  {0x20, 0x20,    3, 0x20,    1, 0x22,   5,   5,   1,  1},
/* 2 RTL+AN       */  {   0,    0,    3,    0,    1,    2,   0,   0,   1,  0},
/* 3 LTR text     */  {   0,    0,    3,    0,    3,    2,   4,   4,   1,  0},
/* 4 LTR+cont     */  {   0,    0,    3,    0,    3, 0x22,   4,   4,   0,  1},
/* 5 RTL+EN+cont  */  {0x20, 0x20,    3, 0x20,    1, 0x22,   5,   5,   0,  1}
  };

/**************************************************************************/
  private static final short impTab_LTR_w[][] =         /* windows compatible */
  {
                     /*   B,    S,    L,    R,   EN,   AN,   W,   N,  IL, Cond */

/* 0 LTR text     */  {   0,    0,    0,    2,    5,    1,   0,   0,   0,  0},
/* 1 LTR+AN       */  {   0,    0,    0,    2,    5,    1,   0,   0,   2,  0},
/* 2 RTL text     */  {   0,    0,    0,    2,    4,    6,   3,   3,   1,  0},
/* 3 RTL cont     */  {   0,    0,    0,    2, 0x14, 0x16,   3,   3,   1,  1},
/* 4 RTL+EN       */  {   0,    0,    0,    2,    4,    6,   3,   3,   2,  0},
/* 5 LTR+EN       */  {   0,    0,    0,    2,    5,    1,   0,   0,   2,  0},
/* 6 RTL+AN       */  {   0,    0,    0,    2,    5,    6,   3,   3,   2,  0},
  };

/**************************************************************************/
  private static final short impTab_LTR_m[][] =         /* insert markers */
  {
                     /*   B,    S,    L,    R,   EN,   AN,   W,   N,  IL, Cond */

/* 0 LTR          */  {   0,    0,    0, 0x63,    0,    1,   0,   0,   0,  0},
/* 1 LTR+AN       */  {   0,    0,    0, 0x63,    0,    1,   2,   2,   2,  0},
/* 2 LTR+AN+N     */  {   0,    0,    0, 0x63,    0, 0x21,   2,   2,   1,  1},
/* 3 RTL text     */  {   0,    0,    0, 0x63, 0x55, 0x56,   4,   4,   1,  0},
/* 4 RTL cont     */  {0x30, 0x30, 0x30, 0x43, 0x55, 0x56,   4,   4,   1,  1},
/* 5 RTL+EN       */  {0x30, 0x30, 0x30, 0x43,    5, 0x56,   4,   4,   2,  0},
/* 6 RTL+AN       */  {0x30, 0x30, 0x30, 0x43, 0x55,    6,   4,   4,   2,  0}
  };
//  The case handled in this table is (visually):  R EN L

/**************************************************************************/
  private static final short impTab_RTL_m[][] =         /* insert markers */
  {
                     /*   B,    S,    L,    R,   EN,   AN,   W,   N,  IL, Cond */

/* 0 RTL text     */  {   0,    0,    3,    0,    1,    1,   0,   0,   0,  0},
/* 1 RTL+EN/AN    */  {   0,    0, 0x73,    0,    1,    1,   2,   2,   1,  0},
/* 2 RTL+EN/AN+N  */  {   0,    0, 0x73,    0,    1,    1,   2,   2,   0,  0},
/* 3 LTR text     */  {   0,    0,    3,    0,    3, 0x86,   4,   4,   1,  0},
/* 4 LTR+N        */  {0x90, 0x90, 0xA3, 0x90,    5, 0x86,   4,   4,   0,  1},
/* 5 LTR+EN       */  {0x90, 0x90, 0xA3, 0x90,    5, 0x86,   4,   4,   1,  1},
/* 6 LTR+AN       */  {0x90, 0x90, 0xA3, 0x90,    6,    6,   4,   4,   1,  1}
  };
//  The cases handled in this table are (visually):  R EN L
//                                                   R L AN L


/***************************/
/*                         */
/*  Fields from ucbStruct  */
/*                         */
/***************************/

  int ucb_ix ;      // index of currently processed character in source order
                    //  target area

  byte ucb_outLev; // Output Level = 0 or 1 according to the direction of output
                   // specified in argument 5 (0 = LTR, 1 = RTL)

  byte ucb_basLev;   // Base Level = 0 or 1 (0 = LTR, 1 = RTL)

  byte ucb_curLev;  // Current Level = between basLev and 15; initially set equal to basLev;
                    // modified by LRE/RLE/LRO/RLO/PDF

  int ucb_impSta;   // Implicit State = between 0 and 18  /.../

  int ucb_condPos;    // hold the position of a conditional string

  int ucb_xType;    // Specifies character type for which
                    // will be  computed Implicit level

  byte ucb_wTarget; // value to put in the output area

/***************************/
/*                         */
/*  Fields from icsStruct  */
/*                         */
/***************************/

  BidiFlag ics_num_flag;
  // Numeric Shape selector = set initially according to argument 6
  // (set to 0 if nominal shaping of European numerals is selected;
  // set to 1 if Arabic-Indic shaping is selected;
  // set to 2 if contextual shaping is selected);
  // set to 0 when meeting U+206F; set to 1 when meeting U+206E

  boolean ics_symmetric;   // Character Swapping selector = set initially according to Swapping
  // (set to 0 if inhibit swapping is selected;
  // set to 1 if activate is selected);
  // set to 0 when meeting U+206A; set to 1 when meeting U+206B

  BidiFlag ics_orient_in;
  BidiFlag ics_orient_out;
  BidiFlag ics_type_in;
  BidiFlag ics_type_out;
  BidiTransform myBdx;                  /* local reference of bdx */
  int ics_size;                         /* length of source text to process */
  char[] ics_buffer_in;
  char[] ics_buffer_out;
  boolean invertInput;
  boolean visToVis;
  short impTab[][];
  byte typeArray[][];

  boolean insertMarkers;
  int insertCnt;                        /* number of confirmed inserts */
  int removeCnt;                        /* number of LRM/RLMs to remove */
  int startL2EN;                        /* start of level 2 run        */
  int lastStrongRTL;                    /* index of last found R or AL */
  boolean reqImpToImp;                  /* impToImp request */
  int impToImpOrient;
  int impToImpPhase;

/*------------------------------------------------------------------------*/

  private static boolean odd(int n)
  {
    return (n & 1) == 1;
  }

/*------------------------------------------------------------------------*/

  private static boolean even(int n)
  {
    return (n & 1) == 0;
  }

/*------------------------------------------------------------------------*/

  private static void invertMap(int[] buffer, int lower_limit, int upper_limit)
  /* invert a buffer of ints, between lower_limit and upper_limit */
  {
    int temp;

    for (; lower_limit < upper_limit; lower_limit++, upper_limit--)
    {
        temp = buffer[lower_limit];
        buffer[lower_limit] = buffer[upper_limit];
        buffer[upper_limit] = temp;
    }
  }

/*------------------------------------------------------------------------*/

  private static void invertMap(byte[] buffer, int lower_limit, int upper_limit)
  /* invert a buffer of bytes, between lower_limit and upper_limit */
  {
    byte temp;

    for (; lower_limit < upper_limit; lower_limit++, upper_limit--)
    {
        temp = buffer[lower_limit];
        buffer[lower_limit] = buffer[upper_limit];
        buffer[upper_limit] = temp;
    }
  }

/*------------------------------------------------------------------------*/

  private static boolean UCQSPAC(char x)
  {
    int low, high, mid;

    low = 0;
    high = notSpacing.length - 1;
    while (low <= high)
    {
        mid = (low + high) / 2;
        if (x < notSpacing[mid][0])
            high = mid - 1;
        else if (x > notSpacing[mid][1])
            low = mid + 1;
        else
            return(false);
    }
    return(true);
  }

/*------------------------------------------------------------------------*/

  private static char UCQSYMM(char x)
  {
    int low, high, mid;
    char c;

    low = 0;
    high = 23;

    while (low <= high)
    {
        mid = (low + high) / 2;
        c = symPairs[mid][0];
        if (x < c)
            high = mid - 1;
        else if (x > c)
            low = mid + 1;
        else
            return(symPairs[mid][1]);
    }
    return x;
  }

/*********************************************************************/
/*                                                                   */
/* This function fills typeArray with 2 ch_types: one original, and  */
/* one simplified after resolving numbers, NSMs and others           */
/*                                                                   */
/*********************************************************************/
  private void fillTypeArray()
  {
    int             i, prev;
    byte            cType, wType;
    boolean         isArabic = false;
    byte[][]        ta;

    ta = typeArray;
    for (i = 0; i < ics_size; i++)
    {
        cType = getChType(ics_buffer_in[i], myBdx.wordBreak);
        ta[i][ORIG] = cType;
        ta[i][FINAL] = UBAT_N;
        if (visToVis)
            continue;
        switch (cType)
        {
          case UBAT_B:
            isArabic = false;
            ta[i][FINAL] = UBAT_B;
            break;
          case UBAT_S:
            /* anything to do for segment separator ??? */
            ta[i][FINAL] = UBAT_S;
            break;
          case UBAT_L:
            isArabic = false;
            ta[i][FINAL] = UBAT_L;
            break;
          case UBAT_R:
            isArabic = false;
            ta[i][FINAL] = UBAT_R;
            break;
          case UBAT_AL:
            isArabic = true;
            ta[i][FINAL] = UBAT_R;
            break;
          case UBAT_EN:
            if (isArabic)
            {
                wType = UBAT_AN;
            }
            else
            {
                wType = UBAT_EN;
                if ((i >= 2) && (ta[i-1][ORIG] == UBAT_ES)
                             && (ta[i-2][ORIG] == UBAT_EN))
                    ta[i-1][FINAL] = UBAT_EN;
                prev = i - 1;
                while ((prev >= 0) && (ta[prev][ORIG] == UBAT_ET))
                    ta[prev--][FINAL] = UBAT_EN;
            }
            if ((i >= 2) && (ta[i-1][ORIG] == UBAT_CS)
                         && (ta[i-2][ORIG] == UBAT_EN))
                ta[i-1][FINAL] = wType;
            ta[i][FINAL] = wType;
            break;
          case UBAT_AN:
            if ((i >= 2) && (ta[i-1][ORIG] == UBAT_CS)
                         && (ta[i-2][FINAL] == UBAT_AN))
                ta[i-1][FINAL] = UBAT_AN;
            ta[i][FINAL] = UBAT_AN;
            break;
          case UBAT_W:
            ta[i][FINAL] = UBAT_W;
            break;
          case UBAT_ET:
            if ((i > 0) && (ta[i-1][FINAL] == UBAT_EN))
                ta[i][FINAL] = UBAT_EN;
            break;
          case UBAT_NSM:
            /* This code does not support NSMs within RTL text in Visual type
               input data. We have seen no requirements for such combination. */
            if (i <= 0) break;
            ta[i][FINAL] = ta[i-1][FINAL];
            break;
        }
    }
  }

/*------------------------------------------------------------------------*/

  private void fillTypeArray2()
  {
    int             i, k, prev;
    byte            cType, wType;
    boolean         isArabic = false;
    byte[][]        ta;

    ta = typeArray;
    for (i = 0; i < ics_size; i++)
    {
        k = myBdx.dstToSrcMap[i];
        cType = ta[k][ORIG];            /* ok even for UBAT_N_SWAP */
        ta[k][FINAL] = UBAT_N;
        switch (cType)
        {
          case UBAT_B:
            isArabic = false;
            ta[k][FINAL] = UBAT_B;
            break;
          case UBAT_S:
            /* anything to do for segment separator ??? */
            ta[k][FINAL] = UBAT_S;
            break;
          case UBAT_L:
            isArabic = false;
            ta[k][FINAL] = UBAT_L;
            break;
          case UBAT_R:
            isArabic = false;
            ta[k][FINAL] = UBAT_R;
            break;
          case UBAT_AL:
            isArabic = true;
            ta[k][FINAL] = UBAT_R;
            break;
          case UBAT_EN:
            if (isArabic)
            {
                wType = UBAT_AN;
            }
            else
            {
                wType = UBAT_EN;
                if ((i >= 2) && (ta[myBdx.dstToSrcMap[i-1]][ORIG] == UBAT_ES)
                             && (ta[myBdx.dstToSrcMap[i-2]][ORIG] == UBAT_EN))
                    ta[myBdx.dstToSrcMap[i-1]][FINAL] = UBAT_EN;
                prev = i - 1;
                while ((prev >= 0) && (ta[myBdx.dstToSrcMap[prev]][ORIG] == UBAT_ET))
                    ta[myBdx.dstToSrcMap[prev--]][FINAL] = UBAT_EN;
            }
            if ((i >= 2) && (ta[myBdx.dstToSrcMap[i-1]][ORIG] == UBAT_CS)
                         && (ta[myBdx.dstToSrcMap[i-2]][ORIG] == UBAT_EN))
                ta[myBdx.dstToSrcMap[i-1]][FINAL] = wType;
            ta[k][FINAL] = wType;
            break;
          case UBAT_AN:
            if ((i >= 2) && (ta[myBdx.dstToSrcMap[i-1]][ORIG] == UBAT_CS)
                         && (ta[myBdx.dstToSrcMap[i-2]][ORIG] == UBAT_AN))
                ta[myBdx.dstToSrcMap[i-1]][FINAL] = UBAT_AN;
            ta[k][FINAL] = UBAT_AN;
            break;
          case UBAT_W:
            ta[k][FINAL] = UBAT_W;
            break;
          case UBAT_ET:
            if ((i > 0) && (ta[myBdx.dstToSrcMap[i-1]][FINAL] == UBAT_EN))
                ta[k][FINAL] = UBAT_EN;
            break;
          case UBAT_NSM:
            /* This method is invoked to transform Visual LTR to Implicit.
               NSMs appearing at the boundary between LTR and RTL text may be
               associated with either side.  Since the NSMs have already
               received a type in the first (Implicit to Visual) phase,
               we leave it as is.                                            */
            break;
        }
    }
  }

/*------------------------------------------------------------------------*/

  private  void addPoint(float newPoint)
  {
    if (myBdx.insertPoints == null)
        myBdx.insertPoints = new Vector(10, 50);
    myBdx.insertPoints.addElement(new Float(newPoint));
  }

/*------------------------------------------------------------------------*/

  private  int afterAN(int i)
  {
    while ((i < ics_size) && (typeArray[i][FINAL] == UBAT_AN))
        i++;
    return i;
  }

/*------------------------------------------------------------------------*/

  private  int afterENAN(int i)      /////////////// check if needed //////////////////////
  {
    while ((i < ics_size) && ((typeArray[i][FINAL] == UBAT_EN) ||
                              (typeArray[i][FINAL] == UBAT_AN)))
        i++;
    return i;
  }

/*------------------------------------------------------------------------*/

  private  int beforeENAN(int i)     /////////////// check if needed //////////////////////
  {
    while ((i >= 0) && ((typeArray[i][FINAL] == UBAT_EN) ||
                        (typeArray[i][FINAL] == UBAT_AN)))
        i--;
    return i;
  }

/****************************************************************************

   Method       : implicitProcessing()
   Objectives   : Computes Implicit level for passed character type
   Parameters   : None.

   Returns      : None.
----------------------------------------------------------------------------*/
  private void implicitProcessing()
  {
    int i, pos;
    short sCond, newIL, newIS ,Special;
    byte oldLevel, newLevel, pType, nType;

    newIS   = impTab[ ucb_impSta ][ ucb_xType ];

    Special =  (short)(newIS >> 4);     /* get 4 high bits */

    newIS = (short)(newIS & 0x0F);      /* get 4 low bits  */
    newIL = impTab[newIS][ITIL];
    newLevel = (byte)(ucb_curLev + newIL);

    if (Special > 0)
        switch (Special)
        {
        case 1:                         /* set conditional run to level 1 */
            for (i = ucb_condPos; i < ucb_ix; i++)
                myBdx.propertyMap[i] = (byte)(ucb_curLev + 1);
            ucb_condPos = -1;
            break;

        case 2:                         /* confirm the conditional run */
            ucb_condPos = -1;
            break;

        case 3:                         /* L after R/AL + possible EN/AN */
            /* check if we had EN after R/AL */
            if (startL2EN >= 0)
                addPoint(startL2EN);
            startL2EN = -1;     /* not within previous if since could also be -2 */
            /* check if we had any relevant EN/AN after R/AL */
            if ((myBdx.insertPoints == null) ||
                (myBdx.insertPoints.size() <= insertCnt))
            {
                /* nothing, just clean up */
                lastStrongRTL = -1;
                break;
            }
            /* reset previous RTL cont to level for LTR text */
            for (i =  lastStrongRTL + 1; i < ucb_ix; i++)
            {
                myBdx.propertyMap[i] = newLevel;
                /* disable possible symmetric swapping for character */
                typeArray[i][ORIG] = (byte)Math.abs(typeArray[i][ORIG]);
            }
            /* mark insert points as confirmed */
            insertCnt = myBdx.insertPoints.size();
            lastStrongRTL = -1;
            break;

        case 4:                         /* R/AL after possible relevant EN/AN */
            /* just clean up */
            if (myBdx.insertPoints != null)
                /* remove all non confirmed insert points */
                myBdx.insertPoints.setSize(insertCnt);
            startL2EN = -1;
            lastStrongRTL = ucb_ix;
            break;

        case 5:                         /* EN/AN after R/AL + possible cont */
            /* confirm possible conditional run */
            ucb_condPos = -1;
            /* check for real AN */
            if ((ucb_xType == UBAT_AN) && (typeArray[ucb_ix][ORIG] == UBAT_AN))
            {
                /* real AN */
                if (startL2EN == -1)    /* if no relevant EN already found */
                {
                    lastStrongRTL = afterAN(ucb_ix) - 1;
                    break;
                }
                if (startL2EN >= 0)     /* after EN, no AN */
                {
                    addPoint(startL2EN);
                    startL2EN = -2;
                }
                /* note AN */
                addPoint(ucb_ix);
                break;
            }
            /* if first EN/AN after R/AL */
            if (startL2EN == -1)
                startL2EN = ucb_ix;
            break;

        case 6:                         /* note location of latest R/AL */
            lastStrongRTL = ucb_ix;
            break;

        case 7:                         /* R followed by EN/AN followed by L */
            i = beforeENAN(ucb_ix - 1);
            addPoint((float)(ics_size - i - 0.7));        /* add RLM before */
            insertCnt = myBdx.insertPoints.size();
            break;

        case 8:                         /* L followed by N followed by AN */
            pos = afterENAN(ucb_ix);
            i = beforeENAN(ucb_ix);
            if ( ((pos >= ics_size) || (typeArray[pos][FINAL] == UBAT_L) ||
                                      (typeArray[pos][FINAL] == UBAT_R))
                 &&
                 ((i >= 0) && (typeArray[i][FINAL] == UBAT_L)) )
                break;
            addPoint((float)(pos - 0.4));                 /* add LRM after  */
            addPoint((float)(i + 1));                     /* add LRM before */
            break;

        case 9:                         /* L followed by N followed by R */
            if (myBdx.insertPoints != null)
                myBdx.insertPoints.setSize(insertCnt);    /* infirm inserts */
            ucb_condPos = -1;           /* confirm possible cont run as RTL */
            break;

        case 10:                        /* L followed by N followed by L */
            if (myBdx.insertPoints != null)
                insertCnt = myBdx.insertPoints.size();   /* confirm inserts */
            break;


        default:
            throw new IndexOutOfBoundsException("invalid action number");
            /* break; */

        }

    sCond = impTab[newIS][ITCOND];

    if (sCond == 0)
    {
      if (ucb_condPos > -1)
      {
        for (i = ucb_condPos; i < ucb_ix; i++)
        {
            oldLevel = myBdx.propertyMap[i];
            myBdx.propertyMap[i] = newLevel;
            if (ics_symmetric && odd(oldLevel ^ newLevel))   /* change parity? */
            /* EN and AN never change parity */
            {
                if (impToImpPhase == 2)
                    pos = myBdx.dstToSrcMap[i];
                else  pos = i;
                /* inverse swap status */
                if (typeArray[pos][ORIG] == UBAT_N)
                    typeArray[pos][ORIG] = UBAT_N_SWAP;
                else if (typeArray[pos][ORIG] == UBAT_N_SWAP)
                    typeArray[pos][ORIG] = UBAT_N;
            }
        }
        ucb_condPos = -1;
      }
    }
    else if (ucb_condPos == -1)
      ucb_condPos =  ucb_ix;

    ucb_impSta = newIS;

    ucb_wTarget = newLevel;
  }

/*------------------------------------------------------------------------*/

  private static byte getChType (char x, boolean wordBreak)
  {
    /* This routine gets the type of a certain character */
    if (wordBreak && (x == 0x0020))  return UBAT_S;
    return getChType(x);
  }

/*------------------------------------------------------------------------*/

  private static byte getChType (char x)
  {
    /* This routine gets the type of a certain character */
    if (
       (x == 0x000A)                    ||
       (x == 0x000D)                    ||
       ((x >= 0x001C) && (x <= 0x001E)) ||
       (x == 0x0085)                    ||
       (x == 0x2029)
       )
        return UBAT_B;

    if ((x == 0x0009) || (x == 0x000B) || (x == 0x001F))
        return UBAT_S;

    if (
       ((x >= 0x0041) && (x <= 0x005A)) ||
       ((x >= 0x0061) && (x <= 0x007A)) ||
       ((x >= 0x00C0) && (x <= 0x00D6)) ||
       ((x >= 0x00D8) && (x <= 0x00F6)) ||
       ((x >= 0x00F8) && (x <= 0x058F)) ||
       ((x >= 0x0900) && (x <= 0x10FF)) ||
       (x == 0x200E)                    ||
       ((x >= 0x20D0) && (x <= 0x20FF)) ||
       ((x >= 0x2160) && (x <= 0x2182)) ||
       ((x >= 0x3040) && (x <= 0x9FFF)) ||
       ((x >= 0xF900) && (x <= 0xFB17)) ||
       (x == 0xFE60)                    ||
       (x == 0xFE6B)                    ||
       (x == 0xFF06)                    ||
       ((x >= 0xFF20) && (x <= 0xFF3A)) ||
       ((x >= 0xFF41) && (x <= 0xFF5A)) ||
       ((x >= 0xFF60) && (x <= 0xFFDF))
       )
        return UBAT_L;

    if (
       ((x >= 0x0591) && (x <= 0x05FF)) ||
       (x == 0x200F)                    ||
       ((x >= 0xFB1D) && (x <= 0xFB4F))
       )
        return UBAT_R;

    if (
       ((x >= 0x0600) && (x <= 0x065F)) ||
       ((x >= 0x066E) && (x <= 0x06EF)) ||
       ((x >= 0x06FA) && (x <= 0x08FF)) ||
       ((x >= 0xFB50) && (x <= 0xFDFF)) ||
       ((x >= 0xFE70) && (x <= 0xFEFC))
       )
        return UBAT_AL;

    if (
       ((x >= 0x0030) && (x <= 0x0039)) ||
       ((x >= 0x00B2) && (x <= 0x00B3)) ||
       (x == 0x00B9)                    ||
       ((x >= 0x06F0) && (x <= 0x06F9)) ||
       (x == 0x2070)                    ||
       ((x >= 0x2074) && (x <= 0x2079)) ||
       ((x >= 0x2080) && (x <= 0x2089)) ||
       ((x >= 0x2460) && (x <= 0x249B)) ||
       (x == 0x24EA)                    ||
       ((x >= 0xFF10) && (x <= 0xFF19))
       )
        return UBAT_EN;

    if (
       ((x >= 0x0660) && (x <= 0x0669)) ||
       ((x >= 0x066B) && (x <= 0x066C))
       )
        return UBAT_AN;

    if (
       ((x >= 0x0023) && (x <= 0x0025)) ||
       /* the next 2 lines were moved to UBAT_ET to comply with Unicode 4.1 */
//     (x == 0x002B)    /* Plus */      ||
//     (x == 0x002D)    /* Minus */     ||
       /* end of updates for Unicode 4.1 */
       ((x >= 0x00A2) && (x <= 0x00A5)) ||
       ((x >= 0x00B0) && (x <= 0x00B1)) ||
       (x == 0x066A)                    ||
       ((x >= 0x09F2) && (x <= 0x09F3)) ||
       (x == 0x0AF1)                    ||
       (x == 0x0BF9)                    ||
       (x == 0x0E3F)                    ||
       (x == 0x17DB)                    ||
       ((x >= 0x2030) && (x <= 0x2034)) ||
       ((x >= 0x207A) && (x <= 0x207B)) ||
       ((x >= 0x208A) && (x <= 0x208B)) ||
       ((x >= 0x20A0) && (x <= 0x20B1)) ||
       (x == 0x212E)                    ||
       ((x >= 0x2212) && (x <= 0x2213)) ||
       (x == 0xFB29)                    ||
       (x == 0xFE5F)                    ||
       ((x >= 0xFE62) && (x <= 0xFE63)) ||
       ((x >= 0xFE69) && (x <= 0xFE6A)) ||
       ((x >= 0xFF03) && (x <= 0xFF05)) ||
       (x == 0xFF0B)                    ||
       (x == 0xFF0D)                    ||
       ((x >= 0xFFE0) && (x <= 0xFFE1)) ||
       ((x >= 0xFFE5) && (x <= 0xFFE6))
       )
        return UBAT_ET;

    if (
       /* the next 2 lines were moved here (from UBAT_ET) to comply with Unicode 4.1 */
       (x == 0x002B)    /* Plus */      ||
       (x == 0x002D)    /* Minus */     ||
       /* end of updates for Unicode 4.1 */
       (x == 0x002F)                    ||
       (x == 0xFF0F)
       )
        return UBAT_ES;

    if (
       (x == 0x002C)                    ||
       (x == 0x002E)                    ||
       (x == 0x003A)                    ||
       (x == 0x00A0)                    ||
       (x == 0x060C)                    ||
       (x == 0xFE50)                    ||
       (x == 0xFE52)                    ||
       (x == 0xFE55)                    ||
       (x == 0xFF0C)                    ||
       (x == 0xFF0E)                    ||
       (x == 0xFF1A)
       )
        return UBAT_CS;

    if (
       (x == 0x000C)                    ||
       (x == 0x0020)                    ||
       (x == 0x1680)                    ||
       (x == 0x180E)                    ||
       ((x >= 0x2000) && (x <= 0x200A)) ||
       (x == 0x2028)                    ||
       (x == 0x202F)                    ||
       (x == 0x205F)                    ||
       (x == 0x3000)
       )
        return UBAT_W;

    if (
       ((x >= 0x202A) && (x <= 0x202E)) ||
       ((x >= 0x206C) && (x <= 0x206F))
       )
        return UBAT_BD;

    return UBAT_N;
  }

/*------------------------------------------------------------------------*/

  private static int firstStrong (BidiText src)
  {
    int type, lim;

    lim = src.offset + src.count;
    for (int i = src.offset; i < lim; i++)
    {
        type = getChType(src.data[i]);
        if (type == UBAT_L || type == UBAT_R)  return type;
    }
    return UBAT_N;
  }

/*------------------------------------------------------------------------*/
  private static int lastStrong (BidiText src)
  {
    int type, lim;

    lim = src.offset + src.count;

    for (int i = lim - 1; i >= src.offset; i--)
    {
        type = getChType(src.data[i]);
        if (type == UBAT_L || type == UBAT_R)  return type;
    }
    return UBAT_N;
  }

/*------------------------------------------------------------------------*/

  private void BaseLvl ()
  {
    visToVis = false;

    if (ics_orient_in == BidiFlag.ORIENTATION_RTL)
        ucb_basLev = 1; /* 0 = LTR, 1 = RTL */
    else  ucb_basLev = 0;
    if (ics_orient_out == BidiFlag.ORIENTATION_RTL)
        ucb_outLev = 1;
    else  ucb_outLev = 0;
    ucb_curLev = ucb_basLev;
    if (ucb_basLev == 0 && ucb_outLev == 1)
        ucb_curLev=2;

    if ((ics_type_in == BidiFlag.TYPE_VISUAL) && (ics_type_out == BidiFlag.TYPE_VISUAL))
        visToVis = true;

    if (myBdx.roundTrip)
    {
        if (ucb_basLev == 1)
            impTab = impTab_RTL_r;
        else  impTab = impTab_LTR_r;
    }
    else if (myBdx.winCompatible)
    {
        if (ucb_basLev == 1)
            impTab = impTab_RTL;
        else  impTab = impTab_LTR_w;
    }
    else if (insertMarkers)
    {
        if (ucb_basLev == 1)
            impTab = impTab_RTL_m;
        else  impTab = impTab_LTR_m;
        startL2EN = -1;                 /* start of level 2 EN run     */
        lastStrongRTL = -1;             /* index of last found R or AL */
        if (myBdx.insertPoints != null)
            myBdx.insertPoints.setSize(0);
    }
    else
    {
        if (ucb_basLev == 1)
            impTab = impTab_RTL;
        else  impTab = impTab_LTR;
    }
    insertCnt = 0;                      /* number of confirmed inserts */
    removeCnt = 0;                      /* number of LRM/RLMs to remove */
    ucb_impSta = 0;
    ucb_condPos = -1;
  }

/***************************************************************************
   Method       : pass2
   Objectives   : Builds Target and Index buffers making reordering
                  source area based on level area
   Parameters   : None
   Returns      : None.
---------------------------------------------------------------------------*/
  private void  pass2()
  {
    byte lowest_level = 100;
    byte highest_level = 0;
    byte work_level;
    byte current_level;
    int i;
    int flip_from = 0, flip_to = ics_size - 1;

    for (i = 0; i < ics_size; i++)
    {
        if (impToImpPhase != 2)  myBdx.dstToSrcMap[i] = i;
        current_level = myBdx.propertyMap[i];

        if (current_level < lowest_level)
            lowest_level  = current_level;
        if (current_level > highest_level)
            highest_level = current_level;
    }

    if (reqImpToImp && (impToImpOrient == 0))  return;

    if (ics_orient_out == BidiFlag.ORIENTATION_RTL)
        /* if output orientation is RTL, invert until lowest even level */
        lowest_level = (byte)((lowest_level + 1) & ~1);
    else
        /* if output orientation is LTR, invert until lowest odd level */
        lowest_level |= 1;

    for (work_level = highest_level; work_level >= lowest_level; work_level--)
    {
        i = 0;
        while (i < ics_size)
        {
            current_level = myBdx.propertyMap[i];

            if (current_level < work_level)
            {
                i++;
                continue;
            }
            flip_from = i;
            for (i = flip_from+1;
                 (i < ics_size) && (myBdx.propertyMap[i] >= work_level);
                 i++);
            flip_to = i - 1;
            invertMap(myBdx.dstToSrcMap, flip_from, flip_to);
        }
    }
  }

/*------------------------------------------------------------------------*/

  private void  pass3()
  {
    int logPos;
    byte xtype;
    char xchar;
    int i;

    for (i = 0; i < ics_size; i++)
    {
        logPos = myBdx.dstToSrcMap[i];
        xchar = ics_buffer_in[logPos];
        xtype = typeArray[logPos][ORIG];
        if (xtype == UBAT_EN)
        {
            if ((ics_num_flag == BidiFlag.NUMERALS_NATIONAL) ||
                ((ics_num_flag == BidiFlag.NUMERALS_CONTEXTUAL) &&
                 (typeArray[logPos][FINAL] == UBAT_AN)))
                xchar += (0x0660 - 0x0030);
        }
        else if (xtype == UBAT_AN)
        {
            if (ics_num_flag == BidiFlag.NUMERALS_NOMINAL)
                xchar -= (0x0660 - 0x0030);
        }
        else if (xtype == UBAT_N_SWAP)
            xchar = UCQSYMM(xchar);
        ics_buffer_out[i] = xchar;
    }
  }

/*------------------------------------------------------------------------*/
/**
 *  This method reorders a Bidi text according to a specified transformation.
 *  @param  src         The Bidi text to reorder.
 *  @param  dst         The Bidi text after reordering
 *  @param  bdx         The transformation to perform.
 */

  synchronized void order(BidiText src, BidiText dst, BidiTransform bdx)
  {
    BidiFlag orient_save;
    int  i=0, j=0;
    int pos, ipos;
    boolean dstToSrcMapRequired;

    if (src.count < 1)
    {
        if (dst.data == null)
            dst.data = new char[0];
        bdx.inpCount = 0;
        bdx.outCount = 0;
        return;
    }

    /*******************/
    /* Initializations */
    /*******************/

    myBdx = bdx;
    ics_orient_in  = src.flags.getOrientation();
    ics_orient_out = dst.flags.getOrientation();
    if (ics_orient_in == BidiFlag.ORIENTATION_CONTEXT_LTR ||
        ics_orient_in == BidiFlag.ORIENTATION_CONTEXT_RTL)
    {
        int type1 = firstStrong(src);
        switch (type1)
        {
          case UBAT_L:
            ics_orient_in = BidiFlag.ORIENTATION_LTR;
            break;
          case UBAT_R:
            ics_orient_in = BidiFlag.ORIENTATION_RTL;
            break;
          case UBAT_N:
            if (ics_orient_in == BidiFlag.ORIENTATION_CONTEXT_RTL)
                ics_orient_in = BidiFlag.ORIENTATION_RTL;
            else  ics_orient_in = BidiFlag.ORIENTATION_LTR;
        }
    }

    if (ics_orient_out == BidiFlag.ORIENTATION_CONTEXT_LTR ||
        ics_orient_out == BidiFlag.ORIENTATION_CONTEXT_RTL)
    {
        int type1 = firstStrong(src);
        switch (type1)
        {
          case UBAT_L:
            type1 = lastStrong(src);
            if (type1 == UBAT_R)
                ics_orient_out = BidiFlag.ORIENTATION_RTL;
            else  ics_orient_out = BidiFlag.ORIENTATION_LTR;
            break;
          case UBAT_R:
            ics_orient_out = BidiFlag.ORIENTATION_RTL;
            break;
          case UBAT_N:
            if (ics_orient_out == BidiFlag.ORIENTATION_CONTEXT_RTL)
                ics_orient_out = BidiFlag.ORIENTATION_RTL;
            else  ics_orient_out = BidiFlag.ORIENTATION_LTR;
        }
    }

    ics_type_in  = src.flags.getType();
    ics_type_out = dst.flags.getType();
    reqImpToImp = (ics_type_in  == BidiFlag.TYPE_IMPLICIT) &&
                  (ics_type_out == BidiFlag.TYPE_IMPLICIT);

    /* check if implicit to implicit case */
    if (myBdx.impToImp && reqImpToImp &&
        (ics_orient_in != ics_orient_out))
    {
        /* LocalData->symmetric is set for each phase in UCB2VIS */
        if (ics_orient_in == BidiFlag.ORIENTATION_LTR)
        {
            impToImpOrient = IMP_LTR;
        }
        else
        {
            impToImpOrient = IMP_RTL;
        }
    }
    else
    {
        impToImpOrient = 0;
    }
    /* check that insertMarkers is only used for Visual LTR to Implicit LTR/RTL */
    insertMarkers = myBdx.insertMarkers;
    if (insertMarkers)
    {
        if ((ics_type_in != BidiFlag.TYPE_VISUAL) ||
            (ics_type_out != BidiFlag.TYPE_IMPLICIT) ||
            (ics_orient_in != BidiFlag.ORIENTATION_LTR) ||
            myBdx.removeMarkers)
        {
            insertMarkers = false;
        }
        else if (ics_orient_out == BidiFlag.ORIENTATION_RTL)
        {
            ics_orient_in  = BidiFlag.ORIENTATION_RTL;
            ics_orient_out = BidiFlag.ORIENTATION_LTR;
        }
    }

    ics_buffer_in = new char[src.count];
    if ((ics_type_in == BidiFlag.TYPE_VISUAL) &&
        (ics_type_out == BidiFlag.TYPE_IMPLICIT) &&
        (ics_orient_in != ics_orient_out) &&
        !insertMarkers)
    {
        invertInput = true;
        int ofs = src.offset + src.count - 1;
        for (int k = 0; k < src.count; k++)
            ics_buffer_in[k] = src.data[ofs - k];
        ics_orient_in = ics_orient_out;
    }
    else
    {
        invertInput = false;
        System.arraycopy(src.data, src.offset, ics_buffer_in, 0, src.count);
    }

    ics_size = src.count;
    ics_num_flag = dst.flags.getNumerals();

    if (reqImpToImp)
        ics_symmetric = false;
    else  ics_symmetric = (dst.flags.getSwap() != src.flags.getSwap());

    ics_buffer_out = new char[src.count];
    typeArray = new byte[src.count][2];

    if ((myBdx.propertyMap == null) || (myBdx.propertyMap.length < src.count))
        myBdx.propertyMap = new byte[src.count];
    if ((myBdx.dstToSrcMap == null) || (myBdx.dstToSrcMap.length < src.count))
        myBdx.dstToSrcMap = new int[src.count];

    /*----------------------------------------*/
    /* Determination of the base level basLev */
    /*----------------------------------------*/

    if (impToImpOrient > 0)
    {
        impToImpPhase = 1;
        orient_save = ics_orient_out;
        ics_orient_out = BidiFlag.ORIENTATION_LTR;
        ics_symmetric = (src.flags.getSwap() == BidiFlag.SWAP_YES);
        BaseLvl();
        fillTypeArray();
        for (ucb_ix = 0; ucb_ix < ics_size; ucb_ix++)
        {
            ucb_xType = typeArray[ucb_ix][FINAL];
            implicitProcessing();
            myBdx.propertyMap[ucb_ix] = ucb_wTarget;
            if ( (typeArray[ucb_ix][ORIG] == UBAT_N) &&
                 ics_symmetric &&
                 odd(ucb_wTarget) )
                typeArray[ucb_ix][ORIG] = UBAT_N_SWAP;
        }
        /* do Implicit process for UBAT_B to resolve possible conditional string */
        ucb_ix = ics_size;
        ucb_xType = UBAT_B;
        implicitProcessing();
        /* Reverse the map by levels */
        pass2();
        ics_orient_out = orient_save;
        /**************************************************************************/
        /* Re-classify according to the new order                                 */
        /**************************************************************************/
        impToImpPhase = 2;
        orient_save = ics_orient_in;
        if (impToImpOrient == IMP_LTR)
        {
            ics_orient_in = BidiFlag.ORIENTATION_RTL;
            invertMap(myBdx.dstToSrcMap, 0, ics_size - 1);
        }
        else  ics_orient_in = BidiFlag.ORIENTATION_LTR;
        ics_symmetric = (dst.flags.getSwap() == BidiFlag.SWAP_YES);
        BaseLvl();
        fillTypeArray2();
        for (ucb_ix = 0; ucb_ix < ics_size; ucb_ix++)
        {
            ipos = myBdx.dstToSrcMap[ucb_ix];
            ucb_xType = typeArray[ipos][FINAL];
            implicitProcessing();
            myBdx.propertyMap[ucb_ix] = ucb_wTarget;
            if ( (Math.abs(typeArray[ipos][ORIG]) == UBAT_N) &&
                 ics_symmetric &&
                 odd(ucb_wTarget) )
                typeArray[ipos][ORIG] *= -1;
        }
        /* do Implicit process for UBAT_B to resolve possible conditional string */
        ucb_ix = ics_size;
        ucb_xType = UBAT_B;
        implicitProcessing();
        /* Reverse the map by levels */
        pass2();
        ics_orient_in = orient_save;
        ics_symmetric = true;
    }
    else
    {
        BaseLvl();
        fillTypeArray();
        for (ucb_ix = 0; ucb_ix < ics_size; ucb_ix++)
        {
            ucb_xType = typeArray[ucb_ix][FINAL];
            implicitProcessing();
            myBdx.propertyMap[ucb_ix] = ucb_wTarget;
            if ( (typeArray[ucb_ix][ORIG] == UBAT_N) &&
                 ics_symmetric &&
                 odd(ucb_wTarget) )
                typeArray[ucb_ix][ORIG] = UBAT_N_SWAP;
        }
        /* do Implicit process for UBAT_B to resolve possible conditional string */
        ucb_ix = ics_size;
        ucb_xType = UBAT_B;
        implicitProcessing();
        /* Reverse the map by levels */
        pass2();
    }

    /* Pass 3 :The logical to visual mapping must be converted to a visual-
               to-logical mapping.
               European digits which need Arabic-Indic shapes are
               translated; Arabic presentation forms which need shaping are
               replaced by the corresponding nominal letters; symbols which
               need swapping are replaced by their symmetric symbol.  */
    pass3();
    dstToSrcMapRequired = myBdx.dstToSrcMapRequired || myBdx.srcToDstMapRequired;
    if (dstToSrcMapRequired)
    {
        if (invertInput)
            for (i = 0; i < src.count; i++)
                myBdx.dstToSrcMap[i] = src.count - myBdx.dstToSrcMap[i] - 1;
    }

    if (myBdx.removeMarkers)
    {
        char c;

        for (pos = 0; pos < src.count; pos++)
        {
            c = ics_buffer_out[pos];
            if ((c == LRM) || (c == RLM))
            {
                removeCnt++;
                continue;
            }
            if (removeCnt > 0)
            {
                ics_buffer_out[pos-removeCnt] = ics_buffer_out[pos];
                myBdx.dstToSrcMap[pos-removeCnt] = myBdx.dstToSrcMap[pos];
            }
        }
    }

    dst.count = src.count - removeCnt + insertCnt;
    if (dst.data == null)
    {
        if ((dst.offset == 0) && (insertCnt == 0))
            dst.data = ics_buffer_out;
        else  dst.data = new char[dst.offset + dst.count];
    }
    if ((dst.offset + dst.count) > dst.data.length)
    {
        char[] temp = new char[dst.offset + dst.count];
        if (dst.offset > 0)
            System.arraycopy(dst.data, 0, temp, 0, dst.offset);
        dst.data = temp;
        temp = null;
    }

    if (insertCnt > 0)                  /* some LRMs to insert */
    {
        /* n + 0.0: add LRM before char n
         * n + 0.3: add RLM before char n
         * n + 0.6: add LRM after  char n
         * n + 0.8: add RLM after  char n
         */
        float f, g;
        char insert;
        int[] tempMap = null;
        for (i = 0; i < insertCnt; i++)
        {
            f = ((Float)myBdx.insertPoints.get(i)).floatValue();
            ipos = (int)f;
            g = f - ipos;
            if ((g > 0.7) || ((g < 0.5) && (g > 0.2)))
                insert = RLM;
            else  insert = LRM;
            if ( ((ucb_basLev == 1) && (insert == LRM))
                 ||
                 ((ucb_basLev != 1) && (insert == RLM)) )
            {
                for (pos = 0; pos < src.count; pos++)
                    if (ipos == myBdx.dstToSrcMap[pos])
                    {
                        ipos = pos;
                        break;
                    }
                myBdx.insertPoints.setElementAt(new Float(ipos + g), i);
            }
        }
        /*  sorting is needed if the insert points are not in ascending order;
            this happens for RTL destination and would happen for RLM in LTR
            destination but there is no such use                            */
        if (ucb_basLev == 1)
            java.util.Collections.sort(myBdx.insertPoints);
        if (dstToSrcMapRequired)
            tempMap = new int[dst.count];

        pos = 0;
        for (i = 0; i < insertCnt; i++)
        {
            f = ((Float)myBdx.insertPoints.get(i)).floatValue();
            ipos = (int)f;
            g = f - ipos;
            if (g > 0.5)
            {
                ipos++;
                g -= 0.5;
            }
            if (g > 0.2)
                insert = RLM;
            else  insert = LRM;
            System.arraycopy(ics_buffer_out, pos, dst.data, dst.offset+pos+i,
                             ipos - pos);
            dst.data[dst.offset+ipos+i] = insert;
            if (dstToSrcMapRequired)
            {
                System.arraycopy(myBdx.dstToSrcMap, pos, tempMap, pos+i,
                                 ipos - pos);
                tempMap[ipos+i] = -1;
            }
            pos = ipos;
        }
        System.arraycopy(ics_buffer_out, pos, dst.data, dst.offset+pos+insertCnt,
                         dst.count - pos - insertCnt);
        if (dstToSrcMapRequired)
        {
            System.arraycopy(myBdx.dstToSrcMap, pos, tempMap, pos+insertCnt,
                             dst.count - pos - insertCnt);
            myBdx.dstToSrcMap = tempMap;
        }
    }
    else
    {
        if  (dst.data != ics_buffer_out)
            System.arraycopy(ics_buffer_out, 0, dst.data, dst.offset, dst.count);
    }

    if (myBdx.srcToDstMapRequired)
    {
        if ((myBdx.srcToDstMap == null) || (myBdx.srcToDstMap.length < src.count))
            myBdx.srcToDstMap = new int[src.count];
        if (removeCnt > 0)
            java.util.Arrays.fill(myBdx.srcToDstMap, 0, src.count, -1);
        for (i = 0; i < dst.count; i++)
        {
            pos = myBdx.dstToSrcMap[i];
            if (pos >= 0)
                myBdx.srcToDstMap[pos] = i;
        }
    }
    if (myBdx.propertyMapRequired)
    {
        for (i = 0; i < src.count; i++)
            if (typeArray[i][ORIG] != UBAT_NSM)
                bdx.propertyMap[i] |= 0x80;
        if (invertInput)
            invertMap(myBdx.propertyMap, 0, src.count - 1);

    }
    myBdx.inpCount = src.count;
    myBdx.outCount = dst.count;

  }

}

