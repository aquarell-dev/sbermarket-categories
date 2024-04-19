package org.sbermarket.settings;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.sbermarket.store.Store;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;

public class Settings {
    private final String seleniumGridUrl;
    private final String sbermarketUrl;
    private final Integer numberOfThreads;
    private final HashSet<Store> stores;
    private final Logger logger = LogManager.getLogger(Settings.class);

    public Settings() {
        this.sbermarketUrl = System.getenv("SBERMARKET_URL");
        this.seleniumGridUrl = System.getenv("GRID_URL");
        this.numberOfThreads = Integer.parseInt(System.getenv("THREADS"));
        this.stores = getStoresFromJson();
        logger.info(String.format("Конфигурация. Кол-во магазинов: %d; кол-во потоков: %d",
            stores.size(),
            numberOfThreads
        ));
    }

    public String getSbermarketUrl() {
        return sbermarketUrl;
    }

    public HashSet<Store> getStores() {
        return stores;
    }

    public URL getSeleniumGridUrl() {
        try {
            return new URI(seleniumGridUrl).toURL();
        } catch (URISyntaxException | MalformedURLException | IllegalArgumentException e) {
            logger.fatal("Невалидная ссылка драйвера");
            return null;
        }
    }

    public Integer getNumberOfThreads() {
        return this.numberOfThreads;
    }

    private HashSet<Store> getStoresFromJson() {
        JSONObject storeConfiguration = loadJSONFromResource("sids.json");

        HashSet<Store> stores = new HashSet<>();

        storeConfiguration.keySet().forEach(key -> {
            JSONArray sids = storeConfiguration.getJSONArray(key);

            for (int i = 0; i < sids.length(); i++) {
                stores.add(new Store(key, sids.getString(i)));
            }
        });

        return stores;
    }

    private JSONObject loadJSONFromResource(String fileName) {
        try (InputStream inputStream = Settings.class.getClassLoader()
            .getResourceAsStream(fileName)) {
            if (inputStream == null) {
                logger.fatal("Файл конфиуграции магазинов не найден");
                System.exit(1);
            }

            byte[] bytes = inputStream.readAllBytes();
            String jsonString = new String(bytes, StandardCharsets.UTF_8);

            return new JSONObject(jsonString);
        } catch (IOException e) {
            logger.fatal("Произошла ошибка при обработке файла конфигурации магазинов");
            System.exit(1);
            return null;
        }
    }
}