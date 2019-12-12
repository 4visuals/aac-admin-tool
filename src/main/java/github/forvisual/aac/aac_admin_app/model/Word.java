package github.forvisual.aac.aac_admin_app.model;

public class Word {

	final public int seq;
	
	final public String wordName;
	
	private String versionCode;

	public Word(int seq, String wordName) {
		super();
		this.seq = seq;
		this.wordName = wordName;
		this.versionCode = "0";
	}
	
	public Word(int seq, String wordName, String versionCode) {
		super();
		this.seq = seq;
		this.wordName = wordName;
		this.versionCode = versionCode;
	}

	public String getVersionCode() {
		return versionCode;
	}

	public void setVersionCode(String versionCode) {
		this.versionCode = versionCode;
	}
	/**
	 * 현재 작업중인 단어인지 나타냄
	 * "작업중인 단어"란, 디비 작업중에 새롭게 입력한 단어임을 나타냄
	 * @return
	 */
	public boolean isWorkingWord() {
		return "0".equals(this.versionCode);
	}
	
	
}
