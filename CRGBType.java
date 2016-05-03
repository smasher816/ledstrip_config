import java.awt.Color;

public class CRGBType extends SettingType {
	Color color;

	CRGBType() {
		super(24);
	}

	void set(Color c) {
		this.color = c;
		if (c!=null) {
			int r = (c.getRed() & 0xFF);
			int g = (c.getGreen() & 0xFF);
			int b = (c.getBlue() & 0xFF);
			value = (int)((b<<16 | g<<8 | r) & 0xFFFFFF);
			//value = c.getRGB();
		}
	}

	@Override
	void set(Object obj) {
		int c = (Integer)obj;
		int r = (c & 0x0000FF) >> 0;
		int g = (c & 0x00FF00) >> 8;
		int b = (c & 0xFF0000) >> 16;
		set(new Color(r,g,b));
	}

	Color getColor() {
		return color;
	}
}
