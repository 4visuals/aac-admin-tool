package github.forvisual.aac.aac_admin_app;

import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class Util {
	public enum AlignmentX { LEFT, CENTER, RIGHT};
	public static List<File> listDir(File dir) {
		List<File> subDir = new ArrayList<File>();
		File [] dirs = dir.listFiles();
		if (dirs == null) {
			dirs = new File[0];
		}
		
		for (File sub : dirs) {
			subDir.add(sub);
		}
		return subDir;
		
	}

	public static List<File> listImages(File origin) {
		List<File> subDir = new ArrayList<File>();
		File [] files = origin.listFiles();
		if (files == null) {
			files = new File[0];
		}
		
		List<File> imgs = new ArrayList<File>();
		for (File each : files) {
			if (notProcessed(each)) {
				imgs.add(each);
			}
		}
		return imgs;
	}

	private static boolean notProcessed(File each) {
		String name = each.getName().toUpperCase();
		String ext = extPart(name);
		return "JPG GIF PNG JPEC".indexOf(ext) >= 0;
	}

	private static String extPart(String filename) {
		int p = filename.lastIndexOf('.');
		p = p < 0 ? filename.length() : p;
		return filename.substring(p+1);
	
	}

	public static void renderImage(Graphics2D g, 
			Image image, 
			double x, double y, double width, double height, AlignmentX alignmentX) {
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
			if ( alignmentX == AlignmentX.LEFT) {
					// offsetX += 0;
			} else if (alignmentX == AlignmentX.CENTER) {
				offsetX += (width - imgW)/2;
			} else if ( alignmentX == AlignmentX.RIGHT) {
				offsetX += (width - imgW);
			}
		} else {
			ratio = width / imgW;
			imgW = width;
			imgH *= ratio;
			offsetY += (height - imgH)/2;
//			if (alignmentX == AlignmentX.CENTER) {
//				offsetY += (height - imgH)/2;
//			} else if ( alignmentX == AlignmentX.LEFT) {
//				offsetX += 0;
//			} else if ( alignmentX == AlignmentX.RIGHT) {
//				offsetX += (width - imgW);
//			}
		}
		
		g.drawImage(image, 
				(int)offsetX, (int)offsetY, (int)(offsetX+imgW),(int)(offsetY+imgH), 
				0, 0, image.getWidth(null), image.getHeight(null), null);
	}
	
	public static void errorDialog(Component opener, String title, String message) {
		Component window = SwingUtilities.getWindowAncestor(opener);
		JOptionPane.showMessageDialog(window, message, title, JOptionPane.ERROR_MESSAGE);
		
	}

	public static void copy(File src, File dst) {
		try {
			Files.copy(src.toPath(), dst.toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			throw new RuntimeException("fail to copy file " + src.getAbsolutePath() + " => " + dst.getAbsolutePath());
		}
	}
	
	public static void copy(InputStream in, OutputStream out) {
//      BufferedInputStream bin; // = asBufIn(in);
//      BufferedOutputStream bout; //  = asBufOut(out);
      int c;
      try(BufferedInputStream bin = asBufIn(in);
          BufferedOutputStream bout= asBufOut(out) ) {
          while ((c=bin.read()) != -1) {
              bout.write(c);
          }
          bout.flush();
      }catch (IOException e) {
          throw new RuntimeException(e);
      }
  }
	
    /**
     * 주어진 파일의 내용으로 finger print 문자열 생성
     * @param file
     * @return
     */
    public static String fingerPrint(File file) {
        try {
            BufferedInputStream in = asBufIn(new FileInputStream(file));
            ByteArrayOutputStream bos = new ByteArrayOutputStream((int) file.length());
            copy(in, bos);

            byte [] data = bos.toByteArray();
            return fingerPrint(data);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    
    private static BufferedInputStream asBufIn(InputStream in) {
        return in.getClass() == BufferedInputStream.class ? (BufferedInputStream)in :
                new BufferedInputStream(in);
    }
    private static BufferedOutputStream asBufOut(OutputStream out) {
        return out.getClass() == BufferedOutputStream.class ? (BufferedOutputStream) out :
                new BufferedOutputStream(out);
    }

    private static MessageDigest md;

    static {
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    /**
     * 주어진 배열에 대응하는 fingerprint 문자열 생성
     * @param data
     * @return
     */
    public static String fingerPrint(byte [] data) {
            md.update(data);
            byte [] hash = md.digest();

            StringBuilder buf = new StringBuilder();
            for (int i = 0; i < hash.length; i++) {
                buf.append(String.format("%02x", hash[i]&0xff));
            }
            return buf.toString();
    }
    

	public static void alert(Component opener, String title, String message) {
		Component window = SwingUtilities.getWindowAncestor(opener);
		JOptionPane.showMessageDialog(window, message, title, JOptionPane.INFORMATION_MESSAGE);
	}

}
