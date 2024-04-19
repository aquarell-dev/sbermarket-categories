package org.sbermarket.manager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.sbermarket.settings.Settings;
import org.sbermarket.store.Category;
import org.sbermarket.store.Store;

import java.util.HashSet;
import java.util.List;

public class Market {
    private final Settings settings;
    private final Logger logger = LogManager.getLogger(Market.class);
    private final HashSet<Store> stores;
    private WebDriver driver;

    public Market(Settings settings, HashSet<Store> stores) {
        this.settings = settings;
        this.stores = stores;
    }

    public void start() {
        this.driver = getDriver();
        logger.info(String.format("Драйвер успешно запущен. (stores=%d)", stores.size()));
        stores.forEach(this::fetchCategories);
        this.driver.quit();
    }

    public List<JSONObject> getJsonStores() {
        return stores.stream()
            .map(Store::toJson)
            .toList();
    }

    private WebDriver getDriver() {
        ChromeOptions capabilities = new ChromeOptions();
        capabilities.addArguments("--window-size=1920,1080");

        return new RemoteWebDriver(settings.getSeleniumGridUrl(), capabilities);
    }

    private void fetchCategories(Store store) {
        List<Category> firstLevelCategories = getFirstLevelCategories(store);

        logger.info(String.format("Собраны категории первого уровня магазина %s(%s)", store.store, store.sid));

        firstLevelCategories.forEach(category -> {
            store.addCategory(category);

            List<Category> secondLevelCategories = getSecondLevelCategories(store, category);

            logger.info(String.format(
                "Собраны подкатегории категории '%s' второго уровня магазина %s(%s)",
                category.name,
                store.store,
                store.sid
            ));
            secondLevelCategories.forEach(category::addChild);
        });

        logger.info(String.format("Собраны категории второго уровня магазина %s(%s)", store.store, store.sid));
    }

    private List<Category> getFirstLevelCategories(Store store) {
        try {
            this.driver.get(store.link);
        } catch (WebDriverException e) {
            logger.error(String.format("Не удалось получить список категорий магазина %s(%s)", store.store, store.sid));
        }

        Document doc = Jsoup.parse(this.driver.getPageSource());

        Elements categories = doc.select("[class^=RootCatalog_list] li a");

        return categories.stream()
            .map(category -> new Category(
                category.children()
                    .select("span")
                    .text(),
                category.attribute("href")
                    .getValue()
            ))
            .toList();
    }

    private List<Category> getSecondLevelCategories(Store store, Category category) {
        try {
            this.driver.get(category.link);
        } catch (WebDriverException e) {
            logger.error(String.format(
                "Не удалось получить список подкатегорий магазина %s(%s) в категории '%s'",
                store.store,
                store.sid,
                category.name
            ));
        }

        Document doc = Jsoup.parse(this.driver.getPageSource());

        Elements categories = doc.select("[class^=NavigationTreeItem_root] a");

        return categories.stream()
            .map(subCategory -> new Category(
                subCategory.text(),
                subCategory.attribute("href")
                    .getValue()
            ))
            .toList();
    }
}
