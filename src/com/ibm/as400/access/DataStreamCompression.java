///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: DataStreamCompression.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2001 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;



/**
The DataStreamCompression class provides support for RLE (Run Length Encoding)
compression and decompression.  When data is compressed using RLE, contiguous
repeating bytes in the datastream are replaced with an RLE record.  When data
is decompressed, the RLE record is expanded out into repeating bytes again.

The format of the output RLE record is as follows:

<ul>

<li>RLE repeater record:

<ul>
<li>1 byte escape
<li>2 byte repeater
<li>2 byte repeat count
</ul>

<li>RLE escape record:

<ul>
<li>1 byte escape
<li>1 byte escape
</ul>

</ul>
      
During compression, each escape byte will be replaced with an RLE escape
record (two escape bytes).  If a byte is not an escape byte, then two bytes
are compared to the next two bytes to determine if the byte pair is repeated.
Compression is done and an RLE repeater record built if the byte pairs are
repeated more than five times (totaling 10 or more bytes repeated).

For additional information on RLE, see the LIPI documentation for the
Remote Command/Program Server. 

**/
class DataStreamCompression
{
  private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";


  final static byte DEFAULT_ESCAPE = (byte) 0x1B;

  final static int ESCAPE_SIZE = 1;
  final static int REPEATER_SIZE = 2;
  final static int COUNT_SIZE = 2;

  final static int REPEATER_RECORD_SIZE = ESCAPE_SIZE + REPEATER_SIZE + COUNT_SIZE;
  final static int ESCAPE_RECORD_SIZE = ESCAPE_SIZE + ESCAPE_SIZE;


  /**
  No need to instantiate an object of this class since the class
  only contains two static methods.  Emphasizing this fact with a
  private ctor.
  **/
  private DataStreamCompression ()
  {
  }


