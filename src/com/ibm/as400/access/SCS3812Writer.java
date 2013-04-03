///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: SCS3812Writer.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.OutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;


/**
  * The SCS3812Writer class writes an SCS 3812 data stream to an output stream,
  * translating characters into bytes of the specified CCSID.
  * SCS3812Writer extends the SCS5219Writer and adds support for Bold (Emphasis),
  * fonts, duplex printing and text orientation.
  *
  * @see SCS5219Writer
  **/

 /* @A1C
  * Moved Bold support from 5219.
  */

public class SCS3812Writer extends SCS5219Writer
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


    /** Constant value for duplex printing.  **/
    public static final int DUPLEX_DUPLEX = 2;
    /** Constant value for simplex printing. **/
    public static final int DUPLEX_SIMPLEX = 1;
    /** Constant value for tumble duplex printing.  **/
    public static final int DUPLEX_TUMBLE = 3;


    /** Constant value for a Courier, 10 pitch font. **/
    public static final int FONT_COURIER_10 = 0;
    /** Constant value for a Courier, 12 pitch font.  **/
    public static final int FONT_COURIER_12 = 1;
    /** Constant value for a Courier, 15 pitch font.  **/
    public static final int FONT_COURIER_15 = 2;
    /** Constant value for a Courier, 17 pitch font.  **/
    public static final int FONT_COURIER_17 = 3;
    /** Constant value for a Courier, 5 pitch font.  **/
    public static final int FONT_COURIER_5 = 4;
    /** Constant value for a Courier, bold, 10 pitch font.  **/
    public static final int FONT_COURIER_BOLD_10 = 5;
    /** Constant value for a Courier, bold, 17 pitch font.  **/
    public static final int FONT_COURIER_BOLD_17 = 6;
    /** Constant value for a Courier, bold, 5 pitch font. **/
    public static final int FONT_COURIER_BOLD_5 = 7;
    /** Constant value for a Courier, italic, 10 pitch font.  **/
    public static final int FONT_COURIER_ITALIC_10 = 8;
    /** Constant value for a Courier, italic, 12 pitch font.  **/
    public static final int FONT_COURIER_ITALIC_12 = 9;
    /** Constant value for a Gothic, 10 pitch font.  **/
    public static final int FONT_GOTHIC_10 = 10;
    /** Constant value for a Gothic, 12 pitch font.  **/
    public static final int FONT_GOTHIC_12 = 11;
    /** Constant value for a Gothic, 13 pitch font.  **/
    public static final int FONT_GOTHIC_13 = 12;
    /** Constant value for a Gothic, 15 pitch font.  **/
    public static final int FONT_GOTHIC_15 = 13;
    /** Constant value for a Gothic, 20 pitch font.  **/
    public static final int FONT_GOTHIC_20 = 14;
    /** Constant value for a Gothic, 27 pitch font.  **/
    public static final int FONT_GOTHIC_27 = 15;
    /** Constant value for a Gothic, bold, 10 pitch font.  **/
    public static final int FONT_GOTHIC_BOLD_10 = 16;
    /** Constant value for a Gothic, bold, 12 pitch font.  **/
    public static final int FONT_GOTHIC_BOLD_12 = 17;
    /** Constant value for a Gothic, italic, 12 pitch font.  **/
    public static final int FONT_GOTHIC_ITALIC_12 = 18;
    /** Constant value for a letter Gothic, 12 pitch font.  **/
    public static final int FONT_LETTER_GOTHIC_12 = 19;
    /** Constant value for a letter Gothic, bold, 12 pitch font.  **/
    public static final int FONT_LETTER_GOTHIC_BOLD_12 = 20;
    /** Constant value for an OCR A, 10 pitch font.  **/
    public static final int FONT_OCR_A_10 = 21;
    /** Constant value for an OCR B, 10 pitch font.  **/
    public static final int FONT_OCR_B_10 = 22;
    /** Constant value for an Oritor, 10 pitch font.  **/
    public static final int FONT_ORATOR_10 = 23;
    /** Constant value for an Oritor, bold, 10 pitch font.  **/
    public static final int FONT_ORATOR_BOLD_10 = 24;
    /** Constant value for a Prestige, 10 pitch font.  **/
    public static final int FONT_PRESTIGE_10 = 25;
    /** Constant value for a Prestige, 12 pitch font.  **/
    public static final int FONT_PRESTIGE_12 = 26;
    /** Constant value for a Prestige, 15 pitch font.  **/
    public static final int FONT_PRESTIGE_15 = 27;
    /** Constant value for a Prestige, bold, 12 pitch font.  **/
    public static final int FONT_PRESTIGE_BOLD_12 = 28;
    /** Constant value for a Prestige, italic, 12 pitch font.  **/
    public static final int FONT_PRESTIGE_ITALIC_12 = 29;
    /** Constant value for a Roman, 10 pitch font.  **/
    public static final int FONT_ROMAN_10 = 30;
    /** Constant value for a script, 12 pitch font.  **/
    public static final int FONT_SCRIPT_12 = 31;
    /** Constant value for a Serif, 10 pitch font.  **/
    public static final int FONT_SERIF_10 = 32;
    /** Constant value for a Serif, 12 pitch font.  **/
    public static final int FONT_SERIF_12 = 33;
    /** Constant value for a Serif, 15 pitch font **/
    public static final int FONT_SERIF_15 = 34;
    /** Constant value for a Serif, bold, 12 pitch font.  **/
    public static final int FONT_SERIF_BOLD_12 = 35;
    /** Constant value for a Serif, italic, 10 pitch font.  **/
    public static final int FONT_SERIF_ITALIC_10 = 36;
    /** Constant value for a Serif, italic, 12 pitch font.  **/
    public static final int FONT_SERIF_ITALIC_12 = 37;


    private static final byte [] SCG = {0x2B, (byte)0xD1, 0x06,
                                        0x01, 0x00, 0x00, 0x00, 0x00};
    private static final byte [] SFID = {0x2B, (byte)0xD1, 0x07,
                                         0x05, 0x00, 0x00, 0x00, 0x00,
                                         0x01};
    private static final byte [] STO = {0x2B, (byte)0xD3, 0x06,
                                        (byte)0xF6, 0x00, 0x00, 0, 0};
    private static final byte [] BES = {0x2B, (byte)0xD1, 0x03, //@A1M - from 5219 class
                                        (byte)0x8A, 0x00};
    private static final byte [] EES = {0x2B, (byte)0xD1, 0x03, //@A1M - from 5219 class
                                        (byte)0x8E, 0x00};


    private byte []      orientation_ = {(byte)0xFF, (byte)0xFF};
    private int          font_ = 0;
    private boolean      bold_ = false;         //@A1M - from 5219 class

    /**
     * Constructs a SCS3812Writer.  The default encoding will be used.
     *
     * @param out An OutputStream.
     *
     * @deprecated Replaced by SCS3812Writer(OutputStream, int, AS400).
        Any SCS3812Writer object that is created without
        specifying an AS400 system object on its constructor may
        not behave as expected in certain environments.        
     **/
    public SCS3812Writer(OutputStream out)
    {
        super(out);
    }


    /**
     * Constructs a SCS3812Writer.
     *
     * @param out An OutputStream.
     * @param ccsid The name of the target CCSID to be used.
     *
     * @exception UnsupportedEncodingException If <I>ccsid</I> is invalid.
     * @deprecated Replaced by SCS3812Writer(OutputStream, int, AS400).
        Any SCS3812Writer object that is created without
        specifying an AS400 system object on its constructor may
        not behave as expected in certain environments.        
     **/
    public SCS3812Writer(OutputStream out,
                         int          ccsid)
          throws UnsupportedEncodingException
    {
       super(out, ccsid);
    }


    // @B1A
    /**
     * Constructs a SCS3812Writer.
     *
     * @param out An OutputStream.
     * @param ccsid The name of the target CCSID to be used.
     * @param system The system
     *
     * @exception UnsupportedEncodingException If <I>ccsid</I> is invalid.
     **/
    public SCS3812Writer(OutputStream out,
                         int          ccsid,
                         AS400       system)
          throws UnsupportedEncodingException
    {
       super(out, ccsid, system);
    }


    /**
     * Constructs a SCS3812Writer.
     *
     * @param out An OutputStream.
     * @param encoding The name of the target encoding to be used.
     *
     * @exception UnsupportedEncodingException If <I>encoding</I> is invalid.
     * @deprecated Replaced by SCS3812Writer(OutputStream, int, AS400).
        Any SCS3812Writer object that is created without
        specifying an AS400 system object on its constructor may
        not behave as expected in certain environments.        
     **/
    public SCS3812Writer(OutputStream out,
                         String       encoding)
           throws UnsupportedEncodingException
    {
        super(out, encoding);
    }


    /** Ends current page.
      *
      * @exception IOException If an error occurs while communicating
      *   with the system.
      **/
    public void endPage()
           throws IOException
    {
       /* Don't want to leave the printer in a bad state.  If bold   */
       /* is on, turn it off before ending the page. The superclass (5219)*/
       /* will handle the underline command. */
       if (bold_ == true) addToBuffer(EES);

       super.endPage();
    }

     /*  Sends out controls to initialize the start of a page.
      *
      * @exception IOException If an error occurs while communicating with the
      *   system.
      */
    void initPage()
         throws IOException
    {
       super.initPage();
       setBold(bold_);               //@A1M (from 5219 class)
       sendSTO();                    //@A1M (from first line of method)

       if (fontOrCPI == FONT_) setFont(font_);
    }


    /*  Outputs the Set Text Orientation command.
      *
      */
    private void sendSTO()
            throws IOException
    {
       byte [] cmd = STO;

       cmd[6] = orientation_[0];
       cmd[7] = orientation_[1];

       addToBuffer(cmd);
    }


    /** Sets bold text on or off.
      *
      * @param bold If true, turns bold on; if false, turns bold off.
      *4
      * @exception IOException If an error occurs while communicating
      *   with the system.
      **/
    public void setBold(boolean bold)
           throws IOException
    {
       bold_ = bold;
       if (pageStarted_ == true) {
          if (bold_ == true) {
             addToBuffer(BES);
          } else {
             addToBuffer(EES);
          } /* endif */
       } /* endif */
    }

    /** Sets duplex printing mode.  This is set once at the start of each
      * page.  Changes in duplex print mode will not take effect until the
      * next new page.
      *
      * @param duplex  The duplex setting.  Valid values are DUPLEX_DUPLEX,
      *   DUPLEX_SIMPLEX, or DUPLEX_TUMBLE.
      *
      **/
    public void setDuplex(int duplex)
    {
        switch (duplex) {
            case DUPLEX_DUPLEX:
            case DUPLEX_SIMPLEX:
            case DUPLEX_TUMBLE:
                plex_ = (byte)duplex;
                break;
            default:
                String arg = "Duplex (" + String.valueOf(duplex) + ")";
                throw new ExtendedIllegalArgumentException(arg, 2);
        }
    }


    /** Sets the current font.  This method will override the setCPI function.
      *
      * @param font The current font.  Constant values are defined for the
      *   valid values.
      *
      * @exception IOException If an error occurs while communicating
      *   with the system.
      **/
    public void setFont(int font)
           throws IOException
    {
        byte [] cmd = SFID;
        SCSFontData fd = new SCSFontData();
        int[] fontIDs = fd.fontIDs; 
        /* Make sure font value is valid                             */
        if ((font < 0) || (font > fontIDs.length))
        {
            String arg = "Font (" + String.valueOf(font) + ")";
            throw new ExtendedIllegalArgumentException(arg, 2);
        }

        font_ = font;
        fontOrCPI = FONT_;

        if (pageStarted_ == false) initPage();  //@A2C - was "true"
   //     {                                     //@A2D

        /* Get font ID and width                                 */
        int fgid = fd.fontIDs[font];
        int width = fd.fontWidths[font];

        /* Update command string.                                */
        cmd[4] = (byte)(fgid >> 8);
        cmd[5] = (byte)fgid;
        cmd[6] = (byte)(width >> 8);
        cmd[7] = (byte)width;

        addToBuffer(cmd);
   //     }                                     //@A2D
    }


    /** Sets text orientation.  This is set once at the start of each page.
      * Changes in text orientation will not take effect until the next
      * new page.
      *
      * @param orientation The text orientation on the paper.  Valid values
      *   are 0, 90, 180 and 270.
      *
      **/
    public void setTextOrientation(int orientation)
    {
       switch (orientation) {
       case 0:
          orientation_[0] = 0x00;
          orientation_[1] = 0x00;
          break;
       case 90:
          orientation_[0] = 0x2D;
          orientation_[1] = 0x00;
          break;
       case 180:
          orientation_[0] = 0x5A;
          orientation_[1] = 0x00;
          break;
       case 270:
          orientation_[0] = (byte)0x87;
          orientation_[1] = 0x00;
          break;
       default:
          String arg = "Orientation (" + String.valueOf(orientation) + ")";
          throw new ExtendedIllegalArgumentException(arg, 2);
       } /* endswitch */
    }

}
