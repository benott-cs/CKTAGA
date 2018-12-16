package edu.uri.cs.ga.scoring;

/**
 * Created by Ben on 12/14/18.
 */
public interface ConsumerWithEnd<T> {
    void finish();
    void accept(T var1);
}

