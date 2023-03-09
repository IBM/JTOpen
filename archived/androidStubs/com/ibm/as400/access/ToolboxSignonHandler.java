package com.ibm.as400.access;

import java.net.UnknownHostException;

public class ToolboxSignonHandler implements SignonHandler {

	public boolean connectionInitiated(SignonEvent event, boolean forceUpdate) {
		// TODO Auto-generated method stub
		return false;
	}

	public void exceptionOccurred(SignonEvent event)
			throws AS400SecurityException {
		// TODO Auto-generated method stub
		
	}

	public boolean passwordAboutToExpire(SignonEvent event,
			int daysUntilExpiration) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean passwordExpired(SignonEvent event) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean passwordIncorrect(SignonEvent event) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean passwordLengthIncorrect(SignonEvent event) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean passwordMissing(SignonEvent event) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean systemNameMissing(SignonEvent event) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean systemNameUnknown(SignonEvent event, UnknownHostException exc) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean userIdDefaultAlreadyAssigned(SignonEvent event,
			String defaultUser) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean userIdAboutToBeDisabled(SignonEvent event) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean userIdDisabled(SignonEvent event) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean userIdLengthIncorrect(SignonEvent event) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean userIdMissing(SignonEvent event) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean userIdUnknown(SignonEvent event) {
		// TODO Auto-generated method stub
		return false;
	}

}
