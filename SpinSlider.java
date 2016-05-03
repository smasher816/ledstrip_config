import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;

public class SpinSlider extends JPanel {
	JSlider slider;
	JSpinner spinner;

	public SpinSlider(int min, int max, int val) {
		this(min, max, val, 1);
		slider.setMinorTickSpacing(16);
		slider.setMajorTickSpacing(64);
	}

	public SpinSlider(int min, int max, int val, int step) {
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

		slider = new JSlider(min, max, val);
		slider.setPaintTicks(true);
		slider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				spinner.setValue(slider.getValue());
			}
		});

		spinner = new JSpinner(new SpinnerNumberModel(val, min, max, step));
		LedConfig.customizeSpinner(spinner);
		spinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				slider.setValue((Integer)spinner.getValue());
			}
		});

		add(slider);
		add(spinner);
	}

	public int getValue() {
		return (Integer)spinner.getValue();
	}

	public void setValue(int value) {
		spinner.setValue(value);
	}

	public void setTickSpacing(int major, int minor) {
		slider.setMajorTickSpacing(major);
		slider.setMinorTickSpacing(minor);
	}

	public JSlider getSlider() {
		return slider;
	}

	public JSpinner getSpinner() {
		return spinner;
	}
}
