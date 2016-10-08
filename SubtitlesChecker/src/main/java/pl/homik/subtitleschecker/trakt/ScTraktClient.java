package pl.homik.subtitleschecker.trakt;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;

import com.uwetrottmann.trakt5.TraktV2;
import com.uwetrottmann.trakt5.entities.AccessToken;
import com.uwetrottmann.trakt5.entities.BaseShow;
import com.uwetrottmann.trakt5.entities.Episode;
import com.uwetrottmann.trakt5.enums.Extended;

import pl.homik.subtitleschecker.model.EpisodeToWatch;
import pl.homik.subtitleschecker.model.TvShow;
import pl.homik.subtitleschecker.trakt.config.TraktConfig;
import retrofit2.Response;

/**
 * Client for accessing TRAKT.TV service
 * 
 * @author Pawel Chwedorowicz
 *
 */
public class ScTraktClient {

    private static final String CLIENT_SECRET = "35ce26c697f6de99a611cae7aee08250b742971a16c3c5a26715a9bd2ffa0e00";
    private static final String API_KEY = "fcce239c0d9f7d9ef29dc95480a15101998cb26bf6fb06f5dccc551bb11a0e29";
    private static final String OAUTH = "urn:ietf:wg:oauth:2.0:oob";

    private final TraktConfig config;
    private final TraktAuthorizationPinProvider authorizer;
    private final TraktV2 trakt;
    private boolean authorized = false;

    /**
     * Constructor
     * 
     * @param config
     *            object which provides and stores trakt authorization tokens
     * @param authorizer
     *            object which interacts with user if trakt authorization is
     *            required
     */
    public ScTraktClient(TraktConfig config,
	    TraktAuthorizationPinProvider authorizer) {
	this.config = config;
	this.authorizer = authorizer;
	this.trakt = new TraktV2(API_KEY, CLIENT_SECRET, OAUTH);
    }

    /**
     * Authorizes with TRAKT.TV. Executing this method after authorization was
     * successful won't do anything
     * 
     * @throws TraktAuthorizationFailedException
     *             if something goes wrong
     */
    public void authorizeTrakt() {
	if (!authorized) {
	    try {

		if (config.getAccesToken() == null
			|| config.getRefreshToken() == null) {
		    // config is empty we need to authorize
		    doStandardAuthorization();
		} else if (!updateAccessTokens()) {
		    // access and refresh tokens are set but lets just refresh
		    // them

		    // refreshing token failed (maybe outdated) lets do the
		    // normal authorization again
		    doStandardAuthorization();
		}
		authorized = true;
	    } catch (IOException | OAuthSystemException e) {
		throw new TraktAuthorizationFailedException(e);
	    }
	}
    }

    private boolean updateAccessTokens() throws IOException {
	updateClientTokensFromConfig();
	return updateConfigIfResponseIsOk(trakt.refreshAccessToken());
    }

    private void updateClientTokensFromConfig() {
	// set tokens from configuration
	trakt.refreshToken(config.getRefreshToken());
	trakt.accessToken(config.getAccesToken());
    }

    private void doStandardAuthorization()
	    throws IOException, OAuthSystemException {

	// prepare authorization url
	OAuthClientRequest acr = trakt.buildAuthorizationRequest(
		"subtitlesCheck" + System.currentTimeMillis());

	// exchange url for user pin code
	String code = authorizer.getAccessCode(acr.getLocationUri());

	// try to use pin to authorize user
	Response<AccessToken> atResponse = trakt
		.exchangeCodeForAccessToken(code);
	if (!updateConfigIfResponseIsOk(atResponse)) {
	    throw new IOException(
		    "authorization failed: " + atResponse.errorBody().string());
	}

    }

    private boolean updateConfigIfResponseIsOk(
	    Response<AccessToken> atResponse) {

	// check if response was ok
	boolean result = atResponse.isSuccessful();
	if (result) {
	    // if ok store tokens to config
	    AccessToken at = atResponse.body();
	    config.setAuthId(at.access_token, at.refresh_token);
	    // we can set the tokens from config to client also
	    updateClientTokensFromConfig();
	}
	return result;

    }

    /**
     * Returns list of shows user have ever watched
     * 
     * @return shows list
     * @throws TraktLoadingFailedException
     *             if something fails
     */
    public List<TvShow> getWatchedShows() {

	List<BaseShow> body = doTraktCall(() -> trakt.sync()
		.watchedShows(Extended.DEFAULT_MIN).execute().body());

	return body.stream()
		.map(e -> new TvShow(e.show.title, e.show.ids.trakt))
		.collect(Collectors.toList());
    }

    /**
     * makes sure user is authorized, executes given method and wraps all
     * exceptions to {@link TraktLoadingFailedException}
     */
    private <V> V doTraktCall(Callable<V> object)
	    throws TraktLoadingFailedException {

	if (!authorized) {
	    throw new IllegalArgumentException("call authorizeTrakt() first!");
	}
	try {
	    // do the call
	    return object.call();
	} catch (Exception e) {
	    // if exception occurs
	    throw new TraktLoadingFailedException(e);
	}
    }

    /**
     * Returns next episode to watch or null if there are no episodes
     * 
     * @param show
     *            of which next episode we want to get
     * @return next episode or null
     * @throws TraktLoadingFailedException
     *             if something fails
     */
    public EpisodeToWatch getNextEpisode(TvShow show) {

	// load show progress
	BaseShow baseShow = doTraktCall(() -> trakt.shows()
		.watchedProgress(show.getTraktId().toString(), false, false,
			Extended.FULL)
		.execute().body());
	// get next episode
	Episode ep = baseShow.next_episode;
	EpisodeToWatch result = null;

	if (ep != null && ep.first_aired != null
		&& ep.first_aired.isBeforeNow()) {
	    String imdbId = ep.ids != null ? ep.ids.imdb : null;
	    // if episode exists and not aired yet return its data
	    result = new EpisodeToWatch(show, ep.title, imdbId, ep.season,
		    ep.number);

	}
	return result;
    }
}
