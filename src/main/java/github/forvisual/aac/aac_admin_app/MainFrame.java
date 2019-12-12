package github.forvisual.aac.aac_admin_app;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Font;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import github.forvisual.aac.aac_admin_app.ui.page.DeployPage;
import github.forvisual.aac.aac_admin_app.ui.page.ImageWorkPage;
import github.forvisual.aac.aac_admin_app.ui.page.SearchPage;
import github.forvisual.aac.aac_admin_app.ui.page.component.search.PictureBaseSearchPanel;

import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class MainFrame extends JFrame {

	private JPanel contentPane;
	Font font = new Font("나눔고딕", Font.BOLD, 20);

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
		} catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InstantiationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IllegalAccessException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (UnsupportedLookAndFeelException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainFrame frame = new MainFrame();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public MainFrame() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 1200, 900);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setFont(font);
		
		contentPane.add(tabbedPane, BorderLayout.CENTER);
		
		tabbedPane.addTab("ImageWork", new ImageWorkPage());
		
		tabbedPane.addTab("Search", new SearchPage());
		
		tabbedPane.addTab("Deploy", new DeployPage());
		
		tabbedPane.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				Component comp = tabbedPane.getSelectedComponent();
				if (comp.getClass() == SearchPage.class) {
					((SearchPage)comp).reload();
				}
			}
		});
		
	}

}