  /**
  Compress data in the source byte array and write the compressed data
  to the returned byte array.
  
  @param source              The source (decompressed) bytes.
  @param sourceOffset        The offset in the source bytes at which to start
                             compressing.
  @param length              The length of the bytes to compress.
  @param escape              The escape character. Use DEFAULT_ESCAPE.
  
  @return  The compressed bytes or null if the data was not compressed.
           If the compressed data length is larger than the uncompressed
           length, null is returned.
  
  **/                                         
  static byte[] compressRLE (byte[] source,
                             int sourceOffset, 
                             int length, 
                             byte escape)
  {
    // Validate the input byte array.
    if (source == null)
    {
      throw new NullPointerException("source");
    }

    // Validate the source offset value.
    if (sourceOffset >= source.length)
    {
      throw new ExtendedIllegalArgumentException("sourceOffset", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }

    // Validate the length value.
    if (length <= 0)
    {
      throw new ExtendedIllegalArgumentException("length", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }

    // Set flags to indicate which type of tracing, if any, should be done.
    boolean traceDiagnostic = Trace.isTraceOn() && Trace.isTraceDiagnosticOn();

    byte[] destination = new byte[length];

    int compressedCount = compressRLEInternal(source, sourceOffset, length, destination, 0, escape);  // @A1A
    if (compressedCount >= 0)
    {                                                                       // @A1A
      byte[] returnBytes = new byte[compressedCount];                                               // @A1A
      System.arraycopy(destination, 0, returnBytes, 0, compressedCount);                            // @A1A
      return returnBytes;                                                                           // @A1A
    }                                                                                                 // @A1A
    else                                                                                              // @A1A
      return null;                                                                                  // @A1A
  }


// @A1A
  /**
  Compress data in the source byte array and write the compressed data
  to the destination byte array.
  
  @param source              The source (decompressed) bytes.
  @param sourceOffset        The offset in the source bytes at which to start
                             compressing.
  @param length              The length of the bytes to compress.
  @param destination         The destination (compressed) bytes.
  @param destinationOffset   The offset in the destination bytes at which to
                             assign compressed bytes.
  @param escape              The escape character. Use DEFAULT_ESCAPE.
  
  @return  The number of compressed bytes, or -1 if the data was not compressed.
           If the compressed data length is larger than the uncompressed length,
           null is returned.
  **/                                         
  static int compressRLE (byte[] source,
                          int sourceOffset, 
                          int length, 
                          byte[] destination,
                          int destinationOffset,
                          byte escape)
  {
    // Validate the input byte array.
    if (source == null)
    {
      throw new NullPointerException("source");
    }

    // Validate the source offset value.
    if (sourceOffset >= source.length)
    {
      throw new ExtendedIllegalArgumentException("sourceOffset", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }

    // Validate the length value.
    if (length <= 0)
    {
      throw new ExtendedIllegalArgumentException("length", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }

    // Validate the input byte array.
    if (destination == null)
    {
      throw new NullPointerException("destinationOffset");
    }

    // Validate the source offset value.
    if (destinationOffset >= destination.length)
    {
      throw new ExtendedIllegalArgumentException("destinationOffset", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }

    return compressRLEInternal(source, sourceOffset, length, destination, destinationOffset, escape);
  }



// @A1A - Moved from compressRLE(byte[], int, int, byte)
  /**
  Compress data in the source byte array and write the compressed data
  to the destination byte array.
  
  @param source              The source (decompressed) bytes.
  @param sourceOffset        The offset in the source bytes at which to start
                             compressing.
  @param length              The length of the bytes to compress.
  @param destination         The destination (compressed) bytes.
  @param destinationOffset   The offset in the destination bytes at which to
                             assign compressed bytes.
  @param escape              The escape character. Use DEFAULT_ESCAPE.
  
  @return  The number of compressed bytes, or -1 if the data was not compressed.
           If the compressed data length is larger than the uncompressed length,
           null is returned.
  **/                                         
  private static int compressRLEInternal (byte[] source,
                                          int sourceOffset, 
                                          int length, 
                                          byte[] destination,
                                          int destinationOffset,
                                          byte escape)
  {
    // Set flags to indicate which type of tracing, if any, should be done.
    boolean traceDiagnostic = Trace.isTraceOn() && Trace.isTraceDiagnosticOn();

    int returnCount = -1;

    int i = sourceOffset;               // Index into source.
    int j = destinationOffset;          // Index into destination.

    boolean overflow = false;           // destination array overflow indicator

    int sourceLength = sourceOffset + length;
    int destinationLength = destination.length;
    if (traceDiagnostic)
    {
      Trace.log(Trace.DIAGNOSTIC, "compressRLE() sourceLength: " + sourceLength);
    }

    while (i < sourceLength && overflow != true)
    {

      // Have an escape byte
      if (source[i] == escape)
      {
        // Bytes fit in destination array
        if (j + ESCAPE_RECORD_SIZE <= destinationLength)
        {
          // Write out an escape record
          destination[j++] = escape;
          destination[j++] = escape;
          ++i;
        }
        // Destination array overflow
        else
        {
          if (traceDiagnostic)
            Trace.log(Trace.DIAGNOSTIC, "Overflow when writing out escape record starting at dest " + j + " ...");
          overflow = true;
        }
      }
      // Have a single, non-escape byte and end of source data.
      else if ((i+1) >= sourceLength)
      {
        // Bytes fit in destination array
        if (j < destinationLength)
        {
          // Write out the last byte.
          destination[j++] = source[i++];
        }
        // Destination array overflow
        else
        {
          if (traceDiagnostic)
            Trace.log(Trace.DIAGNOSTIC, "Overflow when writing out last byte before EOD to dest " + j + " ...");
          overflow = true;
        }
      }
      // Have a single byte, followed by an escape character.
      else if (source[i+1] == escape)
      {
        // Bytes fit in destination array
        if ((j + 1 + ESCAPE_RECORD_SIZE) <= destinationLength)
        {
          // Write out the single byte and then the escape record.
          destination[j++] = source[i++];     // byte before the escape byte
          destination[j++] = escape;
          destination[j++] = escape;
          ++i;                                // byte after the escape byte
        }
        // Destination array overflow
        else
        {
          if (traceDiagnostic)
            Trace.log(Trace.DIAGNOSTIC, "Overflow when writing out single byte and escape record starting at dest " + j + " ...");
          overflow = true;
        }
      }
      // Have at least two non-escape bytes that could be a repeater.
      // Compare two bytes with next two bytes.
      else
      {
        int saveOffset = i;
        //@P0D int repeater = BinaryConverter.byteArrayToUnsignedShort(source, i); // @A2C
        int repeater = ((source[i] & 0xFF) << 8) + (source[i+1] & 0xFF); //@P0A
        int count = 1;                                                      // @A2C
        i += 2;
        // Calculate the number of times these two bytes are repeated.
        while (((i+1) < sourceLength) && repeater == (((source[i] & 0xFF) << 8) + (source[i+1] & 0xFF))) //@P0C
               //@P0D (BinaryConverter.byteArrayToUnsignedShort(source, i) == repeater))
        { // @A2C
          count++;
          i += 2;
        }
        // Calculate the length of the repeating characters.
        int repeatLength = count * REPEATER_SIZE;

        // Determine if we have enough repeating bytes to merit an RLE record.
        if (repeatLength >= REPEATER_RECORD_SIZE * 2)
        {
          // Enough repeating data. Build RLE record.
          if (j + REPEATER_RECORD_SIZE <= destinationLength)
          {
            // Bytes fit in destination array; write out the repeated bytes.
            destination[j] = escape; //@P0C
            //@P0D BinaryConverter.unsignedShortToByteArray(repeater, destination, j); // @A2C
            destination[++j] = (byte)(repeater >>> 8); //@P0A
            destination[++j] = (byte) repeater; //@P0A
            //@P0D BinaryConverter.unsignedShortToByteArray(count, destination, j+2); // @A2C
            //@P0D j += 4;
            destination[++j] = (byte)(count >>> 8); //@P0A
            destination[++j] = (byte) count; //@P0A
            ++j; //@P0A
          }
          // Destination array overflow
          else
          {
            if (traceDiagnostic)
              Trace.log(Trace.DIAGNOSTIC, "Overflow when writing out RLE repeater record starting at dest " + j + " ...");
            overflow = true;
          }
        }
        else
        {
          // Not enough repeating data. Just copy the data to destination array.
          i = saveOffset;
          // Bytes fit in destination array
          if ((j + (repeatLength - 1)) < destinationLength)
          {
            // Write out the repeated bytes.
            for (int n=0; n < repeatLength; n++)
            {
              destination[j++] = source[i++];
            }
          }
          // Destination array overflow
          else
          {
            if (traceDiagnostic)
              Trace.log(Trace.DIAGNOSTIC, "Overflow when writing out non-repeating bytes to dest " + j + " ...");
            overflow = true;
          }
        }
      }
    }

    returnCount = j - destinationOffset;                                 // @A1A
    if (!overflow && (returnCount < length))
    {                           // @A1C
      if (traceDiagnostic)
        Trace.log(Trace.DIAGNOSTIC, "compressRLE() length of compressed bytes returned: " + j);
    }
    else
    {
      returnCount = -1;
      if (traceDiagnostic)
        Trace.log(Trace.DIAGNOSTIC, "compressRLE() returning null (compressed size >= decompressed size)");
    }

    return returnCount;
  }



  /**
  Decompress data in the source byte array and write the decompressed data
  to the returned byte array.
  
  @param source              The source (compressed) bytes.
  @param sourceOffset        The offset in the source bytes at which to start
                             decompressing.
  @param length              The length of the bytes to decompress.
  @param decompressedLength  The length of the bytes in their decompressed state.
                             If this length is provided, the byte array to be
                             returned will be created using this length.  If this
                             length is not known, input 0.  When set to 0, this
                             value will be calculated and the returned byte array
                             will be created with the calculated length.
                             Performance is improved when the correct decompressed
                             length is provided as input.
  @param escape              The escape character. Use DEFAULT_ESCAPE.
  
  @return  The decompressed bytes.
  
  **/                                         
  static byte[] decompressRLE (byte[] source,
                               int sourceOffset,
                               int length,
                               int decompressedLength,
                               byte escape)
  {
    // Validate the input byte array.
    if (source == null)
    {
      throw new NullPointerException("source");
    }

    // Validate the source offset value.
    if (sourceOffset >= source.length)
    {
      throw new ExtendedIllegalArgumentException("sourceOffset", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }

    // Validate the length value.
    if (length <= 0)
    {
      throw new ExtendedIllegalArgumentException("length", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }

    // Validate the decompressed length value.
    if (decompressedLength < 0)
    {
      throw new ExtendedIllegalArgumentException("decompressedLength", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }

    // Create a temporary buffer for the decompressed data.
    byte[] destination;
    if (decompressedLength == 0)
    {
      destination = new byte[2048];
    }
    else
    {
      destination = new byte[decompressedLength];
    }

    return decompressRLEInternal(source, sourceOffset, length, destination, 0, escape, true); // @A1A
  }



// @A1A
  /**
  Decompress data in the source byte array and write the decompressed data
  to the destination byte array.
  
  @param source              The source (compressed) bytes.
  @param sourceOffset        The offset in the source bytes at which to start
                             decompressing.
  @param length              The length of the bytes to decompress.
  @param destination         The destination (decompressed) bytes.
  @param destinationOffset   The offset in the destination bytes at which to
                             assign decompressed bytes.
  @param escape              The escape character. Use DEFAULT_ESCAPE.
  
  @return  The decompressed bytes.
  
  **/                                         
  static void decompressRLE (byte[] source,
                             int sourceOffset,
                             int length,
                             byte[] destination,
                             int destinationOffset,
                             byte escape)
  {
    // Validate the input byte array.
    if (source == null)
    {
      throw new NullPointerException("source");
    }

    // Validate the source offset value.
    if (sourceOffset >= source.length)
    {
      throw new ExtendedIllegalArgumentException("sourceOffset", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }

    // Validate the length value.
    if (length <= 0)
    {
      throw new ExtendedIllegalArgumentException("length", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }

    // Validate the input byte array.
    if (destination == null)
    {
      throw new NullPointerException("destination");
    }

    // Validate the source offset value.
    if (destinationOffset >= destination.length)
    {
      throw new ExtendedIllegalArgumentException("destinationOffset", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
    }

    decompressRLEInternal(source, sourceOffset, length, destination, destinationOffset, escape, false);
  }


// @A1A - Moved from decompressRLE(byte[], int, int, int, byte)
  /**
  Decompress data in the source byte array and write the decompressed data
  to the destination byte array.
  
  @param source              The source (compressed) bytes.
  @param sourceOffset        The offset in the source bytes at which to start
                             decompressing.
  @param length              The length of the bytes to decompress.
  @param destination         The destination (decompressed) bytes.
  @param destinationOffset   The offset in the destination bytes at which to
                             assign decompressed bytes.
  @param escape              The escape character. Use DEFAULT_ESCAPE.
  @param reallocate          true to reallocate the destination array, if needed,
                             false otherwise.
                             
  @return  The decompressed bytes.
  **/                                         
  private static byte[] decompressRLEInternal (byte[] source,
                                               int sourceOffset,
                                               int length,
                                               byte[] destination,
                                               int destinationOffset,
                                               byte escape,
                                               boolean reallocate)
  {
    // Set flags to indicate which type of tracing, if any, should be done.
    boolean traceDiagnostic = Trace.isTraceOn() && Trace.isTraceDiagnosticOn();
    boolean traceError = Trace.isTraceOn() && Trace.isTraceErrorOn();

    int i = sourceOffset;               // Index into source.
    int j = destinationOffset;          // Index into destination.
    int saveI = -1;                     // keep current position of the source index
    int saveJ = -1;                     // keep current position of the destination index

    boolean overflow = false;           // destination array overflow indicator
    int bytesNeeded = 0;                // number of additional bytes needed

    int sourceLength = sourceOffset + length;
    int destinationLength = destination.length;

    if (traceDiagnostic)
    {
      Trace.log(Trace.DIAGNOSTIC, "decompressRLE() sourceLength: " + sourceLength);
      Trace.log(Trace.DIAGNOSTIC, "decompressRLE() destinationLength: " + destinationLength);
    }

    while (i < sourceLength)
    {

      // Have an escape byte
      if (source[i] == escape)
      {
        // Not end of source
        if ((i + ESCAPE_SIZE) < sourceLength)
        {

          // Second byte is escape; have an RLE escape record
          if (source[i + ESCAPE_SIZE] == escape)
          {

            // Byte fits in destination array
            if (j < destinationLength)
            {
              // Add escape byte to destination array
              destination[j] = escape;
            }
            // Destination array overflow
            else
            {
              if (traceDiagnostic)
              {
                Trace.log(Trace.DIAGNOSTIC, "Overflow while decompressing RLE escape record starting at " + j + " ...");
              }
              // save each index only when overflow is encountered 
              // the first time; otherwise, just tally up the number of 
              // additional bytes needed
              if (!overflow)
              {
                saveI = i;
                saveJ = j;
              }
              overflow = true;
              bytesNeeded += ESCAPE_SIZE;
            }
            i += ESCAPE_RECORD_SIZE;
            j += ESCAPE_SIZE;
          }

          // Should have an RLE repeater record;
          // have an escape byte followed by a non-escape byte
          else
          {

            // Not end of source; have a complete RLE repeater record
            if ((i + REPEATER_SIZE + COUNT_SIZE) < sourceLength)
            {

              // Get repeater
              //@P0D int repeater = BinaryConverter.byteArrayToUnsignedShort(source, i + ESCAPE_SIZE); // @A2C
              int repeater = ((source[i+ESCAPE_SIZE] & 0xFF) <<  8) + (source[i+ESCAPE_SIZE+1] & 0xFF); //@P0A
              // Get repeat count
              //@P0D int count = BinaryConverter.byteArrayToUnsignedShort(source, i + ESCAPE_SIZE + REPEATER_SIZE); // @A2C
              int count = ((source[i+ESCAPE_SIZE+REPEATER_SIZE] & 0xFF) << 8) + (source[i+ESCAPE_SIZE+REPEATER_SIZE+1] & 0xFF); //@P0A
              
              // Bytes fit in destination array
              if ((j + (count * REPEATER_SIZE)) <= destinationLength)
              {
                // Write out the bytes to destination array  
                for (int k = 1; k <= count; ++k)
                { // @A2C
                  //@P0D BinaryConverter.unsignedShortToByteArray(repeater, destination, j); // @A2C
                  destination[j]   = (byte)(repeater >>> 8); //@P0A
                  destination[j+1] = (byte) repeater; //@P0A
                  j += REPEATER_SIZE;
                }
              }
              // Destination array overflow
              else
              {
                if (traceDiagnostic)
                {
                  Trace.log(Trace.DIAGNOSTIC, "Overflow while decompressing RLE repeater record starting at dest " + j + " ...");
                }
                // save each index only when overflow is encountered 
                // the first time; otherwise, just tally up the number of 
                // additional bytes needed
                if (!overflow)
                {
                  saveI = i;
                  saveJ = j;
                }
                overflow = true;
                bytesNeeded += (count * REPEATER_SIZE);
                j += (count * REPEATER_SIZE);
              }
              i += REPEATER_RECORD_SIZE;
            }
            // Error (don't have a complete RLE repeater record before EOD)
            else
            {
              if (traceError)
                Trace.log(Trace.ERROR, "Don't have a complete RLE repeater record before EOD ...");
              throw new InternalErrorException(InternalErrorException.SYNTAX_ERROR );
            }
          }

        }
        // End of source reached and have single escape byte
        else
        {
          if (traceError)
            Trace.log(Trace.ERROR, "Don't have a complete RLE escape record before EOD ...");
          throw new InternalErrorException(InternalErrorException.SYNTAX_ERROR );
        }
      }

      // No RLE record found; just copy bytes from source to destination arrray
      else
      {
        // Byte fits in destination array
        if (j < destinationLength)
        {
          // Add byte to destination array
          destination[j++] = source[i++];
        }
        // Destination array overflow
        else
        {
          if (traceDiagnostic)
          {
            Trace.log(Trace.DIAGNOSTIC, "Overflow when writing out single bytes ...");
          }
          // save each index only when overflow is encountered 
          // the first time; otherwise, just tally up the number of 
          // additional bytes needed
          if (!overflow)
          {
            saveI = i;
            saveJ = j;
          }
          overflow = true;
          bytesNeeded += 1;
          ++i;
          ++j;
        }
      }

    }

    // Buffer for decompressed bytes that will be returned.
    byte[] returnBytes;

    if ((overflow) && (reallocate))
    {
      // Destination array too small.  Add bytes needed to length
      // and create a return buffer of the correct length.
      j = saveJ;
      returnBytes = new byte[(j + bytesNeeded)];
      int returnBytesLength = returnBytes.length;
      if (traceDiagnostic)
      {
        Trace.log(Trace.DIAGNOSTIC, "Overflow. Size updated to " + returnBytesLength + " bytes.");
      }
      System.arraycopy(destination, 0, returnBytes, 0, j);
      i = saveI;
      overflow = false;

      while (i < sourceLength)
      {

        // Have an escape byte
        if (source[i] == escape)
        {

          // Not end of source
          if ((i + ESCAPE_SIZE) < sourceLength)
          {

            // Second byte is escape; have an RLE escape record
            if (source[i + ESCAPE_SIZE] == escape)
            {
              // Add escape byte to destination array
              returnBytes[j++] = escape;
              i += ESCAPE_RECORD_SIZE;
            }

            // Should have an RLE repeater record;
            // have an escape byte followed by a non-escape byte
            else
            {

              // Not end of source; have a complete RLE repeater record
              if ((i + REPEATER_SIZE + COUNT_SIZE) < sourceLength)
              {

                // Get repeater
                //@P0D int repeater = BinaryConverter.byteArrayToUnsignedShort(source, i + ESCAPE_SIZE); // @A2C
                int repeater = ((source[i+ESCAPE_SIZE] & 0xFF) << 8) + (source[i+ESCAPE_SIZE+1] & 0xFF); //@P0A
                // Get repeat count
                //@P0D int count = BinaryConverter.byteArrayToUnsignedShort(source, i + ESCAPE_SIZE + REPEATER_SIZE); // @A2C
                int count = ((source[i+ESCAPE_SIZE+REPEATER_SIZE] & 0xFF) << 8) + (source[i+ESCAPE_SIZE+REPEATER_SIZE+1] & 0xFF); //@P0A

                // Write out the bytes to destination array  
                for (int k = 1; k <= count; k++)
                { // @A2C
                  //@P0D BinaryConverter.unsignedShortToByteArray(repeater, returnBytes, j); // @A2C
                  returnBytes[j] = (byte)(repeater >>> 8); //@P0A
                  returnBytes[j+1] = (byte)repeater; //@P0A
                  j += REPEATER_SIZE;
                }
                i += REPEATER_RECORD_SIZE;
              }
              // Error (don't have a complete RLE repeater record before EOD)
              else
              {
                if (traceError)
                  Trace.log(Trace.ERROR, "Don't have a complete RLE repeater record before EOD ...");
                throw new InternalErrorException(InternalErrorException.SYNTAX_ERROR );
              }
            }

          }
          // End of source reached and have single escape byte
          else
          {
            if (traceError)
              Trace.log(Trace.ERROR, "Don't have a complete RLE escape record before EOD ...");
            throw new InternalErrorException(InternalErrorException.SYNTAX_ERROR );
          }
        }

        // No RLE record found; just copy bytes from source to destination arrray
        else
        {
          returnBytes[j++] = source[i++];
        }

      }
      return returnBytes;
    }
    else if ((destination.length > j) && (reallocate))
    {
      // Destination array too big.  Create a return buffer of the correct length,
      // copy data into it, and return the decompressed bytes.
      returnBytes = new byte[j];
      System.arraycopy(destination, destinationOffset, returnBytes, 0, returnBytes.length);
      return returnBytes;
    }
    else
    {
      // Destination array is correct length.  Return the decompressed bytes.
      return destination;
    }

  }

}
