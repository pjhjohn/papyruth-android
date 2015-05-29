package com.montserrat.utils.support.fab;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.montserrat.app.AppManager;
import com.montserrat.utils.view.FloatingActionControlContainer;

/**
 * Created by pjhjohn on 2015-05-29.
 */
public class FloatingActionControl {
    public enum Type { FAB, FAM, NONE }
    private static FloatingActionControl instance = null;
    private FloatingActionControlContainer container;
    private FloatingActionButton fab = null;
    private FloatingActionMenu fam = null;
    private LayoutInflater inflater;

    private FloatingActionControl() {
        this.inflater = LayoutInflater.from(AppManager.getInstance().getContext());
    }
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
    public static FloatingActionButton getButton() {
        FloatingActionControl fac = FloatingActionControl.getInstance();
        return fac.fab;
    }
    public static FloatingActionButton getButton(int fab_id) {
        FloatingActionControl fac = FloatingActionControl.getInstance();
        if(fac.fam == null) return null;
        else return (FloatingActionButton) fac.fam.findViewById(fab_id);
    }
    public static FloatingActionMenu getMenu(){
        FloatingActionControl fac = FloatingActionControl.getInstance();
        return fac.fam;
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

    public Type getControlType() {
        return this.fab == null ? (this.fam == null ? Type.NONE : Type.FAB) : Type.FAM;
    }

    public void setContainerView(FloatingActionControlContainer container) {
        this.container = container;
    }

    public FloatingActionButton setFloatingActionButton(FloatingActionButton fab) {
        this.removeAll();
        this.register(fab);

        this.fab.hide(false);
        this.fab.show(true);
        return this.fab;
    }
    public FloatingActionButton setFloatingActionButton(int resourceId) {
        return this.setFloatingActionButton((FloatingActionButton) this.inflater.inflate(resourceId, null));
    }

    public FloatingActionMenu setFloatingActionMenu(FloatingActionMenu fam) {
        this.removeAll();
        this.register(fam);

        this.fam.hideMenuButton(false);
        this.fam.showMenuButton(true);
        return this.fam;
    }
    public FloatingActionMenu setFloatingActionMenu(int resourceId) {
        return this.setFloatingActionMenu((FloatingActionMenu) this.inflater.inflate(resourceId, null));
    }

    private void removeAll() {
        if(this.fab != null) this.fab.hide(true);
        if(this.fam != null) this.fam.hideMenuButton(true);
        this.container.removeAllViews();
    }

    private void register(FloatingActionButton fab) {
        if(this.container==null) return;
        if(fab==null) return;
        this.fab = fab;
        this.container.addView(fab);
    }
    private void register(FloatingActionMenu fam) {
        if(this.container==null) return;
        if(fam==null) return;
        this.fam = fam;
        this.container.addView(fam);
    }
}
