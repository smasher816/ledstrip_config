import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ColorPanel extends JPanel implements MouseListener {
	private Color color;
	private JColorChooser colorChooser;
	private JDialog colorDialog;

	public ColorPanel(Color color) {
		colorChooser = new JColorChooser();
		colorChooser.setPreviewPanel(new JPanel());
		colorDialog = JColorChooser.createDialog(null, "Color", true, colorChooser,
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					setValue(colorChooser.getColor());
				}
			}, new ActionListener() {
				public void actionPerformed(ActionEvent e) {}
			}
		);

		setValue(color);
		addMouseListener(this);
	}

	public void mousePressed(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
	public void mouseClicked(MouseEvent e) {
		colorDialog.show();
	}

	public void setValue(Color color) {
		this.color = color;
		colorChooser.setColor(color);
		setBackground(color);
	}

	public Color getValue() {
		return color;
	}
}
