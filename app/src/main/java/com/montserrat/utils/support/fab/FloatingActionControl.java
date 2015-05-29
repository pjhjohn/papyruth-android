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
        return ViewObservable.clicks(FloatingActionControl.getButton());
    }
    public static rx.Observable<OnClickEvent> clicks(int fab_id) {
        return ViewObservable.clicks(FloatingActionControl.getButton(fab_id));
    }

    public FloatingActionControl toggle(boolean animate) {
        if(this.fab != null) this.fab.toggle(animate);
        else if(this.fam != null) this.fam.toggleMenuButton(animate);
        return this;
    }
    public FloatingActionControl show(boolean animate) {
        if(this.fab != null) this.fab.show(animate);
        else if(this.fam != null) this.fam.showMenuButton(animate);
        return this;
    }
    public FloatingActionControl hide(boolean animate) {
        if(this.fab != null) this.fab.hide(animate);
        else if(this.fam != null) this.fam.hideMenuButton(animate);
        return this;
    }

    public Type getControlType() {
        return this.fab == null ? (this.fam == null ? Type.NONE : Type.FAB) : Type.FAM;
    }

    public FloatingActionControl setContainerView(FloatingActionControlContainer container) {
        this.container = container;
        return this;
    }

    public FloatingActionControl setButton(int id) {
        this.clear();
        this.fab = (FloatingActionButton)((ViewGroup)this.inflater.inflate(id, this.container, true)).getChildAt(0);
        return this;
    }

    public FloatingActionControl setMenu(int id) {
        this.clear();
        this.fam = (FloatingActionMenu)((ViewGroup)this.inflater.inflate(id, this.container, true)).getChildAt(0);
        return this;
    }

    public FloatingActionControl clear() {
        if(this.fab != null) this.fab.hide(true);
        if(this.fam != null) this.fam.hideMenuButton(true);
        this.container.removeAllViews();
        this.fab = null;
        this.fam = null;
        return this;
    }
}
