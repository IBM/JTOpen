///////////////////////////////////////////////////////////////////////////////
//                                                                             
// AS/400 Toolbox for Java - OSS version                                       
//                                                                             
// Filename: BidiOrder.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

/**
 *  <p><b>Multi-threading considerations:</b> This class is thread-safe,
 *  since its only public method is synchronized, and all instance variables
 *  are initialized within this method.  However, to avoid delays, each
 *  thread should use its own instances of this class.
 **/

class BidiOrder {
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


// Private Variables  :
// ----------------

//private int srcLen;   // length of source text to process

//private String buffer_in ;

private static final int UBAT_B = 0;            /* block separator   */
private static final int UBAT_S = 1;            /* segment separator */
private static final int UBAT_L = 2;            /* left to right     */
private static final int UBAT_R = 3;            /* right to left     */
private static final int UBAT_EN = 4;           /* european digit    */
private static final int UBAT_AN = 5;           /* Arabic-Indic digit */
private static final int UBAT_ET = 6;           /* european digit terminator */
private static final int UBAT_ES = 7;           /* european digit separator  */
private static final int UBAT_CS = 8;           /* common digit separator    */
private static final int UBAT_W  = 9;           /* white space */
private static final int UBAT_N  =10;           /* neutral */
private static final int UBAT_BD =11;           /* bidi special codes */

private static final int ITIL    = 11;
private static final int ITCOND  = 12;

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
private static final short impTab[][] =
{

                     /* B, S,  L,  R,  EN,  AN, ET, ES, CS,  W,  N, IL, Cond */

/* 0 L0 text      */  { 0, 0,  0,  3,   0,   1,  0,  0,  0,  0,  0,  0,  0},
/* 1 L0+AN        */  { 0, 0,  0,  3,   0,   1,  4,  4,  2,  4,  4,  2,  0},
/* 2 L0+AN+cont   */  { 0, 0,  0,  3,   0,   1,  4,  4,  4,  4,  4,  2,  1},
/* 3 L1 text      */  { 0, 0,  0,  3,   6,   1,  5,  4,  4,  4,  4,  1,  0},
//@bd11c/* 4 L1 cont      */  { 0, 0,  0,  3,0xA6,0xA1,  5,  4,  4,  4,  4,  1,  1},
/* 4 L1 cont      */  { 0, 0,  0,  3,0x66,0x61,  5,  4,  4,  4,  4,  1,  1},
/* 5 L1+prefix    */  { 0, 0,  0,  3,0x66,0x41,  5,  4,  4,  4,  4,  1,  1},
/* 6 L1+EN        */  { 0, 0,  0,  3,   6,   1,  8,  7,  7,  4,  4,  2,  0},
/* 7 L1+EN+cont   */  { 0, 0,  0,  3,   6,0x41,  5,  4,  4,  4,  4,  2,  1},
/* 8 L1+EN+suffix */  { 0, 0,  0,  3,   6,   1,  5,  4,  4,  4,  4,  2,  0},

/* 9 L0 text      */  { 9, 9, 16,  9,  11,  14, 10,  9,  9,  9,  9,  0,  0},
/* 10 L0+prefix   */  { 9, 9,0xB0, 9,  11,0xAE,0x8A, 9,  9,  9,  9,  0,  1},
/* 11 L0+EN       */  { 9, 9, 16,  9,  11,  14, 13, 12, 12,  9,  9,  1,  0},
/* 12 L0+EN+cont  */  { 9, 9,0x30, 9,  11,0x2E,0x2A, 9,  9,  9,  9,  1,  1},
/* 13 L0+EN+suffix*/  { 9, 9, 16,  9,  11,  14, 10,  9,  9,  9,  9,  1,  0},
/* 14 L0+AN       */  { 9, 9, 16,  9,  11,  14, 10,  9, 15,  9,  9,  1,  0},
/* 15 L0+AN+cont  */  { 9, 9,0x30, 9,0x2B,  14,0x2A, 9,  9,  9,  9,  1,  1},
/* 16 L1 text     */  { 9, 9, 16,  9,  18,  14, 17, 17, 17, 17, 17,  1,  0},
/* 17 L1+cont     */  { 9, 9, 16,  9,  18,0x2E, 17, 17, 17, 17, 17,  1,  1},
/* 18 L1+EN       */  { 9, 9, 16,  9,  18,  14, 16, 17, 17, 17, 17,  1,  0}
/**************************************************************************/
};

/***************************/
/*                         */
/*  Fields from ucbStruct  */
/*                         */
/***************************/

