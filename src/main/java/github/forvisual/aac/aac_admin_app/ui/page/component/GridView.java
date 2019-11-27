package github.forvisual.aac.aac_admin_app.ui.page.component;

import javax.swing.JPanel;

import github.forvisual.aac.aac_admin_app.AppContext;
import github.forvisual.aac.aac_admin_app.WorkImage;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;

public class GridView extends JPanel implements ActionListener {
	public String mode = null;
	
	public GridView() {
		setLayout(new GridLayout(0, 5, 0, 0));
		List<WorkImage> images = AppContext.getInstance().getBeforeImages();
		for(WorkImage img : images){
			BufferedImage buf = img.asImage();
			// JButton btn = new JButton("");
			ImageView btn = new ImageView(buf);
			// btn.setIcon(new ImageIcon(buf));
			btn.setPreferredSize(new Dimension(128, 128));
			btn.addActionListener(this);
			btn.putClientProperty("img", img);
			add(btn);
		}
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		ImageView imgButotn = (ImageView) e.getSource();
		WorkImage img = (WorkImage) imgButotn.getClientProperty("img");
		
		if ("TRASH".equals(mode)) {
			AppContext.getInstance().moveToTrash(img);
			
		} else if ("REUSE".equals(mode)) {
			AppContext.getInstance().moveToReuse(img);			
		}
		this.remove(imgButotn);
		this.revalidate();
	}

}
