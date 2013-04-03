///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: LineDataRecordWriter.java
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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
  * Writes a record in line data format (with
  * the name of the record format inserted into positions 1-10 of the line data),
  * translating characters into bytes of the specified CCSID.
  * The line data is written to an OutputStream.
  * 
  * @see OutputStream
  *
**/

 
public class LineDataRecordWriter extends Object
{
    private OutputStream outPut;
    private Converter    cvt;
    private ByteArrayOutputStream buffer ;

     /**
     * Constructs a LineDataRecordWriter. The target CCSID defaults to the CCSID  
     * of the system.
     *
     * @param out An OutputStream.
     * @param system The system.
     *
     * @exception UnsupportedEncodingException If <I>ccsid</I> is not valid.
     **/
    public LineDataRecordWriter(OutputStream out,     
                                AS400 system)
      throws UnsupportedEncodingException    
    {
       outPut = out;
       cvt = new Converter(system.getCcsid(), system);
       buffer = new ByteArrayOutputStream();
    }
    

    /**
     * Constructs a LineDataRecordWriter.
     *
     * @param out An OutputStream.
     * @param ccsid The name of the target CCSID to be used.
     * @param system The system.
     *
     * @exception UnsupportedEncodingException If <I>ccsid</I> is not valid.
     **/
    public LineDataRecordWriter(OutputStream out,
                                int ccsid,
                                AS400 system)
                           
          throws UnsupportedEncodingException
    {
       outPut = out;
       cvt = new Converter(ccsid, system);
       buffer = new ByteArrayOutputStream();
    }

   

    // Adds the data to the buffer, which is a ByteArrayOutputStream
    private void addToBuffer(byte [] data)
           throws IOException
    {
       if (outPut == null) {
          throw new IOException("Stream closed");
       }

       int len = data.length;

       buffer.write(data,0,len);
    }
        
    
    /**
     * Gets the CCSID used for this writer.
     *
     * @return The CCSID used for this writer.
     *
     **/
    public int getCcsid()                                                        //@A1C
    {
       if (cvt == null) {
          return 0;
       } else {
          return cvt.getCcsid();
       }
    }


    
    /** Gets the name of the encoding being used by this LineDataRecordWriter.
     * @return The name of the encoding being used by this LineDataRecordWriter.
     * Null may be returned if the stream is closed.
     **/
    public String getEncoding()
    {
       if (cvt == null) {
          return(null);
       } else {
          return cvt.getEncoding();
       }
    }
    
    /** Formats the record data in line data format, with the format id in 
      * in positions 1-10.  The data is translated into bytes
      * using the specified CCSID.  The format of the data is determined by
      * the record format associated with the record. The data is then placed
      * in the outstream buffer. The following record
      * format attributes are required to be set.
      * <ul>
      *   <li>Record format ID
      *   <li>Record format type
      * </ul>
      * For a record created with record format type of VARIABLE_LAYOUT_LENGTH,
      * the record format delimiter must be specified. 
      * <br> For a record created 
      * with a format type of FIXED_LAYOUT_LENGTH, the field description layout
      * attributes, length and alignment must be specified. If alignment is not 
      * specified, alignment will be defaulted to ALIGN_LEFT.
      *
      * @param record The record to be converted to line data.
      *
      **/
    private void retrieveRecord(Record record)
      throws IOException, UnsupportedEncodingException,
             ExtendedIllegalStateException 
           
