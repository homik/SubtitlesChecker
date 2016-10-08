package pl.homik.subtitleschecker.model;

public class EpisodeToWatch {

    private final String imdbId;
    private final Integer season;
    private final Integer episode;
    private final String name;
    private final TvShow show;

    public EpisodeToWatch(TvShow show, String name, String imdbId,
	    Integer season, Integer episode) {
	this.show = show;
	this.name = name;
	this.imdbId = imdbId;
	this.season = season;
	this.episode = episode;
    }

    public Integer getSeason() {
	return season;
    }

    public Integer getEpisode() {
	return episode;
    }

    public String getImdbId() {
	return imdbId;
    }

    public String getName() {
	return name;
    }

    public TvShow getShow() {
	return show;
    }

}
