///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: BidiShape.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
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

class BidiShape
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";


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

  static final char Tail = 0x200B ;
  private static final short shapeTable[][][]=
  {
    /*                    lastLink = 0 | lastLink = 1 | lastLink = 2 | lastLink = 3 */
    /* nextLink = 0 */    { {0,0,0,0},   {0,0,0,0},     {0,1,0,3},     {0,1,0,1}},
    /* nextLink = 1 */    { {0,0,2,2},   {0,0,1,2},     {0,1,1,2},     {0,1,1,3}},
    /* nextLink = 2 */    { {0,0,0,0},   {0,0,0,0},     {0,1,0,3},     {0,1,0,3}},
    /* nextLink = 3 */    { {0,0,1,2},   {0,0,1,2},     {0,1,1,2},     {0,1,1,3}}};

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

//Used by Lam-Alef methods
   static char AlefType[] = {
                   '\u0622',
                   '\u0622',
                   '\u0623',
                   '\u0623',
                   '\u0625',
                   '\u0625',
                   '\u0627',
                   '\u0627',
                    };
//Used by Tashkeel handeling methods
   static char Tashkeel[] = {
                   '\ufe70', //Tanween Fath
                   '\ufe72', //Tanween Dam
                   '\ufe74', //Tanween Kasr
                   '\ufe76', //Fatha
                   '\ufe78', //Dama
                   '\ufe7A', //Kassra
                   '\ufe7c', //Shadda
                   '\ufe7e', //Sekooun
                    };

//Used by Tashkeel handeling methods
   static char Tashkeel_Tatweel[] = {
                   '\ufe71', //Tanween Fath with tatweel
                   '\ufe72', //Tanween Dam -- No Ttween Dam with Tashkeel in the FE Range
                   '\ufe74', //Tanween Kasr -- No Ttween Dam with Tashkeel in the FE Range
                   '\ufe77', //Fatha with tatweel
                   '\ufe79', //Dama with tatweel
                   '\ufe7b', //Kassra with tatweel
                   '\ufe7d', //Shadda with tatweel
                   '\ufe7f', //Sekooun with tatweel
                    };


  private static final int LINKR = 1;
  private static final int LINKL = 2;
  private static final int IRRELEVANT = 4;
  private static final int LAMTYPE = 16;
  private static final int ALEFTYPE = 32;
  private static final int LINKFIELD = 3;


