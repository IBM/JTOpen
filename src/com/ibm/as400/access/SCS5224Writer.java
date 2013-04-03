///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: SCS5224Writer.java
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
  * The SCS5224Writer class writes an SCS 5224 data stream to an output stream,
  * translating characters into bytes of the specified CCSID.
  * SCS5224Writer extends the SCS5256Writer and adds support for
  * setting the characters per inch (CPI) and lines per inch (LPI).
  *
  * @see SCS5256Writer
**/

/* @A1C
 * Fix setCPI() and setLPI() so that they call initPage() if the page has not
 * been started yet.
 */

public class SCS5224Writer extends SCS5256Writer
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


    private static final byte [] SCD = {0x2B, (byte)0xD2, 0x04, 0x29,
                                        0x00, 0x00};
    private static final byte [] SCGL = {0x2B, (byte)0xD1, 0x03,
                                        (byte)0x81, (byte)0xFF};
    private static final byte [] SLD = {0x2B, (byte)0xC6, 0x02, 0x00};

    private byte         LPI = 0x06;

    /* Because a font can override the CPI, and vice versa, we need  */
    /* to keep a flag indicating which command to use to initialize  */
    /* the page.                                                     */
    static final boolean FONT_ = false;
    static final boolean CPI_ = true;
    boolean fontOrCPI = CPI_;
    byte    CPI = 0x0A;


    /**
     * Constructs a SCS5224Writer.  The default encoding will be used.
     *
     * @param out An OutputStream.
     *
     * @deprecated Replaced by SCS5224Writer(OutputStream, int, AS400).
        Any SCS5224Writer object that is created without
        specifying an AS400 system object on its constructor may
        not behave as expected in certain environments.        
     **/
    public SCS5224Writer(OutputStream out)
    {
        super(out);
    }


    /**
     * Constructs a SCS5224Writer.
     *
     * @param out An OutputStream.
     * @param ccsid The name of the target CCSID to be used.
     *
     * @exception UnsupportedEncodingException If <I>ccsid</I> is invalid.
     * @deprecated Replaced by SCS5224Writer(OutputStream, int, AS400).
        Any SCS5224Writer object that is created without
        specifying an AS400 system object on its constructor may
        not behave as expected in certain environments.        
     **/
    public SCS5224Writer(OutputStream out,
                         int          ccsid)
          throws UnsupportedEncodingException
    {
       super(out, ccsid);
    }


    // @B1A
    /**
     * Constructs a SCS5224Writer.
     *
     * @param out An OutputStream.
     * @param ccsid The name of the target CCSID to be used.
     * @param system The system.
     *
     * @exception UnsupportedEncodingException If <I>ccsid</I> is invalid.
     **/
    public SCS5224Writer(OutputStream out,
                         int          ccsid,
                         AS400 system)
          throws UnsupportedEncodingException
    {
       super(out, ccsid, system);
    }


    /**
     * Constructs a SCS5224Writer.
     *
     * @param out An OutputStream.
     * @param encoding The name of the target encoding to be used.
     *
     * @exception UnsupportedEncodingException If <I>encoding</I> is invalid.
     * @deprecated Replaced by SCS5224Writer(OutputStream, int, AS400).
        Any SCS5224Writer object that is created without
        specifying an AS400 system object on its constructor may
        not behave as expected in certain environments.        
     **/
    public SCS5224Writer(OutputStream out,
                         String       encoding)
           throws UnsupportedEncodingException
    {
        super(out, encoding);
    }


    
     /*  Sends out controls to initialize the start of a page.
      *
      */
    void initPage()
         throws IOException
    {
       super.initPage();

       if (fontOrCPI == CPI_) setCPI(CPI);
       setLPI(LPI);
       setCodePage();
    }


    /* Sends SCGL command to set the code page
     *
     */
    void setCodePage()
         throws IOException
    {
        byte [] cmd = SCGL;
        int ccsid = getCcsid();
        SCSFontData fd = new SCSFontData();
        int[] codePage = fd.codePage; 
        int length = codePage.length;

        /* Loop through the list of code pages looking for a match.  */
        /* If one is found, add the corresponding ID to the command. */
        /* If no match is found, the default ID is sent.             */
        for (int i = 0; i < length; i++)
        {
            if (ccsid == codePage[i])
            {
                cmd[cmd.length-1] = fd.codePageID[i];
                break;
            }
        }

        addToBuffer(cmd);
    }


    /** Sets characters per inch.  All following text will be
      * in the set pitch.
      *
      * @param cpi The characters per inch.  Valid values are 10 and 15.
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
       case 15:
          CPI = 15;
          break;
       default:
          String arg = "CPI (" + String.valueOf(cpi) + ")";
          throw new ExtendedIllegalArgumentException(arg, 2);
       } /* endswitch */

       fontOrCPI = CPI_;

//       if (pageStarted_ == true) {                  @A1D
       if (pageStarted_ == false)                   //@A1A
          initPage();                               //@A1A
       else                                         //@A1A
       {
           byte [] cmd = SCD;
           cmd[cmd.length-1] = CPI;
           addToBuffer(cmd);
       }
    }


    /** Sets lines per inch.  All following lines will be in the set
      * lines per inch.
      *
      * @param lpi The lines per inch.  Valid values are 4, 6, 8, 9, and
      *   12.
      *
      * @exception IOException If an error occurs while communicating
      *   with the system.
      **/
    public void setLPI(int lpi)
           throws IOException
    {
       byte LPI1440;

       switch (lpi) {
       case 4:
          LPI = 4;
          LPI1440 = 0x12;
          break;
       case 6:
          LPI = 6;
          LPI1440 = 0x0C;
          break;
       case 8:
          LPI = 8;
          LPI1440 = 0x09;
          break;
       case 9:
          LPI = 9;
          LPI1440 = 0x08;
          break;
       case 12:
          LPI = 12;
          LPI1440 = 0x06;
          break;
       default:
          String arg = "LPI (" + String.valueOf(lpi) + ")";
          throw new ExtendedIllegalArgumentException(arg, 2);
       } /* endswitch */

//       if (pageStarted_ == true) {            @A1D
       if (pageStarted_ == false)             //@A1A
          initPage();                         //@A1A
       else                                   //@A1A
       {
           byte [] cmd = SLD;
           cmd[cmd.length-1] = LPI1440;
           addToBuffer(cmd);
       }
    }
}
