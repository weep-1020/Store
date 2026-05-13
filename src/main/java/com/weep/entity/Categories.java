package com.weep.entity;

import java.util.ArrayList;
import java.util.List;

public class Categories {
    private int id;
    private String category_name;  //分类名称

    private int parent_id;  //父分类ID，0表示顶级分类
    private String description; //分类描述
    private int sort_order;  //排序顺序
    private List<Categories> children;  //子分类列表（非数据库字段）

    public Categories() {
    }

    public Categories(int id, String category_name, int parent_id, String description, int sort_order) {
        this.id = id;
        this.category_name = category_name;
        this.parent_id = parent_id;
        this.description = description;
        this.sort_order = sort_order;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCategory_name() {
        return category_name;
    }

    public void setCategory_name(String category_name) {
        this.category_name = category_name;
    }

    public int getParent_id() {
        return parent_id;
    }

    public void setParent_id(int parent_id) {
        this.parent_id = parent_id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getSort_order() {
        return sort_order;
    }

    public void setSort_order(int sort_order) {
        this.sort_order = sort_order;
    }

    public List<Categories> getChildren() {
        return children;
    }

    public void setChildren(List<Categories> children) {
        this.children = children;
    }

    @Override
    public String toString() {
        return "Categories{" +
                "id=" + id +
                ", category_name='" + category_name + '\'' +
                ", parent_id=" + parent_id +
                ", description='" + description + '\'' +
                ", sort_order=" + sort_order +
                '}';
    }
}
