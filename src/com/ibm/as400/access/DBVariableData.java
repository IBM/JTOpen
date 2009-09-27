///////////////////////////////////////////////////////////////////////////////

//JTOpen (IBM Toolbox for Java - OSS version)                                 

//Filename: DBVariableDataInput.java

//The source code contained herein is licensed under the IBM Public License   
//Version 1.0, which has been approved by the Open Source Initiative.         
//Copyright (C) 2009-2009 International Business Machines Corporation and     
//others. All rights reserved.                                                

///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;



//@array new class
/**
An implementation of DBData which
describes the data used in datastreams for (the release following v6r1) and later
systems when sending arrays as input to stored procedures.
This is the implementation of the x382f and x3901 codepoints.
Since input and output are different, we have toHost and fromHost member data.
Output data from host is received in one byte array, which we parse and save various offsets for later use.
Input data size of header, indicators, data is calculated externally and passed in to constructor.
Before arrays, input/output parms for stored procedures were handled with codepoints x381f and x380e via DBExtendedData.
If an input or output parm is an array, then we use x382f and x3901 codepoints instead.
Note:  This class is modeled after DBExtendedData since (x382F is like x381f and x3901 is like x380e codepoints)

382f (input parms):
    Consistency token 4 bytes 
        Count of '9911'x and '9912'x code points that follow 2 bytes 
        '9911'x ,  data type 2 bytes, data length 4 bytes, # of data items 4 bytes
        '9912'x ,  data type 2 bytes, data length 4 bytes
        all of the indicator data
        all of the data bytes


3901 (output parms):
    Consistency token 4 bytes 
    Row Count 4 bytes   <Ignore>
    Column Count 2 bytes  <Ignore>
    Indicator Size 2 bytes <Ignore>
    RESERVED 4 bytes <Ignore>
    Row Size 4 bytes <Ignore>
    Consistency token 4 bytes 
       Count of '9911'x and '9912'x code points that follow 2 bytes 
       '9911'x ,  data type 2 bytes, data length 4 bytes, # of data items 4 bytes
       '9912'x ,  data type 2 bytes, data length 4 bytes
       all of the indicator data
       all of the data bytes

NOTE 1:  Clients will only receive the information about output parameters. If a stored procedure is called with 3 IN and 2 OUT/INOUT parameters(in any order), the count of parameters returned on this flow is 2, followed by the indicator and data for those 2 OUT/INOUT parameters.
NOTE 2: When returning a NULL array, ZDA will pass '9911'x followed by 'FFFF'x.
*/

