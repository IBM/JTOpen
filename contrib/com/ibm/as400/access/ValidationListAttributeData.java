package com.ibm.as400.access;

/**
 * Represents the value of a validation
 * list attribute. Each validation list entry may contain one or more attributes.
 * For each attribute, there is an associated value. This class models
 * the value. The same information is present as in the superclass
 * <code>ValidationListTranslatedData</code>, but the order and location of
 * the information in the corresponding API structures differ.
 *
 * @author Thomas Johnson (tom.johnson@kingland.com), Kingland Systems Corporation
 */
public class ValidationListAttributeData extends ValidationListTranslatedData {

/**
 * Constructs a ValidationListTranslatedData.
 */
public ValidationListAttributeData() {
	super();
}
/**
 * Constructs a ValidationListTranslatedData from a structure stored as IBM i bytes.
 * <p>
 * The <i>offset</i> indicates the starting position of the structure in the
 * given <i>buffer</i>.
 *
 * @param buffer byte[]
 * @param offset int
 */
public ValidationListAttributeData(byte[] buffer, int offset) {
	super(buffer, offset);
}
/**
 * Returns the offset of CCSID information in the structure when the receiver
 * is written to IBM i bytes.
 * @return int
 */
protected int getWriteOffsetCcsid() {
	return 0;
}
/**
 * Returns the offset of the length of the translated bytes when the receiver
 * is written to an IBM i byte structure.
 * @return int
 */
protected int getWriteOffsetTByteLength() {
	return 4;
}
/**
 * Returns the offset of the translated bytes when the receiver is written to an
 * IBM i byte structure.
 * @return int
 */
protected int getWriteOffsetTBytes() {
	return 16;
}
}
