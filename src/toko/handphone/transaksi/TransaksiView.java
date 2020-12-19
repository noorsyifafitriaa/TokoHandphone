/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package toko.handphone.transaksi;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import toko.handphone.barang.BarangView;
import toko.handphone.barang.CariBarangView;
import toko.handphone.setting.Koneksi;

/**
 *
 * @author ASUS X454W
 */
public class TransaksiView extends javax.swing.JFrame {

    /**
     * Creates new form TransaksiView
     */
    public TransaksiView() {
        initComponents();
        ulang();
        data_pelanggan();
    }
    
    Connection conn = new Koneksi().getKoneksi();
    PreparedStatement pst;
    ResultSet rs;
    String status, sql;
    DefaultTableModel dtm;
    
    private void nota_otomatis(){
        try {
            sql = " select no_nota from tb_penjualan order by no_nota desc limit 1";
            pst = conn.prepareStatement(sql);
            rs = pst.executeQuery();
            if(rs.next()){
                int kode = Integer.parseInt(rs.getString(1).substring(4))+1;
                textNota.setText("NTA-"+kode);
            }else{
                textNota.setText("NTA-1000");
            }
        } catch (SQLException ex) {
            Logger.getLogger(BarangView.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void data_pelanggan(){
           try {
          //  comboPelanggan.removeAllItems();
            //comboPelanggan.addItem("Pilih Pelanggan");
            sql = "select nama_pelanggan from tb_pelanggan";
            pst = conn.prepareStatement(sql);
            rs = pst.executeQuery();
            while(rs.next()){
                comboPelanggan.addItem(rs.getString(1));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,e.toString());
        }
    }
    
    private void ulang(){
        nota_otomatis();
        comboPelanggan.setSelectedIndex(0);
        textNota.setEnabled(false);
        textIDPelanggan.setEnabled(false);
        textKodeBarang.setEnabled(false);
        textNamaBarang.setEnabled(false);
        textKategori.setEnabled(false);
        textHarga.setEnabled(false);
        textStok.setEnabled(false);
        textTotal.setEnabled(false);
        textIDPelanggan.setText("");
        textKodeBarang.setText("");
        textNamaBarang.setText("");
        textKategori.setText("");
        textHarga.setText("");
        textStok.setText("");
        textQty.setText("");
        textTotal.setText("");
        textBayar.setText("");
        textKembali.setText("");
        textKembali.setEnabled(false);
        dtm = (DefaultTableModel)tabelItemBelanja.getModel();
        while(dtm.getRowCount()> 0)
        {
            dtm.removeRow(0);
        }
    }
    
    private void hitung_total(){
        BigDecimal total = new BigDecimal(0);
        for(int a=0; a<tabelItemBelanja.getRowCount(); a++){
            total = total.add(new BigDecimal(tabelItemBelanja.getValueAt(a, 5).toString()));
        }
        textTotal.setText(total.toString());
    }
    
    private boolean validasi(){
        boolean cek = false;
        if(textIDPelanggan.getText().isEmpty()){
            JOptionPane.showMessageDialog(null, "Data Pelanggan belum diisi",null,JOptionPane.ERROR_MESSAGE);
            comboPelanggan.requestFocus();
        }else if(tabelItemBelanja.getRowCount()<=0){
            JOptionPane.showMessageDialog(null, "Data Barang belanja masih kosong",null,JOptionPane.ERROR_MESSAGE);
            buttonCariBarang.requestFocus();
        }else if(textBayar.getText().isEmpty()){
            JOptionPane.showMessageDialog(null, "Textbox Bayar belum diisi",null,JOptionPane.ERROR_MESSAGE);
            textBayar.requestFocus();
        }else if(Integer.parseInt(textBayar.getText())<Integer.parseInt(textTotal.getText())){
            JOptionPane.showMessageDialog(null, "Tidak Melayani hutang",null,JOptionPane.ERROR_MESSAGE);
            textBayar.requestFocus();
    }
        else{
           cek = true; 
        }
        return cek;
    }
    
    private void simpan_transaksi(){
        if(validasi()){
            try {
                pst = conn.prepareStatement("insert into tb_penjualan values (?,?,?,?,?,?)");
                pst.setString(1, textNota.getText());
                pst.setString(2, textIDPelanggan.getText());
                pst.setBigDecimal(3, new BigDecimal(textTotal.getText()));
                pst.setBigDecimal(4, new BigDecimal(textBayar.getText()));
                pst.setBigDecimal(5, new BigDecimal(textKembali.getText()));
                int isSucces = pst.executeUpdate();
                if(isSucces == 1){
                    simpan_item_belanja();
                }
                JOptionPane.showMessageDialog(null, "Data Berhasil disimpan!");
                ulang();
            } catch(SQLException ex){
                JOptionPane.showMessageDialog(null, "Terjadi kesalahan pada Simpan Transaksi: Details \n"+ex.toString());
            }
        }
    }
    
    private void simpan_item_belanja(){
        for(int a = 0; a<= tabelItemBelanja.getRowCount()-1;a++){
            try {
                pst = conn.prepareStatement("insert into tb_detail_penjualan(no_nota,kode_barang,qty) values(?,?,?)");
                String kode; int jumlah;
                kode = tabelItemBelanja.getValueAt(a, 0).toString();
                jumlah = Integer.parseInt(tabelItemBelanja.getValueAt(a, 4).toString());
                pst.setString(1, textNota.getText());
                pst.setString(2, kode);
                pst.setInt(3, jumlah);
                pst.executeUpdate();
                update_stok(kode,jumlah);
            } catch (SQLException ex){
                JOptionPane.showMessageDialog(null, "Terjadi kesalahan pada Simpan item belanja: Details \n"+ex.toString());
            }
        }
    }
    
    private void update_stok(String kode, int jumlah){
        try {
            sql = "update tb_barang set stok=stok-? where kode_barang = ?";
            pst = conn.prepareStatement(sql);
            pst.setInt(1, jumlah);
            pst.setString(2, kode);
            pst.executeUpdate();
        } catch(SQLException ex){
            System.out.println("Terjadi kesalahan pada update stok: " + ex.toString());
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        textNota = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        comboPelanggan = new javax.swing.JComboBox<>();
        textIDPelanggan = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        textKodeBarang = new javax.swing.JTextField();
        buttonCariBarang = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        textNamaBarang = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        textKategori = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        textHarga = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        textStok = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        textQty = new javax.swing.JTextField();
        buttonTambah = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tabelItemBelanja = new javax.swing.JTable();
        jLabel11 = new javax.swing.JLabel();
        textTotal = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        textBayar = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        textKembali = new javax.swing.JTextField();
        buttonBatal = new javax.swing.JButton();
        buttonSimpan = new javax.swing.JButton();
        buttonHapus = new javax.swing.JButton();
        jLabel14 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(153, 204, 255));

        jLabel1.setFont(new java.awt.Font("Times New Roman", 1, 16)); // NOI18N
        jLabel1.setText("FORM TRANSAKSI");

        jLabel2.setText("NO NOTA");

        jLabel4.setText("NAMA PELANGGAN");

        comboPelanggan.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Pilih Pelanggan" }));
        comboPelanggan.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                comboPelangganItemStateChanged(evt);
            }
        });
        comboPelanggan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboPelangganActionPerformed(evt);
            }
        });

        jLabel5.setText("KODE BARANG");

        buttonCariBarang.setText("CARI");
        buttonCariBarang.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonCariBarangActionPerformed(evt);
            }
        });

        jLabel6.setText("NAMA BARANG");

        jLabel7.setText("KATEGORI");

        jLabel8.setText("HARGA");

        jLabel9.setText("STOK");

        jLabel10.setText("JUMLAH BELI");

        buttonTambah.setText("TAMBAH");
        buttonTambah.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonTambahActionPerformed(evt);
            }
        });

        tabelItemBelanja.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Kode Barang", "Nama Barang", "Kategori", "Harga", "Qty", "Subtotal"
            }
        ));
        jScrollPane1.setViewportView(tabelItemBelanja);

        jLabel11.setText("TOTAL");

        jLabel12.setText("BAYAR");

        textBayar.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                textBayarKeyReleased(evt);
            }
        });

        jLabel13.setText("KEMBALI");

        buttonBatal.setText("BATAL");
        buttonBatal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonBatalActionPerformed(evt);
            }
        });

        buttonSimpan.setText("SIMPAN");
        buttonSimpan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonSimpanActionPerformed(evt);
            }
        });

        buttonHapus.setText("HAPUS");
        buttonHapus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonHapusActionPerformed(evt);
            }
        });

        jLabel14.setFont(new java.awt.Font("Algerian", 0, 14)); // NOI18N
        jLabel14.setText("NOOR SYIFA FITRIA - 18630949");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel12)
                                    .addComponent(jLabel11))
                                .addGap(78, 78, 78)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(textTotal, javax.swing.GroupLayout.PREFERRED_SIZE, 205, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(123, 123, 123)
                                        .addComponent(jLabel13)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 48, Short.MAX_VALUE)
                                        .addComponent(textKembali, javax.swing.GroupLayout.PREFERRED_SIZE, 219, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(textBayar, javax.swing.GroupLayout.PREFERRED_SIZE, 205, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(buttonBatal)
                                        .addGap(32, 32, 32)
                                        .addComponent(buttonSimpan))))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel1)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jLabel4)
                                            .addComponent(jLabel5)
                                            .addComponent(jLabel6)
                                            .addComponent(jLabel7)
                                            .addComponent(jLabel2))
                                        .addGap(18, 18, 18)
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(comboPelanggan, javax.swing.GroupLayout.PREFERRED_SIZE, 353, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(textNota, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                                    .addComponent(textKategori, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 210, Short.MAX_VALUE)
                                                    .addComponent(textNamaBarang, javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addComponent(textKodeBarang, javax.swing.GroupLayout.Alignment.LEADING))
                                                .addGap(18, 18, 18)
                                                .addComponent(buttonCariBarang)))))
                                .addGap(10, 10, 10)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel8)
                                    .addComponent(jLabel9)
                                    .addComponent(jLabel10))
                                .addGap(48, 48, 48)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(textIDPelanggan, javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(textQty, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 32, Short.MAX_VALUE)
                                        .addComponent(buttonTambah))
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                            .addComponent(textStok, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 167, Short.MAX_VALUE)
                                            .addComponent(textHarga, javax.swing.GroupLayout.Alignment.LEADING))
                                        .addGap(0, 0, Short.MAX_VALUE))))
                            .addComponent(jScrollPane1))
                        .addContainerGap(183, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(buttonHapus)
                        .addGap(355, 355, 355))))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel14)
                .addGap(286, 286, 286))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addGap(23, 23, 23)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(textNota, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(comboPelanggan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(textIDPelanggan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel8)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel5)
                        .addComponent(textKodeBarang, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(buttonCariBarang)
                        .addComponent(textHarga, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(textNamaBarang, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9)
                    .addComponent(textStok, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(textKategori, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel10)
                    .addComponent(textQty, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(buttonTambah))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(buttonHapus)
                .addGap(15, 15, 15)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel11)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(textTotal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel13)
                            .addComponent(textKembali, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(textBayar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel12)
                            .addComponent(buttonBatal)
                            .addComponent(buttonSimpan))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 51, Short.MAX_VALUE)
                .addComponent(jLabel14)
                .addContainerGap())
        );

        getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void comboPelangganItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_comboPelangganItemStateChanged

    }//GEN-LAST:event_comboPelangganItemStateChanged

    private void buttonCariBarangActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonCariBarangActionPerformed
        CariBarangView cbv = new CariBarangView(this, true);
        cbv.setVisible(true);
    }//GEN-LAST:event_buttonCariBarangActionPerformed

    private void buttonTambahActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonTambahActionPerformed
         if(textKodeBarang.getText().isEmpty()){
            JOptionPane.showMessageDialog(null,"Data Barang Belum diisi");
        }else if(textQty.getText().isEmpty()){
            JOptionPane.showMessageDialog(null,"Jumlah beli belum diisi");
        }else if(Integer.parseInt(textQty.getText()) > Integer.parseInt(textStok.getText())){
            JOptionPane.showMessageDialog(null,"Stok barang tidak cukup");
            textQty.setText("0");
            textQty.requestFocus();
        }else if(Integer.parseInt(textQty.getText())<=0){
           JOptionPane.showMessageDialog(null,"Jumlah beli tidak boleh dibawah nol!");
            textQty.setText("0");
            textQty.requestFocus();
        }else{
            DefaultTableModel dtm = (DefaultTableModel) tabelItemBelanja.getModel();
            ArrayList list = new ArrayList();
            list.add(textKodeBarang.getText());
            list.add(textNamaBarang.getText());
            list.add(textKategori.getText());
            list.add(textHarga.getText());
            list.add(textQty.getText());
            list.add(Integer.parseInt(textHarga.getText()) * Integer.parseInt(textQty.getText()));
            dtm.addRow(list.toArray());
            textKodeBarang.setText("");
            textNamaBarang.setText("");
            textKategori.setText("");
            textHarga.setText("");
            textStok.setText("");
            textQty.setText("");
            hitung_total();
        }
    }//GEN-LAST:event_buttonTambahActionPerformed

    private void comboPelangganActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboPelangganActionPerformed
            try {
            pst = conn.prepareStatement("select id_pelanggan from tb_pelanggan where nama_pelanggan =?");
            pst.setString(1, comboPelanggan.getSelectedItem().toString());
            rs = pst.executeQuery();
            if(rs.next()){
                textIDPelanggan.setText(rs.getString(1));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e.toString());
        }
    }//GEN-LAST:event_comboPelangganActionPerformed

    private void buttonHapusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonHapusActionPerformed
         int row = tabelItemBelanja.getSelectedRow();
        if(row <0){
            JOptionPane.showMessageDialog(null, "Pilih dulu item yang ini di hapus!");
        }else{
            dtm.removeRow(row);
            tabelItemBelanja.setModel(dtm);
            hitung_total();
        }
    }//GEN-LAST:event_buttonHapusActionPerformed

    private void textBayarKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_textBayarKeyReleased
         BigDecimal bayar = new BigDecimal(0);
        if(!textBayar.getText().equals("")){
            bayar = new BigDecimal(textBayar.getText());
        }
        BigDecimal total = new BigDecimal(textTotal.getText());
        BigDecimal kembali = bayar.subtract(total);
        textKembali.setText(kembali.toString());
    }//GEN-LAST:event_textBayarKeyReleased

    private void buttonSimpanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonSimpanActionPerformed
        simpan_transaksi();
    }//GEN-LAST:event_buttonSimpanActionPerformed

    private void buttonBatalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonBatalActionPerformed
        ulang();
    }//GEN-LAST:event_buttonBatalActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(TransaksiView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(TransaksiView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(TransaksiView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(TransaksiView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new TransaksiView().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonBatal;
    private javax.swing.JButton buttonCariBarang;
    private javax.swing.JButton buttonHapus;
    private javax.swing.JButton buttonSimpan;
    private static javax.swing.JButton buttonTambah;
    private javax.swing.JComboBox<String> comboPelanggan;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    public static javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tabelItemBelanja;
    private javax.swing.JTextField textBayar;
    public static javax.swing.JTextField textHarga;
    private javax.swing.JTextField textIDPelanggan;
    public static javax.swing.JTextField textKategori;
    private javax.swing.JTextField textKembali;
    public static javax.swing.JTextField textKodeBarang;
    public static javax.swing.JTextField textNamaBarang;
    private javax.swing.JTextField textNota;
    private javax.swing.JTextField textQty;
    public static javax.swing.JTextField textStok;
    private javax.swing.JTextField textTotal;
    // End of variables declaration//GEN-END:variables
}
