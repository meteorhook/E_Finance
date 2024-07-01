// Generated by view binder compiler. Do not edit!
package com.example.e_finance.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
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

public final class ActivityEfinanceIntroBinding implements ViewBinding {
  @NonNull
  private final ConstraintLayout rootView;

  @NonNull
  public final TextView Agreemnet;

  @NonNull
  public final ImageView Logo;

  @NonNull
  public final TextView Version;

  @NonNull
  public final Button checknew;

  @NonNull
  public final ImageView pgbar;

  private ActivityEfinanceIntroBinding(@NonNull ConstraintLayout rootView,
      @NonNull TextView Agreemnet, @NonNull ImageView Logo, @NonNull TextView Version,
      @NonNull Button checknew, @NonNull ImageView pgbar) {
    this.rootView = rootView;
    this.Agreemnet = Agreemnet;
    this.Logo = Logo;
    this.Version = Version;
    this.checknew = checknew;
    this.pgbar = pgbar;
  }

  @Override
  @NonNull
  public ConstraintLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static ActivityEfinanceIntroBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static ActivityEfinanceIntroBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.activity_efinance_intro, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static ActivityEfinanceIntroBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.Agreemnet;
      TextView Agreemnet = ViewBindings.findChildViewById(rootView, id);
      if (Agreemnet == null) {
        break missingId;
      }

      id = R.id.Logo;
      ImageView Logo = ViewBindings.findChildViewById(rootView, id);
      if (Logo == null) {
        break missingId;
      }

      id = R.id.Version;
      TextView Version = ViewBindings.findChildViewById(rootView, id);
      if (Version == null) {
        break missingId;
      }

      id = R.id.checknew;
      Button checknew = ViewBindings.findChildViewById(rootView, id);
      if (checknew == null) {
        break missingId;
      }

      id = R.id.pgbar;
      ImageView pgbar = ViewBindings.findChildViewById(rootView, id);
      if (pgbar == null) {
        break missingId;
      }

      return new ActivityEfinanceIntroBinding((ConstraintLayout) rootView, Agreemnet, Logo, Version,
          checknew, pgbar);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
