package pl.homik.subtitleschecker.opensubtitles;

public class OpenSubtitlesLoginFailedException extends RuntimeException {

    private static final long serialVersionUID = -3110732403654098945L;

    public OpenSubtitlesLoginFailedException(Exception e) {
	super(e);
    }

}
