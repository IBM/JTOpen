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

/**
 *  <p><b>Multi-threading considerations:</b> This class is thread-safe,
 *  since its only public method is synchronized, and all instance variables
 *  are initialized within this method.  However, to avoid delays, each
 *  thread should use its own instances of this class.
 **/

class BidiOrder
{
  private static final String copyright = "Copyright (C) 1997-2004 International Business Machines Corporation and others.";

// Private Variables  :
// ----------------

//private int srcLen;   // length of source text to process

//private String buffer_in ;

  private static final int UBAT_B = 0;            /* block separator   */
  private static final int UBAT_S = 1;            /* segment separator */
  private static final int UBAT_L = 2;            /* left to right     */
  private static final int UBAT_R = 3;            /* right to left     */
  private static final int UBAT_EN = 4;           /* European digit    */
  private static final int UBAT_AN = 5;           /* Arabic-Indic digit */
  private static final int UBAT_ET = 6;           /* European digit terminator */
  private static final int UBAT_ES = 7;           /* European digit separator  */
  private static final int UBAT_CS = 8;           /* common digit separator    */
  private static final int UBAT_W  = 9;           /* white space */
  private static final int UBAT_N  =10;           /* neutral */
  private static final int UBAT_BD =11;           /* bidi special codes */
  private static final int UBAT_AL =12;           /* Arabic Letter      */
  private static final int UBAT_NSM =13;          /* Non Spacing Mark   */

  private static final int ITIL    = 11;
  private static final int ITCOND  = 12;

  private static final byte TONATIONAL_FLAG = 1;
  private static final byte TONOMINAL_FLAG = 2;
  private static final byte CONTEXTUAL_FLAG = 4;
  private static final byte SWAPPING_FLAG = 8;
  private static final byte IMP_LTR = 4;
  private static final byte IMP_RTL = 8;
  private static final byte ORIG = 0;
  private static final byte FINAL = 1;
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
                    /*   B,   S,   L,   R,  EN,  AN,  ET, ES, CS,  W,  N, IL, Cond */

/* 0 L0 text     */  {   0,   0,   0,   3,   0,   1,   0,  0,  0,  0,  0,  0,  0},
/* 1 L0+AN       */  {   0,   0,   0,   3,   0,   1,   4,  4,  2,  4,  4,  2,  0},
/* 2 L0+AN+CS    */  {   0,   0,   0,   3,   0,   1,   4,  4,  4,  4,  4,  2,  1},
/* 3 L1 text     */  {   0,   0,   0,   3,   6,   1,   5,  4,  4,  4,  4,  1,  0},
/* 4 L1 cont     */  {   0,   0,   0,   3,0x66,0x61,   5,  4,  4,  4,  4,  1,  1},
/* 5 L1+ET       */  {   0,   0,   0,   3,0x66,0x41,   5,  4,  4,  4,  4,  1,  1},
/* 6 L1+EN       */  {   0,   0,   0,   3,   6,   1,   8,  7,  7,  4,  4,  2,  0},
/* 7 L1+EN+ES/CS */  {   0,   0,   0,   3,   6,0x41,   5,  4,  4,  4,  4,  2,  1},
/* 8 L1+EN+ET    */  {   0,   0,   0,   3,   6,   1,   5,  4,  4,  4,  4,  2,  0}
  };
/**************************************************************************/
  private static final short impTab_RTL[][] =
  {
                    /*   B,   S,   L,   R,  EN,  AN,  ET, ES, CS,  W,  N, IL, Cond */

/* 0 L0 text     */  {   0,   0,   7,   0,   2,   5,   1,  0,  0,  0,  0,  0,  0},
/* 1 L0+ET       */  {   0,   0,0xA7,   0,   2,0xA5,0x81,  0,  0,  0,  0,  0,  1},
/* 2 L0+EN       */  {   0,   0,   7,   0,   2,   5,   4,  3,  3,  0,  0,  1,  0},
/* 3 L0+EN+ES/CS */  {   0,   0,0x27,   0,   2,0x25,0x21,  0,  0,  0,  0,  1,  1},
/* 4 L0+EN+ET    */  {   0,   0,   7,   0,   2,   5,   1,  0,  0,  0,  0,  1,  0},
/* 5 L0+AN       */  {   0,   0,   7,   0,   2,   5,   1,  0,  6,  0,  0,  1,  0},
/* 6 L0+AN+CS    */  {   0,   0,0x27,   0,0x24,   5,0x21,  0,  0,  0,  0,  1,  1},
/* 7 L1 text     */  {   0,   0,   7,   0,   9,   5,   8,  8,  8,  8,  8,  1,  0},
/* 8 L1+cont     */  {   0,   0,   7,   0,   9,0x25,   8,  8,  8,  8,  8,  1,  1},
/* 9 L1+EN       */  {   0,   0,   7,   0,   9,   5,   7,  8,  8,  8,  8,  1,  0}
  };
