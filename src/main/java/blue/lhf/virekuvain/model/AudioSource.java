package blue.lhf.virekuvain.model;

import blue.lhf.virekuvain.util.*;

import javax.sound.sampled.*;
import java.io.*;

public class AudioSource implements Closeable {
    private Mixer mixer;
    private TargetDataLine line;
    private CircularAudioInterpreter interpreter;
    private int bufferSize;

    public AudioSource(final int bufferSize, final Mixer mixer) throws LineUnavailableException {
        this.bufferSize = bufferSize;
        start(mixer);
    }

    public static AudioFormat getFormat(final Mixer.Info info) {
        return getFormat(AudioSystem.getMixer(info));
    }

    public static AudioFormat getFormat(final Mixer mixer) {
        if (mixer == null) return null;

        for (final Line.Info rawInfo : mixer.getTargetLineInfo()) {
            if (!(rawInfo instanceof DataLine.Info info)) continue;
            if (info.getFormats().length == 0) continue;
            return info.getFormats()[0];
        }

        return null;
    }

    public double[][] getBuffer() {
        return this.interpreter.getBuffer();
    }

    public void start(final Mixer mixer) throws LineUnavailableException {
        if (this.interpreter != null) {
            this.interpreter.close();
        }

        if (this.line != null) {
            this.line.close();
        }

        if (this.mixer != null) {
            this.mixer.close();
        }

        this.mixer = mixer;
        mixer.open();

        final AudioFormat sourceFormat = getFormat(mixer);
        if (sourceFormat == null) {
            throw new LineUnavailableException("Could not find valid format");
        }

        this.line = AudioSystem.getTargetDataLine(sourceFormat, mixer.getMixerInfo());
        this.interpreter = new CircularAudioInterpreter(this.line, bufferSize);
        this.interpreter.start();
    }

    public void resize(final int newBufferSize) {
        this.interpreter.resize(newBufferSize);
        this.bufferSize = newBufferSize;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    @Override
    public void close() {
        this.interpreter.close();
    }

    public int getChannelCount() {
        return this.interpreter.getChannels();
    }

    public double[][] getChannels() {
        return ProcessingUtils.transpose(getBuffer(), getChannelCount());
    }

    public float getFrameRate() {
        return this.interpreter.getFrameRate();
    }
}
