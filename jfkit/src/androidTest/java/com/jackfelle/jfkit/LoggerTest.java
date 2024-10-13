package com.jackfelle.jfkit;

import android.content.Context;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

@RunWith(AndroidJUnit4.class)
public class LoggerTest
{
	////////////////////////////////////////////////////////////////////////////////////////////////
	// region Properties
	
	private @Nullable File _folder;
	private @Nullable Logger logger;
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////
	// region Properties (Accessors)
	
	private @NonNull Context getContext() {
		Context retObj = InstrumentationRegistry.getInstrumentation().getTargetContext();
		Assert.assertEquals("com.jackfelle.jfkit.test", retObj.getPackageName());
		return retObj;
	}
	
	private synchronized @NonNull File getFolder(@NonNull Context context) {
		File retObj = this._folder;
		if(retObj == null) {
			retObj = context.getApplicationContext().getDir("Logs", Context.MODE_PRIVATE);
			Assert.assertNotNull("Failed to get logs directory.", retObj);
		}
		return retObj;
	}
	
	private @NonNull Logger getLogger() {
		Logger retObj = this.logger;
		Assert.assertNotNull(retObj);
		return retObj;
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////
	// region Lifecycle
	
	@Before public void setUp() {
		Logger.Settings settings = new Logger.Settings();
		settings.setFileName("Test.log");
		settings.rotation = Logger.Rotation.DAY;
		Logger logger = new Logger(this.getContext(), settings);
		logger.setOutputFilter(Logger.Output.ALL);
		logger.setSeverityFilter(Logger.Severity.INFO);
		this.logger = logger;
	}
	
	@After public void tearDown() {
		this.deleteTestLogFile();
		this.logger = null;
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////
	// region Tests
	
	@Test public void testPerformance() {
		Logger logger = this.getLogger();
		
		EnumSet<Logger.Output> output = Logger.Output.ALL;
		if(output.contains(Logger.Output.FILE)) {
			this.deleteTestLogFile();
		}
		
		EnumSet<Logger.Output> consoleOutput = EnumSet.of(Logger.Output.CONSOLE);
		int numberOfCycles = 10;
		int numberOfThreads = 100;
		int linesPerThread = 100;
		String tag = "JFKit";
		List<Long> elapsedTimes = new ArrayList<>();
		List<Thread> threads = new ArrayList<>();
		
		for(int i = 0; i < numberOfCycles; i++) {
			int cycle = i + 1;
			logger.log(tag, String.format(Locale.US, "Started logger performance test. [cycle = '%d']", cycle), consoleOutput, Logger.Severity.EMERGENCY, Logger.Hashtag.NONE);
			long startTime = System.currentTimeMillis();
			CountDownLatch latch = new CountDownLatch(numberOfThreads);
			for(int j = 0; j < numberOfThreads; j++) {
				int threadNumber = j + 1;
				threads.add(new Thread(() -> {
					for(int k = 0; k < linesPerThread; k++) {
						logger.log(tag, String.format(Locale.US, "Thread %d wrote %d lines. [cycle = '%d']", threadNumber, k + 1, cycle), output, Logger.Severity.EMERGENCY, Logger.Hashtag.NONE);
					}
					latch.countDown();
				}));
			}
			for(Thread thread : threads) {
				thread.start();
			}
			try {
				latch.await();
			} catch(InterruptedException exception) {
				logger.log(tag, String.format(Locale.US, "Failed to wait for threads. [cycle = '%d']", cycle), consoleOutput, Logger.Severity.EMERGENCY, Logger.Hashtag.NONE);
			}
			long elapsedTime = System.currentTimeMillis() - startTime;
			logger.log(tag, String.format(Locale.US, "Finished logger performance test. [cycle = '%d'; elapsedTime = '%d']", cycle, elapsedTime), consoleOutput, Logger.Severity.EMERGENCY, Logger.Hashtag.NONE);
			threads.clear();
			elapsedTimes.add(elapsedTime);
		}
		
		if(output.contains(Logger.Output.FILE)) {
			int expectedCount = numberOfThreads * linesPerThread * numberOfCycles;
			List<String> lines = this.readTestLogFileLines();
			int count = lines.size();
			Assert.assertTrue(String.format(Locale.US, "The test log file should have %d lines, not %d!\n", expectedCount, count), (count == expectedCount));
		}
		
		long totalTime = 0;
		for(Long elapsedTime : elapsedTimes) {
			totalTime += elapsedTime;
		}
		long averageTime = totalTime / elapsedTimes.size();
		logger.log(tag, String.format(Locale.US, "Logger performance test results available. [average = '%d'; values = '[%s]']", averageTime, elapsedTimes), consoleOutput, Logger.Severity.EMERGENCY, Logger.Hashtag.NONE);
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////
	// region Utilities
	
	private void deleteTestLogFile() {
		Logger logger = this.logger;
		if(logger == null) {
			return;
		}
		
		File file = logger.getCurrentFile();
		if(file.exists()) {
			Assert.assertTrue(String.format(Locale.US, "Failed to delete the test file. [file = '%s']", file), file.delete());
		}
	}
	
	private @Nullable FileInputStream newInputStream(@NonNull File file) {
		try {
			return new FileInputStream(file);
		} catch(FileNotFoundException exception) {
			return null;
		}
	}
	
	private @NonNull List<String> readTestLogFileLines() {
		File file = this.getLogger().getCurrentFile();
		Assert.assertTrue(String.format(Locale.US, "Test file not found. [file = '%s']", file), file.exists());
		
		FileInputStream inputStream = this.newInputStream(file);
		Assert.assertNotNull(inputStream);
		
		List<String> retObj = new ArrayList<>();
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
			while(true) {
				String line = reader.readLine();
				if(line == null) {
					break;
				}
				retObj.add(line);
			}
		} catch(IOException exception) {
			// Nothing to do.
		} finally {
			try {
				inputStream.close();
			} catch(IOException exception) {
				// Nothing to do.
			}
		}
		return retObj;
	}
	
	// endregion
	////////////////////////////////////////////////////////////////////////////////////////////////
}