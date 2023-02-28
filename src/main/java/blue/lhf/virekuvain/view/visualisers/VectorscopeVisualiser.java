package blue.lhf.virekuvain.view.visualisers;

import blue.lhf.virekuvain.model.*;
import blue.lhf.virekuvain.view.Visualiser;

import java.awt.*;

@VisualiserMetadata(name = "Vectorscope")
public class VectorscopeVisualiser extends Visualiser {

    @Override
    protected void onUpdate(Graphics graphics, int width, int height, AudioSource source) {
        graphics.setColor(getPalette().primary());

        final int viewport = Math.min(width, height);
        final double[][] buffer = source.getBuffer();

        for (double[] frame : buffer) {
            /*
            * In practice, all audio will probably be either mono or stereo (single or dual channel).
            * That being said, we generalise the 2D vectorscope to work with any number of channels
            * by taking the average of all odd and even samples respectively
            * */

            double even = 0, odd = 0;
            for (int i = 0; i < frame.length; i++) {
                if (i % 2 == 0) {
                    even += frame[i];
                } else {
                    odd += frame[i];
                }
            }

            even /= frame.length / 2.0;
            odd  /= frame.length / 2.0;

            final double x =  width  / 2.0 + even * viewport / 2.0;
            final double y = -height / 2.0 + odd  * viewport / 2.0;
            graphics.drawLine((int) x, (int) y, (int) x, (int) y);
        }
    }
}

