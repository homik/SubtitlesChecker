package pl.homik.subtitleschecker.trakt.config;

/**
 * Simple {@link TraktConfig} implementation which just stores trakt
 * configuration in memory
 * 
 * @author Pawel Chwedorowicz
 *
 */
public class RamTraktConfig implements TraktConfig {

    private String accessToken;
    private String refreshToken;

    @Override
    public String getAccesToken() {
	return accessToken;
    }

    @Override
    public String getRefreshToken() {
	return refreshToken;
    }

    @Override
    public void setAuthId(String accessToken, String refreshToken) {
	this.accessToken = accessToken;
	this.refreshToken = refreshToken;

    }

}
