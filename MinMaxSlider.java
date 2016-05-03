import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import com.smartg.swing.JRangeSlider;

public class MinMaxSlider extends JPanel {
	JSpinner input_min, input_max;
	JRangeSlider input_range;

	public MinMaxSlider(int min, int max, int lowVal, int highVal) {
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

		input_min = new JSpinner(new SpinnerNumberModel(lowVal, min, max, 1));
		LedConfig.customizeSpinner(input_min);
		input_min.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				input_range.setValue((Integer)input_min.getValue());
			}
		});

		input_max = new JSpinner(new SpinnerNumberModel(highVal, min, max, 1));
		LedConfig.customizeSpinner(input_max);
		input_max.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				input_range.setSecondValue((Integer)input_max.getValue());
			}
		});

		input_range = new JRangeSlider(min, max, lowVal, highVal-lowVal);
		input_range.setMinorTickSpacing(16);
		input_range.setMajorTickSpacing(64);
		input_range.setPaintTicks(true);
		input_range.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				input_min.setValue(input_range.getValue());
				input_max.setValue(input_range.getSecondValue());
			}
		});

		add(input_min);
		add(input_range);
		add(input_max);
	}

	public int getLowValue() {
		return (Integer)input_min.getValue();
	}

	public int getHighValue() {
		return (Integer)input_max.getValue();
	}

	public void setLowValue(int value) {
		input_min.setValue(value);
		input_range.setValue(value);
	}

	public void setHighValue(int value) {
		input_max.setValue(value);
		input_range.setSecondValue(value);
	}

	public void setRange(int min, int max) {
		SpinnerNumberModel minModel = (SpinnerNumberModel)input_min.getModel();
		minModel.setMinimum(min);
		minModel.setMaximum(max);
		SpinnerNumberModel maxModel = (SpinnerNumberModel)input_max.getModel();
		maxModel.setMinimum(min);
		maxModel.setMaximum(max);
		input_range.setMinimum(min);
		input_range.setMaximum(max);
	}

	public void setRange(int min, int max, int step) {
		setRange(min, max);
		SpinnerNumberModel minModel = (SpinnerNumberModel)input_min.getModel();
		minModel.setStepSize(step);
		SpinnerNumberModel maxModel = (SpinnerNumberModel)input_max.getModel();
		maxModel.setStepSize(step);
	}

	public void setTickSpacing(int major, int minor) {
		input_range.setMajorTickSpacing(major);
		input_range.setMinorTickSpacing(minor);
	}

	public JRangeSlider getRangeSlider() {
		return input_range;
	}
}
