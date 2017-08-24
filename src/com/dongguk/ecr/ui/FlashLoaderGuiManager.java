package com.dongguk.ecr.ui;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Observer;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;

import com.dongguk.ecr.constant.GUIConstant;
import com.dongguk.ecr.constant.ModulesEnum;
import com.dongguk.ecr.framework.service.IServiceManager;
import com.dongguk.ecr.framework.service.observe.IObserver;
import com.dongguk.ecr.framework.ui.GraphicPalette;
import com.dongguk.ecr.service.FlashLoaderService;
import com.dongguk.ecr.ui.config.ConfigLoadPanel;
import com.dongguk.ecr.ui.connection.ConnectionPanel;
import com.dongguk.ecr.ui.logger.LogMessageTerminal;
import com.dongguk.ecr.ui.option.FusingOptionPanel;
import com.dongguk.ecr.ui.property.PropertiesSetUpMenu;
import com.dongguk.ecr.ui.status.StatusTabbedPanel;

/**
 * FlashLoaderGuiManager
 * @author jhun.ahn
 *
 */
public class FlashLoaderGuiManager implements IServiceManager {

	private final HashMap<ModulesEnum, GraphicPalette> mCompMap;
	private static FlashLoaderGuiManager instance;

	private final JPanel panel = new JPanel();
	private final Thread t = new Thread() {
		@Override
		public void run() {
			final JFrame frame = new JFrame("FlashLoader for TizenRT");

			/**--------------------------------------------------------------------**/
			PropertiesSetUpMenu popUp =
					(PropertiesSetUpMenu)mCompMap.get(ModulesEnum.PROPERTY);
			frame.setJMenuBar(popUp.createMenuBar());
			/**--------------------------------------------------------------------**/

			frame.add(panel);
			frame.pack();

			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

			Dimension panelSize = panel.getPreferredSize();
			frame.setBounds(50, 50, panelSize.width, panelSize.height);
			frame.setResizable(false);

			frame.setVisible(true);
		}
	};

	public static FlashLoaderGuiManager getinstance() throws Exception {
		if (instance == null) {
			instance = new FlashLoaderGuiManager();
		}

		return instance;
	}

	public static void setStype() throws Exception {
		UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
	}

	public static void setFontStyle() {
		Enumeration<Object> enume = UIManager.getDefaults().keys();
		while (enume.hasMoreElements()) {
			Object obj = enume.nextElement();
			if (obj.toString().indexOf("font") != -1) {
				UIManager.put(obj.toString(), GUIConstant.systemFont);
			}

			/**
			 * TODO: color scheme
			 * if (obj.toString().indexOf("background") != -1) {
			 * UIManager.put(obj.toString(), Color.DARK_GRAY);
			 * }
			 */
		}
	}

	private FlashLoaderGuiManager() throws Exception {
		this.mCompMap = new HashMap<ModulesEnum, GraphicPalette>();
		for (ModulesEnum m : ModulesEnum.values()) {
			GraphicPalette o = null;
			switch (m) {
			case CONFIG:
				o = new ConfigLoadPanel(this);
				break;
			case CONNECTION:
				o = new ConnectionPanel(this);
				break;
			case FUSING:
				o = new FusingOptionPanel(this);
				break;
			case STATUS:
				o = new StatusTabbedPanel(this);
				break;
			case LOG_MSG:
				o = new LogMessageTerminal(this);
				break;
			case PROPERTY:
				o = new PropertiesSetUpMenu(this);
				break;
			case COMMON:
				continue;
			}

			o.setName(m.toString());

			if (o instanceof IObserver)
				((IObserver )o).connect(m.code);

			mCompMap.put(m, (GraphicPalette) o);
		}

	}

