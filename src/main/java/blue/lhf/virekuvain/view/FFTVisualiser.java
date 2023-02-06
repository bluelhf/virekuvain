package blue.lhf.virekuvain.view;

import blue.lhf.virekuvain.model.AudioSource;

import java.awt.*;
import java.util.Arrays;

import edu.emory.mathcs.jtransforms.fft.*;

public class FFTVisualiser extends Visualiser {
    LinearTimeInterpolation interpolation;

    @Override
    protected void onUpdate(Graphics g, int width, int height, AudioSource source) {
        final double[][] channels = transpose(source.getBuffer(), source.getChannels());

        final double[] bins;
        {
            final double[] mono = new double[source.getBufferSize()];
            for (final double[] channel : channels) {
                for (int j = 0; j < channel.length; ++j) {
                    mono[j] += channel[j] / channels.length;
                }
            }

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


        if (interpolation == null || interpolation.size() != magnitudes.length) {
            interpolation = new LinearTimeInterpolation(magnitudes.length, 500);
        }

        final double[] lerped = Arrays.stream(interpolation.update(magnitudes)).map(Math::cbrt).toArray();
        final double max = Arrays.stream(lerped).max().orElse(1.0);

        g.setColor(getPalette().primary());

        int px = 0;
        int py = (int) getHeight();
        for (int i = 0; i < lerped.length; i++) {
            // Index that condenses high indices into a smaller area
            final int index = (int) (lerped.length * Math.pow(i / (double) lerped.length, 2));
            final double progress = i / (double) lerped.length;
            final int x = (int) (progress * getWidth());
            final int y = (int) (getHeight() - lerped[index] / max * getHeight());
            if (px == x && py == y) continue;
            g.drawLine(px, py, x, y);
            px = x;
            py = y;
        }
    }

    public static int frequencyToIndex(double freq, double samples, int nFFT) {
        return (int) (freq / (samples / nFFT / 2.0));
    }

    public static double indexToFrequency(int i, double samples, int nFFT) {
        return (double) i * (samples / nFFT / 2.);
    }
}
