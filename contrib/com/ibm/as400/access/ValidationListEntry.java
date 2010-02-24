///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: ValidationListEntry.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 2001-2010 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

/**
 * Represents an entry in a system validation list.
 *
 * @author Thomas Johnson (tom.johnson@kingland.com), Kingland Systems Corporation
 */
public class ValidationListEntry {

	private ValidationListTranslatedData entryID_ = null;
	private ValidationListTranslatedData encryptedData_ = null;
	private ValidationListTranslatedData unencryptedData_ = null;
	private ValidationListDataToEncrypt dataToEncrypt_ = null;
	private ValidationListAttributeInfo attrInfo_ = null;
/**
 * Constructs a ValidationListEntry.
 */
public ValidationListEntry() {
	super();
}
/**
 * Constructs a ValidationListEntry from a structure stored as IBM i bytes.
 * <p>
 * The <i>offset</i> indicates the starting position of the structure
 * in the given <i>buffer</i>.
 *
 * @param buffer byte[]
 * @param offset int
 */
public ValidationListEntry(byte[] buffer, int offset) {
	this();
	init(buffer, offset);
}
/**
 * Returns the object containing all attribute information associated with the entry.
 * <p>
 * Attributes and associated values can be assigned by the application prior to
 * adding an entry. When finding an entry, the identifiers for attributes to retrieve
 * are set by the application prior to the find. The corresponding attribute values
 * are filled in as a result of the find operation.
 *
 * @return ValidationListAttributeInfo
 */
public ValidationListAttributeInfo getAttributeInfo() {
	if (attrInfo_ == null)
		attrInfo_ = new ValidationListAttributeInfo();
	return attrInfo_;
}
/**
 * Returns the total length of the corresponding structure when this object is
 * written to IBM i bytes for use by the validation list APIs.
 * <p>
 * Note: The length 1724 is dictated by the API definition (i.e. QSYFDVLE).
 *
 * @return int
 */
public int getByteLength() {
	return 1724;
}
/**
 * Returns the information to encrypt when the entry is subsequently inserted or updated.
 * <p>
 * This differs from the <i>EncryptedData</i> property in that the latter, if present,
 * reflects the data currently encrypted for the entry.
 * <p>
 * If the <i>QsyEncryptData</i> attribute is set to 0, the data to be encrypted can only
 * be used to verify an entry and cannot be retrieved later. This is the default.
 * <p>
 * If the attribute was set to 1, the data to be encrypted can be used to verify an entry
 * and can potentially be returned on a find operation. The system value QRETSVRSEC
 * (Retain system security data) has the final say in determining if the data can be
 * retrieved from the entry. If the system value is set to 0 (Do not retain data), the
 * entry will be added, but the data to be encrypted will not be stored with the entry
 * and cannot be retrieved. If the system value is set to 1 (Retain data),
 * then the data to be encrypted will be stored (in encrypted form) with the entry
 * and can be retrieved. 
 *
 * @return ValidationListDataToEncrypt
 */
public ValidationListDataToEncrypt getDataToEncrypt() {
	if (dataToEncrypt_ == null)
		dataToEncrypt_ = new ValidationListDataToEncrypt();
	return dataToEncrypt_;
}
/**
 * Returns the information currently encrypted for the entry in the validation list.
 * <p>
 * Note: The encrypted data may or may not be retrievable based on the value set
 * for the <i>QsyEncryptData</i> attribute when the entry was inserted.
 *
 * @return ValidationListTranslatedData
 * @see #getDataToEncrypt
 */
public ValidationListTranslatedData getEncryptedData() {
	if (encryptedData_ == null)
		encryptedData_ = new ValidationListTranslatedData();
	return encryptedData_;
}
/**
 * Returns the translated data containing the entry identifier.
 *
 * @return ValidationListTranslatedData
 */
public ValidationListTranslatedData getEntryID() {
	if (entryID_ == null)
		entryID_ = new ValidationListTranslatedData();
	return entryID_;
}
/**
 * Returns the unencrypted data for the entry.
 *
 * @return ValidationListTranslatedData
 */
public ValidationListTranslatedData getUnencryptedData() {
	if (unencryptedData_ == null)
		unencryptedData_ = new ValidationListTranslatedData();
	return unencryptedData_;
}
/**
 * Initialize a ValidationListEntry from a structure stored as IBM i bytes.
 * <p>
 * The <i>offset</i> indicates the starting position of the structure
 * in the given <i>buffer</i>.
 *
 * @param buffer byte[]
 * @param offset int
 */
public void init(byte[] buffer, int offset) {
	// offset to non-encrypted data
	int struct_offset_data = 716;
	// offset to encrypted data
	int struct_offset_encrypted = 108;
	// offset to identifier data
	int struct_offset_id = 0;
	
	setEntryID(new ValidationListTranslatedData(buffer, offset + struct_offset_id));
	setEncryptedData(new ValidationListTranslatedData(buffer, offset + struct_offset_encrypted));
	setUnencryptedData(new ValidationListTranslatedData(buffer, offset + struct_offset_data));
}
/**
 * Sets the object containing all attribute information associated with the entry.
 * <p>
 * Attributes and associated values can be assigned by the application prior to
 * adding an entry. When finding an entry, the identifiers for attributes to retrieve
 * are set by the application prior to the find. The corresponding attribute values
 * are filled in as a result of the find operation.
 *
 * @param info
 *		ValidationListAttributeInfo
 */
public void setAttributeInfo(ValidationListAttributeInfo info) {
	attrInfo_ = info;
}
/**
 * Sets the information to encrypt when the entry is subsequently inserted or updated.
 * <p>
 * This differs from the <i>EncryptedData</i> property in that the latter, if present,
 * reflects the data currently encrypted for the entry.
 * <p>
 * If the <i>QsyEncryptData</i> attribute is set to 0, the data to be encrypted can only
 * be used to verify an entry and cannot be retrieved later. This is the default.
 * <p>
 * If the attribute was set to 1, the data to be encrypted can be used to verify an entry
 * and can potentially be returned on a find operation. The system value QRETSVRSEC
 * (Retain system security data) has the final say in determining if the data can be
 * retrieved from the entry. If the system value is set to 0 (Do not retain data), the
 * entry will be added, but the data to be encrypted will not be stored with the entry
 * and cannot be retrieved. If the system value is set to 1 (Retain data),
 * then the data to be encrypted will be stored (in encrypted form) with the entry
 * and can be retrieved. 
 *
 * @param dataToEncrypt
 *		ValidationListDataToEncrypt
 */
public void setDataToEncrypt(ValidationListDataToEncrypt dataToEncrypt) {
	dataToEncrypt_ = dataToEncrypt;
}
/**
 * Sets the information currently encrypted for the entry in the validation list.
 * <p>
 * Note: The encrypted data may or may not be retrievable based on the value set
 * for the <i>QsyEncryptData</i> attribute when the entry was inserted.
 *
 * @param encryptedData
 *		ValidationListTranslatedData
 */
public void setEncryptedData(ValidationListTranslatedData encryptedData) {
	encryptedData_ = encryptedData;
}
/**
 * Sets the translated data containing the entry identifier.
 *
 * @param entryID
 *		ValidationListTranslatedData
 */
public void setEntryID(ValidationListTranslatedData entryID) {
	entryID_ = entryID;
}
/**
 * Sets the unencrypted data for the entry.
 *
 * @param data
 *		ValidationListTranslatedData
 */
public void setUnencryptedData(ValidationListTranslatedData data) {
	unencryptedData_ = data;
}
}
