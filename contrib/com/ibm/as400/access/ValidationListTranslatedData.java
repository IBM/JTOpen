package com.ibm.as400.access;

/**
 * Copyright © 2001, International Business Machines Corporation and others. All Rights Reserved.
 **
 * <p>
 * The ValidationListTranslatedData class represents language-specific information
 * that is assigned in a standardized format to a validation list entry.
 * This could be information in the non-encrypted data, encrypted data, or
 * identifier portion of a validation list entry. Maximum lengths for each
 * are 1000, 600, and 100 bytes, respectively.
 *
 * @author Thomas Johnson (tom.johnson@kingland.com), Kingland Systems Corporation
 */
public class ValidationListTranslatedData {
	static final String COPYRIGHT =
		"Copyright © 2001, International Business Machines Corporation and others. All Rights Reserved.";

	private byte[] bytes_ = null;
	private int ccsid_ = 0;
/**
 * Constructs a ValidationListTranslatedData.
 */
public ValidationListTranslatedData() {
	super();
}
/**
 * Constructs a ValidationListTranslatedData from a structure stored as server bytes.
 * <p>
 * The <i>offset</i> indicates the starting position of the structure in the
 * given <i>buffer</i>.
 *
 * @param buffer byte[]
 * @param offset int
 */
public ValidationListTranslatedData(byte[] buffer, int offset) {
	this();

	int dataLength =(new AS400Bin4().toInt(buffer, offset + getReadOffsetTByteLength()));
	setCcsid(new AS400Bin4().toInt(buffer, offset + getReadOffsetCcsid()));
	setBytes((byte[])(
		new AS400ByteArray(dataLength).toObject(
			buffer, offset + getReadOffsetTBytes())));
}
/**
 * Constructs a ValidationListTranslatedData from the specified server <i>bytes</i>
 * which are encoded in the given <i>ccsid</i>.
 *
 * @param ccsid int
 * @param bytes byte[]
 */
public ValidationListTranslatedData(int ccsid, byte[] bytes) {
	this();
	setCcsid(ccsid);
	setBytes(bytes);
}
/**
 * Constructs a ValidationListTranslatedData from the given string.
 * <p>
 * The translated bytes are derived by converting the string to server bytes
 * using the given <i>ccsid</i>. The <i>as400</i> is required to perform the
 * conversion from text to bytes. A ccsid of 0 indicates to use the ccsid
 * of the current user.
 *
 * @param s java.lang.String
 * @param ccsid int
 * @param as400 com.ibm.as400.access.AS400
 */
public ValidationListTranslatedData(String s, int ccsid, AS400 as400) {
	this();
	setBytes(s, ccsid, as400);
}
/**
 * Returns the total length of the corresponding structure when this object is
 * written to server bytes for use by the validation list APIs.
 * <p>
 * This is the length of the entire structure, not just the translated bytes.
 *
 * @return int
 */
public int getByteLength() {
	// Assumes variable length data (the translated bytes) will always occur at end
	int total = getWriteOffsetTBytes();
	if (getBytes() != null)
		total += getBytes().length;
	return total;
}
/**
 * Returns the server bytes comprising the translated data.
 * <p>
 * For text conversion, the bytes will be interpreted using the assigned ccsid.
 *
 * @return byte[]
 */
public byte[] getBytes() {
	return bytes_;
}
/**
 * Returns the coded character set identifier used to encode the translated bytes.
 * <p>
 * Valid CCSID values are in the range 1 through 65535. The special value 0
 * can be used to indicate the default CCSID for the current user (when
 * the validation list APIs are invoked on the server). In some cases,
 * primarily attribute data, the special value -1 is also allowed. This
 * indicates that no CCSID value is stored with the data (i.e. binary data,
 * where no conversion is required).
 *
 * @return int
 */
public int getCcsid() {
	return ccsid_;
}
/**
 * Returns the offset of CCSID information in the structure when the receiver is
 * read from server bytes.
 * @return int
 */
protected int getReadOffsetCcsid() {
	return 4;
}
/**
 * Returns the offset of the length of the translated bytes when the receiver
 * is read from a server byte structure.
 * @return int
 */
protected int getReadOffsetTByteLength() {
	return 0;
}
/**
 * Returns the offset of the translated bytes when the receiver is read from an
 * server byte structure.
 * @return int
 */
protected int getReadOffsetTBytes() {
	return 8;
}
/**
 * Returns the result of converting the assigned server bytes to a Java String
 * using the assigned CCSID. Returns null if the assigned ccsid is -1, since
 * the bytes do not represent text.
 * <p>
 * The <i>as400</i> is required to perform the conversion.
 *
 * @param as400 com.ibm.as400.access.AS400
 * @return java.lang.String
*/
public String getString(AS400 as400) {
	int ccsid = getCcsid();

	// check null or non-text value
	if (bytes_ == null || ccsid == -1)
		return null;
	// check unicode; no conversion required
	if (ccsid == 13488)
		return new String(bytes_);
	// check for default ccsid for current user id
	if (ccsid == 0)
		ccsid = as400.getCcsid();

	return
		(String)new AS400Text(bytes_.length, ccsid, as400).toObject(bytes_);
}
/**
 * Returns the length to be specified in the written server byte structure
 * if the assigned data is null.
 * <p>
 * Typically this value is set to 0. However, there are some cases where
 * other values must be specified to maintain proper behavior. For
 * example, when changing an entry a structure must be specified for the
 * data to encrypt, even if the encrypted data should not be changed.
 * However, if the data length in the structure is set to 0 instead of -1,
 * the existing encrypted data is wiped out. This is undesirable since
 * we don't always want the encrypted data changed. We might want to
 * modify the unencrypted data (i.e. user statistics) while leaving
 * the encrypted data (i.e. user password) unchanged.
 *
 * @return int
 */
protected int getWriteNullDataLength() {
	return 0;
}
/**
 * Returns the offset of CCSID information in the structure when the receiver
 * is written to server bytes.
 * @return int
 */
protected int getWriteOffsetCcsid() {
	return getReadOffsetCcsid();
}
/**
 * Returns the offset of the length of the translated bytes when the receiver
 * is written to an server byte structure.
 * @return int
 */
protected int getWriteOffsetTByteLength() {
	return getReadOffsetTByteLength();
}
/**
 * Returns the offset of the translated bytes when the receiver is written to an
 * server byte structure.
 * @return int
 */
protected int getWriteOffsetTBytes() {
	return getReadOffsetTBytes();
}
/**
 * Indicates whether the given CCSID is valid for tagging server data.
 * @return true if valid; false if not.
 */
protected boolean isValidCcsid(int ccsid) {
	boolean isValid = false;
	switch(ccsid) {
		// Universal Character Set (UCS-2 and UTF-8)
		case 13488:	//  UCS-2 Level 1
		
		// CCSIDs for EBCDIC Group 1 (Latin-1) Countries
		case 37:	// USA, Canada (S/370), Netherlands, Portugal, Brazil, Australia, New Zealand
		case 256:	// Word Processing, Netherlands
		case 273:	// Austria, Germany
		case 277:	// Denmark, Norway
		case 278:	// Finland, Sweden
		case 280:	// Italy
		case 284:	// Spain, Latin America (Spanish)
		case 285:	// United Kingdom
		case 297:	// France
		case 500:	// Belgium, Canada (AS/400), Switzerland, International Latin-1
		case 871:	// Iceland
		case 924:	// Latin-0
		case 1140:	// USA, Canada (S/370), Netherlands, Portugal, Brazil, Australia, New Zealand
		case 1141:	// Austria, Germany
		case 1142:	// Denmark, Norway
		case 1143:	// Finland, Sweden
		case 1144:	// Italy
		case 1145:	// Spain, Latin America (Spanish)
		case 1146:	// United Kingdom
		case 1147:	// France
		case 1148:	// Belgium, Canada (AS/400), Switzerland, International Latin-1
		case 1149:	// Iceland
		
		// CCSIDs for EBCDIC Group 1a (Non-Latin-1 SBCS) Countries
		case 420:	// Arabic (Type 4)Visual LTR
		case 423:	// Greek
		case 424:	// Hebrew(Type 4)
		case 870:	// Latin-2 Multilingual
		case 875:	// Greek
		case 880:	// Cyrillic Multilingual
		case 905:	// Turkey Latin-3 Multilingual
		case 918:	// Urdu
		case 1025:	// Cyrillic Multilingual
		case 1026:	// Turkey Latin-5
		case 1097:	// Farsi
		case 1112:	// Baltic Multilingual
		case 1122:	// Estonia
		case 1123:	// Ukraine
		case 8612:	// Arabic (Type 5)
		case 8616:	// Hebrew (Type 6)
		case 12708:	// Arabic (Type 7)
		case 62211:	// Hebrew (Type 5)
		case 62224:	// Arabic (Type 6)
		case 62235:	// Hebrew (Type 10)
 
		// SBCS CCSIDs for EBCDIC Group 2 (DBCS) Countries
		case 290:	// Japan Katakana (extended)
		case 833:	// Korea (extended)
		case 836:	// Simplified Chinese (extended)
		case 838:	// Thailand (extended)
		case 1027:	// Japan English (extended)
		case 1130:	// Vietnam
		case 1132:	// Lao
		case 9030:	// Thailand (extended)
		case 13121:	// Korea Windows
		case 13124:	// Traditional Chinese
		case 28709:	// Traditional Chinese (extended)

		// DBCS CCSIDs for EBCDIC Group 2 (DBCS) Countries
		case 300:	// Japan - including 4370 user-defined characters (UDC)
		case 834:	// Korea - including 1880 UDC 
		case 835:	// Traditional Chinese - including 6204 UDC
		case 837:	// Simplified Chinese - including 1880 UDC
		case 4396:	// Japan - including 1880 UDC
		case 4930:	// Korea Windows
		case 4933:	// Simplified Chinese

		// Mixed CCSIDs for EBCDIC Group 2 (DBCS) Countries
		case 930:	// Japan Katakana/Kanji (extended) - including 4370 UDC
		case 933:	// Korea (extended) - including 1880 UDC
		case 935:	// Simplified Chinese (extended) - including 1880 UDC
		case 937:	// Traditional Chinese (extended) - including 4370 UDC
		case 939:	// Japan English/Kanji (extended) - including 4370 UDC
		case 1364:	// Korea (extended)
		case 1388:	// Traditional Chinese
		case 5026:	// Japan Katakana/Kanji (extended) - including 1880 UDC)
		case 5035:	// Japan English/Kanji (extended) - including 1880 UDC
			isValid = true;
	}
	return isValid;
}
/**
 * Sets the server bytes comprising the translated data.
 * <p>
 * For text conversion, the bytes will be interpreted using the assigned ccsid.
 *
 * @param bytes byte[]
 */
public void setBytes(byte[] bytes) {
	bytes_ = bytes;
}
/**
 * Sets the bytes comprising the translated data from the given string.
 * <p>
 * The translated bytes are derived by converting the string to server bytes
 * using the given <i>ccsid</i>. The <i>as400</i> is required to perform the
 * conversion from text to bytes. A ccsid of 0 indicates to use the ccsid
 * of the current user.
 *
 * @param s java.lang.String
 * @param ccsid int
 * @param as400 com.ibm.as400.access.AS400
 */
public void setBytes(String s, int ccsid, AS400 as400) {
	// if 0, store the current ccsid since it will be used in the conversion
	if (ccsid == 0)
		ccsid = as400.getCcsid();
	setCcsid(ccsid);

	// check for unicode ccsid (no conversion required)
	if (ccsid == 13488) {
		setBytes(s.getBytes());
		return;
	}

	// assume a maximum buffer of (2 * length of the string) to allow for double-byte
	// the buffer will automatically be padded with blanks during conversion
	// blanks are then trimmed
	int len = s.length() * 2;
	byte[] buffer = new AS400Text(len, ccsid, as400).toBytes(s);
	while (len > 0)
		if (buffer[len-1] != 0x40) break;
			else len--;

	byte[] trimmed = new byte[len];
	System.arraycopy(buffer, 0, trimmed, 0, len);
	setBytes(trimmed);
}
/**
 * Sets the coded character set identifier used to encode the translated bytes.
 * <p>
 * Valid CCSID values are in the range 1 through 65535. The special value 0
 * can be used to indicate the default CCSID for the current user (when
 * the validation list APIs are invoked on the server). In some cases,
 * primarily attribute data, the special value -1 is also allowed. This
 * indicates that no CCSID value is stored with the data (i.e. binary data,
 * where no conversion is required).
 *
 * @param ccsid int
 */
public void setCcsid(int ccsid) {
	// Note: Compensate for IBM HTTP Internet User tools, which appear to
	// sometimes insert entries with invalid CCSID identifiers.
	// If not valid, assume 37 as a default.
	// CCSIDs <= 0 are sometimes used to indicate special values
	// when tagging attribute data, etc.
	ccsid_ = (ccsid <= 0 || isValidCcsid(ccsid)) ? ccsid : 37;
}
/**
 * Returns the byte array resulting from converting this object to a structure
 * usable by the server APIs.
 *
 * @return byte[]
 */
public byte[] toBytes() {
	byte[] buffer =
		new byte[getByteLength()];
	toBytes(buffer, 0);
	return buffer;
}
/**
 * Converts this object to a byte structure usable by the server APIs.
 * <p>
 * The server bytes are inserted into the <i>buffer</i> starting at the given
 * <i>offset</i>. The total number of bytes inserted is returned.
 *
 * @param buffer byte[]
 * @param offset int
 * @return int
 */
public int toBytes(byte[] buffer, int offset) {
	byte[] bytes = getBytes();
	int byteLength = (bytes == null) ? getWriteNullDataLength() : bytes.length;

	// write length of translated bytes
	new AS400Bin4().toBytes(byteLength, buffer, offset + getWriteOffsetTByteLength());
	// write ccsid
	new AS400Bin4().toBytes(getCcsid(), buffer, offset + getWriteOffsetCcsid());
	// write translated bytes
	if (byteLength > 0)
		System.arraycopy(
			bytes, 0, buffer, offset+getWriteOffsetTBytes(), byteLength);

	return getByteLength();
}
}
