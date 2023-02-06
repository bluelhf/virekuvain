package blue.lhf.virekuvain.controller;

import blue.lhf.virekuvain.model.*;
import blue.lhf.virekuvain.view.*;
import javafx.application.*;
import javafx.fxml.*;
import javafx.scene.control.*;
import javafx.stage.*;

import javax.sound.sampled.*;

import java.util.concurrent.*;

import static blue.lhf.virekuvain.util.Functional.notNull;

public class ProgramController {
    @FXML
    private Visualiser visualiser;

    @FXML
    private ChoiceBox<Mixer.Info> audioSourceBox;

    private Stage stage;

    private AudioSource source;

    public void initialise(final Stage stage) {
        this.stage = stage;
        populateAudioSources();
    }

    private void populateAudioSources() {
        audioSourceBox.setItems(new MixerList().filtered(notNull(AudioSource::getFormat)));
        audioSourceBox.setConverter(new MixerConverter());
        audioSourceBox.setOnAction(actionEvent -> {
            final Mixer.Info info = audioSourceBox.getValue();
            audioSourceBox.setDisable(true);
            CompletableFuture.runAsync(() -> {
                final Mixer mixer = AudioSystem.getMixer(info);
                try {
                    this.source = new AudioSource(44100, mixer);
                    visualiser.setSource(source);
                } catch (LineUnavailableException ex) {
                    ex.printStackTrace();
                    actionEvent.consume();
                }

                Platform.runLater(() -> audioSourceBox.setDisable(false));
            });
        });
    }

    public void stop() {
        if (this.source != null) source.close();
        Platform.runLater(() -> {
            visualiser.close();
            stage.close();
        });
    }
}
