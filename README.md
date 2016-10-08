# SubtitlesChecker
Java project which uses Trakt.tv and Opensubtitles.org to check if there are subtitles available for tvshows you watch.
Its created with

- [RxJava][1] 
- [opensub4j][2] for opensubtitles.org api
- [trakt-java][3] for trakt.tv api

# Demo example


```java
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

	// just a flag for async thread to check for finished
	AtomicBoolean jobFinished = new AtomicBoolean(false);
	// user input
	Scanner scanner = new Scanner(System.in);

	// the engine
	SubtitlesChecker sc = new SubtitlesChecker(new RamTraktConfig(),
		new SysOutTraktAuthorizer(scanner),
		new SysOutMessageReceiver(jobFinished), POLISH);

	System.out.println("Press 1 for synchronized");
	System.out.println("Press 2 for asynchronized");

	int nextInt = scanner.nextInt();

	if (nextInt == 1) {
	    // starts in synchronous way
	    sc.startSync();
	} else if (nextInt == 2) {
	    // starts in asynchronous way
	    sc.startAsync();
	}

	// wait for finish if async thread was used
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

```

# Example output
```
Press 1 for synchronized
Press 2 for asynchronized
2
please visit url https://trakt.tv/oauth/authorize?response_type=code&redirect_uri=urn%3Aietf%3Awg%3Aoauth%3A2.0%3Aoob&state=subtitlesCheck1475911117318&client_id=fcce239c0d9f7d9ef29dc95480a15101998cb26bf6fb06f5dccc551bb11a0e29 and provide the pin code: 
620BED03
Unwatched episode found Narcos S1E9 starting subtitles searching
Unwatched episode found Family Guy S15E2 starting subtitles searching
Unwatched episode found The Big Bang Theory S10E3 starting subtitles searching
Unwatched episode found The League S4E4 starting subtitles searching
Unwatched episode found The IT Crowd S3E5 starting subtitles searching
Unwatched episode found Modern Family S8E2 starting subtitles searching
Unwatched episode found South Park S20E3 starting subtitles searching
Subtitles for episode Narcos S1E9 were  found
Subtitles for episode Family Guy S15E2 were  found
Subtitles for episode The Big Bang Theory S10E3 were  found
Subtitles for episode The League S4E4 were  not found
Subtitles for episode The IT Crowd S3E5 were  found
Subtitles for episode Modern Family S8E2 were  found
Subtitles for episode South Park S20E3 were  found
Unwatched episode found The Simpsons S28E1 starting subtitles searching
Subtitles for episode The Simpsons S28E1 were  not found
Unwatched episode found American Dad! S13E5 starting subtitles searching
Subtitles for episode American Dad! S13E5 were  not found
Unwatched episode found Shameless S7E1 starting subtitles searching
Subtitles for episode Shameless S7E1 were  not found
Unwatched episode found American Horror Story S5E1 starting subtitles searching
Subtitles for episode American Horror Story S5E1 were  found
Unwatched episode found True Detective S2E1 starting subtitles searching
Subtitles for episode True Detective S2E1 were  found
search finished!
```

[1]: https://github.com/ReactiveX/RxJava
[2]: https://github.com/wtekiela/opensub4j
[3]: https://github.com/UweTrottmann/trakt-java/
