package blue.lhf.virekuvain.view;

import java.time.*;

public abstract class TimeInterpolation {
    private Instant lastUpdate = Instant.now();
    private double[] values;

    protected TimeInterpolation(final int length) {
        this.values = new double[length];
    }

    public double[] update(final double[] targets) {
        final Duration time = Duration.between(lastUpdate, Instant.now());
        final double millis = time.toNanos() / 1E6;
        for (int i = 0, len = values.length; i < len; ++i) {
            if (targets.length <= i) break;
            values[i] = interpolate(values[i], targets[i], millis);
        }

        lastUpdate = Instant.now();
        return values;
    }

    protected abstract double interpolate(final double current, final double target, final double dt);

    public int size() {
        return this.values.length;
    }

    public void resize(final int length) {
        values = new double[length];
        lastUpdate = Instant.now();
    }
}
