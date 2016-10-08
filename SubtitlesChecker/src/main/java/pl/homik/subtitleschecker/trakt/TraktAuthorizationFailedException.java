package pl.homik.subtitleschecker.trakt;

public class TraktAuthorizationFailedException extends RuntimeException {

    private static final long serialVersionUID = -5381549035053333489L;

    public TraktAuthorizationFailedException(Exception e) {
	super(e);
    }
}
