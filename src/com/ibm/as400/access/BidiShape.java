///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: BidiShape.java
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
 *  since its only public method is synchronized, and there are no instance
 *  variables.  However, to avoid delays, each thread should use its own
 *  instances of this class.
 **/

class BidiShape {
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


  /* replaces Arabic base letters by the appropriate
   * presentation forms.
   */

  /* Link attribute are unsigned 16 bit integer with the following format:   */
  /* bit 0:     links to the right                                           */
  /* bit 1:     links to the left                                            */
  /* bit 2:     is irrelevant to linking                                     */
  /* bit 4:     Lam type UNI Code                                            */
  /* bit 5:     Alef type UNI Code                                           */
  /* bit 8-15:  offset to presentation shapes starting at U+FE70             */

  //private static final short shapeTable[4][4][4]=
  private static final short shapeTable[][][]=
                      {
  /*                    lastLink = 0 | lastLink = 1 | lastLink = 2 | lastLink = 3 */
  /* nextLink = 0 */    { {0,0,0,0},   {0,0,0,0},     {0,1,0,3},     {0,1,0,1} },
  /* nextLink = 1 */    { {0,0,2,2},   {0,0,1,2},     {0,1,1,2},     {0,1,1,3} },
  /* nextLink = 2 */    { {0,0,0,0},   {0,0,0,0},     {0,1,0,3},     {0,1,0,3} },
  /* nextLink = 3 */    { {0,0,1,2},   {0,0,1,2},     {0,1,1,2},     {0,1,1,3} }  };

  private static final int convertFEto06[] =
                               {
                                0x64B, 0x64B,
                                0x64C, 0x64C,
                                0x64D, 0x64D,
                                0x64E, 0x64E,
                                0x64F, 0x64F,
                                0x650, 0x650,
                                0x651, 0x651,
                                0x652, 0x652,
                                0x621,
                                0x622, 0x622,
                                0x623,0x623,
                                0x624,0x624,
                                0x625,0x625,
                                0x626,0x626,0x626,0x626,
                                0x627,0x627,
                                0x628,0x628,0x628,0x628,
                                0x629,0x629,
                                0x62A,0x62A,0x62A,0x62A,
                                0x62B,0x62B,0x62B,0x62B,
                                0x62C,0x62C,0x62C,0x62C,
                                0x62D,0x62D,0x62D,0x62D,
                                0x62E,0x62E,0x62E,0x62E,
                                0x62F,0x62F,
                                0x630,0x630,
                                0x631,0x631,
                                0x632,0x632,
                                0x633,0x633, 0x633,0x633,
                                0x634, 0x634,0x634,0x634,
                                0x635,0x635,0x635,0x635,
                                0x636,0x636,0x636,0x636,
                                0x637,0x637,0x637,0x637,
                                0x638,0x638,0x638,0x638,
                                0x639,0x639,0x639,0x639,
                                0x63A,0x63A,0x63A,0x63A,
                                0x641,0x641,0x641,0x641,
                                0x642,0x642,0x642,0x642,
                                0x643,0x643,0x643,0x643,
                                0x644,0x644,0x644,0x644,
                                0x645,0x645,0x645,0x645,
                                0x646,0x646,0x646,0x646,
                                0x647,0x647,0x647,0x647,
                                0x648, 0x648,
                                0x649,0x649,
                                0x64A,0x64A,0x64A,0x64A,
                                0x65C, 0x65C,
                                0x65D,0x65D,
                                0x65E,0x65E,
                                0x65F,0x65F
                               };

  private static final int convert06toFE[] =
                               {
                                0x10,
                                0x11,
                                0x13,
                                0x15,
                                0x17,
                                0x19,
                                0x1D,
                                0x1F,
                                0x23,
                                0x25,
                                0x29,
                                0x2D,
                                0x31,
                                0x35,
                                0x39,
                                0x3B,
                                0x3D,
                                0x3F,
                                0x41,
                                0x45,
                                0x49,
                                0x4D,
                                0x51,
                                0x55,
                                0x59,
                                0x5D,
                                0x0,
                                0x0,
                                0x0,
                                0x0,
                                0x0,
                                0x0,
                                0x61,
                                0x65,
                                0x69,
                                0x6D,
                                0x71,
                                0x75,
                                0x79,
                                0x7D,
                                0x7F,
                                0x81,
                                0x0,
                                0x2,
                                0x4,
                                0x6,
                                0x8,
                                0xA,
                                0xC,
                                0xE
                               };

