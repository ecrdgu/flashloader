package com.dongguk.ecr.ui.property;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;

import javax.imageio.ImageIO;
import javax.naming.ConfigurationException;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import com.dongguk.ecr.common.config.PropertiesFactory;
import com.dongguk.ecr.constant.PropertiesKeys;
import com.dongguk.ecr.framework.common.config.IProperties;
import com.dongguk.ecr.framework.service.IServiceManager;
import com.dongguk.ecr.framework.service.observe.IObserver;
import com.dongguk.ecr.framework.ui.FileSelectablePanel;
import com.dongguk.ecr.framework.ui.GraphicPalette;
import com.dongguk.ecr.framework.ui.IFileBrowser;
import com.dongguk.ecr.main.SystemTypeLoader;
import com.dongguk.ecr.service.FlashLoaderService;

public class PropertiesSetUpMenu extends GraphicPalette implements IObserver {
	private Integer id;
	private final IServiceManager service;

	private final HashMap<String, ITextEditor> mTextMap = new HashMap<>();

	private final ActionListener actionListener = new ActionListener() {
		private JDialog dialog = new JDialog();
		@Override
		public void actionPerformed(ActionEvent e) {
			dialog.setTitle("configure...");
			dialog.add(getPalette());

			dialog.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
			dialog.setBounds(200, 200, 600, 400);
			dialog.setVisible(true);
		}
	};

	private interface ITextEditor {
		void setText(String str);

		String getText();
	}

	private void initialize() {
		IProperties prop = null;
		try {
			prop = PropertiesFactory.createOrget(FlashLoaderService.APP_CONFIG);
		} catch (ConfigurationException e) {
			e.printStackTrace();
		}

		for(String key : mTextMap.keySet()) {
			try {
				mTextMap.get(key).setText(prop.getString(key));
			} catch (ConfigurationException e) {
				//TODO:
			}
		}
	}

