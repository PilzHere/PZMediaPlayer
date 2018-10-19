package media;

public class PZMedia {
	String displayName;

	String filePath;
//	private String title;
//	private String year;

	public PZMedia(String filePath) {
		this.filePath = filePath;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

//	public String getTitle() {
//		return title;
//	}
//	
//	public void setTitle(String title) {
//		this.title = title;
//	}
//	
//	public String getYear() {
//		return year;
//	}
//	
//	public void setYear(String year) {
//		this.year = year;
//	}
}