/**************************************************************************/
  private static final short impTab_LTR_r[][] =
  {
                    /*   B,   S,   L,   R,  EN,  AN,  ET, ES, CS,  W,  N, IL, Cond */

// 0 L0 text     */  {   0,   0,   0,   3,   0,   1,   0,  0,  0,  0,  0,  0,  0},
// 1 L0+AN       */  {   0,   0,   0,   3,   0,   1,   4,  4,  2,  4,  4,  2,  0},
// 2 L0+AN+CS    */  {   0,   0,   0,   3,   0,   1,   4,  4,  4,  4,  4,  1,  1},
// 3 L1 text     */  {   0,   0,   0,   3,   6,   1,   5,  4,  4,  4,  4,  1,  0},
// 4 L1 cont     */  {   0,   0,   0,0xA3,   6,0xA1,   5,  4,  4,  4,  4,  1,  1},
// 5 L1+ET       */  {   0,   0,   0,0xA3,0xC6,0xA1,   5,  4,  4,  4,  4,  1,  1},
// 6 L1+EN       */  {   0,   0,   0,0xA3,   6,0xA1,   8,  7,  7,  4,  4,  2,  1},
// 7 L1+EN+ES/CS */  {   0,   0,   0,0xA3,0xC6,0xA1,   5,  4,  4,  4,  4,  1,  1},
// 8 L1+EN+ET    */  {   0,   0,   0,0xA3,   6,0xA1,   5,  4,  4,  4,  4,  2,  1}
/* 0 L0 text     */  {   0,   0,   0,   3,   0,   0,   0,  0,  0,  0,  0,  0,  0},
/* 1 L0+AN       */  {   0,   0,   0,   3,   0,   0,   4,  4,  2,  4,  4,  2,  0},
/* 2 L0+AN+CS    */  {   0,   0,   0,   3,   0,   0,   4,  4,  4,  4,  4,  1,  1},
/* 3 L1 text     */  {   0,   0,   0,   3,   6,   6,   5,  4,  4,  4,  4,  1,  0},
/* 4 L1 cont     */  {   0,   0,   0,0xA3,   6,   6,   5,  4,  4,  4,  4,  1,  1},
/* 5 L1+ET       */  {   0,   0,   0,0xA3,0xC6,0xC6,   5,  4,  4,  4,  4,  1,  1},
/* 6 L1+EN       */  {   0,   0,   0,0xA3,   6,   6,   8,  7,  7,  4,  4,  2,  1},
/* 7 L1+EN+ES/CS */  {   0,   0,   0,0xA3,0xC6,0xC6,   5,  4,  4,  4,  4,  1,  1},
/* 8 L1+EN+ET    */  {   0,   0,   0,0xA3,   6,   6,   5,  4,  4,  4,  4,  2,  1}
  };
