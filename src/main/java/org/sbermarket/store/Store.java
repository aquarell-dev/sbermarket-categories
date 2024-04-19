package org.sbermarket.store;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public class Store {
    public final String store;
    public final String sid;
    public final String link;
    private final List<Category> categories = new ArrayList<>();

    public Store(String store, String sid) {
        this.store = store;
        this.sid = sid;
        this.link = String.format("%s%s?sid=%s", System.getenv("SBERMARKET_URL"), store, sid);
    }

    /**
     * Разбивает список магазинов на равные части для того,
     * чтобы каждый поток собирал данные с равной части магазинов
     *
     * @param set        список магазинов
     * @param numBatches кол-во равный частей
     * @param <T>        тип списка
     * @return равные части магазинов
     */
    public static <T> List<HashSet<T>> splitIntoBatches(HashSet<T> set, int numBatches) {
        int batchSize = set.size() / numBatches;
        List<HashSet<T>> batches = new ArrayList<>();
        HashSet<T> currentBatch = new HashSet<>();
        int count = 0;

        for (T item : set) {
            if (count == batchSize && batches.size() < numBatches - 1) {
                batches.add(currentBatch);
                currentBatch = new HashSet<>();
                count = 0;
            }
            currentBatch.add(item);
            count++;
        }

        if (!currentBatch.isEmpty()) {
            batches.add(currentBatch);
        }

        return batches;
    }

    public void addCategory(Category category) {
        categories.add(category);
    }

    public JSONObject toJson() {
        JSONObject store = new JSONObject();

        store.put("store", this.store);
        store.put("sid", this.sid);
        store.put("categories", getCategories());

        return store;
    }

    private JSONArray getCategories() {
        return new JSONArray(categories.stream()
            .flatMap(category -> category.getChildren()
                .stream()
                .map(subCategory -> subCategory.toJson(category)))
            .toList());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Store store = (Store) o;
        return Objects.equals(sid, store.sid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sid);
    }
}
