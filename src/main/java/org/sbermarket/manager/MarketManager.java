package org.sbermarket.manager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.sbermarket.settings.Settings;
import org.sbermarket.store.Store;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MarketManager {
    private final ExecutorService executorService;
    private final HashSet<Store> stores;
    private final Settings settings;
    private final Logger logger = LogManager.getLogger(MarketManager.class);
    private final List<JSONObject> records = new ArrayList<>();

    public MarketManager(Settings settings) {
        this.settings = settings;
        this.stores = settings.getStores();
        this.executorService = Executors.newFixedThreadPool(settings.getNumberOfThreads());
    }

    public void start() {
        List<HashSet<Store>> batches = Store.splitIntoBatches(stores, settings.getNumberOfThreads());

        CountDownLatch latch = new CountDownLatch(batches.size());

        batches.forEach(stores -> executorService.submit(() -> {
            try {
                Market market = new Market(settings, stores);
                market.start();
                records.addAll(market.getJsonStores());
                latch.countDown();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }));

        try {
            latch.await();
        } catch (InterruptedException e) {
            return;
        }

        executorService.shutdown();
        saveCategories();
    }

    private void saveCategories() {
        JSONObject categories = new JSONObject();

        LocalDateTime now = LocalDateTime.now();

        // Format the date and time as required
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy-HH-mm");
        String formattedDateTime = now.format(formatter);

        Path filePath = Paths.get("output")
            .resolve(String.format("sbermarket-%s.json", formattedDateTime));

        records.forEach(record -> categories.put(record.getString("sid"), record));

        try (FileWriter fileWriter = new FileWriter(filePath.toAbsolutePath().toFile())) {
            fileWriter.write(categories.toString(2));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
