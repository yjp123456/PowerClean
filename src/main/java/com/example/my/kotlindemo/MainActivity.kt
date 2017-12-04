package com.example.my.kotlindemo


import android.app.Activity
import android.app.AlertDialog
import android.app.AppOpsManager
import android.app.ProgressDialog
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.*
import android.provider.Settings
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.*
import java.io.File
import java.io.FileOutputStream
import java.lang.Double
import java.math.BigDecimal
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.regex.Pattern

class MainActivity : Activity() {

    //val代表常量，相当于final，var代表动态变量
    internal var totalSizeView: TextView? = null//internal代表同一个包名内可以访问，默认是public
    internal var unitView: TextView? = null
    internal var headBar: LinearLayout? = null
    internal var fileList: ListView? = null
    internal var start: Button? = null
    internal var dataList: ArrayList<MyFile>? = null
    internal var fileListAdapter: FileListAdapter? = null
    internal var progressDialog: ProgressDialog? = null
    internal var clean: Button? = null
    private val patterns = arrayListOf<Garbage>()
    private val packagePattern = "[a-z]+([.][a-z]+)+"
    private var totalSize: Long = 0
    private var sizeText: String? = null
    private var unitText: String? = null
    private var exitTime: Long = 0

    private var executorService: ExecutorService? = null
    internal var pk: PackageManager? = null

    var context: Context? = null


