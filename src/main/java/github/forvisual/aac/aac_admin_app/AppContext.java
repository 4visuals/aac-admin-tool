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
			for(Word w: image.getDescriptions()) {
				// Word w = insertWord(con, word);
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
				return new Word(rs.getInt("seq"), rs.getString("word_name"), rs.getString("db_version"));
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
	/**
	 * 현재 작업중인 사진들(배포는 안된 사진들)
	 * 
	 * @return
	 */
	public List<WorkImage> getWorkingImages() {
		String query = "select * from pics where db_version = 0";
		Connection con = getConnection();
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		List<WorkImage> list = new ArrayList<WorkImage>();
		try {
			stmt = con.prepareStatement(query);
			rs = stmt.executeQuery();
			while (rs.next()) {
				int seq = rs.getInt("seq");
				String origin = rs.getString("origin");
				String fname = rs.getString("pic_name");
				int cateSeq = rs.getInt("cate_ref");
				
				File filePath = new File("after/"+ origin + "/" + fname);
				
				WorkImage wi = new WorkImage(seq, origin, filePath, cateSeq);
				wi.setDescription(getDesc(con, seq));
				list.add(wi);
			}
			return list;
		} catch (SQLException e) {
			rollback(con);
			throw new RuntimeException(e);
		} finally {
			close(con, stmt, rs);
		}
	}
	
	public List<String> getAllDescs() {
		String query = "select * from words where db_version = 0";
		Connection con = getConnection();
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		List<String> list = new ArrayList<String>();
		try {
			stmt = con.prepareStatement(query);
			rs = stmt.executeQuery();
			while (rs.next()) {
				String desc = rs.getString("word_name");
				list.add(desc);
			}
			return list;
		} catch (SQLException e) {
			rollback(con);
			throw new RuntimeException(e);
		} finally {
			close(con, stmt, rs);
		}
	}
	
	/**
	 * 이미지의 설명들
	 * @param con
	 * @param picSeq
	 * @return
	 */
	private List<Word> getDesc(Connection con, int picSeq) {
		String query = "select w.* from word_pic wp "
				+ " join words w on wp.word = w.seq "
				+ " where wp.pic = ?";
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		List<Word> list = new ArrayList<>();
		try {
			stmt = con.prepareStatement(query);
			stmt.setInt(1, picSeq);
			rs = stmt.executeQuery();
			while (rs.next()) {
				Word w = new Word(rs.getInt("seq"), rs.getString("word_name"), rs.getString("db_version"));
				// String wordName = rs.getString("word_name");
				list.add(w);
			}
			return list;
		} catch (SQLException e) {
			rollback(con);
			throw new RuntimeException(e);
		} finally {
			close(null, stmt, rs);
		}
	}
	/**
	 * TODO 특정 사진에 연결된 신규 단어 맵핑을 모두 지움
	 * @param con
	 * @param words 
	 * @param wordSeq
	 * @param word
	 */
	void deleteWorkingWordMapping(Connection con, WorkImage pic) {
		List<Word> workingWords = pic.getDescriptions(true);
		String query = "delete from word_pic where pic = ? and word = ?";
		PreparedStatement stmt = null;
		try {
			for (Word w : workingWords) {
				stmt = con.prepareStatement(query);
				stmt.setInt(1, pic.getSeq());
				stmt.setInt(2, w.seq);
				stmt.executeUpdate();
				stmt.clearParameters();
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			ctx.close(null, stmt, null);
		}

	}
	
	public void replaceWords(WorkImage img, List<String> desc) {
		// List<String> words = desc;
		Connection con =getConnection();
		// img.getDescriptions(true);
		deleteWorkingWordMapping(con, img);

		/*
		 * 주어진 단어가 있으면 SEQ반환, 없으면 집어넣고 SEQ반환*/
		try {
			int picSeq = img.getSeq();
			List<Word> words = new ArrayList<Word>();
			for(String each : desc) {
				Word word = findWord(con, each);
				if (word == null) {
					word = insertWord(con, each);
				}
				addMappings(con, picSeq, word.seq);
				words.add(word);
			}
			words.addAll(img.getDescriptions(false));
			img.setDescription(words);
			con.commit();
		} catch (SQLException e) {
			rollback(con);
			throw new RuntimeException(e);
		} finally {
			close(con, null, null);
		}
		
		
	}
	private void addMappings(Connection con, int picSeq, int wordSeq) throws SQLException {
		String query = "INSERT INTO word_pic(word, pic) VALUES(?, ?)"; /// 
		PreparedStatement stmt = con.prepareStatement(query);
		stmt.setInt(1, wordSeq);
		stmt.setInt(2, picSeq);
		stmt.executeUpdate();
		stmt.close();
	}
	public void updateDescription(WorkImage img) {
		// TODO 현재 사진에 연결된 단어를 다 지움. 그리고나서 새로 등록함
	}
	public void updateCategory(int picSeq, int cateSeq) {
		String query = "update pics set cate_ref = ? where seq = ?";
		Connection con = getConnection();
		PreparedStatement stmt = null;
		try {
			stmt = con.prepareStatement(query);
			stmt.setInt(1, cateSeq);
			stmt.setInt(2, picSeq);
			stmt.executeUpdate();
			con.commit();
		} catch (SQLException e) {
			rollback(con);
			throw new RuntimeException(e);
		} finally {
			ctx.close(con, stmt, null);
		}
		
	}
	public List<Word> prepareWords(List<String> descs) {
		
		List<Word> list = new ArrayList<>();
		Connection con = getConnection();
		try {
			for (String wname : descs) {
				Word w = findWord(con, wname);
				if (w == null) {
					w = insertWord(con, wname);
				}
				list.add(w);
			}
			con.commit();
			return list;
		}  catch (SQLException e) {
			rollback(con);
			throw new RuntimeException(e);
		} finally {
			close(con, null, null);
		}
	}
}