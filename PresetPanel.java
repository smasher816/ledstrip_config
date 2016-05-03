import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.plaf.SliderUI;
import com.smartg.swing.JRangeSlider;
import com.smartg.swing.RangeSliderUI;

public class PresetPanel extends JPanel {
	enum InputItems {
		NONE,
		BRIGHTNESS,
		TEMPERATURE,
		PALETTE,
		HUE,
		CYCLE,
		BLINK,
		SENSITIVITY
	}

	enum BaseItems {
		STATIC,
		CYCLE,
	}

	enum ModItems {
		NONE,
		BLINK,
		MUSIC,
	}

	static final BaseItems baseItems[] = BaseItems.values();
	static final InputItems inputItems[] = InputItems.values();
	static final ModItems modItems[] = ModItems.values();

	JComboBox visualizers[] = new JComboBox[Preset.VISUALIZER_COUNT];
	InputPanel inputs[] = new InputPanel[Preset.VISUALIZER_COUNT];
	ColorPanel color;
	JComboBox palette;
	JPanel colorPanel, palettePanel;

	public PresetPanel() {
		super();
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		String[] baseNames = {"Static", "Cycle"};
		String[] modNames = {"None", "Blink", "Music"};
		String[] paletteNames = {"Cloud", "Lava", "Ocean", "Forest", "Rainbow", "Rainbow (Striped)", "Party", "Heat"};

		DefaultListCellRenderer dlcr = new DefaultListCellRenderer();
		dlcr.setHorizontalAlignment(DefaultListCellRenderer.CENTER);

		JPanel visualizer = new JPanel();
		visualizer.setLayout(new BoxLayout(visualizer, BoxLayout.Y_AXIS));
		visualizer.setBorder(BorderFactory.createTitledBorder("Visualizer"));

		visualizers[0] = new JComboBox(baseNames);
		visualizers[0].setRenderer(dlcr);
		visualizers[0].addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					calculateColorOrPaletteVisible();
					baseChanged();
				}
			}
		});
		visualizer.add(createOption("Base", visualizers[0]));

		for (int i=1; i<Preset.VISUALIZER_COUNT; i++) {
			final int index = i;
			visualizers[i] = new JComboBox(modNames);
			visualizers[i].setRenderer(dlcr);
			visualizers[i].addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						calculateColorOrPaletteVisible();
						modChanged(index);
					}
				}
			});
			visualizer.add(createOption("Modifier", visualizers[i]));
		}

		palette = new JComboBox(paletteNames);
		palette.setRenderer(dlcr);
		palette.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					paletteChanged();
				}
			}
		});
		palettePanel = createOption("Palette", palette);
		visualizer.add(palettePanel);

		color = new ColorPanel(Color.white);
		color.setPreferredSize(palette.getPreferredSize());
		colorPanel = createOption("Color", color);
		visualizer.add(colorPanel);

		add(visualizer);

		for (int i=0; i<Preset.INPUT_COUNT; i++) {
			inputs[i] = new InputPanel(i);
			add(Box.createGlue());
			add(inputs[i]);
		}

		baseChanged();
		modChanged(0);
		modChanged(1);
		inputChanged(0);
		inputChanged(1);
		calculateColorOrPaletteVisible();
	}

	private class InputPanel extends JPanel {
		DisabledItemsComboBox type;
		MinMaxSlider range;
		JComboBox ledstrip;
		JPanel ledstripPanel;

		RangeSliderUI normalSlider;
		RangeSliderUI hueSlider;
		TemperatureSliderUI temperatureSlider;
		PaletteSliderUI paletteSlider;

		public InputPanel(final int i) {
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			setBorder(BorderFactory.createTitledBorder("Input "+(i+1)));

			String[] inputNames = {"None", "Brightness", "Temperature", "Palette", "Hue", "Cycle Speed", "Blink Speed", "Music Sensitivity"};
			String[] ledstripNames = {"Both", "Left", "Right"};

			DefaultListCellRenderer dlcr = new DefaultListCellRenderer();
			dlcr.setHorizontalAlignment(DefaultListCellRenderer.CENTER);

			type = new DisabledItemsComboBox(inputNames);
			((DefaultListCellRenderer)type.getRenderer()).setHorizontalAlignment(DefaultListCellRenderer.CENTER);
			type.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						calculateColorOrPaletteVisible();
						inputChanged(i);
					}
				}
			});
			add(createOption("Type", type));

			ledstrip = new JComboBox(ledstripNames);
			ledstrip.setRenderer(dlcr);
			ledstripPanel = createOption("Ledstrip", ledstrip);
			add(ledstripPanel);

			range = new MinMaxSlider(0,255, 50, 200);
			JSlider realSlider = range.getRangeSlider().getSlider();
			normalSlider = new RangeSliderUI(realSlider);
			paletteSlider = new PaletteSliderUI(realSlider);
			temperatureSlider = new TemperatureSliderUI(realSlider);
			hueSlider = new RangeSliderUI(realSlider) {
				public void paintTrack(Graphics g) {
					for (int i=0; i<trackRect.width; i++) {
						float hue = (float)i/trackRect.width;
						g.setColor(Color.getHSBColor(hue, 1, 1));
						g.drawLine(trackRect.x+i, trackRect.y, trackRect.x+i, trackRect.y+trackRect.height);
					}
				}
				protected void paintTrackRange(Graphics g, Rectangle rect) {
					Color currentColor = g.getColor();
					Color color = new Color(currentColor.getRed(), currentColor.getGreen(), currentColor.getBlue(), 64);
					g.setColor(color);
					super.paintTrackRange(g, rect);
				}
			};
			realSlider.setUI(normalSlider);
			add(createOption("Range", range));
		}
	}

	JPanel createOption(String label, Component c) {
		JPanel row = new JPanel(new GridLayout(0,2,5,5));
		row.add(new JLabel(label+": "));
		row.add(c);
		return row;
	}

	void calculateColorOrPaletteVisible() {
		boolean hasHueInput = false;
		boolean hasPaletteInput = false;
		for (int i=0; i<Preset.INPUT_COUNT; i++) {
			InputItems item = inputItems[inputs[i].type.getSelectedIndex()];
			if (item == InputItems.HUE) {
				hasHueInput = true;
			}
			if (item == InputItems.PALETTE) {
				hasPaletteInput = true;
			}
		}

		boolean showPalette = false;
		if (hasHueInput) {
			colorPanel.setVisible(false);
			palettePanel.setVisible(false);
		} else {
			BaseItems item = baseItems[visualizers[0].getSelectedIndex()];
			if (item == BaseItems.CYCLE) {
				showPalette = true;
			} else {
				showPalette = hasPaletteInput;
			}
			palettePanel.setVisible(showPalette);
			colorPanel.setVisible(!showPalette);
		}
	}

	void baseChanged() {
		BaseItems item = baseItems[visualizers[0].getSelectedIndex()];
		for (int i=0; i<Preset.INPUT_COUNT; i++) {
			inputs[i].type.setItemDisabled(InputItems.CYCLE.ordinal(), item!=BaseItems.CYCLE);
		}
	}

	void modChanged(int index) {
		ModItems item = modItems[visualizers[index].getSelectedIndex()];
		for (int i=0; i<Preset.INPUT_COUNT; i++) {
			inputs[i].type.setItemDisabled(InputItems.BLINK.ordinal(), item!=ModItems.BLINK);
			inputs[i].type.setItemDisabled(InputItems.SENSITIVITY.ordinal(), item!=ModItems.MUSIC);
		}
	}

	void paletteChanged() {
		for (int i=0; i<Preset.INPUT_COUNT; i++) {
				inputs[i].paletteSlider.selectPalette(palette.getSelectedIndex());
				inputs[i].range.repaint();
		}
	}

	void inputChanged(int i) {
		MinMaxSlider slider = inputs[i].range;
		JSlider realSlider = slider.getRangeSlider().getSlider();

		InputItems item = inputItems[inputs[i].type.getSelectedIndex()];
		if (item!=InputItems.NONE) {
			slider.setVisible(true);
		}
		if (item!=InputItems.HUE) {
			realSlider.setUI(inputs[i].normalSlider);
		}
		if (item!=InputItems.PALETTE && item!=InputItems.HUE) {
			inputs[i].ledstripPanel.setVisible(false);
		}
		switch (item) {
			case NONE:
				slider.setVisible(false);
				break;
			case TEMPERATURE:
				realSlider.setUI(inputs[i].temperatureSlider);
				slider.setRange(0,10000,100);
				slider.setTickSpacing(0,0);
				break;
			case PALETTE:
				realSlider.setUI(inputs[i].paletteSlider);
				inputs[i].ledstripPanel.setVisible(true);
				palettePanel.setVisible(true);
				slider.setRange(0,255,1);
				slider.setTickSpacing(0,0);
				break;
			case HUE:
				realSlider.setUI(inputs[i].hueSlider);
				slider.setRange(0,255,1);
				slider.setTickSpacing(0,0);
				inputs[i].ledstripPanel.setVisible(true);
				break;
			case CYCLE:
				slider.setRange(0,1000,10);
				slider.setTickSpacing(250,50);
				break;
			case BLINK:
				slider.setRange(0,1000,10);
				slider.setTickSpacing(250,50);
				break;
			case SENSITIVITY:
				slider.setRange(0,500,10);
				slider.setTickSpacing(100,50);
				break;
			default:
				slider.setRange(0,255,1);
				slider.setTickSpacing(64,16);
		}
		slider.repaint();
	}

	public void load(Preset preset) {
		visualizers[0].setSelectedIndex(preset.visualizers[0].getInt());
		for (int i=1; i<Preset.VISUALIZER_COUNT; i++) {
			visualizers[i].setSelectedIndex(preset.visualizers[i].getInt() - BaseItems.values().length);
		}
		for (int i=0; i<Preset.INPUT_COUNT; i++) {
			inputs[i].type.setSelectedIndex(preset.inputs[i].type.getInt());
			inputs[i].ledstrip.setSelectedIndex((byte)preset.inputs[i].ledstrip.getInt()+1);
			inputs[i].range.setLowValue(preset.inputs[i].min.getInt());
			inputs[i].range.setHighValue(preset.inputs[i].max.getInt());
		}
		color.setValue((Color)preset.color.getColor());
		palette.setSelectedIndex(preset.palette.getInt());
	}

	public Preset getPreset() {
		Preset preset = new Preset();
		preset.visualizers[0].set(visualizers[0].getSelectedIndex());
		for (int i=1; i<Preset.VISUALIZER_COUNT; i++) {
			preset.visualizers[i].set(BaseItems.values().length + visualizers[i].getSelectedIndex());
		}
		for (int i=0; i<Preset.INPUT_COUNT; i++) {
			preset.inputs[i].type.set(inputs[i].type.getSelectedIndex());
			preset.inputs[i].ledstrip.set(inputs[i].ledstrip.getSelectedIndex()-1);
			preset.inputs[i].min.set(inputs[i].range.getLowValue());
			preset.inputs[i].max.set(inputs[i].range.getHighValue());
		}
		preset.color.set(color.getValue());
		preset.palette.set(palette.getSelectedIndex());
		return preset;
	}
}
