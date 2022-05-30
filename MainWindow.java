package reader_app;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

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
 * 
 * @author Pol
 *
 */
public class MainWindow extends Application {
	public static Stage secondStage = null, mainStage = null;
	public static Scene mainScene = null;
	protected static File book;
	protected static Stage primaryStage;
	protected static String mainTheme;
	private static boolean readerStarted = false;
	protected static TableColumn<Book, String> imgCol, nameCol, authorCol, genreCol, yearCol, readerLink;
	protected static ObservableList<Book> bookList = FXCollections.observableArrayList();
	@SuppressWarnings("rawtypes")
	protected static TableView tview = new TableView();
	protected static Logger logger = Logger.getLogger("ProgramLog");
	protected static Properties usrProp = new Properties();
	protected static Path listPath, thumbPath, usrPath;

	@Override
	public void start(Stage primaryStage) throws Exception {
		createLocalDeps();
		primaryStage.getIcons().add(new Image("resources\\app_icon.png"));
		primaryStage.setTitle("Log in");
		primaryStage.setMaximized(false);
		primaryStage.setResizable(false);
		primaryStage.setScene(startLogin());
		MainWindow.primaryStage = primaryStage;
		primaryStage.show();
		startCatalog();
	}

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
	 * 
	 * @throws IOException
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
			listPath = Paths.get(System.getProperty("user.dir") + File.separatorChar + "reader_local"
					+ File.separatorChar + "config" + File.separatorChar + "book.list");
			if (Files.notExists(listPath)) {
				Files.createFile(listPath);
			} else {
				readList();
			}
			usrPath = Paths.get(System.getProperty("user.dir") + File.separatorChar + "reader_local"
					+ File.separatorChar + "config" + File.separatorChar + "usr.conf");
			if (Files.notExists(usrPath)) {
				Files.createFile(usrPath);
			} else {
				readConf();
			}
			Files.deleteIfExists(logPath);
			Files.createFile(logPath);
			FileHandler handler = new FileHandler(logPath + "");
			logger.addHandler(handler);
			SimpleFormatter formatter = new SimpleFormatter();
			handler.setFormatter(formatter);
			logger.info("DEBUG: Init program");
		} catch (IOException io) {
			io.printStackTrace();
		}
	}

	public Scene startLogin() {

		GridPane grid = new GridPane();
		grid.setAlignment(Pos.CENTER);
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(25, 25, 25, 25));

		Text scenetitle = new Text("Log in");
		scenetitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
		grid.add(scenetitle, 0, 0, 2, 1);

		Label userName = new Label("User Name:");
		grid.add(userName, 0, 1);

		TextField userTextField = new TextField();
		grid.add(userTextField, 1, 1);

		Label pw = new Label("Password:");
		grid.add(pw, 0, 2);

		PasswordField pwBox = new PasswordField();
		grid.add(pwBox, 1, 2);

		Button btn = new Button("Submit");
		btn.setOnMouseClicked(e -> {
			loadMainWindow();
		});
		HBox hbBtn = new HBox(10);
		hbBtn.setId("login");
		hbBtn.setAlignment(Pos.BOTTOM_RIGHT);
		hbBtn.getChildren().add(btn);
		grid.add(hbBtn, 1, 4);
		Scene formScene = new Scene(grid, 300, 275);
		if (!usrProp.isEmpty()) {
			formScene.getStylesheets().add(getClass().getResource(usrProp.getProperty("CURRENT_THEME")).toString());
			mainTheme = usrProp.getProperty("CURRENT_THEME");
		} else {
			formScene.getStylesheets().add(getClass().getResource("light_theme.css").toString());
			mainTheme = "light_theme.css";
		}
		return formScene;
	}

	public static void loadMainWindow() {
		primaryStage.hide();
		primaryStage.getIcons().add(new Image("resources\\app_icon.png"));
		primaryStage.setTitle("Catalog");
		primaryStage.setScene(mainScene);
		primaryStage.setMaximized(false);
		primaryStage.show();
	}

	@SuppressWarnings("unchecked")
	public void startCatalog() {
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
		mainScene.getStylesheets().add(getClass().getResource(mainTheme).toString());
	}

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
					public void handle(ActionEvent event) {
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
	 * 
	 * @throws IOException
	 */
	private static void readConf() throws IOException {
		BufferedReader reader = Files.newBufferedReader(usrPath);
		String[] params = new String[] { "CURRENT_THEME", "CURRENT_ZOOM" };
		String line;
		int i = 0;
		while ((line = reader.readLine()) != null) {
			usrProp.setProperty(params[i], line);
			i++;
		}
	}

	/**
	 * 
	 * @param file
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static void addToTable(File file, PDDocumentInformation info) {
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
		Book b = new Book(tn, info.getTitle(), info.getAuthor(), info.getSubject(), info.getKeywords(),
				file.getAbsolutePath());
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
	 * 
	 * @param f
	 */
	public static void loadReader(File f) {
		Stage secondStage = new Stage();
		if (readerStarted == false) {
			Reader reader = new Reader();
			reader.startReader();
			readerStarted = true;
		}
		secondStage.setTitle("Reader");
		secondStage.setResizable(false);
		secondStage.setScene(Reader.readerScene);
		if (!MainWindow.usrProp.isEmpty()) {
			Reader.zoomCount = Integer.parseInt(usrProp.getProperty("CURRENT_ZOOM"));
		}
		secondStage.getIcons().add(new Image("resources\\app_icon.png"));
		MainWindow.secondStage = secondStage;
		Reader.loadFile(f);
		secondStage.show();
	}

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
	 * 
	 * @throws IOException
	 */
	private static void userDataStore() throws IOException {
		usrProp.setProperty("CURRENT_THEME", mainTheme);
		usrProp.setProperty("CURRENT_ZOOM", Reader.zoomCount + "");
		FileWriter fw = new FileWriter(usrPath.toString());
		fw.write(usrProp.getProperty("CURRENT_THEME") + "\n" + usrProp.getProperty("CURRENT_ZOOM"));
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

	@Override
	public void stop() {
		try {
			userDataStore();
		} catch (IOException e) {
			logger.severe("IOException: " + e.getMessage());
		}
	}

	/**
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