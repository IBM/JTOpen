///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: LicenseBaseRequest.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

// import java.net.*                              Don't seem to need this
import java.io.CharConversionException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;


/**
*<p>This class is the header and common information for the data streams 
* that request information from the Central Server
* </p>
**/

class LicenseBaseRequest extends ClientAccessDataStream
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";

    protected CharConverter conv_;          

    /** <p>PRODUCT_ID_CCSID_LOCATION - Location of the CCSID for productID in the datastream.</p> **/
     static final int PRODUCT_ID_CCSID_LOCATION = 22;    
    
     /** <p>PRODUCT_ID_LOCATION - Location of the productID in the datastream.</p> **/
    static final int PRODUCT_ID_LOCATION = 26;    
    
    /** <p>FEATURE_CCSID_LOCATION - Location of the CCSID for product feature code in the datastream.</p> **/
    static final int FEATURE_CCSID_LOCATION = 33;    

    /** <p>FEATURE_LOCATION - Location of the product feature code in the datastream.</p> **/
    static final int FEATURE_LOCATION = 37;    
    
    /** <p>RELEASE_CCSID_LOCATION - Location of the CCSID for product release in the datastream.</p> **/
    static final int RELEASE_CCSID_LOCATION = 41;    

    /** <p>RELEASE_LOCATION - Location of the product release in the datastream.</p> **/
    static final int RELEASE_LOCATION = 45;    


    /**
    *<p>The ctor sets the header and blanks out the common information.</p>
    **/  
    LicenseBaseRequest(int size, AS400 system)
    {
        super();
        data_ = new byte [size];
        setLength(size);
        setHeaderID(0);
        setServerID(0xe000);
        setCSInstance(0);
        setCorrelation(0);
        setTemplateLen(size - 20);

        set16bit(0, 20);        // chain, not used

        int i;

        // blank fill product id
        for (i=0; i<7; i++)
        {
            data_[PRODUCT_ID_LOCATION+i] = (byte)0x40;
        }

        // blank fill feature
        for (i=0; i<4; i++)
        {
            data_[FEATURE_LOCATION+i] = (byte)0x40;
        }

        // blank fill release
        for (i=0; i<6; i++)
        {
            data_[RELEASE_LOCATION+i] = (byte)0x40;
        }

        set32bit(37, PRODUCT_ID_CCSID_LOCATION); // product ID ccsid
        set32bit(37, FEATURE_CCSID_LOCATION);    // feature ccsid
        set32bit(37, RELEASE_CCSID_LOCATION);    // release ccsid

        try
        {
            // Needed to switch to the public CharConverter class
            // rather than the package converter class that was 
            // removed. The CharConverter class takes a AS400 object
            // for its ctor.
            conv_ = new CharConverter(37, system);
        }
        catch (UnsupportedEncodingException exc)
        {
            if(Trace.isTraceOn())
            {
                Trace.log(Trace.ERROR, "Unsupported encoding exception - CCSID 37.");
            }
            
        }

        set16bit(0, size-2);               // optional parameter count
    }

    /**
    *<p>The setProductID function converts the product ID passed in to the 
    * the EBCDIC CCSID and sets it into the data stream.</p>
    **/  
    void setProductID(String productID)
    {
        try
        {
            conv_.stringToByteArray(productID, data_, PRODUCT_ID_LOCATION, 7);
        }
        catch (CharConversionException e)
        {
            if(Trace.isTraceOn())
            {
                Trace.log(Trace.ERROR, "Character conversion exception - productID: " + productID);
            }
        }
    }

    /**
    *<p>The setFeature function converts the product feature passed in to the 
    * the EBCDIC CCSID and sets it into the data stream.</p>
    **/  
    void setFeature(String feature)
    {
        try
        {
            conv_.stringToByteArray(feature, data_, FEATURE_LOCATION, 4);
        }
        catch (CharConversionException e)
        {
            if(Trace.isTraceOn())
            {
                Trace.log(Trace.ERROR, "Character conversion exception - feature: " + feature);
            }

        }
    }

    /**
    *<p>The setRelease function converts the product release passed in to the 
    * the EBCDIC CCSID and sets it into the data stream.</p>
    **/  
    void setRelease(String release)
    {
        try
        {
            conv_.stringToByteArray(release, data_, RELEASE_LOCATION, 6);
        }
        catch (CharConversionException e)
        {
            if(Trace.isTraceOn())
            {
                Trace.log(Trace.ERROR, "Character conversion exception - release: " + release);
            }

        }
    }

}

