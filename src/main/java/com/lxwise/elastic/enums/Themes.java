package com.lxwise.elastic.enums;

import atlantafx.base.theme.*;

/**
 * @author lstar
 * @create 2025-02
 * @description: 主题
 */
public enum Themes {
    /**
     * PrimerLight
     */
    primer_light(new PrimerLight()),
    /**
     * PrimerDark
     */
    primer_dark(new PrimerDark()),
    /**
     * NordLight
     */
    nord_light(new NordLight()),
    /**
     * NordDark
     */
    nord_dark(new NordDark()),
    /**
     * CupertinoLight
     */
    cupertino_light(new CupertinoLight()),
    /**
     * CupertinoDark
     */
    cupertino_dark(new CupertinoDark()),
    /**
     * Dracula
     */
    dracula(new Dracula());

    private final Theme theme;

    Themes(Theme theme) {
        this.theme = theme;
    }

    @Override
    public String toString() {
        return theme.getName();
    }

    public Theme theme() {
        return this.theme;
    }

    public boolean isLight() {
    	switch (this){
            case cupertino_light:
            case primer_light:
            case nord_light:
                return true;
        }
        return false;
    }
}
