package it.thisone.iotter.cassandra;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ThreadLocalRandom;

import com.datastax.oss.driver.api.core.ConsistencyLevel;
import com.datastax.oss.driver.api.core.retry.RetryDecision;
import com.datastax.oss.driver.api.core.retry.RetryPolicy;
import com.datastax.oss.driver.api.core.servererrors.CoordinatorException;
import com.datastax.oss.driver.api.core.servererrors.WriteType;
import com.datastax.oss.driver.api.core.session.Request;

// https://stackoverflow.com/questions/30329956/cassandra-datastax-driver-retry-policy/30331540#30331540
public class CustomRetryPolicy implements RetryPolicy {
	private static Logger logger = LoggerFactory.getLogger(CustomRetryPolicy.class);

	private final int readAttempts;
	private final int writeAttempts;
	private final int unavailableAttempts;

	public CustomRetryPolicy(int readAttempts, int writeAttempts, int unavailableAttempts) {
		this.readAttempts = readAttempts;
		this.writeAttempts = writeAttempts;
		this.unavailableAttempts = unavailableAttempts;
	}

	@Override
	public RetryDecision onReadTimeout(Request request, ConsistencyLevel cl, int requiredResponses,
			int receivedResponses, boolean dataReceived, int retryCount) {
		if (retryCount < readAttempts) {
			logger.debug("onReadTimeout retry");
			try {
				int millis = ThreadLocalRandom.current().nextInt(100 * retryCount, 1000);
				Thread.sleep(millis);
			} catch (InterruptedException e1) {
			}
			return RetryDecision.RETRY_SAME;
		}
		logger.error("onReadTimeout rethrow");
		return RetryDecision.RETHROW;
	}

	@Override
	public RetryDecision onWriteTimeout(Request request, ConsistencyLevel cl, WriteType wt, int requiredResponses,
			int receivedResponses, int retryCount) {
		if (retryCount < writeAttempts) {
			logger.debug("onWriteTimeout retry");
			try {
				int millis = ThreadLocalRandom.current().nextInt(100 * retryCount, 1000);
				Thread.sleep(millis);
			} catch (InterruptedException e1) {
			}
			return RetryDecision.RETRY_SAME;
		}
		logger.error("onWriteTimeout rethrow");
		return RetryDecision.RETHROW;
	}

	@Override
	public RetryDecision onUnavailable(Request request, ConsistencyLevel cl, int requiredResponses,
			int receivedResponses, int retryCount) {
		if (retryCount < unavailableAttempts) {
			logger.debug("onUnavailable retry");
			return RetryDecision.RETRY_NEXT;
		}
		logger.error("onUnavailable rethrow");
		return RetryDecision.RETHROW;
	}

	@Override
	public RetryDecision onRequestAborted(Request request, Throwable error, int retryCount) {
		return RetryDecision.RETRY_NEXT;
	}

	@Override
	public RetryDecision onErrorResponse(Request request, CoordinatorException error, int retryCount) {
		return RetryDecision.RETRY_NEXT;
	}

	@Override
	public void close() {
		// nothing to do
	}
}