  /*****************************************************************************/
  private static final int Link06[]=
                               {
                               1           + 32 + 256 * 0x11,
                               1           + 32 + 256 * 0x13,
                               1                + 256 * 0x15,
                               1           + 32 + 256 * 0x17,
                               1 + 2            + 256 * 0x19,
                               1           + 32 + 256 * 0x1D,
                               1 + 2            + 256 * 0x1F,
                               1                + 256 * 0x23,
                               1 + 2            + 256 * 0x25,
                               1 + 2            + 256 * 0x29,
                               1 + 2            + 256 * 0x2D,
                               1 + 2            + 256 * 0x31,
                               1 + 2            + 256 * 0x35,
                               1                + 256 * 0x39,
                               1                + 256 * 0x3B,
                               1                + 256 * 0x3D,
                               1                + 256 * 0x3F,
                               1 + 2            + 256 * 0x41,
                               1 + 2            + 256 * 0x45,
                               1 + 2            + 256 * 0x49,
                               1 + 2            + 256 * 0x4D,
                               1 + 2            + 256 * 0x51,
                               1 + 2            + 256 * 0x55,
                               1 + 2            + 256 * 0x59,
                               1 + 2            + 256 * 0x5D,
                               0, 0, 0, 0, 0, /* 0x63B - 0x63F */
                               1 + 2,
                               1 + 2            + 256 * 0x61,
                               1 + 2            + 256 * 0x65,
                               1 + 2            + 256 * 0x69,
                               1 + 2       + 16 + 256 * 0x6D,
                               1 + 2            + 256 * 0x71,
                               1 + 2            + 256 * 0x75,
                               1 + 2            + 256 * 0x79,
                               1                + 256 * 0x7D,
                               1                + 256 * 0x7F,
                               1 + 2            + 256 * 0x81,
                               4, 4, 4, 4,
                               4, 4, 4, 4,      /* 0x64B - 0x652 */
                               0, 0, 0, 0, 0,
                               0, 0, 0, 0,      /* 0x653 - 0x65B */
                               1                + 256 * 0x85,
                               1                + 256 * 0x87,
                               1                + 256 * 0x89,
                               1                + 256 * 0x8B,
                               0, 0, 0, 0, 0,
                               0, 0, 0, 0, 0,
                               0, 0, 0, 0, 0, 0, /* 0x660 - 0x66F */
                               4,
                               0,
                               1           + 32,
                               1           + 32,
                               0,
                               1           + 32,
                               1, 1,
                               1+2, 1+2, 1+2, 1+2, 1+2, 1+2,
                               1+2, 1+2, 1+2, 1+2, 1+2, 1+2,
                               1+2, 1+2, 1+2, 1+2,
                               1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                               1, 1, 1, 1, 1, 1, 1, 1,
                               1+2, 1+2, 1+2, 1+2, 1+2, 1+2, 1+2, 1+2, 1+2, 1+2,
                               1+2, 1+2, 1+2, 1+2, 1+2, 1+2, 1+2, 1+2, 1+2, 1+2,
                               1+2, 1+2, 1+2, 1+2, 1+2, 1+2, 1+2, 1+2, 1+2, 1+2,
                               1+2, 1+2, 1+2, 1+2, 1+2, 1+2, 1+2, 1+2,
                               1,
                               1+2,
                               1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                               1+2,
                               1,
                               1+2, 1+2, 1+2, 1+2,
                               1, 1
                             };
  /*****************************************************************************/

