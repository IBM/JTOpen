///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: SCS5553Writer.java
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
  * The SCS5553Writer class writes an SCS 5553 data stream to an output stream,
  * translating characters into bytes of the specified CCSID.
  * SCS5553Writer extends the SCS5224Writer and adds support for presentation of
  * control characters, character rotation, grid lines, and font scaling.
  *
  * The 5553 is a DBCS data stream.
  *
  * @see SCS5224Writer
  **/

 /* 
  * This class now subclasses SCS5224Writer instead of SCS5219Writer
  * Added Set Presentation of Control Character (SPPC) which required
  * that initPage() be overridden.
  */


public class SCS5553Writer extends SCS5224Writer
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    /** Constant for normal character rotation. **/
    public static final int CHARACTER_NORMAL = 0;
    /** Constant for 270-degree character rotation. **/
    public static final int CHARACTER_ROTATED = 1;

    /** Constant for double horizontal font scaling.  **/
    public static final int SCALE_DOUBLE_HORIZONTAL = 0x20;
    /** Constant for regular font scaling.  **/
    public static final int SCALE_REGULAR = 0x10;


    /** Constant for all control characters take no printing positions **/
    public static final int SETCCP_NO_PRINT = 0;        //@A1A

    /** Constant for SO/SI are printed as A/N/K blank code (default) **/
    public static final int SETCCP_BLANK_CODE = 1;          //@A1A

    /** Constant for SO takes no position, SI takes 2 A/N/K blanks **/
    public static final int SETCCP_SI_BLANK = 2;            //@A1A

    private static final byte [] SCD = {0x2B, (byte)0xD2, 0x04, 0x29,
        0x00, 0x00};                      
    
    private static final byte [] DGL = {0x2B, (byte)0xFD, 0x00,
                                        0x00, 0x00, 0x00};
    private static final byte [] SFSS = {0x2B, (byte)0xFD, 0x04,
                                        0x02, 0x00, 0x00};
    private static final byte [] STO = {0x2B, (byte)0xD3, 0x04,
                                       (byte)0xF6, 0x00, 0x00};
    private static final byte [] SPPC = {0x2B, (byte)0xFD, 0x04,
                                        0x03, 0x00, 0x00}; //@A1A

    private int setCCP = SETCCP_BLANK_CODE;      //@A1A


    /**
     * Constructs a SCS5553Writer.  The default encoding will be used.
     *
     * @param out An OutputStream.
     *
     * @deprecated Replaced by SCS5553Writer(OutputStream, int, AS400).
        Any SCS5553Writer object that is created without
        specifying an AS400 system object on its constructor may
        not behave as expected in certain environments.        
     **/
    public SCS5553Writer(OutputStream out)
    {
        super(out);
    }


    /**
     * Constructs a SCS5553Writer.
     *
     * @param out An OutputStream.
     * @param ccsid The name of the target CCSID to be used.
     *
     * @exception UnsupportedEncodingException If <I>ccsid</I> is invalid.
     * @deprecated Replaced by SCS5553Writer(OutputStream, int, AS400).
        Any SCS5553Writer object that is created without
        specifying an AS400 system object on its constructor may
        not behave as expected in certain environments.        
     **/
    public SCS5553Writer(OutputStream out,
                         int          ccsid)
          throws UnsupportedEncodingException
    {
       super(out, ccsid);
    }


    // @B1A
    /**
     * Constructs a SCS5553Writer.
     *
     * @param out An OutputStream.
     * @param ccsid The name of the target CCSID to be used.
     * @param system The system.
     *
     * @exception UnsupportedEncodingException If <I>ccsid</I> is invalid.
     **/
    public SCS5553Writer(OutputStream out,
                         int          ccsid,
                         AS400        system)
          throws UnsupportedEncodingException
    {
       super(out, ccsid, system);
    }


    /**
     * Constructs a SCS5553Writer.
     *
     * @param out An OutputStream.
     * @param encoding The name of the target encoding to be used.
     *
     * @exception UnsupportedEncodingException If <I>encoding</I> is invalid.
     * @deprecated Replaced by SCS5553Writer(OutputStream, int, AS400).
        Any SCS5553Writer object that is created without
        specifying an AS400 system object on its constructor may
        not behave as expected in certain environments.        
     **/
    public SCS5553Writer(OutputStream out,
                         String       encoding)
           throws UnsupportedEncodingException
    {
        super(out, encoding);
    }
    
     /*  Sends out controls to initialize the start of a page.
      *
      */
    void initPage()                 //@A1A
         throws IOException
    {
       super.initPage();
       
       // Set the current or default control character presentation.
       setControlCharPresentation(setCCP);      //@A1A
    }

    /** Sets characters per inch.  All following text will be
     * in the set pitch.
     *
     * @param cpi The characters per inch.  Valid values are 10, 12, 13.3, 15, 18 and 20 but 
     * we won't implement 13.3 for now
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
      case 18:
        CPI = 18;
        break;
      case 20:
        CPI = 20;
        break;
      default:
         String arg = "CPI (" + String.valueOf(cpi) + ")";
         throw new ExtendedIllegalArgumentException(arg, 2);
      } /* endswitch */

      fontOrCPI = CPI_;

