package blue.lhf.virekuvain.view.interpolation;

public class LinearTimeInterpolation extends TimeInterpolation {
    private final double duration;

    public LinearTimeInterpolation(int length, double duration) {
        super(length);
        this.duration = duration;
    }

    @Override
    protected double interpolate(double current, double target, double dt) {
        return current + (target - current) * (dt / duration);
    }
}
