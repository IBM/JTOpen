///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: IPaddrConv.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 2002 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.util.commtrace;

import java.util.Vector;

/**
 * Converts an IP address between numeric and presentation format.<br>
 * Numeric format is the following:<br>
 *  IPv4: ddd.ddd.ddd.ddd<br>
 *  IPv6: xxxx:xxxx:xxxx:xxxx:xxxx:xxxx:xxxx:xxxx<br>
 * Presentation format is the same as numeric but with leading 0's removed 
 * and IPv6 addresses compressed if possible:<br>
 *  IPv4: ddd.ddd.d.d<br>
 *  IPv6: xxxx::xxxx:xxxx<br>
 * Support is also avalible for IPv4 addresses encapsulated in IPv6 addresses:<br>
 *  IPv6: x:x:x:x:x:x:d.d.d.d<br>  
 * 
 */
class IPAddressConversion {
	private String IPaddr;
	private int type= UNDEF;
	/** Undefined */
	public static int UNDEF= -1;
	/** IPv4 Address */
	public static int IPv4= 0;
	/** IPv4 Address in hexadecimal notation */
	public static int IPv4Hex= 1;
	/** IPv6 Address */
	public static int IPv6= 2;
	/** A tunneled IPv4 through an IPv6 address */
	public static int IPvMixed= 3;
	/** A tunneled IPv4 in hexadeciaml notation through an IPv6 Address */
	public static int IPvMixedHex= 4;

	/**
	 * Creates a new IPaddrConversion object and sets the type of address. 
	 * @param IPaddr The IP address to convert. 
	 */
	public IPAddressConversion(String IPaddr) {
		this.IPaddr= IPaddr;
		settype();
	}

	/**
	 * Creates a new IPaddrConversion object and sets the type of address. 
	 * @param IPaddr The IP address to convert. 
	 * @param type   The Type of IP addresses.
	 */
	public IPAddressConversion(String IPaddr, int type) {
		this.IPaddr= IPaddr;
		this.type= type;
	}

	public int type() {
		return type;
	}

	/**
	 * Converts a hexadecimal string into an integer string. 
	 */
	private String toDec(String hex) {
		return (new Integer(Integer.parseInt(hex, 16))).toString();
	}

	/**
	 * Splits a string into string arrays based on the given strin.
	 * @param str		The string to split.
	 * @param splitstr	The string to split on.
	 */
	public String[] split(String str, String splitstr) {
		String[] ret;
		Vector temp= new Vector(12);
		int fromIndex= 0, index= 0;
		while ((index= str.indexOf(splitstr, fromIndex)) != -1) {
			temp.add(str.substring(fromIndex, index));
			fromIndex= index + 1;
		}
		temp.add(str.substring(fromIndex));

		ret= new String[temp.size()];
		for (int i= 0; i < ret.length; i++) {
			ret[i]= (String) temp.remove(0);
		}
		return ret;
	}

	/**
	 * Converts this IP address from numeric to presentation.
	 */
	public String ntop() {
		if (type == IPv4 || type == IPv4Hex) {
			String seg[]= split(IPaddr, ".");
			// Break up the IP Address into different segments 
			return ntopIPv4(seg);
		} else if (type == IPv6) {
			String seg[]= split(IPaddr, ":");
			// Break up the IP Address into different segments 
			seg= trim(seg); // Remove extra 0s from the address.
			return ntopIPv6(seg);
		} else if (type == IPvMixed) {
			String seg[]= split(IPaddr, ":");
			String tmp[]= split(seg[7], ".");
			seg[7]= "";
			seg= trim(seg);
			tmp= trim(tmp);

			return (ntopIPv6(seg) + ":" + ntopIPv4(tmp));
		}
		return "";
	}

	/**
	 * Converts the IPv4 portion of this address from numeric to presentation. 
	 * @param seg   The portion of the address to convert.
	 */
	private String ntopIPv4(String seg[]) {
		// String that will be returned.
		StringBuffer ret= new StringBuffer(32);
		for (int i= 0; i < seg.length; i++) {
			// Convert to decimal if the segement is in hex
			if (type == IPv4Hex || type == IPvMixedHex) {
				seg[i]= toDec(seg[i]);
			} else {
				seg[i]= (new Integer(Integer.parseInt(seg[i]))).toString();
			}
			ret.append(seg[i] + ".");
		}
		// Remove the last delimiter and return the array
		return ret.substring(0, (ret.length() - 1));
	}