    int ucb_ix ;      // index of currently processed character in source order
                      //  target area

    int ucb_outLev;  // Output Level = 0 or 1 according to the direction of output
                         // specified in argument 5 (0 = LTR, 1 = RTL)

    int ucb_basLev;  // Base Level = 0 or 1 (0 = LTR, 1 = RTL)

//  Mati: addlev neutralized because it was implemented badly
//  int ucb_addLev;   // Added Level = 0 or 2 (2 if basLev is 0 and the output direction
                      // (from argument 5) is RTL; 0 otherwise)

    int ucb_curLev;   // Current Level = between basLev and 15; initially set equal to basLev;
                      // modified by LRE/RLE/LRO/RLO/PDF

    int ucb_impLev;   // Implicit Level = 0, 1 or 2 relative to curLev as determined by the
                      // Implicit Process; initially set to 0  /.../

    int ucb_impSta;   // Implicit State = between 0 and 18  /.../

/*  int ucb_Over;*/   // Override status = initially set to 0; set to 1 when meeting LRO or
                      // RLO; reset to 0 when meeting LRE or RLE; reset to its preceding
                      // state when meeting PDF

    int ucb_araLet;   // Arabic Letter = set to 0 initially, after meeting LRE, RLE, LRO, RLO,
                      // PDF, after any strong type (L or R) except Arabic letters
                      // (U+0600 to U+065F, U+066D to U+06EF, U+FE70 to U+FEFC);
                      // set to 1 after any Arabic letter

    int ucb_lineSepPos; // hold the position of a line separator character

    int ucb_condPos;    // hold the position of a conditional string

    int ucb_Compac;   // Compaction = set to 0 initially; set to 1 after meeting LRM, RLM,
                      // LRE, RLE, LRO, RLO or PDF if argument 8 is U+FFFF, or if a Lam + Alef
                      // is contracted into a Lamalef ligature for Arabic text

    int ucb_Shaping;  // Shaping = set to 0 initially; set to 1 after meeting
                      // an Arabic letter which needs shaping


    int ucb_xType;    // Specifies character type for which
                      // will be  computed Implicit level

    int ucb_wTarget;  // value to put in the output area

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
    int ics_boc_flag;
    int ics_size;     // length of source text to process
    int ics_size_out;
    int ics_orient;
    int ics_wordbreak;
    int ics_flip_flag;
    int ics_num;
    boolean ics_compc;
    char[] ics_buffer_in;
    char[] ics_buffer_out;
    byte[] ics_A_level;
    int[]  ics_SrcToTrgMap;
    int[]  ics_TrgToSrcMap;
    boolean notImpToImp;
    boolean visToVis;

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

