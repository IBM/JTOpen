///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: ValidationListAttributeInfo.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 2001-2010 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

/**
 * Encapsulates all attribute information
 * assigned to an entry in a validation list. Each validation list entry may contain
 * one or more attributes.
 *
 * @author Thomas Johnson (tom.johnson@kingland.com), Kingland Systems Corporation
 */
public class ValidationListAttributeInfo {

	private ValidationListAttribute[] attributes_ = null;
/**
 * Constructs a ValidationListAttributeInfo.
 */
public ValidationListAttributeInfo() {
	super();
}
/**
 * Constructs a ValidationListAttributeInfo with the given <i>attributes</i>.
 *
 * @param attributes
 *		ValidationListAttribute[]
 */
public ValidationListAttributeInfo(ValidationListAttribute[] attributes) {
	super();
	setAttributes(attributes);
}
/**
 * Returns the attributes assigned to the receiver.
 *
 * @return ValidationListAttribute[]
 */
public ValidationListAttribute[] getAttributes() {
	if (attributes_ == null)
		attributes_ = new ValidationListAttribute[0];
	return attributes_;
}
/**
 * Returns the total length of the corresponding structure when this object is
 * written to IBM i bytes for use by the validation list APIs.
 *
 * @return int
 */
public int getByteLength() {
	int total = 4;
	for(int i = 0; i < getAttributes().length; i++)
		total += getAttributes()[i].getByteLength();
	return total;
}
/**
 * Returns the total length of the corresponding structure when this object is
 * written to IBM i bytes for use by the validation list APIs.
 *
 * @return int
 */
public int getByteLengthForNativeCall() {
	int total = 4;	// Number of attributes
	total += 12;	// Reserved
	for(int i = 0; i < getAttributes().length; i++)
		total += getAttributes()[i].getByteLength();
	return total;
}
/**
 * Sets the attributes assigned to the receiver.
 *
 * @param attributes
 *		ValidationListAttribute[]
 */
public void setAttributes(ValidationListAttribute[] attributes) {
	attributes_ = attributes;
}
/**
 * Sets the data for assigned attributes from the IBM i bytes in the specified <i>buffer</i>.
 * <p>
 * This method is called when finding an entry with specific attributes. The attribute identifiers
 * are provided as input to the API, and a buffer is returned with the corresponding
 * attribute values. It is expected that this object was also used to specify the attributes
 * to retrieve. That way, the contents and order of attribute values in the buffer should
 * exactly match those assigned to this object.
 * <p>
 * Refer to documentation for the QSYFDVLE Security API for a complete description of parameters.
 *
 * @param buffer byte[]
 * @param offset int
 */
public void setAttributesData(byte[] buffer, int offset) {
	ValidationListAttribute[] attribs = getAttributes();
	
	int entrySize; // total size of the entry for a single attribute in the struct
	int dataStructOffset = 12; // from struct defined for output attribute in QSYFDVLE doc
	int position = offset; // current position in the buffer

	for (int i = 0; i < attribs.length; i++) {
		entrySize = new AS400Bin4().toInt(buffer, position);
		attribs[i].setData(new ValidationListAttributeData(buffer, position+dataStructOffset));
		position += entrySize;
	}
}
/**
 * Returns the byte array resulting from converting this object to a structure
 * usable by the system APIs.
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
 * Converts this object to a structure usable by the system APIs.
 * <p>
 * The IBM i bytes are inserted into the <i>buffer</i> starting at the given
 * <i>offset</i>. The total number of bytes inserted is returned.
 *
 * @param buffer byte[]
 * @param offset int
 * @return int
 */
public int toBytes(byte[] buffer, int offset) {
	// write number of attributes
	new AS400Bin4().toBytes(getAttributes().length, buffer, offset);
	int position = offset + 4;

	// write attributes
	for (int i = 0; i < getAttributes().length; i++)
		position += getAttributes()[i].toBytes(buffer, position);
	return position;
}
}
