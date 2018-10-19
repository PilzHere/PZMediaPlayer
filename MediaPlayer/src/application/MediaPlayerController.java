package application;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import helpers.FileExtensionReader;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.MapChangeListener;
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
import media.PZMedia;
import media.music.PZMusic;
import media.video.PZMovie;

public class MediaPlayerController implements Initializable {

	final static String APP_TITLE = "PZ Media Player";
	final static String APP_VERISON = "0.6.4";
	final static String GITHUB_LINK = "https://github.com/PilzHere/PZMediaPlayer";

	@FXML
	Label mediaTimePlayed, mediaTimeEnd, volumeLabel0, volumeLabel1, volumeLabel2, volumeLabel3,
			labelTitleArtistAlbumYear;

	@FXML
	StackPane mediaStackPane, mediaStackPaneFullscreen;

	@FXML
	MediaView mediaView;

	@FXML
	MenuBar menuBar;

	@FXML
	ListView<String> musicList, videosList;

	@FXML
	Button playMedia, pauseMedia, stopMedia, previousMedia, nextMedia, addMusic, removeMusic, addVideo, removeVideo;

	@FXML
	Slider sliderMedia, sliderVolume;

	ArrayList<PZMusic> musicClassList = new ArrayList<>();
	ArrayList<PZMovie> videosClassList = new ArrayList<>();

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		sliderVolume.setValue(25);
		mediaStackPaneFullscreen.setDisable(true);

		clearButtonText(addMusic);
		clearButtonText(removeMusic);
		clearButtonText(addVideo);
		clearButtonText(removeVideo);

		clearButtonText(playMedia);
		clearButtonText(pauseMedia);
		clearButtonText(stopMedia);
		clearButtonText(nextMedia);
		clearButtonText(previousMedia);

		clearLabelText(labelTitleArtistAlbumYear);

		clearLabelText(volumeLabel0);
		clearLabelText(volumeLabel1);
		clearLabelText(volumeLabel2);
		clearLabelText(volumeLabel3);
	}

	void clearLabelText(Label label) {
		label.setText("");
	}

	void clearButtonText(Button button) {
		button.setText("");
	}

//	Sound extension filters.
	final FileChooser.ExtensionFilter extFilterAny = new FileChooser.ExtensionFilter("Any (*.*)", "*");
	final FileChooser.ExtensionFilter extFilterAiff = new FileChooser.ExtensionFilter("AIFF (*.aiff)", "*.aiff");
	final FileChooser.ExtensionFilter extFilterMp3 = new FileChooser.ExtensionFilter("MP3 (*.mp3)", "*.mp3");
	final FileChooser.ExtensionFilter extFilterWav = new FileChooser.ExtensionFilter("WAV (*.wav)", "*.wav");

//	Video extension filters.
	final FileChooser.ExtensionFilter extFilterFLV = new FileChooser.ExtensionFilter("FLV (*.flv)", "*.flv");
	final FileChooser.ExtensionFilter extFilterMpeg4 = new FileChooser.ExtensionFilter("MPEG4 (*.mp4)", "*.mp4");

	final String extMp4 = "mp4";
	final String extWav = "wav";
	final String extMp3 = "mp3";
	final String extAiff = "aiff";
	final String extFlv = "flv";

	MediaPlayer mediaPlayer;

	double oldVolume;
	boolean volumeReset = false;

	Stage stage;

//	resizing window
	boolean widthListenerSet = false;
	boolean heightListenerSet = false;

//	time
	final int secondsInMinute = 60;
	final int secondsInHour = 3600;

//	current track time
	int currentTrackToHoursPlayed;
	int currentTrackToMinsPlayed;
	int currentTrackToSecsPlayed;

	int currentTrackToHoursEnd;
	int currentTrackToMinsEnd;
	int currentTrackToSecsEnd;

	String getFilepathForMediaFile(File file) {
//		If add-media window is cancelled...
		if (file != null) {
			String filePath = file.toURI().toString();
			return filePath;
		} else {
			System.err.println("ERROR: File is null. FileChooser was canceled.");
			return null;
		}
	}

	PZMedia decideTypeOfMedia(String fileExtension, PZMedia tempMedia, String filePath) {
		if (fileExtension.equals(extMp4) || fileExtension.equals(extFlv)) {
			return tempMedia = new PZMovie(filePath);
//			videosList.getItems().add(tempMedia);
		} else if (fileExtension.equals(extAiff) || fileExtension.equals(extMp3) || fileExtension.equals(extWav)) {
			return tempMedia = new PZMusic(filePath);
//			musicList.getItems().add(tempMedia);
		} else {
			tempMedia = null;
			return tempMedia;
		}
	}

	void resetVolume(boolean reset) {
		if (!reset) {
			mediaPlayer.setVolume(0.25f);
			volumeReset = true;
		} else {
			mediaPlayer.setVolume(oldVolume);
		}
	}

	void disposeMediaplayerIfNeeded(MediaPlayer mediaPlayer) {
		if (mediaPlayer != null) {
			oldVolume = mediaPlayer.getVolume();
			mediaPlayer.dispose();
		}
	}

	@FXML
	void addMediaButtonAction(ActionEvent event) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.getExtensionFilters().addAll(extFilterAny, extFilterAiff, extFilterMp3, extFilterWav, extFilterFLV,
				extFilterMpeg4);

