package com.dongguk.ecr.ui.logger;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Observable;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.DefaultCaret;

import com.dongguk.ecr.common.payload.ServiceParams;
import com.dongguk.ecr.constant.EventIds;
import com.dongguk.ecr.constant.ParamKeysEnum;
import com.dongguk.ecr.framework.service.IServiceManager;
import com.dongguk.ecr.framework.service.observe.IObserver;
import com.dongguk.ecr.framework.ui.GraphicPalette;

/**
 * LogMessageTerminal
 * @author jhun.ahn
 *
 */
public class LogMessageTerminal  extends GraphicPalette implements IObserver {
	private final JTextArea txtArea;
	private static final String CLEAR_NAME = "clear";
	private static final String DUMP_NAME = "dump";
	private final IServiceManager service;
	private Integer id;

	private final JFileChooser fc;

	public LogMessageTerminal(IServiceManager service) {
		this.service = service;

		setLayout();

		txtArea = new JTextArea();
		txtArea.setBackground(Color.BLACK);
		txtArea.setForeground(Color.WHITE);
		txtArea.setEditable(false);

		fc = new JFileChooser();

		final JScrollPane scroll = new JScrollPane();
		scroll.setViewportView(txtArea);

		scroll.setBorder(BorderFactory.createEmptyBorder());
		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scroll.setBorder(BorderFactory.createTitledBorder("Message Log"));
		scroll.setAutoscrolls(true);

		JButton btnClear = new JButton(CLEAR_NAME);
		JButton btnDump = new JButton(DUMP_NAME);

		btnClear.setName(CLEAR_NAME);
		btnClear.addActionListener(new ButtonListener());
		btnDump.setName(DUMP_NAME);
		btnDump.addActionListener(new ButtonListener());

		DefaultCaret caret = (DefaultCaret) txtArea.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

		addComponent(scroll, gridx++, gridy++, 3, 1, 1.0D, 1.0D,
				noInsets, GridBagConstraints.LINE_START, GridBagConstraints.BOTH);

		addComponent(btnClear, gridx++, gridy, 1, 1, 0.1D, 0,
				defaultInsets, GridBagConstraints.LINE_END, GridBagConstraints.NONE);

		addComponent(btnDump, gridx++, gridy, 1, 1, 0.0D, 0,
				defaultInsets, GridBagConstraints.LINE_END, GridBagConstraints.NONE);
	}

	@Override
	public void update(Observable arg0, Object arg1) {
		ServiceParams param = null;
		String str = null;
		EventIds cmd = null;

		try {
			if (arg1 instanceof ServiceParams) {
				param = (ServiceParams) arg1;
				cmd = (EventIds) param.getAsIs(ParamKeysEnum.CMD.code);
				str = param.getAsString(ParamKeysEnum.PARAM.code);
			} else if (arg1 instanceof EventIds) {
				cmd = (EventIds) arg1;
			}
		} catch (RuntimeException e) {
			System.err.println("failed to parse argument");
			return;
		}

		if (cmd == null) {
			System.err.println("failed to parse argument");
			return;
		}

		switch (cmd) {
		case UPDATE:
			txtArea.append("User configuration Updated\n");
			break;
		case MSG_APPEND :
			txtArea.append(str);
			txtArea.setAutoscrolls(true);
			break;
		case MSG_CLEAR :
			txtArea.setText("");
			break;
		case MSG_DUMP :
			if (fc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				file.setWritable(true);
				try {
					@SuppressWarnings("resource")
					FileOutputStream os = new FileOutputStream(file);
					StringBuffer strBuf = new StringBuffer();

					strBuf.append("saved time: ");
					strBuf.append(new SimpleDateFormat("yyyy. MM. dd HH:mm:ss").
							format(System.currentTimeMillis()) + '\n');
					strBuf.append("------------------------------------\n");
					strBuf.append(txtArea.getText().getBytes());
					strBuf.append("------------------------------------\n");
					os.write(strBuf.toString().getBytes());
					txtArea.append("Log dump: " + file.getName());

				} catch (FileNotFoundException e) {
					txtArea.append("failed log dump: " + e.getMessage() + '\n');
				} catch (IOException e) {
					txtArea.append("failed log dump: " + e.getMessage() + '\n');
				}

			}
			break;
		default:
			return;
		}

		display();
	}

	private class ButtonListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			AbstractButton btn = (AbstractButton) arg0.getSource();
			String name = btn.getName();

			if (name.equals(CLEAR_NAME)) {
				signal(EventIds.MSG_CLEAR);
			} else if (name.equals(DUMP_NAME)) {
				signal(EventIds.MSG_DUMP);
			}
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
