package com.papyruth.support.opensource.fab;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.papyruth.android.AppManager;
import com.papyruth.support.utility.customview.FloatingActionControlContainer;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.android.view.OnClickEvent;
import rx.android.view.ViewObservable;
import rx.schedulers.Schedulers;
import timber.log.Timber;

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
    public FloatingActionControl toggle(boolean animate, int delay, TimeUnit unit) {
        if (delay <= 0) return this.toggle(animate);
        Observable.just(null).delay(delay, unit).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(unused -> this.toggle(animate));
        return this;
    }
    public FloatingActionControl show(boolean animate) {
        if(this.fab != null) this.fab.show(animate);
        else if(this.fam != null) this.fam.showMenuButton(animate);
        return this;
    }
    public FloatingActionControl show(boolean animate, int delay, TimeUnit unit) {
        if (delay <= 0) return this.show(animate);
        Observable.just(null).delay(delay, unit).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(unused -> this.show(animate));
        return this;
    }
    public FloatingActionControl hide(boolean animate) {
        if(this.fab != null) this.fab.hide(animate);
        else if(this.fam != null) this.fam.hideMenuButton(animate);
        return this;
    }
    public FloatingActionControl hide(boolean animate, int delay, TimeUnit unit) {
        if (delay <= 0) return this.hide(animate);
        Observable.just(null).delay(delay, unit).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(unused -> this.hide(animate));
        return this;
    }
    public FloatingActionControl closeMenuButton(boolean animate) {
        if(fam != null)
            fam.close(animate);
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

        if(this.fab != null) this.fab.hide(animate);
        if(this.fam != null) this.fam.hideMenuButton(animate);
        if(previous_id == control.getId()) return this;

        if(hasControl) {
            if(animate) new Handler().postDelayed(() -> this.container.removeView(this.container.findViewById(previous_id)), 300);
            else this.container.removeView(this.container.findViewById(previous_id));
        }
        this.fab = control instanceof FloatingActionButton? (FloatingActionButton) control : null;
        this.fam = control instanceof FloatingActionMenu? (FloatingActionMenu) control : null;
        if(this.fam != null) {
            this.fam.setClosedOnTouchOutside(true);
            for(int i = 0; i < this.fam.getChildCount(); i++) {
                if (!(this.fam.getChildAt(i) instanceof FloatingActionButton)) continue;
                final FloatingActionButton fab_mini = (FloatingActionButton) this.fam.getChildAt(i);
                fab_mini.setColorNormal(this.fam.getMenuButtonColorNormal());
                fab_mini.setColorPressed(this.fam.getMenuButtonColorPressed());
                fab_mini.setColorRipple(this.fam.getMenuButtonColorRipple());
            }
        }
        this.container.addView(control);
        if(control instanceof FloatingActionButton) ((FloatingActionButton) control).hide(false);
        else if(control instanceof FloatingActionMenu) ((FloatingActionMenu) control).hideMenuButton(false);
        return this;
    }

    public boolean isFAB(){
        return this.fab != null;
    }
    public boolean isFAM(){
        return this.fam != null;
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
