package com.dongguk.ecr.ui.option;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;

import javax.naming.ConfigurationException;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.filechooser.FileFilter;

import com.dongguk.ecr.common.config.PropertiesFactory;
import com.dongguk.ecr.common.payload.ServiceParams;
import com.dongguk.ecr.common.response.SynchResponseHelper.ResponseHolder;
import com.dongguk.ecr.constant.EventIds;
import com.dongguk.ecr.constant.ParamKeysEnum;
import com.dongguk.ecr.constant.PropertiesKeys;
import com.dongguk.ecr.framework.common.config.IProperties;
import com.dongguk.ecr.framework.service.IServiceManager;
import com.dongguk.ecr.framework.service.observe.IObserver;
import com.dongguk.ecr.framework.ui.FileSelectablePanel;
import com.dongguk.ecr.framework.ui.GraphicPalette;
import com.dongguk.ecr.service.FlashLoaderService;
import com.dongguk.ecr.service.config.PartitionInformation;

/**
 * FusingOptionPanel
 *
 * @author jhun.ahn
 *
 */
public class FusingOptionPanel extends GraphicPalette implements IObserver {

	private final JScrollPane scroll;
	private FileSelectablePanel pane;
	private final AbstractButton btnSubmit;

	private static final String ext = "bin";
	private static final String SUBMIT = "submit";
	private static final String CANCEL = "cancel";

	private List<PartitionInformation> partitionList;

	private final PrintStream logger;
	private final IServiceManager service;
	private Integer id;

	private ResponseHolder respHolder = new ResponseHolder() {

		@Override
		public boolean isMatch(Object data) {
			if (!(data instanceof Boolean))
				return false;

			return (Boolean)data;
		}
	};

	public FusingOptionPanel(IServiceManager service) {

		this.service = service;

		setLayout(new GridBagLayout());

		logger = FlashLoaderService.getInstance().getLogger();

		pane = new FileSelectablePanel();
		pane.setFileSelectionMode(0);

		pane.setFilter(new FileFilter() {

			@Override
			public String getDescription() {
				return String.format("binary file data(*.%s)", ext);
			}

			@Override
			public boolean accept(File arg0) {
				return arg0.isDirectory() ? true : arg0.getName().toLowerCase().endsWith("." + ext);
			}
		});

		scroll = new JScrollPane(pane.getPalette());
		scroll.setBorder(BorderFactory.createEmptyBorder());
		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

		scroll.setBorder(BorderFactory.createTitledBorder("Options"));

		btnSubmit = new JToggleButton(SUBMIT, false);
		btnSubmit.addActionListener(new ActionListener() {

			private void add() {
				if (partitionList == null) {
					logger.println("param List is empty");
					return;
				}

				Iterator<PartitionInformation> it = partitionList.iterator();
				while (it.hasNext()) {
					PartitionInformation opt = it.next();
					String desc = opt.getDesc();

					if (!pane.isSelected(desc)) {
						continue;
					}

					opt.setBinary(pane.getFile(desc));
				}

				ServiceParams param = new ServiceParams();
				param.set(ParamKeysEnum.CMD.code, EventIds.FUSING_ITEM_ADDED);
				param.set(ParamKeysEnum.PARAM.code, partitionList);
				signal(param);
			}

			private void submit(boolean b) {
				ServiceParams param = new ServiceParams();
				param.set(ParamKeysEnum.CMD.code,
						b ? EventIds.FUSING_PROCESS_START : EventIds.FUSING_PROCESS_TERMINATE);
				signal(param);
			}

			@Override
			public void actionPerformed(ActionEvent arg0) {
				final Object o = arg0.getSource();
				if (!(o instanceof AbstractButton))
					return;
				boolean isSelect = ((AbstractButton) o).isSelected();
				boolean retCode = true;

				if (isSelect) {
					add();
					if (!respHolder.isResponsed(1000))
						return;

					retCode = (Boolean)respHolder.getReponse();
				}

				if (retCode)
					submit(isSelect);

				setBtnStatus(!isSelect);
				display();
			}
		});

		addComponent(scroll, 0, 0, 3, 1, 1.0, 1.0, noInsets, GridBagConstraints.LINE_START, GridBagConstraints.BOTH);
		addComponent(btnSubmit, 2, 1, 1, 1, 0.0D, 0.0D, GraphicPalette.spaceInsets, GridBagConstraints.LINE_END,
				GridBagConstraints.NONE);

		load();
	}

