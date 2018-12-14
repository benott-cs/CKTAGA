package edu.uri.cs.ga.scoring;

import java.util.function.Consumer;

/**
 * Created by Ben on 12/14/18.
 */
public interface ConsumerWithEnd<T> extends Consumer<T> {
    void finish();
}
