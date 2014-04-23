package checkin;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.File;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

public class MainWindow extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				DataBase.init();
				
				try {
					MainWindow frame = new MainWindow();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public MainWindow() {
		final JList<String> list = new JList<String>();
		final JPanel checkin_tab = new JPanel();
		final JTextField ticket_id= new JTextField();
		
		final JLabel lblStatus = new JLabel("");
		final JLabel lblOptions = new JLabel("");
		final JLabel lblTicket = new JLabel("");
		
		final JToggleButton btnDisable = new JToggleButton("Disable");
		final JToggleButton btnUnused = new JToggleButton("Unused");
		final JToggleButton btnUsed = new JToggleButton("Used");
		
		ticket_id.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String ticketid = ticket_id.getText();
				DataBase.checkInTicket(ticketid, MainWindow.this);
				
				lblStatus.setText(DataBase.getTicketStatus());
				lblOptions.setText(DataBase.getTicketOptions());
				
				// clear field again
				ticket_id.setText("");
				ticket_id.requestFocusInWindow();
				
				// Update Info Fields
				lblTicket.setText(DataBase.getTicketID());
				lblStatus.setText(DataBase.getTicketStatus());
				lblStatus.setBackground(DataBase.getTicketStatusColor());
				lblStatus.setOpaque(true);
				lblOptions.setText(DataBase.getTicketOptions());
				
				// Update Buttons
				btnDisable.setSelected(DataBase.isInvalid());
				btnUnused.setSelected(DataBase.isUnused());
				btnUsed.setSelected(DataBase.isUsed());
			}
		});
		
		checkin_tab.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent arg0) {
				ticket_id.requestFocusInWindow();
			}
		});
		
		setTitle("Checkin App");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.X_AXIS));
		
		final JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		contentPane.add(tabbedPane);
		
		JPanel db_tab = new JPanel();
		tabbedPane.addTab("Database", null, db_tab, null);
		db_tab.setLayout(new BoxLayout(db_tab, BoxLayout.Y_AXIS));
		
		JPanel new_db = new JPanel();
		new_db.setBorder(new TitledBorder(null, "Read new database file", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		db_tab.add(new_db);
		
		JButton btnSelectNewFile = new JButton("Select new file");
		btnSelectNewFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				
				while(true) {
					int ret = chooser.showOpenDialog(MainWindow.this);

					if(ret == JFileChooser.APPROVE_OPTION) {
						File file = chooser.getSelectedFile();

						if(!file.isFile()) {
							JOptionPane.showMessageDialog(MainWindow.this, "Selected node is not a file", "File Error", JOptionPane.ERROR_MESSAGE);
							continue;
						} else if(!file.canRead()) {
							JOptionPane.showMessageDialog(MainWindow.this, "Selected file is not readable", "File Error", JOptionPane.ERROR_MESSAGE);
							continue;
						} else {
							DataBase.setDumpFile(file);
							Map<String,String> events = DataBase.getEvents(MainWindow.this);
							
							list.setListData((String[])events.keySet().toArray());
							break;
						}
					} else {
						break;
					}

				}
				
			}
		});
		new_db.add(btnSelectNewFile);
		
		JPanel event_sel = new JPanel();
		event_sel.setBorder(new TitledBorder(null, "Event Selection", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		db_tab.add(event_sel);
		event_sel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		event_sel.add(list);
		
		JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder(null, "Prepare ticket list", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		db_tab.add(panel);
		
		JButton btnNewButton = new JButton("Update DB");
		panel.add(btnNewButton);
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				List<String> events = list.getSelectedValuesList();

				if(events.isEmpty()) {
					JOptionPane.showMessageDialog(MainWindow.this, "No events selected, aborting", "No Events", JOptionPane.WARNING_MESSAGE);
					return;
				}

				int reply = JOptionPane.showConfirmDialog(MainWindow.this, "This will update the database and delete all information about scanned tickets. Continue?", "Update Database", JOptionPane.YES_NO_OPTION);
				if (reply == JOptionPane.YES_OPTION) {
					DataBase.updateDB(events, MainWindow.this);

					tabbedPane.setEnabledAt(1, DataBase.checkInReady(MainWindow.this));
					checkin_tab.setEnabled(DataBase.checkInReady(MainWindow.this));
					tabbedPane.setSelectedIndex(1);
					ticket_id.requestFocusInWindow();
				}
			}
		});
		tabbedPane.addTab("Checkin", null, checkin_tab, null);
		tabbedPane.setEnabledAt(1, DataBase.checkInReady(this));
		checkin_tab.setEnabled(DataBase.checkInReady(this));
		checkin_tab.setLayout(new BoxLayout(checkin_tab, BoxLayout.Y_AXIS));
		
		JPanel scan_panel = new JPanel();
		scan_panel.setBorder(new TitledBorder(null, "Scan", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		checkin_tab.add(scan_panel);
		
		JLabel lblTicketId = new JLabel("Ticket ID: ");
		scan_panel.add(lblTicketId);
		
		scan_panel.add(ticket_id);
		ticket_id.setColumns(10);
		
		JPanel info_panel = new JPanel();
		info_panel.setEnabled(false);
		info_panel.setBorder(new TitledBorder(null, "Ticket", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		checkin_tab.add(info_panel);
		info_panel.setLayout(new BorderLayout(0, 0));
		
		JPanel panel_1 = new JPanel();
		info_panel.add(panel_1, BorderLayout.NORTH);
		panel_1.setLayout(new GridLayout(0, 2, 0, 0));
		
		JLabel lblTicketId_1 = new JLabel("Ticket ID:");
		panel_1.add(lblTicketId_1);
		
		panel_1.add(lblTicket);
		
		JLabel lblStatus_label = new JLabel("Status:");
		panel_1.add(lblStatus_label);
		panel_1.add(lblStatus);
		
		JLabel lblOptions_label = new JLabel("Options:");
		panel_1.add(lblOptions_label);
		panel_1.add(lblOptions);
		
		JPanel panel_2 = new JPanel();
		info_panel.add(panel_2, BorderLayout.SOUTH);
		panel_2.setLayout(new GridLayout(1, 0, 0, 0));
		
		btnDisable.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				DataBase.setTicketStatus(DataBase.Status.Invalid, MainWindow.this);

				// Update Buttons
				btnDisable.setSelected(true);
				btnUnused.setSelected(false);
				btnUsed.setSelected(false);
				
				ticket_id.requestFocusInWindow();
			}
		});
		panel_2.add(btnDisable);
		
		btnUnused.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DataBase.setTicketStatus(DataBase.Status.Unused, MainWindow.this);

				// Update Buttons
				btnDisable.setSelected(false);
				btnUnused.setSelected(true);
				btnUsed.setSelected(false);
				
				ticket_id.requestFocusInWindow();
			}
		});
		panel_2.add(btnUnused);
		
		btnUsed.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DataBase.setTicketStatus(DataBase.Status.Used, MainWindow.this);

				// Update Buttons
				btnDisable.setSelected(false);
				btnUnused.setSelected(false);
				btnUsed.setSelected(true);
				
				ticket_id.requestFocusInWindow();
			}
		});
		panel_2.add(btnUsed);
	}

}
