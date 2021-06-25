package com.example.footballplatform.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.renderscript.Sampler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.footballplatform.MainActivity;
import com.example.footballplatform.R;
import com.example.footballplatform.databinding.FragmentMenuBinding;

public class MenuFragment extends Fragment {
    private FragmentMenuBinding binding;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentMenuBinding.inflate(getLayoutInflater());

        SharedPreferences sp = getActivity().getSharedPreferences("score", 0);
        binding.scoreText.setText("Лучший счёт: " + String.valueOf(sp.getInt("score", 0)));

        binding.startGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(binding.getRoot()).navigate(R.id.action_menuFragment_to_gameFragment);
            }
        });

        binding.leaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
                System.exit(0);
            }
        });

        return binding.getRoot();
    }
}