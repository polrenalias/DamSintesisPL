package reader_app;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Accordion;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToolBar;
import javafx.scene.control.cell.MapValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MainWindow extends Application {
	public static Stage secondStage = null;
	protected static File book;
	protected static Stage primaryStage;
	private static boolean readerStarted = false;
	@SuppressWarnings("rawtypes")
	protected static TableColumn<Map, String> imgCol, nameCol, authorCol, genreCol, yearCol, readerLink;
	@SuppressWarnings("rawtypes")
	protected static TableView tview = new TableView();
	protected static Logger logger = Logger.getLogger("ProgramLog");
	private static int instance = 0;

	@Override
	public void start(Stage primaryStage) throws Exception {
		primaryStage.getIcons().add(new Image("resources\\app_icon.png"));
		primaryStage.setTitle("Catalog");
		primaryStage.setMaximized(false);
		primaryStage.setScene(startCatalog());
		MainWindow.primaryStage = primaryStage;
		createLocalStorage();
		primaryStage.show();
	}

	private void createLocalStorage() throws IOException {
		Path filepath = null;
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
			filepath = Paths.get(System.getProperty("user.dir") + File.separatorChar + "reader_local"
					+ File.separatorChar + "reader.log");
			try {
				try {
					Files.deleteIfExists(filepath);
				} catch (Exception e) {
					instance++;
					filepath = Paths.get(System.getProperty("user.dir") + File.separatorChar + "reader_local"
							+ File.separatorChar + "reader_"+instance+".log");
				}
				Files.createFile(filepath);
			} catch (SecurityException s) {
				s.printStackTrace();
			}
			FileHandler handler = new FileHandler(filepath + "");
			logger.addHandler(handler);
			SimpleFormatter formatter = new SimpleFormatter();
			handler.setFormatter(formatter);
			logger.info("Init program");
		} catch (IOException io) {
			io.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	public Scene startCatalog() {
		BorderPane bpane = new BorderPane();
		StackPane container = new StackPane(tview);
		tview.setPlaceholder(new Label("No books to display"));
		imgCol = new TableColumn<>("Thumbnail");
		nameCol = new TableColumn<>("Name");
		authorCol = new TableColumn<>("Author");
		genreCol = new TableColumn<>("Genre");
		yearCol = new TableColumn<>("Year");
		readerLink = new TableColumn<>("");
		imgCol.setCellValueFactory(new MapValueFactory<>("Thumbnail"));
		nameCol.setCellValueFactory(new MapValueFactory<>("Name"));
		authorCol.setCellValueFactory(new MapValueFactory<>("Author"));
		genreCol.setCellValueFactory(new MapValueFactory<>("Genre"));
		yearCol.setCellValueFactory(new MapValueFactory<>("Year"));
		readerLink.setCellValueFactory(new MapValueFactory<>("Link"));
		imgCol.setSortable(false);
		nameCol.setSortable(false);
		authorCol.setSortable(false);
		genreCol.setSortable(false);
		yearCol.setSortable(false);
		readerLink.setSortable(false);
		tview.getColumns().addAll(imgCol, nameCol, authorCol, genreCol, yearCol, readerLink);
		tview.getItems().addAll(getContent());
		imgCol.setResizable(false);
		nameCol.setResizable(false);
		authorCol.setResizable(false);
		genreCol.setResizable(false);
		yearCol.setResizable(false);
		readerLink.setResizable(false);
		ToolBar tbar = new ToolBar();
		MenuItem aboutBttn = new MenuItem("About");
		MenuItem helpBttn = new MenuItem("Help");
		Alert helpBox = new Alert(AlertType.INFORMATION);
		Alert aboutBox = new Alert(AlertType.INFORMATION);
		helpBox.setTitle("Help");
		helpBox.setHeaderText("Reader keybindings");
		helpBox.setContentText(
				"F key - Toggle fullscreen\nQ key - Close reader\nLeft arrow key - Previous page\nRight arrow key - Next page\nPlus key - Zoom in\nMinus key - Zoom out");
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
		MenuButton mbutton = new MenuButton("Options", null, aboutBttn, helpBttn);
		TextField tf = new TextField("Search book...");
		tf.setMaxWidth(150);
		tf.setDisable(true);
		final Pane spacer = new Pane();
		HBox.setHgrow(spacer, Priority.ALWAYS);
		tbar.getItems().addAll(mbutton, spacer, tf);
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
		pane1.setDisable(true);
		sortMenu.getPanes().add(pane0);
		sortMenu.getPanes().add(pane1);
		VBox selector = new VBox(sortMenu);
		bpane.setTop(menubar);
		bpane.setCenter(container);
		bpane.setLeft(selector);
		Scene mainScene = new Scene(bpane, 1280, 720);
		selector.prefHeightProperty().bind(mainScene.heightProperty());
		mainScene.getStylesheets().add(getClass().getResource("styles.css").toString());
		return mainScene;
	}

	private static ObservableList<Map<String, Object>> getContent() {
		ObservableList<Map<String, Object>> items = FXCollections.<Map<String, Object>>observableArrayList();

		Map<String, Object> item1 = new HashMap<>();
		item1.put("Thumbnail", new ImageView(new Image("C:\\Users\\Pol\\Downloads\\files\\alice.jpg")));
		item1.put("Name", "Alice in Wonderland");
		item1.put("Author", "Lewis Carroll");
		item1.put("Genre", "Fantasy fiction");
		item1.put("Year", "2008");
		Hyperlink l1 = new Hyperlink("Read now");
		item1.put("Link", l1);
		File file1 = new File("C:\\Users\\Pol\\Downloads\\files\\Alice_in_Wonderland.pdf");
		l1.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				loadReader(file1);
			}
		});

		Map<String, Object> item2 = new HashMap<>();
		item2.put("Thumbnail", new ImageView(new Image("C:\\Users\\Pol\\Downloads\\files\\oz.jpg")));
		item2.put("Name", "The Wonderful Wizard of Oz");
		item2.put("Author", "Frank Baum");
		item2.put("Genre", "Juvenile fantasy");
		item2.put("Year", "1993");
		Hyperlink l2 = new Hyperlink("Read now");
		item2.put("Link", l2);
		File file2 = new File("C:\\Users\\Pol\\Downloads\\files\\Wizard-of-Oz-sample.pdf");
		l2.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				loadReader(file2);
			}
		});

		items.add(item1);
		items.add(item2);

		return items;
	}

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
		primaryStage.hide();
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

	public static void main(String[] args) {
		launch(args);
	}

}