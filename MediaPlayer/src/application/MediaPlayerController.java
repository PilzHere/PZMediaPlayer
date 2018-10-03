package application;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import helpers.FileExtensionReader;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuBar;
import javafx.scene.control.Slider;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

public class MediaPlayerController implements Initializable {
	@FXML
	private Label mediaTimePlayed, mediaTimeEnd, volumeLabel0, volumeLabel1, volumeLabel2, volumeLabel3;

	@FXML
	private StackPane mediaStackPane, mediaStackPaneFullscreen;

	@FXML
	private MediaView mediaView;

	@FXML
	private MenuBar menuBar;

	@FXML
	private ListView<String> musicList, videosList;

	@FXML
	private Button playMedia, pauseMedia, stopMedia, previousMedia, nextMedia, addMusic, removeMusic;

	@FXML
	private Slider sliderMedia, sliderVolume;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		sliderVolume.setValue(25);
		mediaStackPaneFullscreen.setDisable(true);

		clearButtonText(addMusic);
		clearButtonText(removeMusic);
		
		clearButtonText(playMedia);
		clearButtonText(pauseMedia);
		clearButtonText(stopMedia);
		clearButtonText(nextMedia);
		clearButtonText(previousMedia);

		clearLabelText(volumeLabel0);
		clearLabelText(volumeLabel1);
		clearLabelText(volumeLabel2);
		clearLabelText(volumeLabel3);
	}

	private void clearLabelText(Label label) {
		label.setText("");
	}

	private void clearButtonText(Button button) {
		button.setText("");
	}

//	Sound extension filters.
	private final FileChooser.ExtensionFilter extFilterAny = new FileChooser.ExtensionFilter("Any (*.*)", "*");
	private final FileChooser.ExtensionFilter extFilterAiff = new FileChooser.ExtensionFilter("AIFF (*.aiff)",
			"*.aiff");
	private final FileChooser.ExtensionFilter extFilterMp3 = new FileChooser.ExtensionFilter("MP3 (*.mp3)", "*.mp3");
	private final FileChooser.ExtensionFilter extFilterWav = new FileChooser.ExtensionFilter("WAV (*.wav)", "*.wav");

