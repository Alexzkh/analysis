package com.zqykj.tldw.aggregate.searching.esclientrhl.repository;


import org.elasticsearch.search.sort.SortOrder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *  Sort object encapsulation
 **/
public class Sort {
    /**
     * The order list.
     **/
    private List<Order> orders = null;


    public List<Order> listOrders(){
        return orders;
    }


    public Sort(Sort.Order... ods) {
        orders = new ArrayList<>();
        for (Order od : ods) {
            orders.add(od);
        }
    }

    public Sort and(Sort sort) {
        if(orders == null){
            orders = new ArrayList<>();
        }
        sort.orders.forEach(order -> orders.add(order));
        return this;
    }

    public static class Order implements Serializable {
        private final SortOrder direction;
        private final String property;

        public Order(SortOrder direction,String property){
            this.direction = direction;
            this.property = property;
        }

        public SortOrder getDirection() {
            return direction;
        }

        public String getProperty() {
            return property;
        }
    }
}
