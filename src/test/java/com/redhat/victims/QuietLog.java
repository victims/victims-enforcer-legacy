package com.redhat.victims;

/**
 * Created by gm on 9/05/14.
 */
public class QuietLog implements org.apache.maven.plugin.logging.Log {

    public boolean isDebugEnabled() {
        return false;
    }

    public void debug(CharSequence charSequence) {

    }

    public void debug(CharSequence charSequence, Throwable throwable) {

    }

    public void debug(Throwable throwable) {

    }

    public boolean isInfoEnabled() {
        return false;
    }

    public void info(CharSequence charSequence) {

    }

    public void info(CharSequence charSequence, Throwable throwable) {

    }

    public void info(Throwable throwable) {

    }

    public boolean isWarnEnabled() {
        return false;
    }

    public void warn(CharSequence charSequence) {

    }

    public void warn(CharSequence charSequence, Throwable throwable) {

    }

    public void warn(Throwable throwable) {

    }

    public boolean isErrorEnabled() {
        return false;
    }

    public void error(CharSequence charSequence) {

    }

    public void error(CharSequence charSequence, Throwable throwable) {

    }

    public void error(Throwable throwable) {

    }
}
