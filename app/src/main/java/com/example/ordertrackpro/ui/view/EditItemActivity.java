package com.example.ordertrackpro.ui.view;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.ordertrackpro.R;
import com.example.ordertrackpro.ui.controller.IEditItem;
import com.example.ordertrackpro.utils.EditItemModel;

public class EditItemActivity extends AppCompatActivity implements IEditItem {
    private String name = "";
    private double price = 0.0;
    private int qty = 0;
    private String image = "";
    private String classification = "";
    private Uri finalUri;
    private String newName = "";
    private int newQty = 0;
    private double newPrice = 0.0;
    private EditText foodNameET;
    private EditText qtyET;
    private EditText priceET;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_item);
        EditItemModel model = new EditItemModel();
        progressBar = findViewById(R.id.progressBar);
        Intent intent = getIntent();

        if (intent.hasExtra("name")) {
            name = intent.getStringExtra("name");
            price = intent.getDoubleExtra("price", 0.0);
            qty = intent.getIntExtra("qty", 0);
            image = intent.getStringExtra("image");
            classification = intent.getStringExtra("classification");
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ImageView foodImage = findViewById(R.id.foodImage);
        foodNameET = findViewById(R.id.foodName);
        qtyET = findViewById(R.id.qty);
        priceET = findViewById(R.id.price);
        Button updateItem = findViewById(R.id.updateItem);

        priceET.setText(String.valueOf(price));
        qtyET.setText(String.valueOf(qty));
        foodNameET.setText(name);
        Glide.with(getApplicationContext()).load(image).into(foodImage);
        ActivityResultLauncher<PickVisualMediaRequest> pickMedia = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
            if (uri != null) {
                Glide.with(EditItemActivity.this).load(uri).into(foodImage);
                finalUri = uri;
                int flag = Intent.FLAG_GRANT_READ_URI_PERMISSION;
                EditItemActivity.this.getContentResolver().takePersistableUriPermission(finalUri, flag);
            }
            else {
                Toast.makeText(this, "No image is selected.", Toast.LENGTH_SHORT).show();
            }
        });
        foodImage.setOnClickListener(view -> pickMedia.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build()));

        updateItem.setOnClickListener(v -> {
            getValues();
            if (hasChanged()) {
                progressBar.setVisibility(View.VISIBLE);
                EditItemModel editItemModel = new EditItemModel(image, newName, newPrice, newQty);
                model.updateItem(editItemModel, EditItemActivity.this, classification, name, finalUri);
                return;
            }
            Toast.makeText(this, "Please update at least one field to continue!", Toast.LENGTH_SHORT).show();
            
        });


    }

    private void getValues() {
        newName = foodNameET.getText().toString();
        newPrice = Double.parseDouble(priceET.getText().toString());
        newQty = Integer.parseInt(qtyET.getText().toString());
    }
    private boolean hasChanged() {
        if (finalUri == null && newName.equals(name) && newQty == qty && newPrice == price) {
            return false;
        }
        return true;
    }

    @Override
    public void onUpdateItem(boolean verdict, String message) {
        progressBar.setVisibility(View.GONE);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}