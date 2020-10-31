package com.ibm.as400.access.jdbcClient;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.io.Writer;
import java.sql.Clob;

public class ClientClob 
  implements Clob, Serializable {
    /**
   * 
   */
  private static final long serialVersionUID = 1L;
    transient private String data_;

    public ClientClob (String data) { data_ = data;}
    public InputStream getAsciiStream () { return null;}

    // We need this function to work now.
    public Reader getCharacterStream () {
        return new StringReader(data_);
    }


    public Reader getCharacterStream (long pos, long length) {
        return new StringReader(data_.substring((int)pos, (int)( pos + length)));
    }

    public long position (String pattern, long start) { return -1;}
    public long position (Clob pattern, long start) { return -1;}

    // Lob indexes are 1 based.  The string that is used to
    // represent the data is 0 based.  Account for that when
    // returning a substring.
    public String getSubString (long start, int length)
    {
        return data_.substring ((int) start - 1, (int) start + length - 1);
    }

    public long length ()
    {
        return data_.length ();
    }
    public int setString(long pos, String str)                        //@F1A
    {                                                                 //@F1A
        return 0;                                                     //@F1A
    }                                                                 //@F1A
    public int setString(long pos, String str, int offest, int len)   //@F1A
    {                                                                 //@F1A
        // add code to test new methods                               //@F1A
        return 0;                                                     //@F1A
    }                                                                 //@F1A
    public OutputStream setAsciiStream(long pos)                      //@F1A        
    {                                                                 //@F1A
        // add code to test new methods                               //@F1A
        return null;                                                  //@F1A
    }                                                                 //@F1A
    public Writer setCharacterStream(long pos)                        //@F1A
    {                                                                 //@F1A
        // add code to test new methods                               //@F1A
        return null;                                                  //@F1A
    }                                                                 //@F1A
    public void truncate(long len)                                    //@F1A
    {                                                                 //@F1A
        // add code to test new methods                               //@F1A
    }                                                                 //@F1A
public void free() {} 

}
