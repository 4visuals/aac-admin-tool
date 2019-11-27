package github.forvisual.aac.aac_admin_app.ui.page.component;

import javax.swing.JPanel;
import java.awt.GridBagLayout;
import javax.swing.JLabel;

import java.awt.Font;
import java.awt.GridBagConstraints;
import javax.swing.JTextField;

import github.forvisual.aac.aac_admin_app.AppContext;
import github.forvisual.aac.aac_admin_app.Util;
import github.forvisual.aac.aac_admin_app.WorkImage;
import github.forvisual.aac.aac_admin_app.model.Category;

import java.awt.Insets;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
/**
 * 선택된 사진에 대한 입력 양식
 * @author sue10
 *
 */
public class WorkFormPanel extends JPanel implements WorkImageListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1987835500277607421L;
	private JTextField originField;
	private JTextField descField;
	private JLabel lblCategory;
	private JComboBox categoryBox;
	private JButton btnProcess;
	private WorkImage img;
	
	private Category selectedCategory;
	private List<String> descs = new ArrayList<>();
	private DefaultComboBoxModel<Category> cbBoxModel;
	
	Font font = new Font("나눔고딕", Font.BOLD, 20);

	
	FormResultListener listener;
	
	/**
	 * Create the panel.
	 */
	public WorkFormPanel() {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		JLabel lblOrigin = new JLabel("ORIGIN");
		lblOrigin.setFont(font);
		GridBagConstraints gbc_lblOrigin = new GridBagConstraints();
		gbc_lblOrigin.insets = new Insets(0, 0, 5, 5);
		gbc_lblOrigin.anchor = GridBagConstraints.EAST;
		gbc_lblOrigin.gridx = 0;
		gbc_lblOrigin.gridy = 0;
		add(lblOrigin, gbc_lblOrigin);
		
		originField = new JTextField();
		originField.setEditable(false);
		GridBagConstraints gbc_originField = new GridBagConstraints();
		gbc_originField.insets = new Insets(0, 0, 5, 0);
		gbc_originField.fill = GridBagConstraints.HORIZONTAL;
		gbc_originField.gridx = 1;
		gbc_originField.gridy = 0;
		add(originField, gbc_originField);
		originField.setColumns(10);
		
		JLabel lblDescription = new JLabel("DESCRIPTION");
		lblDescription.setFont(font);
		GridBagConstraints gbc_lblDescription = new GridBagConstraints();
		gbc_lblDescription.anchor = GridBagConstraints.EAST;
		gbc_lblDescription.insets = new Insets(0, 0, 5, 5);
		gbc_lblDescription.gridx = 0;
		gbc_lblDescription.gridy = 1;
		add(lblDescription, gbc_lblDescription);
		
		descField = new JTextField();
		descField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					processDescription();
					processWorkImage();
				}
			}
		});
		GridBagConstraints gbc_descField = new GridBagConstraints();
		gbc_descField.insets = new Insets(0, 0, 5, 0);
		gbc_descField.fill = GridBagConstraints.HORIZONTAL;
		gbc_descField.gridx = 1;
		gbc_descField.gridy = 1;
		add(descField, gbc_descField);
		descField.setColumns(10);
		
		lblCategory = new JLabel("CATEGORY");
		lblCategory.setFont(font);
		GridBagConstraints gbc_lblCategory = new GridBagConstraints();
		gbc_lblCategory.anchor = GridBagConstraints.EAST;
		gbc_lblCategory.insets = new Insets(0, 0, 5, 5);
		gbc_lblCategory.gridx = 0;
		gbc_lblCategory.gridy = 2;
		add(lblCategory, gbc_lblCategory);
		
		cbBoxModel = new DefaultComboBoxModel<>();
		{
			List<Category> cates = AppContext.getInstance().getCategories();
			for (Category c : cates) {
				cbBoxModel.addElement(c);
			}
		}
		categoryBox = new JComboBox(cbBoxModel);
		GridBagConstraints gbc_categoryBox = new GridBagConstraints();
		gbc_categoryBox.insets = new Insets(0, 0, 5, 0);
		gbc_categoryBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_categoryBox.gridx = 1;
		gbc_categoryBox.gridy = 2;
		add(categoryBox, gbc_categoryBox);
		{
			JPanel btnPanel = new JPanel();
			
			btnProcess = new JButton("PROCESS");
			btnProcess.setFont(font);
			
			btnPanel.add(btnProcess);
			
			JButton btnReuse = new JButton("이미지 작업");
			btnReuse.addActionListener(e -> processGotoReuseDir());
			btnPanel.add(btnReuse);
			
			JButton btnDel = new JButton("삭제");
			btnDel.addActionListener(e -> processGotoTrash());
			btnPanel.add(btnDel);
			
			
			GridBagConstraints gbc_btnProcess = new GridBagConstraints();
			gbc_btnProcess.gridx = 1;
			gbc_btnProcess.gridy = 3;
			add(btnPanel, gbc_btnProcess);
			
		}
		

	}
	/**
	 * 휴지통으로 ..
	 */
	private void processGotoTrash() {
		AppContext.getInstance().moveToTrash(this.img);
		if (listener != null) {
			listener.submitted(img, true);
		}
	}
	/**
	 * 이미지 추가 작업용 폴더로 이동시킴
	 */
	private void processGotoReuseDir() {
		
		AppContext.getInstance().moveToReuse(this.img);
		if (listener != null) {
			listener.submitted(img, true);
		}
	}

	public void setFormListener (FormResultListener l) {
		listener = l;
	}

	protected void processDescription() {
		String input = descField.getText().trim();
		if (input.length() == 0) {
			Util.errorDialog(this, "입력다시", "설명 입력");
			descField.setText("*");
			descField.selectAll();
			return;
		}
		String [] tokens = input.split(",");
		for (String tk : tokens) {
			String each = tk.trim();
			if (each.length() == 0) {
				continue;
			}
			descs.add(each);
		}
		
	}

	void updateForm(WorkImage img) {
		this.img = img;
		originField.setText(String.format("[%s]%s", img.getOrigin(), img.getImageFile().getName()));
		
		selectedCategory = null;
		descs.clear();
		
		String fname = img.getFileName(true);
		descField.setText(fname);
		descField.requestFocus();
		descField.selectAll();
		
	}
	
	@Override
	public void workImageSelected(WorkImage image) {
		updateForm(image);
	}

	void processWorkImage() {
		
		Category curCate = (Category) cbBoxModel.getSelectedItem();
		if (descs.size() == 0) {
			Util.errorDialog(this, "설명입력", "설명이 없습니다.");
			return;
		}
		
		boolean success = false;
		img.setDescription(descs);
		img.setCateSeq(curCate.getNum());
		try {
			if (AppContext.getInstance().existingPicture(img)) {
				Util.alert(this, "존재합니다", "동일한 이미지 발견");
				return;
			}
			AppContext.getInstance().save(img);
			success = true;
			if (listener != null) {
				listener.submitted(img, success);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		
	}
}
