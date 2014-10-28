package org.teiid.authoring.share.services;

import org.teiid.authoring.share.Constants;


public final class StringUtils {

	public static String checkValidServiceName(String serviceName) {
		String statusMsg = Constants.OK;
				
		// Check that service name is not empty
        if(isEmpty(serviceName)) {
        	return "The service name cannot be empty";
        }
        
        // Must start with a letter
        char c = serviceName.charAt(0);
        if(!Character.isLetter(c)) {
        	return "The first character of the service name must be an alphabetic character";
        }
        
        // Check that remaining chars are 1) letter, 2) digit, or 3) underscore
        int length = serviceName.length();
        for (int index = 1; index < length; index++) {
        	c = serviceName.charAt(index);
        	if(!Character.isLetter(c) && !Character.isDigit(c) && !(c=='_') ) {
        		statusMsg = "The service name character '"+ c + "' at position ["+index+"] is invalid";
        	}
        	if(!statusMsg.equals(Constants.OK)) {
        		break;
        	}
        }
        
        return statusMsg;
	}
	
	/**
	 * <p>
	 * Returns whether the specified text is either empty or null.
	 * </p>
	 *
	 * @param text The text to check; may be null;
	 * @return True if the specified text is either empty or null.
	 * @since 4.0
	 */
	public static boolean isEmpty( final String text ) {
		return (text == null || text.length() == 0);
	}

	/**
	 * Compare string values - considered equal if either are null or empty.
	 *
	 * @param thisValue the first value being compared (can be <code>null</code> or empty)
	 * @param thatValue the other value being compared (can be <code>null</code> or empty)
	 * @return <code>true</code> if values are equal or both values are empty
	 */
	public static boolean valuesAreEqual( String thisValue,
			String thatValue ) {
		if (isEmpty(thisValue) && isEmpty(thatValue)) {
			return true;
		}

		return equals(thisValue, thatValue);
	}

	/**
	 * @param thisString the first string being compared (may be <code>null</code>)
	 * @param thatString the other string being compared (may be <code>null</code>)
	 * @return <code>true</code> if the supplied strings are both <code>null</code> or have equal values
	 */
	public static boolean equals( final String thisString,
			final String thatString ) {
		if (thisString == null) {
			return (thatString == null);
		}

		return thisString.equals(thatString);
	}
	    
}
