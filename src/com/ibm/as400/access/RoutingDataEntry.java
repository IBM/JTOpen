///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: RoutingDataEntry.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 2005-2010 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////
package com.ibm.as400.access;
/**
 * Represents a routing entry data.
 * <br>For example:
 * <pre>
 *      SubsystemEntryList routingDataList = new SubsystemEntryList(AS400(), subsystemLibrary, subsystemName);
 *      RoutingDataEntry routingDataEntry = routingDataList.getRoutingDataEntry();
 *      System.out.println("compareValue is " + routingDataEntry.getcompareValue);
 * </pre>
 * </br>
 * @see SubsystemEntryList
 * @author zhangze
 *
 */
public class RoutingDataEntry {
	private int routingEntrySequenceNum_;
	private String routingEntryProgramName_;
	private String routingEntryProgramLibrary_;
	private String routingEntryClassName_;
	private String routingEntryClassLibrary_;
	private int maxActiveRoutingStep_;
	private int routingEntryPoolIden_;
	private int compareStartPosition_;
	private String compareValue_;
	private String routingEntryThreadResourcesAffinityGroup_;
	private String routingEntryThreadResourcesAffinityLevel_;
	private String routingEntryResourcesAffinityGroup_;
	
	
	
	public int getroutingEntrySequenceNum() {
		return routingEntrySequenceNum_;
	}
	
	public String getroutingEntryProgramName() {
		return routingEntryProgramName_;
	}
	
	public String getroutingEntryProgramLibrary() {
		return routingEntryProgramLibrary_;
	}
	
	public String getroutingEntryClassName() {
		return routingEntryClassName_;
	}
	
	public String routingEntryClassLibrary() {
		return routingEntryClassLibrary_;
	}
	
	public int getmaxActiveRoutingStep() {
		return maxActiveRoutingStep_;
	}
	
	public int getroutingEntryPoolIden() {
		return routingEntryPoolIden_;
	}
	
	public int getcompareStartPosition() {
		return compareStartPosition_;
	}
	
	public String getcompareValue() {
		return compareValue_;
	}
	
	public String getroutingEntryThreadResourcesAffinityGroup() {
		return routingEntryThreadResourcesAffinityGroup_;
	}
	
	public String getroutingEntryThreadResourcesAffinityLevel() {
		return routingEntryThreadResourcesAffinityLevel_;
	}
	
	public String getroutingEntryResourcesAffinityGroup() {
		return routingEntryResourcesAffinityGroup_;
	}
	
	//====================================

	public void setroutingEntrySequenceNum(int routingEntrySequenceNum) {
		routingEntrySequenceNum_ = routingEntrySequenceNum;
	}
	
	public void setroutingEntryProgramName(String routingEntryProgramName) {
		routingEntryProgramName_ = routingEntryProgramName;
	}
	
	public void setroutingEntryProgramLibrary(String routingEntryProgramLibrary) {
		routingEntryProgramLibrary_ = routingEntryProgramLibrary;
	}
	
	public void setroutingEntryClassName(String routingEntryClassName) {
		routingEntryClassName_ = routingEntryClassName;
	}
	
	public void setroutingEntryClassLibrary(String routingEntryClassLibrary) {
		routingEntryClassLibrary_ = routingEntryClassLibrary;
	}
	
	public void setmaxActiveRoutingStep(int maxActiveRoutingStep) {
		maxActiveRoutingStep_ = maxActiveRoutingStep;
	}
	
	public void setroutingEntryPoolIden(int routingEntryPoolIden) {
		routingEntryPoolIden_ = routingEntryPoolIden;
	}
	
	public void setcompareStartPosition(int compareStartPosition) {
		compareStartPosition_ = compareStartPosition;
	}
	
	public void setcompareValue(String compareValue) {
		compareValue_ = compareValue;
	}
	
	public void setroutingEntryThreadResourcesAffinityGroup(String routingEntryThreadResourcesAffinityGroup) {
		routingEntryThreadResourcesAffinityGroup_ = routingEntryThreadResourcesAffinityGroup;
	}
	
	public void setroutingEntryThreadResourcesAffinityLevel(String routingEntryThreadResourcesAffinityLevel) {
		routingEntryThreadResourcesAffinityLevel_ = routingEntryThreadResourcesAffinityLevel;
	}
	
	public void setroutingEntryResourcesAffinityGroup(String routingEntryResourcesAffinityGroup) {
		routingEntryResourcesAffinityGroup_ = routingEntryResourcesAffinityGroup;
	}

}
