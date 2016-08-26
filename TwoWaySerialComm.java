package twoWaySerialComm;

//24 Aug 2016 v1.0  jcf

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Window.Type;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.JTextArea;


   //This version of the TwoWaySerialComm makes use of the 
   //SerialPortEventListener to avoid polling.


public class TwoWaySerialComm
{		
		
	static String userInput = null;
	static RoofControlPanel roofControlPanel = new RoofControlPanel ();	
	static JTextField textSecurityStatus;
	static JTextField textWeatherStatus;
	static JTextField textRoofOpenStatus;
	static JTextField textRoofClosedStatus;
	static JTextField textMountParkedStatus;
	static JTextField textBldgPowerInStatus;
	static JTextField textRoofPowerInStatus;
	static JTextField textMountPowerInStatus;
	static JTextArea textAreaStatus;
	static String line;
	static String oldLine = null;
	//static int analogInputReadingA0;
	//static int roofPositionStatus;
	//static int conditionIndicators;
	//static String conditionOne;
	//static String conditionTwo;
	//static String conditionThree;
	//static JProgressBar progressBar;
	//static Canvas canvasSecurityStatus;

	
		
    public TwoWaySerialComm()
    {
        super();
    }  
    
    //RxTx Serial Connection Protocol    
    
    void connect ( String portName ) throws Exception
    {
        CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
        if ( portIdentifier.isCurrentlyOwned() )
        {
            System.out.println("Error: Port is currently in use");
        }
        else
        {
            CommPort commPort = portIdentifier.open(this.getClass().getName(),2000);
            
            if ( commPort instanceof SerialPort )
            {
                SerialPort serialPort = (SerialPort) commPort;
                serialPort.setSerialPortParams(57600,SerialPort.DATABITS_8,SerialPort.STOPBITS_1,SerialPort.PARITY_NONE);
                
                OutputStream out = serialPort.getOutputStream();
                InputStream in = serialPort.getInputStream();
                
                               
                (new Thread(new SerialWriter(out))).start();
                
                serialPort.addEventListener(new SerialReader(in));
                serialPort.notifyOnDataAvailable(true);              
                                
            }
            else
            {
                System.out.println("Error: Only serial ports are handled by this example.");
            }
        }     
    }
    
    //Serial Input Reader
    
    public static class SerialReader implements SerialPortEventListener 
    {  	        	              	 
        InputStream in;        
                
        //private byte[] buffer = new byte[1024];
        
        public SerialReader ( InputStream in )
        {
            this.in = in;
        }
        
        //Serial Read Input String on event Data Available
        
        @Override
    	public void serialEvent(SerialPortEvent e) {
    		try {
    		   	
    			InputStreamReader stream = new InputStreamReader(in);   //get string from input byte stream
    	        BufferedReader reader = new BufferedReader(stream);		//character stream to buffer
    			line = reader.readLine(); 
    			
    			line = line.trim();									//gets rid of any whitespace
    			
    			System.out.println("String length = " + line.length());
    			
    			if (line.equals(oldLine) != true) {					//only resets textArea if new string is different from prior

    				textAreaStatus.setText(line);
    				
    				oldLine = line;
    				
    				if(line.startsWith("INFO: securityOK")) {	//sends String to be split only if returning Status
    															//only the Status String can start with "INFO: securityOK"
    					visualization(line);					//there are other possible strings for "INFO:"
    				
    				} 
    				
    			}
    			
    		}  catch (IOException ex) {
    			ex.printStackTrace();
    		}   				
    		    
     	}
    	
