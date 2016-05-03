import java.util.List;

public abstract class SettingsGroup {
	abstract List fields();

	int size() {
		int size = 0;
		List<SettingType> fields = fields();
		for (SettingType field : fields) {
			size += field.size();
		}
		return size;
	}
}
