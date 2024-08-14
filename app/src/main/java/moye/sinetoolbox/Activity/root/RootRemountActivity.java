package moye.sinetoolbox.Activity.root;

import android.content.Intent;
import android.os.Bundle;

import moye.sinetoolbox.Activity.BaseActivity;
import moye.sinetoolbox.R;
import moye.sinetoolbox.dialog.RootRemountTipActivity;

public class RootRemountActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remount);

        findViewById(R.id.activity_root_remount_system).setOnClickListener(view -> {
            Intent intent = new Intent(RootRemountActivity.this, RootRemountTipActivity.class);
            intent.putExtra("dd", 1);
            startActivity(intent);
        });
    }
}