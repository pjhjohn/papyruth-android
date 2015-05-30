package com.montserrat.utils.support.fab;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;

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

    public FloatingActionControl setContainer(FloatingActionControlContainer container) {
        this.container = container;
        return this;
    }

    public FloatingActionControl setControl(int layout_id) {
        return this.setControl(layout_id, true);
    }
    public FloatingActionControl setControl(int layout_id, boolean animate) {
        final int previous_id = this.container.getChildCount() > 0 ? this.container.getChildAt(0).getId() : -1;
        final boolean hasControl = this.fab!=null || this.fam!=null;
        View control = this.inflater.inflate(layout_id, this.container, false);
        if(previous_id == control.getId()) {
            if(this.fab != null) this.fab.hide(animate);
            if(this.fam != null) this.fam.hideMenuButton(animate);
            return this;
        }
        if(this.fab != null) this.fab.hide(animate);
        if(this.fam != null) this.fam.hideMenuButton(animate);
        if(hasControl) {
            if(animate) new Handler().postDelayed(() -> this.container.removeView(this.container.findViewById(previous_id)), 300);
            else this.container.removeView(this.container.findViewById(previous_id));
        }
        this.fab = control instanceof FloatingActionButton? (FloatingActionButton) control : null;
        this.fam = control instanceof FloatingActionMenu? (FloatingActionMenu) control : null;
        this.container.addView(control);
        if(control instanceof FloatingActionButton) ((FloatingActionButton) control).hide(false);
        else if(control instanceof FloatingActionMenu) ((FloatingActionMenu) control).hideMenuButton(false);
        return this;
    }

    public FloatingActionControl clear() {
        return clear(true);
    }
    public FloatingActionControl clear(boolean animate) {
        final boolean hasControl = this.fab!=null || this.fam!=null;
        if(this.fab != null) this.fab.hide(animate);
        if(this.fam != null) this.fam.hideMenuButton(animate);
        if(hasControl) {
            if(animate) new Handler().postDelayed(this.container::removeAllViews, 300);
            else this.container.removeAllViews();
        }
        this.fab = null;
        this.fam = null;
        return this;
    }
}
