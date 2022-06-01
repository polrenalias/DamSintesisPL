package reader_app;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;
import java.util.List;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Accordion;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * Main class (Handles the elemental procedures of the application)
 * 
 * @author Pol Renalias
 *
 */
public class MainWindow extends Application {
	// Fields declaration & initialization
	static Stage secondStage = null, mainStage = null;
	static Scene mainScene = null;
	static File book;
	static Stage primaryStage;
	static String mainTheme;
	private static boolean readerStarted = false;
	static Connection con;
	static TableColumn<Book, String> imgCol, nameCol, authorCol, genreCol, yearCol, readerLink;
	static ObservableList<Book> bookList = FXCollections.observableArrayList();
	@SuppressWarnings("rawtypes")
	static TableView tview = new TableView();
	static Logger logger = Logger.getLogger("ProgramLog");
	static Properties usrProp = new Properties();
	static Path listPath, thumbPath, usrPath;
	static String username;

	@Override
	public void start(Stage primaryStage) throws Exception {
		createLocalDeps();
		startConnection();
		primaryStage.getIcons().add(new Image("resources\\app_icon.png"));
		primaryStage.setTitle("Authentication");
		primaryStage.setMaximized(false);
		primaryStage.setResizable(false);
		primaryStage.setScene(startAuthProcess());
		MainWindow.primaryStage = primaryStage;
		primaryStage.show();
	}

	/**
	 * Method used to connect with the database
	 */
	private static void startConnection() {
		try {
			con = DriverManager.getConnection("jdbc:postgresql://192.168.56.100:5432/ReaderHistory", "postgres",
					"1234");
		} catch (SQLException sqle) {
			logger.severe("SQLException - " + sqle.getMessage());
			System.exit(1);
		}
	}

