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
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class DialogMyalertdialogBinding implements ViewBinding {
  @NonNull
  private final ConstraintLayout rootView;

  @NonNull
  public final TextView Negative;

  @NonNull
  public final TextView Positive;

  @NonNull
  public final View line;

  @NonNull
  public final TextView message;

  @NonNull
  public final TextView title;

  private DialogMyalertdialogBinding(@NonNull ConstraintLayout rootView, @NonNull TextView Negative,
      @NonNull TextView Positive, @NonNull View line, @NonNull TextView message,
      @NonNull TextView title) {
    this.rootView = rootView;
    this.Negative = Negative;
    this.Positive = Positive;
    this.line = line;
    this.message = message;
    this.title = title;
  }

  @Override
  @NonNull
  public ConstraintLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static DialogMyalertdialogBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static DialogMyalertdialogBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.dialog_myalertdialog, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static DialogMyalertdialogBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.Negative;
      TextView Negative = ViewBindings.findChildViewById(rootView, id);
      if (Negative == null) {
        break missingId;
      }

      id = R.id.Positive;
      TextView Positive = ViewBindings.findChildViewById(rootView, id);
      if (Positive == null) {
        break missingId;
      }

      id = R.id.line;
      View line = ViewBindings.findChildViewById(rootView, id);
      if (line == null) {
        break missingId;
      }

      id = R.id.message;
      TextView message = ViewBindings.findChildViewById(rootView, id);
      if (message == null) {
        break missingId;
      }

      id = R.id.title;
      TextView title = ViewBindings.findChildViewById(rootView, id);
      if (title == null) {
        break missingId;
      }

      return new DialogMyalertdialogBinding((ConstraintLayout) rootView, Negative, Positive, line,
          message, title);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