//		Opens the "choose file" window.
		File file = fileChooser.showOpenDialog(null);
		String filePath = getFilepathForMediaFile(file);

		if (filePath != null && !filePath.isEmpty()) {
			PZMedia tempMedia = null;
			String fileExtension = FileExtensionReader.getExtension(filePath);
			tempMedia = decideTypeOfMedia(fileExtension, tempMedia, filePath);

			if (tempMedia != null) {
				Media media = new Media(tempMedia.getFilePath());
				tempMedia.setDisplayName(file.getName());

				clearCurrentMediaStrings();
				addMetadataListener(media);
				disposeMediaplayerIfNeeded(mediaPlayer);

				mediaPlayer = new MediaPlayer(media);
				addMediaToAppropriateList(tempMedia);

				resetVolume(volumeReset);
				mediaView.setMediaPlayer(mediaPlayer);
				mediaView.setPreserveRatio(true);

				if (stage == null) {
					stage = (Stage) mediaView.getScene().getWindow();
//					stage.setFullScreenExitHint("Doubleclick any mousebutton to exit fullscreen.");
					stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
				}

////				Test, if exclusive mode...
//				((Stage) stage.getScene().getWindow()).toBack();
//				((Stage) stage.getScene().getWindow()).toFront();

				mediaPlayer.seek(mediaPlayer.getStartTime());
				mediaPlayer.play();
				isPlaying = true;

				switchPlayOrPauseButtonActive();

				addVolumeListener();
				addTimeListener();
				setWidthAndHeightListener();

				setOnMediaFinished();
//				System.out.println("Media was added.");
			} else {
				System.err.println("ERROR: File format not supported.");
			}
		} else {
			System.err.println("ERROR: No media was added.");
		}
	}

	void setOnMediaFinished() {
		mediaPlayer.setOnEndOfMedia(new Runnable() {
			@Override
			public void run() {
				mediaPlayer.stop();
				isPlaying = false;
				switchPlayOrPauseButtonActive();
				clearPlayedTime();
			}
		});
	}

	void addMetadataListener(Media media) {
		media.getMetadata().addListener(new MapChangeListener<String, Object>() {
			@Override
			public void onChanged(Change<? extends String, ? extends Object> ch) {
				if (ch.wasAdded()) {
					handleMetadata(ch.getKey(), ch.getValueAdded());
					labelTitleArtistAlbumYear
							.setText(currentTitle + " - " + currentArtist + "\n" + currentAlbum + " - " + currentYear);
				}
			}
		});
	}

	void addVolumeListener() {
		sliderVolume.setValue(mediaPlayer.getVolume() * 100);
		sliderVolume.valueProperty().addListener(new InvalidationListener() {
			@Override
			public void invalidated(Observable observable) {
				mediaPlayer.setVolume(sliderVolume.getValue() / 100);
				updateVolumeLabels(mediaPlayer.getVolume());
//				System.out.println("Volume: " + mediaPlayer.getVolume() + " | " + "Slider: " + sliderVolume.getValue());
			}
		});
	}

	void addTimeListener() {
		mediaPlayer.currentTimeProperty().addListener(new ChangeListener<Duration>() {
			@Override
			public void changed(ObservableValue<? extends Duration> observable, Duration oldValue, Duration newValue) {
				if (!sliderMedia.isValueChanging()) {
					sliderMedia.maxProperty().set(mediaPlayer.getTotalDuration().toSeconds());
					sliderMedia.setValue(newValue.toSeconds());

					currentTrackToHoursPlayed = (int) mediaPlayer.getCurrentTime().toSeconds() / secondsInHour;
					currentTrackToMinsPlayed = (int) (mediaPlayer.getCurrentTime().toSeconds() % secondsInHour)
							/ secondsInMinute;
					currentTrackToSecsPlayed = (int) mediaPlayer.getCurrentTime().toSeconds() % secondsInMinute;

					mediaTimePlayed.setText(String.format("%02d:%02d:%02d", currentTrackToHoursPlayed,
							currentTrackToMinsPlayed, currentTrackToSecsPlayed));

					currentTrackToHoursEnd = (int) mediaPlayer.getStopTime().toSeconds() / secondsInHour;
					currentTrackToMinsEnd = (int) (mediaPlayer.getStopTime().toSeconds() % secondsInHour)
							/ secondsInMinute;
					currentTrackToSecsEnd = (int) mediaPlayer.getStopTime().toSeconds() % secondsInMinute;

					mediaTimeEnd.setText(String.format("%02d:%02d:%02d", currentTrackToHoursEnd, currentTrackToMinsEnd,
							currentTrackToSecsEnd));
				}
			}
		});
	}

	void setWidthAndHeightListener() {
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
	}

	@FXML
	void removeMusicButtonAction(ActionEvent event) {
		int index = musicList.getSelectionModel().getSelectedIndex();
		if (!musicList.getItems().isEmpty() && !musicClassList.isEmpty()) {
			musicList.getItems().remove(index);
			musicClassList.remove(index);
		}
	}

	@FXML
	void removeVideoButtonAction(ActionEvent event) {
		int index = videosList.getSelectionModel().getSelectedIndex();
		if (!videosList.getItems().isEmpty() && !videosClassList.isEmpty()) {
			videosList.getItems().remove(index);
			videosClassList.remove(index);
		}
	}

	void clearCurrentMediaStrings() {
		currentTitle = "";
		currentArtist = "";
		currentAlbum = "";
		currentYear = "";
	}

	String currentTitle;
	String currentArtist;
	String currentAlbum;
	String currentYear;
