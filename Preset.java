import java.util.List;
import java.util.ArrayList;

public class Preset extends SettingsGroup {
	public final static int VISUALIZER_COUNT = 2;
	public final static int INPUT_COUNT = 2;
	SettingType visualizers[] = {new SettingType(8), new SettingType(8)};
	InputType inputs[] = {new InputType(), new InputType()};
	SettingType palette = new SettingType(8);
	CRGBType color = new CRGBType();

	List fields() {
		List<SettingType> fields = new ArrayList<SettingType>();
		for (int i=0; i<INPUT_COUNT; i++) {
			fields.addAll(inputs[i].fields());
		}
		for (int i=0; i<VISUALIZER_COUNT; i++) {
			fields.add(visualizers[i]);
		}
		fields.add(palette);
		fields.add(color);
		return fields;
	}
}