    private static boolean UCQSPAC(char x)
    {
        int low, high, mid;

        low = 0;
        high = 59;

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

/****************************************************************************

   Method       : implicitProcessing()
   Objectives   : Computes Implicit level for passed character type
   Parameters   : None.

   Returns      : None.
----------------------------------------------------------------------------*/
private void implicitProcessing()
{
   int i=0;
   short  newIL, sCond;
   int newIS ,Special;

   newIS   = (int)impTab[ ucb_impSta ][ ucb_xType ];

   Special =  newIS >> 5;

   newIS = newIS & 0x1F;            /* keep only 5 low bits                   */
   newIL = impTab[newIS][ITIL];     /* ITIL equates 11                        */

   if (Special > 0)
     switch(Special)
      {
        case 1:    /* note a: set characters to level 0 from ucb_condPos until last */
                for(i=ucb_condPos; i < ucb_ix; i++)
                 {
                   ics_TrgToSrcMap[i] = ics_TrgToSrcMap[i] & 0xC000;
                   ics_TrgToSrcMap[i] = ics_TrgToSrcMap[i] |(ucb_curLev);
                 }
                ucb_condPos = -1;                                 //@bd11a
                break;

        case 2:     /* note b: set characters to level 1 from ucb_condPos until last */
               for(i=ucb_condPos; i < ucb_ix; i++)
                {
                 ics_TrgToSrcMap[i] = ics_TrgToSrcMap[i] & 0xC000;
                 ics_TrgToSrcMap[i] = ics_TrgToSrcMap[i] |(ucb_curLev+1);
                }
                ucb_condPos = -1;                                 //@bd11a
               break;

        case 3:    /* note c: set characters to level 1 from ucb_condPos until next to last
                                and set last character to level 2 */
             for(i=ucb_condPos; i < ucb_ix; i++)
              {
                ics_TrgToSrcMap[i] = ics_TrgToSrcMap[i] & 0xC000;
                ics_TrgToSrcMap[i] = ics_TrgToSrcMap[i] |(ucb_curLev+1);
              }
//@bd11c             ics_TrgToSrcMap[i]++;
              ics_TrgToSrcMap[i]+=ucb_curLev+2;
              ucb_condPos = -1;                                 //@bd11a

                break;

        case 4:    /* note d: set ucb_condPos at the current character position  */
                 ucb_condPos = ucb_ix;
                 break;

        case 5:    /* note e: mark that there is no conditional string  */
                ucb_condPos = -1;
                break;

      }

   sCond = impTab[newIS][ITCOND];    /*      ITCOND equates 12                */
   if (sCond == 0)
     {
       if ( ucb_condPos > -1)
         {
           for(i= ucb_condPos; i <  ucb_ix; i++)
                 {
                    ics_TrgToSrcMap[i] = ics_TrgToSrcMap[i] & 0xC000;
                    ics_TrgToSrcMap[i] = ics_TrgToSrcMap[i] |
                                        ( ucb_curLev+newIL);
                    if ( (( ucb_curLev+newIL) % 2 == 0) &&
//@BD2D                       ( (ics_buffer_in.charAt(i) != 0x206C) ||
//@BD2D                       (ics_buffer_in.charAt(i) != 0x206F)  ) )

                       ( (ics_buffer_in[i] != 0x206C) ||                //@BD2A
                         (ics_buffer_in[i] != 0x206F)  ) )              //@BD2A
                       /* clear bit 15 */
                       ics_TrgToSrcMap[i] = ics_TrgToSrcMap[i] & 0x7FFF;
                  }
          }
       /* I beleive that the next few 6 lines should be before the previous
          parenthesis as the UBA document denotes in its pseudo-code */

         ucb_condPos = -1;
        if ( ucb_lineSepPos >= 0)
           {
         /* set Level area to 0 at ucb_lineSepPos position  */
              ics_TrgToSrcMap[ ucb_lineSepPos]=0;
              ucb_lineSepPos = -1;
           }
      }
    else if ( ucb_condPos == -1)
        ucb_condPos =  ucb_ix;

   ucb_impLev = newIL;
   ucb_impSta = newIS;

   ucb_wTarget =  ucb_wTarget | ( ucb_curLev + ucb_impLev );
}

/*------------------------------------------------------------------------*/

    private int getChType (char x)
    {
      /* This routine gets the type of a certain character */

        if (x == 0x2029)  return UBAT_B;
        if (x == 0x0009)  return UBAT_S;
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
            ((x >= 0x05D0) && (x <= 0x065F)) ||
            ((x >= 0x066E) && (x <= 0x06EF)) ||
            ((x >= 0x06FA) && (x >= 0x08FF)) ||
            (x == 0x200F)                    ||
            ((x >= 0xFB20) && (x <= 0xFDFF)) ||
            ((x >= 0xFE70) && (x <= 0xFEFC))
           )
            return UBAT_R;
        if (
            ((x >= 0x0030) && (x <= 0x0039)) ||
            ((x >= 0x00B2) && (x <= 0x00B3)) ||
            (x == 0x00B9)                    ||
            ((x >= 0x00BC) && (x <= 0x00BE)) ||
            (x == 0x2070)                    ||
            ((x >= 0x2074) && (x <= 0x2079)) ||
            ((x >= 0x2080) && (x <= 0x2089)) ||
            ((x >= 0x2153) && (x <= 0x2182)) ||
            ((x >= 0xFF10) && (x <= 0xFF19))
           )
            return UBAT_EN;
        if (
            ((x >= 0x0660) && (x <= 0x0669)) ||
            ((x >= 0x066B) && (x <= 0x066C)) ||
            ((x >= 0x06F0) && (x <= 0x06F9))
           )
            return UBAT_AN;
        if (
            ((x >= 0x0023) && (x <= 0x0025)) ||
            (x == 0x002B)                    ||
            (x == 0x002D)                    ||
            ((x >= 0x00A2) && (x <= 0x00A5)) ||
            ((x >= 0x00B0) && (x <= 0x00B1)) ||
            (x == 0x066A)                    ||
            ((x >= 0x2030) && (x <= 0x2033)) ||
            ((x >= 0x207A) && (x <= 0x207B)) ||
            ((x >= 0x208A) && (x <= 0x208B)) ||
            ((x >= 0x20A0) && (x <= 0x20CF)) ||
            ((x >= 0x2212) && (x <= 0x2213)) ||
            (x == 0xFE5F)                    ||
            ((x >= 0xFE62) && (x >= 0xFE63)) ||
            ((x >= 0xFE69) && (x <= 0xFE6A)) ||
            ((x >= 0xFF03) && (x >= 0xFF05)) ||
            (x == 0xFF0B)                    ||
            (x == 0xFF0D)                    ||
            ((x >= 0xFFE0) && (x <= 0xFFE1)) ||
            ((x >= 0xFFE5) && (x <= 0xFFE6))
           )
            return UBAT_ET;
        if (
            (x == 0x002F)                    ||
            (x == 0xFE52)                    ||
            ((x >= 0xFF0E) && (x <= 0xFF0F))
           )
            return UBAT_ES;
        if (
            (x == 0x002C) || (x == 0x003A) || (x == 0xFE50) ||
            (x == 0xFE55) || (x == 0xFF0C) || (x == 0xFF1A) ||
            (x == 0x002E) || (x == 0x2007)
           )
            return UBAT_CS;
        if (
            (x == 0x0020) || (x == 0x00A0) ||
            ((x >= 0x2000) && (x <= 0x2006)) ||
            ((x >= 0x2008) && (x <= 0x200B)) ||
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

    private int firstStrong (char[] data, int length)
    {
        int type;

        for (int i = 0; i < length; i++)
        {
            type = getChType(data[i]);
            if (type == UBAT_L || type == UBAT_R)  return type;
        }
        return UBAT_N;
    }

/*------------------------------------------------------------------------*/

    private void BaseLvl ()
    {
        if (ics_orient_in == BidiFlag.ORIENTATION_CONTEXT_LTR ||
            ics_orient_in == BidiFlag.ORIENTATION_CONTEXT_RTL)
        {
            int type1 = firstStrong(ics_buffer_in, ics_size);
            switch (type1) {
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

        visToVis = false;

        if (ics_type_in == BidiFlag.TYPE_VISUAL  &&
            ics_type_out == BidiFlag.TYPE_IMPLICIT)
        {
            if (ics_orient_out == BidiFlag.ORIENTATION_RTL)
                ucb_basLev = 1; /* 0 = LTR, 1 = RTL */
            else  ucb_basLev = 0;
            ucb_outLev = 0;
            ucb_curLev = ucb_basLev;
            if ( ucb_basLev == 0 &&
                 ics_orient_in  == BidiFlag.ORIENTATION_LTR )
                ucb_curLev=2;
        }
        else
        {
            if (ics_orient_in == BidiFlag.ORIENTATION_RTL)
                ucb_basLev = 1; /* 0 = LTR, 1 = RTL */
            else  ucb_basLev = 0;
            if (ics_orient_out == BidiFlag.ORIENTATION_RTL)
                ucb_outLev = 1;
            else  ucb_outLev = 0;
            ucb_curLev = ucb_basLev;
            if ( ucb_basLev == 0 && ucb_outLev == 1 )
                ucb_curLev=2;
            if (ics_type_in == BidiFlag.TYPE_VISUAL) /* Vis to Vis */
                visToVis = true;
        }

        ucb_lineSepPos = -1;
        ucb_impSta = (ucb_curLev & 1) * 9;
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
//@bd11m  int pType;
        int savIL;
//@bd2a
        if (ucb_ix ==0)  ucb_pType = UBAT_B;

//@bd11d
//  if(ucb_ix >=1)
////@BD2D    pType=getChType(ics_buffer_in.charAt(ucb_ix-1) );
//    pType=getChType(ics_buffer_in[ucb_ix-1] );
//  else
//    pType=0;

        ucb_wTarget = 0;

        cChar = ics_buffer_in[ ucb_ix ];
        cType = getChType(cChar);

        switch(cType)
        {
        /* Bidi Special Codes */
        case UBAT_BD:
            switch(cChar)
            {
            case 0x206C:  /* Arbabic Shaping Inhibit */
                if (ics_boc_flag == 0xFFFF)
                    ucb_Compac = 1;
                if (ics_boc_flag != 0)
                    ucb_wTarget |= 0x8000;
                ics_formShp = 0;
                break;
            case 0x206D:  /* Arbabic Shaping Activate */
                if (ics_boc_flag == 0xFFFF)
                    ucb_Compac = 1;
                if (ics_boc_flag != 0)
                    ucb_wTarget |= 0x8000;
                ics_formShp = 1;
                break;
            case 0x206E:  /* Numeric Shape National */
                if (ics_boc_flag == 0xFFFF)
                    ucb_Compac = 1;
                if (ics_boc_flag != 0)
                    ucb_wTarget |= 0x8000;
                ics_num_flag = BidiFlag.NUMERALS_NATIONAL;
                break;
            case 0x206F: /* Numeric Shape Nominal */
                if (ics_boc_flag == 0xFFFF)
                    ucb_Compac = 1;
                if (ics_boc_flag != 0)
                    ucb_wTarget |= 0x8000;
                ics_num_flag = BidiFlag.NUMERALS_NOMINAL;
                break;
            }
            break;
        /* Block Separator */
        case UBAT_B:
            /* do implicit process for UBAT_B */
            ucb_wTarget = 0;
            /* redo Determination of the base level ucb_basLev */
            BaseLvl();
            break;
        /* Segment Separator */
        case UBAT_S:
            /* do implicit process for UBAT_S */
            ucb_wTarget = ucb_basLev;
            /* @bd11a */
            ucb_xType = UBAT_S;
            implicitProcessing();
            break;
        default:
            /* source character is Line Separator */
            if (cChar == 0x2028)
            {
                ucb_lineSepPos = ucb_ix;
                ucb_wTarget = 0;
                break;
            }

            /* source character is LRM or RLM */
            if ( (cChar == 0x200E) ||
                 (cChar == 0x200F) )
            {
                if (ics_boc_flag == 0xFFFF)
                    ucb_Compac = 1;
                else if (ics_boc_flag != 0)
                    ucb_wTarget |= 0x8000;
            }

        if ( (cType==UBAT_EN) &&
             (ics_num_flag == BidiFlag.NUMERALS_NATIONAL) )
        {
            ucb_wTarget |= 0x8000;
            cType = UBAT_AN;
        }
        if( (cType==UBAT_EN) &&
            (ics_num_flag == BidiFlag.NUMERALS_CONTEXTUAL) )
//@bd11D           && (ucb_araLet == 1) )
        {
            if (ucb_pType== UBAT_AN ) {
                ucb_wTarget |= 0xa000;          //@bd11D
                cType = UBAT_AN;                //@bd11D
            }
            else  if((ucb_pType== UBAT_W || ucb_pType== UBAT_N ||
                      ucb_pType==UBAT_CS || ucb_pType==UBAT_ES) &&
                     (ucb_ix > 0 && (ics_TrgToSrcMap[ucb_ix-1] & 0x2000) ==0)) {
                int i1=0;
                for (i1=ucb_ix-2; i1>=0; i1--) {
                    ucb_pType=getChType(ics_buffer_in[i1]);
                    if ( (ucb_pType==UBAT_R ) ||
                         ((ics_TrgToSrcMap[i1] & 0x2000) !=0) ) {
                        ucb_wTarget |= 0xa000;
                        cType = UBAT_AN;
                        break;
                    }
                    else  if ( (ucb_pType==UBAT_L) ||
                               (((ucb_pType==UBAT_EN)) &&
                                ((ics_TrgToSrcMap[i1] & 0x2000) ==0)) ) {
                        break;
                    }
                    if ( (ucb_pType==UBAT_L) ||
                         (((ucb_pType==UBAT_EN)) &&
                          ((ics_TrgToSrcMap[i1] & 0x2000) ==0)) )
                        break;
                }
            }
            else  if ( (ucb_ix > 0 && (ics_TrgToSrcMap[ucb_ix -1] & 0x2000) != 0) ||
                       (ucb_pType==UBAT_R) ) {
                ucb_wTarget |= 0xa000;
                cType = UBAT_AN;
            }
        }

        if (cType == UBAT_L)
            ucb_araLet = 0;

        if (cType == UBAT_R)
        {
            if ((cChar >= 0x0600) && (cChar <= 0x06EF))
            {
                ucb_araLet = 1;
                if (ics_txtShp_flag != BidiFlag.TEXT_NOMINAL)
                    ucb_Shaping = 1;
            }
            if ((cChar >= 0xFB50) && (cChar <= 0xFEFC))             //@BD2A
            {
                ucb_araLet = 1;
                if (ics_txtShp_flag != BidiFlag.TEXT_NOMINAL && ics_formShp == 1)
                {
                    ucb_Shaping = 1;
                    ucb_wTarget |= 0x8000;
                }
            }
        }
//@bd11a Start add

/*
Gilan
   Separators change to numbers when surrounded by appropriate numbers.
   Terminators change to numbers when adjacent to an appropriate number.
   Otherwise, separators and terminators change to Other Neutral.
*/
        if (cType == UBAT_ET )
        {
            int iFlag=0, i2=0;

            if(ucb_pType == UBAT_EN )
                cType = UBAT_EN;
            else if(ucb_pType == UBAT_AN )                   //@bd11A
                cType = UBAT_N;                             //@bd11A

            while(iFlag == 0)
            {
                if (ucb_ix+i2+1 < ics_size)
                {
                    ucb_pType = getChType(ics_buffer_in[ ucb_ix +i2+1]);
                }
                //@bd12a
                else {                                          //@bd12a
                    iFlag = 1;                          //@bd12a
                    continue;                           //@bd12a
                }
                //Gilan end
                if(ucb_pType == UBAT_EN )
                {
                    if(ucb_araLet == 1)
                        cType = UBAT_N;                           //@bd11A
                    else                                        //@bd11A
                        cType = UBAT_EN;                           //@bd11A
                    iFlag=1;
                }
                else  if (ucb_pType == UBAT_AN ) {               //@bd11A
                    cType = UBAT_N;                         //@bd11A
                    iFlag=1;                                //@bd11A
                }                                             //@bd11A
                if(ucb_pType == UBAT_ET)
                {
                    i2++;
                    continue;
                }
                else  iFlag=1;
            }
        }

        if (cType == UBAT_ES || cType == UBAT_CS ) {
            ////@bd12a Gilan new
            ucb_xType = (ucb_ix +1 < ics_size) ?
                         getChType(ics_buffer_in[ucb_ix+1]):0; //@bd12a
            //gilan end
            if (ucb_pType == UBAT_EN ) {
                cType = (ucb_xType == UBAT_EN)? UBAT_EN:UBAT_N;
            }
            else if (ucb_pType == UBAT_AN && cType == UBAT_CS) {
                cType = (ucb_xType == UBAT_EN)? UBAT_AN:UBAT_N;
                //Gilan new
            }
//  Mati: I comment out the following 4 lines since not Unicode conformant
//          else if (ucb_xType == UBAT_EN && ics_buffer_in[ucb_ix] == 0x002E) {  //@bd12a
//              ics_buffer_in[ucb_ix] = 0x002C;                                     //@bd12a
//              cType = UBAT_N;                                             //@bd12a
//          }
            else                                                        //@bd12a
                cType = UBAT_N;                                             //@bd12a
        }
//@bd11a End  add
        savIL = ucb_impLev;
        /* do implicit Process for cType */
        ucb_xType = cType;

        if (visToVis)  ucb_xType = UBAT_N;
        implicitProcessing();

        /* Find if character is spacing */
        if ( UCQSPAC(cChar) == false) {
            ucb_wTarget = (ucb_curLev + savIL) | 0x4000;
            if (ucb_ix == 0)  cType = UBAT_N;  else cType = ucb_pType;
            if (ucb_condPos == ucb_ix) ucb_condPos = -1;
        }

        if ( (cType == UBAT_N) && (ics_symmetric == true) &&
             ( (ucb_condPos > -1)  || odd(ucb_curLev+ucb_impLev) ) &&
             ( UCQSYMM(cChar) != cChar) )
            ucb_wTarget |= 0x8000;

            break;   /* Case End */
        }

        ucb_pType = cType;                                     //@bd11a
        ics_TrgToSrcMap[ucb_ix]=ucb_wTarget; /* put ucb_wTarget in target area at
                                                           position ucb_ix  */
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
        int[] lolim=new int[20];
        int[] hilim = new int[20];
/* For each position of the source area, loLim[i] and hiLim[i] will be set  */
/* to  the  low  and  high  boundary  positions  of the encompassing run of */
/* characters with a level greater than or equal to i, for i varying from   */
/* the level of the computed position down to outLev + 1                    */

/* In accordance with clarifications on Unicode 3, there is no special      */
/* treatment of spacing characters here.                                    */
        int i, x, xLev, lastLev=0;
        int iy, wLev;
        char cChar;

        for (x=0; x < 19; x++)
        {
            lolim[x] = 0;
            hilim[x] = ics_size-1;
        }

        for(ucb_ix=0; ucb_ix < ics_size; ucb_ix++)
        {
        i = ics_TrgToSrcMap[ucb_ix];
        /* retain only the level */
        xLev = i & 0x1FFF;
        /* Mati: check symmetric characters that were marked incorrectly for
                 swapping while in conditional state                       */
        if ( ((i & 0x8000) > 0) && even(xLev) ) {
            cChar = ics_buffer_in[ ucb_ix ];
            if (UCQSYMM(cChar) != cChar)
                ics_TrgToSrcMap[ucb_ix] &= 0x7FFF;
        }

        x = ucb_ix;
        /* check the limits : if the last character had a level (lastLev)
           greater than or equal to xLev, its segment is contained in the
           segment of ix, and all boundaries are already known; if xLev
           is greater than lastLev, this is the start of a new segment,
           lolim and hilim must be updated  */
        if (xLev > lastLev)
        {
            wLev = xLev;
            iy = ucb_ix+1;
            while(wLev > lastLev)
            {
                lolim[wLev] = ucb_ix;
                while(iy < ics_size)
                {
                    if ((ics_TrgToSrcMap[iy] & 0x3FFF) < wLev)
                        break;
                    iy++;
                }
                hilim[wLev] = iy-1;
                wLev--;
            }
        }

        /* Reverse according to levels */
        /* but only if notImpToImp     */
        if (notImpToImp)
            for (i= xLev; i >= (ucb_outLev+1); i--)
                x = lolim[i]+hilim[i] -x;

        /* Update target area */
        ics_TrgToSrcMap[ucb_ix] = (ics_TrgToSrcMap[ucb_ix] & 0xa000) + x;
        lastLev = xLev;
        }
    }

/*------------------------------------------------------------------------*/

    private void  pass3()
    {
        int logPos, logPtr, visPos = 0, wPtr;

        for(ucb_ix = 0; ucb_ix < ics_size; ucb_ix++)
        {
            logPos = ucb_ix;
            logPtr = ics_TrgToSrcMap[ucb_ix];
            while( (logPtr & 0x4000) == 0)
            {
                visPos = logPtr & 0x1FFF;
                wPtr = ics_TrgToSrcMap[visPos];
                if( (wPtr & 0x4000) > 0) break;
                ics_TrgToSrcMap[visPos] = logPos+(logPtr & 0xa000) + 0x4000;
                logPos = visPos;
                logPtr = wPtr;
            }
        }
    }

/*------------------------------------------------------------------------*/

private void  pass4()
{
   int i;
//   short xTran;
   int xTran;

   char  xchar;

   for (i=0; i < ics_size; i++)
    {
      xTran = ics_buffer_out[i] & 0x8000;
      xchar = ics_buffer_in[ics_buffer_out[i] & 0x1FFF];
      if (xTran > 0)
          {
             if( (xchar == 0x200E) || (xchar == 0x200F) ||
                 ((xchar >= 0x202A) && (xchar <= 0x202E)) ||
                 ((xchar >= 0x206C) && (xchar <= 0x206F)) )
               xchar = (char)ics_boc_flag;

             else if( (xchar >= 0xFE80) && (xchar <= 0xFEFC) )
               /* xchar = comp2nom(xchar - 0xFE80);*/
               /* There is an array called ComtoNom in uc_tables.h which contatin the
                  corresponding equivalent nominal character */
                ;
             else if( (xchar >= 0xFB50) && (xchar <= 0xFDFF) )
                ;
             else if( (xchar >= 0x0030) && (xchar <= 0x0039)&& (ics_buffer_out[i] & 0x8000) != 0)
                xchar = (char) (xchar + (char)(0x0660 - 0x0030));
             else if( (xchar >= 0x0030) && (xchar <= 0x0039) )
                xchar = xchar ;
             else
               {
//@BD2D                 if((ics_A_level[ics_buffer_out.charAt(i) & 0x1FFF]!=1))

//@bd11c                 if((ics_A_level[ics_buffer_out[i] & 0x1FFF]!=1))         //@BD2A

                 if(ics_symmetric)         //@bd11A
                      xchar = UCQSYMM(xchar);
// Mati: all the following code is not Unicode conformant
//               if((ics_orient_in != ics_orient_out)){
//                 int i1,j;
//                 int cType;
//                 i1=ics_buffer_out[i]& 0x1FFF;
//                 j=i;
//                 do{
//                  cType=getChType(ics_buffer_in[i1]);
//
//                  if(cType==UBAT_R)
//                    break;
//@bd11A
//                  else if(cType==UBAT_L || cType==UBAT_EN && even(ucb_basLev) {
//                    xchar = UCQSYMM(xchar);
//                    break;
//                  } else {
//                    j++;
//                    if (j < ics_size)
//                       i1=ics_buffer_out[j]& 0x1FFF;
//                  }
//                 } while(j<=ics_size);
//               }
               }
          }
       ics_buffer_out[i] = xchar;
    }
}

/*------------------------------------------------------------------------*/

//@BD4C  public String order ( HODbidiAttribute inAttr,HODbidiAttribute outAttr,String inStr)
  synchronized void order(BidiText src, BidiText dst)
  {
   int  i=0, j=0, x=0, RC=0;

   if (src.count < 1)                                           //@bd9a
        return;                                                 //@bd9a

   /*******************/                                        //@BD3M
   /* Initializations */
   /*******************/

   ics_buffer_in = new char[src.count]; //@BD2A

  System.arraycopy(src.data, src.offset, ics_buffer_in, 0, src.count);


   /*******************/
   /* Initializations */
   /*******************/

    ics_orient_in  = src.flags.getOrientation();
    ics_orient_out = dst.flags.getOrientation();
    ics_type_in  = src.flags.getType();
    ics_type_out = dst.flags.getType();
    notImpToImp = (ics_type_in  != BidiFlag.TYPE_IMPLICIT) |
                  (ics_type_out != BidiFlag.TYPE_IMPLICIT);
    ics_size_out=0;
//@BD4C    ics_size = inStr.length();
    ics_size = src.count;
    ics_num_flag = dst.flags.getNumerals();

    ics_txtShp_flag = dst.flags.getText();

    ics_compc = true;
    ics_formShp = 0;
    ics_boc_flag = 0;
    ics_symmetric = (dst.flags.getSwap() != src.flags.getSwap());

    ics_buffer_out = new char [ics_buffer_in.length]; //@BD2A

    ics_A_level = new byte[src.count];
    ics_SrcToTrgMap = new int[src.count];
    ics_TrgToSrcMap = new int[src.count];

    ucb_ix=0;

//@bd11a
    ucb_pType = UBAT_B;
    ucb_Compac=0;
    ucb_Shaping=0;
    ucb_araLet = 0;

//    Slider=&Start;

 /*----------------------------------------*/
 /* Determination of the base level basLev */
 /*----------------------------------------*/

     BaseLvl();

     while(ucb_ix < ics_size )
         {
           pass1();
           ucb_ix++ ;
         }

     /* do Implicit process for UBAT_B to resolve possible conditional string */
     ucb_xType = UBAT_B;

     implicitProcessing();

     //if(ics_A_level)
     // {
        for(i=0; i < ics_size; i++)
        {
          ics_A_level[i] = (byte)(ics_TrgToSrcMap[i] & 0x00FF);
        }

      //}


    /* Pass 2 :This pass must not be executed when there is no request
               for reordering */

     pass2();


    /* Pass 3 :The logical to visual mapping must be converted to a visual-
               to-logical mapping. We use bit 14 in each position of the
               target area to mark positions which have been processed.  */
    pass3();
    for(i=0; i < ics_size; i++)
       ics_buffer_out[i] =(char)ics_TrgToSrcMap[i] ;


    /* Pass 4 :Replaces the content of the target area set by pass3 with
               the source charcters from the indicated position. European
               digits which need Arabic-Indic shapes are translated; Arabic
               presentation forms which need shaping are replaced by the
               corresponding nominal letters; symbols which need swapping
               are replaced by thier symmetric symbol; bidi special codes
               which need replacement are replaced. */
     pass4();


  for (i=0; i< ics_size;i++)
    ics_TrgToSrcMap[i] = ics_TrgToSrcMap[i] & 0x00FF;
  if(ics_compc == true){
    for (i=0; i< ics_size;i++)
      ics_SrcToTrgMap[ics_TrgToSrcMap[ics_TrgToSrcMap[i]]] = ics_TrgToSrcMap[i] ;
  }

  System.arraycopy(ics_buffer_out, 0, dst.data, dst.offset, src.count);
  dst.count = src.count;
}

}
