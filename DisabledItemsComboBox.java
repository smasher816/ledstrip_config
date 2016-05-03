// Based upon https://raginggoblin.wordpress.com/2010/05/04/jcombobox-with-disabled-items/

import java.awt.Component;
import java.awt.Color;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JLabel;
import javax.swing.UIManager;
import javax.swing.DefaultListCellRenderer;

public class DisabledItemsComboBox extends JComboBox {

	public DisabledItemsComboBox() {
		super();
		setRenderer(new DisabledItemsRenderer());
	}

	public DisabledItemsComboBox(String[] items) {
		super(items);
		setRenderer(new DisabledItemsRenderer());
	}

	private Set disabled_items = new HashSet();

	public void setItemDisabled(int index, boolean disabled) {
		if (disabled) {
			disabled_items.add(index);
			if (index == getSelectedIndex()) {
				setSelectedIndex(0);
				/*while (--index > 0) {
					if (!disabled_items.contains(index)) {
						setSelectedIndex(index);
						break;
					}
				}*/
			}
		} else {
			disabled_items.remove(index);
		}
	}

	public void addItem(Object anObject, boolean disabled) {
		super.addItem(anObject);
		if (disabled) {
			disabled_items.add(getItemCount() - 1);
		}
	}

	@Override
	public void removeAllItems() {
		super.removeAllItems();
		disabled_items = new HashSet();
	}

	@Override
	public void removeItemAt(final int anIndex) {
		super.removeItemAt(anIndex);
		disabled_items.remove(anIndex);
	}

	@Override
	public void removeItem(final Object anObject) {
		for (int i = 0; i<getItemCount(); i++) {
			if (getItemAt(i) == anObject) {
				disabled_items.remove(i);
			}
		}
		super.removeItem(anObject);
	}

	@Override
	public void setSelectedIndex(int index) {
		if (!disabled_items.contains(index)) {
			super.setSelectedIndex(index);
		}
	}

	private class DisabledItemsRenderer extends DefaultListCellRenderer {
		@Override
		public Component getListCellRendererComponent(JList list,
				Object value,
				int index,
				boolean isSelected,
				boolean cellHasFocus) {

			if (disabled_items.contains(index)) {
				setBackground(list.getBackground());
				Color disabledColor = UIManager.getColor("ComboBox.disabledForeground");
				if (disabledColor == null) {
					Color c = list.getForeground();
					disabledColor = new Color(c.getRed(), c.getGreen(), c.getBlue(), 128);
				}
				setForeground(disabledColor);
			} else if (isSelected) {
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
			} else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			}
			setFont(list.getFont());
			setText((value == null) ? "" : value.toString());
			return this;
		}
	}
}
