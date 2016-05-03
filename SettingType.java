public class SettingType {
	int size;
	Object value;

	SettingType(int size) {
		this.size = size;
	}

	void set(Object value) {
		this.value = value;
	}

	Object get() {
		return value;
	}

	int getInt() {
		return (Integer)value;
	}

	int size() {
		return size;
	}

	int max() {
		return 1<<size;
	}
}
