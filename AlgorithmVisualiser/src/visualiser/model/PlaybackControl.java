package visualiser.model;

/**
 * Thread-safe remote control shared between the GUI thread (pause/step/cancel/speed)
 * and the sorting thread (checkpoint()). Uses wait/notify on a private lock.
 */
public final class PlaybackControl {
    private final Object lock = new Object();
    private boolean paused, stepRequested, cancelled;
    private volatile int delayMillis = 20;

    public void setDelayMillis(int ms) { delayMillis = Math.max(0, ms); }

    public void setPaused(boolean p) {
        synchronized (lock) {
            paused = p;
            if (!p) stepRequested = false;
            lock.notifyAll();
        }
    }

    public void requestStep() {
        synchronized (lock) { stepRequested = true; lock.notifyAll(); }
    }

    public void cancel() {
        synchronized (lock) { cancelled = true; lock.notifyAll(); }
    }

    public boolean isCancelled() {
        synchronized (lock) { return cancelled; }
    }

    /** Called by the sorting thread after every visible operation: blocks while paused, then sleeps. */
    public void checkpoint() {
        boolean stepped = false;
        synchronized (lock) {
            try {
                while (paused && !stepRequested && !cancelled) lock.wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new SortCancelledException();
            }
            if (cancelled) throw new SortCancelledException();
            if (paused) { stepRequested = false; stepped = true; }
        }
        int d = delayMillis;
        if (!stepped && d > 0) sleep(d);
        else if (isCancelled()) throw new SortCancelledException();
    }

    /** Plain sleep that still reacts to cancellation. */
    public void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SortCancelledException();
        }
        if (isCancelled()) throw new SortCancelledException();
    }
}
