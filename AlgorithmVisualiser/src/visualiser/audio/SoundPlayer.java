package visualiser.audio;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

/**
 * Plays a short sine "blip" per array access. Audio is written by ONE dedicated thread
 * with a tiny bounded queue (extra blips are dropped) so a fast sort never blocks on sound.
 */
public final class SoundPlayer implements AutoCloseable {
    private static final float RATE = 22050f;
    private static final int TONE_MS = 30;

    private final SourceDataLine line;
    private final ExecutorService exec;
    private volatile boolean enabled;

    public SoundPlayer() throws SoundUnavailableException {
        SourceDataLine l;
        try {
            AudioFormat fmt = new AudioFormat(RATE, 16, 1, true, false);
            l = AudioSystem.getSourceDataLine(fmt);
            l.open(fmt, 4096);
            l.start();
        } catch (LineUnavailableException | IllegalArgumentException | SecurityException e) {
            throw new SoundUnavailableException("Audio output not available: " + e.getMessage(), e);
        }
        this.line = l;
        this.exec = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(2),
                r -> { Thread t = new Thread(r, "sound-writer"); t.setDaemon(true); return t; },
                new ThreadPoolExecutor.DiscardPolicy());
    }

    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public void beep(int value, int maxValue) {
        if (!enabled) return;
        double freq = 200 + 1000.0 * value / Math.max(1, maxValue);
        exec.execute(() -> play(freq));
    }

    private void play(double freq) {
        int n = (int) (RATE * TONE_MS / 1000);
        byte[] buf = new byte[n * 2];
        for (int i = 0; i < n; i++) {
            double env = Math.min(1.0, Math.min(i, n - 1 - i) / (n * 0.15));   // fade in/out: no clicks
            short s = (short) (Math.sin(2 * Math.PI * freq * i / RATE) * env * 8000);
            buf[2 * i] = (byte) s;
            buf[2 * i + 1] = (byte) (s >> 8);
        }
        line.write(buf, 0, buf.length);
    }

    @Override public void close() {
        enabled = false;
        exec.shutdownNow();
        line.close();
    }
}
