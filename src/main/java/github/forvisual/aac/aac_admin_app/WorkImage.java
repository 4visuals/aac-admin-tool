package github.forvisual.aac.aac_admin_app;
/**
 * 작업할 이미지
 * @author sue10
 *
 */

import java.awt.Component;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

public class WorkImage {

	private String origin;
	
	private File imageFile;
	
	private List<String> descriptions;
	
	private int cateSeq;

	public WorkImage(String origin, File pictureImage) {
		this.origin = origin;
		this.imageFile = pictureImage;
	}

	@Override
	public String toString() {
		return "[" + origin + "] imageFile=" + imageFile.getAbsolutePath() + ", descriptions=" + descriptions + "]";
	}

	public BufferedImage getImage() {
		try {
			return ImageIO.read(imageFile);
		} catch (IOException e) {
			throw new RuntimeException("error! " + imageFile.getName(), e);
		}
	}

	public String getOrigin() {
		return origin;
	}

	public File getImageFile() {
		return imageFile;
	}

	public void setDescription(List<String> descs) {
		this.descriptions = new ArrayList<>(descs);
	}
	
	public int getCateSeq() {
		return cateSeq;
	}

	public void setCateSeq(int cateSeq) {
		this.cateSeq = cateSeq;
	}

	public String getExtension() {
		String fname = this.imageFile.getName();
		int pos = fname.lastIndexOf('.');
		if (pos <= 0) {
			throw new RuntimeException("파일 이름이 이상함 " + this.imageFile.getAbsolutePath());
		}
		return fname.substring(pos+1);
	}

	public List<String> getDescriptions() {
		return this.descriptions;
	}

	public String getFileName(boolean excludeExtension) {
		String name = this.imageFile.getName();
		if (excludeExtension) {
			int pos = name.lastIndexOf('.');
			name = name.substring(0, pos);
		}
		return name;
	}

	public BufferedImage asImage() {
		File f = getImageFile();
		try {
			return ImageIO.read(f);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	
	
	
}