  private static final short LinkFE[]=
                             {
//@bd10c                               4,
//                               1 + 2,
//                               4, 4, 4, 4, 4,
//                               1 + 2,
//                               4, 1 + 2, 4, 1 + 2,
//                               4, 1 + 2, 4, 1 + 2,

                               1 + 2,
                               1 + 2,
                               1 + 2, 0, 1+ 2, 0, 1+ 2,
                               1 + 2,
                               1+ 2, 1 + 2, 1+2, 1 + 2,
                               1+ 2, 1 + 2, 1+2, 1 + 2,

                               0, 0 + 32, 1 + 32, 0 + 32,
                               1 + 32, 0, 1,  0 + 32,
                               1 + 32, 0, 2,  1 + 2,
                               1, 0 + 32, 1 + 32, 0,
                               2, 1 + 2, 1, 0,
                               1, 0, 2, 1 + 2,
                               1, 0, 2, 1 + 2,
                               1, 0, 2, 1 + 2,
                               1, 0, 2, 1 + 2,
                               1, 0, 2, 1 + 2,
                               1, 0, 1, 0,
                               1, 0, 1, 0,
                               1, 0, 2, 1+2,
                               1, 0, 2, 1+2,
                               1, 0, 2, 1+2,
                               1, 0, 2, 1+2,
                               1, 0, 2, 1+2,
                               1, 0, 2, 1+2,
                               1, 0, 2, 1+2,
                               1, 0, 2, 1+2,
                               1, 0, 2, 1+2,
                               1, 0, 2, 1+2,
                               1, 0, 2, 1+2,
                               1, 0 + 16, 2 + 16, 1 + 2 +16,
                               1 + 16, 0, 2, 1+2,
                               1, 0, 2, 1+2,
                               1, 0, 2, 1+2,
                               1, 0, 1, 0,
                               1, 0, 2, 1+2,
                               1, 0, 1, 0,
                               1, 0, 1, 0,
                               1
                            };

  /*****************************************************************************/
//@bd10d Start
/*
//@bd9a Start
  private static final short IrreleventTable[]=
                             {
                             2,
                             0,
                             0,
                             2,
                             2,
                             2,
                             2,
                             2};

*/

  private static final short IrreleventPos[]=
                             {
                             0x0,
                             0x2,
                             0x4,
                             0x6,
                             0x8,
                             0xA,
                             0xC,
                             0xE,
                             }  ;
//@bd9a End
  private static final int LINKR = 1;
  private static final int LINKL = 2;
  private static final int IRRELEVANT = 4;
  private static final int LAMTYPE = 16;
  private static final int ALEFTYPE = 32;
  private static final int LINKFIELD = 3;
// Mati: remove since not used.      private int lamAlphCount = 0;

