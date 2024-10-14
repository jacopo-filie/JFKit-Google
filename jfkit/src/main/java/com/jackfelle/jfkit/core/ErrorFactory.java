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

package com.jackfelle.jfkit.core;

import com.jackfelle.jfkit.data.Error;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import androidx.annotation.NonNull;

public abstract class ErrorFactory
{
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Properties - Data
	
	private final @NonNull String domain;
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Properties - Memory
	
	private static final @NonNull Map<String, ErrorFactory> SHARED_FACTORIES;
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Properties (Accessors) - Data
	
	public @NonNull String getDomain() {
		return this.domain;
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Methods - Memory management
	
	static {
		SHARED_FACTORIES = new HashMap<>();
	}
	
	public static ErrorFactory getSharedFactory(@NonNull String key) {
		synchronized(ErrorFactory.class) {
			return SHARED_FACTORIES.get(key);
		}
	}
	
	public static void registerSharedFactory(@NonNull ErrorFactory factory, @NonNull String key) {
		synchronized(ErrorFactory.class) {
			SHARED_FACTORIES.put(key, factory);
		}
	}
	
	public static void unregisterSharedFactory(@NonNull String key) {
		synchronized(ErrorFactory.class) {
			SHARED_FACTORIES.remove(key);
		}
	}
	
	public ErrorFactory(@NonNull String domain) {
		// Data
		this.domain = domain;
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Methods - Data management
	
	public String getLocalizedFailureReasonForErrorCode(int errorCode) {
		return this.getLocalizedFailureReasonForErrorCode(errorCode, null);
	}
	
	@SuppressWarnings("SameReturnValue") public String getLocalizedFailureReasonForErrorCode(int errorCode, Map<String, String> items) {
		return null;
	}
	
	public String getLocalizedMessageForErrorCode(int errorCode) {
		return this.getLocalizedMessageForErrorCode(errorCode, null);
	}
	
	@SuppressWarnings("SameReturnValue") public String getLocalizedMessageForErrorCode(int errorCode, Map<String, String> items) {
		return null;
	}
	
	public String getLocalizedRecoverySuggestionForErrorCode(int errorCode) {
		return this.getLocalizedRecoverySuggestionForErrorCode(errorCode, null);
	}
	
	@SuppressWarnings("SameReturnValue") public String getLocalizedRecoverySuggestionForErrorCode(int errorCode, Map<String, String> items) {
		return null;
	}
	
	public Map<String, Object> getUserInfoForErrorCode(int errorCode) {
		return this.getUserInfoForErrorCode(errorCode, null, null);
	}
	
	public Map<String, Object> getUserInfoForErrorCode(int errorCode, Throwable underlyingError, Map<String, Map<String, String>> items) {
		String failureReason = this.getLocalizedFailureReasonForErrorCode(errorCode, ((items != null) ? items.get(Error.LOCALIZED_FAILURE_REASON_KEY) : null));
		String message = this.getLocalizedMessageForErrorCode(errorCode, ((items != null) ? items.get(Error.LOCALIZED_MESSAGE_KEY) : null));
		String recoverySuggestion = this.getLocalizedRecoverySuggestionForErrorCode(errorCode, ((items != null) ? items.get(Error.LOCALIZED_RECOVERY_SUGGESTION_KEY) : null));
		
		if((failureReason == null) && (message == null) && (recoverySuggestion == null) && (underlyingError == null)) {
			return null;
		}
		
		Map<String, Object> retObj = new HashMap<>(4);
		if(failureReason != null) {
			retObj.put(Error.LOCALIZED_FAILURE_REASON_KEY, failureReason);
		}
		if(message != null) {
			retObj.put(Error.LOCALIZED_MESSAGE_KEY, message);
		}
		if(recoverySuggestion != null) {
			retObj.put(Error.LOCALIZED_RECOVERY_SUGGESTION_KEY, recoverySuggestion);
		}
		if(underlyingError != null) {
			retObj.put(Error.UNDERLYING_ERROR_KEY, underlyingError);
		}
		return retObj;
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Methods - Data management (Debug)
	
	public @NonNull String getDebugStringForErrorCode(int errorCode) {
		return String.format(Locale.US, "Unknown error %d", errorCode);
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Methods - Factory management
	
	public @NonNull Error getError(int errorCode) {
		return this.getError(errorCode, null);
	}
	
	public @NonNull Error getError(int errorCode, String message) {
		Map<String, Object> userInfo = this.getUserInfoForErrorCode(errorCode);
		return this.getError(errorCode, message, userInfo);
	}
	
	public @NonNull Error getError(int errorCode, String message, Throwable underlyingError, Map<String, Map<String, String>> items) {
		Map<String, Object> userInfo = this.getUserInfoForErrorCode(errorCode, underlyingError, items);
		return this.getError(errorCode, message, userInfo);
	}
	
	public @NonNull Error getError(int errorCode, String message, Map<String, Object> userInfo) {
		return new Error(this.getDomain(), errorCode, message, userInfo);
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Methods - Factory management (Debug)
	
	public @NonNull Error getDebugPlaceholderError() {
		return this.getDebugPlaceholderError(null);
	}
	
	public @NonNull Error getDebugPlaceholderError(Throwable underlyingError) {
		return this.getError(Integer.MAX_VALUE, "This is a placeholder error: you should replace it before publishing your project.", underlyingError, null);
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
}
