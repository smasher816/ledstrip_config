import java.awt.*;
import javax.swing.JSlider;
import com.smartg.swing.RangeSliderUI;

public class TemperatureSliderUI extends RangeSliderUI {
	public TemperatureSliderUI(JSlider slider) {
		super(slider);
	}

	public void paintTrack(Graphics g) {
		for (int i=0; i<trackRect.width; i++) {
			double temp = slider.getMaximum()*(double)i/trackRect.width;
			g.setColor(kelvin2color(temp));
			g.drawLine(trackRect.x+i, trackRect.y, trackRect.x+i, trackRect.y+trackRect.height);
		}
	}

	protected void paintTrackRange(Graphics g, Rectangle rect) {
		Color currentColor = g.getColor();
		Color color = new Color(currentColor.getRed(), currentColor.getGreen(), currentColor.getBlue(), 160);
		g.setColor(color);
		super.paintTrackRange(g, rect);
	}

	public static Color kelvin2color(double temp) {
		double r,g,b;
		temp /= 100;
		if (temp <= 66) {
			r = 255;
		} else {
			r = temp - 60;
			r = 329.698727446 * Math.pow(r, -0.1332047592);
			if (r < 0) r = 0;
			if (r > 255) r = 255;
		}
		if (temp <= 66) {
			g = temp;
			g = 99.4708025861 * Math.log(g) - 161.1195681661;
			if (g < 0) g = 0;
			if (g > 255) g = 255;
		} else {
			g = temp - 60;
			g = 288.1221695283 * Math.pow(g, -0.0755148492);
			if (g < 0) g = 0;
			if (g > 255) g = 255;
		}
		if (temp >= 66) {
			b = 255;
		} else {
			b = temp - 10;
			b = 138.5177312231 * Math.log(b) - 305.0447927307;
			if (b < 0) b = 0;
			if (b > 255) b = 255;
		}
		return new Color((int)r,(int)g,(int)b);
	}
}