    private val handler = object : Handler() {//kotlin 中extends需要带构造函数，implements不需要

        override fun handleMessage(msg: Message) {
            when (msg.what) {//when取代switch
                0 -> {
                    dataList?.clear()
                    fileListAdapter?.notifyDataSetChanged()
                }
                1 -> {
                    progressDialog?.dismiss()
                    fileListAdapter?.notifyDataSetChanged()
                }
                2 -> progressDialog = ProgressDialog.show(context, "Loading...", "Please wait...", true, false)
                3 -> {
                    totalSizeView?.text = sizeText
                    unitView?.text = unitText
                    headBar?.visibility = View.VISIBLE
                }
                4 -> {
                    MIitmCheck(this@MainActivity).check(CountDownLatch(2))
                }
                else -> {
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        context = this
        //kotlin中仍然可以使用所有Java容器，并且去掉了new关键字
        dataList = arrayListOf<MyFile>()
        fileList = findViewById(R.id.fileList) as ListView
        start = findViewById(R.id.start) as Button
        clean = findViewById(R.id.clean) as Button
        totalSizeView = findViewById(R.id.totalSize) as TextView
        unitView = findViewById(R.id.unit) as TextView
        headBar = findViewById(R.id.headBar) as LinearLayout


        //kotlin中?.代表非空，?:代表为空
        start?.setOnClickListener(object : View.OnClickListener {
            override fun onClick(p0: View?) {
                executorService?.execute {
                    handler.sendEmptyMessage(2)
                    startCollectFile()

                }
            }

        })



        clean?.setOnClickListener {//只有一个参数情况下可以去掉最外围的括号，直接使用lamba表达式
            if (totalSize == 0L)
                Toast.makeText(this@MainActivity, "没有可清理数据", Toast.LENGTH_SHORT).show()
            else {
                progressDialog = ProgressDialog.show(this@MainActivity, "Cleanning...", "Please wait...", true, false)
                executorService?.execute {
                    var i = 0
                    while (i < dataList!!.size) {
                        val item: MyFile = dataList!!.get(i)

                        if (item.isChose) {
                            val file = File(item.filePath)
                            if (file.exists())
                                deleteFile(file)
                            totalSize -= item.size
                            dataList?.removeAt(i)
                            i--

                        }
                        i++
                    }
                    handler.sendEmptyMessage(1)
                    getFormatSize(totalSize, true)
                }

            }
        }
        executorService = Executors.newFixedThreadPool(10)
        fileListAdapter = FileListAdapter(this, dataList!!)
        fileList?.adapter = fileListAdapter
        pk = packageManager
        initPatterns()

        fileList?.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, position, id ->
            val inflater = LayoutInflater.from(this@MainActivity)
            val layout = inflater.inflate(R.layout.dialog_junk_cache_detail, null)
            val builder = AlertDialog.Builder(this@MainActivity).setView(layout).setCancelable(false)

            val dialog = builder.create()
            dialog.show()
            val icon = layout.findViewById(R.id.icon) as ImageView
            val headText = layout.findViewById(R.id.header_text) as TextView
            val ok_btn = layout.findViewById(R.id.ok_button) as Button
            val cancel_btn = layout.findViewById(R.id.cancel_button) as Button
            val path = layout.findViewById(R.id.path_text) as TextView
            val size = layout.findViewById(R.id.size_text) as TextView
            val myFile = dataList!![position]
            val appIcon = myFile.icon
            if (appIcon != null)
                icon.setImageDrawable(appIcon)
            else
                icon.setBackgroundResource(R.drawable.ic_launcher)
            headText.text = myFile.name
            path.text = String.format(resources.getString(R.string.apk_detail_path), myFile.filePath)
            size.text = String.format(resources.getString(R.string.size_content), myFile.formatSize)


            ok_btn.setOnClickListener { view ->

                progressDialog = ProgressDialog.show(this@MainActivity, "Cleanning...", "Please wait...", true, false)
                executorService!!.execute {
                    val file = File(myFile.filePath)
                    if (file.exists())
                        deleteFile(file)
                    totalSize -= myFile.size
                    dataList?.removeAt(position)
                    handler.sendEmptyMessage(1)
                    getFormatSize(totalSize, true)
                    dialog.dismiss()
                }
            }
            cancel_btn.setOnClickListener { dialog.dismiss() }
        }

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        getTopApp(this)
        super.onActivityResult(requestCode, resultCode, data)
    }



    private fun getTopApp(context: Context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val m = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager;
                if (m != null) {
                    val now = System.currentTimeMillis();
                    //获取60秒之内的应用数据
                    val stats: List<UsageStats> = m.queryUsageStats(UsageStatsManager.INTERVAL_BEST, now - 60 * 1000, now);

                    var topActivity = "";
                    //取得最近运行的一个app，即当前运行的app
                    if ((stats != null) && (!stats.isEmpty())) {
                        var j = 0;
                        var i = 0
                        for (i in 0..stats.size - 1) {
                            var t = i
                            if (stats.get(i).getLastTimeUsed() > stats.get(j).getLastTimeUsed()) {
                                j = i;
                            }
                        }
                        topActivity = stats.get(j).getPackageName();
                        var k = 2 + 3
                    }

                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

   /* fun copyFile(newPath: String) {
        try {
            var bytesum = 0
            var byteread = 0
            val inStream = resources.assets.open("tt.cer") //读入原文件
            val fs = FileOutputStream(newPath + "/tt.cer")
            val buffer = ByteArray(1444)
            val length: Int
            byteread = inStream.read(buffer)
            while (byteread != -1) {
                bytesum += byteread //字节数 文件大小
                fs.write(buffer, 0, byteread)
                byteread = inStream.read(buffer)

            }
            inStream.close()

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
*/

    override fun onDestroy() {
        executorService?.shutdownNow()
        super.onDestroy()
    }

    private fun initPatterns() {

        patterns.add(Garbage("微信朋友圈数据", "tencent/MicroMsg/.*/sns"))//weixin peng you quan data
        patterns.add(Garbage("微信临时数据", "tencent/MicroMsg/.*/sfs"))//weixin temp data
        patterns.add(Garbage("QQ空间视频缓存", "Android/data/com.tencent.mobileqq/qzone/video_cache/local"))//qq zone video data
        patterns.add(Garbage("QQ空间zip缓存", "Android/data/com.tencent.mobileqq/qzone/zip_cache"))
        patterns.add(Garbage("QQ空间图片缓存", "Android/data/com.qzone/cache/imageV2"))//qq zone image data
        patterns.add(Garbage("缓存数据", ".*(?i)cache"))//cache file

    }

    private fun deleteFile(root: File) {
        for (file in root.listFiles()) {
            if (file.isDirectory) {
                deleteFile(file)
            }
            if (file.exists())
                file.delete()
        }
    }

    private fun startCollectFile() {
        handler.sendEmptyMessage(0)
        totalSize = 0
        val root = File(Environment.getExternalStorageDirectory().absolutePath)
        eachFile(root)
        handler.sendEmptyMessage(1)
        getFormatSize(totalSize, true)

    }

    private fun eachFile(root: File) {
        for (item in root.listFiles()) {
            if (item.isDirectory) {
                var isMatch = false
                val filePath = item.absolutePath
                for (pattern in patterns) {
                    val m = Pattern.compile(pattern.path).matcher(filePath)
                    if (m.find()) {
                        val m2 = Pattern.compile(packagePattern).matcher(m.group(0))
                        val packageName = if (m2.find()) m2.group(0) else null
                        val size = getFolderSize(item)
                        if (size > 0) {
                            totalSize += size
                            var icon: Drawable? = null
                            try {
                                icon = pk?.getApplicationIcon(packageName)
                            } catch (e: PackageManager.NameNotFoundException) {
                                e.printStackTrace()
                            }

                            val myFile = MyFile(pattern.name, getFormatSize(size, false), filePath, packageName, size, icon)
                            dataList?.add(myFile)
                        }
                        isMatch = true
                        break
                    }
                }
                if (!isMatch)
                    eachFile(item)
            }
        }

    }

    fun getFormatSize(size: Long, isTotal: Boolean): String {
        val kiloByte = (size / 1024).toDouble()
        if (kiloByte < 1) {
            if (isTotal) {
                sizeText = size.toString() + ""
                unitText = "Byte(s)"
                handler.sendEmptyMessage(3)
            }
            return size.toString() + "Byte(s)"
        }

        val megaByte = kiloByte / 1024
        if (megaByte < 1) {
            val result1 = BigDecimal(Double.toString(kiloByte))
            val result = result1.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString()
            if (isTotal) {
                sizeText = result
                unitText = "KB"
                handler.sendEmptyMessage(3)

            }
            return result + "KB"
        }

        val gigaByte = megaByte / 1024
        if (gigaByte < 1) {
            val result2 = BigDecimal(Double.toString(megaByte))
            val result = result2.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString()
            if (isTotal) {
                sizeText = result
                unitText = "MB"
                handler.sendEmptyMessage(3)
            }
            return result + "MB"
        }

        val teraBytes = gigaByte / 1024
        if (teraBytes < 1) {
            val result3 = BigDecimal(Double.toString(gigaByte))
            val result = result3.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString()
            if (isTotal) {
                sizeText = result
                unitText = "GB"
                handler.sendEmptyMessage(3)
            }
            return result + "GB"
        }
        val result4 = BigDecimal(teraBytes)
        val result = result4.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString()
        if (isTotal) {
            sizeText = result
            unitText = "TB"
            handler.sendEmptyMessage(3)
        }
        return result + "TB"
    }


    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_DOWN) {
            if (System.currentTimeMillis() - exitTime > 2000) {
                Toast.makeText(applicationContext, "再按一次退出程序", Toast.LENGTH_SHORT).show()
                exitTime = System.currentTimeMillis()
            } else {
                finish()
                System.exit(0)
            }
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    companion object {

        fun getFolderSize(file: File): Long {
            var size: Long = 0
            try {
                val fileList = file.listFiles()
                for (i in fileList.indices) {
                    if (fileList[i].isDirectory) {
                        size = size + getFolderSize(fileList[i])

                    } else {
                        size = size + fileList[i].length()

                    }
                }
            } catch (e: Exception) {
                // TODO Auto-generated catch block
                e.printStackTrace()
            }

            //return size/1048576;
            return size
        }
    }
}