/**
 * Method shapingRoutine
 * This method represents the shaping routine.
 * @param inAttr    The input Bidi Attributes (TextType, TextShape, ...etc.)
 * @param outAttr   The output Bidi Attributes (TextType, TextShape, ...etc.)
 * @param str       The buffer to be shaped.
 * @param rtl       The buffer orientation.
 */
  synchronized void shapingRoutine(BidiFlagSet inAttr, BidiFlagSet outAttr, char str[], boolean rtl)
    {
    int currLink;      /* link attributes of current character at position Ix */
    int lastLink=0;    /* link attributes of the last character which was not irrelevant to linking   */
    int nextLink=0;    /* link attributes of the next character which is not irrelevant to linking   */
    int prevLink=0;    /* link attributes of the character before the last which is not irrelevant to linking   */
    int lastPos; /* position of the last character  which was not irrelevant   */
    int Nx;      /* position of the next character  which is not irrelevant    */
    int prevPos; /* position of the character before the last which was not irr*/

    int iEnd, Nw, step, Shape, Ix;

    char wLamalef;
    int flag;

    BidiFlag   inTextType, outTextType, inTextShape, outTextShape;

    int bufLen = str.length;
    if ( bufLen == 0)
      return;

    //Initialize variables
    inTextType   = inAttr.getType();
    outTextType  = outAttr.getType();
    inTextShape  = inAttr.getText();
    outTextShape = outAttr.getText();

    if(inTextType == BidiFlag.TYPE_VISUAL && outTextType == BidiFlag.TYPE_IMPLICIT) //Visual to Implicit
    { //We must test if this check is the best check for shapping Shaped -> Nominal
        for (int idx = 0; idx < bufLen ; idx++)
        {
        //Convert the characters from FE to 06 range
            if ( (str[idx] >= 0xFE70) && (str[idx] <= 0xFEF4 ) )
                {
            //This change fix NULL pointer exception with IBMJDK 1.4
            //IBMJDK1.4 cann't resolve casting correctly
                    int newCharValue = str[idx] - 0xFE70;
                    str[idx] = (char)(convertFEto06 [ newCharValue ] ) ;
                }
        }
     }//end if Visual to Implicit
    else if(outTextType == BidiFlag.TYPE_VISUAL)
    { //We must test if this check is the best check for shapping  -> Nominal
      /* This pass is done so that Arabic characters  are processed in language        */
      /* order. If outAttr is RTL    , this means start processing from the begining of*/
      /* the str (source) till its end;  if outAttr is LTR    ,  this  means start   */
      /* processing from the end of the str (source) till its beginning              */
      if (rtl)
      {
        Ix = 0;           /* Setting the low boundary of the processing         */
         iEnd = str.length ;    /* Setting the high boundary of the processing        */
        step = +1;
      }
      else {
         Ix = str.length - 1;  /* Setting the low boundary of the processing         */
        iEnd = -1;       /* Setting the high boundary of the processing        */
        step = -1;
      }


      prevLink = 0;
      lastLink = 0;
      currLink = uba_getLink ( str[Ix] );
      prevPos = Ix;
      lastPos = Ix;
      Nx = -2;
      while (Ix != iEnd)
      {
        if ((currLink & 0xFF00) > 0 )         /* If there are more than one shape   */
        {
          Nw = Ix + step;

            while ( Nx < 0  ) {            /* we need to know about next char */
               if (Nw == iEnd) {
              nextLink = 0;
              Nx = 30000;             /* will stay so until end of pass*/
            }
               else {
                  nextLink = uba_getLink(str[Nw]);
              if ((nextLink & IRRELEVANT) == 0)
                Nx = Nw;
              else Nw += step;
            }
          }
          if (((currLink & ALEFTYPE) > 0)  &&  ((lastLink & LAMTYPE) > 0))
            {
             wLamalef = Lamalef( str[Ix] ); //get from 0x065C-0x065f
            if (wLamalef != 0)
            {
                  if (rtl)
                  {
                     str[lastPos] = wLamalef ;
                            for(int h=Ix;h<str.length-1; h++)
                              {str[h] = str[h+1];}
                     str[str.length-1]= (char) 0x0020;
                Ix=lastPos;
              }
                  else {         /*   LTR  device      drop the Lam               */
                     str[lastPos] =wLamalef ;
                            for(int h=Ix;h>0; h--)
                               {str[h] =str[h-1];}
                     str[0] =(char) 0x0020;
                Ix=lastPos;

              } //LTR
            }//(wLamalef != 0)

            lastLink = prevLink;
            currLink = uba_getLink(wLamalef);
            Nx = -2;              //force recompute of nextLink
            }

          /* get the proper shape according to link ability of
             neighbors and of character; depends on the order of
             the shapes (isolated, initial, middle, final) in the
             compatibility area */

            flag=specialChar (str[Ix]);
            if (outTextShape == BidiFlag.TEXT_INITIAL)
            {
            if (flag==0)
              Shape = 2;
            else
              Shape = 0;
          }
            else if (outTextShape == BidiFlag.TEXT_MIDDLE)
            {
            if (flag == 0)
              Shape = 3;
            else
              Shape = 1;
          }
            else if (outTextShape == BidiFlag.TEXT_FINAL)
            {
            if (flag == 0)
              Shape = 1;
            else
              Shape = 1;
          }
            else if (outTextShape == BidiFlag.TEXT_ISOLATED)
            {
            Shape = 0;
          }
          else
          {
            Shape = shapeTable[nextLink & (LINKR + LINKL)]
                    [lastLink & (LINKR + LINKL)]
                    [currLink & (LINKR + LINKL)];
          }
            str[Ix] =  (char)(0xFE70 + ( currLink >> 8 ) + Shape) ;

         }
        /* move one notch forward    */
         if ((currLink & IRRELEVANT) == 0) {

          prevLink = lastLink;
          lastLink = currLink;
          prevPos = lastPos;
          lastPos = Ix;
        }
        //Tashkil characters
         if ( ((currLink & IRRELEVANT) > 0) && (0 <=  (str[Ix] - 0x064B)) && ((str[Ix] - 0x064B) < IrreleventPos.length ) ) {
            int charidx = str[Ix] - 0x064B;
          int  MyShape =0;
          int next = (int) (nextLink & (LINKR + LINKL));
          int last =lastLink & (LINKR + LINKL);
          if (( (last==3)&& (next==1) )
              || ( (last==3) && (next==3) ))
            MyShape= 1;
          if (((nextLink & ALEFTYPE) > 0)  &&  ((lastLink & LAMTYPE) > 0))
            MyShape=0;
            if ( (str[Ix]==0x064C) //Wawdoma
                 || (str[Ix]==0x064D) ) //kasrten
            MyShape=0;
            str[Ix] =  (char)(0xFE70 + IrreleventPos[charidx]+ MyShape) ;
         }
        Ix += step;
         if ( Ix == Nx ) {
          currLink = nextLink;
          Nx = -2;
        }
        else
         {
          if (Ix != iEnd)
               currLink = uba_getLink ( str[Ix]) ;
         }
      } //end while
    }// end of else to visual
  }
