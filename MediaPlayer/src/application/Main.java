package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

	public static void main(String[] args) {
		launch(args);
	}

////	If we want exclusive look of the application...
//	private double xOffset = 0; 
//	private double yOffset = 0;

	@Override
	public void start(Stage primaryStage) {
		try {
			primaryStage.setTitle(MediaPlayerController.APP_TITLE);

			Parent root = FXMLLoader.load(getClass().getResource("MediaPlayer2.fxml"));

			Scene scene = new Scene(root);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());

			primaryStage.setScene(scene);

////			If we want exclusive look of the application...
//			primaryStage.initStyle(StageStyle.UNDECORATED);
//			root.setOnMousePressed(new EventHandler<MouseEvent>() {
//	            @Override
//	            public void handle(MouseEvent event) {
//	                xOffset = event.getSceneX();
//	                yOffset = event.getSceneY();
//	            }
//	        });
//	        root.setOnMouseDragged(new EventHandler<MouseEvent>() {
//	            @Override
//	            public void handle(MouseEvent event) {
//	                primaryStage.setX(event.getScreenX() - xOffset);
//	                primaryStage.setY(event.getScreenY() - yOffset);
//	            }
//	        });

			primaryStage.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
