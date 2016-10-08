package pl.homik.subtitleschecker.trakt.config;

/**
 * Interface providing Trakt service configuration
 * 
 * @author Pawel Chwedorowicz
 *
 */
public interface TraktConfig {

    /**
     * Returns current access token for user authorization or null if not
     * authorized yet
     * 
     * @return access token or null
     */
    String getAccesToken();

    /**
     * Returns current refresh token for access token refreshing or null if not
     * authorized yet
     * 
     * @return refresh token or null
     */
    String getRefreshToken();

    /**
     * Stores given access token and refresh token
     * 
     * @param accessToken
     *            to store
     * @param refreshToken
     *            to store
     */
    void setAuthId(String accessToken, String refreshToken);

}
