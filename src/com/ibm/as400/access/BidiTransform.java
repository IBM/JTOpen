///////////////////////////////////////////////////////////////////////////////
//
// JTOpen (IBM Toolbox for Java - OSS version)
//
// Filename:  BidiTransform.java
//
// The source code contained herein is licensed under the IBM Public License
// Version 1.0, which has been approved by the Open Source Initiative.
// Copyright (C) 1997-2005 International Business Machines Corporation and
// others.  All rights reserved.
//
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.access;

import java.util.Vector;

/**
 *  Bidi text is a combination of a sequence of characters and a set of
 *  Bidi flags.  Bidi text is implemented by the BidiText class.
 *  <p>
 *  Layout transformations allow to convert a given instance of BidiText
 *  (the source) into another instance (the destination) with possibly
 *  different Bidi flags while conserving the semantics of the text.
 *  <p>
 *  The BidiTransform class defines such a transformation.
 *  A BidiTransform instance contains fields which define what auxilliary
 *  outputs are required from the transformation, and fields to refer
 *  to these outputs.
 *  The work itself is done by the transform method of the BidiText class.
 *  <p>
 *  Boolean fields are used to specify options of the transform operation.
 *  Other fields are used to store auxilliary outputs of the transformation.
 *  <p>
 *  There are no specific constructors for this class.  The default
 *  constructor creates a default BidiFlagSet and puts its reference in the
 *  "flags" instance member.  Unless modified later, this qualifies a
 *  transformation to Implicit LTR swapped numerals-nominal unshaped text.
 *
 *  <p><b>Multi-threading considerations:</b>
 *  each thread must use its own instances of this class.
 *
 */

class BidiTransform {

/**
 *  Option: Bidi flags of destination BidiText
 */
    public BidiFlagSet      flags = new BidiFlagSet();
/**
 *  Option: enable Implicit to Implicit transformations
 */
    public boolean          impToImp = true;
/**
 *  Option: use "roundtrip" algorithm for reordering
 */
    public boolean          roundTrip;
/**
 *  Option: use "Windows compatible" algorithm for reordering
 *  <p>If this option is true, the reordering algorithm is modified to
 *  perform more closely like Windows.  In particular, logical string
 *  "12ABC" in LTR orientation (where ABC represent Arabic or Hebrew letters)
 *  is reordered as "CBA12" instead of "12CBA".
 *  Also, logical string "abc 123 45" (where all digits represent Hindi numbers)
 *  is reordered as "abc 123 45" instead of "abc 45 123".
 */
    public boolean          winCompatible;
/**
 *  Option: add Markers to destination text when needed for round trip
 *  <p>If this option is true, LRM markers are inserted when transforming
 *  from visual LTR to logical (LTR or RTL) where needed to insure round trip.
 *  <p>This option is ignored if the removeMarkers option is true.
 */
    public boolean          insertMarkers;
/**
 *  Option: remove Markers from destination text after performing a Bidi
 *  transformation
 *  <p>If this option is true, LRM and RLM markers in the source text
 *  participate in the reordering, but they are removed from the destination
 *  text after performing the Bidi transformation.
 */
    public boolean          removeMarkers;
/**
 *  Option: shaping options for this transformation
 */
    public ArabicOptionSet  options;
/**
 *  Option: consider white space to always follow base orientation
 */
    public boolean          wordBreak;
/**
 *  Option: create a destination BidiText
 *  <p>If this flag is not true, the transform method will return a null.
 */
    public boolean          destinationRequired = true;
/**
 *  Option: create a source to destination mapping
 */
    public boolean          srcToDstMapRequired;
/**
 *  Option: create a destination to source mapping
 */
    public boolean          dstToSrcMapRequired;
/**
 *  Option: create a property map
 */
    public boolean          propertyMapRequired;
/**
 *  Option: this invocation is a continuation from the previous one
 *  (re-establish same conditions as at the end of last call)
 */
    public boolean          continuation;

/**
 *  Output value: number of characters processed in the source data by
 *  the last transform
 */
    public int      inpCount;
/**
 *  Output value: number of characters written in the destination data by
 *  the last transform
 */
    public int      outCount;
/**
 *  Output value: source-to-destination map from the last transform with
 *  srcToDstMapRequired specified; may be null if no such request.
 *  <p>
 *  If when starting a transformation this field refers to a large enough
 *  array of integers, this array will be re-used to put the new map.
 *  Otherwise a new array will be created.
 *  <p> This map has a number for each character in the "interesting" data
 *  of the source BidiText.  This number is the index of where this
 *  character is moved in the character array of the destination BidiText.
 */
    public int[]    srcToDstMap;
/**
 *  Output value: destination-to-source map from the last transform with
 *  dstToSrcMapRequired specified; may be null if no such request.
 *  <p>
 *  If when starting a transformation this field refers to a large enough
 *  array of integers, this array will be re-used to put the new map.
 *  Otherwise a new array will be created.
 *  <p> This map has a number for each character in the "interesting" data
 *  of the destination BidiText.  This number is the index of the source
 *  character from which the destination character originates.  This index
 *  is relative to the beginning of the "interesting" data.  If the offset
 *  of the source BidiText is not zero, index 0 does not indicate the first
 *  character of the data array, but the character at position "offset".
 */
    public int[]    dstToSrcMap;
/**
 *  Output value: property map from the last transform with
 *  propertyMapRequired specified; may be null if no such request.
 *  <p>
 *  If when starting a transformation this field refers to a large enough
 *  array of bytes, this array will be re-used to put the new map.
 *  Otherwise a new array will be created.
 *  <p> This map has a byte for each character in the "interesting" data
 *  of the source BidiText.  The 6 lower bits of each property element
 *  is the Bidi level of the corresponding input character.
 *  The highest bit is a new-cell indicator for composed character
 *  environments: a value of 0 indicates a zero-length composing character
 *  element, and a value of 1 indicates an element that begins a new cell.
 */
    public byte[]   propertyMap;

    BidiOrder       myOrder;
    BidiShape       myShape;
    BidiFlagSet     flags1, flags2;
    Vector          insertPoints;

    long    internalState;      /* used by continuation calls */

}
