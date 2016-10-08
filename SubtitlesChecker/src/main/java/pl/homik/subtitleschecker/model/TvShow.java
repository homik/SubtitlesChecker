package pl.homik.subtitleschecker.model;

public class TvShow {

    private final String name;
    private final Integer traktId;

    public TvShow(String name, Integer traktId) {
	super();
	this.name = name;
	this.traktId = traktId;
    }

    public String getName() {
	return name;
    }

    public Integer getTraktId() {
	return traktId;
    }

}
