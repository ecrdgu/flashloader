package com.dongguk.ecr.framework.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.LayoutManager;

import javax.swing.JComponent;
import javax.swing.JPanel;

import com.dongguk.ecr.constant.GUIConstant;

/**
 * GraphicPalette
 * @author jhun.ahn
 *
 */
public abstract class GraphicPalette {
	/** top left bottom right **/
	protected static final Insets noInsets    = new Insets(0, 0, 0, 0);
	protected static final Insets entryInsets = new Insets(0, 10, 5, 10);	//topest
	protected static final Insets iconInsets  = new Insets(0, 5, 0, 0);
	protected static final Insets spaceInsets = new Insets(5, 10, 5, 10);

	protected static final Insets wideInsets  = new Insets(0, 5, 0, 5);

	protected static final Insets topInsets  = new Insets(0, 0, 5, 0);
	protected static final Insets bottomInsets  = new Insets(5, 0, 5, 0);
	protected static final Insets defaultInsets = new Insets(5, 5, 5, 5);

	protected int gridx, gridy;
	private JComponent palette;

	public GraphicPalette() {
		this.palette = new JPanel();
	}

	public void setLayout() {
		this.palette.setLayout(new GridBagLayout());
	}

	public void setLayout(LayoutManager layout) {
		this.palette.setLayout(layout);
	}
	/**
	 *
	 */
	public Container getPalette() {
		return this.palette;
	}

	public void display() {
		this.palette.revalidate();
		this.palette.repaint();
	}

	public void clear() {
		this.palette.removeAll();
		gridx = 0;
		gridy = 0;
		display();
	}

	public void remove() {
		this.palette.removeAll();
	}

	public void setName(String name) {
		getPalette().setName(name);
	}

    protected void addComponent(Component component,
            int gridx, int gridy, int gridwidth, int gridheight, double wx, double wy,
            Insets insets, int anchor, int fill) {

		this.addComponent(palette, component, gridx, gridy,
				gridwidth, gridheight, wx, wy,
				insets, anchor, fill);
    }

    protected void addComponent(Container p, Component component,
            int gridx, int gridy, int gridwidth, int gridheight, double wx, double wy,
            Insets insets, int anchor, int fill) {
        GridBagConstraints gbc = new GridBagConstraints(gridx, gridy,
                gridwidth, gridheight, wx, wy, anchor, fill, insets, 0, 0);
        /**
         * gridx, gridy, gridwidth, gridheight, weightx, weighty, anchor, fill, insets, ipadx, ipady
         */
        component.setFont(GUIConstant.systemFont);
        p.add(component, gbc);
    }
}
