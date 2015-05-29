package com.montserrat.utils.support.fab;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

/**
 * Created by pjhjohn on 2015-05-29.
 */
public class FloatingActionControl {
    public enum Type { FAB, FAM, NONE }
    private static FloatingActionControl instance = null;
    private FloatingActionButton fab = null;
    private FloatingActionMenu fam = null;

    private FloatingActionControl() {}
    public static synchronized FloatingActionControl getInstance() {
        if(instance == null) FloatingActionControl.instance = new FloatingActionControl();
        return FloatingActionControl.instance;
    }
    public static synchronized FloatingActionControl getInstance(FloatingActionButton fab) {
        if(instance == null) FloatingActionControl.instance = new FloatingActionControl();
        instance.setFloatingActionButton(fab);
        return FloatingActionControl.instance;
    }
    public static synchronized FloatingActionControl getInstance(FloatingActionMenu fam) {
        if(instance == null) FloatingActionControl.instance = new FloatingActionControl();
        instance.setFloatingActionMenu(fam);
        return FloatingActionControl.instance;
    }

    public static void toggle(boolean animate) {
        instance = FloatingActionControl.getInstance();
        switch(instance.getControlType()) {
            case FAM : instance.fam.toggleMenuButton(animate); break;
            case FAB : instance.fab.toggle(animate);
            case NONE : break;
        }
    }
    public static void show(boolean animate) {
        instance = FloatingActionControl.getInstance();
        switch(instance.getControlType()) {
            case FAM : instance.fam.showMenuButton(animate); break;
            case FAB : instance.fab.show(animate);
            case NONE : break;
        }
    }
    public static void hide(boolean animate) {
        instance = FloatingActionControl.getInstance();
        switch(instance.getControlType()) {
            case FAM : instance.fam.showMenuButton(animate); break;
            case FAB : instance.fab.show(animate);
            case NONE : break;
        }
    }

    public void setFloatingActionButton(FloatingActionButton fab) {
        if(this.fab != null) this.fab.hide(true);
        if(this.fam != null) this.fam.hideMenuButton(true);

        this.fab = fab;
        this.fam = null;

        this.fab.hide(false);
        this.fab.show(true);
    }

    public void setFloatingActionMenu(FloatingActionMenu fam) {
        if(this.fab != null) this.fab.hide(true);
        if(this.fam != null) this.fam.hideMenuButton(true);

        this.fab = null;
        this.fam = fam;

        this.fam.hideMenuButton(false);
        this.fam.showMenuButton(true);
    }

    public Type getControlType() {
        return this.fab == null ? (this.fam == null ? Type.NONE : Type.FAB) : Type.FAM;
    }

    public FloatingActionButton getButton() {
        return this.fab;
    }

    public FloatingActionMenu getMenu() {
        return this.fam;
    }
}
