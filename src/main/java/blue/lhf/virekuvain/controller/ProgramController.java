package blue.lhf.virekuvain.controller;

import blue.lhf.virekuvain.model.*;
import blue.lhf.virekuvain.view.*;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.converter.*;
import org.jetbrains.annotations.Nullable;

import javax.sound.sampled.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.*;

import static blue.lhf.virekuvain.util.Functional.notNull;

public class ProgramController {
    private static final int DEFAULT_BUFFER_SIZE = 4096;

    @FXML
    private VisualiserMultiplexer multiplexer;

    @FXML
    public ColorPicker primaryColour;

    @FXML
    public ColorPicker secondaryColour;

    @FXML
    public ColorPicker backgroundColour;

    @FXML
    private ChoiceBox<Mixer.Info> audioSourceBox;

    @FXML
    private ChoiceBox<Class<Visualiser>> visualiserBox;

    @FXML
    private Label bufferSizeLabel;

    @FXML
    private TextField bufferSizeText;

    private final IntegerProperty bufferSize = new SimpleIntegerProperty(DEFAULT_BUFFER_SIZE);

    private Stage stage;

    private AudioSource source;

    public void initialise(final Stage stage) {
        this.stage = stage;
        onColourUpdated();
        populateAudioSources();
        populateVisualisers();
        populateBufferSize();
    }

    private void populateBufferSize() {
        if (System.getProperty("virekuvain.showBufferSize") == null) {
            bufferSizeText.setVisible(false);
            bufferSizeLabel.setVisible(false);
        }
        bufferSize.bind(bufferSizeText.textProperty().map(Integer::parseInt));
        bufferSizeText.setTextFormatter(new TextFormatter<>(new IntegerStringConverter()));
        bufferSizeText.setText(String.valueOf(DEFAULT_BUFFER_SIZE));
        bufferSizeText.setOnAction(this::onBufferSizeChanged);
    }

    private void onBufferSizeChanged(@Nullable ActionEvent event) {
        if (source != null) {
            source.resize(bufferSize.intValue());
        }
    }

    private void populateAudioSources() {
        audioSourceBox.setItems(new MixerList().filtered(notNull(AudioSource::getFormat)));
        audioSourceBox.setConverter(new MixerConverter());
        audioSourceBox.getSelectionModel().selectFirst();
        audioSourceBox.setOnAction(this::onSourceChanged);
        this.onSourceChanged(null);
    }

    private void populateVisualisers() {
        visualiserBox.setItems(new VisualiserList());
        visualiserBox.setConverter(new VisualiserConverter());
        visualiserBox.getSelectionModel().selectFirst();
        visualiserBox.setOnAction(this::onVisualiserChanged);
        this.onVisualiserChanged(null);
    }

    private void onVisualiserChanged(@Nullable ActionEvent event) {
        final Class<Visualiser> clazz = visualiserBox.getValue();
        try {
            this.multiplexer.setVisualiser(clazz.getConstructor().newInstance());
        } catch (Exception exc) {
            visualiserBox.setItems(visualiserBox.getItems().filtered(c -> c != clazz));
            visualiserBox.getSelectionModel().selectFirst();
            flashRed(visualiserBox);

            System.err.println("Exception when creating visualiser: " + clazz.getName());
            exc.printStackTrace();
        }
    }

    private void onSourceChanged(@Nullable ActionEvent actionEvent) {
        final Mixer.Info info = audioSourceBox.getValue();
        audioSourceBox.setDisable(true);
        CompletableFuture.runAsync(() -> {
            final Mixer mixer = AudioSystem.getMixer(info);
            try {
                this.source = new AudioSource(bufferSize.intValue(), mixer);
                this.multiplexer.setSource(source);
            } catch (LineUnavailableException ex) {
                ex.printStackTrace();
                if (actionEvent != null) actionEvent.consume();
            }

            Platform.runLater(() -> audioSourceBox.setDisable(false));
        });
    }


    @FXML
    private void onColourUpdated() {
        multiplexer.setColourPalette(new ColourPalette(
            ColourPalette.fromFX(primaryColour.getValue()),
            ColourPalette.fromFX(secondaryColour.getValue()),
            ColourPalette.fromFX(backgroundColour.getValue())
        ));
    }

    public void stop() {
        if (this.source != null) source.close();
        Platform.runLater(() -> {
            multiplexer.close();
            stage.close();
        });
    }

    private void flashRed(final Node node) {
        CompletableFuture.runAsync(() -> {
            Platform.runLater(() -> node.setStyle(node.getStyle() + "-fx-border-color: red;"));
            LockSupport.parkNanos((long) 1E9);
            Platform.runLater(() -> node.setStyle(node.getStyle().replaceFirst("-fx-border-color: red;", "")));
        });
    }
}
