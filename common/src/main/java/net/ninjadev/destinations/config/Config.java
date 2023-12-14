package net.ninjadev.destinations.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;

public abstract class Config {

    private static final Gson GSON = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().create();
    protected String root = "config/Destinations/";
    protected String extension = ".json";

    public void generateConfig() {
        this.reset();

        this.writeConfig();
    }

    private File getConfigFile() {
        return new File(this.root + this.getName() + this.extension);
    }

    public abstract String getName();

    @SuppressWarnings("unchecked")
    public <T extends Config> T readConfig() {
        try {
            return (T) GSON.fromJson(new FileReader(this.getConfigFile()), this.getClass());
        } catch (FileNotFoundException e) {
            this.generateConfig();
        }

        return (T) this;
    }

    protected abstract void reset();

    public void writeConfig() {
        try (FileWriter writer = new FileWriter(this.getConfigFile())) {
            File dir = new File(this.root);
            if (!dir.exists() && !dir.mkdirs()) return;
            if (!this.getConfigFile().exists() && !this.getConfigFile().createNewFile()) return;
            GSON.toJson(this, writer);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