/**
 * Method shape.
 * In this method, the shaping process (Arabic options handling and shaping routine) is performed.
 *
 * @param inAttr     The input Bidi Attributes (TextType, TextShape, ...etc.)
 * @param outAttr    The output Bidi Attributes (TextType, TextShape, ...etc.)
 * @param str        The buffer to be shaped.
 * @param optionSet  The Arabic options used in shaping process.
*/
  synchronized char[] shape(BidiFlagSet inAttr, BidiFlagSet outAttr, char str[], ArabicOptionSet optionSet)
  {
    boolean rtl = true;
    BidiFlag   inTextType, outTextType, inTextShape, outTextShape;
    ArabicOption lamAlefOpt, seenOpt, yehHamzaOpt, tashkeelOpt;

    int bufLen = str.length;
    if ( bufLen == 0)
        return str;

    if(optionSet == null) //Set Arabic options to the default values in case user did not specify them.
     {
        optionSet = new ArabicOptionSet();
          }

    //Initialize variables
    inTextType   = inAttr.getType();
    outTextType  = outAttr.getType();
    rtl = outAttr.getOrientation() == BidiFlag.ORIENTATION_RTL;
    inTextShape  = inAttr.getText();
    outTextShape = outAttr.getText();
    lamAlefOpt   = optionSet.getLamAlefMode();
    seenOpt      = optionSet.getSeenMode();
    yehHamzaOpt  = optionSet.getYehHamzaMode();
    tashkeelOpt  = optionSet.getTashkeelMode();

    if(inTextType == BidiFlag.TYPE_VISUAL && outTextType == BidiFlag.TYPE_IMPLICIT) //Visual to Implicit
    {
        //Seen Deshaping
        if( (seenOpt == ArabicOption.SEEN_NEAR) || (seenOpt == ArabicOption.SEEN_AUTO) )
            deshapeSeenNear(str,bufLen);

        /*Yeh Hamza DeShapping*/

        if (( yehHamzaOpt == ArabicOption.YEHHAMZA_TWO_CELL_NEAR ) || ( yehHamzaOpt == ArabicOption.YEHHAMZA_AUTO))
        {
            deshapeYehHamzaTwoCellNear(str, bufLen);
        }

        /*Tashkeel DeShapping*/
        if((tashkeelOpt == ArabicOption.TASHKEEL_KEEP) || ( tashkeelOpt == ArabicOption.TASHKEEL_AUTO))
        {
            //No processing is done
        }
        else if (tashkeelOpt == ArabicOption.TASHKEEL_CUSTOMIZED_ATBEGIN)
        {
            customizeTashkeelAtBegin( str, bufLen);
        }
        else if (tashkeelOpt == ArabicOption.TASHKEEL_CUSTOMIZED_ATEND)
        {
            customizeTashkeelAtEnd( str, bufLen);
        }

        /*LamAlef DeShapping*/
        if( lamAlefOpt == ArabicOption.LAMALEF_RESIZE_BUFFER ){
            str=deshapeLamAlefWithResizeBuffer(str, bufLen);
            bufLen= str.length;
        }
        else if (lamAlefOpt == ArabicOption.LAMALEF_NEAR)
        {
            deshapeLamAlefNear(str, bufLen);
        }
        else if (lamAlefOpt == ArabicOption.LAMALEF_ATBEGIN)
        {
            deshapeLamAlefAtBegin(str, bufLen);
        }
        else if (lamAlefOpt == ArabicOption.LAMALEF_ATEND)
        {
            deshapeLamAlefAtEnd(str, bufLen);
        }
        else if(lamAlefOpt == ArabicOption.LAMALEF_AUTO ){
            deshapeLamAlefAuto(str, bufLen,rtl);
        }

        shapingRoutine(inAttr, outAttr, str, rtl);

     }//end if Visual to Implicit

    else if(outTextType == BidiFlag.TYPE_VISUAL)
    {
        shapingRoutine(inAttr, outAttr, str, rtl);

        /* Seen Shapping*/
        if( (seenOpt == ArabicOption.SEEN_NEAR) || (seenOpt == ArabicOption.SEEN_AUTO) )
            shapeSeenNear(str,bufLen, rtl);

        /* Yeh Hamza Shapping*/
        if( (yehHamzaOpt == ArabicOption.YEHHAMZA_TWO_CELL_NEAR) || ( yehHamzaOpt == ArabicOption.YEHHAMZA_AUTO)){
            shapeYehHamzaTwoCellNear(str, bufLen, rtl);
        }

        /*Tashkeel Shapping*/
        if((tashkeelOpt == ArabicOption.TASHKEEL_KEEP) || ( tashkeelOpt == ArabicOption.TASHKEEL_AUTO))
        {
            //No processing is done
        }
        else if (tashkeelOpt == ArabicOption.TASHKEEL_CUSTOMIZED_ATBEGIN)
        {
            customizeTashkeelAtBegin( str, bufLen);
        }
        else if (tashkeelOpt == ArabicOption.TASHKEEL_CUSTOMIZED_ATEND)
        {
            customizeTashkeelAtEnd( str, bufLen);
        }

        /* LamAlef Handling*/
        if( lamAlefOpt == ArabicOption.LAMALEF_RESIZE_BUFFER ){
            str=handleLamAlefWithResizeBuffer(str, bufLen, rtl);
            bufLen=str.length;
        }
        else if (lamAlefOpt == ArabicOption.LAMALEF_NEAR){
            handleLamAlefNear(str, bufLen, rtl);
        }
        else if (lamAlefOpt == ArabicOption.LAMALEF_ATBEGIN)
        {
            // In case of LTR spaces are already in the absolute begining of buffer
            if (rtl)
                handleLamAlefAtBegin(str, bufLen);
        }
        else if (lamAlefOpt == ArabicOption.LAMALEF_ATEND)
        {
            // In case of RTL spaces are already in the absolute end of buffer
            if (!rtl)
                handleLamAlefAtEnd(str, bufLen);
        }
        else if(lamAlefOpt == ArabicOption.LAMALEF_AUTO )
        {
            //No processing is done
        }

    }// end of else if to shaped
    return str;
  }

