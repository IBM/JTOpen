package com.kingland.as400.access;

import com.ibm.as400.access.*;

/**
 * Copyright © 2001, International Business Machines Corporation and others. All Rights Reserved.
 **
 * <p>
 * The ValidationListAttribute class represents an attribute assigned to an
 * entry in a validation list. Each validation list entry may contain one or more
 * attributes. For each attribute, there is an associated value.
 *
 * @author Thomas Johnson (tom.johnson@kingland.com), Kingland Systems Corporation
 */
public class ValidationListAttribute {
	static final String COPYRIGHT =
		"Copyright © 2001, International Business Machines Corporation and others. All Rights Reserved.";

	private AS400 as400_ = null;
	private AS400Structure attribEntry_ = null;
	private ValidationListAttributeData data_ = null;
	private String identifier_ = null;
	private int maxValueLength_ = 1;
	private int location_ = 0;
	private int type_ = 0;
/**
 * Constructs a validation list attribute.
 */
public ValidationListAttribute() {
	super();
}
/**
 * Constructs a validation list attribute.
 * <p>
 * The <i>as400</i> is used to translate the assigned identifier when converted
 * to AS/400 bytes.
 *
 * @param as400
 *		com.ibm.as400.access.AS400
 */
public ValidationListAttribute(AS400 as400) {
	this();
	setAS400(as400);
}
/**
 * Returns the AS/400 system assigned to the attribute.
 * <p>
 * Used to translate the assigned identifier when converted to AS/400 bytes.
 *
 * @return com.ibm.as400.access.AS400
 */
public AS400 getAS400() {
	return as400_;
}
/**
 * Returns the attribute's parameter structure when written to AS/400 bytes.
 * <p>
 * This structure includes only the fixed fields for the parameter.
 *
 * @return com.ibm.as400.access.AS400Structure
 */
private AS400Structure getAttribEntryStruct() {
	if (attribEntry_ == null)
		attribEntry_ = new AS400Structure(
		new AS400DataType[] {
			new AS400Bin4(), // Length of entry
			new AS400Bin4(), // Location
			new AS400Bin4(), // Type
			new AS400Bin4(), // Displacement to identifier
			new AS400Bin4()  // Length of identifier
		});
	return attribEntry_;
}
/**
 * Returns the total length of the corresponding structure when this object is
 * written to AS/400 bytes for use by the validation list APIs.
 * <p>
 * The size varies based on usage of the receiver. If there is no associated data,
 * it is assumed that the structure being written is to identify an attribute
 * to be retrieved (eg. find an entry). If there is associated data, it is assumed
 * the structure will be used when adding a new entry or changing an existing
 * entry.
 *
 * @return int
 */
public int getByteLength() {
	ValidationListTranslatedData data = getData();

	// start with the length of all fixed fields for the struct.
	// if data is null, assume bytes are to be written for an operation to retrieve the data.
	// refer to QSYADVLE/QSYCHVLE/QSYFDVLE doc for structure definition.
	int total = (data == null) ? 24 : 28;
	
	// add variable length of attribute identifier
	total += getIdentifier().length();
	
	// if applicable, add variable length of attribute data
	if (data != null)
		total += data.getByteLength();

	// return nearest multiple of 4
	int mod = total % 4;
	if (mod > 0)
		total += (4-mod);	
	return total;
}
/**
 * Returns the data associated with the attribute.
 * <p>
 * This value is required when the attribute is referenced to add a new entry or change
 * an existing entry. It should not be set when the attribute is used to identify
 * values to retrieve (eg. when finding an entry).
 *
 * @return com.kingland.as400.access.ValidationListAttributeData
 */
public ValidationListAttributeData getData() {
	return data_;
}
/**
 * Returns the identifier of the attribute.
 * <p>
 * For system-defined attributes, the allowed values are:
 * <ul>
 *   <li>QsyEncryptData - Associated with the data to encrypt.
 *   <p>
 *   If the <i>QsyEncryptData</i> attribute is set to 0, the data to be encrypted can only
 *   be used to verify an entry and cannot be retrieved later. This is the default.
 *   <p>
 *   If the attribute is set to 1, the data to be encrypted can be used to verify an entry
 *   and can potentially be returned on a find operation. The system value QRETSVRSEC
 *   (Retain server security data) has the final say in determining if the data can be
 *   retrieved from the entry. If the system value is set to 0 (Do not retain data), the
 *   entry will be added, but the data to be encrypted will not be stored with the entry
 *   and cannot be retrieved. If the system value is set to 1 (Retain data),
 *   then the data to be encrypted will be stored (in encrypted form) with the entry
 *   and can be retrieved. 
 * </ul>
 *
 * @return java.lang.String
 */
public String getIdentifier() {
	return identifier_;
}
/**
 * Indicates where the attribute should be stored. The allowed values are: 
 * <ul>
 *   <li>0 - The attribute is stored in the validation list object.
 * </ul>
 *
 * @return int
 */
public int getLocation() {
	return location_;
}
/**
 * Returns the maximum length of the value associated with the attribute.
 * <p>
 * This value is referenced when invoking APIs that need to indicate a
 * length for the buffer used to retrieve the attribute value.
 *
 * @return int
 */
public int getMaximumValueLength() {
	return maxValueLength_;
}
/**
 * Returns the attribute type. The allowed values are: 
 * <ul>
 *   <li>0 - This is a system-defined attribute.
 * </ul>
 *
 * @return int
 */
public int getType() {
	return type_;
}
/**
 * Sets the AS/400 system assigned to the attribute.
 * <p>
 * Used to translate the assigned identifier when converted to AS/400 bytes.
 *
 * @param as400 com.ibm.as400.access.AS400
 */
public void setAS400(AS400 as400) {
	as400_ = as400;
}
/**
 * Sets the data associated with the attribute.
 * <p>
 * This value is required when the attribute is referenced to add a new entry or change
 * an existing entry. It should not be set when the attribute is used to identify
 * values to retrieve (eg. when finding an entry).
 *
 * @param data
 *		com.kingland.as400.access.ValidationListAttributeData
 */
public void setData(ValidationListAttributeData data) {
	data_ = data;
}
/**
 * Sets the identifier of the attribute.
 * <p>
 * For system-defined attributes, the allowed values are:
 * <ul>
 *   <li>QsyEncryptData - Associated with the data to encrypt.
 *   <p>
 *   If the <i>QsyEncryptData</i> attribute is set to 0, the data to be encrypted can only
 *   be used to verify an entry and cannot be retrieved later. This is the default.
 *   <p>
 *   If the attribute was set to 1, the data to be encrypted can be used to verify an entry
 *   and can potentially be returned on a find operation. The system value QRETSVRSEC
 *   (Retain server security data) has the final say in determining if the data can be
 *   retrieved from the entry. If the system value is set to 0 (Do not retain data), the
 *   entry will be added, but the data to be encrypted will not be stored with the entry
 *   and cannot be retrieved. If the system value is set to 1 (Retain data),
 *   then the data to be encrypted will be stored (in encrypted form) with the entry
 *   and can be retrieved. 
 * </ul>
 *
 * @param s java.lang.String
 */
public void setIdentifier(String s) {
	identifier_ = s;
}
/**
 * Indicates where the attribute should be stored. The allowed values are: 
 * <ul>
 *   <li>0 - The attribute is stored in the validation list object.
 * </ul>
 *
 * @param location int
 */
public void setLocation(int location) {
	location_ = location;
}
/**
 * Sets the maximum length of the value associated with the attribute.
 * <p>
 * This value is referenced when invoking APIs that need to indicate a
 * length for the buffer used to retrieve the attribute value.
 *
 * @param length int
 */
public void setMaximumValueLength(int length) {
	maxValueLength_ = length;
}
/**
 * Returns the attribute type. The allowed values are: 
 * <ul>
 *   <li>0 - This is a system-defined attribute.
 * </ul>
 *
 * @param type int
 */
public void setType(int type) {
	type_ = type;
}
/**
 * Returns the byte array resulting from converting this object to a structure
 * usable by the AS/400 APIs.
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
 * Converts this object to a structure usable by the AS/400 APIs.
 * <p>
 * The AS/400 bytes are inserted into the <i>buffer</i> starting at the given
 * <i>offset</i>. The total number of bytes inserted is returned.
 *
 * @param buffer byte[]
 * @param offset int
 * @return int
 */
public int toBytes(byte[] buffer, int offset) {
	// check if data has been filled in for the attribute
	// if not, assume bytes being written are for an operation to retrieve the data
	if (getData() == null)
		return toBytesNoData(buffer, offset);

	// offset in structure for the attribute id (refer to QSYADVLE/QSYCHVLE doc)
	int struct_offset_id = 28;
	// length of attribute id; currently assumes sbcs
	int struct_length_id = getIdentifier().length();
	// total length of attribute entry; must be multiple of 4
	int struct_total_len = getByteLength();
	
	// declare objects for common fixed position fields
	Integer[] fixedData = {
		new Integer(struct_total_len), 	// total length
		new Integer(getLocation()),		// attribute location
		new Integer(getType()),			// attribute type
		new Integer(struct_offset_id), 	// displacement to identifier
		new Integer(struct_length_id) 	// length of identifier
	};

	int position = offset;
	
	// write common fixed position fields
	position += getAttribEntryStruct().toBytes(fixedData, buffer, position);
	// write displacement of attribute data
	position += new AS400Bin4().toBytes(
		struct_offset_id + struct_length_id, buffer, position);
	// write length of attribute data
	position += new AS400Bin4().toBytes(
		getData().getByteLength(), buffer, position);
	// write attribute identifier
	position += new AS400Text(struct_length_id, getAS400()).toBytes(
		getIdentifier(), buffer, position);
	// write attribute data
	position += getData().toBytes(buffer, position);
	
	return struct_total_len;
}
/**
 * Converts the attribute and any associated data to a structure usable by the
 * AS/400 APIs.
 * <p>
 * The AS/400 bytes are inserted into the <i>buffer</i> starting at the given
 * <i>offset</i>. The total number of bytes inserted is returned.
 * <p>
 * This method writes the bytes in a format expected when the attribute data is
 * currently unknown and expected to be retrieved into another buffer by the
 * API call. No attribute data is included.
 *
 * @return int
 */
private int toBytesNoData(byte[] buffer, int offset) {
	// offset in structure for the attribute id (refer to QSYFVLE doc)
	int struct_offset_id = 24;
	// length of attribute id; currently assumes sbcs
	int struct_length_id = getIdentifier().length();
	// total length of attribute entry; must be multiple of 4
	int struct_total_len = getByteLength();
	
	// declare objects for common fixed position fields
	Integer[] fixedData = {
		new Integer(struct_total_len), 	// total length
		new Integer(getLocation()),		// attribute location
		new Integer(getType()),			// attribute type
		new Integer(struct_offset_id), 	// displacement to identifier
		new Integer(struct_length_id) 	// length of identifier
	};

	int position = offset;
	
	// write common fixed position fields
	position += getAttribEntryStruct().toBytes(fixedData, buffer, position);
	// write # bytes to be provided for the attribute in receiver buffer
	position += new AS400Bin4().toBytes(
		24 + getMaximumValueLength(), buffer, position); // 24 is from QSYFDVLE API doc
	// write attribute identifier
	position += new AS400Text(struct_length_id, getAS400()).toBytes(
		getIdentifier(), buffer, position);
	
	return struct_total_len;
}
}