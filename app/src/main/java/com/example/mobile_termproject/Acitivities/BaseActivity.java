package com.example.mobile_termproject.Acitivities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.mobile_termproject.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class BaseActivity extends AppCompatActivity {
    protected Toolbar toolbar;
    protected BottomNavigationView bottomNavigationView;
    public static final String TAGdebug = "DEBUG";

    /*
        DB 통합 관리
     */
    @SuppressLint("StaticFieldLeak")
    public static FirebaseFirestore db = FirebaseFirestore.getInstance();

    public static FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    public static class Categories {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_menu_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if ( id == R.id.topBar_userMenu) {
            Log.d(TAGdebug, "상단 바 사용자 메뉴 클릭");
        }
        return super.onOptionsItemSelected(item);
    }

    protected void setTopAndBottomBar(){
        toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);

        bottomNavigationView = findViewById(R.id.bottomAppBar);
        Log.d(TAGdebug, "bottomNavigationView: " + bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Log.d(TAGdebug, "BottomNavigationView item selected: " + item.getItemId());
                int menuId = item.getItemId();
                if(menuId == R.id.bottomBar_myRefri){
                    if(!(BaseActivity.this instanceof MainActivity)) {
                        // 현재 액티비티가 MainActivity가 아닌 경우에만 클릭 시 이동.
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish(); // 현재 Activity 종료해서 스택 정리
                        Log.d(TAGdebug, "냉장고 클릭");
                    }
                    return true;
                } else if (menuId == R.id.bottomBar_price_check) {
                    if (!(BaseActivity.this instanceof PriceCheckActivity)) {
                        Intent intent = new Intent(getApplicationContext(), PriceCheckActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                        Log.d(TAGdebug, "가격확인 클릭");
                    }
                    return true;
                } else if(menuId == R.id.bottomBar_recipe){
                    // TODO: 레시피 추천 눌렀을 때 처리
                    Log.d(TAGdebug, "레시피 클릭");
                    return true;
                } else if(menuId == R.id.bottomBar_others){
                    // TODO: 기타 눌렀을 때 처리
                    Log.d(TAGdebug, "기타 클릭");
                    return true;
                } else {
                    Log.d(TAGdebug, "그 외 경우");
                    return false;
                }
            }
        });
    }
}
