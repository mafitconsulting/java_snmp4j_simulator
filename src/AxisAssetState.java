// Mark Fieldhouse
// Asset status checker

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintStream;

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.text.DocumentFilter;
import javax.swing.JTextArea;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import java.awt.BorderLayout;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;

/**
 *
 * @author Mark
 */
public class AxisAssetState {

   private JTextArea textArea;
   private JTextField ciField;
   private final PrintStream standardOut;
   
     
    public AxisAssetState()  {
         
        textArea = new JTextArea(30, 10);
        textArea.setEditable(false);
	textArea.setFont(new Font("monospaced", Font.PLAIN, 12));
        PrintStream printStream = new PrintStream(new CustomOutputStream(textArea));
        
		
        // keeps reference of standard output stream
        standardOut = System.out;
         
        // re-assigns standard output stream and error output stream
        System.setOut(printStream);
        System.setErr(printStream);
 
        //creates the GUI
		JFrame frame = new JFrame();
        frame.setVisible(true);
	    JMenuBar menubar = new JMenuBar();
        frame.setJMenuBar(menubar);

       // Add an JMenu
        JMenu file = new JMenu("File");
        menubar.add(file);
        JMenuItem exit = new JMenuItem("Exit");
        exit.addActionListener(new exitApp());
        file.add(exit);
	   
	
        JMenu help = new JMenu("Help");
        menubar.add(help);
        JMenuItem about = new JMenuItem("About");
		about.addActionListener(new helpTxt());
        help.add(about);

       //JTextField ciField = new JTextField();
       
        // The label and text field for entering the names for the CI's
        JLabel ciLabel = new JLabel("CI Name(s): ");
        final int FIELD_WIDTH = 15;
        ciField = new JTextField(FIELD_WIDTH);
        DocumentFilter dfilter = new UpcaseFilter();
	    ((AbstractDocument) ciField.getDocument()).setDocumentFilter(dfilter);
 
       // Buttons to trigger search and clear fields
       JButton buttonSearch = new JButton("Search");
	   JButton buttonClear = new JButton("Clear");
 
       // The panel that holds the input components
       JPanel northPanel = new JPanel();
       northPanel.add(ciLabel);
       northPanel.add(ciField);
       northPanel.add(buttonSearch);
	   northPanel.add(buttonClear);
        
	   JScrollPane scrollPane = new JScrollPane(textArea);
	   frame.setTitle("CMDB CI Status Check");
       frame.setSize(420, 410);
       frame.setResizable(false); 
       frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
       frame.add(northPanel, BorderLayout.NORTH);
       frame.add(scrollPane);     
       
         
		
         
         
        // adds event handler for button Search
        buttonSearch.addActionListener((ActionEvent evt) -> {
            if(ciField.getText().equals("")) {
                JOptionPane.showMessageDialog(null,"Please enter at least one CI","Error",JOptionPane.ERROR_MESSAGE);
            }
            else {
                try {
                    textArea.getDocument().remove(0,
                            textArea.getDocument().getLength());
                    
                } catch (BadLocationException ex) {
                }
                
                printOutput();
            }
        });
         
        // adds event handler for button Clear
        buttonClear.addActionListener((ActionEvent evt) -> {
            // clears the text area
            try {
                textArea.getDocument().remove(0,
                        textArea.getDocument().getLength());
                ciField.setText("");
            } catch (BadLocationException ex) {
            }
        });
         
      
    }
     
    

      
    static class exitApp implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            System.exit(0);
        }
    }
    
    class UpcaseFilter extends DocumentFilter 
    {
    	// Servers in CI need to be converted to UPPERCASE, we need a document
        // filter for this. 
    	 
    	
        @Override
        public void insertString(DocumentFilter.FilterBypass fb, int offset,
        	      String text, AttributeSet attr) throws BadLocationException {
        	    fb.insertString(offset, text.toUpperCase(), attr);
        	  }


        @Override
        public void replace(DocumentFilter.FilterBypass fb, int offset, int length,
        	      String text, AttributeSet attr) throws BadLocationException {
        	    fb.replace(offset, length, text.toUpperCase(), attr);
        	  }
    }
         
	
	static class helpTxt implements ActionListener
	{
            @Override
	    public void actionPerformed(ActionEvent e)
		{
		    JOptionPane.showMessageDialog(null, "CMDB CI Status Check\n\nAllows you to search for the status of configuration item (ci)\nfrom T_ASSETS_AXIS table within the Impact DB populated from AXIS.\nSupports single entry CI or comma separated CI lists\nHW_STATUS is Hardware Layer Status in AXIS DB\nV_STATUS is virtual layer status\n\nExample Output:\n\nSearching for Axis CI .......Please be patient\n---------------------------------------------------------------------------------\nHOSTNAME                    HW_STATUS      V_STATUS\n---------------------------------------------------------------------------------\nAPPTTLTSBDC1019     Disposed            Disposed\n\nSearch Completed\n\n\n", "About", JOptionPane.INFORMATION_MESSAGE);
		}
	}

    private void printOutput() {
        Thread thread;
       thread = new Thread(() -> {
           try {
               System.out.println("");
               System.out.println("Searching for CMDB CI .......Please be patient");
               System.out.println("-----------------------------------------------------------");
               System.out.printf("%-20s%-20s%s\n","HOSTNAME","HW_STATUS","V_STATUS");
               System.out.println("-----------------------------------------------------------");
               OracleConnect(); }
           catch (SQLException e) {
           }
       });
        thread.start();
    }
	
	public void OracleConnect() throws SQLException{

		try {

		    Class.forName("oracle.jdbc.driver.OracleDriver");
		} 
		
		catch (ClassNotFoundException e) {

			System.out.println("Cannot locate JDBC Driver?");
			return;
		}

		Connection connection = null;

		try {

			connection = DriverManager.getConnection(
					"jdbc:oracle:thin:@<server>:1521:DB", "user","password");
			
		} 
		
		catch (SQLException e) {

			System.out.println("Connection Failed! Check output console");
			return;
		}

		if (connection != null) {
			viewAxisTable(connection);
		}
		
		else {
			System.out.println("Failed to make connection!");
		}
		   
	}
	
	
	public void viewAxisTable(Connection con) throws SQLException{
		
		String server = ciField.getText();
		String query = null;
		Statement stmt = con.createStatement();
		try {
            if(server.contains(",")) {
                String hosts[] = server.split("\\,");
                for (String host : hosts) {
                    query = "select unique ci_name, ci_hardware, ci_virtual from t_assets_axis where ci_name = '" + host + "'";
                    ResultSet rs = stmt.executeQuery(query);
                    while (rs.next()) {
                        String hardware = rs.getString("ci_hardware");
                        String name = rs.getString("ci_name");
                        String virtual = rs.getString("ci_virtual");
                        if (hardware == null) {
                            hardware = "N/A";
                        }
                        else
                            if (virtual == null) {
                                virtual = "N/A";
                            }
                        System.out.printf("%-20s%-20s%s\n",name,hardware,virtual);
                    }
                }
            }

		    else {
                query = "select unique ci_name, ci_hardware, ci_virtual from t_assets_axis where ci_name = '"+ server +"'" ;
                ResultSet rs = stmt.executeQuery(query);

                while (rs.next()) {
                	String hardware = rs.getString("ci_hardware");
                    String name = rs.getString("ci_name");
                    String virtual = rs.getString("ci_virtual");
                    if (hardware == null) {
                    	hardware = "N/A";
                    }
                    else
                    if (virtual == null) {
                    	virtual = "N/A";
                    }
                    PrintStream printf = System.out.printf("%-20s%-20s%s\n",name,hardware,virtual);
                }
            }   
        }
	    
		catch (SQLException e) {
            }
	
	    finally {
            try { 
		        stmt.close(); 
		    }
		   
		    catch(Exception e) {    
				System.out.println("Cannot close database connection");
		    }
        }
		 System.out.println("");
        System.out.println("Search Complete.");		   
    }
    public static void main(String[] args) throws SQLException {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                AxisAssetState axisAssetState = new AxisAssetState();
            }
        });
    }
}