    	public void visualization(String line){
    		
    		//Split the Input Status String
    		
    		String[] values = line.split(" ");    		
    		
    		String security = values[1];
    		String weather = values[2];
    		String roofOpen = values[3];
    		String roofClosed = values[4];
    		String mountParked = values[5];
    		String bldgPowerIn = values[6];
    		String roofPowerIn = values[7];
    		String mountPowerIn = values[8];
    				
    		System.out.println("Security = " + security);
    		System.out.println("Weather = " + weather);
    		System.out.println("RoofOpen = " + roofOpen);
    		System.out.println("RoofClosed = " + roofClosed);
    		System.out.println("Mount Parked = " + mountParked);
    		System.out.println("Building Power = " + bldgPowerIn);
    		System.out.println("Roof Power = " + roofPowerIn);
    		System.out.println("Mount Power = " + mountPowerIn);
    		
    		//Status Indicator Logic
    		
    		if (security.endsWith("1")) {
        		textSecurityStatus.setText("GOOD");
        		textSecurityStatus.setBackground(Color.GREEN);
    		} else {
        		textSecurityStatus.setText("BAD");
        		textSecurityStatus.setBackground(Color.RED);
    		}
    		
    		if (weather.endsWith("1")) {
        		textWeatherStatus.setText("GOOD");
        		textWeatherStatus.setBackground(Color.GREEN);
    		} else {
        		textWeatherStatus.setText("BAD");
        		textWeatherStatus.setBackground(Color.RED);
    		}
    		
    		if (roofOpen.endsWith("1")) {
    			textRoofOpenStatus.setText("YES");
    			textRoofOpenStatus.setBackground(Color.GREEN);
    		} else {
    			textRoofOpenStatus.setText("NO");
    			textRoofOpenStatus.setBackground(Color.RED);
    		}
    		
    		if (roofClosed.endsWith("1")) {
    			textRoofClosedStatus.setText("YES");
    			textRoofClosedStatus.setBackground(Color.GREEN);
    		} else {
    			textRoofClosedStatus.setText("NO");
    			textRoofClosedStatus.setBackground(Color.RED);
    		}
    		
    		if (mountParked.endsWith("1")) {
    			textMountParkedStatus.setText("PARKED");
    			textMountParkedStatus.setBackground(Color.GREEN);
    		} else {
    			textMountParkedStatus.setText("UNPARKED");
    			textMountParkedStatus.setBackground(Color.RED);
    		}
    		
    		if (bldgPowerIn.endsWith("1")) {
    			textBldgPowerInStatus.setText("ON");
    			textBldgPowerInStatus.setBackground(Color.GREEN);
    		} else {
    			textBldgPowerInStatus.setText("OFF");
    			textBldgPowerInStatus.setBackground(Color.RED);
    		}
    		
    		if (roofPowerIn.endsWith("1")) {
    			textRoofPowerInStatus.setText("ON");
    			textRoofPowerInStatus.setBackground(Color.GREEN);
    		} else {
    			textRoofPowerInStatus.setText("OFF");
    			textRoofPowerInStatus.setBackground(Color.RED);
    		}
    		
    		if (mountPowerIn.endsWith("1")) {
    			textMountPowerInStatus.setText("ON");
    			textMountPowerInStatus.setBackground(Color.GREEN);
    		} else {
    			textMountPowerInStatus.setText("OFF");
    			textMountPowerInStatus.setBackground(Color.RED);
    		}
    		
    	}

    }  //close class SerialReader
    
    //Serial Writer    
     
    public static class SerialWriter implements Runnable 
    {
        static OutputStream out;
        
        public SerialWriter ( OutputStream out )
        {
            this.out = out;
        }
        
        public void run ()
        {
        		
        	    while (userInput != null) {                 	
            	             
                try {
                	
        			out.write(userInput.getBytes());
        			
        		} catch (IOException e1) {
        			e1.printStackTrace();
        			System.exit(-1);
            }
               
        	}                                 
                      
        }       
                
    }  //close class SerialWriter (serial writer thread)
    
    public static class RoofControlPanel {

    	private JFrame frmRfoRoofControl;    	
    	
    	//The GUI is built in class RoofControlPanel, then copied here.  RoofControlPanel is otherwise inactive.
    	 
    	public void roofControlPanel () {
    		
    		RoofControlPanel window = new RoofControlPanel();
    		window.frmRfoRoofControl.setVisible(true);
    				
    	}
    	
    	 //Create the application.
    	
    	public RoofControlPanel() {
    		initialize();
    	}
    	
    	 //Initialize the contents of the frame.
    	 
