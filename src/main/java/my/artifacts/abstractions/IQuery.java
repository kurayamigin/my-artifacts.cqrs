package my.artifacts.abstractions;


public interface IQuery<TKey> {
    TKey getId();
    void setId(TKey id);
}
