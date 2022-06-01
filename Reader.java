package reader_app;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Screen;

/**
 * Reader class that contains the application's book reader inner workings
 * 
 * @author Pol Renalias
 *
 */
public class Reader {
	// Fields declaration and initialization
	static int zoomCount = 1;
	private static Text zoomMsg = new Text("x" + zoomCount);
	private static ImageView iv = new ImageView();
	private static Button nextPage = new Button();
	private static Button prevPage = new Button();
	private static Button firstPage = new Button();
	private static Button lastPage = new Button();
	private static Button zoomPlus = new Button();
	private static Button zoomReset = new Button();
	private static Button zoomMinus = new Button();
	private static BorderPane bpane = new BorderPane();
	private static VBox vBox = new VBox();
	private static int pageCount;
	private static PDDocument document = new PDDocument();
	private static PDFRenderer pdfRenderer = new PDFRenderer(document);
	private static TextField tf = new TextField();
	static Scene readerScene;

	/**
	 * Method used to create the reader window and its functionalities
	 */
	void startReader() {
		int screenW = (int) Screen.getPrimary().getBounds().getWidth();
		int screenH = (int) Screen.getPrimary().getBounds().getHeight();

		nextPage.setOnAction(e -> loadNext());
		prevPage.setOnAction(e -> loadPrev());
		firstPage.setOnAction(e -> {
			pageCount = 1;
			loadPrev();
		});
		lastPage.setOnAction(e -> {
			pageCount = document.getNumberOfPages() - 2;
			loadNext();
		});
		zoomPlus.setOnAction(e -> zoomIn());
		zoomReset.setOnAction(e -> {
			zoomCount = 2;
			zoomPlus.setDisable(false);
			zoomOut();
		});
		zoomMinus.setOnAction(e -> zoomOut());
		tf.setOnKeyPressed(e -> {
			if (e.getCode() == KeyCode.ENTER) {
				try {
					int num = Integer.parseInt(tf.getText());
					if (num != pageCount + 1) {
						loadCustom(num);
					}
				} catch (Exception ex) {
					tf.setText("" + (pageCount + 1));
				}
			}
		});

		iv.setFitHeight(1000);
		iv.setFitWidth(1000);
		iv.setPreserveRatio(true);
		HBox hBox = new HBox(10);
		ScrollPane spane = new ScrollPane();
		vBox.getChildren().add(iv);
		vBox.setPadding(new Insets(10, 410, 10, 410));
		spane.setContent(vBox);
		tf.setMaxWidth(65);
		tf.setId("pageSelector");
		tf.setFont(Font.font("verdana", FontWeight.BOLD, FontPosture.REGULAR, 18));
		StackPane zpane = new StackPane();

		zoomMsg.setFont(Font.font("verdana", FontWeight.BOLD, FontPosture.REGULAR, 18));
		zoomMsg.setFill(Color.WHITE);
		zpane.getChildren().add(zoomMsg);

		zoomMinus.setGraphic(new ImageView("resources\\zoom_minus.png"));
		zoomPlus.setGraphic(new ImageView("resources\\zoom_plus.png"));
		zoomReset.setGraphic(new ImageView("resources\\zoom_reset.png"));
		nextPage.setGraphic(new ImageView("resources\\arrow_next.png"));
		prevPage.setGraphic(new ImageView("resources\\arrow_back.png"));
		firstPage.setGraphic(new ImageView("resources\\arrow_first.png"));
		lastPage.setGraphic(new ImageView("resources\\arrow_last.png"));
		zoomMinus.setTooltip(new Tooltip("Reduce zoom value"));
		zoomPlus.setTooltip(new Tooltip("Increase zoom value"));
		zoomReset.setTooltip(new Tooltip("Reset view"));
		firstPage.setTooltip(new Tooltip("Go to first page"));
		lastPage.setTooltip(new Tooltip("Go to last page"));
		nextPage.setTooltip(new Tooltip("Go to next page"));
		prevPage.setTooltip(new Tooltip("Go to previous page"));

		hBox.getChildren().addAll(zoomMinus, zpane, zoomPlus, new Separator(Orientation.VERTICAL), zoomReset,
				new Separator(Orientation.VERTICAL), firstPage, prevPage, tf, nextPage, lastPage);
		zoomMinus.setDisable(true);
		zoomReset.setDisable(true);

		hBox.setAlignment(Pos.CENTER);
		spane.setMinViewportWidth(1000);
		spane.setHbarPolicy(ScrollBarPolicy.AS_NEEDED);
		spane.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
		bpane.setCenter(spane);
		bpane.setTop(hBox);
		Scene readerScene = new Scene(bpane, screenW, screenH);

		if (zoomCount != 1) {
			zoomMinus.setDisable(false);
			zoomReset.setDisable(false);
			if (zoomCount == 8) {
				zoomPlus.setDisable(false);
			}
			zoomCount /= 2;
			zoomIn();
		}

		readerScene.getStylesheets().add(getClass().getResource(MainWindow.mainTheme).toString());

		readerScene.setOnKeyPressed(e -> {
			if (e.getCode() == KeyCode.ADD || e.getCode() == KeyCode.PLUS) {
				zoomIn();
			} else if (e.getCode() == KeyCode.SUBTRACT || e.getCode() == KeyCode.MINUS) {
				zoomOut();
			} else if (e.getCode() == KeyCode.Z) {
				zoomCount = 2;
				zoomPlus.setDisable(false);
				zoomOut();
			} else if (e.getCode() == KeyCode.LEFT || e.getCode() == KeyCode.KP_LEFT) {
				loadPrev();
			} else if (e.getCode() == KeyCode.RIGHT || e.getCode() == KeyCode.KP_RIGHT) {
				loadNext();
			} else if (e.getCode() == KeyCode.Q) {
				try {
					document.close();
				} catch (IOException io) {
					MainWindow.logger.warning("IOException: " + io.getMessage());
				}
				MainWindow.secondStage.hide();
				MainWindow.secondStage = null;
				MainWindow.primaryStage.show();
			} else if (e.getCode() == KeyCode.F) {
				MainWindow.secondStage.setFullScreen(true);
			}
		});
		Reader.readerScene = readerScene;
	}

