package blue.lhf.virekuvain.view;

import blue.lhf.virekuvain.model.*;

import java.awt.*;

public class WaveformVisualiser extends Visualiser {

    @Override
    protected void onUpdate(Graphics g, int width, int height, AudioSource source) {
        final double[][] channels = transpose(source.getBuffer(), source.getChannels());

        final double[] max = new double[channels.length];
        for (int i = 0, len = channels.length; i < len; i++) {
            final double[] raw = channels[i];
            final double[] channel = new double[source.getBufferSize()];
            System.arraycopy(raw, 0, channel, channel.length - raw.length, raw.length);

            g.setColor(i % 2 == 0 ? getPalette().primary() : getPalette().secondary());
            int px = 0;
            int py = (int) (getHeight() / 2);
            for (int j = 0, cLen = channel.length; j < cLen; ++j) {
                final double value = channel[j];
                if (value > max[i]) max[i] = value;

                final double progress = j / (double) cLen;
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
