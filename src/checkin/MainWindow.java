package checkin;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.FlowLayout;
import javax.swing.ImageIcon;
import java.awt.Dimension;
import javax.swing.border.LineBorder;
import java.awt.Component;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

public class MainWindow extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	final Color panelBG = new Color(238,238,238);

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		/*SwingUtilities.invokeLater(new Runnable() {
			public void run() { //*/
				DataBase.init();
				
				try {
					MainWindow frame = new MainWindow();
					frame.setVisible(true);
					if(DataBase.getPermissions() == DataBase.PermissionStatus.Insufficient) {
						JOptionPane.showMessageDialog(frame, "MySQL user has insufficient permissions", "MySQL Error", JOptionPane.ERROR_MESSAGE);
						System.exit(1);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
		//	}
		//});
	}

	/**
	 * Create the frame.
	 */
	public MainWindow() {
		
		setMinimumSize(new Dimension(0, 420));
		setTitle("Checkin App");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		
		final String[] listHeader = {"Model", "Product name"};
		final JPanel checkin_tab = new JPanel();
		
		final JTextField ticket_id= new JTextField();
		
		final JLabel lblStatUsed = new JLabel(DataBase.getNumUsed());
		lblStatUsed.setAlignmentX(Component.CENTER_ALIGNMENT);
		final JLabel lblStatInvalid = new JLabel(DataBase.getNumInvalid());
		lblStatInvalid.setAlignmentX(Component.CENTER_ALIGNMENT);
		final JLabel lblStatTotal = new JLabel(DataBase.getNumTotal());
		lblStatTotal.setAlignmentX(Component.CENTER_ALIGNMENT);
		final JLabel lblStatUnused = new JLabel(DataBase.getNumUnused());
		lblStatUnused.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		final JToggleButton btnDisable = new JToggleButton("Invalid");
		final JToggleButton btnUnused = new JToggleButton("OK");
		final JToggleButton btnUsed = new JToggleButton("Used");
		
		if(DataBase.getPermissions() != DataBase.PermissionStatus.Admin) {
			btnDisable.setEnabled(false);
			btnUnused.setEnabled(false);
			btnUsed.setEnabled(false);
		}
		
		final JLabel lblStatus = new JLabel("");
		final JTextArea lblOptions = new JTextArea("");
		final JTextArea lblProduct = new JTextArea("");
		final JTextArea lblTicketName = new JTextArea("");
		lblOptions.setFont(new Font("Dialog", Font.BOLD, 14));
		final JLabel lblTicket = new JLabel("");
		lblTicket.setFont(new Font("Dialog", Font.BOLD, 14));
		
		final JPanel panel_info = new JPanel();
		
		final JTable list = new JTable( new String[0][0],listHeader);
		list.getColumnModel().getColumn(1).setPreferredWidth(300);
		
		checkin_tab.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent arg0) {
				ticket_id.requestFocusInWindow();
			}
		});
		
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.X_AXIS));
		
		final JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				if(tabbedPane.getSelectedIndex() == 1) {
					ticket_id.requestFocusInWindow();
				}
			}
		});
		contentPane.add(tabbedPane);
		
		JPanel db_tab = new JPanel();
		if(DataBase.getPermissions() != DataBase.PermissionStatus.Admin) {
			db_tab.setEnabled(false);
		}
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
							final String[][] events = DataBase.getEvents(MainWindow.this);
							
							DefaultTableModel m = new DefaultTableModel(events, listHeader);
							list.setModel(m);
							list.getColumnModel().getColumn(1).setPreferredWidth(300);
							m.fireTableDataChanged();
							//list.setListData((String[])events.keySet().toArray());
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
		event_sel.setLayout(new BorderLayout(0, 0));
		
		JScrollPane scrollPane = new JScrollPane();
		event_sel.add(scrollPane);
		scrollPane.setViewportView(list);
		list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		
		JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder(null, "Prepare ticket list", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		db_tab.add(panel);
		
		JButton btnNewButton = new JButton("Update DB");
		panel.add(btnNewButton);
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int[] rows = list.getSelectedRows();
				List<String> events = new ArrayList<String>();
				final TableModel m = list.getModel();
				for(int i=0;i<rows.length;i++) {
					events.add((String)m.getValueAt(rows[i], 0)); 
				}

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
		
		tabbedPane.setEnabledAt(1, DataBase.checkInReady(this)); // only in checkin tab if data base initialized
		if(DataBase.getPermissions() != DataBase.PermissionStatus.Admin) {
			// User
			tabbedPane.setEnabledAt(0, false); // always in checkin tab
			tabbedPane.setSelectedIndex(1);
		} else {
			// Admin
			tabbedPane.setEnabledAt(0, true); // data base tab always enabled
			tabbedPane.setSelectedIndex(DataBase.checkInReady(this) ? 1 : 0);
		}
		checkin_tab.setEnabled(DataBase.checkInReady(this));
		checkin_tab.setLayout(new BoxLayout(checkin_tab, BoxLayout.Y_AXIS));
		
		lblStatUnused.setFont(new Font("Dialog", Font.PLAIN, 10));
		lblStatUsed.setFont(new Font("Dialog", Font.PLAIN, 10));
		lblStatInvalid.setFont(new Font("Dialog", Font.PLAIN, 10));
		lblStatTotal.setFont(new Font("Dialog", Font.PLAIN, 10));
		
		JPanel panel_1 = new JPanel();
		checkin_tab.add(panel_1);
		panel_1.setLayout(new BoxLayout(panel_1, BoxLayout.X_AXIS));
		
		JPanel panel_4 = new JPanel();
		panel_1.add(panel_4);
		
		ticket_id.addActionListener(new ActionListener() {
			private final Color proc = Color.GRAY;
			private final Color def = lblProduct.getForeground();
			
			public void actionPerformed(ActionEvent arg0) {
				// extract entered code
				final String ticketid = ticket_id.getText();
				
				// do not edit (and scan another ticket) untill we are finished with htis one
				ticket_id.setEditable(false);
				
				// update ticket label
				lblTicket.setText(ticketid);
				
				// chenge all other labels to greyed "processing..."
				panel_info.setBackground(panelBG);
				lblStatus.setForeground(proc);
				lblStatus.setText("processing...");
				lblProduct.setText("");
				lblOptions.setText("");
				lblTicketName.setText("");
				
				// Database operation might block. Keep Swing running by doing everything
				// else in a separate thread
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {

						// Now perform DB operation
						DataBase.checkInTicket(ticketid, MainWindow.this);

						// update info fields
						lblOptions.setText(DataBase.getTicketOptions());
						lblStatus.setText(DataBase.getTicketStatusString());
						panel_info.setBackground(DataBase.getTicketStatusColor());
						lblStatus.setForeground(def);
						lblProduct.setText(DataBase.getProductName());
						lblTicketName.setText(DataBase.getTicketName());
						// Update Info Fields
						// Update Buttons
						btnDisable.setSelected(DataBase.isInvalid());
						btnUnused.setSelected(DataBase.isUnused());
						btnUsed.setSelected(DataBase.isUsed());

						// clear field again
						ticket_id.setEditable(true);
						ticket_id.setText("");
						ticket_id.requestFocusInWindow();
						
						lblStatUnused.setText(DataBase.getNumUnused());
						lblStatUsed.setText(DataBase.getNumUsed());
						lblStatInvalid.setText(DataBase.getNumInvalid());
						lblStatTotal.setText(DataBase.getNumTotal());
					}
				});
			}
		});
		panel_4.setLayout(new BoxLayout(panel_4, BoxLayout.Y_AXIS));
		
		JPanel panel_5 = new JPanel();
		panel_5.setMaximumSize(new Dimension(32767, 150));
		panel_5.setBorder(new TitledBorder(new LineBorder(new Color(184, 207, 229)), "Ticket count", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel_4.add(panel_5);
		GridBagLayout gbl_panel_5 = new GridBagLayout();
		gbl_panel_5.columnWidths = new int[] {150, 80};
		gbl_panel_5.rowHeights = new int[] {12, 12, 12, 12};
		gbl_panel_5.columnWeights = new double[]{0.0, 0.0};
		gbl_panel_5.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		panel_5.setLayout(gbl_panel_5);
		GridBagConstraints gbc_lblStatUnused = new GridBagConstraints();
		gbc_lblStatUnused.anchor = GridBagConstraints.EAST;
		gbc_lblStatUnused.insets = new Insets(0, 0, 0, 5);
		gbc_lblStatUnused.fill = GridBagConstraints.VERTICAL;
		gbc_lblStatUnused.gridx = 0;
		gbc_lblStatUnused.gridy = 0;
		panel_5.add(lblStatUnused, gbc_lblStatUnused);
		
		JLabel lblNewLabel_2 = new JLabel("unused");
		lblNewLabel_2.setFont(new Font("Dialog", Font.PLAIN, 10));
		GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
		gbc_lblNewLabel_2.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel_2.gridx = 1;
		gbc_lblNewLabel_2.gridy = 0;
		panel_5.add(lblNewLabel_2, gbc_lblNewLabel_2);
		
		GridBagConstraints gbc_lblStatUsed = new GridBagConstraints();
		gbc_lblStatUsed.insets = new Insets(0, 0, 0, 5);
		gbc_lblStatUsed.anchor = GridBagConstraints.EAST;
		gbc_lblStatUsed.gridx = 0;
		gbc_lblStatUsed.gridy = 1;
		panel_5.add(lblStatUsed, gbc_lblStatUsed);
		
		JLabel lblUsed = new JLabel("used");
		lblUsed.setFont(new Font("Dialog", Font.PLAIN, 10));
		GridBagConstraints gbc_lblUsed = new GridBagConstraints();
		gbc_lblUsed.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblUsed.gridx = 1;
		gbc_lblUsed.gridy = 1;
		panel_5.add(lblUsed, gbc_lblUsed);
		
		GridBagConstraints gbc_lblStatInvalid = new GridBagConstraints();
		gbc_lblStatInvalid.insets = new Insets(0, 0, 0, 5);
		gbc_lblStatInvalid.anchor = GridBagConstraints.EAST;
		gbc_lblStatInvalid.gridx = 0;
		gbc_lblStatInvalid.gridy = 2;
		panel_5.add(lblStatInvalid, gbc_lblStatInvalid);
		
		JLabel lblInvalid = new JLabel("invalid");
		lblInvalid.setFont(new Font("Dialog", Font.PLAIN, 10));
		GridBagConstraints gbc_lblInvalid = new GridBagConstraints();
		gbc_lblInvalid.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblInvalid.gridx = 1;
		gbc_lblInvalid.gridy = 2;
		panel_5.add(lblInvalid, gbc_lblInvalid);
		gbc_lblStatInvalid.gridx = 0;
		gbc_lblStatInvalid.gridy = 3;
		gbc_lblInvalid.fill = GridBagConstraints.BOTH;
		gbc_lblInvalid.gridx = 1;
		gbc_lblInvalid.gridy = 3;
		
		
		
		GridBagConstraints gbc_lblStatTotal = new GridBagConstraints();
		gbc_lblStatTotal.insets = new Insets(0, 0, 0, 5);
		gbc_lblStatTotal.anchor = GridBagConstraints.EAST;
		gbc_lblStatTotal.gridx = 0;
		gbc_lblStatTotal.gridy = 3;
		panel_5.add(lblStatTotal, gbc_lblStatTotal);
		
		JLabel lblTotal = new JLabel("total");
		lblTotal.setFont(new Font("Dialog", Font.PLAIN, 10));
		GridBagConstraints gbc_lblTotal = new GridBagConstraints();
		gbc_lblTotal.anchor = GridBagConstraints.WEST;
		gbc_lblTotal.gridx = 1;
		gbc_lblTotal.gridy = 3;
		panel_5.add(lblTotal, gbc_lblTotal);
		
		
		
		
		JPanel scan_panel = new JPanel();
		panel_4.add(scan_panel);
		scan_panel.setBorder(new TitledBorder(null, "Scan", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		scan_panel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		JLabel lblTicketId = new JLabel("Ticket ID: ");
		lblTicketId.setHorizontalTextPosition(SwingConstants.CENTER);
		lblTicketId.setHorizontalAlignment(SwingConstants.CENTER);
		scan_panel.add(lblTicketId);
		
		scan_panel.add(ticket_id);
		ticket_id.setColumns(10);
		
		JPanel panel_3 = new JPanel();
		panel_3.setMaximumSize(new Dimension(135, 135));
		panel_1.add(panel_3);
		
		JLabel lblNewLabel = new JLabel("");
		lblNewLabel.setPreferredSize(new Dimension(125, 125));
		lblNewLabel.setMaximumSize(new Dimension(125, 125));
		lblNewLabel.setMinimumSize(new Dimension(125, 125));
		lblNewLabel.setIcon(new ImageIcon(MainWindow.class.getResource("/checkin/logo_small.png")));
		lblNewLabel.setSize(new Dimension(125, 125));
		panel_3.add(lblNewLabel);
		
		lblProduct.setLineWrap(true);
		lblProduct.setWrapStyleWord(true);
		lblProduct.setOpaque(false);
		lblProduct.setEditable(false);
		lblProduct.setFont(new Font("Dialog", Font.BOLD, 14));
		
		lblOptions.setLineWrap(true);
		lblOptions.setWrapStyleWord(true);
		lblOptions.setOpaque(false);
		lblOptions.setEditable(false);
		lblOptions.setFont(new Font("Dialog", Font.BOLD, 14));
		
		lblTicketName.setLineWrap(true);
		lblTicketName.setWrapStyleWord(true);
		lblTicketName.setOpaque(false);
		lblTicketName.setEditable(false);
		lblTicketName.setFont(new Font("Dialog", Font.BOLD, 14));
		
		
		JPanel ticketinfo_panel = new JPanel();
		ticketinfo_panel.setOpaque(false);
		ticketinfo_panel.setEnabled(false);
		ticketinfo_panel.setBorder(new TitledBorder(new LineBorder(new Color(184, 207, 229)), "Ticket info", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		checkin_tab.add(ticketinfo_panel);
		ticketinfo_panel.setLayout(new BorderLayout(0, 0));
		
		ticketinfo_panel.add(panel_info, BorderLayout.NORTH);
		GridBagLayout gbl_panel_info = new GridBagLayout();
		gbl_panel_info.columnWidths = new int[] {100, 310};
		gbl_panel_info.rowHeights = new int[]{15, 0, 0, 15, 15, 15, 0};
		gbl_panel_info.columnWeights = new double[]{0.0, 1.0};
		gbl_panel_info.rowWeights = new double[]{0.0, 0.0, 1.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		panel_info.setLayout(gbl_panel_info);
		
		JLabel lblTicketId_label = new JLabel("Ticket ID:");
		lblTicketId_label.setHorizontalTextPosition(SwingConstants.CENTER);
		lblTicketId_label.setHorizontalAlignment(SwingConstants.RIGHT);
		lblTicketId_label.setFont(new Font("Dialog", Font.BOLD, 14));
		GridBagConstraints gbc_lblTicketId_label = new GridBagConstraints();
		gbc_lblTicketId_label.anchor = GridBagConstraints.NORTHEAST;
		gbc_lblTicketId_label.insets = new Insets(5, 5, 5, 5);
		gbc_lblTicketId_label.gridx = 0;
		gbc_lblTicketId_label.gridy = 0;
		panel_info.add(lblTicketId_label, gbc_lblTicketId_label);
		
		GridBagConstraints gbc_lblTicket = new GridBagConstraints();
		gbc_lblTicket.anchor = GridBagConstraints.NORTH;
		gbc_lblTicket.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblTicket.insets = new Insets(5, 0, 5, 5);
		gbc_lblTicket.gridx = 1;
		gbc_lblTicket.gridy = 0;
		panel_info.add(lblTicket, gbc_lblTicket);
		
		JLabel lblStatus_label = new JLabel("Status:");
		lblStatus_label.setHorizontalTextPosition(SwingConstants.CENTER);
		lblStatus_label.setHorizontalAlignment(SwingConstants.RIGHT);
		lblStatus_label.setFont(new Font("Dialog", Font.BOLD, 14));
		GridBagConstraints gbc_lblStatus_label = new GridBagConstraints();
		gbc_lblStatus_label.anchor = GridBagConstraints.NORTHEAST;
		gbc_lblStatus_label.insets = new Insets(0, 5, 5, 5);
		gbc_lblStatus_label.gridx = 0;
		gbc_lblStatus_label.gridy = 1;
		panel_info.add(lblStatus_label, gbc_lblStatus_label);
		
		lblStatus.setFont(new Font("Dialog", Font.BOLD, 14));
		
		GridBagConstraints gbc_lblStatus = new GridBagConstraints();
		gbc_lblStatus.anchor = GridBagConstraints.NORTH;
		gbc_lblStatus.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblStatus.insets = new Insets(0, 0, 5, 5);
		gbc_lblStatus.gridx = 1;
		gbc_lblStatus.gridy = 1;
		panel_info.add(lblStatus, gbc_lblStatus);
		
		JLabel lblProduct_label = new JLabel("Event:");
		lblProduct_label.setPreferredSize(new Dimension(91, 15));
		lblProduct_label.setMinimumSize(new Dimension(91, 15));
		lblProduct_label.setMaximumSize(new Dimension(91, 15));
		lblProduct_label.setHorizontalTextPosition(SwingConstants.CENTER);
		lblProduct_label.setVerticalTextPosition(SwingConstants.TOP);
		lblProduct_label.setHorizontalAlignment(SwingConstants.RIGHT);
		lblProduct_label.setFont(new Font("Dialog", Font.BOLD, 14));
		lblProduct_label.setVerticalAlignment(SwingConstants.TOP);
		GridBagConstraints gbc_lblProduct_label = new GridBagConstraints();
		gbc_lblProduct_label.anchor = GridBagConstraints.NORTHEAST;
		gbc_lblProduct_label.insets = new Insets(0, 5, 5, 5);
		gbc_lblProduct_label.gridx = 0;
		gbc_lblProduct_label.gridy = 3;
		panel_info.add(lblProduct_label, gbc_lblProduct_label);
		
		GridBagConstraints gbc_lblProduct = new GridBagConstraints();
		gbc_lblProduct.anchor = GridBagConstraints.NORTH;
		gbc_lblProduct.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblProduct.insets = new Insets(0, 0, 5, 5);
		gbc_lblProduct.gridx = 1;
		gbc_lblProduct.gridy = 3;
		panel_info.add(lblProduct, gbc_lblProduct);
		
		JLabel lblTicketName_label = new JLabel("Option 1:");
		lblTicketName_label.setHorizontalTextPosition(SwingConstants.CENTER);
		lblTicketName_label.setVerticalTextPosition(SwingConstants.TOP);
		lblTicketName_label.setVerticalAlignment(SwingConstants.TOP);
		lblTicketName_label.setHorizontalAlignment(SwingConstants.RIGHT);
		lblTicketName_label.setFont(new Font("Dialog", Font.BOLD, 14));
		GridBagConstraints gbc_lblTicketName_label = new GridBagConstraints();
		gbc_lblTicketName_label.anchor = GridBagConstraints.NORTHEAST;
		gbc_lblTicketName_label.insets = new Insets(0, 5, 5, 5);
		gbc_lblTicketName_label.gridx = 0;
		gbc_lblTicketName_label.gridy = 4;
		panel_info.add(lblTicketName_label, gbc_lblTicketName_label);
		
		GridBagConstraints gbc_lblTicketName = new GridBagConstraints();
		gbc_lblTicketName.anchor = GridBagConstraints.NORTH;
		gbc_lblTicketName.insets = new Insets(0, 0, 5, 5);
		gbc_lblTicketName.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblTicketName.gridx = 1;
		gbc_lblTicketName.gridy = 4;
		panel_info.add(lblTicketName, gbc_lblTicketName);
		
		JLabel lblOptions_label = new JLabel("Option 2:");
		lblOptions_label.setMaximumSize(new Dimension(91, 15));
		lblOptions_label.setMinimumSize(new Dimension(91, 15));
		lblOptions_label.setPreferredSize(new Dimension(91, 15));
		lblOptions_label.setHorizontalTextPosition(SwingConstants.CENTER);
		lblOptions_label.setVerticalTextPosition(SwingConstants.TOP);
		lblOptions_label.setVerticalAlignment(SwingConstants.TOP);
		lblOptions_label.setHorizontalAlignment(SwingConstants.RIGHT);
		lblOptions_label.setFont(new Font("Dialog", Font.BOLD, 14));
		GridBagConstraints gbc_lblOptions_label = new GridBagConstraints();
		gbc_lblOptions_label.anchor = GridBagConstraints.NORTHEAST;
		gbc_lblOptions_label.insets = new Insets(0, 5, 5, 5);
		gbc_lblOptions_label.gridx = 0;
		gbc_lblOptions_label.gridy = 5;
		panel_info.add(lblOptions_label, gbc_lblOptions_label);
		
		GridBagConstraints gbc_lblOptions = new GridBagConstraints();
		gbc_lblOptions.anchor = GridBagConstraints.NORTH;
		gbc_lblOptions.insets = new Insets(0, 0, 5, 5);
		gbc_lblOptions.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblOptions.gridx = 1;
		gbc_lblOptions.gridy = 5;
		panel_info.add(lblOptions, gbc_lblOptions);
		
		JPanel panel_2 = new JPanel();
		ticketinfo_panel.add(panel_2, BorderLayout.SOUTH);
		panel_2.setLayout(new GridLayout(1, 0, 0, 0));
		
		btnDisable.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				DataBase.setTicketStatus(DataBase.Status.Invalid, MainWindow.this);

				// Update Buttons
				btnDisable.setSelected(true);
				btnUnused.setSelected(false);
				btnUsed.setSelected(false);

				lblStatUnused.setText(DataBase.getNumUnused());
				lblStatUsed.setText(DataBase.getNumUsed());
				lblStatInvalid.setText(DataBase.getNumInvalid());
				lblStatTotal.setText(DataBase.getNumTotal());

				lblStatus.setText(DataBase.getTicketStatusString());
				panel_info.setBackground(DataBase.getTicketStatusColor());
				
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

				lblStatUnused.setText(DataBase.getNumUnused());
				lblStatUsed.setText(DataBase.getNumUsed());
				lblStatInvalid.setText(DataBase.getNumInvalid());
				lblStatTotal.setText(DataBase.getNumTotal());

				lblStatus.setText(DataBase.getTicketStatusString());
				panel_info.setBackground(DataBase.getTicketStatusColor());
				
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

				lblStatUnused.setText(DataBase.getNumUnused());
				lblStatUsed.setText(DataBase.getNumUsed());
				lblStatInvalid.setText(DataBase.getNumInvalid());
				lblStatTotal.setText(DataBase.getNumTotal());

				lblStatus.setText(DataBase.getTicketStatusString());
				panel_info.setBackground(DataBase.getTicketStatusColor());
				
				ticket_id.requestFocusInWindow();
			}
		});
		panel_2.add(btnUsed);
	}
}
