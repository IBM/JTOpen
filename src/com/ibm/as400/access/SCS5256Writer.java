///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: SCS5256Writer.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
  * The SCS5256Writer class writes an SCS 5256 data stream to an output stream,
  * translating characters into bytes of the specified CCSID.
  * SCS5256Writer is the simplest SCS generator.  It supports
  * text, carriage return, line feed, new line, form feed, Absolute Horiz/Vert
  * positioning, Relative Horiz/Vert positioning, and Set Vertical Format.
  *
  * @see OutputStreamWriter
  * @version  1.1
**/

 /* @A2C 
  * Moved AHPP and AVPP from 5224 class.
  * Added SVF, RHPP, and RVPP methods.
  * Added SVF and SHF to initPage()
  * Updated method descriptions.
  */

public class SCS5256Writer extends OutputStreamWriter
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


    private static final byte [] CR = {0x0D};
    private static final byte [] LF = {0x25};
    private static final byte [] NL = {0x15};
    private static final byte [] FF = {0x0C};
    private static final byte [] SGEA = {0x2B, (byte)0xC8, 0x01};
    private static final byte [] SHF = {0x2B, (byte)0xC1, 0x02, 0x00}; //@A2A
    private static final byte [] SVF = {0x2B, (byte)0xC2, 0x02, 0x00};  //@A2A
    private static final byte [] AHPP = {0x34, (byte)0xC0, 0x00};     //@A2A
    private static final byte [] AVPP = {0x34, (byte)0xC4, 0x00};     //@A2A
    private static final byte [] RHPP = {0x34, (byte)0xC8, 0x00};     //@A2A
    private static final byte [] RVPP = {0x34, (byte)0x4C, 0x00};     //@A2A

    private OutputStream outPut;
    private Converter    cvt;
    private byte []      buffer = new byte [300];
    private int          dataLength = 0;

     /* verticalFormat retains the current # of lines per page.  The
      * initial and default value is 66 which assumes 6 LPI on an 11 inch
      * page.  Users can change this value via the setVertical Format() method.
      */
    private int          verticalFormat_ = 66;          //@A3A

     /* PageStarted is a flag to indicate data has been written to the
      * page.  Some print commands can only appear at the start of a
      * page.
      */
    boolean      pageStarted_ = false;

    /**
     * Constructs a SCS5256Writer.  The default encoding will be used.
     *
     * @param out An OutputStream.
     *              
     * @deprecated Replaced by SCS5256Writer(OutputStream, int, AS400).
        Any SCS5256Writer object that is created without
        specifying an AS400 system object on its constructor may
        not behave as expected in certain environments.        
     **/
    public SCS5256Writer(OutputStream out)
    {
        super(out);
        outPut = out;
      //  cvt = Converter.getConverter(); //@A1C
        cvt = new Converter();  // @A4A
    }


    /**
     * Constructs a SCS5256Writer.
     *
     * @param out An OutputStream.
     * @param ccsid The name of the target CCSID to be used.
     *
     * @exception UnsupportedEncodingException If <I>ccsid</I> is invalid.
     * @deprecated Replaced by SCS5256Writer(OutputStream, int, AS400).
        Any SCS5256Writer object that is created without
        specifying an AS400 system object on its constructor may
        not behave as expected in certain environments.        
     **/
    public SCS5256Writer(OutputStream out,
                         int ccsid)
          throws UnsupportedEncodingException
    {
       super(out);
       outPut = out;
     //  cvt = Converter.getConverter(ccsid);  //@A1C
       cvt = new Converter(ccsid);  // @A4A
    }


    // @B1A
    /**
     * Constructs a SCS5256Writer.
     *
     * @param out An OutputStream.
     * @param ccsid The name of the target CCSID to be used.
     * @param system The system.
     *
     * @exception UnsupportedEncodingException If <I>ccsid</I> is invalid.
     **/
    public SCS5256Writer(OutputStream out,
                         int ccsid,
                         AS400 system)
          throws UnsupportedEncodingException
    {
       super(out);
       outPut = out;
       cvt = new Converter(ccsid, system);
    }


    /**
     * Constructs a SCS5256Writer.
     *
     * @param out An OutputStream.
     * @param encoding The name of the target encoding to be used.
     *
     * @exception UnsupportedEncodingException If <I>encoding</I> is invalid.
     * @deprecated Replaced by SCS5256Writer(OutputStream, int, AS400).
        Any SCS5256Writer object that is created without
        specifying an AS400 system object on its constructor may
        not behave as expected in certain environments.        
     **/
    public SCS5256Writer(OutputStream out,
                         String       encoding)
           throws UnsupportedEncodingException
    {
        super(out);
        outPut = out;
     //  cvt = Converter.getConverter(encoding);  //@A1C
        cvt = new Converter(encoding);  // @A4A 
    }



    /** Moves the print position to the column specified.  Moving
      * beyond the right end of the page will cause an error on the
      * printer.
      *
      * @param column The new horizontal print position.  Valid values are
      *   0 to the maximum print position as set in the SetHorizontalFormat
      *   method.  A value of 0 causes a no-op.  The number of columns on
      *   a line is dependent on the current character width (CPI) and the
      *   width of the page.
      *
      * @exception IOException If an error occurs while communicating
      *   with the server.
      **/
    public void absoluteHorizontalPosition(int column)
           throws IOException
    {
        byte [] cmd = AHPP;

        if ((column < 0) || (column > 255)) {
            String arg = "Column (" + String.valueOf(column) + ")";
            throw new ExtendedIllegalArgumentException(arg, 2);
        }
        if (pageStarted_ == false) initPage();
        cmd[cmd.length-1] = (byte)column;
        addToBuffer(cmd);
    }


    /** Moves the print position to the line number specified.  Moving
      * above the current line causes a form feed and move to the specified
      * line on the next page.
      *
      * @param line The new vertical print position.  Valid values are
      *   0 to the current forms length as specified by the SetVerticalFormat
      *   method.  A value of 0 causes a no-op.  If the value is less than
      *   the current line, the forms are moved to the specified line of the
      *   next logical page.
      *
      * @exception IOException If an error occurs while communicating
      *   with the server.
      **/
    public void absoluteVerticalPosition(int line)
           throws IOException
    {
        byte [] cmd = AVPP;

        if ((line < 0) || (line > 255)) {
            String arg = "Line (" + String.valueOf(line) + ")";
            throw new ExtendedIllegalArgumentException(arg, 2);
        }
        if (pageStarted_ == false) initPage();
        cmd[cmd.length-1] = (byte)line;
        addToBuffer(cmd);
    }

     /* Adds printer controls to the buffer.
      * Bytes are not translated.
      *
      * @param data The bytes to add to the buffer.
      *
      * @exception IOException If an error occurs while communicating
      *   with the server.
      */
    void addToBuffer(byte [] data)
           throws IOException
    {
       if (outPut == null) {
          throw new IOException("Stream closed");
       }

       int len = data.length;

       if ((dataLength + len) > buffer.length) flush();

       for (int i = 0; i < len; i++ ) {
          buffer[dataLength] = data[i];
          dataLength ++;
       } /* endfor */
    }


    /** Adds a carriage return control to the stream.
      *
      * @exception IOException If an error occurs while communicating
      *   with the server.
      **/
    public void carriageReturn()
           throws IOException
    {
        addToBuffer(CR);
    }


    /** Closes the stream.
      *
      * @exception IOException If an error occurs while communicating
      *   with the server.
      **/
    public void close()
           throws IOException
    {
       flush();
       outPut.close();
       outPut = null;               // Null private data to show
       cvt = null;
    }


    /** Ends current page.
      *
      * @exception IOException If an error occurs while communicating
      *   with the server.
      *
      **/
    public void endPage()
           throws IOException
    {
        addToBuffer(FF);
        pageStarted_ = false;
    }


    /** Flushes the stream.
      *
      * @exception IOException If an error occurs while communicating
      *   with the server.
      **/
    public void flush()
           throws IOException
    {
       if (dataLength > 0) {
          if (outPut == null) {
             throw new IOException("Stream closed");
          }

          outPut.write(buffer, 0, dataLength);
          dataLength = 0;
       }
    }


    // Get the CCSID used for this writer.
    int getCcsid()
    {
       if (cvt == null) {
          return 0;
       } else {
          return cvt.getCcsid();
       }
    }


    
    /** Returns the name of the encoding being used by this stream.
      * It may return null if the stream has been closed.
      **/
    public String getEncoding()
    {
       if (cvt == null) {
          return(null);
       } else {
          return cvt.getEncoding();
       }
    }


     /* Sends out controls to initialize the start of a page.
      *
      * @exception IOException If an error occurs while communicating
      *   with the server.
      *
      */
    void initPage()
           throws IOException
    {
       pageStarted_ = true;

       sendSGEA();
       addToBuffer(SHF);  //@A2A - sets Max Chars per line.  For 5256 = 132
       setVerticalFormat(verticalFormat_);  //@A2A @A3C - sets Max lines per page
    }


    /** Adds a line feed control to the stream.
      *
      * @exception IOException If an error occurs while communicating
      *   with the server.
      **/
    public void lineFeed()
           throws IOException
    {
        addToBuffer(LF);
    }


    /** Adds a new line control to the stream.
      *
      * @exception IOException If an error occurs while communicating
      *   with the server.
      **/
    public void newLine()
           throws IOException
    {
        addToBuffer(NL);
    }


    /** Moves the print position the number of characters specified.  Moving
      * beyond the right end of the page will cause an error on the
      * printer.
      *
      * @param chars The number of character widths to move.  Valid values are
      *   0 to the maximum print position minus the current column.  The maximum
      *   print position is the current CPI * 13.2 inches. A value of 0 causes a
      *   no-op.
      *
      * @exception IOException If an error occurs while communicating
      *   with the server.
      **/
    public void relativeHorizontalPosition(int chars)
           throws IOException
    {
        byte [] cmd = RHPP;

        // Just check chars for min and max value.  User must keep track of
        // max value for page size
        if ((chars < 0) || (chars > 255)) {
            String arg = "chars (" + String.valueOf(chars) + ")";
            throw new ExtendedIllegalArgumentException(arg, 2);
        }
        if (pageStarted_ == false) initPage();
        cmd[cmd.length-1] = (byte)chars;
        addToBuffer(cmd);
    }

    /** Moves the print position the number of lines specified.
      *
      * @param lines The number of lines to move down the page.  Valid values are
      *   0 to the last logical line on the page. A value of 0 causes a
      *   no-op.
      *
      * @exception IOException If an error occurs while communicating
      *   with the server.
      **/
    public void relativeVerticalPosition(int lines)
           throws IOException
    {
        byte [] cmd = RVPP;

        // Just check chars for min and max value.  User must keep track of
        // max value for page size
        if ((lines < 0) || (lines > 255)) {
            String arg = "lines (" + String.valueOf(lines) + ")";
            throw new ExtendedIllegalArgumentException(arg, 2);
        }
        if (pageStarted_ == false) initPage();
        cmd[cmd.length-1] = (byte)lines;
        addToBuffer(cmd);
    }

    /* Sends Set Graphic Error Action (SGEA) command.  One multibyte
     * command must be sent for the data stream sniffer to determine
     * this is a SCS data stream.
     *
     */
    private void sendSGEA()
            throws IOException
    {
        addToBuffer(SGEA);
    }

    /** Sets the Vertical Format.  This specifies the maximum number of
      * lines on the page. Note that when the printer executes this command
      * the current line number on the printer is set to 1.  Also, the
      * combination of LPI and Vertical Format should not exceed 159 cm
      * (63.75 inches).  Changing the Line Density changes where the maximum
      * line is on the physical page.
      *
      * @param NumOfLines The maximum number of lines.  Valid values are
      * 0 to 255.  A value of 0 causes vertical format to be set to the
      * printer default.
      *
      * @exception IOException If an error occurs while communicating
      *   with the server.
      **/
    public void setVerticalFormat(int NumOfLines)
            throws IOException
    {
        byte [] cmd = {0,0,0,0};

        if ((NumOfLines < 0) || (NumOfLines > 255)) {
            String arg = "NumOfLines (" + String.valueOf(NumOfLines) + ")";
            throw new ExtendedIllegalArgumentException(arg, 2);
        }
        verticalFormat_ = NumOfLines;

        if (pageStarted_ == false) initPage();

        for(int i=0; i< (cmd.length-1); i++)
        {
            cmd[i] = SVF[i];
        }
        cmd[cmd.length-1] = (byte)NumOfLines;
        addToBuffer(cmd);
    }


    /** Writes a portion of an array of characters.
      *
      * @param databuffer The buffer of characters.
      * @param offset  The offset from which to start writing characters.
      * @param length  The number of characters to write.
      *
      * @exception IOException If an error occurs while communicating
      *   with the server.
      **/
    public void write(char databuffer[],
                      int offset,
                      int length)
           throws IOException
    {
       // Convert input to a string.
       String aStr = new String(databuffer, offset, length);
       write(aStr);
    }


    /** Writes a single character.
      *
      * @param c  The character to write.
      *
      * @exception IOException If an error occurs while communicating
      *   with the server.
      **/
    public void write(int c)
          throws IOException
    {
       // Convert input to a string.
       String aStr = String.valueOf(c);
       write(aStr);
    }


    /** Writes a string.
      *
      * @param str  The string to write.
      *
      * @exception IOException If an error occurs while communicating
      *   with the server.
      **/
    public void write(String str)
           throws IOException
    {
       if (outPut == null) {
          throw new IOException("Stream closed");
       }

       if (pageStarted_ == false) initPage();

       // Convert input to a byte array of proper encoding.
       byte [] convertedBytes = cvt.stringToByteArray(str);

       // If input too large for room in buffer, flush buffer
       if ((dataLength + convertedBytes.length) > buffer.length) {
          flush();

          // If input data is larger than buffer, write data
          if (convertedBytes.length > buffer.length) {
             outPut.write(convertedBytes, 0, convertedBytes.length);
             return;
          }
       }

       // Copy input to buffer
       System.arraycopy(convertedBytes, 0, buffer, dataLength,
                        convertedBytes.length);
       dataLength += convertedBytes.length;
    }


    /** Writes a portion of a string.
      *
      * @param str  The string to write.
      * @param offset  The offset from which to start writing characters.
      * @param length  The number of characters to write.
      *
      * @exception IOException If an error occurs while communicating
      *   with the server.
      **/
    public void write(String str,
                      int offset,
                      int length)
           throws IOException
    {
       String aStr = str.substring(offset, offset + length);
       write(aStr);
    }


}
