package blue.lhf.virekuvain.model;

import javax.sound.sampled.*;
import javax.sound.sampled.AudioFormat.*;

import java.io.*;
import java.nio.*;

import static java.lang.Math.*;
import static javax.sound.sampled.AudioFormat.Encoding.*;
import static javax.sound.sampled.AudioSystem.NOT_SPECIFIED;

public abstract class AudioInterpreter extends Thread implements Closeable {
    public static final int BUFFER_SIZE = 32;
    private final TargetDataLine source;
    private boolean running = true;

    protected AudioInterpreter(TargetDataLine line) throws LineUnavailableException {
        this.source = line;
        this.source.open(this.source.getFormat());
        this.source.start();

        this.setDaemon(true);
    }

    @Override
    public void run() {
        final AudioFormat format = this.source.getFormat();
        final int channels = format.getChannels();
        final int frameSizeBytes = format.getFrameSize();
        final Encoding encoding = format.getEncoding();

        if (frameSizeBytes == NOT_SPECIFIED) {
            throw new IllegalStateException("Frame size MUST be specified.");
        }

        final byte[] bytes = new byte[frameSizeBytes * BUFFER_SIZE];
        final ByteBuffer buffer = ByteBuffer.wrap(bytes);

        int read;
        while (running && (read = this.source.read(bytes, 0, bytes.length)) >= 0) {
            for (int frame = 0; frame < read / frameSizeBytes; ++frame) {
                final double[] values = new double[channels];
                for (int channel = 0; channel < channels; ++channel) {
                    double value;
                    if (encoding == PCM_SIGNED) {
                        value = buffer.get() / 128D;
                    } else if (encoding == PCM_UNSIGNED) {
                        value = decodeUnsigned(buffer);
                    } else if (encoding == ALAW) {
                        value = expandAlaw(decodeUnsigned(buffer));
                    } else if (encoding == ULAW) {
                        value = expandUlaw(decodeUnsigned(buffer));
                    } else if (encoding == PCM_FLOAT) {
                        value = buffer.getFloat();
                    } else {
                        throw new IllegalStateException("Unrecognised encoding " + encoding);
                    }

                    values[channel] = value;
                }
                accept(values);
            }
            buffer.flip();
        }
    }

    protected static final double A = 87.6;
    protected double expandAlaw(final double y) {
        return signum(y) * abs(y) < 1 / (1 + log(A))
            ? abs(y) * (1 + log(A)) / A
            : pow(E, -1 + abs(y) * (1 + log(A))) / A;
    }

    protected static final double U = 255;
    protected double expandUlaw(final double y) {
        return signum(y) * (pow(1 + U, abs(y)) - 1) / U;
    }

    protected double decodeUnsigned(final ByteBuffer buffer) {
        return (buffer.get() & 0xFF) / 128.D - 1;
    }

    protected abstract void accept(final double[] value);

    @Override
    public void close() {
        running = false;
    }
}
