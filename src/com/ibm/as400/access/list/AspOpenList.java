///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  AspOpenList.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 2018-2019 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////


package com.ibm.as400.access.list;

import java.io.IOException;
import com.ibm.as400.access.AS400;
import com.ibm.as400.access.AS400Exception;
import com.ibm.as400.access.AS400SecurityException;
import com.ibm.as400.access.BinaryConverter;
import com.ibm.as400.access.CharConverter;
import com.ibm.as400.access.ErrorCompletingRequestException;
import com.ibm.as400.access.ExtendedIllegalArgumentException;
import com.ibm.as400.access.ObjectDoesNotExistException;
import com.ibm.as400.access.ProgramCall;
import com.ibm.as400.access.ProgramParameter;
import com.ibm.as400.access.Trace;

/**
An {@link com.ibm.as400.access.list.OpenList OpenList} implementation that generates lists of {@link com.ibm.as400.access.list.AspListItem AspListItem} objects.
<pre>
   AS400 system = new AS400("mySystem", "myUserID", "myPassword");
   //Return all ASPs list with format FORMAT_0100(YASP0100)
   AspOpenList list = new AspOpenList(system);
   list.open();
   Enumeration items = list.getItems();
   while (items.hasMoreElements())
   {
       AspListItem item = (AspListItem)items.nextElement();
       System.out.println(item.getASPNumber() + "/" + item.getResourceName() + "/" + item.getVersion() + " - " + item.getASPUsage() + ", " + item.getASPStatus());
   }
   list.close();
</pre>
**/

public class AspOpenList extends OpenList {

	private static final long serialVersionUID = -7701422559222997434L;
	
	/**
    Constant indicating that this list will accept parameters for, and, generate SpooledFileListItem objects in accordance with, the YASP0100 format of the underlying API.
    @see  #setFormat
    **/
   public static final String FORMAT_0100 = "YASP0100";
   
   /**
   Constant indicating that this list will accept parameters for, and, generate SpooledFileListItem objects in accordance with, the YASP0200 format of the underlying API.
   @see  #setFormat
   **/
   public static final String FORMAT_0200 = "YASP0200";
   
   /**
   Constant indicating that this list will accept parameters for, and, generate SpooledFileListItem objects in accordance with, the YASP0300 format of the underlying API.
   @see  #setFormat
   **/
   public static final String FORMAT_0300 = "YASP0300";
   
   /**
   Constant indicating that this list will accept parameters for, and, generate SpooledFileListItem objects in accordance with, the YASP0400 format of the underlying API.
   @see  #setFormat
   **/
   public static final String FORMAT_0400 = "YASP0400";
   
   /**
   Constant indicating that this list will accept parameters for, and, generate SpooledFileListItem objects in accordance with, the YASP0500 format of the underlying API.
   @see  #setFormat
   **/
   public static final String FORMAT_0500 = "YASP0500";
   
   /**
   Constant indicating that this list will accept parameters for, and, generate SpooledFileListItem objects in accordance with, the YASP0600 format of the underlying API.
   @see  #setFormat
   **/
   public static final String FORMAT_0600 = "YASP0600";
   
   /**
    * Content indicate what kind of ASP or what ASP information is returned.
    * @see #setFilterKey(String)
    */
   //Filter by an ASP information
   public static final String ASP_NUMBER = "ASPN_UMBER"; 
   public static final String RESOURCE_NAME = "RESOURCE_NAME";
   public static final String DEVICEDESCRIPTION_NAME = "DEVICEDESCRIPTION_NAME";
   public static final String DATABASE_NAME = "DATABASE_NAME";
   public static final String UNASSIGNED_DISK = "UNASSIGNED_DISK"; //filter data is 0, return all unassigned disk.
   //Filter by ASP type.
   public static final String ALL_ASP_SELECTED = "ALL_ASP_SELECTED";
   public static final String ALL_USER_ASP_SELECTED = "ALL_USER_ASP_SELECTED";
   public static final String ALL_IASP_SELECTED = "ALL_IASP_SELECTED";
   
   
   // Use an enumeration to represent the format value.
   private int format_ = 1;
   //ASP resource name or asp device description name
   private String aspName_;
   //ASP number
   private int aspNumber_;
   //ASP filter key, No filter or filter ASP number, it is 1.
   private int filterKey_ = 1;
   private int filterNumber = 1;
   private byte[] filterNumberbyte_;
   private byte[] formatBytes_;
   private String filterDataType_;
   