  /*****************************************************************************/
//@BD3C public String shape ( HODbidiAttribute inAttr,HODbidiAttribute outAttr,String inStr) {
  synchronized void shape(BidiFlagSet inAttr, BidiFlagSet outAttr, char str06[]) {

    int currLink;      /* link attributes of current character
                                                                at position Ix */
    int lastLink=0;    /* link attributes of the last character
                                         which was not irrelevant to linking   */
    int nextLink=0;    /* link attributes of the next character
                                          which is not irrelevant to linking   */
    int prevLink=0;    /* link attributes of the character
                          before the last which is not irrelevant to linking   */
    int lastPos; /* position of the last character  which was not irrelevant   */
    int Nx;      /* position of the next character  which is not irrelevant    */
    int prevPos; /* position of the character before the last which was not irr*/

    int iEnd, Nw, step,  Shape, i, Ix;

    char wLamalef;
    char[] wBuf = new char[20];

//    int size;
    int j,x;
    int flag;
    int trgIdx = 0;

    boolean RTL = true;
    boolean inOutDiff = false ;
    int size;

    if (str06.length == 0)                                              //@bd6a
        return;                                                         //@bd6a

    RTL = ( outAttr.getOrientation() == BidiFlag.ORIENTATION_RTL );

    inOutDiff = ( outAttr.getOrientation() != inAttr.getOrientation() );

      //StringBuffer result = new StringBuffer(inStr);

//@BD2D      StringBuffer str06 = new StringBuffer(inStr);
//@BD3D      char[] str06 = new char[inStr.length()];                        //@BD2A

//@BD3C      for (int idx = 0; idx < inStr.length(); idx++)
      for (int idx = 0; idx < str06.length ; idx++)
      {
//@BD3C        char inputChar = inStr.charAt(idx);
        char inputChar = str06[idx];

//@bd9c//@bd7c                      if ( ( inputChar >= 0xFE70) && (inputChar <= 0xFEFC ))  //FE range
//@bd9c allow the taskil characters to be converted to 6 range sice now they are shaped.
               if ( ( inputChar >= 0xFE70) && (inputChar <= 0xFEFC ))  //FE range
//@bd9c               if ( ( inputChar >= 0xFE80) && (inputChar <= 0xFEFC ))  //FE range
        {
           {
//@BD2D             str06.setCharAt (trgIdx, (char)(convertFEto06 [ (inputChar - 0xFE70) ] ) );
             str06[trgIdx] = (char)(convertFEto06 [ (inputChar - 0xFE70) ] ) ;  //@BD2A
           }
        }else
        {
//@BD2D           str06.setCharAt (trgIdx, inputChar );
           str06[trgIdx] = inputChar ;
        }

        trgIdx++;
      }

      /* This pass is done so that Arabic characters  are processed in language  */
      /* order. If outAttr is RTL    , this means processing from the begining of*/
      /* the str06 (source) until its end;  if outAttr is LTR    ,  this  means  */
      /* processing from the end of the str06 (source) toward its beginning      */

//@BD2D      StringBuffer result = new StringBuffer(str06.toString());

      if (RTL && inOutDiff)
//      if (RTL)
      {
         Ix = 0;           /* Setting the low boundary of the processing         */
//@BD2D    iEnd = str06.length() ;    /* Setting the high boundary of the processing        */
         iEnd = str06.length ;    /* Setting the high boundary of the processing        *///@BD2A
         step = +1;
         }
      else {
//@BD2D         Ix = str06.length() - 1;  /* Setting the low boundary of the processing         */
         Ix = str06.length - 1;  /* Setting the low boundary of the processing         *///@BD2A
         iEnd = -1;       /* Setting the high boundary of the processing        */
         step = -1;
         }

//@BD2D      size = str06.length();
      size = str06.length;

      prevLink = 0;
      lastLink = 0;

//@BD2D      currLink = uba_getLink ( str06.charAt(Ix) );
      currLink = uba_getLink ( str06[Ix] ); //@BD2A

      prevPos = Ix;
      lastPos = Ix;
      Nx = -2;
      while ( Ix != iEnd ) {

         if ((currLink & 0xFF00) > 0 ) {        /* If there are more than one shape   */
            Nw = Ix + step;

            while ( Nx < 0  ) {            /* we need to know about next char */
               if (Nw == iEnd) {
                  nextLink = 0;
                  Nx = 30000;             /* will stay so until end of pass*/
               }
               else {
//@BD2D                  nextLink = uba_getLink(str06.charAt(Nw));
                  nextLink = uba_getLink(str06[Nw]); //@BD2D

                  if ((nextLink & IRRELEVANT) == 0)
                     Nx = Nw;
                  else Nw += step;
               }
            }

            if ( ((currLink & ALEFTYPE) > 0)  &&  ((lastLink & LAMTYPE) > 0) )
            {
//@BD2D               wLamalef = Lamalef( str06.charAt(Ix) ); //get from 0x065C-0x065f
               wLamalef = Lamalef( str06[Ix] ); //get from 0x065C-0x065f //@BD2A

      //Gilan for now         Compac++;
               if ( wLamalef != 0)
               {
                  if (RTL && inOutDiff)
//                  if (RTL )
                  {

///* To fix the space problem */
//                        int dummy=Ix;
//                        //IcsRec->buffer_out[lastPos] = wLamalef;
//                        str06.setCharAt(lastPos,wLamalef );
//
//                        //while(dummy < IcsRec->size-1 ){  /* drop the Alef */
//                        while(dummy < str06.length()-1 ){  /* drop the Alef */
//
////                          IcsRec->buffer_out[dummy] = IcsRec->buffer_out[dummy+step];
//                          str06.setCharAt(dummy ,str06.charAt(dummy+step) );
//
////                        if(IcsRec->compc == TRUE)
////                          IcsRec->TrgToSrcMap[dummy] = IcsRec->TrgToSrcMap[dummy+step];
//
//                          dummy +=step;
//                        }
//                        size--;
//                        Ix--;
//                        //IcsRec->buffer_out[size]=0;
//
//                        str06.setCharAt(size,(char)0 );
//                        str06.setLength(size);
//
//
//                        //IcsRec->buffer_out[i] = wLamalef;
//                        str06.setCharAt(Ix,wLamalef );
//                        lamAlphCount++;
//                  }
//                  else                  /* LTR device */
//                  {
///* To fix the space in front of the LamAlef */
//                        int dummy=Ix;
//                        while(dummy != size -1){
//                          //IcsRec->buffer_out[dummy] = IcsRec->buffer_out[dummy-step]; /* drop the Lam */
//                          str06.setCharAt(dummy ,str06.charAt(dummy-step) );
//
////                        if(IcsRec->compc == TRUE)
////                          IcsRec->TrgToSrcMap[dummy] = IcsRec->TrgToSrcMap[dummy-step];
//
//                          dummy -= step;
//                        }
//                        size--;
//                        //IcsRec->buffer_out[size]=0;
//                        str06.setCharAt(size, (char)0 );
//                        str06.setLength(size);
//                        //IcsRec->buffer_out[i] = wLamalef;
//                        str06.setCharAt(Ix,wLamalef );
//                        lamAlphCount++;
//                        /* move irrelevant characters */
//                  }

//From Unicode Toolkit

                   // Put LamAlef in one cell followed by 0xFFFF
//Gilan                     str06.setCharAt(lastPos,wLamalef );
//Gilan                     str06.setCharAt(Ix, (char) 0x0020 ); /*     drop the Alef            */
//@BD2D                     str06.setCharAt(Ix,wLamalef );
//@BD2D                     str06.setCharAt(lastPos, (char) 0x0020 ); /*     drop the Alef            */

//@BD4C                     str06[Ix] = wLamalef ;            //@BD2A
//@BD4C                     str06[lastPos]= (char) 0x0020 ; /*     drop the Alef            *///@BD2A

                     str06[lastPos] = wLamalef ;            //@BD2A
                     str06[Ix]= (char) 0x0020 ; /*     drop the Alef            *///@BD2A

                     Ix=lastPos;
                  }
                  else {         /*   LTR  device      drop the Lam               */

//Gilan            str06.setCharAt(Ix ,wLamalef );
//Gilan            str06.setCharAt(lastPos, (char)0x0020); /*     drop the Alef            */

//@BD2D                      str06.setCharAt(Ix, (char)0x0020); /*     drop the Alef            */
//@BD2D                      str06.setCharAt(lastPos ,wLamalef );
                     str06[Ix] = (char)0x0020; /*     drop the Alef  */ //@BD2A
                     str06[lastPos] =wLamalef ;                         //@BD2A

                     Ix=lastPos;

                  } //LTR
               }//(wLamalef != 0)
            lastLink = prevLink;
            currLink = uba_getLink(wLamalef);


      //gilan now            Compac++;
            }

                /* get the proper shape according to link ability of
                   neighbors and of character; depends on the order of
                   the shapes (isolated, initial, middle, final) in the
                   compatibility area */

//@BD2D            flag=specialChar (str06.charAt(Ix));
            flag=specialChar (str06[Ix]);              //@BD2A


            if (outAttr.getText() == BidiFlag.TEXT_INITIAL)
            {
             if(flag==0)
               Shape = 2;
             else
               Shape = 0;
            }
            else if (outAttr.getText() == BidiFlag.TEXT_MIDDLE)
            {
             if(flag == 0)
               Shape = 3;
             else
               Shape = 1;
            }
            else if (outAttr.getText() == BidiFlag.TEXT_FINAL)
            {
             if(flag == 0)
               Shape = 1;
             else
               Shape = 1;
            }
            else if (outAttr.getText() == BidiFlag.TEXT_ISOLATED)
            {
               Shape = 0;
            }
            else
            {
               Shape = shapeTable[nextLink & (LINKR + LINKL)]
                                 [lastLink & (LINKR + LINKL)]
                                 [currLink & (LINKR + LINKL)];
            }

      //      target[Ix] = 0xFE70 + (currLink >> 8 ) + Shape ;

//@BD2D            str06.setCharAt(Ix , (char)(0xFE70 + ( currLink >> 8 ) + Shape) );

            str06[Ix] =  (char)(0xFE70 + ( currLink >> 8 ) + Shape) ;  //@BD2A

         }
         /* move one notch forward    */
         if ((currLink & IRRELEVANT) == 0) {

            prevLink = lastLink;
            lastLink = currLink;
            prevPos = lastPos;
            lastPos = Ix;
         }
//@bd9a Start
          //Tashkil characters
         if ((currLink & IRRELEVANT) > 0) {

             //@BD10D          int tryprnt = str06[Ix] - 0x0600;

             int charidx = str06[Ix] - 0x064B;

//@BD10D             int  MyShape = shapeTable[nextLink & (LINKR + LINKL)]
//                                 [lastLink & (LINKR + LINKL)]
//                                 [IrreleventTable[charidx] & (LINKR + LINKL)];

            int  MyShape =0;
//@bd10a start
             int next = (int) (nextLink & (LINKR + LINKL));
             int last =lastLink & (LINKR + LINKL);

             if  ( ( (last==3)&& (next==1) )
                || ( (last==3) && (next==3) ) )
                MyShape= 1;

//LamAlef type
             if ( ((nextLink & ALEFTYPE) > 0)  &&  ((lastLink & LAMTYPE) > 0) )
                MyShape=0;

             if ( (str06[Ix]==0x064C) //Wawdoma
                 || (str06[Ix]==0x064D) ) //kasrten
                MyShape=0;
//@bd10a end

            str06[Ix] =  (char)(0xFE70 + IrreleventPos[charidx]+ MyShape) ;  //@BD2A
         }

//@bd9a end
         Ix += step;

         if ( Ix == Nx ) {
            currLink = nextLink;
            Nx = -2;
         }
         else
      //      currLink = uba_getLink(target[Ix]);
         {
            if (Ix != iEnd)
//@BD2D               currLink = uba_getLink ( str06.charAt(Ix) );
               currLink = uba_getLink ( str06[Ix]) ;  //@BD2A
         }
      } //end while
//@BD3D        return (new String(str06));

//@bd10 //Seen family , if 2 space after seen change to seen + tail(0x200C)
//+Space
      for (int idx = 0; idx < str06.length ; idx++)
      {
          if (RTL && inOutDiff)
          {
             if ( (SeenChar(str06[idx]))
                  //next character is Space
               && ( (idx+1 < str06.length) && ( str06[idx+1] == 0x0020) ) )

               str06[idx+1] =0x200C;  //ZWNJ so as tobe converted to tail for
                                     //420
          }else
          {
             if ( (SeenChar(str06[idx]))
                  //next character is Space
               && ( (idx-1 >= 0) && ( str06[idx-1] == 0x0020) ) )

               str06[idx-1] =0x200C;  //ZWNJ so as tobe converted to tail for 420
          }
      }

  } //end method

/*------------------------------------------------------------------------*/