	/**
	 * Converts the IPv6 portion of this address from numeric to presentation.
	 * @param seg   The portion of the address to convert.
	 */
	private String ntopIPv6(String[] seg) {
		boolean zero= false, compressed= false;
		int start= 0, end= 0;
		// String that will be returned.
		StringBuffer ret= new StringBuffer(64);
		// Cycle through the segments check for segments with 0s.
		for (int i= 0; i < seg.length; i++) {
			// If the character is 0 and we have not yet hit a zero. 
			if (seg[i].charAt(0) == '0' && !zero) {
				// If there is compression at the beginning of the string
				// add another : so we get ::
				if (i == 0) {
					ret.append(":");
				}
				// Make it so we don't reset our start position. 
				zero= true;
				// The position where the compression will start
				start= i;
			}
			// If 0 and we have not yet set
			// our end position check to make sure we don't go past the
			// end of the array and make sure the next element isn't a 0. 
			if (zero && end == 0 && (i + 1) < seg.length && seg[i + 1].charAt(0) != '0') {
				end= i;
			}
		}
		for (int i= 0; i < seg.length; i++) {
			// If we don't have any 0s just append the segments to the 
			// return string.
			if (!zero) {
				ret.append(seg[i] + ":");
			} else {
				if (!compressed) {

					// If this segment isn't a 0 append it to the return string.
					if (seg[i].charAt(0) != '0') {
						ret.append(seg[i] + ":");
						// If the space between 0s is not equal we need to compress.
					} else if (start != end) {
						// Add another : so we get the compression
						ret.append(":");
						i= end;
						// Skip past the rest of the 0s.
						compressed= true;
					}
				} else {
					ret.append(seg[i] + ":");
				}
			}
		}
		// Remove the last delimiter and return the array
		return ret.substring(0, (ret.length() - 1));
	}

	/**
	 * Trims extra characters from the IP address. 
	 * @param seg   The IP address to trim.
	 * @return String[] containing the IP address.
	 */
	private String[] trim(String[] seg) {
		StringBuffer ret= new StringBuffer(64);
		// Cycle through the segments changing if needed
		for (int i= 0; i < seg.length; i++) {
			// If we have gotten to our first number or not.     
			boolean first= true;
			// Split the segment into a character array
			// makes removing the 0s easy.
			char[] c= seg[i].toCharArray();
			for (int j= 0; j < c.length; j++) {
				// If we have a 0, have not hit our first number,
				if (c[j] == '0' && first) {
					if (type == IPv4 || type == IPv4Hex) {
						// have reached the last possible spot in the address
						// and have not added a zero to our output do so	
						if (j > 1) {
							// Add character to return array
							ret.append("0");
						}
					} else if (type == IPv6) {
						if (j > 2) {
							// Add character to return array
							ret.append("0");
						}
					} else if (type == IPvMixed || type == IPvMixedHex) {
						if (j > 2) {
							// Add character to return array
							ret.append("0");
						}
					}
				} else {
					// If this is the first time with a number we
					// need returned set this flag so the rest of
					// the zeros in this block are preserved.
					if (first) {
						first= false;
					}
					// Add the character to the return array.
					ret.append(c[j]);
				}
			}
			// Add the correct delimiter based on what type of
			// address we have.
			if (type == IPv4 || type == IPv4Hex) {
				ret.append(".");
			} else if (type == IPv6) {
				ret.append(":");
			} else if (type == IPvMixed || type == IPvMixedHex) {
				ret.append(":");
			}
		}
		if (type == IPv4 || type == IPv4Hex) {
			//ret = ret.substring(0,(ret.length()-1));
			// Remove last delimiter from address.
			// Return array using a regex to split the string up
			return split(ret.substring(0, (ret.length() - 1)), ".");
		} else if (type == IPv6) {
			//ret = ret.substring(0,(ret.length()-1));
			// Remove last delimiter from address
			// Return array using a regex to split the string up
			return split(ret.substring(0, (ret.length() - 1)), ":");
		} else if (type == IPvMixed || type == IPvMixedHex) {
			//ret = ret.substring(0,(ret.length()-1));
			// Remove last delimiter from address
			// Return array using a regex to split the string up
			return split(ret.substring(0, (ret.length() - 1)), ":");
		}
		return split(ret.toString(), ".");
	}

