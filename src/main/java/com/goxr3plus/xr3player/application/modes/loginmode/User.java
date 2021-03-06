/**
 * 
 */
package main.java.com.goxr3plus.xr3player.application.modes.loginmode;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import main.java.com.goxr3plus.xr3player.application.Main;
import main.java.com.goxr3plus.xr3player.application.database.PropertiesDb;
import main.java.com.goxr3plus.xr3player.application.tools.fx.JavaFXTools;
import main.java.com.goxr3plus.xr3player.application.tools.fx.NotificationType;
import main.java.com.goxr3plus.xr3player.application.tools.general.ActionTool;
import main.java.com.goxr3plus.xr3player.application.tools.general.InfoTool;
import main.java.com.goxr3plus.xr3player.smartcontroller.media.FileCategory;

/**
 * @author GOXR3PLUS
 *
 */
public class User extends StackPane {
	
	@FXML
	private ImageView imageView;
	
	@FXML
	private Label nameField;
	
	@FXML
	private Label descriptionLabel;
	
	@FXML
	private Label dropBoxLabel;
	
	@FXML
	private Label informationLabel;
	
	@FXML
	private Label warningLabel;
	
	@FXML
	private Label totalLibrariesLabel;
	
	// --------------------------------------------
	
	private final SimpleStringProperty descriptionProperty = new SimpleStringProperty("");
	
	/** The logger for this class */
	private static final Logger logger = Logger.getLogger(User.class.getName());
	
	public static final Image DEFAULT_USER_IMAGE = InfoTool.getImageFromResourcesFolder("user_image.jpg");
	
	/**
	 * Here are stored all the informations about the user and other things like opened libraries etc.
	 */
	private PropertiesDb userInformationDb;
	
	/**
	 * The position of the User into the List
	 */
	//private int position;
	private String userName;
	private LoginMode loginMode;
	
	/** This InvalidationListener is used during the rename of a user */
	private final InvalidationListener renameInvalidator = new InvalidationListener() {
		@Override
		public void invalidated(Observable observable) {
			
			// Remove the Listener
			Main.renameWindow.showingProperty().removeListener(this);
			
			// !Showing
			if (!Main.renameWindow.isShowing()) {
				
				// old && new -> name
				String oldName = getName();
				String newName = Main.renameWindow.getUserInput();
				boolean success = false;
				
				// Remove Bindings
				nameField.textProperty().unbind();
				
				// !XPressed
				if (Main.renameWindow.wasAccepted()) {
					
					// duplicate?
					if (!Main.loginMode.viewer.getItemsObservableList().stream().anyMatch(user -> user != User.this && ( (User) user ).getName().equalsIgnoreCase(newName))
							|| newName.equalsIgnoreCase(oldName)) {
						
						File originalFolder = new File(InfoTool.getAbsoluteDatabasePathWithSeparator() + oldName);
						File outputFolder = new File(InfoTool.getAbsoluteDatabasePathWithSeparator() + newName);
						
						//Check if the Folder can be renamed
						if (originalFolder.renameTo(outputFolder)) { //Success			
							success = true;
							setUserName(nameField.getText());
							nameField.getTooltip().setText(getName());
							
							//Change the absolute path of the UserInformation.properties file
							getUserInformationDb().setFileAbsolutePath(InfoTool.getAbsoluteDatabasePathWithSeparator() + userName + File.separator + "settings" + File.separator
									+ InfoTool.USER_INFORMATION_FILE_NAME);
							
							//Login Mode Sort Comparator
							if (Main.loginMode.getSelectedSortToggleText().contains("Name"))
								Main.loginMode.viewer.sortByComparator(Main.loginMode.getSortComparator());
							
							//Change Pie Data Name
							//							Main.loginMode.getSeries().getData().forEach(pieData -> {
							//								if (pieData.getXValue().equals(InfoTool.getMinString(oldName, 4)))
							//									pieData.setXValue(InfoTool.getMinString(newName, 4));
							//							});
						} else
							ActionTool.showNotification("Error", "An error occured trying to rename the user", Duration.seconds(2), NotificationType.ERROR);
						
					} //This user already exists
					else
						ActionTool.showNotification("Dublicate User", "Name->" + newName + " is already used from another User...", Duration.millis(2000),
								NotificationType.INFORMATION);
				}
				
				//Succeeded?
				if (!success)
					resetTheName();
				
			} // !Showing
		}
		
		/**
		 * Resets the name if the user cancels the rename operation
		 */
		private void resetTheName() {
			nameField.setText(getName());
		}
	};
	
