package com.dongguk.ecr.ui.status;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.swing.DefaultButtonModel;
import javax.swing.JButton;

import com.dongguk.ecr.framework.ui.GraphicPalette;
import com.dongguk.ecr.service.status.StatusEnum;

/**
 * GraphicStatusPanel
 * @author jhun.ahn
 *
 */
class GraphicStatusPanel extends GraphicPalette {

	private final List<GraphicElement> mStatusButtList;
	private final HashMap<String, GraphicElement> mStatusMap;

	public GraphicStatusPanel(int max) {
		setLayout(new GridLayout(1,0,0,0));

		mStatusMap = new HashMap<String, GraphicElement>();
		mStatusButtList = new ArrayList<GraphicElement>();

		init(max);

		display();
	}

	public void setName(String name) {
		getPalette().setName(name);
	}

	public void add(String name, StatusEnum status) {
		int size = mStatusMap.size();
		GraphicElement gE = mStatusButtList.get(size);
		gE.setName(name);
		gE.setStatus(status);
		gE.setToolTipText(name);
		mStatusMap.put(name, gE);
	}

	public boolean change(String name, StatusEnum status) {
		GraphicElement gE = mStatusMap.get(name);
		if (gE == null)
			return false;

		gE.setStatus(status);
		return true;
	}

	public boolean remove(String name) {
		GraphicElement gE = mStatusMap.get(name);
		if (gE == null)
			return false;

		mStatusMap.remove(name);

		if (!mStatusButtList.contains(gE))
			return false;

		Iterator<GraphicElement> it = mStatusButtList.iterator();
		while(it.hasNext()) {
			GraphicElement e = it.next();
			if (e.equals(gE)) {
				gE.setStatus(StatusEnum.DISABLE);
				gE.setName(null);
				gE.setToolTipText(null);
				mStatusButtList.remove(e);
				mStatusButtList.add(e);
				display();

				return true;
			}
		}

		return false;
	}

	public void removeAll() {
		for (GraphicElement gE : mStatusButtList) {
			gE.setStatus(StatusEnum.DISABLE);
			gE.setName(null);
			gE.setToolTipText(null);
		}

		super.clear();

		mStatusMap.clear();
	}

	@Override
	public void display() {
		gridx = 0;
		for (GraphicElement gE : mStatusButtList)
			drawElement(gE);

		super.display();
	}

	private void init(int max) {
		for (int i = 0; i < max; i++) {
			GraphicElement butt = new GraphicElement();
			butt.setStatus(StatusEnum.DISABLE);
			mStatusButtList.add(butt);
		}
	}

	private void drawElement(Component c) {
		getPalette().add(c);
	}

	private class GraphicElement extends JButton {
		/**
		 *
		 */
		private static final long serialVersionUID = 295895846621310210L;
		private StatusEnum status;

		public GraphicElement() {
			this.setModel(new DefaultButtonModel() {
				/**
				 *
				 */
				private static final long serialVersionUID = 906674933798446640L;

				@Override
				public void setPressed(boolean arg0) { }

				@Override
				public void setRollover(boolean arg0) { }
			});

			setStatus(StatusEnum.DISABLE);
		}

		@Override
		public void setName(String name) {
			super.setName(name);
			setText(name);
		}

		public void setStatus(StatusEnum status) {

			if (status == null || status == this.status)
				return;

			switch (status) {
			case DEFAULT:
				setBackground(null);
				setEnabled(true);
				break;
			case DISABLE:
				setBackground(null);
				setEnabled(false);
				break;
			case FAILURE:
				setEnabled(true);
				setBackground(Color.RED);
				break;
			case SUCCESS:
				setEnabled(true);
				setBackground(Color.GREEN);
				break;
			}

			this.status = status;
		}
	}
}