/**
 * Method uba_getLink.
 *
 * @param x the character to be checked
 * @return int
 */
  private static int uba_getLink(char x)
  {
    if (x >= 0x0622 && x <= 0x06D3)  //06 Range
      return(Link06[x-0x0622]);
    else if (x == 0x200D)            //(ZWJ)
      return(3);
    else if (x >= 0x206D && x <= 0x206F)   //Alternate Formating
      return(4);
    else if (x >= 0xFE70 && x <= 0xFEFC)  //FE range
      return(LinkFE[x-0xFE70]);
    else
      return(0);
  }

/**
 * Method Lamalef.
 *
 * @param x     the character to be checked
 * @return char
 */
  private static char Lamalef(char x)
  {
     switch(x)
     {
         case 0x0622:  return (0x065C);
         case 0x0623:  return (0x065D);
         case 0x0625:  return (0x065E);
         case 0x0627:  return (0x065F);
     }
      return(0);
  }

/**
 * Method specialChar.
 *
 * @param ch    the character to be checked
 * @return int
 */
  private static int specialChar(char ch)
  {
    // hamza ,
    if ((ch >= 0x0621 && ch < 0x0626)|| (ch == 0x0627 )||
        (ch > 0x062e && ch < 0x0633) ||
        (ch > 0x0647 && ch < 0x064a) || ch == 0x0629)
      return(1);
    else
      return(0);
  }

