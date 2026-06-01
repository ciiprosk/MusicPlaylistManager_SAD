package it.diem.unisa.musicmanager.dao;

import java.util.List;

public interface DAO<T> {

    List<T> selectAll();

    void insert(T t);

    void update(T t);

    void delete(T t);

}
