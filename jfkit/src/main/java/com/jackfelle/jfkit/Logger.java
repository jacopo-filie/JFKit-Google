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

package com.jackfelle.jfkit;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.jackfelle.jfkit.functions.Consumer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.locks.Lock;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Logger
{
	////////////////////////////////////////////////////////////////////////////////////////////////
	// region Constants
	
	public static final @NonNull String FORMAT_DATE = "%1$@";
	public static final @NonNull String FORMAT_DATE_TIME = "%2$@";
	public static final @NonNull String FORMAT_MESSAGE = "%3$@";
	public static final @NonNull String FORMAT_PROCESS_ID = "%4$@";
	public static final @NonNull String FORMAT_SEVERITY = "%5$@";
	public static final @NonNull String FORMAT_TAG = "%6$@";
	public static final @NonNull String FORMAT_THREAD_ID = "%7$@";
	public static final @NonNull String FORMAT_TIME = "%8$@";
	private static final @NonNull String TAG = "JFKit|Logger";
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////
	// region Properties - File system
	
	public final @NonNull String fileName;
	public final @NonNull File folder;
	public final @NonNull Rotation rotation;
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////
	// region Properties - Filters
	
	private @NonNull EnumSet<Output> _outputFilter = Output.ALL;
	private @NonNull Severity _severityFilter = BuildConfig.DEBUG ? Severity.DEBUG : Severity.INFO;
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////
	// region Properties - Locks
	
	private final @NonNull Object lock = new Object();
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////
	// region Properties - Log format
	
	public final @NonNull DateFormat dateFormat;
	public final @NonNull DateFormat dateTimeFormat;
	public final @NonNull String textFormat;
	public final @NonNull Set<String> textFormatActiveValues;
	public final @NonNull DateFormat timeFormat;
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////
	// region Properties - Observers
	
	private final @NonNull Observers<Delegate> delegates = new Observers<>();
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////
	// region Properties (Accessors) - File system
	
	public @NonNull File getCurrentFile() {
		return this.getFileForDate(new Date());
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////
	// region Properties (Accessors) - Filters
	
	public @NonNull EnumSet<Output> getOutputFilter() {
		synchronized(this.lock) {
			return this._outputFilter;
		}
	}
	
	public void setOutputFilter(@NonNull Output filter) {
		this.setOutputFilter(EnumSet.of(filter));
	}
	
	public void setOutputFilter(@NonNull EnumSet<Output> filter) {
		synchronized(this.lock) {
			this._outputFilter = filter;
		}
	}
	
	public @NonNull Severity getSeverityFilter() {
		synchronized(this.lock) {
			return this._severityFilter;
		}
	}
	
	public void setSeverityFilter(@NonNull Severity filter) {
		synchronized(this.lock) {
			this._severityFilter = filter;
		}
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////
	// region Lifecycle
	
	public Logger(@NonNull Context context) {
		this(context, new Settings());
	}
	
	public Logger(@NonNull Context context, @NonNull Settings settings) {
		String textFormat = settings.getTextFormat();
		
		this.dateFormat = settings.getDateFormat();
		this.dateTimeFormat = settings.getDateTimeFormat();
		this.fileName = settings.getFileName();
		this.folder = settings.getFolder(context);
		this.rotation = settings.rotation;
		this.textFormat = textFormat;
		this.textFormatActiveValues = Logger.findActiveValues(textFormat);
		this.timeFormat = settings.getTimeFormat();
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////
	// region Methods - File system
	
	private @Nullable Date getFileCreationDate(@NonNull File file) {
		if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
			return null;
		}
		
		Exception exception = null;
		try {
			BasicFileAttributes attributes = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
			if(attributes != null) {
				return new Date(attributes.creationTime().toMillis());
			}
		} catch(IOException e) {
			exception = e;
		}
		
		Log.e(Logger.TAG, String.format(Locale.US, "Could not read creation date of log file. [path = '%s'] %s", file.getPath(), Hashtag.join(Hashtag.ERROR, Hashtag.FILE_SYSTEM)), exception);
		return null;
	}
	
	public @NonNull File getFileForDate(@NonNull Date date) {
		File folder = this.folder;
		String fileName = this.fileName;
		
		Rotation rotation = this.rotation;
		if(rotation != Rotation.NONE) {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(date);
			int suffix = calendar.get(rotation.calendarComponent);
			
			String extension = null;
			int index = fileName.lastIndexOf(".");
			if(index > -1) {
				extension = fileName.substring(index);
				fileName = fileName.substring(0, index);
			}
			
			fileName = fileName + "-" + suffix;
			if(!Texts.isNullOrEmpty(extension)) {
				fileName = fileName + extension;
			}
		}
		return new File(folder, fileName);
	}
	
	private boolean prepareFile(@NonNull File file, @NonNull Date currentDate) {
		String filePath = file.getPath();
		boolean exists = file.exists();
		if(exists) {
			// Reads the creation date of the existing log file and check if it's still valid.
			// If the file creation date is not readable, it uses the last modified date.
			Date creationDate = this.getFileCreationDate(file);
			if(creationDate == null) {
				creationDate = new Date(file.lastModified());
			}
			
			// If the log file is not valid anymore, it goes on with the method and replaces it with
			// a new empty one.
			if(this.validateFileByComparingDates(creationDate, currentDate)) {
				return true;
			}
			
			// Deletes the old log file.
			if(!file.delete()) {
				Log.e(Logger.TAG, String.format(Locale.US, "Could not delete previous log file. [path = '%s'] %s", filePath, Hashtag.join(Hashtag.ERROR, Hashtag.FILE_SYSTEM)));
				return false;
			}
		} else {
			Log.i(Logger.TAG, String.format(Locale.US, "Log file does not exist. Checking parent folder. [path = '%s'] %s", filePath, Hashtag.join(Hashtag.ATTENTION, Hashtag.FILE_SYSTEM)));
			
			// Gets the parent folder.
			File folder = file.getParentFile();
			if(folder == null) {
				Log.e(Logger.TAG, String.format(Locale.US, "Could not get parent folder. [path = '%s'] %s", filePath, Hashtag.join(Hashtag.ERROR, Hashtag.FILE_SYSTEM)));
				return false;
			}
			
			// Checks if the parent folder exists.
			String folderPath = folder.getPath();
			if(folder.exists()) {
				Log.i(Logger.TAG, String.format(Locale.US, "Parent folder exists. Creating log file. [path = '%s'] %s", folderPath, Hashtag.FILE_SYSTEM.string));
			} else {
				Log.i(Logger.TAG, String.format(Locale.US, "Parent folder does not exist. Creating it. [path = '%s'] %s", folderPath, Hashtag.join(Hashtag.ATTENTION, Hashtag.FILE_SYSTEM)));
				
				// Creates the parent folder.
				if(folder.mkdirs()) {
					Log.i(Logger.TAG, String.format(Locale.US, "Parent folder created. Creating log file. [path = '%s'] %s", folderPath, Hashtag.FILE_SYSTEM.string));
				} else {
					Log.e(Logger.TAG, String.format(Locale.US, "Could not create parent folder. [path = '%s'] %s", folderPath, Hashtag.join(Hashtag.ERROR, Hashtag.FILE_SYSTEM)));
					return false;
				}
			}
		}
		
		boolean isCreated = false;
		Exception exception = null;
		try {
			// Creates the empty log file.
			isCreated = file.createNewFile();
		} catch(IOException e) {
			exception = e;
		}
		if(isCreated) {
			Log.i(Logger.TAG, String.format(Locale.US, "Log file %s. [path = '%s'] %s", (exists ? "overwritten" : "created"), filePath, Hashtag.FILE_SYSTEM.string));
		} else {
			Log.e(Logger.TAG, String.format(Locale.US, "Could not create log file. [path = '%s'] %s", filePath, Hashtag.join(Hashtag.ERROR, Hashtag.FILE_SYSTEM)), exception);
		}
		return isCreated;
	}
	
	private boolean validateFileByComparingDates(@NonNull Date creationDate, @NonNull Date currentDate) {
		Calendar creationCalendar = Calendar.getInstance();
		Calendar currentCalendar = Calendar.getInstance();
		
		creationCalendar.setTime(creationDate);
		currentCalendar.setTime(currentDate);
		
		if((creationCalendar.get(Calendar.ERA) != currentCalendar.get(Calendar.ERA)) || (creationCalendar.get(Calendar.YEAR) != currentCalendar.get(Calendar.YEAR))) {
			return false;
		}
		
		switch(this.rotation) {
			case HOUR: {
				if(creationCalendar.get(Calendar.HOUR_OF_DAY) != currentCalendar.get(Calendar.HOUR_OF_DAY)) {
					return false;
				}
			}
			case DAY: {
				if(creationCalendar.get(Calendar.DAY_OF_MONTH) != currentCalendar.get(Calendar.DAY_OF_MONTH)) {
					return false;
				}
			}
			case WEEK: {
				if(creationCalendar.get(Calendar.WEEK_OF_MONTH) != currentCalendar.get(Calendar.WEEK_OF_MONTH)) {
					return false;
				}
			}
			case MONTH: {
				if(creationCalendar.get(Calendar.MONTH) != currentCalendar.get(Calendar.MONTH)) {
					return false;
				}
			}
			default: {
				return true;
			}
		}
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////
	// region Methods - Service
	
	public void log(@NonNull String tag, @NonNull String message, @NonNull EnumSet<Output> outputs, @NonNull Severity severity, @NonNull EnumSet<Hashtag> hashtags) {
		this.logAll(tag, Collections.singletonList(message), outputs, severity, hashtags);
	}
	
	public void logAll(@NonNull String tag, @NonNull List<String> messages, @NonNull EnumSet<Output> outputs, @NonNull Severity severity, @NonNull EnumSet<Hashtag> hashtags) {
		synchronized(this.lock) {
			EnumSet<Output> outputFilter = this._outputFilter;
			Severity severityFilter = this._severityFilter;
			
			// Filters by severity.
			if(severity.value > severityFilter.value) {
				return;
			}
			
			// Filters by output.
			outputs.retainAll(outputFilter);
			if(outputs.isEmpty()) {
				return;
			}
			
			// Prepares hashtags.
			String joinedHashtags = Hashtag.join(hashtags);
			
			Set<String> textFormatActiveValues = this.textFormatActiveValues;
			Map<String, String> values = new HashMap<>(textFormatActiveValues.size());
			
			// Adds the tag.
			if(textFormatActiveValues.contains(Logger.FORMAT_TAG)) {
				values.put(Logger.FORMAT_TAG, tag);
			}
			
			// Converts the severity level to string.
			if(textFormatActiveValues.contains(Logger.FORMAT_SEVERITY)) {
				values.put(Logger.FORMAT_SEVERITY, severity.string);
			}
			
			// Gets the current process ID.
			if(textFormatActiveValues.contains(Logger.FORMAT_PROCESS_ID)) {
				values.put(Logger.FORMAT_PROCESS_ID, Integer.toString(android.os.Process.myPid()));
			}
			
			// Gets the current thread ID.
			if(textFormatActiveValues.contains(Logger.FORMAT_THREAD_ID)) {
				values.put(Logger.FORMAT_THREAD_ID, Integer.toString(android.os.Process.myTid()));
			}
			
			// Prepares a buffer for log texts.
			List<String> texts = new ArrayList<>(messages.size());
			
			// From now on we must remain in critical section to assure the timestamp is properly
			// ordered and prevent threads from writing at the same time.
			Date currentDate = new Date();
			
			// Gets the current date.
			if(textFormatActiveValues.contains(Logger.FORMAT_DATE)) {
				values.put(Logger.FORMAT_DATE, Dates.newStringFromDate(currentDate, this.dateFormat));
			}
			
			// Gets the current date and time.
			if(textFormatActiveValues.contains(Logger.FORMAT_DATE_TIME)) {
				values.put(Logger.FORMAT_DATE_TIME, Dates.newStringFromDate(currentDate, this.dateTimeFormat));
			}
			
			// Gets the current time.
			if(textFormatActiveValues.contains(Logger.FORMAT_TIME)) {
				values.put(Logger.FORMAT_TIME, Dates.newStringFromDate(currentDate, this.timeFormat));
			}
			
			Consumer<String> addMessageToValues = textFormatActiveValues.contains(Logger.FORMAT_MESSAGE) ? message -> {
				values.put(Logger.FORMAT_MESSAGE, (joinedHashtags.isEmpty() ? message : String.format("%s %s", message, joinedHashtags)));
			} : null;
			
			String textFormat = this.textFormat;
			for(String message : messages) {
				// Gets the message, appending hashtags if needed.
				if(addMessageToValues != null) {
					addMessageToValues.consume(message);
				}
				
				// Prepares the log text.
				texts.add(Texts.newStringByReplacingKeysInFormat(textFormat, values));
			}
			
			// Logs to console if needed.
			if(outputs.contains(Output.CONSOLE)) {
				this.logMessagesToConsole(messages, joinedHashtags, tag, severity);
			}
			
			// Logs to file if needed.
			if(outputs.contains(Output.FILE)) {
				this.logTextsToFile(texts, currentDate);
			}
			
			// Forwards the log message to the registered delegates if needed.
			if(outputs.contains(Output.DELEGATES)) {
				this.logTextsToDelegates(texts, currentDate);
			}
		}
	}
	
	private void logMessagesToConsole(@NonNull List<String> messages, @NonNull String hashTags, @NonNull String logTag, @NonNull Severity severity) {
		if(messages.isEmpty()) {
			return;
		}
		
		if(hashTags.isEmpty()) {
			for(String message : messages) {
				Log.println(severity.priority, logTag, message);
			}
		} else {
			for(String message : messages) {
				Log.println(severity.priority, logTag, String.format("%s %s", message, hashTags));
			}
		}
	}
	
	private void logTextsToDelegates(@NonNull List<String> texts, @NonNull Date currentDate) {
		if(texts.isEmpty()) {
			return;
		}
		
		this.delegates.notify(delegate -> {
			delegate.logTexts(this, texts, currentDate);
		});
	}
	
	private void logTextsToFile(@NonNull List<String> texts, @NonNull Date currentDate) {
		if(texts.isEmpty()) {
			return;
		}
		
		// Gets the right log file.
		File file = this.getFileForDate(currentDate);
		
		// Prepares the log file.
		if(!this.prepareFile(file, currentDate)) {
			Log.e(Logger.TAG, String.format(Locale.US, "Failed to prepare log file. [path = '%s'] %s", file.getPath(), Hashtag.join(Hashtag.ERROR, Hashtag.FILE_SYSTEM)));
			return;
		}
		
		// Opens the file.
		FileOutputStream outputStream;
		try {
			outputStream = new FileOutputStream(file, true);
		} catch(FileNotFoundException exception) {
			Log.e(Logger.TAG, String.format(Locale.US, "Failed to open log file. [path = '%s'] %s", file.getPath(), Hashtag.join(Hashtag.ERROR, Hashtag.FILE_SYSTEM)), exception);
			return;
		}
		
		// Appends the new texts.
		OutputStreamWriter writer = new OutputStreamWriter(outputStream);
		try {
			for(String text : texts) {
				writer.write(text);
				writer.write("\n");
			}
			writer.flush();
		} catch(IOException exception) {
			Log.e(Logger.TAG, String.format(Locale.US, "Failed to write into log file. [path = '%s'] %s", file.getPath(), Hashtag.join(Hashtag.ERROR, Hashtag.FILE_SYSTEM)), exception);
		}
		
		// Closes the file.
		try {
			writer.close();
			outputStream.close();
		} catch(IOException exception) {
			Log.e(Logger.TAG, String.format(Locale.US, "Failed to close log file. [path = '%s'] %s", file.getPath(), Hashtag.join(Hashtag.ERROR, Hashtag.FILE_SYSTEM)), exception);
		}
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////////
	// region Methods - Observers
	
	public void addDelegate(@NonNull Delegate delegate) {
		this.delegates.add(delegate);
	}
	
	public void removeDelegate(@NonNull Delegate delegate) {
		this.delegates.remove(delegate);
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////
	// region Methods - Utilities
	
	private static @NonNull Set<String> findActiveValues(@NonNull String textFormat) {
		List<String> values = Arrays.asList(Logger.FORMAT_DATE, Logger.FORMAT_DATE_TIME, Logger.FORMAT_MESSAGE, Logger.FORMAT_PROCESS_ID, Logger.FORMAT_SEVERITY, Logger.FORMAT_TAG, Logger.FORMAT_THREAD_ID, Logger.FORMAT_TIME);
		Set<String> retObj = new HashSet<>();
		for(String value : values) {
			if(textFormat.contains(value)) {
				retObj.add(value);
			}
		}
		return retObj;
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////
	// region Enums
	
	public enum Output
	{
		////////////////////////////////////////////////////////////////////////////////////////////
		// region Values
		
		CONSOLE,
		DELEGATES,
		FILE;
		
		// endregion
		////////////////////////////////////////////////////////////////////////////////////////////
		// region Constants
		
		public static final @NonNull EnumSet<Output> ALL = EnumSet.allOf(Output.class);
		
		// endregion
		////////////////////////////////////////////////////////////////////////////////////////////
	}
	
	public enum Rotation
	{
		////////////////////////////////////////////////////////////////////////////////////////////
		// region Values
		
		NONE(-1),
		HOUR(Calendar.HOUR_OF_DAY),
		DAY(Calendar.DAY_OF_MONTH),
		WEEK(Calendar.WEEK_OF_MONTH),
		MONTH(Calendar.MONTH);
		
		// endregion
		////////////////////////////////////////////////////////////////////////////////////////////
		// region Properties
		
		public final int calendarComponent;
		
		// endregion
		////////////////////////////////////////////////////////////////////////////////////////////
		// region Lifecycle
		
		Rotation(int calendarComponent) {
			this.calendarComponent = calendarComponent;
		}
		
		// endregion
		////////////////////////////////////////////////////////////////////////////////////////////
	}
	
	public enum Severity
	{
		////////////////////////////////////////////////////////////////////////////////////////////
		// region Values
		
		EMERGENCY(0, Log.ASSERT, "Emergency"),
		ALERT(1, Log.ASSERT, "Alert"),
		CRITICAL(2, Log.ERROR, "Critical"),
		ERROR(3, Log.ERROR, "Error"),
		WARNING(4, Log.WARN, "Warning"),
		NOTICE(5, Log.INFO, "Notice"),
		INFO(6, Log.INFO, "Info"),
		DEBUG(7, Log.DEBUG, "Debug"),
		VERBOSE(8, Log.VERBOSE, "Verbose");
		
		// endregion
		////////////////////////////////////////////////////////////////////////////////////////////
		// region Properties
		
		public final int priority;
		public final @NonNull String string;
		public final int value;
		
		// endregion
		////////////////////////////////////////////////////////////////////////////////////////////
		// region Lifecycle
		
		Severity(int value, int priority, @NonNull String string) {
			this.priority = priority;
			this.string = string;
			this.value = value;
		}
		
		// endregion
		////////////////////////////////////////////////////////////////////////////////////////////
	}
	
	public enum Hashtag
	{
		////////////////////////////////////////////////////////////////////////////////////////////
		// region Values
		
		ATTENTION("#Attention"),
		CLUE("#Clue"),
		COMMENT("#Comment"),
		CRITICAL("#Critical"),
		DEVELOPER("#Developer"),
		ERROR("#Error"),
		FILE_SYSTEM("#FileSystem"),
		HARDWARE("#Hardware"),
		MARKER("#Marker"),
		NETWORK("#Network"),
		SECURITY("#Security"),
		SYSTEM("#System"),
		USER("#User");
		
		// endregion
		////////////////////////////////////////////////////////////////////////////////////////////
		// region Constants
		
		public static final @NonNull EnumSet<Hashtag> NONE = EnumSet.noneOf(Hashtag.class);
		
		// endregion
		////////////////////////////////////////////////////////////////////////////////////////////
		// region Properties
		
		public final @NonNull String string;
		
		// endregion
		////////////////////////////////////////////////////////////////////////////////////////////
		// region Lifecycle
		
		Hashtag(@NonNull String string) {
			this.string = string;
		}
		
		// endregion
		////////////////////////////////////////////////////////////////////////////////////////////
		// region Methods
		
		public static @NonNull String join(@NonNull EnumSet<Hashtag> hashtags) {
			int size = hashtags.size();
			if(size == 0) {
				return "";
			} else if(size == 1) {
				return hashtags.iterator().next().string;
			}
			
			List<String> strings = new ArrayList<>(size);
			for(Hashtag hashtag : hashtags) {
				strings.add(hashtag.string);
			}
			Collections.sort(strings);
			return String.join(" ", strings);
		}
		
		public static @NonNull String join(@NonNull Hashtag... hashtags) {
			int size = hashtags.length;
			if(size == 0) {
				return "";
			} else if(size == 1) {
				return hashtags[0].string;
			} else {
				return Hashtag.join(EnumSet.copyOf(Arrays.asList(hashtags)));
			}
		}
		
		// endregion
		////////////////////////////////////////////////////////////////////////////////////////////
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////
	// region Classes
	
	public static class Settings
	{
		////////////////////////////////////////////////////////////////////////////////////////////
		// region Properties - File system
		
		private @Nullable String _fileName;
		private @Nullable File _folder;
		public @NonNull Rotation rotation = Rotation.NONE;
		
		// endregion
		////////////////////////////////////////////////////////////////////////////////////////////
		// region Properties - Log format
		
		private @Nullable DateFormat _dateFormat;
		private @Nullable DateFormat _dateTimeFormat;
		private @Nullable String _textFormat;
		private @Nullable DateFormat _timeFormat;
		
		// endregion
		////////////////////////////////////////////////////////////////////////////////////////////
		// region Properties (Accessors) - File system
		
		public @NonNull String getFileName() {
			String retObj = this._fileName;
			if(retObj == null) {
				retObj = this.newDefaultFileName();
				this._fileName = retObj;
			}
			return retObj;
		}
		
		public void setFileName(@Nullable String fileName) {
			this._fileName = fileName;
		}
		
		public @NonNull File getFolder(@NonNull Context context) {
			File retObj = this._folder;
			if(retObj == null) {
				retObj = this.newDefaultFolder(context);
				this._folder = retObj;
			}
			return retObj;
		}
		
		public void setFolder(@Nullable File folder) {
			this._folder = folder;
		}
		
		// endregion
		////////////////////////////////////////////////////////////////////////////////////////////
		// region Properties (Accessors) - Log format
		
		public @NonNull DateFormat getDateFormat() {
			DateFormat retObj = this._dateFormat;
			if(retObj == null) {
				retObj = this.newDefaultDateFormat();
				this._dateFormat = retObj;
			}
			return retObj;
		}
		
		public void setDateFormat(@Nullable DateFormat format) {
			this._dateFormat = format;
		}
		
		public @NonNull DateFormat getDateTimeFormat() {
			DateFormat retObj = this._dateTimeFormat;
			if(retObj == null) {
				retObj = this.newDefaultDateTimeFormat();
				this._dateTimeFormat = retObj;
			}
			return retObj;
		}
		
		public void setDateTimeFormat(@Nullable DateFormat format) {
			this._dateTimeFormat = format;
		}
		
		public @NonNull String getTextFormat() {
			String retObj = this._textFormat;
			if(retObj == null) {
				retObj = this.newDefaultTextFormat();
				this._textFormat = retObj;
			}
			return retObj;
		}
		
		public void setTextFormat(@Nullable String format) {
			this._textFormat = format;
		}
		
		public @NonNull DateFormat getTimeFormat() {
			DateFormat retObj = this._timeFormat;
			if(retObj == null) {
				retObj = this.newDefaultTimeFormat();
				this._timeFormat = retObj;
			}
			return retObj;
		}
		
		public void setTimeFormat(@Nullable DateFormat format) {
			this._timeFormat = format;
		}
		
		// endregion
		////////////////////////////////////////////////////////////////////////////////////////////
		// region Methods - Utilities
		
		private @NonNull DateFormat newDefaultDateFormat() {
			DateFormat retObj = new SimpleDateFormat(this.newDefaultDateFormatPattern(), Locale.getDefault());
			retObj.setTimeZone(TimeZone.getDefault());
			return retObj;
		}
		
		private @NonNull String newDefaultDateFormatPattern() {
			return "yyyy/MM/dd";
		}
		
		private @NonNull DateFormat newDefaultDateTimeFormat() {
			Locale locale = Locale.getDefault();
			String pattern = String.format(locale, "%s %s", this.newDefaultDateFormatPattern(), this.newDefaultTimeFormatPattern());
			DateFormat retObj = new SimpleDateFormat(pattern, Locale.getDefault());
			retObj.setTimeZone(TimeZone.getDefault());
			return retObj;
		}
		
		private @NonNull String newDefaultFileName() {
			return "Logs.log";
		}
		
		private @NonNull File newDefaultFolder(@NonNull Context context) {
			return context.getApplicationContext().getDir("Logs", Context.MODE_PRIVATE);
		}
		
		private @NonNull String newDefaultTextFormat() {
			// 2024-03-08 00:17:37.598 590-1710/? D/OomAdjuster: Not killing cached processes
			return String.format("%s %s [%s:%s] %s", Logger.FORMAT_DATE_TIME, Logger.FORMAT_TAG, Logger.FORMAT_PROCESS_ID, Logger.FORMAT_THREAD_ID, Logger.FORMAT_MESSAGE);
		}
		
		private @NonNull DateFormat newDefaultTimeFormat() {
			DateFormat retObj = new SimpleDateFormat(this.newDefaultTimeFormatPattern(), Locale.getDefault());
			retObj.setTimeZone(TimeZone.getDefault());
			return retObj;
		}
		
		private @NonNull String newDefaultTimeFormatPattern() {
			return "HH:mm:ss.SSSZ";
		}
		
		// endregion
		////////////////////////////////////////////////////////////////////////////////////////////
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////
	// region Interfaces
	
	public interface Delegate
	{
		void logTexts(@NonNull Logger sender, @NonNull List<String> texts, @NonNull Date date);
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////
}