    	private void initialize() {
    		frmRfoRoofControl = new JFrame();
    		frmRfoRoofControl.getContentPane().setBackground(new Color(50, 180, 190));
    		frmRfoRoofControl.setResizable(false);
    		frmRfoRoofControl.setTitle("RFO EAST WING ROOF CONTROL");
    		frmRfoRoofControl.setBounds(100, 100, 450, 450);
    		frmRfoRoofControl.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    		frmRfoRoofControl.getContentPane().setLayout(null);
    		
    		//Command Buttons & Action Event Serial Command Writing-------------
    		//all commands must start with :: and end with newline \n
    		
    		JButton btnRoofOpen = new JButton("Roof OPEN");
    		btnRoofOpen.addActionListener(new ActionListener() {
    			public void actionPerformed(ActionEvent e) {
    				TwoWaySerialComm.userInput = "::Open\n";
    				try{
    				SerialWriter.out.write(TwoWaySerialComm.userInput.getBytes());    				
    				}  catch (IOException ex) {}
    				    				
    				System.out.println("I clicked Roof OPEN");    				
    			}
    		});
    		btnRoofOpen.setBounds(173, 187, 110, 23);
    		frmRfoRoofControl.getContentPane().add(btnRoofOpen);
    		
    		JButton btnRoofClose = new JButton("Roof CLOSE");
    		btnRoofClose.addActionListener(new ActionListener() {
    			public void actionPerformed(ActionEvent e) {
    				TwoWaySerialComm.userInput = "::Close\n";
    				try{
    				SerialWriter.out.write(TwoWaySerialComm.userInput.getBytes());
    				}  catch (IOException ex) {}
    				
    				System.out.println("I clicked Roof CLOSE");
    			}
    		});
    		btnRoofClose.setBounds(303, 187, 110, 23);
    		frmRfoRoofControl.getContentPane().add(btnRoofClose);
    		
    		JButton btnEmergSTOP = new JButton("Emerg STOP");
    		btnEmergSTOP.addActionListener(new ActionListener() {
    			public void actionPerformed(ActionEvent e) {
    				TwoWaySerialComm.userInput = "::Stop\n";
    				try{
    				SerialWriter.out.write(TwoWaySerialComm.userInput.getBytes());
    				}  catch (IOException ex) {}
    				
    				System.out.println("I clicked Emerg STOP");
    			}
    		});
    		btnEmergSTOP.setBounds(173, 221, 110, 23);
    		frmRfoRoofControl.getContentPane().add(btnEmergSTOP);   		
    		
    		JButton btnStatus = new JButton("STATUS");
    		btnStatus.addActionListener(new ActionListener() {
    			public void actionPerformed(ActionEvent e) {
    				TwoWaySerialComm.userInput = "::Status\n";
    				try{
    				SerialWriter.out.write(TwoWaySerialComm.userInput.getBytes());
    				}  catch (IOException ex) {}
    				
    				System.out.println("I clicked Status");
    			}
    		});
    		btnStatus.setBounds(303, 221, 110, 23);
    		frmRfoRoofControl.getContentPane().add(btnStatus);
    		
    		JButton btnRoofPwrOn = new JButton("Roof Pwr ON");
    		btnRoofPwrOn.addActionListener(new ActionListener() {
    			public void actionPerformed(ActionEvent e) {
    				TwoWaySerialComm.userInput = "::RPon\n";
    				try{
    				SerialWriter.out.write(TwoWaySerialComm.userInput.getBytes());
    				}  catch (IOException ex) {}
    				
    				System.out.println("I clicked Roof Pwr ON");
    			}
    		});
    		btnRoofPwrOn.setBounds(173, 255, 110, 23);
    		frmRfoRoofControl.getContentPane().add(btnRoofPwrOn);
    		
    		JButton btnRoofPwrOff = new JButton("Roof Pwr OFF");
    		btnRoofPwrOff.addActionListener(new ActionListener() {
    			public void actionPerformed(ActionEvent e) {
    				TwoWaySerialComm.userInput = "::RPoff\n";
    				try{
    				SerialWriter.out.write(TwoWaySerialComm.userInput.getBytes());
    				}  catch (IOException ex) {}
    				
    				System.out.println("I clicked Roof Pwr OFF");
    			}
    		});
    		btnRoofPwrOff.setBounds(303, 255, 110, 23);
    		frmRfoRoofControl.getContentPane().add(btnRoofPwrOff);
    		
    		JButton btnMountPwrON = new JButton("Mount Pwr ON");
    		btnMountPwrON.setFont(new Font("Tahoma", Font.PLAIN, 10));
    		btnMountPwrON.addActionListener(new ActionListener() {
    			public void actionPerformed(ActionEvent e) {
    				TwoWaySerialComm.userInput = "::MPon\n";
    				try{
    				SerialWriter.out.write(TwoWaySerialComm.userInput.getBytes());
    				}  catch (IOException ex) {}
    				
    				System.out.println("I clicked Mount Pwr ON");
    			}
    		});
    		btnMountPwrON.setBounds(173, 289, 110, 23);
    		frmRfoRoofControl.getContentPane().add(btnMountPwrON);
    		
    		JButton btnMountPwrOff = new JButton("Mount Pwr OFF");
    		btnMountPwrOff.addActionListener(new ActionListener() {
    			public void actionPerformed(ActionEvent e) {
    				TwoWaySerialComm.userInput = "::MPoff\n";
    				try{
    				SerialWriter.out.write(TwoWaySerialComm.userInput.getBytes());
    				}  catch (IOException ex) {}
    				
    				System.out.println("I clicked Mount Pwr OFF");
    			}
    		});
    		btnMountPwrOff.setFont(new Font("Tahoma", Font.PLAIN, 10));
    		btnMountPwrOff.setBounds(303, 289, 110, 23);
    		frmRfoRoofControl.getContentPane().add(btnMountPwrOff);
    		
    		JButton btnOverrideOn = new JButton("Override ON");
    		btnOverrideOn.addActionListener(new ActionListener() {
    			public void actionPerformed(ActionEvent e) {
    				TwoWaySerialComm.userInput = "::OverrideOn\n";
    				try{
    				SerialWriter.out.write(TwoWaySerialComm.userInput.getBytes());
    				}  catch (IOException ex) {}
    				
    				System.out.println("I clicked Override ON");
    			}
    		});
    		btnOverrideOn.setBounds(173, 323, 110, 23);
    		frmRfoRoofControl.getContentPane().add(btnOverrideOn);
    		
    		JButton btnOverrideOff = new JButton("Override OFF");
    		btnOverrideOff.addActionListener(new ActionListener() {
    			public void actionPerformed(ActionEvent e) {
    				TwoWaySerialComm.userInput = "::OverrideOff\n";
    				try{
    				SerialWriter.out.write(TwoWaySerialComm.userInput.getBytes());
    				}  catch (IOException ex) {}
    				
    				System.out.println("I clicked Override OFF");
    			}
    		});
    		btnOverrideOff.setBounds(303, 323, 110, 23);
    		frmRfoRoofControl.getContentPane().add(btnOverrideOff);
    		
    		JButton btnDebugOn = new JButton("Debug ON");
    		btnDebugOn.addActionListener(new ActionListener() {
    			public void actionPerformed(ActionEvent e) {
    				TwoWaySerialComm.userInput = "::DebugOn\n";
    				try{
    				SerialWriter.out.write(TwoWaySerialComm.userInput.getBytes());
    				}  catch (IOException ex) {}
    				
    				System.out.println("I clicked Debug ON");
    			}
    		});
    		btnDebugOn.setBounds(173, 357, 110, 23);
    		frmRfoRoofControl.getContentPane().add(btnDebugOn);
    		
    		JButton btnDebugOff = new JButton("Debug OFF");
    		btnDebugOff.addActionListener(new ActionListener() {
    			public void actionPerformed(ActionEvent e) {
    				TwoWaySerialComm.userInput = "::DebugOff\n";
    				try{
    				SerialWriter.out.write(TwoWaySerialComm.userInput.getBytes());
    				}  catch (IOException ex) {}
    				
    				System.out.println("I clicked Debug OFF");
    			}
    		});
    		btnDebugOff.setBounds(303, 357, 110, 23);
    		frmRfoRoofControl.getContentPane().add(btnDebugOff);
    		
    		//CCD System Status/Message JTextArea------------------------------
    		
    		JLabel lblStatusMessage = new JLabel("CCD System Status / Message");
    		lblStatusMessage.setFont(new Font("Tahoma", Font.BOLD, 11));
    		lblStatusMessage.setBounds(69, 35, 187, 14);
    		frmRfoRoofControl.getContentPane().add(lblStatusMessage); 
    	   		    		
    		textAreaStatus = new JTextArea();
    		textAreaStatus.setLineWrap(true);
    		textAreaStatus.setWrapStyleWord(true);
    		textAreaStatus.setEditable(false);
    		textAreaStatus.setBackground(Color.YELLOW);
    		textAreaStatus.setBounds(69, 60, 309, 60);
    		frmRfoRoofControl.getContentPane().add(textAreaStatus);  		
  		
    		//Status Indicators -----------------------------------------------
    		
    		JLabel lblSecurityStatus = new JLabel("Security");
    		lblSecurityStatus.setHorizontalTextPosition(SwingConstants.RIGHT);
    		lblSecurityStatus.setHorizontalAlignment(SwingConstants.RIGHT);
    		lblSecurityStatus.setBounds(8, 164, 70, 14);
    		frmRfoRoofControl.getContentPane().add(lblSecurityStatus);
    		
    		textSecurityStatus = new JTextField();
    		textSecurityStatus.setHorizontalAlignment(SwingConstants.CENTER);
    		textSecurityStatus.setFont(new Font("Tahoma", Font.BOLD, 11));
    		textSecurityStatus.setColumns(4);    		
    		textSecurityStatus.setBounds(85, 160, 70, 20);
    		frmRfoRoofControl.getContentPane().add(textSecurityStatus);
    		
    		
    		JLabel lblWeatherStatus = new JLabel("Weather");
    		lblWeatherStatus.setHorizontalTextPosition(SwingConstants.RIGHT);
    		lblWeatherStatus.setHorizontalAlignment(SwingConstants.RIGHT);
    		lblWeatherStatus.setBounds(8, 195, 70, 14);
    		frmRfoRoofControl.getContentPane().add(lblWeatherStatus);
    		
    		textWeatherStatus = new JTextField();
    		textWeatherStatus.setFont(new Font("Tahoma", Font.BOLD, 11));
    		textWeatherStatus.setHorizontalAlignment(SwingConstants.CENTER);
    		textWeatherStatus.setColumns(4);
    		textWeatherStatus.setBounds(85, 191, 70, 20);
    		frmRfoRoofControl.getContentPane().add(textWeatherStatus);
    		    		
    		JLabel lblRoofOpenStatus = new JLabel("Roof Open");
    		lblRoofOpenStatus.setHorizontalTextPosition(SwingConstants.RIGHT);
    		lblRoofOpenStatus.setHorizontalAlignment(SwingConstants.RIGHT);
    		lblRoofOpenStatus.setBounds(8, 226, 70, 14);
    		frmRfoRoofControl.getContentPane().add(lblRoofOpenStatus);
    		
    		textRoofOpenStatus = new JTextField();
    		textRoofOpenStatus.setHorizontalAlignment(SwingConstants.CENTER);
    		textRoofOpenStatus.setFont(new Font("Tahoma", Font.BOLD, 11));
    		textRoofOpenStatus.setColumns(4);    		
    		textRoofOpenStatus.setBounds(85, 222, 70, 20);
    		frmRfoRoofControl.getContentPane().add(textRoofOpenStatus);
    		
    		JLabel lblRoofClosedStatus = new JLabel("Roof Closed");
    		lblRoofClosedStatus.setHorizontalTextPosition(SwingConstants.RIGHT);
    		lblRoofClosedStatus.setHorizontalAlignment(SwingConstants.RIGHT);
    		lblRoofClosedStatus.setBounds(8, 255, 70, 14);
    		frmRfoRoofControl.getContentPane().add(lblRoofClosedStatus);
    		
    		textRoofClosedStatus = new JTextField();
    		textRoofClosedStatus.setHorizontalAlignment(SwingConstants.CENTER);
    		textRoofClosedStatus.setFont(new Font("Tahoma", Font.BOLD, 11));
    		textRoofClosedStatus.setColumns(4);
    		textRoofClosedStatus.setBounds(85, 251, 70, 20);
    		frmRfoRoofControl.getContentPane().add(textRoofClosedStatus);
    		
    		JLabel lblMountParkedStatus = new JLabel("Mount Park");
    		lblMountParkedStatus.setHorizontalTextPosition(SwingConstants.RIGHT);
    		lblMountParkedStatus.setHorizontalAlignment(SwingConstants.RIGHT);
    		lblMountParkedStatus.setBounds(8, 284, 70, 14);
    		frmRfoRoofControl.getContentPane().add(lblMountParkedStatus);
    		
    		textMountParkedStatus = new JTextField();
    		textMountParkedStatus.setHorizontalAlignment(SwingConstants.CENTER);
    		textMountParkedStatus.setFont(new Font("Tahoma", Font.BOLD, 11));
    		textMountParkedStatus.setColumns(4);
    		textMountParkedStatus.setBounds(85, 280, 70, 20);
    		frmRfoRoofControl.getContentPane().add(textMountParkedStatus);
    		
    		JLabel lblBldgPowerInStatus = new JLabel("Bldg Pwr");
    		lblBldgPowerInStatus.setHorizontalTextPosition(SwingConstants.RIGHT);
    		lblBldgPowerInStatus.setHorizontalAlignment(SwingConstants.RIGHT);
    		lblBldgPowerInStatus.setBounds(8, 313, 70, 14);
    		frmRfoRoofControl.getContentPane().add(lblBldgPowerInStatus);
    		
    		textBldgPowerInStatus = new JTextField();
    		textBldgPowerInStatus.setHorizontalAlignment(SwingConstants.CENTER);
    		textBldgPowerInStatus.setFont(new Font("Tahoma", Font.BOLD, 11));
    		textBldgPowerInStatus.setColumns(4);
    		textBldgPowerInStatus.setBounds(85, 309, 70, 20);
    		frmRfoRoofControl.getContentPane().add(textBldgPowerInStatus);
    		
    		JLabel lblRoofPowerInStatus = new JLabel("Roof Pwr");
    		lblRoofPowerInStatus.setHorizontalTextPosition(SwingConstants.RIGHT);
    		lblRoofPowerInStatus.setHorizontalAlignment(SwingConstants.RIGHT);
    		lblRoofPowerInStatus.setBounds(8, 342, 70, 14);
    		frmRfoRoofControl.getContentPane().add(lblRoofPowerInStatus);
    		
    		textRoofPowerInStatus = new JTextField();
    		textRoofPowerInStatus.setHorizontalAlignment(SwingConstants.CENTER);
    		textRoofPowerInStatus.setFont(new Font("Tahoma", Font.BOLD, 11));
    		textRoofPowerInStatus.setColumns(4);
    		textRoofPowerInStatus.setBounds(85, 338, 70, 20);
    		frmRfoRoofControl.getContentPane().add(textRoofPowerInStatus);
    		
    		JLabel lblMountPowerInStatus = new JLabel("Mount Pwr");
    		lblMountPowerInStatus.setHorizontalTextPosition(SwingConstants.RIGHT);
    		lblMountPowerInStatus.setHorizontalAlignment(SwingConstants.RIGHT);
    		lblMountPowerInStatus.setBounds(8, 371, 70, 14);
    		frmRfoRoofControl.getContentPane().add(lblMountPowerInStatus);
    		
    		textMountPowerInStatus = new JTextField();
    		textMountPowerInStatus.setHorizontalAlignment(SwingConstants.CENTER);
    		textMountPowerInStatus.setFont(new Font("Tahoma", Font.BOLD, 11));
    		textMountPowerInStatus.setColumns(4);
    		textMountPowerInStatus.setBounds(85, 367, 70, 20);
    		frmRfoRoofControl.getContentPane().add(textMountPowerInStatus);    		
    		    		
    	}
    }  // close class RoofControlPanel (GUI Builder)

        
    public static void main ( String[] args )
    {
        try
        {
            (new TwoWaySerialComm()).connect("COM4");	//make sure COM port number is correct!
        }
        catch ( Exception e )
        {
                       e.printStackTrace();
        }
       
        roofControlPanel.roofControlPanel();              
                
    }  //close main method
 
}  //close outer class TwoWaySerialComm
