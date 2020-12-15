package org.kitteh.craftirc.messaging.processing;

public interface Preprocessor {
    public void preProcess(final PreprocessedMessage msg);
}
