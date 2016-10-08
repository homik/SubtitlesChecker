package pl.homik.subtitleschecker.model;

import java.util.List;

public class EpisodeWithSubtitles {

    private final EpisodeToWatch episode;
    private final List<Subtitles> subtitles;

    public EpisodeWithSubtitles(EpisodeToWatch episode,
	    List<Subtitles> subtitles) {
	this.episode = episode;
	this.subtitles = subtitles;
    }

    public List<Subtitles> getSubtitles() {
	return subtitles;
    }

    public EpisodeToWatch getEpisode() {
	return episode;
    }
}