	/**
	 * Constructor
	 * 
	 * @param userName
	 * @param position
	 * @param loginMode
	 */
	public User(String userName, int position, LoginMode loginMode) {
		this.setUserName(userName);
		//this.updatePosition(position);
		this.loginMode = loginMode;
		
		//Create the UserInformation DB
		userInformationDb = new PropertiesDb(
				InfoTool.getAbsoluteDatabasePathWithSeparator() + userName + File.separator + "settings" + File.separator + InfoTool.USER_INFORMATION_FILE_NAME, false);
		
		// ----------------------------------FXMLLoader-------------------------------------
		FXMLLoader loader = new FXMLLoader(getClass().getResource(InfoTool.USER_FXMLS + "User.fxml"));
		loader.setController(this);
		loader.setRoot(this);
		
		// -------------Load the FXML-------------------------------
		try {
			loader.load();
		} catch (IOException ex) {
			logger.log(Level.WARNING, "", ex);
		}
	}
	
	/**
	 * Called as soon as FXML file has been loaded
	 */
	@FXML
	private void initialize() {
		
		// --Key Listener
		setOnKeyReleased(this::onKeyReleased);
		
		// --Mouse Listener
		setOnMouseEntered(m -> {
			if (!isFocused())
				requestFocus();
		});
		
		// Clip
		//		Rectangle rect = new Rectangle();
		//		rect.widthProperty().bind(this.widthProperty());
		//		rect.heightProperty().bind(this.heightProperty());
		//		rect.setArcWidth(25);
		//		rect.setArcHeight(25);
		// rect.setEffect(new Reflection());
		
		// StackPane -> this
		//		this.setClip(rect);
		// Reflection reflection = new Reflection();
		// reflection.setInput(new DropShadow(4, Color.WHITE));
		// this.setEffect(reflection);
		
		//imageView
		String absoluteImagePath = JavaFXTools.getAbsoluteImagePath("userImage", InfoTool.getAbsoluteDatabasePathWithSeparator() + getName());
		if (absoluteImagePath == null)
			setDefaultImage();
		else
			imageView.setImage(new Image(new File(absoluteImagePath).toURI() + ""));
		
		//Name
		nameField.setText(getName());
		nameField.getTooltip().setText(getName());
		nameField.setOnMouseReleased(m -> {
			if (m.getButton() == MouseButton.PRIMARY && m.getClickCount() == 2 && Main.loginMode.viewer.centerItemProperty().get() == User.this)// Main.loginMode.teamViewer.getTimeline().getStatus() != Status.RUNNING)
				renameUser(nameField);
		});
		
		// ----InformationLabel
		informationLabel.setOnMouseReleased(m -> displayInformation());
		
		// ----DescriptionLabel
		descriptionLabel.visibleProperty().bind(descriptionProperty.isEmpty().not());
		descriptionLabel.setOnMouseReleased(informationLabel.getOnMouseReleased());
		
		//-- 
		warningLabel.setVisible(false);
	}
	
	/**
	 * Open display information stack pane for this user
	 */
	public void displayInformation() {
		if (Main.loginMode.viewer.centerItemProperty().get() == User.this)
			Main.loginMode.userInformation.displayForUser(this);
	}
	
	//	/**
	//	 * @return The Position of the user inside the list
	//	 */
	//	public int getPosition() {
	//		return position;
	//	}
	
	/**
	 * @return the userName
	 */
	public String getName() {
		return userName;
	}
	
	/**
	 * @return the totalLibrariesLabel
	 */
	public Label getTotalLibrariesLabel() {
		return totalLibrariesLabel;
	}
	
	public int getTotalLibraries() {
		return Integer.parseInt(totalLibrariesLabel.getText());
	}
	
	public int getTotalDropboxAccounts() {
		return Integer.parseInt(dropBoxLabel.getText());
	}
	
	/**
	 * @return the imageView
	 */
	public ImageView getImageView() {
		return imageView;
	}
	
	/**
	 * @return the nameField
	 */
	public Label getNameField() {
		return nameField;
	}
	
	/**
	 * @param nameField
	 *            the nameField to set
	 */
	public void setNameField(Label nameField) {
		this.nameField = nameField;
	}
	
	/**
	 * @param userName
	 *            the userName to set
	 */
	public void setUserName(String userName) {
		this.userName = userName;
		if (nameField != null)
			nameField.setText(userName);
	}
	
	//	/**
	//	 * @param position
	//	 *            the position to set
	//	 */
	//	public void updatePosition(int position) {
	//		this.position = position;
	//	}
	
	/**
	 * Renames the current User.
	 * 
	 * @param node
	 *            The node based on which the Rename Window will be position
	 */
	public void renameUser(Node node) {
		
		// Open the Window
		Main.renameWindow.show(getName(), node, "User Renaming", FileCategory.DIRECTORY);
		
		// Bind 
		nameField.textProperty().bind(Main.renameWindow.getInputField().textProperty());
		
		Main.renameWindow.showingProperty().addListener(renameInvalidator);
	}
	
