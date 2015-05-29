package com.montserrat.utils.support.fab;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.montserrat.app.AppManager;
import com.montserrat.utils.view.FloatingActionControlContainer;

import rx.android.view.OnClickEvent;
import rx.android.view.ViewObservable;

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
    public static rx.Observable<OnClickEvent> clicks() {
        return ViewObservable.clicks(getButton());
    }
    public static rx.Observable<OnClickEvent> clicks(int fab_id) {
        return ViewObservable.clicks(getButton(fab_id));
    }

    public static void toggle(boolean animate) {
        FloatingActionControl fac = FloatingActionControl.getInstance();
        if(fac.fab != null) fac.fab.toggle(animate);
        else if(fac.fam != null) fac.fam.toggleMenuButton(animate);
    }
    public static void show(boolean animate) {
        FloatingActionControl fac = FloatingActionControl.getInstance();
        if(fac.fab != null) fac.fab.show(animate);
        else if(fac.fam != null) fac.fam.showMenuButton(animate);
    }
    public static void hide(boolean animate) {
        FloatingActionControl fac = FloatingActionControl.getInstance();
        if(fac.fab != null) fac.fab.hide(animate);
        else if(fac.fam != null) fac.fam.hideMenuButton(animate);
    }

    public Type getControlType() {
        return this.fab == null ? (this.fam == null ? Type.NONE : Type.FAB) : Type.FAM;
    }

    public void setContainerView(FloatingActionControlContainer container) {
        this.container = container;
    }

    public FloatingActionButton setButton(int id) {
        this.removeAll();
        return this.fab = (FloatingActionButton)((ViewGroup)this.inflater.inflate(id, this.container, true)).getChildAt(0);
    }

    public FloatingActionMenu setMenu(int id) {
        this.removeAll();
        return this.fam = (FloatingActionMenu)((ViewGroup)this.inflater.inflate(id, this.container, true)).getChildAt(0);
    }

    private void removeAll() {
        if(this.fab != null) this.fab.hide(true);
        if(this.fam != null) this.fam.hideMenuButton(true);
        this.container.removeAllViews();
    }
}
