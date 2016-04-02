package com.giftextview.demo;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.giftextview.R;
import com.giftextview.demo.ParseUtil.ViewDataEntity;
import com.giftextview.entity.SpanEntity;
import com.giftextview.span.OnTextSpanClickListener;
import com.giftextview.view.GifTextView;
import com.giftextview.view.GifTextViewHelper;

public class MainActivity extends Activity {

	private int mScreenWidth = 0;
	private TextSpanClickListener mListener = null;
	private GifTextViewHelper mHelper = null;
	private ArrayList<ViewDataEntity> mDataList = null;
	private Handler mHandler = null;
	private int mGroupIndex = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mScreenWidth = getWindowManager().getDefaultDisplay().getWidth();
		mListener = new TextSpanClickListener();
		mHelper = GifTextViewHelper.getInstance(this, mScreenWidth,
				mScreenWidth);
		mHandler = new Handler();

		test2();

	}

	@Override
	protected void onResume() {
		super.onResume();
		mHelper.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		mHelper.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mDataList.clear();
		mHelper.onDestroy();
		mHandler.removeCallbacksAndMessages(null);
	}

	public void showDialog(View v) {
		Intent intent = new Intent(this, DialogActivity.class);
		startActivity(intent);
	}

	// *************************************************************************
	// 测试1：单个GifTextView

	private void test1() {

		String data = "捕获Android文本我们需要在ClickSpan的onClick方法中加入自己的控制逻辑，"
				+ "<><2$http://192.168.191.1:8080/WebTest/11.gif><>本文将一个超级"
				+ "<><2$http://192.168.191.1:8080/WebTest/12.gif><>找到你所找, 得到你所想<><1$http://baidu.com/><百度><>"
				+ "totalMemory()这个方法返回的是java虚拟机现在已经从操作系统那里挖过来的内存大小，也就是java虚拟机这个进程当时所占用的所有 内存。如果在运行java的时候没有添加-Xms参数，那"
				+ "<><6$http://192.168.191.1:8080/WebTest/22.rar><>直挖到maxMemory()为止，"
				+ "<><2$http://192.168.191.1:8080/WebTest/13.gif><>所以totalMemory()是慢慢增大的。如果用了-Xms参数，程序在启动"
				+ "<><1$http://192.168.191.1:8080/WebTest/2214.gif><百度知道><>freeMemory()是什么呢，刚才讲到如果在运行java的时候没有添加-Xms参数，那么，在java程"
				+ "<><2$http://192.168.191.1:8080/WebTest/14.gif><><><2$http://192.168.191.1:8080/WebTest/15.gif><>"
				+ "&lt;&gt;&lt;&gt;&lt;&gt;&nbsp; &lt;&gt;&nbsp;<><2$http://192.168.191.1:8080/WebTest/16.gif><>"
				+ " &lt;&gt; &lt;&gt;&nbsp; * @ !打扫打扫打扫&nbsp; 打扫打扫大大松大苏打《》&lt;&gt;das&lt;&gt;"
				+ " 打扫打扫大苏打阿大使大大 asdaｄａｓａｓｄｄａｓｄ２大苏打<><2$http://192.168.191.1:8080/WebTest/17.gif><>"
				+ "　ａｄａｓｄｄａｓｄａｓｄａｓｄ大苏打大苏打．＜＞＜＞＜＞大苏打大苏打２３１３３１２８＆％×％……％＃565%&amp;……&amp;"
				+ "￥￥（*（￥￥%６７６￥＠＃＠！大赛<><-1$mailto:sd!@$#%大大松%^%<><>&amp;*$%$><<>&amp;*$%$\""
				+ " target=_blank>sd!@$#%大大松%^%&lt;&gt;&lt;&gt;&amp;<><2$http://192.168.191.1:8080/WebTest/18.gif><>"
				+ "*$%$><>#@!@!*&amp;&amp;&lt;&gt;&lt;&gt;《》，．＜＞；dasdad大苏打大撒大大"
				+ "<><2$http://192.168.191.1:8080/WebTest/19.gif><>序运行的过程的，内存总是慢慢的从操作系统那里挖的，"
				+ "基本上是用多少挖多少，但是java虚拟机100％的情况下是会稍微多挖一点的，这些挖过来而又没有用上的内存，"
				+ "实际上就是 freeMemory()，<><2$http://192.168.191.1:8080/WebTest/20.gif><><><2$http://192.168.191.1:8080/WebTest/22.gif><>"
				+ "所以freeMemory()的值一般情况下都是很小的，但是如果你在运行java程序的时候使用了-Xms，这个时候因为程序在启动的时候就会无条件的从操作系统中挖-Xms后面定义的内存数，"
				+ "这个时候，<><2$http://192.168.191.1:8080/WebTest/111.jpg><><><2$http://192.168.191.1:8080/WebTest/21.gif><>挖过来的内存可能大部分没用上，所以这个时候freeMemory()可能会有些大。";

		mDataList = ParseUtil.parsing(data);
		ViewDataEntity dataEntity = mDataList.get(0);

		GifTextView gifTextView = (GifTextView) findViewById(R.id.gifTextView);
		gifTextView.setVisibility(View.VISIBLE);
		gifTextView.initView(mGroupIndex, 1, dataEntity.text,
				dataEntity.spanList, mScreenWidth, mScreenWidth, mListener);
	}

	// *************************************************************************
	// 测试2：放入ListView中

	private void test2() {

		String data1 = "<n>【0】.摘要：在2016年，随着实时大数据处理技术被更多的公司接纳使用，"
				+ "必定会对企业的业务分析、人员调整、政策方面带来深远的影响。那些曾在大数据投资的企业也"
				+ "将会在2016年得到回报，<><5$ee_000><>实时大数据分析技术将成为成败的关键。"
				+ "<><2$http://192.168.191.1:8080/WebTest/11.gif><><><5$ee_001><>"
				+ "<><2$http://192.168.191.1:8080/WebTest/12.gif><><><5$ee_002><><n>"
				+ "【1】.很难相信2016年即将要来临，如果社会和商业形势如同电影行业里所预测那样，我们早已驾驶飞行汽"
				+ "车出行……当然，尽管在燃油效率、电动汽车方面取得巨大进展，目前仍旧没有实现飞行汽车的梦想。<><5$ee_003><>"
				+ "不过有一点可以肯定，<><5$ee_003><>在2016年一定会出现一些对企业和社会有着重大的影响新兴的技术。"
				+ "以下是我的一些“预测”：<><2$http://192.168.191.1:8080/WebTest/12.gif><>"
				+ "<><2$http://192.168.191.1:8080/WebTest/13.gif><><><5$ee_004><><><5$ee_004><>"
				+ "<><2$http://192.168.191.1:8080/WebTest/14.gif><><><5$ee_005><>"
				+ "<><2$http://192.168.191.1:8080/WebTest/15.gif><><><5$ee_006><><n>"
				+ "【2】.实时分析将大放异彩<><2$http://192.168.191.1:8080/WebTest/16.gif><>"
				+ "在2016年层出不穷的新技术之中，<><2$http://192.168.191.1:8080/WebTest/17.gif><>"
				+ "实时大数据分析绝对是最为耀眼的那颗珍珠。<><2$http://192.168.191.1:8080/WebTest/18.gif><>"
				+ "Instantly-actionable <><2$http://192.168.191.1:8080/WebTest/18.gif><>"
				+ "分析与Rear-view 数据分析相比不再是一个可选项（而是必备选项）尤其是考虑到消费者和企业的状况。"
				+ "<><2$http://192.168.191.1:8080/WebTest/19.gif><><><5$ee_007><><n>"
				+ "【3】.现在，人人都期望相关且个性化的信息。幸运的是，此类数据的和处理不再被Netflix、"
				+ "Google 或者Amazon等大型云供应商上垄断（目前它已成为主流）。<><5$ee_007><>"
				+ "在2016年，各行各业的公司都有机会获得前所未有的机遇，如改善病人护理、<><5$ee_008><>"
				+ "增加农作物产量以便养活更多的人口。总而言之，各个公司将会更加明智地做出商业决策。<><5$ee_008><><n>"
				+ "【4】.不可预见的领域将会出现新的威胁进而增加了用户的需求<><2$http://192.168.191.1:8080/WebTest/20.gif><>"
				+ "<><2$http://192.168.191.1:8080/WebTest/111.jpg><>随着实时大数据处理的时代来临，新业务的挑战也会随之出现。"
				+ "<><2$http://192.168.191.1:8080/WebTest/21.gif><><><2$http://192.168.191.1:8080/WebTest/22.gif><>"
				+ "<><2$http://192.168.191.1:8080/WebTest/23.gif><>巨大的竞争威胁将自行出现的（而最大的威胁可能来自企业的核心产业），"
				+ "即使那些与你公司业务无关的或者那些从未想到会成为竞争对手的企业将会蚕食你的市场份额。<><5$ee_008><><><5$ee_009><>"
				+ "所以企业必须具备分析数据的能力，预测新兴的威胁，并制定相应的策略应对；<><2$http://192.168.191.1:8080/WebTest/24.gif><>"
				+ "与此同时，企业也应重构和重新评估与客户的互动过程，以保持客户的忠诚度。<><2$http://192.168.191.1:8080/WebTest/25.gif><><n>"
				+ "<n>【5】.多年来企业一直以客户为中心而努力。然而，对大多数客户而言，他们从未看到过投资的回报，并且在当今的大数据时代，“好”已经远远不足以满足客户体验。"
				+ "在2016年，随着新的实时大数据技术到来，更多的公司将会真正地影响当今最为重要的客户体验。<><2$http://192.168.191.1:8080/WebTest/25.gif><>"
				+ "企业能够利用技术推送个性化信息、优惠和服务，以便实现更好的整体客户体验。将日常消费当做重要的事情处理是每个公司都应为之努力的方向；"
				+ "现在，随着实时大数据应用，客户在首次使用时就会感觉到不同企业的差异。<n>"
				+ "【新增1】用来测试会异常吗，哈哈<n>"
				+ "【6】.CIO将加速离职<><2$http://192.168.191.1:8080/WebTest/26.gif><><><1$http://baidu.com/><百度><>"
				+ "在2016年，成功与失败的CIOs之间的差距将会越拉越大。<><2$http://192.168.191.1:8080/WebTest/27.gif><>"
				+ "那些开创性地使用云和大数据的公司CIOs会将这些技术推广更加实用化，并对商业规则的改变有着独特的见解。<><5$ee_010><>"
				+ "那些对此类技术不敏感的CIOs将会和他们的公司一道落后于时代的竞争。<><2$http://192.168.191.1:8080/WebTest/28.gif><>"
				+ "那些早已建立自己大数据平台的公司在2016年的大数据冲刺时将有着巨大的优势。<><2$http://192.168.191.1:8080/WebTest/29.gif><>"
				+ "随着Spark和 Spark 流的到来，他们能够充分发挥在Hadoop上的投资建立的数据仓库的真正潜力。<><5$ee_011><><><5$ee_012><>"
				+ "大数据的拓荒者将在2016年得到他们的投资回报，并且成败CIOs之间的差距将越拉越大。<><2$http://192.168.191.1:8080/WebTest/30.gif><><n>"
				+ "【7】.随着差距的增大，对高素质CIO人才的需求将会进一步提高。随着CIO人才争夺战开始，高水平的CIO将会被哄抢，而水平较低的将会被淘汰。"
				+ "在Talend Connect会议上，已经讨论一些将在2016年数据集成前沿的领军企业。这些领导者采用新的方式将不断增长的数据转化为可操作信息，"
				+ "这不仅提高了他们的业务，而且在很多情况下，也惠及了更广泛的用户。对于那些目前处于落后的公司来说，幸运的是现有的数据集成技术能使得部署 Spark能力更加快速简洁，"
				+ "这意味着能有机会迎头赶上。<n>"
				+ "【8】.<><2$http://192.168.191.1:8080/WebTest/31.gif><><><2$http://192.168.191.1:8080/WebTest/32.gif><>"
				+ "<><2$http://192.168.191.1:8080/WebTest/33.gif><><><2$http://192.168.191.1:8080/WebTest/34.gif><><n>"
				+ "【9】.企业将会重组<><2$http://192.168.191.1:8080/WebTest/34.gif><><><2$http://192.168.191.1:8080/WebTest/35.gif><>"
				+ "现在，实时大数据技术已成为改变商业规则的技术了，并将在2016年产生深远的影响，并也讨论了如不接纳这些新技术带来的不良后果，企业是时候采用此技术以保持领先的地位了。<n>"
				+ "【新增2】千万不要异常<n>"
				+ "【10】.大数据的时代的到临使得企业正在重新考虑他们的组织架构。<><6$http://192.168.191.1:8080/WebTest/22.rar><>"
				+ "实时大数据正在打破传统商业所谓的最佳实践和架构的障碍，“商业+IT”的模式将让位“商业+IT=创新企业”。那些能够弄清楚商业与IT何如合作并加以盈利的公司将会获胜。"
				+ "跨部门的创新中心必将出现，由CEOs、CIOs、CDOs和新涌现的职位CMTOs将利用各种的技能相互合作。这些信息的SWAT部门能转分析为收入，<><5$ee_013><>"
				+ "并驱动着企业开创前所未有的市场，同时也符合所有安全条例和隐私法规。在2016年，公司必须打破桎梏以期接纳实时大数据的下一个阶段，<><5$ee_014><>"
				+ "并将其作为实现贵公司未来一年的成功所在。<><2$http://192.168.191.1:8080/WebTest/36.gif><><><5$ee_015><><n>"
				+ "【11】.期望崭新的一年和技术的创新如约而至！<><2$http://192.168.191.1:8080/WebTest/37.gif><><><5$ee_016><>"
				+ "<><2$http://192.168.191.1:8080/WebTest/38.gif><><><5$ee_017><><><5$ee_018><><n>";

		final String data2 = "<n>【12】.25个Java机器学习工具&库<><2$http://192.168.191.1:8080/WebTest/38.gif><>"
				+ "摘要：本文总结了25个Java机器学习工具&库：Weka集成了数据挖掘工作的机器学习算法、面向数据流挖掘的流行开源框架（MOA）、"
				+ "新型的柔性工作流引擎ADAMS、基于Java的面向文本文件的机器学习工具包Mallet等。<><5$ee_019><><n>"
				+ "【13】.本列表总结了25个Java机器学习工具&库：<><2$http://192.168.191.1:8080/WebTest/39.gif><>"
				+ "<><2$http://192.168.191.1:8080/WebTest/40.gif><>1. Weka集成了数据挖掘工作的机器学习算法。"
				+ "这些算法可以直接应用于一个数据集上或者你可以自己编写代码来调用。Weka包括一系列的工具，如数据预处理、分类、回归、"
				+ "聚类、关联规则以及可视化。<><2$http://192.168.191.1:8080/WebTest/41.gif><><><5$ee_020><><n>"
				+ "【14】.2.Massive Online Analysis（MOA）是一个面向数据流挖掘的流行开源框架，有着非常活跃的成长社区。"
				+ "它包括一系列的机器学习算法（分类、回归、聚类、异常检测、概念漂移检测和推荐系统）和评估工具。关联了WEKA项目，"
				+ "MOA也是用Java编写的，其扩展性更强。<><2$http://192.168.191.1:8080/WebTest/42.gif><>"
				+ "<><2$http://192.168.191.1:8080/WebTest/43.gif><><><2$http://192.168.191.1:8080/WebTest/44.gif><><n>"
				+ "【新增3】异常就苦b了<n>"
				+ "【15】.3.MEKA项目提供了一个面向多标签学习和评价方法的开源实现。在多标签分类中，我们要预测每个输入实例的多个输出变量。"
				+ "这与“普通”情况下只涉及一个单一目标变量的情形不同。此外，MEKA基于WEKA的机器学习工具包。<><5$ee_021><><n>"
				+ "【16】.<><2$http://192.168.191.1:8080/WebTest/45.gif><><><2$http://192.168.191.1:8080/WebTest/46.gif><>"
				+ "<><2$http://192.168.191.1:8080/WebTest/47.gif><><><2$http://192.168.191.1:8080/WebTest/48.gif><>"
				+ "<><2$http://192.168.191.1:8080/WebTest/49.gif><><><2$http://192.168.191.1:8080/WebTest/50.gif><><n>"
				+ "【17】.<><2$http://192.168.191.1:8080/WebTest/50.gif><>4. Advanced Data mining And Machine "
				+ "learning System（ADAMS）是一种新型的柔性工作流引擎，旨在迅速建立并保持真实世界的复杂知识流，它是基于GPLv3发行的。<n>"
				+ "【18】.<><2$http://192.168.191.1:8080/WebTest/51.gif><><2$http://192.168.191.1:8080/WebTest/52.gif>"
				+ "5. Environment for Developing KDD-Applications Supported by Index-Structure（ELKI）是一款基于Java的开源"
				+ "（AGPLv3）数据挖掘软件。ELKI主要集中于算法研究，重点研究聚类分析中的无监督方法和异常检测。<><5$ee_022><><n>"
				+ "【新增4】没异常<n>"
				+ "【19】.<><2$http://192.168.191.1:8080/WebTest/53.gif><><><2$http://192.168.191.1:8080/WebTest/54.gif><>"
				+ "6. Mallet是一个基于Java的面向文本文件的机器学习工具包。<><2$http://192.168.191.1:8080/WebTest/55.gif><>"
				+ "Mallet支持分类算法，如最大熵、朴素贝叶斯和决策树分类。<><2$http://192.168.191.1:8080/WebTest/56.gif><><n>"
				+ "【20】.<><5$ee_023><>7. Encog是一个先进的机器学习框架，集成了支持向量机（SVM）、人工神经网络、遗传算法、贝叶斯网络、隐马尔可夫模型（HMM）、"
				+ "遗传编程和遗传算法。<n>"
				+ "【21】.<><2$http://192.168.191.1:8080/WebTest/57.gif><>8. Datumbox机器学习框架是一个用Java编写的开源框架，允许快速地开发机器学习和统计应用。"
				+ "该框架的核心重点包括大量的机器学习算法以及统计测试，能够处理中等规模的数据集。<><2$http://192.168.191.1:8080/WebTest/58.gif><><n>"
				+ "【22】.<><2$http://192.168.191.1:8080/WebTest/59.gif><>9. Deeplearning4j是使用Java和Scala编写的第一个商业级的、开源的、分布式深入学习库。"
				+ "其设计的目的是用于商业环境中，而不是作为一个研究工具。<><2$http://192.168.191.1:8080/WebTest/60.gif><><n>";

		mDataList = ParseUtil.parsingByAutoSplit(data1);

		final ListView listView = (ListView) findViewById(R.id.listView);
		listView.setVisibility(View.VISIBLE);
		listView.setAdapter(new ListBaseAdapter(this, mDataList));
		mHelper.setAutoDownload(false);

		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				ArrayList<ViewDataEntity> dataList = ParseUtil
						.parsingByAutoSplit(data2);
				mDataList.addAll(dataList);
				mHelper.notifyDataSetChanged(mGroupIndex);
				((ListBaseAdapter) listView.getAdapter())
						.notifyDataSetChanged();
				mHelper.setAutoDownload(true);
			}
		}, 10000);

	}

	private class ListBaseAdapter extends BaseAdapter {

		private ArrayList<ViewDataEntity> dataList = null;
		private LayoutInflater inflater = null;

		public ListBaseAdapter(Context context,
				ArrayList<ViewDataEntity> dataList) {
			inflater = LayoutInflater.from(context);
			this.dataList = dataList;
		}

		@Override
		public int getCount() {
			return dataList.size();
		}

		@Override
		public Object getItem(int position) {
			return dataList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		class ViewHolder {
			GifTextView gifTextView = null;
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {
			ViewHolder holder = null;
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = View.inflate(MainActivity.this,
						R.layout.listview_item, null);
				holder.gifTextView = (GifTextView) convertView
						.findViewById(R.id.gifTextView);
				convertView.setTag(holder);
			} else
				holder = (ViewHolder) convertView.getTag();

			ViewDataEntity dataEntity = dataList.get(position);

			holder.gifTextView.initView(mGroupIndex, position, dataEntity.text,
					dataEntity.spanList, mScreenWidth, mScreenWidth, mListener);

			return convertView;
		}
	}

	private class TextSpanClickListener implements OnTextSpanClickListener {

		@Override
		public void clickLink(GifTextView gifTextView, SpanEntity entity) {
			Log.i("My", "【clickLink】");
		}

		@Override
		public void clickImage(GifTextView gifTextView, SpanEntity entity) {
			Log.i("My", "【clickImage】");
			File file = mHelper.getDownloadPictureFile(entity);
			if (file != null) {
				Toast.makeText(MainActivity.this, "正在打开图片", Toast.LENGTH_SHORT)
						.show();
				Intent intent = new Intent();
				intent.setAction(Intent.ACTION_VIEW);
				intent.setDataAndType(Uri.fromFile(file), "image/*");
				startActivity(intent);
			} else {
				Toast.makeText(MainActivity.this, "正在下载图片", Toast.LENGTH_SHORT)
						.show();
				mHelper.displayPicture(gifTextView, entity);
			}
		}

		@Override
		public void clickGif(GifTextView gifTextView, SpanEntity entity) {
			Log.i("My", "【clickGif】");
			File file = mHelper.getDownloadPictureFile(entity);
			if (file != null) {
				Toast.makeText(MainActivity.this, "正在打开图片", Toast.LENGTH_SHORT)
						.show();
				Intent intent = new Intent();
				intent.setAction(Intent.ACTION_VIEW);
				intent.setDataAndType(Uri.fromFile(file), "image/*");
				startActivity(intent);
			} else {
				Toast.makeText(MainActivity.this, "正在下载图片", Toast.LENGTH_SHORT)
						.show();
				mHelper.displayPicture(gifTextView, entity);
			}
		}

		@Override
		public void clickZip(GifTextView gifTextView, SpanEntity entity) {
			Log.i("My", "【clickZip】");
		}

	}

}
