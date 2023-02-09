package blue.lhf.virekuvain.view.visualisers;

import blue.lhf.virekuvain.model.*;
import blue.lhf.virekuvain.view.*;
import blue.lhf.virekuvain.view.interpolation.*;

import java.awt.*;
import java.util.*;

import static blue.lhf.virekuvain.util.ProcessingUtils.*;

@VisualiserMetadata(name = "Frequency Spectrum")
public class FFTVisualiser extends Visualiser {
    private final MaximisingLinearTimeInterpolation volumeLerp = new MaximisingLinearTimeInterpolation(1, 1000);
    private final Map<Integer, LinearTimeInterpolation> interpolationMap = new HashMap<>();

    protected double[] transformChannel(final double[] channel, final AudioSource source) {
        final double[] fft = fourierTransform(channel);
        final double[] bins = binsInRange(100, 15000).apply(source.getFrameRate()).apply(fft);
        final double[] magnitudes = toMagnitudes(bins);
        return Arrays.stream(magnitudes).map(Math::cbrt).toArray();
    }

    @Override
    protected void onUpdate(Graphics g, int width, int height, AudioSource source) {
        final double[][] channels = source.getChannels();

        final double[][] interpolated = new double[channels.length][];
        for (int channel = 0; channel < channels.length; ++channel) {
            final double[] magnitudes = transformChannel(channels[channel], source);

            final LinearTimeInterpolation interpolation = interpolationMap.computeIfAbsent(channel,
                k -> new LinearTimeInterpolation(magnitudes.length, 70));

            if (interpolation.size() != magnitudes.length) {
                interpolation.resize(magnitudes.length);
            }

            interpolated[channel] = Arrays.stream(interpolation.update(magnitudes)).toArray();
        }

        final double rawMax = Arrays.stream(interpolated).flatMapToDouble(Arrays::stream).max().orElse(1.0);
        final double max = volumeLerp.update(new double[]{rawMax})[0];


        for (int channel = 0, len = interpolated.length; channel < len; ++channel) {
            double[] lerped = interpolated[channel];

            g.setColor(channel % 2 == 0 ? getPalette().primary() : getPalette().secondary());

            int px = 0;
            int py = (int) getHeight();
            for (int i = 0; i < lerped.length; i++) {
                final double progress = i / (double) lerped.length;

                final double relativeIndex = Math.pow(progress, 2) * lerped.length;

                final int x = (int) (progress * getWidth());
                final int y = (int) (getHeight() - indexByDouble(lerped, relativeIndex) / max * getHeight());
                if (px == x && py == y) continue;
                g.drawLine(px, py, x, y);
                px = x;
                py = y;
            }
        }
    }
}
