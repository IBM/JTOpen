///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: NPDataAnalyzer.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

/**
  * Class NPDataAnalyzer is an internal class used to analyze print data
  * streams.  It can detect if they are SCS or AFP and if they are not
  * one of those it will default to USERASCII.
  *
  **/

class NPDataAnalyzer extends Object
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


    private static final String DT_AFPDS     = "*AFPDS";
    private static final String DT_USERASCII = "*USERASCII";
    private static final String DT_SCS       = "*SCS";

    private static boolean dataIsAFP(byte[] buf, int offset, int len)
    {
        boolean fRC = false, fDone = false;
        int     tempOffset = offset;
        int     tempLen;
        while ((tempOffset < len) && !fDone)
        {
            if (buf[tempOffset] == 0x5A)
            {
                // we will say this is AFP if we see atleast 2 0x5As
                if (tempOffset != offset)
                {
                    fRC = true;  // second or later 5A
                }
                // if we have atleast 3 bytes left
                // jump ahead that many bytes
                if ((len-tempOffset) >= 3)
                {
                    // get the AFP structured field length from BigEndian
                    tempOffset++;  // move to length field
                    // tempLen = xlate.getInt16(buf, tempOffset);
                    tempLen = BinaryConverter.byteArrayToUnsignedShort(buf, tempOffset);
                    tempOffset += tempLen;
                    if (tempLen < 5)
                    {
                        // can never be less than 5 for the
                        // length because the length itself is 2 bytes
                        // and then there is the 3 byte triplet.
                        fDone = true;    // we are done here
                        fRC = false;     // not AFP
                    }
                } else {
                    fDone = true;  // not enough data to process we are done

                }
            } else {
                // not a 5A, NOT AFP and we are done
                fRC = false;
                fDone = true;
            }
        } // end while()


        return fRC;
    } // dataIsAFP()

    private static boolean dataIsSCS(byte[] buf, int offset, int len)
    {
        boolean fRC = false;
        boolean fDone = false;

        while ((len > 0) &&
                !fDone)
        {
            // cast next byte to an int and AND (&) it with 0xFF,
            //  because we want unsigned bytes and we do a less
            //  than operation with it (<) and don't want it to be
            //  negative ever.
            int nextByte = (int)buf[offset] & 0x000000FF;
            if (nextByte < (int)SPACE_CC)
            {
                switch(nextByte)
                {
                    /**********************************************************
                    * Process all one byte SCS commands                       *
                    * I do not set the RC to YES here because one byte of these*
                    *  doesnot make it SCS data.  A case in point was where   *
                    *  the use sends 1K of 0x36 codepoints - should be a K of *
                    *  ASCII "6" chars, but we say it is SCS and only contains *
                    *  the 0x36 character, no writable data.                  *
                    * CASE backspace:                                         *
                    * CASE expanded backspace:                                *
                    * CASE unitbackspace:                                     *
                    * CASE bel:                                               *
                    * CASE carriage return:                                   *
                    * CASE form feed:                                         *
                    * CASE required form feed:                                *
                    * CASE graphic escape:                                    *
                    * CASE horizontal tab:                                    *
                    * CASE indent tab:                                        *
                    * CASE line feed:                                         *
                    * CASE required new line:                                 *
                    * CASE null:                                              *
                    * CASE new line:                                          *
                    * CASE interchange record separator:                      *
                    * CASE enable presentation:                               *
                    * CASE inhibit presentation:                              *
                    * CASE word underscore:                                   *
                    * CASE vertical tab:                                      *
                    * CASE switch:                                            *
                    *   Move the SCSDatapointer 1 byte ahead                  *
                    *   subtract 1 from the length remaining                  *
                    *   BREAK                                                 *
                    **********************************************************/
                    case BACKSPACE_CC:
                    case EXPANDED_BACKSPACE_CC:
                    case UNITBACKSPACE_CC:
                    case BEL_CC:
                    case CARRIAGE_RETURN_CC:
                    case SUBSCRIPT_CC:
                    case SUPERSCRIPT_CC:
                    case FORM_FEED_CC:
                    case REQUIRED_FORM_FEED_CC:
                    case HORIZONTAL_TAB_CC:
                    case INDENT_TAB_CC:
                    case LINE_FEED_CC:
                    case INDEX_RETURN_CC:
                    case REQUIRED_NEW_LINE_CC:
                    case NEW_LINE_CC:
                    case INTERCHANGE_RECORD_SEPARATOR_CC:
                    case ENABLE_PRESENTATION_CC:
                    case INHIBIT_PRESENTATION_CC:
                    case WORD_UNDERSCORE_CC:
                    case VERTICAL_TAB_CC:
                    case SHIFT_OUT_CC:
                    case SHIFT_IN_CC:
                    case SUBSTITUTE_CC:
                    case SWITCH_CC:
                    case NULL_CC:
                       offset++;
                       len--;
                       break;
                    /**********************************************************
                    * CASE presentation position:                             *
                    *   IF we have all the data for the command               *
                    *     THEN                                                *
                    *       CALL PRESENTATIONPOSITION(function code from      *
                    *         command, value from command) to move to the     *
                    *         presentations position specified                *
                    *       Subtract the size of the PP command from the      *
                    *         length field in the print message               *
                    *       Add the size of the PP command to the work        *
                    *         pointer so that it points to the next           *
                    *         command                                         *
                    *     ELSE                                                *
                    *       Flag that there is no more stuff to process       *
                    *   BREAK                                                 *
                    **********************************************************/

                    case PRESENTATION_POSITION_CC:
                       if(len >= PP_STRUCT_LEN)
                       {
                          switch(buf[offset+1])
                          {

                             /*************************************************
                             * CASE Absolute Horizontal Presentation Position *
                             * CASE Absolute Verital Presentation Position    *
                             * CASE Relative Horizontal Presentation Position *
                             * CASE Relative Verital Presentation Position    *
                             *   BREAK                                        *
                             *************************************************/
                             case PPCODE_AHPP:
                             case PPCODE_AVPP:
                             case PPCODE_RHPP:
                             case PPCODE_RVPP:
                                offset += PP_STRUCT_LEN;
                                len    -= PP_STRUCT_LEN;
                                fRC = true;
                                break;
                             default:
                                fRC = false;
                                fDone = true;   // we are done here
                          } /* end switch */
                       } else {
                          fDone = true;  // not enough left to process
                       }
                       break;
                    /**********************************************************
                    * CASE set attribute:                                     *
                    *   IF we have all the data for the command               *
                    *     THEN                                                *
                    *       CALL SETATTRIBUTE(type from command, value from   *
                    *         command to set the attribute                    *
                    *       Subtract the size of the SA command from the      *
                    *         length field in the print message               *
                    *       Add the size of the SA command to the work        *
                    *         pointer so that it points to the next           *
                    *         command                                         *
                    *     ELSE                                                *
                    *       Flag that there is no more stuff to process       *
                    *   BREAK                                                 *
                    **********************************************************/

                    case SET_ATTRIBUTE_CC:
                       if(len >= SA_STRUCT_LEN)
                       {
                          switch(buf[offset+1])
                          {

                             /*************************************************
                             * CASE ATTR_CHAR_SET                             *
                             *  we don't support the other attributes on the  *
                             *   the server.                                  *
                             *   BREAK                                        *
                             *************************************************/
                             case ATTR_CHAR_SET:
                                offset += SA_STRUCT_LEN;
                                len    -= SA_STRUCT_LEN;
                                fRC = true;
                                break;
                             default:
                                fRC = false;
                                fDone = true;
                          } /* end switch */
                       } else {
                          fDone = true;
                       }
                       break;
                    /**********************************************
                    * case 2B is a multibyte command              *
                    *  2BCCnntt....                               *
                    * CC is the command class (D1, D2, etc)       *
                    * nn is the number of bytes left in the       *
                    *   command including this nn byte.           *
                    * tt is the type                              *
                    **********************************************/
                    case CSP_CC:
                       if ( (len >= 3) &&
                            (len >= (buf[offset + 2] + 2) ) )
                       {
                          int count = buf[offset+2];
                          switch(buf[offset+1])
                          {

                             /*************************************************
                             * CASE set graphic error action:                 *
                             * CASE set horizontal format:                    *
                             * CASE set line density:                         *
                             * CASE set vertical format:                      *
                             *   BREAK                                        *
                             *************************************************/

                             case GRAPHIC_ERROR_ACTION_CC:
                             case HORIZONTAL_FORMAT_CC:
                             case LINE_DENSITY_CC:
                             case VERTICAL_FORMAT_CC:
                                len    -=  (count + 2);
                                offset +=   (count + 2);
                                fRC = true;

                             break;
                            /*************************************************
                            * CASE FontClass                                 *
                            *  a FONTCLASS is class 'D1'X.  The commands     *
                            *  are SFG, SCG and SCGL, BES, EES               *
                            *     casentry type                              *
                            *     CASE SFG                                   *
                            *     CASE BES                                   *
                            *     CASE EES                                   *
                            *     CASE SCG                                   *
                            *     CASE SFG                                   *
                            *       Set as SCS data and advance past command *
                            *       BREAK                                    *
                            *     DEFAULT                                    *
                            *       Not SCS and we are done.                 *
                            *       BREAK                                    *
                            *   BREAK                                        *
                            *************************************************/

                            case FONTCLASS:
                            {
                               switch (buf[offset+3])    // switch on the type
                               {
                                  case SFG_CC:
                                  case BES_CC:
                                  case EES_CC:
                                  case SCG_CC:
                                  case SCGL_CC:
                                  {
                                     len     -= (count + 2);
                                     offset  += (count + 2);
                                     fRC = true;;
                                    break;
                                  } /* endcase */
                                  default:
                                  {
                                     fRC = false;
                                     fDone = true;
                                     break;
                                  }
                               } /* endswitch */
                               break;
                            } /* end CASE D1 */
                            /*************************************************
                            * CASE D2:                                       *
                            *     casentry type                              *
                            *     CASE SetHorzTabs (STAB)                    *
                            *     CASE SetPrintDensity                       *
                            *     CASE SetLineSpacing                        *
                            *     CASE SetSingleLineDistance                 *
                            *     CASE SetHorizontalMargins                  *
                            *     CASE SetVerticalMargins                    *
                            *     CASE SJM command                           *
                            *     CASE JTF command                           *
                            *     CASE SetErrorAction                        *
                            *     CASE SetInitialConditions                  *
                            *     CASE SetIndentLevel                        *
                            *     CASE SetPresentationColor                  *
                            *     CASE SetPresentationPageMedia              *
                            *     CASE SetPrinterSetup                       *
                            *         Set as SCS data                        *
                            *         Advance past this command              *
                            *         BREAK                                  *
                            *     DEFAULT                                    *
                            *       Not SCS and we are done.                 *
                            *       BREAK                                    *
                            *     ENDCASE                                    *
                            *   BREAK                                        *
                            *************************************************/

                            case DOGTWO:
                            {
                               switch (buf[offset+3])    // switch on the type
                               {
                                  case STAB_CC:
                                  case PRINT_DENSITY_CC:
                                  case SET_LINESPACE_CC:
                                  case SET_LINEDISTANCE_CC:
                                  case SET_HORZMARGINS_CC:
                                  case SET_VERTMARGINS_CC:
                                  case SET_ERROR_ACTION_CC:
                                  case SET_INITCOND_CC:
                                  case SET_INDENTLV_CC:
                                  case SET_JUSTIFYMODE_CC:
                                  case JUSTIFY_TEXTFIELD_CC:
                                  case SET_PRESCOLOR_CC:
                                  case SET_PRESPAGESIZE_CC:
                                  case SET_PPAGEMEDIA_CC:
                                  case SET_PRINTERSETUP_CC:
                                  case RELEASE_LEFT_MARGIN_CC:
                                     len     -= (count+ 2);
                                     offset  += (count+ 2);
                                     fRC = true;
                                    break;

                                  default:
                                     fRC = false;
                                     fDone = true;
                                     break;

                               } /* endswitch */

                               break;
                            } /* end CASE D2 */
                            /*************************************************
                            * CASE D3:                                       *
                            *     casentry type                              *
                            *     CASE SetTextOrientataion                   *
                            *       Set as SCS data                          *
                            *       Advance past this command                *
                            *     DEFAULT                                    *
                            *       Not SCS data and we are done             *
                            *       BREAK                                    *
                            *     ENDCASE                                    *
                            *   BREAK                                        *
                            *************************************************/

                            case DOGTHREE:
                            {
                               switch (buf[offset+3])    // switch on the type
                               {
                                  case STO_CC:
                                  {
                                     len     -= (count+ 2);
                                     offset  += (count+ 2);
                                     fRC = true;
                                     break;
                                  }

                                  default:
                                  {
                                     fRC = false;
                                     fDone = true;
                                  }
                               } /* endswitch */
                               break;
                            } /* end CASE D3 */
                            /*************************************************
                            * CASE D4:                                       *
                            *     casentry type                              *
                            *     CASE Begin Under Score                     *
                            *     CASE End Under Score                       *
                            *     CASE Begin Over Strike                     *
                            *     CASE End Over Strike                       *
                            *       Set as SCS data.                         *
                            *       advance past this command                *
                            *     DEFAULT                                    *
                            *       NOT SCS and we are done                  *
                            *       BREAK                                    *
                            *     ENDCASE                                    *
                            *   BREAK                                        *
                            *************************************************/

                            case DOGFOUR:
                            {
                               switch (buf[offset+3])    // switch on the type
                               {
                                  case BUS_CC:
                                  case EUS_CC:
                                  case BOS_CC:
                                  case EOS_CC:
                                  {
                                     len     -= (count+ 2);
                                     offset  += (count+ 2);
                                     fRC = true;
                                     break;
                                  }

                                  default:
                                  {
                                     fRC = false;
                                     fDone = true;
                                     break;
                                  }
                               } /* endswitch */
                               break;
                            } /* end CASE D4 */

                            /*************************************************
                            * CASE FD:                                       *
                            *     casentry type                              *
                            *     CASE Define Grid Lines                     *
                            *     CASE Set IGC Type                          *
                            *     CASE Set Font Size Scaling                 *
                            *     CASE Set Presentation of Control Char      *
                            *       Set as SCS data.                         *
                            *       advance past this command                *
                            *     DEFAULT                                    *
                            *       NOT SCS and we are done                  *
                            *       BREAK                                    *
                            *     ENDCASE                                    *
                            *   BREAK                                        *
                            *************************************************/

                            case FOXDOG:
                            {
                               switch (buf[offset+3])    // switch on the type
                               {
                                  case DGL_CC:
                                  case SIT_CC:
                                  case SFSS_CC:
                                  case SPCC_CC:
                                  {
                                     len     -= (count+ 2);
                                     offset  += (count+ 2);
                                     fRC = true;
                                     break;
                                  }

                                  default:
                                  {
                                     fRC = false;
                                     fDone = true;
                                     break;
                                  }
                               } /* endswitch */
                               break;
                            } /* end CASE FD */


                            /*************************************************
                            * DEFAULT:                                       *
                            *   NOT SCS and we are done                      *
                            *   BREAK                                        *
                            *************************************************/

                            default:
                               fRC = false;
                               fDone = true;
                            break;
                         } /* end of switch */
                      } else {
                         fDone = true;
                      }
                      break;
                    /**********************************************************
                    * CASE ASCII Transparency                                 *
                    * CASE SCS transparent:                                   *
                    **********************************************************/
                    case ASCII_TRNSPRNT_CC:
                    case TRANSPARENT_CC:
                      if ((len >= 2) &&
                          (len  >= (buf[offset + 1] + 2) ) )

                      {
                         len     -=  buf[offset + 1] + 2;
                         offset  +=  buf[offset + 1] + 2;
                         fRC = true;
                      } else {
                         fDone = true;
                      } /* endif */
                      break;


                   /**********************************************************
                   * Any other control codes are unsupported.                *
                   **********************************************************/

                   /**********************************************************
                   * DEFAULT:                                                *
                   *   Not SCS data, we are done.                            *
                   *   BREAK                                                 *
                   **********************************************************/

                   default:
                     fRC = false;
                     fDone = true;
                     break;
                }


            } else {
                /*************************************************************/
                /* byte is >= 0x40 and is just a regular character, so       */
                /* goto next byte   - we can't say it is SCS data unless we  */
                /* have some SCS commands - EBCDIC text only will not show up*/
                /* as SCS data.                                              */
                /*************************************************************/
                len--;
                offset++;
            }
        }

        return fRC;
    }  // dataIsSCS()

    /** sniffs the data and tells you what it appears to be.
      * @param buf The data buffer.
      * @param offset Offset into the data buffer to start at.
      * @param len The number of bytes to analyze.
      * @return A string with the server Printer Device Type
      *         value of what this data is most likely (*SCS,
      *          *AFPDS or *USERASCII).
      **/
    static String sniff(byte[] buf, int offset, int len)
    {
        String str = DT_USERASCII;
        if (dataIsAFP(buf, offset, len))
        {
            str = DT_AFPDS;
        } else {
            if (dataIsSCS(buf, offset, len))
            {
                str = DT_SCS;
            }
        }
        return str;
    }

    // bunch of constants used for breaking SCS data stream
        private static final byte ASCII_TRNSPRNT_CC               = (byte)0x03;
        private static final byte BACKSPACE_CC                    = (byte)0x16;
        private static final byte UNITBACKSPACE_CC                = (byte)0x1A;
        private static final byte BEL_CC                          = (byte)0x2F;
        private static final byte CARRIAGE_RETURN_CC              = (byte)0x0D;
        private static final byte ENABLE_PRESENTATION_CC          = (byte)0x14;
        private static final byte EXPANDED_BACKSPACE_CC           = (byte)0x36;
        private static final byte FORM_FEED_CC                    = (byte)0x0C;
        private static final byte GRAPHIC_ESCAPE_CC               = (byte)0x08;
        private static final byte HORIZONTAL_TAB_CC               = (byte)0x05;
        private static final byte INDENT_TAB_CC                   = (byte)0x39;
        private static final byte INDEX_RETURN_CC                 = (byte)0x33;
        private static final byte INHIBIT_PRESENTATION_CC         = (byte)0x24;
        private static final byte INTERCHANGE_RECORD_SEPARATOR_CC = (byte)0x1E;
        private static final byte LINE_FEED_CC                    = (byte)0x25;
        private static final byte NEW_LINE_CC                     = (byte)0x15;
        private static final byte NULL_CC                         = (byte)0x00;
        private static final byte PRESENTATION_POSITION_CC        = (byte)0x34;
        private static final byte REQUIRED_FORM_FEED_CC           = (byte)0x3A;
        private static final byte REQUIRED_NEW_LINE_CC            = (byte)0x06;
        private static final byte SPACE_CC                        = (byte)0x40;
        private static final byte REQUIRED_SPACE_CC               = (byte)0x41;
        private static final byte SET_ATTRIBUTE_CC                = (byte)0x28;
        private static final byte SHIFT_OUT_CC                    = (byte)0x0E;
        private static final byte SHIFT_IN_CC                     = (byte)0x0F;
        private static final byte SWITCH_CC                       = (byte)0x2A;
        private static final byte CSP_CC                          = (byte)0x2B;
        private static final byte SUBSTITUTE_CC                   = (byte)0x3F;
        private static final byte GRAPHIC_ERROR_ACTION_CC         = (byte)0xC8;
        private static final byte HORIZONTAL_FORMAT_CC            = (byte)0xC1;
        private static final byte LINE_DENSITY_CC                 = (byte)0xC6;
        private static final byte NUMERIC_SPACE_CC                = (byte)0xE1;
        private static final byte SUBSCRIPT_CC                    = (byte)0x38;
        private static final byte SUPERSCRIPT_CC                  = (byte)0x09;
        private static final byte SYLLABLE_HYPHEN_CC              = (byte)0xCA;
        private static final byte TRANSPARENT_CC                  = (byte)0x35;
        private static final byte VERTICAL_CHANNEL_SELECT_CC      = (byte)0x04;
        private static final byte VERTICAL_TAB_CC                 = (byte)0x0B;
        private static final byte WORD_UNDERSCORE_CC              = (byte)0x23;

        /******************************************************************************/
        /* the following commands all begin with 2BD1                                 */
        /******************************************************************************/
        private static final byte FONTCLASS                       = (byte)0xD1;
        private static final byte SCG_CC                          = (byte)0x01;
        private static final byte SCGL_CC                         = (byte)0x81;
        private static final byte SFG_CC                          = (byte)0x05;
        private static final byte BES_CC                          = (byte)0x8A;
        private static final byte EES_CC                          = (byte)0x8E;

        /******************************************************************************/
        /* the following set all begin with = (byte)0x2BD2 and here we define the type byte   */
        /******************************************************************************/
        private static final byte DOGTWO                          = (byte)0xD2;
        private static final byte STAB_CC                         = (byte)0x01;
        private static final byte JUSTIFY_TEXTFIELD_CC            = (byte)0x03;
        private static final byte SET_INDENTLV_CC                 = (byte)0x07;
        private static final byte SET_LINESPACE_CC                = (byte)0x09;
        private static final byte RELEASE_LEFT_MARGIN_CC          = (byte)0x0B;
        private static final byte SET_JUSTIFYMODE_CC              = (byte)0x0D;
        private static final byte SET_HORZMARGINS_CC              = (byte)0x11;
        private static final byte SET_LINEDISTANCE_CC             = (byte)0x15;
        private static final byte PRINT_DENSITY_CC                = (byte)0x29;
        private static final byte SET_PRESCOLOR_CC                = (byte)0x2D;
        private static final byte SET_PRESPAGESIZE_CC             = (byte)0x40;
        private static final byte SET_INITCOND_CC                 = (byte)0x45;
        private static final byte SET_PPAGEMEDIA_CC               = (byte)0x48;
        private static final byte SET_VERTMARGINS_CC              = (byte)0x49;
        private static final byte SET_PRINTERSETUP_CC             = (byte)0x4C;
        private static final byte SET_ERROR_ACTION_CC             = (byte)0x85;
        private static final byte VERTICAL_FORMAT_CC              = (byte)0xC2;


        /******************************************************************************/
        /* the following set all begin with = (byte)0x2BD3 and here we define the type byte   */
        /******************************************************************************/
        private static final byte DOGTHREE                        = (byte)0xD3;
        private static final byte STO_CC                          = (byte)0xF6;

        /******************************************************************************/
        /* the following set all begin with 0x2BD4 and here we define the type byte   */
        /******************************************************************************/
        private static final byte DOGFOUR                         = (byte)0xD4;
        private static final byte BUS_CC                          = (byte)0x0A;
        private static final byte EUS_CC                          = (byte)0x0E;
        private static final byte BOS_CC                          = (byte)0x72;
        private static final byte EOS_CC                          = (byte)0x76;

        /******************************************************************************/
        /* the following set all begin with 0x2BFD and here we define the type byte   */
        /******************************************************************************/
        private static final byte  FOXDOG                          = (byte)0xFD;
        private static final byte  DGL_CC                          = (byte)0x00;
        private static final byte  SIT_CC                          = (byte)0x01;
        private static final byte  SFSS_CC                         = (byte)0x02;
        private static final byte  SPCC_CC                         = (byte)0x03;

        /**********************************************************************
        * Structure for Presentation Position                                 *
        *  0x34ccvv - cc = code; vv = value;                                  *
        ***********************************************************************/
        private static final byte PP_STRUCT_LEN   = 0x03;      // length of PP structure
        /* defines for PPcode field                                          */
        private static final byte PPCODE_AHPP     = (byte)0xC0;  /* Absolute Horizontal        */
        private static final byte PPCODE_AVPP     = (byte)0xC4;  /* Absolute Veritcal          */
        private static final byte PPCODE_RHPP     = (byte)0xC8;  /* Relative Horizontal        */
        private static final byte PPCODE_RVPP     = (byte)0x4C;  /* Relative Veritcal          */

        /**********************************************************************
        * Structure for Set Attribute                                         *
        *   0x28ttvv   tt=type; vv = value                                    *
        **********************************************************************/
        private static final byte SA_STRUCT_LEN                  = 0x03;  // length of SA structure
        /* Defines for the type field                                        */
        private static final byte ATTR_RESET      = (byte)0x00;   /* Reset                      */
        private static final byte ATTR_COLOR      = (byte)0x42;   /* Set color                  */
        private static final byte ATTR_HILITE     = (byte)0x41;   /* Set highlight              */
        private static final byte ATTR_CHAR_SET   = (byte)0x43;   /* Set character set          */


}