   /**
   Constructs a AspOpenList object with the given system.  By default, this list will generate a list of AspOpenList objects for all ASPs on the system using the default format of {@link #FORMAT_0100 FORMAT_0100}.
   @param  system  The system object representing the system on which the ASP exists
   **/
  public AspOpenList(AS400 system)
  {
      super(system);
      if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Constructing AspOpenList object.");
  }
  
  /**
  Constructs a AspOpenList object with the given system. this list will generate a list of AspOpenList objects for all ASPs on the system using the format.
  @param  system  The system object representing the system on which the ASP exists
  @param  format  The format of ASP information being returned. Support: YASP0100, YASP0200, YASP0300, YASP0400, YASP0500, YASP0600 and UNASSIGNED_DISK
  @see    #setFormat
  **/
  public AspOpenList(AS400 system, String format)
  {
      super(system);
      if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Constructing AspOpenList object.");
      setFormat(format);
  }
  
  /**
  Constructs a AspOpenList object with the given system, format, filter data type. This list will generate a list of AspOpenList objects for specific type ASPs on the system using the format.
  @param  system  The system object representing the system on which the ASP exists
  @param  format  The format of ASP information being returned. Possible values are: FORMAT_0100, FORMAT_0200, FORMAT_0300, FORMAT_0400, FORMAT_0500 and FORMAT_0600
  @param  filterDataType  Filter data type for filter information. Possible values are: ALL_ASP_SELECTED, ALL_USER_ASP_SELECTED and ALL_IASP_SELECTED.
  @see    #setFormat
  **/
  public AspOpenList(AS400 system, String format, String filterDataType)
  {
      super(system);
      if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Constructing AspOpenList object.");
      setFormat(format);
      setFilterKey(filterDataType);
  }
  
  /**
   Constructs a AspOpenList object with the given system, format, ASP number. this list will generate a list of AspOpenList objects for the ASP Number using the format.
   @param  system    The system object representing the system on which the ASP exists
   @param  format    The format of ASP information being returned. Possible values are: FORMAT_0100, FORMAT_0200, FORMAT_0300, FORMAT_0400, FORMAT_0500 and FORMAT_0600
   @param  aspNumber the ASP number.
   @see    #setFormat
   @see    #setASPNumber
   **/
  public AspOpenList(AS400 system, String format, int aspNumber)
  {
      super(system);
      if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Constructing AspOpenList object.");
      setASPNumber(aspNumber);
      setFormat(format);
  } 
  
  /**
   Constructs a AspOpenList object with the given system, format, filterDataType and ASP name. this list will generate a list of AspOpenList objects for the ASP name using the format.
   @param  system           The system object representing the system on which the ASP exists
   @param  format           The format of ASP information being returned. Possible values are: FORMAT_0100, FORMAT_0200, FORMAT_0300, FORMAT_0400, FORMAT_0500 and FORMAT_0600
   @param  filterDataType   The ASP filter data type, Possible values are: RESOURCE_NAME, DEVICE_DESCRIPTION_NAME and DATABASE_NAME
   @param  name             The ASP name
   @see    #setFormat
   @see    #setFilterKey
   @see    #setASPName
   **/
  public AspOpenList(AS400 system, String format, String filterDataType, String name)
  {
      super(system);
      if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Constructing AspOpenList object.");
      setFormat(format);
      setFilterKey(filterDataType);
      setASPName(name);
  }
  
  /**
  Calls QGY/QYASPOL.
  @return  The list information parameter.
  **/
 protected byte[] callOpenListAPI() throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
 {
	 if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "Opening APSs list.");

     // Convert based on server job CCSID.
     CharConverter conv = new CharConverter(system_.getCcsid(), system_);
     
