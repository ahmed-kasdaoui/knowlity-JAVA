package Interfaces;

import Entities.Creator;
import java.util.List;

public interface ICreator {
    void add(Creator creator);
    void update(Creator creator);
    void delete(int id);
    Creator getById(int id);
    List<Creator> getAll();
}