	@Override
	public boolean initialize() {
		int inset = 5;
		int x = inset, y = inset;
		panel.setName("FlashLoader");
		panel.setLayout(null);

		GraphicPalette gp;

		/**--------------------------------------------------------------------**/

		ImagePanel logoImage = new ImagePanel("images/ecr.gif", true);
		logoImage.setName("Img");
		logoImage.setPreferredSize(GUIConstant.IMAGE_SIZE);
		panel.add(logoImage);
		logoImage.setBounds(x, y, GUIConstant.IMAGE_SIZE.width, GUIConstant.IMAGE_SIZE.height);
		y = GUIConstant.IMAGE_SIZE.height + inset;

		/**--------------------------------------------------------------------**/

		JPanel p = new JPanel();
		{
			p.setName("Device & Config");
			p.setLayout(new GridLayout(1,0));

			gp = mCompMap.get(ModulesEnum.CONNECTION);
			JComponent connectPanel = (JComponent) gp.getPalette();
			connectPanel.setBorder(BorderFactory.createTitledBorder("Device"));
			gp.setName("device");

			gp = mCompMap.get(ModulesEnum.CONFIG);
			JComponent cfgPanel = (JComponent) gp.getPalette();
			cfgPanel.setBorder(BorderFactory.createTitledBorder("configuration"));
			gp.setName("config");

			p.add(connectPanel);
			p.add(cfgPanel);
		}

		p.setPreferredSize(GUIConstant.DEVICE_CONF_SIZE);
		panel.add(p);
		p.setBounds(x, y, GUIConstant.DEVICE_CONF_SIZE.width, GUIConstant.DEVICE_CONF_SIZE.height);
		y += GUIConstant.DEVICE_CONF_SIZE.height + inset;

		/**--------------------------------------------------------------------**/

		gp = mCompMap.get(ModulesEnum.STATUS);
		JComponent jobQuePanel = (JComponent) gp.getPalette();
		jobQuePanel.setPreferredSize(GUIConstant.STATUS_SIZE);
		panel.add(jobQuePanel);
		jobQuePanel.setBounds(x, y, GUIConstant.STATUS_SIZE.width, GUIConstant.STATUS_SIZE.height);
		y += GUIConstant.STATUS_SIZE.height + inset;

		/**--------------------------------------------------------------------**/

		gp = mCompMap.get(ModulesEnum.LOG_MSG);
		JComponent msgPanel = (JComponent) gp.getPalette();
		msgPanel.setPreferredSize(GUIConstant.LOGGER_SIZE);
		panel.add(msgPanel);
		msgPanel.setBounds(x, y, GUIConstant.LOGGER_SIZE.width, GUIConstant.LOGGER_SIZE.height);
		x = GUIConstant.LOGGER_SIZE.width;

		/**--------------------------------------------------------------------**/

		gp = mCompMap.get(ModulesEnum.FUSING);
		JComponent optPanel = (JComponent) gp.getPalette();
		optPanel.setPreferredSize(GUIConstant.OPTION_SIZE);
		panel.add(optPanel);
		optPanel.setBounds(x, y, GUIConstant.OPTION_SIZE.width, GUIConstant.OPTION_SIZE.height);

		/**--------------------------------------------------------------------**/

		x += GUIConstant.OPTION_SIZE.width;
		y += GUIConstant.OPTION_SIZE.height + 40;
		panel.setPreferredSize(new Dimension(x + inset, y + inset));
		return true;
	}

	public void start() {
		t.start();
	}

	@Override
	public boolean addObserver(int id, Observer o) {
		IServiceManager service = FlashLoaderService.getInstance();
		return service.addObserver(id, o);
	}

	@Override
	public PrintStream getLogger() {
		IServiceManager service = FlashLoaderService.getInstance();
		return service.getLogger();
	}

	@Override
	public void sendEvent(int id, Object arg) {
		IServiceManager service = FlashLoaderService.getInstance();
		service.sendEvent(id, arg);
	}

	@Override
	public Object get(Object id) {
		return mCompMap.get(id);
	}
}