     //Filter Information Length (Size of filter entry), the smallest size is 16.
     if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "The filter key of filter information is " + filterKey_);
     int filterInformationLength = 16;
     int filterDataSize = 4;
     if (filterKey_ == 4) {
    	 filterInformationLength = 32;
    	 filterDataSize = 18;
     } else if (filterKey_ == 2 || filterKey_ == 3 ) {
    	 filterInformationLength = 24;
    	 filterDataSize = 10;
     }
     
     if (Trace.isTraceOn()) Trace.log(Trace.DIAGNOSTIC, "The filter information length " + filterInformationLength);
     
     //Filter Number, filter number must >= 1.
     filterNumberbyte_ = BinaryConverter.intToByteArray(filterNumber);

     int offset = 0;
     byte[] filterInformation_ = new byte[filterInformationLength];
     
     //Filter Information
	 BinaryConverter.intToByteArray(filterInformationLength, filterInformation_, offset);
	 offset += 4;
	 BinaryConverter.intToByteArray(filterKey_, filterInformation_, offset);
	 offset += 4;
	 BinaryConverter.intToByteArray(filterDataSize, filterInformation_, offset);
	 offset += 4;
    	 if (filterDataType_ == null || filterDataType_.length() == 0 || filterDataType_.equals(ALL_ASP_SELECTED)) {
    		 byte[] allAsp = new byte[] { (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF };
    		 System.arraycopy(allAsp, 0, filterInformation_, offset, 4);
    	 } else if (filterDataType_.equals(ALL_IASP_SELECTED)) {
    		 byte[] allAsp = new byte[] { (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFD };
    		 System.arraycopy(allAsp, 0, filterInformation_, offset, 4);
    	 } else if (filterDataType_.equals(ALL_USER_ASP_SELECTED)) {
    		 byte[] allAsp = new byte[] { (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFE };
    		 System.arraycopy(allAsp, 0, filterInformation_, offset, 4);
    	 } else if (filterDataType_.equals(UNASSIGNED_DISK)) {   //Filter key is 1, Format is FORMAT_0300 and filer data is 0;
    		 byte[] allAsp = new byte[] { (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00 };
    		 System.arraycopy(allAsp, 0, filterInformation_, offset, 4);
    	 } else if (filterDataType_.equals(ASP_NUMBER)) {
    		 BinaryConverter.intToByteArray(aspNumber_, filterInformation_, offset);
    	 } else if (filterDataType_.equals(DATABASE_NAME)) {
    		 conv.stringToByteArray(aspName_, filterInformation_, offset, 18);
    	 } else if (filterDataType_.equals(DEVICEDESCRIPTION_NAME) || filterDataType_.equals(RESOURCE_NAME)) {
    		 conv.stringToByteArray(aspName_, filterInformation_, offset, 10);
    	 } else {  //Never happened.
    		 Trace.log(Trace.ERROR, "Error occur when set filter information");
    	 }

     formatBytes_ = new byte[] {(byte)0xE8, (byte)0xC1, (byte)0xE2, (byte)0xD7, (byte)0xF0, (byte)(0xF0 | format_), (byte)0xF0, (byte)0xF0 };  //KC: EBCDIC collating sequence
     
     // Setup program parameters.
     ProgramParameter[] parameters = new ProgramParameter[8];
     // Receiver variable, output, char(*).
     parameters[0] = new ProgramParameter(0);
     // Length of receiver variable, input, binary(4).
     parameters[1] = new ProgramParameter(new byte[] { 0x00, 0x00, 0x00, 0x00 } );
     // List information, output, char(80).
     parameters[2] = new ProgramParameter(80);
     // Number of records to return, input, binary(4).
     parameters[3] = new ProgramParameter(new byte[] { (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF } );
     // Number of Filter, input, binary(4).
     parameters[4] = new ProgramParameter(filterNumberbyte_);
     // Filter information, input, char(*).
     parameters[5] = new ProgramParameter(filterInformation_);
     // Format of the generated list, input, char(8).
     parameters[6] = new ProgramParameter(formatBytes_);
     // Error code, I/O, char(*).
     parameters[7] = EMPTY_ERROR_CODE_PARM;
     // Sort Information
     //parameters[8] = new ProgramParameter(sortInformation)
     
     // Call the program.
     ProgramCall pc = new ProgramCall(system_, "/QSYS.LIB/QGY.LIB/QYASPOL.PGM", parameters);
     if (!pc.run())
     {
         throw new AS400Exception(pc.getMessageList());
     }

     // List information returned.
     return parameters[2].getOutputData();
 }
 
 /**
  Set ASP number for filter information, filterkey sets to 1.
  @param aspNumber
  **/
 public void setASPNumber(int aspNumber) {
	 if (aspNumber > 0 ) {
		 aspNumber_ = aspNumber;
		 filterKey_ = 1;
		 filterDataType_ = ASP_NUMBER;
	 } else {
		 Trace.log(Trace.ERROR, "Value of parameter 'aspNumber' is not valid: " + aspNumber);
         throw new ExtendedIllegalArgumentException("aspNumber (" + aspNumber + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
	 }
 }
 
 /**
  Set format of the ASP information being returned.
  <ul>
    <li> {@link #FORMAT_0100} - YASP0100
    <li> {@link #FORMAT_0200} - YASP0200
    <li> {@link #FORMAT_0300} - YASP0300
    <li> {@link #FORMAT_0400} - YASP0400
    <li> {@link #FORMAT_0500} - YASP0500
    <li> {@link #FORMAT_0600} - YASP0600
    <li> {@link #UNASSIGNED_DISK} - YASP0300 and filter data is 0
  </ul>
  * @param format
  */
 public void setFormat(String format) {
	 if (format == null) {
		 Trace.log(Trace.ERROR, "Parameter 'format ' is null.");
         throw new NullPointerException("format");
	 }
	 
	 if (format.equalsIgnoreCase(FORMAT_0100)) {
		 format_ = 1;
	 } else if (format.equalsIgnoreCase(FORMAT_0200)) {
		 format_ = 2;
	 } else if (format.equalsIgnoreCase(FORMAT_0300)) {
		 format_ = 3;
	 } else if (format.equalsIgnoreCase(FORMAT_0400)) {
		 format_ = 4;
	 } else if (format.equalsIgnoreCase(FORMAT_0500)) {
		 format_ = 5;
	 } else if (format.equalsIgnoreCase(FORMAT_0600)) {
		 format_ = 6;
	 } else if (format.equalsIgnoreCase(UNASSIGNED_DISK)) {
		 format_ = 3;
		 filterDataType_ = UNASSIGNED_DISK;
	 } else {
		 Trace.log(Trace.ERROR, "Value of parameter 'format' is not valid: " + format);
         throw new ExtendedIllegalArgumentException("format (" + format + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
	 }
 }
 
 /**
  Set filter key for the filter information, Possible values are:
  <ul>
    <li> 1 - When filter data type is {@link #ALL_ASP_SELECTED}, {@link #ALL_IASP_SELECTED}, {@link #ALL_USER_ASP_SELECTED}, {@link #UNASSIGNED_DISK}, {@link #ASP_NUMBER}
    <li> 2 - When filter data type is {@link #RESOURCE_NAME}
    <li> 3 - When filter data type is {@link #DEVICEDESCRIPTION_NAME}
    <li> 4 - When filter data type is {@link #DATABASE_NAME}
  </ul>
  @param filterDataType
  @see {@link #setASPNumber(int)} when setFilterKey({@link #ASP_NUMBER})
  @see {@link #setASPName(String)} when setFilterKey({@link #RESOURCE_NAME}) or setFilterKey({@link #DEVICEDESCRIPTION_NAME}) or setFilterKey({@link #DATABASE_NAME})
  **/
 public void setFilterKey(String filterDataType) {
	 if (filterDataType == null) {
		 Trace.log(Trace.ERROR, "Parameter 'filterDataType ' is null.");
         throw new NullPointerException("filterDataType");
	 }
	 filterDataType_ = filterDataType.toUpperCase().trim();
	 if (filterDataType_.equals(ALL_ASP_SELECTED) || filterDataType_.equals(ALL_IASP_SELECTED) || 
			 filterDataType_.equals(ALL_USER_ASP_SELECTED) || filterDataType_.equals(UNASSIGNED_DISK) || filterDataType_.equals(ASP_NUMBER)) {
		 filterKey_ = 1;
	 } else if (filterDataType_.equals(RESOURCE_NAME) ) {
		 filterKey_ = 2;
	 } else if (filterDataType_.equals(DEVICEDESCRIPTION_NAME)) {
		 filterKey_ = 3;
	 } else if (filterDataType_.equals(DATABASE_NAME)) {
		 filterKey_ = 4;
	 } else {
		 Trace.log(Trace.ERROR, "Parameter 'filterDataType ' is not valid:" + filterDataType +", When filter set to YES");
		 throw new ExtendedIllegalArgumentException("filterDataType (" + filterDataType + ")", ExtendedIllegalArgumentException.PARAMETER_VALUE_NOT_VALID);
	 }
 }
 
 /**
  Set ASP Name for filter information.
  @param name The ASP name
  <ul>
    <li> ASP resource name
    <li> ASP device description name
    <li> database name
  </ul>
  @see #setFilterKey(String)
  **/
 public void setASPName(String name) {
	 if (name == null) {
		 Trace.log(Trace.ERROR, "Parameter 'ASP name for filter ' is null.");
         throw new NullPointerException("name");
	 }
	 aspName_ = name;
	 //ASP name request 10 characters, for high level Java: aspName_ = String.format("%1$-10s",aspName);
	 if (name.length() < 10) {
		 for (int i=0; i < (10 - name.length()); i++) {
			 aspName_ +=" ";
		 }
	 }
	 Trace.log(Trace.INFORMATION, "set ASP Name length is " + aspName_); 
 }
 
 /**
 Returns the format currently in use by this open list.  Possible values are:
 <ul>
 <li>{@link #FORMAT_0100 FORMAT_0100}
 <li>{@link #FORMAT_0200 FORMAT_0200}
 <li>{@link #FORMAT_0300 FORMAT_0300}
 <li>{@link #FORMAT_0400 FORMAT_0400}
 <li>{@link #FORMAT_0500 FORMAT_0500}
 <li>{@link #FORMAT_0600 FORMAT_0600}
 </ul>
 @return  The format.  The default format is FORMAT_0100.
 **/
 public String getASPFormat() {
	 switch (format_)
     {
         case 1:  return FORMAT_0100;
         case 2:  return FORMAT_0200;
         case 3:  return FORMAT_0300;
         case 4:  return FORMAT_0400;
         case 5:  return FORMAT_0500;
         case 6:  return FORMAT_0600;
     }
     return FORMAT_0100;
 }
 
 /**
 Formats the data from QGY/QGYOLSPL.
 **/
 protected Object[] formatOutputData(byte[] data, int recordsReturned, int recordLength) throws AS400SecurityException, ErrorCompletingRequestException, InterruptedException, IOException, ObjectDoesNotExistException
 {
     // Convert based on server job CCSID.
     CharConverter conv = new CharConverter(system_.getCcsid(), system_);
     
     AspListItem[] aspList = new AspListItem[recordsReturned];
     int offset = 0;
     for (int i = 0; i < recordsReturned; ++i)
     {
    	 int aspNumber = BinaryConverter.byteArrayToInt(data, offset);
    	 offset += 4;
    	 if (format_ == 1) {
    		 String aspResourceName = conv.byteArrayToString(data, offset, 10).trim();
    		 offset += 10;
    		 String aspDevDescriptionName = conv.byteArrayToString(data, offset, 10).trim();
    		 offset += 10;
    		 int aspVersion = BinaryConverter.byteArrayToInt(data, offset);
    		 offset += 4;
    		 int aspUsage = BinaryConverter.byteArrayToInt(data, offset);
    		 offset += 4;
    		 int aspStatus = BinaryConverter.byteArrayToInt(data, offset);
    		 offset += 4;
    		 String aspDatabaseName = conv.byteArrayToString(data, offset, 18).trim();
    		 offset += 18;
    		 String primaryASPResourceName = conv.byteArrayToString(data, offset, 10).trim();
    		 offset += 10;
    		 aspList[i] = new AspListItem(aspNumber, aspResourceName, aspDevDescriptionName, aspVersion, aspUsage, aspStatus, aspDatabaseName, primaryASPResourceName);
         } else if (format_ == 2) {
        	 int diskNumber = BinaryConverter.byteArrayToInt(data, offset);
        	 offset += 4;
        	 int aspCapacity = BinaryConverter.byteArrayToInt(data, offset);
        	 offset += 4;
        	 int aspCapacityAvailable = BinaryConverter.byteArrayToInt(data, offset);
        	 offset += 4;
        	 int aspCapacityProtected = BinaryConverter.byteArrayToInt(data, offset);
        	 offset += 4;
        	 int aspCapacityAvaProtected = BinaryConverter.byteArrayToInt(data, offset);
        	 offset += 4;
        	 int aspCapacityUnprotected = BinaryConverter.byteArrayToInt(data, offset);
        	 offset += 4;
        	 int aspCapacityAvaUnprotected = BinaryConverter.byteArrayToInt(data, offset);
        	 offset += 4;
        	 int aspSystemStorage = BinaryConverter.byteArrayToInt(data, offset);
        	 offset += 4;
        	 int OverflowStorage = BinaryConverter.byteArrayToInt(data, offset);
        	 offset += 4;
        	 int space4ErrorLog = BinaryConverter.byteArrayToInt(data, offset);
        	 offset += 4;
        	 int space4MachineLog = BinaryConverter.byteArrayToInt(data, offset);
        	 offset += 4;
        	 int space4MachineTreac = BinaryConverter.byteArrayToInt(data, offset);
        	 offset += 4;
        	 int space4MainStoragedump = BinaryConverter.byteArrayToInt(data, offset);
        	 offset += 4;
        	 int space4Microcode = BinaryConverter.byteArrayToInt(data, offset);
        	 offset += 4;
        	 int storageThresholdPer = BinaryConverter.byteArrayToInt(data, offset);
        	 offset += 4;
        	 String aspType = conv.byteArrayToString(data, offset, 2).trim();
    		 offset += 2;
    		 String overflowRecovery = conv.byteArrayToString(data, offset, 1).trim();
    		 offset += 1;
    		 String endImmeControl = conv.byteArrayToString(data, offset, 1).trim();
    		 offset += 1;
    		 String compressionRecoveryPolicy = conv.byteArrayToString(data, offset, 1).trim();
    		 offset += 1;
    		 String compressedDiskUnitInASP = conv.byteArrayToString(data, offset, 1).trim();
    		 offset += 1;
    		 String balanceStatus = conv.byteArrayToString(data, offset, 1).trim();
    		 offset += 1;
    		 String balanceType = conv.byteArrayToString(data, offset, 1).trim();
    		 offset += 1;
    		 String balanceDateTime = conv.byteArrayToString(data, offset, 13).trim();
    		 offset += 16;  //Reserved BINARY(3)
    		 int balanceDataMoved = BinaryConverter.byteArrayToInt(data, offset);
        	 offset += 4;
        	 int balanceDataRemain = BinaryConverter.byteArrayToInt(data, offset);
        	 offset += 4;
        	 int traceDuration = BinaryConverter.byteArrayToInt(data, offset);
        	 offset += 4;
        	 String traceStatus = conv.byteArrayToString(data, offset, 1).trim();
    		 offset += 1;
    		 String traceDateTime = conv.byteArrayToString(data, offset, 13).trim();
    		 offset += 13;
    		 String changesWritten2Disk = conv.byteArrayToString(data, offset, 1).trim();
    		 offset += 2;   //Reserved BINARY(1)
    		 int multiConnDiskUnit = BinaryConverter.byteArrayToInt(data, offset);
        	 offset += 4;
        	 int geographicMirrorRole = BinaryConverter.byteArrayToInt(data, offset);
        	 offset += 4;
        	 int geographicMirrorCpStat = BinaryConverter.byteArrayToInt(data, offset);
        	 offset += 4;
        	 int geographicMirrorCpDataStat = BinaryConverter.byteArrayToInt(data, offset);
        	 offset += 4;
        	 int geographicMirrorPerfMode = BinaryConverter.byteArrayToInt(data, offset);
        	 offset += 4;
        	 int geographicMirrorResumePriority = BinaryConverter.byteArrayToInt(data, offset);
        	 offset += 4;
        	 int geographicMirrorSuspendTimeout = BinaryConverter.byteArrayToInt(data, offset);
        	 offset += 4;
        	 int diskUnitpresence = BinaryConverter.byteArrayToInt(data, offset);
        	 offset += 4;
        	 aspList[i] = new AspListItem(aspNumber,diskNumber,aspCapacity,aspCapacityAvailable,aspCapacityProtected,aspCapacityAvaProtected,aspCapacityUnprotected,aspCapacityAvaUnprotected,aspSystemStorage,OverflowStorage,space4ErrorLog,space4MachineLog,space4MachineTreac,space4MainStoragedump,space4Microcode,storageThresholdPer,aspType,overflowRecovery,endImmeControl,compressionRecoveryPolicy,compressedDiskUnitInASP,balanceStatus,balanceType,balanceDateTime,balanceDataMoved,balanceDataRemain,traceDuration,traceStatus,traceDateTime,changesWritten2Disk,multiConnDiskUnit,geographicMirrorRole,geographicMirrorCpStat,geographicMirrorCpDataStat,geographicMirrorPerfMode,geographicMirrorResumePriority,geographicMirrorSuspendTimeout,diskUnitpresence);
 
         } else if (format_ == 3) {
        	 //int aspNumber = BinaryConverter.byteArrayToInt(data, offset);
        	 //offset += 4;
        	 String diskType = conv.byteArrayToString(data, offset, 4).trim();
        	 offset += 4;
        	 String diskModel = conv.byteArrayToString(data, offset, 4).trim();
        	 offset += 4;
        	 String diskSerialNumber = conv.byteArrayToString(data, offset, 10).trim();
        	 offset += 10;
        	 String diskResourceName = conv.byteArrayToString(data, offset, 10).trim();
        	 offset += 10;
        	 int diskUnitNumber = BinaryConverter.byteArrayToInt(data, offset);
        	 offset += 4;
        	 int diskCapacity = BinaryConverter.byteArrayToInt(data, offset);
        	 offset += 4;
        	 int diskStorageAvailable = BinaryConverter.byteArrayToInt(data, offset);
        	 offset += 4;
        	 int diskStorageReserved = BinaryConverter.byteArrayToInt(data, offset);
        	 offset += 4;
        	 String mirroredUnitProtected = conv.byteArrayToString(data, offset, 1).trim();
        	 offset += 1;
        	 String mirroredUnitReported = conv.byteArrayToString(data, offset, 1).trim();
        	 offset += 1;
        	 String mirroredUnitStatus = conv.byteArrayToString(data, offset, 1).trim();
        	 offset += 1;
        	 String RAIDType = conv.byteArrayToString(data, offset, 1).trim();
        	 offset += 1;
        	 int unitControl = BinaryConverter.byteArrayToInt(data, offset);
        	 offset += 4;
        	 int blockTransfer2MainStorage = BinaryConverter.byteArrayToInt(data, offset);
        	 offset += 4;
        	 int blockTransferFromMainStorage = BinaryConverter.byteArrayToInt(data, offset);
        	 offset += 4;
        	 int reqDataTransfer2MainStorage = BinaryConverter.byteArrayToInt(data, offset);
        	 offset += 4;
        	 int reqDataTransferFromMainStorage = BinaryConverter.byteArrayToInt(data, offset);
        	 offset += 4;
        	 int permanentBlockTransferFromMainStorage = BinaryConverter.byteArrayToInt(data, offset);
        	 offset += 4;
        	 int reqPermanentBlockTransferFromMainStorage = BinaryConverter.byteArrayToInt(data, offset);
        	 offset += 4;
        	 int sampleCount = BinaryConverter.byteArrayToInt(data, offset);
        	 offset += 4;
        	 int notBusyCount = BinaryConverter.byteArrayToInt(data, offset);
        	 offset += 4;
        	 String compressedStatus = conv.byteArrayToString(data, offset, 1).trim();
        	 offset += 1;
        	 String diskProtectionType = conv.byteArrayToString(data, offset, 1).trim();
        	 offset += 1;
        	 String compressedUnit = conv.byteArrayToString(data, offset, 1).trim();
        	 offset += 1;
        	 String stroageAllocationRestrictUnit = conv.byteArrayToString(data, offset, 1).trim();
        	 offset += 1;
        	 String avaParitySetUnit = conv.byteArrayToString(data, offset, 1).trim();
        	 offset += 1;
        	 String multiConnectionUnit = conv.byteArrayToString(data, offset, 1).trim();
        	 offset += 1;
        	 offset += 2;  //KC miss 2 
        	 aspList[i] = new AspListItem( aspNumber, diskType, diskModel, diskSerialNumber, diskResourceName, diskUnitNumber, diskCapacity, diskStorageAvailable, diskStorageReserved, mirroredUnitProtected, mirroredUnitReported, mirroredUnitStatus, RAIDType, unitControl, blockTransfer2MainStorage, blockTransferFromMainStorage,reqDataTransfer2MainStorage, reqDataTransferFromMainStorage, permanentBlockTransferFromMainStorage, reqPermanentBlockTransferFromMainStorage, sampleCount, notBusyCount, compressedStatus, diskProtectionType, compressedUnit, stroageAllocationRestrictUnit, avaParitySetUnit, multiConnectionUnit);
         } else if (format_ == 4) {
     		String errorCode = conv.byteArrayToString(data, offset, 4).trim();   //Only for iASP
     		offset += 4;
     		int diskUnitNumber = BinaryConverter.byteArrayToInt(data, offset);
     		offset += 4;
     		String mirrorUnitIdentifier = conv.byteArrayToString(data, offset, 1).trim();
     		offset += 1;
     		aspList[i] = new AspListItem( aspNumber, errorCode, diskUnitNumber, mirrorUnitIdentifier);
         } else if (format_ == 5) {
     		String tranfitionTarget = conv.byteArrayToString(data, offset, 2).trim();
     		offset += 4;   //Reserved 2
     		String function = conv.byteArrayToString(data, offset, 16).trim();
     		offset += 16;
     		int currentCount = BinaryConverter.byteArrayToInt(data, offset);
     		offset += 4;
     		int totalCount = BinaryConverter.byteArrayToInt(data, offset);
     		offset += 4;
     		int currentItemCount = BinaryConverter.byteArrayToInt(data, offset);
     		offset += 4;
     		int totalItemCount = BinaryConverter.byteArrayToInt(data, offset);
     		offset += 4;
     		String elapsedTime = conv.byteArrayToString(data, offset, 6);
     		offset += 6;
     		aspList[i] = new AspListItem( aspNumber, tranfitionTarget, function, currentCount, totalCount, currentItemCount, totalItemCount, elapsedTime);
         } else if (format_ == 6) {
         	int useIdentification = BinaryConverter.byteArrayToInt(data, offset);
         	offset += 4;
         	String jobName = conv.byteArrayToString(data, offset, 10).trim();
         	offset += 10;
         	String jobUserName = conv.byteArrayToString(data, offset, 10).trim();
         	offset += 10;
         	String jobNumber = conv.byteArrayToString(data, offset, 4).trim();
         	offset += 4;
         	String threadIdentifier = conv.byteArrayToString(data, offset, 8).trim();
         	offset += 8;
         	String threadStatus = conv.byteArrayToString(data, offset, 4).trim();
         	offset += 4;
         	
         	aspList[i] = new AspListItem( aspNumber, useIdentification, jobName, jobUserName, jobNumber, threadIdentifier, threadStatus);
         }
     }
     
     return aspList;
 }
 
 /**
 Returns receiver variable size based on format used.
 **/
 protected int getBestGuessReceiverSize(int number)
 {
    switch (format_)
    {
        case 1:  return 64 * number;
        case 2:  return 148 * number;
        case 3:  return 94 * number;
        case 4:  return 13 * number;
        case 5:  return 46 * number;
        case 6:  return 52 * number;
    }
    
    return 148 * number;
 }
 
 /**
  @return ASP number
  **/
 public int getAspNumber() {
	 return aspNumber_;
 }
 
 /**
  @return ASP name
  */
 public String getAspName() {
	 if (aspName_ == null) {
		 aspName_ = ""; 
	 }
	 return aspName_;
 }
 
 /**
  @return ASP filter key
  */
  public int getFilterKey() {
	  return filterKey_;
  }
  
  /**
   @return ASP filter data type
   */
  public String getFilterDataType() {
	  if (filterDataType_ == null) {
		  filterDataType_ = "";
	  }
	  return filterDataType_;
  }

}
