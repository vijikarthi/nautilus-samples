/**
 * Copyright (c) 2017 Dell Inc., or its subsidiaries. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 */

package io.pravega.samples.flink;

import io.pravega.client.ClientFactory;
import io.pravega.client.admin.ReaderGroupManager;
import io.pravega.client.admin.StreamManager;
import io.pravega.client.stream.EventRead;
import io.pravega.client.stream.EventStreamReader;
import io.pravega.client.stream.EventStreamWriter;
import io.pravega.client.stream.EventWriterConfig;
import io.pravega.client.stream.ReaderConfig;
import io.pravega.client.stream.ReaderGroupConfig;
import io.pravega.client.stream.Stream;
import io.pravega.client.stream.StreamConfiguration;
import io.pravega.client.stream.impl.JavaSerializer;
import io.pravega.connectors.flink.PravegaConfig;
import org.apache.flink.api.java.utils.ParameterTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.BitSet;
import java.util.UUID;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

public class StreamUtils {

	private static final Logger log = LoggerFactory.getLogger(StreamUtils.class);

	public static final String DEFAULT_SCOPE = "nautilus-samples";

	public static final int DEFAULT_NUM_SEGMENTS = 1;

	private final int numSegments;

	final ParameterTool parameterTool;

	final PravegaConfig pravegaConfig;

	public StreamUtils(ParameterTool parameterTool) {
		this(parameterTool, DEFAULT_NUM_SEGMENTS);
	}

	public StreamUtils(ParameterTool parameterTool, int numSegments) {
		this.parameterTool = parameterTool;
		this.numSegments = numSegments;
		this.pravegaConfig = PravegaConfig.fromParams(parameterTool).withDefaultScope(DEFAULT_SCOPE);
	}

	public int getNumSegments() { return numSegments; }

	public PravegaConfig getPravegaConfig() {
		return pravegaConfig;
	}

	public void publishData(final Stream stream, final int numElements) {
		final EventStreamWriter<Integer> eventWriter = createWriter(stream.getStreamName(), stream.getScope());
		for (int i=1; i<=numElements; i++) {
			eventWriter.writeEvent(i);
		}
		eventWriter.close();
	}

	public EventStreamWriter<Integer> createWriter(final String streamName, final String scope) {
		ClientFactory clientFactory = ClientFactory.withScope(scope, pravegaConfig.getClientConfig());
		return clientFactory.createEventWriter(
				streamName,
				new JavaSerializer<>(),
				EventWriterConfig.builder().build());
	}

	public void validateJobOutputResults(Stream outputStream, int numElements) throws Exception {

		try (EventStreamReader<Integer> reader = getIntegerReader(outputStream)) {

			final BitSet duplicateChecker = new BitSet();

			for (int numElementsRemaining = numElements; numElementsRemaining > 0;) {
				final EventRead<Integer> eventRead = reader.readNextEvent(1000);
				final Integer event = eventRead.getEvent();

				if (event != null) {
					//log.info("event -> {}", event);
					numElementsRemaining--;
					assertFalse("found a duplicate", duplicateChecker.get(event));
					duplicateChecker.set(event);
				}
			}

			// no more events should be there
			assertNull("too many elements written", reader.readNextEvent(1000).getEvent());

			reader.close();
		}

	}

	public EventStreamReader<Integer> getIntegerReader(final Stream stream) {
		ReaderGroupManager readerGroupManager = ReaderGroupManager.withScope(stream.getScope(), pravegaConfig.getClientConfig());
		final String readerGroup = "testReaderGroup" + stream.getScope() + stream.getStreamName();
		readerGroupManager.createReaderGroup(
				readerGroup,
                ReaderGroupConfig.builder().stream(stream).build());

		ClientFactory clientFactory = ClientFactory.withScope(stream.getScope(), pravegaConfig.getClientConfig());
		final String readerGroupId = UUID.randomUUID().toString();
		return clientFactory.createReader(
				readerGroupId,
				readerGroup,
				new JavaSerializer<>(),
				ReaderConfig.builder().build());
	}

	/**
	 * Creates a Pravega stream with a default configuration.
	 *
	 * @param streamName the stream name (qualified or unqualified).
	 */
	public Stream createStream(String streamName) {
		return createStream(streamName, StreamConfiguration.builder().build());
	}

	/**
	 * Creates a Pravega stream with a given configuration.
	 *
	 * @param streamName the stream name (qualified or unqualified).
	 * @param streamConfig the stream configuration (scaling policy, retention policy).
	 */
	public Stream createStream(String streamName, StreamConfiguration streamConfig) {
		// resolve the qualified name of the stream
		Stream stream = pravegaConfig.resolve(streamName);

		try(StreamManager streamManager = StreamManager.create(pravegaConfig.getClientConfig())) {
			// create the requested scope (if necessary)
			streamManager.createScope(stream.getScope());

			// create the requested stream based on the given stream configuration
			streamManager.createStream(stream.getScope(), stream.getStreamName(), streamConfig);
		}

		return stream;
	}

}
