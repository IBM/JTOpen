package com.ibm.as400.access;

import java.sql.SQLException;

public interface SQLVariableCompressible {
  /**
  Converts the contents of the data in compressed bytes, as needed
  in a request to the system.
  @param  rawBytes         the raw bytes for the system.
  @param  offset           the offset into the byte array.
  @param  ccsidConverter   the converter.
  @return number of bytes written
  **/
  public int convertToCompressedBytes(byte[] rawBytes, int offset, ConvTable ccsidConverter)
  throws SQLException;

}
