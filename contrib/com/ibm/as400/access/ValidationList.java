package com.ibm.as400.access;

import java.beans.PropertyVetoException;

/**
 * Represents a system validation list object.
 * <p>
 * Note: The ability to find and modify the list is dependent on
 * the access rights of the signed-on user profile to the
 * validation list.
 *
 * @author Thomas Johnson (tom.johnson@kingland.com), Kingland Systems Corporation
 */
public class ValidationList {

	private static int LISTBUFFER_LENGTH_INITIAL = 32768;  //32K
	private static int LISTBUFFER_LENGTH_NEXT    = 524288; //512K
	
	private AS400 as400_ = null;
	private AS400Structure listInfo_ = null;
	private AS400Structure vlde0100_ = null;
	private QSYSObjectPathName path_ = null;
	private String description_ = null;
/**
 * Constructs a validation list.
 * <p>
 * Note: The <i>AS400</i> and <i>Path</i> properties must be set prior to taking
 * action against the object.
 */
public ValidationList() {
	super();
}
/**
 * Constructs a validation list for the given system.
 * <p>
 * Note: The <i>Path</i> property must be set prior to taking action against
 * the object.
 *
 * @param as400
 *		com.ibm.as400.access.AS400
 *
 */
public ValidationList(AS400 as400) {
	this();
	setAS400(as400);
}
/**
 * Constructs a validation list for the given system and path.
 *
 * @param as400
 *		com.ibm.as400.access.AS400
 * @param path
 *		com.ibm.as400.access.QSYSObjectPathName
 *
 */
public ValidationList(AS400 as400, QSYSObjectPathName path) {
	this();
	setAS400(as400);
	setPath(path);
}
/**
 * Constructs a validation list for the given system
 * and object/library names.
 *
 * @param as400
 *		com.ibm.as400.access.AS400
 * @param obj
 *		java.lang.String
 * @param lib
 *		java.lang.String
 */
public ValidationList(AS400 as400, String obj, String lib) {
	this();
	setAS400(as400);
	setPath(new QSYSObjectPathName(lib, obj, "vldl"));
}
/**
 * Adds the entry to the validation list.
 *
 * @param entry
 *		ValidationListEntry
 * @exception PersistenceException
 *		If an error occurs while calling the AS/400 APIs.
 */
public void addEntry(ValidationListEntry entry) throws PersistenceException {
	ProgramCall pgm = new ProgramCall(getAS400());
	ProgramParameter[] parmList = new ProgramParameter[6];
	try {
		pgm.setProgram(QSYSObjectPathName.toPath("QSYS", "QSYADVLE", "PGM"), parmList);
	} catch (PropertyVetoException pve) { };

	// Refer to documentation for the QSYADVLE Security API for a complete description of parameters
	parmList[0] = getQualifiedNameParm();
	parmList[1] = new ProgramParameter(entry.getEntryID().toBytes());
	parmList[2] = new ProgramParameter(entry.getDataToEncrypt().toBytes());
	parmList[3] = new ProgramParameter(entry.getUnencryptedData().toBytes());
	parmList[4] = new ProgramParameter(entry.getAttributeInfo().toBytes());
	parmList[5] = new ProgramParameter(new AS400Bin4().toBytes(0));

	runProgram(pgm);
}
/**
 * Alters the entry in the validation list.
 *
 * @param entry
 *		ValidationListEntry
 * @exception PersistenceException
 *		If an error occurs while calling the AS/400 APIs.
 */
public void changeEntry(ValidationListEntry entry) throws PersistenceException {
	ProgramCall pgm = new ProgramCall(getAS400());
	ProgramParameter[] parmList = new ProgramParameter[6];
	try {
		pgm.setProgram(QSYSObjectPathName.toPath("QSYS", "QSYCHVLE", "PGM"), parmList);
	} catch (PropertyVetoException pve) { };
		
	// Refer to documentation for the QSYCHVLE Security API for a complete description of parameters
	parmList[0] = getQualifiedNameParm();
	parmList[1] = new ProgramParameter(entry.getEntryID().toBytes());
	parmList[2] = new ProgramParameter(entry.getDataToEncrypt().toBytes());
	parmList[3] = new ProgramParameter(entry.getUnencryptedData().toBytes());
	parmList[4] = new ProgramParameter(entry.getAttributeInfo().toBytes());
	parmList[5] = new ProgramParameter(new AS400Bin4().toBytes(0));
		
	runProgram(pgm);
}
/**
 * Close the current list of entries maintained on the system.
 * <p>
 * The list is opened by the initial call to the list API. In doing so, the entire
 * contents of the list is retrieved as a snapshot and stored in a buffer on the
 * AS/400 system. The list contents can then be retrieved from the pre-filled
 * buffer, in whole or in part, at the convenience of the application by using a
 * system-provided handle. The handle is used to close the list when the
 * application has completed processing of all entries.
 *
 * @param handle byte[]
 * @exception PersistenceException
 *		If an error occurs while calling the AS/400 APIs.
 */
private void closeList(byte[] handle) throws PersistenceException {
	ProgramCall pgm = new ProgramCall(getAS400());
	ProgramParameter[] parmList = new ProgramParameter[2];
	try {
		pgm.setProgram(QSYSObjectPathName.toPath("QGY", "QGYCLST", "PGM"), parmList);
	} catch (PropertyVetoException pve) { };

	// Refer to documentation for the QGYCLST generic list API for a complete description of parameters
	parmList[0] = new ProgramParameter(new AS400ByteArray(handle.length).toBytes(handle));
	parmList[1] = new ProgramParameter(new AS400Bin4().toBytes(0));

	runProgram(pgm);
}
/**
 * Creates the validation list on the AS/400 system.
 *
 * @exception PersistenceException
 *		If an error occurs while calling the AS/400 command.
 */
public void create() throws PersistenceException {
	String obj = getPath().getObjectName();
	String lib = getPath().getLibraryName();
	
	CommandCall cmd = new CommandCall(getAS400(),
		"CRTVLDL VLDL("+lib+"/"+obj+") TEXT('"+getDescription()+"')");
	runCommand(cmd);
}
/**
 * Deletes the validation list from the AS/400 system.
 *
 * @exception PersistenceException
 *		If an error occurs while calling the AS/400 command.
 */
public void delete() throws PersistenceException {
	String obj = getPath().getObjectName();
	String lib = getPath().getLibraryName();
	
	CommandCall cmd = new CommandCall(getAS400(),
		"DLTVLDL VLDL("+lib+"/"+obj+")");
	runCommand(cmd);
}
/**
 * Sets contents of the string buffer at the given offset from the specified string.
 *
 * @param buffer
 *		java.lang.StringBuffer
 * @param offset
 *		int
 * @param s
 *		java.lang.String
 */
private void fillStringBuffer(StringBuffer buffer, int offset, String s) {
	for (int i=0; i<s.length(); i++)
		buffer.setCharAt(i+offset, s.charAt(i));
}
/**
 * Returns an entry from the validation list with the given identifier.
 * <p>
 * The <i>ccsid</i> parameter indicates the ccsid used to store the identifier in the
 * entry on the AS/400 system. This apparently needs to be an exact match in order to
 * find the entry as it was originally inserted.
 * <p>
 * No attribute values are retrieved for the entry.
 *
 * @param identifier
 *		java.lang.String
 * @param ccsid
 *		int
 * @return
 *		ValidationListEntry
 * @exception PersistenceException
 *		If an error occurs while calling the AS/400 APIs.
 */
public ValidationListEntry findEntry(String identifier, int ccsid) throws PersistenceException {
	return findEntry(identifier, ccsid, new ValidationListAttribute[0]);
}
/**
 * Returns an entry from the validation list with the given identifier and attributes.
 * <p>
 * The <i>ccsid</i> parameter indicates the ccsid used to store the identifier in the
 * entry on the AS/400 system. This apparently needs to be an exact match in order to
 * find the entry as it was originally inserted.
 * <p>
 * The <i>attributes</i> parameter indicates the list of attributes to retrieve for
 * the entry. Each attribute specified must contain a valid identifier.
 *
 * @param identifier
 *		java.lang.String
 * @param ccsid
 *		int
 * @param attributes
 *		ValidationListAttribute[]
 * @return
 *		ValidationListEntry
 * @exception PersistenceException
 *		If an error occurs while calling the AS/400 APIs.
 */
public ValidationListEntry findEntry(String identifier, int ccsid, ValidationListAttribute[] attributes) throws PersistenceException {
	ValidationListEntry entry = new ValidationListEntry();

	ProgramCall pgm = new ProgramCall(getAS400());
	ProgramParameter[] parmList = new ProgramParameter[6];
	try {
		pgm.setProgram(QSYSObjectPathName.toPath("QSYS", "QSYFDVLE", "PGM"), parmList);
	} catch (PropertyVetoException pve) { };

	ValidationListTranslatedData entryID =
		new ValidationListTranslatedData(identifier, ccsid, getAS400());
	ValidationListAttributeInfo attrInfo =
		new ValidationListAttributeInfo(attributes);

	// Refer to documentation for the QSYFDVLE Security API for a complete description of parameters
	parmList[0] = getQualifiedNameParm();
	parmList[1] = new ProgramParameter(entryID.toBytes());
	parmList[2] = new ProgramParameter(attrInfo.toBytes());
	parmList[3] = new ProgramParameter(entry.getByteLength());
	parmList[4] = new ProgramParameter(attrInfo.getByteLength());
	parmList[5] = new ProgramParameter(new AS400Bin4().toBytes(0));

	runProgram(pgm);

	entry.init(parmList[3].getOutputData(), 0);
	attrInfo.setAttributesData(parmList[4].getOutputData(), 0);
	entry.setAttributeInfo(attrInfo);

	return entry;
}
/**
 * Returns the AS/400 system containing the validation list.
 *
 * @return com.ibm.as400.access.AS400
 */
public AS400 getAS400() {
	return as400_;
}
/**
 * Returns the text description for the validation list object.
 * <p>
 * Note: Only returned if set by the <code>setDescription()</code> method.
 *
 * @return java.lang.String
 */
public String getDescription() {
	if (description_ != null)
		return description_;
	return "";
}
/**
 * Returns all entries from the validation list.
 * <p>
 * Note: The list is opened by the initial call to the list API. In doing so, the entire
 * contents of the list is retrieved as a snapshot and stored in a buffer on the
 * AS/400 system. The list contents can then be retrieved from the pre-filled
 * buffer, in whole or in part, at the convenience of the application by using a
 * system-provided handle. The handle is used to close the list when the
 * application has completed processing of all entries.
 * <p>
 * Currently the entire list is built and retrieved synchronously by this method.
 * This could potentially be changed to allow for additional processing options,
 * such as asynchronous building of the list, retrieving only a subset of
 * entries, etc. For now we just keep it simple.
 *
 * @exception PersistenceException
 *		If an error occurs while calling the AS/400 APIs.
 */
public ValidationListEntry[] getEntries() throws PersistenceException {
	ValidationListEntry[] list = new ValidationListEntry[0];

	ProgramCall pgm = new ProgramCall(getAS400());
	ProgramParameter[] parmList = new ProgramParameter[7];
	int bufferLength = LISTBUFFER_LENGTH_INITIAL;
	try {
		pgm.setProgram(QSYSObjectPathName.toPath("QGY", "QSYOLVLE", "PGM"), parmList);
	} catch (PropertyVetoException pve) { };
	
	// Refer to documentation for the QSYOLVLE Security API for a complete description of parameters
	parmList[0] = new ProgramParameter(bufferLength);
	parmList[1] = new ProgramParameter(new AS400Bin4().toBytes(bufferLength));
	parmList[2] = new ProgramParameter(getListInfoStruct().getByteLength());
	parmList[3] = new ProgramParameter(new AS400Bin4().toBytes(-1)); // List is built synchronously
	parmList[4] = new ProgramParameter(new AS400Text(8, getAS400()).toBytes("VLDE0100"));
	parmList[5] = getQualifiedNameParm();
	parmList[6] = new ProgramParameter(new AS400Bin4().toBytes(0));

	// Open/initialize the list
	runProgram(pgm);

	Object[] listInfo = (Object[])getListInfoStruct().toObject(parmList[2].getOutputData());
	list = new ValidationListEntry[((Integer)listInfo[0]).intValue()];

	byte[] listHandle = (byte[])listInfo[2];
	int listPosition = ((Integer)listInfo[1]).intValue();

	// Process the entries retrieved on the initial call
	parseEntries(list, 0, parmList[0].getOutputData(), listPosition);

	// Continue to fetch more entries until the entire list is retrieved
	while (listPosition < list.length) {
		listPosition += getNextEntries(listHandle, listPosition, list);
	}

	// Close/cleanup the list
	closeList(listHandle);
	return list;
}
/**
 * Returns the structure used to convert values for a list information parameter.
 * <p>
 * This structure is referenced when calling the QSYOLVLE and QGYGTLE APIs to retrieve
 * the initial and subsequent lists of entries from the validation list, respectively.
 *
 * @return com.ibm.as400.access.AS400Structure
 */
private AS400Structure getListInfoStruct() {
	if (listInfo_ == null)
		listInfo_ = new AS400Structure(
		new AS400DataType[] {
			new AS400Bin4(),			// Total records
			new AS400Bin4(),			// Records returned
			new AS400ByteArray(4),		// Request handle
			new AS400Bin4(),			// Record length
			new AS400Text(1, as400_),	// Info complete indicator
			new AS400ByteArray(13),		// Date/time created (unconverted)
			new AS400Text(1, as400_),	// List status indicator
			new AS400ByteArray(1),		// Reserved
			new AS400Bin4(),			// Length of info returned
			new AS400Bin4()				// First record in buffer
		});
	return listInfo_;
}
/**
 * Retrieve the next group of entries from the buffered list.
 * <p>
 * The list is opened by the initial call to the list API. In doing so, the entire
 * contents of the list is retrieved as a snapshot and stored in a buffer on the
 * AS/400 system. The list contents can then be retrieved from the pre-filled
 * buffer, in whole or in part, at the convenience of the application by using a
 * system-provided handle. The handle is used to close the list when the
 * application has completed processing of all entries.
 * <p>
 * This method accepts the handle and starting position for retrieving the next
 * group of entries. All entries retrieved are stored in the list, and the
 * number of entries retrieved is returned.
 * <p>
 * The current implementation calls for all remaining entries to be retrieved in one
 * pass. If necessary, however, this method could be called multiple times as
 * necessary to fill in the remaining list items.
 *
 * @param listHandle
 *		byte[]
 * @param listPosition
 *		int
 * @param list
 *		ValidationListEntry[]
 * @return
 *		The number of entries retrieved.
 * @exception PersistenceException
 *		If an error occurs while calling the AS/400 APIs.
 */
private int getNextEntries(byte[] listHandle, int listPosition, ValidationListEntry[] list) throws PersistenceException {
	int recordsToReturn = list.length - listPosition;
	int recordsReturned = 0;

	if (recordsToReturn > 0) {
		ProgramCall pgm = new ProgramCall(getAS400());
		ProgramParameter[] parmList = new ProgramParameter[7];

		// Try & retrieve all remaining entries in a single call; keeps it simple for now
		int bufferLength = LISTBUFFER_LENGTH_NEXT;
		try {
			pgm.setProgram(QSYSObjectPathName.toPath("QGY", "QGYGTLE", "PGM"), parmList);
		} catch (PropertyVetoException pve) { };

		// Refer to documentation for the QGYGTLE generic list API for a complete description of parameters
		parmList[0] = new ProgramParameter(bufferLength);
		parmList[1] = new ProgramParameter(new AS400Bin4().toBytes(bufferLength));
		parmList[2] = new ProgramParameter(listHandle);
		parmList[3] = new ProgramParameter(getListInfoStruct().getByteLength());
		parmList[4] = new ProgramParameter(new AS400Bin4().toBytes(recordsToReturn));
		parmList[5] = new ProgramParameter(new AS400Bin4().toBytes(listPosition+1)); // 1-based
		parmList[6] = new ProgramParameter(new AS400Bin4().toBytes(0));

		runProgram(pgm);

		Object[] listInfo = (Object[])getListInfoStruct().toObject(parmList[3].getOutputData());
		recordsReturned = ((Integer)listInfo[1]).intValue();
		parseEntries(list, listPosition, parmList[0].getOutputData(), recordsReturned);
	}
	return recordsReturned;
}
/**
 * Returns the number of entries in the validation list.
 *
 * @exception PersistenceException
 *		If an error occurs while calling the AS/400 APIs.
 */
public int getNumberOfEntries() throws PersistenceException {
	ProgramCall pgm = new ProgramCall(getAS400());
	ProgramParameter[] parmList = new ProgramParameter[7];
	int bufferLength = 512;
	try {
		pgm.setProgram(QSYSObjectPathName.toPath("QGY", "QSYOLVLE", "PGM"), parmList);
	} catch (PropertyVetoException pve) { };
	
	// Refer to documentation for the QSYOLVLE Security API for a complete description of parameters
	parmList[0] = new ProgramParameter(bufferLength);
	parmList[1] = new ProgramParameter(new AS400Bin4().toBytes(bufferLength));
	parmList[2] = new ProgramParameter(getListInfoStruct().getByteLength());
	parmList[3] = new ProgramParameter(new AS400Bin4().toBytes(-1)); // List is built synchronously
	parmList[4] = new ProgramParameter(new AS400Text(8, getAS400()).toBytes("VLDE0100"));
	parmList[5] = getQualifiedNameParm();
	parmList[6] = new ProgramParameter(new AS400Bin4().toBytes(0));

	// Open the list
	runProgram(pgm);

	// Retrieve list information
	Object[] listInfo = (Object[])getListInfoStruct().toObject(parmList[2].getOutputData());

	// Close the list
	closeList((byte[])listInfo[2]);

	// Return the retrieved info
	return ((Integer)listInfo[0]).intValue();
}
/**
 * Returns the location (library context) of the validation list.
 *
 * @return com.ibm.as400.access.QSYSObjectPathName
 */
public QSYSObjectPathName getPath() {
	return path_;
}
/**
 * Returns a program parameter to be used for AS/400 Toolbox program calls that
 * require the qualifed name of the validation list.
 * <p>
 * The object name is set into the first 10 bytes of the parameter value.
 * The library name is set into the next 10 bytes of the parameter value.
 *
 * @return com.ibm.as400.access.ProgramParameter
 */
protected ProgramParameter getQualifiedNameParm() {
	StringBuffer buffer = new StringBuffer("                    ");
	
	fillStringBuffer(buffer,  0, getPath().getObjectName());
	fillStringBuffer(buffer, 10, getPath().getLibraryName());
	
	return new ProgramParameter(
		ProgramParameter.PASS_BY_REFERENCE,
		new AS400Text(20, getAS400()).toBytes(buffer.toString()));
}
/**
 * Returns the structure used to parse entries returned by the list APIs.
 * <p>
 * This structure is referenced when parsing entries returned by calls to the
 * QSYOLVLE and QGYGTLE APIs.
 *
 * @return com.ibm.as400.access.AS400Structure
 */
private AS400Structure getVlde0100Struct() {
	if (vlde0100_ == null)
		vlde0100_ = new AS400Structure(
		new AS400DataType[] {
			new AS400Bin4(), // Length of entry
			new AS400Bin4(), // Displacement to entry ID
			new AS400Bin4(), // Length of entry ID
			new AS400Bin4(), // CCSID of entry ID
			new AS400Bin4(), // Displacement to encrypted data
			new AS400Bin4(), // Length of encrypted data
			new AS400Bin4(), // CCSID of encrypted data
			new AS400Bin4(), // Displacement to entry data
			new AS400Bin4(), // Length of entry data
			new AS400Bin4()  // CCSID of entry data
		});
	return vlde0100_;
}
/**
 * Handles an unexpected exception that was caught as the result of invoking an AS/400
 * API or command. The exception is wrapped and surfaced as a PersistenceException.
 *
 * @exception PersistenceException
 */
private void handleUnexpectedAS400Exception(Throwable e) throws PersistenceException {
	throw new PersistenceException(e);
}
/**
 * Handles unexpected messages that was received as the result of invoking an AS/400
 * API or command. The messages are wrapped and surfaced as a PersistenceException.
 *
 * @exception PersistenceException
 */
private void handleUnexpectedAS400Messages(AS400Message[] messages) throws PersistenceException {
	throw new PersistenceException(messages);
}
/**
 * Parse the validation list entries from the raw (AS/400) bytes of a specified buffer.
 * <p>
 * Beginning with the specified <i>start</i> position, the parsed bytes are inserted
 * as ValidationListEntry objects into the <i>list</i>. The list is assumed to be
 * large enough to contain all the parsed entries. The <i>buffer</i> is assumed
 * to contain <i>numberInBuffer</i> entries.
 *
 * @param list
 *		ValidationListEntry[]
 * @param start
 *		int
 * @param buffer
 *		byte[]
 * @param numberInBuffer
 *		int
 */
private void parseEntries(
	ValidationListEntry[] list,
	int start,
	byte[] buffer,
	int numberInBuffer)
{
	int position = 0;
	for (int i=0; i<numberInBuffer; i++) {
		ValidationListEntry entry = new ValidationListEntry();
		list[start+i] = entry;

		Object[] vlde0100 = (Object[])getVlde0100Struct().toObject(buffer, position);
		int entryLength = ((Integer)vlde0100[0]).intValue();

		// Refer to getVlde0100Struct() for a description of values in the structure
		entry.setEntryID(
			new ValidationListTranslatedData(
				((Integer)vlde0100[3]).intValue(),
				parseEntryData(buffer, position+((Integer)vlde0100[1]).intValue(),
					((Integer)vlde0100[2]).intValue())));	
		entry.setEncryptedData(
			new ValidationListTranslatedData(
				((Integer)vlde0100[6]).intValue(),
				parseEntryData(buffer, position+((Integer)vlde0100[4]).intValue(),
					((Integer)vlde0100[5]).intValue())));
		entry.setUnencryptedData(
			new ValidationListTranslatedData(
				((Integer)vlde0100[9]).intValue(),
				parseEntryData(buffer, position+((Integer)vlde0100[7]).intValue(),
					((Integer)vlde0100[8]).intValue())));

		position += entryLength;
	}
}
/**
 * Parse the translated data from the raw (AS/400) bytes of a specified buffer.
 * <p>
 * Beginning with the specified <i>start</i> position, bytes are returned
 * from the buffer up to the given <i>length</i>.
 *
 * @param buffer byte[]
 * @param position int
 * @param length int
 * @return byte[]
 */
private byte[] parseEntryData(byte[] buffer, int start, int length) {
	byte[] bytes = new byte[length];
	System.arraycopy(buffer, start, bytes, 0, length);
	return bytes;
}
/**
 * Deletes the entry from the validation list.
 * <p>
 * Note: The <i>AS400</i> and <i>Path</i> properties must be set prior to calling
 * this method.
 *
 * @param entry
 *		ValidationListEntry
 * @exception PersistenceException
 *		If an error occurs while calling the AS/400 validation list APIs.
 */
public void removeEntry(ValidationListEntry entry) throws PersistenceException {
	ProgramCall pgm = new ProgramCall(getAS400());
	ProgramParameter[] parmList = new ProgramParameter[3];

	// Refer to documentation for the QSYRMVLE Security API for a complete description of parameters
	try {
		pgm.setProgram(QSYSObjectPathName.toPath("QSYS", "QSYRMVLE", "PGM"), parmList);
	} catch (PropertyVetoException pve) { };

	parmList[0] = getQualifiedNameParm();
	parmList[1] = new ProgramParameter(entry.getEntryID().toBytes());
	parmList[2] = new ProgramParameter(new AS400Bin4().toBytes(0));

	runProgram(pgm);
}
/**
 * Run the given AS/400 command.
 *
 * @param c
 *		com.ibm.as400.access.CommandCall
 * @exception PersistenceException
 *		If the command is not successful or an unexpected exception occurs.
 */
private void runCommand(CommandCall c) throws PersistenceException {
	boolean success = false;
	try {success = c.run();}
		catch (Exception e) { handleUnexpectedAS400Exception(e); }
	if (!success)
		handleUnexpectedAS400Messages(c.getMessageList());
}
/**
 * Run the given AS/400 program call.
 *
 * @param p
 *		com.ibm.as400.access.ProgramCall
 * @exception PersistenceException
 *		If the command is not successful or an unexpected exception occurs.
 */
private void runProgram(ProgramCall p) throws PersistenceException {
		 // Try up to 5 times if object is locked (message CPF9803)
		 int i = 0;
		 boolean success = false;
		 boolean lockedObj = false;
		 PersistenceException err;
		 do {
		 		 try {
		 		 		 success = false;
		 		 		 lockedObj = false;
		 		 		 err = null;

		 		 		 // Pause (if not first time through the loop)
		 		 		 try {
		 		 		 		 if (i > 0) Thread.sleep(5000);
		 		 		 } catch (InterruptedException ie) {}

		 		 		 // Run the program
		 		 		 try {
		 		 		 		 success = p.run();
		 		 		 } catch (Exception e) {
		 		 		 		 handleUnexpectedAS400Exception(e);
		 		 		 }

		 		 		 if (!success) {
		 		 		 		 // Check available messages on the program call
		 		 		 		 AS400Message[] msgs = p.getMessageList();
		 		 		 		 if (msgs != null)
		 		 		 		 		 for (int j = 0; !lockedObj && j<msgs.length; j++)
		 		 		 		 		 		 lockedObj = "CPF9803".equals(msgs[j].getID());
		 		 		 		 // Throw persistence exception ...
		 		 		 		 handleUnexpectedAS400Messages(msgs);
		 		 		 }
		 		 } catch (PersistenceException pe) {
		 		 		 err = pe;
		 		 };
		 } while (++i < 5 && lockedObj);

		 if (err != null)
		 		 throw err;
}
/**
 * Run the given AS/400 service program call.
 *
 * @param p
 *		com.ibm.as400.access.ServiceProgramCall
 * @exception PersistenceException
 *		If the command is not successful or an unexpected exception occurs.
 */
private void runServiceProgram(ServiceProgramCall spc) throws PersistenceException {
		 // Try up to 5 times if object is locked (errno 3406)
		 int i = 0;
		 int errno = 0;
		 do {
		 		 // Pause (if not first time through the loop)
		 		 try { if (i > 0) Thread.sleep(5000); }
		 		 		 catch (InterruptedException ie) {};

		 		 // Run the service program
		 		 runProgram(spc);
		 		 errno = (spc.getReturnValueFormat() == ServiceProgramCall.RETURN_INTEGER
		 		 		 		 && spc.getIntegerReturnValue() == 0)
		 		 		 ? 0
		 		 		 : spc.getErrno();
		 } while (++i < 5 && errno == 3406);

		 // Check for abnormal error condition
		 if (errno == 0)
		 		 return;
		 PersistenceException pe = new PersistenceException(
		 		 new StringBuffer("Procedure named "
		 		 		 ).append(spc.getProcedureName()
		 		 		 ).append(" failed with errorno "
		 		 		 ).append(errno
		 		 		 ).toString());
		 throw pe;
}
/**
 * Sets the AS/400 system containing the validation list.
 *
 * @param as400 com.ibm.as400.access.AS400
 */
public void setAS400(AS400 as400) {
	as400_ = as400;
}
/**
 * Sets the text description for the validation list object.
 * <p>
 * Note: Only recognized if set prior to invoking the <code>create()</code> method.
 *
 * @param s java.lang.String
 */
public void setDescription(String s) {
	description_ = s;
}
/**
 * Sets the location (library context) of the validation list.
 *
 * @param path com.ibm.as400.access.QSYSObjectPathName
 */
public void setPath(QSYSObjectPathName path) {
	path_ = path;
}
/**
 * Verify that the encrypted information specified for the given entry is correct.
 * <p>
 * The <i>EntryID</i> and <i>DataToEncrypt</i> must be specified for the entry prior
 * to verification. Returns true if the data to encrypt matches the data already
 * encrypted for the entry at that ID on the AS/400; otherwise returns false.
 *
 * @param entry
 *		ValidationListEntry
 * @return boolean
 * @exception PersistenceException
 *		If an error occurs while calling the AS/400 APIs.
 */
public boolean verifyEntry(ValidationListEntry entry) throws PersistenceException {
	// Note: length of ID & data to encrypt must be in range accepted by API
	int len = entry.getDataToEncrypt().getBytes().length;
	if (len < 1 || len > 600)
		return false;
	len = entry.getEntryID().getBytes().length;
	if (len < 1 || len > 100)
		return false;

	// Create & prime the program parameters
	ProgramParameter[] parmList = new ProgramParameter[3];
	parmList[0] = getQualifiedNameParm();
	parmList[1] = new ProgramParameter(
		ProgramParameter.PASS_BY_REFERENCE, entry.getEntryID().toBytes());
	parmList[2] = new ProgramParameter(
		ProgramParameter.PASS_BY_REFERENCE, entry.getDataToEncrypt().toBytes());
	ServiceProgramCall sPGMCall = new ServiceProgramCall(getAS400());

	// Refer to documentation for the QsyVerifyValidationLstEntry Security API for a complete description of parameters
	try {
		sPGMCall.setProgram("/QSYS.LIB/QSYVLDL.SRVPGM", parmList);
		sPGMCall.setProcedureName("QsyVerifyValidationLstEntry");
		sPGMCall.setReturnValueFormat(ServiceProgramCall.RETURN_INTEGER);
	} catch (PropertyVetoException pve) { };

	runServiceProgram(sPGMCall);
	return sPGMCall.getIntegerReturnValue() == 0;
}
}
