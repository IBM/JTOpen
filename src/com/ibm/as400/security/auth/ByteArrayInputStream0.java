package com.ibm.as400.security.auth;

import java.io.ByteArrayInputStream;

/**
 Extends the ByteArrayInputStream class, by adding a getter for the protected field <tt>ByteArrayInputStream.pos</tt>.
 This enables applications to query and report the current buffer position.
 **/
final class ByteArrayInputStream0 extends ByteArrayInputStream {

  ByteArrayInputStream0(byte[] bytes)
  {
    super(bytes);
  }


  /**
   Returns the current value of <tt>ByteArrayInputStream.pos</tt>, which is "protected" in the parent class.
   @return The value of the <tt>pos</tt> field.
   **/
  int getPos() {
    return pos;
  }

}
