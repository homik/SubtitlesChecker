package pl.homik.subtitleschecker.opensubtitles;

public class OpenSubtitlesSearchFailedException extends RuntimeException {

    private static final long serialVersionUID = 3978397980568821592L;

    public OpenSubtitlesSearchFailedException(Exception e, String msg) {
	super(e);
    }
}