	private void load() {
		IProperties prop;
		String path = null;
		try {
			prop = PropertiesFactory.createOrget(FlashLoaderService.APP_CONFIG);
			path = prop.getString(PropertiesKeys.getPathCategory("binaries"));
		} catch (ConfigurationException e) {
		}

		pane.setCurrentDirectory(new File(path));
	}

	@Override
	public void clear() {
		pane.clear();
	}

	@Override
	public void display() {
		pane.display();
	}

	@Override
	public void update(Observable arg0, Object arg1) {
		EventIds cmd = null;
		ServiceParams params = null;
		Object o = null;
		boolean retCode = false;

		if (arg1 instanceof ServiceParams) {
			params = (ServiceParams) arg1;
			cmd = (EventIds) params.getAsIs(ParamKeysEnum.CMD.code);
		} else if (arg1 instanceof EventIds) {
			cmd = (EventIds) arg1;
		}

		if (cmd == null)
			return;

		switch (cmd) {
		case UPDATE:
			load();
			break;
		case FUSING_INIT_MAP:
			if (params == null)
				return;

			clear();

			retCode = params.getAsBoolean(ParamKeysEnum.RETCODE.code);
			if (!retCode)
				return;

			o = params.getAsIs(ParamKeysEnum.PARAM.code);
			if (!(o instanceof ArrayList<?>))
				return;

			this.partitionList = new ArrayList<PartitionInformation>();

			List<?> list = (List<?>) o;
			Iterator<?> it = list.iterator();
			while (it.hasNext()) {
				Object e = it.next();
				if (e instanceof PartitionInformation) {
					PartitionInformation param = (PartitionInformation) e;
					String desc = param.getDesc();

					pane.addSelectableItem(desc);
					pane.setActive(desc, false);

					partitionList.add(param);
				}
			}

			break;
		case FUSING_ITEM_ADDED:
			retCode = params.getAsBoolean(ParamKeysEnum.RETCODE.code);
			respHolder.setResponse(retCode);
			break;
		case FUSING_PROCESS_START:
		case FUSING_PROCESS_STOP:
			if (params == null)
				return;

			receiveAck(params);
			break;
		case FUSING_ITEM_CLEAR:
			clear();
			this.gridy = 0;
			break;
		case FUSING_PROCESS_TERMINATE:
			/** wait For FUSING_PROCESS_STOP */
		default:
			return;
		}

		pane.display();
	}

	private void receiveAck(ServiceParams params) {
		EventIds cmd = (EventIds) params.getAsIs(ParamKeysEnum.CMD.code);
		boolean retCode = false;

		try {
			retCode = params.getAsBoolean(ParamKeysEnum.RETCODE.code);
			String msg = params.getAsString(ParamKeysEnum.MESSAGE.code);
			if (msg != null)
				logger.printf("%s: %s\n", cmd.name, msg);

		} catch (RuntimeException e1) {

		}

		boolean bFlag = cmd == EventIds.FUSING_PROCESS_START;

		if (retCode) {
			setBtnStatus(bFlag);
			pane.setEnabled(!bFlag);
		}

		display();
	}

	private void setBtnStatus(boolean b) {
		synchronized (btnSubmit) {
			btnSubmit.setText(b ? CANCEL : SUBMIT);
			btnSubmit.setSelected(b);
		}
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
}
