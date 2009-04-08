package com.ibm.as400.security.auth;

///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                                 
//                                                                             
// Filename: RefreshAgent.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2003 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////
import com.ibm.as400.access.AS400;
import com.ibm.as400.access.ConnectionEvent;
import com.ibm.as400.access.ConnectionListener;
import com.ibm.as400.access.ExtendedIllegalStateException;
import com.ibm.as400.access.Trace;
/**
 * The RefreshAgent class implements the automatic refresh
 * mechanism for credentials.
 *
 * <p>Note: The implementation is potentially generic enough
 * to work with any object implementing a refreshable
 * interface, but is currently limited to credentials.
 *
 */
class RefreshAgent extends Thread implements ConnectionListener {

	private int refreshInterval_, maxRefreshes_ = 0;
	private AS400Credential target_ = null;
	private Exception failure_ = null;
	private boolean stop_ = false;
/**
 * Constructs a RefreshAgent object.
 *
 * <p> Agent thread will be identified as a daemon as
 * to not prevent the JVM from exiting once user
 * threads are complete.
 *
 * <p> Thread will be assigned the maximum permitted 
 * priority within its thread group. 
 *
 */
RefreshAgent() {
	super();
	setDaemon(true);
	setPriority(Thread.MAX_PRIORITY);
}
/**
 * Constructs a RefreshAgent object.
 *
 * @param target
 *		The object to refresh automatically.
 *
 * @param refreshInterval
 *		The number of seconds between refresh attempts.
 *		The first refresh will occur immediately;
 *		the second will occur this many seconds after
 *		the first, and so on.
 *
 * @param maxRefreshes
 *		The maximum number of times to refresh the
 *		credential. A value of negative one (-1)
 *		indicates no maximum.
 *
 */
RefreshAgent(AS400Credential target, int refreshInterval, int maxRefreshes) {
	this();
	setTarget(target);
	setRefreshInterval(refreshInterval);
	setMaxRefreshes(maxRefreshes);
}
/**
 * Invoked when a service has been connected
 * on an AS400 object.
 *
 * @param event The connection event.
 *
 */
public void connected(ConnectionEvent event) {
	// Do nothing; not interested in this event.
}
/**
 * Invoked when a service has been disconnected
 * on an AS400 object.
 *
 * <p> If all services are disconnected, the thread
 * is stopped.
 *
 * @param event The connection event.
 *
 */
public void disconnected(ConnectionEvent event) {
	try {
		AS400 sys = (AS400)event.getSource();
		if (isAlive() && !sys.isConnected()) {
			stopRefresh();
			if (Trace.isTraceOn())
				Trace.log(Trace.INFORMATION,
					new StringBuffer("RefreshAgent stopped after system disconnect >> "
						).append(target_.toString()).toString());
		}
	}
	catch (Exception e) {
		Trace.log(Trace.ERROR,
			"RefreshAgent not stopped after system disconnect >> " 
				+ target_.toString(), e);
	}
}
/**
 * Called when garbage collection determines that there are
 * no more references to the object.
 *
 */
protected void finalize() throws Throwable {
	try {
		if (isAlive()) {
			stopRefresh();
		}
	}
	catch (Exception e) {
		Trace.log(Trace.ERROR,
			"RefreshAgent not stopped on finalize >> " + target_.toString(), e);
	}
	super.finalize();
}
/**
 * Returns the exception resulting from failure of
 * the most recent refresh attempt.
 *
 * @return
 *		The exception; null if not available.
 *
 */
public Throwable getFailure() {
	return failure_;
}
/**
 * Runs the agent.
 *
 * <p> Starts automatic refresh. The first refresh
 * attempt is performed immediately, followed by
 * periodic refresh as appropriate.
 *
 * <p> Automatic attempts are discontinued on the
 * first failed refresh attempt.
 *
 */
public void run() {
	stop_ = false;
	if (Trace.isTraceOn())
		Trace.log(Trace.INFORMATION,
			new StringBuffer("RefreshAgent starting with maximum attempts "
				).append(maxRefreshes_
				).append(" and refresh interval "
				).append(refreshInterval_
				).append(" >> "
				).append(target_.toString()
				).toString());

	validatePropertySet("target", target_);
	AS400 sys = target_.getSystem();
	sys.addConnectionListener(this);
	failure_ = null;
	int i = 0;
	while (!stop_ && failure_ == null && (maxRefreshes_ < 0 || i++ < maxRefreshes_)) {
		try {
			target_.refresh();
			if (maxRefreshes_ < 0 || i < maxRefreshes_)
				sleep(refreshInterval_ * 1000);
		}
		catch(InterruptedException ie) {
			if (stop_)
				Trace.log(Trace.DIAGNOSTIC, "RefreshAgent stopped on interrupt >> " + target_.toString());
			else {
				Trace.log(Trace.ERROR, "RefreshAgent interrupted >> " + target_.toString(), ie);
				failure_ = ie;
			}
		}
		catch(Exception e) {
			Trace.log(Trace.ERROR, "RefreshAgent failed >> " + target_.toString(), e);
			failure_ = e;
		}
	}
	sys.removeConnectionListener(this);
}
/**
 * Sets the number of times to perform refresh.
 *
 * <p> This property cannot be changed when active.
 *
 * @param max
 *		The maximum number of times to refresh the
 *		credential. A value of negative one (-1)
 *		indicates no maximum.
 *
 * @exception ExtendedIllegalStateException
 *		If the property cannot be changed due
 *		to the current state.
 *
 */
void setMaxRefreshes(int max) {
	validatePropertyChange("maxRefreshes");
	maxRefreshes_ = max;
}
/**
 * Sets the number of seconds to pause between attempts.
 *
 * <p> This property cannot be changed when active.
 *
 * @param seconds
 *		The number of seconds between refresh attempts.
 *		The first refresh will occur immediately;
 *		the second will occur this many seconds after
 *		the first, and so on.
 *
 * @exception ExtendedIllegalStateException
 *		If the property cannot be changed due
 *		to the current state.
 *
 */
void setRefreshInterval(int seconds) {
	validatePropertyChange("refreshInterval");
	refreshInterval_ = seconds;
}
/**
 * Sets the target for automatic refresh actions.
 *
 * <p> This property cannot be changed when active.
 *
 * @param impl
 *		The target object.
 *
 * @exception ExtendedIllegalStateException
 *		If the property cannot be changed due
 *		to the current state.
 *
 */
void setTarget(AS400Credential c) {
	validatePropertyChange("target");
	target_ = c;
}
/**
 * Instructs the thread to stop any automatic refresh in progress.
 *
 * <p> Does nothing if not running.
 *
 */
public void stopRefresh() {
	stop_ = true;
	if (isAlive())
		interrupt();
}
/**
 * Validates that the given property can be changed.
 *
 * <p> Properties cannot be changed while active.
 *
 * @param propertyName
 *		The property to be validated.
 *
 * @exception ExtendedIllegalStateException
 *		If the agent is in an active state, indicating
 *		that attributes cannot be modified.
 *
 */
void validatePropertyChange(String propertyName) {
	if (isAlive()) {
		Trace.log(Trace.ERROR, "Property " + propertyName + " not changed (active=true).");
		throw new ExtendedIllegalStateException(propertyName,
			ExtendedIllegalStateException.PROPERTY_NOT_CHANGED);
	}
}
/**
 * Validates that the given property has been set.
 *
 * <p> Performs a simple null check, used to
 * centralize exception handling.
 *
 * @param propertyName
 *		The property to validate.
 *
 * @param value
 *		The property value.
 *
 * @exception ExtendedIllegalStateException
 *		If the property is not set.
 *
 */
void validatePropertySet(String propertyName, Object value) {
	if (value == null) {
		Trace.log(Trace.ERROR, "Required property " + propertyName + " not set.");
		throw new ExtendedIllegalStateException(
			ExtendedIllegalStateException.PROPERTY_NOT_SET);
	}
}
}