	/**
	 * Method used to create a lock file if an instance of the application is
	 * already running
	 * 
	 * @return true/false (depends on the number of instances)
	 */
	private static boolean lockInstance() {
		try {
			final File file = new File("reader.lock");
			if (file.createNewFile()) {
				file.deleteOnExit();
				return true;
			}
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Method used to create (in case they don't already exist) the local
	 * dependencies of the application
	 * 
	 * @throws IOException If the path/directory/file cannot be found/created
	 */
	private void createLocalDeps() throws IOException {
		Path logPath = null;
		try {
			if (System.getProperty("os.name").contains("Windows")) {
				Path path = Paths.get(System.getProperty("user.dir") + File.separatorChar + "reader_local");
				if (!Files.exists(path)) {
					Files.createDirectory(path);
					Files.setAttribute(path, "dos:hidden", true);
				}
			} else if (System.getProperty("os.name") == "Unix" || System.getProperty("os.name") == "Mac") {
				Path path = Paths.get(System.getProperty("user.dir") + File.separatorChar + ".reader_tmp");
				if (!Files.exists(path)) {
					Files.createDirectory(path);
				}
			}
			logPath = Paths.get(System.getProperty("user.dir") + File.separatorChar + "reader_local"
					+ File.separatorChar + "reader.log");
			Path rsrcPath = Paths.get(System.getProperty("user.dir") + File.separatorChar + "reader_local"
					+ File.separatorChar + "resources");
			if (Files.notExists(rsrcPath)) {
				Files.createDirectory(rsrcPath);
			}
			Path configPath = Paths.get(System.getProperty("user.dir") + File.separatorChar + "reader_local"
					+ File.separatorChar + "config");
			if (Files.notExists(configPath)) {
				Files.createDirectory(configPath);
			}
			Files.deleteIfExists(logPath);
			Files.createFile(logPath);
			FileHandler handler = new FileHandler(logPath + "");
			logger.addHandler(handler);
			SimpleFormatter formatter = new SimpleFormatter();
			handler.setFormatter(formatter);
			logger.info("DEBUG - Init program");
		} catch (IOException io) {
			io.printStackTrace();
		}
	}

	/**
	 * Method used to create the authentication window and implement its functions
	 * 
	 * @return the Scene (the functioning authentication window)
	 */
	Scene startAuthProcess() {
		GridPane grid = new GridPane();
		grid.setAlignment(Pos.CENTER);
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(25, 25, 25, 25));

		Text scenetitle = new Text("L-Reader");
		scenetitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
		Label userLabel = new Label("User name:");
		TextField userField = new TextField();
		Label passwLabel = new Label("Password:");
		PasswordField passwField = new PasswordField();

		grid.add(scenetitle, 0, 0, 2, 1);
		grid.add(userLabel, 0, 1);
		grid.add(userField, 1, 1);
		grid.add(passwLabel, 0, 2);
		grid.add(passwField, 1, 2);

		Button logInBttn = new Button("Log in");
		Button signInBttn = new Button("Sign in");
		Button errorSign1 = new Button();
		Button errorSign2 = new Button();
		errorSign1.setId("signerr");
		errorSign2.setId("signerr");
		errorSign1.setGraphic(new ImageView("resources\\warning.png"));
		errorSign2.setGraphic(new ImageView("resources\\warning.png"));
		errorSign1.setVisible(false);
		errorSign2.setVisible(false);
		grid.add(errorSign1, 2, 1);
		grid.add(errorSign2, 2, 2);

		Pattern passwReqs = Pattern.compile("(?=.*?[a-z]+)(?=.*?[A-Z]+)(?=.*?[0-9]+)");

		logInBttn.setOnMouseClicked(e -> {
			errorSign1.setVisible(false);
			errorSign2.setVisible(false);
			errorSign2.setTooltip(null);
			if (userField.getText() != "" & passwField.getText() != "") {
				Matcher matcher = passwReqs.matcher(passwField.getText());
				try {
					if (checkUsername(userField.getText()) == true) {
						if (matcher.find() & passwField.getText().toCharArray().length >= 8) {
							if (attemptLogIn(userField.getText(), passwField.getText()) == true) {
								username = userField.getText();
								initProfile();
								startCatalog();
								loadMainWindow();
							} else {
								errorSign2.setTooltip(new Tooltip("The password isn't correct"));
								errorSign2.setVisible(true);
							}
						} else {
							errorSign2.setTooltip(new Tooltip("The password doesn't meet the complexity requirements"));
							errorSign2.setVisible(true);
						}
					} else {
						errorSign1.setTooltip(new Tooltip("The user doesn't exist"));
						errorSign1.setVisible(true);
					}
				} catch (SQLException se) {
					logger.warning("SQLException - " + se.getMessage());
				}
			}
		});

		signInBttn.setOnMouseClicked(e -> {
			errorSign1.setVisible(false);
			errorSign2.setVisible(false);
			if (userField.getText() != "" & passwField.getText() != "") {
				Matcher matcher = passwReqs.matcher(passwField.getText());
				try {
					if (checkUsername(userField.getText()) == false) {
						if (matcher.find() & passwField.getText().toCharArray().length >= 8) {
							if (attemptSignIn(userField.getText(), passwField.getText()) == true) {
								username = userField.getText();
								initProfile();
								startCatalog();
								loadMainWindow();
							}
						} else {
							errorSign2.setTooltip(new Tooltip("The password doesn't meet the complexity requirements"));
							errorSign2.setVisible(true);
						}
					} else {
						errorSign1.setTooltip(new Tooltip("The user name is already in use"));
						errorSign1.setVisible(true);
					}
				} catch (SQLException se) {
					logger.warning("SQLException - " + se.getMessage());
				}
			}
		});

		HBox hb = new HBox(10);
		hb.setId("login");
		hb.setAlignment(Pos.BOTTOM_RIGHT);
		hb.getChildren().addAll(logInBttn, signInBttn);
		grid.add(hb, 1, 4);

		Scene formScene = new Scene(grid, 352, 225);
		formScene.getStylesheets().add(getClass().getResource("light_theme.css").toString());
		mainTheme = "light_theme.css";

		return formScene;
	}

	/**
	 * Method used to initialize the user profile (the user's book list and the
	 * configuration file)
	 */
	private void initProfile() {
		try {
			listPath = Paths.get(System.getProperty("user.dir") + File.separatorChar + "reader_local"
					+ File.separatorChar + "config" + File.separatorChar + username + ".list");
			if (Files.notExists(listPath)) {
				Files.createFile(listPath);
			} else {
				readList();
			}
			usrPath = Paths.get(System.getProperty("user.dir") + File.separatorChar + "reader_local"
					+ File.separatorChar + "config" + File.separatorChar + username + ".conf");
			if (Files.notExists(usrPath)) {
				Files.createFile(usrPath);
			} else {
				readConf();
			}
		} catch (IOException ioe) {
			logger.severe("IOException - " + ioe.getMessage());
		}
	}

	/**
	 * Method used to check if the user name is correct/already exists
	 * 
	 * @param usr User name
	 * @return true/false (if found/if not found)
	 * @throws SQLException when the query fails for any reason
	 */
	private boolean checkUsername(String usr) throws SQLException {
		String userQuery = "SELECT username FROM users WHERE username = ?";
		PreparedStatement checkUserStatement = con.prepareStatement(userQuery);
		checkUserStatement.setString(1, usr);
		ResultSet userResults = checkUserStatement.executeQuery();
		return userResults.next();
	}

	/**
	 * Method used to attempt the user log in process with the user provided values
	 * 
	 * @param usr User name
	 * @param pwd Password
	 * @return true/false (if password is correct/not correct)
	 * @throws SQLException when the query fails for any reason
	 */
	private boolean attemptLogIn(String usr, String pwd) throws SQLException {
		String userQuery = "SELECT password FROM users WHERE username = ? AND password = ?";
		String encodedPwd = Base64.getEncoder().encodeToString(pwd.getBytes());
		PreparedStatement checkUserStatement = con.prepareStatement(userQuery);
		checkUserStatement.setString(1, usr);
		checkUserStatement.setString(2, encodedPwd);
		ResultSet userResults = checkUserStatement.executeQuery();
		return userResults.next();
	}

	/**
	 * Method used to attempt the user registration process with the user provided
	 * values
	 * 
	 * @param usr User name
	 * @param pwd Password
	 * @return true/false (if data is added/is not added)
	 * @throws SQLException when the query fails for any reason
	 */
	private boolean attemptSignIn(String usr, String pwd) throws SQLException {
		String insertQuery = "INSERT into users(username, password) VALUES (?, ?)";
		String encodedPwd = Base64.getEncoder().encodeToString(pwd.getBytes());
		PreparedStatement registerUser = con.prepareStatement(insertQuery);
		registerUser.setString(1, usr);
		registerUser.setString(2, encodedPwd);
		int checkInsert = registerUser.executeUpdate();
		if (checkInsert > 0) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Method used to load the catalog (main window) after the user authentication
	 * is done
	 */
	static void loadMainWindow() {
		primaryStage.hide();
		primaryStage.getIcons().add(new Image("resources\\app_icon.png"));
		primaryStage.setTitle("Catalog");
		primaryStage.setResizable(true);
		primaryStage.setScene(mainScene);
		primaryStage.setMaximized(false);
		primaryStage.show();
	}

	/**
	 * Method used to create the main window (catalog) and its functionalities
	 */
	@SuppressWarnings("unchecked")
	void startCatalog() {
		BorderPane bpane = new BorderPane();
		StackPane container = new StackPane(tview);

		tview.setPlaceholder(new Label("No books to display"));
		imgCol = new TableColumn<>("Thumbnail");
		nameCol = new TableColumn<>("Name");
		authorCol = new TableColumn<>("Author");
		genreCol = new TableColumn<>("Genre");
		yearCol = new TableColumn<>("Year");
		readerLink = new TableColumn<>("");
		imgCol.setCellValueFactory(new PropertyValueFactory<Book, String>("Thumbnail"));
		nameCol.setCellValueFactory(new PropertyValueFactory<Book, String>("Name"));
		authorCol.setCellValueFactory(new PropertyValueFactory<Book, String>("Author"));
		genreCol.setCellValueFactory(new PropertyValueFactory<Book, String>("Genre"));
		yearCol.setCellValueFactory(new PropertyValueFactory<Book, String>("Year"));
		readerLink.setCellValueFactory(new PropertyValueFactory<Book, String>("Link"));

		imgCol.setSortable(false);
		nameCol.setSortable(false);
		authorCol.setSortable(false);
		genreCol.setSortable(false);
		yearCol.setSortable(false);
		readerLink.setSortable(false);

		tview.getColumns().addAll(imgCol, nameCol, authorCol, genreCol, yearCol, readerLink);
		tview.getItems().addAll(bookList);

		imgCol.setResizable(false);
		nameCol.setResizable(false);
		authorCol.setResizable(false);
		genreCol.setResizable(false);
		yearCol.setResizable(false);
		readerLink.setResizable(false);

		imgCol.prefWidthProperty().bind(tview.widthProperty().divide(7.5));
		nameCol.prefWidthProperty().bind(tview.widthProperty().divide(4));
		authorCol.prefWidthProperty().bind(tview.widthProperty().divide(4));
		genreCol.prefWidthProperty().bind(tview.widthProperty().divide(7));
		yearCol.prefWidthProperty().bind(tview.widthProperty().divide(14));
		readerLink.prefWidthProperty().bind(tview.widthProperty().divide(6.7));

		ToolBar tbar = new ToolBar();
		FileChooser fPrompt = new FileChooser();
		fPrompt.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
		fPrompt.setTitle("Select an e-book");
		MenuItem openBttn = new MenuItem("Add file...");
		MenuItem aboutBttn = new MenuItem("About");
		MenuItem helpBttn = new MenuItem("Help");

		if (System.getProperty("os.name").contains("Windows")) {
			fPrompt.setInitialDirectory(new File("C:\\Users\\" + System.getProperty("user.name") + "\\Documents"));
		} else {
			fPrompt.setInitialDirectory(new File("/home/" + System.getProperty("user.name") + "/Documents"));
		}

		Alert helpBox = new Alert(AlertType.INFORMATION);
		Alert aboutBox = new Alert(AlertType.INFORMATION);
		helpBox.setTitle("Help");
		helpBox.setHeaderText("Reader keybindings");
		helpBox.setContentText(
				"[F] key - Toggle fullscreen\n[Q] key - Close reader\n[\u2190] / [Numpad 4] keys - Previous page\n[\u2192] / [Numpad 6] keys - Next page\n[Numpad +] / [Right brace] keys - Zoom in\n[Numpad -] / [Dash] keys - Zoom out\n[Z] key - Reset view");
		aboutBox.setTitle("About");
		aboutBox.setHeaderText("Project L\nv0.1");
		aboutBox.setContentText("E-book reader application (WIP)\n\n\n\nDeveloped by Diego Fernandez and Pol Renalias");
		Stage stageH = (Stage) helpBox.getDialogPane().getScene().getWindow();
		Stage stageA = (Stage) aboutBox.getDialogPane().getScene().getWindow();
		stageH.getIcons().add(new Image("resources\\app_icon.png"));
		stageA.getIcons().add(new Image("resources\\app_icon.png"));
		helpBox.setGraphic(null);
		aboutBox.setGraphic(new ImageView("resources\\app_icon_s.png"));

		aboutBttn.setOnAction(e -> aboutBox.show());
		helpBttn.setOnAction(e -> helpBox.show());
		openBttn.setOnAction(e -> {
			List<File> list = fPrompt.showOpenMultipleDialog(primaryStage);
			if (list != null) {
				try {
					for (File file : list) {
						PDDocumentInformation docInfo = PDDocument.load(file).getDocumentInformation();
						addToTable(file, docInfo);
					}
				} catch (IOException ioe) {
					logger.warning("IOException" + ioe.getMessage());
				}
			}
		});

		MenuButton mbutton = new MenuButton("Options", null, openBttn, aboutBttn, helpBttn);
		TextField tf = new TextField("Search book...");
		tf.setMaxWidth(150);
		tf.setDisable(true);
		Button themeBttn = new Button();
		themeBttn.setId("theme_switch");
		themeBttn.setTooltip(new Tooltip("Change theme"));
		final Pane spacer = new Pane();
		HBox.setHgrow(spacer, Priority.ALWAYS);
		tbar.getItems().addAll(mbutton, spacer, tf, themeBttn);

		VBox pcontent = new VBox();
		VBox menubar = new VBox(tbar);
		Hyperlink hl0 = new Hyperlink("Name");
		Hyperlink hl1 = new Hyperlink("Author");
		Hyperlink hl2 = new Hyperlink("Genre");
		Hyperlink hl3 = new Hyperlink("Year");
		pcontent.getChildren().addAll(hl0, hl1, hl2, hl3);
		Accordion sortMenu = new Accordion();
		TitledPane pane0 = new TitledPane("Sort by", pcontent);
		TitledPane pane1 = new TitledPane("Browse by", null);

		hl0.setOnAction(e -> sortBy('N'));
		hl1.setOnAction(e -> sortBy('A'));
		hl2.setOnAction(e -> sortBy('G'));
		hl3.setOnAction(e -> sortBy('Y'));

		themeBttn.setOnMouseClicked(e -> {
			if (mainTheme == "light_theme.css") {
				mainScene.getStylesheets().add(getClass().getResource("dark_theme.css").toString());
				mainScene.getStylesheets().remove(getClass().getResource("light_theme.css").toString());
				mainTheme = "dark_theme.css";
				if (readerStarted == true) {
					Reader.readerScene.getStylesheets().add(getClass().getResource("dark_theme.css").toString());
					Reader.readerScene.getStylesheets().remove(getClass().getResource("light_theme.css").toString());
				}
			} else {
				mainScene.getStylesheets().add(getClass().getResource("light_theme.css").toString());
				mainScene.getStylesheets().remove(getClass().getResource("dark_theme.css").toString());
				mainTheme = "light_theme.css";
				if (readerStarted == true) {
					Reader.readerScene.getStylesheets().add(getClass().getResource("light_theme.css").toString());
					Reader.readerScene.getStylesheets().remove(getClass().getResource("dark_theme.css").toString());
				}
			}
		});

		pane1.setDisable(true);
		sortMenu.getPanes().add(pane0);
		sortMenu.getPanes().add(pane1);
		VBox selector = new VBox(sortMenu);
		bpane.setTop(menubar);
		bpane.setCenter(container);
		bpane.setLeft(selector);
		mainScene = new Scene(bpane, 1280, 720);
		selector.prefHeightProperty().bind(mainScene.heightProperty());

		if (mainTheme.equals("light_theme.css")) {
			mainScene.getStylesheets().add(getClass().getResource("light_theme.css").toString());
			mainTheme = "light_theme.css";
		} else if (mainTheme.equals("dark_theme.css")) {
			mainScene.getStylesheets().add(getClass().getResource("dark_theme.css").toString());
			mainTheme = "dark_theme.css";
		}
	}

	/**
	 * Method used to read the current user book list (if it exists)
	 * 
	 * @throws IOException when the attempt to access the list provokes any error
	 */
	private static void readList() throws IOException {
		BufferedReader reader = Files.newBufferedReader(listPath);
		String line;
		int i = 0;

		ImageView tn = new ImageView();
		tn.setFitWidth(90);
		tn.setFitHeight(150);

		try {
			while ((line = reader.readLine()) != null) {
				String[] elements = line.split(",");
				tn = new ImageView(new Image(elements[0], 100, 150, false, false));

				bookList.add(new Book(tn, elements[1], elements[2], elements[3], elements[4], elements[5]));
				bookList.get(i).setThumbnailPath(elements[0]);
				bookList.get(i).setPath(elements[6]);

				bookList.get(i).getLink().setOnAction(new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent arg0) {
						loadReader(new File(elements[6]));
					}
				});
				i++;
			}
		} catch (IOException ioe) {
			logger.severe("IOException: " + ioe.getMessage());
		}
	}

	/**
	 * Method used to read the current user configuration file (if it exists)
	 * 
	 * @throws IOException when the attempt to access the list provokes any error
	 */
	private static void readConf() throws IOException {
		BufferedReader reader = Files.newBufferedReader(usrPath);
		String line;
		byte[] decodBytes;
		int i = 0;

		while ((line = reader.readLine()) != null) {
			decodBytes = Base64.getDecoder().decode(line);
			String prop = new String(decodBytes);

			switch (i) {
			case 0:
				mainTheme = prop;
				break;
			case 1:
				Reader.zoomCount = Integer.parseInt(prop);
				break;
			}
			i++;
		}
	}

	/**
	 * Method invoked when the user adds a book with the file chooser prompt; it is
	 * used to add the book's data to the catalog table
	 * 
	 * @param file Book's file
	 * @param info Book's metadata
	 */
	@SuppressWarnings("unchecked")
	static void addToTable(File file, PDDocumentInformation info) {
		Book b;
		Path thumbPath = Paths.get((System.getProperty("user.dir") + File.separatorChar + "reader_local"
				+ File.separatorChar + "resources" + File.separatorChar + info.getTitle() + "_thumbnail.jpg"));

		if (!Files.exists(thumbPath)) {
			try {
				PDFRenderer pdfRenderer = new PDFRenderer(PDDocument.load(file));
				BufferedImage bim = pdfRenderer.renderImageWithDPI(0, 100, ImageType.RGB);
				ImageIOUtil.writeImage(bim, thumbPath.toString(), 100);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		ImageView tn = new ImageView(new Image(thumbPath.toAbsolutePath().toString()));
		tn.setFitWidth(90);
		tn.setFitHeight(150);

		if (info.getTitle() == null & info.getAuthor() == null & info.getSubject() == null
				& info.getKeywords() == null) {
			b = new Book(tn, file.getName(), "Unknown", "Unknown", "Unknown", file.getAbsolutePath());
		} else if (info.getTitle() != null & info.getAuthor() == null & info.getSubject() == null
				& info.getKeywords() == null) {
			b = new Book(tn, info.getTitle(), "Unknown", "Unknown", "Unknown", file.getAbsolutePath());
		} else if (info.getTitle() != null & info.getAuthor() != null & info.getSubject() == null
				& info.getKeywords() == null) {
			b = new Book(tn, info.getTitle(), info.getAuthor(), "Unknown", "Unknown", file.getAbsolutePath());
		} else {
			b = new Book(tn, info.getTitle(), info.getAuthor(), info.getSubject(), info.getKeywords(),
					file.getAbsolutePath());
		}

		b.setThumbnailPath(thumbPath.toString());
		b.getLink().setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				loadReader(new File(b.getPath()));
			}
		});

		if (!bookList.contains(b)) {
			bookList.add(b);
			ObservableList<Book> tmp = bookList;
			tview.getItems().clear();
			bookList = tmp;
			tview.getItems().addAll(bookList);
			tview.refresh();
		}
	}

