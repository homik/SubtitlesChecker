package pl.homik.subtitleschecker.trakt;

public class TraktLoadingFailedException extends RuntimeException {

    private static final long serialVersionUID = -5519919292024344286L;

    public TraktLoadingFailedException(Exception e) {
	super(e);
    }
}
