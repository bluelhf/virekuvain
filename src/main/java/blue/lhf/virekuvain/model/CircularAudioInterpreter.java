package blue.lhf.virekuvain.model;

import javax.sound.sampled.*;
import java.util.concurrent.*;

public class CircularAudioInterpreter extends AudioInterpreter {
    private final LinkedBlockingDeque<double[]> buffer;

    public CircularAudioInterpreter(TargetDataLine line, final int bufferSize) throws LineUnavailableException {
        super(line);
        this.buffer = new LinkedBlockingDeque<>(bufferSize);
    }

    public double[][] getBuffer() {
        return buffer.toArray(new double[0][0]);
    }

    @Override
    protected void accept(final double[] value) {
        if (buffer.remainingCapacity() <= 0) {
            buffer.removeFirst();
        }

        buffer.addLast(value);
    }
}
