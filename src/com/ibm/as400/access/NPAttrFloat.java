///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (AS/400 Toolbox for Java - OSS version)                              
//                                                                             
// Filename: NPAttrFloat.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

/**
  * For a list of valid attribute IDs, see the NPObject class.
  **/

class NPAttrFloat extends NPAttribute implements Cloneable,
                                                 java.io.Serializable
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";


    static final long serialVersionUID = 4L;


    //
    // we are always dealing with packed 15,5 numbers so just make these
    // static constants.  If we ever have to deal with other types of packed
    // numbers we can make these variables and change them on the constructor
    // based on the attribute ID and the conversion code should still work.
    //
    private static final int  DIGITS    = 15;
    private static final int  DECIMALPT = 5;

    private float attrValue_;    // stored float value in PC terms

    NPAttrFloat(NPAttrFloat attr)
    {
       super(attr);
       attrValue_ = attr.attrValue_;
    }

    NPAttrFloat(int ID)
    {
       super(ID, FLOAT);
    }

    NPAttrFloat(int ID, float value)
    {
       super(ID, FLOAT);
       set(value);
    }

    NPAttrFloat(int ID,
                       byte[] hostDataStream,
                       int offset,
                       int length)
    {
       super(ID, FLOAT, hostDataStream, offset, length, null);
       buildFloatFromHostData();
    }

    /** buildHostData - builds the PACKED 15,5 byte data from the float value
     **/
    private void buildHostData()
    {
       byte[] hostValue;     // array to hold our packed decimal number
       int position;         // position goes from -decimalPt to +(digits-decimalPt).
       int digit;            // value of the digit we are on
       int needed;           // number of bytes needed

       int byteIndex;          // current byte
       boolean fHighNibble;    // current nibble (high or low)
       double MyNumber;         // working copy of the float number
       double mult;

       // I have to add a fraction to the number because of rounding errors.  If I don't do this
       // then if you say
       //   double   x = 12.657;            (for example)
       //   unsigned y = (unsigned)x*1000;
       //  y will be 12656 - not what I want! So, I say y = (unsigned)((x+.0001)*1000) and that
       //   works.  I think this is because 12.657 may really not be exactly that, internally
       //   it may be 12.656999999  is as close as the machine can get to 12.657.
       MyNumber = Math.abs(attrValue_);              // work with the positive number
       MyNumber += 0.000001;                        // fix any rounding errors

       // find out how many bytes are need to hold this guy
       // needed = (unsigned)((((float)digits+1)/2)+0.5);
       needed = (int)((((float)DIGITS+1)/2)+0.5);     // should be 8
       byteIndex = needed - 1;         // start with the last byte
       hostValue = new byte[needed];   // allocate host byte array

       // mult is the multplier you need to get the current digit (we start with the least
       //  significant one) into the 1's column of the number - we then use a cast to integer
       //  to drop the fractional part of the number and a %10 to drop the part of the number
       //  that is 10 or over (that's how we get this one digit).
       //
       // if you have a packed decimal(11,3), mult would start at 1000 and go to
       //  1x10**(-8) when you finished the for loop.
       mult = Math.pow(10, DECIMALPT);

       // First put the sign nibble in the lowest order nibble.
       //   0x0F = positive
       //   0x0D = negative
       if (attrValue_ < 0)         // if this number is negative
       {
          hostValue[byteIndex] = 0x0D;      // negative sign
       } else {
          hostValue[byteIndex] = 0x0F;      // positive sign
       } /* endif */

       // fHighNibble indicates which half-byte we're working on.
       // We started with the low nibble and used it for the sign,
       // now we're on the high nibble.
       fHighNibble = true;  // go to high order nibble

       for (position = -DECIMALPT; position < (DIGITS - DECIMALPT) ; position++ )
       {
          digit = (int)(MyNumber*mult) % 10;
          mult /= 10;
          if (!fHighNibble)
          {
             hostValue[byteIndex] = (byte)digit;     // put it in the low nibble (and zero the high)
          } else {
             hostValue[byteIndex] |= (byte)digit << 4;   // mask it in on the high nibble.
             byteIndex--;                                // go to next byte.
          } /* endif */
          fHighNibble = !fHighNibble;
       } /* endfor */

       super.setHostData(hostValue, null); // @B1C
    }

    // sets the value of attrValue_ based on the current super's host data array
    private void buildFloatFromHostData()
    {
        int needed = (int)((((float)DIGITS+1)/2)+0.5);     // should be 8 for us
        int i, j;
        double mult;
        boolean fHighNibble;    // current nibble (high or low)
        byte[]  hostData;       // packed 15,5 number
        int     byteIndex;      // index into hostData array

        attrValue_ = 0.0F;

        hostData = super.getHostData(null); // @B1C

        if (hostData != null)
        {
           byteIndex = 0;
           mult = Math.pow(10.0,(double)DIGITS-(double)DECIMALPT-1);


           // we should ALWAYS end the loop with fHighNibble == FALSE
           // if digits is ODD
           //   start with high nibble of first byte
           // else
           //   there is a wasted nibble, so start with low nibble of first byte
           if ((DIGITS%2) != 0)
           {
              fHighNibble = true;
           } else {
              fHighNibble = false;
           } /* endif */

           for (i=0; i < DIGITS; i++)
           {
              if (fHighNibble)
              {
                 j = (int)(hostData[byteIndex]) >> 4;       // get high nibble
                 j = j & 0x0F;                               // mask hi order @B3
                 // so int is signed correctly.
              } else {
                 j = (int)(hostData[byteIndex]) & 0x0F;     // get low nibble
                 byteIndex++;
              } /* endif */
              fHighNibble = !fHighNibble;

              // should we check j to be >= 0 && <= 9 here?
              attrValue_ += j*(float)mult;
              mult /= 10.0;

           } /* endfor */

           // put a sign on number
           //  if low order nibble of *pBuf is 0x0D
           //    number is negative
           //  else
           //    number is positive (low nibble should be 0x0F)
           j = (int)(hostData[byteIndex]) & 0x0F;
           if (j == 0x0D)
           {
              attrValue_ *= -1.0;
           } /* endif */
        }  /* endif - hostData == NULL */

    }

    protected Object  clone()
    {
       NPAttrFloat attr;
       attr = new NPAttrFloat(this);
       return attr;
    }

    float get()
    {
       return attrValue_;
    }

    
    void set(float value)
    {
       attrValue_ = value;
       buildHostData();
    }

    void setHostData(byte[] data, ConverterImpl converter)     // @B1C
    {
       super.setHostData(data, converter); // @B1C
       buildFloatFromHostData();
    }

}  // end of class NPAttrFloat