//      if (pageStarted_ == true) {                  @A1D
      if (pageStarted_ == false)                   //@A1A
         initPage();                               //@A1A
      
      // now set the callers CPI
      byte [] cmd = SCD;
      cmd[cmd.length-1] = CPI;
      addToBuffer(cmd);
   }
   
    /** Prints a horizontal grid line at the current line.
      *
      * @param start The absolute position in inches, from the left paper edge,
      *   to start the grid line.
      * @param stop The absolute position in inches, from the left paper edge,
      *   to stop the grid line.
      *
      * @exception IOException If an error occurs while communicating
      *   with the system.
      **/
    public void printHorizontalGridLine(double start, double stop)
           throws IOException
    {
        byte [] positions = new byte [4];
        byte [] cmd = DGL;

        cmd[2] = 0x08;                 /* Set length of command      */
        cmd[cmd.length - 1] = (byte)0x80;  /* Set horizontal option  */

        addToBuffer(cmd);              /* Output command             */

        /* Calculate start and stop in 1440ths of an inch            */
        int iStart = (int)(start * 1440.0);
        int iStop = (int)(stop * 1440.0);

        /* Put start and stop in to an array                         */
        positions[0] = (byte)(iStart >> 8);
        positions[1] = (byte)iStart;
        positions[2] = (byte)(iStop >> 8);
        positions[3] = (byte)iStop;

        addToBuffer(positions);        /* Output start and stop      */
    }


    /** Sets character rotation.  Characters can be rotated 270 degrees
      * for vertical printing.
      *
      * @param rotation The value of character rotation.  Valid values are
      *   CHARACTER_NORMAL and CHARACTER_ROTATED.
      *
      * @exception IOException If an error occurs while communicating
      *   with the system.
      **/
    public void setCharacterRotation(int rotation)
           throws IOException
    {
        byte [] cmd = STO;

        switch (rotation) {
            case CHARACTER_NORMAL:
                cmd[cmd.length -2] = 0x00;
                cmd[cmd.length -1] = 0x00;
                addToBuffer(cmd);      /* Output normal rotation     */
                break;

            case CHARACTER_ROTATED:
                cmd[cmd.length -2] = (byte)0x87;
                cmd[cmd.length -1] = 0x00;
                addToBuffer(cmd);      /* Output 270 degree rotation */
                break;

            default:
                String arg = "Rotation (" + String.valueOf(rotation) + ")";
                throw new ExtendedIllegalArgumentException(arg, 2);
        }
    }


    /** Defines the action taken by the SI/SO characters.  These characters
      * are either not printed at all or printed as spaces.
      *
      * @param set The parameter that defines the presentation option.  Valid
      *   values are SETCCP_NO_PRINT, SETCCP_BLANK_CODE, and SETCCP_SI_BLANK.
      *
      * @exception IOException If an error occurs while communicating
      *   with the system.
      **/
    public void setControlCharPresentation(int set)
            throws IOException
    {
        byte [] cmd = SPPC;

        switch(set) {
            case SETCCP_NO_PRINT:
            case SETCCP_BLANK_CODE:
            case SETCCP_SI_BLANK:
                setCCP = set;
                cmd[4] = (byte)(set >> 8);
                cmd[5] = (byte)set;
                addToBuffer(cmd);
                break;
            default:
                String arg = "set (" + String.valueOf(set) + ")";
                throw new ExtendedIllegalArgumentException(arg, 2);
        }
    }

    /** Sets font scaling.  Allows doubling the horizontal size of the
      * font.  Applies to both A/N/K and IGC characters. Actual characters
      * per inch is affected.
      *
      * @param scale The value of font scaling.  Valid values are SCALE_REGULAR and
      *   SCALE_DOUBLE_HORIZONTAL.
      *
      * @exception IOException If an error occurs while communicating
      *   with the system.
      **/
    public void setFontScaling(int scale)
           throws IOException
    {
        byte [] cmd = SFSS;

        switch (scale) {
            case SCALE_REGULAR:
                cmd[cmd.length -2] = (byte)SCALE_REGULAR;
                addToBuffer(cmd);      /* Output regular font size   */
                break;

            case SCALE_DOUBLE_HORIZONTAL:
                cmd[cmd.length -2] = (byte)SCALE_DOUBLE_HORIZONTAL;
                addToBuffer(cmd);      /* Output double font size    */
                break;

            default:
                String arg = "Scale (" + String.valueOf(scale) + ")";
                throw new ExtendedIllegalArgumentException(arg, 2);
        }
    }



    /** Starts printing vertical grid lines at the specified positions.
      *
      * @param positions An array of absolute positions in inches, from the
      *   left paper edge, to start a vertical grid line.
      *
      * @exception IOException If an error occurs while communicating
      *   with the system.
      **/
    public void startVerticalGridLines(double [] positions)
           throws IOException
    {
        byte [] cmd = DGL;
        int len = positions.length;
        byte [] iPositions = new byte [2 * len];

        cmd[2] = (byte)(4 + (2 * len));  /* Set command length       */
        cmd[5] = 0x40;            /* Set vertical grid line option   */
        addToBuffer(cmd);         /* Add command part to buffer      */

        int iPos;                 /* Position in 1440ths of an inch  */
        int j = 0;                /* iPositions index                */

        /* Loop through the positions, convert to 1440th of an inch  */
        /* and store in a byte array.                                */
        for (int i = 0; i < len; i++) {
            iPos = (int)(positions[i] * 1440.0);
            iPositions[j] = (byte)(iPos >> 8);
            j++;
            iPositions[j] = (byte)iPos;
            j++;
        }
        addToBuffer(iPositions);  /* Add positions to buffer         */
    }


    /** Stops printing vertical grid lines.
      *
      * @exception IOException If an error occurs while communicating
      *   with the system.
      **/
    public void stopVerticalGridLines()
           throws IOException
    {
        byte [] cmd = DGL;

        cmd[2] = 0x04;            /* Set command length              */
        cmd[5] = 0x00;            /* Stop and clear vertical grids   */
        addToBuffer(cmd);         /* Add command to buffer           */
    }
}
