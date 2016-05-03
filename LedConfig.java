/* TODO:
 *
 * Get upload working (add send/receive eep suport to board)
 *  - add progress monitor
 *
 * Do some renaming and code cleanup.
 *
 * Add names to presets?
 *
 * Palette previews in palette dropdown?
 *
 *
 * Preset editor
 * Make another JFrame dedicated to interfacing with live board. Add preset editor as subprogram.
 * //if I add a refresh current palette button then I could remove the individual console commands and just send over the preset config. Add save button to to write current to EEPROM.
 *
 */

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import javax.swing.filechooser.*;
//import java.nio.file.*;
import java.nio.charset.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import java.net.URL;

public class LedConfig extends JFrame implements ActionListener {
    private JTabbedPane tabs;
	private JProgressBar eepromUsage;
	private Serial serial = new Serial();
	String selectedDevice = null;

	JFileChooser fc;
	ConfigPanel configPanel;
	ArrayList<PresetPanel> presetPanels = new ArrayList<PresetPanel>(5);

	public LedConfig() {
		super("Led Configurator");
		setLayout(new BorderLayout());

		fc = new JFileChooser();
		FileNameExtensionFilter eepFilter = new FileNameExtensionFilter("EEPROM Files (.eep)", "eep");
		fc.setFileFilter(eepFilter);
		fc.setSelectedFile(new File("ledstrip.eep"));

		JMenuBar menuBar = new JMenuBar();
			JMenu fileMenu = new JMenu("File");
			fileMenu.setMnemonic(KeyEvent.VK_F);
				JMenuItem loadItem = new JMenuItem("Open");
				loadItem.setMnemonic(KeyEvent.VK_O);
				loadItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
				loadItem.setActionCommand("open");
				loadItem.addActionListener(this);
				fileMenu.add(loadItem);

				JMenuItem saveItem = new JMenuItem("Save");
				saveItem.setMnemonic(KeyEvent.VK_S);
				saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
				saveItem.setActionCommand("save");
				saveItem.addActionListener(this);
				fileMenu.add(saveItem);

			fileMenu.addSeparator();

			JMenuItem importPreset = new JMenuItem("Import");
			importPreset.setMnemonic(KeyEvent.VK_C);
			importPreset.setActionCommand("import");
			importPreset.addActionListener(this);
			fileMenu.add(importPreset);

			JMenu exportMenu = new JMenu("Export");
			exportMenu.setMnemonic(KeyEvent.VK_E);
			fileMenu.add(exportMenu);

				JMenuItem exportCurrent = new JMenuItem("Export current preset");
				exportCurrent.setMnemonic(KeyEvent.VK_C);
				exportCurrent.setActionCommand("export");
				exportCurrent.addActionListener(this);
				exportMenu.add(exportCurrent);

				JMenuItem exportAll = new JMenuItem("Export all presets");
				exportAll.setMnemonic(KeyEvent.VK_A);
				exportAll.setActionCommand("export-all");
				exportAll.addActionListener(this);
				exportMenu.add(exportAll);

			JMenu buildMenu = new JMenu("Upload");
			buildMenu.setMnemonic(KeyEvent.VK_B);
				final JMenu deviceMenu = new JMenu("Devices");
				deviceMenu.setMnemonic(KeyEvent.VK_D);
				deviceMenu.addMenuListener(new MenuListener() {
					public void menuCanceled(MenuEvent e) {}
					public void menuDeselected(MenuEvent e) {}
					public void menuSelected(MenuEvent e) {
						deviceMenu.removeAll();
						ButtonGroup deviceGroup = new ButtonGroup();
						Set<String> devices = serial.listDevices();
						if (devices.size()==1) {
							selectedDevice = devices.iterator().next();
						}
						for (String device : devices) {
							JRadioButtonMenuItem deviceBtn = new JRadioButtonMenuItem(device, device.equals(selectedDevice));
							deviceBtn.addItemListener(new ItemListener() {
								@Override
								public void itemStateChanged(ItemEvent e) {
									if (e.getStateChange() == ItemEvent.SELECTED) {
										selectedDevice = ((JRadioButtonMenuItem)e.getItem()).getText();
									}
								}
							});
							deviceGroup.add(deviceBtn);
							deviceMenu.add(deviceBtn);
						}
					}
				});
				buildMenu.add(deviceMenu);

				JMenuItem buildItem = new JMenuItem("Upload");
				buildItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U, ActionEvent.CTRL_MASK));
				buildItem.setMnemonic(KeyEvent.VK_U);
				buildItem.setActionCommand("upload");
				buildItem.addActionListener(this);
				buildMenu.add(buildItem);

		menuBar.add(fileMenu);
		menuBar.add(buildMenu);

		setJMenuBar(menuBar);

		tabs = new JTabbedPane() {
			public void remove(int index) {
				super.remove(index);
				configPanel.removePreset();
				presetPanels.remove(index-1);
				for (int i=index; i<tabs.getTabCount(); i++) {
					tabs.setTitleAt(i, "Preset "+i);
				}
				calculateUsage();
			}
		};
		tabs.setUI(new MyTabbedPaneUI());
		tabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

		configPanel = new ConfigPanel();
		configPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        tabs.addTab("Global", null, configPanel, "Tip");
        tabs.setMnemonicAt(0, KeyEvent.VK_0);

		JButton addBtn = new JButton("+");
		addBtn.setFocusable(false);
		addBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addPreset();
				int size = calculateUsage();

				//NOTE: Currently has space for exactly 113 presets
				if (size > 2048) {
					JOptionPane.showMessageDialog(LedConfig.this, "The controller will not be able to store this many presets. Please remove some.", "EEPROM Too Large", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		tabs.add("+", null);
		tabs.setTabComponentAt(tabs.getTabCount()-1, addBtn);

		add(tabs);

		JPanel statusbar = new JPanel();
		statusbar.setLayout(new BoxLayout(statusbar, BoxLayout.X_AXIS));
			eepromUsage = new JProgressBar(0, 2048);
			eepromUsage.setStringPainted(true);
			statusbar.add(eepromUsage);
		add(statusbar, BorderLayout.SOUTH);

		/*Dimension min = getMinimumSize();
		min.width = (int)(min.height*1.61803398875);
		setMinimumSize(min);*/
		pack();
	}

	public static void customizeSpinner(JSpinner spinner) {
		JSpinner.DefaultEditor spinnerEditor = (JSpinner.DefaultEditor)spinner.getEditor();
		spinnerEditor.getTextField().setHorizontalAlignment(JTextField.CENTER);
		spinnerEditor.getTextField().setColumns(3);
	}

	private void clearPresets() {
		int lastIndex = tabs.getTabCount()-1;
		for (int i=lastIndex-1; i>=1; i--) {
			tabs.remove(i);
		}
		presetPanels.clear();
	}

	private PresetPanel addPreset() {
			int i = tabs.getTabCount()-1;

			PresetPanel preset = new PresetPanel();
			preset.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

			tabs.insertTab("Preset "+(presetPanels.size()+1), null, preset, null, i);
			tabs.setTabComponentAt(i, new ButtonTabComponent(tabs));
			if (i>=1 && i<=9) {
				tabs.setMnemonicAt(i, KeyEvent.VK_0+i);
			}
			presetPanels.add(preset);
			configPanel.addPreset();
			return preset;
	}

	private void loadEepromData(String ihex) {
		try {
			Settings settings = new Settings();
			String data = IntelHex.decodeEeprom(ihex);
			data = IntelHex.toBytes(settings.version, data);

			int v = settings.version.getInt();
			if (v != Settings.VERSION) {
				JOptionPane.showMessageDialog(this, String.format("Invalid version '%d', expected '%d'.", v, Settings.VERSION), "Error Loading Settings", JOptionPane.ERROR_MESSAGE);
				return;
			}

			List fields = settings.fields();
			fields.remove(0); //remove version field
			data = IntelHex.stringToSettings(data, fields);

			int currentTab = tabs.getSelectedIndex();
			clearPresets();

			Preset presets[] = new Preset[settings.presetCount.getInt()];
			for (int i=0; i<settings.presetCount.getInt(); i++) {
				presets[i] = new Preset();
				data = IntelHex.stringToSettings(data, presets[i].fields());
				PresetPanel presetPanel = addPreset();
				presetPanel.load(presets[i]);
			}

			configPanel.load(settings);
			if (currentTab < tabs.getTabCount()) {
				tabs.setSelectedIndex(currentTab);
			}

			calculateUsage();
		} catch (InvalidRecordException e) {
			JOptionPane.showMessageDialog(this, e.getMessage(), "Error Importing File", JOptionPane.ERROR_MESSAGE);
		}
	}

	private String createEepromData() {
		ArrayList<SettingType> fields = new ArrayList<SettingType>();
		Settings settings = configPanel.getSettings();
		Preset presets[] = new Preset[presetPanels.size()];

		settings.version.set(Settings.VERSION);
		settings.presetCount.set(presetPanels.size());
		fields.addAll(settings.fields());

		for (int i=0; i<presets.length; i++) {
			presets[i] = presetPanels.get(i).getPreset();
			fields.addAll(presets[i].fields());
		}

		return IntelHex.settingsToString(fields);
    }

	void importPreset(File file) {
		try {
			ArrayList<SettingType> fields = new ArrayList<SettingType>();
			String ihex = readStream(new FileInputStream(file));
			String data = IntelHex.decodeEeprom(ihex);

			SettingType version = new SettingType(8);
			fields.add(version);
			data = IntelHex.stringToSettings(data, fields);

			if (version.getInt() != Settings.VERSION) {
				JOptionPane.showMessageDialog(this, String.format("Invalid version '%d', expected '%d'.", version.getInt(), Settings.VERSION), "Error Importing Presets", JOptionPane.ERROR_MESSAGE);
			}

			SettingType count = new SettingType(8);
			fields.clear();
			fields.add(count);
			data = IntelHex.stringToSettings(data, fields);

			Preset presets[] = new Preset[count.getInt()];
			for (Preset preset : presets) {
				preset = new Preset();
				data = IntelHex.stringToSettings(data, preset.fields());
				PresetPanel presetPanel = addPreset();
				presetPanel.load(preset);
			}
		} catch (IOException e) {
			JOptionPane.showMessageDialog(this, e.toString(), "Error Importing File", JOptionPane.ERROR_MESSAGE);
		} catch (InvalidRecordException e) {
			JOptionPane.showMessageDialog(this, e.getMessage(), "Error Importing File", JOptionPane.ERROR_MESSAGE);
		}
	}

	void exportPreset(File file, int i) {
		if (i>0 && i<presetPanels.size()) {
			ArrayList<PresetPanel> list = new ArrayList<PresetPanel>();
			list.add(presetPanels.get(i));
			exportPresets(file, list);
		}
	}

	void exportPresets(File file, ArrayList<PresetPanel> presets) {
		ArrayList<SettingType> fields = new ArrayList<SettingType>();

		SettingType version = new SettingType(8);
		version.set(Settings.VERSION);
		fields.add(version);

		SettingType count = new SettingType(8);
		count.set(presets.size());
		fields.add(count);

		for (PresetPanel panel : presets) {
			fields.addAll(panel.getPreset().fields());
		}

		String data = IntelHex.settingsToString(fields);
		saveFile(file, data);
	}

	int calculateUsage() {
		Settings settings = new Settings();
		Preset preset = new Preset();
		int size = (settings.size()+presetPanels.size()*preset.size()) / 8;
		eepromUsage.setValue(size);
		eepromUsage.setString(String.format("Current Usage: %d / 2048 bytes (%.2f%%)", size, (100.0*size/2048)));
		return size;
	}

	void load(InputStream s) throws IOException {
		loadEepromData(readStream(s));
	}

    public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		if ("open".equals(cmd)) {
			int retval = fc.showOpenDialog(this);
			if (retval == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				try {
					load(new FileInputStream(file));
				} catch (IOException ex) {
					JOptionPane.showMessageDialog(this, ex.toString(), "Error Opening", JOptionPane.ERROR_MESSAGE);
				}
			}
		} else if ("save".equals(cmd)) {
			int retval = fc.showSaveDialog(this);
			if (retval == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				if (file.getPath().lastIndexOf(".") == -1) {
					file = new File(file.getPath()+".eep");
				}
				saveFile(file, createEepromData());
			}
		} else if ("import".equals(cmd)) {
			int retval = fc.showOpenDialog(this);
			if (retval == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				importPreset(file);
			}
		} else if ("export".equals(cmd)) {
			int retval = fc.showSaveDialog(this);
			if (retval == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				exportPreset(file, tabs.getSelectedIndex()-1);
			}
		} else if ("export-all".equals(cmd)) {
			int retval = fc.showSaveDialog(this);
			if (retval == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				exportPresets(file, presetPanels);
			}
		} else if ("upload".equals(cmd)) {
			System.out.println("upload");
			if (selectedDevice!=null) {
				if (!serial.upload(selectedDevice, createEepromData())) {
					JOptionPane.showMessageDialog(this, "Please check the connection and make sure the device is on.", "Error Uploading File", JOptionPane.ERROR_MESSAGE);
				}
			} else {
				JOptionPane.showMessageDialog(this, "Please select a device to upload to.", "Error Uploading File", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	void saveFile(File file, String content) {
		try {
			Writer out = new BufferedWriter(new FileWriter(file));
			try {
			  out.write(content);
			} finally {
			  out.close();
			}
		} catch (IOException e) {
			JOptionPane.showMessageDialog(this, e.toString(), "Error Saving", JOptionPane.ERROR_MESSAGE);
		}
	}

    static String readStream(InputStream in) throws IOException {
		if (in==null) throw new IOException("Invalid input stream.");
		int length = 0;
		byte buffer[] = new byte[1024];
		StringBuilder builder = new StringBuilder();
		while ((length = in.read(buffer)) != -1) {
			builder.append(new String(buffer, 0, length));
		}
		in.close();
		return builder.toString();
    }

	private static void setLAF(String name) {
		try {
			String laf = UIManager.getSystemLookAndFeelClassName();
			for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
				if (name.equals(info.getName())) {
					laf = info.getClassName();
					break;
				}
			}
			UIManager.setLookAndFeel(laf);
		} catch(Exception e) {
			//Safely ignore and fall back to default L&F
		}
	}

	public class MyTabbedPaneUI extends BasicTabbedPaneUI {
		protected void paintTab(Graphics g, int tabPlacement,
								Rectangle[] rects, int tabIndex,
								Rectangle iconRect, Rectangle textRect) {
			if (tabIndex != tabs.getTabCount()-1) {
				super.paintTab(g,tabPlacement,rects,tabIndex,iconRect,textRect);
			}
		}
	}

	public static void main(final String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
				setLAF("Nimbus");

				LedConfig gui = new LedConfig();
				gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				gui.setVisible(true);

				try {
					if (args.length == 1) {
						gui.load(new FileInputStream(args[0]));
					} else {
						InputStream in = gui.getClass().getResourceAsStream("default.eep");
						gui.load(in);
					}
				} catch (IOException e) {
					System.err.println("Warning: Could not load default configuration.");
				}
			}
		});
	}
}
