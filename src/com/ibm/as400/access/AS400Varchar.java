///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: AS400Varchar.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2007 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////
// @AC8 20201027 zhangze  Varchar converter
//                    
///////////////////////////////////////////////////////////////////////////////
package com.ibm.as400.access;

import java.io.UnsupportedEncodingException;

public class AS400Varchar implements AS400DataType {
	static final long serialVersionUID = 4L;
    private int length_;
    private int ccsid_ = 65535;
    transient private String encoding_ = null;
    private AS400 system_;
    transient ConverterImpl tableImpl_;
    private static final String defaultValue_ = "";
    private int varlensize_ = 2;

    /**
     Constructs an AS400Varchar object.
     It uses the most likely CCSID based on the default locale.
     @param  varlensize the size of Varchar length, it must be 2 byte or 4 byte.
     @param  length  The byte length of the IBM i text.  It must be greater than or equal to zero.
     **/
    public AS400Varchar(int varlensize, int length)
    {
        if (length < 0)
        {
            Trace.log(Trace.ERROR, "Value of parameter 'length' is not valid:", length);
            throw new ExtendedIllegalArgumentException("length (" + length + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }
        if (varlensize < 0)
        {
            Trace.log(Trace.ERROR, "Value of parameter 'varlensize' is not valid:", length);
            throw new ExtendedIllegalArgumentException("varlensize (" + varlensize + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }
        varlensize_ = varlensize;
        length_ = length;
    }
    
    /**
    Constructs an AS400Varchar object.
    @param  varlensize  the size of the Varchar length, it must be 2 bytes or 4 bytes
    @param  length  The byte length of the IBM i text.  It must be greater than or equal to zero.
    @param  ccsid  The CCSID of the IBM i text.  It must refer to a valid and available CCSID.  The value 65535 will cause the data type to use the most likely CCSID based on the default locale.
    **/
   public AS400Varchar(int varlensize, int length, int ccsid)
   {
       if (length < 0)
       {
           Trace.log(Trace.ERROR, "Value of parameter 'length' is not valid:", length);
           throw new ExtendedIllegalArgumentException("length (" + length + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
       }
       if (ccsid < 0)
       {
           Trace.log(Trace.ERROR, "Value of parameter 'ccsid' is not valid:", ccsid);
           throw new ExtendedIllegalArgumentException("ccsid (" + ccsid + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
       }
       if (varlensize < 0)
       {
           Trace.log(Trace.ERROR, "Value of parameter 'varlensize' is not valid:", length);
           throw new ExtendedIllegalArgumentException("varlensize (" + varlensize + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
       }
       varlensize_ = varlensize;
       length_ = length;
       ccsid_ = ccsid;
   }
    
    /**
    Constructs AS400Varchar object.
    @param  varlensize  the size of the Varchar length, it must be 2 bytes or 4 bytes
    @param  length  The byte length of the IBM i text.  It must be greater than or equal to zero.
    @param  encoding  The name of a character encoding.  It must be a valid and available encoding.
    **/
   public AS400Varchar(int varlensize, int length, String encoding)
   {
       if (length < 0)
       {
           Trace.log(Trace.ERROR, "Value of parameter 'length' is not valid:", length);
           throw new ExtendedIllegalArgumentException("length (" + length + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
       }
       if (encoding == null)
       {
           Trace.log(Trace.ERROR, "Parameter 'encoding' is null.");
           throw new NullPointerException("encoding");
       }
       if (varlensize < 0)
       {
           Trace.log(Trace.ERROR, "Value of parameter 'varlensize' is not valid:", length);
           throw new ExtendedIllegalArgumentException("varlensize (" + varlensize + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
       }
       varlensize_ = varlensize;
       length_ = length;
       encoding_ = encoding;
   }
   
   /**
   Constructs an AS400Varchar object.
   @param  varlensize  the size of the Varchar length, it must be 2 bytes or 4 bytes
   @param  length  The byte length of the IBM i text.  It must be greater than or equal to zero.
   @param  ccsid  The CCSID of the IBM i text.  It must refer to a valid and available CCSID.  The value 65535 will cause the data type to use the most likely CCSID based on the default locale.
   @param  system  The system from which the conversion table may be downloaded.
   */
  public AS400Varchar(int varlensize, int length, AS400 system)
  {
      this(varlensize, length, 65535, system);
  }
    
    /**
    Constructs an AS400Varchar object.
    @param  varlensize  the size of the Varchar length, it must be 2 bytes or 4 bytes
    @param  length  The byte length of the IBM i text.  It must be greater than or equal to zero.
    @param  ccsid  The CCSID of the IBM i text.  It must refer to a valid and available CCSID.  The value 65535 will cause the data type to use the most likely CCSID based on the default locale.
    @param  system  The system from which the conversion table may be downloaded.
    */
   public AS400Varchar(int varlensize, int length, int ccsid, AS400 system)
   {
       if (length < 0)
       {
           Trace.log(Trace.ERROR, "Value of parameter 'length' is not valid:", length);
           throw new ExtendedIllegalArgumentException("length (" + length + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
       }
       if (ccsid < 0)
       {
           Trace.log(Trace.ERROR, "Value of parameter 'ccsid' is not valid:", ccsid);
           throw new ExtendedIllegalArgumentException("ccsid (" + ccsid + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
       }
       if (system == null)
       {
           Trace.log(Trace.ERROR, "Parameter 'system' is null.");
           throw new NullPointerException("system");
       }
       if (varlensize < 0)
       {
           Trace.log(Trace.ERROR, "Value of parameter 'varlensize' is not valid:", length);
           throw new ExtendedIllegalArgumentException("varlensize (" + varlensize + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
       }
       varlensize_ = varlensize;
       length_ = length;
       ccsid_ = ccsid;
       system_ = system;
   }

    // Package scope constructor for use on the proxy server.  Note that this constructor is only used in AS400FileRecordDescriptionImplRemote.  It is expected that the client code (AS400FileRecordDescription) will call fillInConverter() on each AS400Varchar object returned.
    AS400Varchar(int length, int ccsid, AS400Impl system)
    {
        if (length < 0)
        {
            Trace.log(Trace.ERROR, "Value of parameter 'length' is not valid:", length);
            throw new ExtendedIllegalArgumentException("length (" + length + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }
        if (ccsid < 0)
        {
            Trace.log(Trace.ERROR, "Value of parameter 'ccsid' is not valid:", ccsid);
            throw new ExtendedIllegalArgumentException("ccsid (" + ccsid + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }
        if (system == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'system' is null.");
            throw new NullPointerException("system");
        }
        length_ = length;
        ccsid_ = ccsid;
        // Notice that we have not filled in the Converter object.  We can't do that because we don't know if this object will in the end be used on the public side (Converter) or on the IBM i side (ConverterImpl).
        // We also can't do that yet since the Converter ctor will connect to the system.
    }
    
    // Package scope constructor for use on the proxy server.  Note that this constructor is only used in AS400FileRecordDescriptionImplRemote.  It is expected that the client code (AS400FileRecordDescription) will call fillInConverter() on each AS400Varchar object returned.
    AS400Varchar(int varlensize, int length, int ccsid, AS400Impl system)
    {
        if (length < 0)
        {
            Trace.log(Trace.ERROR, "Value of parameter 'length' is not valid:", length);
            throw new ExtendedIllegalArgumentException("length (" + length + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }
        if (ccsid < 0)
        {
            Trace.log(Trace.ERROR, "Value of parameter 'ccsid' is not valid:", ccsid);
            throw new ExtendedIllegalArgumentException("ccsid (" + ccsid + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }
        if (system == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'system' is null.");
            throw new NullPointerException("system");
        }
        if (varlensize < 0)
        {
            Trace.log(Trace.ERROR, "Value of parameter 'varlensize' is not valid:", length);
            throw new ExtendedIllegalArgumentException("varlensize (" + varlensize + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
        }
        varlensize_ = varlensize;
        length_ = length;
        ccsid_ = ccsid;
        // Notice that we have not filled in the Converter object.  We can't do that because we don't know if this object will in the end be used on the public side (Converter) or on the IBM i side (ConverterImpl).
        // We also can't do that yet since the Converter ctor will connect to the system.
    }

    /**
     Creates a new AS400Varchar object that is identical to the current instance.
     @return  The new object.
     **/
    public Object clone()
    {
        try
        {
            return super.clone();  // Object.clone does not throw exception.
        }
        catch (CloneNotSupportedException e)
        {
            Trace.log(Trace.ERROR, "Unexpected CloneNotSupportedException:", e);
            throw new InternalErrorException(InternalErrorException.UNEXPECTED_EXCEPTION);
        }
    }

    /**
     Returns the byte length of the data type.
     @return  The number of bytes in the IBM i representation of the data type.
     **/
    public int getByteLength()
    {
        return length_ + varlensize_; //length of IBM i text bytes + 2 or 4 bytes of length field;
    }
    
    /**
    Returns the size of the data length, 2 or 4 bytes.
    @return  The size of the data length.
    **/
    public int getVarLengthSize() {
    	return varlensize_;
    }

    /**
     Returns the CCSID of the data type.
     @return  The CCSID.
     **/
    public int getCcsid()
    {
        if (ccsid_ == 65535) setTable();
        return ccsid_;
    }

    // Returns the ConverterImpl object so other classes don't need to create a new Converter if they already have an AS400Varchar object.
    ConverterImpl getConverter()
    {
        setTable();
        return tableImpl_;
    }

    /**
     Returns a Java object representing the default value of the data type.
     @return  The String object representing an empty string ("").
     **/
    public Object getDefaultValue()
    {
        return defaultValue_;
    }

    /**
     Returns the encoding of the data type.
     @return  The encoding of the data type.
     **/
    public String getEncoding()
    {
        if (encoding_ == null) setTable();
        return encoding_;
    }

    /**
     Returns {@link com.ibm.as400.access.AS400DataType#TYPE_TEXT TYPE_TEXT}.
     @return <tt>AS400DataType.TYPE_TEXT</tt>.
     **/
    public int getInstanceType()
    {
        return AS400DataType.TYPE_VARCHAR;
    }

    /**
     * Returns the Java class that corresponds with this data type.
     * @return <tt>String.class</tt>.
     **/
    public Class getJavaType()
    {
      return String.class;
    }

    // This method is used in conjunction with the constructor that takes an AS400Impl.  It is used to fully instantiate the member data of this AS400Varchar object once it has been serialized and received on the client from the proxy server.  We do it this way because we can't create a normal AS400Varchar object on the proxy server and expect it to be valid on the proxy client because its internal Converter object would not be proxified correctly.
    // When an AS400Varchar object is serialized from the proxy server over to the client, the client code must set the converter using this method.
    void setConverter(AS400 system)
    {
        system_ = system;
        setTable();
    }

    // When an AS400Varchar object is serialized from the client over to the proxy server, the server code must set the converter using this method.  Note that we cannot refer directly to the ConverterImplRemote class here, so it is left up to the server code to create that and pass it in to this method.
    void setConverter(ConverterImpl converter)
    {
        tableImpl_ = converter;
        // Just in case this object ever goes back to the client.
        ccsid_ = tableImpl_.getCcsid();
    }

    // Private method to initialize the Converter table and its impl.
    private void setTable()
    {
        if (tableImpl_ == null)
        {
            if (Trace.traceOn_) Trace.log(Trace.CONVERSION, "AS400Varchar object initializing, encoding: " + encoding_ + ", CCSID: " + ccsid_ + ", system: " + system_);
            if (encoding_ != null)
            {
                try
                {
                    Converter table = new Converter(encoding_);
                    ccsid_ = table.getCcsid();
                    tableImpl_ = table.impl;
                }
                catch (UnsupportedEncodingException e)
                {
                    throw new ExtendedIllegalArgumentException("encoding (" + encoding_ + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
                }
            }
            else
            {
                try
                {
                    if (system_ == null)
                    {
                        Converter table;
                        if (ccsid_ == 65535)
                        {
                            table = new Converter();
                            ccsid_ = table.getCcsid();
                        }
                        else
                        {
                            table = new Converter(ccsid_);
                        }
                        tableImpl_ = table.impl;
                    }
                    else
                    {
                        if (ccsid_ == 65535)
                        {
                            ccsid_ = system_.getCcsid();
                        }
                        Converter table = new Converter(ccsid_, system_);
                        tableImpl_ = table.impl;
                    }
                    encoding_ = tableImpl_.getEncoding();
                }
                catch (UnsupportedEncodingException e)
                {
                    Trace.log(Trace.ERROR, "Value of parameter 'ccsid' is not valid:", ccsid_);
                    throw new ExtendedIllegalArgumentException("ccsid (" + ccsid_ + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
                }
            }            
            
            if (Trace.traceOn_) Trace.log(Trace.CONVERSION, "AS400Varchar object initialized, encoding: " + encoding_ + ", CCSID: " + ccsid_ + ", system: " + system_ + ", table: " + tableImpl_);
        }
    }

    /**
     Converts the specified Java object to IBM i format.
     @param  javaValue  The object corresponding to the data type.  It must be an instance of String, and the converted text length must be less than or equal to the byte length of this data type.  If the provided string is not long enough to fill the return array, the remaining bytes will be padded with space bytes (EBCDIC 0x40, ASCII 0x20, or Unicode 0x0020).
     @return  The IBM i representation of the data type.
     **/
    public byte[] toBytes(Object javaValue)
    {
        byte[] serverValue = new byte[length_ + varlensize_];
        toBytes(javaValue, serverValue, 2);
        return serverValue;
    }

    /**
     Converts the specified Java object into IBM i format in the specified byte array.
     @param  javaValue  The object corresponding to the data type.  It must be an instance of String, and the converted text length must be less than or equal to the byte length of this data type.  If the provided string is not long enough to fill the return array, the remaining bytes will be padded with space bytes (EBCDIC 0x40, ASCII 0x20, or Unicode 0x0020).
     @param  serverValue  The array to receive the data type in IBM i format.  There must be enough space to hold the IBM i value.
     @return  The number of bytes in the IBM i representation of the data type.
     **/
    public int toBytes(Object javaValue, byte[] serverValue)
    {
        return toBytes(javaValue, serverValue, 0);
    }

    /**
     Converts the specified Java object into IBM i format in the specified byte array.
     @param  javaValue  The object corresponding to the data type.  It must be an instance of String, and the converted text length must be less than or equal to the byte length of this data type.  If the provided string is not long enough to fill the return array, the remaining bytes will be padded with space bytes (EBCDIC 0x40, ASCII 0x20, or Unicode 0x0020).
     @param  serverValue  The array to receive the data type in IBM i format.  There must be enough space to hold the IBM i value.
     @param  offset  The offset into the byte array for the start of the IBM i value.  It must be greater than or equal to zero.
     @return  The number of bytes in the IBM i representation of the data type.
     **/
    public int toBytes(Object javaValue, byte[] serverValue, int offset)
    {    	
    	if(system_ != null){
    		if(!system_.bidiAS400Varchar)  {
    			//this will disable Bidi conversion
    			return toBytes(javaValue, serverValue, offset, 
    					new BidiConversionProperties(AS400BidiTransform.getStringType(system_.getCcsid())));
    					}
    	} 
    	
    	return toBytes(javaValue, serverValue, offset, new BidiConversionProperties(getSystemBidiType()));   	
    }

    /**
     Converts the specified Java object into IBM i format in the specified byte array.
     @param  javaValue  The object corresponding to the data type.  It must be an instance of String, and the converted text length must be less than or equal to the byte length of this data type.  If the provided string is not long enough to fill the return array, the remaining bytes will be padded with space bytes (EBCDIC 0x40, ASCII 0x20, or Unicode 0x0020).
     @param  serverValue  The array to receive the data type in IBM i format.  There must be enough space to hold the IBM i value.
     @param  offset  The offset into the byte array for the start of the IBM i value.  It must be greater than or equal to zero.
     @param  type  The bidi string type, as defined by the CDRA (Character Data Representation Architecture).  See <a href="BidiStringType.html"> BidiStringType</a> for more information and valid values.
     @return  The number of bytes in the IBM i representation of the data type.
     @see  com.ibm.as400.access.BidiStringType
     **/
    public int toBytes(Object javaValue, byte[] serverValue, int offset, int type)
    {
        return toBytes(javaValue, serverValue, offset, new BidiConversionProperties(type));
    }

    /**
     Converts the specified Java object into IBM i format in the specified byte array.
     @param  javaValue  The object corresponding to the data type.  It must be an instance of String, and the converted text length must be less than or equal to the byte length of this data type.  If the provided string is not long enough to fill the return array, the remaining bytes will be padded with space bytes (EBCDIC 0x40, ASCII 0x20, or Unicode 0x0020).
     @param  serverValue  The array to receive the data type in IBM i format.  There must be enough space to hold the IBM i value.
     @param  offset  The offset into the byte array for the start of the IBM i value.  It must be greater than or equal to zero.
     @param  properties  The bidi conversion properties.
     @return  The number of bytes in the IBM i representation of the data type.
     **/
    public int toBytes(Object javaValue, byte[] serverValue, int offset, BidiConversionProperties properties)
    {
        // Check here to avoid sending bad data to Converter and ConvTable.
        if (javaValue == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'javaValue' is null.");
            throw new NullPointerException("javaValue");
        }

        // Make sure the table is set.
        setTable();
        
        // We need to pad the String before the conversion in the case of a Bidi CCSID, because the Bidi transform needs to affect the entire String so it knows where the padding spaces need to go.
        String toConvert = (String)javaValue;

        byte[] eValue = tableImpl_.stringToByteArray(toConvert, properties);
        int eValueLength = eValue != null? eValue.length:0;

        // Check that converted data fits within data type.
        if (eValueLength > length_)
        {
            Trace.log(Trace.ERROR, "Length of parameter 'javaValue' is not valid: '" + javaValue + "'");
            throw new ExtendedIllegalArgumentException("javaValue (" + toConvert + ")", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
        }
        
        //Set String length in byte array.
        int varOffset = offset + varlensize_;
        if (varlensize_ == 2) {
        	BinaryConverter.shortToByteArray((short)eValueLength, serverValue, 0);	
        } else {
        	BinaryConverter.intToByteArray(eValueLength, serverValue, 0);
        }

        // Let this line throw ArrayIndexException.
        System.arraycopy(eValue, 0, serverValue, varOffset, eValue.length);

        if (Trace.traceOn_) Trace.log(Trace.CONVERSION, "AS400Varchar.toBytes(): Converted javaValue (" + toConvert + ") to:", serverValue, varOffset, length_);

        return length_ + varlensize_; //bytes length + 2/4 bytes of length field
    }

    /**
     Converts the specified IBM i data type to a Java object.
     @param  serverValue  The array containing the data type in IBM i format.  The entire data type must be represented.
     @return  The String object corresponding to the data type.
     **/
    public Object toObject(byte[] serverValue)
    {
        // Check here to avoid sending bad data to Converter and ConvTable.
        if (serverValue == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'serverValue' is null.");
            throw new NullPointerException("serverValue");
        }
        
        if (serverValue.length > length_) {
        	Trace.log(Trace.ERROR, "Length of parameter serverValue is not valid: '" + serverValue.length + "'");
            throw new ExtendedIllegalArgumentException("serverValue ", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
        }
        setTable(); // Make sure the table is set.       
        return tableImpl_.byteArrayToString(serverValue, varlensize_, length_, new BidiConversionProperties(getSystemBidiType()));
    }

    /**
     Converts the specified IBM i data type to a Java object.
     @param  serverValue  The array containing the data type in IBM i format.  The entire data type must be represented.
     @param  offset  The offset into the byte array for the start of the IBM i value. It must be greater than or equal to zero.
     @return  The String object corresponding to the data type.
     **/
    public Object toObject(byte[] serverValue, int offset)
    {
        // Check here to avoid sending bad data to Converter and ConvTable.
        if (serverValue == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'serverValue' is null.");
            throw new NullPointerException("serverValue");
        }
        if (serverValue.length > length_) {
        	Trace.log(Trace.ERROR, "Length of parameter serverValue is not valid: '" + serverValue.length + "'");
            throw new ExtendedIllegalArgumentException("serverValue ", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
        }
        setTable(); // Make sure the table is set.        
        return tableImpl_.byteArrayToString(serverValue, offset + varlensize_, length_, new BidiConversionProperties(getSystemBidiType()));//Bidi-HCG3
    }

    /**
     Converts the specified IBM i data type to a Java object.
     @param  serverValue  The array containing the data type in IBM i format.  The entire data type must be represented.
     @param  offset  The offset into the byte array for the start of the IBM i value. It must be greater than or equal to zero.
     @param  type  The bidi string type, as defined by the CDRA (Character Data Representation Architecture).  See <a href="BidiStringType.html"> BidiStringType</a> for more information and valid values.
     @return  The String object corresponding to the data type.
     @see com.ibm.as400.access.BidiStringType
     **/
    public Object toObject(byte[] serverValue, int offset, int type)
    {
        return toObject(serverValue, offset + varlensize_, new BidiConversionProperties(type));
    }

    /**
     Converts the specified IBM i data type to a Java object.
     @param  serverValue  The array containing the data type in IBM i format.  The entire data type must be represented.
     @param  offset  The offset into the byte array for the start of the IBM i value. It must be greater than or equal to zero.
     @param  properties  The bidi conversion properties.
     @return  The String object corresponding to the data type.
     **/
    public Object toObject(byte[] serverValue, int offset, BidiConversionProperties properties)
    {
        // Check here to avoid sending bad data to Converter and ConvTable.
        if (serverValue == null)
        {
            Trace.log(Trace.ERROR, "Parameter 'serverValue' is null.");
            throw new NullPointerException("serverValue");
        }
        if (serverValue.length > length_) {
        	Trace.log(Trace.ERROR, "Length of parameter serverValue is not valid: '" + serverValue.length + "'");
            throw new ExtendedIllegalArgumentException("serverValue ", ExtendedIllegalArgumentException.LENGTH_NOT_VALID);
        }
        setTable(); // Make sure the table is set
        return tableImpl_.byteArrayToString(serverValue, offset + varlensize_, length_, properties);
    }
        
    private int getSystemBidiType() {
    	if(system_ == null)
    		return BidiStringType.DEFAULT;
    	else
    		return system_.getBidiStringType();	
    }

}

