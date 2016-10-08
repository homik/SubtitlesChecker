package pl.homik.subtitleschecker.model;

public class Subtitles {

    private final String fileName;
    private final String downloadLink;

    public Subtitles(String fileName, String downloadLink) {
	this.fileName = fileName;
	this.downloadLink = downloadLink;
    }

    public String getFileName() {
	return fileName;
    }

    public String getDownloadLink() {
	return downloadLink;
    }

}