	public PropertiesSetUpMenu(IServiceManager service) {
		setLayout();

		this.service = service;

		/**--------------------------------------------------------------------**/

		final String [] fileItems = {
			PropertiesKeys.getOCDCategory("binary"),
			PropertiesKeys.getConfigCategory("partition"),
			PropertiesKeys.getOCDCategory("conf"),
			PropertiesKeys.getPathCategory("base"),
			PropertiesKeys.getPathCategory("binaries"),
		};

		final String [] strItems = {
			PropertiesKeys.getDeviceCategory("vender_id"),
			PropertiesKeys.getDeviceCategory("product_id"),
			/** TODO: currently, not supported */
			/* PropertiesKeys.getServiceCategory("multi_device"), */
			/* PropertiesKeys.getServiceCategory("max_element"), */
		};

		/**--------------------------------------------------------------------**/

		final FileSelectablePanel filePane = new FileSelectablePanel();

		filePane.setFileSelectionMode(2);

		((JPanel)filePane.getPalette()).
			setBorder(BorderFactory.createTitledBorder("File path"));

		for (final String name : fileItems) {
			filePane.addItem(name, fileBrowseListener(name));

			mTextMap.put(name, new ITextEditor() {
				private String str = null;

				@Override
				public void setText(String str) {
					this.str = str;
					filePane.setText(name, str);
				}

				@Override
				public String getText() {
					return this.str;
				}
			});
		}

		/**--------------------------------------------------------------------**/

		JPanel p = new JPanel();
		p.setLayout(new GridBagLayout());
		p.setBorder(BorderFactory.createTitledBorder("String value"));

		for (String name : strItems) {
			JLabel label = new JLabel(name);
			label.setHorizontalAlignment(JLabel.CENTER);

			final JTextField txtField = new JTextField();
			mTextMap.put(name, new ITextEditor() {
				@Override
				public void setText(String str) { txtField.setText(str); }

				@Override
				public String getText() {
					return txtField.getText();
				}
			});

			addComponent(p, label, gridx++, gridy, 1, 1, 0.1, 0.0,
					wideInsets, GridBagConstraints.CENTER,
					GridBagConstraints.NONE);
			addComponent(p, txtField, gridx++, gridy, 1, 1, 0.9, 0.0,
					wideInsets, GridBagConstraints.LINE_START,
					GridBagConstraints.HORIZONTAL);

			gridx = 0;
			gridy++;

		}

		/**--------------------------------------------------------------------**/

		JButton btn = new JButton("set");
		btn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Map<String, String> configMap = new HashMap<>();

				for (String key : mTextMap.keySet()) {
					String str = mTextMap.get(key).getText();
					configMap.put(key, str);
				}

				signal(configMap);
			}
		});

		/**--------------------------------------------------------------------**/

		addComponent(filePane.getPalette(), 0, 0, 1, 1, 1.0, 0.0,
				noInsets, GridBagConstraints.LINE_START,
				GridBagConstraints.HORIZONTAL);

		addComponent(p, 0, 1, 1, 1, 1.0, 0.0,
				noInsets, GridBagConstraints.LINE_START,
				GridBagConstraints.HORIZONTAL);

		addComponent(btn, 0, gridy++, 1, 1, 0.1, 0.0,
				noInsets, GridBagConstraints.LINE_END,
				GridBagConstraints.NONE);

		/**--------------------------------------------------------------------**/

		initialize();
	}

	public JMenuBar createMenuBar() {
		JMenuBar menuBar = new JMenuBar();

		JMenu menu = new JMenu("File");
		JMenu menu2 = new JMenu("Help");

		/** ------------------------------------------------------------- **/
		{
			menu.setMnemonic(KeyEvent.VK_F);
			menu.getAccessibleContext().setAccessibleDescription(
			        "modifier of user configuration.");
			menuBar.add(menu);

			JMenuItem menuItem;
			menuItem = new JMenuItem("modify user configs..", KeyEvent.VK_T);

			menuItem.setAccelerator(KeyStroke.getKeyStroke(
			        KeyEvent.VK_1, ActionEvent.ALT_MASK));
			menuItem.getAccessibleContext().setAccessibleDescription(
			        "This doesn't really do anything");
			menuItem.addActionListener(actionListener);

			menu.add(menuItem);
		}

		/** ------------------------------------------------------------- **/

		{
			menu2.setMnemonic(KeyEvent.VK_H);
			menu2.getAccessibleContext().setAccessibleDescription(
			        "Contact AS...");

			JMenuItem menuItem = new JMenuItem("contact as..", KeyEvent.VK_I);
			menuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					StringBuffer buf = new StringBuffer();
					buf.append("Jihun Ahn");
					buf.append("\n \n");
					buf.append("Dongguk Univ. ECR `17TH");
					buf.append('\n');
					buf.append("Samsung Strategy & Innovation Center");
					buf.append('\n');
					buf.append("SAMSUNG ELECTRONICS CO., LTD.");
					buf.append("\n \n");
					buf.append("E-Mail: jhun.ahnn@gmail.com");
					buf.append('\n');

			        try {
						JOptionPane.showConfirmDialog(null,
								buf.toString(),
								"Info..",
								JOptionPane.CLOSED_OPTION,
								JOptionPane.QUESTION_MESSAGE,
								/* ICON */
								new ImageIcon(getImage("icons/1492120970_User.png")));
					} catch (HeadlessException | IOException e1) {
						e1.printStackTrace();
					}
				}

				public Image getImage(String path) throws IOException {
					StringBuffer strBuffer = new StringBuffer(path);
					if (!path.startsWith("/")) {
						strBuffer.insert(0, File.separatorChar);
						strBuffer.insert(0, "resource");
						strBuffer.insert(0, File.separatorChar);
						strBuffer.insert(0, "..");
						strBuffer.insert(0, SystemTypeLoader.getClassPath());
					}

					File f = new File(strBuffer.toString());
					if (f.exists())
						return ImageIO.read(f);

					return null;

				}
			});
			menu2.add(menuItem);

			menuBar.add(menu2);
		}
		/** ------------------------------------------------------------- **/

		return menuBar;
	}

	@Override
	public void update(Observable o, Object arg) {
		if (!(arg instanceof Boolean))
			return;

		boolean retCode = (boolean) arg;
		JOptionPane.showMessageDialog(getPalette(), "Setted: " + retCode);
	}

	@Override
	public boolean connect(int id) {
		this.id = id;
		if (service == null)
			return false;

		service.addObserver(id, this);
		return true;
	}


	@Override
	public int getId() {
		return this.id;
	}

	@Override
	public boolean signal(Object o) {
		if (this.id == null) {
			return false;
		}

		service.sendEvent(getId(), o);
		return true;
	}

	private IFileBrowser fileBrowseListener(final String name) {
		return new IFileBrowser() {

			@Override
			public void actionPerformed(Object o) {
				String path = null;
				try {
					path = (o instanceof File)?
							((File)o).getCanonicalPath() : null;
				} catch (IOException e) { }

				mTextMap.get(name).setText(path);
			}
		};
	}

}
