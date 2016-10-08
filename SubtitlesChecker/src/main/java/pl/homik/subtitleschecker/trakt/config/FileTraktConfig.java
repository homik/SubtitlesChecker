package pl.homik.subtitleschecker.trakt.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

/**
 * Implementation of {@link TraktConfig} which uses file to store and load
 * config
 * 
 * @author Pawel Chwedorowicz
 *
 */
public class FileTraktConfig implements TraktConfig {

    private static final String PROP_ACCESS_TOKEN = "accessToken";
    private static final String PROP_REFRESH_TOKEN_KEY = "refreshToken";

    private final Properties props;
    private final File configFile;

    /**
     * Constructor
     * 
     * @param configFile
     *            which will be used to store and load config
     */
    public FileTraktConfig(File configFile) {
	this.configFile = configFile;
	props = new Properties();
	if (configFile.exists()) {
	    try (InputStream io = new FileInputStream(configFile)) {
		props.load(io);
	    } catch (IOException e) {
		throw new IllegalStateException("cant read props");
	    }
	}

    }

    @Override
    public String getAccesToken() {
	return props.getProperty(PROP_ACCESS_TOKEN);
    }

    @Override
    public String getRefreshToken() {
	return props.getProperty(PROP_REFRESH_TOKEN_KEY);
    }

    @Override
    public void setAuthId(String accessToken, String refreshToken) {
	props.setProperty(PROP_ACCESS_TOKEN, accessToken);
	props.setProperty(PROP_REFRESH_TOKEN_KEY, refreshToken);
	saveProperties();
    }

    private void saveProperties() {
	try (OutputStream os = new FileOutputStream(configFile)) {
	    props.store(os, null);
	} catch (IOException e) {
	    throw new IllegalStateException("cant save props");
	}
    }

}
