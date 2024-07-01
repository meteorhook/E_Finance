// Generated by view binder compiler. Do not edit!
package com.example.e_finance.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.example.e_finance.R;
import com.example.e_finance.RingProgressBar;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class BudgetLayoutBinding implements ViewBinding {
  @NonNull
  private final ConstraintLayout rootView;

  @NonNull
  public final TextView Count;

  @NonNull
  public final TextView budget;

  @NonNull
  public final TextView date;

  @NonNull
  public final TextView remaining;

  @NonNull
  public final RingProgressBar ringProgressBar;

  @NonNull
  public final TextView textView;

  @NonNull
  public final TextView textView2;

  @NonNull
  public final TextView total;

  @NonNull
  public final TextView unused;

  private BudgetLayoutBinding(@NonNull ConstraintLayout rootView, @NonNull TextView Count,
      @NonNull TextView budget, @NonNull TextView date, @NonNull TextView remaining,
      @NonNull RingProgressBar ringProgressBar, @NonNull TextView textView,
      @NonNull TextView textView2, @NonNull TextView total, @NonNull TextView unused) {
    this.rootView = rootView;
    this.Count = Count;
    this.budget = budget;
    this.date = date;
    this.remaining = remaining;
    this.ringProgressBar = ringProgressBar;
    this.textView = textView;
    this.textView2 = textView2;
    this.total = total;
    this.unused = unused;
  }

  @Override
  @NonNull
  public ConstraintLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static BudgetLayoutBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static BudgetLayoutBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.budget_layout, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static BudgetLayoutBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.Count;
      TextView Count = ViewBindings.findChildViewById(rootView, id);
      if (Count == null) {
        break missingId;
      }

      id = R.id.budget;
      TextView budget = ViewBindings.findChildViewById(rootView, id);
      if (budget == null) {
        break missingId;
      }

      id = R.id.date;
      TextView date = ViewBindings.findChildViewById(rootView, id);
      if (date == null) {
        break missingId;
      }

      id = R.id.remaining;
      TextView remaining = ViewBindings.findChildViewById(rootView, id);
      if (remaining == null) {
        break missingId;
      }

      id = R.id.ringProgressBar;
      RingProgressBar ringProgressBar = ViewBindings.findChildViewById(rootView, id);
      if (ringProgressBar == null) {
        break missingId;
      }

      id = R.id.textView;
      TextView textView = ViewBindings.findChildViewById(rootView, id);
      if (textView == null) {
        break missingId;
      }

      id = R.id.textView2;
      TextView textView2 = ViewBindings.findChildViewById(rootView, id);
      if (textView2 == null) {
        break missingId;
      }

      id = R.id.total;
      TextView total = ViewBindings.findChildViewById(rootView, id);
      if (total == null) {
        break missingId;
      }

      id = R.id.unused;
      TextView unused = ViewBindings.findChildViewById(rootView, id);
      if (unused == null) {
        break missingId;
      }

      return new BudgetLayoutBinding((ConstraintLayout) rootView, Count, budget, date, remaining,
          ringProgressBar, textView, textView2, total, unused);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
