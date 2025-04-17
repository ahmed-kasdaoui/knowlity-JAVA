package Interfaces;
import java.util.List;
public interface InterfaceBlog <T>{
    public void add(T t);
    public void remove(T t);
    public void update(T t);
    public List<T> print();
}