/**************************************************************************/
  private static final short impTab_RTL_r[][] =
  {
                    /*   B,   S,   L,   R,  EN,  AN,  ET, ES, CS,  W,  N, IL, Cond */

// 0 L0 text     */  {   0,   0,   7,   0,   2,   5,   1,  0,  0,  0,  0,  0,  0},
// 1 L0+ET       */  {   0,   0,0xA7,   0,0xC2,0xA5,   1,  0,  0,  0,  0,  0,  1},
// 2 L0+EN       */  {0xA0,0xA0,   7,0xA0,   2,0xA5,   4,  3,  3, 10, 10,  1,  1},
// 3 L0+EN+ES/CS */  {0xA0,0xA0,   7,0xA0,0xC2,0xA5,  11, 10, 10, 10, 10,  0,  1},
// 4 L0+EN+ET    */  {0xA0,0xA0,   7,0xA0,   2,0xA5,   4, 10, 10, 10, 10,  1,  1},
// 5 L0+AN       */  {   0,   0,   7,   0,   2,   5,   1,  0,  6,  0,  0,  1,  0},
// 6 L0+AN+CS    */  {   0,   0,0xA7,   0,0xA4,   5,0xA1,  0,  0,  0,  0,  0,  1},
// 7 L1 text     */  {   0,   0,   7,   0,   9,   5,   8,  8,  8,  8,  8,  1,  0},
// 8 L1+cont     */  {   0,   0,   7,   0,   9,0xA5,   8,  8,  8,  8,  8,  0,  1},
// 9 L1+EN       */  {   0,   0,   7,   0,   9,   5,   7,  8,  8,  8,  8,  1,  0},
//10 L0+EN+cont  */  {0xA0,0xA0,   7,0xA0,   2,0xA5,  11, 10, 10, 10, 10,  0,  1},
//11 10+ET       */  {0xA0,0xA0,   7,0xA0,0xC2,0xA5,  11, 10, 10, 10, 10,  0,  1}
/* 0 L0 text     */  {   0,   0,   7,   0,   2,   2,   1,  0,  0,  0,  0,  0,  0},
/* 1 L0+ET       */  {   0,   0,0xA7,   0,0xC2,0xC2,   1,  0,  0,  0,  0,  0,  1},
/* 2 L0+EN       */  {0xA0,0xA0,   7,0xA0,   2,   2,   4,  3,  3, 10, 10,  1,  1},
/* 3 L0+EN+ES/CS */  {0xA0,0xA0,   7,0xA0,0xC2,0xC2,  11, 10, 10, 10, 10,  0,  1},
/* 4 L0+EN+ET    */  {0xA0,0xA0,   7,0xA0,   2,   2,   4, 10, 10, 10, 10,  1,  1},
/* 5 L0+AN       */  {   0,   0,   7,   0,   2,   2,   1,  0,  6,  0,  0,  1,  0},
/* 6 L0+AN+CS    */  {   0,   0,0xA7,   0,0xA4,0xA4,0xA1,  0,  0,  0,  0,  0,  1},
/* 7 L1 text     */  {   0,   0,   7,   0,   9,   9,   8,  8,  8,  8,  8,  1,  0},
/* 8 L1+cont     */  {   0,   0,   7,   0,   9,   9,   8,  8,  8,  8,  8,  0,  1},
/* 9 L1+EN       */  {   0,   0,   7,   0,   9,   9,   7,  8,  8,  8,  8,  1,  0},
/*10 L0+EN+cont  */  {0xA0,0xA0,   7,0xA0,   2,   2,  11, 10, 10, 10, 10,  0,  1},
/*11 10+ET       */  {0xA0,0xA0,   7,0xA0,0xC2,0xC2,  11, 10, 10, 10, 10,  0,  1}
  };

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

//  Mati: addlev neutralized because it was implemented badly
//  int ucb_addLev;   // Added Level = 0 or 2 (2 if basLev is 0 and the output direction
                      // (from argument 5) is RTL; 0 otherwise)

  byte ucb_curLev;  // Current Level = between basLev and 15; initially set equal to basLev;
                    // modified by LRE/RLE/LRO/RLO/PDF

  byte ucb_impLev;  // Implicit Level = 0, 1 or 2 relative to curLev as determined by the
                    // Implicit Process; initially set to 0  /.../

  int ucb_impSta;   // Implicit State = between 0 and 18  /.../

/*  int ucb_Over;*/   // Override status = initially set to 0; set to 1 when meeting LRO or
                      // RLO; reset to 0 when meeting LRE or RLE; reset to its preceding
                      // state when meeting PDF

  boolean ucb_araLet;   // Arabic Letter = set to false initially, after meeting LRE, RLE, LRO, RLO,
                    // PDF, after any strong type (L or R) except Arabic letters
                    // (U+0600 to U+065F, U+066D to U+06EF, U+FE70 to U+FEFC);
                    // set to true after any Arabic letter

  int ucb_lineSepPos; // hold the position of a line separator character

  int ucb_condPos;    // hold the position of a conditional string

  int ucb_Shaping;  // Shaping = set to 0 initially; set to 1 after meeting
                    // an Arabic letter which needs shaping


  int ucb_xType;    // Specifies character type for which
                    // will be  computed Implicit level

  byte ucb_wTarget; // value to put in the output area

  int ucb_pType;    // Specifies character type for which
                    // will be  computed Implicit level

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

  int ics_formShp;  // Character Shape selector = set initially according to ReqShaping
                    // (set to 0 if inhibit Arabic presentation form shaping is selected;
                    // set to 1 if activate is selected);
                    // set to 0 when meeting U+206C; set to 1 when meeting U+206D

  boolean ics_symmetric;   // Character Swapping selector = set initially according to Swapping
  // (set to 0 if inhibit swapping is selected;
  // set to 1 if activate is selected);
  // set to 0 when meeting U+206A; set to 1 when meeting U+206B

  BidiFlag ics_orient_in;
  BidiFlag ics_orient_out;
  BidiFlag ics_type_in;
  BidiFlag ics_type_out;
  BidiFlag ics_txtShp_flag;
  BidiTransform myBdx;                  /* local reference of bdx */
  int ics_size;     // length of source text to process
  int ics_size_out;
  int ics_orient;
  int ics_flip_flag;
  int ics_num;
  boolean ics_compc;
  char[] ics_buffer_in;
  char[] ics_buffer_out;
  byte[] specialTreatment;
  boolean visToVis;
  short impTab[][];
  byte typeArray[][];

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

