package blue.lhf.virekuvain;

import blue.lhf.virekuvain.controller.*;
import javafx.application.*;
import javafx.fxml.*;
import javafx.scene.*;
import javafx.stage.*;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/program.fxml"));
        stage.setScene(new Scene(loader.load()));

        final ProgramController controller = loader.getController();
        Runtime.getRuntime().addShutdownHook(new Thread(controller::stop));
        controller.initialise(stage);
        stage.show();
    }
}
