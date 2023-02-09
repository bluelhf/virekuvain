package blue.lhf.virekuvain.view.interpolation;

import static java.lang.Math.max;

/**
 * A {@link TimeInterpolation} that decreases linearly, but increases immediately.
 * */
public class MaximisingLinearTimeInterpolation extends LinearTimeInterpolation {
    public MaximisingLinearTimeInterpolation(int length, double duration) {
        super(length, duration);
    }

    @Override
    protected double interpolate(double current, double target, double dt) {
        return max(target, super.interpolate(current, target, dt));
    }
}
