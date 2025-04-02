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

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@PluginDescriptor(name = "Example")
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
            var iconData = new ByteArrayOutputStream();
            try {
                ImageIO.write(icon, "png", iconData);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            var composition = itemManager.getItemComposition(i);
            var record = ItemRecord.builder().id(i).name(composition.getName()).iconPng(Base64.getEncoder().encodeToString(iconData.toByteArray())).build();
            index.add(record);

            var outputFile = new File(ICON_DIRECTORY, i + ".png");
            try (var writer = new FileOutputStream(outputFile)) {
                writer.write(iconData.toByteArray());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        var indexJson = gson.toJson(index);
        var indexFile = new File(OUTPUT_DIRECTORY, "index.json");
        try (var writer = new FileWriter(indexFile)) {
            writer.write(indexJson);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        buildSpriteDB();

        System.exit(0);
    }

    private void buildSpriteDB() {
        var index = new ArrayList<List<String>>();

        for (var i = 0; true; ++i) {
            var sprites = client.getSprites(client.getIndexSprites(), i, 0);
            if (sprites == null) {
                break;
            }

            var pngs = new ArrayList<String>();
            for (var sprite : sprites) {
                if (sprite.getWidth() == 0 || sprite.getHeight() == 0) {
                    pngs.add(null);
                    continue;
                }

                var png = toPNG(sprite.toBufferedImage());
                var b64 = toB64(png);
                pngs.add(b64);
            }

            index.add(pngs);
        }

        var indexJson = gson.toJson(index);
        var indexFile = new File(OUTPUT_DIRECTORY, "sprites.json");
        try (var writer = new FileWriter(indexFile)) {
            writer.write(indexJson);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private byte[] toPNG(BufferedImage image) {
        var out = new ByteArrayOutputStream();

        try {
            ImageIO.write(image, "png", out);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return out.toByteArray();
    }

    private String toB64(byte[] p) {
        return Base64.getEncoder().encodeToString(p);
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
