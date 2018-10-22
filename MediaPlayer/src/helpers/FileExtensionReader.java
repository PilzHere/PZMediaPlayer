package helpers;

/**
 * This object is used to read filenames.
 * @author PilzHere
 * */
public class FileExtensionReader {
	
	/**
	 * Fetches the extension of the file from it's filename.
	 * @param filename
	 * @return
	 */
	public static String getExtension(String filename) {
		if (filename == null) {
			return null;
		}
		
		int extensionPos = filename.lastIndexOf('.');
		int lastUnixPos = filename.lastIndexOf('/');
		int lastWindowsPos = filename.lastIndexOf('\\');
		int lastSeparator = Math.max(lastUnixPos, lastWindowsPos);

		int index = lastSeparator > extensionPos ? -1 : extensionPos;
		if (index == -1) {
			return "";
		} else {
			return filename.substring(index + 1);
		}
	}
}
