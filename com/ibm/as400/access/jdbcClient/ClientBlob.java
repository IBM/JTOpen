package com.ibm.as400.access.jdbcClient;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.sql.Blob;

public class ClientBlob 
  implements Blob, Serializable {
    /**
   * 
   */
  private static final long serialVersionUID = -8024939411981058348L;
    private byte[] data_;

    public ClientBlob (byte[] data) { data_ = data;}

    // We need this function to work now.
    public InputStream getBinaryStream () {
        return new ByteArrayInputStream(data_);
    }

    public InputStream getBinaryStream (long pos, long length) {

        return new ByteArrayInputStream(data_, (int)pos, (int) length);
    }

    public long position (byte[] pattern, long start) { return -1;}
    public long position (Blob pattern, long start) { return -1;}

    public byte[] getBytes (long start, int length)
    {
        // Not correct, but good enough for here.
        return data_;
    }

    public long length ()
    {
        return data_.length;
    }
    public int setBytes(long pos, byte[] bytes)                        //@F1A
    {                                                                  //@F1A
        // add code to test new methods                                //@F1A
        return 0;                                                      //@F1A
    }                                                                  //@F1A
    public int setBytes(long pos, byte[] bytes, int offest, int len)   //@F1A
    {                                                                  //@F1A
        // add code to test new methods                                //@F1A
        return 0;                                                      //@F1A
    }                                                                  //@F1A
    public OutputStream setBinaryStream(long pos)                      //@F1A
    {                                                                  //@F1A
        // add code to test new methods                                //@F1A
        return null;                                                   //@F1A
    }                                                                  //@F1A
    public void truncate(long len)                                     //@F1A
    {                                                                  //@F1A
        // add code to test new methods                                //@F1A
    }                                                                  //@F1A


public void free() {} 

}
