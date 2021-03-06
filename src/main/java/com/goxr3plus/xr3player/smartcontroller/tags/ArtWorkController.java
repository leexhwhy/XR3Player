package main.java.com.goxr3plus.xr3player.smartcontroller.tags;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import main.java.com.goxr3plus.xr3player.application.Main;
import main.java.com.goxr3plus.xr3player.application.tools.fx.NotificationType;
import main.java.com.goxr3plus.xr3player.application.tools.general.ActionTool;
import main.java.com.goxr3plus.xr3player.application.tools.general.InfoTool;

/**
 * 
 * 
 * This class is used to display Album,Artist etc Images from Media Files
 * 
 * @author GOXR3PLUS
 *
 */
public class ArtWorkController extends StackPane {
	
	//--------------------------------------------------------
	
	@FXML
	private StackPane innerStackPane;
	
	@FXML
	private ImageView imageView;
	
	@FXML
	private Label notificationLabel;
	
	@FXML
	private Button save;
	
	//--------------------------------------------------------
	
	/** The logger. */
	private Logger logger = Logger.getLogger(getClass().getName());
	
	private final PictureUpdaterService pictureUpdaterService = new PictureUpdaterService();
	
	/**
	 * Constructor
	 */
	public ArtWorkController() {
		
		// ------------------------------------FXMLLOADER
		FXMLLoader loader = new FXMLLoader(getClass().getResource(InfoTool.FXMLS + "ArtWorkController.fxml"));
		loader.setController(this);
		loader.setRoot(this);
		
		try {
			loader.load();
		} catch (IOException ex) {
			logger.log(Level.SEVERE, "", ex);
		}
	}
	
	/**
	 * Called as soon as .fxml is initialized
	 */
	@FXML
	private void initialize() {
		
		//Save
		save.setOnAction(a -> {
			Optional.ofNullable(Main.specialChooser.prepareToExportImage(Main.window, "cover" + InfoTool.RANDOM.nextInt(50000) + ".png")).ifPresent(file -> {
				//System.out.println(file.getAbsolutePath())
				
				//Do the job using an external Thread
				new Thread(() -> saveToFile(pictureUpdaterService.getImage(), file)).start();
				
				//Show a Notification to  User
				ActionTool.showNotification("Exporting Album Image",
						"From File: \n" + InfoTool.getMinString(InfoTool.getFileName(pictureUpdaterService.getFileAbsolutePath()), 100), Duration.seconds(2),
						NotificationType.SIMPLE);
				
			});
		});
		
	}
	
	/**
	 * Save the Image to a File
	 * 
	 * @param image
	 * @param outputFile
	 */
	private static void saveToFile(Image image , File outputFile) {
		BufferedImage bImage = SwingFXUtils.fromFXImage(image, null);
		try {
			ImageIO.write(bImage, "png", outputFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Shows the album image for the selected Media File
	 * 
	 * @param absolutePath
	 */
	public void showMediaFileImage(String absolutePath) {
		if (absolutePath != null)
			pictureUpdaterService.startService(absolutePath);
	}
	
	/**
	 * @return the imageView
	 */
	public ImageView getImageView() {
		return imageView;
	}
	
	/**
	 * Using this Service as an external Thread which updates the Information
	 * based on the selected Media
	 * 
	 * @author GOXR3PLUS
	 *
	 */
	public class PictureUpdaterService extends Service<Void> {
		
		/**
		 * The absolute path of the given file
		 */
		private String fileAbsolutePath;
		private Image image;
		
		public void startService(String fileAbsolutePath) {
			this.fileAbsolutePath = fileAbsolutePath;
			
			//Restart the Service
			this.restart();
			
		}
		
		@Override
		protected Task<Void> createTask() {
			return new Task<Void>() {
				
				@Override
				protected Void call() throws Exception {
					
					//FileName
					//String fileName = InfoTool.getFileName(fileAbsolutePath)
					
					//Try to find the album image for the given Audio File
					image = InfoTool.getAudioAlbumImage(fileAbsolutePath, -1, -1);
					
					//Run on JavaFX Thread
					Platform.runLater(() -> {
						
						//Save Button
						save.setDisable(image == null);
						
						//Show the File Name
						//fileNameLabel.setText(fileName)
						
						//Set the Image
						if (image != null)
							getImageView().setImage(image);
						
						//Show the Notification Label
						notificationLabel.setVisible(image == null);
						
					});
					
					return null;
				}
			};
		}
		
		/**
		 * The Image that has rendered the last time
		 * 
		 * @return The Image
		 */
		public Image getImage() {
			return image;
		}
		
		public String getFileAbsolutePath() {
			return fileAbsolutePath;
		}
		
	}
	
}
