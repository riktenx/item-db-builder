package com.example;

import com.example.model.ItemIndex;
import com.example.model.ItemRecord;
import com.google.gson.Gson;
import com.google.inject.Provides;

import javax.imageio.ImageIO;
import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileWriter;
import java.io.IOException;

@Slf4j
@PluginDescriptor(
        name = "Example"
)
public class ExamplePlugin extends Plugin {
    private static final File OUTPUT_DIRECTORY = new File("build", "generated-db");
    private static final File ICON_DIRECTORY = new File(OUTPUT_DIRECTORY, "icons");

    @Inject
    private Client client;
    @Inject
    private ClientThread clientThread;
    @Inject
    private ItemManager itemManager;
    @Inject
    private Gson gson;

    @Override
    protected void startUp() {
        clientThread.invoke(this::buildDB);
    }

    private void buildDB() {
        mkdirs();

        var index = new ItemIndex();
        for (var i = 0; i < client.getItemCount(); ++i) {
            var icon = itemManager.getImage(i);
            var outputFile = new File(ICON_DIRECTORY, i + ".png");
            try {
                ImageIO.write(icon, "png", outputFile);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            var composition = itemManager.getItemComposition(i);
            var record = ItemRecord.builder()
                    .name(composition.getName()).build();
            index.add(record);
        }

        index.build();
        var indexJson = gson.toJson(index);
        var indexFile = new File(OUTPUT_DIRECTORY, "index.json");
        try (var writer = new FileWriter(indexFile)) {
            writer.write(indexJson);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.exit(0);
    }

    private void mkdirs() {
        OUTPUT_DIRECTORY.mkdir();
        ICON_DIRECTORY.mkdir();
    }

    @Inject
    private ExampleConfig config;

    @Provides
    ExampleConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(ExampleConfig.class);
    }
}
