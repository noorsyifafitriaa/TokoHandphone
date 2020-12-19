/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package toko.handphone.setting;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *
 * @author ASUS X454W
 */
public class Koneksi {
    String url = "jdbc:mysql://localhost/db_handphone";
    public Connection getKoneksi(){
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url,"root","");
        } catch (SQLException ex) {
            Logger.getLogger(Koneksi.class.getName()).log(Level.SEVERE, null, ex);
        }
        return conn;
    }
}
