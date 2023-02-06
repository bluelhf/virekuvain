package blue.lhf.virekuvain.controller;

import blue.lhf.virekuvain.model.*;
import blue.lhf.virekuvain.view.Visualiser;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.jetbrains.annotations.Nullable;

import javax.sound.sampled.*;
import java.util.concurrent.CompletableFuture;

import static blue.lhf.virekuvain.util.Functional.notNull;

public class ProgramController {
    @FXML
    private Visualiser visualiser;

    @FXML
    public ColorPicker primaryColour;

    @FXML
    public ColorPicker secondaryColour;

    @FXML
    public ColorPicker backgroundColour;

    @FXML
    private ChoiceBox<Mixer.Info> audioSourceBox;

    private Stage stage;

    private AudioSource source;

    public void initialise(final Stage stage) {
        this.stage = stage;
        colourUpdated();
        populateAudioSources();
    }

    private void populateAudioSources() {
        audioSourceBox.setItems(new MixerList().filtered(notNull(AudioSource::getFormat)));
        audioSourceBox.setConverter(new MixerConverter());
        audioSourceBox.getSelectionModel().selectFirst();
        audioSourceBox.setOnAction(this::onSourceChanged);
        this.onSourceChanged(null);
    }

    private void onSourceChanged(@Nullable ActionEvent actionEvent) {
        final Mixer.Info info = audioSourceBox.getValue();
        audioSourceBox.setDisable(true);
        CompletableFuture.runAsync(() -> {
            final Mixer mixer = AudioSystem.getMixer(info);
            try {
                this.source = new AudioSource(4096, mixer);
                visualiser.setSource(source);
            } catch (LineUnavailableException ex) {
                ex.printStackTrace();
                if (actionEvent != null) actionEvent.consume();
            }

            Platform.runLater(() -> audioSourceBox.setDisable(false));
        });
    }


    @FXML
    private void colourUpdated() {
        visualiser.setColourPalette(new ColourPalette(
            ColourPalette.fromFX(primaryColour.getValue()),
            ColourPalette.fromFX(secondaryColour.getValue()),
            ColourPalette.fromFX(backgroundColour.getValue())
        ));
    }

    public void stop() {
        if (this.source != null) source.close();
        Platform.runLater(() -> {
            visualiser.close();
            stage.close();
        });
    }
}
