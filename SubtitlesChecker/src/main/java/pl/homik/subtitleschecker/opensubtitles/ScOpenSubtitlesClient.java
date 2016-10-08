package pl.homik.subtitleschecker.opensubtitles;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.xmlrpc.XmlRpcException;

import com.github.wtekiela.opensub4j.impl.OpenSubtitlesImpl;
import com.github.wtekiela.opensub4j.response.SubtitleInfo;

import pl.homik.subtitleschecker.model.Subtitles;

/**
 * Class provides basic access to OPENSUBTITLES.ORG service
 * 
 * @author Pawel Chwedorowicz
 *
 */
public class ScOpenSubtitlesClient {

    private static final String OS_API_URL = "http://api.opensubtitles.org:80/xml-rpc";

    private final OpenSubtitlesImpl os;
    private String language;
    private boolean logged;

    public ScOpenSubtitlesClient(String language) {
	this.language = language;
	try {
	    os = new OpenSubtitlesImpl(new URL(OS_API_URL));
	} catch (MalformedURLException e) {
	    // this should not happen
	    throw new IllegalStateException(
		    "for some reason url " + OS_API_URL + " is wrong", e);
	}

    }

    /**
     * 
     * Logins to open subtitle service so other methods of this class can be
     * called. If already logged in nothing happens.
     * 
     * @throws OpenSubtitlesLoginFailedException
     *             if something goes wrong
     */
    public void login() {
	if (!logged) {
	    try {
		os.login(language, "homikos");
		logged = true;
	    } catch (XmlRpcException e) {
		throw new OpenSubtitlesLoginFailedException(e);
	    }
	}
    }

    /**
     * Executes search for subtitles for episode of given IMDB id
     * 
     * @param imdbId
     *            id of episode
     * @return list of subtitles available or empty list
     * 
     * @throws OpenSubtitlesSearchFailedException
     *             if somethings goes wrong
     */
    public List<Subtitles> getSubtitlesByImdbId(String imdbId) {

	makeSureLogged();
	// imdb ids starts with 'tt' but opensubtitles just needs the number
	// behind
	String searchId = imdbId.startsWith("tt") ? imdbId.substring(2)
		: imdbId;
	try {
	    return convert(os.searchSubtitles(language, searchId));
	} catch (XmlRpcException e) {
	    throw new OpenSubtitlesSearchFailedException(e,
		    "cant load subs for show: " + searchId);
	}
    }

    /**
     * Executes search for subtitles for episode of given episode deaitls
     * 
     * @param showName
     *            name of tv show
     * @param season
     *            number of season
     * @param episode
     *            number of episode
     * @return list of subtitles available or empty list
     * 
     * @throws OpenSubtitlesSearchFailedException
     *             if somethings goes wrong
     */
    public List<Subtitles> getSubtitlesByNameAndEpisodeNo(String showName,
	    Integer season, Integer episode)
	    throws OpenSubtitlesSearchFailedException {
	makeSureLogged();
	if (season == null || episode == null) {
	    return Collections.emptyList();
	}

	try {
	    return convert(os.searchSubtitles(language, showName,
		    season.toString(), episode.toString()));
	} catch (XmlRpcException e) {
	    throw new OpenSubtitlesSearchFailedException(e,
		    "cant load subs for show: " + showName);
	}
    }

    private List<Subtitles> convert(List<SubtitleInfo> searchSubtitles) {
	if (searchSubtitles == null) {
	    return Collections.emptyList();
	}
	return searchSubtitles.stream()
		.map(e -> new Subtitles(e.getFileName(), e.getDownloadLink()))
		.collect(Collectors.toList());
    }

    private void makeSureLogged() {
	if (!logged) {
	    throw new IllegalStateException(
		    "you must call login() method first");

	}
    }

}