	/**
	 * Method used to invoke the book reader window
	 * 
	 * @param f User selected book
	 */
	static void loadReader(File f) {
		Stage secondStage = new Stage();

		if (readerStarted == false) {
			Reader reader = new Reader();
			reader.startReader();
			readerStarted = true;
		}

		secondStage.setTitle("Reader");
		secondStage.setResizable(false);
		secondStage.setScene(Reader.readerScene);
		secondStage.getIcons().add(new Image("resources\\app_icon.png"));
		MainWindow.secondStage = secondStage;
		Reader.loadFile(f);

		try {
			storeRemoteData(username, f);
		} catch (SQLException se) {
			logger.severe("SQLException - " + se.getMessage());
		}
		secondStage.show();
	}

	/**
	 * Method used to add the reading history of the current user to the database
	 * 
	 * @param usr User name
	 * @param f   Book
	 * @throws SQLException when the query fails for any reason
	 */
	private static void storeRemoteData(String usr, File f) throws SQLException {
		String insertQuery = "INSERT INTO books (id_user, file_name, read_date) VALUES ((SELECT id FROM users WHERE username = ?), ?, CURRENT_TIMESTAMP)";
		PreparedStatement registerData = con.prepareStatement(insertQuery);
		registerData.setString(1, usr);
		registerData.setString(2, f.getName());
		int checkInsert = registerData.executeUpdate();
		if (!(checkInsert > 0)) {
			storeRemoteData(usr, f);
		}
	}