void invertMap(int[] buffer, int lower_limit, int upper_limit)
/* invert a buffer of int, between lower_limit and upper_limit */
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
void fillTypeArray()
{
    int             i, prev;
    byte            cType, wType;
    boolean         isArabic = false;
    byte[][]        ta;

    ta = typeArray;
    for (i = 0; i < ics_size; i++)
    {
        cType = getChType( ics_buffer_in[i], myBdx.wordBreak );
        ta[i][ORIG] = cType;
        ta[i][FINAL] = UBAT_N;
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
                // The following is temporary code until we implement UBAT_AL
                char c = ics_buffer_in[i];
                if (((c >= 0x0600) && (c <= 0x06EF)) ||
                    ((c >= 0xFB50) && (c <= 0xFEFC)))
                    isArabic = true;
                // end of temporary code
                ta[i][FINAL] = UBAT_R;
                break;
            case UBAT_AL:
                isArabic = true;
                ta[i][FINAL] = UBAT_AL;
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
               input data.  We have seen no requirements for such a combination.  */
                if (i <= 0 )  break;
                ta[i][FINAL] = ta[i-1][FINAL];
                break;
        }
    }
}

void fillTypeArray2()
{
    int             i, k, prev;
    byte            cType, wType;
    boolean         isArabic = false;
    byte[][]        ta;

    ta = typeArray;
    for (i = 0; i < ics_size; i++)
    {
        k = myBdx.dstToSrcMap[i];
        cType = ta[k][ORIG];
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
                // The following is temporary code until we implement UBAT_AL
                char c = ics_buffer_in[k];
                if (((c >= 0x0600) && (c <= 0x06EF)) ||
                    ((c >= 0xFB50) && (c <= 0xFEFC)))
                    isArabic = true;
                // end of temporary code
                ta[k][FINAL] = UBAT_R;
                break;
            case UBAT_AL:
                isArabic = true;
                ta[k][FINAL] = UBAT_AL;
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
    byte oldLevel, newLevel;

    newIS   = impTab[ ucb_impSta ][ ucb_xType ];

    Special =  (short)(newIS >> 5);

    newIS = (short)(newIS & 0x1F);   /* keep only 5 low bits             */
    newIL = impTab[newIS][ITIL];     /* ITIL equates 11                  */

    if (Special > 0)
      switch (Special)
      {
        case 1:    /* note a: set characters to level 0 from ucb_condPos until last */
          for (i = ucb_condPos; i < ucb_ix; i++)
          {
            myBdx.propertyMap[i] = ucb_curLev;
          }
          ucb_condPos = -1;
          break;

        case 2:     /* note b: set characters to level 1 from ucb_condPos until last */
          for (i = ucb_condPos; i < ucb_ix; i++)
          {
            myBdx.propertyMap[i] = (byte)(ucb_curLev + 1);
          }
          ucb_condPos = -1;
          break;

        case 3:    /* note c: set characters to level 1 from ucb_condPos until next to last
                                and set last character to level 2 */
          for (i = ucb_condPos; i < ucb_ix; i++)
          {
            myBdx.propertyMap[i] = (byte)(ucb_curLev + 1);
          }
          myBdx.propertyMap[i] = (byte)(ucb_curLev + 2);
          ucb_condPos = -1;

          break;

        case 4:    /* note d: set ucb_condPos at the current character position  */
          ucb_condPos = ucb_ix;
          break;

        case 5:    /* note e: mark that there is no conditional string  */
          ucb_condPos = -1;
          break;

        case 6:    /*         ES, CS or ET before level 2 EN            */
          myBdx.propertyMap[ucb_ix-1] = (byte)(ucb_curLev + 2);
          break;

      }

    sCond = impTab[newIS][ITCOND];    /*      ITCOND equates 12                */

    if (sCond == 0)
    {
      if (ucb_condPos > -1)
      {
        for (i = ucb_condPos; i < ucb_ix; i++)
        {
            oldLevel = myBdx.propertyMap[i];
            newLevel = (byte)(ucb_curLev + newIL);
            myBdx.propertyMap[i] = newLevel;
            if (ics_symmetric && odd(oldLevel ^ newLevel))   /* change parity? */
            /* EN and AN never change parity */
            {
                if (impToImpPhase == 2)
                    pos = myBdx.dstToSrcMap[i];
                else  pos = i;
                /* clear bit 7 of SpecialTreatment */
                specialTreatment[pos] ^= SWAPPING_FLAG;
            }
        }
      }
      /* I believe that the next few 6 lines should be before the previous
         parenthesis as the UBA document denotes in its pseudo-code */

      ucb_condPos = -1;
      if (ucb_lineSepPos >= 0)
      {
        /* set Level area to 0 at ucb_lineSepPos position  */
        myBdx.propertyMap[ucb_lineSepPos] = 0;
        ucb_lineSepPos = -1;
      }
    }
    else if (ucb_condPos == -1)
      ucb_condPos =  ucb_ix;

    ucb_impLev = (byte)newIL;
    ucb_impSta = newIS;

    ucb_wTarget = (byte)(ucb_curLev + ucb_impLev);

  }

/*------------------------------------------------------------------------*/

  private static byte getChType (char x, boolean wordBreak)
  {
    /* This routine gets the type of a certain character */
    if (wordBreak && (x == 0x0020))  return UBAT_S;
    return getChType( x );
  }

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
       ((x > 0x0040)  && (x <= 0x005A)) ||
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
       ((x >= 0x0591) && (x <= 0x065F)) ||
       ((x >= 0x066E) && (x <= 0x06EF)) ||
       ((x >= 0x06FA) && (x <= 0x08FF)) ||
       (x == 0x200F)                    ||
       ((x >= 0xFB20) && (x <= 0xFDFF)) ||
       ((x >= 0xFE70) && (x <= 0xFEFC))
       )
        return UBAT_R;

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
       (x == 0x002B)                    ||
       (x == 0x002D)                    ||
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

    ucb_lineSepPos = -1;
    if (myBdx.roundTrip)
    {
      if (ucb_basLev == 1)
        impTab = impTab_RTL_r;
      else  impTab = impTab_LTR_r;
    }
    else
    {
      if (ucb_basLev == 1)
        impTab = impTab_RTL;
      else  impTab = impTab_LTR;
    }
    ucb_impSta = 0;
    ucb_impLev = 0;
    ucb_condPos = -1;
  }

