package pl.homik.subtitleschecker.trakt;

public interface TraktAuthorizationPinProvider {

    /**
     * Method to get code user authorization code from Trakt authorization
     * service. Code inside should just inform user to access the
     * <code>url</code> and input the validation code displayed.
     * 
     * @param url
     *            which user should access to get authorization code
     * @return user provided authorization code
     */
    String getAccessCode(String url);

}
