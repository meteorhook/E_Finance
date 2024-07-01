// Generated by view binder compiler. Do not edit!
package com.example.e_finance.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Guideline;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.example.e_finance.R;
import com.loper7.date_time_picker.DateTimePicker;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class StatisticsDateBottomsheetdialogBinding implements ViewBinding {
  @NonNull
  private final ConstraintLayout rootView;

  @NonNull
  public final TextView backnow;

  @NonNull
  public final DateTimePicker dateTimePicker;

  @NonNull
  public final TextView dateinfo;

  @NonNull
  public final TextView endDate;

  @NonNull
  public final TextView finish;

  @NonNull
  public final Guideline guideline;

  @NonNull
  public final ConstraintLayout layout;

  @NonNull
  public final TextView startDate;

  @NonNull
  public final TextView title;

  @NonNull
  public final TextView tv;

  private StatisticsDateBottomsheetdialogBinding(@NonNull ConstraintLayout rootView,
      @NonNull TextView backnow, @NonNull DateTimePicker dateTimePicker, @NonNull TextView dateinfo,
      @NonNull TextView endDate, @NonNull TextView finish, @NonNull Guideline guideline,
      @NonNull ConstraintLayout layout, @NonNull TextView startDate, @NonNull TextView title,
      @NonNull TextView tv) {
    this.rootView = rootView;
    this.backnow = backnow;
    this.dateTimePicker = dateTimePicker;
    this.dateinfo = dateinfo;
    this.endDate = endDate;
    this.finish = finish;
    this.guideline = guideline;
    this.layout = layout;
    this.startDate = startDate;
    this.title = title;
    this.tv = tv;
  }

  @Override
  @NonNull
  public ConstraintLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static StatisticsDateBottomsheetdialogBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static StatisticsDateBottomsheetdialogBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.statistics_date_bottomsheetdialog, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static StatisticsDateBottomsheetdialogBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.backnow;
      TextView backnow = ViewBindings.findChildViewById(rootView, id);
      if (backnow == null) {
        break missingId;
      }

      id = R.id.dateTimePicker;
      DateTimePicker dateTimePicker = ViewBindings.findChildViewById(rootView, id);
      if (dateTimePicker == null) {
        break missingId;
      }

      id = R.id.dateinfo;
      TextView dateinfo = ViewBindings.findChildViewById(rootView, id);
      if (dateinfo == null) {
        break missingId;
      }

      id = R.id.endDate;
      TextView endDate = ViewBindings.findChildViewById(rootView, id);
      if (endDate == null) {
        break missingId;
      }

      id = R.id.finish;
      TextView finish = ViewBindings.findChildViewById(rootView, id);
      if (finish == null) {
        break missingId;
      }

      id = R.id.guideline;
      Guideline guideline = ViewBindings.findChildViewById(rootView, id);
      if (guideline == null) {
        break missingId;
      }

      id = R.id.layout;
      ConstraintLayout layout = ViewBindings.findChildViewById(rootView, id);
      if (layout == null) {
        break missingId;
      }

      id = R.id.startDate;
      TextView startDate = ViewBindings.findChildViewById(rootView, id);
      if (startDate == null) {
        break missingId;
      }

      id = R.id.title;
      TextView title = ViewBindings.findChildViewById(rootView, id);
      if (title == null) {
        break missingId;
      }

      id = R.id.tv;
      TextView tv = ViewBindings.findChildViewById(rootView, id);
      if (tv == null) {
        break missingId;
      }

      return new StatisticsDateBottomsheetdialogBinding((ConstraintLayout) rootView, backnow,
          dateTimePicker, dateinfo, endDate, finish, guideline, layout, startDate, title, tv);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
