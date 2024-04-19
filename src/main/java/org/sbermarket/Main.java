package org.sbermarket;

import org.sbermarket.manager.MarketManager;
import org.sbermarket.settings.Settings;

public class Main {
    public static void main(String[] args) {
        Settings settings = new Settings();

        MarketManager manager = new MarketManager(settings);
        manager.start();
    }
}