/***************************************************************************
   Method       : pass1
   Objectives   : Computes level for current processing character (with
                  ucb_ix     offset according to the beginning of source area)
   Parameters   : None.
   Returns      : None.
---------------------------------------------------------------------------*/

  private void  pass1()
  {
    int cType;                  /* current type */
    char cChar;                 /* current char */
    byte treatmentFlag = 0;

    if (ucb_ix ==0)  ucb_pType = UBAT_B;
    ucb_wTarget = 0;
    cChar = ics_buffer_in[ ucb_ix ];
    cType = getChType( cChar, myBdx.wordBreak );

    switch (cType)
    {
      /* Bidi Special Codes */
      case UBAT_BD:

        switch (cChar)
        {
          case 0x206C:  /* Arbabic Shaping Inhibit */
            ics_formShp = 0;
            break;
          case 0x206D:  /* Arbabic Shaping Activate */
            ics_formShp = 1;
            break;
          case 0x206E:  /* Numeric Shape National */
            ics_num_flag = BidiFlag.NUMERALS_NATIONAL;
            break;
          case 0x206F: /* Numeric Shape Nominal */
            ics_num_flag = BidiFlag.NUMERALS_NOMINAL;
            break;
        }
        break;
      /* Block Separator */
      case UBAT_B:
        /* do implicit process for UBAT_B */
        ucb_xType = UBAT_B;
        implicitProcessing();
        ucb_wTarget = 0;
        /* redo Determination of the base level ucb_basLev */
        BaseLvl();
        break;
      /* Segment Separator */
      case UBAT_S:
        if (visToVis)
        {
            cType = UBAT_N;
            ucb_xType = UBAT_N;
            ucb_wTarget = ucb_curLev;
            break;
        }
        /* do implicit process for UBAT_S */
        ucb_xType = UBAT_S;
        implicitProcessing();
        ucb_wTarget = ucb_basLev;
        break;
      default:
        /* source character is Line Separator */
        if (cChar == 0x2028)
        {
          ucb_lineSepPos = ucb_ix;
          ucb_wTarget = 0;
          break;
        }

        if ((cType==UBAT_EN) &&
            (ics_num_flag == BidiFlag.NUMERALS_NATIONAL))
        {
          treatmentFlag = TONATIONAL_FLAG;
//          cType = UBAT_AN;
        }
        if ((cType==UBAT_AN) &&
            (ics_num_flag == BidiFlag.NUMERALS_NOMINAL))
        {
          treatmentFlag = TONOMINAL_FLAG;
//          cType = UBAT_EN;
        }
/*
        if ((cType==UBAT_EN) &&
            (ics_num_flag == BidiFlag.NUMERALS_CONTEXTUAL))
        {
          if (ucb_pType== UBAT_AN)
          {
            treatmentFlag = TONATIONAL_FLAG | CONTEXTUAL_FLAG;
            cType = UBAT_AN;
          }
          else if ((ucb_pType== UBAT_W || ucb_pType== UBAT_N ||
                    ucb_pType==UBAT_CS || ucb_pType==UBAT_ES) &&
                   (ucb_ix > 0 && (specialTreatment[ucb_ix-1] & CONTEXTUAL_FLAG) == 0))
          {
            int i1=0;
            for (i1=ucb_ix-2; i1>=0; i1--)
            {
              ucb_pType=getChType(ics_buffer_in[i1]);
              if ((ucb_pType==UBAT_R ) ||
                  ((specialTreatment[i1] & CONTEXTUAL_FLAG) != 0))
              {
                treatmentFlag = TONATIONAL_FLAG | CONTEXTUAL_FLAG;
                cType = UBAT_AN;
                break;
              }
              if ((ucb_pType==UBAT_L) ||
                  (((ucb_pType==UBAT_EN)) &&
                   ((specialTreatment[i1] & CONTEXTUAL_FLAG) == 0)))
                break;
            }
          }
          else if ((ucb_ix > 0 && (specialTreatment[ucb_ix -1] & CONTEXTUAL_FLAG) != 0) ||
                   (ucb_pType==UBAT_R))
          {
            treatmentFlag = TONATIONAL_FLAG | CONTEXTUAL_FLAG;
            cType = UBAT_AN;
          }
        }
*/
        if ((cType == UBAT_EN) && ucb_araLet)
        {
            cType = UBAT_AN;
            if (ics_num_flag == BidiFlag.NUMERALS_CONTEXTUAL)
                treatmentFlag = TONATIONAL_FLAG | CONTEXTUAL_FLAG;
        }

        if (cType == UBAT_L)
          ucb_araLet = false;

        if (cType == UBAT_R)
        {
          if ((cChar >= 0x0600) && (cChar <= 0x06EF))
          {
            ucb_araLet = true;
            if (ics_txtShp_flag != BidiFlag.TEXT_NOMINAL)
              ucb_Shaping = 1;
          }
          if ((cChar >= 0xFB50) && (cChar <= 0xFEFC))
          {
            ucb_araLet = true;
            if (ics_txtShp_flag != BidiFlag.TEXT_NOMINAL && ics_formShp == 1)
            {
              ucb_Shaping = 1;
            }
          }
        }

/*
Gilan
   Separators change to numbers when surrounded by appropriate numbers.
   Terminators change to numbers when adjacent to an appropriate number.
   Otherwise, separators and terminators change to Other Neutral.
*/
        if (cType == UBAT_ET)
        {
          if (ucb_pType == UBAT_EN)
            cType = UBAT_EN;
          else for (int i2 = ucb_ix+1; i2 < ics_size; i2++)
          {
              ucb_pType = getChType(ics_buffer_in[i2]);
              if (ucb_pType == UBAT_EN)
              {
                if (!ucb_araLet)
                  cType = UBAT_EN;
                break;
              }
              if (ucb_pType != UBAT_ET)
                break;
          }
          if (cType == UBAT_ET)  cType = UBAT_N;
        }

        if (cType == UBAT_ES || cType == UBAT_CS)
        {
          ucb_xType = (ucb_ix +1 < ics_size) ?
                      getChType(ics_buffer_in[ucb_ix+1]):0;
          if (ucb_araLet && (ucb_xType == UBAT_EN))
            ucb_xType = UBAT_AN;
          if (ucb_pType == UBAT_EN)
          {
            cType = (ucb_xType == UBAT_EN)? UBAT_EN:UBAT_N;
          }
          else if (ucb_pType == UBAT_AN && cType == UBAT_CS)
          {
            cType = (ucb_xType == UBAT_AN)? UBAT_AN:UBAT_N;
          }
          else
            cType = UBAT_N;
        }

        /* Find if character is spacing */
        if (UCQSPAC(cChar) == false)
        {
          if ((cType == UBAT_N) && (ucb_ix > 0))
            cType = ucb_pType;

          /*This is the range of Arabic tashkeel characters.
            In case of having a Shadda in the first of buffer or
            on the boundary between english and arabic text, the
            value of "savIL" is 0 which causes this character to
            be processed as an english character.
          */
/*
          if ((cChar >= 0x064B) && (cChar <= 0x0652))
            ucb_wTarget = (byte)(ucb_curLev + ucb_impLev);
          else
            ucb_wTarget = (byte)(ucb_curLev + savIL);
          if (ucb_ix == 0)  cType = UBAT_N;
          else  cType = ucb_pType;
          if (ucb_condPos == ucb_ix) ucb_condPos = -1;
*/
        }

        /* do implicit Process for cType */
        ucb_xType = cType;

        if (visToVis)  ucb_xType = UBAT_N;
        implicitProcessing();

        if ((cType == UBAT_N) && ics_symmetric &&
            odd(ucb_curLev+ucb_impLev) )
          treatmentFlag = SWAPPING_FLAG;

        break;   /* Case End */
    }

    ucb_pType = cType;
    myBdx.propertyMap[ucb_ix] = ucb_wTarget; /* put ucb_wTarget in target area at
                                                       position ucb_ix  */
    specialTreatment[ucb_ix] = treatmentFlag;
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
    if ((lowest_level % 2) == 0)  /* We must reverse until the lowest _odd_ level */
        ++lowest_level;

    for (work_level = highest_level; work_level >= lowest_level; work_level--)
    {
        i = 0;
        while (i < ics_size)
        {
            current_level = myBdx.propertyMap[i];

            if (current_level < work_level)
                i++;
            else
            {
                flip_from = i;
                while ((current_level = myBdx.propertyMap[i]) >= work_level)
                {
                    flip_to = i;
                    i++;
                    if (i >= ics_size)
                        break;
                }
                invertMap( myBdx.dstToSrcMap, flip_from, flip_to );
            }
        }
    }
    if (ics_orient_out == BidiFlag.ORIENTATION_RTL)
        invertMap( myBdx.dstToSrcMap, 0, ics_size - 1 );
  }

