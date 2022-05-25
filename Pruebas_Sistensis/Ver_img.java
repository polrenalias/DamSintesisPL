package Pruebas_Sistensis;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.swing.JFrame;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/*
public class Ver_img extends Application{
	
	private Statement miStatement; 
	private Connection miConexion;
	private ResultSet miResultset;
	
	private TextArea ta = new TextArea();
    ImageView imView = new ImageView();
    
	
	public void start(Stage primaryStage) {
		try {
			//establish the database connection 
			 initializeDB(); 
			 HBox hBox = new HBox(10); 
			  
			 BorderPane bpane = new BorderPane(); 
			 //bpane.setCenter(new ScrollPane(ta)); 
			 bpane.setTop(hBox); 
			 VBox vBox=new VBox (imView);
			 Scene scene = new Scene(vBox, 500, 500); 
			 primaryStage.setTitle("Image");  
			 primaryStage.setScene(scene);  
			 primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	private void initializeDB() {
		try{
			miConexion=DriverManager.getConnection("jdbc:postgresql://192.168.56.101:5432/Sintesis", "postgres", "1234");
			miStatement = miConexion.createStatement();
			miResultset = miStatement.executeQuery("select thumbnail from content where id_content = 5");
			
			while(miResultset.next()) {
				byte[] byteArray=miResultset.getBytes("thumbnail");
				// create the object of ByteArrayInputStream class
				// and initialized it with the byte array.
				ByteArrayInputStream inStreambj = new ByteArrayInputStream(byteArray);
				BufferedImage newImage = ImageIO.read(inStreambj);
				
				// crear el Image y mostrarlo en el ImageView
				Image img = new Image(new ByteArrayInputStream(byteArray));
				imView.setImage(img);
				
			}	
		}
	   	 catch(Exception e){
				System.out.println(e.getMessage());
		}
	}//end method initializeDB

	public static void main(String[] args) { 
		launch(args); 
	}//end method main	
	*/

public class Ver_img{
	public static void main(String[] args) { 
		try{
			Connection miConexion=DriverManager.getConnection("jdbc:postgresql://192.168.56.101:5432/Sintesis", "postgres", "1234");
			Statement miStatement = miConexion.createStatement();
			ResultSet miResultset = miStatement.executeQuery("select thumbnail from content where id_content = 5");
			
			while(miResultset.next()) {
				/*
				byte[] byteArray=miResultset.getBytes("thumbnail");
				// create the object of ByteArrayInputStream class
				// and initialized it with the byte array.
				ByteArrayInputStream inStreambj = new ByteArrayInputStream(byteArray);
				BufferedImage newImage = ImageIO.read(inStreambj);
				
				// crear el Image y mostrarlo en el ImageView
				Image img = new Image(new ByteArrayInputStream(byteArray));
			    ImageView imView = new ImageView();
				imView.setImage(img);
				*/
				File photo = new File("/home/dfernandez/Escritorio/cosa/java2.jpg");
	            FileOutputStream fos = new FileOutputStream(photo);

	            byte[] buffer = new byte[1];
	            InputStream is = miResultset.getBinaryStream("thumbnail");
	            while (is.read(buffer) > 0) {
	                fos.write(buffer);
	            }
	            fos.close();
				
			}	
		}
	   	 catch(Exception e){
				System.out.println(e.getMessage());
		}
	}//end method initializeDB
}