package fr.atesab.horsedebug;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.io.Writer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class HorseConfig {
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static HorseConfig load(File f) {
        try (Reader r = new FileReader(f)) {
            return GSON.fromJson(r, HorseConfig.class);
        } catch (Exception e) {
            return new HorseConfig();
        }
    }

    public boolean show3d = true;

    public void save(File f) {
        try (Writer w = new FileWriter(f)) {
            GSON.toJson(this, w);
        } catch (Exception e) {
        }
    }
}