/*------------------------------------------------------------------------*/

  private void  pass3()
  {
    int logPos;
    byte xTran;
    char xchar;

    for (ucb_ix = 0; ucb_ix < ics_size; ucb_ix++)
    {
      logPos = myBdx.dstToSrcMap[ucb_ix];
      xTran = specialTreatment[logPos];
      xchar = ics_buffer_in[logPos];

      if ((xTran & TONATIONAL_FLAG) > 0)
      {
        if ((xchar >= 0x0030) && (xchar <= 0x0039))
          xchar += (0x0660 - 0x0030);
      }
      else if ((xTran & TONOMINAL_FLAG) > 0)
      {
        if ((xchar >= 0x0660) && (xchar <= 0x0669))
          xchar -= (0x0660 - 0x0030);
      }
      else if ((xTran & SWAPPING_FLAG) > 0)
        xchar = UCQSYMM(xchar);

      ics_buffer_out[ucb_ix] = xchar;
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
    int  i=0, j=0, x=0, RC=0;
    int pos, ipos;
    if (src.count < 1)
      return;


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
        /* The following allocation should be grouped with all other allocations
           once all transformations are changed to use typeArray */
        typeArray = new byte[src.count][2];
    }
    else
    {
        impToImpOrient = 0;
        typeArray = null;
    }

    ics_buffer_in = new char[src.count];
    if ((ics_type_in == BidiFlag.TYPE_VISUAL) &&
        (ics_type_out == BidiFlag.TYPE_IMPLICIT) &&
        (ics_orient_in != ics_orient_out))
    {
      int ofs = src.offset + src.count - 1;
      for (int k = 0; k < src.count; k++)
        ics_buffer_in[k] = src.data[ofs - k];
      ics_orient_in = ics_orient_out;
    }
    else  System.arraycopy(src.data, src.offset, ics_buffer_in, 0, src.count);

    ics_size_out=0;
    ics_size = src.count;
    ics_num_flag = dst.flags.getNumerals();

    ics_txtShp_flag = dst.flags.getText();

    ics_compc = false;
    ics_formShp = 0;
    if (reqImpToImp)
        ics_symmetric = false;
    else  ics_symmetric = (dst.flags.getSwap() != src.flags.getSwap());

    ics_buffer_out = new char [ics_buffer_in.length];

    if ((myBdx.propertyMap == null) || (myBdx.propertyMap.length < src.count))
        myBdx.propertyMap = new byte[src.count];
    if ((myBdx.dstToSrcMap == null) || (myBdx.dstToSrcMap.length < src.count))
        myBdx.dstToSrcMap = new int[src.count];
    specialTreatment = new byte[src.count];

    ucb_ix=0;

    ucb_pType = UBAT_B;
    ucb_Shaping=0;
    ucb_araLet = false;

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
        for (pos = 0; pos < ics_size; pos++)
        {
            ucb_xType = typeArray[pos][FINAL];
            ucb_ix = pos;
            implicitProcessing();
            myBdx.propertyMap[pos] = ucb_wTarget;
            if ( (ucb_xType == UBAT_N) &&
                 ics_symmetric &&
                 odd(ucb_wTarget) )
                specialTreatment[pos] = SWAPPING_FLAG;
            else  specialTreatment[pos] = 0;
        }
        /* do Implicit process for UBAT_B to resolve possible conditional string */
        ucb_ix = ics_size;
        ucb_xType = UBAT_B;
        implicitProcessing();
        /**************************************************************************/
        /* Reverse the map by levels                                              */
        /**************************************************************************/
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
            invertMap( myBdx.dstToSrcMap, 0, ics_size - 1);
        }
        else  ics_orient_in = BidiFlag.ORIENTATION_LTR;
        ics_symmetric = (dst.flags.getSwap() == BidiFlag.SWAP_YES);
        BaseLvl();
        fillTypeArray2();
        for (pos = 0; pos < ics_size; pos++)
        {
            ipos = myBdx.dstToSrcMap[pos];
            ucb_xType = typeArray[ipos][FINAL];
            ucb_ix = pos;
            implicitProcessing();
            myBdx.propertyMap[pos] = ucb_wTarget;
            if ( (ucb_xType == UBAT_N) &&
                 ics_symmetric &&
                 odd(ucb_wTarget) )
                specialTreatment[ipos] ^= SWAPPING_FLAG;
        }
        /* do Implicit process for UBAT_B to resolve possible conditional string */
        ucb_ix = ics_size;
        ucb_xType = UBAT_B;
        implicitProcessing();
        /**************************************************************************/
        /* Reverse the map by levels                                              */
        /**************************************************************************/
        pass2();
        ics_orient_in = orient_save;
        ics_symmetric = true;
    }
    else
    {
        BaseLvl();
        while (ucb_ix < ics_size)
        {
          pass1();
          ucb_ix++ ;
        }

        /* do Implicit process for UBAT_B to resolve possible conditional string */
        ucb_xType = UBAT_B;

        implicitProcessing();

        /* Pass 2 :This pass must not be executed when there is no request
                   for reordering */

        pass2();
    }

    /* Pass 3 :The logical to visual mapping must be converted to a visual-
               to-logical mapping.
               European digits which need Arabic-Indic shapes are
               translated; Arabic presentation forms which need shaping are
               replaced by the corresponding nominal letters; symbols which
               need swapping are replaced by their symmetric symbol.  */
    pass3();

    if (myBdx.srcToDstMapRequired)
    {
      if ((myBdx.srcToDstMap == null) || (myBdx.srcToDstMap.length < src.count))
          myBdx.srcToDstMap = new int[src.count];
      for (i = 0; i< ics_size; i++)
        myBdx.srcToDstMap[myBdx.dstToSrcMap[i]] = i;
    }
    if (myBdx.propertyMapRequired)
    {
        if (typeArray != null)
            for (i = 0; i < src.count; i++)
                if (typeArray[i][ORIG] != UBAT_NSM)
                    bdx.propertyMap[i] |= 0x80;
    }

    System.arraycopy(ics_buffer_out, 0, dst.data, dst.offset, src.count);
    dst.count = src.count;

  }

}

