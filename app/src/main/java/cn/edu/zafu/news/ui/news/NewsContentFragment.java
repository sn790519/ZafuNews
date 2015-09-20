package cn.edu.zafu.news.ui.news;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;


import com.cocosw.bottomsheet.BottomSheet;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.lsjwzh.widget.materialloadingprogressbar.CircleProgressBar;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import cn.edu.zafu.corepage.core.CoreAnim;
import cn.edu.zafu.news.R;
import cn.edu.zafu.news.db.dao.BaseDao;
import cn.edu.zafu.news.db.helper.DatabaseHelper;
import cn.edu.zafu.news.model.NewsContent;
import cn.edu.zafu.news.db.model.NewsItem;
import cn.edu.zafu.news.common.http.client.NewsOkHttpClient;
import cn.edu.zafu.news.common.parser.impl.ContentParser;
import cn.edu.zafu.news.common.screen.ScreenShot;
import cn.edu.zafu.news.ui.app.ToolbarFragment;
import cn.edu.zafu.news.ui.news.adapter.NewsContentAdapter;

/**
 * User: lizhangqu(513163535@qq.com)
 * Date: 2015-09-19
 * Time: 21:47
 * FIXME
 */
public class NewsContentFragment extends ToolbarFragment implements View.OnClickListener{
    private Toolbar toolbar;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getParams();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view =inflater.inflate(R.layout.fragment_newscontent,container,false);
        showToolbar(view,newsItem.getTitle());
        loadDataFromNetwork(newsItem.getUrl());
        initFAM(view);
        initRecyclerView(view);
        return view;

    }

    private NewsItem newsItem = null;
    private RecyclerView mRecyclerView;
    private NewsContentAdapter mAdapter;
    private CircleProgressBar mCircleProgressBar;
    private FloatingActionMenu fam;
    private FloatingActionButton fabCollect;
    private FloatingActionButton fabShare;
    private FloatingActionButton fabSave;
    private FloatingActionButton fabQrcode;
    private BottomSheet sheet;
    private int mScrollOffset = 4;
    private List<NewsContent> list = new ArrayList<NewsContent>();
    private static final int LOAD_DATA = 0x01;
    private static final int LOAD_ERROR = 0x02;
    private static final int SCREENSHOT = 0x03;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case LOAD_DATA:
                    hideCircleProgressBar(getView());
                    mAdapter.notifyDataSetChanged();
                    fam.setVisibility(View.VISIBLE);
                    // ScreenShot.savePic(ScreenShot.getRecyclerViewScreenshot(mRecyclerView, list), "/sdcard/test.png");
                    break;
                case LOAD_ERROR:
                    Toast.makeText(getActivity(), "加载失败！", Toast.LENGTH_SHORT).show();
                    hideCircleProgressBar(getView());

                    mAdapter.notifyDataSetChanged();
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }

        }
    };

    private void hideCircleProgressBar(View view) {
        if (view!=null) {
            mCircleProgressBar = (CircleProgressBar) view.findViewById(R.id.progressBar);
            if (mCircleProgressBar != null) {
                mCircleProgressBar.setVisibility(View.GONE);
            }
        }
    }
    private void initFAM(View view) {
        fam = (FloatingActionMenu) view.findViewById(R.id.fam);
        fabCollect = (FloatingActionButton) view.findViewById(R.id.collect);
        fabShare = (FloatingActionButton) view.findViewById(R.id.share);
        fabSave = (FloatingActionButton) view.findViewById(R.id.save);
        fabQrcode = (FloatingActionButton) view.findViewById(R.id.qrcode);

        fam.setVisibility(View.GONE);
        fabCollect.setOnClickListener(this);
        fabShare.setOnClickListener(this);
        fabSave.setOnClickListener(this);
        fabQrcode.setOnClickListener(this);

    }

    private void loadDataFromNetwork(String url) {
        OkHttpClient client = NewsOkHttpClient.getInstance();
        final Request request = new Request.Builder()
                .url(url)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {

                handler.sendEmptyMessage(LOAD_ERROR);
            }

            @Override
            public void onResponse(Response response) throws IOException {
                ContentParser contentParser = new ContentParser();
                list.clear();
                list.addAll(contentParser.convert(response.body().string()));
                handler.sendEmptyMessage(LOAD_DATA);
            }
        });

    }


    private void initRecyclerView(View view) {

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);
        //如果布局大小一致有利于优化
        mRecyclerView.setHasFixedSize(true);
        //使用线性布局管理器
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        //初始化适配器并绑定适配器
        mAdapter = new NewsContentAdapter(getActivity(), list);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (Math.abs(dy) > mScrollOffset) {
                    if (dy > 0) {
                        if (fam.isOpened()) {
                            fabCollect.hide(true);
                            fabShare.hide(true);
                            fabSave.hide(true);
                            fabQrcode.hide(true);
                            // fam.close(true);
                        }
                        fam.hideMenuButton(true);
                    } else {
                        if (!fam.isOpened()) {
                            //fabCollect.hide(true);
                            //fabShare.hide(true);
                            // fabSave.hide(true);
                            //fam.close(true);
                            fam.showMenuButton(true);
                        }

                    }
                }
            }
        });
    }

    private void getParams() {
        Bundle bundle = getArguments();
        newsItem = (NewsItem) bundle.get("news_item");
    }

    @Override
    public void onClick(final View v) {
        fam.close(true);
        // fam.hideMenuButton(true);
        v.postDelayed(new Runnable() {
            @Override
            public void run() {
                switch (v.getId()) {
                    case R.id.qrcode:
                       // Intent intent = new Intent(getActivity(), QrcodeActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("data", "zafu|" + newsItem.getUrl() + "|" + newsItem.getTitle());
                      //  intent.putExtras(bundle);
                      //  startActivity(intent);
                        openPage("qrcode",bundle, CoreAnim.slide);

                        break;
                    case R.id.collect:
                        BaseDao<NewsItem, Integer> newsItemDao = DatabaseHelper.getNewsItemDao(getActivity());
                        try {
                            List<NewsItem> query = newsItemDao.query(new String[]{"title", "url"}, new Object[]{newsItem.getTitle(), newsItem.getUrl()});
                            if (query == null||query.size()==0) {
                                newsItemDao.save(newsItem);
                                Toast.makeText(getActivity(), "收藏成功!", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getActivity(), "您已收藏过该篇文章，无需重复收藏！", Toast.LENGTH_SHORT).show();
                            }

                        } catch (SQLException e) {
                            e.printStackTrace();
                        }



                        break;
                    case R.id.share:
                        sheet = getShareActions(new BottomSheet.Builder(getActivity()).grid().title("分享"), newsItem.getTitle() + ":" + newsItem.getUrl()).build();
                        sheet.show();
                        break;
                    case R.id.save:
                        Toast.makeText(getActivity(), "正在保存，请稍后...", Toast.LENGTH_SHORT).show();
                        ScreenShot.savePic(ScreenShot.getRecyclerViewScreenshot(mRecyclerView, list), newsItem.getTitle() + ".png");
                        Toast.makeText(getActivity(), "保存成功，文件位于/sdcard/zafu_news/" + newsItem.getTitle() + ".png", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, 200);



    }

    private BottomSheet.Builder getShareActions(BottomSheet.Builder builder, String text) {
        PackageManager pm = getActivity().getPackageManager();

        final Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, text);
        final List<ResolveInfo> list = pm.queryIntentActivities(shareIntent, 0);

        for (int i = 0; i < list.size(); i++) {
            builder.sheet(i, list.get(i).loadIcon(pm), list.get(i).loadLabel(pm));
        }

        builder.listener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ActivityInfo activity = list.get(which).activityInfo;
                ComponentName name = new ComponentName(activity.applicationInfo.packageName,
                        activity.name);
                Intent newIntent = (Intent) shareIntent.clone();
                newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                newIntent.setComponent(name);
                startActivity(newIntent);
            }
        });
        return builder;
    }
}