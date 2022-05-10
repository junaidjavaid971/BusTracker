package com.haroonfazal.haroonapps.bustracker.Utils;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.databinding.DataBindingUtil;

import com.google.android.gms.maps.model.Marker;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.haroonfazal.haroonapps.bustracker.R;
import com.haroonfazal.haroonapps.bustracker.databinding.BottomSheetLayoutBinding;
import com.haroonfazal.haroonapps.bustracker.databinding.CarBottomSheetLayoutBinding;

public class CustomBottomSheetDialogFragment extends BottomSheetDialogFragment {
    BottomSheetLayoutBinding binding;
    Marker marker;

    public CustomBottomSheetDialogFragment(Marker marker) {
        this.marker = marker;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.bottom_sheet_layout, container, false);
        return binding.getRoot();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        View bottomSheet = (View) view.getParent();
        bottomSheet.setBackgroundTintMode(PorterDuff.Mode.CLEAR);
        bottomSheet.setBackgroundTintList(ColorStateList.valueOf(Color.TRANSPARENT));
        bottomSheet.setBackgroundColor(Color.TRANSPARENT);

        binding.tvArrvalTime.setText(marker.getTitle());
        binding.tvDistance.setText(marker.getSnippet() + " km");
    }

    public void updateDetails(Marker marker) {
        binding.tvArrvalTime.setText(marker.getTitle());
        binding.tvDistance.setText(marker.getSnippet() + " km");
    }
}
