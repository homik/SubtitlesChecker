package pl.homik.subtitleschecker;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import pl.homik.subtitleschecker.model.EpisodeToWatch;
import pl.homik.subtitleschecker.model.EpisodeWithSubtitles;
import pl.homik.subtitleschecker.model.TvShow;
import pl.homik.subtitleschecker.model.Subtitles;
import pl.homik.subtitleschecker.opensubtitles.ScOpenSubtitlesClient;
import pl.homik.subtitleschecker.trakt.ScTraktClient;
import pl.homik.subtitleschecker.trakt.TraktAuthorizationPinProvider;
import pl.homik.subtitleschecker.trakt.config.TraktConfig;
import rx.Observable;
import rx.Scheduler;
import rx.schedulers.Schedulers;

/**
 * Class created for checking if subtitles are available for new episodes of
 * shows user is currently watching.
 * 
 * Shows data is retrieved from TRAKT.TV service. Subtitles availability is
 * checked on OPENSUBTITLES.ORG.
 * 
 * @author Pawel Chwedorowicz
 *
 */
public class SubtitlesChecker {

    private final ScTraktClient trakt;
    private final MessageReceiver msgReceiver;
    private final ScOpenSubtitlesClient opemSubtitlesClient;

    /**
     * Constructor
     * 
     * @param config
     *            object which provides and stores trakt authorization tokens
     * @param authorizer
     *            object which interacts with user if trakt authorization is
     *            required
     * @param msgReceiver
     *            object which receives search result informations
     * @param language
     *            for which subtitles should be checked (ISO639-3 language code)
     */
    public SubtitlesChecker(TraktConfig config,
	    TraktAuthorizationPinProvider authorizer,
	    MessageReceiver msgReceiver, String language) {

	this.msgReceiver = msgReceiver;
	this.trakt = new ScTraktClient(config, authorizer);
	this.opemSubtitlesClient = new ScOpenSubtitlesClient(language);
    }

    /**
     * Starts subtitle search job asynchronously using
     * {@link Executors#newWorkStealingPool()} as tasks executor
     */
    public void startAsync() {
	ExecutorService executor = Executors.newWorkStealingPool();
	start(executor, () -> executor.shutdown());
    }

    /**
     * Starts subtitle search job synchronously (blocking)
     */
    public void startSync() {

	Executor directExecutor = command -> command.run();
	start(directExecutor, null);
    }

    /**
     * Start subtitle search job using given executor
     * 
     * @param jobExecutor
     *            will be used to execute whole search process
     * @param doFinally
     *            code which should be execute after job is done (either
     *            successfully or not), may be null
     */
    public void start(Executor jobExecutor, Runnable doFinally) {

	Scheduler jobScheduler = Schedulers.from(jobExecutor);
	Observable<EpisodeWithSubtitles> obs = Observable
		.fromCallable(() -> getShows())
		// observe on executor
		.observeOn(jobScheduler).subscribeOn(jobScheduler)
		// flatten the collection to single element
		.flatMap(e -> Observable.from(e))
		// get next episode
		.flatMap(e -> loadNextEpisode(e))
		// skip if has no next episode
		.filter(e -> e != null)
		// inform that new episode was found
		.doOnNext(e -> msgReceiver.newEpisodeFound(e))
		// second observe on so we can do simultaneously subtitles with
		// episode loading
		.observeOn(jobScheduler)
		// get subtitles for episode
		.map(e -> getSubtitles(e))
		// inform about subtitles
		.doOnNext(e -> msgReceiver.subtitlesLoaded(e))
		.doOnError(e -> msgReceiver.errorOccured(e))
		.doOnCompleted(() -> {
		    msgReceiver.loadingComplete();
		});
	// if doFinally available then add it
	if (doFinally != null) {
	    obs = obs.doAfterTerminate(() -> doFinally.run());
	}
	obs.subscribe();

    }

    private Observable<EpisodeToWatch> loadNextEpisode(TvShow e) {
	// sometimes getting next episode timeouts so we retry
	return Observable.fromCallable(() -> extracted(e)).retry(2);
    }

    private EpisodeToWatch extracted(TvShow e) {
	return trakt.getNextEpisode(e);
    }

    private EpisodeWithSubtitles getSubtitles(EpisodeToWatch e) {

	// make sure logged in
	opemSubtitlesClient.login();

	List<Subtitles> subs;

	if (e.getImdbId() != null) {
	    // load by id if can
	    subs = opemSubtitlesClient.getSubtitlesByImdbId(e.getImdbId());
	} else {
	    // if has no imbdId try to search by name and episode
	    subs = opemSubtitlesClient.getSubtitlesByNameAndEpisodeNo(
		    e.getShow().getName(), e.getSeason(), e.getEpisode());
	}
	return new EpisodeWithSubtitles(e, subs);

    }

    private List<TvShow> getShows() {
	// first authorization is requried
	trakt.authorizeTrakt();
	// then return all ever watched shows
	return trakt.getWatchedShows();
    }

}