	/**
	 * Method used to load the user selected book to the reader
	 */
	static void loadFile(File pdfFilename) {
		try {
			pageCount = -1;
			document = PDDocument.load(pdfFilename);
			pdfRenderer = new PDFRenderer(document);
			loadNext();
			prevPage.setDisable(true);
			firstPage.setDisable(true);
		} catch (IOException e) {
			MainWindow.logger.severe("IOException: " + e.getMessage());
		} catch (Exception e) {
			MainWindow.logger.warning("Exception: " + e.getMessage());
		}
	}

	/**
	 * Method used to load the next page of the book
	 */
	static void loadNext() {
		try {
			if (pageCount < document.getNumberOfPages()) {
				pageCount++;
				BufferedImage bim = pdfRenderer.renderImageWithDPI(pageCount, 300, ImageType.RGB);
				Image img = SwingFXUtils.toFXImage(bim, null);
				iv.setImage(img);
				tf.setText("" + (pageCount + 1));
				prevPage.setDisable(false);
				firstPage.setDisable(false);
				if (pageCount == document.getNumberOfPages() - 1) {
					nextPage.setDisable(true);
					lastPage.setDisable(true);
				}
				bpane.requestFocus();
			}
		} catch (Exception e) {
			MainWindow.logger.warning("Exception: " + e.getMessage());
		}
	}

	/**
	 * Method used to load the previous page of the book
	 */
	static void loadPrev() {
		try {
			if (pageCount > 0) {
				pageCount--;
				BufferedImage bim = pdfRenderer.renderImageWithDPI(pageCount, 300, ImageType.RGB);
				Image img = SwingFXUtils.toFXImage(bim, null);
				iv.setImage(img);
				tf.setText("" + (pageCount + 1));
				nextPage.setDisable(false);
				lastPage.setDisable(false);
				if (pageCount == 0) {
					prevPage.setDisable(true);
					firstPage.setDisable(true);
				}
				bpane.requestFocus();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Method used to load a user inserted (via text field) page (only when it is an
	 * existing one)
	 * 
	 * @param i User provided page number
	 */
	static void loadCustom(int i) {
		try {
			int currentPage = pageCount;
			if (i >= 0 & i <= document.getNumberOfPages()) {
				if (i > currentPage) {
					pageCount = i - 2;
					loadNext();
				} else {
					pageCount = i;
					loadPrev();
				}
			} else {
				tf.setText("" + (pageCount + 1));
			}
		} catch (Exception e) {
			MainWindow.logger.warning("Exception: " + e.getMessage());
		}
	}

	/**
	 * Method used to increase the zoom; invoked by user action or the configuration
	 * file
	 */
	static void zoomIn() {
		try {
			if (zoomCount <= 16) {
				zoomCount *= 2;
				switch (zoomCount) {
				case 2:
					vBox.setPadding(new Insets(10, 375, 10, 375));
					iv.setFitHeight(1100);
					iv.setFitWidth(1100);
					zoomMsg.setText("x" + zoomCount);
					zoomMinus.setDisable(false);
					zoomReset.setDisable(false);
					break;
				case 4:
					vBox.setPadding(new Insets(10, 305, 10, 305));
					iv.setFitHeight(1300);
					iv.setFitWidth(1300);
					zoomMsg.setText("x" + zoomCount);
					break;
				case 8:
					vBox.setPadding(new Insets(10, 250, 10, 250));
					iv.setFitHeight(1500);
					iv.setFitWidth(1500);
					zoomMsg.setText("x" + zoomCount);
					break;
				case 16:
					vBox.setPadding(new Insets(8, 130, 8, 130));
					iv.setFitHeight(1800);
					iv.setFitWidth(1800);
					zoomMsg.setText("x" + zoomCount);
					zoomPlus.setDisable(true);
					break;
				}
			}
		} catch (Exception e) {
			MainWindow.logger.warning("Exception: " + e.getMessage());
		}
	}

	/**
	 * Method used to decrease the zoom value
	 */
	static void zoomOut() {
		try {
			if (zoomCount > 1) {
				switch (zoomCount) {
				case 2:
					vBox.setPadding(new Insets(10, 410, 10, 410));
					iv.setFitHeight(1000);
					iv.setFitWidth(1000);
					zoomMsg.setText("x" + zoomCount / 2);
					zoomMinus.setDisable(true);
					zoomReset.setDisable(true);
					break;
				case 4:
					vBox.setPadding(new Insets(10, 375, 10, 375));
					iv.setFitHeight(1100);
					iv.setFitWidth(1100);
					zoomMsg.setText("x" + zoomCount / 2);
					break;
				case 8:
					vBox.setPadding(new Insets(10, 305, 10, 305));
					iv.setFitHeight(1300);
					iv.setFitWidth(1300);
					zoomMsg.setText("x" + zoomCount / 2);
					break;
				case 16:
					vBox.setPadding(new Insets(10, 250, 10, 250));
					iv.setFitHeight(1500);
					iv.setFitWidth(1500);
					zoomMsg.setText("x" + zoomCount / 2);
					zoomPlus.setDisable(false);
					break;
				}
				zoomCount /= 2;
			}
		} catch (Exception e) {
			MainWindow.logger.warning("Exception: " + e.getMessage());
		}
	}
}