	/**
	 * Converts this IP address from presentation to numeric. 
	 */
	public String pton() {
		if (type == IPv4 || type == IPv4Hex) {
			// Break up the IP Address into different segments 
			String seg[]= split(IPaddr, ".");
			return ptonIPv4(seg);
		} else if (type == IPv6) {
			// Break up the IP Address into different segments 
			String seg[]= split(IPaddr, ":");
			return ptonIPv6(seg);
			// For a mixed address split it up on IPv6 delimiter.
		} else if (type == IPvMixed || type == IPvMixedHex) {
			String seg[]= split(IPaddr, ":");
			int index;
			String tmp[]= null;
			// Cycle through the segments looking for the mixed
			for (int i= 0; i < seg.length; i++) {
				if (seg[i].indexOf('.') != -1) { // segment
					tmp= split(seg[i], "."); // Split up the mixed segment.
					seg[i]= ""; // Set the mixed segment to nothing.
					index= i; // Set the index of our mixed segment.
				}
			}
			// Convert the two addresses and return the result.
			return (ptonIPv6(seg) + ":" + ptonIPv4(tmp));
		}
		return "Unknown Type of IP Address";
	}

	/**
	 * Converts the IPv4 portion of this address from presentation to numeric.
	 * @param seg   The portion of the address to convert.
	 */
	private String ptonIPv4(String seg[]) {
		// String that will be returned.
		StringBuffer ret= new StringBuffer(32);
		// Cycle through the segments chaging if needed.
		for (int i= 0; i < seg.length; i++) {
			int length= seg[i].length();
			// If we have a full address just add the delimiter.
			if (length == 3) {
				ret.append(seg[i] + ".");
				// Otherwise add one 0 and a delimiter
			} else if (length == 2) {
				ret.append("0".concat(seg[i]) + ".");
				// Add two 0s and a delimiter
			} else if (length == 1) {
				ret.append("00".concat(seg[i]) + ".");
			}
		}
		// Remove the last delimiter and return the array 
		return ret.substring(0, ret.length() - 1);
	}

	/**
	 * Converts the IPv6 portion of this address from presentation to numeric.
	 * @param seg   The portion of the address to convert.
	 */
	private String ptonIPv6(String seg[]) {
		// String that will be returned.
		StringBuffer ret= new StringBuffer(64);

		// If the IPaddr is compressed uncompress it.
		if (seg.length < 8) {
			// Overwrite the compressed string
			seg= new String[8];
			// Split into 2 strings.
			String comp[]= split(IPaddr, "::");
			// Divide the two strings so we know how much
			// compression has occured
			String segcomp[]= split(comp[0], ":");
			String segcomp2[]= split(comp[1], ":");
			int size= 0;
			// Calculate the amount we will have to add
			if (comp[0].equals("")) {
				size= 9 - segcomp.length - segcomp2.length;
			} else {
				size= 8 - segcomp.length - segcomp2.length;
			}
			// The position in our return array 
			int pos= 0;
			// Dump the first array into our return array
			for (int i= 0; i < segcomp.length; i++) {
				seg[pos]= segcomp[i];
				if (!comp[0].equals("")) {
					pos++;
				}
			}
			// Add the correct number of blank octets
			for (int i= 0; i < size; i++) {
				seg[pos]= "0";
				pos++;
			}
			// Dump the second array into our return array
			for (int i= 0; i < segcomp2.length; i++) {
				seg[pos]= segcomp2[i];
				pos++;
			}
		}
		for (int i= 0; i < seg.length; i++) {
			int length= seg[i].length();
			// If we have a full address just add the delimiter.
			if (length == 4) {
				ret.append(seg[i] + ":");
				// Add one 0 and a delimiter 
			} else if (length == 3) {
				ret.append("0".concat(seg[i]) + ":");
				// Otherwise add two 0 and a delimiter
			} else if (length == 2) {
				ret.append("00".concat(seg[i]) + ":");
				// Add three 0s and a delimiter
			} else if (length == 1) {
				ret.append("000".concat(seg[i]) + ":");
			}
		}
		// Remove the last delimiter and return the array 
		return ret.substring(0, ret.length() - 1);
	}

	/**
	 * Sets the type of this address. 
	 */
	private void settype() {
		int l= IPaddr.length();

		for (int i= 0; i < l; i++) {
			if (IPaddr.charAt(i) == ':') {
				if (type == UNDEF) {
					type= IPv6;
				}
			} else if (IPaddr.charAt(i) == '.') {
				if (type == UNDEF) {
					type= IPv4;
					break;
				} else {
					type= IPvMixed;
					break;
				}
			}
		}
	}
}
