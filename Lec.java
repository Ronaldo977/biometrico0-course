package lector_;

import com.digitalpersona.onetouch.DPFPDataPurpose;
import com.digitalpersona.onetouch.DPFPFeatureSet;
import com.digitalpersona.onetouch.DPFPGlobal;
import com.digitalpersona.onetouch.DPFPSample;
import com.digitalpersona.onetouch.DPFPTemplate;
import com.digitalpersona.onetouch.capture.DPFPCapture;
import com.digitalpersona.onetouch.capture.event.DPFPDataAdapter;
import com.digitalpersona.onetouch.capture.event.DPFPDataEvent;
import com.digitalpersona.onetouch.capture.event.DPFPErrorAdapter;
import com.digitalpersona.onetouch.capture.event.DPFPErrorEvent;
import com.digitalpersona.onetouch.capture.event.DPFPReaderStatusAdapter;
import com.digitalpersona.onetouch.capture.event.DPFPReaderStatusEvent;
import com.digitalpersona.onetouch.capture.event.DPFPReaderStatusListener;
import com.digitalpersona.onetouch.capture.event.DPFPSensorAdapter;
import com.digitalpersona.onetouch.capture.event.DPFPSensorEvent;
import com.digitalpersona.onetouch.processing.DPFPEnrollment;
import com.digitalpersona.onetouch.processing.DPFPFeatureExtraction;
import com.digitalpersona.onetouch.processing.DPFPImageQualityException;
import com.digitalpersona.onetouch.verification.DPFPVerification;
import com.digitalpersona.onetouch.verification.DPFPVerificationResult;
import java.awt.Image;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class Lec extends javax.swing.JApplet {

 
    @Override
    public void init() {
        Iniciar();
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Lec.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Lec.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Lec.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Lec.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        
        //</editor-fold>

        /* Create and display the applet */
        try {
            java.awt.EventQueue.invokeAndWait(new Runnable() {
                public void run() {
                    initComponents();
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

          //Variables que ayudan a capturar, enrolar y verificar
    private DPFPCapture Lector = DPFPGlobal.getCaptureFactory().createCapture();
    private DPFPEnrollment Reclutador = DPFPGlobal.getEnrollmentFactory().createEnrollment();
    private DPFPVerification Verificador = DPFPGlobal.getVerificationFactory().createVerification();
    private DPFPTemplate template;
    public static String TEMPLATE_PROPERTY = "template";
    /////////////
    public DPFPFeatureSet featureinscripcion;
    public DPFPFeatureSet featureverificacion;
    
    
    protected void Iniciar(){
        Lector.addDataListener(new DPFPDataAdapter(){
            public void dataAcquired(final DPFPDataEvent e){
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        EnviarTexto("Huella Capturada");
                        procesarCaptura(e.getSample());
                    }
                });
            }
        });
        
        Lector.addReaderStatusListener(new DPFPReaderStatusAdapter() {
            @Override
            public void readerConnected(DPFPReaderStatusEvent dpfprs) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        EnviarTexto("Sensor de Huella Activado o conectado");
                    }
                });
            }

            @Override
            public void readerDisconnected(DPFPReaderStatusEvent dpfprs) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        EnviarTexto("Sensor de Huella esta desactivado o no conectado");
                    }
                });
            }
        });
        
        Lector.addSensorListener(new DPFPSensorAdapter(){
            public void fingerTouched(final DPFPSensorEvent e){
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        EnviarTexto("El dedo ha sido colocado sobre el lector de huella");
                    }
                });
            }
            
            public void fingerGone(final DPFPSensorAdapter e){
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        EnviarTexto("El dedo ha sido quitado del lector de huella");
                    }
                });
            }
        });
        
        Lector.addErrorListener(new DPFPErrorAdapter(){
            public void errorReader(final DPFPErrorEvent e){
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        EnviarTexto("Error: "+e.getError());
                    }
                });
            }
        });
    }
    
    public DPFPFeatureSet extraerCaracteristicas(DPFPSample sample,DPFPDataPurpose purpose){
        DPFPFeatureExtraction extractor=DPFPGlobal.getFeatureExtractionFactory().createFeatureExtraction();
        try {
            return extractor.createFeatureSet(sample, purpose);
        } catch (DPFPImageQualityException e) {
            return null;
        }
    }
    
    public void procesarCaptura(DPFPSample sample){
        featureinscripcion = extraerCaracteristicas(sample, DPFPDataPurpose.DATA_PURPOSE_ENROLLMENT);
        featureverificacion = extraerCaracteristicas(sample, DPFPDataPurpose.DATA_PURPOSE_VERIFICATION);
        if(featureinscripcion!=null){
            try {
                System.out.println("Las caracteristicas de la huella han sido creadas");
                Reclutador.addFeatures(featureinscripcion);
                Image image = CrearImagenHuella(sample);
                DibujarHuella(image);
                btnVerificar.setEnabled(true);
                btnIdentificar.setEnabled(true);
            } catch (DPFPImageQualityException ex) {
                System.err.println("Error: "+ex.getMessage());
            }finally{
                EstadoHuellas();    
                switch(Reclutador.getTemplateStatus()){
                    case TEMPLATE_STATUS_READY:
                        stop();
                        setTemplate(Reclutador.getTemplate());
                        EnviarTexto("La plantilla de la huella ha sido creada, ya puede verificarla o identificarla");
                        btnIdentificar.setEnabled(false);
                        btnVerificar.setEnabled(false);
                        btnGuardar.setEnabled(true);
                        btnGuardar.grabFocus();
                        break;
                    case TEMPLATE_STATUS_FAILED:
                        Reclutador.clear();
                        stop();
                        EstadoHuellas();
                        setTemplate(null);
                        JOptionPane.showMessageDialog(Lec.this, "Error en el codigo perro ", "Error al Enrolar", JOptionPane.ERROR_MESSAGE);
                        start();
                        break;
                }
            }
        }
    }   
    public Image CrearImagenHuella(DPFPSample sample){
        return DPFPGlobal.getSampleConversionFactory().createImage(sample);
        
    }
    
    public void DibujarHuella(Image image){
        lblImagenHuella.setIcon(new ImageIcon(
                image.getScaledInstance(lblImagenHuella.getWidth(), lblImagenHuella.getHeight(), image.SCALE_DEFAULT)
        ));
        repaint();
    }
    
    public void EstadoHuellas(){
        EnviarTexto("Muestra de Huellas necesarias para Guardar: " + Reclutador.getFeaturesNeeded());
    }
    
    public void EnviarTexto(String string){
        txtArea.append(string + "\n");
    }
    
    public void start(){
        Lector.startCapture();
        EnviarTexto("Utilizando lector de huella");
    }
    
    public void stop(){
        Lector.stopCapture();
        EnviarTexto("No se está usando el lector de huella");
    }
    
    public DPFPTemplate getTemplate(){
        return template;
    }
    
    public void setTemplate(DPFPTemplate template){
        DPFPTemplate old = this.template;
        this.template = template;
        firePropertyChange(TEMPLATE_PROPERTY, old, template);
    }
    
    ConexMysql cn = new ConexMysql();
    public void guardarHuella()throws SQLException{
    ByteArrayInputStream datosHuella = new ByteArrayInputStream(template.serialize());
    Integer tamañoHuella=template.serialize().length;
     //Nombre de la persona que corresponde la huella
     String dpi=JOptionPane.showInputDialog("DPI: ");
     String nombre=JOptionPane.showInputDialog("Nombre: ");
     try{
         Connection c=cn.Conexion();
         PreparedStatement guardarStmt = c.prepareStatement("INSERT INTO huella (Dpi,huenombre,huehuella) VALUES(?,?,?)");
         //Puedo poner dpi
         guardarStmt.setString(1,dpi);
         guardarStmt.setString(2,nombre);
         guardarStmt.setBinaryStream(3, datosHuella, tamañoHuella);
         //Ejecutar sentencia
         guardarStmt.execute();
         guardarStmt.close();
         JOptionPane.showMessageDialog(null, "Se guardo con exito ");
         cn.desconectar();
         btnGuardar.setEnabled(false);
         btnVerificar.grabFocus();
     }catch(SQLException ex)
     {
         JOptionPane.showMessageDialog(null, "No se guardo, busca tu error!!");
     }finally{cn.desconectar(); 
         
     }
        
    }
    
    
    public void verificarHuella(String nom)
    {
        //Verifica Huella actual contra otra en la base de datos
    
       try{
           Connection c=cn.Conexion();
        PreparedStatement verificarStmt =c.prepareStatement("SELECT huehuella FROM huella where huenombre=?");
        verificarStmt.setString(1, nom);
        ResultSet rs=verificarStmt.executeQuery();
        
        if(rs.next())
        {
            byte templateBuffer[]=rs.getBytes("huehuella");
            DPFPTemplate referenceTemplate = DPFPGlobal.getTemplateFactory().createTemplate(templateBuffer);
            setTemplate(referenceTemplate);
            DPFPVerificationResult result= Verificador.verify(featureverificacion, getTemplate());
            if (result.isVerified())
            
                JOptionPane.showMessageDialog(null, "La huella coincide con la de "+nom,"Verificacion de huella",JOptionPane.INFORMATION_MESSAGE);
            else
                JOptionPane.showMessageDialog(null, "No corresponde con la huella de "+nom,"Verificacion de huella",JOptionPane.ERROR_MESSAGE);
            
            
        }else
        {
            JOptionPane.showMessageDialog(null, "No existe registro para la huella "+nom,"Verificacion de huella",JOptionPane.ERROR_MESSAGE);
        }
        
       }catch(SQLException e)
       {
           JOptionPane.showMessageDialog(null, "Error al verificar la huella");
       }finally
       {
           cn.desconectar();
       }       
        
    }
    
    
    public void identificarHuella() throws IOException 
    {
        try
        {
            Connection c=cn.Conexion();
            PreparedStatement identificarStmt=c.prepareStatement("SELECT huenombre, huehuella FROM huella");
            ResultSet rs=identificarStmt.executeQuery();
            
            while(rs.next())
            {
                byte templateBuffer[]=rs.getBytes("huehuella");
                String nombre=rs.getString("huenombre");
                DPFPTemplate referenceTemplate=DPFPGlobal.getTemplateFactory().createTemplate(templateBuffer);
                setTemplate(referenceTemplate);
                DPFPVerificationResult result= Verificador.verify(featureverificacion,getTemplate());
                if(result.isVerified())
                {
                      JOptionPane.showMessageDialog(null, "La huella capturada es de "+nombre,"Verificacion de huella",JOptionPane.INFORMATION_MESSAGE);
                      return;   
                }
            }
            JOptionPane.showMessageDialog(null, "NO EXISTE NINGUN REGISTRO QUE COINCIDA CON HUELLA ","Verificacion de huella",JOptionPane.ERROR_MESSAGE);
            setTemplate(null);
        }catch(Exception e)
        {
            JOptionPane.showMessageDialog(null, "ERROR AL IDENTIFICAR HUELLA ","Verificacion de huella",JOptionPane.ERROR_MESSAGE);
        }finally{
            cn.desconectar();
        }
        
    }
    
    
    
    
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        btnVerificar = new javax.swing.JButton();
        btnGuardar = new javax.swing.JButton();
        btnIdentificar = new javax.swing.JButton();
        btnSalir = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtArea = new javax.swing.JTextArea();
        lblImagenHuella = new javax.swing.JLabel();

        btnVerificar.setText("Verificar");
        btnVerificar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnVerificarActionPerformed(evt);
            }
        });

        btnGuardar.setText("Guardar");
        btnGuardar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGuardarActionPerformed(evt);
            }
        });

        btnIdentificar.setText("Identificar");
        btnIdentificar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnIdentificarActionPerformed(evt);
            }
        });

        btnSalir.setText("Salir");
        btnSalir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSalirActionPerformed(evt);
            }
        });

        txtArea.setColumns(20);
        txtArea.setRows(5);
        jScrollPane1.setViewportView(txtArea);

        lblImagenHuella.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        lblImagenHuella.setIconTextGap(10);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(btnVerificar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnIdentificar, javax.swing.GroupLayout.DEFAULT_SIZE, 99, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnSalir, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnGuardar, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jScrollPane1)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 25, Short.MAX_VALUE)
                        .addComponent(lblImagenHuella, javax.swing.GroupLayout.PREFERRED_SIZE, 304, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(21, 21, 21)))
                .addGap(22, 22, 22))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblImagenHuella, javax.swing.GroupLayout.PREFERRED_SIZE, 202, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnVerificar)
                    .addComponent(btnGuardar))
                .addGap(40, 40, 40)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnIdentificar)
                    .addComponent(btnSalir))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnVerificarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnVerificarActionPerformed
       String nombre = JOptionPane.showInputDialog("Nombre a verificar: ");
    verificarHuella(nombre);
    Reclutador.clear();
    }//GEN-LAST:event_btnVerificarActionPerformed

    private void btnGuardarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGuardarActionPerformed
       
        try{
           guardarHuella();
           Reclutador.clear();
           lblImagenHuella.setIcon(null);
           start();
       }catch(Exception ex)
       {
           Logger.getLogger(Lec.class.getName()).log(Level.SEVERE,null,ex);
       }
        
        
    }//GEN-LAST:event_btnGuardarActionPerformed

    private void btnIdentificarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnIdentificarActionPerformed
        try{
            identificarHuella();
            Reclutador.clear();
            
            
        }catch(Exception ex)
        {
            Logger.getLogger(Lec.class.getName()).log(Level.SEVERE,null,ex);
        }
    }//GEN-LAST:event_btnIdentificarActionPerformed

    private void btnSalirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSalirActionPerformed
       System.exit(0);
    }//GEN-LAST:event_btnSalirActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnGuardar;
    private javax.swing.JButton btnIdentificar;
    private javax.swing.JButton btnSalir;
    private javax.swing.JButton btnVerificar;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblImagenHuella;
    private javax.swing.JTextArea txtArea;
    // End of variables declaration//GEN-END:variables
}
