package org.sbermarket.store;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Category {
    public final String name;
    public final String link;
    private final List<Category> children = new ArrayList<>();

    public Category(String name, String link) {
        this.name = name;
        this.link = String.format("%s%s", System.getenv("SBERMARKET_URL"), link);
    }

    public void addChild(Category category) {
        children.add(category);
    }

    public List<Category> getChildren() {
        return children;
    }

    public JSONObject toJson(Category originalCategory) {
        JSONObject category = new JSONObject();

        category.put("category", originalCategory.name);
        category.put("subCategory", this.name);
        category.put("link", this.link.replace(System.getenv("SBERMARKET_URL"), ""));

        return category;
    }
}
