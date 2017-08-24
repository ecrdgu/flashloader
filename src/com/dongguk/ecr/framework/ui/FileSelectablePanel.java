package com.dongguk.ecr.framework.ui;

import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

import com.dongguk.ecr.main.SystemTypeLoader;

/**
 * SelectablePane
 * @author jhun.ahn
 *
 */
public class FileSelectablePanel extends GraphicPalette {
	private final HashMap<String, Items> mItemMap;
	private final JFileChooser fc = new JFileChooser();

	public FileSelectablePanel() {
		setLayout();
		this.mItemMap = new HashMap<String, FileSelectablePanel.Items>();
		setCurrentDirectory(new File(SystemTypeLoader.getClassPath()));
	}

	public void setCurrentDirectory(File f) {
		if (f != null && f.exists())
			fc.setCurrentDirectory(f);
	}

	public void setFileSelectionMode(int mode) {
		switch(mode) {
		case JFileChooser.FILES_ONLY:
		case JFileChooser.DIRECTORIES_ONLY:
		case JFileChooser.FILES_AND_DIRECTORIES:
			fc.setFileSelectionMode(mode);
			break;
		default:
			return;
		}
	}

	private void addItem(final String key, IFileBrowser listener, boolean bCheck) {
		Items item = new Items(bCheck);
		item.setName(key);
		item.setListener(listener);

		item.draw(gridx, gridy);

		mItemMap.put(key, item);

		gridy++;
	}

	public void addSelectableItem(String key) {
		addItem(key, null, true);
	}

	public void addSelectableItem(String key, IFileBrowser listener) {
		addItem(key, listener, true);
	}

	public void addItem(String key) {
		addItem(key, null, false);
	}

	public void addItem(String key, IFileBrowser listener) {
		addItem(key, listener, false);
	}

	@Override
	public void clear() {
		mItemMap.clear();
		super.clear();
	}

	public void setText(String key, String str) {
		if (str != null)
			mItemMap.get(key).txtPath.setText(str);
	}

	public void setFilter(FileFilter filter) {
		fc.addChoosableFileFilter(filter);
	}

	public File getFile(String key) {
		return mItemMap.get(key).f;
	}

	public boolean isSelected(String key) {
		Items item = mItemMap.get(key);
		if (item != null)
			return item.btnBrowse.isEnabled();

		return false;
	}

	public void setEnable(String key, boolean b) {
		Items item = mItemMap.get(key);
		if (item != null)
			item.setEnable(b);
	}

	public void setActive(String key, boolean b) {
		Items item = mItemMap.get(key);
		if (item != null)
			item.setActivate(b);
	}

	public void setEnabled(boolean b) {
		Iterator<String> item = mItemMap.keySet().iterator();
		while(item.hasNext()) {
			setEnable(item.next(), b);
		}
	}

	public Iterator<String> getKeys() {
		return mItemMap.keySet().iterator();
	}

	private class Items implements ActionListener {
		private File f;
		private IFileBrowser listener;

		private final JCheckBox ckbSelect;
		private final JButton btnBrowse;
		private final JTextField txtPath;

		public Items(boolean bSelectable) {
			if (!bSelectable) {
				ckbSelect = null;
			} else {
				ckbSelect = new JCheckBox();
				ckbSelect.addActionListener(this);
			}

			btnBrowse = new JButton();
			btnBrowse.addActionListener(this);
			txtPath = new JTextField();
			txtPath.setEditable(false);
		}

		private void setName(String name) {
			btnBrowse.setText(name);
		}

		private void setListener(IFileBrowser l) {
			this.listener = l;
		}

		private void setEnable(boolean bEnable) {
			ckbSelect.setEnabled(bEnable);
			setActivate(bEnable);
		}

		private void setActivate(boolean bEnable) {
			if (!ckbSelect.isSelected())
				bEnable = false;

			txtPath.setEnabled(bEnable);
			btnBrowse.setEnabled(bEnable);
		}

		private void draw(int x, int y) {
			if (ckbSelect != null) {
				addComponent(ckbSelect, x++, y, 1, 1, 0.0D, 0.0D,
						GraphicPalette.noInsets, GridBagConstraints.LINE_START,
						GridBagConstraints.NONE);
			}

			addComponent(btnBrowse, x++, y, 1, 1, 0.1D, 0.0D,
					GraphicPalette.noInsets, GridBagConstraints.LINE_START,
					GridBagConstraints.HORIZONTAL);
			addComponent(txtPath, x, y, 1, 1, 0.9D, 0.0D,
					GraphicPalette.noInsets, GridBagConstraints.LINE_START,
					GridBagConstraints.HORIZONTAL);
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			Object o = arg0.getSource();
			if (!(o instanceof AbstractButton))
				return;

			if (o.equals(btnBrowse)) {
				this.f = select();
				if(listener != null)
					listener.actionPerformed(f);

				if (f != null) {
					fc.setCurrentDirectory(f);
					txtPath.setText(f.getAbsolutePath());
				} else
					txtPath.setText(null);
			} else {
				AbstractButton b = (AbstractButton) o;
				setActivate(b.isEnabled());
			}
		}

		private File select() {
			if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				return file;
			}

			return null;
		}
	}

}
