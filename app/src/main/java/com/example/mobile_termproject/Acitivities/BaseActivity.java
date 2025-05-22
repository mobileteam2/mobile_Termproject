package com.example.mobile_termproject.Acitivities;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.mobile_termproject.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class BaseActivity extends AppCompatActivity {
    protected Toolbar toolbar;
    protected BottomNavigationView bottomNavigationView;
    public static final String TAGdebug = "DEBUG";

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

        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int menuId = item.getItemId();
                if(menuId == R.id.bottomBar_myRefri){
                    // TODO: 내 냉장고 눌렀을 때 처리
                    Log.d(TAGdebug, "냉장고 클릭");
                } else if(menuId == R.id.bottomBar_community){
                    // TODO: 커뮤니티 눌렀을 때 처리
                    Log.d(TAGdebug, "커뮤니티 클릭");
                } else if(menuId == R.id.bottomBar_recipe){
                    // TODO: 레시피 추천 눌렀을 때 처리
                    Log.d(TAGdebug, "레시피 클릭");
                } else if(menuId == R.id.bottomBar_others){
                    // TODO: 기타 눌렀을 때 처리
                    Log.d(TAGdebug, "기타 클릭");
                } else {
                    Log.d(TAGdebug, "그 외 경우");
                }
                return false;
            }
        });
    }
}
