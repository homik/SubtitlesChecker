package pl.homik.subtitleschecker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import pl.homik.subtitleschecker.model.EpisodeToWatch;
import pl.homik.subtitleschecker.model.EpisodeWithSubtitles;
import pl.homik.subtitleschecker.model.Subtitles;
import pl.homik.subtitleschecker.model.TvShow;
import pl.homik.subtitleschecker.opensubtitles.ScOpenSubtitlesClient;
import pl.homik.subtitleschecker.trakt.ScTraktClient;

@RunWith(MockitoJUnitRunner.class)
public class SubtitlesCheckerTest {

    @Test
    public void shoulDoNormalLoading() {
	// given
	TestMessageReciver msgReceiver = new TestMessageReciver();
	ScTraktClient trakt = Mockito.mock(ScTraktClient.class);
	TvShow show1 = new TvShow("test1", 1);
	TvShow show2 = new TvShow("test2", 2);
	TvShow show3 = new TvShow("test3", 3);
	List<TvShow> shows = Arrays.asList(show1, show2, show3);
	Mockito.when(trakt.getWatchedShows()).thenReturn(shows);

	EpisodeToWatch episodeWithSubtitles = new EpisodeToWatch(show1, "ep1",
		"id", 1, 1);
	EpisodeToWatch episodeWithoutSubtitles = new EpisodeToWatch(show2,
		"ep1", null, 1, 1);

	Mockito.when(trakt.getNextEpisode(show1))
		.thenReturn(episodeWithSubtitles);
	Mockito.when(trakt.getNextEpisode(show2))
		.thenReturn(episodeWithoutSubtitles);
	Mockito.when(trakt.getNextEpisode(show3)).thenReturn(null);

	ScOpenSubtitlesClient scOpenSubtitlesClient = Mockito
		.mock(ScOpenSubtitlesClient.class);

	Subtitles subtitles = new Subtitles("name", "link");
	Mockito.when(scOpenSubtitlesClient
		.getSubtitlesByImdbId(episodeWithSubtitles.getImdbId()))
		.thenReturn(Arrays.asList(subtitles));

	Mockito.when(scOpenSubtitlesClient.getSubtitlesByNameAndEpisodeNo(
		Mockito.anyObject(), Mockito.anyObject(), Mockito.anyObject()))
		.thenReturn(Collections.emptyList());

	// when
	SubtitlesChecker checker = new SubtitlesChecker(msgReceiver, trakt,
		scOpenSubtitlesClient);
	checker.startSync();

	// then
	Assertions.assertThat(msgReceiver.complete).isTrue();

	Assertions.assertThat(msgReceiver.foundEps).hasSize(2)
		.containsOnly(episodeWithoutSubtitles, episodeWithSubtitles);
	Assertions.assertThat(msgReceiver.errors).isEmpty();

	Assertions.assertThat(msgReceiver.subtitleSearchResults).hasSize(2);

	Optional<EpisodeWithSubtitles> findFirst = msgReceiver.subtitleSearchResults
		.stream().filter(e -> e.getEpisode() == episodeWithoutSubtitles)
		.findFirst();
	Assertions.assertThat(findFirst.isPresent()).isTrue();
	Assertions.assertThat(findFirst.get().getSubtitles()).isEmpty();

	Optional<EpisodeWithSubtitles> findFirst2 = msgReceiver.subtitleSearchResults
		.stream().filter(e -> e.getEpisode() == episodeWithSubtitles)
		.findFirst();
	Assertions.assertThat(findFirst2.isPresent()).isTrue();
	Assertions.assertThat(findFirst2.get().getSubtitles())
		.containsOnly(subtitles);

    }

    private static class TestMessageReciver implements MessageReceiver {

	List<EpisodeToWatch> foundEps = new ArrayList<>();
	List<Throwable> errors = new ArrayList<>();
	List<EpisodeWithSubtitles> subtitleSearchResults = new ArrayList<>();
	boolean complete = false;

	@Override
	public void newEpisodeFound(EpisodeToWatch ep) {
	    foundEps.add(ep);
	}

	@Override
	public void errorOccured(Throwable e) {
	    errors.add(e);
	}

	@Override
	public void loadingComplete() {
	    complete = true;
	}

	@Override
	public void subtitlesLoaded(EpisodeWithSubtitles e) {
	    subtitleSearchResults.add(e);
	}

    }

}