    {  
       int fldalign;
       int fldlayoutl;
       byte [] convertedBytes;
       FieldDescription fd;
       
       String fvalue;
       String str;

       int strlen;
       int padlen;


       // get the record format
       RecordFormat recfmt = record.getRecordFormat();
       
       // get the record format id from the record
       String recfmtid = recfmt.getRecordFormatID();

       // get record format type from the record
       int recfmttype = recfmt.getRecordFormatType();
       
       // get the all of the fields from the record
       Object fields[] = record.getFields();
       
       // get the delimiter from the record
       char delimiter = recfmt.getDelimiter();
       
       // validate parameters passed in 
       if ("".equals(recfmtid)) {                                                           //@A1C
          throw new ExtendedIllegalStateException("recordFormatID",
                           ExtendedIllegalStateException.PROPERTY_NOT_SET);
       }
       if (recfmttype == 0) {
       
          throw new ExtendedIllegalStateException("recordFormatType for record format "+recfmtid,
                           ExtendedIllegalStateException.PROPERTY_NOT_SET);            //@A1C
       }
      

       
        
       // convert the format id to ebcdic and put into buffer
       byte[] conBytes1 = cvt.stringToByteArray(recfmtid);
       addToBuffer(conBytes1);

       // loop through all fields in record
       for (int i =0; i < fields.length; i++) {
        
          // get field description from record        
          fd = recfmt.getFieldDescription(i);
          
          // get data from field
          fvalue = fields[i].toString();
          
          // set local variable with the length of data
          strlen = fvalue.length();

          // fixed length record logic
          if (recfmttype == RecordFormat.FIXED_LAYOUT_LENGTH ) {
         
             //get alignment style and field length from field description 
             fldalign = fd.getLayoutAlignment();
             fldlayoutl = fd.getLayoutLength();

             // validate alignment value
             if (fldalign == 0)                                                        //@A1C
             {
               fldalign = FieldDescription.ALIGN_LEFT;                                 //@A1C 
             }
           
             // field length greater than the data, so need to pad           
             if (fldlayoutl >= strlen)                                                 //@A1C
             {
                //calculate pad length
                padlen = fldlayoutl - strlen;
                
                //create pad string
                String padstring = pad(padlen);
                
                // align data right
                if (fldalign == FieldDescription.ALIGN_RIGHT ) {
                   str = padstring + fvalue;
                }
                else
                {
                   // align data left
                   str = fvalue + padstring;
                }   
             }
             else
             {
               //field length smaller than data
               throw new ExtendedIllegalArgumentException("Field description " + fd.getFieldName(),
                           ExtendedIllegalArgumentException.LENGTH_NOT_VALID);           //@A1A
               
               //str = fvalue;                                                           //@A1D
             }
          }
          else 
          {
             // validate delimiter
             if (delimiter == '\0')                                                      //@A1C
             {
                  throw new ExtendedIllegalStateException("delimiter for record format "+recfmtid,
                       ExtendedIllegalStateException.PROPERTY_NOT_SET);                  //@A1C
             }

             // Add the delimiter to the data        
             str = fvalue + delimiter;
          }
          
          // convert data to ebcdic and place in outstream buffer
          byte[] conBytes = cvt.stringToByteArray(str);
          addToBuffer(conBytes);

       }

    }
    /** Writes the record data, in line data format, to an OutputStream.  
      * The data is translated into bytes using the specified CCSID.  The format 
      * of the data is determined by the record format associated with the record.  
      * The following record format attributes are required to be set.
      * <ul>
      *   <li>Record format ID
      *   <li>Record format type
      * </ul>
      * For a record created with record format type of VARIABLE_LAYOUT_LENGTH,
      * the record format delimiter must be specified. 
      * <br> For a record created 
      * with a format type of FIXED_LAYOUT_LENGTH, the field description layout
      * attributes, length and alignment must be specified. If alignment is not 
      * specified, alignment will be defaulted to ALIGN_LEFT.
      * <br>
      * If the OutputStream is a SpooledFileOutputStream, the SpooledFileOutputStream 
      * must have the following parameters set:
      * <ul>
      *   <li> <A HREF="PrinterAttributes.html#HDRKEY17.5">
      *        ATTR_CONTROL_CHARACTER - Forms Control Character set to *NONE
      *        </A>
      *   <li> <A HREF="PrinterAttributes.html#HDRKEY177">
      *        ATTR_CONVERT_LINE_DATA - Convert Line Data set to *YES
      *        </A>
      *   <li> <A HREF="PrinterAttributes.html#HDRKEYIFS_3">
      *          ATTR_FORM_DEFINITION - Form definition integrated file system 
      *          name
      *        </A>
      *
      *   <li>  <A HREF="PrinterAttributes.html#HDRKEYIFS_D">
      *          ATTR_PAGE_DEFINITION - Page definition integrated file system 
      *         name
      *        </A>
      *   <li> <A HREF="PrinterAttributes.html#HDRKEY93">
      *          ATTR_PRTDEVTYPE - Printer device type set to *LINE 
      *        </A>
      * </ul>
      * @param record  The record to be converted to line data.
      * @see Record
      *
      * @exception IOException If an error occurs while communicating
      *   with the AS/400.
      * @exception UnsupportedEncodingException If <I.ccsid</I> in not valid.
      **/

    public void writeRecord(Record record)
           throws IOException, UnsupportedEncodingException,
                  ExtendedIllegalStateException 
        
    {
       // intialize outstream buffer 
       buffer.reset();
       
       // get data from record, pad as necessary, convert to ebcdic 
       // and write to outstream buffer
       retrieveRecord(record);
       
       // verify that buffer contains data and the outstream is open
       if (buffer.size() > 0)
       {
          if (outPut == null)
          {
             throw new IOException("Stream closed");
          }

          // write data from outstream buffer to outstream, flush the outstream
          // and reset the buffer
          buffer.writeTo(outPut);
          outPut.flush();
          buffer.reset();
       }
    }
       

    private String pad( int slen)
    {
       StringBuffer buf = new StringBuffer(slen);
       for (int i=0; i < slen; i++) {
          buf.append(" ");

       }
       return buf.toString();
    }

}
