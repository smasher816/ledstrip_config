import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ConfigPanel extends JPanel {
	String[] frequencies = {"63 Hz", "160 Hz", "400 Hz", "1 kHz", "2.5 kHz", "6.25 kH", "16 kHz"};
	JTextField version;
	JComboBox preset;
	JSpinner msgeq7_min;
	SpinSlider brightness;
	ColorPanel correction;
	SpinSlider temperature;
	JComboBox frequency;
	JSpinner sensitivity;
	SpinSlider min;

	public ConfigPanel() {
		super();
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		DefaultListCellRenderer dlcr = new DefaultListCellRenderer();
		dlcr.setHorizontalAlignment(DefaultListCellRenderer.CENTER);

		JPanel general = new JPanel(new GridLayout(0,2,5,5));
		general.setBorder(BorderFactory.createTitledBorder("General"));

			general.add(new JLabel("Version: "));
			version = new JTextField(Integer.toString(Settings.VERSION));
			version.setHorizontalAlignment(SwingConstants.CENTER);
			version.setEditable(false);
			general.add(version);

			general.add(new JLabel("Default Preset: "));
			preset = new JComboBox();
			preset.setRenderer(dlcr);
			general.add(preset);

		JPanel calibration = new JPanel(new GridLayout(0,2,5,5));
		calibration.setBorder(BorderFactory.createTitledBorder("Calibration"));

			calibration.add(new JLabel("Correction: "));
			correction = new ColorPanel(new Color(0xFF,0xB0,0xF0));
			calibration.add(correction);

			calibration.add(new JLabel("MSGEQ7 Min: "));
			msgeq7_min = new JSpinner(new SpinnerNumberModel(20, 0, 255, 1));
			LedConfig.customizeSpinner(msgeq7_min);
			calibration.add(msgeq7_min);

		JPanel lighting = new JPanel(new GridLayout(0,2,5,5));
		lighting.setBorder(BorderFactory.createTitledBorder("Lighting"));

			lighting.add(new JLabel("Brightness: "));
			brightness = new SpinSlider(0, 255, 255);
			lighting.add(brightness);

			lighting.add(new JLabel("Temperature: "));
			temperature = new SpinSlider(0, 10000, 6500, 100);
			temperature.setTickSpacing(0, 0);
			JSlider tempSlider = temperature.getSlider();
			tempSlider.setUI(new TemperatureSliderUI(tempSlider));
			lighting.add(temperature);

		JPanel music = new JPanel(new GridLayout(0,2,5,5));
		music.setBorder(BorderFactory.createTitledBorder("Music"));

			music.add(new JLabel("Frequency: "));
			frequency = new JComboBox(frequencies);
			frequency.setRenderer(dlcr);
			music.add(frequency);

			music.add(new JLabel("Sensitivity Multiplier: "));
			sensitivity = new JSpinner(new SpinnerNumberModel(1.00, 0, 100, 0.01));
			sensitivity.setEditor(new JSpinner.NumberEditor(sensitivity, "0.00"));
			LedConfig.customizeSpinner(sensitivity);
			music.add(sensitivity);

			music.add(new JLabel("Minimum Brightness: "));
			min = new SpinSlider(0, 255, 255);
			music.add(min);

		add(general);
		add(Box.createGlue());
		add(calibration);
		add(Box.createGlue());
		add(lighting);
		add(Box.createGlue());
		add(music);
	}

	public void addPreset() {
		preset.addItem("Preset "+(preset.getItemCount()+1));
	}

	public void removePreset() {
		preset.removeItemAt(preset.getItemCount()-1);
	}

	public void load(Settings settings) {
		version.setText(Integer.toString(settings.version.getInt()));
		preset.setSelectedIndex(settings.preset.getInt());
		msgeq7_min.setValue(settings.msgeq7_min.get());
		brightness.setValue(settings.brightness.getInt());
		correction.setValue(settings.correction.getColor());
		temperature.setValue(settings.temperature.getInt());
		frequency.setSelectedIndex(settings.music_frequency.getInt());
		sensitivity.setValue((double)(settings.music_sensitivity.getInt())/100);
		min.setValue(settings.music_min_brightness.getInt());
	}

	public Settings getSettings() {
		Settings settings = new Settings();
		settings.preset.set(preset.getSelectedIndex());
		settings.msgeq7_min.set(msgeq7_min.getValue());
		settings.brightness.set(brightness.getValue());
		settings.correction.set(correction.getValue());
		settings.temperature.set(temperature.getValue());
		settings.music_frequency.set(frequency.getSelectedIndex());
		settings.music_sensitivity.set((int)((Double)sensitivity.getValue()*100));
		settings.music_min_brightness.set(min.getValue());
		return settings;
	}
}