	/**
	 * Method invoked by the sorting events of the catalog table; used to sort the
	 * table contents by one of the book's metadata values
	 * 
	 * @param selection
	 */
	@SuppressWarnings("unchecked")
	private static void sortBy(char selection) {
		imgCol.setSortable(true);
		nameCol.setSortable(true);
		authorCol.setSortable(true);
		genreCol.setSortable(true);
		yearCol.setSortable(true);
		readerLink.setSortable(true);

		switch (selection) {
		case 'N':
			nameCol.setSortType(TableColumn.SortType.ASCENDING);
			tview.getSortOrder().add(nameCol);
			break;
		case 'A':
			authorCol.setSortType(TableColumn.SortType.ASCENDING);
			tview.getSortOrder().add(authorCol);
			break;
		case 'G':
			genreCol.setSortType(TableColumn.SortType.ASCENDING);
			tview.getSortOrder().add(genreCol);
			break;
		case 'Y':
			yearCol.setSortType(TableColumn.SortType.ASCENDING);
			tview.getSortOrder().add(yearCol);
			break;
		}
		tview.sort();
		tview.getSortOrder().clear();
		imgCol.setSortable(false);
		nameCol.setSortable(false);
		authorCol.setSortable(false);
		genreCol.setSortable(false);
		yearCol.setSortable(false);
		readerLink.setSortable(false);
	}

