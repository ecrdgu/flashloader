package com.dongguk.ecr.ui.status;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.plaf.metal.MetalButtonUI;

/**
 * ButtonTabComponent
 * @author jhun.ahn
 *
 */
class ButtonTabComponent extends JPanel {
	/**
	 *
	 */
	private static final long serialVersionUID = -6429660520882592323L;
	private final JTabbedPane pane;
	private ActionListener listener;

	private static final MouseListener buttonMouseListener = new MouseAdapter() {
		@Override
		public void mouseEntered(MouseEvent e) {
			Component component = e.getComponent();
            if (component instanceof AbstractButton) {
                AbstractButton button = (AbstractButton) component;
                button.setBorderPainted(true);
            }
		}

		@Override
		public void mouseExited(MouseEvent e) {
			Component component = e.getComponent();
            if (component instanceof AbstractButton) {
                AbstractButton button = (AbstractButton) component;
                button.setBorderPainted(false);
            }
		}
	};

	public ButtonTabComponent(JTabbedPane pane, boolean bClose) {
		setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		this.pane = pane;
		this.listener = null;

		setOpaque(false);

		add(new TitleText());
		if (bClose)
			add(new CloseButton());
	}

	public void addActionListener(ActionListener listener) {
		synchronized (listener) {
			this.listener = listener;
		}
	}

	public void removeActionListener() {
		addActionListener(null);
	}

	private ActionListener getListener() {
		synchronized (listener) {
			return listener;
		}
	}

	private class TitleText extends JLabel {
		/**
		 *
		 */
		private static final long serialVersionUID = -2328033725009058763L;

		@Override
		public String getText() {
			int idx = pane.indexOfTabComponent(ButtonTabComponent.this);
			if (idx < 0)
				return null;
			return pane.getTitleAt(idx);
		}
	}

	private class CloseButton extends JButton {

		/**
		 *
		 */
		private static final long serialVersionUID = -7039116819887478522L;

		public CloseButton() {
			final int size = 20;
			setPreferredSize(new Dimension(size, size));
			setToolTipText("close");

			setUI(new MetalButtonUI());

			setContentAreaFilled(false);

            setFocusable(false);
            setBorder(BorderFactory.createEtchedBorder());

            setBorderPainted(false);
            addMouseListener(buttonMouseListener);
            setRolloverEnabled(true);

            addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {

					ActionListener l = getListener();
					if(l != null)
						l.actionPerformed(arg0);
				}
			});
		}

		@Override
		public void updateUI() {
			/*super.updateUI();*/
		}

		@Override
		protected void paintComponent(Graphics arg0) {
			super.paintComponent(arg0);

			Graphics2D g = (Graphics2D) arg0.create();

			if (getModel().isPressed())
				g.translate(1, 1);

			g.setStroke(new BasicStroke(2));
			g.setColor(Color.BLACK);
			if (getModel().isRollover())
				g.setColor(Color.MAGENTA);

			final int delta = 6;

			g.drawLine(delta, delta, getWidth() - delta - 1, getHeight() - delta - 1);
			g.drawLine(getWidth() - delta - 1, delta, delta, getHeight() - delta - 1);

			g.dispose();
		}

	}
}
