package gui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import merger.Merger;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main extends Application {
    private TextArea usersTextArea;
    private TextArea ignoredUsersTextArea;
    private TextField sleepTimeTextField;
    private Label averageScoreLabel;
    private Label maxCountLabel;
    private ProgressBar progressBar;
    private Label progressLabel;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        //Scene and Stage Set-Up
        stage.setTitle("MyAnimeList User Lists Merger");
        final VBox root = new VBox();
        Scene scene = new Scene(root);

        //Creation of Scene Objects
        Label ignoredUserLabel = new Label("Ignored Users:");
        ignoredUsersTextArea = new TextArea();

        Label sleepTimeLabel = new Label("Sleep Time (ms):");
        sleepTimeTextField = new TextField("200");

        Button startButton = new Button("Start");
        startButton.setOnAction((ActionEvent e) -> startButtonClick());

        Label usersLabel = new Label("Users:");
        usersTextArea = new TextArea();

        averageScoreLabel = new Label("Average Score: ");

        maxCountLabel = new Label("Max Count: ");

        progressBar = new ProgressBar();
        progressBar.prefWidthProperty().bind(root.widthProperty());

        progressLabel = new Label("Press button to start the process!");

        //Placing Scene Objects in Scene
        root.getChildren().add(usersLabel);
        root.getChildren().add(usersTextArea);
        root.getChildren().add(ignoredUserLabel);
        root.getChildren().add(ignoredUsersTextArea);
        root.getChildren().add(sleepTimeLabel);
        root.getChildren().add(sleepTimeTextField);
        root.getChildren().add(averageScoreLabel);
        root.getChildren().add(maxCountLabel);
        root.getChildren().add(progressBar);
        root.getChildren().add(progressLabel);
        root.getChildren().add(startButton);


        //Starting the stage
        stage.setScene(scene);
        stage.show();
    }

    private void startButtonClick() {
        progressBar.setProgress(0.0);
        progressLabel.setText("Progress: 0%");
        new Thread(() -> {
            Merger pap = new Merger(Integer.parseInt(sleepTimeTextField.getText()), usersTextArea.getText(), ignoredUsersTextArea.getText());
            int userAmount = pap.getUsers().size();
            int counter = 1;

            //Create Map with anime of all the users
            for (String user : pap.getUsers()) {
                try {
                    pap.addMapToMergedAnimeListsMap(pap.createMapOfAnime(user));
                } catch (IOException | ParserConfigurationException | SAXException | InterruptedException e) {
                    e.printStackTrace();
                }
                //setProgressBar((double) counter++ / userAmount);
                double progress = (double) counter++ / userAmount;
                Platform.runLater(() -> progressBar.setProgress(progress));
                Platform.runLater(() -> progressLabel.setText("Progress: " + progress * 100 + "%"));
            }

            //Create a set of anime that will be removed
            for (String user : pap.getIgnoredUsers()) {
                try {
                    pap.fillSetOfIgnoredAnime(user);
                } catch (InterruptedException | IOException | SAXException | ParserConfigurationException e) {
                    e.printStackTrace();
                }
            }

            //Remove anime from map
            for (Integer animeID : pap.getIgnoredAnimeSet()) {
                pap.removeAnimeFromMergedAnimeListsMap(animeID);
            }
            pap.writeCSV();

            Platform.runLater(() -> maxCountLabel.setText("Max Count: " + pap.getMaxCount()));
            Platform.runLater(() -> averageScoreLabel.setText("Average Score: " + pap.getAverageScore()));
            Platform.runLater(() -> progressBar.setProgress(1.0));
            Platform.runLater(() -> progressLabel.setText("Done!"));
        }).start();
    }
}