	/**
	 * Used to delete a User
	 */
	public void deleteUser(Node owner) {
		//Ask
		if (ActionTool.doQuestion("Delete User",
				"Confirm that you want to 'delete' this user ,\n Name: [ " + ( (User) Main.loginMode.viewer.getSelectedItem() ).getName() + " ]", owner, Main.window)) {
			
			//Try to delete it		
			if (ActionTool.deleteFile(new File(InfoTool.getAbsoluteDatabasePathWithSeparator() + this.getName()))) {
				
				//Delete from the Model Viewer
				Main.loginMode.viewer.deleteItem(this);
				
				//Delete from PieChart
				//Main.loginMode.getSeries().getData().stream().filter(data -> data.getXValue().equals(this.getUserName())).findFirst()
				//		.ifPresent(data -> Main.loginMode.getSeries().getData().remove(data));
				
				//Flip Pane flip to Front
				Main.loginMode.flipPane.flipToFront();
				
			} else
				ActionTool.showNotification("Error", "An error occured trying to delete the user", Duration.seconds(2), NotificationType.ERROR);
		}
	}
	
	/**
	 * This method is called when a key is released.
	 *
	 * @param key
	 *            An event which indicates that a keystroke occurred in a javafx.scene.Node.
	 */
	public void onKeyReleased(KeyEvent key) {
		if (!loginMode.viewer.isCenterItem(this))
			return;
		
		//Check if Control is down
		if (key.isControlDown()) {
			
			KeyCode code = key.getCode();
			if (code == KeyCode.R)
				renameUser(this);
			else if (code == KeyCode.DELETE || code == KeyCode.D)
				deleteUser(this);
			else if (code == KeyCode.E)
				exportImage();
		} else if (key.getCode() == KeyCode.ENTER) {
			Main.startAppWithUser(this);
		} else if (key.getCode() == KeyCode.DELETE) {
			deleteUser(this);
		}
	}
	
	//----------------------------------------About Images---------------------------------------------------------------
	
	/**
	 * Reset's the user image back to the default
	 */
	public void setDefaultImage() {
		
		//Delete the Image inside the database
		deleteUserImage();
		
		//Set ImageView to null
		imageView.setImage(DEFAULT_USER_IMAGE);
	}
	
	/**
	 * The user has the ability to change the Library Image
	 *
	 */
	public void changeUserImage() {
		
		//Check the response
		JavaFXTools.selectAndSaveImage("userImage", InfoTool.getAbsoluteDatabasePathWithSeparator() + getName(), Main.specialChooser, Main.window)
				.ifPresent(imageFile -> imageView.setImage(new Image(imageFile.toURI() + "")));
	}
	
	/**
	 * Export the Library image.
	 */
	public void exportImage() {
		
		String absoluteImagePath = getAbsoluteImagePath();
		
		//Check if image exists
		if (absoluteImagePath == null)
			return;
		
		File file = Main.specialChooser.prepareToExportImage(Main.window, absoluteImagePath);
		
		//Check if user selected a folder for the image to be exported
		if (file != null)
			new Thread(() -> {
				if (!ActionTool.copy(absoluteImagePath, file.getAbsolutePath()))
					Platform.runLater(() -> ActionTool.showNotification("Exporting User Image", "Failed to export User image for \n User=[" + getName() + "]",
							Duration.millis(2500), NotificationType.SIMPLE));
			}).start();
		
	}
	
	/**
	 * Deletes the user background image
	 */
	private boolean deleteUserImage() {
		
		//Delete the User Image
		JavaFXTools.deleteAnyImageWithTitle("userImage", InfoTool.getAbsoluteDatabasePathWithSeparator() + getName());
		
		return true;
	}
	
	/**
	 * The absolute path of the user image in the local system
	 * 
	 * @return
	 */
	public String getAbsoluteImagePath() {
		return JavaFXTools.getAbsoluteImagePath("userImage", InfoTool.getAbsoluteDatabasePathWithSeparator() + getName());
	}
	
	/**
	 * @return the userInformationDb
	 */
	public PropertiesDb getUserInformationDb() {
		return userInformationDb;
	}
	
	/**
	 * @param userInformationDb
	 *            the userInformationDb to set
	 */
	public void setUserInformationDb(PropertiesDb userInformationDb) {
		this.userInformationDb = userInformationDb;
	}
	
	/**
	 * Returns the date this user created based on the folder creation date
	 */
	public String getDateCreated() {
		return InfoTool.getFileCreationDate(new File(userInformationDb.getFileAbsolutePath()));
	}
	
	/**
	 * Returns the Time this user created based on the folder creation date
	 */
	public String getTimeCreated() {
		return InfoTool.getFileCreationTime(new File(userInformationDb.getFileAbsolutePath()));
	}
	
	/**
	 * @return the descriptionLabel
	 */
	public Label getDescriptionLabel() {
		return descriptionLabel;
	}
	
	/**
	 * @return the dropBoxLabel
	 */
	public Label getDropBoxLabel() {
		return dropBoxLabel;
	}
	
	/**
	 * @return the descriptionProperty
	 */
	public SimpleStringProperty getDescriptionProperty() {
		return descriptionProperty;
	}
	
}
