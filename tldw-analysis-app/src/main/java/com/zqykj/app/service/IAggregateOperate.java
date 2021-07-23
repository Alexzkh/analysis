package com.zqykj.app.service;

public interface IAggregateOperate {

   <T> long count(Class<T> clazz);
}