class DBVariableData
implements DBData
{
    private static final String copyright = "Copyright (C) 1997-2001 International Business Machines Corporation and others.";

 

    // Private data.
    private byte[]  rawBytes_           = null;  //will be all data combined when sending in request and receiveing data
    private int     headerCurOffsetToHost_ = -1;     //index into rawBytes_[] (toHost)
    private int     indicatorCurOffsetToHost_ = -1;  //index into rawBytesIndicatorsToHost_[]
    private int     dataCurOffsetToHost_ = -1;       //index into rawBytesDataToHost_[] (may not be used since we call SQLData.convertToRawBytes externally which tracks this offset)
    private int     headerTotalSizeToHost_ = -1;    //length for rawBytesToHost_ when sending to host (could be smaller if null arrays etc)
                                                    //block size starting from count of 9911/9912s up to indicators (not consistency token etc)
    private int     indicatorTotalSizeToHost_ = -1;    //length for rawBytesIndicatorsToHost_
    private int     dataTotalSizeToHost_ = -1;       //length for rawBytesDataToHost_ when sending to host
    
    private int     offset_             = -1; //

    //This was for pre-V5R1 private boolean dataCompressed_     = false; (old pre-ILE compression)
    //Also note: ILE compression is done outside this class on whole stream
    private boolean vlfCompressed_      = false;  //For now, not supported in arrays

    private int     rowCount_           = -1;  //for now, only 1 row
    private int     columnCount_        = -1;
    private int     indicatorSize_      = -1;
    //now always variable private int     rowSize_            = -1;

    //for input parms, offset_, add +4 to get to count of parms.  
    //for output parms, here, we addjust +4 to get to begining of array data (count of parms)
    final private  int actual382fInArrayStart_ = 4;
    final private  int actual3901OutArrayStart_ = 4; //@arraylen
    private int     headerOffsetFromHost_     = -1;  //same as offset_, but a user-friendly name
    private int     indicatorOffsetFromHost_    = -1;
    private int     dataOffsetFromHost_         = -1;
    private int     lengthFromHost_             = -1; //Total length of x382f/x3901 (header/indicators/data)

 
    //Four parallel arrays below are generated to maintain header, indicator, data offset info from a reply from the Host.
    //These four arrays are all 0 offset based.  So you will have to add values in them to headerOffsetFromHost_ or dataOffsetFromHost_ when used 
    //This is because data are variable depending upon the number/type of columns etc
    //Instead of making JDServerRow more messy than it already is, adding these four parallel arrays here
    private int[]   indicatorOffsetsFromHost_ = null; //@array
    private int[]   dataOffsetsFromHost_      = null; //@array 
    private int[]   indicatorCountsFromHost_  = null; //@array number of indicators for a given column (1 if non-array, else array length)
    private int[]   dataLengthsFromHost_      = null; //@array total length of data portion in stream
    private int[]   dataIsArrayFromHost_      = null; //@nullelem flag 1 = array 0 = non-array
    private int[]   dataIsNullArrayFromHost_      = null; //@nullelem flag -1 = null array 0 = non null
    
    /**
    Constructs a DBVariableData object.  Use this when overlaying
    on a reply datastream.  The cached data will be set when overlay()
    is called.
     **/
    public DBVariableData () 
    { 
        //@array nothing to do.  overlay will calculate offsets etc.     
    }
    
    /**
    Constructs a DBVariableData object.  Use this when overlaying
    on a request datastream.  This sets the cached data so that
    the total length can be calculated before calling overlay().
    **/
    public DBVariableData (
            int columnCount,
            int indicatorSize,
            int headerTotalSize,
            int indicatorTotalSize,
            int dataTotalSize)
    throws DBDataStreamException
    {
        //input parm constructor
        rowCount_       = 1;  //always 1 row when sending array
        columnCount_    = columnCount;
        indicatorSize_  = 2; //always 2 for now
        
        headerTotalSizeToHost_ = headerTotalSize; 
        indicatorTotalSizeToHost_ = indicatorTotalSize;
        dataTotalSizeToHost_ = dataTotalSize;
        
    }
 
    /**
    Positions the overlay structure.  This reads the cached data only
    when it was not previously set by the constructor.
     **/
    public void overlay (byte[] rawBytes, int offset)
    {
        
      
        
        if (rowCount_ == -1) {
            //output array
            offset_ = offset;
            lengthFromHost_ = 0; //Total length of x3901 (header/indicators/data)
            //Header is also variable depending on number of columns and whether they are arrays/nulls etc.
            int headerLength = 0; //(consistency=4, count=2, x9911/x9912 blocks) 
            
            //on reply stream, we only need one array rawBytes_[] since host already assembled it.
            //ouput 3901: useful data starts at offset 20, not 4 like input.
            columnCount_       = BinaryConverter.byteArrayToShort(rawBytes, offset_ + actual3901OutArrayStart_);  //count of 9911s or 9912s
            rowCount_    = 1;  //always 1 row
            indicatorSize_  = 2;  //always 2 bytes
            
            indicatorOffsetsFromHost_ = new int[columnCount_]; //one per column
            indicatorCountsFromHost_ = new int[columnCount_]; //one per column - number of indicators for a given column
            dataOffsetsFromHost_ = new int[columnCount_];  //one per column
            dataLengthsFromHost_ = new int[columnCount_];  //one per column
            dataIsArrayFromHost_ = new int[columnCount_];  //one per column //@nullelem
            dataIsNullArrayFromHost_ = new int[columnCount_];  //one per column //@nullelem
            
            //iterate through columns to get total length of data and indicators
            //colDescs used to iterate through each of the 9911/9912 col descriptions.
            int colDescs = offset_ + actual3901OutArrayStart_ + 2; //start of first col description: offset + 4 + 2 (# of 9911 and 9912 descriptors)
            int dataLenAll = 0;
            int indicatorLenAll = 0;
            
            for(int i = 0; i < columnCount_; i++){
                short descType = BinaryConverter.byteArrayToShort(rawBytes, colDescs); //9911=array or 9912=non-array
    
                if(descType == (short)0x9911){
                    //array
                    boolean isNull = BinaryConverter.byteArrayToShort (rawBytes, colDescs + 2) == (short)0Xffff ? true : false; //is whole array null //@nullelem
                    dataIsArrayFromHost_[i] = 1; //@nullelem array type
                   
                    if(isNull){
                        colDescs += 4;  //null = x9911 xffff skip 4 to next description
                        indicatorOffsetsFromHost_[i] = (i == 0 ? 0 : indicatorOffsetsFromHost_[i-1] + (indicatorCountsFromHost_[i-1] * indicatorSize_));  
                        indicatorCountsFromHost_[i] = 0;  //flag that array is null or zero length array if 0 indicator counts and type of array
                        dataOffsetsFromHost_[i] = (i == 0 ? 0 : dataOffsetsFromHost_[i-1]); //keep same as prev since null arrays have no data (so we can later keep a running total)(ie no -1 values)
                        dataLengthsFromHost_[i] = 0;
                        
                        dataIsNullArrayFromHost_[i] = -1; //@nullelem null array
                    }else{
                        int dataLen = BinaryConverter.byteArrayToInt (rawBytes, colDescs + 4);
                        int arrayLen = BinaryConverter.byteArrayToInt(rawBytes, colDescs + 8); //element count //@array2
                        dataLenAll += (dataLen * arrayLen);
                        indicatorLenAll += (indicatorSize_ * arrayLen);
                        indicatorOffsetsFromHost_[i] =  (i == 0 ? 0 : indicatorOffsetsFromHost_[i-1] + (indicatorCountsFromHost_[i-1] * indicatorSize_));
                        indicatorCountsFromHost_[i] = arrayLen;  
                        dataOffsetsFromHost_[i] = (i == 0 ? 0 : dataOffsetsFromHost_[i-1] + dataLengthsFromHost_[i-1]); //keep same as prev since null arrays have no data (so we can later keep a running total)(ie no -1 values)
                        dataLengthsFromHost_[i] = arrayLen * dataLen;
                        colDescs += 12; //0x9911=2, datatype=2, datalen=4, arraylen=4 -> 12  //@array2
                        
                        dataIsNullArrayFromHost_[i] = 0; //@nullelem null array
                    }
                }else if(descType == 0x9912){
                    //non-array
                    dataIsArrayFromHost_[i] = 0; //@nullelem non-array type
                    
                    int dataLen = BinaryConverter.byteArrayToInt (rawBytes, colDescs + 4);
                    dataLenAll += dataLen;
                    indicatorLenAll += indicatorSize_;
                    indicatorOffsetsFromHost_[i] =  (i == 0 ? 0 : indicatorOffsetsFromHost_[i-1] + (indicatorCountsFromHost_[i-1] * indicatorSize_));
                    indicatorCountsFromHost_[i] = 1;  
                    dataOffsetsFromHost_[i] = (i == 0 ? 0 : dataOffsetsFromHost_[i-1] + dataLengthsFromHost_[i-1]); //keep same as prev since null arrays have no data (so we can later keep a running total)(ie no -1 values)
                    dataLengthsFromHost_[i] = dataLen;
                    colDescs += 8; //0x9911=2, datatype=2, datalen=4 -> 8
                
                }
            }
          
           
            //headerLength is len of all non-relevant datastream data and count and 9911s and 9912s (not array header portion only!)
            headerLength = colDescs - offset_  ; //above, colDescs did not start at 0, just decrement offset
            //Here, lengthFromHost_ is length of all the indicators and data
            lengthFromHost_ = headerLength + indicatorLenAll + dataLenAll; //total size of x382f/x3901  
                                                                                       
            rawBytes_           = rawBytes;
            headerOffsetFromHost_    = offset_;
            indicatorOffsetFromHost_    = offset_ + headerLength;
            dataOffsetFromHost_         = indicatorOffsetFromHost_ + indicatorLenAll; //@array start of data in rawBytes[]  
            //Note: end of stream offset= offset_ + lengthFromHost_
          
        }
        else {
            //input array
            //This part is when sending input parms x382f
            rawBytes_           = rawBytes;
            //offset_ is start of header block (including non array info not used)     
            offset_ = offset; //index into rawBytes_[] (toHost)
            headerCurOffsetToHost_ = offset_ + actual382fInArrayStart_;  //initialize to 4 for consistency token 
            setColumnCount (columnCount_); 

            indicatorCurOffsetToHost_ = offset_ + actual382fInArrayStart_ + headerTotalSizeToHost_;   //index into rawBytes_[] (toHost)
            dataCurOffsetToHost_ = indicatorCurOffsetToHost_ + indicatorTotalSizeToHost_;  //index into rawBytes_[] (toHost)
        }
    }



    public int getLength ()
    {
        if(dataTotalSizeToHost_ == -1)
            return lengthFromHost_; //total stream length
        else
            return  actual382fInArrayStart_ + headerTotalSizeToHost_ + indicatorTotalSizeToHost_ + dataTotalSizeToHost_;
    }



    public int getConsistencyToken ()
    {
        //from output stream
        return BinaryConverter.byteArrayToInt (rawBytes_, offset_);
    }



    public int getRowCount ()
    {
        return rowCount_;
    }



    public int getColumnCount ()
    {
        return columnCount_;
    }



    public int getIndicatorSize ()
    {
        return indicatorSize_;
    }



    public int getRowSize ()
    {
        //@array no-op
        return -1;
    }



    public int getIndicator (int rowIndex, int columnIndex)
    {
        return getIndicator(rowIndex, columnIndex, -1);  //@nullelem -1 is for getting indicator of array (not element) or non-array data type
    }



    public int getRowDataOffset (int rowIndex)
    {
        //return offset to start of row.  In our case we only have 1 row, so we can ignore rowIndex. (for now at least)
        //@array
        if(dataCurOffsetToHost_ == -1)
            return dataOffsetFromHost_;      
        else
            return dataCurOffsetToHost_; //Note that this index may increment with each write to the byte array. (but currently does not)
        
    }



    public byte[] getRawBytes ()
    {
        //@array 
        return rawBytes_;
    }



    public void setConsistencyToken (int consistencyToken)
    {
        //same for both input and output
        BinaryConverter.intToByteArray (consistencyToken, rawBytes_,
                offset_); //@array
    }



    public void setRowCount (int rowCount)
    {
        //@array no-op 
        
    }



    public void setColumnCount (int columnCount)
    {
        //input parm offset only
        BinaryConverter.shortToByteArray ((short) columnCount, rawBytes_,
                headerCurOffsetToHost_); //@array
        headerCurOffsetToHost_ += 2; //increment to location of first 9911/9912
    }



    public void setIndicatorSize (int indicatorSize)
    {
        //@array no-op
    }



    public void setRowSize (int rowSize)
    {
        //@array no-op
    }

    /**
     * 
     * @param columnIndex
     * @param type  type of column
     * @param indicatorValue 
     * @param elementDataType  if type is array, then type of elements in array
     * @param dataTypeSize  size of data (if array, then array length * element datatype size)
     * @param arrayLength
     */
    public void setHeaderColumnInfo (int columnIndex, short type, short indicatorValue, short elementDataType, int dataTypeSize, short arrayLength)
    {
        //3 possibilities:
        //1 Array = ‘9911'x ,  data type 2 bytes, data length 4 bytes, # of data items 4 bytes
        //2 Null Array = ‘9911'x , FFFF  (9911 + indicator)
        //3 NonArray = ‘9912'x ,  data type 2 bytes, data length 4 bytes
        if(type == SQLData.NATIVE_ARRAY)
        {
            if(indicatorValue == 0)
            {
                //1
                BinaryConverter.shortToByteArray ((short) 0x9911,
                        rawBytes_, headerCurOffsetToHost_);
                headerCurOffsetToHost_ += 2; //increment 2 bytes
                
                //set datatype of element
                BinaryConverter.shortToByteArray (elementDataType,
                        rawBytes_, headerCurOffsetToHost_);
                headerCurOffsetToHost_ += 2; //increment 2 bytes
               
                //set element data size
                BinaryConverter.intToByteArray (dataTypeSize,
                        rawBytes_, headerCurOffsetToHost_);
                headerCurOffsetToHost_ += 4; //increment 2 bytes
                
                //set array length
                BinaryConverter.intToByteArray (arrayLength,
                        rawBytes_, headerCurOffsetToHost_);
                headerCurOffsetToHost_ += 4; //increment 4 bytes //@array2
                
            }
            else
            {
                //2
                BinaryConverter.shortToByteArray ((short) 0x9911,
                        rawBytes_, headerCurOffsetToHost_);
                headerCurOffsetToHost_ += 2; //increment 2 bytes
                
                //set indicator value of array (not elements)
                BinaryConverter.shortToByteArray ((short) indicatorValue,
                        rawBytes_, headerCurOffsetToHost_);
                headerCurOffsetToHost_ += 2; //increment 2 bytes
                
                
            }
            
        }
        else
        {
            
            //3 non array type
            BinaryConverter.shortToByteArray ((short) 0x9912,
                    rawBytes_, headerCurOffsetToHost_);
            headerCurOffsetToHost_ += 2; //increment 2 bytes
            
            //set datatype of column
            BinaryConverter.shortToByteArray (type,
                    rawBytes_, headerCurOffsetToHost_);
            headerCurOffsetToHost_ += 2; //increment 2 bytes
           
            //set length of data
            BinaryConverter.intToByteArray (dataTypeSize,
                    rawBytes_, headerCurOffsetToHost_);
            headerCurOffsetToHost_ += 4; //increment 2 bytes
            
            
        }
        
    }


    public void setIndicator (int rowIndex, int columnIndex, int indicator)
    {
        //Call this iteratively for each element in the array. (for both null and non-null so that indicatorCurOffsetToHost_ will increment properly)
        //We do not know anything about where to set the indicator for a given column since array lengths vary
   
        //A rowIndex of -1 tells us that the array is null. (need to update inside of header region id flow 
        //0x9911FFFF to 0x382F datastream)
        if(rowIndex == -1)
        {
            //set header 9911ffff
            //If array is null we set the indicator in the header part of the stream.
            //This means that we must also set the header part at the same time.  So call setHeaderColumnInfo()
            setHeaderColumnInfo(columnIndex, (short)SQLData.NATIVE_ARRAY, (short)-1, (short)0, 0, (short)0); 
            //just need to set header for null arrays
        }
        else
        {
            //this sets indicator for both array elements and for non-array single data types
            BinaryConverter.shortToByteArray ((short) indicator,
                    rawBytes_, indicatorCurOffsetToHost_);
            indicatorCurOffsetToHost_ += 2; //increment 2 bytes
        }
    }


     
    /*
    Specifies if variable-length fields are compressed.
    @return true if variable-length fields are compressed, false otherwise.
     */
    public boolean isVariableFieldsCompressed()
    {
        return false;
    }

    // Resets the number of rows to the total number of rows minus the number of rows that contain aliases.
    // This method is called by AS400JDBCDatabaseMetaData.parseResultData().
    public void resetRowCount(int rowCount){  
        //@array no-op
    }

    // Sets the number of aliases the result data contains.
    // This method is called by AS400JDBCDatabaseMetaData.parseResultData().
    public void setAliasCount(int aliases){     
        //@array no-op
    }
    

    //@array
    //Returns indicator (-1 array index means to get indicator of array or non-array data) 0-x means indicator of that elem in the array
    public int getIndicator (int rowIndex, int columnIndex, int arrayIndex){
        if(arrayIndex == -1) //@nullelem
        {
            if( dataIsArrayFromHost_[columnIndex] == 0)
            {
                //non array
                return BinaryConverter.byteArrayToShort (rawBytes_,
                        indicatorOffsetFromHost_ + indicatorOffsetsFromHost_[columnIndex] ); //first elem in indicatorOffsetsFromHost_ is for non-array data 
            }else
            {
                return dataIsNullArrayFromHost_[columnIndex]; //@nullelem already set above in this array
                //is array
               /* if (  indicatorCountsFromHost_[columnIndex] == 0)  //@nullelem  
                    return -1;  //array type and it is null (special case)  (whole array is null, no elements)
                else
                { 
                    //not null
                    return 0;
                }*/
            }
        }
        else //get indicator of array element 
        {
            return BinaryConverter.byteArrayToShort (rawBytes_,
                    indicatorOffsetFromHost_ + indicatorOffsetsFromHost_[columnIndex] + (arrayIndex * indicatorSize_)); //get indicator of array elem
        }
    }

    //@array
    //returns size footprint of data in column
    public int getDataTotalSize (int colIndex){
        return dataLengthsFromHost_[colIndex];
    }
    
    public int[] getDataLengthsFromHost(){
        return dataLengthsFromHost_;
    }

    public int[] getIndicatorCountsFromHost(){
        return indicatorCountsFromHost_;
    }
    
    
    
}