  private static int uba_getLink(char x)
  {
     if(x >= 0x0622 && x <= 0x06D3)  //06 Range
         return(Link06[x-0x0622]);
     else if(x == 0x200D)            //(ZWJ)
         return(3);
     else if(x >= 0x206D && x <= 0x206F)   //Alternate Formating
         return(4);
     else if(x >= 0xFE70 && x <= 0xFEFC)  //FE range
         return(LinkFE[x-0xFE70]);
     else
         return(0);
  }

/*------------------------------------------------------------------------*/

  private static char Lamalef(char x)
  {
    if(x == 0x0622)
      return(0x065C);
    else if(x == 0x0623)
      return(0x065D);
    else if(x == 0x0625)
      return(0x065E);
    else if(x == 0x0627)
      return(0x065F);
    else
      return(0);
  }

/*------------------------------------------------------------------------*/

  private static int specialChar(char ch)
  {
     // hamza ,
     if((ch >= 0x0621 && ch < 0x0626)|| (ch == 0x0627 )||
        (ch > 0x062e && ch < 0x0633) ||
        (ch > 0x0647 && ch < 0x064a) || ch == 0x0629)
          return(1);
     else
          return(0);
  }

/*------------------------------------------------------------------------*/

//@BD5A
/**************************************************************************/
// Adding a method that take an array of UNICODE characters
// that are shaped and ordered , and return an expanded array of
// UNICODE for each Lamalef character.
/**************************************************************************/