	/**
	 * Method used to store the current session data (user's book list and
	 * application configuration)
	 * 
	 * @throws IOException when the one or both files cannot be created/written
	 */
	private static void storeLocalData() throws IOException {
		Files.delete(usrPath);
		FileWriter fw = new FileWriter(usrPath.toString());
		String encTheme = Base64.getEncoder().encodeToString(mainTheme.getBytes());
		String encZoom = Base64.getEncoder().encodeToString(String.valueOf(Reader.zoomCount).getBytes());
		fw.write(encTheme + "\n" + encZoom);
		fw.close();
		Files.delete(listPath);

		if (!bookList.isEmpty()) {
			FileWriter writer = new FileWriter(listPath.toString());
			for (Book b : bookList) {
				writer.write(b.getThumbnailPath() + "," + b.getName() + "," + b.getAuthor() + "," + b.getGenre() + ","
						+ b.getYear() + "," + b.getLink().getText() + "," + b.getPath() + "\n");
			}
			writer.close();
		}
	}

	/**
	 * Override used on the stop method to have the application execute its content
	 * always on the application's closing (whatever situation had caused the
	 * closure)
	 */
	@Override
	public void stop() {
		if (username != null) {
			try {
				storeLocalData();
			} catch (IOException e) {
				logger.severe("IOException: " + e.getMessage());
			}
		}
	}

	/**
	 * Main method used to invoke the lockInstance method and to launch the JavaFX
	 * main thread
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		if (!lockInstance()) {
			logger.warning("An instance is already open. If it's not the case, delete the \"reader.lock\" file.");
			System.exit(0);
		}
		launch(args);
	}
}