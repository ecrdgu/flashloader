package com.dongguk.ecr.ui;

import java.awt.Graphics;
import java.awt.Image;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import com.dongguk.ecr.main.SystemTypeLoader;

/**
 * Print the Default Image
 * @author jhun.ahn
 *
 */
public class ImagePanel extends JPanel {
	private static final long serialVersionUID = 8743274143141321806L;

	private Image image;
	private boolean isFixedRatio = false;

	int width, height;
	int imgX, imgY;

	public ImagePanel() { this(null, false); }

	public ImagePanel(String path, boolean isFixedRatio) {
		try {
			Image img = getImage(path);
			if (img == null) {
				System.err.println("failed open file : " + path);
				return;
			}
			this.isFixedRatio = isFixedRatio;
			setBackground(img);

		} catch (IOException e) {
			System.err.println("failed open image : " + path);
		}
	}

	public void setBackground(Image image) {
		if (this.image != image) {
			this.image = image;

			repaint();
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

	private void fillImage() {

		this.width = image.getWidth(null);
		this.height = image.getHeight(null);
		this.imgX = 0;
		this.imgY = 0;

		int imgWidth = this.width;
		int imgHeight = this.height;

		int width = this.getWidth();
		int height = this.getHeight();

		if (imgWidth > width) {
			this.width = width;
			this.height = (int) (imgHeight * ((double) width / (double) imgWidth));
		}

		if (isFixedRatio && this.height > height) {
			this.width = (int) (width * ((double) height / (double) this.height));
			this.height = height;
		}

		if (isFixedRatio) {
			imgX = (width - this.width) / 2;
			imgY = (height - this.height) / 2;
		}

	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		fillImage();
		g.drawImage(this.image, this.imgX, this.imgY, this.width, this.height, null);
	}

}
