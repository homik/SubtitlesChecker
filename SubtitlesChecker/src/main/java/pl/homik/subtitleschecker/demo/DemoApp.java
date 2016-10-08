package pl.homik.subtitleschecker.demo;

import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

import pl.homik.subtitleschecker.MessageReceiver;
import pl.homik.subtitleschecker.SubtitlesChecker;
import pl.homik.subtitleschecker.model.EpisodeToWatch;
import pl.homik.subtitleschecker.model.EpisodeWithSubtitles;
import pl.homik.subtitleschecker.trakt.TraktAuthorizationPinProvider;
import pl.homik.subtitleschecker.trakt.config.RamTraktConfig;

public class DemoApp {

    private static final String POLISH = "pol";

    public static void main(String[] args) throws InterruptedException {

	AtomicBoolean jobFinished = new AtomicBoolean(false);
	Scanner scanner = new Scanner(System.in);

	SubtitlesChecker sc = new SubtitlesChecker(new RamTraktConfig(),
		new SysOutTraktAuthorizer(scanner),
		new SysOutMessageReceiver(jobFinished), POLISH);

	System.out.println("Press 1 for synchronized");
	System.out.println("Press 2 for asynchronized");

	int nextInt = scanner.nextInt();

	if (nextInt == 1) {
	    sc.startSync();
	} else if (nextInt == 2) {
	    sc.startAsync();
	}

	// just wait if async thread was used
	while (!jobFinished.get()) {
	    Thread.sleep(1);
	}

    }

    private static class SysOutTraktAuthorizer
	    implements TraktAuthorizationPinProvider {

	private Scanner input;

	public SysOutTraktAuthorizer(Scanner input) {
	    this.input = input;
	}

	@Override
	public String getAccessCode(String url) {
	    System.out.println(
		    "please visit url " + url + " and provide the pin code: ");
	    // read user input
	    return input.next();
	}

    }

    private static class SysOutMessageReceiver implements MessageReceiver {

	private AtomicBoolean jobFinished;

	public SysOutMessageReceiver(AtomicBoolean jobFinished) {
	    this.jobFinished = jobFinished;
	}

	@Override
	public void newEpisodeFound(EpisodeToWatch ep) {
	    System.out.println("Unwatched episode found " + asString(ep)
		    + " starting subtitles searching");
	}

	@Override
	public void errorOccured(Throwable e) {
	    System.out.println("ERROR!");
	    e.printStackTrace(System.err);
	    jobFinished.set(true);
	}

	@Override
	public void loadingComplete() {
	    System.out.println("search finished!");
	    jobFinished.set(true);
	}

	@Override
	public void subtitlesLoaded(EpisodeWithSubtitles e) {
	    // if no subtitles then not found
	    String searchResult = e.getSubtitles().isEmpty() ? " not found"
		    : " found";
	    System.out.println("Subtitles for episode "
		    + asString(e.getEpisode()) + " were " + searchResult);
	}

	private String asString(EpisodeToWatch ep) {
	    return ep.getShow().getName() + " S" + ep.getSeason() + "E"
		    + ep.getEpisode();
	}
    }

}
