package com.alfalfascout.CustomVillageTrades;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

// helper class for loading and saving config files 
public class FileAndConfig {
    private File file;
    private Reader reader;
    public FileConfiguration conf;
    
    public FileAndConfig(File f, FileConfiguration c) {
        this.file = f;
        this.conf = c;
        this.reader = null;
    }
    
    public FileAndConfig(Reader r) {
        this.file = null;
        this.reader = r;
        this.conf = YamlConfiguration.loadConfiguration(r);
    }
    
    public FileAndConfig(File f) {
        this.file = f;
        this.conf = YamlConfiguration.loadConfiguration(f);
        this.reader = null;
    }
    
    public String toString() {
        String fileString = " No file.";
        if (!(this.file == null)) {
            fileString = " File: " + this.file.toString() + ".";
        }
        
        String readerString = " No reader.";
        if (!(this.reader == null)) {
            readerString = " Reader: " + this.reader.toString() + ".";
        }
        
        String confString = " FileConfiguration: " + this.conf.toString() + ".";

        return "FileAndConfig Object. " +
                fileString + readerString + confString;
    }
    
    // if this config has a file, save it. otherwise, ignore it
    public void save() {
        if (this.file != null) {
            try {
                this.conf.save(this.file);
            } 
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    // either load it from the file or the reader, depending which exists
    public void load() {
        if (!this.file.equals(null)) {
            try {
                this.conf.load(this.file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InvalidConfigurationException e) {
                e.printStackTrace();
            }
        }
        else {
            this.conf = YamlConfiguration.loadConfiguration(this.reader);
        }
    }
}