/**
 * Method SeenChar.
 * This method checks if the passed chracter is one of the Seen family characters.
 * Seen family characters are Seen, Sheen, Sad and Dad. For each character of them there are
 * two shapes.
 *
 * @param ch        The character to be checked.
 * @return boolean true, if the character belongs to Seen family.
 *                  false, otherwise
 */
  private static boolean SeenChar(char ch)
  {
    if ((ch==0xFEB1) ||
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


//The following methods are responsible for handling Arabic Options


                                    /* ******************************** */
                                    /*                                  */
                                    /*  Methods for Lam Alef handling   */
                                    /*                                  */
                                    /* ******************************** */

/**
 *  Method handleLamAlefWithResizeBuffer.
 *  This method shrink the input buffer by the number of Lam-Alef occurences.
 *  The buffer is supposed to come with LamAlef shaped with a space at end/begin
 *  of the buffer depending on wherther the output is rtl or ltr.
 *  And the method role is to remove this space and shrink the buffer
 *
 *  @param  buffer  The buffer containing the data to be processed.
 *  @param  length  The buffer length.
 *  @param  rtl     The buffer orientation.
 *  @return int The new size of the buffer.
 *
 */

 private char[] handleLamAlefWithResizeBuffer(char[] buffer, int length, boolean rtl){

    int counter =0 ;
    int lamAlefCount=0;
    char compressedBuffer[];

    for (counter=0;counter<length;counter++){
        if(( buffer[counter] >= 0xFEF5) && (buffer[counter] <= 0xFEFC ))
            lamAlefCount++;
    }

    if(lamAlefCount == 0) return buffer;

    compressedBuffer = new char[length-lamAlefCount];
    if(rtl){//Remove the sapces at the end of the buffer
        System.arraycopy(buffer,0, compressedBuffer, 0, buffer.length-lamAlefCount);
    }
    else {//Remove the sapces in the begining of the buffer
        System.arraycopy(buffer, lamAlefCount, compressedBuffer, 0, buffer.length-lamAlefCount);
    }

    return compressedBuffer;
 }

/**
 *  Method deshapeLamAlefWithResizeBuffer.
 *  This method enlarge the input buffer by the number of Lam-Alef occurences.
 *  All Lam-Alef characters are expanded to Lam + Alef characters .
 *
 *  @param  buffer  The buffer containing the data to be processed.
 *  @param  length  The buffer length.
 *  @param  rtl     The buffer orientation.
 *  @return int The new size of the buffer.
 *
 */

 private char[] deshapeLamAlefWithResizeBuffer(char[] buffer, int length){

    char ChAlefType;
    int oldBufferCounter =0 ;
    int newBufferCounter =0 ;
    int lamAlefCount=0;
    char expandedBuffer[];

    for (oldBufferCounter=0;oldBufferCounter<length;oldBufferCounter++){
        if(( buffer[oldBufferCounter] >= 0xFEF5) && (buffer[oldBufferCounter] <= 0xFEFC ))
            lamAlefCount++;
    }
    if(lamAlefCount == 0) return buffer;
    expandedBuffer = new char[length+lamAlefCount];
    oldBufferCounter =0;
        while (oldBufferCounter < length)
        {
           if ( ( buffer[oldBufferCounter] >= 0xFEF5) && (buffer[oldBufferCounter] <= 0xFEFC ))
                {
                    ChAlefType = (char)AlefType[buffer[oldBufferCounter]-0xFEF5];
                    expandedBuffer[newBufferCounter]  =  '\u0644';
                    newBufferCounter++;
                    expandedBuffer[newBufferCounter]  = ChAlefType;
                }
            else
                {
                    expandedBuffer[newBufferCounter] = buffer[oldBufferCounter];
                }
            newBufferCounter++;
            oldBufferCounter ++;
        }

    return expandedBuffer;
 }

/**
 *  Method handleLamAlefNear
 *  This method moves the spaces located in the begining of buffer with respect to
 *  orientation (buffer[0] in LTR and buffer[length -1] in RTL) to the position near
 *  to each of Lam-Alef characters.
 *
 *  @param  buffer  The buffer containing the data to be processed.
 *  @param  length  The buffer length.
 *  @param  rtl     The buffer orientation.
 *
 */

 private void handleLamAlefNear(char[] buffer, int length, boolean rtl)
 {
    int count = 0;
    int tempIdx;

    while (count < length)
    {
        if ((buffer[count] >= 0xFEF5) && (buffer[count] <= 0xFEFC ) && buffer[length - 1] == 0x0020 && rtl)
        {
            for (tempIdx = length -1; tempIdx > count + 1; tempIdx--)
                buffer [tempIdx] = buffer [tempIdx - 1];
            if(count < length - 1)
                buffer[count + 1] = 0x0020;
        }
        else if ((buffer[count] >= 0xFEF5) && (buffer[count] <= 0xFEFC ) && buffer[0] == 0x0020 && !rtl)
        {
            for (tempIdx = 0; tempIdx < count - 1; tempIdx++)
                buffer [tempIdx] = buffer [tempIdx + 1];
            if (count != 0)
                buffer[count - 1] = 0x0020;
        }

        count ++;
    }
 }


/**
 *  Method deshapeLamAlefNear
 *  This method expands Lam-Alef character to Lam and Alef using the space near to
 *  Lam-Alef character. In case no spaces occurs near this character no processing
 *  will be done and the output buffer will contain the Lam-Alef character in its
 *  FE hexadecimal value.
 *
 *  @param  buffer  The buffer containing the data to be processed.
 *  @param  length  The buffer length.
 *
 */

 private void deshapeLamAlefNear(char[] buffer, int length)
 {
    char ChAlefType;
    int count = 1;

  while (count < length)
  {
      if ( (buffer[count] >= 0xFEF5) && (buffer[count] <= 0xFEFC ) &&
            ( (count!= length - 1) && ( buffer[count + 1] == 0x0020) )
         )

    {
      ChAlefType = (char)AlefType[buffer[count]-0xFEF5];
      buffer[count + 1] = ChAlefType;
      buffer[count] = '\u0644';
    }

    count ++;
    }
 }


/**
 *  Method handleLamAlefAtBegin
 *  This method moves the space located in the absolute end of buffer to the absolute begin of buffer so these
 *  spaces can be used in Lam-Alef deshaping in the other way back.
 *  character.
 *
 *  @param  buffer  The buffer containing the data to be processed.
 *  @param  length  The buffer length.
 *
 */

 private void handleLamAlefAtBegin(char[] buffer, int length)
 {
    int count = 0;
    int tempIdx, lamAlefOccurences = 0;

    while (count < length)
    {
        if ((buffer[count] >= 0xFEF5) && (buffer[count] <= 0xFEFC ) && buffer[length - 1] == 0x0020)
        {
            for (tempIdx = length - 1; tempIdx >lamAlefOccurences; tempIdx--)
                buffer [tempIdx] = buffer [tempIdx - 1];
            buffer[lamAlefOccurences] = 0x0020;
            lamAlefOccurences++;
            count++;
        }
        count ++;
    }
 }


/**
 *  Method deshapeLamAlefAtBegin
 *  This method expands Lam-Alef character to Lam and Alef using the spaces in the absolute begin of buffer.
 *  In case no spaces occurs near this character no processing will be done and the output buffer will contain
 *  the Lam-Alef character in its FE hexadecimal value.
 *
 *  @param  buffer  The buffer containing the data to be processed.
 *  @param  length  The buffer length.
 *
 */

 private void deshapeLamAlefAtBegin(char[] buffer, int length)
 {
    char ChAlefType;
    int count = 0 ;
    int tempIdx;

    while (count < length)
    {
      if ( (buffer[count] >= 0xFEF5) && (buffer[count] <= 0xFEFC ) && buffer[0] == 0x0020)
      {
        ChAlefType = (char)AlefType[buffer[count]-0xFEF5];

        for (tempIdx = 0; tempIdx < count - 1; tempIdx++)
            buffer [tempIdx] = buffer [tempIdx + 1];

            buffer[count - 1] = '\u0644';
            buffer[count]     = ChAlefType;
        }

      count ++;
    }

 }


/**
 *  Method handleLamAlefAtEnd
 *  This method moves the space located in the absolute begin of buffer to the absolute end of buffer so these
 *  spaces can be used in Lam-Alef deshaping in the other way back.
 *  character.
 *
 *  @param  buffer  The buffer containing the data to be processed.
 *  @param  length  The buffer length.
 *
 */

 private void handleLamAlefAtEnd(char[] buffer, int length)
 {
    int count = 0;
    int tempIdx, lamAlefOccurences = 0;

    while (count < length)
    {
        if ((buffer[count] >= 0xFEF5) && (buffer[count] <= 0xFEFC ) && buffer[0] == 0x0020)
        {
            for (tempIdx = 0; tempIdx < length - lamAlefOccurences - 1; tempIdx++)
                buffer [tempIdx] = buffer [tempIdx + 1];
            buffer[length - lamAlefOccurences - 1] = 0x0020;
            lamAlefOccurences++;
        }
        count ++;
    }
 }

/**
 *  Method deshapeLamAlefAtEnd
 *  This method expands Lam-Alef character to Lam and Alef using the spaces in the absolute end of buffer.
 *  In case no spaces occurs near this character no processing will be done and the output buffer will contain
 *  the Lam-Alef character in its FE hexadecimal value.
 *
 *  @param  buffer  The buffer containing the data to be processed.
 *  @param  length  The buffer length.
 *
*/

  private void deshapeLamAlefAtEnd(char[] buffer, int length)
 {
    char ChAlefType;
    int count =0 ;
    int tempIdx;

    while (count < length)
    {      
      if ( (buffer[count] >= 0xFEF5) && (buffer[count] <= 0xFEFC ) && buffer[length - 1] == 0x0020)
      {
        ChAlefType = (char)AlefType[buffer[count]-0xFEF5];

        for (tempIdx = length - 1; tempIdx > count + 1; tempIdx--)
            buffer [tempIdx] = buffer [tempIdx - 1];

        buffer[count] = '\u0644';
        buffer[count + 1]     = ChAlefType;

      }

      count ++;
    }

 }

/**
 * Method handleLamAlefAuto.
 *
 *  @param  buffer  The buffer containing the data to be processed.
 *  @param  length  The buffer length.
 *  @param  rtl      The buffer orientation.
 *
 */

  private void handleLamAlefAuto(char[] buffer, int length, boolean rtl)
  {}

/**
 * Method deshapeLamAlefAuto.
 *
 * This method expands LamAlef character to Lam and Alef characters consuming the space
 * located in the in the begining of buffer with respect to orientation (buffer[0] in LTR
 * and buffer[length -1] in RTL)
 *
 *  @param  buffer  The buffer containing the data to be processed.
 *  @param  length  The buffer length.
 *  @param  rtl      The buffer orientation.
        */

 private void deshapeLamAlefAuto(char[] buffer, int length, boolean rtl){

     char ChAlefType;
    int count =0 ;

    while (count < length)
    {
    if ( ( buffer[count] >= 0xFEF5) && (buffer[count] <= 0xFEFC )&&buffer[length-1]==0x0020&&rtl)
        {
        ChAlefType = (char)AlefType[buffer[count]-0xFEF5];

        for (int h=length-1;h>count;h--)
                buffer[h] = buffer[h-1];

        buffer[count+1] =  ChAlefType;
        buffer[count] = '\u0644';
        }
    else if ( ( buffer[count] >= 0xFEF5) && (buffer[count] <= 0xFEFC )&&buffer[0]==0x0020&&!rtl)
        {

        ChAlefType = (char)AlefType[buffer[count]-0xFEF5];

        for (int h=0;h<count;h++)
                buffer[h] = buffer[h+1];

        buffer[count] = ChAlefType;
        buffer[count-1] = '\u0644';

        }
      count ++;
    } // end of loop
 }

                                    /* ******************************** */
                                    /*                                  */
                                    /*     Methods for Seen handling    */
                                    /*                                  */
                                    /* ******************************** */

 /**
 *  This method replaces the space near to Seen character to by a Seen Tail.
 *
 *  @param  buffer  The buffer containing the data to be processed.
 *  @param  length  The buffer length.
 *  @param  rtl  The buffer orientation.
 *
 */

 private void shapeSeenNear(char[] buffer, int length, boolean rtl)
 {
    for (int idx = 0; idx < length ; idx++)
    {
        if(rtl)
        {
            if ( SeenChar(buffer[idx]) && ( (idx+1 < length) && ( buffer[idx+1] == 0x0020) ) )

                buffer[idx+1] =BidiShape.Tail;
        }
        else
        {
            if ( SeenChar(buffer[idx]) && ( (idx-1 >= 0) && ( buffer[idx-1] == 0x0020) ) )

                buffer[idx-1] = BidiShape.Tail;
        }
     }
 }

 /**
 *  This method replaces Seen Tail by a space near to the Seen character.
 *
 *  @param  buffer  The buffer containing the data to be processed.
 *  @param  length  The buffer length.
 *
 */

 private void deshapeSeenNear(char[] buffer, int length)
 {
        for (int idx = 0; idx < length ; idx++)
        {
            if (buffer[idx] == BidiShape.Tail)
                buffer[idx]= (char)0x0020;
        }
 }

/**
 * Method shapeSeenAtBegin.
 *
 * @param buffer
 * @param length
 * @param rtl
 */

 private void shapeSeenAtBegin(char[] buffer, int length, boolean rtl){}
/**
 * Method deshapeSeenAtBegin.
 *
 * @param buffer
 * @param length
 * @param rtl
 */

 private void deshapeSeenAtBegin(char[] buffer, int length, boolean rtl){}

/**
 * Method shapeSeenAtEnd.
 *
 * @param buffer
 * @param length
 * @param rtl
 */
 private void shapeSeenAtEnd(char[] buffer, int length, boolean rtl){}

/**
 * Method deshapeSeenAtEnd.
 *
 * @param buffer
 * @param length
 * @param rtl
 */

 private void deshapeSeenAtEnd(char[] buffer, int length, boolean rtl){}

/**
 * Method handleSeenAuto.
 *
 * @param buffer
 * @param length
 * @param rtl
 */

 private void handleSeenAuto(char[] buffer, int length, boolean rtl){}

/**
 * Method deshapeSeenAuto.
 * @param buffer
 * @param length
 * @param rtl
 */
 private void deshapeSeenAuto(char[] buffer, int length, boolean rtl){}


                                    /* ******************************** */
                                    /*                                  */
                                    /*  Methods for Yeh Hamza handling  */
                                    /*                                  */
                                    /* ******************************** */

/**
 * Method shapeYehHamzaTwoCellNear.
 * This method convert every YehHamza to Yeh + Hamza depending on whether the output
 * buffer is rtl or ltr using the space near the YeahHamza
 *
 *  @param  buffer  The buffer containing the data to be processed.
 *  @param  length  The buffer length.
 *  @param  rtl      The buffer orientation.
 *
 */
 private void shapeYehHamzaTwoCellNear(char[] buffer, int length, boolean rtl){
    int counter=0;
    for (counter =0; counter < length ; counter++)
     {
      if(rtl)
       {
          if ( buffer[counter] == 0xfe8a )
                {
                    buffer[counter]= 0xfef0;//Yeh
                    if ( (counter+1 < length) && ( buffer[counter+1] == 0x0020) )
                      buffer[counter+1] = 0xfe80; //hamza
                }
         if ( buffer[counter] == 0xfe89 )
                {
                    buffer[counter] = 0xfeef ;//Yeh
                    if ( (counter+1 < length) && ( buffer[counter+1] == 0x0020) )
                      buffer[counter+1] = 0xfe80 ;//hamza
                }
          }
        else
          {
             if ( buffer[counter] == 0xfe8a )
              {
                        buffer[counter]= 0xfef0;//Yeh
                        if ( (counter-1 >= 0) && ( buffer[counter-1] == 0x0020) )
                          buffer[counter-1] = 0xfe80; //hamza
              }
            if ( buffer[counter] == 0xfe89 )
              {
                    buffer[counter] = 0xfeef;//Yeh
                    if ( (counter-1 >= 0) && ( buffer[counter-1] == 0x0020) )
                      buffer[counter-1] = 0xfe80 ;//hamza
              }
          }
      }
}


/**
 * Method deshapeYehHamzaTwoCellNear.
 * This method convert every Yeh follwed by a Hamza to YehHamza character
 * and put the spaces near the character
 *
 *  @param  buffer  The buffer containing the data to be processed.
 *  @param  length  The buffer length.
 *
 */
 private void deshapeYehHamzaTwoCellNear(char[] buffer, int length){

    int counter=0;
    for (counter =0; counter < length ; counter++)
     {
          if ((counter+1 <length)&&( buffer[counter] == 0xfef0 )&&(buffer[counter+1] == 0xfe80))
                {
                    buffer[counter]= 0xfe8a;//YehHamze
                    buffer[counter+1] = 0x0020; //Space
                }
         if ((counter+1 <length)&&( buffer[counter] == 0xfeef )&&(buffer[counter+1] == 0xfe80))
                {
                    buffer[counter] = 0xfe89 ;//YehHamza
                    buffer[counter+1] = 0x0020 ;//Space
                }
      }
 }

/**
 * Method shapeYehHamzaAtBegin.
 *
 *  @param  buffer  The buffer containing the data to be processed.
 *  @param  length  The buffer length.
 *  @param  rtl      The buffer orientation.
 *
 */
 private void shapeYehHamzaAtBegin(char[] buffer, int length, boolean rtl){}

/**
 * Method deshapeYehHamzaAtBegin.
 *
 *  @param  buffer  The buffer containing the data to be processed.
 *  @param  length  The buffer length.
 *
 */

 private void deshapeYehHamzaAtBegin(char[] buffer, int length){}

/**
 * Method shapeYehHamzaAtEnd.
 *
 *  @param  buffer  The buffer containing the data to be processed.
 *  @param  length  The buffer length.
 *  @param  rtl      The buffer orientation.
 *
 */

 private void shapeYehHamzaAtEnd(char[] buffer, int length, boolean rtl){}

/**
 * Method deshapeYehHamzaAtEnd.
 *
 *  @param  buffer  The buffer containing the data to be processed.
 *  @param  length  The buffer length.
 *
 */

 private void deshapeYehHamzaAtEnd(char[] buffer, int length){}

/**
 * Method shapeYehHamzaAuto.
 *
 *  @param  buffer  The buffer containing the data to be processed.
 *  @param  length  The buffer length.
 *  @param  rtl      The buffer orientation.
 *
 */

 private void shapeYehHamzaAuto(char[] buffer, int length, boolean rtl)
 {
 }

/**
 * Method deshapeYehHamzaAuto.
 *
 *  @param  buffer  The buffer containing the data to be processed.
 *  @param  length  The buffer length.
 *
 */

 private void deshapeYehHamzaAuto(char[] buffer, int length)
 {
 }

                                    /* ******************************** */
                                    /*                                  */
                                    /*   Methods for Tashkeel handling  */
                                    /*                                  */
                                    /* ******************************** */

/**
 * Method customizeTashkeelZeroWidth.
 *
 *  @param  buffer  The buffer containing the data to be processed.
 *  @param  length  The buffer length.
 *
 */

 private void  customizeTashkeelZeroWidth(char[] buffer, int length){}


/**
 * Method customizeTashkeelWithWidth.
 *
 *  @param  buffer  The buffer containing the data to be processed.
 *  @param  length  The buffer length.
 *
 */

 private void  customizeTashkeelWithWidth(char[] buffer, int length){}

/**
 * Method customizeTashkeelAtBegin.
 *
 *  @param  buffer  The buffer containing the data to be processed.
 *  @param  length  The buffer length.
 *
 */
 private void  customizeTashkeelAtBegin(char[] buffer, int length)
 {
    int count =0 ;
    int tashkeelOcuurences = 0;

    while (count < length)
    {
        if ( ( (buffer[count] >= 0xFE70) && (buffer[count] <= 0xFE72) ) ||
               (buffer[count] == 0xFE74) ||
             ( (buffer[count] >= 0xFE76) && (buffer[count] <= 0xFE7B) ) ||
             ( (buffer[count] >= 0xFE7E) && (buffer[count] <= 0xFE7F) ) )
        {
            for(int idx = count; idx > tashkeelOcuurences; idx--)
                buffer[idx] = buffer[idx - 1];
            buffer[tashkeelOcuurences] = 0x0020;
            tashkeelOcuurences++;
        }
        count ++;
    }
 }


/**
 * Method customizeTashkeelAtEnd.
 *
 *  @param  buffer  The buffer containing the data to be processed.
 *  @param  length  The buffer length.
 *
 */
 private void  customizeTashkeelAtEnd(char[] buffer, int length)
 {
     int count =0 ;
    int tashkeelOcuurences = 0;

    while (count < length)
    {
        if ( ( (buffer[count] >= 0xFE70) && (buffer[count] <= 0xFE72) ) ||
               (buffer[count] == 0xFE74) ||
             ( (buffer[count] >= 0xFE76) && (buffer[count] <= 0xFE7B) ) ||
             ( (buffer[count] >= 0xFE7E) && (buffer[count] <= 0xFE7F) ) )
        {
            for(int idx = count; idx < length - tashkeelOcuurences - 1; idx++)
                buffer[idx] = buffer[idx + 1];
            buffer[length - tashkeelOcuurences - 1] = 0x0020;
            tashkeelOcuurences++;
        }
        count ++;
    }

 }


/**
 * Method handleTashkeelAuto.
 *
 *  @param  buffer  The buffer containing the data to be processed.
 *  @param  length  The buffer length.
 *
 */
 private void  handleTashkeelAuto(char[] buffer, int length){}

 }

