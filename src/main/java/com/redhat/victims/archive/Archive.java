package com.redhat.victims.archive;


public interface Archive {
    public void accept(ArchiveVisitor visitor);
}

