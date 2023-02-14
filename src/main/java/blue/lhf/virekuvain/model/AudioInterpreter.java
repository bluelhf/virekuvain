package blue.lhf.virekuvain.model;

import javax.sound.sampled.*;
import javax.sound.sampled.AudioFormat.*;

import java.io.*;
import java.nio.*;
import java.time.*;

import static java.lang.Math.*;
import static java.nio.ByteOrder.*;
import static javax.sound.sampled.AudioFormat.Encoding.*;
import static javax.sound.sampled.AudioSystem.NOT_SPECIFIED;

public abstract class AudioInterpreter extends Thread implements Closeable {
    private final TargetDataLine source;
    private boolean running = true;

    private ByteBuffer buffer;
    private final int frameSizeBytes;
    private final AudioFormat format;

    private Instant lastUpdate = Instant.now();
    private Duration maxUpdateTime = Duration.ofMillis(0);

    protected AudioInterpreter(TargetDataLine line) throws LineUnavailableException {
        this.source = line;
        final AudioFormat format = this.source.getFormat();
        this.source.open(format, 1);

        this.frameSizeBytes = format.getFrameSize();
        if (frameSizeBytes == NOT_SPECIFIED) {
            throw new IllegalArgumentException("Frame size MUST be specified.");
        }

        this.format = format;

        this.buffer = ByteBuffer.allocateDirect(frameSizeBytes);
        if (!this.buffer.hasArray()) this.buffer = ByteBuffer.allocate(frameSizeBytes);

        this.buffer.order(format.isBigEndian() ? BIG_ENDIAN : LITTLE_ENDIAN);
        this.source.start();
        this.setDaemon(true);
    }

    @Override
    public void run() {
        final AudioFormat format = this.source.getFormat();
        final int channels = format.getChannels();
        final int sampleBits = format.getSampleSizeInBits();
        final int sampleBytes = sampleBits / Byte.SIZE;

        if (sampleBytes < 1 || sampleBytes > 4)
            throw new IllegalArgumentException("Sample size MUST be between 8 and 32 bits.");

        final Encoding encoding = format.getEncoding();

        while (running) {
            final Duration updateDuration = Duration.between(lastUpdate, lastUpdate = Instant.now());
            if (updateDuration.compareTo(maxUpdateTime) > 0) maxUpdateTime = updateDuration;
            final int read = this.source.read(buffer.array(), 0, buffer.capacity());
            for (int frame = 0; frame < read / frameSizeBytes; ++frame) {
                final double[] values = new double[channels];
                for (int channel = 0; channel < channels; ++channel) {
                    double value;
                    if (encoding == PCM_SIGNED) {
                        value = decodeSigned(buffer, sampleBytes);
                    } else if (encoding == PCM_UNSIGNED) {
                        value = decodeUnsigned(buffer, sampleBytes);
                    } else if (encoding == ALAW) {
                        value = expandAlaw(decodeSigned(buffer, sampleBytes));
                    } else if (encoding == ULAW) {
                        value = expandUlaw(decodeSigned(buffer, sampleBytes));
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

    private double decodeSigned(final ByteBuffer buffer, int bytes) {
        return (switch (bytes) {
            case 1 -> buffer.get();
            case 2 -> buffer.getShort();
            case 3 -> buffer.getInt() >> 8;
            case 4 -> buffer.getInt();
            default -> throw new IllegalArgumentException("Can only understand up to 4-byte values, but got " + bytes + " bytes");
        }) / (double) (1 << (bytes * Byte.SIZE - 1));
    }

    private double decodeUnsigned(final ByteBuffer buffer, int bytes) {
        return (switch (bytes) {
            case 1 -> buffer.get() & 0xFF;
            case 2 -> buffer.getShort() & 0xFFFF;
            case 3 -> buffer.getInt() & 0xFFFFFF;
            case 4 -> buffer.getInt() & 0xFFFFFFFFL;
            default -> throw new IllegalArgumentException("Can only understand up to 4-byte values, but got " + bytes + " bytes");
        }) / (double) (1 << (bytes * Byte.SIZE));
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

    protected abstract void accept(final double[] value);

    @Override
    public void close() {
        running = false;
    }

    public int getChannels() {
        return format.getChannels();
    }

    public float getFrameRate() {
        return format.getFrameRate();
    }
}
