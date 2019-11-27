package github.forvisual.aac.aac_admin_app;

import java.io.File;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.management.RuntimeErrorException;

import github.forvisual.aac.aac_admin_app.model.Category;
import github.forvisual.aac.aac_admin_app.model.Word;

public class AppContext {

	static {
		try {
			DriverManager.registerDriver(new org.sqlite.JDBC());
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private static AppContext ctx;
	
	private List<Category> cateList;

	private String dbFilePath = "db/aac-core.db";
	
	private AppContext() {
		loadCategories();
	}
	private void loadCategories() {
		InputStream in = AppContext.class.getResourceAsStream("/cate.txt");
		cateList = new ArrayList<Category>();
		Scanner sc = new Scanner(in);
		while (sc.hasNextLine()) {
			String [] param = sc.nextLine().split(",");
			int num = Integer.parseInt(param[0]);
			String name = param[1];
			cateList.add(new Category(num, name));
		}
		
	}
	public List<Category> getCategories() {
		return cateList;
	}
	public static AppContext getInstance() {
		if (ctx == null) {
			ctx = new AppContext();
		}
		return ctx;
	}
	
	
	
	/**
	 * before 아래에 있는 출처 폴더명 모두 반환
	 * @return
	 */
	public List<String> getOrigins() {
		// before -> 
		return null;
	}
	
	public File getBeforeDir() {
		return new File("before");
	}
	/**
	 * 
	 * @return
	 */
	public List<WorkImage> getBeforeImages() {
		File beforeDir = getBeforeDir();
		List<File> originDirs = Util.listDir(beforeDir);
		List<WorkImage> imgToDo = new ArrayList<WorkImage>();
	
		for (File origin : originDirs) {
			List<File> images = Util.listImages(origin);
			for (File img : images) {
				WorkImage wi = new WorkImage(origin.getName(), img);
				imgToDo.add(wi);
			}
		}
		return imgToDo;
	}
	/**
	 * before 이미지를 처리함
	 * - after 폴더로 복사
	 *   - 유일한 사진 이미지 이름 정해줘야 함
	 * - before 이미지 후처리(처리 되었음 표시 해야함. 현재는 파일 이름 끝에 _YYYYMMDDHHmmss_ 형식의 문자열을 붙여서 표시함)
	 * - 데이터베이스에 저장
	 *   pic - word1, word2, word3 : cate
	 *   
	 *   insert into pics(...)  <- category 정보는 pics 테이블로 등록될듯...
	 *   insert into words(...)
	 *     insert into word_pic(pic, word1)
	 *     insert into word_pic(pic, word2)
	 *     insert into word_pic(pic, word3)
	 *   
	 * TODO 사용자 애플리케이션에서 사진 보여줄때 특정 출처의 사진을 먼저 나오게 하는 기능 필요함
	 *      > PICS 테이블에 ORIGIN 컬럼 추가해야함
	 *      
	 * @param image
	 */
	public void save(WorkImage image) throws Exception {
		System.out.println("여기서 일괄 처리 해야함");
		
		String maxFileName = getMaxPictureNum();
		int fileNum = plusOne(maxFileName);
		
		String genFilename = fileNum + "." + image.getExtension();
		String afterDirName = "after/" + image.getOrigin();
		String fullPath = afterDirName + "/" + genFilename;
		
		Connection con = getConnection();
		try {
			int picturePK = insertPicture(con, genFilename, image.getCateSeq(), image.getOrigin());
			for(String word : image.getDescriptions()) {
				Word w = insertWord(con, word);
				mappingWordPic(con, picturePK, w.seq);
			}
			
			File src = image.getImageFile();
			File dst = new File(fullPath);
			
			Util.copy(src, dst);
			
			String fp = Util.fingerPrint(dst);
			
			setFp(con, picturePK, fp);
			
			if (src.delete()) {
				con.commit();				
			} else {
				throw new Exception("원본 파일 삭제 실패");
			}
		} catch (Exception e) {
			rollback(con);
			throw e;
		} finally {
			close(con, null, null);
		}
		
		
		System.out.println(image);
	}
	
	private void setFp(Connection con, int picSeq, String fp) {
		PreparedStatement stmt = null;
		String query = "update pics set file_fp = ? where seq = ?";
		try {
			stmt = con.prepareStatement(query);
			stmt.setString(1, fp);
			stmt.setInt(2, picSeq);
			
			stmt.executeUpdate();
			
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			close(null, stmt, null);
		}
	}
	private void mappingWordPic(Connection con, int pictureSeq, int wordSeq) {
		PreparedStatement stmt = null;
		String query = "insert into word_pic (word, pic) values(?, ?)";
		try {
			stmt = con.prepareStatement(query);
			stmt.setInt(1, wordSeq);
			stmt.setInt(2, pictureSeq);
			
			stmt.executeUpdate();
			
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			close(null, stmt, null);
		}
		
	}
	private Word findWord(Connection con, String wordName) {
		String query = "select * from words where word_name like ?";
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = con.prepareStatement(query);
			stmt.setString(1, wordName);
			
			rs = stmt.executeQuery();
			
			if (rs.next()) {
				return new Word(rs.getInt("seq"), rs.getString("word_name"));
			} else {
				return null;
			}
		} catch (SQLException e) {
			rollback(con);
			throw new RuntimeException(e);
		} finally {
			close(null, stmt, rs);
		}
	}
	private Word insertWord(Connection con, String word) {
		Word w = findWord(con, word);
		if (w != null) {
			return w;
		}
		PreparedStatement stmt = null;
		String query = "insert into words (word_name) values(?)";
		try {
			stmt = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
			stmt.setString(1, word);
			
			stmt.executeUpdate();
			
			int pk = generatedPK(stmt);
			return new Word(pk, word);
		} catch (SQLException e) {
			rollback(con);
			throw new RuntimeException(e);
		} finally {
			close(null, stmt, null);
		}
	}
	private int generatedPK(PreparedStatement stmt ) {
		ResultSet rs = null;
		try {
			rs = stmt.getGeneratedKeys();
			rs.next();
			int pk = rs.getInt(1);
			return pk;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			close(null, null, rs);
		}
	}
	
	public int insertPicture(Connection con, String fileName, int cate, String origin) {
		PreparedStatement stmt = null;
		String query = "insert into pics (pic_name, cate_ref, origin) values(?, ?, ?);";
		try {
			stmt = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
			stmt.setString(1, fileName);
			stmt.setInt(2, cate);
			stmt.setString(3, origin);
			
			stmt.executeUpdate();
			
			int pk = generatedPK(stmt);
			return pk;
		} catch (SQLException e) {
			rollback(con);
			throw new RuntimeException(e);
		} finally {
			close(null, stmt, null);
		}
	}
	private int plusOne(String fname) {
		int pos = fname.indexOf('.');
		return Integer.parseInt(fname.substring(0, pos)) + 1;
	}
	private void turnOnFK(Connection con) throws SQLException {
		PreparedStatement stmt = con.prepareStatement("PRAGMA foreign_keys=on");
		stmt.executeUpdate();
		stmt.close();
	}
	
	public String getMaxPictureNum() {
		String query = "select pic_name from pics";
		List<String> names = new ArrayList<>();
		
		Connection con = getConnection();
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try {
			stmt = con.prepareStatement(query);
			
			rs = stmt.executeQuery();
			while (rs.next()) {
				names.add(rs.getString("pic_name"));
			}
			return pickMax(names);
		} catch (SQLException e) {
			rollback(con);
			throw new RuntimeException(e);
		} finally {
			close(con, stmt, rs);
		}
		
	}
	public String pickMax(List<String> names) {
		String max = null;
		// 숫자.확장자 패턴
		String pattern = "^\\d+\\.(jpg|png|gif)";
		for (String name : names) {
			if (name.matches(pattern)) {
				max = max(max, name);
			}
		}
		return max == null ? "100000.png" : max;
	}
	private String max(String a, String b) {
		if (a == null) {
			return b;
		} else if (b == null) {
			return a;
		} else {
			int na = Integer.parseInt(a.substring(0, a.indexOf('.')));
			int nb = Integer.parseInt(b.substring(0, b.indexOf('.')));
			return na >= nb ? a : b;
		}
	}
	public Connection getConnection() {
		String url = "jdbc:sqlite:" + dbFilePath ;
		try {
			Connection con = DriverManager.getConnection(url);
			turnOnFK(con);
			con.setAutoCommit(false);
			return con;
		} catch (SQLException e) {
			throw new RuntimeException("DB connection 실패!", e);
		}
	}

	public void close(Connection con, PreparedStatement stmt, Object object) {
		if (con != null) {
			try {
				con.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	public void rollback(Connection con) {
		if (con != null) {
			try {
				con.rollback();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	public boolean existingPicture(WorkImage img) {
		String fp = Util.fingerPrint(img.getImageFile());
		
		String query = "select file_fp from pics where file_fp = ?";
		Connection con = getConnection();
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = con.prepareStatement(query);
			stmt.setString(1, fp);
			
			rs = stmt.executeQuery();
			
			return rs.next();
		} catch (SQLException e) {
			rollback(con);
			throw new RuntimeException(e);
		} finally {
			close(con, stmt, rs);
		}
	}
	public List<WorkImage> getDeployableImages() {
		
		String query = "select * from pics where ";
		return null;
	}
	public void moveToTrash(WorkImage img) {
		moveTo(img, "subtask/trashcan/");
	}
	public void moveToReuse(WorkImage img) {
		moveTo(img, "subtask/reuse/");
	}
	private void moveTo(WorkImage img, String path) {
		File src = img.getImageFile();
		String origin = img.getOrigin();
		
		String subPath = path + origin + "/";
		Util.mkdir(subPath);
		File dest = new File(subPath + src.getName());
		Util.copy(src,dest);
		src.delete();
	}
}