package github.forvisual.aac.aac_admin_app.ui.page;

import javax.swing.JPanel;
import javax.swing.JButton;
import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import github.forvisual.aac.aac_admin_app.ui.page.component.search.PictureBaseSearchPanel;
import github.forvisual.aac.aac_admin_app.ui.page.component.search.WordBaseSearchPanel;

public class SearchPage extends JPanel {

	private PictureBaseSearchPanel picturePanel;

	/**
	 * Create the panel.
	 */
	public SearchPage() {
		setLayout(new BorderLayout(0, 0));
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		add(tabbedPane, BorderLayout.CENTER);
		
		picturePanel = new PictureBaseSearchPanel();
		tabbedPane.addTab("사진으로", picturePanel);
		
		tabbedPane.addTab("어휘로",  new WordBaseSearchPanel());
		
		tabbedPane.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				Component comp = tabbedPane.getSelectedComponent();
				if (comp.getClass() == PictureBaseSearchPanel.class) {
					((PictureBaseSearchPanel)comp).reloadPage();
				}
			}
		});
		}

	public void reload() {
		picturePanel.reloadPage();
	}

}