  static char[] ExpandLamAlef(char[] UniBuff)              //@BD8
  {                                                        //@BD8
    return ExpandLamAlef(UniBuff, UniBuff.length);         //@BD8
  }                                                        //@BD8

/*------------------------------------------------------------------------*/
  //@BD8 public char[] ExpandLamAlef(char[] UniBuff)
  static char[] ExpandLamAlef(char[] UniBuff, int length)  //@BD8
  {
    char [] ExpandedArray;
    final short AlefType[] = {
               (short)0xFE81,
               (short)0xFE82,
               (short)0xFE83,
               (short)0xFE84,
               (short)0xFE87,
               (short)0xFE88,
               (short)0xFE8D,
               (short)0xFE8E,
                };

     int LamAlefNo = 0;

     //@BD8 for (int idx=0; idx < UniBuff.length ;idx++)
     for (int idx=0; idx < length ;idx++)        //@BD8
     {
         char inputChar = UniBuff[idx];
         if ( ( inputChar >= 0xFEF5) && (inputChar <= 0xFEFC ))  //Lamalef
         {
              LamAlefNo++;
                // char ChAlefType = (char)(AlefType[inputChar-0xFEF5]);
         }
     }
     if (LamAlefNo > 0)
     {
        //@BD8 ExpandedArray = new char[ UniBuff.length + LamAlefNo];
        ExpandedArray = new char[ length + LamAlefNo];              //@BD8
        char ChAlefType;
        int trgidx = 0;

        //@BD8 for (int idx=0; idx < UniBuff.length;idx++)
        for (int idx=0; idx < length;idx++)                         //@BD8
        {
             char inputChar = UniBuff[idx] ;

             if ( ( inputChar >= 0xFEF5) && (inputChar <= 0xFEFC ))  //Lamalef Type
             {
                   ExpandedArray[trgidx] = (char)(0xFEDD);
                   trgidx++;

                   ChAlefType = (char)(AlefType[inputChar-0xFEF5]);
                   ExpandedArray[trgidx] = ChAlefType;

             }else

                   ExpandedArray[trgidx] = UniBuff[idx];

             trgidx++;
        }

        return ExpandedArray;

     }else

        return UniBuff;
  }

/*------------------------------------------------------------------------*/

//@bd10 add seen type check

  private static boolean SeenChar(char ch)
  {
     if ( (ch==0xFEB1) ||
          (ch==0xFEB2) ||
          (ch==0xFEB5) ||
          (ch==0xFEB6) ||
          (ch==0xFEB9) ||
          (ch==0xFEBA) ||
          (ch==0xFEBD) ||
          (ch==0xFEBE)
     )
          return(true);
     else
          return(false);
  }

  /*************************************************************************/
  //@BD5A End method

}       //end class