//	Image currentImage;

	void handleMetadata(String key, Object value) {
		switch (key) {
		case "album":
			currentAlbum = value.toString();
			break;
		case "artist":
			currentArtist = value.toString();
			break;
		case "title":
			currentTitle = value.toString();
			break;
		case "year":
			currentYear = value.toString();
			break;
//		    case "image":
//		        currentCover.setImage((Image)value);
//		        break;
		}
	}

	void addMediaToAppropriateList(PZMedia tempMedia) {
		if (tempMedia instanceof PZMovie) {
//			tempMedia.setTitle(currentTitle);
//			tempMedia.setYear(currentYear);

//			tempMedia.setDisplayName(currentTitle + " - " + currentYear);
			videosList.getItems().add(tempMedia.getDisplayName());
			videosClassList.add((PZMovie) tempMedia);
		} else if (tempMedia instanceof PZMusic) {
//			tempMedia.setTitle(currentTitle);
//			((PZMusic) tempMedia).setArtist(currentArtist);
//			((PZMusic) tempMedia).setAlbum(currentAlbum);
//			tempMedia.setYear(currentYear);

//			tempMedia.setDisplayName(currentTitle + " - " + currentArtist + " - " + currentAlbum + " - " + currentYear);

			musicList.getItems().add(tempMedia.getDisplayName());
			musicClassList.add((PZMusic) tempMedia);
		}
	}

	final float volumeHigh = 0.66f;
	final float volumeMedium = 0.33f;

	void updateVolumeLabels(double volume) {
		if (volume == 0) {
			volumeLabel0.setVisible(true);
			volumeLabel1.setVisible(false);
			volumeLabel2.setVisible(false);
			volumeLabel3.setVisible(false);
		} else {
			if (volume >= volumeHigh) {
				volumeLabel0.setVisible(false);
				volumeLabel1.setVisible(false);
				volumeLabel2.setVisible(false);
				volumeLabel3.setVisible(true);
			} else if (volume >= volumeMedium) {
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
	void menuCloseApp(ActionEvent event) {
		Platform.exit();
	}

	@FXML
	void musicListMouseClickedAction(MouseEvent event) {
//		System.out.println("clicked on " + musicList.getSelectionModel().getSelectedItem());
		String filePath;

		if (event.getClickCount() == 2) {
			if (mediaPlayer != null) {
				oldVolume = mediaPlayer.getVolume();
				mediaPlayer.dispose();
			}

			if (musicList.getSelectionModel().getSelectedItem() != null) {
//				System.out.println("selected in list: " + musicList.getSelectionModel().getSelectedIndex());
				filePath = musicClassList.get(musicList.getSelectionModel().getSelectedIndex()).getFilePath();
			} else {
				System.err.println("ERROR: Selected item is null.");
				return;
			}

			if (filePath != null && !filePath.isEmpty()) {
				Media media = new Media(filePath);

				clearCurrentMediaStrings();
				addMetadataListener(media);

				mediaPlayer = new MediaPlayer(media);
				mediaPlayer.setVolume(oldVolume);

				mediaView.setMediaPlayer(mediaPlayer);
				mediaView.setPreserveRatio(true);

				mediaPlayer.seek(mediaPlayer.getStartTime());
				mediaPlayer.play();
				isPlaying = true;

				switchPlayOrPauseButtonActive();

				addTimeListener();
				setWidthAndHeightListener();

				setOnMediaFinished();
			} else {
				System.err.println("ERROR: No media to play.");
			}
		}
	}

//	FORWARD - BACKWARD?!

	@FXML
	void videosListMouseClickedAction(MouseEvent event) {
//		System.out.println("clicked on " + musicList.getSelectionModel().getSelectedItem());
		String filePath;

		if (event.getClickCount() == 2) {
			if (mediaPlayer != null) {
				oldVolume = mediaPlayer.getVolume();
				mediaPlayer.dispose();
			}

			if (videosList.getSelectionModel().getSelectedItem() != null) {
//				System.out.println("selected in list: " + musicList.getSelectionModel().getSelectedIndex());
				filePath = videosClassList.get(videosList.getSelectionModel().getSelectedIndex()).getFilePath();
			} else {
				System.err.println("ERROR: Selected item is null.");
				return;
			}

			if (filePath != null && !filePath.isEmpty()) {
				Media media = new Media(filePath);
				clearCurrentMediaStrings();
				addMetadataListener(media);

				mediaPlayer = new MediaPlayer(media);
				mediaPlayer.setVolume(oldVolume);

				mediaView.setMediaPlayer(mediaPlayer);
				mediaView.setPreserveRatio(true);

				mediaPlayer.seek(mediaPlayer.getStartTime());
				mediaPlayer.play();
				isPlaying = true;

				switchPlayOrPauseButtonActive();

				addTimeListener();
				setWidthAndHeightListener();

				setOnMediaFinished();
			} else {
				System.err.println("ERROR: No media to play.");
			}
		}
	}

	@FXML
	void mediaviewMouseClickAction(MouseEvent event) {
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
			switchPlayOrPauseButtonActive();
		}
	}

	boolean browsingMediaSeeker = false;

	@FXML
	void sliderMediaMouseClickAction(MouseEvent event) {
//		System.out.println("Test 4");
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

			switchPlayOrPauseButtonActive();
		}
	}

	@FXML
	void sliderMediaMousePressedAction(MouseEvent event) {
//		System.out.println("Test 1");
		if (mediaPlayer != null) {
			mediaPlayer.pause();

			switchPlayOrPauseButtonActive();
		}
	}

	@FXML
	void sliderMediaMouseDraggedAction(MouseEvent event) {
//		System.out.println("Test 2");
		if (mediaPlayer != null) {
			mediaPlayer.seek(Duration.seconds(sliderMedia.getValue()));
		}
	}

	@FXML
	void sliderMediaMouseReleasedAction(MouseEvent event) {
//		System.out.println("Test 3");
		if (mediaPlayer != null) {
			if (isPlaying) {
				if (browsingMediaSeeker) {
					mediaPlayer.play();
				}
			}

			switchPlayOrPauseButtonActive();
		}
	}

	void switchPlayOrPauseButtonActive() {
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

	boolean isPlaying = false;

	void playOrPauseMedia() {
		if (mediaPlayer != null) {
			if (!isPlaying) {
				mediaPlayer.play();
//				System.out.println("Media playing.");
			} else {
				mediaPlayer.pause();
//				System.out.println("Media paused.");
			}

			isPlaying = !isPlaying;
			switchPlayOrPauseButtonActive();
		}
	}

	@FXML
	void playAndPauseMediaButttonAction(ActionEvent event) {
		playOrPauseMedia();
	}

	void clearPlayedTime() {
		mediaTimePlayed.setText("00:00:00");
	}

	@FXML
	void stopMediaButttonAction(ActionEvent event) {
		if (mediaPlayer != null) {
			mediaPlayer.seek(mediaPlayer.getTotalDuration());
			mediaPlayer.stop();

			isPlaying = false;
			switchPlayOrPauseButtonActive();
			clearPlayedTime();
//			System.out.println("Media stopped.");
		}
	}

	@FXML
	void sendToWebLink(ActionEvent event) {
		if (Desktop.isDesktopSupported()) {
			try {
				Desktop.getDesktop().browse(new URI(GITHUB_LINK));
			} catch (IOException e1) {
				e1.printStackTrace();
			} catch (URISyntaxException e1) {
				e1.printStackTrace();
			}
		}
	}
}
