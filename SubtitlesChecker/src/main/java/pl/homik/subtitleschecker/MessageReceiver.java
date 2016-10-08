package pl.homik.subtitleschecker;

import pl.homik.subtitleschecker.model.EpisodeToWatch;
import pl.homik.subtitleschecker.model.EpisodeWithSubtitles;

/**
 * Base interface used by {@link SubtitlesChecker} to announce search job
 * progress
 * 
 * @author Pawel Chwedorowicz
 *
 */
public interface MessageReceiver {

    /**
     * Called when unwatched episode was found. Subtitle search jobs will start
     * for this episode.
     * 
     * @param ep
     *            episode data
     */
    void newEpisodeFound(EpisodeToWatch ep);

    /**
     * Called when exception occurs during subtitle search
     * 
     * @param e
     *            Exception
     */
    void errorOccured(Throwable e);

    /**
     * Called when all search jobs are done successfully
     */
    void loadingComplete();

    /**
     * Called when subtitle search job was finished for episode
     * 
     * @param e 
     */
    void subtitlesLoaded(EpisodeWithSubtitles e);

}
