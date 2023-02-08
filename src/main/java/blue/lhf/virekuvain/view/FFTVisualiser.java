package blue.lhf.virekuvain.view;

import blue.lhf.virekuvain.model.AudioSource;

import java.awt.*;
import java.util.*;

import edu.emory.mathcs.jtransforms.fft.*;

public class FFTVisualiser extends Visualiser {
    private final Map<Integer, LinearTimeInterpolation> interpolationMap = new HashMap<>();

    @Override
    protected void onUpdate(Graphics g, int width, int height, AudioSource source) {
        final double[][] channels = transpose(source.getBuffer(), source.getChannels());

        final double[][] lerpedValues = new double[channels.length][];

        for (int channel = 0; channel < channels.length; ++channel) {
            final double[] bins;
            {
                final double[] mono = channels[channel];
                final DoubleFFT_1D fft = new DoubleFFT_1D(mono.length);
                fft.realForward(mono);

                final int minBin = frequencyToIndex(100, source.getFrameRate(), mono.length);
                final int maxBin = frequencyToIndex(15000, source.getFrameRate(), mono.length);
                bins = Arrays.copyOfRange(mono, minBin, maxBin);
            }

            final double[] magnitudes = new double[bins.length / 2];
            for (int i = 0; i < magnitudes.length; i++) {
                magnitudes[i] = Math.sqrt(bins[i * 2] * bins[i * 2] + bins[i * 2 + 1] * bins[i * 2 + 1]);
            }


            final LinearTimeInterpolation interpolation = interpolationMap.computeIfAbsent(channel,
                k -> new LinearTimeInterpolation(magnitudes.length, 500));

            if (interpolation.size() != magnitudes.length) {
                interpolation.resize(magnitudes.length);
            }


            lerpedValues[channel] = Arrays.stream(interpolation.update(magnitudes)).map(Math::cbrt).toArray();
        }

        final double max = Arrays.stream(lerpedValues).flatMapToDouble(Arrays::stream).max().orElse(1.0);

        for (int channel = 0, len = lerpedValues.length; channel < len; ++channel) {
            double[] lerped = lerpedValues[channel];

            g.setColor(channel % 2 == 0 ? getPalette().primary() : getPalette().secondary());

            int px = 0;
            int py = (int) getHeight();
            for (int i = 0; i < lerped.length; i++) {
                final double progress = i / (double) lerped.length;
                final int index = (int) (lerped.length * Math.pow(progress, 2));
                final int x = (int) (progress * getWidth());
                final int y = (int) (getHeight() - lerped[index] / max * getHeight());
                if (px == x && py == y) continue;
                g.drawLine(px, py, x, y);
                px = x;
                py = y;
            }
        }



    }

    public static int frequencyToIndex(double freq, double samples, int nFFT) {
        return (int) (freq / (samples / nFFT / 2.0));
    }
}
