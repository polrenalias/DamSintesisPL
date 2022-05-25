package Pruebas_Sistensis;
import java.sql.*;
/**
 * @author DiegoFS
 */
public class ConectarTabla {
	public static void main(String[] args) {
		try {
			//1.Crear conexion
			Connection miConexion = DriverManager.getConnection("jdbc:postgresql://192.168.56.101:5432/Sintesis", "postgres", "1234");	
			//2.Crear objeto STATEMENT
			Statement miStatement = miConexion.createStatement();
			//3.Ejecutar instruccion sql
			ResultSet miResultset = miStatement.executeQuery("SELECT * FROM Books");
			//4.Leer el resultSet
			while(miResultset.next()) {
				System.out.println(miResultset.getString("ID")+" "+miResultset.getString("Title")+" "+miResultset.getString("Author")+" "+miResultset.getString("Main_Genre")+" "+miResultset.getString("Sub_Genre")+" "+miResultset.getString("Launch_Date"));
			}
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println(e.getMessage());
		}
	}
}