//	Video extension filters.
	private final FileChooser.ExtensionFilter extFilterFLV = new FileChooser.ExtensionFilter("FLV (*.flv)", "*.flv");
	private final FileChooser.ExtensionFilter extFilterMpeg4 = new FileChooser.ExtensionFilter("MPEG4 (*.mp4)",
			"*.mp4");

	private final String extMp4 = "mp4";
	private final String extWav = "wav";
	private final String extMp3 = "mp3";
	private final String extAiff = "aiff";
	private final String extFlv = "flv";

	private String filePath;
	private MediaPlayer mediaPlayer;

	private double oldVolume;
	private boolean volumeReset = false;

	private Stage stage;

	private boolean widthListenerSet = false;
	private boolean heightListenerSet = false;

	@FXML
	private void addMediaButtonAction(ActionEvent event) {
		FileChooser fileChooser = new FileChooser();

		fileChooser.getExtensionFilters().addAll(extFilterAny, extFilterAiff, extFilterMp3, extFilterWav, extFilterFLV,
				extFilterMpeg4);

//		Resetting filepath.
		filePath = null;

//		Opens the add-media window.
		File file = fileChooser.showOpenDialog(null);

//		System.out.println("" + getFileExtension(file));

//		If add-media window is cancelled...
		if (file != null) {
			filePath = file.toURI().toString();

			System.out.println();
		}

		if (filePath != null && !filePath.isEmpty()) {
			Media media = new Media(filePath);

			String fileExtension = FileExtensionReader.getExtension(filePath);
			if (fileExtension.equals(extMp4) || fileExtension.equals(extFlv)) {
				videosList.getItems().add(filePath);
			} else if (fileExtension.equals(extAiff) || fileExtension.equals(extMp3) || fileExtension.equals(extWav)) {
				musicList.getItems().add(filePath);
			} else {
				System.err.println("ERROR: File format not supported.");
				return;
			}

//			musicList.getItems().add(filePath);

			if (mediaPlayer != null) {
				oldVolume = mediaPlayer.getVolume();
				mediaPlayer.dispose();
			}

			mediaPlayer = new MediaPlayer(media);

			if (!volumeReset) {
				mediaPlayer.setVolume(0.25f);
				volumeReset = true;
			} else {
				mediaPlayer.setVolume(oldVolume);
			}

			mediaView.setMediaPlayer(mediaPlayer);
			mediaView.setPreserveRatio(true);

			if (stage == null) {
				stage = (Stage) mediaView.getScene().getWindow();
				
//				stage.setFullScreenExitHint("Doubleclick any mousebutton to exit fullscreen.");
				stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
			}
			
////			Test, if exlusive mode...
//			((Stage) stage.getScene().getWindow()).toBack();
//			((Stage) stage.getScene().getWindow()).toFront();

			mediaPlayer.seek(mediaPlayer.getStartTime());
			mediaPlayer.play();
			isPlaying = true;

			updatePlayButtonText();

			sliderVolume.setValue(mediaPlayer.getVolume() * 100);
			sliderVolume.valueProperty().addListener(new InvalidationListener() {
				@Override
				public void invalidated(Observable observable) {
					mediaPlayer.setVolume(sliderVolume.getValue() / 100);

					updateVolumeLabels(mediaPlayer.getVolume());
//					System.out.println("Volume: " + mediaPlayer.getVolume() + " | " + "Slider: " + sliderVolume.getValue());
				}
			});

			mediaPlayer.currentTimeProperty().addListener(new ChangeListener<Duration>() {
				@Override
				public void changed(ObservableValue<? extends Duration> observable, Duration oldValue,
						Duration newValue) {
//					if (!sliderMedia.isValueChanging()) {
					sliderMedia.maxProperty().set(mediaPlayer.getTotalDuration().toSeconds());
					sliderMedia.setValue(newValue.toSeconds());

					double toHoursPlayed = mediaPlayer.getCurrentTime().toHours();
					String inHoursPlayed = "" + toHoursPlayed;
					double toMinsPlayed = mediaPlayer.getCurrentTime().toMinutes();
					String inMinsPlayed = "" + toMinsPlayed;
					double toSecsPlayed = mediaPlayer.getCurrentTime().toSeconds();
					String inSecsPlayed = "" + toSecsPlayed;

					mediaTimePlayed.setText(inHoursPlayed.substring(0, 1) + ":" + inMinsPlayed.substring(0, 1) + ":"
							+ inSecsPlayed.substring(0, 2));

//						mediaTimePlayed.setText(mediaPlayer.getCurrentTime().toSeconds() / 60 / 60 + ":"
//						+ mediaPlayer.getCurrentTime().toSeconds() / 60 + ":"
//								+ mediaPlayer.getCurrentTime().toSeconds());

					double toHoursEnd = mediaPlayer.getStopTime().toHours();
					String inHoursEnd = "" + toHoursEnd;
					double toMinsEnd = mediaPlayer.getStopTime().toMinutes();
					String inMinsEnd = "" + toMinsEnd;
					double toSecsEnd = mediaPlayer.getStopTime().toSeconds();
					String inSecsEnd = "" + toSecsEnd;

					mediaTimeEnd.setText(inHoursEnd.substring(0, 1) + ":" + inMinsEnd.substring(0, 1) + ":"
							+ inSecsEnd.substring(0, 2));
//					}
				}
			});

			if (!widthListenerSet) {
				stage.widthProperty().addListener((obs, oldVal, newVal) -> {
					DoubleProperty width = mediaView.fitWidthProperty();
					width.bind(Bindings.selectDouble(mediaView.parentProperty(), "width"));
					mediaView.autosize();
				});
				widthListenerSet = true;
			}

			if (!heightListenerSet) {
				stage.heightProperty().addListener((obs, oldVal, newVal) -> {
					DoubleProperty height = mediaView.fitHeightProperty();
					height.bind(Bindings.selectDouble(mediaView.parentProperty(), "height"));
					mediaView.autosize();
				});
				heightListenerSet = true;
			}

			mediaPlayer.setOnEndOfMedia(new Runnable() {
				@Override
				public void run() {
					mediaPlayer.stop();

					isPlaying = false;

					updatePlayButtonText();

					mediaTimePlayed.setText("0:0:0");
				}
			});

			System.out.println("Media was added.");
		} else {
			System.err.println("ERROR: No media was added.");
		}
	}

	private void updateVolumeLabels(double volume) {
		if (volume == 0) {
			volumeLabel0.setVisible(true);
			volumeLabel1.setVisible(false);
			volumeLabel2.setVisible(false);
			volumeLabel3.setVisible(false);
		} else {
			if (volume >= 0.75f) {
				volumeLabel0.setVisible(false);
				volumeLabel1.setVisible(false);
				volumeLabel2.setVisible(false);
				volumeLabel3.setVisible(true);
			} else if (volume >= 0.5f) {
				volumeLabel0.setVisible(false);
				volumeLabel1.setVisible(false);
				volumeLabel2.setVisible(true);
				volumeLabel3.setVisible(false);
			} else {
				volumeLabel0.setVisible(false);
				volumeLabel1.setVisible(true);
				volumeLabel2.setVisible(false);
				volumeLabel3.setVisible(false);
			}
		}
	}

	@FXML
	private void musicListMouseClickedAction(MouseEvent event) {
//		System.out.println("clicked on " + musicList.getSelectionModel().getSelectedItem());
		if (event.getClickCount() == 2) {
			if (mediaPlayer != null) {
				oldVolume = mediaPlayer.getVolume();
				mediaPlayer.dispose();
			}

			if (musicList.getSelectionModel().getSelectedItem() != null) {
				String filePath = musicList.getSelectionModel().getSelectedItem().toString();
			} else {
				System.err.println("ERROR: Selected item is null.");
				return;
			}

			if (filePath != null && !filePath.isEmpty()) {
				Media media = new Media(filePath);

				mediaPlayer = new MediaPlayer(media);

				mediaPlayer.setVolume(oldVolume);

				mediaView.setMediaPlayer(mediaPlayer);
				mediaView.setPreserveRatio(true);

				mediaPlayer.seek(mediaPlayer.getStartTime());
				mediaPlayer.play();
				isPlaying = true;

				updatePlayButtonText();

//				sliderVolume.setValue(mediaPlayer.getVolume() * 100);
//				sliderVolume.valueProperty().addListener(new InvalidationListener() {
//					@Override
//					public void invalidated(Observable observable) {
//						mediaPlayer.setVolume(sliderVolume.getValue() / 100);
////						System.out.println("Volume: " + mediaPlayer.getVolume() + " | " + "Slider: " + sliderVolume.getValue());
//					}
//				});

				mediaPlayer.currentTimeProperty().addListener(new ChangeListener<Duration>() {
					@Override
					public void changed(ObservableValue<? extends Duration> observable, Duration oldValue,
							Duration newValue) {
//						if (!sliderMedia.isValueChanging()) {
						sliderMedia.maxProperty().set(mediaPlayer.getTotalDuration().toSeconds());
						sliderMedia.setValue(newValue.toSeconds());

						double toHoursPlayed = mediaPlayer.getCurrentTime().toHours();
						String inHoursPlayed = "" + toHoursPlayed;
						double toMinsPlayed = mediaPlayer.getCurrentTime().toMinutes();
						String inMinsPlayed = "" + toMinsPlayed;
						double toSecsPlayed = mediaPlayer.getCurrentTime().toSeconds();
						String inSecsPlayed = "" + toSecsPlayed;

						mediaTimePlayed.setText(inHoursPlayed.substring(0, 1) + ":" + inMinsPlayed.substring(0, 1) + ":"
								+ inSecsPlayed.substring(0, 2));

//							mediaTimePlayed.setText(mediaPlayer.getCurrentTime().toSeconds() / 60 / 60 + ":"
//							+ mediaPlayer.getCurrentTime().toSeconds() / 60 + ":"
//									+ mediaPlayer.getCurrentTime().toSeconds());

						double toHoursEnd = mediaPlayer.getStopTime().toHours();
						String inHoursEnd = "" + toHoursEnd;
						double toMinsEnd = mediaPlayer.getStopTime().toMinutes();
						String inMinsEnd = "" + toMinsEnd;
						double toSecsEnd = mediaPlayer.getStopTime().toSeconds();
						String inSecsEnd = "" + toSecsEnd;

						mediaTimeEnd.setText(inHoursEnd.substring(0, 1) + ":" + inMinsEnd.substring(0, 1) + ":"
								+ inSecsEnd.substring(0, 2));
//						}
					}
				});

				if (!widthListenerSet) {
					stage.widthProperty().addListener((obs, oldVal, newVal) -> {
						DoubleProperty width = mediaView.fitWidthProperty();
						width.bind(Bindings.selectDouble(mediaView.parentProperty(), "width"));
						mediaView.autosize();
					});
					widthListenerSet = true;
				}

				if (!heightListenerSet) {
					stage.heightProperty().addListener((obs, oldVal, newVal) -> {
						DoubleProperty height = mediaView.fitHeightProperty();
						height.bind(Bindings.selectDouble(mediaView.parentProperty(), "height"));
						mediaView.autosize();
					});
					heightListenerSet = true;
				}

				mediaPlayer.setOnEndOfMedia(new Runnable() {
					@Override
					public void run() {
						mediaPlayer.stop();

						isPlaying = false;

						updatePlayButtonText();

						mediaTimePlayed.setText("00:00:00");
					}
				});
			} else {
				System.err.println("ERROR: No media to play.");
			}
		}
	}

	@FXML
	private void mediaviewMouseClickAction(MouseEvent event) {
		if (mediaPlayer != null) {
			if (isPlaying) {
				mediaPlayer.pause();
				isPlaying = false;
			} else {
				mediaPlayer.play();
				isPlaying = true;
			}

			if (!stage.isFullScreen()) {
				if (event.getClickCount() == 2) {
					stage.setFullScreen(true);

					menuBar.setDisable(true);
					musicList.setDisable(true);
					videosList.setDisable(true);

					mediaStackPaneFullscreen.setDisable(false);
					mediaStackPaneFullscreen.setVisible(true);
					mediaStackPaneFullscreen.getChildren().add(mediaView);

					DoubleProperty width = mediaView.fitWidthProperty();
					width.bind(Bindings.selectDouble(mediaView.sceneProperty(), "width"));
					DoubleProperty height = mediaView.fitHeightProperty();
					height.bind(Bindings.selectDouble(mediaView.sceneProperty(), "height"));

					mediaView.autosize();

					stage.getScene().setCursor(Cursor.NONE);
				}
			} else {
				if (event.getClickCount() == 2) {
					stage.setFullScreen(false);

					mediaStackPane.getChildren().add(mediaView);

					mediaStackPaneFullscreen.setVisible(false);
					mediaStackPaneFullscreen.setDisable(true);

					mediaView.autosize();

					menuBar.setDisable(false);
					musicList.setDisable(false);
					videosList.setDisable(false);

					stage.getScene().setCursor(Cursor.DEFAULT);
				}
			}
			updatePlayButtonText();
		}
	}

	private boolean browsingMediaSeeker = false;

	@FXML
	private void sliderMediaMouseClickAction(MouseEvent event) {
//		System.out.println("4");
		if (mediaPlayer != null) {
			if (!browsingMediaSeeker) {
				mediaPlayer.pause();
				mediaPlayer.seek(Duration.seconds(sliderMedia.getValue()));
				if (isPlaying) {
					mediaPlayer.play();
				}
			} else {
				browsingMediaSeeker = false;
			}

			updatePlayButtonText();
		}
	}

	@FXML
	private void sliderMediaMousePressedAction(MouseEvent event) {
//		System.out.println("1");
		if (mediaPlayer != null) {
			mediaPlayer.pause();

			updatePlayButtonText();
		}
	}

	@FXML
	private void sliderMediaMouseDraggedAction(MouseEvent event) {
//		System.out.println("2");
		if (mediaPlayer != null) {
			mediaPlayer.seek(Duration.seconds(sliderMedia.getValue()));
		}
	}

	@FXML
	private void sliderMediaMouseReleasedAction(MouseEvent event) {
//		System.out.println("3");
		if (mediaPlayer != null) {
			if (isPlaying) {
				if (browsingMediaSeeker) {
					mediaPlayer.play();
				}
			}

			updatePlayButtonText();
		}
	}

	private void updatePlayButtonText() {
		if (isPlaying) {
			pauseMedia.setDisable(false);
			pauseMedia.setVisible(true);

			playMedia.setVisible(false);
			playMedia.setDisable(true);
		} else {
			pauseMedia.setDisable(true);
			pauseMedia.setVisible(false);

			playMedia.setVisible(true);
			playMedia.setDisable(false);
		}
	}

	private void playOrPauseMedia() {
		if (mediaPlayer != null) {
			if (!isPlaying) {
				mediaPlayer.play();
//				System.out.println("Media playing.");
			} else {
				mediaPlayer.pause();
//				System.out.println("Media paused.");
			}

			isPlaying = !isPlaying;

			updatePlayButtonText();
		}
	}

	private boolean isPlaying = false;

	@FXML
	private void playAndPauseMediaButttonAction(ActionEvent event) {
		playOrPauseMedia();
	}

	@FXML
	private void stopMediaButttonAction(ActionEvent event) {
		if (mediaPlayer != null) {
			mediaPlayer.seek(mediaPlayer.getTotalDuration());
			mediaPlayer.stop();

			isPlaying = false;

			updatePlayButtonText();

			mediaTimePlayed.setText("00:00:00");
//			System.out.println("Media stopped.");
		}
	}
}
