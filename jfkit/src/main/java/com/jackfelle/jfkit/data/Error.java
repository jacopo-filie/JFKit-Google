//
//	The MIT License (MIT)
//
//	Copyright © 2017-2024 Jacopo Filié
//
//	Permission is hereby granted, free of charge, to any person obtaining a copy
//	of this software and associated documentation files (the "Software"), to deal
//	in the Software without restriction, including without limitation the rights
//	to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
//	copies of the Software, and to permit persons to whom the Software is
//	furnished to do so, subject to the following conditions:
//
//	The above copyright notice and this permission notice shall be included in all
//	copies or substantial portions of the Software.
//
//	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
//	IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
//	FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
//	AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
//	LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
//	OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
//	SOFTWARE.
//

package com.jackfelle.jfkit.data;

import com.jackfelle.jfkit.utilities.ObjectIdentifier;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import androidx.annotation.NonNull;

public class Error extends Throwable
{
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Types
	
	public static abstract class Codes
	{
		public static final int UNKNOWN = 0;
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Constants - User info keys
	
	public static final @NonNull String LOCALIZED_FAILURE_REASON_KEY = "localizedFailureReason";
	public static final @NonNull String LOCALIZED_MESSAGE_KEY = "localizedMessage";
	public static final @NonNull String LOCALIZED_RECOVERY_SUGGESTION_KEY = "localizedRecoverySuggestion";
	public static final @NonNull String UNDERLYING_ERROR_KEY = "underlyingError";
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Properties - Data
	
	private final int code;
	private final @NonNull String domain;
	private final Map<String, Object> userInfo;
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Properties (Accessors) - Data
	
	public int getCode() {
		return this.code;
	}
	
	public @NonNull String getDomain() {
		return this.domain;
	}
	
	public Throwable getUnderlyingError() {
		return this.getCause();
	}
	
	public Map<String, Object> getUserInfo() {
		Map<String, Object> retObj = this.userInfo;
		return ((retObj == null) ? null : new HashMap<>(retObj));
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Properties (Accessors) - Data (Localized)
	
	public String getLocalizedFailureReason() {
		return this.getUserInfoStringForKey(Error.LOCALIZED_FAILURE_REASON_KEY);
	}
	
	@Override public String getLocalizedMessage() {
		String retObj = this.getUserInfoStringForKey(Error.LOCALIZED_MESSAGE_KEY);
		if(retObj == null) {
			retObj = super.getLocalizedMessage();
		}
		return retObj;
	}
	
	public String getLocalizedRecoverySuggestion() {
		return this.getUserInfoStringForKey(Error.LOCALIZED_RECOVERY_SUGGESTION_KEY);
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Methods - Memory management
	
	public static Error newError(@NonNull Error error) {
		Error retObj = new Error(error.getDomain(), error.getCode(), error.getMessage(), error.getUserInfo());
		retObj.setStackTrace(error.getStackTrace());
		return retObj;
	}
	
	public static Error newError(@NonNull Throwable throwable) {
		int code = Integer.MAX_VALUE;
		String domain = throwable.getClass().getSimpleName();
		String message = throwable.getMessage();
		Map<String, Object> userInfo = null;
		
		String localizedMessage = throwable.getLocalizedMessage();
		if((localizedMessage != null) && !localizedMessage.equals(message)) {
			userInfo = new HashMap<>(1);
			userInfo.put(Error.LOCALIZED_MESSAGE_KEY, localizedMessage);
		}
		
		Error retObj = new Error(domain, code, message, userInfo);
		retObj.setStackTrace(throwable.getStackTrace());
		return retObj;
	}
	
	public Error(@NonNull String domain, int code) {
		this(domain, code, null, (Throwable)null);
	}
	
	public Error(@NonNull String domain, int code, String message) {
		this(domain, code, message, (Throwable)null);
	}
	
	public Error(@NonNull String domain, int code, String message, Throwable underlyingError) {
		super(message, underlyingError);
		
		Map<String, Object> userInfo = null;
		if(underlyingError != null) {
			userInfo = new HashMap<>(1);
			userInfo.put(Error.UNDERLYING_ERROR_KEY, underlyingError);
		}
		
		this.code = code;
		this.domain = domain;
		this.userInfo = userInfo;
	}
	
	public Error(@NonNull String domain, int code, String message, Map<String, Object> userInfo) {
		super(message, ((userInfo == null) ? null : (Throwable)userInfo.get(Error.UNDERLYING_ERROR_KEY)));
		
		if(userInfo != null) {
			userInfo = new HashMap<>(userInfo);
		}
		
		this.code = code;
		this.domain = domain;
		this.userInfo = userInfo;
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Methods - Data management
	
	public String getUserInfoStringForKey(@NonNull String key) {
		Map<String, Object> userInfo = this.getUserInfo();
		if(userInfo == null) {
			return null;
		}
		
		Object retObj = userInfo.get(key);
		if(!(retObj instanceof String)) {
			return null;
		}
		
		return (String)retObj;
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Methods - Data management (Debug)
	
	public static @NonNull String getDebugMessageForError(Error error) {
		StringBuilder builder = new StringBuilder(String.format(Locale.US, "%s<%d> {domain = '%s', code = '%d'}", error.getClass().getSimpleName(), ObjectIdentifier.getID(error), error.getDomain(), error.getCode()));
		
		String message = error.getMessage();
		if(!Strings.isNullOrEmptyString(message)) {
			builder.append(String.format(Locale.US, ", message = '%s'", message));
		}
		
		Throwable underlyingError = error.getUnderlyingError();
		if(underlyingError != null) {
			builder.append(String.format(Locale.US, ", underlyingError = '%s'", Error.getDebugMessageForError(underlyingError)));
		}
		
		builder.append("}");
		
		return builder.toString();
	}
	
	public static @NonNull String getDebugMessageForError(Throwable throwable) {
		StringBuilder builder = new StringBuilder(String.format(Locale.US, "%s<%d>", throwable.getClass().getSimpleName(), ObjectIdentifier.getID(throwable)));
		
		boolean hasContent = false;
		
		String message = throwable.getMessage();
		if(!Strings.isNullOrEmptyString(message)) {
			hasContent = true;
			builder.append("{");
			builder.append(String.format(Locale.US, "message = '%s'", message));
		}
		
		Throwable underlyingError = throwable.getCause();
		if(underlyingError != null) {
			if(!hasContent) {
				hasContent = true;
				builder.append("{");
			} else {
				builder.append(", ");
			}
			
			builder.append(String.format(Locale.US, "underlyingError = '%s'", Error.getDebugMessageForError(underlyingError)));
		}
		
		if(hasContent) {
			builder.append("}");
		}
		
		return builder.toString();
	}
	
	public @NonNull String getDebugMessage() {
		return Error.getDebugMessageForError(this);
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Methods - Utilities
	
	public boolean checkAgainst(@NonNull String domain, int code) {
		return this.getDomain().equals(domain) && (this.getCode() == code);
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
}
