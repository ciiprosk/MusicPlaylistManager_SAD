package it.diem.unisa.musicmanager.dao;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DAO<T> {

    List<T> selectAll();

    void insert(T t);

    void update(T t);

    void delete(UUID id);

    Optional<T> searchById(UUID id);

    boolean isDuplicated(T t);

}
