//
//	The MIT License (MIT)
//
//	Copyright © 2018-2024 Jacopo Filié
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

import android.text.format.DateUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

import androidx.annotation.NonNull;

public abstract class Dates
{
	////////////////////////////////////////////////////////////////////////////////////////////////
	// region Methods - Calendars
	
	public static @NonNull Calendar newCalendarFromComponents(int year, int month, int day) {
		return Dates.newCalendarFromComponents(year, month, day, 0, 0, 0, 0);
	}
	
	public static @NonNull Calendar newCalendarFromComponents(int year, int month, int day, int hours, int minutes, int seconds) {
		return Dates.newCalendarFromComponents(year, month, day, hours, minutes, seconds, 0);
	}
	
	public static @NonNull Calendar newCalendarFromComponents(int year, int month, int day, int hours, int minutes, int seconds, int milliseconds) {
		year = Math.max(-1, year);
		month = Math.max(-1, month);
		day = Math.max(-1, day);
		hours = Math.max(-1, Math.min(23, hours));
		minutes = Math.max(-1, Math.min(59, minutes));
		seconds = Math.max(-1, Math.min(59, seconds));
		milliseconds = Math.max(-1, Math.min(999, milliseconds));
		
		Calendar retObj = Calendar.getInstance();
		
		if(year != -1) {
			retObj.set(Calendar.YEAR, year);
		}
		if(month != -1) {
			retObj.set(Calendar.MONTH, month);
		}
		if(day != -1) {
			retObj.set(Calendar.DAY_OF_MONTH, day);
		}
		if(hours != -1) {
			retObj.set(Calendar.HOUR_OF_DAY, hours);
		}
		if(minutes != -1) {
			retObj.set(Calendar.MINUTE, minutes);
		}
		if(seconds != -1) {
			retObj.set(Calendar.SECOND, seconds);
		}
		if(milliseconds != -1) {
			retObj.set(Calendar.MILLISECOND, milliseconds);
		}
		
		return retObj;
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////
	// region Methods - Dates & Time
	
	public static @NonNull Date newDateFromComponents(int year, int month, int day) {
		return Dates.newDateTimeFromComponents(year, month, day, 0, 0, 0, 0);
	}
	
	public static @NonNull Date newDateFromString(@NonNull String dateString, @NonNull DateFormat dateFormat) throws ParseException {
		synchronized(Objects.requireNonNull(dateFormat)) {
			return Objects.requireNonNull(dateFormat.parse(dateString));
		}
	}
	
	public static @NonNull Date newDateTimeFromComponents(int year, int month, int day, int hours, int minutes, int seconds) {
		return Dates.newDateTimeFromComponents(year, month, day, hours, minutes, seconds, 0);
	}
	
	public static @NonNull Date newDateTimeFromComponents(int year, int month, int day, int hours, int minutes, int seconds, int milliseconds) {
		return Dates.newCalendarFromComponents(year, month, day, hours, minutes, seconds, milliseconds).getTime();
	}
	
	public static @NonNull Date newTimeFromComponents(int hours, int minutes, int seconds) {
		return Dates.newDateTimeFromComponents(0, 0, 0, hours, minutes, seconds, 0);
	}
	
	public static @NonNull Date newTimeFromComponents(int hours, int minutes, int seconds, int milliseconds) {
		return Dates.newDateTimeFromComponents(0, 0, 0, hours, minutes, seconds, milliseconds);
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////
	// region Methods - Texts
	
	public static @NonNull String newStringFromDate(@NonNull Date date, @NonNull DateFormat dateFormat) {
		synchronized(Objects.requireNonNull(dateFormat)) {
			return dateFormat.format(date);
		}
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////
	// region Methods - Utilities
	
	public static @NonNull Date earlierDate(@NonNull Date date1, @NonNull Date date2) {
		return (date1.before(date2) ? date1 : date2);
	}
	
	public static boolean isSameDate(@NonNull Calendar calendar1, @NonNull Calendar calendar2) {
		return (calendar1.get(Calendar.ERA) == calendar2.get(Calendar.ERA)) && (calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR)) && (calendar1.get(Calendar.DAY_OF_YEAR) == calendar2.get(Calendar.DAY_OF_YEAR));
	}
	
	public static boolean isSameDate(@NonNull Date date1, @NonNull Date date2) {
		return Dates.isSameDate(date1, date2, Calendar.getInstance(), Calendar.getInstance());
	}
	
	public static boolean isSameDate(@NonNull Date date1, @NonNull Date date2, @NonNull Calendar calendar1, @NonNull Calendar calendar2) {
		calendar1.setTime(date1);
		calendar2.setTime(date2);
		return Dates.isSameDate(calendar1, calendar2);
	}
	
	public static boolean isToday(@NonNull Date date) {
		return DateUtils.isToday(date.getTime());
	}
	
	public static boolean isYesterday(@NonNull Date date) {
		Calendar dateCalendar = Calendar.getInstance();
		dateCalendar.setTime(date);
		
		Calendar yesterdayCalendar = Calendar.getInstance();
		yesterdayCalendar.add(Calendar.DATE, -1);
		
		return (dateCalendar.get(Calendar.ERA) == yesterdayCalendar.get(Calendar.ERA)) && (dateCalendar.get(Calendar.YEAR) == yesterdayCalendar.get(Calendar.YEAR)) && (dateCalendar.get(Calendar.DAY_OF_YEAR) == yesterdayCalendar.get(Calendar.DAY_OF_YEAR));
	}
	
	public static @NonNull Date laterDate(@NonNull Date date1, @NonNull Date date2) {
		return (date1.after(date2) ? date1 : date2);
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////
}
