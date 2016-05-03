import java.util.List;
import java.util.ArrayList;

public class Settings extends SettingsGroup {
	public static int VERSION = 2;
	SettingType version = new SettingType(8);
	SettingType presetCount = new SettingType(8);
	SettingType preset = new SettingType(8);
	SettingType msgeq7_min = new SettingType(8);
	SettingType brightness = new SettingType(8);
	CRGBType correction = new CRGBType();
	SettingType temperature = new SettingType(16);
	SettingType music_frequency = new SettingType(8);
	SettingType music_sensitivity = new SettingType(16);
	SettingType music_min_brightness = new SettingType(8);

	List fields() {
		List<SettingType> fields = new ArrayList<SettingType>();
		fields.add(version);
		fields.add(presetCount);
		fields.add(preset);
		fields.add(msgeq7_min);
		fields.add(brightness);
		fields.add(correction);
		fields.add(temperature);
		fields.add(music_frequency);
		fields.add(music_sensitivity);
		fields.add(music_min_brightness);
		return fields;
	}
}
