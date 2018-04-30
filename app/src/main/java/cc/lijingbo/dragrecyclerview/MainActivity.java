package cc.lijingbo.dragrecyclerview;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.support.v7.widget.helper.ItemTouchHelper.Callback;
import android.util.Log;
import cc.lijingbo.dragrecyclerview.adapter.DragAdapter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * https://www.cnblogs.com/wjtaigwh/p/6543354.html
 * https://blog.csdn.net/ap____lLix1/article/details/65434975
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    RecyclerView mRecyclerView;
    GridLayoutManager mLayoutManager;
    private List<DragBean> mList;
    private DragAdapter dragAdapter;
    private ItemTouchHelper itemTouchHelper;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preferences = getSharedPreferences("drag", MODE_PRIVATE);
        String list = preferences.getString("list", null);

        if (null == list) {
            mList = new ArrayList<>();
            DragBean bean;
            for (int i = 0; i < 20; i++) {
                bean = new DragBean();
                bean.setName("He" + (i + 1));
                mList.add(bean);
            }
        } else {
            Gson gson = new Gson();
            Type type = new TypeToken<List<DragBean>>() {
            }.getType();
            mList = gson.fromJson(list, type);
        }

        mRecyclerView = findViewById(R.id.recyclerview);
        mLayoutManager = new GridLayoutManager(MainActivity.this, 4);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(mLayoutManager);
        dragAdapter = new DragAdapter(MainActivity.this, mList);
        mRecyclerView.setAdapter(dragAdapter);
        itemTouchHelper = new ItemTouchHelper(new Callback() {
            /**
             * 设置是否滑动，拖拽方向，需要判断布局结构。GridLayoutManger 上下拖动， LineayLayoutManager 上下左右都可以拖动
             */
            @Override
            public int getMovementFlags(RecyclerView recyclerView, ViewHolder viewHolder) {
                Log.e(TAG, "getMovementFlags()");
                if (recyclerView.getLayoutManager() instanceof GridLayoutManager) {
                    int dragFlags =
                            ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
                    int swipeFlags = 0;
                    return makeMovementFlags(dragFlags, swipeFlags);
                } else if (recyclerView.getLayoutManager() instanceof LinearLayoutManager) {
                    int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
                    int swipeFlags = ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
                    return makeMovementFlags(dragFlags, swipeFlags);
                }
                return 0;
            }

            /**
             * 拖动的时候回调的方法，在这里需要将正在拖拽的 item 和集合的 Item 进行交换数据，然后通知 adapter 更新数据
             */
            @Override
            public boolean onMove(RecyclerView recyclerView, ViewHolder viewHolder, ViewHolder target) {
                Log.e(TAG, "onMove()");
                int fromPosition = viewHolder.getAdapterPosition();
                int targetPosition = target.getAdapterPosition();
                DragBean dragBean = mList.get(fromPosition);
                mList.remove(fromPosition);
                mList.add(targetPosition, dragBean);
                dragAdapter.notifyItemMoved(fromPosition, targetPosition);
                return true;
            }

            /**
             * 滑动调用的方法，
             */
            @Override
            public void onSwiped(ViewHolder viewHolder, int direction) {
                Log.e(TAG, "onSwiped()");
                int position = viewHolder.getAdapterPosition();
                mList.remove(position);
                dragAdapter.notifyItemRemoved(position);
            }

            /**
             * 长按的时候选中的 item, 给当前 item 设置一个高亮背景色
             */
            @Override
            public void onSelectedChanged(ViewHolder viewHolder, int actionState) {
                Log.e(TAG, "onSelectedChanged()");
                if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
                    viewHolder.itemView.setBackgroundColor(Color.LTGRAY);
                }
                super.onSelectedChanged(viewHolder, actionState);

            }

            /**
             * 松手以后，去掉高亮背景色
             */
            @Override
            public void clearView(RecyclerView recyclerView, ViewHolder viewHolder) {
                Log.e(TAG, "clearView()");
                super.clearView(recyclerView, viewHolder);
                viewHolder.itemView.setBackgroundColor(0);
            }
        });
        itemTouchHelper.attachToRecyclerView(mRecyclerView);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        String json = new Gson().toJson(mList);
        preferences.edit().putString("list", json).apply();
    }
}
