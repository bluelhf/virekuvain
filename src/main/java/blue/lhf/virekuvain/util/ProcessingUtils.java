package blue.lhf.virekuvain.util;

import edu.emory.mathcs.jtransforms.fft.*;

import java.util.*;
import java.util.function.*;

import static java.lang.Math.log10;

public class ProcessingUtils {
    private ProcessingUtils() {
    }

    public static double[][] transpose(double[][] data, int rows) {
        final double[][] channels = new double[rows][data.length];
        for (int i = 0; i < data.length; ++i) {
            final double[] sample = data[i];
            for (int j = 0; j < sample.length; ++j) {
                channels[j][i] = sample[j];
            }
        }

        return channels;
    }

    public static Function<Float, Function<double[], double[]>> binsInRange(final int minimumHertz, final int maximumHertz) {
        return (frameRate) -> binsInRange(minimumHertz, maximumHertz, frameRate);
    }

    public static Function<double[], double[]> binsInRange(final int minimumHertz, final int maximumHertz, final double frameRate) {
        return (data) -> binsInRange(minimumHertz, maximumHertz, data, frameRate);
    }

    public static double[] binsInRange(final int minimumHertz, final int maximumHertz, final double[] data, final double frameRate) {
        final int minBin = clamp(binByHertz(minimumHertz, frameRate, data.length), 0, data.length - 1);
        final int maxBin = clamp(binByHertz(maximumHertz, frameRate, data.length), 0, data.length - 1);
        return Arrays.copyOfRange(data, minBin, maxBin);
    }

    public static double[] toMagnitudes(final double[] fft) {
        final double[] magnitudes = new double[fft.length / 2];
        for (int i = 0; i < magnitudes.length; i++) {
            magnitudes[i] = Math.sqrt(fft[i * 2] * fft[i * 2] + fft[i * 2 + 1] * fft[i * 2 + 1]);
        }

        return magnitudes;
    }

    public static double[] fourierTransform(final double[] data) {
        if (data.length == 0) return new double[0];
        final DoubleFFT_1D fft = new DoubleFFT_1D(data.length);
        final double[] copy = new double[data.length];
        System.arraycopy(data, 0, copy, 0, copy.length);
        fft.realForward(copy);
        return copy;
    }

    public static int binByHertz(final double freq, final double samples, final int bins) {
        return (int) (freq / (samples / bins / 2.0));
    }

    public static int clamp(final int value, final int min, final int max) {
        return Math.max(min, Math.min(max, value));
    }

    public static double clamp(final double value, final double min, final double max) {
        return Math.max(min, Math.min(max, value));
    }

    public static double indexByDouble(final double[] data, double index) {
        index = clamp(index, 0, data.length - 1);
        if ((int) index == index) return data[(int) index];
        final int lower = (int) Math.floor(index);
        final int upper = (int) Math.ceil(index);
        return data[lower] * (upper - index) + data[upper] * (index - lower);
    }

    public static double[] smooth(final double[] data, final int factor) {
        double average = 0;
        final double[] smoothed = new double[data.length];
        for (int i = 0; i < data.length; ++i) {
            final double toRemove = i >= factor ? data[i - factor] : 0;
            average += (data[i] - toRemove) / factor;
            smoothed[i] = average;
        }

        return smoothed;
    }

    public static double[] averages(final double[][] data) {
        final int len = data.length;
        final double[] result = new double[len];
        for (int i = 0; i < len; i++) {
            final double[] subarray = data[i];
            for (int j = 0; j < subarray.length; j++) {
                result[i] += subarray[j];
            }

            result[i] /= subarray.length;
        }

        return result;
    }

    public static double map(final double val, final double min1, final double max1, final double min2, final double max2) {
        // f(x) = (x - input_start) / (input_end - input_start) * (output_end - output_start) + output_start
        return (val - min1) / (max1 - min1) * (max2 - min2) + min2;
    }

    public static double toDBFS(final double value) {
        return 20 * log10(value);
    }
}
