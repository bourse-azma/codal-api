package com.ernoxin.codalapi.client;

import com.ernoxin.codalapi.config.CodalRequestProperties;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Process-wide throttle shared by every CODAL RestTemplate. It limits concurrency and spaces the
 * start of upstream requests, including retries.
 */
@Component
public class CodalRequestThrottle {

    private final long minDelayNanos;
    private final Semaphore permits;
    private final Object scheduleMonitor = new Object();
    private long nextStartAtNanos;

    public CodalRequestThrottle(CodalRequestProperties properties) {
        this.minDelayNanos = TimeUnit.MILLISECONDS.toNanos(properties.minDelayMs());
        this.permits = new Semaphore(properties.maxConcurrent(), true);
    }

    public Permit acquire() throws IOException {
        boolean acquired = false;
        try {
            permits.acquire();
            acquired = true;
            waitForScheduledStart();
            return new Permit();
        } catch (InterruptedException ex) {
            if (acquired) {
                permits.release();
            }
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while waiting for the CODAL request throttle", ex);
        }
    }

    private void waitForScheduledStart() throws InterruptedException {
        synchronized (scheduleMonitor) {
            long now = System.nanoTime();
            long waitNanos = Math.max(0, nextStartAtNanos - now);
            if (waitNanos > 0) {
                TimeUnit.NANOSECONDS.sleep(waitNanos);
                now = System.nanoTime();
            }
            nextStartAtNanos = now + minDelayNanos;
        }
    }

    public final class Permit implements AutoCloseable {
        private boolean closed;

        private Permit() {
        }

        @Override
        public void close() {
            if (!closed) {
                closed = true;
                permits.release();
            }
        }
    }
}
