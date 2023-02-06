package blue.lhf.virekuvain.view;

import blue.lhf.virekuvain.model.*;

import java.awt.*;

public class WaveformVisualiser extends Visualiser {
    @Override
    protected void onUpdate(Graphics g, int width, int height, AudioSource source) {
        final double[][] samples = source.getBuf();
        final double[][] channels = new double[2][samples.length];
        for (int i = 0; i < samples.length; ++i) {
            final double[] sample = samples[i];
            for (int j = 0; j < sample.length; ++j) {
                channels[j][i] = sample[j];
            }
        }

        final double[] max = new double[channels.length];
        for (int i = 0, len = channels.length; i < len; i++) {
            final double[] channel = channels[i];
            g.setColor(i % 2 == 0 ? Color.RED : Color.BLUE);
            int px = 0;
            int py = (int) (getHeight() / 2);
            for (double j = 0, cLen = channel.length; j < cLen; j += channel.length / 256.0) {
                final double value = channel[(int) j];
                if (value > max[i]) max[i] = value;

                final double progress = j / cLen;
                final int x = (int) (progress * getWidth());
                final int y = (int) (getHeight() / 2.0 - value * getHeight() / 4.0);
                if (px == x && py == y) continue;
                g.drawLine(px, py, x, y);
                px = x;
                py = y;
            }

            g.drawLine(px, py, (int) getWidth(), (int) (getHeight() / 2.0));
        }
    }
}
