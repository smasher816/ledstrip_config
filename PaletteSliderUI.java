import java.awt.*;
import java.awt.geom.Point2D;
import javax.swing.JSlider;
import com.smartg.swing.RangeSliderUI;

public class PaletteSliderUI extends RangeSliderUI {
	private static final float fracs[] = {0.0f/15, 1.0f/15, 2.0f/15, 3.0f/15, 4.0f/15, 5.0f/15, 6.0f/15, 7.0f/15, 8.0f/15, 9.0f/15, 10.0f/15, 11.0f/15, 12.0f/15, 13.0f/15, 14.0f/15, 15.0f/15};
	int palettes[][] = {
		{0x0000FF, 0x00008B, 0x00008B, 0x00008B, 0x00008B, 0x00008B, 0x00008B, 0x00008B, 0x0000FF, 0x00008B, 0x87CEEB, 0x87CEEB, 0xADD8E6, 0xFFFFFF, 0xADD8E6, 0x87CEEB},
		{0x000000, 0x800000, 0x000000, 0x800000, 0x8B0000, 0x800000, 0x8B0000, 0x8B0000, 0x8B0000, 0xFF0000, 0xFFA500, 0xFFFFFF, 0xFFA500, 0xFF0000, 0x8B0000, 0x000000},
		{0x191970, 0x00008B, 0x191970, 0x000080, 0x00008B, 0x0000CD, 0x2E8B57, 0x008080, 0x5F9EA0, 0x0000FF, 0x008B8B, 0x6495ED, 0x7FFFD4, 0x2E8B57, 0x00FFFF, 0x87CEFA},
		{0x006400, 0x006400, 0x556B2F, 0x006400, 0x008000, 0x228B22, 0x6B8E23, 0x008000, 0x2E8B57, 0x66CDAA, 0x32CD32, 0x9ACD32, 0x90EE90, 0x7CFC00, 0x66CDAA, 0x228B22},
		{0xFF0000, 0xD52A00, 0xAB5500, 0xAB7F00, 0xABAB00, 0x56D500, 0x00FF00, 0x00D52A, 0x00AB55, 0x0056AA, 0x0000FF, 0x2A00D5, 0x5500AB, 0x7F0081, 0xAB0055, 0xD5002B},
		{0xFF0000, 0x000000, 0xAB5500, 0x000000, 0xABAB00, 0x000000, 0x00FF00, 0x000000, 0x00AB55, 0x000000, 0x0000FF, 0x000000, 0x5500AB, 0x000000, 0xAB0055, 0x000000},
		{0x5500AB, 0x84007C, 0xB5004B, 0xE5001B, 0xE81700, 0xB84700, 0xAB7700, 0xABAB00, 0xAB5500, 0xDD2200, 0xF2000E, 0xC2003E, 0x8F0071, 0x5F00A1, 0x2F00D0, 0x0007F9},
		{0x000000, 0x330000, 0x660000, 0x990000, 0xCC0000, 0xFF0000, 0xFF3300, 0xFF6600, 0xFF9900, 0xFFCC00, 0xFFFF00, 0xFFFF33, 0xFFFF66, 0xFFFF99, 0xFFFFCC, 0xFFFFFF},
	};
	int selectedPalette = 0;

	public PaletteSliderUI(JSlider slider) {
		super(slider);
	}

	void selectPalette(int i) {
		selectedPalette = i;
	}

	public void paintTrack(Graphics g) {
		int palette[] = palettes[selectedPalette];
		Color colors[] = new Color[16];
		for (int i=0; i<16; i++) {
			colors[i] = new Color(palette[i], false);
		}

		Point2D start = new Point2D.Float(trackRect.x, trackRect.y);
		Point2D size = new Point2D.Float(trackRect.width, trackRect.height);
		LinearGradientPaint gradient = new LinearGradientPaint(start, size, fracs, colors);

		Graphics2D g2d = (Graphics2D)g;
		g2d.setPaint(gradient);
		g2d.fillRect(trackRect.x, trackRect.y, trackRect.width, trackRect.height);
	}
	protected void paintTrackRange(Graphics g, Rectangle rect) {
		Color currentColor = g.getColor();
		Color color = new Color(currentColor.getRed(), currentColor.getGreen(), currentColor.getBlue(), 160);
		g.setColor(color);
		super.paintTrackRange(g, rect);
	}
};
