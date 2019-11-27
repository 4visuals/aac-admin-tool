package github.forvisual.aac.aac_admin_app.ui.page.component;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;

public class ImageView extends javax.swing.JComponent implements MouseListener{
	private BufferedImage img;
	private ActionListener l;
	public ImageView(BufferedImage img) {
		this.img = img;
		setPreferredSize(new Dimension(128,  128));
		setMaximumSize(new Dimension(128, 128));
		addMouseListener(this);
	}
	
	public void addActionListener(ActionListener here) {
		this.l = here;
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		int W = getWidth();
		int H = getHeight();
		
		if (img != null) {
			renderImage((Graphics2D)g, img, 0, 0, W, H);
		}
		
		g.setColor(Color.GRAY);
		g.drawRect(0,  0, W-1, H-1);
	}
	
	public static void renderImage(Graphics2D g, 
			Image image, 
			double x, double y, double width, double height) {
		double imgW = image.getWidth(null);
		double imgH = image.getHeight(null);
		double areaSlope = height / width;
		double imageSlope = imgH / imgW;
		double ratio =1;
		double offsetX = x;
		double offsetY = y;
		if(imageSlope >= areaSlope) {
			// ratio from height;
			ratio = height / imgH;
			imgH = height;
			imgW *= ratio;
			
			offsetX += (width - imgW)/2;
			
		} else {
			ratio = width / imgW;
			imgW = width;
			imgH *= ratio;
			offsetY += (height - imgH)/2;
//			
		}
		
		g.drawImage(image, 
				(int)offsetX, (int)offsetY, (int)(offsetX+imgW),(int)(offsetY+imgH), 
				0, 0, image.getWidth(null), image.getHeight(null), null);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (l != null) {
			l.actionPerformed(mockActionEvent());
		}
		
	}

	private ActionEvent mockActionEvent() {
		ActionEvent e = new ActionEvent(this, (int) (Math.random()*1000000), null);
		return e;
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

}
