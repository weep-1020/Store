package com.weep.entity;

import java.util.Date;

public class Products {
    private String spid;  //商品编号
    private String p_name;  //商品名称
    private String pic;  //商品图片ID
    private Double price;  //商品价格
    private String description;  //商品描述
    private Integer stock;   //库存数量
    private String category_id;  //商品分类ID（关联categories表）
    private String status;  //状态：1-上架，0-下架
    private Date created_at;  //创建时间
    private Date updated_at;  //更新时间


    public Products() {
    }

    public Products(String spid, String p_name, String pic,
                    Double price, String description, Integer stock,
                    String category_id, String status, Date created_at,
                    Date updated_at) {
        this.spid = spid;
        this.p_name = p_name;
        this.pic = pic;
        this.price = price;
        this.description = description;
        this.stock = stock;
        this.category_id = category_id;
        this.status = status;
        this.created_at = created_at;
        this.updated_at = updated_at;
    }

    public String getSpid() {
        return spid;
    }

    public void setSpid(String spid) {
        this.spid = spid;
    }

    public String getP_name() {
        return p_name;
    }

    public void setP_name(String p_name) {
        this.p_name = p_name;
    }

    public String getPic() {
        return pic;
    }

    public void setPic(String pic) {
        this.pic = pic;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public String getCategory_id() {
        return category_id;
    }

    public void setCategory_id(String category_id) {
        this.category_id = category_id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Date created_at) {
        this.created_at = created_at;
    }

    public Date getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(Date updated_at) {
        this.updated_at = updated_at;
    }

    @Override
    public String toString() {
        return "Products{" +
                "spid='" + spid + '\'' +
                ", p_name='" + p_name + '\'' +
                ", pic='" + pic + '\'' +
                ", price=" + price +
                ", description='" + description + '\'' +
                ", stock=" + stock +
                ", category_id='" + category_id + '\'' +
                ", status='" + status + '\'' +
                ", created_at=" + created_at +
                ", updated_at=" + updated_at +
                '}';
    }
}
