///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: SCS5219Writer.java
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
  * The SCS5219Writer class writes an SCS 5219 data stream to an output stream,
  * translating characters into bytes of the specified CCSID.
  * SCS5219Writer extends SCS5224Writer and adds support for left margin,
  * underline, form type (paper or envelope), form size, print quality, code page,
  * character set, source drawer number, and destination drawer number.
  *
  * @see SCS5224Writer
**/

 /* @A1C 
  * Moved AHPP/AVPP to 5256 class
  * Moved setBold to 3812 class
  * Added setCodePage method that uses SCG command
  */

public class SCS5219Writer extends SCS5224Writer
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


    /** Constant value for draft print quality.  **/
    public static final int QUALITY_DRAFT = 1;
    /** Constant value for near letter print quality.  **/
    public static final int QUALITY_NEAR_LETTER = 2;

    private static final byte [] BUS = {0x2B, (byte)0xD4, 0x03,
                                        0x0A, 0x01};
    private static final byte [] EUS = {0x2B, (byte)0xD4, 0x02,
                                        0x0E};
    private static final byte [] SPPS = {0x2B, (byte)0xD2, 0x06,
                                         0x40, 0, 0, 0, 0};
    private static final byte [] SCD = {0x2B, (byte)0xD2, 0x04, 0x29,
                                        0x00, 0x00};
    private static final byte [] PPM = {0x2B, (byte)0xD2, 0x0A,
                                        0x48, 0x00, 0x00, 0, 0,
                                        0x00, 0, 0, 0};
    private static final byte [] SHM = {0x2B, (byte)0xD2, 0x04, 0x11,
                                        0, 0};
    private static final byte [] SCG = {0x2B, (byte)0xD1, 0x06,
                                        0x01, 0x00, 0x00, 0x00, 0x00};   //@A1A


    private boolean      underline_ = false;
    private double       paperWidth = 8.5;
    private double       paperLength = 11.0;
    private byte         paper = 0x01;
    private byte         sourceDrawer = 0x01;
    private byte         destinationDrawer = 0x01;
    private byte         quality_ = QUALITY_NEAR_LETTER;       //@A2C - was 0
    private double       leftMargin_ = 0.0;
    private int          codepage_ = 0;                      //@A1A
    private int          charset_ = 0;                      //@A1A

    /* Plex (simplex, duplex, tumble) is a parameter of the Set
     * Presentation Media command that is ignored by the 5219.
     * It is implemented in the 3812 printer.
     */
    byte         plex_ = 0;         //@A2C - was 1


    /**
     * Constructs a SCS5219Writer.  The default encoding will be used.
     *
     * @param out An OutputStream.
     *
     * @deprecated Replaced by SCS5219Writer(OutputStream, int, AS400).
        Any SCS5219Writer object that is created without
        specifying an AS400 system object on its constructor may
        not behave as expected in certain environments.        
     **/
    public SCS5219Writer(OutputStream out)
    {
        super(out);
    }


    /**
     * Constructs a SCS5219Writer.
     *
     * @param out An OutputStream.
     * @param ccsid The name of the target CCSID to be used.
     *
     * @exception UnsupportedEncodingException If <I>ccsid</I> is invalid.
     * @deprecated Replaced by SCS5219Writer(OutputStream, int, AS400).
        Any SCS5219Writer object that is created without
        specifying an AS400 system object on its constructor may
        not behave as expected in certain environments.        
     **/
    public SCS5219Writer(OutputStream out,
                         int          ccsid)
          throws UnsupportedEncodingException
    {
       super(out, ccsid);
    }


    // @B1A
    /**
     * Constructs a SCS5219Writer.
     *
     * @param out An OutputStream.
     * @param ccsid The name of the target CCSID to be used.
     * @param system The system.
     *
     * @exception UnsupportedEncodingException If <I>ccsid</I> is invalid.
     **/
    public SCS5219Writer(OutputStream out,
                         int          ccsid,
                         AS400        system)
          throws UnsupportedEncodingException
    {
       super(out, ccsid, system);
    }


    /**
     * Constructs a SCS5219Writer.
     *
     * @param out An OutputStream.
     * @param encoding The name of the target encoding to be used.
     *
     * @exception UnsupportedEncodingException If <I>encoding</I> is invalid.
     * @deprecated Replaced by SCS5219Writer(OutputStream, int, AS400).
        Any SCS5219Writer object that is created without
        specifying an AS400 system object on its constructor may
        not behave as expected in certain environments.        
     **/
    public SCS5219Writer(OutputStream out,
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
       /* Don't want to leave the printer in a bad state.  If underline is on,
       turn it off before ending the page. */
       if (underline_ == true) addToBuffer(EUS);

       super.endPage();
    }


    
     /*  Sends out controls to initialize the start of a page.
      *
      * @exception IOException If an error occurs while communicating
      *   with the system.
      */
    void initPage()
         throws IOException
    {
       super.initPage();

       sendSPPS();
       sendPPM();
       setLeftMargin(leftMargin_);
       setUnderline(underline_);

       if(codepage_ != 0)
       {
          // The user has specified a new character set and/or codepage.
          // This will override the SCGL command done by the superclass
          // initPage() method.
          setCodePage(codepage_, charset_);
       }
    }


   /*  Outputs the Page Presentation Media command.
     *
     */
   private void sendPPM()
           throws IOException
   {
      byte [] cmd = PPM;

      cmd[6] = paper;
      cmd[7] = sourceDrawer;
      cmd[9] = destinationDrawer;
      cmd[10] = quality_;
      cmd[11] = plex_;

      addToBuffer(cmd);

      if(plex_ != 0)            //@A2A
      {
        // This is to fix a duplex problem in IPDS and some newer SCS printers.  Because
        // the system always repeats page formatting commands at the top of each
        // page the PPM command is repeated w/ the last specified value of plex_.
        // If plex_ is non-zero, the duplex mode gets reset to agree w/ plex_ at
        // the start of each page, thereby setting each page to the first of two
        // duplexed pages.  So, duplex never happens.  To fix it, we send another
        // PPM command w/ plex_ = zero which means "keep current duplex setting".
         plex_ = 0;             //@A2A
         cmd[11] = 0;           //@A2A
         addToBuffer(cmd);      //@A2A
      }
   }


    /*  Outputs the Set Presentation Page Size command.
      *
      */
    private void sendSPPS()
            throws IOException
    {
       int width = (int)(paperWidth * 1440.0);
       int length = (int)(paperLength * 1440.0);
       byte [] cmd = SPPS;

       cmd[4] = (byte)(width >> 8);
       cmd[5] = (byte)width;
       cmd[6] = (byte)(length >> 8);
       cmd[7] = (byte)length;

       addToBuffer(cmd);
    }


    /** Sends Set GCGID through GCID (SCG) command to set the code page
      * and character set.  This method must be called before the first
      * character is printed.  Invalid values of codepage and
      * charset may cause a printer exception.
      *
      * @param codepage The code page to be set.
      *
      * @param charset The new character set.
      *
      * @exception IOException If an error occurs while communicating
      *   with the system.
      **/
    public void setCodePage(int codepage, int charset)              //@A1A
         throws IOException
    {
        byte [] cmd = SCG;

        if((codepage != 0) && (charset != 0))
        {
            codepage_ = codepage;
            charset_ = charset;

            cmd[4] = (byte)(charset >> 8);
            cmd[5] = (byte)charset;
            cmd[6] = (byte)(codepage >> 8);
            cmd[7] = (byte)codepage;
            addToBuffer(cmd);
        }
    }


    /** Sets characters per inch.  All following text will be
      * in the set pitch.
      *
      * @param cpi The characters per inch.  Valid values are 10, 12
      *   and 15.
      *
      * @exception IOException If an error occurs while communicating
      *   with the system.
      **/
    public void setCPI(int cpi)
           throws IOException
    {
       switch (cpi) {
       case 10:
          CPI = 10;
          break;
       case 12:
          CPI = 12;
          break;
       case 15:
          CPI = 15;
          break;
       default:
          String arg = "CPI (" + String.valueOf(cpi) + ")";
          throw new ExtendedIllegalArgumentException(arg, 2);
       } /* endswitch */

       fontOrCPI = CPI_;
       if (pageStarted_ == false) initPage();   //@A2C - was "true"
                                                // added the initPage() call.
       byte [] cmd = SCD;
       cmd[cmd.length-1] = CPI;
       addToBuffer(cmd);
    }


    /** Sets destination drawer number.  This is set once at the start of each page.
      * Changes in the destination drawer will not take effect until the next
      * new page.
      *
      * @param drawer The number of the drawer to select.  Valid values are 1 to
      *   255.
      *
      **/
    public void setDestinationDrawer(int drawer)
    {
       if ((drawer < 1) || (drawer > 255)) {
          String arg = "Drawer (" + String.valueOf(drawer) + ")";
          throw new ExtendedIllegalArgumentException(arg, 2);
       } else {
          destinationDrawer = (byte)drawer;
       } /* endif */
    }


    /** Sets envelope size.  This is set once at the start of each page.
      * Changes in the envelope size will not take effect until the next
      * new page.
      *
      * @param width The envelope width in inches.  Valid values are 0.1 to
      *   14.0.
      * @param length The envelope width in inches.  Valid values are 0.1 to
      *   14.0.
      *
      **/
    public void setEnvelopeSize(double width,
                                double length)
    {
       if ((width < 0.1) || (width > 14.0)) {
          String arg = "Width (" + String.valueOf(width) + ")";
          throw new ExtendedIllegalArgumentException(arg, 2);
       }

       if ((length < 0.1) || (length > 14.0)) {
          String arg = "Length (" + String.valueOf(length) + ")";
          throw new ExtendedIllegalArgumentException(arg, 2);
       }

       paper = 0x02;              /* Set flag for envelope           */
       paperWidth = width;
       paperLength = length;
    }


    /** Sets the left margin.  Call this method only while at a line boundary
      * or a printer exception will occur.
      *
      * @param leftMargin The margin distance from the left paper edge in
      *   inches.  Valid values are 0.0 to 14.0.
      *
      * @exception IOException If an error occurs while communicating
      *   with the system.
      **/
    public void setLeftMargin(double leftMargin)
           throws IOException
    {
        if ((leftMargin < 0.0) || (leftMargin > 14.0)) {
            String arg = "Left margin (" + String.valueOf(leftMargin) + ")";
            throw new ExtendedIllegalArgumentException(arg, 2);
        }

        leftMargin_ = leftMargin;
        if (pageStarted_ == false) initPage();   //@A2C - was "true", added initPage()

        byte [] cmd = SHM;
        int margin1440 = (int)(leftMargin * 1440.0);
        cmd[4] = (byte)(margin1440 >> 8);
        cmd[5] = (byte)margin1440;
        addToBuffer(cmd);
    }


    /** Sets paper size.  This is set once at the start of each page.
      * Changes in the paper size will not take effect until the next
      * new page.
      *
      * @param width The paper width in inches.  Valid values are 0.1 to
      *   14.0.
      * @param length The paper length in inches.  Valid values are 0.1 to
      *   14.0.
      *
      **/
    public void setPaperSize(double width,
                             double length)
    {
       if ((width < 0.1) || (width > 14.0)) {
          String arg = "Width (" + String.valueOf(width) + ")";
          throw new ExtendedIllegalArgumentException(arg, 2);
       }

       if ((length < 0.1) || (length > 14.0)) {
          String arg = "Length (" + String.valueOf(length) + ")";
          throw new ExtendedIllegalArgumentException(arg, 2);
       }

       paper = 0x01;              /* Set flag for paper              */
       paperWidth = width;
       paperLength = length;
    }


    /** Sets quality printing.  This is set once at the start of each page.
      * Changes in quality will not take effect until the next
      * new page.
      *
      * @param quality  The type of quality printing.  Valid values are
      *   QUALITY_DRAFT and QUALITY_NEAR_LETTER.
      *
      **/
    public void setQuality(int quality)
    {
        switch (quality) {
            case QUALITY_DRAFT:
            case QUALITY_NEAR_LETTER:
               quality_ = (byte)quality;
               break;
            default:
               String arg = "Quality (" + String.valueOf(quality) + ")";
               throw new ExtendedIllegalArgumentException(arg, 2);
        }
    }


    /** Sets source drawer number.  This is set once at the start of each page.
      * Changes in the source drawer will not take effect until the next
      * new page.
      *
      * @param drawer The number of the drawer to select.  Valid values are 1 to
      *   255.
      *
      **/
    public void setSourceDrawer(int drawer)
    {
       if ((drawer < 1) || (drawer > 255)) {
          String arg = "Drawer (" + String.valueOf(drawer) + ")";
          throw new ExtendedIllegalArgumentException(arg, 2);
       } else {
          sourceDrawer = (byte)drawer;
       } /* endif */
    }


    /** Sets underline on or off.
      *
      * @param ul If true, turns underline on; if false, turns underline off.
      *
      * @exception IOException If an error occurs while communicating
      *   with the system.
      **/
    public void setUnderline(boolean ul)
           throws IOException
    {
       underline_ = ul;
       if (pageStarted_ == true) {
          if (underline_ == true ) {
             addToBuffer(BUS);
          } else {
             addToBuffer(EUS);
          } /* endif */
       } /* endif */
    }

}
