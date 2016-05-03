import java.util.List;
import java.util.ArrayList;

public class InputType extends SettingsGroup {
	SettingType type = new SettingType(8);
	SettingType ledstrip = new SettingType(8);
	SettingType min = new SettingType(16);
	SettingType max = new SettingType(16);

	List fields() {
		List<SettingType> fields = new ArrayList<SettingType>();
		fields.add(type);
		fields.add(ledstrip);
		fields.add(min);
		fields.add(max);
		return fields;
	}
}
